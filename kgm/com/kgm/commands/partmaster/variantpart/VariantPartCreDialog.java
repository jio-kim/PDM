package com.kgm.commands.partmaster.variantpart;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.partmaster.PartMasterOperation;
import com.kgm.commands.partmaster.validator.VariantPartValidator;
import com.kgm.common.SYMCClass;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Variant Part 생성 Dialog
 * 
 */
public class VariantPartCreDialog extends SYMCAbstractDialog {
    /** TC Reigstry */
    private Registry registry;
    private TCSession session;
    /** Variant Part Info Panel */
    private VariantPartMasterInfoPanel infoPanel;
    // [SR140702-059][20140626] KOG Variant Part Create Dialog에 parameter hash map 추가
    private HashMap<String, Object> paramMap;

    /**
     * Variant Part 생성자
     * 
     * @param paramShell
     */
    public VariantPartCreDialog(Shell paramShell) {
        super(paramShell);
        this.registry = Registry.getRegistry(this);
        this.session = CustomUtil.getTCSession();

        setParentDialogCompositeSize(new Point(720, 350));
    }

    /**
     * [SR140702-059][20140626] KOG KOG Variant Part Create Dialog에 parameter hash map 추가 생성자.
     */
    public VariantPartCreDialog(Shell shell, HashMap<String, Object> paramMap) {
        super(shell);
        this.registry = Registry.getRegistry(this);
        this.session = CustomUtil.getTCSession();
        this.paramMap = paramMap;
        setParentDialogCompositeSize(new Point(720, 350));
    }

    /**
     * Main Info Panel 생성
     */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
        setDialogTextAndImage("Varient PartMaster Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
        infoPanel = new VariantPartMasterInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
        return infoPanel;
    }

    /**
     * OK 버튼 실행시 호출
     */
    @Override
    protected boolean apply() {
        try {
            infoPanel.getPropDataMap(infoPanel.attrMap);
            PartMasterOperation operation = new PartMasterOperation(this, SYMCClass.S7_VARIANTPARTTYPE, new HashMap<String, Object>(), infoPanel.attrMap, null);
            session.queueOperation(operation);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Part 생성 Validation
     */
    @Override
    protected boolean validationCheck() {

        try {
            infoPanel.getPropDataMap(infoPanel.attrMap);
            // Variant Part Validator
            VariantPartValidator validator = new VariantPartValidator();
            String strMessage = validator.validate(infoPanel.attrMap, VariantPartValidator.TYPE_VALID_CREATE);
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
