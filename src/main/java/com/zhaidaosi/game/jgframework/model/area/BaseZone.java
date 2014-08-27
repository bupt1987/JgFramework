package com.zhaidaosi.game.jgframework.model.area;

import com.zhaidaosi.game.jgframework.model.BasePosition;
import com.zhaidaosi.game.jgframework.model.map.IBaseMap;

/**
 * 可以随时new的
 * @author Jerry
 */
public abstract class BaseZone extends BaseArea {

    public BaseZone(String name) {
        super(name);
    }

    public BaseZone(String name, BasePosition entrancePosition) {
        super(name, entrancePosition);
    }

    public BaseZone(String name, IBaseMap map) {
        super(name, map);
    }

    public BaseZone(String name, BasePosition entrancePosition, IBaseMap map) {
        super(name, entrancePosition, map);
    }

    @Override
    public abstract void init();


}
