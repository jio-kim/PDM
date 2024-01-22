/**
 * 
 */
package com.symc.plm.me.sdv.dialog.option;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.symc.plm.me.utils.variant.ConditionVector;
import com.symc.plm.me.utils.variant.OptionManager;
import com.symc.plm.me.utils.variant.VariantCheckBoxTableCellEditor;
import com.symc.plm.me.utils.variant.VariantNode;
import com.symc.plm.me.utils.variant.VariantOption;
import com.symc.plm.me.utils.variant.VariantValue;
import com.symc.plm.me.utils.variant.WaitProgressBar;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

//import com.ssangyong.common.utils.table.SortableTableModel;

/**
 * 
 * Function에 설정된 옵션의 조합으로 현재 선택한 BOM line에 컨디션을 설정한다.
 * 
 * [SR150521-012][20150522] shcho, 옵션 적용시 멀티적용 되도록 기능 개선
 * 
 * @author slobbie
 * 
 */
@SuppressWarnings("rawtypes")
public class ConditionSetDialog extends AbstractAIFDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private JTable detailTable;
    private ArrayList<VariantOption> enableOptionSet;
    private Registry registry = null;
    private JTree tree;
    private TCComponentBOMLine[] selectedLines;
    private Vector headerVector = new Vector();
    private int[] columnWidth = { 40, 100, 100, 100, 150 };
    private OptionManager manager = null;
    @SuppressWarnings("unused")
    private List<ConditionVector> conditions = null;
    private JList combinationResultList = new JList();
    private TCSession session = null;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            ConditionSetDialog dialog = new ConditionSetDialog(null, null, null, null, null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    @SuppressWarnings({ "unchecked", "serial" })
    public ConditionSetDialog(ArrayList<VariantOption> enableOptionSet, List<ConditionVector> conditions, TCComponentBOMLine[] selectedBOMLines, Vector<String[]> userDefineErrorList, OptionManager manager) throws Exception {
        super(AIFUtility.getActiveDesktop().getFrame(), false);
        setTitle("Condition Set Dialog");
        setBounds(100, 100, 950, 715);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // this.registry = Registry.getRegistry(this);
        this.registry = Registry.getRegistry("com.ssangyong.commands.variantconditionset.variantconditionset");

        this.enableOptionSet = enableOptionSet;
        this.conditions = conditions;
        this.selectedLines = selectedBOMLines;
        this.manager = manager;
        this.session = selectedBOMLines[0].getSession();
        initTree();

        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));
        {
            JPanel panel = new JPanel();
            panel.setBackground(Color.WHITE);
            contentPanel.add(panel, BorderLayout.WEST);
            panel.setLayout(new BorderLayout(0, 0));
            {
                JPanel panel_1 = new JPanel();
                panel_1.setBackground(Color.WHITE);
                panel_1.setBorder(new TitledBorder(null, "Enable Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                // panel_1.setPreferredSize(new Dimension(200, 300));
                panel.add(panel_1, BorderLayout.CENTER);
                panel_1.setLayout(new BorderLayout(0, 0));
                {
                    JScrollPane pane = new JScrollPane();
                    pane.setPreferredSize(new Dimension(300, 300));
                    pane.setViewportView(tree);
                    pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                    pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    pane.getViewport().setBackground(Color.WHITE);
                    panel_1.add(pane);

                }
            }
        }
        {
            JPanel panel = new JPanel();
            panel.setBackground(Color.WHITE);
            // panel.setPreferredSize(new Dimension(400, 10));
            contentPanel.add(panel, BorderLayout.CENTER);
            panel.setLayout(new BorderLayout(0, 0));
            {
                headerVector.add("USE");
                headerVector.add("CATEGORY");
                headerVector.add("CATEGORY DESC");
                headerVector.add("OPTION CODE");
                headerVector.add("OPTION DESC");

                {
                    JPanel panel_2 = new JPanel();
                    panel_2.setBackground(Color.WHITE);
                    panel.add(panel_2, BorderLayout.WEST);
                    panel_2.setLayout(new BorderLayout(0, 0));
                    {
                        JPanel panel_1 = new JPanel();
                        panel_1.setBackground(Color.WHITE);
                        panel_1.setPreferredSize(new Dimension(10, 50));
                        panel_2.add(panel_1, BorderLayout.NORTH);
                    }
                    {
                        JPanel panel_1 = new JPanel();
                        panel_1.setBackground(Color.WHITE);
                        panel_2.add(panel_1);
                        panel_1.setPreferredSize(new Dimension(60, 100));
                        {
                            JButton button = new JButton() {

                                @Override
                                public Dimension getPreferredSize() {
                                    return new Dimension(50, 40);
                                }

                            };
                            button.setBackground(Color.WHITE);

                            // 중간 테이블에서 우측 테이블로 ==> 기능
                            // 1. selectedLineOptionSet에서 있는지 체크하여, 있다면 그 옵션을 가져온다.
                            // 2. 존재하지 않는다면 옵션을 생성.
                            button.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent arg0) {
                                    add();
                                }
                            });
                            button.setIcon(registry.getImageIcon("ProuctOptionManageForwardArrow2.ICON"));
                            panel_1.add(button);
                        }
                        {
                            JButton button = new JButton() {

                                @Override
                                public Dimension getPreferredSize() {
                                    return new Dimension(50, 40);
                                }

                            };
                            button.setBackground(Color.WHITE);
                            // 우측 테이블에서 좌측 테이블로 <== 기능
                            button.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent actionevent) {
                                    remove();
                                }
                            });
                            button.setIcon(registry.getImageIcon("ProuctOptionManageBackArrow2.ICON"));
                            panel_1.add(button);
                        }
                    }
                }

                JPanel panel_1 = new JPanel();
                panel_1.setBackground(Color.WHITE);
                panel.add(panel_1, BorderLayout.CENTER);
                panel_1.setLayout(new BorderLayout(0, 0));
                {
                    JPanel panel_2 = new JPanel();
                    panel_2.setBackground(Color.WHITE);
                    panel_1.add(panel_2, BorderLayout.NORTH);
                    panel_2.setLayout(new BorderLayout(0, 0));
                    {
                        JPanel panel_3 = new JPanel();
                        panel_3.setBackground(Color.WHITE);
                        FlowLayout flowLayout = (FlowLayout) panel_3.getLayout();
                        flowLayout.setAlignment(FlowLayout.LEADING);
                        panel_2.add(panel_3);
                        {
                            JLabel label = new JLabel("* Target : ");
                            label.setBackground(Color.WHITE);
                            panel_3.add(label);
                        }
                        {
                            JLabel targetBOMLineLabel = new JLabel(selectedBOMLines != null ? selectedBOMLines[0].toDisplayString() : "");
                            targetBOMLineLabel.setBackground(Color.WHITE);
                            targetBOMLineLabel.setFont(new Font(Font.SERIF, Font.BOLD, 12));
                            panel_3.add(targetBOMLineLabel);
                            
                            if(selectedBOMLines.length > 1) {
                                JLabel targetBOMLineCountLabel = new JLabel(" + " + (selectedBOMLines.length - 1));
                                targetBOMLineCountLabel.setBackground(Color.WHITE);
                                targetBOMLineCountLabel.setFont(new Font(Font.SERIF, Font.BOLD, 12));
                                panel_3.add(targetBOMLineCountLabel);
                            }
                        }
                    }
                }

                TableModel model = new DefaultTableModel(null, headerVector) {
                    public Class getColumnClass(int col) {
                        if (col == 0) {
                            return VariantValue.class;
                        }
                        return String.class;
                    }

                    public boolean isCellEditable(int row, int col) {
                        return false;
                    }
                };
                detailTable = new JTable(model);

                TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
                detailTable.setRowSorter(sorter);
                detailTable.addMouseListener(new MouseAdapter() {

                    @Override
                    public void mouseReleased(MouseEvent e) {
                        if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && e.isControlDown() == false) {
                            remove();
                        }
                        super.mouseReleased(e);
                    }

                });
                JScrollPane pane = new JScrollPane();
                pane.setViewportView(detailTable);
                pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                pane.getViewport().setBackground(Color.WHITE);
                panel_1.add(pane);
                columnInit();
            }

            JPanel southPanel = new JPanel(new BorderLayout());
            southPanel.setBackground(Color.WHITE);
            JPanel centerButtonPanel = new JPanel();
            centerButtonPanel.setBackground(Color.WHITE);
            southPanel.add(centerButtonPanel, BorderLayout.NORTH);

            JButton addBtn = new JButton("Add Row");
            addBtn.setBackground(Color.WHITE);
            addBtn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // 컨디션 조합 테이블에 수집된 옵션을 컨디션으로 추가함.
                    try {
                        DefaultTableModel model = (DefaultTableModel) detailTable.getModel();
                        ConditionVector condition = ConditionSetDialog.this.manager.getConditionSet(model.getDataVector());
                        DefaultListModel listModel = (DefaultListModel) combinationResultList.getModel();
                        if (listModel == null) {
                            listModel = new DefaultListModel();
                        }
                        listModel.addElement(condition);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }

            });
            JButton delBtn = new JButton("Del Row");
            delBtn.setBackground(Color.WHITE);
            delBtn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // Combination Results 테이블에서 선택된 옵션 조합을 제거함.
//                    Object[] selectedObj = combinationResultList.getSelectedValues();
//
//                    DefaultListModel listModel = (DefaultListModel) combinationResultList.getModel();
//                    for (int i = 0; selectedObj != null && i < selectedObj.length; i++) {
//                        listModel.removeElement(selectedObj[i]);
//                    }
                	
                	List<Object> selectedObj = combinationResultList.getSelectedValuesList();
                	
                	DefaultListModel listModel = (DefaultListModel) combinationResultList.getModel();
                	for (int i = 0; selectedObj != null && i < selectedObj.size(); i++) {
                		listModel.removeElement(selectedObj.get(i));
                	}

                }

            });
            JButton clearBtn = new JButton("Clear");
            clearBtn.setBackground(Color.WHITE);
            clearBtn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // 컨디션 조합 테이블에 수집된 옵션을 모두 제거함.
                    DefaultTableModel model = (DefaultTableModel) detailTable.getModel();
                    for (int i = model.getRowCount() - 1; model != null && i >= 0; i--) {
                        model.removeRow(i);
                    }

                }

            });
            centerButtonPanel.add(addBtn);
            centerButtonPanel.add(delBtn);
            centerButtonPanel.add(clearBtn);

            JPanel centerListPanel = new JPanel(new BorderLayout());
            centerListPanel.setBackground(Color.WHITE);

            DefaultListModel listModel = new DefaultListModel();
            if(conditions != null) {
                for (ConditionVector conditionVec : conditions) {
                    listModel.addElement(conditionVec);
                }
            }
            combinationResultList.setModel(listModel);
            combinationResultList.addMouseListener(new MouseAdapter() {

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && e.isControlDown() == false) {

                        DefaultTableModel tableModel = (DefaultTableModel) detailTable.getModel();
                        for (int i = tableModel.getRowCount() - 1; tableModel != null && i >= 0; i--) {
                            tableModel.removeRow(i);
                        }
                        combinationResultList.clearSelection();
                        int selectedIdx = combinationResultList.getAnchorSelectionIndex();
                        DefaultListModel listModel = (DefaultListModel) combinationResultList.getModel();
                        ConditionVector conditions = (ConditionVector) listModel.get(selectedIdx);
                        for (ConditionElement elm : conditions) {
                            VariantValue value = ConditionSetDialog.this.manager.getValue(elm.option + ":" + elm.value);

                            if (value == null) {
                                MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.notFoundValue"), "INFORMATION", MessageBox.WARNING);
                                return;
                            }
                            VariantOption option = value.getOption();
                            Vector row = new Vector();
                            row.add(value);
                            row.add(option.getOptionName());
                            row.add(option.getOptionDesc());
                            row.add(value.getValueName());
                            row.add(value.getValueDesc());
                            tableModel.addRow(row);
                        }
                    }
                    super.mouseReleased(e);
                }

            });
            JScrollPane pane = new JScrollPane();
            pane.setPreferredSize(new Dimension(180, 200));
            pane.setViewportView(combinationResultList);
            pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
            pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            pane.getViewport().setBackground(Color.WHITE);
            centerListPanel.add(pane, BorderLayout.CENTER);
            centerListPanel.setBorder(new TitledBorder(null, "Combination Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
            southPanel.add(centerListPanel, BorderLayout.CENTER);

            contentPanel.add(southPanel, BorderLayout.SOUTH);
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setBackground(Color.WHITE);
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("Ok");
                okButton.setBackground(Color.WHITE);
                if (selectedBOMLines != null) {
                    okButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent actionevent) {

                            final WaitProgressBar waitProgress = new WaitProgressBar(ConditionSetDialog.this);
                            waitProgress.start();
                            waitProgress.setStatus("Applying.....");
                            AbstractAIFOperation operation = new AbstractAIFOperation() {

                                @Override
                                public void executeOperation() throws Exception {
                                    try {
                                        apply();
                                        waitProgress.close();
                                        ConditionSetDialog.this.dispose();
                                    } catch (TCException e) {
                                        waitProgress.setStatus(e.getDetailsMessage());
                                        waitProgress.setShowButton(true);
                                    }
                                }

                            };
                            session.queueOperation(operation);
                        }
                    });
                }

                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Close");
                cancelButton.setBackground(Color.WHITE);
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionevent) {
                        ConditionSetDialog.this.dispose();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
        VariantCheckBoxTableCellEditor.unUsedValueList.clear();
        
        if(selectedBOMLines.length > 1) {
            MessageBox.post(AIFDesktop.getActiveDesktop().getShell(), "2개 이상의 BOPLine이 선택되었습니다.", "INFORMATION", MessageBox.INFORMATION);
        }
    }

    /**
     * Condition을 적용함.
     * 
     * @throws TCException
     */
    private void apply() throws TCException {

        String lineMvl = "";
        DefaultListModel listModel = (DefaultListModel) combinationResultList.getModel();
        for (int i = 0; i < listModel.size(); i++) {
            ConditionVector condition = (ConditionVector) listModel.get(i);
            if (condition == null)
                continue;

            String tmpStr = "";
            for (int j = 0; j < condition.size(); j++) {
                ConditionElement elm = condition.get(j);
                tmpStr += (j > 0 ? " and " : "") + MVLLexer.mvlQuoteId(elm.item,true) + ":" + MVLLexer.mvlQuoteId(elm.option, false) + " = " + MVLLexer.mvlQuoteString(elm.value);
            }

            lineMvl += (!lineMvl.equals("") ? " or " : "") + tmpStr;
        }
        TCVariantService svc = session.getVariantService();
        for(TCComponentBOMLine selectedLine : selectedLines) {
            svc.setLineMvlCondition(selectedLine, lineMvl);            
        }
        System.out.println();
    }

    /**
     * 사용가능한 옵션을 보여주는 Tree를 초기화 함.(Function에 정의된 옵션만 사용가능)
     * 
     * @return
     */
    private JTree initTree() {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Option Set");
        if (this.enableOptionSet != null && this.enableOptionSet.size() > 0) {
            for (VariantOption option : this.enableOptionSet) {

                @SuppressWarnings("unused")
                String desc = option.getOptionDesc() == null || option.getOptionDesc().equals("") ? "" : " | " + option.getOptionDesc();
                VariantNode optionNode = new VariantNode(option);
                List<VariantValue> values = option.getValues();

                // 사용가능한 옵션이 존재하는경우만 Option을 추가한다.
                if (values != null && !values.isEmpty()) {
                    int enableChildCount = 0;
                    for (VariantValue value : values) {
                        if (value.getValueStatus() == VariantValue.VALUE_USE) {
                            enableChildCount++;
                        }
                    }
                    if (enableChildCount > 0) {
                        root.add(optionNode);
                    }
                }
            }
        }

        tree = new JTree(root);
        tree.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && e.isControlDown() == false) {
                    add();
                }
                super.mouseReleased(e);
            }

        });
        for (int i = 0; i < tree.getRowCount(); i++) {
            tree.expandRow(i);
        }
        return tree;
    }

    /**
     * 해당 옵션이 이미 포함되어 있는지 확인
     * 
     * @param value
     * @param data
     * @return
     */
    @SuppressWarnings("unchecked")
    private boolean isValidAndCheck(VariantValue value, Vector<Vector> data) {

        for (int i = 0; i < data.size(); i++) {
            Vector row = data.get(i);
            if (value.equals(row.get(0))) {
                if (value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE)
                    value.setValueStatus(VariantValue.VALUE_USE);
                return false;
            } else {
                VariantValue val = (VariantValue) row.get(0);
                if (value.getOption().equals(val.getOption())) {
                    row.removeAllElements();
                    row.add(value);
                    row.add(value.getOption().getOptionName());
                    row.add(value.getOption().getOptionDesc());
                    row.add(value.getValueName());
                    row.add(value.getValueDesc());
                    data.remove(i);
                    data.insertElementAt(row, i);
                    DefaultTableModel model = (DefaultTableModel) detailTable.getModel();
                    model.fireTableDataChanged();
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 컬럼 사이즈 초기화
     */
    public void columnInit() {
        TableColumnModel columnModel = detailTable.getColumnModel();
        int n = headerVector.size();
        for (int i = 0; i < n; i++) {
            columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
            columnModel.getColumn(i).setWidth(columnWidth[i]);
        }
        columnModel.removeColumn(columnModel.getColumn(columnModel.getColumnIndex("USE")));
    }

    /**
     * Condition 조합테이블에 옵션값을 추가함.
     */
    @SuppressWarnings({ "unchecked" })
    private void add() {
        TreePath[] paths = tree.getSelectionPaths();
        for (int i = 0; paths != null && i < paths.length; i++) {
            TreePath path = paths[i];
            VariantNode node = (VariantNode) path.getLastPathComponent();
            Object obj = node.getUserObject();
            DefaultTableModel model = (DefaultTableModel) detailTable.getModel();
            if (obj instanceof VariantValue) {

                VariantValue value = (VariantValue) obj;
                VariantOption option = value.getOption();
                if (isValidAndCheck(value, model.getDataVector())) {
                    value.setNew(true);
                    value.setValueStatus(VariantValue.VALUE_USE);
                    Vector row = new Vector();
                    row.add(value);
                    row.add(option.getOptionName());
                    row.add(option.getOptionDesc());
                    row.add(value.getValueName());
                    row.add(value.getValueDesc());

                    model.addRow(row);
                }
            }
        }
    }

    /**
     * 추가된 Conditiond을 제거함.
     */
    @SuppressWarnings({ "unused", "unchecked" })
    private void remove() {
        int[] selectedIdxs = detailTable.getSelectedRows();
        DefaultTableModel model = (DefaultTableModel) detailTable.getModel();
        ArrayList<VariantValue> selectedValues = new ArrayList<VariantValue>();
        for (int i = selectedIdxs.length - 1; i >= 0; i--) {
            VariantValue value = (VariantValue) model.getValueAt(selectedIdxs[i], 0);
            selectedValues.add(value);
            if (value.isNew() || (!value.isNew() && !value.isUsing())) {

                VariantOption option = value.getOption();
                for (int j = model.getRowCount() - 1; j >= 0; j--) {
                    VariantValue val = (VariantValue) model.getValueAt(j, 0);
                    if (val.equals(value)) {
                        model.removeRow(j);
                        break;
                    }
                }

                // 현재의 테이블 모델에서 모든 Value를 모두 빼면 사용되지 않는 옵션은 AllData에서 빼야함.
                boolean bNeedDataRemove = true;
                for (Vector row : (Vector<Vector>) model.getDataVector()) {
                    if (row.get(1).equals(option.getOptionName())) {
                        bNeedDataRemove = false;
                        break;
                    }
                }

            }
        }

        model.fireTableDataChanged();
    }
}
