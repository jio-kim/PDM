package com.kgm.dto.corporateoption;

import java.text.DecimalFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;

/**
 * Corporate Option History Data (Sheet 3)
 * @author jclee
 *
 */
public class CorporateOptionHistoryData {
	/** Excel Start Row */
	public static final int START_ROW = 6;
	
	/** Excel Col Index */
	public static final int COL_NO = 2;							// No
	public static final int COL_REQ_DATE = 3;                   // 요청일
	public static final int COL_REQ_DESIGNER_USER = 4;          // 요청자(설계담당자)
	public static final int COL_DATE = 5;                       // Date(생성/변경 날짜 : H-BOM 등록 기준)
	public static final int COL_CHANGE_CODE = 6;                // 변경코드
	public static final int COL_CHANGE_CAUSE = 7;               // 변경내용(New, Add, Name, Change)
	public static final int COL_DESC = 8;                       // Option Code Name(변경내용)
	public static final int COL_REQ_ENGINEERING_USER = 9;       // 변경의뢰(기관담당자)
	public static final int COL_REMARK = 10;                    // 비고(생성 이유/목적)
	
	private String sNo = "";	
	private String sReqDate = "";
	private String sReqDesignUser = "";
	private String sDate = "";
	private String sChangeCode = "";
	private String sChangeCause = "";
	private String sDesc = "";
	private String sReqEngineeringUser = "";
	private String sRemark = "";
	
	/**
	 * Constructor
	 * @param row
	 */
	public CorporateOptionHistoryData(Row row) {
		Cell cNo = row.getCell(CorporateOptionHistoryData.COL_NO);
		Cell cReqDate = row.getCell(CorporateOptionHistoryData.COL_REQ_DATE);
		Cell cReqDesignUser = row.getCell(CorporateOptionHistoryData.COL_REQ_DESIGNER_USER);
		Cell cDate = row.getCell(CorporateOptionHistoryData.COL_DATE);
		Cell cChangeCode = row.getCell(CorporateOptionHistoryData.COL_CHANGE_CODE);
		Cell cChangeCause = row.getCell(CorporateOptionHistoryData.COL_CHANGE_CAUSE);
		Cell cDesc = row.getCell(CorporateOptionHistoryData.COL_DESC);
		Cell cReqEngineeringUser = row.getCell(CorporateOptionHistoryData.COL_REQ_ENGINEERING_USER);
		Cell cRemark = row.getCell(CorporateOptionHistoryData.COL_REMARK);
		
		String sNo = getCellText(cNo);
		String sReqDate = getCellText(cReqDate);
		String sReqDesignUser = getCellText(cReqDesignUser);
		String sDate = getCellText(cDate);
		String sChangeCode = getCellText(cChangeCode);
		String sChangeCause = getCellText(cChangeCause);
		String sDesc = getCellText(cDesc);
		String sReqEngineeringUser = getCellText(cReqEngineeringUser);
		String sRemark = getCellText(cRemark);
		
		setNo(sNo);
		setReqDate(sReqDate);
		setReqDesignUser(sReqDesignUser);
		setDate(sDate);
		setChangeCode(sChangeCode);
		setChangeCause(sChangeCause);
		setDesc(sDesc);
		setReqEngineeringUser(sReqEngineeringUser);
		setRemark(sRemark);
	}
	
	/**
	 * Get Excel Text From Cell
	 * @param cell
	 * @return
	 */
	private String getCellText(Cell cell) {
        String value = "";
        if (cell != null) {
            switch (cell.getCellType()) {
                case XSSFCell.CELL_TYPE_FORMULA:
                    value = cell.getCellFormula();
                    break;
                // Integer로 Casting하여 반환함
                case XSSFCell.CELL_TYPE_NUMERIC:
                    value = "" +  getFormatedString(cell.getNumericCellValue());
                    break;
                case XSSFCell.CELL_TYPE_STRING:
                    value = "" + cell.getStringCellValue();
                    break;
                case XSSFCell.CELL_TYPE_BLANK:
                    value = "";
                    break;
                case XSSFCell.CELL_TYPE_ERROR:
                    value = "" + cell.getErrorCellValue();
                    break;
                default:
            }
        }
        return value;
    }
	
	/**
	 * Get Formated String
	 * @param value
	 * @return
	 */
	private String getFormatedString(double value)
    {
      DecimalFormat df = new DecimalFormat("#####################.####");//
      return df.format(value);
    }
	
	public String getNo() {
		return sNo;
	}
	public void setNo(String sNo) {
		this.sNo = sNo;
	}
	public String getReqDate() {
		return sReqDate;
	}
	public void setReqDate(String sReqDate) {
		this.sReqDate = sReqDate;
	}
	public String getReqDesignUser() {
		return sReqDesignUser;
	}
	public void setReqDesignUser(String sReqDesignUser) {
		this.sReqDesignUser = sReqDesignUser;
	}
	public String getDate() {
		return sDate;
	}
	public void setDate(String sDate) {
		this.sDate = sDate;
	}
	public String getChangeCode() {
		return sChangeCode;
	}
	public void setChangeCode(String sChangeCode) {
		this.sChangeCode = sChangeCode;
	}
	public String getChangeCause() {
		return sChangeCause;
	}
	public void setChangeCause(String sChangeCause) {
		this.sChangeCause = sChangeCause;
	}
	public String getDesc() {
		return sDesc;
	}
	public void setDesc(String sDesc) {
		this.sDesc = sDesc;
	}
	public String getReqEngineeringUser() {
		return sReqEngineeringUser;
	}
	public void setReqEngineeringUser(String sReqEngineeringUser) {
		this.sReqEngineeringUser = sReqEngineeringUser;
	}
	public String getRemark() {
		return sRemark;
	}
	public void setRemark(String sRemark) {
		this.sRemark = sRemark;
	}
}