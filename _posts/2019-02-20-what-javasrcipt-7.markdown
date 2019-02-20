---
layout: post
title:  JavaScirpt学习之BOM
date:   2019-02-20 22:45:12 +08:00
category: 前端学习
tags: JavaScript
comments: true
---

* content
{:toc}

如果要在 Web 中使用 JavaScript，那么 BOM（浏览器对象模型）则无疑才是真正的核心。








## window 对象

window 对象有双重角色

它既是通过 JavaScript 访问浏览器窗口的一个接口，又是 ECMAScript 规定的 Global 对象。这意味着在网页中定义的任何一个对象、变量和函数，都以 window 作为其 Global 对象，因此有权访问parseInt()等方法。

- 全局作用域

```js

var age = 29;
function sayAge(){
 alert(this.age);
}
alert(window.age); //29
sayAge(); //29
window.sayAge(); //29
// 定义全局变量与在 window 对象上直接定义属性还是有一点差别：全局变量不能通过 delete 操作符删除，而直接在 window 对象上的定义的属性可以。
var age = 29;
window.color = "red";
//在 IE < 9 时抛出错误，在其他所有浏览器中都返回 false
delete window.age;
//在 IE < 9 时抛出错误，在其他所有浏览器中都返回 true
delete window.color; //returns true
alert(window.age); //29
alert(window.color); //undefined

//尝试访问未声明的变量会抛出错误，但是通过查询 window 对象，可以知道某个可能未声明的变量是否存在。例如：
//这里会抛出错误，因为 oldValue 未定义
var newValue = oldValue;
//这里不会抛出错误，因为这是一次属性查询
//newValue 的值是 undefined
var newValue = window.oldValue;

```

- 窗口关系及框架

如果页面中包含框架，则每个框架都拥有自己的 window 对象，并且保存在 frames 集合中。

```html

<html>
 <head>
 <title>Frameset Example</title>
 </head>
 <frameset rows="160,*">
 <frame src="frame.htm" name="topFrame">
 <frameset cols="50%,50%">
 <frame src="anotherframe.htm" name="leftFrame">
 <frame src="yetanotherframe.htm" name="rightFrame">
 </frameset>
 </frameset>
</html>


```

可以通过window.frames[0]或者 window.frames["topFrame"]来引用上方的框架。恐怕你最好使用`top` 而非 `window` 来引用这些框架（例如，通过 top.frames[0]）。

`top` 对象始终指向最高（最外）层的框架，也就是浏览器窗口。使用它可以确保在一个框架中正确地访问另一个框架。因为对于在一个框架中编写的任何代码来说，其中的 window 对象指向的都是那个框架的特定实例，而非最高层的框架。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/topiframe.png)

与 top 相对的另一个 window 对象是 parent。顾名思义，parent（父）对象始终指向当前框架的直接上层框架。在某些情况下，parent 有可能等于 top；但在没有框架的情况下，parent 一定等于top（此时它们都等于 window）。

- 窗口位置

使用下列代码可以跨浏览器取得窗口左边和上边的位置。

```js
var leftPos = (typeof window.screenLeft == "number") ?
 window.screenLeft : window.screenX;
var topPos = (typeof window.screenTop == "number") ?
 window.screenTop : window.screenY;


```

- 窗口大小

虽然最终无法确定浏览器窗口本身的大小，但却可以取得页面视口的大小，如下所示:

```js
var pageWidth = window.innerWidth,
 pageHeight = window.innerHeight;

if (typeof pageWidth != "number"){
 if (document.compatMode == "CSS1Compat"){
 pageWidth = document.documentElement.clientWidth;
 pageHeight = document.documentElement.clientHeight;
 } else {
 pageWidth = document.body.clientWidth;
 pageHeight = document.body.clientHeight;
 }
}


```

使用 resizeTo()和 resizeBy()方法可以调整浏览器窗口的大小。

- 导航和打开窗口

使用 window.open()方法既可以导航到一个特定的 URL，也可以打开一个新的浏览器窗口。

可以接收 4 个参数：要加载的 URL、窗口目标、一个特性字符串以及一个表示新页面是否取代浏览器历史记录中当前加载页面的布尔值。

```js
//等同于< a href="http://www.wrox.com" target="topFrame"></a>
window.open("http://www.wrox.com/", "topFrame");

//此外，第二个参数也可以是下列任何一个特殊的窗口名称：_self、_parent、_top 或_blank。

```

- 间歇调用和超时调用

```js

//不建议传递字符串！
setTimeout("alert('Hello world!') ", 1000);
//推荐的调用方式
setTimeout(function() {
 alert("Hello world!");
}, 1000);

//设置超时调用
var timeoutId = setTimeout(function() {
 alert("Hello world!");
}, 1000);
//注意：把它取消
clearTimeout(timeoutId);


//不建议传递字符串！
setInterval ("alert('Hello world!') ", 10000);
//推荐的调用方式
setInterval (function() {
 alert("Hello world!");
}, 10000);


var num = 0;
var max = 10;
var intervalId = null;
function incrementNumber() {
  num++;
  //如果执行次数达到了 max 设定的值，则取消后续尚未执行的调
  if (num == max) {
  clearInterval(intervalId);
  alert("Done");
  }
}
intervalId = setInterval(incrementNumber, 500);

var num = 0;
var max = 10;
function incrementNumber() {
 num++;
 //如果执行次数未达到 max 设定的值，则设置另一次超时调用
 if (num < max) {
 setTimeout(incrementNumber, 500);
 } else {
 alert("Done");
 }
}
setTimeout(incrementNumber, 500);


///////最好不要使用间歇调用

```

- 系统对话框

浏览器通过 alert()、confirm()和 prompt()方法可以调用系统对话框向用户显示消息。

```js

if (confirm("Are you sure?")) {
 alert("I'm so glad you're sure! ");
} else {
 alert("I'm sorry to hear you're not sure. ");
}
//某种有输入的情况
var result = prompt("What is your name? ", "");
if (result !== null) {
  alert("Welcome, " + result);
}

```
## location 对象

- location 对象的所有属性

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/localtionparam.png)

查询字符串参数：

```js

function getQueryStringArgs(){
 //取得查询字符串并去掉开头的问号
 var qs = (location.search.length > 0 ? location.search.substring(1) : ""),

 //保存数据的对象
 args = {},

 //取得每一项
 items = qs.length ? qs.split("&") : [],
 item = null,
 name = null,
 value = null,
 //在 for 循环中使用
 i = 0,
 len = items.length;
 //逐个将每一项添加到 args 对象中
 for (i=0; i < len; i++){
 item = items[i].split("=");
 name = decodeURIComponent(item[0]);
 value = decodeURIComponent(item[1]);
 if (name.length) {
 args[name] = value;
 }
 }

 return args;
}

//假设查询字符串是?q=javascript&num=10
var args = getQueryStringArgs();
alert(args["q"]); //"javascript"
alert(args["num"]); //"10"
//可见，每个查询字符串参数都成了返回对象的属性。这样就极大地方便了对每个参数的访问。

```

- 位置操作

使用 location 对象可以通过很多方式来改变浏览器的位置。

```js
location.assign("http://www.wrox.com");

window.location = "http://www.wrox.com";
location.href = "http://www.wrox.com";
//在这些改变浏览器位置的方法中，最常用的是设置 location.href 属性。另外，修改location对象的其他属性也可以改变当前加载的页面。下面的例子展示了通过将hash、search、hostname、pathname 和 port 属性设置为新值来改变 URL。

//假设初始 URL 为 http://www.wrox.com/WileyCDA/
//将 URL 修改为"http://www.wrox.com/WileyCDA/#section1"
location.hash = "#section1";
//将 URL 修改为"http://www.wrox.com/WileyCDA/?q=javascript"
location.search = "?q=javascript";
//将 URL 修改为"http://www.yahoo.com/WileyCDA/"
location.hostname = "www.yahoo.com";
//将 URL 修改为"http://www.yahoo.com/mydir/"
location.pathname = "mydir";
//将 URL 修改为"http://www.yahoo.com:8080/WileyCDA/"
location.port = 8080;

```

浏览器的历史记录中就会生成一条新记录，因此用户通过单击“后退”按钮都会导航到前一个页面。要禁用这种行为，可以使用 replace()方法。这个方法只接受一个参数，即要导航到的 URL；结果虽然会导致浏览器位置改变，但不会在历史记录中生成新记录。

```html
<!DOCTYPE html>
<html>
<head>
 <title>You won't be able to get back here</title>
</head>
 <body>
 <p>Enjoy this page for a second, because you won't be coming back here.</p>
 <script type="text/javascript">
 setTimeout(function () {
 location.replace("http://www.wrox.com/");
 }, 1000);
 </script>
</body>
</html>

```

与位置有关的最后一个方法是 reload()，作用是重新加载当前显示的页面。如果调用 reload()时不传递任何参数，页面就会以最有效的方式重新加载。也就是说，如果页面自上次请求以来并没有改变过，页面就会从浏览器缓存中重新加载。如果要强制从服务器重新加载，则需要像下面这样为该方法递参数 true

```js
location.reload(); //重新加载（有可能从缓存中加载）
location.reload(true); //重新加载（从服务器重新加载）

//位于 reload()调用之后的代码可能会也可能不会执行，这要取决于网络延迟或系统资源等因素。为此，最好将 reload()放在代码的最后一行。

```

## navigator 对象

现在已经成为识别客户端浏览器的事实标准。
如：

appCodeName                     浏览器的名称。通常都是Mozilla，即使在非Mozilla浏览器中也是如此

systemLanguage                  操作系统的语言

- 检测插件

```js
//检测插件（在 IE 中无效）
function hasPlugin(name){
 name = name.toLowerCase();
 for (var i=0; i < navigator.plugins.length; i++){
 if (navigator. plugins [i].name.toLowerCase().indexOf(name) > -1){
 return true;
 }
 }
 return false;
}
//检测 Flash
alert(hasPlugin("Flash"));
//检测 QuickTime
alert(hasPlugin("QuickTime"));

```

## screen 对象

JavaScript 中有几个对象在编程中用处不大，而 screen 对象就是其中之一。screen 对象基本上只用来表明客户端的能力，其中包括浏览器窗口外部的显示器的信息，如像素宽度和高度等。每个浏览器中的 screen 对象都包含着各不相同的属性，下表列出了所有属性及支持相应属性的浏览器。

width 屏幕的像素宽度
left 当前屏幕距左边的像素距离
top 当前屏幕距上边的像素距离

window.resizeTo(screen.availWidth, screen.availHeight);

前面曾经提到过，许多浏览器都会禁用调整浏览器窗口大小的能力，因此上面这行代码不一定在所有环境下都有效。

## history 对象

```js
//后退一页
history.go(-1);
//前进一页
history.go(1);
//前进两页
history.go(2);

//跳转到最近的 wrox.com 页面
history.go("wrox.com");
//跳转到最近的 nczonline.net 页面
history.go("nczonline.net");
//后退一页
history.back();
//前进一页
history.forward();

if (history.length == 0){
 //这应该是用户打开窗口后的第一个页面
}  

```
