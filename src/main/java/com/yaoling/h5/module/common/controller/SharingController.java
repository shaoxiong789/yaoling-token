package com.yaoling.h5.module.common.controller;

import com.yaoling.h5.common.base.BaseController;
import com.yaoling.mongodb.model.CommonRegion;
import com.yaoling.mongodb.model.CommonUser;
import com.yaoling.utils.StringUtil;
import com.yaoling.web.base.Option;
import com.yaoling.web.response.HttpResponseMessage;
import com.yaoling.web.response.ResponseUtil;
import com.yaoling.weixin.api.WeixinApi;
import com.yaoling.weixin.api.output.QRTicket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liangping on 2016-06-24.
 * <p>
 * 江苏摇铃网络科技有限公司，版权所有。
 * Copyright (C) 2015-2016 All Rights Reserved.
 */
@Controller
public class SharingController extends BaseController{

    static Logger logger = LoggerFactory.getLogger(SharingController.class);

    /**
     * 用户分享二维码接口
     * @param appid
     * @return
     */
    @RequestMapping(value = "/{appid}/sharing/qrcode")
    public @ResponseBody
    HttpResponseMessage qrcode(@PathVariable String appid, @RequestParam String userid){

        CommonUser user = mongo.findById(userid, CommonUser.class);
        if(StringUtil.isEmpty(user.getQRCodeUrl())) {
            try {
                StringBuilder url = new StringBuilder("https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=");
                WeixinApi api = new WeixinApi(appid);
                QRTicket ticket = api.createQRLimitTicket("user:".concat(userid));
                if(ticket!=null) {
                    user.setQRCodeUrl(url.append(ticket.getTicket()).toString());
                }
                mongo.save(user);
                return ResponseUtil.responseSuccess(user);
            }catch(Exception e) {
                logger.error("获取二维码失败", e);
            }
        }else{
        	return ResponseUtil.responseSuccess(user);
        }
        return ResponseUtil.responseFail("获取二维码失败",HttpResponseMessage.NOT_SIGNIN);
    }
}
