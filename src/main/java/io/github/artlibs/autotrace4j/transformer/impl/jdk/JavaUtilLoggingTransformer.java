package io.github.artlibs.autotrace4j.transformer.impl.jdk;

import io.github.artlibs.autotrace4j.context.TraceInjector;
import io.github.artlibs.autotrace4j.transformer.abs.AbsVisitorTransformer;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Java Util Logging 增强转换器
 * <p>
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
@SuppressWarnings("unused")
public class JavaUtilLoggingTransformer extends AbsVisitorTransformer {
    private static final String RECORD_CLS = "java.util.logging.LogRecord";
    private static final String FORMATTER_CLS = "java.util.logging.Formatter";

    /**
     * {@inheritDoc}
     */
    @Override
    public ElementMatcher<? super TypeDescription> typeMatcher() {
        return hasSuperType(named(FORMATTER_CLS));
    }

    @Override
    protected MethodMatcherHolder methodMatchers() {
        return ofMatcher(isOverriddenFrom(named(FORMATTER_CLS))
                .and(named("format").and(takesArgument(0, named(RECORD_CLS))))
                .or(named("formatMessage").and(takesArgument(0, named(RECORD_CLS)))));
    }

    @Advice.OnMethodExit
    public static void adviceOnMethodExit(@Advice.Return(readOnly = false
            , typing = Assigner.Typing.DYNAMIC) String result) {
        result = TraceInjector.DF.injectTrace(result);
    }
}
