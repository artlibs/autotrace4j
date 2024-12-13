package io.github.artlibs.autotrace4j;

import io.github.artlibs.autotrace4j.context.TraceContext;
import io.github.artlibs.autotrace4j.exception.LoadInterceptorException;
import io.github.artlibs.autotrace4j.logger.Logger;
import io.github.artlibs.autotrace4j.logger.LoggerFactory;
import io.github.artlibs.autotrace4j.transformer.At4jTransformer;
import io.github.artlibs.autotrace4j.transformer.TransformListener;
import io.github.artlibs.autotrace4j.support.ClassUtils;
import io.github.artlibs.autotrace4j.support.ModuleUtils;
import net.bytebuddy.agent.builder.AgentBuilder;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static io.github.artlibs.autotrace4j.logger.LoggerFactory.getLogFileDirectory;
import static io.github.artlibs.autotrace4j.logger.LoggerFactory.loggerEnabled;

/**
 * Auto Trace Agent for Java.
 * <p>
 * AutoTrace4j 是一个基于 ByteBuddy 字节码增强框架实现的轻量级日志串联追踪组件。
 * 其通过 Java Agent 的方式进行使用，通过对逻辑流当中的关键节点进行增强（生成Trace ID、
 * 传递Trace ID），无侵入地实现链路日志串联。
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
     * @param args           Agent Arguments
     * @param instrument     Instrumentation
     */
    public static void premain(String args, Instrumentation instrument) throws IOException, URISyntaxException {
        agentmain(args, instrument);
    }

    /**
     * Attach API 入口
     * @param args           Agent Arguments
     * @param instrument     Instrumentation
     */
    public static void agentmain(String args, Instrumentation instrument) throws IOException, URISyntaxException {
        AutoTrace4j.Transformer.with(args).on(instrument);
    }

    public static class Transformer {
        private static final Logger logger = LoggerFactory.getLogger(Transformer.class);
        private static List<At4jTransformer> transformerList = null;

        private Transformer(String args){
            logger.debug("args: {}", args);
        }

        /**
         * @param args Agent参数
         * @return TraceBuilder
         */
        public static Transformer with(String args) {
            return new Transformer(args);
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
                logger.debug("已开启[autotrace4j]日志：" + getLogFileDirectory());
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
         * 创建一个 ByteBuddy AgentBuilder 实例，忽略掉部分转换筛选范围：
         *   - jdk.jfr.*
         *   - com.intellij.rt.*
         *   - io.github.artlibs.autotrace4j.*
         * <p>
         * @return AgentBuilder 一个 ByteBuddy Agent Builder
         */
        private AgentBuilder newAgentBuilder() {
            return new AgentBuilder.Default()
                    .ignore(nameStartsWith("jdk.jfr.")
                            .or(nameStartsWith("com.intellij.rt."))
                            .or(nameStartsWith(AutoTrace4j.class.getPackage().getName()))
                    )
                    .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION)
                    .with(AgentBuilder.InjectionStrategy.UsingUnsafe.INSTANCE)
                    .with(new TransformListener());
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
