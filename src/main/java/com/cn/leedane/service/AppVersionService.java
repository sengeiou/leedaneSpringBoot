package com.cn.leedane.service;

import com.cn.leedane.model.HttpRequestInfoBean;
import com.cn.leedane.model.IDBean;
import com.cn.leedane.model.UserBean;
import net.sf.json.JSONObject;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * App版本管理service接口类
 * @author LeeDane
 * 2016年7月12日 上午11:28:51
 * Version 1.0
 */
@Transactional
public interface AppVersionService <T extends IDBean>{
	/**
	 * 获取最新版本号
	 * @param jo 格式"{'version','1.0.0'}"
	 * @param user
	 * @param request
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Map<String, Object> getNewest(JSONObject jo, UserBean user, HttpRequestInfoBean request);
	
	/**
	 * 获取数据库中上传的最新版本
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public List<Map<String, Object>> getNewestVersion();

	/**
	 * 获取app版本列表信息
	 * @param json
	 * @param user
	 * @param request
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public Map<String, Object> paging(JSONObject json, UserBean user, HttpRequestInfoBean request);	
	
	/**
	 * 获取数据库中上传的全部版本
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public List<Map<String, Object>> getAllVersions();
}
