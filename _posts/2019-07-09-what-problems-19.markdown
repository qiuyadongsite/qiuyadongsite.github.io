---
layout: post
title:  JVM2
date:   2019-07-09 20:52:12 +08:00
category: 归零
tags: 性能优化
comments: true
---

* content
{:toc}


JVM!











## 垃圾收集器

CMS

## GC调优工具

- jps
  - java process status
  - jps -l 主类的参数
  - jps -m 运行传入主类的参数
  - jps -v 虚拟机参数

- jstat
  - 类加载，内存，垃圾收集，jit编译信息
  - https://docs.oracle.com/javase/8/docs/technotes/tools/unix/jstat.html

- jinfo
  - 实时调整和查看虚拟机参数
  - -XX:[+/-]option
  - -XX:option=value

- jmap
  - jmap -dump:format=b,file=filepath pid
  - jmap -histo pid

- jhat
  - JVM heap Analysis tool

- jstack

- jconsole

## 性能调优

- 理论

- 工具

- 数据

- 经验

jps

jstat -gcutil [pid] 1000   //每一秒钟打印一次gc状态

jstack 经常使用的命令

jstack -l p [pid] > 11.text

println %x 10

jmap把堆的信息打出来

jmap -heap [pid]

dump出文件之后使用eclipse Memory Analyzer分析工具进行分析
