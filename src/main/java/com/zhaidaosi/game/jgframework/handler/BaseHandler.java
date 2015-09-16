package com.zhaidaosi.game.jgframework.handler;

import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;
import com.zhaidaosi.game.jgframework.message.OutMessage;
import io.netty.channel.Channel;

public abstract class BaseHandler implements IBaseHandler {

    protected String handlerName;

    @Override
    public abstract IBaseMessage run(InMessage im, Channel ch) throws Exception;

    @Override
    public String getHandlerName() {
        return handlerName;
    }

    @Override
    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public static IBaseMessage doHeart() {
        return OutMessage.showSucc("");
    }

}
