package com.kgm.soa.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public  abstract class AbstractDao {
	
	private SqlSession sqlSession = null;
	private static SqlSessionFactory sqlSessionFactory = null;
	private static HashMap<String, SqlSessionFactory> factoryMap = new HashMap<String, SqlSessionFactory>();
	
	public SqlSession getSqlSession(){
		
		if( sqlSessionFactory == null ){
			InputStream inputStream;
			try {
				inputStream = Resources.getResourceAsStream("com/kgm/soa/config/mybatis-config.xml");
				sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if( sqlSession == null){
			sqlSession = sqlSessionFactory.openSession(true);
		}
		
		return sqlSession;
	}
	
	public void sqlSessionClose(){
		if( sqlSession != null){
			sqlSession.close();
			sqlSession = null;
		}
	}
	
	/**
	 * default environment���� �ٸ� connection�� �ʿ��� ��� ���.
	 * �ݵ�� �������� Close�ؾ���.
	 * @param environmentId
	 * @return
	 */
	public static SqlSession getOtherSession(String environmentId){
		SqlSessionFactory sqlSessionFactory = null;
		if( environmentId == null){
			return null;
		}else{
			if( factoryMap.containsKey(environmentId)){
				sqlSessionFactory = factoryMap.get(environmentId);
			}else{
				InputStream inputStream;
				try {
					inputStream = Resources.getResourceAsStream("com/kgm/config/mybatis-config.xml");
					sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream, environmentId);
					factoryMap.put(environmentId, sqlSessionFactory);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}	
		
		return sqlSessionFactory.openSession();
	}
}
