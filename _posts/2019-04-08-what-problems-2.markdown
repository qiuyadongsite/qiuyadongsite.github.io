---
layout: post
title:  TransactionTemplate编程式事务
date:   2019-04-08 22:52:12 +08:00
category: 面试题库
tags: spring
comments: true
---

* content
{:toc}

Spring可以支持编程式事务和声明式事务。

Spring提供的最原始的事务管理方式是基于TransactionDefinition、PlatformTransactionManager、TransactionStatus 编程式事务。

而TransactionTemplate的编程式事务管理是使用模板方法设计模式对原始事务管理方式的封装。








## TransactionTemplate

我们借助TransactionTemplate.execute( ... )执行事务管理的时候，传入的参数有两种选择：

1、TransactionCallback

2、TransactionCallbackWithoutResult

两种区别从命名看就相当明显了，一个是有返回值，一个是无返回值。这个的选择就取决于你是读还是写了。

## 案例实现

1、在spring配置文件中配置相关TransactionTemplate示例：


```xml

<!-- 事务管理器 -->
    <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">
        <property name="dataSource" ref="dataSource"/>
    </bean>
    <!-- 配置 transactionTemplate -->
    <bean id="transactionTemplate"
          class="org.springframework.transaction.support.TransactionTemplate">
        <property name="transactionManager">
            <ref bean="transactionManager"/>
        </property>
    </bean>

```

2、对TransactionCallback进行属性设置（该设置也可以在Spring的配置文件中指定，看个人需求）

```java

//设置事务传播属性
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        // 设置事务的隔离级别,设置为读已提交（默认是ISOLATION_DEFAULT:使用的是底层数据库的默认的隔离级别）
        transactionTemplate.setIsolationLevel(TransactionDefinition.ISOLATION_READ_COMMITTED);
        // 设置是否只读，默认是false
        transactionTemplate.setReadOnly(true);
        // 默认使用的是数据库底层的默认的事务的超时时间
        transactionTemplate.setTimeout(30000);

```

3、业务代码引用：

（1）借助(TransactionCallback)执行事务管理，既带有返回值：

```java

public Object getObject(String str) {
        /*
         *  执行带有返回值<Object>的事务管理
         */
        transactionTemplate.execute(new TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(TransactionStatus transactionStatus) {

                try {
                      ...
                    //.......   业务代码
                    return new Object();
                } catch (Exception e) {
                    //回滚
                    transactionStatus.setRollbackOnly();
                    return null;
                }
            }
        });
}


```

（2）借助(TransactionCallbackWithoutResult)执行事务管理，既无返回值：

```java

public void update(String str) {

         /*
         *  执行无返回值的事务管理
         */
        transactionTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {

                try {

                    // ....  业务代码
                } catch (Exception e){
                    //回滚
                    transactionStatus.setRollbackOnly();
                }

            }
        });
}

```
