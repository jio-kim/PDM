/**
 * 
 */
package com.kgm.admin.ecoadmincheck;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.common.ExportPOIExcel;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.common.utils.progressbar.WaitProgressor;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;

/**
 * @author 208748
 * 
 */
public class MonthlyEngineECOStatusDialog extends Dialog
{

	protected Object result;
	protected Shell shell;
	private WaitProgressor waitProgressor;
	private Group group;
	private Button searchButton;
	private Button exportExcelButton;
	private Grid grid;
	private Label resultCountLabel;
	private SYMCRemoteUtil remoteUtil;
	private Label lblNewLabel;
	private Combo yearCombo;
	private Combo monthCombo;
	private GridColumnGroup gridColumnGroup1;
	private GridColumnGroup gridColumnGroup2;
	private GridColumnGroup gridColumnGroup3;
	private GridColumn gridColumn_1;
	private GridColumn gridColumn_2;
	private GridColumn gridColumn_3;
	private GridColumn gridColumn_4;
	private GridColumn gridColumn_5;
	private GridColumn gridColumn_6;
	private GridColumn gridColumn_7;
	private int count;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public MonthlyEngineECOStatusDialog(Shell parent)
	{
		super(parent);
		setText("월별 엔진 설계변경 현황"); //########## Title
		remoteUtil = new SYMCRemoteUtil();
	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open()
	{
		createContents();
		SWTUIUtilities.centerInParent(getParent(), shell);
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed())
		{
			if (!display.readAndDispatch())
			{
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents()
	{
		shell = new Shell(getParent(), SWT.CLOSE | SWT.RESIZE);
		shell.setMinimumSize(new Point(1030, 300));
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setSize(1056, 631);
		shell.setText(getText());
		shell.setLayout(new GridLayout());
		group = new Group(shell, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		group.setLayout(new GridLayout(5, false));

		lblNewLabel = new Label(group, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Month");

		yearCombo = new Combo(group, SWT.READ_ONLY);
		yearCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		setYearCombo();
		yearCombo.select(0);

		monthCombo = new Combo(group, SWT.READ_ONLY);
		monthCombo.setItems(new String[] { "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12" });
		monthCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		monthCombo.select(Calendar.getInstance().get(Calendar.MONTH));

		searchButton = new Button(group, SWT.NONE);
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String yearString = yearCombo.getText();
				String monthString = monthCombo.getText();
				if (yearString.isEmpty() || monthString.isEmpty())
				{
					MessageBox.post(shell, "년, 월을 입력 후 검색해 주세요.", "검색", MessageBox.WARNING);
					return;
				}
				grid.removeAll();
				final DataSet ds = new DataSet();
				ds.put("YEAR_MONTH", yearString + monthString);
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						waitProgressor = new WaitProgressor(shell);
						waitProgressor.start();
						waitProgressor.setMessage("검색중...");
						try
						{
							search(ds);
							waitProgressor.end();
							MessageBox.post(shell, "검색이 완료되었습니다", "검색", MessageBox.INFORMATION);
						} catch (Exception e)
						{
							waitProgressor.end();
							e.printStackTrace();
							MessageBox.post(shell, e, true);
						}
					}
				}).start();
			}
		});
		searchButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		searchButton.setImage(SWTResourceManager.getImage(MonthlyEngineECOStatusDialog.class, "/icons/find_16.png"));
		searchButton.setText("검색");

		exportExcelButton = new Button(group, SWT.NONE);
		exportExcelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				if (grid.getItemCount() == 0)
				{
					MessageBox.post(shell, "데이터가 없습니다. 검색 후 내려받기 하세요.", "엑셀", MessageBox.INFORMATION);
					return;
				}
				// shell, 테이블, sheet name, title, comment, 제외할 column
				ExportPOIExcel.exportDialog(shell, grid, "월별 엔진 설계변경 현황", "월별 엔진 설계변경 현황", "", null);
			}
		});
		exportExcelButton.setImage(SWTResourceManager.getImage(MonthlyEngineECOStatusDialog.class, "/com/teamcenter/rac/common/images/exceldataset_16.png"));
		exportExcelButton.setText("Excel");

		grid = new Grid(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		grid.setHeaderVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		gridColumnGroup1 = new GridColumnGroup(grid, SWT.NONE);
		gridColumnGroup1.setText("구분");
		GridColumn gridColumn1 = new GridColumn(gridColumnGroup1, SWT.NONE);
		gridColumn1.setWidth(200);
		gridColumn1.setText("부서");
		GridColumn gridColumn2 = new GridColumn(gridColumnGroup1, SWT.NONE);
		gridColumn2.setWidth(100);
		gridColumn2.setText("건수");
		gridColumnGroup2 = new GridColumnGroup(grid, SWT.NONE);
		gridColumnGroup2.setText("ENG");

		gridColumn_1 = new GridColumn(gridColumnGroup2, SWT.NONE);
		gridColumn_1.setAlignment(SWT.CENTER);
		gridColumn_1.setText("GSL");
		gridColumn_1.setWidth(100);

		gridColumn_2 = new GridColumn(gridColumnGroup2, SWT.NONE);
		gridColumn_2.setWidth(100);
		gridColumn_2.setText("DSL");
		gridColumn_2.setAlignment(SWT.CENTER);

		gridColumn_3 = new GridColumn(grid, SWT.NONE);
		gridColumn_3.setAlignment(SWT.CENTER);
		gridColumn_3.setText("SUM");
		gridColumn_3.setWidth(100);

		gridColumnGroup3 = new GridColumnGroup(grid, SWT.NONE);
		gridColumnGroup3.setText("T/M");

		gridColumn_4 = new GridColumn(gridColumnGroup3, SWT.NONE);
		gridColumn_4.setWidth(100);
		gridColumn_4.setText("A/T");
		gridColumn_4.setAlignment(SWT.CENTER);

		gridColumn_5 = new GridColumn(gridColumnGroup3, SWT.NONE);
		gridColumn_5.setWidth(100);
		gridColumn_5.setText("M/T");
		gridColumn_5.setAlignment(SWT.CENTER);

		gridColumn_6 = new GridColumn(gridColumnGroup3, SWT.NONE);
		gridColumn_6.setWidth(100);
		gridColumn_6.setText("RR AXLE");
		gridColumn_6.setAlignment(SWT.CENTER);

		gridColumn_7 = new GridColumn(grid, SWT.NONE);
		gridColumn_7.setWidth(100);
		gridColumn_7.setText("SUM");
		gridColumn_7.setAlignment(SWT.CENTER);

		resultCountLabel = new Label(shell, SWT.NONE);
		resultCountLabel.setAlignment(SWT.RIGHT);
		resultCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	/**
	 * 검색
	 */
	protected void search(DataSet ds) throws Exception
	{
		ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteUtil.execute("com.kgm.service.ECOAdminCheckService", "getMonthlyEngineECOStatus", ds);
		if (resultList == null || resultList.isEmpty())
		{
			return;
		}
		count = 1;
		for (HashMap resultRow : resultList)
		{
			final String groupName = StringUtil.nullToString((String) resultRow.get("GROUP_NAME"));
			final String pt = StringUtil.nullToString((String) resultRow.get("PT"));
			final int ecoCount = ((BigDecimal) resultRow.get("ECO_CNT")).intValue();
			final int endItemCount = ((BigDecimal) resultRow.get("EITEM_CNT")).intValue();
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					GridItem gridItem1 = null;
					GridItem gridItem2 = null;
					for (int row = 0; row < grid.getItemCount(); row++)
					{
						GridItem tmpGridItem = grid.getItem(row);
						if (groupName.equals(tmpGridItem.getText(0)))
						{
							gridItem1 = tmpGridItem;
							gridItem2 = grid.getItem(row + 1);
							break;
						}
					}
					if (gridItem1 == null)
					{
						gridItem1 = new GridItem(grid, SWT.NONE);
						gridItem1.setText(0, groupName);
						gridItem1.setText(1, "ECO 건수");
						gridItem2 = new GridItem(grid, SWT.NONE);
						gridItem2.setText(0, "");
						gridItem2.setText(1, "E/ITEM 건수");
					}

					GridColumn[] columns = grid.getColumns();
					for (GridColumn column : columns)
					{
						if (column.getText().equalsIgnoreCase(pt))
						{
							int columnIndex = grid.indexOf(column);
							gridItem1.setText(columnIndex, ecoCount + "");
							gridItem2.setText(columnIndex, endItemCount + "");
							break;
						}
					}
					for (int j = 0; ((grid.getItemCount() % 4 == 0) && j < grid.getColumnCount()); j++)
					{
						gridItem1.setBackground(j, SWTResourceManager.getColor(224, 255, 255));
						gridItem2.setBackground(j, SWTResourceManager.getColor(224, 255, 255));
					}
					count++;
				}
			});
		}
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				GridItem gridItem1 = new GridItem(grid, SWT.NONE);
				gridItem1.setText(0, "총계");
				gridItem1.setText(1, "ECO 건수");
				GridItem gridItem2 = new GridItem(grid, SWT.NONE);
				gridItem2.setText(0, "");
				gridItem2.setText(1, "E/ITEM 건수");

				int sumEcoGSL = 0;
				int sumEcoDSL = 0;
				int sumEcoEngSum = 0;
				int sumEcoAT = 0;
				int sumEcoMT = 0;
				int sumEcoRRAXLE = 0;
				int sumEcoTmSum = 0;
				int sumEiGSL = 0;
				int sumEiDSL = 0;
				int sumEiEngSum = 0;
				int sumEiAT = 0;
				int sumEiMT = 0;
				int sumEiRRAXLE = 0;
				int sumEiTmSum = 0;
				for (int row = 0; row < grid.getItemCount(); row++)
				{
					GridItem tmpGridItem = grid.getItem(row);
					if (tmpGridItem.getText(1).equalsIgnoreCase("ECO 건수"))
					{
						int ecoGsl = Integer.parseInt(tmpGridItem.getText(2).isEmpty() ? "0" : tmpGridItem.getText(2));
						int ecoDsl = Integer.parseInt(tmpGridItem.getText(3).isEmpty() ? "0" : tmpGridItem.getText(3));
						int ecoEngSum = ecoGsl + ecoDsl;
						tmpGridItem.setText(4, ecoEngSum == 0 ? "" : String.valueOf(ecoEngSum));
						int ecoAt = Integer.parseInt(tmpGridItem.getText(5).isEmpty() ? "0" : tmpGridItem.getText(5));
						int ecoMt = Integer.parseInt(tmpGridItem.getText(6).isEmpty() ? "0" : tmpGridItem.getText(6));
						int ecoRrAxle = Integer.parseInt(tmpGridItem.getText(7).isEmpty() ? "0" : tmpGridItem.getText(7));
						int ecoTmSum = ecoAt + ecoMt + ecoRrAxle;
						tmpGridItem.setText(8, ecoTmSum == 0 ? "" : String.valueOf(ecoTmSum));
						sumEcoGSL += ecoGsl;
						sumEcoDSL += ecoDsl;
						sumEcoEngSum += ecoEngSum;
						sumEcoAT += ecoAt;
						sumEcoMT += ecoMt;
						sumEcoRRAXLE += ecoRrAxle;
						sumEcoTmSum += ecoTmSum;
					}
					if (tmpGridItem.getText(1).equalsIgnoreCase("E/ITEM 건수"))
					{
						int eiGsl = Integer.parseInt(tmpGridItem.getText(2).isEmpty() ? "0" : tmpGridItem.getText(2));
						int eiDsl = Integer.parseInt(tmpGridItem.getText(3).isEmpty() ? "0" : tmpGridItem.getText(3));
						int eiEngSum = eiGsl + eiDsl;
						tmpGridItem.setText(4, eiEngSum == 0 ? "" : String.valueOf(eiEngSum));
						int eiAt = Integer.parseInt(tmpGridItem.getText(5).isEmpty() ? "0" : tmpGridItem.getText(5));
						int eiMt = Integer.parseInt(tmpGridItem.getText(6).isEmpty() ? "0" : tmpGridItem.getText(6));
						int eiRrAxle = Integer.parseInt(tmpGridItem.getText(7).isEmpty() ? "0" : tmpGridItem.getText(7));
						int eiTmSum = eiAt + eiMt + eiRrAxle;
						tmpGridItem.setText(8, eiTmSum == 0 ? "" : String.valueOf(eiTmSum));
						sumEiGSL += eiGsl;
						sumEiDSL += eiDsl;
						sumEiEngSum += eiEngSum;
						sumEiAT += eiAt;
						sumEiMT += eiMt;
						sumEiRRAXLE += eiRrAxle;
						sumEiTmSum += eiTmSum;
					}
				}

				gridItem1.setText(2, sumEcoGSL == 0 ? "" : String.valueOf(sumEcoGSL));
				gridItem1.setText(3, sumEcoDSL == 0 ? "" : String.valueOf(sumEcoDSL));
				gridItem1.setText(4, sumEcoEngSum == 0 ? "" : String.valueOf(sumEcoEngSum));
				gridItem1.setText(5, sumEcoAT == 0 ? "" : String.valueOf(sumEcoAT));
				gridItem1.setText(6, sumEcoMT == 0 ? "" : String.valueOf(sumEcoMT));
				gridItem1.setText(7, sumEcoRRAXLE == 0 ? "" : String.valueOf(sumEcoRRAXLE));
				gridItem1.setText(8, sumEcoTmSum == 0 ? "" : String.valueOf(sumEcoTmSum));
				gridItem2.setText(2, sumEiGSL == 0 ? "" : String.valueOf(sumEiGSL));
				gridItem2.setText(3, sumEiDSL == 0 ? "" : String.valueOf(sumEiDSL));
				gridItem2.setText(4, sumEiEngSum == 0 ? "" : String.valueOf(sumEiEngSum));
				gridItem2.setText(5, sumEiAT == 0 ? "" : String.valueOf(sumEiAT));
				gridItem2.setText(6, sumEiMT == 0 ? "" : String.valueOf(sumEiMT));
				gridItem2.setText(7, sumEiRRAXLE == 0 ? "" : String.valueOf(sumEiRRAXLE));
				gridItem2.setText(8, sumEiTmSum == 0 ? "" : String.valueOf(sumEiTmSum));

				for (int i = 0; i < grid.getColumnCount(); i++)
				{
					gridItem1.setBackground(i, SWTResourceManager.getColor(224, 224, 224));
					gridItem2.setBackground(i, SWTResourceManager.getColor(224, 224, 224));
				}
			}
		});
	}

	private void setYearCombo()
	{
		ArrayList<String> resultList = null;
		try
		{
			resultList = (ArrayList<String>) remoteUtil.execute("com.kgm.service.ECOAdminCheckService", "getYear", null);
		} catch (Exception e)
		{
			e.printStackTrace();
			MessageBox.post(shell, e, true);
			return;
		}
		if (resultList == null || resultList.isEmpty())
		{
			return;
		}
		for (String resultRow : resultList)
		{
			final String year = StringUtil.nullToString((String) resultRow);
			if (year.isEmpty())
			{
				continue;
			}
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					yearCombo.add(year);
				}
			});
		}
	}
}
