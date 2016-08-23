package com.yaoling.h5.controller;

import com.yaoling.aes.AesException;
import com.yaoling.aes.WXBizMsgCrypt;
import com.yaoling.base.YaolingMongoTemplate;
import com.yaoling.h5.common.listener.MessageContext;
import com.yaoling.h5.common.listener.MessageListenerLocator;
import com.yaoling.mongodb.model.CommonUser;
import com.yaoling.mongodb.model.extenstion.CommonWeixinSetting;
import com.yaoling.utils.EnvHelper;
import com.yaoling.utils.ObjectConverter;
import com.yaoling.weixin.common.message.DefaultMessage;
import com.yaoling.weixin.common.message.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class WeixinMessageController {
	 
	private static Logger logger = LoggerFactory.getLogger( WeixinMessageController.class );

	@Autowired
	YaolingMongoTemplate mongo;
	
	@Autowired
	MessageListenerLocator locator;

	/**
	 * 微信公众号消息处理接口，
	 * 
	 * 注意 ：使用 Java Cryptography Extension (JCE) Unlimited Strength Jurisdiction Policy Files 8 Download
	 * 
	 * @param appid 微信公众号的 appid
	 * @param msg_signature 消息签名
	 * @param timestamp 消息时间戳
	 * @param nonce 随机字符串
	 * @param encrypt_type 消息加密类型
	 * @param message 消息体
	 * @return 返回消息对象
	 */
	@RequestMapping(value = "/{appid}/api", method = RequestMethod.POST, produces = "text/plain;charset=utf-8")
	public @ResponseBody
	String message(@PathVariable String appid,			
			@RequestParam String msg_signature, 
			@RequestParam String timestamp, 
			@RequestParam String nonce, 
			@RequestParam String encrypt_type,
			@RequestBody String message) {

		logger.debug("settingid:{}, msg_signature:{}, timestamp:{}, nonce:{}, encrypt_type:{}, com.yaoling.h5.module.message:{}",
					new Object[]{ appid,msg_signature,timestamp,nonce,encrypt_type,message });
		
		CommonWeixinSetting setting = mongo.findOne(new Query(Criteria.where("appid").is(appid)), CommonWeixinSetting.class);
		if(setting==null) return "";
		
		DefaultMessage received = ObjectConverter.xml2Obj(message, DefaultMessage.class);
		//对加密的消息体进行解密
		if(received != null && received.getEncrypt()!=null && received.getEncrypt().length()>0){

			try{
				
				WXBizMsgCrypt encypt = new WXBizMsgCrypt(EnvHelper.getComponentToken(), EnvHelper.getComponentAESKey(), EnvHelper.getComponentAppid());
				
				String mingwen = encypt.decryptMsg(msg_signature, timestamp, nonce, message);
				logger.debug("decrypt com.yaoling.h5.module.message:{}" , mingwen);

				received = ObjectConverter.xml2Obj(mingwen,DefaultMessage.class);	
			}catch(AesException e){
				logger.debug("Encrypt Type is [{}], componentToken:{}, aesKey:{},appid:{} ",
						new Object[]{encrypt_type, EnvHelper.getComponentToken(), EnvHelper.getComponentAESKey(), EnvHelper.getComponentAppid()});
				logger.error("Weixin AesException",e);
				return "";
			}		
		}

		MessageContext context = new MessageContext();
		context.setSetting(setting);
		context.setUser(mongo.findOne(new Query(Criteria.where("openid").is(received.getFromUserName())), CommonUser.class));
		Message reply  = locator.reply(received, context);

		if(logger.isDebugEnabled()){
			logger.debug( reply.toString() );
			logger.debug( reply.toEncryptString() );	
		}
		return "raw".equalsIgnoreCase(encrypt_type)? reply.toString() : reply.toEncryptString();
	}
}
