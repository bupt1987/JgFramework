package com.zhaidaosi.game.jgframework.message;

import com.zhaidaosi.game.jgframework.common.BaseJson;
import com.zhaidaosi.game.jgframework.common.excption.MessageException;

import java.util.HashMap;


public class InMessage implements IBaseMessage {
    /**
     * 控制器handler路径
     */
    private String h;
    /**
     * 参数
     */
    private HashMap<String, Object> p = new HashMap<>();

    public InMessage() {
    }

    public InMessage(String h) {
        this.h = h;
    }

    public InMessage(String h, HashMap<String, Object> p) {
        this.h = h;
        this.p = p;
    }

    public static InMessage getMessage(String msg) throws MessageException {
        if (msg.startsWith("{") && msg.endsWith("}")) {
            return BaseJson.JsonToObject(msg, InMessage.class);
        } else {
            throw new MessageException("msg格式错误");
        }
    }

    public String toString() {
        return BaseJson.ObjectToJson(this);
    }

    public String getH() {
        return h;
    }

    public HashMap<String, Object> getP() {
        return p;
    }

    public void setH(String h) {
        this.h = h;
    }


    public void setP(HashMap<String, Object> p) {
        this.p = p;
    }

    public void putMember(String key, Object value) {
        this.p.put(key, value);
    }

    public Object getMember(String key) {
        return p.get(key);
    }

}
