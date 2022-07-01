package com.lilanz.microservice.util;

import java.util.Date;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import com.lilanz.microservice.ConfigReader;
import com.lilanz.microservice.entity.Res;
import com.sun.mail.util.MailSSLSocketFactory;


public class MySendMail {

	/**
	 * 发送用户的用户名
	 */
	private String USERNAME = ConfigReader.get("userName");
	// 验证收件人邮箱地址
	private static String SENDADDRESS=ConfigReader.get("emailSender");//发件人邮箱
	private static String PASS=ConfigReader.get("emailPass");//发件人密码
	private static String SENDPORT=ConfigReader.get("SENDPORT");//"465";//发送端口
	private static String SENDSERVER=ConfigReader.get("SENDSERVER");//"smtp.qq.com";//发送邮箱服务器
	private static String SENDTRANSPORT="smtp";//发送的协议是简单的邮件传输协议

	public Res sendmail(String toAddress, String content, String zt ) {
		Res res=new Res(0,"发送成功！","");
		// 配置信息
		Properties pro = new Properties();
		pro.put("mail.smtp.host", SENDSERVER);
		pro.put("mail.smtp.auth", "true");
		try {
			// SSL加密
			MailSSLSocketFactory sf = null;
			sf = new MailSSLSocketFactory();
			// 设置信任主机
			sf.setTrustAllHosts(true);
			pro.put("mail.smtp.ssl.enable", "true");
			pro.put("mail.smtp.ssl.socketFactory", sf);
			// 根据邮件的会话属性构造一个发送邮件的Session，这里需要注意的是用户名那里不能加后缀否则便不是用户名
			// 还需要注意的是，这里的密码不是正常使用邮箱的登陆密码，而是客户端生成的另一个专门的授权码
			MailAuthenticator2 authenticator = new MailAuthenticator2(USERNAME,
					PASS);
			Session session = Session.getInstance(pro, authenticator);
			// 根据Session 构建邮件信息
			Message message = new MimeMessage(session);

			// 创建邮件发送者地址
			Address from = new InternetAddress(SENDADDRESS,"福建干部网络学院");
			// 设置邮件消息的发送
			message.setFrom(from);

			if (!toAddress.isEmpty()) {
				// 创建邮件的接收人地址
				Address[] to = InternetAddress.parse(toAddress);
				// 设置邮件接收人地址
				message.setRecipients(Message.RecipientType.TO, to);
				// 邮件主题
				message.setSubject(zt);
				// 邮件容器
				MimeMultipart mimeMultiPart = new MimeMultipart();
				// 设置HTML
				BodyPart bodyPart = new MimeBodyPart();
				// 邮件内容
				String htmlText = content;
				bodyPart.setContent(htmlText, "text/html;charset=utf-8");
				mimeMultiPart.addBodyPart(bodyPart);
				// 添加附件
				//			List<String> fileAddressList = new ArrayList<String>();
				//			fileAddressList.add("d:\\小车.png");
				//			if (fileAddressList != null) {
				//				BodyPart attchPart = null;
				//				for (int i = 0; i < fileAddressList.size(); i++) {
				//					if (!fileAddressList.get(i).isEmpty()) {
				//						attchPart = new MimeBodyPart();
				//						// 附件数据
				//						DataSource source = new FileDataSource(
				//								fileAddressList.get(i));
				//						// 将附件数据源添加到邮件体
				//						attchPart.setDataHandler(new DataHandler(source));
				//						// 设置附件名称为原文件
				//						attchPart.setFileName(MimeUtility.encodeText(source
				//								.getName()));
				//						mimeMultiPart.addBodyPart(attchPart);
				//					}
				//				}
				//			}
				message.setContent(mimeMultiPart);
				message.setSentDate(new Date());
				// 保存邮件
				message.saveChanges();
				// 发送邮件
				Transport.send(message);
			}
		} catch (Exception e) {
			e.printStackTrace();
			res.setErrcode(1);
			res.setErrmsg("发送失败！");
			res.setData(e.getMessage());
		}finally {
		}
		return res;
	}
}

class MailAuthenticator2 extends Authenticator {
	/**
	 * 用户名
	 */
	private String username;
	/**
	 * 密码
	 */
	private String password;

	/**
	 * 创建新的实例 MailAuthenticator.
	 *
	 * @param username
	 * @param password
	 */
	public MailAuthenticator2(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	@Override
	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(username, password);
	}

	public String getUsername() {
		return username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setUsername(String username) {
		this.username = username;
	}

}

