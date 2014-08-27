package com.zhaidaosi.game.jgframework.model.action;

import com.zhaidaosi.game.jgframework.handler.BaseHandlerChannel;


public abstract class BaseAction implements IBaseAction, Cloneable {

    protected int id;
    protected String name;


    public BaseAction(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public abstract void doAction(Object self, Object target, BaseHandlerChannel ch);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public IBaseAction clone() {
        IBaseAction o = null;
        try {
            o = (IBaseAction) super.clone();
        } catch (CloneNotSupportedException e) {
//			e.printStackTrace();
        }
        return o;
    }

}
