package com.kgm.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.VariantDao;

@SuppressWarnings("rawtypes")
public class VariantService {
	public HashMap getItem(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		HashMap map = dao.getItem(ds);
		return map;
	}
	
	public int insertVariantValueDesc(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.insertVariantValueDesc(ds);
	}
	
	public List getVariantValueDesc(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getVariantValueDesc(ds);
	}
	
	public int updateVariantValueDesc(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.updateVariantValueDesc(ds);
	}
	
	public int getUsedCount(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getUsedCount(ds);
	}
	
	public List getUsedOptions(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getUsedOptions(ds);
	}
	
	public int insertOsiInfo(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.insertOsiInfo(ds);
	}
	
	public List getBuildSpecList(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getBuildSpecList(ds);
	}
	
	public List getLocalBuildSpecList(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getLocalBuildSpecList(ds);
	}
	
	public List getBuildSpecInfo(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getBuildSpecInfo(ds);
	}
	
	public String getNewId(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getNewId(ds);
	}
	
	// º¸·ù
//	public String getNextId(DataSet ds) throws Exception{
//		VariantDao dao = new VariantDao();
//		return dao.getNextId(ds);
//	}
	
	public List getProjectCodes() throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getProjectCodes();
	}
	
	public List getValidationInfoList(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getValidationInfoList(ds);
	}
	
	public int insertValidationInfo(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.insertValidationInfo(ds);
	}
	
	public int deleteValidationInfo(DataSet ds) throws Exception{
		VariantDao dao = new VariantDao();
		return dao.deleteValidationInfo(ds);
	}
	
	public List getSpecOptions(DataSet ds)throws Exception{
		VariantDao dao = new VariantDao();
		return dao.getSpecOptions(ds);
	}
	
	@SuppressWarnings("unchecked")
	public HashMap getMinusInfo(DataSet ds) throws Exception{
		
		HashMap map = new HashMap();
//		ArrayList sosList = (ArrayList)ds.get("SOS");
//		for( int i = 0; sosList != null && i < sosList.size(); i++){
			VariantDao dao = new VariantDao();
//			ds.put("SOS_PUID", sosList.get(i));
			map.put(ds.get("SOS_PUID"), dao.getMinusInfo(ds));
//		}
		return map;
	}
    
	/**
	 * OSpec Trim Select
	 * @param ds
	 * @return
	 * @throws Exception
	 */
	public List<HashMap<String, String>> selectOSpecTrim(DataSet ds) throws Exception {
		VariantDao dao = new VariantDao();
    	return dao.selectOSpecTrim(ds);
    }
	
	/**
	 * OSpec Trim Delete
	 * @param ds
	 * @return
	 */
	public boolean deleteOSpecTrim(DataSet ds){
		VariantDao dao = new VariantDao();
        return dao.deleteOSpecTrim(ds);
	}
	
	/**
	 * OSpec Trim Insert
	 * @param ds
	 * @throws Exception
	 */
    public void insertOSpecTrim(DataSet ds) throws Exception {
    	VariantDao dao = new VariantDao();
    	dao.insertOSpecTrim(ds);
    }
}
