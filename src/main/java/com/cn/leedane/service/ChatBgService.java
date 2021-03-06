package com.cn.leedane.service;

import com.cn.leedane.model.HttpRequestInfoBean;
import com.cn.leedane.model.IDBean;
import com.cn.leedane.model.UserBean;
import net.sf.json.JSONObject;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 聊天背景相关service接口类
 * @author LeeDane
 * 2016年7月12日 上午11:31:09
 * Version 1.0
 */
@Transactional
public interface ChatBgService <T extends IDBean>{
	
	
	/**
	 * 获取聊天背景的分页列表
	 * @param jo
	 * @param user
	 * @param request
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Map<String, Object> paging(JSONObject jo, UserBean user, HttpRequestInfoBean request);

	/**
	 * 发布聊天背景
	 * @param jo
	 * @param user
	 * @param request
	 * @return
	 */
	public Map<String, Object> publish(JSONObject jo, UserBean user, HttpRequestInfoBean request);

	/**
	 * 下载付费版本的聊天背景
	 * @param jo
	 * @param user
	 * @param request
	 * @return
	 */
	public Map<String, Object> addChatBg(JSONObject jo, UserBean user, HttpRequestInfoBean request);
}
