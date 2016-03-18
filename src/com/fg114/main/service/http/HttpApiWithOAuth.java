package com.fg114.main.service.http;

import java.util.logging.Logger;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;

import com.fg114.main.service.dto.JsonPack;

/**
 * HTTP访问
 *
 */
public class HttpApiWithOAuth extends AbstractHttpApi {
	
	protected static final Logger LOG = Logger.getLogger(HttpApiWithOAuth.class.getCanonicalName());
	
    public HttpApiWithOAuth(DefaultHttpClient httpClient) {
        super(httpClient);
    }

    public JsonPack doHttpRequest(HttpRequestBase httpRequest) throws Exception {
        return executeHttpRequestWithJson(httpRequest);
    }
    
	public HttpResponse doHttpPost(HttpPost httpPost) throws Exception {
        return executeHttpRequest(httpPost);
	}

	@Override
	public JsonPack doHttpRequest(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception {
		 return executeHttpRequestWithJson(client, httpRequest);
	}
	@Override
	public String doHttpRequestReturnString(DefaultHttpClient client, HttpRequestBase httpRequest) throws Exception {
		return executeHttpRequestWithString(client, httpRequest);
	}
}
