package com.yaoling.h5.module.common.controller;

import com.yaoling.exception.NotSignInException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.yaoling.h5.common.base.BaseController;
import com.yaoling.mongodb.model.CommonSetting;
import com.yaoling.mongodb.model.CommonUser;
import com.yaoling.mongodb.model.CommonUserAssets;
import com.yaoling.web.config.module.ModuleRegistry;
import com.yaoling.web.response.HttpResponseMessage;
import com.yaoling.web.response.ResponseUtil;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/{appid}/usercenter")
public class UserCenterHomeController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(UserCenterHomeController.class);
	
	@Autowired
	ModuleRegistry registry;

	@RequestMapping("index")
	public String index(ModelMap model,
			@PathVariable String appid)
			throws NotSignInException {
		
		CommonSetting setting = this.getSetting(appid,model);
		CommonUserAssets userAssets = mongo.findById(getCurrentUser().getId(), CommonUserAssets.class);
		if(userAssets==null){
			userAssets = new CommonUserAssets();
			userAssets.setId(this.getCurrentUser().getId());
			mongo.save( userAssets );
		}
		model.addAttribute("userAssets", userAssets);
		model.addAttribute("myModules", registry.getModules());
		model.addAttribute("currentUser",getCurrentUser());
		
		return getViewName("usercenter_index", setting.getTheme());
	}

    @RequestMapping("myqrcode")
    public String myQrcode( @PathVariable String appid)
            throws NotSignInException {
        String userid = getCurrentUser().getId();
        logger.debug("========================================================");
        logger.debug("CurrentUserId:{}", userid);

        return String.format("redirect:/%s/usercenter/qrcode?userid=%s", appid, userid);
    }

	/**
	 *
	 * @param model
	 * @param appid
	 * @param bean
	 * @return
	 * @throws NotSignInException
     */
	@RequestMapping(value="userinfo",method = RequestMethod.POST)
	public HttpResponseMessage userinfo(ModelMap model,
			@PathVariable String appid ,CommonUser bean)
			throws NotSignInException{
		CommonUser user = getCurrentUser();
		user.setTel(bean.getTel());
		user.setName(bean.getName());
		user.setQq(bean.getQq());
		mongo.save(user);
		return ResponseUtil.responseSuccess();
	}
	
	@RequestMapping(value="qrcode")
	public String qrcode(ModelMap model,
			@PathVariable String appid, @RequestParam String userid){
		CommonSetting setting = this.getSetting(appid,model);
		model.put("userid", userid);
		return getViewName("usercenter_qrcode", setting.getTheme());
	}
	

}
