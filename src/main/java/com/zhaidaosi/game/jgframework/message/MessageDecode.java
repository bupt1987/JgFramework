package com.zhaidaosi.game.jgframework.message;

import com.zhaidaosi.game.jgframework.common.excption.MessageException;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class MessageDecode extends OneToOneDecoder {

    @Override
    protected Object decode(ChannelHandlerContext ctx, Channel channel,
                            Object msg) throws MessageException {
        if (msg instanceof String) {
            msg = InMessage.getMessage((String) msg);
        }
        return msg;
    }

}
