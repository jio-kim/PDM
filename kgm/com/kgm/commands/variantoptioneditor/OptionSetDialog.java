package com.kgm.commands.variantoptioneditor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

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

import com.kgm.commands.optiondefine.VariantOptionDefinitionDialog;
import com.kgm.commands.variantoptioneditor.tree.VariantNode;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantCheckBoxTableCellEditor;
import com.kgm.common.utils.variant.VariantCheckBoxTableCellRenderer;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantTableCellRenderer;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.common.BOMTreeTable;
import com.teamcenter.rac.pse.services.PSEApplicationService;
import com.teamcenter.rac.pse.variants.modularvariants.ConstraintsModel;
import com.teamcenter.rac.pse.variants.modularvariants.EditConstraintsDialog;
import com.teamcenter.rac.pse.variants.modularvariants.EditErrorCheckDialog;
import com.teamcenter.rac.pse.variants.modularvariants.ModularOptionModel;
import com.teamcenter.rac.util.Registry;

/**
 * Product, Variant, Function�� �ɼ��� ����.
 * ���������δ� Option Validation�� �޽����� �߰��Ͽ�,
 * �ش��ϴ� �ɼǰ��� ǥ��� �� �ֵ��� ��.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"serial", "rawtypes", "unused", "unchecked"})
public class OptionSetDialog extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private final JPanel upperPanel = new JPanel();
	private final JPanel bottomPanel = new JPanel();
	private JTable detailTable;
	private ArrayList<VariantOption> globalOptionSet;	//Corporate Option ������ ������ �ִ� Set
	private ArrayList<VariantOption> enableOptionSet;	// ��� ������ �ɼ������� ������ �ִ� Set
	private ArrayList<VariantOption> selectedLineOptionSet;	//������ BOM line�� ������ �ִ� Option Set
	private ArrayList<VariantOption> optionSetToDelete = new ArrayList<VariantOption>();
	private Registry registry = null;
	private JTree tree;
	private TCComponentBOMLine selectedLine;
	private JCheckBox showAllOptionsChk;
	private Vector headerVector = new Vector();
	private Vector userDefineHeader = new Vector();
	private Vector moduleConstraintHeader = new Vector();
	private int[] columnWidth = {40, 80, 150, 100, 200};
	private int[] userDefineHeaderWidth = {40, 200, 50, 100, 80, 40, 100};
	private int[] moduleConstraintHeaderWidth = {80, 80, 100, 50, 100, 80, 40, 100};
	private Vector<Vector> allData = new Vector();
	private Vector<String[]> userDefineErrorList = null;	//����� ���� ���� ����Ʈ
	private Vector<String[]> moduleConstraintList = null;	//��� ���� ���� ����Ʈ
	private VariantNode currentTreeNode = null;
	private OptionManager manager = null;
	private ArrayList<VariantValue> valueList = new ArrayList<VariantValue>();	//Tree�� �������� Option Value����Ʈ
	private JTable userDefineErrorTable;
	private JTable moduleConstraintTable;
	private static EditErrorCheckDialog editerrorcheckdialog;
	private static EditConstraintsDialog editconstraintsdialog;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			OptionSetDialog dialog = new OptionSetDialog(null , null, null, null, null, null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public OptionSetDialog( ArrayList<VariantOption> globalOptionSet, ArrayList<VariantOption> enableOptionSet, 
			ArrayList<VariantOption> selectedLineOptionSet, TCComponentBOMLine selectedLine, 
			Vector<String[]> userDefineErrorList, Vector<String[]> moduleConstraintList, OptionManager manager) throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setTitle("Specification Management");
//		setBounds(100, 100, 950, 654);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.registry = Registry.getRegistry(this);
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
		JSplitPane splitPane = new JSplitPane();
		splitPane.setBackground(Color.WHITE);
    	
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
		
		splitPane.setPreferredSize(new Dimension(1000, 600));
    	contentPanel.add(splitPane, BorderLayout.CENTER);
    	splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
    	splitPane.setContinuousLayout(true);
    	splitPane.setOneTouchExpandable(true);
		upperPanel.setLayout(new BorderLayout(0, 0));
//		upperPanel.setPreferredSize(new Dimension(1000, 600));
    	splitPane.setLeftComponent(upperPanel);
//    	splitPane.getLeftComponent().setMinimumSize(new Dimension(650, 200));
    	splitPane.setDividerLocation(600);
    	splitPane.setDividerSize(15);
    	
    	JScrollPane bottomPane = new JScrollPane();
    	bottomPane.setBackground(Color.WHITE);
    	bottomPane.setPreferredSize(new Dimension(930,300));
    	bottomPane.setViewportView(bottomPanel);
    	bottomPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    	bottomPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    	bottomPane.getViewport().setBackground(Color.WHITE);
    	
    	splitPane.setRightComponent(bottomPane);
    	splitPane.getRightComponent().setMinimumSize(new Dimension());
    	bottomPanel.setPreferredSize(new Dimension(930, 500));
    	bottomPanel.setLayout(new GridLayout(2, 1, 0, 0));
    	{
    		JPanel panel = new JPanel();
    		panel.setBackground(Color.WHITE);
    		panel.setBorder(new TitledBorder(null, "Checking for Errors", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    		bottomPanel.add(panel);
    		panel.setLayout(new BorderLayout(0, 0));
    		{
    			JPanel panel_1 = new JPanel();
    			panel_1.setBackground(Color.WHITE);
    			panel.add(panel_1, BorderLayout.NORTH);
    			panel_1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
    			{
    				JButton userErrorAddBtn = new JButton("Add");
    				userErrorAddBtn.setBackground(Color.WHITE);
    				panel_1.add(userErrorAddBtn);
    				userErrorAddBtn.addActionListener(new ActionListener() {
    					public void actionPerformed(ActionEvent actionevent) {
    						
    						userDefineErrorAdd();
    					}
    				});
    				
    				JButton userErrorDelBtn = new JButton("Del");
    				userErrorDelBtn.setBackground(Color.WHITE);
					panel_1.add(userErrorDelBtn);
					userErrorDelBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							userDefineErrorDel();
						}
					});
    			}
    		}
    		{
    			JPanel panel_1 = new JPanel();
    			panel_1.setBackground(Color.WHITE);
    			panel.add(panel_1, BorderLayout.CENTER);
    			panel_1.setLayout(new BorderLayout(0, 0));
    			{
    				Vector<Vector> userDefineErrorData = new Vector();
    				for( String[] userDefine: userDefineErrorList){
    					Vector row = new Vector();
    					for( String str : userDefine){
    						row.add(str == null ? "":str);
    					}
    					userDefineErrorData.add(row);
    				}
    				TableModel model = new DefaultTableModel(userDefineErrorData, userDefineHeader) {
    					public Class getColumnClass(int col) {
    						return String.class;
    					}

    					public boolean isCellEditable(int row, int col) {
    						return false;
    					}
    			    };
    			    userDefineErrorTable = new JTable(model);
    			    userDefineErrorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

    			    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
    			    userDefineErrorTable.setRowSorter(sorter);
    			    JScrollPane pane = new JScrollPane();
    				pane.setViewportView(userDefineErrorTable);
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
    		panel.setBorder(new TitledBorder(null, "Module Constraints", TitledBorder.LEADING, TitledBorder.TOP, null, null));
    		bottomPanel.add(panel);
    		panel.setLayout(new BorderLayout(0, 0));
    		{
    			JPanel panel_1 = new JPanel();
    			panel_1.setBackground(Color.WHITE);
    			panel.add(panel_1, BorderLayout.NORTH);
    			panel_1.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
    			{
    				JButton constraintAddBtn = new JButton("Add");
    				constraintAddBtn.setBackground(Color.WHITE);
    				constraintAddBtn.addActionListener(new ActionListener() {
    					public void actionPerformed(ActionEvent actionevent) {
    						moduleConstraintAdd();
    					}
    				});
    				panel_1.add(constraintAddBtn);
    				
    				JButton constraintDelBtn = new JButton("Del");
    				constraintDelBtn.setBackground(Color.WHITE);
					panel_1.add(constraintDelBtn);
					constraintDelBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							
		    				moduleConstraintDel();
						}
					});
    				
    			}
    		}
    		{
    			JPanel panel_1 = new JPanel();
    			panel_1.setBackground(Color.WHITE);
    			panel.add(panel_1, BorderLayout.CENTER);
    			{
    				Vector<Vector> moduleConstraintData = new Vector();
    				for( String[] moduleConstraint: moduleConstraintList){
    					Vector row = new Vector();
    					for( String str : moduleConstraint){
    						row.add(str == null ? "":str);
    					}
    					moduleConstraintData.add(row);
    				}
    				TableModel model = new DefaultTableModel(moduleConstraintData, moduleConstraintHeader) {
    					public Class getColumnClass(int col) {
    						return String.class;
    					}

    					public boolean isCellEditable(int row, int col) {
    						return false;
    					}
    			    };
    			    moduleConstraintTable = new JTable(model);
    			    moduleConstraintTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    			    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
    			    moduleConstraintTable.setRowSorter(sorter);
    			    JScrollPane pane = new JScrollPane();
    				pane.setViewportView(moduleConstraintTable);
    				pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
    				pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
    				pane.getViewport().setBackground(Color.WHITE);
    				panel_1.setLayout(new BorderLayout(0, 0));
    				panel_1.add(pane);
    			}
    		}
    	}
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
					JPanel panel_2 = new JPanel();
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
							JButton button = new JButton(){

								@Override
								public Dimension getPreferredSize() {
									return new Dimension(50, 40);
								}
								
							};
							button.setBackground(Color.WHITE);
							
							//Tree���� ���� ���̺�� ==> ���
							//1. selectedLineOptionSet���� �ִ��� üũ�Ͽ�, �ִٸ� �� �ɼ��� �����´�.
							//2. �������� �ʴ´ٸ� �ɼ��� ����.
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									addValues();
								}
							});
							button.setIcon(registry.getImageIcon("ProuctOptionManageForwardArrow2.ICON"));
							panel_1.add(button);
						}
						
						//�׽�Ʈ �ӽ�
						/*
						{
							JButton button = new JButton("OLD"){

								@Override
								public Dimension getPreferredSize() {
									return new Dimension(60, 40);
								}
								
							};
//							button.setBackground(Color.WHITE);
							
							//Tree���� ���� ���̺�� ==> ���
							//1. selectedLineOptionSet���� �ִ��� üũ�Ͽ�, �ִٸ� �� �ɼ��� �����´�.
							//2. �������� �ʴ´ٸ� �ɼ��� ����.
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									addValues_();
								}
							});
							panel_1.add(button);
						}
						*/
						//�׽�Ʈ �ӽ� ��
						
						{
							JButton button = new JButton(){

								@Override
								public Dimension getPreferredSize() {
									return new Dimension(50, 40);
								}
								
							};
							button.setBackground(Color.WHITE);
							//���� ���̺��� ���� ���̺�� <== ���
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									removeValues();
								}
							});
							button.setIcon(registry.getImageIcon("ProuctOptionManageBackArrow2.ICON"));
							panel_1.add(button);
						}
					}
				}
				
				if( selectedLineOptionSet != null){
					for( VariantOption option : selectedLineOptionSet){
						List<VariantValue> values = option.getValues();
						for( VariantValue value : values){
							Vector row = new Vector();
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
							showAllOptionsChk.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(ActionEvent arg0) {
//								show All Options�� üũ�Ǿ� ������, Table���� üũ �ȵ� ��� �ɼ��� �����ش�.
									DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
									Vector newData = getData(showAllOptionsChk.isSelected());
									model.setDataVector(newData, headerVector);
									columnInit();	
								}
								
							});
						}
						
						TableModel model = new DefaultTableModel(getData(showAllOptionsChk.isSelected()), headerVector) {
							public Class getColumnClass(int col) {
								if( col == 0 ){
									return VariantValue.class;
								}
								return String.class;
							}

							public boolean isCellEditable(int row, int col) {
								if( col == 0 ){
//									if( valueList.contains( getValueAt(row, 0))){
//										return true;
//									}
									return true;
								}
								return false;
							}
						};
						
						detailTable = new JTable(model);

					    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
					    detailTable.setRowSorter(sorter);
					    detailTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
										&& e.isControlDown()==false) {
									removeValues();
								}
								super.mouseReleased(e);
							}
							
						});
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
									OptionSetOperation operation = new OptionSetOperation(OptionSetDialog.this.globalOptionSet, OptionSetDialog.this.enableOptionSet, 
											OptionSetDialog.this.selectedLineOptionSet, OptionSetDialog.this.selectedLine, 
											OptionSetDialog.this.userDefineErrorList, OptionSetDialog.this.moduleConstraintList, OptionSetDialog.this.manager, optionSetToDelete, allData, OptionSetDialog.this, waitProgress);
									OptionSetDialog.this.selectedLine.getSession().queueOperation(operation);
									
								}
							});
							panel_3.add(saveBtn);
							
							//�׽�Ʈ �ӽ�
							/*
							JButton saveBtnOld = new JButton("Save Old");
							saveBtnOld.setBackground(Color.WHITE);
							saveBtnOld.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									final WaitProgressBar waitProgress = new WaitProgressBar(OptionSetDialog.this);
									waitProgress.start();
									OptionSetOperation operation = new OptionSetOperation(OptionSetDialog.this.globalOptionSet, OptionSetDialog.this.enableOptionSet, 
											OptionSetDialog.this.selectedLineOptionSet, OptionSetDialog.this.selectedLine, 
											OptionSetDialog.this.userDefineErrorList, OptionSetDialog.this.moduleConstraintList, OptionSetDialog.this.manager, optionSetToDelete, allData, OptionSetDialog.this, waitProgress, "OLD");
									OptionSetDialog.this.selectedLine.getSession().queueOperation(operation);
									
								}
							});
							panel_3.add(saveBtnOld);
							*/
							//�׽�Ʈ �ӽ� ��
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
					exportBtn.setIcon(new ImageIcon(VariantOptionDefinitionDialog.class.getResource("/com/kgm/common/images/excel_16.png")));
					exportBtn.addActionListener(new ActionListener(){

						@Override
						public void actionPerformed(ActionEvent actionevent) {
							JFileChooser fileChooser = new JFileChooser();
							fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
							Calendar now = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
							sdf.format(now.getTime());
							File defaultFile = new File("Option_Manager_" + sdf.format(now.getTime()) + ".xls");
							fileChooser.setSelectedFile(defaultFile);
							fileChooser.setFileFilter(new FileFilter(){

								@Override
								public boolean accept(File f) {
									if( f.isFile()){
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
							if( result == JFileChooser.APPROVE_OPTION){
								File selectedFile = fileChooser.getSelectedFile();
								try
					            {
									exportToExcel(selectedFile);
									AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
									aif.start();
									
					            }
					            catch (Exception ioe)
					            {
					            	ioe.printStackTrace();
					            }finally{
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
	 * ������ �ɼ��� Excel�� Export ��.
	 * 
	 * @param selectedFile
	 * @throws IOException
	 * @throws WriteException
	 */
	protected void exportToExcel(File selectedFile) throws IOException, WriteException {
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0��° Sheet ����
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);

	    WritableCellFormat cellFormat = new WritableCellFormat(); // ���� ��Ÿ���� �����ϱ� ���� �κ��Դϴ�.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // ���� ��Ÿ���� �����մϴ�. �׵θ��� ���α׸��°ſ���
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // ���� ��Ÿ���� �����ϱ� ���� �κ��Դϴ�.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 1;
	    int initColumnNum = 0;

	    label = new jxl.write.Label(1, startRow, "Product Option Manager(" + this.selectedLine.toDisplayString() + ")", cellFormat);
	    sheet.addCell(label);
	    sheet.mergeCells(1, 1, 3, 1);
	    
	    startRow = 3;
	    Vector excelColumnHeader = new Vector();
	    excelColumnHeader.add("CATEGORY");
	    excelColumnHeader.add("CATEGORY_DESC");
	    excelColumnHeader.add("OPTION_CODE");
	    excelColumnHeader.add("OPTION_DESC");
	    excelColumnHeader.add("STATUS");
	    excelColumnHeader.add("IN_USE");
	    
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
	      cv.setAutosize(true);
	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    int rowNum = 0;
	    startRow = 4;
	    Vector<Vector> data = allData;
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	VariantValue value = (VariantValue)row.get(0);
    		if( value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE){
				continue;
			}
    		
	    	for (int j = 0; j < row.size() + 1; j++)
	    	{
	    		String str = "";
	    		//������ �÷��� 0��° VariantValue���� ���� �����´�.
	    		if( j == 5 ){
//	    			if( value.isUsing()){
//	    				str = "Y";
//	    			}else{
//	    				str = "N";
//	    			}
	    			str = "N/A";
	    		}else if( j == 4 ){
	    			if( value.getValueStatus() == VariantValue.VALUE_USE){
	    				str = "Use";
	    			}else if( value.getValueStatus() == VariantValue.VALUE_NOT_USE){
	    				str = "Unuse";
	    			}else{
	    				str = "-";
	    			}
	    			
	    		}else{
	    			str = (String)row.get(j + 1);
	    		}
	    		
	    		label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, str, cellFormat);
	    		sheet.addCell(label);
	    		
	    	}
	    	rowNum++;
	    }

	    //�� Merge
	    int startIdxToMerge = startRow;
	    int endIdxToMerge = startRow;
	    for (int i = 0; i < data.size() - 1; i++){
	    	
	    	Cell cell = sheet.getCell(initColumnNum, i + startRow);
	    	Cell nextCell = sheet.getCell(initColumnNum, i + startRow + 1);
	    	
	    	if( cell.getContents().equals(nextCell.getContents())){
	    		endIdxToMerge = i + 1 + startRow;
	    	}else{
	    		if( startIdxToMerge < endIdxToMerge){
		    		sheet.mergeCells(initColumnNum, startIdxToMerge, initColumnNum, endIdxToMerge);
		    		WritableCell wCell = sheet.getWritableCell(initColumnNum, startIdxToMerge);
	    			WritableCellFormat cf = new WritableCellFormat(); // ���� ��Ÿ���� �����ϱ� ���� �κ��Դϴ�.
	    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
	    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
	    		    wCell.setCellFormat(cf);
	    		    
		    		sheet.mergeCells(initColumnNum + 1, startIdxToMerge, initColumnNum + 1, endIdxToMerge);
		    		wCell = sheet.getWritableCell(initColumnNum + 1, startIdxToMerge);
	    			cf = new WritableCellFormat(); // ���� ��Ÿ���� �����ϱ� ���� �κ��Դϴ�.
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
	 * ���̺� ������ ����Ÿ�� ������.
	 * 
	 * @param bOnlyChecked	true:üũ�� ����Ÿ��, false:��� ����Ÿ
	 * @return
	 */
	private Vector getData(boolean bOnlyChecked){
		
		Vector newData = new Vector();
		if( bOnlyChecked ){
			for( Vector row : allData){
				VariantValue value = (VariantValue)row.get(0);
				if( value.getValueStatus() == VariantValue.VALUE_USE  
						|| value.getValueStatus() == VariantValue.VALUE_NOT_USE){
					newData.add(row);
				}
			}
		}else{
			for( Vector row : allData){
				VariantValue value = (VariantValue)row.get(0);
				if( value.getValueStatus() == VariantValue.VALUE_USE){
					newData.add(row);
				}
			}
		}
		
		return newData;
		
	}
	
	/**
	 * ��ü Corporate Option �Ǵ� Product Item�� �ִ� ��� �ɼ��� Tree�� �����Ѵ�. 
	 * @return
	 */
	private JTree initTree(){
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Option Set");
		for( VariantOption option : this.enableOptionSet){
			
			VariantNode optionNode = new VariantNode(option);
			List<VariantValue> values = option.getValues();
			
			//��밡���� �ɼ��� �����ϴ°�츸 Option�� �߰��Ѵ�.
			if( values != null && !values.isEmpty() ){
				int enableChildCount = 0;
				for( VariantValue value : option.getValues()){
					if( value.getValueStatus() == VariantValue.VALUE_USE){
						enableChildCount++;
					}
				}
				if( enableChildCount > 0 ){
					root.add(optionNode);
				}
			}
		}
		tree = new JTree(root);
		tree.addMouseListener(new MouseAdapter(){

			// ==> ��ɰ� ����
			// ����� �ɼ����� �߰���.
			@Override
			public void mouseReleased(MouseEvent e) {
				if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
						&& e.isControlDown()==false) {
					addValues();
				}
				super.mouseReleased(e);
			}
			
		});
//		tree.setRootVisible(false);
		return tree;
	}
	
	/**
	 * 
	 * @param value VariantValueŸ���� �ɼǰ�
	 * @param data ���̺� ���� ����Ÿ
	 * @return
	 */
	private boolean isValid(VariantValue value, Vector<Vector> data){
		String optionName = value.getOption().getOptionName();
		String valueName = value.getValueName();
		
		for( Vector row : data){
			if( optionName.equals(row.get(0)) && valueName.equals(row.get(1))){
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * isValid ��ɿ� Use üũ ��� �߰�.
	 * 
	 * 
	 * @param value
	 * @param data
	 * @return
	 */
	private boolean isValidAndCheck(VariantValue value, Vector<Vector> data){
		
		for( Vector row : data){
			if( value.equals(row.get(0))){
				if( value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE)
					value.setValueStatus(VariantValue.VALUE_USE);
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * tree �ʱ�ȭ �� ���̺� ������ ����.
	 * 
	 */
	public void columnInit(){
		
		DefaultTreeModel treeModel = (DefaultTreeModel)tree.getModel();
		DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode)treeModel.getRoot();
		Enumeration enums = rootNode.children();
		while(enums.hasMoreElements()){
			VariantNode optionNode = (VariantNode)enums.nextElement();
			Enumeration childEnums = optionNode.children();
			while(childEnums.hasMoreElements()){
				VariantNode valueNode = (VariantNode)childEnums.nextElement();
				valueList.add((VariantValue)valueNode.getUserObject());
			}
		}
		
		TableColumnModel columnModel = detailTable.getColumnModel();
		int n = headerVector.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
			columnModel.getColumn(i).setWidth(columnWidth[i]);
		}

//		SRME:: [][20140812] swyoon  Prouct, Variant, Function�� �ɼ� ���� �ӵ� ����(�������� ��뿩�� üũ ����).		
		VariantTableCellRenderer cellRenderer = new VariantTableCellRenderer(valueList);
		for( int i = 0; i < columnModel.getColumnCount(); i++){
			TableColumn column = columnModel.getColumn(i);
			if( i == 0 ){
				column.setCellRenderer(new VariantCheckBoxTableCellRenderer(valueList));
				column.setCellEditor(new VariantCheckBoxTableCellEditor(new JCheckBox(), valueList, detailTable));
				continue;
			}
			column.setCellRenderer(cellRenderer);
		}
	}
	
	private void initUserDefineTable(){
		Vector userDefineHeader = new Vector();
		userDefineHeader.add("Level");
		userDefineHeader.add("Message");
		userDefineHeader.add("Way");
		userDefineHeader.add("Item");
		userDefineHeader.add("Category");
		userDefineHeader.add("Op");
		userDefineHeader.add("Code");
	}
	
	private boolean isContain(VariantValue value){
		for( Vector row : allData){
			VariantValue val = (VariantValue)row.get(0);
			
			/**
			 * [SR150126-016][2015.03.10][jclee] �űԿɼ� �߰��� ���� ���BOM�� �ڵ����� �߰��Ǵ� ����
			 * �� �߰��Ǿ��ִ� Option�� ��� Define �� Value�� ��쿡�� True ��ȯ. 
			 */
			//option validation�� not difine�� ��� �ߺ����� ���°� ����
			//origin
			/*
			boolean isUsed = val.getValueStatus() == VariantValue.VALUE_NOT_USE || val.getValueStatus() == VariantValue.VALUE_USE;
			if( val.equals(value) && isUsed){
				return true;
			}
            */
			
			// ���� ��
			
			if( val.equals(value)){
				return true;
			}
			
		}
		
		return false;
	}
	
	//�׽�Ʈ �ӽ�
	private boolean isContain_(VariantValue value){
		for( Vector row : allData){
			VariantValue val = (VariantValue)row.get(0);
			
			/**
			 * [SR150126-016][2015.03.10][jclee] �űԿɼ� �߰��� ���� ���BOM�� �ڵ����� �߰��Ǵ� ����
			 * �� �߰��Ǿ��ִ� Option�� ��� Define �� Value�� ��쿡�� True ��ȯ. 
			 */
			boolean isUsed = val.getValueStatus() == VariantValue.VALUE_NOT_USE || val.getValueStatus() == VariantValue.VALUE_USE;
			if( val.equals(value) && isUsed){
				return true;
			}
		}
		
		return false;
	}
	//�׽�Ʈ �ӽ� ��
	
	private void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				TreePath path = tree.getPathForLocation(e.getX(), e.getY());
				if( path == null) return;
				
				Object obj = path.getLastPathComponent();
				if( obj != null && (obj instanceof VariantNode)){
					currentTreeNode = (VariantNode)obj;
					if(currentTreeNode.getUserObject() instanceof VariantOption)
						popup.show(e.getComponent(), e.getX(), e.getY());
				}else{
					currentTreeNode = null;
				}
				
			}
		});
	}
	
	/**
	 * Dialog��ﶧ �����ߴ� BOM line�� �ɼǼ¿���, �̸��� ���� �ɼ��� ����
	 * @param option
	 * @return
	 */
	public VariantOption getOption(VariantOption option){
		
		for( VariantOption opt : OptionSetDialog.this.selectedLineOptionSet){
			if( opt.getOptionName().equals(option.getOptionName())){
				return opt;
			}
		}
		
		return option;
	}
	
	/**
	 * �ɼ� Tree���� ���� ���̺�� Option Value�� �߰��Ѵ�.
	 * 
	 */
	private void addValues(){
		TreePath[] paths = tree.getSelectionPaths();
		for( int i = 0; paths != null && i < paths.length; i++){
			TreePath path = paths[i];
			if( !( path.getLastPathComponent() instanceof VariantNode)){
				continue;
			}
			
			VariantNode node = (VariantNode)path.getLastPathComponent();
			Object obj = node.getUserObject();
			DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
			if( obj instanceof VariantOption){
				//variant option ���� �� ������ �������� ���� bug fix
				//1. ���� Option value�� used�� ��ϵǾ� �ְ�, ������ value�� �߰��ϴ� ��� used�� �߰����� ����.
				
				//origin
				/*
				VariantOption option = (VariantOption)obj;
				option = getOption(option);
				if( OptionSetDialog.this.selectedLineOptionSet.contains(option)){
					
					//�����Ǿ��� �ɼǿ��� ������.
					if( optionSetToDelete.contains(option)){
						optionSetToDelete.remove(option);
					}
				}

				Enumeration<VariantNode> enums = node.children();
//				ArrayList<VariantValue> tmpList = new ArrayList();
				
				//������ ���̺� �𵨿� Row�߰�
				while( enums.hasMoreElements()){
					VariantNode childNode = enums.nextElement();
					VariantValue value = (VariantValue)childNode.getUserObject();
					if( isValidAndCheck(value, model.getDataVector())){
						
						value.setNew(true);
						value.setValueStatus(VariantValue.VALUE_USE);
						
						Vector row = new Vector();
						row.add(value);
						row.add(option.getOptionName());
						row.add(option.getOptionDesc());
						row.add(value.getValueName());
						row.add(value.getValueDesc());
						model.addRow(row);
//						tmpList.add(value);
					}
				}
				
				//����ÿ� ���Ǵ� allData�� Row�߰�
				if(option.hasValues()){
					for( VariantValue value : option.getValues()){
						
						if( !isContain(value)){
							value.setNew(true);
							Vector row = new Vector();
//							value.setValueStatus(VariantValue.VALUE_USE);
							row.add(value);
							row.add(option.getOptionName());
							row.add(option.getOptionDesc());
							row.add(value.getValueName());
							row.add(value.getValueDesc());
							allData.add(row);
						}else{
							value.setValueStatus(VariantValue.VALUE_USE);
						}
						
					}
				}
				*/
				
				//���� ��
				
				VariantOption option = (VariantOption)obj;
				option = getOption(option);
				if( OptionSetDialog.this.selectedLineOptionSet.contains(option)){
					
					//�����Ǿ��� �ɼǿ��� ������.
					if( optionSetToDelete.contains(option)){
						optionSetToDelete.remove(option);
					}
				}
				
//				Enumeration<VariantNode> enums = node.children(); ����
				Enumeration<TreeNode> enums = node.children();
				List<VariantValue> childList = new ArrayList<VariantValue>(node.getChildCount());
				while( enums.hasMoreElements()){
//					VariantNode childNode = enums.nextElement(); ����
					VariantNode childNode = (VariantNode) enums.nextElement();
					VariantValue value = (VariantValue)childNode.getUserObject();
					childList.add(value);
				}
				
				
				List<VariantValue> list = option.getValues();
				for( VariantValue val : list){
					
					if( isValidAndCheck(val, model.getDataVector())){ // model�� ���� �ȵǾ������� true
						boolean iscontain = childList.contains(val); 
						val.setNew(true );
						val.setValueStatus( iscontain ? VariantValue.VALUE_USE:VariantValue.VALUE_NOT_DEFINE);
						//global option ��� �߰��Ǿ�� �Ѵٸ� �Ʒ� �ּ� ����
//						val.setValueStatus(VariantValue.VALUE_USE);
						Vector row = new Vector();
						row.add( val );
						row.add(option.getOptionName());
						row.add(option.getOptionDesc());
						row.add(val.getValueName());
						row.add(val.getValueDesc());
						
						//������ Value�̸� ������ �𵨿� �߰�.
						//global option ��� �߰��Ǿ�� �Ѵٸ� �Ʒ� ���ǹ� �ּ� ó��
						if( iscontain ){
							model.addRow(row);
						}
						
						// allData�� �����ϰ� ���� ������ �߰���.
						if( !isContain(val)){
//							if( !iscontain ){
//								val.setValueStatus(VariantValue.VALUE_NOT_DEFINE);
//							}else{
//								val.setValueStatus(VariantValue.VALUE_USE);
//							}
							allData.add(row);
						}
						
					}
				}
				
				
			} else if( obj instanceof VariantValue){
				
				VariantValue value = (VariantValue)obj;
				VariantOption option = value.getOption();
				option = getOption(option);
				//���� �ִ� �ɼ��� ���
				if( OptionSetDialog.this.selectedLineOptionSet.contains(option)){
					
					//�����Ǿ��� �ɼǿ��� ������.
					if( optionSetToDelete.contains(option)){
						optionSetToDelete.remove(option);
					}
				}
				
				List<VariantValue> list = option.getValues();
				for( VariantValue val : list){
					
					//not difine�� ����� allData�� �ߺ����� ��ϵǴ°� ����
					//origin
					/*
					if( isValidAndCheck(val, model.getDataVector())){// model�� ���� �ȵǾ������� true
						val.setNew(true );
						val.setValueStatus( value.equals(val) ? VariantValue.VALUE_USE:VariantValue.VALUE_NOT_DEFINE);
						Vector row = new Vector();
						row.add( val );
						row.add(option.getOptionName());
						row.add(option.getOptionDesc());
						row.add(val.getValueName());
						row.add(val.getValueDesc());
						
						//������ Value�̸� ������ �𵨿� �߰�.
						if( val.equals(value) ){
							model.addRow(row);
						}
						
						// allData�� �����ϰ� ���� ������ �߰���.
						if( !isContain(val)){
							if( !val.equals(value) ){
								val.setValueStatus(VariantValue.VALUE_NOT_DEFINE);
							}else{
								val.setValueStatus(VariantValue.VALUE_USE);
							}
							allData.add(row);
						}
						
					}
					*/
					//���� ��
					
					if( isValidAndCheck(val, model.getDataVector())){// model�� ���� �ȵǾ������� true
						val.setNew(true );
						val.setValueStatus( value.equals(val) ? VariantValue.VALUE_USE:VariantValue.VALUE_NOT_DEFINE);
						Vector row = new Vector();
						row.add( val );
						row.add(option.getOptionName());
						row.add(option.getOptionDesc());
						row.add(val.getValueName());
						row.add(val.getValueDesc());
						
						//������ Value�̸� ������ �𵨿� �߰�.
						if( val.equals(value) ){
							model.addRow(row);
						}
						
						// allData�� �����ϰ� ���� ������ �߰���.
						if( !isContain(val)){
//							if( !val.equals(value) ){
//								val.setValueStatus(VariantValue.VALUE_NOT_DEFINE);
//							}else{
//								val.setValueStatus(VariantValue.VALUE_USE);
//							}
							allData.add(row);
						}
						
					}
					
				}
				
			}
		}
	}
	
	//�׽�Ʈ �ӽ�
	private void addValues_(){
		TreePath[] paths = tree.getSelectionPaths();
		for( int i = 0; paths != null && i < paths.length; i++){
			TreePath path = paths[i];
			if( !( path.getLastPathComponent() instanceof VariantNode)){
				continue;
			}
			
			VariantNode node = (VariantNode)path.getLastPathComponent();
			Object obj = node.getUserObject();
			DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
			if( obj instanceof VariantOption){
				
				VariantOption option = (VariantOption)obj;
				option = getOption(option);
				if( OptionSetDialog.this.selectedLineOptionSet.contains(option)){
					
					//�����Ǿ��� �ɼǿ��� ������.
					if( optionSetToDelete.contains(option)){
						optionSetToDelete.remove(option);
					}
				}

//				Enumeration<VariantNode> enums = node.children(); ����
				Enumeration<TreeNode> enums = node.children();
				
//				ArrayList<VariantValue> tmpList = new ArrayList();
				
				//������ ���̺� �𵨿� Row�߰�
				while( enums.hasMoreElements()){
//					VariantNode childNode = enums.nextElement(); ����
					VariantNode childNode = (VariantNode) enums.nextElement();
					
					VariantValue value = (VariantValue)childNode.getUserObject();
					if( isValidAndCheck(value, model.getDataVector())){
						
						value.setNew(true);
						value.setValueStatus(VariantValue.VALUE_USE);
						
						Vector row = new Vector();
						row.add(value);
						row.add(option.getOptionName());
						row.add(option.getOptionDesc());
						row.add(value.getValueName());
						row.add(value.getValueDesc());
						model.addRow(row);
//						tmpList.add(value);
					}
				}
				
				//����ÿ� ���Ǵ� allData�� Row�߰�
				if(option.hasValues()){
					for( VariantValue value : option.getValues()){
						
						if( !isContain_(value)){
							value.setNew(true);
							Vector row = new Vector();
//							value.setValueStatus(VariantValue.VALUE_USE);
							row.add(value);
							row.add(option.getOptionName());
							row.add(option.getOptionDesc());
							row.add(value.getValueName());
							row.add(value.getValueDesc());
							allData.add(row);
						}else{
							value.setValueStatus(VariantValue.VALUE_USE);
						}
						
					}
				}
			}else if( obj instanceof VariantValue){
				
				VariantValue value = (VariantValue)obj;
				VariantOption option = value.getOption();
				option = getOption(option);
				//���� �ִ� �ɼ��� ���
				if( OptionSetDialog.this.selectedLineOptionSet.contains(option)){
					
					//�����Ǿ��� �ɼǿ��� ������.
					if( optionSetToDelete.contains(option)){
						optionSetToDelete.remove(option);
					}
				}
				
				List<VariantValue> list = option.getValues();
				for( VariantValue val : list){
					
					if( isValidAndCheck(val, model.getDataVector())){
						val.setNew(true );
						val.setValueStatus( value.equals(val) ? VariantValue.VALUE_USE:VariantValue.VALUE_NOT_DEFINE);
						Vector row = new Vector();
						row.add( val );
						row.add(option.getOptionName());
						row.add(option.getOptionDesc());
						row.add(val.getValueName());
						row.add(val.getValueDesc());
						
						//������ Value�̸� ������ �𵨿� �߰�.
						if( val.equals(value) ){
							model.addRow(row);
						}
						
						// allData�� �����ϰ� ���� ������ �߰���.
						if( !isContain_(val)){
							if( !val.equals(value) ){
								val.setValueStatus(VariantValue.VALUE_NOT_DEFINE);
							}else{
								val.setValueStatus(VariantValue.VALUE_USE);
							}
							allData.add(row);
						}
						
					}
				}
				
			}
		}
	}
	//�׽�Ʈ �ӽ�
	
	/**
	 * ����(������ �߰��Ǿ��ų� ��� �߰��� Variant �׸�)���� Option Value�� �����Ѵ�.
	 */
	private void removeValues(){
		int[] selectedIdxs = detailTable.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
		ArrayList<VariantValue> selectedValues = new ArrayList();
//		int[] idx = model.getIndexes();
		Vector<Integer> idxToDelete = new Vector();
		for( int i = selectedIdxs.length - 1; i >= 0; i--){
			VariantValue value = (VariantValue)detailTable.getValueAt(selectedIdxs[i], 0);
			
			//Product���� �������� ������ ���, ���̱�� ������, ����ڰ� �ٽ� üũ�� ���� ����.
			if( !valueList.contains(value)) continue; 
			
			selectedValues.add(value);
			if( value.isNew() || ( !value.isNew() && !value.isUsing())){
				
				VariantOption option = value.getOption();
				for( int j = model.getRowCount() - 1; j >= 0; j--){
					VariantValue val = (VariantValue)model.getValueAt(j, 0);
					if( val.equals(value)){
						model.removeRow(j);
						break;
					}
				}
				
				//������ ���̺� �𵨿��� ��� Value�� ��� ���� ������ �ʴ� �ɼ��� AllData���� ������.
				boolean bNeedDataRemove = true;
				for( Vector row : (Vector<Vector>)model.getDataVector()){
					if( row.get(1).equals(option.getOptionName())){
						bNeedDataRemove = false;
						break;
					}
				}
				
				if( bNeedDataRemove ){
					//��ü Data���� �����ǰ�, d
					for( int j = allData.size() - 1; j >= 0; j--){
						Vector row = allData.get(j);
						if( row.get(1).equals(option.getOptionName())){
							allData.remove(j);
							if( OptionSetDialog.this.selectedLineOptionSet.contains(option)){
								if( !optionSetToDelete.contains(option)){
									optionSetToDelete.add(option);
								}
							}
						}
					}
				}else{
					for( int j = allData.size() - 1; j >= 0; j--){
						Vector row = allData.get(j);
						VariantValue val = (VariantValue)row.get(0);
						if( value.equals(val)){
							val.setValueStatus(VariantValue.VALUE_NOT_DEFINE);
						}
					}
				}
			}
		}
		
		model.fireTableDataChanged();
	}
	
	/**
	 * ����� ���� �޽��� �׸� �߰�
	 */
	private void userDefineErrorAdd(){
		AbstractAIFUIApplication aifApp = AIFUtility.getCurrentApplication();
		if( aifApp instanceof PSEApplicationService){
			
			if( editerrorcheckdialog != null){
				editerrorcheckdialog.dispose();
			}
			PSEApplicationService service = (PSEApplicationService)aifApp;
			BOMTreeTable treeTable = (BOMTreeTable)service.getAbstractViewableTreeTable();
			ModularOptionModel moduleModel = null;
			ConstraintsModel constraintModel = null;
			try
	        {
				moduleModel = new ModularOptionModel(treeTable, OptionSetDialog.this.selectedLine, true);
				TCVariantService variantService = OptionSetDialog.this.selectedLine.getSession().getVariantService();
				String lineMvl = variantService.askLineMvl(OptionSetDialog.this.selectedLine);
//				constraintModel = new ConstraintsModel(OptionSetDialog.this.selectedLine.getItem().getProperty("item_id"), lineMvl, OptionSetDialog.this.selectedLine, variantService);
				constraintModel = new ConstraintsModel(OptionSetDialog.this.selectedLine.getItem().getProperty("item_id"), lineMvl, null, OptionSetDialog.this.selectedLine, variantService);
				constraintModel.parse();
				
				editerrorcheckdialog = new EditErrorCheckDialog(AIFUtility.getActiveDesktop().getFrame(), moduleModel, constraintModel, null);
				editerrorcheckdialog.addWindowListener(new WindowAdapter(){
					@Override
					public void windowClosed(WindowEvent windowevent) {
						try {
							Vector<String[]> tmpVec = OptionManager.getUserDefineErrors(OptionSetDialog.this.selectedLine, true);
							OptionSetDialog.this.userDefineErrorList.clear();
							OptionSetDialog.this.userDefineErrorList.addAll(tmpVec);
							
							Vector<Vector> userDefineErrorData = new Vector();
		    				for( String[] userDefine: OptionSetDialog.this.userDefineErrorList){
		    					Vector row = new Vector();
		    					for( String str : userDefine){
		    						row.add(str == null ? "":str);
		    					}
		    					userDefineErrorData.add(row);
		    				}
		    				DefaultTableModel model = (DefaultTableModel)userDefineErrorTable.getModel();
		    				model.setDataVector(userDefineErrorData, userDefineHeader);
		    				SwingUtilities.invokeLater(new Runnable(){

								@Override
								public void run() {
									try {
										OptionSetDialog.this.selectedLine.window().save();
									} catch (TCException e) {
										e.printStackTrace();
									}
								}
		    					
		    				});
		    				
						} catch (TCException e) {
							e.printStackTrace();
						}
						super.windowClosed(windowevent);
					}
				});
                editerrorcheckdialog.run();
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }finally{
	        	
	        }
				
		}
	}
	
	/**
	 * ����� ���� ��ȿ�� üũ ����
	 */
	private void userDefineErrorDel(){
		//����� ���� ��ȿ�� üũ ����
		final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
		waitProgress.start();
		OptionSetOperation operation = new OptionSetOperation(OptionSetDialog.this.selectedLine, 
				OptionSetDialog.this.userDefineErrorList, OptionSetDialog.this.moduleConstraintList, OptionSetDialog.this.manager, 
				userDefineErrorTable, waitProgress, OptionSetOperation.USER_DEFINE_ERROR_DELETE);
		operation.addOperationListener(new InterfaceAIFOperationListener(){

			@Override
			public void endOperation() {
				
				try {
					Vector<String[]> tmpVec = OptionManager.getUserDefineErrors(OptionSetDialog.this.selectedLine, true);
					OptionSetDialog.this.userDefineErrorList.clear();
					OptionSetDialog.this.userDefineErrorList.addAll(tmpVec);
					
					Vector<Vector> userDefineErrorData = new Vector();
    				for( String[] userDefine: OptionSetDialog.this.userDefineErrorList){
    					Vector row = new Vector();
    					for( String str : userDefine){
    						row.add(str == null ? "":str);
    					}
    					userDefineErrorData.add(row);
    				}
    				DefaultTableModel model = (DefaultTableModel)userDefineErrorTable.getModel();
    				model.setDataVector(userDefineErrorData, userDefineHeader);
    				SwingUtilities.invokeLater(new Runnable(){

						@Override
						public void run() {
							try {
								OptionSetDialog.this.selectedLine.window().save();
							} catch (TCException e) {
								e.printStackTrace();
							}
						}
    					
    				});
				} catch (TCException e) {
					e.printStackTrace();
				}finally{
					waitProgress.dispose();
				}
			}

			@Override
			public void startOperation(String arg0) {
				
			}
			
		});
		OptionSetDialog.this.selectedLine.getSession().queueOperation(operation);
	}
	
	/**
	 * �ɼǰ� �������� �߰�
	 */
	private void moduleConstraintAdd(){
		AbstractAIFUIApplication aifApp = AIFUtility.getCurrentApplication();
		if( aifApp instanceof PSEApplicationService){
			
			if( editconstraintsdialog != null){
				editconstraintsdialog.dispose();
			}
			PSEApplicationService service = (PSEApplicationService)aifApp;
			BOMTreeTable treeTable = (BOMTreeTable)service.getAbstractViewableTreeTable();
			ModularOptionModel moduleModel = null;
			ConstraintsModel constraintModel = null;
				try
		        {
					moduleModel = new ModularOptionModel(treeTable, OptionSetDialog.this.selectedLine, true);
					TCVariantService variantService = OptionSetDialog.this.selectedLine.getSession().getVariantService();
					String lineMvl = variantService.askLineMvl(OptionSetDialog.this.selectedLine);
//					constraintModel = new ConstraintsModel(OptionSetDialog.this.selectedLine.getItem().getProperty("item_id"), lineMvl, OptionSetDialog.this.selectedLine, variantService);
					constraintModel = new ConstraintsModel(OptionSetDialog.this.selectedLine.getItem().getProperty("item_id"), lineMvl, null, OptionSetDialog.this.selectedLine, variantService);
					constraintModel.parse();
					
					editconstraintsdialog = new EditConstraintsDialog(AIFUtility.getActiveDesktop().getFrame(), moduleModel, constraintModel, null, false);
					editconstraintsdialog.addWindowListener(new WindowAdapter(){
						@Override
						public void windowClosed(WindowEvent windowevent) {
							try{
								OptionSetDialog.this.moduleConstraintList = OptionManager.getModuleConstraints(OptionSetDialog.this.selectedLine, true);
								Vector<Vector> moduleConstraintData = new Vector();
			    				for( String[] moduleConstraint: OptionSetDialog.this.moduleConstraintList){
			    					Vector row = new Vector();
			    					for( String str : moduleConstraint){
			    						row.add(str == null ? "":str);
			    					}
			    					moduleConstraintData.add(row);
			    				}
								
								DefaultTableModel model = (DefaultTableModel)moduleConstraintTable.getModel();
								model.setDataVector(moduleConstraintData, moduleConstraintHeader);
								SwingUtilities.invokeLater(new Runnable(){

									@Override
									public void run() {
										try {
											OptionSetDialog.this.selectedLine.window().save();
										} catch (TCException e) {
											e.printStackTrace();
										}
									}
			    					
			    				});
							}catch(TCException tce){
								tce.printStackTrace();
							}
							super.windowClosed(windowevent);
						}
					});
					editconstraintsdialog.run();
		        }
		        catch(Exception e)
		        {
		        	e.printStackTrace();
		        }finally{
		        	
		        }
				
		}
	}
	
	/**
	 * �ɼǰ� �������� ����
	 */
	private void moduleConstraintDel(){
		//��� ���� ���� ����
		final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
		waitProgress.start();
		OptionSetOperation operation = new OptionSetOperation(OptionSetDialog.this.selectedLine, 
				OptionSetDialog.this.userDefineErrorList, OptionSetDialog.this.moduleConstraintList, OptionSetDialog.this.manager, 
				moduleConstraintTable, waitProgress, OptionSetOperation.MODULE_CONSTRAINT_DELETE);
		operation.addOperationListener(new InterfaceAIFOperationListener(){

			@Override
			public void endOperation() {
				waitProgress.dispose();
				SwingUtilities.invokeLater(new Runnable(){

					@Override
					public void run() {
						try {
							OptionSetDialog.this.selectedLine.window().save();
						} catch (TCException e) {
							e.printStackTrace();
						}
					}
					
				});
				
			}

			@Override
			public void startOperation(String arg0) {
				
			}
			
		});
		OptionSetDialog.this.selectedLine.getSession().queueOperation(operation);
	}
}
