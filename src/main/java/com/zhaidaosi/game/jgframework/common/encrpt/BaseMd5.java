package com.zhaidaosi.game.jgframework.common.encrpt;

import com.zhaidaosi.game.jgframework.Boot;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

public class BaseMd5 {

    /**
     * 进行MD5加密
     *
     * @param text 原始的SPKEY
     * @return String 指定加密方式为md5后的String
     */
    public static String encrypt(String text) {
        byte[] returnByte;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            returnByte = md5.digest(text.getBytes(Boot.getCharset()));
        } catch (Exception e) {
            return null;
        }
        return DatatypeConverter.printBase64Binary(returnByte);
    }

}
