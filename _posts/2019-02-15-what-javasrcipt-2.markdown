---
layout: post
title:  JavaScirpt学习笔记二
date:   2019-02-15 23:25:12 +08:00
category: 前端学习
tags: JavaScript
comments: true
---

* content
{:toc}

JavaScript一种直译式脚本语言，是一种动态类型、弱类型、基于原型的语言，内置支持类型。








## 变量、作用域和内存问题

- 变量

  JavaScript 变量松散类型的本质，决定了它只是在特定时间用于保存特定值的一个名字而已。

  `基本数据`类型：Undefined、Null、Boolean、Number 和 `String`。

  `引用类型值`指那些可能由多个值构成的对象:

  对于引用类型的值，我们可以为其添加属性和方法，也可以改变和删除其属性和方法。

  ```
  var person = new Object();
  person.name = "Nicholas";
  alert(person.name); //"Nicholas"

  ```
  对象的复制问题：

  复制操作结束后，两个变量实际上将引用同一个对象。因此，改变其中一个变量，就会影响另一个变量，如下面的例子所示：

  ```
  var obj1 = new Object();
  var obj2 = obj1;
  obj1.name = "Nicholas";
  alert(obj2.name); //"Nicholas"

  ```

  但是当对象作为函数参数传递时，值不会改变。

  1) 对象传递指向同一个对象
  ```
function setName(obj) {
 obj.name = "Nicholas";
}
var person = new Object();
setName(person);
alert(person.name); //"Nicholas"

  ```

  2）对象传递重新赋值

  ```
  function setName(obj) {
 obj.name = "Nicholas";
 obj = new Object();
 obj.name = "Greg";
}
var person = new Object();
setName(person);
alert(person.name); //"Nicholas"

  ```

  分析：这个例子与前一个例子的唯一区别，就是在 setName()函数中添加了两行代码：一行代码为 obj重新定义了一个对象，另一行代码为该对象定义了一个带有不同值的 name 属性。在把 person 传递给setName()后，其 name 属性被设置为"Nicholas"。然后，又将一个新对象赋给变量 obj，同时将其 name属性设置为"Greg"。如果 person 是按引用传递的，那么 person 就会自动被修改为指向其 name 属性值为"Greg"的新对象。但是，当接下来再访问 person.name 时，显示的值仍然是"Nicholas"。这说明即使在函数内部修改了参数的值，但原始的引用仍然保持未变。实际上，当在函数内部重写 obj 时，这个变量引用的就是一个局部对象了。而这个局部对象会在函数执行完毕后立即被销毁。

  - 作用域

  没有块级作用域

  ```
  if (true) {
 var color = "blue";
}
alert(color); //"blue"

for (var i=0; i < 10; i++){
 doSomething(i);
}
alert(i); //10

  ```
  >>在编写 JavaScript 代码的过程中，不声明而直接初始化变量是一个常见的错误做法，因为这样可能会导致意外。我们建议在初始化变量之前，一定要先声明，这样就可以避免类似问题。在严格模式下，初始化未经声明的变量会导致错误。

- 查询标识符

  ```
var color = "blue";
function getColor(){
 return color;
}
alert(getColor()); //"blue"
//首先，搜索 getColor()的变量对象，查找其中是否包含一个名为 color 的标识符。在没有找到的情况下，搜索继续到下一个变量对象（全局环境的变量对象），然后在那里找到了名为color 的标识符。

//最后在windows对象中找到
  ```
  对比上例子：

  ```
  var color = "blue";
function getColor(){
 var color = "red";
 return color;
}
alert(getColor()); //"red"
//修改后的代码在 getColor()函数中声明了一个名为 color 的局部变量。调用函数时，该变量就会被声明。而当函数中的第二行代码执行时，意味着必须找到并返回变量 color 的值。搜索过程首先从局部环境中开始，而且在这里发现了一个名为 color 的变量，其值为"red"。因为变量已经找到了，所以搜索即行停止，return 语句就使用这个局部变量，并为函数会返回"red"。也就是说，任何位于局部变量 color 的声明之后的代码，如果不使用 window.color 都无法访问全局 color变量。

  ```
  >>变量查询也不是没有代价的。很明显，访问局部变量要比访问全局变量更快，因为不用向上搜索作用域链。JavaScript 引擎在优化标识符查询方面做得不错，因此这个差别在将来恐怕就可以忽略不计了。

- 总结

  JavaScript 变量可以用来保存两种类型的值：基本类型值和引用类型值。基本类型的值源自以下 5种基本数据类型：Undefined、Null、Boolean、Number 和 String。基本类型值和引用类型值具有以下特点：

  基本类型值在内存中占据固定大小的空间，因此被保存在栈内存中；

  从一个变量向另一个变量复制基本类型的值，会创建这个值的一个副本；

  引用类型的值是对象，保存在堆内存中；

  包含引用类型值的变量实际上包含的并不是对象本身，而是一个指向该对象的指针；

  从一个变量向另一个变量复制引用类型的值，复制的其实是指针，因此两个变量最终都指向同一个对象；

  确定一个值是哪种基本类型可以使用 typeof 操作符，而确定一个值是哪种引用类型可以使用instanceof 操作符。

  所有变量（包括基本类型和引用类型）都存在于一个执行环境（也称为作用域）当中，这个执行环境决定了变量的生命周期，以及哪一部分代码可以访问其中的变量。以下是关于执行环境的几点总结：

  执行环境有全局执行环境（也称为全局环境）和函数执行环境之分；

  每次进入一个新执行环境，都会创建一个用于搜索变量和函数的作用域链；

  函数的局部环境不仅有权访问函数作用域中的变量，而且有权访问其包含（父）环境，乃至全局环境；

  全局环境只能访问在全局环境中定义的变量和函数，而不能直接访问局部环境中的任何数据；

  变量的执行环境有助于确定应该何时释放内存。
  JavaScript 是一门具有自动垃圾收集机制的编程语言，开发人员不必关心内存分配和回收问题。可以对 JavaScript 的垃圾收集例程作如下总结。

  离开作用域的值将被自动标记为可以回收，因此将在垃圾收集期间被删除。

  “标记清除”是目前主流的垃圾收集算法，这种算法的思想是给当前不使用的值加上标记，然后再回收其内存。

  另一种垃圾收集算法是“引用计数”，这种算法的思想是跟踪记录所有值被引用的次数。JavaScript引擎目前都不再使用这种算法；但在 IE 中访问非原生 JavaScript 对象（如 DOM 元素）时，这种算法仍然可能会导致问题。

  当代码中存在循环引用现象时，“引用计数”算法就会导致问题。

  解除变量的引用不仅有助于消除循环引用现象，而且对垃圾收集也有好处。为了确保有效地回收内存，应该及时解除不再使用的全局对象、全局对象属性以及循环引用变量的引用  

## 引用类型

- Object简单使用

```JavaScript
var person = new Object();
person.name = "Nicholas";
person.age = 29;
//更加推荐
var person = {
 name : "Nicholas",
 age : 29
};

//更加推荐
alert(person["name"]); //"Nicholas"
alert(person.name); //"Nicholas"


```
- Array 类型

```JavaScript
var colors = new Array();
var colors = new Array(20);
var colors = new Array("red", "blue", "green");
var colors = new Array(3); // 创建一个包含 3 项的数组
var names = new Array("Greg"); // 创建一个包含 1 项，即字符串"Greg"的数组
//省略 new 操作符的
var colors = Array(3); // 创建一个包含 3 项的数组
var names = Array("Greg"); // 创建一个包含 1 项，即字符串"Greg"的数组
//第二种基本方式是:
var colors = ["red", "blue", "green"]; // 创建一个包含 3 个字符串的数组
var names = []; // 创建一个空数组
var values = [1,2,]; // 不要这样！这样会创建一个包含 2 或 3 项的数组
var options = [,,,,,]; // 不要这样！这样会创建一个包含 5 或 6 项的数组

var colors = ["red", "blue", "green"]; // 创建一个包含 3 个字符串的数组
var names = []; // 创建一个空数组
alert(colors.length); //3
alert(names.length); //0
//length不是只读的，可以修改从而删除元素
var colors = ["red", "blue", "green"]; // 创建一个包含 3 个字符串的数组
colors.length = 2;
alert(colors[2]); //undefined

var colors = ["red", "blue", "green"]; // 创建一个包含 3 个字符串的数组
colors.length = 4;
alert(colors[3]); //undefined

var colors = ["red", "blue", "green"]; // 创建一个包含 3 个字符串的数组
colors[colors.length] = "black"; //（在位置 3）添加一种颜色
colors[colors.length] = "brown"; //（在位置 4）再添加一种颜色

```

  检测数组：
  ```
  if (value instanceof Array){
 //对数组执行某些操作
}

if (Array.isArray(value)){
 //对数组执行某些操作
}


  ```

  转换方法:

  调用数组的 toString()方法会返回由数组中每个值的字符串形式拼接而成的一个以逗号分隔的字符串。

  ```
  //常用一
  var colors = ["red", "blue", "green"]; // 创建一个包含 3 个字符串的数组
alert(colors.toString()); // red,blue,green
alert(colors.valueOf()); // red,blue,green
alert(colors); // red,blue,green
//使用二
var person1 = {
 toLocaleString : function () {
 return "Nikolaos";
 },

 toString : function() {
 return "Nicholas";
 }
};
var person2 = {
 toLocaleString : function () {
 return "Grigorios";
 },

 toString : function() {
 return "Greg";
 }
};
var people = [person1, person2];
alert(people); //Nicholas,Greg
alert(people.toString()); //Nicholas,Greg
alert(people.toLocaleString()); //Nikolaos,Grigorios

//使用join
var colors = ["red", "green", "blue"];
alert(colors.join(",")); //red,green,blue
alert(colors.join("||")); //red||green||blue
//join注意如果数组中的某一项的值是 null 或者 undefined，那么该值在 join()、
//toLocaleString()、toString()和 valueOf()方法返回的结果中以空字符串表示。

  ```

  栈方法:

  ```
  var colors = new Array(); // 创建一个数组
var count = colors.push("red", "green"); // 推入两项
alert(count); //2
count = colors.push("black"); // 推入另一项
alert(count); //3
var item = colors.pop(); // 取得最后一项
alert(item); //"black"
alert(colors.length); //2

var colors = ["red", "blue"];
colors.push("brown"); // 添加另一项
colors[3] = "black"; // 添加一项
alert(colors.length); // 4
var item = colors.pop(); // 取得最后一项
alert(item); //"black"

  ```

  队列方法:

  ```
  var colors = new Array(); //创建一个数组
var count = colors.push("red", "green"); //推入两项
alert(count); //2
count = colors.push("black"); //推入另一项
alert(count); //3
var item = colors.shift(); //取得第一项
alert(item); //"red"
alert(colors.length); //2

var colors = new Array(); //创建一个数组
var count = colors.unshift("red", "green"); //推入两项
alert(count); //2

count = colors.unshift("black"); //推入另一项
alert(count); //3
var item = colors.pop(); //取得最后一项
alert(item); //"green"
alert(colors.length); //2
//这个例子创建了一个数组并使用 unshift()方法先后推入了 3 个值。首先是"red"和"green"，然后是"black"，数组中各项的顺序为"black"、"red"、"green"。在调用 pop()方法时，移除并返回的是最后一项，即"green"。
  ```

  重排序方法:

  ```
  //反转
  var values = [1, 2, 3, 4, 5];
values.reverse();
alert(values); //5,4,3,2,1
  //排序 ，sort()方法比较的也是字符串，
  var values = [0, 1, 5, 10, 15];
values.sort();
alert(values); //0,1,10,15,5

//自己实现排序

function compare(value1, value2) {
 if (value1 < value2) {
 return -1;
 } else if (value1 > value2) {
 return 1;
 } else {
 return 0;
 }
}
//或者
function compare(value1, value2){
 return value2 - value1;
}
var values = [0, 1, 5, 10, 15];
values.sort(compare);
alert(values); //0,1,5,10,15


  ```

  操作方法：
  ```
  var colors = ["red", "green", "blue"];
var colors2 = colors.concat("yellow", ["black", "brown"]);
alert(colors); //red,green,blue
alert(colors2); //red,green,blue,yellow,black,brown

var colors2 = colors.slice(1);
var colors3 = colors.slice(1,4);
alert(colors2); //green,blue,yellow,purple
alert(colors3); //green,blue,yellow
//如果 slice()方法的参数中有一个负数，则用数组长度加上该数来确定相应的位置。例如，在一个包含 5 项的数组上调用 slice(-2,-1)与调用 slice(3,4)得到的结果相同。如果结束位置小于起始位置，则返回空数组

  //splice()的主要用途是向数组的中部插入项

  var colors = ["red", "green", "blue"];
var removed = colors.splice(0,1); // 删除第一项
alert(colors); // green,blue
alert(removed); // red，返回的数组中只包含一项
removed = colors.splice(1, 0, "yellow", "orange"); // 从位置 1 开始插入两项
alert(colors); // green,yellow,orange,blue
alert(removed); // 返回的是一个空数组
removed = colors.splice(1, 1, "red", "purple"); // 插入两项，删除一项
alert(colors); // green,red,purple,orange,blue
alert(removed); // yellow，返回的数组中只包含一项

  ```

  位置方法:

  ```
  var numbers = [1,2,3,4,5,4,3,2,1];
   alert(numbers.indexOf(4)); //3

  ```

  迭代方法:

  每个方法都接收两个参数：要在每一项上运行的函数和（可选的）运行该函数的作用域对象——影响 this 的值。

  以下是这 5 个迭代方法的作用。
  every()：对数组中的每一项运行给定函数，如果该函数对每一项都返回 true，则返回 true。
  filter()：对数组中的每一项运行给定函数，返回该函数会返回 true 的项组成的数组。
  forEach()：对数组中的每一项运行给定函数。这个方法没有返回值。
  map()：对数组中的每一项运行给定函数，返回每次函数调用的结果组成的数组。
  some()：对数组中的每一项运行给定函数，如果该函数对任一项返回 true，则返回 true。
  以上方法都不会修改数组中的包含的值。

  ```
  var numbers = [1,2,3,4,5,4,3,2,1];
var everyResult = numbers.every(function(item, index, array){
 return (item > 2);
});
alert(everyResult); //false
var someResult = numbers.some(function(item, index, array){
 return (item > 2);
});
alert(someResult); //true

var numbers = [1,2,3,4,5,4,3,2,1];
var filterResult = numbers.filter(function(item, index, array){
 return (item > 2);
});
alert(filterResult); //[3,4,5,4,3]

var numbers = [1,2,3,4,5,4,3,2,1];
var mapResult = numbers.map(function(item, index, array){
 return item * 2;
});
alert(mapResult); //[2,4,6,8,10,8,6,4,2]

var numbers = [1,2,3,4,5,4,3,2,1];
numbers.forEach(function(item, index, array){
 //执行某些操作
});

  ```

  归并方法:

  ```
  var values = [1,2,3,4,5];
var sum = values.reduce(function(prev, cur, index, array){
 return prev + cur;
});
alert(sum); //15

  ```
- Date 类型

  Date类型使用自 UTC（Coordinated Universal Time，国际协调时间）1970 年 1 月 1 日午夜（零时）开始经过的毫秒数来保存日期。

  ```
  //取得开始时间
var start = +new Date();
//调用函数
doSomething();
//取得停止时间
var stop = +new Date(),
 result = stop - start;

  ```
  日期格式化方法:

  ```
  getTime() 返回表示日期的毫秒数；与valueOf()方法返回的值相同
setTime(毫秒) 以毫秒数设置日期，会改变整个日期
getFullYear() 取得4位数的年份（如2007而非仅07）
getUTCFullYear() 返回UTC日期的4位数年份
setFullYear(年) 设置日期的年份。传入的年份值必须是4位数字（如2007而非仅07）
setUTCFullYear(年) 设置UTC日期的年份。传入的年份值必须是4位数字（如2007而非仅07）
getMonth() 返回日期中的月份，其中0表示一月，11表示十二月
getUTCMonth() 返回UTC日期中的月份，其中0表示一月，11表示十二月
setMonth(月) 设置日期的月份。传入的月份值必须大于0，超过11则增加年份
setUTCMonth(月) 设置UTC日期的月份。传入的月份值必须大于0，超过11则增加年份
getDate() 返回日期月份中的天数（1到31）
getUTCDate() 返回UTC日期月份中的天数（1到31）
setDate(日) 设置日期月份中的天数。如果传入的值超过了该月中应有的天数，则增加月份
setUTCDate(日) 设置UTC日期月份中的天数。如果传入的值超过了该月中应有的天数，则增加月份
getDay() 返回日期中星期的星期几（其中0表示星期日，6表示星期六）
getUTCDay() 返回UTC日期中星期的星期几（其中0表示星期日，6表示星期六）
getHours() 返回日期中的小时数（0到23）
getUTCHours() 返回UTC日期中的小时数（0到23）
setHours(时) 设置日期中的小时数。传入的值超过了23则增加月份中的天数
setUTCHours(时) 设置UTC日期中的小时数。传入的值超过了23则增加月份中的天数
getMinutes() 返回日期中的分钟数（0到59）
getUTCMinutes() 返回UTC日期中的分钟数（0到59）
setMinutes(分) 设置日期中的分钟数。传入的值超过59则增加小时数
setUTCMinutes(分) 设置UTC日期中的分钟数。传入的值超过59则增加小时数
getSeconds() 返回日期中的秒数（0到59）
getUTCSeconds() 返回UTC日期中的秒数（0到59）
setSeconds(秒) 设置日期中的秒数。传入的值超过了59会增加分钟数
setUTCSeconds(秒) 设置UTC日期中的秒数。传入的值超过了59会增加分钟数
getMilliseconds() 返回日期中的毫秒数
getUTCMilliseconds() 返回UTC日期中的毫秒数
setMilliseconds(毫秒) 设置日期中的毫秒数
...


  ```

- RegExp 类型

  格式：

  ```
    var expression = / pattern / flags ;

    //（pattern）
 //g：表示全局（global）模式，即模式将被应用于所有字符串，而非在发现第一个匹配项时立即停止；
 //i：表示不区分大小写（case-insensitive）模式，即在确定匹配项时忽略模式与字符串的大小写；
 //m：表示多行（multiline）模式，即在到达一行文本末尾时还会继续查找下一行中是否存在与模式匹配的项。

 /*
* 匹配字符串中所有"at"的实例
*/
var pattern1 = /at/g;
/*
* 匹配第一个"bat"或"cat"，不区分大小写
*/
var pattern2 = /[bc]at/i;
/*
* 匹配所有以"at"结尾的 3 个字符的组合，不区分大小写
*/
var pattern3 = /.at/gi;

/*
* 匹配第一个"bat"或"cat"，不区分大小写
*/
var pattern1 = /[bc]at/i;
/*
* 匹配第一个" [bc]at"，不区分大小写
*/
var pattern2 = /\[bc\]at/i;
/*
* 匹配所有以"at"结尾的 3 个字符的组合，不区分大小写
*/
var pattern3 = /.at/gi;
/*
* 匹配所有".at"，不区分大小写
*/
var pattern4 = /\.at/gi;

/*
* 匹配第一个"bat"或"cat"，不区分大小
*/
var pattern1 = /[bc]at/i;
/*
* 与 pattern1 相同，只不过是使用构造函数创建的
*/
var pattern2 = new RegExp("[bc]at", "i");



  ```

  此处不多深究。
