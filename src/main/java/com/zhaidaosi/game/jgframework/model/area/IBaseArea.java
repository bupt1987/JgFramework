package com.zhaidaosi.game.jgframework.model.area;

import com.zhaidaosi.game.jgframework.model.entity.BaseNpc;
import com.zhaidaosi.game.jgframework.model.entity.BasePlayer;
import com.zhaidaosi.game.jgframework.model.entity.IBaseCharacter;
import com.zhaidaosi.game.jgframework.model.entity.IBaseEntity;
import com.zhaidaosi.game.jgframework.model.map.IBaseMap;
import io.netty.channel.group.ChannelGroup;

import java.util.Collection;
import java.util.Map;

public interface IBaseArea {

    void init();

    boolean isOpen();

    void open();

    void close();

    int getId();

    String getName();

    void setName(String name);

    IBaseMap getMap();

    IBaseEntity getEntity(int id);

    Map<Integer, IBaseEntity> getEntities();

    void addEntity(IBaseEntity entity);

    void removeEntity(int id);

    IBaseCharacter getPlayer(int id);

    Collection<IBaseCharacter> getPlayers();

    void addPlayer(BasePlayer player);

    void removePlayer(int id);

    IBaseEntity getNpc(int id);

    void addNpc(BaseNpc npc);

    void removeNpc(int id);

    ChannelGroup getChannelGroup();

}
