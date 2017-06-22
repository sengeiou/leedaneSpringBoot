package com.cn.leedane.service.impl.circle;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cn.leedane.exception.RE404Exception;
import com.cn.leedane.handler.UserHandler;
import com.cn.leedane.handler.circle.CircleHandler;
import com.cn.leedane.handler.circle.CircleMemberHandler;
import com.cn.leedane.mapper.circle.CircleMemberMapper;
import com.cn.leedane.model.OperateLogBean;
import com.cn.leedane.model.UserBean;
import com.cn.leedane.model.circle.CircleBean;
import com.cn.leedane.model.circle.CircleMemberBean;
import com.cn.leedane.service.AdminRoleCheckService;
import com.cn.leedane.service.OperateLogService;
import com.cn.leedane.service.circle.CircleMemberService;
import com.cn.leedane.service.circle.CircleService;
import com.cn.leedane.utils.CollectionUtil;
import com.cn.leedane.utils.ConstantsUtil;
import com.cn.leedane.utils.DateUtil;
import com.cn.leedane.utils.EnumUtil;
import com.cn.leedane.utils.JsonUtil;
import com.cn.leedane.utils.RelativeDateFormat;
import com.cn.leedane.utils.ResponseMap;
import com.cn.leedane.utils.SqlUtil;
import com.cn.leedane.utils.StringUtil;

/**
 * 圈子成员service实现类
 * @author LeeDane
 * 2017年6月20日 下午2:45:08
 * version 1.0
 */
@Service("circleMemberServiceImpl")
public class CircleMemberServiceImpl extends AdminRoleCheckService implements CircleMemberService<CircleMemberBean>{
	Logger logger = Logger.getLogger(getClass());

	@Autowired
	private CircleMemberMapper circleMemberMapper;
	
	@Autowired
	private OperateLogService<OperateLogBean> operateLogService;
	
	@Autowired
	private CircleMemberHandler circleMemberHandler;
	
	@Autowired
	private CircleHandler circleHandler;
	
	@Autowired
	private UserHandler userHandler;
	
	@Autowired
	private CircleService<CircleBean> circleService;

	@Override
	public Map<String, Object> paging(int circleId, JSONObject jsonObject, UserBean user,
			HttpServletRequest request) {
		logger.info("CircleMemberServiceImpl-->paging():jo="+jsonObject.toString());
		ResponseMap message = new ResponseMap();
		int pageSize = JsonUtil.getIntValue(jsonObject, "page_size", ConstantsUtil.DEFAULT_PAGE_SIZE); //每页的大小
		int currentIndex = JsonUtil.getIntValue(jsonObject, "current", 0); //当前的索引
		int total = JsonUtil.getIntValue(jsonObject, "total", 0); //总数
		int start = SqlUtil.getPageStart(currentIndex, pageSize, total);
		List<Map<String, Object>> rs = circleMemberMapper.paging(circleId, start, pageSize, ConstantsUtil.STATUS_NORMAL);
		if(CollectionUtil.isNotEmpty(rs)){
			int memberId = 0;			
			for(int i = 0; i < rs.size(); i++){
				memberId = StringUtil.changeObjectToInt(rs.get(i).get("member_id"));
				rs.get(i).putAll(userHandler.getBaseUserInfo(memberId));
				rs.get(i).put("create_time", RelativeDateFormat.format(DateUtil.stringToDate(StringUtil.changeNotNull(rs.get(i).get("create_time")))));
			}
			message.put("total", circleMemberMapper.getAllMembers(circleId, ConstantsUtil.STATUS_NORMAL).size());
		}
		//保存操作日志
		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"获取圈子id为"+ circleId+"成员列表").toString(), "paging()", ConstantsUtil.STATUS_NORMAL, 0);		
		message.put("message", rs);
		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
		message.put("isSuccess", true);
		return message.getMap();
	}

	@Override
	public Map<String, Object> recommend(int circleId, int memberId, JSONObject jsonObject,
			UserBean user, HttpServletRequest request) {
		logger.info("CircleMemberServiceImpl-->recommend():circleId="+circleId +", memberId="+ memberId);
		ResponseMap message = new ResponseMap();
		
		boolean recommend = JsonUtil.getBooleanValue(jsonObject, "recommend");
		CircleBean circle = circleHandler.getCircleBean(circleId);
		if(circle == null)
			throw new RE404Exception(EnumUtil.getResponseValue(EnumUtil.ResponseCode.该圈子不存在.value));
		
		boolean canAdmin = false;
		List<CircleMemberBean> members = circleMemberMapper.getMember(memberId, circleId, ConstantsUtil.STATUS_NORMAL);
		
		if(CollectionUtil.isEmpty(members))
			throw new RE404Exception(EnumUtil.getResponseValue(EnumUtil.ResponseCode.您已经离开该圈子.value));
		
		if(user.getId() == circle.getCreateUserId()){
			canAdmin = true;
    	}else{
    		canAdmin = SqlUtil.getBooleanByList(members) &&  members.get(0).getRoleType() == CircleServiceImpl.CIRCLE_MANAGER;
    	}
		if(!canAdmin){
			throw new UnauthorizedException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.没有操作权限.value));
		}
		CircleMemberBean memberBean = members.get(0);
		memberBean.setMemberRecommend(recommend);
		boolean result = circleMemberMapper.update(memberBean) > 0;
		if(result){
			message.put("isSuccess", true);
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.修改成功.value));
			message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
		}else{
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.修改失败.value));
			message.put("responseCode", EnumUtil.ResponseCode.修改失败.value);
		}
		return message.getMap();
	}
}