---
layout: post
title:  装饰者设计模式
date:   2019-01-10 22:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中行为类模式，装饰者设计模式。














## 装饰者模式（decorator）,非常特殊的适配器模式

应用场景：为了某个实现类在不修改原始类的基础上进行动态的覆盖或者增加方法，该实现保持着跟原有类的层级关系。

它实际上是一种非常特殊的适配器模式

登录接口

```java

public interface ISigninService {
    public ResultMsg regist(String username,String password);


    /**
     * 登录的方法
     * @param username
     * @param password
     * @return
     */
    public ResultMsg login(String username,String password);
}

```

登录基础实现类

```java

public class SigninService implements ISigninService {

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
扩展第三方实现接口

```java

public interface ISigninForThirdService extends ISigninService {


    public ResultMsg loginForQQ(String openId);

    public ResultMsg loginForWechat(String openId);

    public ResultMsg loginForToken(String token);

    public ResultMsg loginForTelphone(String telphone,String code);

    public ResultMsg loginForRegist(String username,String password);


}

```

第三方登录实现类，

```java

public class SigninForThirdService implements ISigninForThirdService {

    private ISigninService service;
    public SigninForThirdService(ISigninService service){
        this.service = service;
    }

    @Override
    public ResultMsg regist(String username, String password) {
        return service.regist(username,password);
    }

    @Override
    public ResultMsg login(String username, String password) {
        return service.login(username,password);
    }


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
        this.regist(username,null);
        return this.login(username,null);
    }

}


```
测试

```java

public class SigginTest {


    public static void main(String[] args) {

        //原来的功能依旧对外开放，依旧保留
        //新的功能同样的也可以使用

        ISigninForThirdService signinForThirdService = new SigninForThirdService(new SigninService());

        signinForThirdService.loginForQQ("xxssdsd");


//        Decorator
//        Wrapper


        /*

            ===============================================================------
            装饰器模式                          |   适配器模式
            -----------------------------------+---------------------------------
            是一种非常特别的适配器模式            |  可以不保留层级关系
            -----------------------------------+---------------------------------
            装饰者和被装饰者都要实现同一个接口     |  适配者和被适配者没有必然的层级联系
            主要目的是为了扩展，依旧保留OOP关系    |  通常采用代理或者继承形式进行包装
            -----------------------------------+----------------------------------
            满足is-a的关系                      |   满足has-a
            -----------------------------------+----------------------------------
            注重的是覆盖、扩展                   |   注重兼容、转换
            -----------------------------------+----------------------------------


        */


    }

}

```
