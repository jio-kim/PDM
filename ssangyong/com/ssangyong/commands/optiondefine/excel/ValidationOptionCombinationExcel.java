package com.ssangyong.commands.optiondefine.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.ssangyong.commands.optiondefine.excel.exception.ValidationOptionCombinationException;
import com.ssangyong.commands.optiondefine.excel.vo.OptionPart;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.StringUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.services.PSEApplicationService;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.pse.variants.modularvariants.ModularOptionModel;
import com.teamcenter.rac.pse.variants.modularvariants.OVEOption;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class ValidationOptionCombinationExcel {
    private Shell shell;
    private Cursor waitCursor;
    private Cursor arrowCursor;
    private Text fileText;
    private Text progressingText;
    private Class<?> dlgClass;
    private Registry registry;
    private Workbook wb;
    private final static int HEADER_START_X_POS = 1;
    private final static int HEADER_END_X_POS = 4;
    private final static int HEADER_START_Y_POS = 4;
    private Button btnValidation;
    private Button btnRun;
    private TCSession session;
    private TCComponentBOMLine[] targetBOMLines;
    /**
     * Excel Validation시 Excel파일을 파싱하여 만든 FM 하위에 있는 VehiclePart List
     * key:Function Master Id, value:ArrayList<VehiclePart>
     */
    private HashMap<String, ArrayList<OptionPart>> pushOptionData;
    /**
     * Excel Validation시 팀센터에서 존재하는 FM 하위에 있는 VehiclePart List key:Function
     * Master Id, value:ArrayList<VehiclePart>
     */
    private HashMap<String, ArrayList<OptionPart>> mappingFmInfos;

    public ValidationOptionCombinationExcel(Shell shell, Class<?> dlgClass, Registry registry, Text progressing) {
        this.shell = shell;
        this.dlgClass = dlgClass;
        this.registry = registry;
        this.waitCursor = new Cursor(this.shell.getDisplay(), SWT.CURSOR_WAIT);
        this.arrowCursor = new Cursor(this.shell.getDisplay(), SWT.CURSOR_ARROW);
        this.progressingText = progressing;
        session = CustomUtil.getTCSession();
        InterfaceAIFComponent[] targetComps = AIFUtility.getCurrentApplication().getTargetComponents();
        if (targetComps == null || targetComps.length == 0) {
            return;
        }
        targetBOMLines = new TCComponentBOMLine[targetComps.length];
        for (int i = 0; i < targetComps.length; i++) {
            if (!(targetComps[i] instanceof TCComponentBOMLine)) {
                return;
            }
            targetBOMLines[i] = (TCComponentBOMLine) targetComps[i];
        }
    }

    public void load(Text file, Button validationBtn, Button runBtn) {
        this.fileText = file;
        this.btnValidation = validationBtn;
        this.btnRun = runBtn;
        this.btnRun.setEnabled(false);
        progressingText.setText(""); // Progreesing Log Clear
        Display.getDefault().syncExec(new Runnable() {
            public void run() {
                progressingText.append(StringUtil.getTextBundle(registry, "excelLoading", null, dlgClass) + "\n");
                wb = null;
                FileInputStream fis = null;
                try {
                    shell.setCursor(waitCursor);
                    String strFilePath = fileText.getText();
                    fis = new FileInputStream(strFilePath);
                    String strExt = fileText.getText().substring(strFilePath.lastIndexOf(".") + 1);
                    if (strExt.toLowerCase().equals("xls")) {
                        // Excel WorkBook
                        wb = new HSSFWorkbook(fis);
                    } else {
                        // Excel WorkBook
                        wb = new XSSFWorkbook(fis);
                    }
                    fis.close();
                    fis = null;
                    progressingText.append(StringUtil.getTextBundle(registry, "excelLoadingCompleted", null, dlgClass) + "\n");
                    btnValidation.setEnabled(true);
                } catch (Exception e) {
                    btnValidation.setEnabled(false);
                    btnRun.setEnabled(false);
                    progressingText.append(StringUtil.getTextBundle(registry, "excelLoadingError", null, dlgClass) + "\n");
                    MessageBox.post(shell, StringUtil.getTextBundle(registry, "excelLoadingError", null, dlgClass), "Notification", 2);
                    e.printStackTrace();
                } finally {
                    if (fis != null) {
                        try {
                            fis.close();
                            fis = null;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    shell.setCursor(arrowCursor);
                }

            }

        });
    }

    public void doValidation(Button btn) {
        this.btnRun = btn;
        this.pushOptionData = null;
        // key:Function Master Id, value:ArrayList<VehiclePart>
        this.pushOptionData = new HashMap<String, ArrayList<OptionPart>>();
        boolean success = true;
        // progressingText.append("1. Executing Validate update file.\n");
        progressingText.append(StringUtil.getTextBundle(registry, "excelValidateStep1Msg", "MSG", dlgClass) + "\n");
        try {
            Sheet sheet = wb.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();
            int startDataRow = HEADER_START_Y_POS + 1; // 실제 데이타가 있는 Row
            for (int r = startDataRow; r <= rows; r++) {
                Row dataRow = sheet.getRow(r);
                /**
                 * [0]FMPNO [1]PARTNO [2]QTY [3]OPTION_RESULT
                 */
                String[] cellData = new String[4];
                for (int c = HEADER_START_X_POS; c <= HEADER_END_X_POS; c++) {
                    String cell = StringUtil.nullToString(StringUtil.getCellText(dataRow.getCell(c)));
                    cellData[c - 1] = cell;
                }
                if (!(cellData[0].equals("") && cellData[1].equals("") && cellData[2].equals(""))) {
                    OptionPart optionPart = new OptionPart();
                    optionPart.setFmId(cellData[0]);
                    optionPart.setItemId(cellData[1]);
                    optionPart.setQty(cellData[2]);
                    optionPart.setStrCondition(cellData[3]);
                    
					/**
					 * SRME:: [][201400806] Excel을 통한 옵션 Import시 4000초과면 에러 발생. 
					 */	    
                    // [CSH]4000 byte 중간과정 기준 적용.
                    String optionStr = optionPart.getStrCondition().replace("\n", " OR ");
//                    System.out.println("optionStr.length() " + optionStr.length());
//                    System.out.println("optionPart.getStrCondition().length() " + optionPart.getStrCondition().length());
                    if( optionStr.length() > 4000){
//                    if( optionPart.getStrCondition().length() > 4000){
                    	String errorString = "[Error][ Excel " + (r + 1) + " Line ]Can not exceed 4000 Byte option.(" + optionStr.length() + "Byte)\n입력한 Option이 4000 Byte를 초과하였습니다.\nOption 조정 후 다시 저장하여 주십시오.\n";
                        progressingText.append(errorString + "\n");
                        pushOptionData = null;
                        return;
                    }
                    
					//SRME:: [][20140812] 특정 category는 다른 특정 category랑 함께 쓸수없게 제한(special country)
                    try{
						String orgCondition = optionPart.getStrCondition();
						String[] splitStr = orgCondition.split("\n");
						for( String tmpStr : splitStr){
							boolean bFlag = CustomUtil.isCompatibleOptions(session, tmpStr.trim(), false);
							if( !bFlag ){
								String errorString = "[ Excel " + (r + 1) + " Line ]This option includes incompatible." + "\n";
		                        progressingText.append(errorString + "\n");
		                        pushOptionData = null;
								return;
							}   
						}
                    }catch(Exception e){
                    	throw new RuntimeException(e);
                    }
                    
                    optionPart.setExcelLineNumber((r + 1) + ""); // Line Number
                    ArrayList<OptionPart> fmOptionPartList = null;
                    // 이미 등록 FM가 존재하는지 확인
                    if (this.pushOptionData.containsKey(cellData[0])) {
                        fmOptionPartList = this.pushOptionData.get(cellData[0]);
                    } else {
                        fmOptionPartList = new ArrayList<OptionPart>();
                        this.pushOptionData.put(cellData[0], fmOptionPartList);
                    }
                    fmOptionPartList.add(optionPart);
                } else {
                    if (cellData[0].equals("") && cellData[1].equals("") && cellData[2].equals("")) {
                        continue;
                    }
                    success = false;
                    // String errorString = "[ERROR] Line : " + (r + 1) +
                    // " : the data is empty! \n";
                    String errorString = StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorEmptyMsg", new String[] { (r + 1) + "" }) + "\n";
                    progressingText.append(errorString + "\n");
                    pushOptionData = null;
                    return;
                }
            }
            progressingText.append("[OK]\n");
            // final validation check
            if (success) {
                // TC Function Master Item과 검증을 한다.
                this.checkTcBomLine();
                // TC Function Master Item과 EXCEL 파싱 데이터를 비교검증을 한다.
                this.compareTcToExcel();
                // progressingText.append("3. Completed Validation.\n-->If you wanna create data, click on \"Run\".\n");
                progressingText.append(StringUtil.getTextBundle(registry, "excelValidateStep3Msg", "MSG", dlgClass) + "\n");
                btnRun.setEnabled(true);
            } else {
                btnRun.setEnabled(false);
            }
        } catch (ValidationOptionCombinationException ve) {
            ve.printStackTrace();
            // progressingText.append("[ERROR] " + ve.getMessage() + "\n");
            progressingText.append(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorMsg", new String[] { ve.getMessage() }) + "\n");
        } catch (RuntimeException re) {
            re.printStackTrace();
            // progressingText.append("[ERROR] unexpect error!!\n");
            progressingText.append(StringUtil.getTextBundle(registry, "excelErrorUnexpectMsg", "MSG", dlgClass) + "\n");
        } catch (TCException te) {
            te.printStackTrace();
            // progressingText.append("[ERROR] TC error!!\n");
            progressingText.append(StringUtil.getTextBundle(registry, "excelErrorTcMsg", "MSG", dlgClass) + "\n");
        } finally {
            // progressingText.append("[OK]\n");
        }
    }

    /**
     * TC Function Master Item과 검증
     * 
     * @method checkTcBomLine
     * @date 2013. 2. 6.
     * @param
     * @return
     * @exception
     * @throws
     * @see
     */
    private void checkTcBomLine() throws TCException, ValidationOptionCombinationException {
        TCVariantService variantService = session.getVariantService();
        // BOMLine Load 체크
        if (targetBOMLines == null || targetBOMLines.length == 0) {
            // throw new
            // ValidationOptionCombinationException("Function Item을 Structure Open 하세요.");
            throw new ValidationOptionCombinationException(StringUtil.getTextBundle(registry, "functionItemStrManOpen", null, dlgClass));
        }
        TCComponentBOMLine topLine = targetBOMLines[0].window().getTopBOMLine();
        // Function Item 체크
        if (!"S7_Function".equals(topLine.getItem().getType())) {
            // throw new
            // ValidationOptionCombinationException("Item이 Function Type이 아닙니다.");
            throw new ValidationOptionCombinationException(StringUtil.getTextBundle(registry, "excelErrorNotFunctionTypeMsg", "MSG", dlgClass));
        }
        // key:Function Master Id, value:ArrayList<VehiclePart>
        mappingFmInfos = new HashMap<String, ArrayList<OptionPart>>();
        AIFComponentContext[] fmContexts = topLine.getChildren();
        for (AIFComponentContext fmContext : fmContexts) {
            TCComponentBOMLine fmBOMLine = (TCComponentBOMLine) fmContext.getComponent();
            // child.parent();
            String fmItemId = fmBOMLine.getItem().getProperty("item_id");
            ArrayList<OptionPart> optionItemList = new ArrayList<OptionPart>();
            mappingFmInfos.put(fmItemId, optionItemList); // Mapping FM
            AIFComponentContext[] optionItemContexts = fmBOMLine.getChildren();
            for (AIFComponentContext optionItemContext : optionItemContexts) {
                TCComponentBOMLine optionItemBOMLine = (TCComponentBOMLine) optionItemContext.getComponent();
                OptionPart optionPart = new OptionPart();
                optionPart.setFmId(fmItemId);
                optionPart.setItemId(optionItemBOMLine.getItem().getProperty("item_id"));
                optionPart.setObjectName(optionItemBOMLine.getItem().getProperty("object_name"));
                // Option Condition List 조회
                String lineMvl = variantService.askLineMvlCondition(optionItemBOMLine);
                ConditionElement[] conditionElements = ConstraintsModel.parseACondition(lineMvl);
                optionPart.setConditionElements(conditionElements);
                // BOMLine 정보를 저장한다.
                optionPart.setTccomponentbomline(optionItemBOMLine);
                // Function Master Option설정(check item -> fm Item -> function
                // Item) 리스트를 저장한다.
                optionPart.setSetOptions(this.getSetOptions(optionPart.getTccomponentbomline().parent().parent()));
                // Function에 정의된 Option들의 Value List를 저장한다.
                optionPart.setSetOptionValueList(this.setOptionMapList(optionPart));
                optionItemList.add(optionPart);
            }
        }
        if (this.pushOptionData == null) {
            // throw new
            // ValidationOptionCombinationException("retry Validate!");
            throw new ValidationOptionCombinationException(StringUtil.getTextBundle(registry, "excelErrorRetryValidate", "MSG", dlgClass));
        }
    }

    /**
     * Function 에 설정된 Set Option List를 가져온다. (Optopn Value 검증 용)
     * 
     * @method getSetOptions
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private OVEOption[] getSetOptions(TCComponentBOMLine functionBomLine) throws ValidationOptionCombinationException {
        OVEOption[] setOptions = null;
        try {
            // VehiclePart -> FunctionMaster - Function
            // Function에서 Set Option List를 가져온다.
            TCComponentBOMLine line = functionBomLine;
            AbstractAIFUIApplication aifApp = AIFUtility.getCurrentApplication();
            PSEApplicationService service = (PSEApplicationService) aifApp;
            BOMTreeTable treeTable = (BOMTreeTable) service.getAbstractViewableTreeTable();
            ModularOptionModel moduleModel = null;
            moduleModel = new ModularOptionModel(treeTable, line/*
                                                                 * parent BOM
                                                                 * line
                                                                 */, true);
            int[] optionNums = moduleModel.getOptionsForModule(treeTable.getNode(line));
            if (optionNums == null) {
                // throw new
                // ValidationOptionCombinationException("Function Master Item에 Option 설정이 되어있지않습니다.");
                throw new ValidationOptionCombinationException(StringUtil.getTextBundle(registry, "excelErrorNotSetOptions", "MSG", dlgClass));
            }
            setOptions = new OVEOption[optionNums.length];
            if (optionNums != null) {
                for (int i = 0; i < optionNums.length; i++) {
                    setOptions[i] = moduleModel.getOption(optionNums[i]);
                }
            }
        } catch (ValidationOptionCombinationException ve) {
            ve.printStackTrace();
            throw ve;
        } catch (Exception e) {
            e.printStackTrace();
            // throw new
            // ValidationOptionCombinationException("Function Master Item 정보를 확인바랍니다.");
            throw new ValidationOptionCombinationException(StringUtil.getTextBundle(registry, "excelErrorCheckFmItem", "MSG", dlgClass));
        }
        return setOptions;
    }

    /**
     * Function에 정의된 Option들의 Value List를 저장한다.
     * 
     * @method setOptionList
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private HashMap<String, String> setOptionMapList(OptionPart optionPart) {
        HashMap<String, String> optionList = new HashMap<String, String>();
        if (optionPart.getSetOptions() == null) {
            return optionList;
        }
        OVEOption[] options = optionPart.getSetOptions();
        for (int i = 0; options != null && i < options.length; i++) {
            String[] list = options[i].stringVals.values;
            for (int j = 0; list != null && j < list.length; j++) {
                // key : value / value : option[item:name]
                optionList.put(list[j], options[i].option.item + ":" + MVLLexer.mvlQuoteId(options[i].option.name, false));
            }
        }
        return optionList;
    }

    /**
     * Excel 파일 파싱 정보와 TC 정보와 비교하여 Validation Check
     * 
     * *********** 주의: 같은 Part No가 있을 경우 처리방안 해결할 것 *********************
     * 
     * @method compareTcToExcel
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void compareTcToExcel() throws ValidationOptionCombinationException {
        progressingText.append("2. Checking defined options on Function.\n");
        Set<String> keySet = this.pushOptionData.keySet();
        for (String fmId : keySet) {
            ArrayList<OptionPart> optionItemPartList = this.pushOptionData.get(fmId); // ExcelOptionPart데이터
            for (OptionPart optionPart : optionItemPartList) {
                // EXCEL FM ID가 TC FM ID에 존재하는지 확인
                if (!this.mappingFmInfos.containsKey(optionPart.getFmId())) {
                    // throw new ValidationOptionCombinationException("Line : "
                    // + optionPart.getExcelLineNumber() +
                    // " - 등록하실 Excel파일의 Function Maser가 TC에 존재하지 않습니다.");
                    throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistFmItem",
                            new String[] { optionPart.getExcelLineNumber() }));
                }
                ArrayList<OptionPart> mappingOptionPartList = this.mappingFmInfos.get(optionPart.getFmId());
                boolean checkOptionItemPart = false;
                for (OptionPart mappingVehicle : mappingOptionPartList) {
                    // Part No 중복에 대한 방안 -- PartNo, Part Qty로 구분
                    if ((mappingVehicle.getItemId().equals(optionPart.getItemId()))) {
                        try {
                            String bomlineQty = StringUtil.nullToString((mappingVehicle.getTccomponentbomline().getProperty("bl_quantity") + ""));
                            // BOM Line 데이터와 Excel수량 데이터가 없으면 수량을 '1'로 초기화한다.
                            if ("".equals(bomlineQty)) {
                                bomlineQty = "1";
                            } else {
                                // 수량이 정수로 나오지 않으므로 강제로 정수로 변경
                                bomlineQty = (new Double(bomlineQty)).intValue() + "";
                            }
                            if ("".equals(optionPart.getQty())) {
                                optionPart.setQty("1");
                            }
                            // Part No, Qty를 가지고 중복 PartNo를 비교한다.
                            if (optionPart.getQty().equals(bomlineQty)) {
                                /**
                                 * TC에서 조회한 해당 VEHICLE PART의 Condition Element
                                 * List, BOMLine을 Excel Parsing VEHICLE PART에
                                 * 등록한다.
                                 */
                                optionPart.setConditionElements(mappingVehicle.getConditionElements());
                                // BOMLine 정보를 저장한다.
                                optionPart.setTccomponentbomline(mappingVehicle.getTccomponentbomline());
                                // Function Master Option설정 리스트를 저장한다.
                                optionPart.setSetOptions(mappingVehicle.getSetOptions());
                                // Function에 정의된 Option들의 Value List를 저장한다.
                                optionPart.setSetOptionValueList(mappingVehicle.getSetOptionValueList());
                                // Option 설정 Condition을 체크한다.
                                this.checkOptionCondition(optionPart);
                                checkOptionItemPart = true;
                                break;
                            }
                        } catch (TCException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!checkOptionItemPart) {
                    // throw new ValidationOptionCombinationException("Line : "
                    // + optionPart.getExcelLineNumber() +
                    // " - 등록하실 Excel파일의 PARTNO가 TC에 존재하지 않습니다.");
                    throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistPartNumber",
                            new String[] { optionPart.getExcelLineNumber() }));
                }
            }
        }
        progressingText.append("[OK]\n");
    }

    /**
     * TC에서 조회한 해당 VEHICLE PART의 Condition Element List를 가지고 Excel에서 파싱한 Option
     * Condition이 valid한지 체크한다.
     * 
     * 체크완료 후에는 컨디션식을 TC 문장에 맞게 재구성한다.
     * 
     * @method checkOptionCondition
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void checkOptionCondition(OptionPart optionPart) throws ValidationOptionCombinationException {
        String[] ortStrConditions = this.getOrStrConditions(optionPart.getStrCondition());
        ArrayList<String> optionValueList = this.generateOptionStatement(optionPart, ortStrConditions);
        for (int oi = 0; optionValueList != null && oi < optionValueList.size(); oi++) {
            boolean checkConditionValid = false;
            // Key : BOMLine에 설정된 option Valuse >> "value" / Value : BOMLine에
            // 설정된 option Key >> "item:name"
            HashMap<String, String> setOptionList = optionPart.getSetOptionValueList();
            Set<String> keySet = setOptionList.keySet();
            for (String optionValue : keySet) {
                if (optionValue.equals(StringUtil.nullToString(optionValueList.get(oi)))) {
                    checkConditionValid = true;
                    break;
                }

            }
            // 컨디션 문구 정합성 체크
            if (checkConditionValid == false) {
                // throw new ValidationOptionCombinationException("Line : " +
                // vehiclePart.getExcelLineNumber() +
                // " - 등록하실 Excel파일의 OPTION_RESULT가 TC에 존재하지 않습니다.");
                throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistOptionResult",
                        new String[] { optionPart.getExcelLineNumber() }));
            }
        }
    }

    /**
     * OPTION_RESULT 문장에서 '\n'(OR)을 배열 타입으로 추출한다.
     * 
     * @method getConvertStrCondition
     * @date 2013. 2. 8.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    private String[] getOrStrConditions(String strCondition) {
        return StringUtil.getSplitString(strCondition, "\n");
    }

    /**
     * OPTION_RESULT 수식을 OR 배열로 분리한 문구를 AND 연산 체크, 검증 및 TC등록 문구로 변경한다. 리턴은 수식에
     * 사용되는 전체 Option Value값을 리턴한다. (Option 설정값과 비교하기 위해 리턴한다.)
     * 
     * @method getConvertStrCondition
     * @date 2013. 2. 8.
     * @param
     * @return ArrayList<String>
     * @exception
     * @throws
     * @see
     */
    private ArrayList<String> generateOptionStatement(OptionPart optionPart, String[] orStrConditions) throws ValidationOptionCombinationException {
        ArrayList<String> optionValueList = new ArrayList<String>();
        String optionStatement = "";
        if (orStrConditions == null || orStrConditions.length == 0) {
            // throw new ValidationOptionCombinationException("Line : " +
            // optionPart.getExcelLineNumber() +
            // " - 등록하실 Excel파일의 OPTION_RESULT 조건이 없습니다.");
            throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistOptionResultCondition",
                    new String[] { optionPart.getExcelLineNumber() }));
        }
        for (int i = 0; i < orStrConditions.length; i++) {
            if (i > 0) {
                optionStatement += " or ";
            }
            String[] andStrConditions = this.getAndStrConditions(orStrConditions[i]);
            if (andStrConditions != null) {
                for (int j = 0; j < andStrConditions.length; j++) {
                    if (j > 0) {
                        optionStatement += " and ";
                    }
                    if (!"".equals(andStrConditions[j])) {
                        optionValueList.add(andStrConditions[j]);
                        if (i > 0) {
                            optionStatement += " ";
                        }
                        optionStatement += this.getSetOptionItemKeyValue(optionPart, andStrConditions[j]);
                    }
                }
            }
        }
        if (optionValueList.size() == 0) {
            // throw new ValidationOptionCombinationException("Line : " +
            // optionPart.getExcelLineNumber() +
            // " - 등록하실 Excel파일의 OPTION_RESULT 조건이 없습니다.");
            throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistOptionResultCondition",
                    new String[] { optionPart.getExcelLineNumber() }));
        }
        // progressingText.append(" >>>>>>>> STATEMENT : " + optionStatement +
        // "\n");
        // Excel에서 파싱한 문장을 TC에 맞게 컨버젼한 문구로 설정한다.
        optionPart.setStrCondition(optionStatement);
        return optionValueList;
    }

    /**
     * 팀센터 조건 Key / Value값을 생성한다.
     * 
     * --> 00021:A01 = "A0101"
     * 
     * @method getSetOptionItemKeyValue
     * @date 2013. 2. 8.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    private String getSetOptionItemKeyValue(OptionPart vehiclePart, String value) {
        HashMap<String, String> setOptionList = vehiclePart.getSetOptionValueList();
        return setOptionList.get(value) + " = \"" + value + "\"";
    }

    /**
     * '\n'(OR)을 배열의 열 문장을 가지고 AND 배열 문구를 추출한다.
     * 
     * @method getOrStrConditions
     * @date 2013. 2. 8.
     * @param
     * @return String[]
     * @exception
     * @throws
     * @see
     */
    private String[] getAndStrConditions(String orString) {
        if (orString == null) {
            return null;
        }
        // StringUtil.replace(orString, "AND", "and");
        orString = orString.replaceAll("AND", "&");
        orString = orString.replaceAll("and", "&");
        String[] andStrConditions = StringUtil.getSplitString(orString, "&");
        for (int i = 0; andStrConditions != null && i < andStrConditions.length; i++) {
            andStrConditions[i] = StringUtil.nullToString(andStrConditions[i]); // 공백
                                                                                // Trim처리
        }
        return andStrConditions;
    }

    /**
     * Option값 저장 실행
     * 
     * @method validateAndSet
     * @date 2013. 2. 7.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see com.teamcenter.rac.pse.variants.modularvariants.MVConditionDialog -
     *      validateAndSet 참조
     */
    public void run() {
        progressingText.append("4. Run...\n");
        if (this.pushOptionData == null) {
            progressingText.append("[ERROR] Validation Data가 존재하지 않습니다.\n");
        }
        try {
            Set<String> keySet = this.pushOptionData.keySet();
            for (String fmId : keySet) {
                ArrayList<OptionPart> vehiclePartList = this.pushOptionData.get(fmId);
                for (OptionPart vehiclePart : vehiclePartList) {
                    this.saveCondition(vehiclePart);
                }
            }
            progressingText.append("[OK]\n");
        } catch (RuntimeException re) {
            re.printStackTrace();
            progressingText.append("[ERROR] unexpect error!!\n");
        } catch (TCException te) {
            te.printStackTrace();
            progressingText.append("[ERROR] TC Error.\n");
        }
    }

    /**
     * Option 설정 저장
     * 
     * @method saveCondition
     * @date 2013. 2. 7.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void saveCondition(OptionPart vehiclePart) throws TCException {
        // TCVariantService svc = session.getVariantService();
        // String oldVC =
        // vehiclePart.getTccomponentbomline().getProperty("bl_variant_condition");
        // svc.setLineMvlCondition(vehiclePart.getTccomponentbomline(),
        // vehiclePart.getStrCondition());
        ((SYMCBOMLine) vehiclePart.getTccomponentbomline()).setMVLCondition(vehiclePart.getStrCondition());
    }
}
