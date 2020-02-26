---
layout: post
title:  Mycat2
date:   2020-02-26 20:53:12 +08:00
category: 签到系列
tags: Mycat
comments: true
---

* content
{:toc}



签到23！



## 学习总结

二十四桥明月夜，玉人何处教吹箫？ 

##  1、多数据源的读写解决方案

在javaweb中一般的sql运行流程：

Dao---->Mapper(ORM)--->Jdbc--->代理----->数据库服务

- DAO层

  在选择数据源之前，根据一定的规则选择数据源进行操作；

  Spring中提供了AbstractRoutingDataSource:

  1. 首先在application.properties中配置多个数据源
  2. 创建一个注解，@TargetDataSource注解
  3. 创建一个DynamicDataSource基础AbstractRoutingDataSource,重写determineCurrentLoookupKey方法，来决定哪个数据源
  4. 创建一个DynamicDataSourceCofig配置类
  5. 创建DataSourceAspect切面类，对注解@TargetDataSource注解的类进行注解拦截设置数据源
  6. spring-boot中使用@Import({DynamicDataSourceConfig.class})自动装配
  7. 在实现类上加注解，@TargetDataSource(name=DataSourceNames.SECOND)

  优势是不需要依赖ORM框架。实现简单，可以灵活定制；

  不能复用，不能夸语言；

- ORM框架层

  
