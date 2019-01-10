---
layout: post
title:  委派设计模式
date:   2019-01-10 21:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中行为类模式，委派设计模式。














## 委派模式（delegate）,区别于代理、策略

应用场景：项目经理看上去BOSS和员工之间的一个中介

代理模式特殊形式：全权代理

重要特征：项目经理分配任务之前，做一个权衡，类似于策略模式

形式：以delegate、dispatcher结尾的

BOSS类

```
public class Boss {

    public static void main(String[] args) {

        //客户请求（Boss）、委派者（Leader）、被被委派者（Target）
        //委派者要持有被委派者的引用
        //代理模式注重的是过程， 委派模式注重的是结果
        //策略模式注重是可扩展（外部扩展），委派模式注重内部的灵活和复用
        //委派的核心：就是分发、调度、派遣

        //委派模式：就是静态代理和策略模式一种特殊的组合

        new Leader().doing("登录");

    }

}
```

定义统一实现接口（老板关注的只有结果，谁干都一样）

```
public interface ITarget {

    public void doing(String command);

}
```
项目经理就是委派角色

```
public class Leader implements  ITarget {

    private Map<String,ITarget> targets = new HashMap<String,ITarget>();

    public Leader() {
        targets.put("加密",new TargetA());
        targets.put("登录",new TargetB());
    }

    //项目经理自己不干活
    public void doing(String command){
        targets.get(command).doing(command);
    }

}

```

具体干活的目标
员工a

```
public class TargetA implements ITarget {
    @Override
    public void doing(String command) {
        System.out.println("我是员工A，我现在开始干" + command + "工作");
    }
}

```
员工b
```
public class TargetB implements  ITarget {
    @Override
    public void doing(String command) {
        System.out.println("我是员工B，我现在开始干" + command + "工作");
    }
}

```
