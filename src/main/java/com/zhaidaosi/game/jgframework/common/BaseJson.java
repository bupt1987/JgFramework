package com.zhaidaosi.game.jgframework.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseJson {

    private static final Logger log = LoggerFactory.getLogger(BaseJson.class);

    public static <T> T JsonToObject(String jsonStr, Class<T> c) {
        return JSON.parseObject(jsonStr, c);
    }

    public static String ObjectToJson(Object obj) {
        return JSON.toJSONString(obj, SerializerFeature.WriteNonStringKeyAsString, SerializerFeature.BrowserCompatible);
    }

}
