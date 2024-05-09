package com.kgm.mapper;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.kgm.rac.kernel.SYMCBOMEditData;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.kgm.rac.kernel.SYMCPartListData;

public interface MCOHistoryMapper {
    public List<String> selectUserWorkingECO(DataSet ds);

    //public Integer insertECOBOMWork(SYMCBOMEditData bomEditData);
    public String isECOEPLChanged(DataSet ds);
    
    public Integer extractEPL(DataSet ds);

    public List<String> selectEPLData(DataSet ds);

    public List<SYMCECODwgData> selectECODwgList(DataSet ds);
    
    public Integer updateECODwgProperties(SYMCECODwgData bomEditData);

    public List<SYMCBOMEditData> selectECOEplList(DataSet ds);

    public Integer updateECOEPLProperties(SYMCBOMEditData bomEditData);
    
    public List<SYMCPartListData> selectECOPartList(DataSet ds);

    public Integer updateECOPartListProperties(SYMCPartListData partListData);
    
    public List<SYMCBOMEditData> selectECOBOMList(DataSet ds);
    
    // Demon
    public List<HashMap<String, String>> selectECODemonTarget() throws Exception;
    
    public List<HashMap<String, String>> selectOccurrenceECO(DataSet ds) throws Exception;
    
    public Integer updateOccurrenceECOApplied(DataSet ds) throws Exception;
    
    public List<HashMap<String, String>> selectReleasedECO(DataSet ds);
    
    public Integer insertECOInfoToVPM(DataSet ds) throws Exception;
    
    public Integer updateECOInfoInterfacedToVPM(DataSet ds) throws Exception;
    
    /*public Integer reviseBOMPart(DataSet ds);

    public Integer saveAsBOMPart(DataSet ds);*/
}
