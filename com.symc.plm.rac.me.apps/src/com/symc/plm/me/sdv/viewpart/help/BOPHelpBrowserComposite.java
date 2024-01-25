/**
 * 
 */
package com.symc.plm.me.sdv.viewpart.help;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import com.teamcenter.rac.aif.kernel.IPreferenceService;
import com.teamcenter.rac.aifrcp.AbstractWelcomeComposite;
import com.teamcenter.rac.aifrcp.AifrcpPlugin;
//[2024.01.25]수정
//import com.teamcenter.rac.aifrcp.BrowserComposite;
import com.teamcenter.rac.webbrowser.BrowserComposite;
//import com.teamcenter.rac.aifrcp.registry;
import com.teamcenter.rac.util.OSGIUtil;
import com.teamcenter.rac.util.Registry;

public class BOPHelpBrowserComposite extends AbstractWelcomeComposite {
    private static Registry registry = Registry.getRegistry(BrowserComposite.class);
    private static Registry registry_custom = Registry.getRegistry(BOPHelpBrowserComposite.class);
    private static String m_defaultHomeUrl = registry_custom.getString("BrowserView.homeUrl");
    private String m_homeUrl;
    private Browser browser;

    public BOPHelpBrowserComposite(Composite paramComposite) {
        this(paramComposite, null);
    }

    public BOPHelpBrowserComposite(Composite paramComposite, String paramString) {
        this.m_homeUrl = m_defaultHomeUrl;
        if (paramString != null)
            setHomeUrl(paramString);
        else
            setHomeUrl(getHomeUrlPreference());
        initialize(paramComposite);
    }

    public void initialize(Composite paramComposite) {
        paramComposite.setLayout(new FormLayout());
        Composite localComposite = new Composite(paramComposite, 8388608);
        FormData localFormData = new FormData();
        localFormData.top = new FormAttachment(0, 0);
        localFormData.left = new FormAttachment(0, 0);
        localFormData.right = new FormAttachment(100, 0);
        localComposite.setLayoutData(localFormData);
        Label localLabel = new Label(paramComposite, 0);
        localFormData = new FormData();
        localFormData.left = new FormAttachment(0, 0);
        localFormData.right = new FormAttachment(100, 0);
        localFormData.bottom = new FormAttachment(100, 0);
        localLabel.setLayoutData(localFormData);
        this.browser = new Browser(paramComposite, 2048);
        localFormData = new FormData();
        localFormData.top = new FormAttachment(localComposite);
        localFormData.bottom = new FormAttachment(localLabel);
        localFormData.left = new FormAttachment(0, 0);
        localFormData.right = new FormAttachment(100, 0);
        this.browser.setLayoutData(localFormData);
        localComposite.setLayout(new GridLayout(7, false));
        this.browser.setUrl(this.m_homeUrl);

        ToolBar localToolBar = new ToolBar(localComposite, 8388608);
        ToolItem localToolItem = new ToolItem(localToolBar, 0);
        ImageDescriptor localImageDescriptor = AifrcpPlugin.getDefault().getImageDescriptor(registry.getString("BrowserView.homeIcon"));
        Image localImage = localImageDescriptor.createImage();
        localToolItem.setImage(localImage);
        localToolItem.setToolTipText(registry.getString("BrowserView.homeToolTip"));
        localToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                BOPHelpBrowserComposite.this.browser.setUrl(BOPHelpBrowserComposite.this.m_homeUrl);
            }
        });
        localToolItem = new ToolItem(localToolBar, 0);
        localImageDescriptor = AifrcpPlugin.getDefault().getImageDescriptor(registry.getString("BrowserView.backIcon"));
        localImage = localImageDescriptor.createImage();
        localToolItem.setImage(localImage);
        localToolItem.setToolTipText(registry.getString("BrowserView.backToolTip"));
        localToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                BOPHelpBrowserComposite.this.browser.back();
            }
        });
        localToolItem = new ToolItem(localToolBar, 0);
        localImageDescriptor = AifrcpPlugin.getDefault().getImageDescriptor(registry.getString("BrowserView.forwardIcon"));
        localImage = localImageDescriptor.createImage();
        localToolItem.setImage(localImage);
        localToolItem.setToolTipText(registry.getString("BrowserView.forwardToolTip"));
        localToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                BOPHelpBrowserComposite.this.browser.forward();
            }
        });
        localToolItem = new ToolItem(localToolBar, 0);
        localImageDescriptor = AifrcpPlugin.getDefault().getImageDescriptor(registry.getString("BrowserView.reloadIcon"));
        localImage = localImageDescriptor.createImage();
        localToolItem.setImage(localImage);
        localToolItem.setToolTipText(registry.getString("BrowserView.reloadToolTip"));
        localToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                BOPHelpBrowserComposite.this.browser.refresh();
            }
        });
        localToolItem = new ToolItem(localToolBar, 0);
        localImageDescriptor = AifrcpPlugin.getDefault().getImageDescriptor(registry.getString("BrowserView.abortedIcon"));
        localImage = localImageDescriptor.createImage();
        localToolItem.setImage(localImage);
        localToolItem.setToolTipText(registry.getString("BrowserView.abortedToolTip"));
        localToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                BOPHelpBrowserComposite.this.browser.stop();
            }
        });
        localToolItem = new ToolItem(localToolBar, 0);
        localImageDescriptor = AifrcpPlugin.getDefault().getImageDescriptor(registry.getString("BrowserView.printIcon"));
        localImage = localImageDescriptor.createImage();
        localToolItem.setImage(localImage);
        localToolItem.setToolTipText(registry.getString("BrowserView.printToolTip"));
        localToolItem.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent paramSelectionEvent) {
                String str = registry.getString("BrowserView.printCmd");
                BOPHelpBrowserComposite.this.browser.execute(str);
            }
        });
    }

    public void setFocus() {
        this.browser.setFocus();
    }

    public void setUrl(String paramString) {
        this.browser.setUrl(paramString);
    }

    private String getHomeUrlPreference() {
        IPreferenceService localIPreferenceService = (IPreferenceService) OSGIUtil.getService(AifrcpPlugin.getDefault(), IPreferenceService.class.getName());
        // String str = localIPreferenceService.getString(0, "BOPHelp_Browser_Home_URL", m_defaultHomeUrl);
        String str = localIPreferenceService.getStringValue("BOPHelp_Browser_Home_URL");
        if (str == null) {
            str = m_defaultHomeUrl;
        }
        
        return str;
    }

    public void setHomeUrl(String paramString) {
        this.m_homeUrl = paramString;
    }

    public Browser getBrowser() {
        return this.browser;
    }
    
}
