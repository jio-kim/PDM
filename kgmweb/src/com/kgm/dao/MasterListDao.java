package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;

/**
 * [SR170707-024][ljg] Product�� OSI No�� ���� ���� ���� �߰�
 * [SR170707-024][ljg] Product�� Variant ���� ���� ���� �߰�
 */
public class MasterListDao extends AbstractDao {

	public ArrayList getWorkingCCN(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList workingCCNs = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectWorkingCCN", ds);

			return workingCCNs;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public ArrayList getPart(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList parts = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectPart", ds);

			return parts;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public ArrayList getStoredOptionSet(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList optionSet = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectStoredOptionSet", ds);

			return optionSet;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public ArrayList getEssentialName(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.masterlist.selectEssentialName", ds);

			return list;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	public Object getSysGuid(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectSysGuid", ds);

			return obj;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	} 

	public Object getDwgDeployableDate(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectDwgDeployableDate", ds);

			return obj;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	} 

	public Object getBVRModifyDate(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectBVRModifyDate", ds);

			return obj;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	} 

	public Object getExistPart(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			Object obj = session.selectOne("com.kgm.mapper.masterlist.selectExistPart", ds);

			return obj;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}

	/**
	 * �ֽ� Total Weight Master List ��� ������ ������
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public HashMap<String, String> getLatestWMLMTargetData(DataSet ds) throws Exception {
		HashMap<String, String> result = null;        
		try{            
			SqlSession session = getSqlSession();
			result = session.selectOne("com.kgm.mapper.masterlist.getLatestWMLMTargetData", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
	}

	/**
	 * �ֽ� Total Weight Master List BOM ���� ��ȸ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getWeightMasterDataList(DataSet ds) throws Exception {
		List<HashMap<String, Object>> result = null;        
		try{            
			SqlSession session = getSqlSession();
			result = session.selectList("com.kgm.mapper.masterlist.getWeightMasterDataList", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
	}

	/**
	 * BOMLine�� Trim ������ ������
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, Object>> getBOMLineTrimList(DataSet ds) throws Exception {
		List<HashMap<String, Object>> result = null;     
		try{            
			SqlSession session = getSqlSession();
			result = session.selectList("com.kgm.mapper.masterlist.getBOMLineTrimList", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
	}

	/**
	 * [20161019][ymjang] BOM Loading �ӵ� ���� (SQL�� �̿��� DB Query ������� ������)
	 * MLM �ε�� �� Line �� Item �� BOMLine �Ӽ��� �����´�.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getMLMLoadProp(DataSet ds) throws Exception {
		List<Map<String, Object>> result = null;     
		try{            
			SqlSession session = getSqlSession();
			result = session.selectList("com.kgm.mapper.masterlist.getMLMLoadProp", ds);
		}catch(Exception e){
			throw e;
		}finally{
			sqlSessionClose();
		}        
		return result;
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
	public ArrayList getVariantList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList varialtList = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getVariantList", ds);

			return varialtList;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
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
		try{
			SqlSession session = getSqlSession();
			String osi_no = (String)session.selectOne("com.kgm.mapper.masterlist.getOSINo", ds);

			return osi_no;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
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
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getEpl", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
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
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getChildren", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
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
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getAllChildren", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	/**
	 * [csh] ��ü ���� �ڽ� ��� ��������(BOM ������ latest Released)
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2018. 5. 14.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getAllChildrenReleased(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList epl = (ArrayList)session.selectList("com.kgm.mapper.masterlist.getAllChildrenReleased", ds);

			return epl;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);

			throw e;
		}finally{
			sqlSessionClose();
		}
	}
}
