---
layout: post
title:  mybatis学习二
date:   2019-01-14 21:25:12 +08:00
category: 源码学习
tags: mybatis 源码
comments: true
---

* content
{:toc}

- 继续学习mybatis的使用




## 配置文件解读

**mapper文件解读**
 1. namespace
    关联接口的方法，区别类似package的作用
 2. resultMap/resultType
  |名字 | 优势 | 不足 |
  |---|---|--- |
  |resultType|多表关联字段是清楚知道的，性能调优直观|创建很多实体类|
  |resultMap|不需要写join语句|N+1问题|


**代码实例**
1. resultMap

```xml

<resultMap id="detailedBlogResultMap" type="Blog">
  <constructor>
    <idArg column="blog_id" javaType="int"/>
  </constructor>
  <result property="title" column="blog_title"/>
  //1:1 ,类里有个Author类型的author
  <association property="author" javaType="Author">
    <id property="id" column="author_id"/>
    <result property="username" column="author_username"/>
    <result property="password" column="author_password"/>
    <result property="email" column="author_email"/>
    <result property="bio" column="author_bio"/>
    <result property="favouriteSection" column="author_favourite_section"/>
  </association>
  //1:n ,类里有个List<Post> posts
  <collection property="posts" ofType="Post">
    <id property="id" column="post_id"/>
    <result property="subject" column="post_subject"/>
    <association property="author" javaType="Author"/>
    <collection property="comments" ofType="Comment">
      <id property="id" column="comment_id"/>
    </collection>
    <collection property="tags" ofType="Tag" >
      <id property="id" column="tag_id"/>
    </collection>
    <discriminator javaType="int" column="draft">
      <case value="1" resultType="DraftPost"/>
    </discriminator>
  </collection>
</resultMap>

```

2. 自增sql，添加后返回id

```xml
<insert id="insertAuthor" useGeneratedKeys="true"
    keyProperty="id">
  insert into Author (username,password,email,bio)
  values (#{username},#{password},#{email},#{bio})
</insert>

```

3.动态sql

```xml

// if
<select id="findActiveBlogWithTitleLike"
     resultType="Blog">
  SELECT * FROM BLOG
  WHERE state = ‘ACTIVE’
  <if test="title != null">
    AND title like #{title}
  </if>
</select>
// if if if
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG WHERE state = ‘ACTIVE’
  <if test="title != null">
    AND title like #{title}
  </if>
  <if test="author != null and author.name != null">
    AND author_name like #{author.name}
  </if>
</select>

//

<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
  WHERE
  <if test="state != null">
    state = #{state}
  </if>
  <if test="title != null">
    AND title like #{title}
  </if>
  <if test="author != null and author.name != null">
    AND author_name like #{author.name}
  </if>
</select>

//foreache
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

## 缓存
1. 一级缓存
- 基于SqlSession,默认是开启的（减少数据库的压力）
- 有可能读脏数据，命中已经改变的缓存，但一般不会那么设计
- 优点>可能存在的问题
- 更新策略：update delete

2. 二级缓存
- 默认是关闭的，也不建议使用，一般使用redis等第三方来替代
- 问题是：二级缓存是mapper级别的，问题很大
- 更新策略：update delete
- 读脏数据、一旦更新全部失效、例如：连表查询的时候，其他mapper已经修改，但此mapper还有读取了脏数据
