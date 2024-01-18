package com.ssangyong.dao;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.util.LogUtil;
import com.ssangyong.mapper.ECOAdminCheckMapper;

public class ECOAdminCheckDao extends AbstractDao
{
	public ArrayList<HashMap<String, Object>> getCheckList(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getCheckList(ds);
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

	public ArrayList<HashMap<String, Object>> getMonthlyAllVehicleECOStatus(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getMonthlyAllVehicleECOStatus(ds);
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

	public ArrayList<HashMap<String, Object>> getMonthlyAllVehicleEndItemStatus(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getMonthlyAllVehicleEndItemStatus(ds);
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

	public ArrayList<HashMap<String, Object>> getMonthlyVehicleECOStatus(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getMonthlyVehicleECOStatus(ds);
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

	public ArrayList<HashMap<String, Object>> getMonthlyEngineECOStatus(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getMonthlyEngineECOStatus(ds);
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

	public ArrayList<HashMap<String, Object>> getEcoStatusByTeam(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getEcoStatusByTeam(ds);
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

	public ArrayList<HashMap<String, Object>> getMonthlyVehicleECOAnalysis(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getMonthlyVehicleECOAnalysis(ds);
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

	public ArrayList<HashMap<String, Object>> getMonthlyEngineECOAnalysis(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getMonthlyEngineECOAnalysis(ds);
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

	public ArrayList<HashMap<String, Object>> getLOVData(DataSet ds) throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getLOVData(ds);
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

	public ArrayList<HashMap<String, Object>> getSYMCSubGroup() throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getSYMCSubGroup();
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
	
	public ArrayList<String> getYear() throws Exception
	{
		ArrayList<String> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getYear();
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
	
	public ArrayList<HashMap<String, Object>> getEngineList() throws Exception
	{
		ArrayList<HashMap<String, Object>> list = null;
		try
		{
			SqlSession session = getSqlSession();
			ECOAdminCheckMapper mapper = session.getMapper(ECOAdminCheckMapper.class);
			list = mapper.getEngineList();
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
}
