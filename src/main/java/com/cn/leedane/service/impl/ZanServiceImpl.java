package com.cn.leedane.service.impl;

import com.cn.leedane.handler.*;
import com.cn.leedane.mapper.ZanMapper;
import com.cn.leedane.model.HttpRequestInfoBean;
import com.cn.leedane.model.OperateLogBean;
import com.cn.leedane.model.UserBean;
import com.cn.leedane.model.ZanBean;
import com.cn.leedane.redis.util.RedisUtil;
import com.cn.leedane.service.OperateLogService;
import com.cn.leedane.service.ZanService;
import com.cn.leedane.utils.*;
import com.cn.leedane.utils.EnumUtil.DataTableType;
import com.cn.leedane.utils.EnumUtil.NotificationType;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
/**
 * 赞service的实现类
 * @author LeeDane
 * 2016年7月12日 下午2:33:30
 * Version 1.0
 */
@Service("zanService")
public class ZanServiceImpl implements ZanService<ZanBean>{
	Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private ZanMapper zanMapper;
	
	@Autowired
	private ZanHandler zanHandler;
	
	@Autowired
	private FriendHandler friendHandler;
	
	@Autowired
	private UserHandler userHandler;

	@Autowired
	private CommonHandler commonHandler;
	
	@Autowired
	private OperateLogService<OperateLogBean> operateLogService;
	
	@Autowired
	private NotificationHandler notificationHandler;
	
	@Override
	public ResponseModel addZan(JSONObject jo, UserBean user,
			HttpRequestInfoBean request){
		//{\"table_name\":\"t_mood\", \"table_id\":1}
		logger.info("ZanServiceImpl-->addZan():jsonObject=" +jo.toString() +", user=" +user.getAccount());
		String tableName = JsonUtil.getStringValue(jo, "table_name");
		String content = JsonUtil.getStringValue(jo, "content");
		String froms = JsonUtil.getStringValue(jo, "froms");
		long tableId = JsonUtil.getLongValue(jo, "table_id", 0);
		ResponseMap message = new ResponseMap();
		if(SqlUtil.getBooleanByList(zanMapper.exists(ZanBean.class, tableName, tableId, user.getId())))
			return new ResponseModel().error().message("您已点赞，请勿重复操作！").code(EnumUtil.ResponseCode.添加的记录已经存在.value);
		
		if(!SqlUtil.getBooleanByList(zanMapper.recordExists(tableName, tableId)))
			return new ResponseModel().error().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.操作对象不存在.value)).code(EnumUtil.ResponseCode.操作对象不存在.value);
		ZanBean bean = new ZanBean();
		bean.setCreateTime(new Date());
		bean.setCreateUserId(user.getId());
		bean.setStatus(ConstantsUtil.STATUS_NORMAL);
		bean.setTableName(tableName);
		bean.setTableId(tableId);
		bean.setContent(content);
		bean.setFroms(froms);
		boolean result = zanMapper.save(bean) > 0;
		if(result){
			long createUserId = 0;
			String str = "{from_user_remark}给您点赞";
			createUserId = SqlUtil.getCreateUserIdByList(zanMapper.getObjectCreateUserId(tableName, tableId));
			if(createUserId > 0 && createUserId != user.getId()){
				Set<Long> ids = new HashSet<>();
				ids.add(createUserId);
				notificationHandler.sendNotificationByIds(false, user.getId(), ids, str, NotificationType.赞过我, tableName, tableId, bean);
			}
		}
		
		//记录到redis服务器中
		zanHandler.addZanNumber(tableId, tableName);
		
		//记录到redis服务器中
		zanHandler.addZanUser(tableId, tableName, user);
		if(result){
			message.put("success", result);
			message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.点赞成功.value));
			message.put("responseCode", EnumUtil.ResponseCode.请求返回成功码.value);
			return new ResponseModel().ok().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.点赞成功.value));
		}else
			return new ResponseModel().error().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.点赞失败.value)).code(EnumUtil.ResponseCode.点赞失败.value);
	}

	@Override
	public ResponseModel getLimit(JSONObject jo, UserBean user,
			HttpRequestInfoBean request) {
		logger.info("ZanServiceImpl-->getLimit():jsonObject=" +jo.toString() +", user=" +user.getAccount());
		long toUserId = JsonUtil.getLongValue(jo, "toUserId");
		String tableName = JsonUtil.getStringValue(jo, "table_name");
		long tableId = JsonUtil.getLongValue(jo, "table_id", 0);
		String method = JsonUtil.getStringValue(jo, "method", "firstloading"); //操作方式
		int pageSize = JsonUtil.getIntValue(jo, "pageSize", ConstantsUtil.DEFAULT_PAGE_SIZE); //每页的大小
		long lastId = JsonUtil.getLongValue(jo, "last_id"); //开始的页数
		long firstId = JsonUtil.getLongValue(jo, "first_id"); //结束的页数
		boolean showUserInfo = JsonUtil.getBooleanValue(jo, "showUserInfo");
		StringBuffer sql = new StringBuffer();
		List<Map<String, Object>> rs = new ArrayList<Map<String,Object>>();
		//查找该用户所有的关注
		if(StringUtil.isNull(tableName) && toUserId > 0){		
			if("firstloading".equalsIgnoreCase(method)){
				sql.append("select z.id, z.froms, z.content, z.table_name, z.table_id, z.create_user_id, u.account, date_format(z.create_time,'%Y-%m-%d %H:%i:%s') create_time ");
				sql.append(" from "+DataTableType.赞.value+" z inner join "+DataTableType.用户.value+" u on u.id = z.create_user_id where z.create_user_id = ? and z.status = ? ");
				sql.append(" order by z.id desc limit 0,?");
				rs = zanMapper.executeSQL(sql.toString(), toUserId, ConstantsUtil.STATUS_NORMAL, pageSize);
			//下刷新
			}else if("lowloading".equalsIgnoreCase(method)){
				sql.append("select z.id, z.froms, z.content, z.table_name, z.table_id, z.create_user_id, u.account, date_format(z.create_time,'%Y-%m-%d %H:%i:%s') create_time ");
				sql.append(" from "+DataTableType.赞.value+" z inner join "+DataTableType.用户.value+" u on u.id = z.create_user_id where z.create_user_id = ? and z.status = ? ");
				sql.append(" and z.id < ? order by z.id desc limit 0,? ");
				rs = zanMapper.executeSQL(sql.toString(), toUserId, ConstantsUtil.STATUS_NORMAL, lastId, pageSize);
			//上刷新
			}else if("uploading".equalsIgnoreCase(method)){
				sql.append("select z.id, z.froms, z.content, z.table_name, z.table_id, z.create_user_id, u.account, date_format(z.create_time,'%Y-%m-%d %H:%i:%s') create_time ");
				sql.append(" from "+DataTableType.赞.value+" z inner join "+DataTableType.用户.value+" u on u.id = z.create_user_id where z.create_user_id = ? and z.status = ? ");
				sql.append(" and z.id > ? limit 0,?  ");
				rs = zanMapper.executeSQL(sql.toString() , toUserId, ConstantsUtil.STATUS_NORMAL, firstId, pageSize);
			}
		}
		
		//查找该用户指定表的数据
		if(StringUtil.isNotNull(tableName) && toUserId < 1 && tableId > 0){
			if("firstloading".equalsIgnoreCase(method)){
				sql.append("select z.id, z.froms, z.content, z.table_name, z.table_id, z.create_user_id, u.account, date_format(z.create_time,'%Y-%m-%d %H:%i:%s') create_time ");
				sql.append(" from "+DataTableType.赞.value+" z inner join "+DataTableType.用户.value+" u on u.id = z.create_user_id where z.status = ? and z.table_name = ? and z.table_d = ?");
				sql.append(" order by z.id desc limit 0,?");
				rs = zanMapper.executeSQL(sql.toString(), ConstantsUtil.STATUS_NORMAL, tableName, tableId, pageSize);
			//下刷新
			}else if("lowloading".equalsIgnoreCase(method)){
				sql.append("select z.id, z.froms, z.content, z.table_name, z.table_id, z.create_user_id, u.account, date_format(z.create_time,'%Y-%m-%d %H:%i:%s') create_time ");
				sql.append(" from "+DataTableType.赞.value+" z inner join "+DataTableType.用户.value+" u on u.id = z.create_user_id where z.status = ? and z.table_name = ? and z.table_d = ?");
				sql.append(" and z.id < ? order by z.id desc limit 0,? ");
				rs = zanMapper.executeSQL(sql.toString(), ConstantsUtil.STATUS_NORMAL, tableName, tableId, lastId, pageSize);
			//上刷新
			}else if("uploading".equalsIgnoreCase(method)){
				sql.append("select z.id, z.froms, z.content, z.table_name, z.table_id, z.create_user_id, u.account, date_format(z.create_time,'%Y-%m-%d %H:%i:%s') create_time ");
				sql.append(" from "+DataTableType.赞.value+" z inner join "+DataTableType.用户.value+" u on u.id = z.create_user_id where z.status = ? and z.table_name = ? and z.table_d = ?");
				sql.append(" and z.id > ? limit 0,?  ");
				rs = zanMapper.executeSQL(sql.toString() , ConstantsUtil.STATUS_NORMAL, tableName, tableId, firstId, pageSize);
			}
		}
		if(rs !=null && rs.size() > 0){
			int createUserId = 0;
			JSONObject friendObject = friendHandler.getFromToFriends(user.getId());
			String tabName;
			int tabId;
			//为名字备注赋值
			for(int i = 0; i < rs.size(); i++){
				if(StringUtil.isNull(tableName) && tableId <1){
					//在非获取指定表下的赞列表的情况下的前面35个字符
					tabName = StringUtil.changeNotNull((rs.get(i).get("table_name")));
					tabId = StringUtil.changeObjectToInt(rs.get(i).get("table_id"));
					rs.get(i).put("source", commonHandler.getContentByTableNameAndId(tabName, tabId, user));
				}
				if(showUserInfo){
					createUserId = StringUtil.changeObjectToInt(rs.get(i).get("create_user_id"));
					rs.get(i).putAll(userHandler.getBaseUserInfo(createUserId, user, friendObject));
				}
			}	
		}
		
		//保存操作日志
//		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"获取用户ID为：",toUserId , ",表名为：", tableName,"，表id为：",tableId,"的赞列表").toString(), "getLimit()", ConstantsUtil.STATUS_NORMAL, 0);

		return new ResponseModel().ok().message(rs);
	}

	@Override
	public ResponseModel deleteZan(JSONObject jo, UserBean user,
			HttpRequestInfoBean request) {
		logger.info("ZanServiceImpl-->deleteZan():jsonObject=" +jo.toString() +", user=" +user.getAccount());
		long zid = JsonUtil.getLongValue(jo, "zid");
		long createUserId = JsonUtil.getLongValue(jo, "create_user_id");
		//非登录用户不能删除操作
		if(createUserId != user.getId())
			return new ResponseModel().error().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.请先登录.value)).code(EnumUtil.ResponseCode.请先登录.value);
		
		if(zid < 1)
			return new ResponseModel().error().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.没有操作实例.value)).code(EnumUtil.ResponseCode.没有操作实例.value);
		
		boolean result = false;
		ResponseModel responseModel = new ResponseModel();
		ZanBean zanBean = zanMapper.findById(ZanBean.class, zid);
		if(zanBean != null && zanBean.getCreateUserId() == createUserId){
			result = zanMapper.deleteById(ZanBean.class, zid) > 0;
		}else
			responseModel.error().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.操作对象不存在.value)).code(EnumUtil.ResponseCode.操作对象不存在.value);

		if(result){
			//取消赞的数据
			zanHandler.cancelZan(zanBean.getTableId(), zanBean.getTableName(), user);
			responseModel.ok().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.取消点赞成功.value));
		}else
			responseModel.error().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.取消点赞失败.value)).code(EnumUtil.ResponseCode.取消点赞失败.value);
		//保存操作日志
		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"删除赞ID为", zid, "的数据", StringUtil.getSuccessOrNoStr(result)).toString(), "deleteZan()", StringUtil.changeBooleanToInt(result), EnumUtil.LogOperateType.内部接口.value);
		return responseModel;
	}

	@Override
	public ResponseModel getAllZanUser(JSONObject jo, UserBean user,
			HttpRequestInfoBean request) {
		logger.info("ZanServiceImpl-->getAllZanUser():jsonObject=" +jo.toString() +", user=" +user.getAccount());
		ResponseMap message = new ResponseMap();
		long tableId = JsonUtil.getLongValue(jo, "table_id");
		String tableName = JsonUtil.getStringValue(jo, "table_name");

		if(tableId == 0 || StringUtil.isNull(tableName))
			return new ResponseModel().error().message(EnumUtil.getResponseValue(EnumUtil.ResponseCode.操作对象不存在.value)).code(EnumUtil.ResponseCode.操作对象不存在.value);
		
		List<Map<String, Object>> rs = zanMapper.executeSQL("select z.id, u.id create_user_id, date_format(z.create_time,'%Y-%m-%d %H:%i:%s') create_time  from "+DataTableType.赞.value+" z inner join "+DataTableType.用户.value+" u on z.create_user_id = u.id where z.table_name=? and z.table_id = ?", tableName, tableId);
		if(rs != null && rs.size() > 0){
			int createUserId ;
			String[] userArray = new String[rs.size()];
			for(int i = 0; i < rs.size(); i++){
				createUserId = StringUtil.changeObjectToInt(rs.get(i).get("create_user_id"));
				rs.get(i).putAll(userHandler.getBaseUserInfo(createUserId));
				userArray[i] = StringUtil.changeNotNull(rs.get(i).get("id"))+ ","+ StringUtil.changeNotNull(rs.get(i).get("account"));
			}
			
			//同时更新一下赞的信息
			RedisUtil redisUtil = RedisUtil.getInstance();
			String zanUserKey = ZanHandler.getZanUserKey(tableName, tableId);
			String zanKey = ZanHandler.getZanKey(tableName, tableId);
			redisUtil.delete(zanUserKey);
			redisUtil.delete(zanKey);
			redisUtil.addSet(zanUserKey, userArray);
			redisUtil.addString(zanKey, String.valueOf(rs.size()));
		}
		//保存操作日志
//		operateLogService.saveOperateLog(user, request, null, StringUtil.getStringBufferStr(user.getAccount(),"获取表ID为：", tableId, ",表名为：", tableName, "的全部赞用户", StringUtil.getSuccessOrNoStr(true)).toString(), "getAllZanUser()", ConstantsUtil.STATUS_NORMAL, 0);
		return new ResponseModel().ok().message(rs);

	}

	@Override
	public List<Map<String, Object>> executeSQL(String sql, Object... params) {
		return zanMapper.executeSQL(sql, params);
	}
}
