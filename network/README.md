##结构说明

###api
接口目录，例：

`/api/users/list`

###apiservice
API接口，例：

```
public interface GitHubService {
  @GET("users/{user}/repos")
  Call<List<Repo>> listRepos(@Path("user") String user);
}
```

###interceptor
Retrofit `过滤器`

* HttpLoggingInterceptor：格式化日志输出
* HeaderParamsInterceptor：添加请求Header，有需求自己动手
* DynamicUrlInterceptor：动态更换请求Url，一般项目只会用到一个自己服务器的Url。有时需要请求第三方接口，这时需要更换新的请求Url，简单更此类不需要更改，使用方法见：`NetworkPortal`

###util

* ApiResponse：API响应公共类，包含三个实现类：
	* ApiSuccessResponse ：网络请求成功结果正常
	* ApiEmptyResponse	：网络请求结果为空
	* ApiErrorResponse	：网络请求异常，包含：`网络请求失败`和`网络请求数据异常`

	注：<mark>自定义的code码，需要在此类中添加分发处理</mark>
* BaseResponse：	请求结果数据包装类，无论成功失败，结果都会通过此类或实现类进行数据封装<br>
	此类属性是根据常用数据结构体生成，可以根据自己的需求自定义
	
	```
	{
    	"code":200,
    	"data":{
        	"id":74,
        	"user_id":1001,
        	"nickname":"test测试1"
    	},
    	"message":"成功"
}
	```
	
###NetworkConstant
所有网络可配置参数都在此处：

* 请求配置信息
* Code：根据需要添加
* Message：根据需要修改添加，如需要添加多语言，需要手动调用`initErrorMessage()`方法


###NetworkPortal

发起请求唯一入口

`getService(Class)` `getService(Class,url)` 前者使用默认Url，后者使用url传参发起请求
