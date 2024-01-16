/**
 * 
 */
package com.ssangyong.admin.ecoadmincheck;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.eclipse.nebula.widgets.calendarcombo.CalendarCombo;
import org.eclipse.nebula.widgets.calendarcombo.DefaultColorManager;
import org.eclipse.nebula.widgets.calendarcombo.DefaultSettings;
import org.eclipse.nebula.widgets.calendarcombo.ICalendarListener;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
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

import com.ssangyong.common.ExportPOIExcel;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.StringUtil;
import com.ssangyong.common.utils.progressbar.WaitProgressor;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.SWTUIUtilities;

/**
 * @author 208748
 * 
 */
public class EcoStatusByTeamDialog extends Dialog
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
	private Combo combo;
	private Label lblNewLabel_1;
	private CalendarCombo fromCalendarCombo;
	private CalendarCombo toCalendarCombo;
	private GridColumn gridColumn_1;
	private GridColumn gridColumn_2;
	private GridColumn gridColumn_3;
	private GridColumn gridColumn_4;
	private GridColumn gridColumn_5;
	private GridColumn gridColumn_6;
	private GridColumn gridColumn_7;
	private GridColumn gridColumn_8;
	private GridColumn gridColumn_9;
	private GridColumn gridColumn_10;
	private GridColumn gridColumn_11;
	private GridColumn gridColumn_12;
	private GridColumn gridColumn_13;
	private Button button;
	private Button button_1;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public EcoStatusByTeamDialog(Shell parent)
	{
		super(parent);
		setText("팀별 ECO 발행 현황(VEH/ENG/TM)"); //########## Title
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
		group.setLayout(new GridLayout(10, false));

		lblNewLabel = new Label(group, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText("ECO Type");

		combo = new Combo(group, SWT.READ_ONLY);
		combo.setItems(new String[] { "ALL", "Vehicle", "Engine", "T/M" });
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		combo.select(0);

		lblNewLabel_1 = new Label(group, SWT.NONE);
		GridData gd_lblNewLabel_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_1.horizontalIndent = 50;
		lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
		lblNewLabel_1.setText("Date");

		DefaultSettings mSettings = new DefaultSettings()
		{
			public String getDateFormat()
			{
				return "yyyy-MM-dd";
			}
		};
		Calendar toDayCalendar = Calendar.getInstance();
		fromCalendarCombo = new CalendarCombo(group, SWT.READ_ONLY, mSettings, new DefaultColorManager());
		fromCalendarCombo.addCalendarListener(new ICalendarListener()
		{
			public void dateChanged(Calendar date)
			{
				toCalendarCombo.setDisallowBeforeDate(date);
			}

			public void dateRangeChanged(Calendar start, Calendar end)
			{
			}

			public void popupClosed()
			{
			}
		});
		GridData gd_fromCalendarCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_fromCalendarCombo.widthHint = 100;
		fromCalendarCombo.setLayoutData(gd_fromCalendarCombo);
		Calendar firstAtMonthCalendar = (Calendar) toDayCalendar.clone();
		firstAtMonthCalendar.set(Calendar.DATE, 1);
		fromCalendarCombo.setDate(firstAtMonthCalendar);

		Label lblNewLabel_12 = new Label(group, SWT.NONE);
		lblNewLabel_12.setText(" - ");

		toCalendarCombo = new CalendarCombo(group, SWT.READ_ONLY, mSettings, new DefaultColorManager());
		toCalendarCombo.addCalendarListener(new ICalendarListener()
		{
			public void dateChanged(Calendar date)
			{
				fromCalendarCombo.setDisallowAfterDate(date);
			}

			public void dateRangeChanged(Calendar start, Calendar end)
			{
			}

			public void popupClosed()
			{
			}
		});
		fromCalendarCombo.setDependingCombo(toCalendarCombo);
		GridData gd_toCalendarCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_toCalendarCombo.widthHint = 100;
		toCalendarCombo.setLayoutData(gd_toCalendarCombo);
		toCalendarCombo.setDependingCombo(fromCalendarCombo);
		toCalendarCombo.setDate(toDayCalendar);

		button = new Button(group, SWT.NONE);
		button.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Calendar fromCalendar = fromCalendarCombo.getDate();
				if (fromCalendar != null)
				{
					fromCalendar.add(Calendar.YEAR, -1);
					fromCalendarCombo.setDate(fromCalendar);
				}
				Calendar toCalendar = toCalendarCombo.getDate();
				if (toCalendar != null)
				{
					toCalendar.add(Calendar.YEAR, -1);
					toCalendarCombo.setDate(toCalendar);
				}
			}
		});
		button.setToolTipText("1\uB144\uC804");
		button.setText("<");

		button_1 = new Button(group, SWT.NONE);
		button_1.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				Calendar fromCalendar = fromCalendarCombo.getDate();
				if (fromCalendar != null)
				{
					fromCalendar.add(Calendar.YEAR, 1);
					fromCalendarCombo.setDate(fromCalendar);
				}
				Calendar toCalendar = toCalendarCombo.getDate();
				if (toCalendar != null)
				{
					toCalendar.add(Calendar.YEAR, 1);
					toCalendarCombo.setDate(toCalendar);
				}
			}
		});
		button_1.setToolTipText("1\uB144\uD6C4");
		button_1.setText(">");

		searchButton = new Button(group, SWT.NONE);
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				String ecoKind = combo.getText();
				String fromDate = fromCalendarCombo.getDateAsString();
				String toDate = toCalendarCombo.getDateAsString();
				if (fromDate.isEmpty() || toDate.isEmpty())
				{
					MessageBox.post(shell, "검색 기간을 입력해 주세요.", "검색", MessageBox.WARNING);
					return;
				}
				resultCountLabel.setText("검색결과 : 0");
				grid.removeAll();
				final DataSet ds = new DataSet();
				ds.put("ECO_KIND", ecoKind);
				ds.put("FROM_DATE", fromDate);
				ds.put("TO_DATE", toDate);
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
		searchButton.setImage(SWTResourceManager.getImage(EcoStatusByTeamDialog.class, "/icons/find_16.png"));
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
				ExportPOIExcel.exportDialog(shell, grid, "팀별 ECO 발행 현황(VEH,ENG,TM)", "팀별 ECO 발행 현황(VEH,ENG,TM)", "", null);
			}
		});
		exportExcelButton.setImage(SWTResourceManager.getImage(EcoStatusByTeamDialog.class, "/com/teamcenter/rac/common/images/exceldataset_16.png"));
		exportExcelButton.setText("Excel");

		grid = new Grid(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
		grid.setHeaderVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		gridColumn_1 = new GridColumn(grid, SWT.NONE);
		gridColumn_1.setAlignment(SWT.CENTER);
		gridColumn_1.setText("No");
		gridColumn_1.setWidth(50);

		gridColumn_2 = new GridColumn(grid, SWT.NONE);
		gridColumn_2.setAlignment(SWT.CENTER);
		gridColumn_2.setText("Release Date");
		gridColumn_2.setWidth(150);

		gridColumn_3 = new GridColumn(grid, SWT.NONE);
		gridColumn_3.setAlignment(SWT.CENTER);
		gridColumn_3.setWidth(100);
		gridColumn_3.setText("ECO No");

		gridColumn_4 = new GridColumn(grid, SWT.NONE);
		gridColumn_4.setAlignment(SWT.CENTER);
		gridColumn_4.setWidth(80);
		gridColumn_4.setText("Owner");

		gridColumn_5 = new GridColumn(grid, SWT.NONE);
		gridColumn_5.setWidth(200);
		gridColumn_5.setText("Team");

		gridColumn_6 = new GridColumn(grid, SWT.NONE);
		gridColumn_6.setAlignment(SWT.CENTER);
		gridColumn_6.setWidth(95);
		gridColumn_6.setText("Change Reason");

		gridColumn_7 = new GridColumn(grid, SWT.NONE);
		gridColumn_7.setAlignment(SWT.CENTER);
		gridColumn_7.setWidth(90);
		gridColumn_7.setText("E/ITEM Count");

		gridColumn_8 = new GridColumn(grid, SWT.NONE);
		gridColumn_8.setWidth(150);
		gridColumn_8.setText("ECR/ECI No");

		gridColumn_9 = new GridColumn(grid, SWT.NONE);
		gridColumn_9.setWidth(150);
		gridColumn_9.setText("Project");

		gridColumn_10 = new GridColumn(grid, SWT.NONE);
		gridColumn_10.setWidth(100);
		gridColumn_10.setText("ADM Check");

		gridColumn_11 = new GridColumn(grid, SWT.NONE);
		gridColumn_11.setWidth(150);
		gridColumn_11.setText("\uBE44\uACE0");

		gridColumn_12 = new GridColumn(grid, SWT.NONE);
		gridColumn_12.setWidth(250);
		gridColumn_12.setText("ECO Description");

		gridColumn_13 = new GridColumn(grid, SWT.NONE);
		gridColumn_13.setWidth(250);
		gridColumn_13.setText("E/Item Name");

		resultCountLabel = new Label(shell, SWT.NONE);
		resultCountLabel.setAlignment(SWT.RIGHT);
		resultCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	/**
	 * 검색
	 */
	protected void search(DataSet ds) throws Exception
	{
		ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteUtil.execute("com.ssangyong.service.ECOAdminCheckService", "getEcoStatusByTeam", ds);
		if (resultList == null || resultList.isEmpty())
		{
			return;
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
			waitProgressor.setMessage("검색 데이터 로드중...(" + count + "/" + resultSize + ")");
			final String no = count++ + "";
			final String released_date = StringUtil.nullToString((String) resultRow.get("RELEASE_DATE"));
			final String ecoNo = StringUtil.nullToString((String) resultRow.get("ECO_NO"));
			final String user = StringUtil.nullToString((String) resultRow.get("OWNING_USER"));
			final String group = StringUtil.nullToString((String) resultRow.get("OWNING_TEAM"));
			final String reason = StringUtil.nullToString((String) resultRow.get("CHANGE_REASON"));
			final String eiCount = StringUtil.nullToString((String) resultRow.get("ENDITEMCOUNT"));
			final String ecr_eci = StringUtil.nullToString((String) resultRow.get("ECR_ECI"));
			final String project = StringUtil.nullToString((String) resultRow.get("PROJECTX"));
			final String adminCheck = StringUtil.nullToString((String) resultRow.get("PS7_ADMIN_CHECK"));
			final String note = StringUtil.nullToString((String) resultRow.get("PS7_NOTE"));
			final String desc = StringUtil.nullToString((String) resultRow.get("ECO_DESC"));
			final String eiName = StringUtil.nullToString((String) resultRow.get("EITEM_NAME"));
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					int c = 0;
					GridItem gridItem = new GridItem(grid, SWT.NONE);
					gridItem.setText(c++, no);
					gridItem.setText(c++, released_date);
					gridItem.setText(c++, ecoNo);
					gridItem.setText(c++, user);
					gridItem.setText(c++, group);
					gridItem.setText(c++, reason);
					gridItem.setText(c++, eiCount);
					gridItem.setText(c++, ecr_eci);
					gridItem.setText(c++, project);
					gridItem.setText(c++, adminCheck);
					gridItem.setText(c++, note);
					gridItem.setText(c++, desc);
					gridItem.setText(c++, eiName);
				}
			});
		}
	}
}
