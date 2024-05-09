package com.symc.plm.rac.prebom.prebom.dialog.prevehiclepart;

import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.prebom.operation.precreate.PrePartMasterOperation;
import com.symc.plm.rac.prebom.prebom.validator.prevehiclepart.PreVehiclePartValidator;
import com.symc.plm.rac.prebom.prebom.view.prevehiclepart.PreVehiclePartInfoPanel;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR140902-024][20140902] jclee, VPM 비교 로직 불필요로 인해 제거.
 * Vehicle Part 생성 Dialog
 *
 */
public class PreVehiclePartCreateDialog extends SYMCAbstractDialog
{

	/** TC Reigstry */
	private Registry registry;
	/** Vehicle Part Info Panel */
	private PreVehiclePartInfoPanel infoPanel;
	/** Part Manage Dialog에서 넘어온 Param Map */
	private HashMap<String, Object> paramMap;



	/**
	 * Vehicle Part 생성자
	 * 
	 * @param paramShell
	 * @param paramMap : Part Manage Dialog에서 넘어온 Param Map
	 */
	public PreVehiclePartCreateDialog(Shell paramShell, HashMap<String, Object> paramMap)
	{
		super(paramShell);
		this.registry = Registry.getRegistry("com.kgm.commands.partmaster.validator");
		this.paramMap = paramMap;

		setParentDialogCompositeSize(new Point(720, 445));
	}


	/**
	 * Main Info Panel 생성
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite)
	{
		parentScrolledComposite.setBackground(new Color(null, 0, 0, 0));
		setDialogTextAndImage("Pre-VehiclePart Creation Dialog", registry.getImage("NewPartMasterDialogHeader.ICON"));
		infoPanel = new PreVehiclePartInfoPanel(parentScrolledComposite, paramMap, SWT.NONE);
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
			PrePartMasterOperation operation = new PrePartMasterOperation(this, TypeConstant.S7_PREVEHICLEPARTTYPE, paramMap, infoPanel.attrMap, null);
			operation.executeOperation();

			// Apply 버튼 실행한 경우
//			if (isApplyPressed)
//			{
//				String strRegular = (String) infoPanel.attrMap.get("s7_REGULAR_PART");
//				if ("I".equals(strRegular))
//				{
//
//					// 비정규 품번인 경우 신규 ID 발번
//					String strNewID = SYMTcUtil.getNewID("T", 10);
//					infoPanel.setPartNo(strNewID);
//				}
//			}

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
			PreVehiclePartValidator validator = new PreVehiclePartValidator();
			String strMessage = validator.validate(infoPanel.attrMap, PreVehiclePartValidator.TYPE_VALID_CREATE);

			String strRegular = (String) infoPanel.attrMap.get("s7_REGULAR_PART");

			if (!CustomUtil.isEmpty(strMessage))
			{
				MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
				return false;
			}
			else if ("R".equals(strRegular))
			{

				String strPartNo = (String) infoPanel.attrMap.get(PropertyConstant.ATTR_NAME_ITEMID);
				String strOrign = (String) infoPanel.attrMap.get("s7_PART_TYPE");

				/**
				 * [SR140902-024][20140902] jclee, VPM 비교 불필요로 인해 제거.
				 */
//				try
//				{
//					// Vechicle Part 정규품번 생성시 VPM에 존재하는 Part No.인지 Check해야 함..
//					SYMCRemoteUtil remote = new SYMCRemoteUtil();
//					DataSet ds = new DataSet();
//					ds.put("partNo", strPartNo);
//
//					Object result = remote.execute("com.kgm.service.VPMIfService", "getExistVPMPartCnt", ds);
//
//					if (result instanceof Integer)
//					{
//						if (((Integer) result).intValue() > 0)
//						{
//							MessageBox.post(getShell(), "Part No. Exist In VPM", "Error", MessageBox.ERROR);
//							return false;
//						}
//					}
//
//				}
//				// 에러 발생시 Skip
//				catch (Exception e)
//				{
//					e.printStackTrace();
//				}

				// Orign 값이 'K'인 이고 마지막 자리값이 0이 아닌경우 마지막 자리값에서 -1을한 PartNo가 TeamCenter/VPM에 등록되어 있는지 Check
				// 등록되어 있지 않다면 Warning
				if ("K".equals(strOrign) && !strPartNo.endsWith("0"))
				{

					String strEndNo = strPartNo.substring(strPartNo.length() - 1);

					String strPrevNo = this.getKPrevNo(strEndNo.toCharArray()[0]);
					if (strPrevNo == null)
					{
						MessageBox.post(getShell(), "In Part No, Last Character Available Only '0-9,A-Z,a-z'", "Error", MessageBox.ERROR);
						return false;
					}
					else
					{

						String strPrevPartNo = strPartNo.substring(0, strPartNo.length() - 1) + strPrevNo;

						TCComponentItem prevItem = CustomUtil.findItem(TypeConstant.S7_PREVEHICLEPARTTYPE, strPrevPartNo);
						if (prevItem == null)
						{

							org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
							box.setText("Ask Proceed");
							box.setMessage("Previous Item ID(" + strPrevPartNo + ") Does Not Exist.  Are you sure you want to continue?");

							int choice = box.open();
							if (choice == SWT.NO)
								return false;

						}

					}

				}

				// Orign 값이 'A'/'B' 이고 7-8 자릿수의 값이 00이 아닌경우 그 값에서 -1한 PartNo가 TeamCenter/VPM에 등록되어 있는지 Check
				// 등록되어 있지 않다면 Warning
				if (("A".equals(strOrign) || "B".equals(strOrign)))
				{
					String strMidNo = strPartNo.substring(6, 8);
					if (!"00".equals(strMidNo))
					{
						String strPrevMidNo = this.getABPrevNo(strMidNo);

						// Null인 경우 Check하지 않음
						if (strPrevMidNo != null)
						{

							String strPrevPartNo = strPartNo.substring(0, 6) + strPrevMidNo + strPartNo.substring(8, strPartNo.length());
							TCComponentItem prevItem = CustomUtil.findItem(TypeConstant.S7_PREVEHICLEPARTTYPE, strPrevPartNo);
							if (prevItem == null)
							{
								org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(AIFUtility.getActiveDesktop().getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
								box.setText("Ask Proceed");
								box.setMessage("Previous Item ID(" + strPrevPartNo + ") Does Not Exist.  Are you sure you want to continue?");

								int choice = box.open();
								if (choice == SWT.NO)
									return false;
							}

						}

					}

				}
			}

		}
		catch (Exception e)
		{
			MessageBox.post(getShell(), e.getMessage(), "Warning", MessageBox.WARNING);
			return false;
		}

		return true;
	}

	/**
	 * Orign 값이 'K'인 이고 마지막 자리값이 0이 아닌경우 마지막 자리값에서 -1을한 PartNo가 TeamCenter/VPM에 등록되어 있는지 Check
	 * 
	 * 끝자리 발번 체계 => 0-9,A-Z,a-z
	 * 
	 * @param chr
	 * @return
	 */
	private String getKPrevNo(char chr)
	{
		if ('0' < chr && chr <= '9')
		{
			chr = (char) (chr - 1);
			return new String(new char[] { chr });
		}

		if (chr == 'A')
			return "9";

		if ('A' < chr && chr <= 'Z')
		{
			chr = (char) (chr - 1);
			return new String(new char[] { chr });
		}

		if (chr == 'a')
			return "Z";

		if ('a' < chr && chr <= 'z')
		{
			chr = (char) (chr - 1);
			return new String(new char[] { chr });
		}

		return null;
	}

	/**
	 * Orign 값이 'A'/'B' 이고 7-8 자릿수의 값이 00이 아닌경우 그 값에서 -1한 PartNo가 TeamCenter/VPM에 등록되어 있는지 Check
	 * 
	 * @param strMidNo
	 * @return
	 */
	private String getABPrevNo(String strMidNo)
	{

		try
		{
			int nPrevNo = Integer.parseInt(strMidNo) - 1;

			if (nPrevNo < 10)
				return "0" + nPrevNo;
			else
				return "" + nPrevNo;

		}
		catch (NumberFormatException e)
		{
			return null;
		}

	}

}
