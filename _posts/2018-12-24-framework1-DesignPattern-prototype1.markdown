---
layout: post
title:  原型设计模式
date:   2018-12-24 21:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中创建类模式，原型设计模式包含克隆、深度克隆。














## 克隆模式

原型类继承cloneable，目标类为其属性或者list添加进去

在浅克隆中，被复制对象的所有普通成员变量都具有与原来对象相同的值，而所有的对其他对象的引用仍然指向原来的对象。也就是说，浅克隆仅仅复制所考虑的对象，不会复制它所引用的成员对象。

```java

public class Prototype implements Cloneable {

    public String name;

    CloneTarget target = null;
}
```

目标类重写clone

```java

public class CloneTarget extends Prototype {

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

}

```
测试

```java

public class CloneTest {

    public static void main(String[] args) {

        CloneTarget p = new CloneTarget();
        p.name = "Tom";
        p.target = new CloneTarget();
        System.out.println(p.target);

        try {
            CloneTarget obj =  (CloneTarget) p.clone();
            System.out.println(obj.target);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

```

## 深度克隆

在深克隆中被复制的对象的所有普通成员变量也都含有与原来的对象相同的值，出去那些引用其他对象的变量。换言之，在深克隆中，除了对象本身被复制外，对象包含的引用也被复制，也就是其中的成员对象也被复制。

猴子类

```java

public class Monkey {
    public int height;
    public int weight;
    public Date birthday;

}

```
金箍棒类

```java
public class JinGuBang implements Serializable {
    public float h = 100;
    public float d = 10;

    public void big(){
        this.d *= 2;
        this.h *= 2;
    }

    public void small(){
        this.d /= 2;
        this.h /= 2;
    }


}

```
齐天大圣类

```java
public class QiTianDaSheng extends Monkey implements Cloneable,Serializable {

    public JinGuBang jinGuBang;

    public  QiTianDaSheng(){
        //只是初始化
        this.birthday = new Date();
        this.jinGuBang = new JinGuBang();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return this.deepClone();
    }


    public Object deepClone(){
        try{

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(this);

            ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(bis);

            QiTianDaSheng copy = (QiTianDaSheng)ois.readObject();
            copy.birthday = new Date();
            return copy;

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }

    }


    public QiTianDaSheng copy(QiTianDaSheng target){

        QiTianDaSheng qiTianDaSheng = new QiTianDaSheng();
        qiTianDaSheng.height = target.height;
        qiTianDaSheng.weight = target.height;

        qiTianDaSheng.jinGuBang = new JinGuBang();
        qiTianDaSheng.jinGuBang.h = target.jinGuBang.h;
        qiTianDaSheng.jinGuBang.d = target.jinGuBang.d;

        qiTianDaSheng.birthday = new Date();
        return  qiTianDaSheng;
    }
}

```
测试

```java

public class Main {

    public static void main(String[] args) {

        QiTianDaSheng q = new QiTianDaSheng();
        QiTianDaSheng n = q.copy(q);
        System.out.println(q.jinGuBang == n.jinGuBang);

    }
}

```
