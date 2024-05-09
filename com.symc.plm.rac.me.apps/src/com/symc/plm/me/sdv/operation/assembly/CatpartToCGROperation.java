package com.symc.plm.me.sdv.operation.assembly;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.ProgressBar;
import com.symc.plm.me.common.LogFileUtility;
import com.symc.plm.me.sdv.operation.AbstractTCSDVOperation;
import com.symc.plm.me.sdv.service.resource.DatasetUtilities;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.ets.external.RequestResult;
import com.teamcenter.rac.ets.soa.DispatcherSoaProxy;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

/**
 *  [Non-SR] ���� �ڵ�ȭ ���α׷� �߰� CATPart To CGR
 *  2018�� 10�� ���� �ڵ�ȭ ���α׷� ���� 
 *  Demon(SYMC Web) ���� CATPart --> CGR �� ��ȯ���� ���� �׸��� DB ������ ���� �Ͽ� 
 *  ������ ���� ���� ������ ���� CGR ������ ���� ���� �ϰ� �� ��ȯ ��û �ϴ� ���α׷�
 *  ��ȯ ��û�� ��ϰ� ��û ���� ������ C:\\Temp ������ Simple_���ó�¥_CGRFileTransform.txt �� ���� �Ǿ� ����
 *  
 * @author 178650
 *
 */
public class CatpartToCGROperation extends AbstractTCSDVOperation {

	// ssangyong Web�� ���� ���� ������ ���� ��ü ����
	private SYMCRemoteUtil remoteQuery;
	// sam ���� ���� ���
	
	ArrayList<HashMap<String, Object>> specApplicationResultList = new ArrayList<HashMap<String,Object>>();
	
	ArrayList<String> notDuplicateItemId = new ArrayList<String>();
	
    
    @Override
    public void executeOperation() throws Exception {
    	// ���� ��ü ����
    	 this.remoteQuery = new SYMCRemoteUtil();
    	// ������ �Էµ� parameter ���� 
    	DataSet ds = new DataSet();
    	
     	 try{
            ds.put("JOB_PUID", "");
	     	ArrayList<HashMap<String, Object>> resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute("com.kgm.service.SMTestService", "getNotUploadCGRFile", ds);
	    	uploadCGRFile(resultList);
	    	
	    	MessageBox.post(AIFUtility.getActiveDesktop(), "��ȯ ��û�� �Ϸ� �Ǿ����ϴ�.\n C:\\Temp ������ Simple_���ó�¥_CGRFileTransform.txt �� ���� �Ǿ� �ֽ��ϴ�.", "INFORMATION", MessageBox.INFORMATION);
        } catch( Exception e) {
        	e.getStackTrace();
        	MessageBox.post(AIFUtility.getActiveDesktop(), "��ȯ ���� ������ �߻� �Ͽ����ϴ�. \n �����ڿ��� ���� �ϼ���.", "INFORMATION", MessageBox.INFORMATION);
        } 
     	
     	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    
    	
    }// execute ��
    
    
//    
    
////    
////    
////        
////		
////    
////    
////    
////    
////    // , A1.SUPPMODE 
////    // , A1.MODULE_CODE 
   
	////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	// ���� ���� ����� ������ Validation ���߰� ������ ������ �Ӽ����� �Է� �ϱ� ���� �Լ�
//	@Override
//	public void executeOperation() throws Exception {
//		
//		TCComponentItemRevision targetCom =  (TCComponentItemRevision)AIFUtility.getCurrentApplication().getTargetComponent();
//		
//		if( null != targetCom ) {
//			TCComponentItemRevision itemRevision = (TCComponentItemRevision)CustomUtil.findItemRevision("M7_MECORevision", "PBIPA18015", "000");
//			if( null != itemRevision ) {
////				targetCom.add("IMAN_MEWorkArea", itemRevision);
//			  targetCom.setReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, itemRevision);
//			}
//		}
//		
//	} // end executeOperation �Լ�
		
    
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    LogFileUtility logFileUtility;
    
    public void uploadCGRFile(ArrayList<HashMap<String, Object>> notUploadList) throws Exception {
    	DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		String tiem = df.format(new Date());
    	
    	logFileUtility = new LogFileUtility(tiem + "_" + "CGRFileTransform.txt");
    	
    	
    	TCSession session = (TCSession) AifrcpPlugin.getSessionService().getActivePerspectiveSession();
    	
    	// CGR ���� ���� ���� 
    	
    	// 1.������ �̿� �ؼ� ���۵��� ���� ������ ����Ʈ ��ȸ
//    	SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
    	
    	
    	
//    	DataSet ds = new DataSet();
    	// �÷� �Ӽ�
//    	part_number
//      VERSION
//      file_path
    	ArrayList<HashMap<String, Object>> notUploadListArray = notUploadList;
    	
     	// 2. �ش� �����۵��� ã�� ������ CGR ������ �ִ��� �ľ�
    	//     ���ٸ� ���� ����Ʈ �ľ� 
    			TCComponentDataset tempDataset = null;
		    	for( int i = 0; i < notUploadListArray.size(); i ++) {
//		    	for( int i = 0; i < 10; i ++) {
		    		HashMap<String, Object> notUploadHash  =  notUploadListArray.get(i);
		    		String partNumber = (String)notUploadHash.get("PITEM_ID");
		    		String version = (String)notUploadHash.get("PITEM_REVISION_ID");
		    		String filePath = partNumber + "_" + version +".cgr";
		    try {
//		    		String partNumber = "6223321000";
//		    		String version = "002";
//		    		String filePath = "6223321000_002.cgr";
		    		
		    		TCComponentItemRevision findItemRevision = CustomUtil.findItemRevision("S7_VehpartRevision", partNumber, version);
		    		Vector<TCComponentDataset> datasetVector =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_reference", "CATCache");
		    		TCComponentDataset cgrDataset = null;
		    		if( datasetVector.size() == 0 ) {
		    			logFileUtility.writeSimpleReport("ERROR : " + partNumber + "_" + version + "\t CGR �����ͼ� ����");
		    			  Vector<TCComponentDataset> catPartDatasets =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_specification", "CATPart");
			              if( catPartDatasets.size() == 0 ) {
			            	  catPartDatasets =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_reference", "CATPart");
			              }
			              
			              if( catPartDatasets.size() == 0 ) {
			            	  logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart ���� ����");
			            	  continue;
			              }
			              
			              TCComponentTcFile[] imanFiles = catPartDatasets.get(0).getTcFiles();
	    			        File[] files = null;
	    			        if (imanFiles.length > 0) {
	    			        	

	    			            files = catPartDatasets.get(0).getFiles(getNamedRefType(catPartDatasets.get(0), imanFiles[0]), "C:\\temp");
	    			            String fileName = getExtension(files[0]);
	    			            if( !fileName.equalsIgnoreCase("CATPart") ) {
	    			            	logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart Ȯ���ڸ� �ٸ�");
	    			            	continue;
	    			            }
	    			        }
			              
			              TCComponent[] datasetArray = new TCComponent[1];
			              TCComponent[] revisionArray = new TCComponent[1];
			              HashMap<String, String> tempHash = new HashMap<String, String>();
			              datasetArray[0] = catPartDatasets.get(0);
			              revisionArray[0] = findItemRevision;
			        	  RequestResult result = DispatcherSoaProxy.getInstance().createRequest("SIEMENS", "catparttocgr", 1, datasetArray, revisionArray, null, 0, null, "", tempHash);
			        	  TCComponent requestResult = result.getRequest();
			        	  logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CGR �����ͼ� ���� ��û");
		    			System.out.println("ERROR :   " + partNumber + "_" + version + "/t CGR �����ͼ� ����");
		    		} 
		    		else {
		    			cgrDataset = datasetVector.get(0);
		    			
		    			if( cgrDataset != null ) {
		    				 TCComponentTcFile[] imanFile = cgrDataset.getTcFiles();
		    			        File[] file = null;
		    			        if (imanFile.length > 0) {
		    			            file = cgrDataset.getFiles(getNamedRefType(cgrDataset, imanFile[0]), "C:\\temp");
		    			            String fileName = file[0].getName();
		    			            if( file[0] != null ) {
		    			            	if( !filePath.equals(fileName)) {
		    			            		File renameFile = null;
		    			            		file[0].renameTo(renameFile = new File("C:\\temp\\" + filePath));
		    			            		file[0] = renameFile ;

		    			            	} 
		    			            	// 3. �ִٸ� CGR FTP �� ���� ���� 
		//    			            			#cadFTP.ip=10.80.57.184  ���� ����
		//    			            			#cadFTP.ip=10.80.57.184  � ����
		//    			            			#cadFTP.port=21
		//    			            			#cadFTP.login=SYMC  ���� 
        //    			            			#cadFTP.login=EAIIF  � 
		//    			            			#cadFTP.pass=123qwer@
		//    			            			#cadFTP.cadFtpPath=.
		    			            	// D:\CGR_TEST �׽�Ʈ FTP ��Ʈ ����
		    			            	
		    			            	// ����̺� �뷮 Ȯ�� �ϱ�
		    			            	double freeSpace = new File("D:").getFreeSpace() / Math.pow(1024, 3);

		    			            	// �뷮 ����Ͽ� 50GB ���Ϸ� ������ ���� ����
		    			            	if( freeSpace <= 50) {
		    			            		break;
		    			            	}
//		    			            	NetworkUtil.uploadFtpFile("10.80.8.186", 21, "FTPUSER", "ss1786500", "/", ".", new File[] { file[0] });
		    			            	logFileUtility.writeSimpleReport("SUCCESS : " + partNumber + "_" + version + " : " + fileName +"\t ���� �Ϸ�");
		    			            	file[0].delete();
		    			            	logFileUtility.writeSimpleReport("DELETE :  " + partNumber + "_" + version + " : " + fileName +  "\t ���� �Ϸ�");
		    			            	
		    			            	
		    			            	
//		    			            	logFileUtility.writeSimpleReport("DBUPDATE :" + partNumber + "_" + version + "\t ���� ���� DB Update �Ϸ�");
		    			            }
		    			            
		    			        } else {
		    			        	
		    			        	logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CGR ���� ����");
		    			        	findItemRevision.remove("IMAN_reference", cgrDataset);
		    			        	cgrDataset.delete();
		    			        	
//		    			        	  args[0].providerName = "SIEMENS";
//		    			              args[0].serviceName = "catparttocgr";
//		    			              args[0].priority = 1;
//		    			              args[0].interval = -1;
//		    			              args[0].primaryObjects = catpartDataset;
//		    			              args[0].secondaryObjects = new ModelObject[] { revision };
//		    			        	3 - �켱���� 
//		    			        	4 - Dataset 
//		    			        	5 - Revision 
//		    			        	6 - null
//		    			        	7 - 0 
//		    			        	8 - null
//		    			        	9 - ��ȯŸ�� �ϰų� �־ ��
//		    			        	10 - �ʿ��� �� �־� �ٷ��� �־��ִ� ��
		    			              Vector<TCComponentDataset> catPartDataset =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_specification", "CATPart");
		    			              if( catPartDataset.size() == 0 ) {
		    			            	  catPartDataset =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_reference", "CATPart");
		    			              }
		    			              
		    			              if( catPartDataset.size() == 0 ) {
		    			            	  logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart ���� ����");
		    			            	  continue;
		    			              }
		    			              
		    			              
		    			              TCComponentTcFile[] imanFiles = catPartDataset.get(0).getTcFiles();
		  	    			        File[] files = null;
		  	    			        if (imanFiles.length > 0) {
		  	    			        	
		  	    			        			  	    			        	
		  	    			            files = catPartDataset.get(0).getFiles(getNamedRefType(catPartDataset.get(0), imanFiles[0]), "C:\\temp");
		  	    			            String fileExtension = getExtension(files[0]);
		  	    			            if( !fileExtension.equalsIgnoreCase("CATPart")  ) {
		  	    			            	logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart Ȯ���ڸ� �ٸ�");
		  	    			            	continue;
		  	    			            }
		  	    			        }
		    			              
		    			              
		    			              TCComponent[] datasetArray = new TCComponent[1];
		    			              TCComponent[] revisionArray = new TCComponent[1];
		    			              HashMap<String, String> tempHash = new HashMap<String, String>();
		    			              datasetArray[0] = catPartDataset.get(0);
		    			              revisionArray[0] = findItemRevision;
		    			        	  RequestResult result = DispatcherSoaProxy.getInstance().createRequest("SIEMENS", "catparttocgr", 1, datasetArray, revisionArray, null, 0, null, "", tempHash);
		    			        	  TCComponent requestResult = result.getRequest();
		    			        	  logFileUtility.writeSimpleReport("ERROR : " + partNumber + "_" + version + "\t CGR �����ͼ� ���� ��û");
		    			        }
		    				}
		    			}
		    		
					    }catch (Exception e) {
//							logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t ���� �߻�");
//							logFileUtility.writeExceptionTrace("ERROR :   " + partNumber + "_" + version + "\t ���� �߻�", e);
				    }
		    	}
		    
//		    	MessageBox.post(AIFUtility.getActiveDesktop(), "��ȯ ��û�� �Ϸ� �Ǿ����ϴ�.", "INFORMATION", MessageBox.INFORMATION);
    	
    }// uploadCGRFile ��
    
    
    public static String getNamedRefType(TCComponentDataset datasetComponent, TCComponent TCComponent)
            throws Exception {
        String s = "";
        TCProperty imanproperty = datasetComponent.getTCProperty("ref_list");
        TCProperty imanproperty1 = datasetComponent.getTCProperty("ref_names");
        if (imanproperty == null || imanproperty1 == null) {
            return s;
        }
        TCComponent aTCComponent[] = imanproperty.getReferenceValueArray();
        String as[] = imanproperty1.getStringValueArray();
        if (aTCComponent == null || as == null) {
            return s;
        }
        int i = aTCComponent.length;
        if (i != as.length) {
            return s;
        }
        int j = -1;
        for (int k = 0; k < i; k++) {
            if (TCComponent != aTCComponent[k]) {
                continue;
            }
            j = k;
            break;
        }

        if (j != -1) {
            s = as[j];
        }
        return s;
    }
    
    
    public String getExtension(File file) throws Exception {
        if (file.isDirectory())
            return null;
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1);
        }
        return null;
    }


	@Override
	public void startOperation(String commandId) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void endOperation() {
		// TODO Auto-generated method stub
		
	}
    
    
///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////    
	
}
