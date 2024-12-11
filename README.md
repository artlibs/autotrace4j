## Auto Trace for Java
[![Run Tests](https://github.com/artlibs/autotrace4j/actions/workflows/testing.yml/badge.svg)](https://github.com/artlibs/autotrace4j/actions/workflows/testing.yml) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.artlibs/autotrace4j/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.artlibs/autotrace4j/)  [![Release](https://img.shields.io/github/release/artlibs/autotrace4j.svg?style=flat-square)](https://github.com/artlibs/autotrace4j/releases)  [![License: Apache 2.0](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

​	`autotrace4j`是一个基于ByteBuddy编写的轻量级日志跟踪工具，其基本逻辑是在各个上下文当中通过代码增强关键节点来传递`trace id`，最后在日志输出时注入到输出结果当中，以实现日志的跟踪串联。

​	我们借鉴了SkyWalking的实现原理，使用ByteBuddy在各个上下文环节进行关键点增强来传递Trace ID。

#### 易使用

​	基于Agent的方式来使用该工具，对业务代码无侵入。

#### 轻量级

​	只依赖ByteBuddy，且增加的增强代码只是往Thread Local当中写入字符串或读出字符串，没有做额外事项，不会增加性能开销。

## Startup

​	`autotrace4j`的使用非常简单，只需从[release](https://github.com/artlibs/autotrace4j/releases)中下载最新的agent jar包，在启动脚本中以agent方式运行：

```shell
$ java -javaagent=/dir/to/autotrace4j.jar=com.your-domain.biz1.pkg1,com.your-domain.biz2.pkg2 -jar YourJar.jar  # 省略其他无关参数
```

#### 关于`MDC`

可通过`slf4j`或者`log4j`的`MDC`获取当前上下文的Trace ID：

-   当通过 `MDC.get("X-Ato-Span-Id")`时返回当前上下文的 `SpanId`
-   当通过 `MDC.get("X-Ato-P-Span-Id")`时返回当前上下文的 `ParentSpanId`
-   当通过 `MDC.get("X-Ato-Trace-Id")`时返回当前上下文的 `TraceId`

## Supported Context

### 1、Thread

​	针对Thread进行了增强，在创建线程时支持自动Trace跟踪: `java.lang.Thread`

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

### 5、MessageQunue

​	目前支持阿里云ONS和RocketMQ在生产和消费时带上TraceId：

-   RocektMQ：`Producer` & `Consumer`
-   Aliyun ONS：`Producer` & `Consumer`
-   Kafka：comming soon....

### 6、Scheduled Task

​	支持XXL Job、Spring Scheduled定时任务、PowerJob在产生时生成TraceId：

-   XxlJob Handler：`com.handler.com.xxl.job.core.IJobHandler`
- Spring Schedule Task：`org.springframework.scheduling.annotation.Scheduled`
- PowerJob Processor：`tech.powerjob.worker.core.processor.sdk.BasicProcessor`

### 7、Logging

​	支持在logback中输出日志时注入trace id进行输出：

-   Log4j：`log4j:log4j`
-   logback：`ch.qos.logback:logback-core`
-   Java loggin：`java.util.logging`

### 8、Middleware

​	支持如下中间件的增强

-   Dubbo：`org.apache.dubbo.rpc.protocol.dubbo.filter.TraceFilter`
                   `org.apache.dubbo.rpc.protocol.dubbo.filter.FutureFilter`

## Contribute

欢迎贡献你的代码，一起完善`autotrace4j`库！
