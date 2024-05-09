package com.kgm.service;

import java.util.ArrayList;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.DPVInterfaceDao;
import com.kgm.dto.DPVInterfaceData;

public class DPVInterfaceService {
	public ArrayList<DPVInterfaceData> searchTargetReports(DataSet ds) throws Exception{
		DPVInterfaceDao dao = new DPVInterfaceDao();
		return dao.searchTargetReports(ds);
	}

	public void updateInterfaceInfo(DataSet ds) throws Exception{
		DPVInterfaceDao dao = new DPVInterfaceDao();
		dao.updateInterfaceInfo(ds);
	}

	public String getItemRevisionPuid(DataSet ds) throws Exception{
		DPVInterfaceDao dao = new DPVInterfaceDao();
		return dao.getItemRevisionPuid(ds);
	}

	public void updatePartProperty(DataSet ds) throws Exception{
		DPVInterfaceDao dao = new DPVInterfaceDao();
		dao.updatePartProperty(ds);
	}
}
