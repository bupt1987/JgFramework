package com.zhaidaosi.game.jgframework.model.entity;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.action.IBaseAction;
import com.zhaidaosi.game.jgframework.model.area.IBaseArea;
import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class BasePlayer implements IBaseCharacter {

    protected int id;
    protected Channel channel;
    protected String name;
    protected String roll;
    protected int level;
    protected int experience;
    protected int totalHp;
    protected int totalMp;
    protected int hp;
    protected int mp;
    protected BasePosition position;
    protected Map<Integer, IBaseAction> actions = new HashMap<Integer, IBaseAction>();
    protected boolean isInQueue = false;

    @Override
    public void loginHook() {
        if (channel == null || position == null) {
            return;
        }
        IBaseArea area = position.getArea();
        if (area == null) {
            return;
        }
        area.addPlayer(this);
    }

    @Override
    public void logoutHook() {
        if (channel == null || position == null) {
            return;
        }
        IBaseArea area = position.getArea();
        if (area == null) {
            return;
        }
        area.removePlayer(id);
    }

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
    public IBaseArea gArea() {
        if (position != null) {
            return position.getArea();
        }
        return null;
    }

    @Override
    public BasePosition gPosition() {
        return position;
    }

    @Override
    public void sPosition(BasePosition position) {
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
    public Map<Integer, IBaseAction> getActions() {
        return actions;
    }

    @Override
    public void setActions(Map<Integer, IBaseAction> actions) {
        this.actions = actions;
    }

    @Override
    public IBaseAction findActionById(int id) {
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

    @Override
    public Channel gChannel() {
        return channel;
    }

    @Override
    public void sChannel(Channel channel) {
        this.channel = channel;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getExperience() {
        return experience;
    }

    @Override
    public void setExperience(int experience) {
        this.experience = experience;
    }

    @Override
    public int getTotalHp() {
        return totalHp;
    }

    @Override
    public void setTotalHp(int totalHp) {
        this.totalHp = totalHp;
    }

    @Override
    public int getTotalMp() {
        return totalMp;
    }

    @Override
    public void setTotalMp(int totalMp) {
        this.totalMp = totalMp;
    }

    @Override
    public int getHp() {
        return hp;
    }

    @Override
    public void setHp(int hp) {
        this.hp = hp;
    }

    @Override
    public int getMp() {
        return mp;
    }

    @Override
    public void setMp(int mp) {
        this.mp = mp;
    }

    @Override
    public boolean isInQueue() {
        return isInQueue;
    }

    @Override
    public void setIsInQueue(boolean isInQueue) {
        this.isInQueue = isInQueue;
    }

}
