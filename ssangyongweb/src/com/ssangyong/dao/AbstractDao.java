package com.ssangyong.dao;

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
	private int status = 0;
	
	public SqlSession getSqlSession(){
		
		if( sqlSessionFactory == null ){
			InputStream inputStream;
			try {
				inputStream = Resources.getResourceAsStream("com/ssangyong/config/mybatis-config.xml");
				sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
				status = 1;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if( sqlSession == null){
			sqlSession = sqlSessionFactory.openSession(true);
		}
		
		if( status == 1 ){
			try{
				System.out.println(sqlSession.getConnection().getMetaData().getURL());
			}catch (Exception e){
				e.printStackTrace();
			}
		}
		
		return sqlSession;
	}
	
	public void sqlSessionClose(){
		if( sqlSession != null){
			sqlSession.close();
		}
	}
	
	/**
	 * default environment외의 다른 connection이 필요한 경우 사용.
	 * 반드시 수동으로 Close해야함.
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
					inputStream = Resources.getResourceAsStream("com/ssangyong/config/mybatis-config.xml");
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
