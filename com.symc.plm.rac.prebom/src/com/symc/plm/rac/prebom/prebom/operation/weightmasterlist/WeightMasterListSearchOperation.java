/**
 * 
 */
package com.symc.plm.rac.prebom.prebom.operation.weightmasterlist;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.commands.ospec.op.OpValueName;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.lov.SYMCLOVLoader;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.prebom.dialog.weightmasterlist.WeightMasterListDialog.CustomTableModel;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.RevisionRuleEntry;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;

/**
 * @author jinil
 *[20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
public class WeightMasterListSearchOperation extends AbstractAIFOperation {
    private TCComponentItem productItem;
    private ArrayList<StoredOptionSet> selectedSOS;
    private HashMap<String, OpValueName> wtOption;
    private HashMap<String, OpValueName> tmOption;
    private HashMap<String, Vector<String>> tableDataMap = new HashMap<String, Vector<String>>();
    private WaitProgressBar waitBar;
    private Date confDate1;
    private Date confDate2;
    private JTable targetTable;
    private int defaultHeaderCount;
    private Vector<String> totalSumVector = new Vector<String>();
    private ArrayList<String> sameWeightPartLists = new ArrayList<String>();
    private HashMap<String, String> bom1PartWeightMap = new HashMap<String, String>();
    private HashMap<String, String> bom2PartWeightMap = new HashMap<String, String>();
    private static String SearchRevisionRuleName = "Latest Released_revision_rule";
    private HashMap<String, String> tableIndexItemID = new HashMap<String, String>();
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("js"); 
    private String targetFunctionNo = null; // 조회대상 Function No
    private int fmp_inx = 5;
	private int seq_inx = 6;
	private int smode_inx = 10;
	private HashMap<String, String> systemCodeMap;
	private ArrayList<String> scodeListToEnableSum = getSCodeListToEnableSum();

    public WeightMasterListSearchOperation(JTable table, TCComponentItem selectedProduct, ArrayList<StoredOptionSet> selectedSOS, HashMap<String, OpValueName> wtOption, HashMap<String, OpValueName> tmOption, Date fDate, Date tDate, int defaultHeaderCount, String targetFunctionNo, WaitProgressBar waitBar) {
        this.targetTable = table;
        this.productItem = selectedProduct;
        this.selectedSOS = selectedSOS;
        this.wtOption = wtOption;
        this.tmOption = tmOption;
        this.confDate1 = fDate;
        this.confDate2 = tDate;
        this.defaultHeaderCount = defaultHeaderCount;
        this.waitBar = waitBar;
        this.targetFunctionNo= targetFunctionNo;
    }

    /* (non-Javadoc)
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        try
        {
        	systemCodeMap = new HashMap<>();
        	ListOfValuesInfo lovinfo = SYMCLOVLoader.getLOV("S7_SYSTEM_CODE").getListOfValues();
        	String[] systemCodes = SYMCLOVLoader.getLOV("S7_SYSTEM_CODE").getListOfValues().getStringListOfValues();
        	String[] systemNames = SYMCLOVLoader.getLOV("S7_SYSTEM_CODE").getListOfValues().getDescriptions();
        	for(int i=0; i < systemCodes.length; i++){
        		systemCodeMap.put(systemCodes[i], systemNames[i]);
        	}
            
            ArrayList<StoredOptionSet> usageOptionSetList = new ArrayList<StoredOptionSet>();
            for (StoredOptionSet sos : selectedSOS)
            {
                for (String wtKey : wtOption.keySet())
                {
                    for (String tmKey : tmOption.keySet())
                    {
                        StoredOptionSet copySOS = new StoredOptionSet(sos.getName());
                        StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());
                        
                        copySOS.addAll(sos.getOptionSet());

                        ArrayList<String> wtList = new ArrayList<>();
                        wtList.add(wtOption.get(wtKey).getOption());
                        copySOS.getOptionSet().put(wtOption.get(wtKey).getCategory(), wtList);
                        wtList = new ArrayList<>();
                        wtList.add(tmOption.get(tmKey).getOption());
                        copySOS.getOptionSet().put(tmOption.get(tmKey).getCategory(), wtList);

                        // [NoSR][20160321][jclee] Applying Pre OSpec Mandatory
                        sosMandatoried = setMandatory(copySOS);
                        
                        usageOptionSetList.add(sosMandatoried);
                    }
                }

                usageOptionSetList.add(null);
            }
            
            // 1. BOMLine을 얻어온다. 및 LatestReleased 로 설정되도록 한다.
            setMsg("create BOM for " + confDate1.toString() + ".");
            TCComponentBOMLine topLine = getBomline(productItem, confDate1);
            // 2. 하위를 찾으면서 SOS를 적용해 해당 BOMLine을 추출한다.
            if (topLine != null)
            {
                try
                {
                    ArrayList<BOMLineLoader> bomLoaderList = new ArrayList<BOMLineLoader>();

                    AIFComponentContext[] childContexts = topLine.getChildren();
                    
//                    int iTemp = 0;
                    for (AIFComponentContext functionContext : childContexts)
                    {
                        TCComponentBOMLine functionLine = (TCComponentBOMLine) functionContext.getComponent();
                        
                        if (functionLine.getItemRevision() != null)
                        {
                            
                            String functionNo = functionLine.getItemRevision().getProperty(IPropertyName.ITEM_ID);
                            /**
                             * 선택된 Function 에 한해서만 실행되도록 함
                             */
                            if(targetFunctionNo !=null && !functionNo.equals(targetFunctionNo))
                            	continue;
                        
                            for (AIFComponentContext fmpContext : functionLine.getChildren())
                            {
//                            	if (((TCComponentBOMLine)fmpContext.getComponent()).getItem().getProperty("item_id").equals("M26AC300PB1")) {
//									BOMLineLoader bomLoader = new BOMLineLoader((TCComponentBOMLine) fmpContext.getComponent(), usageOptionSetList, true);
//									bomLoaderList.add(bomLoader);
//								}
                                BOMLineLoader bomLoader = new BOMLineLoader((TCComponentBOMLine) fmpContext.getComponent(), usageOptionSetList, true);
                                bomLoaderList.add(bomLoader);
//                                
//                                iTemp++;
                            }
                        }
                        
//                        if (iTemp > 45) {
//							break;
//						}
                    }

                    ExecutorService executor = Executors.newFixedThreadPool(20);
                    for (BOMLineLoader loader : bomLoaderList)
                    {
                        executor.execute(loader);
                    }

                    executor.shutdown();
                    while (!executor.isTerminated())
                    {}
                }
                catch (Exception ex)
                {
                    throw ex;
                }
                finally
                {
                    topLine.window().close();
                }
            }

            if (confDate1.compareTo(confDate2) != 0)
            {
                setMsg("create BOM for " + confDate2.toString() + ".");
                TCComponentBOMLine topLine2 = getBomline(productItem, confDate2);

                if (topLine2 != null)
                {
                    ArrayList<BOMLineLoader> bomLoaderList = new ArrayList<BOMLineLoader>();

                    try
                    {
                        AIFComponentContext[] childContexts = topLine2.getChildren();
                        for (AIFComponentContext functionContext : childContexts)
                        {
                            TCComponentBOMLine functionLine = (TCComponentBOMLine) functionContext.getComponent();
                            if (functionLine.getItemRevision() != null)
                            {
                                for (AIFComponentContext fmpContext : functionLine.getChildren())
                                {
                                    BOMLineLoader bomLoader = new BOMLineLoader((TCComponentBOMLine) fmpContext.getComponent(), usageOptionSetList, false);
                                    bomLoaderList.add(bomLoader);
                                }
                            }
                        }

                        ExecutorService executor = Executors.newFixedThreadPool(20);
                        for (BOMLineLoader loader : bomLoaderList)
                        {
                            executor.execute(loader);
                        }

                        executor.shutdown();
                        while (!executor.isTerminated())
                        {}
                    }
                    catch (Exception ex)
                    {
                        throw ex;
                    }
                    finally
                    {
                        topLine2.window().close();
                    }
                }
            }

            setMsg("Working for total weight and load display.");
            
//            ArrayList<String> scodeListToEnableSum = getSCodeListToEnableSum();

            int totalSumVectorCount = usageOptionSetList.size();
            if (confDate1.compareTo(confDate2) != 0)
                totalSumVectorCount = usageOptionSetList.size() * 2;
            totalSumVector.clear();
            totalSumVector.setSize(totalSumVectorCount);
            for (Vector<String> rowVector : tableDataMap.values())
            {
                if (rowVector.get(defaultHeaderCount - 1).equals("0.0"))
                    continue;

                int endColumnIndex = usageOptionSetList.size() + defaultHeaderCount;
//                String sMode = rowVector.get(smode_inx);
                
                /**
                 * 합산을 할 수 있는 S/CODE 일경우 만 합산
                 */
//                if(!scodeListToEnableSum.contains(sMode))
//                	continue;
                
                if (confDate1.compareTo(confDate2) != 0)
                    endColumnIndex = (usageOptionSetList.size() * 2) + defaultHeaderCount;
                for (int i = defaultHeaderCount; i < endColumnIndex; i++)
                {
                    int sumColIndex = i - defaultHeaderCount;
                    BigDecimal sumDecimal;
                    if (totalSumVector.get(sumColIndex) == null)
                        sumDecimal = new BigDecimal("0.0");
                    else
                        sumDecimal = new BigDecimal(totalSumVector.get(sumColIndex));

                    BigDecimal newValue;
                    int usageIndex = sumColIndex > usageOptionSetList.size() - 1 ? sumColIndex - usageOptionSetList.size() : sumColIndex;
                    if (usageOptionSetList.get(usageIndex) == null)
                    {
                        newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add(new BigDecimal(rowVector.get(i)));
                    }
                    else
                    {
                        newValue = rowVector.get(i) == null ? new BigDecimal("0.0") : sumDecimal.add((new BigDecimal(rowVector.get(i))).multiply(new BigDecimal(rowVector.get(defaultHeaderCount - 1))));
                    }

                    totalSumVector.set(sumColIndex,  newValue.toString());
                }
            }

            Vector<Vector<String>> dataVector = new Vector<Vector<String>>();
            dataVector.addAll(tableDataMap.values());
            

            Vector<String> totalSumRow = new Vector<String>();
            
//            totalSumRow.addAll(Arrays.asList(new String[]{"0", "", "", "", "", "Total Sum Weight", ""}));
            //2016-06-27 : 컬럼 추가로 인한 수정
            totalSumRow.addAll(Arrays.asList(new String[]{"0", "", "", "", "", "", "","", "", "Total Sum Weight", "", "", "", ""}));
            totalSumRow.addAll(totalSumVector);

            if (dataVector.size() > 0)
                dataVector.insertElementAt(totalSumRow, 0);
            
            Collections.sort(dataVector, new TableDataComparator());

            Vector<Object> headerIdentifier = ((CustomTableModel) targetTable.getModel()).getIdentifier();

            ArrayList<TableCellRenderer> cellRenderers = new ArrayList<TableCellRenderer>();
            ArrayList<Integer> columnWidths = new ArrayList<Integer>();
            ArrayList<TableCellRenderer> headerRenderers = new ArrayList<TableCellRenderer>();
            for (int i = 0; i < targetTable.getColumnModel().getColumnCount(); i++)
            {
                cellRenderers.add(targetTable.getColumnModel().getColumn(i).getCellRenderer());
                columnWidths.add(targetTable.getColumnModel().getColumn(i).getWidth());
                headerRenderers.add(targetTable.getColumnModel().getColumn(i).getHeaderRenderer());
            }

            ((CustomTableModel) targetTable.getModel()).setDataVector(dataVector, headerIdentifier);

            for (int i = 0; i < targetTable.getColumnModel().getColumnCount(); i++)
            {
                targetTable.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderers.get(i));
                targetTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths.get(i));
                targetTable.getColumnModel().getColumn(i).setCellRenderer(cellRenderers.get(i));
            }
            targetTable.repaint();

            ArrayList<HashMap<String, String>> partWeightMapList = new ArrayList<>();
            partWeightMapList.add(bom1PartWeightMap);
            partWeightMapList.add(bom2PartWeightMap);

            storeOperationResult(partWeightMapList);
        }
        catch (Exception ex)
        {
            storeOperationResult(ex.getMessage());
            throw ex;
        }
    }

    /**
     * [NoSR][20160315][jclee] 송대영책임 요청
     * SOS에 Pre OSpec Mandatory 적용
     * @param copySOS
     * @return
     */
    private StoredOptionSet setMandatory(StoredOptionSet sos) throws Exception {
    	ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");
        
    	StoredOptionSet sosMandatoried = new StoredOptionSet(sos.getName());
    	sosMandatoried.addAll(sos.getOptionSet());
    	
    	// Select OSpec, Trim Mandatory Option
    	String sOSpecNo = "";
    	String sTrim = "";
    	
    	sOSpecNo = productItem.getLatestItemRevision().getProperty("s7_OSPEC_NO");
    	sTrim = sos.getName().substring(0, sos.getName().indexOf('_'));
    	
    	SYMCRemoteUtil remote = new SYMCRemoteUtil();
    	DataSet ds = new DataSet();
    	ds.setString("OSPEC_NO", sOSpecNo);
    	ds.setString("TRIM", sTrim);
    	
    	@SuppressWarnings("unchecked")
		ArrayList<HashMap<String, String>> alMandatories = (ArrayList<HashMap<String, String>>) remote.execute("com.kgm.service.PreOSpecService", "selectPreOSpecMandatory", ds);
    	
    	// Applying Mandatories
    	for (int inx = 0; inx < alMandatories.size(); inx++) {
			HashMap<String, String> hmMandatory = alMandatories.get(inx);
			String sOptionCategory = hmMandatory.get("OPTION_CATEGORY");
			String sOptionValue = hmMandatory.get("OPTION_VALUE");
			String sRemarkType = hmMandatory.get("REMARK_TYPE");
			String sRemark = hmMandatory.get("REMARK");
			
			if (sRemarkType.equals("Available IF") && sosMandatoried.isInclude(engine, sRemark)) {
				// 해당 Category의 Option Value를 Mandatory Option으로 교체
				sosMandatoried.replaceOptionValue(sOptionCategory, sOptionValue);
			} else if (sRemarkType.equals("NOT Available IF") && sosMandatoried.isInclude(engine, sRemark)) {
				// 해당 Category를 사양에서 제외
				sosMandatoried.removeOptionCategory(sOptionCategory);
			}
		}
    	
		return sosMandatoried;
	}

	public HashMap<String, String> getTableIndexItemIDMap() {
        return tableIndexItemID;
    }

    /**
     * 
     * @param table
     * @param dataMap
     * @param usageColumnStart
     * @param fmpLine
     * @param sosList
     * @param engine
     * @param curWTOption
     * @param curTMOption
     * @param sumVector
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private void getChildBOMLineWithSOS(JTable table, HashMap<String, Vector<String>> dataMap, int usageColumnStart, TCComponentBOMLine fmpLine, TCComponentBOMLine parentLine, ArrayList<StoredOptionSet> sosList
            , ScriptEngine engine, HashMap<String, OpValueName> curWTOption, HashMap<String, OpValueName> curTMOption, Vector<String> sumVector, boolean bomType) throws Exception {
        try
        {
            if (fmpLine.getItemRevision() == null)
                return;
            
            if (parentLine.getItemRevision() == null)
                return;

//            AIFComponentContext []childLines = fmpLine.getChildren();
            AIFComponentContext []childLines = parentLine.getChildren();
            
            String sFMP = fmpLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
            
//            for (int inx = 0; inx < childLines.length; inx++) {
//            	TCComponentBOMLine childBOMLine = (TCComponentBOMLine) childLines[inx].getComponent();
//            	
//            	if (childBOMLine.isPacked()) {
//					childBOMLine.unpack();
//				}
//			}
//            
//            childLines = parentLine.getChildren();
            
            for (AIFComponentContext childLine : childLines)
            {
                Vector<String> rowVector = new Vector<String>();

                TCComponentBOMLine childBOMLine = (TCComponentBOMLine) childLine.getComponent();
                if (childBOMLine.getItemRevision() == null)
                    continue;
                String smode = childBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE);
                // P8은 체크하지 않는다.
//                if (smode.equals("P8"))
//                    continue;
                if(!scodeListToEnableSum.contains(smode))
                	continue;

                TCComponentItemRevision childRevision = childBOMLine.getItemRevision();

                String curVCondition = "";
                String partNo = "";
                String itemId = "";
                String parentID = "";
                String seqNo = "";
                String project = "";
                String nmcd = "";
                String nmcdCD = "";
                String nmcdNM = "";
                String childType = "";
                String system_code = "";
                
                
                double partWeight = 0;

                String[] propValues = childRevision.getProperties(new String[]{PropertyConstant.ATTR_NAME_DISPLAYPARTNO, PropertyConstant.ATTR_NAME_ESTWEIGHT, PropertyConstant.ATTR_NAME_BUDGETCODE, PropertyConstant.ATTR_NAME_ITEMNAME, PropertyConstant.ATTR_NAME_ITEMID, PropertyConstant.ATTR_NAME_ACTWEIGHT, PropertyConstant.ATTR_NAME_ITEMTYPE, PropertyConstant.ATTR_NAME_CHG_TYPE_NM, PropertyConstant.ATTR_NAME_PROJCODE, PropertyConstant.ATTR_NAME_PRD_PROJ_CODE});
                String[] bomValues = childBOMLine.getProperties(new String[] {PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM, PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY, PropertyConstant.ATTR_NAME_BL_VARIANTCONDITION, PropertyConstant.ATTR_NAME_BL_BUDGETCODE, PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO, PropertyConstant.ATTR_NAME_BL_CHG_CD});
                
                partNo = propValues[0];
                itemId = propValues[4];
                childType = propValues[6];
                nmcdNM = propValues[7];
                
                curVCondition = bomValues[2];
                system_code = bomValues[3];
                seqNo = bomValues[4];
                nmcdCD = bomValues[5];
                
                //NMCD 값이 BOM 속성과 Part 속성으로 분리되어있네....
                //Part Type에 다른 nmcd, project 출력
                if (childType.equals("Pre Vehicle Part Revision")) {
                	if (nmcdCD == null  || nmcdCD.trim().equals("")){
                		nmcd = nmcdNM;
                	} else {
                    	nmcd = nmcdCD;
                	}
                	
                	if (nmcdNM != null && (nmcdCD.equals("C") || nmcdNM.equals("N") || nmcdCD.equals("D")))
                    {
                		project = propValues[8];
                    }
                    else if (nmcdNM != null && (nmcdNM.contains("M")))
                    {
                    	project = propValues[9];
                    }
                    else
                    {
                    	project = propValues[8];
                    }
                } else {
                	nmcd = nmcdCD;
                	project = propValues[8];
                }
                
//                if (propValues[7] != null && propValues[7].trim().length() > 0) {
                if (propValues[5] != null && propValues[5].trim().length() > 0 && !propValues[5].trim().equals("0")) {
                	partWeight = Double.valueOf(propValues[5]);
				} else if (propValues[1] != null && propValues[1].trim().length() > 0) {
                	partWeight = Double.valueOf(propValues[1]);
                }

                rowVector.add((dataMap.keySet().size() + 1) + "");
                parentID = parentLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
//                seqNo = childBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO);
                
                String sKey = itemId + "#" + sFMP + "#" + parentID + "#" + seqNo;
                
//                if (sFMP.equals("M460C300PB1") && propValues[5].equals("BOLT-HEX SEMS_M8x1.25x16")) {
//					System.out.println();
//				}
                
                /**
                 * [20150922][jclee] NMCD가 C인 경우 BOMLine의 Owner로 정보 대체
                 * [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동으로 인해 Change Type이 C, D인 경우에도 BOM Line에서 설계자 정보를 가져오도록 수정
                 */
//                rowVector.add(system_code);
//                rowVector.add(propValues[3]);
//                String propertyCD = childBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_CHG_CD);
//                if (propertyCD != null && propertyCD.equals("C")) {
//                	String sGroup = "";
//                	String sOwner = "";
//                	
//                	TCComponentGroup group = (TCComponentGroup)fmpLine.getBOMViewRevision().getReferenceProperty(PropertyConstant.ATTR_NAME_OWNINGGROUP);
//                	TCComponentUser owner = (TCComponentUser)fmpLine.getBOMViewRevision().getReferenceProperty(PropertyConstant.ATTR_NAME_OWNINGUSER);
//                	
//                	sGroup = group.getProperty(PropertyConstant.ATTR_NAME_NAME);
//                	sOwner = owner.getProperty(PropertyConstant.ATTR_NAME_USERNAME);
//                	
//                	rowVector.add(sGroup);
//                	rowVector.add(sOwner);
//				} else {
//					rowVector.add(system_code);
//					rowVector.add(propValues[3]);
//				}
//                String[] sEngInfoValue = childBOMLine.getProperties(new String[] {PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM, PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY});
                rowVector.add(bomValues[0]);
                rowVector.add(bomValues[1]);
                
                /**
                 * [20150922][jclee] System Code가 Null일 경우 X00으로 대체
                 */
//                rowVector.add(propValues[4]);
//                rowVector.add(propValues[4] == null || propValues[4].equals("") ? "X00" : propValues[4]);
                rowVector.add(system_code == null || system_code.equals("") ? "X00" : system_code);
                rowVector.add(systemCodeMap.get(system_code));
                
                /**
                 * [20150922][jclee]FMP Code 추가
                 */
//                rowVector.add(fmpLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID));
                rowVector.add(sFMP);
                String levM = childBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_LEV_M);
                
                //2016-06-27 :  SEQ, LEV(M) 추가
                rowVector.add(seqNo);
                rowVector.add(levM);
                
                rowVector.add(partNo);
                rowVector.add(propValues[3]);
                //2016-06-27 : S/MODE 추가
                rowVector.add(smode);
                rowVector.add(project);
                rowVector.add(nmcd);
                rowVector.add(partWeight + "");

//                if (! bomType && ! dataMap.containsKey(itemId))
                if (! bomType && ! dataMap.containsKey(sKey))
                {
                    for (StoredOptionSet sos : sosList)
                    {
                        rowVector.add("0");
                    }
                }

                int usageIndex = usageColumnStart;
                if (! bomType)
                    usageIndex += sosList.size();

                boolean bIsVCInclude = false;
                int rowAveQty = 0;
                int rowAveDiv = 0;
                for (StoredOptionSet curSOS : sosList)
                {
                    if (curSOS == null)
                    {
                        try
                        {
//                            if (dataMap.containsKey(itemId))
                            if (dataMap.containsKey(sKey))
                            {
                                try {
//                                    rowVector = dataMap.get(itemId);
                                    rowVector = dataMap.get(sKey);
                                    partWeight = Double.valueOf(rowVector.get(defaultHeaderCount - 1));
                                } catch (Exception ex)
                                {
                                    throw ex;
                                }
                            }

                            if (! bomType && rowVector.size() < usageIndex)
                                for (int i = rowVector.size(); i < usageIndex; i++)
                                    rowVector.add("0");

                            BigDecimal newValue;
                            try {
                                newValue = (rowVector.get(usageIndex - 1) == null) ? new BigDecimal("0.0") : new BigDecimal(rowVector.get(usageIndex - 1)).multiply(BigDecimal.valueOf(partWeight));
                            } catch (Exception ex) {
                                throw ex;
                            }

                            try {
                                if (rowVector.size() - 1 < usageIndex)
                                    rowVector.add(newValue.toString());
                                else
                                    rowVector.set(usageIndex, newValue.toString());
                            } catch (Exception ex) {
                                throw ex;
                            }
                        }
                        catch (Exception ex)
                        {
                            throw ex;
                        }
                    }
                    else
                    {
                        boolean isInclude = false;
                        if (curVCondition == null || curVCondition.equals(""))
                            isInclude = true;
                        else
                            isInclude = curSOS.isInclude(engine, BomUtil.convertToSimpleCondition(curVCondition));
                        if (isInclude)
                        {
                            bIsVCInclude = true;

//                            if (dataMap.containsKey(itemId))
                            if (dataMap.containsKey(sKey))
                            {
//                                rowVector = dataMap.get(itemId);
                                rowVector = dataMap.get(sKey);
                                if (rowVector.size() > usageIndex)
                                {
                                    rowVector.set(usageIndex, (Integer.valueOf(rowVector.get(usageIndex)) + 1) + "");
                                }
                                else
                                {
//                                    rowVector.add("1");
                                    rowVector.add(childBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_QUANTITY));
                                }

                                if (confDate1.compareTo(confDate2) != 0)
                                {
                                    if (rowVector.get(defaultHeaderCount - 1).equals(partWeight))
                                    {
                                        if (! sameWeightPartLists.contains(partNo))
                                            sameWeightPartLists.add(partNo);
                                    }
                                    else
                                    {
                                        if (sameWeightPartLists.contains(partNo))
                                            sameWeightPartLists.remove(partNo);
                                    }
                                }
                            }
                            else
                            {
//                                rowVector.add("1");
                                rowVector.add(childBOMLine.getProperty(PropertyConstant.ATTR_NAME_BL_QUANTITY));
                            }
                        }
                        else
                        {
                            rowVector.add("0");
                        }
                    }

                    usageIndex++;
                }

                if (bomType)
                {
//                    if (! bom1PartWeightMap.containsKey(itemId))
//                        bom1PartWeightMap.put(itemId, partWeight + "");
                    if (! bom1PartWeightMap.containsKey(sKey))
                        bom1PartWeightMap.put(sKey, partWeight + "");
                }
                else
                {
//                    if (! bom2PartWeightMap.containsKey(itemId))
//                        bom2PartWeightMap.put(itemId, partWeight + "");
                    if (! bom2PartWeightMap.containsKey(sKey))
                        bom2PartWeightMap.put(sKey, partWeight + "");
                }

                if (bIsVCInclude)
                {
//                    if (! dataMap.containsKey(itemId))
//                    {
//                        tableIndexItemID.put(dataMap.keySet().size() + "", itemId);
//                        dataMap.put(itemId, rowVector);
//                    }
                    if (! dataMap.containsKey(sKey))
                    {
                        tableIndexItemID.put(dataMap.keySet().size() + "", sKey);
                        dataMap.put(sKey, rowVector);
                    }
                }
                
//                getChildBOMLineWithSOS(table, dataMap, usageColumnStart, fmpLine, childBOMLine, sosList, engine, curWTOption, curTMOption, sumVector, bomType);
            }

//            for (int inx = 0; inx < childLines.length; inx++) {
//            	TCComponentBOMLine childBOMLine = (TCComponentBOMLine) childLines[inx].getComponent();
//				childBOMLine.pack();
//			}

            System.gc();
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private TCComponentBOMLine getBomline(TCComponentItem topItem, Date releaseDate) throws Exception {
        try
        {
            TCComponentItemRevision releasedRevision = BomUtil.getItemRevisionOnReleaseDate(topItem, releaseDate);
            if (releasedRevision == null)
                return null;

            TCComponentBOMWindowType winType = (TCComponentBOMWindowType) topItem.getSession().getTypeComponent("BOMWindow");
            TCComponentRevisionRule latestReleasedRule = null;
            TCComponentRevisionRule[] allRevisionRules = TCComponentRevisionRule.listAllRules(topItem.getSession());
            for (TCComponentRevisionRule revisionRule : allRevisionRules) {
                String ruleName = revisionRule.getProperty("object_name");
                if (ruleName.equals(SearchRevisionRuleName)) {
                    latestReleasedRule = revisionRule;
                    break;
                }
            }
            if (latestReleasedRule != null)
            {
                RevisionRuleEntry []ruleEntries = latestReleasedRule.getEntries();
                for (RevisionRuleEntry ruleEntry : ruleEntries)
                {
                    if (ruleEntry.getEntryText().startsWith("Has Status"))
                    {
                        ruleEntry.getTCComponent().setLogicalProperty("date_today", false);
                        ruleEntry.getTCComponent().setDateProperty("effective_date", releaseDate);
                    }
                }
                if (! (ruleEntries != null && ruleEntries.length > 0))
                {
                    RevisionRuleEntry revRuleEntry = latestReleasedRule.createEntry(3);

                    revRuleEntry.getTCComponent().setLogicalProperty("date_today", false);
                    revRuleEntry.getTCComponent().setDateProperty("effective_date", releaseDate);

                    latestReleasedRule.addEntry(revRuleEntry);
                }
            }

            TCComponentBOMWindow window = winType.create(latestReleasedRule);
            window.setClearCacheOnClose(true);
            TCComponentBOMLine newTopLine = window.setWindowTopLine(null, releasedRevision, null, null);

            return newTopLine;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private synchronized void setMsg(String msg)
    {
        if (waitBar != null)
        {
            waitBar.setStatus(msg);
        }
    }

    class BOMLineLoader implements Runnable
    {
        private TCComponentBOMLine fmpLine;
        private ArrayList<StoredOptionSet> usageOptionSetList;
        private boolean isBOMType;

        public BOMLineLoader(TCComponentBOMLine fmpLine, ArrayList<StoredOptionSet> sosList, boolean isBOMType){
            this.fmpLine = fmpLine;
            this.usageOptionSetList = sosList;
            this.isBOMType = isBOMType;
        }

        @Override
        public void run()
        {
            try
            {
                String itemId = fmpLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
                setMsg( "Loading Start " + itemId + " BOM Info.");

                getChildBOMLineWithSOS(targetTable, tableDataMap, defaultHeaderCount, fmpLine, fmpLine, usageOptionSetList, engine, wtOption, tmOption, totalSumVector, isBOMType);
                
                setMsg( "Loading Complete " + itemId + " BOM Info.");
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }
    
    /**
     * FMP, SEQ 로 Sort 함
     */
    private class TableDataComparator implements Comparator<Vector<String>>
    {

		@Override
		public int compare(Vector<String> paramT1, Vector<String> paramT2) {

			String fmpNo1 = paramT1.get(fmp_inx);
			String seq1 = paramT1.get(seq_inx);

			String fmpNo2 = paramT2.get(fmp_inx);
			String seq2 = paramT2.get(seq_inx);
			
            int firstCompare = fmpNo1.compareTo(fmpNo2);
            if (firstCompare != 0)
                return firstCompare;
            else
                return seq1.compareTo(seq2);
		}
    	
    }
    
    /**
     * 합산을 적용하는 Supply Code 리스트
     * @return
     */
    public static ArrayList<String> getSCodeListToEnableSum()
    {
//    	return new ArrayList<String>(Arrays.asList("C0","C1","C7","C7YC8","CD","P0","P1","P1YP8","P7","P7CP8","P7MP8","P7UP8","P7YP8","P7ZP8","PD","PDYP8"));
    	return new ArrayList<String>(Arrays.asList("C1","C7","CD","P1","P1YP8","P7","P7CP8","P7MP8","P7UP8","P7YP8","P7ZP8","PD","PDYP8"));
    }
    
}
