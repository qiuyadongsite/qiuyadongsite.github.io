---
layout: post
title:  并发编程实现原理2
date:   2019-04-01 21:52:12 +08:00
category: 并发编程
tags: 并发编程
comments: true
---

* content
{:toc}


synchronized的原理






## synchronized的使用

在多线程并发编程中synchronized一直是元老级角色，很多人都会称呼它为重量级锁。但是，随着Java SE 1.6对synchronized进行了各种优化之后，有些情况下它就并不那么重了，Java SE 1.6中为了减少获得锁和释放锁带来的性能消耗而引入的偏向锁和轻量级锁，以及锁的存储结构和升级过程。

```java

public class Demo{
private static int count=0;
public static void inc(){
synchronized (Demo.class) {
try {
Thread.sleep(1);
} catch (InterruptedException e) {
e.printStackTrace();
}
count++;
}
}
public static void main(String[] args) throws InterruptedException {
for(int i=0;i<1000;i++){
new Thread(()->Demo.inc()).start();
}
Thread.sleep(3000);
System.out.println("运行结果"+count);
}
}

```

synchronized有三种方式来加锁，分别是

1 修饰实例方法，作用于当前实例加锁，进入同步代码前要获得当前实例的锁

2 静态方法，作用于当前类对象加锁，进入同步代码前要获得当前类对象的锁

3 修饰代码块，指定加锁对象，对给定对象加锁，进入同步代码库前要获得给定对象的锁。

synchronized括号后面的对象

synchronized扩号后后面的对象是一把锁，在java中任意一个对象都可以成为锁，简单来说，我们把object比喻是一个key，拥有这个key的线程才能执行这个方法，拿到这个key以后在执行方法过程中，这个key是随身携带的，并且只有一把。

如果后续的线程想访问当前方法，因为没有key所以不能访问只能在门口等着，等之前的线程把key放回去。所以，synchronized锁定的对象必须是同一个，如果是不同对象，就意味着是不同的房间的钥匙，对于访问者来说是没有任何影响的

synchronized的字节码指令

通过javap -v 来查看对应代码的字节码指令，对于同步块的实现使用了monitorenter和monitorexit指令，前面我们在讲JMM的时候，提到过这两个指令，他们隐式的执行了Lock和UnLock操作，用于提供原子性保证。

monitorenter指令插入到同步代码块开始的位置、monitorexit指令插入到同步代码块结束位置，jvm需要保证每个monitorenter都有一个monitorexit对应。

这两个指令，本质上都是对一个对象的监视器(monitor)进行获取，这个过程是排他的，也就是说同一时刻只能有一个线程获取到由synchronized所保护对象的监视器线程执行到monitorenter指令时，会尝试获取对象所对应的monitor所有权，也就是尝试获取对象的锁；而执行monitorexit，就是释放monitor的所有权

## synchronized的锁的原理

jdk1.6以后对synchronized锁进行了优化，包含偏向锁、轻量级锁、重量级锁; 在了解synchronized锁之前，我们需要了解两个重要的概念，一个是对象头、另一个是monitor

Java对象头

在Hotspot虚拟机中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充；

Java对象头是实现synchronized的锁对象的基础，一般而言，synchronized使用的锁对象是存储在Java对象头里。它是轻量级锁和偏向锁的关键

Mawrk Word

Mark Word用于存储对象自身的运行时数据，如哈希码（HashCode）、GC分代年龄、锁状态标志、线程持有的锁、偏向线程 ID、偏向时间戳等等。Java对象头一般占有两个机器码（在32位虚拟机中，1个机器码等于4字节，也就是32bit）

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/MawrkWord001.png)

在源码中的体现

如果想更深入了解对象头在JVM源码中的定义，需要关心几个文件，oop.hpp/markOop.hpp

oop.hpp，每个 Java Object 在 JVM 内部都有一个 native 的 C++ 对象 oop/oopDesc 与之对应。先在oop.hpp中看oopDesc的定义

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/oopDesc001.png)

```

_mark 被声明在 oopDesc 类的顶部，所以这个 _mark 可以认为是一个 头部, 前面我们讲过头部保存了一些重要的
状态和标识信息，在markOop.hpp文件中有一些注释说明markOop的内存布局
```

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/oopdesc002.png)

Monitor

什么是Monitor？我们可以把它理解为一个同步工具，也可以描述为一种同步机制。所有的Java对象是天生的Monitor，每个object的对象里 markOop->monitor() 里可以保存ObjectMonitor的对象。从源码层面分析一下monitor对象

 oop.hpp下的oopDesc类是JVM对象的顶级基类，所以每个object对象都包含markOop

markOop.hpp**中** markOopDesc继承自oopDesc，并扩展了自己的monitor方法，这个方法返回一个ObjectMonitor指针对象


 objectMonitor.hpp,在hotspot虚拟机中，采用ObjectMonitor类来实现monitor，

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/objectmonitor001.png)

下图展示获取锁和释放锁monitor中数据变化：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/syn000001.png)
