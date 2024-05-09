package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.PartNameGroupDao;
import com.kgm.dto.ExcludeFromNameGroup;

public class PartNameGroupService {
	
	public void insertPngMaster(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.insertPngMaster(ds);
	}
	
	public ArrayList getPngMaster(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngMaster(ds);
	}	
	
	public HashMap getPngDetail(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngDetail(ds);
	}
	
	public void deletePngMaster(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.deletePngMaster(ds);	
	}	
	
	public ArrayList getPngNameList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngNameList(ds);
	}	
	
	public ArrayList getPngConditionList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngConditionList(ds);
	}		
	
	public ArrayList getPngProdOrder(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngProdOrder(ds);
	}	
	
	public void deletePngProdOrder(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.deletePngProdOrder(ds);		
	}	
	
	public ArrayList getPngNewNameList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngNewNameList(ds);
	}	
	
	public ArrayList getProductList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getProductList(ds);
	}
	
	public ArrayList getPngAssign(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngAssign(ds);
	}	
	
	public void savePngAssign(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.savePngAssign(ds);
	}
	
	public ArrayList getUserSpecList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getUserSpecList(ds);
	}
	
	public ArrayList getBuildSpecList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getBuildSpecList(ds);
	}	
	
	public ArrayList getPlan15SpecList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPlan15SpecList(ds);
	}
	
	public ArrayList getResult30SpecList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getResult30SpecList(ds);
	}
	
	//[SR170810][LJG] 60�� ���� ��ȹ Spec List �߰�
	public ArrayList getResult60SpecList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getResult60SpecList(ds);
	}
	
	public ArrayList getEndItemNameList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getEndItemNameList(ds);
	}	
	
	public ArrayList getSpecEndItemNameList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getSpecEndItemNameList(ds);
	}	
	
	public void set1LevelItemList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.set1LevelItemList(ds);
	}
	
	public void deletePngEpl(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.deletePngEpl(ds);
	}
	
	public String getSpec(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getSpec(ds);
	}
	
	public String getRowKey(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getRowKey(ds);
	}
	
	public ArrayList getUserSpecWithCategory(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getUserSpecWithCategory(ds);
	}	
	
	public ArrayList getBuildSpecWithCategory(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getBuildSpecWithCategory(ds);
	}	
	
	public ArrayList getPlanResultSpecWithCategory(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPlanResultSpecWithCategory(ds);
	}	
	
	public void insertPngNewNameFromECO(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.insertPngNewNameFromECO(ds);
	}
	
	/**
	 * �ְ� ���� ����Ʈ ����Ʈ
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<?> getPngWeeklyErrorReport(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngWeeklyErrorReport(ds);
	}
	
	/**
	 * �ְ� ���� ����Ʈ�� ������ ���� ��¥
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public String getPngWeeklyRepLastDate(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getPngWeeklyRepLastDate(ds);
	}
	
	/**
	 * �ش��ϴ� ���� ������ �ɼ� �ڵ� ����Ʈ�� ������
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 4. 10.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<?> getOptionCodeList(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getOptionCodeList(ds);
	}
	
	/**
	 * �ش� ������ �ɼ� �� M/Y �ɼ��� ������
	 * @Copyright : Plmsoft
	 * @author : ������
	 * @since  : 2017. 6. 14.
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public ArrayList<String> getModelYear(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getModelYear(ds);
	}
	
	/**
	 * Build Spec �ɼ� ���� �̷� ���� ��ȸ
	 * 
	 * @author : ���ȣ
	 * @since  : 2018. 4. 17.
	 * @param ds
	 * @return
	 * @throws Exception
	 * 
	 */ 
	public ArrayList getSpecOptionChangeInfo(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getSpecOptionChangeInfo(ds);
	}
	
	/**
	 * SR181025-028 �ְ� ����Ʈ ���� ó�� �Է�
	 * 
	 * @author : ������
	 * @since  : 2018. 10. 25.
	 * @param ds
	 * @return
	 * @throws Exception
	 * 
	 */ 
	public void saveExcludePartData(DataSet ds) throws Exception{
		List<ExcludeFromNameGroup> dataList = (List<ExcludeFromNameGroup>) ds.get("saveData");
		PartNameGroupDao dao = new PartNameGroupDao();
		dao.saveExcludePartData(dataList);
	}
	
	/**
	 * SR181025-028 �ְ� ����Ʈ ���� ó�� ��ȸ
	 * 
	 * @author : ������
	 * @since  : 2018. 10. 25.
	 * @param ds
	 * @return
	 * @throws Exception
	 * 
	 */ 
	public List getExcludePartData(DataSet ds) throws Exception{
		PartNameGroupDao dao = new PartNameGroupDao();
		return dao.getExcludePartData(ds);
	}
}
