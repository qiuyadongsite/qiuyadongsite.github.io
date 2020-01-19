---
layout: post
title:  markdown语法学习
date:   2019-01-11 21:52:12 +08:00
category: 工具学习
tags: markdown 语言使用
comments: true
---

* content
{:toc}

这里总结本博客常用的语法；






## 常用语法总结

- 无序列表：输入-之后输入空格
- 有序列表：输入数字+“.”之后输入空格
- 任务列表：-[空格]空格 文字
- 标题：ctrl+数字
- 表格：ctrl+t
- 生成目录：[TOC]按回车
- 选中一整行：ctrl+l
- 选中单词：ctrl+d
- 选中相同格式的文字：ctrl+e
- 跳转到文章开头：ctrl+home
- 跳转到文章结尾：ctrl+end
- 搜索：ctrl+f
- 替换：ctrl+h
- 引用：输入>之后输入空格
- 代码块：ctrl+alt+f
- 加粗：ctrl+b
- 倾斜：ctrl+i
- 下划线：ctrl+u
- 删除线：alt+shift+5
- 插入图片：直接拖动到指定位置即可或者ctrl+shift+i
- 插入链接：ctrl + k

## 表情

输出表情需要借助 `：`符号。

栗子：`:smile` 显示为 😄,记住是左右两边都要冒号。

使用者可以通过使用`ESC`键触发表情建议补全功能，也可在功能面板启用后自动触发此功能。同时，直接从菜单栏`Edit` -> `Emoji & Symbols`插入UTF8表情符号也是可以的。

或者使用下面的方法

访问网站 <https://emojikeyboard.org/>，找到需要的符号，鼠标左键单击，然后粘贴到需要的地方就行了！🆗



## 数学公式

你可以通过使用**MathJax**来实现*LaTeX*的数学符号的表达。

输入`$$`，然后按下`Enter`键就会弹出一个支持TeX/LaTeX语法的输入框，下面是一个栗子：
$$
\$$
\mathbf{V}_1 \times \mathbf{V}_2 =  \begin{vmatrix} 
\mathbf{i} & \mathbf{j} & \mathbf{k} \\
\frac{\partial X}{\partial u} &  \frac{\partial Y}{\partial u} & 0 \\
\frac{\partial X}{\partial v} &  \frac{\partial Y}{\partial v} & 0 \\
\end{vmatrix}
\$$
$$

## 高亮

想要使用这个功能，需要在设置面板的`Markdown` 栏启动它，之后使用`==`来修饰高亮文本，栗如：

`==highlight==` 显示为 ==highlight== 。

## 行内嵌数学符号[#](https://www.cnblogs.com/hongdada/p/9776547.html#458370953)

想要使用这个功能，需要在设置面板的 `Markdown`栏启用它。然后使用`$`来启动TeX命令，栗如：`$\lim_{x \to \infty} \exp(-x) = 0$` 会以LaTeX的命令形式表达出来。

为了触发行内内嵌数学符号的实时编译你需要：输入`$`然后按下`ESC`键之后输入TeX命令，之后就会弹出一个如图所示的工具提示栏：

[![img](https://pic3.zhimg.com/v2-4033508b043cad96c59ec4edbca92f36_b.gif)](https://pic3.zhimg.com/v2-4033508b043cad96c59ec4edbca92f36_b.gif)

## 下标[#](https://www.cnblogs.com/hongdada/p/9776547.html#3003310097)

想要使用这个功能，需要在设置面板的 `Markdown` 栏启动它，之后使用`~`来修饰下标文本。栗如：

`H~2~O` 和`X~long\ text~` 显示为 H~2~O 和X~long text~ 。

\#### 13.上标

想要使用这个功能，需要在设置面板的 `Markdown` 栏启动它，之后使用`^`来修饰下标文本。栗如：

`X^2^` 显示为 X^2^ 。



### 链接引用与脚注

**链接引用**类似于我们常在论文末尾看到的「参考文献」的写法，你可以通过 `[]:` 的语法来为你的文档加上链接引用。

**脚注**在少数派的文章中也很常见，即某段话结尾右上角标有数字标记，页面底部进行注释的写法。你可以在需要插入脚注标号的位置写 `[^ number ]` ，再在下方通过 `[^ number ]:` 在文档中插入脚注。注意不要遗漏了脚注编号 `number` 前后的空格。

## 参考

https://www.cnblogs.com/hongdada/p/9776547.html#255697297