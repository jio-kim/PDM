package com.ssangyong.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.dao.MasterListDao;

/**
 * [SR170707-024][ljg] Product의 Variant 가져 오는 쿼리 추가
 * [SR170707-024][ljg] Product의 OSI No를 가져 오는 쿼리 추가
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
	 * TC에 존재하는 이름인지 확인.
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
	 * 최신 Total Weight Master List 대상
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, String> getLatestWMLMTargetData(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getLatestWMLMTargetData(ds);
	}
	
	/**
	 * 최신 Total Weight Master List BOM 정보 조회
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getWeightMasterDataList(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getWeightMasterDataList(ds);
	}
	
	/**
	 * BOMLine의 Trim 정보를 가져옴
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getBOMLineTrimList(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getBOMLineTrimList(ds);
	}
	
	/**
	 * [20161019][ymjang] BOM Loading 속도 개선 (SQL을 이용한 DB Query 방식으로 변경함)
	 * MLM 로드시 각 Line 별 Item 및 BOMLine 속성을 가져온다.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getMLMLoadProp(DataSet ds) throws Exception{
		MasterListDao dao = new MasterListDao();
		return dao.getMLMLoadProp(ds);
	}
	
	/**
	 * [SR170707-024][ljg] Product의 Variant 가져 오는 쿼리
	 * @Copyright : Plmsoft
	 * @author : 이정건
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
	 * [SR170707-024][ljg] Product의 OSI No를 가져 오는 쿼리
	 * @Copyright : Plmsoft
	 * @author : 이정건
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
	 * [SR170707-024][ljg] 해당 FMP 하위의 최하위까지 모든 Part 가져 오기
	 * @Copyright : Plmsoft
	 * @author : 이정건
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
	 * [SR170707-024][ljg] 부모 바로 1레벨 하위의 모든 자식들을 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
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
	 * [csh] 전체 하위 자식 노드 가져오기(BOM 정전개 latest working)
	 * @Copyright : Plmsoft
	 * @author : 조석훈
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
	 * [csh] 전체 하위 자식 노드 가져오기(BOM 정전개 latest working)
	 * @Copyright : Plmsoft
	 * @author : 조석훈
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
