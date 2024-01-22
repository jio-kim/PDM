/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.operation.weightmasterlist;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.ssangyong.common.utils.variant.OptionManager;
import com.ssangyong.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;

/**
 * @author jinil
 *
 */
public class WeightMasterListDialogInitOperation extends AbstractAIFOperation
{
    private WaitProgressBar waitBar = null;
    private TCSession session = null;

    public WeightMasterListDialogInitOperation(TCSession tcSession, WaitProgressBar waitBar)
    {
        this.session = tcSession;
        this.waitBar = waitBar;
    }

    /* (non-Javadoc)
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try
        {
            waitBar.setStatus("Search Pre Product Items.");

            ArrayList<TCComponentItem>allProductList = getAllProductList();

            waitBar.setStatus("Search Done.");
            storeOperationResult(allProductList);
//            ospec = BomUtil.getOSpec(ospecRevision);

//            waitBar.setStatus("Loading Fmp Option Info.");
//            for (TCComponentBOMLine fmpLine : allFMPList)
//            {
//                // FMP에서 옵션 리스트를 가져와야 함.
//                optionManager.put(fmpLine.getProperty(PropertyConstant.ATTR_NAME_ITEMID), new OptionManager(fmpLine, false, waitBar));
//                fmpOptionList.put(fmpLine.getProperty(PropertyConstant.ATTR_NAME_ITEMID), optionManager.get(fmpLine.getProperty(PropertyConstant.ATTR_NAME_ITEMID)).getOptionSet(fmpLine,null, null, null, false, false));
//            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private ArrayList<TCComponentItem> getAllProductList() throws Exception {
        try
        {
            ArrayList<TCComponentItem> productLists = new ArrayList<TCComponentItem>();

            TCComponent[] findProducts = CustomUtil.queryComponent("Item...", new String[]{"Type", "Item ID"}, new String[]{TypeConstant.S7_PREPRODUCTTYPE, "*"});

            for (TCComponent findProduct : findProducts)
            {
                productLists.add((TCComponentItem) findProduct);
            }

            return productLists;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private ArrayList<TCComponentBOMLine> getAllFMPList(TCComponentBOMLine bomLine) throws Exception
    {
        ArrayList<TCComponentBOMLine> fmpLines = new ArrayList<TCComponentBOMLine>();

        try
        {
            for (AIFComponentContext childLine : bomLine.getChildren())
            {
                if (((TCComponentBOMLine) childLine.getComponent()).getItemRevision().getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE))
                {
                    fmpLines.add((TCComponentBOMLine) childLine.getComponent());
                }
                else if (((TCComponentBOMLine) childLine.getComponent()).getItemRevision().getType().equals(TypeConstant.S7_PREFUNCTIONREVISIONTYPE))
                {
                    for (AIFComponentContext cchildLine : ((TCComponentBOMLine) childLine.getComponent()).getChildren())
                    {
                        if (((TCComponentBOMLine) cchildLine.getComponent()).getItemRevision().getType().equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE))
                        {
                            fmpLines.add((TCComponentBOMLine) cchildLine.getComponent());
                        }
                    }
                }
            }

            return fmpLines;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {

    }

//    public OSpec getOspec() {
//        return ospec;
//    }
//
//    public HashMap<String, OptionManager> getAllOptionManagerMap()
//    {
//        return optionManager;
//    }
//
//    public HashMap<String, ArrayList<VariantOption>> getAllVariantOption() {
//        return fmpOptionList;
//    }
//
//    public TCComponentBOMLine[] getFMPLines() {
//        return allFMPList.toArray(new TCComponentBOMLine[0]);
//    }

}
