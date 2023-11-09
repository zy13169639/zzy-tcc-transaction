package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;
import com.zzy.jdbc.handler.TypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.Date;

public class TimeOnlyOnlyTypeHandler extends BaseClassTypeHandler<Date> {

    @Override
    public Date doGet(ResultSet resultSet, String columnName) throws Exception {
        Time sqlTime = resultSet.getTime(columnName);
        if (sqlTime != null) {
            return new Date(sqlTime.getTime());
        }
        return null;
    }

    @Override
    public void doSet(PreparedStatement statement, Date param, Integer index) throws Exception {
        statement.setTime(index, new Time(param.getTime()));
    }
}
