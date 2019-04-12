---
layout: post
title:  高并发编程-Unsafe类
date:   2019-04-12 19:56:12 +08:00
category: 并发编程
tags: 并发编程
comments: true
---

* content
{:toc}

Unsafe类使Java拥有了像C语言的指针一样操作内存空间的能力，同时也带来了指针的问题。过度的使用Unsafe类会使得出错的几率变大，因此Java官方并不建议使用的，官方文档也几乎没有。Oracle正在计划从Java 9中去掉Unsafe类，如果真是如此影响就太大了。










## Unsafe的主要功能

我们先来看看Unsafe的初始化方法，这是一个单例模式：

```java

private Unsafe() {}
private static final Unsafe theUnsafe = new Unsafe();
public static Unsafe getUnsafe() {
    Class cc = sun.reflect.Reflection.getCallerClass(2);
    if (cc.getClassLoader() != null)
        throw new SecurityException("Unsafe");
    return theUnsafe;
}

```

方法中，限制了它的 ClassLoader，如果这个方法的调用实例不是由Boot ClassLoader加载的，则会报错。

可以做一个实验，因为Java源码中的类，除扩展包都是由Boot ClassLoader加载的，如果我们new一个Object对象，查看Object对的ClassLoader，它一定是null。所以，正常情况下开发者无法直接使用Unsafe ，如果需要使用它，则需要利用反射

```java

private static Unsafe getUnsafe(){
    try {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        Unsafe unsafe = (Unsafe) field.get(null);
        return unsafe;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return null;
}
```

从Unsafe的方法入手，发现Unsafe主要有以下几个方面的功能：

操纵对象属性

操纵数组元素

线程挂起与恢复、CAS
