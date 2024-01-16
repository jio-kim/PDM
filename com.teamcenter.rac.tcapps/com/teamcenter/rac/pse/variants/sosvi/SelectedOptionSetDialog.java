// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.teamcenter.rac.pse.variants.sosvi;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;

import javax.swing.event.TreeSelectionEvent;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.common.DialogForBOMLine;
import com.teamcenter.rac.common.handlers.AbstractToggleTCPreferenceHandler;
import com.teamcenter.rac.handlers.Messages;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.common.BOMLineNode;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.variants.FilterUnconfiguredDueToClassicVariantsCheckBox;
import com.teamcenter.rac.pse.variants.IVariantRuleDialog;
import com.teamcenter.rac.pse.variants.VariantDialogHelper;
import com.teamcenter.rac.psebase.AbstractBOMLineViewerApplication;
import com.teamcenter.rac.util.ConfirmDialog;
import com.teamcenter.rac.util.DialogIconPanel;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

// Referenced classes of package com.teamcenter.rac.pse.variants.sosvi:
//            SelectedOptionSet, SelectedOptionSetPanel, VariantItemRequirement

@SuppressWarnings("serial")
public class SelectedOptionSetDialog extends DialogForBOMLine implements IVariantRuleDialog {
    @SuppressWarnings("unused")
    private TCComponentBOMLine tccomponentbomline = null;
    private static final String OVE_MESSAGE_ERROR_LEVEL = "ove_message_error_level";
    private SelectedOptionSet sos;
    private SelectedOptionSetPanel sosPanel;
    private int oveMsgLevel;
    private boolean canCreateLegacyVis;
    private FilterUnconfiguredDueToClassicVariantsCheckBox m_filterUnconfiguredDueToClassicVariantsCheckBox;
    private static final int IGNORE_ALL_MSGS = 9999;

    // 멤버 변수 추가
    private boolean m_useNewVariantRuleDialog = false;

    private class SosWindowListener extends WindowAdapter {

        public void windowClosed(WindowEvent windowevent) {
            try {
                TCComponentBOMLine tccomponentbomline = getBOMLine();
                if (tccomponentbomline.isValid())
                    tccomponentbomline.window().setIntProperty(OVE_MESSAGE_ERROR_LEVEL, oveMsgLevel);
            } catch (TCException tcexception) {
                MessageBox.post(tcexception);
            }
        }
    }

    private class RemoveFromSelectionListener extends WindowAdapter {

        public void windowClosed(WindowEvent windowevent) {
            if (m_app != null && (m_app instanceof AbstractBOMLineViewerApplication)) {
                BOMTreeTable bomtreetable = ((AbstractBOMLineViewerApplication) m_app).getViewableTreeTable();
                if (bomtreetable != null)
                    bomtreetable.getTree().removeTreeSelectionListener(SelectedOptionSetDialog.this);
            }
        }
    }

    public SelectedOptionSetDialog(Frame frame, AbstractAIFUIApplication abstractaifuiapplication, TCComponentBOMLine tccomponentbomline) throws TCException {
        super(frame, abstractaifuiapplication, tccomponentbomline);
        m_useNewVariantRuleDialog = false;
        initialize(tccomponentbomline);
    }

    public SelectedOptionSetDialog(Frame frame, AbstractAIFUIApplication abstractaifuiapplication, TCComponentBOMLine tccomponentbomline, boolean flag)
            throws TCException {
        super(frame, abstractaifuiapplication, tccomponentbomline);
        m_useNewVariantRuleDialog = false;
        initialize(tccomponentbomline);
        m_useNewVariantRuleDialog = flag;
    }

    public SelectedOptionSetDialog(Frame frame, Dialog dialog, AbstractAIFUIApplication abstractaifuiapplication, TCComponentBOMLine tccomponentbomline)
            throws TCException {
        super(frame, dialog, abstractaifuiapplication, tccomponentbomline);
        m_useNewVariantRuleDialog = false;
        initialize(tccomponentbomline);
    }

    public SelectedOptionSetDialog(Frame frame, Dialog dialog, AbstractAIFUIApplication abstractaifuiapplication, TCComponentBOMLine tccomponentbomline,
            boolean flag) throws TCException {
        super(frame, dialog, abstractaifuiapplication, tccomponentbomline);
        m_useNewVariantRuleDialog = false;
        initialize(tccomponentbomline);
        m_useNewVariantRuleDialog = flag;
    }

    boolean useNewVariantRuleDialog() {
        return m_useNewVariantRuleDialog;
    }

    private void initialize(TCComponentBOMLine tccomponentbomline) throws TCException {
        Registry registry = Registry.getRegistry(this);
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        this.tccomponentbomline = tccomponentbomline;
        TCComponentBOMWindow tccomponentbomwindow = tccomponentbomline.window();
        oveMsgLevel = tccomponentbomwindow.getIntProperty(OVE_MESSAGE_ERROR_LEVEL);
        if (oveMsgLevel != IGNORE_ALL_MSGS)
            tccomponentbomwindow.setProperty(OVE_MESSAGE_ERROR_LEVEL, Integer.toString(IGNORE_ALL_MSGS));
        addWindowListener(new SosWindowListener());
        BOMTreeTable bomtreetable = ((AbstractBOMLineViewerApplication) getApplication()).getViewableTreeTable();
        if (bomtreetable != null)
            bomtreetable.getTree().addTreeSelectionListener(this);
        addWindowListener(new RemoveFromSelectionListener());
        sos = new SelectedOptionSet(this);
        TCPreferenceService tcpreferenceservice = tccomponentbomline.getSession().getPreferenceService();
//        canCreateLegacyVis = tcpreferenceservice.isTrue(4, "PSEAllowLegacyVICreation");
        Boolean flag = tcpreferenceservice.getLogicalValueAtLocation("PSEAllowLegacyVICreation", TCPreferenceLocation.OVERLAY_LOCATION);
        if (flag == null) {
            flag = false;
        }
        canCreateLegacyVis = flag;

        setDialogTitle(sos.getName(), sos.getModified());
        container.add(new DialogIconPanel(registry.getImageIcon("sos.ICON")), "North");
        sosPanel = new SelectedOptionSetPanel(this);
        container.add(sosPanel, "Center");
        toggleFilterUnconfiguredDueToClassicVariantsCheckBox();
    }

    public void expandIfRootLine() {
        com.teamcenter.rac.aif.kernel.InterfaceAIFComponent ainterfaceaifcomponent[] = Activator.getDefault().getSelectionMediatorService()
                .getTargetComponents();
        if (ainterfaceaifcomponent != null && ainterfaceaifcomponent.length > 0 && (ainterfaceaifcomponent[0] instanceof TCComponentBOMLine)) {
            TCComponentBOMLine tccomponentbomline = (TCComponentBOMLine) ainterfaceaifcomponent[0];
            if (!tccomponentbomline.isRoot())
                return;
        }
        AbstractAIFUIApplication abstractaifuiapplication = getApplication();
        TCComponentBOMLine tccomponentbomline1 = getBOMLine();
        if (tccomponentbomline1 != null) {
            TCSession tcsession = tccomponentbomline1.getSession();
            TCPreferenceService tcpreferenceservice = tcsession.getPreferenceService();
//            if (tcpreferenceservice.isTrue(4, "PSE_expand_on_open"))
//                abstractaifuiapplication.expand();
            Boolean flag = tcpreferenceservice.getLogicalValueAtLocation("PSE_expand_on_open", TCPreferenceLocation.OVERLAY_LOCATION);
            if (flag == null) {
                flag = false;
            }
            if (flag) {
                abstractaifuiapplication.expand();
            }
        }
    }

    public void enableFilterUnconfiguredDueToClassicVariantsCheckBox() {
        if (m_filterUnconfiguredDueToClassicVariantsCheckBox != null)
            m_filterUnconfiguredDueToClassicVariantsCheckBox.setEnabled(true);
    }

    public void toggleFilterUnconfiguredDueToClassicVariantsCheckBox() {
        if (m_filterUnconfiguredDueToClassicVariantsCheckBox != null)
            m_filterUnconfiguredDueToClassicVariantsCheckBox.toggleEnabled(sos.hasSetAnyLegacyOptionValue());
    }

    public void resetFilterUnconfiguredDueToClassicVariantsCheckBox() {
        if (m_filterUnconfiguredDueToClassicVariantsCheckBox != null)
            m_filterUnconfiguredDueToClassicVariantsCheckBox.clearSelected();
    }

    public void setFilterUnconfiguredDueToClassicVariantsMode() {
        if (m_filterUnconfiguredDueToClassicVariantsCheckBox != null) {
            SelectedOptionSet selectedoptionset = getSOS();
            if (selectedoptionset.hasSetAnyLegacyOptionValue())
                m_filterUnconfiguredDueToClassicVariantsCheckBox.setSelected();
            else
                m_filterUnconfiguredDueToClassicVariantsCheckBox.setSelected(false);
        }
    }

    public boolean isFilterUnconfiguredDueToClassicVariantsModeSet() {
        if (m_filterUnconfiguredDueToClassicVariantsCheckBox != null)
            return m_filterUnconfiguredDueToClassicVariantsCheckBox.isSelected();
        else
            return false;
    }

    void resetToDefault() {
        try {
            sos.reset();
        } catch (TCException tcexception) {
            MessageBox.post(tcexception, true);
        }
        refresh();
    }

    void refresh() {
        sosPanel.refresh();
        setDialogTitle(sos.getName(), sos.getModified());
    }

    SelectedOptionSet getSOS() {
        return sos;
    }

    public VariantItemRequirement getVIRequirement()
    {
        try {
            return new VariantItemRequirement(this, canCreateLegacyVis);
        } catch (TCException tcexception) {
            MessageBox.post(tcexception);
        }
        return null;
    }

    public void setDialogTitle(String s, boolean flag) {
        try {
            Registry registry = Registry.getRegistry(this);
            String s1 = (new StringBuilder(String.valueOf(registry.getString("sosDialog.titleLeader")))).append(" ").append(getBOMLine().getItemRevision())
                    .toString();
            if (s != null && s.length() > 0) {
                s1 = (new StringBuilder(String.valueOf(s1))).append(" ").append(registry.getString("separator")).append(" ").toString();
                s1 = (new StringBuilder(String.valueOf(s1))).append(s).toString();
                if (flag)
                    s1 = (new StringBuilder(String.valueOf(s1))).append(registry.getString("varRule.modified")).toString();
            }
            setTitle(s1);
        } catch (TCException tcexception) {
            MessageBox.post(tcexception);
        }
    }

    public void valueChanged(TreeSelectionEvent treeselectionevent) {
        try {
            AbstractAIFUIApplication abstractaifuiapplication = AIFUtility.getCurrentApplication();
            if (abstractaifuiapplication != null && (abstractaifuiapplication instanceof AbstractBOMLineViewerApplication)) {
                BOMTreeTable bomtreetable = ((AbstractBOMLineViewerApplication) abstractaifuiapplication).getViewableTreeTable();
                if (bomtreetable != null) {
                    BOMLineNode abomlinenode[] = bomtreetable.getSelectedBOMLineNodes();
                    if (abomlinenode != null)
                        if (abomlinenode.length == 1)
                            refreshForGivenBOMLine(abomlinenode[0].getBOMLine());
                        else if (abomlinenode.length > 1) {
                            Registry registry = Registry.getRegistry(this);
                            MessageBox.post(registry.getString("sosDialog.multipleSelectedLines"), registry.getString("InvalidSelection"), 1);
                        }
                }
            }
        } catch (TCException tcexception) {
            MessageBox.post(tcexception, true);
        }
    }

    protected void refreshForNewBOMLine() throws TCException {
        AbstractBOMLineViewerApplication abstractbomlineviewerapplication = (AbstractBOMLineViewerApplication) getApplication();
        if (abstractbomlineviewerapplication.isVariantConfigAtRootOnly()) {
            return;
        } else {
            setBOMLine(getTreeTableBOMLine());
            sos = new SelectedOptionSet(this);
            setDialogTitle(sos.getName(), sos.getModified());
            sosPanel.refreshForNewBOMLine();
            return;
        }
    }

    public void refreshForGivenBOMLine(TCComponentBOMLine tccomponentbomline) throws TCException {
        AbstractBOMLineViewerApplication abstractbomlineviewerapplication = (AbstractBOMLineViewerApplication) getApplication();
        if (abstractbomlineviewerapplication.isVariantConfigAtRootOnly()) {
            return;
        } else {
            setBOMLine(tccomponentbomline);
            sos = new SelectedOptionSet(this);
            setDialogTitle(sos.getName(), sos.getModified());
            sosPanel.refreshForNewBOMLine();
            return;
        }
    }

    public void setFilterUnconfiguredDueToClassicVariantsCheckBox(FilterUnconfiguredDueToClassicVariantsCheckBox filterunconfiguredduetoclassicvariantscheckbox) {
        m_filterUnconfiguredDueToClassicVariantsCheckBox = filterunconfiguredduetoclassicvariantscheckbox;
    }

    public boolean isBOMWinInClassicVariantConfigToLoadMode() {
        boolean flag = false;
        try {
            TCComponentBOMLine tccomponentbomline = getBOMLine();
            if (tccomponentbomline != null) {
                TCComponentBOMWindow tccomponentbomwindow = tccomponentbomline.window();
                if (tccomponentbomwindow != null)
                    flag = tccomponentbomwindow.getFilterUnconfiguredDueToClassicVariantsMode();
            }
        } catch (TCException tcexception) {
            MessageBox.post(tcexception);
        }
        return flag;
    }

    public void closeSignaled() {
        VariantDialogHelper.remove(this);
    }

    public boolean wasInvokedFromSetModelRoot() {
        return VariantDialogHelper.wasInvokedFromSetModelRoot(this);
    }

    public void reOpenBOM() {
        Job job = new Job(Messages.getString("ComponentOpenHelper.OpeningComponents")) {

            protected IStatus run(IProgressMonitor iprogressmonitor) {
                Shell shell = PlatformUI.getWorkbench().getWorkbenchWindows()[0].getShell();
                String s = Registry.getRegistry(this).getString("filterUnconfiguredDueToClassicVariants.TITLE");
                String s1 = Registry.getRegistry(this).getString("filterUnconfiguredDueToClassicVariantsInfo.MSG");
                if (2 == ConfirmDialog.prompt(shell, s, s1)) {
                    AbstractAIFUIApplication abstractaifuiapplication = AIFUtility.getCurrentApplication();
                    if (abstractaifuiapplication != null)
                        abstractaifuiapplication.open(getBOMLine());
                } else {
                    TCPreferenceService tcpreferenceservice = AbstractToggleTCPreferenceHandler.getPrefService();
//                    boolean flag = tcpreferenceservice.isTrue(1, "PSEEnableFilteringUnconfigdDueToClassicVariantsPref");
                    Boolean flag = tcpreferenceservice.getLogicalValueAtLocation("PSEEnableFilteringUnconfigdDueToClassicVariantsPref",
                            TCPreferenceLocation.USER_LOCATION);
                    if (flag == null) {
                        flag = false;
                    }

                    if (m_filterUnconfiguredDueToClassicVariantsCheckBox.isBOMWinInClassicVariantConfigToLoadMode())
                        Activator.getDefault().setToggleProperty("BOMWINDOW_IN_FILTERING_UNCONFIGURED_DUE_TO_CLASSIC_VARIANTS_MODE", true, this);
                    else
                        Activator.getDefault().setToggleProperty("BOMWINDOW_IN_FILTERING_UNCONFIGURED_DUE_TO_CLASSIC_VARIANTS_MODE", flag, this);
                }
                return Status.OK_STATUS;
            }

        };
        TCComponentBOMLine tccomponentbomline = getBOMLine();
        if (tccomponentbomline != null) {
            TCSession tcsession = tccomponentbomline.getSession();
            job.setRule(tcsession.getOperationJobRule());
            job.schedule();
        }
    }

    /**
     * 내부의 SosPanel을 가져오기 위해 추가.
     * 
     * @return
     */
    public SelectedOptionSetPanel getSosPanel() {
        return sosPanel;
    }

    /**
     * 옵션값을 변경.
     * 
     * @param value
     * @param row
     * @param column
     */
    public void setValue(Object value, int row, int column) {
        sosPanel.getSosTablePanel().setValueAt(value, row, column);
    }

    public void setValue(TCComponentBOMLine variantLine, HashMap<String, String> map) throws TCException {
        sosPanel.getSosTablePanel().setValue(map);
    }

}
