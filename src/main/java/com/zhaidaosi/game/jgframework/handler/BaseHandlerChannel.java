package com.zhaidaosi.game.jgframework.handler;

import com.zhaidaosi.game.jgframework.message.OutMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.ChannelGroupFuture;

public class BaseHandlerChannel {

    private Channel chanel;
    private ChannelGroup chanelGroup;
    private String handlerName;

    public BaseHandlerChannel(String handlerName, Channel chanel) {
        this.handlerName = handlerName;
        this.chanel = chanel;
    }

    public BaseHandlerChannel(String handlerName, ChannelGroup chanelGroup) {
        this.handlerName = handlerName;
        this.chanelGroup = chanelGroup;
    }

    public Channel getChanel() {
        return chanel;
    }

    public ChannelGroup getChannelGroup() {
        return chanelGroup;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public ChannelFuture write(OutMessage message) {
        message.setH(handlerName);
        if (chanel != null && chanel.isWritable()) {
            return chanel.write(message);
        }
        return null;
    }

    public ChannelGroupFuture writeGroup(OutMessage message) {
        message.setH(handlerName);
        if (chanelGroup != null) {
            return chanelGroup.write(message);
        }
        return null;
    }

}
