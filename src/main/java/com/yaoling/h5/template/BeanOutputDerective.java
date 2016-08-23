package com.yaoling.h5.template;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import com.yaoling.annotation.Column;
import com.yaoling.annotation.Title;
import com.yaoling.base.constant.KeyValue;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;

public class BeanOutputDerective implements TemplateDirectiveModel {

	@Override	
	public void execute(Environment env, @SuppressWarnings("rawtypes") Map param, TemplateModel[] loopVar, TemplateDirectiveBody body)
			throws TemplateException, IOException {
		
		if(loopVar.length<2){
			throw new TemplateException("参数调用错误", env);
		}
		Object bean = ((StringModel)param.get("name")).getWrappedObject();
		if(bean.getClass().getSuperclass()!=null){
			Field[] fields = bean.getClass().getSuperclass().getDeclaredFields();
			
			for (Field field : fields) {
				field.setAccessible(true);
				Column col = field.getAnnotation(Column.class);
				if(col!=null){
					Title title = field.getAnnotation(Title.class);
					if(title==null) continue;
					loopVar[0] = new SimpleScalar(title.value());
	
					loopVar[1] = new SimpleScalar( getValueAsText(field, bean, env, col) );	
					body.render(env.getOut());
				}
			}	
		}
		Field[] fields = bean.getClass().getDeclaredFields();
		
		for (Field field : fields) {
			field.setAccessible(true);
			Column col = field.getAnnotation(Column.class);
			if(col!=null){
				Title title = field.getAnnotation(Title.class);
				String titleText = null;
				if(title!=null) titleText = title.value();
				if(titleText==null) titleText = col.title();
				loopVar[0] = new SimpleScalar(titleText);

				loopVar[1] = new SimpleScalar( getValueAsText(field, bean, env, col) );	
				body.render(env.getOut());
			}
		}		
	}
	
	String getValueAsText(Field field, Object bean, Environment env, Column col){
		try {
			if(field.getType().isAssignableFrom(Date.class)){
				return new SimpleDateFormat(env.getDateTimeFormat()).format(field.get(bean));
			}else if(col.options()!=null && !col.options().isEmpty()){
				Map<Integer,String> values = KeyValue.getValues(col.options());
				return values.get(field.get(bean));
			}else{
				return field.get(bean)==null?"":String.valueOf(field.get(bean));
			}
		} catch (IllegalArgumentException|IllegalAccessException e) {
			return "";
		}
	}

}
