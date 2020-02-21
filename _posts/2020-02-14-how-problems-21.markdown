---
layout: post
title:  uml
date:   2020-02-14 20:53:12 +08:00
category: 签到系列
tags: uml
comments: true
---

* content
{:toc}



签到20！



## 学习总结

南北驱驰报主情，江花边草笑平生

 一年三百六十日，多是横戈马上行

## 1、什么是UML?

**统一建模语言**（Unified Modeling Language， UML） 是一种为**面向对象系统的产品**进行说明、 可视化和编制文档的一种标准语言， 是非专利的第三代建模和规约语言。

UML使用面向对象设计的的建模工具， 但独立于任何具体程序设计语言。



## 2、UML中的图

**用例图（Usecase Diagrams）**

用来描述用户的需求，从用户的角度描述系统的功能， 并指出各功能的执行者，强调谁在使用系统，系统为执行者完成哪些功能。

**类图（Class Diagrams）**

用于定义系统中的类。

**对象图（Object Diagrams）**

类图的一个实例，描述了系统在具体时间点上所包含 的对象以及各个对象之间的关系。

**构件图（Component Diagrams）**

一种特殊的 UML 图来描述系统的静态实现视图。

 **部署图（Deployment Diagrams）**

定义系统中软硬件的物理体系结构。

 **状态图（State Chart Diagrams）**

用来描述类的对象所有可能的状态以及时间发生时 状态的转移条件。

 **协作图（Collaboration Diagrams）**

描述对象之间的合作关系，更侧重和用户对象说 明哪些对象有消息的传递。 

**活动图（Activity Diagrams）**

用来描述满足用例要求索要进行的活动以及活动间的约 束关系。    

**时序图（Sequence Diagrams）**

描述对象之间的交互顺序，着重体现对象间消息传递 的时间顺序，强调对象之间消息的发送顺序，同时显示对象之间的交互过程。 

**包图（Package Diagrams）**

对构成系统的模型元素进行分组整理的图。

 **组合结构图（Composite Structure Diagrams）**

表示类或者构建内部结构的图。 

**时间图（Timing Diagrams）**

用来显示随时间变化，一个或多个元素的值或状态的更 改，也显示时控事件之间的交互和管理它们的时间和期限约束。

 **交互概览图（Interaction Overview Diagrams）**

用活动图来表示多个交互之间的控制 关系的图。    

## 3、类图

#### **事物**

- 类：具有相同属性、方法、语义和关系的描述；
- 接口：一组操作规范，没有实现
-  用例：一组序列的描述
- 包：将事物进行分组

#### 事物关系

- 关联

  引用关系

  ![](http://www.plantuml.com/plantuml/png/SoWkIImgAStDuKhEIImkLWXFB4ajWh8zIdCJyxZ0f8AkhXrKpY4rBmMe7W00)

  

- 聚合

  整体局部关系

  ![](http://www.plantuml.com/plantuml/png/SoWkIImgAStDuN8gBNOfoYnBBLB8rzLLSCx9Byb8BKOJzKWiuN98pKi1gWS0)

- 组合

  更加严格的整体局部关系

  ![](http://www.plantuml.com/plantuml/png/SoWkIImgAStDuV8epKtCKz3IrLNmI4rCGUOcvgLmEQJcfG1L0G00)

- 泛化

  一个更泛化和一个更具体的关系，类继承

  ![](http://www.plantuml.com/plantuml/png/SoWkIImgAStDuN8foYnBBLAmgT7LLN0gBOPmkHnIyrA0iW00)

- 实现

  接口和类的关系

  ![](http://www.plantuml.com/plantuml/png/SoWkIImgAStDuGfBpSXCpabLiAdHqrDmJYpYSaZDIm5A0000)

- 依赖

  一个类的改动影响另一个类

  ![](http://www.plantuml.com/plantuml/png/SoWkIImgAStDuGhEo2nGqDEpK_3FpE5oJYo2inrIyrA02W00)

#### 记忆技巧

- 类关系

  - 箭头方向

    子类指向父类（只有知道对方信息，才能找到，去指向，父类不知道子类，子类知道父类）

  - 继承实现

    - 使用空心的三角箭头
    - 实线，继承，因为都有实体实现
    - 虚线，实现，无实体

  - 关联依赖

    - 虚线表示依赖关系：临时用一下嘛，一般都是将依赖的类作为一个方法的参数或者返回值
    - 实现表示关联关系：关系稳定，一般作为类的一个属性

  - 组合聚合

    - 都用棱形的箭头表示，这个棱形表示一个盘子
    - 空心表示聚合，两者有独立的生命周期，虚，弱
    - 实心表示组合，两者有同样的生命周期，实，强

  - 没有实心的三角箭头

## 4、时序图

表示对象直接发送消息的顺序，强调时间顺序；横轴表示对象，纵轴表示时间；

直观的描述交互的时间顺序和并发；

#### 组成元素

- 角色

  系统角色、人或者机器、子系统等；

- 对象

  使用类名或者对象名

- 生命线

  对象向下的一条虚线，表示对象存在的时间

- 控制焦点

  激活期，对象在该阶段有活动

- 消息

  - 同步消息

  - 异步消息

  - 返回消息

  - 自关联消息

    