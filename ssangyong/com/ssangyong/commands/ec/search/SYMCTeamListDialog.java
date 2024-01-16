package com.ssangyong.commands.ec.search;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.SWTUtilities;
import com.ssangyong.dto.VnetTeamInfoData;

/**
 * ECI 검토 부서 설정 팝업 화면
 * @author DJKIM
 *
 */
public class SYMCTeamListDialog extends SYMCAbstractDialog{

	private Table resultTable, selectedTable; // table object key = code
	private Text teamName;
	private Button searchButton;
	
	private String selectInfos;
	private String selectCodes;
	private static final String SEPERATOR = ",";
	
	public SYMCTeamListDialog(Shell paramShell, String selectCodes) {
		super(paramShell);
		this.selectCodes = selectCodes;
		this.setApplyButtonVisible(false);
	}
	
	private void addSelect(){
		if(resultTable.getSelectionCount() < 1) return;

		TableItem[] selectItems = resultTable.getSelection();
		for(TableItem selectItem : selectItems){
			String teamName = selectItem.getText();
			String teamCode = (String) selectItem.getData("code");
			boolean isNotExist = true;
			TableItem[] selectedItems = selectedTable.getItems();
			for(TableItem selectedItem : selectedItems){
				if(teamCode.equals((String) selectedItem.getData("code"))){
					isNotExist = false;
					break;
				}
			}
			if(isNotExist){
				TableItem rowItem = new TableItem(selectedTable, SWT.NONE);
				rowItem.setText(teamName);
				rowItem.setData("code", teamCode);
			}
		}
	}
	
	@Override
	protected boolean apply() {
		selectInfos = "";
		selectCodes = "";
		StringBuffer addInfo = new StringBuffer();
		StringBuffer addCode = new StringBuffer();
		
		for(TableItem item : selectedTable.getItems()){
			if(addInfo.length() > 0) {
				addInfo.append(SEPERATOR);
				addCode.append(SEPERATOR);
			}
			addInfo.append(item.getText());
			addCode.append(item.getData("code"));
		}
		
		selectInfos = addInfo.toString();
		selectCodes = addCode.toString();
		return true;
	}

	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		this.setDialogTextAndImage("Select Departments", null);
		SWTUtilities.skipKeyEvent(getShell()); //ESC 키 막음
		
		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout (3, false));
		
		createSearchTable(composite);
		createEditeButton(composite);
		createSelectedTable(composite);
		
//		searchTeam();
		if(selectCodes != null && !selectCodes.equals(""))
			setSelectedTeam();
		
		return composite;
	}
	
	private void createEditeButton(Composite paramComp){
		Composite composite = new Composite(paramComp, SWT.NONE);
		composite.setLayout(new GridLayout ());
		composite.setLayoutData(new GridData (SWT.CENTER, SWT.CENTER, false, true));
		
		Button addButton = new Button(composite, SWT.PUSH | SWT.CENTER);
		addButton.setText(">> Add");
		addButton.setLayoutData(new GridData (SWT.FILL, SWT.FILL, false, true));
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				addSelect();
			}
		});
		
		Button deleteButton = new Button(composite, SWT.PUSH);
		deleteButton.setText("<< Delete");
		deleteButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deleteSelect();
			}
		});
	}
	
	private void createSearchTable(Composite paramComp){
		Composite composite = new Composite(paramComp, SWT.NONE);
		composite.setLayout(new GridLayout (4, false));
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, false, true));
		
		Label lbl_id = new Label(composite, SWT.RIGHT);
		lbl_id.setText("Team Name : ");
		
		teamName = new Text(composite, SWT.BORDER);
		teamName.setLayoutData(new GridData (120, SWT.DEFAULT));
		teamName.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchTeam();
				}
			}
		});

		Label serchLabel = new Label(composite, SWT.RIGHT);
		GridData data = new GridData (SWT.FILL, SWT.CENTER, true, true);
		serchLabel.setLayoutData(data);
		
		searchButton = new Button(composite, SWT.PUSH);
		searchButton.setText("Search");
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchTeam();
			}
		});
		
		resultTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.HIDE_SELECTION);
		data = new GridData (SWT.FILL, SWT.FILL, true, true);
		data.horizontalSpan = 4;
		data.minimumHeight = 240;
		resultTable.setLayoutData(data);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				addSelect();
			}
		});
		
		TableColumn column = new TableColumn(resultTable, SWT.NONE);
		column.setText("Team");
		column.setWidth(300);
		
	}
	
	private void createSelectedTable(Composite paramComp){
		Composite composite = new Composite(paramComp, SWT.NONE);
		composite.setLayout(new GridLayout ());
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, false, true));
		
		Label lbl_id = new Label(composite, SWT.RIGHT);
		lbl_id.setText("Selected Team List");
		
		selectedTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.HIDE_SELECTION);
		GridData data = new GridData (SWT.FILL, SWT.FILL, true, true);
		data.minimumHeight = 220;
		selectedTable.setLayoutData(data);
		selectedTable.setHeaderVisible(true);
		selectedTable.setLinesVisible(true);
		selectedTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				deleteSelect();
			}
		});
		
		TableColumn column = new TableColumn(selectedTable, SWT.NONE);
		column.setText("Team");
		column.setWidth(200);
	}
	
	private void deleteSelect(){
		TableItem[] items = selectedTable.getSelection();
		if (items.length == 0) return;
		for(TableItem item : items){
			item.dispose();
		}
	}
	
	public String getSelectedTeams(){
		return selectInfos;
	}

	public String getSelectedTeamCodes() {
		return selectCodes;
	}
	
	private void searchTeam() {
		try{
			CustomECODao dao = new CustomECODao();
			VnetTeamInfoData data = new VnetTeamInfoData();
			String inputName = teamName.getText();
			if(inputName != null && !inputName.equals("")){
				inputName = inputName.replace("*", "%");
				data.setTeam_name(inputName.toUpperCase());
			}

			resultTable.removeAll();

			ArrayList<VnetTeamInfoData> teamInfoDataList = dao.getVnetTeamInfo(data);
			if(teamInfoDataList != null)
				for(VnetTeamInfoData teamInfoData : teamInfoDataList){
					TableItem rowItem = new TableItem(resultTable, SWT.NONE);
					rowItem.setText(teamInfoData.getTeam_name());
					rowItem.setData("code", teamInfoData.getTeam_code());
				}
		} catch(Exception e){
			e.printStackTrace();
		}
	}

	private void setSelectedTeam() {
		try{
			CustomECODao dao = new CustomECODao();
			VnetTeamInfoData data = new VnetTeamInfoData();
			data.setCodeList(selectCodes.split(SEPERATOR));

			ArrayList<VnetTeamInfoData> teamInfoDataList = dao.getVnetTeamNames(data);
			if(teamInfoDataList != null)
				for(VnetTeamInfoData teamInfoData : teamInfoDataList){
					TableItem rowItem = new TableItem(selectedTable, SWT.NONE);
					rowItem.setText(teamInfoData.getTeam_name());
					rowItem.setData("code", teamInfoData.getTeam_code());
				}
		} catch(Exception e){
			e.printStackTrace();
		}
	}
	
	@Override
	protected boolean validationCheck() {
		return true;
	}

}
