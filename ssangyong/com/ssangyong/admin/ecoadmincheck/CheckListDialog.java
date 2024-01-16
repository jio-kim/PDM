/**
 * 
 */
package com.ssangyong.admin.ecoadmincheck;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import org.eclipse.nebula.widgets.calendarcombo.CalendarCombo;
import org.eclipse.nebula.widgets.calendarcombo.DefaultColorManager;
import org.eclipse.nebula.widgets.calendarcombo.DefaultSettings;
import org.eclipse.nebula.widgets.calendarcombo.ICalendarListener;
import org.eclipse.nebula.widgets.grid.Grid;
import org.eclipse.nebula.widgets.grid.GridColumn;
import org.eclipse.nebula.widgets.grid.GridColumnGroup;
import org.eclipse.nebula.widgets.grid.GridItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
public class CheckListDialog extends Dialog
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
	private Composite composite;
	private Button vehicleRadioButton;
	private Button engineRadioButton;
	private Label lblNewLabel_1;
	private Label lblNewLabel_2;
	private Label lblNewLabel_3;
	private Combo productCombo;
	private Combo deptCombo;
	private Text userText;
	private Label lblNewLabel_4;
	private Label lblNewLabel_5;
	private Label lblNewLabel_6;
	private Label lblNewLabel_7;
	private Text ecoNoText;
	private Text projectText;
	private Text newProjectText;
	private Text noteText;
	private Label lblNewLabel_8;
	private Label lblNewLabel_9;
	private Label lblNewLabel_10;
	private Label lblNewLabel_11;
	private Composite composite_1;
	private CalendarCombo fromCalendarCombo;
	private Label lblNewLabel_12;
	private CalendarCombo toCalendarCombo;
	private Combo changeReasonCombo;
	private Combo changeReason1Combo;
	private Combo changeReason2Combo;
	private SYMCRemoteUtil remoteUtil;
	private GridColumn gridColumn_1;
	private GridColumn gridColumn_2;
	private GridColumn gridColumn_3;
	private GridColumn gridColumn_4;
	private GridColumn gridColumn_5;
	private GridColumnGroup gridColumnGroup;
	private GridColumn gridColumn_6;
	private GridColumn gridColumn_7;
	private GridColumn gridColumn_8;
	private GridColumn gridColumn_9;
	private GridColumn gridColumn_10;
	private GridColumn gridColumn_11;
	private GridColumn gridColumn_12;
	private GridColumn gridColumn_13;
	private Button allRadioButton;
	private GridColumn gridColumn_14;
	private Button yearBbutton;
	private Button yearAbutton;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public CheckListDialog(Shell parent)
	{
		super(parent);
		setText("Check List"); //########## Title
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
		lblNewLabel.setText("구분");

		composite = new Composite(group, SWT.NONE);
		composite.setLayout(new GridLayout(3, false));

		allRadioButton = new Button(composite, SWT.RADIO);
		allRadioButton.setSelection(true);
		allRadioButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setLOVCombo(productCombo, "S7_VEHICLE_NO", true);
				setLOVCombo(productCombo, "S7_PRODUCT_CODE", false);
			}
		});
		allRadioButton.setText("전체");

		vehicleRadioButton = new Button(composite, SWT.RADIO);
		vehicleRadioButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setLOVCombo(productCombo, "S7_VEHICLE_NO", true);
			}
		});
		vehicleRadioButton.setText("차량");

		engineRadioButton = new Button(composite, SWT.RADIO);
		engineRadioButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				setLOVCombo(productCombo, "S7_PRODUCT_CODE", true);
			}
		});
		engineRadioButton.setText("엔진");

		lblNewLabel_4 = new Label(group, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText("ECO NO");

		ecoNoText = new Text(group, SWT.BORDER);
		ecoNoText.addVerifyListener(new VerifyListener()
		{
			public void verifyText(VerifyEvent event)
			{
				event.text = event.text.toUpperCase();
			}
		});
		GridData gd_ecoNoText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_ecoNoText.widthHint = 100;
		ecoNoText.setLayoutData(gd_ecoNoText);

		lblNewLabel_8 = new Label(group, SWT.NONE);
		lblNewLabel_8.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_8.setText("Release Date");

		composite_1 = new Composite(group, SWT.NONE);
		GridLayout gl_composite_1 = new GridLayout(3, false);
		gl_composite_1.marginWidth = 0;
		gl_composite_1.marginHeight = 0;
		gl_composite_1.verticalSpacing = 0;
		composite_1.setLayout(gl_composite_1);

		DefaultSettings mSettings = new DefaultSettings()
		{
			public String getDateFormat()
			{
				return "yyyy-MM-dd";
			}
		};
		fromCalendarCombo = new CalendarCombo(composite_1, SWT.READ_ONLY, mSettings, new DefaultColorManager());
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

		lblNewLabel_12 = new Label(composite_1, SWT.NONE);
		lblNewLabel_12.setText(" - ");

		toCalendarCombo = new CalendarCombo(composite_1, SWT.READ_ONLY, mSettings, new DefaultColorManager());
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

		yearBbutton = new Button(group, SWT.NONE);
		yearBbutton.addSelectionListener(new SelectionAdapter()
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
		yearBbutton.setToolTipText("1\uB144\uC804");
		yearBbutton.setText("<");

		yearAbutton = new Button(group, SWT.NONE);
		yearAbutton.addSelectionListener(new SelectionAdapter()
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
		yearAbutton.setToolTipText("1\uB144\uD6C4");
		yearAbutton.setText(">");

		searchButton = new Button(group, SWT.NONE);
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				resultCountLabel.setText("검색결과 : 0");
				grid.removeAll();
				final DataSet ds = new DataSet();
				String eco_kind = allRadioButton.getSelection() ? "ALL" : "";
				eco_kind = vehicleRadioButton.getSelection() ? "V" : eco_kind;
				eco_kind = engineRadioButton.getSelection() ? "E" : eco_kind;
				ds.put("ECO_KIND", eco_kind);
				String productDescription = productCombo.getText();
				if (!productDescription.isEmpty())
				{
					String product = (String) productCombo.getData(productDescription);
					ds.put("PRODUCT", product);
				}
				String groupDescription = deptCombo.getText();
				if (!groupDescription.isEmpty())
				{
					String groupPuid = (String) deptCombo.getData(groupDescription);
					ds.put("GROUP_PUID", groupPuid);
				}
				String userName = userText.getText().trim();
				if (!userName.isEmpty())
				{
					userName = userName.replaceAll("\\*", "%");
					ds.put("USER_NAME", userName);
				}
				String ecoNo = ecoNoText.getText().trim();
				if (!ecoNo.isEmpty())
				{
					ecoNo = ecoNo.replaceAll("\\*", "%");
					ds.put("ECO_NO", ecoNo);
				}
				String projectCode = projectText.getText().trim();
				if (!projectCode.isEmpty())
				{
					projectCode = projectCode.replaceAll("\\*", "%");
					ds.put("PROJECT_CODE", projectCode);
				}
				String newProjectCode = newProjectText.getText().trim();
				if (!newProjectCode.isEmpty())
				{
					newProjectCode = newProjectCode.replaceAll("\\*", "%");
					ds.put("NEW_PROJECT_CODE", newProjectCode);
				}
				String note = noteText.getText().trim();
				if (!note.isEmpty())
				{
					note = note.replaceAll("\\*", "%");
					ds.put("NOTE", note);
				}
				String fromDateString = fromCalendarCombo.getDateAsString();
				if (!fromDateString.isEmpty())
				{
					ds.put("FROM_DATE", fromDateString);
				}
				String toDateString = toCalendarCombo.getDateAsString();
				if (!toDateString.isEmpty())
				{
					ds.put("TO_DATE", toDateString);
				}
				String changeReasonDescription = changeReasonCombo.getText();
				if (!changeReasonDescription.isEmpty())
				{
					String changeReason = (String) changeReasonCombo.getData(changeReasonDescription);
					ds.put("CHANGE_REASON", changeReason);
				}
				String changeReason1Description = changeReason1Combo.getText();
				String changeReason2Description = changeReason2Combo.getText();
				String changeCause = "";
				if (!changeReason1Description.isEmpty())
				{
					changeCause = "'" + changeReason1Combo.getData(changeReason1Description) + "'";
				}
				if (!changeReason2Description.isEmpty())
				{
					changeCause += (changeCause.isEmpty() ? "" : ",") + "'" + changeReason2Combo.getData(changeReason2Description) + "'";
				}
				if (!changeCause.isEmpty())
				{
					ds.put("CHANGE_CAUSE", changeCause);
				}
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
		searchButton.setImage(SWTResourceManager.getImage(CheckListDialog.class, "/icons/find_16.png"));
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
				ExportPOIExcel.exportDialog(shell, grid, "Check List", "Check List", "", null);
			}
		});
		exportExcelButton.setImage(SWTResourceManager.getImage(CheckListDialog.class, "/com/teamcenter/rac/common/images/exceldataset_16.png"));
		exportExcelButton.setText("Excel");

		lblNewLabel_1 = new Label(group, SWT.NONE);
		lblNewLabel_1.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_1.setText("Product");

		productCombo = new Combo(group, SWT.READ_ONLY);
		GridData gd_productCombo = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_productCombo.widthHint = 150;
		productCombo.setLayoutData(gd_productCombo);

		lblNewLabel_5 = new Label(group, SWT.NONE);
		GridData gd_lblNewLabel_5 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_5.horizontalIndent = 30;
		lblNewLabel_5.setLayoutData(gd_lblNewLabel_5);
		lblNewLabel_5.setText("양산 Project");

		projectText = new Text(group, SWT.BORDER);
		projectText.addVerifyListener(new VerifyListener()
		{
			public void verifyText(VerifyEvent event)
			{
				event.text = event.text.toUpperCase();
			}
		});
		projectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblNewLabel_9 = new Label(group, SWT.NONE);
		lblNewLabel_9.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_9.setText("Change Reason");

		changeReasonCombo = new Combo(group, SWT.READ_ONLY);
		changeReasonCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);

		lblNewLabel_2 = new Label(group, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText("Dept.");

		deptCombo = new Combo(group, SWT.READ_ONLY);
		GridData gd_deptCombo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_deptCombo.widthHint = 200;
		deptCombo.setLayoutData(gd_deptCombo);

		lblNewLabel_6 = new Label(group, SWT.NONE);
		lblNewLabel_6.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_6.setText("신규 Project");

		newProjectText = new Text(group, SWT.BORDER);
		newProjectText.addVerifyListener(new VerifyListener()
		{
			public void verifyText(VerifyEvent event)
			{
				event.text = event.text.toUpperCase();
			}
		});
		newProjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblNewLabel_10 = new Label(group, SWT.NONE);
		lblNewLabel_10.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_10.setText("Change Reason 1");

		changeReason1Combo = new Combo(group, SWT.READ_ONLY);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);

		lblNewLabel_3 = new Label(group, SWT.NONE);
		lblNewLabel_3.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_3.setText("Prepared");

		userText = new Text(group, SWT.BORDER);

		lblNewLabel_7 = new Label(group, SWT.NONE);
		lblNewLabel_7.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_7.setText("비고");

		noteText = new Text(group, SWT.BORDER);
		noteText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));

		lblNewLabel_11 = new Label(group, SWT.NONE);
		GridData gd_lblNewLabel_11 = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_11.horizontalIndent = 30;
		lblNewLabel_11.setLayoutData(gd_lblNewLabel_11);
		lblNewLabel_11.setText("Change Reason 2");

		changeReason2Combo = new Combo(group, SWT.READ_ONLY);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);
		new Label(group, SWT.NONE);

		grid = new Grid(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		grid.setHeaderVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));

		gridColumn = new GridColumn(grid, SWT.NONE);
		gridColumn.setAlignment(SWT.CENTER);
		gridColumn.setText("No");
		gridColumn.setWidth(30);

		gridColumn_1 = new GridColumn(grid, SWT.NONE);
		gridColumn_1.setAlignment(SWT.CENTER);
		gridColumn_1.setText("\uC77C\uC790");
		gridColumn_1.setWidth(80);

		gridColumn_2 = new GridColumn(grid, SWT.NONE);
		gridColumn_2.setAlignment(SWT.CENTER);
		gridColumn_2.setText("Product");
		gridColumn_2.setWidth(55);

		gridColumn_3 = new GridColumn(grid, SWT.NONE);
		gridColumn_3.setAlignment(SWT.CENTER);
		gridColumn_3.setText("ECO NO");
		gridColumn_3.setWidth(80);

		gridColumn_14 = new GridColumn(grid, SWT.NONE);
		gridColumn_14.setText("Prepared");
		gridColumn_14.setWidth(70);

		gridColumn_4 = new GridColumn(grid, SWT.NONE);
		gridColumn_4.setAlignment(SWT.CENTER);
		gridColumn_4.setWidth(60);
		gridColumn_4.setText("\uBCC0\uACBD\uC0AC\uC720");

		gridColumn_5 = new GridColumn(grid, SWT.NONE);
		gridColumn_5.setAlignment(SWT.CENTER);
		gridColumn_5.setText("E/Item Group");
		gridColumn_5.setWidth(85);

		gridColumnGroup = new GridColumnGroup(grid, SWT.NONE);
		gridColumnGroup.setText("E/Item \uAC74\uC218");

		gridColumn_6 = new GridColumn(gridColumnGroup, SWT.NONE);
		gridColumn_6.setAlignment(SWT.CENTER);
		gridColumn_6.setWidth(70);
		gridColumn_6.setText("\uBCC0\uACBD\uC0AC\uC7201");

		gridColumn_7 = new GridColumn(gridColumnGroup, SWT.NONE);
		gridColumn_7.setAlignment(SWT.CENTER);
		gridColumn_7.setWidth(70);
		gridColumn_7.setText("\uBCC0\uACBD\uC0AC\uC7202");

		gridColumn_8 = new GridColumn(gridColumnGroup, SWT.NONE);
		gridColumn_8.setAlignment(SWT.CENTER);
		gridColumn_8.setWidth(70);
		gridColumn_8.setText("\uBCC0\uACBD\uC0AC\uC7203");

		gridColumn_9 = new GridColumn(gridColumnGroup, SWT.NONE);
		gridColumn_9.setAlignment(SWT.CENTER);
		gridColumn_9.setWidth(40);
		gridColumn_9.setText("\uACC4");

		gridColumn_10 = new GridColumn(grid, SWT.CENTER);
		gridColumn_10.setWordWrap(true);
		gridColumn_10.setAlignment(SWT.CENTER);
		gridColumn_10.setText("Admin\r\nDesc");
		gridColumn_10.setWidth(80);

		gridColumn_11 = new GridColumn(grid, SWT.NONE);
		gridColumn_11.setText("\uC591\uC0B0 Project");
		gridColumn_11.setWidth(80);

		gridColumn_12 = new GridColumn(grid, SWT.NONE);
		gridColumn_12.setText("\uC2E0\uADDC Project");
		gridColumn_12.setWidth(80);

		gridColumn_13 = new GridColumn(grid, SWT.NONE);
		gridColumn_13.setText("\uBE44\uACE0");
		gridColumn_13.setWidth(250);

		resultCountLabel = new Label(shell, SWT.NONE);
		resultCountLabel.setAlignment(SWT.RIGHT);
		resultCountLabel.setText("검색결과 : 0");
		resultCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		setLOVCombo(productCombo, "S7_VEHICLE_NO", true);
		setLOVCombo(productCombo, "S7_PRODUCT_CODE", false);
		setLOVCombo(changeReasonCombo, "S7_ECO_REASON", true);
		setLOVCombo(changeReason1Combo, "S7_ADMIN_CHECK_CHANGE_CAUSE", true);
		setLOVCombo(changeReason2Combo, "S7_ADMIN_CHECK_CHANGE_CAUSE", true);
		setGroupCombo(deptCombo);
	}

	/**
	 * 검색
	 */
	protected void search(DataSet ds) throws Exception
	{
		ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteUtil.execute("com.ssangyong.service.ECOAdminCheckService", "getCheckList", ds);
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
			final String released_date = StringUtil.nullToString((String) resultRow.get("RELEASED_DATE"));
			final String vehicle_no = StringUtil.nullToString((String) resultRow.get("VEHICLE_NO"));
			final String eco_no = StringUtil.nullToString((String) resultRow.get("ECO_NO"));
			final String owner = StringUtil.nullToString((String) resultRow.get("PUSER_NAME"));
			final String change_reason = StringUtil.nullToString((String) resultRow.get("CHANGE_REASON"));
			final String name_group_cnt = ((BigDecimal) resultRow.get("NAME_GROUP_CNT")).toString();
			final String eitem_cnt1 = ((BigDecimal) resultRow.get("EITEM_CNT1")).toString();
			final String eitem_cnt2 = ((BigDecimal) resultRow.get("EITEM_CNT2")).toString();
			final String eitem_cnt3 = ((BigDecimal) resultRow.get("EITEM_CNT3")).toString();
			final String eitem_cnt = ((BigDecimal) resultRow.get("EITEM_CNT")).toString();
			final String admin_desc = StringUtil.nullToString((String) resultRow.get("ADMIN_DESC"));
			final String regular_project_code = StringUtil.nullToString((String) resultRow.get("REGULAR_PROJECT_CODE"));
			final String new_project_code = StringUtil.nullToString((String) resultRow.get("NEW_PROJECT_CODE"));
			final String note = StringUtil.nullToString((String) resultRow.get("NOTE"));
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					int c = 0;
					GridItem gridItem = new GridItem(grid, SWT.NONE);
					gridItem.setText(c++, no);
					gridItem.setText(c++, released_date);
					gridItem.setText(c++, vehicle_no);
					gridItem.setText(c++, eco_no);
					gridItem.setText(c++, owner);
					gridItem.setText(c++, change_reason);
					gridItem.setText(c++, name_group_cnt);
					gridItem.setText(c++, eitem_cnt1);
					gridItem.setText(c++, eitem_cnt2);
					gridItem.setText(c++, eitem_cnt3);
					gridItem.setText(c++, eitem_cnt);
					gridItem.setText(c++, admin_desc);
					gridItem.setText(c++, regular_project_code);
					gridItem.setText(c++, new_project_code);
					gridItem.setText(c++, note);
				}
			});
		}
	}

	private void setLOVCombo(final Combo combo, String lovName, boolean isRemove)
	{
		if (isRemove)
		{
			combo.removeAll();
		}
		DataSet ds = new DataSet();
		ds.put("LOV_NAME", lovName);
		ArrayList<HashMap> resultList = null;
		try
		{
			resultList = (ArrayList<HashMap>) remoteUtil.execute("com.ssangyong.service.ECOAdminCheckService", "getLOVData", ds);
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
		combo.add("");
		combo.setData("", "");
		for (HashMap resultRow : resultList)
		{
			final String key = (String) resultRow.get("KEY");
			final String desc = (String) resultRow.get("DESCRIPTION");
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					combo.add(desc);
					combo.setData(desc, key);
				}
			});
		}
	}

	private void setGroupCombo(final Combo combo)
	{
		combo.removeAll();
		ArrayList<HashMap> resultList = null;
		try
		{
			resultList = (ArrayList<HashMap>) remoteUtil.execute("com.ssangyong.service.ECOAdminCheckService", "getSYMCSubGroup", null);
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
		combo.add("");
		combo.setData("", "");
		for (HashMap resultRow : resultList)
		{
			final String puid = (String) resultRow.get("PUID");
			final String name = (String) resultRow.get("NAME");
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					combo.add(name);
					combo.setData(name, puid);
				}
			});
		}
	}
}
