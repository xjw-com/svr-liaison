package com.lilanz.microservice.util;

import org.apache.log4j.Logger;


public class LilanzLogger {
  public  static String svrName = "服务名:svr-learncenter";
  public static Logger logger=null ;
  
  public static void error(Exception e) {
		error(e,logger.getName() + " - 发生错误:" + e.getMessage() + "\n" 
				+ svrName);
  } 
  public static void error(Exception e,String Info) {
	  
		StackTraceElement[] stelist =e.getStackTrace();
		StringBuilder sb = new StringBuilder();
		for(StackTraceElement ste :stelist) {
			sb.append("类名:" + ste.getClassName()
			+ " 方法:"+ ste.getMethodName() + " 行:"+ ste.getLineNumber()  + "\n" 
			);    			
		}		
		logger.error(Info + "\n" + sb.toString());
  }
  
  public static void info(String info) {
	    logger.info(svrName + "\n" + info);
	    
  }
}
