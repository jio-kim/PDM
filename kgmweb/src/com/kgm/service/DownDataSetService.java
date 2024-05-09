package com.kgm.service;

import java.util.ArrayList;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.DownDataSetDao;

public class DownDataSetService {
	/**
	 * DataSet Download Log Insert.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 * @param data
	 * @throws Exception 
	 */
	public Integer downDataSetlogInsert(DataSet ds) throws Exception {
		DownDataSetDao dao = new DownDataSetDao();
		return dao.downDataSetlogInsert(ds);
	}
	
	/**
	 * DataSet Download Log Select.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 * @param uid
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList downDataSetlogSelect(DataSet ds) throws Exception {
		DownDataSetDao dao = new DownDataSetDao();
		return dao.downDataSetlogSelect(ds);
	}
	
	/**
	 * DataSet Down Log Date 별 Select.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 9.
	 * @param data
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("rawtypes")
	public ArrayList downDataSetLogDateSelect(DataSet ds) throws Exception {
		DownDataSetDao dao = new DownDataSetDao();
		return dao.downDataSetLogDateSelect(ds);
	}

}
