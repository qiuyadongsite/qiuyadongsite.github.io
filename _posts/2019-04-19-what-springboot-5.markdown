---
layout: post
title:  springmvc之handler method注解
date:   2019-04-19 21:52:12 +08:00
category: 微服务架构
tags: springmvc
comments: true
---

* content
{:toc}


绑定在方法上的参数注解详解：@PathVariable、@RequestHeader,@CookieValue、@RequestParam,@RequestBody、@SessionAttributes,@ModelAttribute
























## 简介

handler method参数绑定常用的注解,我们根据他们处理的Request的不同内容部分分为四类：（主要讲解常用类型）

A、处理requet uri 部分（这里指uri template中variable，不含queryString部分）的注解：`@PathVariable`;

B、处理request header部分的注解：` @RequestHeader`, `@CookieValue`;

C、处理request body部分的注解：`@RequestParam`, `@RequestBody`;

D、处理attribute类型是注解： `@SessionAttributes`, `@ModelAttribute`;

## `@PathVariable`

当使用@RequestMapping URI template样式映射时，即someUrl/{paramId}, 这时的paramId可通过 @Pathvariable注解绑定它传过来的值到方法的参数上。

示例代码：

```java

@Controller
@RequestMapping(“/owners/{ownerId}”)  
publicclass RelativePathUriTemplateController {  
@RequestMapping(“/pets/{petId}”)  
public void findPet(@PathVariable String ownerId, @PathVariable String petId, Model model) {      
// implementation omitted
  }  
}  

```

上面代码把URI template 中变量ownerId的值和petId的值，绑定到方法的参数上。若方法参数名称和需要绑定的uri template中变量名称不一致，需要在@PathVariable(“name”)指定uri template中的名称。

## `@RequestHeader`、`@CookieValue`

@RequestHeader注解，可以把Request请求header部分的值绑定到方法的参数上。

这是一个Request 的header部分：

```

Host                    localhost:8080  
Accept                  text/html,application/xhtml+xml,application/xml;q=0.9  
Accept-Language         fr,en-gb;q=0.7,en;q=0.3  
Accept-Encoding         gzip,deflate  
Accept-Charset          ISO-8859-1,utf-8;q=0.7,*;q=0.7  
Keep-Alive              300  

```

示例代码：

```java

@RequestMapping(“/displayHeaderInfo.do”)  
publicvoid displayHeaderInfo(@RequestHeader(“Accept-Encoding”) String encoding,  
@RequestHeader(“Keep-Alive”) long keepAlive)  {  
}

```

上面的代码，把request header部分的 Accept-Encoding的值，绑定到参数encoding上了， Keep-Alive header的值绑定到参数keepAlive上。
@CookieValue 可以把Request header中关于cookie的值绑定到方法的参数上。
例如有如下Cookie值：

```

JSESSIONID=415A4AC178C59DACE0B2C9CA727CDD84
```

参数绑定的代码：

```java

@RequestMapping(“/displayHeaderInfo.do”)  
publicvoid displayHeaderInfo(@CookieValue(“JSESSIONID”) String cookie)  {  
}  

```

即把JSESSIONID的值绑定到参数cookie上。

## `@RequestParam`, `@RequestBody`

`@RequestParam`
A） 常用来处理简单类型的绑定，通过Request.getParameter() 获取的String可直接转换为简单类型的情况（ String–> 简单类型的转换操作由ConversionService配置的转换器来完成）；因为使用request.getParameter()方式获取参数，所以可以处理get 方式中queryString的值，也可以处理post方式中 body data的值；

B）用来处理Content-Type: 为` application/x-www-form-urlencoded`编码的内容，提交方式GET、POST；

C) 该注解有两个属性： value、required； value用来指定要传入值的id名称，required用来指示参数是否必须绑定；
示例代码：

```java

@Controller
@RequestMapping(“/pets”)  
@SessionAttributes(“pet”)  
publicclass EditPetForm {  
@RequestMapping(method = RequestMethod.GET)  
public String setupForm(@RequestParam(“petId”) int petId, ModelMap model) {  
       Pet pet = this.clinic.loadPet(petId);  
   model.addAttribute(“pet”, pet);  
return“petForm”;  
   }

```

`@RequestBody`

该注解常用来处理Content-Type: 不是application/x-www-form-urlencoded编码的内容，例如application/json, application/xml等；

它是通过使用HandlerAdapter 配置的HttpMessageConverters来解析post data body，然后绑定到相应的bean上的。

因为配置有FormHttpMessageConverter，所以也可以用来处理 application/x-www-form-urlencoded的内容，处理完的结果放在一个MultiValueMap<String, String>里，这种情况在某些特殊需求下使用，详情查看`FormHttpMessageConverter api`;

示例代码：

```java

@RequestMapping(value = “/something”, method = RequestMethod.PUT)  
publicvoid handle(@RequestBody String body, Writer writer) throws IOException {  
  writer.write(body);  
}

```

## `@SessionAttributes`,` @ModelAttribute`

`@SessionAttributes`:

该注解用来绑定HttpSession中的attribute对象的值，便于在方法中的参数里使用。
该注解有value、types两个属性，可以通过名字和类型指定要使用的attribute 对象；
示例代码：

```java

@Controller
@RequestMapping(“/editPet.do”)  
@SessionAttributes(“pet”)  
public class EditPetForm {  
// …
}

```

`@ModelAttribute`


该注解有两个用法，一个是用于方法上，一个是用于参数上；

用于方法上时： 通常用来在处理`@RequestMapping`之前，为请求绑定需要从后台查询的model；

用于参数上时： 用来通过名称对应，把相应名称的值绑定到注解的参数bean上；要绑定的值来源于：

A） `@SessionAttributes` 启用的attribute 对象上；

B） `@ModelAttribute` 用于方法上时指定的model对象；

C） 上述两种情况都没有时，new一个需要绑定的bean对象，然后把request中按名称对应的方式把值绑定到bean中。
用到方法上`@ModelAttribute`的示例代码：


```java

@ModelAttribute
public Account addAccount(@RequestParam String number) {  
return accountManager.findAccount(number);  
}
```

这种方式实际的效果就是在调用`@RequestMapping`的方法之前，为request对象的model里put（“account”， Account）；
用在参数上的`@ModelAttribute`示例代码：

```java

@RequestMapping(value=“/owners/{ownerId}/pets/{petId}/edit”, method = RequestMethod.POST)  
public String processSubmit(@ModelAttribute Pet pet) {  
}

```

首先查询 `@SessionAttributes`有无绑定的Pet对象，若没有则查询`@ModelAttribute`方法层面上是否绑定了Pet对象，若没有则将URI template中的值按对应的名称绑定到Pet对象的各属性上。



`@ModelAttribute`注释void返回值的方法

```java
@Controller
public class HelloModelController {

    @ModelAttribute
    public void populateModel(@RequestParam String abc, Model model) {  
       model.addAttribute("attributeName", abc);  
    }  

    @RequestMapping(value = "/helloWorld")  
    public String helloWorld() {  
       return "helloWorld.jsp";  
    }  

}

```

在这个代码中，访问控制器方法helloWorld时，会首先调用populateModel方法，将页面参数abc(/helloWorld.ht?abc=text)放到model的attributeName属性中，在视图中可以直接访问。

jsp页面页面如下。

```

<%@ page language="java" contentType="text/html; charset=utf-8"
    pageEncoding="utf-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<html>
<head>
</head>
<body>
<c:out value="${attributeName}"></c:out>
</body>
</html>

```

`@ModelAttribute`注释返回具体类的方法

```java

@Controller
public class Hello2ModelController {

    @ModelAttribute
    public User populateModel() {  
       User user=new User();
       user.setAccount("ray");
       return user;
    }  
    @RequestMapping(value = "/helloWorld2")  
    public String helloWorld() {  
       return "helloWorld.jsp";  
    }  
}

```

当用户请求 http://localhost:8080/test/helloWorld2.ht时，首先访问populateModel方法，返回User对象，model属性的名称没有指定，

它由返回类型隐含表示，如这个方法返回User类型，那么这个model属性的名称是user。
这个例子中model属性名称有返回对象类型隐含表示，model属性对象就是方法的返回值。它无须要特定的参数。

jsp 中如下访问：

```

<c:out value="${user.account}"></c:out>

```

也可以指定属性名称

```java

@Controller
public class Hello2ModelController {

    @ModelAttribute(value="myUser")
    public User populateModel() {  
       User user=new User();
       user.setAccount("ray");
       return user;
    }  
    @RequestMapping(value = "/helloWorld2")  
    public String helloWorld(Model map) {  
       return "helloWorld.jsp";  
    }  
}

```
