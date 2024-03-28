package artlibs.test.unit;

import com.github.artlibs.autotrace4j.AutoTrace4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import artlibs.test.unit.jdk.ThreadPoolExecutorCase;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

class ThreadPoolExecutorTest {

    @Test
    void test() throws Exception {
        // enhance: ScheduledThreadPoolExecutor, ThreadPoolExecutor
        AutoTrace4j.premain(this.getClass().getPackage()
                .getName() + ".jdk", ByteBuddyAgent.install());

        // test ThreadPoolExecutor enhancement
        assert ThreadPoolExecutorCase.newCase(
                Executors.newFixedThreadPool(5)
        ).run();

        // test ScheduledThreadPoolExecutor enhancement
        assert ThreadPoolExecutorCase.newCase(
                Executors.newScheduledThreadPool(5)
        ).run();
    }

}
