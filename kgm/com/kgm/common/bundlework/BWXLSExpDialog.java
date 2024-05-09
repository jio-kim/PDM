/**
 * �ϰ� DownLoad Abstract Class
 * �ϰ� DownLoad�� �ʿ��� ������ ����
 * 
 * Excel �߿� Data ��ġ Setting
 * Option�� ���� ��� ����(���� Ŭ�������� �۾��� �°� �����ؾ� ��)
 * Option ���� (dialogs_locale_ko_KR.properties�� ����)
 * ########## Bundle Work Option Start ############
 * # Item ��뿩��
 * BWXLSExpDialog.opt.isItemAvailable=true
 * # BOM ��뿩��
 * BWXLSExpDialog.opt.isBOMAvailable=true
 * # Dataset ��뿩��
 * BWXLSExpDialog.opt.isDatasetAvailable=true
 * # Dataset File �ٿ�ε� ����
 * BWXLSExpDialog.opt.isDatasetDownloadable=true
 * ########## Bundle Work Option End ############
 * 
 * ����
 * �ϰ� DownLoad�� �� Row�� ���ǵ� CellStyle�� �����ϹǷ�
 * Template Excel������ CellStyle�� ���ǵ� �� Row�� �����ؾ���
 */
package com.kgm.common.bundlework;

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

import com.kgm.common.SYMCClass;
import com.kgm.common.bundlework.bwutil.BWDOption;
import com.kgm.common.bundlework.bwutil.BWItemModel;
import com.kgm.common.utils.DateUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.TxtReportFactory;
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

	/* Item Sheet ��ȿ Data End Position�� ��ġ�� Column Index */
	public static final int ITEM_DATA_END_X_POS = 0;

	/* Item Type Value���� ��ġ�� Column Index */
	public static final int ITEM_TYPE_X_POS = 2;
	/* Item Type Value���� ��ġ�� Row Index */
	public static final int ITEM_TYPE_Y_POS = 0;

	/* Excel Header ���� ���� Column Index */
	public static final int ITEM_HEADER_START_X_POS = 1;
	/* Excel Header ���� ���� Row Index */
	public static final int ITEM_HEADER_START_Y_POS = 2;

	/* Item Sheet ��ȿ Data Column Index */
	public static final int ITEM_START_X_POS = 1;
	/* Item Sheet ��ȿ Data Row Index */
	public static final int ITEM_START_Y_POS = 5;

	/* Item Sheet Data ����(END) ���� ��ġ�� Column Index */
	public static final int ITEM_END_X_POS = 0;

	/* Excel Data ���� ������ */
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
	/* Excel�� ��õ� Item�� �Ӽ� Model */
	public BWItemModel headerModel;

	/* Excel Level Attr Name */
	public static final String ITEM_ATTR_LEVEL = "Level";
	/* Excel ItemID Attr Name */
	public static final String ITEM_ATTR_ITEMID = "item_id";
	/* Excel RevisionID Attr Name */
	public static final String ITEM_ATTR_REVISIONID = "item_revision_id";
	/* Excel ItemName Attr Name */
	public static final String ITEM_ATTR_ITEMNAME = "object_name";
	/* Excel TotalSheet Attr Name, Validtion�� ���� ���� */
	public static final String ITEM_ATTR_TOTSHEETNUM = "STCTotalSheet";
	/* Master ID Atrr Name */
	public static final String ITEM_ATTR_MASTER = "STCMaster";

	/* Excel Dataset Type Attr Name */
	public static final String DATASET_ATTR_TYPE = "dataset_type";
	/* Excel Dataset Name Attr Name */
	public static final String DATASET_ATTR_NAME = "object_name";

	public static final String ITEM_ATTR_MATRIALGROUP = "STCMaterialGroup";

	// BWItemData ����� Skip�� Attr List
	public static final String[] szSkipAttrs = { ITEM_ATTR_LEVEL, "old_item_id", "old_item_revision_id" };

	// ��ȿ Column Count
	public int nValidColumnCount = 0;
	// Excel Row Copy�� ���� Index
	public int nCurRow = 0;
	// Excel Total Row Count
	public int nTotalRow = 0;

	public int nWraningCount = 0;

	public TCSession session;
	public TCComponentItemRevision targetRevision;
	/* �ϰ� DownLoad Option */
	public BWDOption bwOption;

	/* Excel ���� ���� ��� */
	public String strXlsFileFullPath;

	/* Template Dataset Name */
	public String strTemplateDSName;

	// TreeItem ���� ArrayList
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
	 * Property�� ��õ� DownLoad Option Loading
	 */
	public void createBundleWorkOption()
	{

		try
		{
			String strMiddleOptName = "opt";

			// DownLoad������ DataSet Type Array
			String[] szDownLoadableDSType = this.getTextBundleArray("DownLoadableDataSet", strMiddleOptName, this.dlgClass);

			// DownLoad ������ Dataset Type�� Ȯ���� List(������ => ',') , ������ �������� ��� ������
			// => '/'
			// ���� ��� ������ �ٿ�ε��.. ���� ������ ���
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

			// Level ��뿩��(false�̸� Bom���� �� ��� Item�� 1Level�� ǥ����)
			this.bwOption.setLevelAvailable(Boolean.parseBoolean(this.getTextBundle("isLevelAvailable", strMiddleOptName, dlgClass)));

			// Item ��� ����
			this.bwOption.setItemAvailable(Boolean.parseBoolean(this.getTextBundle("isItemAvailable", strMiddleOptName, dlgClass)));
			// BOM ��� ����
			this.bwOption.setBOMAvailable(Boolean.parseBoolean(this.getTextBundle("isBOMAvailable", strMiddleOptName, dlgClass)));

			// Dataset ��� ����
			this.bwOption.setDatasetAvailable(Boolean.parseBoolean(this.getTextBundle("isDatasetAvailable", strMiddleOptName, dlgClass)));
			// Dataset File DownLoad ����
			this.bwOption.setDatasetDownloadable(Boolean.parseBoolean(this.getTextBundle("isDatasetDownloadable", strMiddleOptName, dlgClass)));

			// �ϰ� DownLoad�۾��� ���Ǵ� Template DataSet Name
			this.strTemplateDSName = super.getTextBundle("TemplateDatasetName", null, dlgClass);

		} catch (Exception e)
		{
			MessageBox.post(super.shell, super.getTextBundle("AdminWorkInvalid", "MSG", super.dlgClass), "ERROR", 2);
		}

	}

	/**
	 * ȭ����� Template Excel Download ��� Ȱ��ȭ Excel Header �� Load Data Load
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
	 * Template Excel�� ��õ� Header ������ Load
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
		 * this.targetRevision.getProperty("item_id"); String strExcelFileName = strItemID + strTempExtention; // ����� Excel File FullPath this.strXlsFileFullPath = this.strImageRoot +
		 * File.separatorChar + strExcelFileName;
		 */

		Sheet sheet = this.wb.getSheetAt(0);

		this.headerModel = new BWItemModel();

		Row itemTypeRow = sheet.getRow(ITEM_TYPE_Y_POS);
		String strItemType = super.getCellText(itemTypeRow.getCell(ITEM_TYPE_X_POS));

		// Item Type ���� ���� Check
		if (strItemType == null || strItemType.equals(""))
		{
			throw new Exception(super.getTextBundle("XlsItemTypeBlank", "MSG", super.dlgClass));
		}

		// Item ���� Row
		Row classRow = sheet.getRow(ITEM_HEADER_START_Y_POS);
		// Item ���� �� �Ӽ� Row
		Row attrRow = sheet.getRow(ITEM_HEADER_START_Y_POS + 1);

		// ��ȿ Column ����, ù��° "A" Column�� Null �� �� �����Ƿ� +1
		this.nValidColumnCount = classRow.getPhysicalNumberOfCells() + 1;

		// Header Model ����
		for (int i = ITEM_HEADER_START_X_POS; i < nValidColumnCount; i++)
		{
			String strClass = super.getCellText(classRow.getCell(i)).trim();
			String strAttr = super.getCellText(attrRow.getCell(i)).trim();

			this.headerModel.setModelData(strClass, strAttr, new Integer(i));

		}

	}

	/*
	 * ���� Button Click�� ����
	 * 
	 * Excel ���� ���� Report Log ���� ����
	 */
	@Override
	public void execute() throws Exception
	{
		// ���� ��ó��
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
	 * ManualTreeItem�� ����� �Ӽ����� Excel�� ��õ� Model������ �������� ���
	 * 
	 * 
	 * @param treeItem
	 *            : Manual TreeItem
	 * @param sheet
	 *            : �Ӽ����� ����� Excel Sheet
	 * @throws Exception
	 */
	private void createExcel(ManualTreeItem treeItem, Sheet sheet) throws Exception
	{
		this.syncItemState(treeItem, STATUS_INPROGRESS, null);

		// Excel�� ��õ� Model������ �������� ���
		this.setRowData(sheet, treeItem);
		// �ش� Row �Ϸ� ó��
		this.syncItemState(treeItem, STATUS_COMPLETED, "Data Exported");

		this.nCurRow++;

		ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
		this.syncGetChildItem(treeItem, itemList);
		for (int i = 0; i < itemList.size(); i++)
		{
			ManualTreeItem childItem = itemList.get(i);
			if (childItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM))
			{

				// ���ȣ��
				this.createExcel(childItem, sheet);
			}
		}

	}

	/**
	 * ǥ�� Data Row�� �����Ͽ� ������ ���븸ŭ Row/Cell ���� ǥ�� CellStyle�� Copy
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
	 * ManualTreeItem�� ����� ��� �Ӽ� ���� Excel�� ���
	 * 
	 * @param sheet
	 *            : ��� Excel Sheet
	 * @param treeItem
	 *            : Manual TreeItem
	 * @throws Exception
	 */
	private void setRowData(Sheet sheet, ManualTreeItem treeItem) throws Exception
	{
		Row cRow = sheet.getRow(this.nCurRow);

		// ���ǵ� Item���� ��ŭ Looping
		for (int i = 0; i < CLASS_TYPE_LIST.length; i++)
		{
			// Item������ ���ǵ� �Ӽ� Map
			HashMap<String, Integer> attrIndexMap = this.headerModel.getModelAttrs(CLASS_TYPE_LIST[i]);
			Object[] attrNames = attrIndexMap.keySet().toArray();

			for (int j = 0; j < attrNames.length; j++)
			{
				// �ش� �Ӽ��� Excel Column ��ġ����
				Integer intObj = attrIndexMap.get(attrNames[j]);

				if (intObj == null)
					continue;

				// �Ӽ� ��
				String xlsCellValue = treeItem.getBWItemAttrValue(CLASS_TYPE_LIST[i], attrNames[j].toString());

				if (intObj.intValue() == ITEM_XLS_COLUMN_INDEX_LEVEL)
				{
					if (treeItem.getLevel() > -1)
						xlsCellValue = this.getLevel(treeItem.getLevel());
					else
						xlsCellValue = "";
				}

				// �Ӽ��� ���
				cRow.getCell(intObj.intValue()).setCellValue(xlsCellValue);
			}

		}

		this.syncItemState(treeItem, STATUS_INPROGRESS, super.getTextBundle("AttrPrinted", "MSG", super.dlgClass));

		// DownLoad ������ Option �̰� Dataset Type�� ��� ���� �ٿ�ε�
		if (this.bwOption.isDatasetDownloadable() && treeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCDATASET))
		{
			downLoadDataSetFile(treeItem);
			// dataset child�� �������� ����.
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
			// Item Type�� ���
			if (cTreeItem.getItemType().equals(ManualTreeItem.ITEM_TYPE_TCITEM))
			{
				// this.setRowData(sheet, cTreeItem, this.nCurRow++);
			}
			// Dataset Type�� ���
			else
			{
				// ù��° Dataset�� Excel���� Item �Ӽ��� �Բ� ǥ�õ�.
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
				// �ι�° Dataset ���� Item�� �ٸ� row�� ǥ�õ�
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
	 * Job�� UI Thread���� �浹�� ���� UI Update
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
	 * Error, Warning Count ���
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
	 * Level ���� String ��ȯ
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
	 * Dataset�� Reference File�� �ٿ�ε�
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
				this.syncItemState(treeItem, STATUS_COMPLETED, "DownLoad �Ϸ�.");

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
	 * ���õ� Target Item�� Bom �����Ͽ� ManualTreeItem ���� ManualTreeItem�� Excel�� �ʿ��� ��� �Ӽ����� ����
	 */
	@Override
	public void load() throws Exception
	{
		// ��ó��
		loadPre();

		// ManualTreeItem List
		this.itemList = new ArrayList<ManualTreeItem>();

		LoadJob job = new LoadJob(shell.getText());
		job.schedule();

		
	}

	/**
	 * BomLine,Item,ItemRevision,Dataset �Ӽ������� ManualTreeItem�� ����
	 * 
	 * @param bomLine
	 *            : ��� BomLine
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

				// Top Line�� ���, Level(Tree����) ������� �ʴ� ���
				if (pTreeItem == null || !bwOption.isLevelAvailable())
				{
					if (bwOption.isLevelAvailable())
					{
						mTreeItem = new ManualTreeItem(tree, tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strID);
						mTreeItem.setLevel(0);
					}
					// Level(Tree����)�� ���� �ʴ� ��� �ߺ��� Item�� �����Ѵ�.
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

					// Excel�� ��µ� Total Row ���� ����
					nTotalRow++;

					// ItemRevision �߰� �Ӽ� Setting
					//

					// DataSet�� ������� �ʴ� Option�̸� Skip�Ѵ�.
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
						// Object Name���� Sorting
						szTCComponent = SYMTcUtil.sortTCCompoentArray(session, szTCComponent, "object_name");

						int nDSCount = 0;
						for (int j = (szTCComponent.length - 1); j >= 0; j--)
						{
							TCComponent component = szTCComponent[j];

							// DownLoad ��õ� Dataset�� DownLoad��
							for (int i = 0; i < szDownLoadableDSType.length; i++)
							{

								// DownLoad ��õ� DataSet�� ���
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
										// Excel�� ��µ� Total Row ���� ����
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
	 * Excel�� ��õ� �Ӽ����� TreeItem Cell�� �ݿ�
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

			// �Ӽ� Update�� ���ʿ��� �Ӽ��� Skip �Ѵ�.
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
				// �԰�ȭ(���浵��)�� Hidden������ ������.. STCNPI���� �Ӽ��� ����..
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
		// *.xls, *.xlsx Filter ����
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
		// ��ó��
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

			// End ǥ�� Ȯ�� �ؾ� ��..
			// �ʼ� �Ӽ� ���� Ȯ�� �ؾ� ��..

			if (this.bwOption.isDatasetAvailable() && this.bwOption.isDatasetDownloadable())
			{
				this.checkFile(topTreeItem);
			}

		}

		// ��ó��
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

	/** �ʿ�� ���� Ŭ�������� ���� */
	@Override
	public void validatePre() throws Exception
	{

	}

	/** �ʿ�� ���� Ŭ�������� ���� */
	@Override
	public void validatePost() throws Exception
	{

	}

	/** �ʿ�� ���� Ŭ�������� ���� */
	@Override
	public void loadPre() throws Exception
	{

	}

	/** �ʿ�� ���� Ŭ�������� ���� */
	@Override
	public void loadPost() throws Exception
	{

	}

	/** �ʿ�� ���� Ŭ�������� ���� */
	@Override
	public void executePre() throws Exception
	{

	}

	/** �ʿ�� ���� Ŭ�������� ���� */
	@Override
	public void executePost() throws Exception
	{

	}
	

    /** �ʿ�� ���� Ŭ�������� ���� */
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

				// pack �� BOMLine �� �����Ͽ� �б�
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

				// ��ó��
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

					// Top TreeItem ���� ������ �����Ͽ� Excel Data ����
					createExcel(topTreeItem, sheet);

				}

				FileOutputStream out = new FileOutputStream(strXlsFileFullPath);

				wb.write(out);
				out.close();
				out = null;
				wb = null;

				// ���� ��ó��
				executePost();

				shell.getDisplay().syncExec(new Runnable()
				{

					public void run()
					{

						// **************************************//
						// Txt Upload Report ���� //
						// --------------------------------------//

						// Report Factory Instance : HeaderNames, HeaderWidths,
						// Num Column Display Flag, Level Column Display Flag
						TxtReportFactory rptFactory = generateReport(true, true);

						String strDate = DateUtil.getClientDay("yyMMddHHmm");
						String strFileName = "Export_" + strDate + ".log";
						strLogFileFullPath = strImageRoot + File.separatorChar + strFileName;

						// Import Log File ����
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
