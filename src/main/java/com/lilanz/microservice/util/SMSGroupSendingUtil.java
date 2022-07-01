package com.lilanz.microservice.util;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lilanz.microservice.ConfigReader;
import com.lilanz.microservice.MyRequest;
import com.lilanz.microservice.entity.Res;
import org.apache.tomcat.util.codec.binary.Base64;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;


/**
 * 依赖 Md5Util.java 和 HttpUrlConUtil.java，以及对应的前端html ：L1Home/smsGroupSend.html
 * 
 * 具体调用方法：可 参考下面main 方法。
 */
public class SMSGroupSendingUtil {
	/* 可操纵参数 开始 */
	// sameMysl：isSameSmsContent=true短信内容相同时,含义：将待发送列表切片，每片5000人。
	// 注意：绝对不能大于5000，这是中国移动公司MAS接口限制的。
	private static int sameMysl = 4999;
	// multiplyMysl：isSameSmsContent=false给每个人短信内有用户参数时，含义：将待发送列表切片，每片1000人。
	// 注意：绝对不能大于1000.0，这是中国移动公司MAS接口限制的。
	private static int multiplyMysl = 999;
	// 字段别名CNAME：往短信模板中可添加${cname} ,代表用户姓名。${}内部必须与这个字符串完全一致。
	// 还可定义其他别名，从在下列方法 ：queryToSendListByPage中定义。
	private static String CNAME = "cname";
	/* 可操纵参数 结束 */

	private static String GATEWALL = ConfigReader.get("gatewall");// "http://10.9.1.71:8900/";
	private static String DATASERVICE = "base-datams-tlsoft";
	private static SimpleDateFormat sdfMs = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

	/**
	 * 用户名：wlxy 用户密码：Fsa@87890399 ip地址：172.27.65.97 所属分组：中共福建省委党校 协议：https
	 * 
	 * 短信类型：普通短信 可用签名：【中共福建省委党校】 签名编码：CzLXQUYKb 服务代码：106573008836 是否精确匹配：否
	 * 订购关系编码：9005289645 名单属性：黑名单
	 */

	private static String wlxyEcName = "中共福建省委党校";// 企业名称。
	private static String wlxyApId = "wlxy";// 接口账号用户名。
	private static String wlxySecretKey = "Fsa@87890399";// 接口账号密码。
	private static String wlxySign = "CzLXQUYKb";// 签名编码。在云MAS平台『管理』→『接口管理』→『短信接入用户管理』获取。
	private static String wlxyAddSerial = "";// 扩展码。依据申请开户的服务代码匹配类型而定，如为精确匹配，此项填写空字符串（""）；如为模糊匹配，此项可填写空字符串或自定义的扩展码，注：服务代码加扩展码总长度不能超过20位。

//	public static void main(String[] args) {
//		// 这个main方法需要事先选择“接收短信的人列表”。
//
//		// 传进来的参数：
//		String fail = ""; //默认可设为""，代表给待发送列表的人发短信。当 fail=="1",代表给上次发送失败的人发短信。
//		String mycontent = "尊敬的 A先生你好，我们诚挚地邀请您去B旅游。";
//		String userid = "2", username = "测试", userssid = "1";// 是session中保存的登陆的管理员的id,姓名，所属id.
//		// 【注意：接收短信的人列表 不是传参进来，而是事先已经保存到数据库的px_t_masstext表 中，本接口再查找出来】
//
//		// 执行：
//		Res res = smsGroupSend(fail, mycontent, userid, username, userssid);
//		System.out.println("短信发送返回的结果是：" + JSONObject.toJSONString(res));
//	}

	/**
	 * 通用发送方法：发送{短信内容：smsContent}给{接收人：rsvMobiles}
	 * 
	 * @params smsContent 短信内容。
	 * @params rsvMobiles 接收人的手机号码，可多个，字符串用英文逗号隔开，最多不超过5000个。
	 */
	public static Res smsSingleSend(String smsContent, String rsvMobiles) {
		Map<String, Object> reqParams = new HashMap<String, Object>();
		reqParams.put("content", smsContent);
		reqParams.put("mobiles", rsvMobiles);
		return sameContentSubmit(reqParams);// 短信发送。
	}

	/**
	 * 【被调用的接口方法】 fail=="1",代表给上次发送失败的人发短信；否则是给未发送的人发短信。
	 * mycontent--短信内容，里面包含${CNAME}，则isSameSmsContent==false,否则isSameSmsContent==true;
	 * userid, username, userssid是session中保存的登陆的管理员的id,姓名，所属id.
	 */
	public static Res smsGroupSend(String fail, String mycontent, String userid, String username, String userssid) {
		Res res = null, smsRes = null, saveRes = null;
		String dbSafeContent = handleStrPreDb(mycontent);// db字符串 脱敏过滤

		boolean isSameSmsContent = true;
		if (mycontent.indexOf("${" + CNAME + "}") >= 0) {
			isSameSmsContent = false;
		}
		// 一次性查询 待发送、或发送不成功列表。
		res = getToSendList(userssid, fail);
		if (res.getErrcode() == 0) {
			List<Map<String, Object>> dt1 = JSONObject.parseObject(CommonUtil.convertObjToStr(res.getData()),
					new TypeReference<List<Map<String, Object>>>() {
					});
			if (dt1.size() > 0) {
				JSONArray jarrResult = new JSONArray();// 初始化 存放结果的容器

				Map<String, Object> reqParams = new HashMap<String, Object>();
				if (isSameSmsContent) {
					reqParams.put("content", mycontent);
				}

				// 切片：（5000或1000）条短信/每片
				int mysl2 = isSameSmsContent ? sameMysl : multiplyMysl;
				List<List<Map<String, Object>>> slices = subListByCount(dt1, mysl2);
				int pieces_num = slices.size();

				List<Map<String, Object>> slicedLists_i = null;
				for (int i = 0; i < pieces_num; i++) {
					slicedLists_i = slices.get(i);

					if (isSameSmsContent) {
						/* 构建手机号码字符串 开始 */
						String myMobiles = "";
						for (int j = 0; j < slicedLists_i.size(); j++) {
							if ("".equals(myMobiles)) {
								myMobiles += slicedLists_i.get(j).get("mobile").toString();
							} else {
								myMobiles += "," + slicedLists_i.get(j).get("mobile").toString();
							}
						}
						reqParams.put("mobiles", myMobiles);
						/* 构建手机号码字符串 结束 */

						JSONObject job_slice = new JSONObject();
						job_slice.put("order", "第" + (i * mysl2 + 1) + " - " + (i + 1) * mysl2 + "条");

						long t1 = System.currentTimeMillis();
						String start = sdfMs.format(new Date());

						smsRes = sameContentSubmit(reqParams);// 短信发送。
						job_slice.put("smsRes", smsRes);

						long t2 = System.currentTimeMillis();
						String end = sdfMs.format(new Date());
						job_slice.put("start", start);
						job_slice.put("end", end);
						job_slice.put("time", t2 - t1);

						/* 保存入数据库 开始 */
						int sendState = (smsRes != null && smsRes.getErrcode() == 0) ? 1 : 2;
						Map<String, Object> paramsMap = new HashMap<String, Object>();
						paramsMap.put("masContent", dbSafeContent);// mycontent db脱敏
						paramsMap.put("sendState", sendState);
						paramsMap.put("errmsg", smsRes.getErrmsg());
						paramsMap.put("userid", userid);
						paramsMap.put("username", username);
						paramsMap.put("userssid", userssid);
						paramsMap.put("myMobiles", myMobiles);
						saveRes = updateSendState(paramsMap);
						job_slice.put("saveRes", saveRes);
						/* 保存入数据库 结束 */

						jarrResult.add(job_slice);

					} else {
						Map<String, String> contentMap = new HashMap<String, String>();
						String rsvMobile = "", rsvContent = "";
						Map<String, Object> rsvParamsMap = null;
						String myMobiles = "";
						for (int j = 0; j < slicedLists_i.size(); j++) {
							rsvParamsMap = slicedLists_i.get(j);// 接收人的个人信息，如姓名等。
							rsvContent = processTemplate(mycontent, rsvParamsMap);// 拼接出最终内容：（传进来的模板字符串）+（每个接收人的个人信息）
							rsvMobile = rsvParamsMap.get("mobile").toString();
							contentMap.put(rsvMobile, rsvContent);// 接收人手机号 ==》 他对应的短信内容
							if ("".equals(myMobiles)) {
								myMobiles += slicedLists_i.get(j).get("mobile").toString();
							} else {
								myMobiles += "," + slicedLists_i.get(j).get("mobile").toString();
							}
						}
						reqParams.put("content", contentMap);
						reqParams.put("mobiles", "");

						JSONObject job_slice = new JSONObject();
						job_slice.put("order", "第" + (i * mysl2 + 1) + " - " + (i + 1) * mysl2 + "条");

						long t1 = System.currentTimeMillis();
						String start = sdfMs.format(new Date());

						smsRes = multiplySumbit(reqParams);// 短信发送。
						job_slice.put("smsRes", smsRes);

						long t2 = System.currentTimeMillis();
						String end = sdfMs.format(new Date());
						job_slice.put("start", start);
						job_slice.put("end", end);
						job_slice.put("time", t2 - t1);

						/* 保存到数据库 开始 */
						int sendState = (smsRes != null && smsRes.getErrcode() == 0) ? 1 : 2;
						Map<String, Object> paramsMap = new HashMap<String, Object>();
						paramsMap.put("masContent", dbSafeContent);// mycontent db脱敏
						paramsMap.put("sendState", sendState);
						paramsMap.put("errmsg", smsRes.getErrmsg());
						paramsMap.put("userid", userid);
						paramsMap.put("username", username);
						paramsMap.put("userssid", userssid);
						paramsMap.put("myMobiles", myMobiles);
						saveRes = updateSendState(paramsMap);
						job_slice.put("saveRes", saveRes);
						/* 保存入数据库 结束 */

						jarrResult.add(job_slice);
					}
					res = new Res(0, "ok", jarrResult);
				}
			} else {
				res = new Res(1, "没有短信接收人！", "");
			}
		}
		return res;
	}

	// 用户输入内容保存如数据库前，要进行脱敏化。
	private static String handleStrPreDb(String str) {
		str = str.replace("--", "——");
		str = str.replace("\'", "’");
		str = str.replace("\"", "”");
		return str;
	}

	// 一次性获取待发送,发送不成功的短信接收人列表。id正序，id小的先发送。
	private static Res getToSendList(String tzid, String fail) {
		Res res = null;
		int sendState = "1".equals(fail) ? 2 : 0;
		// 从临时表“待发送短信的接收人列表”
		// 中查询“手机号，接收人姓名”.这里字段别名cname对应前端${CNAME},mobile对应后面方法126行与149行，不可以随意改变。
		Map<String, Object> paramsMap = new HashMap<String, Object>();
		paramsMap.put("tzid", tzid);
		paramsMap.put("sendState", sendState);
		String MySql = " select m.receiveUsername " + CNAME
				+ ",m.receivePhonenumber mobile from px_t_masstext m with(nolock) "
				+ " where m.tzid = {tzid} and m.sendState={sendState} and convert(varchar(10), getdate(), 23)=convert(varchar(10), m.createTime, 23)"
				+ " order by m.id desc;";
		paramsMap.put("$sql", MySql);
		String result = HttpUrlConUtil.httpPostURlCon(GATEWALL + DATASERVICE + "/safeExecSQL/wlxy",
				JSONObject.toJSONString(paramsMap));
		res = JSONObject.parseObject(result, Res.class);
		return res;
	}

	/**
	 * 分隔数组 根据每段数量分段
	 *
	 * @param mylist 被分隔的数组
	 * @param count  每段数量
	 * @return
	 */
	private static List<List<Map<String, Object>>> subListByCount(List<Map<String, Object>> mylist, int count) {
		List<List<Map<String, Object>>> result = new ArrayList<>();
		int size = mylist.size();// 数据长度
		if (size > 0 && count > 0) {
			int segments = size / count;// 商
			/**
			 * 1.整除， 即分隔为segments 段; 2.除不尽，即分隔为segments + 1 段
			 */
			segments = size % count == 0 ? segments : segments + 1; // 段数
			List<Map<String, Object>> cutList = null;// 每段List
			for (int i = 0; i < segments; i++) {
				if (i == segments - 1) {
					cutList = mylist.subList(count * i, size);
				} else {
					cutList = mylist.subList(count * i, count * (i + 1));
				}
				result.add(cutList);
			}
		} else {
			result.add(mylist);
		}
		return result;
	}

	// 1.1. 1对多，相同内容群发多个接收人，一次最多5000个。
	private static Res sameContentSubmit(Map<String, Object> reqParams) {
		String myMobiles = reqParams.get("mobiles").toString();// 格式：“13800138000,13800138001,13800138002”
		String mycontent = reqParams.get("content").toString();
		// 1.构建参数
		SendReq sendReq = new SendReq();
		sendReq.setEcName(wlxyEcName);
		sendReq.setApId(wlxyApId);
		sendReq.setSecretKey(wlxySecretKey);
		sendReq.setSign(wlxySign);
		sendReq.setAddSerial(wlxyAddSerial);
		sendReq.setMobiles(myMobiles);
		sendReq.setContent(mycontent);

		String myMac = wlxyEcName + wlxyApId + wlxySecretKey + myMobiles + mycontent + wlxySign + wlxyAddSerial;
		sendReq.setMac(Md5Util.MD5(myMac).toLowerCase());// md5加密并要求小写。
		String reqText = JSON.toJSONString(sendReq);

		Res res = null;
		try {
			// 1.加密
			String encode = Base64.encodeBase64String(reqText.getBytes("UTF-8"));
			// 2.发送请求
			res = sendSMSRequest(1, encode);
		} catch (UnsupportedEncodingException e) {
			res = new Res(1, e.getMessage(), "UTF-8 格式编码错误");
		}
		return res;
	}

	/*
	 * 1.2.多对多，不同内容分别发送多个接收人，一次最多1000个。mobile字段无效，content字段整合了电话号码。
	 * content格式必须这样："content" :
	 * "{ \"13800138000\":\"移动改变生活。\", \"13800138001\":\"神州行，我看行。\" }"
	 */
	private static Res multiplySumbit(Map<String, Object> reqParams) {
		String myMobiles = "";// mobile字段无效
		String mycontent = JSON.toJSONString(reqParams.get("content"));// json 格式化。

		// 1.构建参数
		SendReq sendReq = new SendReq();
		sendReq.setEcName(wlxyEcName);
		sendReq.setApId(wlxyApId);
		sendReq.setSecretKey(wlxySecretKey);
		sendReq.setSign(wlxySign);
		sendReq.setAddSerial(wlxyAddSerial);
		sendReq.setMobiles("");
		sendReq.setContent(mycontent);// 多对多发送短信中，字段“mobiles”无效，手机号在字段“content”中。

		// 构建字段mac
		String myMac = wlxyEcName + wlxyApId + wlxySecretKey + myMobiles + mycontent + wlxySign + wlxyAddSerial;
		sendReq.setMac(Md5Util.MD5(myMac).toLowerCase());// md5加密并要求小写。

		String reqText = JSON.toJSONString(sendReq);

		Res res = null;
		try {
			// 1.加密
			String encode = Base64.encodeBase64String(reqText.getBytes("UTF-8"));
			// 2.发送请求
			res = sendSMSRequest(2, encode);
		} catch (UnsupportedEncodingException e) {
			res = new Res(1, e.getMessage(), "UTF-8 格式编码错误");
		}
		return res;
	}

	// 消息返回实体SendRes ==》Res
	private static Res getResFromSendRes(SendRes sendRes) {
		Res res = new Res();

		boolean success = sendRes.isSuccess();
		res.setErrcode(success ? 0 : 1);
		res.setData(sendRes.getMsgGroup());

		String errmsg = "";
		String rspcod = sendRes.getRspcod();
		switch (rspcod) {
		case "IllegalMac":
			errmsg = "mac校验不通过";
			break;
		case "IllegalSignId":
			errmsg = "无效的签名编码";
			break;
		case "InvalidMessage":
			errmsg = "非法消息，请求数据解析失败";
			break;
		case "InvalidUsrOrPwd":
			errmsg = "非法用户名/密码";
			break;
		case "NoSignId":
			errmsg = "未匹配到对应的签名信息";
			break;
		case "success":
			errmsg = "发送成功";
			break;
		case "TooManyMobiles":
			errmsg = "手机号数量超限";
			break;
		default:
			errmsg = "发送失败";
			break;
		}
		res.setErrmsg(errmsg);
		return res;
	}

	// 0.发送请求
	private static Res sendSMSRequest(int urlCode, String encode) {
		SendRes sendRes = null;
		String url = "";
		if (urlCode == 1 || urlCode == 2) {
			url = "https://112.35.10.201:28888/sms/submit";
		} else if (urlCode == 3) {
			url = "https://112.35.10.201:28888/sms/tmpsubmit";
		}

		String result = HttpUrlConUtil.httpsPostURlCon(url, encode);
		sendRes = JSONObject.parseObject(result, SendRes.class);

		return getResFromSendRes(sendRes);
	}

	/**
	 * 字符串渲染模板
	 * 
	 * @param template 模版
	 * @param params   参数
	 * @return
	 */
	private static String processTemplate(String template, Map<String, Object> params) {
		if (template == null || params == null)
			return null;
		StringBuffer sb = new StringBuffer();
		Matcher m = Pattern.compile("\\$\\{\\w+\\}").matcher(template);
		while (m.find()) {
			String param = m.group();
			Object value = params.get(param.substring(2, param.length() - 1));
			m.appendReplacement(sb, value == null ? "" : value.toString());
		}
		m.appendTail(sb);
		return sb.toString();
	}

	private static Res updateSendState(Map<String, Object> paramsMap) {
		Res res = null;
//		paramsMap.put("masContent", masContent);
//		paramsMap.put("sendState", sendState);
//		paramsMap.put("errmsg", errmsg);
//		paramsMap.put("userid", userid);
//		paramsMap.put("username", username);
//		paramsMap.put("userssid", userssid);
//		paramsMap.put("myMobiles", myMobiles);
		String myMobiles = paramsMap.get("myMobiles").toString();

		String MySql = " SET TRANSACTION ISOLATION LEVEL READ UNCOMMITTED; SET NOCOUNT ON;"
				+ " update px_t_masstext set masContent= {masContent},sendState={sendState},rtnMsg={errmsg},SendTime=getdate(),SendUserid={userid},SendUsername={username}"
				+ " where tzid = {userssid} and sendState=0 and convert(varchar(10), getdate(), 23)=convert(varchar(10), createTime, 23) and receivePhonenumber in ("
				+ myMobiles + ");";
		paramsMap.put("$sql", MySql);
		String result = MyRequest.sendPostUTF8(GATEWALL + DATASERVICE + "/safeExecSQL/wlxy",
				JSONObject.toJSONString(paramsMap));
		res = JSONObject.parseObject(result, Res.class);
		return res;
	}

}

//1.一对多普通短信，多对多普通短信
class SendReq {
	private String ecName; // 集团客户名称
	private String apId; // 用户名
	private String secretKey; // 密码
	private String mobiles; // 手机号码逗号分隔。(如“18137282928,18137282922,18137282923”)
	private String content; // 短信内容。如content中存在双引号，请务必使用转义符\在报文中进行转义（使用JSON转换工具转换会自动增加转义符），否则会导致服务端解析报文异常。
	private String sign; // 网关签名编码，必填，签名编码在中国移动集团开通帐号后分配，可以在云MAS网页端管理子系统-SMS接口管理功能中下载。
	private String addSerial; // 扩展码，根据向移动公司申请的通道填写，如果申请的精确匹配通道，则填写空字符串("")，否则添加移动公司允许的扩展码。
	private String mac; // 参数校验序列，生成方法：将ecName、apId、secretKey、templateId、mobiles、params、sign、addSerial按序拼接（无间隔符），通过MD5（32位小写）计算出的值。

	public String getEcName() {
		return ecName;
	}

	public void setEcName(String ecName) {
		this.ecName = ecName;
	}

	public String getApId() {
		return apId;
	}

	public void setApId(String apId) {
		this.apId = apId;
	}

	public String getMobiles() {
		return mobiles;
	}

	public void setMobiles(String mobiles) {
		this.mobiles = mobiles;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getSign() {
		return sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getAddSerial() {
		return addSerial;
	}

	public void setAddSerial(String addSerial) {
		this.addSerial = addSerial;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getSecretKey() {
		return secretKey;
	}

	public void setSecretKey(String secretKey) {
		this.secretKey = secretKey;
	}
}

/**
 * 发送短信响应实体
 */
class SendRes {
	private String rspcod; // 响应状态码
	private String msgGroup; // 消息批次号，由云MAS平台生成，用于验证短信提交报告和状态报告的一致性（取值msgGroup）注:如果数据验证不通过msgGroup为空
	private boolean success; // 数据校验结果

//	rspcod	说明
//	IllegalMac	mac校验不通过。
//	IllegalSignId	无效的签名编码。
//	InvalidMessage	非法消息，请求数据解析失败。
//	InvalidUsrOrPwd	非法用户名/密码。
//	NoSignId	未匹配到对应的签名信息。
//	success	数据验证通过。
//	TooManyMobiles	手机号数量超限（>5000），应≤5000。

	public String getRspcod() {
		return rspcod;
	}

	public void setRspcod(String rspcod) {
		this.rspcod = rspcod;
	}

	public String getMsgGroup() {
		return msgGroup;
	}

	public void setMsgGroup(String msgGroup) {
		this.msgGroup = msgGroup;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
