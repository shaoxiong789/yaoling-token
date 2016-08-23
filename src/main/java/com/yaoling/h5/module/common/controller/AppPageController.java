package com.yaoling.h5.module.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yaoling.h5.common.base.BaseController;
import com.yaoling.mongodb.model.CommonSetting;
@Controller
public class AppPageController extends BaseController {
    @RequestMapping(value="/{appid}/app/{path}")
    public String defaultTemplate(ModelMap model,@PathVariable String appid,@PathVariable String path){
    	CommonSetting setting = this.getSetting(appid, model);
        model.addAttribute("appid", appid);
        return this.getViewName("app/".concat(path), setting.getTheme());
    }
}
