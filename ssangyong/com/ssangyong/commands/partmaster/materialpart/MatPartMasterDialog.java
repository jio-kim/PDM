package com.ssangyong.commands.partmaster.materialpart;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.commands.partmaster.PartMasterOperation;
import com.ssangyong.commands.partmaster.validator.MatPartValidator;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Material Part 생성 Dialog
 * 
 */
public class MatPartMasterDialog extends SYMCAbstractDialog
{
	/** TC Reigstry */
	private Registry registry;
	/** Material Part Info Panel */
	private MatPartMasterInfoPanel infoPanel;
	/** Part Manage Dialog에서 넘어온 Param Map */
	private HashMap<String, Object> paramMap;


	/**
	 * Material Part 생성자
	 * 
	 * @param paramShell
	 * @param paramMap
	 *        : Part Manage Dialog에서 넘어온 Param Map
	 */
	public MatPartMasterDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry(this);
		this.paramMap = paramMap;

		setParentDialogCompositeSize(new Point(720, 500));
	}

	/**
	 * Main Info Panel 생성
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
	 * OK 버튼 실행시 호출
	 */
	@Override
	protected boolean apply()
	{
		try
		{
			infoPanel.getPropDataMap(infoPanel.attrMap);
			// Part 생성 Operation
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
	 * Part 생성 Validation
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
