package com.ssangyong.dao;

import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.MbomInterfaceMapper;

public class MbomInterfaceDao extends AbstractDao {

	/**
	 * BP_ID와 BP_DATE를 TC에 쌓아 둔다.
	 * 건별로 에러난 내용은 무시함.
	 *
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public int insertBpnInfo(DataSet ds) throws Exception{

		int resultCount = 0;
        try {
            SqlSession session = getSqlSession();
            MbomInterfaceMapper mapper = session.getMapper(MbomInterfaceMapper.class);

            if( ds != null){
    			Object obj = ds.get("data");

    			if ( obj == null ){
    				throw new Exception("could not found data key or value.\nData Type : DataSet<String,HashMap<String, String>> or DataSet<String,List<HashMap<String, String>>>");
    			}

    			if( obj instanceof List){
    				List<HashMap<String, String>> list = (List<HashMap<String, String>>)obj;
    				if( list == null || list.isEmpty()){
    					throw new Exception("could not found data key or value.\nData Type : DataSet<String,HashMap<String, String>> or DataSet<String,List<HashMap<String, String>>>");
    				}

    				for(HashMap<String, String> detailMap:list){
    					try{
    						DataSet bpDs = new DataSet();
    						bpDs.putAll(detailMap);
    			            mapper.insertBpnInfo(bpDs);

    			            resultCount++;
    					}catch( Exception e){
    						e.printStackTrace();
    					}

    				}
    			}else if( obj instanceof HashMap){
    				try{
    					HashMap<String, String> detailMap = (HashMap<String, String>)obj;
    					DataSet bpDs = new DataSet();
    					bpDs.putAll(detailMap);
    					mapper.insertBpnInfo(bpDs);

    					resultCount++;
    				}catch( Exception e){
    					e.printStackTrace();
    				}
    			}
    		}

        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return resultCount;
	}

	/**
	 * PG_ID와 PG_ID_VERSION을 업데이트함.
	 * 하나라도 오류시 롤백.
	 *
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public int updatePgInfo(DataSet ds) throws Exception{
		int resultCount = 0;
		SqlSession session = null;
        try {
            session = getSqlSession();
            session.getConnection().setAutoCommit(false);

            MbomInterfaceMapper mapper = session.getMapper(MbomInterfaceMapper.class);
            Object obj = ds.get("data");
			if ( obj == null ){
				throw new Exception("could not found data key or value.\nData Type : DataSet<String,HashMap<String, String>> or DataSet<String,List<HashMap<String, String>>>");
			}
            if( obj instanceof List){
				List<HashMap<String, String>> list = (List<HashMap<String, String>>)obj;
				if( list == null || list.isEmpty()){
					throw new Exception("could not found data key or value.\nData Type : DataSet<String,HashMap<String, String>> or DataSet<String,List<HashMap<String, String>>>");
				}

				for(HashMap<String, String> detailMap:list){
					DataSet pgDs = new DataSet();
					pgDs.putAll(detailMap);

		            mapper.updatePgInfo(pgDs);
		            resultCount = (Integer)pgDs.get("result_count");
		            if( resultCount < 1)
		            	throw new Exception("Row not updated.");

				}

			}else if( obj instanceof HashMap){

				HashMap<String, String> detailMap = (HashMap<String, String>)obj;
				DataSet pgDs = new DataSet();
				pgDs.putAll(detailMap);

	            mapper.updatePgInfo(pgDs);
	            resultCount = (Integer)pgDs.get("result_count");
	            if( resultCount < 1)
	            	throw new Exception("Row not updated.");

			}
            session.getConnection().commit();


        } catch (Exception e) {
            e.printStackTrace();
            session.getConnection().rollback();
            resultCount = 0;
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return resultCount;
	}

    /**
     * 작업표준서 조회
     *
     * @param ds
     * @return
     * @throws Exception
     */
    public List<HashMap<String, String>> searchProcessSheet(DataSet ds) throws Exception {
        List<HashMap<String, String>> result = null;        
        try{            
            SqlSession session = getSqlSession();
            MbomInterfaceMapper mapper = session.getMapper(MbomInterfaceMapper.class);
            result = mapper.searchProcessSheet(ds);            
        }catch(Exception e){
        	
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
        	
            throw e;
        }finally{
            sqlSessionClose();
        }        
        return result;
    }   
}
