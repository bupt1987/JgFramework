package com.zhaidaosi.game.jgframework.model.area;

import com.zhaidaosi.game.jgframework.model.entity.BaseNpc;
import com.zhaidaosi.game.jgframework.model.entity.BasePlayer;
import com.zhaidaosi.game.jgframework.model.entity.IBaseCharacter;
import com.zhaidaosi.game.jgframework.model.entity.IBaseEntity;
import com.zhaidaosi.game.jgframework.model.map.IBaseMap;
import org.jboss.netty.channel.group.ChannelGroup;

import java.util.Collection;
import java.util.Map;

public interface IBaseArea {

    public void init();

    public boolean isOpen();

    public void open();

    public void close();

    public int getId();

    public String getName();

    public void setName(String name);

    public IBaseMap getMap();

    public IBaseEntity getEntity(int id);

    public Map<Integer, IBaseEntity> getEntities();

    public void addEntity(IBaseEntity entity);

    public void removeEntity(int id);

    public IBaseCharacter getPlayer(int id);

    public Collection<IBaseCharacter> getPlayers();

    public void addPlayer(BasePlayer player);

    public void removePlayer(int id);

    public IBaseEntity getNpc(int id);

    public void addNpc(BaseNpc npc);

    public void removeNpc(int id);

    public ChannelGroup getChannelGroup();


}
