package com.zhaidaosi.game.jgframework.connector;

import com.zhaidaosi.game.jgframework.Boot;
import com.zhaidaosi.game.jgframework.Router;
import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.common.excption.MessageException;
import com.zhaidaosi.game.jgframework.message.*;
import com.zhaidaosi.game.jgframework.model.entity.IBaseCharacter;
import com.zhaidaosi.game.jgframework.rsync.RsyncManager;
import com.zhaidaosi.game.jgframework.session.SessionManager;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.handler.codec.http.websocketx.*;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.handler.timeout.ReadTimeoutException;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.internal.DeadLockProofWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class ServiceConnector implements IBaseConnector {

    private static final Logger log = LoggerFactory.getLogger(ServiceConnector.class);
    private final InetSocketAddress localAddress;
    private ServerBootstrap bootstrap;
    private Timer timer;
    private final long period;
    private final String mode;
    private Object lock = new Object();
    private int connectCount = 0;
    private long startTime;
    public static final String MODE_SOCKET = "socket";
    public static final String MODE_WBSOCKET = "websocket";
    public static final String WEBSOCKET_PATH = "/websocket";

    private int heartbeatTime = Boot.getServiceHeartbeatTime();
    private HashedWheelTimer hashedWheelTimer = new HashedWheelTimer();

    public long getStartTime() {
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
        if (Boot.getServiceThreadCount() > 0) {
            bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), Boot.getServiceThreadCount()));
        } else {
            bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        }
        if (mode.equals(MODE_SOCKET)) {
            bootstrap.setPipelineFactory(new SocketServerPipelineFactory());
        } else if (mode.equals(MODE_WBSOCKET)) {
            bootstrap.setPipelineFactory(new WebSocketServerPipelineFactory());
        } else {
            log.error("Service 运行模式设置错误,必须为" + MODE_SOCKET + "或" + MODE_WBSOCKET);
        }

        SessionManager.init();
        RsyncManager.init();
        bootstrap.bind(localAddress);
        timer = new Timer("RsyncManagerTimer");
        timer.schedule(new MyTimerTask(), period, period);
        startTime = System.currentTimeMillis();
        log.info("Connect Service is running! port : " + localAddress.getPort());
    }

    @Override
    public void stop() {
        if (bootstrap == null) {
            return;
        }
        bootstrap.releaseExternalResources();
        bootstrap = null;
        DeadLockProofWorker.PARENT.remove();
        timer.cancel();
        timer = null;
        SessionManager.destroy();
        RsyncManager.run();
    }

    class SocketServerPipelineFactory implements ChannelPipelineFactory {

        @Override
        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = pipeline();
            if (!Boot.getDebug()) {
                pipeline.addLast("ReadTimeoutHandler", new ReadTimeoutHandler(hashedWheelTimer, heartbeatTime));
            }
            pipeline.addLast("StringEncode", new StringEncoder(Boot.getCharset()));
            pipeline.addLast("StringDecode", new StringDecoder(Boot.getCharset()));
            pipeline.addLast("MessageEncode", new MessageEncode());
            pipeline.addLast("MessageDecode", new MessageDecode());
            pipeline.addLast("Handler", new ServiceChannelHandler());
            return pipeline;
        }

    }

    class WebSocketServerPipelineFactory implements ChannelPipelineFactory {

        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = pipeline();
            if (!Boot.getDebug()) {
                pipeline.addLast("ReadTimeoutHandler", new ReadTimeoutHandler(hashedWheelTimer, heartbeatTime));
            }
            pipeline.addLast("HttpRequestDecoder", new HttpRequestDecoder());
            pipeline.addLast("HttpChunkAggregator", new HttpChunkAggregator(65536));
            pipeline.addLast("HttpResponseEncoder", new HttpResponseEncoder());
            pipeline.addLast("WebSocketMessageEncode", new WebSocketMessageEncode());
            pipeline.addLast("Handler", new ServiceChannelHandler());
            return pipeline;
        }
    }

    class MyTimerTask extends TimerTask {
        @Override
        public void run() {
            log.info("start rsync ...");
            RsyncManager.run();
        }
    }

    class ServiceChannelHandler extends SimpleChannelHandler {

        private WebSocketServerHandshaker handshaker;
        private IBaseCharacter player;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            Throwable t = e.getCause();
            String errorMsg = t.getMessage();
            if (!(t instanceof ClosedChannelException)) {
                Channel ch = ctx.getChannel();
                if (t instanceof IOException && "远程主机强迫关闭了一个现有的连接。".equals(errorMsg)) {
                    // 过滤客户端强制关闭连接造成的异常
                } else if (t instanceof MessageException) {
                    log.error(errorMsg);
                } else if (t instanceof ReadTimeoutException) {
                    log.error("强制关闭超时连接  => " + ch.getRemoteAddress());
                } else if (t instanceof IllegalArgumentException && "empty text".equals(errorMsg)) {
                    // 过滤不是使用websocket连接时造成的异常
                } else {
                    log.error(errorMsg, t);
                }
                ch.close();
            }
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            SessionManager.removeSession(ctx.getChannel());
            synchronized (lock) {
                connectCount--;
            }
            BaseRunTimer.showTimer();
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            synchronized (lock) {
                connectCount++;
            }
            player = Boot.getPlayerFactory().getPlayer();
            player.sChannel(ctx.getChannel());
            ctx.getChannel().setAttachment(player);
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            long startTime = 0;
            if (BaseRunTimer.isActive()) {
                startTime = System.currentTimeMillis();
            }
            Object msg = e.getMessage();
            if (msg instanceof HttpRequest) {
                handleHttpRequest(ctx, (HttpRequest) msg);
            } else {
                InMessage imsg = null;
                IBaseMessage rs = null;
                Channel ch = ctx.getChannel();
                if (msg instanceof WebSocketFrame) {
                    if (msg instanceof CloseWebSocketFrame) {
                        handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) msg);
                        return;
                    }
                    if (msg instanceof PingWebSocketFrame) {
                        ch.write(new PongWebSocketFrame(((WebSocketFrame) msg).getBinaryData()));
                        return;
                    }
                    if (!(msg instanceof TextWebSocketFrame)) {
                        throw new UnsupportedOperationException(String.format("%s msg types not supported", msg.getClass().getName()));
                    }
                    imsg = InMessage.getMessage(((TextWebSocketFrame) msg).getText());
                } else if (msg instanceof IBaseMessage) {
                    imsg = (InMessage) msg;
                }

                boolean error = true;
                if (!SessionManager.isAuthHandler(imsg)) {
                    int result = SessionManager.checkSession(imsg, ch);
                    if (result != SessionManager.ADD_SESSION_ERROR) {
                        error = false;
                        if (result == SessionManager.ADD_SESSION_SUCC) {
                            rs = Router.run(imsg, ch);
                        } else {
                            rs = SessionManager.getWaitMessage(player);
                        }
                    }
                }

                if (error) {
                    log.error("强制关闭没授权的连接  => " + ch.getRemoteAddress());
                    ch.close();
                } else {
                    if (rs != null && ch.isWritable()) {
                        ch.write(rs);
                    }
                }
            }
            if (BaseRunTimer.isActive()) {
                long runningTime = System.currentTimeMillis() - startTime;
                BaseRunTimer.addTimer("ServiceConnector messageReceived run " + runningTime + " ms");
                BaseRunTimer.showTimer();
            }
        }

        private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
            if (req.getMethod() != GET) {
                sendHttpResponse(ctx, req, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }
            if (!WEBSOCKET_PATH.equals(req.getUri()) || !"websocket".equals(req.getHeader(UPGRADE))) {
                HttpResponse res = new DefaultHttpResponse(HTTP_1_1, FORBIDDEN);
                sendHttpResponse(ctx, req, res);
                return;
            }
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
            } else {
                handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
            }
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
            if (res.getStatus().getCode() != 200) {
                res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
                setContentLength(res, res.getContent().readableBytes());
            }
            ctx.getChannel().write(res).addListener(ChannelFutureListener.CLOSE);
        }

        private String getWebSocketLocation(HttpRequest req) {
            return "ws://" + req.getHeader(HOST) + WEBSOCKET_PATH;
        }
    }

}
