package com.github.artlibs.autotrace4j;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;

class AutoTrace4jTest {

    @Test
    void testRaw() throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, URISyntaxException {
        Instrumentation install = ByteBuddyAgent.install();
        AutoTrace4j.premain("com.your-domain.pkg1,com.your-domain.pkg2", install);
    }

}
