/**
 * Part/BOM �Ӽ� �ϰ� Upload Dialog
 * ����Ŭ����(BWXLSImpDialog)��  ������ ����� ����( ���� �߰� ���߽� ���� ����� Override�Ͽ� ���� �Ͽ��� ��)
 * �۾�Option�� bundlework_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.SYMCClass;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.bundlework.bwutil.BWItemData;
import com.kgm.common.bundlework.bwutil.BWItemModel;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMStringUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.schemas.soa._2006_03.exceptions.ServiceException;
import com.teamcenter.services.rac.classification.ClassificationService;
import com.teamcenter.services.rac.classification._2007_01.Classification;
import com.teamcenter.services.rac.classification._2007_01.Classification.ClassAttribute;
import com.teamcenter.services.rac.classification._2007_01.Classification.ClassificationObject;
import com.teamcenter.services.rac.classification._2007_01.Classification.ClassificationProperty;
import com.teamcenter.services.rac.classification._2007_01.Classification.ClassificationPropertyValue;
import com.teamcenter.services.rac.classification._2007_01.Classification.CreateClassificationObjectsResponse;
import com.teamcenter.services.rac.classification._2007_01.Classification.GetAttributesForClassesResponse;
import com.teamcenter.soa.client.model.ErrorStack;

@SuppressWarnings("unchecked")
public class BWClassificationImpDialog extends BWXLSImpDialog {
	public BWClassificationImpDialog(Shell parent, int style) {
		super(parent, style, BWClassificationImpDialog.class);
	}

	@Override
	public void dialogOpen() {
		this.excelFileGroup.setBounds(10, 10, 769, 60);
		this.fileText.setBounds(10, 25, 390, 24);
		this.searchButton.setBounds(410, 26, 77, 22);
		this.logGroup.setBounds(10, 75, 863, 481);
		this.tree.setBounds(10, 22, 843, 300);
		this.text.setBounds(10, 330, 843, 141);
		this.executeButton.setBounds(338, 576, 77, 22);
		this.cancelButton.setBounds(459, 576, 77, 22);
		this.viewLogButton.setBounds(750, 576, 120, 22);

		this.shell.open();
		this.shell.layout();
	}

	@Override
	public void load() throws Exception {

//		if(!checkXML()) {
//			return;
//		}
		
		// ManualTreeItem List
		this.itemList = new ArrayList<ManualTreeItem>();
		this.tree.removeAll();

		Display.getDefault().syncExec(new Runnable() {
			public void run() {
				Workbook wb = null;
				FileInputStream fis = null;
				try {
					shell.setCursor(waitCursor);
					String strFilePath = fileText.getText();
					fis = new FileInputStream(strFilePath);

					String strExt = fileText.getText().substring(strFilePath.lastIndexOf(".") + 1);
					
					if (strExt.toLowerCase().equals("xls")) {
						// Excel WorkBook
						wb = new HSSFWorkbook(fis);
					} else {
						// Excel WorkBook
						wb = new XSSFWorkbook(fis);
					}

					fis.close();
					fis = null;
					// Excel Header ���� Loading
					loadHeader(wb);

					// Item Sheet Data Loading;
					loadData(wb);
				} catch (Exception e) {
					MessageBox.post(shell, getTextBundle("ExcelInValid", "MSG", dlgClass), "Notification", 2);
					e.printStackTrace();
				} finally {
					wb = null;
					if (fis != null) {
						try {
							fis.close();
							fis = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					shell.setCursor(arrowCursor);
				}
			}
		});

		// Load ��ó��
		loadPost();
	}

	// @Override
	// public void loadPost() throws Exception {
	//
	// this.executeButton.setEnabled(true);
	// }

	
    /** Excel Load �� ���������� XML ���� ���翩�� üũ  */
    public boolean checkXML() throws Exception
    {
    	String inputFile = fileText.getText();
		String strFilePath = inputFile.substring(0, inputFile.lastIndexOf("\\") + 1) + getTextBundle("XMLFileName", "", dlgClass);
		File file = new File(strFilePath);
		if(!file.isFile()) {
			MessageBox.post(shell, getTextBundle("XMLInValid", "MSG", dlgClass), "Notification", 2);
			searchButton.setEnabled(true);
			return false;
		}
		return true;
    }
    
    
	/**
	 * Excel Header ���� Loading
	 * 
	 * @param wb
	 * @throws Exception
	 */
	private void loadHeader(Workbook wb) throws Exception {
		Sheet sheet = wb.getSheetAt(0);

		this.headerModel = new BWItemModel();

		Row itemTypeRow = sheet.getRow(ITEM_TYPE_Y_POS);
		this.strTargetItemType = super.getCellText(itemTypeRow.getCell(ITEM_TYPE_X_POS));

		if (strTargetItemType == null || strTargetItemType.equals("")) {
			throw new Exception(super.getTextBundle("XlsItemTypeBlank", "MSG", super.dlgClass));
		}

		Row classRow = sheet.getRow(ITEM_HEADER_START_Y_POS);
		Row attrRow = sheet.getRow(ITEM_HEADER_START_Y_POS + 1);

		this.nValidColumnCount = classRow.getPhysicalNumberOfCells() + 1;
		for (int i = ITEM_HEADER_START_X_POS; i < nValidColumnCount; i++) {
			String strClass = super.getCellText(classRow.getCell(i)).trim();
			String strAttr = super.getCellText(attrRow.getCell(i)).trim();

			this.headerModel.setModelData(strClass, strAttr, new Integer(i));
		}
	}

	/**
	 * Excel Sheet�� ��õ� Data�� Load�Ͽ� ManualTreeItem�� ����
	 * 
	 * @param wb
	 */
	private void loadData(Workbook wb) {
		Sheet sheet = wb.getSheetAt(0);
		int rows = sheet.getPhysicalNumberOfRows();

		Row classRow = sheet.getRow(ITEM_HEADER_START_Y_POS);
		Row attrRow = sheet.getRow(ITEM_HEADER_START_Y_POS + 1);

		for (int r = ITEM_START_Y_POS; r < rows; r++) {
			Row row = sheet.getRow(r);

			if (row == null)
				continue;

			BWItemData stcItemData = new BWItemData();

			for (int i = ITEM_START_X_POS; i < this.nValidColumnCount; i++) {
				String strItemName = super.getCellText(classRow.getCell(i)).trim();
				String strAttrName = super.getCellText(attrRow.getCell(i)).trim();
				String strAttrValue = super.getCellText(row.getCell(i)).trim();

				if (strAttrValue == null) {
					strAttrValue = "";
				} else {
					// System.out.println("strAttrValuePre:"+strAttrValue);
					strAttrValue = strAttrValue.replaceAll("\n", " ");
					// System.out.println("strAttrValuePost:"+strAttrValue);
				}

				stcItemData.setItemData(strItemName, strAttrName, strAttrValue);
			}

			String strItemID = stcItemData.getItemAttrValue("Item", "item_id");
			String strRevID = stcItemData.getItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID);
			String strClassID = stcItemData.getItemAttrValue(CLASS_TYPE_CLASSIFICATION, CLASSIFICATION_ATTR_ID);

			// Excel Item Row�� ����� TreeItem Object
			ManualTreeItem treeItem = null;

			int nLevel = super.getIntValue(stcItemData.getItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_LEVEL));

			// Level 0 ���� TopPart
			if (nLevel == 0) {
				// Top TreeItem
				treeItem = new ManualTreeItem(this.tree, this.tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strItemID);
				treeItem.setLevel(nLevel);
			}

			if (treeItem != null) {
				// Tree Table�� ǥ�õǴ� �Ӽ�
				// treeItem.setText(TREEITEM_COMLUMN_ITEMID,
				// stcItemData.getItemAttrValue(CLASS_TYPE_ITEM,
				// ITEM_ATTR_ITEMID));
				// treeItem.setText(TREEITEM_COMLUMN_REVISION,
				// stcItemData.getItemAttrValue(CLASS_TYPE_REVISION,
				// ITEM_ATTR_REVISIONID));

				treeItem.setBWItemData(stcItemData);
				this.setTreeItemData(treeItem, CLASS_TYPE_ITEM);
				this.setTreeItemData(treeItem, CLASS_TYPE_REVISION);
				this.setTreeItemData(treeItem, CLASS_TYPE_CLASSIFICATION);

				this.itemList.add(treeItem);
			}

			// ItemID ������ ������� �ʴ� �� ������ ���
			if (treeItem != null && !this.bwOption.isItemIDBlankable() && strItemID.trim().equals("")) {
				setErrorStatus(treeItem, "ItemIDRequired");
			}

			// Revision ���� Option�� �ƴԿ��� Revision���� ���� ��� Error ó��
			if (treeItem != null && !this.bwOption.isRevCreatable() && strRevID.equals("")) {
				setErrorStatus(treeItem, "RevisionIDRequired");
			} else if (treeItem != null && strRevID.equals("")) {
				treeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, ITEM_ATTR_REVISIONID, NEW_ITEM_REV);
				// treeItem.setText(TREEITEM_COMLUMN_REVISION, NEW_ITEM_REV);
			}

			if (CustomUtil.isEmpty(strClassID)) {
				setErrorStatus(treeItem, "ClaasificationIDRequired");
			} else {
				treeItem.setBWItemAttrValue(CLASS_TYPE_CLASSIFICATION, CLASSIFICATION_ATTR_ID, strClassID);
			}

			// Revision ID ��ȿ�� Check
			if (strRevID.equals("")) {
				setErrorStatus(treeItem, "RevisionInvalid");
			}
		}
	}

	@Override
	public void executePre() throws Exception {
//		importXML();
	}

	
//		XML import�� ������ �⺻ ����� ���ؼ� �ϵ��� ����
//	
//	/**
//	 * Teamcenter�� XML import �ϴ� �Լ�
//	 */
//	private void importXML() {
//		/* ****** ICAdminXmlImportOperation �Լ� ���� ���� ******
//		 * �Լ� ���� 
//		 * 				m_xmlImportOp = new ICAdminXmlImportOperation(m_session, m_lastFilePath, UpdateExistingObjectsFlag, CreateRelatedObjectsFlag, ImportExistingObjects);
//		 * ���� ����
//		 * 				boolean UpdateExistingObjectsFlag = false; 	//�⺻ flase�� ����
//		 * 				boolean CreateRelatedObjectsFlag = false; 		//�⺻ false�� ����
//		 * 				int ImportExistingObjects = 31;					//�⺻ 31�� ����
//		 * 					KeyLOV = 1
//		 *					Attribute = 2
//		 * 					Class = 4
//		 * 					View = 8
//		 * 					Instance = 16
//		 *					��� ���ý� �� = 31
//		 */		 
//
//		String inputFile = fileText.getText();
//		String strFilePath = inputFile.substring(0, inputFile.lastIndexOf("\\") + 1) + getTextBundle("XMLFileName", "", dlgClass);
//
//		
//		ICAdminXmlImportOperation m_xmlImportOp = new ICAdminXmlImportOperation(session, strFilePath, true, false, 31);
//		try {
//			session.queueOperation(m_xmlImportOp);
//		} catch (Exception exception) {
//			exception.printStackTrace();
//		}
//	}

	/**
	 * ManualTreeItem���� Load�� Data�� Server�� Upload
	 * 
	 * @throws Exception
	 */
	@Override
	public void execute() throws Exception {
		this.executeButton.setEnabled(false);
		
//		 executePre();

		System.out.println("Create...");
		
		ClassificationService classificationService = ClassificationService.getService((TCSession) AIFUtility.getSessionManager().getDefaultSession());

		for (int i = 0; i < this.itemList.size(); i++) {

			ManualTreeItem item = this.itemList.get(i);

			// ������ ��� Item�� Classified ���� �ʴ´�.
			if (STATUS_ERROR == item.getStatus()) {
				continue;
			}

			TCComponent wsoItem = null;
			try {
				wsoItem = (TCComponent) CustomUtil.findItem(SYMCClass.S7_STDPARTTYPE, item.getBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID));
				
				//Item�� ���� ������쿡�� classified ����
				if (wsoItem != null) {
					HashMap<String, Object> rtClassMap = getClassInfos(item);
					HashMap<Integer, String> attributeMap = new HashMap<Integer, String>();
					ArrayList<Integer> attributeIDs = (ArrayList<Integer>) rtClassMap.get("attributeIDs");
					
					//���� �ִ� attributeID �� ��ŭ  Map ����
					for (int j = 0; j < attributeIDs.size(); j++) {
						String attributeData = item.getBWItemAttrValue(CLASS_TYPE_CLASSIFICATION, attributeIDs.get(j) + "");
						attributeData = SYMStringUtil.notNullString(attributeData);
						if (!"".equals(attributeData)) {
							attributeMap.put(attributeIDs.get(j), attributeData);
						}
					}
	
					//classifiedItem�� properties ���� ����(attributeID�� ���� �迭�� ����)
					ClassificationProperty[] ico_props = new ClassificationProperty[attributeMap.size()];
					Integer[] attributeKeys = attributeMap.keySet().toArray(new Integer[attributeMap.size()]);
					for (int j = 0; j < attributeKeys.length; j++) {
						ClassificationProperty classificationProperty = new ClassificationProperty();
						classificationProperty.attributeId = attributeKeys[j];
						ClassificationPropertyValue[] ico_prop_values = new ClassificationPropertyValue[1];
						ico_prop_values[0] = new ClassificationPropertyValue();
						ico_prop_values[0].dbValue = attributeMap.get(attributeKeys[j]);
						classificationProperty.values = ico_prop_values;
						ico_props[j] = classificationProperty;
					}
	
					// Item������ ClassificationObject �������� �����Ͽ� classified ��Ű��
					ClassificationObject classifiedItem = new ClassificationObject();
					classifiedItem.classId = item.getBWItemAttrValue(CLASS_TYPE_CLASSIFICATION, CLASSIFICATION_ATTR_ID);
					classifiedItem.clsObjTag = null;
					classifiedItem.instanceId = item.getBWItemAttrValue(CLASS_TYPE_ITEM, ITEM_ATTR_ITEMID);
					classifiedItem.unitBase = (String) rtClassMap.get("unitBase");
					classifiedItem.wsoId = wsoItem;
					classifiedItem.properties = ico_props;
					CreateClassificationObjectsResponse createICOResponse = classificationService.createClassificationObjects(new ClassificationObject[] { classifiedItem });
					
					//����üũ
					if(createICOResponse.data.sizeOfPartialErrors() > 0 ) {
						for(int j=0; j<createICOResponse.data.sizeOfPartialErrors(); ++j) {
							ErrorStack errorsStacks = createICOResponse.data.getPartialError(j);
							if(errorsStacks != null) {
								int[] errorsCodes = errorsStacks.getCodes();
								
								//Error �ڵ� 71067 ���� �޽����� "Can't classify the object in the same class twice Error"
								if(errorsCodes[0]==71067) {
									System.out.println(errorsStacks.getMessages()[0]);
									setErrorStatus(item, "SameClassTwiceError");
	//								updateICO(classificationService, classifiedItem, ico_props, createICOResponse);
								} else {
									item.setStatus(STATUS_ERROR, errorsStacks.getMessages()[0]);
									System.out.println(errorsStacks.getMessages()[0]);
								}
							}						
						}
					} else {
						item.setStatus(STATUS_COMPLETED);
					}
				}
			} catch (TCException e) {
				e.printStackTrace();
			} finally {
				if (wsoItem == null) {
					setErrorStatus(item, "ItemNotFound");
					continue;
				}
			}
		}

		System.out.println("completed...");
		
		// if( this.nWraningCount > 0)
		// {
		// org.eclipse.swt.widgets.MessageBox box1 = new
		// org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL |
		// SWT.ICON_INFORMATION);
		// box1.setMessage(this.nWraningCount+this.getTextBundle("WarningIgnore",
		// "MSG", dlgClass));
		//
		// if (box1.open() != SWT.OK)
		// {
		// return;
		// }
		// }
		//
		// // ���� ��ư Disable
		// super.executeButton.setEnabled(false);
		// // Excel �˻� ��ư Disable
		// super.searchButton.setEnabled(false);
		//
		// // Top TreeItem Array
		// TreeItem[] szTopItems = super.tree.getItems();
		//
		// // TreeItem�� �������� �ʴ� ���
		// if (szTopItems == null || szTopItems.length == 0)
		// {
		// MessageBox.post(super.shell, super.getTextBundle("UploadInvalid",
		// "MSG", dlgClass), "Notification", 2);
		// return;
		// }
		//
		// ExecutionJob job = new ExecutionJob(shell.getText(), szTopItems);
		// job.schedule();
	}

	/**
	 * classified�� ������ update �Լ�
	 * @param createICOResponse 
	*/
/*
	private void updateICO(ClassificationService classificationService, ClassificationObject classifiedItem, ClassificationProperty[] ico_props, CreateClassificationObjectsResponse createICOResponse) throws ServiceException {
		System.out.println(classifiedItem.instanceId.toString() + " is NOT classified......");
		
        // Set the classification object tag to be updated.
//		classifiedItem.clsObjTag = createICOResponse.clsObjs[0].clsObjTag;
        
	    UpdateClassificationObjectsResponse updateICOResponse = classificationService.updateClassificationObjects( new ClassificationObject[] { classifiedItem } );
        
        if ( updateICOResponse.data.sizeOfPartialErrors() > 0) {
        	for(int j=0; j<updateICOResponse.data.sizeOfPartialErrors(); ++j) {
				ErrorStack errorsStacks = updateICOResponse.data.getPartialError(j);
				if(errorsStacks != null) {
					System.out.println(errorsStacks.getMessages()[0]);
			        throw new ServiceException( "ClassificationService.updateClassificationObjects returned a partial Error.");
				}						
			}
        }
        
        System.out.println("... completed Classification::updateClassificationObjects()");
        
        int objCount = updateICOResponse.clsObjs.length ;
        if ( objCount <= 0 )
        {
            System.out.println("Nothing was done");
        }
        else
        {               
            ClassificationObject[]  updatedICOs = new ClassificationObject[objCount];
            for ( int jnx = 0 ;jnx < objCount ; jnx++ )
            {
                updatedICOs[jnx] = updateICOResponse.clsObjs[jnx];

                System.out.println( "Class ID: " + updatedICOs[jnx].classId );
                System.out.println( "ICO ID: " + updatedICOs[jnx].instanceId );
                System.out.println( "Unit Base: " + updatedICOs[jnx].unitBase );
                System.out.println( "WSO Id: " + updatedICOs[jnx].wsoId );
                for( int attrIndex=0; attrIndex < updatedICOs[jnx].properties.length; attrIndex++)
                {
                    if( updatedICOs[jnx].properties[attrIndex].attributeId == ico_props[0].attributeId )
                    {
                        System.out.println( " Updated Attr ID: " + ico_props[0].attributeId );
                        System.out.println( "  Attr value: " + updatedICOs[jnx].properties[attrIndex].values[0].dbValue );
                    }
                }
             }
        }
	}
*/

	/**
	 *  Class�� ���� ��������
	 *  classID�� ������ class�� �˻��Ͽ� 
	 *  unitBase, Abstract����, attributeIDs������ map�� ��� ����
	*/
	private HashMap<String, Object> getClassInfos(ManualTreeItem treeItem) throws Exception {
		HashMap<String, Object> rtMap = new HashMap<String, Object>();
		String classId = treeItem.getBWItemAttrValue(CLASS_TYPE_CLASSIFICATION, CLASSIFICATION_ATTR_ID);
		ClassificationService classificationService = ClassificationService.getService((TCSession) AIFUtility.getSessionManager().getDefaultSession());
		String unitBase = "";
		Boolean isAbstract = false;

		try {
			Classification.GetClassDescriptionsResponse resClassInfo = classificationService.getClassDescriptions(new String[] { classId });
			if (resClassInfo.descriptions != null) {
				Classification.ClassDef classDef = (Classification.ClassDef) resClassInfo.descriptions.get(classId);
				if (classDef.options == null) {
					throw new Exception("Empty class options!");
				}
				if (classDef.options == null || classDef.options.isAbstract) {
					throw new Exception("not allowed Abstract Class!");
				}
				unitBase = classDef.unitBase;
				isAbstract = classDef.options.isAbstract;
			} else {
				throw new Exception("not founded Class!");
			}

			ArrayList<Integer> attributeIDs = new ArrayList<Integer>();
			GetAttributesForClassesResponse response = classificationService.getAttributesForClasses(new String[] { classId });
			if (response.data.sizeOfPartialErrors() > 0) {
				throw new ServiceException("ClassificationService.getAttributesForClasses returned a partial Error.");
			}
			
			ClassAttribute[] clsattr = new ClassAttribute[1];
			Map<String, ClassAttribute[]> attributes = response.attributes;

			Set<String> keys = attributes.keySet();
			for (Iterator<String> itr = keys.iterator(); itr.hasNext();) {
				String id = itr.next();
				clsattr = attributes.get(id);

				for (int i = 0; i < clsattr.length; i++) {
					ClassAttribute clsattrs = clsattr[i];
					attributeIDs.add(clsattrs.id);

//					System.out.println("Attr Id: " + clsattrs.id);
//					System.out.println("Name: " + clsattrs.name);
//					System.out.println("shortname: " + clsattrs.shortName);
//					System.out.println("description: " + clsattrs.description);
//					System.out.println("annotation: " + clsattrs.annotation);
//					System.out.println("arraysize: " + clsattrs.arraySize);
//					System.out.println("options: " + clsattrs.options);
//					System.out.println("Attribute Length: " + clsattrs.altFormat.formatLength);
//					System.out.println("Attribute Modifier1: " + clsattrs.altFormat.formatModifier1);
//					System.out.println("Attribute Modifier2: " + clsattrs.altFormat.formatModifier2);
//					System.out.println("Attribute Type: " + clsattrs.altFormat.formatType);
//					System.out.println("Annotation: " + clsattrs.annotation);
//					System.out.println("Default Value: " + clsattrs.defaultValue);
//					System.out.println("Config: " + clsattrs.config);
//					System.out.println("Maximum Value: " + clsattrs.maxValue);
//					System.out.println("Minimum Value: " + clsattrs.minValue);
//					System.out.println("Post Config: " + clsattrs.postConfig);
//					System.out.println("Pre Config: " + clsattrs.preConfig);
//					System.out.println("Unit Name: " + clsattrs.unitName);
//					System.out.println("****************************************************");
				}
//				System.out.println("****************************************************");
			}
			
			//unitBase, Abstract����, attributeIDs������ map�� ��� ����
			rtMap.put("unitBase", unitBase);
			rtMap.put("isAbstract", isAbstract);
			rtMap.put("attributeIDs", attributeIDs);

		} catch (ServiceException e) {
			e.printStackTrace();
		}
		return rtMap;
	}

	/**
	 * Tree ITEM�� ���� �޼��� ���
	 * 
	 * @method setErrorStatus
	 * @date 2013. 3. 7.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void setErrorStatus(ManualTreeItem item, String strBundleMsgKey) {
		this.nWraningCount += this.nWraningCount;
		item.setStatus(STATUS_ERROR, super.getTextBundle(strBundleMsgKey, "MSG", super.dlgClass));
	}
}