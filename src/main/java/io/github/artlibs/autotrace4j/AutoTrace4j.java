package io.github.artlibs.autotrace4j;

import io.github.artlibs.autotrace4j.exception.LoadInterceptorException;
import io.github.artlibs.autotrace4j.transformer.At4jTransformer;
import io.github.artlibs.autotrace4j.transformer.TransformListener;
import io.github.artlibs.autotrace4j.support.ClassUtils;
import io.github.artlibs.autotrace4j.support.Constants;
import io.github.artlibs.autotrace4j.support.ModuleUtils;
import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static io.github.artlibs.autotrace4j.support.Constants.concat;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

/**
 * Auto Trace for Java
 *
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class AutoTrace4j {
    private AutoTrace4j(){}

    /**
     * Java Attach agentmain
     *
     * @param bizPackages     biz packages
     * @param instrument      Instrumentation
     */
    public static void agentmain(String bizPackages, Instrumentation instrument) throws IOException, URISyntaxException {
        premain(bizPackages, instrument);
    }

    /**
     * Java Agent premain
     *
     * @param bizPackages     biz packages
     * @param instrument      Instrumentation
     */
    public static void premain(String bizPackages, Instrumentation instrument) throws IOException, URISyntaxException {
        if (Objects.isNull(bizPackages) || bizPackages.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Please specify your java package name prefix (Agent parameter)" +
                    " to determine the biz package scope; such as：\n"
                    + "-javaagent:/dir/to/autotrace4j.jar=com.your-domain1.pkg1,com.your-domain2.pkg2");
        }
        String contextPackage = concat(AutoTrace4j.class.getPackage().getName(), ".", "context");
        ClassUtils.injectClassToBootStrap(instrument, contextPackage);
        // note: this method must be called after injectClassToBootStrap, don't move it forward
        ModuleUtils.compatibleJavaModule(contextPackage);
        // do transform
        AutoTrace4j.Transformer.bizScope(bizPackages).on(instrument);
    }


    public static class Transformer {
        private Transformer(){}

        private static List<At4jTransformer> interceptorList = null;
        private static ElementMatcher.Junction<TypeDescription> bizScopeJunction;

        /**
         * 业务类范围限定
         * @param bizPackages -
         * @return TraceBuilder
         */
        public static Transformer bizScope(String bizPackages) {
            setBizScopeJunction(bizPackages);
            return new Transformer();
        }

        /**
         * 在该 instrument 上执行转换器注入
         * @param instrument Instrumentation
         * @throws IOException -
         */
        public void on(Instrumentation instrument) throws IOException, URISyntaxException {
            AgentBuilder builder = this.newAgentBuilder();
            for (At4jTransformer transformer : loadTransformers()) {
                builder = builder.type(transformer.typeMatcher()).transform(transformer);
            }
            builder.installOn(instrument);
        }

        /**
         * Get Intercept Scope Junction
         * @return packagePrefixes Junction
         */
        public static ElementMatcher.Junction<TypeDescription> getBizScopeJunction() {
            return bizScopeJunction;
        }

        /**
         * init an agent builder
         *
         * @return AgentBuilder
         */
        private AgentBuilder newAgentBuilder() {
            return new AgentBuilder.Default().ignore(
                            nameStartsWith("com.intellij.rt.")
                                    .or(nameStartsWith("jdk.jfr."))
                                    .or(nameStartsWith(AutoTrace4j.class.getPackage().getName()))
                    )
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(AgentBuilder.InjectionStrategy.UsingUnsafe.INSTANCE)
                    .with(new TransformListener());
        }

        /**
         * build bizPackages Junction
         * @param bizPackages package prefixes
         */
        private static void setBizScopeJunction(String bizPackages) {
            for (String prefix : bizPackages.split(Constants.COMMA)) {
                if (Objects.isNull(bizScopeJunction)) {
                    bizScopeJunction = nameStartsWith(prefix);
                    continue;
                }
                bizScopeJunction = bizScopeJunction.or(nameStartsWith(prefix));
            }
        }

        /**
         * 从指定包加载所有增强类并为其创建单例对象
         * @return 增强类实例列表
         * @throws IOException -
         * @throws URISyntaxException -
         */
        private static List<At4jTransformer> loadTransformers() throws IOException, URISyntaxException {
            if (Objects.nonNull(interceptorList) && !interceptorList.isEmpty()) {
                return interceptorList;
            }
            interceptorList = new ArrayList<>(64);
            ClassUtils.walkClassFiles((path, classCanonicalName) -> {
                try {
                    Class<?> clazz = Class.forName(classCanonicalName);
                    if (At4jTransformer.class.isAssignableFrom(clazz)) {
                        interceptorList.add((At4jTransformer) clazz.getDeclaredConstructor().newInstance());
                    }
                } catch (Exception e) {
                    throw new LoadInterceptorException(e);
                }
            }, concat(At4jTransformer.class.getPackage().getName(), ".", "impl"), true);

            return interceptorList;
        }
    }
}
