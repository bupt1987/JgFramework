package com.zhaidaosi.game.jgframework.model.entity;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.action.IBaseAction;

public interface IBaseEntity {

    int getId();

    void setId(int id);

    String getRoll();

    void setRoll(String roll);

    BasePosition getPosition();

    void setPosition(BasePosition position);

    String getName();

    void setName(String name);

    IBaseAction getAction(int id);

    void addAction(IBaseAction action);

    void removeAction(int id);

}
