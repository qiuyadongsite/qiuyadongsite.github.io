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

代码:<https://github.com/qiuyadongsite/show-my-code/tree/master/show-me-ability/src/main/java/com/qyd/learn/show/pattern> https://github.com/qiuyadongsite/show-my-code/tree/master/show-me-ability/src/main/java/com/qyd/learn/show/pattern

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

### 装饰者模式

在不改变原有对象的基础上，将功能附加到对象上，提供了比继承更有弹性的替代方案（扩展原有对象的功能）。

场景：用于扩展一个类的功能或者给一个类添加附加职责、动态的给一个对象添加功能，这些功能也可以再动态的撤销。

| 对比 | 装饰者模式 | 适配器模式     |
| :------------- | :------------- | :------------- |
| 形式      | 一种特殊的适配器模式  | 没有层级关系，装饰者模式有层级关系  |
| 定义      | 装饰者和被装饰者都实现同一个接口，主要目的是为了扩展之后依旧保留OOP关系 |适配器和被适配者没有必然的联系，通常是采用继承或代理的形式进行包装 |
| 功能      | 满足is-a的关系           | 满足 has-a的关系  |
| 功能      | 注重覆盖和扩展           | 注重兼容和转换     |
| 设计      | 前置考虑                 | 后置考虑          |

优点：装饰者是继承的有力补充，比继承更加灵活，不用改变原有的对象的情况下动态的给对象扩展功能，即插即用、通过使用不同的装饰类以及装饰类的排列组合，可以实现不同的效果、装饰者满足开闭原则；

缺点：会出现更多的代码和类，增加程序的复杂度、动态装饰时会更复杂。

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/decorator0001.png)

### 观察着模式（发布订阅模式）

定义了对象之间的一对多依赖，让多个观察者对象同时监听一个主体对象，当主体对象发生变化时，它的所有依赖着都会收到通知并更新。

场景：用于在关联行为之间建立一套触发机制的场景。

优点：观察者和被观察者之间建立一个抽象的耦合、观察者模式支持广播通信。

缺点：观察者之间有过多的细节依赖、提高时间消耗及程序的复杂度、使用得到要避免循环调用。

代码目录图：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/observer090.png)

## 总结和对比

| 分类 | 设计模式     |
| :------------- | :------------- |
| 创建型  | 工厂方法模式（Factory Method）、抽象工厂模式（Abstract Factory）、建造者模式（Builder）、原型模式（Prototype）、单例模式(Singleton)      |
| 结构型  | 适配器模式(Adapter)、桥接模式（Bridge）、组合模式（Composite）、 装饰器模式（Decorator）、门面模式（Facade）、享元模式（Flyweight）、代理模式（Proxy） |
| 行为型  | 解释器模式（Interpreter）、模板方法模式（Template Method）、责任链模式（Chain of Responsibility）、命令模式（Command）、迭代器模式（Iterator）、调解者模式（Mediator）、备忘录模式（Memento）、观察者模式（Observer）、状态模式（State）、策略模式（Strategy）、 访问者模式（Visitor）  |

- 单例模式和工厂模式

工厂类一般就是设计为单例

- 策略模式和工厂模式

工厂模式包括工厂方法和抽象工厂属于创建模式、策略模式属于行为模式

工厂模式主要目的是封装好创建逻辑，策略模式接收工厂创建好的对象，从而实现不同的行为

创建：new 行为：invoke

- 策略模式和委派模式

策略模式是委派模式内部的一种实现形式，策略模式关注的是结果是否能相互替代。支付方式：alipay、wechatPay

委派模式更加关注分发和调度过程，有可能采用if...else...条件分支语句来分发，内部也可以使用策略模式

- 模板方法模式和工厂模式

工厂方法是模板方法的一种特殊实现

- 模板方法和策略模式

模板方法和策略模式都有封装算法

策略模式是使用不同的算法可以相互替换，切不影响客户端应用层的使用

模板方法是针对定义一个算法的流程，将一些有细微差异的部分交给子类实现，策略模式算法实现是封闭的

模板模式不能改变算法流程，策略模式可以改变算法流程且可以替换。策略模式通常用来代替if...else...等条件分支语句。

- 装饰者模式和静态代理模式

装饰者模式关注点在于对对象的动态扩展、添加方法，而代理更加关注控制对对象的访问。

代理模式通常会在代理类中创建被代理对象的实例，而装饰者模式通常把装饰者作为构造参数。

- 装饰者模式和适配器模式

装饰者模式和适配器模式都属于包装类模式（wrapper）

装饰者模式可以实现被装饰者与相同接口或者继承被装饰者作为它的子类，而适配器和被适配器可以实现不同的接口

- 适配器和静态代理模式

适配器可以结合静态代理来实现，保存被适配对象的引用，但不是唯一的实现方式

- 适配器模式和策略模式

在适配业务复杂的情况下，利用策略模式优化动态适配逻辑

学习总结：穷举和类比，看源码进行归类处理，接下来看spring将主要的类进行归类。
