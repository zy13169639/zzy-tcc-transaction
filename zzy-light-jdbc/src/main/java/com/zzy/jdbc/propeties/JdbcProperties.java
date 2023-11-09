package com.zzy.jdbc.propeties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.sql.JDBCType;

@ConfigurationProperties("dt.jdbc")
@Data
public class JdbcProperties {

    public Object ResourceManager;
    private JDBCType nullType;

}
