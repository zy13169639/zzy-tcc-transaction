package com.zzy.jdbc.handler.impl;

import com.zzy.dt.common.util.ByteArrayUtils;
import com.zzy.jdbc.handler.BaseClassTypeHandler;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;

public class BlobTypeHandler extends BaseClassTypeHandler<Byte[]> {

    @Override
    public Byte[] doGet(ResultSet resultSet, String columnName) throws Exception {
        Blob blob = resultSet.getBlob(columnName);
        byte[] returnValue = null;
        if (null != blob) {
            returnValue = blob.getBytes(1, (int) blob.length());
        }
        if (returnValue != null) {
            return ByteArrayUtils.convertToObjectArray(returnValue);
        }
        return null;
    }

    @Override
    public void doSet(PreparedStatement statement, Byte[] param, Integer index) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(ByteArrayUtils.convertToPrimitiveArray(param));
        statement.setBinaryStream(index, bis, param.length);
    }

    @Override
    public Integer getJdbcType() {
        return Types.BLOB;
    }
}
