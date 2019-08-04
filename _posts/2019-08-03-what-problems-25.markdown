---
layout: post
title:  设计模式
date:   2019-08-03 20:52:12 +08:00
category: 归零
tags: 代码优化
comments: true
---

* content
{:toc}


设计模式!











## 概览

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/design001.png)

## 七大设计原则

单一职责原则：一个类只干一类事情，一个接口只做一件事

里式替换原则：子类可以扩展父类的功能不能改变原来的功能。任何基类出现的地方，子类一定可以出现。

接口隔离原则：实现类不去实现与自己无关的接口。使用接口隔离比使用单个接口要好。

开闭原则：对修改关闭，对扩展开放。程序需要扩展的时候不去修改原有代码，实现热插拔方式。易于维护和升级。需要使用接口和抽象类。

依赖倒置原则：依赖于抽象而不是具体实现，降低客户与实现的耦合。

迪米特原则：最少知道原则，一个实体类尽量少的与其他实体之间发生关系，模块相对独立。

合成复用原则：尽量使用类的聚合而不是继承达到代码复用效果。

## 常见设计模式

根据七大设计，将平时开发的经验总结了设计模式，每个设计模式一般也不是独立存在的。

![代码:](https://github.com/qiuyadongsite/show-my-code/tree/master/show-me-ability/src/main/java/com/qyd/learn/show/pattern)https://github.com/qiuyadongsite/show-my-code/tree/master/show-me-ability/src/main/java/com/qyd/learn/show/pattern

### 工厂设计模式

 简单工厂：又叫静态工厂模式，一个工厂类生产所有相关的产品，一旦产品发生改变就要修改工厂类，违反了开闭原则。

 工厂方法：创建一个工厂对象的接口，让实现这个接口的类来决定实例化那个类，让类的实例化推迟到子类中进行。用户只需要关心所需产品对应的工厂，无须关心创建细节。

 抽象工厂：提供一个创建一系列相关或者相互依赖对象的接口，无须指定它们的具体类。强调一系列相关产品对象一起使用创建对象需要大量的重复代码。


代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/factory001.png)

### 单例设计模式

在内存中只有一个实例，减少内存开销，可以避免对资源的多重占用，设置全局访问点，严格控制访问；

- 饿汉式单例
  在单例类首次加载时就创建实例，绝对线程安全，在线程还没出现以前就实例化了，不可能存在访问安全问题。
  缺点：是浪费内存空间
  优点：没有任何锁、执行效率比较高，用户体验比懒汉好

- 懒汉式单例
  被外部调用的时候才创建实例
  双校验方式：因为使用synchronized存在性能问题；
  静态内部类：完美的解决方案

- 注册式单例
  将每一个实例都缓存到统一的容器中，使用唯一标识获取实例（spring中的方案）

- TreadLocal单例
  保证线程内部的全局唯一，天生线程安全

  代码目录图：

  ![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/singleton003.png)

### 原型模式

原型实例指定创建对象的种类，并通过拷贝这些原型创建新的对象。不需要知道任何创建细节，不调用构造函数。

场景：类初始化消耗资源较多、new创建的一个对象非常繁琐、构造函数比较复杂、循环体中生产大量对象，可读性下降

优点：性能比new高，简化了创建过程

缺点：必须配备克隆方法或者复杂方法、改造成本高运用需要得当

在spring中的体现是scope= prototype,

- 浅克隆
  自定义一个克隆公共类，但是对引用对象依然指向的原来的对象。

- 深克隆：
  实现Cloneable、Serializable接口

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/prototype008.png)


### 代理模式

为其他对象提供一种代理，以控制对这个对象的访问。保护目标对象，增强目标对象

- 静态代理
显示的声明被代理的对象

- 动态代理
动态配置和替换被代理对象
原理：1、拿到被代理类的引用，并且获取它的所有接口，2、JDK Proxy类重新生成一个新的类，实现了被代理类所有接口的方法，3、动态生成java代码，把增强逻辑加入到新生成代码中。4、编译生成新的java代码的class文件。5、加载并重新运行新的class，得到类的全新类。显然无法代理final修饰的方法

JDK动态代理是实现了被代理对象的接口，CGLib是继承了被代理对象

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/proxy00111.png)

### 委派模式（Delegate）

负责任务的调度和分配任务，跟代理模式很像，可以看做一种特殊的静态代理的全权代理，但是代理模式注重过程，而委派模式更加注重结果。
SpringMVC 的 DispatcherServlet就是一个委派经典案例。

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/delegate0023.png)

### 策略模式

定义了算法家族、分别封装起来，让它们之间相互替换，此模式让算法的变化不会影响到使用算法的用户，避免使用多重if else和switch;

策略模式往往和委派模式结合使用

场景：支付方式、处理任务方式

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/strategy0001.png)

### 模板模式

指定义一个算法的骨架，兵允许子类为一个或者多个步骤提供实现。

场景：一次性实现一个算法的不变的部分，兵将可变的行为留给子类来实现、各个子类中公共的行为被抽取并集中到一个公共的父类中，从而避免代码的重复。

优点：提高代码的复用性、扩展性、符合开闭原则。

缺点：增加系统的复杂度。

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/template00000.png)

### 适配器模式

指将一个类的接口转换成客户期望的另外一个接口，使原本的接口不兼容类可以一起工作。

场景：已经存在的类，但是它的方法和需求不完全匹配、适配器模式是随着软件维护不同产品、不同厂家功能类似接口不相同情况下的解决方案。

优点：提高类的透明性和复用，现有类复用但不需要改变、目标类和适配器类解耦，提高程序的扩展性。

缺点：增加系统复杂度、源码的阅读难度、乱。

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/adapter40.png)
