package com.lilanz.microservice.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class Res {
	@JSONField(ordinal=1)
	private int errcode=0;
	@JSONField(ordinal=2)
	private String errmsg;
	@JSONField(ordinal=3)
	private Object data;
	public  Res() {
		
	}
	public  Res(int errcode,String errmsg,Object data){
		this.errcode = errcode;
		this.errmsg = errmsg;
		this.data = data;
	}
	public int getErrcode() {
		return errcode;
	}
	public void setErrcode(int errcode) {
		this.errcode = errcode;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
	public Object getData() {
		return data;
	}
	public void setData(Object data) {
		this.data = data;
	}
}
