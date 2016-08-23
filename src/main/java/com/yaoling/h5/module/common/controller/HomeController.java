package com.yaoling.h5.module.common.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yaoling.exception.NotSignInException;
import com.yaoling.exception.WeixinApiException;
import com.yaoling.h5.common.base.BaseController;
import com.yaoling.mongodb.model.CommonSetting;
import com.yaoling.mongodb.model.CommonUser;
import com.yaoling.utils.EnvHelper;
import com.yaoling.web.response.HttpResponseMessage;
import com.yaoling.web.response.ResponseUtil;

@Controller
public class HomeController extends BaseController{

    @RequestMapping(value="/{appid}")
    public String index(ModelMap model,@PathVariable String appid){
		
		CommonSetting setting = this.getSetting(appid,model);
		try {
			Map<String, String> config = this.initWxJSApiConfig(setting.getAppid());
			model.addAttribute("wxConfig", config );
		} catch (WeixinApiException e) {
			e.printStackTrace();
		}
        model.addAttribute("upload_server", EnvHelper.getUploadHost());
        model.addAttribute("statics", EnvHelper.getStaticHost());
        model.addAttribute("appid",appid);
        
        return this.getViewName("index","hinapolean");
    }

    @RequestMapping(value="/{appid}/module/{method}")
    public String defaultTemplate(ModelMap model,@PathVariable String appid,@PathVariable String method){
    	model.addAttribute("upload_server", EnvHelper.getUploadHost());
        model.addAttribute("statics", EnvHelper.getStaticHost());
        model.addAttribute("appid", appid);
        return method;
    }
    
    @RequestMapping("/{appid}/user/user_current")
    public @ResponseBody HttpResponseMessage getUser(@PathVariable String appid)
			throws NotSignInException {
    	CommonUser user  = this.getCurrentUser();
    	return ResponseUtil.responseSuccess(user);
    }

//    @RequestMapping( value="/{appid}/login")
//    public  String accredit(ModelMap model, @PathVariable String appid, @RequestParam String reUrl){
//    	return "redirect:"+reUrl;
//    }

}
