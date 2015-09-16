package com.zhaidaosi.game.jgframework.common;

public class BaseString {

    public static boolean isEmpty(String str) {
        if (str == null) {
            return true;
        }
        str = str.trim();
        return str.equals("");
    }

}
