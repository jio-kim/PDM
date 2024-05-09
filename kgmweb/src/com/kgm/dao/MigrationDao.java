package com.kgm.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.common.util.LogUtil;
import com.kgm.mapper.MigrationMapper;

public class MigrationDao extends AbstractDao {
    /**
     * Migration 대상 리스트를 가져온다.
     * 
     * @method getValidationList
     * @date 2013. 2. 18.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getValidationList(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            MigrationMapper mapper = session.getMapper(MigrationMapper.class);
            result = mapper.getValidationList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
    }
    
    /**
     *  Migration 대상 리스트를 가져온다. (Item)
     * 
     * @method getItemValidationList 
     * @date 2013. 3. 6.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getItemValidationList(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            MigrationMapper mapper = session.getMapper(MigrationMapper.class);
            // X100은 다른 File조회를 하므로 쿼리를 다르게 함.
            if("X100_MIG_VEHPART".equals(ds.get("objectType"))) {
                result = mapper.getX100ItemValidationList(ds);
            } else {
                result = mapper.getItemValidationList(ds);
            }
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
    }

    /**
     * ITEM Migration 후 MIG_FLAG Status를 변경한다.
     * 
     * @method updateMigrationStatus
     * @date 2013. 2. 18.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void updateMigrationStatus(DataSet ds) {
        try {
            SqlSession session = getSqlSession();
            MigrationMapper mapper = session.getMapper(MigrationMapper.class);
            if (ds.containsKey("updateDataList") && ds.containsKey("objectType")) {
                ArrayList<HashMap> updateDataList = (ArrayList<HashMap>) ds.get("updateDataList");
                for (int i = 0; i < updateDataList.size(); i++) {
                    // UPDATE OBJECT TABLE 설정
                    updateDataList.get(i).put("objectType", ds.get("objectType"));
                    mapper.updateMigrationItemStatus(updateDataList.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
    }

    /**
     * 마이그레이션 대상 BOM List를 가져온다.
     * 
     * @method getBOMValidationList
     * @date 2013. 2. 26.
     * @param
     * @return List<HashMap>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("rawtypes")
    public List<HashMap> getBOMValidationList(DataSet ds) {
        List<HashMap> result = null;
        try {
            SqlSession session = getSqlSession();
            MigrationMapper mapper = session.getMapper(MigrationMapper.class);
            result = mapper.getBOMValidationList(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
        return result;
    }

    /**
     * ITEM Migration 후 MIG_FLAG Status를 변경한다.
     * 
     * @method updateMigrationBOMStatus
     * @date 2013. 2. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateMigrationBOMStatus(DataSet ds) {
        try {
            SqlSession session = getSqlSession();
            MigrationMapper mapper = session.getMapper(MigrationMapper.class);
            if (ds.containsKey("updateDataList") && ds.containsKey("objectType")) {
                ArrayList<HashMap> updateDataList = (ArrayList<HashMap>) ds.get("updateDataList");
                for (int i = 0; i < updateDataList.size(); i++) {
                    // UPDATE OBJECT TABLE 설정
                    mapper.updateMigrationBOMStatus(updateDataList.get(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
    }
    
    public void updateMigrationStatusChange(DataSet ds) {
        try {
            SqlSession session = getSqlSession();
            MigrationMapper mapper = session.getMapper(MigrationMapper.class);
            mapper.updateMigrationItemStatus(ds);
        } catch (Exception e) {
            e.printStackTrace();
            
			// [20160928][ymjang] log4j에 의한 에러 로그 기록
			LogUtil.error(e.getMessage(), ds);
            
        } finally {
            sqlSessionClose();
        }
    }
}
