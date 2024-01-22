/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly.temp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;

/**
 * Class Name : SearchMecoView
 * Class Description :
 * 
 * @date 2013. 11. 13.
 * 
 */
public class SearchProductViewTemp extends Composite {
    private Text txtId;
    private Text textName;
    private Table table;

    /**
     * @param parent
     * @param style
     * @param id
     */
    public SearchProductViewTemp(Composite parent, int style) {
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

        Label lblId = new Label(compositCondition, SWT.NONE);
        GridData gd_lblId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblId.widthHint = 105;
        lblId.setLayoutData(gd_lblId);
        lblId.setText("Product 번호");

        txtId = new Text(compositCondition, SWT.BORDER);
        GridData gd_txtId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtId.widthHint = 160;
        txtId.setLayoutData(gd_txtId);

        Button btnSearch = new Button(compositCondition, SWT.NONE);
        GridData gd_btnSearch = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_btnSearch.widthHint = 60;
        gd_btnSearch.minimumWidth = 100;
        btnSearch.setLayoutData(gd_btnSearch);
        btnSearch.setText("검색");

        Label lblName = new Label(compositCondition, SWT.NONE);
        lblName.setText("Product 명");

        textName = new Text(compositCondition, SWT.BORDER);
        GridData gd_textName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_textName.widthHint = 160;
        textName.setLayoutData(gd_textName);
        new Label(compositCondition, SWT.NONE);
        GridData gdSprator = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gdSprator.horizontalSpan = 3;
        Label lSeparator = new Label(compositCondition, SWT.SEPARATOR | SWT.HORIZONTAL);
        lSeparator.setLayoutData(gdSprator);

        table = new Table(compositCondition, SWT.BORDER | SWT.FULL_SELECTION);
        GridData gd_table = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
        gd_table.heightHint = 300;
        table.setLayoutData(gd_table);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        TableColumn tblclmnNo = new TableColumn(table, SWT.NONE);
        tblclmnNo.setWidth(120);
        tblclmnNo.setText("Product 번호");

        TableColumn tblclmnName = new TableColumn(table, SWT.NONE);
        tblclmnName.setWidth(250);
        tblclmnName.setText("Product 명");

    }

}
