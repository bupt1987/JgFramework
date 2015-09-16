package com.zhaidaosi.game.jgframework.connector;

import com.zhaidaosi.game.jgframework.model.entity.IBaseCharacter;
import io.netty.util.AttributeKey;

public interface IBaseConnector {

    AttributeKey<IBaseCharacter> PLAYER = AttributeKey.valueOf("player");

    void start();

    void stop();

}
