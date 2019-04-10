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

Thread中的变量

```java

/* ThreadLocal values pertaining to this thread. This map is maintained
    * by the ThreadLocal class. */
   ThreadLocal.ThreadLocalMap threadLocals = null;

```

ThreadLocalMap类的定义是在ThreadLocal类中，真正的引用却是在Thread类中。同时，ThreadLocalMap中用于存储数据的entry定义：

```java

static class Entry extends WeakReference<ThreadLocal<?>> {
            /** The value associated with this ThreadLocal. */
            Object value;

            Entry(ThreadLocal<?> k, Object v) {
                super(k);
                value = v;
            }
}

```

从中我们可以发现这个Map的key是ThreadLocal类的实例对象，value为用户的值，并不是网上大多数的例子key是线程的名字或者标识。

ThreadLocal的set和get方法代码:

```java

public void set(T value) {
       Thread t = Thread.currentThread();
       ThreadLocalMap map = getMap(t);
       if (map != null)
           map.set(this, value);
       else
           createMap(t, value);
   }

   public T get() {
       Thread t = Thread.currentThread();
       ThreadLocalMap map = getMap(t);
       if (map != null) {
           ThreadLocalMap.Entry e = map.getEntry(this);
           if (e != null) {
               @SuppressWarnings("unchecked")
               T result = (T)e.value;
               return result;
           }
       }
       return setInitialValue();
   }

```

其中的getMap方法：

```java

ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }
```

给当前Thread类对象初始化ThreadlocalMap属性：

```java

void createMap(Thread t, T firstValue) {
       t.threadLocals = new ThreadLocalMap(this, firstValue);
   }
```

## 我们就可以理解ThreadLocal究竟是如何工作的了?

Thread类中有一个成员变量属于ThreadLocalMap类(一个定义在ThreadLocal类中的内部类)，它是一个Map，他的key是ThreadLocal实例对象。

当为ThreadLocal类的对象set值时，首先获得当前线程的ThreadLocalMap类属性，然后以ThreadLocal类的对象为key，设定value。get值时则类似。

ThreadLocal变量的活动范围为某线程，是该线程“专有的，独自霸占”的，对该变量的所有操作均由该线程完成！

也就是说，ThreadLocal 不是用来解决共享对象的多线程访问的竞争问题的，因为ThreadLocal.set() 到线程中的对象是该线程自己使用的对象，其他线程是不需要访问的，也访问不到的。当线程终止后，这些值会作为垃圾回收。

由ThreadLocal的工作原理决定了：每个线程独自拥有一个变量，并非是共享的，下面给出一个例子：

```java

public class Son implements Cloneable{
    public static void main(String[] args){
        Son p=new Son();
        System.out.println(p);
        Thread t = new Thread(new Runnable(){  
            public void run(){
                ThreadLocal<Son> threadLocal = new ThreadLocal<>();
                System.out.println(threadLocal);
                threadLocal.set(p);
                System.out.println(threadLocal.get());
                threadLocal.remove();
                try {
                    threadLocal.set((Son) p.clone());
                    System.out.println(threadLocal.get());
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
                System.out.println(threadLocal);
            }});
        t.start();
    }
}

//输出：
//Son@7852e922
//java.lang.ThreadLocal@3ffc8195
//Son@7852e922
//Son@313b781a
//java.lang.ThreadLocal@3ffc8195

```

也就是如果把一个共享的对象直接保存到ThreadLocal中，那么多个线程的ThreadLocal.get()取得的还是这个共享对象本身，还是有并发访问问题。

所以要在保存到ThreadLocal之前，通过克隆或者new来创建新的对象，然后再进行保存。

ThreadLocal的作用是提供线程内的局部变量，这种变量在线程的生命周期内起作用。

作用：提供一个线程内公共变量（比如本次请求的用户信息），减少同一个线程内多个函数或者组件之间一些公共变量的传递的复杂度，或者为线程提供一个私有的变量副本，这样每一个线程都可以随意修改自己的变量副本，而不会对其他线程产生影响。

## 如何实现一个线程多个ThreadLocal对象，每一个ThreadLocal对象是如何区分的呢？

查看源码，可以看到：

```java

private final int threadLocalHashCode = nextHashCode();
private static AtomicInteger nextHashCode = new AtomicInteger();
private static final int HASH_INCREMENT = 0x61c88647;
private static int nextHashCode() {
      return nextHashCode.getAndAdd(HASH_INCREMENT);
}
```

对于每一个ThreadLocal对象，都有一个final修饰的int型的threadLocalHashCode不可变属性，对于基本数据类型，可以认为它在初始化后就不可以进行修改，所以可以唯一确定一个ThreadLocal对象。

但是如何保证两个同时实例化的ThreadLocal对象有不同的threadLocalHashCode属性：

在ThreadLocal类中，还包含了一个static修饰的AtomicInteger（[əˈtɒmɪk]提供原子操作的Integer类）成员变量（即类变量）和一个static final修饰的常量（作为两个相邻nextHashCode的差值）。由于nextHashCode是类变量，所以每一次调用ThreadLocal类都可以保证nextHashCode被更新到新的值，并且下一次调用ThreadLocal类这个被更新的值仍然可用，同时AtomicInteger保证了nextHashCode自增的原子性。

## 为什么不直接用线程id来作为ThreadLocalMap的key？

这一点很容易理解，因为直接用线程id来作为ThreadLocalMap的key，无法区分放入ThreadLocalMap中的多个value。比如我们放入了两个字符串，你如何知道我要取出来的是哪一个字符串呢？

　　而使用ThreadLocal作为key就不一样了，由于每一个ThreadLocal对象都可以由threadLocalHashCode属性唯一区分或者说每一个ThreadLocal对象都可以由这个对象的名字唯一区分（下面的例子），所以可以用不同的ThreadLocal作为key，区分不同的value，方便存取。

```java

public class Son implements Cloneable{
    public static void main(String[] args){
        Thread t = new Thread(new Runnable(){  
            public void run(){
                ThreadLocal<Son> threadLocal1 = new ThreadLocal<>();
                threadLocal1.set(new Son());
                System.out.println(threadLocal1.get());
                ThreadLocal<Son> threadLocal2 = new ThreadLocal<>();
                threadLocal2.set(new Son());
                System.out.println(threadLocal2.get());
            }});
        t.start();
    }
}

```

## ThreadLocal的内存泄露问题

根据上面Entry方法的源码，我们知道ThreadLocalMap是使用ThreadLocal的弱引用作为Key的。下图是本文介绍到的一些对象之间的引用关系图，实线表示强引用，虚线表示弱引用：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/threadlocal009.png)

如上图，ThreadLocalMap使用ThreadLocal的弱引用作为key，如果一个ThreadLocal没有外部强引用引用他，那么系统gc的时候，这个ThreadLocal势必会被回收，这样一来，ThreadLocalMap中就会出现key为null的Entry，就没有办法访问这些key为null的Entry的value，如果当前线程再迟迟不结束的话，这些key为null的Entry的value就会一直存在一条强引用链：

Thread Ref -> Thread -> ThreaLocalMap -> Entry -> value

永远无法回收，造成内存泄露。

ThreadLocalMap设计时的对上面问题的对策：

ThreadLocalMap的getEntry函数的流程大概为：

首先从ThreadLocal的直接索引位置(通过ThreadLocal.threadLocalHashCode & (table.length-1)运算得到)获取Entry e，如果e不为null并且key相同则返回e；

如果e为null或者key不一致则向下一个位置查询，如果下一个位置的key和当前需要查询的key相等，则返回对应的Entry。

否则，如果key值为null，则擦除该位置的Entry，并继续向下一个位置查询。在这个过程中遇到的key为null的Entry都会被擦除，那么Entry内的value也就没有强引用链，自然会被回收。

仔细研究代码可以发现，set操作也有类似的思想，将key为null的这些Entry都删除，防止内存泄露。

但是光这样还是不够的，上面的设计思路依赖一个前提条件：要调用ThreadLocalMap的getEntry函数或者set函数。

这当然是不可能任何情况都成立的，所以很多情况下需要使用者手动调用ThreadLocal的remove函数，手动删除不再需要的ThreadLocal，防止内存泄露。

所以JDK建议将ThreadLocal变量定义成private static的，这样的话ThreadLocal的生命周期就更长，由于一直存在ThreadLocal的强引用，所以ThreadLocal也就不会被回收，也就能保证任何时候都能根据ThreadLocal的弱引用访问到Entry的value值，然后remove它，防止内存泄露。
