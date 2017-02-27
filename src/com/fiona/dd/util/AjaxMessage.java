package com.fiona.dd.util;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import com.alibaba.fastjson.JSONObject;

public class AjaxMessage
{
  private static Logger _Logger = LogFactory.getLog(AjaxMessage.class);

  public static void write(HttpServletResponse response, Object msg)
  {
    ServletOutputStream out = null;
    response.setContentType("text/html;charset=utf-8");
    try {
      if ((msg == null) || ("".equals(msg))) {
        msg = "{success:false,msg:'没有返回值！'}";
      }
      if ((msg instanceof List)) {
        msg = JsonUtil.array2json((List<?>)msg);
      }

      out = response.getOutputStream();
      out.write(msg.toString().getBytes("UTF-8"));
      _Logger.debug("AjaxMessage:" + msg);
    } catch (Exception e) {
      e.printStackTrace();

      if (out != null)
        try {
          out.flush();
          out.close();
        } catch (IOException e1) {
          e1.printStackTrace();
        }
    }
    finally
    {
      if (out != null)
        try {
          out.flush();
          out.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
    }
  }

  public static String getErrorMsg(Exception e)
  {
    try
    {
      if ((e instanceof SQLException)) {
        SQLException s = (SQLException)e;
        switch (s.getErrorCode()) {
        case 904:
          return "[ORACLE]缺少字段，请更新最新表结构！<br />详情：" + s.getMessage();
        case 942:
          return "[ORACLE]表或视图不存在，请更新最新表结构！<br />详情：" + s.getMessage();
        case 1400:
          return "[ORACLE]数据不能为空，请仔细检查！<br />详情：" + s.getMessage();
        case 936:
          return "[ORACLE]缺少表达式，SQL语句不正确！<br />详情：" + s.getMessage();
        case 933:
          return "[ORACLE]SQL命令未正常结束，请检查SQL语句。<br />详情：" + s.getMessage();
        case 1722:
          return "[ORACLE]无效数字，数字列不能输入非数字字符，请检查。<br />详情：" + s.getMessage();
        case 1422:
          return "[ORACLE]子查询返回多个结果，请检查SQL语句。<br />详情：" + s.getMessage();
        case 7104:
          return "[ORACLE]字符串超长，请检查。<br />详情：" + s.getMessage();
        }
        return "[ORACLE]SQL语句发生错误，请检查。<br />详情：" + s.getMessage();
      }

      if ((e instanceof NullPointerException)) {
        String message = "发生空指针错误：";
        StackTraceElement[] el = e.getStackTrace();
        for (int i = 0; i < el.length; i++) {
          message = message + "<br />文件名：" + el[i].getFileName() + "，行号：" + el[i].getLineNumber() + "，类名：" + el[i].getClass() + "，方法名：" + el[i].getMethodName() + "；";
        }

        return message;
      }

      Throwable throwable = e.getCause();
      String message = e.getMessage();
      return getErrorMsg(throwable, message);
    } catch (Exception e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  public static String getErrorMsg(Throwable throwable, String pMessage) {
    try {
      String message = pMessage;
      Throwable throwableTmp = throwable;

      if ((throwableTmp instanceof SQLException)) {
    	  SQLException s = (SQLException)throwableTmp;
        return getErrorMsg(s);
      }

      while (throwableTmp != null)
      {
        if ((throwableTmp instanceof NullPointerException)) {
          NullPointerException s = (NullPointerException)throwableTmp;
          return getErrorMsg(s);
        }
        message = throwableTmp.getMessage();
        if ((message == null) || ("".equals(message.trim())))
          message = pMessage;
        else {
          pMessage = message;
        }
        throwableTmp = throwableTmp.getCause();
      }

      if ((message == null) || ("".equals(message.trim()))) {
        if (throwable != null) throwable.printStackTrace();
        message = "后台程序发生不可知错误!";
      }
      return stringConvert(message);
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  public static String getErrorMsg(Throwable throwable) {
    return getErrorMsg(throwable, throwable != null ? throwable.getMessage() : null);
  }

  public static String setErrorMsg(Object msg)
  {
    try
    {
      return setErrorMsg(msg, Boolean.valueOf(true));
    } catch (Exception e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  public static String setErrorMsg(Object msg, Boolean suspend)
  {
    try
    {
      if (msg != null) {
        if ((msg instanceof List))
          msg = JsonUtil.array2json((List<?>)msg);
        else {
          msg = stringConvert((String)msg);
        }
      }
      JSONObject obj = new JSONObject();
      obj.put("success", false);
      obj.put("msg", msg == null ? "" : msg);
      if (!suspend.booleanValue()) obj.put("suspend", false);
      return obj.toString();
    } catch (Exception e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  public static String stringConvert(String msg) {
    try {
      if ((msg == null) || ("".equals(msg))) return msg;

      msg = msg.replace("\"", "“");
      msg = msg.replace("'", "‘");
      msg = msg.replace("\r\n", "<br>");
      msg = msg.replace("\n", "<br>");

      if (msg.startsWith("task already taken by")) {
        msg = "任务已经被" + msg.substring("task already taken by".length()) + " 接收！";
      }

      msg = msg.replace("resource ", "资源文件 ");
      msg = msg.replace("does not exist", "不存在");
      return msg.replace("Cannot find property", "没有设置参数");
    }
    catch (Exception e1)
    {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  private static String errorMsg(Exception e)
  {
    try
    {
      String message = getErrorMsg(e);

      return setErrorMsg(message);
    }
    catch (Exception e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  public static void errorMsg(Exception e, HttpServletResponse response, Object msg)
  {
    try
    {
      String json = "";

      if (msg != null) {
        if ((msg instanceof List)) {
          msg = JsonUtil.array2json((List<?>)msg);
        }
        String message = getErrorMsg(e) + "<br>" + msg;
        json = setErrorMsg(message);
      } else {
        json = errorMsg(e);
      }write(response, json);
    } catch (Exception e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  public static void errorMsg(Exception e, HttpServletResponse response)
  {
    try
    {
      errorMsg(e, response, null);
    } catch (Exception e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }

  public static void errorMsg(HttpServletResponse response, Object msg)
  {
    try {
      write(response, setErrorMsg(msg));
    } catch (Exception e1) {
      e1.printStackTrace();
      throw new RuntimeException(e1);
    }
  }
}
