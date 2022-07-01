package com.lilanz.microservice.controller;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lilanz.microservice.ConfigReader;


import com.lilanz.microservice.entity.Res;
import com.lilanz.microservice.service.LiaisonService;
import com.lilanz.microservice.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;


@RestController
@RequestMapping("/webframe")
public class liaisonCoreController {
    public static String GATEWALL = ConfigReader.get("gatewall");
    private String FileDown = ConfigReader.get("LiaisonImgUploadPath");
    @Autowired
    private LiaisonService liaisonService;

    @RequestMapping("sendCode")
    public Res sendCode(HttpServletRequest req, HttpServletResponse resp, @RequestBody String data) throws Exception {
        Res res = null;
        Map<String, Object> params = JSONObject.parseObject(data, new TypeReference<Map<String, Object>>() {
        });
        String MyPhone = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("MyPhone")));
        String MyName = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("MyName")));
        HttpSession session = req.getSession();
        JSONObject obj = new JSONObject();
        int yzm = RandomNumber.getRandNum(100000, 999999);
        String  smsContent=MyName+"您好，您的验证码：" + yzm + "，此验证码只用于福建干部网络学院联络员申请登录验证，请勿转发他人，3分钟内有效！【福建干部网络学院】";

       // return SMSGroupSendingUtil.smsSingleSend(smsContent,rsvMobiles);// 像这样调用。
       // String dxyz =   HttpUrlConUtil.httpPostURlCon(GATEWALL + "base-datams-tlsoft/Send/"+MyPhone+"/"+yzm, obj.toJSONString());
        res =SMSGroupSendingUtil.smsSingleSend(smsContent,MyPhone);

        if (res.getErrcode() == 0) {
            session.setAttribute("SMSVerifCode", yzm);
            System.out.println(yzm);
            res=new Res(0,mobileArea.getMobileAttribution(MyPhone),0);
        }
        return res;

    }

    @RequestMapping("verificat")
    public  Res  verificat(HttpServletRequest req,HttpServletResponse resp,@RequestBody String data) {
        Res res=null;
        Map<String, Object> params = JSONObject.parseObject(data, new TypeReference<Map<String, Object>>() {
        });
        String Code = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Code")));
        HttpSession session = req.getSession();
        String result="";
        //获取登陆信息
            if(session.getAttribute("SMSVerifCode")!=null) {
                String codesession=session.getAttribute("SMSVerifCode").toString();
                if(Code.equals(codesession)) {
                    session.setAttribute("verifySuccess", true);
                    result = "验证成功！";
                  //  session.setAttribute("SMSVerifCode", "");
                  //  session.removeAttribute("SMSVerifCode");
                    res=new Res(0,result,0);
                }else {
                    result = "验证码不正确，请重新输入！";
                    res=new Res(1,result,0);
                }
            }else{
                result = "验证码不存在或验证码已失效！";
                res=new Res(1,result,0);
            }
        return  res;
    }

    @RequestMapping("sendEMail")
    public Res sendEMail(HttpServletRequest req,HttpServletResponse resp, @RequestBody String data) {
        Res res=null;
        Map<String, Object> params = JSONObject.parseObject(data, new TypeReference<Map<String, Object>>() {
        });
        String MyEmail = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("MyEmail")));
        String MyName = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("MyName")));
        HttpSession session = req.getSession();
            int yzm=RandomNumber.getRandNum(100000,999999);
            //SendEMailUtil sendEmail=new SendEMailUtil();
            //System.out.println(data.email);
            //res = sendEmail.SendMail(data.email.toLowerCase(), "驗證碼：" + yzm + "，此驗證碼只用於登錄驗證，請勿轉發他人，3分鐘內有效！【福建幹部網絡學院】","福建干部网络学院管理平台登录验证");
            //res = sendEmail.SendMail(data.email.toLowerCase(), cname+" 您好，您的验证码：" + yzm + "，此验证码只用于登录验证，请勿转发他人，3分钟内有效！【福建干部网络学院】","福建干部网络学院管理平台登录验证");
            MySendMail send=new MySendMail();
            res = send.sendmail(MyEmail.toLowerCase(),  MyName+"您好，您的验证码：" + yzm + "，此验证码只用于福建干部网络学院联络员申请登录验证，请勿转发他人，3分钟内有效！【福建干部网络学院】","福建干部网络学院联络员申请");
            if(res.getErrcode()==0) {
                session.setAttribute("SMSVerifCode", yzm);
                res.setData(JSONObject.toJSONString(data));
            }
        return  res;
    }

    @RequestMapping("cleanCode")
    public  Res  cleanCode(HttpServletRequest req,HttpServletResponse resp) {
        HttpSession session = req.getSession();
        session.setAttribute("SMSVerifCode", "");
        session.removeAttribute("SMSVerifCode");
        return  new Res(0,"ok",0);
    }
    @RequestMapping("applicant")
    public Res applicantemail(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.applicantupdate(request, response, params);
    }
    @RequestMapping("updatelogin")
    public Res updatelogin(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.updatelogin(request, response, params);
    }

    @RequestMapping("selectArea")
    public Res selectArea(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.selectArea(request, response, params);
    }

    @RequestMapping("selectPermissions")
    public Res selectPermissions(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.selectPermissions(request, response, params);
    }

    @RequestMapping("selectPermissions2")
    public Res selectPermissions2(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.selectPermissions2(request, response, params);
    }

    @RequestMapping("CheckInformation")
    public Res CheckInformation(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.CheckInformation(request, response, params);
    }

    @RequestMapping("SaveData")
    public Res SaveData(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.SaveData(request, response, params);
    }


    @RequestMapping("ApplicationInformation")
    public Res ApplicationInformation(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.ApplicationInformation(request, response, params);
    }

    @RequestMapping("selectUnitData")
    public Res selectUnitData(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.selectUnitData(request, response, params);
    }

    @RequestMapping("deleteData")
    public Res deleteData(HttpServletRequest request,HttpServletResponse response,@RequestBody String data) {
        Map<String,Object> params=JSONObject.parseObject(data, new TypeReference<Map<String,Object>>(){});
        return liaisonService.deleteData(request, response, params);
    }

}
