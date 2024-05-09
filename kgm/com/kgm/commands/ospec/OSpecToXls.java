package com.kgm.commands.ospec;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.PrintSetup;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpTrim;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.commands.ospec.op.Option;
import com.kgm.commands.ospec.panel.OSpecTable;

/**
 * [SR150408-022] [20150529] [ymjang] Variant/Function EPL 수량표현 개선방안(양산 및 개발프로젝트 반영하여 출력할수 있도록)
 * OSpec Excel Export
 * @author ymjang
 *
 */
public class OSpecToXls
{
	private OSpecTable sourceOSpecTable = null;
	private OSpecTable targetOSpecTable = null ;
	private String mergedOspecNo = null ;
	private Map<String, XSSFCellStyle> cellStyles = null;
	private List<Map<String, Object>> mergedVarianList = null;
	private List<String> mergedCatGroupList = null;
	private List<String> mergedOptionList = null;
	private Map<String, List<Option>> mergedOptionMap = null;
	private List<String> areaList = null;
	private List<String> passList = null;
	private List<String> engineList = null;
	private List<String> gradeList = null;
	private Map<String, List<String>> areaMap = null;
	private Map<String, List<String>> passMap = null;
	private Map<String, List<String>> engineMap = null;
	private Map<String, List<String>> gradeMap = null;
	
	private String crLf = Character.toString((char)13) + Character.toString((char)10);
	private int headerLabelRowStart = 2;
	private int headerLabelRowCount = 0;
	private int dataLabelColumnCount = 4;
	private int dataRowsStart = 7;
	private int dataColumnStart = 7;
	
    public OSpecToXls(OSpecTable sourceOSpecTable, OSpecTable targetOSpecTable)
    {
    	this.sourceOSpecTable = sourceOSpecTable;
		this.targetOSpecTable = targetOSpecTable;
    }
    
    public int mergedOspec() throws Exception
    {
		String srcOspecNo = sourceOSpecTable.getOspec().getOspecNo();
		String tgtOspecNo = targetOSpecTable.getOspec().getOspecNo();
		srcOspecNo = srcOspecNo.replaceAll("OSI-", "");
		tgtOspecNo = tgtOspecNo.replaceAll("OSI-", "");
		this.mergedOspecNo = srcOspecNo + "&" + tgtOspecNo;
		
		// 1.Variant 병합
		Map<String, String[]> mergedVariantMap = new HashMap<String, String[]>();
        
        // 1-1.Variant Src
        HashMap<String, OpTrim> srcVariantMap = sourceOSpecTable.getOspec().getTrims(); 
        Iterator<String> srcVarKeys = srcVariantMap.keySet().iterator();
		while(srcVarKeys.hasNext()){
			String variantID = srcVarKeys.next();
			OpTrim opTrim = srcVariantMap.get(variantID);	
			String [] arrTrim = (opTrim.getColOrder() + "_" + opTrim.toString()).split("_");
			if (mergedVariantMap.get(variantID) == null)
			{
				mergedVariantMap.put(variantID, arrTrim);
			}
		}
        
		// 1-2.Variant Tgt
        HashMap<String, OpTrim> tgtVariantMap = targetOSpecTable.getOspec().getTrims();	
        Iterator<String> tgtVarKeys = tgtVariantMap.keySet().iterator();
		while(tgtVarKeys.hasNext()){
			String variantID = tgtVarKeys.next();
			OpTrim opTrim = tgtVariantMap.get(variantID);
			String [] arrTrim = (opTrim.getColOrder() + "_" + opTrim.toString()).split("_");
			if (mergedVariantMap.get(variantID) == null)
			{
				mergedVariantMap.put(variantID, arrTrim);
			}
		}
		
		// 1-3. Variant 정렬
		sortByVariant(mergedVariantMap);
		
		// 2.National별 Variant Map 생성
		areaList = new ArrayList<String>();
        passList = new ArrayList<String>();
        engineList = new ArrayList<String>();
        gradeList = new ArrayList<String>();
        
        areaMap = new HashMap<String, List<String>>();
        passMap = new HashMap<String, List<String>>();
        engineMap = new HashMap<String, List<String>>();
        gradeMap = new HashMap<String, List<String>>();
    	
    	for (int i = 0; i < mergedVarianList.size(); i++) {
        	Map<String, Object> variantMapOuter = mergedVarianList.get(i);
        	String[] arrTrim = (String[]) variantMapOuter.get("arryTrim");
        	
			// Area 
			if (!areaList.contains(arrTrim[1]))
			{
				areaList.add(arrTrim[1]);
			}
			if (areaMap.get(arrTrim[1]) == null)
			{
				areaMap.put(arrTrim[1], getVarList(arrTrim, "area"));
			}
			// Pass 
			if (!passList.contains(arrTrim[1] + "_" + arrTrim[2]))
			{
				passList.add(arrTrim[1] + "_" + arrTrim[2]);
			}
			if (passMap.get(arrTrim[1] + "_" + arrTrim[2]) == null)
			{
				passMap.put(arrTrim[1] + "_" + arrTrim[2], getVarList(arrTrim, "pass"));
			}
			// Engine
			if (!engineList.contains(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3]))
			{
				engineList.add(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3]);
			}
			if (engineMap.get(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3]) == null)
			{
				engineMap.put(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3], getVarList(arrTrim, "engine"));
			}
			// Grade
			if (!gradeList.contains(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4]))
			{
				gradeList.add(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4]);
			}
			if (gradeMap.get(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4]) == null)
			{
				gradeMap.put(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4], getVarList(arrTrim, "grade"));
			}
		}
		
		// 3.Option 병합
        // 3-1.Option Src
		mergedOptionMap = new HashMap<String, List<Option>>();
		mergedOptionList = new ArrayList<String>();
		List<Option> srcOptionList = sourceOSpecTable.getOspec().getOptionList();
		for (int i = 0; i < srcOptionList.size(); i++) {
			
			Option srcOption = srcOptionList.get(i);

            if (!mergedOptionList.contains(srcOption.getOpValue()))
            	mergedOptionList.add(srcOption.getOpValue());
            
            // Base Spec 생성을 위하여 Src Option 이 'S'(Standard) 인 경우, 'O'(Option) 으로 변경함.
            if (srcOption.getValue() != null && srcOption.getValue().equals("S"))
            {
            	srcOption.setValue("O");
            }
                        
            if (mergedOptionMap.get(srcOption.getOpValue()) == null)
			{
				List<Option> optionList = new ArrayList<Option>();
				for (int j = 0; j < srcOptionList.size(); j++) {
					if (srcOption.getOpValue().equals(srcOptionList.get(j).getOpValue()))
					{
						optionList.add(srcOptionList.get(j));
					}
				}
				mergedOptionMap.put(srcOption.getOpValue(), optionList);
			}
		}
		
        // 3-2.Option Tgt
		List<Option> tgtOptionList = targetOSpecTable.getOspec().getOptionList();
		for (int i = 0; i < tgtOptionList.size(); i++) {
			
			Option tgtOption = tgtOptionList.get(i);
			
            if (!mergedOptionList.contains(tgtOption.getOpValue()))
            	mergedOptionList.add(tgtOption.getOpValue());
			
			if (mergedOptionMap.get(tgtOption.getOpValue()) == null)
			{
				// Option 이 존재하지 않으면 채롭게 Map에 추가한다.
				List<Option> optionList = new ArrayList<Option>();
				for (int j = 0; j < tgtOptionList.size(); j++) {
					if (tgtOption.getOpValue().equals(tgtOptionList.get(j).getOpValue()))
					{
						optionList.add(tgtOptionList.get(j));
					}
				}
				mergedOptionMap.put(tgtOption.getOpValue(), optionList);
			}
			else
			{
				// Option 이 존재하면 Option Value 가 서로 상이한지를 비교하여 Option Value를 Target 기준으로 변경한다.
				List<Option> optionList = mergedOptionMap.get(tgtOption.getOpValue());
				for (int j = 0; j < optionList.size(); j++) {
					Option option = optionList.get(j);
					if (option.getOpTrim().getTrim().equals(tgtOption.getOpTrim().getTrim()))
					{
						// Target 기준으로 옵션 값을 Update 함.
						option.setValue(tgtOption.getValue());
						/*
						if (option.getValue().equals("-") && !tgtOption.getValue().equals("-"))
						{
							option.setValue(tgtOption.getValue());
						} else if (!option.getValue().equals("S") && tgtOption.getValue().equals("S"))
						{
							option.setValue(tgtOption.getValue());
						*/
					}
				}
			}
		}
		
        // 3-3. Option Variant 병합
		Iterator<String> mergedOptionKeys = mergedOptionMap.keySet().iterator();
    	while(mergedOptionKeys.hasNext()){
    		
    		String optionKey = mergedOptionKeys.next();
			List<Option> optionList = mergedOptionMap.get(optionKey);
			
			for (int i = 0; i < mergedVarianList.size(); i++) {
				Map<String, Object> variantMap = mergedVarianList.get(i);
	        	String findingVariantID = (String) variantMap.get("trim");
	        	
	        	boolean isFound = false;
	        	for (int j = 0; j < optionList.size(); j++) {
					if (findingVariantID.equals(optionList.get(j).getOpTrim().getTrim()))
					{
						isFound = true;
						break;
					}
				}
	        	
	        	if (!isFound)
				{
	        		for (int j = 0; j < srcOptionList.size(); j++) {
	        			Option option = srcOptionList.get(j);
						if (optionKey.equals(option.getOpValue()) && option.getOpTrim().getTrim().equals(findingVariantID))
						{
							optionList.add(option);
						}
	        		}
	        		for (int j = 0; j < tgtOptionList.size(); j++) {
	        			Option option = tgtOptionList.get(j);
						if (optionKey.equals(option.getOpValue()) && option.getOpTrim().getTrim().equals(findingVariantID))
						{
							optionList.add(option);
						}
	        		}
				}
	        	
			}
    	}
    	
    	// Debugging
    	/*
    	Iterator<String> tmpOptionKeys = mergedOptionMap.keySet().iterator();
        while(tmpOptionKeys.hasNext()){
			
    		String optionKey = tmpOptionKeys.next();
			List<Option> optionList = mergedOptionMap.get(optionKey);
			
			for (int j = 0; j < optionList.size(); j++) {
				System.out.println( optionKey + "=" + optionList.get(j).getOpTrim().getTrim());
			}
		}
    	
//        for (int i = 0; i < mergedOptionList.size(); i++) {
//        	Integer order = (Integer) mergedOptionList.get(i).get("order");
//        	String option = (String) mergedOptionList.get(i).get("option");
//        	System.out.println(order + ":" + option);
//		}
        */
    	
		// 5. CategoryGroup List 생성
		mergedCatGroupList =  new ArrayList<String>();
		for (int i = 0; i < mergedOptionList.size(); i++) {
			String option = (String) mergedOptionList.get(i);
			if (!mergedCatGroupList.contains(option.substring(0, 1)))
			{
				mergedCatGroupList.add(option.substring(0, 1));
			}
		}
		
		// 6. Option 정렬
		sortByOption(mergedOptionList);
		
		return 0;
    }
    
    /**
     * export xls
     * @param file
     * @return
     * @throws Exception
     */
    public int export(File file) throws Exception
    {
    	int rowAccessWindowSize = 50;
    	
		Workbook workbook = new SXSSFWorkbook(rowAccessWindowSize);
        Sheet sheet = createFirstSheet(workbook, mergedOspecNo);
        PrintSetup print = sheet.getPrintSetup();

    	this.cellStyles = createCellStyles(workbook);
    	
        // 1. 헤더 컬럼 생성
    	List<List<String>> labelRowList = getHeaderColumns();
        if (labelRowList != null)
        {
            this.headerLabelRowCount = labelRowList.size();
            for (int i = 0; i < labelRowList.size(); i++)
            {
            	List<String> args = (List<String>) labelRowList.get(i);
                createColumnRow(workbook, sheet, args, i);
            }
        }
        
        // 3. 헤더 셀 병합
        setMergedRegion(sheet, getHeaderMargeInfoList());
        
        // 4. 데이터 생성
    	List<List<String>> dataRowList = getDataColumns();
        if (dataRowList != null)
        {
            for (int i = 0; i < dataRowList.size(); i++)
            {
            	List<String> args = (List<String>) dataRowList.get(i);
                createColumnRow(workbook, sheet, args, headerLabelRowCount + i);
            }
        }
        
        // 5. 헤더 셀 병합
        setMergedRegion(sheet, getDataMargeInfoList());        

    	FileOutputStream fos = new FileOutputStream(file);
    	workbook.write(fos);
    	fos.close();

    	return 0;
    }
    
    /**
     * 시트 생성
     */
    private Sheet createFirstSheet(Workbook workbook, String sheetName)
    {
        Sheet sheet = workbook.createSheet();
        workbook.setSheetName(0, sheetName);
        return sheet;
    }
    
    /**
     * 스타일 생성
     */
    private Map<String, XSSFCellStyle> createCellStyles(Workbook workbook)
    {
        Map<String, XSSFCellStyle> styleMap = new HashMap<String, XSSFCellStyle>();
        
        XSSFCellStyle style;
        
        Font titleFont = workbook.createFont();
        titleFont.setFontHeightInPoints((short) 11);
        titleFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
        titleFont.setFontName("Tahoma");
        
        Font headerFont = workbook.createFont();
        headerFont.setFontHeightInPoints((short) 11);
        headerFont.setFontName("Tahoma");
        
        Font dataFont = workbook.createFont();
        dataFont.setFontHeightInPoints((short) 8);
        dataFont.setFontName("Tahoma");
        
        XSSFColor hdBgColor = new XSSFColor(new java.awt.Color(200, 200, 200));
        XSSFColor borderColor = new XSSFColor(new java.awt.Color(0, 0, 0));
        
        //title style
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_LEFT);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.index);
        style.setFillPattern(CellStyle.ALIGN_LEFT);
        style.setFont(titleFont);
        style.setWrapText(false);
        styleMap.put("titleStyle", style);
        
        
        //header style
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(hdBgColor);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setFont(headerFont);
        style.setWrapText(true);

        styleMap.put("headerStyle", style);
        
        //data style
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(IndexedColors.WHITE.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setFont(dataFont);
        style.setWrapText(true);
        styleMap.put("dataStyle", style);
        
        //data + bg
        style = (XSSFCellStyle) workbook.createCellStyle();
        style.setAlignment(CellStyle.ALIGN_CENTER);
        style.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        style.setFillForegroundColor(hdBgColor);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        style.setBorderBottom(CellStyle.BORDER_THIN);
        style.setBottomBorderColor(borderColor);
        style.setBorderLeft(CellStyle.BORDER_THIN);
        style.setLeftBorderColor(borderColor);
        style.setBorderRight(CellStyle.BORDER_THIN);
        style.setRightBorderColor(borderColor);
        style.setBorderTop(CellStyle.BORDER_THIN);
        style.setTopBorderColor(borderColor);
        style.setFont(dataFont);
        style.setWrapText(true);
        styleMap.put("dataBGStyle", style);
        
        return styleMap;
    }
        
    /**
     * Header 리스트
     */
    public List<List<String>> getHeaderColumns() throws Exception
    {
        List<List<String>> labelRowList = new ArrayList();
        List<String> columnList = null ;
        OSpec srcOspec = sourceOSpecTable.getOspec();
        OSpec tgtOspec = targetOSpecTable.getOspec();
        
        //Row No. 1
        columnList = new ArrayList<String>();
        columnList.add("OSPEC_Version_Detail_(" + mergedOspecNo + ")");
        labelRowList.add(columnList);
        
        //Row No. 2
        columnList = new ArrayList<String>();
        columnList.add("");
        labelRowList.add(columnList);

        //Row No. 3
        columnList = new ArrayList<String>();
        columnList.add(srcOspec.getProject() + "+" + tgtOspec.getProject() + " PRODUCTION ORDERING SPECIFICATION");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("Drv" + crLf + "Type");
        columnList.add("All");
        
		for (int i = 0; i < areaList.size(); i++) {
			String area = areaList.get(i);
			List<String> variantList = areaMap.get(area);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(area.split("_")[0]);
			}
		}
        
        columnList.add("Eff-IN");
        columnList.add("REMARK");
        labelRowList.add(columnList);
        
        //Row No. 4
        columnList = new ArrayList<String>();
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        
		for (int i = 0; i < passList.size(); i++) {
			String pass = passList.get(i);
			List<String> variantList = passMap.get(pass);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(pass.split("_")[1]);
			}
		}

        columnList.add("");
        columnList.add("");
        labelRowList.add(columnList);
        
        //Row No. 5
        columnList = new ArrayList<String>();
        columnList.add("OSI-No:" + srcOspec.getOspecNo() + "_" + tgtOspec.getOspecNo());
        columnList.add("");
        
        SimpleDateFormat befDateformat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat aftDateformat = new SimpleDateFormat("yyyy-MM-dd");
        Date srcReleasdDate = befDateformat.parse(srcOspec.getReleasedDate());
        Date tgtReleasdDate = befDateformat.parse(tgtOspec.getReleasedDate());
        
        columnList.add((srcReleasdDate.compareTo(tgtReleasdDate) > 0 ? aftDateformat.format(srcReleasdDate) : aftDateformat.format(tgtReleasdDate)) + " Released");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");

		for (int i = 0; i < engineList.size(); i++) {
			String engine = engineList.get(i);
			List<String> variantList = engineMap.get(engine);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(engine.split("_")[2]);
			}
		}

        columnList.add("");
        columnList.add("S: Standard" + crLf + "O*: Option" + crLf + "M*: Mandatory");
        labelRowList.add(columnList);

        //Row No. 6
        columnList = new ArrayList<String>();
        columnList.add("Category Option");
        columnList.add("");
        columnList.add("");
        columnList.add("Code");
        columnList.add("P/Opt");
        columnList.add("");
        columnList.add("");

		for (int i = 0; i < gradeList.size(); i++) {
			String grade = gradeList.get(i);
			List<String> variantList = gradeMap.get(grade);
			for (int j = 0; j < variantList.size(); j++) {
				columnList.add(grade.split("_")[3]);
			}
		}

        columnList.add("");
        columnList.add("");
        labelRowList.add(columnList);

        //Row No. 7
        columnList = new ArrayList<String>();
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");
        columnList.add("");

		for (int i = 0; i < gradeList.size(); i++) {
			String grade = gradeList.get(i);
			for (int j = 0; j < mergedVarianList.size(); j++) {
				Map<String, Object> variantMap = mergedVarianList.get(j);
				String trim = (String) variantMap.get("trim");
	        	String[] arrTrim = (String[]) variantMap.get("arryTrim");
	        	if (grade.equals(arrTrim[1] + "_" + arrTrim[2] + "_" + arrTrim[3] + "_" + arrTrim[4]))
	        		columnList.add(trim);
			}							
		}
        
        columnList.add("");
        columnList.add("");
        labelRowList.add(columnList);

        return labelRowList;
    }    
    
    /**
     * Data 리스트
     */
    public List<List<String>> getDataColumns() throws Exception
    {
        List<List<String>> dataRowList = new ArrayList<List<String>>();
        List<String> columnList = null ;
        
        for (int i = 0; i < mergedCatGroupList.size(); i++) {

			// Category Group
        	columnList = new ArrayList<String>();
			String categoryGroup = mergedCatGroupList.get(i);
			columnList.add(categoryGroup);
			columnList.add(getCatGroupName(categoryGroup));
			columnList.add("");
			columnList.add("");
			columnList.add("");
			columnList.add("");
			columnList.add("");

			for (int j = 0; j < mergedVarianList.size(); j++) {
				columnList.add("");
			}				
			
			columnList.add("");
			columnList.add("");
        	
			dataRowList.add(columnList);

			// Option 
			for (int j = 0; j < mergedOptionList.size(); j++) {
				
				String optionValue = (String) mergedOptionList.get(j);
				if (!optionValue.startsWith(categoryGroup))
					continue;
				
				List<Option> optionList = mergedOptionMap.get(optionValue);
				
				// option row
				columnList = new ArrayList<String>();
				columnList.add("");
				columnList.add(optionList.get(0).getOpName());
				columnList.add(optionList.get(0).getOpValueName());
				columnList.add(optionList.get(0).getOpValue());
				columnList.add(optionList.get(0).getPackageName());
				columnList.add(optionList.get(0).getDriveType());
				columnList.add(optionList.get(0).getAll());
				
				for (int k = 0; k < mergedVarianList.size(); k++) {
					Map<String, Object> variantMap = mergedVarianList.get(k);
					String variantID = (String) variantMap.get("trim"); 
					boolean isfound = false;
					for (int m = 0; m < optionList.size(); m++) {
						if (variantID.equals(optionList.get(m).getOpTrim().getTrim()))
						{
							columnList.add(optionList.get(m).getValue()); 
							isfound = true;
							break;
						}
					}
					if (!isfound)
					{
						columnList.add("-");
					}
				}
				
				columnList.add(optionList.get(0).getEffIn());
				columnList.add(optionList.get(0).getRemark());
				
				dataRowList.add(columnList);
			}
		}
        
        return dataRowList;
    }    
    
    /**
     * 컬럼 생성
     */
    private void createColumnRow(Workbook workbook, Sheet sheet, List<String> args, int rowNum)
    {
        Row row = sheet.createRow((short) rowNum);
        Cell cell;
        
        row.setHeightInPoints(getRowHeight(rowNum, 0, sheet));

        for (int i = 0; i < args.size(); i++)
        {
            cell = row.createCell(i);
            
            String cellData = (String) args.get(i);
            if (cellData != null)
            {
                cell.setCellValue(cellData);
                cell.setCellStyle(this.cellStyles.get(getCellStyle(rowNum, i)));
            	sheet.setColumnWidth(cell.getColumnIndex(), getColumnWidth(rowNum, cell.getColumnIndex(), sheet));                
                cell.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
    }
        
    /**
     * Get Cell Style
     * @param row
     * @param col
     * @return
     */
    private String getCellStyle(int row, int col)
    {
        String styleName = "";
        
        if (row >= headerLabelRowStart && row < headerLabelRowCount)
        {
            styleName = "headerStyle";
        }
        else if (row < headerLabelRowStart)
        {
        	styleName = "titleStyle";
        }
        else
        {
            if (col < dataLabelColumnCount)
                styleName = "dataBGStyle";
            else
            	styleName = "dataStyle";
        }
        
        return styleName;
    }    
    
    /**
     * Get Row Height
     * @param row
     * @param col
     * @return
     */
    private float getRowHeight(int row, int col, Sheet sheet)
    {
    	float height = 0.0f;
        
        if (row >= headerLabelRowStart && row < headerLabelRowCount)
        {
            height = 42.75f;
        }
        else if (row < headerLabelRowStart)
        {
        	//height = sheet.getDefaultRowHeightInPoints();
        	height = 16.5f;
        }
        else
        {
        	height = 16.5f;
        	//height = sheet.getRow(row).getHeight() * 5;
        }
        
        return height;
    }    

    /**
     * Get Row Height
     * @param row
     * @param col
     * @return
     */
    private int getColumnWidth(int row, int col, Sheet sheet)
    {
    	int width = 0;
    	int colindex = 0;
    	
    	switch (col) {
		case 0:
		case 3:
		case 4:
		case 5:
		case 6:
			width = 5*256;
			break;
		case 1:
		case 2:
			width = 12*256;
			break;
		default:
			width = 8*256;
			break;
		}
    	
    	colindex = dataColumnStart + mergedVarianList.size();
    	// Eff-IN
    	if (col == colindex)
    	{
    		width = 8*256;
    	}
    	
    	// REMARK
    	if (col == colindex + 1)
    	{
    		width = 50*256;
    	}    	
    	
        return width;
    }    
    
    /**
     * Column, Row Information of Cell to Merge
     * int firstRow, int lastRow, int firstCol, int lastCol
     * @return
     * @throws Exception
     */
    public List<Map<Integer, Integer>> getHeaderMargeInfoList() throws Exception
    {
        int startColVariant = 0;
        Map<Integer, Integer> cell = null;
        List<Map<Integer, Integer>> mergeInfo = new ArrayList<Map<Integer, Integer>>();
        
        // PRODUCTION ORDERING SPECIFICATION
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 2);
        cell.put(1, 3);
        cell.put(2, 0);
        cell.put(3, 4);
        mergeInfo.add(cell);
        
        // Drv Type
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 2);
        cell.put(1, 6);
        cell.put(2, 5);
        cell.put(3, 5);
        mergeInfo.add(cell);
        
        // All
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 2);
        cell.put(1, 6);
        cell.put(2, 6);
        cell.put(3, 6);
        mergeInfo.add(cell);
        
        // OSI-No: 
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 4);
        cell.put(1, 4);
        cell.put(2, 0);
        cell.put(3, 1);
        mergeInfo.add(cell);
        
        // Released
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 4);
        cell.put(1, 4);
        cell.put(2, 2);
        cell.put(3, 4);
        mergeInfo.add(cell);
        
        // Category Option
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 5);
        cell.put(1, 6);
        cell.put(2, 0);
        cell.put(3, 2);
        mergeInfo.add(cell);
        
        // Code
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 5);
        cell.put(1, 6);
        cell.put(2, 3);
        cell.put(3, 3);
        mergeInfo.add(cell);
        
        // P/Opt
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 5);
        cell.put(1, 6);
        cell.put(2, 4);
        cell.put(3, 4);
        mergeInfo.add(cell);
        
        // Area
        startColVariant = 7;
		for (int i = 0; i < areaList.size(); i++) {
			String area = areaList.get(i);
			List<String> variantList = areaMap.get(area);

			cell = new HashMap<Integer, Integer>();
            cell.put(0, 2);
            cell.put(1, 2);
            cell.put(2, startColVariant);
            cell.put(3, startColVariant + variantList.size() - 1);
            
            startColVariant = startColVariant + variantList.size();
            
            mergeInfo.add(cell);
		}
        
        // Passenger
        startColVariant = 7;
		for (int i = 0; i < passList.size(); i++) {
			String pass = passList.get(i);
			List<String> variantList = passMap.get(pass);

			cell = new HashMap<Integer, Integer>();
            cell.put(0, 3);
            cell.put(1, 3);
            cell.put(2, startColVariant);
            cell.put(3, startColVariant + variantList.size() - 1);
            
            startColVariant = startColVariant + variantList.size();
            
            mergeInfo.add(cell);
		}
		
        // Engine
        startColVariant = 7;
		for (int i = 0; i < engineList.size(); i++) {
			String engine = engineList.get(i);
			List<String> variantList = engineMap.get(engine);

			cell = new HashMap<Integer, Integer>();
            cell.put(0, 4);
            cell.put(1, 4);
            cell.put(2, startColVariant);
            cell.put(3, startColVariant + variantList.size() - 1);
            
            startColVariant = startColVariant + variantList.size();
            
            mergeInfo.add(cell);
		}

        // Grade
        startColVariant = 7;
		for (int i = 0; i < gradeList.size(); i++) {
			String grade = gradeList.get(i);
			List<String> variantList = gradeMap.get(grade);

			cell = new HashMap<Integer, Integer>();
            cell.put(0, 5);
            cell.put(1, 5);
            cell.put(2, startColVariant);
            cell.put(3, startColVariant + variantList.size() - 1);
            
            startColVariant = startColVariant + variantList.size();
            
            mergeInfo.add(cell);
		}

		// Eff-IN
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 2);
        cell.put(1, 6);
        cell.put(2, startColVariant);
        cell.put(3, startColVariant);
        mergeInfo.add(cell);
        
        // REMARK
        startColVariant = startColVariant + 1;
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 2);
        cell.put(1, 3);
        cell.put(2, startColVariant);
        cell.put(3, startColVariant);
        mergeInfo.add(cell);
        
        // S: Standard 
        // O*: Option 
        // M*: Mandatory
        cell = new HashMap<Integer, Integer>();
        cell.put(0, 4);
        cell.put(1, 6);
        cell.put(2, startColVariant);
        cell.put(3, startColVariant);
        mergeInfo.add(cell);

        return mergeInfo;
    }
    
    /**
     * Column, Row Information of Cell to Merge
     * int firstRow, int lastRow, int firstCol, int lastCol
     * @return
     * @throws Exception
     */
    public List<Map<Integer, Integer>> getDataMargeInfoList() throws Exception
    {
        int startRow = 0;
        Map<Integer, Integer> cell = null;
        List<Map<Integer, Integer>> mergeInfo = new ArrayList<Map<Integer, Integer>>();
        
        // Category
        List<String> catList = new ArrayList<String>();
		for (int i = 0; i < mergedOptionList.size(); i++) {
			String optionValue = (String) mergedOptionList.get(i);
			if (!catList.contains(OpUtil.getCategory(optionValue)))
			{
				catList.add(OpUtil.getCategory(optionValue));
			}
		}
        
        // Category Group
    	startRow = dataRowsStart;
        for (int i = 0; i < mergedCatGroupList.size(); i++) {
        	
        	int catGroupCnt = 0;
			String categoryGroup = mergedCatGroupList.get(i);
			
			for (int j = 0; j < mergedOptionList.size(); j++) {
				String optionValue = (String) mergedOptionList.get(j);
				if (optionValue.startsWith(categoryGroup))
				{
					catGroupCnt ++;
				}
			}
			
			// Category Group
			cell = new HashMap<Integer, Integer>();
	        cell.put(0, startRow);
	        cell.put(1, startRow + catGroupCnt);
	        cell.put(2, 0);
	        cell.put(3, 0);
	        mergeInfo.add(cell);
			
	        // Category Group Name
	        cell = new HashMap<Integer, Integer>();
	        cell.put(0, startRow);
	        cell.put(1, startRow);
	        cell.put(2, 1);
	        cell.put(3, 3);
	        mergeInfo.add(cell);
	        
	        // Category Name
	        int catStartRow = startRow + 1;
			for (int j = 0; j < catList.size(); j++) {
				String category = catList.get(j);

				int catCnt = 0;
				if (category.startsWith(categoryGroup))
				{
					for (int k = 0; k < mergedOptionList.size(); k++) {
						String optionValue = (String) mergedOptionList.get(k);
						if (category.equals(OpUtil.getCategory(optionValue)))
						{
							catCnt ++;
						}
					}
					
					if (catCnt > 1)
					{
				        cell = new HashMap<Integer, Integer>();
				        cell.put(0, catStartRow);
				        cell.put(1, catStartRow + catCnt - 1);
				        cell.put(2, 1);
				        cell.put(3, 1);
				        mergeInfo.add(cell);
					}
					
					catStartRow = catStartRow + catCnt;
				}
			}
			
        	startRow = startRow + 1 + catGroupCnt;
			
        }
        
        return mergeInfo;
    }

    /**
     * Set Cell Merge
     * @param sheet
     * @param args
     */
    private void setMergedRegion(Sheet sheet, List<Map<Integer, Integer>> args)
    {
        for (int i = 0; i < args.size(); i++)
        {
            Map<Integer, Integer> cell = args.get(i);
            sheet.addMergedRegion(new CellRangeAddress(cell.get(0), cell.get(1), cell.get(2), cell.get(3)));
        }
    }   
    /**
     * Category Group Name
     * from H-BOM 
     * @param catgroup
     * @return
     */
    public String getCatGroupName(String catgroup)
    {
    	String catgroupName = null;
    	
    	if (catgroup.equals("2"))
		{
    		catgroupName = "M/YEAR";
		} else if (catgroup.equals("3"))
		{
			catgroupName = "SPECIAL COUNTRY";
		} else if (catgroup.equals("4"))
		{
			catgroupName = "CLIMATE";
		} else if (catgroup.equals("A"))
		{
			catgroupName = "VEHICLE MODEL";
		} else if (catgroup.equals("B"))
		{
			catgroupName = "DRIVE";
		} else if (catgroup.equals("C"))
		{
			catgroupName = "ENGINE";
		} else if (catgroup.equals("D"))
		{
			catgroupName = "FUEL SYSTEM";
		} else if (catgroup.equals("E"))
		{
			catgroupName = "DRIVE TRAIN";
		} else if (catgroup.equals("F"))
		{
			catgroupName = "STEERING";
		} else if (catgroup.equals("G"))
		{
			catgroupName = "SUSPENSION";
		} else if (catgroup.equals("H"))
		{
			catgroupName = "BREAK";
		} else if (catgroup.equals("J"))
		{
			catgroupName = "WHEEL & TIRE";
		} else if (catgroup.equals("K"))
		{
			catgroupName = "EXTERIOR";
		} else if (catgroup.equals("L"))
		{
			catgroupName = "GLAZING";
		} else if (catgroup.equals("M"))
		{
			catgroupName = "INTERIOR";
		} else if (catgroup.equals("N"))
		{
			catgroupName = "SEAT";
		} else if (catgroup.equals("Q"))
		{
			catgroupName = "RESTRAINT";
		} else if (catgroup.equals("R"))
		{
			catgroupName = "INSTRUMENT";
		} else if (catgroup.equals("S"))
		{
			catgroupName = "ELELTRICS";
		} else if (catgroup.equals("T"))
		{
			catgroupName = "HVAC";
		} else if (catgroup.equals("U"))
		{
			catgroupName = "AUDIO & VIDEO";
		} else if (catgroup.equals("X"))
		{
			catgroupName = "Production Interface";
		} else if (catgroup.equals("Z"))
		{
			catgroupName = "Miscellaneous";
		} else
		{
			catgroupName = "Not Found";
		}
			
    	return catgroupName;    	
    }    
    
    /**
     * Variant 를 Sort Order 순으로 정렬한다.
     * @param mergedVariantMap
     */
    public void sortByVariant(Map<String, String[]> mergedVariantMap)
    {
    	Map<String, Object> sortOrderMap = null;
    	mergedVarianList = new ArrayList<Map<String, Object>>();
        String[] keys = mergedVariantMap.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
        	
            String[] arrTrim = mergedVariantMap.get(keys[i]);
        	String sortOrder = String.format("%04d", Integer.parseInt(arrTrim[0])) + keys[i];
        	
        	sortOrderMap = new HashMap<String, Object>();
        	sortOrderMap.put("order", getSortOrder(arrTrim[1], "area") + "_" + 
        	                          getSortOrder(arrTrim[2], "pass") + "_" + 
        			                  getSortOrder(arrTrim[3], "engine") + "_" + 
        	                          getSortOrder(arrTrim[4], "grade") + "_" + sortOrder);
        	sortOrderMap.put("trim", keys[i]);
        	sortOrderMap.put("arryTrim", arrTrim);
        	
        	mergedVarianList.add(sortOrderMap);
        }
        
        MapComparator comp = new MapComparator("order");        
    	Collections.sort(mergedVarianList, comp);     
        
    	System.out.println(mergedVarianList);
    }
    
    /**
     * 옵션을 Sort Order 순으로 정렬한다.
     * @param mergedOptionMap
     */
    public void sortByOption(List<String> mergedOptionList)
    {        
        StringComparator comp = new StringComparator();        
        Collections.sort(mergedOptionList, comp);     
        
    	System.out.println(mergedOptionList);
    }

    /**
     * 옵션을 Sort Order 순으로 정렬한다.
     * @param mergedOptionMap
     */
    /*
    public void sortByOption(Map<String, List<Option>> mergedOptionMap)
    {        
    	Map<String, Object> sortOrderMap = null;
    	List<Map<String, Object>> tmpOptionList = new ArrayList<Map<String, Object>>();
    	String[] keys = mergedOptionMap.keySet().toArray(new String[0]);
        for (int i = 0; i < keys.length; i++) {
        	
            List<Option> optionList = mergedOptionMap.get(keys[i]);
            
        	sortOrderMap = new HashMap<String, Object>();
        	sortOrderMap.put("order", keys[i]);
        	sortOrderMap.put("option", keys[i]);
        	sortOrderMap.put("optionlist", optionList);
        	
        	tmpOptionList.add(sortOrderMap);
        }
        
        MapComparator comp = new MapComparator("order");        
    	Collections.sort(tmpOptionList, comp);     
        
    	System.out.println(tmpOptionList);
    }
	*/
    
    /**
     * O/Spec Header 병합을 위한 각 항목별 Variant List 생성
     * @param value
     * @param flag
     * @return
     */
    public List<String> getVarList(String[] _arrTrim, String flag)
    {
    	List<String> varList = new ArrayList<String>();
    	for (int i = 0; i < mergedVarianList.size(); i++) {
    		
        	Map<String, Object> variantMap = mergedVarianList.get(i);
        	String variantId = (String) variantMap.get("trim");
        	String[] arrTrim = (String[]) variantMap.get("arryTrim");
        	
        	if (flag == "area")
        	{
        		if ( _arrTrim[1].equals(arrTrim[1]) )
    			{
        			varList.add(variantId);
    			}
        	} else if (flag == "pass")
        	{
        		if ( _arrTrim[1].equals(arrTrim[1]) &&
        			 _arrTrim[2].equals(arrTrim[2]) )
    			{
        			varList.add(variantId);
    			}

        	} else if (flag == "engine")
        	{
        		if ( _arrTrim[1].equals(arrTrim[1]) &&
           			 _arrTrim[2].equals(arrTrim[2]) &&
           			 _arrTrim[3].equals(arrTrim[3]) )
    			{
        			varList.add(variantId);
    			}
        	} else if (flag == "grade")
        	{
        		if ( _arrTrim[1].equals(arrTrim[1]) &&
          			 _arrTrim[2].equals(arrTrim[2]) &&
           			 _arrTrim[3].equals(arrTrim[3]) &&
           			 _arrTrim[4].equals(arrTrim[4]) )
    			{
        			varList.add(variantId);
    			}
        	}
    	}
        	
        return varList;
    }
    
    /**
     * Variant Header Sort 순서 
     * @param value
     * @param flag
     * @return
     */
    public String getSortOrder(String value, String flag)
    {
    	if (flag == "area")
    	{
    		return value;
    	} else if (flag == "pass")
    	{
    		value = value.replaceAll("PASS", "").trim();
    		value = String.format("%04d", Integer.parseInt(value)) + " PASS";
    		return value;

    	} else if (flag == "engine")
    	{
    		return value;
    	} else if (flag == "grade")
    	{
    		if (value == "STD")
    		{
    			return "001";
    		} else if (value == "DLX")
    		{
    			return "002";
    		} else if (value == "H/DLX")
    		{
    			return "003";
    		} else
    		{
    			return "999";
    		}
    	} else
    	{
    		return value;
    	}
    }
    
    class MapComparator implements Comparator<Map<String, Object>> 
    {     
    	private final String key;        
    	
    	public MapComparator(String key) 
    	{        
    		this.key = key;    
    	}        
    	
    	@Override    
    	public int compare(Map<String, Object> first, Map<String, Object> second) 
    	{        
    		int result = ((String) first.get(key)).compareTo((String) second.get(key));
    		return result;    
    	}
    }     

    class StringComparator implements Comparator<String> 
    {     
    	@Override    
    	public int compare(String first, String second) 
    	{        
    		int result = first.compareTo(second);
    		return result;    
    	}
    }     

}
