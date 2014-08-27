package com.zhaidaosi.game.jgframework.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright (c) 2013, zhidaosi.com
 *
 * @description: 记录运行时间
 * @author: 俊杰Jerry
 * @date: 2013-7-5
 */
public class BaseRunTimer {

    private static ThreadLocal<List<String>> timerHolder = new ThreadLocal<List<String>>();

    private static final Log log = LogFactory.getLog(BaseRunTimer.class);

    private static boolean active = false;

    public static void addTimer(String msg) {
        if (active) {
            Assert.notNull(msg, "msg cannot be null");
            List<String> timer = getTimer();
            timer.add(msg);
            timerHolder.set(timer);
        }
    }

    private static List<String> getTimer() {
        List<String> timer = (List<String>) timerHolder.get();
        if (timer == null) {
            timer = new ArrayList<String>();
        }
        return timer;
    }

    public static void showTimer() {
        if (active && log.isInfoEnabled()) {
            List<String> timer = getTimer();
            if (timer.size() > 0) {
                String line = "**********************************************************************";
                String message = "\n" + line + "\n";
                for (String msg : timer) {
                    message += "********** ";
                    message += msg;
                    message += "\n";
                }
                message += line;
                log.info(message);
            }
        }
        timerHolder.remove();
    }

    public static boolean isActive() {
        return active;
    }

    public static void toActive() {
        BaseRunTimer.active = true;
    }


}


