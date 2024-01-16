package com.ssangyong.commands.showon;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.Registry;

/**
 * 2D ShowOn Dataset List 조회
 * CATDrawing/PDF Dataset 조회
 * 
 */
public class ViewShowOnDialog extends SYMCAbstractDialog
{

	/** TC Registry */
	private Registry registry;
	/** Target Revision */
	TCComponentItemRevision showOnRev;

	private Table resultTable;

	ArrayList<TCComponentDataset> dataList;

	private String[] columnName = new String[] { "Name", "Type", "Creation Date" };
	private int[] columnSize = new int[] { 240, 120, 120 };

	public ViewShowOnDialog(Shell paramShell, TCComponentItemRevision showOnRev)
	{
		super(paramShell);
		this.registry = Registry.getRegistry(this);
		this.showOnRev = showOnRev;

		super.setApplyButtonVisible(false);

		setParentDialogCompositeSize(new Point(500, 150));

	}

	/**
	 * 화면 초기화
	 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite)
	{
		parentScrolledComposite.setBackground(new Color(null, 255, 255, 255));
		setDialogTextAndImage("ShowOn 2D Dataset", registry.getImage("NewPartMasterDialogHeader.ICON"));

		resultTable = new Table(parentScrolledComposite, SWT.FILL | SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.addMouseListener(new MouseAdapter()
		{
			public void mouseDoubleClick(MouseEvent e)
			{
				int i = resultTable.getSelectionIndex();
				if (i < 0)
					return;
				if (dataList == null || dataList.size() == 0)
					return;

				try
				{
					dataList.get(i).open(false);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

			}
		});

		int i = 0;
		for (String value : columnName)
		{
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(columnSize[i]);
			i++;
		}

		try
		{

			roadDataSet(showOnRev);

		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return resultTable;
	}

	public void roadDataSet(TCComponentItemRevision revision) throws Exception
	{
		resultTable.removeAll();
		dataList = new ArrayList<TCComponentDataset>();

		TCComponent[] references = revision.getRelatedComponents(SYMCECConstant.DATASET_REL);
		for (TCComponent reference : references)
		{
			if (reference instanceof TCComponentDataset)
			{

				TCComponentDataset dataset = (TCComponentDataset) reference;

				if ("CATDrawing".equals(dataset.getType()) || "PDF".equals(dataset.getType()))
				{
					dataList.add(dataset);

					TableItem item = new TableItem(resultTable, SWT.NONE);
					item.setText(0, dataset.getProperty("object_name"));
					item.setText(1, dataset.getType());
					SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
					item.setText(2, dateFormat.format(dataset.getDateProperty("creation_date")));
				}
			}
		}
	}

	protected void afterCreateContents()
	{
		super.okButton.setVisible(false);
		super.cancelButton.setText("Close");
	}

	@Override
	protected boolean apply()
	{
		return true;
	}

	@Override
	protected boolean validationCheck()
	{

		return true;
	}

}
