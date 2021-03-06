package com.cn.leedane.ueditor;

import java.io.File;

import com.cn.leedane.utils.ConstantsUtil;

/**
 * UeditorFolder存放文件夹
 * @author LeeDane
 * 2016年12月1日 下午1:31:40
 * Version 1.0
 */
public class UeditorFolder {
	public static UeditorFolder ueditorFolder;
	
	private UeditorFolder(){
		
	}
	
	public synchronized static UeditorFolder getInstance(){
		if(ueditorFolder == null){
			synchronized (UeditorFolder.class) {
				if(ueditorFolder == null)
					ueditorFolder = new UeditorFolder();
			}
		}
		return ueditorFolder;
	}

	public String getFileFolder(){
		StringBuffer rootPath = new StringBuffer();
		rootPath.append(ConstantsUtil.getDefaultSaveFileFolder());
		rootPath.append(com.cn.leedane.enums.FileType.UEDITOR.value);
		rootPath.append(File.separator);
		rootPath.append(com.cn.leedane.enums.FileType.IMAGE.value);
		return rootPath.toString();
	}
}
