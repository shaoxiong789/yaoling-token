package com.yaoling.h5.security;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import com.yaoling.base.YaolingMongoTemplate;
import org.springframework.stereotype.Component;;

@Component
public class OAuthEntryPoint implements AuthenticationEntryPoint {

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException arg2) throws IOException, ServletException {

        String url = request.getRequestURL().append(request.getQueryString()==null?"":request.getQueryString()).toString();
        request.getSession().setAttribute("forward_url",url);

        StringBuilder loginpath = new StringBuilder();
        loginpath.append(request.getContextPath())
                .append("/").append(extractSid(request.getRequestURI()))
                .append("/login/all");
        response.sendRedirect(loginpath.toString());
	}

    private String extractSid(String path){

        Pattern p = Pattern.compile("([A-Za-z0-9]{24})");
        Matcher m = p.matcher(path);
        if(m.find()){
            return m.group(1);
        }else{
            return null;
        }
    }

}
