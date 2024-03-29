## Auto Trace for Java
[![Run Tests](https://github.com/artlibs/autotrace4j/actions/workflows/test.yml/badge.svg)](https://github.com/artlibs/autotrace4j/actions/workflows/test.yml)  [![Release](https://img.shields.io/github/release/artlibs/autotrace4j.svg?style=flat-square)](https://github.com/artlibs/autotrace4j/releases)  [![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg?style=flat)](http://opensource.org/licenses/MIT)

​	`autotrace4j`是一个基于ByteBuddy编写的轻量级日志跟踪工具，其基本逻辑是在各个上下文当中通过代码增强关键节点来传递`trace id`，最后在日志输出时注入到输出结果当中，以实现日志的跟踪串联。

​	我们借鉴了SkyWalking的实现原理，使用ByteBuddy在各个上下文环节进行关键点增强来传递Trace ID。

#### 易使用

​	基于Agent的方式来使用该工具，对业务代码无侵入。

#### 轻量级

​	只依赖ByteBuddy，且增加的增强代码只是往Thread Local当中写入字符串或读出字符串，没有做额外事项，不会增加性能开销。

## 开始使用

​	`autotrace4j`的使用非常简单，只需从[release](https://github.com/artlibs/autotrace4j/releases)中下载最新的agent jar包，在启动脚本中以agent方式运行：

```shell
$ java -javaagent=/dir/to/autotrace4j.jar=com.your-domain.biz1.pkg1,com.your-domain.biz2.pkg2 -jar YourJar.jar  # 省略其他无关参数
```

## 我们增强了什么

### 1、





























