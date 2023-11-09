package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class LongTypeHandler extends BaseClassTypeHandler<Long> {
    @Override
    public Long doGet(ResultSet resultSet, String columnName) throws Exception {
        return resultSet.getLong(columnName);
    }

    @Override
    public void doSet(PreparedStatement statement, Long param, Integer index) throws Exception {
        statement.setLong(index, param);
    }
}
