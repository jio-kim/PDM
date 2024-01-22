package com.ssangyong.commands.ifpe;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.ui.CheckComboBox;
import com.ssangyong.common.ui.MultiLineToolTip;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.toedter.calendar.JDateChooser;

/**
 * [SR150723-009][20150723] shcho, PE Interface에서 Function 단위 재전송이 가능하도록 기능 변경
 * 
 * @author GE70KE02
 *
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked", "unused"})
public class TcToPeInterfaceDialog extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JDateChooser registeredAfter;
	private JDateChooser registeredBefore;
	private JDateChooser completedAfter;
	private JDateChooser completedBefore;
	
	private Vector productHeaderVector = new Vector();
	private Vector functionHeaderVector = new Vector();
	private Vector ecoHeaderVector = new Vector();
	private Vector noTransHeaderVector = new Vector();
	private int[] projectColumnWidth = {60, 100, 100, 200, 200,1,1};
	private int[] functionColumnWidth = {100, 100, 100, 200, 200,1,1};
	private int[] ecoColumnWidth = {120, 90, 90, 100, 150, 150,1,1};
	private int[] noTransColumnWidth = {60, 100, 100, 200, 200,1,1};
	private JTable productTable;
	private JTable functionTable;
	private JTable ecoTable;
	private JTable noTransTable;
	private JTextField ecoTF;
	private JComboBox productComboForProduct = new JComboBox();
	private JComboBox productComboForFunction = new JComboBox();
	private JComboBox productComboForNoTrans = new JComboBox();
	private JButton transmitBtn = null;
	private JButton btnClear = null;
	private JButton funcTransBtn = null;
	private JButton funcClearBtn = null;
	private JButton ecoSearchBtn = null;

	private JComboBox projectCombo = null;
	private JComboBox statCombo = null;
	private Registry registry = null;
	private TCComponentItemRevision productRevision = null;
	
	public static final String SELECT_A_PROJECT = "select a Project";
	public static final String IF_STAT_CREATION = "CREATION";
	public static final String IF_STAT_PROCESSING = "PROCESSING";
	public static final String IF_STAT_ERROR = "ERROR";
	public static final String IF_STAT_WAITING = "WAITING";
	public static final String IF_STAT_SUCCESS = "SUCCESS";
	public static final String IF_STAT_FAIL = "FAIL";
	public static final String IF_STAT_CLEARED = "CLEARED";
	
	public static final String SELECT_PRODUCT = "Select a Product";
	
	public boolean bPreviousNoTrans = false;
	
	private String WAS_URL = "http://127.0.0.1:7080/symcweb/remote/invoke.do";
	
	private CheckComboBox functionCombo = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			TcToPeInterfaceDialog dialog = new TcToPeInterfaceDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 * @throws Exception 
	 */
	public TcToPeInterfaceDialog() throws Exception {
		
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setTitle("Transmit History View");
		registry = Registry.getRegistry(this);
		transmitBtn = new JButton("Transmit to PE");
		
		//Preferences에 설정한 이유는 운영은 WAS가 2개이며, 이중 한곳에만 PE I/F를 위한 WAR가 설정됨.
		//실행되는 WAR IP를 Preference를 통해 가져오도록 처리함. 
		TCSession session = CustomUtil.getTCSession();
		TCPreferenceService tcpreferenceservice = session.getPreferenceService();
//		String portalWebServer = tcpreferenceservice.getString(0, "PE_IF_HOST_IP");
		String portalWebServer = tcpreferenceservice.getStringValue("PE_IF_HOST_IP");
		
		WAS_URL = "http://" + portalWebServer + "/symcweb/remote/invoke.do";
		
		//존재하는 Product Item을 모두 검색
		TCComponent[] products = CustomUtil.queryComponent("Item...", new String[]{"Type"}, new String[]{"Product"});
		
		getContentPane().setBackground(Color.WHITE);
		setBounds(100, 100, 503, 398);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				JPanel productPanel = new JPanel();
				tabbedPane.addTab("Product", null, productPanel, null);
				productPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel gridPanel = new JPanel();
					productPanel.add(gridPanel, BorderLayout.NORTH);
					gridPanel.setLayout(new GridLayout(0, 1, 0, 0));
					{
						JPanel panel_1 = new JPanel();
						panel_1.setBackground(Color.WHITE);
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						gridPanel.add(panel_1);
						{
							productComboForProduct.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									try {
										reloadProductTransmissionInfo();
									} catch (Exception e) {
										e.printStackTrace();
										MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.notFoundTransInfo"), "INFORMATION", MessageBox.ERROR);
									}
								}
							});
							
							productComboForProduct.addItem(SELECT_PRODUCT);
							productComboForProduct.addItem("All");
							for( TCComponent product : products){
								productComboForProduct.addItem(product);
							}
							productComboForProduct.setPreferredSize(new Dimension(370, 21));
							panel_1.add(productComboForProduct);
							
						}
					}
				}
				{
					JPanel panel = new JPanel();
					productPanel.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BorderLayout(0, 0));
					{
						productHeaderVector.add("Project");
						productHeaderVector.add("Type");
						productHeaderVector.add("Status");
						productHeaderVector.add("Registered Date");
						productHeaderVector.add("Compmleted Date");
						productHeaderVector.add("LOG");
						productHeaderVector.add("IF_ID");
						TableModel model = new DefaultTableModel(null, productHeaderVector) {
							public Class getColumnClass(int col) {
								return String.class;
							}

							public boolean isCellEditable(int row, int col) {
								return false;
							}
					    };
					    productTable = new JTable(model);
					    productTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
					    productTable.setRowSorter(sorter);
					    
					    productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
					    ListSelectionModel rowSM = productTable.getSelectionModel();
					    rowSM.addListSelectionListener(new ListSelectionListener(){

							@Override
							public void valueChanged(
									ListSelectionEvent listselectionevent) {
								Object obj = productComboForProduct.getSelectedItem();	
								//System.out.println("asdfa");
								if( obj != null && (obj.equals(SELECT_PRODUCT) || obj.equals("All") )){
									transmitBtn.setEnabled(false);
									btnClear.setEnabled(false);
									return;
								}
								btnEnabler();
							}
					    	
					    });
					    
					    productTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
										&& e.isControlDown()==false) {
									
									JTable table = (JTable)e.getSource();
									DefaultTableModel model = (DefaultTableModel)table.getModel();
									int rowIdx = table.getSelectedRow();
									
									if( rowIdx > -1 ){
										int modelRowIdx = table.convertRowIndexToModel(rowIdx);
										String txt = (String)model.getValueAt(modelRowIdx, 5);
										TcToPeLogDialog logDlg = new TcToPeLogDialog(TcToPeInterfaceDialog.this);
										logDlg.getLogArea().setText(txt);
										logDlg.setVisible(true);
									}
								}
								super.mouseReleased(e);
							}
							
						});
					    columnWidthInit(productTable, productHeaderVector, projectColumnWidth);
					    JScrollPane pane = new JScrollPane();
						pane.setViewportView(productTable);
						pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						pane.getViewport().setBackground(Color.WHITE);
						
						panel.add(pane);
					}
					{
						JPanel panel_1 = new JPanel();
						panel_1.setBackground(Color.WHITE);
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.RIGHT);
						panel.add(panel_1, BorderLayout.SOUTH);
						{
							transmitBtn.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									
									try{
										Object obj = productComboForProduct.getSelectedItem();
										if( obj != null && (obj.equals(SELECT_PRODUCT) || obj.equals("All") )){
											return;
										}
										TCComponentItem item = (TCComponentItem)obj;
										SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
										DataSet ds = new DataSet();
										ArrayList<HashMap> noTransInfo = null;
										ds.put("PRODUCT_ID", item.getProperty("item_id"));
										noTransInfo = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getNoTransInfo", ds);
										if( noTransInfo != null && !noTransInfo.isEmpty()){
											MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.noTransWarning"), "INFORMATION", MessageBox.WARNING);
											return;
										}
											
										transmit();
									} catch (Exception e) {
										MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.transError"), "INFORMATION", MessageBox.WARNING);
										return;
									}finally{	
										try {
											reloadProductTransmissionInfo();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							});
							{
								btnClear = new JButton("Clear");
								btnClear.addActionListener(new ActionListener() {
									public void actionPerformed(ActionEvent actionevent) {
										
										Object obj = productComboForProduct.getSelectedItem();
										if( obj != null && (obj.equals(SELECT_PRODUCT) || obj.equals("All") )){
											return;
										}
										
										try {
											
											clear();
											btnClear.setEnabled(false);
											
										} catch (Exception e) {
											MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("ifpe.clearError"), "INFORMATION", MessageBox.WARNING);
											return;
										}finally{
											try {
												reloadProductTransmissionInfo();
											} catch (Exception e) {
												e.printStackTrace();
											}
										}
									}
								});
								btnClear.setEnabled(false);
								panel_1.add(btnClear);
							}
							transmitBtn.setEnabled(false);
							panel_1.add(transmitBtn);
						}
					}
				}
			}
			{
				JPanel functionPanel = new JPanel();
				tabbedPane.addTab("Function", null, functionPanel, null);
				functionPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					panel.setBackground(Color.WHITE);
					panel.setLayout(new BorderLayout(0, 0));
					functionPanel.add(panel, BorderLayout.NORTH);
					{

						JPanel panel_1 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel.add(panel_1, BorderLayout.CENTER);
						panel_1.add(productComboForFunction);
						productComboForFunction.addItem(SELECT_PRODUCT);
						
						for( TCComponent product : products){
							productComboForFunction.addItem(product);
						}
						
						productComboForFunction.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								try {
									
									Object obj = productComboForFunction.getSelectedItem();
									if( obj instanceof TCComponentItem){
										TCComponentItem item = (TCComponentItem)obj;
										String itemId = item.getProperty("item_id");									
										
										setFunctionList(itemId);
									}else{
										functionCombo.removeAllItems();
										functionCombo.addItem("");
										
										HashSet set = new HashSet();
										functionCombo.resetObjs(set, false);
									}
									reloadFunctionTransmissionInfo();
								} catch (Exception e) {
									e.printStackTrace();
									MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.notFoundTransInfo"), "INFORMATION", MessageBox.ERROR);
								}
							}
						});						
						
						productComboForFunction.setPreferredSize(new Dimension(370, 21));
						
						functionCombo = new CheckComboBox("Please select Functions"){

							@Override
							public JToolTip createToolTip() {
								MultiLineToolTip tip = new MultiLineToolTip();
						        tip.setComponent(this);
						        return tip;
							}
							
						};
						
						//columnWidthInit(functionTable, functionHeaderVector, functionColumnWidth);	
						
						panel_1.add(functionCombo);
						functionCombo.setPreferredSize(new Dimension(150, 20));
						functionCombo.setPopupWidth(400);
						functionCombo.addActionListener (new ActionListener () {
						    public void actionPerformed(ActionEvent e) {
						    	
						    	//filter();
						    }
						});
						functionCombo.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseEntered(MouseEvent arg0) {
								
								CheckComboBox obj = (CheckComboBox)(arg0.getSource());
								Object[] objs = obj.getSelectedItems();
								
								if( objs == null) {
									obj.setToolTipText(null);
									return;
								}
								
								String toolTipTxt = "";
								for( int i = 0; i < objs.length; i++){
									String val = objs[i].toString();
									toolTipTxt += (i==0 ? "":"\n" ) + val;
								}
								obj.setToolTipText(toolTipTxt);
								super.mouseEntered(arg0);
							}

							@Override
							public void mouseExited(MouseEvent arg0) {
								CheckComboBox obj = (CheckComboBox)(arg0.getSource());
								obj.setToolTipText(null);
								super.mouseExited(arg0);
							}
							
							
						});
					}
					{
						JPanel panel_1 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.TRAILING);
						panel.add(panel_1, BorderLayout.EAST);
						{
							JButton funcAddBtn = new JButton("");
							funcAddBtn.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(
										ActionEvent actionevent) {
									
									String productId = null;
									Object obj = productComboForFunction.getSelectedItem();
									if( obj instanceof TCComponentItem){
										TCComponentItem item = (TCComponentItem)obj;
										try {
											productId = item.getProperty("item_id");
										} catch (TCException e) {
											e.printStackTrace();
											return;
										}									
									}else{
										transmitBtn.setEnabled(false);
										btnClear.setEnabled(false);
										return;
									}
									
									ArrayList funcList = new ArrayList();
									DefaultTableModel model = (DefaultTableModel)functionTable.getModel();
									Vector<Vector> data = model.getDataVector();
									for( Vector row : data){
										if( !funcList.contains(row.get(1))){
											funcList.add(row.get(1));
										}
									}
									
									HashMap functionMap = new HashMap();
									Object[] objects = functionCombo.getSelectedItems();
									for( int i = 0; objects != null && i < objects.length; i++ ){
										FunctionInfo funcInfo = (FunctionInfo)objects[i];
										
										/* [SR150723-009][20150723] shcho, PE Interface에서 Function 단위 재전송이 가능하도록 기능 변경
										if( funcList.contains(funcInfo.getItemId())){
											continue;
										}
										*/
										
										Vector newRow = new Vector();
										newRow.add(productId);
										newRow.add(funcInfo.getItemId());
										newRow.add("Not Trans.");
										newRow.add(".");
										newRow.add(".");
										newRow.add(".");
										newRow.add("");
										data.add(newRow);
										
									}
									
									model.fireTableDataChanged();
									
									btnFuncEnabler();
								}
								
							});
							funcAddBtn.setIcon( registry.getImageIcon("ifpe.add"));
							panel_1.add(funcAddBtn);
						}
						{
							JButton funcRemoveBtn = new JButton("");
							funcRemoveBtn.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(
										ActionEvent actionevent) {
									int[] selectedRows = functionTable.getSelectedRows();
									ArrayList<Integer> rowList = new ArrayList();
									DefaultTableModel model = (DefaultTableModel)functionTable.getModel();
									for( int selectedRow : selectedRows ){
										int modelIndex = functionTable.convertRowIndexToModel(selectedRow);
										String stat = (String)model.getValueAt(modelIndex, 2);
										if( stat == null || stat.equals("Not Trans.") ){
											rowList.add(modelIndex);
//											System.out.println("modelIndex : " + modelIndex);
										}
									}		
									
									Integer[] modelIndexArray = new Integer[rowList.size()];
									Arrays.sort(rowList.toArray(modelIndexArray));
									for( int i = modelIndexArray.length - 1 ; i >= 0; i--){
										model.removeRow(modelIndexArray[i].intValue());
									}
									
									model.fireTableDataChanged();
									
									btnFuncEnabler();
								}
								
							});
							funcRemoveBtn.setIcon( registry.getImageIcon("ifpe.remove"));
							panel_1.add(funcRemoveBtn);
						}
					}
				}
				{
					JPanel panel = new JPanel();
					functionPanel.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BorderLayout(0, 0));
					{
						functionHeaderVector.add("Product");
						functionHeaderVector.add("Function");
						functionHeaderVector.add("Status");
						functionHeaderVector.add("Registered Date");
						functionHeaderVector.add("Compmleted Date");
						functionHeaderVector.add("LOG");
						functionHeaderVector.add("IF_ID");
						TableModel model = new DefaultTableModel(null, functionHeaderVector) {
							public Class getColumnClass(int col) {
								return String.class;
							}

							public boolean isCellEditable(int row, int col) {
								return false;
							}
					    };
					    functionTable = new JTable(model);
					    functionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
					    functionTable.setRowSorter(sorter);
					    
					    functionTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
					    ListSelectionModel rowSM = functionTable.getSelectionModel();
					    rowSM.addListSelectionListener(new ListSelectionListener(){

							@Override
							public void valueChanged(
									ListSelectionEvent listselectionevent) {
								btnFuncEnabler();
							}
					    	
					    });
					    
					    functionTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
										&& e.isControlDown()==false) {
									
									JTable table = (JTable)e.getSource();
									DefaultTableModel model = (DefaultTableModel)table.getModel();
									int rowIdx = table.getSelectedRow();
									
									if( rowIdx > -1 ){
										int modelRowIdx = table.convertRowIndexToModel(rowIdx);
										String txt = (String)model.getValueAt(modelRowIdx, 5);
										TcToPeLogDialog logDlg = new TcToPeLogDialog(TcToPeInterfaceDialog.this);
										logDlg.getLogArea().setText(txt);
										logDlg.setVisible(true);
									}
								}
								super.mouseReleased(e);
							}
							
						});
					    columnWidthInit(functionTable, functionHeaderVector, functionColumnWidth);
					    JScrollPane pane = new JScrollPane();
						pane.setViewportView(functionTable);
						pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						pane.getViewport().setBackground(Color.WHITE);						
						
						panel.add(pane);
					}
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.TRAILING);
					panel.setBackground(Color.WHITE);
					functionPanel.add(panel, BorderLayout.SOUTH);
					{
						funcClearBtn = new JButton("Clear");
						funcClearBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent actionevent) {
								
								try {
									clearFunction();
									funcClearBtn.setEnabled(false);
									
								} catch (Exception e) {
									MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("ifpe.clearError"), "INFORMATION", MessageBox.WARNING);
									return;
								}finally{
									try {
										reloadFunctionTransmissionInfo();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						});						
						funcClearBtn.setEnabled(false);
						panel.add(funcClearBtn);
					}
					{
						funcTransBtn = new JButton("Transmit to PE");
						funcTransBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent actionevent) {

								try {
									Object obj = productComboForFunction
											.getSelectedItem();
									if (obj != null && (obj.equals(SELECT_PRODUCT) )) {
										return;
									}
									
									TCComponentItem item = (TCComponentItem) obj;
//									SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(
//											WAS_URL);
//									DataSet ds = new DataSet();
//									ArrayList<HashMap> noTransInfo = null;
//									ds.put("PRODUCT_ID", item.getProperty("item_id"));
//									noTransInfo = (ArrayList<HashMap>) remoteUtil.execute(
//													"com.symc.remote.service.TcInterfaceService",
//													"getNoTransInfo", ds);
//									if (noTransInfo != null
//											&& !noTransInfo.isEmpty()) {
//										MessageBox
//												.post(TcToPeInterfaceDialog.this,
//														registry.getString("ifpe.noTransWarning"),
//														"INFORMATION",
//														MessageBox.WARNING);
//										return;
//									}

									transmitFunction(item);
								} catch (Exception e) {
									MessageBox
											.post(TcToPeInterfaceDialog.this,
													registry.getString("ifpe.transError"),
													"INFORMATION",
													MessageBox.WARNING);
									return;
								} finally {
									try {
										reloadFunctionTransmissionInfo();
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							}
						});
						
						funcTransBtn.setEnabled(false);
						panel.add(funcTransBtn);
					}
				}
			}
			{
				JPanel ecoPanel = new JPanel();
				tabbedPane.addTab("ECO", null, ecoPanel, null);
				ecoPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					ecoPanel.add(panel, BorderLayout.NORTH);
					panel.setLayout(new GridLayout(3, 0, 0, 0));
					{
						JPanel panel_1 = new JPanel();
						panel_1.setBackground(Color.WHITE);
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel.add(panel_1);
						{
							TCComponent[] projects = CustomUtil.queryComponent("Item...", new String[]{"Type"}, new String[]{"S7_PROJECT"});
							projectCombo = new JComboBox();
							projectCombo.addItem(SELECT_A_PROJECT);
							for( TCComponent project : projects){
								projectCombo.addItem(project);
							}
							panel_1.add(projectCombo);
						}
						{
							statCombo = new JComboBox();
							statCombo.addItem("All");
							statCombo.addItem("CREATION");
							statCombo.addItem("PROCESSING");
							statCombo.addItem("ERROR");
							statCombo.addItem("WAITING");
							statCombo.addItem("SUCCESS");
							statCombo.addItem("FAIL");
							panel_1.add(statCombo);
						}
						{
							ecoTF = new JTextField();
							ecoTF.addKeyListener(new KeyAdapter(){

								@Override
								public void keyReleased(KeyEvent arg0) {
									if( arg0.getKeyChar() == '\n' ){
										TcToPeInterfaceDialog.this.ecoSearchBtn.doClick();
									}
									super.keyReleased(arg0);
								}
							});
							panel_1.add(ecoTF);
							ecoTF.setColumns(10);
						}
						{
							ecoSearchBtn = new JButton("Search");
							ecoSearchBtn.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									
									if( projectCombo.getSelectedItem().equals(SELECT_A_PROJECT)){
										MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.selectProject"), "INFORMATION", MessageBox.WARNING);
										return;
									}
									
									TCComponentItem project = (TCComponentItem)projectCombo.getSelectedItem();
									String statStr = null;
									if( !statCombo.getSelectedItem().equals("All")){
										statStr = (String)statCombo.getSelectedItem();
									}
									
									String ecoStr = ecoTF.getText().trim().toUpperCase(); 
									if( ecoStr.equals("")){
										MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.inputEco"), "INFORMATION", MessageBox.WARNING);
										return;
									}
									ecoStr = ecoStr.replaceAll("\\*", "%");
									
									ArrayList<HashMap> prjList = null;
									SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
									try {
										TCComponentItem projectItem = (TCComponentItem)projectCombo.getSelectedItem();
										TCComponentItemRevision projectRevision = projectItem.getLatestItemRevision();
										String basePrjStr = projectRevision.getProperty("s7_BASE_PRJ");
										if( basePrjStr == null || basePrjStr.equals("")){
											basePrjStr = projectItem.getProperty("item_id");
										}
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
										String tmpDateStr = "";
										
										DataSet ds = new DataSet();
										
										ds.put("PROJECT_ID", basePrjStr);
										ds.put("STAT", statStr);
										ds.put("ECO_ID", ecoStr);
										ds.put("TRANS_TYPE", "E");
										Date date = registeredAfter.getDate();
										ds.put("REGISTERED_AFTER", date == null ? date:sdf.format(date));
										date = registeredBefore.getDate();
										ds.put("REGISTERED_BEFORE", date == null ? date:sdf.format(date));
										date = completedAfter.getDate();
										ds.put("COMPLETED_AFTER", date == null ? date:sdf.format(date));
										date = completedBefore.getDate();
										ds.put("COMPLETED_BEFORE", date == null ? date:sdf.format(date));
										prjList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getProductList", ds);
										Vector data = new Vector();
										if( prjList != null && !prjList.isEmpty()){
											
											SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
											for( HashMap map : prjList){
												Vector row = new Vector();
												row.add(map.get("ECO_ID"));
												row.add(map.get("PROJECT_ID"));
												row.add(map.get("PRODUCT_ID"));
												row.add(map.get("STAT"));
												Date tDate = (Date)map.get("CREATION_DATE");
												if( tDate != null){
													row.add(format.format(tDate));
												}else{
													row.add("");
												}
												
												tDate = (Date)map.get("COMPLETE_DATE");
												if( tDate != null){
													row.add(format.format(tDate));
												}else{
													row.add("");
												}
												row.add(map.get("LOG"));
												data.add(row);
											}
											
											
										}else{
											MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.notFoundResult"), "INFORMATION", MessageBox.WARNING);
										}
										
										DefaultTableModel model = (DefaultTableModel)ecoTable.getModel();
										model.setDataVector( data, ecoHeaderVector);
										columnWidthInit(ecoTable, ecoHeaderVector, ecoColumnWidth);
									} catch (Exception e) {
										e.printStackTrace();
										MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.ecoSearchError"), "INFORMATION", MessageBox.WARNING);
										return;
									}
									
								}
							});
							panel_1.add(ecoSearchBtn);
						}
					}
					{
						JPanel panel_1 = new JPanel();
						panel_1.setBackground(Color.WHITE);
						panel.add(panel_1);
						panel_1.setLayout(new GridLayout(0, 2, 0, 0));
						{
							JPanel panel_2 = new JPanel();
							panel_2.setBackground(Color.WHITE);
							panel_1.add(panel_2);
							{
								JLabel label = new JLabel("Registered After : ");
								panel_2.add(label);
							}
							{
								registeredAfter = new JDateChooser();
								registeredAfter.setPreferredSize(new Dimension(130, 20));
								panel_2.add(registeredAfter);
							}
						}
						{	
							JPanel panel_2 = new JPanel();
							panel_2.setBackground(Color.WHITE);
							panel_1.add(panel_2);
							{
								JLabel label = new JLabel("Registered Before : ");
								panel_2.add(label);
							}
							{
								registeredBefore = new JDateChooser();
								registeredBefore.setPreferredSize(new Dimension(130, 20));
								panel_2.add(registeredBefore);
							}
						}
					}
					{
						JPanel panel_1 = new JPanel();
						panel.add(panel_1);
						panel_1.setLayout(new GridLayout(0, 2, 0, 0));
						{
							JPanel panel_2 = new JPanel();
							panel_2.setBackground(Color.WHITE);
							panel_1.add(panel_2);
							{
								JLabel label = new JLabel("Completed After : ");
								panel_2.add(label);
							}
							{
								completedAfter = new JDateChooser();
								completedAfter.setPreferredSize(new Dimension(130, 20));
								panel_2.add(completedAfter);
							}
						}
						{
							JPanel panel_2 = new JPanel();
							panel_2.setBackground(Color.WHITE);
							panel_1.add(panel_2);
							{
								JLabel label = new JLabel("Completed Before : ");
								panel_2.add(label);
							}
							{
								completedBefore = new JDateChooser();
								completedBefore.setPreferredSize(new Dimension(130, 20));
								panel_2.add(completedBefore);
							}
						}
					}
				}
				{
					JPanel panel = new JPanel();
					ecoPanel.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BorderLayout(0, 0));
					{
						ecoHeaderVector.add("ECO ID");
						ecoHeaderVector.add("Project");
						ecoHeaderVector.add("Product");
						ecoHeaderVector.add("Stat");
						ecoHeaderVector.add("Registered Date");
						ecoHeaderVector.add("Compmleted Date");
						ecoHeaderVector.add("LOG");
						ecoHeaderVector.add("IF_ID");
						TableModel model = new DefaultTableModel(null, ecoHeaderVector) {
							public Class getColumnClass(int col) {
								return String.class;
							}

							public boolean isCellEditable(int row, int col) {
								return false;
							}
					    };
					    ecoTable = new JTable(model);
					    ecoTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
					    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
					    ecoTable.setRowSorter(sorter);
					    
					    ecoTable.addMouseListener(new MouseAdapter(){

							@Override
							public void mouseReleased(MouseEvent e) {
								if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
										&& e.isControlDown()==false) {
									
									JTable table = (JTable)e.getSource();
									DefaultTableModel model = (DefaultTableModel)table.getModel();
									int rowIdx = table.getSelectedRow();
									
									if( rowIdx > -1 ){
										int modelRowIdx = table.convertRowIndexToModel(rowIdx);
										String txt = (String)model.getValueAt(modelRowIdx, 6);
										TcToPeLogDialog logDlg = new TcToPeLogDialog(TcToPeInterfaceDialog.this);
										logDlg.getLogArea().setText(txt);
										logDlg.setVisible(true);
									}
								}
								super.mouseReleased(e);
							}
							
						});
					    columnWidthInit(ecoTable, ecoHeaderVector, ecoColumnWidth);
					    JScrollPane pane = new JScrollPane();
						pane.setViewportView(ecoTable);
						pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
						pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
						pane.getViewport().setBackground(Color.WHITE);
						
						panel.add(pane, BorderLayout.CENTER);
					}
				}
			}
			{
				JPanel noTransmitPanel = new JPanel();
				tabbedPane.addTab("No Transmit", null, noTransmitPanel, null);
				noTransmitPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					noTransmitPanel.add(panel, BorderLayout.NORTH);
					panel.setLayout(new GridLayout(0, 1, 0, 0));
					{
						JPanel panel_1 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel_1.setBackground(Color.WHITE);
						panel.add(panel_1);
						{
							productComboForNoTrans.addItem(SELECT_PRODUCT);
							productComboForNoTrans.addItem("All");
							for( TCComponent product : products){
								productComboForNoTrans.addItem(product);
							}
							
							productComboForNoTrans.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(ActionEvent e) {
									
									try {
										searchNoTransList();
									} catch (Exception e1) {
										e1.printStackTrace();
										MessageBox.post(TcToPeInterfaceDialog.this, e1.getMessage(), "INFORMATION", MessageBox.ERROR);
									}
								}
								
							});
							productComboForNoTrans.setPreferredSize(new Dimension(200, 21));
							panel_1.add(productComboForNoTrans);
						}
					}
				}
				{
					
					noTransHeaderVector.add("Not Trs.");
					noTransHeaderVector.add("Project");
					noTransHeaderVector.add("Base Project");
					noTransHeaderVector.add("Product");
					noTransHeaderVector.add("Creation Date");
					TableModel model = new DefaultTableModel(null, noTransHeaderVector) {
						public Class getColumnClass(int col) {
							if( col == 0){
								return Boolean.class;
							}
							return String.class;
						}

						public boolean isCellEditable(int row, int col) {
							if( col == 0 && row == 0){
								
								Object obj = productComboForNoTrans.getSelectedItem();
								if( obj != null && (obj.equals(SELECT_PRODUCT) || obj.equals("All"))){
									return false;
								}
								if( bPreviousNoTrans == false){
									return true;
								}
								Boolean bB = (Boolean)getValueAt(0, 0);
								if( !bB.booleanValue() ){
									return true;
								}else{
									return false;
								}
							}
							
							return false;
						}
				    };
				    noTransTable = new JTable(model);
				    noTransTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
				    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
				    noTransTable.setRowSorter(sorter);
				    
				    noTransTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				    ListSelectionModel rowSM = noTransTable.getSelectionModel();
				    rowSM.addListSelectionListener(new ListSelectionListener(){

						@Override
						public void valueChanged(
								ListSelectionEvent listselectionevent) {
							int selectedRow = noTransTable.getSelectedRow();
							if( selectedRow > -1 ){
							}
						}
				    	
				    });
				    int n = noTransHeaderVector.size();
				    TableColumnModel columnModel = noTransTable.getColumnModel();
					for (int i = 0; i < n; i++) {
						columnModel.getColumn(i).setPreferredWidth(noTransColumnWidth[i]);
						columnModel.getColumn(i).setWidth(noTransColumnWidth[i]);
					}
				    
				    JScrollPane pane = new JScrollPane();
					pane.setViewportView(noTransTable);
					pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					pane.getViewport().setBackground(Color.WHITE);					
					
					noTransmitPanel.add(pane, BorderLayout.CENTER);
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.RIGHT);
					noTransmitPanel.add(panel, BorderLayout.SOUTH);
					{
						JButton saveBtn = new JButton("Save");
						saveBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								try {
									
									int result = JOptionPane.showConfirmDialog(TcToPeInterfaceDialog.this, 
											registry.getString("ifpe.noTransSetInfo"), 
											"No Trans Project Save", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
									if( result == JOptionPane.YES_OPTION){
										save();
									}
								} catch (Exception e) {
									e.printStackTrace();
									MessageBox.post(TcToPeInterfaceDialog.this, e.getMessage(), "INFORMATION", MessageBox.WARNING);
								}finally{
									
								}
							}
						});
						panel.add(saveBtn);
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						TcToPeInterfaceDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public Vector setFunctionList(String productID) throws Exception{
		Vector data = new Vector();
		
		functionCombo.removeAllItems();
		functionCombo.addItem("");
		
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		ArrayList<HashMap> noTransInfo = null;
		ds.put("PRODUCT_ID", productID);
		
		ArrayList<HashMap> functionList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getFunctionList", ds);
		if( functionList != null ){
			
			for( HashMap map : functionList ){
//				data.add(map.get("ITEM_ID") + "-" + map.get("ITEM_NAME"));
				data.add(new FunctionInfo((String)map.get("ITEM_ID"), map));
			}
			
			HashSet set = new HashSet();
			set.addAll(data);
			functionCombo.resetObjs(set, false);
			
		}
		
		return data;
	}

	/**
	 * 컬럼 사이즈 초기화
	 */
	public void columnWidthInit(JTable table, Vector headerVector, int[] columnWidth){
		TableColumnModel columnModel = table.getColumnModel();
		
		int n = headerVector.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
			columnModel.getColumn(i).setWidth(columnWidth[i]);
		}
		
		int logIdx = columnModel.getColumnIndex("LOG");
		if( logIdx > -1){
			columnModel.removeColumn(columnModel.getColumn(logIdx));
		}
		int ifIdIdx = columnModel.getColumnIndex("IF_ID");
		if( ifIdIdx > -1){
			columnModel.removeColumn(columnModel.getColumn(ifIdIdx));
		}
	}
	
	private void transmitFunction(TCComponentItem productItem) throws Exception{
		
		if( productItem == null) return;
		
		DefaultTableModel model = (DefaultTableModel)functionTable.getModel();
		int[] rowIdxes = functionTable.getSelectedRows();
//		TCComponentItem function = null;
		TCComponentItemRevision productRev = null;
		String stat = "";
		
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		
		for( int rowIdx : rowIdxes){
			int modelRowIdx = functionTable.convertRowIndexToModel(rowIdx);
			String functionID = (String)model.getValueAt(modelRowIdx, 1);
			stat = (String)model.getValueAt(modelRowIdx, 2);
			if( stat == null || stat.equals("Not Trans.") || stat.equals("") || stat.equals(IF_STAT_CLEARED)){
				
				productRev = productItem.getLatestItemRevision();
				String projectStr = productRev.getProperty("s7_PROJECT_CODE");
				
				ds.clear();
				
				ds.put("ECO_ID", null);
				ds.put("PRODUCT_ID", productRev.getProperty("item_id"));
				ds.put("PRODUCT_REV_ID", productRev.getProperty("item_revision_id"));
				ds.put("PROJECT_ID", projectStr);
				ds.put("TRANS_TYPE", "F");
				ds.put("STAT", IF_STAT_CREATION);
				ds.put("FUNCTION_ID", functionID);
				ds.put("FUNCTION_REV_ID", "000");
				
				if( stat.equals(IF_STAT_CLEARED)){
					ds.put("RE_TRANS", "Y");
					String ifID = (String)model.getValueAt(modelRowIdx, 6);
					ds.put("IF_ID", ifID);					
					remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "updateProduct", ds);
				}else{
					remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "insertProduct", ds);
				}
			}
			
		}
		
		
		transmitBtn.setEnabled(false);
	}	
	
	private void transmit() throws Exception{
		
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		
		String projectStr = productRevision.getProperty("s7_PROJECT_CODE");
		ds.put("ECO_ID", null);
		ds.put("PRODUCT_ID", productRevision.getProperty("item_id"));
		ds.put("PRODUCT_REV_ID", productRevision.getProperty("item_revision_id"));
		ds.put("PROJECT_ID", projectStr);
		ds.put("TRANS_TYPE", "P");
		ds.put("STAT", IF_STAT_CREATION);
		
		int selectedRow = productTable.getSelectedRow();
		if( selectedRow > -1 ){
			int modelIndex = productTable.convertRowIndexToModel(selectedRow);
			DefaultTableModel model = (DefaultTableModel)productTable.getModel();
			String stat = (String)model.getValueAt(modelIndex, 2);
			if( stat.equals(IF_STAT_CLEARED)){
				ds.put("RE_TRANS", "Y");
				String ifID = (String)model.getValueAt(modelIndex, 6);
				ds.put("IF_ID", ifID);
				remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "updateProduct", ds);
			}else{
				remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "insertProduct", ds);
			}
			
		}
		
		
		transmitBtn.setEnabled(false);
	}
	
	private void clearFunction() throws Exception{
		
		DefaultTableModel model = (DefaultTableModel)functionTable.getModel();
		int[] rowIdxes = functionTable.getSelectedRows();
		TCComponentItem product = null;
		TCComponentItemRevision productRev = null;
		String functionID = null, ifID = "";
		
		Object obj = this.productComboForFunction.getSelectedItem();
		if( obj instanceof TCComponent){
			product = (TCComponentItem)obj;
			productRev = product.getLatestItemRevision();
		}else{
			return;
		}
		
		if( rowIdxes == null || rowIdxes.length == 0){
			return;
		}
		
		String projectStr = productRev.getProperty("s7_PROJECT_CODE");
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		
		for( int rowIdx : rowIdxes){
			
			DataSet ds = new DataSet();
			
			int modelRowIdx = functionTable.convertRowIndexToModel(rowIdx);
			functionID = (String)model.getValueAt(modelRowIdx, 0);
			ifID = (String)model.getValueAt(modelRowIdx, 6);
			
			ds.put("ECO_ID", null);
			ds.put("PRODUCT_ID", productRev.getProperty("item_id"));
			ds.put("PRODUCT_REV_ID", productRev.getProperty("item_revision_id"));
			ds.put("PROJECT_ID", projectStr);
			ds.put("TRANS_TYPE", "F");
			ds.put("STAT", IF_STAT_CLEARED);
			ds.put("FUNCTION_ID", functionID);
			ds.put("FUNCTION_REV_ID", "000");
			ds.put("IF_ID", ifID);
			
			remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "updateProduct", ds);
		}
	}	
	
	private void clear() throws Exception{
		
		DefaultTableModel model = (DefaultTableModel)productTable.getModel();
		int rowIdx = productTable.getSelectedRow();
		String ifId = "";
		
		if( rowIdx > -1 ){
			int modelRowIdx = productTable.convertRowIndexToModel(rowIdx);
			try{
				ifId = (String)model.getValueAt(modelRowIdx, 6);
			}catch(Exception e){
				ifId = null;
			}
		}else{
			return;
		}
		
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		
		String projectStr = productRevision.getProperty("s7_PROJECT_CODE");
		ds.put("ECO_ID", null);
		ds.put("PRODUCT_ID", productRevision.getProperty("item_id"));
		ds.put("PRODUCT_REV_ID", productRevision.getProperty("item_revision_id"));
		ds.put("PROJECT_ID", projectStr);
		ds.put("TRANS_TYPE", "P");
		ds.put("IF_ID", ifId);
		ds.put("STAT", IF_STAT_CLEARED);
		
		remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "updateProduct", ds);
	}
	
	/**
	 * 선택한 Product에 따라 전송 가능한 한지, Clear가능한지를 체크한다.
	 */
	private void btnEnabler(){
		int selectedRow = productTable.getSelectedRow();
		if( selectedRow > -1 ){
			int modelIndex = productTable.convertRowIndexToModel(selectedRow);
			DefaultTableModel model = (DefaultTableModel)productTable.getModel();
			String stat = (String)model.getValueAt(modelIndex, 2);
			if( stat == null || stat.equals("Not Trans.") || stat.equals("") || stat.equals(IF_STAT_CLEARED)){
				transmitBtn.setEnabled(true);
				btnClear.setEnabled(false);
			}else if( stat != null && stat.equals("ERROR")){
				transmitBtn.setEnabled(false);
				btnClear.setEnabled(true);
			}else{
				transmitBtn.setEnabled(false);
				btnClear.setEnabled(false);
			}
		}
	}
	
	private void btnFuncEnabler(){
		int[] selectedRows = functionTable.getSelectedRows();
		if( selectedRows == null || selectedRows.length < 1) {
			funcTransBtn.setEnabled(false);
			funcClearBtn.setEnabled(false);
			return;
		}
		
		funcTransBtn.setEnabled(true);
		funcClearBtn.setEnabled(true);
		for( int selectedRow : selectedRows){
			int modelIndex = functionTable.convertRowIndexToModel(selectedRow);
			DefaultTableModel model = (DefaultTableModel)functionTable.getModel();
			String stat = (String)model.getValueAt(modelIndex, 2);
			if( stat == null || stat.equals("Not Trans.") || stat.equals("") || stat.equals(IF_STAT_CLEARED)){
				funcClearBtn.setEnabled(false);
			}else if( stat != null && stat.equals("ERROR")){
				funcTransBtn.setEnabled(false);
			}else if( stat != null && stat.equals("CREATION")){
				funcClearBtn.setEnabled(false);
				funcTransBtn.setEnabled(false);
			}else{
				funcClearBtn.setEnabled(false);
				funcTransBtn.setEnabled(false);
			}
		}
	}
	
	private void reloadFunctionTransmissionInfo() throws Exception{
		funcTransBtn.setEnabled(false);
		
		Object obj = productComboForFunction.getSelectedItem();
		
		if( obj != null && obj.equals(SELECT_PRODUCT)){
			if( functionTable == null){
				return;
			}
			DefaultTableModel model = (DefaultTableModel)functionTable.getModel();
			Vector data = new Vector();
			model.setDataVector( data, functionHeaderVector);
			columnWidthInit(functionTable, functionHeaderVector, functionColumnWidth);
			return;
		}
		
		TCComponentItem item = (TCComponentItem)obj;
		String itemId = item.getProperty("item_id");
		
		DefaultTableModel model = (DefaultTableModel)functionTable.getModel();
		Vector data = new Vector();
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		
		ds.put("PRODUCT_ID", itemId);
		ArrayList<HashMap> functionList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getFunctionListToTrans", ds);
		if( functionList != null ){
			for( HashMap map : functionList ){
				Vector row = new Vector();
				row.add(map.get("PRODUCT_ID"));
				row.add(map.get("FUNCTION_ID"));
				row.add(map.get("STAT"));
				Date tmpDate = (Date)map.get("CREATION_DATE");
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String tmpDateStr = "";
				if( tmpDate == null ){
					row.add(".");
				}else{
					tmpDateStr = sdf.format(tmpDate);
					row.add(tmpDateStr);
				}				
				
				tmpDate = (Date)map.get("COMPLETE_DATE");
				if( tmpDate == null ){
					row.add(".");
				}else{
					tmpDateStr = sdf.format(tmpDate);
					row.add(tmpDateStr);
				}
				row.add(map.get("LOG"));
				row.add(map.get("IF_ID"));
				data.add(row);
			}
		}
		model.setDataVector( data, functionHeaderVector);
		columnWidthInit(functionTable, functionHeaderVector, functionColumnWidth);
		return;
	}
	
	private void reloadProductTransmissionInfo() throws Exception{
		transmitBtn.setEnabled(false);
		
		//Product의 Project를 가져오고,
		//TC내의 Project중에서 Base Project가 Product에서 가져온 Project와 동일한 Project를 목록을 가져온다.
		Object obj = productComboForProduct.getSelectedItem();
		
		if( obj != null && obj.equals(SELECT_PRODUCT)){
			if( productTable == null){
				return;
			}
			DefaultTableModel model = (DefaultTableModel)productTable.getModel();
			Vector data = new Vector();
			model.setDataVector( data, productHeaderVector);
			columnWidthInit(productTable, productHeaderVector, projectColumnWidth);
			return;
		}
		
		if( obj != null && obj.equals("All")){
			
			DefaultTableModel model = (DefaultTableModel)productTable.getModel();
			Vector data = new Vector();
			SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
			DataSet ds = new DataSet();
			
			ds.put("TRANS_TYPE", "P");
			ArrayList<HashMap> productList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getProductList", ds);
			if( productList != null ){
				for( HashMap map : productList ){
					Vector row = new Vector();
					row.add(map.get("PROJECT_ID"));
					String tType = (String)map.get("TRANS_TYPE");
					if( tType == null) continue;
					
					row.add(tType);
					row.add(map.get("STAT"));
					
					Date tmpDate = (Date)map.get("CREATION_DATE");
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					String tmpDateStr = sdf.format(tmpDate);
					row.add(tmpDateStr);
					
					tmpDate = (Date)map.get("COMPLETE_DATE");
					if( tmpDate == null ){
						row.add(".");
					}else{
						tmpDateStr = sdf.format(tmpDate);
						row.add(tmpDateStr);
					}
					row.add(map.get("LOG"));
					row.add(map.get("IF_ID"));
					data.add(row);
				}
			}
			
			model.setDataVector( data, productHeaderVector);
			columnWidthInit(productTable, productHeaderVector, projectColumnWidth);
			return;
		}
		
		if( !(obj instanceof TCComponent)){
			if( productTable == null) return;
			DefaultTableModel model = (DefaultTableModel)productTable.getModel();
			Vector data = new Vector();
			model.setDataVector(data, productHeaderVector);
			columnWidthInit(productTable, productHeaderVector, projectColumnWidth);
			return;
		}
		TCComponentItem product = (TCComponentItem)productComboForProduct.getSelectedItem();
		String basePrjStr = "";
		
		//현재 선택된 Product와 연관성이 있는 Project들을 모아둘 List
		ArrayList<TCComponent> projectList = new ArrayList();
		
		productRevision = product.getLatestItemRevision();
		String[] pNames = productRevision.getPropertyNames();
		basePrjStr = productRevision.getProperty("s7_PROJECT_CODE");
		
		if( basePrjStr == null || basePrjStr.trim().equals("")){
			MessageBox.post(TcToPeInterfaceDialog.this, registry.getString("ifpe.notFoundProject"), "INFORMATION", MessageBox.WARNING);
			return;
		}
		TCComponent[] comps = CustomUtil.queryComponent("__SYMC_S7_PROJECTRevision", new String[] { "item_id"}, new String[] { basePrjStr });
		if( comps != null ){
			for( TCComponent com : comps){
				if( !projectList.contains(com))
					projectList.add(com);
			}
		}
		//s7_PROJECT_CODE, s7_BASE_PRJ
		
		//ProjectList 에 포함된 Project는 모두 
		ArrayList<HashMap> productList = null;
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		
		ds.put("PRODUCT_ID", product.getProperty("item_id"));
		ds.put("PRODUCT_REV_ID", productRevision.getProperty("item_revision_id"));
		ds.put("TRANS_TYPE", "P");
		productList = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getProductList", ds);
		
		DefaultTableModel model = (DefaultTableModel)productTable.getModel();
		Vector data = new Vector();
		if( productList == null || productList.isEmpty()){
			for( TCComponent com : projectList ){
				Vector row = new Vector();
				row.add(basePrjStr);
				row.add(".");
				row.add("Not Trans.");
				row.add(".");
				row.add(".");
				data.add(row);
			}
		}else{
			for( HashMap map : productList ){
				Vector row = new Vector();
				row.add(map.get("PROJECT_ID"));
				String tType = (String)map.get("TRANS_TYPE");
				if( tType == null) continue;
				
				row.add(tType);
				row.add(map.get("STAT"));
				
				Date tmpDate = (Date)map.get("CREATION_DATE");
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				String tmpDateStr = sdf.format(tmpDate);
				row.add(tmpDateStr);
				
				tmpDate = (Date)map.get("COMPLETE_DATE");
				if( tmpDate == null ){
					row.add(".");
				}else{
					tmpDateStr = sdf.format(tmpDate);
					row.add(tmpDateStr);
				}
				row.add(map.get("LOG"));
				row.add(map.get("IF_ID"));
				data.add(row);
			}
		}
//		model.fireTableDataChanged();
		model.setDataVector( data, productHeaderVector);
		columnWidthInit(productTable, productHeaderVector, projectColumnWidth);
		
	}
	
	private void save() throws Exception{
		
		TCComponentItem item = (TCComponentItem)productComboForNoTrans.getSelectedItem();
		TCComponentItemRevision revision = item.getLatestItemRevision();
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		ds.put("PRODUCT_ID", revision.getProperty("item_id"));
		ds.put("PRODUCT_REV_ID", revision.getProperty("item_revision_id"));
		
		ArrayList<HashMap> noTransInfo = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getNoTransInfo", ds);
		if( noTransInfo != null && !noTransInfo.isEmpty()){
			return;
		}
		
		String projectStr = revision.getProperty("s7_PROJECT_CODE");
		ds.put("PROJECT_ID", projectStr);
		DefaultTableModel model = (DefaultTableModel)noTransTable.getModel();
		Boolean bChk = (Boolean)model.getValueAt(0, 0);
		
		if( bChk.booleanValue() && !bPreviousNoTrans){
			remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "insertNoTransProduct", ds);
			bPreviousNoTrans = true;
		}else{
			throw new Exception("Can't save NoTransInfo.");
		}
		
	}
	
	private void searchNoTransList() throws Exception{
		Vector data = new Vector();
		Object obj = productComboForNoTrans.getSelectedItem();
		if( obj != null && obj.equals(SELECT_PRODUCT)){
			return;
		}
		
		SYMCRemoteUtil remoteUtil = new SYMCRemoteUtil(WAS_URL);
		DataSet ds = new DataSet();
		ArrayList<HashMap> noTransInfo = null;
		
		if( obj != null && obj.equals("All")){
			ds.put("All", "All");
			noTransInfo = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getNoTransInfo", ds);
			
			for( int i = 0; i < noTransInfo.size(); i++){
				HashMap info = noTransInfo.get(i);
				Vector row = new Vector();
				row.add(new Boolean(true));
				row.add(info.get("PROJECT_ID"));
				row.add(info.get("BASE_PROJECT_ID"));
				row.add(info.get("PRODUCT_ID"));
				row.add(info.get("CREATION_DATE"));
				data.add(row);
			}
			bPreviousNoTrans = false;
			
		}else{
			
			TCComponentItem product = (TCComponentItem)obj;
			String basePrjStr = "";
			
			//현재 선택된 Product와 연관성이 있는 Project들을 모아둘 List
			ArrayList<TCComponent> projectList = new ArrayList();
			TCComponentItemRevision productRevision = null;
			
			productRevision = product.getLatestItemRevision();
			
			ArrayList<HashMap> prjList = null;
			
			
			ds.put("PRODUCT_ID", product.getProperty("item_id"));
			
			noTransInfo = (ArrayList<HashMap>)remoteUtil.execute("com.symc.remote.service.TcInterfaceService", "getNoTransInfo", ds);
			if( noTransInfo == null || noTransInfo.isEmpty()){
				
			}
			String[] pNames = productRevision.getPropertyNames();
			basePrjStr = productRevision.getProperty("s7_PROJECT_CODE");
			TCComponent[] comps = CustomUtil.queryComponent("__SYMC_S7_PROJECTRevision", new String[] { "item_id"}, new String[] { basePrjStr });
			if( comps != null ){
				
				for( TCComponent com : comps){
					if( !projectList.contains(com))
						projectList.add(com);
				}
				
			}
			
			comps = CustomUtil.queryComponent("__SYMC_S7_PROJECTRevision", new String[] { "s7_BASE_PRJ"}, new String[] { basePrjStr });
			if( comps != null ){
				for( TCComponent com : comps){
					if( !projectList.contains(com))
						projectList.add(com);
				}
			}		
			
			for( int i = 0; i < projectList.size(); i++){
				TCComponent project = projectList.get(i);
				Vector row = new Vector();
				TCComponentItemRevision revision = (TCComponentItemRevision)project;
				if( noTransInfo != null && !noTransInfo.isEmpty()){
					if( i == 0 ){
						row.add(new Boolean(true));
					}else{
						row.add(new Boolean(false));
					}
				}else{
					row.add(new Boolean(false));
				}
				
				row.add(revision.getProperty("item_id"));
				row.add(revision.getProperty("s7_BASE_PRJ"));
				row.add(product.toDisplayString());
				if( noTransInfo != null && !noTransInfo.isEmpty() && i == 0){
					row.add(noTransInfo.get(0).get("CREATION_DATE"));
				}else{
					row.add("-");
				}
				data.add(row);
			}
			
			if( noTransInfo != null && !noTransInfo.isEmpty())
				bPreviousNoTrans = true; 
			else
				bPreviousNoTrans = false;
		}
		
		DefaultTableModel model = (DefaultTableModel)noTransTable.getModel();
		model.setDataVector( data, noTransHeaderVector);
		
		int n = noTransHeaderVector.size();
	    TableColumnModel columnModel = noTransTable.getColumnModel();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(noTransColumnWidth[i]);
			columnModel.getColumn(i).setWidth(noTransColumnWidth[i]);
		}
		
	}
}
