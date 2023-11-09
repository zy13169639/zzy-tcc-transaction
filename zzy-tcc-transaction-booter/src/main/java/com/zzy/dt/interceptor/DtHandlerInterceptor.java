//package com.zzy.dt.interceptor;
//
//import com.zzy.dt.common.Constants;
//import com.zzy.dt.core.TransactionContext;
//import com.zzy.dt.core.DtContextHolder;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//public class DtHandlerInterceptor implements HandlerInterceptor {
//
//    @Override
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        String rootId = request.getHeader(Constants.DT_ROOT_HEADER_NAME);
//        TransactionContext context = DtContextHolder.getTransactionContext();
//        if (rootId == null) {
//            context.setRoot(true);
//            return true;
//        }
//        if (rootId.trim().length() == 0) {
//            throw new IllegalStateException("检查到分布式事务信息，但是没有值");
//        }
//        context.setDtRootId(rootId);
//        return true;
//    }
//
//    @Override
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//        DtContextHolder.removeTransactionContext();
//    }
//}
