package com.zhaidaosi.game.jgframework.model.action;

import com.zhaidaosi.game.jgframework.handler.BaseHandlerChannel;


public interface IBaseAction {

    int getId();

    void doAction(Object self, Object target, BaseHandlerChannel ch);

    String getName();

    void setName(String name);

    IBaseAction clone();

}
