/**
 *
 */
package com.symc.plm.me.sdv.view.pert;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import com.kgm.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;


/**
 * [SR150529-021][20150821] [shcho] ���� ��Ʈ ���� ��� ���ϰ��� ��û
 *                                                  X100�� �ϳ��� Line ������ short/long �ΰ��� Station���� ������ ���鼭 PreDecessor�� Successor ���Ṯ�� �߻�. 
 *                                                  �̿����� Station �� Station ���� PreDecessor�� Successor ������ �̷�� ������ ȭ�� ���� ��.
 * 
 * 
 * Class Name : SetSubDecessorView
 * Class Description :
 * @date 	2014. 1. 3.
 * @author  CS.Park
 *
 */
public class SetSubDecessorView extends AbstractSDVViewPane implements SelectionListener {

	private static final Logger logger = Logger.getLogger(SetSubDecessorView.class);

	public static final String EDITOR_EMPTY_SELECTION = " - ";

	public static final int ADD = 1;
	public static final int DELETE = 0;

	public static final int SUCESSOR_IDX_COLUMN = 0;
	public static final int SUCESSOR_ID_COLUMN = 1;
	public static final int SUCESSOR_NAME_COLUMN = 2;
	public static final int SUCESSOR_STATION_COLUMN = 3;
	public static final int DECESSOR_COLUMN = 4;

	private Table tableSucessors;
	private Text text;
	private TCComponentBOPLine currentBOPLine;
	private boolean isDirty;

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @param configId
	 * @param order
	 * @wbp.parser.constructor
	 */
	public SetSubDecessorView(Composite parent, int style, String id) {
		super(parent, style, id);
	}

	/**
	 * @param parent
	 * @param style
	 * @param id
	 * @param configId
	 * @param order
	 */
	public SetSubDecessorView(Composite parent, int style, String id, int configId, String order) {
		super(parent, style, id, configId, order);
	}


	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void initUI(Composite parent) {

		Composite root = new Composite(parent, SWT.NONE);
		root.setLayout(new BorderLayout(0, 0));

		Composite infomationPanel = new Composite(root, SWT.NONE);
		infomationPanel.setLayoutData(BorderLayout.NORTH);
		infomationPanel.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(infomationPanel, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL, SWT.CENTER, false, false, 1, 1));
		composite.setLayout(new GridLayout(2, false));

		Label lblCurrentComponent = new Label(composite, SWT.NONE);
		lblCurrentComponent.setAlignment(SWT.RIGHT);
		GridData gd_lblCurrentComponent = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblCurrentComponent.widthHint = 110;
		lblCurrentComponent.setLayoutData(gd_lblCurrentComponent);
		lblCurrentComponent.setText("Current BOPLine : ");

		text = new Text(composite, SWT.BORDER);
		text.setEditable(false);
		GridData gd_text = new GridData(GridData.HORIZONTAL_ALIGN_FILL, SWT.CENTER, false, false, 1, 1);
		gd_text.widthHint = 400;
		text.setLayoutData(gd_text);

		Composite dataPanel = new Composite(root, SWT.NONE);
		dataPanel.setLayoutData(BorderLayout.CENTER);
		dataPanel.setLayout(new FillLayout(SWT.HORIZONTAL));

		tableSucessors = new Table(dataPanel, SWT.BORDER | SWT.FULL_SELECTION);
		tableSucessors.setLinesVisible(true);
		tableSucessors.setHeaderVisible(true);

		TableColumn tblcolNo = new TableColumn(tableSucessors, SWT.NONE);
		tblcolNo.setWidth(30);
		tblcolNo.setText("NO");
		tblcolNo.setAlignment(SWT.CENTER);

		TableColumn tblcolItemId = new TableColumn(tableSucessors, SWT.NONE);
		tblcolItemId.setWidth(200);
		tblcolItemId.setText("Line Id");

		TableColumn tblcolItemName = new TableColumn(tableSucessors, SWT.NONE);
		tblcolItemName.setWidth(200);
		tblcolItemName.setText("Line Name");

		TableColumn tblcolStationCode = new TableColumn(tableSucessors, SWT.NONE);
		tblcolStationCode.setWidth(75);
		tblcolStationCode.setText("Station No.");

		TableColumn tblcolDecessors = new TableColumn(tableSucessors, SWT.NONE);
		tblcolDecessors.setWidth(495);
		tblcolDecessors.setText("Extension Decessor");
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#setLocalDataMap(org.sdv.core.common.data.IDataMap)
	 */
	@Override
	public void setLocalDataMap(IDataMap dataMap) {
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalDataMap()
	 */
	@Override
	public IDataMap getLocalDataMap() {
		return null;
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getLocalSelectDataMap()
	 */
	@Override
	public IDataMap getLocalSelectDataMap() {
		return null;
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#getInitOperation()
	 */
	@Override
	public AbstractSDVInitOperation getInitOperation() {

		return new AbstractSDVInitOperation(){
			@Override
			public void executeOperation() throws Exception {
				try {
		            if (!(AIFUtility.getCurrentApplication() instanceof MFGLegacyApplication)) {
		                // MPPApplication Check
		                throw new Exception("MPP Application���� �۾��ؾ� �մϴ�.");
		            }
		            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
		            if (selectedTargets == null || selectedTargets.length > 1)
		                throw new Exception("�۾� ����� ������ �ּ���.");

		            if ( !(selectedTargets[0] instanceof TCComponentBOPLine)){
		                throw new Exception("BOPLine�� ������ �ּ���.");
		            }

		            TCComponentBOPLine selectedBOPLine  = (TCComponentBOPLine)selectedTargets[0];

		            RawDataMap targetDataMap = new RawDataMap();
		            targetDataMap.put("CurrentBOPLine", selectedBOPLine, IData.OBJECT_FIELD);
		            getPredecessors(selectedBOPLine, targetDataMap);

		            DataSet targetDataset = new DataSet();
		            targetDataset.addDataMap(getId(), targetDataMap);
		            setData(targetDataset);
		        } catch (Exception ex) {
		            throw ex;
		        }
			}

			public void getPredecessors(TCComponentBOPLine sucessorLine, IDataMap datamap ) throws TCException{
				AIFComponentContext[] predessors = sucessorLine.getAllPredecessors();
				if(predessors != null  && predessors.length > 0){
					List<TCComponentBOPLine> data = new ArrayList<TCComponentBOPLine>();
					for(AIFComponentContext context : predessors){
						if(context.getComponent() instanceof TCComponentBOPLine){
							data.add((TCComponentBOPLine)context.getComponent());
						}
					}
					datamap.put("predecessors", data, IData.LIST_FIELD);
				}
			}
		};
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#initalizeLocalData(int, org.sdv.core.common.IViewPane, org.sdv.core.common.data.IDataSet)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

		setToolbarState(this.isDirty);

		// ������ ����� ȭ�鿡 �����ϴ� �Լ�
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
                currentBOPLine = (TCComponentBOPLine) dataset.getValue("CurrentBOPLine");


                try {
                	List<TCComponentBOPLine> currentChildren = getChildrenList(currentBOPLine);

                	text.setText(currentBOPLine.getItemRevision().toString() + "-" + currentBOPLine.getItemRevision().getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

                	List<TCComponentBOPLine> predecessors = (List<TCComponentBOPLine>)dataset.getValue("predecessors");
                	int rowNum = 0;
                	if(predecessors != null){
                		for(int i=0; i < predecessors.size();i++){
                		    ArrayList<HashMap<String, Object>> predecessorStationList = getLastStationList(predecessors.get(i).getProperty(SDVPropertyConstant.BL_ITEM_ID));
                		    for(int j = 0; j < predecessorStationList.size(); j++) {
                		        createTableItem(rowNum, predecessors.get(i), predecessorStationList.get(j), currentChildren);
                		        rowNum++;
                		    }
                		}
                	}
                	return;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        text.setText("");
	}
	
	
    @SuppressWarnings({ "unchecked" })
    public ArrayList<HashMap<String, Object>> getLastStationList(String itemID) throws Exception{
        ArrayList<HashMap<String, Object>> searchResultData = null;
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
        com.kgm.common.remote.DataSet ds = new com.kgm.common.remote.DataSet();
        ds.put("STATION_ID", itemID);

        searchResultData = (ArrayList<HashMap<String, Object>>) remoteUtil.execute("com.kgm.service.BopPertService", "selectBopStationPertList", ds);

        for(int i = searchResultData.size()-1; i >= 0; i--) {
            if(searchResultData.get(i).get("SUCCESSORS") != null) {
                searchResultData.remove(i);
            }
        }        
        
        return searchResultData;
    }

	/**
	 *
	 * @method createTableItem
	 * @date 2014. 1. 3.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws TCException
	 * @throws
	 * @see
	 */
	private void createTableItem(int row, TCComponentBOPLine lineBOPLine, HashMap<String, Object> stationBOPMap, List<TCComponentBOPLine> currentChildren) throws TCException {

		TCComponentItemRevision lineRevision = lineBOPLine.getItemRevision();
		String stationRevPuid = (String) stationBOPMap.get("REV_PUID");
		TCComponentItemRevision stationRevision = (TCComponentItemRevision) currentBOPLine.getSession().stringToComponent(stationRevPuid);
		String stationCode = stationRevision.getProperty(SDVPropertyConstant.STATION_STATION_CODE);
		
		TableItem tableItem   = new TableItem(tableSucessors, row);
		tableItem.setText(SUCESSOR_IDX_COLUMN, String.valueOf(row+1));
		tableItem.setText(SUCESSOR_ID_COLUMN, lineRevision.getStringProperty(SDVPropertyConstant.ITEM_ITEM_ID));
		tableItem.setText(SUCESSOR_NAME_COLUMN, lineRevision.getStringProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
		tableItem.setText(SUCESSOR_STATION_COLUMN, stationCode);
		addSucessorEditor(tableItem, stationRevision, currentChildren);
	}

	/**
	 *
	 * @method addSucessorEditor
	 * @date 2014. 1. 6.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws TCException
	 * @throws
	 * @see
	 */
	@SuppressWarnings("unchecked")
	protected void addSucessorEditor(TableItem tableItem, TCComponentItemRevision stationRevision, List<TCComponentBOPLine> currentChildren) {

		if(currentChildren == null || currentChildren.size() == 0){
			tableItem.setText(DECESSOR_COLUMN, EDITOR_EMPTY_SELECTION);
			return;
		}
		try{
			TCComponentBOPLine childSucessor = findChildSucessor(stationRevision, currentChildren);
			tableItem.setData("originalValue", (childSucessor == null?null:childSucessor.toDisplayString()));
			tableItem.setData("targetItemRevision", stationRevision);

			TableEditor editor = new TableEditor(tableSucessors);
			CCombo control = new CCombo(tableSucessors, SWT.NONE);
			control.setData("tableItem", tableItem);
			control.addSelectionListener(this);
			control.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
			control.setEditable(false);

			control.add(EDITOR_EMPTY_SELECTION);
			control.select(0);
			for(TCComponentBOPLine child : currentChildren){
				control.add(child.toDisplayString());
				control.setData(child.toDisplayString(), child.getItemRevision());

				if(childSucessor != null && child.getObjectString().equals(childSucessor.getObjectString())){
					int selectedIndex = control.getItemCount() -1;
					control.select(selectedIndex);
					control.setData("previousSelectedIndex", selectedIndex);
				}
			}
			control.pack();
			editor.grabHorizontal = true;
			editor.setEditor(control, tableItem, DECESSOR_COLUMN);
			if (tableItem.getData("editors") == null) {
				HashMap<Integer, TableEditor> editors = new HashMap<Integer, TableEditor>();
				tableItem.setData("editors", editors);
			}
			((HashMap<Integer, TableEditor>) tableItem.getData("editors")).put(DECESSOR_COLUMN, editor);

			tableItem.addDisposeListener(new DisposeListener() {
				@Override
				public void widgetDisposed(DisposeEvent e) {
					TableItem item = (TableItem) e.widget;
					HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>) item.getData("editors");
					if(editors != null) {
						for(Integer key : editors.keySet()) {
							editors.get(key).getEditor().dispose();
							editors.get(key).dispose();
						}
					}
				}
			});
		}catch(Exception ex){
			logger.error(ex);
			tableItem.setText(DECESSOR_COLUMN, EDITOR_EMPTY_SELECTION);
		}

	}

	/**
	 *
	 * @method findChildSucessor
	 * @date 2014. 1. 3.
	 * @author CS.Park
	 * @param
	 * @return TCComponentBOPLine
	 * @throws TCException
	 * @throws
	 * @see
	 */
	private TCComponentBOPLine findChildSucessor(TCComponentItemRevision itemRevision, List<TCComponentBOPLine> currentChildren) throws TCException {
		for(TCComponentBOPLine child : currentChildren){
			TCComponent[] extDecessors = child.getItemRevision().getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
			if(extDecessors != null && extDecessors.length > 0){
				TCComponentItemRevision  sucessorItemRev = itemRevision;
				for(TCComponent comp : extDecessors){
					if(comp.getObjectString().equals(sucessorItemRev.getObjectString())){
						return child;
					}
				}
			}

		}
		return null;
	}

	/**
	 *
	 * @method getChildrenList
	 * @date 2014. 1. 3.
	 * @author CS.Park
	 * @param
	 * @return List<TCComponentBOPLine>
	 * @throws TCException
	 * @see
	 */
	private List<TCComponentBOPLine> getChildrenList(TCComponentBOPLine parentBOPLine) throws TCException {

		List<TCComponentBOPLine> childrenList = new ArrayList<TCComponentBOPLine>();

		if(parentBOPLine != null && parentBOPLine.getChildrenCount() > 0){
			for(AIFComponentContext context : parentBOPLine.getChildren()){
				if(context.getComponent() instanceof TCComponentBOPLine){
					childrenList.add((TCComponentBOPLine)context.getComponent());
				}
			}
		}
		return childrenList;
	}

	@SuppressWarnings("unchecked")
	public void reset(){
		if(this.isDirty && showConfirmDialog("Reset", "Do you want to restore and canceled changed!")){
			try{
    			for(TableItem tableItem : this.tableSucessors.getItems()){
    				CCombo editorControl = null;
    				HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>)tableItem.getData("editors");

    				//DECESSOR_COLUMN = 3 : �޺� Editor �÷��� INDEX�� �ǹ��Ѵ�.
    			    if(editors != null) {
    			    	editorControl = (CCombo)editors.get(DECESSOR_COLUMN).getEditor();
    			    }
    			    //�޺��ڽ��� ���ٴ� ���� ���� �ڽ� ������ ���ٴ� �ǹ��̹Ƿ� ���� ������ �� �������Ƿ� �� �̻��� üũ�� ������.
    			    if(editorControl == null) continue;

    			    //�������� ���� ���氪�� ���Ͽ� ����� ���� ã�� ���� ������ �����´�.
    				String originalValue = (String)tableItem.getData("originalValue");

    				//�� �� ǥ�� ���ڸ�  null�� ��ü�Ͽ� ��
    				originalValue = (originalValue != null && originalValue.equals(EDITOR_EMPTY_SELECTION))? null: originalValue;
    				int originalSelection = 0;
    				if(originalValue != null){
    					String [] items = editorControl.getItems();
    					for(int i=0; i <items.length; i++){
    						if(items[i].equals(originalValue)){
    							originalSelection = i;
    							break;
    						}
    					}
    				}
    				editorControl.select(originalSelection);
    			}
			}catch(Exception ex){
				logger.error(ex);
				return;
			}
			this.isDirty = false;
			setToolbarState(this.isDirty);
		}
	}

	@SuppressWarnings("unchecked")
	public void saveExtDecessors(){

		if(this.isDirty && showConfirmDialog("Save", "Do you want to save to changed!")){
			try{
			for(TableItem tableItem : this.tableSucessors.getItems()){
				CCombo editorControl = null;
				HashMap<Integer, TableEditor> editors = (HashMap<Integer, TableEditor>)tableItem.getData("editors");

				//3 : �޺� Editor �÷��� INDEX�� �ǹ��Ѵ�.
			    if(editors != null) {
			    	editorControl = (CCombo)editors.get(DECESSOR_COLUMN).getEditor();
			    }
			    //�޺��ڽ��� ���ٴ� ���� ���� �ڽ� ������ ���ٴ� �ǹ��̹Ƿ� ���� ������ �� �������Ƿ� �� �̻��� üũ�� ������.
			    if(editorControl == null) continue;

			    //������ ����Station�� BOP������ �����´�.
			    TCComponentItemRevision  sucessorItemRev = (TCComponentItemRevision)tableItem.getData("targetItemRevision");
			    //�������� ���� ���氪�� ���Ͽ� ����� ���� ã�� ���� ������ �����´�.
				String originalValue = (String)tableItem.getData("originalValue");
				String currentValue = (String)editorControl.getItem(editorControl.getSelectionIndex());

				//�� �� ǥ�� ���ڸ�  null�� ��ü�Ͽ� ��
				originalValue = (originalValue != null && originalValue.equals(EDITOR_EMPTY_SELECTION))? null: originalValue;
				currentValue  = (currentValue  != null &&  currentValue.equals(EDITOR_EMPTY_SELECTION))? null: currentValue;


				if(originalValue == null && currentValue != null){
					//add
					updateReferenceArrayProperty((TCComponentItemRevision)editorControl.getData(currentValue), SDVPropertyConstant.ME_EXT_DECESSORS,  sucessorItemRev, ADD);
				}else if(originalValue != null && currentValue == null ){
					//delete
					updateReferenceArrayProperty((TCComponentItemRevision)editorControl.getData(originalValue), SDVPropertyConstant.ME_EXT_DECESSORS,  sucessorItemRev, DELETE);
				}else if(originalValue != null && currentValue != null ){
				    if (!originalValue.equals(currentValue)) {
				        //change( add & delete)
				        updateReferenceArrayProperty((TCComponentItemRevision)editorControl.getData(originalValue), SDVPropertyConstant.ME_EXT_DECESSORS,  sucessorItemRev, DELETE);
				        updateReferenceArrayProperty((TCComponentItemRevision)editorControl.getData(currentValue), SDVPropertyConstant.ME_EXT_DECESSORS,  sucessorItemRev, ADD);
                    }
				}else{
					//path
				}
			}
			}catch(TCException tex){
				logger.error(tex);
				showErrorDialog("Save Error", "Error occuered duaring save!", tex);
				return;
			}catch(Exception ex){
				logger.error(ex);
				showErrorDialog("Save Error", "Error occuered duaring save!", ex);
				return;
			}

			this.isDirty = false;
			setToolbarState(this.isDirty);
		}
	}

	/**
	 *
	 * @method updateReferenceArrayProperty
	 * @date 2014. 1. 4.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	protected void updateReferenceArrayProperty(TCComponentItemRevision decessorItemRev, String propertyName, TCComponentItemRevision sucessorItemRev, int mode) throws TCException {
		if(decessorItemRev != null){
			TCProperty property = decessorItemRev.getTCProperty(propertyName);
			if(property == null) return;
			//���������� �ִ��� ����Ȯ��
			if(decessorItemRev.isModifiable(propertyName)){
    			TCComponent [] values = property.getReferenceValueArray();
    			if(values == null) values = new TCComponent[0];
    			switch (mode) {
    				case ADD: 		values = (TCComponent[]) ArrayUtils.add(values, sucessorItemRev);
    					break;
    				case DELETE :	values = (TCComponent[]) ArrayUtils.removeElement(values, sucessorItemRev);
    					break;
    			}
    			property.setReferenceValueArray(values);

    			//Save�� �ϸ� Lock�� �߻��Ͽ� save()�� ���� ����
    			//decessorItemRev.save();
			}
		}
	}

	/**
	 *
	 * @method showConfirmDialog
	 * @date 2014. 1. 4.
	 * @author CS.Park
	 * @param
	 * @return boolean
	 * @throws
	 * @see
	 */
	protected boolean showConfirmDialog(String title, String message) {
		MessageBox msgBox = new MessageBox(this.getShell(),SWT.OK | SWT.CANCEL | SWT.ICON_QUESTION);
		msgBox.setText(title);
		msgBox.setMessage(message);
		int result = msgBox.open();
		return result == SWT.OK;
	}

	protected void showErrorDialog(String title, String message, Exception ex) {
		MessageBox msgBox = new MessageBox(this.getShell(),SWT.OK | SWT.ICON_ERROR);
		msgBox.setText(title);
		if (ex != null) {
		    msgBox.setMessage(message + "\n\n" +  ex.getLocalizedMessage());
        }else{
            msgBox.setMessage(message + "\n\n");
        }
		msgBox.open();
	}

	protected void setToolbarState(boolean enable){
		for(IButtonInfo buttonInfo : this.getActionToolButtons().values()){
			if(buttonInfo != null){
				buttonInfo.getButton().setEnabled(enable);
			}
		}
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.sdv.core.ui.view.AbstractSDVViewPane#uiLoadCompleted()
	 */
	@Override
	public void uiLoadCompleted() {
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		CCombo editorControl = (CCombo)e.widget;
		int newSelectedIndex = editorControl.getSelectionIndex();
		int oldSelectedIndex = 0;

		if (editorControl.getData("previousSelectedIndex") != null) {
		    oldSelectedIndex = (Integer)editorControl.getData("previousSelectedIndex");
        }

		String selectedItem = editorControl.getItem(newSelectedIndex);

		try{
			TCComponentItemRevision itemRev = (TCComponentItemRevision)editorControl.getData(selectedItem);
    		if(itemRev != null && (!itemRev.isModifiable(SDVPropertyConstant.ME_EXT_DECESSORS) || !CustomUtil.isWritable(itemRev))){
    			//������ ���� ��� ������ �ٽ� �����Ѵ�.
    			showErrorDialog("No Write Access", selectedItem + " is not modifiable. \n\nCan't change to it", null);
    			editorControl.select(oldSelectedIndex);
    		}else{
    		    //���� ������ �����Ѵ�.
    		    editorControl.setData("previousSelectedIndex", newSelectedIndex);
    		    if(!this.isDirty){
    		        this.isDirty = true;
    		        setToolbarState(this.isDirty);
    		    }
    		}
		}catch(Exception ex){
			logger.error(ex);
			showErrorDialog("Error", selectedItem + " is not modifiable. \n\nCan't change to it", ex);
			editorControl.select(oldSelectedIndex);
		}
	}

	/**
	 * Description :
	 * @method :
	 * @date : 2014. 1. 3.
	 * @author : CS.Park
	 * @param :
	 * @return :
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}
}
