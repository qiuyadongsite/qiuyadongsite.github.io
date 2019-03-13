---
layout: post
title:  Java 进阶78条学习总结
date:   2019-01-29 23:52:12 +08:00
category: 语言基础
tags: JAVASE
comments: true
---

* content
{:toc}

基础太差，就有了详读《Effective Java中文版 第2版》的冲动，毕竟是Java经典，而且很多思想和案例基于1.5以后，现在也不落伍！

Java之父这样评价者本书“我很希望10年前就拥有这本书。可能有人认为我不需要任何Java方面的书籍，但我需要这本书！”。












#### 前言

---

花了一周的时间详读，将自己收益的东西记录下来，不太明白的东西也记录了一下，希望对学习Java的同仁有用。


#### 一、创建和销毁对象

---
##### 1.考虑用静态工厂方法代替构造器


静态的工厂方法，只是一个返回类的实例的静态方法；
优势：
- 有名称更加见名识义；
- 不必每次调用时都创建一个新对象；
- 可以返回原返回类型的任何子类型的对象；
- 在创建参数化类型的时候，使代码变的更加简洁；
缺点：
- 类如果不含公有的或者受保护的构造器，就不能被子类化；
- 它与其他静态方法没有任何区别；


##### 2.遇到多个构造器参数时考虑用构造器


静态工厂和构造器有个共同的局限性，他们都不能很好的扩展大量的可选参数。当很多可选参数时，程序员习惯使用层叠构造器模式，造成参数很多，还有没必要的赋值，参数越多代码很难编写，并且可读性下降。
还有一种方式是添加javaBean方式，使用setter为每个属性赋值，问题是：构造过程被分到几个调用中，在构造过程中JavaBean可能处于不一致的状态，使用不一致状态的对象时会出错，程序员需要保证一致性。把类做成不可变的可能，需要保证其线程安全。
推荐使用builder模式：
```
//代码冗长，在参数很多时可用，4个或以上参数更适用
public class NutritionFacts {
	private final int servingSize;
	private final int servings;
	private final int calories;
	private final int fat;
	private final int sodium;
	private final int carbohydrate;

	public static class Builder {
		// Required parameters
		private final int servingSize;
		private final int servings;

		// Optional parameters - initialized to default values
		private int calories = 0;
		private int fat = 0;
		private int carbohydrate = 0;
		private int sodium = 0;

		public Builder(int servingSize, int servings) {
			this.servingSize = servingSize;
			this.servings = servings;
		}

		public Builder calories(int val) {
			calories = val;
			return this;
		}

		public Builder fat(int val) {
			fat = val;
			return this;
		}

		public Builder carbohydrate(int val) {
			carbohydrate = val;
			return this;
		}

		public Builder sodium(int val) {
			sodium = val;
			return this;
		}

		public NutritionFacts build() {
			return new NutritionFacts(this);
		}
	}

	private NutritionFacts(Builder builder) {
		servingSize = builder.servingSize;
		servings = builder.servings;
		calories = builder.calories;
		fat = builder.fat;
		sodium = builder.sodium;
		carbohydrate = builder.carbohydrate;
	}

	public static void main(String[] args) {
		NutritionFacts cocaCola = new NutritionFacts.Builder(240, 8)
				.calories(100).sodium(35).carbohydrate(27).build();
	}
}
```
##### 3.用私有构造器或者枚举类型强化Singleton属性
Singleton指仅仅被实例化一次的类。例如本质上唯一的系统组件，窗口管理器。
之前有两种方法实现Singleton:
- one:
公有的静态的最终态的实例对象，构造方法私有化。一旦被实例化，只会有一个Elvis实例，不多也不少。但对于特权用户的AccessibleObject.setAccessible方法，通过反射机制调用私有构造器。如何需要抵御这种工具可以修改构造器。
- two:
公有成员是个静态的工厂方法；
但是：如果该类是可序列化的。为了实现Singleton：
- 1）声明中加上“implements Serializable”;
- 2)声明所有实例域都是瞬时的，并提供一个readResolve方法。
- 自1.5版本之后的第三种方法：只需要编写一个包含单个元素的枚举类型；

##### 4.通过私有构造器强化不可实例化的能力
一些工具类不需要被实例化，因为实例化没有任何意义。

```
public class UtilityClass {
	// Suppress default constructor for noninstantiability
	private UtilityClass() {
		throw new AssertionError();
	}
}
```
##### 5.避免创建不必要的对象
若一个对象具有相同的功能，需要重用性。如果对象始终是不可变的，那它始终可以被重用。
- 1）使用静态工厂
- 2）静态的初始化器

##### 6.消除过期的对象引用
如果一个栈先是增长，然后再收缩，那么，从栈中弹出来的对象将不会被当做垃圾回收，即使使用栈的程序不在引用这些对象，他们也不会被回收。这是因为，栈内部维护着对这些对象的过期引用。
解决方法可以在：
elements[size]=null;

- 一般而言，只要类是自己管理内存，程序员就应该警惕内存泄漏问题。一般元素被释放掉，则该元素中包含的任何对象引用都应该被清空。
-
- 内存泄漏的另一个常见来源是缓存。
-
- 内存泄漏的第三个常见来源是监听器和其他回调。

##### 7.避免适用终结方法
- 1：在Java中，一般用try-finally块来完成类似的工作。终结方法的缺点在于不能保证会被及时的执行。java语言规范不仅不能保证终结方法会被及时执行，而且根本就不保证它们会被执行。结论：不应该依赖终结方法来更新重要的持久状态。如：依赖终结方法来解放共享资源上的永久锁，很容易让整个分布式系统垮掉。
- 2：如果为未被捕获的异常在终结过程中被抛出来，那么这种异常可以被忽略，并且该对象的终结过程被终止。
- 3：终结方法严重的性能损失。

合理使用：

- 1：显示的终止方法，与try-finally一起使用，经典例子是Inputstream的close方法。
- 2：本地对等体，子类终结方法中要显示的调用超类中的终结方法。

#### 二、对于所有对象都通用的方法

---
讲述何时以及如何覆盖非final的Object的方法；
##### 8.覆盖equals时请遵循通用约定
不需要使用的情况：
- 1）类的每个实例本质上都是唯一的：如Thread;
- 2)不关心类是否提供了“逻辑相等”的测试功能；
- 3）超类已经覆盖了equals，从超类继承过来的行为对于子类也是合适的；
- 4）类是私有的或者包级私有的，可以确定它的equals方法永远不会被调用；
使用的情况：
- 1）值类情形，如Integer、Date、枚举类型。
- 覆盖原则：自反性、对称性、传递性、一致性；
总结：
- 1）使用==操作符检查“参数是否为这个对象的引用”。如果是返回true。这只不过是一种性能优化，如果比较操作有可能很昂贵，就值得这么做。
- 2）使用instanceof操作符检查参数是否为正确的类型。如果不是返回false。
- 3）把参数转换成正确的类型。因为转换之前进行过instanof测试，所以确保会成功。
- 4）对于该类的每个“关键”域，检查参数中的域是否是该对象中对应的域相互匹配。全部测试通过，返回true;
- 5)当你编写完成equals方法之后，问三个问题：它是对称的、传递的、一致的？
告诫：
- 1）覆盖equals时总要覆盖hashCode；
- 2)不要企图让equals方法过于智能；
- 3）不要将equals声明中的Object对象替换为其他的类型。

##### 9.覆盖equals时总要覆盖hashCode
一个常见的错误根源是没有覆盖该方法，如果不覆盖的话，导致无法结合所有基于散咧的集合一起正常运作，这样的集合包括HashMap、HashSet和Hashtable。
原因：如果不覆盖，导致两个相等的实例具有不相等的散列码，违反了hashCode的约定。

```
public int hashCode(){
    return 42;
}
```
这个问题很大，将所有此类的实例放在同个散列桶中，规模很大，不利于性能；

```
public final class PhoneNumber {
	private final short areaCode;
	private final short prefix;
	private final short lineNumber;

	public PhoneNumber(int areaCode, int prefix, int lineNumber) {
		rangeCheck(areaCode, 999, "area code");
		rangeCheck(prefix, 999, "prefix");
		rangeCheck(lineNumber, 9999, "line number");
		this.areaCode = (short) areaCode;
		this.prefix = (short) prefix;
		this.lineNumber = (short) lineNumber;
	}

	private static void rangeCheck(int arg, int max, String name) {
		if (arg < 0 || arg > max)
			throw new IllegalArgumentException(name + ": " + arg);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this)
			return true;
		if (!(o instanceof PhoneNumber))
			return false;
		PhoneNumber pn = (PhoneNumber) o;
		return pn.lineNumber == lineNumber && pn.prefix == prefix
				&& pn.areaCode == areaCode;
	}

	// Broken - no hashCode method!

	// A decent hashCode method - Page 48
	// @Override public int hashCode() {
	// int result = 17;
	// result = 31 * result + areaCode;
	// result = 31 * result + prefix;
	// result = 31 * result + lineNumber;
	// return result;
	// }

	// Lazily initialized, cached hashCode - Page 49
	// private volatile int hashCode; // (See Item 71)
	//
	// @Override public int hashCode() {
	// int result = hashCode;
	// if (result == 0) {
	// result = 17;
	// result = 31 * result + areaCode;
	// result = 31 * result + prefix;
	// result = 31 * result + lineNumber;
	// hashCode = result;
	// }
	// return result;
	// }

	public static void main(String[] args) {
		Map<PhoneNumber, String> m = new HashMap<PhoneNumber, String>();
		m.put(new PhoneNumber(707, 867, 5309), "Jenny");
		System.out.println(m.get(new PhoneNumber(707, 867, 5309)));
	}
}
```
##### 10.始终覆盖toString
为了输出明确，或者解析；

```
@Override
	public String toString() {
		return String.format("(%03d) %03d-%04d", areaCode, prefix, lineNumber);
	}

```
##### 11.慎重的覆盖clone
如果是对象允许克隆，首先实现Cloneable,实现clone方法；
##### 12.考虑实现comparable接口
它是Comparable接口的方法comparaTo;
如果实现了就表明它的实例具有内在的排序关系，为的就是实现对象数组进行排序；

```
Arrays.sort(a);
```

```
public int compareTo(PhoneNumber pn) {
		// Compare area codes
		int areaCodeDiff = areaCode - pn.areaCode;
		if (areaCodeDiff != 0)
			return areaCodeDiff;

		// Area codes are equal, compare prefixes
		int prefixDiff = prefix - pn.prefix;
		if (prefixDiff != 0)
			return prefixDiff;

		// Area codes and prefixes are equal, compare line numbers
		return lineNumber - pn.lineNumber;
	}

```
#### 四、类和接口

---
##### 13.使类和成员的可访问性最小化
- 设计良好的模块会隐藏所有实现细节，把它的API与它的实现清晰的隔离开来。（信息隐藏、封装）
- 原因：
1：可以解除组成系统各模块之间的耦合关系，使模块可以独立的开发、测试、优化、使用、理解、修改。提高了软件的可重用性。
实体的可访问性由它的位置和访问修饰符共同决定。
规则：
- 1：尽可能的使每个类或者成员不被外界访问。
- 对于成员（域、方法、嵌套类、嵌套接口）有四种可能的访问级别，按照可访问递增的顺序罗列出来：
- 私有的-->包级私有的-->受保护的-->公有的


##### 14.在公有类中使用访问方法而非公有域
总结：公有类永远都不应该暴露可变的域，公有类暴露不可变域危害比这个小，但是有时候会需要用包级私有的或者私有的嵌套类来暴露域，无论这个类是可变或不可变的。
##### 15.使可变性最小化
不可变类只是其实例不能被修改的类。（String/基本类型的包装类、BigInteger和BigDecimal）
原则：
- 1：不要提供任何会修改对象状态的方法；
- 2：保证类不会被扩展；
- 3：使所有的域都是final的；
- 4：所有的域都是私有的；
- 5：确保对于任何可变组件的互斥访问；
不可变对象本质上是线程安全的，他们不要求同步；
```
	public static final Complex ZERO = new Complex(0, 0);
	public static final Complex ONE = new Complex(1, 0);
	public static final Complex I = new Complex(0, 1);
```
总结：不要在构造器或者静态工厂之外提供公有的初始化方法，除非有很好的理由！保证每个域的可变可以接受！----此部分需要好好研究！
##### 16.复合优先于继承
继承是实现代码重用的有力手段。在包的内部使用继承是非常安全的，在一个程序员的控制之下。然而，对普通的具体类进行跨越包边界的继承是危险的。
与方法调用不同的是，继承打破了封装性。(子类依赖于超类，超类的改变影响子类)会带来一些不可预估的问题，由于版本的区别，子类的使用无法预估了！
解决方法：不用宽展现有的类，而是在新的类中添加一个私有域，它引用现有类的一个实例！

```
public class ForwardingSet<E> implements Set<E> {
	private final Set<E> s;

	public ForwardingSet(Set<E> s) {
		this.s = s;
	}

	public void clear() {
		s.clear();
	}

	public boolean contains(Object o) {
		return s.contains(o);
	}

	public boolean isEmpty() {
		return s.isEmpty();
	}

	public int size() {
		return s.size();
	}

	public Iterator<E> iterator() {
		return s.iterator();
	}

	public boolean add(E e) {
		return s.add(e);
	}

	public boolean remove(Object o) {
		return s.remove(o);
	}

	public boolean containsAll(Collection<?> c) {
		return s.containsAll(c);
	}

	public boolean addAll(Collection<? extends E> c) {
		return s.addAll(c);
	}

	public boolean removeAll(Collection<?> c) {
		return s.removeAll(c);
	}

	public boolean retainAll(Collection<?> c) {
		return s.retainAll(c);
	}

	public Object[] toArray() {
		return s.toArray();
	}

	public <T> T[] toArray(T[] a) {
		return s.toArray(a);
	}

	@Override
	public boolean equals(Object o) {
		return s.equals(o);
	}

	@Override
	public int hashCode() {
		return s.hashCode();
	}

	@Override
	public String toString() {
		return s.toString();
	}
}

```

```
public class InstrumentedSet<E> extends ForwardingSet<E> {
	private int addCount = 0;

	public InstrumentedSet(Set<E> s) {
		super(s);
	}

	@Override
	public boolean add(E e) {
		addCount++;
		return super.add(e);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		addCount += c.size();
		return super.addAll(c);
	}

	public int getAddCount() {
		return addCount;
	}

	public static void main(String[] args) {
		InstrumentedSet<String> s = new InstrumentedSet<String>(
				new HashSet<String>());
		s.addAll(Arrays.asList("Snap", "Crackle", "Pop"));
		System.out.println(s.getAddCount());
	}
}

```
InstrumentedSet是一个包装类，额外增加了一个计数功能！
总结：所以当a确实是b的一个子类时才能继承，问一句a是b吗？如果是则继承。否则复合。
对于不是为了继承而设计，并且没有文档说要的外来类进行子类化是很危险的。
##### 17.要么为继承而设计，并提供文档说明，要么就禁止继承
- 子类覆盖父类的每个方法带来的影响必须精确的描述。说明自用型。
- 为了继承而设计类，对于这个类会有一些实质性的限制。
- 对于一个并非为了继承而设计的非final具体类在修改了它的内部实现之后，接收到与子类化相关的错误报告并不少见。
###### 解决方法：是对于那些并非为了安全地进行子类化而设计和编写文档的类，要禁止子类化。
- 1:类声明为final的。
- 2：把所有的构造器变成私有的，或者包级私有的，并增加一些公有的静态工厂来替代构造器。

##### 18.接口优于抽象类
java是单继承，抽象类作为类型定义受到了极大的限制。
现有的类可以很容易被更新，以实现新的接口；
接口是定义minxin(混合类型)的理想选择；
接口允许我们构造非层次结构的类型框架；
接口与抽象类结合提供了一个“骨架”：
```
public class IntArrays {
	static List<Integer> intArrayAsList(final int[] a) {
		if (a == null)
			throw new NullPointerException();

		return new AbstractList<Integer>() {
			public Integer get(int i) {
				return a[i]; // Autoboxing (Item 5)
			}

			@Override
			public Integer set(int i, Integer val) {
				int oldVal = a[i];
				a[i] = val; // Auto-unboxing
				return oldVal; // Autoboxing
			}

			public int size() {
				return a.length;
			}
		};
	}

	public static void main(String[] args) {
		int[] a = new int[10];
		for (int i = 0; i < a.length; i++)
			a[i] = i;
		List<Integer> list = intArrayAsList(a);

		Collections.shuffle(list);
		System.out.println(list);
	}
}

```
骨架实现的美妙之处在于，它为抽象类提供了实现上的帮助，但又不强加“抽象类被用作类型定义时”所特有的严格限制。
- 总结：接口通常是定义允许多个实现的类型的最佳途径。但当演变的容易性比灵活性和功能更为重要的时候。必须理解和可以接收这些局限性。公有接口必须经得起全面的测试。

##### 19.接口只用于定义类型
当类实现接口时，接口就充当可以引用这个类的实例类型。类实现了接口，就表明客户端可以对这个类的实例实施某些动作。为了任何其他目的而定义接口是不恰当的。

例外：常量接口，没有包含任何方法，只包含静态的final域，每个域都导出一个常量。使用这些常量的类实现这个接口，以避免用类名来修饰常量名。常量接口是对接口的不良使用。
- 总结：接口应该只被用来定义类型，它们不应该被用来导出常量；

##### 20.类层次优于标签类
标签类过于冗长、容易出错，并且效率低下；
```
class Circle extends Figure {
	final double radius;

	Circle(double radius) {
		this.radius = radius;
	}

	double area() {
		return Math.PI * (radius * radius);
	}
}
```

```
class Square extends Rectangle {
	Square(double side) {
		super(side, side);
	}
}

```
标签类很少使用，要使用层次结构！

##### 21.用函数对象表示策略
总结，函数指针的主要用途就是实现策略模式。在Java中要实现这种模式，要声明一个接口表示该策略，并未每个具体策略声明一个实现了该接口的类。当一个具体策略纸杯使用一次时，通常使用匿名类来声明和实例化这个具体策略类。当一个具体策略是设计用来重复使用的时候，他的类通常就要被实现为私有的静态成员类，并通过公有的静态final域导出，其类型为该策略接口。

```
class Host{
    private static class StrLenCmp implements Comparator<String>,Serializable{
        public int compare(String s1,String s2){
            return s1.length()-s2.length();
        }
    }
    public static final Comparator<String> STRING_LENGTH_COMPATOR =new StrLenCmp();
    ...
}
```
##### 22.优先考虑静态成员类
嵌套类是指被定义在另一个类的内部的类。嵌套类存在的目的应该只是为了外围类提供服务。
1：静态成员类、2：非静态成员类、3：匿名类、4：局部类；后三种也称为内部类；
总结：如果一个嵌套类需要在单个方法之外仍然可见，或者它太长了，不适合放在方法内部，就应该使用成员类。如果成员类的每个实例都需要指向其他外围实例的引用，就把成员类做成非静态的；否者是静态的。假设这个嵌套类属于一个方法的内部，如果你只需要在一个地方创建实例，并且已经有一个预置的类型可以说明这个累的特征，就要把它做成几名类；否则，就做成局部类。

#### 五、泛型

---
##### 23.请不要在新代码中使用原生态类型
如果使用源生态类型，如List而不是List<String>,就失掉了泛型在安全型和表述性方面的所有优势。
List<String>是源生态类型List的一个子类型，而不是参数化类型List<String>的子类型。使用List失掉了安全性，但如果使用List<Object>这样的参数化类型，则不会。这条规则有两个小小的例外：
- 源于泛型信息可以在运行时被擦除；
总结：使用源生态类型会在运行时导致异常，因此不要在新代码中使用。原生态类型只是为了与运入泛型之前的遗留代码进行兼容和互用二提供的。

![image](https://raw.githubusercontent.com/YoucanyouUp1/gitTest/master/fanxing.png)

##### 24.消除非受检警告

最小化警告信息：
![image](https://raw.githubusercontent.com/YoucanyouUp1/gitTest/master/suppresswarings.png)
##### 25.列表优先于数组
数组与泛型相比：
- 1：数组是协变的。数组的类型会随着父子的类型而改变。泛型不会。
合法
```
Object[] objectArray=new Long[1];
objectArray[0]="i am qiuyadong";
```
不合法
```
List<Object> ol=new ArrayList<Long>();
ol.add("i am qiuyadong");
```
虽然都不会将字符串放入Long容器中，但是数组，会在运行时出错，列表会在编译时出错。
- 2：数组是具体的；
泛型会通过擦除来实现，只在编译时强化他们的类型信息，并在运行时抛弃其类型，为了与之前的混用。
总结:数组和泛型有着非常不同的类型规则。数组是协变且可以具体化，泛型是不可变的且可以被擦除的。数组是运行时的类型安全，但没有编译时的类型安全，反之，泛型则也一样。当它们混合使用时使用列表代替数组。

##### 26.优先考虑泛型

上述25条并不是一定的，实际上并不是总想在泛型中使用列表。为了提升性能，ArrayList必须在数组上实现，还有HashMap上实现。
- 总之：使用泛型比使用需要在客户端代码中进行转换的类型来的更加安全，也更加容易，在设计新类型的时候要确保他们不需要这种转换就可以使用。这通常意味着要把类做成泛型的。只要时间允许，就把现有的所有的类型都泛型化。
```
public class Stack<E> {
	private E[] elements;
	private int size = 0;
	private static final int DEFAULT_INITIAL_CAPACITY = 16;

	// The elements array will contain only E instances from push(E).
	// This is sufficient to ensure type safety, but the runtime
	// type of the array won't be E[]; it will always be Object[]!
	@SuppressWarnings("unchecked")
	public Stack() {
		elements = (E[]) new Object[DEFAULT_INITIAL_CAPACITY];
	}

	public void push(E e) {
		ensureCapacity();
		elements[size++] = e;
	}

	public E pop() {
		if (size == 0)
			throw new EmptyStackException();
		E result = elements[--size];
		elements[size] = null; // Eliminate obsolete reference
		return result;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	private void ensureCapacity() {
		if (elements.length == size)
			elements = Arrays.copyOf(elements, 2 * size + 1);
	}

	// Little program to exercise our generic Stack
	public static void main(String[] args) {
		Stack<String> stack = new Stack<String>();
		for (String arg : args)
			stack.push(arg);
		while (!stack.isEmpty())
			System.out.println(stack.pop().toUpperCase());
	}
}
```
##### 27.优先考虑泛型方法
总结：泛型方法就像泛型一样，使用起来比要求客户端转换输入参数并返回值的方法来的更加安全和容易。

```
public class GenericSingletonFactory {
	// Generic singleton factory pattern
	private static UnaryFunction<Object> IDENTITY_FUNCTION = new UnaryFunction<Object>() {
		public Object apply(Object arg) {
			return arg;
		}
	};

	// IDENTITY_FUNCTION is stateless and its type parameter is
	// unbounded so it's safe to share one instance across all types.
	@SuppressWarnings("unchecked")
	public static <T> UnaryFunction<T> identityFunction() {
		return (UnaryFunction<T>) IDENTITY_FUNCTION;
	}

	// Sample program to exercise generic singleton
	public static void main(String[] args) {
		String[] strings = { "jute", "hemp", "nylon" };
		UnaryFunction<String> sameString = identityFunction();
		for (String s : strings)
			System.out.println(sameString.apply(s));

		Number[] numbers = { 1, 2.0, 3L };
		UnaryFunction<Number> sameNumber = identityFunction();
		for (Number n : numbers)
			System.out.println(sameNumber.apply(n));
	}
}
```
##### 28.利用有限制通配符来提升API的灵活性
为了获得最大限度的灵活性，要在表示生产者或者消费者的输入参数上使用通配符类型。如果输入的参数既是生产者也是消费者，那么通配符类型没有好处，就需要严格的类型匹配。
牢记：PECS表示producer-extends,consumer-super。
此部分确实需要好好看；
- 总结：在API中使用通配符类型虽然比较㤇技巧，但是使API灵活的多。如果编写的是将被广泛使用的类库，则一定要适当利用通配符类型。
- 所有的comparable和comparator都是消费者。
```
public class RecursiveTypeBound {
	public static <T extends Comparable<? super T>> T max(List<? extends T> list) {
		Iterator<? extends T> i = list.iterator();
		T result = i.next();
		while (i.hasNext()) {
			T t = i.next();
			if (t.compareTo(result) > 0)
				result = t;
		}
		return result;
	}

	public static void main(String[] args) {
		List<String> argList = Arrays.asList(args);
		System.out.println(max(argList));
	}
}
```
##### 29.优先考虑类型安全的异构容器
泛型最常用于集合，如set何Map,以及单元素的容器，如ThreadLocal和AtomicReference。它冲淡了被参数化了的容器。但是限制了每个容器智能有固定数目的类型参数。
总结：集合API说明了泛型的一般用法，限制你每个容器只能有固定数目的类型参数。你可以通过将类型参数放在键上而不是容器上来避开这一限制。对于这种类型安全的异构容器，可以用Class对象作为键。以这种方式使用的Class对象称作类型令牌。你亦可以使用定制的键类型。例如用DatabaseRow类型表示一个数据库行，用泛型Column<T>作为它的键。
```
public class Favorites {
	// Typesafe heterogeneous container pattern - implementation
	private Map<Class<?>, Object> favorites = new HashMap<Class<?>, Object>();

	public <T> void putFavorite(Class<T> type, T instance) {
		if (type == null)
			throw new NullPointerException("Type is null");
		favorites.put(type, instance);
	}

	public <T> T getFavorite(Class<T> type) {
		return type.cast(favorites.get(type));
	}

	// Typesafe heterogeneous container pattern - client
	public static void main(String[] args) {
		Favorites f = new Favorites();
		f.putFavorite(String.class, "Java");
		f.putFavorite(Integer.class, 0xcafebabe);
		f.putFavorite(Class.class, Favorites.class);

		String favoriteString = f.getFavorite(String.class);
		int favoriteInteger = f.getFavorite(Integer.class);
		Class<?> favoriteClass = f.getFavorite(Class.class);
		System.out.printf("%s %x %s%n", favoriteString, favoriteInteger,
				favoriteClass.getName());
	}
}
```
#### 六、枚举和注解

---
##### 30.用enum代替int常量
通过公有的静态的final域为每个枚举常量导出实例的类。因为没有可以访问的构造器，枚举类型是真正final的。
Java的枚举本质上是int值，它们是实例受控的，是单例的泛型化，实质上是单元素的枚举。
将方法或域添加到枚举类型中，
首先，是想将数据与它的常量关联起来；
```
public enum Planet {
	MERCURY(3.302e+23, 2.439e6), VENUS(4.869e+24, 6.052e6), EARTH(5.975e+24,
			6.378e6), MARS(6.419e+23, 3.393e6), JUPITER(1.899e+27, 7.149e7), SATURN(
			5.685e+26, 6.027e7), URANUS(8.683e+25, 2.556e7), NEPTUNE(1.024e+26,
			2.477e7);
	private final double mass; // In kilograms
	private final double radius; // In meters
	private final double surfaceGravity; // In m / s^2

	// Universal gravitational constant in m^3 / kg s^2
	private static final double G = 6.67300E-11;

	// Constructor
	Planet(double mass, double radius) {
		this.mass = mass;
		this.radius = radius;
		surfaceGravity = G * mass / (radius * radius);
	}

	public double mass() {
		return mass;
	}

	public double radius() {
		return radius;
	}

	public double surfaceGravity() {
		return surfaceGravity;
	}

	public double surfaceWeight(double mass) {
		return mass * surfaceGravity; // F = ma
	}
}
```
将不同的行为与每个枚举常量关联起来被称作特定于常量的方法实现；
```
public enum Operation {
	PLUS("+") {
		double apply(double x, double y) {
			return x + y;
		}
	},
	MINUS("-") {
		double apply(double x, double y) {
			return x - y;
		}
	},
	TIMES("*") {
		double apply(double x, double y) {
			return x * y;
		}
	},
	DIVIDE("/") {
		double apply(double x, double y) {
			return x / y;
		}
	};
	private final String symbol;

	Operation(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}

	abstract double apply(double x, double y);

	// Implementing a fromString method on an enum type - Page 154
	private static final Map<String, Operation> stringToEnum = new HashMap<String, Operation>();
	static { // Initialize map from constant name to enum constant
		for (Operation op : values())
			stringToEnum.put(op.toString(), op);
	}

	// Returns Operation for string, or null if string is invalid
	public static Operation fromString(String symbol) {
		return stringToEnum.get(symbol);
	}

	// Test program to perform all operations on given operands
	public static void main(String[] args) {
		double x = Double.parseDouble(args[0]);
		double y = Double.parseDouble(args[1]);
		for (Operation op : Operation.values())
			System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
	}
}
```
策略枚举：

```
enum PayrollDay {
	MONDAY(PayType.WEEKDAY), TUESDAY(PayType.WEEKDAY), WEDNESDAY(
			PayType.WEEKDAY), THURSDAY(PayType.WEEKDAY), FRIDAY(PayType.WEEKDAY), SATURDAY(
			PayType.WEEKEND), SUNDAY(PayType.WEEKEND);

	private final PayType payType;

	PayrollDay(PayType payType) {
		this.payType = payType;
	}

	double pay(double hoursWorked, double payRate) {
		return payType.pay(hoursWorked, payRate);
	}

	// The strategy enum type
	private enum PayType {
		WEEKDAY {
			double overtimePay(double hours, double payRate) {
				return hours <= HOURS_PER_SHIFT ? 0 : (hours - HOURS_PER_SHIFT)
						* payRate / 2;
			}
		},
		WEEKEND {
			double overtimePay(double hours, double payRate) {
				return hours * payRate / 2;
			}
		};
		private static final int HOURS_PER_SHIFT = 8;

		abstract double overtimePay(double hrs, double payRate);

		double pay(double hoursWorked, double payRate) {
			double basePay = hoursWorked * payRate;
			return basePay + overtimePay(hoursWorked, payRate);
		}
	}
}
```
总结：当需要一组固定常量的时候，与int常量相比，枚举类型的优势是不可言喻的。枚举要易读，也更加安全，功能强大。雨多枚举都不需要显示的构造器或者成员，但许多其他枚举则受益于“每个常量和属性的关联”以及“提供行为受这个属性影响的方法。只有极少数的枚举受益于将多种行为与单个方法关联。在这种相对极少见的情况下，特定于常量的方法要优于启动自由值得枚举，如果多个枚举常量同时共享相同的行为，则考虑策略枚举。
##### 31.用实例域代替序数
许多枚举天生就与一个单独的int值相关联。所有的枚举都有一个ordinal方法，返回每个枚举常量在类型中的数字位置。这是有问题的，维护和扩展很麻烦，若使常量与数字关联如下：
```
public enum Ensemble {
	SOLO(1), DUET(2), TRIO(3), QUARTET(4), QUINTET(5), SEXTET(6), SEPTET(7), OCTET(
			8), DOUBLE_QUARTET(8), NONET(9), DECTET(10), TRIPLE_QUARTET(12);

	private final int numberOfMusicians;

	Ensemble(int size) {
		this.numberOfMusicians = size;
	}

	public int numberOfMusicians() {
		return numberOfMusicians;
	}
}

```
关于Enum规范中的ordinal：“大多数程序员都不需要这个方法。它是设计成用于像EnumSet和EnumMap这种基于枚举的通用数据结构的。”除非你在编写的是这种数据结构，否则最好完全避免适用ordinal;
##### 32.用EnumSet代替位域

```
public class Text {
	public enum Style {
		BOLD, ITALIC, UNDERLINE, STRIKETHROUGH
	}

	// Any Set could be passed in, but EnumSet is clearly best
	public void applyStyles(Set<Style> styles) {
		// Body goes here
	}

	// Sample use
	public static void main(String[] args) {
		Text text = new Text();
		text.applyStyles(EnumSet.of(Style.BOLD, Style.ITALIC));
	}
}
```
正是因为枚举类型要用在集合（Set）中，所以没有利用用位域来表示它。
##### 33.用EnumMap代替序数索引
为公园里的所有植物进行分类，并索引；
```
public class Herb {
	public enum Type {
		ANNUAL, PERENNIAL, BIENNIAL
	}

	private final String name;
	private final Type type;

	Herb(String name, Type type) {
		this.name = name;
		this.type = type;
	}

	@Override
	public String toString() {
		return name;
	}

	public static void main(String[] args) {
		Herb[] garden = { new Herb("Basil", Type.ANNUAL),
				new Herb("Carroway", Type.BIENNIAL),
				new Herb("Dill", Type.ANNUAL),
				new Herb("Lavendar", Type.PERENNIAL),
				new Herb("Parsley", Type.BIENNIAL),
				new Herb("Rosemary", Type.PERENNIAL) };

		// Using an EnumMap to associate data with an enum - Page 162
		Map<Herb.Type, Set<Herb>> herbsByType = new EnumMap<Herb.Type, Set<Herb>>(
				Herb.Type.class);
		for (Herb.Type t : Herb.Type.values())
			herbsByType.put(t, new HashSet<Herb>());
		for (Herb h : garden)
			herbsByType.get(h.type).add(h);
		System.out.println(herbsByType);
	}
}
```
当进行两次索引时，
```
public enum Phase {
	SOLID, LIQUID, GAS;

	public enum Transition {
		MELT(SOLID, LIQUID), FREEZE(LIQUID, SOLID), BOIL(LIQUID, GAS), CONDENSE(
				GAS, LIQUID), SUBLIME(SOLID, GAS), DEPOSIT(GAS, SOLID);

		private final Phase src;
		private final Phase dst;

		Transition(Phase src, Phase dst) {
			this.src = src;
			this.dst = dst;
		}

		// Initialize the phase transition map
		private static final Map<Phase, Map<Phase, Transition>> m = new EnumMap<Phase, Map<Phase, Transition>>(
				Phase.class);
		static {
			for (Phase p : Phase.values())
				m.put(p, new EnumMap<Phase, Transition>(Phase.class));
			for (Transition trans : Transition.values())
				m.get(trans.src).put(trans.dst, trans);
		}

		public static Transition from(Phase src, Phase dst) {
			return m.get(src).get(dst);
		}
	}

	// Simple demo program - prints a sloppy table
	public static void main(String[] args) {
		for (Phase src : Phase.values())
			for (Phase dst : Phase.values())
				if (src != dst)
					System.out.printf("%s to %s : %s %n", src, dst,
							Transition.from(src, dst));
	}
}
```
总结：最好不要用序数来索引数组，而是要用EnumMap。如果你所表示的是多维的，就是用EnumMap<...,EnumMap<...>>。应用程序的程序员一般情况下都不是用Enum.ordimal,有一种情况例外：见（31）。
##### 34.用接口模拟可伸缩的枚举
用例：操作码：它的元素表示在某个机器上的那些操作，要尽可能的让API用户提供他们自己的操作，这样可以有效的扩展ApI所提供的操作集。例子：

```
public interface Operation {
	double apply(double x, double y);
}

```

```
public enum BasicOperation implements Operation {
	PLUS("+") {
		public double apply(double x, double y) {
			return x + y;
		}
	},
	MINUS("-") {
		public double apply(double x, double y) {
			return x - y;
		}
	},
	TIMES("*") {
		public double apply(double x, double y) {
			return x * y;
		}
	},
	DIVIDE("/") {
		public double apply(double x, double y) {
			return x / y;
		}
	};
	private final String symbol;

	BasicOperation(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}
}

```

```
public enum ExtendedOperation implements Operation {
	EXP("^") {
		public double apply(double x, double y) {
			return Math.pow(x, y);
		}
	},
	REMAINDER("%") {
		public double apply(double x, double y) {
			return x % y;
		}
	};

	private final String symbol;

	ExtendedOperation(String symbol) {
		this.symbol = symbol;
	}

	@Override
	public String toString() {
		return symbol;
	}

	// Test class to exercise all operations in "extension enum" - Page 167
	public static void main(String[] args) {
		double x = Double.parseDouble(args[0]);
		double y = Double.parseDouble(args[1]);
		test(ExtendedOperation.class, x, y);

		System.out.println(); // Print a blank line between tests
		test2(Arrays.asList(ExtendedOperation.values()), x, y);
	}

	// test parameter is a bounded type token (Item 29)
	private static <T extends Enum<T> & Operation> void test(Class<T> opSet,
			double x, double y) {
		for (Operation op : opSet.getEnumConstants())
			System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
	}

	// test parameter is a bounded wildcard type (Item 28)
	private static void test2(Collection<? extends Operation> opSet, double x,
			double y) {
		for (Operation op : opSet)
			System.out.printf("%f %s %f = %f%n", x, op, y, op.apply(x, y));
	}
}
```
总结：虽然无法编写可扩展的枚举类型，却可以通过编写接口以及实现该接口的基础枚举类型，对它进行模拟。这样允许客户端编写自己的枚举来实现接口。如果API是根据接口编写的，那么在可以使用基础枚举类型的任何地方也都可以使用这些枚举。
##### 35.注解优先于命名模式
命名模式，按照某种约定设置名字，问题：
- 1：如果拼写错误，会造成错误的安全感，没有报错。
- 2：无法确保它们只用于响应的程序元素上。
- 3：它们没有提供将参数值与程序元素关联起来的好方法。
解决方法运用注解：
@Retention(RetentionPolicy.RUNTIME)元注解，运行时保留。
@Target(ElementType.METHOD)至在方法声明中才合法。
注解永远不会改变被注解代码的语义，但是使它可以通过工具进行特殊的处理。
```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```

```
public class Sample {
	@Test
	public static void m1() {
	} // Test should pass

	public static void m2() {
	}

	@Test
	public static void m3() { // Test Should fail
		throw new RuntimeException("Boom");
	}

	public static void m4() {
	}

	@Test
	public void m5() {
	} // INVALID USE: nonstatic method

	public static void m6() {
	}

	@Test
	public static void m7() { // Test should fail
		throw new RuntimeException("Crash");
	}

	public static void m8() {
	}
}
```
进行测试
```
public class RunTests {
	public static void main(String[] args) throws Exception {
		int tests = 0;
		int passed = 0;
		Class testClass = Class.forName(args[0]);
		for (Method m : testClass.getDeclaredMethods()) {
			if (m.isAnnotationPresent(Test.class)) {
				tests++;
				try {
					m.invoke(null);
					passed++;
				} catch (InvocationTargetException wrappedExc) {
					Throwable exc = wrappedExc.getCause();
					System.out.println(m + " failed: " + exc);
				} catch (Exception exc) {
					System.out.println("INVALID @Test: " + m);
				}
			}

			// Array ExceptionTest processing code - Page 174
			if (m.isAnnotationPresent(ExceptionTest.class)) {
				tests++;
				try {
					m.invoke(null);
					System.out.printf("Test %s failed: no exception%n", m);
				} catch (Throwable wrappedExc) {
					Throwable exc = wrappedExc.getCause();
					Class<? extends Exception>[] excTypes = m.getAnnotation(
							ExceptionTest.class).value();
					int oldPassed = passed;
					for (Class<? extends Exception> excType : excTypes) {
						if (excType.isInstance(exc)) {
							passed++;
							break;
						}
					}
					if (passed == oldPassed)
						System.out.printf("Test %s failed: %s %n", m, exc);
				}
			}
		}
		System.out.printf("Passed: %d, Failed: %d%n", passed, tests - passed);
	}
}
```
上述方式通过判断抛出异常的方式判断是否通过测试；
为了支持只抛出特殊异常时才成功的测试添加支持。
```
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExceptionTest {
	Class<? extends Exception>[] value();
}

```

```
public class Sample2 {
	@ExceptionTest(ArithmeticException.class)
	public static void m1() { // Test should pass
		int i = 0;
		i = i / i;
	}

	@ExceptionTest(ArithmeticException.class)
	public static void m2() { // Should fail (wrong exception)
		int[] a = new int[0];
		int i = a[1];
	}

	@ExceptionTest(ArithmeticException.class)
	public static void m3() {
	} // Should fail (no exception)

	// Code containing an annotation with an array parameter - Page 174
	@ExceptionTest({ IndexOutOfBoundsException.class,
			NullPointerException.class })
	public static void doublyBad() {
		List<String> list = new ArrayList<String>();

		// The spec permits this method to throw either
		// IndexOutOfBoundsException or NullPointerException
		list.addAll(5, null);
	}
}
```
总结：除了“工具铁匠---特定的程序员”外，大多数程序员都不必定义注解类型。但是所有的程序员都应该使用Java平台所提供的预定义的注解类型。还要考虑使用IDE或者静态分析工具所提供了的任何注解。这种注解可以提升由这些工具所提供的诊断信息的质量。但是要注意这些注解还没有标准化，因此如果变换工具或者标准，就有很多工作要做了。
##### 36.坚持使用Override注解
总结：如果你想要的每个方法声明中使用Override注解来覆盖超类声明，编译器可以替你防止大量的错误，但又一个例外。在具体的类中，不必标注你确信覆盖了抽象方法声明的方法。
##### 37.用标记接口定义类型
是没有包含方法声明的接口，而只是指明一个类实现了某个具有某种属性的接口。
如果标记是应该用到任何程序元素而不是类或者接口，就必须使用注解，因为只有类和接口可以用来实现或者扩展接口。如果标记只应有给类和接口就问问自己：我要编写一个还是多个值接收有这种标记的方法呢？如果是这中情况，就优先使用标记接口而非注解。这样你就可以用接口作为相关方法的参数类型，它真正可以为你提供编译时进行类型检查的好处。
总结：标记接口和标记注解都各有用处。如果想要定义一个任何新方法都不会与之相关联的类型，标记接口时最好的选择，如果想要标记程序元素而非类和接口就考虑未来可能要给标记添加更多的信息，或者标记要适用于已经广泛使用了注解类型的框架，那么标记注解就是正确的选择，如果你发现自己编写的目标为ElementType.Type的标记注解类型，就要花点时间考虑清楚,它是否真的应该为注解类型，想想标记接口是否会更加合适呢。
- 上述有点难懂，只是暂时摘抄。

#### 七、方法

---

目标：可用性、健壮性、灵活性。
##### 38.检查参数的有效性
总结：每当编写方法或构造器的时候，应该考虑到它的参数有哪些限制，应该把这些限制写到文档中，并且在和这个方法体的开头处，通过显示的检查来实施这些限制。
##### 39.必要时进行保护性拷贝
java是一门安全的语言，它对于缓冲区溢出、数组越界、非法指针以及其他的内存破坏错误都自动免疫，这些对C/c++是不安全的语言。但是，即使在安全的语言中，如果不采取一点措施，还是无法与其他的类隔离开来。
- 对于构造器的每个可变参数进行保护性的拷贝是必要的。（为了阻止不可靠的子类污染，请不要使用clone方法进行保护性的拷贝）
- 每当编写方法或者构造器时，如果它要允许客户提供的对象进入到内部数据结构中，则有必要考虑一下，客户提供的对象是否有可能是可变的。让拷贝之后的对象而不是原始的数据结构。
- 长度非零的数组总是可变的。
总结：如果类具有从客户端得到或者返回到客户端的可变组件，类就必须保护性的拷贝这些组件。如果拷贝的成本收到限制，并且信任它的客户端不会不恰当的修改组件就可以在文档中指明客户端的职责是不得修改收到影响的组件，以此来代替保护性的拷贝。

##### 40.谨慎设计方法签名
- 谨慎地选择方法的名称
- 不要过于追求提供便利的方法
- 避免过长的参数列表，目标是4个，或者更少。（1：把方法分解成多个方法，每个方法只需要这些参数的一个子集。2：创建辅助类。3从对象构建到方法调用都采用Builder模式。）
- 对于参数类型，要优先使用接口而不是类。
- 对于boolean参数，要优先使用两个元素的枚举类型；

##### 41.慎用重载
- 要调用那个重载方法是在编译时做出决定的。
- 与重载的情形相比，对象的运行时类型并不影响“哪个重载版本将被执行”；选择工作是在编译时运行的，完全基于参数的编译时类型。
- 覆盖机制是规范，重载机制是例外，覆盖机制满足了人文对于方法调用行为的期望。
如何排除这种争议，安全而保守的策略是，永远不要导出两个具有相同参数数目的重载方法，如果方法使用可变参数，保守的策略是根本不要重载它。
总结：“能够重载方法”并不意味着就“应该重载方法”。一般情况下，对于多个具有相同参数数目的方法来说，应该尽量避免重载方法。在某些情况下，特别是涉及构造器的时候，要遵循这条一不可能的。在这种情况下，至少应该避免这样的情形：同一组参数值需经过类型转换就可以被传递给不同的重载方法。如果避免不了这种情况，例如，因为正在改造一个现有的泪已实现的新的接口，就应该保证：当传递同样的参数时，所有重载方法的行为必须一致。如果不能做到这一点，程序员就很难有效的使用被重载的方法或者构造器，他们就不能理解她为什么不能正常的工作。

##### 42.慎用可变参数
可变参数方法接收0个或者多个制定类型的参数。可变参数机制通过先创建一个数组，数组的大小为在调用位置所传递的参数数量，然后将参数值传到数组中，最后将数组传递给方法。
- printf和反射机制都从可变参数中极大地收益。
总结：在定义参数数目不定的方法时，可变参数方法是一种很方便的方式，但是他们不应该被过度滥用。

##### 43.返回零长度的数组或集合而不是null
总结：返回类型为数组或者结合的方法没有理由返回null,而不是返回一个零长度的数组或者结合。这种习惯做法（指返回null）很有可能是从c中传过来的。

##### 44.为所有导出的API元素编写文档注释
- 为了正确的编写API文档，必须在每个被导出的类、接口、构造器、方法和域声明之前增加一个文档注释。
- 为了编写出可维护的代码，还应该为那些没有导出的类、接口、构造器、方法和域编写文档注释。
文档注释应该列出这个方法的所有前提条件和后置条件：
- 前提条件是指为了使客户能够调用这个方法，而必须满足的条件；
- 后置条件是指调用成功完成后，那些条件必须满足。
- 副作用，系统状态中可以观察的变化。
- 所表述的类或者方法的线程安全性；
```
@param
@return
@throws
```
总结：要为API编写文档，文档注释是最好、最有效的途径。对于所有可导出的API元素来说，使用用文档注释应该被看作是强制的。要采用一致的风格来遵循标准的约定。记住，在文档注释内部出现任何HTML标签都是允许的，但是HTML元字符必须要经过转义。

#### 八、通用程序设计

---
##### 45.将局部变量的作用域最小化
可以增强代码的可读性和可维护性，并降低出错的可能性；
- 几乎每个局部变量的声明都应该包含一个初始化的表达式。（除了try-catch外）
总结：将局部变量的作用域最小化的方法是方法小而集中，如果把两个操作合并到同一个方法中，与其中一个操作相关的局部变量有可能会出现执行另一个操作的代码范围之内。为了防止这种情况发生，只要把这个方法分成两个，每个方法各执行一个操作。

##### 46.for-each循环优先于传统的for循环
总结：for-each循环在简洁性和预防bug方面有着传统的for循环无法比拟的优势，并且没有性能损失。但是三种情况无法使用for-each循环；
- 1：过滤---如果需要遍历集合,并删除选定的元素,就需要使用显示的迭代器,以便可以方便调用它的remove方法。
- 2：装换---如果需要遍历列表或者数组，并取代它部分或者全部元素值,就需要列表迭代器或者数组索引，以便设定元素的值；
- 3：平行迭代---如果需要并行的遍历多个集合，就需要显示的控制迭代器或者索引变量，以便所有迭代器或者索引变量都可以得到同步迁移。

##### 47.了解和使用类库
Random.nextInt(int);
- 总结：不要重新发明轮子；

##### 48.如果需要精确的答案，请避免使用float和double
- float和double类型主要为了科学计算和工程计算而设计的。
- 解决精确的问题，使用BigDecimal、int或者long进行货币计算。
总结：如果要精确的答案的计算任务，请不要使用double和float;如果你想让系统记录十进制小数点，不介意不使用基本类型带来的不便，就请使用BigDecimal，还可以选8种舍入模式之一。如果性能非常重要，不介意自己记录小数点，涉及数值又不太大，就可以使用int和long。如果数值范围没有超过9位十进制数字使用int,如果不超过18位，就可以使用long。如果数值超过18位，就必须使用Bigdecimal。

##### 49.基本类型优先于装箱基本类型
每个基本类型都有一个对应的引用类型，装箱基本类型。
- 区别：
- 1：基本类型只有值，而装箱基本类型则具有与它们的值不同的同一性。两个装箱基本类型可以具有相同的值和不同的同一性。
- 2：基本类型只有功能完备的值，装箱类型除了所有功能外，还有个非功能值：null;
- 3:基本类型通常要比装箱类型更节省时间和空间；
使用范围：
- 1：作为集合中的元素，键和值。你不能将基本类型放在集合中，因此必须使用装箱基本类型。
总结：基本类型优先于装箱基本类型。基本类型更加简单，也更加快速。慎重使用装箱，造成异常，导致高开销和不必要的对象创建。

##### 50.如果其他类型更合适，则尽量避免使用字符串
字符串被用来表示文本，它在这方面也确实做的很好。
不适用的范围：
- 1：字符串不适合代替其他的值类型；如果从文件、网络、键盘设备传过来的字符串，能转换成其他类型就装换成其他类型，而不是字符串；
- 2：字符串不适合代替枚举类型；
- 3：字符串不适合代替聚集类型；推荐使用私有的静态成员类；
- 4：字符串也不适合代替能力表；防止相同时冲突；
总结：如果可以使用更加合适的数据类型，或者可以编写更加适当的数据类型，就应该避免使用字符串来表示对象。若使用不当，容易出错。常见：基本类型、枚举类型和聚集类型。

##### 51.当心字符串连接的性能
- 使用“+”把多个字符串合并为一个字符串便利途径。
- 如果数量巨大，为了获得可以接受的性能使用StringBuilder代替String。
总结：不要使用字符串连接操作符来合并多个字符串，除非性能无关紧要，相反，应该使用StringBuilder的append方法，另一种方法是，使用字符数组，或者每次只处理一个字符串，而不是将他们组合起来。

#### 52.通过接口引用对象
优先使用接口而不是类来引用对象。如果有合适的接口类型存在，那么对于参数、返回值、变量和域来说，就都应该使用接口类型进行声明。只有当你利用构造器创建某个对象的时候才真正需要引用和这个对象的类。
应该使用：

```
List<Subscriber> subscribers=new Vector<Subscriber>();

List<Subscriber> subscribers=new ArrayList<Subscriber>();
```
当周围的程序依赖这个具体类的特性的时候，不适合使用这个方式。
- 这样保证只改变一行代码就足够了。
总结：实际上，给定的对象是否具有适合的接口应该是显然的，如果是，用接口引用对象就会使程序更加灵活。如果不是，则使用类层次结构中提够了必要功能的最基础的类。

##### 53.接口优先于反射机制
核心反射机制（java.lang.reflect），提供了“通过程序来访问关于已装载的类的信息”的能力。Class实例，可以获取Constructor、Method和Field实例。

拥有这种能力的代价：
- 1：丧失了编译时类型检查的好处；
- 2：执行反射访问所需要代码非常笨拙和冗长；
- 3：性能丧失。
通常，普通应用程序在运行时不应该以反射方式访问对象；
- 在一些复杂的应用程序需要使用反射机制。包括：浏览器、对象监视器、代码分析工具、解释性的内嵌系统，在RPC系统中使用反射机制也是非常合适的，这样可以不再需要存根编译器。
总结：反射机制是一种的强大的机制，对于特定的复杂系统编程任务，它是非常必要的，但它也是有一些缺点。如果你编写的程序必须要与编译时为止的类一起工作，如有可能，就应该仅仅使用反射机制来实例化对象，而访问对象时则使用编译时已知的某个接口或者超类。

##### 54.谨慎的使用本地方法
JNI允许Java应用程序可以调用本地方法，所谓本地方法是指本地程序设计语言（c或者c++）来编写的特殊方法。本地方法在本地语言中可以执行任意的计算任务，并返回到Java程序设计语言。

本地方法主要有三种用途：
- 1：它们提供了访问特定于平台的机制的能力，比如访问注册表和文件锁。
- 2：访问遗留代码库的能力，从而可以访问遗留数据；
- 3：可以通过本地语言，编写应用程序中性能部分，提高系统的性能。（不提倡）

缺点：
- 本地语言不是安全的，所以，使用本地方法的应用程序也不再能免受内存毁坏错误的影响；
- 难于阅读；
- 难于移植；
总结：极少数情况下会使用本地方法来提高性能，如果你必须使用本地方法来访问底层资源或者遗留代码库，也要尽可能少用本地代码，并且要全面进行测试。本地代码汇总的一个Bug就有可能破坏整个应用程序。

##### 55.慎重的进行优化
有关优化的三条格言：
- 1：很多计算上的过失都被归咎于效率，而不是任何其他的原因---甚至包括盲目的做傻事。
- 2：不要去计较效率上的一些小小的得失，在97%的情况下，不成熟的优化才是一切问题的根源。
- 3:优化两条规则：1）不要进行优化 ；2）（专家）：还是不要进行优化，在你还没有绝对清晰的未优化方案之前，请不要进行优化。
总结：努力编写好程序，速度自然会随着而来，在设计系统的时候，特别是在设计api、线路层协议、永久数据格式的时候，一定要考虑性能的因素。当你构建完系统后要测试性能，如果它足够快，任务完成。如果不够快，可以再性能剖析器的帮助下，找到问题的根源，最后设法优化系统中相关部分。第一个不住是检查所选择的算法,再多的底层优化也无法弥补算法的选择不当。

##### 56.遵循普遍接受的命名惯例
![image](https://raw.githubusercontent.com/YoucanyouUp1/gitTest/master/naming.png)
注意：转换对象类型的方法、返回不同类型的独立对象的方法，通常使用toType,如toString和toArray。返回视图的方法使用asType，如asList。返回一个与被调对象通知的基本类型的方法使用typeValue。静态工厂常用名称：valueOf,of,getInstance,newInstance,getType和newType。

总结：把标准的命名惯例当作一中内在的机制来看待，并且学着用他们作为第二特性，字面惯例非常直接和明确，语法惯例更加复杂和松散。

#### 九、异常

---
有效使用异常，可以提高程序的可读性、可靠性、可维护性。

##### 57.只针对异常的情况才使用异常
基于异常的循环模式不仅模糊了代码的意图，降低了它的性能，而且它还不能保证正常工作。
- 注意：异常应该只用于异常的情况下，它们永远不应该用于正常的控制流。

##### 58.对可恢复的情况使用受检异常，对编程错误使用运行时异常
三种可抛出情况：受检的异常、运行时异常和错误。
1：在决定是使用受检的异常还是非受检的异常时：如果使用调用者适当的恢复，就使用受检的异常。通过抛出受检的异常，强迫调用者使用catch字句中处理该异常，或者将它传播出去。API的设计者让API用户面对受检的异常，以此强制用户从这个异常条件中恢复。用户可以忽视这样的强制要求，只需捕获并忽视即可，但这往往不是好办法。
2：有两种未受检的可抛出结构：运行时异常和错误。在行为上两者是等同的。他们不需要也不应该被捕获的可抛出结构。
总结：受检异常往往指明了可恢复的条件。让客户了解问题所在，恢复使用。

##### 59.避免不必要的使用受检的异常
受检的异常时Java程序设计语言的一个很好的特性，与返回代码不同，它们强迫程序员处理异常的条件，大大增强了可靠性。
- 但是多了，会使程序看起来复杂。

##### 60.优先使用标准的异常
专家级程序员和缺乏经验的程序员的主要区别是，代码重用。
![image](https://raw.githubusercontent.com/YoucanyouUp1/gitTest/master/exceptin.png)
总结：选择重用那个异常并不总是那么精确，因为上表中的“使用场合”并不是相互排斥的。

##### 61.抛出与抽象相对应的异常
- 如果方法抛出的异常与它所执行的任务没有明显的联系，这种情形将会使人不知所措。
总结：如果不能阻止或者处理来自更底层的异常，一般的做法是使用异常转译，除非底层方法碰巧可以保证它抛出的所有异常对高层也合适才可以将异常从底层传播到高层。异常链对高层和底层异常都提供了最佳的功能：它允许抛出适当的高层异常，同时又能捕获底层的原因进行失败分析。

##### 62.每个方法抛出的异常都要有文档

废话！

##### 63.在细节消息中包含能捕获失败的消息
总结：为异常的失败捕获信息提供一些访问方法是合适的。提供这样的方法对于受检的异常比对于未受检的异常更为重要，因为失败---捕获信息对于从失败恢复非常有用。

##### 64.努力使失败保持原子性
当对象抛出异常之后，通常我们期望这个对象仍然保持在一中定义良好的可用状态之中，即使失败是发生在执行某个操作的过程之中，对于受检的异常而言，更为重要。失败的方法调用应该使对象保持对象调用之前的状态。
方法：
- 1：设计一个不可变的对象；
- 2：可变对象：在执行操作之前检查参数的有效性。（1）在执行操作之前检查参数的有效性；2）调整计算处理过程的顺序，使得任何可能会失败的计算部分都在对象被修改之前发生。3）不常用，编写一段恢复代码；4：在对象的一份临时拷贝上执行操作，当操作完成之后再用临时拷贝中的结果代替对象的内容）；
总结：作为方法规范的一部分，产生的任何异常都应该让对象保持在该方法调用之前的状态。

##### 65.不要忽略异常
因常常被违反，因此值得再次提起来。
- 总结：保存错误的状态信息，方便调试。

#### 十、并发

线程机制允许同时进行多个活动。大部分事情是需要并发，而且并发能从多核处理器中获得好的性能的一个条件。

##### 66.同步访问共享的可变数据

关键字synchronized可以保证在同一时刻，只有一个线程可以执行某个方法，或者某一个代码块。

同步：正确的而是用同步可以保证没有任何方法会看到对象处于不一致的状态中。如果没有同步，一个线程的变化就不能被其他线程看到。同步不仅阻止一个线程看到对象处于不一致的状态中，它还可以保证进入同步方法或者同步代码块的每个线程，都看到由同一个锁保护的之前所有的修改效果。

java语言规范保证读或者写一个变量是原子的，除非这个变量的类型是long或者double。
错误的概念：在读或写原子数据的时候，应该避免使用同步。
虽然语言规范保证了线程再读取原子数据的时候，不会看到任意数值，但是它并不保证一个线程写入的值对于另一个线程是可见的。为了在线程之间进行可靠地通信，也为了互斥访问，同步时必要的。java的内存模型，规定了一个线程所做的变化何时以及如歌变成对其他线程可见。
如：
```
public class StopThread {
	private static boolean stopRequested;

	public static void main(String[] args) throws InterruptedException {
		Thread backgroundThread = new Thread(new Runnable() {
			public void run() {
				int i = 0;
				while (!stopRequested)
					i++;//这个代码将永远执行下去被虚拟机转换成{ if(!done)  while(true) i++ ;                     }
			}
		});
		backgroundThread.start();

		TimeUnit.SECONDS.sleep(1);
		stopRequested = true;
	}
}
```
修改成同步之后：

```
public class StopThread {
	private static boolean stopRequested;

	private static synchronized void requestStop() {
		stopRequested = true;
	}

	private static synchronized boolean stopRequested() {
		return stopRequested;
	}

	public static void main(String[] args) throws InterruptedException {
		Thread backgroundThread = new Thread(new Runnable() {
			public void run() {
				int i = 0;
				while (!stopRequested())
					i++;
			}
		});
		backgroundThread.start();

		TimeUnit.SECONDS.sleep(1);
		requestStop();
	}
}

读写都必须为同步
```
上边的同步方法：只是为了它的通信效果，而不是互斥访问；（因为是原子的）

可以通过volatile很好的解决该问题，虽然volatile修饰符不执行互斥访问，但它可以保证任何一个线程的读取该域的时候都将看到最近刚刚被写入的值；
```
public class StopThread {
	private static volatile boolean stopRequested;

	public static void main(String[] args) throws InterruptedException {
		Thread backgroundThread = new Thread(new Runnable() {
			public void run() {
				int i = 0;
				while (!stopRequested)
					i++;
			}
		});
		backgroundThread.start();

		TimeUnit.SECONDS.sleep(1);
		stopRequested = true;
	}
}
```
所以最佳办法是：不共享可变的数据。要么共享不可变的数据，要么压根不共享。换句话说，将可变数据限制在单个线程中。

让一个线程在短时间内修改一个数据对象，然后与其他线程共享，这是可以接受的，只同步共享对象引用的动作。然后其他线程没有进一步的同步也可以读取对象，只要它没有再被修改。这种对象被事实上不可变的。这种一个线程传递给其他线程被称作安全发布：方法1）可以将它保存在静态域中作为类初始化的一部分；2）可以将它保存到volatile域、final域或者通过正常锁定访问的域中；3）或者可以将它放在并发的集合中。

总结：当多个线程共享可变数据的时候，每个读或者写数据的线程都必须是执行同步。如果没有同步，就无法保证一个线程所做的修改可以被另一个线程获知。未能同步共享可变数据会造成程序的活性失败和安全性失败。这样的失败是最难以调试的。他们可能是间歇性的，且与时间相关，程序的行为在不同的vm上可能根本不同。如果只需要线程之间的交互通信，而不需要互斥。volatile修饰符就是一种可以接受的同步形式，但要正确的使用它需要一些技巧。

##### 67.避免过度同步

66条告诉我们缺少同步的危险，这条相反。过度同步可能导致性能降低、死锁、甚至不确定的行为。
为了避免活性失败和安全性失败，在一个呗同步的方法或者代码块中，永远不要放弃客户端的控制。
介绍了一种同步代码块中使用外来方法造成的：失败和死锁；
- 解决问题的方法：
- 1：将外来方法的调用移除代码块；
- 2：java类库提供了一个并发集合。使用CopyOnWriteArrayList,其中没有任何显示的同步。

通常，应该在同步区域内做尽可能少的工作。获得锁，检查共享数据，根据需要转换数据，然后放掉锁。如果有个很耗时的动作，想办法移到同步区域外。

同步成本问题：不是指获取锁所花费的CPU的时间，而是指失去了并行的机会，以及因为需要确保每个核都有一个一致的内存视图而导致的延迟。过度同步也限制vm优化代码执行的能力。

如果你在内部同步了类，就可以使用不同的方法来实现高并发，例如分拆锁、分离锁和非阻塞并发控制。

总结：为了避免死锁和数据破坏，千万不要从同步区域内部调用外来方法。更为一般的讲，要尽量限制同步区域内部的工作量。当你设计一个可变类的时候，要考虑一下它们是否应该自己完成同步操作。这要比永远不要过度同步来的更重要。只有当你有足够的理由一定要在内部同步类的时候，才应该这么做，同时还应该将这个决定清楚的写到文档中。

##### 68.executor和task优先于线程
灵活的基于接口的任务执行工具Executor,创建工作队列。如果想让不止一个线程来处理来自这个队列的请求，只要调用一个不同的静态工厂，这个工厂创建了一种不同的executor Service，称作线程池。java.util.concurrent.Executors类包含了静态工厂，能为你提供所需的大多数executor,想来特别的，直接使用ThreadPoolExecutor类。

- 建议：
- 1：编写小程序，或者轻量级的服务器，使用Executors.newCachedThreadPool是个不错的选择；
- 2：对于大负载的服务器，由于缓存的线程池中，被提交的任务没有排成队列，而是直接交给线程执行，这样当cpu负载过大时，有更多任务时就会创建更多线程，情况会变的更糟。因此最好使用newFixedThreadPool,或者最大限度的控制它就直接使用ThreadPoolExecutor类。


总结：应该尽量不要编写自己的工作队列，而且是还应该尽量不直接使用线程。现在关键的抽象不再是Thread了，它以前既充当工作单元，又是执行机制。现在工作单元和执行机制分开。现在关键的抽象是工作单元，称作任务。任务有两种，Runable机器近似callable（它与Runable类似，但它返回值）。执行任务的通用机制是executor service。本质上Executor所做的工作是执行。它也可以代替Timer的东西，即ScheduledThreadPoolExecutor。虽然timer使用起来更加容易，但是被调度的线程池executor更加灵活。timer只用一个线程来执行任务。这在对长期运行的任务时，会影响到定时的准确性。如果timer唯一的线程抛出未捕获的异常，timer就会停止执行。被调度的线程池executor支持多个线程，并且优雅的从抛出未受检异常的任务中恢复。

##### 69.并发工具优先于wait和notigy
- 几乎没有理由再使用wait和notify了，使用高级的并发工具来代替。
- 1：Executor Framework
- 2:并发集合
- 3：同步器
下面分析2和3：
并发集合为标准的集合接口（List、Quene、Map）提供了高性能的并发实现。这些在内部自己管理同步。因此并发集合中不可能排除并发活动。将它锁定没有什么作用，只会使程序的速度变慢。
优先使用ConcurrentHashMap而不是synchronizedMap或者Hashtable，只要用并发Map代替老式的同步Map，就可以极大的提升并发应用程序的性能。
有些集合接口已经通过阻塞操作进行了扩展，他们会一直等待（阻塞）到可以成功执行为止。例如BlockingQueue扩展了Queue接口，并添加了包括take在内的几个方法，它从队列中删除并返回了头元素，如果为空，就等待。它用于工作队列，生产者消费者队列。

同步器：是一些使线程能够等待另一个线程的对象，允许他们协调动作，最常用的同步器：CountDownLatch和Semaphore。较不常用的是CyclicBarrier和Exchanger。
CountDown Latch 倒计时锁存器：是一次性的障碍，允许一个或者多个线程等待一个或多个其他线程来做某些事情。countdowmLatch的唯一构造器带有一个int类型的参数。这个int参数是指允许所有在等待的线程被处理之前，必须在锁存器上盗用countDown方法的次数；

```
public class ConcurrentTimer {
	private ConcurrentTimer() {
	} // Noninstantiable

	public static long time(Executor executor, int concurrency,
			final Runnable action) throws InterruptedException {
		final CountDownLatch ready = new CountDownLatch(concurrency);
		final CountDownLatch start = new CountDownLatch(1);
		final CountDownLatch done = new CountDownLatch(concurrency);

		for (int i = 0; i < concurrency; i++) {
			executor.execute(new Runnable() {
				public void run() {
					ready.countDown(); // Tell timer we're ready
					try {
						start.await(); // Wait till peers are ready
						action.run();
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					} finally {
						done.countDown(); // Tell timer we're done
					}
				}
			});
		}

		ready.await(); // Wait for all workers to be ready
		long startNanos = System.nanoTime();
		start.countDown(); // And they're off!
		done.await(); // Wait for all workers to finish
		return System.nanoTime() - startNanos;
	}
}

```

为了维护和看懂其他代码：
1：wait方法被用来使线程等待某个条件。它必须在同步区域内部被调用，这个同步区域将队形锁定在了调用wait方法的对象上。始终应该使用wait循环模式来调用wati方法；永远不要在循环之外调用wait方法。循环会在等待之前和之后测试条件。
在等待之前测试条件，当条件已经成立时就跳过等待，这对于确保活性是必要的。如果条件已经成立，并且在线程等待之前，notify(notifyAll)方法已经被调用，则无法保证该线程将会从等待中苏醒过来。
优先总是应该使用notifyAll。如果从优化的角度来看，如果处于等待状态的所有线程都在等待同一个条件，而每次只有一个线程可以从这个条件中被唤醒,那么使用notify。

总结：直接使用wait和notify就像使用并发汇编语言进行编程一样，而concurrent则提供了更高级的语言。没有理由再新代码中使用wait和notify。

##### 70.线程安全性的文档化
当一个类的实例或者静态方法被并发使用的时候，这个类的行为如何，是该类与其客户端程序建立的约定的重要组成部分。

总结：每个类都应该利用字斟句酌的说明和线程安全的注解，清楚的在文档中说明它的线程安全属性。synchronized修饰符与这个文档毫无关系。有条件的线程安全类必须在文档中指明。灵活的使用并发控制采用更加复杂的方法。

##### 71.慎用延迟初始化
延迟初始化时延迟到需要域的值时才将它初始化这种行为。如果永远不需要这个值，这个域就永远不会被初始化。虽然是优化但是它打破类和实例初始化中的有害循环。

除非绝对必要，否则就不要这么做；
总结：大多数的域应该正常的进行初始化化，而不是延迟初始化。如果为了达到性能目标，或者为了破幻有害的初始化循环，而必须延迟初始化一个域，就可以使用相应的延迟初始化方法。对于实力域，就使用双重检查模式。对于静态域，则使用。对于可以接收重复初始化的实例域，也可以考虑使用单冲检查模式。
```
synchronized FieldType getField2() {
		if (field2 == null)
			field2 = computeFieldValue();
		return field2;
	}
```
```
private volatile FieldType field4;

	FieldType getField4() {
		FieldType result = field4;
		if (result == null) { // First check (no locking)
			synchronized (this) {
				result = field4;
				if (result == null) // Second check (with locking)
					field4 = result = computeFieldValue();
			}
		}
		return result;
	}

```

##### 72.不要依赖于线程调度器
编写健壮的、相应良好的、可一直的多线程应用程序，最好的办法是确保可运行线程的平均数量不明显多余处理器的数量。

总结：不要让应用程序的正确性依赖于线程调度器。否则，结果将不健壮，也不具有可移植性。作为推论，不要依赖Thread.yield或者线程优先级。这些设施仅仅对调度器做些暗示。线程优先级（影响线程活性）可以提高一个已经能够正常工作的程序的服务质量，单永远不应该用来修证一个原本并不能工作的程序。

##### 73.避免使用线程组
除了线程、锁和监视器之外，线程系统还提供了一个基本的抽象，即线程组。

总结：线程组并没有提供太多的功能，提供的功能还有缺陷，最后把它忽略掉。如果系统需要设计一个类需要处理线程的逻辑组，使用executo。

#### 十一、序列化

---
它提供了一个框架，用来将对象编码成字节流，并从字节流编码中重新构建对象。
一旦对象被序列化后，它的编码就可以从一台正在运行的虚拟化被传递到另一台虚拟机上，或者被存储到磁盘上，供以后反序列化时用。

##### 74.谨慎的实现Serializable接口
让一个类实现序列化，加入“implements Seriable”字样即可。但是一个类可被序列化的直接开销非常低，但是序列化而付出的长期开销往往是实实在在的。
代价：
1：实现Serializable接口而付出的最大代价是，一旦一个类被发布，就大大降低了“改变这个类的实现”的灵活性。需要制定uuid
2：实现Serializable的第二个代价是，它增加了出现Bug和安全漏洞的可能性。（非法访问）
3：随着类发行新的版本，相关的测试负担也增加了。
用处：
1：如果一个类将要加入到某个框架中，并且该框架依赖于序列化来实现对象的传输或者持久化，对于这个类来说，实现serializable接口就非常有必要。更进一步，如果这个类要成为这个类的一个组件，并且后者必须实现Serializable接口，或者前者也实现了Serializable接口，它就会更易于被后者使用。

经验：比如Date和BigInteger这样的值应该实现Seriablezable,大多数集合类也应该如此。代表活动实体的类，比如线程池，一般不应该实现Serializable。为了继承而设计的类应该进可能少的实现Serializable接口，用户的接口也应该尽可能少的继承Serializable接口。

不觉明历：为了继承而设计的类中，真正实现了Serializable接口的有Throwable类、Component和HttpServlet抽象类。因为Throwable类实现了Serializable接口，所以RMI(远程方法调用协议)的异常可以从服务器端传到客户端。Component实现了Serializable接口，因此GUI(图形用户界面)可以发送保存和恢复。HttpServlet实现了Serializable接口==，因此会话状态可以缓存。==

如果一个专门为了继承而设计的类是不可序列化的，就不可能编写出可序列化的子类。特别是，如果超类没有提供可访问的无参构造器，子类也不可能做到可序列化。因此，对于为了继承而设计的不可序列化的类，就应该考虑提供一个无参构造器。
```
public abstract class AbstractFoo {
	private int x, y; // Our state

	// This enum and field are used to track initialization
	private enum State {
		NEW, INITIALIZING, INITIALIZED
	};

	private final AtomicReference<State> init = new AtomicReference<State>(
			State.NEW);

	public AbstractFoo(int x, int y) {
		initialize(x, y);
	}

	// This constructor and the following method allow
	// subclass's readObject method to initialize our state.
	protected AbstractFoo() {
	}

	protected final void initialize(int x, int y) {
		if (!init.compareAndSet(State.NEW, State.INITIALIZING))
			throw new IllegalStateException("Already initialized");
		this.x = x;
		this.y = y;
		// Do anything else the original constructor did
		init.set(State.INITIALIZED);
	}

	// These methods provide access to internal state so it can
	// be manually serialized by subclass's writeObject method.
	protected final int getX() {
		checkInit();
		return x;
	}

	protected final int getY() {
		checkInit();
		return y;
	}

	// Must call from all public and protected instance methods
	private void checkInit() {
		if (init.get() != State.INITIALIZED)
			throw new IllegalStateException("Uninitialized");
	}
	// Remainder omitted
}
```

```
public class Foo extends AbstractFoo implements Serializable {
	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();

		// Manually deserialize and initialize superclass state
		int x = s.readInt();
		int y = s.readInt();
		initialize(x, y);
	}

	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();

		// Manually serialize superclass state
		s.writeInt(getX());
		s.writeInt(getY());
	}

	// Constructor does not use the fancy mechanism
	public Foo(int x, int y) {
		super(x, y);
	}

	private static final long serialVersionUID = 1856835860954L;
}
```
内部类不应该实现Serializable。

总结：千万不要认为实现Serializable接口会很容易。除非一个类在用了一段时间后会被抛弃，否则，实现Serializable接口是个严肃的承诺。如果一个类是为了继承而设计的,就要更加小心。对于这样的类，在允许子类实现Serializable接口或者禁止子类实现Serializable两者之间的一个折中的方案是，提供一个可访问的无参构造器。这种设计方案允许（但不要求）子类实现Serializable 接口。

##### 75.考虑使用自定义的序列化形式
当你时间紧迫的情况下设计一个类时，一般合理的做法是把工作重心集中在设计最佳API上。
如果没有先认真考虑默认的序列化形式是否合适，则不要贸然接收。

考虑以一个对象为跟的对象图，相对于它的物理表示法而言，该对象的默认序列化形式是一种比较有效的编码方式。
如果一个对象的物理表示法等同于它的逻辑内容，可能就适合于使用默认的序列化形式。

当一个对象的物理表示法与它的逻辑数据内容有实质性的区别时，使用默认序列化形式有4个缺点：
1：它使这个类的导出API永远的束缚在该类的内部表示法上。
2：它会消耗过多的空间。
3：它消耗过多的时间。
4：它会引起栈溢出。
```
public final class StringList implements Serializable {
	private transient int size = 0;
	private transient Entry head = null;

	// No longer Serializable!
	private static class Entry {
		String data;
		Entry next;
		Entry previous;
	}

	// Appends the specified string to the list
	public final void add(String s) {
		// Implementation omitted
	}

	/**
	 * Serialize this {@code StringList} instance.
	 *
	 * @serialData The size of the list (the number of strings it contains) is
	 *             emitted ({@code int}), followed by all of its elements (each
	 *             a {@code String}), in the proper sequence.
	 */
	private void writeObject(ObjectOutputStream s) throws IOException {
		s.defaultWriteObject();
		s.writeInt(size);

		// Write out all elements in the proper order.
		for (Entry e = head; e != null; e = e.next)
			s.writeObject(e.data);
	}

	private void readObject(ObjectInputStream s) throws IOException,
			ClassNotFoundException {
		s.defaultReadObject();
		int numElements = s.readInt();

		// Read in all elements and insert them in list
		for (int i = 0; i < numElements; i++)
			add((String) s.readObject());
	}

	private static final long serialVersionUID = 93248094385L;
	// Remainder omitted
}
```
总结：不太懂
![image](https://raw.githubusercontent.com/YoucanyouUp1/gitTest/master/Seriablizable.png)

76.保护性的编写readObject方法

不严格的说，readObject是一个“用字节流作为唯一参数”的构造器。
```
public final class Period implements Serializable {
	private Date start;
	private Date end;

	/**
	 * @param start
	 *            the beginning of the period
	 * @param end
	 *            the end of the period; must not precede start
	 * @throws IllegalArgumentException
	 *             if start is after end
	 * @throws NullPointerException
	 *             if start or end is null
	 */
	public Period(Date start, Date end) {
		this.start = new Date(start.getTime());
		this.end = new Date(end.getTime());
		if (this.start.compareTo(this.end) > 0)
			throw new IllegalArgumentException(start + " after " + end);
	}

	public Date start() {
		return new Date(start.getTime());
	}

	public Date end() {
		return new Date(end.getTime());
	}

	public String toString() {
		return start + " - " + end;
	}

	// readObject method with validity checking - Page 304
	// This will defend against BogusPeriod attack but not MutablePeriod
	这种校验并不能保证该类防止攻击，从而违反了不可变的约束
	// private void readObject(ObjectInputStream s)
	// throws IOException, ClassNotFoundException {
	// s.defaultReadObject();
	//
	// // Check that our invariants are satisfied
	// if (start.compareTo(end) > 0)
	// throw new InvalidObjectException(start +" after "+ end);
	// }

	// readObject method with defensive copying and validity checking - Page 306
	// This will defend against BogusPeriod and MutablePeriod attacks.
	// private void readObject(ObjectInputStream s)
	// throws IOException, ClassNotFoundException {
	// s.defaultReadObject();
	//
	// // Defensively copy our mutable components
	// start = new Date(start.getTime());
	// end = new Date(end.getTime());
	//
	// // Check that our invariants are satisfied
	// if (start.compareTo(end) > 0)
	// throw new InvalidObjectException(start +" after "+ end);
	// }
}
```
来一种判定：增加一个共有的构造器，其参数对应于该对象中的每个非transient的域，并且无论参数值是什么，都是不进行检查就可以保存到相应的域中的。对于这样的做法，你是否感受到很舒服？如果否，就必须提供一个显示readObject方法,并且它必须执行构造器所要求的所有有效性检查和保护性拷贝。另一种方法是，可以使用序列化代理模式，建78条。

对于非final的可序列化的类，在readObject方法和构造器之间还有其他类似的地方。readObject方法不可以调用可被覆盖的方法。无论是直接还是间接都是不可以的。
总结：
![image](https://raw.githubusercontent.com/YoucanyouUp1/gitTest/master/readObject.png)

##### 77.对于实例控制，枚举类型优先于readResolve
readResolve特性允许你用readObject创建的实例代替另一个实例。对于一个正在被反序列化的对象，如果它的类定义了一个readResolve方法，并且具备正确的声明，那么在反序列化之后，新建对象上的readResolve方法就会被调用。然后，该方法返回的对象引用将被返回，取代新建的对象。这个特性的绝大多数用法中，指向新建对象的引用不需要再被保留，因此立即成为垃圾回收的对象。

事实上，如果依赖readResolve进行实例控制，带有对象引用类型的所有实例域则都必须声明为transient的。否则攻击者就可能在readResolve方法被运行之前，进行类似上例中的攻击。

总结：你应该尽可能地使用枚举类型来实施实例控制的约束条件，如果做不到，同时又需要一个即可序列化优势实例受控的类，就必须提供一个readResolver方法,并确保该类的所有实例域都是基本类型或者是transient的。


##### 78.考虑用序列化代理代替序列化实例
决定实现Serializable接口，会增加出错和出现安全问题的可能性，因为它导致实例要利用语言之外的机制来创建，而不是普通的构造器。然而，有一种方法可以极大的减少这些风险。这种方法就是可以极大的减少这些风险。序列化代理模式。

1：为可序列化的类设计一个私有的静态嵌套类，精确地表示外围类的实例的逻辑状态。这个嵌套类被称作序列化代理，它应该有一个单独的构造器，器参数类型就是那个外围类。这个构造器只从它的参数中复制数据：它不需要进行任何一致性检查或者保护性拷贝。从设计的角度来看，序列化代理的默认序列化形式是外围类最好的序列化形式。 外围类及其序列化代理都必须声明实现Serializable接口。
```
public final class Period implements Serializable {
	private final Date start;
	private final Date end;

	/**
	 * @param start
	 *            the beginning of the period
	 * @param end
	 *            the end of the period; must not precede start
	 * @throws IllegalArgumentException
	 *             if start is after end
	 * @throws NullPointerException
	 *             if start or end is null
	 */
	public Period(Date start, Date end) {
		this.start = new Date(start.getTime());
		this.end = new Date(end.getTime());
		if (this.start.compareTo(this.end) > 0)
			throw new IllegalArgumentException(start + " after " + end);
	}

	public Date start() {
		return new Date(start.getTime());
	}

	public Date end() {
		return new Date(end.getTime());
	}

	public String toString() {
		return start + " - " + end;
	}

	// Serialization proxy for Period class - page 312
	private static class SerializationProxy implements Serializable {
		private final Date start;
		private final Date end;

		SerializationProxy(Period p) {
			this.start = p.start;
			this.end = p.end;
		}

		private static final long serialVersionUID = 234098243823485285L; // Any
																			// number
																			// will
																			// do
																			// (Item
																			// 75)

		// readResolve method for Period.SerializationProxy - Page 313
		private Object readResolve() {
			return new Period(start, end); // Uses public constructor
		}
	}

	// writeReplace method for the serialization proxy pattern - page 312
	private Object writeReplace() {
		return new SerializationProxy(this);
	}

	// readObject method for the serialization proxy pattern - Page 313
	private void readObject(ObjectInputStream stream)
			throws InvalidObjectException {
		throw new InvalidObjectException("Proxy required");
	}
}

```

序列化代理模式有两个局限：它不能与可以被客户端扩展的类兼容。它也不能对对象图中包含循环的某些类兼容。如果你企图从一个对象的序列化代理的readResolve方法内部调用这个对象的中的方法， 就会得到异常因为你还没有这个对象，智有他的序列化代理。

序列化dialing模式所增强的功能和安全性并不是没有代价的。

总结：每当你发现自己必须在一个不能被客户端扩展的类编写readObject或者writeObject方法的时候，就应该考虑使用序列化代理模式。要想稳健的将带有重要约束条件的对象序列化时，这种模式可能是最容易的。


OVER!
