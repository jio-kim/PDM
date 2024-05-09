package com.kgm.commands.partmaster.functionpart;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.partmaster.PartMasterOperation;
import com.kgm.commands.partmaster.validator.FncPartValidator;
import com.kgm.commands.partmaster.validator.StdPartValidator;
import com.kgm.common.SYMCClass;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Function Part ���� Dialog
 * 
 */
public class FunctionPartCreDialog extends SYMCAbstractDialog {
    /** TC Reigstry */
    private Registry registry;
    private TCSession session;
    /** Function Part Info Panel */
    private FncPartMasterInfoPanel infoPanel;
    
    // [SR140702-059][20140626] KOG Variant Part Create Dialog�� parameter hash map �߰�
    private HashMap<String, Object> paramMap;

    /**
     * Function Part ������
     * 
     * @param paramShell
     * @param paramMap
     *            : Part Manage Dialog���� �Ѿ�� Param Map
     */
    public FunctionPartCreDialog(Shell paramShell) {
        super(paramShell);
        this.registry = Registry.getRegistry(this);
        this.session = CustomUtil.getTCSession();

        setParentDialogCompositeSize(new Point(720, 350));
    }
    
    /**
     * [SR140702-059][20140626] KOG KOG Function Part Create Dialog�� parameter hash map �߰� ������.
     */
    public FunctionPartCreDialog(Shell shell, HashMap<String, Object> paramMap) {
        super(shell);
        this.registry = Registry.getRegistry(this);
        this.session = CustomUtil.getTCSession();
        this.paramMap = paramMap;
        setParentDialogCompositeSize(new Point(720, 350));
    }

    /**
     * Main Info Panel ����
     */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
        setDialogTextAndImage("Function PartMaster Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
        infoPanel = new FncPartMasterInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
        return infoPanel;
    }

    /**
     * OK ��ư ����� ȣ��
     */
    @Override
    protected boolean apply() {
        try {
            infoPanel.getPropDataMap(infoPanel.attrMap);
            // Part ���� Operation
            PartMasterOperation operation = new PartMasterOperation(this, SYMCClass.S7_FNCPARTTYPE, new HashMap<String, Object>(), infoPanel.attrMap, null);
            session.queueOperation(operation);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Part ���� Validation
     */
    @Override
    protected boolean validationCheck() {

        try {
            infoPanel.getPropDataMap(infoPanel.attrMap);
            // Function Part Validator
            FncPartValidator validator = new FncPartValidator();
            String strMessage = validator.validate(infoPanel.attrMap, StdPartValidator.TYPE_VALID_CREATE);
            // Error �߻��� �޽��� ���
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
