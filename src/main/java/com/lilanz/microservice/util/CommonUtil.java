package com.lilanz.microservice.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;


import org.apache.tomcat.util.codec.binary.Base64;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;



/**
 * 修改日期：2019-11-4
 * 修改内容：handleParameter方法中新增判断sessionParamMap为null时new一个对象
 * */

public class CommonUtil {
	
	public static   String  fileRoot;
	public static  SimpleDateFormat  format = new SimpleDateFormat("yyyy-MM-dd");


    public static boolean isInnerNetwork(String ip) {
    	if(ip.startsWith("192.168.") || ip.startsWith("10.0.") || ip.startsWith("172.16.")
		||ip.startsWith("121.207.5.")) return  true;
         return false;    	
    }
	public static  String getUserIpInfo(String ip) {
		String ipInfo = "登录IP:" +ip +"局域网  ";
		if(	isInnerNetwork(ip)) {
			ipInfo += "对方和您在同一内部网";
	    }else {
	    	ipInfo += "对方和您不在同一内部网";
	    }
		return ipInfo;
	}
	public  static String getIpAdd() throws Exception {
		String ip="";
		for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
            NetworkInterface intf = en.nextElement();
            String name = intf.getName();
            if (!name.contains("docker") && !name.contains("lo")) {
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    //获得IP
                	InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        String ipaddress = inetAddress.getHostAddress().toString();
                        if (!ipaddress.contains("::") && !ipaddress.contains("0:0:") && !ipaddress.contains("fe80")) {
                            if(!"127.0.0.1".equals(ip)){
                            	ip = ipaddress;
                            }
                        }
                    }
                }
            }
        }
		return ip;
	}
	public static String getIP(HttpServletRequest request){  
        String ip=request.getHeader("x-forwarded-for");  
        if(ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){  
            ip=request.getHeader("Proxy-Client-IP");  
        }  
        if(ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){  
            ip=request.getHeader("WL-Proxy-Client-IP");  
        }  
        if(ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){  
            ip=request.getHeader("X-Real-IP");  
        }  
        if(ip==null || ip.length()==0 || "unknown".equalsIgnoreCase(ip)){  
            ip=request.getRemoteAddr();  
            if (ip.equals("0:0:0:0:0:0:0:1")) {
                // 根据网卡取本机配置的IP
                InetAddress inet = null;
                try {
                    inet = InetAddress.getLocalHost();
                } catch (UnknownHostException e) {
        			LilanzLogger.error(e);
                    e.printStackTrace();
                }
                ip = inet.getHostAddress();
            }
        }  
        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ip != null && ip.length() > 15) { 
            if (ip.indexOf(",") > 0) {
                ip = ip.substring(0, ip.indexOf(","));
            }
        }
        return ip;  
    }  
	
	public static String backPage(int  port) {
		String result = "pos";
		if (port!=8834) {
			result = "";
		}
		return  result;
	}
	
	/*将request 参数重新 封装成HashMap
	 *isSqlParam 是否是sql 中的参数，如果是 空字符串 不抛弃
	 */
	public static Map<String,Object> getParamMap(HttpServletRequest request,boolean isSqlParam) {
		Map<String,String[]> paramMapOnlyRead = request.getParameterMap();
		Map<String,Object> paramMap = new HashMap<>();
	    Set<String> keys= paramMapOnlyRead.keySet();
	    //bid submit（必备的参数在10个以上） 与 通过中转页（打开新的二级BBcommon.jsp必备从参数在5以上） 的区分标识 是 参数的个数
	    //if(!isSqlParam  &&  keys.size() <2) return null;
	    String value= null;
	    for(String key:keys){
	    	if(key.length()==0) continue;	
	    	value = paramMapOnlyRead.get(key)[0] ;   
	    	if(!isSqlParam && value.length()==0) continue;//sql 参数，空字符串 参数不能抛掉

	    	if(key.equalsIgnoreCase("MyBBbid")){
	    		paramMap.put("bid", value);
	   	    }else if(key.equalsIgnoreCase("mydjid")) {
	   	    	paramMap.put("mydjid", value);
	   	    }else if( key.equals("flowdxid")) {
	   	    	if(!paramMap.containsKey("mydjid"))  
	   	    		paramMap.put("mydjid", value.length()==0?"0":value);
	   	    	paramMap.put("flowdxid", value);
	   	    }else if(key.equals("MyQueryString")) {
	   	    	paramMap.put(key, value);
				value = myBase64Decode(value);
				String[]  myParams = value.split("&");
				int index=0;
				for(String param:myParams) {
					index = param.indexOf("=");
					if(param.length() == 0 ||index<0 ) continue;
					key = param.substring(0,index);
					value = param.substring(index+1);
					if(value.trim().length() == 0) continue;
					if(key.equalsIgnoreCase("mydjid")) {//||key.equalsIgnoreCase("MyBBeditid")
			   	    	paramMap.put("mydjid", value); continue;
					}
					if(key.equalsIgnoreCase("MyBBeditid")) {
						paramMap.put("MyBBeditid", value); continue;
					}
					paramMap.put(key, value);
				}	   	    	
	   	    }else {
	    	    paramMap.put(key, value);
	   	    }
	   	    
	    }
	    return paramMap;
	}
	
	public static DecimalFormat df = new DecimalFormat("0.00");  
	public static String convertObjToStr(Object obj) {
		if(obj == null) {
			return  "";
		}else if (obj instanceof Long) {
			return df.format(obj);
		} else if (obj instanceof Double) {
			return df.format(obj);
		}else if (obj instanceof BigDecimal) {
			return ((BigDecimal) obj).toPlainString();
		}else if (obj instanceof Float) {
			return df.format(obj);
		}else if( obj instanceof Date) {
			return format.format((Date)obj);
    	}else {
			return obj.toString();
		}

	}
    
    //base64 编码解码
    public  static  String  myBase64Decode(String encodeStr) {
    	try {
    		return new  String(Base64.decodeBase64(encodeStr.replaceAll("\\s", "+").replace("%2B", "+")
					.replace("%3D", "=").replace("%2","/")
					.getBytes()),"utf-8");
    	}catch (UnsupportedEncodingException e) {
    	   LilanzLogger.error(e);
    	   e.printStackTrace();
    	   return null;
    	}
    }
    public  static  String  myBase64Encode(String originalStr) {
    	try {
    		return Base64.encodeBase64String(originalStr.getBytes("utf-8"));
    	}catch (UnsupportedEncodingException e) {e.printStackTrace();
    	   LilanzLogger.error(e);
    	   return null;
    	}
    }  
    /*
     * 通过框架或界面直接打开的通用页面 1.参数的获取优先顺序 ①request请求体(前提是请求体参数的个数大于2) ②paramStr ③session
	 * 的paramMap
     */
	public static Map<String, Object>  handleParameter(HttpServletRequest req,String paramStr) {

			Map<String, Object> paramsMap = CommonUtil.getParamMap(req, false);
		    boolean isPost = false;
		    if(req.getMethod().equals("POST") ) {
		    	if(!paramsMap.containsKey("MySession")) {
		    		paramsMap = JSONObject.parseObject(paramStr, new TypeReference<HashMap<String, Object>>() {});
		    		paramsMap.put("bid", req.getParameter("bid"));
		    	}
		    	isPost = true;
		    }else{
		    	//System.out.println(JSONObject.toJSONString((Map<String, Object>) req.getSession().getAttribute("paramMap")));
				Map<String, Object> sessionParamMap = (Map<String, Object>) req.getSession().getAttribute("paramMap");
				if(sessionParamMap==null) {
					sessionParamMap=new HashMap<String,Object>();
				}
				if(!paramsMap.containsKey("MySession")) {
					//System.out.println(JSONObject.toJSONString(paramsMap));
					sessionParamMap.put("bid", paramsMap.get("bid"));
					sessionParamMap.put("menuid",paramsMap.get("menuid"));
					sessionParamMap.put("scheduleid", paramsMap.get("scheduleid"));
					paramsMap = sessionParamMap;
				}else {
					paramsMap.put("MyBBbid", sessionParamMap.get("MyBBbid"));
					paramsMap.put("MyBBmenuid",sessionParamMap.get("MyBBmenuid"));
					paramsMap.put("scheduleid", paramsMap.get("scheduleid"));
				}
			}
		    paramsMap.put("$port", req.getServerPort());
		    paramsMap.put("$isPost", isPost);
			return paramsMap;
     }
	
	 public static boolean  checkPass(String pass) {
		 if(pass.matches("\\w*\\d+\\w*")) {
			 if(pass.replaceAll("[0-9]+", "").length()>0) return true;
		 }else {
	         if(pass.toLowerCase().replaceAll("[a-z]+", "").length()>0) return true;
		 }
		 return false;
	 }
	 public static  Map<String,String>  getParamsFromRequestbody(HttpServletRequest req){
			StringBuilder  builder = new StringBuilder();
			String temp;
			Map<String,String>  paramMap = null;
			BufferedReader reader= null;
			try {
				reader = req.getReader();
				while((temp = reader.readLine())!=null){
					builder.append(temp);
				}
				reader.close();
				paramMap = JSONObject.parseObject(builder.toString(),new TypeReference<Map<String,String>>(){});
			} catch (IOException e) {
				e.printStackTrace();
			}finally {
				if(reader!=null) {
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			return paramMap;
	 }
	 /*
	  * @param1  输出信息
	  * @param2  输出文件名
	  */
	 public static void  writeOutMyLog(String logText,String fileName) {
		  SimpleDateFormat  format = new SimpleDateFormat("yyyyMMdd hh:mm:ss");
		  String timeStr = format.format(new Date());
		  if(fileName == null || fileName.length()==0) {
			  fileName  = timeStr.substring(0,6);
		  }
		  File f = new File(fileRoot+"/logs/"+fileName+".txt");

		  FileWriter writer =null;
		  try {
			writer = new FileWriter(f,true);
			writer.append("\r\n"+ timeStr + "\r\n" +logText);
			writer.flush();
			writer.close();
		  } catch (FileNotFoundException e) {
			e.printStackTrace();
		  }catch (IOException e) {
			e.printStackTrace();
		  }finally {
			  if( writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			  }
		  }
	  }
	 
}
