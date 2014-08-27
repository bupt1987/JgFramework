package com.zhaidaosi.game.jgframework.model.entity;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.action.IBaseAction;

import java.util.HashMap;
import java.util.Map;

public class BaseNpc implements IBaseEntity {

    protected int id;
    protected String name;
    protected Map<Integer, IBaseAction> actions = new HashMap<Integer, IBaseAction>();
    protected String roll;
    protected BasePosition position;

    @Override
    public int getId() {
        return id;
    }

    @Override
    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String getRoll() {
        return roll;
    }

    @Override
    public void setRoll(String roll) {
        this.roll = roll;
    }

    @Override
    public BasePosition getPosition() {
        return position;
    }

    @Override
    public void setPosition(BasePosition position) {
        this.position = position;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public IBaseAction getAction(int id) {
        return actions.get(id);
    }

    @Override
    public void addAction(IBaseAction action) {
        actions.put(action.getId(), action);
    }

    @Override
    public void removeAction(int id) {
        actions.remove(id);
    }

}
