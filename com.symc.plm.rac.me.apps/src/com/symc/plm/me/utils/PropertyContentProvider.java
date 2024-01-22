/**
 * 
 */
package com.symc.plm.me.utils;

import java.util.HashMap;

/**
 * Class Name : PropertyContentProvider
 * Class Description : 
 * @date 2013. 11. 19.
 *
 */
public class PropertyContentProvider extends HashMap<String, String>{
    
   /**
     * 
     */
    private static final long serialVersionUID = 1L;
    
    private String typeName ;
   
   PropertyContentProvider(String typeName){
       super();
       this.typeName = typeName;
   }
   
   public String getTypeName(){
       return typeName;
   }
   
   
}
