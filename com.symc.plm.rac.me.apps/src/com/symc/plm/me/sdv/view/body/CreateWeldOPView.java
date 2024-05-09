package com.symc.plm.me.sdv.view.body;

import java.util.HashMap;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
import org.sdv.core.util.UIUtil;

import com.kgm.common.utils.CustomUtil;
import com.symc.plm.me.common.SDVLOVComboBox;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVText;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.body.CreateWeldOPInitOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR140702-044][20140702] jwLee �������� IDü�� ���� (1. LOV�߰�, 2. Serial No.ü�� ����, 3. �������� �ߺ� �˻� �ҽ� �̵�)
 * [SR150416-014][20150416] shcho, �������� ������ ID�� �� Serial No.�� ����ڰ� ���Ƿ� ������ ���� �ϵ��� ����
 * [SR150528-006][20150527] shcho, �������� ������ MECO ���� ǥ�� ���� ���ϴ� ���� ����(gunlist�� ���� ��� �޽����� ��쵵�� ������)
 * [SR150528-007][20150527] shcho, �������� ������ Relesed �� MECO ���� ����� �� �־��� ���� ���� (������ Released �� ��� ������ Revise�ϵ��� ��� �޽��� ���)
 * [SR150524-002][20150730] shcho, �������� �����۾� �ð� ���� �ҿ� ���� 
 * [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
 */
public class CreateWeldOPView extends AbstractSDVViewPane {
    private SDVText operationText;
    private SDVText serialNotext;
    private Combo gunCombo;
    private SDVLOVComboBox weldOptionCombo;

    private IDataSet localDataSet;
    private IDataMap curDataMap;

    private String altPrefix;
    private boolean isAlt;

    //[SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
    protected TCComponentBOPLine initedOperationBOPLine;
    
    private TCComponentBOPLine operationBOPLine;
    private List<TCComponentBOPLine> gunBOPLineList;
    private SelectedMECOView mecoView = null;

    private Registry registry;

    public CreateWeldOPView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    @Override
    protected void initUI() {
    }

    @Override
    protected void initUI(Composite parent) {

        registry = Registry.getRegistry(CreateBodyOPView.class);

        Group grpWeldOP = new Group(parent, SWT.NONE);
        grpWeldOP.setText(registry.getString("CreateWeldDialog.Weld.Infomation"));
        grpWeldOP.setBounds(0, 0, 430, 161);

        Label lblTargetop = new Label(grpWeldOP, SWT.NONE);
        lblTargetop.setBounds(10, 27, 71, 20);
        lblTargetop.setText(registry.getString("CreateWeldDialog.Weld.Associated") + " : ");

        operationText = new SDVText(grpWeldOP, SWT.BORDER | SWT.SINGLE);
        operationText.setBounds(90, 24, 317, 28);
        operationText.setEditable(false);

        Label lblSerealNo = new Label(grpWeldOP, SWT.NONE);
        lblSerealNo.setBounds(236, 73, 74, 20);
        lblSerealNo.setText(registry.getString("CreateWeldDialog.Weld.SerealNO"));

        serialNotext = new SDVText(grpWeldOP, SWT.BORDER | SWT.SINGLE);
        GridData gd_txtSirialNO = new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1);
        gd_txtSirialNO.widthHint = 100;
        serialNotext.setLayoutData(gd_txtSirialNO);
        serialNotext.setBounds(316, 70, 91, 26);

        Label lblGunNo = new Label(grpWeldOP, SWT.NONE);
        lblGunNo.setBounds(10, 73, 71, 20);
        lblGunNo.setText(registry.getString("CreateWeldDialog.Weld.GunNO"));

        Label lblGunOption = new Label(grpWeldOP, SWT.NONE);
        lblGunOption.setBounds(10, 112, 71, 20);
        lblGunOption.setText(registry.getString("CreateWeldDialog.Weld.Option") + " : ");

        weldOptionCombo = new SDVLOVComboBox(grpWeldOP, "M7_BOPB_WELD_OPTION_CODE");
        weldOptionCombo.setMandatory(true);
        weldOptionCombo.setBounds(90, 112, 114, 28);
        weldOptionCombo.setFixedHeight(true);

        gunCombo = new Combo(grpWeldOP, SWT.NONE);
        gunCombo.setBounds(90, 70, 114, 28);

        serialNotext.setMandatory(true);
        serialNotext.setEnabled(true); //[SR150416-014][20150416] shcho, �������� ������ ID�� �� Serial No.�� ����ڰ� ���Ƿ� ������ ���� �ϵ��� ����
        serialNotext.setBackground(getShell().getBackground());

        //serealNOtext.setInputType(SDVText.NUMERIC);
        //serealNOtext.setTextLimit(2);
        
        // [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
        // ������ ���� BOPLine �� �����´�.
        MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
        initedOperationBOPLine = (TCComponentBOPLine) mfgApp.getSelectedBOMLines()[0];
    }

    
    /**
     *
     * @method changeGunComboBox
     * @date 2013. 12. 2.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setGunComboBox(List<TCComponentBOPLine> gunList, List<TCComponentBOPLine> workAreaList){
        if (gunList == null || gunList.isEmpty()){
            gunCombo.removeAll();
            String empty[] = {registry.getString("CreateWeldDialog.Weld.Massage2")};
            gunCombo.setItems(empty);
            gunCombo.select(0);
        }else{
            String gunItem[] = new String[gunList.size()];
            for (int i = 0; i < gunList.size(); i++){
                try {
                    String workArea = getWorkAreaId(workAreaList.get(i).getProperty(SDVPropertyConstant.BL_ITEM_ID));
                    gunItem[i] = workArea + " / " + getGunNO(gunList.get(i).getProperty(SDVPropertyConstant.BL_ITEM_ID));
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }
            gunCombo.removeAll();
            gunCombo.setItems(gunItem);
            gunCombo.select(0);
        }
    }

    /**
     *  �ʱ� View �� �� �����͸� �����Ѵ�
     *
     * @method initViewData
     * @date 2013. 12. 2.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private void initViewData(IDataSet dataset){
        localDataSet = dataset;

        setMecoNO((TCComponentItemRevision) localDataSet.getData("mecoItemRevision"));
        
        operationBOPLine = (TCComponentBOPLine) localDataSet.getData("operationBOPLine");
        operationText.setText(operationBOPLine.toDisplayString());
        
        gunBOPLineList = (List<TCComponentBOPLine>)localDataSet.getData("gunBOPLineList");
        List<TCComponentBOPLine> workAreaList = (List<TCComponentBOPLine>)localDataSet.getData("workAreaList");
        setGunComboBox(gunBOPLineList, workAreaList);
        
        serialNotext.setText((String) localDataSet.getData("weldOPSerealNo"));

    }

    /**
     * GunItemId �� ������ GunNO �� �����Ѵ�
     *
     * @method getGunNO
     * @date 2013. 12. 2.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getGunNO(String gunItemID){
        String gunIDs[] = gunItemID.split("-");
        String gunNO = "";
        if (gunIDs.length > 3){
            for (int i = 3; i < gunIDs.length; i++){
                gunNO += gunIDs[i];
                if (gunIDs.length == (i + 1)) {
                    break;
                }
                gunNO += "-";
            }
        }else{
            gunNO = gunItemID;
        }
        return gunNO;
    }

    private String getWorkAreaId(String workAreaItemID){
        String workAreaID[] = workAreaItemID.split("-");
        String workNO;
        if (workAreaID.length > 3){
            workNO = workAreaID[3];
        }else{
            workNO = workAreaItemID;
        }
        return workNO;
    }

    /**
     * [SR150528-007][20150527] shcho, �������� ������ Relesed �� MECO ���� ����� �� �־��� ���� ���� (������ Released �� ��� ������ Revise�ϵ��� ��� �޽��� ���)
     *
     *
     * @method setMecoNO
     * @date 2013. 12. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void setMecoNO(TCComponentItemRevision mecoRevision){
        AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
        HashMap<String, Object> dataMap = new HashMap<String, Object>();

        if (mecoRevision != null) {
            try {
                if (CustomUtil.isReleased(mecoRevision)) {
                    MessageBox.post(getShell(), "Operation is released. \nPlease revise operation first.", "WARNING", MessageBox.WARNING);
                } else {
                    dataMap.put(SDVTypeConstant.MECO_ITEM_REV, mecoRevision);
                }
            } catch (TCException e) {
                e.printStackTrace();
            }
        }

        if (mecoView == null) {
            mecoView = (SelectedMECOView) dialog.getView("searchMECO");
        }

        mecoView.setParameters(dataMap);
    }


    /**
     * �ۼ��� ����� Data ����
     *
     * @method saveData
     * @date 2013. 11. 20.
     * @param
     * @return Map<String,Object>
     * @exception
     * @throws
     * @see
     */
    private IDataMap saveData(){

        RawDataMap savedDataMap = new RawDataMap();

        String serialNo = serialNotext.getText();

        TCComponentBOPLine gunBOPLine = null;
        if(!gunCombo.getText().equals(registry.getString("CreateWeldDialog.Weld.Massage2"))){
            gunBOPLine = gunBOPLineList.get(gunCombo.getSelectionIndex());
        }

        savedDataMap.put("weldOpOption", weldOptionCombo.getSelectedString(), IData.STRING_FIELD);
        savedDataMap.put("serialNO", serialNo, IData.STRING_FIELD);
        savedDataMap.put("targetOP", operationBOPLine, IData.OBJECT_FIELD);
        savedDataMap.put("gunID", gunBOPLine, IData.OBJECT_FIELD);
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, isAlt, IData.BOOLEAN_FIELD);
        savedDataMap.put(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, altPrefix, IData.STRING_FIELD);

        return savedDataMap;
    }

    /**
     *
     *
     * @method refreshInit
     * @date 2014. 1. 8.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void refreshInit() throws Exception{
    	// [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
        CreateWeldOPInitOperation createWeldInitOp = new CreateWeldOPInitOperation();
        if(initedOperationBOPLine!=null){
        	createWeldInitOp.setInitedOperationBOPLine(initedOperationBOPLine);
        }
        createWeldInitOp.applyAction(true);

        localDataSet = getApplyDataSet();

        initViewData(localDataSet);
    }


    @Override
    public void setLocalDataMap(IDataMap dataMap) {
    }

    @Override
    public IDataMap getLocalDataMap() {
        return this.curDataMap;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        curDataMap = saveData();
        return curDataMap;
    }

    @Override
    public Composite getRootContext() {
        return null;
    }


    @Override
    public AbstractSDVInitOperation getInitOperation() {
    	// [SR151211-006][20151224] taeku.jeong ���������� �������� �����ϴ� Apply Button�� �̿�� TargetOperation�� ���� �Ǵ� ���� �ذ�
        CreateWeldOPInitOperation createWeldInitOp = new CreateWeldOPInitOperation();
        if(initedOperationBOPLine!=null){
        	createWeldInitOp.setInitedOperationBOPLine(initedOperationBOPLine);
        }
        return createWeldInitOp;
    }


    @Override
    public void initalizeData(int result, IViewPane owner, IDataSet dataset)  {
        // Operation ��� �� ������ ComboBox �����
        if (result == SDVInitEvent.INIT_SUCCESS) {
            if (dataset != null){
                AbstractSDVSWTDialog dialog = (AbstractSDVSWTDialog) UIManager.getCurrentDialog();
                isAlt = (Boolean) dataset.getData(SDVPropertyConstant.STATION_IS_ALTBOP);
                altPrefix = (String) dataset.getData(SDVPropertyConstant.STATION_ALT_PREFIX);

                if (mecoView == null){
                    mecoView = (SelectedMECOView) dialog.getView("searchMECO");
                }

                if (isAlt){
                    dialog.setAddtionalTitle("Alternative BOP");
                    dialog.setTitleBackground(UIUtil.getColor(SWT.COLOR_DARK_RED));
                    mecoView.setAlternative(false);
                } else{
                    mecoView.visibleSearchBtn(false);
                }

                initViewData(dataset);
            }
        }
    }



    public void setApplyDataSet(IDataSet dataSet) {
        this.localDataSet = dataSet;
    }

    public IDataSet getApplyDataSet() {
        return this.localDataSet;
    }

    @Override
    public void uiLoadCompleted() {
        //
    }


    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

    }

}
