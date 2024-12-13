package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.context.TraceInjector;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Logback 增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class LogbackOutStreamTransformer extends AbsVisitorTransformer {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        // Supports FileAppender, RollingFileAppender, ConsoleAppender e.g.
        return hasSuperType(named("ch.qos.logback.core.OutputStreamAppender"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isPrivate()
                .and(named("writeBytes"))
                .and(takesArgument(0, byte[].class))
                .and(returns(void.class)));
    }

    /**
     * OnMethodEnter
     * @param byteArray -
     */
    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) byte[] byteArray) {
        byteArray = TraceInjector.DF.injectTrace(new String(byteArray)).getBytes();
    }

}
