package com.zhaidaosi.game.jgframework.model.entity;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.action.IBaseAction;

public interface IBaseEntity {
	
	public int getId();
	
	public void setId(int id);
	
	public String getRoll();
	
	public void setRoll(String roll);

	public BasePosition getPosition();
	
	public void setPosition(BasePosition position);
	
	public String getName();
	
	public void setName(String name);
	
	public IBaseAction getAction(int id);
	
	public void addAction(IBaseAction action);
	
	public void removeAction(int id);
	
}
