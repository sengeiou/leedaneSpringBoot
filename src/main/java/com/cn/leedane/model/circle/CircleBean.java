package com.cn.leedane.model.circle;

import org.apache.solr.client.solrj.beans.Field;

import com.cn.leedane.model.RecordTimeBean;
import com.cn.leedane.mybatis.table.annotation.Column;

/**
 * 圈子实体bean
 * @author LeeDane
 * 2017年5月30日 下午4:54:35
 * version 1.0
 */
public class CircleBean extends RecordTimeBean{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//圈子的状态：0：禁用， 1：正常
	
	/**
	 * 圈子的名称
	 */
	@Field
	private String name;
	
	/**
	 * 圈子的描述信息(介绍信息)
	 */
	@Column("circle_desc")
	@Field
	private String circleDesc;
	
	/**
	 * 圈子的头像
	 */
	@Column("circle_path")
	@Field
	private String circlePath;
	
	@Column("circle_score")
	@Field
	private float circleScore;
	
	@Column("circle_recommend")
	@Field
	private boolean circleRecommend;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCircleDesc() {
		return circleDesc;
	}

	public void setCircleDesc(String circleDesc) {
		this.circleDesc = circleDesc;
	}

	public String getCirclePath() {
		return circlePath;
	}

	public void setCirclePath(String circlePath) {
		this.circlePath = circlePath;
	}

	public float getCircleScore() {
		return circleScore;
	}

	public void setCircleScore(float circleScore) {
		this.circleScore = circleScore;
	}

	public boolean getCircleRecommend() {
		return circleRecommend;
	}

	public void setCircleRecommend(boolean circleRecommend) {
		this.circleRecommend = circleRecommend;
	}
	
	
}
