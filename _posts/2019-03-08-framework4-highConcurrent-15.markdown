---
layout: post
title:  分布式服务治理Dubbo之服务发布与消费源码分析
date:   2019-03-08 21:52:12 +08:00
category: 高并发分布式
tags: dubbo
comments: true
---

* content
{:toc}

  Dubbo使用api与sping两种方式发布服务,以及服务消费。












## dubbo使用Sping发布服务

  继承Sping的方式：

  1 写一个xml约束文件xsd文件：dubbo使用了dubbo.xsd文件

  2 在resources/META-INF/文件夹下写sping.schemas文件

  ```
  http\://code.alibabatech.com/schema/dubbo/dubbo.xsd=META-INF/dubbo.xsd

  ```

  3 在resources/META-INF/文件夹下写sping.handlers文件

  ```
  http\://code.alibabatech.com/schema/dubbo=com.alibaba.dubbo.config.spring.schema.DubboNamespaceHandler
  ```

  4 当sping加载xml时就会按照定义的DubboNamespaceHandler加载文件

  ```xml

  ?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans        
       http://www.springframework.org/schema/beans/spring-beans.xsd        
       http://code.alibabatech.com/schema/dubbo        
       http://code.alibabatech.com/schema/dubbo/dubbo.xsd">

</beans>

  ```

将每个标签解析成相应的类：

  ```java

  public class DubboNamespaceHandler extends NamespaceHandlerSupport {

	static {
		Version.checkDuplicate(DubboNamespaceHandler.class);
	}

	public void init() {
	    registerBeanDefinitionParser("application", new DubboBeanDefinitionParser(ApplicationConfig.class, true));
        registerBeanDefinitionParser("module", new DubboBeanDefinitionParser(ModuleConfig.class, true));
        registerBeanDefinitionParser("registry", new DubboBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("monitor", new DubboBeanDefinitionParser(MonitorConfig.class, true));
        registerBeanDefinitionParser("provider", new DubboBeanDefinitionParser(ProviderConfig.class, true));
        registerBeanDefinitionParser("consumer", new DubboBeanDefinitionParser(ConsumerConfig.class, true));
        registerBeanDefinitionParser("protocol", new DubboBeanDefinitionParser(ProtocolConfig.class, true));
        registerBeanDefinitionParser("service", new DubboBeanDefinitionParser(ServiceBean.class, true));//这里实例化了服务发布者对象
        registerBeanDefinitionParser("reference", new DubboBeanDefinitionParser(ReferenceBean.class, false));//这里实例化消费者发布对象
        registerBeanDefinitionParser("annotation", new DubboBeanDefinitionParser(AnnotationBean.class, true));
    }

}

  ```

5 加载完xml文件后，猜想ServiceBean实现了ApplicationListener所以使用onApplicationEvent发布服务

```java
public interface ApplicationListener<E extends ApplicationEvent> extends EventListener {
    void onApplicationEvent(E var1);
}
```

6 接下来就着重分析ServiceBean的onApplicationEvent方法：

```java

    public void onApplicationEvent(ApplicationEvent event) {
        if (ContextRefreshedEvent.class.getName().equals(event.getClass().getName()) && this.isDelay() && !this.isExported() && !this.isUnexported()) {
            if (logger.isInfoEnabled()) {
                logger.info("The service ready on spring started. service: " + this.getInterface());
            }

            this.export();
        }

    }
```

7 ServiceConfig是ServiceBean的父类，调用父类的export()

经过重重检查之后调用发布逻辑

```java
private void doExportUrlsFor1Protocol(ProtocolConfig protocolConfig, List<URL> registryURLs) {

      if (!"none".toString().equalsIgnoreCase(scope)) {
          if (!"remote".toString().equalsIgnoreCase(scope)) {
              this.exportLocal(url);
          }

          if (!"local".toString().equalsIgnoreCase(scope)) {
              if (logger.isInfoEnabled()) {
                  logger.info("Export dubbo service " + this.interfaceClass.getName() + " to url " + url);
              }

              if (registryURLs != null && registryURLs.size() > 0 && url.getParameter("register", true)) {
                  Iterator i$ = registryURLs.iterator();

                  while(i$.hasNext()) {
                      URL registryURL = (URL)i$.next();
                      url = url.addParameterIfAbsent("dynamic", registryURL.getParameter("dynamic"));
                      URL monitorUrl = this.loadMonitor(registryURL);
                      if (monitorUrl != null) {
                          url = url.addParameterAndEncoded("monitor", monitorUrl.toFullString());
                      }

                      if (logger.isInfoEnabled()) {
                          logger.info("Register dubbo service " + this.interfaceClass.getName() + " url " + url + " to registry " + registryURL);
                      }

                      Invoker<?> invoker = proxyFactory.getInvoker(this.ref, this.interfaceClass, registryURL.addParameterAndEncoded("export", url.toFullString()));
                      Exporter<?> exporter = protocol.export(invoker);
                      this.exporters.add(exporter);
                  }
              } else {
                  Invoker<?> invoker = proxyFactory.getInvoker(this.ref, this.interfaceClass, url);
                  Exporter<?> exporter = protocol.export(invoker);
                  this.exporters.add(exporter);
              }
          }
      }

      this.urls.add(url);
  }


```
