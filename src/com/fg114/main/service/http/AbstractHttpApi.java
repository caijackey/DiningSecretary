package com.fg114.main.service.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;
import android.os.Build;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.fg114.main.app.Fg114Application;
import com.fg114.main.app.Settings;
import com.fg114.main.app.data.CityInfo;
import com.fg114.main.app.location.Loc;
import com.fg114.main.app.location.LocInfo;
import com.fg114.main.service.dto.JsonPack;
import com.fg114.main.service.dto.SoftwareCommonData;
import com.fg114.main.service.dto.UserInfoDTO;
import com.fg114.main.util.ActivityUtil;
import com.fg114.main.util.CalendarUtil;
import com.fg114.main.util.CipherUtils;
import com.fg114.main.util.ContextUtil;
import com.fg114.main.util.ConvertUtil;
import com.fg114.main.util.FileIOUtil;
import com.fg114.main.util.IOUtils;
import com.fg114.main.util.JsonUtils;
import com.fg114.main.util.LogUtils;
import com.fg114.main.util.SessionManager;

/**
 * 
 *
 */
public abstract class AbstractHttpApi implements HttpApi {
	
	private static final String TAG = "AbstractHttpApi";
	private static final boolean DEBUG = false;
	
    private static int TIMEOUT = 15;
    
    private final DefaultHttpClient mHttpClient;
    
    public AbstractHttpApi(DefaultHttpClient httpClient) {
        mHttpClient = httpClient;
    }
    
    /**
     * 更新httpClient的超时时间，秒数
     */
    public void updateHttpClientTimeout(int timeout){
    	updateHttpClientTimeout(mHttpClient, timeout);
    }
    
    /**
     * 更新httpClient的超时时间，秒数
     */
    public static void updateHttpClientTimeout(DefaultHttpClient client, int timeout){
    	if(timeout<15){
    		timeout=15;
    	}
    	TIMEOUT = timeout;
    	if(client!=null){
    		client.getParams().setIntParameter(HttpConnectionParams.CONNECTION_TIMEOUT, timeout*1000);
    		client.getParams().setIntParameter(HttpConnectionParams.SO_TIMEOUT, timeout*1000);
    	}
    }
    
    /**
     * 建立一个线程安全的client。
     *
     * @return HttpClient
     */
    public static final DefaultHttpClient createHttpClient() {
        return createHttpClient(TIMEOUT);
    }
    //具有不同的超时时间
    public static final DefaultHttpClient createHttpClientForUpload() {
    	int timeout=SessionManager.getInstance().getUploadRequestTimeout();
    	return createHttpClient(timeout);
    }
    public static final DefaultHttpClient createHttpClient(int timeout) {
    	// Sets up the http part of the service.
    	final SchemeRegistry supportedSchemes = new SchemeRegistry();
    	// Register the "http" protocol scheme, it is required
    	// by the default operator to look up socket factories.
    	final SocketFactory sf = PlainSocketFactory.getSocketFactory();
    	supportedSchemes.register(new Scheme("http", sf, 80));
    	
    	// Set some client http client parameter defaults.
    	final HttpParams httpParams = createHttpParams(timeout);
    	HttpClientParams.setRedirecting(httpParams, false);
    	
    	final ClientConnectionManager ccm = new ThreadSafeClientConnManager(httpParams,
    			supportedSchemes);
    	
    	return new DefaultHttpClient(ccm, httpParams);
    }
    
	/**
	 * 停止Client连接管理器
	 */
	public static final void shutdownHttpClient(DefaultHttpClient client) {
		if (client != null && client.getConnectionManager() != null) {
			client.getConnectionManager().shutdown();
		}
	}

    
    /**
     * 创建默认设置的http协议参数
     */
    public static final HttpParams createHttpParams(int timeout) {
    	final HttpParams params = new BasicHttpParams();
    	
    	// Turn off stale checking. Our connections break all the time anyway,
    	// and it's not worth it to pay the penalty of checking every time.
    	HttpConnectionParams.setStaleCheckingEnabled(params, false);
    	
    	HttpConnectionParams.setConnectionTimeout(params, timeout * 1000);
    	HttpConnectionParams.setSoTimeout(params, timeout * 1000);
    	HttpConnectionParams.setSocketBufferSize(params, 8192);
    	
    	return params;
    }
    
    /**
     * 执行请求
     * @param httpRequest
     * @param clazz
     * @return
     * @throws Exception
     */
    public JsonPack executeHttpRequestWithJson(HttpRequestBase httpRequest) throws Exception {
    	return executeHttpRequestWithJson(mHttpClient, httpRequest);
    }
    //添加固定header信息
    private void addCommonHeader(HttpRequestBase httpRequest) throws Exception{
    	Context context=ContextUtil.getContext();
    	//将 设备号+时间戳 放入请求头
    	String content = Settings.DEV_ID + "," + String.valueOf(System.currentTimeMillis());
    	String enc = CipherUtils.encodeXms(content);
    	httpRequest.addHeader(Settings.REST_EC_NAME, enc);
    	httpRequest.addHeader("Accept-Encoding", "gzip");
    	boolean haveGpsTag = Loc.isGpsAvailable();
		double longitude = 0;
		double latitude = 0;
		if (haveGpsTag) {
			LocInfo myLoc = Loc.getLocImmediately();
			if (myLoc == null || myLoc.getLoc() == null) {
				haveGpsTag = false;
			} else {
				longitude = myLoc.getLoc().getLongitude();
				latitude = myLoc.getLoc().getLatitude();
			}
		}
		
		//{AppName}/{AppVersion}({Platform}; {MobileDeviceManufacturer/MobileDeviceModel};{MobileDeviceId}; {CityId};{GPS/Adjust/longitude/latitude};{channel};{timeDiff};)
		String appAgent="xms/"+Settings.VERSION_NAME;
    	appAgent+="(";
    	appAgent+=Build.VERSION.RELEASE+";"; //Platform
    	appAgent+="android/"; //MobileDeviceManufacturer
    	appAgent+=URLEncoder.encode(ActivityUtil.getDeviceType(),"utf-8")+";"; //MobileDeviceModel
    	appAgent+=ActivityUtil.getDeviceId(context)+";"; //MobileDeviceId
    	appAgent+=SessionManager.getInstance().getCityInfo(ContextUtil.getContext()).getId()+";"; //CityId
    	appAgent+=ActivityUtil.getWifiMAC(context)+";"; //macAddress
    	appAgent+=(haveGpsTag?1:0)+"/"; //have GPS
    	appAgent+="0/"; //Adjust
    	appAgent+=String.valueOf(longitude)+"/"; //longitude
    	appAgent+=String.valueOf(latitude)+";"; //latitude
    	appAgent+=Settings.SELL_CHANNEL_NUM+";"; //channel
    	appAgent+=Settings.TIME_DIFF+";"; //timeDiff
    	//是否是WIFI 1： true  0：false
    	if(ActivityUtil.isWifi(ContextUtil.getContext())){
    		appAgent+=1+";)";
    	}else{
    		appAgent+=0+";)";
    	}
//    	Log.v("TAG", ActivityUtil.isWifi(ContextUtil.getContext())+"=iswifi");
    	httpRequest.addHeader("app-agent", appAgent);
    	
    	httpRequest.addHeader("token", SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getToken());
    	//头部信息日志
    	if (ActivityUtil.isTestDev(ContextUtil.getContext())) {
    		Settings.requestLog.insert(0,"\n[app-agent]= "+appAgent+"\n");
    	}
    	
    }
    public JsonPack executeHttpRequestWithJson(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception {

    	addCommonHeader(httpRequest);
    	String responseString = "";
		try {
			// 获得返回的流数据
			InputStream is = executeHttpRequestSuccess(client, httpRequest);
			responseString = ConvertUtil.convertStreamToString(is);
			logRequestAndResponse(httpRequest.getURI().toString(), responseString);
			
		} catch (Exception e) {
			StringWriter sout=new StringWriter();
			PrintWriter out=new PrintWriter(sout);
			e.printStackTrace(out);
			logRequestAndResponse(httpRequest.getURI().toString(), sout.toString());
			throw e;
		}
       //Log.i(TAG, responseString);
        //设置返回值
        JsonPack jp = new JsonPack();
        if (responseString != null && !"".equals(responseString)) {
	        JSONObject jsonResponse = new JSONObject(responseString);
	        
	        jp.setRe(jsonResponse.getInt("code"));
	        jp.setUrl(httpRequest.getURI().toString());
	        jp.setNeedUpdateUserInfoTag(jsonResponse.getBoolean("needUpdateUserInfoTag"));
	        if(jsonResponse.has("userInfo") && !jsonResponse.isNull("userInfo")){
	        	UserInfoDTO user=JsonUtils.fromJson(jsonResponse.getJSONObject("userInfo").toString(), UserInfoDTO.class);
	        	jp.setUserInfo(user);
	        }
	        //----
	        if (jp.getRe() == 500) {
	        	jp.setMsg("网络查询出现错误");
				Exception ex = new Exception("Exception 500 from server");
				String msg = "Exception 500 from server ";
				if (httpRequest != null && httpRequest.getURI() != null) {
					msg += httpRequest.getURI().toString();
				}
				ActivityUtil.saveException(ex, msg);
			} else {
				jp.setMsg(jsonResponse.getString("message"));
			}
	        //---
        	if(jsonResponse.has("value")){
        		Object obj = jsonResponse.get("value");
        		if (obj instanceof JSONObject) {
        			JSONObject successResultObject = jsonResponse.getJSONObject("value");  
        			jp.setObj(successResultObject);
        		}
        	}
        }
        return jp;
    }
    //测试机能在内存中记录，最近的一些请求和响应，方便调试，（测试机按 android的“菜单键”可查看）
    private void logRequestAndResponse(String request,String response){
    	
    	if (ActivityUtil.isTestDev(ContextUtil.getContext())) {
			// 记录日志
			if (Settings.requestLog.length() > 1024 * 500) {
				Settings.requestLog.delete(0, Settings.requestLog.length());
			}

			Settings.requestLog.insert(0, "\n========>" + CalendarUtil.getDateTimeString() + "<========\n" + request + "\n"
					+ "************************************\n" + response + "\n");
			
			if (!ActivityUtil.isOnForeground(Fg114Application.getInstance().getApplicationContext())) {
				IOUtils.writeTestInfo(ContextUtil.getContext(), "log_request.txt",  "\n========>" + CalendarUtil.getDateTimeString() + "<========\n" + request + "\n");
			}
		}
    }
    public String executeHttpRequestWithString(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception {
    	
    	addCommonHeader(httpRequest);
    	
    	//获得返回的流数据
    	InputStream is = executeHttpRequestSuccess(client, httpRequest);
    	String responseString =  ConvertUtil.convertStreamToString(is);
    	return responseString;
    }
    

//    /**
//     * 请求成功判断
//     * @param httpRequest
//     * @return
//     * @throws Exception
//     */
//    public InputStream executeHttpRequestSuccess(HttpRequestBase httpRequest) throws Exception{
//    	return executeHttpRequestSuccess(mHttpClient, httpRequest);
//    }
    
    /**
     * 请求成功判断
     * @param httpRequest
     * @return
     * @throws Exception
     */
    private static long in=0;
    private static long out=0;
    public InputStream executeHttpRequestSuccess(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception{
    	 HttpResponse response = executeHttpRequest(client, httpRequest);
    	 int statusCode = response.getStatusLine().getStatusCode();
    	 InputStream ret=null;
         switch (statusCode) {
			case 200:
				boolean isGzip = false;
				Header ceheader = response.getEntity().getContentEncoding();
				if (ceheader != null) {
					HeaderElement[] codecs = ceheader.getElements();
					for (int i = 0; i < codecs.length; i++) {
						if (codecs[i].getName().equalsIgnoreCase("gzip")) {
							isGzip = true;
							break;
						}
					}
				}
				//long nowin=response.getEntity().getContentLength();
				if (isGzip) {
					ret= new GZIPInputStream(response.getEntity().getContent());
				} else {
					ret= response.getEntity().getContent();
				}
				
//				long nowout=0;
//				if(httpRequest instanceof HttpPost){
//					nowout=((HttpPost)httpRequest).getEntity().getContentLength();
//				}else{
//					nowout=1000;
//				}
//				in+=nowin;
//				out+=nowout;
				//Log.d("流量统计："+httpRequest.getURI().getPath(),"请求("+nowout+"/"+out+")"+"--->响应("+nowin+"/"+in+")");
				return ret;
	
	         default:
	             response.getEntity().consumeContent();
	             throw new Exception(response.getStatusLine().toString()+" |request=:"+httpRequest.getURI().toString());
         }
    }
 
    /**
     * execute() an httpRequest catching exceptions and returning null instead.
     *
     * @param httpRequest
     * @return
     * @throws Exception 
     * @throws IOException
     */
    public HttpResponse executeHttpRequest(HttpRequestBase httpRequest) throws Exception {
    	return executeHttpRequest(mHttpClient, httpRequest);
    }
    
    /**
     * execute() an httpRequest catching exceptions and returning null instead.
     *
     * @param httpRequest
     * @return
     * @throws IOException
     */
    public HttpResponse executeHttpRequest(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception {
        try {
//        	Log.e("executeHttpRequest", httpRequest.getURI().toString());
        	client.getConnectionManager().closeExpiredConnections();
            return client.execute(httpRequest);
        } catch (Exception e) {
            httpRequest.abort();
            throw e;
        }
    }
    
    /**
     * 建立GET请求
     */
    public HttpGet createHttpGet(String url, NameValuePair... nameValuePairs) {
        String query = URLEncodedUtils.format(stripNulls(nameValuePairs), HTTP.UTF_8);
        if (DEBUG) Log.d(TAG, query);
        HttpGet httpGet = new HttpGet(url + "?" + query);
        httpGet.setHeader("Connection", "close");
        httpGet.setHeader("accept", "application/json");
        httpGet.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
        return httpGet;
    }

    /**
     * 建立POST请求
     */
    public HttpPost createHttpPost(String url, boolean isSuper57, NameValuePair... nameValuePairs) {
    	String query = URLEncodedUtils.format(stripNulls(nameValuePairs), HTTP.UTF_8);
        HttpPost httpPost = new HttpPost(url + "?" + query);
        try {
	        if (isSuper57) {
	        	//超级小秘书的请求的场合
	        	if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
	        	httpPost.setHeader("Connection", "close");
	        	httpPost.setEntity(new UrlEncodedFormEntity(stripNulls(nameValuePairs), HTTP.UTF_8));
	        } else {
	        	//一般的场合
	        	httpPost.setHeader("accept", "application/json");
	            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
	        	StringEntity se = new StringEntity(generateJsonRequest(nameValuePairs), HTTP.UTF_8);
	        	if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
	      	  	se.setContentType("application/json; charset=utf-8");
	            httpPost.setEntity(se);
	        }
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
    
    /**
     * 建立POST请求
     */
    public HttpPost createHttpPost(String url, NameValuePair... nameValuePairs) {
    	String query = URLEncodedUtils.format(stripNulls(nameValuePairs), HTTP.UTF_8);
    	HttpPost httpPost = new HttpPost(url + "?" + query);
//        HttpPost httpPost = new HttpPost(url);
        try {
        	//一般的场合
        	httpPost.setHeader("accept", "application/json");
        	httpPost.setHeader("Connection", "close");
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
        	StringEntity se = new StringEntity(generateRequest(nameValuePairs), HTTP.UTF_8);
        	if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
      	  	se.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(se);
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
    /**
     * 建立POST请求，重载方法目的是，传输大String（不能放在url里传输）
     */
    public HttpPost createHttpPost(NameValuePair largeString, String url,NameValuePair... nameValuePairs) {
    	String query = URLEncodedUtils.format(stripNulls(nameValuePairs), HTTP.UTF_8);
    	HttpPost httpPost = new HttpPost(url + "?" + query);
//        HttpPost httpPost = new HttpPost(url);
    	try {
    		//一般的场合
    		httpPost.setHeader("accept", "application/json");
    		httpPost.setHeader("Connection", "close");
    		httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
    		StringEntity se = new StringEntity(generateRequest(largeString,nameValuePairs), HTTP.UTF_8);
    		if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
    		se.setContentType("application/json; charset=utf-8");
    		httpPost.setEntity(se);
    	} catch (UnsupportedEncodingException e1) {
    		throw new IllegalArgumentException("Unable to encode http parameters.");
    	}
    	return httpPost;
    }
    
    /**
     * 建立上传文件POST请求
     */
    public HttpPost createHttpPost(String url,InputStream stream, NameValuePair... nameValuePairs) {
    	String query = URLEncodedUtils.format(stripNulls(nameValuePairs), HTTP.UTF_8);
    	if (DEBUG) Log.d(TAG, query);
    	HttpPost httpPost = new HttpPost(url + "?" + query);
        try {
//        	httpPost.setHeader("accept", "application/json");
//            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
        	httpPost.setHeader("Connection", "close");
        	//httpPost.setHeader("Content-Length", ""+stream.available());
        	httpPost.setHeader("Content-Type", "image/*");
            InputStreamEntity inEntity = new InputStreamEntity(stream, stream.available());
        	if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
            httpPost.setEntity(inEntity);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        } catch (IOException e) {
        	throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
    
    /**
     * 建立POST请求
     */
    public HttpPost createHttpPostWithoutParams(String url, NameValuePair... nameValuePairs) {
        HttpPost httpPost = new HttpPost(url);
        try {
        	//一般的场合
        	httpPost.setHeader("accept", "application/json");
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
            httpPost.setHeader("Connection", "close");
        	StringEntity se = new StringEntity(generateJsonRequest(nameValuePairs), HTTP.UTF_8);
        	if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
      	  	se.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(se);
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
    
    
    /**
     * 建立POST请求
     */
    public HttpPost createHttpPostWithoutParams2(String url, NameValuePair... nameValuePairs) {
    	String query = URLEncodedUtils.format(stripNulls(nameValuePairs), HTTP.UTF_8);
    	HttpPost httpPost = new HttpPost(url + "?" );
//        HttpPost httpPost = new HttpPost(url);
        try {
        	//一般的场合
        	httpPost.setHeader("accept", "application/json");
        	httpPost.setHeader("Connection", "close");
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
        	StringEntity se = new StringEntity(generateRequest(nameValuePairs), HTTP.UTF_8);
        	if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
      	  	se.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(se);
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
    
    
    /**
     * 建立POST请求
     */
    public HttpPost createHttpPostGoogle(String url, NameValuePair... nameValuePairs) {
        HttpPost httpPost = new HttpPost(url);
        try {
        	//一般的场合
        	httpPost.setHeader("accept", "application/json");
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json;charset=UTF-8");
            httpPost.setHeader("Connection", "close");
        	StringEntity se = new StringEntity(generateGoogleRequest(nameValuePairs), HTTP.UTF_8);
        	if (DEBUG) Log.d(TAG, generateJsonRequest(nameValuePairs));
      	  	se.setContentType("application/json; charset=utf-8");
            httpPost.setEntity(se);
        } catch (UnsupportedEncodingException e1) {
            throw new IllegalArgumentException("Unable to encode http parameters.");
        }
        return httpPost;
    }
    
    public static List<NameValuePair> stripNulls(NameValuePair... nameValuePairs) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        for (int i = 0; i < nameValuePairs.length; i++) {
            NameValuePair param = nameValuePairs[i];
            if (param.getValue() != null) {
                params.add(param);
            }
        }
        addBaseParams(params);
        return params;
    }
    
    /**
     * 生成Json形式参数
     * @param nameValuePairs
     * @return
     */
    private String generateJsonRequest(NameValuePair... nameValuePairs) {
    	List<NameValuePair> params = stripNulls(nameValuePairs);
    	JSONStringer jStringer = new JSONStringer();
    	try {
	    	jStringer.object();
	        for (NameValuePair param : params) {
	            if (param.getValue() != null) {
	            	Object value;
	            	try {
	            		value = new JSONArray(param.getValue());
	            	} catch (JSONException e) {
	            		try {
	            			value = new JSONObject(param.getValue());
	            		} catch (JSONException ex) {
	     	            	value = param.getValue();
	            		}
	            	}
	            	jStringer.key(param.getName()).value(value);
	            }
	        }
	        jStringer.endObject();
    	} catch (JSONException e) {
    		return null;
    	}
        return jStringer.toString();
    }
    
    /**
     * 生成Json形式参数
     * @param nameValuePairs
     * @return
     */
    private String generateRequest(NameValuePair... nameValuePairs) {
    	List<NameValuePair> params = stripNulls(nameValuePairs);
    	JSONStringer jStringer = new JSONStringer();
    	try {
	    	jStringer.object();
	        for (NameValuePair param : params) {
	            if (param.getValue() != null) {
	            	jStringer.key(param.getName()).value(param.getValue());
	            }
	        }
	        jStringer.endObject();
    	} catch (JSONException e) {
    		return null;
    	}
        return jStringer.toString();
    }
    /**
     * 生成Json形式参数
     * @param nameValuePairs
     * @return
     */
    private String generateRequest(NameValuePair largeString, NameValuePair... nameValuePairs) {
    	List<NameValuePair> params = stripNulls(nameValuePairs);
    	params.add(largeString);
    	JSONStringer jStringer = new JSONStringer();
    	try {
    		jStringer.object();
    		for (NameValuePair param : params) {
    			if (param.getValue() != null) {
    				jStringer.key(param.getName()).value(param.getValue());
    			}
    		}
    		jStringer.key(largeString.getName()).value(largeString.getValue());
    		jStringer.endObject();
    	} catch (JSONException e) {
    		return null;
    	}
    	return jStringer.toString();
    }
    
    /**
     * 生成Json形式参数
     * @param nameValuePairs
     * @return
     */
    private String generateGoogleRequest(NameValuePair... nameValuePairs) {
    	JSONStringer jStringer = new JSONStringer();
    	try {
	    	jStringer.object();
	        for (NameValuePair param : nameValuePairs) {
	            if (param.getValue() != null) {
	            	Object value;
	            	try {
	            		value = new JSONArray(param.getValue());
	            	} catch (JSONException e) {
	            		try {
	            			value = new JSONObject(param.getValue());
	            		} catch (JSONException ex) {
	     	            	value = param.getValue();
	            		}
	            	}
	            	jStringer.key(param.getName()).value(value);
	            }
	        }
	        jStringer.endObject();
    	} catch (JSONException e) {
    		return null;
    	}
        return jStringer.toString();
    }
    
    public static void addBaseParams(List<NameValuePair> params) {
        // 增加设备类型参数
    	if (containsKey(params, "deviceType") == -1) {
    		params.add(new BasicNameValuePair("deviceType", Build.MODEL));
    	}
    	
    	// 增加版本号信息
    	if (containsKey(params, "version") == -1) {
    		params.add(new BasicNameValuePair("version", Settings.VERSION_NAME));
    	}
    	// 增加OS版本号信息
    	if (containsKey(params, "deviceOsVersion") == -1) {
    		params.add(new BasicNameValuePair("deviceOsVersion", ""+Build.VERSION.SDK_INT));
    	}
    	
    	// 增加当前页面参数
    	if (containsKey(params, "currentPage") == -1) {
    		params.add(new BasicNameValuePair("currentPage", Settings.CURRENT_PAGE));
    	}
    	
    	// 增加城市id
    	if (containsKey(params, "cityId") == -1) {
    		CityInfo city = SessionManager.getInstance().getCityInfo(ContextUtil.getContext());
    		if (city == null || TextUtils.isEmpty(city.getId())) {
    			params.add(new BasicNameValuePair("cityId", ""));
    		} else {
    			params.add(new BasicNameValuePair("cityId", city.getId()));
    		}
    	}
    	
    	// 增加gps信息
		if (containsKey(params, "haveGpsTag") == -1 && containsKey(params, "longitude") == -1 && containsKey(params, "latitude") == -1) {
			boolean haveGpsTag = Loc.isGpsAvailable();
			double longitude = 0;
			double latitude = 0;
			if (haveGpsTag) {
				LocInfo myLoc = Loc.getLocImmediately();
				if (myLoc == null || myLoc.getLoc() == null) {
					haveGpsTag = false;
				} else {
					longitude = myLoc.getLoc().getLongitude();
					latitude = myLoc.getLoc().getLatitude();
				}
			}
			params.add(new BasicNameValuePair("haveGpsTag", String.valueOf(haveGpsTag)));
			params.add(new BasicNameValuePair("longitude", String.valueOf(longitude)));
			params.add(new BasicNameValuePair("latitude", String.valueOf(latitude)));
			params.add(new BasicNameValuePair("needCorrectGpsTag", String.valueOf(false)));
		}
    	
    	// 增加设备号信息
    	int indexOfDeviceNumber = containsKey(params, "deviceNumber");
    	if (indexOfDeviceNumber == -1) {
    		params.add(new BasicNameValuePair("deviceNumber", Settings.DEV_ID));
    	} else {
    		// 保证传递的设备号为全局参数，而不是即时获取的
    		String fakeDeviceNumber = params.get(indexOfDeviceNumber).getValue(); // 一些接口即时获取的设备号
    		if (!Settings.DEV_ID.equals(fakeDeviceNumber)) {
    			// 即时获取的设备号和全局设备号不同时，做特殊处理
    			params.remove(indexOfDeviceNumber);
        		params.add(new BasicNameValuePair("deviceNumber", Settings.DEV_ID));
        		params.add(new BasicNameValuePair("fakeDeviceNumber", fakeDeviceNumber));
    		}
    	}
    	
    	 // 增加渠道号参数
    	if (containsKey(params, "sellChannelNumber") == -1) {
    		params.add(new BasicNameValuePair("sellChannelNumber", Settings.SELL_CHANNEL_NUM));
    	}
    	// 增加用户token
    	if (containsKey(params, "token") == -1) {
    		params.add(new BasicNameValuePair("token", SessionManager.getInstance().getUserInfo(ContextUtil.getContext()).getToken()));
    	}
    }
    
    private static int containsKey(List<NameValuePair> params, String key) {
    	if (params == null || params.size() == 0 || TextUtils.isEmpty(key)) {
    		return -1;
    	}
    	for (int i=0; i<params.size(); i++) {
    		NameValuePair pair = params.get(i);
    		if (key.equals(pair.getName())) {
    			return i;
    		}
    	}
    	return -1;
    }
}
