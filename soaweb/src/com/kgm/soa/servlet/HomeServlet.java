package com.kgm.soa.servlet;

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

import com.kgm.common.remote.DataSet;

/**
 * Servlet implementation class HomeServlet
 */
public class HomeServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
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

	      //System.out.println("[S : " + Calendar.getInstance().getTime() + " ] " +  s + "." + s1);
	      objectoutputstream.writeObject(obj);
	      //System.out.println("[E : " + Calendar.getInstance().getTime() + " ] " +  s + "." + s1);
	    }
	    catch (InvocationTargetException invocationtargetexception)
	    {
	    	System.out.println("Exception_LOG_1_Time : " + Calendar.getInstance().getTime());
	    	System.out.println("Exception_LOG_1_ClassName :" + s);
	    	System.out.println("Exception_LOG_1_Method :" + s1);
	    	invocationtargetexception.printStackTrace();
	    	objectoutputstream.writeObject(invocationtargetexception.getCause());
	    }
	    catch (Exception exception)
	    {
	    	System.out.println("Exception_LOG_2_Time : " + Calendar.getInstance().getTime());
	    	System.out.println("Exception_LOG_2_ClassName :" + s);
	    	System.out.println("Exception_LOG_2_Method :" + s1);
	    	exception.printStackTrace();
	    	objectoutputstream.writeObject(exception);
	    }
	}

}
