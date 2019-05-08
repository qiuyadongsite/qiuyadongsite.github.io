---
layout: post
title:  springmvc内部类
date:   2019-04-18 21:52:12 +08:00
category: 微服务架构
tags: springmvc
comments: true
---

* content
{:toc}


重新认识springmvc












## web 应用启动过程

首先web应用是部署在web容器中的，web容器提供一个全局的上下文环境，叫做ServletContext，这个对象也是web容器创建的，这个对象就是为spring ioc容器提供宿主环境的

ServletContext是一个Web应用的全局上下文，可以理解为整个Web应用的全局变量，项目中的所有方法皆可以获取ServletContext。

就到说到所有web项目的web.xml，下面我们先贴出web.xml的一部分配置：

```xml

<context-param>
    <param-name>contextConfigLocation</param-name>
    <param-value>classpath:applicationContext.xml</param-value>
  </context-param>
  <context-param>
    <param-name>log4jConfigLocation</param-name>
    <param-value>classpath:log4j.properties</param-value>
  </context-param>
  <listener>
    <listener-class>org.springframework.web.util.IntrospectorCleanupListener</listener-class>
  </listener>
  <listener>
    <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
  </listener>
  <listener>
    <listener-class>listener.SessionListener</listener-class>
  </listener>
  <filter>
    <filter-name>encodingFilter</filter-name>
    <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
    <init-param>
      <param-name>encoding</param-name>
      <param-value>UTF-8</param-value>
    </init-param>
    <init-param>
      <param-name>forceEncoding</param-name>
      <param-value>true</param-value>
    </init-param>
  </filter>
  <filter-mapping>
    <filter-name>encodingFilter</filter-name>
    <url-pattern>*.do</url-pattern>
  </filter-mapping>
  <filter>
    <filter-name>sessionFilter</filter-name>
    <filter-class>web.filter.SessionFilter</filter-class>
  </filter>
  <filter-mapping>
    <filter-name>sessionFilter</filter-name>
    <url-pattern>*.do</url-pattern>
  </filter-mapping>
  <servlet>
    <servlet-name>springmvc</servlet-name>
    <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
    <load-on-startup>1</load-on-startup>
  </servlet>
  <servlet-mapping>
    <servlet-name>springmvc</servlet-name>
    <url-pattern>*.do</url-pattern>
  </servlet-mapping>

```

启动Tomcat之后，首先会加载web.xml文件：

　　a)容器首先读取web.xml中的<context-param>的配置内容和<listener>标签中配置项；

　　b)紧接着实例化ServletContext对象，并将<context-param>配置的内容转化为键值传递给ServletContext；

　　c)创建<listener>配置的监听器的类实例，并且启动监听；

　　d)随后调用listener的contextInitialized(ServletContextEvent args)方法，ServletContext = ServletContextEvent.getServletContext();

　　　　　　　此时你可以通过ServletContext获取context-param配置的内容并可以加以修改，此时Tomcat还没完全启动完成。

　　e)后续加载配置的各类filter；

　　f)最后加载servlet；

  最后的结论是：web.xml中配置项的加载顺序是context-param=>listener=>filter=>servlet,配置项的顺序并不会改变加载顺序，但是同类型的配置项会应该加载顺序，servlet中也可以通过load-on-startup来指定加载顺序。

  ServletContext中的属性所有的servlet皆可以使用ServletContext.

  `ApplicationContext`

  首先介绍下applicationContext，applicationContext是spring的BeanFactory的实现类：

  ApplicationContext接口的继承关系如上面的截图，ApplicationContext是如何产生的呢，这里我们看之前的web.xml中的

```xml

<listener>
   <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
 </listener>
```

我们看看是如何初始化的

```java

public WebApplicationContext initWebApplicationContext(ServletContext servletContext) {
        if (servletContext.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE) != null) {
            throw new IllegalStateException(
                    "Cannot initialize context because there is already a root application context present - " +
                    "check whether you have multiple ContextLoader* definitions in your web.xml!");
        }

        Log logger = LogFactory.getLog(ContextLoader.class);
        servletContext.log("Initializing Spring root WebApplicationContext");
        if (logger.isInfoEnabled()) {
            logger.info("Root WebApplicationContext: initialization started");
        }
        long startTime = System.currentTimeMillis();

        try {
            // Store context in local instance variable, to guarantee that
            // it is available on ServletContext shutdown.
            if (this.context == null) {
                this.context = createWebApplicationContext(servletContext);
            }
            if (this.context instanceof ConfigurableWebApplicationContext) {
                ConfigurableWebApplicationContext cwac = (ConfigurableWebApplicationContext) this.context;
                if (!cwac.isActive()) {
                    // The context has not yet been refreshed -> provide services such as
                    // setting the parent context, setting the application context id, etc
                    if (cwac.getParent() == null) {
                        // The context instance was injected without an explicit parent ->
                        // determine parent for root web application context, if any.
                        ApplicationContext parent = loadParentContext(servletContext);
                        cwac.setParent(parent);
                    }
                    configureAndRefreshWebApplicationContext(cwac, servletContext);
                }
            }
            servletContext.setAttribute(c, this.context);

            ClassLoader ccl = Thread.currentThread().getContextClassLoader();
            if (ccl == ContextLoader.class.getClassLoader()) {
                currentContext = this.context;
            }
            else if (ccl != null) {
                currentContextPerThread.put(ccl, this.context);
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Published root WebApplicationContext as ServletContext attribute with name [" +
                        WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE + "]");
            }
            if (logger.isInfoEnabled()) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                logger.info("Root WebApplicationContext: initialization completed in " + elapsedTime + " ms");
            }

            return this.context;
        }
        catch (RuntimeException ex) {
            logger.error("Context initialization failed", ex);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, ex);
            throw ex;
        }
        catch (Error err) {
            logger.error("Context initialization failed", err);
            servletContext.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE, err);
            throw err;
        }
    }

```

代码中加粗的部分就是讲WebApplicationContext以WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE为key保存到ServletContext中，所以我们在需要获取时，可以根据request.getSession().

　　getAttribute("WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE")来获取WebApplicationContext.

　　所以WebApplicationContext依赖于ServletContext,ApplicationContext存储了Spring中所有的Bean，

　　但是我们常规的Springmvc项目一般除了applicationContext.xml之外还有springmvc.xml，两个配置文件会对应两个ApplicationContext，springmvc的ApplicationContext中可以调用applicationContext.xml的ApplciationContext。

　　3.获取WebApplication的几种方式

　　　　a)request.getSession().getServletContext().getAttribute("org.springframework.web.context.WebApplicationContext.ROOT")

　　　　b)实现ApplicationContextAware接口

```java

public interface ApplicationContextAware {  

         void setApplicationContext(ApplicationContext applicationContext) throws BeansException;  

}  

```

## HandlerMapping

关系梳理

spring ioc 是spring的核心，用来管理spring bean的生命周期

MVC 是一种使用 MVC（Model View Controller 模型-视图-控制器）设计创建 Web 应用程序的模式

spring mvc 是spring的一个独立的模块，就像AOP一样

在spring mvc中把web框架和spring ioc融合在一起，是通过ContextLoaderListener监听servlet上下文的创建后来加载父容器完成的，然后通过配置一个servlet对象DispatcherServlet：

RequestMappingHandlerMapping也是在DispatcherServlet的初始化过程中自动加载的，默认会自动加载所有实现HandlerMapping接口的bean，且我们可以通过serOrder来设置优先级，系统默认会加载RequestMappingHandlerMapping、BeanNameUrlHandlerMapping、SimpleUrlHandlerMapping 并且按照顺序使用

```java

private void initHandlerMappings(ApplicationContext context) {
        this.handlerMappings = null;

        if (this.detectAllHandlerMappings) {
            // Find all HandlerMappings in the ApplicationContext, including ancestor contexts.
            Map<String, HandlerMapping> matchingBeans =
                    BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerMapping.class, true, false);
            if (!matchingBeans.isEmpty()) {
                this.handlerMappings = new ArrayList<>(matchingBeans.values());
                // We keep HandlerMappings in sorted order.
                AnnotationAwareOrderComparator.sort(this.handlerMappings);
            }
        }
}

```

RequestMappingHandlerMapping 加载过程

RequestMappingHandlerMapping 实现了接口InitializingBean，在bean加载完成后会自动调用afterPropertiesSet方法，在此方法中调用了initHandlerMethods()来实现初始

遍历所有bean，如果bean实现带有注解@Controller或者@RequestMapping 则进一步调用detectHandlerMethods处理，处理逻辑大致就是根据@RequestMapping配置的信息，构建RequestMappingInfo，然后注册到MappingRegistry中

```java

protected void initHandlerMethods() {

        String[] beanNames = (this.detectHandlerMethodsInAncestorContexts ?
                BeanFactoryUtils.beanNamesForTypeIncludingAncestors(obtainApplicationContext(), Object.class) :
                obtainApplicationContext().getBeanNamesForType(Object.class));

        for (String beanName : beanNames) {
            if (!beanName.startsWith(SCOPED_TARGET_NAME_PREFIX)) {
                Class<?> beanType = null;
                beanType = obtainApplicationContext().getType(beanName);
                if (beanType != null && isHandler(beanType)) {
                    detectHandlerMethods(beanName);
                }
            }
        }
        handlerMethodsInitialized(getHandlerMethods());
    }

```

```java

protected void detectHandlerMethods(final Object handler) {
        Class<?> handlerType = (handler instanceof String ?
                obtainApplicationContext().getType((String) handler) : handler.getClass());

        if (handlerType != null) {
            final Class<?> userType = ClassUtils.getUserClass(handlerType);
            Map<Method, T> methods = MethodIntrospector.selectMethods(userType,
                    (MethodIntrospector.MetadataLookup<T>) method -> {
                        try {
                            return getMappingForMethod(method, userType);
                        }
                        catch (Throwable ex) {
                            throw new IllegalStateException("Invalid mapping on handler class [" +
                                    userType.getName() + "]: " + method, ex);
                        }
                    });
            methods.forEach((method, mapping) -> {
                Method invocableMethod = AopUtils.selectInvocableMethod(method, userType);
                registerHandlerMethod(handler, invocableMethod, mapping);
            });
        }
    }

```

RequestMappingHandlerMapping 解析过程

在DispatcherServlet中，根据请求对象调用getHander方法获取HandlerExecutionChain对象

在getHander方法中也是遍历上面默认加载的三个HandlerMapping，当然第一个就是RequestMappingHandlerMapping对象，调用其getHandler方法,根据请求path，找到一个最为匹配的HandlerMethod来处理请求

```java

protected HandlerExecutionChain getHandler(HttpServletRequest request) throws Exception {
        if (this.handlerMappings != null) {
            for (HandlerMapping hm : this.handlerMappings) {
                if (logger.isTraceEnabled()) {
                    logger.trace(
                            "Testing handler map [" + hm + "] in DispatcherServlet with name '" + getServletName() + "'");
                }
                HandlerExecutionChain handler = hm.getHandler(request);
                if (handler != null) {
                    return handler;
                }
            }
        }
        return null;
    }

```

根据请求路径获取HandlerInterceptor，然后和上面获得的HandlerMethod一起构成HandlerExecutionChain返回给DispatcherServlet

DispatcherServlet得到HandlerExecutionChain也就获得了处理此次请求所需的Handler【即我们熟悉的Controller和对应的Action】，后续将会选择合适HandlerAdapter来执行对应的Handler，获取返回值，再根据返回值类型，进一步觉决定用什么方式展示给用户


## HandlerAdapter

HandlerAdapter从字面意思就是处理适配器,也就是handler的适配器模式

DispatcherServlet 通过适配器来执行具体的Handler，开发者也是通过适配器来扩展Handler

源码如下

```java

public interface HandlerAdapter {

	/**
	 * Given a handler instance, return whether or not this {@code HandlerAdapter}
	 * can support it. Typical HandlerAdapters will base the decision on the handler
	 * type. HandlerAdapters will usually only support one handler type each.
	 * <p>A typical implementation:
	 * <p>{@code
	 * return (handler instanceof MyHandler);
	 * }
	 * @param handler handler object to check
	 * @return whether or not this object can use the given handler
	 */
	boolean supports(Object handler);

	/**
	 * Use the given handler to handle this request.
	 * The workflow that is required may vary widely.
	 * @param request current HTTP request
	 * @param response current HTTP response
	 * @param handler handler to use. This object must have previously been passed
	 * to the {@code supports} method of this interface, which must have
	 * returned {@code true}.
	 * @throws Exception in case of errors
	 * @return ModelAndView object with the name of the view and the required
	 * model data, or {@code null} if the request has been handled directly
	 */
	ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

	/**
	 * Same contract as for HttpServlet's {@code getLastModified} method.
	 * Can simply return -1 if there's no support in the handler class.
	 * @param request current HTTP request
	 * @param handler handler to use
	 * @return the lastModified value for the given handler
	 * @see javax.servlet.http.HttpServlet#getLastModified
	 * @see org.springframework.web.servlet.mvc.LastModified#getLastModified
	 */
	long getLastModified(HttpServletRequest request, Object handler);

}


```

针对不同类型handler，需要不同的HandlerAdapter来适配，然后DispatchServlet会根据已经加载的HandlerAdapter集合，选择最适合当前Hander【Handler是根据当前请求path从HandlerMapping中获取的】的HandlerAdapter，然后调用ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)方法，获取ModelAndView对象

 讲到的SimplController这种类型的Handler，就用SimpleControllerHandlerAdapter这种适配器来针对这种Handler来做适配，如下可以看出是先根据supports方法来判断是否支持某种类型的Handler，SimpleControllerHanderAdapter就仅仅支持实现了Controller接口的Handler然后调用Hander方法，其实就是调用Controller的handleRequest方法直接处理请求，返回ModelAndView对象

```java

public class SimpleControllerHandlerAdapter implements HandlerAdapter {

    @Override
    public boolean supports(Object handler) {
        return (handler instanceof Controller);
    }

    @Override
    @Nullable
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        return ((Controller) handler).handleRequest(request, response);
    }

    @Override
    public long getLastModified(HttpServletRequest request, Object handler) {
        if (handler instanceof LastModified) {
            return ((LastModified) handler).getLastModified(request);
        }
        return -1L;
    }

}


```

如何做

DispatchServlet 首先是初始化HandlerAdapt，在Servlet的init方法中加载，其实就是从spring上下文中收集出所有的HandlerAdapter类型的bean，然后排下序就可以了

```java

private void initHandlerAdapters(ApplicationContext context) {
       this.handlerAdapters = null;

       if (this.detectAllHandlerAdapters) {
           // Find all HandlerAdapters in the ApplicationContext, including ancestor contexts.
           Map<String, HandlerAdapter> matchingBeans =
                   BeanFactoryUtils.beansOfTypeIncludingAncestors(context, HandlerAdapter.class, true, false);
           if (!matchingBeans.isEmpty()) {
               this.handlerAdapters = new ArrayList<>(matchingBeans.values());
               // We keep HandlerAdapters in sorted order.
               AnnotationAwareOrderComparator.sort(this.handlerAdapters);
           }
       }
       else {
           try {
               HandlerAdapter ha = context.getBean(HANDLER_ADAPTER_BEAN_NAME, HandlerAdapter.class);
               this.handlerAdapters = Collections.singletonList(ha);
           }
           catch (NoSuchBeanDefinitionException ex) {
               // Ignore, we'll add a default HandlerAdapter later.
           }
       }
   }

```

初始化完成之后，当一个请求进来之后，首先会根据HandlerMapping获取对应的HandlerExecutionChain,然后根据其中的Handler类型找到适合的HandlerAdapt，最后开始执行Handler，然后返回ModelAndView对象

```java

protected HandlerAdapter getHandlerAdapter(Object handler) throws ServletException {
        if (this.handlerAdapters != null) {
            for (HandlerAdapter ha : this.handlerAdapters) {
                if (logger.isTraceEnabled()) {
                    logger.trace("Testing handler adapter [" + ha + "]");
                }
                if (ha.supports(handler)) {
                    return ha;
                }
            }
        }
    }

```

## springMVC 拦截器

  从名称可以看出，主要是拦截Handler，在一个请求进来后，开始执行Handler之前，执行handler之后，handler执行完成后【无论成功与否】，执行一些操作

  应用场景

1、日志记录，可以记录请求信息的日志，以便进行信息监控、信息统计等。

2、权限检查，如登陆检测，进入处理器检测是否登陆，如果没有直接返回到登陆页

HandlerInterceptor 定义

```java

public interface HandlerInterceptor {

    /**
     * 预处理回调方法，实现处理器的预处理，第三个参数为响应的处理器，自定义Controller
     * 返回值：true表示继续流程（如调用下一个拦截器或处理器）；false表示流程中断
     * 不会继续调用其他的拦截器或处理器，此时我们需要通过response来产生响应
     */
    default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        return true;
    }

    /**
     * 后处理回调方法，实现处理器的后处理（渲染视图之前）
     * 此时我们可以通过modelAndView（模型和视图对象）对模型数据进行处理或对视图进行处理
     * modelAndView也可能为null，如API接口返回JSON数据时
     */
    default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable ModelAndView modelAndView) throws Exception {
    }

    /**
     * 整个请求处理完毕回调方法，即在视图渲染完毕时回调
     * 如性能监控中我们可以在此记录结束时间并输出消耗时间
     * 还可以进行一些资源清理，类似于try-catch-finally中的finally，但仅调用处理器执行链中
     */
    default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
            @Nullable Exception ex) throws Exception {
    }

}

```

拦截器初始化

spring mvc中一般是通过 mvc:interceptors 个标签来配置拦截器，其中配置<mvc:interceptor>最终会被解析成MappedInterceptor

```xml

<mvc:mapping path="/**"/>  会被解析成MappedInterceptor的includePatterns属性；
<mvc:exclude-mapping path="/**"/> //会被解析成MappedInterceptor的excludePatterns属性；
<bean/>会被解析成MappedInterceptor的interceptor属性

//这个标签是被InterceptorsBeanDefinitionParser类解析，可以自行查看源码

  <mvc:interceptors>
       <mvc:interceptor>
           <mvc:mapping path="/**"/>
           <bean class="com.seven.springmvc.interceptor.SyslogInterceptor" />
       </mvc:interceptor>

   </mvc:interceptors>

```

interceptor的初始化是在初始化HandlerMapping时完成的，HandlerMapping多数是通过继承AbstractHandlerMapping实现的，
因为AbstractHandlerMapping实现ApplicationContextAware接口，bean初始化完成后会执行setApplicationContext方法，通过源码看到最终执行如下方法

```java

protected void initApplicationContext() throws BeansException {
        extendInterceptors(this.interceptors);//用于扩展拦截器
        detectMappedInterceptors(this.adaptedInterceptors);//直接加载spring 容器中所有实现了MappedInterceptor接口的拦截器
        initInterceptors();//则是把extendInterceptors中的扩展方法加入到adaptedInterceptors集合中
    }

```

```java

protected void detectMappedInterceptors(List<HandlerInterceptor> mappedInterceptors) {
        mappedInterceptors.addAll(
                BeanFactoryUtils.beansOfTypeIncludingAncestors(
                        obtainApplicationContext(), MappedInterceptor.class, true, false).values());
    }

```

detectMappedInterceptors方法就是从spring容器中遍历所有实现HandlerInterceptor接口的bean，并将其包装成MappedInterceptor，并赋值给AbstractHandlerMapping的adaptedInterceptors属性至此每个HandlerMapping加载完成时已经完成了对应的Interceptor的加载工作

### 拦截器实现原理

请求进来后，最终执行DispatcherServlet的doDispatch方法开始处理请求，如下

```java

protected void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HttpServletRequest processedRequest = request;
        HandlerExecutionChain mappedHandler = null;
        boolean multipartRequestParsed = false;

        WebAsyncManager asyncManager = WebAsyncUtils.getAsyncManager(request);

        try {
            ModelAndView mv = null;
            Exception dispatchException = null;

            try {
                processedRequest = checkMultipart(request);
                multipartRequestParsed = (processedRequest != request);

                // 此处开始遍历HandlerMapping，获取HandlerExecutionChain，这里方法执行完后，针对此次请求，已经确定有几个拦截器可以被执行了
                mappedHandler = getHandler(processedRequest);

                HandlerAdapter ha = getHandlerAdapter(mappedHandler.getHandler());

                //开始这行preHandler的预处理方法，会遍历HandlerExecutionChain 中的所有拦截器，并执行器preHandler方法，如果有方法返回false，则直接返回，不在继续执行
                if (!mappedHandler.applyPreHandle(processedRequest, response)) {
                    return;
                }

                // 开始真正执行Handler方法
                mv = ha.handle(processedRequest, response, mappedHandler.getHandler());



                applyDefaultViewName(processedRequest, mv);
//具体Controler中的方法执行完成后开始执行拦截器的postHandler方法，注意此时是和preHandler的执行顺序是相反的
                mappedHandler.applyPostHandle(processedRequest, response, mv);
            }
            //开始遍历并执行拦截器的AfterCompletion方法，注意此时是根据handlerIndex反向执行的
            processDispatchResult(processedRequest, response, mappedHandler, mv, dispatchException);
        }
        catch (Exception ex) {
            triggerAfterCompletion(processedRequest, response, mappedHandler, ex);
        }
        catch (Throwable err) {
            triggerAfterCompletion(processedRequest, response, mappedHandler,
                    new NestedServletException("Handler processing failed", err));
        }
        finally {
            if (asyncManager.isConcurrentHandlingStarted()) {
                // Instead of postHandle and afterCompletion
                if (mappedHandler != null) {
                    mappedHandler.applyAfterConcurrentHandlingStarted(processedRequest, response);
                }
            }
            else {
                // Clean up any resources used by a multipart request.
                if (multipartRequestParsed) {
                    cleanupMultipart(processedRequest);
                }
            }
        }
    }

```

AbstractHandlerMapping中获取HandlerExecutionChain的过程如下，请求进来后，在HandlerMapping中会根据具体的请求path来选择合适的拦截器，因为在初始化HandlerMapping的时候，已经把所有的HandlerInterceptor全部加载完成了

```java

protected HandlerExecutionChain getHandlerExecutionChain(Object handler, HttpServletRequest request) {
        HandlerExecutionChain chain = (handler instanceof HandlerExecutionChain ?
                (HandlerExecutionChain) handler : new HandlerExecutionChain(handler));

        String lookupPath = this.urlPathHelper.getLookupPathForRequest(request);
        for (HandlerInterceptor interceptor : this.adaptedInterceptors) {
            if (interceptor instanceof MappedInterceptor) {
                MappedInterceptor mappedInterceptor = (MappedInterceptor) interceptor;
                if (mappedInterceptor.matches(lookupPath, this.pathMatcher)) {
                    chain.addInterceptor(mappedInterceptor.getInterceptor());
                }
            }
            else {
                chain.addInterceptor(interceptor);
            }
        }
        return chain;
    }


```

拦截器和Filter的区别

Filter是web容器定义的，由servlet容器来加载并执行，基本可以拦截所有请求，interceptor是spring mvc这种web框架定义的，用于在handler执行前后执行的，仅针对handler进行拦截，且是spring 来加载并执行的
针对执行顺序，自然是filter先被执行，然后是拦截器的执行，拦截器是按照顺序执行prehandler，然后按照相反的顺序执行postHandler的，且不论方法执行成功与否，会按照相反的和preHander相反的顺序执行afterCompletion【仅执行已经执行过preHander方法的拦截器】
