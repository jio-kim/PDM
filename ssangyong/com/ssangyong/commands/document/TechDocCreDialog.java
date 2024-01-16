package com.ssangyong.commands.document;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.partmaster.validator.TechDocValidator;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

@SuppressWarnings("unused")
public class TechDocCreDialog extends SYMCAbstractDialog {

    private Registry registry;
    private TCSession session;
    private TechDocInfoPanel infoPanel;
    private TechDocCreDialog dialog;

    /**
     * 
     * @copyright : S-PALM
     * @author : 권오규
     * @since : 2013. 1. 10.
     * @param paramShell
     */
    public TechDocCreDialog(Shell paramShell) {
        super(paramShell);
        this.dialog = this;
        this.registry = Registry.getRegistry(this);
        this.session = CustomUtil.getTCSession();

        setParentDialogCompositeSize(new Point(720, 500));
    }

    /**
     * 
     * @Copyright : S-PALM
     * @author : 권오규
     * @since : 2013. 1. 10.
     * @override
     * @see com.ssangyong.common.dialog.SYMCAbstractDialog#createDialogPanel(org.eclipse.swt.custom.ScrolledComposite)
     * @param parentScrolledComposite
     * @return
     */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
        setDialogTextAndImage("Tech Doc Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
        infoPanel = new TechDocInfoPanel(parentScrolledComposite, SWT.NONE);
        return infoPanel;
    }

    @Override
    protected boolean apply() {
        try {
            infoPanel.getPropDataMap(infoPanel.attrMap);
            TechDocMasterOperation operation = new TechDocMasterOperation(this, SYMCClass.S7_TECHDOCTYPE, new HashMap<String, Object>(), infoPanel.attrMap, infoPanel.fileComposite);
            operation.executeOperation();
            // session.queueOperation(operation);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected boolean validationCheck() 
    {
		try
		{
			infoPanel.getPropDataMap(infoPanel.attrMap);
			// Function Part Validator
			TechDocValidator validator = new TechDocValidator();
			String strMessage = validator.validate(infoPanel.attrMap, TechDocValidator.TYPE_VALID_CREATE);
			// Error 발생시 메시지 출력
			if (!CustomUtil.isEmpty(strMessage))
			{
				MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
				return false;
			}
		}
		catch (Exception e)
		{
			MessageBox.post(getShell(), e.getMessage(), "Warning", MessageBox.WARNING);
			return false;
		}

		return true;
    }

}
