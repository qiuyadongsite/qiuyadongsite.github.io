---
layout: post
title:  链表-LeetCode算法题库
date:   2019-03-29 20:55:12 +08:00
category: 算法
tags: 链表
comments: true
---

* content
{:toc}

链表是线性表，为了更加熟悉使用，这里将在LeetCode收集链表的一些题。








## LeetCode-23-difficult(合并K个排序链表)

合并 k 个排序链表，返回合并后的排序链表。请分析和描述算法的复杂度。

示例:

输入:

```

[
  1->4->5,
  1->3->4,
  2->6
]

```

输出: 1->1->2->3->4->4->5->6


```java

public class LeetCode23 {

    public static void main(String[] args) {
        ListNode list1= new ListNode(1);
        list1.next=new ListNode(4);
        list1.next.next =new ListNode(5);

        ListNode list2= new ListNode(1);
        list2.next=new ListNode(3);
        list2.next.next =new ListNode(4);

        ListNode list3= new ListNode(2);
        list3.next=new ListNode(6);
        ListNode[] lists=new ListNode[]{list1,list2,list3};

        ListNode ret=(new Solution23()).mergeKLists(lists);

        while (ret!=null){
            System.out.printf(ret.val+",");
            ret=ret.next;
        }

    }

}
class Solution23 {

    public ListNode mergeKLists(ListNode[] lists){
       if(lists.length ==0){
           return null;
       }

       if(lists.length ==1){
           return lists[0];
       }

       if (lists.length ==2){
           return mergeTwoLists(lists[0],lists[1]);
       }

       int mid= lists.length/2;

       ListNode[] l1= new ListNode[mid];
       for (int i=0;i< mid;i++){
           l1[i] = lists[i];
       }
       ListNode[] l2= new ListNode[lists.length-mid];
       for (int j=0,k=mid;k<lists.length;k++,j++){
           l2[j]= lists[k];
       }
        return  mergeTwoLists(mergeKLists(l1),mergeKLists(l2));
    }

    private ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if(l1==null){
            return l2;
        }
        if(l2==null){
            return l1;
        }
        ListNode head=null;
        if(l1.val<=l2.val){
            head = l1;
            head.next = mergeTwoLists(l1.next,l2);
        }else{
            head = l2;
            head.next= mergeTwoLists(l1,l2.next);
        }
        return head;
    }

}

```
