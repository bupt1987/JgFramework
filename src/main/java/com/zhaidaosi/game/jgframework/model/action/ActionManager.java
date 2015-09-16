package com.zhaidaosi.game.jgframework.model.action;

import com.zhaidaosi.game.jgframework.common.BaseFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ActionManager {

    private static final Logger log = LoggerFactory.getLogger(ActionManager.class);
    private static Map<Integer, IBaseAction> actions = new HashMap<Integer, IBaseAction>();
    private static final String classSuffix = "Action";

    public static void initAction(String packagePath) {
        if (packagePath == null) {
            return;
        }
        Set<Class<?>> classes = BaseFile.getClasses(packagePath, classSuffix, true);
        for (Class<?> c : classes) {
            IBaseAction obj;
            try {
                obj = (IBaseAction) c.newInstance();
                actions.put(obj.getId(), obj);
                log.info("Action类 : " + c.getName() + " 加载完成");
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("Action类 : " + c.getName() + "加载失败", e);
            }
        }
    }

    public static IBaseAction getAction(int id) {
        return actions.get(id).clone();
    }

}
