package com.kgm.common.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Combo;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.rac.kernel.SYMCECODwgData;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.delete.DeleteOperation;
import com.teamcenter.rac.commands.namedreferences.ImportFilesOperation;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.SOAGenericCreateHelper;
import com.teamcenter.rac.common.viewedit.ViewEditHelper;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupMemberType;
import com.teamcenter.rac.kernel.TCComponentGroupType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentProject;
import com.teamcenter.rac.kernel.TCComponentProjectType;
import com.teamcenter.rac.kernel.TCComponentPseudoFolder;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentRoleType;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCReservationService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;

/**
 * [SR140723-028][20140522] swyoon createApplicationObject 메서드에서 동일 API를 2번 호출하던 Bug Fix.
 */
public class SYMTcUtil {
    // 표준 결재선 결재기능 코드
    final static String APPLINE_STCPartRevision_STD = "STD"; // 규격화 일괄승인
    final static String APPLINE_STCPartRevision_DWG = "DWG"; // 부품/어셈블리(도면 승인)
    final static String APPLINE_STCPPR_NEW_OVER = "NEW_OVER"; // 구매의뢰(3억이상)
    final static String APPLINE_STCPPR_NEW = "NEW"; // 구매의뢰(3억미만)
    final static String APPLINE_STCPPR_DEL_OVER = "DEL_OVER"; // 삭제승인(3억이상)
    final static String APPLINE_STCPPR_DEL = "DEL"; // 삭제승인(3억미만)
    final static String APPLINE_STCSWRevision_STD = "STD"; // SW품목승인
    final static String APPLINE_STCSWRevision_NSW = "NSW"; // SW품목승인(NSW)
    final static String APPLINE_STCSWRevision_BL = "BL"; // SW BL

    /**
     * 폴더를 생성하는 메소드
     * 
     * @param session
     * @param type
     * @param strName
     * @return
     * @throws TCException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static TCComponentFolder createFolder(TCSession session, String type, String strName) throws TCException {
        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(session, type);
        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add("object_name", strName);
        ArrayList list = new ArrayList();
        list.add(createInput);
        List localList = SOAGenericCreateHelper.create(session, createDefinition, list);
        if ((localList != null) && (localList.size() > 0)) {
            return (TCComponentFolder) localList.get(0);
        }
        return null;
    }

    /**
     * STCFolder 생성하는 메소드
     * 
     * @param session
     * @param string
     * @return TCComponentFolder
     * @throws TCException
     */
    public static TCComponentFolder createSTCFolder(TCSession session, String strName) throws TCException {
        return createFolder(session, "STCFolder", strName);
    }

    /**
     * Item 을 생성하는 Method
     * 
     * @param session
     *            - IMANSession
     * @param itemId
     *            - Item ID
     * @param itemName
     *            - Item NAME
     * @param desc
     *            - Item Description
     * @param itemType
     *            - Item Type
     * @param revNo
     *            - Revision Number
     * @return - 생성된 Item
     * @throws Exception
     */
    public static TCComponentItem createItem(TCSession session, String itemId, String itemName, String desc, String itemType, String revNo) throws Exception {
        TCComponentItem item = null;
        try {
            /*
             * LOVUIComponent uomLovComboBox = new LOVUIComponent(session,
             * "Unit of Measures"); uomLovComboBox.setSelectedValue("Piece");
             * Object uomObj = uomLovComboBox.getSelectedObject();
             */
            TCComponentItemType cItemType = (TCComponentItemType) session.getTypeComponent("Item");
            // item = cItemType.create(itemId, revNo, itemType, itemName, desc,
            // (TCComponent)uomObj);
            item = cItemType.create(itemId, revNo, itemType, itemName, desc, null);

        } catch (Exception e) {
            throw e;
        }
        return item;
    }

    /**
     * 입력받은 Revision String 으로 Revision 생성
     * 
     * 
     * @param latestRevision
     *            : Item 에 존재하는 최신 ItemRevision
     * @param newRev
     *            : 생성될 Revision String
     * @return TCComponentItemRevision : 생성된 Item Revision
     * @throws Exception
     */
    public static TCComponentItemRevision createItemRevision(TCComponentItemRevision itemRevision, String newRev) throws Exception {
        TCComponentItemRevision newRevision = itemRevision.saveAs(newRev, itemRevision.getProperty("object_name"), itemRevision.getProperty("object_desc"), true, null);
        return newRevision;
    }

    /**
     * @param session
     * @param objectName
     * @param string
     * @return TCComponentForm
     * @author Jeahun Lee
     * @throws TCException
     */
    public static TCComponentForm createForm(TCSession session, String objectName, String strType) throws TCException {
        TCComponentFormType formType = (TCComponentFormType) session.getTypeComponent(strType);
        TCComponentForm form = formType.create(objectName, "", strType);
        return form;
    }

    public static TCComponentForm createWebLink(TCSession session, String objectName, String url, String description) throws TCException {
        TCComponentFormType formType = (TCComponentFormType) session.getTypeComponent("Web Link");
        TCComponentForm form = formType.create(objectName, description, "Web Link");
        form.setProperty("url", url);
        return form;
    }

    /**
     * 1.DataSet 생성
     * 
     * @param itemRev
     *            : DataSet이 Add 될 ItemRevision Instance
     * @param datasetType
     *            : DataSet Type
     * @param itemid
     *            : Item ID
     * @param revision
     *            : Item Revision
     * @throws Exception
     */
    public static TCComponentDataset createDataSet(TCSession session, TCComponentItemRevision itemRev, String datasetType, String dataSetName) throws Exception {
        TCComponentDataset createDataset = createPasteDataset(session, itemRev, dataSetName, datasetType, TcDefinition.TC_SPECIFICATION_RELATION);

        return createDataset;
    }

    /**
     * 1.DataSet 생성 2.DataSet에 파일첨부
     * 
     * @param itemRev
     *            : DataSet이 Add 될 ItemRevision Instance
     * @param datasetType
     *            : DataSet Type
     * @param itemid
     *            : Item ID
     * @param revision
     *            : Item Revision
     * @param filepath
     *            : File FullPath
     * @throws Exception
     */
    public static TCComponentDataset createDataSet(TCSession session, TCComponent itemRev, String datasetType, String dataSetName, Vector<File> importFiles) throws Exception {
        TCComponentDataset createDataset = null;

        if( itemRev instanceof TCComponentItem  )
        {
        	createDataset = createPasteDataset(session, itemRev, dataSetName, datasetType, TcDefinition.TC_REFERENCE_RELATION);
	        if (createDataset != null && importFiles != null && importFiles.size() > 0) {
	            importFiles(createDataset, importFiles);
	        }
        }
        else
        {
	        if( "DirectModel".equals(datasetType) )
	        {
	            createDataset = createPasteDataset(session, itemRev, dataSetName, datasetType, "IMAN_Rendering");
	        }
	        else
	        {
	            createDataset = createPasteDataset(session, itemRev, dataSetName, datasetType, TcDefinition.TC_SPECIFICATION_RELATION);
	        }
	        if (createDataset != null && importFiles != null && importFiles.size() > 0) {
	            importFiles(createDataset, importFiles);
	        }
        }
        return createDataset;
    }    
    
    /**
     * Dataset & File 생성
     * 
     * @method makeDataSet 
     * @date 2013. 4. 24.
     * @param
     * @return TCComponentDataset
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public static TCComponentDataset makeDataSet(TCSession session, TCComponent itemRev, String datasetType, String dataSetName, Vector<File> importFiles) throws Exception {
        TCComponentDataset createDataset = null;
        // dataset: catia / 확장자: .model - cataia/model 데이터셋 체크
        if(!"catia".equals(datasetType)) {
            createDataset = createDataSet(session, itemRev, datasetType, dataSetName, importFiles);
        } else {
            // model dataset일 경우 dataset을 검색하여 존재하면 dataset을 생성하지않고 검색한 dataset에 add한다.
            Vector<TCComponentDataset> datasets = (Vector<TCComponentDataset>) CustomUtil.getDatasets(itemRev, TcDefinition.TC_SPECIFICATION_RELATION, "All");            
            for (int i = 0; datasets != null && i < datasets.size(); i++) {
                TCComponentDataset findDataSet = datasets.get(i);
                //  cati/model Dataset만 가져온다.
                if ("catia".equals(findDataSet.getType())) {
                    createDataset = findDataSet;
                    importFiles(createDataset, importFiles);                    
                    break;
                }
            }
            // 존재하지않으면 최초생성
            if(createDataset == null) {
                createDataset = createDataSet(session, itemRev, datasetType, dataSetName, importFiles);
            }           
        }
        return createDataset;
    }
    
    /**
     * DataSet 생성 후 Target TCComponet에 Paste
     * 
     * @param session
     *            - TCSession
     * @param targetComp
     *            - DataSet을 붙일 Item
     * @param datasetName
     *            - DataSet Name
     * @param datasetType
     *            - DataSet Type
     * @param relation
     *            - Relation Type
     * @return - 생성된 DataSet
     * @throws Exception
     */
    public static TCComponentDataset createPasteDataset(TCSession session, TCComponent targetComp, String datasetName, String datasetType, String relation) throws Exception {
        if (datasetType == null || datasetType.trim().equals("")) {
            return null;
        }

        TCComponentDataset newDataset = null;

        try {
            TCComponentDatasetType imancomponentdatasettype = (TCComponentDatasetType) session.getTypeComponent(datasetType);
            newDataset = imancomponentdatasettype.create(datasetName, "", datasetType);

            if (targetComp != null)
                targetComp.add(relation, (TCComponent) newDataset);
        } catch (Exception imanexception) {
            imanexception.printStackTrace();
            throw imanexception;
        }
        return newDataset;
    }



    /**
     * 해당 DataSet에 파일 첨부
     * 
     * ** 주의 **
     * 현재 1건 파일 업로드 시에는 Named Reference에 추가로 등록 되나
     * 여러건 업로드 시에는 기존 파일은 없어지고 새로운 파일들이 업로드 된다 (이전 업로드 삭제)
     * 
     * @param newDataset
     *            : 대상 Dataset
     * @param importFiles
     *            : 첨부할 File Vector
     * @throws Exception
     */
    public static void importFiles(TCComponentDataset newDataset, Vector<File> importFiles) throws Exception {
        try {
        	
        	TCComponent[] namedRefs = newDataset.getNamedReferences();
        	for( int i = 0 ; i < namedRefs.length ; i++)
        	{
        		String strOrFileName = namedRefs[i].getProperty("original_file_name");
        		
        		for( int j = 0 ; j < importFiles.size() ; j++)
        		{
        			String strUpFileName = importFiles.get(j).getName();
        			
        			if( strOrFileName.equals(strUpFileName) )
        			{
        				return;
        			}
        		}
        	}
        	
        	
            String[] as = new String[importFiles.size()];
            String[] as1 = new String[importFiles.size()];

            for (int i = 0; i < importFiles.size(); i++) {
                String filePath = ((File) importFiles.elementAt(i)).getPath();
                String fileName = ((File) importFiles.elementAt(i)).getName();
                int p = fileName.lastIndexOf(".");
                String fileExtendsName = fileName.substring(p + 1);

                String namedRefName = getNamedRefType(newDataset, fileExtendsName.toLowerCase());

                // importFileName
                as[i] = filePath;
                // importRefType
                as1[i] = namedRefName;
            }
            
            if(importFiles != null && importFiles.size() == 1) {
                File importFile = importFiles.get(0);                
                String s = newDataset.getType();
                String s1 = as1[0];
                ImportFilesOperation importfilesoperation = new ImportFilesOperation(newDataset, importFile, s, null, s1, Utilities.getCurrentFrame());
                importfilesoperation.executeOperation();
            } else {
                Registry registry = Registry.getRegistry("com.teamcenter.rac.kernel.kernel");
                int l = registry.getInt("TCFile_transfer_buf_size", 0);
                if (l == 0) {
                    l = 512;
                }
                // newDataset.setFiles(as, as2, as3, as1, l); //131072);
                newDataset.setFiles(as, as1); // 131072);
            }
            
        } catch (Exception imanexception1) {
            throw imanexception1;
        }
    }

    public static void deleteReleaseStatus(TCComponent ic) throws Exception {
        deleteReleaseStatus(new TCComponent[] { ic });
    }

    public static void deleteReleaseStatus(TCComponent ic[]) throws Exception {
        TCSession session = ic[0].getSession();
        Markpoint mp = new Markpoint(session);
        try {
            for (int i = 0; i < ic.length; i++) {
                deleteReleaseStatus(ic[i], "Released");
            }
        } catch (Exception e) {
            mp.rollBack();
            throw new Exception(e);
        }
        mp.forget();
    }

    public static void deleteReleaseStatus(TCComponent ic, String name) throws Exception {
        TCComponent comp[] = ic.getRelatedComponents("release_status_list");
        ic.lock();
        for (int i = 0; i < comp.length; i++) {
            if (comp[i] instanceof TCComponentReleaseStatus && comp[i].getProperty("object_name").equals(name)) {
                ic.remove("release_status_list", comp[i]);
                if (comp[i].whereReferenced().length != 1) {
                    comp[i].delete();
                }

            }
        }
        ic.unlock();
    }

    /**
     * 리비젼에 붙어있는 Component를 삭제한다. Copyright : (주) ENES Solutions
     * 
     * @author : mskim
     * @since : 2009. 10. 29.
     * @param revision
     * @param conponentType
     * @param relationType
     * @throws Exception
     */
    public static void deleteComponent(TCComponentItemRevision revision, String conponentType, String relationType) throws Exception {
        AIFComponentContext[] relatedComp = revision.getRelated(relationType);

        if (revision != null) {
            for (int i = 0; i < relatedComp.length; i++) {
                if (relatedComp[i].getComponent().getType().equals(conponentType)) {
                    DeleteOperation del = new DeleteOperation(relatedComp[i]);
                    del.executeOperation();
                }
            }
        }
    }

    /**
     * 프로젝트에 컴포넌트들을 할당한다.
     * 
     * @param session
     * @param project
     * @param comps
     * @throws TCException
     */
    public static void assignToProject(TCSession session, TCComponentProject project, TCComponent[] comps) throws TCException {
        assignToProject(session, project.getProperty("project_id"), comps);
    }

    /**
     * 프로젝트에 컴포넌트들을 할당한다.
     * 
     * @param session
     * @param projectId
     * @param comps
     * @throws TCException
     */
    public static void assignToProject(TCSession session, String projectId, TCComponent[] comps) throws TCException {
        TCComponentProjectType type = (TCComponentProjectType) session.getTypeComponent("TC_Project");
        TCComponentProject project = type.find(projectId);
        if (project == null) {
            throw new TCException("[" + projectId + "]는 존재하지 않는 프로젝트 코드 입니다.");
        }
        type.assignToProject(project, comps);
    }

    /**
     * itemRevision의 topBomLine을 가져오기. Copyright : (주) ENES Solutions
     * 
     * @author : jhkim
     * @since : 2009. 10. 14.
     * @param revision
     * @return
     */
    public static TCComponentBOMLine getTopBOM(TCComponentItemRevision revision) throws TCException {
        TCSession session = revision.getSession();
        TCComponentBOMWindowType type = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        TCComponentRevisionRuleType type2 = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
        TCComponentRevisionRule rule = type2.getDefaultRule();
        TCComponentBOMWindow bomWindow = type.create(rule);
        bomWindow.setWindowTopLine(null, revision, null, null);
        TCComponentBOMLine topLine = bomWindow.getTopBOMLine();
        return topLine;
    }

    /**
     * itemRevision의 1단계 아래의 child 가져오기. Copyright : (주) ENES Solutions
     * 
     * @author : jhkim
     * @since : 2009. 10. 14.
     * @param revision
     * @return
     */
    public static TCComponentBOMLine[] getChildrenBOM(TCComponentItemRevision revision) throws TCException {
        TCSession session = revision.getSession();
        TCComponentBOMWindowType type = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        TCComponentRevisionRuleType type2 = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
        TCComponentRevisionRule rule = type2.getDefaultRule();
        TCComponentBOMWindow bomWindow = type.create(rule);
        bomWindow.setWindowTopLine(null, revision, null, null);
        TCComponentBOMLine topLine = bomWindow.getTopBOMLine();
        AIFComponentContext contexts[] = topLine.getChildren();
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            childLines[i] = (TCComponentBOMLine) contexts[i].getComponent();
        }
        return childLines;
    }

    /**
     * dataset에 대하여 file의 확장자에 해당하는 named reference type을 찾는 method이다.
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @param extendsName
     *            String 파일 확장자 예) txt
     * @return String
     * @throws Exception
     */
    public static String getNamedRefType(TCComponentDataset datasetComponent, String extendsName) throws Exception {
        String s = "";
        NamedReferenceContext[] namedRefContext = null;
        namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
        for (int i = 0; i < namedRefContext.length; i++) {
            String s1 = namedRefContext[i].getNamedReference();
            String s2 = namedRefContext[i].getFileTemplate();
            if (s2.equalsIgnoreCase("*") || s2.equalsIgnoreCase("*.*")) {
                s = s1;
                break;
            } else if (s2.equalsIgnoreCase("*." + extendsName)) {
                s = s1;
                break;
            }
        }
        return s;
    }

    /**
     * DataSet으로 등록된 Excel Template File을 Local 경로로 저장 후 Return
     * 
     * @param session
     * @param strKey
     *            : Dataset Name
     * @return
     * @throws TCException
     */
    public static File getTemplateFile(TCSession session, String strKey, String strDownloadPath) throws Exception {
        File file = null;

        TCComponentDatasetType datsetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
        TCComponentDataset dataSet = datsetType.find(strKey);

        if (dataSet != null) {
            TCComponentTcFile[] imanFile = dataSet.getTcFiles();

            if (imanFile.length > 0) {
                File[] files = null;
                if (strDownloadPath == null)
                    files = dataSet.getFiles(SYMTcUtil.getNamedRefType(dataSet, imanFile[0]));
                else
                    files = dataSet.getFiles(SYMTcUtil.getNamedRefType(dataSet, imanFile[0]), strDownloadPath);

                if (files != null) {
                    file = files[0];
                }
            }
        }

        return file;
    }

    /**
     * DataSet이 가지고 있는 File의 Type을 가지고 온다.
     * 
     * @param dataset
     *            IMANComponentDataset
     * @param component
     *            IMANComponent
     * @return
     * @throws IMANException
     */
    public static String getNamedRefType(TCComponentDataset dataset, TCComponent component) throws Exception {
        String string = "";
        TCProperty ref_list = dataset.getTCProperty("ref_list");
        TCProperty ref_names = dataset.getTCProperty("ref_names");
        if (ref_list == null || ref_names == null) {
            return string;
        }
        TCComponent[] components = ref_list.getReferenceValueArray();
        String as[] = ref_names.getStringValueArray();

        if (components == null || as == null) {
            return string;
        }

        int i = components.length;

        if (i != as.length) {
            return string;
        }

        int j = -1;

        for (int k = 0; k < i; k++) {
            if (component != components[k]) {
                continue;
            }
            j = k;
            break;
        }
        if (j != -1) {
            string = as[j];
        }
        return string;
    }

    /**
     * dataset의 실제 파일 Name Copyright : (주) ENES Solutions
     * 
     * @author : mskim
     * @since : 2010. 1. 14.
     * @param dataset
     * @return
     */
    public static String getOriginalFileName(TCComponentDataset dataset) {

        String org_Name = "";
        try {
            TCComponentTcFile file[] = dataset.getTcFiles();
            if (file != null && file.length != 0) {
                org_Name = file[0].getProperty("original_file_name");
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        return org_Name;
    }

    /**
     * LOV이름으로 LOV 값 가져오기(배열로)
     * 
     * @param lovName
     *            가져올 LOV 이름
     * @param session
     * @return
     * @throws TCException
     * @author s.j park
     */
    public static final String[] getLovValues(String lovName, TCSession session) throws TCException {
        TCComponentListOfValuesType lovType = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] productGroupListofValues = lovType.find(lovName);
        TCComponentListOfValues productGroupListOfValue = productGroupListofValues[0];
        return productGroupListOfValue.getListOfValues().getLOVDisplayValues();

    }

    /**
     * TCComponentQuery에서 query name에 언어 적용한 name을 사용하기 위해 변환된 언어를 리턴하는 함수
     * 
     * @param session
     * @param propertyName
     *            조회할 Component의 Property
     * @return
     */
    public static String getTextServerString(TCSession session, String propertyName) {
        return getTextServerString(session, propertyName, "");
    }

    /**
     * TCComponentQuery에서 query name에 언어 적용한 name을 사용하기 위해 변환된 언어를 리턴하는 함수
     * 
     * @param session
     * @param propertyName
     *            조회할 Component의 Property
     * @param defaultName
     *            조회하지 못했을 경우의 기본 값
     * @return
     */
    public static String getTextServerString(TCSession session, String propertyName, String defaultName) {
        TCTextService textService = null;

        try {
            if (textService == null)
                textService = session.getTextService();
        } catch (Exception exception) {
            textService = null;
        }

        String s2 = null;
        if (textService == null)
            return defaultName;
        try {
            s2 = textService.getTextValue(propertyName);
        } catch (Exception exception1) {
            return defaultName;
        }

        if (s2 == null || s2.length() < 1)
            return defaultName;
        else
            return s2;
    }

    /**
     * 사용자가 할당된 Project배열을 리턴한다
     * 
     * @param string
     * @param session
     * @throws TCException
     * @author Jeahun Lee
     */
    public static TCComponent[] getAssignProjectUser(TCSession session) throws TCException {
        return getAssignProjectUser(session, session.getUserName());
    }

    /**
     * 사용자가 할당된 Project배열을 리턴한다
     * 
     * @param string
     * @param user
     * @throws TCException
     * @author Jeahun Lee
     * @param session
     */
    public static TCComponent[] getAssignProjectUser(TCSession session, String user) throws TCException {
        String props[] = { "ID" };
        String values[] = { user };
        return findComponent(session, "UserBasedProjects", props, values);
    }

    /**
     * 프로젝트의 Role에 할당된 사용자 정보 가져오는 메소드
     * 
     * @param p_pjtID
     *            프로젝트 ID
     * @param p_roleName
     *            역할이름
     * @param session
     *            세션
     * @return
     * @throws TCException
     * @author s.j park
     */
    public static TCComponent[] getRoleUserByPjtID(String p_pjtID, String p_roleName, TCSession session) throws TCException {
        String props[] = { "STCprojectID", "STCprojectRole" };
        String values[] = { p_pjtID, p_roleName };
        return findComponent(session, "STCProjectRole", props, values);
    }

    /**
     * 선택한 품목과 생성자간 프로젝트 공유하는 Project ID 와 명 목록 가져오기.
     * 
     * @param tccomponent
     *            선택 품목
     * @param userId
     *            생성자
     * @param session
     * @return
     * @throws TCException
     * @author j.i sung
     */
    public static final Vector<String[]> getProjectIDAndNames(TCComponent tccomponent, String userId, TCSession session) throws TCException {
        String target_pjt_ids = tccomponent.getProperty("project_ids");
        ArrayList<String[]> arg_pjt_info = SYMTcUtil.getProjectInfoByUserID(session, userId);
        String[] user_pjt_ids = new String[arg_pjt_info.size()];
        Vector<String[]> vt_common_pjt = new Vector<String[]>();

        for (int i = 0; i < arg_pjt_info.size(); i++) {
            String[] result = arg_pjt_info.get(i);
            user_pjt_ids[i] = result[0];

            if (target_pjt_ids.indexOf(user_pjt_ids[i]) != -1) {
                vt_common_pjt.add(result);
            }
        }

        return vt_common_pjt;
    }

    /**
     * 선택한 품목과 생성자간 프로젝트 공유하는 Project ID 목록 가져오기.
     * 
     * @param rev_target
     *            선택품목
     * @param userId
     *            생성자
     * @param session
     * @return
     * @throws TCException
     * @author s.j park
     */
    public static final Vector<String> getProjectID(TCComponentItemRevision rev_target, String userId, TCSession session) throws TCException {
        String target_pjt_ids = rev_target.getProperty("project_ids");
        ArrayList<String[]> arg_pjt_info = SYMTcUtil.getProjectInfoByUserID(session, userId);
        String[] user_pjt_ids = new String[arg_pjt_info.size()];
        Vector<String> vt_common_pjt = new Vector<String>();
        for (int i = 0; i < arg_pjt_info.size(); i++) {
            String[] result = arg_pjt_info.get(i);
            user_pjt_ids[i] = result[0];
            if (target_pjt_ids.indexOf(user_pjt_ids[i]) != -1) {
                vt_common_pjt.add(user_pjt_ids[i]);
            }
        }
        return vt_common_pjt;
    }

    /**
     * 선택한 Component가 해당 프로젝트에 Assign 되어 있는지 체크하는 함수
     * 
     * @param comp
     *            선택 콤포넌트
     * @param project_id
     *            프로젝트 아이디
     * @return
     * @throws Exception
     * @author jinil sung
     */
    @SuppressWarnings("unused")
    public static boolean isTcComponentContainedProject(TCComponent comp, String project_id) throws Exception {
        String target_pjt_ids = comp.getProperty("project_ids");
        TCComponent[] proj = comp.getReferenceListProperty("project_list");

        if (target_pjt_ids.indexOf(project_id) != -1)
            return true;

        return false;
    }

    /**
     * 프로젝트코드에의한 아이템 리비젼인지 체크
     * 
     * @param rev_target
     * @param pjtID
     * @param session
     * @return
     * @throws TCException
     */
    public static final boolean isItemRevisionFilterByPjtID(TCComponentItemRevision rev_target, String pjtID, TCSession session) {
        String target_pjt_ids;
        try {
            target_pjt_ids = rev_target.getProperty("project_ids");
            if (target_pjt_ids.indexOf(pjtID) != -1) {
                return false;
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * 프로젝트 이름 가져오기
     * 
     * @param project_id
     * @return
     * @author s.j park
     */
    public static final String getProjectName(ArrayList<String[]> arg_pjt_info, String project_id) {
        String pjt_name = null;
        for (int i = 0; i < arg_pjt_info.size(); i++) {
            String[] result = arg_pjt_info.get(i);
            if (project_id.equals(result[0])) {
                pjt_name = result[1];
            }
        }
        return pjt_name;
    }

    /**
     * UserID로 Project Info(프로젝트ID, 프로젝트이름)가져오는 메소드
     * 
     * @param session
     * @param userId
     *            생성자
     * @return
     * @author s.j park
     */
    public static final ArrayList<String[]> getProjectInfoByUserID(TCSession session, String userId) {
        ArrayList<String[]> result = new ArrayList<String[]>();
        String[] entries = { "ID" };
        String[] values = { userId };
        try {
            TCComponentQueryType iCompQuerytType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
            TCComponentQuery iCompQuery = (TCComponentQuery) iCompQuerytType.find("UserBasedProjects");
            TCComponent[] aTCComp = iCompQuery.execute(entries, values);

            for (int i = 0; i < aTCComp.length; i++) {
                String[] row = new String[2];
                row[0] = aTCComp[i].getProperty("project_id");
                row[1] = aTCComp[i].getProperty("project_name");
                result.add(row);

            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 현 리비젼의 전 리지전을 반환 (전 리비젼이 없으면 Null반환) Copyright : (주) ENES Solutions
     * 
     * @author : mskim
     * @since : 2010. 1. 28.
     * @param itemRevision
     * @return
     * @throws Exception
     */
    public static TCComponentItemRevision getPreviousRevision(TCComponentItemRevision itemRevision) throws Exception {
        TCComponentItemRevision previousRevision = null;
        TCComponent[] revisionList = itemRevision.getItem().getRelatedComponents("revision_list");
        if (revisionList.length > 1) {
            for (int inx = 0; inx < revisionList.length; inx++) {
                TCComponentItemRevision revision = (TCComponentItemRevision) revisionList[inx];
                if (inx == 0 && revision == itemRevision) {
                    break;
                }
                if (revision == itemRevision) {
                    previousRevision = (TCComponentItemRevision) revisionList[inx - 1];
                }
            }
        }
        return previousRevision;
    }
    

    /**
     * Item 하위 Latest Released Revision을 Return
     * Release된 Revision이 존재하지 않으면 Null Return
     * 
     * @param item
     * @return
     * @throws Exception
     */
    public static TCComponentItemRevision getLatestReleasedRevision(TCComponentItem item) throws Exception {

        TCComponent[] revisionList = item.getRelatedComponents("revision_list");
        if (revisionList.length > 0) {
            for (int inx = (revisionList.length-1); inx >= 0; inx--) {
                TCComponentItemRevision revision = (TCComponentItemRevision) revisionList[inx];
                if( CustomUtil.isReleased(revision) )
                {
                	return revision;
                }
            }
        }
        return null;
    }

    
    
    

    public static TCComponentPseudoFolder getPseudoFolderComponent(TCSession session, TCComponent component, String rel_name) throws TCException {
        AIFComponentContext contexts[] = component.getChildren();
        String displayName = getTextServerString(session, rel_name, "");
        for (AIFComponentContext context : contexts) {
            TCComponent comp = (TCComponent) context.getComponent();
            if (comp.toString().equals(displayName)) {
                return (TCComponentPseudoFolder) comp;
            }
        }
        return null;
    }

    /**
     * user_iD TCComponentGroupMember 구하기 Copyright : (주) ENES Solutions
     * 
     * @author : mskim
     * @since : 2009. 11. 6.
     * @param session
     * @param userId
     * @return
     */
    public static TCComponentGroupMember getTCUser(TCSession session, String userId) {
        try {
            if (userId != null && !userId.equals("")) {
                TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
                TCComponentUser user = userType.find(userId);
                return getGroupMember(user);
            } else {
                return null;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TCComponentGroupMember getGroupMember(TCComponent user) throws TCException {
        TCComponentGroupMemberType gmType = (TCComponentGroupMemberType) user.getSession().getTypeComponent("GroupMember");
        TCComponentGroup defaultGroup = (TCComponentGroup) user.getReferenceProperty("default_group");
        TCComponentGroupMember[] gm = gmType.findByUser((TCComponentUser) user);
        for (int i = 0; i < gm.length; i++) {
            if (defaultGroup.equals(gm[i].getGroup())) {
                return gm[i];
            }
        }
        return null;
    }

    /**
     * 주어진 USERID의 사용자가 Project에 할당 되었는지 여부를 확인하는 함수
     * 
     * Copyright : (주) 링크
     * 
     * @author : Lee Si Jung
     * @since : 2010. 1. 19.
     * @param session
     * @param userID
     * @return
     */
    public static boolean getAssignedProject(TCSession session, String userID) {

        boolean isAssignProject = false;
        try {
            TCComponentGroupMember member = getTCUser(session, userID);
            TCComponentProjectType imancomponentprojecttype = (TCComponentProjectType) session.getTypeComponent("TC_Project");
            TCComponentProject[] proj = imancomponentprojecttype.extent(member.getUser(), false);
            if (proj != null && proj.length > 0) {
                isAssignProject = true;
            }

        } catch (TCException e) {
            e.printStackTrace();
        }
        return isAssignProject;
    }

    public static TCComponentItem getItemByRevMasterForm(TCComponentForm revMasterForm) throws TCException {
        TCComponentItemRevision itemRevision = getItemRevisionByRevMasterForm(revMasterForm);
        return itemRevision == null ? null : itemRevision.getItem();
    }

    public static TCComponentItemRevision getItemRevisionByRevMasterForm(TCComponentForm revMasterForm) throws TCException {
        AIFComponentContext contexts[] = revMasterForm.whereReferenced();
        for (AIFComponentContext context : contexts) {
            String relType = context.getContext().toString();
            if (relType.equals("IMAN_master_form")) {
                return (TCComponentItemRevision) context.getComponent();
            }
        }
        return null;
    }

    public static TCComponentForm getItemRevMasterFormByItemRevision(TCComponentItemRevision revision) throws TCException {
        AIFComponentContext[] contexts = revision.getRelated();
        TCComponentForm form = null;
        for (AIFComponentContext context : contexts) {
            String contextStr = context.getContext().toString();
            if (contextStr.equals("IMAN_master_form_rev")) {
                form = (TCComponentForm) context.getComponent();
            }
        }
        return form == null ? null : form;
    }

    /**
     * AIFComponentContext[] 타입을 TCComponent[]로 변환 Copyright : (주) ENES
     * Solutions
     * 
     * @author : mskim
     * @since : 2009. 10. 16.
     * @param comp
     * @return
     */
    public static TCComponent[] getChangeTCComponent(AIFComponentContext[] comp) {
        TCComponent[] items = new TCComponent[comp.length];
        if (comp != null && comp.length != 0) {
            for (int j = 0; j < comp.length; j++) {
                items[j] = (TCComponent) comp[j].getComponent();
            }
        }
        return items;
    }

    /**
     * InterfaceAIFComponent[] 타입을 TCComponent[]로 변환 Copyright : (주) ENES
     * Solutions
     * 
     * @author : mskim
     * @since : 2009. 12. 7.
     * @param targetComponents
     * @return
     */
    public static TCComponent[] getChangeTCComponent(InterfaceAIFComponent[] targetComponents) {
        TCComponent[] target = new TCComponent[targetComponents.length];
        for (int i = 0; i < targetComponents.length; i++) {
            target[i] = (TCComponent) targetComponents[i];
        }
        return target;
    }

    /**
     * TCComponent[]타입을 TCComponentItemRevision[]타입으로 형변환 Copyright : (주) ENES
     * Solutions
     * 
     * @author : mskim
     * @since : 2009. 10. 30.
     * @param comp
     * @return
     * @throws TCException
     */
    public static TCComponentItemRevision[] getChangeCompRev(TCComponent[] comp) throws TCException {

        TCComponentItemRevision[] compRev = new TCComponentItemRevision[comp.length];
        for (int i = 0; i < comp.length; i++) {
            if (comp[i] instanceof TCComponentItem) {
                TCComponentItem item = (TCComponentItem) comp[i];
                compRev[i] = (TCComponentItemRevision) item.getLatestItemRevision();
            } else if (comp[i] instanceof TCComponentItemRevision) {
                compRev[i] = (TCComponentItemRevision) comp[i];
            }
        }
        return compRev;
    }

    /**
     * TCComponent[] 타입을 TCComponentForm[]으로 형변환 Copyright : (주) ENES Solutions
     * 
     * @author : mskim
     * @since : 2009. 10. 22.
     * @param comp
     * @return
     */
    public static TCComponentForm[] getChangeComponentForm(TCComponent[] comp) {

        TCComponentForm[] forms = null;
        if (comp != null && comp.length != 0) {
            forms = new TCComponentForm[comp.length];
            for (int i = 0; i < forms.length; i++) {
                if (comp[i] instanceof TCComponentForm) {
                    forms[i] = (TCComponentForm) comp[i];
                }

            }
        }
        return forms;
    }

    /**
     * imanRev의 relation type과 revision rule로 연결된 component list가져오기. Copyright
     * : (주) ENES Solutions
     * 
     * @author : jhkim
     * @since : 2009. 10. 14.
     * @param imanRev
     * @param type
     * @param revRule
     * @return
     */
    public static ArrayList<TCComponent> getWhereUsed(TCComponent imanRev, String type, TCComponentRevisionRule revRule) throws TCException {
        TCComponent imanCompRev = null;

        ArrayList<TCComponent> compsList = new ArrayList<TCComponent>();

        TCComponent[] imanComps = imanRev.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
        for (int i = 0; i < imanComps.length; i++) {
            if (imanComps[i].getType().equalsIgnoreCase(type)) {
                imanCompRev = imanComps[i];
                compsList.add(imanCompRev);
            }
        }
        return compsList;
    }

    public static TCComponent searchRelationComponent(TCComponentItemRevision revision, String conponentType, String relationType) throws TCException {
        AIFComponentContext[] relatedComp = revision.getRelated(relationType);
        TCComponent comp = null;
        if (revision != null) {
            for (int i = 0; i < relatedComp.length; i++) {
                if (relatedComp[i].getComponent().getType().equals(conponentType)) {
                    comp = (TCComponent) relatedComp[i].getComponent();
                }
            }
        }
        return comp;
    }

    /**
     * item, itemrevision의 getLatestItemRevision을 반환.
     * 
     * Copyright : (주) ENES Solutions.
     * 
     * @author : jhkim
     * @since : 2009. 11. 12.
     * @param comp
     * @return
     * @throws targetLatestRev
     */
    public static TCComponentItemRevision getTargetCompLatestRev(TCComponent comp) throws TCException {
        TCComponentItemRevision targetLatestRev = null;
        if (comp instanceof TCComponentItem) {
            TCComponentItem targetItem = (TCComponentItem) comp;
            targetLatestRev = targetItem.getLatestItemRevision();
        } else if (comp instanceof TCComponentItemRevision) {
            TCComponentItemRevision targetRev = (TCComponentItemRevision) comp;
            TCComponentItem targetItem = targetRev.getItem();
            targetLatestRev = targetItem.getLatestItemRevision();
        }
        return targetLatestRev;
    }

    /**
     * TargetRevision의 Status Object를 Return 하는 함수
     * 
     * Copyright : (주) 링크
     * 
     * @author : Lee Si Jung
     * @since : 2009. 11. 12.
     * @param targetRevision
     * @return
     * @throws TCException
     */
    public static TCComponentReleaseStatus getStatusObject(TCComponent targetRevision, String statusName) throws TCException {

        TCProperty property = targetRevision.getTCProperty("release_status_list");
        if (property != null) {
            TCComponent[] statuss = property.getReferenceValueArray();
            for (int i = 0; i < statuss.length; i++) {
                if (statuss[i].getProperty("object_name").equals(statusName)) {
                    return (TCComponentReleaseStatus) statuss[i];
                }
            }
        }
        return null;
    }

    /**
     * TargetRevision의 Status Object를 Return 하는 함수
     * 
     * Copyright : (주) 링크
     * 
     * @author : Lee Si Jung
     * @since : 2009. 11. 12.
     * @param targetRevision
     * @return
     * @throws TCException
     */
    public static TCComponentReleaseStatus getStatusObject(TCComponentItemRevision targetRevision) throws TCException {
        TCComponentReleaseStatus status = null;

        TCProperty property = targetRevision.getTCProperty("release_status_list");
        if (property != null) {
            TCComponent[] statuss = property.getReferenceValueArray();
            if (statuss != null && statuss.length > 0) {
                status = (TCComponentReleaseStatus) statuss[0];
            }
        }
        return status;
    }

    /**
     * Component[] 의 revision가져오기 Copyright : (주) ENES Solutions
     * 
     * @author : jhkim
     * @since : 2009. 10. 14.
     * @param itemRevision
     * @param relType
     * @return
     */

    public static TCComponent[] getChildrens(TCComponentItemRevision itemRevision, String relType) throws Exception {

        AIFComponentContext[] context = itemRevision.getRelated(relType);
        TCComponent[] comps = new TCComponent[context.length];

        for (int i = 0; i < context.length; i++) {
            comps[i] = (TCComponent) context[i].getComponent();
        }
        return comps;
    }

    public static TCComponentGroupMember[] searchTCMemberComponent(TCSession session, String userName, String groupName, String roleName) {

        String[] entries = { "이름", "부서(팀)", "역할" };
        String[] values = { userName.trim() + "*", groupName.trim() + "*", roleName.trim() + "*" };
        TCComponentGroupMember[] aMember = null;

        try {
            TCComponentQueryType iCompQuerytType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
            TCComponentQuery iCompQuery = (TCComponentQuery) iCompQuerytType.find("Members");
            TCComponent[] aTCComp = iCompQuery.execute(entries, values);
            aMember = new TCComponentGroupMember[aTCComp.length];
            for (int i = 0; i < aTCComp.length; i++) {
                aMember[i] = (TCComponentGroupMember) aTCComp[i];
            }
        } catch (TCException ie) {
            ie.printStackTrace();
        }
        return aMember;
    }

    /**
     * Rel_Type으로 ItemRevision 하위에 있는 Component를 가지고 온다 Copyright : (주) ENES
     * Solutions
     * 
     * @author : mskim
     * @since : 2009. 10. 14.
     * @param itemRevision
     * @param pseudeFolder
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static Hashtable getRelatedComponentsTOHash(TCComponentItemRevision itemRevision, String pseudeFolder) throws Exception {

        AIFComponentContext[] context = itemRevision.getRelated(pseudeFolder);
        Hashtable hash = new Hashtable();
        TCComponent comps = null;
        if (context.length != 0 && context != null) {
            for (int i = 0; i < context.length; i++) {
                comps = (TCComponent) context[i].getComponent();
                hash.put(comps.getProperty("object_name"), comps);
            }
        }
        return hash;
    }

    /**
     * Dataset이 가지고 있는 모든 reference name을 찾는 method
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @return Vector
     * @throws Exception
     */
    public static Vector<String> getAllNamedRefTypeArray(TCComponentDataset datasetComponent) throws Exception {
        Vector<String> s = new Vector<String>();
        NamedReferenceContext[] namedRefContext = null;
        try {
            namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
            for (int i = 0; i < namedRefContext.length; i++) {
                String s1 = namedRefContext[i].getNamedReference();
                s.addElement(s1);
            }
        } catch (Exception e) {
            throw e;
        }
        return s;
    }

    /**
     * Dataset 내에 있는 모든 named reference를 삭제하는 method이다.
     * 
     * @param dataset
     *            TCComponentDataset
     * @throws Exception
     */
    public static void removeAllNamedReference(TCComponentDataset dataset) throws Exception {
        TCComponentTcFile[] imanFile = dataset.getTcFiles();
        Vector<String> refNameVector = getAllNamedRefTypeArray(dataset);
        for (int i = 0; i < refNameVector.size(); i++) {
            dataset.removeNamedReference(refNameVector.elementAt(i).toString());
        }
        for (int j = 0; j < imanFile.length; j++) {
            imanFile[j].delete();
        }
    }

    /**
     * 해당 Component가 CheckOut 되어 있는지 판별
     * 
     * @param target
     *            : 대상 TCComponent
     * @return
     * @throws TCException
     */
    public static boolean isCheckedOut(TCComponent target) {
        ViewEditHelper veHelper = new ViewEditHelper(target.getSession());
        boolean ischeckOutStatus = veHelper.isCheckedOut(target);

        return ischeckOutStatus;
    }

    /**
     * 해당 Component가 CheckOut 되어 있는지 판별
     * 
     * @param target
     *            : 대상 TCComponent
     * @return
     * @throws Exception
     */
    public static boolean isReserved(TCComponent target) throws Exception {
        TCReservationService tcreservationservice = target.getSession().getReservationService();
        return tcreservationservice.isReserved(target);
    }

    /**
     * 해당 Components를 Checkout 하는 Function
     * 
     * @param targets
     *            TCComponent 배열
     * @return 성공 여부
     * @throws Exception
     */
    public static boolean reserveComponent(TCComponent[] targets) throws Exception {
        boolean isSuccess = true;
        try {
            TCReservationService tcreservationservice = targets[0].getSession().getReservationService();
            tcreservationservice.reserve(targets);
        } catch (Exception e) {
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 해당 Components를 CheckIn 하는 Function
     * 
     * @param targets
     *            TCComponent 배열
     * @return 성공 여부
     * @throws Exception
     */
    public static boolean unreserveComponent(TCComponent[] targets) throws Exception {
        boolean isSuccess = true;
        try {
            TCReservationService tcreservationservice = targets[0].getSession().getReservationService();
            tcreservationservice.unreserve(targets);
        } catch (Exception e) {
            isSuccess = false;
        }
        return isSuccess;
    }

    /**
     * 해당 Component의 수정 가능 여부를 Return
     * 
     * @param target
     * @return 수정 가능시 False Return, Read Only시 True Return
     * @throws Exception
     */
    public static boolean isReadOnly(InterfaceAIFComponent target) throws Exception {
        boolean isReadOnly = true;
        if (target == null)
            return isReadOnly;
        AbstractAIFSession session = target.getSession();
        if ((session == null) || (!(session instanceof TCSession)) || (!(target instanceof TCComponent)))
            return isReadOnly;

        TCComponent localTCComponent = (TCComponent) target;
        isReadOnly = (localTCComponent.okToModify()) ? false : true;
        return isReadOnly;
    }

    /**
     * Component가 결재 완료인지 아닌지를 판단한다.
     * 
     * @param component
     *            TCComponent
     * @return boolean 결재 완료이면 true를 리턴 그렇지 않으면 false를 리턴한다.
     * @throws Exception
     */
    public static boolean isReleased(TCComponent component) throws TCException {
        component.refresh();

        if (!component.getProperty("release_status_list").equalsIgnoreCase("")) {
            return true;
        }

        return false;
    }

    /**
     * Title: Component의 owing user인지 Check함<br>
     * Usage:
     * 
     * @param components
     * @return boolean Component의 소유권자이면 true, 그렇지 않으면 false
     */
    public static boolean isComponentOwner(TCComponent components) {
        try {
            TCSession session = (TCSession) components.getSession();
            TCComponentUser imancomponentuser = (TCComponentUser) (components.getReferenceProperty("owning_user"));
            if (session.getUser() != imancomponentuser) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Title: Component가 Working Status인지 Check함 Usage:
     * 
     * @param components
     * @return boolean Component가 Working이면 true, 그렇지 않으면 false
     */
    public static int isInProcess(TCComponent components) throws TCException {
        boolean hasStatus = !components.getProperty("release_status_list").equalsIgnoreCase("");
        // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체 
//        boolean isInProcess = !components.getProperty("process_stage_list").equalsIgnoreCase("");
        boolean isInProcess = !components.getProperty("fnd0StartedWorkflowTasks").equalsIgnoreCase("");
        return (hasStatus ? TcDefinition.STATUS_HAS_STATUS : 0) + (isInProcess ? TcDefinition.STATUS_IN_PROCESS : 0);
    }

    /**
     * TCComponent 에 작성(쓰기) 권한 여부 반환
     * 
     * @param iComp
     *            TCComponent
     * @return 쓰기 권한 있으면 true
     */
    public static boolean isWritable(TCComponent iComp) {
        try {
            TCAccessControlService accessSvc = iComp.getSession().getTCAccessControlService();
            return accessSvc.checkPrivilege(iComp, "WRITE");
        } catch (TCException ie) {
            ie.printStackTrace();
        }
        return false;
    }

    public static boolean isBOMWritable(TCComponentBOMLine bomLine) throws TCException {
        TCComponent targetBVR = bomLine.getReferenceProperty("bl_bomview_rev");
        if ((targetBVR != null && !isWritable(targetBVR)) || (targetBVR == null && (!isWritable(bomLine.getItem()) || !isWritable(bomLine.getItemRevision())))) {
            return false;
        }
        return true;
    }

    /**
     * 해당 아이디로 된 STCDocment가 있는지 확인하는 메소드
     * 
     * @param session
     * @param itemId
     * @return
     * @throws TCException
     * @author Jeahun Lee
     */
    public static boolean hasDocumentItem(TCSession session, String itemId) throws TCException {
        boolean isItem = false;
        // TCComponentQueryType tccomponentquerytype = (TCComponentQueryType)
        // session.getTypeComponent("ImanQuery");
        // TCComponentQuery tccomponentquery = (TCComponentQuery)
        // tccomponentquerytype.find("Item...");
        // String props[] = { "Item ID", "Type" };
        // String values[] = { itemId, "STCDocument" };
        // TCComponentContextList tccomponentcontextlist =
        // tccomponentquery.getExecuteResultsList(props, values);

        String keys[] = { SYMTcUtil.getTextServerString(session, "k_find_itemid_name"), SYMTcUtil.getTextServerString(session, "Type") };
        String values[] = { itemId, "STCDocument" };

        SYMComponentQuery query = new SYMComponentQuery(session, "Item...");
        TCComponent[] item = query.execute(keys, values);

        if (item.length > 0) {
            isItem = true;
        } else {
            isItem = false;
        }
        return isItem;
    }

    public static TCComponent[] findComponetByQueryKey(TCSession session, String queryKey, String[] keys, String[] values) throws TCException {
        return findComponent(session, getTextServerString(session, queryKey), keys, values);
    }

    public static TCComponent[] findComponet(TCSession session, String[] keys, String[] values) throws TCException {
        return findComponent(session, getTextServerString(session, "k_find_general_name"), keys, values);
    }

    /**
     * Saves Query중 General...를 사용하는 검색 메소드
     * 
     * @param session
     * @param keys
     * @param values
     * @return TCComponentFolder
     * @throws TCException
     * @author Jeahun Lee
     */
    public static TCComponent[] findComponent(TCSession session, String queryName, String[] keys, String[] values) throws TCException {
        TCComponentQueryType tccomponentquerytype = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery tccomponentquery = (TCComponentQuery) tccomponentquerytype.find(queryName);
        TCComponentContextList tccomponentcontextlist = tccomponentquery.getExecuteResultsList(keys, values);
        TCComponent ccomponent[] = tccomponentcontextlist.toTCComponentArray();
        return ccomponent;
    }

    /**
     * Item ID로 Item을 찾기위한 Method
     * 
     * @param session
     *            - TCSession
     * @param itemId
     *            - Search keyword
     * @return - 조회한 TCComponentItem
     * @throws Exception
     */
    public static TCComponentItem findItem(TCSession session, String itemId) throws Exception {
        TCComponentItem item = null;
        try {
            TCComponentItemType cItemType = (TCComponentItemType) session.getTypeComponent("Item");
            
            //item = cItemType.find(itemId);         
            TCComponentItem[] items = cItemType.findItems(itemId);
            if(items != null && items.length > 0) {
            	item = items[0];
            }   
        } catch (Exception e) {
            throw e;
        }
        return item;
    }

    /**
     * STCStandardDrawingNumber 로 Item을 찾기위한 Method
     * 
     * @param session
     *            - TCSession
     * @param itemId
     *            - Search keyword
     * @return - 조회한 TCComponentItem
     * @throws Exception
     */
    public static TCComponentItem findPart(TCSession session, String STCStandardDrawingNumber) throws Exception {
        String keys[] = { SYMTcUtil.getTextServerString(session, "STCStandardDrawingNumber") };
        String values[] = { STCStandardDrawingNumber };

        SYMComponentQuery query = new SYMComponentQuery(session, "STCPart");
        TCComponent[] item = query.execute(keys, values);
        return (TCComponentItem) item[0];
    }

    /**
     * Item ID, Revision 으로 ItemRevision을 찾기위한 Method
     * 
     * @param session
     *            - TCSession
     * @param itemId
     *            - Search keyword
     * @param revision
     *            - Search keyword
     * @return - 조회한 TCComponentItemRevision
     * @throws Exception
     */
    public static TCComponentItemRevision findItemRevision(TCSession session, String itemId, String revision) throws Exception {
        TCComponentItemRevision itemrevision = null;
        try {
            TCComponentItemRevisionType cItemRevisionType = (TCComponentItemRevisionType) session.getTypeComponent("ItemRevision");
            
            //itemrevision = cItemRevisionType.findRevision(itemId, revision);
            TCComponentItemRevision[] itemrevisions = cItemRevisionType.findRevisions(itemId,  revision);
            if(itemrevisions != null && itemrevisions.length > 0) {
            	itemrevision = itemrevisions[0];
            }
        } catch (Exception e) {
            throw e;
        }
        return itemrevision;
    }

    /**
     * STCPart ID로 검색하여 List를 리턴한다.
     * 
     * @param session
     * @param strItemID
     * @return
     * @throws TCException
     * @author Jeahun Lee
     */
    @SuppressWarnings("rawtypes")
    public static List findSTCPartByID(TCSession session, String strItemID) throws TCException {
        java.util.List<InterfaceAIFComponent> list = new Vector<InterfaceAIFComponent>();
        TCComponentQueryType tccomponentquerytype = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery tccomponentquery = (TCComponentQuery) tccomponentquerytype.find(SYMTcUtil.getTextServerString(session, "Item..."));
        // String props[] = { "Item ID", "Type" };
        String keys[] = { SYMTcUtil.getTextServerString(session, "Item ID"), SYMTcUtil.getTextServerString(session, "Type") };
        String values[] = { strItemID, "STCPart" };

        TCComponentContextList tccomponentcontextlist = tccomponentquery.getExecuteResultsList(keys, values);
        list = tccomponentcontextlist.toComponentVector();
        return list;
    }

    /**
     * 그룹명과 롤명을 이용한 멤버를 유저배열로 리턴한다.
     * 
     * @param session
     * @param groupName
     * @param roleName
     * @return
     * @throws TCException
     */
    public static TCComponentUser[] findUserByGroupRoleName(TCSession session, String groupName, String roleName) throws TCException {

        TCComponentRoleType roleType = (TCComponentRoleType) session.getTypeComponent("Role");
        TCComponentRole role = roleType.find(groupName);
        TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
        TCComponentGroup group = groupType.find(roleName);
        TCComponentGroupMemberType memberType = (TCComponentGroupMemberType) session.getTypeComponent("GroupMember");
        TCComponentGroupMember[] member = memberType.findByRole(role, group);

        TCComponentUser[] users = new TCComponentUser[member.length];
        for (int i = 0; i < member.length; i++) {
            TCComponentUser user = (TCComponentUser) member[i].getTCProperty("user").getReferenceValue();
            users[i] = user;
        }
        return users;
    }

    /**
     * 특정 객체와 LOV명을 전달하여 객체에 LOV값을 매핑 시키는 메소드
     * 
     * @param session
     * @param obj
     * @param lovName
     */
    public static void matchLov(TCSession session, Object obj, String lovName) {
        matchLov(session, obj, lovName, null);
    }

    /**
     * 특정 객체와 LOV명을 전달하여 객체에 LOV값을 매핑 시키는 메소드
     * 
     * @param session
     * @param obj
     * @param lovName
     * @param initValue
     *            : obj 객체에 LOV값 외에 초기에 선택되어야 할 값
     */
    public static void matchLov(TCSession session, Object obj, String lovName, String initValue) {
        matchLov(session, obj, lovName, initValue, null);
    }

    /**
     * 특정 객체와 LOV명을 전달하여 객체에 LOV값을 매핑 시키는 메소드
     * 
     * @param session
     * @param obj
     * @param lovName
     * @param initValue
     *            : obj 객체에 LOV값 외에 초기에 선택되어야 할 값
     * @param logger
     *            : Log4j logger 객체.
     */
    public static void matchLov(TCSession session, Object obj, String lovName, String initValue, Logger logger) {
        try {
            if (obj instanceof Combo) {
                ((Combo) obj).removeAll();
                String[] tempArray = getLovValues(lovName, session);
                if (initValue != null) {
                    String[] resultArray = new String[tempArray.length + 1];
                    resultArray[0] = initValue;
                    for (int i = 0; i < tempArray.length; i++) {
                        resultArray[i + 1] = tempArray[i];
                    }
                    ((Combo) obj).setItems(resultArray);
                } else {
                    ((Combo) obj).setItems(tempArray);
                }
            }
        } catch (TCException e) {
            if (logger != null) {
                logger.error("[" + lovName + "] invalid match!!!", e);
            }
            e.printStackTrace();
        }
    }

    /**
     * LOV 이름을 주면 String[] 로 Description을 가져온다.
     * 
     * @param string
     * @return
     * @throws TCException
     */
    public static String[] getLOVDescription(TCSession session, String string) throws TCException {
        TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] listofvalues = listofvaluestype.find(string);
        TCComponentListOfValues listofvalue = listofvalues[0];
        return listofvalue.getListOfValues().getDescriptions();
    }

    /**
     * LOV 이름을 주면 String[] 로 LOVDisplayValues을 가져온다.
     * 
     * @param string
     * @return
     * @throws TCException
     */
    public static String[] getLOVDisplayValues(TCSession session, String string) throws TCException {
        TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] listofvalues = listofvaluestype.find(string);
        TCComponentListOfValues listofvalue = listofvalues[0];
        return listofvalue.getListOfValues().getLOVDisplayValues();
    }

    /**
     * LOV이름과 Index를 주면 그 해당되는 값을 가져온다.
     * 
     * @param lov
     * @param i
     * @return
     */
    public static String getLOVValue(TCSession session, String lov, int i) throws TCException {
        TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session.getTypeComponent("ListOfValues");
        TCComponentListOfValues[] listofvalues = listofvaluestype.find(lov);
        TCComponentListOfValues listofvalue = listofvalues[0];
        Object[] objects = listofvalue.getListOfValues().getListOfValues();
        return objects[i].toString();
    }

    /**
     * STCNPI Item 신규 ID 발번
     * 
     * @param session
     * @return
     */
    public static String getNPISeriesNum(TCSession session) {
        try {
            String strNewID = UniqueID.getID(session, "NPI", 9);
            return strNewID;
        } catch (TCException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static boolean isMultiSheetDrawing(TCComponentItem item) {

        try {
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * ESB에서 가져온 UserInfo를 User 정보로 가공.
     * 
     * @param elements
     *            ESB유저정보
     * @param t_CM
     *            넣을 사용자 테이블
     * @author s.j park
     */
    @SuppressWarnings("unchecked")
    public static final HashMap<String, String> getUserData(Object elements) {
        HashMap<String, String> hm_cmUserInfo = new HashMap<String, String>();
        Vector<String> vt_esbUserInfoRow = (Vector<String>) elements;
        hm_cmUserInfo.put("deptNm", vt_esbUserInfoRow.get(2));
        hm_cmUserInfo.put("userId", vt_esbUserInfoRow.get(5));
        hm_cmUserInfo.put("userNm", vt_esbUserInfoRow.get(1));
        hm_cmUserInfo.put("position", vt_esbUserInfoRow.get(3));
        return hm_cmUserInfo;
    }

    /**
     * TCComponent Array를 특정 속성으로 Sort함
     * 
     * @param session
     * @param result
     * @param strAttName
     * @return
     * @throws TCException
     */
    public static TCComponent[] sortTCCompoentArray(TCSession session, TCComponent[] szTarget, String strSortAttName) throws TCException {

        Hashtable<String, TCComponent> sortHash = new Hashtable<String, TCComponent>();
        Hashtable<String, TCComponent> resultHash = null;
        for (int i = 0; i < szTarget.length; i++) {
            TCComponent tcComp = szTarget[i];

            sortHash.put(tcComp.getProperty(strSortAttName), tcComp);
        }
        resultHash = SYMTcUtil.hashSort(sortHash);
        TCComponent sortResult[] = new TCComponent[resultHash.size()];

        for (int i = 0; i < resultHash.size(); i++) {
            sortResult[i] = (TCComponent) resultHash.get(i + "");
        }

        return sortResult;
    }

    /**
     * Hashtable Sorting
     * 
     * @param h
     * @return
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Hashtable<String, TCComponent> hashSort(Hashtable<String, TCComponent> h) {
        Hashtable<String, TCComponent> sortHash = new Hashtable<String, TCComponent>();
        SortedMap smap = Collections.synchronizedSortedMap(new TreeMap(h));

        synchronized (smap) {
            Iterator iterator = smap.keySet().iterator();

            int iKey = 0;
            while (iterator.hasNext()) {
                String strKey = (String) iterator.next();
                sortHash.put(iKey + "", h.get(strKey));

                iKey = iKey + 1;
            }
        }
        h.clear();
        return sortHash;
    }

    public static HashMap<String, String> checkACADName(String strRevID, String strDrawingNum, String[] szDatasetName) {
        HashMap<String, String> resultMap = new HashMap<String, String>();
        String[] szRev = SYMTcUtil.getSplitString(strRevID, ".");

        if (szRev == null || szRev.length != 2) {
            String strMessage = "유효한 Revision ID가 아닙니다.";
            if (resultMap.containsKey(strRevID))
                resultMap.put(strRevID, resultMap.get(strRevID) + "," + strMessage);
            else
                resultMap.put(strRevID, strMessage);

            return resultMap;
        }

        String strRevPrefix = strRevID.substring(0, 1);

        boolean isPostStandardization = SYMTcUtil.isPostStandardization(strRevID);

        if (szDatasetName.length == 1) {
            String strWishName = strDrawingNum;
            if (isPostStandardization)
                strWishName = strDrawingNum + "R" + strRevPrefix;

            String strMessage = "Dataset명은 '" + strWishName + "'(으)로 시작해야 합니다.";

            if (!szDatasetName[0].startsWith(strWishName)) {
                if (resultMap.containsKey(szDatasetName))
                    resultMap.put(szDatasetName[0], resultMap.get(szDatasetName[0]) + "," + strMessage);
                else
                    resultMap.put(szDatasetName[0], strMessage);
            }

        } else if (szDatasetName.length > 1) {
            int nSheetNum = szDatasetName.length;

            String strTotSheetNum = "" + nSheetNum;

            for (int i = strTotSheetNum.length(); i < 3; i++) {
                strTotSheetNum = "0" + strTotSheetNum;
            }

            String[] szWishName = new String[nSheetNum];
            for (int i = 0; i < nSheetNum; i++) {
                String strSheetNum = "" + (i + 1);

                for (int j = strSheetNum.length(); j < 3; j++) {
                    strSheetNum = "0" + strSheetNum;
                }

                String strWishName = strDrawingNum + "-" + strSheetNum + strTotSheetNum;// +"R"+szRev[0];
                szWishName[i] = strWishName;

                // 첫번째 Sheet인 경우
                if (i == 0) {
                    String strFirstWishName = strWishName;
                    String strMessage = "";
                    if (isPostStandardization) {
                        strFirstWishName = strFirstWishName + "R" + strRevPrefix;
                        strMessage = "AutoCAD 첫번째 Sheet명은 Revision ID가 포함되어 야 합니다.(ex :" + strFirstWishName + ")";
                    } else {
                        strMessage = "AutoCAD 첫번째 Sheet명이 옳바르지 않습니다.(ex :" + strFirstWishName + ")";
                    }

                    boolean isFirstWishNameChecked = false;
                    for (int j = 0; j < nSheetNum; j++) {
                        if (szDatasetName[j].startsWith(strWishName)) {

                            if (!szDatasetName[j].startsWith(strFirstWishName)) {
                                if (resultMap.containsKey(szDatasetName))
                                    resultMap.put(szDatasetName[j], resultMap.get(szDatasetName[j]) + "," + strMessage);
                                else
                                    resultMap.put(szDatasetName[j], strMessage);

                            }

                            isFirstWishNameChecked = true;

                        }
                    }

                    // 첫번째 Dataset 이름을 찾지 못했다면 ItemRevision에 Error Message를
                    // 저장한다.
                    if (!isFirstWishNameChecked) {
                        if (resultMap.containsKey(strRevID))
                            resultMap.put(strRevID, resultMap.get(strRevID) + "," + strMessage);
                        else
                            resultMap.put(strRevID, strMessage);
                    }

                    continue;
                }

                boolean isExistWishName = false;
                for (int j = 0; j < nSheetNum; j++) {

                    if (szDatasetName[j].startsWith(strWishName)) {
                        isExistWishName = true;

                        if (szDatasetName[j].length() >= (strWishName.length() + 2)) {
                            String strRevIndicator = szDatasetName[j].substring(strWishName.length(), strWishName.length() + 1);
                            if (strRevIndicator.equals("R")) {
                                char cRev = szDatasetName[j].charAt(strWishName.length());

                                if (SYMTcUtil.isOtherSheetValid(strRevPrefix.charAt(0), cRev)) {
                                    String strMessage = "Dataset명에 현재 Revision ID 보다 높은 Revision ID가 명시되어 있습니다.";

                                    if (resultMap.containsKey(szDatasetName[j]))
                                        resultMap.put(szDatasetName[j], resultMap.get(szDatasetName[j]) + "," + strMessage);
                                    else
                                        resultMap.put(szDatasetName[j], strMessage);
                                }
                            }
                        }

                        break;
                    }
                }

                if (!isExistWishName) {
                    String strMessage = "'" + strWishName + "'(으)로 시작하는 Dataset명을 찾을 수 없습니다.";

                    if (resultMap.containsKey(strRevID))
                        resultMap.put(strRevID, resultMap.get(strRevID) + "," + strMessage);
                    else
                        resultMap.put(strRevID, strMessage);
                }

            }

            for (int i = 0; i < szDatasetName.length; i++) {
                boolean isValidName = false;
                for (int j = 0; j < szWishName.length; j++) {

                    if (szDatasetName[i].startsWith(szWishName[j])) {
                        isValidName = true;
                        break;
                    }

                }

                if (!isValidName) {
                    String strMessage = "유효한 Dataset명이 아닙니다.";

                    if (resultMap.containsKey(szDatasetName[i]))
                        resultMap.put(szDatasetName[i], resultMap.get(szDatasetName[i]) + "," + strMessage);
                    else
                        resultMap.put(szDatasetName[i], strMessage);
                }

            }

        }

        return resultMap;
    }

    /**
     * 해당 Revision ID가 규격화 이후 버전인지 Check
     * 
     * 1. 규격화시 Revision ID : "-" 2. 규격화 이후 Revision ID : "A"~"Z"
     * 
     * @param strRevID
     * @return
     */
    public static boolean isPostStandardization(String strRevID) {
        char revPrefix = strRevID.charAt(0);

        if (revPrefix == '-') {
            return true;
        }

        if ('A' <= revPrefix && revPrefix <= 'Z') {
            return true;
        }

        return false;
    }

    public static boolean isOtherSheetValid(char cRev, char cDSRev) {
        String strRevRule = " -ABCDEFGHIJKLMNOPQRSTUVXYZ";

        char[] szChar = strRevRule.toCharArray();
        int nRevIndex = 0;
        int nDSRevIndex = 0;

        for (int i = 0; i < szChar.length; i++) {
            if (cRev == szChar[i])
                nRevIndex = i;

            if (cDSRev == szChar[i])
                nDSRevIndex = i;

        }

        if (!(nRevIndex >= nDSRevIndex))
            return false;

        return true;
    }

    /**
     * splitter로 구별되는 문자열을 쪼개어 Vector로 return한다.
     * 
     */
    public static String[] getSplitString(String strValue, String splitter) {
        if (strValue == null || strValue.length() == 0)
            return null;

        if (splitter == null)
            return null;

        StringTokenizer split = new StringTokenizer(strValue, splitter);
        ArrayList<String> strList = new ArrayList<String>();
        while (split.hasMoreTokens()) {
            strList.add(new String(split.nextToken().trim()));
        }

        String[] szValue = new String[strList.size()];
        for (int i = 0; i < strList.size(); i++) {
            szValue[i] = strList.get(i);
        }

        return szValue;
    }

    /**
     * 리비젼에 도면이 붙어 있는지 확인하는 함수(ACAD, Pro-E)
     * 
     * @param rev
     * @return
     */
    public static boolean getRevisionHasDrawingDataset(TCComponentItemRevision rev) throws Exception {
        AIFComponentContext[] context = rev.getChildren();

        for (int i = 0; i < context.length; i++) {
            TCComponent childComponent = (TCComponent) context[i].getComponent();
            if (childComponent.isTypeOf(TcDefinition.DATASET_TYPE_ACAD) || childComponent.isTypeOf(TcDefinition.DATASET_TYPE_CGM) || childComponent.isTypeOf(TcDefinition.DATASET_TYPE_EDIF)
                    || childComponent.isTypeOf(TcDefinition.DATASET_TYPE_ORCAD) || childComponent.isTypeOf(TcDefinition.DATASET_TYPE_PROASM)
                    || childComponent.isTypeOf(TcDefinition.DATASET_TYPE_PRODRW) || childComponent.isTypeOf(TcDefinition.DATASET_TYPE_PROPRT)) {
                return true;
            }
        }

        return false;
    }

    /**
     * revisionRule을 가져옴
     * 
     * @param session
     *            IMANSession
     * @param ruleName
     *            String
     * @return
     * @throws IMANException
     */
    public static TCComponentRevisionRule getRevisionRule(TCSession session, String ruleName) throws Exception {
        if (ruleName == null || ruleName.length() == 0)
            return null;

        TCComponentRevisionRule revRule = null;
        TCComponentRevisionRule[] revRules = TCComponentRevisionRule.listAllRules(session);

        if (revRules == null)
            return null;

        for (int i = 0; i < revRules.length; i++) {
            String revRuleName = revRules[i].getProperty("object_name");
            if (ruleName.trim().equalsIgnoreCase(revRuleName)) {
                revRule = revRules[i];
                break;
            }
        }

        return revRule;
    }

    /**
     * 조건에 맞는 표준결재선이 없는 경우 null을 반환한다. 결재선에서 세부 결재정보(Role, 유형)은 STC_line
     * (STCStdAppLineDetail) 에 배열로 저장되어 있다.
     * 
     * @param target
     *            결재의 Target입니다.(Form류, ItemRevision류 가 들어갑니다.)
     * @return
     * @throws Exception
     */

    public static TCComponent getStdAppLineList(String subType, InterfaceAIFComponent[] target) throws Exception {
        TCSession session = (TCSession) target[0].getSession();
        TCComponentType lTCComponentType = session.getTypeComponent("STCStdAppLine");
        TCComponent[] appLines = lTCComponentType.extent();
        TCComponent targetAppLine = null;

        // 결재대상이 문서인 경우 조건 확인
        String bType = target[0].getType();
        String doc_class1 = "";
        String doc_class2 = "";
        String doc_class3 = "";
        String doc_type = "";
        int matchDepth = 0;
        if (bType.equals("STCDocumentRevision") && target.length == 1) {
            TCComponentItemRevision itemRev = (TCComponentItemRevision) target[0];
            TCComponentItem item = itemRev.getItem();
            doc_type = item.getProperty("STC_doc_type");
            doc_class3 = item.getProperty("STC_doc_class3");
            doc_class2 = item.getProperty("STC_doc_class2");
            doc_class1 = item.getProperty("STC_doc_class1");
        }
        for (int i = 0; i < appLines.length; i++) {
            // 결재선유형(STC_type)이 object type 같은 경우
            String STC_type = appLines[i].getProperty("STC_type");
            String STC_target = appLines[i].getProperty("STC_target"); // Sub
                                                                       // Type
                                                                       // (세부
                                                                       // 기능)
            if (!bType.equals(STC_type))
                continue;
            if (bType.equals("STCDocumentRevision")) {
                if (doc_type != null && doc_type.length() != 0 && doc_type.equals(appLines[i].getProperty("STC_doc_type"))) {
                    // 문서 유형이 같은 것이 무조건 우선임.
                    return appLines[i];
                } else if ("".equals(appLines[i].getProperty("STC_doc_type")) && doc_class3.equals(appLines[i].getProperty("STC_doc_class3"))) {
                    matchDepth = 5;
                    targetAppLine = appLines[i];
                } else if (matchDepth < 4 && "".equals(appLines[i].getProperty("STC_doc_class3")) && doc_class2.equals(appLines[i].getProperty("STC_doc_class2"))) {
                    matchDepth = 4;
                    targetAppLine = appLines[i];
                } else if (matchDepth < 3 && "".equals(appLines[i].getProperty("STC_doc_class2")) && doc_class1.equals(appLines[i].getProperty("STC_doc_class1"))) {
                    matchDepth = 3;
                    targetAppLine = appLines[i];
                }
            } else if (subType != null && subType.equals(STC_target))
                return appLines[i];
            else if (STC_target == null || STC_target.length() == 0)
                return appLines[i];
            else
                throw new Exception("invalid approval line");
        }

        return targetAppLine;
    }

    public static String getObjectString(TCComponent tcObject) {
        String strRet = "";

        try {
            if (tcObject instanceof TCComponentItem && tcObject.getType().equals("STCSW") || tcObject.getType().equals("STCPart")) {
                strRet = tcObject.getProperty("STCStandardDrawingNumber") + "-" + tcObject.getProperty("object_name");
            } else {
                strRet = tcObject.getProperty("object_string");
            }
        } catch (TCException e) {
            e.printStackTrace();
        }
        return strRet;
    }
    
    /**
     * ECO 리스트를 가지고 관련 DWG(ECO-B) 다운로드 기능 실행 - Dataset Filter 분기 (ALL or Filter) 
     * (Filter 조건이 없으면(filterType == null) 기본 Filter조건으로 검색한다.)
     * 
     * @method getEcosDwgDatasetFiles 
     * @date 2013. 4. 3.
     * @param String[] ecos, String strDownloadPath, ArrayList<String> filterType
     * @return  HashMap<"ecoNo.", HashMap<"partNo./revNo.", HashMap<"datasetType", File[]>>>
     * @exception Exception
     * @throws
     * @see
     */
    public static HashMap<String, HashMap<String, HashMap<String, File[]>>> getEcosDwgDatasetFiles(String[] ecos, String strDownloadPath, ArrayList<String> filterType) throws Exception {
        // Filter 조건이 없으면 기본 Filter조건으로 검색한다.
        if(filterType == null) {
            return getEcosDatasetFiles(ecos, strDownloadPath, TcDefinition.CAT_DOWN_FILTER_TYPE);
        } else {
            return getEcosDatasetFiles(ecos, strDownloadPath, filterType);
        }
    }
    
    /**
     * ECO 리스트를 가지고 관련 DWG(ECO-B) 다운로드 기능 실행 - Dataset Filter 적용 
     * 
     * @method getEcosDatasetFiles 
     * @date 2013. 4. 3.
     * @param String[] ecos, String strDownloadPath, ArrayList<String> filterType
     * @return HashMap<"ecoNo.", HashMap<"partNo./revNo.", HashMap<"datasetType", File[]>>>
     * @exception Exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, HashMap<String, HashMap<String, File[]>>> getEcosDatasetFiles(String[] ecos, String strDownloadPath, ArrayList<String> filterType) throws Exception {
        HashMap<String, HashMap<String, HashMap<String, File[]>>> ecoDatasetFiles = new HashMap<String, HashMap<String, HashMap<String, File[]>>>();
        if(ecos == null) {
            return null;
        }        
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
        DataSet ds = new DataSet();        
        for (int i = 0; i < ecos.length; i++) {
            ds.put("ecoNo", ecos[i]);
            // ECO DWG-B List를 조회
            ArrayList<SYMCECODwgData> rows = (ArrayList<SYMCECODwgData>) remote.execute("com.kgm.service.ECOHistoryService", "selectECODwgList", ds);
            // [다운로드 Path]/[ECO No.] 하위에 파일을 생성한다. 
            String ecoDownloadPath = strDownloadPath + "/" + ecos[i];
            File desti = new File(ecoDownloadPath);
            // 각 다운로드 ECO 디렉토리가 존재하지않으면 생성
            if(!desti.exists()) {
                desti.mkdirs();                
            }
            // ECO 하위 DWG의 Datset File Map
            // HashMap<"partNo./revNo.", HashMap<"datasetType", File[]>>
            HashMap<String, HashMap<String, File[]>> ecoDownloadFiles = new HashMap<String, HashMap<String, File[]>>();
            for (int j = 0; rows != null && j < rows.size(); j++) {
                SYMCECODwgData row = rows.get(j);                
                HashMap<String, File[]> ecoFiles = getDatasetFiles(row.getPartNo(), row.getRevisionNo(), filterType, ecoDownloadPath, "000".equals(row.getRevisionNo()) ? null : row.getEcoNo());
                if(ecoFiles != null) {
                    ecoDownloadFiles.put(row.getPartNo() + "/" + row.getRevisionNo(), ecoFiles);
                }
            }
            // ECO Mapping Map (return)
            // HashMap<"ecoNo.", HashMap<"partNo./revNo.", HashMap<"datasetType", File[]>>>
            ecoDatasetFiles.put(ecos[i], ecoDownloadFiles);
        }        
        return ecoDatasetFiles;
    }

    /**
     * Item, revision Id 정보를 가지고 Dataset하위의 파일을 가져온다.
     * 
     * @method getDatasetFiles
     * @date 2013. 2. 22.
     * @param String itemId, String revisionId, ArrayList<String> filterType, String strDownloadPath, String ecoNo
     * @return HashMap<String,File[]>
     * @exception
     * @throws Exception
     * @see
     */
    public static HashMap<String, File[]> getDatasetFiles(String itemId, String revisionId, ArrayList<String> filterType, String strDownloadPath, String ecoNo) throws Exception {
        if ("".equals(StringUtil.nullToString(itemId))) {
            return null;
        }
        if ("".equals(StringUtil.nullToString(revisionId))) {
            return null;
        }
        TCComponentItemRevision itemRevision = CustomUtil.findItemRevision("ItemRevision", itemId, revisionId);
        return getDatasetFiles(itemRevision, filterType, strDownloadPath, ecoNo);
    }

    /**
     * TCComponentItemRevision를 가지고 Dataset하위의 파일을 가져온다.
     * 
     * @method getDatasetFiles
     * @date 2013. 2. 22.
     * @param TCComponentItemRevision itemRevision, ArrayList<String> filterType, String strDownloadPath, String ecoNo
     * @return HashMap<String,File[]>
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    public static HashMap<String, File[]> getDatasetFiles(TCComponentItemRevision itemRevision, ArrayList<String> filterType, String strDownloadPath, String ecoNo) throws Exception {
        HashMap<String, File[]> datasetFiles = new HashMap<String, File[]>();
        Vector<TCComponentDataset> datasets = (Vector<TCComponentDataset>) CustomUtil.getDatasets(itemRevision, TcDefinition.TC_SPECIFICATION_RELATION, "All");
        for (int i = 0; datasets != null && i < datasets.size(); i++) {
            TCComponentDataset dataSet = datasets.get(i);
            // 필터 조건이 존재하면 필터에 존재하는 Dataset만 가져온다.
            if (filterType == null || (filterType != null && filterType.contains(dataSet.getType()))) {
                if (dataSet != null && ("".equals(StringUtil.nullToString(ecoNo)) || dataSet.getProperty("s7_ECO_NO").equals(ecoNo))) {
                    //TCComponentTcFile[] imanFile = dataSet.getTcFiles();
                    TCComponentTcFile[] imanFile = getImanFile(dataSet);
                    if (imanFile == null || imanFile.length == 0) {
                        datasetFiles.put(dataSet.getType(), null);
                    } else {
                        File[] files = null;
                        if (strDownloadPath == null) {
                            files = dataSet.getFiles(SYMTcUtil.getNamedRefType(dataSet, imanFile[0]));
                        } else {
                            files = dataSet.getFiles(SYMTcUtil.getNamedRefType(dataSet, imanFile[0]), strDownloadPath);
                        }
                        datasetFiles.put(dataSet.getType(), files);
                    }
                }
            }
        }
        return datasetFiles;
    }
    
    public static TCComponentTcFile[] getImanFile(TCComponentDataset dataSet) throws Exception {
        ArrayList<TCComponentTcFile> list = new ArrayList<TCComponentTcFile>();
        TCComponentTcFile[] imanFile = dataSet.getTcFiles();
        for (int i = 0; i < imanFile.length; i++) {
            if("CATDrawing".equals(dataSet.getType())) {            
                if("catdrawing".equals(SYMTcUtil.getNamedRefType(dataSet, imanFile[i]))) {
                    list.add(imanFile[i]);
                }
            } else if("CATPart".equals(dataSet.getType())) {
                if("catpart".equals(SYMTcUtil.getNamedRefType(dataSet, imanFile[i]))) {
                    list.add(imanFile[i]);
                }
            } else if("CATProduct".equals(dataSet.getType())) {
                if("catproduct".equals(SYMTcUtil.getNamedRefType(dataSet, imanFile[i]))) {
                    list.add(imanFile[i]);
                }
            } else {
                list.add(imanFile[i]);
            }
            
        }
        if(list.size() > 0) {
            return list.toArray(new TCComponentTcFile[list.size()]);
        } else {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public static HashMap<String, File[]> getDatasetFiles(TCComponentItemRevision rev, ArrayList<String> filterType, String strDownloadPath, Date releasedDate) throws Exception {
        HashMap<String, File[]> datasetFiles = new HashMap<String, File[]>();
        Vector<TCComponentDataset> datasets = (Vector<TCComponentDataset>) CustomUtil.getDatasets(rev, TcDefinition.TC_SPECIFICATION_RELATION, "All");
        for (int i = 0; datasets != null && i < datasets.size(); i++) {
            TCComponentDataset dataSet = datasets.get(i);
            // 필터 조건이 존재하면 필터에 존재하는 Dataset만 가져온다.
            if (filterType == null || (filterType != null && filterType.contains(dataSet.getType()))) {
                Date dsReleasedDate = dataSet.getDateProperty("date_released");
                if ((releasedDate == null && dsReleasedDate == null) || (releasedDate != null && releasedDate.equals(dsReleasedDate))) {
//                    TCComponentTcFile[] imanFile = dataSet.getTcFiles();
                    TCComponentTcFile[] imanFile = getImanFile(dataSet);
                    if (imanFile == null || imanFile.length == 0) {
                        datasetFiles.put(dataSet.getType(), null);
                    } else {
                        File[] files = null;
                        if (strDownloadPath == null) {
                            files = dataSet.getFiles(SYMTcUtil.getNamedRefType(dataSet, imanFile[0]));
                        } else {
                            files = dataSet.getFiles(SYMTcUtil.getNamedRefType(dataSet, imanFile[0]), strDownloadPath);
                        }
                        datasetFiles.put(dataSet.getType(), files);
                    }
                }
            }
        }
        return datasetFiles;
    }

    /**
     * TCComponent의 Owner를 변경한다.
     * 
     * @method changeOwner
     * @date 2013. 2. 27.
     * @param
     * @return void
     * @throws TCException
     * @exception
     * @throws
     * @see
     */
    public static void changeOwner(TCComponent component, String userId) throws TCException {
        TCSession session = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
        if (userId != null && !userId.equals("")) {
            TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
            TCComponentUser user = userType.find(userId);
            TCComponentGroup defaultGroup = (TCComponentGroup) user.getReferenceProperty("default_group");
            component.changeOwner(user, defaultGroup);
        }
    }
    
    public static  void selfRelease(TCComponentItemRevision revision, String procName) throws Exception
    {
      

      TCComponentTaskTemplate template = null;
      TCComponentTaskTemplateType imancomponenttasktemplatetype = (TCComponentTaskTemplateType) revision.getSession().getTypeComponent("EPMTaskTemplate");
      //TCComponentTaskTemplate[] tasktemplate = imancomponenttasktemplatetype.extentReadyTemplates(false);
      TCComponentTaskTemplate[] tasktemplate = imancomponenttasktemplatetype.getProcessTemplates(false, false, null, null, null);
      for (int j = 0; j < tasktemplate.length; j++) 
      {
        if (tasktemplate[j].toString().equals(procName)) 
        {
          template = tasktemplate[j];
          break;
        }
      }
    
      if(template == null)
        throw new Exception("Can't Find SelfRelease Process Template");
      
      // 자가결재 수행
      
      TCComponent[] aimancomponent = new TCComponent[]{revision};
      int a[] = new int[]{1};
      if( revision != null )
      {
        TCComponentProcessType processtype = (TCComponentProcessType) revision.getSession().getTypeComponent("Job");
        processtype.create("Self Release", "",template, aimancomponent, a);
      }
    }
    
    
    public static  void selfRelease(TCComponentItemRevision[] aimancomponent, String procName) throws Exception
    {
    	
      if( aimancomponent == null || aimancomponent.length == 0 )
      {
      	return;
      }
      

      TCComponentTaskTemplate template = null;
      TCComponentTaskTemplateType imancomponenttasktemplatetype = (TCComponentTaskTemplateType) aimancomponent[0].getSession().getTypeComponent("EPMTaskTemplate");
      //TCComponentTaskTemplate[] tasktemplate = imancomponenttasktemplatetype.extentReadyTemplates(false);
      TCComponentTaskTemplate[] tasktemplate = imancomponenttasktemplatetype.getProcessTemplates(false, false, null, null, null);
      
      for (int j = 0; j < tasktemplate.length; j++) 
      {
        if (tasktemplate[j].toString().equals(procName)) 
        {
          template = tasktemplate[j];
          break;
        }
      }
    
      if(template == null)
        throw new Exception("Can't Find SelfRelease Process Template");
      
      // 자가결재 수행
      
      //int a[] = new int[]{aimancomponent.length};

      TCComponentProcessType processtype = (TCComponentProcessType) aimancomponent[0].getSession().getTypeComponent("Job");
      processtype.create("Self Release", "",template, aimancomponent, ProcessUtil.getAttachTargetInt(aimancomponent));
    }
    
    
    public static TCComponent createApplicationObject(TCSession session, String strClassName, String[] szAttrs , String[] szValues)
    {
    	try
    	{
	        com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse response = null;
	    	DataManagementService dmService = DataManagementService.getService(session);
	      
	        com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput crInputData = new com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput();
	        crInputData.boName = strClassName;
	        
	        com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn cproperty = new com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn();
	        cproperty.clientId = "Create";
	        cproperty.data = crInputData;
	        
	        com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn props[] = new com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn[1];
	        props[0] = cproperty;        
	        
	        //[SR140723-028][20140522] swyoon createApplicationObject 메서드에서 동일 API를 2번 호출하던 Bug Fix.
//	        dmService.createObjects(props);
	        
	        response = dmService.createObjects(props);
	        
	        TCComponent[] newCreatedComps = null;
	        if(response.serviceData.sizeOfPartialErrors() == 0)
	        {
	          newCreatedComps = response.output[0].objects;
	          if (szAttrs != null)
	          {
    	          for( int i = 0 ; i < szAttrs.length ; i++)
    	          {
    	        	  newCreatedComps[0].setProperty(szAttrs[i], szValues[i]);
    	          }
	          }
	          
	          return newCreatedComps[0];
	        }
	        else
	        {
	          throw new Exception("Errors : Create");
	        }
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    	}
        
    	return null;
//    	
//    	dmService.create
//      com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput crInput = new com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput();
//      crInput.boName = strClassName;
//      TCComponent usageDataObj = getInstance(m_instance).createComponent(crInput);
//      try
//      {
//        usageDataObj.lock();
//        //"이름", "CM/INCH", "패턴", "제품", "LowerLimit", "UpperLimit", "완성"
//        usageDataObj.setProperty("i0POMName", strValues[0]);
//        usageDataObj.setProperty("i0Cm_Inch", strValues[1]);
//        usageDataObj.setProperty("i0SizeValue1", strValues[2]);
//        usageDataObj.setProperty("i0SizeValue2", strValues[3]);
//        usageDataObj.setProperty("i0SizeValue3", strValues[6]);
//        usageDataObj.setProperty("i0Size", strValues[7]); //기준 Size
//        
//        usageDataObj.save();
//        usageDataObj.unlock();
//      }catch(TCException tce)
//      {
//        tce.printStackTrace();
//        return null;
//      }
//      
//      return usageDataObj;
        
        
    }
    
    
    public static void setShownOnPart(TCComponentItemRevision targetRev, TCComponentItemRevision shownOnRev) throws TCException
    {

    	TCComponent[] comps  = targetRev.getRelatedComponents();
        for (int j = 0; j < comps.length; j++)
        {
          TCComponent comp = comps[j];
          if( comp.getType().equals("DirectModel") )
          { 
        	  targetRev.remove("IMAN_Rendering", comp);
          }
          if( comp.getType().equals("catia") || comp.getType().equals("CATDrawing") )
          { 
        	  targetRev.remove("IMAN_specification", comp);
          }
        }
    	
        TCComponent[] components  = shownOnRev.getRelatedComponents();
        
        for (int j = 0; j < components.length; j++)
        {
          TCComponent comp = (TCComponent) components[j];
          
          // JT는 Object Copy
          if( comp.getType().equals("DirectModel") )
          {          
	          String dataSetName = targetRev.getProperty("item_id") + "/" + targetRev.getProperty("item_revision_id");
	          TCComponentDataset revDataSet = ((TCComponentDataset)comp).saveAs(dataSetName);
	          targetRev.add("IMAN_Rendering", revDataSet);
          }
          
          // 2D는 Reference Copy
          if( comp.getType().equals("catia") || comp.getType().equals("CATDrawing") )
          {          
        	  targetRev.add("IMAN_specification", comp);
          }
        }
    }
    
    public static String getNewID(String preFix, int entire_len){
    	
    	String newId = null;
    	SYMCRemoteUtil remote = new SYMCRemoteUtil();
		try{
			DataSet ds = new DataSet();
			ds.put("PRE_FIX", preFix);
			ds.put("TO_LEN", entire_len);
			newId = (String)remote.execute("com.kgm.service.VariantService", "getNewId", ds);
		}catch( Exception e){
			e.printStackTrace();
		}
		
		return newId;
    }
    
    /**
     * SOA를 통한 Item 생성
     * @param session TC Session
     * @param boName BO Name
     * @param itemPropMap Item 속성 정보
     * @param itemRevisionPropMap Item Revision 속성 정보
     * @return
     */
    public static TCComponent createItemObject(TCSession session, String boName, Map<String,String> itemPropMap, Map<String,String> itemRevisionPropMap) {
        try {
            CreateResponse response = null;
            DataManagementService dmService = DataManagementService.getService(session);

            CreateInput itemInputData = new CreateInput();
            itemInputData.boName = boName;
            itemInputData.stringProps = itemPropMap;
            
            //Revision Compound Inputs
            Map<String,CreateInput[]> compoundCreateInput = new HashMap<String, DataManagement.CreateInput[]>();
            
            CreateInput[] revisionCreateInputs = new CreateInput[1];
            CreateInput revisionCreateInput = new CreateInput();
            revisionCreateInput.boName=boName.concat("Revision");
            revisionCreateInput.stringProps = itemRevisionPropMap;
           
            revisionCreateInputs[0] = revisionCreateInput;
            
            compoundCreateInput.put("revision", revisionCreateInputs);
            //Revision Property 속성 입력
            itemInputData.compoundCreateInput = compoundCreateInput;

            CreateIn createInput = new CreateIn();
            createInput.clientId = "Create";
            createInput.data = itemInputData;

            CreateIn creatInputs[] = new CreateIn[1];
            creatInputs[0] = createInput;

            response = dmService.createObjects(creatInputs);

            TCComponent[] newCreatedComps = null;
            if (response.serviceData.sizeOfPartialErrors() == 0) {
                newCreatedComps = response.output[0].objects;
                return newCreatedComps[0];
            } else {
				throw new Exception("Error occurred during item creation");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
}
