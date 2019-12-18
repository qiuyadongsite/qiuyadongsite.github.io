---
layout: post
title:  JVM
date:   2019-12-18 20:52:12 +08:00
category: 签到系列
tags: 性能优化
comments: true
---

* content
{:toc}


签到4！






## jdk/jre/jvm

jdk:java development kit, java开发包

jre:java runtime environment,java运行环境

jdk是jre的超集，包含了所有的jre、及附加工具如编译跟调试相关的应用工具和应用程序，类库。

jre提供了库，java 虚拟机、JavaSE api、运行必要的组件。

jvm是java核心的工具，为了屏蔽系统不同，使应用程序可以一次编译到处可以执行。

jdk - jre - jvm的结构图https://docs.oracle.com/javase/8/docs/index.html；

jvm的介绍：https://docs.oracle.com/javase/specs/jvms/se8/html/index.html

## jvm8

开发java程序的时候，1，编写Java代码；2，通过javac java文件进行编译生成class 文件；通过java class文件进行运行；

那么从java到class文件属于编译阶段：

java文件->词法分析器（tokens流）->语法分析器（语法树）->语义分析器（抽象语法树）->字节码生成器->class文件

那么class文件怎么理解？https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html

class文件是一个16机制的文件

1，u4 magic,2个字节代表一个字符，4个字符代表模数，表示文件类型

2，u2 minor version,小版本号 u2 major version ,主版本号 代表jdk版本号

3，常量池、访问标志、类索引、父类索引、接口索引、字段表集合、方法集合、属性集合

此文件将会被jvm识别并且加载和运行；

### 类文件到虚拟机

- 装载过程 根据该类的全限定名获取二进制流，将静态的结构转换成方法区运行的数据结构，并且在堆中生成一个这个类的Class对象，作为对方法区访问该类的入口；

- 链接
  - 验证 为了保证类的正确性，对文件格式、元数据、字节码、符号引用进行验证
  - 准备 为类的静态变量初始化默认值
  - 解析 把类的符号引用改为直接引用

- 初始化 对类的静态变量，静态代码块执行，初始化操作

- 使用

- 卸载

### 类加载器ClassLoader

在第一阶段，进行类的装载，需要根据类的全限定名获取二进制流，它们都是通过类加载器进行加载的

1，bootstrap classLoader,使用c++编写的jre/lib/rt.jar,不是classsloader的子类

2，extension classloader jre/lib/.jar其他java平台扩展ext下的jar包

3，app Classloader,负责加载classpath下的jar

4,客户端的classloader,tomcat，jboss下的自行实现的Classloader

双亲委派机制：如果一个类加载器接受到加载任务的时候，首先询问父类是否加载过此类，如果加载过，就不会再加载了，保证了优先级层次性；

如果我想自己实现classloader,修改loadclass的内容，那就破坏了双亲委派机制

类装载的第二三阶段就将静态流转换成方法区运行的数据结构，在堆中生成Class对象，方便堆内对象访问该类的入口，类加载之后都去哪里了？

那么，jvm运行时数据区包括：方法区、堆、虚拟机栈、本地方法栈、程序计数器

- 方法区 类信息、常量、静态变量、及时编译器编译后的代码等，java8之前成为永久区，jdk8叫做元空间，都是方法区的实现 所有线程共享

- 堆 所有线程共享，java对象实例、数组都在堆上分配

- 虚拟机栈 线程执行的区域，并保存调用的状态，一个线程场景一个虚拟机栈，虚拟机栈是线程私有的独有的。栈里存放栈帧

- 程序计数器 cpu调用线程使用的，为了保存线程切换后能够恢复到正确的执行位置，每个线程都有一个程序计数器，线程私有的，java代码保存虚拟机字节码指令地址，如果是native方法，保存空

- 本地方法栈 当前线程执行的方法是native方法，这些方法都在本地方法栈中；

#### java虚拟机栈与栈帧

java虚拟机栈是每个线程私有的存放栈帧，栈帧可以理解为调用过程中每个方法

栈帧：局部变量表、操作数栈、动态连接、返回地址

局部变量表中存放方法的参数和方法内定义的变量，如果需要使用需要配合操作数栈

操作数栈：以压栈和出栈存放操作方法；

动态连接：指向常量池中引用该栈帧的引用，方便调用过程中的动态链家

返回地址：正确和异常返回地址

一个对象的内存布局是什么？

对象头+实例数据+填充

对象头中包含了Mark world+class point + 数组长度

mark world保存着锁状态、分代年龄、hash码等

class point保存真类元数据的内存地址

非堆+堆，堆=old+young,yong=eden+s0+s1

堆又包含创建的对象，数组

一般新创建的对象先存放的yong区，大对象直接去old区；

young区的eden区存不下数据就gc;yong区的gc叫minor GC,清空eden区，复制到s0或者s1,它们同一个时间只有一个区是空的。

直到to区满了，将to区数据复制到old区

当old区打到某个阈值，old区的gc叫major gc

-Xmx20M -Xms20M设置堆的大小是20M

-Xss128k stack spce 设置堆栈的大小

通过在jvisualvm中安装插件Visual GC观察的更形象

## 垃圾回收

### 谁是垃圾

引用计数法：只要有人引用该对象，就不是垃圾，遇到循环引用，那就没办法了

可达性分析：通过GCroot进行引用，在链路上的都不是垃圾

GCroot成员：本地变量表、static变量、常量、本地方法栈的变量、classloader、Thread等；

### 垃圾回收算法

标记清除算法：空间碎片问题

标记复制算法：空间利用率不高

标记整理算法：标记，移动到一边，剩下的清除

分代收集算法：对于yong区，由于存活的时间短，数据量少，通过  标记复制算法

对于old区：数据多，复制来复制去没意义，直接清除算法或者整理算法

垃圾收集算法有了，根据算法开发了垃圾收集器

## 垃圾搜集器

### Serial收集器

简单高效，拥有很高的单线程收集效率

缺点：收集时需要暂停其他线程

算法：复制算法

适用范围：yong

client模式下yong区默认的垃圾回收器

### ParNew收集器

可以认为是Serial收集器的多线程版本

在多cpu时，比Serial效率高；缺点是：暂停其他线程进行收集，单cpu不如Serial

复制算法   新生代   Server模式下yong默认的垃圾回收器

### Parallel Scavenge收集器

新生代收集器，复制算法，又是并行的多线程收集器，看上去跟ParNew类似，更关注吞吐量

垃圾回收所占用时间减少，就提高了系统的吞吐量

-XX:MaxGCPauseMillis 控制最大的垃圾收集停顿的时间

-XX:GCTimeRatio 直接设置吞吐量的大小

### Serial Old收集器

Serial的老年代版本，使用单线程，标记整理算法，运行过程跟Serial一样

### Parllel Old收集器

Parallel old是Parallel Scavenage老年版本，标记整理算法，吞吐量优先

### CMS收集器

使用标记清除算法的老年代收集器，关注的是最短回收停顿时间

1、初始标记 标记GCroot 能关联的对象，stw很短

2、并发标记 进行gc roots Tracing

3、重新标记  修改并发标记的变动内容

4、并发清除

优点是，并发收集、低停顿

缺点：产生大量空间碎片，并发阶段降低吞吐量

### G1收集器

G1:并行、并发、分代收集、空间整合、可预测的停顿

G1跟以上的区别是：将java堆分为大小相等的region,虽然有yong和old但是不需要连续，属于region的一部分的集合

1、初始标记

2、并发标记

3、最终标记 需要暂停用户线程

4、筛选回收 对每个Region的回收价值和成本进行排序，根据用户所期望的GC停顿时间制定回收计划

### 总结

- 收集器分类
  - 串行收集器 Serial和serial old算法，只有一个收集线程，需要stw,适合内存小的设备
  - 并行收集器（关注吞吐量） Parallel Scavenage和Parallel Old 多线程收集并行工作，但是用户线程仍然处于等待；
  - 并发收集器（关注停顿时间） CMS和G1,不一定是并行执行，可能是交替执行，垃圾回收时不需要停顿用户线程，对时间有要求的场景web

- 吞吐量和停顿时间
  - 停顿时间-垃圾收集器进行垃圾回收终端的响应时间 良好的响应提高用户体验
  - 吞吐量-运行用户代码时间/(运行用户代码+垃圾收集时间)高效的利用cpu 适合于后台运算不需要太多交互的任务

  这两个指标垃圾回收算法好坏的标准，也是调优的指标

#### 如何选择哪个垃圾回收器呢？

1、设置堆的大小让服务器自己来选择

2、如果内存小于100M,使用串行收集器

3、对停顿时间没有要求，让jvm自己选

4、如果停顿时间可以大于1秒，选并行或者jvm自己选

5、如果停顿时间小于1秒，选择并发收集器

### G1收集器

jdk7开始使用，jdk8成熟使用，jdk 9默认使用，适用于新老生代

多会使用它？

1、50%以上的对存活对象占用

2、对象分配和晋升变化很快

3、垃圾回收时间比较长

## 如何开启需要的垃圾回收器

1、串行
-XX:+UseSerialGC

-XX:+UseSerialOldGC

2、并行（吞吐量）

-XX:+UseParallelGC

-XX:+UseParalelOldGC

3、并发（关注停顿时间）

-XX:+UseConcMarkSweepGC

-XX:+UseG1GC

## JVM实战
