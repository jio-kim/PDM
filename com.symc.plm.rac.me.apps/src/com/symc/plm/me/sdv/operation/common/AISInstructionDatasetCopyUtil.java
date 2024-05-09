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

import com.kgm.common.utils.SYMTcUtil;
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
 * [SR150714-039][20151117] taeku.jeong ���� ���� �� �۾��׸� Data-set�� ����ǵ��� ����
 * [NONE_SR] [20151120] taeku.jeong �����۾�ǥ�ؼ� ������ȣ ��ȣ ������ ���� ����
 * @author Taeku
 *
 */
public class AISInstructionDatasetCopyUtil {
	
	TCSession session;

	public AISInstructionDatasetCopyUtil(TCSession session){
		this.session = session;
	}
	
	/**
	 * srcItemRevision�� �ִ� ���� �� ���� �����۾�ǥ�ؼ� Dataset��  targetItemRevision�� �������� ���ִ� Function
	 * ��ü�� ������ ����, ������ Operation�� Cop�ϴ� ��� �����۾� ǥ�ؼ��� �������� ���� ��Ȳ�̶� �̰Ϳ� ���� ���� ��û���� �������
	 * @param srcItemRevision �����۾�ǥ�ؼ��� ���� ���� Item Revision
	 * @param targetItemRevision �����۾�ǥ�ؼ��� ���� ������ ����� Item Revision
	 */
	public void assemblyInstructionSheetCopy(TCComponentItemRevision srcItemRevision, TCComponentItemRevision targetItemRevision){
		
		// �����۾� ǥ�ؼ� Dataset�� ������ ã�Ƽ� Hashtable�� ��´�.
		Hashtable<String, TCComponentDataset> findedDataset = getAssemblyInstructionDatasets(srcItemRevision);
		
		if(findedDataset==null){
			return;
		}
		
		// ������ Dataset�� ������ ã�Ƽ� ���� �Ѵ�.
		deleteCurrentDataset(targetItemRevision);
		
        String newItemId = null;
        String newItemRevisionId = null;
        String newDatasetFileName = null;
        
        // ��Ģ�� ���� ���� ItemRevision�� PUID�� ��ȣ�� �����ϱ����� PUID���� �д´�.
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

		// �ѱ� �����۾� ǥ�ؼ��� �ִ��� Ȯ���ϰ� Dataset�� Copy�Ѵ�.
		if(aKorDataset!=null){
			TCComponentDataset newDataset = getCopyedDataset(aKorDataset, newDatasetFileName, newItemId, newPassword);
			// [20151120] taeku.jeong Released ���� ���� ���¿����� Dataset�� UID�� ��ȣ�� ���� �Ѵ�.
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
		
		// ���� �����۾� ǥ�ؼ��� �ִ��� Ȯ���ϰ� Dataset�� Copy�Ѵ�.
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
	 * Item Revision�� ���Ե� ���������۾�ǥ�ؼ��� ��ȣ�� UnProtection �ϴ� ����� ���� �մϴ�.
	 * @param srcItemRevision ��ȣ�� �缳���� ����� Item Revision
	 */
	public void englishAssemblyInstructionSheetUnProtect(TCComponentItemRevision srcItemRevision){
		
		// ������ ���� �����۾� ǥ�ؼ��� ã�ƿ´�.
		Hashtable<String, TCComponentDataset> findedDataset = getAssemblyInstructionDatasets(srcItemRevision);
		
		if(findedDataset==null){
			return;
		}
		
		TCComponentDataset aEngDataset =  findedDataset.get(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
		
		// ���� �����۾� ǥ�ؼ��� ��ȣ�� �缳���Ѵ�.
		// [NONE_SR] [20151120] taeku.jeong �����۾�ǥ�ؼ� ������ȣ ��ȣ ������ ���� ����
		if(aEngDataset!=null){
			assemblyInstructionSheetPasswordRefresh(aEngDataset, (String)null);
		}
	}
	
	/**
	 * �־��� Item Revision�� ���� �� ���� �����۾�ǥ�ؼ� Dataset�� sheet ��ȣ �� ���� ��ȣ ��ȣ�� ��Ģ�� �°� ������ �ش�.
	 * @param srcItemRevision ��ȣ�� �缳���� ����� Item Revision
	 */
	public void assemblyInstructionSheetPasswordRefresh(TCComponentItemRevision srcItemRevision){
		
		// ������ ���� �����۾� ǥ�ؼ��� ã�ƿ´�.
		Hashtable<String, TCComponentDataset> findedDataset = getAssemblyInstructionDatasets(srcItemRevision);
		
		if(findedDataset==null){
			return;
		}
		
		// ��Ģ�� ���� ���� ItemRevision�� PUID�� ��ȣ�� �����ϱ����� PUID���� �д´�.
		// [NONE_SR] [20151120] taeku.jeong �����۾�ǥ�ؼ� ������ȣ ��ȣ ������ ���� ����
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

		// ���� �����۾�ǥ�ؼ��� ��ȣ�� �缳�� �Ѵ�.
		// [NONE_SR] [20151120] taeku.jeong �����۾�ǥ�ؼ� ������ȣ ��ȣ ������ ���� ����
		if(aKorDataset!=null){
			if(isReleased == false){
				newUidStr = aKorDataset.getUid();
			}
			assemblyInstructionSheetPasswordRefresh(aKorDataset, newUidStr);
		}
		
		// ���� �����۾� ǥ�ؼ��� ��ȣ�� �缳���Ѵ�.
		// [NONE_SR] [20151120] taeku.jeong �����۾�ǥ�ؼ� ������ȣ ��ȣ ������ ���� ����
		if(aEngDataset!=null){
			assemblyInstructionSheetPasswordRefresh(aEngDataset, (String)null);
		}
	}
	
	/**
	 * �־��� Dataset�� ���� �־��� ��ȣ�� ��ȣ�� �缳�� �Ѵ�.
	 * ���Լ��� ��������� ������ �پ� �ִ�
	 * Named Reference ������ ������ ������ ���� �����ϰ� �ٽ� Upload�ϴ� ���·� ���� �Ǿ�����.
	 *  
	 * @param targetDataset ����� Dataset
	 * @param newPassword ���ο� ��ȣ
	 */
	private void assemblyInstructionSheetPasswordRefresh(TCComponentDataset targetDataset, String newPassword){
		
		File newFile = null;
		
        TCComponentTcFile[] tcFiles = null;
		try {
			tcFiles = ((TCComponentDataset) targetDataset).getTcFiles();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		// ������ �����۾�ǥ�ؼ� ���� ������ �ִ��� Ȯ���ϰ� File�� �����´�.
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
		
		// ������ ���� ������ ������ ��ȣ�� �缳���ϰ� �ٽ� Dataset�� Upload�Ѵ�.
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
	 * �־��� ItemRevision���� ���� �� ���� �����۾�ǥ�ؼ� Dataset�� ã�Ƽ� �����Ѵ�.
	 * �� �Լ��� Operation�� Copy�ؼ� �����ϴ� �������� ������� �Լ��̹Ƿ� ���λ����� ItemRevision��
	 * �����۾� ǥ�ؼ� Dataset�� �ִ� ���� �����۾�ǥ�ؼ� Template�� �ٿ����� ������ �����ϱ� ������
	 * ������ �����۾�ǥ�ؼ� Dataset�� ����� ����
	 * 
	 * @param itemRevision �����۾�ǥ�ؼ��� ������ ����� ItemRevision
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
	 * �����۾�ǥ�ؼ��� ������� �ϴ� ������ ���� Dataset�� �ִ� ExcelFile�� �����ؼ� ���ο� Dataset�� ����� �� Dataset�� ����� ExcelFile�� �߰��Ѵ�.
	 * @param orignDataset �����۾� ǥ�ؼ��� �ִ� ���� Dataset
	 * @param newDatasetname ���� ������ Dataset�� �̸�
	 * @param newFileName ���λ����� Dataset�� ÷�ε� ExcelFile�� �̸�
	 * @param newPassword �����۾�ǥ�ؼ� ���� �� ��Ʈ��ȣ ��ȣ�� ���� ��ȣ ���ڿ�
	 * @return ���λ����� �����۾�ǥ�ؼ� Dataset
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
            
            // Ȯ���ڸ� �̿��� Excel������ ÷�ε� ���� �´��� Ȯ���Ѵ�.
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
		
		// ���� �����۾�ǥ�ؼ��� ÷�ε� File�� �̿��� ���ο� �����۾�ǥ�ؼ� Dataset�� �����Ѵ�.
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
	 * POI API�� �̿��� �����۾�ǥ�ؼ� Excel������ ��ȣ�� ���� �����Ѵ�.
	 * �̶� �Է����� �־��� ��ȣ�� null �̰ų� ���ڿ��� ���̰� 0 �ΰ�� ��ȣ�� �������� ���� ���·� �����.
	 *  
	 * @param file ��ȣ�� ������ ����� Excel File
	 * @param newPassword ���μ����� ��ȣ. ���࿡ ��ȣ���ڰ� Null�̰ų� ������ ��� ��ȣ�� ���� ���°� �ȴ�.
	 * @return ��ȣ�� ������ ExcelFile�� Return�Ѵ�.
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
     * Workbook ��ü�� ������ȣ�� ��Ʈ��ȣ ��ȣ�� �־��� ��ȣ�� �����Ѵ�.
     * @param workbook ��ȣ���� ����� Workbook ��ü
     * @param password ������ ��ȣ
     */
    static public void changePassword(Workbook workbook, String password) {
    	
        STUnsignedShortHex convertedPassword = AISInstructionDatasetCopyUtil.stringToExcelPassword(workbook, password);
        for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
            XSSFSheet sheet = (XSSFSheet) workbook.getSheetAt(i);
            CTSheetProtection sheetProtection = sheet.getCTWorksheet().getSheetProtection();
            if (sheetProtection == null) {
                sheetProtection = sheet.getCTWorksheet().addNewSheetProtection();
            }
            // [NON-SR][20160205] taeku.jeong WorkSheet ��ȣ�� �ȵǴ� ������ �־ Protection������ ó�� �߰�
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
     * �־��� ���ڿ��� Excel Workbook�� ���� ��ȣ��ü ���·� ��ȯ �ؼ� Return�Ѵ�.
     * @param workbook ����ڷκ��� �Է¹��� �Ϲ� ���ڿ� ������ ��ȣ
     * @param password ��ȯ�� ��ȣ
     * @return
     */
    public static  STUnsignedShortHex stringToExcelPassword(Workbook workbook, String password) {
    	
    	// [NON-SR][20160205] taeku.jeong Sheet ��ȣ, ���չ��� ��ȣ ��ȣ �콬 �˰��� ���� �κ� ����
        short hash = PasswordRecord.hashPassword(password);
        String hex = HexDump.toHex(hash);
        
        STUnsignedShortHex hexPassword = STUnsignedShortHex.Factory.newInstance();
        hexPassword.setStringValue(hex);

        // [NON-SR][20160205] taeku.jeong Sheet ��ȣ, ���չ��� ��ȣ ��ȣ �콬 �˰��� ���� �κ� ������ �ҽ�
        //STUnsignedShortHex hexPassword = STUnsignedShortHex.Factory.newInstance();
        //hexPassword.setStringValue(String.valueOf(HexDump.shortToHex(PasswordRecord.hashPassword(password))).substring(2));

        return hexPassword;
    }
	
    /**
     * �־��� Workbook�� ��ȣ�� ���� ���·� �����.
     * 
     * @param workbook ����� Workbook
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
	 * �־��� Item Revision�� �پ� �ִ� Dataset�߿� ���� �� ���� �����۾�ǥ�ؼ� ����� �پ� �ִ� Dataset�� ã�Ƽ� Hashtable�� ��� Return�Ѵ�.
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
				
				// ã�� Dataset�� ���� �� ���������۾� ǥ�ؼ� ���� �̸��� Key��  Hashtable�� ��´�.
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
