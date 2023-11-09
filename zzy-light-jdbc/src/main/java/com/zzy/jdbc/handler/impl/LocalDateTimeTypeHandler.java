package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;

public class LocalDateTimeTypeHandler extends BaseClassTypeHandler<LocalDateTime> {

    @Override
    public LocalDateTime doGet(ResultSet resultSet, String columnName) throws Exception {
        LocalDateTime localDateTime = resultSet.getObject(columnName,this.getJavaType());
        return localDateTime;
    }

    @Override
    public void doSet(PreparedStatement statement, LocalDateTime param, Integer index) throws Exception {
        statement.setObject(index, param);
    }
}
