package com.symc.plm.me.sdv.operation.wp.search;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;

public class SearchWeldConditionSheetsInitOperation extends AbstractSDVInitOperation {

    @Override
    public void executeOperation() throws Exception
    {
        try {
            //PreviewWeldConditionSheetMecoUpdateOperation.setLastMecoInfo("MEWPB13001");

            DataSet targetDataset = new DataSet();
            RawDataMap targetDataMap = new RawDataMap();

            TCComponentBOPLine selectTarget = getSelectTarget();
            TCComponentBOPWindow selectWindow = (TCComponentBOPWindow) selectTarget.window();
            TCComponentBOPLine topLine = (TCComponentBOPLine) selectWindow.getTopBOMLine();
            targetDataMap.put("topLine", topLine, IData.OBJECT_FIELD);

            // 선택한 BOP 의 RevisionRule 과 적용일을 가져온다 (적용일은 없으면 현재 날짜를 가져온다)
            String windowRevisionRule = selectWindow.getRevisionRule().toString();
            targetDataMap.put("revisionRule", windowRevisionRule, IData.STRING_FIELD);
            String targetWindowRevDate = getWindowRuleDate(selectWindow);
            targetDataMap.put("windowRevisionDate", targetWindowRevDate, IData.STRING_FIELD);

            // VariantRule 을 가져온다
            targetDataMap.put("variantRule", SDVBOPUtilities.getBOMConfiguredVariantSetToString(selectWindow), IData.STRING_FIELD);

            // Product Code 를 가져온다
            String productCode = topLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            targetDataMap.put(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE, productCode, IData.STRING_FIELD);

            // SHOP ID 를 가져온다
            String shopID = topLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
            targetDataMap.put(SDVTypeConstant.BOP_PROCESS_SHOP_ITEM, shopID, IData.STRING_FIELD);

            List<String> lineList = new ArrayList<String>();
            AIFComponentContext[] lines = topLine.getChildren();
            for (int i = 0; i < lines.length; i++)
            {
                TCComponentBOPLine lineBomLine = (TCComponentBOPLine) lines[i].getComponent();
                if (lineBomLine.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_LINE_ITEM))
                {
                    String lineCode = lineBomLine.getItemRevision().getProperty(SDVPropertyConstant.LINE_REV_CODE);
                    lineList.add(lineCode);
                    targetDataMap.put(lineCode, getStationList(lineBomLine), IData.LIST_FIELD);
                }
            }
            targetDataMap.put(SDVPropertyConstant.LINE_REV_CODE, lineList, IData.LIST_FIELD);
            targetDataset.addDataMap("SearchCriteriaInit", targetDataMap);

            setData(targetDataset);

        } catch (Exception ex) {
            throw ex;
        }
    }

    /**
     * 넘어온 Line 하위 Station Code 정보를 가져온다
     *
     * @method getStationList
     * @date 2013. 12. 12.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    private List<String> getStationList(TCComponentBOPLine line) throws TCException
    {
        List<String> stationList = new ArrayList<String>();
        AIFComponentContext[] lineChilds = line.getChildren();
        for (AIFComponentContext lineChild : lineChilds)
        {
            TCComponentBOPLine station = (TCComponentBOPLine) lineChild.getComponent();
            if (station.getItem().getType().equals(SDVTypeConstant.BOP_PROCESS_STATION_ITEM))
                stationList.add(station.getItemRevision().getProperty(SDVPropertyConstant.STATION_STATION_CODE));
        }
        return stationList;
    }

    /**
     *  Window 정보를 받아서 Revision 정보와 rule_date 정보를 가져온다
     *
     * @method getWindowRuleInfo
     * @date 2013. 12. 12.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getWindowRuleDate(TCComponentBOPWindow window) throws TCException
    {
        TCComponentRevisionRule revisionRule = window.getRevisionRule();
        Date date = revisionRule.getDateProperty("rule_date");
        String rule_date = null;
        String today = ExcelTemplateHelper.getToday("yyyy-MM-dd");

        if(date != null)
            rule_date = new SimpleDateFormat("yyyy-MM-dd").format(date);
        else
            rule_date = today;

        return rule_date;
    }

    /**
     *  선택한 Target에 BOPLine 정보를 가져온다
     *
     * @method getSelectTarget
     * @date 2013. 11. 14.
     * @param
     * @return TCComponentBOPLine
     * @exception
     * @throws
     * @see
     */
    public TCComponentBOPLine getSelectTarget()
    {
        TCComponentBOPLine getTarget = null;

        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();
        if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1 && (aaifcomponentcontext[0].getComponent() instanceof TCComponentBOMLine))
        {
            getTarget = (TCComponentBOPLine)aaifcomponentcontext[0].getComponent();
            return getTarget;
        }
        return null;
    }

}
