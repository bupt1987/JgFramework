package com.zhaidaosi.game.jgframework.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class MessageDecode extends MessageToMessageDecoder<Object> {

    @Override
    protected void decode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        if (msg instanceof String) {
            msg = InMessage.getMessage((String) msg);
        }
        out.add(msg);
    }

}
