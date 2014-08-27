package com.zhaidaosi.game.jgframework.common.encrpt;

import com.zhaidaosi.game.jgframework.Boot;
import sun.misc.BASE64Encoder;

import java.security.MessageDigest;

public class BaseMd5 {

    /**
     * 进行MD5加密
     *
     * @param String 原始的SPKEY
     * @return String 指定加密方式为md5后的String
     */
    public static String encrypt(String text) {
        byte[] returnByte = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            returnByte = md5.digest(text.getBytes(Boot.getCharset()));
        } catch (Exception e) {
//			e.printStackTrace();
        }
        return new BASE64Encoder().encode(returnByte);
    }

}
