---
layout: post
title:  分布式服务治理之dubbo概念及使用
date:   2019-03-06 21:52:12 +08:00
category: 高并发分布式
tags: dubbo
comments: true
---

* content
{:toc}

  Dubbo是 阿里巴巴公司开源的一个高性能优秀的服务框架，使得应用可通过高性能的 RPC 实现服务的输出和输入功能，可以和 Spring框架无缝集成。

  Dubbo是一款高性能、轻量级的开源Java RPC框架，它提供了三大核心能力：`面向接口的远程方法调用`，`智能容错和负载均衡`，以及`服务自动注册和发现`。












## dubbo出现的背景

随着互联网的发展，网站应用的规模不断扩大，常规的垂直应用架构已无法应对，分布式服务架构以及流动计算架构势在必行，需一个治理系统确保架构有条不紊的演进。

目前市场一些基本的架构：

单一应用架构

当网站流量很小时，只需一个应用，将所有功能都部署在一起，以减少部署节点和成本。此时，用于简化增删改查工作量的 数据访问框架(ORM) 是关键。

垂直应用架构

当访问量逐渐增大，单一应用增加机器带来的加速度越来越小，将应用拆成互不相干的几个应用，以提升效率。此时，用于加速前端页面开发的 Web框架(MVC) 是关键。

分布式服务架构

当垂直应用越来越多，应用之间交互不可避免，将核心业务抽取出来，作为独立的服务，逐渐形成稳定的服务中心，使前端应用能更快速的响应多变的市场需求。

此时，用于提高业务复用及整合的 分布式服务框架(RPC) 是关键。

流动计算架构

当服务越来越多，容量的评估，小服务资源的浪费等问题逐渐显现，此时需增加一个调度中心基于访问压力实时管理集群容量，提高集群利用率。

此时，用于提高机器利用率的 资源调度和治理中心(SOA) 是关键。

在大规模服务化之前，一般只是简单的暴露和引用远程服务，通过配置url进行调用，用f5或者nginx等进行负载均衡；

## Dubbo架构

官网架构图

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/dubbo1.png)

节点角色说明：

Provider: 暴露服务的服务提供方。

Consumer: 调用远程服务的服务消费方。

Registry: 服务注册与发现的注册中心。

Monitor: 统计服务的调用次调和调用时间的监控中心。

Container: 服务运行容器。


调用关系说明：

0 服务容器负责启动，加载，运行服务提供者。

1 服务提供者在启动时，向注册中心注册自己提供的服务。

2 服务消费者在启动时，向注册中心订阅自己所需的服务。

3 注册中心返回服务提供者地址列表给消费者，如果有变更，注册中心将基于长连接推送变更数据给消费者。

4 服务消费者，从提供者地址列表中，基于软负载均衡算法，选一台提供者进行调用，如果调用失败，再选另一台调用。

5 服务消费者和提供者，在内存中累计调用次数和调用时间，定时每分钟发送一次统计数据到监控中心。

连通性：

注册中心负责服务地址的注册与查找，相当于目录服务，服务提供者和消费者只在启动时与注册中心交互，注册中心不转发请求，压力较小

监控中心负责统计各服务调用次数，调用时间等，统计先在内存汇总后每分钟一次发送到监控中心服务器，并以报表展示

服务提供者向注册中心注册其提供的服务，并汇报调用时间到监控中心，此时间不包含网络开销

服务消费者向注册中心获取服务提供者地址列表，并根据负载算法直接调用提供者，同时汇报调用时间到监控中心，此时间包含网络开销

注册中心，服务提供者，服务消费者三者之间均为长连接，监控中心除外

注册中心通过长连接感知服务提供者的存在，服务提供者宕机，注册中心将立即推送事件通知消费者

注册中心和监控中心全部宕机，不影响已运行的提供者和消费者，消费者在本地缓存了提供者列表

注册中心和监控中心都是可选的，服务消费者可以直连服务提供者

## 简单使用

服务提供者dubbo-Server

包含两个子项目

server-api(通用api)

server-Provider

- server-api(通用api)

在这个子项目中只包含通用接口

```java

public interface IDemoService {

    String protocolDemo(String msg);
}

```

- server-Provider

此项目中包含了所有的接口实现：

```java
public class DemoService implements IDemoService{
    @Override
    public String protocolDemo(String msg) {
        return "I'm Protocol Demo:"+msg;
    }
}

```

此外还包含容器启动类：

```java

public class Bootstrap {

    public static void main(String[] args) throws IOException {
        ClassPathXmlApplicationContext context=
                new ClassPathXmlApplicationContext
                        ("META-INF/spring/dubbo-server.xml");
        context.start();

        System.in.read(); //阻塞当前进程
    }
}


public class Main {
    public static void main(String[] args) throws IOException {

        //默认情况下会使用spring容器来启动服务,多容器启动
        //public static final String DEFAULT_SPRING_CONFIG = "classpath*:META-INF/spring/*.xml";
        //会自动加载该包下的xml启动
        com.alibaba.dubbo.container.Main.main(
                new String[]{"spring","log4j"});
    }
}
```

配置xml：dubbo-server.xml

```xml

<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans        http://www.springframework.org/schema/beans/spring-beans.xsd        http://code.alibabatech.com/schema/dubbo        http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
    <!--提供方信息-->
    <dubbo:application name="dubbo-server" owner="mic"/>

    <!--注册中心-->
    <dubbo:registry id="zk1" address="zookeeper://192.168.11.156:2181"/>

    <dubbo:registry id="zk2" address="zookeeper://192.168.11.157:2181"/>

    <dubbo:protocol port="20880" name="dubbo"/>

    <dubbo:protocol port="8080" name="hessian"/>

    <dubbo:service interface="com.xx.dubbo.IGpHello"
                   ref="gpHelloService" protocol="dubbo,hessian" registry="zk1"/>

    <dubbo:service interface="com.xx.dubbo.IDemoService"
                   ref="demoService" protocol="hessian"/>

    <bean id="gpHelloService" class="com.xx.dubbo.GpHelloImpl"/>

    <bean id="demoService" class="com.xx.dubbo.DemoService"/>
</beans>

```

服务消费者dubbo-client

添加上面的dubbo-api包

配置xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans        http://www.springframework.org/schema/beans/spring-beans.xsd        http://code.alibabatech.com/schema/dubbo        http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
    <!--提供方信息-->
    <dubbo:application name="dubbo-client" owner="xx"/>
    <!--注册中心-->
    <dubbo:registry address="zookeeper://xx:2181?register=false" check="false" file="d:/dubbo-server"/>
    <dubbo:reference id="gpHelloService"
                     interface="com.xx.dubbo.IGpHello"
                     protocol="dubbo"/>
</beans>

```

调用demo

```java

public class App
{
    public static void main( String[] args ) throws IOException, InterruptedException {
        ClassPathXmlApplicationContext context=new
                ClassPathXmlApplicationContext
                ("dubbo-client.xml");
       //得到IGpHello的远程代理对象
            IGpHello iGpHello = (IGpHello) context.getBean("gpHelloService");
            System.out.println(iGpHello.sayHello("xx"));
            System.in.read();
    }
}

```

## 重要注意事项

主机绑定

在发布一个Dubbo服务的时候，会生成一个dubbo://ip:port的协议地址；

集群容错

  什么是容错机制？ 容错机制指的是某种系统控制在一定范围内的一种允许或包容犯错情况的发生，举个简单例子，我们在电脑上运行一个程序，有时候会出现无响应的情况，然后系统会弹出一个提示框让我们选择，是立即结束还是继续等待，然后根据我们的选择执行对应的操作，这就是“容错”。

  在分布式架构下，网络、硬件、应用都可能发生故障，由于各个服务之间可能存在依赖关系，如果一条链路中的其中一个节点出现故障，将会导致雪崩效应。为了减少某一个节点故障的影响范围，所以我们才需要去构建容错服务，来优雅的处理这种中断的响应结果

  Dubbo提供了6种容错机制，分别如下

  1.failsafe 失败安全，可以认为是把错误吞掉（记录日志）

  2.failover(默认)   重试其他服务器； retries（2）

  3.failfast 快速失败， 失败以后立马报错

  4.failback  失败后自动恢复。

  5.forking  forks. 设置并行数

  6.broadcast  广播，任意一台报错，则执行的方法报错配置方式如下，通过cluster方式，配置指定的容错方案

  ```xml

  <dubbo:reference id="gpHelloService"
                     interface="com.xx.dubbo.IGpHello"
                     protocol="dubbo"
    cluster="failsafe"
    />

  ```

服务降级

降级的目的是为了保证核心服务可用。

降级可以有几个层面的分类： 自动降级和人工降级； 按照功能可以分为：读服务降级和写服务降级；

1 对一些非核心服务进行人工降级，在大促之前通过降级开关关闭哪些推荐内容、评价等对主流程没有影响的功能

2 障降级，比如调用的远程服务挂了，网络故障、或者RPC服务返回异常。 那么可以直接降级，降级的方案比如设置默认值、采用兜底数据（系统推荐的行为广告挂了，可以提前准备静态页面做返回）等等

3 限流降级，在秒杀这种流量比较集中并且流量特别大的情况下，因为突发访问量特别大可能会导致系统支撑不了。这个时候可以采用限流来限制访问量。当达到阀值时，后续的请求被降级，比如进入排队页面，比如跳转到错误页（活动太火爆，稍后重试等）

mock就是降级的处理方法

```xml

<dubbo:reference id="gpHelloService"
                   interface="com.xx.dubbo.IGpHello"
                   protocol="dubbo"
   mock="出错后执行的具体本地实现方法的类" timeout="1"
  />

```

配置优先级别

以timeout为例，显示了配置的查找顺序，其它retries, loadbalance等类似。

1.方法级优先，接口级次之，全局配置再次之。

2.如果级别一样，则消费方优先，提供方次之。其中，服务提供方配置，通过URL经由注册中心传递给消费方。

建议由服务提供方设置超时，因为一个方法需要执行多长时间，服务提供方更清楚，如果一个消费方同时引用多个服务，就不需要关心每个服务的超时设置。
