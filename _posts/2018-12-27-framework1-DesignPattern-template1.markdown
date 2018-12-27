---
layout: post
title:  模板设计模式
date:   2018-12-27 23:52:12 +08:00
category: 设计模式
tags: 设计模式
comments: true
---

* content
{:toc}

设计模式中行为类模式，模板设计模式。














## 模板(方法)模式（template）

应用场景：定义一个操作中的算法的骨架，（1.2.3.4.。。。）,而将一些步骤延迟到子类中，使子类可以改变一个算法的结构即可重定义该算法的某些特定步骤。
>* Template Method 模式一般是需要继承的。 这里想要探讨另一种对 Template Method 的理解。 Spring
>* 中的 JdbcTemplate， 在用这个类时并不想去继承这个类， 因为这个类的方法太多， 但是我们还是想用
>* 到 JdbcTemplate 已有的稳定的、 公用的数据库连接， 那么我们怎么办呢？ 我们可以把变化的东西抽出
>* 来作为一个参数传入 JdbcTemplate 的方法中。 但是变化的东西是一段代码， 而且这段代码会用到
>* JdbcTemplate 中的变量。 怎么办？ 那我们就用回调对象吧。 在这个回调对象中定义一个操纵
>* JdbcTemplate 中变量的方法， 我们去实现这个方法， 就把变化的东西集中到这里了。 然后我们再传入
>* 这个回调对象到 JdbcTemplate， 从而完成了调用。 这就是 Template Method 不需要继承的另一种实
>* 现方式。

这里就介绍这种复杂的实用的模板方法模式

定义一个Template

```
public class JdbcTemplate {

    private DataSource dataSource;
    public JdbcTemplate(DataSource dataSource){
        this.dataSource = dataSource;
    }

    private Connection getConnection() throws  Exception{
        return this.dataSource.getConnection();
    }

    private PreparedStatement createPreparedStatement(Connection conn,String sql) throws  Exception{
        return  conn.prepareStatement(sql);
    }


    private ResultSet executeQuery(PreparedStatement pstmt,Object [] values) throws  Exception{
        for (int i = 0; i <values.length; i ++){
            pstmt.setObject(i,values[i]);
        }
        return  pstmt.executeQuery();
    }

    private void closeStatement(Statement stmt) throws  Exception{
        stmt.close();
    }

    private void closeResultSet(ResultSet rs) throws  Exception{
        rs.close();
    }

    private void closeConnection(Connection conn) throws  Exception{
        //通常把它放到连接池回收
    }



    private List<?> parseResultSet(ResultSet rs,RowMapper rowMapper) throws  Exception{
        List<Object> result = new ArrayList<Object>();
        int rowNum = 1;
        while (rs.next()){

            **result.add(rowMapper.mapRow(rs,rowNum ++));**
        }
        return result;
    }


    public List<?> executeQuery(String sql,RowMapper<?> rowMapper,Object [] values){
        try {

            //1、获取连接
            Connection conn = this.getConnection();
            //2、创建语句集
            PreparedStatement pstmt = this.createPreparedStatement(conn,sql);
            //3、执行语句集，并且获得结果集
            ResultSet rs = this.executeQuery(pstmt,values);
            //4、解析语句集
           ist<?> result = this.parseResultSet(rs,rowMapper);

            //5、关闭结果集
            this.closeResultSet(rs);
            //6、关闭语句集
            this.closeStatement(pstmt);
            //7、关闭连接
            this.closeConnection(conn);

            return result;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}


```
具体的Dao

```
public class MemberDao {

    //为什么不继承，主要是为了解耦
    private JdbcTemplate JdbcTemplate = new JdbcTemplate(null);

    public List<?> query(){
        String sql = "select * from t_member";
        return JdbcTemplate.executeQuery(sql,new RowMapper<Member>(){
            @Override
            public Member mapRow(ResultSet rs, int rowNum) throws Exception {
                Member member = new Member();
                member.setUsername(rs.getString("username"));
                member.setPassword(rs.getString("password"));
                member.setAge(rs.getInt("age"));
                member.setAddr(rs.getString("addr"));
                return member;
            }
        },null);
    }


}

```
这里只是对RowMapper的mapRow进行了重写
定义RowMappper接口
```
public interface RowMapper<T> {

    public T mapRow(ResultSet rs, int rowNum) throws Exception;

}

```
实体类

```
public class Member {

    private String username;
    private String password;
    private String nickName;

    private int age;
    private String addr;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}

```
测试

```
public class MemberDaoTest {

    public static void main(String[] args) {

    	MemberDao memberDao = new MemberDao();
        memberDao.query();

    }
}

```
