/**
 *
 */
package com.symc.plm.me.sdv.validate;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.ISDVValidator;
import org.sdv.core.common.exception.SDVException;
import org.sdv.core.common.exception.ValidateSDVException;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.StringUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVStringUtiles;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.Registry;

/**
 * Class Name : PertInfoCheckSDVValidator
 * Class Description :
 *
 * @date 2014. 4. 3.
 *
 */
@SuppressWarnings({ "unchecked" })
public class PertInfoCheckSDVValidator implements ISDVValidator {
    private Registry registry = Registry.getRegistry(PertInfoCheckSDVValidator.class);
    private final String serviceClassName = "com.ssangyong.service.BopPertService";

    /**
     * Description :
     *
     * @method :
     * @date : 2014. 4. 3.
     * @param :
     * @return :
     * @see org.sdv.core.common.ISDVValidator#validate(java.lang.String, java.util.Map)
     */
    @Override
    public void validate(String commandId, Map<String, Object> parameter, Object applicationCtx) throws SDVException {
        try {
            InterfaceAIFComponent[] selectedTargets = CustomUtil.getCurrentApplicationTargets();
            TCComponentBOPLine bopLine = null;
            TCComponentBOPLine bopTopLine = null;

            int stationCount = 0;
            ArrayList<HashMap<String, Object>> pertResult = new ArrayList<HashMap<String, Object>>();
            ArrayList<HashMap<String, Object>> pertDBResult = new ArrayList<HashMap<String, Object>>();

            for (InterfaceAIFComponent selectedTarget : selectedTargets)
            {
                bopLine = (TCComponentBOPLine)selectedTarget;
                bopTopLine = (TCComponentBOPLine) bopLine.window().getTopBOMLine();
            }
            stationCount = getSelectBopStationCount(bopTopLine);
            pertResult = getSelectBopStationPertCountList(bopTopLine);
            pertDBResult = getSelectBopStationDecessorsList(bopTopLine);

            // PERT 정보가 올바르게 되어 있는지 확인한다 (BOP 의 공정 갯수 = PERT 의 순서대로 가져온 공정 갯수)
            if ((stationCount == 0 || pertResult.size() == 0) || (stationCount != pertResult.size())) {
                throw new ValidateSDVException(registry.getString("notPertInfo.Station.MESSAGE", "Please update the information PERT."));
            }

            // PERT 구성중 Successors 가 없는 공정이 1개 이상인지 체크한다
            if (!getPertNotHaveSuccessorsCount(pertResult)) {
                throw new ValidateSDVException(registry.getString("notPertInfo.Station.MESSAGE", "Please update the information PERT."));
            }

            // PERT 정보의 Update 가 필요한지 체크 필요하다면 Update 하고 진행
            boolean pertListFlag = comparePertInfo(pertResult, pertDBResult);
            if (!pertListFlag) {
                registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
                Shell shell = AIFUtility.getActiveDesktop().getShell();
                int confirmRet = ConfirmDialog.prompt(shell, registry.getString("notPertInfo.Confirmation.TITLE", "Confirm"), registry.getString("notPertInfo.BOPPertInfoUpdate.MESSAGE", "History information is also modified PERT. \n Do you want to modify?"));
                if (confirmRet == 2){
                    try {
                        updatePertInfo(bopTopLine, pertDBResult);
                    } catch (TCException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else{
                    throw new ValidateSDVException(registry.getString("updatePertInfo.Station.MESSAGE", "Please update the information PERT."));
                }
            }


        } catch (ValidateSDVException ve) {
            throw ve;
        } catch (Exception e) {
            throw new ValidateSDVException(e.getMessage(), e);
        }

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
    private boolean getPertNotHaveSuccessorsCount(ArrayList<HashMap<String, Object>> pertResult) {
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

        if( pertDBResult.size() == pertResult.size() ) {
        	for (int i = 0; i < pertResult.size(); i++) {
        		String pertSuccessors = (String) pertResult.get(i).get("SUCCESSORS");
        		String pertDBSuccessors = (String) pertDBResult.get(i).get("SUCCESSORS");
        		if (!pertResult.get(i).get("ID").equals(pertDBResult.get(i).get("ID")) || !StringUtil.nullToString(pertSuccessors).equals(StringUtil.nullToString(pertDBSuccessors)) ) {
        			return false;
        		}
        	}
        	return true;
        	
        } else {
        	return false;
        }
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

}
