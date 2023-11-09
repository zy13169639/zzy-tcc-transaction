package com.zzy.jdbc.handler.impl;

import com.zzy.jdbc.handler.BaseClassTypeHandler;
import com.zzy.jdbc.handler.TypeHandler;

import java.io.StringReader;
import java.sql.*;

public class StringClobTypeHandler extends BaseClassTypeHandler<String> {

    @Override
    public String doGet(ResultSet resultSet, String columnName) throws Exception {
        Clob clob = resultSet.getClob(columnName);
        return toString(clob);
    }

    @Override
    public void doSet(PreparedStatement statement, String param, Integer index) throws Exception {
        StringReader reader = new StringReader(param);
        statement.setCharacterStream(index, reader, param.length());
    }

    private String toString(Clob clob) throws SQLException {
        return clob == null ? null : clob.getSubString(1, (int) clob.length());
    }

    @Override
    public Integer getJdbcType() {
        return Types.CLOB;
    }
}
