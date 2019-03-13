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

Error 是 Throwable 的子类，代表编译时间和系统错误，用于指示合理的应用程序不应该试图捕获的严重问题。不应该捕获，捕获也没用，与程序无关的异常。

RuntimeException在默认情况下会得到自动处理。所以通常用不着捕获RuntimeException，但在自己的封装里，也许仍然要选择抛出一部分RuntimeException。可捕获也可不捕获，一般是预料的一种情况。

除了runtimeException以外的异常，都属于checkedException，它们都在java.lang库内部定义。Java编译器要求程序必须捕获或声明抛出这种异常。IOException/SqlException。保证程序的健壮性必须捕获

处理方式：try catch / throws

平时自定义异常的写法是，继承自Exception,必须捕获

```java

public interface IUser{

  public void changePwd() throws SafeException，RejectChangeException;
}

public SafeException extends Excption{

  public SafeException(){
    super();
  }
  public SafeException(String message){
    super(message);
  }
}

public class ExceptionDemo{

  public static void main(String[] args){
    IUser user=null;

    try{
      user.changePwd();
    }catch(SafeException e){
      e.printStackTrace();
    }catch(RejectChangeException e){
      e.printStackTrace();
      //每次增加一类异常，都要修改主要罗纪
    }
  }
}

//如果想扩展changePwd的异常情况,则需要
public RejectChangeException extends Excption{

  public RejectChangeException(){
    super();
  }
  public RejectChangeException(String message){
    super(message);
  }
}

受捕获异常引发的问题：

 1 每次增加一类异常，都要修改主要罗纪
 2 降低代码的可读性

 如何解决守捕获异常引发的问题？

 引入runtimeException,转换为非受检异常，但是对于系统有重大影响的异常情况必须捕获！

```

## Java 的强引用、弱引用、软引用、虚引用

主要考察的是了解对象的生命周期，有效的利用可以提高垃圾回收效率！

强引用，即使再任何情况下也不会垃圾回收，宁愿抛出out of memeory 异常！

```java

public class Demo{
static Object strongRef = new Object();

public static void main(String[] args){
Object obj = strongRef;
strongRef = null;

system.gc();

system.out.printLn("gc之后："+obj);//这里不会被回收
//strongRef是静态成员变量，只有Demo被卸载的时候才会被回收，也就是等进程结束才会被卸载

//由于是强引用，obj是不可能被回收的；

//回收的情况有两种：1：脱离作用域，声明再方法内部，方法调用完成就回收。2：直接设置为null会被回收，如：obj=null;


}
}
```
软引用：

```java

public class Demo{

public static void main(String[] args){

Object softRef= new Object();
//只用在程序发生oom异常前才会被回收，只有在内存不足和设置为null才会被回收，有效的解决oom异常。
SoftReference softReference = new  SoftReference(softRef);
softRet= null;//这中情况不能被回收
softReference= null;//softRef会被回收，也会包空指针异常
system.out.println(softReference.get());



}

}

```

软引用和强引用再内存充足的情况下都不会被回收，只有当内存不足的情况下，软引用才会被回收，避免oom异常；

弱引用

```java

public class Demo{

public static void main(String[] args){

Object weakRef= new Object();

WeakReference weakReference = new  WeakReference(weakRef);

weakRef = null;

system.gc();

system.out.printLn("gc之后："+weakReference.get());//weakRef = null ，只要gc就会被回收

//下边一种重要情况
String  str= "hi";

WeakReference weakReference = new  WeakReference(str);

str = null;

system.gc();

system.out.printLn("gc之后："+weakReference.get());//这里不会被回收，因为string会存入常量池，gc不会回收常量池的内容

}

}

```

虚引用：

```java

public class Demo{

public static void main(String[] args){

RefereceQueue queue= new RefereceQueue();

Object phantoObj= new Object();

PhantomReference PhantomReference=new PhantomReference(phantoObj,queue);

System.out.println(PhantomReference.get());//这里永远都是null

phantoObj= null;

System.gc();

System.out.println(PhantomReference.get());//这里永远都是null

Thread.sleep(100);

System.out.println(queue.pull());//这里是哪个值。意思是该对象释放后，做后续的操作，知道某个对象将要被回收




}

}

```

## Integer的实现机制及装箱拆箱

```java

public class Demo{

public static void main(String[] args){

Integer a=1,b=2;

System.out.println("before a="+a+",b="+b);

swap(a,b);

System.out.println("after a="+a+",b="+b);

}
//这个方法不行
  public static swap(Integer i1,Integer i2){
//这里交换的值不会对原来的值产生影响
//？？？？？明明传递的是引用类型，为啥不行啊，
//解释：java里只有一种传递方式就是按值传递，因为java里都是传递的一个副本

//Integer源码里只用一个 final int value;根本无法改变其值，只能改变其一个副本



//对象的地址是存储在堆里的
//引用存储在栈里面
//引用类型的地址存储的堆里面，主要原因是速度，基本类型与引用类型的拷贝速度是不一样的，对象类型占内存比较大
    Integer tem=i1;
    i1=i2;
    i2=tem;

  }
}

//因为不可能改变final的值，所以用反射

public static swap(Integer i1,Integer i2){

  Field field=Integer.class.getDeclareField("value");
  field.setAccessible(true);//绕过安全检查，因为Field继承了Accessbile,能够修改accessbile值，当修改其值得时候，绕过去了
  int tem= i1.intValue;//tem等于i1
  field.set(i1,i2.intValue);//i1 -> Integer.valueof(i2.intValue).intValue();  此刻i1等于2
  field.set(i2,tem);//i2 ->Integer.valueof(tem).intValue();此刻tem是i1的下标，tem也是2了，所以i2 =2
  //因为valueof从缓存的下标去获取值
//结果是：before a=1，b=2
//       after a=2，b=2
//why     ------------？封箱和拆箱

//1,2都是int类型的，所以，Integer a=1是装箱操作
// 其实相当于Integer a=Integer.valueof("1")操作 -128~127,从缓存中取值
//验证方法：Integer i1=1;Integer i2=1; sysout(i1==i2);如果是对象肯定不等，但是使用了缓存，所以就取出来的对象是相等的


//所以改变的方法
public static swap(Integer i1,Integer i2){

  Field field=Integer.class.getDeclareField("value");
  field.setAccessible(true);
  Integer tem= new Integer(i1.intValue);//通过new，新建了一个新对象，隔离开了开箱封箱
  field.set(i1,i2.intValue);
  field.set(i2,tem);
}
//或者，所以改变的方法
public static swap(Integer i1,Integer i2){

  Field field=Integer.class.getDeclareField("value");
  field.setAccessible(true);
  int tem= i1.intValue;
  field.setInt(i1,i2.intValue);//没有装箱操作，内存地址也不会产生影响
  field.setInt(i2,tem);
}

}
}

```

























## 动态代理的实现机制

## 设计模式的使用落地

## List和set的区别

## ClassLoader的实现机制

##
