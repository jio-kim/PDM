package com.kgm.commands.ec.search;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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
import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * ������ ECI �˻� ȭ��
 * Select ��ư Ȥ�� ���̺� ����Ŭ���ϸ�
 * ���� ������ ��ȯ��.
 * @author DJKIM
 * [SR140701-022][20140902] jclee, ECI�� Vision Net�� �����ͷ� ���������� ����.
 * [20160824][ymjang][SR160823-009] ECO_ECI ����� ����
 *
 */
public class SearchECIDialog extends SYMCAbstractDialog {
	
	/** �˻� ���� */
	private Text eciIDText, creatorText;
	
	private Button searchButton;
	private Table resultTable;
	
	/** ���̺� ��� */
	private String[] tableColumns = SYMCECConstant.ECI_CONCURRENTECO_TABLE_COLS;
	private int[] tableColSizes = new int[]{100, 220, 150, 120, 200};
	
	/** �˻� ���� */
	private String[] searchConditions = new String[]{"item_id", "user_name"};
	private String[] searchValues = new String[]{"", ""};
	
//	private TCComponentItemRevision selectedEciRevision;
//	public TCComponentItemRevision getSelectedEciRevision(){
//		return selectedEciRevision;
//	}
	
	private String selectedECINo;
	public String getSelectedECINo(){
		return selectedECINo;
	}
	
	private GridData gridData = new GridData (SWT.FILL, SWT.FILL, true, true);
	

	public SearchECIDialog(Shell paramShell) {
        super(paramShell, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        setApplyButtonVisible(false);
	}
	
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		getShell().setText("Search ECI");
		
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        
		createSearchLayout(composite);
		// [20160824][ymjang][SR160823-009] ECO_ECI ����� ����
		createInformMessage(composite);
		createTableLayout(composite);
		
		return composite;
	}
	
	private void createSearchLayout(Composite parentComposite) {
		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayout(new GridLayout (6, false));
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label lbl_id = new Label(composite, SWT.RIGHT);
		lbl_id.setText("ECI Approval NO. : ");
		
		eciIDText = new Text(composite, SWT.BORDER);
		eciIDText.setLayoutData(new GridData(220, SWT.DEFAULT));
		eciIDText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});
		
		Label idLabel = new Label(composite, SWT.RIGHT);
		idLabel.setText("Creator : ");
		
		creatorText = new Text(composite, SWT.BORDER);
		creatorText.setLayoutData(new GridData(220, SWT.DEFAULT));
		creatorText.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});

		Label serchLabel = new Label(composite, SWT.RIGHT);
		GridData data = new GridData (SWT.FILL, SWT.CENTER, true, true);
		serchLabel.setLayoutData(data);
		
		searchButton = new Button(composite, SWT.PUSH);
		searchButton.setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
		searchButton.setText("Search");
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				searchAction();
			}
		});
	}
	
	/**
	 * [20160824][ymjang][SR160823-009] ECO_ECI ����� ����
	 * @param parentComposite
	 */
	private void createInformMessage(Composite parentComposite) {
		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayout(new GridLayout ());
		composite.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label lbl_msg = new Label(composite, SWT.RIGHT);
		Font boldFont = new Font( lbl_msg.getDisplay(), new FontData( "Arial", 10, SWT.BOLD ) );
		lbl_msg.setFont( boldFont );
		lbl_msg.setForeground(lbl_msg.getDisplay().getSystemColor(SWT.COLOR_RED));
		lbl_msg.setText("���� ���ε� ECI ���� ��ȸ�� �����մϴ�. ���� ���ε� ECI �� ��ȸ�� �Ұ��Ͽ��� ����������� �����Ͻñ� �ٶ��ϴ�.");
		
	}
	
	private void createTableLayout(Composite parentComposite) {
		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayout(new GridLayout ());
		GridData data = new GridData (SWT.FILL, SWT.FILL, true, true);
		data.minimumHeight = 140;
		composite.setLayoutData(data);
		
		resultTable = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		resultTable.setLayoutData(gridData);
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

	private void searchAction() {
		
		String itemID = eciIDText.getText();
		String creatorName = creatorText.getText();
		if(itemID.equals("")){
			itemID = "*";
		}
		if(creatorName.equals("")){
			creatorName = "*";
		}
		
		// ���� �˻� ����
		if(searchValues[0] == null || !searchValues[0].equals(itemID) || !searchValues[1].equals(creatorName)){
			searchValues[0] = itemID;
			searchValues[1] = creatorName;

			// �˻� ���� Ȯ��
			if(itemID.equals("*") & creatorName.equals("*")){
				MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				box.setText("Asking for proceed");
				box.setMessage("No search condition.\nDo you want to search all eci?");
				if(box.open() != SWT.YES) {
					return;
				}
				eciIDText.setText("*");
				creatorText.setText("*");
			}

			getShell().getDisplay().syncExec(new Runnable() {
				public void run() {
//					search();
					searchFromVNet();
				}
			});
		}
	}
	
	@SuppressWarnings("unused")
    private void search() {
		try {
			TCComponent[] results= CustomUtil.queryComponent("__SYMC_S7_ECI_Revision", searchConditions, searchValues);
			if(results == null) return;
			resultTable.removeAll();
			for (int i = 0; i < results.length; i++) {
				TCComponentItemRevision eciRevision = (TCComponentItemRevision) results[i];
				String[] properties = eciRevision.getProperties(SYMCECConstant.ECI_POPUP_PROPERTIES);
				TableItem rowItem = new TableItem(resultTable, SWT.NONE);
				rowItem.setText(properties);
				rowItem.setData(eciRevision);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Vision NET���� ECI ���� ��ȸ
	 */
	private void searchFromVNet() {
		try {
			CustomECODao dao = new CustomECODao();
			DataSet ds = new DataSet();
			ds.put("ECINO", eciIDText.getText().toUpperCase().replace('*', '%'));
			ds.put("CUSER", creatorText.getText().toUpperCase().replace('*', '%'));
			// [20160620][ymjang] DB Link �� ���� ECI �� ECR ���� I/F�� EAI�� ���� ���� 
			ArrayList<HashMap<String, String>> results = dao.searchECIEAI(ds);
			//ArrayList<HashMap<String, String>> results = dao.searchECI(ds);
			if (results == null) {
				return;
			}
			
			resultTable.removeAll();
			
			for (int inx = 0; inx < results.size(); inx++) {
				String[] sProps = new String[5];
				HashMap<String, String> result = results.get(inx);
				sProps[0] = result.get("ECINO");
				sProps[1] = result.get("TITLE");
				sProps[2] = result.get("CUSER");
				sProps[3] = result.get("CTEAM");
				sProps[4] = result.get("CDATE");
				
				TableItem rowItem = new TableItem(resultTable, SWT.NONE);
				
				rowItem.setText(sProps);
				rowItem.setData("String[]", sProps);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	protected void afterCreateContents() {
		eciIDText.setFocus();
	}

	@Override
	protected boolean validationCheck() {
		return true;
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
		 selectedECINo = selectItems[0].getText();

		return isOK;
	}
}
