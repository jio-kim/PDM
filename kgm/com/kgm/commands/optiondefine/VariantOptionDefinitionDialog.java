package com.kgm.commands.optiondefine;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

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

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.ui.GlassPaneForReq;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.StringUtil;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.pse.variants.modularvariants.CustomMVPanel;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * Corporate Option 정의 dialog 생성
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class VariantOptionDefinitionDialog extends AbstractAIFDialog {

	public static final String CREATE_TITLE = "Option Creation";
	public static final String CHANGE_TITLE = "Option Modification";
	
	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private JTextField optionCodeTF;
	private JTextField optionCodeDescTF;
	private JTextField optionNameTF;
	private JTextField optionNameDescTF;
	
	private Vector headerVector = new Vector();
	private Vector allValueHeaderVector = new Vector();
	private int[] columnWidth = {1, 90, 120};
	private Vector<Vector> data = new Vector<Vector>();
//	private VariantOption option = null;
	private TCComponentBOMLine targetLine = null;
	private JTextField filePath;
	private Registry registry = null;
	private File selectedFile = null;
	private JTable allValueTable;
	private ArrayList<VariantOption> optionSet = null;
	private Vector<Vector> allData = new Vector<Vector>();
	private JButton addBtn = null;
	private JButton delBtn = null;
	private JButton delOptionBtn = null;
	
	private JPanel creationMainPanel = null;
	private OptionManager manager = null;
	private List usedOptionList = null;
	private GlassPaneForReq glassPane = null;
	
	/**
	 * Create the dialog.
	 * @wbp.parser.constructor
	 */
	public VariantOptionDefinitionDialog(JDialog parent, VariantOption option) {
		super(parent, true);
		init();
	}
	
	/**
	 * Create the dialog.
	 * @wbp.parser.constructor
	 */
	public VariantOptionDefinitionDialog(TCComponentBOMLine targetLine, ArrayList<VariantOption> optionSet, OptionManager manager) {
		super(AIFUtility.getActiveDesktop(), false);
		this.targetLine = targetLine;
		this.optionSet = optionSet;
		this.manager = manager;
		init();
	}
	
	/**
	 * Dialog 초기화.
	 */
	private void init(){
		setTitle("Option Definition");
		setBounds(100, 100, 720, 567);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		
		//사용된 옵션 목록 전체를 가져온다.
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("item_rev_puid", targetLine.getItemRevision().getUid());
			usedOptionList = (List)remote.execute("com.kgm.service.VariantService", "getUsedOptions", ds);
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		this.registry = Registry.getRegistry(this);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		headerVector.add("VARIANT_VALUE");
		headerVector.add("OPTION CODE");
		headerVector.add("OPTION DESC");
		ArrayList reqComs = new ArrayList();
		
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			creationMainPanel = new JPanel();
			creationMainPanel.setBackground(Color.WHITE);
			creationMainPanel.setBorder(new TitledBorder(null, CREATE_TITLE, TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(creationMainPanel, BorderLayout.WEST);
			creationMainPanel.setLayout(new BorderLayout(0, 0));
			{
				{
					{
						optionNameTF = new JTextField();
						optionNameTF.setColumns(10);
					}
				}
				{
					{
						optionNameDescTF = new JTextField();
						optionNameDescTF.setColumns(10);
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				creationMainPanel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				JPanel panel_1_1 = new JPanel();
				panel_1.add(panel_1_1, BorderLayout.NORTH);
				panel_1_1.setLayout(new GridLayout(2, 1, 0, 0));
				
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBackground(Color.WHITE);
					FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					panel_1_1.add(panel_2);
					JLabel lblNewLabel = new JLabel("Option Category");
					lblNewLabel.setPreferredSize(new Dimension(100, 15));
					panel_2.add(lblNewLabel);
					panel_2.add(optionNameTF);
				}
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBackground(Color.WHITE);
					FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					panel_1_1.add(panel_2);
					{
						JLabel lblNewLabel_1 = new JLabel("Option Desc.");
						lblNewLabel_1.setPreferredSize(new Dimension(100, 15));
						panel_2.add(lblNewLabel_1);
					}
					
					panel_2.add(optionNameDescTF);
				}
				JPanel panel_1_2 = new JPanel();
				panel_1_2.setBackground(Color.WHITE);
				panel_1.add(panel_1_2, BorderLayout.CENTER);
				panel_1_2.setBorder(new TitledBorder(null, "Allowed Value", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel_1_2.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_2_2 = new JPanel();
					panel_1_2.add(panel_2_2, BorderLayout.NORTH);
					panel_2_2.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_2_1 = new JPanel();
						panel_2_1.setBackground(Color.WHITE);
						panel_2_2.add(panel_2_1, BorderLayout.CENTER);
						FlowLayout fl_panel_2_1 = (FlowLayout) panel_2_1.getLayout();
						fl_panel_2_1.setAlignment(FlowLayout.LEADING);
						{
							optionCodeTF = new JTextField();
							panel_2_1.add(optionCodeTF);
							optionCodeTF.setColumns(10);
						}
						{
							optionCodeDescTF = new JTextField();
							panel_2_1.add(optionCodeDescTF);
							optionCodeDescTF.setColumns(10);
						}
					}
					{
						JPanel panel_2_1 = new JPanel();
						panel_2_1.setBackground(Color.WHITE);
						panel_2_2.add(panel_2_1, BorderLayout.SOUTH);
						{
							//경우에 따라 수정버튼으로 변경 되기도 함.
							//옵션값을 추가함(옵션테이블에 임시로 추가됨)
							addBtn = new JButton("Add");
							addBtn.setBackground(Color.WHITE);
							addBtn.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									
									if( optionCodeTF.getText().trim().equals("")) return;
									
									DefaultTableModel model = (DefaultTableModel)table.getModel();
									
									if( addBtn.getText().equals("Add")){
										for( int i = 0; i < model.getRowCount(); i++){
											String code = (String)model.getValueAt(i, 1);
											if( optionCodeTF.getText().trim().equals(code)){
												MessageBox.post(VariantOptionDefinitionDialog.this, registry.getString("OptionDefine.aleadyInUse"), "INFORMATION", MessageBox.WARNING);
												return;
											}
										}
										
										Vector row = new Vector();
										VariantValue value = new VariantValue(null, optionCodeTF.getText().toUpperCase(), optionCodeDescTF.getText(), VariantValue.VALUE_NOT_USE, true);
										row.add(value);
										row.add(optionCodeTF.getText().toUpperCase());
										row.add(optionCodeDescTF.getText());
										
										model.addRow(row);
									}else{
										for( int i = 0; i < model.getRowCount(); i++){
											VariantValue value = (VariantValue)model.getValueAt(i, 0);
											if( value.getValueName().equals(optionCodeTF.getText())){
												value.setValueDesc(optionCodeDescTF.getText());
												model.setValueAt(optionCodeDescTF.getText(), i, 2);
												break;
											}
										}
										
									}
									
								}
							});
							panel_2_1.add(addBtn);
						}
						{
							//추가된 옵션값을 삭제하는 기능
							//옵션 테이블에 존재하는 옵션을 삭제함. 해당 옵션이 삭제되지 않는 경우만 삭제 가능하며, 사용중일때는 테이블에서 회색으로 표기됨.
							delBtn = new JButton("Del");
							delBtn.setBackground(Color.WHITE);
							delBtn.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									
									DefaultTableModel model = (DefaultTableModel)table.getModel();
									int[] selectedIdxs = table.getSelectedRows();
									TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)table.getRowSorter();
									for( int i = selectedIdxs.length - 1; i >= 0; i--){
										int modelIdx = sorter.convertRowIndexToModel(selectedIdxs[i]);
										VariantValue value = (VariantValue)model.getValueAt(modelIdx, 0);
										if( value.isNew() || (value.getValueStatus() != VariantValue.VALUE_USE)){
											
											for( int j = model.getRowCount() - 1; j >= 0; j--){
												VariantValue val = (VariantValue)model.getValueAt(j, 0);
												if( val.equals(value)){
													model.removeRow(j);
													break;
												}
											}
										}
									}
								}
							});
							panel_2_1.add(delBtn);
						}
					}
				}
				
				TableModel model = new DefaultTableModel(data, headerVector) {
					public Class getColumnClass(int col) {
						if( col == 0 ){
							return VariantValue.class;
						}
						return String.class;
					}

					public boolean isCellEditable(int row, int col) {
						return false;
					}
			    };
			    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
			    
				table = new JTable(model);
				table.setRowSorter(sorter);
				ListSelectionModel lsm = table.getSelectionModel();
				lsm.addListSelectionListener(new ListSelectionListener(){

					@Override
					public void valueChanged(ListSelectionEvent event) {
						if( table.getSelectedRow() >= table.getRowCount() || table.getSelectedRow() < 0){
							return;
						}
						TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)table.getRowSorter();
						int modelRowIdx = sorter.convertRowIndexToModel(table.getSelectedRow());
						DefaultTableModel model = (DefaultTableModel)table.getModel();
						VariantValue value = (VariantValue)model.getValueAt(modelRowIdx, 0);
						if( value.getValueStatus() == VariantValue.VALUE_USE){
							delBtn.setEnabled(false);
						}else{
							delBtn.setEnabled(true);
						}
					}
					
				});
				
				table.addMouseListener(new MouseAdapter(){

					@Override
					public void mouseReleased(MouseEvent e) {
						if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
								&& e.isControlDown()==false) {
							addBtn.setText("Mod");
							TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)table.getRowSorter();
							int modelRowIdx = sorter.convertRowIndexToModel(table.getSelectedRow());
							DefaultTableModel model = (DefaultTableModel)table.getModel();
							VariantValue value = (VariantValue)model.getValueAt(modelRowIdx, 0);
							optionCodeTF.setText(value.getValueName());
							optionCodeDescTF.setText(value.getValueDesc());
						}
						super.mouseReleased(e);
					}
					
				});
				JScrollPane pane = new JScrollPane();
				pane.setPreferredSize(new Dimension(2, 150));
				pane.setViewportView(table);
				pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				pane.getViewport().setBackground(Color.WHITE);
				
				panel_1_2.add(pane, BorderLayout.CENTER);
				reqComs.add(table);
				{
					JPanel panel_1_3 = new JPanel();
					panel_1_3.setBackground(Color.WHITE);
					panel_1.add(panel_1_3, BorderLayout.SOUTH);
					panel_1_3.setBorder(new TitledBorder(null, "Request Based", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					panel_1_3.setLayout(new BorderLayout(0, 0));
					{
						filePath = new JTextField();
						filePath.setBackground(Color.WHITE);
						filePath.setEditable(false);
						panel_1_3.add(filePath, BorderLayout.CENTER);
						filePath.setColumns(10);
					}
					{
						//근거 문서를 첨부하는 기능.
						// 옵션이 추가 및 수정될 때 근거 문서를 첨부 할 수 있다.
						// 추가된 문서는 옵션과 동일한 이름의 Form에 DataSet형태로 연결된다.
						JButton uploadBtn = new JButton("");
						uploadBtn.setBackground(Color.WHITE);
						
						uploadBtn.setIcon(registry.getImageIcon("SEARCH.ICON"));
						uploadBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								
								JFileChooser fileChooser = new JFileChooser();
								fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
								fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("HTML"));
								fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("PDF"));
								fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("Text"));
								fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSWord"));
								fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSPowerPoint"));
								fileChooser.setFileFilter(new OptionDefinitionFileFilter("MSExcel"));
								
								int result = fileChooser.showOpenDialog(VariantOptionDefinitionDialog.this);
								if( result == JFileChooser.APPROVE_OPTION){
									selectedFile = fileChooser.getSelectedFile();
									if( selectedFile != null)
										filePath.setText(selectedFile.getPath());
								}
								
							}
						});
						panel_1_3.add(uploadBtn, BorderLayout.EAST);
					}
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBackground(Color.WHITE);
				creationMainPanel.add(panel_1, BorderLayout.SOUTH);
				{
					//추가된 옵션값을 Corporate Option Item에 정의하고 근거문서를 저장함.
					JButton okBtn = new JButton("OK");
					okBtn.setBackground(Color.WHITE);
					okBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							
							if( !glassPane.isValid() ){
								ArrayList<JComponent> invalidComs = glassPane.getInvalidComponents();
								if( invalidComs != null && invalidComs.size() > 0){
									invalidComs.get(0).setFocusable(true);
									invalidComs.get(0).requestFocus();
									MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("OptionDefine.validation.Fail"), "INFORMATION", MessageBox.WARNING);
									return;
								}
							}
							
							TitledBorder border = (TitledBorder)creationMainPanel.getBorder();
							String curTitle = border.getTitle();
							
							VariantOptionDefinitionOperation operation = new VariantOptionDefinitionOperation(targetLine, 
									selectedFile, optionNameTF.getText().trim().toUpperCase(), optionNameDescTF.getText(), (DefaultTableModel)table.getModel(), curTitle.equals(CREATE_TITLE) ? VariantOptionDefinitionOperation.CREATE_OPTION:VariantOptionDefinitionOperation.UPDATE_OPTION);
							operation.addOperationListener(new InterfaceAIFOperationListener(){

								@Override
								public void endOperation() {

									fieldInit();
									
									VariantOptionDefinitionDialog.this.manager.clear(false);
									try {
										VariantOptionDefinitionDialog.this.manager = new OptionManager(targetLine, true);
										VariantOptionDefinitionDialog.this.optionSet = manager.getOptionSet(targetLine, null, null);
										
										if( optionSet != null){
											allData.removeAllElements();
											for( VariantOption option : optionSet){
												List<VariantValue> values = option.getValues();
												for( VariantValue value : values){
													Vector row = new Vector();
													
													//DB접속이 안될경우 수정이 불가능하도록 하기위해 result == null일 경우는 사용중이라고 가정함.
													if( usedOptionList == null){
														value.setValueStatus(VariantValue.VALUE_USE);
													}else{
														if( usedOptionList.contains(option.getOptionName())){
															value.setValueStatus(VariantValue.VALUE_USE);
														}else{
															value.setValueStatus(VariantValue.VALUE_NOT_USE);
														}
													}
													
													row.add(value);
													row.add(option.getOptionName());
													row.add(option.getOptionDesc());
													row.add(value.getValueName());
													row.add(value.getValueDesc());
													allData.add(row);
												}
											}
											DefaultTableModel model = (DefaultTableModel)allValueTable.getModel();
											model.fireTableDataChanged();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}

								@Override
								public void startOperation(String arg0) {

								}
								
							});
							targetLine.getSession().queueOperationLater(operation);
							
						}
					});
					panel_1.add(okBtn);
				}
				{
					JButton clearBtn = new JButton("CLEAR");
					clearBtn.setBackground(Color.WHITE);
					clearBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							fieldInit();
						}
					});
					panel_1.add(clearBtn);
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(Color.WHITE);
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.setBackground(Color.WHITE);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						VariantOptionDefinitionDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		
		reqComs.add(optionNameTF);
		reqComs.add(optionNameDescTF);
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel.add(panel_1, BorderLayout.WEST);
				panel_1.setLayout(new GridLayout(3, 1, 0, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBackground(Color.WHITE);
					panel_1.add(panel_2);
				}
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBackground(Color.WHITE);
					panel_1.add(panel_2);
					{
						// "<<"
						//특정 옵션을 선택 후 수정(새로운 옵션 추가, 옵션 Desc 수정, 옵션값 제거)함.
						JButton button = new JButton(){

							@Override
							public Dimension getPreferredSize() {
								return new Dimension(50, 40);
							}
							
						};
						button.setBackground(Color.WHITE);
						button.addActionListener(new ActionListener(){

							@Override
							public void actionPerformed(ActionEvent arg0) {
								VariantOptionDefinitionDialog.this.editOption();
							}
							
						});
						panel_2.add(button);
						button.setIcon(registry.getImageIcon("ProuctOptionManageBackArrow2.ICON"));
					}
				}
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBackground(Color.WHITE);
					panel_1.add(panel_2);
				}
			}
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBackground(Color.WHITE);
				panel_1.setBorder(new TitledBorder(null, "Option List", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_1_1 = new JPanel();
					panel_1_1.setBackground(Color.WHITE);
					panel_1.add(panel_1_1, BorderLayout.NORTH);
					FlowLayout fl_panel_1_1 = (FlowLayout) panel_1_1.getLayout();
					fl_panel_1_1.setAlignment(FlowLayout.LEADING);
					{
						JLabel lblNewLabel_2 = new JLabel("Target : ");
						lblNewLabel_2.setFont(new Font("굴림", Font.PLAIN, 12));
						panel_1_1.add(lblNewLabel_2);
					}
					{
						JLabel lblNewLabel_3 = new JLabel(targetLine.toDisplayString());
						lblNewLabel_3.setFont(new Font(Font.SERIF, Font.BOLD, 12));
						panel_1_1.add(lblNewLabel_3);
					}
				}
				{
					JPanel panel_1_1 = new JPanel();
					panel_1.add(panel_1_1, BorderLayout.CENTER);
					panel_1_1.setLayout(new BorderLayout(0, 0));
					{
						if( optionSet != null){
							for( VariantOption option : optionSet){
								List<VariantValue> values = option.getValues();
								for( VariantValue value : values){
									Vector row = new Vector();
									//DB접속이 안될경우 수정이 불가능하도록 하기위해 result == null일 경우는 사용중이라고 가정함.
									if( usedOptionList == null){
										value.setValueStatus(VariantValue.VALUE_USE);
									}else{
										if( usedOptionList.contains(option.getOptionName())){
											value.setValueStatus(VariantValue.VALUE_USE);
										}else{
											value.setValueStatus(VariantValue.VALUE_NOT_USE);
										}
									}
									row.add(value);
									row.add(option.getOptionName());
									row.add(option.getOptionDesc());
									row.add(value.getValueName());
									row.add(value.getValueDesc());
									allData.add(row);
								}
							}
						}
						allValueHeaderVector.add("VALUE");
						allValueHeaderVector.add("CATEGORY");
						allValueHeaderVector.add("CATEGORY_DESC");
						allValueHeaderVector.add("OPTION_CODE");
						allValueHeaderVector.add("OPTION_DESC");
						TableModel model = new DefaultTableModel(allData, allValueHeaderVector) {
							public Class getColumnClass(int col) {
								return String.class;
							}

							public boolean isCellEditable(int row, int col) {
								return false;
							}
						};
						
						allValueTable = new JTable(model);

					    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
					    allValueTable.setRowSorter(sorter);
					    allValueTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								//테이블에서 특정 Row를 선택하여 더블클릭함.
								//수정 모드로 변경.
								if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
										&& e.isControlDown()==false) {
									editOption();
								}
								super.mouseReleased(e);
							}
							
						});
					    allValueTable.getSelectionModel().addListSelectionListener(new ListSelectionListener(){

							@Override
							public void valueChanged(ListSelectionEvent event) {
								int row = allValueTable.getSelectedRow();
								TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)allValueTable.getRowSorter();
								if( row >= allValueTable.getRowCount() || row < 0){
									return;
								}
								int modelIdx = sorter.convertRowIndexToModel(row);
								VariantValue value = (VariantValue)allValueTable.getModel().getValueAt(modelIdx, 0);
								if( value.getValueStatus() == VariantValue.VALUE_NOT_USE ){
									delOptionBtn.setEnabled(true);
								}else{
									delOptionBtn.setEnabled(false);
								}
							}
					    	
					    });
					    allValueTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					    allValueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					    
					    JScrollPane pane = new JScrollPane();
					    pane.setBackground(Color.WHITE);
					    pane.setPreferredSize(new Dimension(390,250));
						pane.setViewportView(allValueTable);
						pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						pane.getViewport().setBackground(Color.WHITE);
						panel_1_1.add(pane);
					}
				}
				{
					JPanel panel_1_1 = new JPanel();
					panel_1_1.setBackground(Color.WHITE);
					panel_1.add(panel_1_1, BorderLayout.SOUTH);
					FlowLayout fl_panel_1_1 = (FlowLayout) panel_1_1.getLayout();
					fl_panel_1_1.setAlignment(FlowLayout.RIGHT);
					{
						delOptionBtn = new JButton("DELETE OPTION");
						delOptionBtn.setBackground(Color.WHITE);
						delOptionBtn.setEnabled(false);
						delOptionBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								int selectedIdx = allValueTable.getSelectedRow();
								if( selectedIdx < 0 ) return;
								
								DefaultTableModel model = (DefaultTableModel)allValueTable.getModel();
								TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)allValueTable.getRowSorter();
								int modelIdx = sorter.convertRowIndexToModel(selectedIdx);
								VariantValue value = (VariantValue)model.getValueAt(modelIdx, 0);
								VariantOption option = value.getOption();
								int reply = ConfirmationDialog.post("Delete Option", StringUtil.getString(registry, "OptionDefine.optionDeleteQuestion", new String[]{option.getOptionName()}));
								if( reply == ConfirmationDialog.YES){
									
									try{
						        		CustomMVPanel.deleteOption(targetLine.getSession().getVariantService(), targetLine, option.getOveOption());
						        		targetLine.refresh();
						        		targetLine.window().save();
						        	}catch(TCException tce){
						        		tce.printStackTrace();
						        		//삭제 실패시 리턴.
						        		return;
						        	}finally{
						        		
						        	}
									
									//테이블에서 삭제
									for(int i = allData.size() - 1; i >= 0; i--){
										Vector row = allData.get(i);
										if( row.get(1).equals(option.getOptionName())){
											allData.remove(i);
										}
									}
									
									// 근거 문서 form찾기
									TCComponent[] com = null;
									try {
										com = CustomUtil.queryComponent("General...", new String[]{"Type", "Name"}, new String[]{"Option Request Based", option.getOptionName()});
										if( com != null && com.length > 0){
											TCComponentForm form = (TCComponentForm)com[0];
											AIFComponentContext[] contexts = form.getRelated("s7_CONTENTS");
											for( int i = 0; contexts != null && i < contexts.length; i++){
												AIFComponentContext context = contexts[i];
												((TCComponentDataset)context.getComponent()).delete();
											}
											form.delete();
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
									
									model.fireTableDataChanged();
								}
							}
						});
						{
							JButton exportBtn = new JButton("Export");
							exportBtn.setBackground(Color.WHITE);
							exportBtn.setOpaque(true);
							exportBtn.setIcon(new ImageIcon(VariantOptionDefinitionDialog.class.getResource("/com/kgm/common/images/excel_16.png")));
							
							//Excel로 Export 함.
							exportBtn.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(ActionEvent actionevent) {
									JFileChooser fileChooser = new JFileChooser();
									fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
									Calendar now = Calendar.getInstance();
									SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
									sdf.format(now.getTime());
									File defaultFile = new File("Option_Difine_" + sdf.format(now.getTime()) + ".xls");
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
									int result = fileChooser.showSaveDialog(VariantOptionDefinitionDialog.this);
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
							panel_1_1.add(exportBtn);
						}
						panel_1_1.add(delOptionBtn);
					}
				}
			}
		}
		columnInit();
		glassPane = new GlassPaneForReq(reqComs, this);
		setGlassPane(glassPane);
		glassPane.setVisible(true);
		
	}
	
	/**
	 * selectedFile 엑셀 파일로 결과를 Export함.
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

	    label = new jxl.write.Label(1, startRow, "Option Dictionary(" + this.targetLine.toDisplayString() + ")", cellFormat);
	    sheet.addCell(label);
	    sheet.mergeCells(1, 1, 3, 1);
	    
	    startRow = 3;
	    Vector excelColumnHeader = new Vector();
	    excelColumnHeader.add("CATEGORY");
	    excelColumnHeader.add("CATEGORY_DESC");
	    excelColumnHeader.add("OPTION_CODE");
	    excelColumnHeader.add("OPTION_DESC");
	    excelColumnHeader.add("IN_USE");
	    
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
	      cv.setAutosize(true);
	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    startRow = 4;
	    Vector<Vector> data = allData;
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	for (int j = 0; j < row.size(); j++)
	    	{
	    		String str = "";
	    		//마지막 컬럼은 0번째 VariantValue에서 값을 가져온다.
	    		if( j == 4 ){
	    			VariantValue value = (VariantValue)row.get(0);
	    			if( value.getValueStatus() == VariantValue.VALUE_USE){
	    				str = "Y";
	    			}else if( value.getValueStatus() == VariantValue.VALUE_NOT_USE){
	    				str = "N";
	    			}else{
	    				str = "-";
	    			}
	    		}else{
	    			str = (String)row.get(j + 1);
	    		}
	    		
	    		label = new jxl.write.Label(j + initColumnNum, i + startRow, str, cellFormat);
	    		sheet.addCell(label);
	    	}
	    }

	    //셀 Merge
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
	 * 옵션 값을 더블 클릭하거나 "<="를 클릭시에, 선택된 옵션카테고리를 수정할 수 있도록
	 * 수정 패널에 셋팅된다.  
	 */
	private void editOption(){
		int selectedIdx = this.allValueTable.getSelectedRow();
		if( selectedIdx < 0) return;
		DefaultTableModel tableModel = (DefaultTableModel)allValueTable.getModel();
		TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>)allValueTable.getRowSorter();
		int modelIdx = sorter.convertRowIndexToModel(selectedIdx);
		VariantValue selectedValue = (VariantValue)tableModel.getValueAt(modelIdx, 0);
		VariantOption option = selectedValue.getOption();
		
		TitledBorder border = (TitledBorder)creationMainPanel.getBorder();
		border.setTitle(CHANGE_TITLE);
		creationMainPanel.repaint();
		optionNameTF.setText(option.getOptionName());
		optionNameTF.setEditable(false);
		optionNameDescTF.setText(option.getOptionDesc());
		
		addBtn.setText("Add");
		delBtn.setEnabled(false);
		optionCodeTF.setText("");
		optionCodeDescTF.setText("");
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		Vector data = model.getDataVector();
		data.removeAllElements();
		if( option.hasValues()){
			
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("option_name", option.getOptionName());
			Integer result = null;
			try {
				//OPtion이 사용되는 횟수 리턴(Condition포함)
				result = (Integer)remote.execute("com.kgm.service.VariantService", "getUsedCount", ds);
			} catch (Exception e) {
				result = 100;//DB에서 오류가 나면 수정 불가능 하도록 설정함.
			}
			List<VariantValue> values = option.getValues();
			for( VariantValue value : values){
				Vector row = new Vector();
				if( result.intValue() > 0){
					value.setValueStatus(VariantValue.VALUE_USE);
				}else{
					value.setValueStatus(VariantValue.VALUE_NOT_USE);
				}
				row.add(value);
				row.add(value.getValueName());
				row.add(value.getValueDesc());
				data.add(row);
			}
			model.fireTableDataChanged();
		}
		
		selectedFile = null;
		filePath.setText("");
		
	}
		
	/**
	 * 카테고리 추가(수정)패널을 초기화함.
	 */
	private void fieldInit(){
		
		TitledBorder border = (TitledBorder)creationMainPanel.getBorder();
		border.setTitle(CREATE_TITLE);
		creationMainPanel.repaint();
		optionNameTF.setText("");
		optionNameTF.setEditable(true);
		optionNameDescTF.setText("");
		addBtn.setText("Add");
		delBtn.setEnabled(true);
		optionCodeTF.setText("");
		optionCodeDescTF.setText("");
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		model.getDataVector().removeAllElements();
		model.fireTableDataChanged();
		selectedFile = null;
		filePath.setText("");
	}
	
	/**
	 * 테이블을 초기화함.
	 */
	public void columnInit(){
		TableColumnModel columnModel = table.getColumnModel();
		int n = headerVector.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
			columnModel.getColumn(i).setWidth(columnWidth[i]);
		}

		VariantOptionValueCellRenderer cellRenderer = new VariantOptionValueCellRenderer();
		for( int i = 0; i < columnModel.getColumnCount(); i++){
			TableColumn column = columnModel.getColumn(i);
			column.setCellRenderer(cellRenderer);
		}
		table.removeColumn(table.getColumn("VARIANT_VALUE"));
		
		int[] columnW = {1, 80, 120, 80, 120};
		columnModel = allValueTable.getColumnModel();
		n = allValueHeaderVector.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnW[i]);
			columnModel.getColumn(i).setWidth(columnW[i]);
		}
		
		cellRenderer = new VariantOptionValueCellRenderer();
		for( int i = 0; i < columnModel.getColumnCount(); i++){
			TableColumn column = columnModel.getColumn(i);
			column.setCellRenderer(cellRenderer);
		}
		allValueTable.removeColumn(allValueTable.getColumn("VALUE"));
	}
	
	
	/**
	 * File Chooser Dialog에 필터형식을 추가함.
	 * 
	 * @author slobbie
	 *
	 */
	public class OptionDefinitionFileFilter extends FileFilter{

		private String type = null;
		
		public OptionDefinitionFileFilter(String type){
			this.type = type;
		}
		
		@Override
		public boolean accept(File f) {
			if( f.isFile()){
				
				String fileName = f.getName().toLowerCase();
				if (type.equals("MSExcel") || type.equals("MSExcelX")) {
					return fileName.endsWith("xls") || fileName.endsWith("xlsx");
				} else if (type.equals("MSWord") || type.equals("MSWordX")) {
					return fileName.endsWith("doc") || fileName.endsWith("docx");
				} else if (type.equals("MSPowerPoint") || type.equals("MSPowerPointX")) {
					return fileName.endsWith("ppt") || fileName.endsWith("pptx");
				} else if (type.equals("Text")) {
					return fileName.endsWith("txt");
				} else if (type.equals("PDF")) {
					return fileName.endsWith("pdf");
				} else if (type.equals("HTML")) {
					return fileName.endsWith("html") || fileName.endsWith("htm");
				} else if (type.equals("ACADDWG")) {
					return fileName.endsWith("dwg");
				} else if (type.equals("JPEG")) {
					return fileName.endsWith("jpg");
				} else if (type.equals("GIF")) {
					return fileName.endsWith("gif");
				} else if (type.equals("IMAGE")) {
					return fileName.endsWith("jpeg") || fileName.endsWith("png") || fileName.endsWith("tif")
							|| fileName.endsWith("tiff") || fileName.endsWith("bmp");
				} else if (type.equals("ZIP")) {
					return fileName.endsWith("zip");
				} else if (type.equals("EML")) {
					return fileName.endsWith("eml");
				} 
				
			}
			return false;
		}

		@Override
		public String getDescription() {
			if (type.equals("MSExcel") || type.equals("MSExcelX")) {
				return "*.xls, *.xlsx";
			} else if (type.equals("MSWord") || type.equals("MSWordX")) {
				return "*.doc, *.docx";
			} else if (type.equals("MSPowerPoint") || type.equals("MSPowerPointX")) {
				return "*.ppt, *.pptx";
			} else if (type.equals("Text")) {
				return "*.txt";
			} else if (type.equals("PDF")) {
				return "*.pdf";
			} else if (type.equals("HTML")) {
				return "*.htm, *.html";
			} else if (type.equals("ACADDWG")) {
				return "*.dwg";
			} else if (type.equals("JPEG")) {
				return "*.jpg";
			} else if (type.equals("GIF")) {
				return "*.gif";
			} else if (type.equals("IMAGE")) {
				return "*.jpeg, *.png *.tif *.tiff *.bmp";
			} else if (type.equals("ZIP")) {
				return "*.zip";
			} else if (type.equals("EML")) {
				return "*.eml";
			}
			
			return "*." + type;
		}
		
	}
}
