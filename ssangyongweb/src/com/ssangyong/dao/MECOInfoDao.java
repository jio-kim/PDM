package com.ssangyong.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.dto.ApprovalLineData;
import com.ssangyong.mapper.SYMCECOMapper;
import com.ssangyong.mapper.SYMCMECOMapper;
import com.ssangyong.rac.kernel.SYMCBOMEditData;
import com.ssangyong.rac.kernel.SYMCBOPEditData;

/**
 * [SR140828-015][20140829] shcho, Migration 후 MECO ID 채번을 601 부터 할 수 있도록 변경. (2015년에는 다시 001부터 채번 할 수 있도록 Preference를 이용한 초기값 설정 적용)
 * [20160928][ymjang] log4j에 의한 에러 로그 기록
 */
public class MECOInfoDao extends AbstractDao {

//	public MECOInfoDao() {
//
//	}

    // [SR140828-015][20140829] shcho, Migration 후 MECO ID 채번을 601 부터 할 수 있도록 변경. (2015년에는 다시 001부터 채번 할 수 있도록 Preference를 이용한 초기값 설정 적용)
	public String getNextMECOSerial(DataSet ds){
		String nextID = null;
		try{
			SqlSession sqlSession = getSqlSession();

			//String prefix = (String) ds.getObject("prefix");

			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			nextID = mapper.getNextMECOSerial(ds);

		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return nextID;
	}

	public ArrayList<ApprovalLineData> loadApprovalLine(DataSet ds){
		ArrayList<ApprovalLineData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			resultList = mapper.loadApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}

	public ArrayList<ApprovalLineData> loadSavedUserApprovalLine(DataSet ds){
		ArrayList<ApprovalLineData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			resultList = mapper.loadSavedUserApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}

	@SuppressWarnings("unchecked")
	public boolean saveApprovalLine(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();

			ArrayList<ApprovalLineData> paramMapList = (ArrayList<ApprovalLineData>) ds.getObject("data");

			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			for(ApprovalLineData map : paramMapList){
				mapper.saveApprovalLine(map);
			}
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean saveUserApprovalLine(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			ArrayList<ApprovalLineData> paramMapList = (ArrayList<ApprovalLineData>) ds.getObject("data");
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			for(ApprovalLineData map : paramMapList){
				mapper.saveUserApprovalLine(map);
			}
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public ArrayList<ApprovalLineData> getApprovalLine(DataSet ds){
		ArrayList<ApprovalLineData> resultList = null;
		try{
			SqlSession sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			resultList = mapper.getApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return resultList;
	}

	public boolean removeApprovalLine(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			ApprovalLineData paramMap = (ApprovalLineData) ds.getObject("data");
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			mapper.removeApprovalLine(paramMap);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public boolean insertMECOEPL(DataSet ds) {
		SqlSession sqlSession = null;
		try {
			sqlSession = getSqlSession();

			ArrayList<SYMCBOPEditData> paramMapList = (ArrayList<SYMCBOPEditData>)ds.getObject("data");

			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			for(SYMCBOPEditData map : paramMapList) {
				mapper.insertMECOEPL(map);
			}
		}catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally {
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	public ArrayList<HashMap<String, String>> checkModifiedMEPL(DataSet ds) {
		ArrayList<HashMap<String, String>> resultList = null;
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCMECOMapper mapper= sqlSession.getMapper(SYMCMECOMapper.class);

			resultList = mapper.checkModifiedMEPL(ds);

		}catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}

		return resultList;
	}

	public void truncateModifiedEPL(DataSet ds) {
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCMECOMapper mapper= sqlSession.getMapper(SYMCMECOMapper.class);
			mapper.truncateModifiedMEPL(ds);

		}catch(Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
	}

	public ArrayList<SYMCBOPEditData> selectMECOEplList(DataSet ds) {
		SqlSession sqlSession = null;
		ArrayList<SYMCBOPEditData> arrlist = null;
		try{
			sqlSession = getSqlSession();
			SYMCMECOMapper mapper= sqlSession.getMapper(SYMCMECOMapper.class);
			arrlist = mapper.selectMECOEplList(ds);

		}catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return arrlist;
	}

	public int deleteMECOEPL (DataSet ds) {
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			return mapper.deleteMECOEPL(ds);
		}catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return 0;

	}

	public ArrayList<SYMCBOMEditData> searchECOEplList(DataSet ds) {

		SqlSession sqlSession = null;
		ArrayList<SYMCBOMEditData> arrlist = null;
		try{
			sqlSession = getSqlSession();
			SYMCMECOMapper mapper= sqlSession.getMapper(SYMCMECOMapper.class);
			arrlist = mapper.searchECOEplList(ds);

		}catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSessionClose();
		}
		return arrlist;
	}

	public ArrayList<HashMap<String, String>> getEndItemMECONoForProcessSheet(DataSet ds) {
	    ArrayList<HashMap<String, String>> resultList = null;
        try{
            SqlSession sqlSession = getSqlSession();
            SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
            resultList = mapper.getEndItemMECONoForProcessSheet(ds);
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }
        return resultList;
	}

    public ArrayList<HashMap<String, String>> getSubsidiaryMECONoForProcessSheet(DataSet ds) {
        ArrayList<HashMap<String, String>> resultList = null;
        try{
            SqlSession sqlSession = getSqlSession();
            SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
            resultList = mapper.getSubsidiaryMECONoForProcessSheet(ds);
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }
        return resultList;
    }

    public ArrayList<HashMap<String, String>> getResourceMECONoForProcessSheet(DataSet ds) {
        ArrayList<HashMap<String, String>> resultList = null;
        try{
            SqlSession sqlSession = getSqlSession();
            SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
            resultList = mapper.getResourceMECONoForProcessSheet(ds);
        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }
        return resultList;
    }
    
    // 수정자 : bc.kim
    // 이종화 차장님 요청 
    // 작업표준서 Preview 에서 공법 조회시 Resource 항목에 속성값이 변경( No Revise ) 되었을때 
    // 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
    public ArrayList<HashMap<String, String>> getSymbomResourceMecoNo(DataSet ds) {
    	ArrayList<HashMap<String, String>> resultList = null;
    	try{
    		SqlSession sqlSession = getSqlSession();
    		SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
    		resultList = mapper.getSymbomResourceMecoNo(ds);
    	}catch(Exception e){
    		e.printStackTrace();
    		
    		// [20160928][ymjang] log4j에 의한 에러 로그 기록
    		LogUtil.error(e.getMessage(), ds);
    		
    	}finally{
    		sqlSessionClose();
    	}
    	return resultList;
    }
    
    
    // 수정자 : bc.kim
    // 이종화 차장님 요청  SR: [SR190131-060]
    // 작업표준서 Preview 에서 공법 조회시 Resource 항목에 속성값이 변경( No Revise ) 되었을때 
    // 해당 Resource 의 기호가 MECO 의 기호에 맞게 변경 되는 로직 추가
    public ArrayList<HashMap<String, String>> getSymbomSubsidiaryMecoNo(DataSet ds) {
    	ArrayList<HashMap<String, String>> resultList = null;
    	try{
    		SqlSession sqlSession = getSqlSession();
    		SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
    		resultList = mapper.getSymbomSubsidiaryMecoNo(ds);
    	}catch(Exception e){
    		e.printStackTrace();
    		
    		// [20160928][ymjang] log4j에 의한 에러 로그 기록
    		LogUtil.error(e.getMessage(), ds);
    		
    	}finally{
    		sqlSessionClose();
    	}
    	return resultList;
    }

    public Date getLastEPLLoadDate(DataSet ds) {
        //System.out.println("getLastEPLLoadDate()");
        Date lastEPLLoadDate = null;
        try {
            SqlSession sqlSession = getSqlSession();
            SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
            lastEPLLoadDate = mapper.getLastEPLLoadDate(ds);
            //System.out.println("LatestEplLoadDate : " + lastEPLLoadDate);
        } catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally{
            sqlSessionClose();
        }

        return lastEPLLoadDate;
    }

    public ArrayList<HashMap<String, String>> getEndItemListOnFunction (DataSet ds) {
    	ArrayList<HashMap<String, String>> resultList = null;
    	try{
    		SqlSession sqlSession = getSqlSession();
    		SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
    		resultList = mapper.getEndItemListOnFunction(ds);

        }catch(Exception e){
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }
        return resultList;
    }

    public String checkExistMEPL(DataSet ds) {

        SqlSession session = getSqlSession();
        SYMCMECOMapper mapper = session.getMapper(SYMCMECOMapper.class);
        String result = mapper.checkExistMEPL(ds);
        sqlSessionClose();
        if("T".equals(result)) {
            return Boolean.TRUE.toString();
        }else{
        	return Boolean.FALSE.toString();
        }
       
        
    }

	public boolean refreshTCTimeStamp(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			mapper.refreshTCTimeStamp(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}

	
	public boolean updateMEcoStatus(DataSet ds){
		SqlSession sqlSession = null;
		boolean reuslt = false;
		try{
			sqlSession = getSqlSession();
			sqlSession.getConnection().setAutoCommit(false);
			SYMCMECOMapper mapper = sqlSession.getMapper(SYMCMECOMapper.class);
			int rslt = mapper.updateMEcoStatus(ds);
			if(rslt > 0) {
				reuslt = true;
			}
//			mapper.refreshTCObject(ds);
//			mapper.refreshTCTimeStamp(ds);
		}catch(Exception e){
			e.printStackTrace();
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
            try {
            	sqlSession.getConnection().commit();
            	sqlSession.getConnection().setAutoCommit(true);
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			sqlSessionClose();
		}
		return reuslt;
	}
	
	/**
	 * SYMC 인트라넷을 통한 메일 발송
	 * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
	 * @return
	 */
	public boolean sendMail(DataSet ds){
		SqlSession sqlSession = null;
		try{
			sqlSession = getSqlSession();
			SYMCECOMapper mapper = sqlSession.getMapper(SYMCECOMapper.class);	
			
			// E-Mail 발송 내용 Logging
// 			Logger logger = Logger.getLogger(ECOInfoDao.class);
// 			logger.info("\n############ SEND MAIL START ############");
// 			logger.info("\n# the_sysid : " + ds.get("the_sysid"));
// 			logger.info("\n# the_sabun : " + ds.get("the_sabun"));
// 			logger.info("\n# the_title : " + ds.get("the_title"));
// 			logger.info("\n# the_remark : " + ds.get("the_remark"));
// 			logger.info("\n# the_tsabun : " + ds.get("the_tsabun"));
// 			logger.info("\n############ SEND MAIL END   ############");
 			
			mapper.sendMail(ds);
		}catch(Exception e){
			e.printStackTrace();
			
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
			
		}finally{
			sqlSession.commit();
			sqlSessionClose();
		}
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public List<HashMap> getChangedNewItemIdList(DataSet dataSet) {
		 
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            SYMCMECOMapper mapper = session.getMapper(SYMCMECOMapper.class);
            result = mapper.getChangedNewItemIdList(dataSet);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), dataSet);
            
        } finally {
            sqlSessionClose();
        }
        return result;
	}
	
    

}
