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
package com.teamcenter.ets.translator.ugs.catparttocgr;

//==== Imports  =================================================================
import java.io.File;
import java.util.List;
import java.util.Properties;

import com.ssangyong.common.remote.DataSet;
import com.teamcenter.ets.load.DefaultDatabaseOperation;
import com.teamcenter.ets.soa.SoaHelper;
import com.teamcenter.soa.client.model.strong.Dataset;
import com.teamcenter.soa.client.model.strong.Group;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.User;
import com.teamcenter.translationservice.task.TranslationDBMapInfo;
import com.teamcenter.translationservice.task.TranslationDBMapInfoItem;

//==== Class ====================================================================
public class DatabaseOperation extends DefaultDatabaseOperation {

	private boolean isDebug = false;
	private Properties prop = null;
//	private static Registry registry = Registry
//			.getRegistry("com.teamcenter.ets.translator.ugs.catparttocgr.catparttocgr");

	protected void load(TranslationDBMapInfo zDbMapInfo, List<String> zFileList)
			throws Exception {

		prop = Util.getDefaultProperties();
		try {
			isDebug = new Boolean(prop.getProperty("isDebug"));
		} catch (Exception e) {
			m_zTaskLogger.info("Could not find 'isDebug' property.");
		}

		if( isDebug){
			m_zTaskLogger.info("m_scResultDir : " + m_scResultDir);
			m_zTaskLogger.info("zFileList.size() : " + zFileList.size());
		}

		try {
			
			//Dataset생성 후, CGR파일을 Dataset과 연결함.
			zDtSetHelper.createInsertDataset(sourceItemRev, sourceDataset,
					"CATCache", "IMAN_reference", "catcgr", m_scResultDir,
					zFileList, false);
			
			if( isDebug){
				m_zTaskLogger.info("Dataset 생성 및 CGR 파일 첨부 성공");
			}
			
		} catch (Exception e) {
			m_zTaskLogger.info("DataSet 등록 실패");
			e.printStackTrace();
		}

		try {
			
			saveFileInfo(zFileList);
			
		} catch (Exception e) {
			// FTP 전송 및 IF_PE_FILE_PATH 테이블에 저장 에러에 대해 처리 필요.
			e.printStackTrace();
			
		}
	} // end load()

	@Override
	public List<String> getResultFileList(TranslationDBMapInfo zDbMapInfo,
			String scResultFileType) {
		// TODO Auto-generated method stub
		if( isDebug){
			m_zTaskLogger.info("============ getResultFileList  실행 ============");
			
			int resultCount = zDbMapInfo.getTranslationDBMapInfoItemCount();
			
			m_zTaskLogger.info("============ resultCount = " + resultCount + " ============");
		}
		
		
		return super.getResultFileList(zDbMapInfo, scResultFileType);
	}

	@Override
	public void processTaskPost() throws Exception {
		// TODO Auto-generated method stub
		m_zTaskLogger.info("=============== processTaskPost  실행 ============");
		
		Dataset[] dataset = zDtSetHelper.getDatasets(sourceItemRev, "CATCache", "IMAN_reference");
		for (int i = 0; i < dataset.length; i++) {
			SoaHelper.getProperties(dataset[i], "object_type");
			String typeStr = ((Dataset) dataset[i]).get_object_type();
			if( typeStr.equalsIgnoreCase("CATCache")){
//				Dataset의 owner를 변경 필요.
				SoaHelper.getProperties(sourceItemRev, new String[]{"owning_user", "owning_group"});
		        User user = (User)sourceItemRev.get_owning_user();
		        Group group = (Group)sourceItemRev.get_owning_group();
		        SoaHelper.changeOwner(dataset[i], user, group)  ;  
			}
        }
		
		//에러가 발생 할 경우, IF_PE_FILE_PATH 테이블에 에러로 추가함.
		try{
			super.processTaskPost();
		}catch(Exception e){
			String servletUrlStr = prop.getProperty("servlet.url");
			String newFileName = sourceItemRev.get_item_id() + "_"
					+ sourceItemRev.get_item_revision_id() + ".cgr";
			saveToDB(servletUrlStr, "ERROR_FILE", newFileName, "TRANSLATION_FAIL");
			throw e;
		}
	}

	private void saveFileInfo(List<String> zFileList)
			throws Exception {

		String ip = prop.getProperty("cadFTP.ip");
		int port = Integer.parseInt(prop.getProperty("cadFTP.port"));
		String login = prop.getProperty("cadFTP.login");
		String pass = prop.getProperty("cadFTP.pass");
		String cadFtpPath = prop.getProperty("cadFTP.cadFtpPath");
		String servletUrlStr = prop.getProperty("servlet.url");

		if (isDebug) {
			m_zTaskLogger.info("FTP ip : " + ip);
			m_zTaskLogger.info("FTP port : " + port);
			m_zTaskLogger.info("FTP id : " + login);
			m_zTaskLogger.info("FTP pwd : " + pass);
			m_zTaskLogger.info("FTP cadFtpPath : " + cadFtpPath);
			m_zTaskLogger.info("servletUrlStr : " + servletUrlStr);
		}

		for (String path : zFileList) {

			String newFileName = sourceItemRev.get_item_id() + "_"
					+ sourceItemRev.get_item_revision_id() + ".cgr";
			File file = new File(m_scResultDir + File.separator + path);
			file.renameTo(new File(m_scResultDir + File.separator + newFileName));
			File newFile = new File(m_scResultDir + File.separator
					+ newFileName);
			
			boolean result = false;
			
			try{
				result = Util.uploadFtpFile(ip, port, login, pass, "/",
						cadFtpPath, newFile);
			}catch(Exception e){
				saveToDB(servletUrlStr, path, newFileName, "FTP_TRANSPER_ERROR");
				throw new Exception("FTP Upload Fail.", e);
			}
			
			if( isDebug ){
				m_zTaskLogger.info("FTP upload result[new = true, update or fail = false] : " + result);
			}
			
			try{
				saveToDB(servletUrlStr, path, newFileName, "SUCCESS");
			}catch(Exception e){
				throw new Exception("IF_PE_CGR_Path insert 실패", e);
			}
		}

	}

	/**
	 * CGR 생성 후 FTP에 올린 결과를 IE_PE_FILE_PATH 테이블에 등록한다.
	 * 
	 * @param servletUrlStr
	 *            호출 할 Web URL
	 * @param orgFileName
	 *            파일명 변경 전 이름.
	 * @param newFileName
	 *            ItemID_ItemRevID의 조합에 의한 새로운 이름.
	 * @throws Exception
	 */
	private void saveToDB(String servletUrlStr, String orgFileName,
			String newFileName, String stat) throws Exception {
		ItemRevision revision = sourceItemRev;
		String itemID = revision.get_item_id();
		String itemRevID = revision.get_item_revision_id();
		String itemName = revision.get_object_name();
		String typeStr = "CATCache";

		DataSet ds = new DataSet();
		ds.put("partNumber", itemID);
		ds.put("version", itemRevID);
		ds.put("partName", itemName);
		ds.put("filePath", newFileName);
		ds.put("fileType", typeStr);
		ds.put("realFileName", orgFileName);
		ds.put("stat", stat);

		if( isDebug ){
			m_zTaskLogger.info("servletUrlStr : " + servletUrlStr);
			m_zTaskLogger.info("ds : " + ds);
		}
		
		Util.execute(servletUrlStr,
				"com.symc.remote.service.TcInterfaceService", "createFilePath",
				ds, false);
	}
}
