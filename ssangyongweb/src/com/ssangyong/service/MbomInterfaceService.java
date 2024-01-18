package com.ssangyong.service;

import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.MbomInterfaceDao;

public class MbomInterfaceService {

	/**
	 * BP ID와  BP DATE를 업데이트 하기 위해, TC의 IF_MBOM_BPN 테이블에 등록한다.
	 *
	 * @param ds
	 * @throws Exception
	 */
	public int insertBpnInfo(DataSet ds) throws Exception{
		MbomInterfaceDao dao = new MbomInterfaceDao();
		return dao.insertBpnInfo(ds);
	}

	/**
	 * PG_ID와 PG_ID_VERSION을 업데이트한다.
	 * TC에서는 실시간으로 보는 항목이 아니므로,
	 * VehPartRevision 또는 StdPartRevision 만 업데이트 한다.
	 *
	 * @param ds
	 * @throws Exception
	 */
	public int updatePgInfo(DataSet ds) throws Exception{
		MbomInterfaceDao dao = new MbomInterfaceDao();
		return dao.updatePgInfo(ds);
	}

    /**
     * TC의 작업표준서(국문/영문)을 조회한다.
     *
     * @param ds
     * @throws Exception
     */
    public List<HashMap<String, String>> searchProcessSheet(DataSet ds) throws Exception {
        MbomInterfaceDao dao = new MbomInterfaceDao();
        return dao.searchProcessSheet(ds);
    }
}
