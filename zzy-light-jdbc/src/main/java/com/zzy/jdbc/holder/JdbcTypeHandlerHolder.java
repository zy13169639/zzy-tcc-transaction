package com.zzy.jdbc.holder;

import cn.hutool.core.lang.Assert;
import com.zzy.dt.common.enums.IEnum;
import com.zzy.dt.common.holder.PrimitiveTypeMappingHolder;
import com.zzy.jdbc.handler.BaseClassTypeHandler;
import com.zzy.jdbc.handler.TypeHandler;
import com.zzy.jdbc.handler.impl.*;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class JdbcTypeHandlerHolder {

    private static final Map<Class, TypeHandler> ENUM_HANDLER_MAP = new ConcurrentHashMap<>();

    private static final Map<Class, Map<Integer, TypeHandler>> CLASS_TYPE_MAP = new HashMap<>();

    private static final Integer NULL_CODE = Integer.MIN_VALUE;

    static {
        register(new BlobTypeHandler());
        register(new StringClobTypeHandler());
        register(new DateTimeTypeHandler());
        register(new TimeOnlyOnlyTypeHandler());
        register(new DateOnlyTypeHandler());
        register(new LocalDateTimeTypeHandler());
        register(new StringTypeHandler());
        ///////////////////////////////////////////////////////
        register(new ByteTypeHandler());
        // register(new EnumTypeHandler());
        register(new IntegerTypeHandler());
        register(new LongTypeHandler());
    }

    public static void register(Class javaType, Integer jdbcType, TypeHandler typeHandler) {
        Assert.isTrue(jdbcType != null, "jdbc类型不能为空");
        Map<Integer, TypeHandler> classTypeHandlerMap = CLASS_TYPE_MAP.get(javaType);
        if (classTypeHandlerMap == null) {
            classTypeHandlerMap = new HashMap<>();
            CLASS_TYPE_MAP.put(javaType, classTypeHandlerMap);
        }
        classTypeHandlerMap.put(jdbcType, typeHandler);
    }

    public static void register(Class javaType, TypeHandler typeHandler) {
        Map<Integer, TypeHandler> classTypeHandlerMap = CLASS_TYPE_MAP.get(javaType);
        if (classTypeHandlerMap == null) {
            classTypeHandlerMap = new HashMap<>();
            CLASS_TYPE_MAP.put(javaType, classTypeHandlerMap);
        }
        classTypeHandlerMap.put(NULL_CODE, typeHandler);
    }

    public static TypeHandler getHandler(Class javaType) {
        return getHandler(javaType, NULL_CODE);
    }

    public static TypeHandler getHandler(Class javaType, Integer jdbcType) {
        if (javaType.isEnum() && IEnum.class.isAssignableFrom(javaType)) {
            TypeHandler typeHandler = ENUM_HANDLER_MAP.get(javaType);
            if (typeHandler != null) {
                return typeHandler;
            } else {
                TypeHandler enumHandler = getEnumHandler(javaType);
                ENUM_HANDLER_MAP.putIfAbsent(javaType, enumHandler);
                return enumHandler;
            }
        }
        if (javaType.isArray()) {
            Class componentType = javaType.getComponentType();
            Class wrapperClass = PrimitiveTypeMappingHolder.getWrapperClass(componentType);
            if (componentType != wrapperClass) {
                Class<?> aClass = Array.newInstance(componentType, 0).getClass();
                return getHandler(aClass, jdbcType);
            }
        }
        Map<Integer, TypeHandler> typeHandlerMap = CLASS_TYPE_MAP.get(javaType);
        if (typeHandlerMap == null) {
            return null;
        }
        TypeHandler typeHandler = typeHandlerMap.get(jdbcType);
        if (typeHandler == null) {
            typeHandler = typeHandlerMap.get(NULL_CODE);
        }
        return typeHandler;
    }

    public static void register(BaseClassTypeHandler baseClassTypeHandler) {
        if (baseClassTypeHandler.getJdbcType() != null) {
            register(baseClassTypeHandler.getJavaType(), baseClassTypeHandler.getJdbcType(), baseClassTypeHandler);
        } else {
            register(baseClassTypeHandler.getJavaType(), baseClassTypeHandler);
        }
    }

    private static TypeHandler getEnumHandler(Class javaType) {
        EnumTypeHandler enumTypeHandler = new EnumTypeHandler();
        enumTypeHandler.setJavaType(javaType);
        return enumTypeHandler;
    }
}
