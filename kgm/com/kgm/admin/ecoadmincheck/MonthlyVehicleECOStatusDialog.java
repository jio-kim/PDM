/**
 * 
 */
package com.kgm.admin.ecoadmincheck;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

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
import com.kgm.common.utils.KeyValueArray;
import com.kgm.common.utils.StringUtil;
import com.kgm.common.utils.progressbar.WaitProgressor;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;

/**
 * @author 208748
 * 
 */
public class MonthlyVehicleECOStatusDialog extends Dialog
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

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public MonthlyVehicleECOStatusDialog(Shell parent)
	{
		super(parent);
		setText("월별 차종 설계변경 현황"); //########## Title
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
		shell.setSize(1180, 631);
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
//				resultCountLabel.setText("검색결과 : 0");
				grid.removeAll();
				int c = grid.getColumnCount();
				for (int i = c - 1; i >= 2; i--)
				{
					grid.getColumn(i).dispose();
				}
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
		searchButton.setImage(SWTResourceManager.getImage(MonthlyVehicleECOStatusDialog.class, "/icons/find_16.png"));
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
				ExportPOIExcel.exportDialog(shell, grid, "월별 차종 설계변경 현황", "월별 차종 설계변경 현황", "", null);
			}
		});
		exportExcelButton.setImage(SWTResourceManager.getImage(MonthlyVehicleECOStatusDialog.class, "/com/teamcenter/rac/common/images/exceldataset_16.png"));
		exportExcelButton.setText("Excel");

		grid = new Grid(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		grid.setHeaderVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		gridColumnGroup1 = new GridColumnGroup(grid, SWT.NONE);
		gridColumnGroup1.setText("구분");
		gridColumnGroup2 = new GridColumnGroup(grid, SWT.NONE);
		gridColumnGroup2.setText("차종");
		GridColumn gridColumn1 = new GridColumn(gridColumnGroup1, SWT.NONE);
		gridColumn1.setWidth(270);
		gridColumn1.setText("부서");
		GridColumn gridColumn2 = new GridColumn(gridColumnGroup1, SWT.NONE);
		gridColumn2.setWidth(100);
		gridColumn2.setText("건수");

		resultCountLabel = new Label(shell, SWT.NONE);
		resultCountLabel.setAlignment(SWT.RIGHT);
		resultCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	/**
	 * 검색
	 */
	protected void search(DataSet ds) throws Exception
	{
		ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteUtil.execute("com.kgm.service.ECOAdminCheckService", "getMonthlyVehicleECOStatus", ds);
		if (resultList == null || resultList.isEmpty())
		{
			return;
		}
		int count = 1;
		final Hashtable<String, String> vehicleListTable = new Hashtable<String, String>();
		KeyValueArray resultKVArray = new KeyValueArray();
		final KeyValueArray sumEcoVehKVArray = new KeyValueArray();
		final KeyValueArray sumEiVehKVArray = new KeyValueArray();
		final KeyValueArray sumEcoGroupKVArray = new KeyValueArray();
		final KeyValueArray sumEiGroupKVArray = new KeyValueArray();
		for (HashMap resultRow : resultList)
		{
			waitProgressor.setMessage("차종 column 생성중.");
			String groupName = StringUtil.nullToString((String) resultRow.get("GROUP_NAME"));
			String vehicleName = StringUtil.nullToString((String) resultRow.get("PLANT_NAME"));
			String vehicleCode = StringUtil.nullToString((String) resultRow.get("PLANT_SEQ"));
			int ecoCount = ((BigDecimal) resultRow.get("ECO_CNT")).intValue();
			int endItemCount = ((BigDecimal) resultRow.get("EITEM_CNT")).intValue();
			//차종 기준 ECO 합계..
			if (sumEcoVehKVArray.containsKey(vehicleName))
			{
				int ecoSum = (Integer) sumEcoVehKVArray.getValueAtKey(vehicleName);
				int sum = ecoCount + ecoSum;
				sumEcoVehKVArray.updateValue(vehicleName, sum);
			} else
			{
				sumEcoVehKVArray.put(vehicleName, ecoCount);
			}
			//차종 기준 End Item 합계..
			if (sumEiVehKVArray.containsKey(vehicleName))
			{
				int eiSum = (Integer) sumEiVehKVArray.getValueAtKey(vehicleName);
				int sum = endItemCount + eiSum;
				sumEiVehKVArray.updateValue(vehicleName, sum);
			} else
			{
				sumEiVehKVArray.put(vehicleName, endItemCount);
			}
			//group 기준 ECO 합계..
			if (sumEcoGroupKVArray.containsKey(groupName))
			{
				int ecoSum = (Integer) sumEcoGroupKVArray.getValueAtKey(groupName);
				int sum = ecoCount + ecoSum;
				sumEcoGroupKVArray.updateValue(groupName, sum);
			} else
			{
				sumEcoGroupKVArray.put(groupName, ecoCount);
			}
			//group 기준 End Item 합계..
			if (sumEiGroupKVArray.containsKey(groupName))
			{
				int eiSum = (Integer) sumEiGroupKVArray.getValueAtKey(groupName);
				int sum = endItemCount + eiSum;
				sumEiGroupKVArray.updateValue(groupName, sum);
			} else
			{
				sumEiGroupKVArray.put(groupName, endItemCount);
			}
			if (resultKVArray.containsKey(groupName))
			{
				KeyValueArray vcKVArray = (KeyValueArray) resultKVArray.getValueAtKey(groupName);
				vcKVArray.put(vehicleName, new Integer[] { ecoCount, endItemCount });
			} else
			{
				KeyValueArray vcKVArray = new KeyValueArray();
				vcKVArray.put(vehicleName, new Integer[] { ecoCount, endItemCount });
				resultKVArray.put(groupName, vcKVArray);
			}
			vehicleListTable.put(vehicleCode, vehicleName);
		}
		Enumeration vcenum = vehicleListTable.keys();
		final List<String> vcList = Collections.list(vcenum);
		Collections.sort(vcList);
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				//column 생성...
				for (int i = 0; i < vcList.size(); i++)
				{
					String vehName = vehicleListTable.get(vcList.get(i));
					GridColumn gridColumn = new GridColumn(gridColumnGroup2, SWT.NONE);
					gridColumn.setAlignment(SWT.CENTER);
					gridColumn.setWidth(vehName.length() * 11);
					gridColumn.setText(vehName);
				}
				GridColumn gridColumn = new GridColumn(grid, SWT.NONE);
				gridColumn.setAlignment(SWT.CENTER);
				gridColumn.setWidth(80);
				gridColumn.setText("SUM");
			}
		});
		for (int i = 0; i < resultKVArray.size(); i++)
		{
			waitProgressor.setMessage("검색 데이터 로드중...(" + count + "/" + resultKVArray.size() + ")");
			final String groupName = (String) resultKVArray.getKey(i);
			final KeyValueArray vcKVArray = (KeyValueArray) resultKVArray.getValue(i);
			final int x = i;
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					GridItem gridItem1 = new GridItem(grid, SWT.NONE);
					gridItem1.setText(0, groupName);
					gridItem1.setText(1, "ECO 건수");
					GridItem gridItem2 = new GridItem(grid, SWT.NONE);
					gridItem2.setText(0, "");
					gridItem2.setText(1, "E/ITEM 건수");

					GridColumn[] columns = grid.getColumns();
					for (GridColumn column : columns)
					{
						Integer[] counts = (Integer[]) vcKVArray.getValueAtKey(column.getText());
						if (counts == null)
						{
							continue;
						}
						int columnIndex = grid.indexOf(column);
						gridItem1.setText(columnIndex, counts[0] == 0 ? "" : String.valueOf(counts[0]));
						gridItem2.setText(columnIndex, counts[1] == 0 ? "" : String.valueOf(counts[1]));
					}
					gridItem1.setText(grid.getColumnCount() - 1, String.valueOf(sumEcoGroupKVArray.getValueAtKey(groupName)));
					gridItem2.setText(grid.getColumnCount() - 1, String.valueOf(sumEiGroupKVArray.getValueAtKey(groupName)));
					for (int j = 0; ((x % 2 != 0) && j < grid.getColumnCount()); j++)
					{
						gridItem1.setBackground(j, SWTResourceManager.getColor(224, 255, 255));
						gridItem2.setBackground(j, SWTResourceManager.getColor(224, 255, 255));
					}
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

				int sumEco = 0;
				int sumEi = 0;
				GridColumn[] columns = grid.getColumns();
				for (GridColumn column : columns)
				{
					Integer ecoCount = (Integer) sumEcoVehKVArray.getValueAtKey(column.getText());
					if (ecoCount == null)
					{
						continue;
					}
					Integer eiCount = (Integer) sumEiVehKVArray.getValueAtKey(column.getText());
					if (eiCount == null)
					{
						continue;
					}
					int columnIndex = grid.indexOf(column);
					gridItem1.setText(columnIndex, String.valueOf(ecoCount));
					gridItem2.setText(columnIndex, String.valueOf(eiCount));
					sumEco += ecoCount;
					sumEi += eiCount;
				}
				gridItem1.setText(grid.getColumnCount() - 1, String.valueOf(sumEco));
				gridItem2.setText(grid.getColumnCount() - 1, String.valueOf(sumEi));
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
