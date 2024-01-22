package com.symc.plm.rac.prebom.masterlist.operation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.teamcenter.rac.aif.AbstractAIFOperation;

public class ExcelDataImportOreration extends AbstractAIFOperation{
    
    private OSpec ospec;
    private List<OpTrim> ospecTrimList;
    private HashMap<String, Object> resultData = new HashMap<String, Object>();
    @SuppressWarnings("rawtypes")
    private Vector<Vector> data;
    private String fileName;
    private int trimSize = 0;
    private int startDataRow = 5;
    
    //[20171220][LJG] 프로토 툴링 컬럼 추가에따른 수정
    //private int totalColSize = 50;
    //private int firstTrimSeq = 18;
//    private int totalColSize = 51;
    private int firstTrimSeq = 19;
  //[20180424][CSH] OSPEC NO 컬럼 추가에따른 수정
    // 20200923 seho EJS Column 추가.
	//[CF-1706] WEIGHT MANAGEMENT 칼럼 추가로 총 column 개수 1 증가함(53->54). by 전성용(20201223)    
    private int totalColSize = 54;

    private static final int START_ROW_INDEX = 2;
    
    public ExcelDataImportOreration(OSpec fromOspec, String fromFileName) {
        this.ospec = fromOspec;
        this.ospecTrimList = ospec.getTrimList();
        this.fileName = fromFileName;
    }

    public void executeOperation() throws Exception {
    	
    	storeOperationResult(resultData);
    	try{
	        File file = new File(fileName);
	        
	        // 엑셀 Sheet 정보를 가져온다
	        Sheet fmpSheet = getSheet(file);
	     
	        // 가져온 시트에 Version 정보와 Template Version 정보를 비교한다
	        isVersionCheck(fmpSheet);
	        
	        // 가져온 시트에 Trim 정보와 Ospec Trim 정보를 비교한다
	        isTrimCheck(fmpSheet);
	        
	        // Excel 데이터를 읽어와 Vector 에 저장한다
	        data = getData(fmpSheet);
	        if (null == data) {
	            throw new Exception("데이터가 존재 하지 않습니다.");
	        }
	        resultData.put("DATA", data);
    	}catch(Exception e){
    		resultData.put("ERROR", e);
    		throw e;
    	}
    }

    /**
     * 
     * @param file
     * @return
     * @throws FileNotFoundException
     * @throws IOException
     */
    private Sheet getSheet(File file) throws FileNotFoundException, IOException {
        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        return workbook.getSheetAt(0);
    }
    
    
    /**
     * 1. Ospec Trim 갯수 비교
     * 2. Ospec Trim 리스트와 Excel Trim 리스트 항목 비교
     * 
     * @param fmpSheet
     * @return
     * @throws Exception 
     */
    private void isTrimCheck(Sheet fmpSheet) throws Exception {
        List<String> excelTrimList = new ArrayList<String>();
        Row firstRow = fmpSheet.getRow(0+START_ROW_INDEX);
        Row row = fmpSheet.getRow(4+START_ROW_INDEX);
        
        trimSize = ospecTrimList.size();
        if (!firstRow.getCell(firstTrimSeq + trimSize).getStringCellValue().trim().equals("S/MODE")) {
            throw new Exception("Excel 에 Ospec 정보가 올바르지 않습니다.");
        }
        for (int i = 0; i < trimSize; i++) {
            excelTrimList.add(row.getCell(firstTrimSeq + i).getStringCellValue().trim());
        }
        for (int i = 0; i < trimSize; i++) {
            if (!excelTrimList.contains(ospecTrimList.get(i).getTrim())) {
                throw new Exception("Excel Ospec 정보가 올바르지 않습니다.");
            }
        }
    }
    
    private void isVersionCheck(Sheet fmpSheet) throws Exception {
    	String version = fmpSheet.getRow(0).getCell(0).getStringCellValue().trim();
    	if(!version.equals("Ver1.3")){
    		throw new Exception("Template 버전이 최신이 아닙니다.\nTemplate을 다운로드하여 작업하여 주십시오.");
    	}
    }

    /**
     * 
     * @param fmpSheet
     * @return
     */
    @SuppressWarnings({ "rawtypes", "unchecked", "serial" })
    private Vector<Vector> getData(Sheet fmpSheet) {
        
        Vector<Vector> vectorList = new Vector() {

			@Override
			public synchronized Object clone() {
				Vector<Vector> newData = new Vector();
				for (int i = 0; i < this.elementCount; i++) {
					Vector row = (Vector) elementData[i];
					Vector newRow = new Vector();
					newRow.addAll(row);

					newData.add(newRow);
				}
				return newData;
			}

		};
        
        int columnSize = totalColSize;
        int lastRowNum = fmpSheet.getLastRowNum() + 1;
        for (int i = startDataRow + START_ROW_INDEX; i < lastRowNum; i++) {
            Row row = fmpSheet.getRow(i);
            if(row == null){
            	continue;
            }
            Vector vectorData = new Vector();
            vectorData.add("");
            for (int j = 0; j < columnSize; j++) {
                if (6 == j) {
                    vectorData.add("");
                    continue;
                }
                if (firstTrimSeq > j) {
                    vectorData = setData(vectorData, row, j);
                } else {
                    if (firstTrimSeq == j ) {
                        for (int k = firstTrimSeq; k < trimSize + firstTrimSeq; k++) {
                            vectorData = setData(vectorData, row, k);
                        }
                    }
                    vectorData = setData(vectorData, row, j + trimSize);
                }
            }
            vectorList.add(vectorData);
        }
        return vectorList;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private Vector setData(Vector vectorData, Row row, int cellSeq){
//    	System.out.println(cellSeq);
        if (null == row.getCell(cellSeq)) {
            vectorData.add("");
        }else{
            if (row.getCell(cellSeq).getCellType() == 0) {
                if (firstTrimSeq + trimSize <= cellSeq) {
                    vectorData.add(row.getCell(cellSeq).toString());
                }else{
                    DecimalFormat dft = new DecimalFormat("########################.########");
                    vectorData.add(dft.format(row.getCell(cellSeq).getNumericCellValue()));
                }
            }else{
            	//[CSH][20180425]Excel Template 작성 시 Null String 때문에 발생되는 오류 방지
//            	System.out.println("row :"+ row.getRowNum() + ", cellSeq  :" + cellSeq + "::::" + row.getCell(cellSeq).getStringCellValue());
                vectorData.add(row.getCell(cellSeq).getStringCellValue().trim());
            }
        }
        return vectorData;
    }
}
