---
layout: post
title:  springboot2
date:   2020-02-10 20:53:12 +08:00
category: 签到系列
tags: springboot
comments: true
---



[TOC]

签到17！



## 学习总结

固然看文档能了解更加细的东西，但是知识点那么多，不可能都看文档，那么还是回到最初的学习，以知识点为根据地，每天总结10个知识点，通过查文档加深记忆！



## 1、springboot是什么？

Spring Boot使创建**独立的**、**基于Spring的**、**生产级应用程序变得很容易**，您可以运行这些应用程序。 

```
Spring Boot makes it easy to create stand-alone, production-grade Spring-based Applications that you can run.
```

- 为所有Spring开发提供更快、更广泛的启动体验。 

  >Provide a radically faster and widely accessible getting-started experience for all Spring development. 

- 不要固执己见，但当需求开始偏离默认值时，要迅速离开。 

  > Be opinionated out of the box but get out of the way quickly as requirements start to diverge from the defaults. 

- 提供对大型项目类(如嵌入式服务器、安全性、度量标准、健康检查和外部化配置)通用的一系列非功能特性。 

  > Provide a range of non-functional features that are common to large classes of projects (such as embedded servers, security, metrics, health checks, and externalized configuration). 

- 完全不需要代码生成，也不需要XML配置。 

  > Absolutely no code generation and no requirement for XML configuration. 

## 2、Starters

启动器是**一组方便的依赖关系描述符**，您可以将其包含在应用程序中。 

```
Starters are a set of convenient dependency descriptors that you can include in your application.
```

- Spring Boot application starters 

  >spring-boot-starter 
  >
  >spring-boot-starter-activemq 
  >
  >spring-boot-starter-amqp 
  >
  >spring-boot-starter-web 
  >
  >...

- Spring Boot production starters 

  > spring-boot-starter-actuator 

- Spring Boot technical starters 

  >spring-boot-starter-jetty 
  >
  >spring-boot-starter-log4j2 
  >
  >spring-boot-starter-logging 
  >
  >spring-boot-starter-reactor-netty 
  >
  >spring-boot-starter-tomcat 
  >
  >spring-boot-starter-undertow 

- #### Creating Your Own Starter

  创建两个模块：

  - `autoconfigure`  
  - `starter`  

  命名：*-spring-boot-starter 

