package com.ssangyong.commands.ec.assign;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.ssangyong.dto.ApprovalLineData;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
/**
 * 저장된 결재선 검색 화면
 * Select 버튼 혹은 테이블 더블클릭하면
 * 선택 정보를 반환함.
 * 
 * [SR141106-028][20141106]shcho, Save시 My approval line name prefix값 변경 불가능 하도록 Dialog 수정
 *  
 * @author DJKIM
 *
 */
public class AskMyAssignDialog {
	
	private Shell shell;
	private TCSession session;
	private Table approvalLineTable;
	
	private Text lineNameText;
	private Label lineNamePrefix;
	private String lineName;
	private String approvalLineTitle;
	private boolean isPrefix; //approvalLineTitle 과 같이 Prefix를 사용하는 Dialog인지 여부
	
	private Button saveButton, cancelButton;

	public AskMyAssignDialog(Shell parent, Table approvalLineTable, TCSession session) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Saved Assign Info");
		shell.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, true));
		SYMDisplayUtil.centerToScreen(shell);
		this.approvalLineTable = approvalLineTable;
		this.session = session;
		this.isPrefix=false;
	}
	
	public AskMyAssignDialog(Shell parent, Table approvalLineTable, String approvalLineTitle, TCSession session) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Saved Assign Info");
		shell.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, true));
		SYMDisplayUtil.centerToScreen(shell);
		this.approvalLineTitle = approvalLineTitle;
		this.approvalLineTable = approvalLineTable;
		this.session = session;
		this.isPrefix = true;
	}

	public void open() {
		createSearchLayout();
		createButtonLayout();
		
		shell.pack();
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}

	
    /**
     * [SR141106-028][20141106]shcho, Save시 My approval line name prefix값 변경 불가능 하도록 Dialog 수정
     */
	private void createSearchLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout (2, false));
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label groupLabel = new Label(composite, SWT.RIGHT);
		groupLabel.setText("My approval line name : ");
		
		if(isPrefix) {
    		Composite subComposite = new Composite(composite, SWT.NONE);
    		subComposite.setLayout(new GridLayout (2, false)); 

    		lineNamePrefix = new Label(subComposite, SWT.NONE);
    		GridData labelVerticalyAlignedGridData = new GridData(); 
    		labelVerticalyAlignedGridData.verticalAlignment = SWT.CENTER; 
    		labelVerticalyAlignedGridData.grabExcessVerticalSpace = true; 
    		labelVerticalyAlignedGridData.horizontalAlignment = SWT.RIGHT; 
    		lineNamePrefix.setLayoutData(labelVerticalyAlignedGridData); 
    		lineNamePrefix.setText(approvalLineTitle+"_");
    
    		lineNameText = new Text(subComposite, SWT.BORDER);
            lineNameText.setLayoutData(new GridData(100, SWT.DEFAULT));
			lineNameText.setEditable(true);
		} else {
            lineNameText = new Text(composite, SWT.BORDER);
            lineNameText.setLayoutData(new GridData(220, SWT.DEFAULT));
		}
		
		GridData data = new GridData (SWT.FILL, SWT.CENTER, true, true);
		data.horizontalSpan = 2;
        Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(data);
	}
	
	private void createButtonLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_CENTER));
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		
        saveButton = new Button(composite, SWT.PUSH);
        saveButton.setText("Save");
        saveButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				save();
				shell.close();
			}
		});
		
		cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
	}

	/**
	 * [SR141106-028][20141106]shcho, Save시 My approval line name prefix값 변경 불가능 하도록 Dialog 수정
	 */
	private void save() {
	    if(isPrefix) {
	        String strPrefix = lineNamePrefix.getText();
	        String strLineName = lineNameText.getText();
	        
	        if(strPrefix.equals("") || strPrefix.equals("_")) {
                MessageBox.post(shell, "Name Prefix ERROR! \nSelect the template before saving.", "ERROR", MessageBox.ERROR);
            } else {
                if(strLineName.equals("")) {
                    lineName = approvalLineTitle;
                } else {
                    lineName = strPrefix + strLineName;                
                }
                saveUserApprovalLine();
            }
	    } else {
	        lineName = lineNameText.getText();
	        
            if(lineName.equals("")) {
                MessageBox.post(shell, "Input the Name!", "ERROR", MessageBox.ERROR);
            } else {
            	saveUserApprovalLine();
           	}
	    }
	}

	/** 결재선 저장 */
	private void saveUserApprovalLine() {
		ArrayList<ApprovalLineData> paramList = new ArrayList<ApprovalLineData>();
		TableItem[] itemList = approvalLineTable.getItems();
		
		CustomECODao dao = new CustomECODao();
		
		try {
			ApprovalLineData map = new ApprovalLineData();
			map.setSaved_name(lineName);
			map.setSaved_user(session.getUser().getUserId());
			map.setEco_no(SYMCECConstant.ECO_PROCESS_TEMPLATE);

			ArrayList<ApprovalLineData> resultSavedApprovalLines = dao.loadApprovalLine(map);

			if(resultSavedApprovalLines != null && resultSavedApprovalLines.size() > 0){
				MessageBox.post(shell, "The name already exist!", "ERROR", MessageBox.ERROR);
				return;
			}
			
			int i = 0;
			for(TableItem item : itemList){
				ApprovalLineData theLine = new ApprovalLineData();
				theLine.setEco_no(SYMCECConstant.ECO_PROCESS_TEMPLATE);
				theLine.setSaved_name(lineName);
				theLine.setSaved_user(session.getUser().getUserId());
				theLine.setSort(i+"");
				theLine.setTask(item.getText(0));
				theLine.setTeam_name(item.getText(1));
				theLine.setUser_name(item.getText(2));
				theLine.setTc_member_puid((String)item.getData("puid"));

				paramList.add(theLine);
				i++;
			}
			dao.saveUserApprovalLine(paramList);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(shell, e.getMessage(), "ERROR", MessageBox.ERROR);
		}
	}
}
