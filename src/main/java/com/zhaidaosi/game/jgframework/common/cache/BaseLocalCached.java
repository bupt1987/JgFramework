package com.zhaidaosi.game.jgframework.common.cache;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 用户保持不会垮服务器的数据
 * @author Jerry
 */
public class BaseLocalCached {

    public static final String INVALID = "%IT IS INVALID!!%";

    private class BaseCacheElement {
        private Object value;
        private long failureTime;

        private BaseCacheElement(Object value, long failureTime) {
            this.value = value;
            this.failureTime = failureTime;
        }
    }

    private ConcurrentMap<String, BaseCacheElement> cache = new ConcurrentHashMap<String, BaseCacheElement>();

    private static Timer timer = null;
    private static List<BaseLocalCached> list = Collections.synchronizedList(new ArrayList<BaseLocalCached>());
    private static final long period = 600000;


    public BaseLocalCached() {
        if (!list.contains(this)) {
            list.add(this);
            startTimer();
        }
    }

    /**
     * 设置缓存
     * @param key
     * @param value
     * @param time 单位秒
     */
    public void set(String key, Object value, int time) {
        cache.put(key, new BaseCacheElement(value, System.currentTimeMillis() + time * 1000));
    }

    public void set(String key, Object value) {
        cache.put(key, new BaseCacheElement(value, 0L));
    }

    public void delete(String key) {
        cache.remove(key);
    }

    /**
     * 判断是否过期，过期返回true
     * @param value
     * @return
     */
    public boolean checkInvalid(Object value) {
        return INVALID.equals(value);
    }

    public Object get(String key) {
        BaseCacheElement element = cache.get(key);
        if (checkTime(element)) {
            return element.value;
        } else {
            if (element != null) {
                cache.remove(key);
            }
            return INVALID;
        }
    }

    private boolean checkTime(BaseCacheElement element) {
        if (element == null) {
            return false;
        }
        if (element.failureTime == 0L || element.failureTime > System.currentTimeMillis()) {
            return true;
        }
        return false;
    }

    public static void startTimer() {
        if (timer == null && list.size() > 0) {
            timer = new Timer("BaseLocalCachedTimer");
            timer.schedule(new InvalidTask(), period, period);
        }
    }

    public static void cancelTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    private static void invalid() {
        for (BaseLocalCached localCached : list) {
            for (Map.Entry<String, BaseCacheElement> entry : localCached.cache.entrySet()) {
                String key = entry.getKey();
                BaseCacheElement element = entry.getValue();
                if (!localCached.checkTime(element)) {
                    localCached.delete(key);
                }
            }
        }
    }

    private static class InvalidTask extends TimerTask {
        @Override
        public void run() {
            BaseLocalCached.invalid();
        }
    }

}

