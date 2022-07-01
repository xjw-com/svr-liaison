package com.lilanz.microservice;

import java.io.IOException;
import java.util.HashMap;

public class ConfigReader {

	protected static HashMap<String, String> baseWebInforMap = null;
	{
		java.lang.System.setProperty("java.net.preferIPv4Stack", "true");
		java.lang.System.setProperty("java.net.preferIPv6Addresses", "false");
	}

	public static String get(String propertyName) {

		if (baseWebInforMap == null) {
			try {
				ReadByClassLoader.readPropFileByClassLoad();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return baseWebInforMap.get(propertyName);
	}

	public static void Init() {
		if (baseWebInforMap != null) {
			baseWebInforMap.clear();
			baseWebInforMap = null;
		}
	}
}
