package com.lilanz.microservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.lilanz.microservice.util.LilanzLogger;



public class MyRequest {
	/**
	 * 向指定URL发送GET方法的请求
	 * 
	 * @param url
	 *            发送请求的URL
	 * @param param
	 *            请求参数，请求参数应该是 name1=value1&name2=value2 的形式。
	 * @return URL 所代表远程资源的响应结果
	 */
	public static String sendGet(String url, String param) {
		String result = "";
		BufferedReader in = null;
		try {
			String urlNameString="";
			if("".equals(param)) {
				urlNameString = url ;
			}else {
				urlNameString = url + "?" + param;
			}
			//String urlNameString = url + "?" + param;
			URL realUrl = new URL(urlNameString);
			// 打开和URL之间的连接
			URLConnection connection = realUrl.openConnection();
			// 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
			// 建立实际的连接
			connection.connect();
			// 获取所有响应头字段
			Map<String, List<String>> map = connection.getHeaderFields();
			// 遍历所有的响应头字段
			for (String key : map.keySet()) {
				//System.out.println(key + "--->" + map.get(key));
			}
			// 定义 BufferedReader输入流来读取URL的响应
			in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {

			System.out.println("发送GET请求出现异常！" + e);
			e.printStackTrace();
			LilanzLogger.error(e);
		}
		// 使用finally块来关闭输入流
		finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return result;
	}

	/**
	 * 向指定 URL 发送POST方法的请求
	 * 
	 * @param url
	 *            发送请求的 URL
	 * @param param
	 *            请求参数，请求参数应该是 json 的形式。
	 * @return 所代表远程资源的响应结果
	 */ 
	public static String sendPostUTF8(String url, String params) {
        
        //System.out.println(url);  
        //System.out.println(params); 
        DefaultHttpClient httpClient = new DefaultHttpClient();  
        httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,80000);
        httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,80000);
        HttpPost httpPost = new HttpPost(url);  
        
        // 设置请求的header  
        httpPost.addHeader("Content-Type", "application/json;charset=utf-8");  
        StringEntity entity = new StringEntity(params, "utf-8");  
        entity.setContentEncoding("UTF-8");  
        entity.setContentType("application/json");  
        httpPost.setEntity(entity);  
        
        // 执行请求  
        CloseableHttpResponse response=null;   
        String result ="";
		try {
			response = httpClient.execute(httpPost);
			HttpEntity  resEntity = response.getEntity();
			result = EntityUtils.toString(resEntity, "utf-8");
			EntityUtils.consume(resEntity);
			//System.out.println(result);
		}catch (ClientProtocolException e) {
			LilanzLogger.error(e);
			e.printStackTrace();
		}catch (ParseException e) {
			LilanzLogger.error(e);
			e.printStackTrace();
		} catch (IOException e) {
			LilanzLogger.error(e);
			e.printStackTrace();
		}finally {
		    try {
		    	if(response!=null) {
		    		response.close();
		    	}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return result;
	}
    
	//post 以键值对的形式 提交参数
	public static String sendPostByKeyVal(String url, Map<String,String> params) {
		DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(url);
        
        httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");  
        String rev ="";
        CloseableHttpResponse response=null; 
        try { 
			   if(null != params) {
			        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			        for (String name : params.keySet()) {
			            nvps.add(new BasicNameValuePair(name, params.get(name)));
			        }
			        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(nvps,"utf-8");
			        entity.setContentEncoding("UTF-8");
			        entity.setContentType("application/x-www-form-urlencoded");
			        httpPost.setEntity(entity);
			    }
		        response = httpClient.execute(httpPost);    
		        int code = response.getStatusLine().getStatusCode();    //检验状态码，如果成功接收数据      
		        if (code == 200) {    
		        	  HttpEntity  resEntity = response.getEntity();
		              rev = EntityUtils.toString(resEntity);
		              EntityUtils.consume(resEntity);
		              return rev;
		        } 
        } catch (ClientProtocolException e) {  
        	LilanzLogger.error(e);
            e.printStackTrace();  
        } catch (IOException e) { 
        	LilanzLogger.error(e);
            e.printStackTrace();  
        } catch (Exception e) {
        	LilanzLogger.error(e);
            e.printStackTrace();  
        }finally {
		    try {
				response.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}   
        return rev;
	}

}
