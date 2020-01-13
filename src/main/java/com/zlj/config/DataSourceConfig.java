package com.zlj.config;

import com.alibaba.druid.pool.DruidDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

@Configuration
public class DataSourceConfig {
    private static Logger logger = LoggerFactory.getLogger(DataSourceConfig.class);

    @Autowired
    private Environment env;

    @Bean
    public DataSource dataSource() {
        DruidDataSource ds = new DruidDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:3306/test");
        ds.setUsername("debian-sys-maint");
        ds.setPassword("MTie2ZhYlrPxrSaw");
        ds.setMaxActive(60);
        ds.setInitialSize(2);
        ds.setMaxWait(60000);
        ds.setMinIdle(1);
        ds.setTimeBetweenEvictionRunsMillis(60000);
        ds.setMinEvictableIdleTimeMillis(300000);
        ds.setValidationQuery("select 'x'");
        ds.setTestWhileIdle(true);
        ds.setTestOnBorrow(true);
        ds.setTestOnReturn(true);
        ds.setPoolPreparedStatements(true);
        ds.setMaxOpenPreparedStatements(20);
        return ds;
    }

    @Bean
    public DataSourceTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }
}
