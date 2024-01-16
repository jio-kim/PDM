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
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.utils.SYMDisplayUtil;
import com.ssangyong.dto.ApprovalLineData;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
/**
 * ECI 결재선 저장 화면
 */
public class AskMyECIAssignDialog {
	
	private Shell shell;
	private TCSession session;
	private String workflows;
	
	private Text lineNameText;
	private String lineName;
	
	private Button saveButton, cancelButton;

	public AskMyECIAssignDialog(Shell parent, String workflows, TCSession session) {
		shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.PRIMARY_MODAL | SWT.RESIZE);
		shell.setLayout(new GridLayout());
		shell.setText("Saved Assign Info");
		shell.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, true));
		SYMDisplayUtil.centerToScreen(shell);
		this.workflows = workflows;
		this.session = session;
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

	private void createSearchLayout() {
		Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new GridLayout (2, false));
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label groupLabel = new Label(composite, SWT.RIGHT);
		groupLabel.setText("My approval line name : ");
		
		lineNameText = new Text(composite, SWT.BORDER);
		lineNameText.setLayoutData(new GridData(220, SWT.DEFAULT));
		
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

	private void save() {
		lineName = lineNameText.getText();

		if(lineName.equals(""))
			MessageBox.post(shell, "Input the Name!", "ERROR", MessageBox.ERROR);

		saveUserApprovalLine();
	}

	/** 결재선 저장 */
	private void saveUserApprovalLine() {
		ArrayList<ApprovalLineData> paramList = new ArrayList<ApprovalLineData>();
		CustomECODao dao = new CustomECODao();
		
		try {
			ApprovalLineData map = new ApprovalLineData();
			map.setSaved_name(lineName);
			map.setSaved_user(session.getUser().getUserId());
			map.setEco_no(SYMCECConstant.ECI_PROCESS_TEMPLATE);

			ArrayList<ApprovalLineData> resultSavedApprovalLines = dao.loadApprovalLine(map);

			if(resultSavedApprovalLines != null && resultSavedApprovalLines.size() > 0){
				MessageBox.post(shell, "The name already exist!", "ERROR", MessageBox.ERROR);
				return;
			}
			
			String[] steps = workflows.split(SYMCECConstant.SEPERATOR);
			int i = 0;
			for(String step : steps){
				String[] stepInfo = step.split(":");
				ApprovalLineData theLine = new ApprovalLineData();
				theLine.setEco_no(SYMCECConstant.ECI_PROCESS_TEMPLATE);
				theLine.setSaved_name(lineName);
				theLine.setSaved_user(session.getUser().getUserId());
				theLine.setSort(i+"");
				theLine.setTask(stepInfo[0]);
				theLine.setTeam_name("");
				theLine.setUser_name(stepInfo[1]);
				theLine.setTc_member_puid(stepInfo[2]);

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
