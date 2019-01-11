---
layout: post
title:  mybatis学习一
date:   2019-01-11 21:25:12 +08:00
category: 源码学习
tags: spring 源码
comments: true
---

* content
{:toc}

- MyBatis 是一款优秀的持久层框架，它支持定制化 SQL、存储过程以及高级映射。(是什么)
- MyBatis 避免了几乎所有的 JDBC 代码和手动设置参数以及获取结果集。（优势）
- MyBatis 可以使用简单的 XML 或注解来配置和映射原生信息，将接口和 Java 的 POJOs(Plain Ordinary Java Object,普通的 Java对象)映射成数据库中的记录。（怎么做的）




## 使用mybatis

**使用过程**
 1. 编程式
 2. 集成到spring

 具体使用过程：

 ```Mermaid
graph LR

业务分析 --> 定义表结构 --> 自动生成工具生成代码

```


**两种使用方式**
1. xml
2. annotation兼容并互补

 |名字 | 优势 | 不足 |
 |---|---|--- |
 |xml | 接口分离、统一管理。复杂的语句可以不影响接口可读性 | 过多xml文件 |
 |annotation | 接口可以看到sql语句，可读性高，复杂联合查询不好维护，不需要再去找xml,使用方便  | 复杂联合查询不好维护，复杂代码的可读性差 |



使用properties文件配置参数，批量导入
```java
<context:property-placeholder  ignore-unresolvable="true"
		location="classpath:dbconfig.properties,classpath:web.properties,classpath:shiro.properties" />
```  
可配操作
1. Environment
2. Typehandler(java和表字段类型的转换实现)

使用typehandler
1. 基础基础类

```java

@MappedJdbcTypes(JdbcType.VARCHAR)
public class TestTypeHandle extends BaseTypeHandler<String> {
}

//2.这是在查询结果里进行设置

<result column="name" jdbcType="VARCHAR" property="name" typeHandler="com.gupao.dal.typehandlers.TestTypeHandle"/>

//3.插入时进行设置

#{name,jdbcType=VARCHAR,typeHandler=com.gupao.dal.typehandlers.TestTypeHandle}

```

- 使用Interceptor（理解为拦截器，插件）


```java

//1.下边的插件作用输出sql语句运行的耗时、并输出sql语句

@Intercepts({@Signature(type = StatementHandler.class, method = "query", args = {Statement.class, ResultHandler.class}),
        @Signature(type = StatementHandler.class, method = "update", args = {Statement.class}),
        @Signature(type = StatementHandler.class, method = "batch", args = {Statement.class})})
public class PerformanceInterceptor implements Interceptor {

    private static final Log logger = LogFactory.getLog(PerformanceInterceptor.class);
    /**
     * SQL 执行最大时长，超过自动停止运行，有助于发现问题。
     */
    private long maxTime = 0;

    /**
     * SQL 是否格式化
     */
    private boolean format = false;

    /**
     * 是否写入日志文件<br>
     * true 写入日志文件，不阻断程序执行！<br>
     * 超过设定的最大执行时长异常提示！
     */
    private boolean writeInLog = false;

    private Method oracleGetOriginalSqlMethod;

    public Object intercept(Invocation invocation) throws Throwable {
        Statement statement;
        Object firstArg = invocation.getArgs()[0];
        if (Proxy.isProxyClass(firstArg.getClass())) {
            statement = (Statement) SystemMetaObject.forObject(firstArg).getValue("h.statement");
        } else {
            statement = (Statement) firstArg;
        }
        try {
            statement.getClass().getDeclaredField("stmt");
            statement = (Statement) SystemMetaObject.forObject(statement).getValue("stmt.statement");
        } catch (Exception e) {
            // do nothing
        }

        String originalSql = null;
        String stmtClassName = statement.getClass().getName();
        if ("oracle.jdbc.driver.T4CPreparedStatement".equals(stmtClassName)) {
            try {
                if (oracleGetOriginalSqlMethod != null) {
                    Object stmtSql = oracleGetOriginalSqlMethod.invoke(statement);
                    if (stmtSql != null && stmtSql instanceof String) {
                        originalSql = (String) stmtSql;
                    }
                } else {
                    Class<?> clazz = Class.forName("oracle.jdbc.driver.OracleStatement");
                    oracleGetOriginalSqlMethod = clazz.getDeclaredMethod("getOriginalSql", (Class<?>) null);
                    if (oracleGetOriginalSqlMethod != null) {
                        Object stmtSql = oracleGetOriginalSqlMethod.invoke(statement);
                        if (stmtSql != null && stmtSql instanceof String) {
                            originalSql = (String) stmtSql;
                        }
                    }
                }
            } catch (Exception e) {//ignore
            }
        }
        if (originalSql == null) {
            originalSql = statement.toString();
        }

        int index = originalSql.indexOf(':');
        if (index > 0) {
            originalSql = originalSql.substring(index + 1, originalSql.length());
        }

        // 计算执行 SQL 耗时
        long start = SystemClock.now();
        Object result = invocation.proceed();
        long timing = SystemClock.now() - start;

        // 格式化 SQL 打印执行结果
        Object target = PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(target);
        MappedStatement ms = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        StringBuilder formatSql = new StringBuilder();
        formatSql.append(" Time：").append(timing);
        formatSql.append(" ms - ID：").append(ms.getId());
        formatSql.append("\n Execute SQL：").append(SqlUtils.sqlFormat(originalSql, format)).append("\n");
        if (this.isWriteInLog()) {
            if (this.getMaxTime() >= 1 && timing > this.getMaxTime()) {
                logger.error(formatSql.toString());
            } else {
                logger.debug(formatSql.toString());
            }
        } else {
            System.err.println(formatSql.toString());
            if (this.getMaxTime() >= 1 && timing > this.getMaxTime()) {
                throw new MybatisPlusException(" The SQL execution time is too large, please optimize ! ");
            }
        }
        return result;
    }

    public Object plugin(Object target) {
        if (target instanceof StatementHandler) {
            return Plugin.wrap(target, this);
        }
        return target;
    }

    public void setProperties(Properties prop) {
        String maxTime = prop.getProperty("maxTime");
        String format = prop.getProperty("format");
        if (StringUtils.isNotEmpty(maxTime)) {
            this.maxTime = Long.parseLong(maxTime);
        }
        if (StringUtils.isNotEmpty(format)) {
            this.format = Boolean.valueOf(format);
        }
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public boolean isFormat() {
        return format;
    }

    public void setFormat(boolean format) {
        this.format = format;
    }

    public boolean isWriteInLog() {
        return writeInLog;
    }

    public void setWriteInLog(boolean writeInLog) {
        this.writeInLog = writeInLog;
    }
}
```

在mybatis-config.xml中进行配置

```xml
<!-- 插件配置 -->
	<plugins>
		 <!--
	     | 分页插件配置
	     | 插件提供二种方言选择：1、默认方言 2、自定义方言实现类，两者均未配置则抛出异常！
	     | overflowCurrent 溢出总页数，设置第一页 默认false
	     | optimizeType Count优化方式 （ 版本 2.0.9 改为使用 jsqlparser 不需要配置 ）
	     | -->
	    <!-- 注意!! 如果要支持二级缓存分页使用类 CachePaginationInterceptor 默认、建议如下！！ -->
		<plugin interceptor="com.baomidou.mybatisplus.plugins.PaginationInterceptor" />
		<!-- SQL 执行分析拦截器 stopProceed 发现全表执行 delete update 是否停止运行
	    <plugin interceptor="com.baomidou.mybatisplus.plugins.SqlExplainInterceptor">
	        <property name="stopProceed" value="false" />
	    </plugin>-->
	    <!-- SQL 执行性能分析，开发环境使用，线上不推荐。 maxTime 指的是 sql 最大执行时长 -->
	    <plugin interceptor="com.baomidou.mybatisplus.plugins.PerformanceInterceptor">
	        <property name="maxTime" value="2000" />
	        <!--SQL是否格式化 默认false-->
	        <property name="format" value="true" />
	    </plugin>

</plugins>


```
