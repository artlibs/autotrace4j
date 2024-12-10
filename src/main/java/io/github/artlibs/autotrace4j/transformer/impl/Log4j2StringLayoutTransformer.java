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
public class Log4j2StringLayoutTransformer extends AbsVisitorTransformer {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        // log4j2: org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender
        // method: directEncodeEvent & writeByteArrayToManager -> layout:encode & layout:toByteArray
        // StringLayout有诸多实现：pattern, json, xml, yaml, csv等等，需要注意可能的破坏格式
        return hasSuperType(named("org.apache.logging.log4j.core.StringLayout"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isPublic()
                .and(named("toSerializable"))
                .and(takesArgument(0, hasSuperType(named("org.apache.logging.log4j.core.LogEvent"))))
                .and(returns(String.class)));
    }

    /**
     * OnMethodExit
     * @param serialized -
     */
    @Advice.OnMethodExit
    public static void adviceOnMethodExit(
            @Advice.Return(typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) String serialized) {
        serialized = TraceInjector.DF.injectTrace(serialized);
    }

}
