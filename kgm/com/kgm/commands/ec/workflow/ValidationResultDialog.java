package com.kgm.commands.ec.workflow;

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
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kgm.common.dialog.SYMCAbstractDialog;

/**
 * 
 *[20161221]CM ECO일 경우, CM Part 여부 컬럼을 추가하여 표시하도록 함
 *[20161227]1.CM ECO가 아닐 경우, CM Part 가 OLD/NEW로 들어오면 표시함
 *[20161227]2. CM ECO가 아닐 경우, Supply Mode 가 'S'로 시작되면 표시함 
 */
public class ValidationResultDialog extends SYMCAbstractDialog {

	private String ecoNo;
	private ArrayList<HashMap<String, String>> tableList;
    private Table resultTable;
    // [SR160325-035][20160329][jclee] SEQ No 누락
//    private String[] columnName = new String[] { "Parent Part No", "Child Part No", "Find No", "Qty", "Supply Mode", "ECO No.", "Old IC", "New IC", "PLT Stk", "A/S Stk" ,"Change Desc."};
//    private int[] columnSize = new int[] { 100, 100, 60, 40, 120, 100, 130, 130, 130, 130, 130 };
//    private String[] columnName = new String[] { "Parent Part No", "Child Part No", "Find No", "Qty", "Seq No", "Supply Mode", "ECO No.", "Old IC", "New IC", "PLT Stk", "A/S Stk" ,"Change Desc."};
//    private int[] columnSize = new int[] { 100, 100, 60, 40, 120, 120, 100, 130, 130, 130, 130, 130 };
    private String[] columnName = new String[] { "Parent Part No", "Child Part No", "Find No", "Qty", "Seq No", "Supply Mode", "ECO No.", "Old IC", "New IC", "PLT Stk", "A/S Stk" ,"Change Desc.","IS CM Part"};
    private int[] columnSize = new int[] { 100, 100, 60, 40, 90, 112, 100, 90, 90, 130, 130, 130 , 103};

    private Button closeButton;
    
    public ValidationResultDialog(Shell parent, int _selection, String ecoNo, ArrayList<HashMap<String, String>> tableList) {
        super(parent, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM | _selection);
        this.ecoNo = ecoNo;
        this.tableList = tableList;
    }

    @Override
    protected boolean apply() {
        return false;
    }

    /** 버튼 변경 */
    protected void createButtonsForButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        closeButton = new Button(composite, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getShell().close();
            }
        });
    }

    /** Composiste 생성 */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        getShell().setText("ECO EPL Validation Result");
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout());

        createSearchComposite(composite);
        cteateSearchResultTable(composite);
        return composite;
    }

    /** TOP Composiste 생성 */
    private void createSearchComposite(Composite paramComposite) {
        Composite composite = new Composite(paramComposite, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(gridData);

        makeLabel(composite, "ECO NO. : " + ecoNo, 200);
        makeLabel(composite, "COUNT : " + tableList.size(), 200);

        Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 10;
        lSeparator.setLayoutData(gridData);

    }

    /** 검색 결과 테이블 생성 */
    private void cteateSearchResultTable(Composite paramComposite) {
        Composite composite = new Composite(paramComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(layoutData);

        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        layoutData.minimumHeight = 200;
        layoutData.horizontalSpan = 3;
        resultTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        resultTable.setHeaderVisible(true);
        resultTable.setLinesVisible(true);
        resultTable.setLayoutData(layoutData);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				// TODO 테이블 더블 클릭 시
			}
		});
		
		//FIXME: [20161221]CM ECO일 경우, CM Part 여부 체크
		if(ecoNo.startsWith("CM"))
		{
		    columnName = new String[] { "Parent Part No", "Child Part No", "Find No", "Qty", "Seq No", "Supply Mode", "ECO No.", "Old IC", "New IC", "PLT Stk", "A/S Stk" ,"Change Desc.","타차종부품 여부"};
		}

        int i = 0;
        for (String value : columnName) {
        	TableColumn column = new TableColumn(resultTable, SWT.NONE);
        	column.setText(value);
        	column.setWidth(columnSize[i]);
        	if(value.equals("Find No") || value.equals("Qty"))
        		column.setAlignment(SWT.RIGHT);
            i++;
        }

        for (HashMap<String, String> map : tableList) {
            TableItem item = new TableItem(resultTable, SWT.NONE);
            if (map.get("PARENT_NO") != null)
                item.setText(0, map.get("PARENT_NO")+"");
            if (map.get("NEW_PART_NO") != null)
                item.setText(1, map.get("NEW_PART_NO")+"");
            if (map.get("NEW_SEQ") != null)
                item.setText(2, map.get("NEW_SEQ")+"");
            if (map.get("NEW_QTY") != null)
                item.setText(3, map.get("NEW_QTY")+"");
            if (map.get("SEQ_NO") != null)	// [SR160325-035][20160329][jclee] SEQ No 누락
            	item.setText(4, map.get("SEQ_NO")+"");
            if (map.get("NEW_SMODE") != null)
                item.setText(5, map.get("NEW_SMODE")+"");
            if (map.get("REF_ECO") != null)
                item.setText(6, map.get("REF_ECO")+"");
            if (map.get("OLD_IC") != null)
                item.setText(7, map.get("OLD_IC")+"");
            if (map.get("NEW_IC") != null)
                item.setText(8, map.get("NEW_IC")+"");
            if (map.get("PLT_ST") != null)
                item.setText(9, map.get("PLT_ST")+"");
            if (map.get("AS_ST") != null)
                item.setText(10, map.get("AS_ST")+"");
            if (map.get("CHANGE_DESC") != null)
                item.setText(11, map.get("CHANGE_DESC")+"");
            //[20161221]CM ECO일 경우, CM Part 여부 체크
    		if(ecoNo.startsWith("CM"))
    		{
	            if (map.get("CM_PART_CHECK") != null)
	                item.setText(12, map.get("CM_PART_CHECK")+"");
    		}else
    		{
    			//[20161227] CM ECO가 아닐 경우 CM Part가 존재하면 'CM ECO 부품' 으로 표시함
	            if (map.get("CM_PART_CHECK_IN_NOT_CM") != null)
	                item.setText(12, map.get("CM_PART_CHECK_IN_NOT_CM")+"");
	            
	            if (map.get("NEW_SMODE") == null)
	            {
	            	//[20161227] CM ECO가 아닐 경우, Supply Mode 가 'S'로 시작되면 표시함 
		            if (map.get("INVALID_SMOD_IN_NOT_CM") != null)
		                item.setText(5, map.get("INVALID_SMOD_IN_NOT_CM")+"");
	            }
	            
    		}
            if (map.get("OCC_THREADS") != null)
                item.setData("OCC_PUIDS", map.get("OCC_THREADS")+"");
        }
    }

    private void makeLabel(Composite paramComposite, String lblName, int lblSize) {
        GridData layoutData = new GridData(lblSize, SWT.DEFAULT);

        Label label = new Label(paramComposite, SWT.LEFT);
        label.setText(lblName);
        label.setLayoutData(layoutData);
    }

    @Override
    protected boolean validationCheck() {
        return true;
    }
   
}