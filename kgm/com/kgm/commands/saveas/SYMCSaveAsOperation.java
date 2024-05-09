package com.kgm.commands.saveas;

import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import com.kgm.commands.partmaster.vehiclepart.VehiclePartMasterInfoPanel;
import com.kgm.common.SYMCClass;
import com.kgm.common.operation.SYMCAWTAbstractCreateOperation;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.Registry;

@SuppressWarnings({"rawtypes", "unused"})
public class SYMCSaveAsOperation extends SYMCAWTAbstractCreateOperation {

	private SYMCSaveAsDialog saveAsDialog;
	private Registry registry = Registry.getRegistry(this);
	/** BomLine를 담을 Vector */
    private Vector getBomVtr;
	private ArrayList newItemList;

	public SYMCSaveAsOperation(SYMCSaveAsDialog dialog) {
		super(dialog);
		this.saveAsDialog = dialog;
	}

	@Override
	public void createItem() throws Exception {
		TCComponent comp = saveAsDialog.getTarget();
		String type = "";
		if (comp instanceof TCComponentItem) {
			type = comp.getType();
		} else if (comp instanceof TCComponentItemRevision) {
			TCComponentItem item = ((TCComponentItemRevision) comp).getItem();
			type = item.getType();
		}
		newComp = CustomUtil.createItem(type,
				saveAsDialog.getInfoPanel().getItemIDTF().getValue().toString(), saveAsDialog.getInfoPanel()
						.getItemRevIDTF().getValue().toString(), saveAsDialog.getInfoPanel().getItemNameTF()
						.getValue().toString(), saveAsDialog.getInfoPanel().getDescTA().getText());
	}

	@Override
	public void endOperation() throws Exception {
		boolean flag = saveAsDialog.getInfoPanel().getChildrenItemAddCK().isSelected();
		System.out.println("flag : " + flag);
		if (flag) {
			TCComponent comp = saveAsDialog.getTarget();
			TCComponentItemRevision compRev = null;
			if (comp instanceof TCComponentItem) {
				compRev = ((TCComponentItem) comp).getLatestItemRevision();
			} else if (comp instanceof TCComponentItemRevision) {
				compRev = (TCComponentItemRevision) comp;
			}
			String type = compRev.getType();

			TCComponentItemRevision itemRev = CustomUtil.findItemRevision(type, saveAsDialog.getInfoPanel()
					.getStandardItemIDTF().getValue().toString(), saveAsDialog.getInfoPanel()
					.getStandardItemRevIDCB().getValue().toString());

			System.out.println("itemRev : " + itemRev);

			AIFComponentContext[] context = null;
			context = itemRev.getRelated("structure_revisions"); // 봄뷰 가져온다.
			getBomVtr = new Vector();
			newItemList = new ArrayList();

			getBomVtr = CustomUtil.getBomLine(context, getBomVtr); // 봄라인가져온다.
			newItemList = CustomUtil.getNewBomLine(newComp, newItemList, getBomVtr);

			int bomVtrSize = getBomVtr.size();
			TCComponentItem saveNewTopItem = null;
			int parentIndex = -1;

			for (int i = 0; i < bomVtrSize; i++) {
				// 대상 가져오기
				TCComponentBOMLine bomLine = ((TCComponentBOMLine) getBomVtr.get(i));

				TCComponentBOMLine parentLine = bomLine.parent();

				if (parentLine != null) {
					for (int z = 0; z < bomVtrSize; z++) {
						if (getBomVtr.get(z) == parentLine) {
							parentIndex = z;
							break;
						}
					}
					/** 새로 생성 된 BOM 구조 아이템 BOM 구조 만들기 위한 TopItem, 자신 획득 */
					saveNewTopItem = (TCComponentItem) newItemList.get(parentIndex);

					/** BOM 구조 실제 생성. */
					CustomUtil.bomViewItemRevisionCheck(saveNewTopItem.getLatestItemRevision(),
							(TCComponentItem) newItemList.get(i));
				}
			} // for end
		} // if end
	}

	@Override
	public void setProperties() throws Exception {
		if (newComp.getType().equals(SYMCClass.S7_VEHPARTTYPE)) {
			setS7_VehpartRevisionProperties(newComp);
		}
	}

	@Override
	public void startOperation() throws Exception {

	}

	public void setS7_VehpartRevisionProperties(TCComponentItem newComp) throws Exception {
		// VEHPART.Attribute=7U_PROJECT_NO,s7U_TYPE,s7S_PART_NUMBER,s7U_PART_NUMBER,s7PART_DESCRIPTION,s7U_KOREAN_NAME,s7U_MAIN_NAME,
		// s7U_SUB_NAME,s7U_REMARK,s7U_FRT_RR,s7U_INR_OTR,s7U_UPR_LWR,s7U_IO,s7U_LR,s7U_REFERENCE,s7U_UNIT,s7U_ECO,s7U_COLOR,s7U_COLOR_ID,
		// s7U_CATEGORY,s7U_MATERIAL,s7U_AL_MATERIAL,s7U_MAT_THICK,s7U_ALT_MAT_THICK,s7U_FINISH,s7U_DWG_STATUS,s7U_SHON,s7U_DWG_DATE,
		// s7U_REL_DATE,s7U_FORWGT,s7U_CALWGT,s7U_REALWGT,s7U_CALSURFACE,s7U_BOUNDING_BOX,s7U_TEST,s7U_DVP_ELEC,s7U_DVP_MATERIAL,
		// s7U_DVP_SAFETY,s7U_DVP_RIG,s7U_DVP_ENGINE,s7U_DVP_PT,s7C_PART_VERSION,s7C_MATURITY,s7C_LASTMOD,s7C_LASTMOD_USER,s7C_ORG_RESPONSIBLE,
		// s7C_RESPONSIBLE,s7C_CREATE,s7C_CREATE_USER,s7U_MODEL_TYPE,s7Stage,s7AS_End_ITEM
		
		String userName = session.getUserName();
		Date date = new Date();
		String groupName = (session.getUser().getGroups()[0]).getGroupName();
		
		TCComponentItemRevision itemRev = newComp.getLatestItemRevision();
		TCProperty[] p = itemRev.getTCProperties(registry.getStringArray("VEHPART.Attribute"));
		VehiclePartMasterInfoPanel partMasterInfoPanel = saveAsDialog.getInfoPanel().getInfoPanel();
//		p[0].setStringValue(partMasterInfoPanel.getProjectCodeTF().getText());
//		p[1].setStringValue(partMasterInfoPanel.getPartOriginCB().getSelectedItem().toString());
//		p[2].setStringValue(partMasterInfoPanel.getPartNOTF().getText());
//		p[3].setStringValue(partMasterInfoPanel.getPartNumberTF().getText());
//		p[4].setStringValue(partMasterInfoPanel.getPartNameTF().getText());
//		p[5].setStringValue(partMasterInfoPanel.getKoreanNameTF().getText());
//		p[6].setStringValue(partMasterInfoPanel.getMainNameTF().getText());
//		p[7].setStringValue(partMasterInfoPanel.getSubNameTF().getText());
//		p[8].setStringValue(partMasterInfoPanel.getNameSpecTF().getText());
//		p[9].setStringValue(partMasterInfoPanel.getLoc1CB().getSelectedItem().toString());
//		p[10].setStringValue(partMasterInfoPanel.getLoc2CB().getSelectedItem().toString());
//		p[11].setStringValue(partMasterInfoPanel.getLoc3CB().getSelectedItem().toString());
//		p[12].setStringValue(partMasterInfoPanel.getLoc4CB().getSelectedItem().toString());
//		p[13].setStringValue(partMasterInfoPanel.getLoc5CB().getSelectedItem().toString());
//		p[14].setStringValue(partMasterInfoPanel.getReferenceTF().getText());
//		p[15].setStringValue(partMasterInfoPanel.getUnitCB().getSelectedItem().toString());
//		p[16].setStringValue(partMasterInfoPanel.getEcoNOTF().getText());
//		p[17].setStringValue(partMasterInfoPanel.getColorCB().getSelectedItem().toString());
//		p[18].setStringValue(partMasterInfoPanel.getColorSectionIDTF().getText());
//		p[19].setStringValue(partMasterInfoPanel.getCatCB().getSelectedItem().toString());
//		p[20].setStringValue(partMasterInfoPanel.getMaterialTF().getText());
//		p[21].setStringValue(partMasterInfoPanel.getAlterMaterialTF().getText());
//		p[22].setDoubleValue(Double.valueOf(getDoubleValue(partMasterInfoPanel.getMaterialThicknessTF()
//				.getText())));
//		p[23].setDoubleValue(Double.valueOf(getDoubleValue(partMasterInfoPanel.getAlterMaterialThicknessTF()
//				.getText())));
//		p[24].setStringValue(partMasterInfoPanel.getFinishTF().getText());
//		p[25].setStringValue(partMasterInfoPanel.getDwgStatusCB().getSelectedItem().toString());
//		p[26].setStringValue(partMasterInfoPanel.getShowOnNOTF().getText());
//		// p[27].setStringValue(partMasterInfoPanel.get);
//		// p[28].setStringValue(partMasterInfoPanel);
//		p[29].setDoubleValue(Double.valueOf(getDoubleValue(partMasterInfoPanel.getEstWeightTF().getText())));
//		p[30].setDoubleValue(Double.valueOf(getDoubleValue(partMasterInfoPanel.getCalWeightTF().getText())));
//		p[31].setDoubleValue(Double.valueOf(getDoubleValue(partMasterInfoPanel.getActWeightTF().getText())));
//		p[32].setDoubleValue(Double.valueOf(getDoubleValue(partMasterInfoPanel.getCalSurfaceTF().getText())));
//		p[33].setStringValue(partMasterInfoPanel.getBoundingBoxTF().getText());
//		p[34].setStringValue(partMasterInfoPanel.getTestResultCB().getSelectedItem().toString());
//		// p[35].setStringValue(partMasterInfoPanel.get);
//		// p[36].setStringValue(partMasterInfoPanel.);
//		// p[37].setStringValue(partMasterInfoPanel.);
//		// p[38].setStringValue(partMasterInfoPanel.);
//		// p[39].setStringValue(partMasterInfoPanel.);
//		// p[40].setStringValue(partMasterInfoPanel.);
//		p[41].setStringValue(partMasterInfoPanel.getPartNumberVersionTF().getText());
//		p[42].setStringValue(partMasterInfoPanel.getMaturityCB().getSelectedItem().toString());
////		p[43].setDateValue(partMasterInfoPanel.getLastModifiedDateDB().getDate());
//		p[43].setDateValue(date);
////		p[44].setStringValue(partMasterInfoPanel.getLastModifyingUserTF().getText());
//		p[44].setStringValue(userName);
////		p[45].setStringValue(partMasterInfoPanel.getOrganizationTF().getText());
//		p[45].setStringValue(groupName);
////		p[46].setStringValue(partMasterInfoPanel.getOwnerTF().getText());
//		p[46].setStringValue(userName);
////		p[47].setDateValue(partMasterInfoPanel.getDateCreatedDB().getDate());
//		p[47].setDateValue(date);
////		p[48].setStringValue(partMasterInfoPanel.getCreationUserTF().getText());
//		p[48].setStringValue(userName);
//		// p[49].setStringValue(partMasterInfoPanel.get);
//		p[50].setStringValue(partMasterInfoPanel.getPartStageCB().getSelectedItem().toString());
//		p[51].setStringValue(partMasterInfoPanel.getAsEndItemTF().getText());

		itemRev.setTCProperties(p);
		itemRev.refresh();
	}

	/**
	 * Double 값으로 변경. "" >> 0
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 12. 21.
	 * @param text
	 * @return
	 */
	private String getDoubleValue(String text) {
		if (text.equals("")) {
			return "0";
		}
		return text;
	}
}
