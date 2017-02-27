package com.fiona.dd.log;

/*
 * Copyright 2013 Yann Le Tallec.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.MongoException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Marker;
 
/**
 * A logback appender that uses Mongo to log messages.
 * <p>
 * Typical configuration:
 * <pre> {@code
 * <appender name=\"TEST\" class=\"org.extendedmind.logback.MongoDBAppender\">
 *     <host>192.168.1.1</host>
 *     <port>27017</port>
 *     <db>log</db>
 *     <collectionPrefix>log-</collectionPrefix>
 * </appender>
 * } </pre>
 * The log messages have the following JSON format (the {@code marker}, {@code exception} and {@code stacktrace} fields are optional):
 * <pre> {@code
 * { "_id" : ObjectId("514b2d529234d98131221578"),
 *   "t" : ISODate("2013-03-21T15:54:58.357Z"),
 *   "d" : {
 *     "logger": "org.extendedmind.logback.MongoDBAppenderTest",
 *     "mdc" : {
 *       "user": "12345"
 *     },
 *     "marker" : "Marker",
 *     "message" : "An error occurend in the test",
 *     "exception" : "java.lang.RuntimeException: java.lang.Exception",
 *     "stacktrace" : [ "at com.assylias.logging.MongoAppenderTest.testCausedBy(MongoAppenderTest.java:129) ~[test-classes/:na]",
 *                    "at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[na:1.7.0_17]",
 *                    "at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57) ~[na:1.7.0_17]",
 *                    "Caused by: java.lang.Exception: null",
 *                    "at com.assylias.logging.MongoAppenderTest.testCausedBy(MongoAppenderTest.java:126) ~[test-classes/:na]",
 *                    "... 20 common frames omitted" ],
 *
 *   }
 * } } </pre>
 * If an error occurs while logging, the message data might also contain a {@code logging_error} field:
 * <pre> {@code
 *    "logging_error" : "Could not log all the event information: com.mongodb.MongoInterruptedException: A driver operation has been interrupted
 *                       at com.mongodb.DBPortPool.get(DBPortPool.java:216)
 *                       at com.mongodb.DBTCPConnector$MyPort.get(DBTCPConnector.java:440)
 *                       ..."
 * } </pre>
 */
public class MongoDBAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {
 
    private String host;
    private String uri;
    private int port;
    private String db;
    private Map<String, DBCollection> logCollectionMap = new HashMap<String, DBCollection>();
    private DB mongoDb = null;
    
    public MongoDBAppender() {
    }
 
    public DBCollection getLogCollection(String level){
    	DBCollection logCollection = logCollectionMap.get(level);
    	if (logCollection == null){
    		logCollection = mongoDb.getCollection(level.toLowerCase() + "_events");
    		logCollectionMap.put(level, logCollection);
    	}
    	return logCollection;
    }
    
    @Override
    public void start() {
        try {
            connect();
            super.start();
        } catch (UnknownHostException | MongoException e) {
            addError("Can't connect to mongo: host=" + host + ", port=" + port, e);
        }
    }
 
    @SuppressWarnings({ "resource", "deprecation" })
	private void connect() throws UnknownHostException {
    	MongoClient client;
    	if (this.uri == null){
	        client = new MongoClient(host, port);
    	}else{
    		client = new MongoClient(new MongoClientURI(this.uri));
    	}
        mongoDb = client.getDB(db == null ? "log" : db);
    }
 
    @Override
    protected void append(ILoggingEvent evt) {
        if (evt == null) return; //just in case
        String level = String.valueOf(evt.getLevel());
        DBObject log = getBasicLog(evt);
        try {
            logException(evt.getThrowableProxy(), log);
            getLogCollection(level).insert(log);
        } catch (Exception e) {
            try {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                log.put("logging_error", "Could not log all the event information: " + sw.toString());
                getLogCollection(level).insert(log);
            } catch (Exception e2) { //really not working
                addError("Could not insert log to mongo: " + evt, e2);
            }
        }
    }
 
    private DBObject getBasicLog(ILoggingEvent evt) {
        DBObject log = new BasicDBObject();
        log.put("t", new Date(evt.getTimeStamp()));

        // Store everything else inside a datamap
        Map<String, Object> dataMap = new HashMap<String, Object>();
        dataMap.put("logger", evt.getLoggerName());
        Marker m = evt.getMarker();
        if (m != null) {
        	dataMap.put("marker", m.getName());
        }
        Map<String, String> mdcMap = evt.getMDCPropertyMap();
        if (!mdcMap.isEmpty()){
          Map<String, Object> mongoMdcMap = new HashMap<String, Object>();
          // Try to save a numeric value as Int
          Iterator<Entry<String,String>> it = mdcMap.entrySet().iterator();          
          while (it.hasNext()) {
        	Entry<String,String> pairs = it.next();        	
        	if (isNumeric(pairs.getValue())){
        		try{
        			Integer intValue = Integer.parseInt(pairs.getValue());
        			mongoMdcMap.put(pairs.getKey(), intValue);
        		}catch(NumberFormatException nfe){  
        			mongoMdcMap.put(pairs.getKey(), pairs.getValue());
        		}
        	}else{
        		mongoMdcMap.put(pairs.getKey(), pairs.getValue());
        	}
            it.remove(); // avoids a ConcurrentModificationException
          }
          
          dataMap.put("mdc", mongoMdcMap);
        }
        dataMap.put("message", evt.getFormattedMessage());
        
        log.put("d", dataMap);
        return log;
    }
 
    @SuppressWarnings("unchecked")
	private void logException(IThrowableProxy tp, DBObject log) {
        if (tp == null) return;
        String tpAsString = ThrowableProxyUtil.asString(tp); //the stack trace basically
        List<String> stackTrace = Arrays.asList(tpAsString.replace("\t","").split(CoreConstants.LINE_SEPARATOR));
        if (stackTrace.size() > 0) {
    		Map<String, Object> dataObject = (Map<String, Object>) log.get("d");
    		dataObject.put("exception", stackTrace.get(0));
            if (stackTrace.size() > 1) {
            	dataObject.put("stacktrace", stackTrace.subList(1, stackTrace.size()));
            }
    		log.put("d", dataObject);        	
        }
    }
 
    public String getUri() {
        return this.uri;
    }
 
    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getHost() {
        return host;
    }
 
    public void setHost(String host) {
        this.host = host;
    }
    
    public int getPort() {
        return port;
    }
 
    public void setPort(int port) {
        this.port = port;
    }
 
    public String getDb() {
        return db;
    }
 
    public void setDb(String db) {
        this.db = db;
    }
    
    private boolean isNumeric(String str){
		for (char c : str.toCharArray()){
	        if (!Character.isDigit(c)) return false;
	    }
	    return true;
    }
 }