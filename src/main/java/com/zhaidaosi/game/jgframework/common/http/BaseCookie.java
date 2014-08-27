package com.zhaidaosi.game.jgframework.common.http;

public class BaseCookie {

    private String key;
    private String value;

    public BaseCookie(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

}
