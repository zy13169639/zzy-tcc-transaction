package com.zzy.dt.common.util;

import cn.hutool.core.util.ArrayUtil;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Zhiyang.Zhang
 * @version 1.0
 * @date 2023/10/5 12:04
 */
public class SpringUtil {

    private static DefaultParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private static SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    public static String[] getMethodParameterNames(Method method) {
        return parameterNameDiscoverer.getParameterNames(method);
    }

    public static Object getSpelValue(Method method, String expression, Object[] args) {
        if (ArrayUtil.isEmpty(args)) {
            return null;
        }
        String[] methodParameterNames = getMethodParameterNames(method);
        Expression parseExpression = spelExpressionParser.parseExpression(expression);
        StandardEvaluationContext standardEvaluationContext = new StandardEvaluationContext();
        Map<String, Object> params = new HashMap(args.length, 1);
        for (int i = 0; i < args.length; i++) {
            params.put(methodParameterNames[i], args[i]);
        }
        standardEvaluationContext.setVariables(params);
        return parseExpression.getValue(standardEvaluationContext);
    }
}
