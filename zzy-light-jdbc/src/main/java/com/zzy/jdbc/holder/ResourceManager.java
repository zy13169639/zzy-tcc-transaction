package com.zzy.jdbc.holder;

import com.zzy.jdbc.propeties.JdbcProperties;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.datasource.DataSourceUtils;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 参数校验
 *
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/4 12:13
 */
public class ResourceManager {


    @Getter
    private static DataSource dataSource;

    @Getter
    private static JdbcProperties jdbcProperties;

    @Getter
    private static String application;

    @Autowired
    public void setDataSource(DataSource datasource) {
        dataSource = datasource;
    }

    @Autowired
    public void setDataSource(JdbcProperties properties) {
        jdbcProperties = properties;
    }

    @Autowired
    public void setApplicationName(@Value("${spring.application.name}") String applicationName) {
        application = applicationName;
    }

    public static Connection getTransactionConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

}
