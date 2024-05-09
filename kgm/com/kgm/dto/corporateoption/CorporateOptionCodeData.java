package com.kgm.dto.corporateoption;

import java.text.DecimalFormat;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;

/**
 * Corporate Option Code Data (Sheet 0)
 * @author jclee
 *
 */
public class CorporateOptionCodeData {
	/** Excel Start Row */
	public static final int START_ROW = 3;
	
	/** Excel Col Index */
	public static final int COL_GROUP_CODE = 1;			// Group Code
	public static final int COL_GROUP_NAME = 2;         // Group Name
	public static final int COL_ITEM_NO = 3;            // Item No
	public static final int COL_ITEM_NAME = 4;          // Item Name
	public static final int COL_MEO = 5;                // MEO (Mutually Exclusive)
	public static final int COL_CODE = 6;               // Code
	public static final int COL_NAME = 7;               // Name
	public static final int COL_REMARK = 8;             // Remark
	public static final int COL_CREATE_DATE = 9;        // Create Date
	public static final int COL_LAST_CHANGE = 10;       // Last Change
	
	private String sGroupCode = "";						
	private String sGroupName = "";
	private String sItemNo = "";
	private String sItemName = "";
	private String sMEO = "";
	private String sCode = "";
	private String sName = "";
	private String sRemark = "";
	private String sCreateDate = "";
	private String sLastChange = "";
	
	/**
	 * Constructor
	 * @param row
	 */
	public CorporateOptionCodeData(Row row) {
		Cell cGroupCode = row.getCell(CorporateOptionCodeData.COL_GROUP_CODE);
		Cell cGroupName = row.getCell(CorporateOptionCodeData.COL_GROUP_NAME);
		Cell cItemNo = row.getCell(CorporateOptionCodeData.COL_ITEM_NO);
		Cell cItemName = row.getCell(CorporateOptionCodeData.COL_ITEM_NAME);
		Cell cMEO = row.getCell(CorporateOptionCodeData.COL_MEO);
		Cell cCode = row.getCell(CorporateOptionCodeData.COL_CODE);
		Cell cName = row.getCell(CorporateOptionCodeData.COL_NAME);
		Cell cRemark = row.getCell(CorporateOptionCodeData.COL_REMARK);
		Cell cCreateDate = row.getCell(CorporateOptionCodeData.COL_CREATE_DATE);
		Cell cLastChange = row.getCell(CorporateOptionCodeData.COL_LAST_CHANGE);
		
		String sGroupCode = getCellText(cGroupCode);
		String sGroupName = getCellText(cGroupName);
		String sItemNo = getCellText(cItemNo);
		String sItemName = getCellText(cItemName);
		String sMEO = getCellText(cMEO);
		String sCode = getCellText(cCode);
		String sName = getCellText(cName);
		String sRemark = getCellText(cRemark);
		String sCreateDate = getCellText(cCreateDate);
		String sLastChange = getCellText(cLastChange);
		
		setGroupCode(sGroupCode);
		setGroupName(sGroupName);
		setItemNo(sItemNo);
		setItemName(sItemName);
		setMEO(sMEO);
		setCode(sCode);
		setName(sName);
		setRemark(sRemark);
		setCreateDate(sCreateDate);
		setLastChange(sLastChange);
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
	
	public String getGroupCode() {
		return sGroupCode;
	}
	public void setGroupCode(String sGroupCode) {
		this.sGroupCode = sGroupCode;
	}
	public String getGroupName() {
		return sGroupName;
	}
	public void setGroupName(String sGroupName) {
		this.sGroupName = sGroupName;
	}
	public String getItemNo() {
		return sItemNo;
	}
	public void setItemNo(String sItemNo) {
		this.sItemNo = sItemNo;
	}
	public String getItemName() {
		return sItemName;
	}
	public void setItemName(String sItemName) {
		this.sItemName = sItemName;
	}
	public String getMEO() {
		return sMEO;
	}
	public void setMEO(String sMEO) {
		this.sMEO = sMEO;
	}
	public String getCode() {
		return sCode;
	}
	public void setCode(String sCode) {
		this.sCode = sCode;
	}
	public String getName() {
		return sName;
	}
	public void setName(String sName) {
		this.sName = sName;
	}
	public String getRemark() {
		return sRemark;
	}
	public void setRemark(String sRemark) {
		this.sRemark = sRemark;
	}
	public String getCreateDate() {
		return sCreateDate;
	}
	public void setCreateDate(String sCreateDate) {
		this.sCreateDate = sCreateDate;
	}
	public String getLastChange() {
		return sLastChange;
	}
	public void setLastChange(String sLastChange) {
		this.sLastChange = sLastChange;
	}
}