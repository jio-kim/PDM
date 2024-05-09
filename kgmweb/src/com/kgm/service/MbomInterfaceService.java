package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.MbomInterfaceDao;

public class MbomInterfaceService {

	/**
	 * BP ID��  BP DATE�� ������Ʈ �ϱ� ����, TC�� IF_MBOM_BPN ���̺� ����Ѵ�.
	 *
	 * @param ds
	 * @throws Exception
	 */
	public int insertBpnInfo(DataSet ds) throws Exception{
		MbomInterfaceDao dao = new MbomInterfaceDao();
		return dao.insertBpnInfo(ds);
	}

	/**
	 * PG_ID�� PG_ID_VERSION�� ������Ʈ�Ѵ�.
	 * TC������ �ǽð����� ���� �׸��� �ƴϹǷ�,
	 * VehPartRevision �Ǵ� StdPartRevision �� ������Ʈ �Ѵ�.
	 *
	 * @param ds
	 * @throws Exception
	 */
	public int updatePgInfo(DataSet ds) throws Exception{
		MbomInterfaceDao dao = new MbomInterfaceDao();
		return dao.updatePgInfo(ds);
	}

    /**
     * TC�� �۾�ǥ�ؼ�(����/����)�� ��ȸ�Ѵ�.
     *
     * @param ds
     * @throws Exception
     */
    public List<HashMap<String, String>> searchProcessSheet(DataSet ds) throws Exception {
        MbomInterfaceDao dao = new MbomInterfaceDao();
        return dao.searchProcessSheet(ds);
    }
}
