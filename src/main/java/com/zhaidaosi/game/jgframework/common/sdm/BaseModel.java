package com.zhaidaosi.game.jgframework.common.sdm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

@SuppressWarnings("serial")
public abstract class BaseModel implements IBaseModel, Cloneable, java.io.Serializable {

    protected static final Logger log = LoggerFactory.getLogger(BaseModel.class);

    public String toString() {
        Field[] fields = this.getClass().getDeclaredFields();
        StringBuffer strBuf = new StringBuffer();
        strBuf.append(this.getClass().getName());
        strBuf.append(" { ");
        for (int i = 0; i < fields.length; i++) {
            Field fd = fields[i];
            fd.setAccessible(true);
            strBuf.append("[" + fd.getName() + ": ");
            try {
                strBuf.append(fd.get(this) + "]");
            } catch (IllegalArgumentException | IllegalAccessException e) {
//					e.printStackTrace();
            }
            if (i != fields.length - 1)
                strBuf.append(", ");
        }

        strBuf.append(" }");
        return strBuf.toString();
    }

    public IBaseModel clone() {
        IBaseModel o = null;
        try {
            o = (IBaseModel) super.clone();
        } catch (CloneNotSupportedException e) {
            log.error(e.getMessage(), e);
        }
        return o;
    }

}
