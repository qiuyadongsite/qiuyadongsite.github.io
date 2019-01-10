---
layout: post
title:  观察者设计模式
date:   2019-01-10 23:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中行为类模式，观察者设计模式。














## 观察者模式（oberver）,目的是（解耦）

应用场景：观察者和被观察者之间没有必然联系

当一件事件触发时，如果关注他就给它添加监听和事件，否则不关心。

通用事件类
事件

```
public class Event {

    //事件源
    private Object source;
    //通知目标
    private Object target;
    //回调
    private Method callback;
    //触发
    private String trigger;

    private long time;

    public Event(Object target, Method callback) {
        this.target = target;
        this.callback = callback;
    }


    public Object getSource() {
        return source;
    }


    public Object getTarget() {
        return target;
    }

    public void setTarget(Object target) {
        this.target = target;
    }

    public Method getCallback() {
        return callback;
    }

    public void setCallback(Method callback) {
        this.callback = callback;
    }

    public String getTrigger() {
        return trigger;
    }

    @Override
    public String toString() {
        return "Event{" +
                "\n\tsource=" + source + ",\n" +
                "\ttarget=" + target + ",\n" +
                "\tcallback=" + callback + ",\n" +
                "\ttrigger='" + trigger + '\'' + "\n" +
                '}';
    }

    public long getTime() {
        return time;
    }

    Event setTime(long time) {
        this.time = time;
        return this;
    }

    Event setSource(Object source) {
        this.source = source;
        return this;
    }

    Event setTrigger(String trigger) {
        this.trigger = trigger;
        return this;
    }
}

```

事件的注册跟监听

```
/**
 * 事件的注册和监听
 * Created by Tom on 2018/3/17.
 */
public class EventLisenter {

    //Map相当于是一个注册器
    protected Map<Enum,Event> events = new HashMap<Enum,Event>();

    public void addLisenter(Enum eventType,Object target,Method callback){
        //注册事件
        //用反射调用这个方法
        events.put(eventType,new Event(target,callback));
    }

    private void trigger(Event e){
        e.setSource(this);
        e.setTime(System.currentTimeMillis());

        try {
            e.getCallback().invoke(e.getTarget(),e);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }


    protected void trigger(Enum call){
        if(!this.events.containsKey(call)){ return ;}
        trigger(this.events.get(call).setTrigger(call.toString()));
    }


}
```
被观察者需要继承监听类

```
/**
 * 被观察者
 * 如果做过Swing开发的话，有一种似曾相识的感觉
 * Created by Tom on 2018/3/17.
 */
public class Mouse extends EventLisenter{

    public void click(){
        System.out.println("鼠标单击");
        this.trigger(MouseEventType.ON_CLICK);
    }


    public void doubleClick(){
        System.out.println("鼠标双击");
        this.trigger(MouseEventType.ON_DOUBLE_CLICK);
    }

    public void up(){
        System.out.println("鼠标弹起");
        this.trigger(MouseEventType.ON_UP);
    }

    public void down(){
        System.out.println("鼠标按下");
        this.trigger(MouseEventType.ON_DOWN);
    }


    public void wheel(){
        System.out.println("鼠标滚动");
        this.trigger(MouseEventType.ON_WHEEL);
    }

    public void move(){
        System.out.println("鼠标移动");
        this.trigger(MouseEventType.ON_MOVE);
    }

    public void over(){
        System.out.println("鼠标悬停");
        this.trigger(MouseEventType.ON_OVER);
    }

}
```

观察者，响应类

```
/**
 * 观察者
 *
 * 回调响应的逻辑，由自己实现

 */
public class MouseEventCallback {

    public void onClick(Event e){
        System.out.println("这是鼠标单击以后要执行的逻辑");
        System.out.println("=======触发鼠标单击事件========\n" + e);
    }

    public void onDoubleClick(Event e){
        System.out.println("=======触发鼠标双击事件========\n" + e);
    }

    public void onUp(Event e){
        System.out.println("=======触发鼠标弹起事件========\n" + e);
    }
    public void onDown(Event e){
        System.out.println("=======触发鼠标按下事件========\n" + e);
    }
    public void onMove(Event e){
        System.out.println("=======触发鼠标移动事件========\n" + e);
    }
    public void onWheel(Event e){
        System.out.println("=======触发鼠标滚动事件========\n" + e);
    }

    public void onOver(Event e){
        System.out.println("=======触发鼠标悬停事件========\n" + e);
    }




}

```
定义一个触发常量类型
```
public enum MouseEventType {
    ON_CLICK,
    ON_DOUBLE_CLICK,
    ON_UP,
    ON_DOWN,
    ON_WHEEL,
    ON_MOVE,
    ON_OVER;

}

```

测试

```
public class MouseTest {

    public static void main(String[] args) {

        /*
        * var input = document.getElementById("username");
        * input.addLisenter("click",function(){
        *
        *     alert("鼠标点击了这个文本框");
        *
        * });
        *
        *
        * */

        //观察者和被观察者之间没有必然联系
        //注册的时候，才产生联系


        //解耦


        try {
//            MouseEventCallback callback = new MouseEventCallback();
//            Method onClick = MouseEventCallback.class.getMethod("onClick", Event.class);


            //人为的调用鼠标的单击事件
            Mouse mouse = new Mouse();
//            mouse.addLisenter(MouseEventType.ON_CLICK, callback,onClick);

            mouse.click();

        }catch (Exception e){
            e.printStackTrace();
        }


    }

}


```
