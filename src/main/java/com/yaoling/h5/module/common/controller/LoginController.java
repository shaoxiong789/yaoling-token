package com.yaoling.h5.module.common.controller;

import com.yaoling.h5.common.base.BaseController;
import com.yaoling.h5.security.LoginProvider;
import com.yaoling.h5.security.LoginProviderLocator;
import com.yaoling.mongodb.model.CommonSetting;
import com.yaoling.utils.StringUtil;
import com.yaoling.web.base.WebUserDetailAdapter;
import com.yaoling.web.response.HttpResponseMessage;
import com.yaoling.web.response.ResponseUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liangping on 2016-06-21.
 * <p>
 * 江苏摇铃网络科技有限公司，版权所有。
 * Copyright (C) 2015-2016 All Rights Reserved.
 */
@Controller
public class LoginController extends BaseController {
	
	static Logger logger = LoggerFactory.getLogger(LoginController.class);

    @RequestMapping( value="/session",produces="application/json")
    public @ResponseBody Map<String, Object> session(HttpSession session)
            throws IOException{

    	Map<String, Object> data = new HashMap<>();
    	data.put("session id:", session.getId());
    	if(session.getAttribute("time")==null){
    		session.setAttribute("time", new Date());
    	}
    	data.put("time", session.getAttribute("time"));

        return data;
    }
    
    @RequestMapping( value="/{appid}/login")
    public  String login(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable String appid,
                         @RequestParam(required = false) String url)
            throws IOException{

        if(!StringUtil.isEmpty(url)){
            request.getSession().setAttribute("forward_url", url);
        }
        //暂时全部跳转到微信支付。
        CommonSetting setting = this.getSetting(appid,null);
        LoginProvider provider = LoginProviderLocator.getProvider("weixin", mongo);
        provider.gotoLoginPage(setting, request, response);

        return "login";
    }

    @RequestMapping( value="/{appid}/login/{provide}")
    public String loginProvider(HttpServletRequest request,
                         HttpServletResponse response,
                         @PathVariable String appid, String provide) throws IOException {
        CommonSetting setting = this.getSetting(appid,null);
        LoginProvider provider = LoginProviderLocator.getProvider(provide, mongo);
        provider.gotoLoginPage(setting, request, response);
        return "login";
    }

    @RequestMapping( value="/{appid}/login/success/{provide}")
    public String success(HttpServletRequest request,
                          HttpServletResponse response,
                          @PathVariable String appid, String provide) throws IOException {

        HttpSession session = request.getSession();
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        logger.debug("Authentication:{}", auth==null?"no user found":auth.getPrincipal());

        WebUserDetailAdapter principal = (WebUserDetailAdapter) (auth.getPrincipal());        
        session.setAttribute("currentUser", principal.getUser());
        
        if(session.getAttribute("forward_url")!=null){
            //response.sendRedirect(String.valueOf(session.getAttribute("forward_url")));
            return "redirect:".concat(String.valueOf(session.getAttribute("forward_url")));
        }

        return "redirect:/usercenter/index";
    }
}
