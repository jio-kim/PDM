/**
 * 
 */
package com.kgm.admin.ecoadmincheck;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
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
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
 * @author 218583
 * 
 */
public class MonthlyEngineECOAnalysisDialog extends Dialog
{

	protected Object result;
	protected Shell shell;
	private WaitProgressor waitProgressor;
	private Group group;
	private Button searchButton;
	private Button exportExcelButton;
	private Grid grid;
	private GridColumn gridColumn;
	private Label resultCountLabel;
	private Button monthButton;
	private Button dayButton;
	private Label monthLb;
	private Combo yearCombo;
	private Combo monthCombo;
	private Button reasonBtn;
	private Button reasonBtn2;
	private Button reasonBtn3;
	private Label dateLabel;
	private Label dayLb;
	private Composite monthComposite;
	private Composite dayComposite;
	private CalendarCombo fromCalendarCombo;
	private CalendarCombo toCalendarCombo;
	private SashForm sashForm;
	private ArrayList<String> groupNameList;
	private ArrayList<String> changReasons;
	private SYMCRemoteUtil remoteUtil;
	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public MonthlyEngineECOAnalysisDialog(Shell parent)
	{
		super(parent);
		setText("���� ���� ���躯�� �м�(�������1,2,3)"); //########## Title
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
		shell.setSize(1050, 600);
		shell.setText(getText());
		shell.setLayout(new GridLayout());
		group = new Group(shell, SWT.NONE);
		group.setLayout(new GridLayout(8, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		
		DataSet ds = new DataSet();
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		
		monthButton = new Button(group, SWT.RADIO);
		monthButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				dayComposite.setVisible(false);
				monthComposite.setVisible(true);
				sashForm.setWeights(new int[] {1, 0});
			}
		});
		monthButton.setText("\uC6D4\uB2E8\uC704");
		
		dayButton = new Button(group, SWT.RADIO);
		dayButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				monthComposite.setVisible(false);
				dayComposite.setVisible(true);
				sashForm.setWeights(new int[] {0, 1});
			}
		});
		dayButton.setText("\uC77C\uB2E8\uC704");
		
		sashForm = new SashForm(group, SWT.NONE);
		
		monthComposite = new Composite(sashForm, SWT.NONE);
		monthComposite.setLayout(new GridLayout(3, false));
		
		monthLb = new Label(monthComposite, SWT.NONE);
		monthLb.setSize(36, 15);
		monthLb.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		monthLb.setText("Month");
		
		yearCombo = new Combo(monthComposite, SWT.READ_ONLY);
		setYearCombo();
		yearCombo.select(0);
		
		monthCombo = new Combo(monthComposite, SWT.READ_ONLY);
		monthCombo.add("01");
		monthCombo.add("02");
		monthCombo.add("03");
		monthCombo.add("04");
		monthCombo.add("05");
		monthCombo.add("06");
		monthCombo.add("07");
		monthCombo.add("08");
		monthCombo.add("09");
		monthCombo.add("10");
		monthCombo.add("11");
		monthCombo.add("12");
		monthCombo.select(0);
		
		dayComposite = new Composite(sashForm, SWT.NONE);
		dayComposite.setLayout(new GridLayout(4, false));
		
		dayLb = new Label(dayComposite, SWT.NONE);
		dayLb.setText("Release Date");
		
		DefaultSettings mSettings = new DefaultSettings()
		{
			public String getDateFormat()
			{
				return "yyyy-MM-dd";
			}
		};
		fromCalendarCombo = new CalendarCombo(dayComposite, SWT.READ_ONLY, mSettings, new DefaultColorManager());
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
		dateLabel = new Label(dayComposite, SWT.NONE);
		dateLabel.setSize(5, 15);
		dateLabel.setText(" - ");
		
		toCalendarCombo = new CalendarCombo(dayComposite, SWT.READ_ONLY, mSettings, new DefaultColorManager());
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
		sashForm.setWeights(new int[] {188, 283});
		
		reasonBtn = new Button(group, SWT.CHECK);
		reasonBtn.setSelection(true);
		reasonBtn.setText("\uBCC0\uACBD\uC0AC\uC7201");
		
		reasonBtn2 = new Button(group, SWT.CHECK);
		reasonBtn2.setText("\uBCC0\uACBD\uC0AC\uC7202");
		
		reasonBtn3 = new Button(group, SWT.CHECK);
		reasonBtn3.setText("\uBCC0\uACBD\uC0AC\uC7203");
		
		searchButton = new Button(group, SWT.NONE);
		searchButton.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		searchButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				resultCountLabel.setText("�˻���� : 0");
				grid.removeAll();
				String monthString = null;
				String toDateString = null;
				String fromDateString = null;
				changReasons = new ArrayList<String>();
				StringBuffer reason = new StringBuffer();
				final DataSet ds = new DataSet();
				if(reasonBtn.getSelection()){
					changReasons.add("1");
				}
				if(reasonBtn2.getSelection()){
					changReasons.add("2");
				}
				if(reasonBtn3.getSelection()){
					changReasons.add("3");
				}
				if(changReasons.size() == 0){
					MessageBox.post(shell, "��������� ������ �ּ���.", "������� ���Է�", MessageBox.INFORMATION);
					return;
				}
				for(String changReason : changReasons){
					reason.append(",").append(changReason);
				}
				String reasonValue = reason.substring(1);
				ds.put("REASON", reasonValue);

				if(monthButton.getSelection()){
					monthString = yearCombo.getText() + monthCombo.getText();
					if (!monthString.isEmpty())
					{
						ds.put("MONTH", monthString);
					}	
					grid.getColumn(0).setText("������");
				}else{
					fromDateString = fromCalendarCombo.getDateAsString();
					if (!fromDateString.isEmpty())
					{
						ds.put("FROM_DATE", fromDateString);
					}
					toDateString = toCalendarCombo.getDateAsString();
					if (!toDateString.isEmpty())
					{
						ds.put("TO_DATE", toDateString);
					}
					grid.getColumn(0).setText("�ϴ���");
				}
				
				new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						waitProgressor = new WaitProgressor(shell);
						waitProgressor.start();
						waitProgressor.setMessage("�˻���...");
						try
						{
							search(ds);
							waitProgressor.end();
							MessageBox.post(shell, "�˻��� �Ϸ�Ǿ����ϴ�", "�˻�", MessageBox.INFORMATION);
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
		searchButton.setImage(SWTResourceManager.getImage(MonthlyEngineECOAnalysisDialog.class, "/icons/find_16.png"));
		searchButton.setText("�˻�");
		
		exportExcelButton = new Button(group, SWT.NONE);
		exportExcelButton.addSelectionListener(new SelectionAdapter()
		{
			@Override
			public void widgetSelected(SelectionEvent e)
			{
				// shell, ���̺�, sheet name, title, comment, ������ columninf
				StringBuffer reason = new StringBuffer();
				for(String changReason : changReasons){
					reason.append(" ").append(changReason);
				}
				ExportPOIExcel.exportDialog(shell, grid,  "���� ���� ���� ���� �м� " + reason.substring(1) , "���� ���� ���� ���� �м� " + reason.substring(1), "", null);
			}
		});
		exportExcelButton.setImage(SWTResourceManager.getImage(MonthlyEngineECOAnalysisDialog.class, "/com/teamcenter/rac/common/images/exceldataset_16.png"));
		exportExcelButton.setText("Excel");

		grid = new Grid(shell, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		grid.setHeaderVisible(true);
		grid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		GridColumnGroup gridColGroup = new GridColumnGroup(grid, SWT.NONE);
		gridColGroup.setText("����");
		
		GridColumn gridColumn = new GridColumn(gridColGroup, SWT.NONE);
		gridColumn.setText("������");
		gridColumn.setWidth(250);
//		gridColumn.setFrozen(true); ����
		
		GridColumn countColumn = new GridColumn(gridColGroup, SWT.NONE);
		countColumn.setText("�Ǽ�");
		countColumn.setWidth(100);
//		countColumn.setFrozen(true); ����
		
		try
		{
			ArrayList<HashMap> resultList = (ArrayList<HashMap>) remoteQuery.execute("com.kgm.service.ECOAdminCheckService", "getEngineList", ds);
			for(HashMap result : resultList){
				GridColumn engColumn = new GridColumn(grid, SWT.NONE);
				engColumn.setText(String.valueOf(result.get("PLANT_CODE")));
				engColumn.setWidth(70);
			}
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
		
		GridColumn sumColumn = new GridColumn(grid, SWT.NONE);
		sumColumn.setText("SUM");
		sumColumn.setWidth(70);
		
		resultCountLabel = new Label(shell, SWT.NONE);
		resultCountLabel.setAlignment(SWT.RIGHT);
		resultCountLabel.setText("�˻���� : 0");
		resultCountLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	protected void search(DataSet ds) throws Exception
	{
		ArrayList<HashMap> resultList = null;
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		try
		{
			resultList = (ArrayList<HashMap>) remoteQuery.execute("com.kgm.service.ECOAdminCheckService", "getMonthlyEngineECOAnalysis", ds);
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
				resultCountLabel.setText("�˻���� : " + resultSize);
			}
		});
		int count = 1;
		final KeyValueArray sumEcoEngKVArray = new KeyValueArray();
		final KeyValueArray sumEiEngKVArray = new KeyValueArray();
		final KeyValueArray sumEcoGroupKVArray = new KeyValueArray();
		final KeyValueArray sumEiGroupKVArray = new KeyValueArray();
		final KeyValueArray sunCommonEcoKVArray = new KeyValueArray();
		final KeyValueArray sunCommonEcoVehKVArray = new KeyValueArray();
		for (HashMap resultRow : resultList)
		{
			String groupNm = StringUtil.nullToString((String) resultRow.get("GROUP_NAME"));
			String projectCode = StringUtil.nullToString((String) resultRow.get("PROJECT_CODE"));
			int ecoCount = ((BigDecimal) resultRow.get("ECO_SINGLE_CNT")).intValue();
			int endItemCount = ((BigDecimal) resultRow.get("EITEM_CNT")).intValue();
			int commonECOCnt = ((BigDecimal) resultRow.get("ECO_COMMON_CNT")).intValue();
			//���� ���� ECO �հ�..
			if (sumEcoEngKVArray.containsKey(projectCode))
			{
				int ecoSum = (Integer) sumEcoEngKVArray.getValueAtKey(projectCode);
				int sum = ecoCount + ecoSum;
				sumEcoEngKVArray.updateValue(projectCode, sum);
			} else
			{
				sumEcoEngKVArray.put(projectCode, ecoCount);
			}
			//���� ���� End Item �հ�..
			if (sumEiEngKVArray.containsKey(projectCode))
			{
				int eiSum = (Integer) sumEiEngKVArray.getValueAtKey(projectCode);
				int sum = endItemCount + eiSum;
				sumEiEngKVArray.updateValue(projectCode, sum);
			} else
			{
				sumEiEngKVArray.put(projectCode, endItemCount);
			}
			//group ���� ECO �հ�..
			if (sumEcoGroupKVArray.containsKey(groupNm))
			{
				int ecoSum = (Integer) sumEcoGroupKVArray.getValueAtKey(groupNm);
				int sum = ecoCount + ecoSum;
				sumEcoGroupKVArray.updateValue(groupNm, sum);
			} else
			{
				sumEcoGroupKVArray.put(groupNm, ecoCount);
			}
			//group ���� End Item �հ�..
			if (sumEiGroupKVArray.containsKey(groupNm))
			{
				int eiSum = (Integer) sumEiGroupKVArray.getValueAtKey(groupNm);
				int sum = endItemCount + eiSum;
				sumEiGroupKVArray.updateValue(groupNm, sum);
			} else
			{
				sumEiGroupKVArray.put(groupNm, endItemCount);
			}
			//group ���� Common ECO �հ�..
			if (sunCommonEcoKVArray.containsKey(groupNm))
			{
				int ecoSum = (Integer) sunCommonEcoKVArray.getValueAtKey(groupNm);
				int sum = commonECOCnt + ecoSum;
				sunCommonEcoKVArray.updateValue(groupNm, sum);
			} else
			{
				sunCommonEcoKVArray.put(groupNm, commonECOCnt);
			}
			//���� ���� Common ECO �հ�..
			if (sunCommonEcoVehKVArray.containsKey(projectCode))
			{
				int ecoSum = (Integer) sunCommonEcoVehKVArray.getValueAtKey(projectCode);
				int sum = commonECOCnt + ecoSum;
				sunCommonEcoVehKVArray.updateValue(projectCode, sum);
			} else
			{
				sunCommonEcoVehKVArray.put(projectCode, commonECOCnt);
			}
		}
		groupNameList = new ArrayList<String>();
//		final ArrayList<HashMap<String, String>> projectLists = new ArrayList<HashMap<String, String>>();
		for (HashMap resultRow : resultList)
		{
			waitProgressor.setMessage("�˻� ������ ���̺� �ε���...(" + count + "/" + resultList.size() + ")");		
			final String groupName = StringUtil.nullToString((String)(resultRow.get("GROUP_NAME")));
				//...
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					if(!groupNameList.contains(groupName)){
						groupNameList.add(groupName);
					}
				}
			});
		}
		
		Display.getDefault().syncExec(new Runnable()
		{
			@Override
			public void run()
			{
				//�׷캰 gridItem ����
				Collections.sort(groupNameList);
				for(int i = 0; i < groupNameList.size(); i++){
					GridItem ecoItem = new GridItem(grid, SWT.NONE);		
					ecoItem.setText(0, groupNameList.get(i));
					ecoItem.setText(1, "ECO �Ǽ�");
					GridItem eitemItem = new GridItem(grid, SWT.NONE);		
					eitemItem.setText(1, "E/ITEM �Ǽ�");
					if(i%2==0){
						for(int j = 0; j < grid.getColumnCount(); j++){
							ecoItem.setBackground(j, SWTResourceManager.getColor(224, 255, 255));
							eitemItem.setBackground(j, SWTResourceManager.getColor(224, 255, 255));
						}
					}
				}
				GridItem totalCount = new GridItem(grid, SWT.None);
				totalCount.setText(0, "�Ѱ�");
				totalCount.setText(1, "ECO �Ǽ�");
				GridItem eItemTotal = new GridItem(grid, SWT.None);
				eItemTotal.setText(1, "E/ITEM �Ǽ�");
				for(int i = 0; i < grid.getColumnCount(); i++){
					totalCount.setBackground(i, SWTResourceManager.getColor(224, 224, 224));
					eItemTotal.setBackground(i, SWTResourceManager.getColor(224, 224, 224));
				}
			}
		});
		
		for (HashMap resultRow : resultList)
		{
			waitProgressor.setMessage("�˻� ������ ���̺� �ε���...(" + count + "/" + resultList.size() + ")");		
			final String groupName = StringUtil.nullToString((String)(resultRow.get("GROUP_NAME")));
			final String projectCode = StringUtil.nullToString((String)(resultRow.get("PROJECT_CODE")));
			final String singleCnt = StringUtil.nullToString(String.valueOf(resultRow.get("ECO_SINGLE_CNT")));
			final String commonCnt = StringUtil.nullToString(String.valueOf(resultRow.get("ECO_COMMON_CNT")));
			final String eitemCnt = StringUtil.nullToString(String.valueOf(resultRow.get("EITEM_CNT")));
				//...
			Display.getDefault().syncExec(new Runnable()
			{
				@Override
				public void run()
				{
					//�÷���� �׷���� ã�Ƽ� ī��Ʈ �Է�
					GridColumn[] gridColumns = grid.getColumns();
					GridItem[] gridItems = grid.getItems();
					for(int i = 0; i < gridColumns.length; i++){
						if(gridColumns[i].getText().equals(projectCode)){
							for(int j = 0; j < gridItems.length; j++){
								if(j%2 == 0){
									if(gridItems[j].getText(0).equals(groupName)){
										if(commonCnt.equals("0")){
											//eco ī��Ʈ
											gridItems[j].setText(i, Integer.valueOf(singleCnt) == 0 ? "" : singleCnt);
										}else{
											//���� ECO ī��Ʈ
											String ecoCnt = Integer.valueOf(singleCnt) == 0 ? "" : singleCnt;
											String commonConut = ecoCnt + "'(" + commonCnt + ")";
											gridItems[j].setText(i, commonConut);
										}
									}else if(gridItems[j].getText(0).equals("�Ѱ�")){
										//�Ѱ� ECO ī��Ʈ
										String comCnt = String.valueOf(sunCommonEcoVehKVArray.getValueAtKey(gridColumns[i].getText()));
										if(comCnt.equals("0")){
											String ecoTotalCount = String.valueOf(sumEcoEngKVArray.getValueAtKey(gridColumns[i].getText()));
											gridItems[j].setText(i, Integer.valueOf(ecoTotalCount) == 0 ? "" : ecoTotalCount);
										}else{ 
											//���� ECO ī��Ʈ �Ѱ�
											String totalCount = String.valueOf(sumEcoEngKVArray.getValueAtKey(gridColumns[i].getText())); 
											String ecoCnt = Integer.valueOf(totalCount) == 0 ? "" : totalCount;
											String comTotalCnt = ecoCnt + "'(" + comCnt + ")";
											gridItems[j].setText(i, comTotalCnt);
										}
									}
								}else{
									if(gridItems[j-1].getText(0).equals(groupName)){
										//E/ITEM ī��Ʈ 
										gridItems[j].setText(i, Integer.valueOf(eitemCnt) == 0 ? "" : eitemCnt);
									}else if(gridItems[j-1].getText(0).equals("�Ѱ�")){
										//E/ITEM ī��Ʈ �Ѱ� 
										String eiTotalCount = String.valueOf(sumEiEngKVArray.getValueAtKey(gridColumns[i].getText()));
										gridItems[j].setText(i, Integer.valueOf(eiTotalCount) == 0 ? "" : eiTotalCount);
									}
								}
							}
						}else if(gridColumns[i].getText().equals("SUM")){
							int sumECOCount = 0;
							int sumCommonCount = 0;
							int sumEItemCount = 0;
							for(int j = 0; j < gridItems.length; j++){
								Integer totalCommonCnt = (Integer) sunCommonEcoKVArray.getValueAtKey(gridItems[j].getText(0));
								if(j%2 == 0){
									if(totalCommonCnt == null || totalCommonCnt == 0){
										Integer ecoCount = (Integer)sumEcoGroupKVArray.getValueAtKey(gridItems[j].getText(0));
										if(ecoCount != null){
											gridItems[j].setText(grid.getColumnCount() - 1, ecoCount == 0 ? "" : String.valueOf(ecoCount));
											sumECOCount += ecoCount;
										}
									}else{
										Integer ecoCount = (Integer) sumEcoGroupKVArray.getValueAtKey(gridItems[j].getText(0));
										String ecoCnt = ecoCount == 0 ? "" : String.valueOf(ecoCount);
										String commonConut = ecoCnt + "'(" + String.valueOf(totalCommonCnt) + ")";
										gridItems[j].setText(grid.getColumnCount() - 1, commonConut);
										sumECOCount += ecoCount;
										sumCommonCount += totalCommonCnt;
									}
								}else{
									Integer eiCount = (Integer)sumEiGroupKVArray.getValueAtKey(gridItems[j-1].getText(0));
									if(eiCount != null){
										gridItems[j].setText(grid.getColumnCount() - 1, eiCount == 0 ? "" : String.valueOf(eiCount));
										sumEItemCount += eiCount;
									}
								}
							}
							if(sumCommonCount == 0){
								grid.getItem(grid.getItemCount()-2).setText(grid.getColumnCount() - 1, Integer.valueOf(sumECOCount) == 0 ? "" : String.valueOf(sumECOCount));
							}else{
								grid.getItem(grid.getItemCount()-2).setText(grid.getColumnCount() - 1, Integer.valueOf(sumECOCount) == 0 ? "" : String.valueOf(sumECOCount) + "'(" + sumCommonCount + ")");
							}
							grid.getItem(grid.getItemCount()-1).setText(grid.getColumnCount() - 1, Integer.valueOf(sumEItemCount) == 0 ? "" : String.valueOf(sumEItemCount));
						}
					}
				}
			});
		}
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
