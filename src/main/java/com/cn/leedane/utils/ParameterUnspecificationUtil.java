package com.cn.leedane.utils;

import com.cn.leedane.exception.ParameterUnspecificationException;
import com.cn.leedane.springboot.SpringUtil;
import org.apache.log4j.Logger;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParser.Operator;
import org.apache.lucene.search.*;
import org.apache.lucene.store.SimpleFSDirectory;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求参数校验的工具类
 * @author LeeDane
 * 2019年11月29日 上午8:30:09
 * Version 1.0
 */
public class ParameterUnspecificationUtil {
	private static Logger logger = Logger.getLogger(MallUtil.class);
	private ParameterUnspecificationUtil(){

	}

	/**
	 * 校验空字符串
	 * @param str
	 * @return
	 */
	public static void checkNullString(String  str){
		if(StringUtil.isNull(str))
			throw new ParameterUnspecificationException("has null string parameter");
	}

	/**
	 * 校验空字符串
	 * @param str
	 * @param msg 字符串为空后的提示信息
	 */
	public static void checkNullString(String  str, String msg){
		if(StringUtil.isNull(str))
			throw new ParameterUnspecificationException(msg);
	}

	/**
	 * 校验long是否大于0
	 * @param l
	 * @param msg 字符串为空后的提示信息
	 */
	public static void checkLong(long  l, String msg){
		if(l < 1L)
			throw new ParameterUnspecificationException(msg);
	}

	/**
	 * 校验Object是否是null
	 * @param obj
	 * @param msg 字符串为空后的提示信息
	 */
	public static void checkObject(Object obj, String msg){
		if(obj == null)
			throw new ParameterUnspecificationException(msg);
	}

	/**
	 * 校验字符串是否是手机号码
	 * @param str
	 * @param msg 字符串为空后的提示信息
	 */
	public static void checkPhone(String  str, String msg){
		if(!StringUtil.isPhone(str))
			throw new ParameterUnspecificationException(msg);
	}

	/**
	 * 校验字符串是否是电子邮箱
	 * @param str
	 * @param msg 字符串为空后的提示信息
	 */
	public static void checkEmail(String  str, String msg){
		if(!StringUtil.isEmail(str))
			throw new ParameterUnspecificationException(msg);
	}

	/**
	 * 校验平台字段是否为空或者是不支持的平台
	 * @param platform
	 */
	public static void checkPlatform(String  platform){
		if(StringUtil.isNull(platform))
			throw new ParameterUnspecificationException("platform code must not null.");

		if(!MallUtil.inPlatform(platform))
			throw new ParameterUnspecificationException("not support platform .");
	}
}
