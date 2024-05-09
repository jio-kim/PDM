/**
 * Part/BOM �Ӽ� �ϰ� Upload Dialog
 * ����Ŭ����(BWXLSImpDialog)��  ������ ����� ����( ���� �߰� ���߽� ���� ����� Override�Ͽ� ���� �Ͽ��� ��)
 * �۾�Option�� bundlework_locale_ko_KR.properties�� ���� �Ǿ� ����
 */
package com.kgm.common.bundlework.imp;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.commands.ec.search.ECOSearchDialog;
import com.kgm.common.bundlework.BWXLSImpDialog;
import com.kgm.common.bundlework.bwutil.BWItemModel;
import com.kgm.common.swtsearch.SearchItemRevDialog;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DateUtil;
import com.kgm.common.utils.TxtReportFactory;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

public class BWReviseAllDialog extends BWXLSImpDialog
{
  TCComponentBOMWindow bomWindow = null;
  TCComponentItemRevision targetRevision;
  
  ArrayList<String> revisedItems;

  public BWReviseAllDialog(Shell parent, int style, TCComponentItemRevision targetRevision)
  {
    super(parent, style, BWReviseAllDialog.class);

    this.targetRevision = targetRevision;
    this.revisedItems = new ArrayList<String>();
    
  }

  @Override
  public void dialogOpen()
  {
    this.excelFileGroup.setBounds(10, 10, 769, 60);
    this.fileText.setBounds(10, 25, 390, 24);
    this.fileText.setEnabled(false);

    this.searchButton.setBounds(410, 26, 77, 22);
    this.logGroup.setBounds(10, 75, 863, 481);
    this.tree.setBounds(10, 22, 843, 300);
    this.text.setBounds(10, 330, 843, 141);
    this.executeButton.setBounds(338, 576, 77, 22);
    this.cancelButton.setBounds(459, 576, 77, 22);
    this.viewLogButton.setBounds(750, 576, 120, 22);

    this.shell.addDisposeListener(new DisposeListener(){

      @Override
      public void widgetDisposed(DisposeEvent e) {

        try
        {
          if( bomWindow != null)
          {
            bomWindow.close();
            bomWindow = null;
          }
        }
        catch(TCException ex)
        {
          ex.printStackTrace();
        }
      }
      
    });
    
    this.shell.open();
    this.shell.layout();
  }

	/**
	 * �˻� Button Click�� ����
	 */
	@Override
	public void selectTarget() throws Exception
	{
		//20230831 cf-4357 seho ��Ʈ �˻� dialog�� ����ϴ� ��� ECO�˻� â�� ����ϵ��� ������.
		ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(this.shell, SWT.SINGLE);
		ecoSearchDialog.getShell().setText("ECO Search");
		ecoSearchDialog.setAllMaturityButtonsEnabled(false);
		ecoSearchDialog.setBInProcessSelect(false);
		ecoSearchDialog.setBCompleteSelect(false);
		ecoSearchDialog.open();
		// �˻��� Eco Item
		TCComponentItemRevision[] selectedItems = ecoSearchDialog.getSelectctedECO();

//		SearchItemRevDialog itemDialog = new SearchItemRevDialog(this.shell, SWT.SINGLE, "ECORevision");
//		TCComponentItemRevision[] selectedItems = (TCComponentItemRevision[]) itemDialog.open();

		if (selectedItems != null)
		{
			fileText.setText(selectedItems[0].toDisplayString());
			fileText.setData(selectedItems[0]);

			// Excel Data Loading
			load();

			// Validation ���� �� �����ư Ȱ��ȭ
			if (validate())
				super.executeButton.setEnabled(true);
		}

	}

  /**
   * Validation Check
   */
  @Override
  public boolean validate() throws Exception
  {
    // Validation ��ó��
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

      // File ���� ���� Check
      this.checkStageRegular(topTreeItem);
    }

    // Validation ��ó��
    validatePost();

    // Error Count, Warning Count
    int[] szErrorCount = { 0, 0 };
    for (int i = 0; i < szTopItems.length; i++)
    {
      // Top TreeItem
      ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
      // Error Count ����
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
    }
    else
      return true;
  }

  private void checkStageRegular(ManualTreeItem treeItem) throws Exception
  {

    String strStage = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_STAGE");
    String strRegular = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_REGULAR_PART");

    if (CustomUtil.isEmpty(strStage))
    {
      treeItem.setStatus(STATUS_ERROR, "'STAGE' Attribute is Required");
    }

    if (CustomUtil.isEmpty(strRegular))
    {
      treeItem.setStatus(STATUS_ERROR, "'Reqular' Attribute is Required");
    }

    if (!"R".equals(strRegular))
    {
      treeItem.setStatus(STATUS_ERROR, " Only 'R' Permitted On 'Regular' Attribute ");
    }

    if (!"P".equals(strStage))
    {

      this.checkParentStage(treeItem, (ManualTreeItem) treeItem.getParentItem());

      if (!CustomUtil.isReleased(treeItem.getTcComp()))
      {
        //treeItem.setStatus(STATUS_ERROR, "Can't Revise(UnRelease State)");
      }

    }

    TreeItem[] childItems = treeItem.getItems();
    for (int i = 0; i < childItems.length; i++)
    {
      ManualTreeItem cItem = (ManualTreeItem) childItems[i];
      this.checkStageRegular(cItem);

    }

  }

  private void checkParentStage(ManualTreeItem treeItem, ManualTreeItem pTreeItem)
  {
    if (pTreeItem == null)
      return;

    String strStage = pTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_STAGE");

    if ("P".equals(strStage))
    {
      treeItem.setStatus(STATUS_ERROR, " Invalid Parent Stage Value : P");
      return;
    }

    checkParentStage(treeItem, (ManualTreeItem) pTreeItem.getParentItem());
  }

  @Override
  public void load() throws Exception
  {
    Display.getDefault().syncExec(new Runnable()
    {
      public void run()
      {
        try
        {
          // Load ��ó��
          loadPre();
          // ManualTreeItem List
          itemList = new ArrayList<ManualTreeItem>();
          // TCComponentItemRevision List
          tcItemRevSet = new HashMap<String, TCComponentItemRevision>();
          shell.setCursor(waitCursor);
          tree.removeAll();
          // Header ���� Loading
          loadHeader();

          // ManualTreeItem List
          itemList = new ArrayList<ManualTreeItem>();

          // Load BOM
          LoadJob job = new LoadJob(shell.getText(), targetRevision);
          job.schedule();

          // loadOption();

          if (validate())
          {
            executeButton.setEnabled(true);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
          MessageBox.post(shell, e.getMessage(), "Notification", 2);
        }
        finally
        {
          // Load ��ó��
          try
          {
            loadPost();
          }
          catch (Exception e)
          {
            e.printStackTrace();
          }
          finally
          {
            shell.setCursor(arrowCursor);
          }
        }
      }
    });
  }

  /**
   * ��� ����
   * 
   * @method loadHeader
   * @date 2013. 2. 19.
   * @param
   * @return void
   * @exception
   * @throws
   * @see
   */
  private void loadHeader() throws Exception
  {
    String[] szClass = new String[] { "Item", "Item", "Revision", "Item", "Revision", "Revision" };
    String[] szAttr = new String[] { "Level", "item_id", "item_revision_id", "object_name", "s7_STAGE", "s7_REGULAR_PART" };
    this.headerModel = new BWItemModel();
    // Type ���� (BOM)
    this.strTargetItemType = "S7_Product";
    for (int i = 0; i < szClass.length; i++)
    {
      this.headerModel.setModelData(szClass[i], szAttr[i], new Integer(i));
    }
  }

  public class LoadJob extends Job
  {

    TCComponentItemRevision targetRevision;

    public LoadJob(String jobName, TCComponentItemRevision targetRevision)
    {

      super(jobName);
      this.targetRevision = targetRevision;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor)
    {


      try
      {

        // pack �� BOMLine �� �����Ͽ� �б�
        //session.getPreferenceService().setString(TCPreferenceService.TC_preference_user, "PSEAutoPackPref", "0");
        session.getPreferenceService().setStringValue("PSEAutoPackPref", "0");
        
        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
        bomWindow = windowType.create(ruleType.getDefaultRule());
        TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, (TCComponentItemRevision) targetRevision, null, null);

        loadBOMData(topLine, null, 0);


        shell.getDisplay().syncExec(new Runnable()
        {

          public void run()
          {
            try
            {
              if (validate())
                executeButton.setEnabled(true);
            }
            catch (Exception e)
            {
              e.printStackTrace();
            }

          }
        });

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }


      return new Status(IStatus.OK, "Loading ", "Job Completed");
    }

  }

  /**
   * BomLine,Item,ItemRevision,Dataset �Ӽ������� ManualTreeItem�� ����
   * 
   * @param bomLine : ��� BomLine
   * @param pTreeItem :
   * @throws Exception
   */
  @SuppressWarnings("unused")
private void loadBOMData(final TCComponentBOMLine bomLine, final ManualTreeItem pTreeItem, final int nLevel) throws Exception
  {

    final TCComponentItemRevision itemRevision = bomLine.getItemRevision();

    final String strID = itemRevision.getProperty("item_id");
    final String strRevision = itemRevision.getProperty("item_revision_id");


    shell.getDisplay().syncExec(new Runnable()
    {

      public void run()
      {
        ManualTreeItem mTreeItem = null;

        // Top Line�� ���, Level(Tree����) ������� �ʴ� ���
        if (pTreeItem == null)
        {

          mTreeItem = new ManualTreeItem(tree, tree.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strID);
          mTreeItem.setLevel(0);

        }
        else
        {
          mTreeItem = new ManualTreeItem(pTreeItem, pTreeItem.getItemCount(), ManualTreeItem.ITEM_TYPE_TCITEM, strID);
          mTreeItem.setLevel(nLevel);

        }

        tree.setSelection(mTreeItem);

        itemList.add(mTreeItem);

        try
        {

          setSTCItemData(mTreeItem, bomLine, CLASS_TYPE_BOMLINE);
          setSTCItemData(mTreeItem, bomLine.getItem(), CLASS_TYPE_ITEM);
          setSTCItemData(mTreeItem, itemRevision, CLASS_TYPE_REVISION);

          mTreeItem.setTcComp(itemRevision);

        }
        catch (Exception e)
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

  public void setSTCItemData(ManualTreeItem treeItem, TCComponent component, String strType) throws TCException
  {
    HashMap<String, Integer> attrIndexMap = this.headerModel.getModelAttrs(strType);
    Object[] attrNames = attrIndexMap.keySet().toArray();

    for (int i = 0; i < attrNames.length; i++)
    {
      String strAttrName = (String) attrNames[i];

      String strAttrValue = component.getProperty((String) attrNames[i]);
      treeItem.setBWItemAttrValue(strType, strAttrName, strAttrValue);

    }
    this.setTreeItemData(treeItem, strType);
  }

  /**
   * ManualTreeItem���� Load�� Data�� Server�� Upload
   * 
   * @throws Exception
   */
  @Override
  public void execute() throws Exception
  {
    // ���� ��ó��
    executePre();

    if (this.nWraningCount > 0)
    {
      org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
      box1.setMessage(this.nWraningCount + this.getTextBundle("WarningIgnore", "MSG", dlgClass));

      if (box1.open() != SWT.OK)
      {
        return;
      }
    }

    // ���� ��ư Disable
    super.executeButton.setEnabled(false);
    // Excel �˻� ��ư Disable
    super.searchButton.setEnabled(false);

    // Top TreeItem Array
    TreeItem[] szTopItems = super.tree.getItems();

    // TreeItem�� �������� �ʴ� ���
    if (szTopItems == null || szTopItems.length == 0)
    {
      MessageBox.post(super.shell, super.getTextBundle("UploadInvalid", "MSG", dlgClass), "Notification", 2);
      return;
    }

    ExecutionJob job = new ExecutionJob(shell.getText(), szTopItems);
    job.schedule();

  }

  /**
   * Item ����
   * 
   * @param treeItem
   */
  private void reviseData(ManualTreeItem treeItem) throws Exception
  {

    this.syncSetItemSelection(treeItem);
    
    String strStage = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_STAGE");
    if(!"P".equals(strStage))
    {
      String strItemID = treeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "item_id");

      if( revisedItems.contains(strItemID))
      {
        this.syncItemState(treeItem, STATUS_COMPLETED, "Already Revised");
      }
      else
      {
      
        try
        {
          
          TCComponentItemRevision newRevision = CustomUtil.reviseForItemRev((TCComponentItemRevision)treeItem.getTcComp(), true, true, true, false, "", "P", (TCComponentItemRevision)this.text.getData());
          String strOldRev = treeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "item_revision_id");
          String strNewRev = newRevision.getProperty("item_revision_id");
          
          this.syncItemState(treeItem, STATUS_COMPLETED, "Revise Completed("+strOldRev+"->"+strNewRev+")");
          
          revisedItems.add(strItemID);
          
        }
        catch (Exception e)
        {
    
          this.syncItemState(treeItem, STATUS_ERROR, e.toString());
          e.printStackTrace();
        }
      }
    }
    else
    {
      this.syncItemState(treeItem, STATUS_SKIP, "");
    }

    ArrayList<ManualTreeItem> itemList = new ArrayList<ManualTreeItem>();
    this.syncGetChildItem(treeItem, itemList);

    for (int i = 0; i < itemList.size(); i++)
    {
      // ���ȣ��
      this.reviseData(itemList.get(i));
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
        for (int i = 0; i < szTopItems.length; i++)
        {
          // Top TreeItem
          ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];

          // Item,DataSet ����
          reviseData(topTreeItem);

        }

        shell.getDisplay().syncExec(new Runnable()
        {

          public void run()
          {
            shell.setCursor(waitCursor);
            // **************************************//
            // Txt Upload Report ���� //
            // --------------------------------------//

            // Report Factory Instance : HeaderNames, HeaderWidths, Num Column
            // Display Flag, Level Column Display Flag
            TxtReportFactory rptFactory = generateReport(true, true);

            if (strImageRoot == null || "".equals(strImageRoot))
            {
              strImageRoot = "c:/temp";

              File temp = new File(strImageRoot);
              if (!temp.exists())
              {
                temp.mkdirs();
              }
            }

            String strDate = DateUtil.getClientDay("yyMMddHHmm");
            String strFileName = "Import_" + strDate + ".log";
            // Upload Log File FullPath
            strLogFileFullPath = strImageRoot + File.separatorChar + strFileName;

            // Import Log File ����
            rptFactory.saveReport(strLogFileFullPath);

            // Error Count, Warning Count
            int[] szErrorCount = { 0, 0 };
            for (int i = 0; i < szTopItems.length; i++)
            {

              // Top TreeItem
              ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];
              getErrorCount(topTreeItem, szErrorCount);
            }
            nWraningCount = szErrorCount[1];

            BWReviseAllDialog.this.text.append("--------------------------\n");
            BWReviseAllDialog.this.text.append("Warning : " + szErrorCount[1] + "\n\n");
            BWReviseAllDialog.this.text.append("Error : " + szErrorCount[0] + "\n\n\n");
            text.append("[" + strLogFileFullPath + "] " + getTextBundle("LogCreated", "MSG", dlgClass) + "\n\n");

            if (szErrorCount[0] > 0)
            {
              text.append(getTextBundle("ActionNotCompleted", "MSG", dlgClass) + "\n");
              MessageBox.post(shell, getTextBundle("ActionNotCompleted", "MSG", dlgClass), "Error", 2);

            }
            else
            {
              text.append(getTextBundle("ActionCompleted", "MSG", dlgClass) + "\n");
              MessageBox.post(shell, getTextBundle("ActionCompleted", "MSG", dlgClass), "Notification", 2);
            }
            text.append("--------------------------\n");

            searchButton.setEnabled(true);
            viewLogButton.setEnabled(true);

          }
        });

      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      finally
      {

        try
        {
          // ���� �� ó��
          executePost();
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }

        shell.getDisplay().syncExec(new Runnable()
        {

          public void run()
          {
            shell.setCursor(arrowCursor);
          }
        });

      }

      return new Status(IStatus.OK, "Exporting", "Job Completed");

    }

  }

}
