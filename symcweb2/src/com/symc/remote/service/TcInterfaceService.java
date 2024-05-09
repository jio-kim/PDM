package com.symc.remote.service;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.util.StringUtil;

public class TcInterfaceService {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<HashMap> getProductList(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List list = (List<HashMap>)commonDao.selectList("com.symc.interface.getProductList", ds);
        return list;
	}

	@SuppressWarnings("unchecked")
	public void insertProduct(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		String guid = (String)commonDao.selectOne("com.symc.interface.getSysGuid", null);
		ds.put("IF_ID", guid);
		commonDao.insert("com.symc.interface.insertProduct", ds);

		String transType = (String)ds.get("TRANS_TYPE");
		if( transType.equals("F")){
			commonDao.insert("com.symc.interface.insertTransFunction", ds);
		}
	}

	public void updateProduct(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		commonDao.update("com.symc.interface.updateProduct", ds);
//
//		String functionID = (String)ds.get("FUNCTION_ID");
//		String functionRevID = (String)ds.get("FUNCTION_REV_ID");
//		String transType = (String)ds.get("TRANS_TYPE");
//
//		if( transType != null && transType.equalsIgnoreCase("F")){
//			ds.clear();
//			ds.put("TRANS_TYPE", "F");
//			ds.put("FUNCTION_REV_ID", functionRevID);
//			ds.put("IF_ID", functionID);
//			List list = (List<HashMap>)commonDao.selectList("com.symc.interface.getProductList", ds);
//		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<HashMap> getNoTransInfo(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap> list = (List<HashMap>)commonDao.selectList("com.symc.interface.getNoTransInfo", ds);
        return list;
	}

	public void insertNoTransProduct(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		commonDao.insert("com.symc.interface.insertNoTransProduct", ds);
	}

	public void createFilePath(DataSet ds)throws Exception{
		TcCommonDao.getTcCommonDao().insert("com.symc.ifpe.createFilePath", ds);
	}

	public void sendMail(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		commonDao.update("com.symc.interface.sendMailEai", ds);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<HashMap> getFunctionListToTrans(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap> list = (List<HashMap>)commonDao.selectList("com.symc.interface.getFunctionListToTrans", ds);
		for( HashMap map :list){
			map.put("LOG", StringUtil.clobToString((java.sql.Clob)map.get("LOG")));
		}
        return list;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public List<HashMap> getFunctionList(DataSet ds) throws Exception{
		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		List<HashMap> list = (List<HashMap>)commonDao.selectList("com.symc.interface.getFunctionList", ds);
        return list;
	}

}
