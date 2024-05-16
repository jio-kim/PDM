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
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import com.kgm.common.utils.variant.VariantTableCellRenderer;
import com.symc.plm.me.sdv.operation.option.OptionSetOperation;
import com.symc.plm.me.utils.variant.OptionManager;
import com.symc.plm.me.utils.variant.VariantCheckBoxTableCellEditor;
import com.symc.plm.me.utils.variant.VariantCheckBoxTableCellRenderer;
import com.symc.plm.me.utils.variant.VariantNode;
import com.symc.plm.me.utils.variant.VariantOption;
import com.symc.plm.me.utils.variant.VariantValue;
import com.symc.plm.me.utils.variant.WaitProgressBar;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;

public class OptionSetDialog extends AbstractAIFDialog {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private final JPanel contentPanel = new JPanel();
    private final JPanel upperPanel = new JPanel();
    private JTable detailTable;
    private ArrayList<VariantOption> globalOptionSet; // Corporate Option 정보를 가지고 있는 Set
    private ArrayList<VariantOption> enableOptionSet; // 등록 가능한 옵션정보를 가지고 있는 Set
    private ArrayList<VariantOption> selectedLineOptionSet; // 선택한 BOM line이 가지고 있는 Option Set
    private JTree tree;
    private TCComponentBOMLine selectedLine;
    private JCheckBox showAllOptionsChk;
    private Vector<String> headerVector = new Vector<String>();
    private Vector<String> userDefineHeader = new Vector<String>();
    private Vector<String> moduleConstraintHeader = new Vector<String>();
    private int[] columnWidth = { 40, 90, 160, 120, 210 };
    private Vector<Vector<?>> allData = new Vector<Vector<?>>();
    private Vector<String[]> userDefineErrorList = null; // 사용자 정의 에러 리스트
    private Vector<String[]> moduleConstraintList = null; // 모듈 구속 조건 리스트
    private ArrayList<VariantValue> valueList = new ArrayList<VariantValue>(); // Tree에 보여지는 Option Value리스트
    protected OptionManager manager = null;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            OptionSetDialog dialog = new OptionSetDialog(null, null, null, null, null, null, null);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public OptionSetDialog(ArrayList<VariantOption> globalOptionSet, ArrayList<VariantOption> enableOptionSet, ArrayList<VariantOption> selectedLineOptionSet, TCComponentBOMLine selectedLine, Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstraintList, OptionManager manager) throws Exception {
        super(AIFUtility.getActiveDesktop().getFrame(), false);
        setTitle("Manage Options");
        // setBounds(100, 100, 950, 654);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        this.globalOptionSet = globalOptionSet;
        this.enableOptionSet = enableOptionSet;
        this.selectedLineOptionSet = selectedLineOptionSet;
        this.selectedLine = selectedLine;
        this.userDefineErrorList = userDefineErrorList;
        this.moduleConstraintList = moduleConstraintList;
        this.manager = manager;
        initTree();

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BorderLayout(0, 0));

        userDefineHeader.add("Level");
        userDefineHeader.add("Message");
        userDefineHeader.add("Way");
        userDefineHeader.add("Item");
        userDefineHeader.add("Category");
        userDefineHeader.add("Op");
        userDefineHeader.add("Code");

        moduleConstraintHeader.add("Set/Fix");
        moduleConstraintHeader.add("Category");
        moduleConstraintHeader.add("Code");
        moduleConstraintHeader.add("Way");
        moduleConstraintHeader.add("Item");
        moduleConstraintHeader.add("Category");
        moduleConstraintHeader.add("Op");
        moduleConstraintHeader.add("Code");

        upperPanel.setLayout(new BorderLayout(0, 0));
        upperPanel.setPreferredSize(new Dimension(1000, 600));

        contentPanel.add(upperPanel, BorderLayout.CENTER);

        {
            JPanel panel = new JPanel();
            upperPanel.add(panel, BorderLayout.WEST);
            panel.setLayout(new BorderLayout(0, 0));
            {
                JPanel panel_1 = new JPanel();
                panel_1.setBackground(Color.WHITE);
                panel_1.setBorder(new TitledBorder(null, "Enable Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
                panel.add(panel_1, BorderLayout.CENTER);
                panel_1.setLayout(new BorderLayout(0, 0));
                {
                    JScrollPane pane = new JScrollPane();
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
            upperPanel.add(panel, BorderLayout.CENTER);
            panel.setLayout(new BorderLayout(0, 0));
            {
                headerVector.add("USE");
                headerVector.add("CATEGORY");
                headerVector.add("CATEGORY DESC");
                headerVector.add("OPTION CODE");
                headerVector.add("OPTION DESC");

                {

                }

                if (selectedLineOptionSet != null) {
                    for (VariantOption option : selectedLineOptionSet) {
                        List<VariantValue> values = option.getValues();
                        for (VariantValue value : values) {
                            Vector<Object> row = new Vector<Object>();
                            row.add(value);
                            row.add(option.getOptionName());
                            row.add(option.getOptionDesc());
                            row.add(value.getValueName());
                            row.add(value.getValueDesc());
                            allData.add(row);
                        }
                    }
                }

                JPanel panel_1 = new JPanel();
                panel.add(panel_1, BorderLayout.CENTER);
                panel_1.setLayout(new BorderLayout(0, 0));
                {
                    JPanel panel_2 = new JPanel();
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
                            JLabel targetBOMLineLabel = new JLabel(selectedLine.toDisplayString());
                            targetBOMLineLabel.setFont(new Font(Font.SERIF, Font.BOLD, 12));
                            panel_3.add(targetBOMLineLabel);
                        }
                        {
                            showAllOptionsChk = new JCheckBox("show All Options");
                            showAllOptionsChk.setBackground(Color.WHITE);
                            panel_3.add(showAllOptionsChk);
                            showAllOptionsChk.setSelected(true);
                            showAllOptionsChk.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent arg0) {
                                    // show All Options가 체크되어 있으면, Table에서 체크 안된 모든 옵션을 보여준다.
                                    DefaultTableModel model = (DefaultTableModel) detailTable.getModel();
                                    Vector<Vector<?>> newData = getData(showAllOptionsChk.isSelected());
                                    model.setDataVector(newData, headerVector);
                                    columnInit();
                                }

                            });
                        }
                        {
                        	JLabel blankLabel = new JLabel("     ");
                        	blankLabel.setBackground(Color.WHITE);
                        	panel_3.add(blankLabel);
                        	JCheckBox allUsedCheck = new JCheckBox("All used");
                        	allUsedCheck.setBackground(Color.WHITE);
                        	panel_3.add(allUsedCheck);
                        	
                        	allUsedCheck.addActionListener(new ActionListener() {

                                @Override
                                public void actionPerformed(ActionEvent arg0) {
                                    DefaultTableModel model = (DefaultTableModel) detailTable.getModel();
                                    Vector<Vector<?>> newData = new Vector<Vector<?>>();
                                    if (((JCheckBox) arg0.getSource()).isSelected())
                                    {
                                    	// 체크되어 있으면 테이블의 모든 체크박스를 체크한다. 체크박스의 글자를 바꾼다.
                                        for (Vector<?> row : allData) {
                                            VariantValue value = (VariantValue) row.get(0);
                                            if (value.getValueStatus() == VariantValue.VALUE_USE || value.getValueStatus() == VariantValue.VALUE_NOT_USE) {
                                            	value.setValueStatus(VariantValue.VALUE_USE);
                                                newData.add(row);
                                            }
                                        }
                                        ((JCheckBox) arg0.getSource()).setText("All unused");
                                    }
                                    else
                                    {
                                        for (Vector<?> row : allData) {
                                            VariantValue value = (VariantValue) row.get(0);
                                            if (value.getValueStatus() == VariantValue.VALUE_USE || value.getValueStatus() == VariantValue.VALUE_NOT_USE) {
                                            	value.setValueStatus(VariantValue.VALUE_NOT_USE);
                                                newData.add(row);
                                            }
                                        }
                                        ((JCheckBox) arg0.getSource()).setText("All used");
                                    }
                                    model.setDataVector(newData, headerVector);
                                    columnInit();
                                }

                            });
                        }

                        TableModel model = new DefaultTableModel(getData(showAllOptionsChk.isSelected()), headerVector) {
                            private static final long serialVersionUID = 1L;

                            public Class<?> getColumnClass(int col) {
                                if (col == 0) {
                                    return VariantValue.class;
                                }
                                return String.class;
                            }

                            public boolean isCellEditable(int row, int col) {
                                if (col == 0) {
                                    if (valueList.contains(getValueAt(row, 0))) {
                                        return true;
                                    }
                                }
                                return false;
                            }
                        };

                        detailTable = new JTable(model);

                        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
                        detailTable.setRowSorter(sorter);
                        // detailTable.addMouseListener(new MouseAdapter() {
                        //
                        // @Override
                        // public void mouseReleased(MouseEvent e) {
                        // if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && e.isControlDown() == false) {
                        // removeValues();
                        // }
                        // super.mouseReleased(e);
                        // }
                        //
                        // });
                        detailTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

                        JScrollPane pane = new JScrollPane();
                        pane.setViewportView(detailTable);
                        pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
                        pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                        pane.getViewport().setBackground(Color.WHITE);
                        panel_1.add(pane);
                        columnInit();
                    }
                    {
                        JPanel panel_3 = new JPanel();
                        panel_3.setBackground(Color.WHITE);
                        panel_2.add(panel_3, BorderLayout.EAST);
                        {
                            JButton saveBtn = new JButton("Save");
                            saveBtn.setBackground(Color.WHITE);
                            saveBtn.addActionListener(new ActionListener() {
                                public void actionPerformed(ActionEvent actionevent) {
                                    final WaitProgressBar waitProgress = new WaitProgressBar(OptionSetDialog.this);
                                    waitProgress.start();
                                    OptionSetOperation operation = new OptionSetOperation(OptionSetDialog.this.selectedLineOptionSet, OptionSetDialog.this.selectedLine, OptionSetDialog.this.userDefineErrorList, OptionSetDialog.this.moduleConstraintList, allData, OptionSetDialog.this, waitProgress);
                                    OptionSetDialog.this.selectedLine.getSession().queueOperation(operation);

                                }
                            });
                            panel_3.add(saveBtn);
                        }
                    }
                }

            }
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setBackground(Color.WHITE);
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton closeButton = new JButton("Close");
                closeButton.setBackground(Color.WHITE);
                closeButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent actionevent) {
                        OptionSetDialog.this.dispose();
                    }
                });
                {
                    JButton exportBtn = new JButton("Export");
                    exportBtn.setBackground(Color.WHITE);
                    exportBtn.setIcon(new ImageIcon(OptionSetDialog.class.getResource("/com/kgm/common/images/excel_16.png")));
                    exportBtn.addActionListener(new ActionListener() {

                        @Override
                        public void actionPerformed(ActionEvent actionevent) {
                            JFileChooser fileChooser = new JFileChooser();
                            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            Calendar now = Calendar.getInstance();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
                            sdf.format(now.getTime());
                            File defaultFile = new File("Option_Manager_" + sdf.format(now.getTime()) + ".xls");
                            fileChooser.setSelectedFile(defaultFile);
                            fileChooser.setFileFilter(new FileFilter() {

                                @Override
                                public boolean accept(File f) {
                                    if (f.isFile()) {
                                        return f.getName().endsWith("xls");
                                    }
                                    return false;
                                }

                                @Override
                                public String getDescription() {
                                    return "*.xls";
                                }

                            });
                            int result = fileChooser.showSaveDialog(OptionSetDialog.this);
                            if (result == JFileChooser.APPROVE_OPTION) {
                                File selectedFile = fileChooser.getSelectedFile();
                                try {
                                    exportToExcel(selectedFile);
                                    AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
                                    aif.start();

                                } catch (Exception ioe) {
                                    ioe.printStackTrace();
                                } finally {
                                }
                            }

                        }

                    });
                    buttonPane.add(exportBtn);
                }
                closeButton.setActionCommand("Close");
                buttonPane.add(closeButton);
            }
        }
        VariantCheckBoxTableCellEditor.unUsedValueList.clear();
    }

    /**
     * 설정된 옵션을 Excel로 Export 함.
     * 
     * @param selectedFile
     * @throws IOException
     * @throws WriteException
     */
    protected void exportToExcel(File selectedFile) throws IOException, WriteException {
        WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
        // 0번째 Sheet 생성
        WritableSheet sheet = workBook.createSheet("new sheet", 0);

        WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
        cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
        Label label = null;

        WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
        headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
        headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

        int startRow = 1;
        int initColumnNum = 0;

        label = new jxl.write.Label(1, startRow, "Product Option Manager(" + this.selectedLine.toDisplayString() + ")", cellFormat);
        sheet.addCell(label);
        sheet.mergeCells(1, 1, 3, 1);

        startRow = 3;
        Vector<String> excelColumnHeader = new Vector<String>();
        excelColumnHeader.add("CATEGORY");
        excelColumnHeader.add("CATEGORY_DESC");
        excelColumnHeader.add("OPTION_CODE");
        excelColumnHeader.add("OPTION_DESC");
        excelColumnHeader.add("STATUS");
        excelColumnHeader.add("IN_USE");

        for (int i = 0; i < excelColumnHeader.size(); i++) {
            label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
            sheet.addCell(label);
            CellView cv = sheet.getColumnView(i + initColumnNum);
            cv.setAutosize(true);
            sheet.setColumnView(i + initColumnNum, cv);
        }

        int rowNum = 0;
        startRow = 4;
        Vector<Vector<?>> data = allData;
        for (int i = 0; i < data.size(); i++) {
            Vector<?> row = data.get(i);
            VariantValue value = (VariantValue) row.get(0);
            if (value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE) {
                continue;
            }

            for (int j = 0; j < row.size() + 1; j++) {
                String str = "";
                // 마지막 컬럼은 0번째 VariantValue에서 값을 가져온다.
                if (j == 5) {
                    if (value.isUsing()) {
                        str = "Y";
                    } else {
                        str = "N";
                    }

                } else if (j == 4) {
                    if (value.getValueStatus() == VariantValue.VALUE_USE) {
                        str = "Use";
                    } else if (value.getValueStatus() == VariantValue.VALUE_NOT_USE) {
                        str = "Unuse";
                    } else {
                        str = "-";
                    }

                } else {
                    str = (String) row.get(j + 1);
                }

                label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, str, cellFormat);
                sheet.addCell(label);

            }
            rowNum++;
        }

        // 셀 Merge
        int startIdxToMerge = startRow;
        int endIdxToMerge = startRow;
        for (int i = 0; i < data.size() - 1; i++) {

            Cell cell = sheet.getCell(initColumnNum, i + startRow);
            Cell nextCell = sheet.getCell(initColumnNum, i + startRow + 1);

            if (cell.getContents().equals(nextCell.getContents())) {
                endIdxToMerge = i + 1 + startRow;
            } else {
                if (startIdxToMerge < endIdxToMerge) {
                    sheet.mergeCells(initColumnNum, startIdxToMerge, initColumnNum, endIdxToMerge);
                    WritableCell wCell = sheet.getWritableCell(initColumnNum, startIdxToMerge);
                    WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
                    cf.setBorder(Border.ALL, BorderLineStyle.THIN);
                    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
                    wCell.setCellFormat(cf);

                    sheet.mergeCells(initColumnNum + 1, startIdxToMerge, initColumnNum + 1, endIdxToMerge);
                    wCell = sheet.getWritableCell(initColumnNum + 1, startIdxToMerge);
                    cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
                    cf.setBorder(Border.ALL, BorderLineStyle.THIN);
                    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
                    wCell.setCellFormat(cf);
                }
                startIdxToMerge = i + 1 + startRow;
            }
        }

        workBook.write();
        workBook.close();
    }

    /**
     * 테이블에 셋팅할 데이타를 리턴함.
     * 
     * @param bOnlyChecked
     *            true:체크된 데이타만, false:모든 데이타
     * @return
     */
    private Vector<Vector<?>> getData(boolean bOnlyChecked) {

        Vector<Vector<?>> newData = new Vector<Vector<?>>();
        if (bOnlyChecked) {
            for (Vector<?> row : allData) {
                VariantValue value = (VariantValue) row.get(0);
                if (value.getValueStatus() == VariantValue.VALUE_USE || value.getValueStatus() == VariantValue.VALUE_NOT_USE) {
                    newData.add(row);
                }
            }
        } else {
            for (Vector<?> row : allData) {
                VariantValue value = (VariantValue)row.get(0);
                if (value.getValueStatus() == VariantValue.VALUE_USE) {
                    newData.add(row);
                }
            }
        }

        return newData;

    }

    /**
     * 전체 Corporate Option 또는 Product Item에 있는 모든 옵션을 Tree로 셋팅한다.
     * 
     * @return
     */
    private JTree initTree() {

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("Option Set");
        for (VariantOption option : this.enableOptionSet) {

            VariantNode optionNode = new VariantNode(option);
            List<VariantValue> values = option.getValues();

            // 사용가능한 옵션이 존재하는경우만 Option을 추가한다.
            if (values != null && !values.isEmpty()) {
                int enableChildCount = 0;
                for (VariantValue value : option.getValues()) {
                    if (value.getValueStatus() == VariantValue.VALUE_USE) {
                        enableChildCount++;
                    }
                }
                if (enableChildCount > 0) {
                    root.add(optionNode);
                }
            }
        }
        tree = new JTree(root);
        // tree.addMouseListener(new MouseAdapter() {
        //
        // // ==> 기능과 동일
        // // 사용할 옵션으로 추가함.
        // @Override
        // public void mouseReleased(MouseEvent e) {
        // if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e) && e.isControlDown() == false) {
        // addValues();
        // }
        // super.mouseReleased(e);
        // }
        //
        // });
        // tree.setRootVisible(false);
        return tree;
    }

    /**
     * tree 초기화 및 테이블 렌더러 셋팅.
     * 
     */
    public void columnInit() {

        DefaultTreeModel treeModel = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        Enumeration<?> enums = rootNode.children();
        while (enums.hasMoreElements()) {
            VariantNode optionNode = (VariantNode) enums.nextElement();
            Enumeration<?> childEnums = optionNode.children();
            while (childEnums.hasMoreElements()) {
                VariantNode valueNode = (VariantNode) childEnums.nextElement();
                valueList.add((VariantValue) valueNode.getUserObject());
            }
        }

        TableColumnModel columnModel = detailTable.getColumnModel();
        int n = headerVector.size();
        for (int i = 0; i < n; i++) {
            columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
            columnModel.getColumn(i).setWidth(columnWidth[i]);
        }

        VariantTableCellRenderer cellRenderer = new VariantTableCellRenderer();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            TableColumn column = columnModel.getColumn(i);
            if (i == 0) {
                column.setCellRenderer(new VariantCheckBoxTableCellRenderer());
                column.setCellEditor(new VariantCheckBoxTableCellEditor(new JCheckBox()));
                continue;
            }
            column.setCellRenderer(cellRenderer);
        }
    }

    /**
     * Dialog띄울때 선택했던 BOM line의 옵션셋에서, 이름이 같은 옵션을 리턴
     * 
     * @param option
     * @return
     */
    public VariantOption getOption(VariantOption option) {

        for (VariantOption opt : OptionSetDialog.this.selectedLineOptionSet) {
            if (opt.getOptionName().equals(option.getOptionName())) {
                return opt;
            }
        }

        return option;
    }

    public ArrayList<VariantOption> getGlobalOptionSet() {
        return globalOptionSet;
    }

}
