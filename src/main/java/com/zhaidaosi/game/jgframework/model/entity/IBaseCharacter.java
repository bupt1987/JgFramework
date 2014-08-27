package com.zhaidaosi.game.jgframework.model.entity;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.action.IBaseAction;
import com.zhaidaosi.game.jgframework.model.area.IBaseArea;
import org.jboss.netty.channel.Channel;

import java.util.Map;

public interface IBaseCharacter {

    public int getId();

    public void setId(int id);

    public String getRoll();

    public void setRoll(String roll);

    public BasePosition gPosition();

    public void sPosition(BasePosition position);

    public IBaseArea gArea();

    public String getName();

    public void setName(String name);

    public IBaseAction findActionById(int id);

    public void addAction(IBaseAction action);

    public void setActions(Map<Integer, IBaseAction> actions);

    public Map<Integer, IBaseAction> getActions();

    public void removeAction(int id);

    public Channel gChannel();

    public void sChannel(Channel channel);

    public int getLevel();

    public void setLevel(int level);

    public int getExperience();

    public void setExperience(int experience);

    public int getTotalHp();

    public void setTotalHp(int totalHp);

    public int getTotalMp();

    public void setTotalMp(int totalMp);

    public int getHp();

    public void setHp(int hp);

    public int getMp();

    public void setMp(int mp);

    public void logoutHook();

    public void loginHook();

    public boolean isInQueue();

    public void setIsInQueue(boolean isInQueue);

}
