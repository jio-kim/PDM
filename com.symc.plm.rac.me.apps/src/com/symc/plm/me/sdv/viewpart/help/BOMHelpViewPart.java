package com.symc.plm.me.sdv.viewpart.help;

import org.eclipse.swt.browser.CloseWindowListener;
import org.eclipse.swt.browser.WindowEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.wb.swt.ResourceManager;

/**
 * 
 * Class Name : BOMHelpViewPart
 * Class Description :
 * @date 2014. 2. 3.
 * 
 */
public class BOMHelpViewPart extends ViewPart {

	public static final String PLUGIN_ID = "com.symc.plm.rac.me.apps";
	public static final String BOM_HELP_ICON = "icons/BOMHelp.png";

	private BOMHelpBrowserComposite child;

	public BOMHelpViewPart() {

	}

	public void createPartControl(Composite paramComposite) {
		setTitleImage(ResourceManager.getPluginImage(PLUGIN_ID, BOM_HELP_ICON));

		this.child = new BOMHelpBrowserComposite(paramComposite);
		this.child.getBrowser().addCloseWindowListener(new CloseWindowListener() {
			public void close(WindowEvent paramWindowEvent) {
				BOMHelpViewPart.this.closeView();
			}
		});
	}

	public void setFocus() {
		this.child.setFocus();
	}

	public void setUrl(String paramString) {
		this.child.setUrl(paramString);
	}

	public void setUrl(String paramString1, String paramString2) {
		this.child.setUrl(paramString1);
		if (paramString2 == null)
			return;
		setPartName(paramString2);
	}

	private void closeView() {
		Object localObject = (getSite() != null) ? getSite().getPage() : null;
		if (localObject == null)
			return;
	}
}
