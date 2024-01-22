package com.symc.plm.rac.prebom.ccn.rendering;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.symc.plm.rac.prebom.ccn.commands.dao.CustomCCNDao;
import com.symc.plm.rac.prebom.ccn.view.PreCCNEPLInfoPanel;
import com.symc.plm.rac.prebom.ccn.view.PreCCNInfoPanel;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.symc.plm.rac.prebom.common.viewer.AbstractPreSYMCViewer;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class PreCCNRevisionRendering extends AbstractPreSYMCViewer{

    private TabFolder tabFolder;
    private PreCCNInfoPanel preCCNInfoRender;
    private PreCCNEPLInfoPanel preCCNEPLInfoRender;
    
    private Registry registry;
    
    public PreCCNRevisionRendering(Composite parent) {
         super(parent);
    }

    @Override
    public void load() {
        if(targetComp != null) {
            try {
                preCCNInfoRender.setProperties();
                if (null != preCCNEPLInfoRender) {
                    preCCNEPLInfoRender.load();
                }
            } catch (Exception e) {
                MessageBox.post(getShell(), "EPL Generation Failed." + "\nPlease try again or contact administrator.", "CCN EPL", MessageBox.ERROR);
                e.printStackTrace();
            }
        }
    }
    

    @Override
    public void save() {
        preCCNInfoRender.save();
    }

    // 체크인 눌렀을때 진행
    @Override
    public boolean isSavable() {
        return preCCNInfoRender.validationCheck();
    }

    
    @Override
    public void createPanel(Composite parent) {
        try {
            registry = Registry.getRegistry(this);
            tabFolder = new TabFolder(parent, SWT.NONE);
            
            TabItem ccnInfo = new TabItem(tabFolder, SWT.NONE);
            ccnInfo.setText(registry.getString("PreCCNRevisionRendering.CCN.A_Page"));
            
            ScrolledComposite sc = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
            preCCNInfoRender = new PreCCNInfoPanel(sc);
            Composite composite = preCCNInfoRender.getComposite();
            sc.setContent(composite);
            sc.setMinSize(380, 480);
            sc.setExpandHorizontal(true);
            sc.setExpandVertical(true);
            ccnInfo.setControl(sc);
            preCCNInfoRender.load();
            
            TabItem ccnEPL = new TabItem(tabFolder, SWT.NONE);
            
            ccnEPL.setText(registry.getString("PreCCNRevisionRendering.CCN.B_Page"));
            
            InterfaceAIFComponent[] comps = SDVPreBOMUtilities.getTargets();
            TCComponentItemRevision ccnRevision = (TCComponentItemRevision) comps[0]; 
            String ccnId = ccnRevision.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
            
            ArrayList<HashMap< String, Object>> resultList = selectMasterSystemCode(ccnId);
            String sysCodes = "";
            if (null != resultList) {
                for (int i = 0; i < resultList.size(); i++) {
                    if (null == resultList.get(i)) {
                        continue;
                    }
                    sysCodes += (String) resultList.get(i).get("MASTER_LIST_SYSCODE");
                    if ((i + 1) == resultList.size()) {
                        break;
                    }
                    sysCodes += ", ";
                }
            }
            
            preCCNInfoRender.sysCodesTxt.setText(sysCodes.toString());
            // Add Tab Folder Listener
            addTabFolderListener();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void addTabFolderListener() {
        tabFolder.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent event) {
                if ("CCN EPL".equals(tabFolder.getSelection()[0].getText())) {
                    if (tabFolder.getSelection()[0].getControl() == null) {
                        ScrolledComposite ccnEPLScrolled = new ScrolledComposite(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
                        preCCNEPLInfoRender = new PreCCNEPLInfoPanel(ccnEPLScrolled);
//                        preCCNEPLInfoRender.setSize(2150, 1400);
                        preCCNEPLInfoRender.setEditable(isEditable());
                        ccnEPLScrolled.setContent(preCCNEPLInfoRender);
                        ccnEPLScrolled.setExpandHorizontal(false);
                        ccnEPLScrolled.setExpandVertical(false);
                        tabFolder.getSelection()[0].setControl(ccnEPLScrolled);
                        preCCNEPLInfoRender.load();
                        }
                    }
            }
        });
    }
    
    private ArrayList<HashMap< String, Object>> selectMasterSystemCode(String ccnId) {                                                
        CustomCCNDao dao = null;
        ArrayList<HashMap< String, Object>> resultList = new ArrayList<HashMap< String, Object>>();
        try {
            dao = new CustomCCNDao();
            resultList = dao.selectMasterSystemCode(ccnId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultList;
    }

}
