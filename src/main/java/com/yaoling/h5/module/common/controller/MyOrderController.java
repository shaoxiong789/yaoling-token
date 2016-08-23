package com.yaoling.h5.module.common.controller;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yaoling.base.constant.KeyValue;
import com.yaoling.base.constant.OrderConstant;
import com.yaoling.base.constant.Payment;
import com.yaoling.exception.NotSignInException;
import com.yaoling.exception.WeixinApiException;
import com.yaoling.h5.common.base.AbstractUserCenterModuleController;
import com.yaoling.mongodb.model.CommonOrderPayTransaction;
import com.yaoling.mongodb.model.CommonSetting;
import com.yaoling.mongodb.model.CommonUser;
import com.yaoling.mongodb.model.extenstion.CommonWeixinSetting;
import com.yaoling.mongodb.model.parent.CommonOrder;
import com.yaoling.mongodb.model.parent.Order;
import com.yaoling.utils.EnvHelper;
import com.yaoling.utils.StringUtil;
import com.yaoling.web.base.BasicListForm;
import com.yaoling.web.response.HttpResponseMessage;
import com.yaoling.web.response.ResponseUtil;
import com.yaoling.weixin.api.WeixinPayApi;
import com.yaoling.weixin.pay.output.UnifiedOrderOutput;
import com.yaoling.weixin.pay.output.WeixinPayJsConfigOutput;

@Controller
@RequestMapping("/{appid}/usercenter")
public class MyOrderController extends AbstractUserCenterModuleController {

	static final Logger logger = LoggerFactory.getLogger(MyOrderController.class);
	
	@Override
	public String[] userCenterModuleEntry() {
		return new String[]{"我的订单","/usercenter/myorders"};
	}
	
	@RequestMapping("myorders")
	public String myorders(ModelMap model,
			@PathVariable String appid,
			@RequestParam(defaultValue="0") int status,
			@RequestParam(defaultValue="0") int page)
			throws NotSignInException{
		
		CommonSetting setting = this.getSetting(appid,model);
		CommonUser user = this.getCurrentUser();
		Query query = new Query(Criteria.where("buyer.userId").is(user.getId()));
		query.with(new PageRequest(page, 25, new Sort(Direction.DESC, "createTime")));
		List<CommonOrder> orders = mongo.find(query, CommonOrder.class);
		model.addAttribute("myorders", orders);
		
		return getViewName("usercenter_myorders", setting.getTheme());
	}
	
	@RequestMapping("myorders.json")
	public @ResponseBody HttpResponseMessage myordersJson(@PathVariable String appid, String type,
			@RequestParam(defaultValue="0") int status,BasicListForm form)
			throws NotSignInException {
		CommonUser user = this.getCurrentUser();
		Query query = new Query(Criteria.where("buyer.userId").is(user.getId()));
		if(status!=0){
			query.addCriteria(Criteria.where("status").is(status));
		}
		if(!StringUtil.isEmpty(type)){
			query.addCriteria(Criteria.where("type").is(type));
		}
		query.with( new Sort(Direction.DESC, "createTime"));
		Page<CommonOrder> page = mongo.findPageList(form, query, CommonOrder.class);
		
		return ResponseUtil.responseSuccess(page);
	}
	
	@RequestMapping("orderscount.json")
	public @ResponseBody HttpResponseMessage getOrdersCount(@PathVariable String appid, String type)
			throws NotSignInException{
		
		CommonUser user = this.getCurrentUser();
		
		Map<String,Object> countMap = new HashMap<String,Object>();
		
		List<Integer> statuslist = Arrays.asList(
				new Integer[]{
						OrderConstant.STATUS_TO_PAY,
						OrderConstant.STATUS_TO_DELIVER,
						OrderConstant.STATUS_TO_RECEIVE,
						OrderConstant.STATUS_FINISHED
						});
		statuslist.forEach((status)->{
			Query query = new Query(Criteria.where("buyer.userId").is(user.getId()));
			if(!StringUtil.isEmpty(type)){
				query.addCriteria(Criteria.where("type").is(type));
			}
			countMap.put(String.valueOf(status), mongo.count(query.addCriteria(Criteria.where("status").is(status)),CommonOrder.class));
		});
		return ResponseUtil.responseSuccess(countMap);
	}
	
	@RequestMapping("orderview")
	public String orderview(ModelMap model,	
			@PathVariable String appid,
			@RequestParam String orderid,
			@RequestParam(required=false) String type){
		
		CommonSetting setting = this.getSetting(appid,model);
		try {
			Map<String, String> config = this.initWxJSApiConfig(setting.getAppid());
			model.addAttribute("wxConfig", config );
		} catch (WeixinApiException e) {
			e.printStackTrace();
		}
		
		model.addAttribute("order", mongo.findById(orderid, Order.getOrderClass(type)));
		
		Query query = new Query(Criteria.where("orderid").is(orderid).and("status").is( CommonOrderPayTransaction.SUCCESS ));
		model.addAttribute("payment", mongo.findOne(query, CommonOrderPayTransaction.class)); 
		
		return getViewName("usercenter_orderview", setting.getTheme());
	}
	
	@RequestMapping("orderdetail.json")
	public @ResponseBody HttpResponseMessage orderDetail(@PathVariable String appid,
			@RequestParam String orderid,
			@RequestParam(required=false) String type){
		CommonSetting setting = this.getSetting(appid,null);
		Map<String,Object> map = new HashMap<String,Object>();
		Map<String, String> config = null;
		try {
			config = this.initWxJSApiConfig(setting.getAppid());
		} catch (WeixinApiException e) {
			logger.error("initWxJSApiConfig failure", e);
		}
		map.put("wxConfig", config);
		map.put("order", mongo.findById(orderid, Order.getOrderClass(type)));
		
		Query query = new Query(Criteria.where("orderid").is(orderid).and("status").is( CommonOrderPayTransaction.SUCCESS ));
		
		map.put("payment", mongo.findOne(query, CommonOrderPayTransaction.class));
		
		return  ResponseUtil.responseSuccess(map);
	}
	
	/**
	 * 获得支付方式
	 * @return
	 * @author dingShaoXiong
	 */
	@RequestMapping("paytypes.json")
	public @ResponseBody HttpResponseMessage getPayTypes(){
		Map<String,Object> map = new HashMap<>();
		map.put("type", KeyValue.getValues("kvPayMode"));
		return  ResponseUtil.responseSuccess(map);
	}
	
	
	
	@RequestMapping(value="pay/create",method=RequestMethod.POST )
	public @ResponseBody WeixinPayJsConfigOutput payCreate(ModelMap model,
			@PathVariable String appid,
			@RequestParam String orderid,
			@RequestParam String timestamp,
			@RequestParam String nonce)
			throws NotSignInException{
		
		CommonUser user = this.getCurrentUser();
		
		CommonWeixinSetting setting = mongo.findOne(new Query(Criteria.where("appid").is(appid)), CommonWeixinSetting.class);
		if(setting.getMchId()==null||setting.getMchId().isEmpty()||setting.getPayKey()==null||setting.getPayKey().isEmpty()){
			WeixinPayJsConfigOutput output = new WeixinPayJsConfigOutput();
			output.setErrcode(400102);
			output.setErrmsg("没有设置支付参数");
			return output;
		}
		
		CommonOrder order = mongo.findById( orderid, CommonOrder.class );
		
		CommonOrderPayTransaction transaction = new CommonOrderPayTransaction();
		
		transaction.setId(ObjectId.get().toString());
		transaction.setAppid(setting.getAppid());
		transaction.setAmount(order.getTotal().multiply(new BigDecimal(100)).intValue());
		transaction.setCreateTime(new Date());
		transaction.setIpAddress(this.getIPAddress());
		transaction.setMchId( setting.getMchId() );
		transaction.setOp(user.getId());
		transaction.setOpenid(user.getOpenid());
		transaction.setOrderId(orderid);
		transaction.setPayment( Payment.weixin );
		transaction.setSid(order.getSid());
		transaction.setStatus( CommonOrderPayTransaction.PAYING );
		
		mongo.save(transaction);		
		try{
			WeixinPayApi api = new WeixinPayApi();
			UnifiedOrderOutput uniOrder = api.createUnifiedOrder(transaction, String.format("%s/api/order/%s/notify", EnvHelper.getWeixinApiHost(),Payment.weixin), setting.getPayKey());	
			if("SUCCESS".equals(uniOrder.getReturn_code())){
				WeixinPayJsConfigOutput output = api.payJsConfig(setting.getAppid(), timestamp, nonce, String.format("prepay_id=%s", uniOrder.getPrepay_id()), "MD5", setting.getPayKey() );
				return output;
			}else{
				WeixinPayJsConfigOutput output = new WeixinPayJsConfigOutput();
				output.setErrcode(400102);
				output.setErrmsg(uniOrder.getReturn_msg());
				return output;
			}
		}catch(WeixinApiException e){
			WeixinPayJsConfigOutput output = new WeixinPayJsConfigOutput();
			output.setErrcode(400101);
			output.setErrmsg(e.getMessage());
			return output;
		}
	
	}
}
