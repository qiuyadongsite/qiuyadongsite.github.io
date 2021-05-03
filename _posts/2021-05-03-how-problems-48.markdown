---
layout: post
title:  开发
date:   2021-05-02 20:53:12 +08:00
category: 源码学习
tags: springboot
comments: true
---

* content
{:toc}

生如蝼蚁，当立鸿鹄之志。命如薄纸，却有不屈之心！





## 概念

服务框架的框架，约定优于配置

- maven的目录结构
- starter,开箱及用
- 默认application.properties

springboot里面没有新技术
内置了spring和tomcat

- 自动装配

- start

- 监控

- spring boot cli

`@SpringBootApplication`

- `componentScan`

扫描注解默认是本包下的所有标有注解的类


- `enableAutoConfiguration`

`AutoConfigurtionPackage`、`Import`

- `SpringbootConfiguration/configuration`

`@configuration`配置类
基于注解实现配置
`@Bean`
`@Scope`
` @ConditionalOnMissingClass`
`@Primary`
`@Import`
`@EnableConfigurationProperties`

## 动态注入

AutoConfigurtionImportSelector

AutoConfigurtionPackage.Registrar

ConditionOnBean条件注入

在目录META-INF/spring.factories文件中配置

org.springframework.boot.autoconfigure.EnableAutoConfiguration=

在目录META-INF/spring-autoconfigure-metadata.properties中配置

org.springframework.boot.autoconfigure.session.RedisSessionConfiguration.ConditionalOnClass

或者直接使用注解

原理：需要实现ImportSelector或者ImportBeanDefinitionRegistrar

## 多数据源配置starter

app.datasource.db1.url=jdbc:mysql://192.168.8.126:3306/db1
app.datasource.db1.username=root
app.datasource.db1.password=root
app.datasource.db1.driver-class-name=com.mysql.jdbc.Driver

app.datasource.db2.url=jdbc:mysql://192.168.8.126:3306/db2
app.datasource.db2.username=root
app.datasource.db2.password=root
app.datasource.db2.driver-class-name=com.mysql.jdbc.Driver

```java

@Configuration
public class JdbcDataSourceConfig {

    @Primary
    @Bean
    @ConfigurationProperties(prefix = "app.datasource.db1")
    public DataSourceProperties db1DataSourceProperties(){
        return new DataSourceProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "app.datasource.db2")
    public DataSourceProperties db2DataSourceProperties(){
        return new DataSourceProperties();
    }

    @Primary
    @Bean
    public DataSource db1DataSource(){
        return db1DataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean
    public DataSource db2DataSource(){
        return db2DataSourceProperties().initializeDataSourceBuilder().build();
    }

    @Bean(name="db1JdbcTemplate")
    public JdbcTemplate db1JdbcTemplate(){
        return new JdbcTemplate(db1DataSource());
    }

    @Bean(name="db2JdbcTemplate")
    public JdbcTemplate db2JdbcTemplate(){
        return new JdbcTemplate(db2DataSource());
    }

}


```


- 测试

```java

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringbootStarterDemoApplicationTests {

    @Test
    public void contextLoads() {
//        System.out.println(dataSource.getClass());
    }

    @Autowired
    JdbcTemplate db1JdbcTemplate;
   /* @Autowired
    DataSource dataSource;*/

    @Test
     public void addDataData(){

        String sql="insert into user_info(name,age) values('mic1',18)";
        db1JdbcTemplate.execute(sql);
     }

}



```
