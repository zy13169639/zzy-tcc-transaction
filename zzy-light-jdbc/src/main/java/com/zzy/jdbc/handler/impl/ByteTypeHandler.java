package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ByteTypeHandler extends BaseClassTypeHandler<Byte> {
    @Override
    public Byte doGet(ResultSet resultSet, String columnName) throws Exception {
        return resultSet.getByte(columnName);
    }

    @Override
    public void doSet(PreparedStatement statement, Byte param, Integer index) throws Exception {
        statement.setByte(index,param);
    }
}
