package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.CommonPartCheckMapper;
import com.kgm.dto.ExcludeFromCommonPartInEcoData;
import com.kgm.dto.ExcludeFromNameGroup;

/**
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class PartNameGroupDao extends AbstractDao {
	public void insertPngMaster(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);
            HashMap pngMaster = (HashMap)ds.get("PNG_MASTER");
            ArrayList<String> pngNameList = (ArrayList)ds.get("PNG_NAME_LIST");
            ArrayList<HashMap<String, Object>> pngConditionList = (ArrayList<HashMap<String, Object>>)ds.get("PNG_CONDITION_LIST");
            
            if(pngMaster == null){
            	throw new Exception("Could not find the PNG_MASTER info.");
            }
            
            session.insert("com.kgm.mapper.partnamegroup.insertPngMaster", pngMaster);
            
            ArrayList tmpList = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngNameList", pngMaster);
            if(tmpList!=null && !tmpList.isEmpty()){
            	session.delete("com.kgm.mapper.partnamegroup.deletePngNameList", pngMaster);
            }   
            
            tmpList = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngConditionList", pngMaster);
            if(tmpList!=null && !tmpList.isEmpty()){
            	session.delete("com.kgm.mapper.partnamegroup.deletePngConditionList", pngMaster);
            } 
            
            HashMap map = new HashMap();
            for( String partName : pngNameList){
            	map.clear();
            	map.put("GROUP_ID", pngMaster.get("GROUP_ID"));
            	map.put("PART_NAME", partName);
            	
            	//New Part Name List에서 해당 part명 제거
            	//제거하지 않음.
//            	session.delete("com.kgm.mapper.partnamegroup.deletePngNewNameList", map);
            	
            	session.insert("com.kgm.mapper.partnamegroup.insertPngNameList", map);
            }
            
            for( HashMap<String, Object> map2 : pngConditionList){
//            	if( map2.get("PART_NAME") == null){
//            		map2.put("PART_NAME", "-");
//            	}
            	session.insert("com.kgm.mapper.partnamegroup.insertPngConditionList", map2);
            }
            session.commit();
            
        } catch (Exception e) {
        	session.rollback();
            e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}
	
	public ArrayList getPngMaster(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList masterInfo = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngMaster", ds);
			
			return masterInfo;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public HashMap getPngDetail(DataSet ds) throws Exception{
		try{
			HashMap resultMap = new HashMap();
			SqlSession session = getSqlSession();
			HashMap masterInfo = (HashMap)session.selectOne("com.kgm.mapper.partnamegroup.getPngMaster", ds);
			resultMap.put("PNG_MASTER", masterInfo);
			
			ArrayList pngNamelist = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngNameList", ds);
			resultMap.put("PNG_NAME_LIST", pngNamelist);
			
			ArrayList conditionList = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngConditionList", ds);
			resultMap.put("PNG_CONDITION_LIST", conditionList);
			
			return resultMap;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public void deletePngMaster(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);
            session.delete("com.kgm.mapper.partnamegroup.deletePngNameList", ds);
            
            session.delete("com.kgm.mapper.partnamegroup.deletePngConditionList", ds);
            
            session.delete("com.kgm.mapper.partnamegroup.deletePngMaster", ds);
            
            session.commit();
        } catch (Exception e) {
        	session.rollback();
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}	
	

	
	public ArrayList getPngNameList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngNameList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public ArrayList getPngConditionList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngConditionList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}		
	
	public ArrayList getPngProdOrder(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngProdOrder", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public void deletePngProdOrder(DataSet ds) throws Exception{
		
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.delete("com.kgm.mapper.partnamegroup.deletePngProdOrder", ds);
            
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
            throw e;
        } finally {
            sqlSessionClose();
        }		
	}	
	
	public ArrayList getPngNewNameList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngNewNameList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public ArrayList getProductList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getProductList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public ArrayList getPngAssign(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPngAssign", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public void savePngAssign(DataSet ds) throws Exception{
		
		SqlSession session = null;
		try{
			session = getSqlSession();
			session.getConnection().setAutoCommit(false);
			
			ArrayList<String> productList = (ArrayList<String>)ds.get("PRODUCT_LIST");
			ArrayList<HashMap<String, String>> dataList = (ArrayList<HashMap<String, String>>)ds.get("DATA");
			
			session.delete("com.kgm.mapper.partnamegroup.deletePngProdOrder", null);
			session.delete("com.kgm.mapper.partnamegroup.deletePngAssign", null);
			
			for(int i = 0; productList != null && i < productList.size(); i++){
				HashMap map = new HashMap();
				map.put("ASSIGN_ORDER", "" + i);
				map.put("PRODUCT", productList.get(i));
				session.insert("com.kgm.mapper.partnamegroup.insertPngProdOrder", map);
			}
			
			for(int i = 0; dataList != null && i < dataList.size(); i++){
				HashMap<String, String> map = dataList.get(i);
				String isUse = map.get("IS_USE");
				if( "1".equals(isUse)){
					session.insert("com.kgm.mapper.partnamegroup.insertPngAssign", map);
				}
			}
			
			session.commit();
		}catch(Exception e){
			session.rollback();
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}	
	
	public ArrayList getUserSpecList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getUserSpecList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getBuildSpecList(DataSet ds) throws Exception{
		SqlSession session = null;
		try{
			session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getBuildSpecList", ds);
			
			return list;
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
	}
	
	public ArrayList getPlan15SpecList(DataSet ds) throws Exception{
		SqlSession session = null;
		try{
			session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPlan15SpecList", ds);
			
			return list;
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
	}
	
	public ArrayList getResult30SpecList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getResult30SpecList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	//[SR170810][LJG] 60일 연동 계획 Spec List 추가
	public ArrayList getResult60SpecList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getResult60SpecList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getEndItemNameList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getEndItemNameList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getSpecEndItemNameList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getSpecEndItemNameList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public String getSpec(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			String result = (String)session.selectOne("com.kgm.mapper.partnamegroup.getSpec", ds);
			
			return result;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public String getRowKey(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			String result = (String)session.selectOne("com.kgm.mapper.partnamegroup.getRowKey", ds);
			
			return result;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getUserSpecWithCategory(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getUserSpecWithCategory", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public ArrayList getBuildSpecWithCategory(DataSet ds) throws Exception{
		SqlSession session = null;
		try{
			session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getBuildSpecWithCategory", ds);
			
			return list;
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
	}
	
	public ArrayList getPlanResultSpecWithCategory(DataSet ds) throws Exception{
		SqlSession session = null;
		try{
			session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getPlanResultSpecWithCategory", ds);
			
			return list;
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
	}
	
	public void insertPngNewNameFromECO(DataSet ds) throws Exception{
		try{
			
			SqlSession session = getSqlSession();
			session.update("com.kgm.mapper.partnamegroup.insertPngNewNameFromECO", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public void set1LevelItemList(DataSet ds) throws Exception{
		try{
			
			SqlSession session = getSqlSession();
			session.update("com.kgm.mapper.partnamegroup.set1LevelItemList", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	public void deletePngEpl(DataSet ds) throws Exception{
		try{
			
			SqlSession session = getSqlSession();
			session.delete("com.kgm.mapper.partnamegroup.deletePngEpl", ds);
			
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	/**
	 * 주간 에러 리포트 리스트
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<?> getPngWeeklyErrorReport(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList<?> list = (ArrayList<?>)session.selectList("com.kgm.mapper.partnamegroup.getPngWeeklyErrorReport", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	/**
	 * 주간 에러 리포트의 마지막 실행 날짜
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String getPngWeeklyRepLastDate(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			String result = (String)session.selectOne("com.kgm.mapper.partnamegroup.getPngWeeklyRepLastDate", ds);
			return result;
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	
	/**
	 * 해당하는 빌드 스펙의 옵션 코드 리스트를 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 7.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<?> getOptionCodeList(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList<?> list = (ArrayList<?>)session.selectList("com.kgm.mapper.partnamegroup.getOptionCodeList", ds);
			
			return list;
		}catch(Exception e){
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	/**
	 * 해당하는 스펙의 옵션 코드중 M/Y 옵션 코드 값을 가져옴
	 * @Copyright : Plmsoft
	 * @author : 이정건
	 * @since  : 2017. 4. 7.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getModelYear(DataSet ds) throws Exception{
		try{
			SqlSession session = getSqlSession();
			ArrayList<?> list = (ArrayList<?>)session.selectList("com.kgm.mapper.partnamegroup.getModelYear", ds);
			
			return (ArrayList<String>)list;
		}catch(Exception e){
			e.printStackTrace();
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}
	}
	
	
	/**
	 * Build Spec 옵션 변경 이력 정보 조회
	 * 
	 * @author : 빈라호
	 * @since  : 2018. 4. 17.
	 * @param ds
	 * @return
	 * @throws Exception
	 * 
	 */ 
	public ArrayList getSpecOptionChangeInfo(DataSet ds) throws Exception{
		SqlSession session = null;
		try{
			session = getSqlSession();
			ArrayList list = (ArrayList)session.selectList("com.kgm.mapper.partnamegroup.getSpecOptionChangeInfo", ds);
			
			return list;
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
	}
	
	//[CSH][SR181025-028]
	public void saveExcludePartData ( List<ExcludeFromNameGroup> dataList  ) throws Exception{

		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 for (Iterator iterator = dataList.iterator(); iterator.hasNext();) {

				 ExcludeFromNameGroup excludeFromNameGroup = (ExcludeFromNameGroup) iterator.next();

				 int flag = excludeFromNameGroup.getChangedFlag();

				 if( ExcludeFromNameGroup.ADDED_FLAG == flag ){
					 session.insert("com.kgm.mapper.partnamegroup.insertExcludeData", excludeFromNameGroup);
				 }else if ( ExcludeFromNameGroup.DELETED_FLAG == flag ){
					 session.delete("com.kgm.mapper.partnamegroup.deleteExcludeData", excludeFromNameGroup);
				 }else if ( ExcludeFromNameGroup.CHANGED_FLAG == flag ){
					 session.update("com.kgm.mapper.partnamegroup.updateExcludeData", excludeFromNameGroup);
				 }else{
				 }

			 }

		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), dataList);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
	 }
	
	//[CSH][SR181025-028]
	public List<ExcludeFromNameGroup> getExcludePartData (DataSet ds)  throws Exception{
		 List<ExcludeFromNameGroup> result = null;
		 try {
			 SqlSession session = getSqlSession();
			 result = session.selectList("com.kgm.mapper.partnamegroup.getExcludePartData", ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
		 return result;
	 }
	
}
