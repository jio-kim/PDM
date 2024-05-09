package com.symc.plm.rac.prebom.masterlist.dialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.Iterator;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.symc.plm.rac.prebom.masterlist.model.MasterAndUssageFindKey;
import com.symc.plm.rac.prebom.masterlist.model.PreBOMMasterListDataUtil;
import com.symc.plm.rac.prebom.masterlist.model.UssageHeaderColum;

/**
 * [SR160621-031][20160707] taeku.jeong
 * �ְ� ������ ������ Pre-BOM �����͸� Ȱ���Ͽ� ������ ����� �� �ִ� ��� ����
 */
public class AllPreBOMUssageExportDialog extends TitleAreaDialog {
	
	private PreBOMMasterListDataUtil preBOMMasterListDataUtil;
	private UssageHeaderColum[] ussageHeaderColums = null;
	private Hashtable<String, MasterAndUssageFindKey> masterAndUssageFindKeyHash = null;
	private Shell parent;
	private String selectedDir = System.getProperty("user.home")+"\\Documents";
	private Label label;
	
	public static final String PREBOM_USSAGE_QUERY_SERVICE = "com.kgm.service.PreBOMUssageExportService";
	Table table;

	public AllPreBOMUssageExportDialog(Shell parent) {
		super(parent);
		this.parent = parent;
	}
	
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		// Set the title
		setTitle("All Pre-BOM Master List Export");
		setMessage("�ֱ� ������ Pre-BOM�� ��� Project�� Usage ������ Excel ���Ϸ� ��� �մϴ�.", IMessageProvider.INFORMATION);
		return contents;
	}
	
	protected Control createDialogArea(Composite parent) {
		
		Composite area = (Composite) super.createDialogArea(parent);
		
		table = new Table(area, SWT.BORDER | SWT.CHECK | SWT.MULTI  | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
	    table.setLayoutData(gd);

	    TableColumn tc1 = new TableColumn(table, SWT.LEFT);
	    tc1.setText("Project");
	    TableColumn tc2 = new TableColumn(table, SWT.CENTER);
	    tc2.setText("������ ��¥");    

	    tc1.setWidth(110);    
	    tc2.setWidth(150);
	    
	    table.setHeaderVisible(true);
	    
	    // �ӽ÷� ǥ�õ� Data�� ǥ���غ�.
	    TableItem item1 = new TableItem(table, SWT.NONE);    
	    item1.setText(new String[] { "Project 1", "DateTime1" });    
	    TableItem item2 = new TableItem(table, SWT.NONE);    
	    item2.setText(new String[] { "Project 2", "DateTime2" });    
	    
//	    tc1.pack();
//	    tc2.pack();
	    
	    label = new Label(area, SWT.BORDER | SWT.WRAP);
	    label.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
	    label.setText("�Ʒ��� ���� ��ư�� ������ ���� ��ġ�� �����ϼ���.");
	    label.setBounds(0, 0, area.getBounds().width, 60);
	    label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
	    // ���� Table�� ǥ���� Data�� List Up�Ѵ�.
		setTableData();
	    
	    return area;
	}
	
	
	private void folderSelection() {
		System.out.println("Folder Button Pressed");
		
		DirectoryDialog directoryDialog = new DirectoryDialog(this.parent);
        
        directoryDialog.setFilterPath(selectedDir);
        directoryDialog.setMessage("Please select a directory and click OK");
        
        String dir = directoryDialog.open();
        if(dir != null) {
          label.setText("Selected dir: " + dir);
          selectedDir = dir;
        }
	}

	@Override
	protected void cancelPressed() {
		super.cancelPressed();
	}

	@Override
	protected void okPressed() {
		
		folderSelection();
		
		ArrayList<Hashtable<String, String>> listData = getSelectedTargetProjectList();
		for (int i = 0;listData!=null && i < listData.size(); i++) {
			String project = listData.get(i).get("Project");
			String eaiCreateDate = listData.get(i).get("EAICreateDate");
			String isChecked = listData.get(i).get("IsChecked");

			System.out.println("project = "+project+", eaiCreateDate = "+eaiCreateDate+", isChecked = "+isChecked);
		}
		
		printSelectedProjects();

	}
	
	private ArrayList<Hashtable<String, String>> getSelectedTargetProjectList(){

		ArrayList<Hashtable<String, String>> listData = null;
		
		TableItem[] selected = table.getItems();
		if(selected!=null && selected.length>0){
			listData = new ArrayList<Hashtable<String, String>>();
		}
		for (int i = 0; i < selected.length; i++) {
			String project = selected[i].getText(0);
			String eaiCreateDate = selected[i].getText(1);
			String isChecked = ""+selected[i].getChecked();
			
			Hashtable<String, String> aHash = new Hashtable<String, String>();
			aHash.put("Project", project);
			aHash.put("EAICreateDate", eaiCreateDate);
			aHash.put("IsChecked", isChecked);
			listData.add(aHash);
		}
		
		return listData;
	}
	
	/**
	 * Table�� ǥ���� Data�� ǥ���ϴ� ����� ���� �Ѵ�.
	 */
	private void setTableData(){
		
		preBOMMasterListDataUtil = new PreBOMMasterListDataUtil();
		masterAndUssageFindKeyHash = preBOMMasterListDataUtil.getProjectDataLIst();
		
		table.removeAll();
		
		Collection<MasterAndUssageFindKey> collection = masterAndUssageFindKeyHash.values();
		for (Iterator iterator = collection.iterator(); iterator.hasNext();) {
			MasterAndUssageFindKey masterAndUssageFindKey = (MasterAndUssageFindKey) iterator.next();
			
			String projectCode = masterAndUssageFindKey.projectCode;
			String eaiCreateTime = masterAndUssageFindKey.eaiCreateTime;
			
        	TableItem tempTableItem = new TableItem(table, SWT.NONE);    
        	tempTableItem.setText(new String[] { projectCode, eaiCreateTime});	
		}
		
	}
	
	private void printSelectedProjects(){
		File targetFoler = new File(selectedDir);
		if(targetFoler!=null && targetFoler.exists() && targetFoler.isDirectory()){
			this.preBOMMasterListDataUtil.setExportTargetFolderPath(selectedDir);
		}
		
		ArrayList<Hashtable<String, String>> selectedProjectList = getSelectedTargetProjectList();
		for (int i = 0;selectedProjectList!=null && i < selectedProjectList.size(); i++) {
			
			String project = selectedProjectList.get(i).get("Project");
			String eaiCreateDate = selectedProjectList.get(i).get("EAICreateDate");
			String isCheckedString = selectedProjectList.get(i).get("IsChecked");
			boolean isChecked = false;
			if(isCheckedString!=null && isCheckedString.trim().equalsIgnoreCase("TRUE")==true){
				isChecked = true;
			}
			
			if(isChecked==true){
				MasterAndUssageFindKey aMasterAndUssageFindKey = masterAndUssageFindKeyHash.get(project);
				printSelectedProjectData(aMasterAndUssageFindKey);
			}
		}
		
		setReturnCode(0);
		close();
	}
	
	private void printSelectedProjectData(MasterAndUssageFindKey masterAndUssageFindKey){
		
		UssageHeaderColum[] ussageHeaderColums = this.preBOMMasterListDataUtil.getUssageColumnHeaderData(masterAndUssageFindKey);
		String newFilePath = this.preBOMMasterListDataUtil.readyExcelFile(masterAndUssageFindKey, ussageHeaderColums);
		
		// Print Master & Ussage Data
		if(newFilePath!=null && new File(newFilePath).exists()){
			this.preBOMMasterListDataUtil.printProjectMasterData(masterAndUssageFindKey, ussageHeaderColums, newFilePath);
		}
	}

}
