package com.ssangyong.commands.partmaster;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.ec.search.ECOSearchDialog;
import com.ssangyong.commands.partmaster.functionmastpart.FunctionMastPartCreDialog;
import com.ssangyong.commands.partmaster.functionpart.FunctionPartCreDialog;
import com.ssangyong.commands.partmaster.productpart.ProductPartCreDialog;
import com.ssangyong.commands.partmaster.variantpart.VariantPartCreDialog;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.swtsearch.SearchItemRevDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class SYMCProductManagerDialog extends SYMCAbstractDialog {

    /** TC Registry */
    private Registry registry;

    /** BaseItem Search Button */
    private Button bBaseItem;
    /** ECO Search Button */
    private Button bECONo;

    /** BaseItem ID Field */
    Text tBaseItem;
    /** ECO No. Field */
    Text tECONo;

    /** PartType Radios */
    Button[] partTypeRadios;
    /** ActionType Radios */
    Button[] actionTypeRadios;

    /** SaveAs시 Target Revision */
    TCComponentItemRevision targetRev;

    // [SR140702-059][20140626] KOG SaveAs Flag
    private boolean isSaveAsFlag;

    /**
     * Create Product 기능에서 호출
     * 
     * @param paramShell
     */
    public SYMCProductManagerDialog(Shell paramShell) {
        super(paramShell);
        this.registry = Registry.getRegistry(this);
    }

    /**
     * SaveAs 기능에서 호출
     * 
     * @param paramShell
     * @param targetRev
     */
    public SYMCProductManagerDialog(Shell paramShell, TCComponentItemRevision targetRev) {
        this(paramShell);
        this.targetRev = targetRev;

    }

    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        setDialogTextAndImage("Selecting Item Type", registry.getImage("NewPartMasterDialogHeader.ICON"));
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout(6, false));

        GridData labelLayoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        labelLayoutData.widthHint = 65;
        labelLayoutData.verticalAlignment = SWT.CENTER;
        GridData separatorLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        separatorLayoutData.horizontalSpan = 6;

        Label lType = new Label(composite, SWT.RIGHT);
        lType.setLayoutData(labelLayoutData);
        lType.setText("Type");

        GridData radioLayoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        radioLayoutData.horizontalSpan = 5;

        GridLayout rgLayout = new GridLayout(6, true);
        rgLayout.marginTop = 0;
        rgLayout.marginLeft = 0;
        rgLayout.marginBottom = 0;
        rgLayout.marginRight = 0;
        Composite type = new Composite(composite, SWT.None);
        type.setLayoutData(radioLayoutData);
        type.setLayout(rgLayout);
        GridData rcLayoutData = new GridData(SWT.FILL, SWT.FILL, false, false);
        rcLayoutData.widthHint = 115;
        rcLayoutData.verticalIndent = 0;
        rcLayoutData.horizontalIndent = 0;

        // Part Types
        partTypeRadios = new Button[4];

        // Product Part
        partTypeRadios[0] = new Button(type, SWT.RADIO);
        if (this.targetRev == null) {
            partTypeRadios[0].setSelection(true);
        }
        partTypeRadios[0].setLayoutData(rcLayoutData);
        partTypeRadios[0].setText(Constants.ITEM_TYPE_PRODUCT);
        partTypeRadios[0].setData("ItemType", SYMCClass.S7_PRODUCTPARTTYPE);
        partTypeRadios[0].setData("RevisionType", SYMCClass.S7_PRODUCTPARTREVISIONTYPE);

        // Variant Part
        partTypeRadios[1] = new Button(type, SWT.RADIO);
        partTypeRadios[1].setLayoutData(rcLayoutData);
        partTypeRadios[1].setText(Constants.ITEM_TYPE_VARIANT);
        partTypeRadios[1].setData("ItemType", SYMCClass.S7_VARIANTPARTTYPE);
        partTypeRadios[1].setData("RevisionType", SYMCClass.S7_VARIANTPARTREVISIONTYPE);

        // Function Part
        partTypeRadios[2] = new Button(type, SWT.RADIO);
        partTypeRadios[2].setLayoutData(rcLayoutData);
        partTypeRadios[2].setText(Constants.ITEM_TYPE_FUNCTION);
        partTypeRadios[2].setData("ItemType", SYMCClass.S7_FNCPARTTYPE);
        partTypeRadios[2].setData("RevisionType", SYMCClass.S7_FNCPARTREVISIONTYPE);

        // Function Master Part
        partTypeRadios[3] = new Button(type, SWT.RADIO);
        partTypeRadios[3].setLayoutData(rcLayoutData);
        partTypeRadios[3].setText(Constants.ITEM_TYPE_FUNCTIONMASTER);
        partTypeRadios[3].setData("ItemType", SYMCClass.S7_FNCMASTPARTTYPE);
        partTypeRadios[3].setData("RevisionType", SYMCClass.S7_FNCMASTPARTREVISIONTYPE);

        // Part Type Radio Button 에 Selection Listener 등록
        for (int i = 0; i < partTypeRadios.length; i++) {
            partTypeRadios[i].addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    String strActionType = "";
                    for (int i = 0; i < actionTypeRadios.length; i++) {
                        if (actionTypeRadios[i].getSelection()) {
                            strActionType = actionTypeRadios[i].getText();
                            break;
                        } else {
                            actionTypeRadios[i].setSelection(false);
                        }
                    }
                    // PartType/ActionType에 따른 UI변경
                    changeType(((Button) e.getSource()).getText(), strActionType);
                }
            });
        }

        Label lSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        lSeparator.setLayoutData(separatorLayoutData);

        // Action Types
        actionTypeRadios = new Button[3];

        Label lAction = new Label(composite, SWT.RIGHT);
        Composite action = new Composite(composite, SWT.None);
        action.setLayoutData(radioLayoutData);
        action.setLayout(rgLayout);
        lAction.setLayoutData(labelLayoutData);
        lAction.setText("Action");

        // New Action
        actionTypeRadios[0] = new Button(action, SWT.RADIO);
        actionTypeRadios[0].setSelection(true);
        actionTypeRadios[0].setLayoutData(rcLayoutData);
        actionTypeRadios[0].setText(Constants.ACTIONTYPE_NEW);
        // SaveAs(Extract) Action
        actionTypeRadios[1] = new Button(action, SWT.RADIO);
        actionTypeRadios[1].setLayoutData(rcLayoutData);
        actionTypeRadios[1].setText(Constants.ACTIONTYPE_EXTRACT);
        // SaveAs(Different) Action
        actionTypeRadios[2] = new Button(action, SWT.RADIO);
        actionTypeRadios[2].setLayoutData(rcLayoutData);
        actionTypeRadios[2].setText(Constants.ACTIONTYPE_DEFERENT);

        // Action Type Radio Button 에 Selection Listener 등록
        for (int i = 0; i < actionTypeRadios.length; i++) {
            actionTypeRadios[i].addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {

                    String strItemType = "";
                    for (int i = 0; i < partTypeRadios.length; i++) {
                        if (partTypeRadios[i].getSelection()) {
                            strItemType = partTypeRadios[i].getText();
                            break;
                        }
                    }
                    // PartType/ActionType에 따른 UI변경
                    changeType(strItemType, ((Button) e.getSource()).getText());
                }
            });
        }

        GridData gdText = new GridData(SWT.FILL, SWT.FILL, false, false);
        gdText.widthHint = 150;

        Image iSearch = new Image(getShell().getDisplay(), registry.getImage("Search.ICON").getImageData().scaledTo(15, 15));
        Label lBaseItem = new Label(composite, SWT.RIGHT);
        lBaseItem.setLayoutData(labelLayoutData);
        lBaseItem.setText("Base Item");
        tBaseItem = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        tBaseItem.setLayoutData(gdText);
        tBaseItem.setEnabled(false);
        bBaseItem = new Button(composite, SWT.PUSH);
        bBaseItem.setImage(iSearch);
        bBaseItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                String strItemRevType = "";

                for (int i = 0; i < partTypeRadios.length; i++) {
                    if (partTypeRadios[i].getSelection()) {

                        strItemRevType = (String) partTypeRadios[i].getData("RevisionType");

                        break;
                    }
                }
                // BaseItem 검색 Dialog Open
                //20230831 cf-4357 seho 파트 검색 dialog 수정으로.. 생성자 및 검색 결과 부분 수정.
                SearchItemRevDialog itemDialog = new SearchItemRevDialog(getShell(), SWT.SINGLE, strItemRevType);
                // 검색된 BaseItem
                TCComponent[] selectedItems = (TCComponent[]) itemDialog.open();

                if (selectedItems != null) {
                    tBaseItem.setText(selectedItems[0].toDisplayString());
                    tBaseItem.setData(selectedItems[0]);
                }

            }
        });

        Label lECONo = new Label(composite, SWT.RIGHT);
        lECONo.setLayoutData(labelLayoutData);
        lECONo.setText("ECO No");
        tECONo = new Text(composite, SWT.BORDER | SWT.READ_ONLY);
        tECONo.setLayoutData(gdText);
        tECONo.setEnabled(false);
        bECONo = new Button(composite, SWT.PUSH);
        bECONo.setImage(iSearch);
        // Selection Listener 등록
        bECONo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                // ECO 검색 Dialog Open
                ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE);
                ecoSearchDialog.getShell().setText("ECO Search");
                ecoSearchDialog.setAllMaturityButtonsEnabled(false);
                ecoSearchDialog.setBInProcessSelect(false);
                ecoSearchDialog.setBCompleteSelect(false);

                ecoSearchDialog.open();
                // 검색된 Eco Item
                TCComponentItemRevision[] ecos = ecoSearchDialog.getSelectctedECO();
                if (ecos != null && ecos.length > 0) {
                    TCComponentItemRevision ecoIR = ecos[0];

                    tECONo.setText(ecoIR.toDisplayString());
                    tECONo.setData(ecoIR);

                }

            }
        });

        // SaveAs Action 인경우
        // Target Revision 속성 값 Setting
        if (this.targetRev != null) {
            try {
                // Target Revision의 Item Type
                String strItemType = this.targetRev.getItem().getType();

                tBaseItem.setText(this.targetRev.toDisplayString());
                tBaseItem.setData(this.targetRev);

                // SaveAs(Extract) Default Setting
                actionTypeRadios[0].setSelection(false);
                actionTypeRadios[1].setSelection(true);

                // Part Type Setting
                for (int i = 0; i < partTypeRadios.length; i++) {
                    if (strItemType.equals(partTypeRadios[i].getData("ItemType"))) {
                        partTypeRadios[i].setSelection(true);

                        this.changeType(partTypeRadios[i].getText(), Constants.ACTIONTYPE_EXTRACT);

                        break;
                    }
                }
            } catch (TCException e1) {
                e1.printStackTrace();
            }
        } else {
            // PartType/ActionType에 따른 UI변경
            this.changeType(Constants.ITEM_TYPE_PRODUCT, Constants.ACTIONTYPE_NEW);
        }

        getShell().setMinimumSize(800, 220);
        return composite;
    }

    /**
     * 선택된 Part Type에 따른 Stage, Regular 제어
     * 
     * @param strText
     *            : Part Type Text
     */
    private void changeType(String strItemType, String strActionType) {
        // Vehicle Part Type
        if (Constants.ITEM_TYPE_FUNCTIONMASTER.equals(strItemType)) {
            actionTypeRadios[0].setEnabled(true);
            actionTypeRadios[1].setEnabled(true);
            actionTypeRadios[2].setEnabled(true);

            // New Action Type
            // BaseItem/ECO Disable
            if (Constants.ACTIONTYPE_NEW.equals(strActionType)) {
                this.bBaseItem.setEnabled(false);
                this.bECONo.setEnabled(false);

                this.tBaseItem.setText("");
                this.tECONo.setText("");

            }
            // SAVE AS(Extract) Action Type
            // BaseItem/ECO Enable
            else if (Constants.ACTIONTYPE_EXTRACT.equals(strActionType) || Constants.ACTIONTYPE_DEFERENT.equals(strActionType)) {
                this.bBaseItem.setEnabled(true);
                this.bECONo.setEnabled(true);
            }

        }
        // Standard Part Type
        else {
            actionTypeRadios[0].setEnabled(true);
            actionTypeRadios[1].setEnabled(false);
            actionTypeRadios[2].setEnabled(false);

            actionTypeRadios[0].setSelection(true);
            actionTypeRadios[1].setSelection(false);
            actionTypeRadios[2].setSelection(false);

            bBaseItem.setEnabled(false);
            bECONo.setEnabled(false);
        }

    }

    /**
     * 'OK' Button 실행시 실행
     * 
     */
    @Override
    protected boolean apply() {
        // Part 생성 Dialog로 넘겨줄 Parameter Map
        final HashMap<String, Object> paramMap = new HashMap<String, Object>();

        // Item Type 저장
        for (int i = 0; i < partTypeRadios.length; i++) {
            if (partTypeRadios[i].getSelection()) {
                paramMap.put(Constants.ATTR_NAME_ITEMTYPE, partTypeRadios[i].getText());
                break;
            }
        }

        // Action Type 저장
        for (int i = 0; i < actionTypeRadios.length; i++) {
            if (actionTypeRadios[i].getSelection()) {
                paramMap.put(Constants.ATTR_NAME_ACTIONTYPE, actionTypeRadios[i].getText());
                break;
            }
        }

        // BaseItem 저장
        if (bBaseItem.isEnabled())
            paramMap.put(Constants.ATTR_NAME_BASEITEMID, tBaseItem.getData());
        // ECOItem 저장
        if (bECONo.isEnabled())
            paramMap.put(Constants.ATTR_NAME_ECOITEMID, tECONo.getData());

        // [SR140702-059][20140626] KOG Param Map에 SaveAs Parameter Setting.
        if (isSaveAsMode()) {
            paramMap.put(Constants.COMMAND_SAVE_AS, "TRUE");
        } else {
            paramMap.put(Constants.COMMAND_SAVE_AS, "FALSE");
        }

        // 현재창을 닫기 위해 Thread 구현
        this.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                try {

                    Shell shell = AIFUtility.getActiveDesktop().getShell();

                    String strItemType = (String) paramMap.get(Constants.ATTR_NAME_ITEMTYPE);

                    // Product Part 생성 Dialog
                    if (Constants.ITEM_TYPE_PRODUCT.equals(strItemType)) {
                        ProductPartCreDialog partMasterDialog = new ProductPartCreDialog(shell, paramMap);
                        partMasterDialog.open();
                    }
                    // Variant Part 생성 Dialog
                    else if (Constants.ITEM_TYPE_VARIANT.equals(strItemType)) {
                        VariantPartCreDialog partMasterDialog = new VariantPartCreDialog(shell, paramMap);
                        partMasterDialog.open();
                    }
                    // Function Part 생성 Dialog
                    else if (Constants.ITEM_TYPE_FUNCTION.equals(strItemType)) {
                        FunctionPartCreDialog partMasterDialog = new FunctionPartCreDialog(shell, paramMap);
                        partMasterDialog.open();
                    }
                    // Function Master Part 생성 Dialog
                    else if (Constants.ITEM_TYPE_FUNCTIONMASTER.equals(strItemType)) {
                        FunctionMastPartCreDialog partMasterDialog = new FunctionMastPartCreDialog(shell, paramMap);
                        partMasterDialog.open();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        return true;
    }

    @Override
    /**
     * 현재 창을 닫기 위해 Override
     */
    protected void okPressed() {
        if (!validationCheck()) {
            return;
        }
        if (apply()) {
            this.close();
        }
    }

    /**
     * Validation Check
     */
    @Override
    protected boolean validationCheck() {

        if (bBaseItem.isEnabled() && "".equals(tBaseItem.getText().trim())) {
            MessageBox.post(getShell(), "BaseItem Is Required.", "Warning", MessageBox.WARNING);
            return false;
        }
        if (bECONo.isEnabled() && "".equals(tECONo.getText().trim())) {
            MessageBox.post(getShell(), "ECO No Is Required.", "Warning", MessageBox.WARNING);
            return false;
        }

        return true;
    }

    /**
     * [SR140702-059][20140626] KOG Set SaveAs Flag
     */
    public void setSaveAsMode(boolean flag) {
        this.isSaveAsFlag = flag;
    }

    /**
     * [SR140702-059][20140626] KOG Is SaveAs Flag
     */
    public boolean isSaveAsMode() {
        return isSaveAsFlag;
    }

}
