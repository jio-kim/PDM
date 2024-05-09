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
 *  - CorporateOption Item의 Latest Revision과 연결된 Dataset NamedReference 중 수정일자가 최신인 Excel File을 읽어 생성
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
	 * Corporate Option 최종 Revision 반환
	 * @return
	 * @throws Exception
	 */
	private TCComponentItemRevision getLatestCorporateItemRevision() throws Exception {
		TCComponent[] cItems = CustomUtil.queryComponent("Item...", new String[]{"Type", "ItemID"}, new String[]{"Corporate Option Item", "CorporateOption-001"});
		
		// Corporate Option은 반드시 하나만 존재하므로 0개이거나 1개를 초과할 경우 에러 발생.
		if (cItems.length != 1) {
			new Exception("Error has occur when load a Corporate Option Item.");
		}
		
		return ((TCComponentItem)cItems[0]).getLatestItemRevision();
	}
	
	/**
	 * Corporate Item Revision의 최종 수정일자 Excel 반환
	 * @return
	 * @throws Exception
	 */
	private File getLatestNamedReferenceFile() throws Exception {
		TCComponent[] relatedComponents = cCorporateOptionItemRevision.getRelatedComponents();
		
		// Revision 밑에 있는 모든 MS Excel Type의 Dataset 수집
		ArrayList<TCComponentDataset> alDataset = new ArrayList<TCComponentDataset>();
		for (int inx = 0; inx < relatedComponents.length; inx++) {
			if (relatedComponents[inx] instanceof TCComponentDataset && relatedComponents[inx].getProperty("object_type").equals("MS Excel")) {
				TCComponentDataset dataset = (TCComponentDataset)relatedComponents[inx];
				alDataset.add(dataset);
			}
		}
		
		// Dataset이 포함하고 있는 Corporate Option Excel File 중 last_mod_date가 가장 최신 File을 가져옴. (여러개가 존재할 가능성이 있음.)
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
		Sheet sheetOptionCodes = getExcelSheet(0);	// 0번 Sheet
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
		Sheet sheetOptionHistories = getExcelSheet(3);	// 3번 Sheet
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
	 * Excel에서 Sheet 반환
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
    		StringBuffer sbResultTemp = new StringBuffer();	// \n(OR)으로 Split된 Line 별 변환 결과
    		
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
					// Corporate Option 내에 존재하지 않는 Option 발견 시
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
	 * 지정한 Option Code의 Corporate Option Code Data 반환
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