/**
 * 
 */
package com.ssangyong.commands.standardpart;

import java.io.File;
import java.util.Vector;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.ExcelService;

/**
 * @author Hoony
 *
 */
public class StandardPartImportOperation extends AbstractAIFOperation {

	private String filePathAndName;
	private String folderPath;
	private TCSession session = CustomUtil.getTCSession();
	private String confirmMessage;

	public StandardPartImportOperation(String excelPath, String catPath) {
		this.filePathAndName = excelPath;
		this.folderPath = catPath;
	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		File file = new File(filePathAndName);
		String sheetName = "Sheet1";
		int startRow = 0;
		confirmMessage = "";

		
		//엑셀로 부터 Part List 추출
		ExcelService.createService();
		Vector<String[]> excelData = ExcelService.importExcel(file, sheetName, startRow);
		
		//엑셀 데이터가 있을 경우  Item 및 Dataset 생성
		if(!excelData.isEmpty()) {
			String message = validation(excelData);
			if(message.equals("") || message==null) {		
				
				//Dataset에 첨부할 실제 파일이 없는경우에도 그대로 진행 할지 confirm
				if(!confirmMessage.isEmpty()) {
					int confirmResult = ConfirmDialog.prompt(AIFUtility.getActiveDesktop().getShell(), "Notice", confirmMessage + "\n" + "file does not exist. Are you sure you want to continue?");
					if(confirmResult==3) {	//ok는 2, no는3 이다.
						return;
					}										
				}				
				
				//폴더가 없을 경우 폴더 생성
				TCComponentFolder objFolder = checkExistFolder("StandardParts");
				if(objFolder == null) {
					objFolder = createFolder("StandardParts");		
				}
				
				//row단위로 풀면서 Item 및 Dataset 생성
				for(int i=0; i<excelData.size(); i++) {
					String[] row = excelData.get(i);
					if(i>0) {	//헤드라인이 아닌경우
						
						//이미 등록되어있는지 item 사전 검색 기능 추가 및 있을 경우 처리 문제 확인 필요
						
						//Stdpart Item 생성
						TCComponentItem item = CustomUtil.createItem(SYMCClass.S7_STDPARTTYPE, row[1], SYMCClass.ITEM_REV_ID, row[3], null);
						
						//Item 붙여넣기
						objFolder.add("contents", item);						
						
						//Item Revision 가져오기
						TCComponentItemRevision itemRevision = CustomUtil.findItemRevision("ItemRevision", row[1], SYMCClass.ITEM_REV_ID);
						
						//Dataset 생성 (실제 파일이 있는 것만 생성)
						String existFilePath = checkExistFile(row[4]);
						if(existFilePath!=null) {
							String datasetName = itemRevision.toDisplayString().substring(0, (itemRevision.toString().indexOf(";")));			
							TCComponentDataset dataset = CustomUtil.createPasteDataset(itemRevision, datasetName, null, "CATPart", "IMAN_specification");
							String namedRef = CustomUtil.getNamedRefType(dataset, getExtension(row[4]));		
							dataset.setFiles(new String[] { existFilePath }, new String[] { "CATPart" }, new String[] { "Plain" }, new String[] { namedRef });		
							//dataset.setFiles(new String[] { filepath }, new String[] { "File" }, new String[] { "Plain" }, new String[] { namedRef });				
						}			
					}
				}
			} else {
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), message, "Error", MessageBox.ERROR);
			}
		}
	}
	
	/**
	 * 엑셀의 공백 셀 및 파일 존재 유무 체크 함수
	*/	
	private String validation(Vector<String[]> excelData) {
		String[] headLine = null;
		String message = "";
		for(int i=0; i<excelData.size(); i++) {
			String[] row = excelData.get(i);
			int arraySize = row.length;
			
			if(i==0) {
				headLine = row;
			} else {
				
				//엑셀에서 공백인 셀의 존재여부 사전 체크
				for(int j=0; j<arraySize; j++) {
					String cell = row[j];
					if(cell.isEmpty()) {
						message += "There is no " + headLine[j] + " value in the Row " + (i+1) + ".\n";
					}
				}
				
				//엑셀 목록 마지막셀에 지정된 도면 파일이 실제 존재하는지 사전 체크
				if(!row[4].isEmpty() && row[4] !=null) {
					if(checkExistFile(row[4])==null) {
						confirmMessage  += row[4] + "\n";
					}				
				}
			}			
		}
		return message;
	}

	/**
	 * Teamcenter의 HomeFolder 하위에 폴더 존재 여부 확인
	*/
	private TCComponentFolder checkExistFolder(String folderName) {
		AIFComponentContext[] childrenContext;
		try {
			childrenContext = session.getUser().getHomeFolder().getChildren("contents"); // 하위 구조 가져옴
			for (final AIFComponentContext element : childrenContext) {
				final InterfaceAIFComponent infComp = element.getComponent(); // 하위구조물 선언
				if (infComp.getType().equalsIgnoreCase("Folder")) // 하위구조 타입이 폴더 인 경우
				{
					String s = infComp.toString();
					System.out.println("===================> " + s);
					if (infComp.toString().equals(folderName)) // 하위구조물의 이름이 생성될 폴더명과 같은지 확인
					{ 
						return (TCComponentFolder) infComp; // 같은면 이미 존재 하는것으로 대상 폴더 선언
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), e.toString(), "Error", MessageBox.ERROR);
		}
		
		return null;
	}
	
	/**
	 * Teamcenter의 HomeFolder 하위에 폴더 생성
	*/
	private TCComponentFolder createFolder(String folderName) throws TCException {
		// 홈 폴더 아래에 folderName이 없어서 신규 폴더 folderName을 생성.
		final TCComponentFolderType localTCComponentFolderType = (TCComponentFolderType) session.getTypeComponent("Folder");
		final TCComponentFolder newfolder = localTCComponentFolderType.create(folderName, "", "Folder");
		session.getUser().getHomeFolder().add("contents", newfolder);

		return newfolder;
	}
	
	/**
	 * 파일 존재 여부 확인 (입력받은 엑셀안의 CATPart파일명과 실제 CATPart파일명과 비교하여 파일경로 리턴)
	*/
	private String checkExistFile(String fileName) {
		String catFile = folderPath + "/" + fileName;
		File file = new File(catFile);
		if(!file.exists()) {
			return null;		
		}

		return file.getAbsolutePath();
	}
	
	/**
	 * 파일 확장자 추출
	 */
	private String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
	}
}
