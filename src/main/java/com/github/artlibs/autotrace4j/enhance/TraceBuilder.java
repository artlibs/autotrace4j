package com.github.artlibs.autotrace4j.enhance;

import com.github.artlibs.autotrace4j.AutoTrace4j;
import com.github.artlibs.autotrace4j.enhance.interceptor.*;
import com.github.artlibs.autotrace4j.enhance.wrapper.DelegateWrapper;
import com.github.artlibs.autotrace4j.enhance.wrapper.InstanceWrapper;
import com.github.artlibs.autotrace4j.enhance.wrapper.StaticWrapper;
import com.github.artlibs.autotrace4j.support.Constants;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.Morph;
import net.bytebuddy.matcher.ElementMatcher;
import java.io.*;
import java.lang.instrument.Instrumentation;
import java.util.*;
import static net.bytebuddy.matcher.ElementMatchers.*;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

/**
 * 功能：构造一个 Byte Buddy Agent
 *
 * @author Fury
 * @since 2023-01-05
 *
 * All rights Reserved.
 */
public final class TraceBuilder {
    private static ElementMatcher.Junction<TypeDescription> packagePrefixesJunction;

    private TraceBuilder(){}

    /**
     * 构建一个 TraceBuilder 实例
     * @param enhancePackagePrefixes 待增强的packagePrefixes
     * @return TraceBuilder
     */
    public static TraceBuilder enhance(String enhancePackagePrefixes) {
        System.out.println("Enhance on package prefixes: " + enhancePackagePrefixes);
        processPackagePrefix(enhancePackagePrefixes);
        return new TraceBuilder();
    }

    /**
     * Transform codes on instrument
     * @param instrument Instrumentation
     * @throws IOException -
     * @throws InstantiationException -
     * @throws IllegalAccessException -
     * @throws ClassNotFoundException -
     */
    public void on(Instrumentation instrument) throws IOException, InstantiationException,
            IllegalAccessException, ClassNotFoundException {
        AgentBuilder agentBuilder = this.getAgentBuilder();
        for (Interceptor interceptor : Loader.load()) {
            if (Objects.isNull(interceptor.typeMatcher()) || Objects.isNull(interceptor.methodMatcher())) {
                continue;
            }

            agentBuilder = agentBuilder.type(interceptor.typeMatcher()).transform((builder, typeDescription, classLoader, module) -> {
                DynamicType.Builder<?> newBuilder = interceptor.transformType(builder, typeDescription, classLoader);
                if (Objects.isNull(newBuilder)) {
                    newBuilder = builder;
                }

                if (InterceptType.DELEGATE == interceptor.interceptType()) {
                    return this.delegateEnhancer(newBuilder, interceptor, typeDescription);
                } else if (InterceptType.VISITOR == interceptor.interceptType()) {
                    Visitor visitorEnhancer = (Visitor) interceptor;
                    return newBuilder.visit(Advice.to(visitorEnhancer.visitor())
                            .on(isMethod().and(interceptor.methodMatcher())));
                }

                return newBuilder;
            });
        }
        agentBuilder.installOn(instrument);
    }

    /**
     * Get packagePrefixes Junction
     * @return packagePrefixes Junction
     */
    public static ElementMatcher.Junction<TypeDescription> getPackagePrefixesJunction() {
        return packagePrefixesJunction;
    }

    /**
     * 构建一个 Byte buddy AgentBuilder 来转换增强代码
     * @return Byte buddy AgentBuilder
     * @throws ClassNotFoundException -
     * @throws IOException -
     * @throws InstantiationException -
     * @throws IllegalAccessException -
     */
    private AgentBuilder getAgentBuilder() throws ClassNotFoundException,
            IOException, InstantiationException, IllegalAccessException {
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
     * @param typeDescription 待增强类的TypeDescription
     * @return DynamicType.Builder
     */
    private DynamicType.Builder<?> delegateEnhancer(DynamicType.Builder<?> builder, Interceptor interceptor, TypeDescription typeDescription) {
        try {
            DelegateWrapper<?> enhancerWrapper;
            if (((Delegate<?>) interceptor).enhanceStaticMethod()) {
                enhancerWrapper = StaticWrapper.wrap((Static) interceptor);
            } else {
                enhancerWrapper = InstanceWrapper.wrap((Instance) interceptor);
            }

            return builder.method(isMethod().and(interceptor.methodMatcher()))
                    .intercept(MethodDelegation.withDefaultConfiguration()
                            .withBinders(Morph.Binder.install(MorphCallable.class))
                            .to(enhancerWrapper));
        } catch (Exception e) {
            System.err.println("Failed to transform " + typeDescription.getCanonicalName());
            e.printStackTrace();
            return builder;
        }
    }

    /**
     * 定义要增强时忽略的前缀
     * @return ElementMatcher
     * @throws ClassNotFoundException -
     * @throws InstantiationException -
     * @throws IllegalAccessException -
     * @throws IOException -
     */
    private ElementMatcher<? super TypeDescription> buildIgnore() throws ClassNotFoundException,
            InstantiationException, IllegalAccessException, IOException {
        ElementMatcher.Junction<? super TypeDescription> junction = any();
        for (Interceptor interceptor : Loader.load()) {
            junction = junction.and(not(interceptor.typeMatcher()));
        }

        return junction;
    }

    /**
     * 处理增强包前缀，构建packagePrefixes Junction
     * @param enhancePackagePrefixes 增强包前缀
     */
    private static void processPackagePrefix(String enhancePackagePrefixes) {
        for (String prefix : enhancePackagePrefixes.split(Constants.COMMA)) {
            if (Objects.isNull(packagePrefixesJunction)) {
                packagePrefixesJunction = nameStartsWith(prefix);
                continue;
            }

            packagePrefixesJunction = packagePrefixesJunction
                    .or(nameStartsWith(prefix));
        }
    }

}
