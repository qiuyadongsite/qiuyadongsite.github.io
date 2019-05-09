---
layout: post
title:  springcloud之方法熔断
date:   2019-04-20 21:52:12 +08:00
category: 微服务架构
tags: springmvc
comments: true
---

* content
{:toc}


springcloud服务熔断其实就是处理请求超时的方法。























## 低级版本（无容错实现）

实现服务熔断（Future）

```java

private final ExecutorService executorService = Executors.newSingleThreadExecutor();

   /**
    * 简易版本
    *
    * @param message
    * @return
    * @throws InterruptedException
    */
   @GetMapping("/say2")
   public String say2(@RequestParam String message) throws Exception {
       Future<String> future = executorService.submit(() -> {
           return doSay2(message);
       });
       // 100 毫秒超时
       String returnValue = future.get(100, TimeUnit.MILLISECONDS);
       return returnValue;
   }

```

## 低级版本+（带容错实现）

```java

private final ExecutorService executorService = Executors.newSingleThreadExecutor();

   /**
    * 简易版本
    *
    * @param message
    * @return
    * @throws InterruptedException
    */
   @GetMapping("/say2")
   public String say2(@RequestParam String message) throws Exception {
       Future<String> future = executorService.submit(() -> {
           return doSay2(message);
       });
       // 100 毫秒超时
       String returnValue = null;
       try {
           returnValue = future.get(100, TimeUnit.MILLISECONDS);
       } catch (InterruptedException | ExecutionException | TimeoutException e) {
           // 超级容错 = 执行错误 或 超时
           returnValue = errorContent(message);
       }
       return returnValue;
   }

```

## 中级版本

```java

/**
    * 中级版本
    *
    * @param message
    * @return
    * @throws InterruptedException
    */
   @GetMapping("/middle/say")
   public String middleSay(@RequestParam String message) throws Exception {
       Future<String> future = executorService.submit(() -> {
           return doSay2(message);
       });
       // 100 毫秒超时
       String returnValue = null;

       try {
           returnValue = future.get(100, TimeUnit.MILLISECONDS);
       } catch (TimeoutException e) {
           future.cancel(true); // 取消执行
           throw e;
       }
       return returnValue;
   }

```

增加对异常处理的方法

```java

@RestControllerAdvice(assignableTypes = ServerController.class)
public class CircuitBreakerControllerAdvice {

    @ExceptionHandler
    public void onTimeoutException(TimeoutException timeoutException,
                                   Writer writer) throws IOException {
        writer.write(errorContent("")); // 网络 I/O 被容器
        writer.flush();
        writer.close();
    }

    public String errorContent(String message) {
        return "Fault";
    }

}

```

## 高级版本（无注解实现）

```java

/**
    * 高级版本
    *
    * @param message
    * @return
    * @throws InterruptedException
    */
   @GetMapping("/advanced/say")
   public String advancedSay(@RequestParam String message) throws Exception {
       return doSay2(message);
   }

```

```java

@Aspect
@Component
public class ServerControllerAspect {

    private ExecutorService executorService = newFixedThreadPool(20);

    @Around("execution(* com.gupao.micro.services.spring.cloud." +
            "server.controller.ServerController.advancedSay(..)) && args(message) ")
    public Object advancedSayInTimeout(ProceedingJoinPoint point, String message) throws Throwable {
        Future<Object> future = executorService.submit(() -> {
            Object returnValue = null;
            try {
                returnValue = point.proceed(new Object[]{message});
            } catch (Throwable ex) {
            }
            return returnValue;
        });

        Object returnValue = null;
        try {
            returnValue = future.get(100, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true); // 取消执行
            returnValue = errorContent("");
        }
        return returnValue;
    }

    public String errorContent(String message) {
        return "Fault";
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }

}

```

## 高级版本（带注解实现）


抽离代码

```java

private Object doInvoke(ProceedingJoinPoint point,
                           String message, long timeout) throws Throwable {

       Future<Object> future = executorService.submit(() -> {
           Object returnValue = null;
           try {
               returnValue = point.proceed(new Object[]{message});
           } catch (Throwable ex) {
           }
           return returnValue;
       });

       Object returnValue = null;
       try {
           returnValue = future.get(timeout, TimeUnit.MILLISECONDS);
       } catch (TimeoutException e) {
           future.cancel(true); // 取消执行
           returnValue = errorContent("");
       }
       return returnValue;
   }

```

Aspect 注解实现


```java

@Around("execution(* com.gupao.micro.services.spring.cloud." +
           "server.controller.ServerController.advancedSay2(..)) && args(message) && @annotation(circuitBreaker)")
   public Object advancedSay2InTimeout(ProceedingJoinPoint point,
                                       String message,
                                       CircuitBreaker circuitBreaker) throws Throwable {
       long timeout = circuitBreaker.timeout();
       return doInvoke(point, message, timeout);
   }

```

反射API 实现

```java

@Around("execution(* com.gupao.micro.services.spring.cloud." +
            "server.controller.ServerController.advancedSay2(..)) && args(message) ")
    public Object advancedSay2InTimeout(ProceedingJoinPoint point,
                                        String message) throws Throwable {

        long timeout = -1;
        if (point instanceof MethodInvocationProceedingJoinPoint) {
            MethodInvocationProceedingJoinPoint methodPoint = (MethodInvocationProceedingJoinPoint) point;
            MethodSignature signature = (MethodSignature) methodPoint.getSignature();
            Method method = signature.getMethod();
            CircuitBreaker circuitBreaker = method.getAnnotation(CircuitBreaker.class);
            timeout = circuitBreaker.timeout();
        }
        return doInvoke(point, message, timeout);
    }

```

## 高级版本（信号灯实现 = 单机版限流方案）

```java

@Around("execution(* com.gupao.micro.services.spring.cloud." +
           "server.controller.ServerController.advancedSay3(..))" +
           " && args(message)" +
           " && @annotation(circuitBreaker) ")
   public Object advancedSay3InSemaphore(ProceedingJoinPoint point,
                                         String message,
                                         SemaphoreCircuitBreaker circuitBreaker) throws Throwable {
       int value = circuitBreaker.value();
       if (semaphore == null) {
           semaphore = new Semaphore(value);
       }
       Object returnValue = null;
       try {
           if (semaphore.tryAcquire()) {
               returnValue = point.proceed(new Object[]{message});
               Thread.sleep(1000);
           } else {
               returnValue = errorContent("");
           }
       } finally {
           semaphore.release();
       }

       return returnValue;

   }

```
