package com.symc.plm.rac.prebom.prebom.dialog.prefuncmaster;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.prebom.operation.precreate.PrePartMasterOperation;
import com.symc.plm.rac.prebom.prebom.validator.prefuncmaster.PreFuncMasterValidator;
import com.symc.plm.rac.prebom.prebom.view.prefuncmaster.PreFuncMasterInfoPanel;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Function Master Part 생성 Dialog
 * 
 */
public class PreFuncMasterCreateDialog extends SYMCAbstractDialog
{
	/** TC Reigstry */
	private Registry registry;
	private TCSession session;
	/** Function Master Part Info Panel */
	private PreFuncMasterInfoPanel infoPanel;

	/** Part Manage Dialog에서 넘어온 Param Map */
	private HashMap<String, Object> paramMap;

	/**
	 * Function Master Part 생성자
	 * 
	 * @param paramShell
	 * @param paramMap
	 *        : Part Manage Dialog에서 넘어온 Param Map
	 */
	public PreFuncMasterCreateDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.dialog.dialog");
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
		setDialogTextAndImage("Pre-FunctionMaster Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new PreFuncMasterInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
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
			PrePartMasterOperation operation = new PrePartMasterOperation(this, TypeConstant.S7_PREFUNCMASTERTYPE, paramMap, infoPanel.attrMap, null);
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
			// Function Master Part Validator
			PreFuncMasterValidator validator = new PreFuncMasterValidator();
			String strMessage = validator.validate(infoPanel.attrMap, PreFuncMasterValidator.TYPE_VALID_CREATE);
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
