package com.yaoling.h5.controller;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yaoling.base.constant.Payment;
import com.yaoling.h5.common.base.BaseController;
import com.yaoling.mongodb.model.CommonOrderPayTransaction;
import com.yaoling.mongodb.model.extenstion.CommonWeixinSetting;
import com.yaoling.mongodb.model.parent.CommonOrder;
import com.yaoling.mongodb.model.parent.Order;
import com.yaoling.web.adapter.pay.PayNotification;
import com.yaoling.web.adapter.pay.PayNotifyTask;
import com.yaoling.web.adapter.pay.PaymentAdapter;
import com.yaoling.web.adapter.pay.PaymentAdapterRegistry;

@Controller
@RequestMapping("api")
public class ApiController extends BaseController {
 
	final static Logger logger = LoggerFactory.getLogger(ApiController.class);
 
	@CrossOrigin
	@RequestMapping("/weixin/appid")
	public @ResponseBody String weixinappid(@RequestParam String sid) {
		Query q = new Query(Criteria.where("sid").is( sid ));
		CommonWeixinSetting wxsetting = mongo.findOne(q, CommonWeixinSetting.class );
		return wxsetting==null?"":wxsetting.getAppid();
	}
	 
	@Autowired
	PaymentAdapterRegistry registry; 
	
	/**
	 * 支付结果异步通知
	 * @param payment
	 * @param request
     * @return
     */
	@RequestMapping(value="/order/{payment}/notify", method=RequestMethod.POST )
	public @ResponseBody String orderNotify(@PathVariable Payment payment,HttpServletRequest request ) {

		PaymentAdapter adapter = registry.createAdapter(payment);
		logger.debug("Recieved a notification by using {} {}", new Object[]{payment, adapter});
		try{
			PayNotification notification = adapter.buildNotification(request);
			CommonOrderPayTransaction pt = mongo.findById(notification.getOut_trade_no(), CommonOrderPayTransaction.class);
			CommonWeixinSetting setting = mongo.findById(pt.getSid(), CommonWeixinSetting.class);
			if(adapter.verify(notification, setting.getPayKey())) {
				adapter.doNotifyTask(new PayNotifyTask() {
					@Override
					public void run() {
						if (pt.getStatus()!=CommonOrderPayTransaction.SUCCESS ) {
							Update update = new Update();
							update.set("payTime", new Date());
							update.set("payStatus", Order.PAY_STATUS_PAID);
							update.set("status", Order.STATUS_TO_DELIVER);
							mongo.updateFirst(new Query(Criteria.where("id").is(pt.getOrderId())), update,
									CommonOrder.class);

							pt.setPayNo(notification.getTransaction_id());
							pt.setPayTime(new Date());
							pt.setStatus(CommonOrderPayTransaction.SUCCESS);
							mongo.save(pt);
						}
					}
				});
				logger.debug("Recieved a notification for order {} {}", new Object[]{pt.getOrderId(), pt.getPayNo()});
				return adapter.success();
			}
		}catch(NullPointerException e){
			logger.error("Pay notification proccess failed");
		}
		return adapter.fail("signature failed or format unmatched!");
	}
}
