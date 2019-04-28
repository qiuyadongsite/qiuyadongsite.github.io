---
layout: post
title:  代理设计模式
date:   2018-12-24 23:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中创建类模式，代理模式包含静态代理，动态代理。
应用场景：为其他对象提供一种代理以控制对这个对象的访问；
















代理模式图
![模式图](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/porxy.png)

## 静态代理

优点：可以做到不对目标对象进行修改的前提下，对目标对象进行功能的扩展和拦截。

缺点：因为代理对象，需要实现与目标对象一样的接口，会导致代理类十分繁多，不易维护，同时一旦接口增加方法，则目标对象和代理类都需要维护。

共同的接口

```java

/**
 * 目标对象实现的接口
 * @author jiyukai
 */
public interface BussinessInterface {
    void execute();
}

```

目标的实现类

```java

/**
 * 目标对象实现类
 * @author jiyukai
 */
public class Bussiness implements BussinessInterface{

    @Override
    public void execute() {
        System.out.println("执行业务逻辑...");
    }
}

```

代理类

```java

/**
 * 代理类，通过实现与目标对象相同的接口
 * 并维护一个代理对象，通过构造器传入实际目标对象并赋值
 * 执行代理对象实现的接口方法，实现对目标对象实现的干预
 * @author jiyukai
 */
public class BussinessProxy implements BussinessInterface{

    private BussinessInterface bussinessImpl;

    public BussinessProxy(BussinessInterface bussinessImpl) {
        this.bussinessImpl = bussinessImpl;
    }

    @Override
    public void execute() {
        System.out.println("前拦截...");
        bussinessImpl.execute();
        System.out.println("后拦截...");
    }
}

```

## 动态代理

动态代理是指动态的在内存中构建代理对象（需要我们制定要代理的目标对象实现的接口类型），即利用JDK的API生成指定接口的对象，也称之为JDK代理或者接口代理。

优点：代理对象无需实现接口，免去了编写很多代理类的烦恼，同时接口增加方法也无需再维护目标对象和代理对象，只需在事件处理器中添加对方法的判断即可。

缺点：代理对象不需要实现接口，但是目标对象一定要实现接口，否则无法使用JDK动态代理。

```java

/**
 * 动态代理对象，无需实现任何接口
 * 通过传入任何类型的目标对象并指定接口
 * 调用JDK接口动态创建代理对象
 * @author jiyukai
 */
public class ProxyFactory {

    private Object targetObject;

    public ProxyFactory(Object targetObject) {
        this.targetObject = targetObject;
    }

    public Object getProxyInstance(){
        return Proxy.newProxyInstance(
                targetObject.getClass().getClassLoader(), //和目标对象的类加载器保持一致
                targetObject.getClass().getInterfaces(), //目标对象实现的接口，因为需要根据接口动态生成对象
                new InvocationHandler() { //InvocationHandler:事件处理器，即对目标对象方法的执行

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        System.out.println("前拦截...");

                        Object result = method.invoke(proxy, args);

                        System.out.println("后拦截...");
                        return result;
                    }
                });
    }
}
```
