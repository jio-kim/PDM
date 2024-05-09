package com.kgm.commands.partmaster;

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

import com.kgm.commands.ec.search.ECOSearchDialog;
import com.kgm.commands.partmaster.materialpart.MatPartMasterDialog;
import com.kgm.commands.partmaster.stdpart.StdPartMasterDialog;
import com.kgm.commands.partmaster.vehiclepart.VehiclePartMasterDialog;
import com.kgm.common.SYMCClass;
import com.kgm.common.SYMCLOVCombo;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.swtsearch.SearchItemRevDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class SYMCPartManagerDialog extends SYMCAbstractDialog {
    /** TC Registry */
    private Registry registry;
    /** Stage ComboBox */
    private SYMCLOVCombo cStage;
    /** Regular ComboBox */
    private SYMCLOVCombo cRegular;

    /** BaseItem Search Button */
    private Button bBaseItem;
    /** ECO Search Button */
    private Button bECONo;

    /** BaseItem ID TextField */
    Text tBaseItem;
    /** ECO ID TextField */
    Text tECONo;

    /** Part Type Radio Buttons */
    Button[] partTypeRadios;
    /** Action Type Radio Buttons */
    Button[] actionTypeRadios;
    /** SaveAS(Different) ���ý� �������� CheckBox(���ý� Target Revision�� Dataset Rev.�� �°� */
    Button succeededChk;

    /** SaveAs�� Target Revision */
    TCComponentItemRevision targetRev;
    // [SR140702-059][20140626] KOG SaveAs Flag
    private boolean isSaveAsFlag;

    /**
     * Create Part ��ɿ��� ȣ��
     * 
     * @param paramShell
     */
    public SYMCPartManagerDialog(Shell paramShell) {
        super(paramShell);
        this.registry = Registry.getRegistry(this);
    }

    /**
     * SaveAs ��ɿ��� ȣ��
     * 
     * @param paramShell
     * @param targetRev
     */
    public SYMCPartManagerDialog(Shell paramShell, TCComponentItemRevision targetRev) {
        this(paramShell);
        this.targetRev = targetRev;

    }

    /**
     * ȭ�� �ʱ�ȭ
     */
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
        // Group g = new Group(composite, SWT.None);
        // g.setLayout(new RowLayout(SWT.HORIZONTAL));

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
        rcLayoutData.widthHint = 130;
        rcLayoutData.verticalIndent = 0;
        rcLayoutData.horizontalIndent = 0;

        // Part Types
        partTypeRadios = new Button[3];

        // Vehicle Part
        partTypeRadios[0] = new Button(type, SWT.RADIO);
        partTypeRadios[0].setSelection(true);
        partTypeRadios[0].setLayoutData(rcLayoutData);
        partTypeRadios[0].setText(Constants.ITEM_TYPE_VEHICLEPART);
        partTypeRadios[0].setData("ItemType", SYMCClass.S7_VEHPARTTYPE);

        // Standard Part
        partTypeRadios[1] = new Button(type, SWT.RADIO);
        partTypeRadios[1].setLayoutData(rcLayoutData);
        partTypeRadios[1].setText(Constants.ITEM_TYPE_STANDARDPART);
        partTypeRadios[1].setData("ItemType", SYMCClass.S7_STDPARTTYPE);

        // Material Part
        partTypeRadios[2] = new Button(type, SWT.RADIO);
        partTypeRadios[2].setLayoutData(rcLayoutData);
        partTypeRadios[2].setText(Constants.ITEM_TYPE_MATERIALPART);
        partTypeRadios[2].setData("ItemType", SYMCClass.S7_MATPARTTYPE);

        // Part Type Radio Button �� Selection Listener ���
        for (int i = 0; i < partTypeRadios.length; i++) {
            partTypeRadios[i].addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent e) {
                    String strActionType = "";
                    for (int i = 0; i < actionTypeRadios.length; i++) {
                        if (actionTypeRadios[i].getSelection()) {
                            strActionType = actionTypeRadios[i].getText();
                            break;
                        }
                    }

                    // PartType/ActionType�� ���� UI����
                    changeType(((Button) e.getSource()).getText(), strActionType, cStage.getText());
                }
            });
        }

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

        // SaveAs(Different) Action ���ý� �������� CheckBox
        succeededChk = new Button(action, SWT.CHECK);
        succeededChk.setLayoutData(rcLayoutData);
        succeededChk.setText("Succeed Dataset Rev.?");
        succeededChk.setVisible(false);

        // Action Type Radio Button �� Selection Listener ���
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
                    // PartType/ActionType/Stage�� ���� UI����
                    changeType(strItemType, ((Button) e.getSource()).getText(), cStage.getText());
                }
            });
        }

        Label lSeparator = new Label(composite, SWT.HORIZONTAL | SWT.SEPARATOR);
        lSeparator.setLayoutData(separatorLayoutData);

        // Stage
        Label lStage = new Label(composite, SWT.RIGHT);
        lStage.setLayoutData(labelLayoutData);
        lStage.setText("Stage");
        cStage = new SYMCLOVCombo(composite, "s7_STAGE");
        GridData gdStage = new GridData(SWT.NONE, SWT.FILL, false, false);
        gdStage.widthHint = 165;
        gdStage.horizontalSpan = 2;
        cStage.setLayoutData(gdStage);

        // Selection Listener ���
        cStage.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {

                String strItemType = "";
                for (int i = 0; i < partTypeRadios.length; i++) {
                    if (partTypeRadios[i].getSelection()) {
                        strItemType = partTypeRadios[i].getText();
                        break;
                    }
                }

                String strActionType = "";
                for (int i = 0; i < actionTypeRadios.length; i++) {
                    if (actionTypeRadios[i].getSelection()) {
                        strActionType = actionTypeRadios[i].getText();
                        break;
                    }
                }
                // PartType/ActionType/Stage�� ���� UI����
                changeType(strItemType, strActionType, cStage.getText());

            }
        });

        // Regular Attr
        Label lRegular = new Label(composite, SWT.RIGHT);
        lRegular.setLayoutData(labelLayoutData);
        lRegular.setText("Regular");
        cRegular = new SYMCLOVCombo(composite, "s7_REGULAR_PART");
        cRegular.setLayoutData(gdStage);

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
        // Selection Listener ���
        bBaseItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                // BaseItem �˻� Dialog Open
                SearchItemRevDialog itemDialog = new SearchItemRevDialog(getShell(), SWT.SINGLE, "S7_VehpartRevision");
                // �˻��� BaseItem
                TCComponent[] selectedItems = (TCComponent[]) itemDialog.open();

				if (selectedItems != null) {
					// 20140128 by bskwak (������C) ��� ���� ������ �׸��� �ƴ�, ���� ���� revision�� ���������� ����
					// 20230831 cf-4357 seho ���� �������� ������ �˻� ȭ�鿡�� �̹� �ɷ����� ���⼭ �ٽ� üũ�� �ʿ䰡 ����.
					tBaseItem.setText(selectedItems[0].toDisplayString());
					tBaseItem.setData(selectedItems[0]);
//					try {
//						// 20150115 bykim LatestItemRevision -> ReleasedItemRevisions
//						// TCComponentItemRevision compItemRevLatest = selectedItems[0].getItem().getLatestItemRevision();
//						TCComponentItemRevision[] releasedItemRevisions = selectedItems[0].getItem().getReleasedItemRevisions();
//						if (releasedItemRevisions == null || releasedItemRevisions.length == 0) {
//							throw new TCException("released�� �������� �������� �ʽ��ϴ�.");
//						}
//						TCComponentItemRevision releasedItemRevision = releasedItemRevisions[0];
//						tBaseItem.setText(releasedItemRevision.toDisplayString());
//						tBaseItem.setData(releasedItemRevision);
//					} catch (TCException e) {
//						MessageBox.post(getShell(), e.getMessage(), "Error", MessageBox.ERROR);
//
//						tBaseItem.setText("");
//						tBaseItem.setData(null);
//
//						e.printStackTrace();
//					}
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
        // Selection Listener ���
        bECONo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                // ECO �˻� Dialog Open
                ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE);
                ecoSearchDialog.getShell().setText("ECO Search");
                ecoSearchDialog.setAllMaturityButtonsEnabled(false);
                ecoSearchDialog.setBInProcessSelect(false);
                ecoSearchDialog.setBCompleteSelect(false);

                ecoSearchDialog.open();
                // �˻��� Eco Item
                TCComponentItemRevision[] ecos = ecoSearchDialog.getSelectctedECO();
                if (ecos != null && ecos.length > 0) {
                    TCComponentItemRevision ecoIR = ecos[0];

                    tECONo.setText(ecoIR.toDisplayString());
                    tECONo.setData(ecoIR);

                }

            }
        });

        // SaveAs Action �ΰ��
        // Target Revision �Ӽ� �� Setting
        if (this.targetRev != null) {
            try {
                // Target Revision�� Item Type
                String strItemType = this.targetRev.getItem().getType();

                // Stage
                String strStage = this.targetRev.getProperty("s7_STAGE");
                cStage.setText(strStage);
                if ("P".equals(strStage))
                    cStage.setEnabled(false);

                tBaseItem.setText(this.targetRev.toDisplayString());
                tBaseItem.setData(this.targetRev);

                // SaveAs(Extract) Default Setting
                actionTypeRadios[0].setSelection(false);
                actionTypeRadios[1].setSelection(true);

                // Part Type Setting
                for (int i = 0; i < partTypeRadios.length; i++) {
                    if (strItemType.equals(partTypeRadios[0].getData("ItemType"))) {
                        partTypeRadios[0].setSelection(true);
                        // PartType/ActionType/Stage�� ���� UI����
                        this.changeType(partTypeRadios[0].getText(), Constants.ACTIONTYPE_EXTRACT, strStage);

                        break;
                    }
                }

            } catch (TCException e1) {
                e1.printStackTrace();
            }

        } else {
            // PartType/ActionType/Stage�� ���� UI����
            this.changeType(Constants.ITEM_TYPE_VEHICLEPART, Constants.ACTIONTYPE_NEW, "");
        }

        getShell().setMinimumSize(800, 220);
        return composite;
    }

    /**
     * PartType/ActionType/Stage�� ���� UI����
     * 
     * @param strItemType
     *            : Item Type
     * @param strActionType
     *            : Action Type
     * @param strStage
     *            : Stage
     */
    private void changeType(String strItemType, String strActionType, String strStage) {
        // Vehicle Part Type
        if (Constants.ITEM_TYPE_VEHICLEPART.equals(strItemType)) {
            // ��� Action Type ���
            actionTypeRadios[0].setEnabled(true);
            actionTypeRadios[1].setEnabled(true);
            actionTypeRadios[2].setEnabled(true);

            cRegular.setEnabled(true);

            cStage.setEnabled(true);

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

                if (strStage.equals("P")) {
                    bECONo.setEnabled(true);
                } else {
                    bECONo.setEnabled(false);
                }
            }

            // Stage ���� 'P'�� ��� Regular 'R' �ڵ� Setting
            if (strStage.equals("P")) {
                cRegular.setText("R");
                cRegular.setEnabled(false);
            } else {
                cRegular.setEnabled(true);
            }

        }
        // Standard Part Type
        else if (Constants.ITEM_TYPE_STANDARDPART.equals(strItemType)) {
            // ��� Action Type ���
            actionTypeRadios[0].setEnabled(true);
            actionTypeRadios[1].setEnabled(true);
            actionTypeRadios[2].setEnabled(true);

            // Regular/Stage Disable
            cRegular.setText("");
            cStage.setText("");
            cRegular.setEnabled(false);
            cStage.setEnabled(false);
            bBaseItem.setEnabled(false);
            bECONo.setEnabled(false);

            // SaveAs �ΰ�� BaseItem Enable
            if (Constants.ACTIONTYPE_EXTRACT.equals(strActionType) || Constants.ACTIONTYPE_DEFERENT.equals(strActionType)) {
                this.bBaseItem.setEnabled(true);

            }

        }
        // Material Part Type
        else if (Constants.ITEM_TYPE_MATERIALPART.equals(strItemType)) {
            // New ActionType�� ���
            actionTypeRadios[0].setEnabled(true);
            actionTypeRadios[0].setSelection(true);

            actionTypeRadios[1].setSelection(false);
            actionTypeRadios[2].setSelection(false);
            actionTypeRadios[1].setEnabled(false);
            actionTypeRadios[2].setEnabled(false);

            // Regular/Stage Disable
            cRegular.setText("");
            cStage.setText("");
            cRegular.setEnabled(false);
            cStage.setEnabled(false);
            bBaseItem.setEnabled(false);
            bECONo.setEnabled(false);

        }

        // SaveAs(Different) ActionType�� ��� Dataset Rev. ��� CheckBox Enable
        succeededChk.setVisible(false);
        if (Constants.ACTIONTYPE_DEFERENT.equals(strActionType)) {
            succeededChk.setVisible(true);
            succeededChk.setSelection(true);
        } else
            succeededChk.setVisible(false);

    }

    /**
     * 'OK' Button ����� ����
     * 
     */
    @Override
    protected boolean apply() {

        // Part ���� Dialog�� �Ѱ��� Parameter Map
        final HashMap<String, Object> paramMap = new HashMap<String, Object>();

        // Item Type ����
        for (int i = 0; i < partTypeRadios.length; i++) {
            if (partTypeRadios[i].getSelection()) {
                paramMap.put(Constants.ATTR_NAME_ITEMTYPE, partTypeRadios[i].getText());
                break;
            }
        }

        // Action Type ����
        String strActionType = "";
        for (int i = 0; i < actionTypeRadios.length; i++) {
            if (actionTypeRadios[i].getSelection()) {
                strActionType = actionTypeRadios[i].getText();
                paramMap.put(Constants.ATTR_NAME_ACTIONTYPE, strActionType);
                break;
            }
        }

        // Stage ����
        paramMap.put(Constants.ATTR_NAME_STAGE, cStage.getText());
        // Regular ����
        paramMap.put(Constants.ATTR_NAME_REGULAR, cRegular.getText());
        // Dataset Rev. ��� ���� ����
        paramMap.put(Constants.ATTR_NAME_DATASETSUCCEED, new Boolean(this.succeededChk.getSelection()));

        // BaseItem ����
        if (bBaseItem.isEnabled())
            paramMap.put(Constants.ATTR_NAME_BASEITEMID, tBaseItem.getData());
        // ECOItem ����
        if (bECONo.isEnabled())
            paramMap.put(Constants.ATTR_NAME_ECOITEMID, tECONo.getData());

        if (strActionType.equals(Constants.ACTIONTYPE_DEFERENT)) {
            String strMessage = CustomUtil.validateSaveAs((TCComponentItemRevision) tBaseItem.getData());
            if (!CustomUtil.isEmpty(strMessage)) {
                MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
                return false;
            }

        }

        // [SR140702-059][20140626] KOG Param Map�� SaveAs Parameter Setting.
        if (isSaveAsMode()) {
            paramMap.put(Constants.COMMAND_SAVE_AS, "TRUE");
        } else {
            paramMap.put(Constants.COMMAND_SAVE_AS, "FALSE");
        }

        // ����â�� �ݱ� ���� Thread ����
        this.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                try {

                    Shell shell = AIFUtility.getActiveDesktop().getShell();

                    String strActionType = (String) paramMap.get(Constants.ATTR_NAME_ITEMTYPE);

                    // Vehicle Part
                    if (Constants.ITEM_TYPE_VEHICLEPART.equals(strActionType)) {
                        VehiclePartMasterDialog partMasterDialog = new VehiclePartMasterDialog(shell, paramMap);
                        partMasterDialog.open();
                    }
                    // Standard Part
                    else if (Constants.ITEM_TYPE_STANDARDPART.equals(strActionType)) {
                        StdPartMasterDialog partMasterDialog = new StdPartMasterDialog(shell, paramMap);
                        partMasterDialog.open();
                    }
                    // Material Part
                    else if (Constants.ITEM_TYPE_MATERIALPART.equals(strActionType)) {
                        MatPartMasterDialog partMasterDialog = new MatPartMasterDialog(shell, paramMap);
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
     * ���� â�� �ݱ� ���� Override
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

        if (cStage.isEnabled() && "".equals(cStage.getText().trim())) {
            MessageBox.post(getShell(), "Stage Is Required.", "Warning", MessageBox.WARNING);
            return false;
        } else if (cRegular.isEnabled() && "".equals(cRegular.getText().trim())) {
            MessageBox.post(getShell(), "Regular Is Required.", "Warning", MessageBox.WARNING);
            return false;
        }

        if (bBaseItem.isEnabled() && "".equals(tBaseItem.getText().trim())) {
            MessageBox.post(getShell(), "BaseItem Is Required.", "Warning", MessageBox.WARNING);
            return false;
        }

        //
        // if( bECONo.isEnabled() && "".equals(tECONo.getText().trim()) )
        // {
        // MessageBox.post(getShell(), "ECO No Is Required.", "Warning", MessageBox.WARNING);
        // return false;
        // }
        //

        cStage.setMandatory(false);
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
