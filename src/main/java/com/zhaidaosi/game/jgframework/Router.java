package com.zhaidaosi.game.jgframework;

import com.zhaidaosi.game.jgframework.common.BaseFile;
import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.common.BaseString;
import com.zhaidaosi.game.jgframework.handler.BaseHandler;
import com.zhaidaosi.game.jgframework.handler.IBaseHandler;
import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;
import com.zhaidaosi.game.jgframework.message.OutMessage;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Set;

public class Router {

    private static final Logger log = LoggerFactory.getLogger(Router.class);
    private static HashMap<String, IBaseHandler> handlers = new HashMap<>();
    private static final String classSuffix = "Handler";
    public static final String ERROR_HANDLERNAME = "_error_";
    public static final String HEART_HANDLERNAME = "_heart_";
    public static final String WAIT_HANDLERNAME = "_wait_";

    /**
     * 初始化handler
     */
    public static void init() {
        Set<Class<?>> classes = BaseFile.getClasses(Boot.SERVER_HANDLER_PACKAGE_PATH, classSuffix, true);

        for (Class<?> c : classes) {
            String className = c.getName();
            IBaseHandler handler;
            try {
                handler = (IBaseHandler) c.newInstance();
                // 只取Handler前面部分
                String handlerName = className.replace(Boot.SERVER_HANDLER_PACKAGE_PATH + ".", "").replace(classSuffix, "").toLowerCase();
                handler.setHandlerName(handlerName);
                handlers.put(handlerName, handler);
                log.info("handler类 : " + className + " 加载完成");
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("handler类 : " + className + " 加载失败", e);
            }
        }
    }

    /**
     * 运行handler
     * @param im
     * @param ch
     * @return
     * @throws Exception
     */
    public static IBaseMessage run(InMessage im, Channel ch) throws Exception {
        long startTime = 0;
        if (BaseRunTimer.isActive()) {
            startTime = System.currentTimeMillis();
        }
        IBaseMessage rs = null;
        String handlerName = im.getH();
        if (!BaseString.isEmpty(handlerName)) {
            try {
                if (handlerName.equals(HEART_HANDLERNAME)) {
                    rs = BaseHandler.doHeart();
                    rs.setH(HEART_HANDLERNAME);
                } else {
                    IBaseHandler handler = handlers.get(handlerName);
                    if (handler == null) {
                        rs = OutMessage.showError("handler不存在", 21000);
                    } else {
                        rs = handler.run(im, ch);
                    }
                    if (rs != null) {
                        rs.setH(handlerName);
                    }
                }
            } finally {
                if (BaseRunTimer.isActive()) {
                    long runningTime = System.currentTimeMillis() - startTime;
                    BaseRunTimer.addTimer("Handler : " + handlerName + " run " + runningTime + " ms");
                }
            }
        } else {
            rs = OutMessage.showError("handler不能为空", 20000);
            rs.setH(ERROR_HANDLERNAME);
        }
        return rs;
    }

}
