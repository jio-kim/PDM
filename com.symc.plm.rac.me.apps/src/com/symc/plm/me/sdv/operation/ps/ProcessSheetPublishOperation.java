package com.symc.plm.me.sdv.operation.ps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.sdv.core.common.data.DataSet;
import org.sdv.core.common.data.IData;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.common.data.RawDataMap;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.operation.AbstractSDVActionOperation;

import com.kgm.common.WaitProgressBar;
import com.symc.plm.me.common.SDVBOPUtilities;
import com.symc.plm.me.common.SDVProcessUtils;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.sdv.excel.transformer.ProcessSheetPublishTransformer;
import com.symc.plm.me.sdv.operation.meco.MECOCreationUtil;
import com.symc.plm.me.utils.CustomUtil;
import com.symc.plm.me.utils.ProcessUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.cme.application.MFGLegacyApplication;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentBOPWindow;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR140820-052][20140708] shcho, MECO 반려 상태 중일때 작업표준서 Publish 할 수 있도록 수정
 * [SR141105-014][20141105] shcho, 작업표준서 Item(psItem) 생성시 RevisionID를 "000A" 고정 값에서, 파라미터로 넘겨받는 revId로 변경
 * [SR141105-014][20141217] shcho, 작업표준서 Item(psItem) 생성시 RevisionID를 파라미터로 넘겨받는 revId에서, "000A" 고정 값으로 다시 변경
 *                                      (Publish Revision생성 규칙은 숫자3자리+ 영문1자리다. 그리고 Migraiton 이후 신규 생성건은 000A부터 시작한다.)
 * [SR141105-014][20150105] shcho, 작업표준서 Item(psItem) 생성시 RevisionID를 고정값 "000A"에서  파라미터로 넘겨받는 revId+"A" 값으로 변경.
 *                                      (Publish Revision생성 규칙은 숫자3자리+ 영문1자리다. 그리고 W222의 경우 Migraiton 이후 한번도 Publish를 하지 않아서 000A고정값인 경우 문제가 될 수 있다.)
 * [SR141119-021][20150116] ymjang, 영문 작업표준서 결재란 공백 오류 수정 의뢰 
 *                                  1) 작성자는 현재 Publishing 하고 있는 사용자가 출력되도록 변경함.
 *                                  2) 결재자는 symcweb 에서 Scheduler 에 의해서 생성됨.
 * [P0078][20150225] ymjang, Publish 중 또는 결재중일때 표시되는 진행정보 윈도우창이 다른 프로그램을 사용해도 화면 맨 앞에 보여지고 있어 다른 프로그램 작업하기 곤란함
 * [NON_SR][20151002] taeku.jeong 이종화 차장님의 확인 요청으로 인해
 *                                              End Item MECO List를 읽어와서 변경기호를 표기해주는 부분의 Query 및 
 *                                              Publist 버튼 활성화 조건 추가
 */
public class ProcessSheetPublishOperation extends AbstractSDVActionOperation {
    private String processType;
//    private String productCode;
    private File file;
    private int configId = 0;
    private TCComponentItemRevision mecoRevision;

    private WaitProgressBar progress;

    private Registry registry = Registry.getRegistry(this);

    public ProcessSheetPublishOperation(int actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        System.out.println("ProcessSheetPublishOperation Type A");
    }

    public ProcessSheetPublishOperation(String actionId, String ownerId, IDataSet dataSet) {
        super(actionId, ownerId, dataSet);
        System.out.println("ProcessSheetPublishOperation Type B");
    }

    public ProcessSheetPublishOperation(int actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        System.out.println("ProcessSheetPublishOperation Type C");
    }

    public ProcessSheetPublishOperation(String actionId, String ownerId, Map<String, Object> parameters, IDataSet dataset) {
        super(actionId, ownerId, parameters, dataset);
        System.out.println("ProcessSheetPublishOperation Type D");
    }

    @Override
    public void startOperation(String commandId) {
    	
    	String actionId = getActionId();
    	String ownerId = getOwnerId();
    	int tempConfigId = 0;

    	System.out.println("actionId = "+actionId);	// actionId = Publish
    	System.out.println("ownerId = "+ownerId);	// ownerId = searchResultView
    	
    	if(ownerId==null || (ownerId!=null && ownerId.trim().equalsIgnoreCase("searchResultView")==false)){
    		return;
    	}
    	
        IDataSet dataset = getDataSet();
        List<HashMap<String, Object>> opList = null;

        if(dataset != null) {
            Collection<IDataMap> dataMaps = dataset.getAllDataMaps();
            if(dataMaps != null) {
                for(IDataMap dataMap : dataMaps) {
                    if(dataMap.containsKey("targetOperationList")) {
                        opList = dataMap.getTableValue("targetOperationList");
                        if(dataMap.containsKey("configId")) {
                        	tempConfigId = dataMap.getIntValue("configId");
                        }
                        break;
                    }
                }
            }
        }
        
        if(tempConfigId!=0){
        	System.out.println("Return 1");
        	return;
        }
        
        if(opList==null || (opList!=null && opList.size()<1)){
        	System.out.println("Return 2");
        	return;
        }
    }
    
    private boolean isEPLUpdateAble(TCComponentItemRevision mecoRevision, TCComponentItemRevision operationRevision){
    	
        boolean isMEPLLoadAble = false;

    	try {
    		boolean isReleased = false;
    		if(operationRevision!=null){
    			mecoRevision = (TCComponentItemRevision) operationRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
    		}
    		
			if(mecoRevision!=null){
				isReleased = CustomUtil.isReleased(mecoRevision);
			}
			
			if(isReleased==false && mecoRevision!=null){
				TCComponentProcess process = null;
				try {
					process = CustomUtil.getWorkFlowProcess(mecoRevision);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if(process==null){
					isMEPLLoadAble = true;
				}
			}

		} catch (TCException e) {
			e.printStackTrace();
		}

    	return isMEPLLoadAble;
    }

    @Override
    public void executeOperation() throws Exception {
        // 1. 이미지 템플릿 가져오기
        // 2. 이미지 템플릿을 복사하여 신규 파일 생성
        // 3. 신규 파일에 데이터 쓰기
        // 4. Publish 된 아이템이 있는지 체크
        //  4.1 있으면, 개정
        //  4.2 없으면, 신규 아이템 생성
        // 5. 아이템 리비전에 데이터셋 생성
        // 6. 데이터셋에 작업표준서 파일 할당
        // 7. MECO 번호 가져오기
        // 8. MECO에 작업표준서 리비전 할당

        progress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
        progress.setWindowSize(500, 400);
        progress.start();
        progress.setShowButton(false);
//        progress.setStatus("[" + new Date() + "]" + "Process Sheet Publish start.");
        progress.setStatus("Process Sheet Publish start.");
        progress.setAlwaysOnTop(true);

        try {
            IDataSet dataset = getDataSet();
            List<HashMap<String, Object>> opList = null;

//            progress.setStatus("[" + new Date() + "]" + "Getting target operations..");
            progress.setStatus("Getting target operations..");

            if(dataset != null) {
                Collection<IDataMap> dataMaps = dataset.getAllDataMaps();
                if(dataMaps != null) {
                    for(IDataMap dataMap : dataMaps) {
                        if(dataMap.containsKey("targetOperationList")) {
                            opList = dataMap.getTableValue("targetOperationList");
                            if(dataMap.containsKey("configId")) {
                                configId = dataMap.getIntValue("configId");
                            }
                            break;
                        }
                    }
                }
            }
            
            // [P0078] [20150225] ymjang, Publish 중 또는 결재중일때 표시되는 진행정보 윈도우창이 다른 프로그램을 사용해도 화면 맨 앞에 보여지고 있어 다른 프로그램 작업하기 곤란함
            // 영문작업표준서일 경우만, progress 창을 숨길 수 있도록 처리함.
            progress.setAlwaysOnTop(configId == 1 ? false : true);

            if(opList == null) {
                throw new Exception("Target Operations is null.");
            }

            int size = opList.size();
//            progress.setStatus("[" + new Date() + "]" + "Number of target operations : " + size);
            progress.setStatus( "Number of target operations : " + size);
            
			/////////////////////////////////////////////////////////////////////////////////////////////
			/*
			* [Non-SR]bc.kim Search Korean Process Sheet 에서 Publish 버튼 클릭시 
			*  	   MECO EPL 생성로직 추가 이종화 차장님 요청
			*/
            
            String mecoNo = "";
            TCComponentItemRevision operationRevision = null;
           for( HashMap<String, Object> operationMap : opList ) {
               boolean isReleased = false;        	   
        	   TCComponentBOPLine operationLine = (TCComponentBOPLine)operationMap.get( "OPERATION_BOPLINE" );
        	   
        	   if(operationLine!=null){
	   				try { 
	       				operationRevision = (TCComponentItemRevision) operationLine.getItemRevision();
		       			if(operationRevision != null){
		       				mecoRevision = (TCComponentItemRevision) operationRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
		       			}
		       			if(mecoRevision != null){
		       				mecoNo = mecoRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
		       				isReleased = CustomUtil.isReleased(mecoRevision);
		       			}
	   				} catch (TCException e) {
	   					e.printStackTrace();
					}
   				}
       			
       			if(isReleased==false && mecoRevision!=null && operationRevision!=null){
       				MECOCreationUtil aMECOCreationUtil = new MECOCreationUtil(mecoRevision);
       				boolean isValideMEPL = aMECOCreationUtil.isValideOperationMEPL(mecoNo, operationRevision);
       			
       			
       				if(isValideMEPL==false){
       					progress.setStatus("MECO EPL 생성중...");
       					progress.setStatus("EPL " + operationMap.get("item_id") + "생성중...");
						new CustomUtil().buildMEPL((TCComponentChangeItemRevision) mecoRevision, true);
       					progress.setStatus("EPL " + operationMap.get("item_id") + "생성완료");
       					progress.setStatus("MECO EPL 생성완료");
       				}
       				System.out.println("isValideMEPL = "+isValideMEPL);
       			
       			}
        	   
           }
			////////////////////////////////////////////////////////////////////////////////////////////

            AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
            TCComponentBOPLine shopLine = null;
            if(application instanceof MFGLegacyApplication) {
                TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();//bopWindow 안닫아도 되는지???
                shopLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
                this.processType = shopLine.getItemRevision().getTCProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).getStringValue();
            }
            
            // 2015-10-06 taeku.jeong Republish 강제로 가능하도록 수정
            boolean isForcedRepublish = false;
            String currentUserId = null;
        	String currentUserName = null;
        	
        	TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
        	if(session !=null){
        		currentUserId = session.getUser().getUserId();
        		currentUserName = session.getUserName();
        	}
        	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
        		if(((TCSession)AIFUtility.getCurrentApplication().getSession()).hasBypass()==true){
        			isForcedRepublish = true;
        		}
        	}

            // 국문일 경우 Publish 권한 체크를 수행한다.
            // 조립은 권한 해제PTP-A1-PVHA2019/000;1-C300 Assembly BOP (View)
        	// 2015-10-06 taeku.jeong : Bypass된 Infodba0, infodba 에게는 권한 Check 해재 추가
            if(configId == 0 && isForcedRepublish==false && "A".equals(processType)==false) {
                progress.setStatus("Authorization checking...");
                String loginUserId = ((TCSession) AIFUtility.getDefaultSession()).getUser().getUserId();
                List<String> mecoList = new ArrayList<String>();
                for(int i = 0; i < size; i++) {
                    String mecoId = (String) opList.get(i).get(SDVPropertyConstant.OPERATION_REV_MECO_NO);
                    if(mecoId != null && !mecoList.contains(mecoId)) {
                        mecoList.add(mecoId);
                    }
                }
                
                StringBuffer sb = new StringBuffer();
                // MECO의 Owning User가 아니면 Operation을 Publish 할 수 없도록 한다.
                for(String mecoId : mecoList) {
                    TCComponentItemRevision mecoRev = SDVBOPUtilities.FindItem(mecoId, SDVTypeConstant.MECO_ITEM).getLatestItemRevision();
                    TCComponentUser mecoOwningUser = (TCComponentUser) mecoRev.getReferenceProperty(SDVPropertyConstant.ITEM_OWNING_USER);
                    
                    System.out.println("user = "+mecoOwningUser.getUserId());
                    System.out.println("loginUserId = "+loginUserId);
                    
                    if(!loginUserId.equals(mecoOwningUser.getUserId())) {
                       sb.append(mecoId + "\n");
                    }
                }
                
                for(int i = 0; i < size; i++) {
                    HashMap<String, Object> operation = opList.get(i);
                    TCComponentBOPLine bopLine = (TCComponentBOPLine) operation.get("OPERATION_BOPLINE");
                    TCComponentUser itemRevisoinOwningUser = (TCComponentUser)bopLine.getItemRevision().getReferenceProperty("owning_user");
                    String owningUserId = itemRevisoinOwningUser.getUserId();
                    
                    System.out.println("itemRevisoinOwningUser = "+itemRevisoinOwningUser);
                    System.out.println("currentUserId = "+currentUserId);
                    
                    if(currentUserId!=null && currentUserId.trim().equalsIgnoreCase(owningUserId)==false){
                    	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
                    		continue;
                    	}
                    	 sb.append(bopLine.toString() + "\n");
                    }
                }
                
                if(sb.length() > 0) {
                    sb.insert(0, "The access is denied.\n");
                    throw new Exception(sb.toString());
                }
            }

//            progress.setStatus("[" + new Date() + "]" + "Start Publshing.");
            progress.setStatus("Start Publshing.");

            List<InterfaceAIFComponent> publishItemRevs = new ArrayList<InterfaceAIFComponent>();
            for(int i = 0; i < size; i++) {
                HashMap<String, Object> operation = opList.get(i);
                String itemId = (String) operation.get(SDVPropertyConstant.ITEM_ITEM_ID);
                String revId = (String) operation.get(SDVPropertyConstant.ITEM_REVISION_ID);

//                progress.setStatus("[" + new Date() + "]" + "Publishing process sheet of " + itemId + "/" + revId + " (" + (i + 1) + "/" + size + ")");
                progress.setStatus("Publishing process sheet of " + itemId + "/" + revId + " (" + (i + 1) + "/" + size + ")");
//                IDataSet resultDataset = getChildData(itemId, shopLine);

                TCComponentBOPLine bopLine = (TCComponentBOPLine) operation.get("OPERATION_BOPLINE");
                IDataSet resultDataset = getOperationData((TCComponentBOPLine) operation.get("OPERATION_BOPLINE"));
                
                // [SR141119-021] [20150116] ymjang, 작성자는 Publishing 하는 사용자로 재설정함.
                IDataMap headerDataMap = resultDataset.getDataMap("HeaderInfo");
                
                // [NON_SR] [2015-10-16] taeku.jeong Infodba가 Publish 하는경우 작성자이름을Operatoin Item Revsion의 Owner로 설정하도록 수정함 
                TCComponentUser itemRevisoinOwningUser = (TCComponentUser)bopLine.getItemRevision().getReferenceProperty("owning_user");
                
            	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
            		headerDataMap.put(SDVPropertyConstant.ITEM_OWNING_USER, ProcessSheetUtils.getUserName(configId, itemRevisoinOwningUser), IData.STRING_FIELD);
            	}else{
            		headerDataMap.put(SDVPropertyConstant.ITEM_OWNING_USER, ProcessSheetUtils.getUserName(configId, CustomUtil.getTCSession().getUser()), IData.STRING_FIELD);
            	}
                resultDataset.addDataMap("HeaderInfo", headerDataMap); 
                
                this.file = getFile(bopLine.getItemRevision());
                String itemUid = null;
                if(bopLine!=null){
                	itemUid = bopLine.getItem().getUid();
                }

                // meco Setting
                TCComponentItemRevision itemRevision = bopLine.getItemRevision();
                mecoRevision = (TCComponentItemRevision) itemRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);

                // Password 설정
                IDataMap pwMap = new RawDataMap();
                
                if(itemUid!=null){
                    System.out.println("uid : " + itemUid);
                    pwMap.put("Password", itemUid, IData.STRING_FIELD);                	
                }else{
                    System.out.println("uid : " + operation.get("UID"));
                    pwMap.put("Password", operation.get("UID"), IData.STRING_FIELD);                	
                }
                resultDataset.addDataMap("Password", pwMap);
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                /*
                 * Publish 시 Validation 추가 
                 * 작표 Publish 시 End Item 변경기호 유무를 체크 하여 없을 경우 에러 및 로그 발생하여 Publish 종료
                 */
               
	                List<HashMap<String, Object>> endItemList = (List<HashMap<String, Object>>) resultDataset.getDataMap("EndItemList").getTableValue("EndItemList");
	                for(HashMap<String, Object> endItemHash : endItemList) {
                	 if( endItemHash.get("SYMBOL") == null ||  endItemHash.get("SYMBOL").toString().equals("")) {
                		  String endItem = endItemHash.get("bl_item_item_id").toString();
                		  String endItemRev = endItemHash.get("bl_rev_item_revision_id").toString();
                		  throw new Exception("Error : " + endItem + "/" + endItemRev + "의 변경기호가 누락 되었습니다.");
                		 
                	 } 
	              }
                
               
                
        //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	    
	    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	                IDataMap bomLine = new RawDataMap();   
	                bomLine.put("BOMLINE_OBJECT", bopLine,  IData.OBJECT_FIELD);
	                resultDataset.addDataMap("BOMLINE_OBJECT", bomLine);
	    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                ProcessSheetPublishTransformer transformer = new ProcessSheetPublishTransformer();
                transformer.setProcessType(processType);
                transformer.print(file, resultDataset, configId);

                // Publish 된 아이템 리비전 가져오기
                TCComponentItemRevision psItemRevision = getProcessSheetItemRev(itemId, revId);
                if(psItemRevision == null) {
                    if(i < size - 1) {
                        int retVal = ConfirmDialog.prompt(UIManager.getCurrentDialog().getShell(),
                                "Publish", registry.getString("OperationContinue.Message"));
                        if(retVal == IDialogConstants.YES_ID) {
                            continue;
                        } else {
                            throw new Exception(registry.getString("OperationCancel.Message"));
                        }
                    } else {
                        throw new Exception(registry.getString("OperationCancel.Message"));
                    }
                }

                // [NON_SR] [2015-10-16] taeku.jeong Infodba가 Publish 하는경우 작성자이름을Operatoin Item Revsion의 Owner로 설정하도록 수정함
                // [NON_SR] [2015-10-20] taeku.jeong Infodba가 Publish 하는경우 작성일을 Released 날짜로 설정한다.
            	if(currentUserName!=null && currentUserName.indexOf("infodba")>=0){
            		itemRevisoinOwningUser = null;
            		itemRevisoinOwningUser = (TCComponentUser)bopLine.getItemRevision().getReferenceProperty("owning_user");
            		operation.put(SDVPropertyConstant.ITEM_OWNING_USER, ProcessSheetUtils.getUserName(configId, itemRevisoinOwningUser));
            		
                    // Preview 화면의 Publish 정보를 Refresh하기 위해 dataMap에 데이터 설정
            		Date releasedDate = mecoRevision.getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
            		if(releasedDate!=null){
            			operation.put(SDVPropertyConstant.ITEM_CREATION_DATE, releasedDate);
            		}else{
            			operation.put(SDVPropertyConstant.ITEM_CREATION_DATE, psItemRevision.getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE));
            		}

            	}else{
            		TCComponentUser user = (TCComponentUser) psItemRevision.getReferenceProperty(SDVPropertyConstant.PS_REV_LAST_PUB_USER);
            		operation.put(SDVPropertyConstant.ITEM_OWNING_USER, ProcessSheetUtils.getUserName(configId, user));
            		
                    // Preview 화면의 Publish 정보를 Refresh하기 위해 dataMap에 데이터 설정
                    operation.put(SDVPropertyConstant.ITEM_CREATION_DATE, psItemRevision.getDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE));
            	}

                // 국문이면, MECO에 작업표준서 Add
                if(configId == 0) {
                    TCComponent[] releaseStatusList = mecoRevision.getReferenceListProperty(SDVPropertyConstant.ITEM_REV_RELEASE_STATUS_LIST);
                    // MECO가 이미 Release 된 이후에 재 Publish의 경우
                    if(releaseStatusList != null && releaseStatusList.length > 0) {
                        // 자동 Release
                        String prefName = registry.getString("PublishProcessSheet_WorkflowTemplateNamePreference_KO");
                        TCPreferenceService prefService = ((TCSession) getSession()).getPreferenceService();
//                        String templateName = prefService.getString(TCPreferenceService.TC_preference_site, prefName);
                        String templateName = prefService.getStringValueAtLocation(prefName, TCPreferenceLocation.OVERLAY_LOCATION);

                        SDVProcessUtils.createProcess(templateName, registry.getString("ProcessSheetPublishProcessDescription"), new TCComponent[] {psItemRevision});
                    } else {
                        TCComponent[] comps = mecoRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                        boolean checkFlag = false;
                        if(comps != null) {
                            for(TCComponent comp : comps) {
                                String psItemId = comp.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
                                if(psItemId.endsWith(itemId)) {
                                    checkFlag = true;
                                    break;
                                }
                            }
                        }
                        if(!checkFlag) {
                            mecoRevision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, psItemRevision);
                        }
                    }
                }

                publishItemRevs.add(psItemRevision);
            }

//            progress.setStatus("[" + new Date() + "]" + "Complete to publish.");
            progress.setStatus("Complete to publish.");
            progress.close();

            setDataSet(dataset);

            if(configId == 0) {
                MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Publish", MessageBox.INFORMATION);
            } else {
                int retVal = ConfirmDialog.prompt(UIManager.getCurrentDialog().getShell(), "Publish", registry.getString("ProcessSheetWorkflowRequest.Message"));
                if(retVal == IDialogConstants.YES_ID) {
                    InterfaceAIFComponent[] comps = new InterfaceAIFComponent[publishItemRevs.size()];
                    for(int i = 0; i < publishItemRevs.size(); i++) {
                        comps[i] = publishItemRevs.get(i);
                    }
                    new SDVNewProcessCommand(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), comps,
                            registry.getString("PublishProcessSheet_WorkflowTemplateNamePreference_EN"));
                }
            }
        } catch(Exception e) {
        	if(file != null){
        		String fileName = file.toString();
            	String folderName = fileName.substring(0, fileName.lastIndexOf("\\"));
            	File folder = new File(folderName);
            	file.delete();
            	folder.delete();
        	}
        	progress.setStatus(e.getMessage());
            progress.close("작업표준서 생성이 중단 되었습니다. 진행 정보창의 메시지 확인 하시기 바랍니다.",true,false);
            progress.setShowButton(true);
            e.printStackTrace();
        }
    }

    /**
     * [SR141105-014][20141105] shcho, 작업표준서 Item(psItem) 생성시 RevisionID를 "000A" 고정 값에서, 파라미터로 넘겨받는 revId로 변경
     * [SR141105-014][20141217] shcho, 작업표준서 Item(psItem) 생성시 RevisionID를 파라미터로 넘겨받는 revId에서, "000A" 고정 값으로 다시 변경
     *                                      (Publish Revision생성 규칙은 숫자3자리+ 영문1자리다. 그리고 Migraiton 이후 신규 생성건은 000A부터 시작한다.)
     * [SR141105-014][20150105] shcho, 작업표준서 Item(psItem) 생성시 RevisionID를 고정값 "000A"에서  파라미터로 넘겨받는 revId+"A" 값으로 변경.
     *                                      (Publish Revision생성 규칙은 숫자3자리+ 영문1자리다. 그리고 W222의 경우 Migraiton 이후 한번도 Publish를 하지 않아서 000A고정값인 경우 문제가 될 수 있다.)
     *                                      
     * 
     * @param itemId
     * @param revId
     * @return
     * @throws Exception
     */
    private TCComponentItemRevision getProcessSheetItemRev(String itemId, String revId) throws Exception {
        TCComponentItemRevision psItemRevision = null;
        TCComponentItem psItem = SDVBOPUtilities.FindItem(getProcessSheetPrefix() + itemId, SDVTypeConstant.PROCESS_SHEET_ITEM);

        if(psItem == null) {
            psItem = SDVBOPUtilities.createItem(SDVTypeConstant.PROCESS_SHEET_ITEM, getProcessSheetPrefix() + itemId, revId+"A", "", "");

//            addToFolder(psItem);
            psItemRevision = psItem.getLatestItemRevision();
        } else {
            for(TCComponent revision : psItem.getRelatedComponents("revision_list")) {
                revision.refresh();
            }

            // [SR140820-052][20140708] shcho, MECO 반려 상태 중일때 작업표준서 Publish 할 수 있도록 수정
            TCComponentItemRevision[] revisions = psItem.getInProcessItemRevisions();
            if(revisions != null && revisions.length > 0) {
                for(TCComponentItemRevision revision : revisions) {
                    if(revision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(revId)) {
                        if(ProcessUtil.isWorkingStatus(revision)) {
                            psItemRevision = revision;
                        } else {
                            progress.setStatus("[" + new Date() + "]" + "Error : " + "공법" + itemId + "/" + revId + registry.getString("ProcessSheetInProcess.Message"));
                            return null;
                        }
                    }
                }
            }

            revisions = psItem.getWorkingItemRevisions();
            if(revisions != null && revisions.length > 0) {
                for(TCComponentItemRevision revision : revisions) {
                    if(revision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID).startsWith(revId)) {
                        psItemRevision = revision;
                    }
                }
            }

            if(psItemRevision == null) {
                String latestRevId = psItem.getLatestItemRevision().getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
                if(latestRevId.startsWith(revId)) {
                    revId = psItem.getNewRev();
                } else {
                    revId = revId + "A";
                }

                psItemRevision = psItem.revise(revId, psItem.getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME), "");
            }
        }

        AIFComponentContext[] contexts = psItemRevision.getChildren();
        TCComponentDataset dataset = null;
        if(contexts != null) {
            for(int i = 0; i < contexts.length; i++) {
                if(contexts[i].getComponent() instanceof TCComponentDataset) {
                    dataset = (TCComponentDataset) contexts[i].getComponent();
                    break;
                }
            }
        }

        Vector<File> importFiles = new Vector<File>();
        importFiles.add(file);
        if(dataset == null) {
            String datasetName = getProcessSheetPrefix() + itemId + "/" + revId + "A";
            SYMTcUtil.createDataSet((TCSession) AIFUtility.getDefaultSession(), psItemRevision, "MSExcelX", datasetName, importFiles);
        } else {
            SYMTcUtil.removeAllNamedReference(dataset);
            SYMTcUtil.importFiles(dataset, importFiles);
        }

        file.delete();

        psItemRevision.setDateProperty(SDVPropertyConstant.PS_REV_LAST_PUB_DATE, new Date());
        psItemRevision.setReferenceProperty(SDVPropertyConstant.PS_REV_LAST_PUB_USER, ((TCSession) getSession()).getUser());

        return psItemRevision;
    }

//    private void addToFolder(TCComponentItem item) throws TCException {
//        String[] folderNames = new String[4];
//        folderNames[0] = registry.getString("ProcessSheetFolderName");
//        folderNames[1] = registry.getString("ProcessSheetFolderName." + configId);
//        folderNames[2] = productCode;
//        folderNames[3] = registry.getString("ProcessSheetFolderName." + processType);
//
//        TCComponentFolder folder = getFolder(folderNames);
//        if(folder != null) {
//            folder.setRelated("contents", new TCComponent[] {item});
//        }
//    }
//
//    private TCComponentFolder getFolder(String[] folderNames) throws TCException {
//        TCComponentFolder[] parents = new TCComponentFolder[folderNames.length];
//        for(int i = 0; i < folderNames.length; i++) {
//            if(i == 0) {
//                parents[i] = findFolder(null, folderNames[i]);
//            } else {
//                parents[i] = findFolder(parents[i - 1], folderNames[i]);
//            }
//        }
//
//        return parents[folderNames.length - 1];
//    }
//
//    private TCComponentFolder findFolder(TCComponentFolder parent, String name) throws TCException {
//        TCComponentFolder folder = null;
//        if(parent == null) {
//            TCComponent[] comps = SDVQueryUtils.executeSavedQuery("General...", new String[] {"Type", "Name"}, new String[] {"Folder", name});
//            if(comps != null && comps.length > 0) {
//                folder = (TCComponentFolder) comps[0];
//            }
//        } else {
//            TCComponent[] comps = parent.getRelatedComponents("contents");
//            if(comps != null) {
//                for(int i = 0; i < comps.length; i++) {
//                    if(comps[i] instanceof TCComponentFolder && comps[i].getProperty(SDVPropertyConstant.ITEM_OBJECT_NAME).equals(name)) {
//                        folder = (TCComponentFolder) comps[i];
//                        break;
//                    }
//                }
//            }
//        }
//
//        if(folder == null) {
//            // FIXME : 폴더 이름에 ??이 붙음
//            folder = SYMTcUtil.createFolder((TCSession) getSession(), "Folder", name);
////            folder = SDVBOPUtilities.createFolder(name, "");
//        }
//
//        if(parent != null) {
//            parent.setRelated("contents", new TCComponent[] {folder});
//        }
//
//        return folder;
//    }

    private String getProcessSheetPrefix() {
        return registry.getString("ProcessSheetItemIDPrefix." + this.configId);
    }

//    protected IDataSet getData(String itemId, ) throws Exception {
//        IDataSet dataSet = null;

//        AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
//        if(application instanceof MFGLegacyApplication) {
//            TCComponentBOPWindow bopWindow = (TCComponentBOPWindow) ((MFGLegacyApplication) application).getBOMWindow();
//            try {
//                TCComponentBOPLine topLine = (TCComponentBOPLine) bopWindow.getTopBOMLine();
//                dataSet = getChildData(itemId, topLine);
//            } catch (TCException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return dataSet;
//    }

//    private IDataSet getChildData(String itemId, TCComponentBOPLine bopLine) throws Exception {
//        IDataSet dataset = null;
//        if(ProcessSheetUtils.isOperation(bopLine)) {
//            if(itemId.equals(bopLine.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID))) {
//                dataset = getOperationData(bopLine);
//                this.file = getFile(bopLine.getItemRevision());
//
//                // meco Setting
//                TCComponentItemRevision itemRevision = bopLine.getItemRevision();
//                mecoRevision = (TCComponentItemRevision) itemRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
//            }
//        } else {
////            if(ProcessSheetUtils.isShop(bopLine)) {
////                this.processType = bopLine.getItemRevision().getTCProperty(SDVPropertyConstant.SHOP_REV_PROCESS_TYPE).getStringValue();
////                this.productCode = bopLine.getItemRevision().getProperty(SDVPropertyConstant.SHOP_REV_PRODUCT_CODE);
////            }
//
//            if(ProcessSheetUtils.isShop(bopLine) || ProcessSheetUtils.isLine(bopLine) || ProcessSheetUtils.isStation(bopLine)) {
//                if(bopLine.getChildrenCount() > 0) {
//                    AIFComponentContext[] contexts = bopLine.getChildren();
//                    for(AIFComponentContext context : contexts) {
//                        dataset = getChildData(itemId, (TCComponentBOPLine) context.getComponent());
//                        if(dataset != null) break;
//                    }
//                }
//            }
//        }
//
//        return dataset;
//    }

    private File getFile(TCComponentItemRevision revision) throws Exception {
        File file = null;
        TCComponent[] dataSets = null;
        if(configId == 0) {
            dataSets = revision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
        } else {
            dataSets = revision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
        }

        String itemId = revision.getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
        String revId = revision.getProperty(SDVPropertyConstant.ITEM_REVISION_ID);
        if(configId == 1) {
            itemId = ProcessSheetUtils.changeToEnglishOperationId(itemId, processType);
        }

        if(dataSets == null || dataSets.length == 0) {
            dataSets = new TCComponentDataset[1];

            if(configId == 0) {
                dataSets[0] = SDVBOPUtilities.getTemplateDataset("M7_TEM_DocItemID_ProcessSheet_Kor", itemId + "/" + revId, itemId);
                revision.add(SDVTypeConstant.PROCESS_SHEET_KO_RELATION, dataSets[0]);
            } else {
                dataSets[0] = ProcessSheetUtils.translateProcessSheet(revision, processType);
                revision.add(SDVTypeConstant.PROCESS_SHEET_EN_RELATION, dataSets[0]);
            }
        }

        String fileName = itemId + "-" + revId + ".xlsx";

        for(int i = 0; i < dataSets.length; i++) {
            if(dataSets[i] instanceof TCComponentDataset) {
                TCComponentDataset dataSet = (TCComponentDataset) dataSets[i];
                TCComponentTcFile[] files = dataSet.getTcFiles();
                if(files != null && files.length > 0) {
                    file = files[0].getFile(null, fileName);
                }
            }
        }

        return file;
    }

    private IDataSet getOperationData(TCComponentBOPLine bopLine) throws Exception {
        ProcessSheetDataHelper dataHelper = new ProcessSheetDataHelper(this.processType, configId);

        IDataSet dataSet = new DataSet();
        dataSet.addDataMap("MECOList", dataHelper.getMECOList(bopLine));
        dataSet.addDataMap("HeaderInfo", dataHelper.getHeaderInfo(bopLine));
        dataSet.addDataMap("ResourceList", dataHelper.getResourceList(bopLine));
        dataSet.addDataMap("ActivityList", dataHelper.getActivityList(bopLine));
        dataSet.addDataMap("EndItemList", dataHelper.getEndItemList(bopLine));
        dataSet.addDataMap("SubsidiaryList", dataHelper.getSubsidiaryList(bopLine));
        dataSet.addDataMap("OperationInfo", dataHelper.getOperationInfo(bopLine));

        return dataSet;
    }

    @Override
    public void endOperation() {
//        if(configId == 0 && getExecuteResult() == ISDVActionOperation.SUCCESS) {
////            progress.setStatus("[" + new Date() + "]" + "Complete to publish.");
////            progress.close();
//            MessageBox.post(UIManager.getCurrentDialog().getShell(), registry.getString("OperationComplete.Message"), "Publish", MessageBox.INFORMATION);
//        }
    }

//    private boolean loadMECOEPL(List<String> mecoList) throws Exception {
//        CustomUtil cutomUtil = new CustomUtil();
//        for(String mecoId : mecoList) {
//            progress.setStatus("[" + new Date() + "]" + "Loading MEPL of " + mecoId);
//            TCComponentItem mecoItem = SDVBOPUtilities.FindItem(mecoId, SDVTypeConstant.MECO_ITEM);
//            if(mecoItem != null) {
//					 // MEPL 생성 (MECO_EPL Table에 Data 생성)
//                ArrayList<SYMCBOPEditData> meplList = cutomUtil.buildMEPL((TCComponentChangeItemRevision) mecoItem.getLatestItemRevision());
//                if(meplList == null){
//                    return false; //throw new NullPointerException("Error occured on M-EPL loading");
//                }
//            }
//        }
//
//        return true;
//    }

}
