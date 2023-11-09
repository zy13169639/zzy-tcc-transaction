package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;
import com.zzy.jdbc.handler.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;

public class DateOnlyTypeHandler extends BaseClassTypeHandler<Date> {

    @Override
    public Date doGet(ResultSet resultSet, String columnName) throws Exception {
        java.sql.Date sqlDate = resultSet.getDate(columnName);
        if (sqlDate != null) {
            return new Date(sqlDate.getTime());
        }
        return null;
    }

    @Override
    public void doSet(PreparedStatement statement, Date param, Integer index) throws Exception {
        statement.setDate(index, new java.sql.Date(param.getTime()));
    }
}
