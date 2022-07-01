package com.lilanz.microservice.util;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.springframework.web.multipart.MultipartFile;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.lilanz.microservice.ConfigReader;
import com.lilanz.microservice.entity.Res;

public class FileUpUtil {

    //上传文件，经测试
    /**
     * file:上传的文件
     * fileName：保存的文件名称（保存在数据库中的名称）
     * path:上传到服务器存放的路径 
     **/
    public static Res singleFileUpload(MultipartFile file,String fileName,String path) {
    	Res res =new Res(0,"上传成功","");
        if (Objects.isNull(file) || file.isEmpty()) {
            return new Res(1,"文件为空，请重新上传","");
        }
        File parentDir = new File(path);
        File targetPath = new File(parentDir, fileName);
        try {
	        file.transferTo(targetPath);
        } catch(Exception e){
        	e.printStackTrace();
        	res.setErrmsg("上传失败");
        	res.setErrcode(1);
        	res.setData(e.getMessage());
        }
        return res;
    }
	
	
//	/**
//     * 上传文件
//     * @param request
//     * @param response
//     * @param serverPath    服务器地址:(http://172.16.5.102:8090/)
//     * @param path             文件路径（不包含服务器地址：upload/）
//     * @return
//	 * @throws IOException 
//	 * @throws ClientHandlerException 
//	 * @throws UniformInterfaceException 
//     */
//    public static String upload(Client client, MultipartFile file, HttpServletRequest request,
//    		HttpServletResponse response, String serverPath, String path,String oldFileName) throws UniformInterfaceException, ClientHandlerException, IOException{
//        // 文件名称生成策略（UUID uuid = UUID.randomUUID()）
//        Date d = new Date();
//        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
//        String formatDate = format.format(d);
//        String str = "";
//        for(int i=0 ;i <5; i++){
//            int n = (int)(Math.random()*90)+10;
//            str += n;
//        }
//        // 获取文件的扩展名
//        
//        String extension = oldFileName.substring(oldFileName.lastIndexOf(".")+1);//file.getOriginalFilename()
//        
//        // 文件名
//        String fileName = formatDate + str + "." + extension;
//        //相对路径
//        String relaPath = path + fileName;
//        
//        String a = serverPath + path.substring(0, path.lastIndexOf("/"));
//        System.out.println("fileNmae:"+a);
//        File file2 = new File(a);
//        if(!file2.exists()){
//            boolean mkdirs = file2.mkdirs();
//            System.out.println(mkdirs);
//        }
//        
//        // 另一台tomcat的URL（真实路径）
//        String realPath = serverPath + relaPath;
//        // 设置请求路径
//        WebResource resource = client.resource(realPath);
//
//        // 发送开始post get put（基于put提交）
//        
//	    resource.put(String.class, file.getBytes());
//	    return fileName+";"+relaPath+";"+realPath;
//        
//    }
//    
//    /**
//     * 删除文件
//     * @param filePath（文件完整地址：http://172.16.5.102:8090/upload/1234.jpg）
//     * @return
//     */
//    public static String delete(String filePath){
//        try {
//            Client client = new Client();
//            WebResource resource = client.resource(filePath);
//            resource.delete();
//            return "y";
//        } catch (Exception e) {
//            e.printStackTrace();
//            return "n";
//        }
//    }
    
    
	/**
     * 通过文件绝对路径 删除单个文件,文件名要为路径全名。
     * @param filePath
     */
//	public static Res delFile(String fileName)
//    {
//		Res res=new Res(0,"ok","");
//		String result="ok";
//        try{
//            File file = new File(fileName);
//            if(file.isFile() && file.exists()) {
//            	file.delete();
//            	result=file.getName() + " 文件删除成功！";
//            }else{
//            	result=file.getName() + " 没有该文件，删除失败！";
//            }
//        }catch(Exception e){
//            e.printStackTrace();
//            res.setErrcode(1);
//            result="文件删除失败！";
//        }
//        res.setErrmsg(result);
//        return res;
//    }
	
	/* 删除文件或文件夹，其中file=new File(文件全路径+文件名); */
	public static boolean delFile(File file) {
        if (!file.exists()) {
            return false;
        }

        if (file.isDirectory()) {
            File[] files = file.listFiles();
            for (File f : files) {
                delFile(f);
            }
        }
        return file.delete();
    }
	
//	public static void main(String[] args) {
//		String fullName = ConfigReader.get("uploadPath")+"\\"+"001";
//		File file=new File(fullName);
//		System.out.println("结果=="+delFile(file));
//	}

}
