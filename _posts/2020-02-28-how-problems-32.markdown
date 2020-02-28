---
layout: post
title:  maven1
date:   2020-02-28 20:53:12 +08:00
category: 签到系列
tags: maven
comments: true
---

* content
{:toc}



签到32！



## 学习总结

不要人夸好颜色，只留清气满乾坤。 

## 1、Maven是什么？

它是依赖管理、项目构建和项目信息管理的工具

特点：

1. 依赖管理
2. 多模块构建
3. 一致项目结构
4. 一致构建模型和插件机制

使用：

1. 对系统进行构建，打包
2. 依赖管理

规约：

1. /src/main/java/:源文件目录
2. /src/main/resources/:java配置文件和资源文件
3. /src/test/java/:java测试代码
4. /src/test/resources/:java测试的配置和资源文件
5. /target;编译过程中的.class，war,jar等
6. pom:配置文件

**常用命令**：

- `mvn compile` ：编译源代码。 
- `mvn install` ：在本地 Repository 中安装 jar 。 
- `mvn clean` ：清除项目目录中的生成结果。 
- `mvn test` ：运行应用程序中的单元测试。 
- `mvn package` ：根据项目生成的 jar/war 等。 
- `mvn clean package -Dmaven.test.skip=true` ：清除以前的包后重新打包，跳过测试类。 

优点缺点：

- 优点
  - 依赖管理
  - 易于上手
  - 便于系统升级
  - 多模块开发
  - 好用的插件
- 缺点
  - 学习难度大
  - 约定大于配置，约定很多
  - 需要使用ideal,好用
  - 很多仓库需要使用国内的

## 2、Maven组成?

坐标：

- groupId:当前的项目隶属的实际项目；
- artifactId:实际项目名称；
- version:使用该构件的版本；
- packaging:Maven项目的打包方式，以及使用构件的什么包；
- classifier:定义构件的输出附件，不能直接使用配合插件使用；

依赖：

- dependencies
  - `groupId` ：依赖项的 `groupId` 。
  - `artifactId` ：依赖项的 `artifactId` 。
  - `version` ：依赖项的 `version` 。
  - `scope` ：依赖项的适用范围。
  - `exclusions` ：排除项目中的依赖冲突时使用。 
- dependencyManagement  
  - `<dependencyManagement />` ， 统一了 Maven 中依赖的版本号，定义在 `dependencie />` 中的依赖，在不指定具体版本号时，就会沿着上层找到 `<dependencyManagement />` 中的依赖，并使用它的版本号。这样的话，当有多个子项目引用同一个依赖时，就不需要重复声明各自的版本号，只需统一使用 `<dependencyManagement />` 中的版本号即可。
  - `<dependencyManagement />` 中出现的依赖，并不一定会在项目中使用，而 `<dependencie />` 中的依赖，肯定是包含在项目中的。
- 如何解决冲突
  - 遇到冲突的时候第一步，要找到 Maven 加载的到时是什么版本的 jar 包，通过们 `mvn dependency:tree` 查看依赖树，或者使用 [IDEA Maven Helper](https://plugins.jetbrains.com/plugin/7179-maven-helper) 插件。
  - 然后，通过 Maven 的依赖原则来调整坐标在 pom 文件的申明顺序是最好的办法，或者使用将冲突中不想要的 jar 引入的 jar 进行 `<exclusions>` 掉。

## 3、Maven生命周期？

**ideal中的生命周期**：

- clean ：清理自动生成的文件，也就是 target 目录。
- validate ：验证 Maven 描述文件是否有效。
- compile ：编译 java 代码。
- test ：运行测试代码。
- package ：项目打成 jar、war 包等。
- verify ：验证构件包是否有效。
- install ：将构件包安装到本地仓库。
- deploy ：将构件包部署到远程仓库。
- site ：生成项目站点。

**插件**：

Maven 生命周期的每一个阶段的具体实现都是由 Maven 插件实现的。插件通常提供了一个目标的集合，并且可以使用下面的语法执行：`mvn [plugin-name]:[goal-name]` ；

Maven 提供了下面两种类型的插件：

- Build plugins ：在构建时执行，并在 `pom.xml` 的 元素中配置。
- Reporting plugins ：在网站生成过程中执行，并在 `pom.xml` 的元素中配置。

下面是一些常用插件的列表：

- clean ：构建之后清理目标文件。删除目标目录。
- compiler ：编译 Java 源文件。
- surefile ：运行 JUnit 单元测试。创建测试报告。
- jar ：从当前工程中构建 JAR 文件。
- war ：从当前工程中构建 WAR 文件。
- javadoc ：为工程生成 Javadoc 。
- antrun ：从构建过程的任意一个阶段中运行一个 ant 任务的集合。

## 4、Maven的仓库？

**分类**：

- 本地仓库
- 远程仓库
  - 中央仓库
  - 私服
  - 其他公共库

Maven 会先搜索本地仓库（repository），发现本地没有然后从远程仓库（中央仓库）获取。

- 但中央仓库只有一个，最好从其镜象处下载。国内可以用阿里云下的服务器。【*其它公共库*】
- 也有通过 Nexus 搭建的私服进行获取的。【*私服*】

 **Maven 中的仓库分为两种**：

Maven 会根据模块的版本号(`pom` 文件中的 `version`)中是否带有 `-SNAPSHOT` 来判断是快照版本还是正式版本。 

- SNAPSHOT 快照仓库

  那么在 `mvn deploy` 时会自动发布到快照版本库中，会覆盖老的快照版本。而在使用快照版本的模块，在不更改版本号的情况下，直接编译打包时，Maven 会自动从镜像服务器上下载最新的快照版本。 

- RELEASE 发布仓库

  如果是正式发布版本，那么在 `mvn deploy` 时会自动发布到正式版本库中，而使用正式版本的模块，在不更改版本号的情况下，编译打包时如果本地已经存在该版本的模块则不会主动去镜像服务器上下载。 

所以，我们在开发阶段，可以将公用库的版本设置为快照版本，而被依赖组件则引用快照版本进行开发，在公用库的快照版本更新后，我们也不需要修改 pom 文件提示版本号来下载新的版本，直接 mvn 执行相关编译、打包命令即可重新下载最新的快照库了，从而也方便了我们进行开发。 



**什么是私服？**

私服是一种特殊的远程仓库，它是架设在局域网内的仓库服务，私服代理广域网上的远程仓库，供局域网内的 Maven 用户使用。当 Maven 需要下载构件的时候，它从私服请求，如果私服上不存在该构件，则从外部的远程仓库下载，缓存在私服上之后，再为 Maven 的下载请求提供服务。我们还可以把一些无法从外部仓库下载到的构件上传到私服上。

? Maven 私服的 5 个特性：

- 1、节省自己的外网带宽：减少重复请求造成的外网带宽消耗。
- 2、加速 Maven 构件：如果项目配置了很多外部远程仓库的时候，构建速度就会大大降低。
- 3、部署第三方构件：有些构件无法从外部仓库获得的时候，我们可以把这些构件部署到内部仓库(私服)中，供内部 Maven 项目使用。
- 4、提高稳定性，增强控制：Internet 不稳定的时候，Maven 构建也会变的不稳定，一些私服软件还提供了其他的功能。
- 5、降低中央仓库的负荷：Maven 中央仓库被请求的数量是巨大的，配置私服也可以大大降低中央仓库的压力。

当前主流的 Maven 私服：

- Apache 的 Archiva
- JFrog 的 Artifactory
- 【主流】Sonatype 的 Nexus 。

常见的 Maven 私服的仓库类型：

- （宿主仓库）hosted repository 。
- （代理仓库）proxy repository 。
- （仓库组）group repository 。

对于大多数公司，一般来说使用 Nexus + 阿里云仓库的方式，可参考如下两篇文章：

**如何配置远程仓库？**

用文本编辑器工具打开 `setting.xml` 文件，增加一个 `<mirror />`：

```
<mirrors>
    <mirror>
        <id>nexus-aliyun</id>
        <mirrorOf>*</mirrorOf>
        <name>Nexus aliyun</name>
        <url>http://maven.aliyun.com/nexus/content/groups/public</url>
    </mirror>
</mirrors>

123456789
```

**如何使用 Maven 将 jar 包发布到中央仓库？**

 详细的，参见 [《发布 jar 包到 Maven 中央仓库》](https://blog.csdn.net/ljbmxsm/article/details/78009268) 文章。