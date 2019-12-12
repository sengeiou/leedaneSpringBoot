package com.cn.leedane.service.impl.mall;

import com.cn.leedane.handler.mall.S_ProductHandler;
import com.cn.leedane.mall.jingdong.api.DetailBigFieldApi;
import com.cn.leedane.mall.model.SearchProductRequest;
import com.cn.leedane.mall.model.SearchProductResult;
import com.cn.leedane.mall.model.ProductPromotionLinkBean;
import com.cn.leedane.mall.pdd.api.DetailSimpleApi;
import com.cn.leedane.mall.taobao.api.AlimamaShareLink;
import com.cn.leedane.mall.taobao.api.SearchMaterialApi;
import com.cn.leedane.mall.taobao.api.SearchProductApi;
import com.cn.leedane.mapper.mall.S_ProductMapper;
import com.cn.leedane.model.HttpRequestInfoBean;
import com.cn.leedane.model.IDBean;
import com.cn.leedane.model.OperateLogBean;
import com.cn.leedane.model.UserBean;
import com.cn.leedane.model.mall.S_PlatformProductBean;
import com.cn.leedane.service.OperateLogService;
import com.cn.leedane.service.mall.MallRoleCheckService;
import com.cn.leedane.service.mall.MallSearchService;
import com.cn.leedane.service.mall.S_TaobaoService;
import com.cn.leedane.utils.*;
import com.taobao.api.ApiException;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 淘宝商品的service的实现类
 * @author LeeDane
 * 2017年12月6日 下午7:52:25
 * version 1.0
 */
@Service("mallSearchService")
public class MallSearchServiceImpl extends MallRoleCheckService implements MallSearchService<IDBean>{
	Logger logger = Logger.getLogger(getClass());

	@Autowired
	private S_ProductHandler productHandler;
	
	@Autowired
	private S_ProductMapper productMapper;
	
	@Autowired
	private OperateLogService<OperateLogBean> operateLogService;
	
	@Autowired
	private S_TaobaoService<IDBean> taobaoService;
	@Override
	public Map<String, Object> product(JSONObject jo, UserBean user, HttpRequestInfoBean request) throws Exception {
		logger.info("MallSearchServiceImpl-->product():jo="+jo);
		ResponseMap message = new ResponseMap();

		long current = JsonUtil.getLongValue(jo, "current", 0);
		long rows = JsonUtil.getLongValue(jo, "rows", 10);
		String keyword = JsonUtil.getStringValue(jo, "keyword"); //搜索关键字
		String platform = JsonUtil.getStringValue(jo, "platform", EnumUtil.ProductPlatformType.淘宝.value);
		String sort = JsonUtil.getStringValue(jo, "sort");
		Map<String, Object> resultMap = new HashMap<String, Object>();
		SearchProductRequest productRequest = new SearchProductRequest();
		productRequest.setKeyword(keyword);
		productRequest.setPageSize(rows);
		productRequest.setPageNo(current);
		productRequest.setSort(sort);

		SearchProductResult productResult = null;
		if(EnumUtil.ProductPlatformType.淘宝.value.equalsIgnoreCase(platform) || EnumUtil.ProductPlatformType.淘宝.value.equalsIgnoreCase(platform)){
			productResult = SearchProductApi.searchProduct(productRequest);
		}else if(EnumUtil.ProductPlatformType.京东.value.equalsIgnoreCase(platform)){
			productResult = com.cn.leedane.mall.jingdong.api.SearchProductApi.searchProduct(productRequest);
		}else if(EnumUtil.ProductPlatformType.拼多多.value.equalsIgnoreCase(platform)){
			//由于拼多多不支持链接查询，但是支持商品ID查询
			if(StringUtil.isLink(productRequest.getKeyword())){
				String productId = CommonUtil.parseLinkParams(productRequest.getKeyword(), "goods_id");
				if(StringUtil.isNotNull(productId))
					productRequest.setKeyword(productId);
			}

			productResult = com.cn.leedane.mall.pdd.api.SearchProductApi.searchProduct(productRequest);
		}
		message.put("platform", platform);
		message.put("total", productResult.getTotal());
		resultMap.put("list", productResult.getTaobaoItems());
		message.put("isSuccess", true);
		message.put("message", resultMap);
		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);

		//保存操作日志
//		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user != null ? user.getAccount(): "","对淘宝的商品发起查询", "结果是：", StringUtil.getSuccessOrNoStr(result)).toString(), "search()", ConstantsUtil.STATUS_NORMAL, 0);
		return message.getMap();
	}
	@Override
	public Map<String, Object> buildShare(String taobaoId, UserBean user,
										 HttpRequestInfoBean request) {
		logger.info("MallSearchServiceImpl-->buildShare():taobaoId="+taobaoId);
		ResponseMap message = new ResponseMap();
		try {
			AlimamaShareLink alimama = new AlimamaShareLink();
			JSONObject dataJson = alimama.doParse(taobaoId).getJSONObject("data");
			if(dataJson != null){
				ProductPromotionLinkBean promotionLinkBean = new ProductPromotionLinkBean();
				promotionLinkBean.setClickUrl(JsonUtil.getStringValue(dataJson, "clickUrl"));
				promotionLinkBean.setCouponLink(JsonUtil.getStringValue(dataJson, "couponLink"));
				promotionLinkBean.setCouponLinkTaoToken(JsonUtil.getStringValue(dataJson, "couponLinkTaoToken"));
				promotionLinkBean.setCouponShortLinkUrl(JsonUtil.getStringValue(dataJson, "couponShortLinkUrl"));
				promotionLinkBean.setQrCodeUrl(JsonUtil.getStringValue(dataJson, "qrCodeUrl"));
				promotionLinkBean.setShortLinkUrl(JsonUtil.getStringValue(dataJson, "shortLinkUrl"));
				promotionLinkBean.setTaoToken(JsonUtil.getStringValue(dataJson, "taoToken"));
				message.put("message", promotionLinkBean);
				message.put("isSuccess", true);
			}else{
				message.put("message", "该商品暂时没有佣金，无法生成共享链接！");
			}

		} catch (Exception  e) {
			e.printStackTrace();
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.淘宝api请求失败.value));
			message.put("responseCode", EnumUtil.ResponseCode.淘宝api请求失败.value);
		}

		//保存操作日志
//		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"对淘宝的商品ID为", taobaoId, "生成分享链接。", "结果是：", StringUtil.getSuccessOrNoStr(true)).toString(), "buildShare()", ConstantsUtil.STATUS_NORMAL, 0);
		return message.getMap();
	}

	@Override
	public Map<String, Object> productRecommend(long productId, JSONObject jo, UserBean user,
												HttpRequestInfoBean request) throws ApiException {

		logger.info("MallSearchServiceImpl-->productRecommend(): productId="+ productId +", jo="+jo);
		ResponseMap message = new ResponseMap();

		long count = JsonUtil.getLongValue(jo, "count", 12);
		message.put("message", SearchMaterialApi.search(productId, count).getTaobaoItems());
		message.put("isSuccess", true);
		message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);

		//保存操作日志
//		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user != null ? user.getAccount(): "","对淘宝的商品发起查询", "结果是：", StringUtil.getSuccessOrNoStr(result)).toString(), "search()", ConstantsUtil.STATUS_NORMAL, 0);
		return message.getMap();
	}

	@Override
	public Map<String, Object> bigfield(String productId, JSONObject json, UserBean user,
										 HttpRequestInfoBean request) throws Exception {
		logger.info("MallSearchServiceImpl-->bigfield():productId="+productId);
		ResponseMap message = new ResponseMap();
		if(productId.startsWith("tb_")){
			//return taobaoService.transform(json, user, request);
		}else if(productId.startsWith("jd_")){
			String productIdTemp = productId.substring(3, productId.length());
			message.put("message", DetailBigFieldApi.get(productIdTemp));
			message.put("isSuccess", true);
			return message.getMap();
		}else if(productId.startsWith("pdd_")){
			String productIdTemp = productId.substring(4, productId.length());
			S_PlatformProductBean platformProductBean = DetailSimpleApi.getDetail(productIdTemp);
			message.put("message", platformProductBean == null? "商品不存在": platformProductBean.getDetail());
			message.put("isSuccess", true);
		}
		return message.getMap();
	}

	@Override
	public Map<String, Object> shop(JSONObject jo, UserBean user, HttpRequestInfoBean request) {
		return null;
	}

	@Override
	public Map<String, Object> activity(JSONObject jo, UserBean user, HttpRequestInfoBean request) {
		return null;
	}
}
