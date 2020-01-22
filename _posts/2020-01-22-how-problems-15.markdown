---
layout: post
title:  springboot
date:   2020-01-22 20:53:12 +08:00
category: 签到系列
tags: spring
comments: true
---

* content
{:toc}


签到15！



## Overview

The latest copy is available at [docs.spring.io/spring-boot/docs/current/reference](https://docs.spring.io/spring-boot/docs/current/reference). 

## Getting Started

### Introducing Spring Boot	

> Spring Boot makes it easy to create stand-alone, production-grade Spring-based Applications that you can run.

- Provide a radically faster and widely accessible getting-started experience for all Spring development.
- Be opinionated out of the box but get out of the way quickly as requirements start to diverge from the defaults.
- Provide a range of non-functional features that are common to large classes of projects.
- Absolutely no code generation and no requirement for XML configuration.

### Developing Your First Spring Boot Application

- #### Creating an Executable Jar

  ```xml
  <build>
      <plugins>
          <plugin>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-maven-plugin</artifactId>
          </plugin>
      </plugins>
  </build>
  ```

## Using Spring Boot

### Build Systems

- maven

  - `spring-boot-starter-parent`

  >Maven users can inherit from the `spring-boot-starter-parent` project to obtain sensible defaults. 

  ```xml
  <parent>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-parent</artifactId>
      <version>2.2.4.RELEASE</version>
  </parent>
  ```

  - without the Parent POM

    ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <!-- Import dependency management from Spring Boot -->
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.2.4.RELEASE</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```

  - Using the Spring Boot Maven Plugin

    >can package the project as an executable jar 

    ```xml
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
    ```

- gradle

- ant

### Starters

>you get a one-stop shop for all the Spring and related technologies that you need without having to hunt through sample code and copy-paste loads of dependency descriptors. 
>
> What’s in a name:spring-boot-starter-*

-  Spring Boot application starters
  - spring-boot-starter
  - spring-boot-starter-activemq 
  - spring-boot-starter-data-jdbc 
  - spring-boot-starter-data-redis 
  - spring-boot-starter-web 
-   Spring Boot production starters
  - spring-boot-starter-actuator
- Spring Boot technical starters 
  - spring-boot-starter-jetty 
  - spring-boot-starter-log4j2 
  - spring-boot-starter-tomcat 

### Structuring Your Code

- Using the “default” Package

  > When a class does not include a package declaration, it is considered to be in the “default package”. The use of the “default package” is generally discouraged and should be avoided.

- Locating the Main Application Class

  > We generally recommend that you locate your main application class in a root package above other classes.  

- Configuration Classes

  >Many Spring configuration examples have been published on the Internet that use XML configuration. If possible, always try to use the equivalent Java-based configuration. Searching for `Enable*` annotations can be a good starting point. 

  - Importing Additional Configuration Classes

    > The `@Import` annotation can be used to import additional configuration classes. Alternatively, you can use `@ComponentScan` to automatically pick up all Spring components, including `@Configuration` classes. 

  - Importing XML Configuration

    >If you absolutely must use XML based configuration, we recommend that you still start with a `@Configuration` class. You can then use an `@ImportResource` annotation to load XML configuration files. 

  - Auto-configuration

    >You need to opt-in to auto-configuration by adding the `@EnableAutoConfiguration` or `@SpringBootApplication` annotations to one of your `@Configuration` classes. 

    - Gradually Replacing Auto-configuration

      >If you need to find out what auto-configuration is currently being applied, and why, start your application with the `--debug` switch. Doing so enables debug logs for a selection of core loggers and logs a conditions report to the console. 

    - Disabling Specific Auto-configuration Classes

      >If you find that specific auto-configuration classes that you do not want are being applied, you can use the exclude attribute of `@EnableAutoConfiguration` to disable them;

      ```java
      @Configuration(proxyBeanMethods = false)
      @EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
      public class MyConfiguration {
      }
      ```

      >If the class is not on the classpath, you can use the `excludeName` attribute of the annotation and specify the fully qualified name instead. Finally, you can also control the list of auto-configuration classes to exclude by using the `spring.autoconfigure.exclude` property. 

  - Spring Beans and Dependency Injection

    > You are free to use any of the standard Spring Framework techniques to define your beans and their injected dependencies.  If a bean has one constructor, you can omit the `@Autowired`, as shown in the following example:

    ```java
    @Service
    public class DatabaseAccountService implements AccountService {
    
        private final RiskAssessor riskAssessor;
    //Notice how using constructor injection lets the riskAssessor field be marked as final, indicating that it cannot be subsequently changed.
        public DatabaseAccountService(RiskAssessor riskAssessor) {
            this.riskAssessor = riskAssessor;
        }
    
        // ...
    
    }
    ```

- Using the @SpringBootApplication Annotation

  > - `@EnableAutoConfiguration`: enable [Spring Boot’s auto-configuration mechanism](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-auto-configuration)
  > - `@ComponentScan`: enable `@Component` scan on the package where the application is located (see [the best practices](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-structuring-your-code))
  > - `@Configuration`: allow to register extra beans in the context or import additional configuration classes

- Running Your Application

  - Running from an IDE

  - Running as a Packaged Application

    ```java
    $ java -jar target/myapplication-0.0.1-SNAPSHOT.jar
    $ java -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n \
           -jar target/myapplication-0.0.1-SNAPSHOT.jar
    ```

  - Using the Maven Plugin

    >$ mvn spring-boot:run
    >
    >$ export MAVEN_OPTS=-Xmx1024m

  - Using the Gradle Plugin

- Developer Tools

  >Developer tools are automatically disabled when running a fully packaged application.  
  >
  >consider excluding devtools or set the `-Dspring.devtools.restart.enabled=false` system property 
  >
  >If you want to use a [certain remote devtools feature](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#using-boot-devtools-remote), you need to disable the `excludeDevtools` build property to include it. 

  ```xml
  <dependencies>
      <dependency>
          <groupId>org.springframework.boot</groupId>
          <artifactId>spring-boot-devtools</artifactId>
          <optional>true</optional>
      </dependency>
  </dependencies>
  ```

   - Property Defaults

     >For example, Thymeleaf offers the `spring.thymeleaf.cache` property. Rather than needing to set these properties manually, the `spring-boot-devtools` module automatically applies sensible development-time configuration. 

     

  