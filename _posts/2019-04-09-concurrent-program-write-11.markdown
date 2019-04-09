---
layout: post
title:  高并发编程-ThreadLocal
date:   2019-04-09 22:54:12 +08:00
category: 并发编程
tags: 并发编程
comments: true
---

* content
{:toc}


ThreadLocal类是修饰变量的，重点是在控制变量的作用域，初衷可不是为了解决线程并发和线程冲突的，而是为了让变量的种类变的更多更丰富，方便人们使用罢了。很多开发语言在语言级别都提供这种作用域的变量类型。

根据变量的作用域，可以将变量分为全局变量，局部变量。

简单的说，类里面定义的变量是全局变量，函数里面定义的变量是局部变量。

还有一种作用域是线程作用域，线程一般是跨越几个函数的。

为了在几个函数之间共用一个变量，所以才出现：线程变量，这种变量在Java中就是ThreadLocal变量。

全局变量，范围很大；局部变量，范围很小。无论是大还是小，其实都是定死的。而线程变量，调用几个函数，则决定了它的作用域有多大。

ThreadLocal是跨函数的，虽然全局变量也是跨函数的，但是跨所有的函数，而且不是动态的。

ThreadLocal也是跨函数的，但是跨哪些函数呢，由线程来定，更灵活。

ThreadLocal类是修饰变量的，是在控制它的作用域，是为了增加变量的种类而已，这才是ThreadLocal类诞生的初衷，它的初衷可不是解决线程冲突的。








## 源码分析

为了解释ThreadLocal类的工作原理，必须同时介绍与其工作甚密的其他几个类

ThreadLocalMap（内部类）
Thread

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/threadlocal001.png)
