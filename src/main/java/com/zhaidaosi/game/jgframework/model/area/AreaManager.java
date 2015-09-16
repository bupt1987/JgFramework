package com.zhaidaosi.game.jgframework.model.area;

import com.zhaidaosi.game.jgframework.common.BaseFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class AreaManager {

    private static final Logger log = LoggerFactory.getLogger(AreaManager.class);
    private static Map<Integer, IBaseArea> areas = new HashMap<Integer, IBaseArea>();
    private static final String classSuffix = "Area";

    public static void initArea(String packagePath) {
        if (packagePath == null) {
            return;
        }
        Set<Class<?>> classes = BaseFile.getClasses(packagePath, classSuffix, true);
        for (Class<?> c : classes) {
            IBaseArea obj;
            try {
                obj = (IBaseArea) c.newInstance();
                areas.put(obj.getId(), obj);
                log.info("Area类 : " + c.getName() + " 加载完成");
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("Area类 : " + c.getName() + "加载失败", e);
            }
        }
        for (Map.Entry<Integer, IBaseArea> entry : areas.entrySet()) {
            entry.getValue().init();
        }
    }

    public static IBaseArea getArea(int id) {
        return areas.get(id);
    }

}
