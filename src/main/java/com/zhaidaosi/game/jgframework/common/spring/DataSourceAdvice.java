package com.zhaidaosi.game.jgframework.common.spring;

import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.common.sdm.BaseService;
import org.springframework.aop.AfterReturningAdvice;
import org.springframework.aop.MethodBeforeAdvice;
import org.springframework.aop.ThrowsAdvice;

import java.lang.reflect.Method;

public class DataSourceAdvice implements MethodBeforeAdvice,
        AfterReturningAdvice, ThrowsAdvice {

    private long startTime;

    public void before(Method method, Object[] args, Object target) {
        if (BaseRunTimer.isActive()) {
            startTime = System.currentTimeMillis();
        }
        String database = null;
        if (target instanceof BaseService) {
            database = ((BaseService) target).getDatabase();
        }
        if (method.getName().startsWith("save")
                || method.getName().startsWith("add")
                || method.getName().startsWith("update")
                || method.getName().startsWith("delete")) {
            DataSourceSwitcher.setMaster(database);
        } else {
            DataSourceSwitcher.setSlave(database);
        }
    }

    public void afterReturning(Object arg0, Method method, Object[] args,
                               Object target) {
        DataSourceSwitcher.clearDataSource();
        this.setRunTime(method, target);
    }

    public void afterThrowing(Method method, Object[] args, Object target,
                              Exception ex) {
        DataSourceSwitcher.clearDataSource();
        this.setRunTime(method, target);
    }

    private void setRunTime(Method method, Object target) {
        if (BaseRunTimer.isActive()) {
            long runningTime = System.currentTimeMillis() - startTime;
            BaseRunTimer.addTimer(target.getClass().getName() + "."
                    + method.getName() + " run " + runningTime + " ms");
        }
    }

}
