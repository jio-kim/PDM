package com.symc.plm.rac.prebom.ccn.dialog;

import java.util.HashMap;

import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.ccn.operation.PreCCNCreateOperation;
import com.symc.plm.rac.prebom.ccn.validator.PreCCNValidator;
import com.symc.plm.rac.prebom.ccn.view.PreCCNInfoPanel;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * CCN 생성 Dialog
 * 
 */
public class PreCCNCreateDialog extends SYMCAbstractDialog {

    /** TC Reigstry */
    private Registry registry;
    /** Pre-CCN Info Panel */
    private PreCCNInfoPanel infoPanel;
    /** Pre-CCN Dialog 에서 넘어온 Param Map */
    private HashMap<String, Object> paramMap;

    /**
     * Pre-CCN 생성자
     * 
     * @param paramShell
     * @param paramMap : Pre-CCN Dialog 에서 넘어온 Param Map
     * 
     */
    public PreCCNCreateDialog(Shell paramShell, HashMap<String, Object> paramMap) {
        super(paramShell);
        this.registry = Registry.getRegistry("com.symc.plm.rac.prebom.ccn.dialog.dialog");

        this.paramMap = paramMap;

        // 560, 435
        // [20240305] 창 크기 수정
        setParentDialogCompositeSize(new Point(680, 550));
    }

    /**
     * Main Info Panel 생성
     */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        parentScrolledComposite.setBackground(new Color(null, 255, 255, 255));
        setDialogTextAndImage("Pre-CCN Creation Dialog", registry.getImage("TeamcenterDialog.ICON"));
        infoPanel = new PreCCNInfoPanel(parentScrolledComposite, true);
        try {
            infoPanel.load();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return infoPanel;
    }

    protected boolean apply() {
        try    {
            // Pre-CCN 생성 Operation
            PreCCNCreateOperation operation = new PreCCNCreateOperation(this, TypeConstant.S7_PRECCNTYPE, paramMap, infoPanel.attrMap, null);
            operation.executeOperation();
//            session.queueOperation(operation);
            return true;
        } catch (Exception e) {
            MessageBox.post(getShell(), registry.getString("CreateCCN.Error.MESSAGE"), "Warning", MessageBox.WARNING);
            e.printStackTrace();
        }
        return false;
    }
    

    /**
     * Pre-CCN 생성 Validation
     */
    @Override
    protected boolean validationCheck(){
        try    {
            infoPanel.attrMap = infoPanel.getPropDataMap();
            // CCN Validator
            PreCCNValidator validator = new PreCCNValidator();
            String strMessage = validator.validate(infoPanel.attrMap, PreCCNValidator.TYPE_VALID_CREATE);

            // Error 발생시 메시지 출력
            if (!CustomUtil.isEmpty(strMessage)) {
                MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
                return false;
            }
        } catch (Exception e) {
            MessageBox.post(getShell(), e.getMessage(), "Warning", MessageBox.WARNING);
            return false;
        }
        return true;
    }

}
