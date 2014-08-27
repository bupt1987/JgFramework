package com.zhaidaosi.game.jgframework.common.excption;

@SuppressWarnings("serial")
public class MessageException extends BaseException {

    public MessageException(String msg) {
        super(msg, 50000);
    }

}
