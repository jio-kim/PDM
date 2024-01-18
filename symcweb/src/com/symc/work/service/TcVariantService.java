package com.symc.work.service;

import java.util.HashMap;
import java.util.List;

import com.symc.common.dao.TcCommonDao;

public class TcVariantService {

    /**
     * VARIANT_VALUE를 조회하여 I/F 테이블(IF_VARIANT_VALUE)에 등록한다.
     *
     * @method createVariantValue
     * @date 2013. 8. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void  createVariantValue() throws Exception {
        this.createVariantValueInfo(this.getVariantValueList());
    }

    /**
     * IF_VARIANT_VALUE를 조회한다.
     *
     * @method getVariantValueList
     * @date 2013. 8. 7.
     * @param
     * @return List<HashMap<String, String>>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public List<HashMap<String, String>> getVariantValueList() throws Exception {
        TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
        return (List<HashMap<String, String>>) commonDao.selectList("com.symc.tc.variant.getVariantValueList", null);

    }

    /**
     * I/F 테이블(IF_VARIANT_VALUE)에 정보를 등록
     *
     * @method createVariantValueInfo
     * @date 2013. 8. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void createVariantValueInfo(List<HashMap<String, String>> list) throws Exception {
        TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
        commonDao.delete("com.symc.ifpe.deleteVariantValueInfo", null);
        commonDao.insertList("com.symc.ifpe.createVariantValueInfo", list);

    }
}
