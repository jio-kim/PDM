package com.ssangyong.soa.dao;

import org.apache.ibatis.session.SqlSession;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.soa.mapper.SendMailEAIMapper;

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
