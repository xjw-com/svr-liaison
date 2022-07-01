package com.lilanz.microservice.service;


import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.lilanz.microservice.ConfigReader;
import com.lilanz.microservice.MyRequest;
import com.lilanz.microservice.entity.Res;
import com.lilanz.microservice.util.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LiaisonService {

    private static String GATEWALL = ConfigReader.get("gatewall");
    private static String DATASERVICE = ConfigReader.get("dataService");
    private String uploadHost = ConfigReader.get("LiaisonImgUploadPath");  // 上传图片保存路径
    public Res applicantupdate(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String MyEmail = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("MyEmail")));
        String MyPhone = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("MyPhone")));
        String MyName = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("MyName")));
        String type = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("type")));
        String sql = "";
        sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + " select dbo.f_EBPwd(email) email,dbo.f_EBPwd(phone) phone from  t_user_applicant where email=dbo.f_DBPwd({MyEmail}) and  phone=dbo.f_DBPwd({MyPhone}) ";
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("MyEmail", MyEmail);
        paramsMap.put("MyPhone", MyPhone);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        List<Map<String, Object>> detailList = null;
        detailList = JSONObject.parseObject(res.getData().toString(), new TypeReference<List<Map<String, Object>>>() {
        });
        if (detailList.size() > 0) {//当信息都符合--更新登录时间
            String sql2 = "";
            sql2 = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                    + " update  t_user_applicant set lasttime=getdate() where email=dbo.f_DBPwd({MyEmail}) and phone=dbo.f_DBPwd({MyPhone});"
                    + " select id,dbo.f_EBPwd(email) email,dbo.f_EBPwd(phone) phone from  t_user_applicant where email=dbo.f_DBPwd({MyEmail}) and phone=dbo.f_DBPwd({MyPhone})";
            Map<String, Object> paramsMap2 = new HashMap<String, Object>();
            paramsMap2.put("$sql", sql2);
            paramsMap2.put("MyEmail", MyEmail);
            paramsMap2.put("MyPhone", MyPhone);
            paramsMap2.put("MyName", MyName);
            String result2 = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap2));
            res = JSONObject.parseObject(result2, Res.class);
        } else {//当信息不全符合
            String sql2 = "";//
            if (type.equals("phone")) {
                sql2 = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                        + " select id,dbo.f_EBPwd(email) email,dbo.f_EBPwd(phone) phone from  t_user_applicant where phone=dbo.f_DBPwd({MyPhone})";
            } else if (type.equals("email")) {
                sql2 = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                        + " select id,dbo.f_EBPwd(email) email,dbo.f_EBPwd(phone) phone from  t_user_applicant where email=dbo.f_DBPwd({MyEmail})";
            }
            Map<String, Object> paramsMap2 = new HashMap<String, Object>();
            paramsMap2.put("$sql", sql2);
            paramsMap2.put("MyEmail", MyEmail);
            paramsMap2.put("MyPhone", MyPhone);
            paramsMap2.put("MyName", MyName);
            String result2 = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap2));
            res = JSONObject.parseObject(result2, Res.class);
        }
        return res;
    }

    public Res updatelogin(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String MyPhone = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Phone")));
        String MyEmail = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Email")));
        String MyName = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Name")));
        String type = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("type")));
        String sql = "";
        if (type.equals("email")) {
            sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                    + "update  t_user_applicant set lasttime=getdate(),name={MyName},phone=dbo.f_DBPwd({MyPhone}) where email=dbo.f_DBPwd({MyEmail}) ";
        } else if (type.equals("phone")) {
            sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                    + "update  t_user_applicant set lasttime=getdate(),name={MyName},email=dbo.f_DBPwd({MyEmail}) where phone=dbo.f_DBPwd({MyPhone}); ";
        } else {
            sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                    + " insert into t_user_applicant(name,phone,email,lasttime,createtime)"
                    + " values ({MyName},dbo.f_DBPwd({MyPhone}),dbo.f_DBPwd({MyEmail}),getdate(),getdate()) "
                    + " select id from t_user_applicant where dbo.f_DBPwd({MyPhone})=phone and dbo.f_DBPwd({MyEmail})=email ";
        }
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("MyEmail", MyEmail);
        paramsMap.put("MyPhone", MyPhone);
        paramsMap.put("MyName", MyName);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }

    public Res selectArea(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();//kh.ty=0 kh.qy=1 and gkid=0
        String sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + " select fl.mc,fl.cs from yx_t_khfl fl "
                + " inner join yx_t_khb kh on kh.khfl=fl.id and kh.isqy=1 "
                + " where fl.gkid=0 and kh.ty=0 and kh.qy=1 "
                + " order by fl.dm;";
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }

    public Res selectPermissions(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String Area = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Area")));
        String sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + " select c.khid,c.khmc"
                + " from yx_t_khfl a"
                + " inner join yx_t_khb b on a.ssid=b.khfl and b.isqy=1"
                + " inner join yx_t_khb c on c.ccid+'-' like b.ccid+'-%' and c.isqy=1"
                + " where a.gkid=0 and a.cs={Area} and b.ty=0 and c.ty=0 and b.qy=1 and c.qy=1"
                + " order by c.khdm";
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("Area", Area);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }

    public Res selectPermissions2(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String Area = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Area")));
        String sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + " select c.khid,c.khmc"
                + " from yx_t_khfl a"
                + " inner join yx_t_khb c on a.id=c.khfl and c.isqy=1"
                + " where a.gkid=0 and a.cs={Area} and c.qy=1 and c.ty=0"
                + " order by c.khdm";
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("Area", Area);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }

    public Res CheckInformation(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String OldName = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("OldName")));
        String OldPhone = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("OldPhone")));
        String OldId = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("OldId")));
        String dwid = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("dwid")));
        String sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + " select id from t_user where tzid={dwid} and cname={OldName}  and dbo.f_EBPwd(sfzh)={OldId} and dbo.f_EBPwd(yddh)={OldPhone};";
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("OldName", OldName);
        paramsMap.put("OldPhone", OldPhone);
        paramsMap.put("OldId", OldId);
        paramsMap.put("dwid", dwid);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }

    public Res SaveData(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String Applicantid = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Applicantid")));
        String Ssqy = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Ssqy")));
        String Dwid = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Dwid")));
        String dwmc = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("dwmc")));
        String Applytype = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Applytype")));
        String olduserid = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("olduserid")));
        String Name = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Name")));
        String Zwmc = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Zwmc")));
        String Sfzh = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Sfzh")));
        String Yddh = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Yddh")));
        String Lxdh = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Lxdh")));
        String Email = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Email")));
        String Jurisdiction = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Jurisdiction")));
        String Remark = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Remark")));
        String ApplyImg = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("ApplyImg")));
        String id = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("user_applyid")));
        String Shbs = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Shbs")));

        String check;
        Object checkId = 0;
        if (ApplyImg.length() > 0) {
            MultipartFile file = BASE64DecodedMultipartFile
                    .base64ToMultipart(CommonUtil.convertObjToStr(params.get("file")));
            String type = ApplyImg.substring(ApplyImg.lastIndexOf("."));
            if (!type.equals(".jpg") && !type.equals(".bmp") && !type.equals(".png")&&!type.equals(".jpeg")
                    && !type.equals(".gif")) {
                res.setErrmsg("error:只能上传图片！jpg|bmp|png|gif|jpeg");
                return res;
            }
            // 构建图片新名称
            String newName = "apply_" + Name + "_" + Jurisdiction + "_" + System.currentTimeMillis() + ".jpg";

            // 判断文件夹是否存在，不存在就创建一个。
            File file_path = new File(uploadHost);
            if (!file_path.exists()) {
                file_path.mkdirs();
            }
            // 保存文件到硬盘
            res = FileUpUtil.singleFileUpload(file, newName, uploadHost);
            if (res.getErrcode() != 0) {
                res.setErrmsg("error:文件保存失败！" + uploadHost + "\\" + newName);
                return res;
            }
            String sql2 = "";
            if (Jurisdiction.length() > 0) {
                sql2 = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON;"
                        + " select ISNULL(q.id,c.id) userid,ISNULL(q.cname,c.cname) cname,dbo.f_EBPwd(ISNULL(q.yddh,c.yddh)) sj"
                        + " from ("
                        + " select isnull(f.cs,'') cs,isnull(f1.ssid,f.ssid) ssid,"
                        + " (case when CHARINDEX('sz',isnull(f.cs,''))>0 then f.ssid else ISNULL(f.id,f1.ssid) end)qyid"
                        + " from yx_t_khb k "
                        + " left join yx_t_khfl f on k.khfl=f.cs and k.isqy=0"
                        + " left join yx_t_khfl f1 on k.khfl=cast(f1.id as varchar(50)) and k.isqy=1"
                        + " where k.khid={Jurisdiction}"
                        + " )a "
                        + " left join t_user q on a.qyid=q.admin_qy and a.ssid>1 and a.cs<>'sz' "
                        + " left join ("
                        + " select u.id,u.cname,u.yddh"
                        + " FROM wlxy.dbo.fl_t_groupuser g "
                        + " inner join t_user u on g.userid=u.id "
                        + " where g.id=3"
                        + " )c on a.cs='sz' or a.ssid=1";
            } else {
                sql2 = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON;"
                        + "select * from ("
                        + " select ISNULL(q.id,c.id) userid,ISNULL(q.cname,c.cname) cname,dbo.f_EBPwd(ISNULL(q.yddh,c.yddh)) sj"
                        + " from ("
                        + " select MAX(isnull(f1.ssid,f.ssid)) ssid, max(isnull(f.cs,'')) cs,"
                        + " max((case when CHARINDEX('sz',isnull(f.cs,''))>0 then f.ssid else ISNULL(f.id,f1.ssid) end))qyid"
                        + " from yx_t_khb k "
                        + " left join yx_t_khfl f on k.khfl=f.cs and k.isqy=0"
                        + " left join yx_t_khfl f1 on k.khfl=cast(f1.id as varchar(50)) and k.isqy=1"
                        + " where f.cs={Ssqy}"
                        + " )a "
                        + " left join t_user q on a.qyid=q.admin_qy and a.ssid>1 and a.cs<>'sz' "
                        + " left join ("
                        + " select u.id,u.cname,u.yddh"
                        + " FROM wlxy.dbo.fl_t_groupuser g "
                        + " inner join t_user u on g.userid=u.id "
                        + " where g.id=3"
                        + " )c on a.cs='sz' or a.ssid=1)t group by t.userid,t.sj,t.cname;"
                        + " "
                        + " "
//                        " select ISNULL(q.id,c.id) userid,ISNULL(q.cname,c.cname) cname,dbo.f_EBPwd(ISNULL(q.yddh,c.yddh)) sj"
//                        + " from ("
//                        + " select isnull(f.cs,'') cs,isnull(f1.ssid,f.ssid) ssid,"
//                        + " (case when CHARINDEX('sz',isnull(f.cs,''))>0 then f.ssid else ISNULL(f.id,f1.ssid) end)qyid"
//                        + " from yx_t_khb k "
//                        + " left join yx_t_khfl f on k.khfl=f.cs and k.isqy=0"
//                        + " left join yx_t_khfl f1 on k.khfl=cast(f1.id as varchar(50)) and k.isqy=1"
//                        + " where k.khid='9042' "
//                        + " )a "
//                        + " left join t_user q on a.qyid=q.admin_qy and a.ssid>1 and a.cs<>'sz'  --普通单位、区县级别权限审核人"
//                        + " left join ( "
//                        + " select u.id,u.cname,u.yddh"
//                        + " FROM wlxy.dbo.fl_t_groupuser g "
//                        + " inner join t_user u on g.userid=u.id "
//                        + " where g.id=3 "
//                        + " )c on a.cs='sz' or a.ssid=1 "
                        + " ";
            }
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("$sql", sql2);
            paramsMap.put("Jurisdiction", Jurisdiction);
            paramsMap.put("Ssqy", Ssqy);
            String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));

            res = JSONObject.parseObject(result, Res.class);
            if (res.getErrcode() == 0) {
                if (res.getData() != null) {
                    List<Map<String, Object>> dataLists = JSONObject.parseObject(res.getData().toString(),
                            new TypeReference<List<Map<String, Object>>>() {
                            });
                    for(int  i=0;i<dataLists.size();i++ ){
                        if(dataLists.get(i).get("sj").toString().length()>0) {
                            check = dataLists.get(i).get("sj").toString();
                            JSONObject obj = new JSONObject();
                            check="15779779447";
                            String textMessage = "福建干部网络学院消息：" + dataLists.get(i).get("cname").toString() + "您好，您有一条由" + Name + "发来的【联络员申请】办理单据，请登录管理平台的待办事项进行操作。";
                            res =SMSGroupSendingUtil.smsSingleSend(textMessage,check);
                            if (res.getErrcode() != 0) {
                                return res;
                            }
                        }
                    }
                    if(dataLists.size()>0){

                        checkId=  dataLists.get(0).get("userid");
                    }
                }
            }
                String sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                        + " update t_user_apply set Ssqy={Ssqy},Dwid={Dwid},Applytype={Applytype},olduserid={olduserid},Name={Name},Applytime=getdate(),dwmc={dwmc},"
                        + " Sfzh=dbo.f_DBPwd({Sfzh}),Yddh=dbo.f_DBPwd({Yddh}),Lxdh=dbo.f_DBPwd({Lxdh}),Email=dbo.f_DBPwd({Email}),Zwmc={Zwmc},"
                        + " Jurisdiction={Jurisdiction},Remark={Remark},ApplyImg={ApplyImg},Shbs='2',Shrid={checkId}"
                        + " where id={id};"
                        + " "
                        + " "
                        + " insert into fl_t_flowlog(docID,senderID,sender,dt,predicate) values(-{id},-{Applicantid},{Name},getdate(),'申请')"
                        ;
                Map<String, Object> paramsMap2 = new HashMap<String, Object>();
                paramsMap2.put("$sql", sql);
                assert Applicantid != null;
                paramsMap2.put("Applicantid",Integer.parseInt(Applicantid));
                paramsMap2.put("Ssqy", Ssqy);
                paramsMap2.put("Dwid", Dwid);
                paramsMap2.put("dwmc", dwmc);
                paramsMap2.put("Applytype", Applytype);
                paramsMap2.put("olduserid", olduserid);
                paramsMap2.put("Name", Name);
                paramsMap2.put("Zwmc", Zwmc);
                paramsMap2.put("Sfzh", Sfzh);
                paramsMap2.put("Yddh", Yddh);
                paramsMap2.put("Lxdh", Lxdh);
                paramsMap2.put("Email", Email);
                paramsMap2.put("Jurisdiction", Jurisdiction);
                paramsMap2.put("Remark", Remark);
                paramsMap2.put("Shbs", Shbs);
                paramsMap2.put("ApplyImg", newName);
                assert id != null;
                paramsMap2.put("id",Integer.parseInt(id));
                paramsMap2.put("checkId", checkId);
                String result2 = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap2));
            res = JSONObject.parseObject(result2, Res.class);
            return res;
        } else {//保存
            String sql = "";
            //更新保存
            if (id.length() > 0) {
                sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                        + " update t_user_apply set Ssqy={Ssqy},Dwid={Dwid},Applytype={Applytype},olduserid={olduserid},Name={Name},Applytime=getdate(),dwmc={dwmc},"
                        + " Sfzh=dbo.f_DBPwd({Sfzh}),Yddh=dbo.f_DBPwd({Yddh}),Lxdh=dbo.f_DBPwd({Lxdh}),Email=dbo.f_DBPwd({Email}),Zwmc={Zwmc},"
                        + " Jurisdiction={Jurisdiction},Remark={Remark},Shbs='2',Shrid={checkId}"
                        + " where id={id} ;";
            } else {//新增保存保存
                sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                        + " insert into t_user_apply(Applicantid,Ssqy,Dwid,dwmc,Applytype,olduserid,Name,Sfzh,Yddh,Lxdh,Email,Zwmc,Jurisdiction,Remark,Shbs,Shrid) "
                        + " values ({Applicantid},{Ssqy},{Dwid},{dwmc},{Applytype},{olduserid},{Name}, dbo.f_DBPwd({Sfzh}), dbo.f_DBPwd({Yddh}), dbo.f_DBPwd({Lxdh}), dbo.f_DBPwd({Email}),{Zwmc},{Jurisdiction},{Remark},{Shbs},{checkId});"
                        + " select max(id) id from t_user_apply where Applicantid={Applicantid}"
                        + " ";
            }
            Map<String, Object> paramsMap = new HashMap<String, Object>();
            paramsMap.put("$sql", sql);
            paramsMap.put("id", id);
            paramsMap.put("Applicantid", Applicantid);
            paramsMap.put("Ssqy", Ssqy);
            paramsMap.put("Dwid", Dwid);
            paramsMap.put("dwmc", dwmc);
            paramsMap.put("Applytype", Applytype);
            paramsMap.put("olduserid", olduserid);
            paramsMap.put("Name", Name);
            paramsMap.put("Zwmc", Zwmc);
            paramsMap.put("Sfzh", Sfzh);
            paramsMap.put("Yddh", Yddh);
            paramsMap.put("Lxdh", Lxdh);
            paramsMap.put("Email", Email);
            paramsMap.put("Jurisdiction", Jurisdiction);
            paramsMap.put("Remark", Remark);
            paramsMap.put("Shbs", Shbs);
            paramsMap.put("checkId", checkId);
            String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
            res = JSONObject.parseObject(result, Res.class);
            return res;
        }
        // 新增申请审批日志
        //更新保存
    }

    public Res ApplicationInformation(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String Applicantid = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Applicantid")));
        String applicant_phone = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Yddh")));
        String E_mail = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Email")));
        String count0 = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("count0")));
        String count1 = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("count1")));
        String sql = "";

        sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + " select * from ("
                + " select ROW_NUMBER() over(order by a.Applytime desc) xh,case when isnull(ck.id,'0')='0' then '0' else  dbo.f_EBPwd(ck.yddh) end checksj ,b.num,a.id,a.ssqy,fl.mc mc,a.dwid, isnull(k.khmc,a.dwmc) khmc,a.Applytype,a.olduserid, t.cname oldname,dbo.f_EBPwd(t.sfzh) oldsfzh,dbo.f_EBPwd(t.yddh) oldphone,a.Name,a.Zwmc,"
                + " dbo.f_EBPwd(a.Sfzh)sfzh,dbo.f_EBPwd(a.yddh)Yddh,dbo.f_EBPwd(a.Lxdh)Lxdh,dbo.f_EBPwd(a.Email)Email,isnull(h.khmc,a.dwmc) qx_khmc,a.Jurisdiction,a.Remark,a.ApplyImg,a.Shbs,"
                + " case when a.shbs=0 then '未申请' else case when a.shbs=1 then '已审核' else case when a.shbs=2 then '申请中' else case when a.shbs=3 then '退审' end end end end appltstate, "
                + " a.Applytime,a.Shrid "
                + " from t_user_apply a "
                + " left join yx_t_khfl fl on a.Ssqy=fl.cs"
                + " left join yx_t_khb k on a.dwid=k.khid"
                + " left join yx_t_khb h on a.Jurisdiction=h.khid"
                + " left join t_user t on t.id=a.olduserid"
                + " left join t_user ck on ck.id=a.Shrid"
                + " left join (select COUNT(*) num from t_user_apply where  Applicantid={Applicantid} and({E_mail}=dbo.f_EBPwd(email) or {applicant_phone}=dbo.f_EBPwd(Yddh)))b on 1=1"
                + " where a.Applicantid={Applicantid} "
                + " )t where t.xh between {count0} and {count1} ";


        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("Applicantid", Applicantid);
        paramsMap.put("applicant_phone", applicant_phone);
        paramsMap.put("E_mail", E_mail);
        paramsMap.put("count0", count0);
        paramsMap.put("count1", count1);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }

    public Res selectUnitData(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String word = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("word")));
        String Area = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("Area")));
        String sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + " select kh.khmc,kh.khid from yx_t_khb kh"
                + " inner join yx_t_khfl fl on kh.khfl=fl.cs "
                + " where kh.ty=0 and kh.qy=1 and kh.isqy=0 and fl.cs={Area} and fl.gkid=0 and khmc like '%" + word + "%'  order by kh.khid;";
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("Area", Area);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }

    public Res deleteData(HttpServletRequest request, HttpServletResponse response, Map<String, Object> params) {
        Res res = new Res();
        String id = CommonUtil.myBase64Decode(CommonUtil.convertObjToStr(params.get("id")));
        String sql = "SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON; "
                + "delete from t_user_apply where id={id}";
        Map<String, Object> paramsMap = new HashMap<String, Object>();
        paramsMap.put("$sql", sql);
        paramsMap.put("id", id);
        String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/tlsoft", JSONObject.toJSONString(paramsMap));
        res = JSONObject.parseObject(result, Res.class);
        return res;
    }
}
