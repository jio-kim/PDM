/**
 *[ SR180410-037 ]
 * - 2018-04-19
 * - beenlaho 
 */

package com.kgm.commands.namegroup.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.kgm.commands.namegroup.model.SpecOptionChangeInfo;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;

public class PartNameGroupDao {

	private SYMCRemoteUtil remoteQuery;
	
	private DataSet ds;
	
	
	public static final String PART_NAME_GROUP_SERVICE_CLASS = "com.kgm.service.PartNameGroupService";//com.kgm.dao.ECOInfoDao·Î ÇØµµ µÊ
	
	public PartNameGroupDao (){
		this.remoteQuery = new SYMCRemoteUtil();
	}
	
	
   public ArrayList<SpecOptionChangeInfo> getSpecOptionChangeInfo( Set<String> specSet ) throws Exception {
       
	   DataSet dataSet = new DataSet();
	   
	   ArrayList<SpecOptionChangeInfo> returnValues = new ArrayList<SpecOptionChangeInfo>();
	   
	   Iterator<String> iteration = specSet.iterator();
	   ArrayList<String> specList = new ArrayList<String>();

	   for (Iterator iterator = specSet.iterator(); iterator.hasNext();) {
		   
		   String specNo = (String) iterator.next();
		   specList.add( specNo );
	   
	   }
		                                                                                                                                 //getSpecOptionChangeInfo
	   dataSet.put("specList", specList);
	   
	   ArrayList<HashMap<String, String>> result = (ArrayList<HashMap<String, String>>)remoteQuery.execute(PART_NAME_GROUP_SERVICE_CLASS, "getSpecOptionChangeInfo", dataSet);
       
	   for( HashMap<String, String> data : result ){
		   
		   SpecOptionChangeInfo tmp = new SpecOptionChangeInfo( data );
		   
		   System.out.println( tmp );
		   
		   returnValues.add( tmp );
		   
		   
	   }
	   
	   return returnValues;
   }
	

}
