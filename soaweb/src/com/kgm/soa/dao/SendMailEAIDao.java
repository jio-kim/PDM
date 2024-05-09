package com.kgm.soa.dao;

import org.apache.ibatis.session.SqlSession;

import com.kgm.common.remote.DataSet;
import com.kgm.soa.mapper.SendMailEAIMapper;

public class SendMailEAIDao extends AbstractDao
{
	public void sendMailEAI(DataSet ds) throws Exception
	{
		try
		{
			SqlSession sqlSession = getSqlSession();
			SendMailEAIMapper mapper = sqlSession.getMapper(SendMailEAIMapper.class);
			mapper.sendMailEAI(ds);
		} catch (Exception e)
		{
			throw e;
		} finally
		{
			sqlSessionClose();
		}
	}
}
