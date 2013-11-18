package com.zhaidaosi.game.jgframework.session;

public interface IBaseSercretFactory {

	public String createSercret(int userId) throws Exception;
	
	public int checkSercret(String sercret) throws Exception;
	
}
