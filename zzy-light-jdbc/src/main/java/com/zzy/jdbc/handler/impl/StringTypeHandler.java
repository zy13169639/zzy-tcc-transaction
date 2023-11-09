package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;

import java.sql.*;

public class StringTypeHandler extends BaseClassTypeHandler<String> {


    @Override
    public String doGet(ResultSet resultSet, String columnName) throws Exception {
        return resultSet.getString(columnName);
    }

    @Override
    public void doSet(PreparedStatement statement, String param, Integer index) throws Exception {
        statement.setString(index, param);
    }
}
