package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class IntegerTypeHandler extends BaseClassTypeHandler<Integer> {
    @Override
    public Integer doGet(ResultSet resultSet, String columnName) throws Exception {
        return resultSet.getInt(columnName);
    }

    @Override
    public void doSet(PreparedStatement statement, Integer param, Integer index) throws Exception {
        statement.setInt(index,param);
    }
}
