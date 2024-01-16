// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.teamcenter.rac.pse.variants.sosvi;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.commands.paste.PasteCommand;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentFolder;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPropertyDescriptor;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.DialogIconPanel;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.MultiLineWrappedTextLabel;
import com.teamcenter.rac.util.Registry;

// Referenced classes of package com.teamcenter.rac.pse.variants.sosvi:
//            SelectedOptionSetDialog, SavedOptionSetSelectionPanel, SavedConfiguration, SelectedOptionSet

@SuppressWarnings({ "rawtypes", "serial", "unchecked" })
class SaveSelectedOptionSetDialog extends JDialog {
    private static class SaveMode {

        public String toString() {
            return mode;
        }

        private String mode;

        SaveMode(String s) {
            mode = s;
        }
    }

    private class AddToPanel extends JPanel {

        private JRadioButton newStuffFolder;
        private JRadioButton homeFolder;
        private JRadioButton moduleRev;
        private JComboBox relationSelection;

        AddToPanel() {
            super(new BorderLayout(0, 5));
            // setBorder(new EmptyBorder(10, 0, 0, 0));
            Registry registry = Registry.getRegistry(this);
            String s = registry.getString("saveConfig.addToPanel.label");

            // Project Code 추가
            JPanel prjPanel = new JPanel(new BorderLayout(0, 0));
            prjPanel.add(new Label("Project Code"), BorderLayout.NORTH);
            projectNameCombo.addItem("");
            SYMCRemoteUtil remote = new SYMCRemoteUtil();
            try {
                DataSet ds = new DataSet();
                ArrayList<String> list = (ArrayList<String>) remote.execute("com.ssangyong.service.VariantService", "getProjectCodes", ds);
                if (list != null) {
                    for (String prjectName : list) {
                        projectNameCombo.addItem(prjectName);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            prjPanel.add(projectNameCombo, BorderLayout.CENTER);
            add(prjPanel, BorderLayout.NORTH);

            if (canSaveInModule)
                s = (new StringBuilder(String.valueOf(s))).append(registry.getString("saveConfig.addToPanel.explanation")).toString();
            MultiLineWrappedTextLabel multilinewrappedtextlabel = new MultiLineWrappedTextLabel(s);
            add(multilinewrappedtextlabel, BorderLayout.CENTER);// North
            newStuffFolder = new JRadioButton(registry.getString("saveConfig.newStuffFolder"));
            homeFolder = new JRadioButton(registry.getString("saveConfig.homeFolder"));
            canSaveInModule = false;
            try {
                TCPreferenceService tcpreferenceservice;
                if (sosDialog.getBOMLine() != null)
                    tcpreferenceservice = sosDialog.getBOMLine().getSession().getPreferenceService();
                else
                    tcpreferenceservice = ((TCSession) sosDialog.getApplication().getSession()).getPreferenceService();
                // String as[] = tcpreferenceservice.getStringArray(0, "PSESavedConfigRelationTypes");
                String as[] = tcpreferenceservice.getStringValues("PSESavedConfigRelationTypes");

                canSaveInModule = as.length > 0;
                relationSelection = new JComboBox();
                String as1[] = getDisplayNamesForRelations(as);
                String as2[];
                int j = (as2 = as1).length;
                for (int i = 0; i < j; i++) {
                    String s1 = as2[i];
                    relationSelection.addItem(s1);
                }

            } catch (Exception exception) {
                MessageBox.post(exception);
            }
            ButtonGroup buttongroup = new ButtonGroup();
            if (canSaveInModule) {
                moduleRev = new JRadioButton(registry.getString("saveConfig.moduleRev"));
                buttongroup.add(moduleRev);
            }
            buttongroup.add(homeFolder);
            buttongroup.add(newStuffFolder);
            newStuffFolder.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent) {
                    relationSelection.setEnabled(false);
                    saveMode = SaveSelectedOptionSetDialog.NEWSTUFF;
                }

            });
            homeFolder.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent) {
                    relationSelection.setEnabled(false);
                    saveMode = SaveSelectedOptionSetDialog.HOMEFOLDER;
                }

            });
            JPanel jpanel = new JPanel(new GridLayout(0, 1, 0, 1));
            if (canSaveInModule) {
                moduleRev.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent actionevent) {
                        relationSelection.setEnabled(true);
                        saveMode = SaveSelectedOptionSetDialog.MODULE;
                        saveRelationType = (String) m_relationTypeNames.get(relationSelection.getSelectedItem().toString());
                    }
                });
                relationSelection.addItemListener(new ItemListener() {

                    public void itemStateChanged(ItemEvent itemevent) {
                        if (itemevent.getStateChange() == 2)
                            saveRelationType = null;
                        else
                            saveRelationType = (String) m_relationTypeNames.get(itemevent.getItem().toString());
                    }
                });
                JPanel jpanel1 = new JPanel(new BorderLayout(2, 0));
                jpanel1.add(moduleRev, "West");
                jpanel1.add(relationSelection, "Center");
                jpanel.add(jpanel1);
            }
            jpanel.add(homeFolder);
            jpanel.add(newStuffFolder);
            if (canSaveInModule) {
                moduleRev.doClick();
            } else {
                newStuffFolder.setSelected(true);
                saveMode = SaveSelectedOptionSetDialog.NEWSTUFF;
            }
            add(jpanel, BorderLayout.SOUTH);// "Center"
        }

        @SuppressWarnings("unused")
        JComboBox<String> gerPrjectNames() {
            return projectNameCombo;
        }
    }

    SaveSelectedOptionSetDialog(SelectedOptionSetDialog selectedoptionsetdialog) throws TCException {
        super(selectedoptionsetdialog, true);
        m_relationTypeNames = new HashMap();
        sosDialog = selectedoptionsetdialog;
        Registry registry = Registry.getRegistry(this);
        Container container = getContentPane();
        saveMode = new SaveMode("Save Mode not set");
        String s = registry.getString("saveConfig.titleLeader");
        setTitle((new StringBuilder(String.valueOf(s))).append(" ").append(selectedoptionsetdialog.getBOMLine().getItemRevision().toString()).toString());
        container.add(new DialogIconPanel(registry.getImageIcon("sos.ICON")), "North");
        selectionPanel = new SavedOptionSetSelectionPanel(selectedoptionsetdialog, registry.getString("saveConfig.label"), true);
        JPanel jpanel = new JPanel(new BorderLayout(0, 5));
        jpanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        jpanel.add(selectionPanel, "Center");
        jpanel.add(new AddToPanel(), "South");
        container.add(jpanel, "Center");
        JPanel jpanel1 = new JPanel(new GridLayout(1, 0, 5, 0));
        JButton jbutton = new JButton(registry.getString("saveConfig.ok"));
        jbutton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                try {
                    saveSos();
                } catch (TCException tcexception) {
                    MessageBox.post(tcexception, true);
                    return;
                }
                setVisible(false);
                dispose();
            }
        });
        jpanel1.add(jbutton);
        JButton jbutton1 = new JButton(registry.getString("saveConfig.cancel"));
        jbutton1.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                setVisible(false);
                dispose();
            }
        });
        jpanel1.add(jbutton1);
        JPanel jpanel2 = new JPanel(new GridBagLayout());
        jpanel2.setBorder(new EmptyBorder(0, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE));
        jpanel2.add(jpanel1);
        container.add(jpanel2, "South");
    }

    private void saveSos() throws TCException {
        if (projectNameCombo != null
                && (projectNameCombo.getSelectedIndex() < 0 || projectNameCombo.getSelectedItem() == null || projectNameCombo.getSelectedItem().equals(""))) {
            throw new TCException("select a Project Code");
        }

        SavedConfiguration savedconfiguration = selectionPanel.getSelectedSavedConfig();
        if (savedconfiguration != null) {
            TCComponent tccomponent = savedconfiguration.getComponent();
            if (tccomponent.getTypeComponent().isTypeOf("StoredOptionSet")) {
                // User SOS 저장시에 Project Code 저장.
                String projectCode = (String) projectNameCombo.getSelectedItem();
                if (tccomponent != null) {
                    tccomponent.setProperty("s7_PROJECT_CODE", projectCode);
//                    tccomponent.save();
                }
                sosDialog.getSOS().overwriteSavedOptionSet(tccomponent, selectionPanel.getSosDescription());
                return;
            }
        }
        if (saveMode == NEWSTUFF)
            saveSosToNewStuffFolder();
        else if (saveMode == HOMEFOLDER)
            saveSosToHomeFolder();
        else if (saveMode == MODULE)
            saveSosToModule();
        else
            throw new TCException((new StringBuilder("Unsupported SOS save mode: ")).append(saveMode).toString());
        sosDialog.getSOS().setName(selectionPanel.getSosName());
        sosDialog.getSOS().setModified(false);
    }

    private void saveSosToNewStuffFolder() throws TCException {
        TCComponentFolder tccomponentfolder;
        TCSession session = null;
        // if (sosDialog.getBOMLine() != null)
        // tccomponentfolder =
        // TCComponentFolder.getNewStuffFolder(sosDialog.getBOMLine().getSession());
        // else
        // tccomponentfolder = TCComponentFolder.getNewStuffFolder((TCSession)
        // sosDialog.getApplication().getSession());
        if (sosDialog.getBOMLine() != null)
            session = sosDialog.getBOMLine().getSession();
        else
            session = (TCSession) sosDialog.getApplication().getSession();

        tccomponentfolder = session.getUser().getNewStuffFolder();
        saveSosToFolder(tccomponentfolder);
    }

    private void saveSosToHomeFolder() throws TCException {
        TCComponentFolder tccomponentfolder;
        TCSession session = null;
        // if (sosDialog.getBOMLine() != null)
        // tccomponentfolder =
        // TCComponentFolder.getHomeFolder(sosDialog.getBOMLine().getSession());
        // else
        // tccomponentfolder = TCComponentFolder.getHomeFolder((TCSession)
        // sosDialog.getApplication().getSession());
        if (sosDialog.getBOMLine() != null)
            session = sosDialog.getBOMLine().getSession();
        else
            session = (TCSession) sosDialog.getApplication().getSession();

        tccomponentfolder = session.getUser().getHomeFolder();
        saveSosToFolder(tccomponentfolder);
    }

    private void saveSosToFolder(TCComponentFolder tccomponentfolder) throws TCException {

        TCComponent tccomponent = createAndSaveDbSos();

        // User SOS 저장시에 Project Code 저장.
        String projectCode = (String) projectNameCombo.getSelectedItem();
        if (tccomponent != null) {
            tccomponent.setProperty("s7_PROJECT_CODE", projectCode);
            tccomponent.save();
        }

        PasteCommand pastecommand = new PasteCommand(new TCComponent[] { tccomponent }, new InterfaceAIFComponent[] { tccomponentfolder }, Boolean.FALSE);
        pastecommand.executeModeless();
    }

    private void saveSosToModule() throws TCException {
        if (saveRelationType == null) {
            throw new TCException("Cannot save SOS without a relation type");
        } else {
            TCComponent tccomponent = createAndSaveDbSos();

            // User SOS 저장시에 Project Code 저장.
            String projectCode = (String) projectNameCombo.getSelectedItem();
            if (tccomponent != null) {
                tccomponent.setProperty("s7_PROJECT_CODE", projectCode);
                tccomponent.save();
            }

            TCComponentItemRevision tccomponentitemrevision = sosDialog.getBOMLine().getItemRevision();
            tccomponentitemrevision.add(saveRelationType, tccomponent);
            return;
        }
    }

    private TCComponent createAndSaveDbSos() throws TCException {
        TCComponent tccomponent = sosDialog.getSOS().createSavedOptionSet(selectionPanel.getSosName(), selectionPanel.getSosDescription(), true);
        return tccomponent;
    }

    private String[] getDisplayNamesForRelations(String as[]) {
        String as1[] = null;
        try {
            TCComponentItemRevision tccomponentitemrevision = sosDialog.getBOMLine().getItemRevision();
            TCPropertyDescriptor atcpropertydescriptor[] = tccomponentitemrevision.getPasteRelations();
            if (atcpropertydescriptor != null && atcpropertydescriptor.length != 0) {
                int i = atcpropertydescriptor.length;
                ArrayList arraylist = new ArrayList(0);
                for (int j = 0; j < i; j++)
                    if (as.length > 0) {
                        for (int k = 0; k < as.length; k++) {
                            String s = atcpropertydescriptor[j].getName();
                            String s1 = atcpropertydescriptor[j].getDisplayName();
                            if (!as[k].equals(s) && !as[k].equals(s1))
                                continue;
                            m_relationTypeNames.put(s1, s);
                            arraylist.add(s1);
                            break;
                        }

                    } else {
                        m_relationTypeNames.put(atcpropertydescriptor[j].getDisplayName(), atcpropertydescriptor[j].getName());
                        arraylist.add(atcpropertydescriptor[j].getDisplayName());
                    }

                as1 = (String[]) arraylist.toArray(new String[0]);
            }
        } catch (TCException tcexception) {
            logger.error(tcexception.getClass().getName(), tcexception);
        }
        return as1;
    }

    private static final Logger logger = Logger.getLogger(com.teamcenter.rac.pse.variants.sosvi.SaveSelectedOptionSetDialog.class);
    private static final int MARGIN_SIZE = 15;
    private static final SaveMode NEWSTUFF = new SaveMode("newstuff");
    private static final SaveMode HOMEFOLDER = new SaveMode("home");
    private static final SaveMode MODULE = new SaveMode("module");
    private boolean canSaveInModule;
    protected HashMap<String, String> m_relationTypeNames;
    private SelectedOptionSetDialog sosDialog;
    private SavedOptionSetSelectionPanel selectionPanel;
    private SaveMode saveMode;
    private String saveRelationType;
    private JComboBox<String> projectNameCombo = new JComboBox<String>();

}
