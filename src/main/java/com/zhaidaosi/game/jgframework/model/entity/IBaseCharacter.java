package com.zhaidaosi.game.jgframework.model.entity;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.action.IBaseAction;
import com.zhaidaosi.game.jgframework.model.area.IBaseArea;
import io.netty.channel.Channel;

import java.util.Map;

public interface IBaseCharacter {

    int getId();

    void setId(int id);

    String getRoll();

    void setRoll(String roll);

    BasePosition gPosition();

    void sPosition(BasePosition position);

    IBaseArea gArea();

    String getName();

    void setName(String name);

    IBaseAction findActionById(int id);

    void addAction(IBaseAction action);

    void setActions(Map<Integer, IBaseAction> actions);

    Map<Integer, IBaseAction> getActions();

    void removeAction(int id);

    Channel gChannel();

    void sChannel(Channel channel);

    int getLevel();

    void setLevel(int level);

    int getExperience();

    void setExperience(int experience);

    int getTotalHp();

    void setTotalHp(int totalHp);

    int getTotalMp();

    void setTotalMp(int totalMp);

    int getHp();

    void setHp(int hp);

    int getMp();

    void setMp(int mp);

    void logoutHook();

    void loginHook();

    boolean isInQueue();

    void setIsInQueue(boolean isInQueue);

}
