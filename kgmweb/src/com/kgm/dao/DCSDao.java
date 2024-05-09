package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.common.util.StringUtil;
import com.kgm.mapper.DCSMapper;

/**
 * [20160606][ymjang] ���� �߼� ��� ���� (through EAI)
 * [20160928][ymjang] log4j�� ���� ���� �α� ���
 * [20161201][ymjang] commit/rollback ���� ��ġ ����
 */
public class DCSDao extends AbstractDao {

	public DCSDao() {

	}

	public boolean updateDCSStatus(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			mapper.updateDCSStatus(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}

	public boolean refreshTCObject(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			mapper.refreshTCObject(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}

	public boolean refreshTCTimeStamp(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			mapper.refreshTCTimeStamp(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}

	/**
	 * SYMC ��Ʈ����� ���� ���� �߼�
	 */
	public boolean sendMail(DataSet dataSet) {
		SqlSession sqlSession = null;

		try {
			sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			
			// E-Mail �߼� ���� Logging
// 			Logger logger = Logger.getLogger(DCSDao.class);
// 			logger.info("\n############ SEND MAIL START ############");
// 			logger.info("\n# the_sysid : " + dataSet.get("the_sysid"));
// 			logger.info("\n# the_sabun : " + dataSet.get("the_sabun"));
// 			logger.info("\n# the_title : " + dataSet.get("the_title"));
// 			logger.info("\n# the_remark : " + dataSet.get("the_remark"));
// 			logger.info("\n# the_tsabun : " + dataSet.get("the_tsabun"));
// 			logger.info("\n############ SEND MAIL END   ############");
 			
			mapper.sendMail(dataSet);
			
			sqlSession.commit();
			
		} catch (Exception e) {
			e.printStackTrace();
			
			sqlSession.rollback();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), dataSet);
			
		} finally {
			sqlSessionClose();
		}

		return true;
	}

    /**
     * SYMC ��Ʈ����� ���� ���� �߼� (through EAI)
     * @param ds #{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun}
     * @return
     */
    public boolean sendMailEai(DataSet ds){
        SqlSession sqlSession = null;
        try{
            sqlSession = getSqlSession();
            DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
            
            // [NON-SR][2017.02.13] taeku.jeong
            // java.sql.SQLException: ORA-01461: LONG ���� LONG ���� ������ ���� ���ε��� �� �ֽ��ϴ�.
            // SUBSTR(#{the_remark,jdbcType=VARCHAR,mode=IN}, 1, 4000) �� ��� �߻� �� �� �ִ� Error�� �Ǵ�
            // Data�� SQL �����ϸ鼭 Size�� ���߱⺸�� ������ Size�� �´� Data�� ����� ��������...
            String title = (String)ds.get("the_title");
            String remark = (String)ds.get("the_remark");
            String toUsers = (String)ds.get("the_tsabun");
            
            title = StringUtil.getByteSizedStr(title, "UTF-8", 4000);
            remark = StringUtil.getByteSizedStr(remark, "UTF-8", 4000);
            toUsers = StringUtil.getByteSizedStr(toUsers, "UTF-8", 4000);		
            	
            ds.put("the_title", title);
            ds.put("the_remark", remark);
            ds.put("the_tsabun", toUsers);
            		
//            // E-Mail �߼� ���� Logging
// 			Logger logger = Logger.getLogger(DCSDao.class);
// 			logger.info("\n############ SEND MAIL START ############");
// 			logger.info("\n# the_sysid : " + ds.get("the_sysid"));
// 			logger.info("\n# the_sabun : " + ds.get("the_sabun"));
// 			logger.info("\n# the_title : " + ds.get("the_title"));
// 			logger.info("\n# the_remark : " + ds.get("the_remark"));
// 			logger.info("\n# the_tsabun : " + ds.get("the_tsabun"));
// 			logger.info("\n############ SEND MAIL END   ############");
         			
            mapper.sendMailEai(ds);
            
            sqlSession.commit();
            
        }catch(Exception e){
            e.printStackTrace();
            
            sqlSession.rollback();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        }finally{
            sqlSessionClose();
        }
        return true;
    }
	
	public ArrayList<HashMap<String, Object>> getDCSList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getDCSList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	public ArrayList<HashMap<String, Object>> getPSCList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getPSCList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	public int getDCSInWorkCount(DataSet ds){
		int cnt = 0;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			cnt = mapper.getDCSInWorkCount(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return cnt;
	}

	public int getPSCInWorkCount(DataSet ds){
		int cnt = 0;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			cnt = mapper.getPSCInWorkCount(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return cnt;
	}
	
	public ArrayList<HashMap<String, Object>> getStandbyList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getStandbyList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	public ArrayList<HashMap<String, Object>> getSignOffbyTaskList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getSignOffbyTaskList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getProcessingList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getProcessingList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getConsultationDelayList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getConsultationDelayList(ds);
		} catch(Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}

	public ArrayList<HashMap<String, Object>> getMyDCSList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getMyDCSList(ds);
		} catch(Exception e) {
			e.printStackTrace();
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
	
	public ArrayList<HashMap<String, Object>> getMyPSCList(DataSet ds){
		ArrayList<HashMap<String, Object>> resultList = null;
		try {
			SqlSession sqlSession = getSqlSession();
			DCSMapper mapper = sqlSession.getMapper(DCSMapper.class);
			resultList = mapper.getMyPSCList(ds);
		} catch(Exception e) {
			e.printStackTrace();

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
		} finally {
			sqlSessionClose();
		}
		
		return resultList;
	}
}
