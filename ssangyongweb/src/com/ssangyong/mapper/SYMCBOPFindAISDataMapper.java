package com.ssangyong.mapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.ssangyong.common.remote.DataSet;

public interface SYMCBOPFindAISDataMapper {

    public ArrayList<HashMap> findKORInstructionSheets(DataSet ds);

    public ArrayList<HashMap> findENGInstructionSheets(DataSet ds);
	
    public int insertKORAssySheetsData(DataSet dataSet);
    
    public int insertKORBodySheetsData(DataSet dataSet);
    
    public int insertKORPaintSheetsData(DataSet dataSet);
    
    public int insertENGAssySheetsData(DataSet dataSet);
    
    public int insertENGBodySheetsData(DataSet dataSet);
    
    public int insertENGPaintSheetsData(DataSet dataSet);
    
    public ArrayList<HashMap<String, Object>> findPublishItemRevListDataXML(DataSet ds);
    
    public ArrayList<HashMap<String, Object>> findPublishItemRevListDataList(DataSet ds);
    
    public int deleteBeforDatas(DataSet dataSet);
}
