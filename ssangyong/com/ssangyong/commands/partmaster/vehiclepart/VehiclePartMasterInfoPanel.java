package com.ssangyong.commands.partmaster.vehiclepart;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.wb.swt.SWTResourceManager;

import com.ssangyong.commands.ec.search.ECOSearchDialog;
import com.ssangyong.commands.partmaster.Constants;
import com.ssangyong.commands.partmaster.validator.VehiclePartValidator;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.SYMCLOVComboBox;
import com.ssangyong.common.SYMCLabel;
import com.ssangyong.common.SYMCText;
import com.ssangyong.common.swtsearch.SearchItemDialog;
import com.ssangyong.common.swtsearch.SearchItemRevDialog;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.common.utils.TxtReportFactory;
import com.ssangyong.viewer.AbstractSYMCViewer;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR140617-016] [20140422] Selective part ��� �߰�. (code by ������)
 * [SR140616-019][20140619][bskwak] ������ ǰ�� Concept �ܰ� ǰ������ ���� ���� : Stage ���� ���� ��� �⺻ revision �ʱⰪ ����ϴ� ������ ���� ��.
 * [SR140729-026][20140730][jclee] Part Revision�� ALC Code ���� Display ��� �߰�.
 * [SR180130-033][LJG]
 * 1. E-BOM Part Master(Eng. Info) �� "Responsibility" => "DWG Creator" �� ����
   2. Responsibility Filed �� LOV �� �߰� : Supplier, Collaboration, SYMC
   3. �ű� part ���� �� ���� LOV Black BOX, Gray Box, White Box ���úҰ� ó��
   4. Revision Up �� ���� Responsibiliy �� ���� => ���� �������ϵ��� ó��
 * 
 * @author bs
 * 
 */
public class VehiclePartMasterInfoPanel extends Composite implements IPageComplete {

    /** TC Registry */
    private Registry registry;
    /** TC Session */
    private TCSession session;

    /** Part Orign Combo */
    protected SYMCLOVComboBox partOrignCB;

    /** Stage Combo */
    protected SYMCLOVComboBox partStageCB;
    /** Unit Combo */
    protected SYMCLOVComboBox unitCB;
    /** System Code Combo */
    protected SYMCLOVComboBox sysCodeCB;
    /** Drw Status Combo */
    protected SYMCLOVComboBox dwgStatusCodeCB;
    /** Drw Size Combo */
    protected SYMCLOVComboBox dwgSizeCodeCB;
    /** Regular Combo */
    protected SYMCLOVComboBox regularCB;
    /** Category Combo */
    protected SYMCLOVComboBox categoryCB;
    /** Color Combo */
    protected SYMCLOVComboBox colorIDCB;
    /** Color ID Combo */
    protected SYMCLOVComboBox colorSectionCB;
    /** Responsibility Combo */
    protected SYMCLOVComboBox responCB;
    /** Cat. V4 Type Combo */
    protected SYMCLOVComboBox catV4CB;

    // [SR140617-016] [20140422] Selective part ��� �߰�. (code by ������)
    /** Selective Part Combo */
    protected SYMCLOVComboBox selectivePartCB;

    /** Part No. Field */
    protected SYMCText partNoText;
    /** Part Rev. Field */
    protected SYMCText partRevisionText;
    /** Display Part No. Field */
    protected SYMCText dispPartNoText;
    /** PartName Field */
    protected SYMCText partNameText;
    /** Kor PartName Field */
    protected SYMCText koreanNameText;
    /** Showon No. Field */
    protected SYMCText shownOnNoText;
    /** Reference Field */
    protected SYMCText referenceText;
    /** ECO No. Field */
    protected SYMCText ecoNoText;
    /** SoftWare No. */
    protected SYMCText softNoText;

    /** Project Code Field */
    protected SYMCText projectCodeText;

    /** Selective Part Field */
    protected SYMCText selPartText;
    /** Material Field */
    protected SYMCText materialText;
    /** Alter Material Field */
    protected SYMCText alterMaterialText;
    /** Material Thickness Field */
    protected SYMCText materialThickText;
    /** Alter Material Thickness Field */
    protected SYMCText alterMaterialThcikText;
    /** Finish Field */
    protected SYMCText fisnishText;
    /** Bounding Box Field */
    protected SYMCText boundingBoxText;
    /** Cal. Surface Field */
    protected SYMCText calSurfaceText;
    /** End Item Field */
    protected SYMCText asEndItemText;
    /** Est. Weight Field */
    protected SYMCText estWeightText;
    /** Cal. Weight Field */
    protected SYMCText calWeightText;
    /** Act. Weight Field */
    protected SYMCText actWeightText;
    /** [SR140324-030][20140619] KOG DEV SES Spec No Field �߰� Veh. Part */
    protected SYMCText sesSpecNoText;
    /** Test Report Field */
    protected SYMCText testReportText;

    /** Name Spec Field */
    protected SYMCText nameSpecText;
    /** Change Description Field */
    protected SYMCText chgDescText;
    /** ALC Code Field */
    protected SYMCText alcCodeText;

    /** VPM ECO No.(���̱׷��̼�, Interface�� �߻��� VPM ECO No.) */
    protected SYMCText vpmEcoNoText;

    /** Part Name Search Button */
    protected Button partNameButton;
    /** ShowOn Part Search Button */
    protected Button showOnNoButton;
    /** Project Search Button */
    protected Button projSchButton;
    
    /** Actual Weight Update Button */
    protected Button updateActualWeightButton;
    /** SES Spec No Update Button */
    protected Button updateSESSpecNoButton;

    /** SaveAs�� Target Revision */
    TCComponentItemRevision baseItemRev;
    /** ��ȸ�� Target Revision */
    TCComponentItemRevision targetRevision;
    /** ȭ�鿡 �Էµ� �Ӽ� Map */
    HashMap<String, Object> attrMap;
    /** ���� Loading�� �Ӽ� Map(�����׸� Check�� ���� ���, attrMap�� oldAttrMap�� ��) */
    HashMap<String, Object> oldAttrMap;
    /** PartManage Dialog���� �Ѿ�� Param Map */
    HashMap<String, Object> paramMap;
    
    boolean isInit = true;
    
    //���� ��Ʈ ������ ���� ȭ���� ����, ���� �� ���� Viewer�ǿ��� viewing�� ��츦 ���� �ϱ� ����
    private boolean isRendering = false;
    
    /**
     * E-BOM ���� ���� Target Weight, Regulation, Critical �Ӽ� �߰�
     */
    //////////////////////////////////////////////////////////////////////////
    protected SYMCText text_targetWeight;
    protected SYMCText text_regulation;
    protected SYMCText text_critical;
    /////////////////////////////////////////////////////////////////////////
    /**
     * Create Part Menu�� ���� ȣ���
     * 
     * @param parent
     * @param paramMap
     *            : PartManage Dialog���� �Ѿ�� Param Map
     * @param style
     *            : Dialog SWT Style
     */
    public VehiclePartMasterInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style) {
        super(parent, style);
        this.registry = Registry.getRegistry(this);
        this.session = CustomUtil.getTCSession();
        this.attrMap = new HashMap<String, Object>();
        this.paramMap = paramMap;

        initUI();

        this.setInitData(paramMap);
        setControlData();
    }

    /**
     * Revision ���� �� ViewerTab���� ȣ��
     * 
     * @param parent
     * @param style
     */
    public VehiclePartMasterInfoPanel(Composite parent, int style) {
        super(parent, style);
        registry = Registry.getRegistry(this);
        session = CustomUtil.getTCSession();
        attrMap = new HashMap<String, Object>();
        oldAttrMap = new HashMap<String, Object>();
        
        initUI();
        
        // [SR150521-024][20150717][jclee] My Teamcenter���� Part Load �� 2�� Load�Ǵ°� ����.
//        setViewMode();
        try {
            InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
            // Target�� Revision�� ���
            if (comp != null && comp instanceof TCComponentItemRevision) {
                targetRevision = (TCComponentItemRevision) comp;
                // [SR150521-024][20150717][jclee] My Teamcenter���� Part Load �� 2�� Load�Ǵ°� ����.
                // SYMCPropertyViewer Class ���� �� ���� Refresh �ϸ鼭 Load ���ֱ⶧��
//                this.setInitData(targetRevision);
                this.getPropDataMap(this.oldAttrMap);
            }
            // Target�� BomLine�� ���
            else if (comp != null && comp instanceof TCComponentBOMLine) {
                targetRevision = ((TCComponentBOMLine) comp).getItemRevision();
                this.setInitData(targetRevision);
                this.getPropDataMap(this.oldAttrMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        setControlData();
        
        layout();
    }

    /**
     * ��ȸȭ���� ��� �����Ұ� �׸� Setting
     */
    public void setViewMode() {

        partOrignCB.setEnabled(false);
        projectCodeText.setEnabled(false);
        partStageCB.setEnabled(false);
        unitCB.setEnabled(false);

        partNoText.setEnabled(false);
        partRevisionText.setEnabled(false);
        partNameText.setEnabled(false);
        koreanNameText.setEnabled(false);

        // partNameButton.setEnabled(false);
        // projSchButton.setEnabled(false);

        shownOnNoText.setEnabled(false);
        vpmEcoNoText.setEnabled(false);
        ecoNoText.setEnabled(false);
        materialText.setEnabled(false);
        alterMaterialText.setEnabled(false);
        
        alcCodeText.setEnabled(false);

        // sesSpecNoText.setEnabled(true);
        // sesSpecNoText.setEditable(false);
        
        /**
         * [TC10 Upgrade][2015.04.30][jclee]
         * SYMCCheckInOutComposite�� ��� �̵� 
         */
        updateSESSpecNoButton.setEnabled(isSESSpecNoAccessCheck());
        
        // r / c ���� catia���� �޾ƿ��� ������ ���� �Ұ�
        text_regulation.setEnabled(false);
        text_critical.setEnabled(false);
        
        //color id ���� ���� color section no ó��
        String sColorID = colorIDCB.getSelectedString();
		if (sColorID != null && !sColorID.equals("") && sColorID.length() > 0 && !sColorID.equals(".")) {
			if (colorSectionCB != null) {
				colorSectionCB.setEnabled(true);
			}
		} else {
			if (colorSectionCB != null) {
				colorSectionCB.setEnabled(false);
			}
		}
		
		//dwg status ���� ���� UI ó��
		String strDwgStatus = dwgStatusCodeCB.getSelectedString();
        // Drw Status ���� '.' �ΰ�� DwgSize/CatV4Type �ʼ�
        if (".".equals(strDwgStatus)) {
            this.showOnNoButton.setEnabled(false);
            this.dwgSizeCodeCB.setEnabled(true);
        }
        // Drw Status ���� 'H' �ΰ�� Showon No. �ʼ�
        else if ("H".equals(strDwgStatus)) {
            showOnNoButton.setEnabled(true);
            this.dwgSizeCodeCB.setEnabled(false);
        } else if ("K".equals(strDwgStatus)) {
            showOnNoButton.setEnabled(false);
            this.dwgSizeCodeCB.setEnabled(false);
        }
        
        //cat V4 Type�� ������ ��Ȱ��ȭ �ε� �ϳ�.
        catV4CB.setEnabled(false);
    }

    /**
     * ȭ�鿡 �Էµ� �Ӽ� ���� ����
     * 
     * @param attributeMap
     *            : �Ӽ����� ����� HashMap
     * @return
     * @throws Exception
     */
    public HashMap<String, Object> getPropDataMap(HashMap<String, Object> attributeMap) throws Exception {

        // Part No.
        attributeMap.put("item_id", partNoText.getText());
        attributeMap.put("item_revision_id", partRevisionText.getText());

        attributeMap.put("object_name", partNameText.getText());

        // Part Orign
        attributeMap.put("s7_PART_TYPE", partOrignCB.getSelectedString());
        // Project No.
        attributeMap.put("s7_PROJECT_CODE", projectCodeText.getText());
        // Part Stage
        attributeMap.put("s7_STAGE", partStageCB.getSelectedString());

        // Display Part No.(Excel ����)
        attributeMap.put("s7_DISPLAY_PART_NO", dispPartNoText.getText());

        // Part Kor Name
        attributeMap.put("s7_KOR_NAME", koreanNameText.getText());
        // Unit
        attributeMap.put("uom_tag", unitCB.getSelectedString());
        // System Code
        attributeMap.put("s7_BUDGET_CODE", sysCodeCB.getSelectedString());

        // Drawing Status
        attributeMap.put("s7_DRW_STAT", dwgStatusCodeCB.getSelectedString());
        // Shown On No.(TypedReference)
        attributeMap.put("s7_SHOWN_PART_NO", shownOnNoText.getData());
        // Drawing Size
        attributeMap.put("s7_DRW_SIZE", dwgSizeCodeCB.getSelectedString());

        // CAT V4 Type
        attributeMap.put("s7_CAT_V4_TYPE", catV4CB.getSelectedString());

        // Reference No.
        attributeMap.put("s7_REFERENCE", referenceText.getText());

        // ECO No.(TypedReference)
        attributeMap.put("s7_ECO_NO", ecoNoText.getData());

        attributeMap.put("s7_VPM_ECO_NO", vpmEcoNoText.getText());

        // Software Mng. No.
        attributeMap.put("s7_SOFTWARE_MNG_NO", softNoText.getText());

        // Regualr
        attributeMap.put("s7_REGULAR_PART", regularCB.getSelectedString());

        // Category
        attributeMap.put("s7_REGULATION", categoryCB.getSelectedString());

        // [SR140617-016] [20140422] Selective part ��� �߰�. (code by ������)
        // Selective Part
        attributeMap.put("s7_SELECTIVE_PART", selectivePartCB.getSelectedString());

        // Color ID
        attributeMap.put("s7_COLOR", colorIDCB.getSelectedString());
        // Color Section ID
        attributeMap.put("s7_COLOR_ID", colorSectionCB.getSelectedString());
//        System.out.println("colorSectionCB.getSelectedString() : "+colorSectionCB.getSelectedString());
        // Material(TypedReference)
        attributeMap.put("s7_MATERIAL", materialText.getData());
        // Alter Material(TypedReference)
        attributeMap.put("s7_ALT_MATERIAL", alterMaterialText.getData());
        // Material Thickness
        attributeMap.put("s7_THICKNESS", materialThickText.getText());
        // Alter Material Thickness
        attributeMap.put("s7_ALT_THICKNESS", alterMaterialThcikText.getText());
        // Finish
        attributeMap.put("s7_FINISH", fisnishText.getText());
        // Responsibility
        attributeMap.put("s7_RESPONSIBILITY", responCB.getSelectedString());
        // Bounding Box
        attributeMap.put("s7_BOUNDINGBOX", boundingBoxText.getText());
        // Est. Weight
        attributeMap.put("s7_EST_WEIGHT", estWeightText.getText());
        // Cal. Surface(M2)
        attributeMap.put("s7_CAL_SURFACE", calSurfaceText.getText());
        // Cal. Weight(kg)
        attributeMap.put("s7_CAL_WEIGHT", calWeightText.getText());
        // As End Item
        attributeMap.put("s7_AS_END_ITEM", asEndItemText.getText());
        // Act. Weight(kg)
        attributeMap.put("s7_ACT_WEIGHT", actWeightText.getText());
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // EBOM ���� ���� Target Weight, Regulation, Critical �Ӽ� �߰�
        attributeMap.put("s7_TARGET_WEIGHT", text_targetWeight.getText());
        attributeMap.put("s7_R", text_regulation.getText());
        attributeMap.put("s7_C", text_critical.getText());
        /////////////////////////////////////////////////////////////////////////////////////////////////////////
        // [SR140324-030][20140619] KOG DEV Veh. Part Attribute Map �� SES Spec No. �߰�
        attributeMap.put("s7_SES_SPEC_NO", sesSpecNoText.getText());
        // Test Report Number
        attributeMap.put("s7_DVP_RESULT", testReportText.getText());
        attributeMap.put("object_desc", nameSpecText.getText());

        // Chg. Description
        attributeMap.put("s7_CHANGE_DESCRIPTION", chgDescText.getText());

        return attributeMap;
    }

    /**
     * Create Part ����� ���� ȣ��� ��� �Ӽ� �� Setting
     * 
     * @param paramMap
     *            : Part Manage Dialog���� �Ѿ�� Parameter Map
     */
    private void setInitData(HashMap<String, Object> paramMap) {
        String strStage = (String) paramMap.get(Constants.ATTR_NAME_STAGE);
        String strRegular = (String) paramMap.get(Constants.ATTR_NAME_REGULAR);

        // SaveAs�� ��� Target Revision
        this.baseItemRev = (TCComponentItemRevision) paramMap.get(Constants.ATTR_NAME_BASEITEMID);
        if (baseItemRev != null) {
            try {
                // �ش� Revision �� ȭ�鿡 Setting
                this.setInitData(this.baseItemRev);
                // ItemID �ʱ�ȭ
                dispPartNoText.setText("");
                vpmEcoNoText.setText("");
                // [SR140729-026][20140825] jclee, Save As �� ALC Code �� Blank.
                alcCodeText.setText("");
                // [SR141022-033][20141027] jclee, Save As �� Test Report �� Blank.
                testReportText.setText("");
            } catch (TCException e) {
                e.printStackTrace();
            }
        }

        // Item Revision �ʱ�ȭ
        // [SR140616-019][20140619][bskwak] ������ ǰ�� Concept �ܰ� ǰ������ ���� ���� : Stage ���� ���� ��� �⺻ revision �ʱⰪ ����ϴ� ������ ���� ��.
        // if ("P".equals(strStage))
        // {
        
        // [20150107] TC10 ���׷��̵� �� ���� Ȯ���Ͽ� ���� ��.
        if(strStage.equals("C")) {
        	partRevisionText.setText("A");
        } else {
        	partRevisionText.setText(SYMCClass.ITEM_REV_ID);        	
        }
        
        // }
        // else
        // partRevisionText.setText("A");

        partStageCB.setSelectedString(strStage);
        regularCB.setSelectedString(strRegular);

        ecoNoText.setText("");
        ecoNoText.setData(null);

        TCComponentItemRevision ecoItemRev = (TCComponentItemRevision) paramMap.get(Constants.ATTR_NAME_ECOITEMID);
        if (ecoItemRev != null) {
            ecoNoText.setText(ecoItemRev.toDisplayString());
            ecoNoText.setData(ecoItemRev);
        }

    }

    /**
     * ȭ�� Data ����
     */
    public void refreshData() {
        if (targetRevision != null) {
            try {
                this.setInitData(targetRevision);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

//    /**
//     * SaveAs Ȥ�� ViewerTab(��ȸ) ��ɿ��� ���õ� Revision�� �� Setting
//     * 
//     * @param targetRevision
//     * @throws TCException
//     */
//    @SuppressWarnings("deprecation")
//	private void setInitData(TCComponentItemRevision targetRevision) throws TCException {
//    	String strSaveAs = "";
//    	if (paramMap != null) {
//    		strSaveAs = (String) paramMap.get(Constants.COMMAND_SAVE_AS);
//		}
//    	
//    	try {
//    		VehiclePartDao dao = new VehiclePartDao();
//    		
//    		ArrayList<VehiclePartInfoData> alSelectedVehiclePartInfo = dao.getVehiclePartInfo(targetRevision);
//    		
//    		if (alSelectedVehiclePartInfo.size() != 1) {
//				return;
//			}
//    		
//    		VehiclePartInfoData data = alSelectedVehiclePartInfo.get(0);
//			
//			this.attrMap.put("s7_MAIN_NAME", data.getMainName());
//	        this.attrMap.put("s7_SUB_NAME", data.getSubName());
//	        this.attrMap.put("s7_LOC1_FR", data.getLoc1FR());
//	        this.attrMap.put("s7_LOC2_IO", data.getLoc2IO());
//	        this.attrMap.put("s7_LOC3_UL", data.getLoc3UL());
//	        this.attrMap.put("s7_LOC4_EE", data.getLoc4EE());
//	        this.attrMap.put("s7_LOC5_LR", data.getLoc5LR());
//	        
//	        partNoText.setText(data.getItemID());
//	        partRevisionText.setText(data.getItemRevisionID());
//	        partNameText.setText(data.getObjectName());
//	        
//	        // Part Orign
//	        partOrignCB.setSelectedString(data.getPartType());
//	        
//	        // Project No.
//	        // [SR140702-059][20140626] KOG Veh. Part SaveAs �ÿ� ProjectCode Blank ó��.
//	        // [SR140729-026][20140825] jclee, Veh. Part SaveAs �ÿ� ALCCode Blank ó��.
//	        // [SR141022-033][20141027] jclee, Veh. Part SaveAs �ÿ� Test Report Blank ó��.
//	        if (paramMap != null) {
//	            if (strSaveAs != null && strSaveAs.equals("TRUE")) {
//	                projectCodeText.setText("");
//	                alcCodeText.setText("");
//	                testReportText.setText("");
//	            } else {
//	                projectCodeText.setText(data.getProjectCode());
//	                alcCodeText.setText(data.getPGID());
//	                testReportText.setText(data.getDvpResult());
//	            }
//	        } else {
//	            projectCodeText.setText(data.getProjectCode());
//	            alcCodeText.setText(data.getPGID());
//	            testReportText.setText(data.getDvpResult());
//	        }
//	        
//	        // Part Stage
//	        partStageCB.setSelectedString(data.getStage());
//	        
//	        // Display Part No.(Excel ����)
//	        dispPartNoText.setText(data.getDisplayPartNo());
//	        
//	        // Part Kor Name
//	        koreanNameText.setText(data.getKorName());
//	        
//	        // Unit
//	        unitCB.setSelectedString(data.getSymbol());
//	        
//	        // System Code
//	        sysCodeCB.setSelectedString(data.getBudgetCode());
//	        
//	        String strDwgStat = data.getDrwStat();
//	        // Drawing Status
//	        dwgStatusCodeCB.setSelectedString(strDwgStat);
//	        
//	        if ("H".equals(strDwgStat))
//	        	this.showOnNoButton.setEnabled(true);
//	        
//	        // Shown On No.(TypedReference)
//	        String sShownOnNoUID = data.getShownPartNoUID().replace("AAAAAAAAAAAAAA", "");
//	        String sECONoUID = data.getEcoNoUID().replace("AAAAAAAAAAAAAA", "");
//	        String sMaterialUID = data.getMaterialUID().replace("AAAAAAAAAAAAAA", "");
//	        String sAltMaterialUID = data.getAltMeterialUID().replace("AAAAAAAAAAAAAA", "");
//	        
//	        String[] saRefComponentUIDs = new String[] {sShownOnNoUID, sECONoUID, sMaterialUID, sAltMaterialUID};
//	        
//	        TCComponent[] refComponents = session.stringToComponent(saRefComponentUIDs);
////	        TCComponent[] refComponents = new TCComponent[4];
//	        
//	        TCComponent showComp = refComponents[0];
//	        if (showComp != null) {
//	        	shownOnNoText.setText(showComp.getProperty("item_id"));
//	        	shownOnNoText.setData(showComp);
//	        }
//	        
//	        // Drawing Size
//	        dwgSizeCodeCB.setSelectedString(data.getDrwSize());
//	        
//	        // Cat V4 Type
//	        catV4CB.setSelectedString(data.getCatV4Type());
//	        
//	        // [SR140617-016] [20140422] Selective part ��� �߰�. (code by ������)
//	        // selective Part
//	        selectivePartCB.setSelectedString(data.getSelectivePart());
//	        
//	        // Reference No.
//	        referenceText.setText(data.getReference());
//	        
//	        // ECO No.(TypedReference)
//	        TCComponent ecoComp = refComponents[1];
//	        if (ecoComp != null) {
//	        	ecoNoText.setText(ecoComp.toDisplayString());
//	        	ecoNoText.setData(ecoComp);
//	        }
//	        
//	        vpmEcoNoText.setText(data.getVpmECONo());
//	        
//	        // SOFTWARE_MNG_NO
//	        softNoText.setText(data.getSoftwareMngNo());
//	        
//	        // Regualr
//	        regularCB.setSelectedString(data.getRegularPart());
//	        
//	        // Category
//	        categoryCB.setSelectedString(data.getRegulation());
//	        
//	        // Selective Part
//	        // paramMap.put("s7_PART_TYPE", partNameText.getText());
//	        
//	        /**
//	         * [SR����][2015.04.28][jclee] Color ID�� ���õǾ����� ��쿡�� Color Section ���� �����ϵ��� ����.
//	         */
//	        String sColorID = data.getColor();
//	        String sColorSection = data.getColorID();
//	        
//	        // Color ID
//	        colorIDCB.setSelectedString(sColorID);
//	        
//	        // Color Section ID
//	        if (!(sColorID == null || sColorID.equals("") || sColorID.length() == 0 || sColorID.equals("."))) {
//	        	colorSectionCB.setSelectedString(sColorSection);
//	        }
//	        
//	        // Material(TypedReference)
//	        TCComponent matComp = refComponents[2];
//	        if (matComp != null) {
//	        	materialText.setText(matComp.toDisplayString());
//	        	materialText.setData(matComp);
//	        }
//	        
//	        // Alter Material(TypedReference)
//	        TCComponent altMatComp = refComponents[3];
//	        if (altMatComp != null) {
//	        	alterMaterialText.setText(altMatComp.toDisplayString());
//	        	alterMaterialText.setData(altMatComp);
//	        }
//	        
//	        // Material Thickness
//	        materialThickText.setText(data.getThickness());
//	        // Alter Material Thickness
//	        alterMaterialThcikText.setText(data.getAltThickness());
//	        // Finish
//	        fisnishText.setText(data.getFinish());
//	        // Responsibility
//	        responCB.setSelectedString(data.getResponsibility());
//	        
//	        // Est. Weight
//	        estWeightText.setText(data.getEstWeight());
//	        // Cal. Surface(M2)
//	        // ǥ���� 5.10 �ڸ����� ���� (from �۴뿵C, 20130618)
//	        calSurfaceText.setText(data.getCalSurface());
//	        // Cal. Weight(kg)
//	        calWeightText.setText(data.getCalWeight());
//	        // As End Item
//	        asEndItemText.setText(data.getAsEndItem());
//	        
//	        // SES Spec No Button
//	        updateSESSpecNoButton.setEnabled(isSESSpecNoAccessCheck());
//	        
//	        // Test Report Number
//	        testReportText.setText(data.getDvpResult());
//	        
//	        nameSpecText.setText(data.getObjectDesc());
//	        
//	        // Chg. Description
//	        // [SR150521-044][2015.06.24][jclee] Save AS �� Setting
//	        if (strSaveAs != null && strSaveAs.equals("TRUE")) {
//	        	chgDescText.setText("");
//	        } else {
//	        	chgDescText.setText(data.getChangeDescription());
//	        }
//	        
//	        // Bounding Box
//	        boundingBoxText.setText(data.getBoundingbox());
//	        // Act. Weight(kg)
//	        actWeightText.setText(data.getActWeight());
//	        
//	        // [SR140324-030][20140619] KOG DEV Veh. Part ��ȸ�� SES Spec No. Property �� Text�� ����.
//	        // [SR150521-044][2015.06.24][jclee] Save AS �� Setting
//	        if (strSaveAs != null && strSaveAs.equals("TRUE")) {
//	        	sesSpecNoText.setText("");
//	        } else {
//	        	sesSpecNoText.setText(data.getSesSpecNo());
//	        }
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    }
    
    private void setInitData(TCComponentItemRevision targetRevision) throws TCException {
    	String strSaveAs = "";
    	if (paramMap != null) {
    		strSaveAs = (String) paramMap.get(Constants.COMMAND_SAVE_AS);
    	}
    	
    	this.attrMap.put("s7_MAIN_NAME", targetRevision.getStringProperty("s7_MAIN_NAME"));
    	this.attrMap.put("s7_SUB_NAME", targetRevision.getStringProperty("s7_SUB_NAME"));
    	this.attrMap.put("s7_LOC1_FR", targetRevision.getStringProperty("s7_LOC1_FR"));
    	this.attrMap.put("s7_LOC2_IO", targetRevision.getStringProperty("s7_LOC2_IO"));
    	this.attrMap.put("s7_LOC3_UL", targetRevision.getStringProperty("s7_LOC3_UL"));
    	this.attrMap.put("s7_LOC4_EE", targetRevision.getStringProperty("s7_LOC4_EE"));
    	this.attrMap.put("s7_LOC5_LR", targetRevision.getStringProperty("s7_LOC5_LR"));
    	

    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	//[SR180724-011-09]
    	// EBOM �������� Target Weight, Regulation, Critical �Ӽ� �߰�
    	this.attrMap.put("s7_TARGET_WEIGHT", targetRevision.getStringProperty("s7_TARGET_WEIGHT"));
    	this.attrMap.put("s7_R", targetRevision.getStringProperty("s7_R"));
    	this.attrMap.put("s7_C", targetRevision.getStringProperty("s7_C"));
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////

    	partNoText.setText(targetRevision.getProperty("item_id"));
    	partRevisionText.setText(targetRevision.getProperty("item_revision_id"));
    	partNameText.setText(targetRevision.getProperty("object_name"));
    	
    	// Part Orign
    	partOrignCB.setSelectedString(targetRevision.getProperty("s7_PART_TYPE"));
    	
    	// Project No.
    	// [SR140702-059][20140626] KOG Veh. Part SaveAs �ÿ� ProjectCode Blank ó��.
    	// [SR140729-026][20140825] jclee, Veh. Part SaveAs �ÿ� ALCCode Blank ó��.
    	// [SR141022-033][20141027] jclee, Veh. Part SaveAs �ÿ� Test Report Blank ó��.
    	if (paramMap != null) {
    		if (strSaveAs != null && strSaveAs.equals("TRUE")) {
    			projectCodeText.setText("");
    			alcCodeText.setText("");
    			testReportText.setText("");
    		} else {
    			projectCodeText.setText(targetRevision.getProperty("s7_PROJECT_CODE"));
    			alcCodeText.setText(targetRevision.getProperty("m7_PG_ID"));
    			testReportText.setText(targetRevision.getProperty("s7_DVP_RESULT"));
    		}
    	} else {
    		projectCodeText.setText(targetRevision.getProperty("s7_PROJECT_CODE"));
    		alcCodeText.setText(targetRevision.getProperty("m7_PG_ID"));
    		testReportText.setText(targetRevision.getProperty("s7_DVP_RESULT"));
    	}
    	
    	// Part Stage
    	partStageCB.setSelectedString(targetRevision.getProperty("s7_STAGE"));
    	
    	// Display Part No.(Excel ����)
    	dispPartNoText.setText(targetRevision.getProperty("s7_DISPLAY_PART_NO"));
    	
    	// Part Kor Name
    	koreanNameText.setText(targetRevision.getProperty("s7_KOR_NAME"));
    	
    	// Unit
    	unitCB.setSelectedString(targetRevision.getItem().getProperty("uom_tag"));
    	// System Code
    	sysCodeCB.setSelectedString(targetRevision.getProperty("s7_BUDGET_CODE"));
    	
    	String strDwgStat = targetRevision.getProperty("s7_DRW_STAT");
    	// Drawing Status
    	dwgStatusCodeCB.setSelectedString(strDwgStat);
    	
    	if ("H".equals(strDwgStat))
    		this.showOnNoButton.setEnabled(true);
    	
    	// Shown On No.(TypedReference)
    	TCComponent showComp = targetRevision.getReferenceProperty("s7_SHOWN_PART_NO");
    	if (showComp != null) {
    		shownOnNoText.setText(showComp.getProperty("item_id"));
    		shownOnNoText.setData(showComp);
    	}
    	
    	// Drawing Size
    	dwgSizeCodeCB.setSelectedString(targetRevision.getProperty("s7_DRW_SIZE"));
    	
    	// Cat V4 Type
    	catV4CB.setSelectedString(targetRevision.getProperty("s7_CAT_V4_TYPE"));
    	
    	// [SR140617-016] [20140422] Selective part ��� �߰�. (code by ������)
    	// selective Part
    	selectivePartCB.setSelectedString(targetRevision.getProperty("s7_SELECTIVE_PART"));
    	
    	// Reference No.
    	referenceText.setText(targetRevision.getProperty("s7_REFERENCE"));
    	
    	// ECO No.(TypedReference)
    	TCComponent ecoComp = targetRevision.getReferenceProperty("s7_ECO_NO");
    	if (ecoComp != null) {
    		ecoNoText.setText(ecoComp.toDisplayString());
    		ecoNoText.setData(ecoComp);
    	}
    	
    	vpmEcoNoText.setText(targetRevision.getProperty("s7_VPM_ECO_NO"));
    	
    	// SOFTWARE_MNG_NO
    	softNoText.setText(targetRevision.getProperty("s7_SOFTWARE_MNG_NO"));
    	
    	// Regualr
    	regularCB.setSelectedString(targetRevision.getProperty("s7_REGULAR_PART"));
    	
    	// Category
    	categoryCB.setSelectedString(targetRevision.getProperty("s7_REGULATION"));
    	
    	// Selective Part
    	// paramMap.put("s7_PART_TYPE", partNameText.getText());
    	
    	/**
    	 * [SR����][2015.04.28][jclee] Color ID�� ���õǾ����� ��쿡�� Color Section ���� �����ϵ��� ����.
    	 */
//        // Color ID
//        colorIDCB.setSelectedString(targetRevision.getProperty("s7_COLOR"));
//        // Color Section ID
//        colorSectionCB.setSelectedString(targetRevision.getProperty("s7_COLOR_ID"));
    	String sColorID = targetRevision.getProperty("s7_COLOR");
    	String sColorSection = targetRevision.getProperty("s7_COLOR_ID");
    	
    	// Color ID
    	colorIDCB.setSelectedString(sColorID);
    	
    	// Color Section ID
    	if (!(sColorID == null || sColorID.equals("") || sColorID.length() == 0 || sColorID.equals("."))) {
    		colorSectionCB.setSelectedString(sColorSection);
    	}
    	
    	// Material(TypedReference)
    	TCComponent matComp = targetRevision.getReferenceProperty("s7_MATERIAL");
    	if (matComp != null) {
    		materialText.setText(matComp.toDisplayString());
    		materialText.setData(matComp);
    	}
    	
    	// Alter Material(TypedReference)
    	TCComponent altMatComp = targetRevision.getReferenceProperty("s7_ALT_MATERIAL");
    	if (altMatComp != null) {
    		alterMaterialText.setText(altMatComp.toDisplayString());
    		alterMaterialText.setData(altMatComp);
    	}
    	
    	// Material Thickness
    	materialThickText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_THICKNESS"), "########.##"));
    	// Alter Material Thickness
    	alterMaterialThcikText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_ALT_THICKNESS"), "########.##"));
    	// Finish
    	fisnishText.setText(targetRevision.getProperty("s7_FINISH"));
    	// Responsibility
    	String responsibility = targetRevision.getProperty("s7_RESPONSIBILITY");
    	if(isRendering){
    		responCB.setSelectedString(responsibility);
		}else{
			if(!responsibility.startsWith("White Box") && !responsibility.startsWith("Black Box") && !responsibility.startsWith("Gray Box") && !responsibility.startsWith("SYMC")){
	    		responCB.setSelectedString(responsibility);
			}
	    	else{
	    		responCB.setSelectedString("");
	    	}
		}
    	
    	// Est. Weight
    	estWeightText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_EST_WEIGHT"), "########.####"));
    	// Cal. Surface(M2)
    	// ǥ���� 5.10 �ڸ����� ���� (from �۴뿵C, 20130618)
    	calSurfaceText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_CAL_SURFACE"), "########.##########"));
    	// Cal. Weight(kg)
    	calWeightText.setText(this.getFormatedString(targetRevision.getDoubleProperty("s7_CAL_WEIGHT"), "########.####"));
    	// As End Item
    	asEndItemText.setText(targetRevision.getProperty("s7_AS_END_ITEM"));
    	
    	// SES Spec No Button
    	updateSESSpecNoButton.setEnabled(isSESSpecNoAccessCheck());
    	
    	// Test Report Number
    	testReportText.setText(targetRevision.getProperty("s7_DVP_RESULT"));
    	
    	nameSpecText.setText(targetRevision.getProperty("object_desc"));
    	//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	// EBOM ���� ���� Regulation, Critical �Ӽ� �߰�
    	text_critical.setText(targetRevision.getProperty("s7_C"));
    	text_regulation.setText(targetRevision.getProperty("s7_R"));
    	/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    	
    	// Chg. Description
    	// [SR150521-044][2015.06.24][jclee] Save AS �� Setting
    	if (strSaveAs != null && strSaveAs.equals("TRUE")) {
    		chgDescText.setText("");
    	} else {
    		chgDescText.setText(targetRevision.getProperty("s7_CHANGE_DESCRIPTION"));
    	}
    	
    	TCComponent refComp = targetRevision.getReferenceProperty("s7_Vehpart_TypedReference");
    	if (refComp != null) {
    		refComp.refresh();
    		
    		// Bounding Box
    		boundingBoxText.setText(refComp.getProperty("s7_BOUNDINGBOX"));
    		// Act. Weight(kg)
    		actWeightText.setText(this.getFormatedString(refComp.getDoubleProperty("s7_ACT_WEIGHT"), "########.####"));
    		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    		// EBOM ���� ���� Target Weight �Ӽ� �߰�
    		text_targetWeight.setText(this.getFormatedString(refComp.getDoubleProperty("s7_TARGET_WEIGHT"), "########.####"));
    		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    		// [SR140324-030][20140619] KOG DEV Veh. Part ��ȸ�� SES Spec No. Property �� Text�� ����.
    		// [SR150521-044][2015.06.24][jclee] Save AS �� Setting
    		if (strSaveAs != null && strSaveAs.equals("TRUE")) {
    			sesSpecNoText.setText("");
    		} else {
    			sesSpecNoText.setText(refComp.getStringProperty("s7_SES_SPEC_NO"));
    		}
    	}
    }

    public String getFormatedString(double value, String format) {

        DecimalFormat df = new DecimalFormat(format);
        return df.format(value);
    }

    /**
     * ����(CheckIn)�� Validation Check
     * 
     * @return
     */
    public boolean isSavable() {
        try {
            // �Էµ� �Ӽ� ���� ������
            this.getPropDataMap(this.attrMap);
//            System.out.println("isSavable attrMap.get(s7_COLOR_ID) : "+attrMap.get("s7_COLOR_ID"));
            // Validation Check
            VehiclePartValidator validator = new VehiclePartValidator();
            String strMessage = validator.validate(this.attrMap, VehiclePartValidator.TYPE_VALID_MODIFY);

            if (!CustomUtil.isEmpty(strMessage)) {
        		MessageBox.post(getShell(), strMessage, "Warning", MessageBox.WARNING);
                return false;
            } else {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * ����(CheckIn)�� ȣ��
     * 
     * @return
     */
    public void saveAction() {
        try {
//        	System.out.println("saveAction attrMap.get(s7_COLOR_ID) : "+attrMap.get("s7_COLOR_ID"));
            this.attrMap.remove("item_id");
            // DR Check Flag�� �Ӽ��� �ƴ�
            attrMap.remove("DR_CHECK_FLAG");

            // ActWeight/Bounding Box �Ӽ��� Object�� ������( Release �Ŀ� �������� �ؾ� ��)
            TCComponent refComp = targetRevision.getReferenceProperty("s7_Vehpart_TypedReference");
            // �Ӽ� Object�� ���� ��� ����
            if (refComp != null) {

                refComp.setProperty("s7_ACT_WEIGHT", (String) attrMap.get("s7_ACT_WEIGHT"));
                // EBOM ���� ���� Target Weight �Ӽ� �߰�
                refComp.setProperty("s7_TARGET_WEIGHT", (String) attrMap.get("s7_TARGET_WEIGHT"));
                refComp.setProperty("s7_BOUNDINGBOX", (String) attrMap.get("s7_BOUNDINGBOX"));
                // [SR140324-030][20140703] KOG SES Spec No. �߰�.
                refComp.setProperty("s7_SES_SPEC_NO", attrMap.get("s7_SES_SPEC_NO").toString());
                refComp.refresh();

            }
            // �Ӽ� Object�� �����ϴ� ��� ����
            else {
                // [SR140324-030][20140703] KOG SES Spec No. �߰�.
                refComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX", "s7_SES_SPEC_NO" }, new String[] { (String) attrMap.get("s7_ACT_WEIGHT"), (String) attrMap.get("s7_TARGET_WEIGHT"), (String) attrMap.get("s7_BOUNDINGBOX"), attrMap.get("s7_SES_SPEC_NO").toString() });

                targetRevision.setReferenceProperty("s7_Vehpart_TypedReference", refComp);
            }

            // attrMap�� �����ϴ� ��� �Ӽ��� �ϰ� Update �ǹǷ� Revision �Ӽ��� �ƴѰ�� ����
            attrMap.remove("s7_ACT_WEIGHT");
            attrMap.remove("s7_TARGET_WEIGHT");
            attrMap.remove("s7_BOUNDINGBOX");
            // [SR140324-030][20140703] KOG SES Spec No. �߰�.
            attrMap.remove("s7_SES_SPEC_NO");
            attrMap.remove("uom_tag");
            
            // Dwg Status�� "."�� �ƴ� ��� Regulation / Critical �� ����.
//            if(!((String) attrMap.get("s7_DRW_STAT")).equals(".")){
//            	text_regulation.setText("");
//            	text_critical.setText("");
//            	attrMap.put("s7_R", "");
//            	attrMap.put("s7_C", "");
//            }
            

            String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
            TCProperty[] props = targetRevision.getTCProperties(szKey);

            for (int i = 0; i < props.length; i++) {

                if (props[i] == null) {
                    System.out.println(szKey[i] + " is Null");
                    continue;
                }

                Object value = attrMap.get(szKey[i]);
                CustomUtil.setObjectToPropertyValue(props[i], value);
                
//                System.out.println("szKey[i] : " + szKey[i]);
//                System.out.println("value : " + value);

            }

            // �Ӽ� �� �ϰ� �ݿ�
            targetRevision.setTCProperties(props);
            targetRevision.refresh();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * ȭ�� �ʱ�ȭ
     */
    private void initUI() {
        setBackground(new Color(null, 255, 255, 255));
        setLayout(new GridLayout(1, false));

        FormLayout groupLayout = new FormLayout();
        groupLayout.marginTop = 5;
        groupLayout.marginBottom = 5;
        groupLayout.marginLeft = 5;
        groupLayout.marginRight = 5;
        
        // /////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////Basic Info Start//////////////////////////////////
        // /////////////////////////////////////////////////////////////////////////////////////////

        Group group = new Group(this, SWT.NONE);
        group.setLayout(groupLayout);
        group.setText("Basic Info");
        group.setBackground(new Color(null, 255, 255, 255));

        FormData labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(0);

        SYMCLabel partOriginLabel = new SYMCLabel(group, "Part Origin", labelFormData);
        FormData comboFormData = new FormData(113, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(0);
        comboFormData.left = new FormAttachment(partOriginLabel, 5);
        comboFormData.height = 20;
        
        partOrignCB = new SYMCLOVComboBox(group, SWT.BORDER, session, "S7_PART_ORIGIN");
        partOrignCB.setLayoutData(comboFormData);
        partOrignCB.setMandatory(true);
        partOrignCB.addPropertyChangeListener(new IPropertyChangeListener() {
            // Vehicle Part ������ Orign M/T ���� ���� �Ұ�
            public void propertyChange(PropertyChangeEvent event) {

                String strSel = partOrignCB.getSelectedString();

                if (!CustomUtil.isEmpty(strSel) && (strSel.startsWith("M") || strSel.startsWith("T"))) {
                    MessageBox.post(getShell(), "No Avaliable Orign Type", "BOM Save", MessageBox.INFORMATION);
                    partOrignCB.setSelectedIndex(0);
                    return;
                }

            }
        });

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(partOrignCB, 10);
        SYMCLabel projectCodeLabel = new SYMCLabel(group, "Project Code", labelFormData);

        FormData textFormData = new FormData(90, 15);
        textFormData.top = new FormAttachment(0);
        textFormData.left = new FormAttachment(projectCodeLabel, 5);
        textFormData.height = 15;

        projectCodeText = new SYMCText(group, true, textFormData);
        projectCodeText.setLayoutData(textFormData);
        projectCodeText.setMandatory(true);
        projectCodeText.setEnabled(false);
        projectCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        FormData buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(0);
        buttonFormData.left = new FormAttachment(projectCodeText, 5);
        projSchButton = new Button(group, SWT.PUSH);
        projSchButton.setImage(registry.getImage("Search.ICON"));
        projSchButton.setLayoutData(buttonFormData);
        projSchButton.addSelectionListener(new RevSelectionAdapter(projectCodeText, "S7_PROJECTRevision"));

        labelFormData = new FormData(95, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(projSchButton, 10);
        SYMCLabel partStageLabel = new SYMCLabel(group, "Part Stage", labelFormData);
        comboFormData = new FormData(113, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(0);
        comboFormData.left = new FormAttachment(partStageLabel, 5);
        comboFormData.height = 20;
        
        partStageCB = new SYMCLOVComboBox(group, SWT.BORDER, session, "S7_STAGE");
        partStageCB.setLayoutData(comboFormData);
        partStageCB.setEnabled(false);
        partStageCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partStageCB, 5);
        labelFormData.left = new FormAttachment(0);

        SYMCLabel partNoLabel = new SYMCLabel(group, "Part No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(partStageCB, 5);
        textFormData.left = new FormAttachment(partNoLabel, 5);

        partNoText = new SYMCText(group, true, textFormData);
        textFormData = new FormData(20, 18);
        textFormData.top = new FormAttachment(partStageCB, 5);
        textFormData.left = new FormAttachment(partNoText, 5);
        partRevisionText = new SYMCText(group, textFormData);
        partRevisionText.setEnabled(false);
        partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partStageCB, 5);
        labelFormData.left = new FormAttachment(partRevisionText, 29);
        SYMCLabel dispPartNoLabel = new SYMCLabel(group, "Display Part No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(partStageCB, 5);
        textFormData.left = new FormAttachment(dispPartNoLabel, 5);
        dispPartNoText = new SYMCText(group, textFormData);
        dispPartNoText.setEnabled(false);
        dispPartNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(dispPartNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel partNameLabel = new SYMCLabel(group, "Part Name", labelFormData);
        textFormData = new FormData(581, 18);
        textFormData.top = new FormAttachment(dispPartNoText, 5);
        textFormData.left = new FormAttachment(partNameLabel, 5);
        partNameText = new SYMCText(group, true, textFormData);
        partNameText.setEnabled(false);
        partNameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(dispPartNoText, 5);
        buttonFormData.left = new FormAttachment(partNameText, 5);
        partNameButton = new Button(group, SWT.PUSH);
        partNameButton.setImage(registry.getImage("Search.ICON"));
        partNameButton.setLayoutData(buttonFormData);
        partNameButton.addSelectionListener(new SelectionAdapter() {
            // Part Name �˻� Dialog Open
            public void widgetSelected(SelectionEvent event) {
                // Part Name �˻� Dialog
                PartNameDialog itemDialog = new PartNameDialog(getShell(), SWT.SINGLE, attrMap);
                itemDialog.open();

                // PartName/KorName Setting
                if (attrMap.get("object_name") != null) {
                    partNameText.setText((String) attrMap.get("object_name"));
                    koreanNameText.setText((String) attrMap.get("s7_KOR_NAME"));
                }
            }
        });

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(partNameText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel koreanNameLabel = new SYMCLabel(group, "Korean Name", labelFormData);
        textFormData = new FormData(581, 18);
        textFormData.top = new FormAttachment(partNameText, 5);
        textFormData.left = new FormAttachment(koreanNameLabel, 5);
        koreanNameText = new SYMCText(group, textFormData);
        koreanNameText.setEnabled(false);
        koreanNameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(koreanNameText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel unitLabel = new SYMCLabel(group, "Unit", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(koreanNameText, 5);
        comboFormData.left = new FormAttachment(unitLabel, 5);
        comboFormData.height = 20;
        unitCB = new SYMCLOVComboBox(group, SWT.BORDER, session, "Unit of Measures");
        unitCB.setLayoutData(comboFormData);
        unitCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(koreanNameText, 5);
        labelFormData.left = new FormAttachment(unitCB, 62);
        SYMCLabel sysCodeLabel = new SYMCLabel(group, "System Code", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(koreanNameText, 5);
        comboFormData.left = new FormAttachment(sysCodeLabel, 5);
        comboFormData.height = 20;
        sysCodeCB = new SYMCLOVComboBox(group, SWT.BORDER, session, "s7_SYSTEM_CODE");
        sysCodeCB.setLayoutData(comboFormData);
        sysCodeCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(sysCodeCB, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel dwgStatusLabel = new SYMCLabel(group, "Dwg Status", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(sysCodeCB, 5);
        comboFormData.left = new FormAttachment(dwgStatusLabel, 5);
        comboFormData.height = 20;
        dwgStatusCodeCB = new SYMCLOVComboBox(group, SWT.BORDER, session, "s7_DRW_STAT");
        dwgStatusCodeCB.setLayoutData(comboFormData);
        dwgStatusCodeCB.setMandatory(true);

        dwgStatusCodeCB.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                // Drw Status ���� ���� UI����
                changeTypeUI();
            }
        });

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(sysCodeCB, 5);
        labelFormData.left = new FormAttachment(dwgStatusCodeCB, 62);
        SYMCLabel shownOnNoLabel = new SYMCLabel(group, "Shown On No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(sysCodeCB, 5);
        textFormData.left = new FormAttachment(shownOnNoLabel, 5);
        shownOnNoText = new SYMCText(group, textFormData);
        shownOnNoText.setEnabled(false);
        shownOnNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(sysCodeCB, 5);
        buttonFormData.left = new FormAttachment(shownOnNoText, 5);
        showOnNoButton = new Button(group, SWT.PUSH);
        showOnNoButton.setImage(registry.getImage("Search.ICON"));
        showOnNoButton.setLayoutData(buttonFormData);
        showOnNoButton.addSelectionListener(new ItemSelectionAdapter(shownOnNoText, "S7_Vehpart"));
        showOnNoButton.setEnabled(false);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(shownOnNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel dwgSizeLabel = new SYMCLabel(group, "Dwg Size", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(shownOnNoText, 5);
        comboFormData.left = new FormAttachment(dwgSizeLabel, 5);
        comboFormData.height = 20;
        dwgSizeCodeCB = new SYMCLOVComboBox(group, SWT.BORDER, session, "s7_DRW_SIZE");
        dwgSizeCodeCB.setLayoutData(comboFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(shownOnNoText, 5);
        labelFormData.left = new FormAttachment(dwgSizeCodeCB, 62);
        SYMCLabel referenceLabel = new SYMCLabel(group, "Reference No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(shownOnNoText, 5);
        textFormData.left = new FormAttachment(referenceLabel, 5);
        referenceText = new SYMCText(group, textFormData);
        // referenceText.setEnabled(false);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(referenceText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel catLabel = new SYMCLabel(group, "Cat. V4 Type", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(referenceText, 5);
        comboFormData.left = new FormAttachment(catLabel, 5);
        comboFormData.height = 20;
        catV4CB = new SYMCLOVComboBox(group, SWT.BORDER, session, "S7_CAT_V4_TYPE");
        catV4CB.setLayoutData(comboFormData);
        // [SR150521-050][2015.06.25][jclee] CAT V4 �ԷºҰ�
//        catV4CB.setEnabled(true);
        catV4CB.setData(AbstractSYMCViewer.SKIP_ENABLE, "true");
//        catV4CB.setEnabled(false);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(referenceText, 5);
        labelFormData.left = new FormAttachment(catV4CB, 62);
        SYMCLabel regularLabel = new SYMCLabel(group, "Regular", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(referenceText, 5);
        comboFormData.left = new FormAttachment(regularLabel, 5);
        comboFormData.height = 20;
        regularCB = new SYMCLOVComboBox(group, SWT.BORDER, session, "s7_REGULAR_PART");
        regularCB.setLayoutData(comboFormData);
        regularCB.setEnabled(false);
        regularCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(regularCB, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel softNoLabel = new SYMCLabel(group, "Software No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(regularCB, 5);
        textFormData.left = new FormAttachment(softNoLabel, 5);
        softNoText = new SYMCText(group, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(regularCB, 5);
        labelFormData.left = new FormAttachment(softNoText, 62);
        SYMCLabel ecoNoLabel = new SYMCLabel(group, "ECO No", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(regularCB, 5);
        textFormData.left = new FormAttachment(ecoNoLabel, 5);
        ecoNoText = new SYMCText(group, textFormData);
        ecoNoText.setEnabled(false);
        ecoNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(regularCB, 5);
        buttonFormData.left = new FormAttachment(ecoNoText, 5);
        Button ecoNButton = new Button(group, SWT.PUSH);
        ecoNButton.setImage(registry.getImage("Search.ICON"));
        ecoNButton.setLayoutData(buttonFormData);
        ecoNButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent event) {
                // ECO �˻� Dialog
                ECOSearchDialog ecoSearchDialog = new ECOSearchDialog(getShell(), SWT.SINGLE);
                ecoSearchDialog.getShell().setText("ECO Search");
                ecoSearchDialog.setAllMaturityButtonsEnabled(false);
                ecoSearchDialog.setBInProcessSelect(false);
                ecoSearchDialog.setBCompleteSelect(false);

                ecoSearchDialog.open();

                // ECO �˻� ���
                TCComponentItemRevision[] ecos = ecoSearchDialog.getSelectctedECO();
                if (ecos != null && ecos.length > 0) {
                    TCComponentItemRevision ecoIR = ecos[0];

                    ecoNoText.setText(ecoIR.toDisplayString());
                    ecoNoText.setData(ecoIR);

                }

            }
        });

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(ecoNoText, 5);
        labelFormData.left = new FormAttachment(0, 378);
        SYMCLabel vpmEcoNoLabel = new SYMCLabel(group, "VPM ECO No.", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(ecoNoText, 5);
        textFormData.left = new FormAttachment(vpmEcoNoLabel, 5);
        vpmEcoNoText = new SYMCText(group, textFormData);
        vpmEcoNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        vpmEcoNoText.setEnabled(false);

        // /////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////Basic Info End ///////////////////////////////////

        // ///////////////////////////////////////Eng. Info Start///////////////////////////////////
        // /////////////////////////////////////////////////////////////////////////////////////////

        Group group2 = new Group(this, SWT.NONE);
        group2.setLayout(groupLayout);
        group2.setText("Eng. Info");
        group2.setBackground(new Color(null, 255, 255, 255));

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel colorIDLabel = new SYMCLabel(group2, "Color ID", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(0);
        comboFormData.left = new FormAttachment(colorIDLabel, 5);
        comboFormData.height = 20;
        colorIDCB = new SYMCLOVComboBox(group2, SWT.BORDER, session, "s7_COLOR");
        colorIDCB.setMandatory(true);
        colorIDCB.setLayoutData(comboFormData);
        
        //colorIDCB ���� �����Ͽ��� ���������� �Է��� �ȵǴ� ���� ����
        colorIDCB.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                // Color ID ���� ���� UI����
            	String sColorID = colorIDCB.getSelectedString();
				if (sColorID != null && !sColorID.equals("") && sColorID.length() > 0 && !sColorID.equals(".")) {
					if (colorSectionCB != null) {
						colorSectionCB.setEnabled(true);
					}
				} else {
					if (colorSectionCB != null) {
						colorSectionCB.setSelectedItem("");
						colorSectionCB.setEnabled(false);
					}
				}
            }
        });
//        colorIDCB.getTextField().addListener(SWT.Verify, new Listener() {
//			@Override
//			public void handleEvent(Event paramEvent) {
//				// ���� ȭ�� Open�� Color ID���� ���� Setting�Ǿ����� �����Ƿ� �Ʒ� �������� ������ ���⶧���� �Ʒ��� ���� ����.
//				if (!isInit) {
//					String sColorID = colorIDCB.getSelectedString();
//					if (sColorID != null && !sColorID.equals("") && sColorID.length() > 0 && !sColorID.equals(".")) {
//						if (colorSectionCB != null) {
//							colorSectionCB.setEnabled(true);
//						}
//					} else {
//						if (colorSectionCB != null) {
//							colorSectionCB.setSelectedItem("");
//							colorSectionCB.setEnabled(false);
//						}
//					}
//				}
//				
//				isInit = false;
//			}
//		});
//
//        colorIDCB.redraw();

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(0);
        labelFormData.left = new FormAttachment(colorIDCB, 62);
        SYMCLabel selPartLabel = new SYMCLabel(group2, "Selective Part", labelFormData);

        // [SR140617-016] [20140422] Selective part ��� �߰�. (code by ������)
        comboFormData = new FormData(202, 18);
        comboFormData.top = new FormAttachment(0);
        comboFormData.left = new FormAttachment(selPartLabel, 5);
        comboFormData.height = 20;
        selectivePartCB = new SYMCLOVComboBox(group2, SWT.BORDER, session, "S7_SELECTIVE_PART");
        selectivePartCB.setLayoutData(comboFormData);

        selectivePartCB.redraw();

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(selectivePartCB, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel colorSectionLabel = new SYMCLabel(group2, "Color Section No", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(selectivePartCB, 5);
        comboFormData.left = new FormAttachment(colorSectionLabel, 5);
        comboFormData.height = 20;
        colorSectionCB = new SYMCLOVComboBox(group2, SWT.BORDER, session, "S7_COLOR_SECTION_ID");
        colorSectionCB.setLayoutData(comboFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(selectivePartCB, 5);
        labelFormData.left = new FormAttachment(colorSectionCB, 62);
        SYMCLabel materialLabel = new SYMCLabel(group2, "Material", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(selectivePartCB, 5);
        textFormData.left = new FormAttachment(materialLabel, 5);
        materialText = new SYMCText(group2, textFormData);
        materialText.setEnabled(false);
        materialText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(selectivePartCB, 5);
        buttonFormData.left = new FormAttachment(materialText, 5);
        Button materialButton = new Button(group2, SWT.PUSH);
        materialButton.setImage(registry.getImage("Search.ICON"));
        materialButton.setLayoutData(buttonFormData);
        materialButton.addSelectionListener(new RevSelectionAdapter(materialText, "S7_MaterialRevision"));
        
        // [SR160328-033][20160330][jclee] Material Clear Button
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(selectivePartCB, 5);
        buttonFormData.left = new FormAttachment(materialButton, 5);
        Button materialClearButton = new Button(group2, SWT.PUSH);
        materialClearButton.setImage(registry.getImage("Clear.ICON"));
        materialClearButton.setLayoutData(buttonFormData);
        materialClearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				materialText.setText("");
				materialText.setData(null);
			}
		});

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(materialText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel categoryLabel = new SYMCLabel(group2, "Category", labelFormData);
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(materialText, 5);
        comboFormData.left = new FormAttachment(categoryLabel, 5);
        comboFormData.height = 20;
        categoryCB = new SYMCLOVComboBox(group2, SWT.BORDER, session, "S7_CATEGORY");
        categoryCB.setLayoutData(comboFormData);
        // [SR141126-021][2014.11.27][jclee] Category �ʼ� �Է� ������ ����.
        categoryCB.setMandatory(true);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(materialText, 5);
        labelFormData.left = new FormAttachment(categoryCB, 62);
        SYMCLabel alterMaterialLabel = new SYMCLabel(group2, "Alter Material", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(materialText, 5);
        textFormData.left = new FormAttachment(alterMaterialLabel, 5);
        alterMaterialText = new SYMCText(group2, textFormData);
        alterMaterialText.setEnabled(false);
        alterMaterialText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(materialText, 5);
        buttonFormData.left = new FormAttachment(alterMaterialText, 5);
        Button altermaterialButton = new Button(group2, SWT.PUSH);
        altermaterialButton.setImage(registry.getImage("Search.ICON"));
        altermaterialButton.setLayoutData(buttonFormData);
        altermaterialButton.addSelectionListener(new RevSelectionAdapter(alterMaterialText, "S7_MaterialRevision"));

        // [SR160328-033][20160330][jclee] AlterMaterial Clear Button
        buttonFormData = new FormData(21, 21);
        buttonFormData.top = new FormAttachment(materialText, 5);
        buttonFormData.left = new FormAttachment(altermaterialButton, 5);
        Button altermaterialClearButton = new Button(group2, SWT.PUSH);
        altermaterialClearButton.setImage(registry.getImage("Clear.ICON"));
        altermaterialClearButton.setLayoutData(buttonFormData);
        altermaterialClearButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				alterMaterialText.setText("");
				alterMaterialText.setData(null);
			}
		});
        
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(alterMaterialText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel materiaThicklLabel = new SYMCLabel(group2, "Material Thick.", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(alterMaterialText, 5);
        textFormData.left = new FormAttachment(materiaThicklLabel, 5);
        materialThickText = new SYMCText(group2, textFormData);

        labelFormData = new FormData(130, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(alterMaterialText, 5);
        labelFormData.left = new FormAttachment(materialThickText, 33);
        SYMCLabel alterMaterialThickLabel = new SYMCLabel(group2, "Alter Material Thick.", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(alterMaterialText, 5);
        textFormData.left = new FormAttachment(alterMaterialThickLabel, 5);
        alterMaterialThcikText = new SYMCText(group2, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(alterMaterialThcikText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel finishLabel = new SYMCLabel(group2, "Finish", labelFormData);
        textFormData = new FormData(202, 16);
        textFormData.top = new FormAttachment(alterMaterialThcikText, 5);
        textFormData.left = new FormAttachment(finishLabel, 5);
        fisnishText = new SYMCText(group2, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(alterMaterialThcikText, 5);
        labelFormData.left = new FormAttachment(materialThickText, 62);
        SYMCLabel responLabel = new SYMCLabel(group2, "DWG Creator", labelFormData); //[SR180130-033][LJG] "Responsibility" => "DWG Creator" �� ����
        comboFormData = new FormData(207, SWT.DEFAULT);
        comboFormData.top = new FormAttachment(alterMaterialThcikText, 5);
        comboFormData.left = new FormAttachment(responLabel, 5);
        comboFormData.height = 20;
        responCB = new SYMCLOVComboBox(group2);
        responCB.setLayoutData(comboFormData);
        responCB.setMandatory(true);
        TCComponentListOfValues lov = TCComponentListOfValuesType.findLOVByName("S7_RESPONSIBILITY");
		String[] str = null;
		try {
			str = lov.getListOfValues().getStringListOfValues();
			for(int i=0; i<str.length; i++){
				if(!str[i].startsWith("White Box") && !str[i].startsWith("Black Box") && !str[i].startsWith("Gray Box") && !str[i].startsWith("SYMC")){
					responCB.addItem(str[i]);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
//        responCB.addActionListener(SWT.Selection, new Listener() {
//			@Override
//			public void handleEvent(Event arg0) {
//				if(isRendering){
//					return;
//				}
//				String	s7_RESPONSIBILITY = responCB.getSelectedString();
//				if(s7_RESPONSIBILITY.equalsIgnoreCase("White Box") || s7_RESPONSIBILITY.equalsIgnoreCase("Black Box") || s7_RESPONSIBILITY.equalsIgnoreCase("Gray Box")){
//					MessageBox.post(VehiclePartMasterInfoPanel.this.getShell(), "You cannot select White/Black/Gray Box", "Warning", MessageBox.INFORMATION);
//					responCB.setText("");
//					return;
//				}
//			}
//		});

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(responCB, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel estWeightLabel = new SYMCLabel(group2, "Est. Weight", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(responCB, 5);
        textFormData.left = new FormAttachment(estWeightLabel, 5);
        estWeightText = new SYMCText(group2, textFormData);
        labelFormData = new FormData();
        labelFormData.top = new FormAttachment(responCB, 5);
        labelFormData.left = new FormAttachment(estWeightText, 5);
        Label kg2label = new Label(group2, SWT.NONE);
        kg2label.setText("��");
        kg2label.setLayoutData(labelFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(responCB, 5);
        labelFormData.left = new FormAttachment(estWeightText, 63);
        SYMCLabel calSurfaceBoxLabel = new SYMCLabel(group2, "Cal. Surface", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(responCB, 5);
        textFormData.left = new FormAttachment(calSurfaceBoxLabel, 5);
        calSurfaceText = new SYMCText(group2, textFormData);
        labelFormData = new FormData();
        labelFormData.top = new FormAttachment(responCB, 5);
        labelFormData.left = new FormAttachment(calSurfaceText, 5);
        Label m2label = new Label(group2, SWT.NONE);
        m2label.setText("��");
        m2label.setLayoutData(labelFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(calSurfaceText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel calWeightLabel = new SYMCLabel(group2, "Cal. Weight", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(calSurfaceText, 5);
        textFormData.left = new FormAttachment(calWeightLabel, 5);
        calWeightText = new SYMCText(group2, textFormData);
        labelFormData = new FormData();
        labelFormData.top = new FormAttachment(calSurfaceText, 5);
        labelFormData.left = new FormAttachment(calWeightText, 5);
        Label kg3label = new Label(group2, SWT.NONE);
        kg3label.setText("��");
        kg3label.setLayoutData(labelFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(calSurfaceText, 5);
        labelFormData.left = new FormAttachment(kg3label, 46);
        SYMCLabel asEndItemLabel = new SYMCLabel(group2, "A/S End Item", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(calSurfaceText, 5);
        textFormData.left = new FormAttachment(asEndItemLabel, 5);
        asEndItemText = new SYMCText(group2, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(asEndItemText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel actWeightLabel = new SYMCLabel(group2, "Act. Weight", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(asEndItemText, 5);
        textFormData.left = new FormAttachment(actWeightLabel, 5);
        actWeightText = new SYMCText(group2, textFormData);
        labelFormData = new FormData();
        labelFormData.top = new FormAttachment(asEndItemText, 5);
        labelFormData.left = new FormAttachment(actWeightText, 5);
        Label kg4label = new Label(group2, SWT.NONE);
        kg4label.setText("��");
        kg4label.setLayoutData(labelFormData);
        buttonFormData = new FormData(23, 23);
        buttonFormData.top = new FormAttachment(calWeightText, 5);
        buttonFormData.left = new FormAttachment(kg4label, 5);
        updateActualWeightButton = new Button(group2, SWT.PUSH);
        updateActualWeightButton.setImage(registry.getImage("Update.ICON"));
        updateActualWeightButton.setLayoutData(buttonFormData);
        updateActualWeightButton.setData(AbstractSYMCViewer.SKIP_ENABLE, "true");
        updateActualWeightButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				UpdateActWeightDialog dialog = new UpdateActWeightDialog(getShell());
				dialog.open();
			}
		});
        
        
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////
        
         /**
          * E-BOM ���� ���� 
          * Part ����/���� UI �� Target Weight �Ӽ� �߰�     
          */
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(asEndItemText, 5);
        labelFormData.left = new FormAttachment(updateActualWeightButton, 18);
        SYMCLabel label_tartgetWeight = new SYMCLabel(group2, "Target Weight", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(asEndItemText, 5);
        textFormData.left = new FormAttachment(label_tartgetWeight, 5);
        text_targetWeight = new SYMCText(group2, textFormData);
        labelFormData = new FormData();
        labelFormData.top = new FormAttachment(asEndItemText, 5);
        labelFormData.left = new FormAttachment(text_targetWeight, 5);
        Label kg5label = new Label(group2, SWT.NONE);
        kg5label.setText("��");
        kg5label.setLayoutData(labelFormData);
        buttonFormData = new FormData(23, 23);
        buttonFormData.top = new FormAttachment(asEndItemText, 5);
        buttonFormData.left = new FormAttachment(kg5label, 5);
        Button updateTargetWeightButton = new Button(group2, SWT.PUSH);
        updateTargetWeightButton.setImage(registry.getImage("Update.ICON"));
        updateTargetWeightButton.setLayoutData(buttonFormData);
        updateTargetWeightButton.setData(AbstractSYMCViewer.SKIP_ENABLE, "true");
        updateTargetWeightButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				UpdateTargetWeightDialog dialog = new UpdateTargetWeightDialog(getShell());
				dialog.open();
			}
		});
        
        
        
        /**
         * E-BOM ���� ���� Regulation, Critical �Ӽ� �߰�
         */
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(actWeightText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel regulationLabel = new SYMCLabel(group2, "Regulation", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(actWeightText, 5);
        textFormData.left = new FormAttachment(regulationLabel, 5);
        text_regulation = new SYMCText(group2, textFormData);

        labelFormData = new FormData(130, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(text_targetWeight, 5);
        labelFormData.left = new FormAttachment(text_regulation, 33);
        SYMCLabel criticalLabel = new SYMCLabel(group2, "Critical", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(text_targetWeight, 5);
        textFormData.left = new FormAttachment(criticalLabel, 5);
        text_critical = new SYMCText(group2, textFormData);
        
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(text_regulation, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel boundingBoxLabel = new SYMCLabel(group2, "Bounding Box", labelFormData);
        textFormData = new FormData(581, 18);
        textFormData.top = new FormAttachment(text_regulation, 5);
        textFormData.left = new FormAttachment(boundingBoxLabel, 5);
        boundingBoxText = new SYMCText(group2, textFormData);

        // [SR140324-030][20140619] KOG DEV UI Initialize �κп� SES Spec No. Label, Text �߰�
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(boundingBoxText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel sesSpecNoLabel = new SYMCLabel(group2, "SES Spec No.", labelFormData);
        textFormData = new FormData(568, 36);
        textFormData.top = new FormAttachment(boundingBoxText, 5);
        textFormData.left = new FormAttachment(sesSpecNoLabel, 5);
        sesSpecNoText = new SYMCText(group2, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL | SWT.MULTI, textFormData);
        sesSpecNoText.setData("SES_SPEC_NO_TEXT");
        
        buttonFormData = new FormData(36, 36);
        buttonFormData.top = new FormAttachment(boundingBoxText, 5);
        buttonFormData.left = new FormAttachment(sesSpecNoText, 5);
        updateSESSpecNoButton = new Button(group2, SWT.PUSH);
        updateSESSpecNoButton.setImage(registry.getImage("Update.ICON"));
        updateSESSpecNoButton.setLayoutData(buttonFormData);
        updateSESSpecNoButton.setData(AbstractSYMCViewer.SKIP_ENABLE, "true");
        updateSESSpecNoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				UpdateSESSpecNoDialog dialog = new UpdateSESSpecNoDialog(getShell());
				dialog.open();
			}
		});

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(sesSpecNoText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel testReportLabel = new SYMCLabel(group2, "Test Report No", labelFormData);
        textFormData = new FormData(581, 18);
        textFormData.top = new FormAttachment(sesSpecNoText, 5);
        textFormData.left = new FormAttachment(testReportLabel, 5);
        testReportText = new SYMCText(group2, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(testReportText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel nameSpecLabel = new SYMCLabel(group2, "Name Spec", labelFormData);
        textFormData = new FormData(581, 18);
        textFormData.top = new FormAttachment(testReportText, 5);
        textFormData.left = new FormAttachment(nameSpecLabel, 5);
        nameSpecText = new SYMCText(group2, textFormData);

        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(nameSpecText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel chgDescLabel = new SYMCLabel(group2, "Chg.Description", labelFormData);
        textFormData = new FormData(585, 36);
        textFormData.top = new FormAttachment(nameSpecText, 5);
        textFormData.left = new FormAttachment(chgDescLabel, 5);
        chgDescText = new SYMCText(group2, SWT.BORDER | SWT.MULTI, textFormData);
        
        labelFormData = new FormData(100, SWT.DEFAULT);
        labelFormData.top = new FormAttachment(chgDescText, 5);
        labelFormData.left = new FormAttachment(0);
        SYMCLabel alcCodeLabel = new SYMCLabel(group2, "ALC Code", labelFormData);
        textFormData = new FormData(202, 18);
        textFormData.top = new FormAttachment(chgDescText, 5);
        textFormData.left = new FormAttachment(alcCodeLabel, 5);
        alcCodeText = new SYMCText(group2, textFormData);
        alcCodeText.setEnabled(false);	// [SR140729-026][20140825] jclee, ALC Code ���� �Ұ�.

        // /////////////////////////////////////////////////////////////////////////////////////////
        // ///////////////////////////////////////Eng. Info End/////////////////////////////////////
        // /////////////////////////////////////////////////////////////////////////////////////////

        this.pack();

    }

    /**
     * Drw Status���� ���� Showon No/DwgSize/CatV4Type UI ����
     * 
     */
    private void changeTypeUI() {
        String strDwgStatus = dwgStatusCodeCB.getSelectedString();
        // Drw Status ���� '.' �ΰ�� DwgSize/CatV4Type �ʼ�
        if (".".equals(strDwgStatus)) {
            this.showOnNoButton.setEnabled(false);
            this.dwgSizeCodeCB.setEnabled(true);
            // [SR150521-050][2015.06.25][jclee] CAT V4 �ԷºҰ�
//            this.catV4CB.setEnabled(true);
//            this.catV4CB.setEnabled(false);

            shownOnNoText.setText("");
            shownOnNoText.setData(null);
        }
        // Drw Status ���� 'H' �ΰ�� Showon No. �ʼ�
        else if ("H".equals(strDwgStatus)) {
            showOnNoButton.setEnabled(true);
            this.dwgSizeCodeCB.setEnabled(false);
//            this.catV4CB.setEnabled(false);

            this.dwgSizeCodeCB.setSelectedIndex(0);
            this.catV4CB.setSelectedIndex(0);
            
         // Dwg Status�� "."�� �ƴ� ��� Regulation / Critical �� ����
            text_regulation.setText("");
            text_critical.setText("");

        } else if ("K".equals(strDwgStatus)) {
            showOnNoButton.setEnabled(false);
            this.dwgSizeCodeCB.setEnabled(false);
//            this.catV4CB.setEnabled(false);

            this.dwgSizeCodeCB.setSelectedIndex(0);
            this.catV4CB.setSelectedIndex(0);

            shownOnNoText.setText("");
            shownOnNoText.setData(null);
            
         // Dwg Status�� "."�� �ƴ� ��� Regulation / Critical �� ����
            text_regulation.setText("");
            text_critical.setText("");
        }

    }

    /**
     * Control ���� �� ������ �߰�
     * 
     */
    private void setControlData() {

        materialThickText.setInputType(SYMCText.DOUBLE);
        alterMaterialThcikText.setInputType(SYMCText.DOUBLE);
        estWeightText.setInputType(SYMCText.DOUBLE);
        calWeightText.setInputType(SYMCText.DOUBLE);
        actWeightText.setInputType(SYMCText.DOUBLE);
        calSurfaceText.setInputType(SYMCText.DOUBLE);
        /////////////////////////////////////////////////////////////////////////////////////////////////
        text_targetWeight.setInputType(SYMCText.DOUBLE);
        ////////////////////////////////////////////////////////////////////////////////////////////////

        String strRegular = regularCB.getSelectedString();

        if (!"R".equals(strRegular)) {
            unitCB.setMandatory(false);
            sysCodeCB.setMandatory(false);
            dwgStatusCodeCB.setMandatory(false);
            colorIDCB.setMandatory(false);
            responCB.setMandatory(false);
            estWeightText.setMandatory(false);
            estWeightText.redraw();

            partOrignCB.setEnabled(false);
            partOrignCB.setSelectedString("T");

            partNoText.setEnabled(false);
            partNameText.setEnabled(true);
            koreanNameText.setEnabled(true);
            partNameButton.setEnabled(false);

            partNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            partNameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
            koreanNameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

            try {

                String strNewID = SYMTcUtil.getNewID("T", 10);
                partNoText.setText(strNewID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Red "*" �ڵ�������� ����
            estWeightText.setMandatory(true);

        }

    }

    @Override
    public boolean isPageComplete() {
        return true;
    }

    /**
     * Revision �˻��� ���Ǵ� Selection Adapter
     */
    class RevSelectionAdapter extends SelectionAdapter {
        /** �˻��� Item ID���� Setting�� Field */
        SYMCText targetText;
        /** �˻��� Revision Type */
        String strItemRevType;

        /**
         * Selection Adapter ������
         * 
         * @param targetText
         *            : �˻��� Item ID���� Setting�� Field
         * @param strItemRevType
         *            : �˻��� Revision Type
         */
        RevSelectionAdapter(SYMCText targetText, String strItemRevType) {
            this.targetText = targetText;
            this.strItemRevType = strItemRevType;
        }

        public void widgetSelected(SelectionEvent event) {
            // �˻� Dialog
            SearchItemRevDialog itemDialog = new SearchItemRevDialog(getShell(), SWT.SINGLE, strItemRevType);
            // ���õ� Revision
          //20230831 cf-4357 seho ��Ʈ �˻� dialog ��������.. �˻� ��� �κ� ����.
            TCComponent[] selectedItems = (TCComponent[]) itemDialog.open();

            if (selectedItems != null) {
                // Project�� ItemID�� ����
                if (SYMCClass.S7_PROJECTREVISIONTYPE.equals(this.strItemRevType)) {
                    try {
                        targetText.setText(selectedItems[0].getProperty("item_id"));
                    } catch (TCException e) {
                        e.printStackTrace();
                    }
                }
                // ItemID/Revision Object ����
                else {
                    targetText.setText(selectedItems[0].toDisplayString());
                    targetText.setData(selectedItems[0]);
                }

            }
        }
    }

    /**
     * Item �˻��� ���Ǵ� Selection Adapter
     */
    class ItemSelectionAdapter extends SelectionAdapter {
        /** �˻��� Item ID���� Setting�� Field */
        SYMCText targetText;
        /** �˻��� Item Type */
        String strItemType;

        /**
         * Selection Adapter ������
         * 
         * @param targetText
         *            : �˻��� Item ID���� Setting�� Field
         * @param strItemRevType
         *            : �˻��� Item Type
         */
        ItemSelectionAdapter(SYMCText targetText, String strItemType) {
            this.targetText = targetText;
            this.strItemType = strItemType;
        }

        public void widgetSelected(SelectionEvent event) {

            SearchItemDialog itemDialog = new SearchItemDialog(getShell(), SWT.SINGLE, strItemType);
            TCComponentItem[] selectedItems = (TCComponentItem[]) itemDialog.open();

            if (selectedItems != null) {
                try {
                    targetText.setText(selectedItems[0].getProperty("item_id"));
                    targetText.setData(selectedItems[0]);
                } catch (TCException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * TC ViewerTab���� ȭ�� �̵��� ������ �׸��� �����ϴ��� Check
     * 
     * ���� Loading�� �Ӽ����� ���� �Ӽ����� ��
     */
    public boolean isModified() {
        if (this.targetRevision == null || !this.targetRevision.isCheckedOut())
            return false;

        HashMap<String, Object> newAttrMap = new HashMap<String, Object>();

        try {
            this.getPropDataMap(newAttrMap);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Object newValue = "";
        Object oldValue = null;
        for (Object key : this.oldAttrMap.keySet().toArray()) {
            oldValue = this.oldAttrMap.get(key);
            if (oldValue == null)
                oldValue = "";
            newValue = newAttrMap.get(key);
            if (newValue == null)
                newValue = "";
            if (!oldValue.equals(newValue))
                return true;
        }

        return false;
    }

	/**
	 * [SR140324-030][20140620] KOG DEV PropertyViewer (Viewer) ���� User role Check for SES Spec No. Update Command Enable.
	 */
	private boolean isSESSpecNoAccessCheck() {
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		String type = targetComponents[0].getType();
		TCComponentItemRevision revision = null;
		if (type.equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || type.equals(SYMCClass.S7_STDPARTREVISIONTYPE)) {
			revision = (TCComponentItemRevision) targetComponents[0];
		} else {
			return false;
		}

		try {
			if (isWorkingStatus(revision)) {
				return true;
			} else {
				TCComponentUser user = CustomUtil.getTCSession().getUser();
				TCComponentGroup[] groups = user.getGroups();
				for (TCComponentGroup group : groups) {
					TCComponentRole[] roles = user.getRoles(group);
					for (TCComponentRole role : roles) {
						if (("CLASSIFICATIONADMIN").equals(role.toDisplayString())) {
							return true;
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return false;
	}

    /**
     * Title: Component�� Working Status���� Check�� Usage:
     * [SR ����][20141229][jclee] Component�� In Work �����̰� Workflow�� Process Stage�� Creator�� ��� Working Status�� true�� �ν��ϴ� ���� �߰�
     * 
     * @param components
     * @return boolean Component�� Working�̸� true, �׷��� ������ false
     */
    public static boolean isWorkingStatus(TCComponent components) throws TCException {
        String sMaturity = components.getProperty("s7_MATURITY");
        String sReleaseStatusList = components.getProperty("release_status_list");
        String sProcessStageList = components.getProperty("process_stage_list");
        String sProcessStage = "";
        TCComponent[] process_stage_list = components.getReferenceListProperty("process_stage_list");
        
        // 1. Release�Ǿ����� �ʰ� Workflow�� ���� ���
        if (sReleaseStatusList.equalsIgnoreCase("") && sProcessStageList.equalsIgnoreCase("")) {
			return true;
		}
        
        // 2. Workflow�� ���������� Part�� Maturity�� In Work�̰� Workflow�� Process Stage�� Creator�� ���. (Workflow "Reject")
        if (process_stage_list.length != 0) {
        	for (int inx = 0; inx < process_stage_list.length; inx++) {
        		if (process_stage_list[inx].getType().equals("EPMReviewTask")) {
        			sProcessStage = process_stage_list[inx].getProperty("current_name");
        		}
        	}
        	
        	// 2.1. Part�� Maturity�� In Work�̰� Workflow�� Process Stage�� Creator�� ���
        	if (sMaturity.equalsIgnoreCase("In Work") && sProcessStage.equalsIgnoreCase("Creator")) {
        		return true;
        	}
		}
        
        return false;
    }
    
    /**
     * Value�� Null�� ���� ��� �� String���� ��������
     * @param hm
     * @return
     */
    private HashMap<String, String> setNullToString(HashMap<String, String> hm) {
    	HashMap<String, String> hmNew = new HashMap<String, String>();
    	
    	Iterator<String> i = hm.keySet().iterator();
    	while (i.hasNext()) {
    		String sKey = i.next();
			Object oValue = hm.get(sKey);
			
			if (oValue == null) {
				hmNew.put(sKey, "");
			} else {
				hmNew.put(sKey, oValue.toString().trim());
			}
		}
    	
    	return hmNew;
    }
    
    public void setRenderingMode(){
    	isRendering = true;
    }
}
