/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.dialog.revise;

import java.awt.Frame;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

import com.kgm.common.SYMCText;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SWTUtilities;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.prebom.dialog.preccn.CCNSearchDialog;
import com.teamcenter.rac.aif.common.AIFTableLine;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * @author jinil
 *
 */
public class PreReviseDialog extends SYMCAbstractDialog {
    private TCSession session;
    private Registry registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.dialog.revise.revise");
    private SYMCText textCCNNo;
    private SYMCText textChangeDesc;
    private Button btnSearchCCN;
    private TCTable table;
    private boolean isBOMLine;
    private boolean isCCNRequired;
    private String project_code;

    public PreReviseDialog(Shell paramShell) {
        super(paramShell, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        session = CustomUtil.getTCSession();
        setApplyButtonVisible(false);
        super.create();
    }

    public PreReviseDialog(Shell shell, HashMap<String, Object> param, String prjcode) {
        super(shell, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM);
        session = CustomUtil.getTCSession();

        isBOMLine = (boolean) param.get("TargetBOMLine");
        isCCNRequired = (boolean) param.get("TargetCCNRequired");
        project_code = prjcode;
        
        setApplyButtonVisible(false);
        super.create();

        setInitData(param);
    }

    @SuppressWarnings("unchecked")
    private void setInitData(HashMap<String, Object> param) {
        if (param != null && param.size() > 0)
        {
            ArrayList<TCComponent> targetList = (ArrayList<TCComponent>) param.get("TargetRevisions");
            if (targetList != null && targetList.size() > 0)
            {
                table.removeAllRows();
                table.addRows(targetList.toArray(new TCComponent[0]));
            }
            if (isCCNRequired)
            {
                textCCNNo.setEnabled(true);
                btnSearchCCN.setEnabled(true);
                textCCNNo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            }
        }
    }

    /* (non-Javadoc)
     * @see com.kgm.common.dialog.SYMCAbstractDialog#createDialogPanel(org.eclipse.swt.custom.ScrolledComposite)
     */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        getShell().setText("Revise Dialog");
        getShell().setImage(com.teamcenter.rac.common.Activator.getDefault().getImage("icons/search_16.png"));
        SWTUtilities.skipKeyEvent(getShell());
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new BorderLayout(0, 0));

        Composite headComposite = new Composite(composite, SWT.NONE);
        headComposite.setLayout(new BorderLayout(0, 0));
        headComposite.setLayoutData(BorderLayout.NORTH);

        Group grpCCN = new Group(headComposite, SWT.NONE);
        grpCCN.setLayoutData(BorderLayout.NORTH);
        grpCCN.setText("CCN Information");
        grpCCN.setLayout(new GridLayout(5, false));

        Label lblMECONo = new Label(grpCCN, SWT.NONE);
        lblMECONo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        lblMECONo.setText("CCN No");

        textCCNNo = new SYMCText(grpCCN, SWT.BORDER | SWT.READ_ONLY);
        textCCNNo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        textCCNNo.setMandatory(true);

        btnSearchCCN = new Button(grpCCN, SWT.NONE);
        btnSearchCCN.setText("Search CCN");
        btnSearchCCN.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                // ECO 검색 Dialog
                CCNSearchDialog ccnSearchDialog = new CCNSearchDialog(getShell(), SWT.SINGLE);
                ccnSearchDialog.getShell().setText("CCN Search");

                ccnSearchDialog.open();

                // ECO 검색 결과
                TCComponentItemRevision[] ecos = ccnSearchDialog.getSelectctedECO();
                if (ecos != null && ecos.length > 0) {
                    TCComponentItemRevision ecoIR = ecos[0];

                    //[CSH 20181204] Revise 대상과 CCN Project Code가 일치하여야 함. (기술관리 송대영 책임)
                    try{
	                    if(!project_code.equals("") && !project_code.equals(ecoIR.getProperty(PropertyConstant.ATTR_NAME_PROJCODE))){
	                    	MessageBox.post(AIFUtility.getActiveDesktop(), "Project Code of CCN and FMP do not match.", registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.INFORMATION);
	                    } else {
		                    textCCNNo.setText(ecoIR.toDisplayString());
		                    textCCNNo.setData(ecoIR);
	                    }
                    } catch (Exception e){
                    	e.printStackTrace();
                    }
                }
            }
        });

        Group grpDesc = new Group(headComposite, SWT.NONE);
        grpDesc.setLayoutData(BorderLayout.CENTER);
        grpDesc.setText("Change Description");
        grpDesc.setLayout(new GridLayout(1, false));

        textChangeDesc = new SYMCText(grpDesc, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        textChangeDesc.setAlwaysShowScrollBars(false);
        GridData gd_textChangeDesc = new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 0);
        gd_textChangeDesc.heightHint = 50;
        textChangeDesc.setLayoutData(gd_textChangeDesc);

        if (! isCCNRequired)
        {
            textCCNNo.setEnabled(false);
            btnSearchCCN.setEnabled(false);
            textCCNNo.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        }

        Group reviseListGroup = new Group(composite, SWT.NONE);
        reviseListGroup.setLayoutData(BorderLayout.CENTER);
        reviseListGroup.setText("Revise List");
        reviseListGroup.setLayout(new BorderLayout(0, 0));

        Composite reviseListComp = new Composite(reviseListGroup, SWT.EMBEDDED);
        
        Frame frame = SWT_AWT.new_Frame(reviseListComp);
        
        Panel panel = new Panel();
        frame.add(panel);
        panel.setLayout(new java.awt.BorderLayout(0, 0));
        
        JRootPane rootPane = new JRootPane();
        panel.add(rootPane);

        String[] columnNames;
        if (isBOMLine)
            columnNames = new String[]{PropertyConstant.ATTR_NAME_BL_ITEM_ID, PropertyConstant.ATTR_NAME_BL_ITEM_REVISION_ID, PropertyConstant.ATTR_NAME_BL_REV_ITEM_NAME, PropertyConstant.ATTR_NAME_BL_REV_RELEASELIST};
        else
            columnNames = new String[]{PropertyConstant.ATTR_NAME_ITEMID, PropertyConstant.ATTR_NAME_ITEMREVID, PropertyConstant.ATTR_NAME_ITEMNAME, PropertyConstant.ATTR_NAME_RELEASESTATUSLIST};
        table = new TCTable(session, columnNames);//"ItemRevision_ColumnPreferences", "ItemRevision_ColumnWidthPreferences");
        table.setColumnWidths(new String[]{"24", "8", "30", "15"});
        
        JScrollPane scrollPane = new JScrollPane();
        rootPane.getContentPane().add(scrollPane, BorderLayout.CENTER);
        scrollPane.getViewport().add(table);

        table.setEditable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addEmptyRow();

        return composite;
    }

    /* (non-Javadoc)
     * @see com.kgm.common.dialog.SYMCAbstractDialog#validationCheck()
     */
    @Override
    protected boolean validationCheck() {
        if (isCCNRequired)
        {
            if (textCCNNo.getData() == null)
            {
                MessageBox.post(getShell(), registry.getString("PreReviseDialog.MESSAGE.NoInputEcoNo"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.WARNING);
                return false;
            }

            if (textChangeDesc.getText() == null || textChangeDesc.getText().trim().length() == 0)
            {
                MessageBox.post(getShell(), registry.getString("PreReviseDialog.MESSAGE.ChangeDesc"), registry.getString("PreReviseDialog.MESSAGE.Title.Warning"), MessageBox.WARNING);
                return false;
            }
        }

        return true;
    }

    /* (non-Javadoc)
     * @see com.kgm.common.dialog.SYMCAbstractDialog#apply()
     */
    @Override
    protected boolean apply() {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("TargetBOMLine", isBOMLine);
        param.put("CCNRequired", isCCNRequired);
        param.put("CCNObject", textCCNNo.getData());
        ArrayList<TCComponent> tableDatas = new ArrayList<TCComponent>();
        for (AIFTableLine tableData : table.dataModel.getAllData())
        {
            tableDatas.add((TCComponent) tableData.getComponent());
        }

        param.put("TargetRevisions", tableDatas.toArray(new TCComponent[0]));
        param.put("ChangeDesc", textChangeDesc.getText());

        PreReviseOperation op = new PreReviseOperation(param);

        try
        {
            op.executeOperation();

            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }

}
