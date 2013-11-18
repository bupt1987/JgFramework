package com.zhaidaosi.game.jgframework.session;

import com.zhaidaosi.game.jgframework.common.BaseDate;
import com.zhaidaosi.game.jgframework.common.encrpt.BaseRsa;
import com.zhaidaosi.game.jgframework.common.excption.BaseException;

public class BaseSercretFactory implements IBaseSercretFactory {

	@Override
	public String createSercret(int userId) throws BaseException {
		String time = BaseDate.time2String(BaseDate.FORMAT_YY_MM_DD_HH_MM_SS);
		String input = userId + "_" + time;
		String bytes = BaseRsa.encrypt(input);
		if(bytes == null){
			throw new BaseException("秘钥生成失败", 100);
		}
		return bytes;
	}

	@Override
	public int checkSercret(String sercret) throws Exception {
		sercret = BaseRsa.decrypt(sercret);
		String[] arr = sercret.split("_");
		if(arr.length != 2){
			throw new BaseException("非法秘钥", 101);
		}
		int userId = Integer.valueOf(arr[0]);
		if(userId <= 0){
			throw new BaseException("非法秘钥", 101);
		}
		//有效期1分钟
		if(BaseDate.string2Time(arr[1]) - System.currentTimeMillis() > 60000){
			throw new BaseException("秘钥已失效", 102);
		}
		return userId;
	}

}
