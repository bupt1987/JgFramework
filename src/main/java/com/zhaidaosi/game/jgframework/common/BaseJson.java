package com.zhaidaosi.game.jgframework.common;

import java.io.IOException;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class BaseJson {

	private static final Logger log = LoggerFactory.getLogger(BaseJson.class);

	private static ObjectMapper mapper;

	static {
		mapper = new ObjectMapper();
		// mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
	}

	public static <T> T JsonToObject(String jsonStr, Class<T> c) {
		try {
			return mapper.readValue(jsonStr, c);
		} catch (IOException e) {
			log.error("json to Object error : " + jsonStr + " => " + c.getName(), e);
		}
		return null;
	}

	public static String ObjectToJson(Object obj) {
		StringWriter sw = new StringWriter();
		try {
			mapper.writeValue(sw, obj);
		} catch (IOException e) {
			log.error("Objct to json error : " + obj, e);
		}

		try {
			sw.flush();
			sw.close();
		} catch (IOException e) {
			log.error("StringWriter flush | close error", e);
		}
		return sw.toString();

	}

}
