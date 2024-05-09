package com.kgm.commands.ec.search;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DatasetService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.util.MessageBox;

/*
 * [20170314][ymjang] 암호화 문서 경고 문구 삽입
 */
public class FileAttachmentComposite {
    
	private String[] columnName = new String[]{"File Name", "Creator", "Creation Date"};
	private int[] columnSize = new int[]{363, 150, 150};
	
	private Button addFileButton, deleteFileButton;
	private Table  resultTable;
	public Group group;
	private boolean isFileModified = false;
	
    public boolean isFileModified() {
		return isFileModified;
	}

  public FileAttachmentComposite(Composite parent, GridData layoutData) {
      group = new Group (parent, SWT.NONE);
      group.setLayout (new GridLayout(3, false));
      group.setText ("Attachment Doc.");

      group.setLayoutData(layoutData);
      createComposite();
  }
    
	public FileAttachmentComposite(Composite parent) {
    	group = new Group (parent, SWT.NONE);
		group.setLayout (new GridLayout(3, false));
		group.setText ("Attachment Doc.");
		GridData layoutData = new GridData (SWT.FILL, SWT.FILL, true, false);
		layoutData.minimumHeight = 250;
		group.setLayoutData(layoutData);
		createComposite();
  }
    
    public Composite createComposite(){
		//Label lbl_blank = new Label(group, SWT.RIGHT);
		GridData layoutData = new GridData (SWT.FILL, SWT.CENTER, true, false);
		//lbl_blank.setLayoutData(layoutData);
		
		Label lblAttachFileMsg = new Label(group, SWT.NONE);
		lblAttachFileMsg.setFont(SWTResourceManager.getFont("맑은 고딕", 13, SWT.BOLD));
		lblAttachFileMsg.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		lblAttachFileMsg.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1));
		lblAttachFileMsg.setText("※ 암호화된 문서는 반드시 암호를 해제하신 후, 등록하셔야 합니다. ※");
		
		addFileButton = new Button(group, SWT.NONE);
		addFileButton.setText("Add");
		addFileButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				addFile();
			}
		});
		
		deleteFileButton = new Button(group, SWT.NONE);
		deleteFileButton.setText("Delete");
		deleteFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteFile();
			}
		});
		
		layoutData = new GridData (SWT.FILL, SWT.CENTER, true, true);
		layoutData.horizontalSpan = 3;
		layoutData.minimumHeight=80;
		
		resultTable = new Table(group, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.setLayoutData(layoutData);
		
		
		int i = 0;
		for(String value : columnName){
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(columnSize[i]);
			i++;
		}		
		return group;
    	
    }
    
    private void addFile(){
    	FileDialog fileDialog = new FileDialog(resultTable.getShell(), SWT.OPEN);
		fileDialog.setFilterExtensions(new String[] {"*.xls;", "*.*"});
		fileDialog.setFilterNames(new String[] {"Excel (*.xls)", "All Files (*.*)"});
		String name = fileDialog.open();
		if(name == null) return;
		File file = new File(name);
		if (!file.exists()) {
			MessageBox.post(resultTable.getShell(), "File "+file.getName()+" "+" Does_not_exist", "ERROR", MessageBox.ERROR);
			return;
		}
		TableItem item = new TableItem(resultTable, SWT.NONE);
		item.setText(0, file.getName());
		item.setText(1, CustomUtil.getTCSession().getUserName());
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		item.setText(2, dateFormat.format(new Date()));
		item.setData("path", file.getAbsolutePath());
		isFileModified = true;
    }
    
	public void createDatasetAndMakerelation(TCComponentItemRevision revision) throws Exception {
		TCComponent[] references = revision.getRelatedComponents(SYMCECConstant.DATASET_REL);
		for(TCComponent reference : references){
			if(reference instanceof TCComponentDataset){
			  revision.remove(SYMCECConstant.DATASET_REL, reference);
			}
		}

		TableItem[] items = resultTable.getItems();
		int itemCount = items.length;
		for(int i = 0 ;  i < itemCount ; i++){
			DatasetService.createService(CustomUtil.getTCSession());
			TCComponentDataset dataSet = DatasetService.createDataset((String)items[i].getData("path"));
			revision.add(SYMCECConstant.DATASET_REL, dataSet);
		}
		
		isFileModified = false;
	}
	
	public void createDatasetAndMakerelation(TCComponentItem item) throws Exception {
		TCComponent[] references = item.getRelatedComponents(SYMCECConstant.ITEM_DATASET_REL);
		for(TCComponent reference : references){
			if(reference instanceof TCComponentDataset){
				item.remove(SYMCECConstant.ITEM_DATASET_REL, reference);
			}
		}

		TableItem[] items = resultTable.getItems();
		int itemCount = items.length;
		for(int i = 0 ;  i < itemCount ; i++){
			DatasetService.createService(CustomUtil.getTCSession());
			TCComponentDataset dataSet = DatasetService.createDataset((String)items[i].getData("path"));
			item.add(SYMCECConstant.ITEM_DATASET_REL, dataSet);
		}
		
		isFileModified = false;
	}
	
    private void deleteFile(){
		TableItem[] items = resultTable.getSelection();
		if (items.length == 0) return;
		items[0].dispose();
		isFileModified = true;
    }
    
	public void resizeTable() {
		GridData layoutData = new GridData (SWT.FILL, SWT.FILL, true, true);
		resultTable.setLayoutData(layoutData);
	}
    
	public void roadDataSet(TCComponentItemRevision revision) throws Exception {
		if(resultTable.getItemCount() > 0) resultTable.removeAll();
		
		TCComponent[] references = revision.getRelatedComponents(SYMCECConstant.DATASET_REL);
		for(TCComponent reference : references){
			if(reference instanceof TCComponentDataset){
				
				TCComponentDataset dataset = (TCComponentDataset) reference;
				
				DatasetService.createService(CustomUtil.getTCSession());
				File[] files = DatasetService.getFiles(dataset);
				/**
				 * [SR140930-009][20141016][jclee] File Creator, Creation Date 정상 표기 수정.
				 */
				Date dCreationDate = reference.getDateProperty("creation_date");
				String sOwner = ((TCComponentUser) reference.getReferenceProperty("owning_user")).getUserId();
				
				for(File file : files){
					TableItem item = new TableItem(resultTable, SWT.NONE);
					item.setText(0, file.getName());
//					item.setText(1, CustomUtil.getTCSession().getUserName());
					item.setText(1, sOwner);
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//					item.setText(2, dateFormat.format(new Date()));
					item.setText(2, dateFormat.format(dCreationDate));
					item.setData("path", file.getAbsolutePath());
					System.out.println(file.getAbsolutePath());
				}
			}
		}
	}
	
	public void roadDataSet(TCComponentItem revision) throws Exception {
		if(resultTable.getItemCount() > 0) resultTable.removeAll();
		
		TCComponent[] references = revision.getRelatedComponents(SYMCECConstant.ITEM_DATASET_REL);
		for(TCComponent reference : references){
			if(reference instanceof TCComponentDataset){
				
				TCComponentDataset dataset = (TCComponentDataset) reference;
				
				DatasetService.createService(CustomUtil.getTCSession());
				File[] files = DatasetService.getFiles(dataset);
				for(File file : files){
					TableItem item = new TableItem(resultTable, SWT.NONE);
					item.setText(0, file.getName());
					item.setText(1, CustomUtil.getTCSession().getUserName());
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					item.setText(2, dateFormat.format(new Date()));
					item.setData("path", file.getAbsolutePath());
					System.out.println(file.getAbsolutePath());
				}
			}
		}
	}	
	
}