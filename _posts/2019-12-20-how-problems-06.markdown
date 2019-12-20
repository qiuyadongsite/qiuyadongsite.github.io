---
layout: post
title:  JVM2
date:   2019-12-20 20:52:12 +08:00
category: 签到系列
tags: 性能优化
comments: true
---

* content
{:toc}


签到6！






## jvm参数

### 标准参数(稳定不变)
-version
-help

### 非标准参数（可变根据版本）

- -X参数

-Xint 解释执行 -Xcomp 第一次使用就编译成本地代码 -Xmixed 混合模式

- -XX设置参数
  - boolean类型的参数（-禁用+使用）：-XX:+UseConcMarkSweepGC使用cms垃圾回收器

  - 非boolean参数：-XX:MaxGCPauseMaillis=500

  - 其他参数：-Xms1000=-XX:+InitalHeapSize=1000 -Xmx1000=-XX:MaxHeapSize=1000 -Xss100=-XX:+ThreadStackSize=100

- 查看参数：-XX:+PrintFlagsFinal -version>flags.txt 打印所有jvm参数以及值 查看具体参数使用jinfo

### 设置方法

开发工具eclipse、ideal中设置

运行jar包时设置 java -XX:+UseG1GC xxx.jar

web容器如tomcat会在脚本里设置

也可以通过jinfo实时调整java进程的参数 jinfo -flag [flag] [pid]

```
//常见参数
CICompilerCount
InitHeapSize
MaxHeapSize
NewSize
MaxNewSize
OldSize
MetaspaceSize
MaxMetaspaceSize
UseParallelGC
UseParallelOldGC
UseConcMarkSweepGC
UseG1GC
NewRatio
SurvivorRatio
HeapDumpOnOutOfMemoryError
HeapDumpPath
PrintGCDetails
PrintGCTimeStamps
PrintGCDateStamps
Xloggc:$CATALINA_HOME/logs/gc.log
-Xss128
MaxTenuringThreshold
InitiatingHeapOccupancyPercent
G1HeapWastePercent
MaxGCPauseMillis
ConcGCThreads
G1OldCSetRegionThresholdPercent=1 g1只把10%的oldRegion 加入到CSet中


```

## 常用命令

### jps

查看java进程信息

### jinfo

- 实时查看jvm的参数

jinfo -flag name pid

jinfo -flag UseG1GC 12265

- 修改参数（只能修改PrintFlagsFinal中type为Managetable的flag）

jinfo -flag +/-Boolean pid

jinfo -flag xx=ee pid

- 查看进程中设置过的参数

jinfo -flags pid

### jstat 查看虚拟机性能统计信息

- 查看类加载信息

jstat -class pid 1000 10 查看类每1000毫米加载10次打印出来加载内容

jstat -gc pid 1000 10 查看垃圾回收情况

### jstack 查看堆栈信息

jstack pid 查看线程的加载堆栈的状态信息 如查看死锁

### jmap 生成堆栈为快照

- 打印堆内存相关信息

jmap -heap pid

- dump堆内存相关信息

jmap -dump:format=b,file=heap.hprof pid

- 当内存溢出时，自动dump出文件

-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=xx.hpof

## 常用的工具

就是将上述的查看信息统计出来使用图形界面展示；

### jconsole

统计堆内存、线程、类、vm概要、Mbean

### jvisualvm

可以监控Cpu、堆内存、类、线程、概要、还可以使用ViualGC插件形象查看垃圾回收过程

也可以远程监控tomcat等

### arthas

是阿里开源的java诊断工具，采用命令交互模式

java -jar arthas-boot.jar -h

查看如何使用

### MAT

java堆分析器，用于查找内存泄漏

首先dump出文件

手动

jmap -dump:format=b,file=heap.hprof 11111

自动

-XX:+HeapDumpOnOutOfMemoryError -XX:HeapPath=xx.hprof

在HistoGram中可以列出内存中的对象，个数及大小

Leak Suspects 查看内存泄漏的主要原因

Top customers 查看大对象

### GC日志分析工具

先保存gc日志

-XX:+PrintGCDEtails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps

-Xloggc:gc.log

- 在线工具gceasy：https://gceasy.io

- gcviewer-1.36-SNAPSHOT

关注吞吐量、停顿时间、GC次数

## 垃圾回收

什么情况下GC?

1、当eden区或者s区不够用

2、老年代不够用了

3、方法区不够用了

4、调用了system.gc()

怎么打印出gc日志？

-XX:+PrintGCDetails -XX:+PrintGCTimeStamps -XX:+PrintGCDateStamps

-Xloggc:gc.log

关注的就三个点：

CMS、G1关注停顿时间的

ParallelGC、ParallelOldGC关注吞吐量的

垃圾回收次数将会影响cpu是否频繁

### 如果调优

是否选择G1

1、50%的堆被存活对象占用
2、分配对象和晋升速度变化非常大
3、垃圾回收时间比较长

-XX:+UseG1GC 使用G1

观看吞吐量、最大最小停顿时间、平均停顿时间、gc次数

- 这个是垃圾回收器选择

- 调整堆的大小

-XX:MateSpaceSize 设置元空间大小

-Xms500M
-Xmx500M

- 调整最大停顿时间

-XX:MaxGCPauseMillis=20

- 启动并发GC时堆空间占用百分比

-XX:InitiatingHeapOccupancyPercent=45

超过这个值才GC

### G1的调整指南

不要手动设置新生代和老年代的大小，只要设置堆大小就行了，因为它会自动调整来满足最有停顿时间的要求

不断调优暂停时间，但是不要太严格，否则gc次数会大，pc频繁调用曾大了pc的负荷

使用-XX:ConcGCThreads来曾加标记线程的数量

要适当曾加堆的内存大小，并不是越大越好，越大那么gc一次的时间就会越大
