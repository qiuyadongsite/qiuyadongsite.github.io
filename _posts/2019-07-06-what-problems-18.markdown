---
layout: post
title:  JVM1
date:   2019-07-06 20:52:12 +08:00
category: 归零
tags: 性能优化
comments: true
---

* content
{:toc}


JVM！











## 常用的命令

java -version

：包含的含义

jps

jinfo -flags [ID]

jmap -heap [ID] 看内存情况,有dumplog

命令看文档路径：docs.oracle.com/javase/8/docs/  ,search

JDK JRE JVM 图

一流程序员看规范

为啥直接用Object

java的启动类

双亲委派与沙箱安全机制

- JVM的体系结构

JVM-操作系统-硬件系统

JVM体系结构图

运行时数据区：class-子类装载器系统-运行时数据区-执行引擎

- 类装载器ClassLoader

classLoader加载class文件(模板)，存储类的元信息，再进入jvm

反射有哪几种机制：三种，class.forname .class getClass()

根classloader(bootstrap calsss loader rt.jar)-extension class loader(ext/)-system classloader

test.getClass().getClassLoader.getParent().getParent();//null,根加载器，由于它是bootstrap classloader，c++写的，所以是null,在java中找不到
test.getClass().getClassLoader.getParent()；//$ExtClassloader@

test.getClass().getClassLoader；//$AppClassloader@

new Object().getClass().getClassLoader();//null ,由于它是bootstrap classloader，c++写的，所以是null,在java中找不到,Object在rt.jar中

验证双亲委派机制

写一个在Java.lang;包下的String,写main方法运行，提示找不到main,父加载器加载了，子加载器就不会再加载了。

为了安全，任何父亲都加载不到，自己才会加载
沙箱安全机制就是，jdk的代码不会被java的使用者篡改，因为任何一个类，用户都可以自定义，但是不能执行

- 执行引擎

负责解释命令，提交给操作系统执行

- Native方法

被native修饰的方法，本地方法，java解决不了，不擅长的领域。调用c、c++

融合不同语言为java所用

- 本地方法栈

弹匣。是一种数据结构。main方法中有两个方法

test()
add()
main()

add（int a, int b）{
  int result= a+b
}

这个就是栈，栈中存的其实就是栈帧，它是一种数据结构，栈帧：局部变量表+操作数栈+帧数据区

add()在外部理解是函数，在jvm中是栈帧：a,b,resule就是存储在局部变量表中，操作数栈就是将它们压到该栈中，

- PC寄存器

每一个线程都有一个程序计数器，存储下一次执行方法的指针，指令的地址

- 方法区

类信息（构造方法、接口定义）、静态变量、常量、运行时的常量池，该区为共享区间；GC处理的地方；永久代和元信息的概念就是方法区接口定义的实现，不要搞混了

asm生成类文件的


- 栈区

栈内存：本地变量（输入参数，输出参数，方法内变量）+栈操作（出栈、入栈操作）+栈帧数据（包括类文件，方法等）

由于两天没学习把玩了5年的游戏卸载了！

栈存的都是栈帧

占中存放对象的引用，堆中存放具体地址，方法区存放类的基本信息className

- 堆

新生代+ 养老代+永久代

堆逻辑上新生代+养老+方法区

永久代/元空间是方法区的实现

-Xmx32M -Xms32M

outofMemoryError:Java heap space 堆溢出

怎么解决这个问题？


Runtime.getRuntime().maxMemory();//java虚拟机试图使用最大内存

Runtime.getRuntime().totalMemory();//java虚拟机中内存总量

-Xms1024m -Xmx1024m -XX:+PrintGCDetails

输出都是981.5MB

Heap

PSYoungGen  


-Xms1m -Xmx8m -XX:+HeapDumpOnOutOfMemoryError,dump出堆溢出信息

 GCroot不可达，对象到不了GCroot就被回收，解决相互引用的问题

 GCroot对象可以是：虚拟机栈（局部变量表）、方法区的类属性所引用的对象、方法区的常量所引用的对象、本地方法栈所引用的对象

回收算法：

标记清除算法

从GCroot开始扫描再清除

复制算法：

从from到to，只能使用一个空间，也耗时，基本没有空间浪费碎片

标记整理：

标记扫描，并到一端，耗时

永远记得一个网站，java语言规范，与虚拟机规范地址

https://docs.oracle.com/javase/specs/

eden s0 s1 old memory 元空间

对象相关内容

对象创建

给对象分配内存+

线程安全性问题+

初始化对象+

执行构造方法


指针碰撞，已分配内存和未分配内存中间有个指针，通过移动指针分配内存空间

空间列表，有个表记录哪些内存空间已经分配

线程安全问题：

当多个线程同时分配内存空间时出现的问题

线程同步(基本不用)

本地线程分配缓冲（TLAB），每个线程内单独分配内存

对象的结构

- Header(对象头)
  - 自身运行时数据

   【自身运行时数据、哈希值、GC分代年龄、锁状态标志、线程持有锁、偏向线程ID、偏向时间戳】

   - 类型指针
   - 数组长度【只有数组对象有】

- InstanceData

 相同宽度的数据分配到一起（long/double）

- Padding(对象填充)

8字节的整数倍

对象的访问定位

使用句柄

直接指针

垃圾回收

如何判断对象是垃圾

引用计数法：性能差、相互引用问题

-verbose:gc -XX:+PrintGCDetails

 大对象直接分配到老年代，-XX:PretenureSizeThreshold

 -verbose:gc -XX:+PrintGCDetails -Xmx20 -XMS10m -XX:pretenureSizeThreshold=6m

 长期存活的对象分配老年代,15次GC还活着就去老年代

 -XX:MaxTenuringThreshold=15

空间分配担保

-XX:+HandlePromotionFailure,检查老年代最大可连续空间是否大于历次晋升到老年代对象的平均大小，默认启动的，不用管

动态对象年龄

-XX：TargetSurvivorRaton

逃逸分析和栈上分配

方法内部的对象返回到了方法外或者赋值给方法外的对象变量就是对象逃逸

栈上分配，方法内的变量栈内分配

class文件

java -verbose xx.class文件进行反编译



垃圾收集器

java7

-XX:MaxGCPauseMillis 垃圾收集停顿时间：这个值设置小了，空间就会小，不停的GC导致吞吐量减小

-XX:GCTimeRatio吞吐量大小（0，100）
