package com.yaoling.h5.security;

import javax.servlet.http.HttpServletRequest;

import com.yaoling.base.YaolingMongoTemplate;

public class LoginProviderLocator {

	public static LoginProvider getProvider(String name, YaolingMongoTemplate mongo) {
		return new WeixinWebLoginProvider(mongo);
	}
	
	static String autoChoose(HttpServletRequest request){
		return "weixin";
	}
}
