package com.symc.work.job;

import java.util.HashMap;
import java.util.List;

import com.kgm.common.remote.DataSet;
import com.symc.common.dao.TcCommonDao;
import com.symc.common.exception.NotLoadedChildLineException;
import com.symc.common.job.task.ExecuteTask;
import com.symc.common.soa.biz.Session;
import com.symc.common.soa.biz.TcVariantUtil;
import com.symc.common.soa.service.TcPreferenceManagementService;
import com.symc.common.soa.service.TcServiceManager;
import com.symc.common.util.ContextUtil;
import com.symc.common.util.StringUtil;
import com.symc.work.model.ProductInfoVO;
import com.symc.work.service.PeEcIfService;
import com.symc.work.service.PeFirstDistributeIfService;
import com.symc.work.service.PeFunctionIfService;
import com.symc.work.service.TcLoginService;
import com.symc.work.service.TcPeIFService;
import com.teamcenter.services.internal.strong.structuremanagement._2011_06.VariantManagement.ModularOption;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.CompletePreference;
import com.teamcenter.services.strong.administration._2012_09.PreferenceManagement.GetPreferencesResponse;

public class TcPeIFJob extends ExecuteTask {

    private Session session;
    private TcPeIFService tcPeIFService;
    private PeFirstDistributeIfService peFirstDistributeIfService;
    private PeEcIfService peEcIfService;
    private PeFunctionIfService peFunctionIfService;

    public TcPeIFJob() {
        super(TcPeIFJob.class);
    }

    /**
     * 초도 / EC 배포 시작
     *
     * 주의 : 초도배포와 EC 배포 시작포인트를 여기 둔 이유는 Service단에 두 메소드를 같이 두개되면 두 로직이 트랜잭션
     * 처리되므로 (...Service Class에 있는 경우 : (1. 초도배포)는 성공 했으나 (2. EC 배포)가 실패할 경우 성공한
     * 초도 배포가 전부 롤백되는 상황발생) 배포별 메소드 콜을 여기 JOB Class에서 call 하는 방식으로 변경 함.
     */
    @Override
    public String startTask() throws Exception {
//        long startTime = System.currentTimeMillis();
        StringBuffer log = new StringBuffer();
        TcLoginService tcLoginService = new TcLoginService();
        try {
            // 1. TC Session 생성
            session = tcLoginService.getTcSession();
            tcPeIFService = (TcPeIFService) ContextUtil.getBean("tcPeIFService");
            // 초도 배포서비스
            peFirstDistributeIfService = (PeFirstDistributeIfService) ContextUtil.getBean("peFirstDistributeIfService");
            // EC 등록 서비스
            peEcIfService = (PeEcIfService) ContextUtil.getBean("peEcIfService");
            // FUNCTION 등록 서비스
            peFunctionIfService = (PeFunctionIfService) ContextUtil.getBean("peFunctionIfService");
            // 2. Option Master정보를 TC에서 가져온다.
            TcVariantUtil tcVariantUtil = new TcVariantUtil(session);
            ModularOption[] modularOptions = tcVariantUtil.getOptionMaster();
            // 3. 초도 배포
            log.append(this.firstDistributeIf(session, modularOptions));
            // 4. EC 배포
            log.append(this.ecIf(session, modularOptions));
            // 4. FUNCTION 배포
            log.append(this.functionIf(session, modularOptions));
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.logout();
            }
//            // 배치시간을 IF_PE_BATCH_TIME 테이블에 등록
//            if(tcPeIFService != null) {
//                tcPeIFService.createBatchTime(startTime, System.currentTimeMillis());
//            }
        }
        return log.toString();
    }

    /**
     * 초도배포
     *
     * @method firstDistributeIf
     * @date 2013. 7. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String firstDistributeIf(Session session, ModularOption[] modularOptions) throws Exception {
        StringBuffer log = new StringBuffer();
        String[] status = { "CREATION" };
        String transType = "P";
        // 1. 초도배포 조회
        List<ProductInfoVO> peProductList = tcPeIFService.getPeProductList(status, transType);
        for (int i = 0; peProductList != null && i < peProductList.size(); i++) {
            ProductInfoVO productInfoVO = null;
            // << 주의 >>
            // 이부분은
            // tcPeIFService.createProductExpandItem
            // tcPeIFService.changePeProjectStatus
            // 두 메소드가 DB 부분이 트랜잭션이 되지않으므로 각별한 주의가 필요함.
            // tcPeIFService.createProductExpandItem 메소드내에서는 트랜잭션 처리가 되나 여기
            // 부분에서는 에러가날 경우에도 상태를 DB 처리해야하므로 예외 처리를 함.
            StringBuffer productLog = null;
            try {
                productInfoVO = peProductList.get(i);
                // 상태변경(PROCESSING) 및 진행중 로그 저장
                tcPeIFService.changeStatusProcessing(productInfoVO, log);
                // PRODUCT 정보를 가지고 하위 FULL 전개 [PRODUCT 기준 초도배포 등록]
                productLog = peFirstDistributeIfService.createProductExpandItem(session, modularOptions, productInfoVO);
                // 상태변경(WAITING) 및 성공 로그 저장
                tcPeIFService.changeStatusWaiting(productInfoVO, log);
                // PRODUCT I/F 정보 등록 후 EAI에 통보하기 위한 테이블(IF_PE_EAI_JOB) 정보 업데이트
                tcPeIFService.createEaiJob(productInfoVO);
            } catch (Exception e) {
                e.printStackTrace();
                // Return Log 등록
                try {
                    tcPeIFService.changeStatusErrorAndStackTraceString(productInfoVO, e, log);
                } catch(Exception ex) {
                    tcPeIFService.appandLog(StringUtil.getStackTraceString(ex), log);
                }
                // throw e;
            } finally {
                // BaseException Log 처리 - FTP 에러 처리 etc,,
                if(productLog != null) {
                    tcPeIFService.appandLog(productLog.toString(), log);
                }
            }
        }
        return log.toString();
    }

    /**
     * 2. EC 배포 시작
     *
     * @method ecIf
     * @date 2013. 7. 22.
     * @param
     * @return String
     * @exception
     * @throws
     * @see
     */
    public String ecIf(Session session, ModularOption[] modularOptions) throws Exception {
        StringBuffer log = new StringBuffer();
        String[] status = { "CREATION" };
        String transType = "E";
        // 1. EC배포 조회
        List<ProductInfoVO> peProductList = tcPeIFService.getPeProductList(status, transType);
        for (int i = 0; peProductList != null && i < peProductList.size(); i++) {
            ProductInfoVO productInfoVO = null;
            // << 주의 >>
            // 에러가날 경우에도 상태를 DB 처리해야하므로 예외 처리를 함.
            StringBuffer productLog = null;

            productInfoVO = peProductList.get(i);
            try {

                boolean isFirstDistribute = tcPeIFService.checkFirstDistribute(productInfoVO.getProductId());
                // 초도 배포 상태가 아니면 File Log기록 후 Skip
                if(!isFirstDistribute) {
                    tcPeIFService.ecoSkipLog(productInfoVO, log);
                    continue;
                }
                // ERROR 가 존재하면 Skip
                boolean isError = tcPeIFService.checkProductError(productInfoVO.getProductId());
                if(isError) {
                    tcPeIFService.errorSkipLog(productInfoVO, log);
                    continue;
                }
                // 상태변경(PROCESSING) 및 진행중 로그 저장
                tcPeIFService.changeStatusProcessing(productInfoVO, log);
                // [PRODUCT 기준 EC 등록]
                productLog = peEcIfService.createProductEc(session, modularOptions, productInfoVO);
                // 상태변경(WAITING) 및 성공 로그 저장
                tcPeIFService.changeStatusWaiting(productInfoVO, log);
                // PRODUCT I/F 정보 등록 후 EAI에 통보하기 위한 테이블(IF_PE_EAI_JOB) 정보 업데이트
                tcPeIFService.createEaiJob(productInfoVO);

             // 에러 발생 시 메일을 발송-테스트
//                NotLoadedChildLineException e = new NotLoadedChildLineException("TEST Exception");
//                e.addInfo(NotLoadedChildLineException.ITEM_ID, "TEST00001");
//        		e.addInfo(NotLoadedChildLineException.ITEM_REVISION_ID, "000");
//                sendMail(session, e, productInfoVO);
            } catch (Exception e) {
                e.printStackTrace();
                // Return Log 등록
                try {
                    tcPeIFService.changeStatusErrorAndStackTraceString(productInfoVO, e, log);
                } catch(Exception ex) {
                    tcPeIFService.appandLog(StringUtil.getStackTraceString(ex), log);
                }

                // 에러 발생 시 메일을 발송
                sendMail(session, e, productInfoVO);

                // throw e;
            } finally {
                // BaseException Log 처리 - FTP 에러 처리 etc,,
                if(productLog != null) {
                    tcPeIFService.appandLog(productLog.toString(), log);
                }
            }
        }
        return log.toString();
    }

    /**
     * 3. FUNCTION 배포 시작
     * 
     * @method functionIf 
     * @date 2014. 2. 10.
     * @param
     * @return Object
     * @exception
     * @throws
     * @see
     */
    private String functionIf(Session session2, ModularOption[] modularOptions) throws Exception {
        StringBuffer log = new StringBuffer();
        String[] status = { "CREATION" };
        String transType = "F";
        // 1. FUNCTION 배포 조회
        List<ProductInfoVO> peProductList = tcPeIFService.getPeProductList(status, transType);
        for (int i = 0; peProductList != null && i < peProductList.size(); i++) {
            ProductInfoVO productInfoVO = null;
            // << 주의 >>
            // 에러가날 경우에도 상태를 DB 처리해야하므로 예외 처리를 함.
            StringBuffer productLog = null;
            productInfoVO = peProductList.get(i);
            try {
                boolean isFirstDistribute = tcPeIFService.checkFirstDistribute(productInfoVO.getProductId());
                // 초도 배포 상태가 아니면 File Log기록 후 Skip
                if(!isFirstDistribute) {
                    tcPeIFService.ecoSkipLog(productInfoVO, log);
                    continue;
                }
                // ERROR 가 존재하면 Skip
                boolean isError = tcPeIFService.checkProductError(productInfoVO.getProductId());
                if(isError) {
                    tcPeIFService.errorSkipLog(productInfoVO, log);
                    continue;
                }
                // 상태변경(PROCESSING) 및 진행중 로그 저장
                tcPeIFService.changeStatusProcessing(productInfoVO, log);
                // [PRODUCT 기준 FUNCTION 등록]
                productLog = peFunctionIfService.createFunctionExpandItem(session, modularOptions, productInfoVO);
                // 상태변경(WAITING) 및 성공 로그 저장
                tcPeIFService.changeStatusWaiting(productInfoVO, log);
                // PRODUCT I/F 정보 등록 후 EAI에 통보하기 위한 테이블(IF_PE_EAI_JOB) 정보 업데이트
                tcPeIFService.createEaiJob(productInfoVO);

             // 에러 발생 시 메일을 발송-테스트
//                NotLoadedChildLineException e = new NotLoadedChildLineException("TEST Exception");
//                e.addInfo(NotLoadedChildLineException.ITEM_ID, "TEST00001");
//              e.addInfo(NotLoadedChildLineException.ITEM_REVISION_ID, "000");
//                sendMail(session, e, productInfoVO);
            } catch (Exception e) {
                e.printStackTrace();
                // Return Log 등록
                try {
                    tcPeIFService.changeStatusErrorAndStackTraceString(productInfoVO, e, log);
                } catch(Exception ex) {
                    tcPeIFService.appandLog(StringUtil.getStackTraceString(ex), log);
                }

                // 에러 발생 시 메일을 발송
                sendMail(session, e, productInfoVO);

                // throw e;
            } finally {
                // BaseException Log 처리 - FTP 에러 처리 etc,,
                if(productLog != null) {
                    tcPeIFService.appandLog(productLog.toString(), log);
                }
            }
        }
        return log.toString();
    }
    
    @SuppressWarnings({ "unchecked" })
	private void sendMail(Session session,Exception e, ProductInfoVO productInfoVO){

    	TcServiceManager manager = new TcServiceManager(session);
//    	TcSessionServiceManager sessionServiceManager = null;
//    	MultiPreferencesResponse response = null;
//		try {
//			sessionServiceManager = manager.getSessionService();
//			ScopedPreferenceNames[] prefNames = new ScopedPreferenceNames[1];
//			prefNames[0] = new ScopedPreferenceNames();
//			prefNames[0].names = new String[]{"PE_IF_ADMIN"};
//			prefNames[0].scope = "site";
//			response = sessionServiceManager.getPreferences(prefNames);
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}
//
//		String[] list = response.preferences[0].values;

    	TcPreferenceManagementService prefManager = null;
    	CompletePreference retPrefValue = null;
		try {
			prefManager = manager.getPreferenceService();
			GetPreferencesResponse ret = prefManager.getPreferences(new String[]{"PE_IF_ADMIN"}, true);
			if (ret != null && ret.data.sizeOfPartialErrors() == 0)
			{
				for (CompletePreference pref : ret.response)
					if (pref.definition.protectionScope.toUpperCase().equals("site".toUpperCase()))
						retPrefValue = pref;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		String[] list = retPrefValue.values.values;
		if( list != null && list.length > 0){

    		String toUsers = "";
    		String body = "";
    		for( int i = 0; i < list.length; i++ ){
    			String toUser = list[i];
    			if( i > 0){
					toUsers += "," + toUser;
				}else{
					toUsers = toUser;
				}
    		}
    		// NotLoadedChildLineException 일 경우, ECO No(초가 아닌 경우)를 제공.
            if( e instanceof NotLoadedChildLineException){
            	NotLoadedChildLineException exception = (NotLoadedChildLineException)e;
            	HashMap<String, String> map = exception.getExceptionInfoMap();
            	String itemID = map.get(NotLoadedChildLineException.ITEM_ID);
            	String itemRevID = map.get(NotLoadedChildLineException.ITEM_REVISION_ID);
            	String title = "New PLM : TC to PE I/F Error 알림";
            	body = "<PRE>";
        		body += "TC to PE Interface 중 하위 BOM line 을 읽지 못하는 에러 발생" + "<BR>";
        		body += " -Product ID: " + productInfoVO.getProductId() + "<BR>";
        		body += " -ECO NO. : " + productInfoVO.getEcoId() + "<BR>";
        		body += " -Item ID: " + itemID + "<BR>";
        		body += " -Revision ID : " + itemRevID + "<BR>";
        		body += "</PRE>";

            	DataSet ds = new DataSet();
        		ds.put("the_sysid", "NPLM");
        		ds.put("the_sabun", "NPLM");

        		ds.put("the_title", title);
        		ds.put("the_remark", body);
        		ds.put("the_tsabun", toUsers);

        		TcCommonDao commonDao;
				try {
					commonDao = TcCommonDao.getTcCommonDao();
					commonDao.update("com.symc.interface.sendMailEai", ds);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

            }else{

            	String title = "New PLM : TC to PE I/F Error 알림";
            	body = "<PRE>";
        		body += "TC to PE Interface 중, 에러 발생" + "<BR>";
        		body += " -Product ID: " + productInfoVO.getProductId() + "<BR>";
        		body += " -ECO NO. : " + productInfoVO.getEcoId() + "<BR>";
        		body += "</PRE>";

            	DataSet ds = new DataSet();
        		ds.put("the_sysid", "NPLM");
        		ds.put("the_sabun", "NPLM");

        		ds.put("the_title", title);
        		ds.put("the_remark", body);
        		ds.put("the_tsabun", toUsers);

        		TcCommonDao commonDao;
				try {
					commonDao = TcCommonDao.getTcCommonDao();
					commonDao.update("com.symc.interface.sendMailEai", ds);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

            }

    	}

    }
}
