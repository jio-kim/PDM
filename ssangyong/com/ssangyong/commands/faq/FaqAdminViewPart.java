package com.ssangyong.commands.faq;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

import com.ssangyong.common.remote.DataSet;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 * [NON-SR][ymjang] 유형 항목 변경 --> 설계, 기관으로
 */
public class FaqAdminViewPart extends ViewPart {

    private FaqQueryService faqQueryService;
    private AddfileQueryService addfileQueryService;
    private FaqAdminView faqAdminView;
    private FaqView faqView;

    private Shell parentShell;

    private Table table;

    private List<String> columnInfoList;

    public FaqAdminViewPart() {
        parentShell = Display.getCurrent().getActiveShell();
        if (parentShell == null) {
            parentShell = Display.getCurrent().getShells()[0];
        }

        faqQueryService = new FaqQueryService();
        addfileQueryService = new AddfileQueryService();
        faqAdminView = new FaqAdminView(faqQueryService, addfileQueryService, this, parentShell);
        faqView = new FaqView(parentShell);
    }

    @Override
    public void createPartControl(Composite paramComposite) {
        initUI(paramComposite);
    }

    @Override
    public void setFocus() {

    }

    public void initUI(Composite paramComposite) {
        setTitleImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.FAQ_IMAGE_PATH));

        Composite composite = new Composite(paramComposite, SWT.NONE);
        composite.setLayout(new BorderLayout(0, 0));

        Composite north_composite = new Composite(composite, SWT.NONE);
        north_composite.setLayoutData(BorderLayout.NORTH);
        north_composite.setLayout(new GridLayout(1, false));

        ToolBar toolBar = new ToolBar(north_composite, SWT.FLAT | SWT.RIGHT);

        ToolItem addToolItem = new ToolItem(toolBar, SWT.NONE);
        addToolItem.setWidth(30);
        addToolItem.setToolTipText("Create");
        addToolItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.ADD_IMAGE_PATH));
        addToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                faqAdminView.createFaqView();
            }
        });

        ToolItem editToolItem = new ToolItem(toolBar, SWT.NONE);
        editToolItem.setWidth(30);
        editToolItem.setToolTipText("Edit");
        editToolItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.EDIT_IMAGE_PATH));
        editToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (table.getSelectionIndex() == -1) {
                    MessageDialog.openInformation(parentShell, "Information", "Please select row for modification.");
                    return;
                }

                faqAdminView.updateFaqView(table.getSelection()[0]);
            }
        });

        ToolItem deleteToolItem = new ToolItem(toolBar, SWT.NONE);
        deleteToolItem.setWidth(30);
        deleteToolItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.DELETE_IMAGE_PATH));
        deleteToolItem.setToolTipText("Delete");
        deleteToolItem.addSelectionListener(new SelectionAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (table.getSelectionIndex() == -1) {
                    MessageDialog.openInformation(parentShell, "Information", "Please select row for delete.");
                    return;
                }

                if (MessageDialog.openConfirm(parentShell, "Confirm", "Would you really deleted?")) {

					try {
						
                		TableItem tableItem = table.getSelection()[0];
                		
	                    String ouid = (String) tableItem.getData("faq_puid");
	                    ArrayList<HashMap<String, Object>> addfileList = (ArrayList<HashMap<String, Object>>) tableItem.getData("addfileList");

	                    DataSet dataSet = new DataSet();
	                    dataSet.put("faq_puid", ouid);
	                    dataSet.put("addfile_puid", ouid);
	                    
	                    /*
	                    HashMap<String, Object> dataMap = null;
	                    for (int i = 0; i < addfileList.size(); i++) { 
	                    	String DOC_ID = addfileList.get(i).get("DOC_ID").toString();
							TCComponentItem deleteItem = CustomUtil.findItem("Document", DOC_ID);
							
							if (deleteItem != null)
							{
			                    dataMap = new HashMap<String, Object>();
								dataMap.put("deleteItem", deleteItem);
								
								DeleteDocOperation deleteOperation = new DeleteDocOperation(dataMap);
								deleteOperation.executeOperation();
							}
	                    }
						*/
	                    
	                    if (faqQueryService.deleteFaq(dataSet)) {
	                        MessageDialog.openInformation(parentShell, "Information", "Guide Manual is deleted.");
	                        refreshTable();
	                    }

					} catch (Exception e1) {
						e1.printStackTrace();
						MessageDialog.openError(parentShell, "Error", e1.getMessage() == null ? e1.toString() : e1.getMessage());
					}
                }
            }
        });

        ToolItem refreshToolItem = new ToolItem(toolBar, SWT.NONE);
        refreshToolItem.setToolTipText("Refresh");
        refreshToolItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.REFRESH_IMAGE_PATH));
        refreshToolItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                refreshTable();
            }
        });

        // 위로
        ToolItem upItem = new ToolItem(toolBar, SWT.NONE);
        upItem.setToolTipText("Up");
        upItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.ARROWUP_IMAGE_PATH));
        upItem.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                final List<HashMap<String, Object>> newList = getCurrentTableList();

                int selecttionCount = table.getSelectionCount();
                if (selecttionCount == 0)
                    return;

                TableItem[] selectedItems = table.getSelection();
                final List<HashMap<String, Object>> tempActivityList = new ArrayList<HashMap<String, Object>>();
                int[] selectedIdxs = new int[selectedItems.length];

                // NewActivity List에서 선택한 대상을 임시 List에 담기
                for (int i = 0; i < selectedItems.length; i++) {
                	int dataMapIndex = (Integer) selectedItems[i].getData("dataMapIndex");
                    tempActivityList.add(newList.get(dataMapIndex));
                    selectedIdxs[i] = dataMapIndex;
                }

                // 더이상 위가 없는 경우 처리 안함
                if (selectedIdxs[0] == 0) {
                    return;
                }

                // NewActivity List에서 제거
                for (int j = selectedIdxs.length - 1; j >= 0; j--) {
                    newList.remove(selectedIdxs[j]);
                }

                // 임시 List에서 NewActivity List의 새로운 위치로 담기
                for (int k = 0; k < tempActivityList.size(); k++) {
                    newList.add(selectedIdxs[k] - 1, tempActivityList.get(k));
                }

                // Table에 적용
                setTableData(newList);

                // table 선택항목 다시 지정
                for (int n = 0; n < selectedIdxs.length; n++) {
                    selectedIdxs[n] = selectedIdxs[n] - 1;
                }
                
                table.setSelection(selectedIdxs);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        // 아래로
        ToolItem downItem = new ToolItem(toolBar, SWT.NONE);
        downItem.setToolTipText("Down");
        downItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.ARROWDOWN_IMAGE_PATH));
        downItem.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
            	
                final List<HashMap<String, Object>> newList = getCurrentTableList();

                int selecttionCount = table.getSelectionCount();
                if (selecttionCount == 0)
                    return;

                TableItem[] selectedItems = table.getSelection();
                final List<HashMap<String, Object>> tempActivityList = new ArrayList<HashMap<String, Object>>();
                int[] selectedIdxs = new int[selectedItems.length];

                // NewActivity List에서 선택한 대상을 임시 List에 담기
                for (int i = 0; i < selectedItems.length; i++) {
                    int dataMapIndex = (Integer) selectedItems[i].getData("dataMapIndex");
                    tempActivityList.add(newList.get(dataMapIndex));
                    selectedIdxs[i] = dataMapIndex;
                }

                // 더이상 아래가 없는 경우 처리 안함
                if (selectedIdxs[selectedItems.length - 1] == newList.size() - 1) {
                    return;
                }

                // NewActivity List에서 제거
                for (int j = selectedIdxs.length - 1; j >= 0; j--) {
                    newList.remove(selectedIdxs[j]);
                }

                // 임시 List에서 NewActivity List의 새로운 위치로 담기
                for (int k = 0; k < tempActivityList.size(); k++) {
                    newList.add(selectedIdxs[k] + 1, tempActivityList.get(k));
                }

                // Table에 적용
                setTableData(newList);

                // table 선택항목 다시 지정
                for (int n = 0; n < selectedIdxs.length; n++) {
                    selectedIdxs[n] = selectedIdxs[n] + 1;
                }
                table.setSelection(selectedIdxs);
            	
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        ToolItem sortSaveItem = new ToolItem(toolBar, SWT.NONE);
        sortSaveItem.setToolTipText("Save Sort");
        sortSaveItem.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.SAVE_IMAGE_PATH));
        sortSaveItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                saveSortTable();
                refreshTable();
            }
        });

        Composite center_composite = new Composite(composite, SWT.NONE);
        center_composite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
        center_composite.setLayoutData(BorderLayout.CENTER);
        center_composite.setLayout(new GridLayout(1, false));

        table = new Table(center_composite, SWT.BORDER | SWT.FULL_SELECTION);
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseDoubleClick(MouseEvent e) {
                Table table = (Table) e.getSource();
                TableItem tableItem = table.getItem(table.getSelectionIndex());
                faqView.detailFaqView(tableItem);
            }
        });

        columnInfoList = new ArrayList<String> ();
        // 번호
        createTableColumn("\uBC88\uD638", 50, SWT.CENTER);
        columnInfoList.add("\uBC88\uD638");
        // 유형
        createTableColumn("\uc720\ud615", 50, SWT.CENTER);        
        columnInfoList.add("\uc720\ud615");
        // 제목
        createTableColumn("\uC81C\uBAA9", 500, SWT.LEFT);
        columnInfoList.add("\uC81C\uBAA9");
        // 작성자
        createTableColumn("\uc791\uc131\uc790", 100, SWT.CENTER);
        columnInfoList.add("\uc791\uc131\uc790");
        // 작성일시
        createTableColumn("\uc791\uc131\uc77c\uc2dc", 150, SWT.CENTER);
        columnInfoList.add("\uc791\uc131\uc77c\uc2dc");

        refreshTable();
    }

    /**
     *
     *
     * @method createTableColumn
     * @date 2014. 3. 20.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createTableColumn(String columnName, int width, int align) {
        TableColumn tableColumn = new TableColumn(table, SWT.NONE);
        tableColumn.setText(columnName);
        tableColumn.setWidth(width);
		tableColumn.setAlignment(align);
    }

    /**
     *
     *
     * @method refreshTable
     * @date 2014. 3. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public void refreshTable() {
        table.removeAll();
        DataSet inputDataSet = new DataSet();
        inputDataSet.put("NO-PARAM", null);
        ArrayList<HashMap<String, Object>> faqList = faqQueryService.selectFaqList(inputDataSet);
        if (faqList == null) {
            //MessageDialog.openError(parentShell, "Error", "Error has occurred while getting the list of notice." + "\n" + "Connection refused." + "\n" + "Please contact to administrator.");
            return;
        }

        DataSet dataSet = null;
        for (int i = 0; i < faqList.size(); i++) {

            HashMap<String, Object> faqMap = faqList.get(i);

            String ouid = (String) faqMap.get("FAQ_PUID");
            BigDecimal seq = (BigDecimal) faqMap.get("FAQ_SEQ");
            String faq_type = (String) faqMap.get("FAQ_TYPE");
            String title = (String) faqMap.get("TITLE");
            String contents = (String) faqMap.get("CONTENTS");
            String create_user = (String) faqMap.get("CREATE_USER");
            String creation_date = (String) faqMap.get("CREATE_DATE");

            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, String.valueOf(seq));
            tableItem.setText(1, getFaqTypeName(faq_type));
            tableItem.setText(2, title);
            tableItem.setText(3, create_user);
            tableItem.setText(4, creation_date);
            
            // 첨부파일목록 조회
            dataSet = new DataSet();
            dataSet.put("addfile_puid", ouid);
            ArrayList<HashMap<String, Object>> addfileList = addfileQueryService.selectAddfileList(dataSet);
            
            tableItem.setData("dataMapIndex", i);
            tableItem.setData("faq_puid", ouid);
            tableItem.setData("faq_seq", String.valueOf(seq));
            tableItem.setData("faq_type", faq_type);
            tableItem.setData("title", title);
            tableItem.setData("contents", contents);
            tableItem.setData("create_user", create_user);
            tableItem.setData("creation_date", creation_date);
            tableItem.setData("addfileList", addfileList);
        }
    }

    public void saveSortTable() {
    	
    	TableItem[] items = table.getItems();
        DataSet dataSet = null;
        for (int i = 0; i < items.length; i++) {
        	
			dataSet = new DataSet();
			dataSet.put("faq_puid", items[i].getData("faq_puid"));
			dataSet.put("faq_seq", i+1);
			
			faqQueryService.updateFaqSeq(dataSet);
        }
    	
    }

    /**
     * 현재 테이블의 모든 값을 List로 가져오는 함수
     * 
     * @return
     */
    public List<HashMap<String, Object>> getCurrentTableList() {
    	
    	TableItem[] items = table.getItems();
        List<HashMap<String, Object>> tableValueList = new ArrayList<HashMap<String, Object>>();
        for (int i = 0; i < items.length; i++) {
        	
            HashMap<String, Object> tableMap = new HashMap<String, Object>();
            
            tableMap.put("dataMapIndex", items[i].getData("dataMapIndex"));
            tableMap.put("faq_puid", items[i].getData("faq_puid"));
            tableMap.put("faq_seq", items[i].getData("faq_seq"));
            tableMap.put("faq_type", items[i].getData("faq_type"));
            tableMap.put("title", items[i].getData("title"));
            tableMap.put("contents", items[i].getData("contents"));
            tableMap.put("create_user", items[i].getData("create_user"));
            tableMap.put("creation_date", items[i].getData("creation_date"));
            tableMap.put("addfileList", items[i].getData("addfileList"));
            
            tableValueList.add(tableMap);
        }

        return tableValueList;
    }

    public void setTableData(List<HashMap<String, Object>> dataList) {

        table.removeAll();

        for(int i = 0; i < dataList.size(); i++) {
        	
            HashMap<String, Object> dataMap = dataList.get(i);
            
            TableItem tableItem = new TableItem(table, SWT.NONE);
            tableItem.setText(0, dataMap.get("faq_seq").toString());
            tableItem.setText(1, getFaqTypeName(dataMap.get("faq_type").toString()));
            tableItem.setText(2, dataMap.get("title").toString());
            tableItem.setText(3, dataMap.get("create_user").toString());
            tableItem.setText(4, dataMap.get("creation_date").toString());
            
            tableItem.setData("dataMapIndex", i);
            tableItem.setData("faq_puid", dataMap.get("faq_puid"));
            tableItem.setData("faq_seq", dataMap.get("faq_seq"));
            tableItem.setData("faq_type", dataMap.get("faq_type"));
            tableItem.setData("title", dataMap.get("title"));
            tableItem.setData("contents", dataMap.get("contents"));
            tableItem.setData("create_user", dataMap.get("create_user"));
            tableItem.setData("creation_date", dataMap.get("creation_date"));
            tableItem.setData("addfileList", dataMap.get("addfileList"));
        }
    }

    public String getFaqTypeCode(String typeName) {
    	
    	String typeCode = null;
    	
    	if (typeName == null || typeName.equals("")) {
    		return null;
    	}
    	
    	if (typeName.equals("설계")) {
    		typeCode = "1";
    	} else if (typeName.equals("기관")) {
    		typeCode = "2";
    	} else if (typeName.equals("DCS")) {
    		typeCode = "3";
    	} else if (typeName.equals("전체")) {
    		typeCode = "A";
    	} 
    	
    	return typeCode;
    }
    
    public String getFaqTypeName(String typeCode) {
    	
    	String typeName = null;
    	
    	if (typeCode == null || typeCode.equals("")) {
    		return null;
    	}
    	
    	if (typeCode.equals("1")) {
    		typeName = "설계";
    	} else if (typeCode.equals("2")) {
    		typeName = "기관";
    	} else if (typeCode.equals("3")) {
    		typeName = "DCS";
    	} else if (typeCode.equals("A")) {
    		typeName = "전체";
    	} 
    	
    	return typeName;
    }
    
}
