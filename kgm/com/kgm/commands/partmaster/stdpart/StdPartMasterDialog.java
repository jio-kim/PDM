package com.kgm.commands.partmaster.stdpart;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.commands.partmaster.PartMasterOperation;
import com.kgm.commands.partmaster.validator.StdPartValidator;
import com.kgm.common.SYMCClass;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;


/**
 * Standard Part 생성 Dialog
 *
 */
public class StdPartMasterDialog extends SYMCAbstractDialog
{
	/** TC Reigstry */
	private Registry registry;
	private TCSession session;
	/** Standard Part Info Panel */
	private StdPartMasterInfoPanel infoPanel;
	/** Part Manage Dialog에서 넘어온 Param Map */
	private HashMap<String, Object> paramMap;

	/**
	 * Standard Part 생성자
	 * 
	 * @param paramShell
	 * @param paramMap : Part Manage Dialog에서 넘어온 Param Map
	 */
	public StdPartMasterDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry(this);
		this.session = CustomUtil.getTCSession();

		this.paramMap = paramMap;

		setParentDialogCompositeSize(new Point(720, 350));
	}

	/**
	 * Main Info Panel 생성
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite)
	{
		parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
		setDialogTextAndImage("Standard PartMaster Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new StdPartMasterInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
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
			// Part 생성 Operation
			PartMasterOperation operation = new PartMasterOperation(this, SYMCClass.S7_STDPARTTYPE, paramMap, infoPanel.attrMap, null);
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
			// Vehicle Part Validator
			StdPartValidator validator = new StdPartValidator();
			String strMessage = validator.validate(infoPanel.attrMap, StdPartValidator.TYPE_VALID_CREATE);

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
