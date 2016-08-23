package com.yaoling.h5.security;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yaoling.mongodb.model.CommonSetting;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public interface LoginProvider {
	public void gotoLoginPage(CommonSetting setting, HttpServletRequest request, HttpServletResponse response) throws IOException;
	public UserDetails fetchUser(PreAuthenticatedAuthenticationToken token);
	public Object getPrincipal(HttpServletRequest request );
}
