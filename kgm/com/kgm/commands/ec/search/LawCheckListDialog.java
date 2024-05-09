package com.kgm.commands.ec.search;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.SWTUtilities;

/**
 * 인증 체크 팝업 화면
 * @author DJKIM
 *
 */
public class LawCheckListDialog extends SYMCAbstractDialog{

	private Table resultTable;
	private String[] tableColumns = new String[]{"구분", "국내", "유럽", "중국", "기타"};
	private int[] tableColSizes = new int[]{100, 100, 100, 100, 100};
	private Button carDomestic, carEurope, carChina, carEtc, unitDomestic, unitEurope, unitChina, unitEtc, notApplicable;
	
	private HashMap<String,String> lovMap = new HashMap<String,String>();
	private HashMap<String,Button> lawCheckMap = new HashMap<String,Button>();
	private String selectInfo;
	private static final String SEPERATOR = ",";
	
	public LawCheckListDialog(Shell paramShell, String selectInfo) {
		super(paramShell);
		this.selectInfo = selectInfo;
		this.setApplyButtonVisible(false);
	}

	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		this.setDialogTextAndImage("Law Check", null);
		SWTUtilities.skipKeyEvent(getShell()); //ESC 키 막음
		
		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout ());
		
		createLawListTable(composite);
		
		initData();
		
		return composite;
	}
	
	private Composite createLawListTable(Composite paramComp){
		Composite composite = new Composite(paramComp, SWT.NONE);
		composite.setLayout(new GridLayout());
		
		StringBuffer noteText = new StringBuffer();
		noteText.append("※ DR1,DR2 부품인 경우 해당 인증지역에 모두 체크\n");
		noteText.append("  ▶ DR 입력 오류에 의한 자동검토지정(인증/생산/물류) 누락으로\n");
		noteText.append("    COP감사 지적사항 발생시, 책임은 ECI 작성자에게 있습니다.\n");
		noteText.append("  ▶ Y290 DR 대상 부품 설변시 영문 작성\n");
		StyledText note = new StyledText (composite, SWT.MULTI | SWT.BORDER);
		note.setText (noteText.toString());
		GridData layoutData = new GridData (SWT.FILL, SWT.FILL, true, false);
		note.setLayoutData(layoutData);
		
		layoutData = new GridData (SWT.FILL, SWT.FILL, true, true);
//		layoutData.minimumHeight = 140;
		
		resultTable = new Table(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
		resultTable.setLayoutData(layoutData);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				apply();
			}
		});
		
		int i = 0;
		for(String value : tableColumns){
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(tableColSizes[i]);
			i++;
		}
		
		TableItem tableItem = new TableItem (resultTable,SWT.NONE);
		
		tableItem.setText(0, "차량인증");
		TableEditor tableEditor = new TableEditor (resultTable);
		carDomestic = new Button(resultTable, SWT.CHECK);
		carDomestic.pack();
		tableEditor.minimumWidth = carDomestic.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(carDomestic, tableItem, 1);
		
		tableEditor = new TableEditor (resultTable);
		this.carEurope = new Button(resultTable, SWT.CHECK);
		carEurope.pack();
		tableEditor.minimumWidth = carEurope.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(carEurope, tableItem, 2);
		
		tableEditor = new TableEditor (resultTable);
		this.carChina = new Button(resultTable, SWT.CHECK);
		carChina.pack();
		tableEditor.minimumWidth = carChina.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(carChina, tableItem, 3);
		
		tableEditor = new TableEditor (resultTable);
		this.carEtc = new Button(resultTable, SWT.CHECK);
		carEtc.pack();
		tableEditor.minimumWidth = carEtc.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(carEtc, tableItem, 4);
		
		tableItem = new TableItem (resultTable,SWT.NONE);
		tableItem.setText(0, "단품인증");
		tableEditor = new TableEditor (resultTable);
		this.unitDomestic = new Button(resultTable, SWT.CHECK);
		unitDomestic.pack();
		tableEditor.minimumWidth = unitDomestic.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(unitDomestic, tableItem, 1);
		
		tableEditor = new TableEditor (resultTable);
		this.unitEurope = new Button(resultTable, SWT.CHECK);
		unitEurope.pack();
		tableEditor.minimumWidth = unitEurope.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(unitEurope, tableItem, 2);
		
		tableEditor = new TableEditor (resultTable);
		this.unitChina = new Button(resultTable, SWT.CHECK);
		unitChina.pack();
		tableEditor.minimumWidth = unitChina.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(unitChina, tableItem, 3);
		
		tableEditor = new TableEditor (resultTable);
		this.unitEtc = new Button(resultTable, SWT.CHECK);
		unitEtc.pack();
		tableEditor.minimumWidth = unitEtc.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(unitEtc, tableItem, 4);
		
		
		tableItem = new TableItem (resultTable,SWT.NONE);
		tableItem.setText(0, "해당없음");
		tableEditor = new TableEditor (resultTable);
		this.notApplicable = new Button(resultTable, SWT.CHECK);
		notApplicable.pack();
		tableEditor.minimumWidth = notApplicable.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(notApplicable, tableItem, 1);
		
		return composite;
	}
	
	/**
	 * 인증 체크 맵 생성
	 */
	private void initData() {
		lovMap.put("CD", "국내 차량인증");
		lovMap.put("CE", "유럽 차량인증");
		lovMap.put("CC", "중국 차량인증");
		lovMap.put("CT", "기타 차량인증");
		lovMap.put("UD", "국내 단품인증");
		lovMap.put("UE", "유럽 단품인증");
		lovMap.put("UC", "중국 단품인증");
		lovMap.put("UT", "기타 단품인증");
		lovMap.put("NA", "해당사항없음");
		
		lawCheckMap.put("CD", carDomestic);
		lawCheckMap.put("CE", carEurope);
		lawCheckMap.put("CC", carChina);
		lawCheckMap.put("CT", carEtc);
		lawCheckMap.put("UD", unitDomestic);
		lawCheckMap.put("UE", unitEurope);
		lawCheckMap.put("UC", unitChina);
		lawCheckMap.put("UT", unitEtc);
		lawCheckMap.put("NA", notApplicable);
		
		String[] lawChecks = selectInfo.split(SEPERATOR);
		for(String lawCheck : lawChecks){
			for(Object key : lovMap.keySet().toArray()){
				if(lawCheck.equals(lovMap.get(key))){
					lawCheckMap.get(key).setSelection(true);
					break;
				}
			}
		}
	}
	
	@Override
	protected boolean apply() {
		selectInfo = "";
		StringBuffer addInfo = new StringBuffer();
		for(Object key : lawCheckMap.keySet().toArray()){
			if(lawCheckMap.get(key).getSelection()){
				if(addInfo.length() > 0) addInfo.append(SEPERATOR);
				addInfo.append(lovMap.get(key));
			}
		}
		selectInfo = addInfo.toString(); 
		return true;
	}
	
	public String getLawChecks(){
		System.out.println("getLawChecks()");
		return selectInfo;
	}

	@Override
	protected boolean validationCheck() {
		return true;
	}
}
