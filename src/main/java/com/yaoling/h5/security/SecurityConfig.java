package com.yaoling.h5.security;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.EnableGlobalAuthentication;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.mongodb.MongoClientURI;
import com.yaoling.base.YaolingMongoTemplate;
import com.yaoling.base.constant.EnvKey;

@Configuration
@EnableWebSecurity
@EnableGlobalAuthentication
@PropertySource("classpath:/config.properties")
@ComponentScan("com.yaoling.h5.security")
public class SecurityConfig extends WebSecurityConfigurerAdapter {
	
	@Autowired
    Environment env;
	
	@Autowired
	YaolingMongoTemplate mongo;

	@Autowired
	OAuthEntryPoint oAuthEntryPoint;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {	

		http
			.csrf().disable()
			.authorizeRequests()
			.antMatchers("/*","/*/api","/api/**","/*/module/**","/module/**","/*/login/**").permitAll()
			.antMatchers("/*/usercenter/**").hasRole("USER")
		.and()
			.addFilter(new YaolingLoginFilter(this.authenticationManager(),mongo))
			.exceptionHandling().authenticationEntryPoint(oAuthEntryPoint)
		.and()
			.logout()
			.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));

	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth)
			throws Exception {

		PreAuthenticatedAuthenticationProvider provider = new PreAuthenticatedAuthenticationProvider();
		provider.setPreAuthenticatedUserDetailsService(userDetailsService);
		auth.authenticationProvider( provider );
				
	}
	
	@Autowired
	PreAuthUserDetailsService userDetailsService;
	
	@Bean
	public MongoDbFactory mongodbFactory() throws UnknownHostException{
		return new SimpleMongoDbFactory(new MongoClientURI(env.getProperty( EnvKey.HOST_DB )));
	}
	
	@Bean
	public YaolingMongoTemplate mongoTemplate() throws UnknownHostException{
		return new YaolingMongoTemplate( mongodbFactory() );
	}

}
