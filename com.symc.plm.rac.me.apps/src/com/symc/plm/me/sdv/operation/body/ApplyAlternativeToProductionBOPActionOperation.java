/**
 *
 */
package com.symc.plm.me.sdv.operation.body;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.IStatus;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.cme.time.common.ActivityUtils;
import com.teamcenter.rac.kernel.SoaUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentAppGroupBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentCfgActivityLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEActivity;
import com.teamcenter.rac.kernel.TCComponentMEAppearancePathNode;
import com.teamcenter.rac.kernel.TCComponentMECfgLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.kernel.VariantCondition;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.psebase.common.AbstractViewableTreeTable;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 *
 */
public class ApplyAlternativeToProductionBOPActionOperation extends AbstractSDVActionOperation {
	private TCSession session;
	private Registry registry = Registry.getRegistry(ApplyAlternativeToProductionBOPActionOperation.class);
	private TCComponentBOPLine bop_shop_bopline = null;
	private TCComponentBOMLine plantLine = null;
	private final int ADD = 1;
    private final int DELETE = 0;

	/**
	 * @param actionId
	 * @param ownerId
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(int actionId, String ownerId, IDataSet dataset) {
		super(actionId, ownerId, dataset);
	}

	/**
	 * @param actionId
	 * @param operationId
	 * @param ownerId
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(int actionId, String operationId, String ownerId, IDataSet dataset) {
		super(actionId, operationId, ownerId, dataset);
	}

	/**
	 * @param actionId
	 * @param ownerId
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(String actionId, String ownerId, IDataSet dataset) {
		super(actionId, ownerId, dataset);
	}

	/**
	 * @param actionId
	 * @param ownerId
	 * @param parameters
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
		super(actionId, ownerId, parameters, dataset);
	}

	/**
	 * @param actionId
	 * @param ownerId
	 * @param parameters
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
		super(actionId, ownerId, parameters, dataset);
	}

	/**
	 * @param operationId
	 * @param actionId
	 * @param ownerId
	 * @param parameters
	 * @param dataset
	 */
	public ApplyAlternativeToProductionBOPActionOperation(String operationId, String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
		super(operationId, actionId, ownerId, parameters, dataset);
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
	 */
	@Override
	public void startOperation(String commandId) {
	}

	/* (non-Javadoc)
	 * @see org.sdv.core.common.ISDVOperation#endOperation()
	 */
	@Override
	public void endOperation() {
	}

	/* (non-Javadoc)
	 * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
	 */
	@Override
	public void executeOperation() throws Exception {
		session = CustomUtil.getTCSession();

		IDataSet dataSet = getDataSet();

		try
		{
			Object alt_bop_obj = dataSet.getValue("applyAlternativeToProductionBOPView", SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
			Object target_mproduct_obj = dataSet.getValue("applyAlternativeToProductionBOPView", SDVTypeConstant.EBOM_MPRODUCT);
			Object meco_obj = dataSet.getValue("applyAlternativeToProductionBOPView", SDVTypeConstant.MECO_ITEM);

	    	if (alt_bop_obj == null || (! (alt_bop_obj instanceof TCComponentBOPLine)))
	    		throw new Exception("[" + registry.getString("ApplyAltToProduct.AltShopRequired.MESSAGE", "Alternative BOP") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."));

	    	if (meco_obj == null || meco_obj.toString().trim().length() == 0)
	    		throw new Exception("[" + SYMTcUtil.getPropertyDisplayName(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV, SDVPropertyConstant.SHOP_REV_MECO_NO) + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."));

	    	if (target_mproduct_obj == null || (! (target_mproduct_obj instanceof TCComponentItemRevision)))
	    		throw new Exception("[" + registry.getString("ApplyAltToProduct.ProductShopRequired.MESSAGE", "M-Product") + "]" + registry.getString("RequiredField.MESSAGE", "is a required field."));

			// 양산적용할 Alternative BOP의 Shop을 가져온다.
			TCComponentItemRevision alt_shop_rev = null;


			String target_obj_type = ((TCComponentBOPLine) alt_bop_obj).getItem().getType();
			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
				alt_shop_rev = ((TCComponentBOPLine) alt_bop_obj).getItemRevision();
			else if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
				alt_shop_rev = ((TCComponentBOPLine) alt_bop_obj).parent().getItemRevision();
			else if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
				alt_shop_rev = ((TCComponentBOPLine) alt_bop_obj).parent().parent().getItemRevision();
			else
				throw new Exception("Not support item type.");

            String alt_prefix = alt_shop_rev.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
	    	TCComponent workarea_rev = alt_shop_rev.getRelatedComponent(SDVTypeConstant.MFG_WORKAREA);
			plantLine = CustomUtil.getBomline((TCComponentItemRevision) workarea_rev, session);

			// 양산적용할 Shop이 존재하는지 먼저 체크한다.
			String alt_shop_code = alt_shop_rev.getProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE);
			String product_code = "P".concat(((TCComponentItemRevision) target_mproduct_obj).getProperty(SDVPropertyConstant.ITEM_ITEM_ID).substring(1));
			String vehicle_code = null;
			String bop_shop_code = SDVPropertyConstant.ITEM_ID_PREFIX + "-"  + alt_shop_code + "-"  + product_code;//alt_shop_code.substring(alt_shop_prefix.length() + 1);
			TCComponentItem bop_shop_item = SDVBOPUtilities.FindItem(bop_shop_code, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM);
			boolean is_shop_created = false;

			if (target_mproduct_obj != null)
			{
				TCComponentItem sproductItem = SDVBOPUtilities.FindItem(product_code, SDVTypeConstant.EBOM_PRODUCT_ITEM);
				if (sproductItem == null)
					throw new NullPointerException(registry.getString("ProjectItemIsNull.MESSAGE", "Project Item not found of Product Item[%s].").replace("%s", product_code));

				TCComponentItem projectItem = SDVBOPUtilities.FindItem(sproductItem.getLatestItemRevision().getProperty(SDVPropertyConstant.S7_PROJECT_CODE), SDVTypeConstant.EBOM_PROJECT_ITEM);
				if (projectItem == null)
					throw new NullPointerException(registry.getString("ProjectItemIsNull.MESSAGE", "Project Item not found of Product Item[%s].").replace("%s", product_code));

				vehicle_code = projectItem.getLatestItemRevision().getProperty(SDVPropertyConstant.S7_VEHICLE_NO);
			}

			if (bop_shop_item == null && (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM)))
				throw new NullPointerException(registry.getString("ApplyProductBOPItemNotFound.MESSAGE", "Apply Shop Item can not find. contact to BOP Admin."));
			else if (bop_shop_item == null)
			{
				// 공정을 선택했을 때 에러로 표시한다.
				if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
					throw new Exception("Can not find BOP Shop Item.[" + bop_shop_code + "]");

				// Shop을 생성하게 해야 한다.
				bop_shop_item = SDVBOPUtilities.createItem(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, bop_shop_code, SDVPropertyConstant.ITEM_REV_ID_ROOT, alt_shop_rev.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), alt_shop_rev.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
//				bop_shop_item = alt_shop_rev.saveAsItem(bop_shop_code, SDVPropertyConstant.ITEM_REV_ID_ROOT);
				is_shop_created = true;
			}

			TCComponentItemRevision bop_shop_revision = bop_shop_item.getLatestItemRevision();
			TCComponentItemRevision shopPreRevision = null;
			if (! target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
			{
				if (CustomUtil.isReleased(bop_shop_revision))
				{
					// 샵과 라인은 워킹이 아니면 에러로 표시해야 하는게 아닌지 확인할 것.
					String new_rev_id = bop_shop_item.getNewRev();
					shopPreRevision = bop_shop_item.getLatestItemRevision();
					bop_shop_revision = bop_shop_item.revise(new_rev_id, bop_shop_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_shop_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
				}
			}

			TCComponent revMECO = null;
			if (! target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM) && ! target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
			{
				revMECO = bop_shop_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				if (revMECO == null)
				{
					// 만약에 리비전이 이미 개정되어 있으면 MECO정보를 어떻게 할지 결정해야 함.
					bop_shop_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, (TCComponent) meco_obj);

					if (shopPreRevision != null)
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_PROBLEM_ITEM, shopPreRevision);

					if (! ((TCComponentChangeItemRevision) meco_obj).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_shop_revision))
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_shop_revision);
				}

				// Shop 정보를 복사한다.
				setProperties(bop_shop_revision, alt_shop_rev, product_code, vehicle_code);
			}

			// 양산 Shop의 BOPLine을 만든다.
			bop_shop_bopline = CustomUtil.getBopline(bop_shop_revision, session);

			// 생성한 Shop에 옵션을 복사하자.
			if (is_shop_created)
				copyTopOptions((TCComponentBOPLine) alt_bop_obj, bop_shop_bopline);

			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM))
			{
				// Alternative BOP에서 선택한 항목이 Shop이면 하위 복사
				if (((TCComponentBOPLine) alt_bop_obj).hasChildren())
					makeChildBOPLine(alt_shop_code, (TCComponentBOPLine) alt_bop_obj, bop_shop_bopline, product_code, vehicle_code, (TCComponent) meco_obj, alt_prefix);
			}

			// Alternative BOP에서 선택한 항목이 Line일 때
			TCComponentBOPLine bop_line_bopline = null;
			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
			{
				// Alternative BOP에서 선택한 항목이 라인이면 양산 라인이 존재하는지 체크
				String line_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
				String bop_line_code = SDVPropertyConstant.ITEM_ID_PREFIX  + "-" + alt_shop_code + "-"  + line_id + "-"  + product_code + "-00";
				TCComponentItem bop_line_item = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, bop_line_code);

				// 존재하지 않으면 생성
				if (bop_line_item == null)
				{
					if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
						throw new Exception("Can not find Line Item.[" + bop_line_code + "]");

					bop_line_item = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().saveAsItem(bop_line_code, SDVPropertyConstant.ITEM_REV_ID_ROOT);
				}

				TCComponentItemRevision bop_line_revision = bop_line_item.getLatestItemRevision();
				TCComponent linePreRevision = null;
				if (CustomUtil.isReleased(bop_line_revision))
				{
					if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
						throw new Exception("BOP Line Revision is not modify status. Please first Line Revise.[" + bop_line_code + "]");

					String new_rev_id = bop_line_item.getNewRev();
					linePreRevision = bop_line_item.getLatestItemRevision();
					bop_line_revision = bop_line_item.revise(new_rev_id, bop_line_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_line_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
				}

				revMECO = bop_line_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
				if (revMECO == null)
				{
					bop_line_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, (TCComponent) meco_obj);

					if (linePreRevision != null)
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_PROBLEM_ITEM, linePreRevision);

					if (! ((TCComponentChangeItemRevision) meco_obj).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_line_revision))
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_line_revision);
				}

				// 라인의 정보 복사
				setProperties(bop_line_revision, ((TCComponentBOPLine) alt_bop_obj).getItemRevision(), product_code, vehicle_code);

				bop_line_item.refresh();
				bop_shop_bopline.refresh();
				bop_shop_bopline.window().refresh();

				// 양산 BOPShop 하위에 존재하는지 체크해서 존재하지 않으면 달자.
				for(AIFComponentContext alt_child_line : bop_shop_bopline.getChildren())
				{
					if (alt_child_line.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine child_bopline = (TCComponentBOPLine) alt_child_line.getComponent();
					String child_id = child_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					if (child_id.equals(bop_line_code))
					{
						bop_line_bopline = child_bopline;
						break;
					}
				}

				// 양산 BOPShop 하위에 라인이 없으면 달자
				if (bop_line_bopline == null)
					bop_line_bopline = (TCComponentBOPLine) bop_shop_bopline.add(bop_line_item, null);

				if (bop_line_bopline != null)
					makeChildBOPLine(alt_shop_code, (TCComponentBOPLine) alt_bop_obj, bop_line_bopline, product_code, vehicle_code, (TCComponent) meco_obj, alt_prefix);
			}
			else
			{
				// 선택한 항목이 공정이면 BOPShop하위에서 BOPLine을 찾는다.
				bop_shop_bopline.refresh();
				bop_shop_bopline.window().refresh();

				if (bop_shop_bopline.hasChildren())
				{
					String line_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
					for(AIFComponentContext shop_child_line : bop_shop_bopline.getChildren())
					{
						if (shop_child_line.getComponent() instanceof TCComponentAppGroupBOPLine)
							continue;

						TCComponentBOPLine child_bopline = (TCComponentBOPLine) shop_child_line.getComponent();
						String child_id = child_bopline.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
						if (child_id.equals(line_id))
						{
							bop_line_bopline = child_bopline;
							break;
						}
					}
				}
			}

			// 공정을 선택해서 적용할 건데, 라인이 없으면 에러
			if (bop_line_bopline == null)
				throw new Exception("Apply BOPLine Item can not find. contact to BOP Admin.");

			if (target_obj_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
			{
				// Alternative BOP에서 선택한 항목이 공정이면 양산 공정이 존재하는지 체크
				String line_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
				String station_id = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().getProperty(SDVPropertyConstant.STATION_REV_CODE);
				String bop_station_code = SDVPropertyConstant.ITEM_ID_PREFIX  + "-" + alt_shop_code + "-"  + line_id + "-" + station_id + "-" + product_code + "-00";
				TCComponentItem bop_station_item = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_LINE_ITEM, bop_station_code);
				TCComponentBOPLine bop_station_bopline = null;

				// 존재하지 않으면 생성
				if (bop_station_item == null)
				{
					bop_station_item = ((TCComponentBOPLine) alt_bop_obj).getItemRevision().saveAsItem(bop_station_code, SDVPropertyConstant.ITEM_REV_ID_ROOT);
				}

				TCComponentItemRevision bop_station_revision = bop_station_item.getLatestItemRevision();
				TCComponent stationPreRevision = null;
				if (CustomUtil.isReleased(bop_station_revision))
				{
					String new_rev_id = bop_station_item.getNewRev();
					stationPreRevision = bop_station_item.getLatestItemRevision();
					bop_station_revision = bop_station_item.revise(new_rev_id, bop_station_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_station_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
				}

				// 공정의 MECO를 설정
//				revMECO = bop_station_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
//				if (revMECO == null)
				{
					bop_station_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, (TCComponent) meco_obj);

					if (stationPreRevision != null)
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_PROBLEM_ITEM, stationPreRevision);

					if (! ((TCComponentChangeItemRevision) meco_obj).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_station_revision))
						((TCComponentChangeItemRevision) meco_obj).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_station_revision);
				}

				// 공정의 정보 복사
				setProperties(bop_station_revision, ((TCComponentBOPLine) alt_bop_obj).getItemRevision(), product_code, vehicle_code);

				bop_station_item.refresh();
				bop_line_bopline.refresh();
				bop_line_bopline.window().refresh();

				// 라인 하위에 존재하는지 체크해서 존재하지 않으면 달자.
				AIFComponentContext[] alt_child_lines = bop_line_bopline.getChildren();
				for(AIFComponentContext alt_child_line : alt_child_lines)
				{
					if (alt_child_line.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine child_bopline = (TCComponentBOPLine) alt_child_line.getComponent();
					String child_id = child_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					if (child_id.equals(bop_station_code))
					{
						bop_station_bopline = child_bopline;
						break;
					}
				}

				// 라인 하위에 공정이 없으면 달자
				if (bop_station_bopline == null)
					bop_station_bopline = (TCComponentBOPLine) bop_shop_bopline.add(bop_station_item, null);

				if (bop_station_bopline != null)
					makeChildBOPLine(alt_shop_code, (TCComponentBOPLine) alt_bop_obj, bop_station_bopline, product_code, vehicle_code, (TCComponent) meco_obj, alt_prefix);
			}

			updatePertInfo((TCComponentBOPLine) alt_bop_obj, bop_shop_bopline);

			bop_shop_bopline.window().save();
			bop_shop_bopline.window().refresh();
//			bop_shop_bopline.window().close();


			final TCComponentItemRevision openRevision = bop_shop_revision;
			AbstractAIFOperation openOperation = new AbstractAIFOperation() {

				@Override
				public void executeOperation() throws Exception {
                    try {
                    	MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                    	AbstractViewableTreeTable[] openTreeTables = mfgApp.getViewableTreeTables();
                    	boolean isOpened = false;
                    	for (AbstractViewableTreeTable openTreeTable : openTreeTables)
                    	{
                    		if (openTreeTable.getBOMRoot().getItem().equals(openRevision.getItem()))
                    		{
                    			isOpened = true;
                    		}
                    	}

                    	if (! isOpened)
                    		mfgApp.open(openRevision.getItem());
                    	storeOperationResult(IStatus.OK);
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.storeOperationResult(IStatus.ERROR);
                        return;
                    }
				}
			};

			session.queueOperation(openOperation);
		}
		catch (Exception ex)
		{
			setErrorMessage(ex.getMessage());
			setExecuteError(ex);
			throw ex;
		}
		finally
		{
			if (bop_shop_bopline != null)
				bop_shop_bopline.window().close();
			if (plantLine != null)
				plantLine.window().close();
		}
	}


    /**
	 * 옵션을 복사하는 함수
	 * @param option_from_bopline
	 * @param option_copyto_bopline
	 * @throws Exception
	 */
	private void copyTopOptions(TCComponentBOPLine option_from_bopline, TCComponentBOPLine option_copyto_bopline) throws Exception
	{
		try
		{
			TCVariantService variantService = session.getVariantService();
			ModularOption[] srcOptions = SDVBOPUtilities.getModularOptions(option_from_bopline);
			ModularOption[] copyToOptions = SDVBOPUtilities.getModularOptions(option_copyto_bopline);

			// 복사하기 전에 모든 옵션들을 없애자.
			if (copyToOptions != null && copyToOptions.length > 0)
			{
				// Option 조건 부터 먼저 삭제
				variantService.setLineMvl(option_copyto_bopline, "");
				option_copyto_bopline.save();
				// 각 옵션 삭제
				for (ModularOption copyToOption : copyToOptions)
				{
					variantService.lineDeleteOption(option_copyto_bopline, copyToOption.optionId);
					option_copyto_bopline.save();
				}
			}

			// 옵션들을 등록
//			String[] corpIds = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_item_ids");
			String[] corpIds = session.getPreferenceService().getStringValuesAtLocation("PSM_global_option_item_ids", TCPreferenceLocation.OVERLAY_LOCATION);
			
			for (ModularOption srcOption : srcOptions) {
                HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
                OVEOption oveOption = CustomMVPanel.getOveOption(option_from_bopline, options, srcOption);
                // 저장될 옵션 값
                String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

                // (옵션 추가) 옵션을 대상 BOMLINE에 추가함
                variantService.lineDefineOption(option_copyto_bopline, optionValue);
                option_copyto_bopline.save();
            }

			// 옵션의 조건 설정
            String srcOptItemId = MVLLexer.mvlQuoteId(option_from_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
            String targetOptItemId = MVLLexer.mvlQuoteId(option_copyto_bopline.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);

            // 원본 옵션 유효성검사 조건을 가져옴
            String lineMvl = variantService.askLineMvl(option_from_bopline).replace(srcOptItemId, targetOptItemId);

            // 대상 BOMLINE에 옵션 유효성검사 조건을 생성함
            variantService.setLineMvl(option_copyto_bopline, lineMvl);

            option_copyto_bopline.save();
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * 하위 모든 자식들을 BOMLine에서 제거한다.
	 * @param alt_shop_bopline
	 */
	private void cutAllChildren(TCComponentBOPLine bopLine) throws Exception {
		try
		{
			if (bopLine == null)
				return;

			AIFComponentContext[] child_boplines = bopLine.getChildren();
			ArrayList<TCComponentBOMLine> to_delete_lines = new ArrayList<TCComponentBOMLine>();

			for (AIFComponentContext child_context : child_boplines)
			{
				if (! (child_context.getComponent() instanceof TCComponentAppGroupBOPLine))
					to_delete_lines.add((TCComponentBOMLine) child_context.getComponent());
			}

			if (to_delete_lines.size() > 0)
			{
				SDVBOPUtilities.disconnectObjects(bopLine, to_delete_lines);
			}

			bopLine.save();
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * 선택한 Alternative BOP 하위 아이템을 양산 BOP로 복사하고 정보를 설정하는 함수
	 *
	 * @param altBopLine
	 * @param targetBopLine
	 * @param altPrefix
	 * @param withEndItem
	 * @throws Exception
	 */
	private void makeChildBOPLine(String shop_code, TCComponentBOPLine altBopLine, TCComponentBOPLine targetBopLine, String product_code, String vehicle_code, TCComponent mecoComponent, String alt_prefix) throws Exception {
		try
		{
			AIFComponentContext[] alt_child_boplines = altBopLine.getChildren();

			// 양산 BOP 하위에 자식이 존재하면 모두 잘라내고 새로 붙여주자.
			cutAllChildren(targetBopLine);

			// Alternative BOP 하위 자식들의 양산 BOP 항목이 존재하는지 체크
			if (alt_child_boplines != null && alt_child_boplines.length > 0)
			{
				for (AIFComponentContext alt_child_context : alt_child_boplines)
				{
					if (alt_child_context.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine added_new_bopline = null;
					TCComponentBOPLine alt_child_bopline = (TCComponentBOPLine) alt_child_context.getComponent();

					String alt_child_type = alt_child_bopline.getItem().getType();
					String alt_occ_type = alt_child_bopline.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE);
					TCComponentItemRevision alt_bop_revision = alt_child_bopline.getItemRevision();
					alt_bop_revision.refresh();

					// BOP 아이템이면 아이템 자체를 복사해야 한다. 있으면 복사하지 않고 속성만 수정
					if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) ||
						alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM))
					{
						String bop_child_id = null;
//						String alt_prefix = alt_bop_revision.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
						String alt_item_id = alt_bop_revision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
						String line_code = alt_bop_revision.getProperty(SDVPropertyConstant.LINE_REV_CODE);
						if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
							bop_child_id = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shop_code + "-" + line_code + "-" + product_code + "-00";
						else if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
							bop_child_id = SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shop_code + "-" + line_code + "-" + alt_bop_revision.getProperty(SDVPropertyConstant.STATION_STATION_CODE) + "-" + product_code + "-00";
						else
							bop_child_id = alt_item_id.replace(alt_prefix + "-", "");
							//bop_child_id = vehicle_code + "-" + shop_code + "-" + alt_child_bopline.getItemRevision().getProperty(SDVPropertyConstant.OPERATION_REV_OPERATION_CODE) + "-" + "00";

						TCComponentItem bop_child_item = CustomUtil.findItem(alt_child_type, bop_child_id);
						TCComponentItemRevision bop_child_revision = null;

						// 임시 BOP가 존재하지 않으면 생성한다.
						if (bop_child_item == null)
						{
//							alt_child_item = CustomUtil.createItem(alt_child_type, alt_child_id, SDVPropertyConstant.ITEM_REV_ID_ROOT, target_child_bopline.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), target_child_bopline.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
							bop_child_item = alt_bop_revision.saveAsItem(bop_child_id, SDVPropertyConstant.ITEM_REV_ID_ROOT);
						}

						bop_child_revision = bop_child_item.getLatestItemRevision();
						TCComponent preItemRevision = null;
						if (CustomUtil.isReleased(bop_child_revision))
						{
							String new_rev_id = bop_child_item.getNewRev();
							preItemRevision = bop_child_item.getLatestItemRevision();
							bop_child_revision = bop_child_item.getLatestItemRevision().saveAs(new_rev_id);
//							bop_child_revision = bop_child_item.revise(new_rev_id, bop_child_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), bop_child_revision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));

						}

						// 개정을 하건 생성을 하건 MECO에 연결한다.
//						TCComponent revMECO = bop_child_revision.getReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO);
//						if (revMECO == null)
						{
							bop_child_revision.setReferenceProperty(SDVPropertyConstant.ITEM_REV_MECO_NO, mecoComponent);

							if (preItemRevision != null)
								((TCComponentChangeItemRevision) mecoComponent).add(SDVTypeConstant.MECO_PROBLEM_ITEM, preItemRevision);

							if (! ((TCComponentChangeItemRevision) mecoComponent).isRelationSet(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_child_revision))
								((TCComponentChangeItemRevision) mecoComponent).add(SDVTypeConstant.MECO_SOLUTION_ITEM, bop_child_revision);
						}

						// 아이템의 속성을 복사하자.
						setProperties(bop_child_revision, alt_bop_revision, product_code, vehicle_code);

						// 여기는 하위 데이타셋을 모두 제거하는 부분
						AIFComponentContext []rev_under_items = bop_child_revision.getChildren(new String[]{SDVTypeConstant.PROCESS_SHEET_KO_RELATION, SDVTypeConstant.PROCESS_SHEET_EN_RELATION, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION});
						for (AIFComponentContext rev_under_item : rev_under_items)
						{
							if (rev_under_item.getComponent() instanceof TCComponentDataset)
							{
								try
								{
									((TCComponentDataset) rev_under_item.getComponent()).delete();
								}
								catch (TCException ex)
								{
									bop_child_revision.cutOperation(rev_under_item.getContext().toString(), new TCComponent[]{(TCComponent) rev_under_item.getComponent()});
									try
									{
										((TCComponentDataset) rev_under_item.getComponent()).delete();
									}
									catch (TCException ex2)
									{
										ex2.printStackTrace();
									}
								}
							}
						}

						// 국문작업표준서 템플릿 연결
						if (bop_child_item.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
							bop_child_item.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
							bop_child_item.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM))
						{
							bop_child_revision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, SDVBOPUtilities.getTemplateDataset(SDVTypeConstant.PROCESS_SHEET_TEMPLATE_PREF_NAME, null));
						}

						bop_child_item.refresh();
						if (bop_child_item != null)
						{
							for (AIFComponentContext childContext : targetBopLine.getChildren())
							{
								if (childContext.getComponent() instanceof TCComponentAppGroupBOPLine)
									continue;

								if (bop_child_id.equals(childContext.getComponent().getProperty(SDVPropertyConstant.BL_ITEM_ID)))
								{
									added_new_bopline = (TCComponentBOPLine) childContext.getComponent();
									break;
								}
							}
							if (added_new_bopline == null)
							{
								ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
								addToChild.add(bop_child_item);
								TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, null);
								if (addedChild == null || addedChild.length == 0)
									throw new Exception("Can not add to BOP Line.");
								added_new_bopline = (TCComponentBOPLine) addedChild[0];
								added_new_bopline.save();
								targetBopLine.save();
//								added_new_bopline = (TCComponentBOPLine) targetBopLine.add(bop_child_item, null);
							}
						}

						if (added_new_bopline != null)
						{
							setBOPLineProperties(alt_child_bopline, added_new_bopline);
						}

						// 공정 및 라인의 하위는 우선 전부 제거하고 다시 붙인다.
						if ((alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
							 alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
							 alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM) ||
							 alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM)) && added_new_bopline != null)
						{
							// EndItem 을 복사하지 않을 경우 공법의 하위를 모두 제거한다.
							cutAllChildren(added_new_bopline);

//long startTime = System.currentTimeMillis();
							// EndItem/용접점/Plant 을 복사할 때는 PathNode 연결도 같이 연결하여야 한다.
							copyOperationChild(alt_child_bopline, added_new_bopline);
//System.out.println("copyOperationChild time =>" + (System.currentTimeMillis() - startTime));
						}

						if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM) ||
							alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM) ||
							alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM))
						{
							// 공법들의 Activity 복사는 어떻게 되나?
							copyMEActivitiesOfOperation(alt_child_bopline, added_new_bopline);
						}

						// 라인이나 공정은 다시 하위를 복사하도록 재귀호출 한다.
						if (alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM) || alt_child_type.equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
							makeChildBOPLine(shop_code, alt_child_bopline, added_new_bopline, product_code, vehicle_code, mecoComponent, alt_prefix);
					}
					else if (alt_occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE))
					{
						// 타입이 공정이고, 공정하위의 Plant의 MEResource만 복제한다.
						TCComponentMEAppearancePathNode[] linkedPaths = alt_child_bopline.askLinkedAppearances(false);

						if (linkedPaths != null && linkedPaths.length > 0)
						{
							TCComponentBOMLine plantBOMLine = plantLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantLine);
							TCComponentBOMLine productPlantBOMLine = null;
							boolean is_alt_plant = false;

							// MEResource는 상위가 Alternative인지 체크하고 그 상위의 상위에서 상위의 Product Plant라인을 검색하자.
							if (plantBOMLine.parent().getItemRevision().isValidPropertyName(SDVPropertyConstant.PLANT_REV_IS_ALTBOP))
								is_alt_plant = plantBOMLine.parent().getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);
							if (is_alt_plant)
							{
								// 상위가 Alternative가 아니면 그 상위에서 Alternative를 찾는다.
								productPlantBOMLine = getProductBOPLineInParent(plantBOMLine.parent().parent(), plantBOMLine.parent(), true);
								if (productPlantBOMLine != null)
								{
									// 상위의 Alternative Plant 하위 자기 Plant라인을 찾는다.
									productPlantBOMLine = getProductBOPLineInParent(productPlantBOMLine, plantBOMLine, false);
								}
								else
								{
									throw new NullPointerException("Can not find Product Plant Item of [" + plantBOMLine.parent().getProperty(SDVPropertyConstant.BL_ITEM_ID) + "]");
								}

								ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
								addToChild.add(productPlantBOMLine);
								TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, alt_occ_type);
								if (addedChild == null || addedChild.length == 0)
									throw new Exception("Can not add to BOP Line.");
								added_new_bopline = (TCComponentBOPLine) addedChild[0];
								added_new_bopline.save();
								altBopLine.save();
//								added_new_bopline = (TCComponentBOPLine) targetBopLine.assignAsChild(productPlantBOMLine, alt_occ_type);

								setBOPLineProperties(alt_child_bopline, added_new_bopline);
							}
							else
							{
								ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
								addToChild.add(alt_child_bopline.getItem());
								TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, alt_occ_type);
								if (addedChild == null || addedChild.length == 0)
									throw new Exception("Can not add to BOP Line.");
								added_new_bopline = (TCComponentBOPLine) addedChild[0];
								added_new_bopline.save();
								altBopLine.save();
//								added_new_bopline = (TCComponentBOPLine) targetBopLine.add(alt_child_bopline.getItem(), alt_occ_type);

								added_new_bopline.linkToAppearance(linkedPaths[0], false);

								setBOPLineProperties(alt_child_bopline, (TCComponentBOPLine) added_new_bopline);
							}
						}
						else
						{
							ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
							addToChild.add(alt_child_bopline.getItem());
							TCComponent []addedChild = SDVBOPUtilities.connectObject(targetBopLine, addToChild, alt_occ_type);
							if (addedChild == null || addedChild.length == 0)
								throw new Exception("Can not add to BOP Line.");
							added_new_bopline = (TCComponentBOPLine) addedChild[0];
							added_new_bopline.save();
							altBopLine.save();
//						added_new_bopline = (TCComponentBOPLine) targetBopLine.add(alt_child_bopline.getItem(), alt_occ_type);

							setBOPLineProperties(alt_child_bopline, (TCComponentBOPLine) added_new_bopline);
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * 상위 BOM Line에서 Alternative Plant에 해당하는 Product Plant를 찾아 리턴하는 함수
	 *
	 * @param parentLine
	 * @param targetLine
	 * @param altCheck
	 * @return
	 * @throws Exception
	 */
	private TCComponentBOMLine getProductBOPLineInParent(TCComponentBOMLine parentLine, TCComponentBOMLine targetLine, boolean altCheck) throws Exception
	{
		try
		{
			String item_id = targetLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			if (altCheck)
			{
				String alt_prefix = targetLine.getItemRevision().getProperty(SDVPropertyConstant.PLANT_REV_ALT_PREFIX);
				boolean is_alt = targetLine.getItemRevision().getLogicalProperty(SDVPropertyConstant.PLANT_REV_IS_ALTBOP);

				if (is_alt)
				{
					for (AIFComponentContext childLine : parentLine.getChildren())
					{
						if (childLine.getComponent() instanceof TCComponentAppGroupBOPLine)
							continue;

						String child_id = ((TCComponentBOMLine) childLine.getComponent()).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

						if (item_id.replace(alt_prefix + "-", "").equals(child_id))
							return (TCComponentBOMLine) childLine.getComponent();
					}

					if (parentLine.parent() != null)
					{
						TCComponentBOMLine findLine = getProductBOPLineInParent(parentLine.parent(), parentLine, altCheck);
						if (findLine != null)
						{
//							item_id = parentLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
							for (AIFComponentContext findChildLine : findLine.getChildren())
							{
								if (findChildLine.getComponent() instanceof TCComponentAppGroupBOPLine)
									continue;

								String child_id = ((TCComponentBOMLine) findChildLine.getComponent()).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

								if (item_id.replace(alt_prefix + "-", "").equals(child_id))
									return (TCComponentBOMLine) findChildLine.getComponent();
							}
						}
					}
				}
			}
			else
			{
				for (AIFComponentContext childLine : parentLine.getChildren())
				{
					if (childLine.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					String child_id = ((TCComponentBOMLine) childLine.getComponent()).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

					if (item_id.equals(child_id))
						return (TCComponentBOMLine) childLine.getComponent();
				}
			}

			return null;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * 공법 하위 Activity 들을 복제하는 함수
	 *
	 * @param copyFromOpLine
	 * @param copyToOpLine
	 * @throws Exception
	 */
	private void copyMEActivitiesOfOperation(TCComponentBOMLine copyFromOpLine, TCComponentBOMLine copyToOpLine) throws Exception {
		try
		{
//long startTime = System.currentTimeMillis();
			String []timeProperties = registry.getStringArray("CopyActivityProperties.BODY");
			TCComponentMEActivity copyfrom_root_activity = (TCComponentMEActivity) copyFromOpLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
			TCComponent[] copyfrom_child_activities = ActivityUtils.getSortedActivityChildren(copyfrom_root_activity);

//			TCComponentMEActivity copyto_root_activity = (TCComponentMEActivity) copyToOpLine.getItemRevision().getReferenceProperty(SDVPropertyConstant.ACTIVITY_ROOT_ACTIVITY);
//			TCComponent[] copyto_child_activities = ActivityUtils.getSortedActivityChildren(copyto_root_activity);

			TCComponent activityRootLine = copyToOpLine.getReferenceProperty("bl_me_activity_lines");
			if (activityRootLine != null && activityRootLine instanceof TCComponentCfgActivityLine)
			{
				TCComponent[] childLines = ActivityUtils.getSortedActivityChildren((TCComponentCfgActivityLine) activityRootLine);
				for (TCComponent childActivityLine : childLines)
				{
					TCComponentMECfgLine parentLine = ((TCComponentCfgActivityLine) childActivityLine).parent();
					ActivityUtils.removeActivity((TCComponentCfgActivityLine) childActivityLine);
					parentLine.save();
				}
			}
			((TCComponentCfgActivityLine) activityRootLine).save();

//			for (TCComponent copyto_child_activity : copyto_child_activities)
//			{
//				// Alternative 공법의 모든 액티비티 일단 삭제
//				copyto_child_activity.delete();
//			}
//System.out.println("copyMEActivitiesOfOperation Delete time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

            HashMap<String, TCComponentBOPLine> toolBOMLineList = getAssignedToolBOMLine(copyToOpLine);
//System.out.println("copyMEActivitiesOfOperation getTool time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

            for (TCComponent copyfrom_child_activity : copyfrom_child_activities)
			{
				// 각각의 액티비티를 복제해야 한다. 모든 속성들과 함께.
				TCComponent[] copyto_activities = ActivityUtils.createActivitiesBelow(new TCComponent[] { activityRootLine }, copyfrom_child_activity.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
                TCComponentCfgActivityLine copyto_activity_line = (TCComponentCfgActivityLine) copyto_activities[0];
                TCComponentMEActivity copyto_activity = (TCComponentMEActivity) copyto_activity_line.getUnderlyingComponent();
//System.out.println("copyMEActivitiesOfOperation CreateActivity time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

				HashMap<String, String> propertyMap = new HashMap<String, String>();
				for (String property : timeProperties)
				{
					String []propValue = SoaUtil.marshallTCProperty(copyfrom_child_activity.getTCProperty(property));
					if (propValue != null && propValue.length > 0 && propValue[0].trim().length() > 0)
					{
						if (! propertyMap.containsKey(property))
							propertyMap.put(property, propValue[0]);
					}
				}
				if (propertyMap.size() > 0)
					copyto_activity.setProperties(propertyMap);
//                // Activity Time
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).setDoubleValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_UNIT_TIME).getDoubleValue());
//                // Category
//            	copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CATEGORY).getStringValue());
//                // Work Code (SYSTEM Code)
//                // 작업약어
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_SYSTEM_CODE).getStringValue());
//                // Time System Frequency
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).setDoubleValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_TIME_SYSTEM_FREQUENCY).getDoubleValue());
//                // KPC
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_POINT).getStringValue());
//                // KPC 관리기준
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).getStringValue());
//                // Process Type
////                alt_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).setStringValue(target_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_CONTROL_BASIS).getStringValue());
//                // English Name
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_ENG_NAME).getStringValue());
                // Workers -- (Array)
                String[] workerList = copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER).getStringValueArray();
                ArrayList<String> workerArray = new ArrayList<String>();
                for (String worker : workerList)
                	if (worker != null && worker.trim().length() > 0)
                		workerArray.add(worker.trim());
                if (workerArray.size() > 0)
                	copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORKER).setStringValueArray(workerArray.toArray(new String[0]));
                else
                	copyto_activity.setProperty(SDVPropertyConstant.ACTIVITY_WORKER, null);
//                // Overlay Type
//                copyto_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE).setStringValue(copyfrom_child_activity.getTCProperty(SDVPropertyConstant.ACTIVITY_WORK_OVERLAP_TYPE).getStringValue());
                // MECO
//                copyto_activity.setStringProperty(SDVPropertyConstant.ACTIVITY_MECO_NO, copyfrom_child_activity.getStringProperty(SDVPropertyConstant.ACTIVITY_MECO_NO));

                // Activity 공구자원 할당
                // 공법 하위 METool occtype에 해당하는 BOMLine을 모두 가져온다. 그리고, 할당된 공구 리스트의 ID와 공법 하위 공구를 비교하여 그 BOMLine을 Tool로 할당한다.
                if (toolBOMLineList != null)
                {
	                String[] tools = ((TCComponentMEActivity) copyfrom_child_activity).getReferenceToolList(copyFromOpLine);
	                ArrayList<TCComponentBOPLine> reference_tool_list = new ArrayList<TCComponentBOPLine>();
	                for (String tool : tools)
	                {
	                	if (toolBOMLineList != null && toolBOMLineList.containsKey(tool))
	                		reference_tool_list.add(toolBOMLineList.get(tool));
	                }
	                if (reference_tool_list.size() > 0)
	                	copyto_activity.addReferenceTools(copyToOpLine, reference_tool_list.toArray(new TCComponentBOPLine[0]));
                }
//System.out.println("copyMEActivitiesOfOperation setActivityProperty time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();

                copyto_activity.save();
                activityRootLine.save();
//System.out.println("copyMEActivitiesOfOperation save Activity time =>" + (System.currentTimeMillis() - startTime));
//startTime = System.currentTimeMillis();
			}
//System.out.println("copyMEActivitiesOfOperation setProperty =>" + (System.currentTimeMillis() - startTime));
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * 공법 하위 모든 Tool 리스트를 찾아 리턴하는 함수
	 *
	 * @param copyToOpLine
	 * @return
	 * @throws Exception
	 */
	private HashMap<String, TCComponentBOPLine> getAssignedToolBOMLine(TCComponentBOMLine copyToOpLine) throws Exception {

		try
		{
			HashMap<String, TCComponentBOPLine> childToolList = null;
			TCComponentBOMLine[] childs = SDVBOPUtilities.getUnpackChildrenBOMLine(copyToOpLine);
			for (TCComponentBOMLine operationUnderBOMLine : childs) {
				if (operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_TOOL) ||
					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE) ||
					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_SUBSIDIARY) ||
//					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_GROUP) ||
					operationUnderBOMLine.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA))
				{
					String itemId = operationUnderBOMLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
					if (childToolList == null)
						childToolList = new HashMap<String, TCComponentBOPLine>();
					if (! childToolList.containsKey(itemId))
						childToolList.put(itemId, (TCComponentBOPLine) operationUnderBOMLine);
				}
			}

			return childToolList;
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * 공법 Structure 하위 자식들을 복사하는 함수
	 *
	 * @param fromBopLine
	 * @param copyToBopLine
	 */
	private void copyOperationChild(TCComponentBOPLine fromBopLine, TCComponentBOPLine copyToBopLine) throws Exception {
		try
		{
			// 공법 하위 자식들을 모두 복제한다.
			// BOP Line 속성들을 복사한다.
			// 하위 자식들 중 MEConsumed는 원래의 PathNode를 맺어준다.
			// 하위 자식들 중 MEWorkArea는 원래(Alternative Plant가 있으면 그것으로 연결)의 Plant PathNode를 맺어준다.
			if (fromBopLine.hasChildren())
			{
				for (AIFComponentContext childContext : fromBopLine.getChildren())
				{
					if (childContext.getComponent() instanceof TCComponentAppGroupBOPLine)
						continue;

					TCComponentBOPLine child_line = (TCComponentBOPLine) childContext.getComponent();
					String occ_type = child_line.getStringProperty(SDVPropertyConstant.BL_OCC_TYPE);

					if (occ_type != null && (occ_type.equals(SDVTypeConstant.OCC_TYPE_MECONSUMED) || occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWELDPOINT) ||
											  occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA) || occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE)))
					{
						TCComponentMEAppearancePathNode[] linkedPaths = child_line.askLinkedAppearances(false);

//						if (linkedPaths == null || linkedPaths.length == 0)
//							throw new Exception("Can not find BOM Line information from Product BOM.");

						TCComponentBOMLine product_plant_bom_line = null;
						if (occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA) || occ_type.equals(SDVTypeConstant.BOP_PROCESS_OCCURRENCE_RESOURCE))
						{
							if (occ_type.equals(SDVTypeConstant.OCC_TYPE_MEWORKAREA))
							{
								// WorkArea는 Alternative가 존재하기 때문에 Alternative가 아닌 일반 WorkArea의 Pathnode 정보를 연결해 줘야 한다.
								TCComponentBOMLine plant_bom_line = (TCComponentBOMLine) child_line.getReferenceProperty("bl_me_refline");

								if (plant_bom_line == null)
									throw new Exception("Please load Plant BOM.");

								plant_bom_line = plantLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantLine);

								String plant_item_type = plant_bom_line.getItem().getType();
								// 일반공법 하위의 복사
								if (plant_item_type.equals(SDVTypeConstant.PLANT_OPAREA_ITEM) || plant_item_type.equals(SDVTypeConstant.PLANT_STATION_ITEM))
								{
//long startTime = System.currentTimeMillis();
									product_plant_bom_line = getProductBOPLineInParent(plant_bom_line.parent(), plant_bom_line, true);
//System.out.println("getProductBOPLineInParent time =>" + (System.currentTimeMillis() - startTime));

									if (product_plant_bom_line != null)
									{
										ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
										addToChild.add(product_plant_bom_line);
										TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.OCC_TYPE_MEWORKAREA);
										if (addedChild == null || addedChild.length == 0)
											throw new Exception("Can not add to BOP Line.");
										TCComponentBOPLine added_bop_line = (TCComponentBOPLine) addedChild[0];
										added_bop_line.save();
										copyToBopLine.save();
//										TCComponentBOPLine added_bop_line = (TCComponentBOPLine) copyToBopLine.assignAsChild(product_plant_bom_line, SDVTypeConstant.OCC_TYPE_MEWORKAREA);

										setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
									}
								}
								else
								{
									// 용접공법 하위는 Plant정보가 아닌 Plant하위 설비 정보가 오기 때문에 상위를 체크해서 처리해야 한다.
//long startTime = System.currentTimeMillis();
									product_plant_bom_line = getProductBOPLineInParent(plant_bom_line.parent().parent(), plant_bom_line.parent(), true);
//System.out.println("getProductBOPLineInParent time =>" + (System.currentTimeMillis() - startTime));

									if (product_plant_bom_line != null)
									{
										product_plant_bom_line = getProductBOPLineInParent(product_plant_bom_line, plant_bom_line, false);

										if (product_plant_bom_line != null)
										{
											ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
											addToChild.add(product_plant_bom_line);
											TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.OCC_TYPE_MEWORKAREA);
											if (addedChild == null || addedChild.length == 0)
												throw new Exception("Can not add to BOP Line.");
											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) addedChild[0];
											added_bop_line.save();
											copyToBopLine.save();
//											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) copyToBopLine.assignAsChild(product_plant_bom_line, SDVTypeConstant.OCC_TYPE_MEWORKAREA);

											setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
										}
									}
								}
							}
							else
							{
								// MEResource 는 Plant에서 온 것과 일반 설비로 구분된다.
								TCComponentBOMLine plant_bom_line = (TCComponentBOMLine) child_line.getReferenceProperty("bl_me_refline");

								if (plant_bom_line == null)
								{
									// 일반설비일 경우는 그냥 설정하자.
									ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
									addToChild.add(child_line.getItem());
									TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
									if (addedChild == null || addedChild.length == 0)
										throw new Exception("Can not add to BOP Line.");
									TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//									TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);

									setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);

									added_bop_line.save();
									child_line.save();
								}
								else
								{
									// Plant에서 온 MEResource는 상위를 체크해서 가져온다.
									plant_bom_line = plantLine.window().getBOMLineFromAppearancePathNode(linkedPaths[0], plantLine);

									// 상위 Plant BOM Line에서 Alternative 가 아닌 Product Plant BOM Line을 찾는다.
									product_plant_bom_line = getProductBOPLineInParent(plant_bom_line.parent().parent(), plant_bom_line.parent(), true);

									if (product_plant_bom_line != null)
									{
										// Product Plant BOM Line에서 해당 Resource를 찾아 붙여준다.
										product_plant_bom_line = getProductBOPLineInParent(product_plant_bom_line, plant_bom_line, false);

										if (product_plant_bom_line != null)
										{
											ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
											addToChild.add(product_plant_bom_line);
											TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, SDVTypeConstant.OCC_TYPE_MEWORKAREA);
											if (addedChild == null || addedChild.length == 0)
												throw new Exception("Can not add to BOP Line.");
											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) addedChild[0];
											added_bop_line.save();
											copyToBopLine.save();
//											TCComponentBOPLine added_bop_line = (TCComponentBOPLine) copyToBopLine.assignAsChild(product_plant_bom_line, SDVTypeConstant.OCC_TYPE_MEWORKAREA);

											setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
										}
									}
									else
									{
										ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
										addToChild.add(child_line.getItem());
										TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
										if (addedChild == null || addedChild.length == 0)
											throw new Exception("Can not add to BOP Line.");
										TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//										TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);
										setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);

										added_bop_line.linkToAppearance(linkedPaths[0], false);

										added_bop_line.save();
										copyToBopLine.save();
									}
								}
							}
						}
						else
						{
							ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
							addToChild.add(child_line.getItem());
							TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
							if (addedChild == null || addedChild.length == 0)
								throw new Exception("Can not add to BOP Line.");
							TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//							TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);
							setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);

							added_bop_line.linkToAppearance(linkedPaths[0], false);

							added_bop_line.save();
							copyToBopLine.save();
						}
					}
					else
					{
						// 일반 리소스들..
						ArrayList<InterfaceAIFComponent> addToChild = new ArrayList<InterfaceAIFComponent>();
						addToChild.add(child_line.getItem());
						TCComponent []addedChild = SDVBOPUtilities.connectObject(copyToBopLine, addToChild, occ_type);
						if (addedChild == null || addedChild.length == 0)
							throw new Exception("Can not add to BOP Line.");
						TCComponentBOMLine added_bop_line = (TCComponentBOPLine) addedChild[0];
//						TCComponentBOMLine added_bop_line = copyToBopLine.add(child_line.getItem(), occ_type);
						setBOPLineProperties(child_line, (TCComponentBOPLine) added_bop_line);
					}
				}
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * BOP Line의 정보를 설정하는 함수
	 *
	 * @param target_bopline
	 * @param alt_bopline
	 * @throws Exception
	 */
	private void setBOPLineProperties(TCComponentBOPLine from_bopline, TCComponentBOPLine setto_bopline) throws Exception
	{
		try
		{
//long startTime = System.currentTimeMillis();
			String []bopLineProperties = registry.getStringArray("CopyBOPLineProperties");

			setto_bopline.refresh();
//			setto_bopline.window().refresh();
			if (bopLineProperties == null)
			{
				System.out.println("ApplyAlternativeToProductionBOPActionOperation.setBOPLineProperties() = Copy Property is null.");
				return;
			}

			HashMap<String, String> lineProperties = new HashMap<String, String>();
			for (String bopLineProperty : bopLineProperties)
			{
				if ((bopLineProperty.equals("bl_variant_condition")) || (bopLineProperty.equals("bl_condition_tag")) || (bopLineProperty.equals("bl_formula")))
				{
				    try
				    {
				    	if (! from_bopline.getProperty(SDVPropertyConstant.BL_OCC_TYPE).equals(SDVTypeConstant.OCC_TYPE_MECONSUMED))
				    	{
					        TCComponent localTCComponent = from_bopline.getReferenceProperty("bl_condition_tag");
					        Object localObject1;
					        if (localTCComponent == null)
					        {
					            localObject1 = from_bopline.getProperty("bl_variant_condition");
					            Object setToObject = setto_bopline.getProperty("bl_variant_condition");
					            if (localObject1 != null && setToObject != null && ! setToObject.equals(localObject1))
					            	from_bopline.getSession().getVariantService().setLineMvlCondition(setto_bopline, (String)localObject1);
					        }
					        else
					        {
					        	TCComponent toTCComponent = setto_bopline.getReferenceProperty("bl_condition_tag");
					        	if (toTCComponent != null && ! localTCComponent.equals(toTCComponent))
					        	{
						            localObject1 = VariantCondition.create(localTCComponent, setto_bopline.window());
						            setto_bopline.setReferenceProperty("bl_condition_tag", ((VariantCondition) localObject1).toCondition());
					        	}
					        }
					        setto_bopline.save();
				    	}
				    }
				    catch (Exception ex)
				    {
				        throw ex;
				    }
				}
				else
				{
					if (from_bopline.isValidPropertyName(bopLineProperty) && setto_bopline.isValidPropertyName(bopLineProperty))
					{
						String []targetValue = SoaUtil.marshallTCProperty(from_bopline.getTCProperty(bopLineProperty));
						String []altValue = SoaUtil.marshallTCProperty(setto_bopline.getTCProperty(bopLineProperty));
						if (targetValue == null || altValue == null || targetValue.length == 0 || altValue.length == 0 || ! targetValue[0].equals(altValue[0]))
							if (! lineProperties.containsKey(bopLineProperty))
								lineProperties.put(bopLineProperty, (targetValue == null || targetValue.length == 0 ? null : targetValue[0]));
					}
				}
			}

			if (lineProperties.size() > 0)
				setto_bopline.setProperties(lineProperties);

			setto_bopline.save();
//System.out.println("setBOPLineProperty time =>" + (System.currentTimeMillis() - startTime));
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	/**
	 * BOP 항목들의 정보를 설정하는 함수
	 *
	 * @param setToRevision
	 * @param fromRevision
	 * @throws Exception
	 */
	private void setProperties(TCComponentItemRevision setToRevision, TCComponentItemRevision fromRevision, String product_code, String vehicle_code) throws Exception {
		try
		{
//long startTime = System.currentTimeMillis();
			String []itemProperties = null;
			String []revProperties = null;

			setToRevision.refresh();
			if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM_REV))
			{
				itemProperties = registry.getStringArray("CopyShopItemProperties.BODY");
				revProperties = registry.getStringArray("CopyShopRevisionProperties.BODY");
			}
			else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM_REV))
			{
				revProperties = registry.getStringArray("CopyLineRevisionProperties.BODY");
			}
			else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM_REV))
			{
//				revProperties = registry.getStringArray("CopyStationRevisionProperties.BODY");
                if (setToRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) && fromRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) &&
                    ! setToRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME).equals(fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME)))
                    setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

                if (fromRevision.isValidPropertyName(SDVPropertyConstant.STATION_ALT_PREFIX))
                    setToRevision.setProperty(SDVPropertyConstant.STATION_ALT_PREFIX, "");
                if (fromRevision.isValidPropertyName(SDVPropertyConstant.STATION_IS_ALTBOP))
                    setToRevision.setLogicalProperty(SDVPropertyConstant.STATION_IS_ALTBOP, false);

                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS) && fromRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS))
                {
                    // 기존 EXT_DECESSORS 가 정의 되어 있으면 리스트를 삭제한다
                    TCComponent[] bopDecessorsStations = setToRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
                    for (TCComponent bopDecessorsStation : bopDecessorsStations) {
                        updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, (TCComponentItemRevision)bopDecessorsStation, DELETE);
                    }
                    // 새로 EXT_DECESSORS 가 정의 한다
                    TCComponent[] decessorsStations = fromRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
                    if (decessorsStations != null && decessorsStations.length > 0)
                    {
                        for (TCComponent decessorsStation : decessorsStations) {
                            String altPrefix= fromRevision.getProperty(SDVPropertyConstant.STATION_ALT_PREFIX);
                            String extDecessorsID = decessorsStation.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                            extDecessorsID = extDecessorsID.replace(altPrefix + "-", "");
                            TCComponentItem extDecessorsItem = SDVBOPUtilities.FindItem(extDecessorsID, SDVTypeConstant.BOP_PROCESS_STATION_ITEM);
                            if (extDecessorsItem != null) {
                                TCComponentItemRevision extDecessorsItemrev = extDecessorsItem.getLatestItemRevision();
                                updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, extDecessorsItemrev, ADD);
                            }
                        }
                    }
                }
//                if (setToRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS) && fromRevision.isValidPropertyName(SDVPropertyConstant.ME_EXT_DECESSORS))
//                {
//                    TCComponent[] extDecessorsLines = fromRevision.getReferenceListProperty(SDVPropertyConstant.ME_EXT_DECESSORS);
//                    if (extDecessorsLines != null && extDecessorsLines.length > 0)
//                      {
//                          for (TCComponent extDecessorsLine : extDecessorsLines) {
//                              updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, (TCComponentItemRevision)extDecessorsLine, DELETE);
//                              String altPrefix= fromRevision.getProperty(SDVPropertyConstant.STATION_ALT_PREFIX);
//                              String extDecessorsID = extDecessorsLine.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
//                              extDecessorsID = extDecessorsID.replace(altPrefix + "-", "");
//                              TCComponentItem extDecessorsItem = SDVBOPUtilities.FindItem(extDecessorsID, SDVTypeConstant.BOP_PROCESS_LINE_ITEM);
//                              if (extDecessorsItem != null) {
//                                  TCComponentItemRevision extDecessorsItemrev = extDecessorsItem.getLatestItemRevision();
//                                  updateReferenceArrayProperty(setToRevision, SDVPropertyConstant.ME_EXT_DECESSORS, extDecessorsItemrev, ADD);
//                              }
//                          }
//                      }
//                }

                setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
            }
			else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV) ||
					  fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV) ||
					  fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV))
			{
				itemProperties = registry.getStringArray("CopyOperationItemProperties.BODY");
				revProperties = registry.getStringArray("CopyOperationRevisionProperties.BODY");
			}
			else if (fromRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV))
			{
				if (setToRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) && fromRevision.getItem().isValidPropertyName(SDVPropertyConstant.ITEM_OBJECT_NAME) &&
					! setToRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME).equals(fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME)))
					setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));

				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX))
					setToRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, "");
				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP))
					setToRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, false);

				if (setToRevision.isValidPropertyName(SDVPropertyConstant.WELDOP_REV_TARGET_OP) && fromRevision.isValidPropertyName(SDVPropertyConstant.WELDOP_REV_TARGET_OP))
				{
					TCComponent targetOpRevision = fromRevision.getReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP);
					if (targetOpRevision != null)
					{
						String altPrefix = targetOpRevision.getProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX);
						String altItemID = targetOpRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
						String item_id = altItemID.replace(altPrefix + "-", "");

						TCComponentItem targetOPItem = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_OPERATION_ITEM, item_id);
						if (targetOPItem == null)
							throw new Exception("WeldOperation's target Operation was not found[" + item_id + "].");

						setToRevision.setReferenceProperty(SDVPropertyConstant.WELDOP_REV_TARGET_OP, targetOPItem.getLatestItemRevision());
					}
				}

				setToRevision.setProperty(SDVPropertyConstant.ITEM_OBJECT_NAME, fromRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME));
			}

			if (itemProperties != null)
			{
//				setToRevision.getItem().setTCProperties(fromRevision.getItem().getTCProperties(itemProperties));
				HashMap<String, String> propertyMap = new HashMap<String, String>();
				for (String property : itemProperties)
				{
					if (setToRevision.getItem().isValidPropertyName(property) && fromRevision.getItem().isValidPropertyName(property))
					{
						String []propValue = SoaUtil.marshallTCProperty(fromRevision.getItem().getTCProperty(property));
						String []setToValue = SoaUtil.marshallTCProperty(setToRevision.getItem().getTCProperty(property));
						if (propValue == null || propValue.length == 0 || setToValue == null || setToValue.length == 0 || ! propValue[0].equals(setToValue[0]))
						{
							if (! propertyMap.containsKey(property))
								propertyMap.put(property, (propValue == null || propValue.length == 0 ? null : propValue[0]));
						}
					}
				}
				if (propertyMap.size() > 0)
					setToRevision.getItem().setProperties(propertyMap);
			}
			if (revProperties != null)
			{
				HashMap<String, String> setPropertyList = new HashMap<String, String>();
				for (String property : revProperties)
				{
					if (property.equals(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE))
					{
						if (setToRevision.isValidPropertyName(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE))
							setToRevision.setProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, product_code);
					}
					else if (property.equals(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE))
					{
						if (setToRevision.isValidPropertyName(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE))
							setToRevision.setProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, vehicle_code);
					}
					else
					{
						if (setToRevision.isValidPropertyName(property) && fromRevision.isValidPropertyName(property))
						{
							if (property.equals(SDVPropertyConstant.OPERATION_REV_INSTALL_DRW_NO))
							{
								ArrayList<String> dwgList = new ArrayList<String>();
								String[] dwgNos = fromRevision.getTCProperty(property).getStringArrayValue();
								for (String dwgNo : dwgNos)
								{
									if (dwgNo != null && dwgNo.trim().length() > 0)
										dwgList.add(dwgNo);
								}
								if (dwgList.size() > 0)
									setToRevision.getTCProperty(property).setStringValueArray(dwgList.toArray(new String[0]));
								else
									setToRevision.setProperty(property, null);
							}
							else
							{
								String[] propValue = SoaUtil.marshallTCProperty(fromRevision.getTCProperty(property));
								String[] setToValue = SoaUtil.marshallTCProperty(setToRevision.getTCProperty(property));
								if (propValue == null || propValue.length == 0 || setToValue == null || setToValue.length == 0 || ! propValue[0].equals(setToValue[0]))
								{
									if (! setPropertyList.containsKey(property))
										setPropertyList.put(property, (propValue == null || propValue.length == 0 ? null : propValue[0]));
								}
//								setToRevision.setTCProperty(fromRevision.getTCProperty(property));
							}
						}
					}
				}

				if (setPropertyList.size() > 0)
					setToRevision.setProperties(setPropertyList);

				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX))
					setToRevision.setProperty(SDVPropertyConstant.OPERATION_REV_ALT_PREFIX, "");
				if (fromRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP))
					setToRevision.setLogicalProperty(SDVPropertyConstant.OPERATION_REV_IS_ALTBOP, false);
			}
//System.out.println("setProperty time =>" + (System.currentTimeMillis() - startTime));
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}

	private void updateReferenceArrayProperty(TCComponentItemRevision decessorItemRev, String propertyName, TCComponentItemRevision sucessorItemRev, int mode) throws TCException {
        if(decessorItemRev != null){
            TCProperty property = decessorItemRev.getTCProperty(propertyName);
            if(property == null) return;
            //수정권한이 있는지 여부확인
            if(decessorItemRev.isModifiable(propertyName)){
                TCComponent [] values = property.getReferenceValueArray();
                if(values == null) values = new TCComponent[0];
                switch (mode) {
                    case ADD:       values = (TCComponent[]) ArrayUtils.add(values, sucessorItemRev);
                        break;
                    case DELETE :   values = (TCComponent[]) ArrayUtils.removeElement(values, sucessorItemRev);
                        break;
                }
                property.setReferenceValueArray(values);

                //Save를 하면 Lock이 발생하여 save()를 하지 않음
                //decessorItemRev.save();
            }
        }
    }

	 /**
     *  양산BOP의 Decessors 를 정의한다
     * @param alt_bop_obj
     * @param bop_shop_bopline
     * @param alt_prefix
     * @throws Exception
     */
    private void updatePertInfo(TCComponentBOPLine alt_bop_obj, TCComponentBOPLine bop_shop_bopline) throws Exception{

        TCComponent[] altDecessors = alt_bop_obj.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);

        TCComponentItemRevision alt_bop_revision = alt_bop_obj.getItemRevision();
        String alt_prefix = alt_bop_revision.getProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
        String alt_item_id = alt_bop_revision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);

        // ALTBOP 의 정의된 PreDecessors 를 양산 BOP에 적용한다
        if (altDecessors != null && altDecessors.length > 0) {
            // 양산BOP(Line)을 찾는다
            String bopID = alt_item_id.replace(alt_prefix + "-", "");
            TCComponentBOPLine bopLine = findBopLine(bopID, bop_shop_bopline);

            // 양산BOP(Line) decessors 를 찾는다
            List<TCComponentBOMLine> decessors = findDecessors(altDecessors, alt_prefix, bop_shop_bopline);
            if (decessors.size() > 0) {
                removeBopLineDecessors(bopLine);
                bopLine.addPredecessors(decessors);
            }
        }

        // 공정의 Decessors 를 정의 한다
       AIFComponentContext[] stationList = alt_bop_obj.getChildren();
       for (AIFComponentContext station : stationList) {
           TCComponentBOPLine stationBopLine = (TCComponentBOPLine)station.getComponent();
           TCComponent[] stationDecessors = stationBopLine.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);

           // ALTBOP 의 정의된 PreDecessors 를 양산 BOP에 적용한다
           if (stationDecessors != null && stationDecessors.length > 0) {
               // 양산BOP(Station)을 찾는다
               String stationLineProp = stationBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
               String bopID = stationLineProp.replace(alt_prefix + "-", "");
               TCComponentBOPLine stationLine = findBopLine(bopID, bop_shop_bopline);

               // 양산BOP(Line) stationDecessors 를 찾는다
               List<TCComponentBOMLine> decessors = findDecessors(stationDecessors, alt_prefix, bop_shop_bopline);
               if (decessors.size() > 0) {
                   removeBopLineDecessors(stationLine);
                   stationLine.addPredecessors(decessors);
               }
           }
       }
    }

    /**
     * Decessors 로 정의된 ALTBOPLine 을  양산BOPLine으로 바꾼다.
     * @param decessors
     * @param prefix
     * @param altBopLine
     * @return
     * @throws TCException
     */
    private List<TCComponentBOMLine> findDecessors( TCComponent[] decessors, String prefix, TCComponentBOPLine bopShopLine) throws TCException{
        List<TCComponentBOMLine> decessorsList = new ArrayList<TCComponentBOMLine>();
        for (TCComponent decessor : decessors) {
//            String bopID = prefix + "-" + decessor.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            String bopID = decessor.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            bopID = bopID.replace(prefix+ "-", "");
            decessorsList.add((TCComponentBOMLine)findBopLine(bopID, bopShopLine));
        }
        return decessorsList;
    }

    /**
     *  양산BOP의 정의된 PreDecessors 를 삭제한다
     * @param bopLine
     * @throws TCException
     */
    private void removeBopLineDecessors(TCComponentBOPLine bopLine) throws TCException{
        TCComponent[] decessors = bopLine.getReferenceListProperty(SDVPropertyConstant.ME_PREDECESSORS);
        // 기존 양산BOP의 정의된 PreDecessors 를 삭제한다
        if (decessors != null && decessors.length > 0) {
            for (TCComponent decessor : decessors) {
                bopLine.removePredecessor((TCComponentBOMLine)decessor);
            }
        }
    }

    /**
     *  ALTBOPLine 과 대칭되는 양산BOPLine 을 찾아온다
     * @param prefix
     * @param bopID
     * @param bopLine
     * @return
     * @throws TCException
     */
    private TCComponentBOPLine findBopLine(String bopID, TCComponentBOPLine bopLine) throws TCException{
        TCComponentBOPLine bop = null;
        if (bopLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM) || bopLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM)) {
            AIFComponentContext[] bopChilds = bopLine.getChildren();
            for (AIFComponentContext bopChild : bopChilds) {
                bop = (TCComponentBOPLine)bopChild.getComponent();
                if (bop.getProperty(SDVPropertyConstant.BL_ITEM_ID).equals(bopID)) {
                    return bop;
                }
                bop = findBopLine(bopID, bop);
                if (bop != null) {
                    return bop;
                }
            }
        }
        return bop;
    }
}
