package io.github.artlibs.autotrace4j.transformer;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.matcher.ElementMatcher;
import net.bytebuddy.utility.JavaModule;


/**
 * AutoTrace Transformer
 *
 * @author Fury
 * @since 2024-03-30
 *
 * All rights Reserved.
 */
public interface At4jTransformer extends AgentBuilder.Transformer {
    /**
     * 类型匹配器，用来筛选需要转换增强的类
     * <p>
     * @return Type Matcher
     */
    ElementMatcher<? super TypeDescription> typeMatcher();

    /**
     * 类型转换，可在此处为需要转换增强的类增加属性字段
     * <p>
     * @param builder origin DynamicType.Builder
     * @param typeDescription TypeDescription
     * @param classLoader ClassLoader
     * @param module JavaModule
     * @return new DynamicType.Builder
     */
    default DynamicType.Builder<?> transformType(
            DynamicType.Builder<?> builder,
            TypeDescription typeDescription,
            JavaModule module,
            ClassLoader classLoader) {
        return builder;
    }
}
