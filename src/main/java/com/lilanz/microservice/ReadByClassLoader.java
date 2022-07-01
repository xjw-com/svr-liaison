package com.lilanz.microservice;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

public class ReadByClassLoader {

	public static void readPropFileByClassLoad() throws IOException {
		// 读取src下面config包内的配置文件db3.properties
		InputStream in = ReadByClassLoader.class.getClassLoader().getResourceAsStream("bootstrap.properties");
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		Properties props = new Properties();
		try {
			props.load(br);
			ConfigReader.baseWebInforMap = new HashMap<String,String>();		
			for (Object s : props.keySet()) {
				ConfigReader.baseWebInforMap.put((String) s, props.getProperty(s.toString()));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			br.close();
			in.close();			
		}
	}
	
}
