---
layout: post
title:  spring1
date:   2020-02-24 20:53:12 +08:00
category: 签到系列
tags: spring
comments: true
---

* content
{:toc}

签到26！



# 学习总结

明月出天山，苍茫云海间。 

长风几万里，吹度玉门关。 

## 1、Spring策略

#### 四大策略

- 基于pojo的轻量级和最小侵入性编程

- 通过依赖注入和面向接口松耦合；
- 基于切面和惯性进行声明式编程；
- 通过切面和模板减少样板式代码；

#### 三种方法

- BOP:面向bean

- DI:依赖注入

  创建对象、管理对象之间的相互关系；

  BeanFactory对象模型：单例和原型；

- AOP:面向切面

  日志和事务管理、权限认证、自动缓存、统一错误处理、调试信息输出等；

  将影响多个类的行为封装到可重用的模块中。

## 2、Spring核心模块

#### 核心模块

- Spring-bean、Spring-core

  控制反转和依赖注入；其中BeanFactory是核心类，将对应用程序的配置和依赖关系、实际的应用程序代码进行分离；当bean在使用时，实例创建和依赖关系装配；

- Spring-context

  扩展了BeanFactory,增添了Bean生命周期，架构事件、资源加载透明化等，还增加了企业级支持，邮件、远程访问、任务调度等；区别于Beanfactory,容器创建时就会初始化和依赖关系装配；

  - Spring-context-support,ioc容器的扩展支持以及子IOC容器
  - Spring-context-indexer是Spring类管理组件和ClassPath扫描

- Spring-expression

  统一表达式语言扩展模块，可以查询管理运行中的对象；

#### AOP和设备支持

- spring-aop

  以jvm的动态代理技术为基础，设计出一系列的AOP横切实现，比如前置通知、返回通知、异常通知等，Pointcut接口匹配切入点，可以实现现有的切入点的设计横切面，也可以扩展相关方法；

- spring-aspects

  集成AspectJ框架，为SpringAop提供多种AOP实现方法

- spring-instruments

  作为aop的支援模块，作用在jvm启动时生成一个代理类，通过修改字节码，从而改变类的功能，实现AOP的功能；

#### 数据访问与集成

Spring-jdbc、Spring-tx、Spring-orm、Spring-jms和Spring-oxm等5个模块组成；

#### Web组件

Spring-web、spring-webmvc、spring-websocket和spring-webflux四个模块；

#### 通信报文

spring-messaging模块

#### 集成测试

spring-test

#### 集成兼容

spring-framework-born,解决不同模块依赖版本不同问题

