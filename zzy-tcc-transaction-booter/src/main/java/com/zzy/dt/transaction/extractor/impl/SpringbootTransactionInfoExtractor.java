package com.zzy.dt.transaction.extractor.impl;

import com.zzy.dt.common.Constants;
import com.zzy.dt.common.exception.DtCommonException;
import com.zzy.dt.common.util.WebUtil;
import com.zzy.dt.context.TransactionInfo;
import com.zzy.dt.transaction.extractor.TransactionInfoExtractor;
import com.zzy.jdbc.holder.ResourceManager;

import javax.servlet.http.HttpServletRequest;

public class SpringbootTransactionInfoExtractor implements TransactionInfoExtractor {

    @Override
    public TransactionInfo extract() {
        HttpServletRequest request = WebUtil.getHttpRequest();
        if (request == null) {
            throw new DtCommonException(Constants.SYSTEM_ERROR, "不是http请求");
        }
        String rootId = request.getHeader(Constants.DT_ROOT_HEADER_NAME);
        String from = request.getHeader(Constants.DT_FROM);
        TransactionInfo.TransactionInfoBuilder builder = TransactionInfo.builder();
        if (rootId == null) {
            builder.isRoot(true);
            builder.from(ResourceManager.getApplication());
        } else {
            if (rootId.trim().length() == 0) {
                throw new IllegalStateException("检查到分布式事务信息，但是值为空");
            }
            builder.rootId(rootId);
            builder.from(from);
        }
        return builder.build();
    }
}
