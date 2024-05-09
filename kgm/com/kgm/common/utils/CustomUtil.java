package com.kgm.common.utils;

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

import com.kgm.Activator;
import com.kgm.common.SYMCClass;
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
import com.teamcenter.services.rac.core.DataManagementService;
import com.teamcenter.services.rac.core._2008_06.DataManagement;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateIn;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateInput;
import com.teamcenter.services.rac.core._2008_06.DataManagement.CreateResponse;

/**
 * Common Utility Class
 * [SR140616-019][20140619][bskwak] ������ ǰ�� Concept �ܰ� ǰ������ ���� ���� : Stage ���� ���� ��� �⺻ revision �ʱⰪ ����ϴ� ������ ���� ��.
 * => ���� Rev�� �ƴ� ���� rev�� ����ϴ� logic���� ��ü.
 * [SR140324-030][20140625] KOG DEV Veh. Part Revise �� �� ���� Typed Reference Object�� SES Spec No. �� ����.
 * [SR141106-036][2014.11.17][jclee] Part Revise �� Change Description ����ó��
 * [UPGRADE][20240308] reviseForItemRev ����  ����
 */
@SuppressWarnings({"rawtypes", "unchecked", "unused", "restriction"})
public class CustomUtil {
    public static int GROUP_LEADER = 0;
    public static int GROUP_MEMBER = 0;

    public static String CATETORY_EXPORT = "Export";
    public static String CATETORY_DATASET = "Dataset";

    public static final String NA_RENAME_FILE = "@@N/A/F@@";

    /**
     * Title: Item ID�� Item�� ã��<br>
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
     * �̹� ������� �ִ� Saved query�� �̿��Ͽ� imancomponent�� �˻��ϴ� method�̴�.
     * 
     * @param savedQueryName
     *            String ����� query name
     * @param entryName
     *            String[] �˻� ���� name(�������� name)
     * @param entryValue
     *            String[] �˻� ���� value
     * @return TCComponent[] �˻� ���
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
     * �˻� ��� ������ �����Ѵ�.
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
     * ����Ÿ ������ �̿��Ͽ� ���� �迭�� �����Ѵ�.
     * 
     * @Copyright : S-PALM
     * @author : ������
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
     * Title: Item ID�� Revision ID�� Item Revision�� ã��<br>
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
     * Title: Item ID�� Revision ID�� Item Revision�� ã��<br>
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
     * Title: Item�� ������<br>
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
     * Title: Ư�� Component Type�� Next Revision ID�� �����´�.<br>
     * Desc : Item�� null�� �ƴ� ��� Item�� ���� Revision ID�� ��������,null�� ��� Component�� �ʱ� Revision ID�� ������<br>
     * Usage: getNextRevID(item,"Document");
     * 
     * @param item
     *            Item or null
     * @param compType
     *            Compoent Type
     * @return newRevId Item�� ���� revision ID
     */
    public static String getNextRevID(TCComponentItem item, String compType) throws Exception {
        TCSession session = getTCSession();
        String newRevId = "";
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
        newRevId = itemType.getNewRev(item);
        return newRevId;
    }

    /**
     * Vehicle Part/Function Master Revision ID ����
     * Stage ���� "P"�� �ƴѰ�� �ش�
     * 
     * A -> Z -> AA -> ZZ
     */
    public static String getNextCustomRevID(String currentRevID) throws Exception {

        if (currentRevID == null || currentRevID.trim().equals("") || currentRevID.length() > 2)
            throw new Exception("�ùٸ��� ���� Revision ID �Դϴ�.");

        char[] szChar = currentRevID.toCharArray();

        for (int i = (szChar.length - 1); i >= 0; i--) {
            if (('A' > szChar[i]) || (szChar[i] > 'Z')) {
                throw new Exception("�ùٸ��� ���� Revision ID �Դϴ�.");
            }
        }

        if ("ZZ".equals(currentRevID))
            throw new Exception("�� �̻� �����Ͻ� �� �����ϴ�.");

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
     * Title: Ư�� Component Type�� Next Item ID�� �����´�. <br>
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
     * Title: Item Revision�� ���� Revision�� �����´�. <br>
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
                // user�� ������ revision�� first revision�� ��� ���� revision�� null�̵ȴ�.
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
     * Title: Item�� Master Form�� ������<br>
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
     * Title: Item�� Revision Master Form�� ������<br>
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
     * Title: Item�� Latest Revision Master Form�� ������<br>
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
     * Title: Item Revision �Ʒ��� DataSet�� ������<br>
     * Usage: getDatasets(itemRevision,"IMAN_specification","MSExcel")
     * 
     * @param itemRevision
     * @param relationType
     * @param dataType
     *            dataType�� "All"�̸� ��� dataSet�� ������
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
     * ������ �о� byte array�� ��ȯ�����ִ� method�̴�.
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
     * Title: Component�� owing user���� Check��<br>
     * Usage:
     * 
     * @param components
     * @return boolean Component�� ���������̸� true, �׷��� ������ false
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
     * ���� ���õ� object�� ��� �����ϰ� ������ �پ� �ִ� object���� ã�� ������ �����Ѵ�.
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
     * Title: Component�� Working Status���� Check�� Usage:
     * [SR ����][20141229][jclee] Component�� In Work �����̰� Workflow�� Process Stage�� Creator�� ��� Working Status�� true�� �ν��ϴ� ���� �߰�
     * 
     * @param components
     * @return boolean Component�� Working�̸� true, �׷��� ������ false
     */
    public static boolean isWorkingStatus(TCComponent components) throws TCException {
        components.refresh();
        
        String sMaturity = components.getProperty("s7_MATURITY");
        String sReleaseStatusList = components.getProperty("release_status_list");
        String sProcessStageList = components.getProperty("process_stage_list");
        String sProcessStage = "";
        // [20240404][UPGRADE] TC12.2 ���� process_stage_list �� Root Task �� ǥ���ϵ��� �Ǿ� �־� fnd0StartedWorkflowTasks �� ��ü
//        TCComponent[] process_stage_list = components.getReferenceListProperty("process_stage_list");
        TCComponent[] process_stage_list = components.getReferenceListProperty("fnd0StartedWorkflowTasks");
        
        // 1. Release�Ǿ����� �ʰ� Workflow�� ���� ���
        if (sReleaseStatusList.equalsIgnoreCase("") && sProcessStageList.equalsIgnoreCase("")) {
			return true;
		}
        
        // 2. Workflow�� ���������� Part�� Maturity�� In Work�̰� Workflow�� Process Stage�� Creator�� ���. (Workflow "Reject")
        if (process_stage_list.length != 0) {
        	for (int inx = 0; inx < process_stage_list.length; inx++) {
        		if (process_stage_list[inx].getType().equals("EPMReviewTask")) {
        			sProcessStage = process_stage_list[inx].getProperty("current_name");
        		}
        	}
        	
        	// 2.1. Part�� Maturity�� In Work�̰� Workflow�� Process Stage�� Creator�� ���
        	if (sMaturity.equalsIgnoreCase("In Work") && sProcessStage.equalsIgnoreCase("Creator")) {
        		return true;
        	}
		}
        
        return false;
    }

    /**
     * component�� ���������� �ƴ����� �Ǵ��Ѵ�.
     * 
     * @param components
     *            TCComponent
     * @return boolean
     * @throws TCException
     */
    public static boolean isInProcess(TCComponent components) throws TCException {
        // components.refresh();
        // [20240404][UPGRADE] TC12.2 ���� process_stage_list �� Root Task �� ǥ���ϵ��� �Ǿ� �־� fnd0StartedWorkflowTasks �� ��ü
//        if (components.getProperty("release_status_list").equalsIgnoreCase("") && !components.getProperty("process_stage_list").equalsIgnoreCase("")) {
    	if (components.getProperty("release_status_list").equalsIgnoreCase("") && !components.getProperty("fnd0StartedWorkflowTasks").equalsIgnoreCase("")) {
            return true;
        }
        return false;
    }

    /**
     * Component�� ���� �Ϸ����� �ƴ����� �Ǵ��Ѵ�.
     * 
     * @param components
     *            TCComponent
     * @return boolean ���� �Ϸ��̸� true�� ���� �׷��� ������ false�� �����Ѵ�.
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
     * �Էµ� component�� ���� �źε� ���������� üũ�Ѵ�.
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
     * Process�� �źεǾ����� �ƴ����� �Ǵ��Ѵ�.
     * 
     * @param wfProcess
     *            TCComponentProcess
     * @return boolean ���� �ź��̸� true�� ���� �׷��� ������ false�� �����Ѵ�.
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
                if (levelTasks[i].getName().equals("Submit") && (levelTasks[i].getTaskState().equals("Started") || levelTasks[i].getTaskState().equals("���۵�"))) {
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
     *            ������ component
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
        // EngineeringChange ����
        newEC = engChangeType.create(itemId, revId, itemName, ecType, description);
        // ecType����
        newEC.setECType(ecType);

        return newEC;
    }

    /**
     * Title: EngChange Revision�� EngForm�� ���δ�.<br>
     * Usage: attachECFormToEngChange(engChange,forms)
     * 
     * @param engChange
     * @param forms
     */
    public static void attachECFormToEngChange(TCComponentEngineeringChange engChange, Vector forms) throws Exception {
        TCComponentItemRevision engChangeRevision = engChange.getIR();
        if (forms != null && forms.size() > 0) {
            // �������� ��� formRelation�� IMAN_specification�̱⿡ �������� �־���.
            engChangeRevision.insertRelated("IMAN_specification", forms, 0);
        }
    }

    /**
     * Title: Ư�� Form Type�� �ش��ϴ� EngChange Form�� �����Ѵ�.<br>
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
        TCComponent ecFormComp[]; // Change Component�� Form Components
        TCComponentForm createECForm = null; // ������ Change Form
        TCComponentFormType createFormType = null;
        String as[] = null; // change Type�� �̸�
        TCComponentType compType = session.getTypeComponent("ChangeTypeData");
        ecComponents = compType.extent();
        as = new String[ecComponents.length];
        for (int i = 0; i < ecComponents.length; i++) {
            TCComponentChangeType changeType = (TCComponentChangeType) ecComponents[i];
            as[i] = changeType.askName();
            if (ECType.equalsIgnoreCase(as[i])) { // Change�� ���ǵ� form type�� �����´�.
                ecFormComp = changeType.getTCProperty("form_types").getReferenceValueArray();
                for (int j = 0; j < ecFormComp.length; j++) {
                    if (ecFormComp[j].toString().equals(formType)) {
                        // Change Form �̸��� ����
                        String ecFormCompName = changeId + "/" + revId + "-" + formType;
                        ecFormCompName = ecFormCompName.getBytes().length <= 32 ? ecFormCompName : new String(ecFormCompName.getBytes(), 0, 32);
                        // Change Form ����
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
     * �Ϲ� form�� �����ϴ� method�̴�.
     * 
     * @param formName
     *            String form�� �̸�.
     * @param formDesc
     *            String form�� ����.
     * @param formType
     *            String form�� type.
     * @return TCComponentForm
     * @throws Exception
     */
    public static TCComponentForm createTCComponentForm(String formName, String formDesc, String formType) throws Exception {
        TCComponentFormType TCComponentformtype = (TCComponentFormType) getTCSession().getTypeComponent(formType);
        return (TCComponentForm) TCComponentformtype.create(formName, formDesc, formType);
    }

    /**
     * Folder�� �����ϴ� Method �̴�
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
     * Title: �ڵ� ������ EngChange ID�� �������� �޼ҵ� <br>
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
        String as[] = null; // change Type�� �̸�
        String changeId = "";
        // ���� �� Change Type�� �����´�.
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
                    // EngChange ID�� �Ѵܰ� �ڵ�����
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
     * TCComponent�� check out �������� check �Ѵ�.<br>
     * item, revision, form, dataset...����.. ��� component���� ����.
     * 
     * @param components
     *            TCComponent chek�ϰ��� �ϴ� component.
     * @return boolean checkout�Ǿ� ������ true�� return.
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
     * Dataset�� �����ϰ� target�� Ư�� releation���� ���δ�.
     * 
     * @param session
     *            TCSession
     * @param targetComp
     *            TCComponent ���̰����ϴ� ���
     * @param datasetName
     *            String dataset�� �̸�
     * @param description
     *            String dataset�� ����
     * @param datasetType
     *            String dataset�� Type
     * @param relation
     *            String target�� ���϶��� ����
     * @return TCComponentDataset ������ Dataset
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
     * Dataset ���� named reference���� original file name�� �����Ѵ�.<br>
     * ������ reference name������ �ϰ� ����ǹǷ� �ݵ�� ���� �ؾ� �Ѵ�.<br>
     * �������� named reference�� ���� ��쿡�� �ݵ�� �����Ͽ� ����Ͽ��� �Ѵ�.
     * 
     * @param dataset
     *            TCComponentDataset �����ϰ��� �ϴ� dataset
     * @param ref_names
     *            String �����ϰ��� �ϴ� reference type name
     * @param originalFileName
     *            String ������ �̸�. ��) aaa.txt
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
     * �ݾ׿� �޸��� �����ϰ� �տ� �ܸ� ���δ�.
     * 
     * @param amt
     *            ��ȯ�� �ݾ�
     * @param dec
     *            �Ҽ��ڸ���
     * @return
     * 
     *         <p>
     * 
     *         <pre>
     *  - ��� ��
     *        String date = NumberUtil.getCurrencyNationNumber(123456.123, 3)
     *  ��� : ��123,456.123
     * </pre>
     */
    public static String getCurrencyNationNumber(double amt, int dec) {
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        nf.setMaximumFractionDigits(dec);
        return nf.format(amt);
    }

    /**
     * �� method�� dataset�� ���� ������ ���ϴ� ���͸��� ���� �޴� method�̴�.<br>
     * ��, ������ ���� �� �κ��� dataset���� �ϳ��� ���ϸ��� �����޵��� �Ǿ� ������ ������ ���� ��� Exception�� �߻��Ѵ�.<br>
     * �ϳ� �̻��� ��� ������� �����޾����� �˼� ����. �����ϵ��� �Ѵ�.
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
     * �� method�� dataset�ȿ� �ִ� ��� file�� �����޴� method�̴�. Leehonghee[2/8] Add if (imanFile.length > 0 )
     * �� �߰��Ͽ� dataset.getFiles(...) ���� �߻��ϴ� Null Point Dialog�� �����Ѵ�.
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
     * Ư�� dataset�ȿ� �ִ� named reference�� reference type name�� �޾ƿ��� method�̴�.
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @param TCComponent
     *            TCComponent named TCComponentfile ��ü
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
     * AttachFileComponent �� ���� ��ü�� ��� �ִ� ��쿡�� ����� �� �ִ� method�̴�.<br>
     * �ݵ�� �����Ͽ��� �Ѵ�.
     * 
     * @param newDataset
     *            TCComponentDataset ������ import�� dataset ��ü
     * @param attachFileComponenent
     *            AttachFileComponent import�� file ��ü.
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
     * dataset�� �Ϲ� ���� ��ü�� �ø��� �ִ� method�̴�.
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
     * Dataset ���� �ִ� ��� named reference�� �����ϴ� method�̴�.
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
     * dataset�� ���Ͽ� file�� Ȯ���ڿ� �ش��ϴ� named reference type�� ã�� method�̴�.
     * 
     * @param datasetComponent
     *            TCComponentDataset
     * @param extendsName
     *            String ���� Ȯ���� ��) txt
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
     * Dataset�� ������ �ִ� ��� reference name�� ã�� method
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
     * ���� �α����� TCSession ��ü�� �����´�.
     * 
     * @return TCSession ���� �α��� ��ü�� �����Ѵ�.
     */
    public static TCSession getTCSession() {
        return (TCSession) AIFUtility.getSessionManager().getDefaultSession();
    }

    /**
     * ItemRevision ������ ����� TCComponentForm�� ������<br>
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
     * ���� ������ ���ϴ� �̸��� ���Ϸ� �ٲٷ��� �Ҷ� ����Ѵ�.
     * 
     * @param sourceFile
     *            File �ٲٰ��� �ϴ� ���� ����
     * @param newFileName
     *            String �ٲٰ��� �ϴ� �̸�
     * @return File ���� �ٲ� ����
     */
    public static File renameFile(File sourceFile, String newFileName) {
        File newFile = new File(sourceFile.getParent(), newFileName + sourceFile.getName().substring(sourceFile.getName().indexOf(".")));
        sourceFile.renameTo(newFile);
        return newFile;
    }

    // /**
    // * ���� ���������� ���ο� ���̵� ä���Ҷ� ����ϴ� method�̴�.<br>
    // * �� prefix�� ���� ������ sequence�� ������.
    // * @param prefix String sequence �տ� ���� �κ�.
    // * @param sequenceSpaceCount int ����� sequence�� �ڸ���...
    // * @return String ä���� ID
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
     * JDBC�� �̿��Ͽ� Oracle�� ���� �����Ͽ� Data�� �������� �κ��̴�.
     * 
     * @param query
     *            String ������ select query
     * @return Vector ���� �� ��� query ���� vector
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
     * Servlet�� �̿��Ͽ� JDBC ����
     * 
     * @param query
     *            String ������ select query
     * @param dbuser
     *            ������ DB
     *            (JDBCExecuteDBProperty.DB_USER_INFODBA,JDBCExecuteDBProperty.DB_USER_DMESUSER
     *            ,JDBCExecuteDBProperty.DB_USER_WTMADM)
     * @return ArrayList ���� �� ��� query ���� ArrayList
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
     * Servlet�� �̿��Ͽ� JDBC ����
     * 
     * @param query
     *            ������ Select SQL
     * @param dbuser
     *            ������ DB
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
     * ������ ���� ��¥�� �������� ���� method��.
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
     * Ư�� String�� ���� ���ϴ� byte��ŭ �߶� return���ִ� method��.
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
     * JDBC�� �̿��Ͽ� update�� �� ��� �� method�� �̿��Ͽ� refresh�� �Ͽ��� ��.
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
    // //timestamp �Է�. ���� ����
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

    // //���� ������ �Է�
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
    // * �������μ����� �����̷� ������ �����´�.
    // * @param proccess TCComponentProcess
    // * @return
    // * @throws Exception
    // */
    // public static ArrayList getWorkFlowHistory(TCComponentProcess proccess) throws Exception
    // {
    // ArrayList workFlowList = new ArrayList();
    // ArrayList sqlDataList = new ArrayList();

    // //process�� puid����
    // String puid = proccess.getUid();
    // StringBuffer query = new StringBuffer();

    // query.append("SELECT PT.POBJECT_NAME TASK_NAME, U.PUSER_NAME USER_NAME, G.PNAME GROUP_NAME, ")
    // .append("  R.PROLE_NAME ROLE_NAME,SO.PDECISION_DATE DECISION_DATE, ")
    // .append("  DECODE(SO.PDECISION ,'89','����','78','�ź�','') DECISION, ")
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
     * ������ Object�� �������μ��� �����ش�.
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
    // * ���ϻ����Ȳ �α�
    // * @param catetory Export/Dataset - static ������ ����Ǿ� �ִ°��� ����ؾ���.
    // * @param titleName Export �� (��:���� �˻� ��� ����Ʈ)
    // * @param subModuleName ������ ������ ������ �� �ִ� Key(�˻������ ����, �� �� �ֿ� �˻�����, �������乮���� Export�� ������ ������ȣ
    // �� �� ��� �����ڰ� �Ǵ��Ͽ� �ֿ� Key�� String���� �Է��Ѵ�)
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
     * ����κ��� �Ѵ� �� ��¥�� return��.
     * 
     * @return
     */
    public static String getFromDate() {

        // �Ѵ� �� ��¥�� ���ؿ��� ���� calendar����
        Calendar calendar = Calendar.getInstance();
        String days = "";
        String month = "";
        String year = "";

        // �Ѵ� ���� ���ϱ�
        calendar.add(Calendar.MONTH, -1);

        // string������ setting
        days = String.valueOf(calendar.get(Calendar.DATE));
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));

        // ��¥�� ���̰� 1�̸�, �տ� 0�� ���̱�(�� 05��)
        if (days.length() == 1) {
            days = "0" + days;
        }
        // ��(month)�� ���̰� 1�̸� �տ� 0�� ���̱�(�� 03��)
        if (month.length() == 1) {
            month = "0" + month;
        }

        // yyyy-MM-dd hh:mm:ss
        String dateString = year + "-" + month + "-" + days + " " + "00:00:00";

        return dateString;
    }

    /**
     * Today 23:59:59�� return��.
     * 
     * @return
     */
    public static String getToDate() {
        Calendar calendar = Calendar.getInstance();
        String days = "";
        String month = "";
        String year = "";

        // string������ setting
        days = String.valueOf(calendar.get(Calendar.DATE));
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));

        // ��¥�� ���̰� 1�̸�, �տ� 0�� ���̱�(�� 05��)
        if (days.length() == 1) {
            days = "0" + days;
        }
        // ��(month)�� ���̰� 1�̸� �տ� 0�� ���̱�(�� 03��)
        if (month.length() == 1) {
            month = "0" + month;
        }

        // yyyy-MM-dd hh:mm:ss
        String dateString = year + "-" + month + "-" + days + " " + "23:59:59";

        return dateString;
    }

    /**
     * ���� ��¥�� ������.
     * 
     * @return
     */
    public static String getTODAY() {
        Calendar calendar = Calendar.getInstance();
        String days = "";
        String month = "";
        String year = "";

        // string������ setting
        days = String.valueOf(calendar.get(Calendar.DATE));
        month = String.valueOf(calendar.get(Calendar.MONTH) + 1);
        year = String.valueOf(calendar.get(Calendar.YEAR));

        // ��¥�� ���̰� 1�̸�, �տ� 0�� ���̱�(�� 05��)
        if (days.length() == 1) {
            days = "0" + days;
        }
        // ��(month)�� ���̰� 1�̸� �տ� 0�� ���̱�(�� 03��)
        if (month.length() == 1) {
            month = "0" + month;
        }

        // yyyy-MM-dd hh:mm:ss
        String dateString = year + "-" + month + "-" + days;

        return dateString;
    }

    /**
     * �׷��� ����� �����ϴ� �޼���
     * 
     * @param group
     *            �׷�
     * @return TCComponentGroupMember[] ã�� �׷����.
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

                if (roleName.endsWith("��")) {
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
     * Component�� ���� �ʱ� �Է½� �ѱ� �Է��� �ǵ��� �ϴ� method�̴�.<br>
     * 
     * @param component
     *            Component �ʱ� �ѱ� �Է��� �ʿ��� component
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
     * ���õ� Jtable Cell �� Clipboard �� Copy
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
     * Dataset Type�� Ȯ���� ������ �׿� �´� NamedReferenceType�� ã�´�.
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
     * �޼��� Pattern�� �Ķ��Ÿ�� Merge�Ͽ� Return �Ѵ�
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
     * Source Text�� Text Server �� Text�� ��ȯ�Ѵ�.
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
     * Source Text�� Text Server �� Text�� ��ȯ�Ѵ�.
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
     * TCComponentQuery���� query name�� ��� ������ name�� ����ϱ� ���� ��ȯ�� �� �����ϴ� �Լ�
     * 
     * @param session
     * @param propertyName
     *            ��ȸ�� Component�� Property
     * @param defaultName
     *            ��ȸ���� ������ ����� �⺻ ��
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
     * ���Ϸ� �����ͼ� ����
     * 
     * @Copyright : S-PALM
     * @author : ������
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
     * @author : ������
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
     * @author : ������
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
     * �ʼ� �ʵ� ������Ʈ Decotation
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
     * �ɼǰ� ��������(Scope : Site�߿���)
     * 
     * @Copyright : S-PALM
     * @author : �ڼ���
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
     * �ɼǰ���(�迭) ��������(Scope : Site�߿���)
     * 
     * @Copyright : S-PALM
     * @author : �ڼ���
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
     * Property type�� �� ����
     * 
     * @Copyright : S-PALM
     * @author : �ڼ���
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
     * Property type�� �� ����(TypedReference ����)
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
                // ItemRevision�� ���
                if (strValue.lastIndexOf("/") > 0) {
                    String itemID = strValue.substring(0, strValue.lastIndexOf("/"));
                    String itemRev = strValue.substring(strValue.lastIndexOf("/") + 1, strValue.length());

                    TCComponent[] comps = CustomUtil.queryComponent("Item Revision...", new String[] { "ItemID", "Revision" }, new String[] { itemID, itemRev });

                    if (comps != null && comps.length == 1) {
                        property.setReferenceValue(comps[0]);
                    }

                }
                // Item�� ���
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
                // [SR140702-058][20140701] KOG Blank ���� ���� ó��. value ��ü�� ���� ��� String ���� null ��. (20140707, bskwak)
                if (string == null || string.isEmpty()) {
                    return;
                }
                // ������� Excel Upload���� ���
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
     * IRDC �� ����� Type �� Item �� �����ϴ� Method
     * 
     * @Copyright : S-PALM
     * @author : �躴��
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
     * Object[]�� ���� �о �迭�� ù��° �ε����� ����("")�� �־ Object[]�� �����Ѵ�.
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
     * LOV �̸��� �ָ� String[] �� LOVDisplayValues�� �����´�.
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
     * revisionRule �� �����´�.
     * 
     * @Copyright : S-PALM
     * @author : �ڼ���
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
     * BOMLine ���� �������� BY ������ ������
     * 
     * @Copyright : S-PALM
     * @author : �ڼ���
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
     * Null String �� üũ�ϴ� Method
     * 
     * @Copyright : S-PALM
     * @author : �躴��
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
     * Color ���� Util
     * 
     * @Copyright : S-PALM
     * @author : ������
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
     * BOM Top Line ��������.
     * 
     * @Copyright : S-PALM
     * @author : �ǻ��
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
     * BOM Windows ����.
     * 
     * @Copyright : S-PALM
     * @author : �ǻ��
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
     * BomView �� ȹ��.
     * 
     * @Copyright : S-PALM
     * @author : �ǻ��
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
            findChildren(bomTopLine, getBomVtr); // ���� ������Ʈ���� ã�´�.

            bomWindow.close();
        }
        return getBomVtr;
    }

    /**
     * �ڽ� BOM Line�� BOM Vector�� ����.
     * 
     * @Copyright : S-PALM
     * @author : �ǻ��
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
     * ���ο� SaveAs ����� �Ǵ� Item��.
     * 
     * @Copyright : S-PALM
     * @author : �ǻ��
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

                // ItemRevision Type�� ���� �Ӽ� �� ���� ����.
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
     * ��� Ÿ�� Item�� topRevison�� ���.
     * 
     * @Copyright : S-PALM
     * @author : �ǻ��
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
     * app�� target ���� �����´�.
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
     * Target Stage ���� 'P'�ΰ��
     * Deferent SaveAs�� ���� 1Level Child���� Working���°� �����ϸ� �ȵ�.
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
            // Target Stage ���� 'P'�� ��츸 �ش�
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
                // Standard part�� ��� Stage �� null�� ��찡 �����ϹǷ� �̺κ� �˻縦 skip. (�� �ܿ� ���� �������� �Ǵܵ� From �ɿ���) : 20130617
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
     * Revise ����� P Stage �� ���
     * ������ ������ Stage�� P/Working�� ���� Part�� ECO No.�� �ٸ���� Revise ���� �Ұ�
     * 
     * @param targetRevision
     * @return
     */
    public static String validateRevise(TCComponentItemRevision targetRevision, String strECONo) {
        StringBuffer szMessage = new StringBuffer();

        try {
            String strTargetStage = targetRevision.getProperty("s7_STAGE");
            // Target Stage ���� 'P'�� ��츸 �ش�
            if (!"P".equals(strTargetStage))
                return "";

            // Latest Working Revision Rule
            TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(targetRevision.getSession(), "Latest Working");

            TCComponent[] imanComps = targetRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
            for (int i = 0; i < imanComps.length; i++) {
                if (imanComps[i].getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || imanComps[i].getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)) {
                    // Parent Stage
                    String strParentStage = imanComps[i].getProperty("s7_STAGE");

                    // Working, 'P' Stage�� ���
                    if (CustomUtil.isWorkingStatus(imanComps[i]) && "P".equals(strParentStage)) {
                        TCComponent ecoRev = imanComps[i].getReferenceProperty("s7_ECO_NO");

                        // ECO�� ������ Error
                        if (ecoRev == null) {
                            szMessage.append("'" + imanComps[i] + "' Parent Part ECO No. is Null\n");
                            continue;
                        }

                        String strParentEcoID = ecoRev.getProperty("item_id");

                        // ���� ECO NO.�� ��ġ���� ������ Error
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

        // SaveAs�� ���� 1Level Child���� Working���°� �����ϸ� �ȵ�.

        return szMessage.toString();
    }

    // ////////////////////////////////////////

    /**
     * ���õ� TCComponentItemRevision�� �����Ѵ�.
     * �̶�, ������ saveAs�� �ϸ� DeepCopy = true�̴�.
     * 
     * @param targetRevision
     *            ���� ��� ItemRevision
     * @param is3DCheck
     *            ���� Dialog���� 3D Check����(���ý� true). true�̸� ��ü���� �ƴϸ� ����������.
     * @param is2DCheck
     *            ���� Dialog���� 2D Check����(���ý� true). true�̸� ��ü���� �ƴϸ� ����������.
     * @param isSoftwareCheck
     *            ���� Dialog���� Software Check����(���ý� true). true�̸� ��ü���� �ƴϸ� �������� �ʴ´�.
     * @param isDeepCopy
     *            deepCopy ����
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

            /** �Ӽ������� ���� revise���� saveAs�� ���� **/
            newRevision = targetRevision.saveAs(revId, partName, desc, isDeepCopy, null);
            newRevision.setProperty("s7_STAGE", stage);
            newRevision.setProperty("s7_MATURITY", "In Work");
            
            /**
             * [SR141106-036][2014.11.17][jclee] Veh Part Revise �� Change Description ����ó��
             */
            if (newRevision.getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE)) {
            	newRevision.setProperty("s7_CHANGE_DESCRIPTION", "");
			}

            TCComponent refComp = targetRevision.getReferenceProperty("s7_Vehpart_TypedReference");
            if (refComp != null) {
                String strActWeight = refComp.getProperty("s7_ACT_WEIGHT");
                String strTargetWeight = refComp.getProperty("s7_TARGET_WEIGHT");
                String strBoundingBox = refComp.getProperty("s7_BOUNDINGBOX");
                // ���Ŀ� Target Weight ���� Part Revise �� ���� �� �� �ֵ��� �߰� 
                // String strTargetWeight = refComp.getProperty("s7_TARGET_WEIGHT");l
                // [SR140324-030][20140625] KOG DEV Veh. Part Revise �� �� ���� Typed Reference Object�� SES Spec No. �� ����.
                String strSESSpecNo = refComp.getStringProperty("s7_SES_SPEC_NO");

                TCComponent newRefComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX", "s7_SES_SPEC_NO" }, new String[] { strActWeight, strTargetWeight, strBoundingBox, strSESSpecNo });

                newRevision.setReferenceProperty("s7_Vehpart_TypedReference", newRefComp);

            } else {
                // [SR140324-030][20140625] KOG DEV Veh. Part Revise �� �� ���� Typed Reference Object�� ���� ��� SES Spec No. Empty�� ����.
                TCComponent newRefComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference", new String[] { "s7_ACT_WEIGHT", "s7_TARGET_WEIGHT", "s7_BOUNDINGBOX", "s7_SES_SPEC_NO" }, new String[] { "0", "0", "", "" });

                newRevision.setReferenceProperty("s7_Vehpart_TypedReference", newRefComp);
            }


            /** PartRevision �Ӽ��� ECO�� Reference Type���� �ִ´�. **/
            if (ecoRevision != null) {
                newRevision.setReferenceProperty("s7_ECO_NO", ecoRevision);
            }

//            newRevision.setProperty("s7_STAGE", stage);
//            newRevision.setProperty("s7_MATURITY", "In Work");
//            
//            newRevision.lock();
//            newRevision.save();
            
            /** Dataset�� Rule �� ���� �����Ѵ�. **/
            /**
             * [20140112][jclee] TC10 Upgrade ���� ����.
             *  - Dataset ���� �� Save ����. (The instance is not locked ���� ����)
             */
            /**
             * [UPGRADE][20240308] save �� The instance is not locked �����߻�, setPropery �� SOA setProperty�� ����ؼ� save(deprecated) �� �ʿ����.
             */
            //newRevision.save();
            newRevision = relateDatasetToItemRevision(targetRevision, newRevision, is3DCheck, is2DCheck, isSoftwareCheck, ecoRevision, true);
//            newRevision.setProperty("s7_STAGE", stage);
//            newRevision.setProperty("s7_MATURITY", "In Work");
            
//            /**
//             * [20140108][jclee]
//             * ���� ������ �����ϴ� ��쿡�� Save �۾� ����. �� �ܿ��� ������ ��.
//             * Save�� �������� ���� ��� ���� Part Revise �� ���� Revision�� ������ �ǵ��ư��� ������ ����.
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
     * Revise�� itemRevision�� ����� Dataset�� ���ǿ� ���� Object Copy or Reference Copy �Ѵ�.
     * Revise Dialog���� Dataset�� Check���ο� ���� Object Copy( check��) , Reference Copy( uncheck��) �� �����Ѵ�.
     * MS-Offect�迭�� dataset�� Check���ο� ������� �׻� Object Copy�� �Ѵ�.
     * 
     * [SR140723-026][20140715] swyoon, ItemRevision ���� Dataset���� Relation �������� �����Լ� ���� ���� ����
     * 
     * @param targetRevision
     *            Revise�� item
     * @param newRevision
     *            Revise�� item
     * @param is3DCheck
     *            ���� Dialog���� 3D Check����(���ý� true). true�̸� ��ü���� �ƴϸ� ����������.
     * @param is2DCheck
     *            ���� Dialog���� 2D Check����(���ý� true). true�̸� ��ü���� �ƴϸ� ����������.
     * @param isSoftwareCheck
     *            ���� Dialog���� Software Check����(���ý� true). true�̸� ��ü���� �ƴϸ� �������� ����.
     * @param ecoNo
     *            newRevision�� ����� eco�� item_id
     *            Dataset�� �Ӽ������� set��.
     * @return
     * @throws Exception
     */
    public static TCComponentItemRevision relateDatasetToItemRevision(TCComponentItemRevision targetRevision, TCComponentItemRevision newRevision, boolean is3DCheck, boolean is2DCheck, boolean isSoftwareCheck, TCComponentItemRevision ecoRevision, boolean isSucceeded) throws Exception {
        Registry registry = Registry.getRegistry("com.kgm.commands.revise.revise");
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

        // Dataset�� Type �� Catia v4�� ���� ��쿡�� Part Revision�� S7_CAT_V4_TYPE �Ӽ� Ȯ�� ��
        // �Ӽ��� 3D�̰�, Revise Dialog���� ������ �Ӽ��� 2D �� ��쿡�� ���� �޼����� ���� Dialog�� �����Ѵ�.
        for (int i = 0; i < relatedComponents.length; i++) {
            if (relatedComponents[i] instanceof TCComponentDataset) {
                String type = relatedComponents[i].getType();

                if (type.equals("catia")) {
                    String v4Type = targetRevision.getProperty("s7_CAT_V4_TYPE");
                    if (!is3DCheck && is2DCheck) {
                        if (v4Type.equals("3D")) {
                            // String errMsg = "���õ� �������� �Ӽ�[CAT.V4 Type]�� Type(3D)�� Revise Dialog���� ���õ� Type(2D)�� ���� �ٸ��ϴ�. ";
                            String errMsg = registry.getString("ReviseDialog.MESSAGE.WrongSelected");
                            MessageBox.post(errMsg, "Error", MessageBox.ERROR);
                            throw new Exception(errMsg);
                        }
                    }
                }
            }
        } // end for

        /** Dataset�� ItemRevision�� relation�� IMAN_specification���� �Ѵ�. **/
        String relation_Name = SYMCClass.SPECIFICATION_REL; // IMAN_specification

        /** Part�� item_id�� revision ��� */
        String newRevId = newRevision.getProperty("item_revision_id");

        for (int i = 0; i < relatedComponents.length; i++) {
            if (relatedComponents[i] instanceof TCComponentDataset) {

                String type = relatedComponents[i].getType();
                boolean isOfficeDataset = type.equals("MSExcel") || type.equals("MSExcelX") || type.equals("MSPowerPoint") || type.equals("MSPowerPointX") || type.equals("MSWord") || type.equals("MSWordX") ? true : false;
                boolean isReferenceDataset = relatedComponents[i].getProperty("object_name").indexOf("Reference") > -1;
                boolean isCorrectionsDataset = relatedComponents[i].getProperty("object_name").indexOf("Corrections") > -1;
                // JT������ IMAN_Rendering ����
                if (type.equals("DirectModel")) {
                    // 3D Check ������ ��� ��ü����, JT�� Copy���� ����
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

                // PDF ������ ���
                if (type.equals("PDF")) {
                    // 2D Check ������ ��� ��ü����, PDF�� Copy���� ����
                    if (is2DCheck) {
                        /** Object Copy */
                        // newRevision.add("IMAN_Rendering", updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
                    	
                    	// [SR150325-015][2015.04.08][jclee] ���� ���� PDF�� ��� 2D ������ üũ�ص� Revise�� Reference Type���� �ٿ���.
                    	if (relatedComponents[i] != null && isCorrectionsDataset) {
                    		newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
                    	}
                    } else {
                        /** Reference Copy */
                        // [SR140702-061][20140626] KOG Reference Copy Relation Name ���� (IMAN_reference).
                        newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
                    }

                    continue;
                }
                
                // [SR����][2015.06.26][jclee] Dataset Name�� Reference�� ������ ��� Check ���� �� Dataset Type�� �ҹ��ϰ� ��ü���� ����
                if (isReferenceDataset) {
                	newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
				} else {
					/** ���õ� CheckBox�� ������ Office ������ �����ϰ� ��� Reference Copy�� �Ѵ�. **/
					/** Offect ���� ������ ������ Object Copy�̴�. **/
					if (isNotAllChecked) {
						if (isOfficeDataset) {
							newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
						} else {
							/** Office �̿��� ������ ���������Ѵ�. **/
							// [SR140702-061][20140626] KOG Reference Copy Relation Name ���� (IMAN_reference).
							newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
						}
					} else {
						/** 3D, 2D, Software �� �� �ϳ��� ���õǾ��� ���.. **/
						if (type.equals("CATPart") || type.equals("CATProduct")) {
							if (is3DCheck) {
								/** Object Copy */
								newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
							} else {
								/** Reference Copy */
								// [SR140702-061][20140626] KOG Reference Copy Relation Name ���� (IMAN_reference).
								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
							}
						} // end if
						else if (type.equals("CATDrawing")) {
							if (is2DCheck) {
								/** Object Copy */
								newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
							} else {
								/** Reference Copy */
								// [SR140702-061][20140626] KOG Reference Copy Relation Name ���� (IMAN_reference).
								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
							}
						} else if (type.equals("PDF") || type.equals("TIF") || type.equals("HPGL")) {
							/** 2D üũ�� �Ǿ� ���� ��쿡�� CATDrawing Type�� �ű� �����Ѵ�. **/
							if (!is2DCheck) {
								/** Reference Copy */
								// [SR140702-061][20140626] KOG Reference Copy Relation Name ���� (IMAN_reference).
								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
							} else {
								// [SR150325-015][2015.04.08][jclee] ���� ���� PDF�� ��� 2D ������ üũ�ص� Revise�� Reference Type���� �ٿ���.
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
//								// [SR140702-061][20140626] KOG Reference Copy Relation Name ���� (IMAN_reference).
//								newRevision.add(SYMCClass.REFERENCE_REL, relatedComponents[i]);
//							}
							//[20180719][CSH]������� ���� å�� 3D Check �� nocopy... v5�� ��ȯ�ؼ� ���ε� �ؾ��ϹǷ�... �������� reference copy  
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
     * Dataset ���� �� ����� Part�� Rev Id�� ECO No�� Dataset �Ӽ��� Update �Ѵ�.
     * [20140112][jclee] TC10 Upgrade ����.
     *  - Dataset Save As �� ���� �߻�. (CXPOM_wrong_class)
     *  - �ű� Dataset ���� �� Named Reference ���� �� ���� ÷��
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

    		// ÷������ ����Ʈ ����
    		tcFiles = SYMTcUtil.getImanFile((TCComponentDataset)relatedComponent);
    		Vector<File> importFiles = new Vector<File>();
    		String sEXT = "";
    		
    		for (int inx = 0; inx < tcFiles.length; inx++) {
    			File file = tcFiles[inx].getFile(null);
    			importFiles.add(file);
    			sEXT = getExtension(file);
    			
    			// TODO ÷������ Ȯ���ڸ����� Dataset Type�� �����ϴ� ��� �ܿ� ���� Dataset���� Type�� �������� ����� ���� ��� �ش� �������� ������ ��.
    			if (sEXT.equalsIgnoreCase("CATProduct") || sEXT.equalsIgnoreCase("CATPart") || sEXT.equalsIgnoreCase("CATDrawing")) {
    				dataSetType = sEXT;
				}
    			// ���� : bc.kim
    			// ���� ���� : CATIA V4 �� �ش� �ϴ� ���� Revise �� �ش� ������ Type �� ��� �߰� 
    			//             ���� ��� Validation ������ �ش� �Ǵ� Type �� ���� ��� �ش� Dataset �� Type �� ���� �Ͽ� �״�� ����
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
    		
    		/** ECO No�� Dataset�� �����Ѵ�. */
    		revDataSet.setProperty("s7_ECO_NO", ecoNo);
    		/** Part Revision�� Rev ID ���� Dataset�� �����Ѵ�. */
    		revDataSet.setProperty("s7_REVISION_ID", newRevId);
		} catch (Exception e) {
			throw new Exception("�����ͼ� ���� �� ���� �߻�.\n����������� ���� �ϼ���.");
		}

        return revDataSet;
    }

    /**
     * DataSetName / DataSetRevisionID - ParentItemRevisionID
     * 
     * @Copyright : S-PALM
     * @author : �ǿ���
     * @since : 2012. 12. 20.
     * @param dataSetName
     * @return
     * @throws Exception
     */
    private static String getDataSetName(String strItemID, String revId, String dataSetName, boolean isSucceeded) throws Exception {
        if (dataSetName.indexOf("/") == -1) {
            String errMsg = "Dataset�� Name������ �߸��Ǿ����ϴ�. �����ڿ��� �����ϼ���.!!";
            MessageBox.post(errMsg, "Error", MessageBox.ERROR);
            throw new Exception(errMsg);
        }

        if (dataSetName.indexOf(";") > 0) {
            dataSetName = dataSetName.substring(0, dataSetName.indexOf(";"));
        }

        // ���� Dataset Revision ID�� �°����� �ʴ� ���
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
     * ������ �����۸������� ������ ȭ�鿡 ����
     * 
     * @Copyright : S-PALM
     * @author : �ǿ���
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

                // Stage ���� 'P'�� ��� Revision ID�� ���� 3�ڸ�(001->999)
                if ("P".equals(strStage)) {
                    // target revision�� 'P' �̸�
                    if ("P".equals(strCurrentStage)) {
                        strNextRevID = CustomUtil.getNextRevID(component.getItem(), new String("Item"));
                    }
                    // Stage���� 'D' -> 'P'�� ����ȴٸ�
                    else {
                        strNextRevID = "000";
                    }

                }
                // Stage ���� 'P'�� �ƴ� ��� Revision ID�� ���� 2�ڸ�(A->ZZ)
                // [SR140616-019][20140619][bskwak] ������ ǰ�� Concept �ܰ� ǰ������ ���� ���� : Stage ���� ���� ��� �⺻ revision �ʱⰪ ����ϴ� ������ ���� ��.
                // => ���� Rev�� �ƴ� ���� rev�� ����ϴ� logic���� ��ü.
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
     * Item revision ������ ���� class.
     * ���� ���� : Item ID ascending, Item revision ascending
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
     * Named Reference File�� Item������ ������ ImanFile original filename�� rename�Ѵ�.
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
        // [SR����][20150710][jclee] Corrections, Reference Dataset�� ��� Rename���� �ʵ��� ����
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
     * Reference File Name�� ���� Revision�� Item, ItemRevision�� �°� �����Ѵ�.
     * 
     * ITEM : 30100CD000
     * REV : 002
     * FILE_NAME : PTPART_30100CD000_001_--B_DETAIL_41419C52C5040CAF_41419E1C48DF46FC.pdf (�������� 001 ����)
     * ->
     * FILE_NAME : 30100CD000_002_--B_DETAIL.pdf (������ 002 ���� ����)
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
        // TODO: CAD PART REV(---, --A...)�� ������ �ʴ´�.
        // String prvPartRev = getCadDatasetRev(dataset);
        // String fileName = itemId + "_" + revId + "_" + prvPartRev;
        String fileName = itemId + "_" + revId;
        String datasetType = dataset.getType();
        // V4�� ��� Dataset 1���� model ������ 2���� �޷��ִ� ��찡 �����Ƿ� File���� �����Ѵ�.(MASTER, DETAIL)
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
        // file Ȯ���� add
        // 30100CD000_002_--B_DETAIL + .pdf
        fileName = fileName + originalFileName.substring(originalFileName.lastIndexOf("."));
        return fileName;
    }

    /**
     * Dataset Name���� VPM CAD REV������ ���´�.
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
                    // XA �� ����
                    cadRev = "-" + datasetNames[1].substring(3);
                }
            }
        }
        return cadRev;
    }

    // [SR140702-058][20140701] KOG CustomUtil get Image �޼ҵ� �߰�.
    public static Image getImage(String imagePath) {
        URL fullPathString = BundleUtility.find(Activator.getDefault().getBundle(), imagePath);
        return ImageDescriptor.createFromURL(fullPathString).createImage();
    }

    // [SR140702-058][20140701] KOG CustomUtil String > Date ��ȯ �޼ҵ� �߰�.
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

    // [SR140702-058][20140701] KOG CustomUtil Date > String ��ȯ �޼ҵ� �߰�.
    public static String getStringDateFromDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(date);
    }
    
	/**
	 *
	 * SRME:: [][20140811] swyoon  �Բ� ����� �� ���� �ɼ����� ����.
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
			[3W16, 3W17, 3W25, 3W35, 3W46, 3W51] ==> ����	(������)
			[3C09, 3C61]:[3WCC, 3WDD]	==> �ٴ��	(������)
		 */
		
		// Teamcenter Type�� �ɼ��̸�, Simple Type(H-BOM���� ����ϴ� ����)���� ������.
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
			
//			3W02:3W09	==> 1:1 �Ǵ� [3C09, 3C61]:[3WCC, 3WDD] Ÿ��
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
	 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
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
	 * [SR150421-027][20150811][ymjang] PLM system �������� - Manual ��ȸ ������� �߰�
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
    
    /**
     * SOA�� ���� Item ����
     * @param session TC Session
     * @param boName BO Name
     * @param itemPropMap Item �Ӽ� ����
     * @param itemRevisionPropMap Item Revision �Ӽ� ����
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
            //Revision Property �Ӽ� �Է�
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
