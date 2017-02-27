package com.fiona.dd.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LogFactory {
	public static Logger getLog(Class<?> clazz) {
		return LoggerFactory.getLogger(clazz);
	}

	public static Logger getLog(String logName) {
		return LoggerFactory.getLogger(logName);
	}
	
}
