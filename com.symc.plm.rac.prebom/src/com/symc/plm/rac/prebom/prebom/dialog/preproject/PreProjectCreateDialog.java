package com.symc.plm.rac.prebom.prebom.dialog.preproject;

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
import com.symc.plm.rac.prebom.prebom.validator.preproject.PreProjectValidator;
import com.symc.plm.rac.prebom.prebom.view.preproject.PreProjectInfoPanel;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Project 생성 Dialog
 *
 */
public class PreProjectCreateDialog extends SYMCAbstractDialog
{
	/** TC Reigstry */
	private Registry registry;
	private TCSession session;
	/** Standard Part Info Panel */
	private PreProjectInfoPanel infoPanel;
	/** Param Map */
	private HashMap<String, Object> paramMap;

	/**
	 * Project 생성자
	 * 
	 * @param paramShell
	 */
	public PreProjectCreateDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry(this);
		this.session = CustomUtil.getTCSession();

		this.paramMap = paramMap;

		setParentDialogCompositeSize(new Point(720, 245));
	}

	/**
	 * Main Info Panel 생성
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite)
	{
		parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
		setDialogTextAndImage("Pre-Project Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new PreProjectInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
		return infoPanel;
	}

	/**
	 * OK 버튼 실행시 호출
	 */
	@Override
	protected boolean apply()
	{
		try
		{
			infoPanel.getPropDataMap(infoPanel.attrMap);
			// Part 생성 Operation
			PrePartMasterOperation operation = new PrePartMasterOperation(this, TypeConstant.S7_PREPROJECTTYPE, paramMap, infoPanel.attrMap, null);
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
	 * Part 생성 Validation
	 */
	@Override
	protected boolean validationCheck()
	{

		try
		{
			infoPanel.getPropDataMap(infoPanel.attrMap);
			// Project Validator
			PreProjectValidator validator = new PreProjectValidator();
			String strMessage = validator.validate(infoPanel.attrMap, PreProjectValidator.TYPE_VALID_CREATE);

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
