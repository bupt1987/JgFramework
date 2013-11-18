package com.zhaidaosi.game.jgframework.message;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class WebSocketMessageEncode extends OneToOneEncoder {

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel,
			Object msg) {
		if (msg != null && msg instanceof IBaseMessage) {
			msg = new TextWebSocketFrame(((IBaseMessage) msg).toString());
		}
		return msg;
	}

}
