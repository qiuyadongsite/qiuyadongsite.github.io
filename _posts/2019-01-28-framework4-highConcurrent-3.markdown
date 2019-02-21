---
layout: post
title:  JMM解决多线程问题
date:   2019-01-28 23:52:12 +08:00
category: 高并发分布式
tags: 多线程
comments: true
---

* content
{:toc}

对于高并发编程，多线程是重点，JMM怎么解决原子性、可见性、有序性的问题？












## JMM概念

在Java中提供了一系列和并发处理相关的关键字，比如volatile、Synchronized、final、juc等，使得我们不需要关心底层的编译器优化、缓存一致性的问题了，所以在Java内存模型中，除了定义了一套规范，还提供了开放的指令在底层进行封装后，提供给开发人员使用。

- 原子性保障

在java中提供了两个高级的字节码指令monitorenter和monitorexit，在Java中对应的Synchronized来保证代码块内的操作是原子的。

- 可见性

Java中的volatile关键字提供了一个功能，那就是被其修饰的变量在被修改后可以立即同步到主内存，被其修饰的变量在每次是用之前都从主内存刷新。因此，可以使用volatile来保证多线程操作时变量的可见性。

除了volatile，Java中的synchronized和final两个关键字也可以实现可见性

- 有序性

在Java中，可以使用synchronized和volatile来保证多线程之间操作的有序性。实现方式有所区别：volatile关键字会禁止指令重排。synchronized关键字保证同一时刻只允许一条线程操作。

- volatile如何保证可见性

JVM就会向处理器发送一条Lock前缀的指令，把这个变量所在的缓存行的数据写回到系统内存，再根据我们前面提到过的MESI的缓存一致性协议，来保证多CPU下的各个高速缓存中的数据的一致。

- volatile防止指令重排序

指令重排的目的是为了最大化的提高CPU利用率以及性能，CPU的乱序执行优化在单核时代并不影响正确性，但是在多核时代的多线程能够在不同的核心上实现真正的并行，一旦线程之间共享数据，就可能会出现一些不可预料的问题。

指令重排序必须要遵循的原则是，不影响代码执行的最终结果，编译器和处理器不会改变存在数据依赖关系的两个操作的执行顺序，(这里所说的数据依赖性仅仅是针对单个处理器中执行的指令和单个线程中执行的操作.)这个语义，实际上就是as-if-serial语义，不管怎么重排序，单线程程序的执行结果不会改变，编译器、处理器都必须遵守as-if-serial语义。

- 多核心多线程下的指令重排影响

```java
private static int x = 0, y = 0;
private static int a = 0, b = 0;
public static void main(String[] args) throws InterruptedException {
Thread t1 = new Thread(() -> {
a = 1;
x = b;
});
Thread t2 = new Thread(() -> {
b = 1;
y = a;
});
t1.start();
t2.start();
t1.join();
t2.join();
System.out.println("x=" + x + "->y=" + y);
}

```

所以单纯的解决可见性问题还不够，还需要解决处理器重排序问题。

- 内存屏障

两个问题，一个是编译器的优化乱序和CPU的执行乱序，我们可以分别使用优化屏障和内存屏障这两个机制来解决：

- volatile为什么不能保证原子性

set或者get的场景是适合volatile。

对一个原子递增的操作，会分为三个步骤：1.读取volatile变量的值到local；2.增加变量的值；3.把local的值写回让其他线程可见。

- synchronized的使用
备注：为重量级锁

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
修饰实例方法\ 静态方法\ 修饰代码块
