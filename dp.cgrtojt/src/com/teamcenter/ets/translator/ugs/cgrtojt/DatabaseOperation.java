/*==============================================================================
 Copyright 2009.
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
================================================================================
File description:   This custom class is a cgrtojt specific sub class of the base
                    DatabaseOperation class which performs the loading operation
                    to Tc. This class stores results for translation requests.
                    This is a configuration specified class based on provider
                    name and translator name in DispatcherClient property file.

        Filename:   DatabaseOperation.java
=================================================================================*/

//==== Package  =================================================================
package com.teamcenter.ets.translator.ugs.cgrtojt;

//==== Imports  =================================================================
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.httpclient.HttpState;

import com.teamcenter.ets.load.DefaultDatabaseOperation;

import com.teamcenter.ets.request.TranslationRequest;
import com.teamcenter.ets.soa.Credentials;
import com.teamcenter.ets.soa.SOAExceptionHandler;
import com.teamcenter.ets.util.DataSetHelper;
import com.teamcenter.ets.util.Registry;

import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.SoaConstants;
import com.teamcenter.soa.client.Connection;
import com.teamcenter.soa.client.CredentialManager;
import com.teamcenter.soa.client.ExceptionHandler;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.translationservice.task.TranslationDBMapInfo;

//==== Class ====================================================================
public class DatabaseOperation extends DefaultDatabaseOperation {
   
   private static Registry registry = Registry.getRegistry("com.teamcenter.ets.translator.ugs.cgrtojt.cgrtojt");
   
   protected void load(TranslationDBMapInfo zDbMapInfo, List<String> zFileList) throws Exception {
      
      Dataset dataset = (Dataset) this.primaryObj;
      
      // Connect Server
      String className = registry.getString("Class.Name");
      // 아래와 같이 시스템 환경변수를 사용되지 않음.
      // String tcRoot = System.getProperty("TC_ROOT");
      // String tcRoot = System.getProperty("PATH");
      
      String classPath = Util.class.getProtectionDomain().getCodeSource().getLocation().getPath();
      
      StringBuffer sb = new StringBuffer(classPath);
      
      Integer idx = sb.indexOf("DispatcherClient");
      
      String dpRoot = classPath.substring(1, idx);
      
      String transPath = dpRoot + "Module" + File.separator + "Translators";
      
      String path = transPath + File.separator + className + File.separator + className + ".properties";
      
      Properties prop = new Properties();
      
      prop.load(new FileInputStream(path));
      
      String hostPath = prop.getProperty("Tc.Host");
      
      CredentialManager credentialMgr = new Credentials();
      
      String userName = prop.getProperty("User.Name");
      
      String userPassword = prop.getProperty("User.Password");
      
      credentialMgr.setUserPassword(userName, userPassword, "");
      ExceptionHandler expHandler = new SOAExceptionHandler();
      
//      Connection connection = new Connection(hostPath, new HttpState(), credentialMgr, SoaConstants.REST, SoaConstants.HTTP, true);
      Connection connection = new Connection(hostPath, credentialMgr, SoaConstants.REST, SoaConstants.HTTP);
      connection.setExceptionHandler(expHandler);
      
      DataManagementService dataMgtService = DataManagementService.getService(connection);
      
      ServiceData serviceData = dataMgtService.getProperties(new ModelObject[] { dataset }, new String[] { "ref_list", "object_name" });
      dataset = (Dataset) serviceData.getPlainObject(0);
      
      sourceDataset = dataset;
      
      Util ut = new Util();
      String s = "";
      
      for (int i = 0; i < zFileList.size(); i++) {
         s += "\n" + zFileList.get(i);
      }
      
      ut.setLog(s);
      
      loadPart(zFileList);
      
   } // end load()
   
   protected void loadPart(List<String> zFileList) throws Exception {
      zDtSetHelper.createInsertDataset(sourceItemRev, sourceDataset, DataSetHelper.TC_DS_TYPE_DIRECT_MODEL, DataSetHelper.TC_REL_TYPE_RENDERING, DataSetHelper.TC_NR_TYPE_JT_PART, m_scResultDir, zFileList, false);
   } // loadPart
}
