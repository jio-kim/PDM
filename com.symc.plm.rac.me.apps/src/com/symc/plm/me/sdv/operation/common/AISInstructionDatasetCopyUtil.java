package com.symc.plm.me.sdv.operation.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Hashtable;

import org.apache.poi.hssf.record.PasswordRecord;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.HexDump;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnsignedShortHex;

import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;

/**
 * [SR150714-039][20151117] taeku.jeong 공법 복사 시 작업그림 Data-set도 복사되도록 보완
 * [NONE_SR] [20151120] taeku.jeong 조립작업표준서 문서보호 암호 설정을 위한 수정
 * @author Taeku
 *
 */
public class AISInstructionDatasetCopyUtil {
	
	TCSession session;

	public AISInstructionDatasetCopyUtil(TCSession session){
		this.session = session;
	}
	
	/**
	 * srcItemRevision에 있는 국문 및 영문 조립작업표준서 Dataset을  targetItemRevision에 복제생성 해주는 Function
	 * 차체를 제외한 조립, 도장의 Operation을 Cop하는 경우 조립작업 표준서가 생성되지 않은 상황이라 이것에 대한 개선 요청으로 만들어짐
	 * @param srcItemRevision 조립작업표준서를 가진 원본 Item Revision
	 * @param targetItemRevision 조립작업표준서를 복제 생성할 대상인 Item Revision
	 */
	public void assemblyInstructionSheetCopy(TCComponentItemRevision srcItemRevision, TCComponentItemRevision targetItemRevision){
		
		// 조립작업 표준서 Dataset이 있으면 찾아서 Hashtable에 담는다.
		Hashtable<String, TCComponentDataset> findedDataset = getAssemblyInstructionDatasets(srcItemRevision);
		
		if(findedDataset==null){
			return;
		}
		
		// 기존의 Dataset이 있으면 찾아서 삭제 한다.
		deleteCurrentDataset(targetItemRevision);
		
        String newItemId = null;
        String newItemRevisionId = null;
        String newDatasetFileName = null;
        
        // 규칙에 따라 현재 ItemRevision의 PUID를 암호로 설정하기위해 PUID값을 읽는다.
        //String newPassword = targetItemRevision.getUid();
        String newPassword = null;
		try {
			newPassword = targetItemRevision.getItem().getUid();
		} catch (TCException e1) {
			e1.printStackTrace();
		}
		try {
			newItemId = targetItemRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			newItemRevisionId = targetItemRevision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		if(newItemId!=null && newItemId.trim().length()>0){
			if(newItemRevisionId!=null && newItemRevisionId.trim().length()>0){
				newDatasetFileName = newItemId + "/" + newItemRevisionId;
			}
		}
		
		TCComponentDataset aKorDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
		TCComponentDataset aEngDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);

		// 한글 조립작업 표준서가 있는지 확인하고 Dataset을 Copy한다.
		if(aKorDataset!=null){
			TCComponentDataset newDataset = getCopyedDataset(aKorDataset, newDatasetFileName, newItemId, newPassword);
			// [20151120] taeku.jeong Released 되지 않은 상태에서는 Dataset의 UID를 암호로 설정 한다.
			//String newPassword2 = newDataset.getUid();
			assemblyInstructionSheetPasswordRefresh(aKorDataset, newPassword);
			if(newDataset!=null){
				try {
					targetItemRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, newDataset);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
		
		// 영문 조립작업 표준서가 있는지 확인하고 Dataset을 Copy한다.
		if(aEngDataset!=null){
			TCComponentDataset newDataset = getCopyedDataset(aEngDataset, newDatasetFileName, newItemId, (String)null);
			if(newDataset!=null){
				try {
					targetItemRevision.add(SDVTypeConstant.PROCESS_SHEET_EN_RELATION, newDataset);
				} catch (TCException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	/**
	 * Item Revision에 포함된 영문조립작업표준서의 암호를 UnProtection 하는 기능을 수행 합니다.
	 * @param srcItemRevision 암호를 재설정할 대상인 Item Revision
	 */
	public void englishAssemblyInstructionSheetUnProtect(TCComponentItemRevision srcItemRevision){
		
		// 국문과 영문 조립작업 표준서를 찾아온다.
		Hashtable<String, TCComponentDataset> findedDataset = getAssemblyInstructionDatasets(srcItemRevision);
		
		if(findedDataset==null){
			return;
		}
		
		TCComponentDataset aEngDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
		
		// 영문 조립작업 표준서의 암호를 재설정한다.
		// [NONE_SR] [20151120] taeku.jeong 조립작업표준서 문서보호 암호 설정을 위한 수정
		if(aEngDataset!=null){
			assemblyInstructionSheetPasswordRefresh(aEngDataset, (String)null);
		}
	}
	
	/**
	 * 주어진 Item Revision의 국문 및 영문 조립작업표준서 Dataset의 sheet 보호 및 문서 보호 암호를 규칙에 맞게 수정해 준다.
	 * @param srcItemRevision 암호를 재설정할 대상인 Item Revision
	 */
	public void assemblyInstructionSheetPasswordRefresh(TCComponentItemRevision srcItemRevision){
		
		// 국문과 영문 조립작업 표준서를 찾아온다.
		Hashtable<String, TCComponentDataset> findedDataset = getAssemblyInstructionDatasets(srcItemRevision);
		
		if(findedDataset==null){
			return;
		}
		
		// 규칙에 따라 현재 ItemRevision의 PUID를 암호로 설정하기위해 PUID값을 읽는다.
		// [NONE_SR] [20151120] taeku.jeong 조립작업표준서 문서보호 암호 설정을 위한 수정
		boolean isReleased = false;
		try {
			isReleased = SYMTcUtil.isReleased(srcItemRevision);
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		//String newUidStr = srcItemRevision.getUid();
		String newUidStr = null;
		try {
			newUidStr = srcItemRevision.getItem().getUid();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		TCComponentDataset aKorDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
		TCComponentDataset aEngDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);

		// 국문 조립작업표준서의 암호를 재설정 한다.
		// [NONE_SR] [20151120] taeku.jeong 조립작업표준서 문서보호 암호 설정을 위한 수정
		if(aKorDataset!=null){
			if(isReleased == false){
				newUidStr = aKorDataset.getUid();
			}
			assemblyInstructionSheetPasswordRefresh(aKorDataset, newUidStr);
		}
		
		// 영문 조립작업 표준서의 암호를 재설정한다.
		// [NONE_SR] [20151120] taeku.jeong 조립작업표준서 문서보호 암호 설정을 위한 수정
		if(aEngDataset!=null){
			assemblyInstructionSheetPasswordRefresh(aEngDataset, (String)null);
		}
	}
	
	/**
	 * 주어진 Dataset에 대해 주어진 암호로 암호를 재설정 한다.
	 * 이함수는 수행과정에 기존에 붙어 있던
	 * Named Reference 파일을 복제후 기존의 것을 삭제하고 다시 Upload하는 형태로 구현 되어있음.
	 *  
	 * @param targetDataset 대상인 Dataset
	 * @param newPassword 새로운 암호
	 */
	private void assemblyInstructionSheetPasswordRefresh(TCComponentDataset targetDataset, String newPassword){
		
		File newFile = null;
		
        TCComponentTcFile[] tcFiles = null;
		try {
			tcFiles = ((TCComponentDataset) targetDataset).getTcFiles();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		// 기존의 조립작업표준서 엑셀 파일이 있는지 확인하고 File을 가져온다.
		for (int i = 0; i < tcFiles.length; i++) {
            String orgFileName = null;
			try {
				orgFileName = tcFiles[i].getProperty("original_file_name");
			} catch (TCException e) {
				e.printStackTrace();
			}
            String[] orgFileNameSplit = orgFileName.split("[.]");
            String extFileName = orgFileNameSplit[orgFileNameSplit.length - 1];
            if(extFileName!=null ){
            	if(extFileName.trim().equalsIgnoreCase("xls") || extFileName.trim().equalsIgnoreCase("xlsx")){
            		try {
            			newFile = tcFiles[i].getFile(null, orgFileName );
            		} catch (TCException e) {
            			e.printStackTrace();
            		}
            	}
            }
            
            if(newFile!=null && newFile.exists()==true){
            	break;
            }
		}
		
		// 기존의 엑셀 파일이 있으면 암호를 재설정하고 다시 Dataset에 Upload한다.
        if (newFile != null && newFile.exists()==true) {
            try {
            	updateFile(newFile, newPassword);
            	SDVBOPUtilities.datasetUpdate(newFile, targetDataset);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
		
	}
	
	/**
	 * 주어진 ItemRevision에서 국문 및 영문 조립작업표준서 Dataset을 찾아서 삭제한다.
	 * 이 함수는 Operation을 Copy해서 생성하는 목적으로 만들어진 함수이므로 새로생성된 ItemRevision에
	 * 조립작업 표준서 Dataset이 있는 경우는 조립작업표준서 Template을 붙여놓은 것으로 간주하기 때문에
	 * 기존의 조립작업표준서 Dataset을 지우는 것임
	 * 
	 * @param itemRevision 조립작업표준서를 삭제할 대상인 ItemRevision
	 */
	private void deleteCurrentDataset(TCComponentItemRevision itemRevision){
		
		Hashtable<String, TCComponentDataset> findedDataset = getAssemblyInstructionDatasets(itemRevision);
		
		if(findedDataset==null){
			return;
		}
		
		TCComponentDataset aKorDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
		TCComponentDataset aEngDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);

		if(aKorDataset!=null){
			try {
				itemRevision.remove(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, aKorDataset);
				aKorDataset.delete();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
		
		if(aEngDataset!=null){
			try {
				itemRevision.remove(SDVTypeConstant.PROCESS_SHEET_EN_RELATION, aEngDataset);
				aEngDataset.delete();
			} catch (TCException e) {
				e.printStackTrace();
			}
		}

	}
	
	/**
	 * 조립작업표준서를 복재생성 하는 과정에 원본 Dataset에 있는 ExcelFile을 복사해서 새로운 Dataset을 만들고 그 Dataset에 복사된 ExcelFile을 추가한다.
	 * @param orignDataset 조립작업 표준서가 있는 원본 Dataset
	 * @param newDatasetname 새로 생성될 Dataset의 이름
	 * @param newFileName 새로생성될 Dataset에 첨부될 ExcelFile의 이름
	 * @param newPassword 조립작업표준서 문서 및 시트보호 암호로 사용될 암호 문자열
	 * @return 새로생성된 조립작업표준서 Dataset
	 */
	private TCComponentDataset getCopyedDataset(TCComponentDataset orignDataset, String newDatasetname, String newFileName, String newPassword){

		TCComponentDataset newDataset = null;
		
		File newFile = null;
		
        TCComponentTcFile[] tcFiles = null;
		try {
			tcFiles = ((TCComponentDataset) orignDataset).getTcFiles();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		for (int i = 0; i < tcFiles.length; i++) {
            String orgFileName = null;
			try {
				orgFileName = tcFiles[i].getProperty("original_file_name");
			} catch (TCException e) {
				e.printStackTrace();
			}
            String[] orgFileNameSplit = orgFileName.split("[.]");
            String extFileName = orgFileNameSplit[orgFileNameSplit.length - 1];
            String newFullFileName = newFileName + "." + extFileName;
            
            // 확장자를 이용해 Excel파일이 첨부된 것이 맞는지 확인한다.
            if(extFileName!=null ){
            	if(extFileName.trim().equalsIgnoreCase("xls") || extFileName.trim().equalsIgnoreCase("xlsx")){
                    try {
        				newFile = tcFiles[i].getFile(null, newFullFileName );
        			} catch (TCException e) {
        				e.printStackTrace();
        			}
            	}
            }

            if(newFile!=null && newFile.exists()==true){
            	break;
            }
		}
		
		// 원본 조립작업표준서에 첨부된 File을 이용해 새로운 조립작업표준서 Dataset을 생성한다.
        if (newFile != null && newFile.exists()==true) {
            try {
            	updateFile(newFile, newPassword);
				newDataset = SDVBOPUtilities.createDataset(newFile.getAbsolutePath(), newDatasetname);
			} catch (Exception e) {
				e.printStackTrace();
			}
        }

       return newDataset;
	}
	
	/**
	 * POI API를 이용해 조립작업표준서 Excel파일의 암호를 새로 설정한다.
	 * 이때 입력으로 주어진 암호가 null 이거나 문자열의 길이가 0 인경우 암호가 설정되지 않은 상태로 만든다.
	 *  
	 * @param file 암호를 설정할 대상인 Excel File
	 * @param newPassword 새로설정될 암호. 만약에 암호문자가 Null이거나 공백인 경우 암호는 없는 상태가 된다.
	 * @return 암호가 설정된 ExcelFile을 Return한다.
	 * @throws Exception
	 */
    private File updateFile(File file, String newPassword) throws Exception {

        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        
        if(newPassword!=null && newPassword.trim().length()>0){
        	changePassword(workbook, newPassword);
        }else{
        	releasePassword(workbook);
        }
        
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }
    
    /**
     * Workbook 개체의 문서보호와 시트보호 암호를 주어진 암호로 설정한다.
     * @param workbook 암호설정 대상인 Workbook 개체
     * @param password 설정할 암호
     */
    static public void changePassword(Workbook workbook, String password) {
    	
        STUnsignedShortHex convertedPassword = AISInstructionDatasetCopyUtil.stringToExcelPassword(workbook, password);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
            CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
            if (sheetProtection == null) {
                sheetProtection = sheet.getCTWorksheet().addNewSheetProtection();
            }
            // [NON-SR][20160205] taeku.jeong WorkSheet 보호가 안되는 오류가 있어서 Protection을위한 처리 추가
            sheetProtection.xsetPassword(convertedPassword);
            sheetProtection.setSheet(true);
            sheetProtection.setScenarios(true);
            sheetProtection.setObjects(false);
        }

        CTWorkbookProtection workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().getWorkbookProtection();
        if (workbookProtection == null) {
            workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().addNewWorkbookProtection();
            workbookProtection.setLockStructure(true);
            workbookProtection.setLockWindows(true);
        }
        workbookProtection.xsetWorkbookPassword(convertedPassword);
    }

    /**
     * 주어진 문자열을 Excel Workbook에 사용될 암호객체 형태로 변환 해서 Return한다.
     * @param workbook 사용자로부터 입력받은 일반 문자열 형태의 암호
     * @param password 변환된 암호
     * @return
     */
    public static  STUnsignedShortHex stringToExcelPassword(Workbook workbook, String password) {
    	
    	// [NON-SR][20160205] taeku.jeong Sheet 보호, 통합문서 보호 암호 헤쉬 알고리즘 적용 부분 수정
        short hash = PasswordRecord.hashPassword(password);
        String hex = HexDump.toHex(hash);
        
        STUnsignedShortHex hexPassword = STUnsignedShortHex.Factory.newInstance();
        hexPassword.setStringValue(hex);

        // [NON-SR][20160205] taeku.jeong Sheet 보호, 통합문서 보호 암호 헤쉬 알고리즘 적용 부분 수정전 소스
        //STUnsignedShortHex hexPassword = STUnsignedShortHex.Factory.newInstance();
        //hexPassword.setStringValue(String.valueOf(HexDump.shortToHex(PasswordRecord.hashPassword(password))).substring(2));

        return hexPassword;
    }
	
    /**
     * 주어진 Workbook의 암호를 없는 상태로 만든다.
     * 
     * @param workbook 대상인 Workbook
     */
	private void releasePassword(Workbook workbook) {
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
            CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
            if (sheetProtection != null) {
                sheet.getCTWorksheet().unsetSheetProtection();
            }
        }

        CTWorkbookProtection workbookProtection = ((XSSFWorkbook) workbook).getCTWorkbook().getWorkbookProtection();
        if (workbookProtection != null) {
            ((XSSFWorkbook) workbook).getCTWorkbook().unsetWorkbookProtection();
        }
    }
	
	/**
	 * 주어진 Item Revision에 붙어 있는 Dataset중에 국문 과 영문 조립작업표준서 관계로 붙어 있는 Dataset을 찾아서 Hashtable에 담아 Return한다.
	 * @param srcItemRevision
	 * @return
	 */
	private Hashtable<String, TCComponentDataset> getAssemblyInstructionDatasets(TCComponentItemRevision srcItemRevision){
		Hashtable<String, TCComponentDataset> findedDatasetHash = null;
		
		String[] targetRelationType = new String[]{SDVTypeConstant.PROCESS_SHEET_KO_RELATION,
				SDVTypeConstant.PROCESS_SHEET_EN_RELATION};
		
		try {
			AIFComponentContext[] childContexts = srcItemRevision.getChildren(targetRelationType);
			
			for (int i = 0;childContexts!=null && i < childContexts.length; i++) {
				String context = null;
				TCComponentDataset contextDataset = null;
				AIFComponentContext currentContext = childContexts[i];
				
				if(currentContext!=null){
					TCComponent component = (TCComponent)currentContext.getComponent();
					if(component instanceof TCComponentDataset){
						context = (String)currentContext.getContext();
						contextDataset = (TCComponentDataset)component;
					}
				}
				
				// 찾은 Dataset을 국문 과 영문조립작업 표준서 관계 이름을 Key로  Hashtable에 담는다.
				if(context!=null && context.trim().length()>0){
					
					if(findedDatasetHash==null){
						findedDatasetHash = new Hashtable<String, TCComponentDataset>();
					}
					
					findedDatasetHash.put(context, contextDataset);
				}
			}
			
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return findedDatasetHash;
		
	}
	

}
