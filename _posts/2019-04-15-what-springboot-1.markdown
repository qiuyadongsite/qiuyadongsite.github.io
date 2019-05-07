---
layout: post
title:  springboot-SpringApplication
date:   2019-04-16 21:52:12 +08:00
category: 微服务架构
tags: Springboot
comments: true
---

* content
{:toc}

springboot启动类就是SpringApplication。













## SpringApplication

### 简单实例

为了使用springboot，[springboot快速构建](https://start.spring.io/)，下载代码；

拷贝相关的依赖

创建启动类

```java

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);
	}

}

```

这里有两个关键`@SpringBootApplication`注解类、SpringApplication启动类；

`@SpringBootApplication`

```java

@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(excludeFilters = {
		@Filter(type = FilterType.CUSTOM, classes = TypeExcludeFilter.class),
		@Filter(type = FilterType.CUSTOM, classes = AutoConfigurationExcludeFilter.class) })
public @interface SpringBootApplication {
    ...
}

```

`@ComponentScan` :Spring Framework 3.1引入

`@EnableAutoConfiguration`:
  激活自动装配 @Enable -> @Enable 开头的

- @EnableWebMvc
- @EnableTransactionManagement
- @EnableAspectJAutoProxy
- @EnableAsync

@SpringBootConfiguration : 等价于 @Configuration -> Configuration Class 注解

@Component -> @ComponentScan

处理类 -> ConfigurationClassParser


扫描类 ->

- ClassPathBeanDefinitionScanner
  - ClassPathScanningCandidateComponentProvider

  ```java

  protected void registerDefaultFilters() {
		this.includeFilters.add(new AnnotationTypeFilter(Component.class));
    	...
	}

  ```

## spring的注解编程模型

- `@Component`

  - @Service

  ```java

  @Component
  public @interface Service {
      ...
  }
  ```

  - @Repository

    ```java
        @Component
        public @interface Repository {
            ...
        }
    ```
  - @Controller

    ```java
        @Component
        public @interface Controller {
            ...
        }
  ```      
  - @Configuration

    ```java
        @Component
        public @interface Configuration {
        	...
        }

    ```    

## Spring 模式注解：Stereotype Annotations

Spring 注解驱动示例

注解驱动上下文 AnnotationConfigApplicationContext ， Spring Framework 3.0 开始引入的

```java

@Configuration
public class SpringAnnotationDemo {

    public static void main(String[] args) {

        //   XML 配置文件驱动       ClassPathXmlApplicationContext
        // Annotation 驱动
        // 找 BeanDefinition
        // @Bean @Configuration
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        // 注册一个 Configuration Class = SpringAnnotationDemo
        context.scan("com.gupao.microservicesproject");
        // 上下文启动
        context.refresh();

        System.out.println(context.getBean(SpringAnnotationDemo.class));

    }
}

```

`@SpringBootApplication` 标注当前一些功能

- @SpringBootApplication

  - @SpringBootConfiguration

    - @Configuration

      - @Component

##  Spring Boot 应用的引导

基本写法

SpringApplication

```java

SpringApplication springApplication = new SpringApplication(MicroservicesProjectApplication.class);
        Map<String,Object> properties = new LinkedHashMap<>();
        properties.put("server.port",0);
        springApplication.setDefaultProperties(properties);
        springApplication.run(args);
```      


SpringApplicationBuilder

```java

new SpringApplicationBuilder(MicroservicesProjectApplication.class) // Fluent API
                // 单元测试是 PORT = RANDOM
                .properties("server.port=0")  // 随机向 OS 要可用端口
                .run(args);

```

Spring Boot 引导示例

```java

@SpringBootApplication
public class MicroservicesProjectApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MicroservicesProjectApplication.class);
        Map<String,Object> properties = new LinkedHashMap<>();
        properties.put("server.port",0);
        springApplication.setDefaultProperties(properties);
        ConfigurableApplicationContext context = springApplication.run(args);
        // 有异常？
        System.out.println(context.getBean(MicroservicesProjectApplication.class));
    }
}

```

调整示例为 非 Web 程序

```java

@SpringBootApplication
public class MicroservicesProjectApplication {

    public static void main(String[] args) {
        SpringApplication springApplication = new SpringApplication(MicroservicesProjectApplication.class);
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("server.port", 0);
        springApplication.setDefaultProperties(properties);
        // 设置为 非 web 应用
        springApplication.setWebApplicationType(WebApplicationType.NONE);
        ConfigurableApplicationContext context = springApplication.run(args);
        // 有异常？
        System.out.println(context.getBean(MicroservicesProjectApplication.class));
        // 输出当前 Spring Boot 应用的 ApplicationContext 的类名
        System.out.println("当前 Spring 应用上下文的类：" + context.getClass().getName());
    }
}


```

## Spring Boot 事件

Spring 事件

Spring 内部发送事件

- ContextRefreshedEvent

  - ApplicationContextEvent

    - ApplicationEvent

refresh() -> finishRefresh() ->  publishEvent(new ContextRefreshedEvent(this));

- ContextClosedEvent
  - ApplicationContextEvent
    - ApplicationEvent

close() -> doClose() -> publishEvent(new ContextClosedEvent(this));


自定义事件

PayloadApplicationEvent



Spring 事件 都是 ApplicationEvent 类型


发送 Spring 事件通过  ApplicationEventMulticaster#multicastEvent(ApplicationEvent, ResolvableType)


Spring 事件的类型 ApplicationEvent

Spring 事件监听器 ApplicationListener

Spring 事件广播器 ApplicationEventMulticaster

- 实现类：SimpleApplicationEventMulticaster



Spring 事件理解为消息

ApplicationEvent 相当于消息内容

ApplicationListener 相当于消息消费者、订阅者

ApplicationEventMulticaster 相当于消息生产者、发布者

Spring Boot 事件监听示例

```java

@EnableAutoConfiguration
public class SpringBootEventDemo {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringBootEventDemo.class)
                .listeners(event -> { // 增加监听器
                    System.err.println("监听到事件 ： " + event.getClass().getSimpleName());
                })
                .run(args)
                .close();
        ; // 运行
    }
}

```

1. ApplicationStartingEvent（1）
2. ApplicationEnvironmentPreparedEvent（2）
3. ApplicationPreparedEvent（3）
4. ContextRefreshedEvent
5. ServletWebServerInitializedEvent
6. ApplicationStartedEvent（4）
7. ApplicationReadyEvent（5）
8. ContextClosedEvent
9. ApplicationFailedEvent (特殊情况)（6）


Spring Boot 事件监听器

```java

org.springframework.context.ApplicationListener=\
org.springframework.boot.ClearCachesApplicationListener,\
org.springframework.boot.builder.ParentContextCloserApplicationListener,\
org.springframework.boot.context.FileEncodingApplicationListener,\
org.springframework.boot.context.config.AnsiOutputApplicationListener,\
org.springframework.boot.context.config.ConfigFileApplicationListener,\
org.springframework.boot.context.config.DelegatingApplicationListener,\
org.springframework.boot.context.logging.ClasspathLoggingApplicationListener,\
org.springframework.boot.context.logging.LoggingApplicationListener,\
org.springframework.boot.liquibase.LiquibaseServiceLocatorApplicationListener

```

ConfigFileApplicationListener 监听 ApplicationEnvironmentPreparedEvent 事件

从而加载 application.properties 或者 application.yml 文件



Spring Boot 很多组件依赖于 Spring Boot 事件监听器实现，本质是 Spring Framework 事件/监听机制





SpringApplication 利用

- Spring 应用上下文（ApplicationContext）生命周期控制 注解驱动 Bean
- Spring 事件/监听（ApplicationEventMulticaster）机制加载或者初始化组件





q1：webApplicationType分为三种都有什么实用地方



 q2：框架底层的事件是单线程么？业务实现是否可以使用事件去实现？如果使用事件实现会不会是不是会有性能问题？


```java

public class SimpleApplicationEventMulticaster extends AbstractApplicationEventMulticaster {

  @Nullable
  private Executor taskExecutor;
    ...
}


```
