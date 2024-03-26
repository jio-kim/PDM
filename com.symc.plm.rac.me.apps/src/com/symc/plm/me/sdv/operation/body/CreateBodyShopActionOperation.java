/**
 * 
 */
package com.symc.plm.me.sdv.operation.body;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.operation.common.MecoOwnerCheckUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;

/**
 * Class Name : CreateBodyShopActionOperation
 * Class Description : 
 * [SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
 *  [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link���� 
 * 
 * @date 2013. 11. 20.
 *
 */
public class CreateBodyShopActionOperation extends AbstractSDVActionOperation {
	private Registry registry;
	private TCSession session;
	private TCComponentItemRevision shopRev;
	
    public CreateBodyShopActionOperation(int actionId, String ownerId, IDataSet dataSet) {
		super(actionId, ownerId, dataSet);

        registry = Registry.getRegistry(CreateBodyShopActionOperation.class);
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
    	IDataSet dataSet = getDataSet();
	    /* [CF-3537] [20230131] ���� �˻� ȭ�鿡�� �ݷ��� MECO�� �˻� �ȵǴ� ������ �־ �Ʒ� �������� ���� 
	    isWorkingStatus�� �ݷ��� MECO�� ���� �� �ְ� ���� ���� SearchTypeItemView���� MecoSearchView �˻�â���� ����  �Ʒ� getValue�κп� ȭ���� key���� ���� ���� �Ӽ��� ����*/
//    	Object meco_no = dataSet.getValue("mecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
    	Object meco_no = dataSet.getValue(SDVPropertyConstant.MECO_SELECT, SDVPropertyConstant.MECO_REV);
    	
    	// [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
    	if(meco_no!=null){
    		if(meco_no instanceof TCComponentItemRevision){
    			TCComponentItemRevision mecoRevision = isOwnedMECO((TCComponentItemRevision)meco_no);
    			if(mecoRevision==null){
    				throw new Exception("Check MECO owning user");
    			}
    		}
    	}
    	
		String shop_code = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_REV_SHOP_CODE);
		final Object mproduct_object = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
		final Object plant_object = dataSet.getValue("shopView", SDVTypeConstant.PLANT_SHOP_ITEM);
		String jph = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_REV_JPH);
		String allowance = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_REV_ALLOWANCE);
		Object is_alt = dataSet.getValue("shopView", SDVPropertyConstant.SHOP_REV_IS_ALTBOP);
		String alt_prefix = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_REV_ALT_PREFIX);
		String kor_name = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_REV_KOR_NAME);
		String eng_name = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_REV_ENG_NAME);
		String vehicle_korname = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME);
		String vehicle_engname = dataSet.getStringValue("shopView", SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME);

		if (mproduct_object == null)
			throw new NullPointerException(registry.getString("TargetItemInvalid.MESSAGE", "M-Product is null. contact to BOP Admin"));

		String mproductNo = ((TCComponentBOMLine) mproduct_object).getProperty(SDVPropertyConstant.BL_ITEM_ID);
		String item_id = (is_alt != null && is_alt.toString().toUpperCase().equals("TRUE") ? alt_prefix + "-" : "") + SDVPropertyConstant.ITEM_ID_PREFIX + "-" + shop_code + "-" + "P" + mproductNo.substring(1);

		session = CustomUtil.getTCSession();
		Markpoint mp = new Markpoint(session);
		TCComponentItem shopItem = null;
		try
		{
			if (item_id == null || item_id.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.SHOP_REV_SHOP_CODE + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (kor_name == null || kor_name.trim().length() == 0)
			{
				throw new NullPointerException(SDVPropertyConstant.SHOP_REV_KOR_NAME + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (is_alt != null && is_alt.toString().toUpperCase().equals("FALSE") && (meco_no == null || meco_no.toString().trim().length() == 0))
			{
				throw new NullPointerException(SDVPropertyConstant.SHOP_REV_MECO_NO + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}

			if (is_alt != null && is_alt.toString().toUpperCase().equals("TRUE") && (alt_prefix == null || alt_prefix.trim().length() == 0))
			{
				throw new NullPointerException(SDVPropertyConstant.SHOP_REV_ALT_PREFIX + " " + registry.getString("RequiredField.MESSAGE", "is a required field."));
			}
			
//			// ���� : bc.kim ��ü���� Shop ������ Plant View�� BOM ������ �Ǿ� ������ ���� ���  Validation �߰�
//			if( plant_object == null ) {
//				throw new NullPointerException("Shop ������ �ʿ��� Plant Bop�� �������� �ʾҽ��ϴ�. \n Plant BOP�� ���� ������ �ּ���" );
//			}
			//[20240326][UPGRADE] ���׷��̵� ���� ������� ���� ����
			//shopItem = SYMTcUtil.createItem(session, item_id, kor_name == null ? "" : kor_name, "", SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, SDVPropertyConstant.ITEM_REV_ID_ROOT);
			
			//Item Property �Ӽ� �Է�
			Map<String, String> itemPropMap = new HashMap<>();
			Map<String, String> itemRevsionPropMap = new HashMap<>();
			itemPropMap.put(SDVPropertyConstant.ITEM_ITEM_ID, item_id);
			itemPropMap.put(SDVPropertyConstant.ITEM_OBJECT_NAME, kor_name == null ? "" : kor_name);
			itemPropMap.put(SDVPropertyConstant.ITEM_OBJECT_DESC, item_id);
			
			//Item Revision �Ӽ� �Է�
			itemRevsionPropMap.put(SDVPropertyConstant.ITEM_REVISION_ID, SDVPropertyConstant.ITEM_REV_ID_ROOT);
			
			shopItem = (TCComponentItem)SYMTcUtil.createItemObject(session, SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, itemPropMap, itemRevsionPropMap);			
			
			shopRev = shopItem.getLatestItemRevision();
			//shopItem.lock();

			if (is_alt != null && is_alt.toString().toUpperCase().equals("TRUE"))
			{
				copyBOPShopOptions(shopRev, alt_prefix);
			}
			
			if (shop_code != null && shop_code.trim().length() > 0)
				shopRev.setProperty(SDVPropertyConstant.SHOP_REV_SHOP_CODE, shop_code);
			
			if (mproduct_object != null)
			{
				TCComponentItem sproductItem = SDVBOPUtilities.FindItem("P".concat(mproductNo.substring(1)), SDVTypeConstant.EBOM_PRODUCT_ITEM);
				TCComponentItem projectItem = SDVBOPUtilities.FindItem(sproductItem.getLatestItemRevision().getProperty(SDVPropertyConstant.S7_PROJECT_CODE), SDVTypeConstant.EBOM_PROJECT_ITEM);
				if (projectItem == null)
				{
					throw new NullPointerException(registry.getString("ProjectItemIsNull.MESSAGE", "Project Item not found of Product Item[%s].").replace("%s", "P".concat(mproductNo.substring(1))));
				}
				shopRev.setProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, sproductItem.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
				shopRev.setProperty(SDVPropertyConstant.SHOP_REV_VEHICLE_CODE, projectItem.getLatestItemRevision().getProperty(SDVPropertyConstant.S7_VEHICLE_NO));
			}
			//[SR140723-010][20140717] shcho, m7_JPH �Ӽ��� Ÿ���� �������� �ε� �Ҽ������� ����. �Ҽ�������5�ڸ����� �Է°���.
			if (jph != null && jph.trim().length() > 0)
				//shopRev.setIntProperty(SDVPropertyConstant.SHOP_REV_JPH, Integer.valueOf(jph));
			    shopRev.setDoubleProperty(SDVPropertyConstant.SHOP_REV_JPH, Double.parseDouble(jph));
			    

			if (allowance != null && allowance.trim().length() > 0)
				shopRev.setDoubleProperty(SDVPropertyConstant.SHOP_REV_ALLOWANCE, Double.valueOf(allowance));

			if (is_alt != null && is_alt.toString().trim().length() > 0)
				shopRev.setLogicalProperty(SDVPropertyConstant.SHOP_REV_IS_ALTBOP, is_alt.toString().toUpperCase().equals("TRUE") ? true : false);
			
			if (alt_prefix != null && alt_prefix.trim().length() > 0)
				shopRev.setProperty(SDVPropertyConstant.SHOP_REV_ALT_PREFIX, alt_prefix);
			
			if (eng_name != null && eng_name.trim().length() > 0)
				shopItem.setProperty(SDVPropertyConstant.SHOP_ENG_NAME, eng_name);

			// ��ü �Ӽ� ����
			shopRev.getTCProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).setStringValue("B");

			if (vehicle_korname != null && vehicle_korname.trim().length() > 0)
				 shopItem.setProperty(SDVPropertyConstant.SHOP_VEHICLE_KOR_NAME, vehicle_korname);

			if (vehicle_engname != null && vehicle_engname.trim().length() > 0)
				 shopItem.setProperty(SDVPropertyConstant.SHOP_VEHICLE_ENG_NAME, vehicle_engname);

			// NewStuff ������ �߰��Ѵ�.
			session.getUser().getNewStuffFolder().add("contents", shopItem);
			
			if (meco_no != null)
			{
				shopRev.setReferenceProperty(SDVPropertyConstant.SHOP_REV_MECO_NO, (TCComponent) meco_no);

				// MECO Solution�� ����
				((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, shopRev);
			}
			
			//shopItem.save();

			final AbstractAIFOperation openOperation = new AbstractAIFOperation() {
				
				@Override
				public void executeOperation() throws Exception {
                    try {
                    	MFGLegacyApplication mfgApp = (MFGLegacyApplication) AIFUtility.getCurrentApplication();
                    	mfgApp.open(shopRev);
                    	this.storeOperationResult(IStatus.OK);
                    } catch (Exception e) {
                        e.printStackTrace();
                        this.storeOperationResult(IStatus.ERROR);
                        return;
                    }
				}
			};
			openOperation.addOperationListener(new InterfaceAIFOperationListener() {
				@Override
				public void startOperation(String arg0) {
				}
				
				@Override
				public void endOperation() {
					try {
						Thread.sleep(1000);

						if (openOperation.getOperationResult().equals(IStatus.OK))
						{
						    /*  [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link���� 
							if (mproduct_object != null)
								shopRev.add(SDVTypeConstant.MFG_TARGETS, ((TCComponentBOMLine) mproduct_object).getItemRevision());
							*/
							
							if (plant_object != null)
								shopRev.add(SDVTypeConstant.MFG_WORKAREA, ((TCComponentBOMLine) plant_object).getItemRevision());
//							shopRev.save();
						}
					} catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
			});
			session.queueOperation(openOperation);
		}
		catch (Exception ex)
		{
			mp.rollBack();
			setErrorMessage(ex.getMessage());
			setExecuteError(ex);
			throw ex;
		}
		finally
		{
//			if (shopItem != null)
//				shopItem.unlock();

			session.setStatus("");
		}

		mp.forget();
	}
    
	/**
	 * [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
	 * MECO�� Owner �� ���� Login �� User�� �ٸ� ��� Operation�� ���̻� ���� �� �� ������ �Ѵ�.
	 * @return
	 */
	private TCComponentItemRevision isOwnedMECO(TCComponentItemRevision mecoRevision){
		
		TCComponentItemRevision ownMecoItemRevision = null;
	
    	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoRevision, (TCSession)this.getSession());
    	ownMecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
		
	    return ownMecoItemRevision;
	}

    /**
     * �ɼ� ����
     * @param altShopRevision
     * @param altPrefix
     * @throws Exception
     */
	private void copyBOPShopOptions(TCComponentItemRevision altShopRevision, String altPrefix) throws Exception {
		try
		{
//			session.setStatus("Coping Shop Options...");

			String alt_item_id = altShopRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			TCComponentItem bopShopItem = CustomUtil.findItem(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, alt_item_id.replace(altPrefix + "-", ""));
			if (bopShopItem != null)
			{
				TCComponentBOMLine bopShopLine = CustomUtil.getBomline(bopShopItem.getLatestItemRevision(), session);
				TCComponentBOMLine altShopLine = CustomUtil.getBomline(altShopRevision, session);

				TCVariantService variantService = session.getVariantService();
				ModularOption[] srcOptions = SDVBOPUtilities.getModularOptions(bopShopLine);
				ModularOption[] copyToOptions = SDVBOPUtilities.getModularOptions(altShopLine);

				// �����ϱ� ���� ��� �ɼǵ��� ������.
				if (copyToOptions != null && copyToOptions.length > 0)
				{
					// Option ���� ���� ���� ����
					variantService.setLineMvl(altShopLine, "");
					// �� �ɼ� ����
					for (ModularOption copyToOption : copyToOptions)
					{
						variantService.lineDeleteOption(altShopLine, copyToOption.optionId);
					}
				}

				// �ɼǵ��� ���
//				String[] corpIds = session.getPreferenceService().getStringArray(TCPreferenceService.TC_preference_site, "PSM_global_option_item_ids");
				String[] corpIds = session.getPreferenceService().getStringValuesAtLocation("PSM_global_option_item_ids", TCPreferenceLocation.OVERLAY_LOCATION);
				for (ModularOption srcOption : srcOptions) {
	                HashMap<Integer, OVEOption> options = new HashMap<Integer, OVEOption>();
	                OVEOption oveOption = CustomMVPanel.getOveOption(bopShopLine, options, srcOption);
	                // ����� �ɼ� ��
	                String optionValue = SDVBOPUtilities.getOptionString(corpIds[0], oveOption.option.name, oveOption.option.desc);

	                // (�ɼ� �߰�) �ɼ��� ��� BOMLINE�� �߰���
	                variantService.lineDefineOption(altShopLine, optionValue);
	            }

				// �ɼ��� ���� ����
	            String srcOptItemId = MVLLexer.mvlQuoteId(bopShopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);
	            String targetOptItemId = MVLLexer.mvlQuoteId(altShopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID), true);

	            // ���� �ɼ� ��ȿ���˻� ������ ������
	            String lineMvl = variantService.askLineMvl(bopShopLine).replace(srcOptItemId, targetOptItemId);

	            // ��� BOMLINE�� �ɼ� ��ȿ���˻� ������ ������
	            variantService.setLineMvl(altShopLine, lineMvl);
			}
		}
		catch (Exception ex)
		{
			throw ex;
		}
	}
}
