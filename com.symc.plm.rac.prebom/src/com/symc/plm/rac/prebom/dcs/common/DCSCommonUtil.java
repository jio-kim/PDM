package com.symc.plm.rac.prebom.dcs.common;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import com.ssangyong.common.remote.DataSet;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.SavedQueryConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.symc.plm.rac.prebom.common.util.SDVQueryUtils;
import com.symc.plm.rac.prebom.dcs.service.DCSHitsQueryService;
import com.symc.plm.rac.prebom.dcs.service.DCSQueryService;
import com.symc.plm.rac.prebom.dcs.service.DCSVisionNetQueryService;
import com.symc.plm.rac.prebom.dcs.service.ENVService;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.NamedReferenceContext;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentGroupMember;
import com.teamcenter.rac.kernel.TCComponentGroupType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCComponentUserType;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.Registry;

/**
 * [DCS 현업 사용문제 신고내역] [20150708][ymjang] InActive 사용자만 조회되도록 쿼리 수정함.
 * [20151005][ymjang] 첨부문서의 경우, 결재 진행중인 경우, 삭제시 Reference 오류 발생함.
 * [20151006][ymjang] 사용자 비활성화 여부와 그룹 멤버의 비활성화 여부도 함께 체크함.
 * [20151223][ymjang] 조직개편으로 팀코드 변경. 협의 완료된 경우는 변경전 코드로 팀정보를 가져올 수 있도록 개선함.
 * [20160308][ymjang] 조직 변경 및 결재 상세 정보 속도 개선을 위한 결재 History 생성 기능 추가
 * [20160328][ymjang] 조직변경으로 인하여 현재 존재하지 않는 팀코드인 경우, Skip
 * [20160504][ymjang] 시간 24시간 타입으로 변경함.
 * [20180410][csh] 활성 상태인 그룹맴버만 가져오도록 변경 getGroupMembers()
 */
public class DCSCommonUtil {

	private static Registry registry = Registry.getRegistry(DCSCommonUtil.class);
	private static TCSession session = (TCSession) AIFUtility.getDefaultSession();

	private static DCSHitsQueryService dcsHitsQueryService = new DCSHitsQueryService();
	private static DCSVisionNetQueryService dcsVisionNetQueryService = new DCSVisionNetQueryService();

	public DCSCommonUtil() {

	}

	public static TCSession getTCSession() {
		return session;
	}

	public static TCComponentItem getItem(String itemID) throws Exception {
		TCComponentItem item = null;

		TCComponentItemType itemType = (TCComponentItemType) getTCSession().getTypeComponent("Item");
		TCComponentItem[] items = itemType.findItems(itemID);
		if (items != null && items.length > 0) {
			item = items[0];
		}

		return item;
	}

	public static TCComponentItemRevision getItemRevision(String itemId, String revId) throws Exception {
		TCComponentItemRevision itemRevision = null;

		TCComponentItemRevisionType itemRevisionType = (TCComponentItemRevisionType) session.getTypeComponent("ItemRevision");
		TCComponentItemRevision[] itemRevisions = itemRevisionType.findRevisions(itemId, revId);
		if (itemRevisions != null && itemRevisions.length > 0) {
			itemRevision = itemRevisions[0];
		}

		return itemRevision;
	}

	public static TCComponentItemRevision getLatestItemRevision(String type, String itemId) throws Exception {
		TCComponentItem item = SDVPreBOMUtilities.findItem(type, itemId);
		TCComponentItemRevision itemRevision = item.getLatestItemRevision();

		return itemRevision;
	}

	public static TCComponentDataset getDataset(TCComponentItemRevision itemRevision, String relation) {
		TCComponentDataset dataset = null;

		try {
			TCComponent component = itemRevision.getRelatedComponent(relation);
			dataset = (TCComponentDataset) component;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return dataset;
	}

	public static TCComponent[] getDatasets(TCComponentItemRevision itemRevision, String relation) {
		TCComponent[] components = null;

		try {
			components = itemRevision.getRelatedComponents(relation);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return components;
	}

	public static void createDataset(TCComponentItemRevision itemRevision, String relation, String datasetName, String fileName) throws Exception {
		TCComponentDataset dataset = SDVPreBOMUtilities.createDataset(fileName, datasetName);
		dataset.setLogicalProperty(PropertyConstant.ATTR_NAME_ISDCS, true);
		TCComponentTcFile[] tcFile = dataset.getTcFiles();
		tcFile[0].setOriginalFileName(dataset, datasetName + DCSFileUtil.getFileExtension(fileName));
		itemRevision.add(relation, dataset);
	}

	// [20151005][ymjang] 첨부문서의 경우, 결재 진행중인 경우, 삭제시 Reference 오류 발생함. 
	// 신규 추가
	public static TCComponentDataset createDatasetWithReturn(TCComponentItemRevision itemRevision, String relation, String datasetName, String fileName) throws Exception {
		TCComponentDataset dataset = SDVPreBOMUtilities.createDataset(fileName, datasetName);
		dataset.setLogicalProperty(PropertyConstant.ATTR_NAME_ISDCS, true);
		TCComponentTcFile[] tcFile = dataset.getTcFiles();
		tcFile[0].setOriginalFileName(dataset, datasetName + DCSFileUtil.getFileExtension(fileName));
		itemRevision.add(relation, dataset);
		return dataset;
	}
	
	public static void deleteDataset(TCComponentItemRevision itemRevision, String relation) throws Exception {
		TCComponent[] components = itemRevision.getRelatedComponents(relation);
		for (TCComponent component : components) {
			TCComponentDataset dataset = (TCComponentDataset) component;
			itemRevision.remove(relation, dataset);
			dataset.delete();
		}
	}

	public static void removeAllNamedReference(TCComponentDataset dataset) throws Exception {
		NamedReferenceContext[] contexts = dataset.getDatasetDefinitionComponent().getNamedReferenceContexts();
		for (int i = 0; i < contexts.length; i++) {
			String namedReference = contexts[i].getNamedReference();
			dataset.removeNamedReference(namedReference);
		}
	}

	public static TCComponentTcFile getNamedReferencesFile(TCComponentDataset dataset) {
		TCComponentTcFile tcFile = null;

		try {
			TCComponent[] component = dataset.getNamedReferences();
			tcFile = (TCComponentTcFile) component[0];
		} catch (Exception e) {
			e.printStackTrace();
		}

		return tcFile;
	}

	public static String getNamedReferencesFileName(TCComponentDataset dataset) {
		String fileName = null;

		try {
			TCComponent[] component = dataset.getNamedReferences();
			TCComponentTcFile tcFile = (TCComponentTcFile) component[0];
			fileName = tcFile.getProperty("original_file_name");
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fileName;
	}

	public static TCComponentUser getUser(String userId) throws Exception {
		TCComponentUser user = null;

		TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
		user = userType.find(userId);

		return user;
	}

	public static TCComponentGroup getGroup(String groupName) throws Exception {
		TCComponentGroup group = null;

		try {
			TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
			group = groupType.find(groupName);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return group;
	}

	public static TCComponentGroup getGroupInTeamcenter(String vNetTeamCode) {
		Shell shell = Display.getCurrent().getActiveShell();

		TCComponentGroup group = null;

		try {
			String queryName = SavedQueryConstant.SEARCHGROUP;
			String[] entryNames = new String[] { "Group Description" };
			String[] entryValues = new String[] { vNetTeamCode };

			TCComponent[] groupList = SDVQueryUtils.executeSavedQuery(queryName, entryNames, entryValues);
			if (groupList.length > 0) {
				group = (TCComponentGroup) groupList[0];
			}
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(shell, "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		return group;
	}
	
	/**
	 * [SR160920-012][20161012] taeku.jeong Vision Net 과 Teamcenter 두 System에서 유효한 상태가 아닌 팀정보를
	 * 읽어 Message로 만들어 Return 한다.
	 * @param consultationDeptCodeList
	 * @param referenceDeptCodeList
	 * @return
	 */
	public static String notValideTeamMessage( List<String> consultationDeptCodeList, List<String> referenceDeptCodeList){
		
		String messageBody = null;
		
		Vector<String> notValideConsultationTeam = new Vector<String>();
		Vector<String> notValideReferenceTeam = new Vector<String>();
		
		ArrayList<String> valideTeamCode = dcsVisionNetQueryService.getVnetAndTcLiveSameTeamCode();
		
		if(valideTeamCode==null || (valideTeamCode!=null && valideTeamCode.size()<1)){
			return messageBody;
		}
		
		for (int i = 0; consultationDeptCodeList!=null && i < consultationDeptCodeList.size(); i++) {
			String consultationDeptCode = consultationDeptCodeList.get(i);
			if(consultationDeptCode==null || (consultationDeptCode!=null && consultationDeptCode.trim().length()<1)){
				continue;
			}
			
			if(valideTeamCode.contains(consultationDeptCode.trim())==false){
				if(notValideConsultationTeam.contains(consultationDeptCode)==false){
					notValideConsultationTeam.add(consultationDeptCode);
				}
			}
		}
		
		for (int i = 0; referenceDeptCodeList!=null && i < referenceDeptCodeList.size(); i++) {
			String referenceDeptCode = referenceDeptCodeList.get(i);
			if(referenceDeptCode==null || (referenceDeptCode!=null && referenceDeptCode.trim().length()<1)){
				continue;
			}
			referenceDeptCode = referenceDeptCode.trim();
			if(valideTeamCode.contains(referenceDeptCode)==false){
				if(notValideReferenceTeam.contains(referenceDeptCode)==false){
					notValideReferenceTeam.add(referenceDeptCode);
				}
			}
		}
		
		String consultationDeptTitle = "" + registry.getString("ConsultationDept.NAME", "협의부서");
		String referenceDeptTitle = "" + registry.getString("ReferenceDept.NAME", "참조부서");

		int count = 0;
		for (int i = 0; notValideConsultationTeam!=null && i < notValideConsultationTeam.size(); i++) {
			String consultationDeptCode = notValideConsultationTeam.get(i);
			
			if(consultationDeptCode!=null && consultationDeptCode.trim().length()>0){
				ArrayList<HashMap<String, Object>> resultList = dcsVisionNetQueryService.getVnetTeamName(consultationDeptCode);
				for (int j = 0; resultList!=null && j < resultList.size(); j++) {
					String tName = null;
					HashMap<String, Object> rowHash = resultList.get(j);
					if(rowHash!=null){
						Object tNameObject = rowHash.get("TEAM_NAME");
						if(tNameObject!=null){
							tName = tNameObject.toString();
						}
						//Object tEngNameObject = rowHash.get("TEAM_E_NAME");
						//if(tEngNameObject!=null){
						//	String tEName = tEngNameObject.toString();
						//}
					}
					
					if(tName!=null && tName.trim().length()>0){
						String tempStr = tName+"["+consultationDeptCode.trim()+"]";
						if(messageBody==null || (messageBody!=null && messageBody.trim().length()<1)){
							messageBody = consultationDeptTitle+" : "+tempStr;
						}else{
							messageBody = messageBody+", "+tempStr;
						}
						count++;
					}
				}
			}
		}
		
		count = 0;
		for (int i = 0; notValideReferenceTeam!=null && i < notValideReferenceTeam.size(); i++) {
			String referenceTeamCode = notValideReferenceTeam.get(i);
			if(referenceTeamCode!=null && referenceTeamCode.trim().length()>0){
				ArrayList<HashMap<String, Object>> resultList = dcsVisionNetQueryService.getVnetTeamName(referenceTeamCode);
				for (int j = 0; resultList!=null && j < resultList.size(); j++) {
					String tName = null;
					HashMap<String, Object> rowHash = resultList.get(j);
					if(rowHash!=null){
						Object tNameObject = rowHash.get("TEAM_NAME");
						if(tNameObject!=null){
							tName = tNameObject.toString();
						}
						//Object tEngNameObject = rowHash.get("TEAM_E_NAME");
						//if(tEngNameObject!=null){
						//	String tEName = tEngNameObject.toString();
						//}
					}
					
					if(tName!=null && tName.trim().length()>0){
						String tempStr = tName+"["+referenceTeamCode.trim()+"]";
						if(messageBody==null || (messageBody!=null && messageBody.trim().length()<1)){
								messageBody = referenceDeptTitle+" : "+tempStr;
						}else{
							if(count==0){
								messageBody = messageBody+"\n"+referenceDeptTitle+" : "+tempStr;
							}else{
								messageBody = messageBody+", "+tempStr;
							}
						}
						count++;
					}
				}
			}
		}
		
		return messageBody;
	}
	
	/**
	 * [20160328][ymjang] 조직변경으로 인하여 현재 존재하지 않는 팀코드 유무 체크
	 */
	public static HashMap<String, Object> isExistGroupInVnet( List<String> consultationDeptCodeList, List<String> referenceDeptCodeList) {
		Shell shell = Display.getCurrent().getActiveShell();

		HashMap<String, Object> dataMap = new HashMap<String, Object>();

		List<TCComponentGroup> consultationDeptList = new ArrayList<TCComponentGroup>();
		List<TCComponentGroup> referenceDeptList = new ArrayList<TCComponentGroup>();

		StringBuilder message_1 = new StringBuilder();
		StringBuilder message_2 = new StringBuilder();

		try {
			if (consultationDeptCodeList != null) {
				for (int i = 0; i < consultationDeptCodeList.size(); i++) {
					String vNetTeamCode = consultationDeptCodeList.get(i);
					String vNetTeamName = getVNetTeamName(vNetTeamCode);
					if (vNetTeamName == null) {
						if (message_1.toString().isEmpty()) {
							message_1.append(registry.getString("ConsultationDept.NAME") + " : ");
						}
						message_1.append("'" + vNetTeamCode + "'" + ", ");
					}
				}

				if (!message_1.toString().isEmpty()) {
					message_1 = message_1.delete(message_1.lastIndexOf(","), message_1.length());
				}
			}

			if (referenceDeptCodeList != null) {
				for (int i = 0; i < referenceDeptCodeList.size(); i++) {
					String vNetTeamCode = referenceDeptCodeList.get(i);
					String vNetTeamName = getVNetTeamName(vNetTeamCode);

					if (vNetTeamName == null) {
						if (message_2.toString().isEmpty()) {
							message_2.append(registry.getString("ReferenceDept.NAME") + " : ");
						}
						message_2.append("'" + vNetTeamCode + "'" + ", ");
					}
				}

				if (!message_2.toString().isEmpty()) {
					message_2 = message_2.delete(message_2.lastIndexOf(","), message_2.length());
				}
			}

			String message = message_1.toString();
			if (!message_2.toString().isEmpty()) {
				message = message_1.toString() + "\n" + message_2.toString();
			}

			dataMap.put("message", message);
			dataMap.put("consultationDeptList", consultationDeptList);
			dataMap.put("referenceDeptList", referenceDeptList);
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(shell, "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		return dataMap;
	}
	
	public static HashMap<String, Object> isExistGroupInTeamcenter( List<String> consultationDeptCodeList, List<String> referenceDeptCodeList) {
		Shell shell = Display.getCurrent().getActiveShell();

		HashMap<String, Object> dataMap = new HashMap<String, Object>();

		List<TCComponentGroup> consultationDeptList = new ArrayList<TCComponentGroup>();
		List<TCComponentGroup> referenceDeptList = new ArrayList<TCComponentGroup>();

		StringBuilder message_1 = new StringBuilder();
		StringBuilder message_2 = new StringBuilder();

		try {
			if (consultationDeptCodeList != null) {
				for (int i = 0; i < consultationDeptCodeList.size(); i++) {
					String vNetTeamCode = consultationDeptCodeList.get(i);
					String vNetTeamName = getVNetTeamName(vNetTeamCode);
					
					// [20160328][ymjang] 조직변경으로 인하여 현재 존재하지 않는 팀코드인 경우, Skip 
					if (vNetTeamName == null) 
						continue;
					
					TCComponentGroup group = getGroupInTeamcenter(vNetTeamCode);
					if (group == null) {
						if (message_1.toString().isEmpty()) {
							message_1.append(registry.getString("ConsultationDept.NAME") + " : ");
						}
						message_1.append("'" + vNetTeamName+"("+vNetTeamCode+")" + "'" + ", ");
					} else {
						TCComponentGroupMember groupMember = getTeamLeader(group.toDisplayString());
						if (groupMember == null) {
							if (message_1.toString().isEmpty()) {
								message_1.append(registry.getString("ConsultationDept.NAME") + " : ");
							}
							message_1.append("'" + vNetTeamName+"("+vNetTeamCode+")" + "'" + ", ");
						} else {
							consultationDeptList.add(group);
						}
					}
				}

				if (!message_1.toString().isEmpty()) {
					message_1 = message_1.delete(message_1.lastIndexOf(","), message_1.length());
				}
			}

			if (referenceDeptCodeList != null) {
				for (int i = 0; i < referenceDeptCodeList.size(); i++) {
					String vNetTeamCode = referenceDeptCodeList.get(i);
					String vNetTeamName = getVNetTeamName(vNetTeamCode);

					// [20160328][ymjang] 조직변경으로 인하여 현재 존재하지 않는 팀코드인 경우, Skip 
					if (vNetTeamName == null) 
						continue;
					
					TCComponentGroup group = getGroupInTeamcenter(vNetTeamCode);
					if (group == null) {
						if (message_2.toString().isEmpty()) {
							message_2.append(registry.getString("ReferenceDept.NAME") + " : ");
						}
						message_2.append("'" + vNetTeamName+"("+vNetTeamCode+")" + "'" + ", ");
					} else {
						TCComponentGroupMember groupMember = getTeamLeader(group.toDisplayString());
						if (groupMember == null) {
							if (message_2.toString().isEmpty()) {
								message_2.append(registry.getString("ReferenceDept.NAME") + " : ");
							}
							message_2.append("'" + vNetTeamName+"("+vNetTeamCode+")" + "'" + ", ");
						} else {
							referenceDeptList.add(group);
						}
					}
				}

				if (!message_2.toString().isEmpty()) {
					message_2 = message_2.delete(message_2.lastIndexOf(","), message_2.length());
				}
			}

			String message = message_1.toString();
			if (!message_2.toString().isEmpty()) {
				message = message_1.toString() + "\n" + message_2.toString();
			}

			dataMap.put("message", message);
			dataMap.put("consultationDeptList", consultationDeptList);
			dataMap.put("referenceDeptList", referenceDeptList);
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(shell, "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		return dataMap;
	}

	public static String isExistGroupInTeamcenter(TCComponentItemRevision itemRevision) {
		Shell shell = Display.getCurrent().getActiveShell();

		StringBuilder message_1 = new StringBuilder();
		StringBuilder message_2 = new StringBuilder();

		try {
			//PropertyConstant.ATTR_NAME_CONSULTATIONDEPT
			String[] consultationDeptCodes = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_CONSULTATIONDEPTCODE).getStringArrayValue();
			for (int i = 0; i < consultationDeptCodes.length; i++) {
				String vNetTeamCode = consultationDeptCodes[i];
				String vNetTeamName = getVNetTeamName(vNetTeamCode);

				TCComponentGroup group = getGroupInTeamcenter(consultationDeptCodes[i]);
				if (group == null) {
					if (message_1.toString().isEmpty()) {
						message_1.append(registry.getString("ConsultationDept.NAME") + " : ");
					}
					message_1.append("'" + vNetTeamName + "'" + ", ");
				} else {
					TCComponentGroupMember groupMember = getTeamLeader(group.toDisplayString());
					if (groupMember == null) {
						if (message_1.toString().isEmpty()) {
							message_1.append(registry.getString("ConsultationDept.NAME") + " : ");
						}
						message_1.append("'" + vNetTeamName + "'" + ", ");
					}
				}
			}

			if (!message_1.toString().isEmpty()) {
				message_1 = message_1.delete(message_1.lastIndexOf(","), message_1.length());
			}

			String[] referenceDeptCodes = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_REFERENCEDEPTCODE).getStringArrayValue();
			for (int i = 0; i < referenceDeptCodes.length; i++) {
				String vNetTeamCode = referenceDeptCodes[i];
				String vNetTeamName = getVNetTeamName(vNetTeamCode);

				TCComponentGroup group = getGroupInTeamcenter(referenceDeptCodes[i]);
				if (group == null) {
					if (message_2.toString().isEmpty()) {
						message_2.append(registry.getString("ReferenceDept.NAME") + " : ");
					}
					message_2.append("'" + vNetTeamName + "'" + ", ");
				} else {
					TCComponentGroupMember groupMember = getTeamLeader(group.toDisplayString());
					if (groupMember == null) {
						if (message_2.toString().isEmpty()) {
							message_2.append(registry.getString("ReferenceDept.NAME") + " : ");
						}
						message_2.append("'" + vNetTeamName + "'" + ", ");
					}
				}
			}

			if (!message_2.toString().isEmpty()) {
				message_2 = message_2.delete(message_2.lastIndexOf(","), message_2.length());
			}
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(shell, "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		String message = message_1.toString();
		if (!message_2.toString().isEmpty()) {
			message = message_1.toString() + "\n" + message_2.toString();
		}

		return message;
	}

	public static TCComponentGroupMember getGroupMember(String userId) {
		Shell shell = Display.getCurrent().getActiveShell();

		TCComponentGroupMember groupMember = null;

		try {
			TCComponentUser user = getUser(userId);
			groupMember = getGroupMember(user);
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(shell, "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}

		return groupMember;
	}

	public static TCComponentGroupMember getGroupMember(TCComponentUser user) throws Exception {
		TCComponentGroupMember groupMember = null;

		TCComponentGroup defaultGroup = (TCComponentGroup) user.getReferenceProperty("default_group");
		TCComponentGroupMember[] groupMembers = user.getGroupMembers();
		for (TCComponentGroupMember tempGroupMember : groupMembers) {
			if (defaultGroup.equals(tempGroupMember.getGroup())) {
				groupMember = tempGroupMember;
			}
		}

		return groupMember;
	}

	public static TCComponentGroupMember[] getGroupMembers(String groupName) throws Exception {
		TCComponentGroupMember[] groupMembers = null;
		//[20180410][csh] 활성화된 사용자만 조회되도록 수정
		String queryName = SavedQueryConstant.SEARCHGROUPMEMBER;
		String[] entryNames = new String[] { "Group Name","Status" };
		String[] entryValues = new String[] { groupName, "FALSE" };

		List<TCComponentGroupMember> groupMemberList = new ArrayList<TCComponentGroupMember>();
		TCComponent[] components = SDVQueryUtils.executeSavedQuery(queryName, entryNames, entryValues);
		for (TCComponent component : components) {
			TCComponentGroupMember tempGroupMember = (TCComponentGroupMember) component;
			TCComponentGroup tempGroup = tempGroupMember.getGroup();
			TCComponentUser tempUser = tempGroupMember.getUser();
			if (tempUser.getProperty("status").equals("0")) {
				TCComponentGroup defaultGroup = (TCComponentGroup) tempUser.getReferenceProperty("default_group");
				if (tempGroup.equals(defaultGroup)) {
					groupMemberList.add(tempGroupMember);
				}
			}
		}

		groupMembers = groupMemberList.toArray(new TCComponentGroupMember[groupMemberList.size()]);
		for (int i = groupMembers.length - 2; i > 0; i--) {
			for (int j = 0; j <= i; j++) {
				String value1 = groupMembers[j].getUser().getProperty(PropertyConstant.ATTR_NAME_USERNAME);
				String value2 = groupMembers[j + 1].getUser().getProperty(PropertyConstant.ATTR_NAME_USERNAME);
				if (value1.compareTo(value2) > 0) {
					TCComponentGroupMember temp = groupMembers[j];
					groupMembers[j] = groupMembers[j + 1];
					groupMembers[j + 1] = temp;
				}
			}
		}

		return groupMembers;
	}
	
	public static TCComponentGroupMember getTeamLeader(String groupName) throws Exception {
		TCComponentGroupMember groupMember = null;

		String queryName = SavedQueryConstant.SEARCHGROUPMEMBER;
		// [DCS 현업 사용문제 신고내역] [20150708][ymjang] InActive 사용자만 조회되도록 쿼리 수정함.
		String[] entryNames = new String[] { "Group Name", "Role Name"};
		String[] entryValues = new String[] { groupName, "TEAM_LEADER"};
		TCComponent[] components = SDVQueryUtils.executeSavedQuery(queryName, entryNames, entryValues);
		for (TCComponent component : components) {
			component.refresh();
			TCComponentGroupMember tempGroupMember = (TCComponentGroupMember) component;
			TCComponentUser tempUser = tempGroupMember.getUser();
			TCComponentGroup defaultGroup = (TCComponentGroup) tempUser.getReferenceProperty("default_group");
			if (components.length > 1) {
				// [20151006][ymjang] 사용자 비활성화 여부와 그룹 멤버의 비활성화 여부도 함께 체크함.
			    if (defaultGroup.toDisplayString().equals(groupName) && tempUser.getProperty("status").equals("0") && tempGroupMember.getProperty("status").equals("False")) {
			    //if (defaultGroup.toDisplayString().equals(groupName) && tempUser.getProperty("status").equals("0")) {
                    groupMember = tempGroupMember;
                }
            }else{
                if (defaultGroup.toDisplayString().equals(groupName)) {
                    groupMember = tempGroupMember;
                }
            }
		}
		return groupMember;
	}

	public static File getTeamplateFile(String preferenceName, String exportPath) throws Exception {
		File file = null;

		TCPreferenceService prefService = session.getPreferenceService();
		String itemId = prefService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.OVERLAY_LOCATION);
		TCComponentItem item = getItem(itemId);
		if (item != null) {
			item.refresh();

			TCComponentItemRevision[] releasedItemRevisions = item.getReleasedItemRevisions();
			if (releasedItemRevisions.length > 0) {
				TCComponentItemRevision revision = item.getReleasedItemRevisions()[0];
				TCComponentDataset dataset = (TCComponentDataset) revision.getRelatedComponent("TC_Attaches");
				TCComponentTcFile[] tcFiles = dataset.getTcFiles();
				TCComponentTcFile tcFile = tcFiles[0];

				if (exportPath == null) {
					file = tcFile.getFmsFile();
					if (file.exists()) {
						file.delete();
					}

					file = tcFile.getFmsFile();
					file.setWritable(true);
				} else {
					file = new File(exportPath);
					if (file.exists()) {
						if (file.delete()) {
							file = new File(exportPath);
						}
					}

					if (tcFile.getFmsFile().renameTo(file)) {
						file.setWritable(true);
					}
				}
			}
		}

		return file;
	}
	
	/**
	 * [NONSR][20160701] taeku.jeong
	 * 결재 요청 단계에서 협의 부서 코드랑 협의 부서 Group의 수가 일치하는지 확인 하는 Function 
	 * @param itemRevision
	 * @return
	 * @throws Exception
	 */
	public static boolean isSameCountDeptAndDeptCode(TCComponentItemRevision itemRevision) throws Exception {
		String[] consultationDeptCodes = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_CONSULTATIONDEPTCODE).getStringArrayValue();
		int codeCount = 0;
		Vector<String> codeV = new Vector<String>();
		for (int i = 0;consultationDeptCodes!=null && i < consultationDeptCodes.length; i++) {
			String tempStr  = consultationDeptCodes[i];
			if(tempStr!=null && (tempStr.trim().length()>0)){
				if(codeV.contains(tempStr.trim().toUpperCase())==false){
					codeV.add(tempStr.trim().toUpperCase());
				}
			}
		}
		if(codeV!=null && codeV.size()>0){
			codeCount = codeV.size();
			codeV.clear();
		}
		
		int componentCount = 0;
		Vector<TCComponent> componentV = new Vector<TCComponent>();
		TCProperty consultationDeptTCProperty = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_CONSULTATIONDEPT);
		if(consultationDeptTCProperty!=null){
			TCComponent[] components = consultationDeptTCProperty.getReferenceValueArray();
			for (int i = 0;components!=null && i < components.length; i++) {
				TCComponent tempComponent = components[i];
				if(tempComponent!=null){
					if(componentV.contains(tempComponent)==false){
						componentV.add(tempComponent);
					}
				}
			}
		}
		if(componentV!=null && componentV.size()>0){
			componentCount = componentV.size();
			componentV.clear();
		}
		
		boolean isSameCount = true;
		if(componentCount!=codeCount){
			isSameCount = false;
		}
		
		return isSameCount;
	}

	public static void setDeptFromDeptCode(TCComponentItemRevision itemRevision) throws Exception {
		String[] consultationDeptCodes = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_CONSULTATIONDEPTCODE).getStringArrayValue();
		TCComponent[] consultationDeptComponent = new TCComponent[consultationDeptCodes.length];
		for (int i = 0; i < consultationDeptCodes.length; i++) {
			TCComponentGroup group = getGroupInTeamcenter(consultationDeptCodes[i]);
			consultationDeptComponent[i] = group;
		}
		TCProperty consultationDeptTCProperty = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_CONSULTATIONDEPT);
		consultationDeptTCProperty.setReferenceValueArray(consultationDeptComponent);

		String[] referenceDeptCodes = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_REFERENCEDEPTCODE).getStringArrayValue();
		TCComponent[] referenceDeptComponent = new TCComponent[referenceDeptCodes.length];
		for (int i = 0; i < referenceDeptCodes.length; i++) {
			TCComponentGroup group = getGroupInTeamcenter(referenceDeptCodes[i]);
			referenceDeptComponent[i] = group;
		}
		TCProperty referenceDeptTCProperty = itemRevision.getTCProperty(PropertyConstant.ATTR_NAME_REFERENCEDEPT);
		referenceDeptTCProperty.setReferenceValueArray(referenceDeptComponent);
	}

	public static boolean isActiveUser(TCComponentUser user) {
		boolean isActiveUser = false;

		try {
			if (user.getProperty("status").equals("0")) {
				isActiveUser = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isActiveUser;
	}

	public static boolean isActiveUser(String userId) {
		boolean isActiveUser = false;

		try {
			TCComponentUser user = null;

			TCComponentUserType userType = (TCComponentUserType) session.getTypeComponent("User");
			user = userType.find(userId);

			if (user != null) {
				if (user.getProperty("status").equals("0")) {
					isActiveUser = true;
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
		}

		return isActiveUser;
	}

	public static boolean isReleased(TCComponentItemRevision itemRevision) {
		boolean isReleased = false;

		try {
			if (!itemRevision.getProperty(PropertyConstant.ATTR_NAME_RELEASESTATUSLIST).equals("")) {
				isReleased = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isReleased;
	}

	public static boolean isInProcess(TCComponentItemRevision itemRevision) {
		boolean isInProcess = false;

		try {
			if (!itemRevision.getProperty(PropertyConstant.ATTR_NAME_PROCESSSTAGELIST).equals("")) {
				isInProcess = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return isInProcess;
	}

	public static void fileDownload(TCComponentDataset dataset) {
		Shell shell = Display.getCurrent().getActiveShell();

		try {
			TCComponentTcFile[] tcFile = dataset.getTcFiles();
			if (tcFile.length > 0) {
				String fileName = tcFile[0].getProperty(PropertyConstant.ATTR_NAME_ORIGINALFILENAME);
				String exportPath = DCSUIUtil.openFileDialog(shell, fileName);
				if (exportPath != null) {
					int lastIndexOf = exportPath.lastIndexOf("\\");
					fileName = exportPath.substring(lastIndexOf + 1, exportPath.length());
					File file = tcFile[0].getFile(exportPath.substring(0, lastIndexOf), fileName);
					if (file.exists()) {
						MessageDialog.openInformation(shell, "Information", registry.getString("DownloadSucceed.MESSAGE"));
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(shell, "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}
	}

	public static HashMap<String, Object> getVNetUserInfo(String userId) throws Exception {
		HashMap<String, Object> dataMap = null;

		DataSet dataSet = new DataSet();
		dataSet.put("user_id", userId);

		List<HashMap<String, Object>> resultList = selectVNetUserList(dataSet);
		if (resultList != null && resultList.size() == 1) {
			dataMap = resultList.get(0);
		}

		return dataMap;
	}

	public static String getVNetTeamName(TCComponentGroup group) throws Exception {
		String vNetTeamCode = group.getProperty("description");
		String vNetTeamName = DCSCommonUtil.getVNetTeamName(vNetTeamCode);
		if (vNetTeamName == null) {
			vNetTeamName = group.toDisplayString();
		}

		return vNetTeamName;
	}

	public static String getVNetTeamName(String teamCode) throws Exception {
		String vNetTeamName = null;

		HashMap<String, Object> dataMap = getVNetTeamInfo(teamCode);
		if (dataMap != null) {
			vNetTeamName = (String) dataMap.get("TEAM_NAME");
		}

		return vNetTeamName;
	}

	public static HashMap<String, Object> getVNetTeamInfo(String teamCode) throws Exception {
		HashMap<String, Object> dataMap = null;

		DataSet dataSet = new DataSet();
		dataSet.put("team_code", teamCode);
		
		List<HashMap<String, Object>> resultList = selectVNetTeamList(dataSet);
		if (resultList != null && resultList.size() == 1) {
			dataMap = resultList.get(0);
		}

		return dataMap;
	}

	public static List<HashMap<String, Object>> selectVNetUserList(DataSet dataSet) throws Exception {
		List<HashMap<String, Object>> resultList = dcsVisionNetQueryService.selectVNetUserList(dataSet);

		return resultList;
	}

	public static List<HashMap<String, Object>> selectVNetTeamList(DataSet dataSet) throws Exception {
		List<HashMap<String, Object>> resultList = dcsVisionNetQueryService.selectVNetTeamList(dataSet);

		return resultList;
	}

	/* 
	 * [20151223][ymjang] 조직개편으로 팀코드 변경. 협의 완료된 경우는 변경전 코드로 팀정보를 가져올 수 있도록 개선함.
	 * 아래 총 4개 Method
	 * getVNetTeamHistName, getVNetTeamHistName, getVNetTeamHistInfo, selectVNetTeamHistList, searchVNetTeamHistList
	 */	
	public static String getVNetTeamHistName(TCComponentGroup group) throws Exception {
		String vNetTeamCode = group.getProperty("description");
		String vNetTeamName = DCSCommonUtil.getVNetTeamHistName(vNetTeamCode);
		if (vNetTeamName == null) {
			vNetTeamName = group.toDisplayString();
		}

		return vNetTeamName;
	}

	public static String getVNetTeamHistName(String teamCode) throws Exception {
		String vNetTeamName = null;

		HashMap<String, Object> dataMap = getVNetTeamHistInfo(teamCode);
		if (dataMap != null) {
			vNetTeamName = (String) dataMap.get("TEAM_NAME");
		}

		return vNetTeamName;
	}
	
	public static HashMap<String, Object> getVNetTeamHistInfo(String teamCode) throws Exception {
		HashMap<String, Object> dataMap = null;

		DataSet dataSet = new DataSet();
		dataSet.put("team_code", teamCode);
		
		List<HashMap<String, Object>> resultList = selectVNetTeamHistList(dataSet);
		if (resultList != null && resultList.size() == 1) {
			dataMap = resultList.get(0);
		}

		return dataMap;
	}
	
	public static List<HashMap<String, Object>> selectVNetTeamHistList(DataSet dataSet) throws Exception {
		List<HashMap<String, Object>> resultList = dcsVisionNetQueryService.selectVNetTeamHistList(dataSet);

		return resultList;
	}

	/**
	 * [20160308][ymjang] 조직 변경 및 결재 상세 정보 속도 개선을 위한 결재 History 생성 기능 추가
	 * @param itemRevision
	 * @param owningGroup
	 * @param owningUser
	 * @param subTeamLeader
	 * @param teamLeader
	 * @param workflowType
	 * @param rejectComment
	 * @param reviewResult
	 * @return
	 * @throws Exception
	 */
	public static Boolean insertDCSWorkflowHistory(TCComponentItemRevision itemRevision, TCComponentGroupMember reportgUser, 
			                                       TCComponentGroupMember reviewer, TCComponentGroupMember subTeamLeader, TCComponentGroupMember teamLeader, 
			                                       TCComponentProcess process, TCComponentTask targetTask,
			                                       String comment, String rejectComment, String reviewResult, String applyDate) throws Exception {
		
		//[20160504][ymjang] 시간 24시간 타입으로 변경함.
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		Boolean isOk = false;
		try {
			
			DataSet dataSet = new DataSet();
			
			String dcsNO = itemRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
			String dcsRev = itemRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID);
			dataSet.put("DCS_NO", dcsNO);
			dataSet.put("DCS_REV", dcsRev);
			
			// 결재유형
			String workflowType = null;
			String processName = process.getName();
			if (processName.contains(registry.getString("OwningGroup.NAME", "작성부서"))) {
				workflowType = "1";
			} else if (processName.contains(registry.getString("ConsultationDept.NAME", "협의부서"))) {
				workflowType = "2";
			} else if (processName.contains(registry.getString("ReferenceDept.NAME", "참조부서"))) {
				workflowType = "4";
			}
			if (workflowType == null) {
				throw new Exception("Not Found Workflow Type ==> " + dcsNO + " " + dcsRev);
			}
			dataSet.put("WORKFLOW_TYPE", workflowType);
			
			// 결재 팀
			/* eg.)
			   DCS [DCA165T11_00] [TRIM DESIGN2.SYMC - 작성부서] 결재요청.
			   DCS [DCA165T11_00] [ADVANCE QUALITY.SYMC_MFG - 협의부서] 검토요청.
			   DCS [DCA165T11_00] [ELECTRIC SYSTEM DESIGN.SYMC - 참조부서] 검토요청.
			 */	
			// Process Name 으로 부터 팀명을 가져온다.
			String groupName = processName.substring(processName.lastIndexOf("[")+1, processName.lastIndexOf(" - "));
			
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentGroupType groupType = (TCComponentGroupType) session.getTypeComponent("Group");
			TCComponentGroup workflowOwningGroup = groupType.find(groupName);
			if (workflowOwningGroup == null) {
				throw new Exception("Not Found Group Name ==> " + dcsNO + " " + dcsRev + ":" + groupName);
			}
			String vNetTeamCode = workflowOwningGroup.getProperty("description");
			String vNetTeamName = getVNetTeamName(vNetTeamCode);
			dataSet.put("TEAM_CODE", vNetTeamCode);
			dataSet.put("TEAM_NAME", vNetTeamName);
			if (vNetTeamCode == null || vNetTeamName == null) {
				throw new Exception("Not Found TEAM_CODE ==> " + dcsNO + " " + dcsRev + ":" + groupName);
			}
			
			if (workflowType.equals("1")) {
				// 최초 결재 상신시
				// 작성자
				if (reportgUser != null) {
					dataSet.put("MAKE_USER_NAME", reportgUser.getUser().getProperty(PropertyConstant.ATTR_NAME_USERNAME));
					dataSet.put("MAKE_USER", reportgUser.getUserId());
					dataSet.put("MAKE_DATE", format.format(new Date()));
					dataSet.put("MAKE_COMMENT", "");
				}
			}
						
			// Reviewer
			if (reviewer != null) {
				dataSet.put("MAKE_USER_NAME", reviewer.getUser().getProperty(PropertyConstant.ATTR_NAME_USERNAME));
				dataSet.put("MAKE_USER", reviewer.getUserId());
				dataSet.put("MAKE_COMMENT", comment);
			}
			
			// Sub TeamLeader
			if (subTeamLeader != null) {
				dataSet.put("FIRST_CREATE_USER_NAME", subTeamLeader.getUser().getProperty(PropertyConstant.ATTR_NAME_USERNAME));
				dataSet.put("FIRST_CREATE_USER", subTeamLeader.getUserId());				
				dataSet.put("FIRST_COMMENT", comment);
			}
			
			// TeamLeader
			if (teamLeader != null) {
				dataSet.put("SECOND_CREATE_USER_NAME", teamLeader.getUser().getProperty(PropertyConstant.ATTR_NAME_USERNAME));
				dataSet.put("SECOND_CREATE_USER", teamLeader.getUserId());
				dataSet.put("SECOND_COMMENT", comment);
			}
			
			// 결재를 수행했을 때만 일자를 입력한다.
			if (targetTask != null && !DCSStringUtil.isEmpty(reviewResult)) {
				if (targetTask.getName().equals("Reviewer")) {
					dataSet.put("MAKE_DATE", format.format(new Date()));
				}
				if (targetTask.getName().equals("Sub Team Leader")) {
					dataSet.put("FIRST_CREATE_DATE", format.format(new Date()));
				}
				if (targetTask.getName().equals("Team Leader")) {
					dataSet.put("SECOND_CREATE_DATE", format.format(new Date()));
				}
			}
			
			dataSet.put("REJECT_COMMENT", rejectComment);
			dataSet.put("APPLY_DATE", applyDate);
			
			// 최종 결재시에만
			if (!DCSStringUtil.isEmpty(applyDate)) {
				dataSet.put("REVIEW_RESULT", reviewResult);
			} else {
				dataSet.put("REVIEW_RESULT", "");
			}
			
			//TCComponent owningUser = itemRevision.getReferenceProperty(PropertyConstant.ATTR_NAME_OWNINGUSER);
			dataSet.put("CREATE_USER", session.getUser().getUserId());
			dataSet.put("CREATE_DATE", format.format(new Date()));
			
			// Reject 후, Reviewer 재상신시 기 결재내역 초기화
			// DCS 삭제 후, 동일한 DCS 결재 상신시 기 결재내역 초기화
			if (workflowType.equals("1")) {
				if (reportgUser != null || reviewer != null) {
					dataSet.put("FIRST_CREATE_DATE", "");
					dataSet.put("FIRST_COMMENT", "");
					dataSet.put("SECOND_CREATE_DATE", "");
					dataSet.put("SECOND_COMMENT", "");
					dataSet.put("REJECT_COMMENT", "");
					dataSet.put("REVIEW_RESULT", "");
					dataSet.put("APPLY_DATE", "");
				}
			}
			
			isOk = dcsHitsQueryService.saveDCSWorkflowHistory(dataSet);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			sendAdminMail("DCS_ADMIN", "결재 History 생성 오류", e.getMessage());
		} 
		
		return isOk;
	}
	
	/**
	 * [20160310] [ymjang] Admin 메일 발송
	 * @param title
	 * @param errMsg
	 * @throws Exception
	 */
	public static void sendAdminMail(String toUser, String title, String errMsg) throws Exception {
		
		ENVService envService = new ENVService();
		Map<String, String> envMap = envService.getTCWebEnv();
		String toAdminUser = envMap.get(toUser);
		
		DataSet dataSet = new DataSet();
		dataSet.put("the_sysid", "NPLM");
		dataSet.put("the_sabun", "NPLM");
		dataSet.put("the_title", title);
		dataSet.put("the_remark", errMsg);
		dataSet.put("the_tsabun", toAdminUser);
		   
		DCSQueryService queryService = new DCSQueryService();
		queryService.sendMail(dataSet);
	}
	
}
