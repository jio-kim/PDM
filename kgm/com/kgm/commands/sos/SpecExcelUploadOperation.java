package com.kgm.commands.sos;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.paste.PasteCommand;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentVariantRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.sosvi.SelectedOptionSetDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.services.internal.rac.structuremanagement.VariantManagementService;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptions;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInfo;
import com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsOutput;

public class SpecExcelUploadOperation extends AbstractAIFOperation {
	private TCSession session;

	private String sFilePath;
	private TCComponentBOMWindow bwProduct;
	private HashMap<String, HashMap<String, String>> hmSpecs;
	private HashMap<Integer, String> hmSpecMap;
	private HashMap<String, String> hmOptions;
	private ArrayList<String> alAvailableOptionCodes;

	private WaitProgressBar progress;

	public SpecExcelUploadOperation(WaitProgressBar progress, TCSession session, String sFilePath, TCComponentBOMWindow bwProduct) {
		this.progress = progress;
		this.session = session;
		this.sFilePath = sFilePath;
		this.bwProduct = bwProduct;
	}

	@Override
	public void executeOperation() throws Exception {
		Markpoint mp = new Markpoint(session);
		
		progress.setWindowSize(500, 400);
		progress.setAlwaysOnTop(true);
		progress.setShowButton(false);
		progress.start();

		try {
			progress.setStatus("Analyzing Excel file...");

			Workbook workbook = null;
			InputStream is = new FileInputStream(new File(sFilePath));

			workbook = new HSSFWorkbook(is);
			Sheet sheet = workbook.getSheetAt(0);

			// Project Code
			int iProjectRow = 1;
			int iProjectColumn = 0;

			Cell cellProject = sheet.getRow(iProjectRow).getCell(iProjectColumn);
			String sProject = cellProject == null ? "" : cellProject.getStringCellValue();

			int iHeaderRow = 2;
			int iStartRow = 2;
			int iStartColumn = 2;

			int iRowCount = sheet.getPhysicalNumberOfRows();

			hmSpecs = new HashMap<String, HashMap<String, String>>();
			hmSpecMap = new HashMap<Integer, String>();

			// Row 별 Option Code 저장.
			for (int inx = iStartRow + 1; inx < iRowCount; inx++) {
				Row row = sheet.getRow(inx);
				Cell cell = row.getCell(0);
				String sOptionCode = cell.getStringCellValue();

				hmSpecMap.put(inx, sOptionCode);
			}

			Row rowHeader = sheet.getRow(iHeaderRow);
			int iColumnCount = rowHeader.getPhysicalNumberOfCells();

			for (int inx = iStartColumn; inx < iColumnCount; inx++) {
				String sSpecNo = "";

				for (int jnx = iStartRow; jnx < iRowCount; jnx++) {
					Row row = sheet.getRow(jnx);
					Cell cell = row.getCell(inx);
					String sValue = cell.getStringCellValue();
					String sOptionCode = hmSpecMap.get(jnx);

					if (jnx == iHeaderRow) {
						sSpecNo = sValue;
						hmSpecs.put(sValue, new HashMap<String, String>());
						continue;
					}

					HashMap<String, String> hmSpec = hmSpecs.get(sSpecNo);
					hmSpec.put(sOptionCode, sValue);
				}
			}

			// Product에 선언 된 Option Code 외 다른 Code가 있을 경우 Error
			progress.setStatus("Compare the Spec Option code with the Product Option code...");

			TCComponentBOMLine blProduct = bwProduct.getTopBOMLine();

			com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput inputModularOptions[] = new com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput[1];
			VariantManagementService serviceVariantManagement = VariantManagementService.getService(blProduct.getSession());
			com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput inputModularOption = new com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsInput();
			inputModularOption.bomWindow = bwProduct;
			inputModularOption.bomLines = new TCComponentBOMLine[] { blProduct };
			inputModularOptions[0] = inputModularOption;

			serviceVariantManagement.getBOMVariantConfigOptions(bwProduct, blProduct);
			com.teamcenter.services.internal.rac.structuremanagement._2011_06.VariantManagement.ModularOptionsForBomResponse responseModularOptionsForBom = serviceVariantManagement.getModularOptionsForBom(inputModularOptions);

			ModularOptionsOutput[] outputModularOptions = responseModularOptionsForBom.optionsOutput;
			ModularOptionsInfo[] infoModularOptions = outputModularOptions[0].optionsInfo;
			ModularOptions optionModular = infoModularOptions[0].options;
			ModularOption[] optionModulars = optionModular.options;
			
			TCVariantService vs = blProduct.getSession().getVariantService();
			String sMVL = vs.askLineMvl(blProduct);
			ConstraintsModel cm = new ConstraintsModel(blProduct.getItem().getProperty("item_id"), sMVL, null, blProduct, vs);
			if (!cm.parse()) {
				progress.setStatus("Fail : Condition cannot be parse!");
				return;
			}

			String[][] ecs = cm.errorChecksTableData();
			ArrayList<String> alNotDefineORNotUse = new ArrayList<String>();

			for (int inx = 0; inx < ecs.length; inx++) {
				String[] ec = ecs[inx];
				String sOptionCode = ec[6];

				alNotDefineORNotUse.add(sOptionCode);
			}

			alAvailableOptionCodes = new ArrayList<String>();
			hmOptions = new HashMap<String, String>();

			for (int inx = 0; inx < optionModulars.length; inx++) {
				ModularOption mo = optionModulars[inx];
				String[] saAllowedValues = mo.allowedValues;
				String sOptionCategory = mo.optionName;

				for (int jnx = 0; jnx < saAllowedValues.length; jnx++) {
					String sAllowedValue = saAllowedValues[jnx];

					if (!alNotDefineORNotUse.contains(sAllowedValue)) {
						alAvailableOptionCodes.add(sAllowedValue);
						hmOptions.put(sAllowedValue, sOptionCategory);
					}
				}
			}

			Set<Integer> ksSpecMap = hmSpecMap.keySet();
			Iterator<Integer> itSpecMap = ksSpecMap.iterator();

			StringBuffer sbError = new StringBuffer();
			while (itSpecMap.hasNext()) {
				int iOptionCode = itSpecMap.next();
				String sOptionCode = hmSpecMap.get(iOptionCode);

				if (!alAvailableOptionCodes.contains(sOptionCode)) {
					String sMessage = sOptionCode + " is not available.";
					if (sbError.length() > 0) {
						sbError.append("\n").append(sMessage);
					} else {
						sbError.append(sMessage);
					}
				}
			}

			if (sbError.length() > 0) {
				progress.setStatus(sbError.toString());
				return;
			}

			// Spec 생성
			progress.setStatus("Creating Stored option set...");
			Set<String> ksSpecNo = hmSpecs.keySet();
			Iterator<String> itSpecNo = ksSpecNo.iterator();

			// 여러개의 Spec을 동시에 생성하려고 했으나...CreateSOSThread내 호출되는 SelectedOptionSetDlg에 SetValue하는 위치에서
			// Session당 하나만 부여되는 변수에 값을 Set한 후 VariantService를 이용하여 생성하므로 동시에 여러개를 생성하지 못함.
			// 그래서 결국...속도가 오래걸리는 것은 감수하고 하나씩만 생성하도록 수정
			// TODO (즉, Thread당 변수를 할당 후 생성이 가능하다면 동시에 생성 가능. 불가능하다면 Anotation 제거할 것.)
			ExecutorService executor = Executors.newFixedThreadPool(1);

			while (itSpecNo.hasNext()) {
				String sSpecNo = itSpecNo.next();
				HashMap<String, String> hmSpec = hmSpecs.get(sSpecNo);

				CreateSOSThread thread = new CreateSOSThread(progress, sSpecNo, sProject, hmSpec, hmOptions, bwProduct);
				executor.execute(thread);
			}

			executor.shutdown();
			while (!executor.isTerminated()) {
			}

			mp.forget();
			progress.setStatus("Stored Option Set (Excel) 완료.");
			progress.setStatus("My Teamcenter에서 Home 내 New Stuff Folder에 저장되었습니다.");
			
		} catch (Exception e) {
			mp.rollBack();
			e.printStackTrace();
			
			progress.setStatus("Fail!");
			progress.setStatus(e.getMessage());
		} finally {
			if (progress != null) {
				progress.setShowButton(true);
			}
		}
	}

	/**
	 * Create Stored Option Set Thread.
	 * @author jclee
	 * 
	 */
	private class CreateSOSThread extends Thread {
		private WaitProgressBar progress;

		private String sSpecNo;
		private String sProject;
		private HashMap<String, String> hmSpec;
		private HashMap<String, String> hmOptions;
		private TCComponentBOMWindow bwProduct;

		/**
		 * Contructor
		 * @param progress
		 * @param sSpecNo
		 * @param sProject
		 * @param hmSpec
		 * @param hmOptions
		 * @param bwProduct
		 */
		public CreateSOSThread(WaitProgressBar progress, String sSpecNo, String sProject, HashMap<String, String> hmSpec, HashMap<String, String> hmOptions, TCComponentBOMWindow bwProduct) {
			this.progress = progress;
			this.sSpecNo = sSpecNo;
			this.sProject = sProject;
			this.hmSpec = hmSpec;
			this.hmOptions = hmOptions;
			this.bwProduct = bwProduct;
		}

		/**
		 * Run Thread
		 */
		public void run() {
			try {
				progress.setStatus("Creating " + sSpecNo + "...", false);

				Set<String> ksOptionCode = hmSpec.keySet();
				Iterator<String> itOptionCode = ksOptionCode.iterator();

				HashMap<String, String> hmResult = new HashMap<String, String>();

				while (itOptionCode.hasNext()) {
					String sOptionCode = itOptionCode.next();
					String sValue = hmSpec.get(sOptionCode);

					if (!(sValue == null || sValue.equals("") || sValue.equals("-"))) {
						String sOptionCategory = hmOptions.get(sOptionCode) == null ? "" : hmOptions.get(sOptionCode).toString();

						if (hmResult.containsKey(sOptionCategory)) {
							throw new Exception("The '" + sOptionCategory + "' Category is duplicated in '" + sSpecNo + "'.");
						}

						hmResult.put(sOptionCategory, sOptionCode);
					}
				}

				// SOS 생성.
				TCComponentBOMLine blProduct = bwProduct.getTopBOMLine();
				TCVariantService vs = blProduct.getSession().getVariantService();
				
				SelectedOptionSetDialog dlgSOS = new SelectedOptionSetDialog(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), blProduct);
				dlgSOS.setValue(blProduct, hmResult);
				
				
				TCComponent cSOS = vs.getSos(blProduct);
				List<TCComponentVariantRule> parentVariantRules = bwProduct.askVariantRules();
				TCComponentVariantRule vrParent = null;
				if (parentVariantRules != null && parentVariantRules.size() > 0) {
					vrParent = parentVariantRules.get(0);
				}

				TCComponentVariantRule vr = vrParent.copy();
				TCComponent cVariantConfig = vs.createVariantConfig(vr, new TCComponent[] { cSOS });
				TCComponent cStoredConfig = null;
				try {
					cStoredConfig = vs.writeStoredConfiguration(sSpecNo, cVariantConfig);
					cStoredConfig.setStringProperty("object_desc", sSpecNo);
					cStoredConfig.setProperty("s7_PROJECT_CODE", sProject);
					cStoredConfig.save();

					// 생성된 SOS를 New Stuff Folder에 붙여넣기를 함.
					TCComponentFolder folder = session.getUser().getNewStuffFolder();

					PasteCommand cmdPaste = new PasteCommand(new TCComponent[] { cStoredConfig }, new InterfaceAIFComponent[] { folder }, Boolean.FALSE);
					cmdPaste.executeModeless();
				} catch (TCException tcexception) {
					vs.deleteVariantConfig(cVariantConfig);
					throw tcexception;
				}

				vs.clearSos(blProduct);
				vs.clearVariantConfig(cVariantConfig);
				vs.deleteVariantConfig(cVariantConfig);
				
				if (dlgSOS != null) {
					dlgSOS.dispose();
					dlgSOS = null;
				}
				
				progress.setStatus("Complete");
			} catch (Exception e) {
				progress.setStatus("is FAIL!");
				progress.setStatus(e.getMessage());
				e.printStackTrace();
			}
		}
	}
}
