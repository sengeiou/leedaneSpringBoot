package com.cn.leedane.controller.clock;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.cn.leedane.controller.BaseController;
import com.cn.leedane.model.clock.ClockInBean;
import com.cn.leedane.service.clock.ClockInService;
import com.cn.leedane.utils.ControllerBaseNameUtil;
import com.cn.leedane.utils.ResponseMap;

/**
 * 打卡接口controller
 * @author LeeDane
 * 2018年9月11日 下午9:52:13
 * version 1.0
 */
@RestController
@RequestMapping(value = ControllerBaseNameUtil.clockIn)
public class ClockInController extends BaseController{

	protected final Log log = LogFactory.getLog(getClass());

	@Autowired
	private ClockInService<ClockInBean> clockInService;

	/**
	 * 获取指定日期的打卡任务的列表
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> dateClocks(Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();
		
		checkRoleOrPermission(model, request);
		message.putAll(clockInService.add(getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}

	/**
	 * 获取指定日期的打卡任务的列表
	 * @param clockId
	 * @param date
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{clockId}/{date}/ins", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> clockIns(@PathVariable("clockId") long clockId, @PathVariable("date") String date, Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(model, request);
		message.putAll(clockInService.clockIns(clockId, date, getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}

	/**
	 * 获取指定日期的用户打卡详情
	 * @param clockId
	 * @param toUserId
	 * @param date
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{clockId}/user/{toUserId}/{date}", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> userClockIns(@PathVariable("clockId") long clockId, @PathVariable("toUserId") long toUserId, @PathVariable("date") String date, Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(model, request);
		message.putAll(clockInService.getUserClockIn(clockId, toUserId, date, getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}

	/**
	 * 通知管理员
	 * @param clockId
	 * @param clockInId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{clockId}/{clockInId}/notification", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> userClockInNotification(@PathVariable("clockId") long clockId, @PathVariable("clockInId") long clockInId, Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(model, request);
		message.putAll(clockInService.userClockInNotification(clockId, clockInId, getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}

	/**
	 * 打卡审核
	 * @param clockId
	 * @param clockInId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{clockId}/{clockInId}/check", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> userClockInCheck(@PathVariable("clockId") long clockId, @PathVariable("clockInId") long clockInId, Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(model, request);
		message.putAll(clockInService.clockInCheck(clockId, clockInId, getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}

	/**
	 * 添加位置信息
	 * @param clockId
	 * @param clockInId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{clockId}/{clockInId}/add/location", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> clockInAddLocation(@PathVariable("clockId") long clockId, @PathVariable("clockInId") long clockInId, Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(model, request);
		message.putAll(clockInService.clockInAddLocation(clockId, clockInId, getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}

	/**
	 * 添加图片信息
	 * @param clockId
	 * @param clockInId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{clockId}/{clockInId}/add/image", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> clockInAddImage(@PathVariable("clockId") long clockId, @PathVariable("clockInId") long clockInId, Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(model, request);
		message.putAll(clockInService.clockInAddImage(clockId, clockInId, getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}

	/**
	 * 删除打卡的资源
	 * @param clockId
	 * @param clockInId
	 * @param model
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/{clockId}/{clockInId}/{resourceId}", method = RequestMethod.DELETE, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> deleteResource(@PathVariable("clockId") long clockId, @PathVariable("clockInId") long clockInId, @PathVariable("resourceId") long resourceId, Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();

		checkRoleOrPermission(model, request);
		message.putAll(clockInService.deleteResource(clockId, clockInId, resourceId, getJsonFromMessage(message), getMustLoginUserFromShiro(), getHttpRequestInfo(request)));
		return message.getMap();
	}
}
