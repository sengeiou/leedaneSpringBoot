package com.cn.leedane.springboot;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

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
		Map<String, Object> message = new HashMap<String, Object>();
        message.put("isSuccess", false);        
        StringPrintWriter strintPrintWriter = new StringPrintWriter();  
        exception.printStackTrace(strintPrintWriter);
        logger.info(strintPrintWriter.getString());
        message.put("message", /*"服务器异常"*/strintPrintWriter.getString());//将错误信息传递给view  
        
        JSONObject jsonObject = JSONObject.fromObject(message);
		response.setCharacterEncoding("utf-8");
		System.out.println("服务器返回:"+jsonObject.toString());
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
		return null;
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
