---
layout: post
title:  适配器设计模式
date:   2019-01-10 20:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中行为类模式，适配器设计模式。














## 适配器模式（adapter）(目的是兼容)

应用场景：老系统运行比较稳定，为了保持其稳定性，不便去再修改原来的代码。但是又要为了兼容新的需求或标准，我们不得不在系统去做一些文章。（向下兼容）

如：我懒的注册，只想用QQ、微信、微博等登录；


登录类（通用的稳定方法）

```java

public class SiginService {

    /**
     * 注册方法
     * @param username
     * @param password
     * @return
     */
    public ResultMsg regist(String username,String password){
        return  new ResultMsg(200,"注册成功",new Member());
    }


    /**
     * 登录的方法
     * @param username
     * @param password
     * @return
     */
    public ResultMsg login(String username,String password){
        return null;
    }

}


```
扩展的登录类

```java
/**
 *
 * 稳定的方法不去动，直接继承下来
 * Created by Tom on 2018/3/14.
 */
public class SiginForThirdService extends SiginService {

    public ResultMsg loginForQQ(String openId){
        //1、openId是全局唯一，我们可以把它当做是一个用户名(加长)
        //2、密码默认为QQ_EMPTY
        //3、注册（在原有系统里面创建一个用户）

        //4、调用原来的登录方法

        return loginForRegist(openId,null);
    }

    public ResultMsg loginForWechat(String openId){
        return null;
    }

    public ResultMsg loginForToken(String token){
        //通过token拿到用户信息，然后再重新登陆了一次
        return  null;
    }

    public ResultMsg loginForTelphone(String telphone,String code){

        return null;
    }

    public ResultMsg loginForRegist(String username,String password){
        super.regist(username,null);
        return super.login(username,null);
    }







}

```

测试

```java
public class SiginForThirdServiceTest {

    public static void main(String[] args) {

        SiginForThirdService service = new SiginForThirdService();

        //不改变原来的代码，也要能够兼容新的需求
        //还可以再加一层策略模式
        service.loginForQQ("sdfgdgfwresdf9123sdf");


    }



```
