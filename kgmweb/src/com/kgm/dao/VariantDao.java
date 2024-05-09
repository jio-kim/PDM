package com.kgm.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.VariantMapper;

/**
 * [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
 * [NON-SR] [20160825] [ymjang] 동일한 Vaiant 에 각 옵션별로 업로드시간이 조금씩 틀려서 HBOM에서 최신 O/Spec 정보를 읽을 때 문제 발행.
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class VariantDao extends AbstractDao {
	public HashMap getItem(DataSet ds) throws Exception{
		
		HashMap map = null;
		try{
			SqlSession session = getSqlSession();
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			map = mapper.getItem(ds);
//			map = (HashMap)session.selectOne("com.kgm.mapper.VariantMapper.getItem", ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return map;
	}
	
	public int insertVariantValueDesc(DataSet ds) throws Exception{
		int result = -1;
		
		try{
			
			SqlSession session = getSqlSession();
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.insertVariantValueDesc(ds);
//			result = session.insert("com.kgm.mapper.VariantMapper.insertVariantValueDesc", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public int updateVariantValueDesc(DataSet ds) throws Exception{
		int result = -1;
		
		try{
			
			SqlSession session = getSqlSession();
//			result = session.update("com.kgm.mapper.VariantMapper.updateVariantValueDesc", ds);
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.updateVariantValueDesc(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public List getVariantValueDesc(DataSet ds) throws Exception{
		List result = null;
		
		try{
			
			SqlSession session = getSqlSession();
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getVariantValueDesc(ds);
//			result = session.selectList("com.kgm.mapper.VariantMapper.getVariantValueDesc", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public int getUsedCount(DataSet ds) throws Exception{
		
		Integer result = null;
		try{
			
			SqlSession session = getSqlSession();
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getUsedCount(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public List getUsedOptions(DataSet ds) throws Exception{
		List result = null;
		
		try{
			
			SqlSession session = getSqlSession();
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getUsedOptions(ds);
//			result = session.selectList("com.kgm.mapper.VariantMapper.getVariantValueDesc", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}

	public int insertOsiInfo(DataSet ds) throws Exception{
		
		int result = -1;
		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);

			VariantMapper mapper = session.getMapper(VariantMapper.class);
			
			String osiNo = (String)ds.get("OSI_NO");
			String productNo = (String)ds.get("PRODUCT_NO");
			String creDate = (String)ds.get("CRE_DATE");
			String updDate = (String)ds.get("UPD_DATE");			
			HashMap<String, ArrayList<String>> osiSpec = (HashMap<String, ArrayList<String>>)ds.get("OSI_SPEC");
			Set oKeys = osiSpec.keySet();
			Iterator<String> oIts = oKeys.iterator();
			DataSet ds2 = new DataSet();
			while(oIts.hasNext()){
				String key = oIts.next();
				ArrayList<String> values = osiSpec.get(key);
				
				ds2.clear();
				ds2.put("OSI_NO", osiNo);
				ds2.put("PRODUCT_NO", productNo);
				ds2.put("CRE_DATE", creDate);
				ds2.put("UPD_DATE", updDate);
				ds2.put("VARIANT_NO", key);
				mapper.deleteOsiInfo(ds2);
				
				// [NON-SR] [20160825] [ymjang] 동일한 Vaiant 에 각 옵션별로 업로드시간이 조금씩 틀려서 HBOM에서 최신 O/Spec 정보를 읽을 때 문제 발행.
				for( String value : values){
					ds2.clear();
					ds2.put("OSI_NO", osiNo);
					ds2.put("PRODUCT_NO", productNo);
					ds2.put("CRE_DATE", creDate);
					ds2.put("UPD_DATE", updDate);
					ds2.put("VARIANT_NO", key);
					ds2.put("OPTION_VAL", value);
					result = mapper.insertOsiInfo(ds2);
				}
			}

			// [NON-SR] [20150609] [ymjang] Base Spec IF 용 데이터 생성을 위한 Map 생성
			HashMap<String, ArrayList<String>> baseSpec = (HashMap<String, ArrayList<String>>)ds.get("BASE_SPEC");
			Set bKeys = baseSpec.keySet();
			Iterator<String> bIts = bKeys.iterator();
			ds2 = new DataSet();
			while(bIts.hasNext()){
				String key = bIts.next();
				ArrayList<String> values = osiSpec.get(key);
				
				ds2.clear();
				ds2.put("OSI_NO", osiNo);
				ds2.put("PRODUCT_NO", productNo);
				ds2.put("CRE_DATE", creDate);
				ds2.put("UPD_DATE", updDate);
				ds2.put("VARIANT_NO", key);
				mapper.deleteBSpecInfo(ds2);
				
				for( String value : values){
					ds2.clear();
					ds2.put("OSI_NO", osiNo);
					ds2.put("PRODUCT_NO", productNo);
					ds2.put("CRE_DATE", creDate);
					ds2.put("UPD_DATE", updDate);
					ds2.put("VARIANT_NO", key);
					ds2.put("OPTION_VAL", value);
					result = mapper.insertBSpecInfo(ds2);
				}
			}
			
			session.getConnection().commit();
			
		}catch(Exception e){
			try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result; 
	}
	
	public List getLocalBuildSpecList(DataSet ds) throws Exception{
		List result = null;
		SqlSession session = getSqlSession();
		try{
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getLocalBuildSpecList(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public synchronized List getBuildSpecList(DataSet ds) throws Exception{
		List result = null;
		
		SqlSession session = getSqlSession();
		try{
//			Bulid Spec 가져오고, TC 에 정보를 저장함.			
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getBuildSpecList(ds);

			session.getConnection().setAutoCommit(false);

			deleteBuildSpecList(ds, session);
			
			for( int i = 0; result != null && i < result.size(); i++){
				HashMap row = (HashMap)result.get(i);
				ds.put("SPEC_NO", row.get("SPEC_NO"));
				ds.put("DESCRIPTION", row.get("DESCRIPTION"));
				insertBuidSpecList(ds, session);
			}						
		}catch(Exception e){
			session.getConnection().rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			session.getConnection().commit();
			sqlSessionClose();
		}
		
		return result;
	}
	
	public int deleteBuildSpecList(DataSet ds, SqlSession session) throws Exception{
		int result = -1;
		try{
			
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.deleteBuildSpecList(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
//			외부 세션은 Close하지 않음.
		}
		return result;
	}
	
	public int insertBuidSpecList(DataSet ds, SqlSession session) throws Exception{
		int result = -1;
		try{
			
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.insertBuidSpecList(ds);
			
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
//			외부 세션은 Close하지 않음.
		}
		return result;
	}
	
	public List getBuildSpecInfo(DataSet ds) throws Exception{
		List<HashMap> result = null;
		ArrayList<HashMap> newResult = new ArrayList<HashMap>();
		SqlSession session = getSqlSession();
		try{

			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getBuildSpecInfo(ds);
			
			//교육에서는 Category가 숫자로 되어 있는 Option은 Skip.
			//운영에서는 Category가 숫자로 되어 있는 경우는 없다.
			if( result != null ){
				for( HashMap map : result){
					String obj = (String)map.get("CATE_NO");
					try{
						Integer.parseInt(obj);
						
					}catch( Exception e){
						newResult.add(map);
					}
				}
			}
			
		}catch(Exception e){
			session.rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			session.commit();
			sqlSessionClose();
		}
		
		return newResult;
	}
	
	public String getNewId(DataSet ds) throws Exception{
		
		String newId = null;
		SqlSession session = getSqlSession();
		try{
			
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			newId = mapper.getNewId(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return newId;
	}
	
	/*public String getNextId(DataSet ds) throws Exception{
		
		String newId = null;
		SqlSession session = getSqlSession();
		try{
			
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			newId = mapper.getNextId(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return newId;
	}*/
	
	public List getProjectCodes() throws Exception{
		List result = null;
		
		try{
			
			SqlSession session = getSqlSession();
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getProjectCodes();
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage());
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public List getValidationInfoList(DataSet ds) throws Exception{
		List result = null;
		SqlSession session = getSqlSession();
		try{
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getValidationInfoList(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public int insertValidationInfo(DataSet ds) throws Exception{
		
		int result = -1;
		SqlSession session = getSqlSession();
		try{

			VariantMapper mapper = session.getMapper(VariantMapper.class);
			mapper.insertValidationInfo(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result; 
	}
	
	public int deleteValidationInfo(DataSet ds) throws Exception{
		int result = -1;
		
		SqlSession session = getSqlSession();
		try{
			
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.deleteValidationInfo(ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		return result;
	}
	
	public List getSpecOptions(DataSet ds) throws Exception{
		List result = null;
		SqlSession session = getSqlSession();
		try{
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getSpecOptions(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	public List getMinusInfo(DataSet ds) throws Exception{
		List result = null;
		SqlSession session = getSqlSession();
		try{
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.getMinusInfo(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
	
	/**
	 * OSpec Trim Select
	 * @param ds
	 * @return
	 * @throws Exception
	 */
    public List<HashMap<String, String>> selectOSpecTrim(DataSet ds) throws Exception {
    	List<HashMap<String, String>> result = null;        
    	try {
    		SqlSession session = getSqlSession();
    		VariantMapper mapper = session.getMapper(VariantMapper.class);
    		result = mapper.selectOSpecTrim(ds);
    	} catch(Exception e) {
    		
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
    		
    		throw e;
    	} finally {
    		sqlSessionClose();
    	}        
    	return result;
    }
    
    /**
     * OSpec Trim Delete
     * @param ds
     * @return
     */
    public boolean deleteOSpecTrim(DataSet ds){
		boolean resultList = false;
		try{
			SqlSession sqlSession = getSqlSession();
			VariantMapper mapper = sqlSession.getMapper(VariantMapper.class);	
			mapper.deleteOSpecTrim(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
			resultList = true;
		}
		return resultList;
	}
    
	/**
	 * OSpec Trim Insert
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public int insertOSpecTrim(DataSet ds) throws Exception{
		int result = -1;
		
		try{
			SqlSession session = getSqlSession();
			VariantMapper mapper = session.getMapper(VariantMapper.class);
			result = mapper.insertOSpecTrim(ds);
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
		
		return result;
	}
}
