package com.symc.plm.rac.prebom.common.viewer;

import org.eclipse.swt.widgets.Display;

import com.teamcenter.rac.common.tcviewer.factory.AbstractSWTViewerFactory;
import com.teamcenter.rac.util.viewer.IViewerEvent;
import com.teamcenter.rac.util.viewer.SubViewerListener;
import com.teamcenter.rac.util.viewer.ViewerEvent;

/**
 * 
 * Viewer Custom
 * 
 */
public class PreSYMCPropertyViewerFactory extends AbstractSWTViewerFactory {
	@Override
	@SuppressWarnings("rawtypes")
	public void loadViewer(final SubViewerListener listener) {
		Runnable runnable = new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					PreSYMCPropertyViewer viewer = new PreSYMCPropertyViewer(getParent());
					viewer.setContentProvider(new PreSYMCPropertyViewerContentProvider());
					listener.done(viewer);
				} catch (Exception e) {
					e.printStackTrace();
					ViewerEvent viewerError = new ViewerEvent(this, IViewerEvent.NOVIEWDATAFOUND);
					listener.error(viewerError);
				} finally {

				}
			}
		};
		Display.getDefault().asyncExec(runnable);
	}
}
