---
layout: post
title:  springboot3
date:   2020-02-17 20:53:12 +08:00
category: 签到系列
tags: springboot
comments: true
---



[TOC]



签到21！



## 学习总结

世事短如春梦，人情薄似秋云。不须计较苦劳心，万事原来有命。

幸遇三杯酒好，况逢一朵花新。片时欢笑且相亲，明日阴晴未定。 

## 1、springboot约定优于配置

- maven的目录结构
  - 默认有resources文件夹存放配置文件
  - 默认打包方式是jar
- spring-boot-starter-web中默认包含springmvc相关的依赖内置tomcat,轻易构建web应用
- 默认提供application.properties/yml文件
- 默认通过spring.profiles.active属性来决定运行环境读取的配置文件
- EnableAutoConfiguration默认对于依赖的starter进行自动装配

## 2、简介@SpringbootApplication注解

@SpingbootApplication是个复合注解，由@Configuration、@EnableAutoConfiguration、@Componentscan组成；

- @Configuration注解

  Spring3开始，spring支持两种bean的配置方式，xml和javaConfig，其中任何一个标注了@Configuration的java类定义为javaConfig配置类，这个类中任何标注了@Bean的方法，它的返回值都会注册到spring的ioc容器，方法名默认成为这个bean的id;

- @Componentscan

  相当于xml配置中的<context:component-scan>。主要作用就是扫描指定路径下的标示了需要配置的类，自动装配到spring的ioc容器中。这些标示配置的形式：@Component、@Repository、@Service、@Controller这些注解类。

- @EnableAutoConfiguration

  主要作用帮助springboot应用把符合条件的@Configuration配置加载到当前的springboot创建的ioc容器中。

  - @Enable

    从spring3.1开始，Enable主要是javaCofig框架的更进一步的完善，避免配置大量的代码从而降低使用难度

    - @EnableWebMVC
    - @EnableScheduling

  - @Import

    每一个Enable开头的注解都带有一个@Import注解，它类似于<import resource/>形式的注解，Enable注解不仅仅可以将多个Cofiguration整合，还可以实现一些复杂的场景，比如可以根据上下文来激活不同类型的bean,@Import接口可以配置三种不同的class;

    - 普通的bean护着带有Configuration的bean

    - 实现ImportSelector接口进行动态注入（通过过滤进行筛选）

      - @Import(AutoConfigurationImportSelector.class)

      AutoConfigurationImportSelector#selectImports：帮助springboot应用把所有符合@Configuration配置都加载到当前springboot创建的ioc容器，在这里提供了SpringFactoriesLoader的支持，使用@Conditional进行条件过滤；SpringFactoriesLoader跟java的spi机制类似，区别是会将key加载到ioc，而不是所有的；

      >AutoConfigurationImportSelector会先扫描spring-autoconfiguration-metadata.properties文件，最后扫描spring.factories对应的类，会结合前面配置的元数据进行过滤。
      >
      >如何过滤？
      >
      >如果当前classpath环境中没有相关的依赖，意味着这些类没必要进行加载，这种条件加载，可以有效的减少@configuration类的数量，降低springboot的启动时间。

    - 实现ImportBeanDefinitionRegistrar接口进行动态注入（直接注册到上下文）

## 3、实现一个简单的自动装配

- 定义一个@Configuration配置类

  这里返回所有@Bean标示的bean;

- 在resources/META-INF下

  - 创建spring.factories文件

    ```
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=定义的@Configuration完全限定名
    ```

  - 创建spring-autoconfigure-metadata.properties文件

    ```
    定义的@Configuration完全限定名.ConditionalOnClass=在当前项目的classpath下存在某个类
    ```

- 将该包添加到springboot的maven中

## 4、什么是starter?

相当于模块，它将所需要的依赖整合起来并对模块内的bean根据条件进行自动配置。使用者只需要依赖相应功能的starter,无需过多配置和依赖，springboot会自动扫描和加载相应的模块。这些starter的使用，开发者不需要过多的关注框架的配置，只需要关注业务即可！

#### 开发自己的starter

- 命名*-spring-boot-starter

- 开发Configuration类

  - 该配置类可以使用Import导入其他的Configuration

  - 可以使用注解@ConditionalOnMissingClass，@ConditionalOnClass等添加过滤条件，可以使用@Primary标示默认的bean;

    > ```
    > @ConfigurationProperties注解，可以配置prefix=x.x.x属性值，意思是前缀是该值得key在配置文件中进行过滤。
    > x.x.x.info.something
    > 自动进行参数值得注入；
    > ```

  - 实现多数据源

    - 配置文件

    ```
    app.datasource.db1.url=jdbc:mysql://192.168.8.126:3306/db1
    app.datasource.db1.username=root
    app.datasource.db1.password=root
    app.datasource.db1.driver-class-name=com.mysql.jdbc.Driver
    
    app.datasource.db2.url=jdbc:mysql://192.168.8.126:3306/db2
    app.datasource.db2.username=root
    app.datasource.db2.password=root
    app.datasource.db2.driver-class-name=com.mysql.jdbc.Driver
    ```

    - 开发Configuration类

    ```
    @Configuration
    public class JdbcDataSourceConfig {
    
        /*@Primary*/
        @Bean
        @ConfigurationProperties(prefix = "app.datasource.db1")
        public DataSourceProperties db1DataSourceProperties(){
            return new DataSourceProperties();
        }
    
        @Bean
        @ConfigurationProperties(prefix = "app.datasource.db2")
        public DataSourceProperties db2DataSourceProperties(){
            return new DataSourceProperties();
        }
    
        /*@Primary*/
        @Bean
        public DataSource db1DataSource(){
            return db1DataSourceProperties().initializeDataSourceBuilder().build();
        }
    
        @Bean
        public DataSource db2DataSource(){
            return db2DataSourceProperties().initializeDataSourceBuilder().build();
        }
    
        @Bean(name="db1JdbcTemplate")
        public JdbcTemplate db1JdbcTemplate(){
            return new JdbcTemplate(db1DataSource());
        }
    
        @Bean(name="db2JdbcTemplate")
        public JdbcTemplate db2JdbcTemplate(){
            return new JdbcTemplate(db2DataSource());
        }
    }
    ```

## 5、日志

日志的作用

- 可以为系统提供错误以及日常的定位
- 对访问记录进行跟踪
- 在大型互联网中，基于日志收集以及分析用户的用户画像、比如兴趣爱好、点击行为等。

历史

- apache的log4j首先提出，想捐给sun没同意

- sun开发了jul,基本就是模仿log4j

- 由于jul和log4j的存在，存在使用混乱，apache开发了jcl框架,底层可以使用jul也可以使用log4j

- 原log4j的作者认为jcl不够优秀，开发了slf4j框架，并提供了具体实现logback

- 由于logback赶超了log4j体系，apche重写log4j,成立了log4j2

  所以，总结

- 日志框架：jcl、slf4j

- 日志系统：log4j,log4j2、logback，jul

最常用的是，slf4j搭配log4j2或者logback

## 6、介绍Actuator

springboot提供了Actuator监控springboot项目；Actuator提供了很多Endpoint，有些涉及到安全问题，默认不能打开；

- 开启所有的endpoint

  ```
  managenment.endpoints.web.exposure.include=*
  ```

- health

  健康检查，返回up,down。为了打印更详细信息

  ```
  management.endpoints.health.show-details=always
  ```

- loggers

  显示当前的日志配置信息，针对每个包对应的日志级别

- beans

  springboot中所有的bean

- Dump

  获取活动线程的快照

- Mappings

  返回全部url

- conditions

  显示当前所有的条件注解，提供一份自动配置生效的条件

- shutdown

  关闭应用程序命令

  ```
  management.endpoint.shutdown.denabled=true
  ```

- Env

  获取全部的环境信息

## 7、什么是JMX

java Management Exitensions,java管理扩展，提供了对应用程序和jvm的监控和管理的功能。

- 服务器中各种资源的使用情况，CPU、内存
- JVM内存使用情况
- JVM线程使用情况

Actuator中，就是基于JMX的技术来实现的。

## 8、Spring Boot有哪几种读取配置的方式？

**读取application文件：**

在application.yml或者properties文件中添加：

```
info.address=USA
info.company=Spring
info.degree=high
```

**一、@Value注解读取方式：** 

```java
@Component
public class InforConfig{
    @Value("${info.address}")
    private String address;
     @Value("${info.company}")
    private String company;
     @Value("${info.degree}")
    private String degree;
    
}
```

**二、@ConfigurationProperties注解读取方式：** 

```java
@Component
@ConfigurationProperties(prefix="info")
public class InforConfig{
    private String address;
    private String company;
    private String degree;
    
}
```



**读取指定文件：**

资源目录下建立config/db-config.properties:

```
db.username=root
db.password=123456
```

**一、@PropertySource+@Value注解读取方式：** 

```java
//注意：@PropertySource不支持yml文件读取
@Component
@PropertySource(value={"config/xx.properties"})
public class InforConfig{
    @Value("${info.address}")
    private String address;
     @Value("${info.company}")
    private String company;
     @Value("${info.degree}")
    private String degree;
    
}
```

**二、@PropertySource+@ConfigurationProperties注解读取方式：** 

```java
@Component
@PropertySource(value={"config/xx.properties"})
@ConfigurationProperties(prefix="info")
public class InforConfig{
    private String address;
    private String company;
    private String degree;
    
}
```

**三、Environment读取方式：**

以上所有加载出来的配置都可以通过Environment注入获取到：

```java
@Autowired
private Environment env;
String getProperty(String key);

```

## 9、Springboot配置加载顺序

在 Spring Boot 里面，可以使用以下几种方式来加载配置：

>1、properties文件；
>
>2、YAML文件；
>
>3、系统环境变量；
>
>4、命令行参数；
>
>等等……

**配置属性加载的顺序如下：** 数字小的优先级越高，即数字小的会覆盖数字大的参数值 

```
1、开发者工具 `Devtools` 全局配置参数；
 
2、单元测试上的 `@TestPropertySource` 注解指定的参数；
 
3、单元测试上的 `@SpringBootTest` 注解指定的参数；
 
4、命令行指定的参数，如 `java -jar springboot.jar --name="Java技术栈"`；
 
5、命令行中的 `SPRING_APPLICATION_JSONJSON` 指定参数, 如 `java -Dspring.application.json='{"name":"Java技术栈"}' -jar springboot.jar`
 
6、`ServletConfig` 初始化参数；
 
7、`ServletContext` 初始化参数；
 
8、JNDI参数（如 `java:comp/env/spring.application.json`）；
 
9、Java系统参数（来源：`System.getProperties()`）；
 
10、操作系统环境变量参数；
 
11、`RandomValuePropertySource` 随机数，仅匹配：`ramdom.*`；
 
12、JAR包外面的配置文件参数（`application-{profile}.properties（YAML）`）
 
13、JAR包里面的配置文件参数（`application-{profile}.properties（YAML）`）
 
14、JAR包外面的配置文件参数（`application.properties（YAML）`）
 
15、JAR包里面的配置文件参数（`application.properties（YAML）`）
 
16、`@Configuration`配置文件上 `@PropertySource` 注解加载的参数；
 
17、默认参数（通过 `SpringApplication.setDefaultProperties` 指定）；

```

## 10、Spring Boot 如何定义多套不同环境配置？

首先我们要了解一个名词：**Profile**

简单来说，Profile就是Spring Boot可以对不同环境或者指令来读取不同的配置文件。

假如有开发、测试、生产三个不同的环境，需要定义三个不同环境下的配置。

- 基于properties文件类型

> 你可以另外建立3个环境下的配置文件：
>
> applcation.properties
>
> application-dev.properties
>
> application-test.properties
>
> application-prod.properties
>
> 然后在applcation.properties文件中指定当前的环境spring.profiles.active=test，这时候读取的就是application-test.properties文件。

- **基于yml文件类型**

只需要一个applcation.yml文件就能搞定，推荐此方式。

```yml
spring:
	profiles:
		active:prod
		
---
spring:
	profiles:dev
server:
	port:8080
---
spring:
	profiles:test
server:
	port:8081
---
spring.profiles:prod
spring.profiles.include:
	- proddb
	- prodmq
server:
	port:8082
---
spring:
	profiles:proddb
db:
	name:mysql
```

此时读取的就是prod的配置，prod包含proddb,prodmq，此时可以读取proddb,prodmq下的配置。

也可以同时激活三个配置。

 ```
spring.profiles.active:prod,proddb,prodmq
 ```

- **基于Java代码**

在JAVA配置代码中也可以加不同Profile下定义不同的配置文件，@Profile注解只能组合使用@Configuration和@Component注解。

```java
@Configuration
@Profile("prod")
public class ProductCofiguration{
    
}
```

- 指定profile:

在main方法启动方式：

```
--spring.profiles.active=prod
```

插件启动：

```
spring-boot:run -Drun.profiles=prod
```

jar运行方式：

```
java -jar xx.jar --spring.profiles.active=prod
```

除了在配置文件和命令行中指定Profile，还可以在启动类中写死指定，通过SpringApplication.setAdditionalProfiles方法。