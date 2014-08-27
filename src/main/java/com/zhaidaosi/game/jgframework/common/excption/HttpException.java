package com.zhaidaosi.game.jgframework.common.excption;

@SuppressWarnings("serial")
public class HttpException extends BaseException {

    public HttpException(String message) {
        super(message, 60000);
    }

}
