package com.cn.leedane.controller;

import com.cn.leedane.model.FilePathBean;
import com.cn.leedane.model.UploadBean;
import com.cn.leedane.model.UserBean;
import com.cn.leedane.service.FilePathService;
import com.cn.leedane.service.UploadService;
import com.cn.leedane.utils.*;
import com.cn.leedane.utils.EnumUtil.DataTableType;
import com.cn.leedane.utils.EnumUtil.ResponseCode;
import net.sf.json.JSONObject;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = ControllerBaseNameUtil.fp)
public class FilePathController extends BaseController{

	private Logger logger = Logger.getLogger(getClass());

	//上传filePath表的service
	@Autowired
	private FilePathService<FilePathBean> filePathService;
	
	@Autowired
	private UploadService<UploadBean> uploadService;
	

	/**
	 * 分页获取指定的图片列表
	 * @return
	 */
	@RequestMapping(value = "/userImages", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> getUserImagePaging(Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();
		
		checkRoleOrPermission(model, request);;
		List<Map<String, Object>> result= filePathService.getUserImageByLimit(getJsonFromMessage(message), getUserFromMessage(message), getHttpRequestInfo(request));
		logger.info("获得文件路径的数量：" +result.size());
		message.put("success", true);
		message.put("message", result);
		return message.getMap();
	}
	
	/**
     * 合并单个文件
     * 对断点上传的文件进行合并
     * @return
	 * @throws IOException 
     */
	@RequestMapping(value = "/mergePortFile", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
    public Map<String, Object> mergePortFile(Model model, HttpServletRequest request) throws IOException {
    	ResponseMap message = new ResponseMap();
        
    	if(!checkParams(message, request))
			return message.getMap();
		
    	checkRoleOrPermission(model, request);;
    	
    	JSONObject json = getJsonFromMessage(message);
    	UserBean user = getUserFromMessage(message);
    	String fileName = JsonUtil.getStringValue(json, "fileName");//必须
    	String tableUuid = JsonUtil.getStringValue(json, "uuid");  //客户端生成的唯一性uuid标识符
    	String tableName = JsonUtil.getStringValue(json, "tableName");  //客户端生成的唯一性uuid标识符
    	int order = JsonUtil.getIntValue(json, "order", 0); //多张图片时候的图片的位置，必须，为空默认是0	
    	String version = JsonUtil.getStringValue(json, "file_version");  //文件版本信息
    	String desc = JsonUtil.getStringValue(json, "file_desc"); //文件的描述信息
        
    	
    	//只有APP_Version才需要版本号和版本描述信息
    	if(ConstantsUtil.UPLOAD_APP_VERSION_TABLE_NAME.equalsIgnoreCase(tableName)){
			if(StringUtil.isNull(version) || StringUtil.isNull(desc)){
				message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.某些参数为空.value));
				message.put("responseCode", EnumUtil.ResponseCode.某些参数为空.value);
				return message.getMap();
			}
		}
        //filePathService.merge(tableUuid, tableName, order, user, request);
        List<Map<String, Object>> list = uploadService.getOneUpload(tableUuid, tableName, order, user, getHttpRequestInfo(request));
        if(list != null && list.size() >0){
        	
        	StringBuffer sourcePath = new StringBuffer();
        	sourcePath.append(ConstantsUtil.getDefaultSaveFileFolder());
        	sourcePath.append("temporary");
			sourcePath.append(File.separator);
        	sourcePath.append(user.getId());
        	sourcePath.append("_");
        	sourcePath.append(tableUuid);
        	sourcePath.append("_");
        	sourcePath.append(DateUtil.DateToString(new Date(), "yyyyMMddHHmmss"));
        	sourcePath.append("_");
        	sourcePath.append(fileName);
        	
        	File sourceFile = new File(sourcePath.toString());
        	if(sourceFile.exists()){
        		sourceFile.deleteOnExit();
        		sourceFile.createNewFile();
        	}else{
        		sourceFile.createNewFile();
        	}
        	
        	//保存合并后的输出对象
        	FileOutputStream out = new FileOutputStream(sourceFile);
        	for(Map<String, Object> map: list){
        		if(!FileUtil.readFile(StringUtil.changeNotNull(map.get("path")), out))
        			return message.getMap();
        		
        	}
        	//合并后关闭流
        	if(out != null){
        		out.flush();
        		out.close();
        	}
        	message.put("success", filePathService.saveSourceAndEachFile(sourcePath.toString(), user, tableUuid, tableName, order, version, desc));
    		return message.getMap();
        }else{
        	message.put("message", ResponseCode.没有操作实例.value);
        	message.put("responseCode", EnumUtil.getResponseValue(ResponseCode.没有操作实例.value));
    		return message.getMap();
        }
    }
    
    /**
     * 合并成功后删除片段的临时文件
     * @return
     */
	@RequestMapping(value = "/portFile", method = RequestMethod.DELETE, produces = {"application/json;charset=UTF-8"})
    public Map<String, Object> deletePortFile(Model model, HttpServletRequest request) {
		ResponseMap message = new ResponseMap();
        
    	if(!checkParams(message, request))
			return message.getMap();
		
    	checkRoleOrPermission(model, request);;
    	
    	JSONObject json = getJsonFromMessage(message);
    	UserBean user = getUserFromMessage(message);
    	String tableUuid = JsonUtil.getStringValue(json, "uuid"); //客户端生成的唯一性uuid标识符
    	String tableName = JsonUtil.getStringValue(json, "tableName");  //客户端生成的唯一性uuid标识符
    	if(tableName.equalsIgnoreCase(DataTableType.用户.value)){
    		logger.info("更新用户的头像的缓存数据");
    		userHandler.updateUserPicPath(user.getId(), "30x30");
    	}
    	int order = JsonUtil.getIntValue(json, "order", 0); //多张图片时候的图片的位置，必须，为空默认是0	
        List<Map<String, Object>> list = uploadService.getOneUpload(tableUuid, tableName, order, user, getHttpRequestInfo(request));
        message.put("success", true);
        
        if(list != null && list.size() >0){   	
        	logger.info("删除的文件的数量："+list.size());
        	UploadBean upload;
        	for(Map<String, Object> map: list){
    			upload = new UploadBean();
    			upload.setTableName(tableName);
    			upload.setTableUuid(tableUuid);
    			upload.setfOrder(order);
    			upload.setSerialNumber(StringUtil.changeObjectToInt(map.get("serial_number")));
    			upload.setPath(String.valueOf(map.get("path")));
    			if(!uploadService.cancel(upload, user, getHttpRequestInfo(request)))
    				return message.getMap();
    			
        	}
        	message.put("success", true);
    		return message.getMap();
        }else{
        	logger.error("删除的文件的数量为0");
        }
        logger.error("删除断点片段文件发生异常");
        
        message.put("message", EnumUtil.getResponseValue(EnumUtil.ResponseCode.服务器处理异常.value));
		message.put("responseCode", EnumUtil.ResponseCode.服务器处理异常.value);
		return message.getMap();
    }
    
    /**
     * 分页获取上传的文件
     * @return
     */
	@RequestMapping(value = "/paths", method = RequestMethod.GET, produces = {"application/json;charset=UTF-8"})
    public Map<String, Object> paging(Model model, HttpServletRequest request) {
    	ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();
		
		checkRoleOrPermission(model, request);;
		
		message.putAll(filePathService.getUploadFileByLimit(getJsonFromMessage(message), getUserFromMessage(message), getHttpRequestInfo(request)));
		return message.getMap();
    }
	
	/**
	 * 上传用户头像链接
	 * @return
	 */
	@RequestMapping(value = "/uploadHeadImgLink", method = RequestMethod.POST, produces = {"application/json;charset=UTF-8"})
	public Map<String, Object> uploadUserHeadImageLink(Model model, HttpServletRequest request){
		ResponseMap message = new ResponseMap();
		if(!checkParams(message, request))
			return message.getMap();
		
		checkRoleOrPermission(model, request);;
		
		message.putAll(userService.uploadUserHeadImageLink(getJsonFromMessage(message), getUserFromMessage(message), getHttpRequestInfo(request)));
		return message.getMap();
	}
}
