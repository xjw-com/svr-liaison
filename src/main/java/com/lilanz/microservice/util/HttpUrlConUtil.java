package com.lilanz.microservice.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;
import javax.net.ssl.TrustManager;

public class HttpUrlConUtil {
	public static final int DEFAULT_BUFFER_SIZE = 1024*4 ;

	public static String httpAjax(String redirectPath) {
		String[] paths = redirectPath.split("&");
	    String head = paths[0].substring(0,paths[0].indexOf("/setMy")+1);
	    //第一次连接，返回session id
        String sessionId= httpSendIIS(paths[0],"",1);        
        //第二次使用session id连接
        paths[1] = paths[1].replace("path=", "");
        if(paths[1].startsWith("../")) paths[1] = paths[1].substring(3);
		try {
			paths[1] = URLDecoder.decode(paths[1],"gb2312");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        paths[1] = head + paths[1];
        String result = httpSendIIS(paths[1],sessionId,0);
        return result;
	}
	
	public static String httpPostURlCon(String link, String params) {
		
		HttpURLConnection httpUrlConnection = null;
		URL url = null;
		InputStreamReader reader=null;
		InputStream urlStream =null;
		Writer sw=null;
		try {
			url = new URL(link);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			httpUrlConnection.setConnectTimeout(80000);
			httpUrlConnection.setRequestProperty("Accept-Charset", "UTF-8");
			httpUrlConnection.setRequestProperty("connection", "keep-Alive");
			httpUrlConnection.setRequestMethod("POST");
			// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在 http正文内，因此需要设为true, 默认情况下是false;
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setDoInput(true); // 设置是否从httpUrlConnection读入，默认情况下是true;
			httpUrlConnection.setUseCaches(false); // Post 请求不能使用缓存
			httpUrlConnection.setRequestProperty("Content-type", "application/json");
			httpUrlConnection.connect();

			// 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，所以在开发中不调用上述的connect()也可以。
			OutputStream outStrm = httpUrlConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStrm));
			writer.write(params);
			writer.flush();
			writer.close();

			sw = new StringWriter();
			// getInputStream, 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
			urlStream = httpUrlConnection.getInputStream(); // 向对象输出流写出数据，这些数据将存到内存缓冲区中 
			reader = new InputStreamReader(urlStream);
			int n = 0;
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			while (-1 != (n = reader.read(buffer))) {
				sw.write(buffer, 0, n);
			}
			reader.close();
			urlStream.close();
			sw.close();
			httpUrlConnection.disconnect();
			return sw.toString();
		}catch (Exception e) {
			LilanzLogger.error(e);
			e.printStackTrace();
		}finally{
			try {
				if(reader!=null) {
					reader.close();
				}
				if(urlStream!=null) {
					urlStream.close();
				}
				if(sw!=null) {
					sw.close();
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
			httpUrlConnection.disconnect();
		}
		return null;
	}
    public static String httpSendIIS(String link,String sessionId,int type) {
		HttpURLConnection httpUrlConnection = null;
		InputStreamReader reader=null;
		InputStream urlStream =null;
		try {
			URL url = new URL(link);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			httpUrlConnection.setConnectTimeout(60000);
			httpUrlConnection.setRequestProperty("Accept-Charset", "UTF-8");
			httpUrlConnection.setRequestProperty("connection", "keep-Alive");
			httpUrlConnection.setRequestMethod("POST");
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setUseCaches(false); // Post 请求不能使用缓存
			httpUrlConnection.setRequestProperty("Content-type", "application/json");
			if(sessionId!=null) httpUrlConnection.setRequestProperty("Cookie", sessionId);
			
			httpUrlConnection.getOutputStream().close();
			// 调用getInputStream,将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
			urlStream = httpUrlConnection.getInputStream(); // 向对象输出流写出数据，这些数据将存到内存缓冲区中 
			Writer sw = new StringWriter();
			if(type!=0) {
				System.out.print("cookie:"+httpUrlConnection.getHeaderField("Set-Cookie"));
				sw.write(httpUrlConnection.getHeaderField("Set-Cookie"));
			}else {
				reader = new InputStreamReader(urlStream,"GBK");//旧页面
				int n = 0;
				char[] buffer = new char[DEFAULT_BUFFER_SIZE];
				while (-1 != (n = reader.read(buffer))) {
					sw.write(buffer, 0, n);
				}
				reader.close();
			}
			urlStream.close();
			httpUrlConnection.disconnect();
			return sw.toString();
		} catch (Exception e) {
			LilanzLogger.error(e);
			e.printStackTrace();
		}finally{
			try {
				if(reader!=null)  reader.close();
				urlStream.close();
			}catch (IOException e) {
				e.printStackTrace();
			}
			httpUrlConnection.disconnect();
		}
		return null;
	}
    
    //20210318 短信接口跳过认证
    public static String httpsPostURlCon(String link, String params) {

		HttpURLConnection httpUrlConnection = null;
		URL url = null;
		InputStreamReader reader = null;
		InputStream urlStream = null;
		Writer sw = null;
		try {
			/* 尝试跳过验证 开始 lxc*/

			// 直接通过主机认证
			HostnameVerifier hv = new HostnameVerifier() {
				@Override
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};
			// 配置认证管理器
			TrustManager[] trustAllCerts = { new TrustAllTrustManager() };
			SSLContext sc = SSLContext.getInstance("SSL");
			SSLSessionContext sslsc = sc.getServerSessionContext();
			sslsc.setSessionTimeout(0);
			sc.init(null, trustAllCerts, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// 激活主机认证
			HttpsURLConnection.setDefaultHostnameVerifier(hv);

			/* 尝试跳过验证结束  lxc */
			
			url = new URL(link);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			httpUrlConnection.setConnectTimeout(80000);
			httpUrlConnection.setRequestProperty("Accept-Charset", "UTF-8");
			httpUrlConnection.setRequestProperty("connection", "keep-Alive");
			httpUrlConnection.setRequestMethod("POST");
			// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在 http正文内，因此需要设为true, 默认情况下是false;
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setDoInput(true); // 设置是否从httpUrlConnection读入，默认情况下是true;
			httpUrlConnection.setUseCaches(false); // Post 请求不能使用缓存
			httpUrlConnection.setRequestProperty("Content-type", "application/json");
			httpUrlConnection.connect();

			// 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，所以在开发中不调用上述的connect()也可以。
			OutputStream outStrm = httpUrlConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStrm));
			writer.write(params);
			writer.flush();
			writer.close();

			sw = new StringWriter();
			// getInputStream, 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
			urlStream = httpUrlConnection.getInputStream(); // 向对象输出流写出数据，这些数据将存到内存缓冲区中
			reader = new InputStreamReader(urlStream);
			int n = 0;
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			while (-1 != (n = reader.read(buffer))) {
				sw.write(buffer, 0, n);
			}
			reader.close();
			urlStream.close();
			sw.close();
			httpUrlConnection.disconnect();
			return sw.toString();
		} catch (Exception e) {
//			LilanzLogger.error(e);
			e.printStackTrace();
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (urlStream != null) {
					urlStream.close();
				}
				if (sw != null) {
					sw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			httpUrlConnection.disconnect();
		}
		return null;
	}
    
	//TODO 20210310 新增 访问时代凌宇接口
	public static String httpPostURlDataTransfer(String link, String params) {
		 String X_HW_ID="Hi-fjdx__dataservice.api.person";
		 String X_HW_APPKEY="alBg39OupN5hXW4400PCgw==";
		
		HttpURLConnection httpUrlConnection = null;
		URL url = null;
		InputStreamReader reader=null;
		InputStream urlStream =null;
		Writer sw=null;
		try {
			/* 尝试跳过验证 开始 */

			// 直接通过主机认证
			HostnameVerifier hv = new HostnameVerifier() {
				@Override
				public boolean verify(String urlHostName, SSLSession session) {
					return true;
				}
			};
			// 配置认证管理器
			TrustManager[] trustAllCerts = { new TrustAllTrustManager() };
			SSLContext sc = SSLContext.getInstance("SSL");
			SSLSessionContext sslsc = sc.getServerSessionContext();
			sslsc.setSessionTimeout(0);
			sc.init(null, trustAllCerts, null);
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			// 激活主机认证
			HttpsURLConnection.setDefaultHostnameVerifier(hv);
			
			
			url = new URL(link);
			httpUrlConnection = (HttpURLConnection) url.openConnection();
			
			
			httpUrlConnection.setConnectTimeout(120000);
			httpUrlConnection.setRequestProperty("Accept-Charset", "UTF-8");
			httpUrlConnection.setRequestProperty("connection", "keep-Alive");
			httpUrlConnection.setRequestMethod("POST");
			// 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在 http正文内，因此需要设为true, 默认情况下是false;
			httpUrlConnection.setDoOutput(true);
			httpUrlConnection.setDoInput(true); // 设置是否从httpUrlConnection读入，默认情况下是true;
			httpUrlConnection.setUseCaches(false); // Post 请求不能使用缓存
			httpUrlConnection.setRequestProperty("Content-type", "application/json");
			httpUrlConnection.setRequestProperty("X-HW-ID", X_HW_ID);
			httpUrlConnection.setRequestProperty("X-HW-APPKEY", X_HW_APPKEY);
			httpUrlConnection.connect();
			
			// 此处getOutputStream会隐含的进行connect(即：如同调用上面的connect()方法，所以在开发中不调用上述的connect()也可以。
			OutputStream outStrm = httpUrlConnection.getOutputStream();
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outStrm));
			writer.write(params);
			writer.flush();
			writer.close();

			sw = new StringWriter();
			// getInputStream, 将内存缓冲区中封装好的完整的HTTP请求电文发送到服务端。
			urlStream = httpUrlConnection.getInputStream(); // 向对象输出流写出数据，这些数据将存到内存缓冲区中 
			reader = new InputStreamReader(urlStream);
			int n = 0;
			char[] buffer = new char[DEFAULT_BUFFER_SIZE];
			while (-1 != (n = reader.read(buffer))) {
				sw.write(buffer, 0, n);
			}
			reader.close();
			urlStream.close();
			sw.close();
			httpUrlConnection.disconnect();
			return sw.toString();
		}catch (Exception e) {
			LilanzLogger.error(e);
			e.printStackTrace();
		}finally{
			try {
				if(reader!=null) {
					reader.close();
				}
				if(urlStream!=null) {
					urlStream.close();
				}
				if(sw!=null) {
					sw.close();
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
			httpUrlConnection.disconnect();
		}
		return null;
	}
}

//一个类，信任所有，一个尝试直接跳过安全证书验证的努力 lxc。
class TrustAllTrustManager implements TrustManager, javax.net.ssl.X509TrustManager {
	@Override
	public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		return null;
	}

	@Override
	public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType)
			throws java.security.cert.CertificateException {
		return;
	}

	@Override
	public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType)
			throws java.security.cert.CertificateException {
		return;
	}

}
