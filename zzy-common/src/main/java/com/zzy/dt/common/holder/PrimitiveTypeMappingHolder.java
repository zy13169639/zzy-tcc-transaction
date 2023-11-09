package com.zzy.dt.common.holder;

import com.zzy.dt.common.type.PrimitiveTypeMapping;

import java.util.Base64;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class PrimitiveTypeMappingHolder {

    private static final Set<PrimitiveTypeMapping> PRIMITIVE_TYPE_MAPPINGS = new HashSet<>();

    private static final Set<Class> BASIC_TYPE = new HashSet<>();

    static {
        register(PrimitiveTypeMapping.of(int.class, Integer.class));
        register(PrimitiveTypeMapping.of(byte.class, Byte.class));
        register(PrimitiveTypeMapping.of(long.class, Long.class));
        register(PrimitiveTypeMapping.of(char.class, Character.class));
        register(PrimitiveTypeMapping.of(float.class, Float.class));
        register(PrimitiveTypeMapping.of(double.class, Double.class));
        register(PrimitiveTypeMapping.of(short.class, Short.class));
        register(PrimitiveTypeMapping.of(boolean.class, Boolean.class));

    }

    public static void register(PrimitiveTypeMapping mapping) {
        BASIC_TYPE.add(mapping.getPrimitiveType());
        BASIC_TYPE.add(mapping.getWrapperType());
        PRIMITIVE_TYPE_MAPPINGS.add(mapping);
    }

    public static Class getWrapperClass(Class primitiveClass) {
        return PRIMITIVE_TYPE_MAPPINGS.stream().filter(mapping -> mapping.getPrimitiveType() == primitiveClass).findFirst().map(m -> m.getWrapperType()).orElse(primitiveClass);
    }

    public static boolean isBasicType(Class clazz) {
        return BASIC_TYPE.contains(clazz);
    }
}
