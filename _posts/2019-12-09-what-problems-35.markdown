---
layout: post
title:  JDK1.8新特性
date:   2019-12-09 20:52:12 +08:00
category: 语言基础
tags: jdk8
comments: true
---

* content
{:toc}


keys!






## 接口的默认方法

在接口中定义一个default的方法，在匿名内部类或者使用实现类的对象调用。

```java

public class DefaultMethodTest {
    public static void main(String[] args) {
        Formula formula=new Formula() {
            @Override
            public double calculate(int a) {
                return sqrt(a*100);
            }
        };
        System.out.println(formula.calculate(100));
    }
}

```

## Lambda表达式

```java

Collections.sort(names, (String a, String b) -> {
    return b.compareTo(a);
});

Collections.sort(names, (String a, String b) -> b.compareTo(a));

names.sort((a, b) -> b.compareTo(a));

```

Lamda 表达式作用域(Lambda Scopes):

- 可以直接在 lambda 表达式中访问外部的局部变量

- 实例字段和静态变量都有读写访问权限

- 无法从 lambda 表达式中访问默认方法

## 函数式接口

“函数式接口”是指仅仅只包含一个抽象方法,但是可以有多个非抽象方法(也就是上面提到的默认方法)的接口,像这样的接口，可以被隐式转换为lambda表达式。java.lang.Runnable 与 java.util.concurrent.Callable 是函数式接口最典型的两个例子。

```java

new Thread(()->{
                   try {
                       TimeUnit.MILLISECONDS.sleep(1);
                       Thread.interrupted();
                   } catch (InterruptedException e) {
                       e.printStackTrace();
                   }
               },"wait_sleep").start();

```

## 内置函数式接口(Built-in Functional Interfaces)

- Predicate 接口是只有一个参数的返回布尔类型值的 断言型 接口

- Function 接口接受一个参数并生成结果

- Supplier 接口产生给定泛型类型的结果

- Consumer 接口表示要对单个输入参数执行的操作

- Comparator 是老Java中的经典接口

```java

Comparator<Person> comparator = (p1, p2) -> p1.firstName.compareTo(p2.firstName);

Person p1 = new Person("John", "Doe");
Person p2 = new Person("Alice", "Wonderland");

comparator.compare(p1, p2);             // > 0
comparator.reversed().compare(p1, p2);  // < 0

```

## Optionals

Optionals不是函数式接口，而是用于防止 NullPointerException 的漂亮工具

```java

//of（）：为非null的值创建一个Optional
Optional<String> optional = Optional.of("bam");
// isPresent（）： 如果值存在返回true，否则返回false
optional.isPresent();           // true
//get()：如果Optional有值则将其返回，否则抛出NoSuchElementException
optional.get();                 // "bam"
//orElse（）：如果有值则将其返回，否则返回指定的其它值
optional.orElse("fallback");    // "bam"
//ifPresent（）：如果Optional实例有值则为其调用consumer，否则不做处理
optional.ifPresent((s) -> System.out.println(s.charAt(0)));     // "b"

```

## Streams

java.util.Stream 表示能应用在一组元素上一次执行的操作序列

- Filter(过滤)

过滤通过一个predicate接口来过滤并只保留符合条件的元素，该操作属于中间操作

```java

// 测试 Filter(过滤)
       stringList
               .stream()
               .filter((s) -> s.startsWith("a"))
               .forEach(System.out::println);//aaa2 aaa1

```

- Sorted(排序)

排序是一个 中间操作，返回的是排序好后的 Stream

```java

// 测试 Sort (排序)
       stringList
               .stream()
               .sorted()
               .filter((s) -> s.startsWith("a"))
               .forEach(System.out::println);// aaa1 aaa2排序只创建了一个排列好后的Stream，而不会影响原有的数据源

```

- Map(映射)

中间操作 map 会将元素根据指定的 Function 接口来依次将元素转成另外的对象。

```java

// 测试 Map 操作
        stringList
                .stream()
                .map(String::toUpperCase)
                .sorted((a, b) -> b.compareTo(a))
                .forEach(System.out::println);// "DDD2", "DDD1", "CCC", "BBB3", "BBB2", "AAA2", "AAA1"

```

- Match(匹配)

Stream提供了多种匹配操作，允许检测指定的Predicate是否匹配整个Stream。所有的匹配操作都是 最终操作 ，并返回一个 boolean 类型的值。

```java

boolean anyStartsWithA =
                stringList
                        .stream()
                        .anyMatch((s) -> s.startsWith("a"));
        System.out.println(anyStartsWithA);      // true

        boolean allStartsWithA =
                stringList
                        .stream()
                        .allMatch((s) -> s.startsWith("a"));

        System.out.println(allStartsWithA);      // false

        boolean noneStartsWithZ =
                stringList
                        .stream()
                        .noneMatch((s) -> s.startsWith("z"));

        System.out.println(noneStartsWithZ);      // true

```

- Count(计数)

计数是一个 最终操作，返回Stream中元素的个数，返回值类型是 long。

```java

//测试 Count (计数)操作
        long startsWithB =
                stringList
                        .stream()
                        .filter((s) -> s.startsWith("b"))
                        .count();
        System.out.println(startsWithB);    // 3

```

- Reduce(规约)

这是一个 最终操作 ，允许通过指定的函数来讲stream中的多个元素规约为一个元素，规约后的结果是通过Optional 接口表示的：

```java

//测试 Reduce (规约)操作
        Optional<String> reduced =
                stringList
                        .stream()
                        .sorted()
                        .reduce((s1, s2) -> s1 + "#" + s2);

        reduced.ifPresent(System.out::println);//aaa1#aaa2#bbb1#bbb2#bbb3#ccc#ddd1#ddd2

```

其他例子

```java

// 字符串连接，concat = "ABCD"
String concat = Stream.of("A", "B", "C", "D").reduce("", String::concat);
// 求最小值，minValue = -3.0
double minValue = Stream.of(-1.5, 1.0, -3.0, -2.0).reduce(Double.MAX_VALUE, Double::min);
// 求和，sumValue = 10, 有起始值
int sumValue = Stream.of(1, 2, 3, 4).reduce(0, Integer::sum);
// 求和，sumValue = 10, 无起始值
sumValue = Stream.of(1, 2, 3, 4).reduce(Integer::sum).get();
// 过滤，字符串连接，concat = "ace"
concat = Stream.of("a", "B", "c", "D", "e", "F").
 filter(x -> x.compareTo("Z") > 0).
 reduce("", String::concat);

```

## Parallel Streams(并行流)

串行Stream上的操作是在一个线程中依次完成，而并行Stream则是在多个线程上同时执行

```java

//并行排序
long t0 = System.nanoTime();

long count = values.parallelStream().sorted().count();
System.out.println(count);

long t1 = System.nanoTime();

long millis = TimeUnit.NANOSECONDS.toMillis(t1 - t0);
System.out.println(String.format("parallel sort took: %d ms", millis));

```

## Maps

Map 类型不支持 streams，不过Map提供了一些新的有用的方法来处理一些日常任务

 map.keySet().stream(),map.values().stream()和map.entrySet().stream()

 ```java

 //putIfAbsent 阻止我们在null检查时写入额外的代码;forEach接受一个 consumer 来对 map 中的每个元素操作。
 Map<Integer, String> map = new HashMap<>();

 for (int i = 0; i < 10; i++) {
     map.putIfAbsent(i, "val" + i);
 }

 map.forEach((id, val) -> System.out.println(val));//val0 val1 val2 val3 val4 val5 val6 val7 val8 val9

 map.computeIfPresent(3, (num, val) -> val + num);
 map.get(3);             // val33

 map.computeIfPresent(9, (num, val) -> null);
 map.containsKey(9);     // false

 map.computeIfAbsent(23, num -> "val" + num);
 map.containsKey(23);    // true

 map.computeIfAbsent(3, num -> "bam");
 map.get(3);             // val33

 map.remove(3, "val3");
map.get(3);             // val33
map.remove(3, "val33");
map.get(3);

map.getOrDefault(42, "not found");  // not found

map.merge(9, "val9", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9
map.merge(9, "concat", (value, newValue) -> value.concat(newValue));
map.get(9);             // val9concat
//Merge 做的事情是如果键名不存在则插入，否则则对原键对应的值做合并操作并重新插入到map中。

 ```

## Date API(日期相关API)

- Clock

Clock 类提供了访问当前日期和时间的方法，Clock 是时区敏感的，可以用来取代 System.currentTimeMillis() 来获取当前的微秒数。

```java

Clock clock = Clock.systemDefaultZone();
long millis = clock.millis();
System.out.println(millis);//1552379579043
Instant instant = clock.instant();
System.out.println(instant);
Date legacyDate = Date.from(instant); //2019-03-12T08:46:42.588Z
System.out.println(legacyDate);//Tue Mar 12 16:32:59 CST 2019

```

- Timezones(时区)

在新API中时区使用 ZoneId 来表示

```java

System.out.println(ZoneId.getAvailableZoneIds());

ZoneId zone1 = ZoneId.of("Europe/Berlin");
ZoneId zone2 = ZoneId.of("Brazil/East");
System.out.println(zone1.getRules());// ZoneRules[currentStandardOffset=+01:00]
System.out.println(zone2.getRules());// ZoneRules[currentStandardOffset=-03:00]

```

- LocalTime(本地时间)

LocalTime 定义了一个没有时区信息的时间，例如 晚上10点或者 17:30:15

- LocalDate(本地日期)

LocalDate 表示了一个确切的日期，比如 2014-03-11

- LocalDateTime(本地日期时间)

LocalDateTime 同时表示了时间和日期，相当于前两节内容合并到一个对象上了。LocalDateTime 和 LocalTime还有 LocalDate 一样，都是不可变的。LocalDateTime 提供了一些能访问具体字段的方法。

## Annotations(注解)

在Java 8中支持多重注解了

```java

@interface Hints {
    Hint[] value();
}

@Hint("hint1")
@Hint("hint2")
class Person {}

```
