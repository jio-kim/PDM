/**
 * 일괄 DownLoad Abstract Class
 * 일괄 DownLoad에 필요한 공통기능 구현
 * 
 * Excel 중요 Data 위치 Setting
 * Option에 따른 기능 수행(하위 클래스에서 작업에 맞게 변경해야 함)
 * Option 정의 (dialogs_locale_ko_KR.properties에 정의)
 * ########## Bundle Work Option Start ############
 * # Item 사용여부
 * BWXLSExpDialog.opt.isItemAvailable=true
 * # BOM 사용여부
 * BWXLSExpDialog.opt.isBOMAvailable=true
 * # Dataset 사용여부
 * BWXLSExpDialog.opt.isDatasetAvailable=true
 * # Dataset File 다운로드 여부
 * BWXLSExpDialog.opt.isDatasetDownloadable=true
 * ########## Bundle Work Option End ############
 * 
 * 주의
 * 일괄 DownLoad시 빈 Row에 정의된 CellStyle을 복사하므로
 * Template Excel파일은 CellStyle이 정의된 빈 Row가 존재해야함
 */
package com.ssangyong.common.bundlework;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.bundlework.bwutil.BWDOption;
import com.ssangyong.common.bundlework.bwutil.BWItemModel;
import com.ssangyong.common.utils.DateUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.common.utils.TxtReportFactory;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public abstract class BWXLSExpDialog extends BundleWorkDialog
{

	/* Item Sheet 유효 Data End Position이 위치한 Column Index */
	public static final int ITEM_DATA_END_X_POS = 0;

	/* Item Type Value값이 위치한 Column Index */
	public static final int ITEM_TYPE_X_POS = 2;
	/* Item Type Value값이 위치한 Row Index */
	public static final int ITEM_TYPE_Y_POS = 0;

	/* Excel Header 정보 시작 Column Index */
	public static final int ITEM_HEADER_START_X_POS = 1;
	/* Excel Header 정보 시작 Row Index */
	public static final int ITEM_HEADER_START_Y_POS = 2;

	/* Item Sheet 유효 Data Column Index */
	public static final int ITEM_START_X_POS = 1;
	/* Item Sheet 유효 Data Row Index */
	public static final int ITEM_START_Y_POS = 5;

	/* Item Sheet Data 종료(END) 값이 위치한 Column Index */
	public static final int ITEM_END_X_POS = 0;

	/* Excel Data 종료 지시자 */
	public static final String ITEM_END_VALUE = "End";

	/* Item Sheet Level Column Index */
	public static final int ITEM_XLS_COLUMN_INDEX_LEVEL = 1;

	/* TreeItem ItemID Column Index */
	static final int TREEITEM_COMLUMN_ITEMID = 0;
	/* TreeItem Revision Column Index */
	static final int TREEITEM_COMLUMN_REVISION = 1;
	/* TreeItem PartName Column Index */
	static final int TREEITEM_COMLUMN_PARTNAME = 2;

	/* Excel WorkBook */
	public Workbook wb;
	/* Excel에 명시된 Item별 속성 Model */
	public BWItemModel headerModel;

	/* Excel Level Attr Name */
	public static final String ITEM_ATTR_LEVEL = "Level";
	/* Excel ItemID Attr Name */
	public static final String ITEM_ATTR_ITEMID = "item_id";
	/* Excel RevisionID Attr Name */
	public static final String ITEM_ATTR_REVISIONID = "item_revision_id";
	/* Excel ItemName Attr Name */
	public static final String ITEM_ATTR_ITEMNAME = "object_name";
	/* Excel TotalSheet Attr Name, Validtion을 위해 관리 */
	public static final String ITEM_ATTR_TOTSHEETNUM = "STCTotalSheet";
	/* Master ID Atrr Name */
	public static final String ITEM_ATTR_MASTER = "STCMaster";

	/* Excel Dataset Type Attr Name */
	public static final String DATASET_ATTR_TYPE = "dataset_type";
	/* Excel Dataset Name Attr Name */
	public static final String DATASET_ATTR_NAME = "object_name";

	public static final String ITEM_ATTR_MATRIALGROUP = "STCMaterialGroup";

	// BWItemData 저장시 Skip할 Attr List
	public static final String[] szSkipAttrs = { ITEM_ATTR_LEVEL, "old_item_id", "old_item_revision_id" };

	// 유효 Column Count
	public int nValidColumnCount = 0;
	// Excel Row Copy를 위한 Index
	public int nCurRow = 0;
	// Excel Total Row Count
	public int nTotalRow = 0;

	public int nWraningCount = 0;

	public TCSession session;
	public TCComponentItemRevision targetRevision;
	/* 일괄 DownLoad Option */
	public BWDOption bwOption;

	/* Excel 파일 저장 경로 */
	public String strXlsFileFullPath;

	/* Template Dataset Name */
	public String strTemplateDSName;

	// TreeItem 관리 ArrayList
	public ArrayList<ManualTreeItem> itemList;

	public HashMap<String, TCComponentItemRevision> revisionMap;

	public String strExcelFilePath;
	

    public ArrayList<TCComponentBOMWindow> bomWindowList;

	public BWXLSExpDialog(Shell parent, TCComponentItemRevision itemRevision, int style, Class<?> cls)
	{
		super(parent, style, cls);

		this.session = (TCSession) AifrcpPlugin.getSessionService().getActivePerspectiveSession();
		this.targetRevision = itemRevision;
		this.createBundleWorkOption();

		this.revisionMap = new HashMap<String, TCComponentItemRevision>();
		
        this.bomWindowList = new ArrayList<TCComponentBOMWindow>();
		
	      this.shell.addDisposeListener(new DisposeListener(){

	          @Override
	          public void widgetDisposed(DisposeEvent e) {

	            try
	            {
	              for( int i = 0 ; i < bomWindowList.size() ; i++)
	              {
	                TCComponentBOMWindow bomWindow = bomWindowList.get(i);
	                if(bomWindow != null)
	                {
	                  bomWindow.close();
	                  bomWindow = null;
	                }
	              }
	            }
	            catch(TCException ex)
	            {
	              ex.printStackTrace();
	            }
	          }
	          
	        });
	      
		
		
	}

	public BWXLSExpDialog(Shell parent, TCComponentItemRevision itemRevision, int style)
	{
		this(parent, itemRevision, style, BWXLSExpDialog.class);

	}

	/**
	 * Property에 명시된 DownLoad Option Loading
	 */
	public void createBundleWorkOption()
	{

		try
		{
			String strMiddleOptName = "opt";

			// DownLoad가능한 DataSet Type Array
			String[] szDownLoadableDSType = this.getTextBundleArray("DownLoadableDataSet", strMiddleOptName, this.dlgClass);

			// DownLoad 가능한 Dataset Type별 확장자 List(구분자 => ',') , 파일이 여러개인 경우 구분자
			// => '/'
			// 현재 모든 파일이 다운로드됨.. 차후 수정시 사용
			String[] szDownLoadableDSFileExt = this.getTextBundleArray("DownLoadableDatasetFileExtList", strMiddleOptName, this.dlgClass);

			this.bwOption = new BWDOption(szDownLoadableDSType);

			if (szDownLoadableDSType.length == szDownLoadableDSFileExt.length)
			{
				for (int i = 0; i < szDownLoadableDSType.length; i++)
				{
					String[] szExt = BundleWorkDialog.getSplitString(szDownLoadableDSFileExt[i], "/");
					this.bwOption.setDataRefExt(szDownLoadableDSType[i], szExt);
				}
			} else
			{
				throw new Exception();
			}

			// Level 사용여부(false이면 Bom전개 시 모든 Item은 1Level로 표시함)
			this.bwOption.setLevelAvailable(Boolean.parseBoolean(this.getTextBundle("isLevelAvailable", strMiddleOptName, dlgClass)));

			// Item 사용 여부
			this.bwOption.setItemAvailable(Boolean.parseBoolean(this.getTextBundle("isItemAvailable", strMiddleOptName, dlgClass)));
			// BOM 사용 여부
			this.bwOption.setBOMAvailable(Boolean.parseBoolean(this.getTextBundle("isBOMAvailable", strMiddleOptName, dlgClass)));

			// Dataset 사용 여부
			this.bwOption.setDatasetAvailable(Boolean.parseBoolean(this.getTextBundle("isDatasetAvailable", strMiddleOptName, dlgClass)));
			// Dataset File DownLoad 여부
			this.bwOption.setDatasetDownloadable(Boolean.parseBoolean(this.getTextBundle("isDatasetDownloadable", strMiddleOptName, dlgClass)));

			// 일괄 DownLoad작업에 사용되는 Template DataSet Name
			this.strTemplateDSName = super.getTextBundle("TemplateDatasetName", null, dlgClass);

		} catch (Exception e)
		{
			MessageBox.post(super.shell, super.getTextBundle("AdminWorkInvalid", "MSG", super.dlgClass), "ERROR", 2);
		}

	}

	/**
	 * 화면생성 Template Excel Download 기능 활성화 Excel Header 값 Load Data Load
	 */
	@Override
	public void dialogOpen()
	{
		try
		{
			super.dialogOpen();
			//super.shell.setImage(Activator.imageDescriptorFromPlugin("com.stc.cms", "icons/export_16.png").createImage());

		} catch (Exception e)
		{
			e.printStackTrace();
		}

	}

	/**
	 * Template Excel에 명시된 Header 정보를 Load
	 * 
	 * @throws Exception
	 */
	public void loadHeader() throws Exception
	{
		// Template File Load
		File tempFile = SYMTcUtil.getTemplateFile(this.session, this.strTemplateDSName, null);
		FileInputStream fis = null;
		try
		{
			String strExt = tempFile.getAbsolutePath().substring(tempFile.getAbsolutePath().lastIndexOf(".") + 1);
			fis = new FileInputStream(tempFile);
			if (strExt.toLowerCase().equals("xls"))
			{

				// Excel WorkBook
				wb = new HSSFWorkbook(fis);
			} else
			{
				// Excel WorkBook
				wb = new XSSFWorkbook(fis);
			}

			fis.close();
			fis = null;

		} catch (Exception e)
		{
			wb = null;
			if (fis != null)
			{
				try
				{
					fis.close();
					fis = null;
				} catch (IOException ex)
				{
					ex.printStackTrace();
				}
			}

			MessageBox.post(super.shell, super.getTextBundle("ExcelInvalid", "MSG", super.dlgClass), "ERROR", 2);
			throw e;
		}

		/*
		 * String strTempFileName = tempFile.getName(); String strTempExtention = strTempFileName.substring(tempFile.getName().lastIndexOf(".")); String strItemID =
		 * this.targetRevision.getProperty("item_id"); String strExcelFileName = strItemID + strTempExtention; // 저장될 Excel File FullPath this.strXlsFileFullPath = this.strImageRoot +
		 * File.separatorChar + strExcelFileName;
		 */

		Sheet sheet = this.wb.getSheetAt(0);

		this.headerModel = new BWItemModel();

		Row itemTypeRow = sheet.getRow(ITEM_TYPE_Y_POS);
		String strItemType = super.getCellText(itemTypeRow.getCell(ITEM_TYPE_X_POS));

		// Item Type 존재 여부 Check
		if (strItemType == null || strItemType.equals(""))
		{
			throw new Exception(super.getTextBundle("XlsItemTypeBlank", "MSG", super.dlgClass));
		}

		// Item 유형 Row
		Row classRow = sheet.getRow(ITEM_HEADER_START_Y_POS);
		// Item 유형 별 속성 Row
		Row attrRow = sheet.getRow(ITEM_HEADER_START_Y_POS + 1);

		// 유효 Column 갯수, 첫번째 "A" Column이 Null 일 수 있으므로 +1
		this.nValidColumnCount = classRow.getPhysicalNumberOfCells() + 1;

		// Header Model 생성
		for (int i = ITEM_HEADER_START_X_POS; i < nValidColumnCount; i++)
		{
			String strClass = super.getCellText(classRow.getCell(i)).trim();
			String strAttr = super.getCellText(attrRow.getCell(i)).trim();

			this.headerModel.setModelData(strClass, strAttr, new Integer(i));

		}

	}

	/*
	 * 실행 Button Click시 수행
	 * 
	 * Excel 파일 생성 Report Log 파일 생성
	 */
	@Override
	public void execute() throws Exception
	{
		// 실행 전처리
		executePre();

		if (this.nWraningCount > 0)
		{
			org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
			box1.setMessage(this.nWraningCount + super.getTextBundle("WarningIgnore", "MSG", super.dlgClass));

			if (box1.open() != SWT.OK)
			{
				return;
			}
		}

		super.executeButton.setEnabled(false);
		super.cancelButton.setEnabled(false);

		try
		{
			TreeItem[] szTopItems = super.tree.getItems();

			if (szTopItems == null || szTopItems.length == 0)
			{
				MessageBox.post(super.shell, super.getTextBundle("UploadInvalid", "MSG", dlgClass), "Notification", 2);
				return;
			}

			Sheet sheet = wb.getSheetAt(0);

			this.copyRowStyle(sheet);

			this.nCurRow = ITEM_START_Y_POS;

			ExecutionJob job = new ExecutionJob(shell.getText(), szTopItems);
			job.schedule();

		} catch (Exception e)
		{
			MessageBox.post(super.shell, e.toString(), "Error", 2);
			e.printStackTrace();
		}

	}

	/**
	 * ManualTreeItem에 저장된 속성값을 Excel에 명시된 Model정보를 바탕으로 출력
	 * 
	 * 
	 * @param treeItem
	 *            : Manual TreeItem
	 * @param sheet
	 *            : 속성값이 저장될 Excel Sheet
	 * @throws Exception
	 */
	private void createExcel(ManualTreeItem treeItem, Sheet sheet) throws Exception
	{
		this.syncItemState(treeItem, STATUS_INPROGRESS, null);

		// Excel에 명시된 Model정보를 바탕으로 출력
		this.setRowData(sheet, treeItem);
		// 해당 Row 완료 처리
		this.syncItemState(treeItem, STATUS_COMPLETED, "Data Exported");

		this.nCurRow++;

		ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
		this.syncGetChildItem(treeItem, itemList);
		for (int i = 0; i < itemList.size(); i++)
		{
			ManualTreeItem childItem = itemList.get(i);
			if (childItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM))
			{

				// 재귀호출
				this.createExcel(childItem, sheet);
			}
		}

	}

	/**
	 * 표준 Data Row를 참조하여 생성될 내용만큼 Row/Cell 생성 표준 CellStyle을 Copy
	 * 
	 * @param sheet
	 */
	private void copyRowStyle(Sheet sheet)
	{

		Row orgRow = sheet.getRow(ITEM_START_Y_POS);
		int nStartPos = ITEM_START_Y_POS + 1;
		int nEndPos = this.nTotalRow + ITEM_START_Y_POS;
		for (int i = nStartPos; i < nEndPos; i++)
		{
			Row newRow = sheet.createRow(i);
			for (int j = 0; j < this.nValidColumnCount; j++)
			{
				Cell orgCell = orgRow.getCell(j);

				Cell newCell = newRow.createCell(j);

				if (orgCell != null)
					newCell.setCellStyle(orgCell.getCellStyle());

				if ((i + 1) == nEndPos && j == 0)
					newCell.setCellValue(ITEM_END_VALUE);

			}
		}
	}

	/**
	 * ManualTreeItem에 저장된 모든 속성 값을 Excel에 출력
	 * 
	 * @param sheet
	 *            : 대상 Excel Sheet
	 * @param treeItem
	 *            : Manual TreeItem
	 * @throws Exception
	 */
	private void setRowData(Sheet sheet, ManualTreeItem treeItem) throws Exception
	{
		Row cRow = sheet.getRow(this.nCurRow);

		// 정의된 Item유형 만큼 Looping
		for (int i = 0; i < CLASS_TYPE_LIST.length; i++)
		{
			// Item유형에 정의된 속성 Map
			HashMap<String, Integer> attrIndexMap = this.headerModel.getModelAttrs(CLASS_TYPE_LIST[i]);
			Object[] attrNames = attrIndexMap.keySet().toArray();

			for (int j = 0; j < attrNames.length; j++)
			{
				// 해당 속성의 Excel Column 위치정보
				Integer intObj = attrIndexMap.get(attrNames[j]);

				if (intObj == null)
					continue;

				// 속성 값
				String xlsCellValue = treeItem.getBWItemAttrValue(CLASS_TYPE_LIST[i], attrNames[j].toString());

				if (intObj.intValue() == ITEM_XLS_COLUMN_INDEX_LEVEL)
				{
					if (treeItem.getLevel() > -1)
						xlsCellValue = this.getLevel(treeItem.getLevel());
					else
						xlsCellValue = "";
				}

				// 속성값 출력
				cRow.getCell(intObj.intValue()).setCellValue(xlsCellValue);
			}

		}

		this.syncItemState(treeItem, STATUS_INPROGRESS, super.getTextBundle("AttrPrinted", "MSG", super.dlgClass));

		// DownLoad 가능한 Option 이고 Dataset Type인 경우 파일 다운로드
		if (this.bwOption.isDatasetDownloadable() && treeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCDATASET))
		{
			downLoadDataSetFile(treeItem);
			// dataset child는 존재하지 않음.
			return;
		} else if (!this.bwOption.isDatasetDownloadable() && treeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCDATASET))
		{
			this.syncItemState(treeItem, STATUS_COMPLETED, null);
		}

		// TreeItem[] childItems = treeItem.getItems();
		ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
		this.syncGetChildItem(treeItem, itemList);

		int nChildDataset = 0;
		for (int i = 0; i < itemList.size(); i++)
		{
			ManualTreeItem cTreeItem = itemList.get(i);
			// Item Type인 경우
			if (cTreeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM))
			{
				// this.setRowData(sheet, cTreeItem, this.nCurRow++);
			}
			// Dataset Type인 경우
			else
			{
				// 첫번째 Dataset은 Excel에서 Item 속성과 함께 표시됨.
				if (nChildDataset == 0)
				{
					HashMap<String, Integer> attrIndexMap = this.headerModel.getModelAttrs(CLASS_TYPE_DATASET);
					Object[] attrNames = attrIndexMap.keySet().toArray();

					for (int j = 0; j < attrNames.length; j++)
					{
						Integer intObj = attrIndexMap.get(attrNames[j]);

						if (intObj == null)
							continue;

						String xlsCellValue = cTreeItem.getBWItemAttrValue(CLASS_TYPE_DATASET, attrNames[j].toString());
						cRow.getCell(intObj.intValue()).setCellValue(xlsCellValue);
					}
					this.syncItemState(cTreeItem, STATUS_INPROGRESS, super.getTextBundle("AttrPrinted", "MSG", super.dlgClass));

					if (this.bwOption.isDatasetDownloadable())
						downLoadDataSetFile(cTreeItem);
					else
					{

						this.syncItemState(cTreeItem, STATUS_COMPLETED, null);
					}

				}
				// 두번째 Dataset 부터 Item과 다른 row에 표시됨
				else
				{
					this.nCurRow++;
					this.setRowData(sheet, cTreeItem);

				}
				nChildDataset++;
			}
		}

	}

	/**
	 * Job과 UI Thread간의 충돌을 피해 UI Update
	 * 
	 * 
	 * @param treeItem
	 * @param nStatus
	 * @param strMessage
	 */
	public void syncItemState(final ManualTreeItem treeItem, final int nStatus, final String strMessage)
	{
		shell.getDisplay().syncExec(new Runnable()
		{

			public void run()
			{
				if (strMessage == null)
					treeItem.setStatus(nStatus);
				else
					treeItem.setStatus(nStatus, strMessage);
			}

		});
	}

	public void syncGetChildItem(final ManualTreeItem treeItem, final ArrayList<ManualTreeItem> itemList)
	{
		shell.getDisplay().syncExec(new Runnable()
		{

			public void run()
			{
				TreeItem[] childItems = treeItem.getItems();
				for (int i = 0; i < childItems.length; i++)
				{
					itemList.add((ManualTreeItem) childItems[i]);
				}
			}

		});

	}

	/**
	 * Error, Warning Count 계산
	 * 
	 * @param treeItem
	 * @param szError
	 */
	public void getErrorCount(ManualTreeItem treeItem, int[] szError)
	{
		if (treeItem.getStatus() == STATUS_ERROR)
			szError[0]++;
		else if (treeItem.getStatus() == STATUS_WARNING)
			szError[1]++;

		TreeItem[] childItems = treeItem.getItems();
		for (int i = 0; i < childItems.length; i++)
		{
			ManualTreeItem cItem = (ManualTreeItem) childItems[i];

			this.getErrorCount(cItem, szError);

		}

	}

	/**
	 * Level 정보 String 반환
	 */
	public String getLevel(int nLevel)
	{

		StringBuffer strBuf = new StringBuffer();
		for (int i = 0; i < nLevel; i++)
		{

			strBuf.append(" ");
		}

		strBuf.append(nLevel);

		return strBuf.toString();
	}

	/**
	 * Dataset의 Reference File을 다운로드
	 * 
	 * @param treeItem
	 * @throws Exception
	 */
	public void downLoadDataSetFile(ManualTreeItem treeItem) throws Exception
	{
		TCComponentDataset dataset = treeItem.getDataSet();
		if (dataset == null)
		{
			this.syncItemState(treeItem, STATUS_ERROR, super.getTextBundle("DatasetNotFound", "MSG", super.dlgClass));
			return;
		}
		try
		{

			TCComponentTcFile[] imanFile = dataset.getTcFiles();

			if (imanFile.length > 0)
			{
				dataset.getFiles(SYMTcUtil.getNamedRefType(dataset, imanFile[0]), this.strImageRoot);
				this.syncItemState(treeItem, STATUS_COMPLETED, "DownLoad 완료.");

			} else
			{
				this.syncItemState(treeItem, STATUS_WARNING, super.getTextBundle("FileNotFound", "MSG", super.dlgClass));
			}

		} catch (Exception e)
		{
			this.syncItemState(treeItem, STATUS_ERROR, e.toString());
			e.printStackTrace();
		}
	}

	/*
	 * 선택된 Target Item을 Bom 전개하여 ManualTreeItem 생성 ManualTreeItem에 Excel에 필요한 모든 속성값을 저장
	 */
	@Override
	public void load() throws Exception
	{
		// 전처리
		loadPre();

		// ManualTreeItem List
		this.itemList = new ArrayList<ManualTreeItem>();

		LoadJob job = new LoadJob(shell.getText());
		job.schedule();

		
	}

	/**
	 * BomLine,Item,ItemRevision,Dataset 속성정보를 ManualTreeItem에 저장
	 * 
	 * @param bomLine
	 *            : 대상 BomLine
	 * @param pTreeItem
	 *            :
	 * @throws Exception
	 */
	private void loadBOMData(final TCComponentBOMLine bomLine, final ManualTreeItem pTreeItem, final int nLevel) throws Exception
	{
		
		if( dlgClass.getSimpleName().equals("DPVExpDialog") && nLevel > 1 )
		{
			return;
		}
		
		// Thread.sleep(2000);

		final TCComponentItemRevision itemRevision = bomLine.getItemRevision();

		final String strID = itemRevision.getProperty("item_id");
		final String strRevision = itemRevision.getProperty("item_revision_id");

		final String strMapKey = strID + "/" + strRevision;

		shell.getDisplay().syncExec(new Runnable()
		{

			public void run()
			{
				ManualTreeItem mTreeItem = null;

				// Top Line인 경우, Level(Tree구성) 사용하지 않는 경우
				if (pTreeItem == null || !bwOption.isLevelAvailable())
				{
					if (bwOption.isLevelAvailable())
					{
						mTreeItem = new ManualTreeItem(tree, tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strID);
						mTreeItem.setLevel(0);
					}
					// Level(Tree구성)을 하지 않는 경우 중복된 Item은 제외한다.
					else
					{
						if (!revisionMap.containsKey(strMapKey))
						{
							mTreeItem = new ManualTreeItem(tree, tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strID);
							mTreeItem.setLevel(0);
						}
					}

				} else
				{
					mTreeItem = new ManualTreeItem(pTreeItem, pTreeItem.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strID);
					mTreeItem.setLevel(nLevel);

				}

				tree.setSelection(mTreeItem);

				itemList.add(mTreeItem);

				try
				{

					if (!bwOption.isLevelAvailable() && !revisionMap.containsKey(strMapKey))
					{
						revisionMap.put(strMapKey, itemRevision);
					}

					setSTCItemData(mTreeItem, bomLine, CLASS_TYPE_BOMLINE);
					setSTCItemData(mTreeItem, bomLine.getItem(), CLASS_TYPE_ITEM);
					setSTCItemData(mTreeItem, itemRevision, CLASS_TYPE_REVISION);

					// Excel에 출력될 Total Row 개수 증가
					nTotalRow++;

					// ItemRevision 추가 속성 Setting
					//

					// DataSet을 사용하지 않는 Option이면 Skip한다.
					if (bwOption.isDatasetAvailable())
					{

						TCComponentItemRevision revision = null;

						if (bwOption.isDatasetDownloadable())
						{

							revision = itemRevision;

						} else
						{
							revision = itemRevision;
						}

						if (SYMTcUtil.isCheckedOut(revision.getItem()))
							mTreeItem.setStatus(STATUS_WARNING, getTextBundle("ItemCheckouted", "MSG", dlgClass));

						if (SYMTcUtil.isCheckedOut(revision))
							mTreeItem.setStatus(STATUS_WARNING, getTextBundle("RevisionCheckouted", "MSG", dlgClass));

						String[] szDownLoadableDSType = bwOption.getDownLoadableDSType();

						AIFComponentContext[] context = revision.getChildren();
						TCComponent[] szTCComponent = new TCComponent[context.length];
						for (int j = 0; j < context.length; j++)
						{
							szTCComponent[j] = (TCComponent) context[j].getComponent();
						}
						// Object Name으로 Sorting
						szTCComponent = SYMTcUtil.sortTCCompoentArray(session, szTCComponent, "object_name");

						int nDSCount = 0;
						for (int j = (szTCComponent.length - 1); j >= 0; j--)
						{
							TCComponent component = szTCComponent[j];

							// DownLoad 명시된 Dataset만 DownLoad함
							for (int i = 0; i < szDownLoadableDSType.length; i++)
							{

								// DownLoad 명시된 DataSet인 경우
								if (component.isTypeOf(szDownLoadableDSType[i]))
								{

									TCComponentDataset dataset = (TCComponentDataset) component;

									String strDataSetName = dataset.getProperty("object_name");
									ManualTreeItem dataSetTreeItem = new ManualTreeItem(mTreeItem, 0, ManualTreeItem.ITEM_TYPE_TCDATASET, strDataSetName);

									dataSetTreeItem.setDataSet(dataset);

									setSTCItemData(dataSetTreeItem, dataset, CLASS_TYPE_DATASET);

									dataSetTreeItem.setText(TREEITEM_COMLUMN_ITEMID, strDataSetName);

									if (SYMTcUtil.isCheckedOut(dataset))
										dataSetTreeItem.setStatus(STATUS_WARNING, getTextBundle("DatasetCheckouted", "MSG", dlgClass));

									if (nDSCount != 0)
									{
										// Excel에 출력될 Total Row 개수 증가
										nTotalRow++;
									}
									nDSCount++;

								}
							}
						}
					}

				} catch (Exception e)
				{
					e.toString();
				}

			}

		});

		ManualTreeItem currentItem = this.itemList.get(this.itemList.size() - 1);

		TCComponent[] children = bomLine.getRelatedComponents("bl_child_lines");

		for (TCComponent child : children)
		{
			TCComponentBOMLine childBOMLine = (TCComponentBOMLine) child;
			this.loadBOMData(childBOMLine, currentItem, nLevel + 1);
		}
	}

	/**
	 * Excel에 명시된 속성값을 TreeItem Cell에 반영
	 * 
	 * @param treeItem
	 */
	private void setTreeItemData(ManualTreeItem treeItem, String strItemType)
	{
		for (int i = 0; i < super.attrMappingMap.size(); i++)
		{
			String[] szItemAttrMapping = super.attrMappingMap.get(i);

			// Not Applicable Check
			if (szItemAttrMapping == null || szItemAttrMapping[0].equals("N/A"))
				continue;

			if (strItemType.equals(szItemAttrMapping[0]))
			{
				String strValue = treeItem.getBWItemAttrValue(szItemAttrMapping[0], szItemAttrMapping[1]);
				treeItem.setText(i, strValue);

			}

		}
	}

	public void setSTCItemData(ManualTreeItem treeItem, TCComponent component, String strType) throws TCException
	{
		HashMap<String, Integer> attrIndexMap = this.headerModel.getModelAttrs(strType);
		Object[] attrNames = attrIndexMap.keySet().toArray();

		for (int i = 0; i < attrNames.length; i++)
		{
			String strAttrName = (String) attrNames[i];

			// 속성 Update가 불필요한 속성은 Skip 한다.
			boolean isSkipAttr = false;
			for (int j = 0; j < szSkipAttrs.length; j++)
			{
				if (strAttrName.equals(szSkipAttrs[j]))
				{
					isSkipAttr = true;
					break;
				}
			}

			if (isSkipAttr)
				continue;

			if (!component.isValidPropertyName(strAttrName))
			{
				// 규격화(국방도번)시 Hidden값으로 내려감.. STCNPI에는 속성이 없음..
				if (!ITEM_ATTR_MATRIALGROUP.equals(strAttrName))
				{
					treeItem.setStatus(STATUS_ERROR, strType + "." + strAttrName + super.getTextBundle("AttrInvalid", "MSG", super.dlgClass));
					continue;
				}
			}

			String strAttrValue = component.getProperty((String) attrNames[i]);
			treeItem.setBWItemAttrValue(strType, strAttrName, strAttrValue);

		}
		this.setTreeItemData(treeItem, strType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.stc.cms.dialog.STCBundleWorkImpl#selectTarget()
	 */
	@Override
	public void selectTarget() throws Exception
	{
		String item_id = this.targetRevision.getProperty("item_id");

		FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE);
		fDialog.setFilterNames(new String[] { "Excel File" });
		fDialog.setFileName(item_id + ".xls");
		// *.xls, *.xlsx Filter 설정
		fDialog.setFilterExtensions(new String[] { "*.xls" });
		fDialog.open();

		if (fDialog.getFilterPath() == null || fDialog.getFilterPath().equals(""))
			return;

		String strfileName = fDialog.getFileName();
		if ((strfileName == null) || (strfileName.equals("")))
			return;

		String strDownLoadFilePath = fDialog.getFilterPath() + File.separatorChar + strfileName;

		File checkFile = new File(strDownLoadFilePath);
		if (checkFile.exists())
		{

			org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
			box1.setMessage(strDownLoadFilePath + this.getTextBundle("FileExist", "MSG", dlgClass));

			if (box1.open() != SWT.OK)
			{
				return;
			}
		}

		this.fileText.setText(strDownLoadFilePath);
		this.strImageRoot = fDialog.getFilterPath();
		this.strXlsFileFullPath = strDownLoadFilePath;

		super.searchButton.setEnabled(false);

		this.loadHeader();
		this.load();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.stc.cms.dialog.STCBundleWorkImpl#validate()
	 */
	@Override
	public boolean validate() throws Exception
	{
		// 전처리
		validatePre();

		// Upload File Check
		TreeItem[] szTopItems = super.tree.getItems();

		if (szTopItems == null || szTopItems.length == 0)
		{
			return false;
		}

		for (int i = 0; i < szTopItems.length; i++)
		{

			// Top TreeItem
			ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];

			// End 표시 확인 해야 함..
			// 필수 속성 정보 확인 해야 함..

			if (this.bwOption.isDatasetAvailable() && this.bwOption.isDatasetDownloadable())
			{
				this.checkFile(topTreeItem);
			}

		}

		// 후처리
		validatePost();

		// Error Count, Warning Count
		int[] szErrorCount = { 0, 0 };
		for (int i = 0; i < szTopItems.length; i++)
		{

			// Top TreeItem
			ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
			this.getErrorCount(topTreeItem, szErrorCount);
		}

		this.nWraningCount = szErrorCount[1];

		super.text.append("\n----------------------------------\n");
		super.text.append("Warning : " + szErrorCount[1] + "\n\n");
		super.text.append("Error : " + szErrorCount[0]);
		super.text.append("\n----------------------------------\n");

		if (szErrorCount[0] > 0)
		{
			super.text.append(super.getTextBundle("ErrorOccured", "MSG", super.dlgClass));
			return false;
		} else
			return true;

	}

	private void checkFile(ManualTreeItem treeItem)
	{

		if (treeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCDATASET))
		{
			String strDataSetName = treeItem.getBWItemAttrValue(CLASS_TYPE_DATASET, DATASET_ATTR_NAME);
			String strDataSetType = treeItem.getBWItemAttrValue(CLASS_TYPE_DATASET, DATASET_ATTR_TYPE);

			String[] szDataSetExts = this.bwOption.getDataRefExts(strDataSetType);

			if (szDataSetExts == null || szDataSetExts.length == 0)
			{
				treeItem.setStatus(STATUS_ERROR, super.getTextBundle("DatasetTypeInvalid", "MSG", super.dlgClass));
				return;
			}

			for (int i = 0; i < szDataSetExts.length; i++)
			{
				String strFileFullPath = this.strImageRoot + File.separatorChar + strDataSetName + "." + szDataSetExts[i];

				File importFile = new File(strFileFullPath);
				if (importFile.exists())
				{
					treeItem.setStatus(STATUS_WARNING, strFileFullPath + super.getTextBundle("FileExist", "MSG", super.dlgClass));
				}
			}

			return;
		}

		TreeItem[] childItems = treeItem.getItems();
		for (int i = 0; i < childItems.length; i++)
		{
			ManualTreeItem cItem = (ManualTreeItem) childItems[i];
			this.checkFile(cItem);

		}
	}

	/** 필요시 하위 클래스에서 구현 */
	@Override
	public void validatePre() throws Exception
	{

	}

	/** 필요시 하위 클래스에서 구현 */
	@Override
	public void validatePost() throws Exception
	{

	}

	/** 필요시 하위 클래스에서 구현 */
	@Override
	public void loadPre() throws Exception
	{

	}

	/** 필요시 하위 클래스에서 구현 */
	@Override
	public void loadPost() throws Exception
	{

	}

	/** 필요시 하위 클래스에서 구현 */
	@Override
	public void executePre() throws Exception
	{

	}

	/** 필요시 하위 클래스에서 구현 */
	@Override
	public void executePost() throws Exception
	{

	}
	

    /** 필요시 하위 클래스에서 구현 */
    @Override
    public void importDataPost(ManualTreeItem treeItem) throws Exception
    {
        
    }

	public class LoadJob extends Job
	{

		public LoadJob(String name)
		{

			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{

			TCComponentBOMWindow bomWindow = null;
			try
			{

				// pack 된 BOMLine 을 분할하여 읽기
				//session.getPreferenceService().setString(TCPreferenceService.TC_preference_user, "PSEAutoPackPref", "1");
				session.getPreferenceService().setStringValue("PSEAutoPackPref", "1");
				
				TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
				TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
				bomWindow = windowType.create(ruleType.getDefaultRule());
				
	            bomWindowList.add(bomWindow);				
				
				TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, (TCComponentItemRevision) targetRevision, null, null);

				if( dlgClass.getSimpleName().equals("DPVExpDialog") )
				{
					TCComponent[] children = topLine.getRelatedComponents("bl_child_lines");

					for (TCComponent child : children)
					{
						TCComponentBOMLine childBOMLine = (TCComponentBOMLine) child;
						
						if( SYMCClass.S7_VEHPARTREVISIONTYPE.equals(childBOMLine.getItemRevision().getType()) )
						{
						  loadBOMData(childBOMLine, null, 1);
						}
					}
				}
				else
				{
					loadBOMData(topLine, null, 0);
				}

				// 후처리
				loadPost();
				

				shell.getDisplay().syncExec(new Runnable()
				{

					public void run()
					{
						try
						{
							if (validate())
								executeButton.setEnabled(true);
						} catch (Exception e)
						{
							e.printStackTrace();
						}

					}
				});

			} catch (Exception e)
			{
				e.printStackTrace();
			}

			return new Status(IStatus.OK, "Loading ", "Job Completed");

		}

	}

	public class ExecutionJob extends Job
	{
		TreeItem[] szTopItems;

		public ExecutionJob(String name, TreeItem[] szTopItems)
		{
			super(name);
			this.szTopItems = szTopItems;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor)
		{

			try
			{
				Sheet sheet = wb.getSheetAt(0);

				for (int i = 0; i < szTopItems.length; i++)
				{

					// Top TreeItem
					ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];

					// Top TreeItem 에서 하위로 전개하여 Excel Data 생성
					createExcel(topTreeItem, sheet);

				}

				FileOutputStream out = new FileOutputStream(strXlsFileFullPath);

				wb.write(out);
				out.close();
				out = null;
				wb = null;

				// 실행 후처리
				executePost();

				shell.getDisplay().syncExec(new Runnable()
				{

					public void run()
					{

						// **************************************//
						// Txt Upload Report 생성 //
						// --------------------------------------//

						// Report Factory Instance : HeaderNames, HeaderWidths,
						// Num Column Display Flag, Level Column Display Flag
						TxtReportFactory rptFactory = generateReport(true, true);

						String strDate = DateUtil.getClientDay("yyMMddHHmm");
						String strFileName = "Export_" + strDate + ".log";
						strLogFileFullPath = strImageRoot + File.separatorChar + strFileName;

						// Import Log File 생성
						rptFactory.saveReport(strLogFileFullPath);

						// Error Count, Warning Count
						int[] szErrorCount = { 0, 0 };
						for (int i = 0; i < szTopItems.length; i++)
						{

							// Top TreeItem
							ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
							// this.checkFile(topTreeItem);

							getErrorCount(topTreeItem, szErrorCount);
						}
						nWraningCount = szErrorCount[1];

						text.append("--------------------------\n");
						text.append("Warning : " + szErrorCount[1] + "\n\n");
						text.append("Error : " + szErrorCount[0] + "\n\n\n");
						text.append("[" + strLogFileFullPath + "] " + getTextBundle("LogCreated", "MSG", dlgClass) + "\n\n");

						if (szErrorCount[0] > 0)
						{
							text.append(getTextBundle("ActionNotCompleted", "MSG", dlgClass) + "\n");
							MessageBox.post(shell, getTextBundle("ActionNotCompleted", "MSG", dlgClass), "Error", 2);

						} else
						{
							text.append(getTextBundle("ActionCompleted", "MSG", dlgClass) + "\n");
							MessageBox.post(shell, getTextBundle("ActionCompleted", "MSG", dlgClass), "Notification", 2);
						}

						text.append("--------------------------\n");

						cancelButton.setEnabled(true);
						viewLogButton.setEnabled(true);

					}
				});

			} catch (Exception e)
			{
				e.printStackTrace();
			}

			return new Status(IStatus.OK, "Exporting", "Job Completed");

		}

	}

}
