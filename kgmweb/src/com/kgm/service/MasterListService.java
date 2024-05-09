package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.MasterListDao;

/**
 * [SR170707-024][ljg] Product�� Variant ���� ���� ���� �߰�
 * [SR170707-024][ljg] Product�� OSI No�� ���� ���� ���� �߰�
 */
public class MasterListService {
	
	public ArrayList getWorkingCCN(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getWorkingCCN(ds);
	}
	
	public ArrayList getPart(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getPart(ds);
	}
	
	public ArrayList getStoredOptionSet(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getStoredOptionSet(ds);
	}
	
	public ArrayList getEssentialName(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getEssentialName(ds);
	}
	
	public Object getSysGuid(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getSysGuid(ds);
	}
	
	public Object getDwgDeployableDate(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getDwgDeployableDate(ds);
	}
	
    public Object getBVRModifyDate(DataSet ds) throws Exception{
        MasterListDao dao = new MasterListDao();
        return dao.getBVRModifyDate(ds);
    }
    
	/**
	 * TC�� �����ϴ� �̸����� Ȯ��.
	 * 
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public Object getExistPart(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getExistPart(ds);
	}
	
	/**
	 * �ֽ� Total Weight Master List ���
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, String> getLatestWMLMTargetData(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getLatestWMLMTargetData(ds);
	}
	
	/**
	 * �ֽ� Total Weight Master List BOM ���� ��ȸ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getWeightMasterDataList(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getWeightMasterDataList(ds);
	}
	
	/**
	 * BOMLine�� Trim ������ ������
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getBOMLineTrimList(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getBOMLineTrimList(ds);
	}
	
	/**
	 * [20161019][ymjang] BOM Loading �ӵ� ���� (SQL�� �̿��� DB Query ������� ������)
	 * MLM �ε�� �� Line �� Item �� BOMLine �Ӽ��� �����´�.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getMLMLoadProp(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getMLMLoadProp(ds);
	}
	
	/**
	 * [SR170707-024][ljg] Product�� Variant ���� ���� ����
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getVariantList(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getVariantList(ds);
	}
	
	/**
	 * [SR170707-024][ljg] Product�� OSI No�� ���� ���� ����
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String getOSINo(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getOSINo(ds);
	}
	
	/**
	 * [SR170707-024][ljg] �ش� FMP ������ ���������� ��� Part ���� ����
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList getEpl(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getEpl(ds);
	}
	
	/**
	 * [SR170707-024][ljg] �θ� �ٷ� 1���� ������ ��� �ڽĵ��� ������
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 7. 12.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getChildren(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getChildren(ds);
	}
	
	/**
	 * [csh] ��ü ���� �ڽ� ��� ��������(BOM ������ latest working)
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2018. 5. 14.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getAllChildren(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getAllChildren(ds);
	}
	
	/**
	 * [csh] ��ü ���� �ڽ� ��� ��������(BOM ������ latest working)
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2018. 5. 14.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getAllChildrenReleased(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getAllChildrenReleased(ds);
	}
	
}
