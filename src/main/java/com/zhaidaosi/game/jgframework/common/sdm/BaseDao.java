package com.zhaidaosi.game.jgframework.common.sdm;

import com.zhaidaosi.game.jgframework.Boot;
import org.hibernate.LockMode;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class BaseDao extends HibernateDaoSupport implements IBaseDao {
    private static final Logger log = LoggerFactory
            .getLogger(BaseDao.class);

    protected String tableName = "";
    protected String modelName = "";
    protected String entityPackage = Boot.SDM_BASE_PACKAGE_NAME + ".model.";

    @Override
    public List<?> find(String queryString) {
        return this.getQuery(queryString, null).list();
    }

    @Override
    public List<?> find(String queryString, Map<String, Object> data) {
        return this.getQuery(queryString, data).list();
    }

    @Override
    public List<?> find(String queryString, Map<String, Object> data, int start, int limit) {
        return this.getQuery(queryString, data, start, limit).list();
    }

    @Override
    public int total() {
        return this.total(null, null);
    }

    @Override
    public int total(String queryString) {
        return this.total(queryString, null);
    }

    @Override
    public int total(Map<String, Object> where) {
        return this.total(null, where);
    }


    @Override
    public int total(String queryString, Map<String, Object> where) {
        if (queryString == null) {
            queryString = "select count(1) from " + this.tableName;
            if (where != null) {
                queryString += " where " + this.formateWhereSql(where, "and");
                ;
            }
        }

        return ((Number) getQuery(queryString, where, false).uniqueResult()).intValue();
    }

    @Override
    public int update(String queryString) {
        return this.execute(queryString);
    }

    @Override
    public int update(Map<String, Object> where) {
        String queryString = "update " + this.tableName + " set " + this.formateWhereSql(where, ",");
        return this.execute(queryString, where);
    }

    @Override
    public int delete(String queryString) {
        return this.execute(queryString);
    }

    @Override
    public int delete(String queryString, Map<String, Object> where) {
        return this.execute(queryString, where);
    }

    @Override
    public int execute(String queryString) {
        return this.execute(queryString, null);
    }

    @Override
    public int execute(String queryString, Map<String, Object> where) {
        int result = this.getQuery(queryString, where, false).executeUpdate();
        return result;
    }

    @Override
    public List<IBaseModel> findByProperty(String propertyName, Object value) {
        log.debug("finding Object instance with property: " + propertyName
                + ", value: " + value);
        try {
            String queryString = "from " + this.modelName + " as model where model."
                    + propertyName + "= ?";
            return getHibernateTemplate().find(queryString, value);
        } catch (RuntimeException re) {
            log.error("find by property name failed", re);
            throw re;
        }
    }

    @Override
    public IBaseModel findOneByProperty(String propertyName, Object value) {
        log.debug("finding Object instance with property: " + propertyName
                + ", value: " + value);
        try {
            String queryString = "from " + this.modelName + " as model where model."
                    + propertyName + "= ?";
            return (IBaseModel) getHibernateTemplate().find(queryString, value).get(0);
        } catch (RuntimeException re) {
            log.error("find by property name failed", re);
            throw re;
        }
    }

    @Override
    public IBaseModel findById(int id) {
        log.debug("getting Object instance with id: " + id);
        try {
            IBaseModel instance = (IBaseModel) getHibernateTemplate().get(
                    this.entityPackage + this.modelName, id);
            return instance;
        } catch (RuntimeException re) {
            log.error("get failed", re);
            throw re;
        }
    }

    @Override
    public void update(IBaseModel persistentInstance) {
        log.debug("update Object instance");
        try {
            getHibernateTemplate().update(persistentInstance);
            log.debug("update successful");
        } catch (RuntimeException re) {
            log.error("update failed", re);
            throw re;
        }
    }

    @Override
    public void save(IBaseModel transientInstance) {
        log.debug("saving CronModel instance");
        try {
            getHibernateTemplate().save(transientInstance);
            log.debug("save successful");
        } catch (RuntimeException re) {
            log.error("save failed", re);
            throw re;
        }
    }

    @Override
    public void delete(IBaseModel persistentInstance) {
        log.debug("deleting Object instance");
        try {
            getHibernateTemplate().delete(persistentInstance);
            log.debug("delete successful");
        } catch (RuntimeException re) {
            log.error("delete failed", re);
            throw re;
        }
    }

    @Override
    public List<IBaseModel> findAll() {
        log.debug("finding all Object instances");
        try {
            String queryString = "from " + this.modelName;
            return getHibernateTemplate().find(queryString);
        } catch (RuntimeException re) {
            log.error("find all failed", re);
            throw re;
        }
    }

    @Override
    public List<IBaseModel> findAll(int start, int limit) {
        log.debug("finding all Object instances");
        try {
            HibernateTemplate ht = getHibernateTemplate();
            int PRE_MAX_RESULT = ht.getMaxResults();
            int PRE_LIMIT = ht.getFetchSize();
            ht.setMaxResults(limit);
            ht.setFetchSize(start);
            String queryString = "from " + this.modelName;
            List<IBaseModel> list = ht.find(queryString);
            ht.setMaxResults(PRE_MAX_RESULT);
            ht.setFetchSize(PRE_LIMIT);
            return list;
        } catch (RuntimeException re) {
            log.error("find all failed", re);
            throw re;
        }
    }

    @Override
    public IBaseModel merge(IBaseModel detachedInstance) {
        log.debug("merging BaseModel instance");
        try {
            IBaseModel result = (IBaseModel) getSession().merge(detachedInstance);
            log.debug("merge successful");
            return result;
        } catch (RuntimeException re) {
            log.error("merge failed", re);
            throw re;
        }
    }

    @Override
    public void saveOrUpdate(IBaseModel instance) {
        log.debug("attaching dirty BaseModel instance");
        try {
            getSession().saveOrUpdate(instance);
            log.debug("attach successful");
        } catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }

    @Override
    public void attachClean(IBaseModel instance) {
        log.debug("attaching clean Article instance");
        try {
            getSession().lock(instance, LockMode.NONE);
            log.debug("attach successful");
        } catch (RuntimeException re) {
            log.error("attach failed", re);
            throw re;
        }
    }

    protected SQLQuery getQuery(String queryString, Map<String, Object> where) {
        return this.getQuery(queryString, where, true);
    }

    protected SQLQuery getQuery(String queryString, Map<String, Object> where, boolean returnMap) {
        return this.getQuery(queryString, where, -1, -1, returnMap);
    }

    protected SQLQuery getQuery(String queryString, Map<String, Object> where, int start, int limit) {
        return this.getQuery(queryString, where, start, limit, true);
    }

    protected String formateWhereSql(Map<String, Object> where, String middleString) {
        String queryString = "";
        Iterator<String> iterator = where.keySet().iterator();
        boolean hasNext = iterator.hasNext();
        while (hasNext) {
            String key = iterator.next();
            queryString += key + " = :" + key;
            hasNext = iterator.hasNext();
            if (hasNext) {
                queryString += " " + middleString + " ";
            }
        }
        return queryString;
    }

    protected SQLQuery getQuery(String queryString, Map<String, Object> where, int start, int limit, boolean returnMap) {
        Session session = this.getSession();
        SQLQuery query = null;
        if (start > -1 && limit > 0) {
            query = session.createSQLQuery(queryString);
            query.setFirstResult(start);
            query.setMaxResults(limit);
        } else {
            query = session.createSQLQuery(queryString);
        }
        if (where != null) {
            for (Entry<String, Object> entry : where.entrySet()) {
                Object value = entry.getValue();
                String key = entry.getKey();
                if (value instanceof Collection) {
                    query.setParameterList(key, (Collection) value);
                } else if (value instanceof Object[]) {
                    query.setParameterList(key, (Object[]) value);
                } else {
                    query.setParameter(key, value);
                }
            }
        }
        if (returnMap) {
            query.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        }
        return query;
    }

    @Autowired
    public void setSessionFactoryOverride(SessionFactory sessionFactory) {
        super.setSessionFactory(sessionFactory);
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }
}
