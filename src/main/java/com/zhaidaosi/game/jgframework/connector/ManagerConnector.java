package com.zhaidaosi.game.jgframework.connector;

import com.zhaidaosi.game.jgframework.Boot;
import com.zhaidaosi.game.jgframework.common.BaseDate;
import com.zhaidaosi.game.jgframework.common.BaseIp;
import com.zhaidaosi.game.jgframework.message.OutMessage;
import com.zhaidaosi.game.jgframework.session.SessionManager;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.*;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.frame.DelimiterBasedFrameDecoder;
import org.jboss.netty.handler.codec.frame.Delimiters;
import org.jboss.netty.handler.codec.string.StringDecoder;
import org.jboss.netty.handler.codec.string.StringEncoder;
import org.jboss.netty.util.CharsetUtil;
import org.jboss.netty.util.internal.DeadLockProofWorker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

import static org.jboss.netty.channel.Channels.pipeline;

public class ManagerConnector implements IBaseConnector {

    private static final Logger log = LoggerFactory.getLogger(ManagerConnector.class);
    private final InetSocketAddress localAddress;
    private ServerBootstrap bootstrap;

    public ManagerConnector(int port) {
        this.localAddress = new InetSocketAddress(port);
    }

    @Override
    public void start() {
        if (bootstrap != null) {
            return;
        }
        bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(Executors.newCachedThreadPool(), Executors.newCachedThreadPool(), 1));
        bootstrap.setPipelineFactory(new TelnetServerPipelineFactory());
        bootstrap.bind(localAddress);
        log.info("Manager Service is running! port : " + localAddress.getPort());
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


    class TelnetServerPipelineFactory implements ChannelPipelineFactory {

        public ChannelPipeline getPipeline() throws Exception {
            ChannelPipeline pipeline = pipeline();
            pipeline.addLast("framer", new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
            pipeline.addLast("StringEncode", new StringEncoder(CharsetUtil.UTF_8));
            pipeline.addLast("StringDecode", new StringDecoder(CharsetUtil.UTF_8));
            pipeline.addLast("Handler", new MyChannelHandler());
            return pipeline;
        }
    }


    class MyChannelHandler extends SimpleChannelHandler {

        private final String nextLine = "\r\n";
        private boolean close = false;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
            log.error(e.getCause().getMessage(), e.getCause());
        }

        @Override
        public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            Channel ch = e.getChannel();
            String ip = ch.getRemoteAddress().toString();
            String[] ipArr = ip.split(":");
            String realIp = ipArr[0].substring(ipArr[0].indexOf("/") + 1);
            if (!ManagerService.checkIp(realIp)) {
                ch.close();
            } else {
                ctx.getChannel().write("Please input user name:" + nextLine);
                ManagerService.goNextSetp(ch.getId());
                log.info("ManagerConnector connected " + ip);
            }
        }

        @Override
        public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
            Channel ch = e.getChannel();
            ManagerService.clear(ch.getId());
            log.info("ManagerConnector closed " + ch.getRemoteAddress());
        }

        @Override
        public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
            Channel ch = e.getChannel();
            String request = (String) e.getMessage();
            request = request.toLowerCase();
            String response = "Please type something.";
            if (request.length() > 0) {
                Integer cid = ch.getId();
                Integer step = ManagerService.getStep(cid);
                if (step == 1 || step == 2) {
                    response = login(request, step, cid);
                } else {
                    response = handler(request);
                }
            }
            ChannelFuture future = ctx.getChannel().write(response + nextLine);
            if (close) {
                future.addListener(ChannelFutureListener.CLOSE);
            }
        }

        private String handler(String request) {
            String response;
            String[] requestArr = request.split(" ");
            AuthConnector authConnector = Boot.getAuthConnector();
            ServiceConnector serviceConnector = Boot.getServiceConnector();

            switch (requestArr[0]) {
                case "exit":
                case "quit":
                    response = "Have a good day!";
                    close = true;
                    break;
                case "help":
                    response = "#### status : system status" + nextLine;
                    response += "#### start : start all service" + nextLine;
                    response += "#### stop : stop all service" + nextLine;
                    response += "#### start-service : start service" + nextLine;
                    response += "#### stop-service : stop service" + nextLine;
                    response += "#### start-auth : start auth" + nextLine;
                    response += "#### stop-auth : stop auth" + nextLine;
                    response += "#### pause-auth : pause auth" + nextLine;
                    response += "#### resume-auth : resume auth" + nextLine;
                    response += "#### msg : send msg to all users" + nextLine;
                    response += "#### exit/quit : exit";
                    break;
                case "status":
                    ServiceConnector c = Boot.getServiceConnector();
                    long startTime = c.getStartTime();
                    response = nextLine + "#### pid : " + ManagerService.getPid() + nextLine;
                    response += "#### start_time : " + BaseDate.time2String(BaseDate.FORMAT_YY_MM_DD_HH_MM_SS, startTime) + nextLine;
                    response += "#### running time : " + (System.currentTimeMillis() - startTime) / 1000 + "s" + nextLine;
                    response += "#### socket_connect : " + c.getConnectCount() + nextLine;
                    response += "#### socket_wait : " + SessionManager.getWaitCount() + nextLine;
                    response += "#### login_user_count : " + SessionManager.getUserCount() + nextLine;
                    break;
                case "start":
                    serviceConnector.start();
                    if (authConnector != null) {
                        authConnector.start();
                    }
                    response = "start is ok";
                    break;
                case "stop":
                    serviceConnector.stop();
                    if (authConnector != null) {
                        authConnector.stop();
                    }
                    response = "stop is ok";
                    break;
                case "start-service":
                    serviceConnector.start();
                    response = "start service is ok";
                    break;
                case "stop-service":
                    serviceConnector.stop();
                    response = "stop service is ok";
                    break;
                case "start-auth":
                    if (authConnector != null) {
                        authConnector.start();
                    }
                    response = "start auth is ok";
                    break;
                case "stop-auth":
                    if (authConnector != null) {
                        authConnector.stop();
                    }
                    response = "stop auth is ok";
                    break;
                case "pause-auth":
                    if (authConnector != null) {
                        authConnector.pause();
                    }
                    response = "pause auth is ok";
                    break;
                case "resume-auth":
                    if (authConnector != null) {
                        authConnector.resume();
                    }
                    response = "resume auth is ok";
                    break;
                case "msg":
                    if (requestArr.length < 2) {
                        response = "Please input msg.";
                    } else {
                        String msg = "";
                        for (int i = 1; i < requestArr.length; i++) {
                            msg += requestArr[i] + " ";
                        }
                        msg = msg.trim();
                        response = sendMsg(msg);
                    }
                    break;
                default:
                    response = "ERROR";
                    break;
            }
            return response;
        }

        private String sendMsg(String msg) {
            Map<Integer, Channel> channels = SessionManager.getChannels();
            for (Entry<Integer, Channel> entry : channels.entrySet()) {
                Channel ch = entry.getValue();
                if (ch.isWritable()) {
                    ch.write(OutMessage.showSucc(msg));
                }
            }
            return "OK";
        }

        private String login(String request, Integer step, Integer cid) {
            String response;
            if (step == 1) {
                String user = Boot.getManagerUser();
                if (!request.equals(user)) {
                    response = "User name is error, please input again!";
                } else {
                    ManagerService.goNextSetp(cid);
                    response = "Please input password:";
                }
            } else {
                String password = Boot.getManagerPassword();
                if (!request.equals(password)) {
                    response = "Password is error, please input again!";
                } else {
                    ManagerService.goNextSetp(cid);
                    response = "Login success!";
                }
            }
            return response;
        }
    }

    static class ManagerService {

        private static ConcurrentHashMap<Integer, Integer> authStep = new ConcurrentHashMap<>();

        private static int pId = 0;

        static boolean checkIp(String ip) {
            ArrayList<Long[]> ipList = Boot.getManagerAllowIps();
            for (Long[] longs : ipList) {
                if (BaseIp.checkIp(ip, longs)) {
                    return true;
                }
            }
            return false;
        }

        static Integer getStep(Integer cid) {
            Integer setp = authStep.get(cid);
            if (setp == null) {
                setp = 1;
                authStep.put(cid, 1);
            }
            return setp;
        }

        static void goNextSetp(Integer cid) {
            Integer setp = authStep.get(cid);
            if (setp == null) {
                authStep.put(cid, 1);
            } else {
                authStep.put(cid, ++setp);
            }
        }

        static void clear(Integer cid) {
            authStep.remove(cid);
        }

        static int getPid() {
            if (pId == 0) {
                RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();
                String name = runtime.getName();
                try {
                    pId = Integer.parseInt(name.substring(0, name.indexOf('@')));
                } catch (Exception e) {
                    pId = -1;
                }
            }
            return pId;
        }

    }

}
