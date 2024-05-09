package com.kgm.commands.sos;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMViewRevision;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.tcservices.TcBOMService;
import com.teamcenter.rac.kernel.tcservices.TcResponseHelper;
import com.teamcenter.rac.pse.plugin.Activator;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;

public class SpecExcelUploadCommand extends AbstractAIFCommand {
	@Override
	protected void executeCommand() throws Exception {
		WaitProgressBar progress = new WaitProgressBar(AIFDesktop.getActiveDesktop());

		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		TCComponent cTarget = this.getTarget();

		TCComponentBOMLine blProduct = null;
		TCComponentBOMWindow bwProduct = null;

		boolean isVirtualBOMWindow = false;

		try {
			// Product Item or Item Revision or BOM Line을 선택했을 경우에만 생성 가능. (User Spec)
//			if (cTarget == null || (!((cTarget instanceof TCComponentBOMLine) && ((TCComponentBOMLine) cTarget).getItem().getType().equals("S7_Product")) && !((cTarget instanceof TCComponentItem) && ((TCComponentItem) cTarget).getType().equals("S7_Product")) && !((cTarget instanceof TCComponentItemRevision) && ((TCComponentItemRevision) cTarget).getItem().getType().equals("S7_Product")))) {
//				MessageBox.post(AIFUtility.getActiveDesktop(), "Select a Product item or Product Top BOM Line.", "INFO", MessageBox.INFORMATION);
//				return;
//			}
			
			// Structure Manager에서 Product BOM Line을 Top으로 하는 BOM Window를 열었을 경우에만 작업 수행 가능.
			if (cTarget == null || !((cTarget instanceof TCComponentBOMLine) && ((TCComponentBOMLine) cTarget).getItem().getType().equals("S7_Product"))) {
				MessageBox.post(AIFUtility.getActiveDesktop(), "Select a Product item or Product Top BOM Line.", "INFO", MessageBox.INFORMATION);
				return;
			}

			// BOM Window 생성 및 Top BOM Line 지정.
//			if (cTarget instanceof TCComponentBOMLine && ((TCComponentBOMLine) cTarget).getItem().getType().equals("S7_Product")) {
//				blProduct = (TCComponentBOMLine) cTarget;
//				bwProduct = blProduct.window();
//			} else if (cTarget instanceof TCComponentItem && ((TCComponentItem) cTarget).getType().equals("S7_Product")) {
//				TCComponentItem item = (TCComponentItem) cTarget;
//				TCComponentItemRevision itemRevision = item.getLatestItemRevision();
//
//				bwProduct = getBOMWindow(itemRevision, "Latest Working", "bom_view");
//				blProduct = bwProduct.getTopBOMLine();
//
//				isVirtualBOMWindow = true;
//			} else if (cTarget instanceof TCComponentItemRevision && ((TCComponentItemRevision) cTarget).getItem().getType().equals("S7_Product")) {
//				TCComponentItemRevision itemRevision = (TCComponentItemRevision) cTarget;
//
//				bwProduct = getBOMWindow(itemRevision, "Latest Working", "bom_view");
//				blProduct = bwProduct.getTopBOMLine();
//
//				isVirtualBOMWindow = true;
//			}

			blProduct = (TCComponentBOMLine) cTarget;
			bwProduct = blProduct.window();
			
			if (!bwProduct.getTopBOMLine().getItem().getType().equals("S7_Product")) {
				MessageBox.post("The top line is not a Product.", "Error", MessageBox.ERROR);
				return;
			}

			if (bwProduct.isModified()) {
				MessageBox.post("Save first.", "Error", MessageBox.ERROR);
				return;
			}

			FileDialog dlgFile = new FileDialog(AIFUtility.getActiveDesktop().getShell(), SWT.OPEN);
			dlgFile.setFilterExtensions(new String[] { "*.xls", "*.xlsx" });

			String sFilePath = dlgFile.open();

			if (sFilePath == null || sFilePath.equals("") || sFilePath.length() == 0) {
				return;
			}

			SpecExcelUploadOperation op = new SpecExcelUploadOperation(progress, session, sFilePath, bwProduct);
			session.queueOperation(op);

			super.executeCommand();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (isVirtualBOMWindow && bwProduct != null) {
				bwProduct.close();
			}

			if (progress != null) {
				progress.setShowButton(true);
			}
		}
	}

	public TCComponent getTarget() {
		TCComponent target = null;
		AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

		AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

		if (aaifcomponentcontext != null && aaifcomponentcontext.length == 1) {
			target = (TCComponent) aaifcomponentcontext[0].getComponent();
			return target;
		}

		return target;
	}

	/**
	 * BOM Window 를 가져옴
	 * 
	 * @param itemRevision
	 *            아이템 리비전
	 * @return
	 * @throws Exception
	 */
	public static TCComponentBOMWindow getBOMWindow(TCComponentItemRevision itemRevision, String ruleName, String viewType) throws Exception {
		TCComponentBOMWindow bomWindow = null;
		TCSession session = (TCSession) AIFUtility.getCurrentApplication().getSession();
		
		// BOM View Revision을 가져옴.
		TCComponentBOMViewRevision viewRevision = getBOMViewRevision(itemRevision, viewType);
		
		// 리비전 룰을 가져옴
		TCComponentRevisionRule revRule = CustomUtil.getRevisionRule(session, ruleName);
		
		// BOMWindow를 생성
		// memo : BOM View Revision, Revision Rule을 가져온 후 START-END 방법으로 해도 BOM Window 생성 가능.
		////// START ////// 
		TCPreferenceService svcTCPreference = (TCPreferenceService) OSGIUtil.getService(Activator.getDefault(), TCPreferenceService.class);
		svcTCPreference.setLogicalValueAtLocation("EnableEndItem_" + session.getUser().getUid(), true, TCPreferenceService.TCPreferenceLocation.USER_LOCATION);
		TcResponseHelper responseHelper = TcBOMService.openBOMWindow(session, null, itemRevision, null, viewRevision, revRule, null, null);
		TCComponent[] returnedObjects = responseHelper.getReturnedObjects();
		
		for (int inx = 0; inx < returnedObjects.length; inx++) {
			if (returnedObjects[inx] instanceof TCComponentBOMWindow) {
				bomWindow = (TCComponentBOMWindow) returnedObjects[inx];
			} else {
				try {
					System.out.println(session.getVariantService().askLineMvl((TCComponentBOMLine) returnedObjects[inx]));
				} catch (Exception e) {
				}
			}
		}
		
		if (bomWindow == null) {
			return null;
		}
		
		bomWindow.setProperty("fnd0bw_is_mono_mode", String.valueOf(true));
		svcTCPreference = (TCPreferenceService)OSGIUtil.getService(Activator.getDefault(), TCPreferenceService.class);
		svcTCPreference.removePreferenceInstanceAtLocation(TCPreferenceService.TCPreferenceLocation.USER_LOCATION, "EnableEndItem_" + session.getUser().getUid());
		////// END //////
		
		// 아래 코드를 위의 코드로 대체 가능
//		TCComponentBOMWindowType windowType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
//		bomWindow = windowType.create(revRule);
//		
//		bomWindow.setWindowTopLine(itemRevision.getItem(), itemRevision, null, viewRevision);
		
		
		return bomWindow;
	}

	/**
	 * 
	 * @param comp
	 * @param viewType
	 * @return
	 * @throws Exception
	 */
	public static TCComponentBOMViewRevision getBOMViewRevision(TCComponent comp, String viewType) throws Exception {
		TCComponent[] arrayStructureRevision = comp.getRelatedComponents("structure_revisions");
		for (TCComponent bvr : arrayStructureRevision) {
			TCComponentBOMViewRevision bomViewRevision = (TCComponentBOMViewRevision) bvr;
			if (bomViewRevision.getReferenceProperty("bom_view").getProperty("view_type").equals(viewType)) {
				return bomViewRevision;
			}
		}

		return null;
	}
}