package com.kgm.dto.corporateoption;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;

/**
 * Corporate Option Class
 *  - CorporateOption Item�� Latest Revision�� ����� Dataset NamedReference �� �������ڰ� �ֽ��� Excel File�� �о� ����
 * 
 * @author jclee
 *
 */
public class CorporateOptionData {
	private ArrayList<CorporateOptionCodeData> alCorporateOptionCodeList;
	private ArrayList<CorporateOptionHistoryData> alCorporateOptionHistoryList;
	private TCComponentItemRevision cCorporateOptionItemRevision;
	private File fLatest;
	
	/**
	 * Constructor
	 * @throws Exception
	 */
	public CorporateOptionData() throws Exception {
		cCorporateOptionItemRevision = getLatestCorporateItemRevision();
		fLatest = getLatestNamedReferenceFile();
		
		initialize();
	}
	
	/**
	 * Initialize
	 * @throws Exception 
	 */
	private void initialize() throws Exception {
		initCorporateOptionCodeList(fLatest);
		initCorporateOptionHistoryList(fLatest);
	}
	
	/**
	 * Corporate Option ���� Revision ��ȯ
	 * @return
	 * @throws Exception
	 */
	private TCComponentItemRevision getLatestCorporateItemRevision() throws Exception {
		TCComponent[] cItems = CustomUtil.queryComponent("Item...", new String[]{"Type", "ItemID"}, new String[]{"Corporate Option Item", "CorporateOption-001"});
		
		// Corporate Option�� �ݵ�� �ϳ��� �����ϹǷ� 0���̰ų� 1���� �ʰ��� ��� ���� �߻�.
		if (cItems.length != 1) {
			new Exception("Error has occur when load a Corporate Option Item.");
		}
		
		return ((TCComponentItem)cItems[0]).getLatestItemRevision();
	}
	
	/**
	 * Corporate Item Revision�� ���� �������� Excel ��ȯ
	 * @return
	 * @throws Exception
	 */
	private File getLatestNamedReferenceFile() throws Exception {
		TCComponent[] relatedComponents = cCorporateOptionItemRevision.getRelatedComponents();
		
		// Revision �ؿ� �ִ� ��� MS Excel Type�� Dataset ����
		ArrayList<TCComponentDataset> alDataset = new ArrayList<TCComponentDataset>();
		for (int inx = 0; inx < relatedComponents.length; inx++) {
			if (relatedComponents[inx] instanceof TCComponentDataset && relatedComponents[inx].getProperty("object_type").equals("MS Excel")) {
				TCComponentDataset dataset = (TCComponentDataset)relatedComponents[inx];
				alDataset.add(dataset);
			}
		}
		
		// Dataset�� �����ϰ� �ִ� Corporate Option Excel File �� last_mod_date�� ���� �ֽ� File�� ������. (�������� ������ ���ɼ��� ����.)
		Date dLatest = null;
		TCComponentTcFile tfLatestNamedRef = null;
		for (int inx = 0; inx < alDataset.size(); inx++) {
			TCComponentDataset dataset = alDataset.get(inx);
			TCComponent cNamedRef = dataset.getNamedRefComponent("excel");
			TCComponentTcFile tfNamedRef = (TCComponentTcFile) cNamedRef;
			Date dLastModDate = tfNamedRef.getDateProperty("last_mod_date");
			
			if (dLatest == null) {
				tfLatestNamedRef = tfNamedRef;
				dLatest = dLastModDate;
			} else if (dLatest.before(dLastModDate)) {
				tfLatestNamedRef = tfNamedRef;
				dLatest = dLastModDate;
			}
		}
		
		if (tfLatestNamedRef == null) {
			new Exception("Named Reference is null");
		}
		
		return tfLatestNamedRef.getFmsFile();
	}
	
	/**
	 * Option Code List
	 * @param fLatest
	 */
	private void initCorporateOptionCodeList(File fLatest) throws Exception {
		Sheet sheetOptionCodes = getExcelSheet(0);	// 0�� Sheet
		int iRows = sheetOptionCodes.getPhysicalNumberOfRows();
		alCorporateOptionCodeList = new ArrayList<CorporateOptionCodeData>();
		
		for (int inx = CorporateOptionCodeData.START_ROW; inx < iRows + 1; inx++) {
			Row row = sheetOptionCodes.getRow(inx);
			
			if (row == null) {
				continue;
			}
			
			CorporateOptionCodeData data = new CorporateOptionCodeData(row);
			alCorporateOptionCodeList.add(data);
		}
	}
	
	/**
	 * Option Change History List
	 * @param fLatest
	 */
	private void initCorporateOptionHistoryList(File fLatest) throws Exception {
		Sheet sheetOptionHistories = getExcelSheet(3);	// 3�� Sheet
		int iRows = sheetOptionHistories.getPhysicalNumberOfRows();
		alCorporateOptionHistoryList = new ArrayList<CorporateOptionHistoryData>();
		
		for (int inx = CorporateOptionHistoryData.START_ROW; inx < iRows + 1; inx++) {
			Row row = sheetOptionHistories.getRow(inx);

			if (row == null) {
				continue;
			}
			
			CorporateOptionHistoryData data = new CorporateOptionHistoryData(row);
			alCorporateOptionHistoryList.add(data);
		}
	}
	
	/**
	 * Excel���� Sheet ��ȯ
	 * @param iSheetNo
	 * @return
	 * @throws Exception
	 */
	private Sheet getExcelSheet(int iSheetNo) throws Exception {
		Workbook workbook = null;
		InputStream is = new FileInputStream(fLatest);
		workbook = new HSSFWorkbook(is);
		Sheet sheet = workbook.getSheetAt(iSheetNo);
		return sheet;
	}
	
	public ArrayList<CorporateOptionCodeData> getCorporateOptionCodeList() {
		return alCorporateOptionCodeList;
	}
	
	public ArrayList<CorporateOptionHistoryData> getCorporateOptionHistoryList() {
		return alCorporateOptionHistoryList;
	}

	public TCComponentItemRevision getCorporateOptionItemRevision() {
		return cCorporateOptionItemRevision;
	}

	public void setCorporateOptionItemRevision(TCComponentItemRevision cCorporateOptionItemRevision) {
		this.cCorporateOptionItemRevision = cCorporateOptionItemRevision;
	}

	public File getLatestFile() {
		return fLatest;
	}

	public void setLatestFile(File fLatest) {
		this.fLatest = fLatest;
	}
	
	/**
	 * 
	 * @param sOptionCodeStatement
	 * @param sSeparate
	 * @return
	 */
	public String getOptionNameStatement(String sOptionCodeStatement, String sSeparate) {
		StringBuffer sbResult = new StringBuffer();
    	String[] sSplittedVC = null;
    	String sSpace = " ";
    	
    	if (sOptionCodeStatement == null || sOptionCodeStatement.equals("") || sOptionCodeStatement.length() == 0) {
			return "";
		} else {
			sSplittedVC = sOptionCodeStatement.split("\n");
		}
    	
    	for (int inx = 0; inx < sSplittedVC.length; inx++) {
    		String[] sAndSplittedVC = sSplittedVC[inx].split(" AND ");
    		StringBuffer sbResultTemp = new StringBuffer();	// \n(OR)���� Split�� Line �� ��ȯ ���
    		
    		for (int jnx = 0; jnx < sAndSplittedVC.length; jnx++) {
    			String sOptionCodeTemp = sAndSplittedVC[jnx];
    			boolean isMatch = false;
    			
    			for (int knx = 0; knx < alCorporateOptionCodeList.size(); knx++) {
    				CorporateOptionCodeData dataCorporateOption = alCorporateOptionCodeList.get(knx);
    				String sCorporateOptionCode = dataCorporateOption.getCode();
    				
    				if (sOptionCodeTemp.equals(sCorporateOptionCode)) {
						if (sbResultTemp.length() == 0) {
							sbResultTemp.append(dataCorporateOption.getName());
						} else {
							sbResultTemp.append(sSpace).append(sSeparate).append(sSpace).append(dataCorporateOption.getName());
						}
						
						isMatch = true;
						break;
					}
    			}
    			
    			if (!isMatch) {
					// Corporate Option ���� �������� �ʴ� Option �߰� ��
    				if (sbResultTemp.length() == 0) {
    					sbResultTemp.append("No Name");
    				} else {
    					sbResultTemp.append(sSpace).append(sSeparate).append(sSpace).append("No Name");
    				}
				}
			}
    		
    		if (inx != sSplittedVC.length - 1) {
    			sbResult.append(sbResultTemp.toString()).append("\n");
    		} else {
    			sbResult.append(sbResultTemp.toString());
    		}
		}
    	
    	return sbResult.toString();
	}
	
	/**
	 * ������ Option Code�� Corporate Option Code Data ��ȯ
	 * @param sOptionCode
	 * @return
	 */
	public CorporateOptionCodeData getCorporateOptionCodeData(String sOptionCode) {
		if (sOptionCode == null || sOptionCode.equals("") || sOptionCode.length() == 0) {
			return null;
		}
		
		if (alCorporateOptionCodeList == null || alCorporateOptionCodeList.size() == 0) {
			return null;
		}
		
		CorporateOptionCodeData dataCorporateOptionCode = null;
		for (int inx = 0; inx < alCorporateOptionCodeList.size(); inx++) {
			CorporateOptionCodeData dataCorporateOptionCodeTemp = alCorporateOptionCodeList.get(inx);
			String sOptionCodeTemp = dataCorporateOptionCodeTemp.getCode();
			
			if (sOptionCodeTemp.equals(sOptionCode)) {
				dataCorporateOptionCode = dataCorporateOptionCodeTemp;
				break;
			}
		}
		
		return dataCorporateOptionCode;
	}
}