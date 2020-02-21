---
layout: post
title:  jvm1
date:   2020-02-13 20:53:12 +08:00
category: 签到系列
tags: jvm
comments: true
---

* content
{:toc}



签到20！



## 学习总结

一重山，两重山。山远天高烟水寒，相思枫叶丹。

菊花开，菊花残。塞雁高飞人未还，一帘风月闲。



## 1、jvm的运行时数据区

- java

  >java是多用途的、面向对象的、高并发的语言。可以运行在提供了运行环境的各式各样的主机中。
  >
  >只要在该运行环境中编译一次，就可以随时运行。

- jvm

  >java虚拟机，就是java的运行环境，它像是真正的计算机，包含了一组指令集以及运行时操作内存空间；
  >
  >为了提高安全性，定义了特殊格式的class文件；
  >
  >任何语言只要遵循class文件的格式，都可以将java虚拟机作为它们的运行环境；

- 运行时数据区

  - 线程私有

    - PC寄存器

      非native方法：存储当前运行指令的地址；native方法：返回的是undefind；

      为了保存线程切换后能够恢复到正确的执行位置 ；

    - 虚拟机栈

      为每一个线程提供一个栈，存储栈帧，每个栈帧理解为调用的每一个方法；

      - 栈帧：局部变量表、操作数栈、方法返回地址、动态链接地址、指向运行时常量池的引用

    - 本地方法栈

      不同的编程语言的兼容，native方法

  - 线程共享

    - 堆

      java对象实例、数组都在堆上分配 ，垃圾回收主要在这里执行

    - 方法区

      类信息、常量、静态变量、及时编译器编译后的代码等，java8之前成为永久区，jdk8叫做元空间

## 2、垃圾回收简述

#### 回收区域

​	jvm运行时，线程私有的会随着线程运行完成而结束，一般不存在垃圾回收；线程共有的区域是方法区和堆，方法区存储的是类信息、常量、静态变量以及生成的代码信息一般也不会回收；堆是主要的垃圾回收区；

#### 堆的区域划分

​	这里垃圾回收根据传统的划分方法：

- yong
  - eden、s0、s1
- old

#### 垃圾回收

- 定位垃圾

  - 引用计数法

    只要被引用，就不是垃圾

  - 可达性分析

    GCroot引用链路之外的都是垃圾

    - GCroot

      局部变量表、静态变量、常量、classloader、Thread等；

- 垃圾回收算法

  - 标记清除算法 -引起空间碎片

  - 标记复制算法 -空间利用率不高

  - 标记整理算法 -标记，移动到一边

  - yong区

    由于对象存活时间短，数据量也少，使用标记复制算法，用空间换时间

  - old区

    数据量多，复制来复制区没有实际意义，使用标记清除算法或者标记整理算法

## 3、垃圾收集器

- yong区垃圾回收算法
  - Serial收集器

    作用于yong,简单高效,使用单线程收集，缺点是需要stop word

  - ParNew收集器

    Serial的多线程版本，在多cpu时比Serial效率高；

  - Parallel Scavenge算法

    与ParNew类似，更加关注吞吐量，怎么提高吞吐量呢？垃圾回收的时间短！

    -XX:MaxGCPauseMillis 控制最大的垃圾收集停顿的时间

    -XX:GCTimeRatio 直接设置吞吐量的大小

- old区垃圾回收算法

  - Serial Old算法

    Serial的老年版本，但是使用标记整理算法

  - Parallel Old收集算法

    Parallel Scavenage算法的老年版本，标记整理，吞吐量优先

  - CMS算法

    标记清除算法的老年代实现，为啥使用标记清除，为了更好的提高用户体验，关注最短的回收停顿时间

    - 初步标记，GCroot关联的对象
    - 并发标记，进行GCroot链路跟踪
    - 重新标记，修改以前标记的变动内容
    - 并发清除

    优点：并发收集，低停顿

    缺点：产生空间碎片，并发阶段吞吐量就会降低

- G1收集器

  关键：并行、并发、分代收集、空间整理、可预测停顿

  算法：标记清除和标记复制算法

  region：它将java堆分为大小相等的region,每一个region中都关联一个Remembered Set.RS的数据结构是hash表，存储活对象的指针。当region数据发生变化，就会扫描rs中的card table得到内存使用情况和活对象。如果一个对象大于region的四分之三成为巨大对象，有个heap regions 存这类大对象；

  实现过程：

  - 初步标记

    标记GCroot可达对象，需要stw

  - 并发标记

    与应用程序一起执行，并对对象进行标记

  - 最终标记

    对并发标记阶段对象改变进行修改，同样stw

  - 筛选回收

    对每个区域region回收成本和价值进行排序，根据停顿时间，选择收集某个区域对象并统计每个月区域的对象数量

- CMS与G1进行对比

  -  分代

    CMS中，堆是分永久代、新生代、老年代的；G1中堆被划分region，操作的对象是以整个区域为对象的；

  - 算法

    CMS使用标记清除算法，G1使用压缩算法（标记清除+标记复制算法）

  - 停顿时间可控

    G1建立在可预测停顿的模型；CMS不可控；

## 4、收集器如何选择

- 收集器分类

  - 串行收集器 Serial和serial old算法，只有一个收集线程，需要stw,适合内存小的设备
  - 并行收集器（关注吞吐量） Parallel Scavenage和Parallel Old 多线程收集并行工作，但是用户线程仍然处于等待；
  - 并发收集器（关注停顿时间） CMS和G1,不一定是并行执行，可能是交替执行，垃圾回收时不需要停顿用户线程，对时间有要求的场景web

- 吞吐量和停顿时间

  - 停顿时间

     垃圾收集器进行垃圾回收终端的响应时间 良好的响应提高用户体验

  - 吞吐量-

    运行用户代码时间/(运行用户代码+垃圾收集时间)高效的利用cpu 适合于后台运算不需要太多交互的任务

  这两个指标垃圾回收算法好坏的标准，也是调优的指标

- #### 如何选择哪个垃圾回收器呢？

  1、设置堆的大小让服务器自己来选择

  2、如果内存小于100M,使用串行收集器

  3、对停顿时间没有要求，让jvm自己选

  4、如果停顿时间可以大于1秒，选并行或者jvm自己选

  5、如果停顿时间小于1秒，选择并发收集器

- G1收集器

  G1的首要目的是为那些需要大容量内存和较小GC延迟的应用程序提供解决方案

  jdk7开始使用，jdk8成熟使用，jdk 9默认使用，适用于新老生代；多会使用它？

  1、50%以上的对存活对象占用

  2、对象分配和晋升变化很快

  3、垃圾回收时间不要太长

## 5、JVM参数操作

#### 标准的一些参数（稳定不变）

-version、 -help

#### 非标准参数（可变，不同版本可能微调）

- -X参数

  -Xint 解释执行 -Xcomp 第一次使用就编译成本地代码 -Xmixed 混合模式

- -XX设置参数

  - boolean类型的参数（-禁用+使用）：-XX:+UseConcMarkSweepGC使用cms垃圾回收器
  - 非boolean参数：-XX:MaxGCPauseMaillis=500

- 其他参数

  -Xms1000  =-XX:+InitalHeapSize=1000

  -Xmx1000=-XX:MaxHeapSize=1000

   -Xss100=-XX:+ThreadStackSize=100

#### 查看参数

-XX:+PrintFlagsFinal -version>flags.txt

 打印所有jvm参数以及值 查看具体参数使用jinfo

#### 设置参数

- 开发工具eclipse、ideal中设置
- 运行jar包时设置 java -XX:+UseG1GC xxx.jar
- web容器如tomcat会在脚本里设置
- 也可以通过jinfo实时调整java进程的参数 jinfo -flag [flag][pid]

## 6、jvm命令

#### jps(看进程号)

查看java进程信息

#### jinfo（查看与设置参数）

- 实时查看jvm的参数

  jinfo -flag name pid

  jinfo -flag UseG1GC 12265

- 修改参数（只能修改PrintFlagsFinal中type为Managetable的flag）

  jinfo -flag +/-Boolean pid

  jinfo -flag xx=ee pid

-  查看进程中设置过的参数

  jinfo -flags pid

#### jstat（查看虚拟机性能统计信息）

- 查看类加载信息

  jstat -class pid 1000 10 查看类每1000毫米加载10次打印出来加载内容

- 查看GC信息

  jstat -gc pid 1000 10 查看垃圾回收情况

#### jstack(查看堆栈信息)

​	jstack pid 查看线程的加载堆栈的状态信息 如查看死锁

#### jmap (生成堆栈为快照)

- 打印堆内存相关信息

  jmap -heap pid

- dump堆内存相关信息

  jmap -dump:format=b,file=heap.hprof pid

- 当内存溢出时，自动dump出文件

  -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=xx.hpof

## 7、常用工具

通过命令还是不够明显，需要图形化界面工具

- jconsole

  统计堆内存、线程、类、vm概要、Mbean

- jvisualvm

  可以监控Cpu、堆内存、类、线程、概要、还可以使用ViualGC插件形象查看垃圾回收过程也可以远程监控tomcat等

- arthas

  是阿里开源的java诊断工具，采用命令交互模式

  java -jar arthas-boot.jar -h 查看如何使用

- MAT（堆栈分析器）

  java堆分析器，用于查找内存泄漏

  - 首先dump出文件
    - 手动 jmap -dump:format=b,file=heap.hprof 11111
    - 自动 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapPath=xx.hprof

  在HistoGram中可以列出内存中的对象，个数及大小

  Leak Suspects 查看内存泄漏的主要原因

  Top customers 查看大对象

- GC日志分析工具

  - 先保存gc日志

    -XX:+PrintGCDEtails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -Xloggc:gc.log

  - 工具

    - 在线工具gceasy：https://gceasy.io
    - gcviewer-1.36-SNAPSHOT

  关注吞吐量、停顿时间、GC次数

## 8、调优

- 什么时候GC

  - 当eden区或者s区不够用
  - 老年代不够用了
  - 方法区不够用了
  - 调用了system.gc()

- 怎么打印出gc日志？

  -XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps -Xloggc:gc.log

- 垃圾回收器的选择关注的就三个点：

  CMS、G1关注停顿时间的

  ParallelGC、ParallelOldGC关注吞吐量的

  垃圾回收次数将会影响cpu是否频繁

- G1如何调优

  - 首先，观看吞吐量、最大最小停顿时间、平均停顿时间、gc次数

  - 调整堆的大小

    -XX:MateSpaceSize 设置元空间大小

    -Xms500M -Xmx500M

    并不是越大越好，越大那么gc一次的时间就会越大

  - 调整最大停顿时间
    -XX:MaxGCPauseMillis=20

    不断调优暂停时间，但是不要太严格，否则gc次数会大，pc频繁调用曾大了pc的负荷

  - 启动并发GC时堆空间占用百分比

    -XX:InitiatingHeapOccupancyPercent=45 超过这个值才GC

  - 使用-XX:ConcGCThreads来曾加标记线程的数量
