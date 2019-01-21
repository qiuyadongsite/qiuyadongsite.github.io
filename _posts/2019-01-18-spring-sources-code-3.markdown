---
layout: post
title:  spring源码解析三
date:   2019-01-18 22:25:12 +08:00
category: 源码学习
tags: spring 源码
comments: true
---

* content
{:toc}

前一篇详细从源码介绍了sping的定位、加载、注册，这一篇将从具体使用介绍spring,基于 XML 的依赖注入、基于 Annotation 的依赖注入；




## 基于 XML 的依赖注入

- 依赖注入发生的时间
当 Spring IOC 容器完成了 Bean 定义资源的定位、载入和解析注册以后，IOC 容器中已经管理类 Bean定义的相关数据，但是此时 IOC 容器还没有对所管理的 Bean 进行依赖注入，依赖注入在以下两种情况发生：
  1. 用户第一次通过 getBean 方法向 IOC 容索要 Bean 时，IOC 容器触发依赖注入。

  2. 当用户在 Bean 定义资源中为<bean>元素配置了 lazy-init 属性，即让容器在解析注册 Bean 定义时进行预实例化，触发依赖注入。

  BeanFactory 接口定义了 Spring IOC 容器的基本功能规范，是 Spring IOC 容器所应遵守的最底层最基本的编程规范。BeanFactory 接口中定义了几个 getBean 方法，就是用户向 IOC 容器索取管理的Bean 的方法，我们通过分析其子类的具体实现，理解 Spring IOC 容器在用户索取 Bean 时如何完成依赖注入。
