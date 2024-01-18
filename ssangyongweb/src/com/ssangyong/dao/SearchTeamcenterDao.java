package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.SearchTeamcenterMapper;

public class SearchTeamcenterDao extends AbstractDao
{
	public ArrayList<HashMap<String, Object>> searchItemRevision(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			SearchTeamcenterMapper mapper = session.getMapper(SearchTeamcenterMapper.class);
			list = mapper.searchItemRevision(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
			LogUtil.error(e.getMessage());
			throw e;
		} finally
		{
			sqlSessionClose();
		}
		return list;
	}

	public ArrayList<HashMap<String, Object>> executeSqlSelect(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			SearchTeamcenterMapper mapper = session.getMapper(SearchTeamcenterMapper.class);
			list = mapper.executeSqlSelect(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
			LogUtil.error(e.getMessage());
			throw e;
		} finally
		{
			sqlSessionClose();
		}
		return list;
	}

	public void executeSqlInsert(DataSet ds) throws Exception
	{
		try
		{
			SqlSession session = getSqlSession();
			SearchTeamcenterMapper mapper = session.getMapper(SearchTeamcenterMapper.class);
			mapper.executeSqlInsert(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
			LogUtil.error(e.getMessage());
			throw e;
		} finally
		{
			sqlSessionClose();
		}
	}

	public void executeSqlUpdate(DataSet ds) throws Exception
	{
		try
		{
			SqlSession session = getSqlSession();
			SearchTeamcenterMapper mapper = session.getMapper(SearchTeamcenterMapper.class);
			mapper.executeSqlUpdate(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
			LogUtil.error(e.getMessage());
			throw e;
		} finally
		{
			sqlSessionClose();
		}
	}

	public void executeSqlDelete(DataSet ds) throws Exception
	{
		try
		{
			SqlSession session = getSqlSession();
			SearchTeamcenterMapper mapper = session.getMapper(SearchTeamcenterMapper.class);
			mapper.executeSqlDelete(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
			LogUtil.error(e.getMessage());
			throw e;
		} finally
		{
			sqlSessionClose();
		}
	}
}
