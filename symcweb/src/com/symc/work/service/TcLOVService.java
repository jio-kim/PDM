package com.symc.work.service;

import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;
import com.symc.work.model.KeyLOV;

public class TcLOVService {

    /**
     * TC KEY_LOV를 조회하여 I/F 테이블(IF_PE_ENV_VALUES)에 등록한다.
     *
     * @method createEnvValues
     * @date 2013. 8. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void  createEnvValues(String id) throws Exception {
        this.createEnvValuesInfo(id, this.getKeyLOVList(id));
    }

    /**
     * KEY_LOV ID를 가지고 VALUE를 조회한다.
     *
     * @method getKeyLOVList
     * @date 2013. 8. 7.
     * @param
     * @return List<KeyLOV>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public List<KeyLOV> getKeyLOVList(String id) throws Exception {
        TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
        HashMap<String,String> param = new HashMap<String,String>();
        param.put("id", id);
        return (List<KeyLOV>) commonDao.selectList("com.symc.tc.lov.getLOVList", param);

    }

    /**
     * KEY_LOV ID를 가지고 VALUE, DESCRIPTION를 조회한다.
     *
     * @method getKeyLOVList
     * @date 2013. 8. 7.
     * @param
     * @return List<KeyLOV>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public List<KeyLOV> getLOVValueList(String lovID) throws Exception {
        TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
        HashMap<String,String> param = new HashMap<String,String>();
        param.put("id", lovID);
        return (List<KeyLOV>) commonDao.selectList("com.symc.tc.lov.getLOVValues", param);

    }

    /**
     * I/F 테이블에 KEY_LOV VALUE정보를 등록
     *
     * @method createEnvValuesInfo
     * @date 2013. 8. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createEnvValuesInfo(String id, List<KeyLOV> lovList) throws Exception {
        TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
        HashMap<String, String> deleteMap = new HashMap<String, String>();
        deleteMap.put("envName", id);
        commonDao.delete("com.symc.ifpe.deleteEnvValuesInfo", deleteMap);
        commonDao.insertList("com.symc.ifpe.createEnvValuesInfo", lovList);

    }
}
