---
layout: post
title:  线性表
date:   2019-02-18 20:45:12 +08:00
category: 算法
tags: 线性表
comments: true
---

* content
{:toc}

线性表是一种逻辑结构，相同数据类型的n个数据元素的有限序列，除第一个元素外，每个元素有且仅有一个直接前驱，除最后一个元素外，每个元素有且仅有一个直接后继。








## 线性表的概念

- 特点

  元素个数有限

  逻辑上元素有先后次序

  数据类型相同

  仅讨论元素间的逻辑关系

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/linetable.png)

| | 数组 | 链表     |
| :------------- | :------------- | :------------- |
| 优点 | 随机访问性强、查找速度快 |   插入删除速度快、 内存利用率高，不会浪费内存、 大小没有固定，拓展很灵活。     |
| 缺点 | 插入和删除效率低、可能浪费内存、内存空间要求高，必须有足够的连续内存空间。数组大小固定，不能动态拓展 | 不能随机查找，必须从第一个开始遍历，查找效率低  |

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/linetable1.png)

- 循环单链表

  与单链表的区别在于，表中最后一个节点的指针不为null，而改为指向头结点（第一个节点），从而整个链表形成一个环。判断循环单链表是否为空，判断是否等于头指针。只有一个尾指针的循环单链表，可以很方便的操作表头和表尾，因为尾指针的后继就是头指针O(1) 。

- 循环双链表

  与双链表的区别在于，头结点的prior指针指向尾节点，尾节点的next指针指向头结点。

- 常见面试题
  - 归并排序
  应该算是链表排序最佳的选择了，保证了最好和最坏时间复杂度都是nlogn，而且它在数组排序中广受诟病的空间复杂度在链表排序中也从O(n)降到了O(1)。

  ```java

  public class Leetcode148 {
    public static void main(String[] args) {
        ListNode h1=new ListNode(3);
        ListNode h2=new ListNode(1);
        ListNode h3=new ListNode(2);
        ListNode h4=new ListNode(4);
        h1.next=h2;
        h2.next=h3;
        h3.next=h4;

        System.out.printf(Solution.sortList(h1)+"");

    }
}
class Solution {
    public static ListNode sortList(ListNode head) {
        if (head == null || head.next == null) {
            return head;
        }
        ListNode newListNode=null;
        ListNode quickNode=head;
        ListNode slowNode=head;
        ListNode prev = null;


        while (quickNode!=null && quickNode.next!=null){
            prev=slowNode;
            slowNode=slowNode.next;
            quickNode=quickNode.next.next;
        }


        // 注意断链
        prev.next = null;

        ListNode left = sortList(head);
        ListNode right = sortList(slowNode);
        return mergeTwoLists(left, right);

    }
    // 递归实现链表排序
    public static ListNode  mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode res = null;
        if (l1 == null)
            return l2;
        if (l2 == null)
            return l1;
        if (l1.val <= l2.val) {
            res = l1;
            l1.next = mergeTwoLists(l1.next, l2);
        } else {
            res = l2;
            l2.next = mergeTwoLists(l1, l2.next);
        }
        return res;
    }

}
class ListNode {
          int val;
          ListNode next;
          ListNode(int x) { val = x; }

    @Override
    public String toString() {
              String retStr="";
              ListNode cur=this;

              while(cur!=null){
                  retStr+=cur.val+",";
                  cur=cur.next;
              }
        return retStr;
    }


  ```
