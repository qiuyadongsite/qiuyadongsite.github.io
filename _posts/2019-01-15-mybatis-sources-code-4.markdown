---
layout: post
title:  mybatis源码学习二
date:   2019-01-15 22:25:12 +08:00
category: 源码学习
tags: mybatis 源码
comments: true
---

* content
{:toc}

- 学习mybatis的源码，mapper映射文件配置之insert、update、delete，将简单介绍 insert, update, delete 的配置及使用，以后会对mybatis的源码进行深入讲解。




## 配置insert, update, delete

```xml
<?xml version="1.0" encoding="UTF-8" ?>   
<!DOCTYPE mapper   
PUBLIC "-//ibatis.apache.org//DTD Mapper 3.0//EN"  
"http://ibatis.apache.org/dtd/ibatis-3-mapper.dtd">

<!-- mapper 为根元素节点， 一个namespace对应一个dao -->
<mapper namespace="com.dy.dao.UserDao">

    <insert
      <!-- 1. id （必须配置）
        id是命名空间中的唯一标识符，可被用来代表这条语句。
        一个命名空间（namespace） 对应一个dao接口,
        这个id也应该对应dao里面的某个方法（相当于方法的实现），因此id 应该与方法名一致 -->

      id="insertUser"

      <!-- 2. parameterType （可选配置, 默认为mybatis自动选择处理）
        将要传入语句的参数的完全限定类名或别名， 如果不配置，mybatis会通过ParameterHandler 根据参数类型默认选择合适的typeHandler进行处理
        parameterType 主要指定参数类型，可以是int, short, long, string等类型，也可以是复杂类型（如对象） -->

      parameterType="com.demo.User"

      <!-- 3. flushCache （可选配置，默认配置为true）
        将其设置为 true，任何时候只要语句被调用，都会导致本地缓存和二级缓存都会被清空，默认值：true（对应插入、更新和删除语句） -->

      flushCache="true"

      <!-- 4. statementType （可选配置，默认配置为PREPARED）
        STATEMENT，PREPARED 或 CALLABLE 的一个。这会让 MyBatis 分别使用 Statement，PreparedStatement 或 CallableStatement，默认值：PREPARED。 -->

      statementType="PREPARED"

      <!-- 5. keyProperty (可选配置， 默认为unset)
        （仅对 insert 和 update 有用）唯一标记一个属性，MyBatis 会通过 getGeneratedKeys 的返回值或者通过 insert 语句的 selectKey 子元素设置它的键值，默认：unset。如果希望得到多个生成的列，也可以是逗号分隔的属性名称列表。 -->

      keyProperty=""

      <!-- 6. keyColumn     (可选配置)
        （仅对 insert 和 update 有用）通过生成的键值设置表中的列名，这个设置仅在某些数据库（像 PostgreSQL）是必须的，当主键列不是表中的第一列的时候需要设置。如果希望得到多个生成的列，也可以是逗号分隔的属性名称列表。 -->

      keyColumn=""

      <!-- 7. useGeneratedKeys (可选配置， 默认为false)
      oracle等不支持id自增长的，可根据其id生成策略，先获取id
        （仅对 insert 和 update 有用）这会令 MyBatis 使用 JDBC 的 getGeneratedKeys 方法来取出由数据库内部生成的主键（比如：像 MySQL 和 SQL Server 这样的关系数据库管理系统的自动递增字段），默认值：false。  -->

      useGeneratedKeys="false"

      <!-- 8. timeout  (可选配置， 默认为unset, 依赖驱动)
        这个设置是在抛出异常之前，驱动程序等待数据库返回请求结果的秒数。默认值为 unset（依赖驱动）。 -->
      timeout="20">

    <update
      id="updateUser"
      parameterType="com.demo.User"
      flushCache="true"
      statementType="PREPARED"
      timeout="20">

    <delete
      id="deleteUser"
      parameterType="com.demo.User"
      flushCache="true"
      statementType="PREPARED"
      timeout="20">
</mapper>

```

- 实用内容

获取插入后的Id
```xml
<!-- 对应userDao中的insertUser方法，  -->
   <insert id="insertUser" parameterType="com.dy.entity.User">
           <!-- oracle等不支持id自增长的，可根据其id生成策略，先获取id

        <selectKey resultType="int" order="BEFORE" keyProperty="id">
              select seq_user_id.nextval as id from dual
        </selectKey>

        -->

        <!-- mysql插入数据后，获取id -->
        <selectKey keyProperty="id" resultType="int" order="AFTER" >
               SELECT LAST_INSERT_ID() as id
           </selectKey>

           insert into user(id, name, password, age, deleteFlag)
               values(#{id}, #{name}, #{password}, #{age}, #{deleteFlag})
   </insert>


   <selectKey
        <!-- selectKey 语句结果应该被设置的目标属性。如果希望得到多个生成的列，也可以是逗号分隔的属性名称列表。 -->
        keyProperty="id"
        <!-- 结果的类型。MyBatis 通常可以推算出来，但是为了更加确定写上也不会有什么问题。MyBatis 允许任何简单类型用作主键的类型，包括字符串。如果希望作用于多个生成的列，则可以使用一个包含期望属性的 Object 或一个 Map。 -->
        resultType="int"
        <!-- 这可以被设置为 BEFORE 或 AFTER。如果设置为 BEFORE，那么它会首先选择主键，设置 keyProperty 然后执行插入语句。如果设置为 AFTER，那么先执行插入语句，然后是 selectKey 元素 - 这和像 Oracle 的数据库相似，在插入语句内部可能有嵌入索引调用。 -->
        order="BEFORE"
        <!-- 与前面相同，MyBatis 支持 STATEMENT，PREPARED 和 CALLABLE 语句的映射类型，分别代表 PreparedStatement 和 CallableStatement 类型。 -->
        statementType="PREPARED">


```
## 配置mapper映射文件配置之select、resultMap

```xml
<select
        <!--  1. id （必须配置）
        id是命名空间中的唯一标识符，可被用来代表这条语句。
        一个命名空间（namespace） 对应一个dao接口,
        这个id也应该对应dao里面的某个方法（相当于方法的实现），因此id 应该与方法名一致  -->

     id="selectPerson"

     <!-- 2. parameterType （可选配置, 默认为mybatis自动选择处理）
        将要传入语句的参数的完全限定类名或别名， 如果不配置，mybatis会通过ParameterHandler 根据参数类型默认选择合适的typeHandler进行处理
        parameterType 主要指定参数类型，可以是int, short, long, string等类型，也可以是复杂类型（如对象） -->
     parameterType="int"

     <!-- 3. resultType (resultType 与 resultMap 二选一配置)
         resultType用以指定返回类型，指定的类型可以是基本类型，可以是java容器，也可以是javabean -->
     resultType="hashmap"

     <!-- 4. resultMap (resultType 与 resultMap 二选一配置)
         resultMap用于引用我们通过 resultMap标签定义的映射类型，这也是mybatis组件高级复杂映射的关键 -->
     resultMap="personResultMap"

     <!-- 5. flushCache (可选配置)
         将其设置为 true，任何时候只要语句被调用，都会导致本地缓存和二级缓存都会被清空，默认值：false -->
     flushCache="false"

     <!-- 6. useCache (可选配置)
         将其设置为 true，将会导致本条语句的结果被二级缓存，默认值：对 select 元素为 true -->
     useCache="true"

     <!-- 7. timeout (可选配置)
         这个设置是在抛出异常之前，驱动程序等待数据库返回请求结果的秒数。默认值为 unset（依赖驱动）-->
     timeout="10000"

     <!-- 8. fetchSize (可选配置)
         这是尝试影响驱动程序每次批量返回的结果行数和这个设置值相等。默认值为 unset（依赖驱动)-->
     fetchSize="256"

     <!-- 9. statementType (可选配置)
         STATEMENT，PREPARED 或 CALLABLE 的一个。这会让 MyBatis 分别使用 Statement，PreparedStatement 或 CallableStatement，默认值：PREPARED-->
     statementType="PREPARED"

     <!-- 10. resultSetType (可选配置)
         FORWARD_ONLY，SCROLL_SENSITIVE 或 SCROLL_INSENSITIVE 中的一个，默认值为 unset （依赖驱动）-->
     resultSetType="FORWARD_ONLY">

```
resultMap的配置介绍
```xml
<!--
        1.type 对应类型，可以是javabean, 也可以是其它
        2.id 必须唯一， 用于标示这个resultMap的唯一性，在使用resultMap的时候，就是通过id指定
     -->
    <resultMap type="" id="">

        <!-- id, 唯一性，注意啦，这个id用于标示这个javabean对象的唯一性， 不一定会是数据库的主键（不要把它理解为数据库对应表的主键）
            property属性对应javabean的属性名，column对应数据库表的列名
            （这样，当javabean的属性与数据库对应表的列名不一致的时候，就能通过指定这个保持正常映射了）
        -->
        <id property="" column=""/>

        <!-- result与id相比， 对应普通属性 -->    
        <result property="" column=""/>

        <!--
            constructor对应javabean中的构造方法
         -->
        <constructor>
            <!-- idArg 对应构造方法中的id参数 -->
            <idArg column=""/>
            <!-- arg 对应构造方法中的普通参数 -->
            <arg column=""/>
        </constructor>

        <!--
            collection，对应javabean中容器类型, 是实现一对多的关键
            property 为javabean中容器对应字段名
            column 为体现在数据库中列名
            ofType 就是指定javabean中容器指定的类型
        -->
        <collection property="" column="" ofType=""></collection>

        <!--
            association 为关联关系，是实现N对一的关键。
            property 为javabean中容器对应字段名
            column 为体现在数据库中列名
            javaType 指定关联的类型
         -->
        <association property="" column="" javaType=""></association>
    </resultMap>
```
## 配置强大的动态SQL
1. if(如果if中为空，语句有问题)
```xml
<select id="findUserById" resultType="user">
           select * from user where
           <if test="id != null">
               id=#{id}
           </if>
            and deleteFlag=0;
</select>

```
2. 解决if中的问题where；
```xml
<select id="findUserById" resultType="user">
           select * from user
           <where>
               <if test="id != null">
                   id=#{id}
               </if>
               and deleteFlag=0;
           </where>
 </select>

```
3. 2的处理其实就是处理遇到and和or时的实际问题，用trim
```xml
<trim prefix="WHERE" prefixOverrides="AND |OR ">
  ...
</trim>

```
4. 遇到update时，if和where时会有问题
```xml
//如下
<update id="updateUser" parameterType="com.dy.entity.User">
           update user set
           <if test="name != null">
               name = #{name},
           </if>
           <if test="password != null">
               password = #{password},
           </if>
           <if test="age != null">
               age = #{age}
           </if>
           <where>
               <if test="id != null">
                   id = #{id}
               </if>
               and deleteFlag = 0;
           </where>
</update>
```
解决方案：
```xml
<update id="updateUser" parameterType="com.dy.entity.User">
           update user
        <set>
          <if test="name != null">name = #{name},</if>
             <if test="password != null">password = #{password},</if>
             <if test="age != null">age = #{age},</if>
        </set>
           <where>
               <if test="id != null">
                   id = #{id}
               </if>
               and deleteFlag = 0;
           </where>
</update>

```
或者
```xml
<trim prefix="SET" suffixOverrides=",">
  ...
</trim>

```
5. foreach
```xml
<select id="selectPostIn" resultType="domain.blog.Post">
  SELECT *
  FROM POST P
  WHERE ID in
  <foreach item="item" index="index" collection="list"
      open="(" separator="," close=")">
        #{item}
  </foreach>
</select>

```
6. choose
```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG WHERE state = ‘ACTIVE’
  <choose>
    <when test="title != null">
      AND title like #{title}
    </when>
    <when test="author != null and author.name != null">
      AND author_name like #{author.name}
    </when>
    <otherwise>
      AND featured = 1
    </otherwise>
  </choose>
</select>

```
