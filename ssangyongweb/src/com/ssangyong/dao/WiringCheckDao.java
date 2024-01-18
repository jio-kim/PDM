package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.WiringCheckMapper;

public class WiringCheckDao extends AbstractDao
{
	public ArrayList<HashMap<String, Object>> findUserInVNet(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			WiringCheckMapper mapper = session.getMapper(WiringCheckMapper.class);
			list = mapper.findUserInVNet(ds);
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

	public ArrayList<HashMap<String, Object>> getWiringMailList(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			WiringCheckMapper mapper = session.getMapper(WiringCheckMapper.class);
			list = mapper.getWiringMailList(ds);
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

	public void deleteWiringMailList() throws Exception
	{
		try
		{
			SqlSession session = getSqlSession();
			WiringCheckMapper mapper = session.getMapper(WiringCheckMapper.class);
			mapper.deleteWiringMailList();
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

	public void insertWiringMailList(DataSet ds) throws Exception
	{
		try
		{
			SqlSession session = getSqlSession();
			WiringCheckMapper mapper = session.getMapper(WiringCheckMapper.class);
			mapper.insertWiringMailList(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
			LogUtil.error(e.getMessage(), ds);
			throw e;
		} finally
		{
			sqlSessionClose();
		}
	}

	public ArrayList<HashMap<String, Object>> getWiringCategoryNo() throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			WiringCheckMapper mapper = session.getMapper(WiringCheckMapper.class);
			list = mapper.getWiringCategoryNo();
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

	public void deleteWiringCategoryNo() throws Exception
	{
		try
		{
			SqlSession session = getSqlSession();
			WiringCheckMapper mapper = session.getMapper(WiringCheckMapper.class);
			mapper.deleteWiringCategoryNo();
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

	public void insertWiringCategoryNo(DataSet ds) throws Exception
	{
		try
		{
			SqlSession session = getSqlSession();
			WiringCheckMapper mapper = session.getMapper(WiringCheckMapper.class);
			mapper.insertWiringCategoryNo(ds);
		} catch (Exception e)
		{
			e.printStackTrace();
			LogUtil.error(e.getMessage(), ds);
			throw e;
		} finally
		{
			sqlSessionClose();
		}
	}

}
