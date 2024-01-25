package com.symc.plm.me.sdv.operation.ps;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.common.Activator;
import com.teamcenter.rac.common.TCConstants;
import com.teamcenter.rac.kernel.ResourceMember;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentGroup;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTaskTemplate;
import com.teamcenter.rac.kernel.TCComponentTaskTemplateType;
import com.teamcenter.rac.kernel.TCComponentTaskType;
import com.teamcenter.rac.kernel.TCComponentType;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.FilterDocument;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.iTextArea;
import com.teamcenter.rac.util.iTextField;
import com.teamcenter.rac.util.combobox.iComboBox;
import com.teamcenter.rac.util.log.Debug;
import com.teamcenter.rac.workflow.commands.assignmentlist.AssignAllTasksPanel;
import com.teamcenter.rac.workflow.commands.newprocess.AbstractProcessDialog;
import com.teamcenter.rac.workflow.commands.newprocess.ITemplateFilter;
import com.teamcenter.rac.workflow.commands.newprocess.NewProcessOperation;
import com.teamcenter.rac.workflow.commands.newprocess.NoCustomFilteringRequiredException;
import com.teamcenter.rac.workflow.commands.newprocess.TemplateFilterService;
import com.teamcenter.rac.workflow.common.taskproperties.TaskAttachmentsPanel;
import com.teamcenter.rac.workflow.processdesigner.ProcessDesignerApplicationPanel;

public class SDVNewProcessDialog extends AbstractProcessDialog {
    private static final long serialVersionUID = 6099585580802051979L;

    private static final Logger logger = Logger.getLogger(SDVNewProcessDialog.class);
    public JPanel inputPanel;
    public JPanel templatePanel;
    public JLabel processNameLabel;
    public JLabel processDescLabel;
    public JLabel processDefinitionLabel;
    public JLabel processFilterLabel;
    public iTextField processNameTextField;
    public iTextArea processDescTextArea;
    public JScrollPane processDescScrollPane;
    public iComboBox processDefinitionComboBox;
    public ButtonGroup filterRadioButtonGroup;
    public JRadioButton allRadioButton;
    public JRadioButton assignedRadioButton;
    public JCheckBox switchOffUCCheckBox;
    public boolean currentSelection = true;
    public boolean switchOffUCTemplates = false;
    public boolean onlyAssigned = false;
    public JTabbedPane tabPanel;
    public TaskAttachmentsPanel attachmentsPanel;
    TCComponent[] attachments = null;
    public ProcessDesignerApplicationPanel processDesignerPanel;
    public ImageIcon attachmentTabIcon;
    public ImageIcon processDesignerTabIcon;
    public Vector<TCComponent> procListAll = new Vector<TCComponent>();
    public Vector<Object> procList = new Vector<Object>();
    public Vector<TCComponent> procListAssigned = new Vector<TCComponent>();
    public NewProcessOperation newProcessOp;
    public LoadProcDefsOperation loadOp;
    protected AssignAllTasksPanel assignPanel = null;
    public int radioButtonFlag = 1;
    public int curSelTemplateIndex = -1;
    public TCSession session = null;
    protected ResourceMember[] selResourceList = null;
    public InterfaceAIFComponent[] pasteTargets = null;
    public ITemplateFilter filterInstancer = null;
    static final int ALL = 1;
    static final int NONE = 2;
    static final int ASSIGNED = 3;
    public JCheckBox inheritTargetsCheckBox;
    TCComponent creatorTask = null;
    TCComponent[] creatorTaskTargets = null;
    boolean inheritTargetsEnabled = false;
    boolean inheritTargetsSelected = false;

    // template �̸��� ��ϵ� Preference Name
    private String templatePrefName;

    public SDVNewProcessDialog(Frame frame, SDVNewProcessCommand command, String templatePrefName) {
        super(frame, command);

        this.templatePrefName = templatePrefName;

        this.session = command.session;
        this.pasteTargets = command.targetArray;
        TCComponent localTCComponent = null;
        Boolean localBoolean = (Boolean) Activator.getDefault().getToggleProperty("subProcessToggleProperty");

        if ((localBoolean != null) && (localBoolean.booleanValue())) {
            InterfaceAIFComponent[] localObject = Activator.getDefault().getSelectionMediatorService().getTargetComponents();
            for (int i = 0; i < localObject.length; ++i) {
                if (!(localObject[i] instanceof TCComponentTask))
                    continue;
                localTCComponent = (TCComponent) localObject[i];
                break;
            }
        }
        Activator.getDefault().setToggleProperty("subProcessToggleProperty", false, this);
        if (localTCComponent != null) {
            this.creatorTask = localTCComponent;
            String localObject = null;
            TCPreferenceService localTCPreferenceService = null;
            if (this.session != null) {
                localTCPreferenceService = this.session.getPreferenceService();
//                localObject = localTCPreferenceService.getString(0, "EPM_multiple_processes_targets");
                localObject = localTCPreferenceService.getStringValue("EPM_multiple_processes_targets");
            }
            if ((localObject != null) && (((String) localObject).length() != 0) && (((String) localObject).trim().toLowerCase().equals("on"))) {
                this.inheritTargetsEnabled = true;
//                localObject = localTCPreferenceService.getString(0, "EPM_sub_process_target_inheritance");
                localObject = localTCPreferenceService.getStringValue("EPM_sub_process_target_inheritance");
                if ((localObject != null) && (((String) localObject).length() != 0) && (((String) localObject).trim().toLowerCase().equals("on")))
                    this.inheritTargetsSelected = true;
            }
        }
        if (SwingUtilities.isEventDispatchThread())
            initUI();
        else
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SDVNewProcessDialog.this.initUI();
                }
            });

    }

    private String[] createRenderIcons(List<? extends TCComponent> paramVector) {
        String[] arrayOfString1 = null;
        int i = paramVector.size();
        if (i > 0) {
            arrayOfString1 = new String[i];
            String[] arrayOfString2 = { "template_stage", "object_name" };
            String[][] arrayOfString = (String[][]) null;
            try {
                arrayOfString = TCComponentType.getPropertiesSet(paramVector, arrayOfString2);
                String str = null;
                for (int j = 0; j < i; ++j) {
                    str = arrayOfString[j][0];
                    if (str.equals("2")) {
                        arrayOfString1[j] = "blank";
                    } else {
                        if (!(str.equals("1")))
                            continue;
                        arrayOfString1[j] = "underconstruction";
                    }
                }
            } catch (Exception localException) {
                MessageBox localMessageBox = new MessageBox(localException);
                localMessageBox.setVisible(true);
            }
        }
        return arrayOfString1;
    }

    public void initUI() {
        super.initUI();
        Registry localRegistry = Registry.getRegistry(this);
        if (this.creatorTask == null)
            setTitle(localRegistry.getString("command.TITLE"));
        else
            setTitle(localRegistry.getString("subprocess_command.TITLE"));
        this.mainPanel.setLayout(new VerticalLayout());
        this.inputPanel = new JPanel(new PropertyLayout());
        this.templatePanel = new JPanel(new HorizontalLayout());
        this.dialogIcon.setIcon(localRegistry.getImageIcon("newProcess.ICON"));
        if (this.creatorTask == null)
            this.processNameLabel = new JLabel(localRegistry.getString("name"));
        else
            this.processNameLabel = new JLabel(localRegistry.getString("subprocess_name"));
        String str1 = TCSession.getServerEncodingName(this.session);
        int i = TCConstants.getDefaultMaxNameSize(this.session);
        FilterDocument localFilterDocument = new FilterDocument(i, str1);
        this.processNameTextField = new iTextField(localFilterDocument, "", 20, i, true, this.inputPanel);
        this.processNameTextField.setBorder(new EtchedBorder());
        this.processDescLabel = new JLabel(localRegistry.getString("description"));
        localFilterDocument = new FilterDocument(240, str1);
        this.processDescTextArea = new iTextArea(localFilterDocument, "", 3, 40, this.inputPanel);
        this.processDescTextArea.setLengthLimit(240);
        this.processDescTextArea.setLineWrap(true);
        this.processDescTextArea.setWrapStyleWord(true);
        this.processDescScrollPane = new JScrollPane(this.processDescTextArea);
        this.processDefinitionLabel = new JLabel(localRegistry.getString("processDefinition"));
        this.processDefinitionComboBox = new iComboBox();
        this.processDefinitionComboBox.setAutoCompleteSuggestive(false);
        this.processDefinitionComboBox.setMaximumRowCount(10);
        this.processDefinitionComboBox.getTextField().setColumns(32);
        this.processDefinitionComboBox.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent paramKeyEvent) {
                if (SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedItemCount() < 1)
                    return;
                Object localObject = SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedItem();
                if ((localObject == null) || (!(localObject instanceof TCComponentTaskTemplate)))
                    return;
                Registry localRegistry = Registry.getRegistry(this);
                TCComponentTaskTemplate localTCComponentTaskTemplate = (TCComponentTaskTemplate) localObject;
                int i = SDVNewProcessDialog.this.tabPanel.getSelectedIndex();
                String str = SDVNewProcessDialog.this.tabPanel.getTitleAt(i);
                if (str.equals(localRegistry.getString("process"))) {
                    SDVNewProcessDialog.this.processDesignerPanel.open(localTCComponentTaskTemplate);
                    SDVNewProcessDialog.this.processDesignerPanel.revalidate();
                } else {
                    if (!(str.equals(localRegistry.getString("assignAllTasks"))))
                        return;
                    int j = SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedIndex();
                    if (SDVNewProcessDialog.this.curSelTemplateIndex == j)
                        return;
                    SDVNewProcessDialog.this.curSelTemplateIndex = j;
                    SDVNewProcessDialog.this.assignPanel.open(localTCComponentTaskTemplate);
                }
            }
        });

        this.processDefinitionComboBox.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent paramActionEvent) {
                if (SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedItemCount() < 1)
                    return;
                Object localObject = SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedItem();
                if (!(localObject instanceof TCComponentTaskTemplate))
                    return;
                Registry localRegistry = Registry.getRegistry(this);
                TCComponentTaskTemplate localTCComponentTaskTemplate = (TCComponentTaskTemplate) localObject;
                int i = SDVNewProcessDialog.this.tabPanel.getSelectedIndex();
                String str = SDVNewProcessDialog.this.tabPanel.getTitleAt(i);
                if (str.equals(localRegistry.getString("process"))) {
                    SDVNewProcessDialog.this.processDesignerPanel.open(localTCComponentTaskTemplate);
                    SDVNewProcessDialog.this.processDesignerPanel.revalidate();
                } else {
                    if (!(str.equals(localRegistry.getString("assignAllTasks"))))
                        return;
                    int j = SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedIndex();
                    if (SDVNewProcessDialog.this.curSelTemplateIndex == j)
                        return;
                    SDVNewProcessDialog.this.curSelTemplateIndex = j;
                    SDVNewProcessDialog.this.assignPanel.open(localTCComponentTaskTemplate);
                }
            }
        });

        this.switchOffUCCheckBox = new JCheckBox(localRegistry.getString("uctemplates"));
        this.switchOffUCCheckBox.setSelected(false);
        this.switchOffUCCheckBox.setEnabled(false);
        boolean bool = false;
        try {
            TCComponentUser localTCComponentUser = this.session.getUser();
            bool = localTCComponentUser.getTCProperty("is_member_of_dba").getLogicalValue();
        } catch (Exception localException) {
            MessageBox.post(this.parentFrame, localException);
            return;
        }
        if (bool == true) {
            this.switchOffUCCheckBox.setSelected(true);
            this.switchOffUCCheckBox.setEnabled(true);
        } else {
            this.switchOffUCCheckBox.setVisible(false);
        }
        this.switchOffUCCheckBox.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent paramItemEvent) {
                if (paramItemEvent.getStateChange() == 1)
                    SDVNewProcessDialog.this.switchOffUCTemplates = false;
                else
                    SDVNewProcessDialog.this.switchOffUCTemplates = true;
                SDVNewProcessDialog.this.procListAll.clear();
                SDVNewProcessDialog.this.procList.clear();
                SDVNewProcessDialog.this.procListAssigned.clear();
                SDVNewProcessDialog.this.populateProcessDefList();
            }
        });
        this.processFilterLabel = new JLabel(localRegistry.getString("processFilter"));
        this.allRadioButton = new JRadioButton(localRegistry.getString("all"), this.currentSelection);
        this.allRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramActionEvent) {
                if (SDVNewProcessDialog.this.currentSelection)
                    return;
                SDVNewProcessDialog.this.populateProcessDefList();
                SDVNewProcessDialog.this.currentSelection = true;
            }
        });
        this.allRadioButton.setEnabled(false);
        this.assignedRadioButton = new JRadioButton(localRegistry.getString("assigned"), !(this.currentSelection));
        this.assignedRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent paramActionEvent) {
                if (SDVNewProcessDialog.this.currentSelection != true)
                    return;
                SDVNewProcessDialog.this.populateProcessDefList();
                SDVNewProcessDialog.this.currentSelection = false;
            }
        });
        this.assignedRadioButton.setEnabled(false);
        this.filterRadioButtonGroup = new ButtonGroup();
        this.filterRadioButtonGroup.add(this.allRadioButton);
        this.filterRadioButtonGroup.add(this.assignedRadioButton);
        this.tabPanel = new JTabbedPane();
        int j;
        int k;
        if (this.pasteTargets != null) {
            j = this.pasteTargets.length;
            this.attachments = new TCComponent[j];
            for (k = 0; k < j; ++k)
                this.attachments[k] = ((TCComponent) this.pasteTargets[k]);
        }
        this.attachmentsPanel = new TaskAttachmentsPanel(this.session, this.attachments, this.desktop.getCurrentApplication());
        this.attachmentsPanel.addAttachmentChangeListener(this);
        this.attachmentsPanel.setPreferredSize(new Dimension(350, 250));
        if (this.creatorTask != null) {
            this.inheritTargetsCheckBox = new JCheckBox(localRegistry.getString("inherit_targets"));
            this.inheritTargetsCheckBox.setEnabled(this.inheritTargetsEnabled);
            if (this.inheritTargetsEnabled) {
                this.inheritTargetsCheckBox.setSelected(this.inheritTargetsSelected);
                if (this.inheritTargetsSelected)
                    addInheritedTargets();
            }
            this.inheritTargetsCheckBox.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent paramItemEvent) {
                    if (paramItemEvent.getStateChange() == 1) {
                        SDVNewProcessDialog.this.inheritTargetsSelected = true;
                        SDVNewProcessDialog.this.addInheritedTargets();
                    } else {
                        SDVNewProcessDialog.this.inheritTargetsSelected = false;
                        SDVNewProcessDialog.this.removeInheritedTargets();
                    }
                    if ((!(SDVNewProcessDialog.this.assignedRadioButton.isSelected())) && (!(SDVNewProcessDialog.this.onlyAssigned)))
                        return;
                    SDVNewProcessDialog.this.populateProcessDefList();
                }
            });
        }
        this.processDesignerPanel = new ProcessDesignerApplicationPanel(this.session);
        this.processDesignerPanel.initializeDisplay();
        this.attachmentTabIcon = localRegistry.getImageIcon("attachments.ICON");
        this.processDesignerTabIcon = localRegistry.getImageIcon("process.ICON");
        this.tabPanel.addTab(localRegistry.getString("attachments"), this.attachmentTabIcon, this.attachmentsPanel);
        this.tabPanel.addTab(localRegistry.getString("process"), this.processDesignerTabIcon, this.processDesignerPanel);
        this.assignPanel = new AssignAllTasksPanel(this.session);
        if (!(this.inheritTargetsSelected)) {
            this.assignPanel.setTargetObjects(this.attachments);
        } else {
            j = 0;
            k = 0;
            if (this.attachments != null)
                j += this.attachments.length;
            if (this.creatorTaskTargets != null)
                j += this.creatorTaskTargets.length;
            TCComponent[] arrayOfTCComponent = new TCComponent[j];
            int l;
            if (this.attachments != null)
                for (l = 0; l < this.attachments.length; ++l)
                    arrayOfTCComponent[(k++)] = this.attachments[l];
            if (this.creatorTaskTargets != null)
                for (l = 0; l < this.creatorTaskTargets.length; ++l)
                    arrayOfTCComponent[(k++)] = this.creatorTaskTargets[l];
            this.assignPanel.setTargetObjects(arrayOfTCComponent);
        }
        this.tabPanel.addTab(localRegistry.getString("assignAllTasks"), this.assignPanel);
        this.tabPanel.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent paramChangeEvent) {
                Registry localRegistry = Registry.getRegistry(this);
                int i = ((JTabbedPane) paramChangeEvent.getSource()).getSelectedIndex();
                String str = SDVNewProcessDialog.this.tabPanel.getTitleAt(i);
                if (SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedItemCount() < 1)
                    return;
                Object localObject = SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedItem();
                if (!(localObject instanceof TCComponentTaskTemplate))
                    return;
                TCComponentTaskTemplate localTCComponentTaskTemplate = (TCComponentTaskTemplate) localObject;
                if (str.equals(localRegistry.getString("process"))) {
                    SDVNewProcessDialog.this.processDesignerPanel.open(localTCComponentTaskTemplate);
                    SDVNewProcessDialog.this.processDesignerPanel.revalidate();
                } else {
                    if (!(str.equals(localRegistry.getString("assignAllTasks"))))
                        return;
                    int j = SDVNewProcessDialog.this.processDefinitionComboBox.getSelectedIndex();
                    if (SDVNewProcessDialog.this.curSelTemplateIndex == j)
                        return;
                    SDVNewProcessDialog.this.curSelTemplateIndex = j;
                    SDVNewProcessDialog.this.assignPanel.open(localTCComponentTaskTemplate);
                }
            }
        });
        this.inputPanel.add("1.1.right.top.preferred.preferred", this.processNameLabel);
        this.inputPanel.add("1.2.center.center.preferred.preferred", this.processNameTextField);
        this.inputPanel.add("2.1.right.top.preferred.preferred", this.processDescLabel);
        this.inputPanel.add("2.2.center.center.preferred.preferred", this.processDescScrollPane);
        this.inputPanel.add("3.1.right.top.preferred.preferred", this.processDefinitionLabel);
        this.inputPanel.add("3.2.center.center", this.processDefinitionComboBox);
        this.templatePanel.add("left.bind.center.center", this.switchOffUCCheckBox);
        this.templatePanel.add("right.bind.center.center", this.assignedRadioButton);
        this.templatePanel.add("right.bind.center.center", this.allRadioButton);
        this.templatePanel.add("right.bind.center.center", this.processFilterLabel);
        this.applyButton.setVisible(false);
        this.mainPanel.add("top.bind.center.center", this.inputPanel);
        this.mainPanel.add("top.bind.center.center", this.templatePanel);
        if (this.creatorTask != null)
            this.mainPanel.add("top.bind.center.center", this.inheritTargetsCheckBox);
        this.mainPanel.add("top.bind", new Separator());
        this.mainPanel.add("unbound.bind.center.top", this.tabPanel);
        this.mainPanel.setPreferredSize(new Dimension(750, 650));
        setMinimumSize(new Dimension(600, 600));
        setModal(false);
        pack();
        centerToScreen(1.1D, 1.0D);
        TCComponent localTCComponent = null;
        if (this.pasteTargets != null)
            localTCComponent = (TCComponent) this.pasteTargets[0];
        else if ((this.creatorTaskTargets != null) && (this.creatorTaskTargets.length > 0))
            localTCComponent = this.creatorTaskTargets[0];
        if (localTCComponent != null) {
            String str2 = localTCComponent.toString();
            if (str2.getBytes().length > i)
                str2 = new String(str2.getBytes(), 0, i);
            this.processNameTextField.setText(str2);
        }
        startLoadProcDefsOperation();
    }

    public void stopPressed() {
    }

    public void setPerformable(boolean paramBoolean) {
        this.okButton.setEnabled(paramBoolean);
    }

    public boolean isPerformable() {
        boolean i = false;
        if ((this.processNameTextField.getText().length() > 0) && (this.processDefinitionComboBox.isEnabled()) && (this.processDefinitionComboBox.getSelectedObject() != null))
            i = true;
        return i;
    }

    public boolean showCloseButton() {
        return false;
    }

    public void startCommandOperation() {
        Registry localRegistry = Registry.getRegistry(this);
        try {
            this.selResourceList = this.assignPanel.getSelectedResources();
            this.assignPanel.saveModifyAssignmentList();
            this.newProcessOp = ((NewProcessOperation) localRegistry.newInstanceFor("newProcessOperation", new Object[] { this }));
            this.newProcessOp.addOperationListener(this);
            this.newProcessOp.addPropertyChangeListener(this);
            this.session.queueOperation(this.newProcessOp);
        } catch (Exception localException) {
            Debug.printStackTrace("NEWPROCESS", localException);
            MessageBox.post(this.parentFrame, localException);
        }
    }

    public void startOperation(String paramString) {
        super.startOperation(paramString);
        this.processNameTextField.setEnabled(false);
        this.processDefinitionComboBox.setEnabled(false);
        this.processDescTextArea.setEnabled(false);
        validate();
    }

    public void endOperation() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                SDVNewProcessDialog.this.endOperation();
            }
        });
        this.newProcessOp.removeOperationListener(this);
        if (!(this.newProcessOp.isAbortRequested())) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    SDVNewProcessDialog.this.applyButton.setVisible(false);
                    SDVNewProcessDialog.this.processNameTextField.setEnabled(true);
                    SDVNewProcessDialog.this.processDefinitionComboBox.setEnabled(true);
                    SDVNewProcessDialog.this.processDescTextArea.setEnabled(true);
                    SDVNewProcessDialog.this.processNameTextField.requestFocus();
                    SDVNewProcessDialog.this.processNameTextField.selectAll();
                    SDVNewProcessDialog.this.validate();
                    if (!(SDVNewProcessDialog.this.newProcessOp.getSuccessFlag()))
                        return;
                    SDVNewProcessDialog.this.okButton.setVisible(false);
                    SDVNewProcessDialog.this.disposeDialog();
                }
            });
            if (!(this.newProcessOp.getSuccessFlag()))
                return;
            this.attachmentsPanel.removeAttachmentChangeListener(this);
        } else {
            if (Debug.isOn("NEWPROCESSUI")) {
                Debug.println("------------------------------------------------");
                Debug.println("====> Before cleaning up the process endOperation()...");
                Debug.println("------------------------------------------------");
            }
            this.newProcessOp.cleanUp();
        }
    }

    public String getProcessName() {
        return this.processNameTextField.getText();
    }

    public String getProcessDescription() {
        return this.processDescTextArea.getText();
    }

    public String getProcessDefinition() {
        return this.processDefinitionComboBox.getSelectedItem().toString();
    }

    public Object getProcessTemplate() {
        return this.processDefinitionComboBox.getSelectedItem();
    }

    public TCComponent[] getAttachmentComponents() {
        return this.attachmentsPanel.getAttachments();
    }

    public int[] getAttachmentTypes() {
        return this.attachmentsPanel.getAttachmentTypes();
    }

    public TCSession getSession() {
        return this.session;
    }

    public TCComponent getCreatorTask() {
        return this.creatorTask;
    }
	//[2024.01.25]수정
    //추상 메소드 누락으로 인한 추가
	public TCComponent[] getAttachmentComponentsByTypes(int[] arg0) {
		return this.attachmentsPanel.getAttachmentsByTypes(arg0);
	}


    private void addInheritedTargets() {
        if ((!(this.inheritTargetsEnabled)) || (!(this.inheritTargetsSelected)))
            return;
        try {
            if (this.creatorTaskTargets == null)
                this.creatorTaskTargets = ((TCComponentTask) this.creatorTask).getAttachments(TCAttachmentScope.GLOBAL, 1);
            if (this.creatorTaskTargets.length > 0)
                this.attachmentsPanel.insertTargetComponents(this.creatorTaskTargets);
        } catch (Exception localException) {
            MessageBox localMessageBox = new MessageBox(localException);
            localMessageBox.setVisible(true);
        }
    }

    private void removeInheritedTargets() {
        try {
            if (this.creatorTaskTargets == null)
                this.creatorTaskTargets = ((TCComponentTask) this.creatorTask).getAttachments(TCAttachmentScope.GLOBAL, 1);
            if (this.creatorTaskTargets.length > 0)
                this.attachmentsPanel.removeTargetComponents(this.creatorTaskTargets);
        } catch (Exception localException) {
            MessageBox localMessageBox = new MessageBox(localException);
            localMessageBox.setVisible(true);
        }
    }

    public TCComponent[] getAttachmentComponentsOfType(int paramInt) {
        TCComponent[] arrayOfTCComponent1 = getAttachmentComponents();
        int[] arrayOfInt = getAttachmentTypes();
        if ((arrayOfTCComponent1 == null) || (arrayOfTCComponent1.length == 0))
            return null;
        Vector<TCComponent> localVector = new Vector<TCComponent>();
        for (int i = 0; i < arrayOfTCComponent1.length; ++i) {
            if (arrayOfInt[i] != paramInt)
                continue;
            localVector.addElement(arrayOfTCComponent1[i]);
        }
        TCComponent[] arrayOfTCComponent2 = (TCComponent[]) (TCComponent[]) localVector.toArray(new TCComponent[localVector.size()]);
        return arrayOfTCComponent2;
    }

    public ResourceMember[] getSelectedResources() {
        return this.selResourceList;
    }

    public void populateProcessDefList() {
        this.processDefinitionComboBox.removeAllItems();
        this.curSelTemplateIndex = -1;
        int i;
        String[] localObject;
        Registry registry;
        if ((this.allRadioButton.isSelected()) && (!(this.onlyAssigned))) {
            i = this.procListAll.size();
            if (i < 1) {
                getProcListAll();
                i = this.procListAll.size();
            }
            if (i < 1) {
                this.processDefinitionComboBox.setEnabled(true);
                registry = Registry.getRegistry(this);
                MessageBox.post(registry.getString("noProcessTemplateDefined"), registry.getString("error.TITLE"), 2);
                return;
            }
            localObject = createRenderIcons(this.procListAll);
            this.processDefinitionComboBox.addItems(this.procListAll.toArray(), localObject);
            this.processDefinitionComboBox.sort(localObject);
            if (Debug.isOn("NEWPROCESSUI")) {
                Debug.println("------------------------------------------------");
                Debug.println("====> ALL option: Set Selected Index ...");
                Debug.println("------------------------------------------------");
            }
        } else if ((this.assignedRadioButton.isSelected()) || (this.onlyAssigned)) {
            i = this.procListAssigned.size();
            if (i < 1) {
                getProcListAssigned();
                i = this.procListAssigned.size();
            }
            if (i > 0) {
                localObject = createRenderIcons(this.procListAssigned);
                this.processDefinitionComboBox.addItems(this.procListAssigned.toArray(), localObject);
                if (Debug.isOn("NEWPROCESSUI")) {
                    Debug.println("------------------------------------------------");
                    Debug.println("====> ASSIGNED option num > 0 : Set Selected Index ...");
                    Debug.println("------------------------------------------------");
                }
            }
            Vector<?> vectorObj = filterTemplates();
            if (this.filterInstancer != null)
                if (vectorObj != null) {
                    int j = vectorObj.size();
                    logger.debug("Number of templates returned are ");
                    logger.debug(Integer.valueOf(j));
                    this.processDefinitionComboBox.removeAllItems();
                    for (int k = 0; k < j; ++k) {
                        if (vectorObj.get(k) == null)
                            continue;
                        this.processDefinitionComboBox.addItem(vectorObj.get(k));
                    }
                } else {
                    logger.info("No templates returned");
                    this.processDefinitionComboBox.removeAllItems();
                }
        }
        this.processDefinitionComboBox.setAutoCompleteSuggestive(false);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (SDVNewProcessDialog.this.processDefinitionComboBox.getItemCount() <= 0) {
                    SDVNewProcessDialog.this.assignPanel.clearPanel();
                } else {
                    SDVNewProcessDialog.this.processDefinitionComboBox.setSelectedIndex(0);
                    SDVNewProcessDialog.this.processDefinitionComboBox.updateSelections();
                }
            }
        });
    }

    public void propertyChange(PropertyChangeEvent paramPropertyChangeEvent) {
        if ((paramPropertyChangeEvent.getSource() != this.attachmentsPanel) || (paramPropertyChangeEvent.getPropertyName() != "attachment_changed"))
            return;
        this.procListAssigned.removeAllElements();
        if ((!(this.assignedRadioButton.isSelected())) && (!(this.onlyAssigned)))
            return;
        final Object localObject = this.processDefinitionComboBox.getSelectedItem();
        populateProcessDefList();
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object[] arrayOfObject = SDVNewProcessDialog.this.processDefinitionComboBox.getItems();
                int i = 0;
                for (int j = 0; j < arrayOfObject.length; ++j) {
                    if (arrayOfObject[j] != localObject)
                        continue;
                    i = 1;
                    break;
                }
                if (i != 0)
                    SDVNewProcessDialog.this.processDefinitionComboBox.setSelectedItem(localObject);
                else
                    SDVNewProcessDialog.this.processDefinitionComboBox.setSelectedIndex(0);
                SDVNewProcessDialog.this.processDefinitionComboBox.updateSelections();
            }
        });
    }

    public void getProcListAll() {
        TCComponentTaskTemplate[] temp = null;
        TCComponentTaskTemplate[] arrayOfTCComponentTaskTemplate = null;
        try {
            TCComponentTaskTemplateType localTCComponentTaskTemplateType = (TCComponentTaskTemplateType) this.session.getTypeComponent("EPMTaskTemplate");
            if (localTCComponentTaskTemplateType != null) {
                try {
//                    temp = localTCComponentTaskTemplateType.extentReadyTemplates(this.switchOffUCTemplates);
                	temp = localTCComponentTaskTemplateType.getProcessTemplates(!(this.switchOffUCTemplates), false, null, null, null);

                    TCPreferenceService prefService = this.session.getPreferenceService();
//                    String[] templateNames = prefService.getStringArray(TCPreferenceService.TC_preference_site, templatePrefName);
                    String[] templateNames = prefService.getStringValuesAtLocation(templatePrefName, TCPreferenceLocation.OVERLAY_LOCATION);
                    if(templateNames != null) {
                        arrayOfTCComponentTaskTemplate = new TCComponentTaskTemplate[templateNames.length];
                        for(int i = 0; i < templateNames.length; i++) {
                            for(int j = 0; j < temp.length; j++) {
                                if(templateNames[i].equals(temp[j].getName())) {
                                    arrayOfTCComponentTaskTemplate[i] = temp[j];
                                    break;
                                }
                            }
                        }
                    }
//                    arrayOfTCComponentTaskTemplate = temp;
//                    arrayOfTCComponentTaskTemplate = localTCComponentTaskTemplateType.extentReadyTemplates(this.switchOffUCTemplates);
                } catch (TCException localTCException2) {
                    MessageBox.post(this.parentFrame, localTCException2);
                }
                int i = 0;
                if (arrayOfTCComponentTaskTemplate != null) {
                    i = arrayOfTCComponentTaskTemplate.length;
                }
                this.procListAll.clear();
                for (int j = 0; j < i; ++j)
                    this.procListAll.addElement(arrayOfTCComponentTaskTemplate[j]);
            }
        } catch (TCException localTCException1) {
            MessageBox.post(this.parentFrame, localTCException1);
        }
    }

    public void getProcListAssigned() {
        try {
            TCComponent[] arrayOfTCComponent = getAttachmentComponents();
            int[] arrayOfInt = this.attachmentsPanel.getAttachmentTypes();
            if (arrayOfTCComponent == null)
                return;
            int i = arrayOfTCComponent.length;
            if (i == 0)
                return;
            HashSet<String> localHashSet = new HashSet<String>();
            for (int j = 0; j < i; ++j) {
                if (arrayOfInt[j] != 1)
                    continue;
                localHashSet.add(arrayOfTCComponent[j].getType());
            }
            String[] arrayOfString = new String[localHashSet.size()];
            localHashSet.toArray(arrayOfString);
            TCComponentGroup localTCComponentGroup = this.session.getGroup();
            String str = localTCComponentGroup.getFullName();
            TCComponentTaskTemplate[] arrayOfTCComponentTaskTemplate = null;
            TCComponentTaskTemplateType localTCComponentTaskTemplateType = (TCComponentTaskTemplateType) this.session.getTypeComponent("EPMTaskTemplate");
            if (localTCComponentTaskTemplateType != null)
//                arrayOfTCComponentTaskTemplate = localTCComponentTaskTemplateType.getAssignedProcessesForMultipleObjects(this.switchOffUCTemplates, str, arrayOfString, arrayOfString.length);
            	arrayOfTCComponentTaskTemplate = localTCComponentTaskTemplateType.getProcessTemplates(!(this.switchOffUCTemplates), true, null, arrayOfString, str);
            int k = 0;
            if (arrayOfTCComponentTaskTemplate != null)
                k = arrayOfTCComponentTaskTemplate.length;
            if (k > 0) {
                this.procListAssigned.clear();
                for (int l = 0; l < k; ++l)
                    this.procListAssigned.addElement(arrayOfTCComponentTaskTemplate[l]);
            }
        } catch (Exception localException) {
            MessageBox.post(this.parentFrame, localException);
            return;
        }
    }

    public void initProcessDefList() {
        int i = 1;
        TCPreferenceService localTCPreferenceService = this.session.getPreferenceService();
//        String str = localTCPreferenceService.getString(0, "CR_allow_alternate_procedures");
        String str = localTCPreferenceService.getStringValue("CR_allow_alternate_procedures");
        if ((str != null) && (str.length() > 0))
            if (str.equalsIgnoreCase("ANY"))
                i = 1;
            else if (str.equalsIgnoreCase("none"))
                i = 2;
            else if (str.equalsIgnoreCase("Assigned"))
                i = 3;
        int j;
        int k;
        if (i == 1) {
            this.allRadioButton.doClick();
            getProcListAll();
            j = this.procListAll.size();
            this.procList.clear();
            for (k = 0; k < j; ++k)
                this.procList.addElement(this.procListAll.elementAt(k));
        } else if (i == 2) {
            this.processFilterLabel.setVisible(false);
            this.allRadioButton.setVisible(false);
            this.assignedRadioButton.setVisible(false);
            getProcListAssigned();
            j = this.procListAssigned.size();
            this.onlyAssigned = true;
            this.procList.clear();
            for (k = 0; k < j; ++k)
                this.procList.addElement(this.procListAssigned.elementAt(k));
        } else {
            if (i != 3)
                return;
            this.processFilterLabel.setVisible(true);
            this.allRadioButton.setVisible(true);
            this.assignedRadioButton.setVisible(true);
            this.assignedRadioButton.doClick();
            getProcListAssigned();
            j = this.procListAssigned.size();
            this.procList.clear();
            for (k = 0; k < j; ++k)
                this.procList.addElement(this.procListAssigned.elementAt(k));
            this.radioButtonFlag = 3;
        }
    }

    public void startLoadProcDefsOperation() {
        this.loadOp = new LoadProcDefsOperation(Registry.getRegistry(this).getString("loadingDefTemplates"));
        this.session.queueOperation(this.loadOp);
    }

    public Vector<?> filterTemplates() {
        Vector<TCComponentTaskType> localVector1 = null;
        Vector<TCComponent> localVector2 = new Vector<TCComponent>();
        TemplateFilterService localTemplateFilterService = TemplateFilterService.getInstance();
        this.filterInstancer = localTemplateFilterService.getTemplateFilter();
        Registry localRegistry = Registry.getRegistry(this);
        if (this.filterInstancer != null) {
            getProcListAll();
            try {
                Vector<TCComponentTaskType> procTypeListAll = new Vector<TCComponentTaskType>();
                for(TCComponent comp :procListAll){
                    procTypeListAll.add((TCComponentTaskType)comp);
                }


                Vector<TCComponentTaskType> procTypeListAssigned = new Vector<TCComponentTaskType>();
                for(TCComponent comp :procListAssigned){
                    procTypeListAssigned.add((TCComponentTaskType)comp);
                }

                localVector1 = this.filterInstancer.getFilteredTemplates(procTypeListAll,procTypeListAssigned, this.pasteTargets, this.session);
            } catch (Exception localException1) {
                if (localException1 instanceof NoCustomFilteringRequiredException) {
                    logger.debug("Custom Filtering is not required. Passing the OOTB filtered templates for display");
                    return this.procListAssigned;
                }
                logger.error("Exception passed by the custom code", localException1);
                MessageBox.post(localRegistry.getString("customCodeException"), localRegistry.getString("error.TITLE"), 1);
                return null;
            }
            try {
                if (localVector1 != null) {
                    int i = localVector1.size();
                    for (int j = 0; j < i; ++j) {
                        if ((localVector1.get(j) == null) || (localVector2.contains(localVector1.get(j))))
                            continue;
                        localVector2.add(localVector1.get(j));
                    }
                }
                if (localVector2.size() > 0) {
                    boolean bool = this.procListAll.containsAll(localVector2);
                    if (bool)
                        return localVector2;
                    if (!(bool)) {
                        logger.error("Templates returned from custom code are invalid");
                        MessageBox.post(localRegistry.getString("templatesNotInDatabase"), localRegistry.getString("error.TITLE"), 1);
                        return null;
                    }
                }
            } catch (Exception localException2) {
                logger.error("Exception", localException2);
                MessageBox.post(this.parentFrame, localException2);
                return null;
            }
        }
        return localVector2;
    }

    private class LoadProcDefsOperation extends AbstractAIFOperation {
        public LoadProcDefsOperation(String paramString) {
            super(paramString);
        }

        public void executeOperation() {
            Object localObject;
            try {
                SDVNewProcessDialog.this.initProcessDefList();
                if (isAbortRequested())
                    return;
            } catch (Exception localException1) {
                localObject = Registry.getRegistry(this);
                MessageBox.post(SDVNewProcessDialog.this.parentFrame, ((Registry) localObject).getString("loadProcDefsError"), "", ((Registry) localObject).getString("error.TITLE"), 1);
                return;
            }
            int i = 0;
            try {
                localObject = SDVNewProcessDialog.this.session.getPreferenceService();
//                String str = ((TCPreferenceService) localObject).getString(0, "CR_allow_alternate_procedures");
                String str = ((TCPreferenceService) localObject).getStringValue("CR_allow_alternate_procedures");
                if ((str != null) && (str.length() > 0) && (str.equalsIgnoreCase("Assigned")))
                    i = 1;
            } catch (Exception localException2) {
                MessageBox.post(SDVNewProcessDialog.this.parentFrame, localException2);
            }
            if (i == 0)
                SDVNewProcessDialog.this.populateProcessDefList();
            SDVNewProcessDialog.this.currentSelection = (SDVNewProcessDialog.this.radioButtonFlag != 3);
        }
    }

}
