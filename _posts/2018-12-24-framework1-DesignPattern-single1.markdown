---
layout: post
title:  单例设计模式
date:   2018-12-24 20:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中创建类模式，单例包含饿汉式、懒汉式、注册式、序列化。














## 饿汉式

概念：它是在类加载的时候就立即初始化，并且创建单例对象
优点：没有加任何的锁、执行效率比较高，
缺点：类加载的时候就初始化，不管你用还是不用，我都占着空间，绝对线程安全，在线程还没出现以前就是实例化了，不可能存在访问安全问题；
```
public class Hungry {
    private Hungry(){}
    //先静态、后动态
    //先属性、后方法
    //先上后下
    private static final Hungry hungry = new Hungry();
    public static Hungry getInstance(){
        return  hungry;
    }
}
```

## 懒汉式

优点：用的时候加载，节省空间；
缺点：枷锁，性能差；
```
public class Lazy {
    private Lazy(){};
    private static Lazy instance=null;
    public static synchronized Lazy getInstance(){
            if(instance==null){
                instance=new Lazy();
            }
            return instance;
    }
}

```
补充：双重校验，像这样两次判空的机制叫做双重检测机制。
涉及到了JVM编译器的指令重排。
指令重排是什么意思呢？比如java中简单的一句 instance = new Singleton，会被编译器编译成如下JVM指令：

memory =allocate();    //1：分配对象的内存空间

ctorInstance(memory);  //2：初始化对象

instance =memory;     //3：设置instance指向刚分配的内存地址

但是这些指令顺序并非一成不变，有可能会经过JVM和CPU的优化，指令重排成下面的顺序：
memory =allocate();    //1：分配对象的内存空间
instance =memory;     //3：设置instance指向刚分配的内存地址
ctorInstance(memory);  //2：初始化对象

当线程A执行完1,3,时，instance对象还未完成初始化，但已经不再指向null。此时如果线程B抢占到CPU资源，执行  if（instance == null）的结果会是false，从而返回一个没有初始化完成的instance对象。
经过volatile的修饰，始终保证是下面的顺序：
memory =allocate();    //1：分配对象的内存空间
ctorInstance(memory);  //2：初始化对象
instance =memory;     //3：设置instance指向刚分配的内存地址
如此在线程B看来，instance对象的引用要么指向null，要么指向一个初始化完毕的Instance，而不会出现某个中间态，保证了安全。
```
public class Lazy {
    private Lazy(){};
    private static Lazy instance=null;
    public static  Lazy getInstance(){

            if(instance==null){
              synchronized(Lazy.class){
                  if(instance==null){
                    instance=new Lazy();
                }
              }

            return instance;
    }
}
## 懒汉式升级版

特点：在外部类被调用的时候内部类才会被加载，内部类一定是要在方法调用之前初始化，巧妙地避免了线程安全问题
只有优点：这种形式兼顾饿汉式的内存浪费，也兼顾synchronized性能问题
```
public class LazyThree {

    private boolean initialized = false;
    //默认使用LazyThree的时候，会先初始化内部类
    //如果没使用的话，内部类是不加载的
    private LazyThree(){
        synchronized (LazyThree.class){
            if(initialized == false){
                initialized = !initialized;
            }else{
                throw new RuntimeException("单例已被侵犯");
            }
        }
    }
    //每一个关键字都不是多余的
    //static 是为了使单例的空间共享
    //保证这个方法不会被重写，重载
    public static final LazyThree getInstance(){
        //在返回结果以前，一定会先加载内部类
        return LazyHolder.LAZY;
    }
    //默认不加载
    private static class LazyHolder{
        private static final LazyThree LAZY = new LazyThree();
    }
}


```
## 注册式

Spring中的做法，就是用这种注册式单例
```
public class RegisterMap {

    private RegisterMap(){}

    private static Map<String,Object> register = new ConcurrentHashMap<String,Object>();

    public static RegisterMap getInstance(String name){
        if(name == null){
            name = RegisterMap.class.getName();
        }

        if(register.get(name) == null){
            try {
                register.put(name, new RegisterMap());
            }catch(Exception e){
                e.printStackTrace();
            }

        }
        return (RegisterMap)register.get(name);
    }


}

```

## 枚举式

```
public enum Color {
    RED(){
       private int r = 255;
       private int g = 0;
       private int b = 0;

    },BLACK(){
        private int r = 0;
        private int g = 0;
        private int b = 0;
    },WHITE(){
        private int r = 255;
        private int g = 255;
        private int b = 255;
    };
}

```

## 序列化式

主要防止反序列化时导致单例破坏
```
//反序列化时导致单例破坏
public class Seriable implements Serializable {

    //序列化就是说把内存中的状态通过转换成字节码的形式
    //从而转换一个IO流，写入到其他地方(可以是磁盘、网络IO)
    //内存中状态给永久保存下来了

    //反序列化
    //讲已经持久化的字节码内容，转换为IO流
    //通过IO流的读取，进而将读取的内容转换为Java对象
    //在转换过程中会重新创建对象new
    public  final static Seriable INSTANCE = new Seriable();
    private Seriable(){}
    public static  Seriable getInstance(){
        return INSTANCE;
    }
    private  Object readResolve(){
        return  INSTANCE;
    }
}

```
序列化单例测试代码
```
public class SeriableTest {
    public static void main(String[] args) {
        Seriable s1 = null;
        Seriable s2 = Seriable.getInstance();

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("Seriable.obj");
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(s2);
            oos.flush();
            oos.close();


            FileInputStream fis = new FileInputStream("Seriable.obj");
            ObjectInputStream ois = new ObjectInputStream(fis);
            s1 = (Seriable)ois.readObject();
            ois.close();

            System.out.println(s1);
            System.out.println(s2);
            System.out.println(s1 == s2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```
