package com.symc.plm.me.utils;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JTable;

import org.apache.commons.collections.CollectionUtils;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.swt.widgets.Control;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.rac.kernel.SYMCBOPEditData;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.command.meco.dao.CustomMECODao;
import com.symc.plm.me.sdv.operation.meco.MECOCreationUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.create.BOCreateDefinitionFactory;
import com.teamcenter.rac.common.create.CreateInstanceInput;
import com.teamcenter.rac.common.create.IBOCreateDefinition;
import com.teamcenter.rac.common.create.ICreateInstanceInput;
import com.teamcenter.rac.common.create.SOAGenericCreateHelper;
import com.teamcenter.rac.kernel.EffectivityExpression;
import com.teamcenter.rac.kernel.EffectivityValidityRange;
import com.teamcenter.rac.kernel.ListOfValuesInfo;
import com.teamcenter.rac.kernel.Markpoint;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.RevisionRuleEntry;
import com.teamcenter.rac.kernel.TCAccessControlService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMView;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevisionType;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentBOPWindowType;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentChangeType;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinitionType;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentEffectivity;
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
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
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
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.services.rac.manufacturing.CoreService;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextInputInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FindNodeInContextResponse;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.FoundNodesInfo;
import com.teamcenter.services.rac.manufacturing._2010_09.Core.NodeInfo;

/**
 * Common Utility Class
 */
public class CustomUtil {
	public static int GROUP_LEADER = 0;
	public static int GROUP_MEMBER = 0;

	public static String CATETORY_EXPORT = "Export";
	public static String CATETORY_DATASET = "Dataset";
	public static SimpleDateFormat simpleDateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public CustomUtil () {

	}
	/**
	 * Title: Item ID로 Item을 찾음<br>
	 * Usage: findItem("Document","00001") *
	 *
	 * @param compType         ComponentType
	 * @param itemId           Item Id
	 * @return item
	 * @throws TCException
	 */
	public static TCComponentItem findItem(String compType, String itemId) throws TCException {
		TCSession session = getTCSession();
		TCComponentItem item  = null;
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);

		//TCComponentItemType.find(String itemId) 가 deprecated되어서 findItems()으로 변경함
		//아이템 이름으로 찾지만 비슷한 이름으로 여러개가 나올 수 있으므로
		//2013-11-28 CSPark
		TCComponentItem [] items = itemType.findItems(itemId);
		if(items != null && items.length > 0){
		    item = items[0];
		}
		return item;
	}

	   /**
     * Title: Item ID로 Item 들을 찾음<br>
     * Usage: findItem("Document","00001") *
     *
     * @param compType         ComponentType
     * @param itemId           Item Id
     * @return item
     * @throws TCException
     */
    public static TCComponentItem[] findItems(String compType, String itemId) throws TCException {
        TCSession session = getTCSession();
        TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);

        //TCComponentItemType.find(String itemId) 가 deprecated되어서 findItems()으로 변경함
        //아이템 이름으로 찾지만 비슷한 이름으로 여러개가 나올 수 있으므로
        //2013-11-28 CSPark
        TCComponentItem [] items = itemType.findItems(itemId);
        return items;
    }

	/**
	 * 이미 만들어져 있는 Saved query를 이용하여 imancomponent를 검색하는 method이다.
	 *
	 * @param savedQueryName           String 저장된 query name
	 * @param entryName                String[] 검색 조건 name(오리지날 name)
	 * @param entryValue               String[] 검색 조건 value
	 * @return TCComponent[]           검색 결과
	 * @throws Exception
	 *
	 */
	public static TCComponent[] queryComponent(String savedQueryName, String[] entryName, String[] entryValue) throws Exception {
		TCSession session = getTCSession();
//		session.getPreferenceService().setString(1, "QRY_dataset_display_option", "2");
		session.getPreferenceService().setStringValue("QRY_dataset_display_option", "2");

		TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
		TCComponentQuery query = (TCComponentQuery) queryType.find(savedQueryName);
		String[] queryEntries = session.getTextService().getTextValues(entryName);
		for(int i = 0 ; queryEntries != null && i < queryEntries.length ; i++) {
		    if(queryEntries[i] == null || queryEntries[i].equals("")) {
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
	 * @Copyright : KIMIES
	 * @author :
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

	public static TCComponentFolder[] findFolder(String folderName, String folderType, String owningUser)
			throws TCException {
		TCSession session = getTCSession();
		TCComponentQueryType queryType = (TCComponentQueryType) session.getTypeComponent("ImanQuery");
		TCComponentQuery query = (TCComponentQuery) queryType.find(getTextServerString(session, "k_find_general_name"));
		String entryNames[] = { getTextServerString(session, "Name"), getTextServerString(session, "Type"),getTextServerString(session, "OwningUser") };
		String entryValues[] = { folderName, folderType, owningUser };
		com.teamcenter.rac.kernel.TCComponent queryResults[] = query.execute(entryNames, entryValues);
		List<TCComponentFolder> folders = new ArrayList<TCComponentFolder>();
		for (int i = 0; i < queryResults.length; i++)
			if (queryResults[i] instanceof TCComponentFolder)
				folders.add((TCComponentFolder) queryResults[i]);

		return folders.toArray(new TCComponentFolder[folders.size()]);
	}

	/**
	 * Title: Item ID와 Revision ID로 Item Revision을 찾음<br>
	 * Usage: findItemRevision("DocumentRevision","00001","A")
	 *
	 * @param compTypefindRevisions    Component Type
	 * @param itemId                   Item ID
	 * @param revisionId               ItemRevision Id
	 * @return itemRevision
	 * @throws Exception
	 */
	public static TCComponentItemRevision findItemRevision(String compType, String itemId, String revisionId) throws Exception {
		TCSession session = getTCSession();
		TCComponentItemRevision itemRevision = null;
		TCComponentItemRevisionType itemRevisionType = (TCComponentItemRevisionType) session.getTypeComponent(compType);
		if (itemRevisionType != null) {
		    TCComponentItemRevision [] itemRevisions = itemRevisionType.findRevisions(itemId, revisionId);
		    //TCComponentItemRevisionType.findRevision(String itemId, String revisionId)가 deprecated되어서 findRevisions()로 변경함
	        //비슷한 이름으로 여러개가 나올 수 있으므로
	        //2013-11-28 CSPark
		    if(itemRevisions != null && itemRevisions.length > 0){
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
	public static TCComponentItemRevision findLatestItemRevision(String itemTypeName, String itemId)
			throws TCException {
		TCSession session = getTCSession();
		TCComponentItemRevision itemRevision = null;
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(itemTypeName);
        //TCComponentItemType.find(String itemId) 가 deprecated되어서 findItems()으로 변경함
        //아이템 이름으로 찾지만 비슷한 이름으로 여러개가 나올 수 있으므로
        //2013-11-28 CSPark
        TCComponentItem [] items = itemType.findItems(itemId);
        if(items != null && items.length > 0){
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
	public static TCComponentItem createItem(String compType, String itemId, String revision,
			String itemName, String desc) throws TCException {
		TCSession session = getTCSession();
		TCComponentItem item = null;
		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
		item = itemType.create(itemId, revision, compType, itemName, desc, null);
		return item;
	}


//	public static TCComponentItem createItem(String compType, String itemId, String revision,
//			String itemName, String desc, String strUOM) throws TCException {
//		TCSession session = getTCSession();
//
//        LOVUIComponent uomLovComboBox = new LOVUIComponent(session, "Unit of Measures");
//        uomLovComboBox.setSelectedValue(strUOM);
//        Object uomObj = uomLovComboBox.getSelectedObject();
//
//
//		TCComponentItem item = null;
//		TCComponentItemType itemType = (TCComponentItemType) session.getTypeComponent(compType);
//		item = itemType.create(itemId, revision, compType, itemName, desc, (TCComponent)uomObj);
//		return item;
//	}


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


    if( currentRevID == null || currentRevID.trim().equals("") || currentRevID.length() > 2 )
      throw new Exception("올바르지 않은 Revision ID 입니다.");

    char[] szChar =  currentRevID.toCharArray();

    for( int i = (szChar.length-1) ; i >= 0 ; i-- )
    {
      if( ('A' > szChar[i]) || (szChar[i] > 'Z'))
      {
        throw new Exception("올바르지 않은 Revision ID 입니다.");
      }
    }



    if( "ZZ".equals(currentRevID))
      throw new Exception("더 이상 개정하실 수 없습니다.");

    for( int i = (szChar.length-1) ; i >= 0 ; i-- )
    {

      if( ( szChar[i] + 1 ) > 'Z' )
      {
        if( i == 0 )
        {
          szChar[i] = 'A';
          return "A"+new String(szChar);
        }
        szChar[i] = 'A';
        continue;
      }
      else
      {
        szChar[i] =  (char)(szChar[i] + 1);
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
	public static TCComponentItemRevision getPreviousRevision(TCComponentItemRevision itemRevision)
			throws Exception {
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
	public static Vector<TCComponentDataset> getDatasets(TCComponent itemRevision, String relationType,
			String dataType) throws Exception {
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
			TCComponentUser TCComponentuser = (TCComponentUser) (components
					.getReferenceProperty("owning_user"));
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
			if (whereContext[i].getComponent() instanceof TCComponentEnvelope
					|| whereContext[i].getComponent() instanceof TCComponentTask
					|| whereContext[i].getComponent() instanceof TCComponentProcess) {
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
	 *
	 * @param components
	 * @return boolean Component가 Working이면 true, 그렇지 않으면 false
	 */
	public static boolean isWorkingStatus(TCComponent components) throws TCException {
		components.refresh();
		if (components.getProperty("release_status_list").equalsIgnoreCase("")
        	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체
//				&& components.getProperty("process_stage_list").equalsIgnoreCase("")) {
				&& components.getProperty("fnd0StartedWorkflowTasks").equalsIgnoreCase("")) {
			return true;
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
		if (components.getProperty("release_status_list").equalsIgnoreCase("")
        	    // [20240404][UPGRADE] TC12.2 이후 process_stage_list 는 Root Task 만 표시하도록 되어 있어 fnd0StartedWorkflowTasks 로 교체
//				&& !components.getProperty("process_stage_list").equalsIgnoreCase("")) {
				&& !components.getProperty("fnd0StartedWorkflowTasks").equalsIgnoreCase("")) {
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
				if (levelTasks[i].getName().equals("Submit")
						&& (levelTasks[i].getTaskState().equals("Started") || levelTasks[i].getTaskState()
								.equals("시작됨"))) {
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
	public static TCComponentEngineeringChange createEngChange(TCComponent targetComp, String itemId,
			String revId, String itemName, String ecType, String description) throws Exception {
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
	public static void attachECFormToEngChange(TCComponentEngineeringChange engChange, List<? extends TCComponent> forms)
			throws Exception {
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
	public static TCComponentForm createECFormByFormType(String changeId, String revId, String ECType,
			String formDesc, String formType) throws Exception {
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
						ecFormCompName = ecFormCompName.getBytes().length <= 32 ? ecFormCompName
								: new String(ecFormCompName.getBytes(), 0, 32);
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
	public static TCComponentForm createTCComponentForm(String formName, String formDesc, String formType)
			throws Exception {
		TCComponentFormType TCComponentformtype = (TCComponentFormType) getTCSession().getTypeComponent(
				formType);
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
	public static TCComponentFolder createFolder(String folderName, String folderDesc, String folderType)
			throws Exception {
		TCComponentFolderType TCComponentfoldertype = (TCComponentFolderType) getTCSession()
				.getTypeComponent(folderType);
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
					TCComponentTcId compId = (TCComponentTcId) changeType.getTCProperty("id_format")
							.getReferenceValue();
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
	public static TCComponentDataset createPasteDataset(TCComponent targetComp, String datasetName,
			String description, String datasetType, String relation) throws Exception {
		TCSession session = targetComp.getSession();
		TCComponentDataset newDataset = null;
		String s = null;
		try {
			TCPreferenceService imanpreferenceservice = session.getPreferenceService();
			String s1 = "IMAN_" + datasetType + "_Tool";
//			s = imanpreferenceservice.getString(0, s1);
			s = imanpreferenceservice.getStringValue(s1);
		} catch (Exception ex) {
			s = null;
		}
		try {
			TCComponentDatasetType TCComponentdatasettype = (TCComponentDatasetType) session
					.getTypeComponent(datasetType);
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
	public static void changeNamedRef(TCComponentDataset dataset, String ref_names, String originalFileName)
			throws Exception {
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
     * <p>
     *  <pre>
     *  - 사용 예
     *        String date = NumberUtil.getCurrencyNationNumber(123456.123, 3)
     *  결과 : ￦123,456.123
     * </pre>
	 * </p>
	 * @param amt       변환할 금액
	 * @param dec       소수자리수
	 * @return
	 */
	public static String getCurrencyNationNumber(double amt, int dec) {
		NumberFormat nf = NumberFormat.getCurrencyInstance();
		nf.setMaximumFractionDigits(dec);
		return nf.format(amt);
	}

	/**
	 * 이 method는 dataset안에 있는 모든 file을 내려받는 method이다.
	 * <history>
	 * Add if (imanFile.length > 0 )를 추가 : dataset.getFiles(...) 에서 발생하는 Null Point Dialog를 방지한다.
	 * </history>
	 *
	 * @param dataset              TCComponentDataset
	 * @param export_subDir        String
	 * @return File[]
	 * @throws Exception
	 */
	public static File[] exportDataset(TCComponentDataset dataset, String export_subDir) throws Exception {
		Registry registry = Registry.getRegistry("client_specific");
		String export_dir = registry.getString("TCExportDir") + File.separator + export_subDir;
		
		File folder = new File(export_dir);
		if(export_dir!=null && export_dir.trim().length()>0 && (folder!=null && folder.exists()==false)){
			folder.mkdirs();	
		}
		if (folder.exists()==false) {
			throw new Exception("Export folder not found.");
		}
		TCComponentTcFile[] imanFile = dataset.getTcFiles();
		
		File[] file = null;
		if (imanFile!=null && imanFile.length > 0) {
			System.out.println("getNamedRefType(dataset, imanFile[0]) = "+getNamedRefType(dataset, imanFile[0]));
			file = dataset.getFiles(getNamedRefType(dataset, imanFile[0]), export_dir);
		}
		return file;
	}

	/**
	 * 특정 dataset안에 있는 named reference의 reference type name을 받아오는 method이다.
	 *
	 * @param datasetComponent         TCComponentDataset
	 * @param TCComponent              TCComponent named TCComponentfile 객체
	 * @return String named reference  type name
	 * @throws Exception
	 */
	public static String getNamedRefType(TCComponentDataset datasetComponent, TCComponent TCComponent) throws Exception {
		String namedRefName = "";
		TCProperty refListProperty = datasetComponent.getTCProperty("ref_list");
		TCProperty refNamesProperty = datasetComponent.getTCProperty("ref_names");

		if (refListProperty == null || refNamesProperty == null) {
			return namedRefName;
		}
		TCComponent componetValues[] = refListProperty.getReferenceValueArray();
		String referenceValues[] = refNamesProperty.getStringValueArray();
		if (componetValues == null || referenceValues == null) {
			return namedRefName;
		}
		int i = componetValues.length;
		if (i != referenceValues.length) {
			return namedRefName;
		}
		int j = -1;
		for (int k = 0; k < i; k++) {
			if (TCComponent != componetValues[k]) {
				continue;
			}
			j = k;
			break;
		}

		if (j != -1) {
		    namedRefName = referenceValues[j];
		}
		return namedRefName;
	}

	/**
	 * dataset에 일반 파일 객체를 올릴수 있는 method이다.
	 *
	 * @param newDataset   TCComponentDataset
	 * @param file         File
	 * @throws Exception
	 */
	public static void importFiles(TCComponentDataset newDataset, File file) throws Exception {

        Registry registry = Registry.getRegistry("com.ugsolutions.iman.kernel.kernel");
        int tansferBufferSize = registry.getInt("IMANFile_transfer_buf_size", 512);

		String[] filePaths = new String[1];
		String[] namedRefNames = new String[1];
		String[] dataTypes = new String[1];
		String[] namedRefTypes = new String[1];

		String filePath = file.getPath();
		String fileName = file.getName();
		int extIndex = fileName.lastIndexOf(".");

		String fileExtendsName = fileName.substring(extIndex + 1);
		String namedRefName = getNamedRefType(newDataset, fileExtendsName.toLowerCase());
		// importFileName
		filePaths[0] = filePath;
		// importRefType
		namedRefNames[0] = namedRefName;
		// importFileType
		dataTypes[0] = "File";
		namedRefTypes[0] = "Plain";

		newDataset.setFiles(filePaths, namedRefNames, dataTypes, namedRefTypes, tansferBufferSize);
	}

	/**
	 * Dataset 내에 있는 모든 named reference를 삭제하는 method이다.
	 *
	 * @param dataset          TCComponentDataset
	 * @throws Exception
	 */
	public static void removeAllNamedReference(TCComponentDataset dataset) throws Exception {
		TCComponentTcFile[] imanFile = dataset.getTcFiles();
		List<String> refNameVector = getAllNamedRefTypeArray(dataset);
		for(String refName : refNameVector){
		    if(refName == null) continue;
            dataset.removeNamedReference(refName);
		}
		for (int j = 0; j < imanFile.length; j++) {
			imanFile[j].delete();
		}
	}

	/**
     * 표준작업요령서용 Dataset 내에 있는 모든 named reference를 삭제하는 method이다.
     *
     * @param dataset          TCComponentDataset
     * @throws Exception
     */
    public static void removeAllNamedReferenceOfSWM(TCComponentDataset dataset) throws Exception {
        List<String> refNameVector = getAllNamedRefTypeArray(dataset);
        for(String refName : refNameVector){
            if(refName == null) continue;
            dataset.removeNamedReference(refName);
        }
    }

	/**
	 * dataset에 대하여 file의 확장자에 해당하는 named reference type을 찾는 method이다.
	 *
	 * @param datasetComponent             TCComponentDataset
	 * @param extendsName                  String 파일 확장자 예) txt
	 * @return String
	 * @throws Exception
	 */
	public static String getNamedRefType(TCComponentDataset datasetComponent, String extendsName)throws Exception {
		String namedReferenceFileName = "";
		NamedReferenceContext[] namedRefContext = null;
		namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
		for (int i = 0; i < namedRefContext.length; i++) {
			String dsNamedReferenceFileName = namedRefContext[i].getNamedReference();
			String dsNamedReferenceExtentionFilter = namedRefContext[i].getFileTemplate();
			if (dsNamedReferenceExtentionFilter.equalsIgnoreCase("*") || dsNamedReferenceExtentionFilter.equalsIgnoreCase("*.*")) {
			    namedReferenceFileName = dsNamedReferenceFileName;
				break;
			} else if (dsNamedReferenceExtentionFilter.equalsIgnoreCase("*." + extendsName)) {
			    namedReferenceFileName = dsNamedReferenceExtentionFilter;
				break;
			}
		}
		return namedReferenceFileName;
	}

	/**
	 * Dataset이 가지고 있는 모든 reference name을 찾는 method
	 *
	 * @param datasetComponent         TCComponentDataset
	 * @return Vector
	 * @throws Exception
	 */
	public static List<String> getAllNamedRefTypeArray(TCComponentDataset datasetComponent) throws Exception {
	    List<String> namedRefTypeList = new Vector<String>();
		NamedReferenceContext[] namedRefContext = null;
		try {
			namedRefContext = datasetComponent.getDatasetDefinitionComponent().getNamedReferenceContexts();
			for (int i = 0; i < namedRefContext.length; i++) {
				String s1 = namedRefContext[i].getNamedReference();
				namedRefTypeList.add(s1);
			}
		} catch (Exception e) {
			throw e;
		}
		return namedRefTypeList;
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
	public static TCComponentForm getRelatedFormFromItemRevision(TCComponentItemRevision itemRevision,
			String relationType, String componentType) throws Exception {
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
	 * @param sourceFile           File 바꾸고자 하는 원본 파일
	 * @param newFileName          String 바꾸고자 하는 이름
	 * @return File 최종 바뀐 파일
	 */
	public static File renameFile(File sourceFile, String newFileName) {
		File newFile = new File(sourceFile.getParent(), newFileName
				+ sourceFile.getName().substring(sourceFile.getName().indexOf(".")));
		sourceFile.renameTo(newFile);
		return newFile;
	}

	/**
	 * 서버의 현재 날짜를 가져오기 위한 method임.
	 *
	 * @return Date
	 * @throws Exception
	 */
	public static Date getServerDate() throws Exception {
	    TCComponentFolder newStuffFolder = getTCSession().getUser().getNewStuffFolder();
	    newStuffFolder.setProperty("object_desc", newStuffFolder.getProperty("object_desc"));
		return newStuffFolder.getDateProperty("last_mod_date");
	}

	/**
	 * 특정 String에 대해 원하는 byte만큼 잘라 return해주는 method임.
	 *
	 * @param name         String
	 * @param _byte        int
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
	 * 선택한 Object의 결재프로세스 돌려준다.
	 *
	 * @param TCComponent              TCComponent
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
	 * @param group                        그룹
	 * @return TCComponentGroupMember[]    찾은 그룹장들.
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
	 * @param component        Component 초기 한글 입력이 필요한 component
	 */
	public static void addTextFieldListenerForKorean(Component component) {
		InputContext input = component.getInputContext();
		Character.Subset[] subset = { Character.UnicodeBlock.HANGUL_SYLLABLES };
		input.setCharacterSubsets(subset);
	}


//	public static TCComponentGroup getGroupFromCode(String code) {
//		TCComponentGroup group = null;
//		TCComponent[] comps = null;
//
//		try {
//            TCClassService classService = getTCSession().getClassService();
//
//			comps = classService.findByClass("Group", "description", code);
//			if (comps != null) {
//				group = (TCComponentGroup) comps[0];
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		return group;
//	}

    /**
     *
     *
     * @method getDesignerInfo
     * @param
     * @return String[]
     */
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
	 * 선택된 JTable Cell 을 Clipboard 로 Copy
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

	/**
	 * @method createPasteImportDataset
	 * @date 2013. 11. 28.
	 * @param
	 * @return TCComponentDataset
	 */
	public static TCComponentDataset createPasteImportDataset(TCComponent targetComp, String datasetName,
			String description, String datasetType, String relation, String[] filePathNames,
			String[] namedRefs) throws Exception {
		TCSession session = targetComp.getSession();
		TCComponentDataset newDataset = null;
		try {
			TCComponentDatasetType TCComponentdatasettype = (TCComponentDatasetType) session.getTypeComponent(datasetType);
			newDataset = TCComponentdatasettype.setFiles(datasetName, description, datasetType,filePathNames, namedRefs);
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
	 * @param datasetType          String
	 * @param extensionName        String
	 * @return String
	 * @throws Exception
	 */
	public static String getNamedReferenceTypeString(String datasetType, String extensionName)throws Exception {
		String namedReferenceTypeString = "";
		TCComponentDatasetDefinitionType TCComponentdatasetdefinitiontype = (TCComponentDatasetDefinitionType) getTCSession().getTypeComponent("DatasetType");
		TCComponentDatasetDefinition TCComponentdatasetdefinition = TCComponentdatasetdefinitiontype
				.find(datasetType);
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

	public static TCComponentItem createPasteItem(TCComponent parent, String relationType, String compType,
			String itemId, String revision_id, String itemName, String desc) throws TCException {
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

	public static TCComponentItem createPasteItem(TCComponent parent, String relationType, String compType,
			String itemName, String desc) throws TCException {
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
	 * @param msg           String
	 * @param params       String[]
	 * @return String
	 */
	@SuppressWarnings("unchecked")
    public static String mergeMessage(String msg, Object messages) {
		if (messages instanceof String) {
			msg = msg.replaceFirst("@", (String)messages);
		} else if (messages instanceof String[]) {
			String[] params = (String[])messages;
			for (int i = 0; i < params.length; i++) {
				msg = msg.replaceFirst("@", params[i]);
			}
		} else if (messages instanceof Map) {
			Map<String, String> messageMap = (Map<String, String>)messages;

			for(String key : messageMap.keySet()){
                String replaceRegExp = "\\$" + key;
                msg = msg.replaceAll(replaceRegExp, (messageMap.get(key) == null)?null:messageMap.get(key).toString());
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
	 * @param sourceText   Single Text
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

	static public TCComponentForm getNewForm(String formName, String desc, String formType, boolean saveForm)
			throws TCException {
		TCComponentFormType formComponentType = (TCComponentFormType) getTCSession().getTypeComponent(
				formType);
		return formComponentType.create(formName, desc, formType, saveForm);
	}

//	static public TCComponentFolder getNewFolder(String name, String desc, String type,
//			InterfaceAIFComponent target) throws Exception {
//		InterfaceAIFComponent[] targets = { target };
//		TCSession session = getTCSession();
//		NewFolderCommand folderCommand = new NewFolderCommand(session, name, desc, type, targets);
//		NewFolderOperation folderOperation = new NewFolderOperation(folderCommand);
//		session.performOperation(folderOperation);
//		return (TCComponentFolder) folderOperation.getNewFolder();
//	}

	static public TCComponentFolder getHomeFolder() throws TCException {
		return getTCSession().getUser().getHomeFolder();
	}

	static public void setReferenceValueArray(String property, TCComponent target, TCComponent[] source)
			throws TCException {
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
	 * @param propertyName         조회할 Component의 Property
	 * @param defaultName          조회하지 못했을 경우의 기본 값
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
	 * @Copyright : KIMIES
	 * @author :
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
//		String[] dataset_Per_Extension = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, "SPALM_DragNDropCopy_Dataset_Extension_List");
		String[] dataset_Per_Extension = preferenceService.getStringValuesAtLocation("SPALM_DragNDropCopy_Dataset_Extension_List", TCPreferenceLocation.OVERLAY_LOCATION);
		
		for (String string : dataset_Per_Extension) {
			if (extension.equals(string.substring(0, string.indexOf("=")))) {
				try {
					dataset = datasetType.create(getFileName(file), "", string
							.substring(string.indexOf("=") + 1));
					String namedReference = getNamedReference(session, dataset, extension);
					dataset
							.setFiles(new String[] { file.getAbsolutePath() },
									new String[] { namedReference });
				} catch (Exception e) {
					System.out.println("Dataset Type No Found! Select DatasetType Dialog Open...");
					return dataset;
				}
			}
		}
		return dataset;
	}

	public static TCComponentDataset createDataset(TCSession session, File file, String type)
			throws Exception {
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
	 * @Copyright : KIMIES
	 * @author :
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
				if (namedRefTypes[i].getFileFormat().equals("TEXT")
						&& namedRefTypes[i].getFileTemplate().equalsIgnoreCase("*.*")) {
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
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2011. 8. 29.
	 * @param session
	 * @param revision
	 * @param viewType
	 * @return TCComponentBOMViewRevision
	 * @throws Exception
	 */
	public static TCComponentBOMViewRevision createBOMViewRevision(TCSession session,
			TCComponentItemRevision revision, String viewType) throws Exception {
		TCComponentBOMViewRevisionType bomViewRevisionType = (TCComponentBOMViewRevisionType) session
				.getTypeComponent("PSBOMViewRevision");
		TCComponentViewType[] viewTypes = bomViewRevisionType.getAvailableViewTypes(revision
				.getProperty("item_id"), revision.getProperty("item_revision_id"));
		for (int i = 0; i < viewTypes.length; i++) {
			if (viewTypes[i].toString().equals(viewType)) {
				return bomViewRevisionType.create(revision.getProperty("item_id"), revision
						.getProperty("item_revision_id"), viewTypes[i], false);
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

	public static ControlDecoration setRequiredFieldSymbol(Control comp, String style, String desc,
			boolean isShowOnlyFocus) {
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
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2011. 6. 1.
	 * @param session
	 * @param preferenceName
	 * @return
	 */
	public static final String getSitePreferenceValue(TCSession session, String preferenceName) {
		String result;
		TCPreferenceService preferenceService = session.getPreferenceService();
//		result = preferenceService.getString(TCPreferenceService.TC_preference_site, preferenceName);
		result = preferenceService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
		return result;
	}

	/**
	 * 옵션값들(배열) 가져오기(Scope : Site중에서)
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2011. 6. 1.
	 * @param session
	 * @param preferenceName
	 * @return
	 */
	public static final String[] getSitePreferenceValues(TCSession session, String preferenceName) {
		String[] result;
		TCPreferenceService preferenceService = session.getPreferenceService();
//		result = preferenceService.getStringArray(TCPreferenceService.TC_preference_site, preferenceName);
		result = preferenceService.getStringValuesAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
		return result;
	}

	/**
	 * Property type별 값 셋팅
	 *
	 * @Copyright : KIMIES
	 * @author :
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
//			property.setFloatValueData(Float.parseFloat(string));
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

    if( TCProperty.PROP_typed_reference == i || TCProperty.PROP_untyped_reference == i )
    {

      if( value == null || "".equals(value.toString()) )
      {
        property.setReferenceValue(null);
      }
      else if( value instanceof TCComponent )
      {
        property.setReferenceValue((TCComponent)value);
      }
      else if( value instanceof TCComponent[] )
      {
        property.setReferenceValueArray((TCComponent[])value);
      }
      else if( value instanceof String )
      {


        String strValue = (String)value;
        // ItemRevision인 경우
        if( strValue.lastIndexOf("/") > 0)
        {
          String itemID = strValue.substring(0,strValue.lastIndexOf("/"));
          String itemRev =  strValue.substring(strValue.lastIndexOf("/")+1,strValue.length());

          TCComponent[] comps = CustomUtil.queryComponent("Item Revision...", new String[] { "ItemID",  "Revision" }, new String[] {
              itemID, itemRev });

          if(comps != null && comps.length == 1 )
          {
            property.setReferenceValue(comps[0]);
          }

        }
        // Item인 경우
        else
        {
          TCComponent[] comps = CustomUtil.queryComponent("Item...", new String[] { "ItemID"}, new String[] {
              strValue });

          if(comps != null && comps.length == 1 )
          {
            property.setReferenceValue(comps[0]);
          }
        }

      }

    }
    else if( TCProperty.PROP_date == i )
    {
    	if( value instanceof Date )
    	{
    		property.setDateValue((Date)value);
    	}
    	else
    	{
    		String string = (String)value;
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

    }
    else
    {
      String string = (String)value;
      setStringToPropertyValue(property, string);
    }

  }


	/**
	 * IRDC 가 적용된 Type 의 Item 을 생성하는 Method
	 *
	 * @Copyright : KIMIES
	 * @author :
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
	public static TCComponentItem createIRDCItem(TCSession session, String itemType, String itemId,
			String itemName, String itemDesc, String itemRev, String[] revPropertyNames,
			String[] revPropertyValues) throws TCException {
		TCComponentItem irdcItem = null;

		IBOCreateDefinition createDefinition = BOCreateDefinitionFactory.getInstance().getCreateDefinition(
				session, itemType);
		 Map<String, List<IBOCreateDefinition>> secondaryCreateDefinitions = createDefinition.getSecondaryCreateDefinitions();
		List<IBOCreateDefinition> revList = (List<IBOCreateDefinition>) secondaryCreateDefinitions.get("revision");
		IBOCreateDefinition createDefinitionRev = (IBOCreateDefinition) revList.get(0);

		List<ICreateInstanceInput> createInputList = new ArrayList<ICreateInstanceInput>();
		CreateInstanceInput createInput = new CreateInstanceInput(createDefinition);
		createInput.add("item_id", itemId);
		createInput.add("object_name", itemName);
		createInput.add("object_desc", itemDesc);
		createInputList.add(createInput);

		CreateInstanceInput createInputRev = new CreateInstanceInput(createDefinitionRev);
		createInputRev.add("item_revision_id", itemRev);
		createInputList.add(createInputRev);

		List<ICreateInstanceInput> createInputRevList = new ArrayList<ICreateInstanceInput>();
		CreateInstanceInput createInputRev2 = new CreateInstanceInput(createDefinitionRev);
		for (int propertyCount = 0; propertyCount < revPropertyNames.length; propertyCount++) {
			if (!(isNullString(revPropertyValues[propertyCount])))
				createInputRev2.add(revPropertyNames[propertyCount], revPropertyValues[propertyCount]);
		}
		createInputRevList.add(createInputRev2);

		List<ICreateInstanceInput> argInput = new ArrayList<ICreateInstanceInput>(0);
		argInput.addAll(createInputList);
		argInput.addAll(createInputRevList);

		 List<TCComponent> resultList = SOAGenericCreateHelper.create(session, createDefinition, argInput);
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
		TCComponentListOfValuesType listofvaluestype = (TCComponentListOfValuesType) session
				.getTypeComponent("ListOfValues");
		TCComponentListOfValues[] listofvalues = listofvaluestype.find(string);
		TCComponentListOfValues listofvalue = listofvalues[0];
		return listofvalue.getListOfValues().getLOVDisplayValues();
	}

	/**
	 * revisionRule 을 가져온다.
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2011. 12. 12.
	 * @param session
	 * @param ruleName
	 * @return
	 * @throws Exception
	 */
	public static TCComponentRevisionRule getRevisionRule(TCSession session, String ruleName)
			throws Exception {
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
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2011. 12. 12.
	 * @param targetRevision
	 * @param session
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOMLine getBomline(TCComponentItemRevision targetRevision, TCSession session)
			throws TCException {
		TCComponentBOMLine topLine = null;
		if (targetRevision != null) {
			TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session
					.getTypeComponent("BOMWindow");
			TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session
					.getTypeComponent("RevisionRule");
			TCComponentBOMWindow bomWindow = windowType.create(ruleType.getDefaultRule());
			topLine = bomWindow.setWindowTopLine(null, targetRevision, null, null);
		}
		return topLine;
	}

	/**
	 * BOMLine 정보 가져오기 BY 아이템 리비젼
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2011. 12. 12.
	 * @param targetRevision
	 * @param session
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOPLine getBopline(TCComponentItemRevision targetRevision, TCSession session)
			throws TCException {
		TCComponentBOPLine topLine = null;
		if (targetRevision != null) {
			TCComponentBOPWindowType windowType = (TCComponentBOPWindowType) session
					.getTypeComponent("BOPWindow");
			TCComponentRevisionRuleType ruleType = (TCComponentRevisionRuleType) session
					.getTypeComponent("RevisionRule");
			TCComponentBOPWindow bopWindow = windowType.createBOPWindow(ruleType.getDefaultRule());
			topLine = (TCComponentBOPLine) bopWindow.setWindowTopLine(null, targetRevision, null, null);
		}
		return topLine;
	}

	/**
	 * Null String 을 체크하는 Method
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2011. 11. 15.
	 * @param checkString
	 * @return boolean
	 */
	public static boolean isNullString(String checkString) {
		if (checkString == null || "".equals(checkString) || "null".equals(checkString))
			return true;
		return false;
	}


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
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2012. 12. 20.
	 * @param bomWindow
	 * @param revision
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOMLine getTopBomLine(TCComponentBOMWindow bomWindow, TCComponentItemRevision revision)
			throws TCException {
		TCComponentBOMLine bomLine = null;
		bomLine = bomWindow.setWindowTopLine(null, revision, null, null);
		return bomLine;
	}

	/**
	 * BOM Windows 생성.
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2012. 12. 20.
	 * @return
	 * @throws TCException
	 */
	public static TCComponentBOMWindow createBOMWindow() throws TCException {
		TCSession session = getTCSession();
		TCComponentBOMWindow bomWindow = null;
		TCComponentRevisionRuleType revisionRuleType = (TCComponentRevisionRuleType) session
				.getTypeComponent("RevisionRule");
		TCComponentRevisionRule revisionRule = revisionRuleType.getDefaultRule();
		TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType) session
				.getTypeComponent("BOMWindow");
		bomWindow = bomWindowType.create(revisionRule);
		return bomWindow;
	}

	/**
	 * BomView 를 획득.
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2012. 12. 20.
	 * @param context
	 * @throws Exception
	 */
	public static List<TCComponentBOMLine> getBomLine(AIFComponentContext[] context, List<TCComponentBOMLine> getBomVtr) throws Exception {
		TCSession session = getTCSession();
		if (context.length > 0) {
			TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) context[0]
					.getComponent();
			TCComponentRevisionRuleType revisionRuleType = (TCComponentRevisionRuleType) session
					.getTypeComponent("RevisionRule");
			TCComponentRevisionRule revisionRule = revisionRuleType.getDefaultRule();
			TCComponentBOMWindowType bomWindowType = (TCComponentBOMWindowType) session
					.getTypeComponent("BOMWindow");
			TCComponentBOMWindow bomWindow = bomWindowType.create(revisionRule);
			TCComponentBOMLine bomTopLine = bomWindow.setWindowTopLine(null, null, null, bomViewRevision);
			getBomVtr.add(bomTopLine);
			findChildren(bomTopLine, getBomVtr); // 하위 컴포넌트들을 찾는다.

			bomWindow.close();
		}
		return getBomVtr;
	}

	/**
	 * 자식 BOM Line를 BOM Vector에 담음.
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2012. 12. 20.
	 * @param bomLine
	 * @throws Exception
	 */
	public static void findChildren(TCComponentBOMLine bomLine, List<TCComponentBOMLine> getBomVtr) throws Exception {
		AIFComponentContext[] bomLineContext = bomLine.getChildren();
		if (bomLineContext.length != 0) {
			for (int i = 0; i < bomLineContext.length; i++) {
				InterfaceAIFComponent con = bomLineContext[i].getComponent();
				TCComponentBOMLine bom = (TCComponentBOMLine) con;
				getBomVtr.add(bom);
				findChildren((TCComponentBOMLine) bomLineContext[i].getComponent(), getBomVtr);
			}
		}
	}

	/**
	 * 새로운 SaveAs 대상이 되는 Item들.
	 *
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2012. 12. 20.
	 * @param itemRev
	 * @throws Exception
	 */
	public static List<TCComponentItem> getNewBomLine(TCComponentItem itemComp, List<TCComponentItem> newItemList, List<TCComponentBOMLine> getBomVtr) throws Exception {
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
			for(TCComponentBOMLine bomLine : getBomVtr){
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
	 * @Copyright : KIMIES
	 * @author :
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
	  public static InterfaceAIFComponent[] getCurrentApplicationTargets()
	  {
	    InterfaceAIFComponent[] targets = AIFUtility.getCurrentApplication().getTargetComponents();
	    return targets;
	  }

	public static boolean isEmpty(String str)
	{
	  if( str == null || "".equals(str.trim())  )
	    return true;
	  else
	    return false;
	}

	/**
	 * Target Stage 값이 'P'인경우
	 * Deferent SaveAs시 하위 1Level Child에서 Working상태가 존재하면 안됨.
	 * @param targetRevision
	 * @return
	 */
	public static String validateSaveAs(TCComponentItemRevision targetRevision)
	{
		StringBuffer szMessage = new StringBuffer();
		TCComponentBOMWindow bomWindow = null;
		try
		{

			TCComponentItemRevision latestReleseRev = SYMTcUtil.getLatestReleasedRevision(targetRevision.getItem());
			if( latestReleseRev == null )
			{
				szMessage.append("Please Select Latest Released Revision.\n");
				return szMessage.toString();
			}

			if( !targetRevision.equals(latestReleseRev) )
			{
				szMessage.append("Please Select Latest Released Revision.\n");
				return szMessage.toString();
			}




			String strTargetStage = targetRevision.getProperty("s7_STAGE");
			// Target Stage 값이 'P'인 경우만 해당
			if( !"P".equals(strTargetStage) )
				return "";

	        TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) targetRevision.getSession().getTypeComponent("BOMWindow");

			// Latest Working Revision Rule
			TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(targetRevision.getSession(), "Latest Working");
	        bomWindow = windowType.create(revRule);

	        TCComponentBOMLine topLine = bomWindow.setWindowTopLine(null, (TCComponentItemRevision) targetRevision, null, null);
	        TCComponent[] childLines = topLine.getRelatedComponents("bl_child_lines");
            for (int j = 0; j < childLines.length; j++)
            {
                TCComponentBOMLine cLine = (TCComponentBOMLine) childLines[j];

                TCComponentItemRevision childRevision = cLine.getItemRevision();
                String strChildStage = childRevision.getProperty("s7_STAGE");
                // Standard part인 경우 Stage 가 null인 경우가 존재하므로  이부분 검사를 skip. (뒷 단에 영향 없음으로 판단됨 From 심영주) : 20130617
            	if( !"P".equals(strChildStage) && !SYMCClass.S7_STDPARTREVISIONTYPE.equals(childRevision.getType()))
            	{
            		szMessage.append("'"+childRevision+"' Child Part Stage '"+strChildStage+"'\n");
            	}

            	if( CustomUtil.isWorkingStatus(childRevision))
            	{
            		szMessage.append("'"+childRevision+"' Child Part is Working State\n");
            	}

            }

		}
		catch(Exception e)
		{
			szMessage.append(e.getMessage());
			e.printStackTrace();
		}
		finally
		{
			try
			{
	            if(bomWindow != null)
	            {
	              bomWindow.close();
	              bomWindow = null;
	            }

			}
			catch(TCException e)
			{
				e.printStackTrace();
			}
		}

		return szMessage.toString();
	}


	/**
	 * Revise 대상이 P Stage 인 경우
	 * 역전개 수행한 Stage가 P/Working인 상위 Part의 ECO No.가 다른경우 Revise 수행 불가
	 * @param targetRevision
	 * @return
	 */
	public static String validateRevise(TCComponentItemRevision targetRevision, String strECONo)
	{
		StringBuffer szMessage = new StringBuffer();

		try
		{
			String strTargetStage = targetRevision.getProperty("s7_STAGE");
			// Target Stage 값이 'P'인 경우만 해당
			if( !"P".equals(strTargetStage) )
				return "";

			// Latest Working Revision Rule
			TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(targetRevision.getSession(), "Latest Working");

			TCComponent[] imanComps = targetRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
	        for (int i = 0; i < imanComps.length; i++) {
	            if (imanComps[i].getType().equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || imanComps[i].getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE) )
	            {
	            	// Parent Stage
	            	String strParentStage = imanComps[i].getProperty("s7_STAGE");

	            	// Working, 'P' Stage인 경우
	                if( CustomUtil.isWorkingStatus(imanComps[i]) && "P".equals(strParentStage) )
	                {
	                	TCComponent ecoRev = imanComps[i].getReferenceProperty("s7_ECO_NO");

	                	// ECO가 없으면 Error
	                	if( ecoRev == null )
	                	{
	                	    szMessage.append("'"+imanComps[i]+"' Parent Part ECO No. is Null\n");
	                	    continue;
	                	}

	                	String strParentEcoID = ecoRev.getProperty("item_id");

	                	// 상위 ECO NO.와 일치하지 않으면 Error
	                	if( !strParentEcoID.equals(strECONo) )
	                	{
	                		szMessage.append("'"+imanComps[i]+"' Parent Part ECO No("+strParentEcoID+") Does Not Match\n");
	                		continue;
	                	}
	                }
	            }
	        }
		}
		catch(Exception e)
		{
			//e.printStackTrace();
			szMessage.append(e.getMessage());
		}

		// SaveAs시 하위 1Level Child에서 Working상태가 존재하면 안됨.

		return szMessage.toString();
	}




	//////////////////////////////////////////

	/**
	 * 선택된 TCComponentItemRevision을 개정한다.
	 * 이때, 개정은 saveAs로 하며 DeepCopy = true이다.
	 * @param targetRevision	개정 대상 ItemRevision
	 * @param is3DCheck			개정 Dialog에서 3D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
	 * @param is2DCheck			개정 Dialog에서 2D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
	 * @param isSoftwareCheck	개정 Dialog에서 Software Check여부(선택시 true). true이면 개체복사 아니면 복사하지 않는다.
	 * @param isDeepCopy		deepCopy 여부
	 * @param desc
	 * @param stage
	 * @throws Exception
	 */
	public static TCComponentItemRevision reviseForItemRev(TCComponentItemRevision targetRevision, boolean is3DCheck, boolean is2DCheck, boolean isSoftwareCheck, boolean isDeepCopy , String desc, String stage, TCComponentItemRevision ecoRevision) throws Exception {

	    TCComponentItemRevision newRevision = null;
	    Markpoint mp = new Markpoint(targetRevision.getSession());

	    try
		{
//			String ecoNo = "";
//	  		if(ecoRevision != null) {
//	  			ecoNo = ecoRevision.getProperty("item_id");
//	  		}

	  		/** Revise */

	  		Map<String, String> resultMap = setItemInfoForRevise(targetRevision, stage);
	  		String partName = resultMap.get("partName");
	  		String revId = resultMap.get("revId");

	  		/** 속성값으로 인해 revise에서 saveAs로 변경 **/
	  		newRevision = targetRevision.saveAs(revId, partName, desc, isDeepCopy, null);
	  		newRevision.setProperty("s7_STAGE", stage);
	  		newRevision.setProperty("s7_MATURITY", "In Work");

	        TCComponent refComp = targetRevision.getReferenceProperty("s7_Vehpart_TypedReference");
	        if( refComp != null )
	        {
		      	  String strActWeight = refComp.getProperty("s7_ACT_WEIGHT" );
		      	  String strBoundingBox = refComp.getProperty("s7_BOUNDINGBOX" );

		      	  TCComponent newRefComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference",
	      			new String[]{"s7_ACT_WEIGHT","s7_BOUNDINGBOX"}, new String[]{strActWeight,strBoundingBox });

		      	newRevision.setReferenceProperty("s7_Vehpart_TypedReference", newRefComp);

	        }
	        else
	        {
	        	TCComponent newRefComp = SYMTcUtil.createApplicationObject(targetRevision.getSession(), "S7_Vehpart_TypedReference",
	      			new String[]{"s7_ACT_WEIGHT","s7_BOUNDINGBOX"}, new String[]{"0","" });

	        	newRevision.setReferenceProperty("s7_Vehpart_TypedReference", newRefComp);
	        }


	  		/** Dataset를 Rule 에 따라 연결한다. **/
	  		newRevision = relateDatasetToItemRevision(targetRevision, newRevision, is3DCheck, is2DCheck, isSoftwareCheck, ecoRevision, true);



	  		/** PartRevision 속성에 ECO를 Reference Type으로 넣는다. **/
	  		if(ecoRevision != null) {
	  			newRevision.setReferenceProperty("s7_ECO_NO", ecoRevision);
	  		}

	  		mp.forget();
	  		mp = null;
		}// end try
	    catch(Exception e)
	    {
	    	e.printStackTrace();
	    	if (mp != null)
	    	{
	    		mp.rollBack();
	    		mp = null;
	    	}

	    	throw new Exception(e.getMessage());
	    }
	    finally
	    {
	    	if (mp != null)
	    	{
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
	 * @param targetRevision
	 * 			Revise할 item
	 * @param newRevision
	 * 			Revise된 item
	 * @param is3DCheck
	 * 			개정 Dialog에서 3D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
	 * @param is2DCheck
	 * 			개정 Dialog에서 2D Check여부(선택시 true). true이면 개체복사 아니면 참조복사임.
	 * @param isSoftwareCheck
	 * 			개정 Dialog에서 Software Check여부(선택시 true). true이면 개체복사 아니면 복사하지 않음.
	 * @param ecoNo
	 * 			newRevision에 연결될 eco의 item_id
	 * 			Dataset의 속성값으로 set됨.
	 * @return
	 * @throws Exception
	 */
	public static TCComponentItemRevision relateDatasetToItemRevision(TCComponentItemRevision targetRevision, TCComponentItemRevision newRevision, boolean is3DCheck, boolean is2DCheck, boolean isSoftwareCheck, TCComponentItemRevision ecoRevision, boolean isSucceeded) throws Exception {
		Registry registry = Registry.getRegistry("com.ssangyong.commands.revise.revise");
		TCComponent[] relatedComponents = targetRevision.getRelatedComponents();
  		boolean isNotAllChecked = !is3DCheck && !is2DCheck && !isSoftwareCheck ? true : false;

  		String ecoNo = "";
  		if(ecoRevision != null) {
  			ecoNo = ecoRevision.getProperty("item_id");
  		}

  		// Dataset의 Type 중 Catia v4가 있을 경우에는 Part Revision의 S7_CAT_V4_TYPE 속성 확인 후
  		// 속성이 3D이고,  Revise Dialog에서 선택한 속성이 2D 일 경우에는 에러 메세지를 띄우고 Dialog를 종료한다.
  		for(int i=0; i<relatedComponents.length; i++) {
  			if (relatedComponents[i] instanceof TCComponentDataset) {
  				String type = relatedComponents[i].getType();

  				if (type.equals("catia")) {
  					String v4Type = targetRevision.getProperty("s7_CAT_V4_TYPE");
  					if (!is3DCheck && is2DCheck) {
  						if(v4Type.equals("3D")) {
//							String errMsg = "선택된 아이템의 속성[CAT.V4 Type]의 Type(3D)과 Revise Dialog에서 선택된 Type(2D)이 서로 다릅니다. ";
  							String errMsg = registry.getString("ReviseDialog.MESSAGE.WrongSelected");
  							MessageBox.post(errMsg, "Error", MessageBox.ERROR);
  							throw new Exception(errMsg);
  						}
  					}
  				}
  			}
  		}	// end for

  		/** Dataset과 ItemRevision의 relation은 IMAN_specification으로 한다. **/
  		String relation_Name = SYMCClass.SPECIFICATION_REL;	// IMAN_specification

  		/** Part의 item_id와 revision 얻기 */
		String newRevId = newRevision.getProperty("item_revision_id");

		for (int i = 0; i < relatedComponents.length; i++) {
  			if (relatedComponents[i] instanceof TCComponentDataset) {





  				String type = relatedComponents[i].getType();
  				boolean isOfficeDataset = type.equals("MSExcel") || type.equals("MSExcelX") || type.equals("MSPowerPoint")
  						|| type.equals("MSPowerPointX") || type.equals("MSWord") || type.equals("MSWordX") ? true : false;


  				// JT파일은 IMAN_Rendering 관계
  				if( type.equals("DirectModel")  )
  				{
  					    // 3D Check 상태인 경우 개체복사, JT는 Copy하지 않음
			    		if(is3DCheck) {
  			    			/** Object Copy */
  			    			//newRevision.add("IMAN_Rendering", updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    		} else {
  			    			/** Reference Copy */
  							newRevision.add("IMAN_Rendering", relatedComponents[i]);
  			    		}

			    		continue;
  				}

  				if(type.equals("CATCache")) {

  	                 continue;
  	            }

  				// PDF 파일인 경우
  				if( type.equals("PDF")  )
  				{
  					    // 2D Check 상태인 경우 개체복사, PDF는 Copy하지 않음
			    		if(is2DCheck) {
  			    			/** Object Copy */
  			    			//newRevision.add("IMAN_Rendering", updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    		} else {
  			    			/** Reference Copy */
  							newRevision.add(relation_Name, relatedComponents[i]);
  			    		}

			    		continue;
  				}


  			    /** 선택된 CheckBox가 없으면 Office 종류를 제외하고 모두 Reference Copy를 한다.**/
  			    /** Offect 관련 파일은 무조건 Object Copy이다. **/
  			    if(isNotAllChecked) {
  			    	if(isOfficeDataset) {
  			    		newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    	} else {
  			    		/** Office 이외의 파일은 참조복사한다. **/
  			    		newRevision.add(relation_Name, relatedComponents[i]);
  			    	}
  			    } else {
  			    	/** 3D, 2D, Software 셋 중 하나라도 선택되었을 경우..  **/
  			    	if (type.equals("CATPart") || type.equals("CATProduct")) {
  			    		if(is3DCheck) {
  			    			/** Object Copy */
  			    			newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    		} else {
  			    			/** Reference Copy */
  							newRevision.add(relation_Name, relatedComponents[i]);
  			    		}
  			    	} // end if
  			    	else if(type.equals("CATDrawing")) {
  			    		if(is2DCheck) {
  			    			/** Object Copy */
  			    			newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    		} else {
  			    			/** Reference Copy */
  							newRevision.add(relation_Name, relatedComponents[i]);
  			    		}
  			    	}
  			    	else if(type.equals("PDF") || type.equals("TIF") || type.equals("HPGL")) {
  			    		/** 2D 체크가 되어 있을 경우에는 CATDrawing Type만 신규 생성한다. **/
  			    		if(!is2DCheck) {
  			    			/** Reference Copy */
  							newRevision.add(relation_Name, relatedComponents[i]);
  			    		}
  			    	}
  			    	else if(isOfficeDataset) {
  			    		/** Object Copy */
  		    			newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    	}
  			    	else if(type.equals("Zip")) {
  			    		if(isSoftwareCheck) {
  			    			newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    		}
  			    	}
  			    	else if(type.equals("catia")) {
  			    		if(is3DCheck || is2DCheck) {
  			    			newRevision.add(relation_Name, updateDataSetForEcoNoAndNewRev(newRevision, relatedComponents[i], ecoNo, newRevId, isSucceeded));
  			    		} else {
  			    			newRevision.add(relation_Name, relatedComponents[i]);
  			    		}
  			    	}
  			    }
  			}
  		}	// end for

		return newRevision;
	}

	/**
	 * Dataset 생성 후 연결되 Part의 Rev Id와 ECO No를 Dataset 속성에 Update 한다.
	 * @param relatedComponent
	 * @param ecoNo
	 * @param newRevId
	 * @return
	 * @throws Exception
	 * @throws Exception
	 */
	public static TCComponentDataset updateDataSetForEcoNoAndNewRev(TCComponentItemRevision newRevision, TCComponent relatedComponent, String ecoNo, String newRevId, boolean isSucceeded) throws Exception, Exception {
		String dataSetName = getDataSetName(newRevision.getProperty("item_id"), newRevId,relatedComponent.getProperty("object_name"), isSucceeded );
		TCComponentDataset revDataSet = ((TCComponentDataset) relatedComponent).saveAs(dataSetName);

		/** ECO No를 Dataset에 저장한다. */
		revDataSet.setProperty("s7_ECO_NO", ecoNo);
		/** Part Revision의 Rev ID 값을 Dataset에 저장한다. */
		revDataSet.setProperty("s7_REVISION_ID", newRevId);

		return revDataSet;
	}

	/**
	 * DataSetName / DataSetRevisionID - ParentItemRevisionID
	 * @Copyright : KIMIES
	 * @author :
	 * @since : 2012. 12. 20.
	 * @param dataSetName
	 * @return
	 * @throws Exception
	 */
	private static String getDataSetName(String strItemID,String revId, String dataSetName, boolean isSucceeded) throws Exception {
		if(dataSetName.indexOf("/") == -1) {
			String errMsg = "Dataset의 Name형식이 잘못되었습니다. 관리자에게 문의하세요.!!";
			MessageBox.post(errMsg, "Error", MessageBox.ERROR);
			throw new Exception(errMsg);
		}

		if( dataSetName.indexOf(";") > 0 )
		{
			dataSetName = dataSetName.substring(0, dataSetName.indexOf(";"));
		}

		// 이전 Dataset Revision ID를 승계하지 않는 경우
		if(!isSucceeded)
		{
			dataSetName = strItemID + "/" + revId;
			return dataSetName;
		}


		if (dataSetName.contains("/") && dataSetName.contains("-")) {
			String dataSetRevId = dataSetName.substring(dataSetName.lastIndexOf("-") + 1, dataSetName.length());

			dataSetName = strItemID + "/" + revId + "-" + getNextDataSetRevID(dataSetRevId);
		} else if(!dataSetName.contains("-")){
			dataSetName = strItemID + "/" + revId + "-" + "A";
		}
		return dataSetName;
	}

	/**
	   * 선택한 아이템리비전의 정보를 화면에 셋팅
	   *
	   * @Copyright : KIMIES
	   * @author :
	 * @return
	   * @since : 2012. 12. 20.
	   */
	public static Map<String, String> setItemInfoForRevise(TCComponentItemRevision component, String strStage)
	{
		Map<String, String> resultMap = new HashMap<String, String>();
	    try
	    {
	    	String strRevType = component.getType();
	    	String strCurrentRevID = component.getProperty("item_revision_id");
        String strCurrentStage = component.getProperty("s7_STAGE");
	    	String strNextRevID = "";
//	    	String strStage = "";
	    	if( SYMCClass.S7_VEHPARTREVISIONTYPE.equals(strRevType) )
	        {

	    		// Stage 값이 'P'인 경우 Revision ID는 숫자 3자리(001->999)
	    		if ("P".equals(strStage))
	    		{
	    		  // target revision이 'P' 이면
	    		  if( "P".equals(strCurrentStage))
	    		  {
	            strNextRevID = CustomUtil.getNextRevID(component.getItem(), new String("Item"));
	    		  }
	    		  // Stage값이 'D' -> 'P'로 변경된다면
	    		  else
	    		  {
	    		    strNextRevID = "000";
	    		  }

	    		}
	    		// Stage 값이 'P'가 아닌 경우 Revision ID는 문자 2자리(A->ZZ)
	    		else
	    		{
	    			strNextRevID = CustomUtil.getNextCustomRevID(strCurrentRevID);

	    		}
	      }
	      else
	      {
	        strNextRevID = CustomUtil.getNextRevID(component.getItem(), new String("Item"));
	      }

//	      String partID = component.getProperty("item_id");
	      String partName = component.getProperty("object_name");

//	      resultMap.put("partId", partID);
	      resultMap.put("revId", strNextRevID);
	      resultMap.put("partName", partName);
//	      resultMap.put("stage", strStage);

	    }
	    catch (Exception e)
	    {
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
							next = true;;
						}
				} else {
					temp.add("Z");
				}
			} else {
				return revId;
			}
		}

		for (int i = temp.size(); i > 0 ; i--) {
			nextRevId += temp.get(i - 1);
		}
		return nextRevId;
	}

	/**
	 *
	 * @method getYear
	 * @date 2013. 11. 28.
	 * @author Yunjae,Jung
	 * @param
	 * @return String
	 * @throws
	 * @see
	 */
    public static String getYear() {
        return getTODAY().substring(2, 4);
    }

	public static ArrayList<String> getWorkflowTask(String templateName, TCSession session) throws TCException {

		ArrayList<String> mecoTask = new ArrayList<String>();

		TCComponentTaskTemplateType compTaskTmpType = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
		TCComponentTaskTemplate template = compTaskTmpType.find(templateName, 0);

		AIFComponentContext[]  aifcomponentContexts = template.getChildren();
		for(AIFComponentContext aifcomponentContext : aifcomponentContexts) {
		    String aifType = aifcomponentContext.getComponent().getType();
			if(aifType.equals("EPMReviewTaskTemplate") && (!(aifcomponentContext.toString().equals("Creator")))) {
				mecoTask.add(aifcomponentContext.toString());
			}
		}
		//force to add reference task on task list.
		mecoTask.add(SDVPropertyConstant.WF_TASK_REFERENCES);
		return mecoTask;
	}
	
	/**
	 * 주어진 Template 이름을 가진 Workflow Template에서 처음으로 시작되는 ReviewTask Task의 이름을
	 * 찾아서 Return하는 함수
	 * SR150604-024 수정으로 인해 추가됨
	 * 2015-10-13 taeku.jeong
	 *  
	 * @param session
	 * @param templateName
	 * @return 조건에 맞는 첫번째 ReviewTask Task 이름을 Return 한다.
	 */
	public static String getFirstEPMReviewTaskName(TCSession session, String templateName){

		String firstTaskName = null;
		
		try {
			TCComponentTaskTemplateType compTaskTmpType = (TCComponentTaskTemplateType)session.getTypeComponent("EPMTaskTemplate");
			TCComponentTaskTemplate template = compTaskTmpType.find(templateName, 0);
			TCComponent[] startSuccessors = template.getReferenceListProperty("start_successors");

			for (int i = 0;startSuccessors!=null && i < startSuccessors.length; i++) {

				String aifType = startSuccessors[i].getType();
				 if(aifType.equals("EPMReviewTaskTemplate") == true) {
						String currentTaskName = startSuccessors[i].getProperty("template_name");
						if(currentTaskName!=null && currentTaskName.trim().length()>0 && currentTaskName.trim().equalsIgnoreCase("Creator")==false){
							firstTaskName = currentTaskName;
						}else{
							firstTaskName = findFirstEPMReviewTaskName(startSuccessors[i]);
						}
				 }else{
					 firstTaskName = findFirstEPMReviewTaskName(startSuccessors[i]);
				 }
				 
				 if(firstTaskName!=null && firstTaskName.trim().length()>0){
					 return firstTaskName;
				 }
				 
			}

			
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		return firstTaskName;
	}
	
	/**
	 * 주어진 Task의 Child Node중에 ReviewTask Task를 발견하면 해당 Task의 이름을 Return한다.
	 * SR150604-024 수정으로 인해 추가됨
	 * 2015-10-13 taeku.jeong
	 * 
	 * @param currentTask
	 * @return
	 */
	private static String findFirstEPMReviewTaskName(TCComponent currentTask){
		String firstTaskName = null;
		
		TCComponent[] successors = null;
		try {
			successors = currentTask.getReferenceListProperty("successors");
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		for (int i = 0;successors!=null && i < successors.length; i++) {
			
			String aifType = successors[i].getType();
			 if(aifType.equals("EPMReviewTaskTemplate") == true) {
					String currentTaskName = null;
					try {
						currentTaskName = successors[i].getProperty("template_name");
					} catch (TCException e) {
						e.printStackTrace();
					}
					
					if(currentTaskName!=null && currentTaskName.trim().length()>0 && currentTaskName.trim().equalsIgnoreCase("Creator")==false){
						firstTaskName = currentTaskName;
						return firstTaskName;
					}else{
						return findFirstEPMReviewTaskName(successors[i]);
					}
					
			 }else{
				 return findFirstEPMReviewTaskName(successors[i]);
			 }
			 
		}
		
		return firstTaskName;
	}

	public static long diffOfDate(String begin, String end) throws Exception {

	    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
	    Date beginDate = formatter.parse(begin);
	    Date endDate = formatter.parse(end);

	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / (24 * 60 * 60 * 1000);



	    return diffDays;
	 }

	public static long diffOfDate(Date beginDate, Date endDate) throws Exception {

//	    SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
//	    Date beginDate = formatter.parse(begin);
//	    Date endDate = formatter.parse(end);

	    long diff = endDate.getTime() - beginDate.getTime();
	    long diffDays = diff / (24 * 60 * 60 * 1000);



	    return diffDays;
	 }

	public static String[] loadWorkflowTemplate(String mecoType) throws Exception {

		TCPreferenceService tcpreferenceservice = getTCSession().getPreferenceService();

//		String[] mecoTaskTemplates = tcpreferenceservice.getStringArray(0, mecoType+".Workflow.Template");
		String[] mecoTaskTemplates = tcpreferenceservice.getStringValues(mecoType+".Workflow.Template");

		return mecoTaskTemplates;
	}

	/**
	 * [NON-SR][20160818] taeku.jeong MECO EPL 생성 절차변경으로 인해 Operation의 EPL 생성은 다른 모듈에서
	 * Preview 단계에 실행 되므로 Operation은 제외 하고 MECO EPL Data를 생성 한다.
	 * @param mecoRevision
	 * @param checkSoltionItem whereReference를 이용하여 MECO를 속성으로 가진 모든 대상을 다시 찾아낼 것인지 여부
	 */
	public ArrayList<SYMCBOPEditData> buildMEPL(TCComponentChangeItemRevision mecoRevision, boolean checkSoltionItem) throws Exception
	{
		if (mecoRevision == null)
		{
			throw new Exception("MECO is Null...");
		}
		ArrayList<SYMCBOPEditData> arrResultEPL = null;
		TCSession session = mecoRevision.getSession();
		session.setStatus("MECO EPL 생성...");
		System.out.println("build mepl 시작...");
		try
		{
			TCComponent[] solutionList = null;
			//4개 type에 대해서 MEPL을 생성하는 것을 제외함. - Preview 단계에서 MEPL이 만들어짐.
			Vector<String> excptoinRevisionTypeVector = new Vector<String>();
//			excptoinRevisionTypeVector.add(SDVTypeConstant.BOP_PROCESS_ASSY_OPERATION_ITEM_REV);
//			excptoinRevisionTypeVector.add(SDVTypeConstant.BOP_PROCESS_BODY_OPERATION_ITEM_REV);
//			excptoinRevisionTypeVector.add(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV);
//			excptoinRevisionTypeVector.add(SDVTypeConstant.BOP_PROCESS_PAINT_OPERATION_ITEM_REV);

//	        solutionList = mecoRevision.getRelatedComponents(SDVTypeConstant.MECO_SOLUTION_ITEM);
			//솔루션 리스트를 찾아옴.
			session.setStatus("MECO EPL : Solution 리스트 생성...");
			System.out.println("솔루션 폴더 리스트 찾아서 붙이기. 또는 그냥 가져오기.");
			if (checkSoltionItem)
			{
				//MECO 리비전 하위 solution, problem을 다시 찾아 붙이거나 떼내는 작업을 수행 한 후 solution아이템들을 가져온다.
				solutionList = getSolutionItemsAfterReGenerate(mecoRevision);
			} else
			{
				solutionList = mecoRevision.getRelatedComponents(SDVTypeConstant.MECO_SOLUTION_ITEM);
			}

			TCComponentItemRevision prevItemRevision = null;
			arrResultEPL = new ArrayList<SYMCBOPEditData>();

			if (solutionList.length == 0)
			{
				MessageBox.post("No exist change data. First perform to create change data.", "Notify", MessageBox.INFORMATION);
				session.setReadyStatus();
				return null;
			}
			int total = solutionList.length;
			int count = 1;
			System.out.println("솔루션 리스트 별 작업 시작...");
			for (TCComponent solTccomponent : solutionList)
			{
				session.setStatus("[" + count + "/" + total + "] " + solTccomponent.toDisplayString());
				System.out.println("[" + count++ + "/" + total + "] " + solTccomponent.toDisplayString());
				// [NON-SR][20160816] Taeku.Jeong 상신과정에 MECO EPL을 다시 생성하는 과정에 문제가 많이 발생되어
				// Operation Revision 종류의 경우 기존 MECO EPL 생성 과정에서 제외하고 PreView 과정에 EPL이 자동 생성 되도록
				// 수정 한다.
				if (solTccomponent != null && solTccomponent instanceof TCComponentItemRevision)
				{
					TCComponentItemRevision solComponent = (TCComponentItemRevision) solTccomponent;
					String itemRevisionTypeName = solComponent.getType();
					if (itemRevisionTypeName != null && itemRevisionTypeName.trim().length() > 0)
					{
						if (excptoinRevisionTypeVector.contains(itemRevisionTypeName.trim()) == true)
						{
							continue;
						}
					}
				}

				System.out.println(" -> 이전 리비전 찾음...");
				//TODO: Compare PARENT_MOD_DATE in MECO_EPL to BOMView Last Mod Date of Solution ItemRevision
				//SYMCBOPEditData.setParent_mod_date(simpleDateFormat.format(tcBomviewRevision.getDateProperty(SDVPropertyConstant.ITEM_LAST_MODIFY_DATE)));
				prevItemRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision) solTccomponent);
				System.out.println("이전 이후에대해 비교 로직 시작.......");
				arrResultEPL.addAll(compareChildItems(mecoRevision, solTccomponent, prevItemRevision, solutionList));
			}
			session.setStatus("MECO EPL : 이전 데이터 삭제...");
			deleteMECOEPL(mecoRevision);
			session.setStatus("MECO EPL : 새 데이터 저장...");
			insertMECOEPL(arrResultEPL);
			session.setReadyStatus();
		} catch (Exception e)
		{
			e.printStackTrace();
			session.setReadyStatus();
			throw e;
		}
		return arrResultEPL;
	}

	private void makeMEPLData(String mecoId, String mecoUserId, ArrayList<HashMap> meplResultList, ArrayList<SYMCBOPEditData> arrMecoBomData)
	{
		for(HashMap<String, String> meplHashmap : meplResultList)
		{
			SYMCBOPEditData symcBOPEditData = new SYMCBOPEditData();

			symcBOPEditData.setMecoNo(mecoId);
			symcBOPEditData.setUserId(mecoUserId);

			symcBOPEditData.setParentPuid(meplHashmap.get("PARENT_REV_PUID"));
			symcBOPEditData.setParentNo(meplHashmap.get("PARENT_ID"));
			symcBOPEditData.setParentRev(meplHashmap.get("PARENT_REV_ID"));
			symcBOPEditData.setParentName(meplHashmap.get("PARENT_NAME"));
			symcBOPEditData.setParentType(meplHashmap.get("PARENT_REV_TYPE"));
			symcBOPEditData.setParent_mod_date(meplHashmap.get("PARENT_STRUCT_LAST_MOD_DATE"));

			symcBOPEditData.setOld_child_puid(meplHashmap.get("OLD_CHILD_REVU"));
			symcBOPEditData.setOld_child_no(meplHashmap.get("OLD_CHILD_ID"));
			symcBOPEditData.setOld_child_rev(meplHashmap.get("OLD_CHILD_REV"));
			symcBOPEditData.setOld_child_name(meplHashmap.get("OLD_ITEM_NAME"));
			symcBOPEditData.setOld_child_type(meplHashmap.get("OLD_ITEM_TYPE"));
			symcBOPEditData.setOld_qty(meplHashmap.get("OLD_QTY_V"));
			symcBOPEditData.setOld_vc(meplHashmap.get("OLD_OPTION_COND"));
			symcBOPEditData.setOld_occ_uid(meplHashmap.get("OLD_OCC_PUID"));

			symcBOPEditData.setNew_child_puid(meplHashmap.get("NEW_CHILD_REVU"));
			symcBOPEditData.setNew_child_no(meplHashmap.get("NEW_CHILD_ID"));
			symcBOPEditData.setNew_child_rev(meplHashmap.get("NEW_CHILD_REV"));
			symcBOPEditData.setNew_child_name(meplHashmap.get("NEW_ITEM_NAME"));
			symcBOPEditData.setNew_child_type(meplHashmap.get("NEW_ITEM_TYPE"));
			symcBOPEditData.setNew_qty(meplHashmap.get("NEW_QTY_V"));
			symcBOPEditData.setNew_vc(meplHashmap.get("NEW_OPTION_COND"));
			symcBOPEditData.setNew_occ_uid(meplHashmap.get("NEW_OCC_PUID"));

			symcBOPEditData.setChangeType(meplHashmap.get("CHANGE_TYPE"));
			symcBOPEditData.setSeq(meplHashmap.get("NEW_ORDER_NO"));
			//삭제일 경우 new 가 없다...
			if(symcBOPEditData.getChangeType().equalsIgnoreCase("D"))
			{
				symcBOPEditData.setSeq(meplHashmap.get("OLD_ORDER_NO"));
			}
			//자 아이템 타입이 sub... 어쩌고 이면 수량을 다른걸 넣는다.
			if(SDVTypeConstant.BOP_PROCESS_SUBSIDIARY_ITEM.equalsIgnoreCase(symcBOPEditData.getOld_child_type()))
			{
				symcBOPEditData.setOld_qty(meplHashmap.get("OLD_SUBSIDIARY_QTY"));
				symcBOPEditData.setNew_qty(meplHashmap.get("NEW_SUBSIDIARY_QTY"));
			}

			arrMecoBomData.add(symcBOPEditData);
		}
	}
	/**
	 * [SR150122-027][20150210] shcho, Find automatically replaced end item (공법 할당 E/Item의 설계 DPV에 의한 자동 변경 오류 해결) (10버전에서 개발 된 소스 9으로 이식함)
	 * 
	 */
	private ArrayList<SYMCBOPEditData> compareChildItems(TCComponentChangeItemRevision mecoRevision, TCComponent solutionComponent, TCComponent problemComponent, TCComponent[] solutionList) throws Exception
	{
		ArrayList<SYMCBOPEditData> arrMecoBomData = new ArrayList<SYMCBOPEditData>();
		try
		{
			System.out.println("문제, 솔루션 bop 라인 정보 추출...");
			//Get parent of SolutionItem, but except parent in solutionFolder.
			ArrayList<TCComponent> arrParentList = addHistoryOnlyRevise((TCComponentItemRevision) solutionComponent, solutionList);
			System.out.println("parent 검색...완료");
			String mecoId = mecoRevision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			String mecoUserId = ((TCComponentUser) mecoRevision.getRelatedComponent(SDVPropertyConstant.ITEM_OWNING_USER)).getUserId();
			String solutionItemId = solutionComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
			String oldRevId = null;
			if (problemComponent != null)
			{
				oldRevId = problemComponent.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
			}
			String newRevId = solutionComponent.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
			//솔루션 아이템 자체 변경 EPL 정보를 만들어냄.
			//모를 찾아내어 자신이 자인 경우의 정보를 만드는 것임.
			for (int i = 0; arrParentList != null && i < arrParentList.size(); i++)
			{
				TCComponent parentComponent = arrParentList.get(i);
				String parentItemId = parentComponent.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
				String parentRevId = parentComponent.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
				ArrayList<HashMap> resultList = MECOCreationUtil.getMEPLResultList(mecoId, parentItemId, parentRevId, parentRevId, solutionItemId);
				makeMEPLData(mecoId, mecoUserId, resultList, arrMecoBomData);
			}
			//솔루션의 하위에 대해 EPL정보를 만들어낸다.
			ArrayList<HashMap> resultList = MECOCreationUtil.getMEPLResultList(mecoId, solutionItemId, oldRevId, newRevId, null);
			makeMEPLData(mecoId, mecoUserId, resultList, arrMecoBomData);
			System.out.println("솔루션에 대한 정보 생성 완료.");
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return arrMecoBomData;
	}

	private ArrayList<TCComponent> addHistoryOnlyRevise(TCComponentItemRevision tcItemRevision, TCComponent[] solutionList) throws TCException
	{

		ArrayList<TCComponent> parentTccomponentlist = new ArrayList<TCComponent>();
		TCComponentRevisionRule[] allRevisionRules = TCComponentRevisionRule.listAllRules(getTCSession());
		TCComponentRevisionRule latestReleasedRule = null;
		for (TCComponentRevisionRule revisionRule : allRevisionRules)
		{
			String ruleName = revisionRule.getProperty("object_name");
			if (ruleName.equals("Latest Working For ME"))
			{
				latestReleasedRule = revisionRule;
				break;
			}
		}
		ArrayList<TCComponent> arrTccomponent = SYMTcUtil.getWhereUsed(tcItemRevision, new String[] {}, latestReleasedRule);

		//찾은 parent를 걸러준다... parent의 모든 리비전이 나올 수 있으므로 나온것 중에 최신리비전만 허용한다.
		//왜냐하면 최신 리비전에서 잘려졌을 수도 있기 때문...
		//그리고 솔루션에 있는 것들은 제외한다. 솔루션아이템별로 MEPL을 또 만들기 때문에 불필요하다.
		for (TCComponent parentComponent : arrTccomponent)
		{
			//현재 리비전이 최종 리비전이 아니면 제외...
			TCComponentItem parentItem = ((TCComponentItemRevision) parentComponent).getItem();
			if (!parentItem.getLatestItemRevision().equals(parentComponent))
			{
				continue;
			}
			//현재 아이템이 솔루션에 포함되어 있으면 제외
			boolean isContainSolution = false;
			for (TCComponent solComponent : solutionList)
			{
				TCComponentItem solItem = ((TCComponentItemRevision) solComponent).getItem();
				if (solItem.equals(parentItem))
				{
					isContainSolution = true;
					break;
				}
			}
			if (isContainSolution)
			{
				continue;
			}
			parentTccomponentlist.add(parentComponent);
		}
		return parentTccomponentlist;
	}

	private void deleteMECOEPL(TCComponentChangeItemRevision mecoRevision) {
		CustomMECODao dao = null;
		try {
			dao = new CustomMECODao();
			 dao.deleteMECOEPL(mecoRevision.getItem().toString());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private void insertMECOEPL(ArrayList<SYMCBOPEditData> arrSYMCBOPEditData) {

		CustomMECODao dao = null;
		try {
			dao = new CustomMECODao();
			dao.insertMECOEPL(arrSYMCBOPEditData);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static ArrayList<HashMap<String, String>> getRevisionEffectivityReferencedByMeco() throws Exception {

		ArrayList<HashMap<String, String>> array_effectivity_event = null;

		TCPreferenceService tcpreferenceservice = getTCSession().getPreferenceService();

//		String[] effectivity_items = tcpreferenceservice.getStringArray(TCPreferenceService.TC_preference_site, "REVISION_EFFECTIVITY_MANAGE_ITEM");
		String[] effectivity_items = tcpreferenceservice.getStringValuesAtLocation("REVISION_EFFECTIVITY_MANAGE_ITEM", TCPreferenceLocation.OVERLAY_LOCATION);
		array_effectivity_event = new ArrayList<HashMap<String,String>>();
		
		for(String effectivity_item : effectivity_items ) {
			
			TCComponentItem tcComponentEffectivityItem = SDVBOPUtilities.FindItem(effectivity_item, "Item"); //M7_EffectivityRule
			if(tcComponentEffectivityItem==null){
				continue;
			}
			
			TCComponentItemRevision tcComponentEffectivityItemRevision = tcComponentEffectivityItem.getLatestItemRevision();
			
			TCComponent[] releaseStatusList = tcComponentEffectivityItemRevision.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
			
			TCComponentReleaseStatus rel_status = (TCComponentReleaseStatus) releaseStatusList[0];
			
			TCComponentEffectivity effectivities[] = rel_status.getEffectivities();
			
			
			HashMap<String, String> mapEffectivity = null;
			String[] propsEffectivity = {SDVPropertyConstant.EFFECTIVITY_ID, SDVPropertyConstant.EFFECTIVITY_STATUS };
			String[] propsValEffectivity = null;
			
			for(TCComponentEffectivity tccomponentEffectivity : effectivities) {
				mapEffectivity = new HashMap<String, String>();
				propsValEffectivity = tccomponentEffectivity.getProperties(propsEffectivity);
				
				if(propsValEffectivity[0].startsWith(SDVPropertyConstant.EFFECTIVITY_EVENT_PREFIX) && propsValEffectivity[1].equals("1")) {
					mapEffectivity = new HashMap<String, String>();
					mapEffectivity.put(SDVPropertyConstant.EFFECTIVITY_ID, propsValEffectivity[0]);
					mapEffectivity.put(SDVPropertyConstant.EFFECTIVITY_DATES,  new SimpleDateFormat("yyyy-MM-dd").format(tccomponentEffectivity.askDates()[0]));
					array_effectivity_event.add(mapEffectivity);
				}
			}
		}
		return array_effectivity_event;
	}

	private String[] boplineProperties =
		{
			SDVPropertyConstant.BL_ITEM_ID,  //0
			SDVPropertyConstant.BL_OCC_INT_ORDER_NO, //1
			SDVPropertyConstant.BL_OBJECT_TYPE, //2
			SDVPropertyConstant.BL_REV_OBJECT_NAME,
			SDVPropertyConstant.BL_QUANTITY,//4
			SDVPropertyConstant.BL_VARIANT_CONDITION,
			SDVPropertyConstant.BL_OCC_FND_OBJECT_ID, //6
			SDVPropertyConstant.BL_ITEM_REV_ID,
			SDVPropertyConstant.BL_ABS_OCC_ID,//8
			SDVPropertyConstant.BL_NOTE_SUBSIDIARY_QTY //9

		};

	/**
	 * MECO Reload.
	 * Solution Items, Problem Items Setting.
	 * 완료 후 Solution Item 목록 반환.
	 * [SR141117-014][2014.12.19][jclee] Problem Items Setting 로직 추가.
	 * @param changeRevision MECO
	 * @return Solution Items List
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused", "unchecked" })
    public static TCComponent[] getSolutionItemsAfterReGenerate(TCComponentChangeItemRevision changeRevision) throws Exception {
		// 중복 방지 삭제
		System.out.println("솔루션 아이템 찾기 및 다시 붙여넣기...");
		TCComponent[] solutionList = changeRevision.getRelatedComponents(SDVTypeConstant.MECO_SOLUTION_ITEM);
		TCComponent[] problemList = changeRevision.getRelatedComponents(SDVTypeConstant.MECO_PROBLEM_ITEM);
		System.out.println("기존 문제, 솔루션 찾아내기.");

		HashMap<String, TCComponent> solutionMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> problemMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> changeItemMap = new HashMap<String, TCComponent>();
		HashMap<String, TCComponent> changeItemPrevRevMap = new HashMap<String, TCComponent>();	// 변경된 Item Revision의 이전 Revision 목록. Problem Items에 추가 또는 삭제될 대상 비교 목적.

		for(TCComponent tccomponent : solutionList) {
			solutionMap.put(tccomponent.getUid(), tccomponent);
		}

		// [SR141117-014][2014.12.19][jclee] Problem Items의 UID 목록 추출
		for(TCComponent tccomponent : problemList) {
			problemMap.put(tccomponent.getUid(), tccomponent);
		}

		System.out.println("meco의 사용처 검색 where reference... 이전 리비전도 찾음.");
		AIFComponentContext[] aifcomponentcontexts = changeRevision.whereReferenced();
		ArrayList<String> changedItemRevison = new ArrayList<String>();
		for(AIFComponentContext aifcomponentcontext : aifcomponentcontexts) {

		    //[SR141208-003][20141205] shcho, changeRevision.whereReferenced 중 MECOItem은 SolutionItem Attach에서 제외한다.
		    //[SR없음][2015320] shcho, changeRevision.whereReferenced 중 TCComponentItemRevision이 아닌 것은 SolutionItem Attach에서 제외하는 것으로 변경.
			if(!"0".equals(aifcomponentcontext.getComponent().getProperty("active_seq")) && (aifcomponentcontext.getComponent() instanceof TCComponentItemRevision)) {
                changeItemMap.put(aifcomponentcontext.getComponent().getUid(), (TCComponent)aifcomponentcontext.getComponent());  
                
                // [SR141117-014][2014.12.19][jclee] 이전 Revision 추출 후 Problem Items 추가 대상 목록에 추가
                TCComponentItemRevision preRevision = CustomUtil.getPreviousRevision((TCComponentItemRevision)aifcomponentcontext.getComponent());
                if (preRevision != null) {
                	// 현재 Revision이 First Revision일 경우 Null이 되며, Problem Items 비교대상에 추가되지 않는다.
                	changeItemPrevRevMap.put(preRevision.getUid(), preRevision);
				}
            }else{
                //Skip.
            }

		}

		System.out.println("찾은것들을 이용 추가 삭제...");
		Object[] noAttach_solution_ids = CollectionUtils.subtract(changeItemMap.keySet(),solutionMap.keySet()).toArray(new Object[0]);
		Object[] dettach_solution_ids =  CollectionUtils.subtract(solutionMap.keySet(), changeItemMap.keySet()).toArray(new Object[0]);
		
		// [SR141117-014][2014.12.19][jclee] 기존 Problem Items에 존재하는 목록과 현재 변경된 Problem Items 목록 비교. 추가해야할 대상과 삭제해야될 대상 구분.
		Object[] noAttach_problem_ids = CollectionUtils.subtract(changeItemPrevRevMap.keySet(),problemMap.keySet()).toArray(new Object[0]);
		Object[] dettach_problem_ids =  CollectionUtils.subtract(problemMap.keySet(), changeItemPrevRevMap.keySet()).toArray(new Object[0]);

		int resultCount1 = noAttach_solution_ids.length;
		if(noAttach_solution_ids!=null && resultCount1 > 0){


			String[] itemRevisions = new String[resultCount1];
			for(int i = 0 ; i < resultCount1 ; i++){
				itemRevisions[i] = noAttach_solution_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0) {
				changeRevision.add(SDVTypeConstant.MECO_SOLUTION_ITEM, tcComponents);
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
				changeRevision.remove(SDVTypeConstant.MECO_SOLUTION_ITEM,tcComponents);
		}
		
		// [SR141117-014][2014.12.19][jclee] Problem Items 추가 리스트 반영
		int resultCount3 = noAttach_problem_ids.length;
		if(noAttach_problem_ids!=null && resultCount3 > 0){
			
			String[] itemRevisions = new String[resultCount3];
			for(int i = 0 ; i < resultCount3 ; i++){
				itemRevisions[i] = noAttach_problem_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0) {
				changeRevision.add(SDVTypeConstant.MECO_PROBLEM_ITEM, tcComponents);
			}
		}
		
		// [SR141117-014][2014.12.19][jclee] Problem Items 제거 리스트 반영
		int resultCount4 = dettach_problem_ids.length;
		if(dettach_problem_ids!=null && resultCount4 > 0){
			
			String[] itemRevisions = new String[resultCount4];
			for(int i = 0 ; i < resultCount4 ; i++){
				itemRevisions[i] = dettach_problem_ids[i].toString();
			}
			TCComponent[] tcComponents = getTCSession().stringToComponent(itemRevisions);
			if(tcComponents.length > 0)
				changeRevision.remove(SDVTypeConstant.MECO_PROBLEM_ITEM,tcComponents);
		}
		System.out.println("추가 삭제 끝////");

		return changeRevision.getRelatedComponents(SDVTypeConstant.MECO_SOLUTION_ITEM);
	}


	/**
	 *  현재 가지고 있는 BOMLine 다른 Window 에서 찾을때 사용
	 *
	 * @method findOtherWindowBomLine
	 * @date 2014. 3. 25.
	 * @param  topBomLine          (찾으려는 대상이 있는 Window 에 TOPBomLine)
	 * @param  bomLines            (찾으려는 원본 BomLines)
	 * @param  absStatus           (찾으려는 봄라인이 bl_abs_occ_id 를 가지고 있다면 true, 없으면 false)
	 * @param  searchAllContext    (default false)
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
    public static TCComponent[] findOtherWindowBomLine(TCComponent topBomLine, TCComponent[] bomLines, boolean absStatus, boolean searchAllContext) throws TCException  {
        CoreService coreservice = CoreService.getService(CustomUtil.getTCSession());
        ArrayList<FindNodeInContextInputInfo> arraylist = new ArrayList<FindNodeInContextInputInfo>(1);
        FindNodeInContextInputInfo findnodeincontextinputinfo = null;
        findnodeincontextinputinfo = new FindNodeInContextInputInfo();
        findnodeincontextinputinfo.context = topBomLine;
        findnodeincontextinputinfo.nodes = bomLines;
        findnodeincontextinputinfo.byIdOnly = absStatus;
        findnodeincontextinputinfo.allContexts = searchAllContext;
        findnodeincontextinputinfo.inContextLine = null;
        arraylist.add(0, findnodeincontextinputinfo);
        FindNodeInContextInputInfo afindnodeincontextinputinfo[] = (FindNodeInContextInputInfo[])arraylist.toArray(new FindNodeInContextInputInfo[arraylist.size()]);
        FindNodeInContextResponse findnodeincontextresponse = coreservice.findNodeInContext(afindnodeincontextinputinfo);
        if(findnodeincontextresponse != null)
        {
            //SoaUtil.handlePartialErrors(findnodeincontextresponse.serviceData, );
            if(findnodeincontextresponse.resultInfo != null){
                for (FoundNodesInfo findNodeInContextInputInfo2 : findnodeincontextresponse.resultInfo) {
                    for (NodeInfo nodeInfo : findNodeInContextInputInfo2.resultNodes) {
                        if (nodeInfo.foundNodes.length >0 ) {
                            TCComponent[] foundBomLine = (TCComponent[])nodeInfo.foundNodes;
                            return foundBomLine;
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /**
     * [NON-SR][2016.01.07] taeku.jeong PE->TC Migration Test 진행중 Exception발생으로 Problem Items에 이미 존재하는지 Check 하도록 Function을 추가함
     * @param changeRevision
     * @param itemRevision
     * @return
     */
    public static boolean isExistInProblemItems(TCComponentChangeItemRevision changeRevision, TCComponentItemRevision itemRevision){

    	boolean isExist = false;
    		//TCComponent[] solutionList = changeRevision.getRelatedComponents(SDVTypeConstant.MECO_SOLUTION_ITEM);
    		TCComponent[] problemList = null;
			try {
				problemList = changeRevision.getRelatedComponents(SDVTypeConstant.MECO_PROBLEM_ITEM);
			} catch (TCException e) {
				e.printStackTrace();
			}
    		
    	for (int i = 0;problemList!=null && i < problemList.length; i++) {
    		if(problemList[i].equals(itemRevision)==true){
    			isExist = true;
    			break;
    		}
		}
    	
    	return isExist;
    }

    /**
     * [NON-SR][2016.01.07] taeku.jeong PE->TC Migration Test 진행중 Exception발생으로 Solution Items에 이미 존재하는지 Check 하도록 Function을 추가함
     * @param changeRevision
     * @param itemRevision
     * @return
     */
    public static boolean isExistInSolutionItems(TCComponentChangeItemRevision changeRevision, TCComponentItemRevision itemRevision){

    	boolean isExist = false;
    		TCComponent[] solutionList = null;
			try {
				solutionList = changeRevision.getRelatedComponents(SDVTypeConstant.MECO_SOLUTION_ITEM);
			} catch (TCException e) {
				e.printStackTrace();
			}
    		
    	for (int i = 0;solutionList!=null && i < solutionList.length; i++) {
    		if(solutionList[i].equals(itemRevision)==true){
    			isExist = true;
    			break;
    		}
		}
    	
    	return isExist;
    }

}
