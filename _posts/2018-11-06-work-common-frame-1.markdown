---
layout: post
title:  spring概述
date:   2018-11-06 13:52:12 +08:00
category: 架构师
tags: 常用源码框架 spring
comments: true
---

* content
{:toc}

对于java程序员来说，最常用的就是spring框架，首先对spring框架有简单认识。





## Spring体系结构

![架构图](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/common-frame-1.jpg)


### 1.核心容器

>* 由spring-beans、spring-core、spring-context和spring-expression（spEL Spring Expression Language）4个模块组成

1. spring-beans和spring-core模块是spring框架的核心模块，包含了控制反转和依赖注入。BeanFactory接口是spring框架中的核心接口，它是工厂模式的具体实现，BeanFactory使用控制反转对应用程序的配置和依赖性规范与实际应用程序代码进行了分离。但BeanFactory容器实例化后并不会自动实例化Bean，只有当Bean被使用时，BeanFactory才会对该Bean进行实例化与依赖关系的装配。

spring-bean定义的是规范

spring-context 工厂的实现，DI的实现

spring-core 最顶层的核心包

2. spring-context模块架构于孩心模块之上，他扩展/BeanFactory，为它添加了Bean生命周期控制、框架事件体系以及资源加载透明化等功能。此外该模块还提供了许多企业级支持，如邮件访问、远程访问、任务调度等，ApplicationContext是该模块的核心接口，它是BeanFactory的超类，与BeanFactory不同，ApplicationContext容器实例化后会自动对所有的单实例Bean进行实例化与依赖关系的装配，使之处于待用状态。

3. spring-expression模块是统一表达式语言（unifiedEL)的扩展模块，可以查询、管理运行中的对象，同时也方便的可以调用对象方法、操作数组、集合等。它的语法类似于传统EL，但提供了额外的功能，最出色的要数函数调用和简单字符串的模板函数。这种语言的特性是基于Spring产品的需求而设计，他可以非常方便地同SpringIOC进行交互。

### 2.AOP和设备支持

>* spring-aop、spring-aspects和spring-instrumentation 3个模块组成。

1. spring-aop是Spring的另一个核心模块，是Aop主要的实现模块。作为继oop后，对程序员影响最大的编程思想之一，Aop极大地开拓了人们对于编程的思路。在Spring中，他是以JVM的动态代理技术为基础，然后设计出了一系列的Aop横切实现，比如前罝通知、返回通知、异常通知等，同时，Pointcut接口来匹配切入点，可以使用现有的切入点来设计横切面，也可以扩展相关方法根据需求进行切入。

2. spring-aspects模块集成自AspectJ框架，主要是为Spring Aop提供多种实现方法。

3. spring-instrumentation模块是基于JAVASE中的 java.lang.instrument 进行设计的，应该算是Aop的一个支援模块，主要作用是在JVM启用时，生成一个代理类，程序员通过代理类在运行时修改类的字节，从而改变一个类的功能，实现Aop的功能。在分类里，我把他分在了Aop模块下，在Spring官方文档里对这个地方也有点含糊不清，这里是纯个人观点

### 3.数据访问及集成

>* 由spring—jdbc、spring-tx、spring—orm、spring-jmsf和spring-oxm 5个模块组成。

1. spring-jdbc模块是Spring提供的JDBC抽象框架的主要实现模块，用于简化SpringJDBC。主要是提供JDBC模板方式、关系数据库对象化方式、Simplejdbc方式、亊务管理来简化JDBC编程，主要实现类是JdbcTemplate、SimpleJdbcTemplate和NamedParameterJdbcTemplate

2. spring-tx模块是Springjdbc事务控制实现模块。使用Spring框架，它对事务做了很好的封装，通过它的A0P配置，可以灵活的配置在任何一层；但是在很多的需求和应用，直接使用JDBC事务控制还是有其优势的。其实，亊务是以业务逻辑为基础的；一个完整的业务应该对应业务层里的一个方法；如果业务操作失败，则整个亊务回滚：所以，亊务控制是绝对应该放在业务层的：但是，持久层的设计则应该遵循一个很重要的原则：保证操作的原子性，即持久层里的每个方法都应该是不可以分割的。所以，在使用SpringJDBC事务控制时，应该注意其特殊性。

3. spring-orm模块是0RM框架支持模块，主要集成Hibernate，JavaPersistence API(JPA)和Java DataObjects(JDO)用于资源管理、数据访问对象(DAO)的实现和亊务策略。

4. spring-jms模块(Java Messaging Service)能够发送和接受信息，自SpringFramework4.1以后，他还提供了对spring-messaging模块的支撑。

5. spring-oxm模块主要提供一个抽象层以支撑PXM（Object-to-XML-Mapping的缩写，它是一个O/M-mapper，将java对象映射成XML数据，或者将XML数据映射成java对象）例如JAXB，CastorXMLBeans，JIBX和XStream

### 4.web

>* 由spring-web、spring-webmvc、spring-websocket和spring-webmvc-portlet4 个模块组成。

1. spring-web模块为Spring提供了最基础Web支持，主要建立于核心容器之上，通过Servlet或者Listeners来初始化IOC容器,也包含一些与Web相关的支持。

2. spring-webmvc模块众所周知是一个的Web-Servlet模块，实现了SpringMVC (model-view-controller)的Web应用。

3. spring-websocket 模块主要是与Web前端的全双工通讯的协议。（资料缺乏，这是个人理解）

4. spring-webmvc-portlet模块是知名的Web-Portlets模块(portlets在Web门户上管理和显示的可插拔的用户界面组产生可以聚合到门户页面中的标记语言代码的片段，如HTML，XML等），主要是为SpringMVC提丧Portlets组件支持。

### 5.报文发送

>* 即spring-messaging模块。

spring-messaging是Spring4新加入的一个模块，主要职责是为Spring框架集成一些基础的报文

传送应用。

### 6.Test

>* 即spring-test模块。

spring-test模块主要为测试提供支持的，毕竟在不需要发布（程序）到你的应用服务器或者连接

到其他企业设施的情况下能够执行一些集成测试或者其他测试对于任何企业都是非常重要的。

### 7.依赖结构图

![架构图](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/common-frame-2.jpg)
由图可得，IOC的实现包spring-beans和AOP的实现包spring-aop是整个框架的基础，而spring-core则是整个框架的核心，基础功能都在这三个包里。

在此基础之上，spring-context提供上下文环境，为各个模块提供粘合作用。

在spring-context基础之上提供了spring-tx和spring-orm包，web部分的功能是依赖spring-web实现的

spring-struts模块在spring4中被移除

8.spring源码下载

a）https://github.com/spring-projects/spring-framework

b）下载gradle http://services.gradle.org/distributions/

c）解压，配置GRADLE_HOME和PATH

d）验证gradle -v

e）构建eclipse项目 gradle eclipse -x:eclipse

f）引入到eclipse环境中
