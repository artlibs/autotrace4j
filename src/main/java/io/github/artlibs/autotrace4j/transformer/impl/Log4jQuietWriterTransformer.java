package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import static io.github.artlibs.autotrace4j.context.TraceContext.injectTraceId;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Log4j的输出注入
 * <p>
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class Log4jQuietWriterTransformer extends AbsVisitorTransformer {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("org.apache.log4j.helpers.QuietWriter"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isPublic()
                .and(named("write"))
                .and(takesArgument(0, String.class))
                .and(returns(void.class)));
    }

    /**
     * OnMethodEnter
     * @param message -
     */
    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) String message) {
        message = injectTraceId(message);
    }

}
