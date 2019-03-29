---
layout: post
title:  树-LeetCode算法题库
date:   2019-03-29 21:55:12 +08:00
category: 算法
tags: 树
comments: true
---

* content
{:toc}

树，为了更加熟悉使用，这里将在LeetCode收集树的一些题。








## LeetCode-94-mid(二叉树的中序遍历)

给定一个二叉树，返回它的中序 遍历。

示例:

```

输入: [1,null,2,3]
   1
    \
     2
    /
   3

输出: [1,3,2]

```

```java

public class LeetCode94 {

    public static void main(String[] args) {
        TreeNode94 head= new TreeNode94(1);
        head.left=null;
        head.right= new TreeNode94(2);
        head.right.left=new TreeNode94(3);
        List<Integer> list =(new Solution94()).inorderTraversal(head);
        for (int i = 0; i < list.size(); i++) {
        System.out.printf(list.get(i)+",");
        }
    }
}
class Solution94 {
    public List<Integer> inorderTraversal(TreeNode94 root) {
        List<Integer> list=new ArrayList<>();

        Stack<TreeNode94> stack= new Stack<>();

        TreeNode94 cur=root;
        while (cur!=null|| !stack.isEmpty()){
            if(cur!=null){
                stack.push(cur);
                cur=cur.left;
            }else{
                cur = stack.pop();
                list.add(cur.val);
                cur= cur.right;
            }
        }
        return  list;
    }
}
 class TreeNode94 {
      int val;
      TreeNode94 left;
      TreeNode94 right;
      TreeNode94(int x) { val = x; }
 }

```
