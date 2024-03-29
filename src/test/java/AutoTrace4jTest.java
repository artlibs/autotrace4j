import com.github.artlibs.autotrace4j.AutoTrace4j;
import net.bytebuddy.agent.ByteBuddyAgent;
import testing.artlibs.autotrace4j.jdk.ThreadPoolExecutorCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Executors;

public class AutoTrace4jTest {

    @Test
    public void testThreadPoolExecutor() throws Exception {
        // 01. Setup
        AutoTrace4j.premain(ThreadPoolExecutorCase.class
                .getPackage().getName(), ByteBuddyAgent.install());

        // 02. Exercise
        boolean tpe = ThreadPoolExecutorCase.newCase(
                Executors.newFixedThreadPool(5)
        ).run();
        boolean stp = ThreadPoolExecutorCase.newCase(
                Executors.newScheduledThreadPool(5)
        ).run();

        // 03. Verify
        Assertions.assertTrue(tpe);
        Assertions.assertTrue(stp);
    }

}
