package com.yaoling.h5.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yaoling.mongodb.model.CommonSetting;
import com.yaoling.web.base.WebUserDetailAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

import com.yaoling.mongodb.model.CommonUser;

public class DevelopLoginProvider implements LoginProvider {
	
	private static final Logger logger = LoggerFactory.getLogger(DevelopLoginProvider.class);

	@Override
	public void gotoLoginPage(CommonSetting setting, HttpServletRequest request, HttpServletResponse response) throws IOException {
		
		logger.debug(request.getRequestURI());
		logger.debug(request.getContextPath());
		
		response.sendRedirect(request.getRequestURI().concat("?code=1234123"));

	}

	@Override
	public UserDetails fetchUser(PreAuthenticatedAuthenticationToken token) {
		CommonUser user = new CommonUser();
		user.setId("123456");
		user.setNickname("开发者");
		WebUserDetailAdapter adapter = new WebUserDetailAdapter(user);
		return adapter;
	}

	@Override
	public Object getPrincipal(HttpServletRequest request) {
		return null;
	}


}
