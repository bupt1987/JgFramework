package com.zhaidaosi.game.jgframework.common.cache;

import com.whalin.MemCached.MemCachedClient;
import com.whalin.MemCached.SockIOPool;
import com.zhaidaosi.game.jgframework.Boot;

import java.util.ArrayList;

public class BaseMemCached extends MemCachedClient {

    // 创建全局的唯一实例
    private static BaseMemCached mc = new BaseMemCached();
    private final static String KEYPREFIX;

    // 设置与缓存服务器的连接池
    static {
        String[] servers;
        Integer[] weights;
        ArrayList<String> confServers = Boot.getMemcacheServers();
        KEYPREFIX = Boot.getMemcacheKeyPrefix();
        int size = confServers.size();
        if (size > 1) {
            servers = new String[size];
            weights = new Integer[size];
            for (int i = 0; i < size; i++) {
                String[] temp2 = confServers.get(i).split(",");
                servers[i] = temp2[0];
                if (temp2.length > 1) {
                    weights[i] = new Integer(temp2[1]);
                } else {
                    weights[i] = 1;
                }
            }
        } else {
            // 服务器列表和其权重
            servers = new String[]{"127.0.0.1:11211"};
            weights = new Integer[]{1};
        }
        // 获取socke连接池的实例对象
        SockIOPool pool = SockIOPool.getInstance();
        // 设置服务器信息
        pool.setServers(servers);
        pool.setWeights(weights);
        // 设置初始连接数、最小和最大连接数以及最大处理时间
        pool.setInitConn(100);
        pool.setMinConn(100);
        pool.setMaxConn(2000);
        pool.setMaxIdle(1000 * 60 * 60 * 6);
        // 设置主线程的睡眠时间
        pool.setMaintSleep(30);
        // 设置TCP的参数，连接超时等
        pool.setNagle(false);
        //连接建立后的超时时间
        pool.setSocketTO(3000);
        //连接建立时的超时时间
        pool.setSocketConnectTO(0);
        pool.setHashingAlg(SockIOPool.NEW_COMPAT_HASH);
        // 初始化连接池
        pool.initialize();
    }

    /**
     * 保护型构造方法，不允许实例化！
     *
     */
    private BaseMemCached() {

    }


    /**
     * 获取唯一实例.
     */
    public static BaseMemCached getInstance() {
        return mc;
    }

    public static String getMcKey(String key) {
        return KEYPREFIX + key;
    }

}
