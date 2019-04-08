---
layout: post
title:  高并发编程-CountDownLatch深入解析
date:   2019-04-08 21:54:12 +08:00
category: 并发编程
tags: 并发编程
comments: true
---

* content
{:toc}


CountDownLatch允许一个或者多个线程一直等待，直到一组其它操作执行完成。在使用CountDownLatch时，需要指定一个整数值，此值是线程将要等待的操作数。当某个线程为了要执行这些操作而等待时，需要调用await方法。await方法让线程进入休眠状态直到所有等待的操作完成为止。当等待的某个操作执行完成，它使用countDown方法来减少CountDownLatch类的内部计数器。当内部计数器递减为0时，CountDownLatch会唤醒所有调用await方法而休眠的线程们。







## 实例演示

```java

public class Test {
    public static final CountDownLatch countDownLatch= new CountDownLatch(20);

    public static void main(String[] args) {


        for(int i=0;i<20;i++){
            new Thread(()->{
                countDownLatch.countDown();
                try {
                    countDownLatch.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("我是第"+Thread.currentThread().getName());
            },i+"").start();
        }
    }
}


```
