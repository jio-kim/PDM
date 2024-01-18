package com.symc.remote.controller;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ssangyong.common.remote.DataSet;

@Controller
@RequestMapping("/remote/*")
public class RemoteController {
	
    /**
     * Login Page
     * 
     * @method login 
     * @date 2013. 6. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({"rawtypes", "cast"})
	@RequestMapping("/invoke")
    public void invoke(HttpServletRequest request, HttpServletResponse response) throws Exception {
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
	        {
	          aclass = (new Class[]
	          { Class.forName("com.ssangyong.common.remote.DataSet") });
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
	      System.out.println("Exception_LOG_1_ClassName :" + s);
	      System.out.println("Exception_LOG_1_Method :" + s1);
	      invocationtargetexception.printStackTrace();
	      objectoutputstream.writeObject(invocationtargetexception.getCause());
	    }
	    catch (Exception exception)
	    {
	      System.out.println("Exception_LOG_2_ClassName :" + s);
	      System.out.println("Exception_LOG_2_Method :" + s1);
	      exception.printStackTrace();
	      objectoutputstream.writeObject(exception);
	    }
    }
}
