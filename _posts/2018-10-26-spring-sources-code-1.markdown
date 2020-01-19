---
layout: post
title:  spring源码解析一
date:   2018-10-26 12:25:12 +08:00
category: 源码学习
tags: spring 源码
comments: true
---

* content
{:toc}

Java源码的学习一直就是普通程序员与好程序员的区别，so，开始学习源码，既然学习源码，就学习比较全面的spring框架源码学习。




## Spirng的源码版本命名规则和环境搭建

**版本命名规则**

- x.y.z
  x 表示主版本，当API的兼容性变化时，x自增；（意味着大版本更新）

  y 表示次版本，当增加功能时（不影响API的兼容性），y自增；（意味着增加方法，增加接口等）

  z 表示修改号，如bug修复；（意味着方法内部发生改动）

- x.y.z.[a-c] （先行版本不稳定，可能存在兼容问题）

- x.y.z.dev[正整数]，开发版本

注：版本号的排序规则为依次比较主版本号、次版本号和修订号的数值，如 1.0.0 < 1.0.1 < 1.1.1
< 2.0.0；对于先行版本号和开发版本号，有：1.0.0.a100 < 1.0.0，2.1.0.dev3 < 2.1.0；当存
在字母时，以 ASCII 的排序来比较，如 1.0.0.a1 < 1.0.0.b1。

一些修饰的词：
`Snapshot`: 版本代表不稳定、尚处于开发中的版本
`Alpha`: 内部版本
`Beta`: 测试版
`Demo`: 演示版
`Enhance`: 增强版
`Free`: 自由版
`Full Version`: 完整版，即正式版
`LTS`: 长期维护版本
`Release`: 发行版
`RC`: 即将作为正式版发布
`Standard`: 标准版
`Ultimate`: 旗舰版
`Upgrade`: 升级版

Spring 版本命名规则

`. Release` 版本则代表稳定的版本

`. GA `版本则代表广泛可用的稳定版(General Availability)

`. M` 版本则代表里程碑版(M 是 Milestone 的意思）具有一些全新的功能或是具有里程碑意义的版本。

`. RC` 版本即将作为正式版发布

**环境搭建**
第 一 步 ：https://github.com/spring-projects/spring-framework/archive/v5.0.2.RELEASE.zip

第二步：下载 gradle http://downloads.gradle.org/distributions/gradle-1.6-bin.zip

第三步：解压,配置 GRADLE_HOME 和 Path

第四步：验证 gradle -v，环境变量是否正确

第五步：点击 gradlew.bat 构建项目

- 这里如果有问题：下载太慢，在Gradle中配置镜像
在C:\Users\qiuyd\.gradle目录下创建init.gradle

```
allprojects{
    repositories {
        def ALIYUN_REPOSITORY_URL = 'http://maven.aliyun.com/nexus/content/groups/public'
        def ALIYUN_JCENTER_URL = 'http://maven.aliyun.com/nexus/content/repositories/jcenter'
        all { ArtifactRepository repo ->
            if(repo instanceof MavenArtifactRepository){
                def url = repo.url.toString()
                if (url.startsWith('https://repo1.maven.org/maven2')) {
                    project.logger.lifecycle "Repository ${repo.url} replaced by $ALIYUN_REPOSITORY_URL."
                    remove repo
                }
                if (url.startsWith('https://jcenter.bintray.com/')) {
                    project.logger.lifecycle "Repository ${repo.url} replaced by $ALIYUN_JCENTER_URL."
                    remove repo
                }
            }
        }
        maven {
                url ALIYUN_REPOSITORY_URL
            url ALIYUN_JCENTER_URL
        }
    }
}

```
- 修改Gradle缓存文件夹路径（Gradle下载的文件/jar包）

 增加一个环境变量GRADLE_USER_HOME，指定为想要存放的地方

 - 如果配置了gradle后，它还下载，在spring源码下修改

 K:\ideal-workspace\spring-framework-5.0.2.RELEASE\gradle\wrapper\gradle-wrapper.properties

 参数设为：distributionUrl=file:///D:/gradle/gradle-4.4.1-all.zip

添加aspectj的编译：

 https://blog.csdn.net/a704397849/article/details/102754505

## Spring的基本概念

简介：解决java开发中`简化开发`的目标;

- 主要采取了 4 个关键策略：

 1. 基于 POJO 的轻量级和最小侵入性编程；

 2. 通过依赖注入和面向接口松耦合；

 3. 基于切面和惯性进行声明式编程；

 4. 通过切面和模板减少样板式代码；

而他主要是通过：面向 Bean、依赖注入以及面向切面这三种方式来达成的;

- BOP 面向Bean编程

Spring 提供了 IOC （控制反转）容器通过配置文件或者注解的方式来管理对象之间的依赖关系。

DI：依赖注入

- BeanFactory 支持两个对象模型
  1. 单例：模型提供了具有特定名称的对象的共享实例，可以在查询时对其进行检索。Singleton 是
默认的也是最常用的对象模型。对于无状态服务对象很理想。

  2. 原型：模型确保每次检索都会创建单独的对象。在每个用户都需要自己的对象时，原型模型最适
合。

- AOP 面向切面编程

它允许程序员对横切关注点或横切典型的职责分界线的行为（例如日志和事务管理）进行模块化。AOP 的核心构造是方面（切面），它将那些影响多个类的行为封装到可重用的模块中。

在典型的面向对象开发方式中，可能要将日志记录语句放在所有方法和 Java 类中才能实现日志功能。在 AOP 方式中，可以反过来将日志服务模块化，并以声明的方式将它们应用到需要日志的组件上。当然，优势 就是 Java 类不需要知道日志服务的存在，也不需要考虑相关的代码。所以，用 Spring AOP 编写的应用程序代码是松散耦合的。

`AOP 编程的常用场景有：Authentication 权限认证、Logging 日志、Transctions Manager 事务、
Lazy Loading 懒加载、Context Process 上下文处理、Error Handler 错误跟踪（异常捕获机制）
、Cache 缓存`等。

## Sping架构与简介

架构图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/springframe.png)

- 图解：
  1. 核心模块：由 spring-beans、spring-core、spring-context 和 spring-expression（Spring Expression Language, SpEL） 4 个模块组成。

>spring-beans 和 spring-core 模块是 Spring 框架的核心模块，包含了控制反转（Inversion of
Control, IOC）和依赖注入（Dependency Injection, DI）。BeanFactory 接口是 Spring 框架中
的核心接口，它是工厂模式的具体实现。BeanFactory 使用控制反转对应用程序的配置和依赖性规范与
实际的应用程序代码进行了分离。但 BeanFactory 容器实例化后并不会自动实例化 Bean，只有当 Bean
被使用时 BeanFactory 容器才会对该 Bean 进行实例化与依赖关系的装配。

>spring-context 模块构架于核心模块之上，他扩展了 BeanFactory，为她添加了 Bean 生命周期
控制、框架事件体系以及资源加载透明化等功能。此外该模块还提供了许多企业级支持，如邮件访问、
远程访问、任务调度等，ApplicationContext 是该模块的核心接口，她是 BeanFactory 的超类，与
BeanFactory 不同，ApplicationContext 容器实例化后会自动对所有的单实例 Bean 进行实例化与
依赖关系的装配，使之处于待用状态。

>spring-expression 模块是统一表达式语言（EL）的扩展模块，可以查询、管理运行中的对象，
同时也方便的可以调用对象方法、操作数组、集合等。它的语法类似于传统 EL，但提供了额外的功能，
最出色的要数函数调用和简单字符串的模板函数。这种语言的特性是基于 Spring 产品的需求而设计，
他可以非常方便地同 Spring IOC 进行交互。

  2. AOP 和设备支持：由 spring-aop、spring-aspects 和 spring-instrument 3 个模块组成。
>spring-aop 是 Spring 的另一个核心模块，是 AOP 主要的实现模块。作为继 OOP 后，对程序员影
响最大的编程思想之一，AOP 极大地开拓了人们对于编程的思路。在 Spring 中，他是以 JVM 的动态代
理技术为基础，然后设计出了一系列的 AOP 横切实现，比如前置通知、返回通知、异常通知等，同时，
Pointcut 接口来匹配切入点，可以使用现有的切入点来设计横切面，也可以扩展相关方法根据需求进
行切入。

>spring-aspects 模块集成自 AspectJ 框架，主要是为 Spring AOP 提供多种 AOP 实现方法。

>spring-instrument 模块是基于 JAVA SE 中的"java.lang.instrument"进行设计的，应该算是
AOP 的一个支援模块，主要作用是在 JVM 启用时，生成一个代理类，程序员通过代理类在运行时修改类
的字节，从而改变一个类的功能，实现 AOP 的功能。在分类里，我把他分在了 AOP 模块下，在 Spring 官
方文档里对这个地方也有点含糊不清，这里是纯个人观点。

3. 数据访问及集成：由 spring-jdbc、spring-tx、spring-orm、spring-jms 和 spring-oxm 5 个
模块组成。

>spring-jdbc 模块是 Spring 提供的 JDBC 抽象框架的主要实现模块，用于简化 Spring JDBC。主
要是提供 JDBC 模板方式、关系数据库对象化方式、SimpleJdbc 方式、事务管理来简化 JDBC 编程，主
要实现类是 JdbcTemplate、SimpleJdbcTemplate 以及 NamedParameterJdbcTemplate。

>spring-tx 模块是 Spring JDBC 事务控制实现模块。使用 Spring 框架，它对事务做了很好的封装，
通过它的 AOP 配置，可以灵活的配置在任何一层；但是在很多的需求和应用，直接使用 JDBC 事务控制
还是有其优势的。其实，事务是以业务逻辑为基础的；一个完整的业务应该对应业务层里的一个方法；
如果业务操作失败，则整个事务回滚；所以，事务控制是绝对应该放在业务层的；但是，持久层的设计
则应该遵循一个很重要的原则：保证操作的原子性，即持久层里的每个方法都应该是不可以分割的。所
以，在使用 Spring JDBC 事务控制时，应该注意其特殊性。

>spring-orm 模块是 ORM 框架支持模块，主要集成 Hibernate, Java Persistence API (JPA) 和
Java Data Objects (JDO) 用于资源管理、数据访问对象(DAO)的实现和事务策略。
spring-jms 模块（Java Messaging Service）能够发送和接受信息，自 Spring Framework 4.1
以后，他还提供了对 spring-messaging 模块的支撑。

>spring-oxm 模块主要提供一个抽象层以支撑 OXM（OXM 是 Object-to-XML-Mapping 的缩写，它是
一个 O/M-mapper，将 java 对象映射成 XML 数据，或者将 XML 数据映射成 java 对象），例如：JAXB,
Castor, XMLBeans, JiBX 和 XStream 等。

4. Web：由 spring-web、spring-webmvc、spring-websocket 和 spring-webflux 4 个模块组
成。

>spring-web 模块为 Spring 提供了最基础 Web 支持，主要建立于核心容器之上，通过 Servlet 或
者 Listeners 来初始化 IOC 容器，也包含一些与 Web 相关的支持。
spring-webmvc 模 块 众 所 周 知 是 一 个 的 Web-Servlet 模 块 ， 实 现 了 Spring MVC
（model-view-Controller）的 Web 应用。

>spring-websocket 模块主要是与 Web 前端的全双工通讯的协议。（资料缺乏，这是个人理解）

>spring-webflux 是一个新的非堵塞函数式 Reactive Web 框架，可以用来建立异步的，非阻塞，
事件驱动的服务，并且扩展性非常好。

5. 报文发送：即 spring-messaging 模块。
>spring-messaging 是从 Spring4 开始新加入的一个模块，主要职责是为 Spring 框架集成一些基
础的报文传送应用。

6. Test：即 spring-test 模块。
>spring-test 模块主要为测试提供支持的，毕竟在不需要发布（程序）到你的应用服务器或者连接
到其他企业设施的情况下能够执行一些集成测试或者其他测试对于任何企业都是非常重要的。

各个模块的依赖关系：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/springdependency.png)
