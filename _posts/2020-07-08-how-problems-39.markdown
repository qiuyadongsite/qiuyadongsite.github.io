---
layout: post
title:  springboot开始篇
date:   2020-07-08 21:53:12 +08:00
category: 技术官网
tags: springboot
comments: true
---

* content
{:toc}



纵被春风吹作雪，绝胜南陌碾成尘

简介、系统要求、初步使用springboot；



## 简介

- 概念

容易的创建独立、生产级别的基于spring的可运行应用；

创建了固有的以spring为平台的第三方库的模板，为了用户更方便的，最少的,避免去争执的开发可用项目；

- 目标

提供快速、易于入门的体验；

提供默认配置，也可以通过选项很快的从默认情况分离；

提供非业务的功能特性（内置服务器、安全、性能、心跳检查、额外配置）

没有代码生成和xml配置；

## 系统要求

- 项目构建需求：

（2.3.1）java8、spring5.2.7、Maven/Gradle

- servlet容器

tomcat9.0、jetty9.4、undertow2.0

## 构建项目

- maven方式

- gradle方式

- springbootcli方式

## 开发第一个项目

- 创建pom

```xml
   <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.3.1.RELEASE</version>
    </parent>

    <dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
</dependencies>

```

- 最简单代码

```java
@RestController
@EnableAutoConfiguration
public class Example {

    @RequestMapping("/")
    String home() {
        return "Hello World!";
    }

    public static void main(String[] args) {
        SpringApplication.run(Example.class, args);
    }

}

```

- 创建可执行jar

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
