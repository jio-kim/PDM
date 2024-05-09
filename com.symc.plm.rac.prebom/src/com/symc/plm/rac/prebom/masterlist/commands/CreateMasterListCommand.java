package com.symc.plm.rac.prebom.masterlist.commands;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListModeDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.MasterListPreBomViewDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.SelectDateDlg;
import com.symc.plm.rac.prebom.masterlist.dialog.SelectFMPDlg;
import com.symc.plm.rac.prebom.masterlist.model.MasterListDataMapper;
import com.symc.plm.rac.prebom.masterlist.model.SimpleTcObject;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.masterlist.operation.BOMLoadOperation;
import com.symc.plm.rac.prebom.masterlist.operation.PreBOMLoadWithDateOperation;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.util.MessageBox;

public class CreateMasterListCommand extends AbstractAIFCommand {

	static MasterListModeDlg modeDlg = null;
	private static MasterListDlg dlg = null;
	private Date selectedDate = null;
	private TCComponentItemRevision selectedRevision = null;
//	private TCComponentBOMLine selectedLine = null;
	private TCComponentBOMLine workingFmpTopLine = null;
	
	@Override
	protected void executeCommand() throws Exception {
		
		InterfaceAIFComponent[] coms = CustomUtil.getTargets();
		
		if( coms[0] instanceof TCComponentBOMLine ){
			if( ((TCComponentBOMLine)coms[0]).window().isModified()){
				MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Save window first...", "INFO", MessageBox.INFORMATION);
				return;
			}
			selectedRevision = ((TCComponentBOMLine)coms[0]).getItem().getLatestItemRevision();
		}else if( coms[0] instanceof TCComponentItem){
			selectedRevision = ((TCComponentItem)coms[0]).getLatestItemRevision();
		}else if(coms[0] instanceof TCComponentItemRevision){
			selectedRevision = ((TCComponentItemRevision)coms[0]).getItem().getLatestItemRevision();
		}else{
			MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Select a Pre_Product, Pre_Function or Pre-FMP Type.", "INFO", MessageBox.INFORMATION);
			return;
		}
		
		String selectedType = selectedRevision.getType();
		
		if( selectedType.equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE) 
				|| selectedType.equals(TypeConstant.S7_PREFUNCTIONREVISIONTYPE)){
			preBomViewAction();
		}else if( selectedType.equals(TypeConstant.S7_PREFUNCMASTERREVISIONTYPE)){
			fmpAction((TCComponentItemRevision)selectedRevision);
		}else{
			MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), "Select a Pre_Product or Pre-FMP BOM Line.", "INFO", MessageBox.INFORMATION);
			return;
		}
		
	}
	
	public void loadPreProductInfo(Date selectedDate) {
		this.selectedDate = selectedDate;
		ArrayList<String> alSelectedFMP = new ArrayList<String>();
		
		// [SR없음][20151030][jclee] FMP 선택 Load
		SelectFMPDlg dlg = new SelectFMPDlg(this.selectedRevision, this.selectedDate);
		dlg.setVisible(true);
		
		if (!dlg.isOKClicked()) {
			return;
		}
		
		alSelectedFMP = dlg.getSelectedFMPs();
		
		final WaitProgressBar waitBar = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
		waitBar.start();
		final PreBOMLoadWithDateOperation operation = new PreBOMLoadWithDateOperation(selectedRevision, selectedDate, alSelectedFMP, waitBar);
		operation.addOperationListener(new InterfaceAIFOperationListener() {
			
			@Override
			public void startOperation(String arg0) {
				System.out.println("PreBOMLoadWithDateOperation Start : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));					
			}
			
			@Override
			public void endOperation() {
				// TODO Auto-generated method stub
				HashMap<String, Object> resultData = (HashMap<String, Object>)operation.getOperationResult();
				
				try {
					
					Object obj = resultData.get(PreBOMLoadWithDateOperation.DATA_ERROR);
					if( obj != null){
						throw (Exception)obj;
					}
					
					OSpec ospec = (OSpec)resultData.get(PreBOMLoadWithDateOperation.DATA_OSPEC);
					ArrayList<String> essentialNames = (ArrayList<String>)resultData.get(PreBOMLoadWithDateOperation.DATA_ESSENTIAL_NAMES);
					HashMap<String, StoredOptionSet> storedOptionSetMap = (HashMap<String, StoredOptionSet>)resultData.get(PreBOMLoadWithDateOperation.DATA_STORED_OPTION_SET);
					Vector<Vector> data = (Vector<Vector>)resultData.get(PreBOMLoadWithDateOperation.DATA_DATA);
					Date date = (Date)resultData.get(PreBOMLoadWithDateOperation.DATA_DATE);
					
					String currentUserId = (String)resultData.get("USER_ID");
					String currentUserName = (String)resultData.get("USER_NAME");
					String currentGroup = (String)resultData.get("USER_GROUP");
					String currentPa6Group = (String)resultData.get("USER_PA6_GROUP");
					boolean isCordinator = (boolean)resultData.get("IS_CORDINATOR");
					
					String itemId = selectedRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					final String title = "MasterList_" + itemId + "_View(" + sdf.format(date) + ")";
					
					MasterListPreBomViewDlg dlg = new MasterListPreBomViewDlg(title, ospec, data, null, null
							, essentialNames, currentUserId, currentUserName, currentGroup, currentPa6Group, isCordinator);
					dlg.setVisible(true);
					
					waitBar.dispose();
					
//					CreateMasterListCommand.super.executeCommand();
				} catch (Exception e) {
					e.printStackTrace();
					waitBar.setStatus(e.getMessage());
					waitBar.setShowButton(true);
				} finally {
					System.out.println("PreBOMLoadWithDateOperation End : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));					
				}
			}
		});
		selectedRevision.getSession().queueOperation(operation);
	}

	private void preBomViewAction() throws Exception{
		SelectDateDlg dlg = new SelectDateDlg(this);
		setRunnable(dlg);
		super.executeCommand();
	}
	
	private void fmpAction( final TCComponentItemRevision fmpRevision) throws Exception{
		
		final TCComponentItemRevision preProductRevision = (TCComponentItemRevision)BomUtil.getParent(fmpRevision, TypeConstant.S7_PREPRODUCTREVISIONTYPE);
		if( preProductRevision == null){
			MessageBox.post(AIFUtility.getActiveDesktop(), "Could not find PreProduct.", "INFO", MessageBox.INFORMATION);
			return;
		}
		
		TCProperty tcProp = fmpRevision.getTCProperty("s7_CCN_NO");
		// 릴리즈 되어 있는 경우는 Mode Selection Dialog Open
		if( CustomUtil.isReleased(fmpRevision) || tcProp == null || tcProp.toString().equals("")){
			if( modeDlg != null){
				modeDlg.dispose();
				modeDlg = null;
			}
			modeDlg = new MasterListModeDlg(this, preProductRevision, fmpRevision);
			modeDlg.setVisible(true);
		}else{
			try{
				openMasterListDlg(preProductRevision, fmpRevision, true);
			}catch(Exception e){
				MessageBox.post(AIFUtility.getActiveDesktop().getFrame(), e.getMessage(), "INFO", MessageBox.INFORMATION);
				return;
			}
		}
	}
	
	public void openMasterListDlg(final TCComponentItemRevision preProductRevision, final TCComponentItemRevision fmpRevision, final boolean isEditable) throws Exception{
		
//		TCComponentItemRevision fmpRevision = fmpLine.getItem().getLatestItemRevision();
		TCComponentItemRevision ospecRevision = null;
		
		TCProperty tcProp = fmpRevision.getTCProperty(PropertyConstant.ATTR_NAME_CCNNO);
		if( tcProp == null || tcProp.toString().equals("")){
			SimpleTcObject tcObj = modeDlg.getSelectedCCN();
			if( tcObj == null){
				throw new Exception("Could not found CCN."); 
			}
			TCComponentItemRevision ccnRevision = (TCComponentItemRevision)fmpRevision.getSession().stringToComponent( tcObj.getPuid());
			ospecRevision = BomUtil.getOSpecRevisionWithCCN(ccnRevision);
		}else{
			ospecRevision = BomUtil.getOSpecRevision(fmpRevision);
		}
		
		if( ospecRevision == null){
			throw new Exception("Could not found OSpec Revision.");
		}
		
//		CCN결재 시에 확인 하므로 MLM에서는 무조건 CCN에 있는 OSI_NO만 확인 후 처리.
//		TCComponentItemRevision ospecLatestRevision = ospecRevision.getItem().getLatestItemRevision();
//		if( !ospecRevision.equals(ospecLatestRevision)){
//			 int response = JOptionPane.showConfirmDialog(null, "OSpec version has been changed. \nAre you sure you want to apply the latest version?", "Confirm",
//			        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
//			 if (response == JOptionPane.YES_OPTION) {
//				 //최신 Ospec을 가져온다.
//				 ospecRevision = ospecLatestRevision;
//			 }else if( response == JOptionPane.NO_OPTION ){
//			 }else{
//				 return;
//			 }
//		}
		
		final WaitProgressBar waitBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
		waitBar.setSize(600, 300);
		waitBar.start();
		
//		waitBar.setLocationRelativeTo(AIFUtility.getActiveDesktop());
		
		/**
		 * FIXME: BOM Window 를 FMP 가 아니라 Function Revision 의 Window 을 연다.
		 */
		//Pre Function Revision
		TCComponentItemRevision preFunctionRevision = (TCComponentItemRevision)BomUtil.getParent(fmpRevision, TypeConstant.S7_PREFUNCTIONREVISIONTYPE);
		TCComponentBOMLine preFunctionTopBOMLine = CustomUtil.getBomline(preFunctionRevision, preFunctionRevision.getSession());
		
		AIFComponentContext[] fmpContexts = preFunctionTopBOMLine.getChildren();
		for(AIFComponentContext fmpContext : fmpContexts)
		{
			TCComponentBOMLine  fmpBOMLine = (TCComponentBOMLine)fmpContext.getComponent();
			if(fmpRevision.equals(fmpBOMLine.getItemRevision()))
			{
				workingFmpTopLine = fmpBOMLine;
				break;
			}
		}

		//정의된 옵션을 가져오기위해 Working Fmp Top Line을 생성한다.
		//fmpRevision을 Top으로 BOM Window를 생성하며, Top은 working상태일수도 있고, Release상태일 수도 있다.
		//final TCComponentBOMLine workingFmpTopLine = CustomUtil.getBomline(fmpRevision, fmpRevision.getSession());
		
		final BOMLoadOperation operation = new BOMLoadOperation(preProductRevision, workingFmpTopLine, ospecRevision, waitBar);
		operation.addOperationListener(new InterfaceAIFOperationListener() {
			
			@Override
			public void startOperation(String arg0) {
				System.out.println("BOMLoadOperation Start : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));					
				waitBar.setStatus("FMP BOM Loading...");
			}
			
			@Override
			public void endOperation() {
				
				HashMap<String, Object> resultData = (HashMap<String, Object>)operation.getOperationResult();
				Exception exception = (Exception)resultData.get(BOMLoadOperation.DATA_ERROR);
				if( exception != null){
					waitBar.setStatus(exception.getMessage());
					waitBar.setShowButton(true);
					return;
				}
				
				String[] contents = (String[])resultData.get("CONTENTS");
				
				OSpec ospec = operation.getOspec();
				if( ospec == null){
					waitBar.setStatus("Could not found OSpec.");
					waitBar.setShowButton(true);
					return;
				}
				
				MasterListDataMapper dataMapper = operation.getDataMapper();
				MasterListDataMapper releaseDataMapper = operation.getReleaseDataMapper();
				waitBar.setStatus("Creating dialog...");
				
				try {
					SimpleTcObject ccnObj = null;
					if( modeDlg != null){
						ccnObj = modeDlg.getSelectedCCN();
					}
					
					if( dlg != null){
						dlg.dispose();
					}
					
					if( !hasTrimOption(operation.getFmpOptionList())){
						waitBar.setStatus("FMP has not TRIM Options.");
						waitBar.setShowButton(true);
						return;
					}
					
					String userId = (String)resultData.get("USER_ID");
					String userName = (String)resultData.get("USER_NAME");
					String userGroup = (String)resultData.get("USER_GROUP");
					String userPa6Group = (String)resultData.get("USER_PA6_GROUP");
					boolean isCordinator = (Boolean)resultData.get("IS_CORDINATOR");
					
					dlg = new MasterListDlg(userId, userName, userGroup, userPa6Group, isCordinator, contents, operation.getEssentialNames(), operation.getStoredOptionSetMap(), operation.getOptionManager(), preProductRevision, workingFmpTopLine, operation.getFmpOptionList(), ospec, releaseDataMapper, dataMapper, ccnObj, isEditable);
					dlg.addWindowListener(new WindowAdapter(){

						@Override
						public void windowClosing(WindowEvent windowevent) {
							
							TCComponentBOMLine workingFmpTopLine = dlg.getWorkingFmpTopLine();
							if( workingFmpTopLine != null){
								try {
									TCComponentBOMWindow window = workingFmpTopLine.window();
									if( window != null){
										window.close();
									}
								} catch (TCException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							super.windowClosed(windowevent);
						}
						
					});
					waitBar.dispose();
					dlg.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
					waitBar.setStatus(e.getMessage());
					waitBar.setShowButton(true);
				} finally {
					System.out.println("BOMLoadOperation End : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
				}
			}
		});
		workingFmpTopLine.getSession().queueOperation(operation);
	}
	
	private boolean hasTrimOption(ArrayList<VariantOption> enableOptionList){
		for( int i = 0; i < enableOptionList.size(); i++){
			VariantOption variantOption = enableOptionList.get(i);
			if( variantOption.getOptionName().equalsIgnoreCase("TRIM")){
				return true;
			}
		}
		return false;
	}
}
