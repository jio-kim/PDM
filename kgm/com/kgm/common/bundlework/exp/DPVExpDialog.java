package com.kgm.common.bundlework.exp;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;

import com.kgm.common.bundlework.BWXLSExpDialog;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

/**
 * �ϰ� DPV�� ���� VehiclePart List�� Excel �ϰ� �ٿ�ε�
 * 
 * [SR140922-019][20140922] jhcho, 
 *  1. DPV����� �̿��Ͽ� part ������ Structure Manager ���� DPV�� Maturity �� Release�� ǥ�õǴ� ���� In work�� ���� 
 *  2. ECO_NO �ʱ�ȭ
 *  3. VPM_ECO_NO �ʱ�ȭ
 * 
 */
public class DPVExpDialog extends BWXLSExpDialog
{

	public DPVExpDialog(Shell parent, TCComponentItemRevision itemRevision, int style)
	{
		super(parent, itemRevision, style, DPVExpDialog.class);
	}

	@Override
	public void dialogOpen()
	{
		super.dialogOpen();
	}

	/**
	 * Data Load ��ó��
	 * 
	 * Double������ Data�� �Ӽ����� �´� �ڸ����� ǥ��
	 * [SR140922-019][20140922] jhcho, 
	 * 
	 *  1. DPV����� �̿��Ͽ� part ������ Structure Manager ���� DPV�� Maturity �� Release�� ǥ�õǴ� ���� In work�� ���� 
	 *  2. ECO_NO �ʱ�ȭv
	 *  3. VPM_ECO_NO �ʱ�ȭ
	 */
	@Override
	public void loadPost() throws Exception
	{
		shell.getDisplay().syncExec(new Runnable()
		{

			public void run()
			{
				TreeItem[] szTopItems = tree.getItems();

				for (int i = 0; i < szTopItems.length; i++)
				{

					// Top TreeItem
					ManualTreeItem topTreeItem = (ManualTreeItem) szTopItems[i];

					String strItemID = topTreeItem.getBWItemAttrValue(CLASS_TYPE_ITEM, "item_id");
					String strRevID = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "item_revision_id");
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_ITEM, "old_item_id", strItemID);
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "old_item_revision_id", strRevID);
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "item_revision_id", "000");

					String strEstWeight = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_EST_WEIGHT");
					String strCalWeight = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_CAL_WEIGHT");
					String strActWeight = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_ACT_WEIGHT");
					String strCalSurface = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_CAL_SURFACE");

					String strThick = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_THICKNESS");
					String strAltThick = topTreeItem.getBWItemAttrValue(CLASS_TYPE_REVISION, "s7_ALT_THICKNESS");

					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_EST_WEIGHT", getFormatedString(strEstWeight, "########.####"));
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_CAL_WEIGHT", getFormatedString(strCalWeight, "########.####"));
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_ACT_WEIGHT", getFormatedString(strActWeight, "########.####"));
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_CAL_SURFACE", getFormatedString(strCalSurface, "########.####"));

					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_THICKNESS", getFormatedString(strThick, "########.##"));
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_ALT_THICKNESS", getFormatedString(strAltThick, "########.##"));
                    // MATURITY �ʱ�ȭ
					topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_MATURITY", "In Work");
					// ECO_NO �ʱ�ȭ
                    topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_ECO_NO", "");
                    // VPM_ECO_NO �ʱ�ȭ
                    topTreeItem.setBWItemAttrValue(CLASS_TYPE_REVISION, "s7_VPM_ECO_NO", "");
				}
			}
		});

	}

	public String getFormatedString(String value, String format)
	{
		if (CustomUtil.isEmpty(value))
			return "";

		try
		{

			DecimalFormat df = new DecimalFormat(format);
			return df.format(Double.parseDouble(value));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return "";

	}

	@Override
	public void importDataPost(ManualTreeItem treeItem) throws Exception
	{

	}

}
