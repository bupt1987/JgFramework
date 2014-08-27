package com.zhaidaosi.game.jgframework.common.spring;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class DynamicDataSource extends AbstractRoutingDataSource {

    private String defaultDatabase;

    @Override
    protected Object determineCurrentLookupKey() {
        return DataSourceSwitcher.getDataSource();
    }

    public void setDefaultDatabase(String defaultDatabase) {
        this.defaultDatabase = defaultDatabase;
        DataSourceSwitcher.setDefaultDatabase(this.defaultDatabase);
    }

}


