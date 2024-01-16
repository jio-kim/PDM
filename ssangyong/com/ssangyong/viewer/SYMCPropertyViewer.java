package com.ssangyong.viewer;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISaveablePart;

import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentChangeEvent;
import com.teamcenter.rac.aif.kernel.AIFComponentEvent;
import com.teamcenter.rac.aif.kernel.AIFComponentPropertyChangeEvent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponentEventListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.tcviewer.TCComponentViewerInput;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRole;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.AdapterUtil;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.SWTUIUtilities;
import com.teamcenter.rac.util.event.ClientEventDispatcher;
import com.teamcenter.rac.util.event.IClientEvent;
import com.teamcenter.rac.util.viewer.IViewerEvent;
import com.teamcenter.rac.util.viewer.ViewerEvent;
import com.teamcenter.rac.viewer.utils.CheckInOutComposite;
import com.teamcenter.rac.viewer.view.AbstractSwtSubViewer;

/**
 * 
 * Viewer Custom
 * [SR161230-026] 20160110 자동으로 Refresh 으로 될수 있도록 시스템개선, 단 ECO는 제외함(Refresh 시 ECO Generate 를 창이 두번 나타나기 때문)
 */
public class SYMCPropertyViewer extends AbstractSwtSubViewer implements ISaveablePart, InterfaceAIFComponentEventListener {

	private Registry registry;

	private TCComponent targetComp;
	private Composite m_composite;
	private AbstractSYMCViewer symcViewer;

	public SYMCPropertyViewer(Composite parent) {
		registry = Registry.getRegistry(this);

		GridLayout parentLayout = SWTUIUtilities.tightGridLayout(1);
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(parentLayout);

		m_composite = new Composite(mainComposite, SWT.NONE);
		m_composite.setLayout(new FillLayout());
		m_composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		/**
		 * [TC10 Upgrade][2015.04.30][jclee]
		 * Check Out을 하고있는 Object를 선택했음에도 불구하고 Check Out버튼이 활성화되어있는 문제 대응
		 *  - 기존 Customizing 한 SYMCCheckInOutComposite를 OOTB CheckInOutComposite 로 교체
		 */
//		SYMCCheckInOutComposite m_cicoComposite = new SYMCCheckInOutComposite(mainComposite);
//		m_cicoComposite.addCommandId("com.ssangyong.commands.partmaster.vehiclepart.UpdateActWeightCommand");
//		if (isSESSpecNoAccessCheck()) {
//			m_cicoComposite.addCommandId("com.ssangyong.commands.partmaster.vehiclepart.UpdateSESSpecNoCommand");
//		}
//		m_cicoComposite.createToolbar();
//		m_cicoComposite.panelLoaded();
		CheckInOutComposite m_cicoComposite = new CheckInOutComposite(mainComposite);
		m_cicoComposite.panelLoaded();
		
		AIFUtility.getDefaultSession().addAIFComponentEventListener(this);
	}

	@Override
	public void setInput(Object viewerInput) {
		TCComponentViewerInput input = (TCComponentViewerInput) AdapterUtil.getAdapter(viewerInput, TCComponentViewerInput.class);
		targetComp = (TCComponent) input.getViewableObj();
		//[SR161230-026] 20160110 자동으로 Refresh 으로 될수 있도록 시스템개선, 단 ECO는 제외함(Refresh 시 ECO Generate 를 창이 두번 나타나기 때문)
		try {
			if(!targetComp.getType().equals("S7_ECORevision"))
				targetComp.refresh();
		} catch (TCException e) {
			e.printStackTrace();
		}
		
		String viewerKey = targetComp.getType() + ".COMPONENTVIEWER";
		symcViewer = (AbstractSYMCViewer) registry.newInstanceFor(viewerKey, m_composite);
		if (symcViewer == null) {

		} else {
			symcViewer.layout();
			symcViewer.setComponent(targetComp);
		}

		super.setInput(targetComp);
	}

	@Override
	public void inputChanged(Object input, Object oldInput) {
		// [SR150521-024][20150717][jclee] 불필요한 로드 작업으로 인한 속도 저하 방지.
//		SYMCPropertyViewerContentProvider cp = (SYMCPropertyViewerContentProvider) getContentProvider();
//		cp.inputChanged(this, oldInput, input);

		if (symcViewer != null) {
			symcViewer.setComponent(targetComp);
//			symcViewer.load();
		}

		ViewerEvent viewerEvent = new ViewerEvent(this, IViewerEvent.SHOW_HEADER);
		viewerEvent.queueEvent();
	}

	@Override
	public Control getControl() {
		return m_composite;
	}

	@Override
	public void refresh() {
		if (m_composite == null || m_composite.isDisposed()) {
			return;
		}

		if (symcViewer != null) {
			symcViewer.refresh();
		}

		m_composite.layout();
		try {
			if (targetComp.okToModify() && targetComp.isCheckedOut()) {
				if (symcViewer != null) {
					symcViewer.setEditable(true);
					symcViewer.setControlReadWrite(symcViewer);
				}
			} else {
				if (symcViewer != null) {
					symcViewer.setEditable(false);
					symcViewer.setControlReadOnly(symcViewer);
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	// @SuppressWarnings("rawtypes")
	// @Override
	// public Object getAdapter(Class adapter) {
	// if (adapter.equals(IContributionItem[].class)) {
	// return getCommandContributions();
	// }
	// return null;
	// }

	// private IContributionItem[] getCommandContributions() {
	// List<IContributionItem> list = new ArrayList<IContributionItem>();
	// CommandContributionItemParameter contributionParameters = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "", "com.teamcenter.rac.checkOut", CommandContributionItem.STYLE_PUSH);
	// contributionParameters.mode = CommandContributionItem.MODE_FORCE_TEXT;
	// list.add(new CommandContributionItem(contributionParameters));
	// contributionParameters = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "", "com.teamcenter.rac.checkIn", CommandContributionItem.STYLE_PUSH);
	// contributionParameters.mode = CommandContributionItem.MODE_FORCE_TEXT;
	// list.add(new CommandContributionItem(contributionParameters));
	// contributionParameters = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "", "com.teamcenter.rac.saveCheckOut", CommandContributionItem.STYLE_PUSH);
	// contributionParameters.mode = CommandContributionItem.MODE_FORCE_TEXT;
	// list.add(new CommandContributionItem(contributionParameters));
	// contributionParameters = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "", "com.teamcenter.rac.cancelCheckOut", CommandContributionItem.STYLE_PUSH);
	// contributionParameters.mode = CommandContributionItem.MODE_FORCE_TEXT;
	// list.add(new CommandContributionItem(contributionParameters));
	//
	// contributionParameters = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "", "com.ssangyong.commands.partmaster.vehiclepart.UpdateActWeightCommand", CommandContributionItem.STYLE_PUSH);
	// contributionParameters.mode = CommandContributionItem.MODE_FORCE_TEXT;
	// list.add(new CommandContributionItem(contributionParameters));
	//
	// // [SR140324-030][20140620] KOG DEV PropertyViewer (Viewer) 에서 User role Check for SES Spec No. Update Command Enable.
	// if (isSESSpecNoAccessCheck()) {
	// contributionParameters = new CommandContributionItemParameter(PlatformUI.getWorkbench(), "", "com.ssangyong.commands.partmaster.vehiclepart.UpdateSESSpecNoCommand", CommandContributionItem.STYLE_PUSH);
	// contributionParameters.mode = CommandContributionItem.MODE_FORCE_TEXT;
	// list.add(new CommandContributionItem(contributionParameters));
	// }
	//
	// return list.toArray(new IContributionItem[list.size()]);
	// }

	/**
	 * [SR140324-030][20140620] KOG DEV PropertyViewer (Viewer) 에서 User role Check for SES Spec No. Update Command Enable.
	 */
	private boolean isSESSpecNoAccessCheck() {
		InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();
		String type = targetComponents[0].getType();
		TCComponentItemRevision revision = null;
		if (type.equals(SYMCClass.S7_VEHPARTREVISIONTYPE) || type.equals(SYMCClass.S7_STDPARTREVISIONTYPE)) {
			revision = (TCComponentItemRevision) targetComponents[0];
		} else {
			return false;
		}

		try {
			if (CustomUtil.isWorkingStatus(revision)) {
				return true;
			} else {
				TCComponentUser user = CustomUtil.getTCSession().getUser();
				TCComponentGroup[] groups = user.getGroups();
				for (TCComponentGroup group : groups) {
					TCComponentRole[] roles = user.getRoles(group);
					for (TCComponentRole role : roles) {
						if (("CLASSIFICATIONADMIN").equals(role.toDisplayString())) {
							return true;
						}
					}
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}

		return false;
	}

	// Implementation of ISaveablePart
	@Override
	public boolean isDirty() {
		// Check if the value has been changed
		if (symcViewer != null) {
			return symcViewer.isDirty();
		}

		return true;
	}

	// Implementation of ISaveablePart
	public boolean isSaveAsAllowed() {
		return false;
	}

	// Implementation of ISaveablePart
	@Override
	public boolean isSaveOnCloseNeeded() {
		return true;
	}

	// Implementation of ISaveablePart
	@Override
	public void doSaveAs() {
		// not supported
	}

	// Implementation of ISaveablePart
	@Override
	public void doSave(final IProgressMonitor monitor) {
		Exception ex2 = null;
		
		try {
			setBusy(true);

			if (symcViewer != null) {
				if (!symcViewer.isSavable()) {
					ClientEventDispatcher.fireEventLater(SYMCPropertyViewer.this, IClientEvent.COMPLETE_LIST_LOADED,
							TCComponent.class, targetComp, Exception.class, ex2);
				    return;
				} else {
					symcViewer.save();

					// 2015-04-20 finally에 있던 내용을 여기로 옮김. 이유는 validate 메세지를 표시하고도 Check-In Dialog가 뜨는 현상이 발생되었기 때문.
					ClientEventDispatcher.fireEventLater(SYMCPropertyViewer.this, IClientEvent.SS_VIEWER_SAVE_COMPLETE, 
							TCComponent.class, targetComp, Exception.class, ex2);
				}
			}
		} catch (Exception e) {
			MessageBox.post(e);
			ex2 = e;
		} finally {
            setBusy(false);
		}
	}

	// Implement InterfaceAIFComponentEventListener
	@Override
	public void processComponentEvents(AIFComponentEvent[] events) {
		if (getInput() == null) {
			// nothing to be done.
			return;
		}

		boolean requireUpdate = false;
		Arrays.sort(events);
		for (AIFComponentEvent event : events) {
			InterfaceAIFComponent targetComponent = event.getComponent();
			if (targetComponent == getInput()) // changed.
			{
				if (event instanceof AIFComponentPropertyChangeEvent) {
					// For property change event, don’t need to refresh the whole panel
				} else if (event instanceof AIFComponentChangeEvent) {
					requireUpdate = true;
					break;
				}
			}
		}
		if (requireUpdate) {
			SWTUIUtilities.asyncExec(new Runnable() {
				// update the check in/out UI widgets only
				@Override
				public void run() {
					refresh();
				}
			});
		}
	}

	public void closeSignal() {
		AIFUtility.getDefaultSession().removeAIFComponentEventListener(this);
	}

}
