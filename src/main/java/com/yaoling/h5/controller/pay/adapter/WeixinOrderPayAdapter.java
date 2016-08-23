package com.yaoling.h5.controller.pay.adapter;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.yaoling.base.constant.Payment;
import com.yaoling.exception.WeixinApiException;
import com.yaoling.utils.ObjectConverter;
import com.yaoling.utils.SignHelper;
import com.yaoling.web.adapter.pay.AbstractPaymentAdapter;
import com.yaoling.web.adapter.pay.PayNotification;
import com.yaoling.web.adapter.pay.PayTransaction;
import com.yaoling.web.adapter.pay.PaymentAdapter;
import com.yaoling.weixin.pay.output.PayNotify;

@Component
public class WeixinOrderPayAdapter extends AbstractPaymentAdapter implements PaymentAdapter{
	
	static final Logger logger = LoggerFactory.getLogger(WeixinOrderPayAdapter.class);

	/**
	 * 跳转支付页面
	 * @param response
	 * @param transaction
	 */
	@Override
	public String toPay(HttpServletResponse response, final PayTransaction transaction) {
		//do nothing. 没用这种方式发起
		return null;
	}

	/**
	 * 生成支付二维码
	 * @param transaction
	 * @return
     */
	@Override
	public String createPayBill(PayTransaction transaction) {
		//do nothing. 没用这种方式发起		
		return "";
	}

	@Override
	public Payment name() {
		return Payment.weixin;
	}

	@Override
	public PayNotification buildNotification(HttpServletRequest request) {		
		try {
			return ObjectConverter.xmlStreamToBean(request.getInputStream(), PayNotify.class);
		} catch (WeixinApiException|IOException e) {
			return null;
		}
	}

	@Override
	public boolean verify(PayNotification notification, String payKey) {
		try {
			String local = SignHelper.sign(notification, payKey );
			logger.debug("Notification verify: local {} = remote {}", new Object[]{local, notification.getSign()});
			return local.equals( notification.getSign());
		} catch (WeixinApiException e) {
			logger.error("signature failed", e);
			return false;
		}
	}

	@Override
	public String success() {
		return "<xml><return_code>SUCCESS</return_code><return_msg>OK</return_msg></xml>";
	}

	@Override
	public String fail(String errorMsg) {
		return String.format("<xml><return_code>FAIL</return_code><return_msg>%s</return_msg></xml>", errorMsg);
	}

}
