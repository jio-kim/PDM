package com.symc.plm.rac.prebom.prebom.view.prevehiclepart;

import java.text.DecimalFormat;
import java.util.HashMap;

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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;

import com.kgm.commands.partmaster.vehiclepart.PartNameDialog;
import com.kgm.common.SYMCLOVComboBox;
import com.kgm.common.SYMCLabel;
import com.kgm.common.SYMCText;
import com.kgm.common.swtsearch.SearchItemRevDialog;
import com.kgm.common.utils.CustomUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.SDVLOVUtils;
import com.symc.plm.rac.prebom.prebom.dialog.preccn.CCNSearchDialog;
import com.symc.plm.rac.prebom.prebom.validator.prevehiclepart.PreVehiclePartValidator;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * [SR140617-016] [20140422] Selective part 기능 추가. (code by 정윤재)
 * [SR140616-019][20140619][bskwak] 비정규 품번 Concept 단계 품번생성 오류 개선 : Stage 구분 없이 모두 기본 revision 초기값 사용하는 것으로 변경 함.
 * [SR140729-026][20140730][jclee] Part Revision의 ALC Code 정보 Display 기능 추가.
 * [20160715][ymjang] 중량 표시 자리수 조정
 * [20161209][ymjang] Cal Weight --> Tgt Weight 로 변경함.
 * [20160103] projectCodeText --> projCodeCB 변경. LOV로 변경함
 * @author bs
 * 
 */
public class PreVehiclePartInfoPanel extends Composite implements IPageComplete {

	/** TC Registry */
	private Registry registry;
	/** TC Session */
	private TCSession session;

	/** Part Orign Combo */
	protected SYMCLOVComboBox partOrignCB;

	/** Stage Combo */
	protected SWTComboBox partStageCB;
	/** Unit Combo */
	protected SYMCLOVComboBox unitCB;
	/** System Code Combo */
	protected SYMCLOVComboBox sysCodeCB;
	/** Category Combo */
	protected SWTComboBox categoryCB;
	/** Color Combo */
	protected SWTComboBox colorIDCB;

	// [SR140617-016] [20140422] Selective part 기능 추가. (code by 정윤재)
	/** Selective Part Combo */
	//    protected SYMCLOVComboBox selectivePartCB;

	// Category, Color ID는 기본 값이 .으로.
	// Selective Part 는 필요 없음.
	// 
	/** Part No. Field */
	protected SYMCText partNoText;
	/** Part Rev. Field */
	protected SYMCText partRevisionText;
	/** Display Part No. Field */
	//    protected SYMCText dispPartNoText;
	/** PartName Field */
	protected SYMCText partNameText;
	/** Part Name Search Button */
	protected Button partNameButton;
	/** Kor PartName Field */
	protected SYMCText koreanNameText;
	/** CCN No. Field */
	protected SYMCText ccnNoText;
	protected Button ccnNoButton;

	/** Project Code Field */
	//protected SYMCText projectCodeText;
	/** Project Code Combo */
	protected SYMCLOVComboBox projCodeCB;

	/** Selective Part Field */
	//    protected SYMCText selPartText;
	/** Est. Weight Field */
	protected SYMCText estWeightText;
	/** Cal. Weight Field */
	protected SYMCText calWeightText;

	/** Change Description Field */
	protected SYMCText chgDescText;

	/** Contents Field */
	//    protected SYMCText contentsText;

	/** Project Search Button */
	//protected Button projSchButton;

	/** SaveAs시 Target Revision */
	TCComponentItemRevision baseItemRev;
	/** 조회시 Target Revision */
	TCComponentItemRevision targetRevision;
	/** 화면에 입력된 속성 Map */
	public HashMap<String, Object> attrMap;
	/** 최초 Loading시 속성 Map(수정항목 Check를 위해 사용, attrMap과 oldAttrMap을 비교) */
	HashMap<String, Object> oldAttrMap;
	/** PartManage Dialog에서 넘어온 Param Map */
	HashMap<String, Object> paramMap;

	TCComponentBOMLine targetBOMLine;

	/**
	 * Create Part Menu를 통해 호출됨
	 * 
	 * @param parent
	 * @param paramMap
	 *            : PartManage Dialog에서 넘어온 Param Map
	 * @param style
	 *            : Dialog SWT Style
	 */
	public PreVehiclePartInfoPanel(Composite parent, HashMap<String, Object> paramMap, int style) {
		super(parent, style);
		this.registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.view.view");
		this.session = CustomUtil.getTCSession();
		this.attrMap = new HashMap<String, Object>();
		this.paramMap = paramMap;

		initUI();

		this.setInitData(paramMap);
		setControlData();
	}

	/**
	 * Revision 선택 후 ViewerTab에서 호출
	 * 
	 * @param parent
	 * @param style
	 */
	public PreVehiclePartInfoPanel(Composite parent, int style) {
		super(parent, style);
		registry = Registry.getRegistry("com.symc.plm.rac.prebom.prebom.view.view");
		session = CustomUtil.getTCSession();
		attrMap = new HashMap<String, Object>();
		oldAttrMap = new HashMap<String, Object>();
		initUI();

		setViewMode();
		try {
			InterfaceAIFComponent comp = AIFUtility.getCurrentApplication().getTargetComponent();
			// Target이 Revision인 경우
			//            if (comp != null && comp instanceof TCComponentItemRevision) {
			//                targetRevision = (TCComponentItemRevision) comp;
			//                this.setInitData(targetRevision);
			//                this.getPropDataMap(this.oldAttrMap);
			//            }
			//            else // Target이 BomLine인 경우
			if (comp != null && comp instanceof TCComponentBOMLine) {
				targetBOMLine = (TCComponentBOMLine) comp;
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
	 * 조회화면인 경우 수정불가 항목 Setting
	 */
	public void setViewMode() {

		partOrignCB.setEnabled(false);
		//projectCodeText.setEnabled(false);
		partStageCB.setEnabled(false);
		unitCB.setEnabled(false);

		partNoText.setEnabled(false);
		partRevisionText.setEnabled(false);
		partNameText.setEnabled(false);
		koreanNameText.setEnabled(false);

		// partNameButton.setEnabled(false);
		// projSchButton.setEnabled(false);

		ccnNoText.setEnabled(false);
		ccnNoButton.setEnabled(false);

		//        contentsText.setEnabled(false);
	}

	/**
	 * 화면에 입력된 속성 값을 저장
	 * 
	 * @param attributeMap
	 *            : 속성값이 저장될 HashMap
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, Object> getPropDataMap(HashMap<String, Object> attributeMap) throws Exception {
		attributeMap.put(PropertyConstant.ATTR_NAME_ITEMTYPE, TypeConstant.S7_PREVEHICLEPARTTYPE);

		//String projectCode = projectCodeText.getText();
		String projectCode = projCodeCB.getSelectedString();
		String systemCode = sysCodeCB.getSelectedString();
		attributeMap.put(PropertyConstant.ATTR_NAME_ITEMID, BomUtil.getNewId(projectCode, systemCode));

		// Part No.
		attributeMap.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, partNoText.getText());
		attributeMap.put(PropertyConstant.ATTR_NAME_ITEMREVID, partRevisionText.getText());

		attributeMap.put(PropertyConstant.ATTR_NAME_ITEMNAME, partNameText.getText());

		// Part Orign
		attributeMap.put(PropertyConstant.ATTR_NAME_PARTTYPE, partOrignCB.getSelectedString());
		// Project No.
		attributeMap.put(PropertyConstant.ATTR_NAME_PROJCODE, projectCode);
		// Part Stage
		attributeMap.put(PropertyConstant.ATTR_NAME_STAGE, partStageCB.getSelectedItem());

		// Display Part No.(Excel 참고)
		//        attributeMap.put(PropertyConstant.ATTR_NAME_DISPLAYPARTNO, dispPartNoText.getText());

		// Part Kor Name
		attributeMap.put(PropertyConstant.ATTR_NAME_KORNAME, koreanNameText.getText());
		// Unit
		attributeMap.put(PropertyConstant.ATTR_NAME_UOMTAG, unitCB.getSelectedString());
		// System Code
		attributeMap.put(PropertyConstant.ATTR_NAME_BL_BUDGETCODE, systemCode);

		// CCN No.(TypedReference)
		attributeMap.put(PropertyConstant.ATTR_NAME_CCNNO, ccnNoText.getData());

		// Category
		attributeMap.put(PropertyConstant.ATTR_NAME_REGULATION, categoryCB.getSelectedItem());

		// [SR140617-016] [20140422] Selective part 기능 추가. (code by 정윤재)
		// Selective Part
		//        attributeMap.put(PropertyConstant.ATTR_NAME_SELECTIVEPART, selectivePartCB.getSelectedString());

		// Color ID
		attributeMap.put(PropertyConstant.ATTR_NAME_COLORID, colorIDCB.getSelectedItem());
		// Est. Weight
		attributeMap.put(PropertyConstant.ATTR_NAME_ESTWEIGHT, estWeightText.getText());
		// Cal. Weight(kg)
		//[20161209][ymjang] Cal Weight --> Tgt Weight 로 변경함.
		attributeMap.put(PropertyConstant.ATTR_NAME_TARGET_WEIGHT, calWeightText.getText());
		//attributeMap.put(PropertyConstant.ATTR_NAME_CALWEIGHT, calWeightText.getText());

		// Chg. Description
		attributeMap.put("s7_CHANGE_DESCRIPTION", chgDescText.getText());

		// Contents
		//        attributeMap.put(PropertyConstant.ATTR_NAME_CONTENTS, contentsText.getText());

		return attributeMap;
	}

	/**
	 * Create Part 기능을 통해 호출된 경우 속성 값 Setting
	 * 
	 * @param paramMap
	 *            : Part Manage Dialog에서 넘어온 Parameter Map
	 */
	private void setInitData(HashMap<String, Object> paramMap) {
		String strStage = (String) paramMap.get(PropertyConstant.ATTR_NAME_STAGE);
		//        String strRegular = (String) paramMap.get(PropertyConstant.ATTR_NAME_REGULAR);

		// [20150107] TC10 업그레이드 시 문제 확인하여 수정 함.
		//        if(strStage == null || strStage.equals("C")) {
		//        	partRevisionText.setText("A");
		//        } else {
		partRevisionText.setText("000");
		//        }

		if (strStage == null)
			partStageCB.setSelectedItem("C");
		else
			partStageCB.setSelectedItem(strStage);

		ccnNoText.setText("");
		ccnNoText.setData(null);

		TCComponentItemRevision ecoItemRev = (TCComponentItemRevision) paramMap.get(PropertyConstant.CCNITEM);
		if (ecoItemRev != null) {
			ccnNoText.setText(ecoItemRev.toDisplayString());
			ccnNoText.setData(ecoItemRev);
		}

		//        colorIDCB.setSelectedItem(".; NONE");
		colorIDCB.setSelectedItem(".");
		categoryCB.setSelectedItem(".");
	}

	/**
	 * 화면 Data 갱신
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

	/**
	 * SaveAs 혹은 ViewerTab(조회) 기능에서 선택된 Revision의 값 Setting
	 * 
	 * @param targetRevision
	 * @throws TCException
	 */
	private void setInitData(TCComponentItemRevision targetRevision) throws TCException {

		this.attrMap.put("s7_MAIN_NAME", targetRevision.getStringProperty("s7_MAIN_NAME"));
		this.attrMap.put("s7_SUB_NAME", targetRevision.getStringProperty("s7_SUB_NAME"));
		this.attrMap.put("s7_LOC1_FR", targetRevision.getStringProperty("s7_LOC1_FR"));
		this.attrMap.put("s7_LOC2_IO", targetRevision.getStringProperty("s7_LOC2_IO"));
		this.attrMap.put("s7_LOC3_UL", targetRevision.getStringProperty("s7_LOC3_UL"));
		this.attrMap.put("s7_LOC4_EE", targetRevision.getStringProperty("s7_LOC4_EE"));
		this.attrMap.put("s7_LOC5_LR", targetRevision.getStringProperty("s7_LOC5_LR"));

		partNoText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_DISPLAYPARTNO));
		partRevisionText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID));
		partNameText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME));

		// Part Orign
		partOrignCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PARTTYPE));

		// Project No.
		// [SR140702-059][20140626] KOG Veh. Part SaveAs 시에 ProjectCode Blank 처리.
		// [SR140729-026][20140825] jclee, Veh. Part SaveAs 시에 ALCCode Blank 처리.
		// [SR141022-033][20141027] jclee, Veh. Part SaveAs 시에 Test Report Blank 처리.
		if (paramMap != null) {
			//projectCodeText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
			projCodeCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
		} else {
			// projectCodeText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
			projCodeCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_PROJCODE));
		}

		// Part Stage
		partStageCB.setSelectedItem(targetRevision.getProperty(PropertyConstant.ATTR_NAME_STAGE));

		// Display Part No.(Excel 참고)
		//        dispPartNoText.setText(targetRevision.getProperty("s7_DISPLAY_PART_NO"));

		// Part Kor Name
		koreanNameText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_KORNAME));

		// Unit
		unitCB.setSelectedString(targetRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_UOMTAG));
		// System Code
		sysCodeCB.setSelectedString(targetBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_BUDGETCODE));

		// [SR140617-016] [20140422] Selective part 기능 추가. (code by 정윤재)
		// selective Part
		//        selectivePartCB.setSelectedString(targetRevision.getProperty(PropertyConstant.ATTR_NAME_SELECTIVEPART));

		// ECO No.(TypedReference)
		TCComponent ecoComp = targetRevision.getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
		if (ecoComp != null) {
			ccnNoText.setText(ecoComp.toDisplayString());
			ccnNoText.setData(ecoComp);
		}

		// Category
		categoryCB.setSelectedItem(targetRevision.getProperty(PropertyConstant.ATTR_NAME_REGULATION));

		// Selective Part
		// paramMap.put("s7_PART_TYPE", partNameText.getText());

		// Color ID
		colorIDCB.setSelectedItem(targetRevision.getProperty(PropertyConstant.ATTR_NAME_COLORID));

		//[20160715][ymjang] 중량 표시 자리수 조정
		// Est. Weight
		estWeightText.setText(this.getFormatedString(targetRevision.getDoubleProperty(PropertyConstant.ATTR_NAME_ESTWEIGHT), "#########.#########"));
		// Cal. Weight(kg)
		//[20161209][ymjang] Cal Weight --> Tgt Weight 로 변경함. 
		calWeightText.setText(this.getFormatedString(targetRevision.getDoubleProperty(PropertyConstant.ATTR_NAME_TARGET_WEIGHT), "#########.#########"));
		//calWeightText.setText(this.getFormatedString(targetRevision.getDoubleProperty(PropertyConstant.ATTR_NAME_CALWEIGHT), "#########.#########"));

		// Chg. Description
		chgDescText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMDESC));

		// Contents
		//        contentsText.setText(targetRevision.getProperty(PropertyConstant.ATTR_NAME_CONTENTS));
	}

	public String getFormatedString(double value, String format) {

		DecimalFormat df = new DecimalFormat(format);
		return df.format(value);
	}

	/**
	 * 수정(CheckIn)시 Validation Check
	 * 
	 * @return
	 */
	public boolean isSavable() {
		try {
			// 입력된 속성 값을 가져옴
			this.getPropDataMap(this.attrMap);
			// Validation Check
			PreVehiclePartValidator validator = new PreVehiclePartValidator();
			String strMessage = validator.validate(this.attrMap, PreVehiclePartValidator.TYPE_VALID_MODIFY);

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
	 * 수정(CheckIn)시 호출
	 * 
	 * @return
	 */
	public void saveAction() {
		try {
			this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMID);
			this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMTYPE);
			this.attrMap.remove(PropertyConstant.ATTR_NAME_ITEMREVID);
			// DR Check Flag는 속성이 아님
			attrMap.remove("DR_CHECK_FLAG");

			attrMap.remove(PropertyConstant.ATTR_NAME_UOMTAG);

			String[] szKey = attrMap.keySet().toArray(new String[attrMap.size()]);
			TCProperty[] props = targetRevision.getTCProperties(szKey);

			for (int i = 0; i < props.length; i++) {

				if (props[i] == null) {
					System.out.println(szKey[i] + " is Null");
					continue;
				}

				Object value = attrMap.get(szKey[i]);
				CustomUtil.setObjectToPropertyValue(props[i], value);

			}

			// 속성 값 일괄 반영
			targetRevision.setTCProperties(props);
			targetRevision.refresh();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 화면 초기화
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
			// Vehicle Part 생성시 Orign M/T 값은 선택 불가
			public void propertyChange(PropertyChangeEvent event) {

				String strSel = partOrignCB.getSelectedString();

				if (!CustomUtil.isEmpty(strSel) && (strSel.startsWith("M") || strSel.startsWith("T"))) {
					MessageBox.post(getShell(), "No Avaliable Orign Type", "BOM Save", MessageBox.INFORMATION);
					partOrignCB.setSelectedIndex(0);
					return;
				}

			}
		});
		partOriginLabel.setVisible(false);
		partOrignCB.setVisible(false);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(0);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel projectCodeLabel = new SYMCLabel(group, "Project Code", labelFormData);

		FormData textFormData = new FormData(90, 15);
		textFormData.top = new FormAttachment(0);
		textFormData.left = new FormAttachment(projectCodeLabel, 5);
		textFormData.height = 15;

		//        projectCodeText = new SYMCText(group, true, textFormData);
		//        projectCodeText.setLayoutData(textFormData);
		//        projectCodeText.setMandatory(true);
		//        projectCodeText.setEnabled(false);
		//        projectCodeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		projCodeCB = new SYMCLOVComboBox(group, SWT.BORDER, session, PropertyConstant.ATTR_NAME_PROJCODE);
		projCodeCB.setLayoutData(comboFormData);
		projCodeCB.setMandatory(true);


		FormData buttonFormData = new FormData(21, 21);
		buttonFormData.top = new FormAttachment(0);
		//buttonFormData.left = new FormAttachment(projectCodeText, 5);
		buttonFormData.left = new FormAttachment(projCodeCB, 5);

		//        projSchButton = new Button(group, SWT.PUSH);
		//        projSchButton.setImage(registry.getImage("Search.ICON"));
		//        projSchButton.setLayoutData(buttonFormData);
		//        projSchButton.addSelectionListener(new RevSelectionAdapter(projectCodeText, TypeConstant.S7_PREPROJECTREVISIONTYPE));

		//        labelFormData = new FormData(95, SWT.DEFAULT);
		//        labelFormData.top = new FormAttachment(0);
		//        labelFormData.left = new FormAttachment(projSchButton, 10);
		SYMCLabel partStageLabel = new SYMCLabel(group, "Part Stage", labelFormData);
		comboFormData = new FormData(113, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(0);
		comboFormData.left = new FormAttachment(partStageLabel, 5);
		comboFormData.height = 20;
		partStageCB = new SWTComboBox(group, SWT.BORDER);
		SDVLOVUtils.comboValueSetting(partStageCB, "S7_STAGE");
		partStageCB.setLayoutData(comboFormData);
		partStageCB.setEnabled(false);
		//        partStageCB.setMandatory(true);
		//        partStageCB.setVisible(false);
		//        partStageLabel.setVisible(false);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(partStageCB, 5);
		labelFormData.left = new FormAttachment(0);

		SYMCLabel partNoLabel = new SYMCLabel(group, "Part No", labelFormData);
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(partStageCB, 5);
		textFormData.left = new FormAttachment(partNoLabel, 5);

		partNoText = new SYMCText(group, true, textFormData);
		partNoText.setTextLimit(18);
		textFormData = new FormData(20, 18);
		textFormData.top = new FormAttachment(partStageCB, 5);
		textFormData.left = new FormAttachment(partNoText, 5);
		partRevisionText = new SYMCText(group, textFormData);
		partRevisionText.setEnabled(false);
		partRevisionText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		//        labelFormData = new FormData(100, SWT.DEFAULT);
		//        labelFormData.top = new FormAttachment(partStageCB, 5);
		//        labelFormData.left = new FormAttachment(partRevisionText, 29);
		//        SYMCLabel dispPartNoLabel = new SYMCLabel(group, "Display Part No", labelFormData);
		//        textFormData = new FormData(202, 18);
		//        textFormData.top = new FormAttachment(partStageCB, 5);
		//        textFormData.left = new FormAttachment(dispPartNoLabel, 5);
		//        dispPartNoText = new SYMCText(group, textFormData);
		//        dispPartNoText.setEnabled(false);
		//        dispPartNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(partRevisionText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel partNameLabel = new SYMCLabel(group, "Part Name", labelFormData);
		textFormData = new FormData(581, 18);
		textFormData.top = new FormAttachment(partRevisionText, 5);
		textFormData.left = new FormAttachment(partNameLabel, 5);
		partNameText = new SYMCText(group, true, textFormData);
		partNameText.setEnabled(false);
		partNameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		buttonFormData = new FormData(21, 21);
		buttonFormData.top = new FormAttachment(partRevisionText, 5);
		buttonFormData.left = new FormAttachment(partNameText, 5);
		partNameButton = new Button(group, SWT.PUSH);
		partNameButton.setImage(registry.getImage("Search.ICON"));
		partNameButton.setLayoutData(buttonFormData);
		partNameButton.addSelectionListener(new SelectionAdapter() {
			// Part Name 검색 Dialog Open
			public void widgetSelected(SelectionEvent event) {
				// Part Name 검색 Dialog
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
		SYMCLabel ccnNoLabel = new SYMCLabel(group, "CCN No", labelFormData);
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(sysCodeCB, 5);
		textFormData.left = new FormAttachment(ccnNoLabel, 5);
		ccnNoText = new SYMCText(group, textFormData);
		ccnNoText.setEnabled(false);
		//        ccnNoText.setMandatory(true);
		ccnNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		buttonFormData = new FormData(21, 21);
		buttonFormData.top = new FormAttachment(sysCodeCB, 5);
		buttonFormData.left = new FormAttachment(ccnNoText, 5);
		ccnNoButton = new Button(group, SWT.PUSH);
		ccnNoButton.setImage(registry.getImage("Search.ICON"));
		ccnNoButton.setLayoutData(buttonFormData);
		ccnNoButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				// ECO 검색 Dialog
				CCNSearchDialog ccnSearchDialog = new CCNSearchDialog(getShell(), SWT.SINGLE);
				ccnSearchDialog.getShell().setText("CCN Search");

				ccnSearchDialog.open();

				// ECO 검색 결과
				TCComponentItemRevision[] ecos = ccnSearchDialog.getSelectctedECO();
				if (ecos != null && ecos.length > 0) {
					TCComponentItemRevision ecoIR = ecos[0];

					ccnNoText.setText(ecoIR.toDisplayString());
					ccnNoText.setData(ecoIR);
				}
			}
		});

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
		colorIDCB = new SWTComboBox(group2, SWT.BORDER);
		SDVLOVUtils.comboValueSetting(colorIDCB, "s7_COLOR");
		SDVLOVUtils.setMandatory(colorIDCB);
		colorIDCB.setLayoutData(comboFormData);

		colorIDCB.redraw();

		//        labelFormData = new FormData(100, SWT.DEFAULT);
		//        labelFormData.top = new FormAttachment(0);
		//        labelFormData.left = new FormAttachment(colorIDCB, 62);
		//        SYMCLabel selPartLabel = new SYMCLabel(group2, "Selective Part", labelFormData);
		//
		//        // [SR140617-016] [20140422] Selective part 기능 추가. (code by 정윤재)
		//        comboFormData = new FormData(202, 18);
		//        comboFormData.top = new FormAttachment(0);
		//        comboFormData.left = new FormAttachment(selPartLabel, 5);
		//        comboFormData.height = 20;
		//        selectivePartCB = new SYMCLOVComboBox(group2, SWT.BORDER, session, "S7_SELECTIVE_PART");
		//        selectivePartCB.setLayoutData(comboFormData);
		//
		//        selectivePartCB.redraw();

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(colorIDCB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel categoryLabel = new SYMCLabel(group2, "Category", labelFormData);
		comboFormData = new FormData(207, SWT.DEFAULT);
		comboFormData.top = new FormAttachment(colorIDCB, 5);
		comboFormData.left = new FormAttachment(categoryLabel, 5);
		comboFormData.height = 20;
		categoryCB = new SWTComboBox(group2, SWT.BORDER, true);
		SDVLOVUtils.comboValueSetting(categoryCB, "S7_CATEGORY");
		SDVLOVUtils.setMandatory(categoryCB);
		categoryCB.setLayoutData(comboFormData);
		// [SR141126-021][2014.11.27][jclee] Category 필수 입력 란으로 변경.
		categoryCB.setMandatory(true);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(categoryCB, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel estWeightLabel = new SYMCLabel(group2, "Est. Weight", labelFormData);
		textFormData = new FormData(202, 18);
		textFormData.top = new FormAttachment(categoryCB, 5);
		textFormData.left = new FormAttachment(estWeightLabel, 5);
		estWeightText = new SYMCText(group2, textFormData);
		labelFormData = new FormData();
		labelFormData.top = new FormAttachment(categoryCB, 5);
		labelFormData.left = new FormAttachment(estWeightText, 5);
		Label kg2label = new Label(group2, SWT.NONE);
		kg2label.setText("㎏");
		kg2label.setLayoutData(labelFormData);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(categoryCB, 5);
		labelFormData.left = new FormAttachment(kg2label, 45);
		SYMCLabel calWeightLabel = new SYMCLabel(group2, "Target Weight", labelFormData);
		textFormData = new FormData(196, 18);
		textFormData.top = new FormAttachment(categoryCB, 5);
		textFormData.left = new FormAttachment(calWeightLabel, 5);
		calWeightText = new SYMCText(group2, textFormData);
		labelFormData = new FormData();
		labelFormData.top = new FormAttachment(categoryCB, 5);
		labelFormData.left = new FormAttachment(calWeightText, 5);
		Label kg3label = new Label(group2, SWT.NONE);
		kg3label.setText("㎏");
		kg3label.setLayoutData(labelFormData);

		labelFormData = new FormData(100, SWT.DEFAULT);
		labelFormData.top = new FormAttachment(estWeightText, 5);
		labelFormData.left = new FormAttachment(0);
		SYMCLabel chgDescLabel = new SYMCLabel(group2, "Chg.Description", labelFormData);
		textFormData = new FormData(560, 54);
		textFormData.top = new FormAttachment(estWeightText, 5);
		textFormData.left = new FormAttachment(chgDescLabel, 5);
		chgDescText = new SYMCText(group2, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, textFormData);

		//        labelFormData = new FormData(100, SWT.DEFAULT);
		//        labelFormData.top = new FormAttachment(chgDescText, 5);
		//        labelFormData.left = new FormAttachment(0);
		//        SYMCLabel contentsLabel = new SYMCLabel(group2, "Contents", labelFormData);
		//        textFormData = new FormData(560, 54);
		//        textFormData.top = new FormAttachment(chgDescText, 5);
		//        textFormData.left = new FormAttachment(contentsLabel, 5);
		//        contentsText = new SYMCText(group2, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL, textFormData);
		// /////////////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////////////////////Eng. Info End/////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////////////

		this.pack();

	}

	/**
	 * Control 세팅 및 리스너 추가
	 * 
	 */
	private void setControlData() {

		estWeightText.setInputType(SYMCText.DOUBLE);
		calWeightText.setInputType(SYMCText.DOUBLE);

		unitCB.setMandatory(true);
		sysCodeCB.setMandatory(true);
		colorIDCB.setMandatory(true);
		estWeightText.setMandatory(true);
		estWeightText.redraw();

		//            partOrignCB.setEnabled(false);
		//            partOrignCB.setSelectedString("T");

		//            partNoText.setEnabled(false);
		partNameText.setEnabled(false);
		koreanNameText.setEnabled(false);

		partNoText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		partNameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		koreanNameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));

		try {

			//                String strNewID = SYMTcUtil.getNewID("T", 10);
			//                partNoText.setText(strNewID);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	public boolean isPageComplete() {
		return true;
	}

	/**
	 * Revision 검색시 사용되는 Selection Adapter
	 */
	//20230906 cf-4357 seho 사용하지 않는 class를 주석 처리함.
	// 아래 SearchItemRevDialog 를 사용하는 부분이 있는데.. 개선 요구사항에 의해 프로그램 변경이 되었음
//	class RevSelectionAdapter extends SelectionAdapter {
//		/** 검색된 Item ID값이 Setting될 Field */
//		SYMCText targetText;
//		/** 검색할 Revision Type */
//		String strItemRevType;
//
//		/**
//		 * Selection Adapter 생성자
//		 * 
//		 * @param targetText
//		 *            : 검색된 Item ID값이 Setting될 Field
//		 * @param strItemRevType
//		 *            : 검색할 Revision Type
//		 */
//		RevSelectionAdapter(SYMCText targetText, String strItemRevType) {
//			this.targetText = targetText;
//			this.strItemRevType = strItemRevType;
//		}
//
//		public void widgetSelected(SelectionEvent event) {
//			// 검색 Dialog
//			SearchItemRevDialog itemDialog = new SearchItemRevDialog(getShell(), SWT.SINGLE, strItemRevType);
//			// 선택된 Revision
//			TCComponentItemRevision[] selectedItems = (TCComponentItemRevision[]) itemDialog.open();
//
//			if (selectedItems != null) {
//				// Project는 ItemID만 저장
//				if (TypeConstant.S7_PREPROJECTREVISIONTYPE.equals(this.strItemRevType)) {
//					try {
//						targetText.setText(selectedItems[0].getProperty(PropertyConstant.ATTR_NAME_ITEMID));
//					} catch (TCException e) {
//						e.printStackTrace();
//					}
//				}
//				// ItemID/Revision Object 저장
//				else {
//					targetText.setText(selectedItems[0].toDisplayString());
//					targetText.setData(selectedItems[0]);
//				}
//
//			}
//		}
//	}

	/**
	 * TC ViewerTab에서 화면 이동시 수정된 항목이 존재하는지 Check
	 * 
	 * 최초 Loading시 속성값과 현재 속성값을 비교
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

	//    public void setPartNo(String strNewID) throws Exception {
	//        partNoText.setText(strNewID);
	//    }

}
