package com.zhaidaosi.game.jgframework;

import com.zhaidaosi.game.jgframework.common.BaseIp;
import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.common.BaseString;
import com.zhaidaosi.game.jgframework.common.cache.BaseLocalCached;
import com.zhaidaosi.game.jgframework.common.encrpt.BaseDes;
import com.zhaidaosi.game.jgframework.common.encrpt.BaseRsa;
import com.zhaidaosi.game.jgframework.common.spring.ServiceManager;
import com.zhaidaosi.game.jgframework.connector.AuthConnector;
import com.zhaidaosi.game.jgframework.connector.ManagerConnector;
import com.zhaidaosi.game.jgframework.connector.ServiceConnector;
import com.zhaidaosi.game.jgframework.model.action.ActionManager;
import com.zhaidaosi.game.jgframework.model.area.AreaManager;
import com.zhaidaosi.game.jgframework.model.entity.BasePlayerFactory;
import com.zhaidaosi.game.jgframework.model.entity.IBasePlayerFactory;
import com.zhaidaosi.game.jgframework.session.SessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.util.TimeZone;

public class Boot {

    public static String BASE_PACKAGE_NAME;
    public static String SDM_BASE_PACKAGE_NAME;
    public static String SERVER_RSYNC_PACKAGE_PATH;
    public static String SERVER_HANDLER_PACKAGE_PATH;

    private static final Logger log = LoggerFactory.getLogger(Boot.class);
    private final static String SERVER_PROPERTIES = "jgframework.properties";
    private final static String ALL_STRING = "all";
    private final static String SERVICE_STRING = "service";
    private final static String AUTH_STRING = "auth";
    private static String actionPackage;
    private static String areaPackage;
    private static int servicePort = 28080;
    private static String serviceMode = ServiceConnector.MODE_WEB_SOCKET;
    private static long serviceSyncPeriod = 60000;
    private static int serviceThreadCount = 0;
    private static int serviceHeartbeatTime = 60;
    private static int serviceMaxLoginUser = 0;
    private static ArrayList<String> serviceIps = new ArrayList<>();
    private static int authPort = 18080;
    private static int authThreadCount = 0;
    private static String authHandler;
    private static boolean debug = true;
    private static ArrayList<String> memcacheServers = new ArrayList<>();
    private static String memcacheKeyPrefix = "jg-";
    private static Charset charset = Charset.forName("UTF-8");
    private static int managerPort = 38080;
    private static ArrayList<Long[]> managerAllowIps = new ArrayList<>();
    private static String managerUser = "admin";
    private static String managerPassword = "admin";
    private static ServiceConnector serviceConnector = null;
    private static ManagerConnector managerConnector = null;
    private static AuthConnector authConnector = null;
    private static String args = null;
    private static IBasePlayerFactory playerFactory = new BasePlayerFactory();
    private static boolean useSpring = true;

    private static boolean init = false;

    /**
     * 初始化参数
     * @return
     */
    private static boolean init() {
        if (init) {
            return true;
        }
        boolean error = false;
        try {
            Properties pps = new Properties();
            pps.load(Boot.class.getClassLoader().getResourceAsStream(SERVER_PROPERTIES));

            BASE_PACKAGE_NAME = pps.getProperty("base.package.name");
            if (BaseString.isEmpty(BASE_PACKAGE_NAME)) {
                error = true;
                log.error("base.package.name 必须填写");
            }
            SDM_BASE_PACKAGE_NAME = BASE_PACKAGE_NAME + ".sdm";
            SERVER_RSYNC_PACKAGE_PATH = BASE_PACKAGE_NAME + ".rsync";
            SERVER_HANDLER_PACKAGE_PATH = BASE_PACKAGE_NAME + ".handler";

            //设置默认时间
            if (!BaseString.isEmpty(pps.getProperty("time.zone"))) {
                TimeZone.setDefault(TimeZone.getTimeZone(pps.getProperty("time.zone")));
            }

            //是否使用spring
            if (!BaseString.isEmpty(pps.getProperty("useSpring"))) {
                useSpring = pps.getProperty("useSpring").equals("true");
            }

            //设置运行模式
            debug = "debug".equals(pps.getProperty("run.mode"));

            if (debug) {
                BaseRunTimer.toActive();
            }
            if (ALL_STRING.equals(args) || SERVICE_STRING.equals(args)) {
                //service端
                if (!BaseString.isEmpty(pps.getProperty("service.port"))) {
                    servicePort = Integer.valueOf(pps.getProperty("service.port"));
                }
                if (!BaseString.isEmpty(pps.getProperty("service.mode"))) {
                    serviceMode = pps.getProperty("service.mode");
                }
                if (!BaseString.isEmpty(pps.getProperty("service.threadCount"))) {
                    serviceThreadCount = Integer.valueOf(pps.getProperty("service.threadCount"));
                }
                if (!BaseString.isEmpty(pps.getProperty("service.syncPeriod"))) {
                    serviceSyncPeriod = Integer.valueOf(pps.getProperty("service.syncPeriod"));
                    if (serviceSyncPeriod < 3) {
                        error = true;
                        log.error("service.syncPeriod 必须大于3秒");
                    }
                    serviceSyncPeriod = serviceSyncPeriod * 1000;
                }
                if (!BaseString.isEmpty(pps.getProperty("service.heartbeatTime"))) {
                    serviceHeartbeatTime = Integer.valueOf(pps.getProperty("service.heartbeatTime"));
                }
                if (!BaseString.isEmpty(pps.getProperty("service.maxLoginUser"))) {
                    serviceMaxLoginUser = Integer.valueOf(pps.getProperty("service.maxLoginUser"));
                }
            }

            if (ALL_STRING.equals(args) || AUTH_STRING.equals(args)) {
                //auth端
                if (!BaseString.isEmpty(pps.getProperty("service.ips"))) {
                    Collections.addAll(serviceIps, pps.getProperty("service.ips").split(";"));
                } else {
                    error = true;
                    log.error("service.ips 必须填写");
                }
                if (!BaseString.isEmpty(pps.getProperty("auth.port"))) {
                    authPort = Integer.valueOf(pps.getProperty("auth.port"));
                }
                if (!BaseString.isEmpty(pps.getProperty("auth.threadCount"))) {
                    authThreadCount = Integer.valueOf(pps.getProperty("auth.threadCount"));
                }
                if (!BaseString.isEmpty(pps.getProperty("auth.handler"))) {
                    authHandler = pps.getProperty("auth.handler");
                } else {
                    error = true;
                    log.error("auth.handler 必须填写");
                }
            }

            //manager端
            if (!BaseString.isEmpty(pps.getProperty("manager.port"))) {
                managerPort = Integer.valueOf(pps.getProperty("manager.port"));
            }
            if (!BaseString.isEmpty(pps.getProperty("manager.user"))) {
                managerUser = pps.getProperty("manager.user");
            }
            if (!BaseString.isEmpty(pps.getProperty("manager.password"))) {
                managerPassword = pps.getProperty("manager.password");
            }
            String[] ips;
            if (!BaseString.isEmpty(pps.getProperty("manager.allowIps"))) {
                ips = pps.getProperty("manager.allowIps").split(";");
            } else {
                ips = new String[]{"127.0.0.1"};
            }
            setManagerAllowIps(ips);

            //memcache
            if (!BaseString.isEmpty(pps.getProperty("memcache.servers"))) {
                Collections.addAll(memcacheServers, pps.getProperty("memcache.servers").split(";"));
            } else {
                memcacheServers.add("127.0.0.1:11211,1");
            }
            if (!BaseString.isEmpty(pps.getProperty("memcache.keyPrefix"))) {
                memcacheKeyPrefix = pps.getProperty("memcache.keyPrefix");
            }

            //des密钥
            if (!BaseString.isEmpty(pps.getProperty("des.key"))) {
                BaseDes.setDesKey(pps.getProperty("des.key"));
            }
            //rsa密钥
            if (!BaseString.isEmpty(pps.getProperty("rsa.publicKey")) && !BaseString.isEmpty(pps.getProperty("rsa.privateKey"))) {
                BaseRsa.init(pps.getProperty("rsa.publicKey"), pps.getProperty("rsa.privateKey"));
            }
            //系统字符集
            if (!BaseString.isEmpty(pps.getProperty("charset"))) {
                charset = Charset.forName(pps.getProperty("charset"));
            }

        } catch (Exception e) {
            error = true;
            log.error("系统启动失败", e);
        }
        if (!error) {
            init = true;
        }
        return init;
    }

    /**
     * 设置允许ip段
     * @param ips
     */
    private static void setManagerAllowIps(String[] ips) {
        for (String ip : ips) {
            managerAllowIps.add(BaseIp.stringToIp(ip));
        }
    }

    /**
     * 设置扫描action的包路径，例如：com.zhaidaosi.game.server.model.action
     * @param actionPackage
     */
    public static void setActionPackage(String actionPackage) {
        Boot.actionPackage = actionPackage;
    }

    /**
     * 设置扫描area的包路径，例如：com.zhaidaosi.game.server.model.area
     * @param areaPackage
     */
    public static void setAreaPackage(String areaPackage) {
        Boot.areaPackage = areaPackage;
    }

    /**
     * 获取player的工厂，默认是BasePlayerFactory
     * @return
     */
    public static IBasePlayerFactory getPlayerFactory() {
        return playerFactory;
    }

    /**
     * 设置player的工厂，默认是BasePlayerFactory
     * @param playerFactory
     */
    public static void setPlayerFactory(IBasePlayerFactory playerFactory) {
        Boot.playerFactory = playerFactory;
    }

    /**
     * 设置mc的前缀
     * @return
     */
    public static String getMemcacheKeyPrefix() {
        return memcacheKeyPrefix;
    }

    /**
     * 获取service的连接对象
     * @return
     */
    public static ServiceConnector getServiceConnector() {
        return serviceConnector;
    }

    /**
     * 获取auth的连接对象
     * @return
     */
    public static AuthConnector getAuthConnector() {
        return authConnector;
    }

    /**
     * 获取manager服务端口
     * @return
     */
    public static int getManagerPort() {
        return managerPort;
    }

    /**
     * 获取manager允许访问的ip段
     * @return
     */
    public static ArrayList<Long[]> getManagerAllowIps() {
        return managerAllowIps;
    }

    /**
     * 获取manager的登录用户
     * @return
     */
    public static String getManagerUser() {
        return managerUser;
    }

    /**
     * 获取manager的用户密码
     * @return
     */
    public static String getManagerPassword() {
        return managerPassword;
    }


    /**
     * 获取默认字符集
     * @return
     */
    public static Charset getCharset() {
        return charset;
    }

    /**
     * service服务器工作线程数
     * @return
     */
    public static int getServiceThreadCount() {
        return serviceThreadCount;
    }

    /**
     * 认证服务器工作线程数
     * @return
     */
    public static int getAuthThreadCount() {
        return authThreadCount;
    }

    /**
     * 返回所有的memcache配置
     * @return
     */
    public static ArrayList<String> getMemcacheServers() {
        return memcacheServers;
    }

    /**
     * 返回所有service服务器ip
     * @return
     */
    public static ArrayList<String> getServiceIps() {
        return serviceIps;
    }

    /**
     * 返回认证服务的所有handler
     * @return
     */
    public static String getAuthHandler() {
        return authHandler;
    }

    /**
     * 返回服务器端口
     * @return
     */
    public static int getServicePort() {
        return servicePort;
    }

    /**
     * 返回service服务器个数
     * @return
     */
    public static int getServiceCount() {
        return serviceIps.size();
    }

    /**
     * 返回service的同步间隔
     * @return
     */
    public static long getServiceSyncPeriod() {
        return serviceSyncPeriod;
    }

    /**
     * 获取心跳检查时间
     * @return
     */
    public static int getServiceHeartbeatTime() {
        return serviceHeartbeatTime;
    }

    /**
     * 通过ip返回service的连接地址
     * @param ip
     * @return
     */
    public static String getServiceAddress(String ip) {
        if (serviceMode.equals(ServiceConnector.MODE_SOCKET)) {
            return ip + ":" + servicePort;
        } else {
            return "ws://" + ip + ":" + servicePort + ServiceConnector.WEB_SOCKET_PATH;
        }
    }

    /**
     * 是否开启debug模式
     * @return
     */
    public static boolean getDebug() {
        return debug;
    }


    /**
     * 重启服务
     */
    public static void restart() {
        stop();
        start();
    }

    /**
     * 停止服务
     */
    public static void stop() {
        BaseLocalCached.cancelTimer();
        if (authConnector != null) {
            authConnector.stop();
        }
        if (serviceConnector != null) {
            serviceConnector.stop();
        }
        if (managerConnector != null) {
            managerConnector.stop();
        }
    }

    /**
     * 启动服务
     */
    public static void start() {
        start(Boot.args);
    }

    /**
     * 启动服务
     * @param args ALL_STRING,SERVICE_STRING,AUTH_STRING
     */
    public static void start(String args) {
        if (Boot.args == null) {
            Boot.args = args == null ? ALL_STRING : args;
        }
        if (!Boot.ALL_STRING.equals(Boot.args) && !Boot.SERVICE_STRING.equals(Boot.args) & !Boot.AUTH_STRING.equals(Boot.args)) {
            Boot.args = args = ALL_STRING;
        }

        //初始化参数
        if (init()) {
            //加载spring
            if (useSpring) {
                ServiceManager.init();
            }
            //开启本地缓存清理任务
            BaseLocalCached.startTimer();
            //加载路由
            Router.init();

            if (args == null || args.equals(ALL_STRING) || args.equals(SERVICE_STRING)) {
                //加载动作
                ActionManager.initAction(actionPackage);
                //加载区域
                AreaManager.initArea(areaPackage);

                if (serviceMaxLoginUser > 0) {
                    SessionManager.setMaxUser(serviceMaxLoginUser);
                } else {
                    serviceMaxLoginUser = 0;
                }

                if (serviceConnector == null) {
                    serviceConnector = new ServiceConnector(servicePort, serviceSyncPeriod, serviceMode);
                }
                serviceConnector.start();

                if (managerConnector == null) {
                    managerConnector = new ManagerConnector(managerPort);
                }
                managerConnector.start();

            }
            if (args == null || args.equals(ALL_STRING) || args.equals(AUTH_STRING)) {
                if (authConnector == null) {
                    authConnector = new AuthConnector(authPort);
                }
                authConnector.start();
            }
        }
    }

}
