---
layout: post
title:  高并发分布式一
date:   2019-12-15 20:52:12 +08:00
category: 签到系列
tags: 高并发分布式
comments: true
---

* content
{:toc}


签到2！






## 从单体到分布式

单体--集群--soa--微服务

微服务架构-springcloud

- 网关 zuul

- 服务调用 Feign

- 服务通信和发现 restTemplate、Eureka/Consoul/zk

- 消息驱动 Stream

- 消息总线 Bus

- 负载 Ribbon

- 熔断、限流、降级 hystrix(Dashboard仪表盘)

- 配置中心 config

- 监控 sleuth

围绕springboot搭建springcloud有两个体现：netflix、alibaba

目前支持springboot2的springcloud版本：Finchley、Greenwich

微服务体系springcloud-alibaba

- Dubbo 用于实现高性能Java Rpc通信

- Nacos 服务注册发现、配置管理、服务管理

- Sentinel 流量控制、熔断、系统负载保护

- RocketMQ 分布式消息系统

- Seata 分布式事务解决方案

最后一个阶段：serviceMesh

## Springboot相关

无论是springcloud netflix还是springcloud alibaba，都需要springboot支持；

- 约定优于配置

1、Maven目录结构 默认在resources下存放配置文件、默认打包成jar包

2、spring-boot-start-web默认了springmvc的依赖和内置了tomcat容器

3、默认提供application.properties/yml文件

4、使用spring.profiles.active决定运行环境读取的配置文件

5、EnableAutoConfiguration默认依赖的start进行自动装载

### 如何实现的约定优于配置

启动类上面有个复合注解SpringBootApplication：包括Configuration、EnableAutoConfiguration、ComponentScan

- Configuration IOC容器配置类，通过注解的方式实现xml中配置的bean

- ComponentScan 标识指定路径下@Component/@Reposity/@Service/@Controller

- EnableAutoConfiguration 属于Enable注解，其中延伸的注解有@EnableWebMvc

```

引入了这样一个类DelegatingWebMvcConfiguration，通过@Bean注册了和<mvc:annotation-driven/>一样的组件，RequestMappingHandlerMapping、RequestMappingHandlerAdatper、HandlerExceptionResolver等等，只要有个Spring管理的bean继承WebMvcConfigurer或WebMvcConfigurerAdapter，重写方法即可自定义<mvc:annotation-driven/>.

其实@EnableWebMvc == @Import({DelegatingWebMvcConfiguration.class})

```

这个注解可以实现即插即用自动装配，在spring-autoconfigure-metadata设置条件，onCondition当有这个条件时，去装载spring.factories中的类，实现动态装配通过条件过滤可以有效的减少configuration类的数量。具体实现通过实现ImportSelector进行条件筛选注册bean,通过实现ImportBeanDefinitionRegistrar注册类。

### springboot中的日志

spring-boot-starter-logging引入

最早apache的log4j,他想把它给sun却被拒绝，sun自己开发了jul基本是模仿log4j。混乱的log管理使得apache我又来了，搞一个commons logging,定义了借口，无论底层使用的是log4j还是jul都能简单是使用。log4j的原作者觉得commons logging不够优秀又开发slf4j作用类似于Commons logging,logback就是slf4j的实现。apache升级了log4j,又搞出了log4j2。

so：

log框架：jcl、slf4j

log系统：log4j、log4j2、logback、jul

### Actuator

用于对springboot项目的监控

开启访问相关的URL：mangement.endpoints.web.exposure.inclue=*

spring应用的健康检查：management.endpoint.health.show-details=always

查看包日志级别：loggers

查看springboot中ioc容器的所有bean:beans

查看线程活跃的快照：dump

查看url路径：Mappings

查看当前所有条件注解，实现自动配置：conditions

外部调用关闭应用程序：mangement.endpoint.shutdown.enable=true

获取环境信息：env

打开actuator的自动装配的spring.factorys中有：DataSourcesHealthIndicator/DiskSpaceHealthIndicator/RedisHealthIndicator

Actuator除了rest方式发布的Endpoint,它还把端点以JMX MBean方式发布出来，java mangementExtensions,管理扩展点

jmx可以查看：服务器个各种资源cpu、内存，jvm内存,jvm线程，jvm使用情况

### ConfigurationProperties注解

将配置参数映射到类上使用，如@ConfigurationProperties(prefix = "app.datasource.db1")，可以将db1下配置的具体实现的bean上。实现数据源的动态改变。

@Primary选出默认的实现

@SpringBootApplication的exclude可以排除某些自动装配的配置


## 注册中心的设计

从单体架构发展到分布式架构，势必造成远程通讯，这里有spring实现的RestTemplateBuilder来创建RestTemplate来调用，它没有造轮子而是使用现成的jdk或者HttpClient;

常见的通信工具有：apache的HTT Client,或者Square的OkHttp,还有NetFlix的Feign。

### Http vs Rpc通信

rpc优势在于使用高效的网络模型如nio,以及给为特定场景创建的序列化和协议；

http在于成熟稳定、广泛支持、易于扩展，消息可读性好；

有了远程通讯以后，目标服务扩容，那么：

负载均衡、服务地址如何发现、服务器状态的改变客户端需要尽快感知；

必须使用一个中间件来处理这些事情，用于服务的注册和发现；

dubbo中使用zookeeper,netflix使用Eureka和Consul

### 关于zk

- 分布式一致性 所有分布式节点都要发送请求，只能有一个请求提交，其他节点都会感知到这个请求；

拜占庭将军问题

为了解决这个问题，提出了paxos协议，用于不可信的网络环境中，按照paxos协议可以达成对某个提议的一致性；

那google Chubby是干嘛的呢？在Google中有一个GFS文件系统，需要选择一个节点是master,并且让所有节点都同意，chubby就是让这些节点创建一个文件，把自己的url写在该文件中，其他节点读取，就知道了谁是master了。粗粒度的处理方式，雅虎根据这个思想创建了zk。并不是为了做注册中心开发的zk，而是解决分布式锁的一种设计开发的，用于分布式协调；

为了防止单点故障，zk搞成集群，集群中有主节点有从节点

主节点：事务请求的唯一调度和处理者

从节点：非事务性请求处理，转发事务、参与leader投票、参与事务请求proposal投票

leader如何保证和follower节点的数据一致性呢？并且要求强一致性!使用2pc;

使用2pc，发现follower需要投票，为了提高处理，减少投票，搞一个observer节点用于处理非事务性操作，不参与投票；

- zk的数据结构

持久节点、持久有序节点、临时节点（临时节点和客户会话绑定在一起，会话关闭，节点失效）、临时有序节点

Container 节点，当其子节点都被删掉，它也会被删除

persistent_wait_ttl 超过ttl未被修改，且其没有子节点就会被删除

persistent_sequential_wait_ttl 同理

stat 状态信息 获取一些信息

为了保证数据的原子性，zk添加了版本信息，有三种版本，版本控制是乐观锁的实现，不需要枷锁，保证数据的原子性；

- Watcher zk的分布式数据的发布订阅功能，运行客户端向服务器注册一个watcher监听，发生一次触发一次事件通知，客户端需要反复注册watcher。

- java调用zk(Curator)

重试策略、权限控制、使用curator-recipes事件监听

可以使用curator开发分布式锁（利用有序节点）：分布式可重入排它锁、分布式排它锁、分布式读写锁  leader选举；

- ZAB协议（zookeeper Atomic Broadcast）支持崩溃恢复协议，来实现分布式数据一致性协议

两种模式：崩溃恢复、原子广播

ZXID事务id,64位，高32位存储版本号，低32存储消息个数

协议部分单独搞了，太需要认真了！
