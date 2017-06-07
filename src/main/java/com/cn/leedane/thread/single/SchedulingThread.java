package com.cn.leedane.thread.single;

import org.apache.log4j.Logger;
import org.quartz.SchedulerException;

import com.cn.leedane.model.JobManageBean;
import com.cn.leedane.task.spring.scheduling.BaseScheduling;

/**
 * 任务调度的线程
 * @author LeeDane
 * 2017年6月7日 上午10:16:01
 * version 1.0
 */
public class SchedulingThread implements Runnable{
	private Logger logger = Logger.getLogger(getClass());
	
	/**
	 * 任务调度实例
	 */
	private BaseScheduling baseScheduling;
	
	/**
	 * 计划任务信息实体
	 */
	private JobManageBean scheduleJob;
	
	public SchedulingThread(BaseScheduling baseScheduling, JobManageBean scheduleJob){
		this.baseScheduling = baseScheduling;
		this.scheduleJob = scheduleJob;
	}

	@Override
	public void run() {
		try {
			baseScheduling.execute();
		} catch (SchedulerException e) {
			logger.error("任务调度执行失败，任务名称："+ scheduleJob.getJobName() 
						+", 任务分组："+ scheduleJob.getJobGroup()
						+", 任务bean: "+ scheduleJob.getClassName()
						+", 任务描述： "+ scheduleJob.getJobDesc());
			e.printStackTrace();
			e.printStackTrace();
		}
	}

}
