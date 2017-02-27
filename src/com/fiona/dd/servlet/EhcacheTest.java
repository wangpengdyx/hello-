package com.fiona.dd.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.fiona.dd.util.AjaxMessage;
import com.fiona.dd.util.LogFactory;
import com.fiona.ehcache.EhcacheManager;
import com.fiona.ehcache.EhcacheUtil;

public class EhcacheTest extends HttpServlet {
	
	private final Logger logger = LogFactory.getLog(this.getClass()) ;
	private EhcacheManager ehcacheManager = new EhcacheManager();

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void excute(HttpServletRequest request, HttpServletResponse response)
			throws Exception {
		String parameter = request.getParameter("param");  
		logger.info("输入参数param值为：" + parameter);

		response.setContentType("text/json; charset=UTF-8");  
//		String method = request.getMethod();
		StringBuffer resultBuffer = new StringBuffer();
		
		String ehcachResult = "" ;
		if(parameter.equals("1")){
			ehcachResult = ehcachWrite();
		}else if (parameter.equals("2")){
			ehcachResult = ehcachRemove();
		}else if(parameter.equals("3")){
			ehcachResult = ehcachClear();
		}else if(parameter.equals("4")){
			ehcachResult = ehcachRead();
		}else{
			ehcachResult = ehcachRead(parameter);
		}
			
		resultBuffer.append(ehcachResult);
//		resultBuffer.append("方法：" + method );
//		resultBuffer.append("输入参数param值：" + parameter);
		response.getOutputStream().write(resultBuffer.toString().getBytes("UTF-8"));  
	}

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		logger.info("进入doGet方法！");
		try {
			this.excute(request, response);
		} catch (Exception e) {
			logger.error(AjaxMessage.getErrorMsg(e));
		}
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		logger.info("进入doPost方法！");
		try {
			excute(request, response);
		} catch (Exception e) {
			logger.error(AjaxMessage.getErrorMsg(e));
		}
	}
	
	private String ehcachWrite(){
		EhcacheUtil.put("sacode", "sacode.1", "2");
		EhcacheUtil.put("sacode", "sacode.2", "3");
		EhcacheUtil.put("sacode", "sacode.3", "4");
		EhcacheUtil.put("saname", "saname.1", "2");
		EhcacheUtil.put("saname", "saname.2", "3");
		EhcacheUtil.put("saname", "saname.3", "4");
		
		return "缓存写入完成！";
	}
	
	private String ehcachRemove(){
		EhcacheUtil.remove("sacode", "sacode.2");
		EhcacheUtil.remove("saname", "saname.3");
		
		return "缓存sacode删除2，缓存saname删除3！";
	}
	
	private String ehcachClear(){
		EhcacheUtil.clear("sacode");
		
		return "缓存sacode清除全部！";
	}
	
	private String ehcachRead(){
		return this.ehcacheManager.getAliasString();
	}
	
	private String ehcachRead(String alias){
		return ehcacheManager.getKeyCacheString(alias);
	}

}
