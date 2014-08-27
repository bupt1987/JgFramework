package com.zhaidaosi.game.jgframework.common.sdm;

import java.util.List;
import java.util.Map;

public interface IBaseDao {

    public List<?> find(String queryString);

    public List<?> find(String queryString, Map<String, Object> data);

    public List<?> find(String queryString, Map<String, Object> data, int start, int limit);

    public int total();

    public int total(String queryString);

    public int total(Map<String, Object> where);

    public int total(String queryString, Map<String, Object> where);

    public int update(String queryString);

    public int update(Map<String, Object> where);

    public int delete(String queryString);

    public int delete(String queryString, Map<String, Object> where);

    public int execute(String queryString);

    public int execute(String queryString, Map<String, Object> where);

    public List<IBaseModel> findByProperty(String propertyName, Object value);

    public IBaseModel findOneByProperty(String propertyName, Object value);

    public IBaseModel findById(int id);

    public void update(IBaseModel persistentInstance);

    public void save(IBaseModel transientInstance);

    public void delete(IBaseModel persistentInstance);

    public List<IBaseModel> findAll();

    public List<IBaseModel> findAll(int start, int limit);

    public IBaseModel merge(IBaseModel detachedInstance);

    public void saveOrUpdate(IBaseModel instance);

    public void attachClean(IBaseModel instance);

}
