package com.zhaidaosi.game.jgframework.connector;

import com.zhaidaosi.game.jgframework.Boot;
import com.zhaidaosi.game.jgframework.Router;
import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;
import com.zhaidaosi.game.jgframework.message.OutMessage;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsConfig;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class AuthConnector implements IBaseConnector {

    private static final Logger log = LoggerFactory.getLogger(AuthConnector.class);
    private final InetSocketAddress localAddress;
    private ServerBootstrap bootstrap;
    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private String charset = Boot.getCharset().name();
    private boolean isPause = false;

    public AuthConnector(int port) {
        this.localAddress = new InetSocketAddress(port);
    }

    @Override
    public void start() {
        if (bootstrap != null) {
            return;
        }

        bootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);

        if (Boot.getAuthThreadCount() > 0) {
            workerGroup = new NioEventLoopGroup(Boot.getAuthThreadCount());
        } else {
            workerGroup = new NioEventLoopGroup();
        }

        try {
            bootstrap.group(bossGroup, workerGroup)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .channel(NioServerSocketChannel.class)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.SO_REUSEADDR, true)
                    .childHandler(new HttpServerInitializer())
                    .bind(localAddress);
            log.info("Auth Service is running! port : " + localAddress.getPort());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    void pause() {
        isPause = true;
    }

    void resume() {
        isPause = false;
    }

    @Override
    public void stop() {
        if (bootstrap == null) {
            return;
        }
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }

    private class HttpServerInitializer extends ChannelInitializer<SocketChannel> {

        @Override
        public void initChannel(SocketChannel ch) {
            CorsConfig corsConfig = CorsConfig.withAnyOrigin().build();
            ChannelPipeline p = ch.pipeline();
            p.addLast(new HttpResponseEncoder());
            p.addLast(new HttpRequestDecoder());
            p.addLast(new HttpObjectAggregator(65536));
            p.addLast(new ChunkedWriteHandler());
            p.addLast(new CorsHandler(corsConfig));
            p.addLast(new HttpHandler());
        }

    }

    private class HttpHandler extends ChannelInboundHandlerAdapter {

        private InMessage inMsg;

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            if (isPause) {
                sendHttpResponse(ctx, OutMessage.showError("系统已关闭", 11000).toString(), true);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) throws Exception {
            String error = t.getMessage();
            log.error(error, t);
            sendHttpResponse(ctx, OutMessage.showError("系统错误:" + error, 10000).toString(), true);
            ctx.close();
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            BaseRunTimer.showTimer();
        }

        @Override
        public void channelReadComplete(ChannelHandlerContext ctx) {
            ctx.flush();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;
                if (request.getMethod() != POST) {
                    sendErrorHttpResponse(ctx, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                    return;
                }
                inMsg = new InMessage(Boot.getAuthHandler() + request.getUri().substring(1).replace("/", "\\.").toLowerCase());
            }

            long startTime = 0;
            if (BaseRunTimer.isActive()) {
                startTime = System.currentTimeMillis();
            }

            if (msg instanceof HttpContent) {

                HttpContent httpContent = (HttpContent) msg;
                ByteBuf content = httpContent.content();
                String post = content.toString(Boot.getCharset());
                content.release();

                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(post, Boot.getCharset(), false);
                Map<String, List<String>> params = queryStringDecoder.parameters();
                if (!params.isEmpty()) {
                    for (Entry<String, List<String>> p : params.entrySet()) {
                        String key = p.getKey();
                        List<String> vals = p.getValue();
                        inMsg.putMember(key, vals.get(0));
                    }
                }
                IBaseMessage rs = Router.run(inMsg, ctx.channel());

                if (BaseRunTimer.isActive()) {
                    long runningTime = System.currentTimeMillis() - startTime;
                    BaseRunTimer.addTimer("AuthConnector messageReceived run " + runningTime + " ms");
                }
                if (rs == null) {
                    sendErrorHttpResponse(ctx, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
                } else {
                    sendHttpResponse(ctx, rs.toString(), true);
                }
            }
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, String res, boolean isJson) {
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, OK, Unpooled.wrappedBuffer(res.getBytes()));
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
//            response.headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            if (isJson) {
                response.headers().set(CONTENT_TYPE, "application/json; charset=" + charset);
            } else {
                response.headers().set(CONTENT_TYPE, "text/html; charset=" + charset);
            }
            ctx.channel().write(response).addListener(ChannelFutureListener.CLOSE);
        }

        private void sendErrorHttpResponse(ChannelHandlerContext ctx, DefaultFullHttpResponse response) {
            response.headers().set(CONTENT_LENGTH, response.content().readableBytes());
            ctx.channel().write(response).addListener(ChannelFutureListener.CLOSE);
        }

    }

}
