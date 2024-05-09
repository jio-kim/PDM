/**
 * 
 */
package com.symc.plm.me.sdv.view.common;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Panel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.AbstractSDVSWTDialog;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.ReviseInitOperation;
import com.symc.plm.me.sdv.view.meco.MecoSelectView;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.common.TCTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * 
 * [SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
 * [CF-3537] [20230131]isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경   
 * Class Name : ReviseView
 * Class Description : 
 * @date 2013. 11. 15.
 *
 */
public class ReviseView extends AbstractSDVViewPane {
    private TCSession session;
    private TCTable table;
    private boolean isWeldOPOnlyMEW = false;
    private ArrayList<InterfaceAIFComponent> reviseTargetList = new ArrayList<InterfaceAIFComponent>();

    /**
     * @param parent
     * @param style
     * @param id
     */
    public ReviseView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.ui.view.AbstractSDVViewPane#initUI()
     */
    @Override
    protected void initUI(Composite parent) {
        session = CustomUtil.getTCSession();
        registry = Registry.getRegistry(ReviseView.class);

//        setLayout(new FillLayout(SWT.HORIZONTAL));
        
        Group reviseListGroup = new Group(parent, SWT.NONE);
        reviseListGroup.setText("Revise List");
        reviseListGroup.setLayout(new swing2swt.layout.BorderLayout(0, 0));
        
        Composite reviseListComp = new Composite(reviseListGroup, SWT.EMBEDDED);
        
        Frame frame = SWT_AWT.new_Frame(reviseListComp);
        
        Panel panel = new Panel();
        frame.add(panel);
        panel.setLayout(new BorderLayout(0, 0));
        
        JRootPane rootPane = new JRootPane();
        panel.add(rootPane);

        String[] columnNames = new String[]{SDVPropertyConstant.ITEM_ITEM_ID, SDVPropertyConstant.ITEM_REVISION_ID, SDVPropertyConstant.ITEM_OBJECT_NAME, SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST};
        table = new TCTable(session, columnNames);//"ItemRevision_ColumnPreferences", "ItemRevision_ColumnWidthPreferences");
        table.setColumnWidths(new String[]{"24", "8", "30", "15"});
        
        JScrollPane scrollPane = new JScrollPane();
        rootPane.getContentPane().add(scrollPane, BorderLayout.CENTER);
        scrollPane.getViewport().add(table);

        table.setEditable(false);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        table.addEmptyRow();
    }

    /**
     * @param paramters
     *            the parameters to set
     */
    @Override
    public void setParameters(Map<String, Object> parameters) {
    	if (parameters != null)
    	{
    	}
    }

	@Override
	public void setLocalDataMap(IDataMap dataMap) {
	}

	@Override
	public IDataMap getLocalDataMap() {
		return getLocalSelectDataMap();
	}

	@Override
	public IDataMap getLocalSelectDataMap() {
		RawDataMap reviseData = new RawDataMap();

		if (table.getRowCount() == 0)
			return reviseData;

		try {
			ArrayList<String> checkPlantTypes = new ArrayList<String>();
			checkPlantTypes.addAll(Arrays.asList(registry.getStringArray("NeedToNotMECO.TYPE")));
			String itemType;
			if (reviseTargetList.size() > 0 && reviseTargetList.get(0) instanceof TCComponentItemRevision)
				itemType = ((TCComponentItemRevision) reviseTargetList.get(0)).getItem().getType();
			else if (reviseTargetList.size() > 0 && reviseTargetList.get(0) instanceof TCComponentBOMLine)
				itemType = ((TCComponentBOMLine) reviseTargetList.get(0)).getItem().getType();
			else
				itemType = "";
			if (checkPlantTypes.contains(itemType))
				reviseData.put("SkipMECO", true, IData.BOOLEAN_FIELD);
			else
				reviseData.put("SkipMECO", false, IData.BOOLEAN_FIELD);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}


		reviseData.put(getId(), reviseTargetList == null || reviseTargetList.size() == 0 ? null : reviseTargetList, IData.LIST_FIELD);

		return reviseData;
	}

	@Override
	public Composite getRootContext() {
		return null;
	}

	@Override
	public AbstractSDVInitOperation getInitOperation() {
		return new ReviseInitOperation();
	}

	@Override
	public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null) {
                Object targetObjects = dataset.getValue("TargetObjects");
                ArrayList<String> checkPlantTypes = new ArrayList<String>();
                checkPlantTypes.addAll(Arrays.asList(registry.getStringArray("NeedToNotMECO.TYPE")));

                Object mecoObject = dataset.getValue("MECOObject");

                try {
                	boolean isNotNeedMECOType = false;
                	boolean isAnotherItemType = false;
                	ArrayList<?> targetObjectList = (ArrayList<?>) targetObjects;
                	String targetItemType = null;
                	String prevItemType = null;

    				HashMap<String, Object> dataMap = new HashMap<String, Object>();
                	AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
            		IViewPane mecoView = dialog.getView("reviseMecoView");
            		
                	for (int i = 0; i < targetObjectList.size(); i++)
                	{
                		if (targetObjectList.get(i) instanceof TCComponentItemRevision)
                			targetItemType = ((TCComponentItemRevision) targetObjectList.get(i)).getItem().getType();
                		else if (targetObjectList.get(i) instanceof TCComponentBOMLine)
                			targetItemType = ((TCComponentBOMLine) targetObjectList.get(i)).getItem().getType();
                		else
                			return;

                		if (prevItemType == null)
                			prevItemType = targetItemType;
                		
                		if (checkPlantTypes.contains(targetItemType))
                		{
                			isNotNeedMECOType = true;

                			if (! prevItemType.equals(targetItemType))
                    			isAnotherItemType = true;
                		}
                		else if (targetItemType.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
                		{
                			TCComponent targetOP;
                			if (targetObjectList.get(i) instanceof TCComponentItemRevision)
                				targetOP = ((TCComponentItemRevision) targetObjectList.get(i)).getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
                			else
                				targetOP = ((TCComponentBOPLine) targetObjectList.get(i)).getItemRevision().getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
                			
                			if (! SYMTcUtil.isReleased(targetOP))
                			{
                				String targetMECO = targetOP.getProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
                				TCComponentItem mecoItem = SYMTcUtil.findItem(session, targetMECO);
                				
                				dataMap.put(SDVTypeConstant.MECO_ITEM_REV, mecoItem.getLatestItemRevision());
                			}
                			else
                			{
                				//isWeldOPOnlyMEW = true;
                			}

                    		if (! prevItemType.equals(targetItemType)) {
                                isAnotherItemType = true;
                    		} else {
                    		    //[SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
                    		    isWeldOPOnlyMEW = true;
                    		}
                    			
                		}
                	}
                	//[SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
                	//if ((isAnotherItemType && isNotNeedMECOType) || (isAnotherItemType && isWeldOPOnlyMEW))
                	if ((isAnotherItemType && isNotNeedMECOType))
                	{
                		throw new Exception("Please select same type.");
                	}
                	
                	//[SR141208-036] [20150203] shcho, 용접점의 추가 삭제에 의해 일반공법 변경 사유 발생 시 MEP와 MEW용 MECO를 별개로 발행해야만 하는 것을 하나의 MEP에서 할 수 있도록 변경
                	//if (mecoObject != null && isWeldOPOnlyMEW)
                		//throw new Exception("Please select only WeldOperation or remove selected WeldOperation.");
                	
                	if (mecoObject != null && isNotNeedMECOType)
                		throw new Exception("Please select only BOP Item or remove selected BOP Item.");

                	if (mecoObject != null && dataMap.size() > 0 && ! mecoObject.equals(dataMap.get(SDVTypeConstant.MECO_ITEM_REV)))
                		throw new Exception("Please select same MECO contact BOP Item.");

                	if (mecoObject != null && dataMap.size() == 0)
                		dataMap.put(SDVTypeConstant.MECO_ITEM_REV, mecoObject);

                	if (isNotNeedMECOType)
                	    /* [CF-3537] [20230131] 기존 검색 화면에서 반려된 MECO가 검색 안되는 문제가 있어서 아래 내용으로 수정 
                	    isWorkingStatus와 반려된 MECO도 나올 수 있게 수정 기존 SearchTypeItemView에서 MecoSearchView 검색창으로 변경 */
//                		((SelectedMECOView) mecoView).setAlternative(false);
                		((MecoSelectView) mecoView).setAlternative(false);

                	if (dataMap.keySet().size() > 0)
                		mecoView.setParameters(dataMap);

                	if (targetObjects != null && targetObjects instanceof ArrayList<?>)
                	{
                		ArrayList<TCComponentItemRevision> tableList = new ArrayList<TCComponentItemRevision>();
                		for (int i = 0; i < targetObjectList.size(); i++)
                		{
                			reviseTargetList.add((InterfaceAIFComponent) targetObjectList.get(i));

                			if (targetObjectList.get(i) instanceof TCComponentItemRevision)
                				tableList.add((TCComponentItemRevision) targetObjectList.get(i));
                			else if (targetObjectList.get(i) instanceof TCComponentBOMLine)
                				tableList.add(((TCComponentBOMLine) targetObjectList.get(i)).getItemRevision());
                		}

                		table.removeAllRows();
        	        	table.addRows(tableList.toArray(new TCComponentItemRevision[0]));
                	}
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

	}

	@Override
	public void uiLoadCompleted() {
	}

	public boolean isWeldOPOnlyMECOMEW()
	{
		return isWeldOPOnlyMEW;
	}
}
