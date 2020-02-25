---
layout: post
title:  序列化与反序列化
date:   2020-02-25 20:53:12 +08:00
category: 签到系列
tags: 序列化
comments: true
---

* content
{:toc}

签到27！



# 学习总结

日暮苍山远，天寒白屋贫。

柴门闻犬吠，风雪夜归人。

## 1、序列化是什么？  

java平台中的对象一般会随着jvm的生命周期结束而结束，为了保存持久化该对象或者传输，需要将该对象序列化；就是将对象转化为字节序列的过程；反序列化就是将字节序列转化成对象的过程；

## 2、JDK原生序列化和反序列化

jdk提供了两个类:

- ObjectOutputStream

  - writeObject(obj)

    将obj对象进行序列化，把得到的序列写到目标输出流

- ObjectInputStream

  - readObject()

    将原输入流中读取的字节序列，反序列化对象

#### 注意事项：

- 该对象需要实现Serializable接口

  提供了SerialVersionUID，提供了一个序列化标识符

  - SerialVersionUID

    有了该版本号，比对版本号，如果没有配置版本号，无论是增加字段方法还是修改文件，都会导致反序列化失败！所以必须配置版本号；

    ```java
    private static final long serialVersionUID=1L;
    ```

- Transient

  在变量的声明前添加，阻止其序列化，其值被设置成初始值！int设置为0.对象设置为null

  - 绕过Transient的方法

    对象的readObject()、writeObject()两个方法；

总结：

1. 序列化只针对对象的状态进行保存，对象的方法不关心；
2. 父类序列化子类一定也序列化了
3. 当一个对象实例引入拎一个对象，另一个对象也进行序列化（深度克隆）
4. Transient可以阻止序列化，readObject().wirteObject()可以绕开Transient.

## 3、常见序列化框架

**jdk序列化的问题**

- 序列化的文件太大，传输效率低
- 无法跟其他语言对接

**简单了解**

文件大小，序列化效率，可读性如何，语言是否相关；如下：

- xml

  XStream和jdk自身的xml序列化反序列化

- json

  - Jackson
  - 阿里的fastjson
  - goole的Gson

  jackson与fastjson比gson效率高，jackson与gson比fastjson稳定；fastjson的api易用性好；

- Hession序列化

- Avro序列化

- Kyro序列化

  dubbo现在已经不使用Hession2而使用kyro

- ProtoBuf序列化

  google的序列化框架，独立于平台和语言；

