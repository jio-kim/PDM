package com.symc.plm.rac.prebom.common.viewer;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISaveablePart;

import com.symc.plm.rac.prebom.ccn.rendering.PreCCNRevisionRendering;
import com.symc.plm.rac.prebom.prebom.rendering.SYMCPreFuncMasterRevisionRendering;
import com.symc.plm.rac.prebom.prebom.rendering.SYMCPreFunctionRevisionRendering;
import com.symc.plm.rac.prebom.prebom.rendering.SYMCPreProductRevisionRendering;
import com.symc.plm.rac.prebom.prebom.rendering.SYMCPreProjectRevisionRendering;
import com.symc.plm.rac.prebom.prebom.rendering.SYMCPreVehiclePartRevisionRendering;
import com.teamcenter.rac.aif.kernel.AIFComponentChangeEvent;
import com.teamcenter.rac.aif.kernel.AIFComponentEvent;
import com.teamcenter.rac.aif.kernel.AIFComponentPropertyChangeEvent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponentEventListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.tcviewer.TCComponentViewerInput;
import com.teamcenter.rac.kernel.TCComponent;
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
 * [SR161230-026] 20160110 자동으로 Refresh 으로 될수 있도록 시스템개선
 */
public class PreSYMCPropertyViewer extends AbstractSwtSubViewer implements ISaveablePart, InterfaceAIFComponentEventListener {

	private Registry registry;

	private TCComponent targetComp;
	private Composite pre_composite;
	private AbstractPreSYMCViewer preSymcViewer;

	public PreSYMCPropertyViewer(Composite parent) {
		registry = Registry.getRegistry(this);

		GridLayout parentLayout = SWTUIUtilities.tightGridLayout(1);
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(parentLayout);

		pre_composite = new Composite(mainComposite, SWT.NONE);
		pre_composite.setLayout(new FillLayout());
		pre_composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		CheckInOutComposite pre_cicoComposite = new CheckInOutComposite(mainComposite);
		pre_cicoComposite.panelLoaded();
		
		AIFUtility.getDefaultSession().addAIFComponentEventListener(this);
	}

	@Override
	public void setInput(Object viewerInput) {
		TCComponentViewerInput input = (TCComponentViewerInput) AdapterUtil.getAdapter(viewerInput, TCComponentViewerInput.class);
		targetComp = (TCComponent) input.getViewableObj();
		
		//[SR161230-026] 20160110 자동으로 Refresh 으로 될수 있도록 시스템개선	
		try {
			targetComp.refresh();
		} catch (TCException e) {
			e.printStackTrace();
		}

		String viewerKey = targetComp.getType() + ".COMPONENTVIEWER";
		
		preSymcViewer = (AbstractPreSYMCViewer) registry.newInstanceFor(viewerKey, pre_composite);
		if (preSymcViewer == null) {
			if (viewerKey.equals("S7_PreCCNRevision.COMPONENTVIEWER")) {
				preSymcViewer = new PreCCNRevisionRendering(pre_composite);
			} else if (viewerKey.equals("S7_PreVehPartRevision.COMPONENTVIEWER")) {
				preSymcViewer = new SYMCPreVehiclePartRevisionRendering(pre_composite);				
			} else if (viewerKey.equals("S7_PreProductRevision.COMPONENTVIEWER")) {
				preSymcViewer = new SYMCPreProductRevisionRendering(pre_composite);
			} else if (viewerKey.equals("S7_PreFunctionRevision.COMPONENTVIEWER")) {
				preSymcViewer = new SYMCPreFunctionRevisionRendering(pre_composite);
			} else if (viewerKey.equals("S7_PreFuncMasterRevision.COMPONENTVIEWER")) {
				preSymcViewer = new SYMCPreFuncMasterRevisionRendering(pre_composite);
			} else if (viewerKey.equals("S7_PreProjectRevision.COMPONENTVIEWER")) {
				preSymcViewer = new SYMCPreProjectRevisionRendering(pre_composite);
			}
		} else {
			preSymcViewer.layout();
			preSymcViewer.setComponent(targetComp);
		}
		super.setInput(targetComp);
	}

	@Override
	public void inputChanged(Object input, Object oldInput) {
		PreSYMCPropertyViewerContentProvider cp = (PreSYMCPropertyViewerContentProvider) getContentProvider();
		cp.inputChanged(this, oldInput, input);

		if (preSymcViewer != null) {
			preSymcViewer.setComponent(targetComp);
			//[20160110]  두번 Load 하는 것 막음 		
			//preSymcViewer.load();
		}

		ViewerEvent viewerEvent = new ViewerEvent(this, IViewerEvent.SHOW_HEADER);
		viewerEvent.queueEvent();
	}

	@Override
	public Control getControl() {
		return pre_composite;
	}

	@Override
	public void refresh() {
		if (pre_composite == null || pre_composite.isDisposed()) {
			return;
		}

		if (preSymcViewer != null) {
			preSymcViewer.refresh();
		} else {
			pre_composite.layout();

			try {
				if (targetComp.okToModify() && targetComp.isCheckedOut()) {
					if (preSymcViewer != null) {
						preSymcViewer.setEditable(true);
						preSymcViewer.setControlReadWrite(preSymcViewer);
					}
				} else {
					if (preSymcViewer != null) {
						preSymcViewer.setEditable(false);
						preSymcViewer.setControlReadOnly(preSymcViewer);
					}
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}

	// Implementation of ISaveablePart
	@Override
	public boolean isDirty() {
		// Check if the value has been changed
		if (preSymcViewer != null) {
			return preSymcViewer.isDirty();
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

			if (preSymcViewer != null) {
				if (! preSymcViewer.isSavable()) {
				    ClientEventDispatcher.fireEventLater(PreSYMCPropertyViewer.this, IClientEvent.COMPLETE_LIST_LOADED, 
                            TCComponent.class, targetComp, Exception.class, ex2);
				    return;
				} else {
				    preSymcViewer.save();
				    ClientEventDispatcher.fireEventLater(PreSYMCPropertyViewer.this, IClientEvent.SS_VIEWER_SAVE_COMPLETE, 
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
