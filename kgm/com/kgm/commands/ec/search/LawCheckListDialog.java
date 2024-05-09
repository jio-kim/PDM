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
 * ���� üũ �˾� ȭ��
 * @author DJKIM
 *
 */
public class LawCheckListDialog extends SYMCAbstractDialog{

	private Table resultTable;
	private String[] tableColumns = new String[]{"����", "����", "����", "�߱�", "��Ÿ"};
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
		SWTUtilities.skipKeyEvent(getShell()); //ESC Ű ����
		
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
		noteText.append("�� DR1,DR2 ��ǰ�� ��� �ش� ���������� ��� üũ\n");
		noteText.append("  �� DR �Է� ������ ���� �ڵ���������(����/����/����) ��������\n");
		noteText.append("    COP���� �������� �߻���, å���� ECI �ۼ��ڿ��� �ֽ��ϴ�.\n");
		noteText.append("  �� Y290 DR ��� ��ǰ ������ ���� �ۼ�\n");
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
		
		tableItem.setText(0, "��������");
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
		tableItem.setText(0, "��ǰ����");
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
		tableItem.setText(0, "�ش����");
		tableEditor = new TableEditor (resultTable);
		this.notApplicable = new Button(resultTable, SWT.CHECK);
		notApplicable.pack();
		tableEditor.minimumWidth = notApplicable.getSize ().x;
		tableEditor.horizontalAlignment = SWT.CENTER;
		tableEditor.setEditor(notApplicable, tableItem, 1);
		
		return composite;
	}
	
	/**
	 * ���� üũ �� ����
	 */
	private void initData() {
		lovMap.put("CD", "���� ��������");
		lovMap.put("CE", "���� ��������");
		lovMap.put("CC", "�߱� ��������");
		lovMap.put("CT", "��Ÿ ��������");
		lovMap.put("UD", "���� ��ǰ����");
		lovMap.put("UE", "���� ��ǰ����");
		lovMap.put("UC", "�߱� ��ǰ����");
		lovMap.put("UT", "��Ÿ ��ǰ����");
		lovMap.put("NA", "�ش���׾���");
		
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
