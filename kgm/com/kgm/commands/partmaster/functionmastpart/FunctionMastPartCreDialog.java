package com.kgm.commands.partmaster.functionmastpart;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.partmaster.PartMasterOperation;
import com.kgm.commands.partmaster.validator.FncMastPartValidator;
import com.kgm.common.SYMCClass;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Function Master Part ���� Dialog
 * 
 */
public class FunctionMastPartCreDialog extends SYMCAbstractDialog
{
	/** TC Reigstry */
	private Registry registry;
	private TCSession session;
	/** Function Master Part Info Panel */
	private FncMastPartMasterInfoPanel infoPanel;

	/** Part Manage Dialog���� �Ѿ�� Param Map */
	private HashMap<String, Object> paramMap;

	/**
	 * Function Master Part ������
	 * 
	 * @param paramShell
	 * @param paramMap
	 *        : Part Manage Dialog���� �Ѿ�� Param Map
	 */
	public FunctionMastPartCreDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry(this);
		this.session = CustomUtil.getTCSession();

		this.paramMap = paramMap;

		setParentDialogCompositeSize(new Point(720, 350));
	}

	/**
	 * Main Info Panel ����
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite)
	{
		parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
		setDialogTextAndImage("FunctionMaster Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new FncMastPartMasterInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
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
			PartMasterOperation operation = new PartMasterOperation(this, SYMCClass.S7_FNCMASTPARTTYPE, paramMap, infoPanel.attrMap, null);
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
			// Function Master Part Validator
			FncMastPartValidator validator = new FncMastPartValidator();
			String strMessage = validator.validate(infoPanel.attrMap, FncMastPartValidator.TYPE_VALID_CREATE);
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
