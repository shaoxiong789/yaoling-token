package com.yaoling.h5.module.common.advisor;

import com.yaoling.exception.NotSignInException;
import com.yaoling.web.response.HttpResponseMessage;
import com.yaoling.web.response.ResponseUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@ControllerAdvice
public class GlobleExceptionHandler {
	
	static final Logger logger = LoggerFactory.getLogger(GlobleExceptionHandler.class);

	@ExceptionHandler
	public @ResponseBody
	HttpResponseMessage handleBusinessException(NotSignInException ex) {
		logger.error(ex.getMessage());
		return ResponseUtil.responseFail("not signin", HttpResponseMessage.NOT_SIGNIN);
	}

}
