package com.fg114.main.service.http;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fg114.main.service.dto.JsonPack;

/**
 * 后台连接接口
 *
 */
public interface HttpApi {
	/**
	 * 执行请求
	 * @param httpRequest
	 * @param clazz
	 * @return 
	 * @throws Exception
	 */
	public JsonPack doHttpRequest(HttpRequestBase httpRequest) throws Exception;
	
	/**
	 * 执行请求
	 * @param httpRequest
	 * @param clazz
	 * @return 
	 * @throws Exception
	 */
	public JsonPack doHttpRequest(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception;
	
	/**
	 * @param client
	 * @param httpRequest
	 * @return 返回的是非jsonpack的原始响应字符串
	 * @throws Exception
	 */
	public String doHttpRequestReturnString(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception;

	/**
	 * 建立GET请求
	 * @param url
	 * @param nameValuePairs
	 * @return
	 */
	public HttpGet createHttpGet(String url, NameValuePair... nameValuePairs);
	/**
	 * 建立POST请求
	 * @param largeString
	 * @param url
	 * @param nameValuePairs
	 * @return
	 */
	public HttpPost createHttpPost(NameValuePair largeString, String url, NameValuePair... nameValuePairs);

	/**
	 * 建立POST请求
	 * @param url
	 * @param isSuper57
	 * @param nameValuePairs
	 * @return
	 */
	public HttpPost createHttpPost(String url, boolean isSuper57, NameValuePair... nameValuePairs);
	

	/**
	 * 建立不要解析json数据的POST请求
	 * @param url
	 * @param isSuper57
	 * @param nameValuePairs
	 * @return
	 */
	public HttpPost createHttpPost(String url, NameValuePair... nameValuePairs);
	
	/**
	 * 建立上传文件POST请求
	 * @param url
	 * @param isSuper57
	 * @param nameValuePairs
	 * @return
	 */
	public HttpPost createHttpPost(String url, InputStream stream, NameValuePair... nameValuePairs);
	
	/**
	 * 执行POST请求
	 * @param httpRequest
	 * @return
	 * @throws IOException 
	 */
	public HttpResponse executeHttpRequest(HttpRequestBase httpRequest) throws Exception;
	
	
	/**
	 * 执行Post请求(不将请求参数包含在url中)
	 * @param url
	 * @param nameValuePairs
	 * @return
	 */
	public HttpPost createHttpPostWithoutParams(String url, NameValuePair... nameValuePairs);
	
	
	/**
	 * 执行Post请求(不将请求参数包含在url中)
	 * @param url
	 * @param nameValuePairs
	 * @return
	 */
	public HttpPost createHttpPostWithoutParams2(String url, NameValuePair... nameValuePairs);
	
	/**
	 * 执行POST请求
	 * @param httpRequest
	 * @return
	 * @throws IOException 
	 */
	public HttpResponse executeHttpRequest(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception;
	
	public HttpPost createHttpPostGoogle(String url, NameValuePair... nameValuePairs);
}
