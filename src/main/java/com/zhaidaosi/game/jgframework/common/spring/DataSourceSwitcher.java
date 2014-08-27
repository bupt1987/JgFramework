package com.zhaidaosi.game.jgframework.common.spring;

import org.springframework.util.Assert;

public class DataSourceSwitcher {

    private static ThreadLocal<String> contextHolder = new ThreadLocal<String>();
    private static String defaultDatabase;


    public static void setDataSource(String dataSource) {
        Assert.notNull(dataSource, "dataSource cannot be null");
        contextHolder.set(dataSource);
    }

    public static void setMaster(String database) {
        if (database == null) {
            database = defaultDatabase;
        }
        setDataSource(database + "-master");
    }

    public static void setSlave(String database) {
        if (database == null) {
            database = defaultDatabase;
        }
        setDataSource(database + "-slave");
    }

    public static String getDataSource() {
        return (String) contextHolder.get();
    }

    public static void clearDataSource() {
        contextHolder.remove();
    }

    public static void setDefaultDatabase(String database) {
        defaultDatabase = database;
    }

}


