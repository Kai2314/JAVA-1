package com.pic.util;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.BasicHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.junit.Test;

public class SSLHttpClientTest {
	
	private CloseableHttpClient getSSLHttpClient() throws Exception{
		SSLContext sslContext = SSLContext.getInstance(SSLConnectionSocketFactory.SSL);
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                        System.out.println("getAcceptedIssuers =============");
                        return null;
                }

                public void checkClientTrusted(X509Certificate[] certs,
                                String authType) {
                        System.out.println("checkClientTrusted =============");
                }

                public void checkServerTrusted(X509Certificate[] certs,
                                String authType) {
                        System.out.println("checkServerTrusted =============");
                }
		 } }, new SecureRandom());
		HttpClientBuilder builder = HttpClientBuilder.create();
		SSLConnectionSocketFactory sslConnectionFactory = new SSLConnectionSocketFactory(sslContext, SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
	    builder.setSSLSocketFactory(sslConnectionFactory);
	    Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
	            .register("https", sslConnectionFactory)
	            .build();
	    HttpClientConnectionManager ccm = new BasicHttpClientConnectionManager(registry);
	    builder.setConnectionManager(ccm);
	    return builder.build();
	}
	
	/**
	 * httpclient post json, 使用ssl方式, 
	 * @param path
	 * @param data
	 * @param doGet
	 * @return
	 * @throws Exception
	 */
	private String getSSLReplyContent(String path, String data, boolean doGet) throws Exception {
		CloseableHttpClient client = null ;
		HttpRequestBase method = null ;
		CloseableHttpResponse response = null ;
		String rtnData = "";
		try {			
			client = this.getSSLHttpClient();
			
			URIBuilder builder = new URIBuilder().setPath(path) ;
			if(doGet) {
				method = new HttpGet (builder.build()) ;
			}else{
				HttpPost post = new HttpPost(builder.build()) ;
				StringEntity input = new StringEntity(data, ContentType.APPLICATION_JSON);
				post.setEntity(input);
				method = post ;
			}
			response = client.execute(method) ;
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {				
				System.out.println("HttpClient execute failed.["+response.getStatusLine().getStatusCode()+"]:"+response.getStatusLine().getReasonPhrase()) ;
			}else{
				HttpEntity entity = response.getEntity() ;
				rtnData = EntityUtils.toString(entity) ;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(response != null) {
				try {
					response.close() ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(method != null) {
				method.releaseConnection(); 
			}
			if(client != null) {
				try {
					client.close() ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return rtnData;
	}
}
