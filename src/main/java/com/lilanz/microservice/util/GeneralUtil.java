package com.lilanz.microservice.util;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

public class GeneralUtil {

    /**
     * 获取当前系统时间
     **/
    public static String getNowDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    /**
     * 获取当前年份
     **/
    public static String getNowYear() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
        return sdf.format(new Date());
    }

    /**
     * 获取时间戳
     **/
    public static String getTimeStamp() {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");//设置日期格式
        return df.format(new Date());// new Date()为获取当前系统时间
    }

    /**
     * 获取时间与当前时间差
     **/
    public static long getTimeDifference(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            long old = sdf.parse(time).getTime();
            long now = sdf.parse(sdf.format(new Date())).getTime();
            return (now - old) / 1000;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 集合分组
     **/
    public static List<Map<String, Object>> getListByGroup(Map<Object, List<Map<String, Object>>> info, String sum) {
        List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
        Iterator<Object> iter = info.keySet().iterator();
        String params = "";
        while (iter.hasNext()) {
            Object key = iter.next();
            List<Map<String, Object>> value = info.get(key);
            Map<String, Object> infoMap = info.get(key).get(0);
            for (int i = 1; i < value.size(); i++) {
                Iterator<String> valueIter = value.get(i).keySet().iterator();
                while (valueIter.hasNext()) {
                    String valueKey = valueIter.next();
                    if (sum.indexOf(valueKey) >= 0) {
                        if ("jd".equals(valueKey)) {

                        } else if ("totalsessiontime".equals(valueKey)) {
                            if (Double.valueOf(value.get(i).get("totalsessiontime").toString()) >= Double.valueOf(value.get(i).get("sc").toString())) {
                                infoMap.put(valueKey, Double.valueOf(value.get(i).get("sc").toString()) + Double.valueOf(infoMap.get(valueKey).toString()));
                            } else {
                                infoMap.put(valueKey, Double.valueOf(value.get(i).get(valueKey).toString()) + Double.valueOf(infoMap.get(valueKey).toString()));
                            }
                        } else {
                            infoMap.put(valueKey, Double.valueOf(value.get(i).get(valueKey).toString()) + Double.valueOf(infoMap.get(valueKey).toString()));
                        }
                    } else {
                        if (infoMap.get(valueKey).toString().compareTo(value.get(i).get(valueKey).toString()) < 0) {
                            infoMap.put(valueKey, value.get(i).get(valueKey));
                        }
                    }
                }
            }
            if (sum.indexOf("jd") >= 0) {
                Integer sc = new Double(Double.valueOf(infoMap.get("sc").toString()) * 100).intValue();
                Integer totaltime = new Double(Double.valueOf(infoMap.get("totalsessiontime").toString()) * 100).intValue();
                System.out.println("sc:" + sc + "_" + totaltime);
                DecimalFormat df = new DecimalFormat("#0.00");
                System.out.println("jd:" + df.format((float) totaltime / sc));
                infoMap.put("jd", df.format(((float) totaltime / sc) * 100));
            }
            res.add(infoMap);
        }
        return res;
    }

    /* 生成新文件名 */
    public static String getNewFileName(String fileName) {
        String oldName = fileName.substring(0, fileName.lastIndexOf("."));
        if (oldName.length() > 15) {
            oldName = oldName.substring(0, 15);
        }
        String type = fileName.substring(fileName.lastIndexOf("."));
        return (oldName + "-" + (int) (Math.random() * 999999) + "-" + GeneralUtil.getTimeStamp() + type);
    }
    /**
     * 对集合进行分组
     **/
//	private static List<Map<String,Object>> getListByGroup(List<Map<String,Object>> list) {
//		List<Map<String,Object>> result = new ArrayList<Map<String,Object>>();
//		Map<String, Integer> map = new HashMap<String, Integer>();
//
//		for (Map<String,Object> bean : list) {
//			if (map.containsKey(bean.getGroup())) {
//				map.put(bean.getGroup(), map.get(bean.getGroup()) + bean.getMoney());
//			} else {
//				map.put(bean.getGroup(), bean.getMoney());
//			}
//		}
//		for (Entry<String, Integer> entry : map.entrySet()) {
//			result.add(new JavaBean(entry.getKey(), entry.getValue()));
//		}
//		return result;
//	}

}
