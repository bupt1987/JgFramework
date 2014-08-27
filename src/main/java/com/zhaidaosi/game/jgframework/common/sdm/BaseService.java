package com.zhaidaosi.game.jgframework.common.sdm;

import java.util.List;

public abstract class BaseService {

    protected IBaseDao dao;

    protected abstract void setDao();

    public IBaseModel findById(int id) {
        return dao.findById(id);
    }

    public List<?> findAll(int start, int limit) {
        return dao.findAll(start, limit);
    }

    public List<?> findByProperty(String propertyName, Object value) {
        return dao.findByProperty(propertyName, value);
    }

    public IBaseModel findOneByProperty(String propertyName, Object value) {
        return dao.findOneByProperty(propertyName, value);
    }

    public int total() {
        return dao.total();
    }

    public void save(IBaseModel obj) {
        dao.save(obj);
    }

    public void update(IBaseModel obj) {
        dao.update(obj);
    }

    public List<?> findAll() {
        return dao.findAll();
    }

    public void delete(IBaseModel obj) {
        dao.delete(obj);
    }

    public String getDatabase() {
        return null;
    }

    protected void setDao(IBaseDao dao) {
        this.dao = dao;
    }

}
