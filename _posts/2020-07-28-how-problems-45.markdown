---
layout: post
title:  Spring Cloud Netflix-openfeign实践
date:   2020-07-28 20:53:12 +08:00
category: 源码学习
tags: sprincloud openfeign
comments: true
---

* content
{:toc}

天生我材比有用，千金散尽还复来！





通过接口模板形式的调用，简化调用流程，使用动态代理http发送请求，默认集成ribbon进行负载均衡；

### 简单使用

#### 配置调用接口

```java

    public interface OrderService {
        @GetMapping("/orders")
        String orders();
        @PostMapping("/order")
        int insert(OrderDto dto);
    }

    @FeignClient("order-service")
    public interface OrderServiceFeignClient extends OrderService{
    }

```

#### 客户端配置调用

```java

    //首先加入api依赖，然后扫描
    @EnableFeignClients(basePackages = "com.xx.xx.clients")
    //注入调用
    @Autowired
    OrderServiceFeignClient orderServiceFeignClient;
    @GetMapping("/test")
    public String test(){
       return orderServiceFeignClient.getAllOrder();
    }

```
 
#### 其他配置

```java

    # 配置指定服务的提供者的地址列表
    order-service.ribbon.listOfServers=\
      localhost:8080,localhost:8082
    #配置日志级别  
    logging.level.xx.clients.OrderServiceFeignClient=DEBUG
    # 日志输出配置
    # @Configuration
    # public class FeignLogConfig {
    #   @Bean
    #    Logger.Level feignLogger(){
    #        return Logger.Level.FULL;
    #   }
    # }

```

### 源码跟踪

#### 自动装配注解@EnableFeignClients

```java

    @Import({FeignClientsRegistrar.class})
    public @interface EnableFeignClients {
        String[] basePackages() default {};
    }

```

#### 客户端注册FeignClientsRegistrar

```java

    class FeignClientsRegistrar
        implements ImportBeanDefinitionRegistrar{
        public void registerBeanDefinitions(AnnotationMetadata metadata,
    			BeanDefinitionRegistry registry) {
            //注册默认的配置
    		registerDefaultConfiguration(metadata, registry);
            //具体的注册客户
    		registerFeignClients(metadata, registry);
    	}

        private void registerFeignClient(BeanDefinitionRegistry registry,
    			AnnotationMetadata annotationMetadata, Map<String, Object> attributes) {
    		String className = annotationMetadata.getClassName();
            //向spring容器注册FeignClientFactoryBean
    		BeanDefinitionBuilder definition = BeanDefinitionBuilder
    				.genericBeanDefinition(FeignClientFactoryBean.class);
        }
    }

```

#### 客户工厂类FeignClientFactoryBean

```java

    class FeignClientFactoryBean
        implements FactoryBean<Object>{
        <T> T getTarget() {
            //获取上下文
            FeignContext context = this.applicationContext.getBean(FeignContext.class);
    	    //根据上下文创建复杂对象的构建者
            Feign.Builder builder = feign(context);
            //负载均衡包装
            client = ((LoadBalancerFeignClient) client).getDelegate();
            builder.client(client);
            //动态代理客户端
            return (T) targeter.target(this, builder, context,
    				new HardCodedTarget<>(this.type, this.name, url));
        }
    }

```

#### 动态代理客户端ReflectiveFeign

```java

    //动态代理
    public <T> T newInstance(Target<T> target) {
        //服务节点封装器
         Map<String, MethodHandler> nameToHandler = targetToHandlersByName.apply(target);
        T proxy = (T) Proxy.newProxyInstance(target.type().getClassLoader(),
            new Class<?>[] {target.type()}, handler);
    }
    //具体代理对象
    static class FeignInvocationHandler implements InvocationHandler {
         public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
             //使用策略模式调用具体的服务节点
             return dispatch.get(method).invoke(args);
         }
    }

```

#### 请求解析并调用

```java

    class SynchronousMethodHandler implements MethodHandler {
        public Object invoke(Object[] argv){
            //构建请求模板
            RequestTemplate template = buildTemplateFromArgs.create(argv);
            //发出请求并解码返回的数据
            return executeAndDecode(template, options);
        }
        Object executeAndDecode(RequestTemplate template, Options options){
            //LoadBalancerFeignClient进行调用，并负载均衡
           response = client.execute(request, options);
        }
    }

```
