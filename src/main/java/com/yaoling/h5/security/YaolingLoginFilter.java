package com.yaoling.h5.security;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;

import com.yaoling.base.YaolingMongoTemplate;

public class YaolingLoginFilter extends
		AbstractPreAuthenticatedProcessingFilter {

	static Logger logger = LoggerFactory.getLogger(YaolingLoginFilter.class);
	
	private YaolingMongoTemplate mongo;
	
	public YaolingLoginFilter(AuthenticationManager am,YaolingMongoTemplate mongodb) {
		this.setAuthenticationManager(am);
		this.mongo = mongodb;
	}

	@Override
	protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
		logger.debug("Credentials code:{}", request.getParameter("code"));
		return request.getParameter("code");
	}

	@Override
	protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
		if(request.getParameter("code")==null){
			return null;
		}else{
			LoginProvider provider = LoginProviderLocator.getProvider(LoginProviderLocator.autoChoose(request), mongo);	
			return provider.getPrincipal(request);
		}
	}

}
