package com.yaoling.h5.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.AuthenticationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Service;

import com.yaoling.base.YaolingMongoTemplate;

@Service
public class PreAuthUserDetailsService implements
		AuthenticationUserDetailsService<PreAuthenticatedAuthenticationToken> {

	static final Logger logger = LoggerFactory.getLogger(PreAuthUserDetailsService.class);
	@Autowired
	private YaolingMongoTemplate mongo;
	@Override
	public UserDetails loadUserDetails(PreAuthenticatedAuthenticationToken token) throws UsernameNotFoundException {
		logger.debug("Load User {}", token.getName());
		LoginProvider provider = LoginProviderLocator.getProvider(String.valueOf(token.getCredentials()),mongo);		
		return provider.fetchUser(token);
	}

}
