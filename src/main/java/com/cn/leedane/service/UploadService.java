package com.cn.leedane.service;

import com.cn.leedane.model.HttpRequestInfoBean;
import com.cn.leedane.model.IDBean;
import com.cn.leedane.model.UploadBean;
import com.cn.leedane.model.UserBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
/**
 * 断点续传Service类
 * @author LeeDane
 * 2016年7月12日 上午11:36:23
 * Version 1.0
 */
@Transactional
public interface UploadService<T extends IDBean>{

	/**
	 * 添加断点续传(已经存在直接返回true)
	 * @param upload
	 * @param user
	 * @param request
	 * @return
	 */
	public boolean addUpload(UploadBean upload, UserBean user, HttpRequestInfoBean request);

	

	/**
	 * 删除单个断点续传记录(数据库和服务器断点片段)
	 * @param upload
	 * @param user
	 * @param request
	 * @return
	 */
	public boolean cancel(UploadBean upload, UserBean user, HttpRequestInfoBean request) ;



	/**
	 * 获取当个文件的断点续传记录列表，按照从小到大排列
	 * @param tableUuid
	 * @param tableName
	 * @param order
	 * @param user
	 * @param request
	 * @return
	 */
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	public List<Map<String, Object>> getOneUpload(String tableUuid, String tableName, int order, UserBean user,
			HttpRequestInfoBean request);
}
