package com.github.artlibs.autotrace4j.core;

import com.github.artlibs.autotrace4j.AutoTrace4j;
import com.github.artlibs.autotrace4j.core.interceptor.*;
import com.github.artlibs.autotrace4j.core.wrapper.AbstractDelegateWrapper;
import com.github.artlibs.autotrace4j.core.wrapper.InstanceInterceptorWrapper;
import com.github.artlibs.autotrace4j.core.wrapper.StaticInterceptorWrapper;
import com.github.artlibs.autotrace4j.exception.LoadInterceptorException;
import com.github.artlibs.autotrace4j.utils.ClassUtils;
import com.github.artlibs.autotrace4j.utils.Constants;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.*;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

/**
 * Build a Byte Buddy Agent
 *
 * @author Fury
 * @since 2023-01-05
 *
 * All rights Reserved.
 */
public final class TraceAgentBuilder {
    private TraceAgentBuilder(){}

    private static List<Interceptor> interceptorList = null;
    private static ElementMatcher.Junction<TypeDescription> interceptScopeJunction;

    /**
     * 构建一个 TraceBuilder 实例
     * @param packagePrefixes -
     * @return TraceBuilder
     */
    public static TraceAgentBuilder intercept(String packagePrefixes) {
        interceptPackagePrefixes(packagePrefixes);
        return new TraceAgentBuilder();
    }

    /**
     * Transform codes on instrument
     * @param instrument Instrumentation
     * @throws IOException -
     */
    public void on(Instrumentation instrument) throws IOException, URISyntaxException {
        AgentBuilder agentBuilder = this.getAgentBuilder();
        for (Interceptor interceptor : loadInterceptor()) {
            if (Objects.isNull(interceptor.typeMatcher()) || Objects.isNull(interceptor.methodMatcher())) {
                continue;
            }

            agentBuilder = agentBuilder.type(interceptor.typeMatcher())
                    .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) -> {
                DynamicType.Builder<?> newBuilder = interceptor.transformType(builder, typeDescription, classLoader);
                if (Objects.isNull(newBuilder)) {
                    newBuilder = builder;
                }

                switch (interceptor.interceptType()) {
                    case VISITOR:
                        AbstractVisitorInterceptor visitorEnhancer = (AbstractVisitorInterceptor) interceptor;
                        return newBuilder.visit(Advice.to(visitorEnhancer.visitor())
                                .on(isMethod().and(interceptor.methodMatcher())));
                    case DELEGATE:
                        return this.delegateInterceptor(newBuilder, interceptor);
                    default:
                        return newBuilder;
                }
            });
        }
        agentBuilder.installOn(instrument);
    }

    /**
     * Get Intercept Scope Junction
     * @return packagePrefixes Junction
     */
    public static ElementMatcher.Junction<TypeDescription> getInterceptScopeJunction() {
        return interceptScopeJunction;
    }

    /**
     * 构建一个 Byte buddy AgentBuilder 来转换增强代码
     * @return Byte buddy AgentBuilder
     * @throws IOException -
     */
    private AgentBuilder getAgentBuilder() throws IOException, URISyntaxException {
        return new AgentBuilder.Default()
                .ignore(this.buildIgnore())
                .ignore(nameStartsWith("com.intellij.rt.")
                            .or(nameStartsWith("jdk.jfr."))
                            .or(nameStartsWith("com.alibaba.csp."))
                            .or(nameStartsWith("org.apache.skywalking."))
                            .or(nameStartsWith("com.navercorp.pinpoint."))
                            .or(nameStartsWith(AutoTrace4j.class.getPackage().getName()))
                            .or(nameStartsWith("org.springframework.boot.devtools"))
                            .or(nameStartsWith("org.springframework.cloud.sleuth."))
                            .or(isAnnotatedWith(named("org.springframework.boot.autoconfigure.SpringBootApplication"))))
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION).with(new AutoListener());
    }

    /**
     * 代理增强方式
     * @param builder DynamicType.Builder
     * @param interceptor Enhancer 增强器实现
     * @return DynamicType.Builder
     */
    private DynamicType.Builder<?> delegateInterceptor(DynamicType.Builder<?> builder, Interceptor interceptor) {
        try {
            AbstractDelegateWrapper<?> wrapper;
            if (((AbstractDelegateInterceptor<?>) interceptor).enhanceStaticMethod()) {
                wrapper = StaticInterceptorWrapper.wrap((AbstractStaticInterceptor) interceptor);
            } else {
                wrapper = InstanceInterceptorWrapper.wrap((AbstractInstanceInterceptor) interceptor);
            }

            return builder.method(isMethod().and(interceptor.methodMatcher()))
                    .intercept(MethodDelegation.withDefaultConfiguration()
                            .withBinders(Morph.Binder.install(MorphCallable.class))
                            .to(wrapper));
        } catch (Exception e) {
            e.printStackTrace();
            return builder;
        }
    }

    /**
     * 定义要增强时忽略的前缀
     * @return ElementMatcher
     * @throws IOException -
     */
    private ElementMatcher<? super TypeDescription> buildIgnore() throws IOException, URISyntaxException {
        ElementMatcher.Junction<? super TypeDescription> junction = any();
        for (Interceptor interceptor : loadInterceptor()) {
            junction = junction.and(not(interceptor.typeMatcher()));
        }

        return junction;
    }

    /**
     * build packagePrefixes Junction
     * @param packagePrefixes 增强包前缀
     */
    private static void interceptPackagePrefixes(String packagePrefixes) {
        for (String prefix : packagePrefixes.split(Constants.COMMA)) {
            if (Objects.isNull(interceptScopeJunction)) {
                interceptScopeJunction = nameStartsWith(prefix);
                continue;
            }

            interceptScopeJunction = interceptScopeJunction
                    .or(nameStartsWith(prefix));
        }
    }

    private static List<Interceptor> loadInterceptor() throws IOException, URISyntaxException {
        if (Objects.nonNull(interceptorList) && !interceptorList.isEmpty()) {
            return interceptorList;
        }
        interceptorList = new ArrayList<>(16);

        ClassUtils.walkClassFiles((path, classCanonicalName) -> {
            try {
                Class<?> clazz = Class.forName(classCanonicalName);
                if (Interceptor.class.isAssignableFrom(clazz)) {
                    interceptorList.add((Interceptor) clazz.newInstance());
                }
            } catch (Exception e) {
                throw new LoadInterceptorException(e);
            }
        }, Interceptor.class.getPackage().getName() + ".impl");

        return interceptorList;
    }
}
