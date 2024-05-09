/**
 * 
 */
package com.kgm.commands.standardpart;

import java.io.File;
import java.util.Vector;

import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.ExcelService;
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

		
		//������ ���� Part List ����
		ExcelService.createService();
		Vector<String[]> excelData = ExcelService.importExcel(file, sheetName, startRow);
		
		//���� �����Ͱ� ���� ���  Item �� Dataset ����
		if(!excelData.isEmpty()) {
			String message = validation(excelData);
			if(message.equals("") || message==null) {		
				
				//Dataset�� ÷���� ���� ������ ���°�쿡�� �״�� ���� ���� confirm
				if(!confirmMessage.isEmpty()) {
					int confirmResult = ConfirmDialog.prompt(AIFUtility.getActiveDesktop().getShell(), "Notice", confirmMessage + "\n" + "file does not exist. Are you sure you want to continue?");
					if(confirmResult==3) {	//ok�� 2, no��3 �̴�.
						return;
					}										
				}				
				
				//������ ���� ��� ���� ����
				TCComponentFolder objFolder = checkExistFolder("StandardParts");
				if(objFolder == null) {
					objFolder = createFolder("StandardParts");		
				}
				
				//row������ Ǯ�鼭 Item �� Dataset ����
				for(int i=0; i<excelData.size(); i++) {
					String[] row = excelData.get(i);
					if(i>0) {	//�������� �ƴѰ��
						
						//�̹� ��ϵǾ��ִ��� item ���� �˻� ��� �߰� �� ���� ��� ó�� ���� Ȯ�� �ʿ�
						
						//Stdpart Item ����
						TCComponentItem item = CustomUtil.createItem(SYMCClass.S7_STDPARTTYPE, row[1], SYMCClass.ITEM_REV_ID, row[3], null);
						
						//Item �ٿ��ֱ�
						objFolder.add("contents", item);						
						
						//Item Revision ��������
						TCComponentItemRevision itemRevision = CustomUtil.findItemRevision("ItemRevision", row[1], SYMCClass.ITEM_REV_ID);
						
						//Dataset ���� (���� ������ �ִ� �͸� ����)
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
	 * ������ ���� �� �� ���� ���� ���� üũ �Լ�
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
				
				//�������� ������ ���� ���翩�� ���� üũ
				for(int j=0; j<arraySize; j++) {
					String cell = row[j];
					if(cell.isEmpty()) {
						message += "There is no " + headLine[j] + " value in the Row " + (i+1) + ".\n";
					}
				}
				
				//���� ��� ���������� ������ ���� ������ ���� �����ϴ��� ���� üũ
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
	 * Teamcenter�� HomeFolder ������ ���� ���� ���� Ȯ��
	*/
	private TCComponentFolder checkExistFolder(String folderName) {
		AIFComponentContext[] childrenContext;
		try {
			childrenContext = session.getUser().getHomeFolder().getChildren("contents"); // ���� ���� ������
			for (final AIFComponentContext element : childrenContext) {
				final InterfaceAIFComponent infComp = element.getComponent(); // ���������� ����
				if (infComp.getType().equalsIgnoreCase("Folder")) // �������� Ÿ���� ���� �� ���
				{
					String s = infComp.toString();
					System.out.println("===================> " + s);
					if (infComp.toString().equals(folderName)) // ������������ �̸��� ������ ������� ������ Ȯ��
					{ 
						return (TCComponentFolder) infComp; // ������ �̹� ���� �ϴ°����� ��� ���� ����
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
	 * Teamcenter�� HomeFolder ������ ���� ����
	*/
	private TCComponentFolder createFolder(String folderName) throws TCException {
		// Ȩ ���� �Ʒ��� folderName�� ��� �ű� ���� folderName�� ����.
		final TCComponentFolderType localTCComponentFolderType = (TCComponentFolderType) session.getTypeComponent("Folder");
		final TCComponentFolder newfolder = localTCComponentFolderType.create(folderName, "", "Folder");
		session.getUser().getHomeFolder().add("contents", newfolder);

		return newfolder;
	}
	
	/**
	 * ���� ���� ���� Ȯ�� (�Է¹��� �������� CATPart���ϸ�� ���� CATPart���ϸ�� ���Ͽ� ���ϰ�� ����)
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
	 * ���� Ȯ���� ����
	 */
	private String getExtension(String fileName) {
		return fileName.substring(fileName.lastIndexOf(".")+1).toLowerCase();
	}
}
