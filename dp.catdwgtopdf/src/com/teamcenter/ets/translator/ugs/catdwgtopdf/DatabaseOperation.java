/*==============================================================================
 Copyright 2009.
 Siemens Product Lifecycle Management Software Inc.
 All Rights Reserved.
================================================================================
File description:   This custom class is a catdwgtopdf specific sub class of the base
                    DatabaseOperation class which performs the loading operation
                    to Tc. This class stores results for translation requests.
                    This is a configuration specified class based on provider
                    name and translator name in DispatcherClient property file.

        Filename:   DatabaseOperation.java
=================================================================================*/

//==== Package  =================================================================
package com.teamcenter.ets.translator.ugs.catdwgtopdf;

//==== Imports  =================================================================
import java.util.List;

import com.teamcenter.ets.load.DefaultDatabaseOperation;

import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.ets.util.DataSetHelper;

import com.teamcenter.services.strong.core.DataManagementService;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.ServiceData;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.translationservice.task.TranslationDBMapInfo;

//==== Class ====================================================================
public class DatabaseOperation extends DefaultDatabaseOperation {
   
   protected void load(TranslationDBMapInfo zDbMapInfo, List<String> zFileList) throws Exception {
      Dataset dataset = (Dataset) this.primaryObj;
      
      DataManagementService dataMgtService = DataManagementService.getService(SoaHelper.getSoaConnection());
      
      ServiceData serviceData = dataMgtService.getProperties(new ModelObject[] { dataset }, new String[] { "ref_list", "object_name" });
      dataset = (Dataset) serviceData.getPlainObject(0);
      
      sourceDataset = dataset;
      
      zDtSetHelper.createInsertDataset(sourceItemRev, sourceDataset, "PDF", DataSetHelper.TC_REL_TYPE_SPECIFICATION, "PDF_Reference", m_scResultDir, zFileList, false);
      
   } // end load()
}
