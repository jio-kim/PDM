package com.kgm.dao;

import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.WeldPointMapper;

/**
 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
 * [SR150714-022][20150907][ymjang] ������ ����(CATIA Feature Name) �߰��� ���� �� BOP �÷� ���� ��û
 */
public class WeldPointDao extends AbstractDao {

	public void updateDateReleasedWithEco(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            mapper.updateDateReleasedWithEco(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}

	public void insertWeldPointGroupInfo(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            mapper.insertWeldPointGroupInfo(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}

	public ArrayList<HashMap<String, Object>> getDifferentWeldPoint(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getDifferentWeldPoint(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}
	
	/**
	 * ������ �׷��� Revise�ϰ� �������� �ٽ� �����ϴ� ���� ���� �ϴ� ���
	 * ���� ������ �׷�� ������ ������ �׷쿡 ���Ե� �������� ��ġ�� �����ѵ� Feature Name�� �ٸ� ��찡 �ִ�.
	 * �̷� ��� �����Ǿ� ���� ������ �������� Feature Name�� ���� �������� �����ϰ� ������ �ֱ����� �� �����
	 * ã�� Query�� �����ϴ� �Լ��̴�.
	 * 2015-09-23 taeku
	 * 
	 * @param ds
	 * @return
	 * 	�������� ��ġ ������ �������� 4mm �̳����� ������ ��ġ�� �ִ� ���� ������ ���� ������ �����ϰ�
	 * �ش� �������� ��ġ������ ��ġ���� (��ǥ�� 3��), ���� Type, Sheets ���� Old Feature Name, New Feature Name
	 * ������ �˻���� Column Name�� Key�� �ϴ� Column�� �������� HashMap�� List�� ��Ƽ� Return �Ѵ�.
	 * @throws Exception
	 */
	public ArrayList<HashMap<String, Object>> getFeatureNameUpdateTargetList(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getFeatureNameUpdateTargetList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	public ArrayList<HashMap<String, Object>> getChildren(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getChildren(ds);
            if(list != null){
            	for( HashMap<String, Object> map : list){
                    Clob vc = (Clob)map.get("CONDITION");
                    map.put("CONDITION", clobToString(vc));
            	}
            }
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	public HashMap<String, String> getLatestRevision(DataSet ds) throws Exception{
		HashMap<String, String> map = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            map = mapper.getLatestRevision(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return map;
	}

	public String getPreviousRevisionID(DataSet ds) throws Exception{
		String preRevID = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            preRevID = mapper.getPreviousRevisionID(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return preRevID;
	}


	public ArrayList<HashMap<String, Object>> getWeldPoints(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getWeldPoints(ds);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	public ArrayList<HashMap<String, Object>> getRemovedWeldPoint(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getRemovedWeldPoint(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	public ArrayList<HashMap<String, Object>> getAddedWeldPoint(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getAddedWeldPoint(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	public HashMap<String, Object> getEcoEplInfo(DataSet ds) throws Exception{
		HashMap<String, Object> map = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            map = mapper.getEcoEplInfo(ds);
            Clob vc = (Clob)map.get("NEW_VC");
            map.put("NEW_VC", clobToString(vc));
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return map;
	}

	public String clobToString(Clob clob) throws SQLException, IOException {
		if (clob == null) {
			return "";
		}
		StringBuffer strOut = new StringBuffer();
		String str = "";
		BufferedReader br = new BufferedReader(clob.getCharacterStream());
		while ((str = br.readLine()) != null) {
			strOut.append(str);
		}
		return strOut.toString();
	}
	
	public void deleteWeldPointGroupPreRevision(DataSet ds) throws Exception{
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            mapper.deleteWeldPointGroupPreRevision(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
	}

	/**
	 * [SR150714-022][20150907][ymjang] ������ ����(CATIA Feature Name) �߰��� ���� �� BOP �÷� ���� ��û
	 */
	public void insertWeldPointGroupPreRevision(DataSet ds) throws Exception{

		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);

			WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
			
			String item_id = (String)ds.get("item_id");
			String item_rev_id = (String)ds.get("item_rev_id");
			List <Map<String, Object>>  targetList = (List <Map<String, Object>>)ds.get("targetList");
			
			// �� ������ Weld Group �� ������ ��, �� ������.
			DataSet paramDS = new DataSet();
			paramDS.put("item_id", item_id);
			paramDS.put("item_rev_id", item_rev_id);
			mapper.deleteWeldPointGroupPreRevision(paramDS);

			for (int i = 0; i < targetList.size(); i++) {
				Map<String, Object> targetMap = targetList.get(i);

				paramDS = new DataSet();
				paramDS.put("item_id", item_id);
				paramDS.put("item_rev_id", item_rev_id);
				paramDS.put("weld_type", targetMap.get("weld_type").toString());
				paramDS.put("sheets", targetMap.get("sheets").toString());
				paramDS.put("transform_tra0",  (BigDecimal)targetMap.get("transform_tra0"));
				paramDS.put("transform_tra1", (BigDecimal)targetMap.get("transform_tra1"));
				paramDS.put("transform_tra2", (BigDecimal)targetMap.get("transform_tra2"));
				paramDS.put("feature_name", targetMap.get("feature_name").toString());
				
				mapper.insertWeldPointGroupPreRevision(paramDS);
			}
			
			session.getConnection().commit();
			
		}catch(Exception e){
			
			e.printStackTrace();			
			try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }

			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}		
	}
	
	public ArrayList<HashMap<String, Object>> getDifferentWeldPointUp(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getDifferentWeldPointUp(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	public ArrayList<HashMap<String, Object>> getRemovedWeldPointUp(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getRemovedWeldPointUp(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	public ArrayList<HashMap<String, Object>> getAddedWeldPointUp(DataSet ds) throws Exception{
		ArrayList<HashMap<String, Object>> list = null;
        try {
            SqlSession session = getSqlSession();
            WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
            list = mapper.getAddedWeldPointUp(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }

        return list;
	}

	/**
	 * [������ 2�� ������] [20150907][ymjang] ������ ó���� �� ��� Part ���� ���¸� ����ϱ� ���� ���� ���̺� �����͸� �����Ѵ�.
	 */
	public void updateWeldPointTransLog(DataSet ds) throws Exception{

		SqlSession session = getSqlSession();
		try{
			session.getConnection().setAutoCommit(false);
			WeldPointMapper mapper = session.getMapper(WeldPointMapper.class);
			mapper.updateWeldPointTransLog(ds);
			session.getConnection().commit();
			
		}catch(Exception e){

			e.printStackTrace();
			try {
                session.getConnection().rollback();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
			
			// [20160928][ymjang] log4j�� ���� ���� �α� ���
			LogUtil.error(e.getMessage(), ds);
			
			throw e;
		}finally{
			sqlSessionClose();
		}		
	}
	
}
