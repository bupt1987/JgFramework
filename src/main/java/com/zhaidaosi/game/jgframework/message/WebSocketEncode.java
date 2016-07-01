package com.zhaidaosi.game.jgframework.message;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

public class WebSocketEncode extends MessageToMessageEncoder<Object> {

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) {
        if(msg == null) {
            return ;
        }
        if (msg instanceof IBaseMessage) {
            TextWebSocketFrame tsf = new TextWebSocketFrame(msg.toString());
            out.add(tsf);
        } else {
            ReferenceCountUtil.retain(msg);
            out.add(msg);
        }
    }

}
