package com.kgm.commands.saveas;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import com.kgm.commands.partmaster.vehiclepart.VehiclePartMasterInfoPanel;
import com.kgm.common.FunctionField;
import com.kgm.common.SYMCAWTLabel;
import com.kgm.common.SYMCAWTTitledBorder;
import com.kgm.common.SYMCClass;
import com.kgm.common.SYMCComboBox;
import com.kgm.common.SYMCInterfaceInfoPanel;
import com.kgm.common.utils.ComponentService;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.iTextArea;

public class SYMCSaveAsInfoPanel extends JPanel implements SYMCInterfaceInfoPanel, ActionListener {

	private static final long serialVersionUID = 1L;
	private TCComponent targetComp;
	private Registry registry = Registry.getRegistry(this);

	/** Item ID textfield */
	private FunctionField itemIDTF = new FunctionField(11, true);
	/** Item Rev ID textfield */
	private FunctionField itemRevIDTF = new FunctionField(11, true);
	/** Item Name textfield */
	private FunctionField itemNameTF = new FunctionField(11, true);
	/** Asign buttion */
	private JButton asignBT = new JButton("할당");

	/** 저장 대상 하위 자식 아이템 포함/미포함 checkbox */
	private JCheckBox childrenItemAddCK = new JCheckBox("체크시 하위 대상 포함");
	/** 설명 textarea */
	private iTextArea descTA = new iTextArea(3, 10);

	/** unit of measure textfield */
	private FunctionField unitOfMeasureTF = new FunctionField(11, false);
	/** item Type textfield */
	private FunctionField itemTypeTF = new FunctionField(11, false);

	/** 기준 Item ID textfield */
	private FunctionField standardItemIDTF = new FunctionField(11, false);
	/** 기준 Item Rev ID combobox */
	private SYMCComboBox standardItemRevIDCB = new SYMCComboBox();
	/** 기준 Item Name textfield */
	private FunctionField standardItemNameTF = new FunctionField(11, false);

	private JDialog dialog;
	private JPanel viewPanel;
	private VehiclePartMasterInfoPanel infoPanel;

	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 18.
	 * @param dialog
	 */
	public SYMCSaveAsInfoPanel(JDialog dialog, TCComponent targetComp) {
		super(new VerticalLayout(5, 5, 5, 5, 5));
		this.dialog = dialog;
		this.targetComp = targetComp;

		initUI();

		/** Component 수정 불가 지정 */
		componentModifySetting();

		/** 대상의 타입과 측정 단위 및 기준 리비젼 값 등록 */
		targetRevValueSetting();
	}

	private void initUI() {
		setOpaque(false);

		JPanel itemPanel = new JPanel(new VerticalLayout(3, 3, 3, 3, 3));
		itemPanel.setOpaque(false);
		itemPanel.setBorder(new SYMCAWTTitledBorder("아이템 정보 입력"));

		JPanel descPanel = new JPanel(new PropertyLayout(5, 5, 5, 5, 5, 5));
		descPanel.setOpaque(false);
		descPanel.setBorder(new SYMCAWTTitledBorder(""));

		JPanel standardItemPanel = new JPanel(new VerticalLayout(3, 3, 3, 3, 3));
		standardItemPanel.setOpaque(false);
		standardItemPanel.setBorder(new SYMCAWTTitledBorder("기준 정보"));

		viewPanel = getViewInfoPanel();

		JPanel left_panel = new JPanel();
		left_panel.setOpaque(false);

		JPanel right_panel = new JPanel();
		right_panel.setOpaque(false);

		JPanel mainPanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		mainPanel.setOpaque(false);

		JPanel totalPanel = new JPanel(new GridLayout(1, 1, 3, 3));
		totalPanel.setOpaque(false);
		totalPanel.setBorder(new SYMCAWTTitledBorder(""));

		JPanel itemtopPanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		itemtopPanel.setOpaque(false);
		JPanel itemmainPanel = new JPanel(new PropertyLayout(5, 5, 5, 5, 5, 5));
		itemmainPanel.setOpaque(false);

		JPanel standardtopPanel = new JPanel(new VerticalLayout(5, 5, 5, 5, 5));
		standardtopPanel.setOpaque(false);
		JPanel standardmainPanel = new JPanel(new PropertyLayout(5, 5, 5, 5, 5, 5));
		standardmainPanel.setOpaque(false);

		// /////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////////// Item Panel ////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////
		itemtopPanel.add("unbound.bind", new SYMCAWTLabel("ID / 리비젼 - 이름"));

		itemmainPanel.add("1.1.left.center.resizable.preferred", itemIDTF);
		JLabel label1_1 = new JLabel("/");
		itemmainPanel.add("1.2.center.center.preferred.preferred", label1_1);
		itemmainPanel.add("1.3.left.center.resizable.preferred", itemRevIDTF);
		JLabel label2_1 = new JLabel("-");
		itemmainPanel.add("1.4.center.center.preferred.preferred", label2_1);
		itemmainPanel.add("1.5.left.center.resizable.preferred", itemNameTF);
		itemmainPanel.add("1.6.left.center.resizable.preferred", asignBT);

		asignBT.addActionListener(this);
		asignBT.setActionCommand("asignBT");
		// /////////////////////////////////////////////////////////////////////////////////
		// /////////////////////////// Item Panel END //////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////

		// /////////////////////////////////////////////////////////////////////////////////
		// ///////////////////////////// Desc Panel ////////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////
		descPanel.add("1.1.left.center.preferred.preferred", new SYMCAWTLabel("포함/미포함"));
		descPanel.add("1.2.right.center.resizable.preferred", childrenItemAddCK);
		childrenItemAddCK.setOpaque(false);

		descPanel.add("2.1.left.center.preferred.preferred", new SYMCAWTLabel("설명"));
		descPanel.add("2.2.right.center.resizable.preferred", new JScrollPane(descTA));

		descPanel.add("3.1.left.center.preferred.preferred", new SYMCAWTLabel("측정 단위"));
		descPanel.add("3.2.right.center.resizable.preferred", unitOfMeasureTF);

		descPanel.add("4.1.left.center.preferred.preferred", new SYMCAWTLabel("아이템 타입"));
		descPanel.add("4.2.right.center.resizable.preferred", itemTypeTF);
		// /////////////////////////////////////////////////////////////////////////////////
		// /////////////////////////// Desc Panel END //////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////

		// /////////////////////////////////////////////////////////////////////////////////
		// /////////////////////////// Standard Panel //////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////
		standardtopPanel.add("unbound.bind", new SYMCAWTLabel("ID / 리비젼 - 이름"));

		standardmainPanel.add("1.1.left.center.resizable.preferred", standardItemIDTF);
		JLabel label1_2 = new JLabel("/");
		standardmainPanel.add("1.2.center.center.preferred.preferred", label1_2);
		standardmainPanel.add("1.3.left.center.resizable.preferred", standardItemRevIDCB);
		JLabel label2_2 = new JLabel("-");
		standardmainPanel.add("1.4.center.center.preferred.preferred", label2_2);
		standardmainPanel.add("1.5.left.center.resizable.preferred", standardItemNameTF);
		// /////////////////////////////////////////////////////////////////////////////////
		// ////////////////////////// Standard Panel END ////////////////////////////////
		// /////////////////////////////////////////////////////////////////////////////////

		itemPanel.add("unbound.bind", itemtopPanel);
		itemPanel.add("unbound.bind", itemmainPanel);

		standardItemPanel.add("unbound.bind", standardtopPanel);
		standardItemPanel.add("unbound.bind", standardmainPanel);

		mainPanel.add("bound.bind", itemPanel);
		mainPanel.add("bound.bind", descPanel);
		mainPanel.add("bound.bind", standardItemPanel);

		totalPanel.add(mainPanel);

		if (targetComp.getType().equals(SYMCClass.S7_VEHPARTTYPE)
				|| targetComp.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE)) {
			totalPanel.add(viewPanel);
		} else {
			JScrollPane scrollPane = new JScrollPane(viewPanel);
			scrollPane.getViewport().setBackground(Color.WHITE);

			scrollPane.setPreferredSize(new Dimension(150, 280));
			/** 스크롤 속도 설정 */
			scrollPane.getVerticalScrollBar().setUnitIncrement(10);

			totalPanel.add(scrollPane);
		}

		add("unbound.bind", totalPanel);

		ComponentService.setLabelSize(itemmainPanel, 120, 22);
		ComponentService.setLabelSize(descPanel, 120, 22);
		ComponentService.setLabelSize(standardmainPanel, 120, 22);
		ComponentService.setComboboxSize(standardmainPanel, 120, 22);

		label1_1.setPreferredSize(new Dimension(20, 22));
		label1_2.setPreferredSize(new Dimension(20, 22));
		label2_1.setPreferredSize(new Dimension(20, 22));
		label2_2.setPreferredSize(new Dimension(20, 22));
	}

	/**
	 * 속성 View Panel 처리.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 20.
	 * @return
	 */
	private JPanel getViewInfoPanel() {
		if (targetComp.getType().equals(SYMCClass.S7_VEHPARTTYPE)
				|| targetComp.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE)) {
//			infoPanel = new PartMasterInfoPanel();
//			infoPanel.setPreferredSize(new Dimension(300, 330));
//
//			/** 속성값 등록 */
//			propertiesSetting();
//			return infoPanel;
			JPanel panel = new JPanel(new PropertyLayout(5, 5, 5, 5, 5, 5));
			panel.setOpaque(false);
			try {
				TCProperty[] tcProperty = targetComp.getAllTCProperties();

				int proSize = tcProperty.length;
				int count = 1;
				for (int i = 0; i < proSize; i++) {
					String value = tcProperty[i].getDisplayValue();
					String field = tcProperty[i].getName();
					String proName = tcProperty[i].getPropertyName();

					panel.add(count + ".1.left.center.preferred.preferred", new SYMCAWTLabel(field));
					FunctionField functionTF = new FunctionField(11, false);
					// functionTF.setEditable(false);
					// functionTF.setEnabled(false);
					functionTF.setText(value);
					panel.add(count + ".2.left.center.resizable.preferred", functionTF);

					System.out.println("name : " + field + "  >>  value : " + value + "  &&  proName : "
							+ proName);

					count++;
				}

				ComponentService.setLabelSize(panel, 140, 22);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return panel;
		} else {
			JPanel panel = new JPanel(new PropertyLayout(5, 5, 5, 5, 5, 5));
			panel.setOpaque(false);
			try {
				TCProperty[] tcProperty = targetComp.getAllTCProperties();

				int proSize = tcProperty.length;
				int count = 1;
				for (int i = 0; i < proSize; i++) {
					String value = tcProperty[i].getDisplayValue();
					String field = tcProperty[i].getName();
					String proName = tcProperty[i].getPropertyName();

					panel.add(count + ".1.left.center.preferred.preferred", new SYMCAWTLabel(field));
					FunctionField functionTF = new FunctionField(11, false);
					// functionTF.setEditable(false);
					// functionTF.setEnabled(false);
					functionTF.setText(value);
					panel.add(count + ".2.left.center.resizable.preferred", functionTF);

					System.out.println("name : " + field + "  >>  value : " + value + "  &&  proName : "
							+ proName);

					count++;
				}

				ComponentService.setLabelSize(panel, 140, 22);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return panel;
		}
	}

	/**
	 * 선택 대상 속성 값 등록.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 20.
	 */
	@SuppressWarnings("unused")
    private void propertiesSetting() {
		TCComponentItemRevision itemRev = null;
		try {
			if (targetComp instanceof TCComponentItem) {
				itemRev = ((TCComponentItem) targetComp).getLatestItemRevision();
			} else if (targetComp instanceof TCComponentItemRevision) {
				itemRev = (TCComponentItemRevision) targetComp;
			}

			TCProperty[] p = itemRev.getTCProperties(registry.getStringArray("VEHPART.Attribute"));

//			infoPanel.getProjectCodeTF().setText(p[0].getStringValue());
//			infoPanel.getPartOriginCB().setSelectedItem(p[1].getStringValue());
//			infoPanel.getPartNOTF().setText(p[2].getStringValue());
//			infoPanel.getPartNumberTF().setText(p[3].getStringValue());
//			infoPanel.getPartNameTF().setText(p[4].getStringValue());
//			infoPanel.getKoreanNameTF().setText(p[5].getStringValue());
//			infoPanel.getMainNameTF().setText(p[6].getStringValue());
//			infoPanel.getSubNameTF().setText(p[7].getStringValue());
//			infoPanel.getNameSpecTF().setText(p[8].getStringValue());
//			infoPanel.getLoc1CB().setSelectedItem(p[9].getStringValue());
//			infoPanel.getLoc2CB().setSelectedItem(p[10].getStringValue());
//			infoPanel.getLoc3CB().setSelectedItem(p[11].getStringValue());
//			infoPanel.getLoc4CB().setSelectedItem(p[12].getStringValue());
//			infoPanel.getLoc5CB().setSelectedItem(p[13].getStringValue());
//			infoPanel.getReferenceTF().setText(p[14].getStringValue());
//			infoPanel.getUnitCB().setSelectedItem(p[15].getStringValue());
//			infoPanel.getEcoNOTF().setText(p[16].getStringValue());
//			infoPanel.getColorCB().setSelectedItem(p[17].getStringValue());
//			infoPanel.getColorSectionIDTF().setText(p[18].getStringValue());
//			infoPanel.getCatCB().setSelectedItem(p[19].getStringValue());
//			infoPanel.getMaterialTF().setText(p[20].getStringValue());
//			infoPanel.getAlterMaterialTF().setText(p[21].getStringValue());
//			infoPanel.getMaterialThicknessTF().setText(String.valueOf(p[22].getDoubleValue()));
//			infoPanel.getAlterMaterialThicknessTF().setText(String.valueOf(p[23].getDoubleValue()));
//			infoPanel.getFinishTF().setText(p[24].getStringValue());
//			infoPanel.getDwgStatusCB().setSelectedItem(p[25].getStringValue());
//			infoPanel.getShowOnNOTF().setText(p[26].getStringValue());
//			// p[27].getStringValue(infoPanel.get);
//			// p[28].getStringValue(infoPanel);
//			infoPanel.getEstWeightTF().setText(String.valueOf(p[29].getDoubleValue()));
//			infoPanel.getCalWeightTF().setText(String.valueOf(p[30].getDoubleValue()));
//			infoPanel.getActWeightTF().setText(String.valueOf(p[31].getDoubleValue()));
//			infoPanel.getCalSurfaceTF().setText(String.valueOf(p[32].getDoubleValue()));
//			infoPanel.getBoundingBoxTF().setText(p[33].getStringValue());
//			infoPanel.getTestResultCB().setSelectedItem(p[34].getStringValue());
//			// p[35].getStringValue(infoPanel.get);
//			// p[36].getStringValue(infoPanel.);
//			// p[37].getStringValue(infoPanel.);
//			// p[38].getStringValue(infoPanel.);
//			// p[39].getStringValue(infoPanel.);
//			// p[40].getStringValue(infoPanel.);
//			infoPanel.getPartNumberVersionTF().setText(p[41].getStringValue());
//			infoPanel.getMaturityCB().setSelectedItem(p[42].getStringValue());
//			infoPanel.getLastModifiedDateDB().setDate(p[43].getDateValue());
//			infoPanel.getLastModifyingUserTF().setText(p[44].getStringValue());
//			infoPanel.getOrganizationTF().setText(p[45].getStringValue());
//			infoPanel.getOwnerTF().setText(p[46].getStringValue());
//			infoPanel.getDateCreatedDB().setDate(p[47].getDateValue());
//			infoPanel.getCreationUserTF().setText(p[48].getStringValue());
//			// p[49].getStringValue(infoPanel.get);
//			infoPanel.getPartStageCB().setSelectedItem(p[50].getStringValue());
//			infoPanel.getAsEndItemTF().setText(p[51].getStringValue());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 대상 리비젼 기준 값 셋팅.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 20.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
    private void targetRevValueSetting() {
		String uom_tag = "";
		String revID = "";
		String object_name = "";
		String item_id = "";
		String type = "";
		String[] revIDArr = null;
		TCComponentItem item = null;
		TCComponentItemRevision itemRev = null;
		TCComponent comp = null;
		ArrayList revIDArrayList = new ArrayList();
		try {
			type = targetComp.getType();
			if (targetComp instanceof TCComponentItem) {
				item = ((TCComponentItem) targetComp);
				uom_tag = targetComp.getProperty("uom_tag");

				itemRev = item.getLatestItemRevision();
				revID = itemRev.getProperty("item_revision_id");
				object_name = itemRev.getProperty("object_name");
				item_id = itemRev.getProperty("item_id");

				AIFComponentContext[] comps = item.getChildren();
				int compsSize = comps.length;
				for (int i = 0; i < compsSize; i++) {
					comp = (TCComponent) comps[i].getComponent();
					if (comp instanceof TCComponentItemRevision) {
						revIDArrayList.add(comp.getProperty("item_revision_id"));
					}
				}
				int revSize = revIDArrayList.size();
				revIDArr = new String[revSize];
				for (int j = 0; j < revSize; j++) {
					revIDArr[j] = (String) revIDArrayList.get(j);
				}
			} else if (targetComp instanceof TCComponentItemRevision) {
				itemRev = (TCComponentItemRevision) targetComp;
				item = itemRev.getItem();

				uom_tag = item.getProperty("uom_tag");

				revID = itemRev.getProperty("item_revision_id");
				object_name = itemRev.getProperty("object_name");
				item_id = itemRev.getProperty("item_id");
				revIDArr = new String[] { revID };
			} else {
				revIDArr = new String[] { "" };
			}

			standardItemIDTF.setText(item_id);

			DefaultComboBoxModel ComboBoxModel = new DefaultComboBoxModel(revIDArr);
			standardItemRevIDCB.setModel(ComboBoxModel);
			standardItemRevIDCB.setSelectedItem(revID);

			standardItemNameTF.setText(object_name);

			unitOfMeasureTF.setText(uom_tag);

			itemTypeTF.setText(type);

			/** Deep copy */
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Component 수정 불가 설정.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 18.
	 */
	private void componentModifySetting() {
		itemIDTF.setEditable(false);
		itemIDTF.setEnabled(false);

		itemRevIDTF.setEditable(false);
		itemRevIDTF.setEnabled(false);

		// 측정단위는 Item의 속성으로 존재.
		unitOfMeasureTF.setEditable(false);
		unitOfMeasureTF.setEnabled(false);

		itemTypeTF.setEditable(false);
		itemTypeTF.setEnabled(false);

		standardItemIDTF.setEditable(false);
		standardItemIDTF.setEnabled(false);

		standardItemNameTF.setEditable(false);
		standardItemNameTF.setEditable(false);
	}

	@Override
	public boolean validCheck() {
		if (itemIDTF.getText() == null || itemIDTF.getText().equals("")) {
			MessageBox.post(dialog, "다른 이름으로 저장되는 대상의 ID를 할당 하십시오.", "알림.", MessageBox.INFORMATION);
			return false;
		}
		if (itemRevIDTF.getText() == null || itemRevIDTF.getText().equals("")) {
			MessageBox.post(dialog, "다른 이름으로 저장되는 대상의 RevisionID를 할당 하십시오.", "알림.", MessageBox.INFORMATION);
			return false;
		}
		if (itemNameTF.getText() == null || itemNameTF.getText().equals("")) {
			MessageBox.post(dialog, "다른 이름으로 저장되는 대상의 이름을 입력 하십시오.", "알림.", MessageBox.INFORMATION);
			return false;
		}
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String act = e.getActionCommand();
		if (act.equals("asignBT")) {
			try {
				String revID = "";
				TCComponentItem item = null;
				TCComponentItemRevision itemRev = null;
				if (targetComp instanceof TCComponentItem) {
					item = (TCComponentItem) targetComp;
					String str = item.getProperty("displayable_revisions");
					String[] strs = str.split("[/]");
					String str1 = strs[1].toString();
					String[] strs1 = str1.split("[;]");
					revID = strs1[0].toString();
				} else if (targetComp instanceof TCComponentItemRevision) {
					itemRev = (TCComponentItemRevision) targetComp;
					item = itemRev.getItem();
					String str = item.getProperty("displayable_revisions");
					String[] strs = str.split("[/]");
					String str1 = strs[1].toString();
					String[] strs1 = str1.split("[;]");
					revID = strs1[0].toString();
				}

				String itemID = CustomUtil.getNextItemId("Item");
				itemIDTF.setText(itemID);

				itemRevIDTF.setText(revID);
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
	}

	public TCComponent getTargetComp() {
		return targetComp;
	}

	public FunctionField getItemIDTF() {
		return itemIDTF;
	}

	public FunctionField getItemRevIDTF() {
		return itemRevIDTF;
	}

	public FunctionField getItemNameTF() {
		return itemNameTF;
	}

	public iTextArea getDescTA() {
		return descTA;
	}

	public FunctionField getUnitOfMeasureTF() {
		return unitOfMeasureTF;
	}

	public FunctionField getItemTypeTF() {
		return itemTypeTF;
	}

	public FunctionField getStandardItemIDTF() {
		return standardItemIDTF;
	}

	public SYMCComboBox getStandardItemRevIDCB() {
		return standardItemRevIDCB;
	}

	public FunctionField getStandardItemNameTF() {
		return standardItemNameTF;
	}

	public JPanel getViewPanel() {
		return viewPanel;
	}

	public JCheckBox getChildrenItemAddCK() {
		return childrenItemAddCK;
	}

	public VehiclePartMasterInfoPanel getInfoPanel() {
		return infoPanel;
	}
}
