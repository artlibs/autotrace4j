package tech.powerjob.worker.core.processor;

public class ProcessResult {

    private boolean success = false;

    private String msg;

    public ProcessResult(boolean success) {
        this.success = success;
    }

}
