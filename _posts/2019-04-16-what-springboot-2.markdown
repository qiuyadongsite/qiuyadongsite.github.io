---
layout: post
title:  Thymeleaf
date:   2019-04-16 21:52:12 +08:00
category: 微服务架构
tags: Thymeleaf
comments: true
---

* content
{:toc}

Thymeleaf 视图技术、













## Thymeleaf

渲染上下文（模型） Model

- Spring Web MVC

  - 接口类型

    - Model

    - ModelMap

    - ModelAndView

      - Model  -> ModelMap

      - View

	- 注解类型

    - `@ModelAttrtibute`

    EL 表达式

- 字符值
- 多种数据类型
- 逻辑表达式
  - if else
- 迭代表达式
  - for each / while
- 反射
  - Java Reflection
  - CGLib

  模板寻址

  prefix + view-name + suffix

```

# Thymeleaf 配置
spring.thymeleaf.cache = false
spring.thymeleaf.prefix = classpath:/templates/thymeleaf/
spring.thymeleaf.suffix = .dota2

```

```html

<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Good Thymes Virtual Grocery</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
    <link rel="stylesheet" type="text/css" media="all"
          href="../../css/gtvg.css" th:href="@{/css/gtvg.css}" />
</head>
<body>
<p th:text="#{home.welcome}">Welcome to our grocery store!</p>
<p th:text="${!#strings.isEmpty(message)}">Message!</p>
<p th:text="${string.isNotBlank(message) ? message : 'No Content'}">Message!!!!!</p>
</body>
</html>

```

## Spring MVC 模板渲染逻辑

Spring MVC 核心总控制器 `DispatcherServlet`

C ：DispatcherServlet

M

- Spring MVC（部分）
  - Model / ModelMap / ModelAndView(Model 部分)
  - `@ModelAttribute`

-  模板引擎（通常）

- 通用的方式
  - 模板上下文
    - 内建/内嵌自己工具变量


   JSP 内置（ built-in ）九大变量

- Servlet Scope 上下文（Spring @Scope）
  - PageContext（page 变量）
    - 关注当前页面
    - A forward B
      - A 变量只能 A 页面使用，不能共享给 B
      - A t 和 B t 可以采用同名变量，同时使用
  - Servlet Request（请求上下文） - WebApplicationContext#SCOPE_REQUEST
    - 关注当前请求
      - A forward B
        - A 请求变量可以用于 B 页面

  - Servlet Session（会话上下文） - WebApplicationContext#SCOPE_SESSION
    - 关注当前会话
      - A forward / redirect B
        - A 请求变量可以用于 B 页面

  - Servlet ServletContext（应用上下文） - WebApplicationContext#SCOPE_APPLICATION
    - 关注当前应用
      - 用户 A 和 用户 B 会话可以共享

- JSP 内置变量( JSP = Servlet )

  - out（Writer = ServletResponse#getWriter()）

  - exception ( Throwable)

  - config( ServletConfig )

  - page ( Jsp Servlet 对象)

  - response（ServletResponse)

  Thymeleaf 内置变量

  StandardExpressionObjectFactory -> 构建 IExpressionContext

  - 上下文（模型）
  - strings
  - numbers
  - ...

  V：

- 视图对象
  - Servlet
    - RequestDispatcher#forward
    - RequestDispatcher#include
    - HttpServletResponse#sendRedirect
  - Spring MVC
    - View
      - forward:
        - InternalResourceView
      - redirect:
        - RedirectView
  - Struts
    - Action
      - ForwardAction
      - RedirectAction

      视图处理对象



      - Spring MVC
        - `*.do `-> DispatcherServlet -> Controller -> View -> ViewResolver -> View#render -> HTML -> HttpServletResponse
          - Thymeleaf
            - ViewResolver -> ThymeleafViewResolver
            - View -> ThymeleafView
              - 通过模板名称解析模板资源（ClassPathResource）
                - TemplateResolution
              - 读取资源，并且渲染内容 HTML
                - IEngineContext
                - ProcessorTemplateHandler
              - HTML 内容输出到 Response
            - 源码路径
              - org.thymeleaf.TemplateEngine#process(org.thymeleaf.TemplateSpec, org.thymeleaf.context.IContext, java.io.Writer)
              - org.thymeleaf.engine.TemplateManager#parseAndProcess
          - JSP
                <bean id="viewResolver" class="org.springframework.web.servlet.view.InternalResourceViewResolver">
                    <property name="viewClass" value="org.springframework.web.servlet.view.JstlView"/>
                    <property name="prefix" value="/WEB-INF/jsp/"/>
                    <property name="suffix" value=".jsp"/>
                </bean>
            - ViewResolver ->InternalResourceViewResolver
            - View -> JstlView
              - Foward -> RequestDispatcher


      - Struts
        - `*.do` -> ActionServlet -> Action -> ForwardAction -> RequestDispatcher -> JSP（Servlet） -> HTML -> HttpServletResponse

## 学习技巧

  学会配置代码

  假设需要了解的技术是 thymeleaf -> thymeleaf Properties -> ThymeleafProperties

  第一步：找到 @ConfigurationProperties，确认前缀

  ```java

  @ConfigurationProperties(prefix = "spring.thymeleaf")
public class ThymeleafProperties {

}

  ```

  比如：“spring.thymeleaf”



第二步：进一步确认，是否字段和属性名一一对应

```
spring.thymeleaf.cache
spring.thymeleaf.mode=HTML

```

```java

@ConfigurationProperties(prefix = "spring.thymeleaf")
public class ThymeleafProperties {
    ...
	private boolean cache = true;
    ...
    private String mode = "HTML";
    ...
}

```

配置项前缀 - spring.messages

学会打断点

DispatcherServlet#doDispatch  -> 拦截请求

Controller  -> 执行业务，并且控制视图

DispatcherServlet#resolveViewName -> 视图解析

DispatcherServlet#render -> 视图渲染

## 国际化

Locale

Spring MVC

- Locale
  - Servlet
    - ServletRequest#getLocale()
      - Accept-Language: en,zh;q=0.9,zh-TW;q=0.8
  - LocaleContextHolder
    - 来自于
      - DispatcherServlet
        - FrameworkServlet#initContextHolders
    - 存储是ThreadLocal

    Spring Boot

    - MessageSource
      - MessageSourceAutoConfiguration
        - MessageSourceProperties
          - message.properties
          - message_en.properties
          - message_zh.properties
          - message_zh_CN.properties
          - message_zh_TW.properties

hymeleaf

- key => messageSource.get

JSP -> JSP 模板 -> 翻译 Servlet Java 源文件 -> 编译 Servlet Class -> Servlet 加载 -> Servlet 执行（字节码执行）

Thymeleaf -> Thymeleaf 模板 -> 解释执行模板表达式（动态运行时）

很明显jsp速度更快，也很明显jsp适合对外项目
