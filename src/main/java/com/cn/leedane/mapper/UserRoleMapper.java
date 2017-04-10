package com.cn.leedane.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.cn.leedane.model.RoleBean;
import com.cn.leedane.model.UserRoleBean;

/**
 * 用户角色关系的mapper接口类
 * @author LeeDane
 * 2017年4月10日 上午10:34:09
 * version 1.0
 */
public interface UserRoleMapper  extends BaseMapper<UserRoleBean>{
	
	public List<RoleBean> getUserRoleBeans(
				@Param("userId") int userId, @Param("status") int status);
}
