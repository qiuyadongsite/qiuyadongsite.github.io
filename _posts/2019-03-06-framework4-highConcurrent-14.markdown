---
layout: post
title:  分布式服务治理之dubbo源码分析
date:   2019-03-06 22:52:12 +08:00
category: 高并发分布式
tags: dubbo
comments: true
---

* content
{:toc}

  Dubbo的使用很简单，如果要掌握，需要详细分析源码。












## SPI机制

  SPI全称（service provider interface），是JDK内置的一种服务提供发现机制，目前市面上有很多框架都是用它来做服务的扩展发现，大家耳熟能详的如JDBC、日志框架都有用到；

  简单来说，它是一种动态替换发现的机制。举个简单的例子，如果我们定义了一个规范，需要第三方厂商去实现，那么对于我们应用方来说，只需要集成对应厂商的插件，既可以完成对应规范的实现机制。 形成一种插拔式的扩展手段。

  代码实例：

定义规范：

  ```java
  public interface DataBaseDriver {

    String connect(String hospt);
}

  ```

MysqlDriver去实现：

```java

public class MysqlDriver implements DataBaseDriver{

    @Override
    public String connect(String s) {
        return "begin build Mysql connection";
    }
}
//并且在resources/services/com.XX.spi.DataBaseDriver文件中添加

//com.gupaoedu.spi.MysqlDriver

```

测试demo:

```java
public class DataBaseConnection {

    public static void main(String[] args) {
        ServiceLoader<DataBaseDriver> serviceLoader=
                ServiceLoader.load(DataBaseDriver.class);

        for(DataBaseDriver driver:serviceLoader){
            System.out.println(driver.connect("localhost"));
        }
    }
}
//将实现者的项目添加到测试pom中
```

SPI的缺点

1	JDK标准的SPI会一次性加载实例化扩展点的所有实现，什么意思呢？就是如果你在META-INF/service下的文件里面加了N个实现类，那么JDK启动的时候都会一次性全部加载。那么如果有的扩展点实现初始化很耗时或者如果有些实现类并没有用到，那么会很浪费资源

2	如果扩展点加载失败，会导致调用方报错，而且这个错误很难定位到是这个原因

## Dubbo优化后的SPI实现

Dubbo的SPI机制规范

  大部分的思想都是和SPI是一样，只是下面两个地方有差异。

1	需要在resource目录下配置META-INF/dubbo或者META-INF/dubbo/internal或者META-INF/services，并基于SPI接口去创建一个文件

2	文件名称和接口名称保持一致，文件内容和SPI有差异，内容是KEY对应Value
