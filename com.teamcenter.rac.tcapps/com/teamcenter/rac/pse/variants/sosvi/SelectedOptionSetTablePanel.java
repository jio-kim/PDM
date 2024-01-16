// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) 

package com.teamcenter.rac.pse.variants.sosvi;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;

import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.DefaultVariantConfigCheckBox;
import com.teamcenter.rac.pse.variants.FilterUnconfiguredDueToClassicVariantsCheckBox;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.treetable.table.JamSwingTable;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.VerticalLayout;

// Referenced classes of package com.teamcenter.rac.pse.variants.sosvi:
//            SelectedOptionSetDialog, SelectedOptionSet, OptionFilterCheckbox, ValueSelection, 
//            SelectedOptionSetTableModel

@SuppressWarnings("serial")
class SelectedOptionSetTablePanel extends JPanel {
    private class SOSTableCellRenderer extends DefaultTableCellRenderer {

        public Component getTableCellRendererComponent(JTable jtable, Object obj, boolean flag, boolean flag1, int i, int j) {
            ValueSelection valueselection = sosDialog.getSOS().getFilteredValueSelections()[i];
            JLabel jlabel = (JLabel) super.getTableCellRendererComponent(jtable, obj, flag, flag1, i, j);
            jlabel.setToolTipText(valueselection.getDefinition());
            jlabel.setEnabled(valueselection.isPublic() && !sosDialog.isVi());
            if (sosDialog.isVi())
                jlabel.setForeground(Color.lightGray);
            else
                jlabel.setForeground(flag ? Color.white : Color.black);
            return jlabel;
        }

    }

    private class SelectedOptionSetTable extends JamSwingTable {
    	private String[][] errorChecks;
    	
        void refresh() {
            ((SelectedOptionSetTableModel) getModel()).fireTableDataChanged();
        }

        void refreshForNewBOMLine() {
            ((SelectedOptionSetTableModel) getModel()).setSelectedOptionSet(sosDialog.getSOS());
        }

        public boolean getSurrendersFocusOnKeystroke() {
            return true;
        }
        
        public void setErrorChecks(String[][] errorChecks) {
        	this.errorChecks = errorChecks;
        }

        public TableCellEditor getCellEditor(int i, int j) {
            if (j == 1) {
                return null;
            } else {
//                ValueSelection valueselection = sosDialog.getSOS().getFilteredValueSelections()[i];
//                return valueselection.getValueSelectionTableCellEditor();

                // 테이블 셀 클릭시에 기존에 선택된 값이 선택되어 있도록 수정함.
                // ======= Start =========================
                ValueSelection valueselection = sosDialog.getSOS().getFilteredValueSelections()[i];
                Object curValue = sosTable.getValueAt(i, j);
                if (curValue != null && !curValue.equals("")) {
                    if (valueselection instanceof FixedValueSelection) {
                        FixedValueSelection fixedSelection = (FixedValueSelection) valueselection;
                        ((FixedValueSelection)valueselection).setErrorChecks(errorChecks);
                        TableCellEditor editor = valueselection.getValueSelectionTableCellEditor();
                        @SuppressWarnings("rawtypes")
                        JComboBox combo = fixedSelection.getJcombobox();

                        for (int k = 0; k < combo.getModel().getSize(); k++) {

                            Object tmpObj = combo.getItemAt(k);
                            if (tmpObj instanceof String) {
                                int idx = ((String) tmpObj).indexOf("_");
                                if (idx > -1) {
                                    String str = ((String) tmpObj).substring(0, idx);
                                    if (str.equals(curValue)) {
                                        combo.setSelectedIndex(k);
                                        break;
                                    }
                                }
                            }
                        }

                        return editor;

                    }
                }
                return valueselection.getValueSelectionTableCellEditor();
                // ======= End =========================
            }
        }

        public void resizeColumnToFit(int i) {
        }

        private static final int PREFERRED_FIRST_COL_WIDTH = 175;

        SelectedOptionSetTable() {
            super(new SelectedOptionSetTableModel(sosDialog.getSOS()));
            getColumnModel().getColumn(1).setPreferredWidth(PREFERRED_FIRST_COL_WIDTH);
            setDefaultRenderer(getColumnClass(1), new SOSTableCellRenderer());
            setEnabled(!sosDialog.isVi());
        }
    }

    SelectedOptionSetTablePanel(SelectedOptionSetDialog selectedoptionsetdialog) {
        super(new VerticalLayout(2, 7, 7, 7, 7));
        sosDialog = selectedoptionsetdialog;
        Registry registry = Registry.getRegistry(this);
        JPanel jpanel = new JPanel(new PropertyLayout());
        jpanel.add("1.1.left", new JLabel(registry.getString("Setfix.label")));
        typeCombo = new JComboBox<String>(types);
        if (selectedoptionsetdialog.getSOS().doFixNotSet())
            typeCombo.setSelectedItem(FIX);
        else
            typeCombo.setSelectedItem(SET);
        typeCombo.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent actionevent) {
                setfixChanged();
            }

        });
        jpanel.add("1.2.left", typeCombo);
        add(TOP, jpanel);
        JLabel jlabel = new JLabel(registry.getString("sosTable.header"));
        add(TOP, jlabel);
        sosTable = new SelectedOptionSetTable();
        JScrollPane jscrollpane = new JScrollPane(sosTable);
        add("unbound", jscrollpane);
        jscrollpane.setPreferredSize(new Dimension(SCROLLPANE_WIDTH, SCROLLPANE_HEIGHT));
        AbstractTableModel abstracttablemodel = (AbstractTableModel) sosTable.getModel();
        optFilterCheckBox = new OptionFilterCheckbox(selectedoptionsetdialog.getSOS(), abstracttablemodel);
        add(BOTTOM, optFilterCheckBox);
        final TCPreferenceService prefService = ((TCSession) selectedoptionsetdialog.getApplication().getSession()).getPreferenceService();
        boolean flag = sosDialog.isBOMWinInClassicVariantConfigToLoadMode();
        if (!flag) {
//            boolean flag1 = prefService.isTrue(1, "PSEEnableFilteringUnconfigdDueToClassicVariantsPref");
            Boolean flag1 = prefService.getLogicalValueAtLocation("PSEEnableFilteringUnconfigdDueToClassicVariantsPref", TCPreferenceLocation.USER_LOCATION);
            if (flag1 == null) {
                flag1 = false;
            }

            if (flag1) {
                SelectedOptionSet selectedoptionset = selectedoptionsetdialog.getSOS();
                flag = selectedoptionset.hasLegacyOptions();
            }
        }
        if (flag) {
            m_filterUnconfiguredDueToClassicVariantsCheckBox = new FilterUnconfiguredDueToClassicVariantsCheckBox(selectedoptionsetdialog);
            add(BOTTOM, m_filterUnconfiguredDueToClassicVariantsCheckBox);
            selectedoptionsetdialog.setFilterUnconfiguredDueToClassicVariantsCheckBox(m_filterUnconfiguredDueToClassicVariantsCheckBox);
            m_filterUnconfiguredDueToClassicVariantsCheckBox.setEnabled(false);
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    DefaultVariantConfigCheckBox.createCheckBox(sosDialog, SelectedOptionSetTablePanel.this, prefService);
                }

            });
        }
    }

    void refresh() {
        sosTable.refresh();
    }

    void refreshForNewBOMLine() {
        sosDialog.getSOS().setShowOnlyPublic(optFilterCheckBox.isSelected());
        AbstractTableModel abstracttablemodel = (AbstractTableModel) sosTable.getModel();
        optFilterCheckBox.reset(sosDialog.getSOS(), abstracttablemodel);
        sosTable.refreshForNewBOMLine();
    }

    private void setfixChanged() {
        String s = (String) typeCombo.getSelectedItem();
        if (s.equals(FIX))
            sosDialog.getSOS().setFixValues(true);
        else
            sosDialog.getSOS().setFixValues(false);
    }

    public SelectedOptionSetTable getSosTable() {
        return sosTable;
    }
    
    public SelectedOptionSetTable getTable() {
    	return sosTable;
    }

    /**
     * 테이블 값을 변경하기 위해 추가함 메서드. 사용여부는 차후에 확인 필요함.
     * 
     * @param value
     * @param row
     * @param column
     */
    public void setValueAt(Object value, int row, int column) {

        ValueSelection valueselection = sosDialog.getSOS().getFilteredValueSelections()[row];
        try {
            valueselection.setValue(value);
        } catch (InvalidValueException e) {
            e.printStackTrace();
        } catch (TCException e) {
            e.printStackTrace();
        } finally {
            sosTable.repaint();
        }
    }

    public void setValue(HashMap<String, String> standardVariantOptions) {

        ValueSelection[] valueSelections = sosDialog.getSOS().getFilteredValueSelections();
        for (int i = 0; valueSelections != null && i < valueSelections.length; i++) {

            String value = standardVariantOptions.get(valueSelections[i].getName());
            try {
                if (value != null)
                    valueSelections[i].setValue(value);
            } catch (InvalidValueException e) {
                e.printStackTrace();
            } catch (TCException e) {
                e.printStackTrace();
            } finally {
            }
        }
    }
    
    /**
     * [SR150522-021][2015.06.01][jclee] 속도 개선
     * Parse Condition
     * @return
     * @throws Exception
     */
    public void parseCondition() throws Exception {
        try {
        	TCSession session = (TCSession) AIFUtility.getSessionManager().getDefaultSession();
        	TCVariantService variantService = session.getVariantService();
        	
        	TCComponent component = getTarget();
        	SYMCBOMLine bomLine = null;
        	if (component instanceof SYMCBOMLine) {
        		// [SR없음][2015.06.30][shcho] MProduct 일 경우 Validation 기능 Bypass
        		if (((SYMCBOMLine) component).getItem().getType().equals("M7_MfgProduct")) {
					return;
				}
        		
				SYMCBOMLine blTarget = (SYMCBOMLine)component;
				bomLine = (SYMCBOMLine) blTarget.window().getTopBOMLine();
			}
        	
        	if (bomLine == null) {
        		throw new TCException("Structure Manager에서 BOM Line을 읽어오는데 실패하였습니다.");
			}
        	
            String lineMvl = variantService.askLineMvl(bomLine);
            ConstraintsModel constraintsModel = new ConstraintsModel(bomLine.getItem().getProperty("item_id"), lineMvl, new HashMap(), bomLine, variantService);

            if (!constraintsModel.parse()) {
                throw new TCException("Condition을 파싱 할 수 없습니다.");
            }

            String[][] errorChecks = constraintsModel.errorChecksTableData();
            getSosTable().setErrorChecks(errorChecks);
            return;
        } catch (TCException tce) {
            tce.printStackTrace();
        }
        
        return;
    }
    
    public TCComponent getTarget()
    {
        TCComponent target = null;
        AbstractAIFUIApplication abstractaifuiapplication = AIFDesktop.getActiveDesktop().getCurrentApplication();

        AIFComponentContext aaifcomponentcontext[] = abstractaifuiapplication.getTargetContexts();

        if(aaifcomponentcontext != null && aaifcomponentcontext.length == 1)
        {
            target = (TCComponent) aaifcomponentcontext[0].getComponent();
            return target;
        }
        
        return target;
    }

    private static final String BOTTOM = "bottom";
    private static final String TOP = "top";
    private static final String FIX = "fix";
    private static final String SET = "set";
    private SelectedOptionSetDialog sosDialog;
    private SelectedOptionSetTable sosTable;
    private OptionFilterCheckbox optFilterCheckBox;
    private FilterUnconfiguredDueToClassicVariantsCheckBox m_filterUnconfiguredDueToClassicVariantsCheckBox;
    private static final int SCROLLPANE_WIDTH = 350;
    private static final int SCROLLPANE_HEIGHT = 250;
    private static final String types[] = { SET, FIX };
    private JComboBox<String> typeCombo;

}
