package com.kgm.commands.common;

import javax.swing.JPanel;

import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.kgm.Opotion;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.exporttoexcel.ExportToExcelOperation;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.officeliveservices.ExcelExportOption;
import com.teamcenter.rac.officeliveservices.InterfaceExcelExportable;
import com.teamcenter.rac.services.ISelectionMediatorService;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.OSGIUtil;

/**
 * BOM Excel Report
 * 
 * @author baek
 * 
 */
public class BOMStructureReportCommand extends AbstractAIFCommand {

	private AbstractAIFUIApplication application = null;
	private InterfaceExcelExportable excelSupport;
	private InterfaceAIFComponent[] targets = null;
	private TCSession tcSession = null;

	public BOMStructureReportCommand() {

		application = AIFUtility.getCurrentApplication();
		InterfaceAIFComponent selecteTarget = application.getTargetComponent();

		if (selecteTarget == null) {
			MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "Select BOM Line(s) to Export ", "Error", MessageBox.INFORMATION);
			return;
		}

		try {
			tcSession = (TCSession) selecteTarget.getSession();

			setTargetEnableToExport();

			executeBomExportExcel();
		} catch (Exception ex) {
			MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), ex.toString(), "Error", MessageBox.ERROR);
		}
	}

	/**
	 * Export 할 수 있는 Target 설정
	 */
	private void setTargetEnableToExport() throws Exception {
		AbstractAIFUIApplication application = AIFUtility.getCurrentApplication();
		IWorkbenchWindow localIWorkbenchWindow = AIFUtility.getActiveDesktop().getDesktopWindow();
		if (localIWorkbenchWindow == null)
			localIWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage localIWorkbenchPage = localIWorkbenchWindow.getActivePage();
		IWorkbenchPart localIWorkbenchPart = localIWorkbenchPage.getActivePart();
		if (localIWorkbenchPart instanceof InterfaceExcelExportable) {
			this.excelSupport = ((InterfaceExcelExportable) localIWorkbenchPart);
			this.targets = this.excelSupport.getComponentsToExport(ExcelExportOption.UserSelected);
		} else {
			JPanel appPanel = null;
			if (application != null)
				appPanel = application.getApplicationPanel();
			if ((appPanel != null) && (appPanel instanceof InterfaceExcelExportable)) {
				this.excelSupport = ((InterfaceExcelExportable) appPanel);
				this.targets = this.excelSupport.getComponentsToExport(ExcelExportOption.UserSelected);
			} else {
				ISelectionMediatorService localISelectionMediatorService = (ISelectionMediatorService) OSGIUtil.getService(Activator.getDefault(),
						ISelectionMediatorService.class);
				this.targets = localISelectionMediatorService.getTargetComponents();
			}
		}
	}

	/**
	 * Export 되는 Target 가져옴
	 * 
	 * @return
	 */
	protected TCComponent[] getExportedTargets() {
		TCComponent[] exportedTragets = null;
		ISelectionMediatorService localISelectionMediatorService = (ISelectionMediatorService) OSGIUtil.getService(Activator.getDefault(),
				ISelectionMediatorService.class);
		if (this.excelSupport != null) {
			exportedTragets = (TCComponent[]) targets;
		} else {
			InterfaceAIFComponent[] arrayOfInterfaceAIFComponent = localISelectionMediatorService.getTargetComponents();
			exportedTragets = new TCComponent[arrayOfInterfaceAIFComponent.length];
			for (int i = 0; i < arrayOfInterfaceAIFComponent.length; i++) {
				exportedTragets[i] = (TCComponent) arrayOfInterfaceAIFComponent[i];
			}
		}
		return exportedTragets;
	}

	/**
	 * Excel Export
	 */
	private void executeBomExportExcel() {

		TCComponent[] exportTargets = getExportedTargets();
		String[] exportColumnProperties = excelSupport.getDisplayedPropertyNames();
		if(Opotion.isDebug) {
			ExportToExcelOperation op = new ExportToExcelOperation(exportTargets, (short) 1, exportColumnProperties, null, "Export in progress ...", false, false);
			tcSession.queueOperation(op);

		}else {
			ExportToExcelOperation op = new ExportToExcelOperation(exportTargets, (short) 1, exportColumnProperties, null, "Export in progress ...", false, false);
			tcSession.queueOperation(op);

		}
		
	}
}
