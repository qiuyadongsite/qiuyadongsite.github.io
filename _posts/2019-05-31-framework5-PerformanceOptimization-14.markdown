---
layout: post
title:  Tomcat的优化
date:   2019-05-31 21:52:12 +08:00
category: 算法
tags: 集合类
comments: true
---

* content
{:toc}























## Tomcat是什么

>>The Apache Tomcat® software is an open source implementation of the Java Servlet, JavaServer Pages, Java Expression Language and Java WebSocket technologies.

翻译：Apache Tomcat 是Java Servlet、JavaServer页面、Java表达式语言和Java WebSoSk技术的开源实现。

[各个版本Tomcat源码下载地址](https://archive.apache.org/dist/tomcat/)或者官网的[Tomcat8.5](https://tomcat.apache.org/download-80.cgi)(本节研究)

- 源码的编译到调试运行
  - 安装ant
  - 新建pom.xml

    在源目录下新建pom.xml

    ```xml

    <?xml version="1.0" encoding="UTF-8"?>
    <project xmlns="http://maven.apache.org/POM/4.0.0"
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">
        <modelVersion>4.0.0</modelVersion>
        <groupId>org.apache.tomcat</groupId>
        <artifactId>Tomcat8.0</artifactId>
        <name>Tomcat8.0</name>
        <version>8.0</version>
        <build>
            <finalName>Tomcat8.0</finalName>
            <sourceDirectory>java</sourceDirectory>
            <testSourceDirectory>test</testSourceDirectory>
            <resources>
                <resource>
                    <directory>java</directory>
                </resource>
            </resources>

            <testResources>
                <testResource>
                    <directory>test</directory>
                </testResource>
            </testResources>
            <plugins>
                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-compiler-plugin</artifactId>
                    <version>2.3</version>
                    <configuration>
                        <encoding>UTF-8</encoding>
                        <source>1.8</source>
                        <target>1.8</target>
                    </configuration>
                </plugin>
            </plugins>
        </build>
        <dependencies>
            <dependency>
                <groupId>junit</groupId>
                <artifactId>junit</artifactId>
                <version>4.12</version>
                <scope>test</scope>
            </dependency>
            <dependency>
                <groupId>org.easymock</groupId>
                <artifactId>easymock</artifactId>
                <version>3.4</version>
            </dependency>
            <dependency>
                <groupId>ant</groupId>
                <artifactId>ant</artifactId>
                <version>1.7.0</version>
            </dependency>
            <dependency>
                <groupId>wsdl4j</groupId>
                <artifactId>wsdl4j</artifactId>
                <version>1.6.2</version>
            </dependency>
            <dependency>
                <groupId>javax.xml</groupId>
                <artifactId>jaxrpc</artifactId>
                <version>1.1</version>
            </dependency>
            <dependency>
                <groupId>org.eclipse.jdt.core.compiler</groupId>
                <artifactId>ecj</artifactId>
                <version>4.5.1</version>
            </dependency>
        </dependencies>
    </project>

    ```

    - 在目录下ant
    - 导入ideal目录，引入pom文件
    - 新建Aplication
      在在Man class:中填入，org.apache.catalina.startup.Bootstrap
      在VM options:中填入，
```

-Dcatalina.home="K:\ideal_work\showmycode\tomcat8.0\apache-tomcat-8.5.41-src\output\build"
-server
-Xms512m
-Xmx512m

```
  - 运行
