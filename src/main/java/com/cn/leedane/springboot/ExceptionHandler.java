package com.cn.leedane.springboot;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cn.leedane.exception.*;
import com.cn.leedane.mall.github.GithubException;
import com.cn.leedane.mall.pdd.PddException;
import com.cn.leedane.notice.NoticeException;
import com.jd.open.api.sdk.JdException;
import com.suning.api.exception.SuningApiException;
import com.taobao.api.ApiException;
import org.apache.log4j.Logger;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.ExcessiveAttemptsException;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.LockedAccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authc.pam.UnsupportedTokenException;
import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import com.cn.leedane.exception.user.BannedAccountException;
import com.cn.leedane.exception.user.CancelAccountException;
import com.cn.leedane.exception.user.NoActiveAccountException;
import com.cn.leedane.exception.user.NoCompleteAccountException;
import com.cn.leedane.exception.user.NoValidationEmailAccountException;
import com.cn.leedane.exception.user.StopUseAccountException;
import com.cn.leedane.utils.CommonUtil;
import com.cn.leedane.utils.EnumUtil;
import com.cn.leedane.utils.EnumUtil.ResponseCode;
import com.cn.leedane.utils.StringUtil;

/**
 * sprngmvc全局异常的处理类
 * @author LeeDane
 * 2017年3月30日 上午10:14:17
 * version 1.0
 */
@Component
public class ExceptionHandler implements HandlerExceptionResolver {
	private Logger logger = Logger.getLogger(getClass());

	@Override
	public ModelAndView resolveException(HttpServletRequest request,
			HttpServletResponse response, Object handler, Exception exception) {
		
		
		boolean isPageRequest = CommonUtil.isPageRequest(request);
		
		Map<String, Object> message = new HashMap<String, Object>();
        message.put("success", false);
		if(exception instanceof org.apache.shiro.authz.UnauthorizedException){//没有授权异常
			logger.error(EnumUtil.getResponseValue(ResponseCode.没有操作权限.value), exception);
			if(isPageRequest){
				return new ModelAndView("forward:/403");
			}
			 message.put("message", EnumUtil.getResponseValue(ResponseCode.没有操作权限.value) +":"+ exception.getMessage());
			 message.put("responseCode", ResponseCode.没有操作权限.value);
		}else if(exception instanceof UnsupportedTokenException){//不支持token异常
			logger.error(EnumUtil.getResponseValue(ResponseCode.token过期或无效.value), exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.token过期或无效.value));
			message.put("responseCode", ResponseCode.token过期或无效.value);
			
		}else if(exception instanceof RE404Exception){//404异常
			logger.error(EnumUtil.getResponseValue(ResponseCode.资源不存在.value), exception);
			if(isPageRequest)
				return new ModelAndView("forward:/404");
			
			message.put("message", exception.getMessage());
			message.put("responseCode", ResponseCode.资源不存在.value);
		}else if(exception instanceof UnknownAccountException){//未知账户
			logger.error("对用户进行登录验证..验证未通过,未知账户", exception);
			if(isPageRequest)
				return new ModelAndView("redirect:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.用户名或者密码不匹配.value));
			message.put("responseCode", ResponseCode.用户名或者密码不匹配.value);
			
		}else if(exception instanceof BannedAccountException){//用户已被禁言
			logger.error("对用户进行登录验证..验证未通过,用户已经被禁言了", exception);
			if(isPageRequest)
				return new ModelAndView("redirect:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.用户已被禁言.value));
			message.put("responseCode", ResponseCode.用户已被禁言.value);
			
		}else if(exception instanceof CancelAccountException){//用户已经注销
			logger.error("对用户进行登录验证..验证未通过,用户已经注销了", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.用户已经注销.value));
			message.put("responseCode", ResponseCode.用户已经注销.value);
			
		}else if(exception instanceof StopUseAccountException){//用户已被禁止使用
			logger.error("对用户进行登录验证..验证未通过,用户暂时被禁止使用", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.用户已被禁止使用.value));
			message.put("responseCode", ResponseCode.用户已被禁止使用.value);
			
		}else if(exception instanceof NoValidationEmailAccountException){//用户未验证邮箱
			logger.error("对用户进行登录验证..验证未通过,用户未验证邮箱", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.请先验证邮箱.value));
			message.put("responseCode", ResponseCode.请先验证邮箱.value);
			
		}else if(exception instanceof NoActiveAccountException){//注册未激活账户
			logger.error("对用户进行登录验证..验证未通过,用户未激活", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.注册未激活账户.value));
			message.put("responseCode", ResponseCode.注册未激活账户.value);
			
		}else if(exception instanceof NoCompleteAccountException){//未完善信息
			logger.error("对用户进行登录验证..验证未通过,用户未完善信息", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.未完善信息.value));
			message.put("responseCode", ResponseCode.未完善信息.value);
			
		}else if(exception instanceof IncorrectCredentialsException){//密码不正确
			logger.error("对用户进行登录验证..验证未通过,错误的凭证", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.密码不正确.value));
			message.put("responseCode", ResponseCode.密码不正确.value);
			
		}else if(exception instanceof LockedAccountException){//账户已锁定
			logger.error("对用户进行登录验证..验证未通过,账户已锁定", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.账户已锁定.value));
			message.put("responseCode", ResponseCode.账户已锁定.value);
			
		}else if(exception instanceof ExcessiveAttemptsException){//用户名或密码错误次数过多
			logger.error("对用户进行登录验证..验证未通过,错误次数过多", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.用户名或密码错误次数过多.value));
			message.put("responseCode", ResponseCode.用户名或密码错误次数过多.value);
			
		}else if(exception instanceof AuthenticationException){//账号或密码不匹配
			logger.error("对用户进行登录验证..验证未通过,堆栈轨迹如下", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.账号或密码不匹配.value));
			message.put("responseCode", ResponseCode.账号或密码不匹配.value);
			
		}else if(exception instanceof NullPointerException){//空指针异常
			exception.printStackTrace();
			logger.error("系统报错，空指针异常！！！", exception);
			//mo
			if(isPageRequest)
				try {
					return new ModelAndView("forward:/null-pointer?errorMessage="+ URLEncoder.encode(exception.getMessage(), "UTF-8"));
				} catch (UnsupportedEncodingException e) {
					e.printStackTrace();
				}
			
			if(StringUtil.isNull(exception.getMessage()))
				message.put("message", "空指针异常");
			else
				message.put("message", exception.getMessage());
			message.put("detail", exception.getMessage());
			message.put("responseCode", ResponseCode.空指针异常.value);
			
		}else if(exception instanceof DuplicateKeyException){
			logger.error("mysql表唯一约束报错！！！", exception);
			message.put("message", "违反唯一性约束，请确定是否已经有相同数据存在！");
			message.put("detail", exception.getMessage());
			message.put("responseCode", ResponseCode.空指针异常.value);
		}else if(exception instanceof DataIntegrityViolationException){
			logger.error("mysql保存异常，某些字段长度太长！", exception);
			if(isPageRequest)
				return new ModelAndView("forward:/lg");
			message.put("message", EnumUtil.getResponseValue(ResponseCode.某些字段超过其存储所需的长度.value));
			message.put("responseCode", ResponseCode.某些字段超过其存储所需的长度.value);
		}else if(exception instanceof ParameterUnspecificationException){
			logger.error("参数不规范的异常", exception);
			message.put("message", EnumUtil.getResponseValue(ResponseCode.参数为空或者不符合规范异常.value) +"：" + exception.getMessage());
			message.put("responseCode", ResponseCode.参数为空或者不符合规范异常.value);
		}else if(exception instanceof CompleteOrderDeleteException){
			logger.error("已经完成的订单被删除的异常", exception);
			message.put("message", EnumUtil.getResponseValue(ResponseCode.完成状态的订单无法删除的异常.value));
			message.put("responseCode", ResponseCode.完成状态的订单无法删除的异常.value);
		}else if(exception instanceof TestRoleException){
			logger.error("已经完成的订单被删除的异常", exception);
			message.put("message", exception.getMessage());
			message.put("responseCode", ResponseCode.测试角色权限受限异常.value);
		}else if(exception instanceof MustLoginException){
			logger.error("必须登录才能使用该功能的异常, 此处不打印异常信息");
			if(isPageRequest)
				return new ModelAndView("redirect:/lg?errorcode="+ EnumUtil.ResponseCode.请先登录.value +"&ref="+ CommonUtil.getFullPath(request) +"&t="+ UUID.randomUUID().toString());
			message.put("message", exception.getMessage());
			message.put("responseCode", ResponseCode.请先登录.value);
		}else if(exception instanceof MustAdminLoginException){
			logger.error("必须是管理员账号登录才能使用的异常", exception);
			if(isPageRequest){
				//使用权限管理工具进行用户的退出，跳出登录，给出提示信息
		        SecurityUtils.getSubject().logout();
				ModelAndView model = new ModelAndView("redirect:/lg?errorcode=" +EnumUtil.ResponseCode.请使用有管理员权限的账号登录.value +"&t="+ UUID.randomUUID().toString() +"&ref="+ CommonUtil.getFullPath(request));
				//model.addAttribute("errorMessage", EnumUtil.getResponseValue(ResponseCode.请使用有管理员权限的账号登录.value));
				return model;
			}
			message.put("message", exception.getMessage());
			message.put("responseCode", ResponseCode.请使用有管理员权限的账号登录.value);
		}else if(exception instanceof MobCodeErrorException){
			logger.error("验证码验证失败", exception);
			message.put("message", EnumUtil.getResponseValue(ResponseCode.验证码验证失败.value));
			message.put("responseCode", ResponseCode.验证码验证失败.value);
		}else if(exception instanceof IllegalOperationException){
			logger.error("非法操作异常", exception);
			message.put("message", exception.getMessage());
			message.put("responseCode", ResponseCode.非法操作异常.value);
		}else if(exception instanceof PageErrorException){
			logger.error("页面错误异常", exception);
			return new ModelAndView("forward:/403");
		}else if(exception instanceof NoNodeAvailableException){
			logger.error("ES服务器连接异常", exception);
			message.put("message", EnumUtil.getResponseValue(ResponseCode.ES服务器连接异常.value));
			message.put("responseCode", ResponseCode.ES服务器连接异常.value);
		}else if(exception instanceof IndexNotFoundException){
			logger.error("ES索引不存在异常", exception);
			message.put("message", EnumUtil.getResponseValue(ResponseCode.ES索引不存在.value));
			message.put("responseCode", ResponseCode.ES索引不存在.value);
		}else if(exception instanceof ApiException){
			logger.error("淘宝联盟api异常", exception);
			message.put("message", "淘宝开放平台：" +exception.getMessage());
			message.put("responseCode", ResponseCode.淘宝api异常.value);
		}else if(exception instanceof JdException){
			logger.error("京东联盟api异常", exception);
			message.put("message", "京东开放平台：" +exception.getMessage());
			message.put("responseCode", ResponseCode.京东api异常.value);
		}else if(exception instanceof PddException){
			logger.error("拼多多联盟api异常", exception);
			message.put("message", "拼多多开放平台：" +exception.getMessage());
			message.put("responseCode", ResponseCode.拼多多api异常.value);
		}else if(exception instanceof SuningApiException){
			logger.error("苏宁联盟api异常", exception);
			message.put("message", "苏宁平台：" +exception.getMessage());
			message.put("responseCode", ResponseCode.苏宁api异常.value);
		}else if(exception instanceof GithubException){
			logger.error("github异常", exception);
			message.put("message", "github开放平台：" +exception.getMessage());
			message.put("responseCode", ResponseCode.GitHub异常.value);
		}else if(exception instanceof NoticeException){
			logger.error("信息发送异常", exception);
			message.put("message", "信息发送异常：" +exception.getMessage());
			message.put("responseCode", ResponseCode.服务器处理异常.value);
		}else{
//			StringPrintWriter strintPrintWriter = new StringPrintWriter();  
//	        exception.printStackTrace(strintPrintWriter);
//	        logger.error(strintPrintWriter.getString());
//	        message.put("message", /*"服务器异常"*/strintPrintWriter.getString());//将错误信息传递给view  
			
			try{
	        	//StringPrintWriter strintPrintWriter = new StringPrintWriter();  
		        //exception.printStackTrace(strintPrintWriter);
	        	//logger.error(strintPrintWriter.getString());
				logger.error("服务器出现异常，请稍后重试！", exception);
		        message.put("message", "服务器出现异常，请稍后重试！");//将错误信息传递给view  
	        }catch(Exception e){
	        	e.printStackTrace();
	        	logger.error("服务器异常", exception);
	        	message.put("message", "服务器异常");//将错误信息传递给view  
	        }
		}
		
		ModelAndView mav = new ModelAndView();
		
		MappingJackson2JsonView view = new MappingJackson2JsonView();
		view.setAttributesMap(message);
		mav.setView(view);
		return mav;
        /*JSONObject jsonObject = JSONObject.fromObject(message);
		response.setCharacterEncoding("utf-8");
		//logger.info("服务器返回:"+jsonObject.toString());
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
			writer.append(jsonObject.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(writer != null)
				writer.close();
		}
		return null;*/
	}  
	
	
}

class StringPrintWriter extends PrintWriter{  
	  
    public StringPrintWriter(){  
        super(new StringWriter());  
    }  
     
    public StringPrintWriter(int initialSize) {  
          super(new StringWriter(initialSize));  
    }  
     
    public String getString() {  
          flush();  
          return ((StringWriter) this.out).toString();  
    }  
     
    @Override  
    public String toString() {  
        return getString();  
    }  
}  
