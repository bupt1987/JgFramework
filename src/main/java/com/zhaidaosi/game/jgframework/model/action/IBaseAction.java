package com.zhaidaosi.game.jgframework.model.action;

import com.zhaidaosi.game.jgframework.handler.BaseHandlerChannel;


public interface IBaseAction {

	public int getId();
	
	public void doAction(Object self, Object target, BaseHandlerChannel ch);
	
	public String getName();
	
	public void setName(String name);
	
	public IBaseAction clone();
	
}
