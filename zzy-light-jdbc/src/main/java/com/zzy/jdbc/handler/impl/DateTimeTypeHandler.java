package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;
import com.zzy.jdbc.handler.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.util.Date;

public class DateTimeTypeHandler extends BaseClassTypeHandler<Date> {

    @Override
    public Date doGet(ResultSet resultSet, String columnName) throws Exception {
        Timestamp timestamp = resultSet.getTimestamp(columnName);
        if (timestamp != null) {
            return new Date(timestamp.getTime());
        }
        return null;
    }

    @Override
    public void doSet(PreparedStatement statement, Date param, Integer index) throws Exception {
        statement.setDate(index,new java.sql.Date(param.getTime()));
    }
}
