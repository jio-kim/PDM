/**
 * 
 */
package com.kgm.admin.ecoadmincheck;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
import org.eclipse.swt.widgets.Combo;

/**
 * @author 208748
 * 
 */
public class MonthlyAllVehicleECOStatusDialog extends Dialog
{

	protected Object result;
	protected Shell shell;
	private WaitProgressor waitProgressor;
	private Group group;
	private Label lblNewLabel;
	private Button searchButton;
	private Button exportExcelButton;
	private Grid grid;
	private GridColumn gridColumn;
	private Label resultCountLabel;
	private Combo yearCombo;
	private Label reportTypeLb;
	private Button ecoRBtn;
	private Button engItemRBtn;
	private ArrayList<String> test;
	protected String reportType;
	private SYMCRemoteUtil remoteUtil;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public MonthlyAllVehicleECOStatusDialog(Shell parent)
	{
		super(parent);
		setText("월별 전차종(ECO/E-ITEM)"); //########## Title
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
		shell.setBackgroundMode(SWT.INHERIT_FORCE);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setSize(922, 631);
		shell.setText(getText());
		shell.setLayout(new GridLayout());
		group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout(7, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		lblNewLabel = new Label(group, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("Year");
		
		DataSet ds = new DataSet();
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		
		yearCombo = new Combo(group, SWT.READ_ONLY);
		setYearCombo();
		yearCombo.select(0);
		
		reportTypeLb = new Label(group, SWT.NONE);
		reportTypeLb.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		reportTypeLb.setText("Report Type Choice");
		
		ecoRBtn = new Button(group, SWT.RADIO);
		ecoRBtn.setText("ECO");
		
		engItemRBtn = new Button(group, SWT.RADIO);
		engItemRBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		engItemRBtn.setText("END ITEM");
		
		searchButton = new Button(group, SWT.NONE);
		searchButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				resultCountLabel.setText("검색결과 : 0");
				grid.removeAll();
				grid.getColumn(0).setText(yearCombo.getText()+" Year");
				final DataSet ds = new DataSet();
				ds.put("year", yearCombo.getText());
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
		searchButton.setImage(SWTResourceManager.getImage(MonthlyAllVehicleECOStatusDialog.class, "/icons/find_16.png"));
		searchButton.setText("검색");

		exportExcelButton = new Button(group, SWT.NONE);
		exportExcelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// shell, 테이블, sheet name, title, comment, 제외할 columninf
				ExportPOIExcel.exportDialog(shell, grid, yearCombo.getText() + "년도 월별 전차종("+reportType+")" , yearCombo.getText() + "년도 월별 전차종("+reportType+")", "", null);
			}
		});
		exportExcelButton.setImage(SWTResourceManager.getImage(MonthlyAllVehicleECOStatusDialog.class, "/com/teamcenter/rac/common/images/exceldataset_16.png"));
		exportExcelButton.setText("Excel");

		grid = new Grid(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		grid.setHeaderVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		String[] tableColumns = {" Year", "1Mon", "2Mon", "3Mon", "4Mon", "5Mon", "6Mon", "7Mon", "8Mon", "9Mon", "10Mon", "11Mon", "12Mon", "Sum"};
		int[] tableWidth = {100, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60, 60,};
		for(int i = 0; i < tableColumns.length; i ++){
			GridColumn column = new GridColumn(grid, SWT.NONE);
			if(i == 0){
//				column.setFrozen(true);
				
//				column.setMoveable(false);
//				column.setResizeable(false);
			}
			column.setText(tableColumns[i]);
			column.setWidth(tableWidth[i]);
		}

		resultCountLabel = new Label(shell, SWT.NONE);
		resultCountLabel.setAlignment(SWT.RIGHT);
		resultCountLabel.setText("검색결과 : 0");
		resultCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	protected void search(DataSet ds) throws Exception
	{
		ArrayList<HashMap> resultList = null;
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		try
		{
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					if(ecoRBtn.getSelection()){
						reportType = "ECO";
					}else{
						reportType = "ENDITEM";
					}
				}
			});
			if(reportType.equals("ECO")){
				resultList = (ArrayList<HashMap>) remoteQuery.execute("com.kgm.service.ECOAdminCheckService", "getMonthlyAllVehicleECOStatus", ds);	
			}else{
				resultList = (ArrayList<HashMap>) remoteQuery.execute("com.kgm.service.ECOAdminCheckService", "getMonthlyAllVehicleEndItemStatus", ds);
			}
			if (resultList == null || resultList.isEmpty())
			{
				return;
			}
		} catch (Exception e)
		{
			throw e;
		}
		final int resultSize = resultList.size();
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				resultCountLabel.setText("검색결과 : " + resultSize);
			}
		});
		int count = 1;
		for (HashMap resultRow : resultList)
		{
			waitProgressor.setMessage("검색 데이터 테이블 로드중...(" + count + "/" + resultList.size() + ")");		
			final String vehicleName = StringUtil.nullToString((String)(resultRow.get("VEHICLE_NAME")));
			final String jan = StringUtil.nullToString((String)(resultRow.get("1월")));
			final String feb = StringUtil.nullToString((String)(resultRow.get("2월")));
			final String mar = StringUtil.nullToString((String)(resultRow.get("3월")));
			final String apr = StringUtil.nullToString((String)(resultRow.get("4월")));
			final String may = StringUtil.nullToString((String)(resultRow.get("5월")));
			final String jun = StringUtil.nullToString((String)(resultRow.get("6월")));
			final String jul = StringUtil.nullToString((String)(resultRow.get("7월")));
			final String aug = StringUtil.nullToString((String)(resultRow.get("8월")));
			final String sep = StringUtil.nullToString((String)(resultRow.get("9월")));
			final String oct = StringUtil.nullToString((String)(resultRow.get("10월")));
			final String nov = StringUtil.nullToString((String)(resultRow.get("11월")));
			final String dec = StringUtil.nullToString((String)(resultRow.get("12월")));
			final String sum = StringUtil.nullToString(String.valueOf(resultRow.get("SUM")));
				//...
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					GridItem gridItem = new GridItem(grid, SWT.NONE);		
					gridItem.setText(0, vehicleName);
					gridItem.setText(1, jan);
					gridItem.setText(2, feb);
					gridItem.setText(3, mar);
					gridItem.setText(4, apr);
					gridItem.setText(5, may);
					gridItem.setText(6, jun);
					gridItem.setText(7, jul);
					gridItem.setText(8, aug);
					gridItem.setText(9, sep);
					gridItem.setText(10, oct);
					gridItem.setText(11, nov);
					gridItem.setText(12, dec);
					gridItem.setText(13, sum);

				}
			});
		}
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run()
			{
				grid.getItem(grid.getItems().length-1).setText(0,"Summary");
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
