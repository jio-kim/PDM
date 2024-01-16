package com.ssangyong.common.utils;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.im.InputContext;
import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
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
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.internal.util.BundleUtility;

import com.ssangyong.Activator;
import com.ssangyong.common.SYMCClass;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.SOAGenericCreateHelper;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevisionType;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
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
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentTask;
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
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;

/**
 * Common Utility Class
 * [SR140616-019][20140619][bskwak] 비정규 품번 Concept 단계 품번생성 오류 개선 : Stage 구분 없이 모두 기본 revision 초기값 사용하는 것으로 변경 함.
 * => 영문 Rev이 아닌 숫자 rev을 사용하는 logic으로 대체.
 * [SR140324-030][20140625] KOG DEV Veh. Part Revise 할 때 기존 Typed Reference Object에 SES Spec No. 값 복사.
 * [SR141106-036][2014.11.17][jclee] Part Revise 시 Change Description 공란처리
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused", "restriction"})
public class CustomUtil {
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
        String sProcessStageList = components.getProperty("process_stage_list");
        String sProcessStage = "";
        TCComponent[] process_stage_list = components.getReferenceListProperty("process_stage_list");
        
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
        if (components.getProperty("release_status_list").equalsIgnoreCase("") && !components.getProperty("process_stage_list").equalsIgnoreCase("")) {
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
            targetText = CustomUtil.getTCSession().getTextService().getTextValues(sourceText);
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
            targetText = CustomUtil.getTCSession().getTextService().getTextValue(sourceText);
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
    
    public static String getTextServerString(String propertyName) {
        return getTextServerString(getTCSession(), propertyName);
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

                    TCComponent[] comps = CustomUtil.queryComponent("Item Revision...", new String[] { "ItemID", "Revision" }, new String[] { itemID, itemRev });

                    if (comps != null && comps.length == 1) {
                        property.setReferenceValue(comps[0]);
                    }

                }
                // Item인 경우
                else {
                    TCComponent[] comps = CustomUtil.queryComponent("Item...", new String[] { "ItemID" }, new String[] { strValue });

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
        if (checkString == null || "".equals(checkString) || "null".equalsIgnoreCase(checkString))
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

                newItem = CustomUtil.createItem(type, CustomUtil.getNextItemId(type), revID, name, desc);

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

    /**
     * Target Stage 값이 'P'인경우
     * Deferent SaveAs시 하위 1Level Child에서 Working상태가 존재하면 안됨.
     * 
     * @param targetRevision
     * @return
     */
    public static String validateSaveAs(TCComponentItemRevision targetRevision) {
        StringBuffer szMessage = new StringBuffer();
        TCComponentBOMWindow bomWindow = null;
        try {

            TCComponentItemRevision latestReleseRev = SYMTcUtil.getLatestReleasedRevision(targetRevision.getItem());
            if (latestReleseRev == null) {
                szMessage.append("Please Select Latest Released Revision.\n");
                return szMessage.toString();
            }

            if (!targetRevision.equals(latestReleseRev)) {
                szMessage.append("Please Select Latest Released Revision.\n");
                return szMessage.toString();
            }

            String strTargetStage = targetRevision.getProperty("s7_STAGE");
            // Target Stage 값이 'P'인 경우만 해당
            if (!"P".equals(strTargetStage))
                return "";

            TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) targetRevision.getSession().getTypeComponent("BOMWindow");

            // Latest Working Revision Rule
            TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(targetRevision.getSession(), "Latest Working");
            bomWindow = windowType.create(revRule);

            // if( bomWindow instanceof SYMCBOMWindow)
            // {
            // ((SYMCBOMWindow)bomWindow).skipHistory(true);
            // }

            TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, (TCComponentItemRevision) targetRevision, null, null);
            TCComponent[] childLines = topLine.getRelatedComponents("bl_child_lines");
            for (int j = 0; j < childLines.length; j++) {
                TCComponentBOMLine cLine = (TCComponentBOMLine) childLines[j];

                TCComponentItemRevision childRevision = cLine.getItemRevision();
                String strChildStage = childRevision.getProperty("s7_STAGE");
                // Standard part인 경우 Stage 가 null인 경우가 존재하므로 이부분 검사를 skip. (뒷 단에 영향 없음으로 판단됨 From 심영주) : 20130617
                if (!"P".equals(strChildStage) && !SYMCClass.S7_STDPARTREVISIONTYPE.equals(childRevision.getType())) {
                    szMessage.append("'" + childRevision + "' Child Part Stage '" + strChildStage + "'\n");
                }

                if (CustomUtil.isWorkingStatus(childRevision)) {
                    szMessage.append("'" + childRevision + "' Child Part is Working State\n");
                }

            }

        } catch (Exception e) {
            szMessage.append(e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (bomWindow != null) {
                    bomWindow.close();
                    bomWindow = null;
                }

            } catch (TCException e) {
                e.printStackTrace();
            }
        }

        return szMessage.toString();
    }

    /**
     * Revise 대상이 P Stage 인 경우
     * 역전개 수행한 Stage가 P/Working인 상위 Part의 ECO No.가 다른경우 Revise 수행 불가
     * 
     * @param targetRevision
     * @return
     */
    public static String validateRevise(TCComponentItemRevision targetRevision, String strECONo) {
        StringBuffer szMessage = new StringBuffer();

        try {
            String strTargetStage = targetRevision.getProperty("s7_STAGE");
            // Target Stage 값이 'P'인 경우만 해당
            if (!"P".equals(strTargetStage))
                return "";

            // Latest Working Revision Rule
            TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(targetRevision.getSession(), "Latest Working");

            TCComponent[] imanComps = targetRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
            for (int i = 0; i < imanComps.length; i++) {
                if (imanComps[i].getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || imanComps[i].getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)) {
                    // Parent Stage
                    String strParentStage = imanComps[i].getProperty("s7_STAGE");

                    // Working, 'P' Stage인 경우
                    if (CustomUtil.isWorkingStatus(imanComps[i]) && "P".equals(strParentStage)) {
                        TCComponent ecoRev = imanComps[i].getReferenceProperty("s7_ECO_NO");

                        // ECO가 없으면 Error
                        if (ecoRev == null) {
                            szMessage.append("'" + imanComps[i] + "' Parent Part ECO No. is Null\n");
                            continue;
                        }

                        String strParentEcoID = ecoRev.getProperty("item_id");

                        // 상위 ECO NO.와 일치하지 않으면 Error
                        if (!strParentEcoID.equals(strECONo)) {
                            szMessage.append("'" + imanComps[i] + "' Parent Part ECO No(" + strParentEcoID + ") Does Not Match\n");
                            continue;
                        }
                    }
                }
            }
        } catch (Exception e) {
            // e.printStackTrace();
            szMessage.append(e.getMessage());
        }

        // SaveAs시 하위 1Level Child에서 Working상태가 존재하면 안됨.

        return szMessage.toString();
    }

    // ////////////////////////////////////////

    /**
     * 선택된 TCComponentItemRevision을 개정한다.
     * 이때, 개정은 saveAs로 하며 DeepCopy = true이다.
     * 
     * @param targetRevision
     *            개정 대상 ItemRevision
     * @param is3DCheck
     *            개정 Dialog에서 3D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
     * @param is2DCheck
     *            개정 Dialog에서 2D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
     * @param isSoftwareCheck
     *            개정 Dialog에서 Software Check여부(선택시 true). true이면 개체복사 아니면 복사하지 않는다.
     * @param isDeepCopy
     *            deepCopy 여부
     * @param desc
     * @param stage
     * @throws Exception
     */
    public static TCComponentItemRevision reviseForItemRev(TCComponentItemRevision targetRevision, boolean is3DCheck, boolean is2DCheck, boolean isSoftwareCheck, boolean isDeepCopy, String desc, String stage, TCComponentItemRevision ecoRevision) throws Exception {

        TCComponentItemRevision newRevision = null;
        Markpoint mp = new Markpoint(targetRevision.getSession());

        try {
            String ecoNo = "";
            if (ecoRevision != null) {
                ecoNo = ecoRevision.getProperty("item_id");
            }

            /** Revise */

            Map<String, String> resultMap = setItemInfoForRevise(targetRevision, stage);
            String partName = resultMap.get("partName");
            String revId = resultMap.get("revId");

            /** 속성값으로 인해 revise에서 saveAs로 변경 **/
            newRevision = targetRevision.saveAs(revId, partName, desc, isDeepCopy, null);
            newRevision.setProperty("s7_STAGE", stage);
            newRevision.setProperty("s7_MATURITY", "In Work");
            
            /**
             * [SR141106-036][2014.11.17][jclee] Veh Part Revise 시 Change Description 공란처리
             */
            if (newRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE)) {
            	newRevision.setProperty("s7_CHANGE_DESCRIPTION", "");
			}

            TCComponent refComp = targetRevision.getReferenceProperty("s7_Vehpart_TypedReference");
            if (refComp != null) {
                String strActWeight = refComp.getProperty("s7_ACT_WEIGHT");
                String strTargetWeight = refComp.getProperty("s7_TARGET_WEIGHT");
                String strBoundingBox = refComp.getProperty("s7_BOUNDINGBOX");
                // 추후에 Target Weight 값도 Part Revise 시 가져 갈 수 있도록 추가 
                // String strTargetWeight = refComp.getProperty("s7_TARGET_WEIGHT");l
                // [SR140324-030][20140625] KOG DEV Veh. Part Revise 할 때 기존 Typed Reference Object에 SES Spec No. 값 복사.
                String strSESSpecNo = refComp.getStringProperty("s7_SES_SPEC_NO");

                TCComponent newRefComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX", "s7_SES_SPEC_NO" }, new String[] { strActWeight, strTargetWeight, strBoundingBox, strSESSpecNo });

                newRevision.setReferenceProperty("s7_Vehpart_TypedReference", newRefComp);

            } else {
                // [SR140324-030][20140625] KOG DEV Veh. Part Revise 할 때 기존 Typed Reference Object가 없을 경우 SES Spec No. Empty값 생성.
                TCComponent newRefComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX", "s7_SES_SPEC_NO" }, new String[] { "0", "0", "", "" });

                newRevision.setReferenceProperty("s7_Vehpart_TypedReference", newRefComp);
            }


            /** PartRevision 속성에 ECO를 Reference Type으로 넣는다. **/
            if (ecoRevision != null) {
                newRevision.setReferenceProperty("s7_ECO_NO", ecoRevision);
            }

//            newRevision.setProperty("s7_STAGE", stage);
//            newRevision.setProperty("s7_MATURITY", "In Work");
//            
//            newRevision.lock();
//            newRevision.save();
            
            /** Dataset를 Rule 에 따라 연결한다. **/
            /**
             * [20140112][jclee] TC10 Upgrade 관련 수정.
             *  - Dataset 연결 전 Save 수행. (The instance is not locked 에러 대응)
             */
            newRevision.save();
            newRevision = relateDatasetToItemRevision(targetRevision, newRevision, is3DCheck, is2DCheck, isSoftwareCheck, ecoRevision, true);
//            newRevision.setProperty("s7_STAGE", stage);
//            newRevision.setProperty("s7_MATURITY", "In Work");
            
//            /**
//             * [20140108][jclee]
//             * 하위 구조가 존재하는 경우에만 Save 작업 수행. 그 외에는 에러가 남.
//             * Save를 수행하지 않을 경우 하위 Part Revise 시 이전 Revision의 값으로 되돌아가는 현상이 생김.
//             */
//            AIFComponentContext[] aifChildren = newRevision.getChildren();
//            for (int inx = 0; inx < aifChildren.length; inx++) {
//            	String sChildType = aifChildren[inx].getComponent().getProperty("object_type");
//            	if (sChildType.equals("BOMView Revision")) {
//            		newRevision.save();
//				} else {
//					newRevision.save();
//				}
//			}
            
//			newRevision.save();
            
            mp.forget();
            mp = null;  
        }// end try
        catch (Exception e) {
            e.printStackTrace();
            if (mp != null) {
                mp.rollBack();
                mp = null;
            }

            throw new Exception(e.getMessage());
        } finally {
            if (mp != null) {
                mp.rollBack();
                mp = null;
            }
            
        }

        return newRevision;
    }

    /**
     * Revise시 itemRevision에 연결된 Dataset을 조건에 따라 Object Copy or Reference Copy 한다.
     * Revise Dialog에서 Dataset의 Check여부에 따라 Object Copy( check시) , Reference Copy( uncheck시) 를 진행한다.
     * MS-Offect계열의 dataset은 Check여부에 관계없이 항상 Object Copy를 한다.
     * 
     * [SR140723-026][20140715] swyoon, ItemRevision 하위 Dataset들의 Relation 가져오는 공통함수 로직 오류 수정
     * 
     * @param targetRevision
     *            Revise할 item
     * @param newRevision
     *            Revise된 item
     * @param is3DCheck
     *            개정 Dialog에서 3D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
     * @param is2DCheck
     *            개정 Dialog에서 2D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
     * @param isSoftwareCheck
     *            개정 Dialog에서 Software Check여부(선택시 true). true이면 개체복사 아니면 복사하지 않음.
     * @param ecoNo
     *            newRevision에 연결될 eco의 item_id
     *            Dataset의 속성값으로 set됨.
     * @return
     * @throws Exception
     */
    public static TCComponentItemRevision relateDatasetToItemRevision(TCComponentItemRevision targetRevision, TCComponentItemRevision newRevision, boolean is3DCheck, boolean is2DCheck, boolean isSoftwareCheck, TCComponentItemRevision ecoRevision, boolean isSucceeded) throws Exception {
        Registry registry = Registry.getRegistry("com.ssangyong.commands.revise.revise");
        AIFComponentContext[] contexts =  targetRevision.getChildren();
        TCComponent[] relatedComponents = null;
        if( contexts != null){
            relatedComponents = new TCComponent[contexts.length];
            for( int i = 0; i < contexts.length; i++ ){
                relatedComponents[i] = (TCComponent)contexts[i].getComponent();
            }
        }
//        TCComponent[] relatedComponents = targetRevision.getRelatedComponents();
        boolean isNotAllChecked = !is3DCheck && !is2DCheck && !isSoftwareCheck ? true : false;

        String ecoNo = "";
        if (ecoRevision != null) {
            ecoNo = ecoRevision.getProperty("item_id");
        }

        // Dataset의 Type 중 Catia v4가 있을 경우에는 Part Revision의 S7_CAT_V4_TYPE 속성 확인 후
        // 속성이 3D이고, Revise Dialog에서 선택한 속성이 2D 일 경우에는 에러 메세지를 띄우고 Dialog를 종료한다.
        for (int i = 0; i < relatedComponents.length; i++) {
            if (relatedComponents[i] instanceof TCComponentDataset) {
                String type = relatedComponents[i].getType();

                if (type.equals("catia")) {
                    String v4Type = targetRevision.getProperty("s7_CAT_V4_TYPE");
                    if (!is3DCheck && is2DCheck) {
                        if (v4Type.equals("3D")) {
                            // String errMsg = "선택된 아이템의 속성[CAT.V4 Type]의 Type(3D)과 Revise Dialog에서 선택된 Type(2D)이 서로 다릅니다. ";
                            String errMsg = registry.getString("ReviseDialog.MESSAGE.WrongSelected");
                            MessageBox.post(errMsg, "Error", MessageBox.ERROR);
                            throw new Exception(errMsg);
                        }
                    }
                }
            }
        } // end for

        /** Dataset과 ItemRevision의 relation은 IMAN_specification으로 한다. **/
        String relation_Name = SYMCClass.SPECIFICATION_REL; // IMAN_specification

        /** Part의 item_id와 revision 얻기 */
        String newRevId = newRevision.getProperty("item_revision_id");

        for (int i = 0; i < relatedComponents.length; i++) {
            if (relatedComponents[i] instanceof TCComponentDataset) {

                String type = relatedComponents[i].getType();
                boolean isOfficeDataset = type.equals("MSExcel") || type.equals("MSExcelX") || type.equals("MSPowerPoint") || type.equals("MSPowerPointX") || type.equals("MSWord") || type.equals("MSWordX") ? true : false;
                boolean isReferenceDataset = relatedComponents[i].getProperty("object_name").indexOf("Reference") > -1;
                boolean isCorrectionsDataset = relatedComponents[i].getProperty("object_name").indexOf("Corrections") > -1;
                // JT파일은 IMAN_Rendering 관계
                if (type.equals("DirectModel")) {
                    // 3D Check 상태인 경우 개체복사, JT는 Copy하지 않음
                    if (is3DCheck) {
                        /** Object Copy */
                        // newRevision.add("IMAN_Rendering", updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
                    } else {
                        /** Reference Copy */
                        newRevision.add("IMAN_Rendering", relatedComponents[i]);
                    }

                    continue;
                }

                if (type.equals("CATCache")) {

                    continue;
                }

                // PDF 파일인 경우
                if (type.equals("PDF")) {
                    // 2D Check 상태인 경우 개체복사, PDF는 Copy하지 않음
                    if (is2DCheck) {
                        /** Object Copy */
                        // newRevision.add("IMAN_Rendering", updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
                    	
                    	// [SR150325-015][2015.04.08][jclee] 보정 내역 PDF의 경우 2D 도면을 체크해도 Revise시 Reference Type으로 붙여줌.
                    	if (relatedComponents[i] != null && isCorrectionsDataset) {
                    		newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
                    	}
                    } else {
                        /** Reference Copy */
                        // [SR140702-061][20140626] KOG Reference Copy Relation Name 변경 (IMAN_reference).
                        newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
                    }

                    continue;
                }
                
                // [SR없음][2015.06.26][jclee] Dataset Name이 Reference로 끝나는 경우 Check 여부 및 Dataset Type을 불문하고 개체복사 수행
                if (isReferenceDataset) {
                	newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
				} else {
					/** 선택된 CheckBox가 없으면 Office 종류를 제외하고 모두 Reference Copy를 한다. **/
					/** Offect 관련 파일은 무조건 Object Copy이다. **/
					if (isNotAllChecked) {
						if (isOfficeDataset) {
							newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
						} else {
							/** Office 이외의 파일은 참조복사한다. **/
							// [SR140702-061][20140626] KOG Reference Copy Relation Name 변경 (IMAN_reference).
							newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
						}
					} else {
						/** 3D, 2D, Software 셋 중 하나라도 선택되었을 경우.. **/
						if (type.equals("CATPart") || type.equals("CATProduct")) {
							if (is3DCheck) {
								/** Object Copy */
								newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
							} else {
								/** Reference Copy */
								// [SR140702-061][20140626] KOG Reference Copy Relation Name 변경 (IMAN_reference).
								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
							}
						} // end if
						else if (type.equals("CATDrawing")) {
							if (is2DCheck) {
								/** Object Copy */
								newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
							} else {
								/** Reference Copy */
								// [SR140702-061][20140626] KOG Reference Copy Relation Name 변경 (IMAN_reference).
								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
							}
						} else if (type.equals("PDF") || type.equals("TIF") || type.equals("HPGL")) {
							/** 2D 체크가 되어 있을 경우에는 CATDrawing Type만 신규 생성한다. **/
							if (!is2DCheck) {
								/** Reference Copy */
								// [SR140702-061][20140626] KOG Reference Copy Relation Name 변경 (IMAN_reference).
								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
							} else {
								// [SR150325-015][2015.04.08][jclee] 보정 내역 PDF의 경우 2D 도면을 체크해도 Revise시 Reference Type으로 붙여줌.
								if (relatedComponents[i] != null && isCorrectionsDataset && type.equals("PDF")) {
									newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
								}
							}
						} else if (isOfficeDataset) {
							/** Object Copy */
							newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
						} else if (type.equals("Zip")) {
							if (isSoftwareCheck) {
								newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
							}
						} else if (type.equals("catia")) {
//							if (is3DCheck || is2DCheck) {
//								newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
//							} else {
//								// [SR140702-061][20140626] KOG Reference Copy Relation Name 변경 (IMAN_reference).
//								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
//							}
							//[20180719][CSH]기술관리 어경식 책임 3D Check 시 nocopy... v5로 변환해서 업로드 해야하므로... 나머지는 reference copy  
							if (!is3DCheck) {
								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
							}
						}
					}
				}

            }
        } // end for

        return newRevision;
    }

    /**
     * Dataset 생성 후 연결되 Part의 Rev Id와 ECO No를 Dataset 속성에 Update 한다.
     * [20140112][jclee] TC10 Upgrade 대응.
     *  - Dataset Save As 시 에러 발생. (CXPOM_wrong_class)
     *  - 신규 Dataset 생성 후 Named Reference 생성 및 파일 첨부
     * 
     * @param relatedComponent
     * @param ecoNo
     * @param newRevId
     * @return
     * @throws Exception
     * @throws Exception
     */
    public static TCComponentDataset updateDataSetForEcoNoAndNewRev(TCComponentItemRevision newRevision, TCComponent relatedComponent, String ecoNo, String newRevId, boolean isSucceeded) throws Exception {
        String dataSetName = getDataSetName(newRevision.getProperty("item_id"), newRevId, relatedComponent.getProperty("object_name"), isSucceeded);
        String dataSetType = "";
        
        TCComponentDataset revDataSet = null;
        TCSession session = getTCSession();
        TCComponentTcFile[] tcFiles = null;
    	try {
//    		revDataSet = ((TCComponentDataset) relatedComponent).saveAs(dataSetName, newRevId, newRevId);

    		// 첨부파일 리스트 추출
    		tcFiles = SYMTcUtil.getImanFile((TCComponentDataset)relatedComponent);
    		Vector<File> importFiles = new Vector<File>();
    		String sEXT = "";
    		
    		for (int inx = 0; inx < tcFiles.length; inx++) {
    			File file = tcFiles[inx].getFile(null);
    			importFiles.add(file);
    			sEXT = getExtension(file);
    			
    			// TODO 첨부파일 확장자명으로 Dataset Type을 결정하는 방법 외에 기존 Dataset에서 Type을 가져오는 방법이 있을 경우 해당 로직으로 변경할 것.
    			if (sEXT.equalsIgnoreCase("CATProduct") || sEXT.equalsIgnoreCase("CATPart") || sEXT.equalsIgnoreCase("CATDrawing")) {
    				dataSetType = sEXT;
				}
    			// 수정 : bc.kim
    			// 수정 내용 : CATIA V4 에 해당 하는 파일 Revise 시 해당 파일의 Type 이 없어서 추가 
    			//             위의 모든 Validation 로직에 해당 되는 Type 이 없는 경우 해당 Dataset 의 Type 을 추출 하여 그대로 적용
    			else {
    				dataSetType = ((TCComponentDataset)relatedComponent).getType();
    			}
			}
    		
    		if (sEXT == null || sEXT.isEmpty() || sEXT.equals("") || sEXT.length() == 0 || importFiles.isEmpty() || importFiles.size() == 0) {
				throw new Exception("CATIA File is not imported in Dataset.");
			}
    		
    		TCComponentDatasetType componentDatasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
    		revDataSet = componentDatasetType.create(dataSetName, dataSetName, dataSetType);
    		SYMTcUtil.importFiles(revDataSet, importFiles);
    		
    		/** ECO No를 Dataset에 저장한다. */
    		revDataSet.setProperty("s7_ECO_NO", ecoNo);
    		/** Part Revision의 Rev ID 값을 Dataset에 저장한다. */
    		revDataSet.setProperty("s7_REVISION_ID", newRevId);
		} catch (Exception e) {
			throw new Exception("데이터셋 복제 중 오류 발생.\n기술관리팀에 문의 하세요.");
		}

        return revDataSet;
    }

    /**
     * DataSetName / DataSetRevisionID - ParentItemRevisionID
     * 
     * @Copyright : S-PALM
     * @author : 권오규
     * @since : 2012. 12. 20.
     * @param dataSetName
     * @return
     * @throws Exception
     */
    private static String getDataSetName(String strItemID, String revId, String dataSetName, boolean isSucceeded) throws Exception {
        if (dataSetName.indexOf("/") == -1) {
            String errMsg = "Dataset의 Name형식이 잘못되었습니다. 관리자에게 문의하세요.!!";
            MessageBox.post(errMsg, "Error", MessageBox.ERROR);
            throw new Exception(errMsg);
        }

        if (dataSetName.indexOf(";") > 0) {
            dataSetName = dataSetName.substring(0, dataSetName.indexOf(";"));
        }

        // 이전 Dataset Revision ID를 승계하지 않는 경우
        if (!isSucceeded) {
            dataSetName = strItemID + "/" + revId;
            return dataSetName;
        }

        if (dataSetName.contains("/") && dataSetName.contains("-")) {
            String dataSetRevId = dataSetName.substring(dataSetName.lastIndexOf("-") + 1, dataSetName.length());

            dataSetName = strItemID + "/" + revId + "-" + getNextDataSetRevID(dataSetRevId);
        } else if (!dataSetName.contains("-")) {
            dataSetName = strItemID + "/" + revId + "-" + "A";
        }
        return dataSetName;
    }

    /**
     * 선택한 아이템리비전의 정보를 화면에 셋팅
     * 
     * @Copyright : S-PALM
     * @author : 권오규
     * @return
     * @since : 2012. 12. 20.
     */
    public static Map<String, String> setItemInfoForRevise(TCComponentItemRevision component, String strStage) {
        Map<String, String> resultMap = new HashMap<String, String>();
        try {
            String strRevType = component.getType();
            String strCurrentRevID = component.getProperty("item_revision_id");
            String strCurrentStage = component.getProperty("s7_STAGE");
            String strNextRevID = "";
            // String strStage = "";
            if (SYMCClass.S7_VEHPARTREVISIONTYPE.equals(strRevType)) {

                // Stage 값이 'P'인 경우 Revision ID는 숫자 3자리(001->999)
                if ("P".equals(strStage)) {
                    // target revision이 'P' 이면
                    if ("P".equals(strCurrentStage)) {
                        strNextRevID = CustomUtil.getNextRevID(component.getItem(), new String("Item"));
                    }
                    // Stage값이 'D' -> 'P'로 변경된다면
                    else {
                        strNextRevID = "000";
                    }

                }
                // Stage 값이 'P'가 아닌 경우 Revision ID는 문자 2자리(A->ZZ)
                // [SR140616-019][20140619][bskwak] 비정규 품번 Concept 단계 품번생성 오류 개선 : Stage 구분 없이 모두 기본 revision 초기값 사용하는 것으로 변경 함.
                // => 영문 Rev이 아닌 숫자 rev을 사용하는 logic으로 대체.
                else {
                    // strNextRevID = CustomUtil.getNextCustomRevID(strCurrentRevID);
                    strNextRevID = CustomUtil.getNextRevID(component.getItem(), new String("Item"));
                }
            } else {
                strNextRevID = CustomUtil.getNextRevID(component.getItem(), new String("Item"));
            }

            // String partID = component.getProperty("item_id");
            String partName = component.getProperty("object_name");

            // resultMap.put("partId", partID);
            resultMap.put("revId", strNextRevID);
            resultMap.put("partName", partName);
            // resultMap.put("stage", strStage);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    private static String getNextDataSetRevID(String revId) {
        Vector<String> temp = new Vector<String>();
        String nextRevId = "";
        boolean next = true;

        for (int i = revId.length(); i > 0; i--) {
            if (revId.charAt(i - 1) != 'Z') {
                if (next) {
                    int intchar = revId.charAt(i - 1);
                    intchar++;
                    char[] char1 = Character.toChars(intchar);
                    if (revId.length() > 1) {
                        temp.add(String.valueOf(char1[0]));
                        next = false;
                    } else {
                        temp.add(String.valueOf(char1[0]));
                    }
                } else {
                    temp.add(String.valueOf(revId.charAt(i - 1)));
                    next = false;
                }
            } else if (revId.charAt(i - 1) == 'Z') {
                if (next) {
                    if (i == 1) {
                        temp.add("AA");
                    } else {
                        temp.add("A");
                        next = true;
                        ;
                    }
                } else {
                    temp.add("Z");
                }
            } else {
                return revId;
            }
        }

        for (int i = temp.size(); i > 0; i--) {
            nextRevId += temp.get(i - 1);
        }
        return nextRevId;
    }

    /**
     * Item revision 정렬을 위한 class.
     * 정렬 기준 : Item ID ascending, Item revision ascending
     * 20140128 by bskwak
     */
    public static Comparator<TCComponent> comparatorItemRev = new Comparator<TCComponent>() {
        public int compare(TCComponent obj1, TCComponent obj2) {
            int iVar = 0;
            try {
                String obj1_ItemId = ((TCComponentItemRevision) obj1).getProperty("item_id");
                String obj2_ItemId = ((TCComponentItemRevision) obj2).getProperty("item_id");

                String obj1_Rev = ((TCComponentItemRevision) obj1).getProperty("item_revision_id");
                String obj2_Rev = ((TCComponentItemRevision) obj2).getProperty("item_revision_id");

                iVar = obj1_ItemId.compareTo(obj2_ItemId);
                if (iVar == 0)
                    iVar = obj1_Rev.compareTo(obj2_Rev);
            } catch (Exception e) {
                iVar = 0;
            }

            return iVar;
        }
    };

    /**
     * Dataset Reference File copy
     * 
     * @method copyDatasetReferenceFile
     * @date 2014. 4. 14.
     * @param
     * @return TCComponentItemRevision
     * @exception
     * @throws
     * @see
     */
    public static void renameDatasetReferenceFile(TCComponentItemRevision revision) throws Exception {
        TCComponent[] relatedComponents = revision.getRelatedComponents();
        for (int i = 0; i < relatedComponents.length; i++) {
            if (relatedComponents[i] instanceof TCComponentDataset) {
                // String type = relatedComponents[i].getType();
                TCComponentDataset dataset = (TCComponentDataset) relatedComponents[i];
                renameOriginalImanFileFromItemInfo(revision, dataset);
            }
        }
    }

    /**
     * Named Reference File을 Item정보를 가지고 ImanFile original filename을 rename한다.
     * 
     * @method renameOriginalImanFileFromItemInfo
     * @date 2014. 4. 14.
     * @param
     * @return ArrayList<File>
     * @exception
     * @throws
     * @see
     */
    public static ArrayList<File> renameOriginalImanFileFromItemInfo(TCComponentItemRevision revision, TCComponentDataset dataset) throws Exception {
        ArrayList<File> datasetFiles = new ArrayList<File>();
        TCComponentTcFile[] tcFiles = dataset.getTcFiles();
        // [SR없음][20150710][jclee] Corrections, Reference Dataset의 경우 Rename하지 않도록 수정
        boolean isReferenceDataset = dataset.getProperty("object_name").indexOf("Reference") > -1;
        boolean isCorrectionsDataset = dataset.getProperty("object_name").indexOf("Corrections") > -1;
        
        if (!(isReferenceDataset || isCorrectionsDataset)) {
        	for (int j = 0; j < tcFiles.length; j++) {
        		
        		TCComponentTcFile[] imanFiles = dataset.getTcFiles();
        		for (TCComponentTcFile tcComponentTcFile : imanFiles) {
        			String renameOriginalFileName = getReNameFileName(revision, dataset, tcComponentTcFile.getProperty("original_file_name"));
        			tcComponentTcFile.setOriginalFileName(dataset, renameOriginalFileName);
        		}
        	}
        }
        
        return datasetFiles;
    }

    /**
     * Reference File Name을 현재 Revision의 Item, ItemRevision에 맞게 변경한다.
     * 
     * ITEM : 30100CD000
     * REV : 002
     * FILE_NAME : PTPART_30100CD000_001_--B_DETAIL_41419C52C5040CAF_41419E1C48DF46FC.pdf (리비젼은 001 상태)
     * ->
     * FILE_NAME : 30100CD000_002_--B_DETAIL.pdf (리비젼 002 상태 변경)
     * 
     * @method getReNameFileName
     * @date 2014. 4. 14.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getReNameFileName(TCComponentItemRevision revision, TCComponentDataset dataset, String originalFileName) throws Exception {
        String itemId = revision.getProperty("item_id");
        String revId = revision.getProperty("item_revision_id");
        String upperFileName = StringUtil.nullToString(originalFileName).toUpperCase();
        String cadOriginType = ""; // MASTER, DETAIL
        // TODO: CAD PART REV(---, --A...)을 붙이지 않는다.
        // String prvPartRev = getCadDatasetRev(dataset);
        // String fileName = itemId + "_" + revId + "_" + prvPartRev;
        String fileName = itemId + "_" + revId;
        String datasetType = dataset.getType();
        // V4인 경우 Dataset 1개에 model 파일이 2개가 달려있는 경우가 있으므로 File명을 구분한다.(MASTER, DETAIL)
        if ("catia".equals(datasetType)) {
            if (upperFileName.indexOf("MASTER") > -1) {
                cadOriginType = "MASTER";
            } else if (upperFileName.indexOf("DETAIL") > -1) {
                cadOriginType = "DETAIL";
            }
            if (!"".equals(cadOriginType)) {
                fileName += "_" + cadOriginType;
            }
        }
        // file 확장자 add
        // 30100CD000_002_--B_DETAIL + .pdf
        fileName = fileName + originalFileName.substring(originalFileName.lastIndexOf("."));
        return fileName;
    }

    /**
     * Dataset Name에서 VPM CAD REV정보를 얻어온다.
     * 
     * @method getCadDatasetRev
     * @date 2014. 4. 14.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public static String getCadDatasetRev(TCComponentDataset dataset) throws Exception {
        String datasetName = dataset.getProperty("object_name");
        if (datasetName.indexOf("/") == -1) {
            return "---";
        }
        String[] datasetNames = datasetName.split("/");
        String cadRev = "";
        if (datasetNames.length == 2) {
            // 11111/000-A -> [11111], [000-A] -> SUCCESS
            if (datasetNames[1].indexOf("-") > -1) {
                cadRev = StringUtil.nullToString(datasetNames[1].split("-")[1]);
                if (cadRev.length() == 1) {
                    cadRev = "--" + cadRev;
                } else if (cadRev.length() == 2) {
                    cadRev = "-" + cadRev;
                }
            } else {
                // 11111/000 -> [11111], [000] -> SUCCESS
                if (datasetNames[1].length() == 3) {
                    cadRev = "---";
                }
                // 11111/000XA -> [11111], [000XA] -> ERROR & SUCCESS
                else if (datasetNames[1].length() > 3) {
                    // XA 만 추출
                    cadRev = "-" + datasetNames[1].substring(3);
                }
            }
        }
        return cadRev;
    }

    // [SR140702-058][20140701] KOG CustomUtil get Image 메소드 추가.
    public static Image getImage(String imagePath) {
        URL fullPathString = BundleUtility.find(Activator.getDefault().getBundle(), imagePath);
        return ImageDescriptor.createFromURL(fullPathString).createImage();
    }

    // [SR140702-058][20140701] KOG CustomUtil String > Date 변환 메소드 추가.
    public static Date getDateFromStringDate(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            if (strDate == null || strDate.isEmpty()) {
                return null;
            }
            return sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    // [SR140702-058][20140701] KOG CustomUtil Date > String 변환 메소드 추가.
    public static String getStringDateFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
	/**
	 *
	 * SRME:: [][20140811] swyoon  함께 사용할 수 없는 옵션인지 리턴.
	 * 
	 * @param session
	 * @param conditionStr
	 * @param bTcType
	 * @return
	 * @throws Exception
	 */
	public static boolean isCompatibleOptions (TCSession session, String conditionStr, boolean bTcType) throws Exception {

		/*
		 * 3A17, 3A46, 3A51, 3B16, 3C09, 3C61, 3D00, 3D25, 3E35, 3F02, 3W02, 3W09, 
		 * 3W16, 3W17, 3W25, 3W35, 3W46, 3W51, 3WCC, 3WDD 
		 * 
		 * EX)
		 * 	3W02:3W09	==> 1:1 
		 *  3W02:3W10
			[3W16, 3W17, 3W25, 3W35, 3W46, 3W51] ==> 각각	(미적용)
			[3C09, 3C61]:[3WCC, 3WDD]	==> 다대다	(미적용)
		 */
		
		// Teamcenter Type의 옵션이면, Simple Type(H-BOM에서 사용하는 형식)으로 변경함.
		// EX)  F82AXA2015:A50 = "A50E" and F82AXA2015:A40 = "A40D" or F82AXA2015:U10 = "U10X"
		//		==> A50E and A40D@U10X
		if( bTcType ){
			conditionStr = SYMStringUtil.convertToSimple(conditionStr);
		}
		
		conditionStr = conditionStr.trim();
		
		TCPreferenceService preferenceService = session.getPreferenceService();
		//String[] incompatibleOptions = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "SYMC_INCOMPATIBLE_OPTIONS");
		String[] incompatibleOptions = preferenceService.getStringValuesAtLocation("SYMC_INCOMPATIBLE_OPTIONS", TCPreferenceLocation.OVERLAY_LOCATION);
		
		String tmpStr = conditionStr.toUpperCase();
		tmpStr = tmpStr.replaceAll(" OR ", "@");
		String[] options = tmpStr.split("@");
		
		HashMap<String, ArrayList<String>> icMap = new HashMap();
		for( int i = 0; incompatibleOptions != null && i < incompatibleOptions.length; i++){
			
//			3W02:3W09	==> 1:1 또는 [3C09, 3C61]:[3WCC, 3WDD] 타입
			if( incompatibleOptions[i].indexOf(":") > -1){
				String[] icOptions = incompatibleOptions[i].split(":");
				if( icMap.containsKey(icOptions[0])){
					ArrayList<String> list = icMap.get(icOptions[0]);
					if( !list.contains(icOptions[1])){
						list.add(icOptions[1]);
					}
				}else{
					ArrayList<String> list = new ArrayList();
					list.add(icOptions[1]);
					icMap.put(icOptions[0], list);
				}
				
				if( icMap.containsKey(icOptions[1])){
					ArrayList<String> list = icMap.get(icOptions[1]);
					if( !list.contains(icOptions[0])){
						list.add(icOptions[0]);
					}
				}else{
					ArrayList<String> list = new ArrayList();
					list.add(icOptions[0]);
					icMap.put(icOptions[1], list);
				}
				
			}
			
		}
		
		ArrayList<String> opList = new ArrayList();
		for( int i = 0; i < options.length; i++){
			String option = options[i];
			String[] eachOption = option.split(" AND ");
			opList.clear();
			for( int j = 0; j < eachOption.length; j++){
				tmpStr = eachOption[j].trim();
				if( !opList.contains(tmpStr)){
					opList.add( tmpStr );
				}
			}
			
			for( int j = 0; j < eachOption.length; j++){
				tmpStr = eachOption[j].trim();
				if( icMap.containsKey(tmpStr)){
					ArrayList<String> valueList = icMap.get(tmpStr);
					for( String value : valueList){
						if( opList.contains(value)){
							return false;
						}
					}
				}
			}			
		}
		
		return true;
	}        

	/**
	 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
	 * @param str
	 * @return
	 */
	public static boolean isHTML(String str) {
        int length = str.length();
        int lastIndexOf = str.lastIndexOf("</html>");

        if (lastIndexOf != -1) {
            String prefix = str.substring(0, 6).trim();
            String suffix = str.substring(lastIndexOf, length).trim();

            if (prefix.equals("<html>") && suffix.equals("</html>")) {
                return true;
            }
        }

        return false;
    }

	/**
	 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
	 * @param filePath
	 * @param datasetName
	 * @return
	 * @throws Exception
	 */
    public static TCComponentDataset createDataset(String filePath, String datasetName) throws Exception {
    	
    	TCSession session = getTCSession();
    	TCComponentDatasetType datasetType = (TCComponentDatasetType) session.getTypeComponent("Dataset");
		
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
	
}
