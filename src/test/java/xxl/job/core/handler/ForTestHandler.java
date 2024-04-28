package xxl.job.core.handler;

import xxl.job.core.biz.model.ReturnT;

/**
 * For Test Handler
 *
 * @author Fury
 * @since 2024-04-30
 * <p>
 * All rights Reserved.
 */
public class ForTestHandler extends IJobHandler {
    @Override
    public ReturnT<String> execute(String param) throws Exception {
        return ReturnT.SUCCESS;
    }

    @Override
    public void execute() throws Exception {
        // NO Sonar
    }
}
