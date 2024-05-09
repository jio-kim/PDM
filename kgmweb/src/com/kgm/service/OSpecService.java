package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.OSpecDao;

public class OSpecService {
	
	public void insertOSpec(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		dao.insertOSpec(ds);		
	}
	
	public ArrayList getOspecMaster(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getOspecMaster(ds);
	}
	
	public ArrayList getOspecDetail(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getOspecDetail(ds);
	}
	
	public HashMap getOspec(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getOspec(ds);
	}
	
	public ArrayList getGModel(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getGModel(ds);
	}
	
	public ArrayList getProject(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getProject(ds);
	}
	
	public ArrayList getOspecTrim(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getOspecTrim(ds);
	}
	
	public ArrayList getOptionGroup(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getOptionGroup(ds);
	}
	
	public void insertOptionGroup(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		dao.insertOptionGroup(ds);		
	}
	
	public ArrayList getOptionGroupDetail(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getOptionGroupDetail(ds);
	}
	
	public void deleteOptionGroup(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		dao.deleteOptionGroup(ds);		
	}
	
	public HashMap<String, ArrayList> getReferedOptionGroup(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getReferedOptionGroup(ds);
	}
	
	public ArrayList getFunctionList(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getFunctionList(ds);
	}
	
	public ArrayList getUsedCondition(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getUsedCondition(ds);
	}
    
    public void updateOSpecTrimStat(DataSet ds) throws Exception {
    	OSpecDao dao = new OSpecDao();
    	dao.updateOSpecTrimStat(ds);
    }
    /**
     * option name, description �� �Է¹޾� ������Ʈ�� ��
     */
    public void updateOpGroupMaster(DataSet ds) throws Exception {
    	OSpecDao dao = new OSpecDao();
    	dao.updateOpGroupMaster(ds);
    }
    /**
     * option group condition ������ ������Ʈ ��
     */
    public void updateOpGroupCondition(DataSet ds) throws Exception {
    	OSpecDao dao = new OSpecDao();
    	dao.updateOpGroupCondition(ds);
    }
    
    /**
     * OSpec Group �� Condition �� ������
     */
    public String getOpGroupCondition(DataSet ds) throws Exception {
    	OSpecDao dao = new OSpecDao();
    	return dao.getOpGroupCondition(ds);
    }
    
    //[SR181211-009][CSH]External Table���� DCS ���� ��������
    public ArrayList getDCSInfo(DataSet ds) throws Exception{
		OSpecDao dao = new OSpecDao();
		return dao.getDCSInfo(ds);
	}
}
