package com.github.artlibs.testcase;

import com.github.artlibs.autotrace4j.context.AutoTraceCtx;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;

/**
 * XxlJobCase
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class XxlJobCase extends IJobHandler {
    private Tuple injected = null;

    // add since xxl-job v2.3.0
    @Override
    public ReturnT<String> execute(String p) throws Exception {
        injected = new Tuple(
                AutoTraceCtx.getTraceId(),
                AutoTraceCtx.getSpanId(),
                AutoTraceCtx.getParentSpanId()
        );
        return ReturnT.SUCCESS;
    }

    public Tuple getInjected() {
        return injected;
    }

}
