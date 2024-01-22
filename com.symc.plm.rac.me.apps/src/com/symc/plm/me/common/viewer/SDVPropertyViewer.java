package com.symc.plm.me.common.viewer;

import java.util.Arrays;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISaveablePart;

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
 * 
 */
public class SDVPropertyViewer extends AbstractSwtSubViewer implements ISaveablePart, InterfaceAIFComponentEventListener {

	private Registry registry;

	private TCComponent targetComp;
	private Composite m_composite;
	private AbstractSDVViewer sdvViewer;

	public SDVPropertyViewer(Composite parent) {
		registry = Registry.getRegistry(this);

		GridLayout parentLayout = SWTUIUtilities.tightGridLayout(1);
		Composite mainComposite = new Composite(parent, SWT.NONE);
		mainComposite.setLayout(parentLayout);

		m_composite = new Composite(mainComposite, SWT.NONE);
		m_composite.setLayout(new FillLayout());
		m_composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		CheckInOutComposite m_cicoComposite = new CheckInOutComposite(mainComposite);
		m_cicoComposite.panelLoaded();

		AIFUtility.getDefaultSession().addAIFComponentEventListener(this);
	}

	@Override
	public void setInput(Object viewerInput) {
		TCComponentViewerInput input = (TCComponentViewerInput) AdapterUtil.getAdapter(viewerInput, TCComponentViewerInput.class);
		targetComp = (TCComponent) input.getViewableObj();

		String viewerKey = targetComp.getType() + ".COMPONENTVIEWER";
		sdvViewer = (AbstractSDVViewer) registry.newInstanceFor(viewerKey, m_composite);
		if (sdvViewer == null) {

		} else {
			sdvViewer.layout();
			sdvViewer.setComponent(targetComp);
		}

		super.setInput(targetComp);
	}

	@Override
	public void inputChanged(Object input, Object oldInput) {
		SDVPropertyViewerContentProvider cp = (SDVPropertyViewerContentProvider) getContentProvider();
		cp.inputChanged(this, oldInput, input);

		if (sdvViewer != null) {
			sdvViewer.setComponent(targetComp);
//			sdvViewer.load();
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

		if (sdvViewer != null) {
			sdvViewer.refresh();
		} else {
			m_composite.layout();

			try {
				if (targetComp.okToModify() && targetComp.isCheckedOut()) {
					if (sdvViewer != null) {
						sdvViewer.setEditable(true);
						sdvViewer.setControlReadWrite(sdvViewer);
					}
				} else {
					if (sdvViewer != null) {
						sdvViewer.setEditable(false);
						sdvViewer.setControlReadOnly(sdvViewer);
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
		if (sdvViewer != null) {
			return sdvViewer.isDirty();
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

			if (sdvViewer != null) {
				if (!sdvViewer.isSavable()) {
				} else {
					sdvViewer.save();
				}
			}
		} catch (Exception e) {
			MessageBox.post(e);
			ex2 = e;
		} finally {
			ClientEventDispatcher.fireEventLater(SDVPropertyViewer.this, IClientEvent.SS_VIEWER_SAVE_COMPLETE, 
					TCComponent.class, targetComp, Exception.class, ex2);
			
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
					// For property change event, don¡¯t need to refresh the whole panel
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
