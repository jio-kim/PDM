package com.kgm.commands.partmaster.materialpart;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.partmaster.PartMasterOperation;
import com.kgm.commands.partmaster.validator.MatPartValidator;
import com.kgm.common.SYMCClass;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Material Part ���� Dialog
 * 
 */
public class MatPartMasterDialog extends SYMCAbstractDialog
{
	/** TC Reigstry */
	private Registry registry;
	/** Material Part Info Panel */
	private MatPartMasterInfoPanel infoPanel;
	/** Part Manage Dialog���� �Ѿ�� Param Map */
	private HashMap<String, Object> paramMap;


	/**
	 * Material Part ������
	 * 
	 * @param paramShell
	 * @param paramMap
	 *        : Part Manage Dialog���� �Ѿ�� Param Map
	 */
	public MatPartMasterDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry(this);
		this.paramMap = paramMap;

		setParentDialogCompositeSize(new Point(720, 500));
	}

	/**
	 * Main Info Panel ����
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite)
	{
		parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
		setDialogTextAndImage("Material PartMaster Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new MatPartMasterInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
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
			PartMasterOperation operation = new PartMasterOperation(this, SYMCClass.S7_MATPARTTYPE, paramMap, infoPanel.attrMap, infoPanel.fileComposite);
			operation.executeOperation();
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
			// Material Part Validator
			MatPartValidator validator = new MatPartValidator();
			String strMessage = validator.validate(infoPanel.attrMap, MatPartValidator.TYPE_VALID_CREATE);

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
