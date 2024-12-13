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
public class Log4j2StringLayoutTransformer extends AbsVisitorTransformer {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        // log4j2: org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender
        // method: directEncodeEvent & writeByteArrayToManager -> layout:encode & layout:toByteArray
        // StringLayout有诸多实现：pattern, json, xml, yaml, csv等等，需要注意可能的破坏格式
        return hasSuperType(named("org.apache.logging.log4j.core.layout.Encoder"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isPublic()
                .and(named("encode"))
                .and(takesArgument(0, StringBuilder.class))
                .and(takesArgument(1, hasSuperType(named("org.apache.logging.log4j.core.layout.ByteBufferDestination"))))
                .and(returns(void.class))
        );
    }

    /**
     * OnMethodEnter
     * @param builder -
     */
    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) StringBuilder builder) {
        builder = new StringBuilder(TraceInjector.DF.injectTrace(builder.toString()));
    }

}
