package com.symc.common.dao;

import java.util.List;

import org.apache.ibatis.session.SqlSession;

import com.symc.common.util.ContextUtil;

public class TcCommonDao {

	private SqlSession tcSqlSession;

	public void setTcSqlSession(SqlSession tcSqlSession) {
		this.tcSqlSession = tcSqlSession;
	}

	public Object selectOne(String qry, Object object){
		return tcSqlSession.selectOne(qry, object);
	}

	public List<?> selectList(String qry) {
		return tcSqlSession.selectList(qry);
	}

	public List<?> selectList(String qry, Object object) {
        return tcSqlSession.selectList(qry, object);
    }

	public void insert(String qry, Object object) {
	    tcSqlSession.insert(qry, object);
	}

	public void insertList(String qry,  List<?> list) {
		for (int i = 0; list != null && i < list.size(); i++) {
			this.insert(qry, list.get(i));
		}
	}

	public void update(String qry, Object object) {
        tcSqlSession.update(qry, object);
    }

    public void updateList(String qry,  List<?> list) {
        for (int i = 0; list != null && i < list.size(); i++) {
            this.update(qry, list.get(i));
        }
    }

    public void delete(String qry, Object object) {
        tcSqlSession.delete(qry, object);
    }

    public void deleteList(String qry,  List<?> list) {
        for (int i = 0; list != null && i < list.size(); i++) {
            this.delete(qry, list.get(i));
        }
    }

	public SqlSession getTcSqlSession() {
	    return this.tcSqlSession;
	}

	public static TcCommonDao getTcCommonDao() throws Exception {
	    return (TcCommonDao)ContextUtil.getBean("tcCommonDao");
	}
}
