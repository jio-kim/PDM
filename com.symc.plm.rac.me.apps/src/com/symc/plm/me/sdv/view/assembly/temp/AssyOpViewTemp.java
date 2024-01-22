/**
 * 
 */
package com.symc.plm.me.sdv.view.assembly.temp;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import swing2swt.layout.BorderLayout;

/**
 * Class Name : AssyOpView
 * Class Description :
 * 
 * @date 2013. 11. 18.
 * 
 */
public class AssyOpViewTemp extends Composite {
    private Text txtVehicleCode;
    private Text txtLineCode;
    private Text txtFunctionCode;
    private Text txt;
    private Text txtProductNo;
    private Text txtBopVersion;
    private Text txtOpKorName;
    private Text txtOpEngName;
    private Text txtStationCode;
    private Text txtOperaterCode;
    private Text txtOpSequence;
    private Table table;

    /**
     * Create the composite.
     * 
     * @param parent
     * @param style
     */
    public AssyOpViewTemp(Composite parent, int style) {
        super(parent, style);
        // setLayout(new FillLayout(SWT.VERTICAL));
        BorderLayout borderLayout = new BorderLayout();
        setLayout(borderLayout);

        Group groupOp = new Group(this, SWT.NONE);
        groupOp.setText("공법 정보");
        groupOp.setLayoutData(BorderLayout.NORTH);
        groupOp.setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Composite groupOpComposite = new Composite(groupOp, SWT.NONE);
        GridLayout gl_groupOpComposite = new GridLayout(5, false);
        groupOpComposite.setLayout(gl_groupOpComposite);
        gl_groupOpComposite.horizontalSpacing = 10;
        gl_groupOpComposite.marginLeft = 10;
        groupOpComposite.setLayout(gl_groupOpComposite);        

        Label lblVehicleCode = new Label(groupOpComposite, SWT.NONE);
        GridData gd_lblVehicleCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lblVehicleCode.widthHint = 100;
        lblVehicleCode.setLayoutData(gd_lblVehicleCode);
        lblVehicleCode.setText("차종 코드");

        txtVehicleCode = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txtVehicleCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtVehicleCode.widthHint = 45;
        txtVehicleCode.setLayoutData(gd_txtVehicleCode);
        new Label(groupOpComposite, SWT.NONE);

        Label lblLineCode = new Label(groupOpComposite, SWT.NONE);
        lblLineCode.setText("Line 코드");

        txtLineCode = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txtLineCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtLineCode.widthHint = 45;
        txtLineCode.setLayoutData(gd_txtLineCode);

        Label lblFunctionCode = new Label(groupOpComposite, SWT.NONE);
        lblFunctionCode.setText("Function 코드");

        txtFunctionCode = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txtFunctionCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtFunctionCode.widthHint = 45;
        txtFunctionCode.setLayoutData(gd_txtFunctionCode);
        new Label(groupOpComposite, SWT.NONE);

        Label lblOpCode = new Label(groupOpComposite, SWT.NONE);
        lblOpCode.setText("공법 코드");

        txt = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txt = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txt.widthHint = 45;
        txt.setLayoutData(gd_txt);

        Label lblProductNo = new Label(groupOpComposite, SWT.NONE);
        lblProductNo.setText("Product No.");

        txtProductNo = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txtProductNo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtProductNo.widthHint = 80;
        txtProductNo.setLayoutData(gd_txtProductNo);
        new Label(groupOpComposite, SWT.NONE);

        Label lblBopVersion = new Label(groupOpComposite, SWT.NONE);
        lblBopVersion.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblBopVersion.setText("BOP 구분 버전");

        txtBopVersion = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txtBopVersion = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtBopVersion.widthHint = 45;
        txtBopVersion.setLayoutData(gd_txtBopVersion);

        Label lblOpKorName = new Label(groupOpComposite, SWT.NONE);
        lblOpKorName.setText("공법 한글명");

        txtOpKorName = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txtOpKorName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
        gd_txtOpKorName.widthHint = 340;
        txtOpKorName.setLayoutData(gd_txtOpKorName);

        Label lblOpEngName = new Label(groupOpComposite, SWT.NONE);
        lblOpEngName.setText("공법 영문명");

        txtOpEngName = new Text(groupOpComposite, SWT.BORDER);
        GridData gd_txtOpEngName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 4, 1);
        gd_txtOpEngName.widthHint = 340;
        txtOpEngName.setLayoutData(gd_txtOpEngName);

        Label lblDR = new Label(groupOpComposite, SWT.NONE);
        lblDR.setText("DR");

        Combo combo = new Combo(groupOpComposite, SWT.NONE);
        GridData gridData_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData_1.widthHint = 45;
        combo.setLayoutData(gridData_1);


        Group groupStation = new Group(this, SWT.NONE);
        groupStation.setLayoutData(BorderLayout.CENTER);
        groupStation.setText("공정 정보");

        GridLayout gl_groupStation = new GridLayout(5, false);
        gl_groupStation.horizontalSpacing = 10;
        gl_groupStation.marginLeft = 10;
        groupStation.setLayout(gl_groupStation);

        Label lblStationCode = new Label(groupStation, SWT.NONE);
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gridData.widthHint = 100;
        lblStationCode.setLayoutData(gridData);
        lblStationCode.setText("공정 코드");

        txtStationCode = new Text(groupStation, SWT.BORDER);
        GridData gd_txtStationCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtStationCode.widthHint = 80;
        txtStationCode.setLayoutData(gd_txtStationCode);
        new Label(groupStation, SWT.NONE);

        Label lblOperaterCode = new Label(groupStation, SWT.NONE);
        GridData gridData_2 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
        gridData_2.widthHint = 80;
        lblOperaterCode.setLayoutData(gridData_2);
        lblOperaterCode.setText("작업자 코드");

        txtOperaterCode = new Text(groupStation, SWT.BORDER);
        GridData gd_txtOperaterCode = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtOperaterCode.widthHint = 80;
        txtOperaterCode.setLayoutData(gd_txtOperaterCode);

        Label lblOpSequence = new Label(groupStation, SWT.NONE);
        lblOpSequence.setText("작업순");

        txtOpSequence = new Text(groupStation, SWT.BORDER);
        GridData gd_txtOpSequence = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_txtOpSequence.widthHint = 80;
        txtOpSequence.setLayoutData(gd_txtOpSequence);
        new Label(groupStation, SWT.NONE);

        Label lblOpWorkLocation = new Label(groupStation, SWT.NONE);
        lblOpWorkLocation.setText("작업 위치");

        Combo lovOpLocation = new Combo(groupStation, SWT.NONE);
        GridData gd_lovOpLocation = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_lovOpLocation.widthHint = 80;
        lovOpLocation.setLayoutData(gd_lovOpLocation);

        Group groupEtc = new Group(this, SWT.NONE);
        groupEtc.setLayoutData(BorderLayout.SOUTH);
        groupEtc.setText("기타 정보");
        GridLayout gl_groupEtc = new GridLayout(4, false);
        gl_groupEtc.horizontalSpacing = 10;
        gl_groupEtc.marginLeft = 10;
        groupEtc.setLayout(gl_groupEtc);

        Label lbldwgNo = new Label(groupEtc, SWT.NONE);
        lbldwgNo.setText("장착도면번호");

        table = new Table(groupEtc, SWT.BORDER | SWT.FULL_SELECTION | SWT.HIDE_SELECTION | SWT.VIRTUAL);
        GridData gd_table = new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1);
        gd_table.widthHint = 140;
        gd_table.heightHint = 40;
        table.setLayoutData(gd_table);
        table.setLinesVisible(true);

        Label lblSystem = new Label(groupEtc, SWT.NONE);
        lblSystem.setText("조립 시스템");

        Combo comboSystem = new Combo(groupEtc, SWT.NONE);
        GridData gd_comboSystem = new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1);
        gd_comboSystem.widthHint = 80;
        comboSystem.setLayoutData(gd_comboSystem);

        Label lblUBodyWork = new Label(groupEtc, SWT.NONE);
        lblUBodyWork.setText("U/Body Work");

        Combo combo_1 = new Combo(groupEtc, SWT.NONE);
        GridData gd_combo_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_combo_1.widthHint = 45;
        combo_1.setLayoutData(gd_combo_1);

        Label lblPartLocation = new Label(groupEtc, SWT.NONE);
        lblPartLocation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblPartLocation.setText("자재투입위치(상하)");

        Combo combo_2 = new Combo(groupEtc, SWT.NONE);
        GridData gd_combo_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
        gd_combo_2.widthHint = 80;
        combo_2.setLayoutData(gd_combo_2);

        Label lblMaxTimeCheck = new Label(groupEtc, SWT.NONE);
        lblMaxTimeCheck.setText("최대작업시간 유무");

        Button btnCheckMaxTime = new Button(groupEtc, SWT.CHECK);
        btnCheckMaxTime.setText("---");

        Label lblMainVehicleCheck = new Label(groupEtc, SWT.NONE);
        lblMainVehicleCheck.setText("대표차종 유무");

        Button btnCheckButton = new Button(groupEtc, SWT.CHECK);
        btnCheckButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
        TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setWidth(155);

        final TableEditor editor = new TableEditor(table);
        // The editor must have the same size as the cell and must
        // not be any smaller than 50 pixels.
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;
        editor.minimumWidth = 50;
        final int EDITABLECOLUMN = 0;

        for (int i = 0; i < 3; i++) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { "" });
        }

        table.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                // Clean up any previous editor control
                Control oldEditor = editor.getEditor();
                if (oldEditor != null)
                    oldEditor.dispose();

                // Identify the selected row
                TableItem item = (TableItem) e.item;
                if (item == null)
                    return;

                // The control that will be the editor must be a child of the Table
                Text newEditor = new Text(table, SWT.NONE);
                newEditor.setText(item.getText(EDITABLECOLUMN));
                newEditor.addModifyListener(new ModifyListener() {
                    public void modifyText(ModifyEvent me) {
                        Text text = (Text) editor.getEditor();
                        editor.getItem().setText(EDITABLECOLUMN, text.getText());
                    }
                });

                newEditor.addKeyListener(new KeyListener() {
                    public void keyReleased(KeyEvent e) {
                        if (e.keyCode == 13) {
                            editor.dispose();
                        }
                    }

                    public void keyPressed(KeyEvent e) {
                    }
                });

                newEditor.selectAll();
                newEditor.setFocus();
                editor.setEditor(newEditor, item, EDITABLECOLUMN);
            }
        });
    }
}
