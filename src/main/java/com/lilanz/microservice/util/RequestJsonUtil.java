package com.lilanz.microservice.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import javax.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;


/**
 * 获取前端参数流并转为json字符串 
 **/
@Component
public class RequestJsonUtil {

    private String getJsonStrByGETRquest(HttpServletRequest request) throws UnsupportedEncodingException {
        String json = null;
        String httpMethod = request.getMethod();
        if ("GET".equals(httpMethod)) {
        	json = new String(request.getQueryString().getBytes("iso-8859-1"), "utf-8").replaceAll("%22", "\"");
        }
        return json;
    }

    private String getJsonStrByPOSTRquest(HttpServletRequest request) throws IOException {
        String json = null;
        String httpMethod = request.getMethod();
        if ("POST".equals(httpMethod)){
            //判断是否为空
            int contentLength = request.getContentLength();
            //System.out.println("contentLength:"+contentLength);
            if (contentLength < 0) {
                return null;
            }
            byte buffer[] = new byte[contentLength];
//            for (int i = 0; i < contentLength; ) {
//
//                int readlen = request.getInputStream().readLine(buffer, i, contentLength - i);//.read(buffer, i, contentLength - i);
//                if (readlen == -1) {
//                    break;
//                }
//                i += readlen;
//            }
            request.getInputStream().read(buffer);
            String charEncoding = request.getCharacterEncoding();
            if (charEncoding == null) {
                charEncoding = "UTF-8";
            }
            json = new String(buffer, charEncoding);
        }
        return json;
    }
}
