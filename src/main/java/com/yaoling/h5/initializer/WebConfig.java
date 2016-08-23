package com.yaoling.h5.initializer;

import com.mongodb.MongoClientURI;
import com.yaoling.base.YaolingMongoTemplate;
import com.yaoling.base.constant.EnvKey;
import com.yaoling.h5.common.interceptor.UserRecommenderInterceptor;
import com.yaoling.h5.template.BeanOutputDerective;
import com.yaoling.h5.template.FreemarkerDatabaseTemplateLoader;
import com.yaoling.h5.template.HttpTemplateLoader;
import com.yaoling.utils.EnvHelper;
import com.yaoling.web.adapter.pay.PaymentAdapterRegistry;
import freemarker.cache.TemplateNameFormat;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.UrlResource;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@Configuration
@EnableWebMvc
@PropertySource("classpath:/config.properties")
@ComponentScan(basePackages = {"com.yaoling.h5.module", "com.yaoling.h5.controller", "com.yaoling.h5.common"})
public class WebConfig extends WebMvcConfigurerAdapter {
	
	static Logger logger = LoggerFactory.getLogger(WebConfig.class);

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		super.addResourceHandlers(registry);

		registry.addResourceHandler("/styles/**").addResourceLocations("http://static.iyaoling.com/styles/**");
	}


	@Autowired
    Environment env;

	@Bean
	public EnvHelper initEnv(){
		return new EnvHelper(this.env);
	}

	@Bean
	public FreeMarkerConfigurer freeMarkerConfigurer() {
	    FreeMarkerConfigurer fmc = new FreeMarkerConfigurer();
	    fmc.setDefaultEncoding("utf-8");
		try {
			freemarker.template.Configuration conf = fmc.createConfiguration();
			conf.setClassicCompatible(false);
			conf.setLocalizedLookup(false);
			conf.setIncompatibleImprovements(freemarker.template.Configuration.VERSION_2_3_22);
			conf.setTemplateNameFormat(TemplateNameFormat.DEFAULT_2_4_0 );
		    conf.setTemplateLoader(freeMarkerTemplateLoader());
		    conf.setTemplateExceptionHandler( TemplateExceptionHandler.HTML_DEBUG_HANDLER );
		    conf.setWhitespaceStripping(true);
		    conf.setDateFormat("yyyy-MM-dd");
		    conf.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
		    conf.setSharedVariable("beanOutput", new BeanOutputDerective());
		    fmc.setConfiguration( conf );
		} catch (IOException|TemplateException e) {
			logger.error(e.getMessage(), e);
		}
	    logger.info("initial Freemarker configuration");
	    return fmc;
	}
	@Bean
	public HttpTemplateLoader freeMarkerTemplateLoader() {
		HttpTemplateLoader loader = new HttpTemplateLoader();
		loader.setBaseUrl("http://static.wei3dian.com/");
		return loader;
	}

	@Bean 
	public FreeMarkerViewResolver viewResolver(){
		FreeMarkerViewResolver resolver = new FreeMarkerViewResolver();
		resolver.setViewClass(org.springframework.web.servlet.view.freemarker.FreeMarkerView.class);
		resolver.setSuffix(".html");
		resolver.setRequestContextAttribute("ctx");
		resolver.setContentType("text/html;charset=utf-8");
		logger.info("Setting up FreeMarkerViewResolver as viewresolver.");
		return resolver;
	}

//	@Override
//	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
//		super.configureMessageConverters(converters);
//
//		converters.add(new MappingJackson2HttpMessageConverter());
//
//		StringHttpMessageConverter string = new StringHttpMessageConverter(Charset.forName("utf-8"));
//		List<MediaType> types= new ArrayList<MediaType>();
//		types.add(MediaType.TEXT_PLAIN );
//		string.setSupportedMediaTypes(types);
//		string.setWriteAcceptCharset(true);
//		converters.add(string);
//		
//		converters.add(new MappingJackson2XmlHttpMessageConverter());
//
//	}	
	
	@Bean
	public RequestMappingHandlerAdapter converters(){
		
		List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>();
		converters.add(new MappingJackson2HttpMessageConverter());
		
		StringHttpMessageConverter string = new StringHttpMessageConverter(Charset.forName("utf-8"));
		List<MediaType> types= new ArrayList<MediaType>();
		types.add(MediaType.TEXT_PLAIN );
		string.setSupportedMediaTypes(types);
		string.setWriteAcceptCharset(true);
		converters.add(string);
		
		converters.add(new MappingJackson2XmlHttpMessageConverter());
		
		RequestMappingHandlerAdapter mappingHandler = new RequestMappingHandlerAdapter();
		mappingHandler.setMessageConverters(converters);
		return mappingHandler;
	}

	@Bean
	public MongoDbFactory mongodbFactory() throws UnknownHostException{
		logger.info(String.format("Setup Mongodb connection with parameters: %s", env.getProperty(EnvKey.HOST_DB) ));
		return new SimpleMongoDbFactory(new MongoClientURI( env.getProperty(EnvKey.HOST_DB) ));
	}
	
	@Bean
	public YaolingMongoTemplate mongoTemplate() throws UnknownHostException{
		return new YaolingMongoTemplate( mongodbFactory() );
	}

	@Bean
	public CommonsMultipartResolver multipartResolver(){
		return new CommonsMultipartResolver();
	}

	@Bean
	public PaymentAdapterRegistry paymentAdapterRegistry(){
		return new PaymentAdapterRegistry();
	}
	
	@Bean
	public UserRecommenderInterceptor userRecommenderInterceptor(){
		return new UserRecommenderInterceptor();
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		super.addInterceptors(registry);
		registry.addWebRequestInterceptor(userRecommenderInterceptor());
	}
	
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		registry.addMapping("/**")
			.allowedOrigins("*")
			.allowedMethods("GET","POST")
			.maxAge(3600);
	}

	@Bean
	public CorsFilter corsFilter() {

	    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	    CorsConfiguration config = new CorsConfiguration();
	    config.setAllowCredentials(true); // you USUALLY want this
	    config.addAllowedOrigin("*");
	    config.addAllowedHeader("*");
	    config.addAllowedMethod("GET");
	    config.addAllowedMethod("POST");
	    source.registerCorsConfiguration("/**", config);
	    return new CorsFilter(source);
	}
	
}
