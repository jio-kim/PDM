package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.dto.ExcludeFromCommonPartInEcoData;
import com.ssangyong.dto.TCBomLineData;
import com.ssangyong.dto.TCEcoModel;
import com.ssangyong.dto.TCPartModel;
import com.ssangyong.mapper.CommonPartCheckMapper;

/**
 * [SR180329-023][20180411] csh
 * 공용부품 품번변경 누락 검토기능
 */
public class CommonPartCheckDao extends AbstractDao {
	 
	 @SuppressWarnings("rawtypes")
	public List<TCEcoModel> getEcoList(DataSet ds)  throws Exception  {
        List<TCEcoModel> result = null;
        try {
            SqlSession session = getSqlSession();
            CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
            result = mapper.getEcoList(ds);
        } catch (Exception e) {
            e.printStackTrace();
			LogUtil.error(e.getMessage(), ds);
			throw e;
            
        } finally {
            sqlSessionClose();
        }
        return result;
	 }
	 
	 public List<TCPartModel> getOldPartListWithN1 (DataSet ds)  throws Exception  {
		 List<TCPartModel> result = null;
		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 result = mapper.getOldPartListWithN1(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
		 return result;
	 }
	 
	 public List<TCBomLineData> getCommonPartCheckReport (DataSet ds)  throws Exception {
		 List<TCBomLineData> result = null;
		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 result = mapper.getCommonPartCheckReport(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
		 return result;
	 }
	 
	 public List<ExcludeFromCommonPartInEcoData> getExcludePartData (DataSet ds)  throws Exception{
		 List<ExcludeFromCommonPartInEcoData> result = null;
		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 result = mapper.getExcludePartData(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
		 return result;
	 }
		 
	 
	 public void saveExcludePartData ( List<ExcludeFromCommonPartInEcoData> dataList  ) throws Exception{

		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 for (Iterator iterator = dataList.iterator(); iterator.hasNext();) {

				 ExcludeFromCommonPartInEcoData excludeFromCommonPartInEcoData = (ExcludeFromCommonPartInEcoData) iterator.next();

				 int flag = excludeFromCommonPartInEcoData.getChangedFlag();

				 if( ExcludeFromCommonPartInEcoData.ADDED_FLAG == flag ){
					 mapper.insertExcludePartData(excludeFromCommonPartInEcoData);
				 }else if ( ExcludeFromCommonPartInEcoData.DELETED_FLAG == flag ){
					 mapper.deleteExcludePartData(excludeFromCommonPartInEcoData);
				 }else if ( ExcludeFromCommonPartInEcoData.CHANGED_FLAG == flag ){
					 mapper.updateExcludePartData(excludeFromCommonPartInEcoData);
				 }else{
					 //LogUtil.error("ExcludeFromCommonPartInEcoData.FLAG is 0 ", excludeFromCommonPartInEcoData );
					 //throw new TCException(" ExcludeFromCommonPartInEcoData.FLAG is 0 " + excludeFromCommonPartInEcoData);
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

	 public void updateExcludePartData(ExcludeFromCommonPartInEcoData data)  throws Exception{
		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.updateExcludePartData(data);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), data);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
	 }

	 public void insertExcludePartData(ExcludeFromCommonPartInEcoData data)  throws Exception{
		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.insertExcludePartData(data);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), data);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
	 }

	 public void deleteExcludePartData(ExcludeFromCommonPartInEcoData data)  throws Exception{
		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.deleteExcludePartData(data);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), data);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }
	 }
	 
	 public void createReport ( DataSet ds  ) throws Exception{

		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.createReport(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }

	 }
	 
	 public ArrayList<String> getFunctionList(DataSet ds)  throws Exception  {
		 ArrayList<String> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
	            result = mapper.getFunctionList(ds);
	        } catch (Exception e) {
	            e.printStackTrace();
				LogUtil.error(e.getMessage(), ds);
				throw e;
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
		 }
	 
	 public ArrayList<String> getChildList(DataSet ds)  throws Exception  {
		 ArrayList<String> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
	            result = mapper.getChildList(ds);
	        } catch (Exception e) {
	            e.printStackTrace();
				LogUtil.error(e.getMessage(), ds);
				throw e;
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
		 }
	
	 public void updateNmcd ( DataSet ds  ) throws Exception{

		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.updateNmcd(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }

	 }
	 
	 public void insertNmcd ( DataSet ds  ) throws Exception{

		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.insertNmcd(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }

	 }
	 
	 public void deleteNmcd ( DataSet ds  ) throws Exception{

		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.deleteNmcd(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }

	 }
	 
	 public void mergeNmcd ( DataSet ds  ) throws Exception{

		 try {
			 SqlSession session = getSqlSession();
			 CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
			 mapper.mergeNmcd(ds);
		 } catch (Exception e) {
			 e.printStackTrace();
			 LogUtil.error(e.getMessage(), ds);
			 throw e;

		 } finally {
			 sqlSessionClose();
		 }

	 }
	 
	 public ArrayList<String> getEplList(DataSet ds)  throws Exception  {
		 ArrayList<String> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
	            result = mapper.getEplList(ds);
	        } catch (Exception e) {
	            e.printStackTrace();
				LogUtil.error(e.getMessage(), ds);
				throw e;
	            
	        } finally {
	            sqlSessionClose();
	        }
	        return result;
		 }
	 
	 public ArrayList<String> getVnetTeamNameK(DataSet ds)  throws Exception  {
		 ArrayList<String> result = null;
	        try {
	            SqlSession session = getSqlSession();
	            CommonPartCheckMapper mapper = session.getMapper(CommonPartCheckMapper.class);
	            result = mapper.getVnetTeamNameK(ds);
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
