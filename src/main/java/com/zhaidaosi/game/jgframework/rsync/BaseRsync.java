package com.zhaidaosi.game.jgframework.rsync;

import com.zhaidaosi.game.jgframework.common.sdm.IBaseModel;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class BaseRsync implements IBaseRsync {

    private boolean isRunning = false;
    private ConcurrentMap<Integer, IBaseModel> mapOne = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, IBaseModel> mapTwo = new ConcurrentHashMap<>();
    private ConcurrentMap<Integer, IBaseModel> mapThree = new ConcurrentHashMap<>();
    protected Map<Integer, IBaseModel> rsyncMap = new HashMap<>();


    @Override
    public abstract void runRsync();

    @Override
    public void addRsync(Integer id, IBaseModel obj) {
        this.getNowMap().put(id, obj);
    }

    @Override
    public void setRsyncMap(Map<Integer, IBaseModel> map) {
        this.rsyncMap = map;
    }

    @Override
    public void clearRsyncMap() {
        this.rsyncMap.clear();
    }

    @Override
    public IBaseModel get(Integer id) {
        IBaseModel model = this.getNowMap().get(id);
        if (model == null) {
            model = this.rsyncMap.get(id);
        }
        return model;
    }

    @Override
    public Map<Integer, IBaseModel> getNeedRsync() {
        return new HashMap<>(this.getNeedRsyncMap());
    }

    @Override
    public void clearNeedRsync() {
        this.getNeedRsyncMap().clear();
    }

    @Override
    public boolean isRunning() {
        return isRunning;
    }

    @Override
    public void toRunning() {
        this.isRunning = true;
    }

    @Override
    public void toStop() {
        this.isRunning = false;
    }

    /**
     * 选择当前map
     * @return
     */
    private ConcurrentMap<Integer, IBaseModel> getNowMap() {
        return this.selectMap(this.getSelect(System.currentTimeMillis()));

    }

    private ConcurrentMap<Integer, IBaseModel> getNeedRsyncMap() {
        return this.selectMap(this.getSelect(System.currentTimeMillis() - RsyncManager.getSyncPeriod()));
    }

    /**
     * 判断当前是哪个map
     * @return
     */
    private int getSelect(long now) {
        return (int) (Math.floor(now / RsyncManager.getSyncPeriod()) % 3);
    }

    /**
     * 选择map
     * @param select
     * @return
     */
    private ConcurrentMap<Integer, IBaseModel> selectMap(int select) {
        if (select == 0) {
            return mapOne;
        } else if (select == 1) {
            return mapTwo;
        } else {
            return mapThree;
        }
    }

}
