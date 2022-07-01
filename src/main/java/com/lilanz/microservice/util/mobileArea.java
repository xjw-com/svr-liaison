package com.lilanz.microservice.util;

import com.lilanz.microservice.entity.Res;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class mobileArea {
    private static String getSoapRequest(String mobileCode) {

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "\n"
                + "<soap:Envelope xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" + " "
                + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\"" + " "
                + "xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">" + "\n"
                + "<soap:Body>" + "\n"
                + "<getMobileCodeInfo" + " " + "xmlns=\"http://WebXml.com.cn/\">" + "\n"
                + "<mobileCode>" + mobileCode + "</mobileCode>" + "\n"
                + "<userID></userID>" + "\n"
                + "</getMobileCodeInfo>" + "\n"
                + "</soap:Body>" + "\n"
                + "</soap:Envelope>"
        );
        return sb.toString();

    }

    private static InputStream getSoapInputStream(String mobileCode) {
        try {
            String soap = getSoapRequest(mobileCode);
            if (soap == null)
                return null;
            URL url = new URL("http://www.webxml.com.cn/WebServices/MobileCodeWS.asmx");
            URLConnection conn = url.openConnection();
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "text/xml; charset=utf-8");
            conn.setRequestProperty("Content-Length", Integer.toString(soap.length()));
            conn.setRequestProperty("SOAPAction", "http://WebXml.com.cn/getMobileCodeInfo");
            OutputStream os = conn.getOutputStream();
            OutputStreamWriter osw = new OutputStreamWriter(os, "UTF-8");
            osw.write(soap);
            osw.flush();
            osw.close();
            osw.close();
            InputStream is = conn.getInputStream();
            return is;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

    public static String getMobileNoTrack(String mobileCode) {
        try {
            org.w3c.dom.Document document = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            InputStream is = getSoapInputStream(mobileCode);
            DocumentBuilder db = dbf.newDocumentBuilder();
            document = db.parse(is);
            NodeList nl = document.getElementsByTagName("getMobileCodeInfoResult");
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < nl.getLength(); i++) {
                org.w3c.dom.Node n = nl.item(i);
                if (n.getFirstChild().getNodeValue().equals("手机号码错误")) {
                    sb = new StringBuffer("#");
                    System.out.println("手机号码输入有误");
                    break;
                }
                sb.append(n.getFirstChild().getNodeValue() + "\n");
            }
            is.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getMobileAttribution(String Mobile){
        String str = "";
        str = mobileArea.getMobileNoTrack(Mobile);
        if(str != null && !"".equals(str)){
            str = str.substring(str.indexOf("：")+1);
            String strArry [] = new String[]{};
            strArry = str.split(" ");
            str = strArry[0];
        }
        return str;
    }

}
