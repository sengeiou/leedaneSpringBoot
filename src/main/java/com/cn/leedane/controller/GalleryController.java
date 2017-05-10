package com.cn.leedane.controller;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cn.leedane.model.GalleryBean;
import com.cn.leedane.service.GalleryService;
import com.cn.leedane.utils.ControllerBaseNameUtil;
import com.cn.leedane.utils.ResponseMap;

@RestController
@RequestMapping(value = ControllerBaseNameUtil.gl)
public class GalleryController extends BaseController{

	private Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private GalleryService<GalleryBean> galleryService;
	
	
	/**
	 * 添加网络链接到图库
	 * @return
	 * @throws Exception 
	 */
	@RequestMapping(value = "/photo", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> addLink(HttpServletRequest request) throws Exception{
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request)){
			return message.getMap();
		}
		
		checkRoleOrPermission(request);
		message.putAll(galleryService.addLink(getJsonFromMessage(message), getUserFromMessage(message), request));
		return message.getMap();
		
	}
	
	/**
	 * 获取图库的照片列表
	 * @return
	 */
	@RequestMapping(value = "/photos", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"}) 
	public Map<String, Object> paging(@RequestParam(value="pageSize", required = false) int pageSize,
			@RequestParam(value="last_id", required = false) int lastId,
			@RequestParam(value="first_id", required = false) int firstId,
			@RequestParam(value="method", required = true) String method,
			HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(request);
		
		List<Map<String, Object>> result= galleryService.getGalleryByLimit(getJsonFromMessage(message), getUserFromMessage(message), request);
		logger.info("获得图库的数量：" +result.size());
		message.put("isSuccess", true);
		message.put("message", result);
		return message.getMap();
	}
	

	/**
	 * 移出图库
	 * @return
	 */
	@RequestMapping(value = "/photo/{gid}", method = RequestMethod.DELETE, produces = {"application/json;charset=UTF-8"}) 
	public Map<String, Object> delete(HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();
		
		checkRoleOrPermission(request);
		message.putAll(galleryService.delete(getJsonFromMessage(message), getUserFromMessage(message), request));
		return message.getMap();
	}
}
