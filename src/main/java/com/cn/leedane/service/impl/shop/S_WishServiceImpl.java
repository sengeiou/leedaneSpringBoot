package com.cn.leedane.service.impl.shop;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import com.cn.leedane.handler.shop.S_ProductHandler;
import com.cn.leedane.handler.shop.S_WishHandler;
import com.cn.leedane.mapper.shop.S_WishMapper;
import com.cn.leedane.model.OperateLogBean;
import com.cn.leedane.model.UserBean;
import com.cn.leedane.model.shop.S_ProductBean;
import com.cn.leedane.model.shop.S_WishBean;
import com.cn.leedane.service.AdminRoleCheckService;
import com.cn.leedane.service.OperateLogService;
import com.cn.leedane.service.shop.S_WishService;
import com.cn.leedane.utils.CollectionUtil;
import com.cn.leedane.utils.ConstantsUtil;
import com.cn.leedane.utils.EnumUtil;
import com.cn.leedane.utils.EnumUtil.DataTableType;
import com.cn.leedane.utils.LayuiTableResponseMap;
import com.cn.leedane.utils.ResponseMap;
import com.cn.leedane.utils.SqlUtil;
import com.cn.leedane.utils.StringUtil;

/**
 * 购物商品的service的实现类
 * @author LeeDane
 * 2017年11月7日 下午4:48:03
 * version 1.0
 */
@Service("S_WishService")
public class S_WishServiceImpl extends AdminRoleCheckService implements S_WishService<S_WishBean>{
	Logger logger = Logger.getLogger(getClass());

	@Autowired
	private S_ProductHandler productHandler;
	
	@Autowired
	private S_WishHandler wishHandler;
	
	@Autowired
	private OperateLogService<OperateLogBean> operateLogService;
	
	@Autowired
	private S_WishMapper wishMapper;
	
	@Override
	public Map<String, Object> add(int productId, UserBean user,
			HttpServletRequest request) {
		
		logger.info("S_WishServiceImpl-->add():productId="+productId);
		S_ProductBean productBean = productHandler.getNormalProductBean(productId);
		if(productBean == null)
			throw new NullPointerException(EnumUtil.getResponseValue(EnumUtil.ResponseCode.该商品不存在或已被删除.value));
		
		ResponseMap message = new ResponseMap();
		
		S_WishBean wishBean = new S_WishBean();
		String returnMsg = "已成功添加到心愿单！";
		wishBean.setStatus(ConstantsUtil.STATUS_NORMAL);
		Date createTime = new Date();
		wishBean.setProductId(productId);
		wishBean.setCreateTime(createTime);
		wishBean.setCreateUserId(user.getId());
		boolean result = false;
		try{
			result = wishMapper.save(wishBean) > 0;
			if(result)
				wishHandler.deleteWishCache(user.getId());
		}catch(DuplicateKeyException e){ //唯一键约束异常不做处理
			result = true;
		}
		
		message.put("isSuccess", result);
		if(result){
			message.put("message", returnMsg);
			message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
		}else{
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.数据库保存失败.value));
			message.put("responseCode", EnumUtil.ResponseCode.数据库保存失败.value);
		}
		//保存操作日志
		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"把商品ID为", productId, "添加到心愿单:", productBean.getTitle() , "结果是：", StringUtil.getSuccessOrNoStr(result)).toString(), "add()", ConstantsUtil.STATUS_NORMAL, 0);
				
		return message.getMap();
	}
	
	@Override
	public Map<String, Object> getWishNumber(UserBean user,
			HttpServletRequest request) {
		
		logger.info("S_WishServiceImpl-->getWishNumber():user="+user.getId());
		ResponseMap message = new ResponseMap();
		
		message.put("isSuccess", true);
		message.put("message", wishHandler.getWishNumber(user.getId()));
		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
		
		return message.getMap();
	}
	
	
	@Override
	public Map<String, Object> paging(int current, int pageSize, UserBean user, HttpServletRequest request){
		logger.info("S_WishServiceImpl-->paging():current=" +current +", pageSize="+ pageSize);
		LayuiTableResponseMap message = new LayuiTableResponseMap();
		List<Map<String, Object>> rs = new ArrayList<Map<String, Object>>();
		int start = SqlUtil.getPageStart(current, pageSize, 0);
		rs = wishMapper.paging(user.getId(), ConstantsUtil.STATUS_NORMAL, start, pageSize);
		
		if(CollectionUtil.isNotEmpty(rs)){
			for(Map<String, Object> m: rs){
				int productId = StringUtil.changeObjectToInt(m.get("product_id"));
				if(productId > 0){
					S_ProductBean productBean = productHandler.getProductBean(productId);
					if(productBean != null){
						m.put("code", productBean.getCode());
						m.put("title", productBean.getTitle());
						m.put("subtitle", productBean.getSubtitle());
						m.put("digest", productBean.getDigest());
						m.put("platform", productBean.getPlatform());
						m.put("price", productBean.getPrice());
						m.put("oldPrice", productBean.getOldPrice());
						m.put("cashBackRatio", productBean.getCashBackRatio());
						BigDecimal b = new BigDecimal((productBean.getPrice() * productBean.getCashBackRatio()) / 100);  
						double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();  
						m.put("cashBack", f1);
						m.put("shopId", productBean.getShopId());
						if(productBean.getShop() != null)
							m.put("shopName", productBean.getShop().getName());
						m.put("mainImgLink", productBean.getMainImgLinks().split(";")[0]);
						m.put("category", productBean.getCategory());
					}
				}
			}
		}
		message.setCode(0);
		message.setCount(wishHandler.getWishNumber(user.getId()));
		//保存操作日志
		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"获取心愿单列表，current="+ current, ", pageSize=", pageSize).toString(), "paging()", ConstantsUtil.STATUS_NORMAL, 0);
		message.put("isSuccess", true);
		message.put("data", rs);
		return message.getMap();
	}

	@Override
	public int getWishTotal(int productId, String toDayString){
		logger.info("S_WishServiceImpl-->getWishTotal():productId=" + productId +", toDayString ="+ toDayString);
		return SqlUtil.getTotalByList(wishMapper.getTotal(DataTableType.商品心愿单.value, "where product_id = "+ productId +" and DATE_FORMAT(create_time, '%Y-%c-%d') = '" + toDayString +"'"));
	}
}