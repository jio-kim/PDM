package com.ssangyong.commands.vpm.report;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.commands.ec.SYMCECConstant;
import com.ssangyong.commands.ec.search.SearchUserDialog;
import com.ssangyong.commands.vpm.report.dao.CustomReportDao;
import com.ssangyong.commands.vpm.report.utils.ExcelUtilForReport;
import com.ssangyong.common.SYMCDateTimeButton;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.dialog.SYMCAbstractDialog;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.ui.services.NavigatorOpenService;
import com.teamcenter.rac.util.MessageBox;

public class VPMReportDialog extends SYMCAbstractDialog {

    private CustomReportDao dao;
    
    private Text ecoNo, cPno;  
    
    private Label countLabel;
    
    private Combo validCombo, notInformedCombo, incompleteWorkCombo;
    
    private SYMCDateTimeButton searchDateFrom, searchDateTo;    

    private Button searchButton, setWorkerButton, noticeProcessButton, completeProcessButton, unApplyProcessButton, excelExportButton;    

    private Table resultTable;
    
    private String[] columnName = new String[] { "ECO", "Type", "모품번 Part Origin",  "모품번", "모품번 Revision", "자품번 Part Origin", "자품번", "자품번 Revision", "유효/비유효", "ECO 담당자", "ECO 담당부서", "작업자", "작업부서", "통보날짜", "작업완료일", "미반영(SKIP)" };
    
    private int[] columnSize = new int[] { 80, 40, 80, 110, 80, 80, 110, 80, 80, 80, 160, 120, 120, 100, 100, 100 };
    
    private static final String[] VALID_COMBO_MSG = new String[] { "ALL", "Y(유효)", "N(비유효)" };
    
    private static final String[] VALID_COMBO_CODE = new String[] { "ALL", "Y", "N" };

    private Button closeButton;    
    
    private WaitProgressBar waitProgress;

    public VPMReportDialog(Shell parent, int _selection) {
        super(parent, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM | _selection);
    }

    @Override
    protected boolean apply() {
        return false;
    }

    /** 버튼 변경 */
    protected void createButtonsForButtonBar(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(2, true));
        excelExportButton = new Button(composite, SWT.PUSH);
        excelExportButton.setText("Excel Export");
        excelExportButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                try {
                    ExcelUtilForReport.exportDataXLS(getShell(), resultTable, "VPM Report", 0);                    
                } catch(Exception ex) {
                    ex.printStackTrace();
                    MessageBox.post(getShell(), "Excel Export Error", "ERROR", MessageBox.ERROR);
                }
            }
        });
        closeButton = new Button(composite, SWT.PUSH);
        closeButton.setText("Close");
        closeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                getShell().close();
            }
        });
    }

    /** Composiste 생성 */
    @Override
    protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
        dao = new CustomReportDao();
        getShell().setText("Monitoring VPM");
        Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
        composite.setLayout(new GridLayout());

        createSearchComposite(composite);
        cteateSearchResultTable(composite);        
        return composite;
    }

    /** 검색 조건 Composiste 생성 */
    private void createSearchComposite(Composite paramComposite) {
        Composite composite = new Composite(paramComposite, SWT.NONE);
        GridLayout layout = new GridLayout(10, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(gridData);

        // #1        
        makeLabel(composite, "ECO NO.", 110);
        ecoNo = new Text(composite, SWT.BORDER);
        gridData = new GridData(120, SWT.DEFAULT);
        ecoNo.setLayoutData(gridData);
        ecoNo.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                case SWT.CR:
                    search();
                    break;
                default:
                    // ignore everything else
                }
            }
        });
     
        makeLabel(composite, "From : ", 110);
        searchDateFrom = new SYMCDateTimeButton(composite);
        gridData = new GridData();        
        searchDateFrom.setLayoutData(gridData);
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.add(Calendar.MONTH, -3); // 3개월 전으로 설정
        searchDateFrom.setDate(fromCalendar.get(Calendar.YEAR), fromCalendar.get(Calendar.MONTH), fromCalendar.get(Calendar.DAY_OF_MONTH));

        makeLabel(composite, "To : ", 110);
        searchDateTo = new SYMCDateTimeButton(composite);
        gridData = new GridData();        
        searchDateTo.setLayoutData(gridData);
        Calendar toCalendar = Calendar.getInstance();   // 오늘 날자 설정
        searchDateTo.setDate(toCalendar.get(Calendar.YEAR), toCalendar.get(Calendar.MONTH), toCalendar.get(Calendar.DAY_OF_MONTH));

        Label label = new Label(composite, SWT.RIGHT);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 3;
        label.setLayoutData(gridData);

        // 검색 버튼
        searchButton = new Button(composite, SWT.PUSH);
        searchButton.setText("Search");
        searchButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                search();
            }
        });

        // #2        
        makeLabel(composite, "자품번", 110);
        cPno = new Text(composite, SWT.BORDER);
        gridData = new GridData(120, SWT.DEFAULT);
        cPno.setLayoutData(gridData);
        cPno.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                switch (e.keyCode) {
                case SWT.CR:
                    search();
                    break;
                default:
                    // ignore everything else
                }
            }
        });
        
        makeLabel(composite, "유효/비유효", 110);
        validCombo = new Combo(composite, SWT.READ_ONLY);
        validCombo.setItems(VALID_COMBO_MSG);
        validCombo.select(1);
        gridData = new GridData(120, SWT.DEFAULT);
        validCombo.setLayoutData(gridData);
        validCombo.setEnabled(false);
        
        makeLabel(composite, "통보", 110);
        notInformedCombo = new Combo(composite, SWT.READ_ONLY);
        notInformedCombo.setItems(new String[] { "ALL", "Y", "N" });
        notInformedCombo.select(0);
        gridData = new GridData(120, SWT.DEFAULT);
        notInformedCombo.setLayoutData(gridData);        

        makeLabel(composite, "작업미완료", 110);
        incompleteWorkCombo = new Combo(composite, SWT.READ_ONLY);
        incompleteWorkCombo.setItems(new String[] { "ALL", "Y", "N" });
        incompleteWorkCombo.select(0);
        gridData = new GridData(120, SWT.DEFAULT);
        incompleteWorkCombo.setLayoutData(gridData);
        
        Label label7 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 10;
        label7.setLayoutData(gridData);

        Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 10;
        lSeparator.setLayoutData(gridData);
        
        Composite buttonComposite1 = new Composite(composite, SWT.NONE);      
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 10;
        buttonComposite1.setLayoutData(gridData);
        layout = new GridLayout(15, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        buttonComposite1.setLayout(layout);
        
        
        countLabel = new Label(buttonComposite1, SWT.RIGHT);
        countLabel.setText("[총 : - 건]");
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        countLabel.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);       
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(gridData);      
        
        setWorkerButton = new Button(buttonComposite1, SWT.PUSH);
        setWorkerButton.setText("작업자지정");
        setWorkerButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	
        		TCComponentGroupMember addMember = null;
        		SearchUserDialog searchDialog = new SearchUserDialog(getShell());
        		int returnInt = searchDialog.open();
        		if(returnInt == 0){
        			addMember = searchDialog.getSelectedMember();
        			
        			TableItem[] items = resultTable.getSelection();
        			if( items == null || items.length == 0)
        				return;
        			
        			String[] selectedItem = null;
        			try {
        			    waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
        		        waitProgress.start();
        		        waitProgress.setStatus("updating.. ");
        		        
        				selectedItem = addMember.getProperties(new String[]{"group", "the_user"});
        				ArrayList<DataSet> updateCustomList = new ArrayList<DataSet>();
        				
        				for( int i = 0 ; i < items.length ; i++ )
        				{
        				    String tcUser = StringUtil.nullToString(selectedItem[1]+"("+addMember.getUserId()+")");
        				    String tcDept = StringUtil.nullToString(selectedItem[0]);
        					items[i].setText(11, tcUser);
        					items[i].setText(12, tcDept);
        					DataSet updateData = new DataSet();
        					updateData.put("TC_USER", tcUser);
        					updateData.put("TC_DEPT", tcDept);
        					updateData.put("GUID", items[i].getData("GUID"));
        					updateCustomList.add(updateData);
        				}
        				dao.updateVPMCustomSetWorker(updateCustomList); 				
        			} catch (Exception ex) {
        				ex.printStackTrace();
        				MessageBox.post(getShell(), "Update Error", "ERROR", MessageBox.ERROR);
        			} finally {
        			    waitProgress.close();
        			    search();
        			}
        			
        		}
        		
            	
            }
        });
        gridData = new GridData(80, SWT.DEFAULT);
        gridData.horizontalSpan = 1;
        setWorkerButton.setLayoutData(gridData);
        
        label = new Label(buttonComposite1, SWT.RIGHT);        
        gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
        gridData.horizontalSpan = 1;
        label.setLayoutData(gridData);
        
        noticeProcessButton = new Button(buttonComposite1, SWT.PUSH);
        noticeProcessButton.setText("통보처리");
        noticeProcessButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
               
    			TableItem[] items = resultTable.getSelection();
    			if( items == null || items.length == 0)
    				return;
    			
    			ArrayList<DataSet> updateCustomList = new ArrayList<DataSet>();
				try {
				    waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
                    waitProgress.start();
                    waitProgress.setStatus("updating.. ");
				    for( int i = 0 ; i < items.length ; i++ )
    				{
    					Date currentDate = null;
    					DataSet updateData = new DataSet();               
    					if( CustomUtil.isEmpty(items[i].getText(13)) )
    					{
    						currentDate = Calendar.getInstance().getTime();
    				        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    				        String stringReleasDate = sdf.format(currentDate);
    						
    						items[i].setText(13, stringReleasDate);
    						updateData.put("INFORM_DATE", "Y");
    					}
    					else
    					{
    						items[i].setText(13, "");
    						updateData.put("INFORM_DATE", "N");
    					}
    					
    					// currentDate 로 Update
    					updateData.put("GUID", items[i].getData("GUID"));
                        updateCustomList.add(updateData);					
    				}
				
				    dao.updateVPMCustomNoticeProcess(updateCustomList);				    
				} catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.post(getShell(), "Update Error", "ERROR", MessageBox.ERROR);
                } finally {
                    waitProgress.close();
                    search();
                }
            	
            }
        });
        gridData = new GridData(80, SWT.DEFAULT);
        gridData.horizontalSpan = 1;
        noticeProcessButton.setLayoutData(gridData);
        
        completeProcessButton = new Button(buttonComposite1, SWT.PUSH);
        completeProcessButton.setText("완료처리");
        completeProcessButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
               
    			TableItem[] items = resultTable.getSelection();
    			if( items == null || items.length == 0)
    				return;
    			
    			ArrayList<DataSet> updateCustomList = new ArrayList<DataSet>();
    			try {
    			    waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
                    waitProgress.start();
                    waitProgress.setStatus("updating.. ");
        			for( int i = 0 ; i < items.length ; i++ )
    				{
    					Date currentDate = null;
    					DataSet updateData = new DataSet(); 
    					if( CustomUtil.isEmpty(items[i].getText(14)) )
    					{
    						currentDate = Calendar.getInstance().getTime();
    				        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    				        String stringReleasDate = sdf.format(currentDate);
    						
    						items[i].setText(14, stringReleasDate);
    						updateData.put("END_DATE", "Y");
    					}
    					else
    					{
    						items[i].setText(14, "");
    						updateData.put("END_DATE", "N");
    					}
    					
    					// currentDate 로 Update
    					updateData.put("GUID", items[i].getData("GUID"));
                        updateCustomList.add(updateData);
    					
    				}
    				dao.updateVPMCustomCompleteProcess(updateCustomList);                   
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.post(getShell(), "Update Error", "ERROR", MessageBox.ERROR);
                } finally {
                    waitProgress.close();
                    search();
                }
            	
            }
        });
        gridData = new GridData(80, SWT.DEFAULT);
        gridData.horizontalSpan = 1;
        completeProcessButton.setLayoutData(gridData);
        
        unApplyProcessButton = new Button(buttonComposite1, SWT.PUSH);
        unApplyProcessButton.setText("미반영(SKIP)");
        unApplyProcessButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
               
    			TableItem[] items = resultTable.getSelection();
    			if( items == null || items.length == 0)
    				return;
    			
    			ArrayList<DataSet> updateCustomList = new ArrayList<DataSet>();
    			 try {
        			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
                    waitProgress.start();
                    waitProgress.setStatus("updating.. ");               
                    for( int i = 0 ; i < items.length ; i++ )
    				{
    				    DataSet updateData = new DataSet(); 
    				    String status = "";
    					if( CustomUtil.isEmpty(items[i].getText(15)) )
    					{
    						status = "Y";
    						items[i].setText(15, status);
    					}
    					else
    					{
    						items[i].setText(15, "");
    					}
    					
    					// status 로 Update
    					updateData.put("USER_SKIP", status);
    					updateData.put("GUID", items[i].getData("GUID"));
                        updateCustomList.add(updateData);
    					
    				}				
                    dao.updateVPMCustomUserSkip(updateCustomList);                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    MessageBox.post(getShell(), "Update Error", "ERROR", MessageBox.ERROR);
                } finally {
                    waitProgress.close();
                    search();
                }
            	
            }
        });
        gridData = new GridData(80, SWT.DEFAULT);
        gridData.horizontalSpan = 1;
        unApplyProcessButton.setLayoutData(gridData);

    }

    /** 검색 결과 테이블 생성 */
    private void cteateSearchResultTable(Composite paramComposite) {
        Composite composite = new Composite(paramComposite, SWT.NONE);
        composite.setLayout(new GridLayout());
        GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
        composite.setLayoutData(layoutData);

        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        layoutData.minimumHeight = 400;
        layoutData.horizontalSpan = 3;
        resultTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
        resultTable.setHeaderVisible(true);
        resultTable.setLinesVisible(true);
        resultTable.setLayoutData(layoutData);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] selectItems = resultTable.getSelection();
				try {
					TCComponentItem item = CustomUtil.findItem(SYMCECConstant.ECOTYPE, (String) selectItems[0].getText(3));
					if(item != null){
					    NavigatorOpenService openService = new NavigatorOpenService();
					    openService.open(item);
					    getShell().close();
					}
				} catch (TCException e1) {
					e1.printStackTrace();
				}
			}
		});

        int i = 0;
        for (String value : columnName) {
            TableColumn column = new TableColumn(resultTable, SWT.NONE);
            column.setText(value);
            column.setWidth(columnSize[i]);
            i++;
        }
    }

    private void makeLabel(Composite paramComposite, String lblName, int lblSize) {
        GridData layoutData = new GridData(lblSize, SWT.DEFAULT);

        Label label = new Label(paramComposite, SWT.RIGHT);
        label.setText(lblName);
        label.setLayoutData(layoutData);
    }

    @SuppressWarnings({ "rawtypes" })
    private void search() {
       DataSet searchCondition = new DataSet();
        String eco_no = ecoNo.getText();
        if (eco_no != null && eco_no.length() > 0) {
            eco_no = eco_no.replace("*", "%");
            searchCondition.put("ECO_NO", eco_no.toUpperCase() + '%');
        }
        String childNo = cPno.getText();
        if (childNo != null && childNo.length() > 0) {
            childNo = childNo.replace("*", "%");
            searchCondition.put("CPNO", '%' + childNo.toUpperCase() + '%');
        }
        String searchDateFromDate = searchDateFrom.getYear() + "-" + String.format("%1$02d", (searchDateFrom.getMonth() + 1)) + "-" + String.format("%1$02d", searchDateFrom.getDay()) + "";        
        searchCondition.put("FROM_DATE", searchDateFromDate);
        String searchDateToDate = searchDateTo.getYear() + "-" + String.format("%1$02d", (searchDateTo.getMonth() + 1)) + "-" + String.format("%1$02d", searchDateTo.getDay()) + "";        
        searchCondition.put("TO_DATE", searchDateToDate);        
        searchCondition.put("NOT_INFORMED", ("ALL".equals(notInformedCombo.getItem(notInformedCombo.getSelectionIndex()))) ? null : notInformedCombo.getItem(notInformedCombo.getSelectionIndex()));
        searchCondition.put("INCOMPLETE_WORK", ("ALL".equals(incompleteWorkCombo.getItem(incompleteWorkCombo.getSelectionIndex()))) ? null : incompleteWorkCombo.getItem(incompleteWorkCombo.getSelectionIndex()));
        searchCondition.put("IS_VALID", ("ALL".equals(this.getValidCode(validCombo.getSelectionIndex()))) ? null : this.getValidCode(validCombo.getSelectionIndex()));  
        
        waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
        waitProgress.start();
        waitProgress.setStatus("loading.. ");
        // Label 카운트 설정
        countLabel.setText("[총 : - 건]");
        try {
            dao = new CustomReportDao();            
            ArrayList<HashMap> resultList = dao.getValidateVPMList(searchCondition);       
            resultTable.removeAll();
            String rowKey = "";
            Color rowColor = null;      
            int rowCount = 0;
            for (int i = 0; i < resultList.size(); i++) {
                HashMap data = resultList.get(i);           
                String key = StringUtil.nullToString((String)data.get("GROUP_KEY"));
                // key가 다르면 다른 Row 색깔을 변경
                if(!rowKey.equals(key)) {
                    rowCount = rowCount + 1;
                    if(rowColor == null || rowColor.getRGB().toString().equals(SWTResourceManager.getColor(SWT.COLOR_GRAY).getRGB().toString())) {
                        rowColor = SWTResourceManager.getColor(SWT.COLOR_WHITE);
                    } else {
                        rowColor = SWTResourceManager.getColor(SWT.COLOR_GRAY);
                    }                
                    rowKey = key;
                }
                //{ "ECO", "Type", "모품번 PART ORIGIN",  "모품번", "모품번 Revision", "자품번 PART ORIGIN", "자품번", "자품번 Revision","ECO 담당자", "ECO 담당부서", "TC 작업자", "작업부서", "통보날짜", "작업완료일", "적용 미반영" };
                TableItem item = new TableItem(resultTable, SWT.NONE);
                item.setBackground(rowColor);
                item.setText(0, StringUtil.nullToString((String)data.get("ECO_NO")));
                item.setText(1, StringUtil.nullToString((String)data.get("CHANGE_DIV")));
                item.setText(2, StringUtil.nullToString((String)data.get("MPNO_PART_TYPE")));
                item.setText(3, StringUtil.nullToString((String)data.get("MPNO")));
                item.setText(4, StringUtil.nullToString((String)data.get("MPNO_VER")));
                item.setText(5, StringUtil.nullToString((String)data.get("CPNO_PART_TYPE")));
                item.setText(6, StringUtil.nullToString((String)data.get("CPNO")));
                item.setText(7, StringUtil.nullToString((String)data.get("CPNO_VER")));                   
                item.setText(8, StringUtil.nullToString((String)data.get("IS_VALID")));

                item.setText(9, StringUtil.nullToString((String)data.get("ECO_USER")));
                item.setText(10, StringUtil.nullToString((String)data.get("ECO_DEPT")));
                item.setText(11, StringUtil.nullToString((String)data.get("TC_USER")));
                item.setText(12, StringUtil.nullToString((String)data.get("TC_DEPT")));
                item.setText(13, StringUtil.nullToString((String)data.get("INFORM_DATE_CHAR")));
                item.setText(14, StringUtil.nullToString((String)data.get("END_DATE_CHAR")));
                item.setText(15, StringUtil.nullToString((String)data.get("USER_SKIP")));
                item.setData("GROUP_KEY", StringUtil.nullToString((String)data.get("GROUP_KEY")));
                item.setData("GUID", StringUtil.nullToString((String)data.get("GUID")));
            }
            // Label 카운트 설정
            countLabel.setText("[총 : " + rowCount + " 건]");           
        } catch (Exception e) {            
            e.printStackTrace();
            MessageBox.post(getShell(), "Search Error", "ERROR", MessageBox.ERROR);
        } finally {
            waitProgress.close();
        }

    }

    @Override
    protected boolean validationCheck() {
        return true;
    } 
    
    /**
     * 
     * Validate Code Index로 Validate Code를 가져온다.
     * 
     * @method getValidCode 
     * @date 2013. 5. 31.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getValidCode(int codeNo) {
        return VALID_COMBO_CODE[codeNo];        
    }  
}