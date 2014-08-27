package com.zhaidaosi.game.jgframework.connector;

import com.zhaidaosi.game.jgframework.Boot;
import com.zhaidaosi.game.jgframework.Router;
import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;
import com.zhaidaosi.game.jgframework.message.OutMessage;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.*;
import org.jboss.netty.util.internal.DeadLockProofWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.ACCESS_CONTROL_ALLOW_ORIGIN;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
import static org.jboss.netty.handler.codec.http.HttpMethod.POST;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;

public class AuthConnector implements IBaseConnector {

    private static final Logger log = LoggerFactory.getLogger(AuthConnector.class);
    private final InetSocketAddress localAddress;
    public static final String POST_USERNAME = "username";
    public static final String POST_PASSWORD = "password";
    private MyChannelHandler myHandler = new MyChannelHandler();
    private ServerBootstrap bootstrap;
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
        if (Boot.getAuthThreadCount() > 0) {
            bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), Boot.getAuthThreadCount()));
        } else {
            bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool()));
        }
        // Enable TCP_NODELAY to handle pipelined requests without latency.
        bootstrap.setOption("child.tcpNoDelay", true);
        bootstrap.setOption("child.reuseAddress", true);
        // Set up the event pipeline factory.
        bootstrap.setPipelineFactory(new HttpServerPipelineFactory());
        bootstrap.bind(localAddress);
        log.info("Auth Service is running! port : " + localAddress.getPort());
    }

    public void pause() {
        isPause = true;
    }

    public void resume() {
        isPause = false;
    }

    @Override
    public void stop() {
        if (bootstrap == null) {
            return;
        }
        bootstrap.releaseExternalResources();
        bootstrap = null;
        DeadLockProofWorker.PARENT.remove();
    }

    class HttpServerPipelineFactory implements ChannelPipelineFactory {

        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = pipeline();
            // Uncomment the following line if you want HTTPS
            // SSLEngine engine =
            // SecureChatSslContextFactory.getServerContext().createSSLEngine();
            // engine.setUseClientMode(false);
            // pipeline.addLast("ssl", new SslHandler(engine));
            pipeline.addLast("HttpRequestDecoder", new HttpRequestDecoder());
            // Uncomment the following line if you don't want to handle
            // HttpChunks.
            pipeline.addLast("HttpChunkAggregator", new HttpChunkAggregator(1048576));
            pipeline.addLast("HttpResponseEncoder", new HttpResponseEncoder());
            // Remove the following line if you don't want automatic content
            // compression.
            // pipeline.addLast("HttpContentCompressor", new
            // HttpContentCompressor());
            pipeline.addLast("Handler", myHandler);
            return pipeline;
        }
    }

    class MyChannelHandler extends SimpleChannelHandler {

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            if (isPause) {
                sendHttpResponse(ctx, OutMessage.showError("系统已关闭", 11000).toString(), true);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            Throwable cause = e.getCause();
            String error = cause.getMessage();
            log.error(error, cause);
            sendHttpResponse(ctx, OutMessage.showError("系统错误:" + error, 10000).toString(), true);
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            BaseRunTimer.showTimer();
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

            HttpRequest request = (HttpRequest) e.getMessage();

            if (request.getMethod() != POST) {
                sendErrorHttpResponse(ctx, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
                return;
            }

            long startTime = 0;
            if (BaseRunTimer.isActive()) {
                startTime = System.currentTimeMillis();
            }

            ChannelBuffer content = request.getContent();
            String path = request.getUri().substring(1).replace("/", "\\.").toLowerCase();
            InMessage imsg = new InMessage(Boot.getAuthHandler() + path);
            if (content.readable()) {
                String post = content.toString(Boot.getCharset());
                QueryStringDecoder queryStringDecoder = new QueryStringDecoder(post, Boot.getCharset(), false);
                Map<String, List<String>> params = queryStringDecoder.getParameters();
                if (!params.isEmpty()) {
                    for (Entry<String, List<String>> p : params.entrySet()) {
                        String key = p.getKey();
                        List<String> vals = p.getValue();
                        imsg.putMember(key, vals.get(0));
                    }
                }
            }
            IBaseMessage rs = Router.run(imsg, ctx.getChannel());

            if (BaseRunTimer.isActive()) {
                long runningTime = System.currentTimeMillis() - startTime;
                BaseRunTimer.addTimer("AuthConnector messageReceived run " + runningTime + " ms");
            }
            if (rs == null) {
                sendErrorHttpResponse(ctx, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
            } else {
                sendHttpResponse(ctx, rs.toString(), true);
            }
        }

        private void sendHttpResponse(ChannelHandlerContext ctx, String res, boolean isJson) {
            HttpResponse response = new DefaultHttpResponse(HTTP_1_1, OK);
            ChannelBuffer content = ChannelBuffers.copiedBuffer(res, Boot.getCharset());
            response.setContent(content);
            setContentLength(response, content.readableBytes());
            response.setHeader(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
            if (isJson) {
                response.setHeader(CONTENT_TYPE, "application/json; charset=" + charset);
            } else {
                response.setHeader(CONTENT_TYPE, "text/html; charset=" + charset);
            }
            ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        }

        private void sendErrorHttpResponse(ChannelHandlerContext ctx, HttpResponse response) {
            ChannelBuffer content = ChannelBuffers.copiedBuffer(response.getStatus().toString(), Boot.getCharset());
            response.setContent(content);
            setContentLength(response, content.readableBytes());
            ctx.getChannel().write(response).addListener(ChannelFutureListener.CLOSE);
        }

    }

}
