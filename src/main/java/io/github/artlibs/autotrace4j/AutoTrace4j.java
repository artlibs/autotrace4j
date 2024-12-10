package io.github.artlibs.autotrace4j;

import io.github.artlibs.autotrace4j.context.TraceContext;
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

import static io.github.artlibs.autotrace4j.logger.LoggerFactory.getLogFileDirectory;
import static io.github.artlibs.autotrace4j.logger.LoggerFactory.loggerEnabled;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;

/**
 * Auto Trace Agent for Java.
 * <p>
 * AutoTrace4j 是一个借鉴 SkyWalking 基于 ByteBuddy 字节码增强框架实现的
 * 轻量级日志串联追踪组件。其通过 Java Agent 的方式进行使用，通过对逻辑流当中
 * 的关键节点进行增强（生成Trace ID、传递Trace ID），无侵入地实现链路日志串联。
 * <p>
 * @author Fury
 * @since 2024-03-30
 * <p>
 * All rights Reserved.
 */
public final class AutoTrace4j {
    private AutoTrace4j(){}

    /**
     * Java Agent 入口
     *
     * @param bizPackages     业务包前缀，用以辅助筛选增强范围
     * @param instrument      Instrumentation
     */
    public static void premain(String bizPackages, Instrumentation instrument) throws IOException, URISyntaxException {
        agentmain(bizPackages, instrument);
    }

    /**
     * Attach API 入口
     *
     * @param bizPackages     业务包前缀，用以辅助筛选增强范围
     * @param instrument      Instrumentation
     */
    public static void agentmain(String bizPackages, Instrumentation instrument) throws IOException, URISyntaxException {
        if (Objects.isNull(bizPackages) || bizPackages.trim().isEmpty()) {
            throw new IllegalArgumentException(
                "Please specify your biz package prefix (Agent parameter)" +
                    " to determine the biz intercept scope; e.g.：\n"
                    + "-javaagent:/dir/to/autotrace4j.jar=com.your-domain1.pkg,com.your-domain2.pkg");
        }

        AutoTrace4j.Transformer.withBizScope(bizPackages).on(instrument);
    }

    public static class Transformer {
        private Transformer(){}

        private static List<At4jTransformer> transformerList = null;
        private static ElementMatcher.Junction<TypeDescription> bizScopeJunction;

        /**
         * 业务类范围限定，用以辅助筛选增强范围
         * <p>
         * @param bizPackages     业务包前缀，用以辅助筛选增强范围
         * @return TraceBuilder
         */
        public static Transformer withBizScope(String bizPackages) {
            setBizScopeJunction(bizPackages);
            return new Transformer();
        }

        /**
         * 在该 Instrumentation 上执行转换器的注入
         * <p>
         * @param instrument Instrumentation
         * @throws IOException -
         */
        public void on(Instrumentation instrument) throws IOException, URISyntaxException {
            String contextPackage = AutoTrace4j.class.getPackage().getName() + ".context";

            // inject context class into bootstrap loader
            ClassUtils.injectClassToBootStrap(instrument, contextPackage);

            // note: this method must be called after injectClassToBootStrap
            ModuleUtils.compatibleJavaModule(contextPackage);

            if (loggerEnabled()) {
                System.err.println("已开启[autotrace4j]日志：" + getLogFileDirectory());
            }

            AgentBuilder builder = this.newAgentBuilder();
            for (At4jTransformer transformer : loadTransformers()) {
                builder = builder.type(transformer.typeMatcher()).transform(transformer);
            }
            builder.installOn(instrument);

            // init trace for main thread.
            TraceContext.setSpanId(TraceContext.generate());
            TraceContext.setTraceId(TraceContext.generate());
        }

        /**
         * 获取业务范围判断器
         * <p>
         * @return Junction
         */
        public static ElementMatcher.Junction<TypeDescription> getBizScopeJunction() {
            return bizScopeJunction;
        }

        /**
         * 创建一个 ByteBuddy AgentBuilder 实例，忽略掉部分转换筛选范围：
         *   - com.intellij.rt.*
         *   - jdk.jfr.*
         *   - io.github.artlibs.autotrace4j.*
         * <p>
         * @return AgentBuilder 一个 ByteBuddy Agent Builder
         */
        private AgentBuilder newAgentBuilder() {
            return new AgentBuilder.Default()
                    .ignore(nameStartsWith("com.intellij.rt.")
                            .or(nameStartsWith("jdk.jfr."))
                            .or(nameStartsWith(AutoTrace4j.class.getPackage().getName()))
                    )
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(AgentBuilder.InjectionStrategy.UsingUnsafe.INSTANCE)
                    .with(new TransformListener());
        }

        /**
         * 构建业务类范围限定的判断器实例
         * <p>
         * @param bizPackages 业务包前缀
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
         * <p>
         * @return 增强类实例列表
         * @throws IOException -
         * @throws URISyntaxException -
         */
        private static List<At4jTransformer> loadTransformers() throws IOException, URISyntaxException {
            if (Objects.nonNull(transformerList) && !transformerList.isEmpty()) {
                return transformerList;
            }

            transformerList = new ArrayList<>(64);
            ClassUtils.walkClassFiles((path, classCanonicalName) -> {
                try {
                    Class<?> clazz = Class.forName(classCanonicalName);
                    if (At4jTransformer.class.isAssignableFrom(clazz)) {
                        transformerList.add((At4jTransformer) clazz.getDeclaredConstructor().newInstance());
                    }
                } catch (Exception e) {
                    throw new LoadInterceptorException(e);
                }
            }, At4jTransformer.class.getPackage().getName() + ".impl", true);

            return transformerList;
        }
    }
}
