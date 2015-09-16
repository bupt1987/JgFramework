package com.zhaidaosi.game.jgframework.rsync;

import com.zhaidaosi.game.jgframework.Boot;
import com.zhaidaosi.game.jgframework.common.BaseFile;
import com.zhaidaosi.game.jgframework.common.BaseRunTimer;
import com.zhaidaosi.game.jgframework.common.sdm.IBaseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RsyncManager {

    private static long serviceSyncPeriod;
    private static final Logger log = LoggerFactory.getLogger(RsyncManager.class);
    private static HashMap<Class<?>, IBaseRsync> rsyncMap;
    private static final String classSuffix = "Rsync";

    public static void init() {
        rsyncMap = new HashMap<Class<?>, IBaseRsync>();
        serviceSyncPeriod = Boot.getServiceSyncPeriod();
        Set<Class<?>> classes = BaseFile.getClasses(Boot.SERVER_RSYNC_PACKAGE_PATH, classSuffix, true);
        for (Class<?> c : classes) {
            IBaseRsync obj;
            try {
                obj = (IBaseRsync) c.newInstance();
                rsyncMap.put(c, obj);
                log.info("rsync类 : " + c.getName() + " 加载完成");
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("rsync类 : " + c.getName() + "加载失败", e);
            }
        }
    }

    public static long getSyncPeriod() {
        return serviceSyncPeriod;
    }

    /**
     * 添加异步同步
     * @param uid
     * @param class
     */
    public static void add(Integer userId, Class<?> c, IBaseModel obj) {
        IBaseRsync rsync = rsyncMap.get(c);
        if (rsync != null) {
            rsync.addRsync(userId, obj);
        } else {
            log.error("添加异步任务失败: " + c.getName() + "异步类不存在");
        }
    }

    public static IBaseModel get(Integer userId, Class<?> c) {
        IBaseModel model = null;
        IBaseRsync rsync = rsyncMap.get(c);
        if (rsync != null) {
            model = rsync.get(userId);
        }
        return model;
    }

    public static void run() {

        long time = System.currentTimeMillis() % serviceSyncPeriod;
        if (time < 1500) {
            try {
                Thread.sleep(1500 - time);
            } catch (InterruptedException e) {
                log.error("sleep error", e);
            }
        }

        Map<IBaseRsync, Map<Integer, IBaseModel>> needRsync = new HashMap<IBaseRsync, Map<Integer, IBaseModel>>();
        for (Map.Entry<Class<?>, IBaseRsync> entry : rsyncMap.entrySet()) {
            IBaseRsync rsync = entry.getValue();
            //如果前一个任务还没有完成则不导入任务，任务推至下次运行
            if (!rsync.isRunning()) {
                Map<Integer, IBaseModel> map = rsync.getNeedRsync();
                if (map.size() > 0) {
                    needRsync.put(rsync, map);
                    rsync.clearNeedRsync();
                }
            } else {
                log.error(rsync.getClass().getName() + " 任务被推迟！！！");
            }
        }
        for (Map.Entry<IBaseRsync, Map<Integer, IBaseModel>> entry : needRsync.entrySet()) {
            RsyncThread rt = new RsyncThread(entry.getKey(), entry.getValue());
            rt.start();
        }
    }

}

class RsyncThread extends Thread {

    private IBaseRsync rsync;
    private Map<Integer, IBaseModel> map;

    public RsyncThread(IBaseRsync rsync, Map<Integer, IBaseModel> map) {
        this.rsync = rsync;
        this.map = map;
    }

    public void run() {
        rsync.toRunning();
        rsync.setRsyncMap(map);
        rsync.runRsync();
        rsync.clearRsyncMap();
        rsync.toStop();
        BaseRunTimer.showTimer();
    }

}
