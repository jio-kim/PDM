package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

import jxl.Cell;
import jxl.CellView;
import jxl.Workbook;
import jxl.format.Alignment;
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

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.ui.MultiLineToolTip;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.model.MasterListDataMapper;
import com.symc.plm.rac.prebom.masterlist.model.RevisionIDComboBoxObject;
import com.symc.plm.rac.prebom.masterlist.model.SimpleTcObject;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.masterlist.operation.BOMLoadWithDateOperation;
import com.symc.plm.rac.prebom.masterlist.operation.BOMUpdateOperation;
import com.symc.plm.rac.prebom.masterlist.operation.ExcelDataImportOreration;
import com.symc.plm.rac.prebom.masterlist.operation.ExportMasterListTemplateOperation;
import com.symc.plm.rac.prebom.masterlist.util.WebUtil;
import com.symc.plm.rac.prebom.masterlist.view.CheckComboBox;
import com.symc.plm.rac.prebom.masterlist.view.ColumnGroup;
import com.symc.plm.rac.prebom.masterlist.view.GroupableTableHeader;
import com.symc.plm.rac.prebom.masterlist.view.MasterListReq;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel.FilterColumn;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.toedter.calendar.JDateChooser;

/**
 * [20161028][ymjang] 소스 정리
 * [20170314][ymjang] 암호화 문서 경고 문구 삽입
 * [SR170824-025][LJG] FMP리비전이 001일 경우만 Excel 업로드 가능
 */
@SuppressWarnings({ "unchecked", "rawtypes", "serial" })
public class MasterListDlg extends JFrame implements MasterListReq {

	private final JPanel contentPanel = new JPanel();
	private JTabbedPane tabbedPane = null;
	
	private JComboBox cbCCN = null;
	private JLabel lbProject = null;
	private JLabel lbOspec = null;
	
	private String[] contents = null;
	
	private JComboBox cbPreFMPRevision = null;
	private JDateChooser dateChooser = null;
	private JCheckBox chckbxShowDeletedLine = null;
	private JSplitPane splitPane = null;
	
	private JTextField tfFilePath;
	private MasterListTablePanel masterListTablePanel = null;
	private CheckComboBox columnCheckCombo = null;
	private TCComponentItemRevision preProductRevision = null, fmpRevision = null;
	private OSpec ospec = null;
	@SuppressWarnings("unused")
	private Vector<Vector> data = null, releaseData = null;
	private SimpleTcObject ccnObj = null;
	private boolean isEditable = false;
	private TCSession session = null;
	private MasterListDataMapper releaseDataMapper = null, dataMapper = null;
	private TCComponentBOMLine workingFmpTopLine = null;
	private ArrayList<VariantOption> enableOptionSet = null;
	private OptionManager optionManager = null;
	
	//BOM Line Key(SYS_GUID)를 Row와 매핑.
	private HashMap<String, Vector> keyRowMapper = null, releaseKeyRowMapper;
	private HashMap<String, StoredOptionSet> storedOptionSetMap = null;
	
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js"); 
	private ArrayList<String> essentialNames = null;
	
	private String currentUserId = null;
	private String currentUserName = null;
	private String currentUserGroup = null;
	private String currentUserPa6Group = null;
	private boolean isCordinator = false;
	private String fmpId = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MasterListDlg dialog = new MasterListDlg(null, null, null, null, false, null, null, null, null, null, null, null, null, null, null, null, false);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Create the dialog.
	 * 
	 * @throws Exception
	 */
	public MasterListDlg(String currentUserId, String currentUserName, String currentUserGroup, String currentUserPa6Group,
			boolean isCordinator, String[] contents, ArrayList<String> essentialNames, HashMap<String, StoredOptionSet> storedOptionSetMap, 
			OptionManager optionManager, TCComponentItemRevision preProductRevision, 
			TCComponentBOMLine workingFmpTopLine, 
			ArrayList<VariantOption> enableOptionSet, OSpec ospec, MasterListDataMapper releaseDataMapper, 
			MasterListDataMapper dataMapper, SimpleTcObject ccnObj, boolean isEditable) throws Exception {
		
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.currentUserId = currentUserId;
		this.currentUserName = currentUserName;
		this.currentUserGroup = currentUserGroup;
		this.currentUserPa6Group = currentUserPa6Group;
		this.isCordinator = isCordinator;
		this.contents = contents;
		this.essentialNames = essentialNames;
		this.storedOptionSetMap = storedOptionSetMap;
		this.optionManager = optionManager;
		this.preProductRevision = preProductRevision;
		this.workingFmpTopLine = workingFmpTopLine;
		this.fmpRevision = workingFmpTopLine.getItemRevision();
		this.fmpId = fmpRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
		this.enableOptionSet = enableOptionSet;
		this.ospec = ospec;
		keyRowMapper = new HashMap();
		releaseKeyRowMapper = new HashMap();
		this.dataMapper = dataMapper;
		this.releaseDataMapper = releaseDataMapper;
		
		this.data = dataMapper.createMasterListData(keyRowMapper, storedOptionSetMap, engine);
		
		if( releaseDataMapper != null ){
			this.releaseData = releaseDataMapper.createMasterListData(releaseKeyRowMapper, storedOptionSetMap, engine);
		}
		this.ccnObj = ccnObj;
		this.isEditable = isEditable;
		session = CustomUtil.getTCSession();
		
		init();
	}
	
	public ArrayList<String> getWorkingKeyList() {
		return dataMapper.getKeyList();
	}
	
	@Override
	public String getCurrentUserId() {
		return currentUserId;
	}

	public String getCurrentUserName() {
		return currentUserName;
	}

	public String getCurrentUserGroup() {
		return currentUserGroup;
	}

	@Override
	public String getCurrentUserPa6Group() {
		return currentUserPa6Group;
	}

	public boolean isCordinator() {
		return isCordinator;
	}

	public ArrayList<TCComponentBOMLine> getBOMLines(String systemRowKey) {
		ArrayList<TCComponentBOMLine> list = new ArrayList();
		HashMap<String, HashMap<String, ArrayList<TCComponentBOMLine>>> bomLineMap = dataMapper.getBomlineMap();
		HashMap<String, ArrayList<TCComponentBOMLine>> map = bomLineMap.get(systemRowKey);
		if( map != null){
			String[] keys = map.keySet().toArray(new String[map.size()]);
			for( String key : keys){
				ArrayList<TCComponentBOMLine> bomlines = map.get(key);
				for( TCComponentBOMLine line : bomlines){
					try {
						if( line != null && line.isValidUid() && !list.contains(line)){
							list.add(line);
						}
					} catch (TCException e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}
		return list;
	}

	public String[] getContents() {
		return contents;
	}

	public OptionManager getOptionManager() {
		return optionManager;
	}

	public TCComponentBOMLine getWorkingFmpTopLine() {
		return workingFmpTopLine;
	}

	public void setWorkingFmpTopLine(TCComponentBOMLine workingFmpTopLine) {
		this.workingFmpTopLine = workingFmpTopLine;
	}

	private void init() throws Exception{
		setTitle("Master List" + (isEditable ? "(Edit)":"(View)"));
		setIconImage( Toolkit.getDefaultToolkit().getImage(ValidationDlg.class.getResource("/icons/tcdesktop_16.png")) );
		setBounds(100, 100, 1240, 700);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		String[] projectArray = CustomUtil.getLOVDisplayValues(session, "S7_PROJECT_CODE");
		String[] projects = new String[projectArray.length + 1];
		projects[0] = "PROJECT";
		System.arraycopy(projectArray, 0, projects, 1, projectArray.length);
		
		String[] systemCodeArray = CustomUtil.getLOVDisplayValues(session, "S7_SYSTEM_CODE");
		String[] systemCodes = new String[systemCodeArray.length + 1];
		systemCodes[0] = "SYSTEM CODE";
		System.arraycopy(systemCodeArray, 0, systemCodes, 1, systemCodeArray.length);
		
		ArrayList<SimpleTcObject> ospecList = BomUtil.getOSpecList(preProductRevision);
		if( ospecList.isEmpty()){
			throw new Exception("Could not find O/Spec info.");
		}
		
		masterListTablePanel = new MasterListTablePanel(this, ospec, data);
		{
//			UIManager.put("TabbedPane.selected", UIManager.getColor("control"));  
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			{
				JPanel workspacePanel = new JPanel();
				tabbedPane.addTab("Workspace", null, workspacePanel, null);
				workspacePanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
					flowLayout_1.setAlignment(FlowLayout.LEADING);
					workspacePanel.add(panel, BorderLayout.CENTER);
					{
						JLabel lblCcn = new JLabel("CCN:");
						panel.add(lblCcn);
					}
					{
						cbCCN = new JComboBox();
						cbCCN.setFont(new Font("굴림", Font.BOLD, 12));
						ArrayList<HashMap> ccns = WebUtil.getWorkingCCN();
						cbCCN.addItem("Select a CCN");
						if( ccns != null){
							for( HashMap map : ccns){
								String ccnNo = (String)map.get("CCN_NO");
								String puid = (String)map.get("PUID");
								SimpleTcObject ccn = new SimpleTcObject(ccnNo, puid);
								cbCCN.addItem(ccn);
							}
							cbCCN.addItemListener(new ItemListener() {
								
								@Override
								public void itemStateChanged(ItemEvent arg0) {
									Object obj = cbCCN.getSelectedItem();
									if(obj instanceof SimpleTcObject){
										SimpleTcObject simpleTcObj = (SimpleTcObject)obj;
										try {
											TCComponentItemRevision ccnRev = (TCComponentItemRevision)session.stringToComponent(simpleTcObj.getPuid());
											lbOspec.setText(ccnRev.getProperty(PropertyConstant.ATTR_NAME_OSPECNO));
										} catch (TCException e) {
											MessageBox.post(MasterListDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
											return;
										}
									}else{
										lbOspec.setText("");
									}
								}
							});
							
						}
						
						panel.add(cbCCN);
					}
					{
						JLabel lblProjectA = new JLabel("Project:");
						panel.add(lblProjectA);
					}
					{
						lbProject = new JLabel(preProductRevision.getProperty("s7_PROJECT_CODE"));
						lbProject.setFont(new Font("굴림", Font.BOLD, 12));
						panel.add(lbProject);
					}
					{
						JLabel lblNewLabel_1 = new JLabel("   FMP:");
						panel.add(lblNewLabel_1);
					}
					{
						JLabel lbFmp = new JLabel(fmpRevision.getItem().getProperty("item_id"));
						lbFmp.setFont(new Font("굴림", Font.BOLD, 12));
						panel.add(lbFmp);
					}
					{
						JLabel lblOspec = new JLabel("   O/Spec:");
						panel.add(lblOspec);
					}
					{
						lbOspec = new JLabel("N/A");
						lbOspec.setFont(new Font("굴림", Font.BOLD, 12));
						if( ccnObj != null){
							cbCCN.setSelectedItem(ccnObj);
						}else{
							TCProperty prop = fmpRevision.getTCProperty(PropertyConstant.ATTR_NAME_CCNNO);
							TCComponentItemRevision ccnRev = (TCComponentItemRevision)prop.getReferenceValue();
							if( ccnRev == null){
								throw new TCException("Could not found CCN No.");
							}
							if( !CustomUtil.isReleased(fmpRevision)){
								ccnObj = new SimpleTcObject(ccnRev.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID), ccnRev.getUid());
								cbCCN.setSelectedItem(ccnObj);
							}else{
								lbOspec.setText(ccnRev.getProperty(PropertyConstant.ATTR_NAME_OSPECNO));
							}
						}
						
						panel.add(lbOspec);
					}
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout_1 = (FlowLayout) panel.getLayout();
					flowLayout_1.setAlignment(FlowLayout.TRAILING);
					workspacePanel.add(panel, BorderLayout.EAST);
					{
						JButton btnNewButton_2 = new JButton("사양적용계산");
						if( isEditable ){
							btnNewButton_2.setEnabled(true);
						}else{
							btnNewButton_2.setEnabled(false);
						}
						panel.add(btnNewButton_2);
						btnNewButton_2.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								
								applySpec();
								
							}
						});
					}
					
				}
			}
			 
			{
				JPanel loadInTcPanel = new JPanel();
				tabbedPane.addTab("Load in TC",loadInTcPanel);
				loadInTcPanel.setLayout(new BorderLayout(0, 0));
				JPanel panel_2 = new JPanel();
				
				loadInTcPanel.add(panel_2, BorderLayout.CENTER);
				FlowLayout fl_panel_2 = (FlowLayout) panel_2.getLayout();
				fl_panel_2.setAlignment(FlowLayout.LEADING);
				{
					// [][20160426][jclee] BOM Load시 Item Revision ID를 선택할 수 있는 기능 추가
					cbPreFMPRevision = new JComboBox();
					cbPreFMPRevision.setFont(new Font("굴림", Font.BOLD, 12));
					cbPreFMPRevision.addItem("Select a Revision ID");
					
					TCComponent[] caRevision = fmpRevision.getItem().getReferenceListProperty("revision_list");
					for (int inx = 0; inx < caRevision.length; inx++) {
						TCComponentItemRevision irFMP = (TCComponentItemRevision) caRevision[inx]; 
						RevisionIDComboBoxObject rco = new RevisionIDComboBoxObject(irFMP);
						
						if (rco.getItemRevisionID() == null || rco.getItemRevisionID().equals("") || rco.getItemRevisionID().length() == 0 || rco.getItemRevisionID().equals("000")) {
							continue;
						}
						
						cbPreFMPRevision.addItem(rco);
					}
					
					cbPreFMPRevision.addItemListener(new ItemListener() {
						@Override
						public void itemStateChanged(ItemEvent arg0) {
							if (cbPreFMPRevision.getSelectedIndex() == 0) {
								dateChooser.setDate(new Date());
							} else {
								Object oSelectedItem = cbPreFMPRevision.getSelectedItem();
								
								if (oSelectedItem instanceof RevisionIDComboBoxObject) {
									RevisionIDComboBoxObject rcoSelected = (RevisionIDComboBoxObject) oSelectedItem;
									Date dReleased = rcoSelected.getReleasedDate();
									
									if (dReleased != null) {
										dateChooser.setDate(dReleased);
									} else {
										dateChooser.setDate(new Date());
									}
								}
							}
						}
					});
					panel_2.add(cbPreFMPRevision);
					
					dateChooser = new JDateChooser(null, "yyyy-MM-dd", false, null);
					panel_2.add(dateChooser);
				}
				{
					JButton btnLoad = new JButton("Search");
					btnLoad.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							
							searchMasterListView();
						}
					});
					panel_2.add(btnLoad);
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.TRAILING);
					loadInTcPanel.add(panel, BorderLayout.EAST);
				}
			}
			{
				JPanel importPanel = new JPanel();
				tabbedPane.addTab("Excel Import", importPanel);
				importPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					importPanel.add(panel, BorderLayout.CENTER);
					{
						tfFilePath = new JTextField();
						panel.add(tfFilePath);
						tfFilePath.setColumns(30);
						
						JLabel lblAttachFileMsg = new JLabel();
						lblAttachFileMsg.setFont(new Font("맑은 고딕", Font.BOLD, 13));
						lblAttachFileMsg.setForeground(Color.RED);
						lblAttachFileMsg.setText("※ 암호화된 문서는 반드시 암호를 해제하신 후, 등록하셔야 합니다. ※");
						panel.add(lblAttachFileMsg);
						
					}
					{
						JButton btnSearch = new JButton("Search");
						btnSearch.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								//[SR170824-025][LJG] FMP리비전이 001일 경우만 Excel 업로드 가능
								try {
									if(!fmpRevision.getStringProperty("item_revision_id").equalsIgnoreCase("001")){
										MessageBox.post(MasterListDlg.this, "FMP Revision is Not 001", "Information", MessageBox.WARNING);
										return;
									}
								} catch (TCException e1) {
									e1.printStackTrace();
								}
								JFileChooser fileChooser = new JFileChooser();
								fileChooser.setFileFilter(new FileFilter(){

									@Override
									public boolean accept(File f) {
										if (f.isDirectory()) {
									        return true;
									    }
										
										if( f.isFile()){
											return f.getName().endsWith("xls") || f.getName().endsWith("xlsx");
										}
										return false;
									}

									@Override
									public String getDescription() {
										return "*.xls;*.xlsx";
									}

								});
								int result = fileChooser.showOpenDialog(MasterListDlg.this);
								if( result == JFileChooser.APPROVE_OPTION){
									File selectedFile = fileChooser.getSelectedFile();
									MasterListDlg.this.tfFilePath.setText( selectedFile.getAbsolutePath() );
								}
							}
						});
						panel.add(btnSearch);

						JButton btnImport = new JButton("Import");
						panel.add(btnImport);
						btnImport.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								//[SR170824-025][LJG] FMP리비전이 001일 경우만 Excel 업로드 가능
								try {
									if(!fmpRevision.getStringProperty("item_revision_id").equalsIgnoreCase("001")){
										MessageBox.post(MasterListDlg.this, "FMP Revision is Not 001", "Information", MessageBox.WARNING);
										return;
									}
								} catch (TCException e1) {
									e1.printStackTrace();
								}
								importExcel();
							}
						});
					}
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.TRAILING);
					importPanel.add(panel, BorderLayout.EAST);
					{
						JButton btnTemplate = new JButton("Template");
						btnTemplate.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								ExportMasterListTemplateOperation operation = new ExportMasterListTemplateOperation(ospec);			
								session.queueOperation(operation);
							}
						});
						panel.add(btnTemplate);
					}
				}
			}
			{
				JPanel preferencePanel = new JPanel();
				FlowLayout fl_preferencePanel = (FlowLayout) preferencePanel.getLayout();
				fl_preferencePanel.setAlignment(FlowLayout.LEADING);
				tabbedPane.addTab("Preferences", null, preferencePanel, null);
				columnCheckCombo = new CheckComboBox("select Columns"){

					@Override
					public JToolTip createToolTip() {
						MultiLineToolTip tip = new MultiLineToolTip();
				        tip.setComponent(this);
				        return tip;
					}
					
				};
				
				columnCheckCombo.addMouseListener(new MouseAdapter(){

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
							toolTipTxt += ( i != 0 && i%5 == 0 ? "\n":( i != 0 ? ", ":"") ) + val;
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
				
				
				Object[] fixedColumnPre = masterListTablePanel.getFixedColumnPre();
				Object[] fixedColumnPost = masterListTablePanel.getFixedColumnPost();
				ArrayList columnList = new ArrayList();
				for( int i = 1; i < fixedColumnPre.length;i++){
					columnList.add(fixedColumnPre[i]);
				}
				for( int i = 0; i < fixedColumnPost.length;i++){
					columnList.add(fixedColumnPost[i]);
				}
				HashSet set = new HashSet();
				set.addAll(columnList);
				columnCheckCombo.resetObjs(set, false);
				{
					JLabel lblColumns = new JLabel("Columns : ");
					preferencePanel.add(lblColumns);
				}
				preferencePanel.add(columnCheckCombo);
				{
					JButton btnApply = new JButton("Apply");
					btnApply.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							Vector columns = new Vector();
							Object[] objs = columnCheckCombo.getSelectedItems();
							columns.add(masterListTablePanel.new FilterColumn("","", 0));
							for( int i = 0; objs != null && i < objs.length; i++){
								columns.add(objs[i]);
							}
							try {
								masterListTablePanel.reloadTable(columns);
								masterListTablePanel.setShowDeletedLine(chckbxShowDeletedLine.isSelected());
							} catch (TCException e) {
								e.printStackTrace();
							}
						}
					});
					preferencePanel.add(btnApply);
				}
			}
		}
		{

			splitPane = new JSplitPane();
			splitPane.setDividerSize(10);
			splitPane.setOneTouchExpandable(true);
			splitPane.setContinuousLayout(true);
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane.setTopComponent(tabbedPane);
			
			splitPane.setBottomComponent(masterListTablePanel);

			contentPanel.add(splitPane, BorderLayout.CENTER);
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new BorderLayout(0, 0));
			{
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setHgap(0);
					flowLayout.setVgap(0);
					flowLayout.setAlignment(FlowLayout.LEADING);
					buttonPane.add(panel, BorderLayout.WEST);
					{
						JPanel panel_1 = new JPanel();
						FlowLayout flowLayout_1 = (FlowLayout) panel_1.getLayout();
						flowLayout_1.setAlignment(FlowLayout.LEADING);
						panel_1.setPreferredSize(new Dimension(30, 34));
						panel.add(panel_1);
						{
							chckbxShowDeletedLine = new JCheckBox("show Deleted Line");
							chckbxShowDeletedLine.setVisible(false);
							chckbxShowDeletedLine.setSelected(true);
							chckbxShowDeletedLine.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									if( chckbxShowDeletedLine.isSelected()){
										masterListTablePanel.setShowDeletedLine(true);
									}else{
										masterListTablePanel.setShowDeletedLine(false);
									}
								}
							});
							panel_1.add(chckbxShowDeletedLine);
						}
						{
							JButton btnReload = new JButton("Reload");
							btnReload.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									
									DefaultTableModel model = (DefaultTableModel)masterListTablePanel.getTable().getModel();
									for(int i = model.getRowCount() - 1; i >= 0; i--){
										model.removeRow(i);
									}
									
									Vector<Vector> newData = dataMapper.createMasterListData(keyRowMapper, storedOptionSetMap, engine);
									Vector<Vector> newCloneData = (Vector<Vector>)newData.clone();
									masterListTablePanel.setData(newData);
									for(int i = 0; i < newData.size(); i++){
										Vector row = newCloneData.get(i);
										model.addRow(row);
									}
									masterListTablePanel.refreshRowNum();
								}
							});
							btnReload.setVisible(false);
							panel_1.add(btnReload);
						}
					}
					{
						JLabel lblNewLabel = new JLabel("Change Base : ");
						panel.add(lblNewLabel);
					}
					{
						JRadioButton rdbtnCurrentBom = new JRadioButton("Current BOM");
						rdbtnCurrentBom.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								masterListTablePanel.setCompareWithRelease(false);
							}
						});
						rdbtnCurrentBom.setSelected(true);
						panel.add(rdbtnCurrentBom);
						
						JRadioButton rdbtnReleaseBom = new JRadioButton("Release BOM");
						rdbtnReleaseBom.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								masterListTablePanel.setCompareWithRelease(true);
							}
						});
						panel.add(rdbtnReleaseBom);
						
						ButtonGroup buttonGroup = new ButtonGroup();
						buttonGroup.add(rdbtnCurrentBom);
						buttonGroup.add(rdbtnReleaseBom);
					}
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.TRAILING);
					buttonPane.add(panel);
					JButton btnExport = new JButton("Export");
					btnExport.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							exportToExcel();
						}
					});
					panel.add(btnExport);
					
					final JButton btnApply = new JButton("Apply");
					btnApply.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							
							try {
								apply(false);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					panel.add(btnApply);
					if( !isEditable){
						btnApply.setEnabled(false);
					}
					
					JButton okButton = new JButton("OK");
					panel.add(okButton);
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							try {
								apply(true);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					if( !isEditable){
						okButton.setEnabled(false);
					}
					
					JButton closeButton = new JButton("Close");
					panel.add(closeButton);
					closeButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							closeWorkingWindow();
							dispose();
						}
					});
					
				}
			}
		}
	}
	
	private void importExcel(){
		String strFilePath = MasterListDlg.this.tfFilePath.getText();
		
		if( strFilePath.trim().equals("")){
			MessageBox.post(MasterListDlg.this, "Select a File to import.", "Information", MessageBox.INFORMATION);
			return;
		}
		
		try {
			workingFmpTopLine.refresh();
		} catch (TCException e1) {
			e1.printStackTrace();
			MessageBox.post(MasterListDlg.this, e1.getMessage(), "Error", MessageBox.ERROR);
			return;
		}
		if( workingFmpTopLine.hasChildren()){
			MessageBox.post(MasterListDlg.this, "FMP already has children.", "Information", MessageBox.INFORMATION);
			return;
		}
		
		final WaitProgressBar waitBar = new WaitProgressBar(MasterListDlg.this);
		waitBar.start();
		waitBar.setStatus("Loading Excel...");
		final ExcelDataImportOreration imoprtOperation = new ExcelDataImportOreration(ospec, strFilePath);
		imoprtOperation.addOperationListener(new InterfaceAIFOperationListener() {
			
			@Override
			public void startOperation(String s) {
				
			}
			
			@Override
			public void endOperation() {
				
				try {
					HashMap<String, Object> resultData = (HashMap<String, Object>)imoprtOperation.getOperationResult();
					Object errorObj = resultData.get("ERROR");
					if( errorObj != null){
						throw (Exception)errorObj;
					}
					String fmpNo = fmpRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
					Vector<Vector> data = (Vector<Vector>)resultData.get("DATA");
					for( int i = 0; data != null && i < data.size(); i++){
						Vector row = data.get(i);
						CellValue partIdCellValue = new CellValue(
								row.get(MasterListTablePanel.MASTER_LIST_PART_ID_IDX + 1).toString()
							);
						String sysRowKey = BomUtil.getNewSystemRowKey();
						HashMap<String, Object> dataMap = new HashMap();
						dataMap.put(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY, sysRowKey);
						dataMap.put(PropertyConstant.ATTR_NAME_UOMTAG, "EA");
						partIdCellValue.setData(dataMap);
						
						Object nmcdObj = row.get(MasterListTablePanel.MASTER_LIST_NMCD_IDX + 1);
						if( !"C".equalsIgnoreCase(nmcdObj.toString()) && !"D".equalsIgnoreCase( nmcdObj.toString())){
							Object obj = row.get(MasterListTablePanel.MASTER_LIST_PROJECT_IDX + 1);
							if( obj == null || obj.equals("")){
								row.setElementAt(new CellValue(getProject()), MasterListTablePanel.MASTER_LIST_PROJECT_IDX + 1);
							}
						}
						row.setElementAt(partIdCellValue, MasterListTablePanel.MASTER_LIST_PART_ID_IDX + 1);
						
						CellValue levelACellValue = new CellValue(
								row.get(MasterListTablePanel.MASTER_LIST_LEV_A_IDX + 1).toString()
							);
						row.setElementAt(levelACellValue, MasterListTablePanel.MASTER_LIST_LEV_A_IDX + 1);
						
						//옵션이 입력되어야 할 이름이라면 Req_Opt = Y
						CellValue partNameCellValue = new CellValue(
								row.get(MasterListTablePanel.MASTER_LIST_PART_NAME_IDX + 1).toString()
							);
						if( essentialNames.contains(partNameCellValue.getValue())){
							CellValue essentialCellValue = new CellValue("Y");
							row.setElementAt(essentialCellValue, MasterListTablePanel.MASTER_LIST_REQ_OPT_IDX + 1);
						}
						
						String funcNo = row.get(MasterListTablePanel.MASTER_LIST_FUNCTION_IDX + 1).toString();
						if( funcNo.equals("")){
							row.set(MasterListTablePanel.MASTER_LIST_FUNCTION_IDX + 1, "F" + fmpNo.substring(1, 4));
						}
						
						String parentNo = row.get(MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX + 1).toString();
						if( parentNo.equals("")){
							row.set(MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX + 1, fmpNo);
						}
					}
					
					masterListTablePanel = new MasterListTablePanel(MasterListDlg.this, ospec, data);
					splitPane.setBottomComponent(masterListTablePanel);
					splitPane.revalidate();
					waitBar.dispose();
				} catch (Exception e) {
					waitBar.setStatus(e.getMessage());
					waitBar.setShowButton(true);
					return;
				}
				
			}
		});
		session.queueOperation(imoprtOperation);
	}
	
	private void searchMasterListView(){
		final WaitProgressBar waitBar = new WaitProgressBar(MasterListDlg.this);
		waitBar.start();
		
		Date date = dateChooser.getDate();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String dateStr = sdf.format(date) + " 23:59:59";
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			date = sdf.parse(dateStr);
			
			final BOMLoadWithDateOperation operation = new BOMLoadWithDateOperation(workingFmpTopLine, essentialNames, date, waitBar);
			operation.addOperationListener(new InterfaceAIFOperationListener() {
				
				@Override
				public void startOperation(String s) {
					System.out.println("BOMLoadWithDateOperation Start : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));					
				}
				
				@Override
				public void endOperation() {
					try {
						HashMap<String, Object> result = (HashMap<String, Object>)operation.getOperationResult();
						Exception exception = (Exception)result.get(BOMLoadWithDateOperation.DATA_ERROR);
						if( exception != null){
							throw exception;
						}
						MasterListDataMapper releaseDataMapper = (MasterListDataMapper)result.get(BOMLoadWithDateOperation.DATA_MAPPER);
						Date date = (Date)result.get(BOMLoadWithDateOperation.DATA_DATE);
						HashMap<String, StoredOptionSet> storedOptionSetMap = (HashMap<String, StoredOptionSet>)result.get(BOMLoadWithDateOperation.DATA_STORED_OPTION_SET);
						OSpec ospec = (OSpec)result.get(BOMLoadWithDateOperation.DATA_OSPEC);
						
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
						
						HashMap<String, Vector> keyRowMapper = new HashMap();
						Vector<Vector> data = releaseDataMapper.createMasterListData(keyRowMapper, storedOptionSetMap, engine);
						
						MasterListViewDlg viewDlg = new MasterListViewDlg(MasterListDlg.this, ospec, data, keyRowMapper
								, null, MasterListDlg.this.getCurrentUserId(), MasterListDlg.this.getCurrentUserName(), MasterListDlg.this.getCurrentUserGroup(), MasterListDlg.this.getCurrentUserPa6Group(), MasterListDlg.this.isCordinator());
						viewDlg.setTitle(sdf.format(date));
						viewDlg.setPreferredSize(new Dimension(800, 600));
						viewDlg.setVisible(true);
						waitBar.dispose();
					} catch (Exception e) {
						e.printStackTrace();
						waitBar.setStatus(e.getMessage());
						waitBar.setShowButton(true);
					}finally{
						System.out.println("BOMLoadWithDateOperation End : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
					}
				}
			});
			session.queueOperation(operation);
			
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
	}
	
	private void applySpec(){
		JTable table = masterListTablePanel.getTable();
		DefaultTableModel model = (DefaultTableModel)masterListTablePanel.getTable().getModel();
		
		for( int i = 0; i < model.getRowCount(); i++){
			@SuppressWarnings("unused")
			String partId = model.getValueAt(i, MasterListTablePanel.MASTER_LIST_PART_ID_IDX).toString();
			String parentId = model.getValueAt(i, MasterListTablePanel.MASTER_LIST_PARENT_ID_IDX).toString();
			Object obj = model.getValueAt(i, MasterListTablePanel.MASTER_LIST_SPEC_IDX);
			
			String spec = obj.toString();
			
			//Trim옵션은 Master List상에 나타나지 않으므로 데이타를 가져온다.
			if( spec.equals("")){
				if( obj instanceof CellValue){
					CellValue specCellValue = (CellValue)obj;
					HashMap<String,Object> dataMap = specCellValue.getData();
					if( dataMap != null){
						Object tmpObj = dataMap.get("SPEC");
						if( tmpObj != null){
							spec = tmpObj.toString();
						}
					}
				}
			}
			
			String parentSpec = BOMUpdateOperation.getParentSpec(masterListTablePanel.getTable(), fmpId, i, parentId, null, true);
			if( !parentSpec.equals("")){
				spec = parentSpec + (spec.equals("") ? "" : " and (" + spec + ")");
			}
			
//			conditionMap.put(partId, spec);
			
			//Spec값이 없을 경우, 대표수량을 Usage있는 Cell에만 적용함.
			if( spec.equals("")){
				
				obj = model.getValueAt(i, MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
				
				boolean isEmptyRow = true;
				for( int j = masterListTablePanel.getFixedColumnPre().length - 1; j < masterListTablePanel.getFixedColumnPre().length + ospec.getTrimList().size() - 1;j++){
					Object timObj = model.getValueAt(i, j);
					String tmpStr = timObj.toString().trim();
					isEmptyRow &= tmpStr.equals("");
					if( !isEmptyRow){
						break;
					}
				}
				for( int j = masterListTablePanel.getFixedColumnPre().length - 1; j < masterListTablePanel.getFixedColumnPre().length + ospec.getTrimList().size() - 1;j++){
					Object timObj = model.getValueAt(i, j);
					String tmpStr = timObj.toString();
					boolean isOpt = false;
					if( tmpStr.indexOf("(") > -1){
						tmpStr = tmpStr.replaceAll("\\(", "");
						isOpt = true;
					}
					
					if( tmpStr.indexOf(")") > -1){
						tmpStr = tmpStr.replaceAll("\\)", "");
						isOpt = true;
					}
					
					// 모든 컬럼(Usage)이 비었을 경우 대표수량을 입력.
					if( isEmptyRow){
						if( tmpStr.equals("") ){
							model.setValueAt(new CellValue(obj.toString()), i, j);
							continue;
						}
					}else{
						if( !tmpStr.equals("") ){
							try{
								int qty = Integer.parseInt(obj.toString());
								CellValue cellValue = new CellValue(isOpt ? "(" + qty + ")" : "" + qty);
								model.setValueAt(cellValue, i, j);
							}catch( Exception ee){
								table.setRowSelectionInterval(i, i);
								table.setColumnSelectionInterval(j, j);
								MessageBox.post(MasterListDlg.this, ee.getMessage(), "ERROR", MessageBox.ERROR);
								return;
							}	
						}
					}
					
				}
//				continue;
			}else{
				obj = model.getValueAt(i, MasterListTablePanel.MASTER_LIST_REPRESENTATIVE_QUANTITY_IDX);
				String representativeQty = obj.toString();
				for( int j = masterListTablePanel.getFixedColumnPre().length - 1; j < masterListTablePanel.getFixedColumnPre().length + ospec.getTrimList().size() - 1;j++){
					String columnName = model.getColumnName(j);
					StoredOptionSet sosStd = storedOptionSetMap.get(columnName + "_STD");
					StoredOptionSet sosOpt = storedOptionSetMap.get(columnName + "_OPT");
//					obj = model.getValueAt(i, j);
					CellValue cellValue = null;
					
					// [20161028][ymjang] 소스 정리 
					if (sosStd.isInclude(engine, spec)){
						cellValue = new CellValue("");
						model.setValueAt(cellValue, i, j);
						cellValue.setValue(representativeQty);
					} else if(sosOpt.isInclude(engine, spec)){
						cellValue = new CellValue("");
						model.setValueAt(cellValue, i, j);
						cellValue.setValue( representativeQty.equals("") ? "":"(" + representativeQty + ")");
					} else {
						cellValue = new CellValue("");
						model.setValueAt(cellValue, i, j);
					}
					
//					if( sosStd.isInclude(engine, spec)){
//						
////						obj = model.getValueAt(i, j);
////						if( obj instanceof CellValue){
////							cellValue = (CellValue)obj;
////						}else{
////							cellValue = new CellValue("");
////							model.setValueAt(cellValue, i, j);
////						}
//						cellValue = new CellValue("");
//						model.setValueAt(cellValue, i, j);
//						cellValue.setValue(representativeQty);
//					}else{
//						obj = model.getValueAt(i, j);
//						if( sosOpt.isInclude(engine, spec)){
//							
////							if( obj instanceof CellValue){
////								cellValue = (CellValue)obj;
////							}else{
////								cellValue = new CellValue("");
////								model.setValueAt(cellValue, i, j);
////							}
//							cellValue = new CellValue("");
//							model.setValueAt(cellValue, i, j);
//							cellValue.setValue( representativeQty.equals("") ? "":"(" + representativeQty + ")");
//						}else{
//							cellValue = new CellValue("");
//							model.setValueAt(cellValue, i, j);
//						}
//					}
					
				}
			}
			
		}
		
		table.repaint();
	}
	
	private void apply(final boolean afterClose) throws Exception{
		ArrayList<String> errorList = new ArrayList();
		if( !validateBasicInfo(errorList)){
			MessageBox.post(MasterListDlg.this, errorList.get(0), "ERROR", MessageBox.ERROR);
			return;
		}
		
		final WaitProgressBar waitBar = new WaitProgressBar(MasterListDlg.this);
		waitBar.start();
		
		final BOMUpdateOperation operation = new BOMUpdateOperation(MasterListDlg.this, waitBar, dataMapper, releaseDataMapper, storedOptionSetMap, preProductRevision.getProperty("s7_PROJECT_CODE"));
//		try {
//			// dataMapper는 Command에서 생성한 WokringFmpLine을 Top으로 생성한 BOM Window 기반의 정보이다.
//			operation = new BOMUpdateOperation(MasterListDlg.this, waitBar, dataMapper, releaseDataMapper, storedOptionSetMap);
//		} catch (Exception e) {
//			e.printStackTrace();
//			waitBar.setStatus(e.getMessage());
//			waitBar.setShowButton(true);
//			return;
//		}
		operation.addOperationListener(new InterfaceAIFOperationListener() {
			
			@Override
			public void startOperation(String s) {
				System.out.println("BOMUpdateOperation Start : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			}
			
			@Override
			public void endOperation() {
				
				masterListTablePanel.getTable().repaint();
				
				HashMap<String, Object> resultData = (HashMap<String, Object>)operation.getOperationResult();
				if( !resultData.containsKey(BOMLoadWithDateOperation.DATA_ERROR)){
					if( afterClose ){
						closeWorkingWindow();
						dispose();
					}
				}
				System.out.println("BOMUpdateOperation End : " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
			}
		});
		TCSession session = CustomUtil.getTCSession();
		session.queueOperation(operation);
	}
	
	public ArrayList<String> getEssentialNames() {
		return essentialNames;
	}

	private void closeWorkingWindow(){
		TCComponentBOMLine workingFmpTopLine = getWorkingFmpTopLine();
		if( workingFmpTopLine != null){
			try {
				TCComponentBOMWindow window = workingFmpTopLine.window();
				if( window != null){
					window.close();
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isEditable() {
		return isEditable;
	}

	public void setEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public JComboBox getCbCCN() {
		return cbCCN;
	}

	public String getProject() {
		return lbProject.getText();
	}

	public TCComponentItemRevision getFmpRevision() {
		return fmpRevision;
	}

	public MasterListTablePanel getMasterListTablePanel() {
		return masterListTablePanel;
	}

	public HashMap<String, Vector> getKeyRowMapper() {
		return keyRowMapper;
	}

	public HashMap<String, Vector> getReleaseKeyRowMapper() {
		return releaseKeyRowMapper;
	}	
	
	private boolean validateBasicInfo(ArrayList<String> errorList){
		Object obj = cbCCN.getSelectedItem();
		if( !(obj instanceof SimpleTcObject)){
			errorList.add("Select a CCN");
			return false;
		}
		return true;
	}
	
	private void exportToExcel(){
		
		String preStr = "MasterList_";
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File(preStr + sdf.format(now.getTime()) + ".xls");
		fileChooser.setSelectedFile(defaultFile);
//		fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSEXCEL"));
		fileChooser.setFileFilter(new FileFilter(){

			public boolean accept(File f) {
				if(f.isDirectory())
				{
					return true;
				}
				if( f.isFile()){
					return f.getName().endsWith("xls");
				}
				return false;
			}

			public String getDescription() {
				return "*.xls";
			}
			
		});
		int result = fileChooser.showSaveDialog(this);
		if( result == JFileChooser.APPROVE_OPTION){
			File selectedFile = fileChooser.getSelectedFile();
			try
            {
				export(selectedFile, masterListTablePanel, currentUserName, currentUserGroup, isCordinator);
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            	MessageBox.post(this, e.getMessage(), "ERROR", MessageBox.ERROR);
            }
		}
		
	}
	
	public static Vector createTableHeader(Vector headerVector, MasterListTablePanel masterListTablePanel){
		headerVector.clear();

		@SuppressWarnings("unused")
		int columnIdx = 0;
		for (int i = 0; i < masterListTablePanel.getFixedColumnPre().length; i++) {
			headerVector.add(masterListTablePanel.getFixedColumnPre()[i]);
		}

		int tmpColumIdx = masterListTablePanel.getFixedColumnPre().length;
		ArrayList<OpTrim> trimList = masterListTablePanel.getOspec().getTrimList();
		for (OpTrim trim : trimList) {
			headerVector.add(masterListTablePanel.new FilterColumn(trim.getTrim(), "", tmpColumIdx++) );
		}

		for (int i = 0; i < masterListTablePanel.getFixedColumnPost().length; i++) {
			headerVector.add(masterListTablePanel.getFixedColumnPost()[i]);
		}
		
		return headerVector;
	}
	
	public static void export(File selectedFile, MasterListTablePanel masterListTablePanel
			, String currentUserName, String currentUserGroup, boolean isCordinator) throws IOException, WriteException{
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);
	    
	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    cellFormat.setAlignment(Alignment.CENTRE);
	    cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat eplFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    eplFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    eplFormat.setWrap(true);
	    eplFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 0;
	    int initColumnNum = 0;

	    //모든 컬럼이 표기되는 테이블을 임시로 생성.
	    HashMap<Integer, List<ColumnGroup>> colGroupMap = new HashMap();
	    int colGroupMaxSize = 1;
	    Vector<Object> columns = createTableHeader(new Vector(), masterListTablePanel);
	    columns.removeElementAt(0);
//	    columns.remove(0);
	    DefaultTableModel model = new DefaultTableModel(null, columns);
		JTable table = new JTable(model){
			protected JTableHeader createDefaultTableHeader() {
				return new GroupableTableHeader(columnModel);
			}
		};
		for (int i = 1; i < columns.size() - 1; i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(masterListTablePanel.new FilterRenderer((FilterColumn)columns.get(i + 1)));
		}
		
		ArrayList currentPreColumnList = new ArrayList();
		for( int i = 0; i < masterListTablePanel.getFixedColumnPre().length; i++){
			currentPreColumnList.add(masterListTablePanel.getFixedColumnPre()[i]);
		}
		
		GroupableTableHeader header = (GroupableTableHeader)table.getTableHeader();
		masterListTablePanel.setGroupColumn(table, masterListTablePanel.getOspec().getTrimList(), 1, header, currentPreColumnList);
		
    	TableColumnModel tcm = table.getColumnModel();
	    for( int i = 1; i < columns.size(); i++){
	    	int tableIdx = table.convertColumnIndexToView(i - 1);
	    	if( tableIdx < 0){
	    		continue;
	    	}
	    	@SuppressWarnings("unused")
			Object obj = columns.get(i);
	    	Enumeration<ColumnGroup> enums = header.getColumnGroups( tcm.getColumn(tableIdx) );
	    	if( enums != null){
	    		List tmpList = Collections.list(enums);
	    		if( colGroupMaxSize < tmpList.size()){
	    			colGroupMaxSize = tmpList.size();
	    		}
	    		colGroupMap.put(i, tmpList);
	    	}
	    }
	    
	    //Header Write & 세로 Merge
	    for (int i = 0; i < columns.size(); i++){
	    	List<ColumnGroup> colGroupList = colGroupMap.get(i);
	    	if( colGroupList == null){
				for (int j = 0; j < colGroupMaxSize; j++) {
					label = new jxl.write.Label(i + initColumnNum, startRow + j, 
							columns.get(i).toString(),
							headerCellFormat);
					sheet.addCell(label);
				}
				sheet.mergeCells(i + initColumnNum, startRow, i + initColumnNum, startRow + colGroupMaxSize);
	    		WritableCell wCell = sheet.getWritableCell(i + initColumnNum, startRow);
    			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
    			cf.setBackground(Colour.GREY_25_PERCENT);
    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
    			cf.setAlignment(Alignment.CENTRE);
    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
    		    wCell.setCellFormat(cf);
	    	}else{
	    		int j = 0;
	    		for (; j < colGroupList.size(); j++) {
					label = new jxl.write.Label(i + initColumnNum, startRow + j, 
							(String)colGroupList.get(j).getHeaderValue(),
							headerCellFormat);
					sheet.addCell(label);
					
		    		WritableCell wCell = sheet.getWritableCell(i + initColumnNum, startRow + j);
	    			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    			cf.setBackground(Colour.GREY_25_PERCENT);
	    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
	    			cf.setAlignment(Alignment.CENTRE);
	    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
	    		    wCell.setCellFormat(cf);
				}
	    		
	    		for( ; j <= colGroupMaxSize; j++){
	    			label = new jxl.write.Label(i + initColumnNum, startRow + j, 
							columns.get(i).toString(),
							headerCellFormat);
					sheet.addCell(label);
	    		}
	    		
	    		sheet.mergeCells(i + initColumnNum, startRow + colGroupList.size(), i + initColumnNum, startRow + colGroupMaxSize);
	    		WritableCell wCell = sheet.getWritableCell(i + initColumnNum, startRow + colGroupList.size());
    			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
    			cf.setBackground(Colour.GREY_25_PERCENT);
    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
    			cf.setAlignment(Alignment.CENTRE);
    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
    		    wCell.setCellFormat(cf);
	    		
	    	}
	    	
			CellView cv = sheet.getColumnView(i + initColumnNum);
			cv.setSize(columns.get(i).toString().length() * 700);
			
//			if( i == 0 ){
//				sheet.setColumnView(600, cv);
//			}else{
//				sheet.setColumnView(i + initColumnNum, cv);
//			}
			sheet.setColumnView(i + initColumnNum, cv);
	    }
	    
	    //Header 가로 Merge
	    int startIdxToMerge = 0, endIdxToMerge = 0;
	    for( int i = colGroupMaxSize - 1; i >= 0; i--){
	    	startIdxToMerge = 0; 
	    	endIdxToMerge = 0;
	    	for( int j = 0; j < columns.size(); j++){
		    	if( isSameWithNextCell(sheet, initColumnNum + j, i + startRow)){
		    		endIdxToMerge = j + initColumnNum + 1;
		    	}else{
		    		if( startIdxToMerge < endIdxToMerge){
			    		sheet.mergeCells(startIdxToMerge , i + startRow, endIdxToMerge, i + startRow);
			    		WritableCell wCell = sheet.getWritableCell(startIdxToMerge, i + startRow);
		    			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
		    			cf.setBackground(Colour.GREY_25_PERCENT);
		    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
		    			cf.setAlignment(Alignment.CENTRE);
		    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
		    		    wCell.setCellFormat(cf);
		    		}
		    		startIdxToMerge = j + 1 + initColumnNum;
		    	}
		    }
	    }	    
	    
	    //Data Write.
	    table = masterListTablePanel.getTable();
	    startRow += colGroupMaxSize + 1;
	    for( int i = 0; i < table.getRowCount(); i++){
	    	for( int j = 0; j < columns.size(); j++){
//	    		if( j == 0){
//	    			label = new jxl.write.Label(j + initColumnNum, i + startRow, i + 1 + "", cellFormat);
//	        		sheet.addCell(label);
//	    		}else{
//	    			label = new jxl.write.Label(j + initColumnNum, i + startRow, table.getModel().getValueAt(i, j - 1) + "", cellFormat);
//	        		sheet.addCell(label);
//	    		}
	    		
	    		String valueStr = table.getModel().getValueAt(i, j).toString();
	    		
	    		//보안 적용
//	    		if( j == masterListTablePanel.MASTER_LIST_EST_COST_MATERIAL_IDX 
//	    				|| j == masterListTablePanel.MASTER_LIST_TARGET_COST_MATERIAL_IDX 
//						|| j == masterListTablePanel.MASTER_LIST_PRT_TOOLG_INVESTMENT_IDX 
//						|| j == masterListTablePanel.MASTER_LIST_PRD_TOOL_COST_IDX
//						|| j == masterListTablePanel.MASTER_LIST_PRD_SERVICE_COST_IDX 
//						|| j == masterListTablePanel.MASTER_LIST_PRD_SAMPLE_COST_IDX
//						|| j == masterListTablePanel.MASTER_LIST_PRD_SUM_IDX){
//	    			
//	    			Object deptObj = table.getModel().getValueAt(i, masterListTablePanel.MASTER_LIST_ENG_DEPT_NM_IDX);
//	    			String deptStr = deptObj.toString();
//	    			Object resObj = table.getModel().getValueAt(i, masterListTablePanel.MASTER_LIST_ENG_RESPONSIBILITY_IDX);
//	    			String resStr = resObj.toString();
//	    			
//	    			if( currentUserGroup.equalsIgnoreCase(deptStr)
//							&& currentUserName.equalsIgnoreCase(resStr)){
//					}else{
//						if( currentUserGroup.equalsIgnoreCase(deptStr) && isCordinator){
//						}else{
//							valueStr = "******";
//						}
//					}
//	    		}
	    		
	    		label = new jxl.write.Label(j + initColumnNum , i + startRow, valueStr, cellFormat);
        		sheet.addCell(label);
	    	}
	    }
	    workBook.write();
	    workBook.close();	
	}	

	private static boolean isSameWithNextCell(WritableSheet sheet, int column, int row){
		if( row < 0){
			return true;
		}
		Cell cell = sheet.getCell(column, row);
    	Cell nextCell = sheet.getCell(column + 1, row);
    	if( !cell.getContents().equals(nextCell.getContents())){
    		return false;
    	}else{
    		return isSameWithNextCell(sheet, column, row - 1);
    	}
	}

	public OSpec getOspec() {
		return ospec;
	}

	public ArrayList<VariantOption> getEnableOptionSet() {
		return enableOptionSet;
	}

	public void setEnableOptionSet(ArrayList<VariantOption> enableOptionSet) {
		this.enableOptionSet = enableOptionSet;
	}

	@Override
	public HashMap<String, ArrayList<String>> getWorkingChildRowKeys() {
		return dataMapper.getChildRowKeyMap();
	}
}
