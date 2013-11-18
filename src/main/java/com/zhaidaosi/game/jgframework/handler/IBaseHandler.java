package com.zhaidaosi.game.jgframework.handler;

import org.jboss.netty.channel.Channel;

import com.zhaidaosi.game.jgframework.message.IBaseMessage;
import com.zhaidaosi.game.jgframework.message.InMessage;

public interface IBaseHandler {

	public IBaseMessage run(InMessage im, Channel ch) throws Exception;
	
	public String getHandlerName();
	
	public void setHandlerName(String handlerName);
	
}
