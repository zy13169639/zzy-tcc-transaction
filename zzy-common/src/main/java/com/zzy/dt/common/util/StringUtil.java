package com.zzy.dt.common.util;

import cn.hutool.core.collection.CollectionUtil;

import java.util.Collection;
import java.util.stream.Collectors;

public class StringUtil {

    public static <E> String toString(Collection<E> collection) {
        if (CollectionUtil.isEmpty(collection)) {
            return "";
        }
        return collection.stream().map(o -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("\"");
            if (o != null) {
                stringBuilder.append(o.toString());
            }
            stringBuilder.append("\"");
            return stringBuilder.toString();
        }).collect(Collectors.joining(",", "[", "]"));
    }
}
