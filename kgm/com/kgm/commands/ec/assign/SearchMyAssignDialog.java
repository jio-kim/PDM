package com.kgm.commands.ec.assign;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
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
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.SYMCLOVCombo;
import com.kgm.common.utils.SYMDisplayUtil;
import com.kgm.dto.ApprovalLineData;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
/**
 * 사용자 지정 결재선 정보를 보여 줌
 * @author DJKIM
 *
 */
public class SearchMyAssignDialog {
	
	private Shell shell;
	private TCSession session;
	private String[] approvalLines;
	private SYMCLOVCombo approvalLineCombo;
	private Button deleteButton;
	
	private Table resultTable;
	private String[] tableColumns = new String[]{"Task", "Dept.", "User", "puid"};
	private int[] tableColSizes = new int[]{300, 200, 120, 0};
	
	private Button selectButton, cancelButton;

	@SuppressWarnings("unused")
    private String org_code;
	/** Return Value[선택 값] **/
	private ArrayList<ApprovalLineData> resultSavedApprovalLines;
	private boolean isECI = false;

	public SearchMyAssignDialog(Shell parent, TCSession session, String[] approvalLines) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Search Saved Assign Info");
		SYMDisplayUtil.centerToScreen(shell);
		this.session = session;
		this.approvalLines = approvalLines;
	}
	
	public SearchMyAssignDialog(Shell parent, TCSession session, String[] approvalLines, boolean isECI) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Search Saved Assign Info");
		SYMDisplayUtil.centerToScreen(shell);
		this.session = session;
		this.approvalLines = approvalLines;
		this.isECI = isECI;
	}
	
	public SearchMyAssignDialog(Shell parent, TCSession session, String[] approvalLines, String org_code) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Search Saved Assign Info");
		SYMDisplayUtil.centerToScreen(shell);
		this.session = session;
		this.approvalLines = approvalLines;
		this.org_code = org_code;
	}

	public ArrayList<ApprovalLineData> open() {
		createSearchLayout();
		createTableLayout();
		createButtonLayout();
		shell.pack();
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return resultSavedApprovalLines;
	}

	private void createSearchLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout (4, false));
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label groupLabel = new Label(composite, SWT.RIGHT);
		groupLabel.setText("Select Saved Approval Line : ");
		
		approvalLineCombo = new SYMCLOVCombo(composite);
		approvalLineCombo.setLayoutData(new GridData(220, SWT.DEFAULT));
		for(String approvalLine : approvalLines){
			approvalLineCombo.add(approvalLine, null);
		}
		approvalLineCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchBySavedname();
			}
		});
		approvalLineCombo.setText(approvalLines[0]);
		
		Label label = new Label(composite, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		deleteButton = new Button(composite, SWT.PUSH);
		deleteButton.setText("Delete Approval Line");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteSavedname();
			}
		});
		
		Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 4;
        lSeparator.setLayoutData(gridData);
	}
	
	private void createTableLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout ());
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
		gridData.minimumHeight = 140;
//		composite.setLayoutData(gridData);
		
		resultTable = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		resultTable.setLayoutData(gridData);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				shell.close();
			}
		});
		
		int i = 0;
		for(String value : tableColumns){
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(tableColSizes[i]);
			i++;
		}
		searchBySavedname();
	}
	
	private void createButtonLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_END));
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		composite.setLayout(layout);
		
		selectButton = new Button(composite, SWT.PUSH);
		selectButton.setText("Select");
		selectButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				shell.close();
			}
		});
		
		cancelButton = new Button(composite, SWT.PUSH);
		cancelButton.setText("Close");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				resultSavedApprovalLines = null;
				shell.close();
			}
		});
	}

	/**
	 * 사용자 결재선 이름으로 리스트 가져오기
	 */
	private void searchBySavedname() {
		CustomECODao dao = new CustomECODao();
		
		try{
			ApprovalLineData map = new ApprovalLineData();
			map.setSaved_user(session.getUser().getUserId());
			map.setSaved_name(approvalLineCombo.getText());
			if(isECI){
				map.setEco_no(SYMCECConstant.ECI_PROCESS_TEMPLATE);
			}else{
				map.setEco_no(SYMCECConstant.ECO_PROCESS_TEMPLATE);
			}

			resultSavedApprovalLines = dao.loadApprovalLine(map);
			
			resultTable.removeAll();
			// FIXED 2013.05.14, DJKIM, 박수경 CJ: 사용자의 상태 변경이 발생 할수 있으므로 사용자 상태 확인하여 부적절한 사용자가 있는 경우 사용자 확인 메시지 발생.
			StringBuffer messageAll = new StringBuffer();
			TCComponentGroupMember groupMember = null;
			boolean isSkip = false;
			for (ApprovalLineData resultSavedApprovalLine : resultSavedApprovalLines) {
				StringBuffer message = new StringBuffer();
				try{
					if("References".equals(resultSavedApprovalLine.getTask())){
						isSkip = true;
					}else{
						isSkip = false;
						groupMember =  (TCComponentGroupMember) session.stringToComponent(resultSavedApprovalLine.getTc_member_puid());
					}
				}catch(Exception e){
					message.append("\nCannot find group member.\n"+resultSavedApprovalLine.getUser_name() + " in " + resultSavedApprovalLine.getTeam_name() + " is removed.");
				}
				
				if(groupMember.getMemberInactive() && !isSkip){
					message.append("\nGroup member is in inactive status.\n"+resultSavedApprovalLine.getUser_name() + " in " + resultSavedApprovalLine.getTeam_name() + " is in inactive status.");
				}
				if(!groupMember.getUser().isValid() && !isSkip){
					message.append("\nUser is in inactive status.\n"+resultSavedApprovalLine.getUser_name() + " in " + resultSavedApprovalLine.getTeam_name() + " is in inactive status.");
				}
				if(message.toString().equals("")){
					TableItem rowItem = new TableItem(resultTable, SWT.NONE);
					rowItem.setText(0, resultSavedApprovalLine.getTask());
					rowItem.setText(1, resultSavedApprovalLine.getTeam_name());
					rowItem.setText(2, resultSavedApprovalLine.getUser_name());
					rowItem.setText(3, resultSavedApprovalLine.getTc_member_puid());
					
//					if("MFG".equals(org_code)) {
//						rowItem.setData("saved_name", approvalLineCombo.getText());
//					}
				}else{
					messageAll.append(message.toString());
				}
			}
			if(!messageAll.toString().equals("")){
				messageAll.append("\n\nPlease rebuild this approval line.");
				MessageBox.post(shell, messageAll.toString(), "Information", MessageBox.INFORMATION);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	private void deleteSavedname(){
		CustomECODao dao = new CustomECODao();
		
		try{
			ApprovalLineData map = new ApprovalLineData();
			map.setSaved_user(session.getUser().getUserId());
			map.setSaved_name(approvalLineCombo.getText());
			if(isECI){
				map.setEco_no(SYMCECConstant.ECI_PROCESS_TEMPLATE);
			}else{
				map.setEco_no(SYMCECConstant.ECO_PROCESS_TEMPLATE);
			}
			
			dao.deleteApprovalLine(map);
			MessageBox.post("Saved Approval Line is deleted successfully.","Information", MessageBox.INFORMATION);
			shell.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
