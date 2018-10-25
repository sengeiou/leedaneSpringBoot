package com.cn.leedane.service.mall;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.cn.leedane.model.IDBean;
import com.cn.leedane.model.UserBean;

/**
 * 首页商品轮播service接口类
 * @author LeeDane
 * 2017年12月26日 下午2:27:29
 * version 1.0
 */
@Transactional
public interface S_HomeCarouselService <T extends IDBean>{
	/**
	 * 添加轮播
	 * @param json
	 * @param user
	 * @param request
	 * @return
	 */
	public  Map<String,Object> add(JSONObject json, UserBean user, HttpServletRequest request);
	
	/**
	 * 删除轮播
	 * @param carouselId
	 * @param user
	 * @param request
	 * @return
	 */
	public  Map<String,Object> delete(int carouselId, UserBean user, HttpServletRequest request);
	
	
	/**
	 * 获取轮播商品列表
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public  Map<String,Object> carousel();

}