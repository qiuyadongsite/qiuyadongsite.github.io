---
layout: post
title:  单点登录
date:   2019-05-21 21:52:12 +08:00
category: 开发经验
tags: 开发经验
comments: true
---

* content
{:toc}


开发经验将会记录开发中的惯例，单点登录是开发的第一步，这里将总结高并发项目中的登录问题。




















## 基本概念

### http协议

- 无状态性

Http 协议本身是无状态的，客户端只需要简单的向服务器请求下载某些文件，无论是客户端还是服务器都没必要记录彼此过去的行为，每一次请求之间是独立的，然后我们很快发现如果能够提供一些按照需要生成的动态信息会使 web 变得更加有用。

比如我们做了一个需要登录授权才能执行的动作，但是因为 http 协议没办法保存这个登录的用户的状态，因此当下一次再执行一个需要授权操作时，还需要再次登录。这将导致用户体验非常差。因此需要一种机制能够识别每次请求的用户，来实现会话保存的目的。

- Session

服务端提供了一种叫 Session 的机制，对于每个用户的请求，会生成一个唯一的标识。当程序需要为某个客户端的请求创建一个 session 的时候，服务器首先检查这个客户端的请求是否包含了一个 session 标识- session id；

如果已包含一个 session id 则说明以前已经为客户端创建过 session，服务器就按照 session id 把这个 session 检索出来使用（如果检索不到，会新建一个）；如果客户端请求不包含 sessionid，则为此客户端创建一个session 并且生成一个与此 session 相关联的 session id，session id 的值是一个既不会重复，又不容易被找到规律的仿造字符串。[客户端是如何存储 sessionid 的呢？]

Cookie

浏览器提供了一种叫 cookie 的机制，保存当前会话的唯一标识。每次 HTTP 请求，客户端都会发送相应的 Cookie 信息到服务端。客户端第一次请求，由于 cookie 中并没有携带 sessionid，服务端会创建一个 sessionid，写入到客户端的 cookie 中。以后每次请求，都会携带这个 id 给到服务器端。这样一来，便解决了无状态的问题。[如果客户端浏览器禁用了 cookie，一般会通过 URL 重写的方式来进行会话，也就是在 url 中携带 sessionid]

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/cookie0001.png)

### 集群环境下的 session 共享问题

如果网站请求流量较大，那么单台 tomcat 设备是无法承接这些流量的，这个时候就需要开始对服务器做集群。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/lb0001.png)

采用了多台机器做集群以后，就势必要通过一种机制来实现请求的路由。因为对于用户来说，我访问的是一个域名，至于后端应该请求到哪一台，用户并不需要关心。所以这里会使用负载均衡设备；

- 关于负载均衡

负载均衡的主要目的是：把用户的请求分发到多台后端的设备上，用以均衡服务器的负载。我们可以把负载均衡器划分为两大类：

硬件负载均衡器和软件负载均衡器。

  - 硬件负载

最常用的硬件负载设备有 F5 和 netscaler、Redware，F5是基于 4 层负载，netscaler 是 7 层负载。

所谓的四到七层负载均衡，就是在对后台的服务器进行负载均衡时，依据四层的信息或七层的信息来决定怎么样转发流量四层的负载均衡，就是通过发布三层的 IP 地址（VIP），然后
加四层的端口号，来决定哪些流量需要做负载均衡，转发至后台服务器，并记录下这个 TCP 或者 UDP 的流量是由哪台服务器处理的，后续这个连接的所有流量都同样转发到同一台服务器处理。

七层的负载均衡，就是在四层的基础上（没有四层是绝对不可能有七层的），再考虑应用层的特征，比如同一个 Web 服务器的负载均衡，除了根据 VIP(virtual ip)加 80 端口辨别是否需要处理的流量，还可根据七层的 URL、浏览器类别、语言来决定是否要进行负载均衡。举个例子，如果你的 Web 服务器分成两组，一组是中文语言的，一组是英文语言的，那么七层负载均衡就可以当用户来访问你的域名时，自动辨别用户语言，然后选择对应的语言服务器组进行负载均衡处理。

硬件负载均衡设备的有点是稳定性高、同时有专门的技术服务团队支撑。但是价格比较贵，一般的都要几十万起。所以那种大的企业，没有专业的运维团队。直接花钱买解决方案。

  - 软件负载

  比较主流的开源软件负载技术有: lvs、HAProxy、Nginx 等，对于小公司来说或者大型的互联网企业，基本都采用软件负载均衡技术来实现流量均衡。

不同的负载均衡技术有不同的特点，

比如 LVS 是基于 4 层的负载负载技术，抗负载能力比较强

HAProxy 和 Nginx 是基于 7 层的负载均衡技术，需要根据请求的 url 进行分流

- 负载均衡算法

引入负载均衡器以后，就势必需要一个负载均衡算法对请求进行转发，那么，常见的负载均衡算法有以下几种:

  - 轮询算法及加权轮询算

轮询法是指负载均衡服务器将客户端请求按顺序轮流分配到后端服务器上，以达到负载均衡的目的。

假设现在有 6 个客户端请求，2 台后端服务器。

当第一个请求到达负载均衡服务器时，负载均衡服务器会将这个请求分派到后端服务器 1；

当第二个请求到害时，负载均衡服务器会将这个请求分派到后端服务器 2。

然后第三个请求到达，由于只有两台后端服务器，故请求 3 会被分派到后端服务器 1

对于后端服务器的性能差异，可以对处理能力较好的服务器增加权重，这样，性能好的服务器能处理更多的任务。性能较差的服务器处理较少的任务。

假设有 6 个客户端请求，2 台后端服务器。后端服务器 1 被赋予权值 5，后端服务器 2 被赋予赋予权值 1。这样一来，客户端请求 1，2，3，4，5 都被分派到服务器 1 处理；客
户端请求 6 被分派到服务器 2 处理。接下来，请求 7，8，9，10，11 被分派到服务器 1，请求 12 被分派到服务器 2


  - 最小连接数

由于不同的客户端请求的操作对于后端来说复杂度是不同的，也就会导致服务端的处理时间也不一样。最小连接数法根据后端服务器当前的连接数情况，动态地选取其中积压连接数最小的一台服务器来处理当前的请求，尽可能提高后端服务器的利用效率，合理地将请求分流到每一台服务器。

假设客户端请求 1，2，3，4，5 已被分派给服务器 1 和服务器 2

  - 随机算法

随机法也很简单，就是随机选择一台后端服务器进行请求的处理。由于每次服务器被挑中的概率都一样，客户端的请求可以被均匀地分派到所有的后端服务器上。

  - 哈希算法

根据获取客户端的 IP 地址，通过哈希函数计算得到的一个数值，用该数值对服务器列表的大小进行取模运算，得到的结果便是客服端要访问服务器的序号。

采用源地址哈希法进行负载均衡，同一 IP 地址的客户端，当后端服务器列表不变时，它每次都会映射到同一台后端服务器进行访问。

## 问题解决

### Session 共享问题的解决方法

- session sticky

session sticky(粘性) , 保证同一个会话的请求都在同一个web 服务器上处理，这样的话，就完全不需要考虑到会话的问题了。比如前面说的负载均衡算法中，哈希算法就是
一个典型的实现手段。

这种实现方式会有些问题：

1 如果一台 web 服务器宕机或者重启，那么这台机器上保存的会话数据都会丢失，会造成用户暂时无法访问的问题，或者用户之前的授权操作需要再执行一次

2 通过这种方式实现的 session 保持，没有办法进行 4 层网络转发，只能在 7 层网络上进行解析并转发

- session replication

session 复制，通过相关技术实现 session 复制，使得集群中的各个服务器相互保存各自节点存储的 session 数据。tomcat 本身就可以实现 session 复制的功能，基于 IP 组播
放方式。大家课后可以去了解下如何配置这种实现方式的问题：

1 同步 session 数据会造成网络开销，随着集群规模越大，同步 session 带来的带宽影响也越大

2 每个节点需要保存集群中所有节点的 session 数据，就需要比较大的内存来存储。

- session 统一存储

集群中的各个节点的 session 数据，统一存储到一个存储设备中。那么每个节点去拿 session 的时候，就不是从自己的内存中去获得，而是从相应的第三方存储中去拿。对于这个方案来说，无论是哪个节点新增或者修改了 session 数据，最终都会发生在这个集中存储的地方。

这个存储设备可以是 redis、也可以是 mysql。

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/session5007.png)

这种实现方式的问题：

1 读写 session 数据需要进行网络操作，存在不稳定性和延迟性

2 如果存储 session 的服务器出现故障，将大规模的影响到应用

- Cookie Based

Cookie Based 方法，简单来说，就是不依赖容器本身的Session 机制。

而是服务端基于一定的算法，生成一个 token 给到客户端，客户端每次请求，都会携带这个 token。当服务端收到 token 以后，先验证 token 是否有效，再解密这个 token 获取关键数据进行处理

### Cookie Based 实现方式

基于纯 Cookie 的方式，也就是客户端每 次请求都携带身份信息给到服务端。

比较典型的方式是 JWT，全称是 JSON Web Tokens。是一种简洁的并且在两个计算机之间安全传递信息的表述性声明规范。JWT 的声明一般被用来在客户端和服务端之间传递被认证的用户身份信息，以便于从资源服务器获取资源。比如用在用户登录上。

这个和最原始的 cookie+session 方式是不一样的，其实不是，JWT 强调的是服务端不对 token进行存储，而是直接通过签名算法验证并解密 token 得到相应数据进行处理

![](https://raw.githubusercontent.com/qiuyadongsite/qiuyadongsite.github.io/master/_posts/images/jwt001.png)

- JWT token 的组成

jwt token 由三个部分组成，头部（header）、有效载荷（playload）、签名（signature），打开网站：https://jwt.io/

  - header

格式如下，typ 和 alg 分别对应的全称是 type(类 型)和algorithm(算法)，类型可以自定义。alg:hs256 表示当前 token是使用 HS256 算法来进行加密的。

```JSON

{
"typ": "JWT",
"alg": "HS256"
}

```

  - playload

Payload 里面是 Token 的具体内容，也是一个 json 字符串，这些内容里面有一些是标准字段，你也可以添加其它需要的内容；

payload 的 json 结构并不像 header 那么简单，payload 用来承载要传递的数据，它的 json 结构实际上是对 JWT 要传递的数据的一组声明，这些声明被 JWT标准称为 claims , JWT 默认提供了一些标准的 Claim，具体内容如下。

每一个 claim 都有特定的含义和作用，

```

 iss(Issuser)：代表这个 JWT 的签发主体；
 sub(Subject)：代表这个 JWT 的主体，即它的所有人；
 aud(Audience)：代表这个 JWT 的接收对象；
 exp(Expiration time)：是一个时间戳，代表这个 JWT 的过期时间；
 nbf(Not Before)：是一个时间戳，代表这个 JWT 生效的开始时间，意味着在这个时间之前验证 JWT 是会失败的；
 iat(Issued at)：是一个时间戳，代表这个 JWT 的签发时间；
 jti(JWT ID)：是 JWT 的唯一标识。

```
按照 JWT 标准的说明：标准的 claims 都是可选的，在生成playload 不强制用上面的那些 claim，你可以完全按照自己的想法来定义 payload 的结构，不过这样搞根本没必要：

第一是，如果把 JWT 用于认证， 那么 JWT 标准内规定的几个claim就足够用了，甚至只需要其中一两个就可以了，假如想往 JWT 里多存一些用户业务信息，比如角色和用户名等，这倒是用自定义的 claim 来添加；

第二是，JWT 标准里面针对它自己规定的 claim 都提供了有详细的验证规则描述，每个实现库都会参照这个描述来提供 JWT 的验证实现，所以如果是自定义的 claim 名称，那么你用到的实现库就不会主动去验证这些 claim。

- signature

创建签名需要使用编码后的 header 和 payload 以及一个密钥，使用 header 中指定签名算法进行签名。组成格式如下

```

 header (base64 后的)
 payload (base64 后的)
 secret

```

这个部分需要 base64 加密后的 header 和 base64 加密后的 payload 使用.连接组成的字符串，然后通过 header 中声明的加密方式进行加盐 secret 组合加密，然后就构成了
jwt 的第三部分。

signature=HS256(base64(header)+”.”+base64(payload),secret);

最后将这 3 个部分组成一个完整的字符串构成了 JWT：

base64(header)+”.”+base64(payload)+”.”+sinature

secret 是保存在服务器端的，jwt 的签发生成也是在服务器端的，secret 就是用来进行 jwt 的签发和 jwt 的验证，所以，它就是你服务端的私钥，在任何场景都不应该流露出去。一旦客户端得知这个 secret, 那就意味着客户端是可以自我签发 jwt 了

- 总结

jwt 实际上就是定义了一套数据加密以及验签的算法的规范，根据这个规范来实现单点登录，以及数据传输及验签功能。但是这个方案会存在一些问题比如：不能传递敏感信息，因为 jwt 中的部分内容可以解密，只是不能修改而已。

## 代码实现

## 登录

登录的时候，根据用户名+密码+...生产token

```java

@RestController
public class UserController extends BaseController{
  @PostMapping("/login")
   public ResponseData doLogin(String username, String password,
                                    HttpServletResponse response){

      ResponseData data=new ResponseData();
      UserLoginRequest request=new UserLoginRequest();
      request.setPassword(password);
      request.setUserName(username);
      UserLoginResponse userLoginResponse=userCoreService.login(request);
      response.addHeader("Set-Cookie",  "access_token="+userLoginResponse.getToken()+";Path=/;HttpOnly");//在cookie中设置token

      data.setMessage(userLoginResponse.getMsg());
      data.setCode(userLoginResponse.getCode());
      data.setData(GpmallWebConstant.GPMALL_ACTIVITY_ACCESS_URL);
      return data;
   }
}

public UserLoginResponse login(UserLoginRequest request) {
       Log.info("login request:"+request);
       UserLoginResponse response=new UserLoginResponse();
       try {
           beforeValidate(request);
           User user=userMapper.getUserByUserName(request.getUserName());
           if(user==null||!user.getPassword().equals(request.getPassword())){
               response.setCode(ResponseCodeEnum.USERORPASSWORD_ERRROR.getCode());
               response.setMsg(ResponseCodeEnum.USERORPASSWORD_ERRROR.getMsg());
               return response;
           }
           Map<String,Object> map=new HashMap<>();
           map.put("uid",user.getId());
           map.put("exp",DateTime.now().plusDays(1).toDate().getTime()/1000);

           response.setToken(JwtTokenUtils.generatorToken(map));//根据map生产token

           response.setUid(user.getId());
           response.setAvatar(user.getAvatar());
           response.setCode(ResponseCodeEnum.SUCCESS.getCode());
           response.setMsg(ResponseCodeEnum.SUCCESS.getMsg());
       }catch (Exception e){
           Log.error("login occur exception :"+e);
           ServiceException serviceException=(ServiceException) ExceptionUtil.handlerException4biz(e);
           response.setCode(serviceException.getErrorCode());
           response.setMsg(serviceException.getErrorMessage());
       }finally {
           Log.info("login response->"+response);
       }

       return response;
   }


```

## 访问其他页面

设置公共方法校验token

```java

public CheckAuthResponse validToken(CheckAuthRequest request) {
        CheckAuthResponse response=new CheckAuthResponse();
        try{
            beforeValidateAuth(request);

            Claims claims=JwtTokenUtils.phaseToken(request.getToken());//校验token，校验通过就不需要登录，校验失败再重新登录
            response.setUid(claims.get("uid").toString());
            response.setCode(ResponseCodeEnum.SUCCESS.getCode());
            response.setMsg(ResponseCodeEnum.SUCCESS.getMsg());

        }catch (ExpiredJwtException e){
            Log.error("Expire :"+e);
            response.setCode(ResponseCodeEnum.TOKEN_EXPIRE.getCode());
            response.setMsg(ResponseCodeEnum.TOKEN_EXPIRE.getMsg());
        }catch (SignatureException e1){
            Log.error("SignatureException :"+e1);
            response.setCode(ResponseCodeEnum.SIGNATURE_ERROR.getCode());
            response.setMsg(ResponseCodeEnum.SIGNATURE_ERROR.getMsg());
        }catch (Exception e){
            Log.error("login occur exception :"+e);
            ServiceException serviceException=(ServiceException) ExceptionUtil.handlerException4biz(e);
            response.setCode(serviceException.getErrorCode());
            response.setMsg(serviceException.getErrorMessage());
        }finally {
            Log.info("response:"+response);
        }

        return response;
    }



    public class CookieUtil {
    //获取request中的cookie,再从cookie中可以获取token
    	public static String getCookieValue(HttpServletRequest request, String key) {
    		Cookie[] cookies = request.getCookies();
    		if (ArrayUtils.isNotEmpty(cookies)) {
    			for (Cookie cookie : cookies) {
    				if (StringUtils.equals(cookie.getName(), key)) {
    					return cookie.getValue();
    				}
    			}
    		}
    		return null;
    	}
    }
```
