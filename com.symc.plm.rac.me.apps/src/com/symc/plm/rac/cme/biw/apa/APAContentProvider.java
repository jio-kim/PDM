// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3)

package com.symc.plm.rac.cme.biw.apa;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchWindow;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.PartData;
import com.symc.plm.rac.cme.biw.apa.resulttable.datastructure.ResultData;
import com.symc.plm.rac.cme.biw.apa.search.PartSearch;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.framework.treetable.CMEBOMTreeTable;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.psebase.AbstractBOMLineViewerApplication;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.PlatformHelper;
import com.teamcenter.rac.util.Registry;

// Referenced classes of package com.teamcenter.rac.cme.biw.apa:
//            APADialog

/**
 * [SR140721-004][20140729] shcho, �����ǳ� Dialog�� ǥ�õǴ� ������ Dialog���� ����ڰ� �Է��� ������ ���� �� �� �ֵ��� ����.
 * [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
 * [NON-SR][20150703] shcho, �˻� ����� ȭ�鿡 �ùٷ� �ݿ����� �ʴ� ��찡 ���������� �߻��Ͽ� ����ȭ ó�� ���� �߰�
 */
@SuppressWarnings({ "unchecked" })
public class APAContentProvider
    implements IStructuredContentProvider
{

    private TCComponentRevisionRule revisionRule;
    public ResultData[] getDisplayData()
    {
        return displayData;
    }

    APAContentProvider(APADialog apadialog)
    {
        search = null;
        viewerApp = null;
        filterTypes = null;
        filterTypesOn = false;
        filter_out_assigned = false;
        dialog = apadialog;
    }

    public Object[] getElements(Object obj)
    {
        if(search == null)
            search = new PartSearch(dialog.getSession());
        if(viewerApp == null) {
            viewerApp = (AbstractBOMLineViewerApplication)AIFUtility.getCurrentApplication();
            revisionRule = viewerApp.getCurrentConfigurable().getRevisionRule();
        }
        displayData = resultData;
        if(filterTypesOn)
            filterResults(displayData);
        if(filter_out_assigned)
            filterOutAssigned(displayData);
        if(displayData == null)
            displayData = new ResultData[0];
        return displayData;
    }

    private TCComponent[] getDisplayedMFGs()
    {
        if(displayData == null)
            return null;
        int i = displayData.length;
        TCComponent atccomponent[] = new TCComponent[i];
        for(int j = 0; j < i; j++)
            atccomponent[j] = (TCComponent)displayData[j].getRowContext().get("MFGName");

        return atccomponent;
    }

    @SuppressWarnings({ "rawtypes" })
    public void filterResults(ResultData aresultdata[])
    {
        ArrayList arraylist = new ArrayList();
        if(filterTypes == null)
        {
            displayData = new ResultData[0];
            return;
        }
        if(filterTypes.contains("ShowAll"))
        {
            displayData = aresultdata;
            return;
        }
        ResultData aresultdata1[] = aresultdata;
        int i = aresultdata1.length;
        for(int j = 0; j < i; j++)
        {
            ResultData resultdata = aresultdata1[j];
            String s = resultdata.getColumnValue("MFGType");
            if(filterTypes.contains(s))
                arraylist.add(resultdata);
        }

        displayData = (ResultData[])arraylist.toArray(new ResultData[arraylist.size()]);
    }

    public void dispose()
    {
    }

    public void inputChanged(Viewer viewer, Object obj, Object obj1)
    {
    }

    //[SR140721-004][20140729] shcho, �����ǳ� Dialog�� ǥ�õǴ� ������ Dialog���� ����ڰ� �Է��� ������ ���� �� �� �ֵ��� ����.
    public void performSpatialSearch(final TCComponentBOMLine productBOMLine)
        throws Exception
    {
        final TCComponent atccomponent[] = getDisplayedMFGs();
        clearDisplayData();

        // [NON-SR][20160119] taeku.jeong Connected Part Dialog���� Connected Part �˻��� UI ����������� ��Ÿ���� ���� ���� 
//        Display display = Display.getDefault();
//        display.syncExec(new Runnable() {
//            @Override
//            public void run() {
//                displayData = search.searchClosestParts(atccomponent, viewerApp, revisionRule, displayData, productBOMLine);
//            }
//        });
        
        displayData = search.searchClosestParts(atccomponent, viewerApp, revisionRule, displayData, productBOMLine);

        updateSearchData();
    }

    private void clearDisplayData()
        throws Exception
    {
        ResultData aresultdata[] = displayData;
        int i = aresultdata.length;
        for(int j = 0; j < i; j++)
        {
            ResultData resultdata = aresultdata[j];
            // jwlee �ҽ����� 5���� 18����
            for(int k = 0; k <= 26; k++)
            {
                String s = ResultData.columnIndexToKey(k);
                Object obj = resultdata.getColumnObjectValue(s);
                if(!(obj instanceof PartData))
                    continue;
                PartData partdata = (PartData)obj;
                if(partdata.isSuggested())
                    resultdata.removeColumn(s);
            }

        }

    }

    private void updateSearchData()
    {
        ResultData aresultdata[] = displayData;
        int i = aresultdata.length;
        for(int j = 0; j < i; j++)
        {
            ResultData resultdata = aresultdata[j];
            resultData[resultdata.getSerialNumber()] = resultdata;
        }

    }

    public void generateData(final TCComponentBOMLine tccomponentbomline)
    {
        if(search == null)
            search = new PartSearch(dialog.getSession());
        Display display = Display.getDefault();
        display.syncExec(new Runnable() {
            @Override
            public void run() {
                resultData = search.searchAll(tccomponentbomline);
            }
        });
        
        if(resultData == null)
            return;
        for(int i = 0; i < resultData.length; i++)
            resultData[i].setSerialNumber(i);

    }

    public void setFilterTypes(ArrayList<String> arraylist, boolean flag)
    {
        filterTypes = arraylist;
        filterTypesOn = flag;
    }

    @SuppressWarnings({ "rawtypes", "unused" })
    public void filterOutAssigned(ResultData aresultdata[])
    {
        if(!filter_out_assigned)
            return;
        ArrayList arraylist = new ArrayList();
        if(aresultdata == null)
            return;
        ResultData aresultdata1[] = aresultdata;
        int i = aresultdata1.length;
        for(int j = 0; j < i; j++)
        {
            ResultData resultdata = aresultdata1[j];
            int k = resultdata.getColumnValues().size() - 3;
            Object obj = null;
            boolean flag = false;
            if(k > 0)
            {
                PartData apartdata[] = new PartData[k];
                int l = 0;
                do
                {
                    if(l >= k)
                        break;
                    apartdata[l] = (PartData)resultdata.getColumnObjectValue((new StringBuilder()).append("Part").append(l + 1).toString());
                    if(apartdata[l].isDoConnection() && !apartdata[l].isSuggested())
                    {
                        flag = true;
                        break;
                    }
                    l++;
                } while(true);
            }
            if(!flag && !resultdata.getfilterOut())
                arraylist.add(resultdata);
        }

        displayData = (ResultData[])arraylist.toArray(new ResultData[arraylist.size()]);
    }

    public boolean isFilter_out_assigned()
    {
        return filter_out_assigned;
    }

    public void setFilter_out_assigned(boolean flag)
    {
        filter_out_assigned = flag;
    }

    @SuppressWarnings("rawtypes")
    public void modifyPartSettings(boolean flag, boolean flag1, Object obj, int i)
    {
        if(!(obj instanceof ResultData))
            return;
        ResultData resultdata = (ResultData)obj;
        int j = resultdata.getRowContext().size();
        
        if(j - i >= 0)
        {
            Hashtable hashtable = resultdata.getColumnValues();
            // [NON-SR][20160106] taeku.jeong Connected Part Add �Ǵ� Remove ��ư�� ������� ���õ� Part���� �������� ����
            // Add �Ǵ� Remove�� ǥ�õǴ� ���� ���� append()�� Arggument���� 3�� ������ ������.
            PartData partdata = (PartData)hashtable.get((new StringBuilder()).append("Part").append(i-3).toString());
            if(partdata == null || flag && partdata.isDoConnection() || !flag && !partdata.isDoConnection())
                return;
            partdata.setDoConnection(flag);
            if(flag1 && !partdata.isRemovalCandidate())
                partdata.setSuggested(true);
            else
            if(flag)
                partdata.setRemovalCandidate(false);
        }
    }

    public ResultData[] getResultData()
    {
        return resultData;
    }

    @SuppressWarnings("rawtypes")
    public PartData getPartData(Object obj, int i)
    {
        if(!(obj instanceof ResultData))
        {
            return null;
        } else
        {
            ResultData resultdata = (ResultData)obj;
            Hashtable hashtable = resultdata.getColumnValues();
            PartData partdata = (PartData)hashtable.get((new StringBuilder()).append("Part").append(i-3).toString());
            return partdata;
        }
    }

    @SuppressWarnings("rawtypes")
    public Object getDisplayPartLine(Object obj, int i)
    {
        if(!(obj instanceof ResultData))
            return null;
        TCComponent tccomponent = null;
        ResultData resultdata = (ResultData)obj;
        Hashtable hashtable = resultdata.getRowContext();
        System.out.println("getDisplayPartLine.i = "+i);
        if(i == 0 || i == 1 || i == 2 || i == 3){
            tccomponent = (TCComponent)hashtable.get("MFGName");
        }else{
        	String keyStr = (new StringBuilder()).append("Part").append(i-3).toString();
        	System.out.println("getDisplayPartLine.keyStr = "+keyStr);
            tccomponent = (TCComponent)hashtable.get(keyStr);
            System.out.println("getDisplayPartLine.tccomponent = "+tccomponent);
        }
        return tccomponent;
    }

    public boolean isEmpty()
    {
        return resultData == null || resultData.length == 0;
    }

    /**
     * jwlee MPP�� Ȱ��ȭ �Ǿ��ִ� view ���� BOMView �� �����Ͽ� BOMLine ������ ��ư���
     *       MPP �� �������� TAB View �߿� cc ������ TYPE ������ ���Ͽ� ������ BOP �ǰ�
     *       �����Ǿ� �ִ� BOMVIEW �����͸� �����´�
     *       
     *  [SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Link������ MProduct�� ã�� �� �ֵ��� ����
     *       
     */
    public TCComponentBOMLine getBOMLine(TCComponentBOMLine selectBOMLine) {
        String rootBomView = null;
        String targetProduct = null;
        try {
            TCComponentItemRevision itemRevision = selectBOMLine.window().getTopBOMLine().getItemRevision();
            String productCode = itemRevision.getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
            if(productCode != null) {
                targetProduct = "M".concat(productCode.substring(1));
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        IViewReference[] arrayOfIViewReference = PlatformHelper.getCurrentPage().getViewReferences();
        //ISelection iSelection = PlatformHelper.getCurrentPage().getSelection();
        for (IViewReference viewRerence : arrayOfIViewReference) {
            IViewPart localIViewPart = viewRerence.getView(false);
            if (localIViewPart == null)
                continue;
            CMEBOMTreeTable cmeBOMTreeTable = (CMEBOMTreeTable) AdapterUtil.getAdapter(localIViewPart, CMEBOMTreeTable.class);
            if (cmeBOMTreeTable == null)
                continue;

            rootBomline = cmeBOMTreeTable.getBOMRoot();
            try {
                rootBomView = rootBomline.getProperty("bl_item_item_id");
            } catch (TCException e) {
                e.printStackTrace();
            }
            if (targetProduct != null && rootBomView != null)
            {
                if (targetProduct.equals(rootBomView))
                    return rootBomline;
            }

        }
        return null;
    }

    /**
     * jwlee BOP �ֻ����� SHOP�� ����Ǿ� �ִ� BOMView ����(M7_Product) �� Ȯ���Ͽ� M_Product ID ������ ��ȯ �Ѵ�
     */
    /*[SR150122-027][20150309]shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link������ ���̻� ������.
    public static String getMProduct(TCComponentItemRevision revision) throws TCException {
        String mProductType = null;
        String mProductId = null;
        TCComponent[] mProduct = revision.getRelatedComponents("IMAN_METarget");
        if (mProduct.length == 1)
        {
            mProductType = mProduct[0].getProperty("object_type");
            if (mProductType.equals("S7_ProductRevision") || mProductType.equals("M7_MfgProductRevision"))
                return mProductId = mProduct[0].getProperty("item_id");
        }
        return mProductId;
    }
    */

    /**
     * jwlee ���� �ǳ� �˻��� ���� �Ϸ��� BOP �� OccurrenceGroup �� �����Ǿ� �ִ��� üũ�Ѵ�
     *
     * @method occurrenceGroupCheck
     * @date 2013. 12. 31.
     * @param
     * @return boolean
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    public boolean occurrenceGroupCheck(TCComponentBOMLine selectBOMLine) throws TCException
    {
        TCComponentBOMLine topBomLine =  selectBOMLine.window().getTopBOMLine();
        if (topBomLine.getReferenceListProperty(SDVPropertyConstant.BL_MFG0ASSIGNED_MATERIAL) == null || topBomLine.getReferenceListProperty(SDVPropertyConstant.BL_MFG0ASSIGNED_MATERIAL).equals(""))
            return false;

        return true;
    }

    /**
     * ���� �Ѿ�� BOPShop PERT ������� List �� �ѱ��
     *
     * @method getSelectBopStationPertCountList
     * @date 2014. 3. 26.
     * @param
     * @return ArrayList<HashMap<String,Object>>
     * @exception
     * @throws
     * @see
     */
    public ArrayList<HashMap<String, Object>> getSelectBopStationPertCountList(TCComponentBOMLine topBopLine) throws Exception{
        ArrayList<HashMap<String, Object>> results;
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();

        DataSet ds = new DataSet();
        ds.put("SHOP_ID", topBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

        results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectBopStationPertCountList", ds);

        return results;
    }

    /**
     * ���� �Ѿ�� BOPShop ���� ������ ������ �����Ѵ�
     *
     * @method getSelectBopStationCount
     * @date 2014. 3. 26.
     * @param
     * @return int
     * @exception
     * @throws
     * @see
     */
    public int getSelectBopStationCount(TCComponentBOMLine topBopLine) throws Exception{
        int stationCount = 0;
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();

        DataSet ds = new DataSet();
        ds.put("SHOP_ID", topBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

        ArrayList<HashMap<String, Object>> results;

        results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectBopStationCount", ds);
        if (results.size() > 0) {
            stationCount = Integer.parseInt(results.get(0).get("COUNT").toString());
        }
        return stationCount;
    }

    /**
     * DB�� ����Ǿ� �ִ� PERT ������ �����´�
     *
     * @method getSelectBopStationDecessorsList
     * @date 2014. 3. 26.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public ArrayList<HashMap<String, Object>> getSelectBopStationDecessorsList(TCComponentBOMLine topBopLine) throws Exception{
        ArrayList<HashMap<String, Object>> results;
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();

        DataSet ds = new DataSet();
        ds.put("SHOP_ID", topBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));

        results = (ArrayList<HashMap<String, Object>>) remoteUtil.execute(serviceClassName, "selectBopStationDecessorsList", ds);

        return results;
    }

    /**
     * ����ڰ� PERT ���� ������Ʈ�� �ʿ��ϴٰ� ��û�� �ش� �޼ҵ带 ȣ���Ѵ�
     * PERT ���� ����
     *
     * @method updatePertInfo
     * @date 2014. 3. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void updatePertInfo(TCComponentBOMLine topBopLine, ArrayList<HashMap<String, Object>> pertDBResult) throws Exception{
        SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil();
        DataSet ds = new DataSet();
        ds.put("SHOP_ID", topBopLine.getProperty(SDVPropertyConstant.BL_ITEM_ID));
        ds.put("USER", CustomUtil.getTCSession().getUserName());

        if (pertDBResult != null && pertDBResult.size() > 0) {
            String toDate = CustomUtil.getToDate();
            toDate = toDate.replace("-", "");
            String[] toDateList = toDate.split(" ");
            toDate = toDateList[0];
            Date CREATE_DATE = (Date) pertDBResult.get(0).get("MODIFY_DATE");
            String dbDate = SDVStringUtiles.dateToString(CREATE_DATE, "yyyyMMdd");
            //String dbDate = CustomUtil.get

            Long toDateLong = Long.parseLong(toDate);
            Long dbDateLong = Long.parseLong(dbDate);

            long resultDate = toDateLong - dbDateLong;
            if (resultDate > 1) {
                remoteUtil.execute(serviceClassName, "updateBopPertInfo", ds);
            }else{
                remoteUtil.execute(serviceClassName, "deleteBopPertInfo", ds);
            }
        }

        remoteUtil.execute(serviceClassName, "insertBopStationDecessorsInfo", ds);
    }

    /**
     * Successors �� ���� ������ �Ѱ� �̻����� üũ�Ѵ�
     *
     * @method getPertNotHaveSuccessorsCount
     * @date 2014. 4. 9.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public boolean getPertNotHaveSuccessorsCount(ArrayList<HashMap<String, Object>> pertResult) {
        int successorsCount = 0;
        for (int i = 0; i < pertResult.size(); i++) {
            if (pertResult.get(i).get("SUCCESSORS") == null || pertResult.get(i).get("SUCCESSORS").equals("")) {
                successorsCount++;
            }
            if (successorsCount > 1) {
                return false;
            }
        }
        return true;
    }

    /**
     * �ǽð����� ������ PERT ������ DB �� ����� PERT ������ ���Ѵ�
     *
     * @method comparePertInfo
     * @date 2014. 3. 26.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public boolean comparePertInfo(ArrayList<HashMap<String, Object>> pertResult, ArrayList<HashMap<String, Object>> pertDBResult){
        if (pertDBResult == null || pertDBResult.size() == 0) return false;

        for (int i = 0; i < pertResult.size(); i++) {
            if (!pertResult.get(i).get("ID").equals(pertDBResult.get(i).get("ID"))) {
                return false;
            }
        }
        return true;
    }

    private APADialog dialog;
    private ResultData resultData[];
    private PartSearch search;
    private AbstractBOMLineViewerApplication viewerApp;
    private ArrayList<String> filterTypes;
    private boolean filterTypesOn;
    private boolean filter_out_assigned;
    private ResultData displayData[];
    private TCComponentBOMLine rootBomline;
    @SuppressWarnings("unused")
    private Registry registry;
    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(PartSearch.class);
    private final String serviceClassName = "com.kgm.service.BopPertService";
}
