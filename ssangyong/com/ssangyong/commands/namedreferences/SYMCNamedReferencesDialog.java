package com.ssangyong.commands.namedreferences;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import org.apache.log4j.Logger;

import com.teamcenter.rac.aif.AIFClipboard;
import com.teamcenter.rac.aif.AIFPortal;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.common.AIFIdentifier;
import com.teamcenter.rac.aif.kernel.AIFComponentChangeEvent;
import com.teamcenter.rac.aif.kernel.AIFComponentEvent;
import com.teamcenter.rac.aif.kernel.AbstractAIFSession;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponentEventListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.namedreferences.ImportFilesFileChooser;
import com.teamcenter.rac.commands.namedreferences.ImportFilesOperation;
import com.teamcenter.rac.commands.namedreferences.PasteNamedReferencesDialog;
import com.teamcenter.rac.commands.namedreferences.RemoveNamedRefOperation;
import com.teamcenter.rac.commands.newdataset.IFilesSelector;
import com.teamcenter.rac.commands.newdataset.TCFileDescriptor;
import com.teamcenter.rac.commands.newdataset.TCFileSelectorService;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetDefinition;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ButtonLayout;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.Cookie;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.log.Debug;

// Referenced classes of package com.teamcenter.rac.commands.namedreferences:
//            ImportFilesFileChooser, ImportFilesOperation, RemoveNamedRefOperation, PasteNamedReferencesDialog

@SuppressWarnings({"serial", "rawtypes", "unchecked", "unused"})
public class SYMCNamedReferencesDialog extends AbstractAIFDialog implements InterfaceAIFComponentEventListener
{
    
	private class ToolSelectedDialog extends AbstractAIFDialog
    {

        private void initializeDialog()
        {
            setTitle(appReg.getString("selectTool.TITLE"));
            JPanel jpanel = new JPanel(new VerticalLayout(5, 2, 2, 2, 2));
            getContentPane().add(jpanel);
            toolListModel = new DefaultListModel();
            toolList = new JList(toolListModel);
            toolList.setSelectionMode(0);
            JScrollPane jscrollpane = new JScrollPane(toolList);
            JPanel jpanel1 = new JPanel(new VerticalLayout(5, 2, 2, 2, 2));
            JLabel jlabel = new JLabel(appReg.getString("userWizard"));
            Font font = jlabel.getFont();
            font = new Font(font.getName(), 1, font.getSize());
            jlabel.setFont(font);
            jpanel1.add("top.nobind.left.top", jlabel);
            jpanel1.add("unbound.bind.center.center", jscrollpane);
            JPanel jpanel2 = new JPanel(new HorizontalLayout(15, 2, 2, 2, 2));
            jpanel2.add("left.bind.left.center", new JLabel(appReg.getOverlayImageIcon("stepBackground.ICON", "newForm32.ICON", 1)));
            jpanel2.add("unbound.bind.center.center", jpanel1);
            JPanel jpanel3 = new JPanel();
            jpanel3.setLayout(new ButtonLayout(1, 3, 20));
            nextButton = new JButton(appReg.getString("nextButton"), appReg.getImageIcon("next.ICON"));
            nextButton.setHorizontalTextPosition(2);
            nextButton.setMnemonic(appReg.getString("next.MNEMONIC").charAt(0));
            nextButton.setEnabled(false);
            nextButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent actionevent)
                {
                    doNextOperation();
                    disposeDialog();
                }
            });
            
            toolList.addListSelectionListener(new ListSelectionListener() {

                public void valueChanged(ListSelectionEvent listselectionevent)
                {
                    nextButton.requestFocusInWindow();
                }

            });
            
            jpanel3.add(nextButton);
            jpanel.add("top.nobind.left.top", new JLabel(appReg.getImageIcon("Tool.ICON"), 0));
            jpanel.add("top.bind.center.center", new Separator());
            jpanel.add("unbound.bind.left.center", jpanel2);
            jpanel.add("bottom.bind.center.center", jpanel3);
            jpanel.add("bottom.bind.center.center", new Separator());
            loadTools();
            if(toolComponents == null)
                return;
            if(toolComponents.length == 1)
            {
                selectedToolComponent = toolComponents[0];
                return;
            } else
            {
                toolList.setSelectedIndex(0);
                nextButton.setEnabled(true);
                setDefaultCloseOperation(0);
                addWindowListener(new WindowAdapter() {

                    public void windowClosing(WindowEvent windowevent)
                    {
                        selectedToolComponent = null;
                        disposeDialog();
                    }
                });
                pack();
                centerToScreen(1.5D, 1.0D, 0.59999999999999998D, 0.40000000000000002D);
                setVisible(true);
                return;
            }
        }

        private void loadTools()
        {
            try
            {
                toolComponents = datasetComponent.getDatasetDefinitionComponent().getToolsForNamedRef(namedRef, 4);
            }
            catch(TCException tcexception)
            {
                MessageBox.post(parent, tcexception);
                return;
            }
            if(toolComponents == null)
                return;
            TCComponent atccomponent[] = toolComponents;
            int i = atccomponent.length;
            for(int j = 0; j < i; j++)
            {
                TCComponent tccomponent = atccomponent[j];
                toolListModel.addElement(tccomponent);
            }

            toolList.revalidate();
            toolList.doLayout();
            toolList.repaint();
        }

        private void doNextOperation()
        {
            selectedToolComponent = (TCComponent)toolList.getSelectedValue();
        }

        private JButton nextButton;
        private JList toolList;
        private DefaultListModel toolListModel;
        private String namedRef;
        final SYMCNamedReferencesDialog this$0;



        public ToolSelectedDialog(Frame frame, String s)
        {
            this$0 = SYMCNamedReferencesDialog.this;
//            super(frame, true);
            namedRef = s;
            initializeDialog();
        }
    }

    private class RightTextRenderer extends DefaultTableCellRenderer
    {

        public Component getTableCellRendererComponent(JTable jtable, Object obj, boolean flag, boolean flag1, int i, int j)
        {
            super.getTableCellRendererComponent(jtable, obj, flag, flag1, i, j);
            if(null != obj)
                setText(obj.toString());
            else
                setText("");
            return this;
        }


        public RightTextRenderer()
        {
            super();
            setHorizontalAlignment(4);
        }
    }

    private class NamedReferencesTableModel extends DefaultTableModel
    {

        public AIFIdentifier[] getColumnIdentifiers()
        {
            return identifiers;
        }

        public int getColumnCount()
        {
            return identifiers.length;
        }

        public int getRowCount()
        {
            if(compVector == null)
                return 0;
            else
                return compVector.size();
        }

        public String getColumnName(int i)
        {
            return (String)identifiers[i].getColumnName();
        }

        public Object getValueAt(int i, String s)
        {
            String s1 = "";
            TCComponent tccomponent = getComponentAt(i);
            try
            {
                if(s.equals("object_name"))
                    s1 = getNamedRefType(tccomponent);
                else
                if(s.equals("original_file_name"))
                {
                    if(tccomponent instanceof TCComponentTcFile)
                        s1 = tccomponent.getProperty(s);
                    else
                        s1 = tccomponent.getProperty("object_name");
                } else
                if((tccomponent instanceof TCComponentTcFile) && s.equals("last_mod_date"))
                {
                    TCPreferenceService tcpreferenceservice = ((TCSession)session).getPreferenceService();
                    //boolean flag = tcpreferenceservice.isTrue(4, "Display_OSFile_LMD_for_TCFile");
                    Boolean flag = tcpreferenceservice.getLogicalValueAtLocation("Display_OSFile_LMD_for_TCFile", TCPreferenceLocation.convertLocationFromLegacy(TCPreferenceService.TC_preference_site));
                    if(flag == null) {
                    	flag = false;
                    }
                    
                    if(Debug.isOn("dataset"))
                    {
                        Debug.println((new StringBuilder()).append("Preference [Display_OSFile_LMD_for_TCFile] value [").append(flag).append("]").toString());
                        if(flag)
                            Debug.println((new StringBuilder()).append("Use OSFile LMD for LMD of TCFile [").append(tccomponent.toString()).append("]").toString());
                        else
                            Debug.println((new StringBuilder()).append("Use LMD of TCFile [").append(tccomponent.toString()).append("]").toString());
                    }
                    if(osLastModifiedDateTable.containsKey(tccomponent))
                    {
                        s1 = (String)osLastModifiedDateTable.get(tccomponent);
                    } else
                    {
                        if(flag)
                            s1 = ((TCComponentTcFile)tccomponent).getOSLastModifiedDate();
                        else
                            s1 = tccomponent.getProperty(s);
                        osLastModifiedDateTable.put(tccomponent, s1);
                    }
                } else
                {
                    s1 = tccomponent.getProperty(s);
                }
            }
            catch(TCException tcexception)
            {
                System.out.println(tcexception.toString());
            }
            return s1;
        }

        public Object getValueAt(int i, int j)
        {
            String s = (String)identifiers[j].getColumnId();
            return getValueAt(i, s);
        }

        public TCComponent getComponentAt(int i)
        {
            return (TCComponent)compVector.elementAt(i);
        }

        public Class getColumnClass(int i)
        {
            return getValueAt(0, i).getClass();
        }

        public boolean isCellEditable(int i, int j)
        {
            String s = (String)identifiers[j].getColumnId();
            return s.equals("original_file_name") && (getComponentAt(i) instanceof TCComponentTcFile);
        }

        public void setValueAt(Object obj, int i, int j)
        {
            TCComponent tccomponent = getComponentAt(i);
            startModifyFileName(tccomponent, (String)obj);
            fireTableCellUpdated(i, j);
        }

        public void removeRow(int i)
        {
            TCComponent tccomponent = getComponentAt(i);
            if(osLastModifiedDateTable.containsKey(tccomponent))
                osLastModifiedDateTable.remove(tccomponent);
            compVector.removeElementAt(i);
            fireTableRowsDeleted(i, i);
        }

        public void removeRows(int ai[])
        {
            int ai1[] = ai;
            int i = ai1.length;
            for(int j = 0; j < i; j++)
            {
                int k = ai1[j];
                removeRow(k);
            }

        }

        public void removeAllRows()
        {
            int i = getRowCount();
            for(int j = 0; j < i; j++)
                removeRow(0);

        }

        public void addRow(TCComponent tccomponent)
        {
            if(tccomponent == null)
            {
                return;
            } else
            {
                int i = compVector.size();
                compVector.addElement(tccomponent);
                fireTableRowsInserted(i, i);
                return;
            }
        }

        private String getNamedRefType(TCComponent tccomponent)
        {
            String s;
            s = "";
            TCProperty tcproperty;
            TCProperty tcproperty1;
            TCComponent atccomponent[];
            String as[];
            int i;
            int j;
            int k;
            try
            {
                tcproperty = datasetComponent.getTCProperty("ref_list");
                tcproperty1 = datasetComponent.getTCProperty("ref_names");
                if(tcproperty == null || tcproperty1 == null)
                    return s;
            }
            catch(TCException tcexception)
            {
                return "";
            }
            atccomponent = tcproperty.getReferenceValueArray();
            as = tcproperty1.getStringValueArray();
            if(atccomponent == null || as == null)
                return s;
            i = atccomponent.length;
            if(i != as.length)
                return s;
            j = -1;
            k = 0;
            do
            {
                if(k >= i)
                    break;
                if(tccomponent == atccomponent[k])
                {
                    j = k;
                    break;
                }
                k++;
            } while(true);
            if(j != -1)
                s = as[j];
            return s;
        }

        private Vector compVector;
        private AIFIdentifier identifiers[];
        private Hashtable osLastModifiedDateTable;


        public NamedReferencesTableModel(String as[])
        {
            compVector = new Vector();
            identifiers = null;
            osLastModifiedDateTable = new Hashtable();
            Registry registry = Registry.getRegistry(this);
            String as1[] = null;
            String as2[] = null;
            try
            {
                Cookie cookie = Cookie.getCookie("namedreference", true);
                as1 = cookie.getStringArray("columnname");
                as2 = cookie.getStringArray("columnwidth");
                cookie.close();
            }
            catch(Exception exception) { }
            if(as1 == null || as1.length == 0)
            {
                int i = 7;
                if(as != null && as.length > 0)
                    i = 7 + as.length;
                as1 = new String[i];
                as1[0] = "object_name";
                as1[1] = "original_file_name";
                as1[2] = "file_size";
                as1[3] = "owning_site";
                as1[4] = "object_type";
                as1[5] = "last_mod_date";
                as1[6] = "volume_tag";
                if(as != null && as.length > 0)
                {
                    for(int k = 0; k < as.length; k++)
                        as1[k + 7] = as[k];

                }
            } else
            if(as != null && as.length > 0)
            {
                Vector vector = Utilities.convertArrayToVector(as1);
                String as3[] = as;
                int j1 = as3.length;
                for(int l1 = 0; l1 < j1; l1++)
                {
                    String s1 = as3[l1];
                    if(!vector.contains(s1))
                        vector.add(s1);
                }

                as1 = (String[])(String[])vector.toArray(new String[vector.size()]);
            }
            int j = as1.length;
            Integer ainteger[] = new Integer[j];
            if(as2 == null || as2.length == 0)
            {
                for(int l = 0; l < j; l++)
                    ainteger[l] = null;

            } else
            {
                for(int i1 = 0; i1 < j; i1++)
                    if(i1 < as2.length)
                        ainteger[i1] = new Integer(as2[i1]);
                    else
                        ainteger[i1] = null;

            }
            identifiers = new AIFIdentifier[j];
            for(int k1 = 0; k1 < j; k1++)
            {
                String s = (new StringBuilder()).append(as1[k1]).append(".NAME").toString();
                identifiers[k1] = new AIFIdentifier(registry.getString(s, as1[k1]), as1[k1]);
                if(k1 < ainteger.length)
                    identifiers[k1].setClientData(ainteger[k1]);
            }

            setColumnIdentifiers(identifiers);
        }
    }


    public SYMCNamedReferencesDialog(TCComponentDataset tccomponentdataset, Frame frame)
    {
//    	super(tccomponentdataset, frame);
    	super(frame, true);
    	datasetComponent = null;
        truncateFileName = "FALSE";
        parent = frame;
        session = tccomponentdataset.getSession();
        datasetComponent = tccomponentdataset;
        initializeDialog();
    }

    public SYMCNamedReferencesDialog(TCComponentDataset tccomponentdataset, Dialog dialog)
    {
//        super(tccomponentdataset, dialog);
    	super(dialog, true);
    	datasetComponent = null;
        truncateFileName = "FALSE";
        parent = dialog;
        session = tccomponentdataset.getSession();
        datasetComponent = tccomponentdataset;
        initializeDialog();
    }

    public SYMCNamedReferencesDialog(TCComponentDataset tccomponentdataset)
    {
//        super(tccomponentdataset);
    	super(true);
    	datasetComponent = null;
        truncateFileName = "FALSE";
        session = tccomponentdataset.getSession();
        datasetComponent = tccomponentdataset;
        initializeDialog();
    }

    private void initializeDialog()
    {
    	appReg = Registry.getRegistry(this);
        setTitle(appReg.getString("command.TITLE"));
        setModal(false);
        JPanel jpanel = new JPanel(new VerticalLayout(5, 2, 2, 2, 2));
        getContentPane().add(jpanel);
        JPanel jpanel1 = new JPanel(new HorizontalLayout(2, 2, 2, 2, 2));
        JPanel jpanel2 = new JPanel(new ButtonLayout());
        TCPreferenceService tcpreferenceservice = ((TCSession)session).getPreferenceService();
        //String as[] = tcpreferenceservice.getStringArray(0, "com.teamcenter.rac.commands.namedreferences.AdditionalColumnShown");
        //truncateFileName = tcpreferenceservice.getString(0, "TC_truncate_file_name");
        String as[] = tcpreferenceservice.getStringValues("com.teamcenter.rac.commands.namedreferences.AdditionalColumnShown");
        truncateFileName = tcpreferenceservice.getStringValue("TC_truncate_file_name");
        model = new NamedReferencesTableModel(as);
        table = new JTable(model);
        table.setPreferredScrollableViewportSize(new Dimension(550, 100));
        RightTextRenderer righttextrenderer = new RightTextRenderer();
        TableColumn tablecolumn = getTableColumnByName("file_size");
        tablecolumn.setCellRenderer(righttextrenderer);
        table.setAutoResizeMode(0);
        tableScrollPane = new JScrollPane(table);
        DefaultTableCellRenderer defaulttablecellrenderer = new DefaultTableCellRenderer();
        defaulttablecellrenderer.setToolTipText(appReg.getString("changeFileName.TIP"));
        TableColumn tablecolumn1 = getTableColumnByName("original_file_name");
        tablecolumn1.setCellRenderer(defaulttablecellrenderer);
        AIFIdentifier aaifidentifier[] = model.getColumnIdentifiers();
        AIFIdentifier aaifidentifier1[] = aaifidentifier;
        int i = aaifidentifier1.length;
        for(int j = 0; j < i; j++)
        {
            AIFIdentifier aifidentifier = aaifidentifier1[j];
            TableColumn tablecolumn2 = getTableColumnByName((String)aifidentifier.getColumnId());
            if(aifidentifier.getClientData() != null)
            {
                Integer integer = (Integer)aifidentifier.getClientData();
                tablecolumn2.setPreferredWidth(integer.intValue());
            }
        }

        JPanel jpanel3 = new JPanel(new ButtonLayout());
        openButton = new JButton(appReg.getString("open"));
        openButton.setMnemonic(appReg.getString("open.MNEMONIC").charAt(0));
        openButton.setToolTipText(appReg.getString("open.TOOLTIP"));
        importButton = new JButton(appReg.getString("importbutton"));
        importButton.setMnemonic(appReg.getString("import.MNEMONIC").charAt(0));
        importButton.setToolTipText(appReg.getString("import.TOOLTIP"));
        exportButton = new JButton(appReg.getString("exportbutton"));
        exportButton.setMnemonic(appReg.getString("export.MNEMONIC").charAt(0));
        exportButton.setToolTipText(appReg.getString("export.TOOLTIP"));
        jpanel3.add(openButton);
        jpanel3.add(importButton);
        jpanel3.add(exportButton);
        JPanel jpanel4 = new JPanel(new HorizontalLayout());
        cutButton = new JButton(appReg.getImageIcon("cut.ICON"));
        cutButton.setFocusPainted(false);
        cutButton.setMargin(new Insets(0, 0, 0, 0));
        cutButton.setToolTipText(appReg.getString("cut.TOOLTIP"));
        copyButton = new JButton(appReg.getImageIcon("copy.ICON"));
        copyButton.setFocusPainted(false);
        copyButton.setMargin(new Insets(0, 0, 0, 0));
        copyButton.setToolTipText(appReg.getString("copy.TOOLTIP"));
        pasteButton = new JButton(appReg.getImageIcon("paste.ICON"));
        pasteButton.setFocusPainted(false);
        pasteButton.setMargin(new Insets(0, 0, 0, 0));
        pasteButton.setToolTipText(appReg.getString("paste.TOOLTIP"));
        deleteButton = new JButton(appReg.getImageIcon("delete.ICON"));
        deleteButton.setFocusPainted(false);
        deleteButton.setMargin(new Insets(0, 0, 0, 0));
        deleteButton.setToolTipText(appReg.getString("remove.TOOLTIP"));
        jpanel4.add("right.nobind.center.center", deleteButton);
        jpanel4.add("right.nobind.center.center", pasteButton);
        jpanel4.add("right.nobind.center.center", copyButton);
        jpanel4.add("right.nobind.center.center", cutButton);
        jpanel1.add("left.nobind.left.center", jpanel3);
        jpanel1.add("right.nobind.right.center", jpanel4);
        JButton jbutton = new JButton(appReg.getString("close"));
        jbutton.setMnemonic(appReg.getString("close.MNEMONIC").charAt(0));
        jpanel2.add(jbutton);
        JLabel jlabel = new JLabel(appReg.getImageIcon("namedreferences.ICON"), 0);
        JLabel jlabel1 = new JLabel(appReg.getImageIcon("modeless.ICON"));
        jlabel1.setToolTipText(appReg.getString("modeless.TOOLTIP"));
        JPanel jpanel5 = new JPanel(new HorizontalLayout(2, 0, 0, 0, 0));
        jpanel5.add("left", jlabel);
        jpanel5.add("right", jlabel1);
        jpanel.add("top", jpanel5);
        jpanel.add("top.bind", new Separator());
        jpanel.add("bottom.bind.center.top", jpanel2);
        jpanel.add("bottom.bind", new Separator());
        jpanel.add("bottom.bind", jpanel1);
        jpanel.add("unbound.bind.center.top", tableScrollPane);
        importButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                startImportFilesOperation();
            }

        });
        exportButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                startExportFilesOperation();
            }

        });
        openButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                startOpenFileOperation();
            }

        });
        copyButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                startCopyOperation();
            }

        });
        cutButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                startCutNamedRefOperation();
            }

        });
        pasteButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                startPasteOperation();
            }

        });
        deleteButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                startDeleteNamedRefOperation();
            }

        });
        jbutton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent)
            {
                closePressed();
            }
        });
        pack();
        centerToScreen(1.5D, 1.0D);
        jbutton.requestFocusInWindow();
        loadNamedReferences();
        session.addAIFComponentEventListener(this);
        validateButtons();
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            public void valueChanged(ListSelectionEvent listselectionevent)
            {
                validateButtons();
            }

        });
    }

    public void run()
    {
        setVisible(true);
    }

    public void closePressed()
    {
        session.removeAIFComponentEventListener(this);
        TableColumnModel tablecolumnmodel = table.getColumnModel();
        int i = tablecolumnmodel.getColumnCount();
        String as[] = new String[i];
        String as1[] = new String[i];
        AIFIdentifier aaifidentifier[] = model.getColumnIdentifiers();
        for(int j = 0; j < i; j++)
        {
            TableColumn tablecolumn = tablecolumnmodel.getColumn(j);
            Integer integer = new Integer(tablecolumn.getWidth());
            as1[j] = integer.toString();
            as[j] = getPropertyName(table.getColumnName(j), aaifidentifier);
        }

        try
        {
            Cookie cookie = Cookie.getCookie("namedreference", true);
            cookie.setStringArray("columnname", as);
            cookie.setStringArray("columnwidth", as1);
            cookie.close();
        }
        catch(Exception exception) { }
        disposeDialog();
    }

    private String getPropertyName(String s, AIFIdentifier aaifidentifier[])
    {
        String s1 = "";
        if(aaifidentifier != null)
        {
            AIFIdentifier aaifidentifier1[] = aaifidentifier;
            int i = aaifidentifier1.length;
            int j = 0;
            do
            {
                if(j >= i)
                    break;
                AIFIdentifier aifidentifier = aaifidentifier1[j];
                if(aifidentifier.getColumnName().equals(s))
                {
                    s1 = (String)aifidentifier.getColumnId();
                    break;
                }
                j++;
            } while(true);
        }
        return s1;
    }

    private TableColumn getTableColumnByName(String s)
    {
        int i = 0;
        AIFIdentifier aaifidentifier[] = model.getColumnIdentifiers();
        int j = 0;
        do
        {
            if(j >= aaifidentifier.length)
                break;
            if(s.equals(aaifidentifier[j].getColumnId()))
            {
                i = j;
                break;
            }
            j++;
        } while(true);
        return table.getColumnModel().getColumn(i);
    }

    private int[] checkTableSelection(boolean flag, boolean flag1)
    {
        int ai[] = table.getSelectedRows();
        if(ai == null || ai.length == 0)
        {
            if(flag1)
                MessageBox.post(parent, appReg.getString("noSelection"), appReg.getString("warning.TITLE"), 4);
            return null;
        }
        if(!flag && ai.length > 1)
        {
            if(flag1)
                MessageBox.post(parent, appReg.getString("noMultipleSelection"), appReg.getString("warning.TITLE"), 4);
            return null;
        } else
        {
            return ai;
        }
    }

    private void startExportFilesOperation()
    {
    	
        if(Debug.isOn("dataset,export"))
            Debug.println("==> startExportFilesOperation");
        int ai[] = checkTableSelection(true, true);
        if(ai == null)
            return;
        int i = ai.length;
        Vector vector = new Vector();

        for(int j = 0; j < i; j++)
        {
        	TCComponent tccomponent = model.getComponentAt(ai[j]);
            if(!(tccomponent instanceof TCComponentTcFile))
                continue;
            vector.addElement(tccomponent);
            final TCComponentTcFile tccomponenttcfile = (TCComponentTcFile)tccomponent;
            
            if(exportFc == null)
            {
                exportFc = new JFileChooser(getDefaultExportDirectory());
                exportFc.setFileSelectionMode(0);
                exportFc.setApproveButtonText(appReg.getString("export"));
            }
            exportFc.rescanCurrentDirectory();
            exportFc.setDialogTitle((new StringBuilder()).append(appReg.getString("exportFile.TITLE")).append(" ... ").append(tccomponenttcfile).toString());
            File file = exportFc.getCurrentDirectory();
            String s = (new StringBuilder()).append(file.getAbsolutePath()).append(File.separator).append(tccomponenttcfile).toString();
            File file1 = new File(s);
            String s1 = tccomponenttcfile.toString();
            int k = s1.lastIndexOf('.');
            String s2 = null;
            if(k == -1)
                s2 = s1;
            else
                s2 = s1.substring(0, k);
            if(Debug.isOn("dataset,export"))
                Debug.println((new StringBuilder()).append("Processing TCComponentTcFile [").append(tccomponenttcfile).append("] defaultFileLocation [").append(s).append("], file name [").append(s1).append("] absFileName [").append(s2).append("]").toString());
            try
            {
                TCPreferenceService tcpreferenceservice = ((TCSession)session).getPreferenceService();
                //String s3 = tcpreferenceservice.getString(0, "Dataset_File_Export_Option");
                String s3 = tcpreferenceservice.getStringValue("Dataset_File_Export_Option");
                if(Debug.isOn("dataset,export"))
                    Debug.println((new StringBuilder()).append("Pref [Dataset_File_Export_Option] value [").append(s3).append("]").toString());
                if(s3 != null && s3.length() > 0)
                    if(s3.equalsIgnoreCase("LOWERCASE"))
                        file1 = new File((new StringBuilder()).append(file.getAbsolutePath()).append(File.separator).append(s2.toLowerCase()).append(s1.substring(k)).toString());
                    else
                    if(s3.equalsIgnoreCase("UPPERCASE"))
                        file1 = new File((new StringBuilder()).append(file.getAbsolutePath()).append(File.separator).append(s2.toUpperCase()).append(s1.substring(k)).toString());
            }
            catch(Exception exception)
            {
                if(Debug.isOn("dataset,export"))
                    Debug.printStackTrace(exception);
            }
            exportFc.setSelectedFile(file1);
            if(Debug.isOn("dataset,export"))
                Debug.println((new StringBuilder()).append("setSelectedFile [").append(file1.getName()).append("]").toString());
            File file2 = null;
            Frame frame = Utilities.getCurrentFrame();
            if(exportFc.showOpenDialog(frame) == 0)
            {
                file2 = exportFc.getSelectedFile();
                do
                {
                	if(file2 == null || !file2.exists())
                    	break;
                    if(ConfirmationDialog.post(frame, appReg.getString("fileExist.TITLE"), appReg.getString("fileExists")) == 2)
                    {
                    	
                        if(Debug.isOn("dataset,export"))
                            Debug.println((new StringBuilder()).append("Deleting [").append(file2.getName()).append("]").toString());
                        if(!file2.delete())
                        {
                            MessageBox.post(appReg.getString("exportFailure.MESSAGE"), appReg.getString("exportFailure.TITLE"), 4);
                            file2 = null;
                        }
                    } else
                    if(exportFc.showOpenDialog(frame) == 0)
                        file2 = exportFc.getSelectedFile();
                    else
                        file2 = null;
                } while(true);
            }
            if(Debug.isOn("dataset,export"))
                Debug.println((new StringBuilder()).append("getSelectedFile [").append(file2.getName()).append("]").toString());
            if(file2 != null)
            {
               final File file3 = file2;
                Utilities.setCookie("filechooser", true, "DatasetExport.DIR", file3.getParent());
                session.queueOperation(new AbstractAIFOperation() {
                    
                	public void executeOperation()
                    {
                        try
                        {
                        	if(Debug.isOn("dataset,export"))
                                Debug.println((new StringBuilder()).append("getSelectedFile [").append(file3.getName()).append("] parent [[").append(file3.getParent()).append("]").toString());
                        	tccomponenttcfile.getFile(file3.getParent(), file3.getName());
                        	
                        }
                        catch(Exception exception1)
                        {
                            if(Debug.isOn("dataset,export"))
                                Debug.printStackTrace(exception1);
                            MessageBox.post(parent, exception1);
                        }
                    }
                }
                );                   

//            }
//        }

		        if(vector.isEmpty())
		        {
		            MessageBox.post(parent, appReg.getString("noValidExportSelection"), appReg.getString("warning.TITLE"), 4);
		            return;
		        } else
		        {
		            return;
		        }
            }
        }
    }

    private void startImportFilesOperation()
    {
        TCComponentDatasetDefinition tccomponentdatasetdefinition = null;
        com.teamcenter.rac.kernel.NamedReferenceContext anamedreferencecontext[] = null;
        try
        {
            tccomponentdatasetdefinition = datasetComponent.getDatasetDefinitionComponent();
            anamedreferencecontext = tccomponentdatasetdefinition.getNamedReferenceContexts();
        }
        catch(Exception exception)
        {
            MessageBox.post(parent, exception);
        }
        if(anamedreferencecontext == null || anamedreferencecontext.length == 0)
        {
            MessageBox.post(parent, appReg.getString("noRefTypeFound"), appReg.getString("warning.TITLE"), 4);
            return;
        }
        Frame frame = Utilities.getCurrentFrame();
        TCFileSelectorService tcfileselectorservice = TCFileSelectorService.getInstance();
        IFilesSelector ifilesselector = tcfileselectorservice.getFileSelector();
        if(ifilesselector == null)
        {
	        ImportFilesFileChooser importfilesfilechooser = new ImportFilesFileChooser(tccomponentdatasetdefinition, frame);
	        int i = importfilesfilechooser.showDialog(frame, null);
	        if(i == 0)
	        {
	            File file = importfilesfilechooser.getSelectedFile();
	            String s = importfilesfilechooser.getType();
	            String s1 = importfilesfilechooser.getReferenceType();
	            if(file != null)
	            {
	                Utilities.setCookie("filechooser", true, "DatasetImport.DIR", file.getParent());
	                char c;
	                if(truncateFileName.equalsIgnoreCase("TRUE"))
	                    c = '\036';
	                else
	                    c = '\204';
	                if(file.getName().length() > c)
	                {
	                    StringBuffer stringbuffer = (new StringBuffer(appReg.getString("fileNameLengthTooLong"))).append(" (").append(c).append(").");
	                    MessageBox.post(parent, stringbuffer.toString(), appReg.getString("warning.TITLE"), 4);
	                } else
	                {
	                    ImportFilesOperation importfilesoperation = new ImportFilesOperation(datasetComponent, file, s, null, s1, Utilities.getCurrentFrame());
	                    session.queueOperation(importfilesoperation);
	                }
	            } else
	            {
	                MessageBox.post(parent, appReg.getString("noImportFileSelection"), appReg.getString("warning.TITLE"), 4);
	            }
	        }
        } else {
        	try
            {
                logger.debug((new StringBuilder()).append("Delegating file selection to extensionPointImplementation class: ").append(ifilesselector.getClass()).toString());
                ifilesselector.initialize(tccomponentdatasetdefinition, null, null);
                java.util.List list = ifilesselector.getSelectedFiles();
                if(list != null && !list.isEmpty())
                {
                    logger.debug("File descriptor information for files selected by custom code");
                    TCFileDescriptor tcfiledescriptor1;
                    for(Iterator iterator = list.iterator(); iterator.hasNext(); logger.debug(tcfiledescriptor1))
                        tcfiledescriptor1 = (TCFileDescriptor)iterator.next();

                    TCFileDescriptor tcfiledescriptor = (TCFileDescriptor)list.get(0);
                    if(tcfiledescriptor != null)
                    {
                        File file1 = tcfiledescriptor.getFile();
                        if(file1 != null)
                            Utilities.setCookie("filechooser", true, "DatasetImport.DIR", file1.getParent());
                        else
                            logger.error("File object in TCFileDescriptor return by custom code is null");
                    } else
                    {
                        logger.error("TCFileDescriptor object in List<TCFileDescriptor> returned by custom code is null");
                    }
                } else
                {
                    logger.debug("Custom code did not return any information about selected files. Looks like user has not selected any files");
                    if(list == null)
                        logger.debug("getSelectedFiles returned null");
                    else
                        logger.debug("getSelectedFiles returned empty list");
                }
                ImportFilesOperation importfilesoperation = new ImportFilesOperation(datasetComponent, ifilesselector, null, Utilities.getCurrentFrame());
                session.queueOperation(importfilesoperation);
            }
            catch(Exception exception1)
            {
                MessageBox messagebox = new MessageBox(parent, exception1);
                messagebox.setModal(true);
                messagebox.setVisible(true);
                return;
            }
        }
    }

    private void startOpenFileOperation()
    {
        int ai[] = checkTableSelection(false, true);
        if(ai == null)
            return;
        Registry registry = Registry.getRegistry("com.teamcenter.rac.common.actions.actions");
        TCComponent tccomponent = model.getComponentAt(ai[0]);
        if(tccomponent instanceof TCComponentTcFile)
        {
            String s = (String)model.getValueAt(ai[0], "object_name");
            new ToolSelectedDialog(Utilities.getCurrentFrame(), s);
            if(selectedToolComponent == null)
                return;
            String s1 = (String)model.getValueAt(ai[0], "original_file_name");
            AbstractAIFCommand abstractaifcommand1 = (AbstractAIFCommand)registry.newInstanceFor("openAsCommand", new Object[] {
                datasetComponent, selectedToolComponent, null, s1, Utilities.getParentFrame(this)
            });
            abstractaifcommand1.executeModeless();
        } else
        if(tccomponent != null)
        {
            com.teamcenter.rac.aif.AIFDesktop aifdesktop = AIFUtility.getActiveDesktop();
            AbstractAIFCommand abstractaifcommand = (AbstractAIFCommand)registry.newInstanceFor("openCommand", new Object[] {
                aifdesktop, tccomponent
            });
            abstractaifcommand.executeModeless();
        }
    }

    private void startCutNamedRefOperation()
    {
        int ai[] = checkTableSelection(true, true);
        if(ai == null)
            return;
        ArrayList arraylist = new ArrayList();
        ArrayList arraylist1 = new ArrayList();
        ArrayList arraylist2 = new ArrayList();
        ArrayList arraylist3 = new ArrayList();
        int ai1[] = ai;
        int l = ai1.length;
        for(int i1 = 0; i1 < l; i1++)
        {
            int j1 = ai1[i1];
            TCComponent tccomponent = model.getComponentAt(j1);
            if(tccomponent instanceof TCComponentTcFile)
            {
                arraylist.add(tccomponent);
                String s = (String)model.getValueAt(j1, "object_name");
                String s2 = (String)model.getValueAt(j1, "original_file_name");
                arraylist1.add(s);
                arraylist2.add(s2);
                continue;
            }
            String s1 = model.getNamedRefType(tccomponent);
            if(s1 == null)
            {
                MessageBox.post(parent, (new StringBuilder()).append(appReg.getString("cannotCut")).append(" ").append(tccomponent.toString()).toString(), appReg.getString("warning.TITLE"), 4);
            } else
            {
                arraylist.add(tccomponent);
                arraylist3.add(s1);
            }
        }

        if(!arraylist.isEmpty())
        {
            int i = arraylist.size();
            InterfaceAIFComponent ainterfaceaifcomponent[] = (InterfaceAIFComponent[])(InterfaceAIFComponent[])arraylist.toArray(new InterfaceAIFComponent[i]);
            Registry registry = Registry.getRegistry("com.teamcenter.rac.common.actions.actions");
            AbstractAIFCommand abstractaifcommand = (AbstractAIFCommand)registry.newInstanceFor("copyCommand", new Object[] {
                ainterfaceaifcomponent
            });
            abstractaifcommand.executeModeless();
        }
        try
        {
            sourceClassificationString = datasetComponent.getProperties(prop);
        }
        catch(TCException tcexception)
        {
            MessageBox.post(parent, tcexception);
        }
        if(!arraylist1.isEmpty())
        {
            int j = arraylist1.size();
            String as[] = (String[])(String[])arraylist1.toArray(new String[j]);
            String as2[] = (String[])(String[])arraylist2.toArray(new String[j]);
            RemoveNamedRefOperation removenamedrefoperation1 = new RemoveNamedRefOperation(datasetComponent, as, as2, Utilities.getCurrentFrame());
            session.queueOperation(removenamedrefoperation1);
        }
        if(!arraylist3.isEmpty())
        {
            int k = arraylist3.size();
            String as1[] = (String[])(String[])arraylist3.toArray(new String[k]);
            RemoveNamedRefOperation removenamedrefoperation = new RemoveNamedRefOperation(datasetComponent, as1, Utilities.getCurrentFrame());
            session.queueOperation(removenamedrefoperation);
        }
    }

    private void startDeleteNamedRefOperation()
    {
        int ai[] = checkTableSelection(true, true);
        if(ai == null)
            return;
        if(ConfirmationDialog.post((Frame)parent, appReg.getString("delete.TITLE"), appReg.getString("delete")) != 2)
            return;
        ArrayList arraylist = new ArrayList();
        ArrayList arraylist1 = new ArrayList();
        ArrayList arraylist2 = new ArrayList();
        ArrayList arraylist3 = new ArrayList();
        int ai1[] = ai;
        int k = ai1.length;
        for(int l = 0; l < k; l++)
        {
            int i1 = ai1[l];
            TCComponent tccomponent = model.getComponentAt(i1);
            if(tccomponent instanceof TCComponentTcFile)
            {
                arraylist.add(tccomponent);
                String s = (String)model.getValueAt(i1, "object_name");
                String s2 = (String)model.getValueAt(i1, "original_file_name");
                
                // CAD File은 삭제하지 않는다.
                String sType = datasetComponent.getType();
                if(sType.equalsIgnoreCase("CATPart") || sType.equalsIgnoreCase("CATProduct") || sType.equalsIgnoreCase("CATDrawing")
                		|| sType.equalsIgnoreCase("catia")) {
                	MessageBox.post(AIFUtility.getActiveDesktop().getShell(), appReg.getString("NoDeleteCADDataFile.MESSAGE"), "Alert", MessageBox.WARNING);
                	return;
                }
                
                arraylist1.add(s);
                arraylist2.add(s2);
                continue;
            }
            String s1 = model.getNamedRefType(tccomponent);
            if(s1 == null)
            {
                MessageBox.post(parent, (new StringBuilder()).append(appReg.getString("cannotDelete")).append(" ").append(tccomponent.toString()).toString(), appReg.getString("warning.TITLE"), 4);
            } else
            {
                arraylist.add(tccomponent);
                arraylist3.add(s1);
            }
        }

        if(!arraylist1.isEmpty())
        {
            arraylist2.add(":::::::::://////////\\\\\\\\\\");
            arraylist1.add(":::::::::://////////\\\\\\\\\\");
            int i = arraylist1.size();
            String as[] = (String[])(String[])arraylist1.toArray(new String[i]);
            String as2[] = (String[])(String[])arraylist2.toArray(new String[i]);
            RemoveNamedRefOperation removenamedrefoperation1 = new RemoveNamedRefOperation(datasetComponent, as, as2, Utilities.getCurrentFrame());
            session.queueOperation(removenamedrefoperation1);
        }
        if(!arraylist3.isEmpty())
        {
            int j = arraylist3.size();
            String as1[] = (String[])(String[])arraylist3.toArray(new String[j]);
            RemoveNamedRefOperation removenamedrefoperation = new RemoveNamedRefOperation(datasetComponent, as1, Utilities.getCurrentFrame());
            session.queueOperation(removenamedrefoperation);
        }
    }

    private void startCopyOperation()
    {
        int ai[] = checkTableSelection(true, true);
        if(ai == null)
            return;
        int i = ai.length;
        TCComponent atccomponent[] = new TCComponent[i];
        for(int j = 0; j < i; j++)
        {
            atccomponent[j] = model.getComponentAt(ai[j]);
            atccomponent[j].setClientObject(model.getValueAt(ai[j], "object_name"));
        }

        try
        {
            sourceClassificationString = datasetComponent.getProperties(prop);
        }
        catch(TCException tcexception)
        {
            MessageBox.post(parent, tcexception);
        }
        Registry registry = Registry.getRegistry("com.teamcenter.rac.common.actions.actions");
        AbstractAIFCommand abstractaifcommand = (AbstractAIFCommand)registry.newInstanceFor("copyCommand", new Object[] {
            atccomponent
        });
        abstractaifcommand.executeModeless();
    }

    private void startPasteOperation()
    {
        AIFClipboard aifclipboard = AIFPortal.getClipboard();
        Transferable transferable = aifclipboard.getContents(this);
        if(transferable == null)
        {
            MessageBox.post(parent, appReg.getString("clipboardEmpty"), appReg.getString("warning.TITLE"), 4);
            return;
        }
        Vector vector = new Vector();
        try
        {
            vector = (Vector)transferable.getTransferData(new DataFlavor(java.util.Vector.class, "AIF Vector"));
        }
        catch(Exception exception)
        {
            MessageBox.post(parent, exception);
            return;
        }
        if(vector == null || vector.isEmpty())
        {
            MessageBox.post(parent, appReg.getString("clipboardEmpty"), appReg.getString("warning.TITLE"), 4);
            return;
        }
        Vector vector1 = new Vector();
        for(int i = 0; i < vector.size(); i++)
            if(vector.elementAt(i) instanceof TCComponent)
                vector1.addElement(vector.elementAt(i));

        try
        {
            destClassificationString = datasetComponent.getProperties(prop);
        }
        catch(TCException tcexception)
        {
            MessageBox.post(parent, tcexception);
        }
        if(sourceClassificationString != null && destClassificationString != null && (!sourceClassificationString[0].equals(destClassificationString[0]) || !sourceClassificationString[1].equals(destClassificationString[1])))
        {
            MessageBox.post(parent, appReg.getString("incorrectClassification"), appReg.getString("error.TITLE"), 1);
            return;
        }
        if(vector1.isEmpty())
        {
            MessageBox.post(parent, appReg.getString("cannotPaste"), appReg.getString("warning.TITLE"), 4);
            return;
        } else
        {
            TCComponent atccomponent[] = (TCComponent[])(TCComponent[])vector1.toArray(new TCComponent[vector1.size()]);
            PasteNamedReferencesDialog pastenamedreferencesdialog = new PasteNamedReferencesDialog(datasetComponent, atccomponent, Utilities.getCurrentFrame());
            pastenamedreferencesdialog.setVisible(true);
            return;
        }
    }

    private void startModifyFileName(TCComponent tccomponent, String s)
    {
        if(tccomponent == null || s == null || s.length() == 0)
            return;
        String s1 =null;
		try {
			s1 = tccomponent.getProperty("original_file_name");
		} catch (TCException e) {
			e.printStackTrace();
		}
        if(s1.equals(s))
            return;
        try
        {
            if(tccomponent instanceof TCComponentTcFile)
            {
                TCComponentTcFile tccomponenttcfile = (TCComponentTcFile)tccomponent;
                tccomponenttcfile.setOriginalFileName(datasetComponent, s);
            } else
            {
                tccomponent.setProperty("object_name", s);
                tccomponent.firePropertyChangeEvent("object_name");
            }
        }
        catch(TCException tcexception)
        {
            MessageBox.post(parent, tcexception);
        }
        return;
    }

    private void loadNamedReferences()
    {
        String s = (new StringBuilder()).append(appReg.getString("loadingNamedRef")).append(datasetComponent.toString()).toString();
        session.queueOperation(new AbstractAIFOperation(s) {

            public void executeOperation()
            {
                tableScrollPane.setCursor(Cursor.getPredefinedCursor(3));
                TCComponent atccomponent[];
                try
                {
                    atccomponent = datasetComponent.getNamedReferences();
                }
                catch(TCException tcexception)
                {
                    MessageBox.post(parent, tcexception);
                    tableScrollPane.setCursor(Cursor.getPredefinedCursor(0));
                    return;
                }
                if(atccomponent == null || atccomponent.length == 0)
                {
                    tableScrollPane.setCursor(Cursor.getPredefinedCursor(0));
                    return;
                }
                try
                {
                    model.removeAllRows();
                    TCComponent atccomponent1[] = atccomponent;
                    int i = atccomponent1.length;
                    for(int j = 0; j < i; j++)
                    {
                        TCComponent tccomponent = atccomponent1[j];
                        model.addRow(tccomponent);
                    }

                }
                catch(Exception exception)
                {
                    MessageBox.post(parent, exception);
                }
                tableScrollPane.setCursor(Cursor.getPredefinedCursor(0));
                tableScrollPane.validate();
                tableScrollPane.repaint();
                table.validate();
                table.repaint();
            }

//            final NamedReferencesDialog this$0;

//            
//            {
//                this$0 = NamedReferencesDialog.this;
//                super(s);
//            }
        }
);
    }

    public void processComponentEvents(AIFComponentEvent aaifcomponentevent[])
    {
        AIFComponentEvent aaifcomponentevent1[] = aaifcomponentevent;
        int i = aaifcomponentevent1.length;
        for(int j = 0; j < i; j++)
        {
            AIFComponentEvent aifcomponentevent = aaifcomponentevent1[j];
            if(!(aifcomponentevent instanceof AIFComponentChangeEvent))
                continue;
            TCComponent tccomponent = (TCComponent)aifcomponentevent.getComponent();
            if(!(tccomponent instanceof TCComponentDataset))
                continue;
            TCComponentDataset tccomponentdataset = (TCComponentDataset)tccomponent;
            if(tccomponentdataset != datasetComponent)
                continue;
            
            Utilities.invokeLater(new Runnable() {

                public void run()
                {
                    model.removeAllRows();
                }
            });
            
//            model.removeAllRows();
            loadNamedReferences();
            break;
        }

    }

    protected String getDefaultExportDirectory()
    {
        String s = Utilities.getCookie("filechooser", "DatasetExport.DIR", true);
        if(s == null || s.length() == 0)
        {
            TCPreferenceService tcpreferenceservice = ((TCSession)session).getPreferenceService();
            //s = tcpreferenceservice.getString(0, "defaultExportDirectory");
            s = tcpreferenceservice.getStringValue("defaultExportDirectory");
        }
        if(s != null)
        {
            s = s.trim();
            if(s.length() == 0)
                s = null;
        }
        return s;
    }

    private void validateButtons()
    {
        boolean flag = !table.getSelectionModel().isSelectionEmpty();
        exportButton.setEnabled(flag);
        cutButton.setEnabled(flag);
        deleteButton.setEnabled(flag);
        copyButton.setEnabled(flag);
        openButton.setEnabled(flag);
        
        try
        {
            if(datasetComponent.isTypeOf("FullText"))
            {
                deleteButton.setEnabled(false);
                cutButton.setEnabled(false);
                importButton.setEnabled(false);
                pasteButton.setEnabled(false);
            }
        }
        catch(Exception exception)
        {
            MessageBox.post(parent, exception);
        }
    }

    private static final Logger logger = Logger.getLogger(com.teamcenter.rac.commands.namedreferences.NamedReferencesDialog.class);
    private JTable table;
    private JScrollPane tableScrollPane;
    private NamedReferencesTableModel model;
    private JButton openButton;
    private JButton importButton;
    private JButton exportButton;
    private JButton cutButton;
    private JButton copyButton;
    private JButton pasteButton;
    private JButton deleteButton;
    private Window parent;
    private AbstractAIFSession session;
    private Registry appReg;
    private TCComponentDataset datasetComponent;
    private String truncateFileName;
    private static String sourceClassificationString[];
    private static String destClassificationString[];
    String prop[] = {
        "ip_classification", "gov_classification"
    };
    public static final String OBJECT_NAME_COLUMN_ID = "object_name";
    public static final String TCFILE_NAME_COLUMN_ID = "original_file_name";
    public static final String SIZE_COLUMN_ID = "file_size";
    public static final String REMOTE_COLUMN_ID = "owning_site";
    public static final String TYPE_COLUMN_ID = "object_type";
    public static final String LASTMODIFIED_COLUMN_ID = "last_mod_date";
    public static final String VOLUME_COLUMN_ID = "volume_tag";
    public static final String REF_LIST = "ref_list";
    public static final String REF_NAMES = "ref_names";
    public static final String ADDITIONAL_COLUMN = "com.teamcenter.rac.commands.namedreferences.AdditionalColumnShown";
    public static final String TC_TRUNCATE_FILE_NAME = "TC_truncate_file_name";
    protected JFileChooser exportFc;
    private TCComponent toolComponents[];
    private TCComponent selectedToolComponent;



















}


/*
	DECOMPILATION REPORT

	Decompiled from: C:\Siemens\Teamcenter\OTW8\rac\plugins\com.teamcenter.rac.common_8000.0.jar
	Total time: 172 ms
	Jad reported messages/errors:
	Couldn't resolve all exception handlers in method getNamedRefType
	Couldn't resolve all exception handlers in method startModifyFileName
	Exit status: 0
	Caught exceptions:
*/