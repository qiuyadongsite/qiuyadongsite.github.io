---
layout: post
title:  策略设计模式
date:   2018-12-27 22:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中行为类模式，策略设计模式。














## 策略模式（strategy）

应用场景：定义一系列选择项，把它们封装起来，实现一个借口，实现的结果是相同的。由用户去选择！
例如：支付方式、旅游路线的选择

定义一个公共借口

```java
/**
 * 支付渠道
 */
public interface Payment {

    public PayState pay(String uid, double amount);

}
```

各种实现方式：
第一种：

```java
1
public class AliPay implements Payment {

    @Override
    public PayState pay(String uid, double amount) {
        System.out.println("欢迎使用支付宝");
        System.out.println("查询账户余额，开始扣款");
        return new PayState(200,"支付成功",amount);
    }
}



```
第二种：

```java
2
public class JDPay implements Payment {

    @Override
    public PayState pay(String uid, double amount) {
        System.out.println("欢迎使用京东白条");
        System.out.println("查询账户余额，开始扣款");
        return new PayState(200,"支付成功",amount);
    }
}




```

第三种：

```java

3
public class WechatPay implements Payment {

    @Override
    public PayState pay(String uid, double amount) {
        System.out.println("欢迎使用微信支付");
        System.out.println("直接从微信红包扣款");
        return new PayState(200,"支付成功",amount);
    }
}




```

 姑且把这个枚举当做一个常量去维护

```java

public enum PayType {
    ALI_PAY(new AliPay()),
    WECHAT_PAY(new WechatPay()),
    UNION_PAY(new UnionPay()),
    JD_PAY(new JDPay());

    private Payment payment;
    PayType(Payment payment){
        this.payment = payment;
    }

    public Payment get(){ return  this.payment;}
}
```

返回状态

```java
public class PayState {
    private int code;
    private Object data;
    private String msg;

    public PayState(int code, String msg,Object data) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public String toString(){
        return ("支付状态：[" + code + "]," + msg + ",交易详情：" + data);
    }
}

```

订单类

```java

public class Order {
    private String uid;
    private String orderId;
    private double amount;

    public Order(String uid,String orderId,double amount){
        this.uid = uid;
        this.orderId = orderId;
        this.amount = amount;
    }


    //这个参数，完全可以用Payment这个接口来代替
    //为什么？

    //完美地解决了switch的过程，不需要在代码逻辑中写switch了
    //更不需要写if    else if
    public PayState pay(PayType payType){
        return payType.get().pay(this.uid,this.amount);
    }

}


```
测试

```java

public class PayStrategyTest {

    public static void main(String[] args) {

        //省略把商品添加到购物车，再从购物车下单
        //直接从点单开始
        Order order = new Order("1","20180311001000009",324.45);

        //开始支付，选择微信支付、支付宝、银联卡、京东白条、财付通
        //每个渠道它支付的具体算法是不一样的
        //基本算法固定的

        //这个值是在支付的时候才决定用哪个值
        System.out.println(order.pay(PayType.WECHAT_PAY));

        //BeanFactory

        //根据url去自动选择
        //爬取百度的数据     BaiduParser
        //                 SinaParser
        //                 SougouParser
        //返回一个解析好的Json格式，统一好了，保存入库


    }

}

```
