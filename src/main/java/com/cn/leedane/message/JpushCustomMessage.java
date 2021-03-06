package com.cn.leedane.message;

import java.util.Set;

import org.apache.log4j.Logger;

import cn.jpush.api.JPushClient;
import cn.jpush.api.common.resp.APIConnectionException;
import cn.jpush.api.common.resp.APIRequestException;
import cn.jpush.api.push.PushResult;
import cn.jpush.api.push.model.Message;
import cn.jpush.api.push.model.Platform;
import cn.jpush.api.push.model.PushPayload;
import cn.jpush.api.push.model.audience.Audience;

import com.cn.leedane.utils.ConstantsUtil;
import com.cn.leedane.message.notification.CustomMessage;

/**
 * Jpush发送自定义的消息
 * @author LeeDane
 * 2016年7月12日 下午2:22:45
 * Version 1.0
 */
public class JpushCustomMessage implements CustomMessage{
	private Logger logger = Logger.getLogger(getClass());
	
	@Override
	public boolean sendToRegistrationIDs(Set<String> registrationIds, String content, String key, int value) {
		return false;
	}

	@Override
	public boolean sendToAlias(String alias, String content, int businessType) {
		long timeLive = 7*24*60*60;
		JPushClient jpushClient = new JPushClient(
				ConstantsUtil.JPUSH_MASTER_SECRET, ConstantsUtil.JPUSH_APPKEY, false, timeLive);
		PushPayload payload = buildPushAlias(alias, content, businessType);
		try {
			PushResult result = jpushClient.sendPush(payload);
			return result.getResponseCode() == 200;

		} catch (APIConnectionException e) {
			logger.error("Connection error, should retry later");

		} catch (APIRequestException e) {
			logger.error("Should review the error, and fix the request");
			logger.error("HTTP Status: " + e.getStatus());
			logger.error("Error Code: " + e.getErrorCode());
			logger.error("Error Message: " + e.getErrorMessage());
		}
		return false;
	}
	
	@Override
	public boolean sendToTag(String tag, String content, int businessType) {
		return false;
	}
	
	@Override
	public boolean sendToAllUser(String content, int businessType) {
		return false;
	}
	
	/**
	 * 把自定义消息推送给指定的用户的客户端
	 * @param alias
	 * @param content
	 * @return
	 */
	private static PushPayload buildPushAlias(String alias, String content, int businessType) {
        return PushPayload.newBuilder()
                .setPlatform(Platform.all())
                .setAudience(Audience.alias(alias))
                .setMessage(Message.newBuilder()
                        .setMsgContent(content)
                        .addExtra("businessType", businessType)
                        .build())
                .build();
    }

}
