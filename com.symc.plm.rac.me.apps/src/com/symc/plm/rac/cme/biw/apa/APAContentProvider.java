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
 * [SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
 * [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
 * [NON-SR][20150703] shcho, 검색 결과가 화면에 올바로 반영되지 않는 경우가 간헐적으로 발생하여 동기화 처리 로직 추가
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

    //[SR140721-004][20140729] shcho, 결합판넬 Dialog에 표시되는 개수를 Dialog에서 사용자가 입력한 값으로 조절 할 수 있도로 변경.
    public void performSpatialSearch(final TCComponentBOMLine productBOMLine)
        throws Exception
    {
        final TCComponent atccomponent[] = getDisplayedMFGs();
        clearDisplayData();

        // [NON-SR][20160119] taeku.jeong Connected Part Dialog에서 Connected Part 검색중 UI 응답없음으로 나타나는 현상 수정 
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
            // jwlee 소스변경 5에서 18변경
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
            // [NON-SR][20160106] taeku.jeong Connected Part Add 또는 Remove 버튼을 누른경우 선택된 Part보다 오른쪽의 것이
            // Add 또는 Remove로 표시되는 현상 수정 append()의 Arggument에서 3을 빼도록 수정함.
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
     * jwlee MPP의 활성화 되어있는 view 에서 BOMView 를 선택하여 BOMLine 정보를 담아간다
     *       MPP 의 여러개에 TAB View 중에 cc 정보와 TYPE 정보를 비교하여 선택한 BOP 탭과
     *       연관되어 있는 BOMVIEW 데이터를 가져온다
     *       
     *  [SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Link해제된 MProduct를 찾을 수 있도록 수정
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
     * jwlee BOP 최상의인 SHOP에 연결되어 있는 BOMView 정보(M7_Product) 를 확인하여 M_Product ID 정보를 반환 한다
     */
    /*[SR150122-027][20150309]shcho, 공법 할당 E/Item의 설계 DPV에 의한 자동 변경 문제 해결 - Shop과 MProduct Link해제로 더이상 사용안함.
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
     * jwlee 결합 판넬 검색을 실행 하려는 BOP 의 OccurrenceGroup 이 생성되어 있는지 체크한다
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
     * 현재 넘어온 BOPShop PERT 순서대로 List 를 넘긴다
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
     * 현재 넘어온 BOPShop 하위 공정의 갯수를 리턴한다
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
     * DB의 저장되어 있는 PERT 정보를 가져온다
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
     * 사용자가 PERT 정보 업데이트가 필요하다고 요청시 해당 메소드를 호출한다
     * PERT 정보 저장
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
     * Successors 가 없는 공정이 한개 이상인지 체크한다
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
     * 실시간으로 가져온 PERT 정보와 DB 의 저장된 PERT 정보를 비교한다
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
