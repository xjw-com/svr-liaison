package com.lilanz.microservice.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class RandomNumber {

	//获取随机数
	public static int getRandNum(int min, int max) {
	    int randNum = min + (int)(Math.random() * ((max - min) + 1));
	    return randNum;
	}

}
