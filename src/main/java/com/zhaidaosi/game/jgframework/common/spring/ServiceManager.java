package com.zhaidaosi.game.jgframework.common.spring;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class ServiceManager {

    private static ClassPathXmlApplicationContext context = null;

    public static void init() {
        context = new ClassPathXmlApplicationContext("applicationContext.xml");
    }

    public static <T> Object getService(String id) {
        return context.getBean(id);
    }

}
