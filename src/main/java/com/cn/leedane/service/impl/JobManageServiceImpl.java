package com.cn.leedane.service.impl;

import com.cn.leedane.exception.OperateException;
import com.cn.leedane.handler.JobHandler;
import com.cn.leedane.handler.UserHandler;
import com.cn.leedane.mapper.JobManageMapper;
import com.cn.leedane.model.HttpRequestInfoBean;
import com.cn.leedane.model.JobManageBean;
import com.cn.leedane.model.OperateLogBean;
import com.cn.leedane.model.UserBean;
import com.cn.leedane.service.JobManageService;
import com.cn.leedane.service.OperateLogService;
import com.cn.leedane.utils.*;
import com.cn.leedane.utils.EnumUtil.DataTableType;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 具体任务的管理(增删改查)
 * @author LeeDane
 * 2017年6月5日 下午2:32:28
 * version 1.0
 */
@Service("jobManageService")
public class JobManageServiceImpl implements JobManageService<JobManageBean>{
	private Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private JobManageMapper jobManageMapper;
	
	@Autowired
	private OperateLogService<OperateLogBean> operateLogService;

	@Autowired
	private UserHandler userHandler;

	@Autowired
	private JobHandler jobHandler;
	
	@Override
    public Map<String, Object> add(JSONObject jo, UserBean user, HttpRequestInfoBean request) throws SchedulerException{
		logger.info("JobManageServiceImpl-->add():jo="+jo.toString());
		ResponseMap message = new ResponseMap();
		SqlUtil sqlUtil = new SqlUtil();
		JobManageBean jobManageBean = (JobManageBean) sqlUtil.getBean(jo, JobManageBean.class);
		if(jobManageBean == null)
			throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.资源不存在.value));
		
		if(StringUtil.isNull(jobManageBean.getJobName()) 
				|| StringUtil.isNull(jobManageBean.getJobGroup())
				|| StringUtil.isNull(jobManageBean.getCronExpression()))
			throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.参数不存在或为空.value));
		
		//校验是否存在
		if(SqlUtil.getTotalByList(jobManageMapper.isExist(jobManageBean.getJobName(), jobManageBean.getJobGroup(), ConstantsUtil.STATUS_NORMAL)) > 0){
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.添加的记录已经存在.value));
			message.put("responseCode", EnumUtil.ResponseCode.添加的记录已经存在.value);
			return message.getMap();
		}
		jobManageBean.setCreateUserId(user.getId());
		jobManageBean.setCreateTime(new Date());
        boolean result = jobManageMapper.save(jobManageBean) > 0;
		
		if(!result){
			throw new OperateException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.数据库保存失败.value));
		}
		//只有正常状态才去启动任务
    	if(jobManageBean.getStatus() == ConstantsUtil.STATUS_NORMAL){
			jobHandler.start(jobManageBean);
    	}
        message.put("success", true);
		message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.操作成功.value));
		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
		return message.getMap();
    }
    
    @Override
    public Map<String, Object> update(JSONObject jo, UserBean user, HttpRequestInfoBean request) throws SchedulerException{
    	logger.info("JobManageServiceImpl-->update():jo="+jo.toString());
		ResponseMap message = new ResponseMap();

		long jid = JsonUtil.getLongValue(jo, "id", 0);
		JobManageBean oldJobManageBean = null;
		if(jid < 1 || (oldJobManageBean = jobManageMapper.findById(JobManageBean.class, jid)) == null)
			throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.参数不存在或为空.value));
		
    	SqlUtil sqlUtil = new SqlUtil();
    	JobManageBean jobManageBean = (JobManageBean) sqlUtil.getUpdateBean(jo, oldJobManageBean);
		if(jobManageBean == null){
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.根据请求参数构建实体对象失败.value));
			message.put("responseCode", EnumUtil.ResponseCode.根据请求参数构建实体对象失败.value);
			return message.getMap();
		}
		
		if(StringUtil.isNull(jobManageBean.getJobName()) 
				|| StringUtil.isNull(jobManageBean.getJobGroup())
				|| StringUtil.isNull(jobManageBean.getCronExpression()))
			throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.参数不存在或为空.value));
	
    	
		jobManageBean.setModifyUserId(user.getId());
		jobManageBean.setModifyTime(new Date());
        boolean result = jobManageMapper.update(jobManageBean) > 0;
        if(result){
        	//停掉旧的任务
			jobHandler.stop(oldJobManageBean.getJobName(), oldJobManageBean.getJobGroup());

            if(jobManageBean.getStatus() == ConstantsUtil.STATUS_NORMAL){
            	//启动新的任务
				jobHandler.start(jobManageBean);
            }else{
				//停掉新的任务
				jobHandler.stop(jobManageBean.getJobName(), jobManageBean.getJobGroup());
            }
            
        	message.put("success", true);
    		message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.修改成功.value));
    		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
        }else{
    		message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.修改失败.value));
    		message.put("responseCode", EnumUtil.ResponseCode.修改失败.value);
        }
		return message.getMap();
    }
    
    @Override
    public Map<String, Object> delete(long jid, HttpRequestInfoBean request) throws SchedulerException{
    	logger.info("JobManageServiceImpl-->delete():jid="+jid);
		ResponseMap message = new ResponseMap();
		
		JobManageBean jobManageBean = null;
		if(jid < 1 || (jobManageBean = jobManageMapper.findById(JobManageBean.class, jid)) == null)
			throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.参数不存在或为空.value));
        
        boolean result = jobManageMapper.deleteById(JobManageBean.class, jid) > 0;
        if(result){
			//停掉任务
			jobHandler.stop(jobManageBean.getJobName(), jobManageBean.getJobGroup());
        	message.put("success", true);
    		message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.删除成功.value));
    		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
        }else{
    		message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.删除失败.value));
    		message.put("responseCode", EnumUtil.ResponseCode.删除失败.value);
        }
		return message.getMap();
    }
    
    @Override
	public Map<String, Object> paging(JSONObject jsonObject, UserBean user,
			HttpRequestInfoBean request) {
		logger.info("JobManageServiceImpl-->paging():jo="+jsonObject.toString());
		ResponseMap message = new ResponseMap();
		int pageSize = JsonUtil.getIntValue(jsonObject, "page_size", ConstantsUtil.DEFAULT_PAGE_SIZE); //每页的大小
		int currentIndex = JsonUtil.getIntValue(jsonObject, "current", 0); //当前的索引
		int total = JsonUtil.getIntValue(jsonObject, "total", 0); //总数
		int start = SqlUtil.getPageStart(currentIndex, pageSize, total);
		List<Map<String, Object>> rs = jobManageMapper.paging(start, pageSize, ConstantsUtil.STATUS_NORMAL);

		if(CollectionUtil.isNotEmpty(rs)){
			int createUserId = 0;			
			for(int i = 0; i < rs.size(); i++){
				createUserId = StringUtil.changeObjectToInt(rs.get(i).get("create_user_id"));
				rs.get(i).putAll(userHandler.getBaseUserInfo(createUserId));
			}
		}
		message.put("total", SqlUtil.getTotalByList(jobManageMapper.getTotal(DataTableType.任务.value, null)));
		//保存操作日志
//		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"获取任务列表").toString(), "paging()", ConstantsUtil.STATUS_NORMAL, 0);
		message.put("message", rs);
		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
		message.put("success", true);
		return message.getMap();
	}

	@Override
	public Map<String, Object> deletes(String jobids, UserBean user,
			HttpRequestInfoBean request) throws SchedulerException {
		logger.info("JobManageServiceImpl-->deletes():jobids="+ jobids);
		ResponseMap message = new ResponseMap();
		if(StringUtil.isNull(jobids))
			throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.参数不存在或为空.value));
		
		String[] pmidArray = jobids.split(",");
		
		boolean result = false;
		for(int i = 0; i < pmidArray.length; i++){
			
			JobManageBean jobManageBean = jobManageMapper.findById(JobManageBean.class, StringUtil.changeObjectToInt(pmidArray[i]));
			if(jobManageBean == null)
				throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.资源不存在.value));
			
			result = jobManageMapper.deleteById(JobManageBean.class, jobManageBean.getId()) > 0;
			//停掉新的任务
			jobHandler.stop(jobManageBean.getJobName(), jobManageBean.getJobGroup());
		}
		
		if(result){
			//保存操作日志
			operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"批量删除任务,jobids="+ jobids).toString(), "deletes()", ConstantsUtil.STATUS_NORMAL, EnumUtil.LogOperateType.内部接口.value);
			message.put("success", result);
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.操作成功.value));
			message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
		}else{
			throw new Error();
		}
		return message.getMap();
	}
}
