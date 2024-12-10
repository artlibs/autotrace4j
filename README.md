## Auto Trace for Java
[![Run Tests](https://github.com/artlibs/autotrace4j/actions/workflows/testing.yml/badge.svg)](https://github.com/artlibs/autotrace4j/actions/workflows/testing.yml) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.artlibs/autotrace4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.artlibs/autotrace4j/)  [![Release](https://img.shields.io/github/release/artlibs/autotrace4j.svg?style=flat-square)](https://github.com/artlibs/autotrace4j/releases)  [![License: Apache 2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

​	`autotrace4j`是一个基于ByteBuddy编写的轻量级日志跟踪工具，其基本逻辑是在各个上下文当中通过代码增强关键节点来传递`trace id`，最后在日志输出时注入到输出结果当中，以实现日志的跟踪串联。

#### 易使用

​	基于Agent的方式来使用该工具，对业务代码无侵入。

#### 轻量级

​	只依赖ByteBuddy，且增加的增强代码只是往Thread Local当中写入字符串或读出字符串，没有做额外事项，不会增加性能开销。

## Startup

​	`autotrace4j`的使用非常简单，只需从[release](https://github.com/artlibs/autotrace4j/releases)中下载最新的agent jar包，在启动脚本中以agent方式运行：

```shell
$ java -javaagent=/dir/to/autotrace4j.jar=com.your-domain.biz1.pkg1,com.your-domain.biz2.pkg2 -Dautotrace4j.log.enable=true  -jar YourJar.jar  # 省略其他无关参数
```

-   一般情况下不需要开启autotrace4j内部日志，即***不需要***`-Dautotrace4j.log.enable=true`，如果需要观测到增强过程增强了哪些类，或在调试autotrace4j时有需要，可开启该日志选项，其他选项可使用默认参数：
    -   `-Dautotrace4j.log.enable=true` 设置开启autotrace4j内部日志
    -   `-Dautotrace4j.log.dir=/path/to/your/log/dir` 修改autotrace4j内部日志保存路径
    -   `-Dautotrace4j.log.level=TRACE` （可选`TRACE`, `DEBUG`, `INFO`, `WARN`, `ERROR`）修改日志级别
    -   `-Dautotrace4j.log.file.retention=5` 设置autotrace4j内部日志保留天数为5天
    -   `-Dautotrace4j.log.file.size=10485760`设置单个文件大小为10M(`10*1024*1024=10485760`)

#### 关于日志

可通过如下系统属性开启和设置日志来观察增强过程：

-   `autotrace4j.log.enable`：是否开启autotrace4j日志，默认`false`
-   `autotrace4j.log.dir`：autotrace4j日志文件保存路径，默认为临时目录`java.io.tmpdir`
-   `autotrace4j.log.level`：autotrace4j日志级别，默认为`DEBUG`
-   `autotrace4j.log.file.retention`：autotrace4j日志文件保留时间，单位天，默认为`7`天
-   `autotrace4j.log.file.size`：autotrace4j日志文件大小限制，单位字节(`B`)，默认为`0`表示不限制

#### 关于`MDC`

可通过`slf4j`或者`log4j`的`MDC`获取当前上下文的Trace ID：

-   当通过 `MDC.get("X-Ato-Span-Id")`时返回当前上下文的 `SpanId`
-   当通过 `MDC.get("X-Ato-P-Span-Id")`时返回当前上下文的 `ParentSpanId`
-   当通过 `MDC.get("X-Ato-Trace-Id")`时返回当前上下文的 `TraceId`

## Supported Context

### 1、Thread

​	针对Thread进行了增强，在创建线程时支持自动传递当前Trace: `java.lang.Thread`

### 2、Thread Pool

​	基于如下包作为基础的线程池均支持自动Trace跟踪:

-   `java.util.concurrent.ThreadPoolExecutor`
-   `java.util.concurrent.ForkJoinPool`
-   `java.util.concurrent.ScheduledThreadPoolExecutor`

### 3、Http Client

​	基于如下几个Client的HTTP请求客户端在发送请求时都会自动将当前上下文的TraceId设置到请求头：

-   OkHttp3：`com.squareup.okhttp3:okhttp`
-   JDK Http Client：`jdk:sun.net.www.http.HttpClient`
-   ApacheHttpClient：`org.apache.httpcomponents:httpclient`

### 4、Http Servlet

​	我们支持了HTTP Filter和HTTP Servlet来从请求头当中接收TraceId并设置到当前上下文：

-   `javax.servlet.Filter`
-   `javax.servlet.http.HttpServlet`

### 5、Middleware

​	支持阿里云ONS和RocketMQ在生产和消费时传递trace，支持Dubbo：

-   RocektMQ：`Producer` & `Consumer`
-   Aliyun ONS：`Producer` & `Consumer`
-   Dubbo：`org.apache.dubbo.rpc.protocol.dubbo.filter.TraceFilter`
                   `org.apache.dubbo.rpc.protocol.dubbo.filter.FutureFilter`

### 6、Scheduled

​	支持XXL Job、Spring Scheduled定时任务、PowerJob在产生时传递TraceId：

-   XxlJob Handler：`com.handler.com.xxl.job.core.IJobHandler`
- Spring Schedule Task：`org.springframework.scheduling.annotation.Scheduled`
- PowerJob Processor：`tech.powerjob.worker.core.processor.sdk.BasicProcessor`

### 7、Logging

​	支持几个主流日志框架的最终输出，自动加入trace信息(主要针对Stream和RollingFile的字符串和JSON格式)：

-   `JUL`、`Log4j`、`Logback`、`Log4j2`

## Contribute

欢迎贡献你的代码(`Fork` & `Pull Request`)，一起完善`autotrace4j`库！

-   如何增加支持：

    在`io.github.artlibs.autotrace4j.transformer`下仿照其他转换器新增

-   关于单元测试：

    在`io.github.artlibs.autotrace4j.At4jTest`中参考其他案例增加单元测试

本地单测：

```shell
$ export JAVA_HOME=/path/to/your/jdk21/home
$ make test
```

