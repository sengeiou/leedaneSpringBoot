package com.cn.leedane.model.mall;

import com.cn.leedane.model.RecordTimeBean;
import com.cn.leedane.mybatis.table.annotation.Column;
import com.cn.leedane.mybatis.table.annotation.Table;

/**
 * 推荐人实体bean
 * @author LeeDane
 * 2017年12月11日 下午3:31:21
 * version 1.0
 */
@Table(value="t_mall_referrer")
public class S_ReferrerBean extends RecordTimeBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//推荐人的状态：0：禁用， 1：正常  2：删除

	/**
	 * 推荐人的生成码
	 */
	@Column("code")
	private String code;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
