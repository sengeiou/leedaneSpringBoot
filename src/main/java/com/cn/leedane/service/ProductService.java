package com.cn.leedane.service;

import org.springframework.transaction.annotation.Transactional;

import com.cn.leedane.model.IDBean;

/**
 * 商品service接口类
 * @author LeeDane
 * 2016年7月12日 上午11:34:39
 * Version 1.0
 */
@Transactional
public interface ProductService <T extends IDBean>{

	
}
