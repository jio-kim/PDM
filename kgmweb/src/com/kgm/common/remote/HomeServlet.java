package com.kgm.common.remote;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.kgm.common.remote.DataSet;

/**
 * [20160919][ymjang] 오류시 Log4j 로그 파일 기록하도록 개선
 * Servlet implementation class HomeServlet
 */
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	static Logger logger = Logger.getLogger(HomeServlet.class);
	
    /**
     * @see HttpServlet#HttpServlet()
     */
    public HomeServlet() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@SuppressWarnings({"rawtypes", "cast"})
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/octet-stream");

	    ServletOutputStream servletoutputstream = response.getOutputStream();
	    ObjectOutputStream objectoutputstream = new ObjectOutputStream(servletoutputstream);

	    ServletInputStream inputstream = request.getInputStream();
	    ObjectInputStream input = new ObjectInputStream(inputstream);

	    String s = null;
	    String s1 = null;
	    DataSet dataset = null;

	    try
	    {
	      dataset = (DataSet) input.readObject();
	      boolean flag = false;
	      Class aclass[] = null;
	      DataSet adataset[] = null;

	      if (dataset.size() > 0)
	      {
	        s = dataset.getString("class_name");
	        s1 = dataset.getString("method");
	        flag = dataset.getBoolean("blnReturn");

	        dataset.remove("class_name");
	        dataset.remove("method");
	        dataset.remove("blnReturn");
	        
	        if (dataset.size() > 0)
	        //if (dataset!=null && dataset.size() > 0)
	        {
	          aclass = (new Class[]
	          { Class.forName("com.kgm.common.remote.DataSet") });
	          adataset = (new DataSet[]
	          { dataset });
	        }
	      }

	      Method method = Class.forName(s).getMethod(s1, aclass);
	      Object obj = null;

	      if (flag)
	        obj = method.invoke(Class.forName(s).newInstance(), adataset);
	      else
	        method.invoke(Class.forName(s).newInstance(), adataset);

	      objectoutputstream.writeObject(obj);
	    }
	    catch (InvocationTargetException invocationtargetexception)
	    {
	    	System.out.println("Exception_LOG_1_Time : " + Calendar.getInstance().getTime());
	    	System.out.println("Exception_LOG_1_ClassName :" + s);
	    	System.out.println("Exception_LOG_1_Method :" + s1);
	    	
	    	logger.error("Exception_LOG_1_Time : " + Calendar.getInstance().getTime());
	    	logger.error("Exception_LOG_1_ClassName :" + s);
	    	logger.error("Exception_LOG_1_Method :" + s1);
	    	logger.error(invocationtargetexception.getMessage());
	    	
	    	invocationtargetexception.printStackTrace();
	    	objectoutputstream.writeObject(invocationtargetexception.getCause());
	    }
	    catch (Exception exception)
	    {
	    	System.out.println("Exception_LOG_2_Time : " + Calendar.getInstance().getTime());
	    	System.out.println("Exception_LOG_2_ClassName :" + s);
	    	System.out.println("Exception_LOG_2_Method :" + s1);

	    	logger.error("Exception_LOG_2_Time : " + Calendar.getInstance().getTime());
	    	logger.error("Exception_LOG_2_ClassName :" + s);
	    	logger.error("Exception_LOG_2_Method :" + s1);
	    	logger.error(exception.getMessage());
	    	
	    	/*
	    	if (s1 != null && s1.equalsIgnoreCase("getGbomlineListVec"))
	    	{
	    		if (dataset != null)
	    		{
	    			String modelCode = dataset.getString("modelCode");
	    			String plantCode = dataset.getString("plantCode");

	    			System.out.println("Model Code :" + modelCode);
	    			System.out.println("Plant Code :" + plantCode);
	    		}
	    	}
	    	*/
	    	exception.printStackTrace();
	    	objectoutputstream.writeObject(exception);
	    }
	}

}
