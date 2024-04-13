package com.github.artlibs.autotrace4j.interceptor;

import com.github.artlibs.autotrace4j.AutoTrace4j;
import com.github.artlibs.autotrace4j.exception.LoadInterceptorException;
import com.github.artlibs.autotrace4j.interceptor.base.AbstractVisitorInterceptor;
import com.github.artlibs.autotrace4j.support.ClassUtils;
import com.github.artlibs.autotrace4j.support.Constants;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.*;

/**
 * Build a Byte Buddy Agent
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public final class Transformer {
    private Transformer(){}

    private static List<Interceptor> interceptorList = null;
    private static ElementMatcher.Junction<TypeDescription> interceptScopeJunction;

    /**
     * build an InterceptorBuilder
     * @param enhancePackages -
     * @return TraceBuilder
     */
    public static Transformer intercept(String enhancePackages) {
        interceptPackages(enhancePackages);
        return new Transformer();
    }

    /**
     * Transform codes on instrument
     * @param instrument Instrumentation
     * @throws IOException -
     */
    public void on(Instrumentation instrument) throws IOException, URISyntaxException {
        AgentBuilder agentBuilder = this.newAgentBuilderWithIgnore();
        for (Interceptor interceptor : loadInterceptor()) {
            agentBuilder = agentBuilder
                .type(interceptor.typeMatcher())
                .transform((builder, typeDescription, classLoader, javaModule, protectionDomain) -> {
                    DynamicType.Builder<?> newBuilder = interceptor.doTypeTransform(
                        builder, typeDescription, javaModule, classLoader
                    );
                    if (Objects.isNull(newBuilder)) {
                        newBuilder = builder;
                    }
                    // for jdk class, using asm visitor mode to intercept
                    if (interceptor.isVisitorMode()) {
                        AbstractVisitorInterceptor visitor = (AbstractVisitorInterceptor) interceptor;
                        newBuilder = visitor.visit(newBuilder);
                    } else {
                        // for other class, using method delegate mode to intercept
                        // this mode supports to add fields to the target object class.
                        newBuilder = newBuilder
                            .method(isMethod().and(interceptor.methodMatcher()))
                            .intercept(
                                MethodDelegation
                                    .withDefaultConfiguration()
                                    .withBinders(Morph.Binder.install(MorphCall.class))
                                    .to(interceptor)
                            );
                    }
                    return newBuilder == null ? builder : newBuilder;
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
     * init an agent builder with ignore
     *
     * @return AgentBuilder
     */
    private AgentBuilder newAgentBuilderWithIgnore() {
        return new AgentBuilder.Default().ignore(
                nameStartsWith("com.intellij.rt.")
                    .or(nameStartsWith("jdk.jfr."))
                    .or(nameStartsWith("com.alibaba.csp."))
                    .or(nameStartsWith("org.apache.skywalking."))
                    .or(nameStartsWith("com.navercorp.pinpoint."))
                    .or(nameStartsWith(AutoTrace4j.class.getPackage().getName()))
                    .or(nameStartsWith("org.springframework.boot.devtools"))
                    .or(nameStartsWith("org.springframework.cloud.sleuth."))
                    .or(isAnnotatedWith(named("org.springframework.boot.autoconfigure.SpringBootApplication")))
            )
            .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
            .with(AgentBuilder.InjectionStrategy.UsingUnsafe.INSTANCE)
            .with(new AutoListener());
    }

    /**
     * build packagePrefixes Junction
     * @param packagePrefixes package prefixes
     */
    private static void interceptPackages(String packagePrefixes) {
        for (String prefix : packagePrefixes.split(Constants.COMMA)) {
            if (Objects.isNull(interceptScopeJunction)) {
                interceptScopeJunction = nameStartsWith(prefix);
                continue;
            }
            interceptScopeJunction = interceptScopeJunction
                    .or(nameStartsWith(prefix));
        }
    }

    /**
     * load interceptor classes
     * @return list of interceptors
     * @throws IOException -
     * @throws URISyntaxException -
     */
    private static List<Interceptor> loadInterceptor() throws IOException, URISyntaxException {
        if (Objects.nonNull(interceptorList) && !interceptorList.isEmpty()) {
            return interceptorList;
        }
        interceptorList = new ArrayList<>(32);
        ClassUtils.walkClassFiles((path, classCanonicalName) -> {
            try {
                Class<?> clazz = Class.forName(classCanonicalName);
                if (Interceptor.class.isAssignableFrom(clazz)) {
                    interceptorList.add((Interceptor) clazz.newInstance());
                }
            } catch (Exception e) {
                throw new LoadInterceptorException(e);
            }
        }, Interceptor.class.getPackage().getName() + ".impl", true);

        return interceptorList;
    }
}
