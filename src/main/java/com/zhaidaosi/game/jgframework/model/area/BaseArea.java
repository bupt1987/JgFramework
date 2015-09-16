package com.zhaidaosi.game.jgframework.model.area;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.entity.BaseNpc;
import com.zhaidaosi.game.jgframework.model.entity.BasePlayer;
import com.zhaidaosi.game.jgframework.model.entity.IBaseCharacter;
import com.zhaidaosi.game.jgframework.model.entity.IBaseEntity;
import com.zhaidaosi.game.jgframework.model.map.IBaseMap;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器启动时通过AreaManager自动初始化的，不需要重复new的
 *
 * @author Jerry
 */
public abstract class BaseArea implements IBaseArea {

    protected int id;
    protected String name;
    protected boolean open = true;
    protected IBaseMap map;
    protected Map<Integer, IBaseEntity> entitys = new HashMap<>();
    protected Map<Integer, IBaseEntity> npcs = new HashMap<>();
    protected Map<Integer, IBaseCharacter> players = new HashMap<>();
    protected ChannelGroup channelGroup;
    protected final Object lock = new Object();
    protected BasePosition entrancePosition = new BasePosition(this);

    public BaseArea(String name) {
        this.id = hashCode();
        this.name = name;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    public BaseArea(int id, String name) {
        this.id = id;
        this.name = name;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    public BaseArea(String name, BasePosition entrancePosition) {
        this.id = hashCode();
        this.name = name;
        this.entrancePosition = entrancePosition;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    public BaseArea(int id, String name, BasePosition entrancePosition) {
        this.id = id;
        this.name = name;
        this.entrancePosition = entrancePosition;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    public BaseArea(String name, IBaseMap map) {
        this.id = hashCode();
        this.name = name;
        this.map = map;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    public BaseArea(int id, String name, IBaseMap map) {
        this.id = id;
        this.name = name;
        this.map = map;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    public BaseArea(String name, BasePosition entrancePosition, IBaseMap map) {
        this.id = hashCode();
        this.name = name;
        this.map = map;
        this.entrancePosition = entrancePosition;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    public BaseArea(int id, String name, BasePosition entrancePosition, IBaseMap map) {
        this.id = id;
        this.name = name;
        this.map = map;
        this.entrancePosition = entrancePosition;
        this.channelGroup = new DefaultChannelGroup(name, GlobalEventExecutor.INSTANCE);
    }

    @Override
    public abstract void init();

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void open() {
        this.open = true;
        init();
    }

    @Override
    public void close() {
        this.open = false;
        channelGroup.clear();
        entitys.clear();
        players.clear();
        npcs.clear();
        map = null;
    }

    @Override
    public int getId() {
        return id;
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
    public IBaseMap getMap() {
        return map;
    }

    @Override
    public IBaseEntity getEntity(int id) {
        return entitys.get(id);
    }

    @Override
    public Map<Integer, IBaseEntity> getEntities() {
        return entitys;
    }

    @Override
    public void addEntity(IBaseEntity entity) {
        entitys.put(entity.getId(), entity);
    }

    @Override
    public void removeEntity(int id) {
        entitys.remove(id);
    }

    @Override
    public IBaseCharacter getPlayer(int id) {
        return players.get(id);
    }

    @Override
    public Collection<IBaseCharacter> getPlayers() {
        return players.values();
    }

    @Override
    public void addPlayer(BasePlayer player) {
        int id = player.getId();
        synchronized (lock) {
            IBaseArea area = player.gArea();
            if (area != null) {
                area.removePlayer(id);
            }
            players.put(id, player);
            channelGroup.add(player.gChannel());
            player.sPosition(entrancePosition);
        }
    }

    @Override
    public void removePlayer(int id) {
        synchronized (lock) {
            IBaseCharacter player = players.get(id);
            if (player != null) {
                players.remove(id);
                channelGroup.remove(player.gChannel());
            }
        }
    }

    @Override
    public IBaseEntity getNpc(int id) {
        return npcs.get(id);
    }

    @Override
    public void addNpc(BaseNpc npc) {
        npcs.put(npc.getId(), npc);
    }

    @Override
    public void removeNpc(int id) {
        npcs.remove(id);
    }

    @Override
    public ChannelGroup getChannelGroup() {
        return channelGroup;
    }

}
