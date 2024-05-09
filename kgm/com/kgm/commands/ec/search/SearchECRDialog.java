package com.kgm.commands.ec.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

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
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.common.SYMCDateTimeButton;
import com.kgm.common.dialog.SYMCAbstractDialog;
/**
 * ECR 검색 화면
 * 참조 테이블 Vision-net 시스템의 CALS 참조
 * [DB-LINK로 연결 필수 임.]
 * @author DJKIM
 *
 */
public class SearchECRDialog extends SYMCAbstractDialog {
	/** ECR 시스템 NO */
	private String ecrSeqNo;
	/** ECR 등록 번호 */
	private String ecrRegNo;
	
	/** 검색 조건 */
	private Text docNo, title, cteam, cuser;
	@SuppressWarnings("unused")
    private Button chkIsCDateFrom, chkIsCDateTo;
	private SYMCDateTimeButton cdateFrom, cdateTo;
	private String txtCdateFrom, txtCdateTo, txtDocNo, txtTitle, txtCteam, txtCuser;
	
	/** 테이블 구성 요소 */
	private Table resultTable;

	private String[] tableColumns = new String[]{"ECR NO.", "TITLE", "Team", "Creator", "Creation Date"};
	private int[] tableColSizes = new int[]{100, 420, 150, 120, 200};
	private Text searchCount;
	private int lastSortColumn= -1;
	
	public SearchECRDialog(Shell paramShell, int _selection) {
        super(paramShell, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        setApplyButtonVisible(false);
	}
	
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		getShell().setText("Searching ECR");
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        
		createSearchLayout(composite);
		createTableLayout(composite);
		return composite;
	}

	@Override
	protected boolean validationCheck() {
		boolean isOK = true;
		return isOK;
	}

	@Override
	protected boolean apply() {
		boolean isOK = false;
		// 최대 입력 개수는 3개까지만 가능(32글자)
		 if(resultTable.getSelectionCount() < 1 || resultTable.getSelectionCount() > 5) {
			 MessageBox msgBox = new MessageBox(getShell(), SWT.ICON_ERROR);
			 msgBox.setMessage("최대 입력 개수는 5개까지만 가능합니다.");
			 msgBox.open();
			 return isOK;
		 }
		 
		 isOK = true;
		 TableItem[] selectItems = resultTable.getSelection();
		 
		 // [SR150116-017][2015.04.02][jclee] ECR 멀티 입력 기능 추가
		 for (int inx = 0; inx < selectItems.length; inx++) {
			 if (ecrSeqNo == null || ecrSeqNo.equals("")) {
				 ecrSeqNo = (String) selectItems[inx].getData("seqno");
			} else {
				ecrSeqNo = ecrSeqNo + "," + (String) selectItems[inx].getData("seqno");
			}
			 
			 if (ecrRegNo == null || ecrRegNo.equals("")) {
				 ecrRegNo = (String) selectItems[inx].getText(0);
			} else {
				ecrRegNo = ecrRegNo + "," + (String) selectItems[inx].getText(0);
			}
		 }

		return isOK;
	}
	
	private void createSearchLayout(Composite parentComposite) {
		Composite composite = new Composite(parentComposite, SWT.NONE);
		GridLayout layout = new GridLayout(10, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false)); 

		//#1
		makeLabel(composite, "ECR NO.", 80);
		docNo = new Text(composite, SWT.BORDER);
		docNo.setLayoutData(new GridData(100, SWT.DEFAULT));
		docNo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});

		makeLabel(composite, "TITLE", 80);
		title = new Text(composite, SWT.BORDER);
		title.setLayoutData(new GridData(100, SWT.DEFAULT));
		title.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});

		makeLabel(composite, "TEAM", 80);
		cteam = new Text(composite, SWT.BORDER);
		cteam.setLayoutData(new GridData(100, SWT.DEFAULT));
		cteam.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});

		makeLabel(composite, "Creator", 80);
		cuser = new Text(composite, SWT.BORDER);
		cteam.setLayoutData(new GridData(100, SWT.DEFAULT));
		cteam.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					searchAction();
				}
			}
		});

		Label label = new Label(composite, SWT.RIGHT);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		//검색 버튼
		Button searchButton = new Button(composite, SWT.PUSH);
		searchButton.setText("Search");
		searchButton.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				searchAction();
			}
		});
		
		//#2
		Date currentTime = new Date();
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
		int year = Integer.parseInt(simpleDateformat.format(currentTime));
		simpleDateformat = new SimpleDateFormat("MM");
		int month = Integer.parseInt(simpleDateformat.format(currentTime))-1;
		simpleDateformat = new SimpleDateFormat("dd");
		int day = Integer.parseInt(simpleDateformat.format(currentTime));
		
		makeLabel(composite, "Creation Date", 110);
		
		// [SR140819-044][jclee][20140911] 생성일 From 검색 조건 Toggle용 Check Box 추가
		Composite compositeForCDateFrom = new Composite(composite, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		compositeForCDateFrom.setLayout(layout);
		compositeForCDateFrom.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false)); 
		
		chkIsCDateFrom = new Button(compositeForCDateFrom, SWT.CHECK);
		GridData gridData = new GridData(15, SWT.DEFAULT);
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = false;
		gridData.horizontalAlignment = SWT.CENTER;
		chkIsCDateFrom.setLayoutData(gridData);
		chkIsCDateFrom.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				cdateFrom.setEnabled(chkIsCDateFrom.getSelection());
			}
		});
		
		cdateFrom = new SYMCDateTimeButton(compositeForCDateFrom);
		gridData = new GridData();
//		gridData.horizontalSpan = 2;
		cdateFrom.setLayoutData(gridData);
		cdateFrom.setDate(year-1, month, day);
		// [SR140819-044][jclee][20140911] 생성일 From 검색 조건 Toggle
		cdateFrom.setEnabled(false);
		
		Label label3 = new Label(composite, SWT.CENTER);
		label3.setText("~");
		gridData = new GridData(120, SWT.DEFAULT);
		label3.setLayoutData(gridData);
		
		cdateTo = new SYMCDateTimeButton(composite);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		cdateTo.setLayoutData(gridData);
		// [SR140819-044][jclee][20140911] 생성일 From 검색 조건 Toggle
		cdateTo.setDate(year, month, day);
		
		Label label4 = new Label(composite, SWT.RIGHT);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 4;
		label4.setLayoutData(gridData);
		
		Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData (SWT.FILL, SWT.FILL, true, false);
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
		
		// [SR150116-017][2015.04.02][jclee] ECR 멀티 입력 기능 추가
//		resultTable = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		resultTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
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
			final int columnIndex = i;
			column.addSelectionListener(new SelectionAdapter() {		
				public void widgetSelected(SelectionEvent e) {
					sort(columnIndex);
				}
			});
			i++;
		}
	}
	
	/**
	 * ECR 시스템 등록 번호
	 */
	public String getECRSeqNo(){
		return ecrSeqNo;
	}
	
	/**
	 * ECR 문서 번호
	 */
	public String getECRRegNo(){
		return ecrRegNo;
	}
	
	private void makeLabel(Composite paramComposite, String lblName, int lblSize){
		GridData layoutData = new GridData(lblSize, SWT.DEFAULT);
		
		Label label = new Label(paramComposite, SWT.RIGHT);
		label.setText(lblName);
		label.setLayoutData(layoutData);
	}
	
	private void searchAction() {
		txtDocNo = docNo.getText();
		txtTitle = title.getText();
		txtCteam = cteam.getText();
		txtCuser = cuser.getText();
		
		// [SR140819-044][jclee][20140911] 생성일 From 검색 조건 Toggle
		String tempCDateFrom = String.format("%1$02d", cdateFrom.getYear()) +String.format("%1$02d", cdateFrom.getMonth()) + String.format("%1$02d", cdateFrom.getDay());
		String tempCDateTo = String.format("%1$02d", cdateTo.getYear()) +String.format("%1$02d", cdateTo.getMonth()) + String.format("%1$02d", cdateTo.getDay());
		
		// 검색 조건 확인
		if(txtDocNo.equals("") && txtTitle.equals("") & txtCteam.equals("") & txtCuser.equals("") & tempCDateFrom.equals("") & tempCDateTo.equals("")){
			MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
			box.setText("Asking for proceed");
			box.setMessage("You have no search condition.\nThis can take a lot of time.\nDo you want to proceed?");
			if(box.open() != SWT.YES) {
				return;
			}
		}
		
		txtDocNo = txtDocNo.replace("*", "%");
		txtDocNo = txtDocNo.toUpperCase()+'%';
		
		txtTitle = txtTitle.replace("*", "%");
		txtTitle = txtTitle.toUpperCase()+'%';
		
		txtCteam = txtCteam.replace("*", "%");
		txtCteam = txtCteam.toUpperCase()+'%';
		
		txtCuser = txtCuser.replace("*", "%");
		txtCuser = txtCuser.toUpperCase()+'%';

		// [SR140819-044][jclee][20140911] 생성일 From 검색 조건 Toggle
//		txtCdateFrom = cdateFrom.getYear()+"-"+String.format("%1$02d", (cdateFrom.getMonth()+1))+"-"+String.format("%1$02d", cdateFrom.getDay())+"";
//		txtCdateTo = cdateTo.getYear()+"-"+String.format("%1$02d", (cdateTo.getMonth()+1))+"-"+String.format("%1$02d", cdateTo.getDay())+"";
		if (chkIsCDateFrom.getSelection()) {
			txtCdateFrom = cdateFrom.getYear()+"-"+String.format("%1$02d", (cdateFrom.getMonth()+1))+"-"+String.format("%1$02d", cdateFrom.getDay())+"";
		}
		
		txtCdateTo = cdateTo.getYear()+"-"+String.format("%1$02d", (cdateTo.getMonth()+1))+"-"+String.format("%1$02d", cdateTo.getDay())+"";
		
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				search();
			}
		});
	}
	
	private void search() {
		CustomECODao dao = new CustomECODao();
		ArrayList<HashMap<String, String>> searchResult = null;
		try {
			
			// [20160620][ymjang] DB Link 를 통한 ECI 및 ECR 정보 I/F를 EAI로 변경 개선
			searchResult = dao.searchECREAI(txtCdateFrom, txtCdateTo, txtDocNo, txtTitle, txtCteam, txtCuser);
			//searchResult = dao.searchECR(txtCdateFrom, txtCdateTo, txtDocNo, txtTitle, txtCteam, txtCuser);
			if(searchResult == null) {
				return;
			}
			resultTable.removeAll();
			
			searchCount.setText("Search Count : "+searchResult.size());
			
			for (HashMap<String, String> resultMap : searchResult) {
				TableItem rowItem = new TableItem(resultTable, SWT.NONE);
				rowItem.setText(0, resultMap.get("DOCNO"));
				rowItem.setText(1, resultMap.get("TITLE"));
				rowItem.setText(2, resultMap.get("CTEAM"));
				rowItem.setText(3, resultMap.get("CUSER"));
				rowItem.setText(4, resultMap.get("CDATE"));
				rowItem.setData("seqno", resultMap.get("SEQNO"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	private void sort(int column) {
		if(resultTable.getItemCount() <= 1) return;

		TableItem[] items = resultTable.getItems();
		String[][] data = new String[items.length][resultTable.getColumnCount()];
		for(int i = 0; i < items.length; i++) {
			for(int j = 0; j < resultTable.getColumnCount(); j++) {
				data[i][j] = items[i].getText(j);
			}
		}
		
		Arrays.sort(data, new RowComparator(column));
		
		if (lastSortColumn != column) {
			resultTable.setSortColumn(resultTable.getColumn(column));
			resultTable.setSortDirection(SWT.DOWN);
			for (int i = 0; i < data.length; i++) {
				items[i].setText(data[i]);
			}
			lastSortColumn = column;
		} else {
			// reverse order if the current column is selected again
			resultTable.setSortDirection(SWT.UP);
			int j = data.length -1;
			for (int i = 0; i < data.length; i++) {
				items[i].setText(data[j--]);
			}
			lastSortColumn = -1;
		}
		
	}
	
	/**
	 * To compare entries (rows) by the given column
	 */
	@SuppressWarnings("rawtypes")
	private class RowComparator implements Comparator {
		private int column;
		
		/**
		 * Constructs a RowComparator given the column index
		 * @param col The index (starting at zero) of the column
		 */
		public RowComparator(int col) {
			column = col;
		}
		
		/**
		 * Compares two rows (type String[]) using the specified
		 * column entry.
		 * @param obj1 First row to compare
		 * @param obj2 Second row to compare
		 * @return negative if obj1 less than obj2, positive if
		 * 			obj1 greater than obj2, and zero if equal.
		 */
		public int compare(Object obj1, Object obj2) {
			String[] row1 = (String[])obj1;
			String[] row2 = (String[])obj2;
			
			return row1[column].compareTo(row2[column]);
		}
	}
	
}
