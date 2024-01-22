package com.symc.plm.rac.cme.biw.apa;

import java.awt.Frame;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.symc.plm.me.utils.BOPStructureDataUtility;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.aifrcp.AIFUtility;
//import com.teamcenter.rac.cme.framework.util.MFGStructureTypeUtil;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
//import com.teamcenter.rac.cme.framework.util.MFGStructureType;

// Referenced classes of package com.teamcenter.rac.cme.biw.apa:
//            APADialog


/**
 * [NON-SR][20150610] shcho, 용접점을 선택해서 작업시 에러가 발생하는 오류 수정 (TC9에서는 용접점 선택시 문제가 없었으나 TC10에서 오류 발생)
 * 
 *
 */
public class APAHandler extends AbstractHandler
    implements IExecutableExtension
{
	public String findKey = null;
	
    public APAHandler()
    {
    }

    @SuppressWarnings("rawtypes")
	public Object execute(ExecutionEvent executionevent)
        throws ExecutionException
    {
        final TCComponentBOMLine selectedTarget;
        final AbstractAIFSession session;
        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

        APAContentProvider getConProvider = new APAContentProvider(apaDialog);

        session = abstractaifuiapplication.getSession();
        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();
//        boolean flag = false;
        boolean viewFlag = false;
//        boolean occurrenceFlag = false;
        boolean pertCountFlag = true;
        boolean pertErrorCountFlag = true;
        boolean pertListFlag = false;
        String unPERTInformatikonStr = null;
        
        int stationCount = 0;
        ArrayList<HashMap<String, Object>> pertResult = new ArrayList<HashMap<String, Object>>();
        ArrayList<HashMap<String, Object>> pertDBResult = new ArrayList<HashMap<String, Object>>();
        if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1 && (aaifcomponentcontext[0].getComponent() instanceof TCComponentBOMLine))
        {
            selectedTarget = (TCComponentBOMLine)aaifcomponentcontext[0].getComponent();
            // jwlee 선택한 BOP 라인의 E-BOM 라인 정보 가져옴
            rootBOMLine = getConProvider.getBOMLine(selectedTarget);

            if (rootBOMLine != null){
                viewFlag = true;
            }
            
    		if(selectedTarget!=null){

    			//[NON-SR][20160128] Taeku.jeong Connected Part 구성을확하는 기존 Query가 너무 느려서
        		// 다른 방법으로 확인 하는 Query를 적용하기위해 아래의 부분을 추가 한다.
    			// [END] PERT 정보 확인 방법 개선 추가 ---- [20160128]
    			if(selectedTarget instanceof TCComponentBOMLine){
                	BOPStructureDataUtility aBOPStructureDataUtility = new BOPStructureDataUtility();
                	try {
						aBOPStructureDataUtility.deleteOldShopStructureData();
					} catch (Exception e) {
						e.printStackTrace();
					}

                	// BOM Line정보를 확인 한다. // 선택된 Bomline의 Shop, Line, Station 정보를 추출
                	aBOPStructureDataUtility.initBOMLineData((TCComponentBOMLine)selectedTarget);

                	String shopItemId = aBOPStructureDataUtility.getShopId();
                	String lineItemId = aBOPStructureDataUtility.getLineId();
                	String stationItemId = aBOPStructureDataUtility.getStationId();
                	
                	// BOP Structure Data를 생성한다.
                	String findKey = null;
                	try {
                		//2020-09-02 seho 리비전 룰 적용이 필요하여 수정함. 여기서는 그냥 latest revision으로.. 
						findKey = aBOPStructureDataUtility.makeNewBOPInformationData(shopItemId, null);
					} catch (Exception e) {
						e.printStackTrace();
					}
                	
                	if(findKey!=null){
                		
                		int predecessorStationCount = 0;
                		int allStationCount = aBOPStructureDataUtility.getAllStationCount(findKey);
                		if(stationItemId!=null){
                			List<HashMap> allPredecessorStationList = aBOPStructureDataUtility.getPredecessorStationsAtLine(findKey, stationItemId);
                			if(allPredecessorStationList!=null){
                				predecessorStationCount = allPredecessorStationList.size();
                			}
                		}
                		
                		Vector<String> predecessorLinesIdV = new Vector<String>();
                		List<HashMap> predecessorLines = aBOPStructureDataUtility.getPredecessorLines(findKey, lineItemId);
                		if(predecessorLines!=null){
                			for (int i = 0; predecessorLines!=null && i < predecessorLines.size(); i++) {
                				HashMap tempHashMap = predecessorLines.get(i);
                				// LINE_ID
                				String tempLineId = (String)tempHashMap.get("LINE_ID");
                				if(tempLineId!=null && 
                						tempLineId.trim().length()>0 && 
                						predecessorLinesIdV.contains(tempLineId.trim())==false){
                					predecessorLinesIdV.add(tempLineId.trim());
                				}
							}
                		}
                		List<HashMap> notPERTedList = aBOPStructureDataUtility.getUnPertedStationList(findKey);
                		int includedCurrentLine = 0;
                		if(notPERTedList!=null){
                			// PERT 연결되지 않은 Station이 속하 Line이 현재  Connected PART를 구성 하려고 하는 Line과
                			// 관련이 있는지 확인 해야 한다.
                			for (int i = 0;notPERTedList!=null && i < notPERTedList.size(); i++) {
                				HashMap aHashMap = notPERTedList.get(i);
                				//PARENT_ID, CHILD_ID, PARENT_APP_PATH_NODE_PUID, APP_NODE_PUID
                				String tempLineId = (String)aHashMap.get("PARENT_ID");
                				String tempStationId = (String)aHashMap.get("CHILD_ID");
                				if(tempLineId!=null && tempLineId.trim().equalsIgnoreCase(lineItemId.trim())==true){
                					includedCurrentLine++;
                					if(unPERTInformatikonStr==null){
                						unPERTInformatikonStr = tempLineId +" <-> "+tempStationId;
                					}else{
                						unPERTInformatikonStr = unPERTInformatikonStr+"\n" +tempLineId +" <-> "+tempStationId;
                					}
                				}else if(predecessorLinesIdV.contains(tempLineId)==true) {
                					includedCurrentLine++;
                					if(unPERTInformatikonStr==null){
                						unPERTInformatikonStr = tempLineId +" <-> "+tempStationId;
                					}else{
                						unPERTInformatikonStr = unPERTInformatikonStr+"\n" +tempLineId +" <-> "+tempStationId;
                					}
                				}
							}
                			// 이건 나중에 Warning 용으로 필요할듯.
                			int notPERTedListSize = notPERTedList.size();
                		}
                		
                		// Structure에 구성된 Station이 없는 경우
                		if(allStationCount < 1){
                			pertCountFlag = false;
                		}
                		// PERT 구성되지 않은 Data가 Connected Part 구성하려는 Station의
                		// PERT 구성 경로에 포함된 경우
                		if(includedCurrentLine>0){
                			pertErrorCountFlag = false;
                		}
                		
                	}
                	
    			}
    			// [END] PERT 정보 확인 방법 개선 추가 ---- [20160128]
    			
    		}
    		
            /**
             *  jwlee PERT 정보가 정상적으로 입력되어 있는지 확인한다
             *  검증 1 : PERT 순서대로 가져온 공정 갯수와 BOP 의 공정 갯수 비교
             *  검증 2 : 기존의 입력되어 있는 공정 순서와 DB 의 저장되어 있는 PERT 순서 비교
             */

    		// [NON-SR][20160128] Taeku.jeong Connected Part 구성을확하는 기존 Query가 너무 느려서
    		// 다른 방법으로 확인 하는 Query를 적용하기위해 아래의 부분은 Remark 한다.
    		// Remark Start --------- [20160128]
    		/*
            try {
                stationCount = getConProvider.getSelectBopStationCount(selectedTarget.window().getTopBOMLine());
                pertResult = getConProvider.getSelectBopStationPertCountList(selectedTarget.window().getTopBOMLine());
                pertDBResult = getConProvider.getSelectBopStationDecessorsList(selectedTarget.window().getTopBOMLine());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if ((stationCount == 0 || pertResult.size() == 0) || (stationCount != pertResult.size())) {
                pertCountFlag = false;
            }

            */
    		// Remark End --------- [20160128]
            
            // jwlee 선택한 BOP 라인의 OccurrenceGroup 이 생성되어 있는지 체크한다
//            try {
//                occurrenceFlag = getConProvider.occurrenceGroupCheck(selectedTarget);
//            } catch (TCException e) {
//                e.printStackTrace();
//            }


//            MFGStructureType mfgstructuretype = MFGStructureTypeUtil.getStructureType(selectedTarget);
//            if(mfgstructuretype.isProduct() || mfgstructuretype.isProcess() || mfgstructuretype.isOperation()){
//                flag = true;
//            }  
            //[NON-SR][20150610] shcho, 용접점을 선택해서 작업시 에러가 발생하는 오류 수정 (TC9에서는 용접점 선택시 문제가 없었으나 TC10에서 오류 발생)
//            if(MFGStructureTypeUtil.isProduct(selectedTarget) || MFGStructureTypeUtil.isProcess(selectedTarget) || MFGStructureTypeUtil.isOperation(selectedTarget)) {
//            	flag = true;
//            }
        }
        else
        {
            selectedTarget = null;
        }
        //[NON-SR][20150610] shcho, 용접점을 선택해서 작업시 에러가 발생하는 오류 수정 (TC9에서는 용접점 선택시 문제가 없었으나 TC10에서 오류 발생)
//        if(!flag)
//        {
//            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
//            MessageBox.post(registry.getString("wrongSelection.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
//            return null;
//        }
        if(!viewFlag)
        {
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            MessageBox.post(registry.getString("wrongNoOpenBOMView.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
            return null;
        }
        if (!pertCountFlag) {
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            MessageBox.post(registry.getString("wrongNotPertInfo.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
            return null;
        }
        
        // PERT 구성중 Successors 가 없는 공정이 1개 이상인지 체크한다
        //if (!getConProvider.getPertNotHaveSuccessorsCount(pertResult)) {
        // [NON-SR][20160128] Taeku.jeong Connected Part 구성을 위한 경로상에
        // PERT 연결이 누락된 Data가 있는지 확인 하고 누락된 Data를 보여 준다.
        if(pertErrorCountFlag==false){
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            String messageStr = registry.getString("wrongNotPertInfo.MESSAGE")+"\n"
            		+unPERTInformatikonStr;
            String messageTitle = registry.getString("wrongSelection.TITLE");
            
            System.out.println(""+messageStr);
            MessageBox.post(messageStr, messageTitle, 1);
            return null;
        }
  
/*
        // PERT 정보의 Update 가 필요한지 체크 필요하다면 Update 하고 진행
        pertListFlag = getConProvider.comparePertInfo(pertResult, pertDBResult);
        if (!pertListFlag) {
            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
            Shell shell = AIFUtility.getActiveDesktop().getShell();
            int confirmRet = ConfirmDialog.prompt(shell, registry.getString("Confirmation.TITLE", "Confirm"), registry.getString("BOPPertInfoUpdate.MESSAGE", "History information is also modified PERT. \n Do you want to modify?"));
            if (confirmRet == 2){
                try {
                    getConProvider.updatePertInfo(selectedTarget.window().getTopBOMLine(), pertDBResult);
                } catch (TCException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
*/        
//        if (!occurrenceFlag) {
//            registry = Registry.getRegistry(com.symc.plm.rac.cme.biw.apa.APAHandler.class);
//            MessageBox.post(registry.getString("wrongNotCreateOccurrenceGroup.MESSAGE"), registry.getString("wrongSelection.TITLE"), 1);
//            return null;
//        }
        try
        {
        	
/*        	
            Display display = Display.getDefault();
            display.asyncExec(new Runnable() {

                public void run()
                {
                    Shell shell = AIFUtility.getActiveDesktop().getShell();
                    apaDialog = new APADialog(shell, session, selectedTarget, rootBOMLine);
                    apaDialog.setContent();
                    if(apaDialog.isEmpty())
                        apaDialog.close();
                    else
                        apaDialog.open();
                }

                @SuppressWarnings("unused")
                final APAHandler this$0;


            {
                this$0 = APAHandler.this;
            }
            }
        );
*/
        	// [NON-SR][20160119] taeku.jeong Connected Part Dialog에서 Connected Part 검색중 UI 응답없음으로 나타나는 현상 수정 
            IWorkbench workbench = PlatformUI.getWorkbench();
            IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
            Shell shell = workbenchWindow.getShell();

            apaDialog = new APADialog(shell, session, selectedTarget, rootBOMLine);
            apaDialog.setContent();
            if(apaDialog.isEmpty()){
                apaDialog.close();
            }else{
                apaDialog.open();
            }
        }
        catch(Exception exception)
        {
            logger.error(exception.getClass().getName(), exception);
            MessageBox messagebox = new MessageBox(parentFrame, exception);
            messagebox.setModal(true);
            messagebox.setVisible(true);
        }
        return null;
    }

    public void setInitializationData(IConfigurationElement iconfigurationelement, String s, Object obj)
        throws CoreException
    {
        parentFrame = AIFDesktop.getActiveDesktop();
    }

    private static final Logger logger = Logger.getLogger(APAHandler.class);
    protected APADialog apaDialog;
    protected Frame parentFrame;
    protected TCComponentBOMLine rootBOMLine;
    protected Registry registry;

}
