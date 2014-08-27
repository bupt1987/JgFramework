package com.zhaidaosi.game.jgframework.common.excption;


@SuppressWarnings("serial")
public class BaseException extends Exception implements IBaseException {

    protected int code = 0;

    public BaseException(String msg, int code) {
        super(msg);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

}
