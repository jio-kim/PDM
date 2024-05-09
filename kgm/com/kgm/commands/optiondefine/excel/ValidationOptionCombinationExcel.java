package com.kgm.commands.optiondefine.excel;

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

import com.kgm.commands.optiondefine.excel.exception.ValidationOptionCombinationException;
import com.kgm.commands.optiondefine.excel.vo.OptionPart;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
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
     * Excel Validation�� Excel������ �Ľ��Ͽ� ���� FM ������ �ִ� VehiclePart List
     * key:Function Master Id, value:ArrayList<VehiclePart>
     */
    private HashMap<String, ArrayList<OptionPart>> pushOptionData;
    /**
     * Excel Validation�� �����Ϳ��� �����ϴ� FM ������ �ִ� VehiclePart List key:Function
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
            int startDataRow = HEADER_START_Y_POS + 1; // ���� ����Ÿ�� �ִ� Row
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
					 * SRME:: [][201400806] Excel�� ���� �ɼ� Import�� 4000�ʰ��� ���� �߻�. 
					 */	    
                    // [CSH]4000 byte �߰����� ���� ����.
                    String optionStr = optionPart.getStrCondition().replace("\n", " OR ");
//                    System.out.println("optionStr.length() " + optionStr.length());
//                    System.out.println("optionPart.getStrCondition().length() " + optionPart.getStrCondition().length());
                    if( optionStr.length() > 4000){
//                    if( optionPart.getStrCondition().length() > 4000){
                    	String errorString = "[Error][ Excel " + (r + 1) + " Line ]Can not exceed 4000 Byte option.(" + optionStr.length() + "Byte)\n�Է��� Option�� 4000 Byte�� �ʰ��Ͽ����ϴ�.\nOption ���� �� �ٽ� �����Ͽ� �ֽʽÿ�.\n";
                        progressingText.append(errorString + "\n");
                        pushOptionData = null;
                        return;
                    }
                    
					//SRME:: [][20140812] Ư�� category�� �ٸ� Ư�� category�� �Բ� �������� ����(special country)
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
                    // �̹� ��� FM�� �����ϴ��� Ȯ��
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
                // TC Function Master Item�� ������ �Ѵ�.
                this.checkTcBomLine();
                // TC Function Master Item�� EXCEL �Ľ� �����͸� �񱳰����� �Ѵ�.
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
     * TC Function Master Item�� ����
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
        // BOMLine Load üũ
        if (targetBOMLines == null || targetBOMLines.length == 0) {
            // throw new
            // ValidationOptionCombinationException("Function Item�� Structure Open �ϼ���.");
            throw new ValidationOptionCombinationException(StringUtil.getTextBundle(registry, "functionItemStrManOpen", null, dlgClass));
        }
        TCComponentBOMLine topLine = targetBOMLines[0].window().getTopBOMLine();
        // Function Item üũ
        if (!"S7_Function".equals(topLine.getItem().getType())) {
            // throw new
            // ValidationOptionCombinationException("Item�� Function Type�� �ƴմϴ�.");
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
                // Option Condition List ��ȸ
                String lineMvl = variantService.askLineMvlCondition(optionItemBOMLine);
                ConditionElement[] conditionElements = ConstraintsModel.parseACondition(lineMvl);
                optionPart.setConditionElements(conditionElements);
                // BOMLine ������ �����Ѵ�.
                optionPart.setTccomponentbomline(optionItemBOMLine);
                // Function Master Option����(check item -> fm Item -> function
                // Item) ����Ʈ�� �����Ѵ�.
                optionPart.setSetOptions(this.getSetOptions(optionPart.getTccomponentbomline().parent().parent()));
                // Function�� ���ǵ� Option���� Value List�� �����Ѵ�.
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
     * Function �� ������ Set Option List�� �����´�. (Optopn Value ���� ��)
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
            // Function���� Set Option List�� �����´�.
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
                // ValidationOptionCombinationException("Function Master Item�� Option ������ �Ǿ������ʽ��ϴ�.");
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
            // ValidationOptionCombinationException("Function Master Item ������ Ȯ�ιٶ��ϴ�.");
            throw new ValidationOptionCombinationException(StringUtil.getTextBundle(registry, "excelErrorCheckFmItem", "MSG", dlgClass));
        }
        return setOptions;
    }

    /**
     * Function�� ���ǵ� Option���� Value List�� �����Ѵ�.
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
     * Excel ���� �Ľ� ������ TC ������ ���Ͽ� Validation Check
     * 
     * *********** ����: ���� Part No�� ���� ��� ó����� �ذ��� �� *********************
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
            ArrayList<OptionPart> optionItemPartList = this.pushOptionData.get(fmId); // ExcelOptionPart������
            for (OptionPart optionPart : optionItemPartList) {
                // EXCEL FM ID�� TC FM ID�� �����ϴ��� Ȯ��
                if (!this.mappingFmInfos.containsKey(optionPart.getFmId())) {
                    // throw new ValidationOptionCombinationException("Line : "
                    // + optionPart.getExcelLineNumber() +
                    // " - ����Ͻ� Excel������ Function Maser�� TC�� �������� �ʽ��ϴ�.");
                    throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistFmItem",
                            new String[] { optionPart.getExcelLineNumber() }));
                }
                ArrayList<OptionPart> mappingOptionPartList = this.mappingFmInfos.get(optionPart.getFmId());
                boolean checkOptionItemPart = false;
                for (OptionPart mappingVehicle : mappingOptionPartList) {
                    // Part No �ߺ��� ���� ��� -- PartNo, Part Qty�� ����
                    if ((mappingVehicle.getItemId().equals(optionPart.getItemId()))) {
                        try {
                            String bomlineQty = StringUtil.nullToString((mappingVehicle.getTccomponentbomline().getProperty("bl_quantity") + ""));
                            // BOM Line �����Ϳ� Excel���� �����Ͱ� ������ ������ '1'�� �ʱ�ȭ�Ѵ�.
                            if ("".equals(bomlineQty)) {
                                bomlineQty = "1";
                            } else {
                                // ������ ������ ������ �����Ƿ� ������ ������ ����
                                bomlineQty = (new Double(bomlineQty)).intValue() + "";
                            }
                            if ("".equals(optionPart.getQty())) {
                                optionPart.setQty("1");
                            }
                            // Part No, Qty�� ������ �ߺ� PartNo�� ���Ѵ�.
                            if (optionPart.getQty().equals(bomlineQty)) {
                                /**
                                 * TC���� ��ȸ�� �ش� VEHICLE PART�� Condition Element
                                 * List, BOMLine�� Excel Parsing VEHICLE PART��
                                 * ����Ѵ�.
                                 */
                                optionPart.setConditionElements(mappingVehicle.getConditionElements());
                                // BOMLine ������ �����Ѵ�.
                                optionPart.setTccomponentbomline(mappingVehicle.getTccomponentbomline());
                                // Function Master Option���� ����Ʈ�� �����Ѵ�.
                                optionPart.setSetOptions(mappingVehicle.getSetOptions());
                                // Function�� ���ǵ� Option���� Value List�� �����Ѵ�.
                                optionPart.setSetOptionValueList(mappingVehicle.getSetOptionValueList());
                                // Option ���� Condition�� üũ�Ѵ�.
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
                    // " - ����Ͻ� Excel������ PARTNO�� TC�� �������� �ʽ��ϴ�.");
                    throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistPartNumber",
                            new String[] { optionPart.getExcelLineNumber() }));
                }
            }
        }
        progressingText.append("[OK]\n");
    }

    /**
     * TC���� ��ȸ�� �ش� VEHICLE PART�� Condition Element List�� ������ Excel���� �Ľ��� Option
     * Condition�� valid���� üũ�Ѵ�.
     * 
     * üũ�Ϸ� �Ŀ��� ����ǽ��� TC ���忡 �°� �籸���Ѵ�.
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
            // Key : BOMLine�� ������ option Valuse >> "value" / Value : BOMLine��
            // ������ option Key >> "item:name"
            HashMap<String, String> setOptionList = optionPart.getSetOptionValueList();
            Set<String> keySet = setOptionList.keySet();
            for (String optionValue : keySet) {
                if (optionValue.equals(StringUtil.nullToString(optionValueList.get(oi)))) {
                    checkConditionValid = true;
                    break;
                }

            }
            // ����� ���� ���ռ� üũ
            if (checkConditionValid == false) {
                // throw new ValidationOptionCombinationException("Line : " +
                // vehiclePart.getExcelLineNumber() +
                // " - ����Ͻ� Excel������ OPTION_RESULT�� TC�� �������� �ʽ��ϴ�.");
                throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistOptionResult",
                        new String[] { optionPart.getExcelLineNumber() }));
            }
        }
    }

    /**
     * OPTION_RESULT ���忡�� '\n'(OR)�� �迭 Ÿ������ �����Ѵ�.
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
     * OPTION_RESULT ������ OR �迭�� �и��� ������ AND ���� üũ, ���� �� TC��� ������ �����Ѵ�. ������ ���Ŀ�
     * ���Ǵ� ��ü Option Value���� �����Ѵ�. (Option �������� ���ϱ� ���� �����Ѵ�.)
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
            // " - ����Ͻ� Excel������ OPTION_RESULT ������ �����ϴ�.");
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
            // " - ����Ͻ� Excel������ OPTION_RESULT ������ �����ϴ�.");
            throw new ValidationOptionCombinationException(StringUtil.getString(registry, "VariantOptionCombinationDialog.MSG.excelErrorNotExistOptionResultCondition",
                    new String[] { optionPart.getExcelLineNumber() }));
        }
        // progressingText.append(" >>>>>>>> STATEMENT : " + optionStatement +
        // "\n");
        // Excel���� �Ľ��� ������ TC�� �°� �������� ������ �����Ѵ�.
        optionPart.setStrCondition(optionStatement);
        return optionValueList;
    }

    /**
     * ������ ���� Key / Value���� �����Ѵ�.
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
     * '\n'(OR)�� �迭�� �� ������ ������ AND �迭 ������ �����Ѵ�.
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
            andStrConditions[i] = StringUtil.nullToString(andStrConditions[i]); // ����
                                                                                // Trimó��
        }
        return andStrConditions;
    }

    /**
     * Option�� ���� ����
     * 
     * @method validateAndSet
     * @date 2013. 2. 7.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see com.teamcenter.rac.pse.variants.modularvariants.MVConditionDialog -
     *      validateAndSet ����
     */
    public void run() {
        progressingText.append("4. Run...\n");
        if (this.pushOptionData == null) {
            progressingText.append("[ERROR] Validation Data�� �������� �ʽ��ϴ�.\n");
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
     * Option ���� ����
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
