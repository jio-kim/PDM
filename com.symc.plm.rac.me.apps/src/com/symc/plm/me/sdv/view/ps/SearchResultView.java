package com.symc.plm.me.sdv.view.ps;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.table.SDVTableView;
import org.sdv.core.ui.view.table.model.ColumnInfoModel;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import com.ssangyong.common.WaitProgressBar;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetUtils;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentPerson;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [Non-SR] [20150116] ymjang, 이전 Publishing 된 작업 표준서 선택시 구분을 위해 바탕색을 변경함.
 * [P0068] [20150127] ymjang, 영문작업표준서 검색 결과를 현재 영문,국문 순으로 컬럼이 셋팅되어있어 불편함. 
 * [P0068] [20150209] shcho, 영문작업표준서 검색 결과를 국문, 영문 순으로 변경 후 버그 수정 
 * [P0083] [20150225] ymjang, Search 화면에서 각 공법에 일련번호를 추가 요망.
 *                            1) index 순서 뒤로 한칸씩 이동함.
 *                            2) 정렬시 일련번호 재 생성 
 * [P0079] [20150225] ymjang, Search 화면에서 공법서 1개 또는 Multi 선택했을 때 하단 왼쪽에 선택한 개수가 표현되기 바람
 * [NON-SR][20150319] ymjang, 국문/영문에 따른 컬럼 인덱스 상이에 따른 오류 수정
 * [SR150317-021] [20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
 * [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
 * [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
 * [SR150427-006] [20150611] shcho, Working 인 공법 선택시에 publish 버튼이 비활성 되는 오류 수정
 */
public class SearchResultView extends SDVTableView {

	private boolean isPEEEnabled = true;

    private Registry registry;

    private Label lblResultCount;

    private static String exportFilePath;

    private WaitProgressBar progress;
    
    public TCSession session;

    public SearchResultView(Composite parent, int style, String id) {
        this(parent, style, id, 0);
    }

    public SearchResultView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

    public SearchResultView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, configId, order);
    }

    @Override
    protected void initUI(Composite parent) {
        setColInfoModel();
        

        if(AIFUtility.getActiveDesktop() != null){
        	AbstractAIFUIApplication application = AIFUtility.getActiveDesktop().getCurrentApplication();
        	if(application!=null){
        		this.session = (TCSession)application.getSession();
        	}
        }
        
        parent.setLayout(new BorderLayout());

        Composite labelComposite = new Composite(parent, SWT.NONE);
        labelComposite.setLayoutData(BorderLayout.SOUTH);
        labelComposite.setLayout(new FillLayout(SWT.HORIZONTAL));

        lblResultCount = new Label(labelComposite, SWT.NONE);
        lblResultCount.setAlignment(SWT.RIGHT);

        Composite tableComposite = new Composite(parent, SWT.NONE);
        tableComposite.setLayoutData(BorderLayout.CENTER);

        super.initUI(tableComposite);
        
        // [P0083] [20150225] ymjang, Search 화면에서 공법서 1개 또는 Multi 선택했을 때 하단 왼쪽에 선택한 개수가 표현되기 바람
        table.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event e) {
            	if (table.getSelectionCount() > 0)
                	lblResultCount.setText(table.getSelectionCount() +"/" + table.getItemCount() + registry.getString("SelectedCountLabel.Message"));
            	else
            		lblResultCount.setText(table.getItemCount() + registry.getString("SearchResultCountLabel.Message"));
            	
            	// [SR150317-021] [20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
            	// [SR150312-024] [20150324] Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
            	/*
            	if (getConfigId() != 0)
            		return;
            	*/
            	
            	TableItem[] tableItems = table.getSelection();
                if(tableItems != null) {
                    for(TableItem tableItem : tableItems) {
                    	
                        boolean isEnabled = true;

                    	//System.out.println("enabled = " + isEnabled);
                        setEabledPublishButton(isEnabled);
                        
                        String publishRev = null;
                        boolean isSelectLastRev = true;
                        String date_released = tableItem.getText(getColumnIndex("date_released"));
                        HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) tableItem.getData("editors");
                        
                        if(editors == null) {
                            publishRev = null;
                        } else {
                            TableEditor editor = editors.get(getColumnIndex("publish_rev"));
                            CCombo combo = (CCombo) editor.getEditor();                            
                            publishRev = combo.getItem(combo.getSelectionIndex());
                            isSelectLastRev = combo.getSelectionIndex() == (combo.getItemCount()-1);
                        }

                        // Publishing 되어 있지 않은 공법일 경우는 Open/Download 버튼 비활성화
                        if (publishRev == null)
                        {
                        	setEabledButton("Download", false);
                        	setEabledButton("Open", false);
                        } else {
                            setEabledButton("Download", true);
                            setEabledButton("Open", true);                            
                        }
                        
                        // 국문일 경우,
                    	if (getConfigId() == 0)
                    	{
                            if (publishRev != null)
                            {
                                //[SR150427-006] [20150611] shcho, Working 인 공법 선택시에 publish 버튼이 비활성 되는 오류 수정
                            	// isEnabled = date_released.isEmpty() && (isSelectLastRev) ? true : false;
                            	if(date_released.isEmpty()==true && isSelectLastRev==true){
                            		isEnabled = true;
                            	}else{
                            		
                            		// [NON-SR][20160818] taeku.jeong bypass가 켜져 있으면 Publish 버튼을 Enable 하도록 수정 
                            		boolean hasByPass = false; 
                                    if(session!=null){
                                    	hasByPass = session.hasBypass();
                                    }

                            		if(hasByPass==true){
                            			isEnabled = true;
                            		}else{
                            			isEnabled = false;
                            		}
                            	}
                            	
                            	//[NON-SR] [20150611] shcho, 선택된 PublishRev이 마지막인 경우에만 Preview 버튼이 활성되도록 변경 
                            	setEabledButton("Preview", isSelectLastRev);
                            }
                            
                        	//System.out.println("enabled = " + isEnabled);
                            setEabledPublishButton(isEnabled);
                            
                            if (!isEnabled)
                            	break;
                    	}
                    	
                        // [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
                        // 영문일 경우,
                    	if (getConfigId() == 1)
                    	{
                            if (date_released.isEmpty())
                            {
                            	isEnabled = false;
                            }
                            
                        	setEabledButton("Publish", isEnabled);
                        	setEabledButton("CreateWorkflow", isEnabled);
                        	setEabledButton("Edit Operation", isEnabled && isPEEEnabled);
                        	setEabledButton("Edit Activity", isEnabled && isPEEEnabled);

                        	if (!isEnabled)
                            	break;
	                        
                    	}
                    }
                }            	
            }
    	});
        
    }

    private void setColInfoModel() {
        registry = Registry.getRegistry(this);

        String[] ids = registry.getStringArray("table.column.search.id." + getConfigId());
        String[] names = registry.getStringArray("table.column.search.name." + getConfigId());
        String[] widths = registry.getStringArray("table.column.search.width." + getConfigId());
        String[] sorts = registry.getStringArray("table.column.search.sort." + getConfigId());
        String[] types = registry.getStringArray("table.column.search.type." + getConfigId());
        String[] alignments = registry.getStringArray("table.column.search.alignment." + getConfigId());

        List<ColumnInfoModel> colModelList = new ArrayList<ColumnInfoModel>();
        for(int i = 0; i < ids.length; i++) {
            ColumnInfoModel colModel = new ColumnInfoModel();
            colModel.setColId(ids[i]);
            colModel.setColName(names[i]);
            colModel.setColumnWidth(Integer.parseInt(widths[i]));
            colModel.setSort(Boolean.parseBoolean(sorts[i]));
            colModel.setColType(Integer.parseInt(types[i]));
            colModel.setEditable(true);
            String align = alignments[i].toUpperCase();
            if("LEFT".equals(align)) {
                colModel.setAlignment(SWT.LEFT);
            } else if("CENTER".equals(align)) {
                colModel.setAlignment(SWT.CENTER);
            } else {
                colModel.setAlignment(SWT.RIGHT);
            }
            colModelList.add(colModel);
        }

        setColumnInfo(colModelList);
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {
        this.dataMap = dataMap;

        if(dataMap.containsKey("actionId")) {
            String actionId = dataMap.getStringValue("actionId");
            try {
                if("Search".equals(actionId)) {
                    table.setSortDirection(SWT.NONE);
                    table.setSortColumn(null);

                    List<HashMap<String, Object>> operationList = (List<HashMap<String, Object>>) dataMap.getTableValue("operationList");
                    if(operationList!=null){
                    	lblResultCount.setText(operationList.size() + registry.getString("SearchResultCountLabel.Message"));
                    }else{
                    	lblResultCount.setText("0" + registry.getString("SearchResultCountLabel.Message"));
                    }
                    setTableData(operationList);
                    //20210616 CF-2224 (이종화차장,김용환부장 요청) 영문 검색일 경우 검색 한 후에 Revision Rule이 Latest Released 이고 검색 조건 Released 공법이 체크되어 있는
                    // 경우가 아니면 Preview, Edit Operation, Edit Activity를 비활성화 시켜서 작업을 못하도록 막음
                    if (getConfigId() == 1)
                    {
                    	isPEEEnabled = true;
                    	// 조건확인...
                    	Control[] controls = getParent().getChildren();
                    	for(Control control : controls)
                    	{
                    		if(control instanceof SearchConditionView)
                    		{
                    			SearchConditionView searchConditionView = (SearchConditionView)control;
                    			IDataMap iDataMap = searchConditionView.getLocalDataMap();
                    			isPEEEnabled = (Boolean)iDataMap.get("release_operation").getValue() && searchConditionView.revisionRuleName.startsWith("Latest Released");
                    			break;
                    		}
                    	}
                    	setEabledButton("Edit Operation", isPEEEnabled);
                    	setEabledButton("Edit Activity", isPEEEnabled);
                    }
                } else if("UpdateOperationName".equals(actionId)) {
                    TableItem[] tableItems = table.getSelection();
                    if(tableItems != null) {
                        for(TableItem tableItem : tableItems) {
                            refreshEnglishOperationName(tableItem);
                        }
                    }
                } else if("Publish".equals(actionId)) {
                    if(table.getSelectionCount() > 0) {
                        TableItem[] tableItems = table.getSelection();
                        for(TableItem tableItem : tableItems) {
                            refreshPublishInfo(tableItem);
                        }
//                        table.redraw();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            throw new SDVRuntimeException(registry.getString("MissingActionId.Message"));
        }
    }

    private void refreshEnglishOperationName(TableItem tableItem) throws Exception {
        String itemId = tableItem.getText(getColumnIndex("item_id"));
        String revId = tableItem.getText(getColumnIndex("item_revision_id"));
        TCComponentItemRevision itemRevision = CustomUtil.findItemRevision(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM_REV, itemId, revId);

        if(itemRevision != null) {
            itemRevision.refresh();
            String engOpName = itemRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ENG_NAME);
            tableItem.setText(getColumnIndex("m7_ENG_NAME"), engOpName);
            int dataIndex = (Integer) tableItem.getData("dataMapIndex");
            dataList.get(dataIndex).put(SDVPropertyConstant.OPERATION_REV_ENG_NAME, engOpName);
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshPublishInfo(TableItem tableItem) throws Exception {
        String itemId = tableItem.getText(5);
        String revId = tableItem.getText(6);
        
        System.out.println("Item Revison : "+itemId+"/"+revId);

        itemId = registry.getString("ProcessSheetItemIDPrefix." + getConfigId()) + itemId;

        TCComponentItem item = SDVBOPUtilities.FindItem(itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);
        // 마지막 Publish 된 것만 가져와서 Combo에 추가
        if(item != null) {
            TCComponent[] revisions = item.getRelatedComponents("revision_list");
            TCComponentItemRevision revision = null;
            String pubRevId = null;
            
            if(revisions!=null){
            	for(int i = revisions.length - 1; i >= 0; i--) {
            		pubRevId = revisions[i].getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
            		if(pubRevId.startsWith(revId)) {
            			revision = (TCComponentItemRevision) revisions[i];
            			HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) tableItem.getData("editors");
            			if(editors == null) {
            				editors = new HashMap<Integer, TableEditor>();
            			}
            			
            			CCombo control = null;
            			TableEditor editor = ((HashMap<Integer, TableEditor>) editors).get(16);
            			if(editor == null) {
            				editor = new TableEditor(table);
            				editor.grabHorizontal = true;
            			}
            			
            			control = (CCombo) editor.getEditor();
            			if(control == null) {
            				control = new CCombo(table, SWT.NONE);
            				control.setData("tableItem", tableItem);
            				control.addSelectionListener(this);
            				control.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
            				control.setEditable(false);
            			}
            			
            			boolean addFlag = true;
            			if(control.getItemCount() > 0) {
            				for(int j = 0; j < control.getItemCount(); j++) {
            					String columnData = control.getItem(j);
            					if(columnData.equals(pubRevId)) {
            						addFlag = false;
            						break;
            					}
            				}
            			}
            			
            			if(addFlag) {
            				control.add(pubRevId);
            				control.select(control.getItemCount() - 1);
            				
            				editor.setEditor(control, tableItem, getColumnIndex("publish_rev"));
            				editors.put(getColumnIndex("publish_rev"), editor);
            				//                    control.pack();
            				tableItem.setData("editors", editors);
//                        ((HashMap<Integer, TableEditor>) tableItem.getData("editors")).put(i, editor);
            			}
            			
            			
            			break;
            		}
            	}
            } // End of if(revisions!=null)

            int dataIndex = (Integer) tableItem.getData("dataMapIndex");
            Object[] revs = (Object[]) dataList.get(dataIndex).get("publish_rev");
            Object[] newRevs = null;
            if(revs == null) {
                newRevs = new Object[1];
            } else {
                newRevs = new Object[revs.length + 1];
                for(int i = 0; i < revs.length; i++) {
                    newRevs[i] = revs[i];
                }
            }

            newRevs[newRevs.length - 1] = pubRevId;

            dataList.get(dataIndex).put("publish_rev", newRevs);
            TCComponent[] releaseStatusList = null;
            if(revision!=null){
            	releaseStatusList = revision.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
            }
            if(releaseStatusList != null && releaseStatusList.length > 0) {
                String status = releaseStatusList[releaseStatusList.length - 1].getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME);
                dataList.get(dataIndex).put("publish_status", status);
                tableItem.setText(getColumnIndex("publish_status"), status);
            } else {
                dataList.get(dataIndex).put("publish_status", null);
                tableItem.setText(getColumnIndex("publish_status"), "");
            }
            String lastModDate = null;
            if(revision!=null){
            	lastModDate = SDVStringUtiles.dateToString(revision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE), "yyyy-MM-dd HH:mm");
            }
            dataList.get(dataIndex).put("publsih_date", lastModDate);
            tableItem.setText(getColumnIndex("publsih_date"), lastModDate);

            TCComponentUser tcUser = null;
            TCComponentPerson person = null;
            if(revision!=null){
            	tcUser = (TCComponentUser) revision.getReferenceProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_USER);
            	if(tcUser!=null){
            		person = (TCComponentPerson) tcUser.getUserInformation().get(0);
            	}
            }
            
            if(person != null) {
                String userName = person.getProperty("user_name");
                dataList.get(dataIndex).put("publish_user", userName);
                dataList.get(dataIndex).put("publish_user_person", person);
                tableItem.setText(getColumnIndex("publish_user"), userName);
            } else {
                String userId = tcUser.getUserId();
                dataList.get(dataIndex).put("publish_user", userId);
                dataList.get(dataIndex).put("publish_user_person", null);
                tableItem.setText(getColumnIndex("publish_user"), userId);
            }
        }
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if(e.widget instanceof CCombo) {
            CCombo combo = (CCombo) e.widget;
            TableItem tableItem = (TableItem) combo.getData("tableItem");
            String itemId = tableItem.getText(getColumnIndex("item_id"));
            String publishRev = combo.getItem(combo.getSelectionIndex());
            String date_released = tableItem.getText(getColumnIndex("date_released"));
            
            TableItem selectedTableItem = null;
            int selectIdx = table.getSelectionIndex(); 
            if(selectIdx >= 0) {
                selectedTableItem = table.getItem(selectIdx);
            }
            
            // [Non-SR] [20150115] 이전 Publishing 된 작업 표준서 선택시 구분을 위해 바탕색을 변경함.
            if (combo.getSelectionIndex() != (combo.getItemCount()-1))
            {
            	combo.setBackground(UIUtil.getColor(SWT.COLOR_YELLOW));

            	if(selectedTableItem != null && selectedTableItem.hashCode() == tableItem.hashCode()) {
            	    // [SR150317-021] [20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
            	    setEabledPublishButton(false);
            	    //[NON-SR] [20150611] shcho, 선택된 PublishRev이 마지막인 경우에만 Preview 버튼이 활성되도록 변경
            	    setEabledButton("Preview", false);
            	}
            }
            else
            {
            	combo.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));

            	if(selectedTableItem != null && selectedTableItem.hashCode() == tableItem.hashCode()) {
                	// [SR150317-021] [20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
                	// [SR150427-006] [20150611] shcho, Working 인 공법 선택시에 publish 버튼이 비활성 되는 오류 수정
                	setEabledPublishButton(date_released.isEmpty() ? true : false);
                	//[NON-SR] [20150611] shcho, 선택된 PublishRev이 마지막인 경우에만 Preview 버튼이 활성되도록 변경
            	    setEabledButton("Preview", true);
            	}
            }
            
            int dataMapIndex = (Integer) tableItem.getData("dataMapIndex");
            dataList.get(dataMapIndex).put("selected_publish_rev", publishRev);

            try {
                TCComponentItemRevision itemRevision = getPublishRev(itemId, publishRev);
                if(itemRevision != null) {
                    TCComponent[] statusList = itemRevision.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                    if(statusList != null && statusList.length > 0) {
                        tableItem.setText(getColumnIndex("publish_status"), statusList[statusList.length - 1].getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
                    } else {
                        tableItem.setText(getColumnIndex("publish_status"), "");
                    }
                    Date lastModDate = itemRevision.getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE);
                    if(lastModDate != null) {
                        tableItem.setText(getColumnIndex("publsih_date"), SDVStringUtiles.dateToString(lastModDate, "yyyy-MM-dd HH:mm"));
                    } else {
                        tableItem.setText(getColumnIndex("publsih_date"), "");
                    }
                    TCComponentUser tcUser = (TCComponentUser) itemRevision.getReferenceProperty(SDVPropertyConstant.PS_REV_LAST_PUB_USER);
                    if(tcUser != null) {
                        TCComponentPerson person = (TCComponentPerson) tcUser.getUserInformation().get(0);
                        tableItem.setText(getColumnIndex("publish_user"), person.getProperty("user_name"));
                    } else {
                        tableItem.setText(getColumnIndex("publish_user"), "");
                    }
                }
            } catch (Exception e1) {
                e1.printStackTrace();
            }

        }
    }

    @Override
    public IDataMap getLocalDataMap() {
        return getLocalSelectDataMap();
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        if(table.getSelectionCount() == 0) {
            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("NoSelectOperation.Message"), "Search", MessageBox.ERROR);
        }

        List<HashMap<String, Object>> operationList = new ArrayList<HashMap<String,Object>>();
        TableItem[] items = table.getSelection();
        for(int i = 0; i < items.length; i++) {
            int dataMapIndex = (Integer) items[i].getData("dataMapIndex");
            operationList.add(dataList.get(dataMapIndex));
        }

        dataMap.put("targetOperationList", operationList, IData.TABLE_FIELD);
        dataMap.put("configId", getConfigId(), IData.INTEGER_FIELD);
        dataMap.put("process_type", (String) operationList.get(0).get("process_type"), IData.STRING_FIELD);
        dataMap.put("viewId", getId());

        return dataMap;
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    protected void validateConfig(int configId) {
        if (configId != 0 && configId != 1) {
            throw new SDVRuntimeException("View[" + getId() + " not supported config Id :" + configId);
        }
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    public void processSheetPreview() throws SDVException {
        if(validateOneSelection()) {
            TableItem tableItem = table.getSelection()[0];
            int dataMapIndex = (Integer) tableItem.getData("dataMapIndex");
            
            // [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
            // 공법의 릴리즈 상태를 Parameter 로 넘기기 위해 값을 저장한다.
            String date_released = tableItem.getText(getColumnIndex("date_released"));
            dataList.get(dataMapIndex).put(SDVPropertyConstant.ITEM_DATE_RELEASED, date_released);
            
//            String commandId = "symc.me.bop.ProcessSheetPreviewKoCommand";
//
//            Map<String, Object> parameter = new HashMap<String, Object>();
//            parameter.put("targetOperaion", dataList.get(dataMapIndex));
//
//            ISDVOperation operation = OperationBeanFactory.getOperator(commandId);
//            operation.setParameter(commandId, parameter, null);
//            if(operation instanceof IDialogOpertation) {
//                operation.startOperation(commandId);
//                try {
//                    operation.executeOperation();
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    throw new SDVException(e.getMessage(), e);
//                }
//                operation.endOperation();
//            }


            try {
                IDialog previewDialog = null;
                if(getConfigId() == 0) {
                    previewDialog = UIManager.getDialog(getShell(), "symc.dialog.processSheetPreviewDialogKO");
                } else {
                    previewDialog = UIManager.getDialog(getShell(), "symc.dialog.processSheetPreviewDialogEN");
                }

                Map<String, Object> parameter = new HashMap<String, Object>();
                if (getConfigId() == 1)
                	dataList.get(dataMapIndex).put("IS_ENABLE_ENG_PREVIEW_BUTTON", isPEEEnabled);
                parameter.put("targetOperaion", dataList.get(dataMapIndex));
                previewDialog.setParameters(parameter);
                previewDialog.open();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void processSheetOpen() {
        if(validateOneSelection()) {
            TableItem tableItem = table.getSelection()[0];
            HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) tableItem.getData("editors");
            if(editors == null) {
                MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("NotPublished.Message"), "Open", MessageBox.ERROR);
                return;
            } else {
                TableEditor editor = editors.get(getColumnIndex("publish_rev"));
                CCombo combo = (CCombo) editor.getEditor();
                String publishRev = combo.getItem(combo.getSelectionIndex());
                String itemId = tableItem.getText(getColumnIndex("item_id"));
                try {
                    openPublishRev(itemId, publishRev);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private TCComponentItemRevision getPublishRev(String itemId, String itemRev) throws Exception {
        itemId = registry.getString("ProcessSheetItemIDPrefix." + getConfigId()) + itemId;

        return CustomUtil.findItemRevision(SDVTypeConstant.PROCESS_SHEET_ITEM_REV, itemId, itemRev);
    }

    private void openPublishRev(String itemId, String itemRev) throws Exception {
        TCComponentItemRevision revision = getPublishRev(itemId, itemRev);
        if(revision != null) {
            TCComponent component = revision.getRelatedComponent("IMAN_specification");
            if(component instanceof TCComponentDataset) {
                TCComponentDataset dataset = (TCComponentDataset) component;
                TCComponentTcFile[] files = dataset.getTcFiles();
                if(files != null && files.length > 0) {
                    String filePath = files[0].getFile(null).getAbsolutePath();
                    ProcessSheetUtils.openExcleFile(filePath);
//                    openExcelFile(filePath, null);
                }
            }
        }
    }

//    private void openExcelFile(String filePath, OleListener oleListener) throws IOException {
//        Composite oleComposite = new Composite(this, SWT.NONE);
//        oleComposite.setLayoutData(BorderLayout.CENTER);
//        oleComposite.setLayout(new FillLayout());
//
//        appControlSite = new OleControlSite(new OleFrame(oleComposite, SWT.NONE), SWT.NONE, "Excel.Application");
//        appControlSite.doVerb(OLE.OLEIVERB_OPEN);
//
//        OleAutomation application = new OleAutomation(appControlSite);
//        application.setProperty(application.getIDsOfNames(new String[] {"Visible"})[0], new Variant(true));
//        OleAutomation workbooks = application.getProperty(application.getIDsOfNames(new String[] {"Workbooks"})[0]).getAutomation();
//        Variant varResult = workbooks.invoke(workbooks.getIDsOfNames(new String[] {"Open"})[0], new Variant[] {new Variant(filePath)});
//        if(varResult != null) {
//            System.out.println(" copy invoke result of BSHEET = " + varResult);
//            varResult.dispose();
//        } else {
//            System.out.println("=====failed invoke copySheet method ====");
//        }
//    }

    private boolean validateOneSelection() {
        if(table.getSelectionCount() == 0) {
            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("NoSelectOperation.Message"), "Open", MessageBox.ERROR);
            return false;
        }

        if(validateSelection()) {
            if(table.getSelection().length > 1) {
                MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("MultiSelectOperation.Message"), "Open", MessageBox.ERROR);
                return false;
            }
        }

        return true;
    }

    private boolean validateSelection() {
        if(table.getSelectionCount() == 0) {
            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("NoSelectOperation.Message"), "Open", MessageBox.ERROR);
            return false;
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public void exportSearchResult() {
        String defaultFileName = registry.getString("ProcessSheetListDefaultFileName." + getConfigId()) + SDVStringUtiles.dateToString(new Date(), "yyyyMMdd");

        openFileDialog(defaultFileName, ".xlsx");
        if(exportFilePath != null) {
            try {
                Workbook workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet();
                Row row = sheet.createRow(0);
                TableColumn[] columns = table.getColumns();
                for(int i = 0; i < columns.length; i++) {
                    row.createCell(i).setCellValue(columns[i].getText());
                }

                TableItem[] items = table.getItems();
                for(int i = 0; i < items.length; i++) {
                    row = sheet.createRow(i + 1);
                    for(int j = 0; j < columns.length; j++) {
                        HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) items[i].getData("editors");
                        if(editors != null) {
                            TableEditor editor = editors.get(j);
                            if(editor != null && editor.getEditor() != null) {
                                CCombo combo = (CCombo) editor.getEditor();
                                row.createCell(j).setCellValue(combo.getText());
                            } else {
                                row.createCell(j).setCellValue(items[i].getText(j));
                            }
                        } else {
                            row.createCell(j).setCellValue(items[i].getText(j));
                        }
                    }
                }

                FileOutputStream fos = new FileOutputStream(exportFilePath);
                workbook.write(fos);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("CompleteToExportResultList.Message"), "List", MessageBox.INFORMATION);
        }
    }

    public static void openFileDialog(final String defaultFileName, final String extention) {

        Display.getDefault().syncExec(new Runnable() {
            @Override
            public void run() {
                FileDialog fileDialog = new FileDialog(UIManager.getCurrentDialog().getShell(), SWT.SAVE);
                fileDialog.setFileName(defaultFileName);
                fileDialog.setFilterExtensions(new String[]{"*" + extention});
                fileDialog.setOverwrite(true);
                exportFilePath = fileDialog.open();
            }
        });
    }

    @Override
    public void uiLoadCompleted() {
        // 통합테스트 시 적용
        if(getConfigId() == 1) {
            AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
            if(application instanceof MFGLegacyApplication) {
                TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
                try {
                    TCComponentRevisionRule revisionRuleComp = bopWindow.getRevisionRule();

                    if(revisionRuleComp != null) {
                        String revisionRule = revisionRuleComp.toString();
                        
                        // [SR150312-024] [20150324] Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
                        // --> M7_ProcessSheet_EN_EditableRevisionRule Preference 에 Latest Working For ME 추가
                        TCPreferenceService prefService = ((TCSession) AIFUtility.getDefaultSession()).getPreferenceService();
//                        String[] prefValues = prefService.getStringArray(TCPreferenceService.TC_preference_site, "M7_ProcessSheet_EN_EditableRevisionRule");
                        String[] prefValues = prefService.getStringValuesAtLocation("M7_ProcessSheet_EN_EditableRevisionRule", TCPreferenceLocation.OVERLAY_LOCATION);
                        if(prefValues != null) {
                            for(String value : prefValues) {
                                if(value.equals(revisionRule)) {
                                    return;
                                }
                            }
                        }
                    }

                    LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
                    for(String key : buttons.keySet()) {
                        IButtonInfo button = buttons.get(key);
                        if(!"List".equals(button.getActionId())) {
                            button.getButton().setEnabled(false);
                        }
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * [NON-SR][20160816] Taeku.Jeong MECO EPL Load를 Operation 단위로 수행 하도록 변경과정에
     * 더이상 필요없어진 Function으로 Remark 처리함
     */
//    public void loadMEPL() {
//        progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
//        progress.setWindowSize(500, 400);
//        progress.start();
//        progress.setShowButton(true);
//        progress.setStatus("[" + new Date() + "]" + "MEPL Load start.");
//        progress.setAlwaysOnTop(true);
//
//        try {
//            if(table.getSelectionCount() == 0) {
//                throw new Exception(registry.getString("NoSelectOperation.Message"));
//            }
//
//            List<String> mecoList = new ArrayList<String>();
//            for(TableItem tableItem : table.getSelection()) {
//                int dataMapIndex = (Integer) tableItem.getData("dataMapIndex");
//                String mecoNo = (String) dataList.get(dataMapIndex).get(SDVPropertyConstant.OPERATION_REV_MECO_NO);
//                if(!mecoList.contains(mecoNo)) {
//                    mecoList.add(mecoNo);
//                }
//            }
//
//            progress.setStatus("[" + new Date() + "]" + "Authorization checking...");
//
//            List<TCComponentItemRevision> mecoRevList = new ArrayList<TCComponentItemRevision>();
//            String loginUserId = ((TCSession) AIFUtility.getDefaultSession()).getUser().getUserId();
//            StringBuffer sb = new StringBuffer();
//            for(String mecoNo : mecoList) {
//                TCComponentItemRevision mecoRev = SDVBOPUtilities.FindItem(mecoNo, SDVTypeConstant.MECO_ITEM).getLatestItemRevision();
//                mecoRevList.add(mecoRev);
//
//                TCComponentUser user = (TCComponentUser) mecoRev.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
//                if(!loginUserId.equals(user.getUserId())) {
//                    sb.append(mecoNo + "\n");
//                }
//            }
//
//            if(sb.length() > 0) {
//                sb.insert(0, "The access is denied.\n");
//                throw new Exception(sb.toString());
//            }
//
//            for(TCComponentItemRevision mecoRev : mecoRevList) {
////                TCComponentItem mecoItem = SDVBOPUtilities.FindItem(mecoNo, SDVTypeConstant.MECO_ITEM);
////                if(mecoItem != null) {
//                    progress.setStatus("[" + new Date() + "]" + "Loading MEPL of " + mecoRev);
//                    // MEPL 생성 (MECO_EPL Table에 Data 생성)
//                    CustomUtil cutomUtil = new CustomUtil();
//                    ArrayList<SYMCBOPEditData> meplList = cutomUtil.buildMEPL((TCComponentChangeItemRevision) mecoRev, true);
//                    if(meplList == null){
//                        throw new NullPointerException("Error occured on M-EPL loading");
//                    }
////                }
//            }
//            progress.close();
//            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Load MEPL", MessageBox.ERROR);
//        } catch (Exception e) {
//            e.printStackTrace();
//            progress.setStatus("[" + new Date() + "]" + "Error : " + e.getMessage());
////            MessageBox.post(UIManager.getCurrentDialog().getShell(), e.getMessage(), "Load MEPL", MessageBox.ERROR);
//        }
//    }
    
    /**
     * [P0083] [20150225] ymjang, Search 화면에서 각 공법에 일련번호를 추가 요망.
     * 정렬시 일련번호 재 생성 
     */                       
    @Override
    public void doSort(TableColumn currentColumn) {
    	
    	super.doSort(currentColumn);
    	
    	// 정렬시 일련번호 재 생성 
        TableItem[] items = table.getItems(); 
        int rowIdx = 0;
        for (int i = 0; i < items.length; i++) {
        	items[i].setText(0, String.valueOf(++rowIdx));
        }
    }
    
    /**
     * [NON-SR][20150319] ymjang, 국문/영문에 따른 컬럼 인덱스 상이에 따른 오류 수정
     * 국문/영문에 따른 Column Index 값이 상이하므로 해당 컬럼의 Index값을 리턴한다.
     */ 
    private int getColumnIndex(String colName)
    {
    	int columnIndex = -1;
    	
        String[] ids = registry.getStringArray("table.column.search.id." + getConfigId());
        for (int i = 0; i < ids.length; i++) {
			if (colName.equals(ids[i]))
			{
				columnIndex = i;
				break;
			}
		}
        
        return columnIndex;        
    }
    
    /**
     * [SR150317-021] [20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
     * @param isEnabled
     */
    private void setEabledPublishButton(boolean isEnabled)
    {
        LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
        for(String key : buttons.keySet()) {
            IButtonInfo button = buttons.get(key);
            if("Publish".equals(button.getActionId())) {
                button.getButton().setEnabled(isEnabled);
            }
            
        	if (getConfigId() != 0)
                if("CreateWorkflow".equals(button.getActionId())) {
                    button.getButton().setEnabled(isEnabled);
                }
        }
    }

    /**
     * [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
     * @param buttonId
     * @param isEnabled
     */
    private void setEabledButton(String buttonId, boolean isEnabled)
    {
        LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
        for(String key : buttons.keySet()) {
            IButtonInfo button = buttons.get(key);
            if(buttonId.equals(button.getActionId())) {
                button.getButton().setEnabled(isEnabled);
            }
        }
    }

}
