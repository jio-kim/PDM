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
 *  [Non-SR] 업무 자동화 프로그램 추가 CATPart To CGR
 *  2018년 10월 업무 자동화 프로그램 으로 
 *  Demon(SYMC Web) 에서 CATPart --> CGR 로 변환하지 못한 항목을 DB 쿼리로 추출 하여 
 *  하위에 실제 물리 파일이 없는 CGR 데이터 셋을 삭제 하고 재 변환 요청 하는 프로그램
 *  변환 요청한 목록과 요청 실패 유무는 C:\\Temp 폴더에 Simple_오늘날짜_CGRFileTransform.txt 로 저장 되어 있음
 *  
 * @author 178650
 *
 */
public class CatpartToCGROperation extends AbstractTCSDVOperation {

	// ssangyong Web을 통한 쿼리 실행을 위한 객체 선언
	private SYMCRemoteUtil remoteQuery;
	// sam 파일 저장 경로
	
	ArrayList<HashMap<String, Object>> specApplicationResultList = new ArrayList<HashMap<String,Object>>();
	
	ArrayList<String> notDuplicateItemId = new ArrayList<String>();
	
    
    @Override
    public void executeOperation() throws Exception {
    	// 쿼리 객체 생성
    	 this.remoteQuery = new SYMCRemoteUtil();
    	// 쿼리에 입력될 parameter 정의 
    	DataSet ds = new DataSet();
    	
     	 try{
            ds.put("JOB_PUID", "");
	     	ArrayList<HashMap<String, Object>> resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute("com.kgm.service.SMTestService", "getNotUploadCGRFile", ds);
	    	uploadCGRFile(resultList);
	    	
	    	MessageBox.post(AIFUtility.getActiveDesktop(), "변환 요청이 완료 되었습니다.\n C:\\Temp 폴더에 Simple_오늘날짜_CGRFileTransform.txt 로 저장 되어 있습니다.", "INFORMATION", MessageBox.INFORMATION);
        } catch( Exception e) {
        	e.getStackTrace();
        	MessageBox.post(AIFUtility.getActiveDesktop(), "변환 도중 에러가 발생 하였습니다. \n 관리자에게 문의 하세요.", "INFORMATION", MessageBox.INFORMATION);
        } 
     	
     	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//    
    	
    }// execute 끝
    
    
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
	// 현업 사용시 사용자 에러나 Validation 미추가 등으로 누락된 속성값을 입력 하기 위한 함수
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
//	} // end executeOperation 함수
		
    
   ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    
    LogFileUtility logFileUtility;
    
    public void uploadCGRFile(ArrayList<HashMap<String, Object>> notUploadList) throws Exception {
    	DateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
		String tiem = df.format(new Date());
    	
    	logFileUtility = new LogFileUtility(tiem + "_" + "CGRFileTransform.txt");
    	
    	
    	TCSession session = (TCSession) AifrcpPlugin.getSessionService().getActivePerspectiveSession();
    	
    	// CGR 파일 전송 로직 
    	
    	// 1.쿼리를 이용 해서 전송되지 못한 아이템 리스트 조회
//    	SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
    	
    	
    	
//    	DataSet ds = new DataSet();
    	// 컬럼 속성
//    	part_number
//      VERSION
//      file_path
    	ArrayList<HashMap<String, Object>> notUploadListArray = notUploadList;
    	
     	// 2. 해당 아이템들을 찾아 하위에 CGR 파일이 있는지 파악
    	//     없다면 없는 리스트 파악 
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
		    			logFileUtility.writeSimpleReport("ERROR : " + partNumber + "_" + version + "\t CGR 데이터셋 없음");
		    			  Vector<TCComponentDataset> catPartDatasets =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_specification", "CATPart");
			              if( catPartDatasets.size() == 0 ) {
			            	  catPartDatasets =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_reference", "CATPart");
			              }
			              
			              if( catPartDatasets.size() == 0 ) {
			            	  logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart 파일 없음");
			            	  continue;
			              }
			              
			              TCComponentTcFile[] imanFiles = catPartDatasets.get(0).getTcFiles();
	    			        File[] files = null;
	    			        if (imanFiles.length > 0) {
	    			        	

	    			            files = catPartDatasets.get(0).getFiles(getNamedRefType(catPartDatasets.get(0), imanFiles[0]), "C:\\temp");
	    			            String fileName = getExtension(files[0]);
	    			            if( !fileName.equalsIgnoreCase("CATPart") ) {
	    			            	logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart 확장자명 다름");
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
			        	  logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CGR 데이터셋 생성 요청");
		    			System.out.println("ERROR :   " + partNumber + "_" + version + "/t CGR 데이터셋 없음");
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
		    			            	// 3. 있다면 CGR FTP 로 파일 전송 
		//    			            			#cadFTP.ip=10.80.57.184  개발 서버
		//    			            			#cadFTP.ip=10.80.57.184  운영 서버
		//    			            			#cadFTP.port=21
		//    			            			#cadFTP.login=SYMC  개발 
        //    			            			#cadFTP.login=EAIIF  운영 
		//    			            			#cadFTP.pass=123qwer@
		//    			            			#cadFTP.cadFtpPath=.
		    			            	// D:\CGR_TEST 테스트 FTP 루트 폴더
		    			            	
		    			            	// 드라이브 용량 확인 하기
		    			            	double freeSpace = new File("D:").getFreeSpace() / Math.pow(1024, 3);

		    			            	// 용량 계산하여 50GB 이하로 남으면 강제 종료
		    			            	if( freeSpace <= 50) {
		    			            		break;
		    			            	}
//		    			            	NetworkUtil.uploadFtpFile("10.80.8.186", 21, "FTPUSER", "ss1786500", "/", ".", new File[] { file[0] });
		    			            	logFileUtility.writeSimpleReport("SUCCESS : " + partNumber + "_" + version + " : " + fileName +"\t 전송 완료");
		    			            	file[0].delete();
		    			            	logFileUtility.writeSimpleReport("DELETE :  " + partNumber + "_" + version + " : " + fileName +  "\t 삭제 완료");
		    			            	
		    			            	
		    			            	
//		    			            	logFileUtility.writeSimpleReport("DBUPDATE :" + partNumber + "_" + version + "\t 전송 성공 DB Update 완료");
		    			            }
		    			            
		    			        } else {
		    			        	
		    			        	logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CGR 파일 없음");
		    			        	findItemRevision.remove("IMAN_reference", cgrDataset);
		    			        	cgrDataset.delete();
		    			        	
//		    			        	  args[0].providerName = "SIEMENS";
//		    			              args[0].serviceName = "catparttocgr";
//		    			              args[0].priority = 1;
//		    			              args[0].interval = -1;
//		    			              args[0].primaryObjects = catpartDataset;
//		    			              args[0].secondaryObjects = new ModelObject[] { revision };
//		    			        	3 - 우선순위 
//		    			        	4 - Dataset 
//		    			        	5 - Revision 
//		    			        	6 - null
//		    			        	7 - 0 
//		    			        	8 - null
//		    			        	9 - 변환타입 암거나 넣어도 됨
//		    			        	10 - 필요한 값 넣어 줄려고 넣어주는 값
		    			              Vector<TCComponentDataset> catPartDataset =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_specification", "CATPart");
		    			              if( catPartDataset.size() == 0 ) {
		    			            	  catPartDataset =   DatasetUtilities.getDatasets(findItemRevision, "IMAN_reference", "CATPart");
		    			              }
		    			              
		    			              if( catPartDataset.size() == 0 ) {
		    			            	  logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart 파일 없음");
		    			            	  continue;
		    			              }
		    			              
		    			              
		    			              TCComponentTcFile[] imanFiles = catPartDataset.get(0).getTcFiles();
		  	    			        File[] files = null;
		  	    			        if (imanFiles.length > 0) {
		  	    			        	
		  	    			        			  	    			        	
		  	    			            files = catPartDataset.get(0).getFiles(getNamedRefType(catPartDataset.get(0), imanFiles[0]), "C:\\temp");
		  	    			            String fileExtension = getExtension(files[0]);
		  	    			            if( !fileExtension.equalsIgnoreCase("CATPart")  ) {
		  	    			            	logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t CatPart 확장자명 다름");
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
		    			        	  logFileUtility.writeSimpleReport("ERROR : " + partNumber + "_" + version + "\t CGR 데이터셋 생성 요청");
		    			        }
		    				}
		    			}
		    		
					    }catch (Exception e) {
//							logFileUtility.writeSimpleReport("ERROR :   " + partNumber + "_" + version + "\t 오류 발생");
//							logFileUtility.writeExceptionTrace("ERROR :   " + partNumber + "_" + version + "\t 오류 발생", e);
				    }
		    	}
		    
//		    	MessageBox.post(AIFUtility.getActiveDesktop(), "변환 요청이 완료 되었습니다.", "INFORMATION", MessageBox.INFORMATION);
    	
    }// uploadCGRFile 끝
    
    
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
