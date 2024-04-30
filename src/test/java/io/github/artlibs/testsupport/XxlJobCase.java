package io.github.artlibs.testsupport;

import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.ForTestHandler;
import com.xxl.job.core.handler.annotation.XxlJob;

/**
 * XxlJobCase, we copy the related class and not ref the xxl-job pom
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public class XxlJobCase {
    //@JobHandler(value = "mySchedule")
    public static class BeforeV212 extends ForTestHandler implements XxlJobCaseRunner,Injected {
        // before v2.1.2, there is no @XxlJob, should have @JobHandler

        @Override
        public ReturnT<String> execute(String param){
            graspInjected();
            return ReturnT.SUCCESS;
        }

        @Override
        public void trigger() {
            this.execute("args");
        }
    }

    //@JobHandler(value = "mySchedule")
    public static class AfterV212Case1 extends ForTestHandler implements XxlJobCaseRunner,Injected {
        // after v2.1.2, there is @XxlJob, should have @JobHandler
        @Override
        public ReturnT<String> execute(String param) {
            graspInjected();
            return ReturnT.SUCCESS;
        }

        @Override
        public void trigger() {
            this.execute("args");
        }
    }

    //@JobHandler(value = "mySchedule")
    public static class AfterV212Case2 extends ForTestHandler implements XxlJobCaseRunner,Injected {
        // after v2.1.2, there is @XxlJob, should have @JobHandler
        @Override
        @XxlJob(value = "mySchedule")
        public ReturnT<String> execute(String param) {
            graspInjected();
            return ReturnT.SUCCESS;
        }

        @Override
        public void trigger() {
            this.execute("args");
        }
    }

    public static class AfterV212Case3 implements XxlJobCaseRunner,Injected {
        // after v2.1.2, there is @XxlJob
        @XxlJob(value = "mySchedule")
        public void myScheduleTask() {
            graspInjected();
        }

        @Override
        public void trigger() {
            this.myScheduleTask();
        }
    }

    public static class AfterV230Case1 extends ForTestHandler implements XxlJobCaseRunner,Injected {
        // add since v2.3.0
        @Override
        public void execute() {
            graspInjected();
        }

        @Override
        public void trigger() {
            this.execute();
        }
    }

    public static class AfterV230Case2 extends ForTestHandler implements XxlJobCaseRunner,Injected {
        // add since v2.3.0
        @Override
        @XxlJob(value = "mySchedule")
        public void execute() {
            graspInjected();
        }

        @Override
        public void trigger() {
            this.execute();
        }
    }

    public interface XxlJobCaseRunner {
        void trigger();
    }
}
