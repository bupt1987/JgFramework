package com.zhaidaosi.game.jgframework.handler;

import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;
import io.netty.channel.Channel;

public interface IBaseHandler {

    IBaseMessage run(InMessage im, Channel ch) throws Exception;

    String getHandlerName();

    void setHandlerName(String handlerName);

}
