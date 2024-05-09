package com.symc.plm.rac.prebom.prebom.dialog.preproduct;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.prebom.operation.precreate.PrePartMasterOperation;
import com.symc.plm.rac.prebom.prebom.validator.preproduct.PreProductValidator;
import com.symc.plm.rac.prebom.prebom.view.preproduct.PreProductInfoPanel;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Product Part ���� Dialog
 * 
 */
public class PreProductCreateDialog extends SYMCAbstractDialog
{

	/** TC Reigstry */
	private Registry registry;
	private TCSession session;
	/** Standard Part Info Panel */
	private PreProductInfoPanel infoPanel;
	/** Part Manage Dialog���� �Ѿ�� Param Map */
	private HashMap<String, Object> paramMap;

	/**
	 * Product Part ������
	 * 
	 * @param paramShell
	 * @param paramMap
	 *        : Part Manage Dialog���� �Ѿ�� Param Map
	 */
	public PreProductCreateDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.dialog.dialog");
		this.session = CustomUtil.getTCSession();

		this.paramMap = paramMap;

		setParentDialogCompositeSize(new Point(720, 340));
	}

	/**
	 * Main Info Panel ����
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite)
	{
		parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
		setDialogTextAndImage("Pre-Product Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new PreProductInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
		return infoPanel;
	}

	/**
	 * OK ��ư ����� ȣ��
	 */
	@Override
	protected boolean apply()
	{
		try
		{
			infoPanel.getPropDataMap(infoPanel.attrMap);
			// Part ���� Operation
			final PrePartMasterOperation operation = new PrePartMasterOperation(this, TypeConstant.S7_PREPRODUCTTYPE, paramMap, infoPanel.attrMap, null);

//			final AbstractAIFUIApplication aif = AIFUtility.getCurrentApplication();
//			if (aif.getClass().getSimpleName().equals("PSEApplicationService"))
//			{
//    			operation.addOperationListener(new InterfaceAIFOperationListener() {
//                    @Override
//                    public void startOperation(String arg0) {
//                    }
//                    
//                    @Override
//                    public void endOperation() {
//                        try {
//                            Thread.sleep(1000);
//    
//                            if (operation.isOperationDone() && operation.getNewComp() != null)
//                            {
//                                aif.open(operation.getNewComp());
//                            }
//                        } catch (Exception e) {
//                            e.printStackTrace();
//                            return;
//                        }
//                    }
//                });
//			}
			session.queueOperation(operation);
			return true;

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * Part ���� Validation
	 */
	@Override
	protected boolean validationCheck()
	{

		try
		{
			infoPanel.getPropDataMap(infoPanel.attrMap);
			// Product Part Validator
			PreProductValidator validator = new PreProductValidator();
			String strMessage = validator.validate(infoPanel.attrMap, PreProductValidator.TYPE_VALID_CREATE);

			// Error �߻��� �޽��� ���
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
