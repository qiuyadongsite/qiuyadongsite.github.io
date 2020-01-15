---
layout: post
title:  dubbo2
date:   2020-01-03 20:52:12 +08:00
category: 签到系列
tags: 分布式高并发
comments: true
---

* content
{:toc}


签到9！






## Dubbo

不仅解决了进程间的通信问题而且还解决了程序员不需要考虑的服务路由、容错、负载均衡、降级等问题

### 启动运行

#### spring方式

当dubbo无论是使用spring的classPathXMLApplicationContext还是AnnotationConfigApplicationContext加载xml还是注解；

或是直接调用Container的Main.main方法；

都是根据spring的那一套自定义的类进行启动的具体：

1.定位xml文件；2.解析xml；3.初始化和启动事件；

在resources中创建spring.schemas:url=xsd文件，spring.handlers中：url=class类

class类DubboNamespaceHandler extends NamespaceHandlerSupport 实现了init，将xsd解析的标签初始化注入到容器里；

registerBeanDefinitionParser("application", new DubboBeanDefinitionParser(ApplicationConfig.class, true));

分为两种，Config结尾的只是初始化实例，而已bean结束的要做其他的事情：如ServiceBean、ReferenceBean等

```java
ServiceBean<T> extends ServiceConfig<T> implements InitializingBean, DisposableBean,
        ApplicationContextAware, ApplicationListener<ContextRefreshedEvent>, BeanNameAware,
        ApplicationEventPublisherAware {
            
}
//初始化后操作
public interface InitializingBean {
    void afterPropertiesSet() throws Exception;
}

public interface DisposableBean {
    void destroy() throws Exception;
}
//获取spring容器
public interface ApplicationContextAware extends Aware {
    void setApplicationContext(ApplicationContext var1) throws BeansException;
}

//事件监听
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    void onApplicationEvent(E var1);
}

public interface BeanNameAware extends Aware {
    void setBeanName(String var1);
}
//事件发布
public interface ApplicationEventPublisherAware extends Aware {
    void setApplicationEventPublisher(ApplicationEventPublisher var1);
}
```

很容易就理解了afterPropertiesSet方法中进行服务的发布；

### dubbo使用

dubbo对rest的支持

dubbo-admin的使用

使用注解的方式使用

####  容错

failover:出现错误后，重试其他服务器，配合retries,来设置重试次数，重试会带来延时,通常用于读操作；

failfast:快速失败，只发送一次调用，通常用于新增记录，幂等操作，配置mock到达，失败之后的操作

failsafe:失败安全策略，失败忽略，将保护系统，通常用于审计日志

failback:通常用于消息通知策略，失败自动恢复操作，后台记录失败请求，定时重发，默认5秒后

forking:并行调用多的服务器，只要第一个成功就成功了，forks，设置并发数

broadcast:广播到所有的服务器，任何一台出错就出错了，通常更新所有的服务器更新缓存记录日志等



#### 降级

人工降级、自动降级；读降级、写降级

1.对于非核心业务，当系统负载加大后，通过设置开关，关闭这些业务，降低系统负载

2.故障降级，比如操作失败后，（网络问题、服务器问题），可以通过兜底数据给用户保护系统，

3.限流降级，当流量到了某个程度，给用户提示页面或者加入阻塞队列

如failfast配合mock进行服务降级

check=false,服务启动时看看依赖的服务是否可用，这里不进行检测，默认检查

#### 版本升级

通过版本号进行服务的版本控制

#### 配置中心

nacos、zookeeper都可以做配置中心，需要配置

```
dubbo.config-center.address=zookeeper://192.168.42.101:2181
dubbo.config-center.app-name=springboot-dubbo-provider
```

可以设置外部的覆盖本地的配置

```
dubbo.config-center.highest-priority=false
```

这样zookeeper中就多了一条:dubbo/config

#### 元数据

当配置的参数过多后，url就会越长，由于是网络通信，必然造成网络开销

引入元数据后，配置元数据

```
dubbo.metadata-report.address=zookeeper://192.168.42.101:2181
dubbo.registry.simplified=true
```

### SPI

java的服务提供者接口，java有原始实现；dubbo进行了改进，动态发现和替换机制；

协议、拦截、集群、路由、负载、序列化、容器等无论是自己扩展还是使用，都需要了解spi

冯唐易老、李广难封；不忘初心。





























