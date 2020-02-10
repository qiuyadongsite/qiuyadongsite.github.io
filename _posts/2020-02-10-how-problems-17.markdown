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

## 3、怎么理解自动装配？

Spring Boot自动配置尝试根据添加的jar依赖项自动配置Spring应用程序。 

>Spring Boot auto-configuration attempts to automatically configure your Spring application based on the jar dependencies that you have added.     
>
>You should only ever add one **@SpringBootApplication** or **@EnableAutoConfiguration** annotation.    

**@SpringBootApplication**：

- @EnableAutoConfiguration    

- @ComponentScan    

- @Configuration    

   

**@Import**

**@Conditional**



## 4、理解Production-Ready

- Springboot Actuator

使用场景：监视和管理投入生产的应用；

监控媒介：HTTP或者JMX端点；

端点类型：审计、健康、指标收集；

基本特点：自动运行；

- Springboot Actuator Endpoints

beans:

conditions:

env:

health:

info:

- 外部化配置

Bean的@Value注入

Spring Environment读取

@ConfigurationProperties绑定到结构化对象

## 5、理解注解驱动编程

### 注解历史

2.5

依赖注入：@Autowired

依赖查找：@Qualifier

组件声明：@Component、@Service

Springmvc注解：@Controller、@RequestMapping、@ModelAttribute等

JSR-250注解：@Resource以及@PostConstruct、@PreDestroy（init-method、destroy-method）

3.x

配置类注解：@Configuration

AnnotationConfigApplicationContext:

@Bean

@DependsOn

@Lazy

@Primary

@Role

@Profile

springmvc方面的注解：@RequestHeader、@CookieValue、@RequestPart、@PathVariable、@RequestBody、@ResponseStatus

JAX-RS

配置属性：Environment接口、PropertySources

@PropertySource

缓存抽象Cache和CacheManager

异步@Async、@Scheduled、异步web请求处理deferredResule

检验：@Validated(JSR-303)

模块驱动：@EnableWebMvc(RequestMapping、RequestMappingHandlerAdapter、HandlerExceptionResolver)

4.x

@Conditional

java.time(jsr-310)

@Repeatable(jsr-337)

@PropertySource

@ComponentScans

@EventListener(ApplicationListener)

@AliasFor

@RequestMapping

@GetMapping

@RestController

@RestControllerAdvice

@CrossOrigin(CorsRegistry)

@Lookup

5.x

@Indexed

NonNull、Nullable(JSR-305)

### 注解分类

- 元注解(Meta-Annotations)

  - java元注解：@Inherited、@Documented、@Repeatable等
  - spring元注解：@Component

- Spring模式注解(Stereotype)

  如@Repository、@Controller、@Service都是@Component派生的，理解派生性

- Spring组合注解(Composed)

  如@TransactionalService、@SpringbootApplication

- Spring注解属性别名和覆盖(Attribute Aliases and Overrides)

## 6、Spring注解驱动设计模式



