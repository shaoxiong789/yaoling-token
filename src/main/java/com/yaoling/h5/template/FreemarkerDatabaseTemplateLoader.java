package com.yaoling.h5.template;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.yaoling.mongodb.model.CommonTemplate;

import freemarker.cache.TemplateLoader;

public class FreemarkerDatabaseTemplateLoader implements TemplateLoader {
	
	private static Logger logger = LoggerFactory.getLogger(FreemarkerDatabaseTemplateLoader.class);
	
	private MongoTemplate mongo; 

	/**
	 * Closes the template source, releasing any resources held that are only required for reading the template and/or its metadata.
	 */
	@Override
	public void closeTemplateSource(Object templateSource) throws IOException {
		//do nothing.
	}
	/**
	 * Finds the template in the backing storage and returns an object that identifies the storage location where the template can be loaded from.
	 */
	@Override
	public Object findTemplateSource(String templateSource) throws IOException {
		Query query = new Query(Criteria.where("name").is(templateSource));
		CommonTemplate template = mongo.findOne(query, CommonTemplate.class);
		logger.debug(String.format("find template name [%s] template: %s ", templateSource, template ));
		return template==null?new CommonTemplate():template;
	}

	/**
	 * Returns the time of last modification of the specified template source.
	 */
	@Override
	public long getLastModified(Object templateSource) {
		return ((CommonTemplate)templateSource).getUpdateTime().getTime();
	}

	/**
	 * Returns the character stream of a template represented by the specified template source.
	 */
	@Override
	public Reader getReader(Object templateSource, String encoding) throws IOException {
		String content = ((CommonTemplate)templateSource).getContent();
		return new StringReader(content==null?"":content);
	}
	/**
	 * @return the mongo
	 */
	public MongoTemplate getMongo() {
		return mongo;
	}
	/**
	 * @param mongo the mongo to set
	 */
	public void setMongo(MongoTemplate mongo) {
		this.mongo = mongo;
	}

}
