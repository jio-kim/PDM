/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly.temp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

/**
 * Class Name : SearchMecoView
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class SearchMecoViewTemp extends Composite {
    private Text txtMeco;
    private Text text;
    private Table table;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SearchMecoViewTemp(Composite parent, int style)  {
        super(parent, style);
        initUI();
    }


    protected void initUI() {
        FillLayout fillLayout = new FillLayout(SWT.VERTICAL);
        fillLayout.marginWidth = 5;
        fillLayout.marginHeight = 5;
        setLayout(fillLayout);

        Composite compositCondition = new Composite(this, SWT.NONE);
        GridLayout gl_compositCondition = new GridLayout(3, false);
        compositCondition.setLayout(gl_compositCondition);

        Label lblMeco = new Label(compositCondition, SWT.NONE);
        GridData gd_lblMeco = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblMeco.widthHint = 105;
        lblMeco.setLayoutData(gd_lblMeco);
        lblMeco.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 10, SWT.NORMAL));
        lblMeco.setText("MECO ¹øÈ£");

        txtMeco = new Text(compositCondition, SWT.BORDER);
        GridData gd_txtMeco = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtMeco.widthHint = 160;
        txtMeco.setLayoutData(gd_txtMeco);

        Button btnSearch = new Button(compositCondition, SWT.NONE);
        GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnSearch.widthHint = 60;
        gd_btnSearch.minimumWidth = 100;
        btnSearch.setLayoutData(gd_btnSearch);
        btnSearch.setText("°Ë»ö");
        
        Label lblMecoName = new Label(compositCondition, SWT.NONE);
        lblMecoName.setText("MECO ¸í");
        lblMecoName.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 10, SWT.NORMAL));
        
        text = new Text(compositCondition, SWT.BORDER);
        GridData gd_text = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_text.widthHint = 160;
        text.setLayoutData(gd_text);
        new Label(compositCondition, SWT.NONE);
        
        Label lblCreator = new Label(compositCondition, SWT.NONE);
        lblCreator.setText("»ý¼ºÀÚ");
        lblCreator.setFont(SWTResourceManager.getFont("¸¼Àº °íµñ", 10, SWT.NORMAL));
        
        Combo combo = new Combo(compositCondition, SWT.NONE);
        GridData gd_combo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_combo.widthHint = 127;
        combo.setLayoutData(gd_combo);
        
        Button btnCheckButton = new Button(compositCondition, SWT.CHECK);
        btnCheckButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            }
        });
        btnCheckButton.setText("Owned MECO");
        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 3;
        Label lSeparator = new Label(compositCondition, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(gdSprator);
        
        table = new Table(compositCondition, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        
        TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn.setWidth(120);
        tblclmnNewColumn.setText("MECO ¹øÈ£");
        
        TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
        tblclmnNewColumn_1.setWidth(250);
        tblclmnNewColumn_1.setText("MECO ¸í");
        
        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setWidth(130);
        tableColumn.setText("»ý¼ºÀÚ");

    }


}
