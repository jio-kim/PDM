package com.kgm.commands.ec.search;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.SortListenerFactory;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroupMember;

/**
 * [20170125] 기술관리 Task 일 경우에는 ECO_COMPLETE Role 만 검색되도록 함
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SearchUserDialog extends SYMCAbstractDialog {
	
	/** 검색 조건 */
	private Text cteam, cuser;
	private String txtCteam, txtCuser;
	
	/** 테이블 구성 요소 */
	private Table resultTable;

	private String[] tableColumns = new String[]{"Dept.", "User Name", "Role", "User ID"};
	private int[] tableColSizes = new int[]{220, 150, 150, 100};
	private Text searchCount;
	private String org_code="";
	private String task_name="";
	
	
	private TCComponentGroupMember selectedMember;
    private HashMap selectedReferencesMember;
	public TCComponentGroupMember getSelectedMember(){
		return selectedMember;
	}
	
	public HashMap getSelectedReferencesMember() {
		return selectedReferencesMember;
	}
	public SearchUserDialog(Shell paramShell) {
        super(paramShell, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        setApplyButtonVisible(false);
	}
	
	public SearchUserDialog(Shell paramShell , String org_code, String task_name) {
        super(paramShell, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        this.org_code = org_code; 
        this.task_name = task_name;
        setApplyButtonVisible(false);
	}
	
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		getShell().setText("Search Member");
		
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        
		createSearchLayout(composite);
		createTableLayout(composite);
		
		return composite;
	}
	
	private void createSearchLayout(Composite parentComposite) {
		Composite composite = new Composite(parentComposite, SWT.NONE);
		GridLayout layout = new GridLayout(6, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false)); 

		makeLabel(composite, "Team Name", 80);
		cteam = new Text(composite, SWT.BORDER);
		cteam.setLayoutData(new GridData(100, SWT.DEFAULT));

		makeLabel(composite, "User Name", 80);
		cuser = new Text(composite, SWT.BORDER);
		cuser.setLayoutData(new GridData(100, SWT.DEFAULT));

		Label label = new Label(composite, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		//검색 버튼
		Button searchButton = new Button(composite, SWT.PUSH);
		searchButton.setText("Search");
		searchButton.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		searchButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				searchAction();
			}
		});
		
		Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 10;
        lSeparator.setLayoutData(gridData);
	}
	
	private void createTableLayout(Composite parentComposite) {
		Composite composite = new Composite(parentComposite, SWT.NONE);
		GridLayout layout = new GridLayout ();
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData data = new GridData (SWT.FILL, SWT.FILL, true, true);
		data.minimumHeight = 300;
		composite.setLayoutData(data);
		
		searchCount = new Text(composite, SWT.LEFT);
		searchCount.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		searchCount.setText("Search Count : ");
		
		resultTable = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		resultTable.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, true));
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				apply();
				close();
			}
		});
		
		int i = 0;
		for(String value : tableColumns){
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(tableColSizes[i]);
			column.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
			i++;
		}
	}
	
	private void makeLabel(Composite paramComposite, String lblName, int lblSize){
		GridData layoutData = new GridData(lblSize, SWT.DEFAULT);
		
		Label label = new Label(paramComposite, SWT.RIGHT);
		label.setText(lblName);
		label.setLayoutData(layoutData);
	}
	
	private void searchAction() {
		String curTeam = cteam.getText();
		String curUser = cuser.getText();
		if(curTeam.equals("")){
			curTeam = "*";
		}
		if(curUser.equals("")){
			curUser = "*";
		}
		
		// 동일 검색 제외
		if(txtCteam == null || !txtCteam.equals(curTeam) || !txtCuser.equals(curUser)){
			txtCteam = curTeam;
			txtCuser = curUser;

			// 검색 조건 확인
			if(txtCteam.equals("*") & txtCuser.equals("*")){
				MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				box.setText("Asking for proceed");
				box.setMessage("No search condition.\nDo you want to search all members?");
				if(box.open() != SWT.YES) {
					return;
				}
				cteam.setText("*");
				cuser.setText("*");
			}

			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
					search();
				}
			});

			setOKButtonVisible(true);
		}
	}
	
	private void search() {
		TCComponent[] resultList = null;
		ArrayList<HashMap<String, String>> referenceList= null;
		CustomECODao ecoDao = null;
		try {


			
			if("References".equals(task_name) && "MFG".equals(org_code)) {
				
				ecoDao = new CustomECODao();
				referenceList = ecoDao.searchUserOnVnet(txtCteam, txtCuser);
				
				if(referenceList == null) {
					return;
				}
				
				resultTable.removeAll();
				
				searchCount.setText("Search Count : "+referenceList.size());
				
				for(HashMap userMap : referenceList) {
					//"group", "the_user", "role", "user_name", "fnd0objectId"
					String[] properties = {(String)userMap.get("TEAM"), (String)userMap.get("THE_USER"), (String)userMap.get("ROLE"), (String)userMap.get("USER_NAME"), (String)userMap.get("fnd0objectId")  };
					TableItem rowItem = new TableItem(resultTable, SWT.NONE);
					rowItem.setText(properties);
					rowItem.setData(properties);
				}
				
			}else{
				
				resultList = CustomUtil.queryComponent("__SYMC_group_members", new String[] { "Group", "user_name" }, new String[] {
						txtCteam, txtCuser });
				if(resultList == null) {
					return;
				}
				
				resultTable.removeAll();
				
				boolean isTMTask = "Technical Management".equalsIgnoreCase(task_name); // Task 가 기술관리인지 여부
				int searchCnt = 0;

				for(TCComponent result : resultList) {
					TCComponentGroupMember member = (TCComponentGroupMember) result;
					String[] properties = member.getProperties(SYMCECConstant.ECO_MEMBER_PROPERTIES);
					
					//[20170125] 기술관리 Task 일 경우에는 ECO_COMPLETE Role 만 나타나도록 함
					if(isTMTask && !"ECO_COMPLETE".equalsIgnoreCase(properties[2]))
						continue;					
					
					if(org_code != null && org_code.equals("MFG")) {
						
						if(properties[0].endsWith("MFG")){
							properties[0] = properties[0].replace(".MFG", "");
							TableItem rowItem = new TableItem(resultTable, SWT.NONE);
							rowItem.setText(properties);   //{"group", "the_user", "role", "user_name", "fnd0objectId"};
							rowItem.setData(member);
						}
					}else {
						if(properties[0].endsWith("SYMC")){
							properties[0] = properties[0].replace(".SYMC", "");
							TableItem rowItem = new TableItem(resultTable, SWT.NONE);
							rowItem.setText(properties);
							rowItem.setData(member);
							searchCnt++;
						}
						
					}
				}

				searchCount.setText("Search Count : "+(!isTMTask?resultList.length:searchCnt));

			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	protected void afterCreateContents() {
		cteam.setFocus();
	}

	@Override
	protected boolean validationCheck() {
		boolean isOK = true;
		return isOK;
	}

	@Override
	protected boolean apply() {
		boolean isOK = false;
		 
		 if(resultTable.getSelectionCount() < 1){
			 searchAction();
			 return isOK;
		 }
		 
		 isOK = true;
		 
		 TableItem[] selectItems = resultTable.getSelection();
		 if("References".equals(task_name)) {
			 String[] tmpArray = (String[]) selectItems[0].getData();
//			 selectedReferencesMember.put("TEAM", value)
//			 selectedReferencesMember.put("USER_NAME", value)
//			 selectedReferencesMember.put("THE_USER, value)
//			 selectedReferencesMember =selectItems[0].getData(); //[PLM TFT, 박수경, Reference, 036001, null]
			 selectedReferencesMember = new HashMap<String, String>();
			 int cnt = tmpArray.length;
			 for(int i=0; i < cnt; i++) {
				 
				 if(i ==0) {
					 
					 selectedReferencesMember.put("TEAM", tmpArray[i]);
				 }else if(i==1) {
					 selectedReferencesMember.put("USER_NAME", tmpArray[i]);
				 }else if(i==3) {
					 selectedReferencesMember.put("THE_USER", tmpArray[i]);
				 }
			 }
		 }else{
			 
			 selectedMember = (TCComponentGroupMember) selectItems[0].getData();
		 }
		return isOK;
	}
}
