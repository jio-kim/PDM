package com.kgm.soa.common.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class PropUtil
{
    public PropUtil()
    {
    }
        
    /**
     * Property Value Read
     * @param key
     */
    public static String getPropValue(String key)
    {
    	Properties properties = new Properties();        
    	try 
    	{            
    		properties.load(new FileInputStream("com/ssangyong/common/properties/common.properties"));         
    	} catch (FileNotFoundException e) 
    	{            
    		e.printStackTrace();        
    	} catch (IOException e) {            
    		e.printStackTrace();        
    	}
    	
    	return properties.getProperty(key);
    }
    
}
