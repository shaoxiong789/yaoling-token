package com.yaoling.h5.security;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yaoling.mongodb.model.CommonSetting;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.util.Assert;

import com.qiniu.util.StringUtils;
import com.yaoling.base.YaolingMongoTemplate;
import com.yaoling.base.constant.Status;
import com.yaoling.exception.WeixinApiException;
import com.yaoling.mongodb.model.CommonUser;
import com.yaoling.mongodb.model.extenstion.CommonWeixinSetting;
import com.yaoling.web.base.WebUserDetailAdapter;
import com.yaoling.weixin.api.SCOPE;
import com.yaoling.weixin.api.WeixinApi;
import com.yaoling.weixin.api.output.WebAccessToken;

public class WeixinWebLoginProvider implements LoginProvider {
	
	private YaolingMongoTemplate mongo;
	
	public WeixinWebLoginProvider(YaolingMongoTemplate mongo) {
		this.mongo = mongo;
	}

	private static final Logger logger = LoggerFactory.getLogger(WeixinWebLoginProvider.class);

	@Override
    public void gotoLoginPage(CommonSetting setting,HttpServletRequest request, HttpServletResponse response) throws IOException {
        if(setting!=null) {
            WeixinApi api = new WeixinApi(setting.getAppid());
            try {
                //String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
                StringBuilder url = new StringBuilder();
                url.append(request.getScheme()).append("://")
                        .append(request.getServerName()).append(request.getContextPath())
                        .append("/login/success/weixin");

                response.sendRedirect(api.getWebAuthorizeCodeURL(url.toString(), SCOPE.snsapi_base));
            } catch (WeixinApiException e) {
                logger.error("Login redirect failed.", e);
			}
		}
	}

	@Override
	public UserDetails fetchUser(PreAuthenticatedAuthenticationToken token) {
		WebAccessToken accesstoken = (WebAccessToken)token.getPrincipal();
		CommonUser user = mongo.findOne(new Query(Criteria.where("openid").is(accesstoken.getOpenid())), CommonUser.class);

		WebUserDetailAdapter adapter = new WebUserDetailAdapter(user);
		Collection<GrantedAuthority> authorities = new ArrayList<>();
		authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
		adapter.setAuthorities(authorities);
		return adapter;
	}

	public Object getPrincipal(HttpServletRequest request) {
		String appid = extractAppid(request);
		if(appid!=null){
			WeixinApi api = new WeixinApi(appid);
			try {
				WebAccessToken token = api.getWebAccessToken(request.getParameter("code"));
				logger.debug("Weixin Authority Code is {}",request.getParameter("code"));
				CommonUser user = mongo.findOne(new Query(Criteria.where("openid").is(token.getOpenid())), CommonUser.class);
				CommonWeixinSetting setting = mongo.findOne(new Query(Criteria.where("appid").is(appid)),CommonWeixinSetting.class);
                if(user==null && setting !=null ){
					user = new CommonUser();
					user.setSid(setting.getId());
					user.setId(ObjectId.get().toString());
					user.setNickname( token.getOpenid() );
					user.setOpenid( token.getOpenid() );
					user.setUnionid( token.getUnionid() );
					user.setStatus( Status.ENABLE );					
					user.setPassword(" ");
					user.setCreatetime(new Date());
					mongo.save(user);
				}
				return token;
			} catch (WeixinApiException e) {
				logger.error("", e);
			}
		}
		return null;
	}

	private String extractAppid(HttpServletRequest request){

		String[] subs = request.getServerName().split("\\.");

		if(subs[0].matches("^wx[\\w]{16}$")) {
			return subs[0];
		}else {
			return null;
		}
	}

}
