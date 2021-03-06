package com.cn.leedane.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cn.leedane.model.AttentionBean;
import com.cn.leedane.service.AttentionService;
import com.cn.leedane.utils.ControllerBaseNameUtil;
import com.cn.leedane.utils.EnumUtil;
import com.cn.leedane.utils.JsonUtil;
import com.cn.leedane.utils.ResponseMap;

@RestController
@RequestMapping(value = ControllerBaseNameUtil.at)
public class AttentionController extends BaseController{	
	private Logger logger = Logger.getLogger(getClass());
	
	//关注service
	@Autowired
	private AttentionService<AttentionBean> attentionService;

	/**
	 * 添加关注
	 * @return
	 */
	@RequestMapping(value = "/attention", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> add(Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();
		
		checkRoleOrPermission(model, request);
		message.putAll(attentionService.addAttention(getJsonFromMessage(message), getUserFromMessage(message), getHttpRequestInfo(request)));
		return message.getMap();
	}
	
	/**
	 * 取消关注
	 * @return
	 */
	@RequestMapping(value = "/attention", method = RequestMethod.DELETE, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> delete(Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request)){
			return message.getMap();
		}
		
		checkRoleOrPermission(model, request);
		message.putAll(attentionService.deleteAttention(getJsonFromMessage(message), getUserFromMessage(message), getHttpRequestInfo(request)));
		return message.getMap();
	}
	
	/**
	 * 获取关注列表
	 * @return
	 */
	@RequestMapping(value = "/attentions", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> paging(Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();
		
		checkRoleOrPermission(model, request);
		//为了安全，必须是登录用户才能操作
		long toUserId = JsonUtil.getLongValue(getJsonFromMessage(message), "toUserId");
		if(toUserId != getUserFromMessage(message).getId()){
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.没有操作权限.value));
			message.put("responseCode", EnumUtil.ResponseCode.没有操作权限.value);
			return message.getMap();
		}
		List<Map<String, Object>> result= attentionService.getLimit(getJsonFromMessage(message), getUserFromMessage(message), getHttpRequestInfo(request));
		logger.info("获得关注的数量：" +result.size());
		message.put("success", true);
		message.put("message", result);
		return message.getMap();
	}
}
