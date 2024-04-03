package com.symc.plm.rac.prebom.common.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.im.InputContext;
import java.io.File;
import java.io.FileInputStream;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JTable;

import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.widgets.Control;

import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.CommonConstant;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.SOAGenericCreateHelper;
import com.teamcenter.rac.kernel.IPropertyName;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevisionType;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentChangeType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentEngineeringChange;
import com.teamcenter.rac.kernel.TCComponentEngineeringChangeType;
import com.teamcenter.rac.kernel.TCComponentEnvelope;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentFolderType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupMemberType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentProcessType;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentTcId;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentViewType;
import com.teamcenter.rac.kernel.TCDateEncoder;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCReservationService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCTextService;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;
//import org.apache.commons.collections.CollectionUtils;

/**
 * Common Utility Class
 * [SR140616-019][20140619][bskwak] 비정규 품번 Concept 단계 품번생성 오류 개선 : Stage 구분 없이 모두 기본 revision 초기값 사용하는 것으로 변경 함.
 * => 영문 Rev이 아닌 숫자 rev을 사용하는 logic으로 대체.
 * [SR140324-030][20140625] KOG DEV Veh. Part Revise 할 때 기존 Typed Reference Object에 SES Spec No. 값 복사.
 * [SR141106-036][2014.11.17][jclee] Part Revise 시 Change Description 공란처리
 * [20161014][ymjang] 변경사유 누락 수정
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class SDVPreBOMUtilities {
	private static TCComponentDatasetType datasetType;
	
    public static int GROUP_LEADER = 0;
    public static int GROUP_MEMBER = 0;

    public static String CATETORY_EXPORT = "Export";
    public static String CATETORY_DATASET = "Dataset";

    public static final String NA_RENAME_FILE = "@@N/A/F@@";

    /**
     * Title: Item ID로 Item을 찾음<br>
     * Usage: findItem("Document","00001") *
     * 
     * @param compType
     *            ComponentType
     * @param itemId
     *            Item Id
     * @return item
     * @throws TCException
     */
    public static TCComponentItem findItem(String compType, String itemId) throws TCException {
        TCSession session = getTCSession();
        TCComponentItem item = null;
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        //item = itemType.find(itemId);
        TCComponentItem[] items = itemType.findItems(itemId);
        if(items != null && items.length > 0) {
        	item = items[0];
        }
        
        return item;
    }

    /**
     * 이미 만들어져 있는 Saved query를 이용하여 imancomponent를 검색하는 method이다.
     * 
     * @param savedQueryName
     *            String 저장된 query name
     * @param entryName
     *            String[] 검색 조건 name(오리지날 name)
     * @param entryValue
     *            String[] 검색 조건 value
     * @return TCComponent[] 검색 결과
     * @throws Exception
     * 
     */
    public static TCComponent[] queryComponent(String savedQueryName, String[] entryName, String[] entryValue) throws Exception {
        TCSession session = getTCSession();
        //session.getPreferenceService().setString(1, "QRY_dataset_display_option", "2");
        session.getPreferenceService().setStringValue("QRY_dataset_display_option", "2");
        
        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(savedQueryName);
        String[] queryEntries = session.getTextService().getTextValues(entryName);
        for (int i = 0; queryEntries != null && i < queryEntries.length; i++) {
            if (queryEntries[i] == null || queryEntries[i].equals("")) {
                queryEntries[i] = entryName[i];
            }
        }
        return query.execute(queryEntries, entryValue);
    }

    /**
     * 검색 결과 개수를 리턴한다.
     * 
     * @param savedQueryName
     *            String
     * @param entryName
     *            String[]
     * @param entryValue
     *            String[]
     * @return int
     * @throws Exception
     */
    public static int queryCount(String savedQueryName, String[] entryName, String[] entryValue) throws Exception {
        TCSession session = getTCSession();
        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(session.getTextService().getTextValue(savedQueryName));
        return query.count(session.getTextService().getTextValues(entryName), entryValue);
    }

    /**
     * 팀센타 쿼리를 이용하여 폴더 배열을 리턴한다.
     * 
     * @Copyright : S-PALM
     * @author : 이정건
     * @since : 2011. 8. 29.
     * @param folderName
     * @return TCComponentFolder[]
     * @throws TCException
     */
    public static TCComponentFolder[] findFolder(String folderName) throws TCException {
        return findFolder(folderName, "", "");
    }

    public static TCComponentFolder[] findFolder(String folderName, String folderType) throws TCException {
        return findFolder(folderName, folderType, "");
    }

    public static TCComponentFolder[] findFolder(String folderName, String folderType, String owningUser) throws TCException {
        TCSession session = getTCSession();
        TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
        TCComponentQuery query = (TCComponentQuery) queryType.find(getTextServerString(session, "k_find_general_name"));
        String entryNames[] = { getTextServerString(session, "Name"), getTextServerString(session, "Type"), getTextServerString(session, "OwningUser") };
        String entryValues[] = { folderName, folderType, owningUser };
        com.teamcenter.rac.kernel.TCComponent queryResults[] = query.execute(entryNames, entryValues);
        ArrayList folders = new ArrayList();
        for (int i = 0; i < queryResults.length; i++)
            if (queryResults[i] instanceof TCComponentFolder)
                folders.add((TCComponentFolder) queryResults[i]);

        return (TCComponentFolder[]) folders.toArray(new TCComponentFolder[folders.size()]);
    }

    /**
     * Title: Item ID와 Revision ID로 Item Revision을 찾음<br>
     * Usage: findItemRevision("DocumentRevision","00001","A")
     * 
     * @param compType
     *            Component Type
     * @param itemId
     *            Item ID
     * @param revisionId
     *            ItemRevision Id
     * @return itemRevision
     * @throws Exception
     */
    public static TCComponentItemRevision findItemRevision(String compType, String itemId, String revisionId) throws Exception {
        TCSession session = getTCSession();
        TCComponentItemRevision itemRevision = null;
        TCComponentItemRevisionType itemRevisionType = (TCComponentItemRevisionType) session.getTypeComponent(compType);
        if (itemRevisionType != null) {
            //itemRevision = itemRevisionType.findRevision(itemId, revisionId);           
            TCComponentItemRevision[] itemRevisions = itemRevisionType.findRevisions(itemId, revisionId);
            if(itemRevisions != null && itemRevisions.length > 0) {
            	itemRevision = itemRevisions[0];
            }
        }
        return itemRevision;
    }

    /**
     * Title: Item ID와 Revision ID로 Item Revision을 찾음<br>
     * Usage: findItemRevision("EngChange","00001") *
     * 
     * @param itemTypeName
     * @param itemId
     * @return itemRevision
     * @throws TCException
     */
    public static TCComponentItemRevision findLatestItemRevision(String itemTypeName, String itemId) throws TCException {
        TCSession session = getTCSession();
        TCComponentItemRevision itemRevision = null;
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(itemTypeName);
        //TCComponentItem item = itemType.find(itemId);
//        if (item != null) {
//        	itemRevision = item.getLatestItemRevision();
//        }
        
        TCComponentItem[] items = itemType.findItems(itemId);
        if(items != null && items.length > 0) {
        	itemRevision = items[0].getLatestItemRevision();
        }
        
        return itemRevision;
    }

    /**
     * Title: Item을 생성함<br>
     * Usage: createItem("Document", "00001", "A","DOC NAME","Description")
     * 
     * @param compType
     *            Component Type ex)"Document"
     * @param itemId
     *            Item ID ex)"itemID"
     * @param revision
     *            Item Revision ex)"A"
     * @param itemName
     *            Item Object Name ex)"Document NAME"
     * @param desc
     *            Item Description ex) "Description"
     * @return item
     * @throws TCException
     */
    public static TCComponentItem createItem(String compType, String itemId, String revision, String itemName, String desc) throws TCException {
        TCSession session = getTCSession();
        TCComponentItem item = null;
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        item = itemType.create(itemId, revision, compType, itemName, desc, null);
        return item;
    }

//    public static TCComponentItem createItem(String compType, String itemId, String revision, String itemName, String desc, String strUOM) throws TCException {
//        TCSession session = getTCSession();
//
//        LOVUIComponent uomLovComboBox = new LOVUIComponent(session, "Unit of Measures");
//        uomLovComboBox.setSelectedValue(strUOM);
//        Object uomObj = uomLovComboBox.getSelectedObject();
//
//        TCComponentItem item = null;
//        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
//        item = itemType.create(itemId, revision, compType, itemName, desc, (TCComponent) uomObj);
//        return item;
//    }

    /**
     * Title: 특정 Component Type의 Next Revision ID를 가져온다.<br>
     * Desc : Item이 null이 아닐 경우 Item의 다음 Revision ID를 가져오고,null일 경우 Component의 초기 Revision ID를 가져옴<br>
     * Usage: getNextRevID(item,"Document");
     * 
     * @param item
     *            Item or null
     * @param compType
     *            Compoent Type
     * @return newRevId Item의 다음 revision ID
     */
    public static String getNextRevID(TCComponentItem item, String compType) throws Exception {
        TCSession session = getTCSession();
        String newRevId = "";
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        newRevId = itemType.getNewRev(item);
        return newRevId;
    }

    /**
     * Vehicle Part/Function Master Revision ID 생성
     * Stage 값이 "P"가 아닌경우 해당
     * 
     * A -> Z -> AA -> ZZ
     */
    public static String getNextCustomRevID(String currentRevID) throws Exception {

        if (currentRevID == null || currentRevID.trim().equals("") || currentRevID.length() > 2)
            throw new Exception("올바르지 않은 Revision ID 입니다.");

        char[] szChar = currentRevID.toCharArray();

        for (int i = (szChar.length - 1); i >= 0; i--) {
            if (('A' > szChar[i]) || (szChar[i] > 'Z')) {
                throw new Exception("올바르지 않은 Revision ID 입니다.");
            }
        }

        if ("ZZ".equals(currentRevID))
            throw new Exception("더 이상 개정하실 수 없습니다.");

        for (int i = (szChar.length - 1); i >= 0; i--) {

            if ((szChar[i] + 1) > 'Z') {
                if (i == 0) {
                    szChar[i] = 'A';
                    return "A" + new String(szChar);
                }
                szChar[i] = 'A';
                continue;
            } else {
                szChar[i] = (char) (szChar[i] + 1);
                break;
            }

        }
        return new String(szChar);

    }

    /**
     * Title: 특정 Component Type의 Next Item ID를 가져온다. <br>
     * Usage: getNextItemId("Document")
     * 
     * @param compType
     *            Compoent Type
     * @return nextItemId
     */
    public static String getNextItemId(String compType) throws Exception {
        TCSession session = getTCSession();
        String nextItemId = "";
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        nextItemId = itemType.getNewID();
        return nextItemId;
    }

    /**
     * Title: Item Revision의 이전 Revision을 가져온다. <br>
     * Usage: getPreviousRevision(itemRevision)
     * 
     * @param itemRevision
     * @return previousRevision
     * @throws Exception
     */
    public static TCComponentItemRevision getPreviousRevision(TCComponentItemRevision itemRevision) throws Exception {
        TCComponentItemRevision previousRevision = null;
        TCComponent[] revisionList = itemRevision.getItem().getRelatedComponents("revision_list");
        if (revisionList.length > 1) {
            for (int inx = 0; inx < revisionList.length; inx++) {
                TCComponentItemRevision revision = (TCComponentItemRevision) revisionList[inx];
                // user가 선택한 revision이 first revision인 경우 이전 revision은 null이된다.
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
     * Title: Item의 Master Form을 가져옴<br>
     * Usage: getItemMasterForm(item)
     * 
     * @param item
     * @return itemMasterForm
     */
    public static TCComponentForm getItemMasterForm(TCComponentItem item) throws TCException {
        TCComponent itemMasterForm = null;
        itemMasterForm = item.getRelatedComponent("IMAN_master_form");
        return (TCComponentForm) itemMasterForm;
    }

    /**
     * Title: Item의 Revision Master Form을 가져옴<br>
     * Usage: getItemRevMasterForm(itemRev)
     * 
     * @param itemRev
     * @return itemRevMasterForm
     */
    public static TCComponentForm getItemRevMasterForm(TCComponentItemRevision itemRev) throws TCException {
        TCComponent itemRevMasterForm = null;
        itemRevMasterForm = itemRev.getRelatedComponent("IMAN_master_form_rev");
        return (TCComponentForm) itemRevMasterForm;
    }

    /**
     * Title: Item의 Latest Revision Master Form을 가져옴<br>
     * Usage: getLatestRevMasterForm(item)
     * 
     * @param item
     * @return itemRevMasterForm
     */
    public static TCComponentForm getLatestRevMasterForm(TCComponentItem item) throws Exception {
        TCComponent itemRevMasterForm = null;
        itemRevMasterForm = item.getLatestItemRevision().getRelatedComponent("IMAN_master_form_rev");
        return (TCComponentForm) itemRevMasterForm;
    }

    /**
     * Title: Item Revision 아래의 DataSet을 가져옴<br>
     * Usage: getDatasets(itemRevision,"IMAN_specification","MSExcel")
     * 
     * @param itemRevision
     * @param relationType
     * @param dataType
     *            dataType이 "All"이면 모든 dataSet을 가져옴
     * @return datasets
     */
    public static Vector getDatasets(TCComponent itemRevision, String relationType, String dataType) throws Exception {
        Vector<TCComponentDataset> datasets = new Vector<TCComponentDataset>();
        TCComponentDataset tmpdataset = null;
        if (itemRevision == null) {
            return null;
        }
        TCComponent[] tmpComponent = itemRevision.getRelatedComponents(relationType);
        for (int i = 0; i < tmpComponent.length; i++) {
            if (tmpComponent[i] instanceof TCComponentDataset) {
                tmpdataset = (TCComponentDataset) tmpComponent[i];

                if (!dataType.equalsIgnoreCase("All")) {
                    if (tmpdataset.getType().equalsIgnoreCase(dataType)) {
                        if (datasets == null) {
                            datasets = new Vector<TCComponentDataset>();
                        }
                        datasets.addElement(tmpdataset);
                    }
                } else {
                    if (datasets == null) {
                        datasets = new Vector<TCComponentDataset>();
                    }
                    datasets.addElement(tmpdataset);
                }
            }
        }
        return datasets;
    }

    /**
     * 파일을 읽어 byte array로 변환시켜주는 method이다.
     * 
     * @param file
     *            File
     * @return byte[]
     * @throws Exception
     */
    public static byte[] getFile(File file) throws Exception {
        byte[] buffer = new byte[512];
        byte[] fis = new byte[(int) file.length()];
        int c, i = 0;

        FileInputStream file_in = new FileInputStream(file);

        while ((c = file_in.read(buffer)) != -1) {
            System.arraycopy(buffer, 0, fis, i, c);
            i = i + c;
        }

        file_in.close();

        return fis;
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
            TCComponentUser TCComponentuser = (TCComponentUser) (components.getReferenceProperty("owning_user"));
            if (session.getUser() != TCComponentuser) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * 결재 관련된 object를 모두 제외하고 상위에 붙어 있는 object들을 찾아 개수를 리턴한다.
     * 
     * @param component
     *            TCComponent
     * @return int
     * @throws Exception
     */
    public static TCComponent[] getWhereReferenceExceptProcess(TCComponent component) throws Exception {
        Vector<InterfaceAIFComponent> tmp = new Vector<InterfaceAIFComponent>();
        AIFComponentContext[] whereContext = component.whereReferenced();
        for (int i = 0; i < whereContext.length; i++) {
            if (whereContext[i].getComponent() instanceof TCComponentEnvelope || whereContext[i].getComponent() instanceof TCComponentTask || whereContext[i].getComponent() instanceof TCComponentProcess) {
                continue;
            }
            tmp.addElement(whereContext[i].getComponent());
        }
        TCComponent[] returnComponent = new TCComponent[tmp.size()];
        for (int i = 0; i < tmp.size(); i++) {
            returnComponent[i] = (TCComponent) tmp.elementAt(i);
        }
        return returnComponent;
    }

    /**
     * Title: Component가 Working Status인지 Check함 Usage:
     * [SR 없음][20141229][jclee] Component가 In Work 상태이고 Workflow의 Process Stage가 Creator일 경우 Working Status를 true로 인식하는 로직 추가
     * 
     * @param components
     * @return boolean Component가 Working이면 true, 그렇지 않으면 false
     */
    public static boolean isWorkingStatus(TCComponent components) throws TCException {
        components.refresh();
        
        String sMaturity = components.getProperty("s7_MATURITY");
        String sReleaseStatusList = components.getProperty("release_status_list");
	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체 
//        String sProcessStageList = components.getProperty("process_stage_list");
        String sProcessStageList = components.getProperty("fnd0StartedWorkflowTasks");
        String sProcessStage = "";
	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체 
//        TCComponent[] process_stage_list = components.getReferenceListProperty("process_stage_list");
        TCComponent[] process_stage_list = components.getReferenceListProperty("fnd0StartedWorkflowTasks");
        
        // 1. Release되어있지 않고 Workflow도 없는 경우
        if (sReleaseStatusList.equalsIgnoreCase("") && sProcessStageList.equalsIgnoreCase("")) {
			return true;
		}
        
        // 2. Workflow가 존재하지만 Part의 Maturity가 In Work이고 Workflow의 Process Stage가 Creator인 경우. (Workflow "Reject")
        if (process_stage_list.length != 0) {
        	for (int inx = 0; inx < process_stage_list.length; inx++) {
        		if (process_stage_list[inx].getType().equals("EPMReviewTask")) {
        			sProcessStage = process_stage_list[inx].getProperty("current_name");
        		}
        	}
        	
        	// 2.1. Part의 Maturity가 In Work이고 Workflow의 Process Stage가 Creator인 경우
        	if (sMaturity.equalsIgnoreCase("In Work") && sProcessStage.equalsIgnoreCase("Creator")) {
        		return true;
        	}
		}
        
        return false;
    }

    /**
     * component가 결재중인지 아닌지를 판단한다.
     * 
     * @param components
     *            TCComponent
     * @return boolean
     * @throws TCException
     */
    public static boolean isInProcess(TCComponent components) throws TCException {
        // components.refresh();
	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체 
//        if (components.getProperty("release_status_list").equalsIgnoreCase("") && !components.getProperty("process_stage_list").equalsIgnoreCase("")) {
    	if (components.getProperty("release_status_list").equalsIgnoreCase("") && !components.getProperty("fnd0StartedWorkflowTasks").equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }

    /**
     * Component가 결재 완료인지 아닌지를 판단한다.
     * 
     * @param components
     *            TCComponent
     * @return boolean 결재 완료이면 true를 리턴 그렇지 않으면 false를 리턴한다.
     * @throws Exception
     */
    public static boolean isReleased(TCComponent components) throws TCException {
        components.refresh();
        if (!components.getProperty("release_status_list").equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }

    /**
     * 입력된 component가 결재 거부된 상태인지를 체크한다.
     * 
     * @param component
     *            TCComponent
     * @return boolean
     * @throws TCException
     */
    public static boolean isRejected(TCComponent component) throws TCException {
        AIFComponentContext[] aifComp = component.whereReferenced();
        for (int i = 0; i < (aifComp == null ? 0 : aifComp.length); i++) {
            if (aifComp[i].getComponent() instanceof TCComponentProcess) {
                return isRejected((TCComponentProcess) aifComp[i].getComponent());
            }
        }
        return false;
    }

    /**
     * Process가 거부되었는지 아닌지를 판단한다.
     * 
     * @param wfProcess
     *            TCComponentProcess
     * @return boolean 결재 거부이면 true를 리턴 그렇지 않으면 false를 리턴한다.
     * @throws Exception
     */
    public static boolean isRejected(TCComponentProcess wfProcess) throws TCException {
        boolean isRejected = false;
        TCComponentTask rootTask = null;
        TCComponentTask[] levelTasks = null;

        try {
            if (wfProcess == null) {
                return false;
            }

            wfProcess.refresh();
            rootTask = wfProcess.getRootTask();
            rootTask.refresh();
            levelTasks = rootTask.getSubtasks();

            for (int i = 0; i < levelTasks.length; i++) {
                levelTasks[i].refresh();
                // System.out.println(levelTasks[i].getName()+":"+levelTasks[i].getTaskState());
                if (levelTasks[i].getName().equals("Submit") && (levelTasks[i].getTaskState().equals("Started") || levelTasks[i].getTaskState().equals("시작됨"))) {
                    isRejected = true;
                    break;
                }
            } // for
        } catch (TCException ex) {
            ex.printStackTrace();
            return false;
        }
        return isRejected;
    }

    /**
     * Title: Creating and pasting Engineering Change <br>
     * Usage: createEngChange(session,targetComp,"00001","A","Equipment","Equipment Problem",
     * "Description")
     * 
     * @param session
     * @param targetComp
     *            선택한 component
     * @param itemId
     *            Item ID
     * @param revId
     *            revision ID
     * @param itemName
     *            item Name
     * @param ecType
     *            EC Type
     * @param description
     * @return newEC
     */
    public static TCComponentEngineeringChange createEngChange(TCComponent targetComp, String itemId, String revId, String itemName, String ecType, String description) throws Exception {
        TCSession session = getTCSession();
        TCComponentEngineeringChange newEC = null;
        TCComponentEngineeringChangeType engChangeType = null;
        engChangeType = (TCComponentEngineeringChangeType) session.getTypeComponent("EngineeringChange");
        // EngineeringChange 생성
        newEC = engChangeType.create(itemId, revId, itemName, ecType, description);
        // ecType설정
        newEC.setECType(ecType);

        return newEC;
    }

    /**
     * Title: EngChange Revision에 EngForm을 붙인다.<br>
     * Usage: attachECFormToEngChange(engChange,forms)
     * 
     * @param engChange
     * @param forms
     */
    public static void attachECFormToEngChange(TCComponentEngineeringChange engChange, Vector forms) throws Exception {
        TCComponentItemRevision engChangeRevision = engChange.getIR();
        if (forms != null && forms.size() > 0) {
            // 문제점일 경우 formRelation이 IMAN_specification이기에 고정값을 주었다.
            engChangeRevision.insertRelated("IMAN_specification", forms, 0);
        }
    }

    /**
     * Title: 특정 Form Type에 해당하는 EngChange Form을 생성한다.<br>
     * Usage: createECFormByFormType(session,
     * "00001","A","Equipment Problem","Description","Equipment Problem Form")
     * 
     * @param session
     * @param changeId
     *            ex)"changeId"
     * @param revId
     *            ex)"A"
     * @param ECType
     *            ex)"Equipment Change"
     * @param formDesc
     *            ex)"description"
     * @param formType
     *            ex)"Equipment Problem Form"
     * @return createECForm
     */
    public static TCComponentForm createECFormByFormType(String changeId, String revId, String ECType, String formDesc, String formType) throws Exception {
        TCSession session = getTCSession();
        TCComponent ecComponents[]; // Change Components
        TCComponent ecFormComp[]; // Change Component의 Form Components
        TCComponentForm createECForm = null; // 생성될 Change Form
        TCComponentFormType createFormType = null;
        String as[] = null; // change Type의 이름
        TCComponentType compType = session.getTypeComponent("ChangeTypeData");
        ecComponents = compType.extent();
        as = new String[ecComponents.length];
        for (int i = 0; i < ecComponents.length; i++) {
            TCComponentChangeType changeType = (TCComponentChangeType) ecComponents[i];
            as[i] = changeType.askName();
            if (ECType.equalsIgnoreCase(as[i])) { // Change의 정의된 form type을 가져온다.
                ecFormComp = changeType.getTCProperty("form_types").getReferenceValueArray();
                for (int j = 0; j < ecFormComp.length; j++) {
                    if (ecFormComp[j].toString().equals(formType)) {
                        // Change Form 이름을 구성
                        String ecFormCompName = changeId + "/" + revId + "-" + formType;
                        ecFormCompName = ecFormCompName.getBytes().length <= 32 ? ecFormCompName : new String(ecFormCompName.getBytes(), 0, 32);
                        // Change Form 생성
                        createFormType = (TCComponentFormType) session.getTypeComponent("Form");
                        createECForm = createFormType.create(ecFormCompName, formDesc, formType);
                        break;
                    }
                }
            }
        }
        return createECForm;
    }

    /**
     * 일반 form을 생성하는 method이다.
     * 
     * @param formName
     *            String form의 이름.
     * @param formDesc
     *            String form의 설명.
     * @param formType
     *            String form의 type.
     * @return TCComponentForm
     * @throws Exception
     */
    public static TCComponentForm createTCComponentForm(String formName, String formDesc, String formType) throws Exception {
        TCComponentFormType TCComponentformtype = (TCComponentFormType) getTCSession().getTypeComponent(formType);
        return (TCComponentForm) TCComponentformtype.create(formName, formDesc, formType);
    }

    /**
     * Folder를 생성하는 Method 이다
     * 
     * @param folderName
     *            String
     * @param folderDesc
     *            String
     * @param folderType
     *            String
     * @return TCComponentFolder
     * @throws Exception
     */
    public static TCComponentFolder createFolder(String folderName, String folderDesc, String folderType) throws Exception {
        TCComponentFolderType TCComponentfoldertype = (TCComponentFolderType) getTCSession().getTypeComponent(folderType);
        return (TCComponentFolder) TCComponentfoldertype.create(folderName, folderDesc, folderType);
    }

    /**
     * Title: 자동 증가된 EngChange ID를 가져오는 메소드 <br>
     * Usage: getEngChangeIDByECType(session, "Equipment Problem")
     * 
     * @param session
     * @param ECType
     *            "Equipment Change"
     * @return changeId
     */
    public static String getEngChangeIDByECType(String ECType) throws Exception {
        TCSession session = getTCSession();
        TCComponent ecComponents[]; // Change Components
        String as[] = null; // change Type의 이름
        String changeId = "";
        // 정의 된 Change Type을 가져온다.
        TCComponentType compType = session.getTypeComponent("ChangeTypeData");
        ecComponents = compType.extent();
        as = new String[ecComponents.length];
        for (int i = 0; i < ecComponents.length; i++) {
            TCComponentChangeType changeType = (TCComponentChangeType) ecComponents[i];
            as[i] = changeType.askName();
            if (ECType.equalsIgnoreCase(as[i])) {
                ECType = as[i];
                if (changeType != null) {
                    TCComponentTcId compId = (TCComponentTcId) changeType.getTCProperty("id_format").getReferenceValue();
                    // EngChange ID값 한단계 자동증가
                    if (compId != null) {
                        changeId = compId.getNextValue();
                    }
                }
                break;
            }
        }
        return changeId;
    }

    /**
     * TCComponent가 check out 상태인지 check 한다.<br>
     * item, revision, form, dataset...등등등.. 모든 component들이 가능.
     * 
     * @param components
     *            TCComponent chek하고자 하는 component.
     * @return boolean checkout되어 있으면 true를 return.
     * @throws Exception
     */
    public static boolean isTargetCheckOut(TCComponent components) throws Exception {
        components.refresh();
        TCSession TCSession = (TCSession) components.getSession();
        TCReservationService imanreservationservice = TCSession.getReservationService();
        if (imanreservationservice.isReserved(components)) {
            return true;
        }
        return false;
    }

    /**
     * Dataset을 생성하고 target에 특정 releation으로 붙인다.
     * 
     * @param session
     *            TCSession
     * @param targetComp
     *            TCComponent 붙이고자하는 대상
     * @param datasetName
     *            String dataset의 이름
     * @param description
     *            String dataset의 설명
     * @param datasetType
     *            String dataset의 Type
     * @param relation
     *            String target에 붙일때의 관계
     * @return TCComponentDataset 생성된 Dataset
     * @throws Exception
     */
    public static TCComponentDataset createPasteDataset(TCComponent targetComp, String datasetName, String description, String datasetType, String relation) throws Exception {
        TCSession session = targetComp.getSession();
        TCComponentDataset newDataset = null;
        String s = null;
        try {
            TCPreferenceService imanpreferenceservice = session.getPreferenceService();
            String s1 = "IMAN_" + datasetType + "_Tool";
            //s = imanpreferenceservice.getString(0, s1);
            s = imanpreferenceservice.getStringValue(s1);
        } catch (Exception ex) {
            s = null;
        }
        try {
            TCComponentDatasetType TCComponentdatasettype = (TCComponentDatasetType) session.getTypeComponent(datasetType);
            newDataset = TCComponentdatasettype.create(datasetName, description, null, null, datasetType, s);
            if (targetComp != null) {
                targetComp.add(relation, (TCComponent) newDataset);
            }
        } catch (Exception TCException) {
            TCException.printStackTrace();
            throw TCException;
        }
        return newDataset;
    }

    /**
     * Dataset 내의 named reference들의 original file name을 변경한다.<br>
     * 변경은 reference name단위로 일괄 변경되므로 반드시 주의 해야 한다.<br>
     * 여러개의 named reference가 있을 경우에는 반드시 주의하여 사용하여야 한다.
     * 
     * @param dataset
     *            TCComponentDataset 변경하고자 하는 dataset
     * @param ref_names
     *            String 변경하고자 하는 reference type name
     * @param originalFileName
     *            String 변경할 이름. 예) aaa.txt
     * @throws Exception
     * @see getNamedRefType(TCComponentDataset datasetComponent, TCComponent TCComponent) throws
     *      Exception
     */
    public static void changeNamedRef(TCComponentDataset dataset, String ref_names, String originalFileName) throws Exception {
        TCComponentTcFile[] imanFiles = dataset.getTcFiles();
        for (int i = 0; i < imanFiles.length; i++) {
            String referenceName = getNamedRefType(dataset, imanFiles[i]);
            if (referenceName.equalsIgnoreCase(ref_names)) {
                imanFiles[i].setOriginalFileName(dataset, originalFileName);
            }
        }
    }

    /**
     * 금액에 콤마를 삽입하고 앞에 ￦를 붙인다.
     * 
     * @param amt
     *            변환할 금액
     * @param dec
     *            소수자리수
     * @return
     * 
     *         <p>
     * 
     *         <pre>
     *  - 사용 예
     *        String date = NumberUtil.getCurrencyNationNumber(123456.123, 3)
     *  결과 : ￦123,456.123
     * </pre>
     */
    public static String getCurrencyNationNumber(double amt, int dec) {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setMaximumFractionDigits(dec);
        return nf.format(amt);
    }

    /**
     * 이 method는 dataset의 실제 파일을 원하는 디렉터리에 내려 받는 method이다.<br>
     * 단, 주의할 점은 이 부분은 dataset내의 하나의 파일만을 내려받도록 되어 있으며 파일이 없는 경우 Exception이 발생한다.<br>
     * 하나 이상인 경우 어느것이 내려받아질지 알수 없다. 주의하도록 한다.
     * 
     * @param dataset
     *            TCComponentDataset
     * @param export_subDir
     *            String
     * @return File
     * @throws Exception
     * @see getNamedRefType(TCComponentDataset datasetComponent, TCComponent TCComponent) throws
     *      Exception
     */
    // public static File exportDataset(TCComponentDataset dataset, String export_subDir) throws
    // Exception
    // {
    // Registry registry = Registry.getRegistry("client_specific");
    // String export_dir = registry.getString("IMANExportDir") + File.separator + export_subDir;
    // File folder = new File(export_dir);
    // folder.mkdirs();
    // if(!folder.exists())
    // {
    // throw new Exception("Export folder not found.");
    // }
    // TCComponentImanFile[] imanFile = dataset.getImanFiles();
    // String fileName = imanFile[0].getProperty("original_file_name");
    // File file = dataset.getFile(getNamedRefType(dataset, imanFile[0]), fileName, export_dir);
    // return file;
    // }

    /**
     * 이 method는 dataset안에 있는 모든 file을 내려받는 method이다. Leehonghee[2/8] Add if (imanFile.length > 0 )
     * 를 추가하여 dataset.getFiles(...) 에서 발생하는 Null Point Dialog를 방지한다.
     * 
     * @param dataset
     *            TCComponentDataset
     * @param export_subDir
     *            String
     * @return File[]
     * @throws Exception
     * 
     */
    public static File[] exportDataset(TCComponentDataset dataset, String export_subDir) throws Exception {
        Registry registry = Registry.getRegistry("client_specific");
        String export_dir = registry.getString("TCExportDir") + File.separator + export_subDir;
        File folder = new File(export_dir);
        folder.mkdirs();
        if (!folder.exists()) {
            throw new Exception("Export folder not found.");
        }
        TCComponentTcFile[] imanFile = dataset.getTcFiles();
        File[] file = null;
        if (imanFile.length > 0) {
            file = dataset.getFiles(getNamedRefType(dataset, imanFile[0]), export_dir);
        }
        return file;
    }

    /**
     * 특정 dataset안에 있는 named reference의 reference type name을 받아오는 method이다.
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @param TCComponent
     *            TCComponent named TCComponentfile 객체
     * @return String named reference type name
     * @throws Exception
     */
    public static String getNamedRefType(TCComponentDataset datasetComponent, TCComponent TCComponent) throws Exception {
        String s = "";
        TCProperty imanproperty = datasetComponent.getTCProperty("ref_list");
        TCProperty imanproperty1 = datasetComponent.getTCProperty("ref_names");
        if (imanproperty == null || imanproperty1 == null) {
            return s;
        }
        TCComponent aTCComponent[] = imanproperty.getReferenceValueArray();
        String as[] = imanproperty1.getStringValueArray();
        if (aTCComponent == null || as == null) {
            return s;
        }
        int i = aTCComponent.length;
        if (i != as.length) {
            return s;
        }
        int j = -1;
        for (int k = 0; k < i; k++) {
            if (TCComponent != aTCComponent[k]) {
                continue;
            }
            j = k;
            break;
        }

        if (j != -1) {
            s = as[j];
        }
        return s;
    }

    /**
     * AttachFileComponent 가 파일 객체를 담고 있는 경우에만 사용할 수 있는 method이다.<br>
     * 반드시 주의하여야 한다.
     * 
     * @param newDataset
     *            TCComponentDataset 파일을 import할 dataset 객체
     * @param attachFileComponenent
     *            AttachFileComponent import할 file 객체.
     * @throws Exception
     */
    // public static void importFiles(TCComponentDataset newDataset, AttachFileComponent
    // attachFileComponenent) throws Exception
    // {
    // String[] as = new String[1];
    // String[] as1 = new String[1];
    // String[] as2 = new String[1];
    // String[] as3 = new String[1];

    // String filePath = ((File)attachFileComponenent.getAttachObject()).getPath();
    // String fileName = ((File)attachFileComponenent.getAttachObject()).getName();
    // int p = fileName.lastIndexOf(".");
    // String fileExtendsName = fileName.substring(p + 1);
    // String namedRefName = getNamedRefType(newDataset, fileExtendsName.toLowerCase());
    // //importFileName
    // as[0] = filePath;
    // //importRefType
    // as1[0] = namedRefName;
    // //importFileType
    // as2[0] = "File";
    // as3[0] = "Plain";
    // Registry registry = Registry.getRegistry("com.ugsolutions.iman.kernel.kernel");
    // int l = registry.getInt("IMANFile_transfer_buf_size", 512);
    // newDataset.setFiles(as, as2, as3, as1, l);
    // }

    /**
     * dataset에 일반 파일 객체를 올릴수 있는 method이다.
     * 
     * @param newDataset
     *            TCComponentDataset
     * @param file
     *            File
     * @throws Exception
     */
    public static void importFiles(TCComponentDataset newDataset, File file) throws Exception {
        String[] as = new String[1];
        String[] as1 = new String[1];
        String[] as2 = new String[1];
        String[] as3 = new String[1];

        String filePath = file.getPath();
        String fileName = file.getName();
        int p = fileName.lastIndexOf(".");
        String fileExtendsName = fileName.substring(p + 1);
        String namedRefName = getNamedRefType(newDataset, fileExtendsName.toLowerCase());
        // importFileName
        as[0] = filePath;
        // importRefType
        as1[0] = namedRefName;
        // importFileType
        as2[0] = "File";
        as3[0] = "Plain";
        Registry registry = Registry.getRegistry("com.ugsolutions.iman.kernel.kernel");
        int l = registry.getInt("IMANFile_transfer_buf_size", 512);
        newDataset.setFiles(as, as2, as3, as1, l);
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
        Vector refNameVector = getAllNamedRefTypeArray(dataset);
        for (int i = 0; i < refNameVector.size(); i++) {
            dataset.removeNamedReference(refNameVector.elementAt(i).toString());
        }
        for (int j = 0; j < imanFile.length; j++) {
            imanFile[j].delete();
        }
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
     * Dataset이 가지고 있는 모든 reference name을 찾는 method
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @return Vector
     * @throws Exception
     */
    public static Vector getAllNamedRefTypeArray(TCComponentDataset datasetComponent) throws Exception {
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
     * 현재 로그인한 TCSession 객체를 가져온다.
     * 
     * @return TCSession 현재 로그인 객체를 리턴한다.
     */
    public static TCSession getTCSession() {
        return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }

    /**
     * ItemRevision 하위의 연결된 TCComponentForm을 가져옴<br>
     * Usage: getRelatedFormFromItemRevision(itemRevision, "IMAN_specification","HKMC ME RQMT Form")
     * 
     * @param TCComponentForm
     * @return Vector
     */
    public static TCComponentForm getRelatedFormFromItemRevision(TCComponentItemRevision itemRevision, String relationType, String componentType) throws Exception {
        TCComponentForm relatedComp = null;
        TCComponent relatedComps[] = itemRevision.getRelatedComponents(relationType);
        for (int i = 0; i < relatedComps.length; i++) {
            if (relatedComps[i] instanceof TCComponentForm) {
                if (relatedComps[i].getType().equals(componentType)) {
                    relatedComp = (TCComponentForm) relatedComps[i];
                    break;
                }
            }
        }
        return relatedComp;
    }

    /**
     * 원본 파일을 원하는 이름의 파일로 바꾸려고 할때 사용한다.
     * 
     * @param sourceFile
     *            File 바꾸고자 하는 원본 파일
     * @param newFileName
     *            String 바꾸고자 하는 이름
     * @return File 최종 바뀐 파일
     */
    public static File renameFile(File sourceFile, String newFileName) {
        File newFile = new File(sourceFile.getParent(), newFileName + sourceFile.getName().substring(sourceFile.getName().indexOf(".")));
        sourceFile.renameTo(newFile);
        return newFile;
    }

    // /**
    // * 문서 문제점에서 새로운 아이디를 채번할때 사용하는 method이다.<br>
    // * 각 prefix에 따라 각각의 sequence가 따진다.
    // * @param prefix String sequence 앞에 붙일 부분.
    // * @param sequenceSpaceCount int 사용할 sequence의 자리수...
    // * @return String 채번된 ID
    // * @throws Exception
    // */
    // public static String createNewID(String prefix, int sequenceSpaceCount) throws Exception
    // {
    // ArrayList sqlDataList = new ArrayList();
    // String newID = "";
    // try
    // {
    // String query = "SELECT '" + prefix + "' || LPAD( NVL( MAX( SUBSTR(PITEM_ID, LENGTH('" +
    // prefix + "') + 1) ) + 1, 1 ), " + sequenceSpaceCount + ", '0') AS RE "
    // + "FROM   PITEM "
    // + "WHERE  SUBSTR(PITEM_ID, 1, LENGTH('" + prefix + "')) = '" + prefix + "' "
    // + "AND    LENGTH(PITEM_ID) - LENGTH('" + prefix + "') = " + sequenceSpaceCount + " ";

    // sqlDataList = executeJDBCSelectQuery(query.toString(),
    // JDBCExecuteDBProperty.DB_USER_INFODBA);

    // for(int i = 0; i < sqlDataList.toArray().length; i++)
    // {
    // ArrayList objList = (ArrayList)sqlDataList.get(i);
    // newID = (String)objList.get(0);
    // break;
    // }
    // } catch(Exception ex)
    // {
    // throw ex;
    // }

    // return newID;
    // }

    /**
     * JDBC를 이용하여 Oracle에 직접 접속하여 Data를 가져오는 부분이다.
     * 
     * @param query
     *            String 실행할 select query
     * @return Vector 실행 후 결과 query 이중 vector
     * @throws Exception
     */
    /*
     * public static Vector executeJDBCSelectQuery(String query) throws Exception {
     * DBConnectionManager connectionManager = null; Connection connection = null; Statement stmt =
     * null; ResultSet rs = null; Vector dataVector = new Vector(); try { connectionManager =
     * DBConnectionManager.getInstance(); connection =
     * connectionManager.getConnection(DBConnectionManager.HKMC_TCENG_DB); stmt =
     * connection.createStatement(); rs = stmt.executeQuery(query); while(rs.next()) { Vector
     * rowVector = new Vector(); int columnCount = rs.getMetaData().getColumnCount(); for(int i = 0;
     * i < columnCount; i++) { rowVector.addElement(rs.getObject(i + 1)); }
     * dataVector.addElement(rowVector); } } catch(Exception e) { throw e; } finally {
     * if(connectionManager != null) {
     * connectionManager.freeConnection(DBConnectionManager.HKMC_TCENG_DB, connection); } try {
     * if(stmt != null) { stmt.close(); } if(rs != null) { rs.close(); } } catch(SQLException ex) {
     * throw ex; } } return dataVector; }
     */
    /**
     * Servlet을 이용하여 JDBC 실행
     * 
     * @param query
     *            String 실행할 select query
     * @param dbuser
     *            접속할 DB
     *            (JDBCExecuteDBProperty.DB_USER_INFODBA,JDBCExecuteDBProperty.DB_USER_DMESUSER
     *            ,JDBCExecuteDBProperty.DB_USER_WTMADM)
     * @return ArrayList 실행 후 결과 query 이중 ArrayList
     * @throws Exception
     */
    // public static ArrayList executeJDBCSelectQuery(String query, String dbuser) throws Exception
    // {
    // //return new JDBCExecuteDB(dbuser).executeJDBCSelectQuery(query, null);

    // ArrayList al = null;

    // return executeJDBCSelectQuery(query, dbuser, al);
    // }

    // public static ArrayList executeJDBCSelectQuery(String query, String dbuser, ArrayList al)
    // throws Exception
    // {
    // return new JDBCExecuteDB(dbuser).executeJDBCSelectQuery(query, al);
    // //return executeJDBCSelectQuery(query, dbuser, null, al);
    // }

    /**
     * Servlet을 이용하여 JDBC 실행
     * 
     * @param query
     *            실행할 Select SQL
     * @param dbuser
     *            접속할 DB
     *            USer(JDBCExecuteDBProperty.DB_USER_INFODBA,JDBCExecuteDBProperty.DB_USER_DMESUSER
     *            ,JDBCExecuteDBProperty.DB_USER_WTMADM)
     * @param session
     * @return
     * @throws Exception
     */
    // public static ArrayList executeJDBCSelectQuery(String query, String dbuser, TCSession
    // session) throws Exception
    // {
    // //return new JDBCExecuteDB(dbuser, session).executeJDBCSelectQuery(query);
    // return executeJDBCSelectQuery(query, dbuser, session, null);
    // }

    // public static ArrayList executeJDBCSelectQuery(String query, String dbuser, TCSession
    // session, ArrayList al) throws Exception
    // {
    // return new JDBCExecuteDB(dbuser, session).executeJDBCSelectQuery(query, al);
    // }

    /**
     * 서버의 현재 날짜를 가져오기 위한 method임.
     * 
     * @return Date
     * @throws Exception
     */
    public static Date getServerDate() throws Exception {
        getTCSession().getUser().getNewStuffFolder().setProperty("object_desc", getTCSession().getUser().getNewStuffFolder().getProperty("object_desc"));
        return getTCSession().getUser().getNewStuffFolder().getDateProperty("last_mod_date");
        // String query = "SELECT SYSDATE FROM DUAL ";
        // ArrayList returnData = executeJDBCSelectQuery(query,
        // JDBCExecuteDBProperty.DB_USER_INFODBA);
        // return(Date)((ArrayList)returnData.get(0)).get(0);
    }

    /**
     * 특정 String에 대해 원하는 byte만큼 잘라 return해주는 method임.
     * 
     * @param name
     *            String
     * @param _byte
     *            int
     * @return String
     */
    public static String cutString(String name, int _byte) {
        if (name.getBytes().length > _byte) {
            for (int j = _byte - 3; j > 0; j--) {
                if (name.getBytes().length > _byte - 3) {
                    if (name.length() >= j) {
                        name = name.substring(0, j);
                    }
                } else {
                    name += "...";
                    break;
                }
            }
        }
        return name;
    }

    /**
     * JDBC를 이용하여 update를 할 경우 이 method를 이용하여 refresh를 하여야 함.
     * 
     * @param comp
     *            TCComponent[]
     * @throws Exception
     */
    // public static void refreshForJDBCUpdate(TCComponent[] comp) throws Exception
    // {
    // if(comp == null)
    // {
    // return;
    // }
    // String timestamp = String.valueOf(getServerDate().getTime());
    // JDBCExecuteDB updateJdbc = new JDBCExecuteDB(JDBCExecuteDBProperty.DB_USER_INFODBA);
    // for(int i = 0; i < comp.length; i++)
    // {
    // //timestamp 입력. 수정 여부
    // String tag = comp[i].getUid();
    // ArrayList colArray = new ArrayList();
    // colArray.add("ptimestamp");
    // ArrayList colValueArray = new ArrayList();
    // StringBuffer stringBuffer = new StringBuffer(timestamp);
    // stringBuffer.setLength(14);
    // timestamp = stringBuffer.toString().replace('    // colValueArray.add(timestamp);
    // ArrayList whereColArray = new ArrayList();
    // whereColArray.add("puid");
    // ArrayList whereColValueArray = new ArrayList();
    // whereColValueArray.add(tag);
    // updateJdbc.addUpdateSql("PPOM_OBJECT", colArray, colValueArray, whereColArray,
    // whereColValueArray);

    // //최종 수정일 입력
    // ArrayList colArray1 = new ArrayList();
    // colArray1.add("plast_mod_date");
    // ArrayList colValueArray1 = new ArrayList();
    // colValueArray1.add(getServerDate());
    // ArrayList whereColArray1 = new ArrayList();
    // whereColArray1.add("puid");
    // ArrayList whereColValueArray1 = new ArrayList();
    // whereColValueArray1.add(tag);
    // updateJdbc.addUpdateSql("PPOM_APPLICATION_OBJECT", colArray1, colValueArray1, whereColArray1,
    // whereColValueArray1);
    // }
    // updateJdbc.executeUpdate();
    // for(int i = 0; i < comp.length; i++)
    // {
    // comp[i].clearCache();
    // comp[i].refresh();
    // }
    // }

    // /**
    // * 결재프로세스의 결재이력 정보를 가져온다.
    // * @param proccess TCComponentProcess
    // * @return
    // * @throws Exception
    // */
    // public static ArrayList getWorkFlowHistory(TCComponentProcess proccess) throws Exception
    // {
    // ArrayList workFlowList = new ArrayList();
    // ArrayList sqlDataList = new ArrayList();

    // //process의 puid구함
    // String puid = proccess.getUid();
    // StringBuffer query = new StringBuffer();

    // query.append("SELECT PT.POBJECT_NAME TASK_NAME, U.PUSER_NAME USER_NAME, G.PNAME GROUP_NAME, ")
    // .append("  R.PROLE_NAME ROLE_NAME,SO.PDECISION_DATE DECISION_DATE, ")
    // .append("  DECODE(SO.PDECISION ,'89','승인','78','거부','') DECISION, ")
    // .append("  SO.PCOMMENTS COMMENTS ")
    // .append("FROM PSIGNOFF SO, PVGROUPMEMBER M, PVUSER U, PVPERSON P, PVGROUP G, PVROLE R, ")
    // .append("  PVEPMTASK T, PATTACHMENTS A, PVEPMTASK PT ")
    // .append("WHERE T.RPARENT_PROCESSU = '")
    // .append(puid)
    // .append("' AND SO.PUID = A.PVALU_0 AND A.PUID = T.PUID AND T.RPARENT_TASKU = PT.PUID ")
    // .append("  AND SO.RGROUP_MEMBERU = M.PUID AND M.RUSERU = U.PUID AND U.RPERSONU = P.PUID ")
    // .append("  AND M.RGROUPU = G.PUID AND M.RROLEU = R.PUID ")
    // .append("  AND T.POBJECT_NAME = 'select-signoff-team' ")
    // .append("ORDER BY SO.PDECISION_DATE");
    // try
    // {
    // sqlDataList = executeJDBCSelectQuery(query.toString(),
    // JDBCExecuteDBProperty.DB_USER_INFODBA);

    // for(int i = 0; i < sqlDataList.toArray().length; i++)
    // {
    // ArrayList objList = (ArrayList)sqlDataList.get(i);
    // ArrayList tempList = new ArrayList();
    // tempList.add((String)objList.get(0));
    // tempList.add((String)objList.get(1));
    // tempList.add((String)objList.get(2));
    // tempList.add((String)objList.get(3));
    // tempList.add((Timestamp)objList.get(4));
    // tempList.add((String)objList.get(5));
    // tempList.add((String)objList.get(6));
    // workFlowList.add(tempList);
    // }
    // } catch(Exception ex)
    // {
    // throw ex;
    // }

    // return workFlowList;
    // }

    /**
     * 선택한 Object의 결재프로세스 돌려준다.
     * 
     * @param TCComponent
     *            TCComponent
     * @return TCComponentProcess
     * @throws Exception
     */
    public static TCComponentProcess getWorkFlowProcess(TCComponent TCComponent) throws Exception {
        TCComponentProcess process = null;
        AIFComponentContext[] aaifcomponentcontext;
        InterfaceAIFComponent a;
        TCComponent com;

        try {
            if (TCComponent instanceof TCComponentProcess) {
                process = (TCComponentProcess) TCComponent;
            } else {
                TCComponent.refresh(); // refresh
                aaifcomponentcontext = TCComponent.whereReferenced(true);

                for (int i = 0; i < aaifcomponentcontext.length; i++) {
                    a = aaifcomponentcontext[i].getComponent();

                    com = (TCComponent) a;

                    if (com instanceof TCComponentProcess) {
                        process = (TCComponentProcess) com;
                        break;
                    }
                }
            }
        } catch (TCException ex) {
            throw ex;
        }

        return process;
    }

    // /**
    // * 파일사용현황 로그
    // * @param catetory Export/Dataset - static 변수로 선언되어 있는것을 사용해야함.
    // * @param titleName Export 명 (예:문서 검색 결과 리스트)
    // * @param subModuleName 데이터 종류별 구분할 수 있는 Key(검색결과는 차종, 모델 등 주요 검색조건, 구조검토문제점 Export는 문제점 관리번호
    // 등 각 모듈 개발자가 판단하여 주요 Key를 String으로 입력한다)
    // * @throws Exception
    // */
    // public static void addFileMonitorLog(String catetory, String titleName, String subModuleName)
    // throws Exception
    // {
    // TCSession session = CustomUtil.getTCSession();
    // String userid = session.getUser().getProperty("user_id");
    // String userdept = session.getGroup().getProperty("name");

    // JDBCExecuteDB jdbcDB = new JDBCExecuteDB(JDBCExecuteDBProperty.DB_USER_DMESUSER);

    // String strTableName = "hkmc_file_monitor";

    // ArrayList col = new ArrayList();
    // ArrayList val = new ArrayList();

    // // user_id
    // col.add("user_id");
    // val.add(userid);

    // //user_dept
    // col.add("user_dept");
    // val.add(userdept);

    // // catetory
    // col.add("category");
    // val.add(catetory);

    // // titleName
    // col.add("type");
    // val.add(titleName);

    // // key
    // col.add("key");
    // val.add(subModuleName);

    // jdbcDB.addInSertSql(strTableName, col, val);

    // jdbcDB.executeUpdate();
    // }

    /**
     * 현재로부터 한달 전 날짜를 return함.
     * 
     * @return
     */
    public static String getFromDate() {

        // 한달 전 날짜를 구해오기 위해 calendar생성
        Calendar calendar = Calendar.getInstance();
        String days = "";
        String month = "";
        String year = "";

        // 한달 전을 구하기
        calendar.add(Calendar.MONTH, -1);

        // string값으로 setting
        days = String.valueOf(calendar.get(Calendar.DATE));
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));

        // 날짜의 길이가 1이면, 앞에 0을 붙이기(예 05일)
        if (days.length() == 1) {
            days = "0" + days;
        }
        // 달(month)의 길이가 1이면 앞에 0을 붙이기(예 03월)
        if (month.length() == 1) {
            month = "0" + month;
        }

        // yyyy-MM-dd hh:mm:ss
        String dateString = year + "-" + month + "-" + days + " " + "00:00:00";

        return dateString;
    }

    /**
     * Today 23:59:59를 return함.
     * 
     * @return
     */
    public static String getToDate() {
        Calendar calendar = Calendar.getInstance();
        String days = "";
        String month = "";
        String year = "";

        // string값으로 setting
        days = String.valueOf(calendar.get(Calendar.DATE));
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));

        // 날짜의 길이가 1이면, 앞에 0을 붙이기(예 05일)
        if (days.length() == 1) {
            days = "0" + days;
        }
        // 달(month)의 길이가 1이면 앞에 0을 붙이기(예 03월)
        if (month.length() == 1) {
            month = "0" + month;
        }

        // yyyy-MM-dd hh:mm:ss
        String dateString = year + "-" + month + "-" + days + " " + "23:59:59";

        return dateString;
    }

    /**
     * 오늘 날짜를 리턴함.
     * 
     * @return
     */
    public static String getTODAY() {
        Calendar calendar = Calendar.getInstance();
        String days = "";
        String month = "";
        String year = "";

        // string값으로 setting
        days = String.valueOf(calendar.get(Calendar.DATE));
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));

        // 날짜의 길이가 1이면, 앞에 0을 붙이기(예 05일)
        if (days.length() == 1) {
            days = "0" + days;
        }
        // 달(month)의 길이가 1이면 앞에 0을 붙이기(예 03월)
        if (month.length() == 1) {
            month = "0" + month;
        }

        // yyyy-MM-dd hh:mm:ss
        String dateString = year + "-" + month + "-" + days;

        return dateString;
    }

    /**
     * 그룹의 장들을 리턴하는 메서드
     * 
     * @param group
     *            그룹
     * @return TCComponentGroupMember[] 찾은 그룹장들.
     * @throws Exception
     */
    public static TCComponentGroupMember[] getGroupLeaders(TCComponentGroup group) throws Exception {
        TCSession session;
        TCComponentGroupMemberType gmType;
        TCComponentGroupMember[] groupMembers = null;
        TCComponentRole[] groupRoles;
        TCComponentRole findRole = null;
        String roleName;

        try {
            if (group == null) {
                return groupMembers;
            }

            session = getTCSession();

            gmType = (TCComponentGroupMemberType) session.getTypeComponent("GroupMember");

            groupRoles = group.getRoles();

            for (int i = 0; i < groupRoles.length; i++) {
                roleName = groupRoles[i].toString();

                if (roleName.endsWith("장")) {
                    findRole = groupRoles[i];
                }
            }

            if (findRole != null) {
                groupMembers = gmType.findByRole(findRole, group);
            }
        } catch (Exception e) {
            throw e;
        }

        return groupMembers;
    }

    /**
     * Component에 대해 초기 입력시 한글 입력이 되도록 하는 method이다.<br>
     * 
     * @param component
     *            Component 초기 한글 입력이 필요한 component
     */
    public static void addTextFieldListenerForKorean(Component component) {
        InputContext input = component.getInputContext();
        Character.Subset[] subset = { Character.UnicodeBlock.HANGUL_SYLLABLES };
        input.setCharacterSubsets(subset);
    }

//    public static TCComponentGroup getGroupFromCode(String code) {
//        TCComponentGroup group = null;
//        TCComponent[] comps = null;
//        try {
//            comps = getTCSession().getClassService().findByClass("Group", "description", code);
//            if (comps != null) {
//                group = (TCComponentGroup) comps[0];
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return group;
//    }

    public static String[] getDesignerInfo(TCComponentItemRevision itemrevision) {
        String[] info = new String[] { "", "" };
        TCComponentForm revForm = null;

        try {
            if (itemrevision != null && itemrevision.getType().equals("ItemRevision")) {
                revForm = (TCComponentForm) itemrevision.getRelatedComponent("IMAN_master_form_rev");
                info[0] = revForm.getProperty("dsgn_dcd");
                info[1] = revForm.getProperty("dsgn_crgr_eeno");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return info;
    }

    /**
     * 선택된 Jtable Cell 을 Clipboard 로 Copy
     * 
     * @param selectTable
     *            JTable
     */
    public static void getClipboardData(JTable selectTable) {
        Clipboard system = null;
        StringSelection stsel = null;
        StringBuffer sbf = new StringBuffer();

        int numcols = selectTable.getSelectedColumnCount();
        int numrows = selectTable.getSelectedRowCount();
        int[] rowsselected = selectTable.getSelectedRows();
        int[] colsselected = selectTable.getSelectedColumns();
        if (numrows <= 0) {
            return;
        }

        for (int i = 0; i < numrows; i++) {
            for (int j = 0; j < numcols; j++) {
                sbf.append(selectTable.getValueAt(rowsselected[i], colsselected[j]));
                if (j < numcols - 1) {
                    sbf.append("\t");
                }
            }
            sbf.append("\n");
        }
        stsel = new StringSelection(sbf.toString());
        system = Toolkit.getDefaultToolkit().getSystemClipboard();
        system.setContents(stsel, stsel);
    }

    /**
     * Current Application
     * 
     * @return AbstractAIFUIApplication
     */
    public static AbstractAIFUIApplication getCurrentApplication() {
        Window window = Utilities.getCurrentWindow();

        while (true) {
            if (window instanceof AIFDesktop) {
                break;
            } else {
                if (window.getOwner() == null) {
                    return null;
                }

                window = window.getOwner();
            }

        }

        return ((AIFDesktop) window).getCurrentApplication();
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////////////
    // /////////////////////////// N E W /////////////////////////////
    // ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public static TCComponentDataset createPasteImportDataset(TCComponent targetComp, String datasetName, String description, String datasetType, String relation, String[] filePathNames, String[] namedRefs) throws Exception {
        TCSession session = targetComp.getSession();
        TCComponentDataset newDataset = null;
        try {
            TCComponentDatasetType TCComponentdatasettype = (TCComponentDatasetType) session.getTypeComponent(datasetType);
            newDataset = TCComponentdatasettype.setFiles(datasetName, description, datasetType, filePathNames, namedRefs);
            if (targetComp != null) {
                targetComp.add(relation, (TCComponent) newDataset);
            }
        } catch (Exception TCException) {
            TCException.printStackTrace();
            throw TCException;
        }
        return newDataset;
    }

    /**
     * Dataset Type과 확장자 명으로 그에 맞는 NamedReferenceType을 찾는다.
     * 
     * @param datasetType
     *            String
     * @param extensionName
     *            String
     * @return String
     * @throws Exception
     */
    public static String getNamedReferenceTypeString(String datasetType, String extensionName) throws Exception {
        String namedReferenceTypeString = "";
        TCComponentDatasetDefinitionType TCComponentdatasetdefinitiontype = (TCComponentDatasetDefinitionType) getTCSession().getTypeComponent("DatasetType");
        TCComponentDatasetDefinition TCComponentdatasetdefinition = TCComponentdatasetdefinitiontype.find(datasetType);
        NamedReferenceContext[] namedReferenceContext = TCComponentdatasetdefinition.getNamedReferenceContexts();
        for (int i = 0; i < namedReferenceContext.length; i++) {
            String s1 = namedReferenceContext[i].getNamedReference();
            String s2 = namedReferenceContext[i].getFileTemplate();
            if (s2.equalsIgnoreCase("*") || s2.equalsIgnoreCase("*.*")) {
                namedReferenceTypeString = s1;
            } else if (s2.equalsIgnoreCase("*." + extensionName)) {
                namedReferenceTypeString = s1;
                break;
            }
        }
        if (namedReferenceTypeString.equals("")) {
            throw new Exception("[not Found NamedReferenceType] : " + datasetType + " / " + extensionName);
        }
        return namedReferenceTypeString;
    }

    public static TCComponentItem createPasteItem(TCComponent parent, String relationType, String compType, String itemId, String revision_id, String itemName, String desc) throws TCException {
        TCSession session = getTCSession();
        TCComponentItem item = null;
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        item = itemType.create(itemId, revision_id, compType, itemName, desc, null);
        if (parent.isTypeOf("Home")) {
            ((TCComponentFolder) parent).add(relationType, new TCComponent[] { item });
        } else {
            parent.add(relationType, item);
        }
        return item;
    }

    public static TCComponentItem createPasteItem(TCComponent parent, String relationType, String compType, String itemName, String desc) throws TCException {
        TCSession session = getTCSession();
        TCComponentItem item = null;
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        String itemId = itemType.getNewID();
        String revision_id = itemType.getNewRev(null);
        item = itemType.create(itemId, revision_id, compType, itemName, desc, null);
        if (parent.isTypeOf("Newstuff Folder")) {
            ((TCComponentFolder) parent).add(relationType, new TCComponent[] { item });
        } else {
            parent.add(relationType, item);
        }
        return item;
    }

    /**
     * 메세지 Pattern과 파라메타를 Merge하여 Return 한다
     * 
     * @param msg
     *            String
     * @param params
     *            String[]
     * @return String
     */
    public static String mergeMessage(String msg, Object param) {
        if (param instanceof String) {
            msg = msg.replaceFirst("@", (String) param);
        } else if (param instanceof String[]) {
            String[] params = (String[]) param;

            for (int i = 0; i < params.length; i++) {
                msg = msg.replaceFirst("@", params[i]);
            }
        } else if (param instanceof Map) {
            Map params = (Map) param;

            for (int i = 0; i < params.size(); i++) {
                Iterator itr = params.keySet().iterator();

                while (itr.hasNext()) {
                    Object key = itr.next();
                    String paramName = "\\$" + key;
                    msg = msg.replaceAll(paramName, (String) params.get(key));
                }
            }
        }

        return msg;
    }

    public static String getCurrentSiteName() {
        try {
            return getTCSession().getCurrentSite().getSiteInfo().getSiteName();
        } catch (TCException ex) {
            return "";
        }
    }

    public static boolean isWritable(TCComponent iComp) {
        try {
            TCAccessControlService accessSvc = iComp.getSession().getTCAccessControlService();
            return accessSvc.checkPrivilege(iComp, "WRITE");
        } catch (TCException ie) {
            ie.printStackTrace();
        }
        return false;
    }

    public static String getViewType(TCComponentBOMViewRevision bvr) {
        try {
            TCComponentBOMView bv = (TCComponentBOMView) bvr.getReferenceProperty("bom_view");
            return getViewType(bv);
        } catch (TCException ie) {
            ie.printStackTrace();
        }
        return null;
    }

    public static String getViewType(TCComponentBOMView bv) {
        try {
            TCComponent viewType = bv.getReferenceProperty("view_type");
            return viewType.getStringProperty("name");
        } catch (TCException ie) {
            ie.printStackTrace();
        }
        return null;
    }

    /**
     * Source Text를 Text Server 의 Text로 변환한다.
     * 
     * @param sourceText
     *            Array Text
     * @return
     */
    public static String[] getStringFromTextservice(String[] sourceText) {
        String targetText[] = null;
        try {
            targetText = SDVPreBOMUtilities.getTCSession().getTextService().getTextValues(sourceText);
        } catch (TCException e) {
            e.printStackTrace();
        }
        return targetText == null ? sourceText : targetText;
    }

    /**
     * Source Text를 Text Server 의 Text로 변환한다.
     * 
     * @param sourceText
     *            Single Text
     * @return
     */
    public static String getStringFromTextservice(String sourceText) {
        String targetText = null;
        try {
            targetText = SDVPreBOMUtilities.getTCSession().getTextService().getTextValue(sourceText);
        } catch (TCException e) {
            e.printStackTrace();
        }
        return targetText == null ? sourceText : targetText;
    }

    static public TCComponentForm getNewForm(String formName, String desc, String formType, boolean saveForm) throws TCException {
        TCComponentFormType formComponentType = (TCComponentFormType) getTCSession().getTypeComponent(formType);
        return formComponentType.create(formName, desc, formType, saveForm);
    }

//    static public TCComponentFolder getNewFolder(String name, String desc, String type, InterfaceAIFComponent target) throws Exception {
//        InterfaceAIFComponent[] targets = { target };
//        TCSession session = getTCSession();
//        NewFolderCommand folderCommand = new NewFolderCommand(session, name, desc, type, targets);
//        NewFolderOperation folderOperation = new NewFolderOperation(folderCommand);
//        session.performOperation(folderOperation);
//        return (TCComponentFolder) folderOperation.getNewFolder();
//    }

    static public TCComponentFolder getHomeFolder() throws TCException {
        return getTCSession().getUser().getHomeFolder();
    }

    static public void setReferenceValueArray(String property, TCComponent target, TCComponent[] source) throws TCException {
        TCProperty imanProperty = target.getTCProperty(property);
        imanProperty.setReferenceValueArray(source);
        target.setTCProperty(imanProperty);
    }

    static public Object getLOVKeyValueFromProperty(TCProperty property, boolean isKey) throws TCException {
        Object returnObject = null;
        TCComponentListOfValues lovAttached = property.getLOV();
        if (lovAttached != null) {
            ListOfValuesInfo valueInfo = lovAttached.getListOfValues();
            int lovIndex = valueInfo.getValueIndex(property.getPropertyData());
            if (lovIndex != -1) {
                if (isKey)
                    returnObject = valueInfo.getLOVDisplayValues()[lovIndex];
                else
                    returnObject = valueInfo.getDescriptions()[lovIndex];
            }
        }
        return returnObject;
    }

    static public boolean isAttactedLOVProperty(TCProperty property) {
        return property.getLOV() != null;
    }

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
     * 파일로 데이터셋 생성
     * 
     * @Copyright : S-PALM
     * @author : 이정건
     * @since : 2011. 8. 29.
     * @param session
     * @param file
     * @return TCComponentDataset
     * @throws Exception
     */
    public static TCComponentDataset createDataset(TCSession session, File file) throws Exception {

        TCPreferenceService preferenceService = session.getPreferenceService();
        TCComponentDatasetType datasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");

        TCComponentDataset dataset = null;
        String extension = getExtension(file);
        //String[] dataset_Per_Extension = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "SPALM_DragNDropCopy_Dataset_Extension_List");
        String[] dataset_Per_Extension = preferenceService.getStringValuesAtLocation("SPALM_DragNDropCopy_Dataset_Extension_List", TCPreferenceLocation.OVERLAY_LOCATION);
        
        for (String string : dataset_Per_Extension) {
            if (extension.equals(string.substring(0, string.indexOf("=")))) {
                try {
                    dataset = datasetType.create(getFileName(file), "", string.substring(string.indexOf("=") + 1));
                    String namedReference = getNamedReference(session, dataset, extension);
                    dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { namedReference });
                } catch (Exception e) {
                    System.out.println("Dataset Type No Found! Select DatasetType Dialog Open...");
                    return dataset;
                }
            }
        }
        return dataset;
    }

    public static TCComponentDataset createDataset(TCSession session, File file, String type) throws Exception {
        TCComponentDatasetType datasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
        TCComponentDataset dataset = datasetType.create(getFileName(file), "", type);
        String extension = getExtension(file);
        String namedReference = getNamedReference(session, dataset, extension);
        dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { namedReference });
        return dataset;
    }

    public static TCComponent createFolder(TCSession session, File file, String type) throws Exception {
        TCComponentFolderType fType = (TCComponentFolderType) session.getTypeComponent("Folder");
        TCComponentFolder newObjectg = fType.create(getFileName(file), "", type);
        return newObjectg;
    }

    public static String getFileName(File file) throws Exception {
        if (file.isDirectory()) {
            return file.getName();
        }
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0) {
            return filename.substring(0, i);
        }
        return null;
    }

    public static String getExtension(File file) throws Exception {
        if (file.isDirectory())
            return null;
        String filename = file.getName();
        int i = filename.lastIndexOf('.');
        if (i > 0 && i < filename.length() - 1) {
            return filename.substring(i + 1).toLowerCase();
        }
        return null;
    }

    /**
     * 
     * @Copyright : S-PALM
     * @author : 이정건
     * @since : 2011. 8. 29.
     * @param session
     * @param dataset
     * @param ext
     * @return String
     */
    public static String getNamedReference(TCSession session, TCComponentDataset dataset, String ext) {

        String namedReference = null;

        try {
            TCComponentDatasetDefinition def = dataset.getDatasetDefinitionComponent();
            NamedReferenceContext namedRefTypes[] = def.getNamedReferenceContexts();
            for (int i = 0; i < namedRefTypes.length; i++) {
                if (namedRefTypes[i].getFileTemplate().equalsIgnoreCase("*." + ext)) {
                    namedReference = namedRefTypes[i].getNamedReference();
                }
                if (namedRefTypes[i].getFileFormat().equals("TEXT") && namedRefTypes[i].getFileTemplate().equalsIgnoreCase("*.*")) {
                    namedReference = namedRefTypes[i].getNamedReference();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return namedReference;
    }

    /**
     * 
     * @Copyright : S-PALM
     * @author : 이정건
     * @since : 2011. 8. 29.
     * @param session
     * @param revision
     * @param viewType
     * @return TCComponentBOMViewRevision
     * @throws Exception
     */
    public static TCComponentBOMViewRevision createBOMViewRevision(TCSession session, TCComponentItemRevision revision, String viewType) throws Exception {
        TCComponentBOMViewRevisionType bomViewRevisionType = (TCComponentBOMViewRevisionType) session.getTypeComponent("PSBOMViewRevision");
        TCComponentViewType[] viewTypes = bomViewRevisionType.getAvailableViewTypes(revision.getProperty("item_id"), revision.getProperty("item_revision_id"));
        for (int i = 0; i < viewTypes.length; i++) {
            if (viewTypes[i].toString().equals(viewType)) {
                return bomViewRevisionType.create(revision.getProperty("item_id"), revision.getProperty("item_revision_id"), viewTypes[i], false);
            }
        }
        return null;
    }

    /**
     * 필수 필드 컴포넌트 Decotation
     * 
     * @param comp
     * @param style
     * @author s.j park
     */
    public static ControlDecoration setRequiredFieldSymbol(Control comp, String style, boolean isShowOnlyFocus) {
        return setRequiredFieldSymbol(comp, style, null, isShowOnlyFocus);
    }

    public static ControlDecoration setRequiredFieldSymbol(Control comp, String style, String desc, boolean isShowOnlyFocus) {
        ControlDecoration controldecoration = new ControlDecoration(comp, 0x20080);
        FieldDecoration requiredDecorator = FieldDecorationRegistry.getDefault().getFieldDecoration(style);
        if (desc == null) {
            controldecoration.setDescriptionText(requiredDecorator.getDescription());
        } else {
            controldecoration.setDescriptionText(desc);
        }
        controldecoration.setImage(requiredDecorator.getImage());
        controldecoration.setShowOnlyOnFocus(isShowOnlyFocus);
        controldecoration.setShowHover(true);
        return controldecoration;
    }

    /**
     * 옵션값 가져오기(Scope : Site중에서)
     * 
     * @Copyright : S-PALM
     * @author : 박성준
     * @since : 2011. 6. 1.
     * @param session
     * @param preferenceName
     * @return
     */
    public static final String getSitePreferenceValue(TCSession session, String preferenceName) {
        String result;
        TCPreferenceService preferenceService = session.getPreferenceService();
        //result = preferenceService.getString(TCPreferenceService.TC_preference_site, preferenceName);
        result = preferenceService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
        return result;
    }

    /**
     * 옵션값들(배열) 가져오기(Scope : Site중에서)
     * 
     * @Copyright : S-PALM
     * @author : 박성준
     * @since : 2011. 6. 1.
     * @param session
     * @param preferenceName
     * @return
     */
    public static final String[] getSitePreferenceValues(TCSession session, String preferenceName) {
        String[] result;
        TCPreferenceService preferenceService = session.getPreferenceService();
        //result = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, preferenceName);
        result = preferenceService.getStringValuesAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
        return result;
    }

    /**
     * Property type별 값 셋팅
     * 
     * @Copyright : S-PALM
     * @author : 박성준
     * @since : 2011. 11. 21.
     * @param property
     * @param string
     * @throws TCException
     */
    public static void setStringToPropertyValue(TCProperty property, String string) throws TCException {
        int i = property.getPropertyType();
        switch (i) {
        case TCProperty.PROP_char:
            if (string.length() <= 0)
                break;
            property.setCharValueData(string.charAt(0));
            break;
        case TCProperty.PROP_date:
            property.setDateValueData(TCDateEncoder.getDate(string));
            break;
        case TCProperty.PROP_double:
            if (string.length() <= 0)
                break;
            property.setDoubleValueData(Double.parseDouble(string));
            break;
        case TCProperty.PROP_float:
            if (string.length() <= 0)
                break;
            //property.setFloatValueData(Float.parseFloat(string));
            property.setFloatValueData(Double.parseDouble(string));
            break;
        case TCProperty.PROP_int:
            if (string.length() <= 0)
                break;
            property.setIntValueData(Integer.parseInt(string));
            break;
        case TCProperty.PROP_logical:
            Boolean boolean1 = new Boolean(string);
            property.setLogicalValueData(boolean1.booleanValue());
            break;
        case TCProperty.PROP_short:
            Short short1 = new Short(string);
            property.setShortValueData(short1.shortValue());
            break;
        case TCProperty.PROP_string:
            if (property.getPropertyDescriptor().isArray()) {
                StringTokenizer tokenizer = new StringTokenizer(string, "|");
                String[] strings = new String[tokenizer.countTokens()];
                int j = 0;
                while (tokenizer.hasMoreElements()) {
                    strings[j++] = (String) tokenizer.nextElement();
                }
                property.setStringValueArrayData(strings);
            } else {
                property.setStringValueData(string);
            }
            break;
        case TCProperty.PROP_note:
            property.setNoteValueData(string);
            break;
        default:
            break;
        }
    }

    /**
     * Property type별 값 셋팅(TypedReference 지원)
     * 
     */
    public static void setObjectToPropertyValue(TCProperty property, Object value) throws Exception {
        int i = property.getPropertyType();

        if (TCProperty.PROP_typed_reference == i || TCProperty.PROP_untyped_reference == i) {

            if (value == null || "".equals(value.toString())) {
                property.setReferenceValue(null);
            } else if (value instanceof TCComponent) {
                property.setReferenceValue((TCComponent) value);
            } else if (value instanceof TCComponent[]) {
                property.setReferenceValueArray((TCComponent[]) value);
            } else if (value instanceof String) {

                String strValue = (String) value;
                // ItemRevision인 경우
                if (strValue.lastIndexOf("/") > 0) {
                    String itemID = strValue.substring(0, strValue.lastIndexOf("/"));
                    String itemRev = strValue.substring(strValue.lastIndexOf("/") + 1, strValue.length());

                    TCComponent[] comps = SDVPreBOMUtilities.queryComponent("Item Revision...", new String[] { "ItemID", "Revision" }, new String[] { itemID, itemRev });

                    if (comps != null && comps.length == 1) {
                        property.setReferenceValue(comps[0]);
                    }

                }
                // Item인 경우
                else {
                    TCComponent[] comps = SDVPreBOMUtilities.queryComponent("Item...", new String[] { "ItemID" }, new String[] { strValue });

                    if (comps != null && comps.length == 1) {
                        property.setReferenceValue(comps[0]);
                    }
                }

            }

        } else if (TCProperty.PROP_date == i) {
            if (value instanceof Date) {
                property.setDateValue((Date) value);
            } else {
                String string = (String) value;
                // [SR140702-058][20140701] KOG Blank 값에 대한 처리. value 자체가 없는 경우 String 값이 null 임. (20140707, bskwak)
                if (string == null || string.isEmpty()) {
                    return;
                }
                // 기술문서 Excel Upload에서 사용
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date date = null;
                try {
                    date = sdf.parse(string);
                } catch (ParseException e) {
                    e.printStackTrace();

                    throw new Exception(e.getMessage());
                }
                property.setDateValue(date);
            }

        } else {
            String string = (String) value;
            setStringToPropertyValue(property, string);
        }

    }

    /**
     * IRDC 가 적용된 Type 의 Item 을 생성하는 Method
     * 
     * @Copyright : S-PALM
     * @author : 김병균
     * @since : 2011. 11. 24.
     * @param session
     *            {@link TCSession} - Session
     * @param itemType
     *            {@link String} - ITEM Type
     * @param itemId
     *            {@link String} - ITEM ID
     * @param itemName
     *            {@link String} - ITEM Name
     * @param itemDesc
     *            {@link String} - ITEM Description
     * @param itemRev
     *            {@link String} - ITEM Revision
     * @param revPropertyNames
     *            {@link String[]} - ITEM Revision Property Names
     * @param revPropertyValues
     *            {@link String[]} - ITEM Revision Property Values
     * @return
     * @throws TCException
     */
    public static TCComponentItem createIRDCItem(TCSession session, String itemType, String itemId, String itemName, String itemDesc, String itemRev, String[] revPropertyNames, String[] revPropertyValues) throws TCException {
        TCComponentItem irdcItem = null;

        IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(session, itemType);
        Map secondaryCreateDefinitions = createDefinition.getSecondaryCreateDefinitions();
        List revList = (ArrayList) secondaryCreateDefinitions.get("revision");
        IBOCreateDefinition createDefinitionRev = (IBOCreateDefinition) revList.get(0);

        List createInputList = new ArrayList();
        CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
        createInput.add("item_id", itemId);
        createInput.add("object_name", itemName);
        createInput.add("object_desc", itemDesc);
        createInputList.add(createInput);

        CreateInstanceInput createInputRev = new CreateInstanceInput(createDefinitionRev);
        createInputRev.add("item_revision_id", itemRev);
        createInputList.add(createInputRev);

        List createInputRevList = new ArrayList();
        CreateInstanceInput createInputRev2 = new CreateInstanceInput(createDefinitionRev);
        for (int propertyCount = 0; propertyCount < revPropertyNames.length; propertyCount++) {
            if (!(isNullString(revPropertyValues[propertyCount])))
                createInputRev2.add(revPropertyNames[propertyCount], revPropertyValues[propertyCount]);
        }
        createInputRevList.add(createInputRev2);

        List argInput = new ArrayList(0);
        argInput.addAll(createInputList);
        argInput.addAll(createInputRevList);

        List resultList = SOAGenericCreateHelper.create(session, createDefinition, argInput);
        if (resultList != null && resultList.size() > 0) {
            for (int j = 0; j < resultList.size(); j++) {
                TCComponent tempComponent = (TCComponent) resultList.get(j);
                if (itemType.equals(tempComponent.getType())) {
                    irdcItem = (TCComponentItem) tempComponent;
                }
            }
        }

        return irdcItem;
    }

    /**
     * Object[]의 값을 읽어서 배열의 첫번째 인덱스에 공백("")를 넣어서 Object[]로 리턴한다.
     */
    public static Object[] getAddBlankArray(Object[] arr) {
        ArrayList<Object> arrayList = new ArrayList<Object>();
        arrayList.add(0, "");
        for (int i = 0; i < arr.length; i++) {
            arrayList.add(i + 1, arr[i]);
        }
        return arrayList.toArray();
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
     * revisionRule 을 가져온다.
     * 
     * @Copyright : S-PALM
     * @author : 박성준
     * @since : 2011. 12. 12.
     * @param session
     * @param ruleName
     * @return
     * @throws Exception
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
     * BOMLine 정보 가져오기 BY 아이템 리비젼
     * 
     * @Copyright : S-PALM
     * @author : 박성준
     * @since : 2011. 12. 12.
     * @param targetRevision
     * @param session
     * @return
     * @throws TCException
     */
    public static TCComponentBOMLine getBomline(TCComponentItemRevision targetRevision, TCSession session) throws TCException {
        TCComponentBOMLine topLine = null;
        if (targetRevision != null) {
            TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
            TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
            TCComponentBOMWindow bomWindow = windowType.create(ruleType.getDefaultRule());
            topLine = bomWindow.setWindowTopLine(null, targetRevision, null, null);
        }
        return topLine;
    }

    /**
     * Null String 을 체크하는 Method
     * 
     * @Copyright : S-PALM
     * @author : 김병균
     * @since : 2011. 11. 15.
     * @param checkString
     * @return boolean
     */
    public static boolean isNullString(String checkString) {
        if (checkString == null || "".equals(checkString) || "null".equals(checkString))
            return true;
        return false;
    }

    /**
     * Color 관련 Util
     * 
     * @Copyright : S-PALM
     * @author : 이정건
     * @since : 2012. 3. 21. Package ID : com.pungkang.common.utils.CustomUtil.java
     */
    public enum Colors {

        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // various colors in the pallete
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        Pink(255, 175, 175), Green(159, 205, 20), Orange(213, 113, 13), Yellow(Color.yellow), Red(189, 67, 67), LightBlue(208, 223, 245), Blue(Color.blue), Black(0, 0, 0), White(255, 255, 255), Gray(Color.gray.getRed(), Color.gray.getGreen(), Color.gray.getBlue()), LightGray(200, 200, 200);

        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // constructors
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

        Colors(Color c) {
            _myColor = c;
        }

        Colors(int r, int g, int b) {
            _myColor = new Color(r, g, b);
        }

        Colors(int r, int g, int b, int alpha) {
            _myColor = new Color(r, g, b, alpha);
        }

        Colors(float r, float g, float b, float alpha) {
            _myColor = new Color(r, g, b, alpha);
        }

        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // data
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

        private Color _myColor;

        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
        // methods
        // XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

        public Color alpha(float t) {
            return new Color(_myColor.getRed(), _myColor.getGreen(), _myColor.getBlue(), (int) (t * 255f));
        }

        public static Color alpha(Color c, float t) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (t * 255f));
        }

        public Color color() {
            return _myColor;
        }

        public Color color(float f) {
            return alpha(f);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("r=").append(_myColor.getRed()).append(", g=").append(_myColor.getGreen()).append(", b=").append(_myColor.getBlue()).append("\n");
            return sb.toString();
        }

        public String toHexString() {
            Color c = _myColor;
            StringBuilder sb = new StringBuilder();
            sb.append("#");
            sb.append(Integer.toHexString(_myColor.getRed()));
            sb.append(Integer.toHexString(_myColor.getGreen()));
            sb.append(Integer.toHexString(_myColor.getBlue()));
            return sb.toString();
        }

    }// end enum Colors

    public static int[] getStringArrayToIntArray(String[] strs) {
        if (strs != null && strs.length > 0) {
            int[] ints = new int[strs.length];
            for (int i = 0; i < strs.length; i++) {
                ints[i] = Integer.parseInt(strs[i]);
            }
            return ints;
        }
        return null;
    }

    /**
     * BOM Top Line 가져오기.
     * 
     * @Copyright : S-PALM
     * @author : 권상기
     * @since : 2012. 12. 20.
     * @param bomWindow
     * @param revision
     * @return
     * @throws TCException
     */
    public static TCComponentBOMLine getTopBomLine(TCComponentBOMWindow bomWindow, TCComponentItemRevision revision) throws TCException {
        TCComponentBOMLine bomLine = null;
        bomLine = bomWindow.setWindowTopLine(null, revision, null, null);
        return bomLine;
    }

    /**
     * BOM Windows 생성.
     * 
     * @Copyright : S-PALM
     * @author : 권상기
     * @since : 2012. 12. 20.
     * @return
     * @throws TCException
     */
    public static TCComponentBOMWindow createBOMWindow() throws TCException {
        TCSession session = getTCSession();
        TCComponentBOMWindow bomWindow = null;
        TCComponentRevisionRuleType revisionRuleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
        TCComponentRevisionRule revisionRule = revisionRuleType.getDefaultRule();
        TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        bomWindow = bomWindowType.create(revisionRule);
        return bomWindow;
    }

    /**
     * BomView 를 획득.
     * 
     * @Copyright : S-PALM
     * @author : 권상기
     * @since : 2012. 12. 20.
     * @param context
     * @throws Exception
     */
    public static Vector getBomLine(AIFComponentContext[] context, Vector getBomVtr) throws Exception {
        TCSession session = getTCSession();
        if (context.length > 0) {
            TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) context[0].getComponent();
            TCComponentRevisionRuleType revisionRuleType = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
            TCComponentRevisionRule revisionRule = revisionRuleType.getDefaultRule();
            TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
            TCComponentBOMWindow bomWindow = bomWindowType.create(revisionRule);
            TCComponentBOMLine bomTopLine = bomWindow.setWindowTopLine(null, null, null, bomViewRevision);
            getBomVtr.addElement(bomTopLine);
            findChildren(bomTopLine, getBomVtr); // 하위 컴포넌트들을 찾는다.

            bomWindow.close();
        }
        return getBomVtr;
    }

    /**
     * 자식 BOM Line를 BOM Vector에 담음.
     * 
     * @Copyright : S-PALM
     * @author : 권상기
     * @since : 2012. 12. 20.
     * @param bomLine
     * @throws Exception
     */
    public static void findChildren(TCComponentBOMLine bomLine, Vector getBomVtr) throws Exception {
        AIFComponentContext[] bomLineContext = bomLine.getChildren();
        if (bomLineContext.length != 0) {
            for (int i = 0; i < bomLineContext.length; i++) {
                InterfaceAIFComponent con = bomLineContext[i].getComponent();
                TCComponentBOMLine bom = (TCComponentBOMLine) con;
                getBomVtr.addElement(bom);
                findChildren((TCComponentBOMLine) bomLineContext[i].getComponent(), getBomVtr);
            }
        }
    }

    /**
     * 새로운 SaveAs 대상이 되는 Item들.
     * 
     * @Copyright : S-PALM
     * @author : 권상기
     * @since : 2012. 12. 20.
     * @param itemRev
     * @throws Exception
     */
    public static ArrayList getNewBomLine(TCComponentItem itemComp, ArrayList newItemList, Vector getBomVtr) throws Exception {
        newItemList.add(itemComp);

        int bomVtrSize = getBomVtr.size();
        TCComponentItem item = null;
        String type = "";
        String name = "";
        String desc = "";
        String revID = "";
        TCComponentItem newItem = null;
        TCComponentItemRevision itemRev = null;

        if (bomVtrSize > 1) {
            for (int i = 1; i < bomVtrSize; i++) {
                TCComponentBOMLine bomLine = ((TCComponentBOMLine) getBomVtr.get(i));
                item = bomLine.getItem();

                itemRev = item.getLatestItemRevision();

                // ItemRevision Type에 따른 속성 값 가져 오기.
                if (itemRev.getType().equals("")) {
                    item = itemRev.getItem();
                }
                type = item.getType();
                name = itemRev.getProperty("object_name");
                desc = itemRev.getProperty("object_desc");

                String str = item.getProperty("displayable_revisions");
                String[] strs = str.split("[/]");
                String str1 = strs[1].toString();
                String[] strs1 = str1.split("[;]");
                revID = strs1[0].toString();

                newItem = SDVPreBOMUtilities.createItem(type, SDVPreBOMUtilities.getNextItemId(type), revID, name, desc);

                newItemList.add(newItem);
            }
        }
        return newItemList;
    }

    /**
     * 대상 타겟 Item을 topRevison에 등록.
     * 
     * @Copyright : S-PALM
     * @author : 권상기
     * @since : 2012. 12. 20.
     * @param topRevision
     * @param targetItem
     * @throws Exception
     */
    public static void bomViewItemRevisionCheck(TCComponentItemRevision topRevision, TCComponentItem targetItem) throws Exception {
        TCComponentItemRevision topItemRevison = topRevision;
        TCComponentItemRevision targetItemRevision = targetItem.getWorkingItemRevisions()[0];

        TCComponentBOMWindow bomWindow = createBOMWindow();

        TCComponentBOMLine bomLine = getTopBomLine(bomWindow, topItemRevison);
        bomLine.add(null, targetItemRevision, null, false);
        bomWindow.save();
        topItemRevison = targetItemRevision;
    }

    /**
     * app의 target 값을 가져온다.
     * 
     * @return InterfaceAIFComponent[] target
     */
    public static InterfaceAIFComponent[] getTargets() {
        // jay
        // List<AbstractAIFUIApplication> apps = AIFDesktop.getActiveDesktop().getApplications();
        // InterfaceAIFComponent[] targets = apps.get(0).getTargetComponents();

        InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
        return targets;
    }

    public static boolean isEmpty(String str) {
        if (str == null || "".equals(str.trim()))
            return true;
        else
            return false;
    }
    
    public static TCComponentDataset createDataset(String filePath, String datasetName) throws Exception {
		if (datasetType == null) {
			datasetType = (TCComponentDatasetType) ((TCSession) AIFUtility.getDefaultSession()).getTypeComponent("Dataset");
		}

		TCComponentDataset dataset = null;
		File file = new File(filePath);
		if (file != null) {
			String extension = getExtension(file);
			if (extension != null && !extension.equals("")) {
				if (datasetName == null) {
					datasetName = getFileName(file);
				}

				if (extension.equals("xls")) {
					dataset = datasetType.create(datasetName, "", "MSExcel");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcel" }, new String[] { "Plain" }, new String[] { "excel" });
				} else if (extension.equals("xlsx")) {
					dataset = datasetType.create(datasetName, "", "MSExcelX");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcelX" }, new String[] { "Plain" }, new String[] { "excel" });
				} else if (extension.equals("doc")) {
					dataset = datasetType.create(datasetName, "", "MSWord");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSWord" }, new String[] { "Plain" }, new String[] { "word" });
				} else if (extension.equals("docx")) {
					dataset = datasetType.create(datasetName, "", "MSWordX");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSWordX" }, new String[] { "Plain" }, new String[] { "word" });
				} else if (extension.equals("ppt")) {
					dataset = datasetType.create(datasetName, "", "MSPowerPoint");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSPowerPoint" }, new String[] { "Plain" }, new String[] { "powerpoint" });
				} else if (extension.equals("pptx")) {
					dataset = datasetType.create(datasetName, "", "MSPowerPointX");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSPowerPointX" }, new String[] { "Plain" }, new String[] { "powerpoint" });
				} else if (extension.equals("txt")) {
					dataset = datasetType.create(datasetName, "", "Text");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Text" }, new String[] { "Plain" }, new String[] { "Text" });
				} else if (extension.equals("pdf")) {
					dataset = datasetType.create(datasetName, "", "PDF");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "PDF" }, new String[] { "Plain" }, new String[] { "PDF_Reference" });
				} else if (extension.equals("jpg")) {
					dataset = datasetType.create(datasetName, "", "JPEG");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "JPEG" }, new String[] { "Plain" }, new String[] { "JPEG_Reference" });
				} else if (extension.equals("gif")) {
					dataset = datasetType.create(datasetName, "", "GIF");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "GIF" }, new String[] { "Plain" }, new String[] { "GIF_Reference" });
				} else if (extension.equals("jpeg") || extension.equals("png") || extension.equals("tif") || extension.equals("tiff") || extension.equals("bmp")) {
					dataset = datasetType.create(datasetName, "", "Image");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Image" }, new String[] { "Plain" }, new String[] { "Image" });
				} else if (extension.equals("dwg")) {
					dataset = datasetType.create(datasetName, "", "M7_RESOURCEDRAWINGDWG");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "M7_RESOURCEDRAWINGDWG" }, new String[] { "Plain" }, new String[] { "M7_DWG" });
				} else if (extension.equals("zip")) {
					dataset = datasetType.create(datasetName, "", "Zip");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Zip" }, new String[] { "Plain" }, new String[] { "ZIPFILE" });
				} else if (extension.equals("htm") || extension.equals("html")) {
					dataset = datasetType.create(datasetName, "", "HTML");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "Text" }, new String[] { "Plain" }, new String[] { "HTML" });
				} else if (extension.equals("eml")) {
					dataset = datasetType.create(datasetName, "", "EML");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "EML" }, new String[] { "Plain" }, new String[] { "EML_Reference" });
				} else if (extension.equalsIgnoreCase("CATPart")) {
					dataset = datasetType.create(datasetName, "", "CATPart");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "CATPart" }, new String[] { "Plain" }, new String[] { "catpart" });
				} else if (extension.equalsIgnoreCase("cgr")) {
					dataset = datasetType.create(datasetName, "", "CATCache");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "CATCache" }, new String[] { "Plain" }, new String[] { "catcgr" });
				} else if (extension.equalsIgnoreCase("jt")) {
					dataset = datasetType.create(datasetName, "", "DirectModel");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "DirectModel" }, new String[] { "Plain" }, new String[] { "JTPART" });
				} else {
					dataset = datasetType.create(datasetName, "", "MISC");
					dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MISC" }, new String[] { "Plain" }, new String[] { "MISC_BINARY" });
				}
			}
		}
		return dataset;
	}
    
    public static TCComponentItem FindItem(String itemId, String itemTypeName) throws Exception {
		TCComponentItemType itemType = (TCComponentItemType) getTCSession().getTypeComponent(itemTypeName);
		if (itemType == null)
			return null;
		TCComponentItem[] items = itemType.findItems(itemId);
		if (items.length == 0)
			return null;
		return items[0];
	}
    
    /**
     * 자가결재 워크플로우를 이용하여 결재를 진행한다
     * @param revision (대상 아이템리비전)
     * @param workFlowName (진행 할 셀프 워크플로우 이름)
     * @throws Exception
     */
    public static void selfRelease(TCComponentItemRevision revision, String workFlowName) throws Exception {
      TCComponentTaskTemplate template = null;
      TCComponentTaskTemplateType imancomponenttasktemplatetype = (TCComponentTaskTemplateType) revision.getSession().getTypeComponent("EPMTaskTemplate");
      TCComponentTaskTemplate[] tasktemplate = imancomponenttasktemplatetype.getProcessTemplates(false, false, null, null, null);
      for (int j = 0; j < tasktemplate.length; j++) {
    	  if (tasktemplate[j].toString().equals(workFlowName)) {
    		  template = tasktemplate[j];
    		  break;
        }
      }
    
      if(template == null){
    	  throw new Exception("Can't Find SelfRelease Process Template");
      }
      
      // 자가결재 수행
      TCComponent[] aimancomponent = new TCComponent[]{revision};
      int a[] = new int[]{1};
      if( revision != null ) {
    	  TCComponentProcessType processtype = (TCComponentProcessType) revision.getSession().getTypeComponent("Job");
    	  processtype.create("Self Release", "",template, aimancomponent, a);
      }
    }
    
    /**
     * CCN Reload.
     * Solution Items, Problem Items Setting.
     * 완료 후 Solution Item 목록 반환.
     * @param changeRevision CCN
     * @return Solution Items List
     * @throws Exception
     */
    public static TCComponent[] getSolutionItemsAfterReGenerate(TCComponentChangeItemRevision changeRevision) throws Exception {
        // 중복 방지 삭제
        TCComponent[] solutionList = changeRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
        TCComponent[] problemList = changeRevision.getRelatedComponents(TypeConstant.CCN_PROBLEM_ITEM);

        HashMap<String, TCComponent> solutionMap = new HashMap<String, TCComponent>();
        HashMap<String, TCComponent> problemMap = new HashMap<String, TCComponent>();
        HashMap<String, TCComponent> changeItemMap = new HashMap<String, TCComponent>();
        HashMap<String, TCComponent> changeItemPrevRevMap = new HashMap<String, TCComponent>(); // 변경된 Item Revision의 이전 Revision 목록. Problem Items에 추가 또는 삭제될 대상 비교 목적.

        for(TCComponent tccomponent : solutionList) {
            solutionMap.put(tccomponent.getUid(), tccomponent);
        }

        // Problem Items의 UID 목록 추출
        for(TCComponent tccomponent : problemList) {
            problemMap.put(tccomponent.getUid(), tccomponent);
        }

        AIFComponentContext[] aifcomponentcontexts = changeRevision.whereReferenced();
        ArrayList<String> changedItemRevison = new ArrayList<String>();
        for(AIFComponentContext aifcomponentcontext : aifcomponentcontexts) {

            // changeRevision.whereReferenced 중 MECOItem은 SolutionItem Attach에서 제외한다.
            // changeRevision.whereReferenced 중 TCComponentItemRevision이 아닌 것은 SolutionItem Attach에서 제외하는 것으로 변경.
            if(!"0".equals(aifcomponentcontext.getComponent().getProperty("active_seq")) && (aifcomponentcontext.getComponent() instanceof TCComponentItemRevision)) {
                changeItemMap.put(aifcomponentcontext.getComponent().getUid(), (TCComponent)aifcomponentcontext.getComponent());  
                
                // 이전 Revision 추출 후 Problem Items 추가 대상 목록에 추가
                TCComponentItemRevision preRevision = getPreviousRevision((TCComponentItemRevision)aifcomponentcontext.getComponent());
                if (preRevision != null) {
                    // 현재 Revision이 First Revision일 경우 Null이 되며, Problem Items 비교대상에 추가되지 않는다.
                    changeItemPrevRevMap.put(preRevision.getUid(), preRevision);
                }
            }else{
                //Skip.
            }

        }

        Object[] noAttach_solution_ids = subtract(changeItemMap.keySet(),solutionMap.keySet()).toArray(new Object[0]);
        Object[] dettach_solution_ids =  subtract(solutionMap.keySet(), changeItemMap.keySet()).toArray(new Object[0]);
        
        // 기존 Problem Items에 존재하는 목록과 현재 변경된 Problem Items 목록 비교. 추가해야할 대상과 삭제해야될 대상 구분.
        Object[] noAttach_problem_ids = subtract(changeItemPrevRevMap.keySet(),problemMap.keySet()).toArray(new Object[0]);
        Object[] dettach_problem_ids =  subtract(problemMap.keySet(), changeItemPrevRevMap.keySet()).toArray(new Object[0]);

        int resultCount1 = noAttach_solution_ids.length;
        if(noAttach_solution_ids!=null && resultCount1 > 0){


            String[] itemRevisions = new String[resultCount1];
            for(int i = 0 ; i < resultCount1 ; i++){
                itemRevisions[i] = noAttach_solution_ids[i].toString();
            }
            TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
            if(tcComponents.length > 0) {
                changeRevision.add(TypeConstant.CCN_SOLUTION_ITEM, tcComponents);
            }
        }


        int resultCount2 = dettach_solution_ids.length;
        if(dettach_solution_ids!=null && resultCount2 > 0){

            String[] itemRevisions = new String[resultCount2];
            for(int i = 0 ; i < resultCount2 ; i++){
                itemRevisions[i] = dettach_solution_ids[i].toString();
            }
            TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
            if(tcComponents.length > 0)
                changeRevision.remove(TypeConstant.CCN_SOLUTION_ITEM,tcComponents);
        }
        
        // Problem Items 추가 리스트 반영
        int resultCount3 = noAttach_problem_ids.length;
        if(noAttach_problem_ids!=null && resultCount3 > 0){
            
            String[] itemRevisions = new String[resultCount3];
            for(int i = 0 ; i < resultCount3 ; i++){
                itemRevisions[i] = noAttach_problem_ids[i].toString();
            }
            TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
            if(tcComponents.length > 0) {
                changeRevision.add(TypeConstant.CCN_PROBLEM_ITEM, tcComponents);
            }
        }
        
        // Problem Items 제거 리스트 반영
        int resultCount4 = dettach_problem_ids.length;
        if(dettach_problem_ids!=null && resultCount4 > 0){
            
            String[] itemRevisions = new String[resultCount4];
            for(int i = 0 ; i < resultCount4 ; i++){
                itemRevisions[i] = dettach_problem_ids[i].toString();
            }
            TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
            if(tcComponents.length > 0)
                changeRevision.remove(TypeConstant.CCN_PROBLEM_ITEM,tcComponents);
        }

        return changeRevision.getRelatedComponents(TypeConstant.CCN_SOLUTION_ITEM);
    }
    
    public static Collection subtract(Collection a, Collection b) {
        ArrayList list = new ArrayList(a);
        for (Iterator it = b.iterator(); it.hasNext();) {
            list.remove(it.next());
        }
        return list;
    }
    
    /**
     * BOM Window 를 가져옴
     * 
     * @param session
     *            TCSession
     * @param itemRevision
     *            아이템 리비전
     * @param ruleName
     *            리비전 룰 Name
     * @param viewType
     *            뷰타입명
     * @return
     * @throws Exception
     */
    public static TCComponentBOMWindow getBOMWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {
        TCComponentRevisionRule revRule;
        TCSession session = getTCSession();
        TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
        // 리비전 룰을 가져옴
        revRule = getRevisionRule(session, ruleName);
        // BOMWindow를 생성
        TCComponentBOMWindow bomWindow = null;
        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        bomWindow = windowType.create(revRule);
        bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);

        return bomWindow;
    }
    
    /**
     * 리비전 하위에 view와 타입이 일치하는 BOMViewRevision 검색하여 반환한다.
     * 
     * @param revision
     *            ItemRevision TCComponent
     * @param viewType
     *            뷰타입 String
     * @return bomViewRevision TCComponentBOMViewRevision
     * @throws TCException
     */
    public static TCComponentBOMViewRevision getBOMViewRevision(TCComponent comp, String viewType) throws Exception {
        comp.refresh();
        TCComponent[] arrayStructureRevision = comp.getRelatedComponents("structure_revisions");

        for (TCComponent bvr : arrayStructureRevision) {
            TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) bvr;

            if (bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type").equals(viewType)) {
                return bomViewRevision;
            }
        }

        return null;
    }
    
    /**
     * 하위 ComponentBOMLine List
     * 
     * @method getChildrenBOMLine
     * @date 
     * @param
     * @return TCComponentBOMLine[]
     * @exception
     * @throws
     * @see
     */
    public static TCComponentBOMLine[] getChildrenBOMLine(TCComponentBOMLine parentBOMLine) throws Exception {
        AIFComponentContext contexts[] = parentBOMLine.getChildren();
        TCComponentBOMLine childLines[] = new TCComponentBOMLine[contexts.length];
        for (int i = 0; i < childLines.length; i++) {
            childLines[i] = (TCComponentBOMLine) contexts[i].getComponent();
        }
        return childLines;
    }
    
    /**
     * 숫자를 던지면 원하는 자리수로 변경 예) 34 -> 00034
     * @param num
     * @param format
     * @return
     */
    public static String getFormmatedNumber(int num, String format) {
        String result;
        StringBuffer formattedNum = new StringBuffer();
        String strNum = "" + num;
        if (format == null) {
            result = strNum;
        }else{
            try {
                for (int i = 0; i < format.length() - strNum.length(); i++) {
                    formattedNum.append(format.charAt(i));
                }
                formattedNum.append(strNum);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                result = "error";
            }
        }
        result = formattedNum.toString();
        return result;
    }
    
    public static String getCCNId(String projectCode, String sysCode) throws TCException{
        String year_YY = CustomUtil.getTODAY().substring(2, 4);
        String systemCode = sysCode.substring(0, 1);
        String ccnID = projectCode + systemCode + year_YY + "-";
//        TCComponent[] tccomps = SDVQueryUtils.executeSavedQuery("Item...", new String[]{"ItemID", "Type"}, new String[]{ccnID + "*", TypeConstant.S7_PRECCNTYPE});
//        String idCount = getFormmatedNumber(tccomps.length + 1, "0000");
//        ccnID = ccnID + idCount;
        
        // [NoSR][2016.03.02][jclee] Engine Product CCN No 채번 시 12자리로 자리수가 결정될 경우 Seq No가 2자리 밖에 생성되지 않는 현상 발생 (Engine Project Code가 6자리로 총 Length가 14자리가 되어야함)
        // 해결방법 1. CCN No Prefix Length + 4로 뒤 Seq No의 자리수를 4로 확정지어줌
        // 해결방법 2. Project Code가 Engine Project Code일 경우 총 자리수를 14자리로 확정지어줌
        //  : 이 중 해결방법 1을 적용. -> Engine Project Code인지 아닌지를 확인하는 로직이 추가되면 속도에도 문제가 있을뿐 아니라
        //    13 혹은 14자리 이상의 CCN No가 나와야할 경우 또 로직을 변경해야 하므로 Seq No를 4자리로 확정지어주는 방식 채택.
        // ccnID = SYMTcUtil.getNewID(ccnID, 12);
        ccnID = SYMTcUtil.getNewID(ccnID, ccnID.length() + 4);
        return ccnID;
    }

    public static TCComponentItemRevision createCCNItem(String releaseCCNType, HashMap<String,Object> propMap) throws Exception
    {
        try {
            String year_YY = CustomUtil.getTODAY().substring(2, 4);
            String month_MM = CustomUtil.getTODAY().substring(5, 7);
            String projectCode = propMap.get(PropertyConstant.ATTR_NAME_PROJCODE).toString();
            String systemCode = propMap.get(PropertyConstant.ATTR_NAME_BL_BUDGETCODE).toString().substring(0, 3);

            String ospecCode = getOspecId(projectCode);

            String ccnID = getCCNId(projectCode.substring(0, 4), systemCode);
            
            // [20161014][ymjang] 변경사유 누락 수정
            String object_desc = propMap.get(PropertyConstant.ATTR_NAME_ITEMDESC) == null ? "" : propMap.get(PropertyConstant.ATTR_NAME_ITEMDESC).toString();

            //[UPGRADE][20240308] CCN 생성 오류 수정
//            TCComponentItem ccnItem = CustomUtil.createItem(TypeConstant.S7_PRECCNTYPE, ccnID, CommonConstant.CCNINITREVISIONNO, ccnID, object_desc);
            
			//Item Property 속성 입력
			Map<String, String> itemPropMap = new HashMap<>();
			Map<String, String> itemRevsionPropMap = new HashMap<>();
			itemPropMap.put(IPropertyName.ITEM_ID, ccnID);
			itemPropMap.put(IPropertyName.OBJECT_NAME, ccnID);
			itemPropMap.put(IPropertyName.OBJECT_DESC, object_desc);
			//Item Revision 속성 입력
			itemRevsionPropMap.put(IPropertyName.ITEM_REVISION_ID, CommonConstant.CCNINITREVISIONNO);
            
			//CCN Item 생성
        	TCSession session = getTCSession();
			TCComponentItem ccnItem = (TCComponentItem) CustomUtil.createItemObject(session, TypeConstant.S7_PRECCNTYPE, itemPropMap, itemRevsionPropMap);
			
            TCComponentItemRevision ccnRevision = ccnItem.getLatestItemRevision();
            
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_PROJCODE, projectCode);
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_SYSTEMCODE, systemCode);
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_PROJECTTYPE, releaseCCNType);
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_OSPECNO, ospecCode);
            ccnRevision.setProperty(PropertyConstant.ATTR_NAME_GATENO, (String) propMap.get(PropertyConstant.ATTR_NAME_GATENO));

            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_REGULATION, (boolean) propMap.get(PropertyConstant.ATTR_NAME_REGULATION));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_COSTDOWN, (boolean) propMap.get(PropertyConstant.ATTR_NAME_COSTDOWN));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_ORDERINGSPEC, (boolean) propMap.get(PropertyConstant.ATTR_NAME_ORDERINGSPEC));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, (boolean) propMap.get(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, (boolean) propMap.get(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_STYLINGUPDATE, (boolean) propMap.get(PropertyConstant.ATTR_NAME_STYLINGUPDATE));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_WEIGHTCHANGE, (boolean) propMap.get(PropertyConstant.ATTR_NAME_WEIGHTCHANGE));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, (boolean) propMap.get(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE));
            ccnRevision.setLogicalProperty(PropertyConstant.ATTR_NAME_THEOTHERS, (boolean) propMap.get(PropertyConstant.ATTR_NAME_THEOTHERS));

            return ccnRevision;
        } catch (Exception ex) {
             throw ex;
        }
    }

    
    /**
     * CCN 을지에 VARCHAR2 로 저장된 날짜를 (예:1982-10-27 04:22) 예시 형식으로 바꾸어 리턴한다
     * @param dbDate
     * @return
     * @throws TCException
     */
    public static String getChangeCCNDBDate(String dbDate) throws TCException{
        if (null == dbDate  || dbDate.equals("")) {
            return "";
        }
        return dbDate.substring(0, 4) + "-" + dbDate.substring(4, 6) + "-" + dbDate.substring(6, 8) + " " + dbDate.substring(8, 10) + ":" + dbDate.substring(10, 12);
    }
    
    /**
     * CCN 에 연결되어 있는 ProjectID 를 통해 PreProduct 를 가져와서 OspecID 를 리턴한다
     * @param projectCode
     * @return
     * @throws Exception
     */
    public static String getOspecId(String projectCode) throws Exception {
        TCComponent[] tcComponents = SDVPreBOMUtilities.queryComponent("__SYMC_S7_PreProductRevision", new String[]{"Project Code"}, new String[]{projectCode});
        if (null != tcComponents && tcComponents.length > 0) {
            TCComponentItemRevision productRevision = null;
            for (TCComponent tcComponent : tcComponents) {
                if (tcComponent.getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)) {
                    productRevision = SYMTcUtil.getLatestReleasedRevision(((TCComponentItemRevision)tcComponent).getItem());
                    break;
                }
            }
            return productRevision.getProperty(PropertyConstant.ATTR_NAME_OSPECNO);
        }
        return null;
    }
    
}