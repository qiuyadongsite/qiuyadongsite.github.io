---
layout: post
title:  数据结构与算法一
date:   2019-06-12 21:52:12 +08:00
category: 性能优化
tags: 算法
comments: true
---

* content
{:toc}



数据结构是用来存储一定结构的数据的，而算法是对这些数据的操作，最后利用计算机的运算功能得到想要的结果，可以帮助人们完成人工很难完成的任务。所以非常有必要对数据结构与算法进行学习，不懂数据结构与算法，就好比武林中只会三脚猫功夫的人，没有好结果。



















## 数据结构

根据《数据结构实用教程第二版》画出如下图，第一遍先掌握如下：

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/dataStruct001.png)

### 集合

Set接口，有无序的HashSet和有序的TreeSet两个实现类。Set具体实现是实现在java中用Map的key值存储值；

### 线性结构

- 线性表

List接口：具体可查看ArrayList、LinkedList等；

- 栈和队列

可查看：Vector、Stack、Queue、PriorityQueue等

- 存储结构

顺序存储是实用数组，链式存储是使用链表，在增删改查时各有优缺点；

## 树

### 二叉树

- 特性

第i层上至多有2i-1个结点（i>=1）、深度为k的二叉树至多有2k-1个结点（k>=1）、对任何一颗二叉树T，如果其终端结点数为n0,度为2的结点数为n2，则n0 = n2+1、具有n个结点的完全二叉树深度为[log2n]+1 ([x]表示不	大于	x的最大整数)。

- 遍历
    前序遍历：（中-左-右）

    ```java

     //递归    
     public void preLoadTree(BinaryTreeNode node){
       if(node==null){
           return;
       }else{
           System.out.println("前序遍历："+node.data);
           preLoadTree(node.leftNode);
           preLoadTree(node.rightNode);
       }
   }
    //非递归
    public void nonpre(BinaryTreeNode node){
          Stack<BinaryTreeNode> stack=new Stack<>();
          stack.push(node);
          while(!stack.isEmpty()){
              BinaryTreeNode pop = stack.pop();
              System.out.println("non pre: "+ pop.data);
              if(pop.rightNode!=null){
                  stack.push(pop.rightNode);
              }
              if(pop.leftNode!=null){
                  stack.push(pop.leftNode);
              }
          }
		}

    ```

代码太多了，使用链接方式；
    中序遍历：（左-中-右）
    后序遍历：（左-右-中）

    [代码地址](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/tree/BinaryTreeDemo.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/tree/BinaryTreeDemo.java

### 特殊二叉树

- 二叉搜索树

左子树小于根节点，根节点大于右子树。符合中序遍历原则。

对于二叉搜索树的代码（构建、删除、检索）
[代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/tree/SearchBinaryTree.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/tree/SearchBinaryTree.java

- 堆

小根堆和大根堆。

小根堆是一课完全二叉树：根节点小于左右子树。

大根堆：根节点大于左右子树；具体可以在堆排序中得到应用

[代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeepSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeepSort.java

- 哈夫曼树

n个带权叶子节点构成的所有二叉树中，带权WPL路径最小的二叉树，就是哈夫曼树，常用于数据压缩领域。

构建哈夫曼树：找最小的两个节点作为叶子节点，将权值相加得到类似新的叶子节点，找倒数第三小的构建新的二叉树，以此类推。

- 线索二叉树

也叫二叉树的线索化，根据二叉树的某种遍历顺序如：中序遍历。将二叉树搞成线性存储，中序前驱，中序后继，根没有中序前驱，终点没中序后继。

- 平衡二叉树

若二叉树中每个阶段的左右子树高度差至多为1，则为平衡二叉树，AVL树。

## 图

### 概念

图由定点集合边集组成，可分为有向图合无向图，无向图看这个节点到其他节点的边就是度，有向图有入度和出度之分。

完全图，互相之间都有线；

稠密图、稀疏图之分；连通图和非连通图之分；带权的图叫做网。

### 存储结构

- 邻接矩阵

定点之间相邻关系的矩阵。二位数组表示法，带权值和不带权值，对角线为0，无穷值代表不连接。

- 邻接表

对每个节点建立一个邻接关系的单链表，并把表头指向一个一维数组。类似hashmap

- 边集数组

利用一维数组存储图中所有边的一种方式。

### 图的遍历

- 深度优先搜索遍历

类似先根遍历，是一个递归方式，定义一个存放节点访问与否的boolean数组。从根出发访问一个节点，并标识已经访问过，以此类推，访问到头后，再回退到初始节点并且没有未被访问的节点为止。

- 广度优先搜索遍历

类似对树的按层遍历，从根开始，访问所有临界节点，以此类推。

### 图的应用

按照图的遍历，深度优先生成树、广度优先生成树。

- 最小生成树

  具有权最小的生成树称为最小生成树

- 普里姆算法

每次从树T中到T外的所有边中找一个条最短的边。

[代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/graph/Graph.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/graph/Graph.java

- 克鲁斯卡尔算法

先按照权值进行排序，从小到大选取不形成回路。

[代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/graph/GraphKruskal.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-ability/src/main/java/com/qyd/learn/show/algorithm/graph/GraphKruskal.java

最短路径、拓扑排序、关键路径下次搞吧，能力有限。

## 算法

算法博大精深，这里将从最基本说起，能力有限。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/suanfa2.png)

### 特性

有穷性，可以得到有限的结果。确切性，指结果唯一。输入性，有处理的数据。输出性，干了某件事有个结果。可行性，当前情况可以有结果。

### 优劣的判断

时间复杂度，其实就是指运行的指令数，一般会根据处理的数的n的值，发生规律变化。

空间复杂度，一个算法在运行过程中临时占用存储空间大小的量度。
直接插入排序的时间复杂度是O(n^2),空间复杂度是O(1) ，递归算法O(n)的空间复杂度。

正确是必须的。可读是好维护说的。健壮性其实就是考虑到各种不同特殊情况。

### 运算要素

不想看，关键位运算必须掌握。

### 排序算法

- 插入排序
  - 直接插入排序
  思想：往有序的队列中插入，后边的元素后移。
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/InsertSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/InsertSort.java

  - 二分法插入排序
  思想：直接插入排序不高效，同样往有序的队列中插入，二分插入直到left指针不小于right指针，找到位置快，同样后边的元素后移。
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/BinaryInsortSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/BinaryInsortSort.java

  - 希尔排序
  思想：增量的对比，以及增量改变对比，最后保证输出结果，不稳定排序。
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeerSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeerSort.java
- 选择排序
  - 简单选择排序
  思想：选最小的放最前边
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeerSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeerSort.java

  - 堆排序
  思想：构建大堆，根节点下沉，放最后一个。以此类推。
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeepSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/HeepSort.java

- 交换排序
  - 冒泡排序
  思想：进行n轮比较，每次上浮一个最大的。O(n^2)
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/MaoSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/MaoSort.java
  - 快速交换排序
  思想：找到第一个元素在集合中的位置，进行交换，以此对左边和右边递归。
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/QuickSwap.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/QuickSwap.java

- 归并排序
  思想：一分为二，最后分为一个一个元素，再合并两个有序数组；
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/MergeSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/MergeSort.java

- 基数排序
  思想：针对数字特性的排序，找到最大数的位数，将从各位数开始，个位-十位-百位，把他们分别放入ArrayList<ArrayList>的集合的集合中。遍历即为结果。
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/BasicSort.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/sort/BasicSort.java

### 算法分析方法

- 递归法
  - 汉诺塔
  思想：移动柱子上的盘子。
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/HaNota.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/HaNota.java

  - 二分查找法
  思想：递归左右指针，直到找到mid
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/search/BinarySearch.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/search/BinarySearch.java

  - 欧几里得算法
  思想：求最大公约数M N M>N 等价于N M%N
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/Gcd.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/Gcd.java

  - 阶乘求解
  思想：递归
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/CallNFact.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/CallNFact.java

- 穷举法
  - 泊松分酒
  思想：倒出一定数量的酒；
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/ShareWine.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/recur/ShareWine.java

- 贪心算法
  - 背包问题
  思想：得到最好的价值最大化的结果
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/PackageProblem.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/PackageProblem.java

- 分治法
  思想：大问题变小，小问题化了；
  - n个球队n-1天进行的循环赛比赛
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/SportsSchedule.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/SportsSchedule.java

  - 寻找棋盘特殊位置
  找出棋盘的特殊位置，其他用L覆盖
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/ChessBoradProblem.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/ChessBoradProblem.java

- 动态规划法
  思想：根据现阶段有效情况，执行下阶段操作
  - 求最长公共子序列
  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/LCS.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/LCS.java

- 迭代法

  - Fibonacci数列

  [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/Fibonacci.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/Fibonacci.java

- 回溯法
  - 八皇后
    思想：在棋盘上放8个皇后，对角线和平行线上不能有皇后，放位置的时候，放到最后发现放不下了，回退再找合适的结果。
    [代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/Queen.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/Queen.java

### 其他

- 约瑟夫杀人算法

[代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/Josephus.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/Josephus.java

- 大数相乘

[代码](https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/BigCount.java)：https://github.com/qiuyadongsite/show-my-code/blob/master/show-me-common/src/main/java/com/qyd/learn/show/algorithm/others/BigCount.java

看代码查看原文，或者查看https://qiuyadongsite.github.io/

下一篇再加深。
