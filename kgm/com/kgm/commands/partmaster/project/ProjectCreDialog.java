package com.kgm.commands.partmaster.project;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.partmaster.PartMasterOperation;
import com.kgm.commands.partmaster.validator.ProjectValidator;
import com.kgm.common.SYMCClass;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Project ���� Dialog
 *
 */
public class ProjectCreDialog extends SYMCAbstractDialog
{
	/** TC Reigstry */
	private Registry registry;
	private TCSession session;
	/** Standard Part Info Panel */
	private ProjectInfoPanel infoPanel;
	/** Param Map */
	private HashMap<String, Object> paramMap;

	/**
	 * Project ������
	 * 
	 * @param paramShell
	 */
	public ProjectCreDialog(Shell paramShell, HashMap<String, Object> paramMap)
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
		setDialogTextAndImage("Project Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new ProjectInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
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
			PartMasterOperation operation = new PartMasterOperation(this, SYMCClass.S7_PROJECTTYPE, paramMap, infoPanel.attrMap, null);
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
			// Project Validator
			ProjectValidator validator = new ProjectValidator();
			String strMessage = validator.validate(infoPanel.attrMap, ProjectValidator.TYPE_VALID_CREATE);

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
