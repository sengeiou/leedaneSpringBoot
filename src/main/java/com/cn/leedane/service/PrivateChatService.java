package com.cn.leedane.service;

import com.cn.leedane.model.HttpRequestInfoBean;
import com.cn.leedane.model.IDBean;
import com.cn.leedane.model.UserBean;
import net.sf.json.JSONObject;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 私信相关service接口类
 * @author LeeDane
 * 2016年7月12日 上午11:34:30
 * Version 1.0
 */
@Transactional
public interface PrivateChatService <T extends IDBean>{

	/**
	 * 获取私信的分页列表
	 * @param jo
	 * @param user
	 * @param request
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Map<String, Object> getLimit(JSONObject jo, UserBean user, HttpRequestInfoBean request);
	
	/**
	 * 发送私信信息
	 * @param jo
	 * @param user
	 * @param request
	 * @return
	 */
	public Map<String, Object> send(JSONObject jo, UserBean user, HttpRequestInfoBean request);
}
