package com.yaoling.h5.template;

import freemarker.cache.TemplateLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by liangping on 2016-07-26.
 * <p>
 * 江苏摇铃网络科技有限公司，版权所有。
 * Copyright (C) 2015-2016 All Rights Reserved.
 */
public class HttpTemplateLoader implements TemplateLoader {

    private static Logger logger = LoggerFactory.getLogger(HttpTemplateLoader.class);

    private String baseUrl;

    @Override
    public Object findTemplateSource(String name) throws IOException {
        StringBuilder sb = new StringBuilder(baseUrl);
        sb.append(name);
        URLConnection connection = new URL(sb.toString()).openConnection();
        return connection;
    }

    @Override
    public long getLastModified(Object templateSource) {
        return ((URLConnection)templateSource).getIfModifiedSince();
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        URLConnection template = (URLConnection)templateSource;
        return new InputStreamReader(template.getInputStream());
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
