package io.github.artlibs.testsupport;

import tech.powerjob.worker.core.processor.ProcessResult;
import tech.powerjob.worker.core.processor.TaskContext;
import tech.powerjob.worker.core.processor.sdk.BasicProcessor;

public class PowerJobCase implements BasicProcessor, Injected {
    @Override
    public ProcessResult process(TaskContext context) throws Exception {
        graspInjected();
        return new ProcessResult(true);
    }

}
