---
layout: post
title:  工厂设计模式
date:   2018-12-24 22:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中创建类模式，工厂包含简单工厂，抽象工厂。














## 简单工厂

简单工厂模式中，只要添加新的产品类，就得去修改工厂类，这样做势必违反了开闭原则。

```java

public class SimpleFactory {

    public Milk getMilk(String name){
        if("特仑苏".equals(name)){
            return new Telunsu();
        }else if("伊利".equals(name)){
            return new Yili();
        }else if("蒙牛".equals(name)){
            return new Mengniu();
        }else {
            System.out.println("不能生产您所需的产品");
            return null;
        }
    }

}

```
test

```java

/**
 * 小作坊式的工厂模型
 */
public class SimpleFactoryTest {
    public static void main(String[] args) {
        //这个new的过程实际上一个比较复杂的过程
        //有人民币及不需要自己new了
       // System.out.println(new Telunsu().getName());

        //小作坊式的生产模式
        //用户本身不再关心生产的过程，而只需要关心这个结果

        //假如：特仑苏、伊利、蒙牛
        //成分配比都是不一样的
        SimpleFactory factory = new SimpleFactory();

        //把用户的需求告诉工厂
        //创建产品的过程隐藏了，对于用户而且完全不清楚是怎么产生的
        System.out.println(factory.getMilk("AAA"));
        //知其然，知其所以然，知其所必然

    }
}


```
## 抽象工厂

新增抽象工厂类，让抽象产品对应抽象工厂，让具体产品对应具体工厂，实际的创建工作推迟到子类工厂中去做。

工厂接口-标准

```java
/**
 * 工厂模型
 */
public interface Factory {

    //工厂必然具有生产产品技能，统一的产品出口
    Milk getMilk();

}

```

蒙牛子场

```java

public class MengniuFactory implements  Factory {
    @Override
    public Milk getMilk() {
        return new Mengniu();
    }
}

```

test

```java

public class FactoryTest {
    public static void main(String[] args) {
        //货比三家
        //不知道谁好谁好谁坏
        //配置，可能会配置错
        Factory factory = new SanluFactory();
        System.out.println(factory.getMilk());

    }
}

```
