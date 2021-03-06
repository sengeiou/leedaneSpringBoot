package com.cn.leedane.task.spring;

import java.util.Date;

import org.apache.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.cn.leedane.model.JobManageBean;
import com.cn.leedane.springboot.SpringUtil;
import com.cn.leedane.task.spring.scheduling.AbstractScheduling;
import com.cn.leedane.thread.ThreadUtil;
import com.cn.leedane.thread.single.SchedulingThread;
import com.cn.leedane.utils.DateUtil;
import com.cn.leedane.utils.StringUtil;

/**
 * 定时任务运行工厂类
 * @author LeeDane
 * 2017年6月5日 下午2:30:25
 * version 1.0
 */
@DisallowConcurrentExecution
public class QuartzJobFactory implements Job{
	
	private Logger logger = Logger.getLogger(getClass());
	
	@Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
         final JobManageBean scheduleJob = (JobManageBean)context.getMergedJobDataMap().get("scheduleJob");
         if(StringUtil.isNotNull(scheduleJob.getClassName())){
        	 logger.info(DateUtil.DateToString(new Date())+ "--->任务名称 = [" + scheduleJob.getJobName() + "]");
        	 Object obj = SpringUtil.getBean(scheduleJob.getClassName());
        	 if(null != obj){
        		new ThreadUtil().singleTask(new SchedulingThread((AbstractScheduling) obj, scheduleJob));
        	 } else{
        		 logger.error("任务调度执行失败, 原因是没有找到对应的任务实现bean!");
        	 }
         }
    }
}
