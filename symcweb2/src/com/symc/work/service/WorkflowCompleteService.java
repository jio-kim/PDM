package com.symc.work.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import com.ssangyong.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcItemUtil;
import com.symc.common.soa.biz.TcWorkflowUtil;
import com.symc.common.soa.util.TcConstants;
import com.symc.soa.service.MECOReportService;
import com.symc.soa.service.SDVTCDataManager;
import com.teamcenter.soa.client.model.ModelObject;
import com.teamcenter.soa.client.model.strong.ItemRevision;
import com.teamcenter.soa.client.model.strong.Person;
import com.teamcenter.soa.client.model.strong.User;

/**
 * [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
 */
public class WorkflowCompleteService {

	private Session tcSession = null;
	private TcItemUtil tcItemUtil = null;
	private TcWorkflowUtil tcWorkflowUtil = null;
	private HashMap<String, Object> resultMap = null;
	public static final String INVOKER_MECO = "MECO";
	public static final String INVOKER_SWMD  = "STANDARD WORK INSTRUCTION";
	public TcLoginService tcLoginService = null;
	public SDVTCDataManager dataManager = null;

	public WorkflowCompleteService() throws Exception {
		tcLoginService = new TcLoginService();
		this.tcSession = tcLoginService.getTcSession();
		tcItemUtil = new TcItemUtil(tcSession);
		tcWorkflowUtil = new TcWorkflowUtil(tcSession);
        this.dataManager = new SDVTCDataManager(tcSession);
        try {
            this.dataManager.setByPass();
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
//	public List<HashMap<String,String>> searchMecoReadyToComplete() throws Exception {
//		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
//		List<HashMap<String,String>> arrMecoReadyToCompleteList = null;
//		HashMap<String, String> parmaMap = new HashMap<String, String>();
//		parmaMap.put("TABLE_NAME", "MECOREVISION");
//		parmaMap.put("IS_COMPLETED", "END");
//		arrMecoReadyToCompleteList = (List<HashMap<String,String>>)commonDao.selectList("com.symc.meco.searchMecoReadyToComplete", parmaMap);
//
//		return arrMecoReadyToCompleteList;
//	}

	public void startServiceForMeco(String meco_uid) throws Exception{
		System.out.println("Starting-WorkflowCompleteService.startServiceForMeco ");
//		Session session = null;

		try{

			if(!"".equals(meco_uid) && null !=meco_uid ) {
//				session = tcLoginService.getTcSession();
				tcItemUtil = new TcItemUtil(tcSession);
				tcWorkflowUtil = new TcWorkflowUtil(tcSession);
				ItemRevision[] itemRevision = null;
				HashMap<String, Object> updateMap = new HashMap<String, Object>();

				try{
					itemRevision = tcItemUtil.getItemRevisionObjects(new String[] {meco_uid});
					updateMap.put("m7_IS_COMPLETED", "Completed");
//					updateMap.put("m7_MECO_MATURITY", "Completed");

//					tcItemUtil.getProperties(itemRevision, new String[] {TcConstants.MECO_TYPED_REFERENCE});

					tcItemUtil.setAttributes(itemRevision[0], updateMap);

					setEffectivityOnMecoRevision(itemRevision, tcSession);
				}catch(Exception e) {
					e.printStackTrace();
				}
				System.out.println();
				resultMap = tcWorkflowUtil.completeProcess(new String[] {meco_uid}, null);

				if(resultMap.get(TcConstants.TC_RETURN_MESSAGE).equals(TcConstants.TC_RETURN_OK)) {


					sendRequestMailForMECO(itemRevision[0].get_item_id());

					System.out.println("Successfully completed-WorkflowCompleteService.startServiceForMeco");
				}else{

					throw new Exception(resultMap.get(TcConstants.TC_RETURN_FAIL_REASON).toString());
				}
			}else{
				throw new Exception("Error-meco_uid is null in WorkflowCompleteService.startServiceForSWMD.");
			}

		}catch(Exception e){
			throw e;
		} finally {
			if (tcSession != null)
				tcSession.logout();
		}
	}

	@SuppressWarnings("unchecked")
	public void sendRequestMailForMECO(String mecoId) throws Exception {
		File deployFile = null;
		ItemRevision mecoRevision = null;
		MECOReportService mecoService = null;
		try{

			mecoService = new MECOReportService(tcSession);
			deployFile = mecoService.getFileMECOReport(mecoId);
			String file_path ="\\\\150.1.11.105\\s4c3\\SYMC_PLM_FTP";
			File destFile = new File(file_path);
			if(destFile.isDirectory()) {
				if(destFile.canWrite()) {
					System.out.println("Can write!!");
				}else{
					System.out.println("Can't write!!");
				}
			}
			if(deployFile.isFile()) {
				deployFile.renameTo(new File(file_path+File.separator+deployFile.getName()));
			}


			mecoRevision = dataManager.getLatestItemRevision(mecoId);
			mecoRevision = (ItemRevision) dataManager.loadObjectWithProperties(mecoRevision,
					new String[] { TcConstants.PROP_DATE_RELEASED, TcConstants.PROP_ITEM_ID, TcConstants.MECO_PROJECT, TcConstants.MECO_MATURITY,
								   TcConstants.MECO_EFFECT_DATE, TcConstants.MECO_EFFECT_EVENT, TcConstants.MECO_CHANGE_REASON, TcConstants.PROP_OBJECT_DESC, TcConstants.PROP_OWNING_GROUP,
								   TcConstants.PROP_OWNING_USER
								   });
			String mecNo = mecoId;
			String products = mecoRevision.getPropertyObject(TcConstants.MECO_PROJECT).getStringValue();
			String changeDesc = mecoRevision.getPropertyObject(TcConstants.PROP_OBJECT_DESC).getStringValue();

//					mecoRevision.getPropertyObject(TcConstants.PROP_OWNING_USER).getStringValue();
            User user = (User) mecoRevision.get_owning_user();
            user = (User) dataManager.loadObjectWithProperties(user, new String[] { "person" });
            Person person = user.get_person();
            person = (Person) dataManager.loadObjectWithProperties(person, new String[] { TcConstants.PROP_DEPT_NAME ,TcConstants.PROP_USER_ID });
            String fromUser = user.get_user_id();
            String dept = person.get_PA6();

			String title = "New PLM : MECO[" + mecNo + "] Release";
			String body = "<PRE>";
			body += "Please see following contents.." + "<BR>";
			body += " -MECO NO. : " + mecNo + "<BR>";
			body += " -Project : " + products + "<BR>";
			body += " -Change Desc. : " + changeDesc + "<BR>";
			body += " -Department : " + dept + "<BR>";
			body += " -Creator  : " + fromUser + "<BR>";
            /**
             * [SR141120-029][20141230] ymjang MECO 상세 정보를 보여주기 위한 Deploy File이 연결이 안되고 있음.
             */
            body += " -Deploy File : " + "<a href='" + file_path+File.separator+deployFile.getName() +"'>" + deployFile.getName() + "</a>" + "<BR>";
            //body += " -Deploy File : " +file_path+File.separator+deployFile.getName()+ "<BR>";
			
			body += "</PRE>";

			TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
			DataSet ds = new DataSet();
			ds.put("mecoRevPuid", mecoRevision.get_item_id());
			List<HashMap<String,String>> arrMecoReadyToCompleteList = null;

			arrMecoReadyToCompleteList = (List<HashMap<String,String>>)commonDao.selectList("com.symc.meco.searchDistributor", ds);
			for(HashMap<String, String> userMap : arrMecoReadyToCompleteList) {

				ds = new DataSet();
				ds.put("the_sysid", "NPLM");

				if(fromUser == null || fromUser.equals(""))
					ds.put("the_sabun", "NPLM");
				else
					ds.put("the_sabun", fromUser);

				ds.put("the_title", title);
				ds.put("the_remark", body);
				ds.put("the_tsabun", userMap.get("PUSER_ID"));

				try{
			    // [20160606][ymjang] 메일 발송 방식 개선 (through EAI)
				commonDao.update("com.symc.meco.sendMailEai", ds);
				//commonDao.update("com.symc.meco.sendMail", ds);
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
//			dao.sendMail(fromUser, title, body, toUsers);
//			{call CALS.MAILSEND@LINK_001_VNET(#{the_sysid},#{the_sabun},#{the_title},#{the_remark},#{the_tsabun})
		}catch(Exception e){
			throw (new Exception("sendRequestMail error"));
		}
	}


	public void startServiceForSWMD(String rev_uid) throws Exception{

		try{

			if(!"".equals(rev_uid) && null !=rev_uid ) {

				resultMap = tcWorkflowUtil.completeProcess(new String[] {rev_uid}, null);

				if(resultMap.get(TcConstants.TC_RETURN_MESSAGE).equals(TcConstants.TC_RETURN_OK)) {

				}else{

					throw new Exception(resultMap.get(TcConstants.TC_RETURN_FAIL_REASON).toString());
				}

			}else{
				throw new Exception("Error-rev_uid is null in WorkflowCompleteService.startServiceForSWMD.");
			}

		}catch(Exception e){
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	public void setEffectivityOnMecoRevision(ItemRevision[] itemRevision, Session session ) throws Exception {
		tcItemUtil = new TcItemUtil(session);
		tcItemUtil.getProperties(itemRevision, new String[] {TcConstants.MECO_TYPED_REFERENCE});
		ModelObject meco_effectivity = itemRevision[0].getPropertyObject(TcConstants.MECO_TYPED_REFERENCE).getModelObjectValue();

		TcCommonDao commonDao = TcCommonDao.getTcCommonDao();
		DataSet ds = new DataSet();
		ds.put("mecoRevPuid", meco_effectivity.getUid());
		commonDao.update("com.symc.meco.updateMEcoEffectivitDate", ds);
	}
}
