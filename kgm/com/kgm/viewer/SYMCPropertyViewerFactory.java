package com.kgm.viewer;

import org.eclipse.swt.widgets.Display;

import com.teamcenter.rac.common.tcviewer.factory.AbstractSWTViewerFactory;
import com.teamcenter.rac.util.viewer.SubViewerListener;

/**
 * 
 * Viewer Custom
 * 
 */
public class SYMCPropertyViewerFactory extends AbstractSWTViewerFactory {

	public void loadViewer(@SuppressWarnings("rawtypes") final SubViewerListener listener) {
		Runnable runnable = new Runnable() {
			@SuppressWarnings("unchecked")
			public void run() {
				try {
					SYMCPropertyViewer viewer = new SYMCPropertyViewer(getParent());
					viewer.setContentProvider(new SYMCPropertyViewerContentProvider());
					listener.done(viewer);
				} catch (Exception e) {
					e.printStackTrace();
					// ViewerEvent viewerError = new ViewerEvent(this, IViewerEvent.NOVIEWDATAFOUND);
					// listener.error(viewerError);
				} finally {

				}
			}
		};

		Display.getDefault().asyncExec(runnable);
	}

}