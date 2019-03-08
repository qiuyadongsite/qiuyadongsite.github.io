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

5 加载完xml文件后，猜想ServiceBean实现了InitializingBean所以使用afterPropertiesSet发布服务

```java
public interface InitializingBean {

	/**
	 * Invoked by a BeanFactory after it has set all bean properties supplied
	 * (and satisfied BeanFactoryAware and ApplicationContextAware).
	 * <p>This method allows the bean instance to perform initialization only
	 * possible when all bean properties have been set and to throw an
	 * exception in the event of misconfiguration.
	 * @throws Exception in the event of misconfiguration (such
	 * as failure to set an essential property) or if initialization fails.
   在beanfactory设置了所有提供的bean属性之后调用

*（并满足BeanFactoryAware和ApplicationContextAware）。

*<p>此方法只允许bean实例执行初始化

*当所有bean属性都设置好并抛出

*配置错误时出现异常。

*@在配置错误时引发异常（例如

*如未能设置基本属性）或初始化失败。
	 */


	void afterPropertiesSet() throws Exception;

  // if (! isDelay()) {
  //          export();
  //      }

}

```  

接下来就着重分析ServiceConfig的export方法：

一些简单的校验检查后,获取注册中心：

```java
private void doExportUrls() {
        List<URL> registryURLs = loadRegistries(true);
        //是不是获得注册中心的配置
        // registry://172.19.42.25:2181/com.alibaba.dubbo.registry.RegistryService?application=dubbo-server&dubbo=2.5.3&owner=mic&pid=11640&registry=zookeeper&timestamp=1552032137819 id="com.alibaba.dubbo.config.RegistryConfig" />
        for (ProtocolConfig protocolConfig : protocols) { //是不是支持多协议发布
          //protocolConfig <dubbo:protocol name="dubbo" port="20880" id="dubbo" />
            doExportUrlsFor1Protocol(protocolConfig, registryURLs);
        }
    }

```

接下来

```java
             if (registryURLs != null && registryURLs.size() > 0
                        && url.getParameter("register", true)) {
                    for (URL registryURL : registryURLs) {
                        url = url.addParameterIfAbsent("dynamic", registryURL.getParameter("dynamic"));
                        URL monitorUrl = loadMonitor(registryURL);
                        if (monitorUrl != null) {
                            url = url.addParameterAndEncoded(Constants.MONITOR_KEY, monitorUrl.toFullString());
                        }
                        if (logger.isInfoEnabled()) {
                            logger.info("Register dubbo service " + interfaceClass.getName() + " url " + url + " to registry " + registryURL);
                        }
                        //通过proxyFactory来获取Invoker对象
                        Invoker<?> invoker = proxyFactory.getInvoker(ref, (Class) interfaceClass, registryURL.addParameterAndEncoded(Constants.EXPORT_KEY, url.toFullString()));
                        //发布服务
                        Exporter<?> exporter = protocol.export(invoker);
                        //将exporter添加到list中
                        exporters.add(exporter);
                    }
                }

```

先分析注册服务：Exporter<?> exporter = protocol.export(invoker)
