## 网易视频云点播Server Http API接口文档 

### <font color=#00C5CD>接口概述</font>

### API调用说明

本文档中，所有调用视频云点播服务端接口的请求都需要按此规则校验。

### API token校验

![](http://i.imgur.com/kv3vnEf.png)

以下参数需要放在Http Request Header中

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>AppKey</td><td>String</td><td>是</td><td>开发者平台分配的appKey</td></tr>
<tr><td>CurTime</td><td>String</td><td>是</td><td>当前UTC时间戳，从1970年1月1日0点0分0秒开始到现在的秒数</td></tr>
<tr><td>CheckSum</td><td>String</td><td>是</td><td>服务器认证需要，SHA1(AppSecret+body+CurTime)，16进制字符小写</td></tr>
</tbody>
</table>

<font color=red>重要提示</font>：本文档中提供的所有接口均面向开发者服务器端调用，用于计算token的secretKey开发者应妥善保管，可在应用的服务器端存储和使用，但不应该存储或传递到客户端，也不应该在网页等前端代码中嵌入。

计算CheckSum的java代码举例如下：

<pre><code><font style="font-weight:bold;">import</font> java.security.MessageDigest;

public class <font color=red>CheckSumBuilder</font> {
	<font style="font-weight:bold;">public static</font> String <font color=red>getCheckSum</font>(String appSecret, String body, String curTime) {
		<font style="font-weight:bold;">return</font> encode(<font color=red>"sha1"</font>, appSecret + body + curTime);
	}
	<font style="font-weight:bold;">private static</font> String <font color=red>encode</font>(String algorithm, String value) {
		if (value == <font style="font-weight:bold;">null</font>) {
			<font style="font-weight:bold;">return null</font>;
		}
		try {
			MessageDigest messageDigest = MessageDigest.getInstance(algorithm);
			messageDigest.update(value.getBytes());
			<font style="font-weight:bold;">return</font> getFormattedText(messageDigest.digest());
		} <font style="font-weight:bold;">catch</font> (Exception e) {
			<font style="font-weight:bold;">throw new</font> RuntimeException(e);
		}
	}
	<font style="font-weight:bold;">private static</font> String <font color=red>getFormattedText</font>(<font style="font-weight:bold;">byte</font>[] bytes) {
		<font style="font-weight:bold;">int</font> len = bytes.length;
		StringBuilder buf = <font style="font-weight:bold;">new</font> StringBuilder(len * 2);
		<font style="font-weight:bold;">for</font> (<font style="font-weight:bold;">int</font> j = 0; j < len; j++) {
			buf.append(HEX_DIGITS[(bytes[j] >> <font color=green>4</font>) & <font color=green>0x0f</font>]);
			buf.append(HEX_DIGITS[bytes[j] & <font color=green>0x0f]</font>);
		}
		<font style="font-weight:bold;">return</font> buf.toString();
	}
	<font style="font-weight:bold;">private static final char</font>[] HEX_DIGITS = { <font color=red>'0'</font>, <font color=red>'1'</font>, <font color=red>'2'</font>, <font color=red>'3'</font>, <font color=red>'4'</font>, <font color=red>'5'</font>, <font color=red>'6'</font>, <font color=red>'7'</font>, <font color=red>'8'</font>, <font color=red>'9'</font>, <font color=red>'a'</font>, <font color=red>'b'</font>, <font color=red>'c'</font>, <font color=red>'d'</font>, <font color=red>'e'</font>, <font color=red>'f'</font> };
}</code></pre>

### 接口说明 ###

所有接口都只支持POST请求。

所有接口请求Content-Type类型为：application/json;charset=utf-8。

所有接口返回类型为JSON，同时进行UTF-8编码。

----------

### <font color=#00C5CD>文件上传</font>

### 普通上传初始化

### 接口说明 ###

普通一次直传文件的初始化。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/upload/init</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>type</td><td>int</td><td>是</td><td>上传文件类型，包含有：1（视频文件）、2（PDF文件）、3（字幕文件）</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"type":1}'</font> http://vod.126.net/upload/init
</code></pre>

### HttpClient请求示例（以下个接口的HttpClient调用方式参考此处） ###

<pre></code><font style="font-weight:bold;">import</font> org.apache.http.HttpResponse;
<font style="font-weight:bold;">import</font> org.apache.http.NameValuePair;
<font style="font-weight:bold;">import</font> org.apache.http.client.entity.UrlEncodedFormEntity;
<font style="font-weight:bold;">import</font> org.apache.http.client.methods.HttpPost;
<font style="font-weight:bold;">import</font> org.apache.http.impl.client.DefaultHttpClient;
<font style="font-weight:bold;">import</font> org.apache.http.message.BasicNameValuePair;
<font style="font-weight:bold;">import</font> org.apache.http.util.EntityUtils;

<font style="font-weight:bold;">import</font> java.util.ArrayList;
<font style="font-weight:bold;">import</font> java.util.Date;
<font style="font-weight:bold;">import</font> java.util.List;

<font style="font-weight:bold;">public class</font> <font color=red>Test</font> {
	<font style="font-weight:bold;">public static void</font> <font color=red>main</font>(String[] args) <font style="font-weight:bold;">throws</font> Exception{
		DefaultHttpClient httpClient = <font style="font-weight:bold;">new</font> DefaultHttpClient();
		String url = <font color=red>"http://vcloud.126.net/vod/upload/init"</font>;
		HttpPost httpPost = <font style="font-weight:bold;">new</font> HttpPost(url);

		String appKey = <font color=red>"94kid09c9ig9k1loimjg012345123456"</font>;
		String appSecret = <font color=red>"123456789012"</font>;
		String body =  <font color=red>"{\"type\":1}"</font>;
		String curTime = String.valueOf((new Date()).getTime() / <font color=green>1000L</font>);
		String checkSum = CheckSumBuilder.getCheckSum(appSecret, body ,curTime);<font color=grey>//参考 计算CheckSum的java代码</font>

		<font color=grey>// 设置请求的header</font>
		httpPost.addHeader(<font color=red>"AppKey"</font>, appKey);
		httpPost.addHeader(<font color=red>"CurTime"</font>, curTime);
		httpPost.addHeader(<font color=red>"CheckSum"</font>, checkSum);
		httpPost.addHeader(<font color=red>"Content-Type"</font>, <font color=red>"application/json;charset=utf-8"</font>);

		<font color=grey>// 设置请求的参数</font>
		StringEntity params = <font style="font-weight:bold;">new</font> StringEntity(<font color=red>"{\"type\":0}"</font>);
		httpPost.setEntity(params);

		<font color=grey>// 执行请求</font>
		HttpResponse response = httpClient.execute(httpPost);

		<font color=grey>// 打印执行结果</font>
		System.out.println(EntityUtils.toString(response.getEntity(), <font color=red>"utf-8"</font>));
	}
}
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>xNosToken</td><td>String</td><td>访问nos的token信息</td></tr>
<tr><td>bucket</td><td>String</td><td>存储上传文件的nos桶名</td></tr>
<tr><td>nosKey</td><td>String</td><td>存储上传文件的object对象名</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"xNosToken" : "XXX",
		"bucket" : "XXX",
		"nosKey" : "XXX"
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

----------

### 断点续传初始化

### 接口说明 ###

断点续传文件的初始化。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/upload/init_multi</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>type</td><td>int</td><td>是</td><td>上传文件类型，包含有：1（视频文件）、2（PDF文件）、3（字幕文件）</td></tr>
<tr><td>fileName</td><td>String</td><td>是</td><td>上传文件的名称</td></tr>
<tr><td>fileSize</td><td>Long</td><td>是</td><td>上传文件的大小（单位：字节）</td></tr>
<tr><td>modified</td><td>Long</td><td>是</td><td>上传文件的最近修改时间</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"type":1,"fileName":"XXX","fileSize":XXX,"modified":XXX}'</font> http://vod.126.net/upload/init_multi
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>xNosToken</td><td>String</td><td>访问nos的token信息</td></tr>
<tr><td>bucket</td><td>String</td><td>存储上传文件的nos桶名</td></tr>
<tr><td>nosKey</td><td>String</td><td>存储上传文件的object对象名</td></tr>
<tr><td>context</td><td>String</td><td>上传文件在nos中的上下文标识</td></tr>
<tr><td>offset</td><td>Long</td><td>上传文件在nos中已经上传的部分相对整个文件的偏移量</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"xNosToken" : "XXX",
		"bucket" : "XXX",
		"nosKey" : "XXX",
		"context" : "XXX",
		"offset" : XXX
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 存放nos的上传文件的上下文 ##

### 接口说明 ###

断点续传需要存放nos返回的上传文件的上下文，从而计算出已上传文件相对整个文件的偏移量。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/context/add</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>fileName</td><td>String</td><td>是</td><td>上传文件的名称</td></tr>
<tr><td>fileSize</td><td>Long</td><td>是</td><td>上传文件的大小（单位：字节）</td></tr>
<tr><td>modified</td><td>Long</td><td>是</td><td>上传文件的最近修改时间</td></tr>
<tr><td>context</td><td>String</td><td>是</td><td>上传文件在nos中的上下文标识</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"fileName":"XXX","fileSize":XXX,"modified":XXX,"context":"XXX"}'</font> http://vod.126.net/context/add
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 视频文件信息添加 ##

### 接口说明 ###

文件上传nos完成后，添加视频文件信息。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/video/add</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>fileName</td><td>String</td><td>是</td><td>视频文件的名称</td></tr>
<tr><td>fileSize</td><td>Long</td><td>是</td><td>视频文件的大小（单位：字节）</td></tr>
<tr><td>bucket</td><td>String</td><td>是</td><td>视频文件上传至nos的桶名</td></tr>
<tr><td>nosKey</td><td>String</td><td>是</td><td>视频文件上传至nos的对象名</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"fileName":"XXX","fileSize":XXX,"bucket":"XXX","nosKey":"XXX"}'</font> http://vod.126.net/video/add
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## PDF文件信息添加 ##

### 接口说明 ###

文件上传nos完成后，添加PDF文件信息。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/pdf/add</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>fileName</td><td>String</td><td>是</td><td>PDF文件的名称</td></tr>
<tr><td>fileSize</td><td>Long</td><td>是</td><td>PDF文件的大小（单位：字节）</td></tr>
<tr><td>bucket</td><td>String</td><td>是</td><td>PDF文件上传至nos的桶名</td></tr>
<tr><td>nosKey</td><td>String</td><td>是</td><td>PDF文件上传至nos的对象名</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"fileName":"XXX","fileSize":XXX,"bucket":"XXX","nosKey":"XXX"}'</font> http://vod.126.net/pdf/add
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 字幕文件信息添加 ##

### 接口说明 ###

文件上传nos完成后，添加字幕文件信息。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/caption/add</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>fileName</td><td>String</td><td>是</td><td>字幕文件的名称</td></tr>
<tr><td>fileSize</td><td>Long</td><td>是</td><td>字幕文件的大小（单位：字节）</td></tr>
<tr><td>vid</td><td>Long</td><td>是</td><td>字幕文件对应视频文件的主ID</td></tr>
<tr><td>language</td><td>String</td><td>是</td><td>字幕文件的语言</td></tr>
<tr><td>bucket</td><td>String</td><td>是</td><td>字幕文件上传至nos的桶名</td></tr>
<tr><td>nosKey</td><td>String</td><td>是</td><td>字幕文件上传至nos的对象名</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"fileName":"XXX","fileSize":XXX,"vid":XXX,"language":"CN","bucket":"XXX","nosKey":"XXX"}'</font> http://vod.126.net/caption/add
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

# 视频文件转码 #

## 视频转码重置 ##

### 接口说明 ###

转码失败后重新进行转码。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/transfer/reset</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>vid</td><td>Long</td><td>是</td><td>视频文件的主ID</td></tr>
<tr><td>weight</td><td>int</td><td>否</td><td>视频文件的转码权重（普通队列权重为1-99，100位加急队列）</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"vid":XXX}'</font> http://vod.126.net/transfer/reset
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 视频转码权重设置 ##

### 接口说明 ###

设置视频的转码权重（转码的优先级）。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/transfer/setweight</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>vid</td><td>Long</td><td>是</td><td>视频文件的主ID</td></tr>
<tr><td>weight</td><td>int</td><td>是</td><td>视频文件的转码权重（普通队列权重为1-99，100位加急队列）</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"vid":XXX,"weight":12}'</font> http://vod.126.net/transfer/setweight
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 转码结果回调URL设置 ##

### 接口说明 ###

转码成功后需要回调的URL设置。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/transfer/setcallback</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>callbackUrl</td><td>String</td><td>是</td><td>视频转码成功后的回调URL</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"callbackUrl":"XXX"}'</font> http://vod.126.net/transfer/setcallbackurl
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

# 视频文件获取 #

## 单个视频文件获取 ##

### 接口说明 ###

获取单个视频文件的详细信息。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/video/get</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>vid</td><td>Long</td><td>是</td><td>视频文件的主ID</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"vid":XXX}'</font> http://vod.126.net/video/get
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
<tr><td>vid</td><td>Long</td><td>视频主ID</td></tr>
<tr><td>videoName</td><td>String</td><td>视频名称</td></tr>
<tr><td>status</td><td>int</td><td>视频的状态（10：正常可播放；20：处理失败；30：正在处理；40：初始化；50：等待转码）</td></tr>
<tr><td>snapshot</td><td>String</td><td>视频截图的URL地址</td></tr>
<tr><td>videoDuration</td><td>int</td><td>视频播放时长（单位：秒）</td></tr>
<tr><td>addTime</td><td>Long</td><td>视频添加时间</td></tr>
<tr><td>completeTime</td><td>Long</td><td>视频转码完毕时间</td></tr>
<tr><td>videoSourceKey</td><td>String</td><td>视频源文件的nosKey</td></tr>
<tr><td>shdMp4Url</td><td>String</td><td>超高清mp4视频地址</td></tr>
<tr><td>hdMp4Url</td><td>String</td><td>高清mp4视频地址</td></tr>
<tr><td>sdMp4Url</td><td>String</td><td>标清mp4视频地址</td></tr>
<tr><td>shdFlvUrl</td><td>String</td><td>超高清flv视频地址</td></tr>
<tr><td>hdFlvUrl</td><td>String</td><td>高清flv视频地址</td></tr>
<tr><td>sdFlvUrl</td><td>String</td><td>标清flv视频地址</td></tr>
<tr><td>shdSecretFlvUrl</td><td>String</td><td>超高清加密flv视频地址</td></tr>
<tr><td>hdSecretFlvUrl</td><td>String</td><td>高清加密flv视频地址</td></tr>
<tr><td>sdSecretFlvUrl</td><td>String</td><td>标清加密flv视频地址</td></tr>
<tr><td>initialSize</td><td>Long</td><td>上传原始视频文件的大小（单位：字节）</td></tr>
<tr><td>shdMp4Size</td><td>Long</td><td>超高清mp4视频文件的大小（单位：字节）</td></tr>
<tr><td>hdMp4Size</td><td>Long</td><td>高清mp4视频文件的大小（单位：字节）</td></tr>
<tr><td>sdMp4Size</td><td>Long</td><td>标清mp4视频文件的大小（单位：字节）</td></tr>
<tr><td>shdFlvSize</td><td>Long</td><td>超高清flv视频文件的大小（单位：字节）</td></tr>
<tr><td>hdFlvSize</td><td>Long</td><td>高清flv视频文件的大小（单位：字节）</td></tr>
<tr><td>sdFlvSize</td><td>Long</td><td>标清flv视频文件的大小（单位：字节）</td></tr>
<tr><td>shdSecretFlvSize</td><td>Long</td><td>超高清加密flv视频文件的大小（单位：字节）</td></tr>
<tr><td>hdSecretFlvSize</td><td>Long</td><td>高清加密flv视频文件的大小（单位：字节）</td></tr>
<tr><td>sdSecretFlvSize</td><td>Long</td><td>标清加密flv视频文件的大小（单位：字节）</td></tr>
<tr><td>language</td><td>String</td><td>视频文件对应的字幕语言</td></tr>
<tr><td>url</td><td>String</td><td>视频文件对应的字幕文件地址</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"vid" : XXX,
		"videoName" : "XXX",
		"status" : 10,
		"snapshot" : "XXX",
		"videoDuration" : XXX,
		"addTime" : XXX,
		"completeTime" : XXX,
		"videoSourceKey" : "XXX", 
		"shdMp4Url" : "XXX",
		"hdMp4Url" : "XXX",
		"sdMp4Url" : "XXX",
		"shdFlvUrl" : "XXX",
		"hdFlvUrl" : "XXX",
		"sdFlvUrl" : "XXX",
		"shdSecretFlvUrl" : "XXX",
		"hdSecretFlvUrl" : "XXX",
		"sdSecretFlvUrl" : "XXX",
		"initialSize" : XXX,
		"shdMp4Size" : XXX,
		"hdMp4Size" : XXX,
		"sdMp4Size" : XXX,
		"shdFlvSize" : XXX,
		"hdFlvSize" : XXX,
		"sdFlvSize" : XXX,
		"shdSecretFlvSize" : XXX,
		"hdSecretFlvSize" : XXX,
		"sdSecretFlvSize" : XXX,
		"captions" : [
			{
			"language" : "XXX",
			"url" : "XXX"
			},
			...
			]
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 多个视频文件获取 ##

### 接口说明 ###

获取多个视频文件的详细信息。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/video/get_multi</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>vids</td><td>List</td><td>是</td><td>多个视频文件的主ID组成的列表</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"vids":[XXX,...,XXX]}'</font> http://vod.126.net/video/get_multi
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td><font style="font-weight:bold;" color=red>响应字段表同获取单个视频文件信息的响应字段表</font></tbody></td></tr>
</tbody></table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"list" : [
			{
			"vid" : XXX,
			"videoName" : "XXX",
			"status" : 10,
			"snapshot" : "XXX",
			"videoDuration" : XXX,
			"addTime" : XXX,
			"completeTime" : XXX,
			"videoSourceKey" : "XXX", 
			"shdMp4Url" : "XXX",
			"hdMp4Url" : "XXX",
			"sdMp4Url" : "XXX",
			"shdFlvUrl" : "XXX",
			"hdFlvUrl" : "XXX",
			"sdFlvUrl" : "XXX",
			"shdSecretFlvUrl" : "XXX",
			"hdSecretFlvUrl" : "XXX",
			"sdSecretFlvUrl" : "XXX",
			"initialSize" : XXX,
			"shdMp4Size" : XXX,
			"hdMp4Size" : XXX,
			"sdMp4Size" : XXX,
			"shdFlvSize" : XXX,
			"hdFlvSize" : XXX,
			"sdFlvSize" : XXX,
			"shdSecretFlvSize" : XXX,
			"hdSecretFlvSize" : XXX,
			"sdSecretFlvSize" : XXX,
			"captions" : [
				{
				"language" : "XXX",
				"url" : "XXX"
				},
				...
				]
			},
			...
		],
		"count" : 12
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 视频文件列表获取 ##

### 接口说明 ###

获取某一个server用户视频文件的详细信息列表并分页显示。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/video/list</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>uid</td><td>Long</td><td>是</td><td>server用户的主ID</td></tr>
<tr><td>index</td><td>int</td><td>是</td><td>获取视频列表分页后索引总数</td></tr>
<tr><td>size</td><td>int</td><td>是</td><td>获取视频列表一页的总条数</td></tr>
<tr><td>status</td><td>int</td><td>否</td><td>根据视频状态过滤选择（10：正常可播放；20：处理失败；30：正在处理；40：初始化；50：等待转码）</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"uid":XXX,"index":3,"size":50}'</font> http://vod.126.net/video/list
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td><font style="font-weight:bold;" color=red>响应字段表同获取单个视频文件信息的响应字段表</font></tbody></td></tr>
</tbody></table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"list" : [
			{
			"vid" : XXX,
			"videoName" : "XXX",
			"status" : 10,
			"snapshot" : "XXX",
			"videoDuration" : XXX,
			"addTime" : XXX,
			"completeTime" : XXX,
			"videoSourceKey" : "XXX", 
			"shdMp4Url" : "XXX",
			"hdMp4Url" : "XXX",
			"sdMp4Url" : "XXX",
			"shdFlvUrl" : "XXX",
			"hdFlvUrl" : "XXX",
			"sdFlvUrl" : "XXX",
			"shdSecretFlvUrl" : "XXX",
			"hdSecretFlvUrl" : "XXX",
			"sdSecretFlvUrl" : "XXX",
			"initialSize" : XXX,
			"shdMp4Size" : XXX,
			"hdMp4Size" : XXX,
			"sdMp4Size" : XXX,
			"shdFlvSize" : XXX,
			"hdFlvSize" : XXX,
			"sdFlvSize" : XXX,
			"shdSecretFlvSize" : XXX,
			"hdSecretFlvSize" : XXX,
			"sdSecretFlvSize" : XXX,
			"captions" : [
				{
				"language" : "XXX",
				"url" : "XXX"
				},
				...
				]
			},
			...
		],
		"count" : 120,
		"index" : 3,
		"size" : 50
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

# PDF文件获取 #

## 单个PDF文件获取 ##

### 接口说明 ###

获取单个PDF文件的详细信息。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/pdf/get</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>pid</td><td>Long</td><td>是</td><td>PDF文件的主ID</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"vid":XXX}'</font> http://vod.126.net/pdf/get
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>说明</td></tr>
<tr><td>code</td><td>int</td><td>错误码</td></tr>
<tr><td>msg</td><td>String</td><td>错误信息</td></tr>
<tr><td>pid</td><td>Long</td><td>PDF文件主ID</td></tr>
<tr><td>pdfName</td><td>String</td><td>PDF文件名称</td></tr>
<tr><td>status</td><td>int</td><td>PDF的状态（10：正常可播放；20：处理失败；30：正在处理；40：初始化；50：等待转码）</td></tr>
<tr><td>size</td><td>Long</td><td>上传原始PDF文件的大小（单位：字节）</td></tr>
<tr><td>addTime</td><td>Long</td><td>PDF添加时间</td></tr>
<tr><td>completeTime</td><td>Long</td><td>PDF转码完毕时间</td></tr>
<tr><td>pdfSourceKey</td><td>String</td><td>PDF源文件的nosKey</td></tr>
<tr><td>numOfPages</td><td>int</td><td>PDF文件的页数</td></tr>
<tr><td>url</td><td>String</td><td>PDF源文件的地址</td></tr>
<tr><td>pagewhRatio</td><td>double</td><td>PDF源文件的宽高比</td></tr>
<tr><td>pageIndex</td><td>int</td><td>PDF源文件的分页序号</td></tr>
<tr><td>swfUrl</td><td>String</td><td>PDF转码成swf文件的分页对应的地址</td></tr>
<tr><td>swfKey</td><td>double</td><td>PDF转码成swf文件的分页对应的nosKey</td></tr>
</tbody>
</table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"pid" : XXX,
		"pdfName" : "XXX",
		"status" : 10,
		"size" : XXX,
		"addTime" : XXX,
		"completeTime" : XXX,
		"pdfSourceKey" : "XXX", 
		"numOfPages" : XXX,
		"url" : "XXX",
		"pagewhRatio" : "XXX",
		"pdfSwfs" : [
			{
			"pageIndex" : XXX,
			"swfUrl" : "XXX",
			"swfKey" : "XXX"
			},
			...
			]
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## 多个PDF文件获取 ##

### 接口说明 ###

获取多个PDF文件的详细信息。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/pdf/get_multi</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>pids</td><td>List</td><td>是</td><td>多个PDF文件的主ID组成的列表</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"pids":[XXX,...,XXX]}'</font> http://vod.126.net/pdf/get_multi
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td><font style="font-weight:bold;" color=red>响应字段表同获取单个PDF文件信息的响应字段表</font></tbody></td></tr>
</tbody></table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"list" : [
			{
			"pid" : XXX,
			"pdfName" : "XXX",
			"status" : 10,
			"size" : XXX,
			"addTime" : XXX,
			"completeTime" : XXX,
			"pdfSourceKey" : "XXX", 
			"numOfPages" : XXX,
			"url" : "XXX",
			"pagewhRatio" : "XXX",
			"pdfSwfs" : [
				{
				"pageIndex" : XXX,
				"swfUrl" : "XXX",
				"swfKey" : "XXX"
				},
				...
			]
			...
		],
		"count" : 12
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

## PDF文件列表获取 ##

### 接口说明 ###

获取某一个server用户PDF文件的详细信息列表并分页显示。

### 请求说明 ###

<pre><code><font style="font-weight:bold;">POST</font> <font color=red>http://vod.126.net/pdf/list</font> <font style="font-weight:bold;">HTTP/1.1</font>
Content-Type: <font color=red>application/json;charset=utf-8</font>
</code></pre>

### 参数说明 ###

<table>
<tbody>
<tr><td>参数</td><td>类型</td><td>必需</td><td>说明</td></tr>
<tr><td>uid</td><td>Long</td><td>是</td><td>server用户的主ID</td></tr>
<tr><td>index</td><td>int</td><td>是</td><td>获取PDF列表分页后索引总数</td></tr>
<tr><td>size</td><td>int</td><td>是</td><td>获取PDF列表一页的总条数</td></tr>
<tr><td>status</td><td>int</td><td>否</td><td>根据PDF文件状态过滤选择（10：正常可播放；20：处理失败；30：正在处理；40：初始化；50：等待转码）</td></tr>
</tbody>
</table>

### curl请求示例 ###

<pre><code>curl -X POST -H <font color=red>"Content-Type:application/json"</font> -H <font color=red>"uid:XXX"</font> -H <font color=red>"AppKey:XXX"</font> -H <font color=red>"CheckSum:XXX"</font> -H <font color=red>"CurTime:XXX"</font> -d 
<font color=red>'{"uid":XXX,"index":3,"size":50}'</font> http://vod.126.net/pdf/list
</code></pre>

### 返回说明 ###

http 响应：json

<table>
<tbody>
<tr><td><font style="font-weight:bold;" color=red>响应字段表同获取单个PDF文件信息的响应字段表</font></tbody></td></tr>
</tbody></table>

<pre><code><font color=red>"Content-Type": "application/json; charset=utf-8"</font>
{
	"code" : XXX,
	"msg" : "XXX",
	"ret" : {
		"list" : [
			{
			"pid" : XXX,
			"pdfName" : "XXX",
			"status" : 10,
			"size" : XXX,
			"addTime" : XXX,
			"completeTime" : XXX,
			"pdfSourceKey" : "XXX", 
			"numOfPages" : XXX,
			"url" : "XXX",
			"pagewhRatio" : "XXX",
			"pdfSwfs" : [
				{
				"pageIndex" : XXX,
				"swfUrl" : "XXX",
				"swfKey" : "XXX"
				},
				...
			]
			...
		],
		"count" : 120,
		"index" : 3,
		"size" : 50
	}
}</code></pre>

<table>
<tbody>
<tr><td><font color=red>code参考code状态表</font></tbody></td></tr>
</tbody></table>

# code状态表 #

<table>
<tbody>
<tr><td>code</td><td>详细描述</td></tr>
<tr><td>200</td><td>操作成功</td></tr>
<tr><td>403</td><td>请求信息不完整</td></tr>
<tr><td>407</td><td>用户不存在</td></tr>
<tr><td>501</td><td>内部错误</td></tr>
<tr><td>607</td><td>用户ID为空</td></tr>
<tr><td>613</td><td>CheckSum为空</td></tr>
<tr><td>614</td><td>APPKey为空</td></tr>
<tr><td>615</td><td>CurTime为空</td></tr>
<tr><td>616</td><td>CheckSum认证失败</td></tr>
<tr><td>702</td><td>用户空间配额已满</td></tr>
</tbody>
</table>