package com.zhaidaosi.game.jgframework.message;

import com.zhaidaosi.game.jgframework.common.BaseJson;
import com.zhaidaosi.game.jgframework.common.excption.MessageException;
import org.apache.commons.beanutils.PropertyUtils;

import java.lang.reflect.InvocationTargetException;

public class OutMessage implements IBaseMessage {

    private int code = 0;

    private String h;

    private Object result = null;

    public OutMessage() {
    }

    public OutMessage(Object result, int code) {
        this.code = code;
        this.result = result;
    }

    public OutMessage(Object result, int code, String h) {
        this.code = code;
        this.result = result;
        this.h = h;
    }

    public String toString() {
        return BaseJson.ObjectToJson(this);
    }

    public static OutMessage getMessage(String om) throws MessageException {
        if (om.startsWith("{") && om.endsWith("}")) {
            return BaseJson.JsonToObject(om, OutMessage.class);
        } else {
            throw new MessageException("msg格式错误");
        }
    }

    public static OutMessage showError(String result) {
        return showMessage(result, 1);
    }

    public static OutMessage showError(String result, String h) {
        return showMessage(result, 1, h);
    }

    public static OutMessage showError(String result, int code) {
        return showMessage(result, code);
    }

    public static OutMessage showSucc(Object result, String h) {
        return showMessage(result, 0, h);
    }

    public static OutMessage showSucc(Object result) {
        return showMessage(result, 0);
    }

    private static OutMessage showMessage(Object result, int code) {
        return new OutMessage(result, code);
    }

    private static OutMessage showMessage(Object result, int code, String h) {
        return new OutMessage(result, code, h);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getResult() {
        return result;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public Object getResultValue(String key) {
        Object value = null;
        try {
            value = PropertyUtils.getProperty(result, key).toString();
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        return value;
    }

    public String getH() {
        return h;
    }

    public void setH(String h) {
        this.h = h;
    }

}
