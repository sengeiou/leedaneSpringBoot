package com.cn.leedane.service.circle;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.springframework.transaction.annotation.Transactional;

import com.cn.leedane.model.IDBean;
import com.cn.leedane.model.UserBean;

/**
 * 帖子的Service类
 * @author LeeDane
 * 2017年5月30日 下午8:12:53
 * version 1.0
 */
@Transactional
public interface CirclePostService <T extends IDBean>{

	/**
	 * 写帖子
	 * @param circleId
	 * @param json
	 * @param user
	 * @param request
	 * @return
	 */
	public Map<String, Object> add(int circleId, JSONObject json, UserBean user, HttpServletRequest request);
	
	/**
	 * 更新帖子
	 * @param circleId
	 * @param json
	 * @param user
	 * @param request
	 * @return
	 */
	public Map<String, Object> update(int circleId, JSONObject json, UserBean user, HttpServletRequest request);
	
	
}
