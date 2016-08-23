package com.yaoling.h5.module.common.controller;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.yaoling.h5.common.base.BaseController;
import com.yaoling.mongodb.model.CommonRegion;
import com.yaoling.web.base.Option;

@Controller
@RequestMapping("/{appid}/region")
public class RegionController extends BaseController{
	
	Logger logger = LoggerFactory.getLogger(RegionController.class);
	
	/**
	 * 省份
	 * @return
     */
	@RequestMapping(value = "/provinces",produces="application/json")
	public @ResponseBody List<Option> provinces(@PathVariable String appid){
		List<CommonRegion> province = mongo.find(new Query(Criteria.where("parentId").is("0")), CommonRegion.class);
		List<Option> options = new ArrayList<Option>();
		province.forEach(a->{
			options.add(new Option(a.getName(), a.getName()));
		});
		return options;
	}

	/**
	 * 城市 或者 县/区
	 * @param parent
	 * @return
     */
	@RequestMapping(value = {"/cities","counties"},produces="application/json")
	public @ResponseBody List<Option> cities(@PathVariable String appid,@RequestParam String parent){
		Query query = new Query(Criteria.where("name").is(parent));
		CommonRegion p = mongo.findOne(query, CommonRegion.class);
		List<CommonRegion> list = mongo.find(new Query(Criteria.where("parentId").is(p.getId())), CommonRegion.class);
		List<Option> options = new ArrayList<Option>();
		list.forEach(a->{
			options.add(new Option(a.getName(), a.getName()));
		});
		return options;
	}

}
