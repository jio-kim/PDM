package com.ssangyong.service;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.ECOHistoryDao;
import com.ssangyong.rac.kernel.SYMCBOMEditData;
import com.ssangyong.rac.kernel.SYMCECODwgData;
import com.ssangyong.rac.kernel.SYMCPartListData;

public class MECOHistoryService {

    public List<String> selectUserWorkingECO(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectUserWorkingECO(ds);
    }
    
    
    public Boolean isECOEPLChanged(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.isECOEPLChanged(ds);
    }
    
    public Boolean extractEPL(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.extractEPL(ds);
    }

    /**
     * ECO-B(DWG) 테이블 리스트
     * 
     * @method selectECODwgList
     * @date 2013. 2. 20.
     * @param
     * @return List<String>
     * @exception
     * @throws
     * @see
     */
    public List<SYMCECODwgData> selectECODwgList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECODwgList(ds);
    }
    
    /**
     * ECO-B Properties Update
     * 
     * @method updateECOEPLProperties 
     * @date 2013. 3. 5.
     * @param
     * @return Boolean
     * @exception
     * @throws
     * @see
     */
    public Boolean updateECODwgProperties(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateECODwgProperties(ds);
    }
    
    /**
     * ECO-C(EPL) 테이블 리스트
     * 
     * @method selectECOEplList 
     * @date 2013. 2. 20.
     * @param
     * @return List<SYMCBOMEditData>
     * @exception
     * @throws
     * @see
     */
    public List<SYMCBOMEditData> selectECOEplList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECOEplList(ds);
    }

	/*public Boolean insertECOBOMWork(DataSet ds){
	    ECOHistoryDao dao = new ECOHistoryDao();
	    return dao.insertECOBOMWork(ds);
	}*/
    
	public List<String> selectEPLData(DataSet ds) {
	    ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectEPLData(ds);
	}
	
	/**
	 * ECO-C Properties Update
	 * 
	 * @method updateECOEPLProperties 
	 * @date 2013. 3. 5.
	 * @param
	 * @return Boolean
	 * @exception
	 * @throws
	 * @see
	 */
	public Boolean updateECOEPLProperties(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateECOEPLProperties(ds);
    }
	
	/**
	 * ECO-D(PartList) 테이블 리스트
	 * 
	 * @method selectECOPartList 
	 * @date 2013. 3. 8.
	 * @param
	 * @return List<SYMCPartListData>
	 * @exception
	 * @throws
	 * @see
	 */
    public List<SYMCPartListData> selectECOPartList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECOPartList(ds);
    }
	
	/**
	 * ECO-D Properties Update
	 * 
	 * @method updateECOPartListProperties 
	 * @date 2013. 3. 8.
	 * @param
	 * @return Boolean
	 * @exception
	 * @throws
	 * @see
	 */
    public Boolean updateECOPartListProperties(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateECOPartListProperties(ds);
    }
    
    public List<SYMCBOMEditData> selectECOBOMList(DataSet ds) {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECOBOMList(ds);
    }
    
    // ECO Demon
    public List<HashMap<String, String>> selectECODemonTarget() throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectECODemonTarget();
    }
    
    public List<HashMap<String, String>> selectOccurrenceECO(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectOccurrenceECO(ds);
    }
    
    public Integer updateOccurrenceECOApplied(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.updateOccurrenceECOApplied(ds);
    }
    
    public List<HashMap<String, String>> selectReleasedECO(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.selectReleasedECO(ds);
    }

    public Boolean insertECOInfoToVPM(DataSet ds) throws Exception {
        ECOHistoryDao dao = new ECOHistoryDao();
        return dao.insertECOInfoToVPM(ds);
    }

}
