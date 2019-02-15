---
layout: post
title:  JavaScirpt学习笔记一
date:   2019-02-15 22:25:12 +08:00
category: 前端学习
tags: JavaScript
comments: true
---

* content
{:toc}

JavaScript一种直译式脚本语言，是一种动态类型、弱类型、基于原型的语言，内置支持类型。








# script元素

- script
定义了下列 6 个属性:

defer：可选。表示脚本可以延迟到文档完全被解析和显示之后再执行。只对外部脚本文件有效。IE7 及更早版本对嵌入脚本也支持这个属性。

async：可选。表示应该立即下载脚本，但不应妨碍页面中的其他操作，比如下载其他资源或等待加载其他脚本。只对外部脚本文件有效。<script type="text/javascript" async src="example1.js"></script> 。但与 defer不同的是，标记为 async 的脚本并不保证按照指定它们的先后顺序执行。

charset：可选。很少有人用。

language：已废弃。

src：可选。表示包含要执行代码的外部文件。

type：可选。不过，这个属性并不是必需的，如果没有指定这个属性，则其默认值仍为
text/javascript。

- 注意事项

无论如何包含代码，只要不存在 defer 和 async 属性，浏览器都会按照<script>元素在页面中出现的先后顺序对它们依次进行解析。换句话说，在第一个<script>元素包含的代码解析完成后，第二个<script>包含的代码才会被解析，然后才是第三个、第四个……

现代 Web 应用程序一般都把全部 JavaScript 引用放在<body>元素中页面内容的后面。

- 在XHTML中的用法

这里比较语句 a < b 中的小于号（<）在 XHTML 中将被当作开始一个新标签来解析。

解决方案：

1 用相应的 HTML 实体（&lt;）替换代码中所有的小于号（<）；

2 在 XHTML（XML）中，CData 片段是文档中的一个特殊区域，这个区域中可以包含不需要解析的任意格式的文本内容；

```
<![CDATA[
 ...
]]>

```
- 嵌入代码与外部文件

最好的做法还是尽可能使用外部文件来包含 JavaScript 代码;

可维护性
可缓存
适应未来

- 小结
把 JavaScript 插入到 HTML 页面中要使用<script>元素。使用这个元素可以把 JavaScript 嵌入到HTML 页面中，让脚本与标记混合在一起；也可以包含外部的 JavaScript 文件。而我们需要注意的地方有:

 在包含外部 JavaScript 文件时，必须将 src 属性设置为指向相应文件的 URL。而这个文件既可以是与包含它的页面位于同一个服务器上的文件，也可以是其他任何域中的文件。

 所有<script>元素都会按照它们在页面中出现的先后顺序依次被解析。在不使用 defer 和async 属性的情况下，只有在解析完前面<script>元素中的代码之后，才会开始解析后面<script>元素中的代码。

 由于浏览器会先解析完不使用 defer 属性的<script>元素中的代码，然后再解析后面的内容，所以一般应该把<script>元素放在页面最后，即主要内容后面，</body>标签前面。

 使用 defer 属性可以让脚本在文档完全呈现之后再执行。延迟脚本总是按照指定它们的顺序执行。

 使用 async 属性可以表示当前脚本不必等待其他脚本，也不必阻塞文档呈现。不能保证异步脚本按照它们在页面中出现的顺序执行。

另外，使用<noscript>元素可以指定在不支持脚本的浏览器中显示的替代内容。但在启用了脚本的情况下，浏览器不会显示<noscript>元素中的任何内容。

## 基本概念

- 数据类型
ECMAScript 中有 5 种简单数据类型： Undefined、Null、Boolean、Number和 String。

还有 1种复杂数据类型——Object，Object 本质上是由一组无序的名值对组成的。

- typeof操作符

1 "undefined"——如果这个值未定义

Undefined 类型只有一个值，即特殊的 undefined。在使用 var 声明变量但未对其加以初始化时，这个变量的值就是 undefined;实际上，undefined 值是派生自 null 值的，因此 ECMA-262 规定对它们的相等性测试要返回 true：
alert(null == undefined); //true

2 "boolean"——如果这个值是布尔值；
3  "string"——如果这个值是字符串；
4 "number"——如果这个值是数值

NaN，即非数值（Not a Number）是一个特殊的数值，这个数值用于表示一个本来要返回数值的操作数未返回数值的情况（这样就不会抛出错误了）。
NaN 与任何值都不相等，包括 NaN 本身。
alert(isNaN(NaN)); //true
alert(isNaN(10)); //false（10 是一个数值）
alert(isNaN("10")); //false（可以被转换成数值 10）
alert(isNaN("blue")); //true（不能转换成数值）
alert(isNaN(true)); //false（可以被转换成数值 1）

5  "object"——如果这个值是对象或 null

Null 类型是第二个只有一个值的数据类型，这个特殊的值是 null。从逻辑角度来看，null 值表示一个空对象指针，而这也正是使用 typeof 操作符检测 null 值时会返回"object"的原因;

6  "function"——如果这个值是函数。

- 数值转换

有 3 个函数可以把非数值转换为数值：Number()、parseInt()和 parseFloat()。

Number()函数的转换规则如下。
 如果是 Boolean 值，true 和 false 将分别被转换为 1 和 0。

 如果是数字值，只是简单的传入和返回。

 如果是 null 值，返回 0。

 如果是 undefined，返回 NaN。

 如果是字符串，遵循下列规则：
 如果字符串中只包含数字（包括前面带正号或负号的情况），则将其转换为十进制数值，即"1"会变成 1，"123"会变成 123，而"011"会变成 11（注意：前导的零被忽略了）；
 如果字符串中包含有效的浮点格式，如"1.1"，则将其转换为对应的浮点数值（同样，也会忽略前导零）；
 如果字符串中包含有效的十六进制格式，例如"0xf"，则将其转换为相同大小的十进制整数值；
 如果字符串是空的（不包含任何字符），则将其转换为 0；
 如果字符串中包含除上述格式之外的字符，则将其转换为 NaN。
 如果是对象，则调用对象的 valueOf()方法，然后依照前面的规则转换返回的值。如果转换
的结果是 NaN，则调用对象的 toString()方法，然后再次依照前面的规则转换返回的字符
串值

var num1 = Number("Hello world!"); //NaN
var num2 = Number(""); //0
var num3 = Number("000011"); //11
var num4 = Number(true); //1

parseInt规则：

第一个字符不是数字字符或者负号，parseInt()就会返回 NaN；

例如，"1234blue"会被转换为 1234，因为"blue"会被完全忽略。类似地，"22.5",会被转换为 22，因为小数点并不是有效的数字字符。

String类型：

字符串可以由双引号（"）或单引号（'）表示，因此下面两种字符串的写法都是有效的：

`\n` 换行
`\t `制表
`\b` 空格
`\r `回车
`\f` 进纸
`\\ `斜杠
`\'` 单引号（'），在用单引号表示的字符串中使用。例如：'He said, \'hey.\''
`\"` 双引号（"），在用双引号表示的字符串中使用。例如："He said, \"hey.\""
`\xnn `以十六进制代码nn表示的一个字符（其中n为0～F）。例如，\x41表示"A"
`\unnnn `以十六进制代码nnnn表示的一个Unicode字符（其中n为0～F）。例如，\u03a3表示希腊字符Σ

 如果值有 toString()方法，则调用该方法（没有参数）并返回相应的结果；
 如果值是 null，则返回"null"；
 如果值是 undefined，则返回"undefined"。

Object类型:

var o = new Object();
Object 的每个实例都具有下列属性和方法:

hasOwnProperty(propertyName)：用于检查给定的属性在当前对象实例中（而不是在实例的原型中）是否存在。如：o.hasOwnProperty("name")

propertyIsEnumerable(propertyName)：用于检查给定的属性是否能够使用 for-in 语句（本章后面将会讨论）来枚举。与 hasOwnProperty()方法一样，作为参数的属性名必须以字符串形式指定。

toLocaleString()：返回对象的字符串表示，该字符串与执行环境的地区对应。
toString()：返回对象的字符串表示。
valueOf()：返回对象的字符串、数值或布尔值表示。通常与 toString()方法的返回值相同。

- 语句

for-in 语句是一种精准的迭代语句，可以用来枚举对象的属性。以下是 for-in 语句的语法：
for (var propName in window) {
 document.write(propName);
}


- 函数

推荐的做法是要么让函数始终都返回一个值，要么永远都不要返回值。否则，如果函数有时候返回值，有时候有不返回值，会给调试代码带来不便。

函数不介意传递进来多少个参数，也不在乎传进来参数是什么数据类型。也就是说，即便你定义的函数只接收两个参数，在调用这个函数时也未必一定要传递两个参数。可以传递一个、三个甚至不传递参数，而解析器永远不会有什么怨言。

函数不能像传统意义上那样实现重载。
