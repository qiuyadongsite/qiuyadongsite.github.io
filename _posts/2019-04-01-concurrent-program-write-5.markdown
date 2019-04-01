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
