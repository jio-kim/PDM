package com.symc.plm.me.common.viewer;

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
public class SDVPropertyViewerFactory extends AbstractSWTViewerFactory {
	@Override
	public void loadViewer(@SuppressWarnings("rawtypes") final SubViewerListener listener) {
		Runnable runnable = new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					SDVPropertyViewer viewer = new SDVPropertyViewer(getParent());
					viewer.setContentProvider(new SDVPropertyViewerContentProvider());
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
