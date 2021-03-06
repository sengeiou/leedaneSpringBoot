package com.cn.leedane.handler;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONObject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.cn.leedane.mapper.FriendMapper;
import com.cn.leedane.redis.util.RedisUtil;
import com.cn.leedane.utils.ConstantsUtil;
import com.cn.leedane.utils.EnumUtil.DataTableType;
import com.cn.leedane.utils.StringUtil;

/**
 * 好友处理类
 * @author LeeDane
 * 2016年3月21日 上午10:09:40
 * Version 1.0
 */
@Component
public class FriendHandler {
	
	@Autowired
	private FriendMapper friendMapper;
	
	private RedisUtil redisUtil = RedisUtil.getInstance();

	@Autowired
	private CircleOfFriendsHandler circleOfFriendsHandler;
	
	@Autowired
	private UserHandler userHandler;

	/**
	 * 获取好友列表的json对象
	 * @param getUserId
	 * @return
	 */
	public JSONObject getFromToFriends(long getUserId){
		String friendKey = getFriendKey(getUserId);
		JSONObject friendObject = new JSONObject();
		//评论
		if(!redisUtil.hasKey(friendKey)){
			String sql = " select to_user_id id, (case when to_user_remark = '' || to_user_remark = null then (select u.account from "+DataTableType.用户.value+" u where  u.id = to_user_id and u.status = "+ConstantsUtil.STATUS_NORMAL+") else to_user_remark end ) remark from "+DataTableType.好友.value+" where from_user_id =? and status = "+ConstantsUtil.STATUS_NORMAL+" "
					+" UNION " 
					+" select from_user_id id, (case when from_user_remark = '' || from_user_remark = null then (select u.account from "+DataTableType.用户.value+" u where  u.id = from_user_id and u.status = "+ConstantsUtil.STATUS_NORMAL+") else from_user_remark end ) remark from "+DataTableType.好友.value+" where to_user_id = ? and status = "+ConstantsUtil.STATUS_NORMAL;
	
			List<Map<String, Object>> friends = friendMapper.executeSQL(sql, getUserId, getUserId);
			Set<Long> fids = new HashSet<Long>();
			String friendIdKey = getFriendIdsKey(getUserId);
			fids.add(getUserId);
			if(friends != null && friends.size() > 0){
				long id = 0;
				for(int i =0; i < friends.size(); i++){
					id = StringUtil.changeObjectToLong(friends.get(i).get("id"));
					fids.add(id);
					friendObject.put("user_" +id, String.valueOf(friends.get(i).get("remark")));
					friendObject.put("mobile_phone", userHandler.getUserMobilePhone(id));
				}
				redisUtil.addString(friendKey, friendObject.toString());
			}
			redisUtil.addString(friendIdKey, setToString(fids));
		}else{
			String friends = redisUtil.getString(friendKey);
			if(StringUtil.isNotNull(friends)){
				friendObject = JSONObject.fromObject(friends);
			}
		}
		return friendObject;
	}
	
	/**
	 * 删除好友关系
	 * @param fromUserId
	 * @param toUserId
	 */
	public void delete(long fromUserId, long toUserId){
		redisUtil.delete(getFriendKey(fromUserId));
		redisUtil.delete(getFriendKey(toUserId));
		redisUtil.delete(getFriendIdsKey(fromUserId));
		redisUtil.delete(getFriendIdsKey(toUserId));
	}

	
	/**
	 * 将set集合转成字符串
	 * @param set
	 * @return
	 */
	private String setToString(Set<Long> set){
		String str = "";
		if(set != null && set.size() > 0){
			StringBuffer result = new StringBuffer();
			for(Long s: set){
				result.append(s);
				result.append(";");
			}
			
			str = result.toString().substring(0, result.toString().length() -1);
		}
		return str;
	}
	
	/**
	 * 获取该用户的列表好友列表(包括自己)
	 * @param userId
	 * @return
	 */
	public Set<Long> getFromToFriendIds(long userId){
		String friendIdKey = getFriendIdsKey(userId);
		JSONObject friendObject = new JSONObject();
		Set<Long> fids = new HashSet<Long>();
		//评论
		if(!redisUtil.hasKey(friendIdKey)){
			String sql = " select to_user_id id, (case when to_user_remark = '' || to_user_remark = null then (select u.account from "+DataTableType.用户.value+" u where  u.id = to_user_id and u.status = "+ConstantsUtil.STATUS_NORMAL+") else to_user_remark end ) remark from "+DataTableType.好友.value+" where from_user_id =? and status = "+ConstantsUtil.STATUS_NORMAL+" "
					+" UNION " 
					+" select from_user_id id, (case when from_user_remark = '' || from_user_remark = null then (select u.account from "+DataTableType.用户.value+" u where  u.id = from_user_id and u.status = "+ConstantsUtil.STATUS_NORMAL+") else from_user_remark end ) remark from "+DataTableType.好友.value+" where to_user_id = ? and status = "+ConstantsUtil.STATUS_NORMAL;
	
			List<Map<String, Object>> friends = friendMapper.executeSQL(sql, userId, userId);
			String friendKey = getFriendKey(userId);
			fids.add(userId);
			if(friends != null && friends.size() > 0){
				long id = 0;
				for(int i =0; i < friends.size(); i++){
					id = StringUtil.changeObjectToInt(friends.get(i).get("id"));
					fids.add(id);
					friendObject.put("user_" +id, String.valueOf(friends.get(i).get("remark")));
				}
				redisUtil.addString(friendKey, friendObject.toString());
			}
			redisUtil.addString(friendIdKey, setToString(fids));
		}else{
			String friendIds = redisUtil.getString(friendIdKey);
			return stringToSet(friendIds, userId);
		}
		return fids;
	}
	
	/**
	 * 添加好友成功后，更新redis两边的数据
	 * @param toUserId
	 * @param userId
	 */
	public boolean addFriends(long toUserId, long userId, String toUserRemark, String userRemark){
		JSONObject toUserObject = getFromToFriends(toUserId);
		JSONObject userObject = getFromToFriends(userId);
		String toUserKey = getFriendKey(toUserId);
		String userKey = getFriendKey(userId);
		
		String toUserFriendIdKey = getFriendIdsKey(toUserId);
		String userFriendIdKey = getFriendIdsKey(userId);
		
		//处理好友关系的id
		//更新对方的好友列表
		Set<Long> ids = getFromToFriendIds(toUserId);
		ids.add(userId);
		redisUtil.addString(toUserFriendIdKey, setToString(ids));
			
		//更新自己的好友列表
		ids = getFromToFriendIds(userId);
		ids.add(toUserId);
		redisUtil.addString(userFriendIdKey, setToString(ids));
		
		//更新双方的timeline
		//myCircleOfFriendsHandler.mergeTimeLine(userId, toUserId);
		
		if(toUserObject == null){//还没有好友
			toUserObject = new JSONObject();
		}else{
			redisUtil.delete(toUserKey);
		}
		toUserObject.put("user_" +userId, userRemark);
		redisUtil.addString(toUserKey, toUserObject.toString());
		
		if(userObject == null){//还没有好友
			userObject = new JSONObject();
		}
		
		userObject.put("user_" +toUserId, toUserRemark);
		return redisUtil.addString(userKey, userObject.toString());
	}
	
	/**
	 * 判断toUserId是否在userId关注列表中
	 * @param userId
	 * @param toUserId
	 * @return
	 */
	public boolean inFriend(long userId, long toUserId){
		boolean result = false;
		if(userId == toUserId || userId < 1 || toUserId < 1)
			return result;
		Set<Long> set = getFromToFriendIds(userId);
		if(set != null && set.size() >0){
			result = set.contains(toUserId);
		}
		return result;
	}
	
	
	/**
	 * 将字符串转成数组集合
	 * @param str
	 * @param userId
	 * @return
	 */
	private Set<Long> stringToSet(String str, long userId){
		if(StringUtil.isNull(str)){
			return new HashSet<Long>();
		}
		Set<Long> ids = new HashSet<Long>();
		Object[] objs = str.split(";");
		for(int i =0; i < objs.length; i++){
			ids.add(StringUtil.changeObjectToLong(objs[i]));
		}
		return ids;
	}
	/**
	 * 获取好友id列表在redis的key(包括自己)
	 * @param userId
	 * @return
	 */
	public static String getFriendIdsKey(long userId){
		return ConstantsUtil.FRIEND_ID_REDIS +userId;
	}
	
	/**
	 * 获取评论在redis的key
	 * @param userId
	 * @return
	 */
	public static String getFriendKey(long userId){
		return ConstantsUtil.FRIEND_REDIS +userId;
	}
}
