package com.kgm.service;

import java.util.ArrayList;
import java.util.HashMap;

import com.kgm.common.remote.DataSet;
import com.kgm.dao.SYMCBOPFindAISDataDao;

public class SYMCBOPFindAISDataService {

	private SYMCBOPFindAISDataDao dao;

    /**
     * 임시저장해 놓은 Data에서 검색조건에 맞는 국문 조립작업 표준서 Data를 Query하는 Service Function
     * @param ds
     * @return
     */
    public ArrayList<HashMap> findKORInstructionSheets(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findKORInstructionSheets(ds);
    }

    /**
     * 임시저장해 놓은 Data에서 검색조건에 맞는 영문 조립작업 표준서 Data를 Query하는 Service Function
     * @param ds
     * @return
     */
    public ArrayList<HashMap> findENGInstructionSheets(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findENGInstructionSheets(ds);
    }

    /**
     * 조립 국문 조립작업표준서 검색을위해 임시로 Data를 저장하는 Service Function
     * @param dataSet
     * @return
     */
    public int insertKORAssySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertKORAssySheetsData(dataSet);
    }
    
    /**
     * 차체 국문 조립작업표준서 검색을위해 임시로 Data를 저장하는 Service Function
     * @param dataSet
     * @return
     */
    public int insertKORBodySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertKORBodySheetsData(dataSet);
    }
    
    /**
     * 도장 국문 조립작업표준서 검색을위해 임시로 Data를 저장하는 Service Function
     * @param dataSet
     * @return
     */
    public int insertKORPaintSheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertKORPaintSheetsData(dataSet);
    }
    
    /**
     * 조립 영문 조립작업표준서 검색을위해 임시로 Data를 저장하는 Service Function
     * @param dataSet
     * @return
     */
    public int insertENGAssySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertENGAssySheetsData(dataSet);
    }
    
    /**
     * 차체 영문 조립작업표준서 검색을위해 임시로 Data를 저장하는 Service Function
     * @param dataSet
     * @return
     */
    public int insertENGBodySheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertENGBodySheetsData(dataSet);
    }
    
    /**
     * 도장 영문 조립작업표준서 검색을위해 임시로 Data를 저장하는 Service Function 
     * @param dataSet
     * @return
     */
    public int insertENGPaintSheetsData(DataSet dataSet) {
        dao = new SYMCBOPFindAISDataDao();
        return dao.insertENGPaintSheetsData(dataSet);
    }
    
    /**
     * XML Element 형태로 Data를 검색해서 Return하는 Service Function
     * Return Type을 받는 부분에서 형변환을 할 수 없는 현상이 발생됨
     * 아마도 별도로 형변환을위한 추가적인 처리를 해줘야 할 것으로 보임.
     * @param ds
     * @return
     */
    public ArrayList<HashMap<String, Object>> findPublishItemRevListDataXML(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findPublishItemRevListDataXML(ds);
    }
    
    /**
     * 조립작업표준서 검색 결과 Row에 ComboBox로 한글또는 영문 조립작업표준서 Document Item Revision Id를 List 하는데
     * 이것을 여러번 반복하지 않고 한번만 읽어서 XML 문서로 만들고 담아놓기 위해 위의 findPublishItemRevListDataXML을
     * 대체하기위해 만든 Service Function 임. 
     * @param ds
     * @return
     */
    public ArrayList<HashMap<String, Object>> findPublishItemRevListDataList(DataSet ds){
		dao = new SYMCBOPFindAISDataDao();
        return dao.findPublishItemRevListDataList(ds);
    }
    
    /**
     * 생성된지 7일이 지난 Data를 삭제하는 Service Function
     * @param dataSet
     * @return
     */
    public int deleteBeforDatas(DataSet dataSet){
		dao = new SYMCBOPFindAISDataDao();
        return dao.deleteBeforDatas(dataSet);
    }
    
}
