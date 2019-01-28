---
layout: post
title:  常用java概念
date:   2019-01-28 23:52:12 +08:00
category: 语言基础
tags: JAVASE
comments: true
---

* content
{:toc}

对于开发工程师来说，编程功底是地基，决定上层建筑，so，重视java基础！












## 受检异常和非受检异常及常用场景

- java里的异常包括以下

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/exception8.png)

Error 是 Throwable 的子类，代表编译时间和系统错误，用于指示合理的应用程序不应该试图捕获的严重问题。大多数这样的错误都是异常条件。

RuntimeException在默认情况下会得到自动处理。所以通常用不着捕获RuntimeException，但在自己的封装里，也许仍然要选择抛出一部分RuntimeException。

除了runtimeException以外的异常，都属于checkedException，它们都在java.lang库内部定义。Java编译器要求程序必须捕获或声明抛出这种异常。
