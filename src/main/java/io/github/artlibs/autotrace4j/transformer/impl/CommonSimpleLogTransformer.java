package io.github.artlibs.autotrace4j.transformer.impl;

import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import static io.github.artlibs.autotrace4j.context.TraceContext.injectTraceId;
import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Commons Logging SimpleLog 增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public class CommonSimpleLogTransformer extends AbsVisitorTransformer {

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named("org.apache.commons.logging.impl.SimpleLog"));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isProtected()
                .and(named("write"))
                .and(takesArgument(0, StringBuffer.class))
                .and(returns(void.class)));
    }

    /**
     * OnMethodEnter
     * @param buffer -
     */
    @Advice.OnMethodEnter
    public static void adviceOnMethodEnter(
            @Advice.Argument(value = 0, typing = Assigner.Typing.DYNAMIC
                    , readOnly = false) StringBuffer buffer) {
        buffer = new StringBuffer(injectTraceId(buffer.toString()));
    }

}
