package com.symc.plm.me.sdv.view.ps;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.common.exception.SDVRuntimeException;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;
import org.sdv.core.util.UIUtil;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetUtils;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.Registry;

/**
 * 
 * [SR141203-038][20141208] shcho, 도장 ParallelLine인 경우 ComboBox에서 서로다른 ParallelLine 식별 불가 오류 수정 (검색 수행시 ParallelLine의 작업표준서는 검색 안되는 오류 포함)
 * [SR150312-024][20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
 *                                  - 결재 공법 조회조건 추가
 */
public class SearchConditionView extends AbstractSDVViewPane {
    private Registry registry;

    private Composite ruleComposite;

    private Text txtProdNo;
    private Text txtShop;
    private Text txtStation;
    private Text txtOpId;
    private Text txtOpName;
    private Text txtOwner;
    private Text txtVariant;
    private Text txtMeco;
    private Text txtPublishUser;

    private Combo cmbLine;

    private Button chkEmpty;
    private Button chkDifferent;
    private Button chkNoRelease;
    private Button chkRelease;    
    
    private String processType;

    private TCComponentBOPLine shopLine;
    // 20210616 [CF-2224] 리비전 룰을 결과에서 확인해야 하는데 방법이 없어 먼저 전역변수저장해두고 나중에 가져가도록 함.
	public String revisionRuleName = "";

    public SearchConditionView(Composite parent, int style, String id) {
        super(parent, style, id);
        registry = Registry.getRegistry(this);
    }

    public SearchConditionView(Composite parent, int style, String id, int configId, String order) {
        super(parent, style, id, configId, order);
        registry = Registry.getRegistry(this);
    }

    @Override
    protected void initUI(Composite parent) {
        parent.setLayout(new FillLayout(SWT.HORIZONTAL));

        // 엔터키 눌렀을 때 창 닫힘 막기
//        Display.getDefault().addFilter(SWT.Traverse, new Listener() {
//
//            @Override
//            public void handleEvent(Event event) {
//                if(SWT.TRAVERSE_RETURN == event.detail) {
//                    event.doit = false;
//                }
//            }
//        });

//        Composite composite = new Composite(parent, SWT.NONE);
//        GridLayout gl_composite = new GridLayout(1, false);
//        gl_composite.verticalSpacing = 0;
//        composite.setLayout(gl_composite);

        Group group = new Group(parent, SWT.NONE);
        GridLayout gl_group = new GridLayout(12, false);
        gl_group.marginBottom = 5;
        gl_group.marginHeight = 0;
        group.setLayout(gl_group);

        ruleComposite = new Composite(group, SWT.NONE);
        GridData gd_ruleComposite = new GridData(SWT.FILL, SWT.CENTER, false, false, 12, 1);
        gd_ruleComposite.heightHint = 30;
        ruleComposite.setLayoutData(gd_ruleComposite);
        ruleComposite.setBackground(UIUtil.getColor(SWT.COLOR_DARK_GRAY));

        try {
            Label lblProdNo = new Label(group, SWT.NONE);
            GridData gd_lblProdNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblProdNo.widthHint = 86;
            lblProdNo.setLayoutData(gd_lblProdNo);
            lblProdNo.setAlignment(SWT.RIGHT);
            lblProdNo.setText("Product No.");

            txtProdNo = new Text(group, SWT.BORDER | SWT.READ_ONLY);
            txtProdNo.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
            GridData gd_txtProdNo = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_txtProdNo.widthHint = 110;
            txtProdNo.setLayoutData(gd_txtProdNo);

            Label lblShop = new Label(group, SWT.NONE);
            GridData gd_lblShop = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblShop.widthHint = 100;
            lblShop.setLayoutData(gd_lblShop);
            lblShop.setAlignment(SWT.RIGHT);
            lblShop.setText("Shop");

            txtShop = new Text(group, SWT.BORDER | SWT.READ_ONLY);
            txtShop.setBackground(UIUtil.getColor(SWT.COLOR_WHITE));
            GridData gd_txtShop = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_txtShop.widthHint = 100;
            txtShop.setLayoutData(gd_txtShop);

            Label lblLine = new Label(group, SWT.NONE);
            GridData gd_lblLine = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblLine.widthHint = 74;
            lblLine.setLayoutData(gd_lblLine);
            lblLine.setAlignment(SWT.RIGHT);
            lblLine.setText("Line");

            cmbLine = new Combo(group, SWT.NONE);
            GridData gd_cmbLine = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_cmbLine.widthHint = 71;
            cmbLine.setLayoutData(gd_cmbLine);

            Label lblStation = new Label(group, SWT.NONE);
            GridData gd_lblStation = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblStation.widthHint = 100;
            lblStation.setLayoutData(gd_lblStation);
            lblStation.setAlignment(SWT.RIGHT);
            lblStation.setText("Station");

            txtStation = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtStation = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
            gd_txtStation.widthHint = 100;
            txtStation.setLayoutData(gd_txtStation);

            Label lblOpId = new Label(group, SWT.NONE);
            GridData gd_lblOpId = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblOpId.widthHint = 60;
            lblOpId.setLayoutData(gd_lblOpId);
            lblOpId.setAlignment(SWT.RIGHT);
            lblOpId.setText("Op. ID");

            txtOpId = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtOpId = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
            gd_txtOpId.widthHint = 103;
            txtOpId.setLayoutData(gd_txtOpId);

            Label lblOpName = new Label(group, SWT.NONE);
            GridData gd_lblOpName = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblOpName.widthHint = 70;
            lblOpName.setLayoutData(gd_lblOpName);
            lblOpName.setAlignment(SWT.RIGHT);
            lblOpName.setText("Op. Name");

            txtOpName = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtOpName = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_txtOpName.widthHint = 135;
            txtOpName.setLayoutData(gd_txtOpName);

            Label lblVariant = new Label(group, SWT.NONE);
            GridData gd_lblVariant = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblVariant.widthHint = 67;
            lblVariant.setLayoutData(gd_lblVariant);
            lblVariant.setAlignment(SWT.RIGHT);
            lblVariant.setText("Option");

            txtVariant = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtVariant = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_txtVariant.widthHint = 119;
            txtVariant.setLayoutData(gd_txtVariant);

            Label lblOwner = new Label(group, SWT.NONE);
            GridData gd_lblOwner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblOwner.widthHint = 100;
            lblOwner.setLayoutData(gd_lblOwner);
            lblOwner.setAlignment(SWT.RIGHT);
            lblOwner.setText("MECO Owner");

            txtOwner = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtOwner = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_txtOwner.widthHint = 100;
            txtOwner.setLayoutData(gd_txtOwner);

            Label lblMeco = new Label(group, SWT.NONE);
            GridData gd_lblMeco = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblMeco.widthHint = 74;
            lblMeco.setLayoutData(gd_lblMeco);
            lblMeco.setAlignment(SWT.RIGHT);
            lblMeco.setText("MECO ID");

            txtMeco = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtMeco = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_txtMeco.widthHint = 71;
            txtMeco.setLayoutData(gd_txtMeco);

            Label lblPublishUser = new Label(group, SWT.NONE);
            GridData gd_lblPublishUser = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_lblPublishUser.widthHint = 100;
            lblPublishUser.setLayoutData(gd_lblPublishUser);
            lblPublishUser.setAlignment(SWT.RIGHT);
            lblPublishUser.setText("Publish User");

            txtPublishUser = new Text(group, SWT.BORDER | SWT.SINGLE);
            GridData gd_txtPublishUser = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
            gd_txtPublishUser.widthHint = 100;
            txtPublishUser.setLayoutData(gd_txtPublishUser);

            chkNoRelease = new Button(group, SWT.CHECK);
            chkNoRelease.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            chkNoRelease.setText("미결재 공법");
            if(getConfigId() == 0) {
                chkNoRelease.setToolTipText("Working중 또는 결재가 진행중인 공법");
            } else {
                chkNoRelease.setToolTipText("작업표준서 Publish는 하였으나 아직 결재 상신이 되지 않은 공법");
            }

            chkEmpty = new Button(group, SWT.CHECK);
            chkEmpty.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            chkEmpty.setText("없는 공법");
            chkEmpty.setToolTipText("공법에 Publish 된 작업표준서가 없는 공법");

            chkDifferent = new Button(group, SWT.CHECK);
            chkDifferent.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            chkDifferent.setText("다른 공법");
            chkDifferent.setToolTipText("공법에 Publish 된 작업표준서는 있으나 해당 공법 Rev에 Publish 된 작업표준서가 없는 공법");

            // [SR150312-024] [20150324] Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
            chkRelease = new Button(group, SWT.CHECK);
            chkRelease.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
            chkRelease.setText("Released 공법");
            chkRelease.setSelection(true);
            chkRelease.setToolTipText("Release 완료 된 공법");
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        IDataMap dataMap = new RawDataMap();
        dataMap.put("configId", getConfigId(), IData.INTEGER_FIELD);

        dataMap.put("process_type", processType, IData.STRING_FIELD);
        dataMap.put("shop", shopLine, IData.OBJECT_FIELD);
        if(cmbLine.getData(cmbLine.getItem(cmbLine.getSelectionIndex())) != null) {
            dataMap.put("line", cmbLine.getData(cmbLine.getItem(cmbLine.getSelectionIndex())), IData.OBJECT_FIELD);
        }

        dataMap.put("station_code", txtStation.getText(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.ITEM_ITEM_ID, txtOpId.getText(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.OPERATION_REV_KOR_NAME, txtOpName.getText(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.BL_OCC_MVL_CONDITION, txtVariant.getText(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.ITEM_OWNING_USER, txtOwner.getText(), IData.STRING_FIELD);
        dataMap.put(SDVPropertyConstant.OPERATION_REV_MECO_NO, txtMeco.getText(), IData.STRING_FIELD);
        dataMap.put("publish_user", txtPublishUser.getText(), IData.STRING_FIELD);
        dataMap.put("empty_operation", chkEmpty.getSelection(), IData.BOOLEAN_FIELD);
        dataMap.put("different_operation", chkDifferent.getSelection(), IData.BOOLEAN_FIELD);
        dataMap.put("norelease_operation", chkNoRelease.getSelection(), IData.BOOLEAN_FIELD);
        dataMap.put("release_operation", chkRelease.getSelection(), IData.BOOLEAN_FIELD);
        
        return dataMap;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new AbstractSDVInitOperation() {

            public IDataMap getInitData() {
                IDataMap dataMap = new RawDataMap();

                AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
                if(application instanceof MFGLegacyApplication) {
                    TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
                    try {
                        TCComponentRevisionRule revisionRuleComp = bopWindow.getRevisionRule();

                        if(revisionRuleComp != null) {
                            dataMap.put("revisionRule", revisionRuleComp.toString(), IData.STRING_FIELD);
                            dataMap.put("effectivity", revisionRuleComp.toStringEffectivityOnly(), IData.STRING_FIELD);
                        }

                        dataMap.put("variantRule", SDVBOPUtilities.getBOMConfiguredVariantSetToString(bopWindow), IData.STRING_FIELD);

                        shopLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
                        TCComponentItemRevision shopRevision = shopLine.getItemRevision();

                        String[] propNames = new String[] {
                                SDVPropertyConstant.SHOP_REV_PRODUCT_CODE,
                                SDVPropertyConstant.SHOP_REV_SHOP_CODE
                        };

                        String[] propValues = shopRevision.getProperties(propNames);

                        dataMap.put("productCode", propValues[0], IData.STRING_FIELD);
                        dataMap.put("shopCode", propValues[1], IData.STRING_FIELD);

                        processType = shopRevision.getTCProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).getStringValue();

                        List<TCComponentBOPLine> lineList = getLines(shopLine);
                        dataMap.put("LineCodeList", lineList, IData.LIST_FIELD);
                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                }

                return dataMap;
            }

            private List<TCComponentBOPLine> getLines(TCComponentBOPLine bopLine) throws TCException {
                List<TCComponentBOPLine> lineList = new ArrayList<TCComponentBOPLine>();

                if(bopLine.getChildrenCount() > 0) {
                    AIFComponentContext[] children = bopLine.getChildren();
                    for(AIFComponentContext child : children) {
                        TCComponentBOPLine childLine = (TCComponentBOPLine) child.getComponent();
                        if(ProcessSheetUtils.isLine(childLine)) {
                            lineList.add(childLine);
                        }
                    }
                }

                return lineList;
            }

            @Override
            public void executeOperation() throws Exception {
                IDataSet dataset = new DataSet();
                dataset.addDataMap("searchConditionView", getInitData());

                setData(dataset);
            }

        };
    }

    @Override
    public void uiLoadCompleted() {
        getShell().setDefaultButton(getActionToolButtons().get("Search").getButton());
    }

    @Override
    /**
     * [SR141203-038][20141208] shcho, 도장 ParallelLine인 경우 ComboBox에서 서로다른 ParallelLine 식별 불가 오류 수정 (검색 수행시 ParallelLine의 작업표준서는 검색 안되는 오류 포함)
     * 
     */
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if(result == SDVInitEvent.INIT_SUCCESS){
            if(dataset!=null && dataset.containsMap("searchConditionView")) {
                IDataMap dataMap = dataset.getDataMap("searchConditionView");

                Point point = null;
                int startX = 5;

                CLabel lblRevisionRule = new CLabel(ruleComposite, SWT.NONE);
                lblRevisionRule.setImage(SWTResourceManager.getImage(SearchConditionView.class, "/icons/revisionrule_16.png"));
                //20210616 [CF-2224] 리비전 룰을 결과에서 확인해야 하는데 방법이 없어 먼저 전역변수저장해두고 나중에 가져가도록 함.
                revisionRuleName = dataMap.getStringValue("revisionRule");
                lblRevisionRule.setText(revisionRuleName);
                point = lblRevisionRule.computeSize(-1, -1, true);
                lblRevisionRule.setBounds(startX, 3, point.x, 26);
                lblRevisionRule.setForeground(UIUtil.getColor(SWT.COLOR_WHITE));
                startX += point.x + 5;

                CLabel lblEffectivity = new CLabel(ruleComposite, SWT.NONE);
                lblEffectivity.setImage(SWTResourceManager.getImage(SearchConditionView.class, "/com/teamcenter/rac/common/images/effectivity_16.png"));
                lblEffectivity.setText(dataMap.getStringValue("effectivity"));
                point = lblEffectivity.computeSize(-1, -1, true);
                lblEffectivity.setBounds(startX, 3, point.x, 26);
                lblEffectivity.setForeground(UIUtil.getColor(SWT.COLOR_WHITE));
                startX += point.x + 5;

                CLabel lblVariantRule = new CLabel(ruleComposite, SWT.NONE);
                lblVariantRule.setImage(SWTResourceManager.getImage(SearchConditionView.class, "/icons/variantrule_16.png"));
                String variantRule = dataMap.getStringValue("variantRule");
                if("".equals(variantRule)) {
                    variantRule = registry.getString("BOPVariantConditionNotConfigured");
                }
                lblVariantRule.setText(variantRule);
                point = lblVariantRule.computeSize(-1, -1, true);
                lblVariantRule.setBounds(startX, 3, point.x, 26);
                lblVariantRule.setForeground(UIUtil.getColor(SWT.COLOR_WHITE));

                txtProdNo.setText(dataMap.getStringValue("productCode"));
                txtShop.setText(dataMap.getStringValue("shopCode"));

                cmbLine.add("ALL");
                cmbLine.select(0);

                @SuppressWarnings("unchecked")
                List<TCComponentBOPLine> lineList = (List<TCComponentBOPLine>) dataMap.getListValue("LineCodeList");
                if(lineList != null) {
                    try {
                        for(TCComponentBOPLine line : lineList) {
                            TCComponentItemRevision lineItemRev = line.getItemRevision();
                            if(lineItemRev != null) {
                                String lineCode = lineItemRev.getProperty(SDVPropertyConstant.LINE_REV_CODE);
                                String parallelLineNo = lineItemRev.getProperty(SDVPropertyConstant.LINE_PARALLEL_LINE_NO);
                                if(lineCode != null && !"".equals(lineCode)) {
                                    if(parallelLineNo != null && !"".equals(parallelLineNo)) {
                                        cmbLine.add(lineCode + "-" + parallelLineNo);
                                        cmbLine.setData(lineCode + "-" + parallelLineNo, line);
                                    } else {
                                        cmbLine.add(lineCode);
                                        cmbLine.setData(lineItemRev.getProperty(SDVPropertyConstant.LINE_REV_CODE), line);
                                    }
                                }
                            }
                        }
                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                }
                
                int configId = getConfigId();
                if(configId==0){
                	
                	chkEmpty.setSelection(false);
                    chkEmpty.setEnabled(false);
                    
                	chkDifferent.setSelection(false);
                    chkDifferent.setEnabled(false);

                    chkRelease.setSelection(false);    
                	
                	//chkNoRelease.setEnabled(false);
                	//chkNoRelease.setVisible(false);
                }else{
                	
                }
            }
        }
    }

    @Override
    protected void validateConfig(int configId) {
        if (configId != 0 && configId != 1) {
            throw new SDVRuntimeException("View[" + getId() + " not supported config Id :" + configId);
        }
    }
}