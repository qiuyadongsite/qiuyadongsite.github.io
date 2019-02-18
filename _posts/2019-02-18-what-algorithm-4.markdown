---
layout: post
title:  图
date:   2019-02-18 23:45:12 +08:00
category: 算法
tags: 图
comments: true
---

* content
{:toc}

图(graph)是由一些点(vertex)和这些点之间的连线(edge)所组成的；其中，点通常被成为"顶点(vertex)"，而点与点之间的连线则被成为"边或弧"(edege)。通常记为，G=(V,E)。








## 概念

- 图分类

  有向图和无向图

  ![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/PG01.png)

  ![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/PG21.png)  

  ![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/PG31.png)

- 邻接点和度
 - 邻接点
一条边上的两个顶点叫做邻接点。 例如，上面无向图G0中的顶点A和顶点C就是邻接点。
在有向图中，除了邻接点之外；还有"入边"和"出边"的概念。 顶点的入边，是指以该顶点为终点的边。而顶点的出边，则是指以该顶点为起点的边。 例如，上面有向图G2中的B和E是邻接点；<B,E>是B的出边，还是E的入边。
  - 度

  在无向图中，某个顶点的度是邻接到该顶点的边(或弧)的数目。 例如，上面无向图G0中顶点A的度是2。

  在有向图中，度还有"入度"和"出度"之分。 某个顶点的入度，是指以该顶点为终点的边的数目。而顶点的出度，则是指以该顶点为起点的边的数目。 顶点的度=入度+出度。 例如，上面有向图G2中，顶点B的入度是2，出度是3；顶点B的度=2+3=5。

- 图的存储结构

  - 邻接矩阵
  邻接矩阵是指用矩阵来表示图。它是采用矩阵来描述图中顶点之间的关系(及弧或边的权)。 假设图中顶点数为n，则邻接矩阵定义为：

  ![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/p11.png)

  通常采用两个数组来实现邻接矩阵：一个一维数组用来保存顶点信息，一个二维数组来用保存边的信息。 邻接矩阵的缺点就是比较耗费空间。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/p12.png)

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/p13.png)

  - 邻接表

  邻接表是图的一种链式存储表示方法。它是改进后的"邻接矩阵"，它的缺点是不方便判断两个顶点之间是否有边，但是相对邻接矩阵来说更省空间。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/p141.png)  

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/p151.png)  

## 图的遍历

- 
