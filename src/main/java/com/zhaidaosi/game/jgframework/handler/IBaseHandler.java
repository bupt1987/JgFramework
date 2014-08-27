package com.zhaidaosi.game.jgframework.handler;

import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;
import org.jboss.netty.channel.Channel;

public interface IBaseHandler {

    public IBaseMessage run(InMessage im, Channel ch) throws Exception;

    public String getHandlerName();

    public void setHandlerName(String handlerName);

}
