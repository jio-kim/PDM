package com.symc.common.util;

import javax.servlet.ServletContext;

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.context.ContextLoader;

public class ContextUtil {
    public static ApplicationContext appContext = null;
    public static ServletContext sc = null;
    
    public static Object getBean(String id, boolean isWeb) throws Exception{
        return getBean(id);
    }
    
    public static Object getBean(String id) throws Exception{
        Object obj = null;
        
        if(appContext == null){
            //String attr = FrameworkServlet.SERVLET_CONTEXT_PREFIX + "action"; 
            appContext = ContextLoader.getCurrentWebApplicationContext();
        }
                
        if(appContext != null) {
            //java.beans.Introspector.flushCaches();
            obj =  appContext.getBean(id);  
        }else{          
            if(appContext == null){

//              System.out.println(sun.reflect.Reflection.getCallerClass(2)); 
                appContext = new ClassPathXmlApplicationContext(
                        new String[] {"/spring/context-*.xml"});
            }
            BeanFactory factory = (BeanFactory) appContext;
            obj = factory.getBean(id);
        }
        return obj;
    }
}
