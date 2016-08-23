package com.yaoling.h5.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by liangping on 2016-06-22.
 * <p>
 * 江苏摇铃网络科技有限公司，版权所有。
 * Copyright (C) 2015-2016 All Rights Reserved.
 */
public class SubDomainFilter implements Filter {

    static final Logger logger = LoggerFactory.getLogger(SubDomainFilter.class);

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        String[] subs = request.getServerName().split("\\.");

        if(subs[0].matches("^wx[\\w]{16}$") && request.getRequestURI().indexOf(subs[0])==-1) {
            String path = request.getRequestURI()
                    .replaceFirst(
                            request.getContextPath(),
                            "/".concat(subs[0]));
            logger.debug("Rewrite path: {} to {}", new Object[]{request.getRequestURI(),path});
            request.getRequestDispatcher(path).forward(request, response);
            return;
        }

        filterChain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}
