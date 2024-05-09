/**
 * 
 */
package com.symc.plm.me.sdv.operation.common;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import jxl.Image;

import org.apache.poi.POIXMLDocumentPart;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackageRelationshipCollection;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFChildAnchor;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFConnector;
import org.apache.poi.xssf.usermodel.XSSFCreationHelper;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFPictureData;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.poi.xssf.usermodel.XSSFShapeGroup;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFSimpleShape;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTConnector;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTGraphicalObjectFrame;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTGroupShape;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture;
import org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTSheetProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTWorkbookProtection;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.STUnsignedShortHex;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.kgm.dto.EndItemData;
import com.kgm.commands.ec.ecostatus.utility.CopyExcelSheet;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomBOPDao;
import com.symc.plm.me.sdv.excel.common.ExcelTemplateHelper;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.symc.plm.me.sdv.excel.transformer.PreviewWeldConditionSheetExcelTransformer;
import com.symc.plm.me.sdv.operation.ps.ProcessSheetDataHelper;
import com.symc.plm.me.sdv.operation.wp.PreviewWeldConditionSheetDataHelper;
import com.symc.plm.me.utils.BOPLineUtility;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.symc.plm.me.utils.TcDefinition;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.kernel.bvr.TCComponentMfgBvrBOMLine;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentMEOP;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 *[SR150122-027][20150210] shcho, Find automatically replaced end item (���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ�) (10�������� ���� �� �ҽ� 9���� �̽���)
 *[SR����][20150217]shcho, PE I/F���� ���ÿ��� �ڵ� Replace�� ������ üũ���� �ʵ��� ���� (ReviseActionOperation�� PE I/F������ �Բ� ����ϱ� ����)
 *[SR150122-027][20150414] shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link����
 *[CF-3537] [20230131]isWorkingStatus�� �ݷ��� MECO�� ���� �� �ְ� ���� ���� SearchTypeItemView���� MecoSearchView �˻�â���� ����
 *[20240312][UPGRADE] BOMLine Ȯ���� ó���ϴ� ���� �߰�
 *
 */
public class ReviseActionOperation extends AbstractSDVActionOperation {
    private Registry registry = Registry.getRegistry(ReviseActionOperation.class);
    private TCComponentBOPWindow newBopWindow;
    
    private TCComponent mProductRevision;
    
    /** PE I/F ȣ�� Flag **/
    private boolean peFlag = false;

    /**
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public ReviseActionOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
    }
    
    /** PE I/F ����
     * @param actionId
     * @param ownerId
     * @param dataSet
     */
    public ReviseActionOperation(int actionId, String ownerId, IDataSet dataSet, boolean peFlag) {
        super(actionId, ownerId, dataSet);
        this.peFlag = peFlag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#startOperation(java.lang.String)
     */
    @Override
    public void startOperation(String commandId) {
        try {
            newBopWindow = SDVBOPUtilities.createBOPWindow("Latest Working For ME");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.sdv.core.common.ISDVOperation#endOperation()
     */
    @Override
    public void endOperation() {
        if(newBopWindow != null) {
            try {
                newBopWindow.close();
            } catch (TCException e) {
                e.printStackTrace();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.teamcenter.rac.aif.AbstractAIFOperation#executeOperation()
     */
    @Override
    public void executeOperation() throws Exception {
        IDataSet dataSet = getDataSet();
        revise(dataSet);
    }

    /**
     * Revise execute
     * 
     * @method revise
     * @date 2013. 12. 17.
     * @param
     * @return TCComponentItemRevision
     * @exception
     * @throws
     * @see
     */
    public TCComponentItemRevision revise(IDataSet dataSet) throws Exception {
        TCComponentItemRevision newRevision = null;
        ArrayList<Object> reviseTargetList = new ArrayList<Object>();
	    /* [CF-3537] [20230131] ���� �˻� ȭ�鿡�� �ݷ��� MECO�� �˻� �ȵǴ� ������ �־ �Ʒ� �������� ���� 
	    isWorkingStatus�� �ݷ��� MECO�� ���� �� �ְ� ���� ���� SearchTypeItemView���� MecoSearchView �˻�â���� ����  �Ʒ� getValue�κп� ȭ���� key���� ���� ���� �Ӽ��� ����*/
        Object meco_no = dataSet.getValue("reviseMecoView", SDVPropertyConstant.SHOP_REV_MECO_NO);
        if(meco_no == null)
        	meco_no = dataSet.getValue("reviseMecoView", SDVPropertyConstant.MECO_REV );
        
        // [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
        if(meco_no!=null){
        	if(meco_no instanceof TCComponentItemRevision){
        		TCComponentItemRevision mecoRevision = isOwnedMECO((TCComponentItemRevision)meco_no);
        		if(mecoRevision==null){
        			throw new Exception("Check MECO owning user");
        		}
        	}
        }
        
        reviseTargetList.addAll(dataSet.getListValue("reviseView", "reviseView"));
        Object isSkipMECO = dataSet.getValue("reviseView", "SkipMECO");

        if (isSkipMECO == null || (isSkipMECO != null && !((Boolean) isSkipMECO))) {
            if (meco_no == null) {
                throw new NullPointerException("MECO is null.");
            }
            if (!(meco_no instanceof TCComponentChangeItemRevision)) {
                throw new Exception("MECO Type is mismatch.");
            }
        }

        TCSession session = CustomUtil.getTCSession();
        // Mark Point
        Markpoint mp = new Markpoint(session);
//        if(meco_no != null) {
//        	isSkipMECO = false;
//        	
//        } else {
//        	isSkipMECO = true;
//        }
//        
        try {
        	
        	if( (isSkipMECO != null && ((Boolean) isSkipMECO)) && meco_no == null )  {
        		 for (Object reviseTarget : reviseTargetList) {
        			 TCComponentItemRevision oldRevision = null;
                     if (reviseTarget instanceof TCComponentItemRevision) {
                         oldRevision = (TCComponentItemRevision) reviseTarget;
                     } else if (reviseTarget instanceof TCComponentMfgBvrBOMLine) {
                         oldRevision = ((TCComponentMfgBvrBOMLine) reviseTarget).getItemRevision();
                     }
                     
                     
                     String newRevId = oldRevision.getItem().getNewRev();
                     newRevision = oldRevision.saveAs(newRevId);
                     
                     //[20240312][UPGRADE] BOMLine�� �ƴҽ� ���� �ȵǵ��� ��
                     if(reviseTarget instanceof TCComponentBOMLine)
                     {
	                     ((TCComponentBOMLine) reviseTarget).window().newIrfWhereConfigured(newRevision);
	                     ((TCComponentBOMLine) reviseTarget).window().fireChangeEvent();
                     }
        		 }
        		 
        		return newRevision; 
        	} 
        	
        	TCComponentBOMLine topLine = ((TCComponentBOPLine) reviseTargetList.get(0)).window().getTopBOMLine();
            
            for (Object reviseTarget : reviseTargetList) {
                if (isAbortRequested()) {
                    return null;
                }
                if (!(reviseTarget instanceof TCComponentItemRevision) && !(reviseTarget instanceof TCComponentBOMLine)) {
                    setAbortRequested(true);
                    throw new Exception(registry.getString("TargetCanNotRevise.MESSAGE", "Target Object can not Revise."));
                }
                TCComponentItemRevision oldRevision = null;
                if (reviseTarget instanceof TCComponentItemRevision) {
                    oldRevision = (TCComponentItemRevision) reviseTarget;
                } else if (reviseTarget instanceof TCComponentBOMLine) {
                    oldRevision = ((TCComponentBOMLine) reviseTarget).getItemRevision();
                }
                if (!SYMTcUtil.isReleased(oldRevision)) {
                    continue;
                }
                String newRevId = oldRevision.getItem().getNewRev();
                
                // TCComponentItemRevision newRevision = oldRevision.getItem().revise(newRevId, oldRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), oldRevision.getProperty(SDVPropertyConstant.ITEM_OBJECT_DESC));
                newRevision = oldRevision.saveAs(newRevId);
                
                if (newRevision.isValidPropertyName(SDVPropertyConstant.S7_MATURITY))
                    newRevision.setProperty(SDVPropertyConstant.S7_MATURITY, "In Work");

                if (isSkipMECO == null || (isSkipMECO != null && !((Boolean) isSkipMECO))) {
                    if (newRevision.isValidPropertyName(SDVPropertyConstant.OPERATION_REV_MECO_NO)) {
                        // newRevision.setProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO, ((TCComponentItemRevision) meco_no).getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                        newRevision.getTCProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO).setReferenceValue((TCComponentItemRevision) meco_no);
                    }
                    
                    // MECO�� �����ϱ�
                    // [NON-SR][2016.01.07] taeku.jeong PE->TC Migration Test ������ Exception�߻����� Problem, Solution Items�� �̹� �����ϴ��� Check �ϵ��� ������.
                    if(CustomUtil.isExistInProblemItems((TCComponentChangeItemRevision) meco_no, oldRevision)==false){
                    	((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_PROBLEM_ITEM, oldRevision);
                    }
                    if(CustomUtil.isExistInSolutionItems((TCComponentChangeItemRevision) meco_no, newRevision)==false){
                    	((TCComponentChangeItemRevision) meco_no).add(SDVTypeConstant.MECO_SOLUTION_ITEM, newRevision);
                    }
                    
                }

                if (newRevision.getType().equals(SDVTypeConstant.STANDARD_WORK_METHOD_ITEM_REV)) {
                    updateStdWorkMethodDataset(session, newRevision);
                }
                
                // [20150209 - Replace ��� �߰�] revise ����� Operation�� ��� ������ �ڵ� Replace �� �������� �ִ��� Ȯ��
                // [SR150122-027][20150414] shcho, ���� �Ҵ� E/Item�� ���� DPV�� ���� �ڵ� ���� ���� �ذ� - Shop�� MProduct Link����
                /*
                TCComponent[] relComps = topLine.getItemRevision().getRelatedComponents(SDVTypeConstant.MFG_TARGETS);
                if(relComps != null) {
                    for(TCComponent comp : relComps) {
                        String objectType = comp.getProperty(SDVPropertyConstant.ITEM_OBJECT_TYPE);
                        if (objectType.equals(SDVTypeConstant.EBOM_MPRODUCT_REV)) {
                            mProductRevision = comp;
                            break;
                        }
                    }
                }
                */
                mProductRevision = SDVBOPUtilities.getConnectedMProductItemRevision(topLine.getItemRevision());

                boolean opFlag = false;
                ArrayList<EndItemData> endItemList = null;
                Map<String, TCComponentItemRevision> endItemRevs = null;
                //[SR����][20150217]shcho, PE I/F���� ���ÿ��� �ڵ� Replace�� ������ üũ���� �ʵ��� ���� (ReviseActionOperation�� PE I/F������ �Բ� ����ϱ� ����) 
                if(!peFlag) {
                    if(reviseTarget instanceof TCComponentBOPLine) {
                        TCComponentItem item = ((TCComponentBOPLine) reviseTarget).getItem();
                        if(item instanceof TCComponentMEOP) {
                            // �ڵ� Replace �� �������� ���� ��� MProduct���� ����� �����´�.
                            CustomBOPDao dao = new CustomBOPDao();
                            endItemList = dao.findReplacedEndItems(((TCComponentBOPLine) reviseTarget).getProperty(SDVPropertyConstant.BL_ITEM_PUID),
                                    mProductRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                            
                            List<EndItemData> targetEndItems = new ArrayList<EndItemData>();
                            if(endItemList != null) {
                                String revId = ((TCComponentBOPLine) reviseTarget).getProperty(SDVPropertyConstant.BL_ITEM_REV_ID);
                                for(EndItemData endItem : endItemList) {
                                    if(revId.equals(endItem.getPitem_revision_id())) {
                                        targetEndItems.add(endItem);
                                    }
                                }
                            }
                            
                            if(targetEndItems.size() > 0) {
                                endItemRevs = getReplacedEndItems((TCComponentBOPLine) reviseTarget, targetEndItems);
                                if( endItemRevs.size() > 0 ) {
                                	opFlag = true;
                                }
                            }
                            
                        }
                    }
                }
                newRevision.save();
                newRevision.getItem().refresh();
                
                if(opFlag) {
                    if(endItemRevs != null && endItemRevs.size() > 0) {
                        // End Item ��ü ���� Ȯ�� �޼��� ����
                        StringBuffer sb = new StringBuffer();
                        sb.append(newRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                        sb.append(" "+ "������ �ڵ� Replace �� End Item �� �ֽ��ϴ�." + "\n" + "������ �ڵ� Replace �� End Item���� �ڵ� ���� �˴ϴ�." + "\n");
                        
                        Iterator<String> it = endItemRevs.keySet().iterator();
                        while(it.hasNext()) {
                            String key = it.next();
                            sb.append(key);
                            sb.append(" -> ");
                            sb.append(endItemRevs.get(key).getProperty(SDVPropertyConstant.ITEM_ITEM_ID));
                            sb.append("\n");
                        }   
                        	/*
                        	 * BOP Revise �� ���� ������ �ڵ� Replace �� End Item �� ������ �˻� �ؼ� ��ü ������ ���� �������� 
                        	 * ��ü ���� �ʴ� ����� ���� ���� ��ü�� ����
                        	 * 
                        	 */
//                        MessageBox.post(sb.toString(), "Warning", MessageBox.WARNING);
//                        int response = ConfirmationDialog.post(registry.getString("Confirm.NAME"), sb.toString());
                        
//                        if(response == 2) {
                        	
							TCComponentBOMViewRevision viewRevision = SDVBOPUtilities.getBOMViewRevision(newRevision, "bom_view");
							newBopWindow.setWindowTopLine(newRevision.getItem(), newRevision, null, viewRevision);
							TCComponentBOMLine newOpLine = newBopWindow.getTopBOMLine();
							String newItemId = newOpLine.getProperty(SDVPropertyConstant.BL_ITEM_ID);
							
							// EndItemList�� OccThreadPuId�� �ߺ� ���� �ʵ��� List �Ѵ�.
							ArrayList<String> autoChangedOccThreadUs = new ArrayList<String>();
                            for (int i = 0;endItemList!=null && i < endItemList.size(); i++) {
								// Operation Item Id�� �����ϰ� 
								String parentNodeId = endItemList.get(i).getPitem_id();
                            	if(parentNodeId!=null && parentNodeId.trim().equalsIgnoreCase(newItemId)==true){
									String occThreadPuid = endItemList.get(i).getOcc_threadu();
									if(autoChangedOccThreadUs.contains(occThreadPuid)==false){
										autoChangedOccThreadUs.add(occThreadPuid);
									}
                            	}
                            }
                            
                            // �ڵ������ BOMLine�� ã�Ƽ� ����� Item Revsion���� �ٲ��ش�.
                            for (int i = 0;autoChangedOccThreadUs!=null && i < autoChangedOccThreadUs.size(); i++) {
                            	String occThreadPuid = autoChangedOccThreadUs.get(i);
                            	
                            	// Occurrence�� Thread PUID�� �̿��� ����� BOM Line�� ã�´�.
                            	TCComponentBOMLine[] findedBOMLines = newBopWindow.findConfigedBOMLinesForAbsOccID(occThreadPuid, true, newOpLine);
                            	for (int j = 0; j < findedBOMLines.length; j++) {
                                    TCComponentItemRevision target = endItemRevs.get(findedBOMLines[j].getProperty(SDVPropertyConstant.BL_ITEM_ID));
                                    if(target!=null){
                                    	findedBOMLines[j].replace(target.getItem(), target, null);
                                    }
								}
							}

                            newBopWindow.save();
//                        }
                    }
                }
          
                try {
    				Thread.sleep(100);
    			} catch (InterruptedException e) {
    				e.printStackTrace();
    			}
                
                // BOMLine �� ��� Refresh
                if (reviseTarget instanceof TCComponentBOMLine) {
                    ((TCComponentBOMLine) reviseTarget).window().newIrfWhereConfigured(newRevision);
                    ((TCComponentBOMLine) reviseTarget).window().fireChangeEvent();
                }
                
				//////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
				// [���� ����ǥ ���ø� ����] �������� ������ �� ���ø� -> �ű� ���ø� ���� ���� �Ǹ� 
                //   						 �̶� ���� ���� ���� 
				replaceNewWeldCondition(oldRevision, newRevision, (TCComponentBOPLine)reviseTarget);
				
				
				/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }
            
            // �̻���� ���� �Ǿ����Ƿ� ����� ������ ����ǵ��� Mark Point ����� �����Ѵ�.
            mp.forget();
            
        } catch (Exception ex) {
            mp.rollBack();
            throw ex;
        }

        return newRevision;
    }
    
	/**
	 * [SR160224-028][20160328] taeku.jeong MECO Owner Ȯ�α�� �߰�
	 * MECO�� Owner �� ���� Login �� User�� �ٸ� ��� Operation�� ���̻� ���� �� �� ������ �Ѵ�.
	 * @return
	 */
	private TCComponentItemRevision isOwnedMECO(TCComponentItemRevision mecoRevision){
		
		TCComponentItemRevision ownMecoItemRevision = null;
	
    	MecoOwnerCheckUtil aMecoOwnerCheckUtil = new MecoOwnerCheckUtil(mecoRevision, (TCSession)this.getSession());
    	ownMecoItemRevision = aMecoOwnerCheckUtil.getOwnedMecoRevision();
		
	    return ownMecoItemRevision;
	}

    /**
     * �����ͼ¿� ���� ǥ���۾���ɼ� ���� ������ ���� ���Ϸ� ���ε�
     * 
     * @method updateStdWorkMethodDataset
     * @date 2014. 1. 22.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private void updateStdWorkMethodDataset(TCSession session, TCComponentItemRevision newRevision) throws Exception {
        File updateFile = updateFile(session, newRevision);
        Vector<File> importFiles = new Vector<File>();
        importFiles.add(updateFile);
        TCComponentDataset dataSet = null;

        AIFComponentContext[] contexts = newRevision.getChildren();
        if (contexts != null) {
            for (AIFComponentContext context : contexts) {
                if (context.getComponent() instanceof TCComponentDataset) {
                    dataSet = (TCComponentDataset) context.getComponent();
                    break;
                }
            }
        }

        if (dataSet != null) {
            CustomUtil.removeAllNamedReferenceOfSWM((TCComponentDataset) dataSet);
            SYMTcUtil.importFiles(dataSet, importFiles);
            updateFile.delete();
        }
    }

    private File updateFile(TCSession session, TCComponentItemRevision newRevision) throws Exception {
        File file = getFile(newRevision, session);

        Workbook workbook = new XSSFWorkbook(new FileInputStream(file));
        Sheet sheet = workbook.getSheetAt(0);

        // ������
        Row rowApprover = sheet.getRow(3);
        rowApprover.getCell(30).setCellValue("");

        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.flush();
        fos.close();

        return file;
    }

    private static File getFile(TCComponentItemRevision itemRevision, TCSession session) throws Exception {
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        datasets = CustomUtil.getDatasets(itemRevision, TcDefinition.TC_SPECIFICATION_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        File[] localfile = null;
        localfile = CustomUtil.exportDataset(datasets.get(0), session.toString());
        return localfile[0];
    }

    private Map<String, TCComponentItemRevision> getReplacedEndItems(TCComponentBOPLine operationLine, List<EndItemData> targetEndItems) throws Exception {
        Map<String, TCComponentItemRevision> revisions = new HashMap<String, TCComponentItemRevision>();
        
        AIFComponentContext[] contexts = operationLine.getChildren();
        if(contexts != null) {
            for(EndItemData endItem : targetEndItems) {
                String occPuid = endItem.getOcc_puid();
                for(AIFComponentContext context : contexts) {
                    TCComponentBOPLine bopLine = (TCComponentBOPLine) context.getComponent();
                    if(occPuid.equals(bopLine.getProperty(SDVPropertyConstant.BL_OCC_FND_OBJECT_ID))) {
                        bopLine.setDefaultBackgroundColor(null);
                        revisions.put(endItem.getCitem_id(), findEndItemInMProduct(bopLine));
                    }
                }               
            }
        }
        
        return revisions;
    }
    
    private TCComponentItemRevision findEndItemInMProduct(TCComponentBOPLine bopLine) throws TCException, Exception {
        TCComponentItemRevision itemRevision = null;
        
        TCComponentBOMLine endItemBomline = SDVBOPUtilities.getAssignSrcBomLine(bopLine.window(), bopLine);
        if(endItemBomline != null) {
            itemRevision = endItemBomline.getItemRevision();
        }
        
        return itemRevision;
    }
    
    
    /**
     *  ������ ������ Revise �� ��� �� ��������ǥ�� Revise �� ���Ǵ� �޼���
     *  ���� ������ �� �ҽ��� �����Ǵ� �������� ���� ���� �����鿡 �ش�
     * @param bopLine
     * @throws TCException
     * @throws Exception
     */
    private void replaceNewWeldCondition(TCComponentItemRevision oldRevision, TCComponentItemRevision newRevision, TCComponentBOPLine reviseTarget) throws TCException, Exception {
    	Date oldCreateDate = oldRevision.getDateProperty("creation_date");
    	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date d = df.parse("2018-05-16");
        String opType = oldRevision.getType();
    	// ������ Ÿ���� ������ ������ �ƴϰų� �������� ������ �����̸� �������� ����
    	if( !opType.equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV) || !oldCreateDate.before(d)) {
    		return;
    	} 
    	
    	File oldWeldConditionTemplateFile = null;
    	File newWeldConditionTemplateFile = null;
    	File tempNewWeldConditionTemplateFile = null;
    	Vector<TCComponentDataset> oldReviseOpDatasets  = CustomUtil.getDatasets(oldRevision, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
		TCComponentTcFile[] tempFiles = ((TCComponentDataset) oldReviseOpDatasets.get(0)).getTcFiles();
        if(tempFiles != null && tempFiles.length > 0) {
        	oldWeldConditionTemplateFile = tempFiles[0].getFile(null);
        	tempFiles = null;
        }
        
        Vector<TCComponentDataset> newReviseOpDatasets  = CustomUtil.getDatasets(newRevision, SDVTypeConstant.WELD_CONDITION_SHEET_RELATION, TcDefinition.DATASET_TYPE_EXCELX);
        CustomUtil.removeAllNamedReference(newReviseOpDatasets.get(0));
        
        TCComponentItem weldConditionItem = CustomUtil.findItem("Document", "ME_DOCTEMP_11");
        TCComponentItemRevision weldConditionItemRevision = weldConditionItem.getLatestItemRevision();
        Vector<TCComponentDataset> newWeldConditionDatasets = CustomUtil.getDatasets(weldConditionItemRevision, "TC_Attaches", TcDefinition.DATASET_TYPE_EXCELX );
        tempFiles = ((TCComponentDataset) newWeldConditionDatasets.get(0)).getTcFiles();
        
        if(tempFiles != null && tempFiles.length > 0) {
        	tempNewWeldConditionTemplateFile = tempFiles[0].getFile(null);
        }
        
     // ������ �ʿ��� ���Ͽ� �̸��� �����Ѵ�
        String targetName = newRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String fileName = targetName + ExcelTemplateHelper.getToday("yyyy-MM-dd");
        
		System.out.println("fileName = "+fileName);
        
		newWeldConditionTemplateFile = CustomUtil.renameFile(tempNewWeldConditionTemplateFile, fileName);
        
        
        String weldConditionSheetType = ProcessSheetDataHelper.getWeldConditionSheetType(newRevision);
        
        
        if( null != oldWeldConditionTemplateFile  && null != newWeldConditionTemplateFile ) {
        	Workbook oldTemplateWorkbook = null;
        	
        	  try {
        		  oldTemplateWorkbook = new XSSFWorkbook(new FileInputStream(oldWeldConditionTemplateFile));
        	 
        		  
        		
        	PreviewWeldConditionSheetDataHelper weldConditionDataHelper = new PreviewWeldConditionSheetDataHelper(weldConditionSheetType);
        	
        	weldConditionDataHelper.setUserDefineTarget(reviseTarget);
        	
        	IDataSet newRevisionDataset = weldConditionDataHelper.getDataSet();
        	
        	PreviewWeldConditionSheetExcelTransformer transFormer = new PreviewWeldConditionSheetExcelTransformer();
        	
        	
        	
        	Workbook newTemplateWorkbook = PreviewWeldConditionSheetExcelTransformer.initWorkBook(newWeldConditionTemplateFile, weldConditionSheetType);
        	
        	transFormer.print(newWeldConditionTemplateFile, newTemplateWorkbook, newRevisionDataset, weldConditionSheetType);
        	
        	Workbook newTemplateWorkbook1 = new XSSFWorkbook(new FileInputStream(newWeldConditionTemplateFile));
        	
        	copyWeldConditionInfo(oldTemplateWorkbook, newTemplateWorkbook1, weldConditionSheetType);
        	
        		
        		FileOutputStream fos = new FileOutputStream(newWeldConditionTemplateFile);
    			newTemplateWorkbook1.write(fos);
    			fos.flush();
    			fos.close();
    		} catch (Exception e) {
    			MessageBox.post(AIFUtility.getActiveDesktop(), "���� ���� New Template ���� ������ ������ �߻� �߽��ϴ�. \n" , "ERROR", MessageBox.ERROR);
    			e.printStackTrace();
      		  return;
    		} 
        	 
//        	CustomUtil.removeAllNamedReference(newWeldConditionDatasets.get(0));
        	Vector<File> fileVector = new Vector<File>();
        	fileVector.add(newWeldConditionTemplateFile);
        	SYMTcUtil.importFiles(newReviseOpDatasets.get(0), fileVector);
        	
        }
    	
    }
    
    
    private void copyWeldConditionInfo(Workbook oldTemplateWorkbook, Workbook newTemplateWorkbook, String weldConditionSheetType)  {

      	 for( int i = 0; i < oldTemplateWorkbook.getNumberOfSheets(); i ++ ) {
      		 
      		Sheet newSheet = null;
      		String currentSheetName = oldTemplateWorkbook.getSheetName(i);
      		 if( currentSheetName.trim().equalsIgnoreCase("MECO_LIST") || currentSheetName.trim().equalsIgnoreCase("COPYSHEET") || currentSheetName.trim().equalsIgnoreCase("SYSTEMSHEET")){
      			 continue;
      		 }
      		 // ���� �߰� --> ������ UserSheet + ���� Or �� + ���� ���·� �Ǿ� �־� Sheet �̸��� ������ ����ġ �ʾ� ���� �߰ߵǴ� ���� Sheet �̸��� �߰� �ؾ���
      		 // ������  Preview ���� ������ Sheet �߰� ��ư�� ������ ������ �߰� �Ǵµ� �̶��� UserSheet + ���� ���·� �߰� �ǳ� 
      		 // ����ڰ� ������ �̸��� ���������� �ٲ�
      		 if( currentSheetName.startsWith("UserSheet")) {
      		
      			int copySheetIndex = newTemplateWorkbook.getSheetIndex("copySheet");
      			newSheet = newTemplateWorkbook.cloneSheet(copySheetIndex);
      			newTemplateWorkbook.setSheetOrder(newSheet.getSheetName(), newTemplateWorkbook.getNumberOfSheets() - 1);
      			newTemplateWorkbook.setSheetName(newTemplateWorkbook.getNumberOfSheets() - 1, currentSheetName);
      		
      		 } else if( Pattern.matches("^[0-9]*$", currentSheetName)) {
      			// ���� �߰� ������ ���  ���� �̸����� �Ǿ� ����
      			newSheet = newTemplateWorkbook.getSheet(currentSheetName);
      			
      		 }  else {
      			 // �߰��� ���� �߿� Sheet �̸��� UserSheet �� �ƴ� ���
      			String userSheet = "UserSheet";
      			int userSheetNumber = 0;
      			// �� ���ø� ���� ����(����Sheet) �� �ƴϰ� ���� �ý��� Sheet�� �ƴѰ͵��� 
      			for( int j = 0 ; j < oldTemplateWorkbook.getNumberOfSheets() ; j ++ ) {
      				if(  Pattern.matches("^[0-9]*$", currentSheetName) || currentSheetName.trim().equalsIgnoreCase("MECO_LIST") || currentSheetName.trim().equalsIgnoreCase("COPYSHEET") || currentSheetName.trim().equalsIgnoreCase("SYSTEMSHEET")){
      	      			 continue;
      	      		 } 
      				// ������ UserSheet
      				if(  oldTemplateWorkbook.getSheetName(i).startsWith(userSheet)) {
      					userSheetNumber ++;
      				}
      			}
      			
      			int copySheetIndex = newTemplateWorkbook.getSheetIndex("copySheet");
      			newSheet = newTemplateWorkbook.cloneSheet(copySheetIndex);
      			newTemplateWorkbook.setSheetOrder(newSheet.getSheetName(), newTemplateWorkbook.getNumberOfSheets() - 1);
      			newTemplateWorkbook.setSheetName(newTemplateWorkbook.getNumberOfSheets() - 1, userSheet + userSheetNumber + 1);
      		 }
      		 
      		 Sheet oldSheet = oldTemplateWorkbook.getSheetAt(i);
      		 copyImageData(oldSheet, newSheet, newTemplateWorkbook, weldConditionSheetType, newTemplateWorkbook.getSheetIndex(currentSheetName));
      		 // Schedule ���γ��� �κ� OLD Template ����
      		 
      		 
      		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
      		 
      		 CTSheetProtection sheetProtection = ((XSSFSheet)newSheet).getCTWorksheet().getSheetProtection();

             CTWorkbookProtection workbookProtection =  ((XSSFWorkbook)newSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
 
		     	STUnsignedShortHex convertedPassword = AISInstructionDatasetCopyUtil.stringToExcelPassword(newTemplateWorkbook, "symc");
			    if (sheetProtection == null) {
	                sheetProtection = ((XSSFSheet)newSheet).getCTWorksheet().addNewSheetProtection();
	            }
			    sheetProtection.xsetPassword(convertedPassword);
	            sheetProtection.setSheet(true);
	            sheetProtection.setScenarios(true);
	            sheetProtection.setObjects(false);
	            
	            if (workbookProtection == null) {
	                workbookProtection = ((XSSFWorkbook) newTemplateWorkbook).getCTWorkbook().addNewWorkbookProtection();
	                workbookProtection.setLockStructure(true);
	                workbookProtection.setLockWindows(true);
	            }
	            workbookProtection.xsetWorkbookPassword(convertedPassword);
      		 
      			 
      		 }
      		 
      	 }
    
    
    private void copyImageData( Sheet oldTemplateSheet, Sheet newTemplateSheet,  Workbook newTemplateWorkbook, String weldConditionSheetType, int sheetIndex ) {
    	  
    	  CTSheetProtection sheetProtection = ((XSSFSheet)newTemplateSheet).getCTWorksheet().getSheetProtection();
          if (sheetProtection != null) {
        	  ((XSSFSheet)newTemplateSheet).getCTWorksheet().unsetSheetProtection();
        	  if(newTemplateSheet.getSheetName().startsWith("UserSheet")) {
        		  newTemplateSheet.getWorkbook().setSheetHidden(sheetIndex, false);
        		  
        	  }
          }
      

          CTWorkbookProtection workbookProtection =  ((XSSFWorkbook)newTemplateSheet.getWorkbook()).getCTWorkbook().getWorkbookProtection();
	      if (workbookProtection != null) {
	    	  ((XSSFWorkbook)newTemplateSheet.getWorkbook()).getCTWorkbook().unsetWorkbookProtection();
	      }
	      
	      int rowStartOfSheetType = 0;
	      int rowEndOfSheetType = 0;
	      
	      int columnStartOfSheetType = 0;
	      int columnendOfSheetType = 0;
	      
	      if(weldConditionSheetType!=null && weldConditionSheetType.trim().equalsIgnoreCase("SPOT_TYPE")==true){
	    	  rowStartOfSheetType = 7;
	    	  rowEndOfSheetType = 36;
	    	  
	    	  columnStartOfSheetType = 7;
	    	  columnendOfSheetType = 41;
	      } else {
	    	  rowStartOfSheetType = 7;
	    	  rowEndOfSheetType = 36;
	      }
	      // ������ Cell ���� ����
// 	  if(!oldTemplateSheet.getSheetName().startsWith("UserSheet") && !oldTemplateSheet.getSheetName().startsWith("��")) {
 	  if(Pattern.matches("^[0-9]*$", oldTemplateSheet.getSheetName())) {
 		  
      	for( int i = rowStartOfSheetType; i <= rowEndOfSheetType; i++ ) {
      			Row oldRow = oldTemplateSheet.getRow(i);
      		Row newRow = newTemplateSheet.getRow(i);
      		if( null == newRow) {
      			newRow = newTemplateSheet.createRow(i);
      		}
      		newRow.setHeight(oldRow.getHeight());
      		for(int j = columnStartOfSheetType; j <= columnendOfSheetType; j++ ) {
      			if(weldConditionSheetType.trim().equalsIgnoreCase("SPOT_TYPE")) {
      				if( i <= 16) {
      					if( j > 31) {
      						break;
      					}
      				}
      			}
      			
      			
      			Cell newCell = newRow.getCell(j);
      			
      			if( null == newCell ) {
      				newCell = newRow.createCell(j);
      			}
      			cellCopy(newTemplateWorkbook, newCell, oldRow.getCell(j)) ;
      			newTemplateSheet.setColumnWidth(j, oldTemplateSheet.getColumnWidth(j));
      		}
      	}
 	
 	  }
      	
      XSSFDrawing drawingOld = (XSSFDrawing)oldTemplateSheet.createDrawingPatriarch(); 
      XSSFDrawing drawingNew = (XSSFDrawing)newTemplateSheet.createDrawingPatriarch(); 
      
     for (int i = 0; i < drawingOld.getShapes().size(); i++) {
     		XSSFShape shape = drawingOld.getShapes().get(i);
         if (shape instanceof XSSFPicture) {
//         	System.out.println("Picture Num : "  + i);
             XSSFPicture pic = (XSSFPicture) shape;
             XSSFPictureData picdata = pic.getPictureData();
             int pictureIndex = newTemplateSheet.getWorkbook().addPicture(picdata.getData(), picdata.getPictureType());
            
             
             	XSSFClientAnchor anchor  = (XSSFClientAnchor)pic.getAnchor();
                 CTMarker markerFrom = anchor.getFrom();
                 CTMarker markerTo = anchor.getTo();
                 
                 anchor.setDx1((int) markerFrom.getColOff());
                 anchor.setDx2((int) markerTo.getColOff());
                 anchor.setDy1((int) markerFrom.getRowOff());
                 anchor.setDy2((int) markerTo.getRowOff());
                 anchor.setCol1(markerFrom.getCol());
                 anchor.setCol2(markerTo.getCol());
                 anchor.setRow1(markerFrom.getRow());
                 anchor.setRow2(markerTo.getRow());
             
             XSSFPicture picture = drawingNew.createPicture(anchor, pictureIndex);
             XmlObject xmlObject = pic.getCTPicture().copy();
             picture.getCTPicture().set(xmlObject);
             
         }  
         else if( shape instanceof XSSFConnector) {
         	
         	CreationHelper helper = newTemplateWorkbook.getCreationHelper();
             XSSFDrawing drawing1 = (XSSFDrawing)newTemplateSheet.createDrawingPatriarch();
             XSSFClientAnchor anchor1 = new XSSFClientAnchor();
         	
         	XSSFConnector simpleShpe = (XSSFConnector) shape;
         	int connectorType = simpleShpe.getShapeType();
         	
         	XSSFClientAnchor  anchor =  (XSSFClientAnchor)simpleShpe.getAnchor();
         	CTMarker ctMarker =  anchor.getTo();
         	System.out.println(ctMarker.toString());
         	anchor1.setDx1(anchor.getDx1());
             anchor1.setDy1(anchor.getDy1());
     		anchor1.setCol1(anchor.getCol1());
         	anchor1.setRow1(anchor.getRow1());
             
             
             if(null != ctMarker.toString() && !"<xml-fragment/>".equals(ctMarker.toString())) {
             	anchor1.setDx2(anchor.getDx2());
               anchor1.setDy2(anchor.getDy2());
             	anchor1.setCol2(anchor.getCol2());
             	anchor1.setRow2(anchor.getRow2());
             } 
             
             XSSFConnector connector = drawing1.createConnector(anchor1);
             connector.setShapeType(connectorType);
             connector.getCTConnector().setSpPr(simpleShpe.getCTConnector().getSpPr());
             connector.getCTConnector().setStyle(simpleShpe.getCTConnector().getStyle());
             connector.getCTConnector().setNvCxnSpPr(simpleShpe.getCTConnector().getNvCxnSpPr());
             connector.getCTConnector().setMacro(simpleShpe.getCTConnector().getMacro());
             System.out.println("Connector :" + connector.getShapeType());
         	
         } 
         
             else if( shape instanceof XSSFSimpleShape) {
             	
         	XSSFSimpleShape simpleShape = (XSSFSimpleShape) shape;
             XSSFClientAnchor  anchor =  (XSSFClientAnchor)simpleShape.getAnchor();

             XSSFDrawing drawing1 = (XSSFDrawing)newTemplateSheet.createDrawingPatriarch();
             XSSFClientAnchor anchor1 = new XSSFClientAnchor();
             int connectorType = simpleShape.getShapeType();
           

         	CTMarker ctMarkerTo =  anchor.getTo();
         	CTMarker ctMarkerFrom =  anchor.getFrom();
         	
             if(null != ctMarkerTo.toString() && "<xml-fragment/>".equals(ctMarkerTo.toString())) {
             	
             	anchor1.setDx1(anchor.getDx1());
             	anchor1.setDy1(anchor.getDy1());
             	
             	anchor1.setCol1(anchor.getCol1());
                 anchor1.setRow1(anchor.getRow1());
             	
             	anchor1.setDx2(anchor.getDx1());
                 anchor1.setDy2(anchor.getDy1());
             
             	anchor1.setCol2(anchor.getCol1() + 2);
             	anchor1.setRow2(anchor.getRow1() + 2);
             	
             } else {

             	XmlObject objectTo = ctMarkerTo.copy();
             	XmlObject objectFrom = ctMarkerFrom.copy();
             	anchor1.getFrom().set(objectFrom);
             	anchor1.getTo().set(objectTo);
             }
             XSSFSimpleShape simple =  drawing1.createSimpleShape(anchor1);
             simple.setShapeType(connectorType);
             XmlObject xmlObject = simpleShape.getCTShape().copy();
             simple.getCTShape().set(xmlObject);
             
//             System.out.println("SimpleShape :" + simple.getCTShape().getTxBody());
             }
         
             else if( shape instanceof XSSFShapeGroup) {
             	
             	XSSFShapeGroup simpleShape = (XSSFShapeGroup) shape;
                 XSSFClientAnchor  anchor =  (XSSFClientAnchor)simpleShape.getAnchor();

                 XSSFDrawing drawing1 = (XSSFDrawing)newTemplateSheet.createDrawingPatriarch();
                 XSSFClientAnchor anchor1 = new XSSFClientAnchor();
                 
                 ///////////////////////////////////////////////////////////////////////////////////////////////
                
                 CTPicture[] groupPic  =  simpleShape.getCTGroupShape().getPicArray();
            	 
        		 for( int m = 0; m < groupPic.length; m ++ ) {
        			 POIXMLDocumentPart  documentPart = simpleShape.getDrawing().getRelationById(groupPic[m].getBlipFill().getBlip().getEmbed());
        			 if( documentPart instanceof XSSFPictureData) {
        				 XSSFPictureData picData = (XSSFPictureData)documentPart;
        				 int pictureIndex = newTemplateSheet.getWorkbook().addPicture(picData.getData(), picData.getPictureType());
        				 XSSFPicture picture = drawingNew.createPicture(new XSSFClientAnchor(), pictureIndex);
        				 
        				 picture.getCTPicture().set(groupPic[m].copy());
        			 }
        		 }
                  
                 ///////////////////////////////////////////////////////////////////////////////////////////////
                  
             	CTMarker ctMarkerTo =  anchor.getTo();
             	CTMarker ctMarkerFrom =  anchor.getFrom();
             	
                 if(null != ctMarkerTo.toString() && "<xml-fragment/>".equals(ctMarkerTo.toString())) {
                 	
                 	anchor1.setDx1(anchor.getDx1());
                 	anchor1.setDy1(anchor.getDy1());
                 	
                 	anchor1.setCol1(anchor.getCol1());
                     anchor1.setRow1(anchor.getRow1());
                 	
                 	anchor1.setDx2(anchor.getDx1());
                     anchor1.setDy2(anchor.getDy1());
                 
                 	anchor1.setCol2(anchor.getCol1() + 2);
                 	anchor1.setRow2(anchor.getRow1() + 2);
                 	
                 } else {

                 	XmlObject objectTo = ctMarkerTo.copy();
                 	XmlObject objectFrom = ctMarkerFrom.copy();
                 	anchor1.getFrom().set(objectFrom);
                 	anchor1.getTo().set(objectTo);
                 }
                 XSSFShapeGroup simple =  drawing1.createGroup(anchor1);
 	   			 XmlObject xmlObject = simpleShape.getCTGroupShape().copy();
 	             simple.getCTGroupShape().set(xmlObject);
//                 System.out.println("SimpleShape :" + simple.getCTGroupShape());
                 }
         }  // for�� ��
		     try {
			    	
			    	PackageRelationshipCollection oldRelations = drawingOld.getPackagePart().getRelationships();
			    	PackageRelationshipCollection  newrelations = drawingNew.getPackagePart().getRelationships();
			    	Iterator<PackageRelationship> newIterator = newrelations.iterator();
			    	
			    	while(newIterator.hasNext()) {
			    		PackageRelationship relation = newIterator.next();
			    		newIterator.remove();
			    		
			    	}
			    	
			    	for (PackageRelationship oldRelation : oldRelations ) {
			    		newrelations.addRelationship(oldRelation);
			    	}
			    	
			    } catch (Exception e) {
			    	e.printStackTrace();
			    }

      }
    
    
	 public  void cellCopy(   Workbook sourceWB, Cell targetCell, Cell sourceCell) {
//		 CellStyle newStyle = sourceWB.createCellStyle();
		 XSSFCellStyle sourceCellStyle = (XSSFCellStyle)sourceCell.getCellStyle();
		 Font sourceCellFontIndex = sourceCellStyle.getFont();
		 CellStyle targetCellStyle = null;
		 

		  switch( sourceCell.getCellType()) {
		 
			case XSSFCell.CELL_TYPE_NUMERIC :
				 double numValue = sourceCell.getNumericCellValue();
//				 newStyle.cloneStyleFrom(sourceCell.getCellStyle());
					 targetCell.setCellValue(getFormatedString(numValue));
					 targetCellStyle = targetCell.getCellStyle();
					 targetCellStyle.setFont(sourceCellFontIndex);
					 targetCell.setCellStyle( targetCellStyle );
					 
					
					 
				  break;
			case XSSFCell.CELL_TYPE_STRING :
				 String cellValue = sourceCell.getStringCellValue();
//				 newStyle.cloneStyleFrom(sourceCell.getCellStyle());
					 targetCell.setCellValue(cellValue);
					 targetCellStyle = targetCell.getCellStyle();
					 targetCellStyle.setFont(sourceCellFontIndex);
					 targetCell.setCellStyle( targetCellStyle );

				  break;
		  }
				
		 }
	 
	 
	 public static String getFormatedString(double value) {
	        DecimalFormat df = new DecimalFormat("#####################.####");//
	        return df.format(value);
	    }
          
    
   
    
   
}
