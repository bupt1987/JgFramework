package com.zhaidaosi.game.jgframework.connector;

import com.zhaidaosi.game.jgframework.Boot;
import com.zhaidaosi.game.jgframework.Router;
import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.common.excption.MessageException;
import com.zhaidaosi.game.jgframework.message.*;
import com.zhaidaosi.game.jgframework.model.entity.IBaseCharacter;
import com.zhaidaosi.game.jgframework.rsync.RsyncManager;
import com.zhaidaosi.game.jgframework.session.SessionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.handler.timeout.ReadTimeoutException;

import static io.netty.handler.codec.http.HttpHeaders.Names.*;
import static io.netty.handler.codec.http.HttpMethod.*;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.*;

import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;


public class ServiceConnector implements IBaseConnector {

    private static final Logger log = LoggerFactory.getLogger(ServiceConnector.class);
    private final InetSocketAddress localAddress;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private ServerBootstrap bootstrap;
    private Timer timer;
    private final long period;
    private final String mode;
    private final Object lock = new Object();
    private int connectCount = 0;
    private long startTime;
    public static final String MODE_SOCKET = "socket";
    public static final String MODE_WEB_SOCKET = "websocket";
    public static final String WEB_SOCKET_PATH = "/websocket";

    private int heartbeatTime = Boot.getServiceHeartbeatTime();

    long getStartTime() {
        return startTime;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public ServiceConnector(int port, long period, String mode) {
        this.localAddress = new InetSocketAddress(port);
        this.period = period;
        this.mode = mode;
    }

    public void start() {
        if (bootstrap != null) {
            return;
        }
        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);

        if (Boot.getServiceThreadCount() > 0) {
            workerGroup = new NioEventLoopGroup(Boot.getServiceThreadCount());
        } else {
            workerGroup = new NioEventLoopGroup();
        }

        try {
            bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class);

            switch (mode) {
                case MODE_SOCKET:
                    bootstrap.childHandler(new SocketServerInitializer());
                    break;
                case MODE_WEB_SOCKET:
                    bootstrap.childHandler(new WebSocketServerInitializer());
                    break;
                default:
                    log.error("Service 运行模式设置错误,必须为" + MODE_SOCKET + "或" + MODE_WEB_SOCKET);
                    return;
            }

            SessionManager.init();
            RsyncManager.init();
            timer = new Timer("SyncManagerTimer");
            timer.schedule(new MyTimerTask(), period, period);
            startTime = System.currentTimeMillis();
            bootstrap.option(ChannelOption.TCP_NODELAY, true)
                    .option(ChannelOption.RCVBUF_ALLOCATOR, AdaptiveRecvByteBufAllocator.DEFAULT)
                    .option(ChannelOption.SO_RCVBUF, 128 * 1024)
                    .option(ChannelOption.SO_SNDBUF, 128 * 1024)
                    .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                    .bind(localAddress);
            log.info("Connect Service is running! port : " + localAddress.getPort());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (bootstrap == null) {
            return;
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        bootstrap = null;
        timer.cancel();
        timer = null;
        SessionManager.destroy();
        RsyncManager.run();
    }

    private class SocketServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            if (!Boot.getDebug()) {
                pipeline.addLast(new IdleStateHandler(heartbeatTime * 2, 0, heartbeatTime, TimeUnit.SECONDS));
            }
            pipeline.addLast(new StringEncoder(Boot.getCharset()));
            pipeline.addLast(new StringDecoder(Boot.getCharset()));
            pipeline.addLast(new MessageEncode());
            pipeline.addLast(new MessageDecode());
            pipeline.addLast(new ServiceChannelHandler());
        }

    }

    private class WebSocketServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline pipeline = ch.pipeline();
            if (!Boot.getDebug()) {
                pipeline.addLast(new IdleStateHandler(heartbeatTime * 2, 0, heartbeatTime, TimeUnit.SECONDS));
            }
            pipeline.addLast(
                    new HttpResponseEncoder(),
                    new HttpRequestDecoder(),
                    new HttpObjectAggregator(65536),
                    new WebSocketEncode(),
                    new ServiceChannelHandler()
            );
        }
    }

    private class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            log.info("start sync ...");
            RsyncManager.run();
        }
    }

    private class ServiceChannelHandler extends ChannelInboundHandlerAdapter {

        private WebSocketServerHandshaker handshake;
        private IBaseCharacter player;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception {
            String errorMsg = t.getMessage();
            if (!(t instanceof ClosedChannelException)) {
                Channel ch = ctx.channel();
                if (t instanceof MessageException) {
                    log.error(errorMsg);
                } else if (t instanceof ReadTimeoutException) {
                    log.error("强制关闭超时连接  => " + ch.remoteAddress());
                } else {
                    log.error(errorMsg, t);
                }
                ch.close();
            }
        }

        @Override
        public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
           /*心跳处理*/
            if (evt instanceof IdleStateEvent) {
                IdleStateEvent event = (IdleStateEvent) evt;
                if (event.state() == IdleState.READER_IDLE) {
                    log.debug("close read time out connect => " + ctx.channel().remoteAddress());
                    ctx.disconnect();
                } else if (event.state() == IdleState.WRITER_IDLE) {
                    log.debug("close write time out connect => " + ctx.channel().remoteAddress());
                } else if (event.state() == IdleState.ALL_IDLE) {
                    ctx.writeAndFlush(new PingWebSocketFrame());
                }
            }
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            synchronized (lock) {
                connectCount++;
            }
            player = Boot.getPlayerFactory().getPlayer();
            player.sChannel(ctx.channel());
            ctx.channel().attr(IBaseConnector.PLAYER).set(player);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            SessionManager.removeSession(ctx.channel());
            synchronized (lock) {
                connectCount--;
            }
            BaseRunTimer.showTimer();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            long startTime = 0;
            if (BaseRunTimer.isActive()) {
                startTime = System.currentTimeMillis();
            }
            try {
                if (msg instanceof FullHttpRequest) {
                    handleHttpRequest(ctx, (FullHttpRequest) msg);
                } else {
                    handleWebSocketRequest(ctx, msg);
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
            if (BaseRunTimer.isActive()) {
                long runningTime = System.currentTimeMillis() - startTime;
                BaseRunTimer.addTimer("ServiceConnector messageReceived run " + runningTime + " ms");
                BaseRunTimer.showTimer();
            }
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        private void handleWebSocketRequest(ChannelHandlerContext ctx, Object msg) throws Exception {
            InMessage inMsg = null;
            IBaseMessage rs = null;
            Channel ch = ctx.channel();
            if (msg instanceof WebSocketFrame) {
                if (msg instanceof CloseWebSocketFrame) {
                    handshake.close(ctx.channel(), ((CloseWebSocketFrame) msg).retain());
                    return;
                }
                if (msg instanceof PingWebSocketFrame) {
                    ctx.write(new PongWebSocketFrame(((PingWebSocketFrame) msg).content().retain()));
                    return;
                }
                if (msg instanceof PongWebSocketFrame) {
                    return;
                }
                if (!(msg instanceof TextWebSocketFrame)) {
                    throw new UnsupportedOperationException(String.format("%s msg types not supported", msg.getClass().getName()));
                }
                inMsg = InMessage.getMessage(((TextWebSocketFrame) msg).text());
            } else if (msg instanceof IBaseMessage) {
                inMsg = (InMessage) msg;
            }

            boolean error = true;
            if (!SessionManager.isAuthHandler(inMsg)) {
                int result = SessionManager.checkSession(inMsg, ch);
                if (result != SessionManager.ADD_SESSION_ERROR) {
                    error = false;
                    if (result == SessionManager.ADD_SESSION_SUCC) {
                        rs = Router.run(inMsg, ch);
                    } else {
                        rs = SessionManager.getWaitMessage(player);
                    }
                }
            }

            if (error) {
                log.error("强制关闭没授权的连接  => " + ch.remoteAddress());
                ch.close();
            } else if (rs != null && ch.isWritable()) {
                ch.write(rs);
            }
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
            // Handle a bad request.
            if (!req.getDecoderResult().isSuccess()) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
                return;
            }
            if (req.getMethod() != GET) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }
            if (!WEB_SOCKET_PATH.equals(req.getUri())) {
                sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            handshake = wsFactory.newHandshaker(req);
            if (handshake == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshake.handshake(ctx.channel(), req);
            }
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
            // Generate an error page if response getStatus code is not OK (200).
            if (res.getStatus().code() != 200) {
                ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
                res.content().writeBytes(buf);
                buf.release();
                HttpHeaders.setContentLength(res, res.content().readableBytes());
            }

            // Send the response and close the connection if necessary.
            ChannelFuture f = ctx.channel().writeAndFlush(res);
            if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
                f.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private String getWebSocketLocation(HttpRequest req) {
            return "ws://" + req.headers().get(HOST) + WEB_SOCKET_PATH;
        }
    }

}
