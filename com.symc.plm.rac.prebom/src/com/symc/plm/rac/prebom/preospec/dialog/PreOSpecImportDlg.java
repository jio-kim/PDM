package com.symc.plm.rac.prebom.preospec.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ospec.op.OSpec;
import com.kgm.commands.ospec.op.OpCategory;
import com.kgm.commands.ospec.op.OpGroup;
import com.kgm.commands.ospec.op.OpTrim;
import com.kgm.commands.ospec.op.OpUtil;
import com.kgm.commands.ospec.op.Option;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.DatasetService;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.common.util.BomUtil;
import com.symc.plm.rac.prebom.common.util.BundleUtil;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.common.util.SDVPreBOMUtilities;
import com.symc.plm.rac.prebom.common.util.SDVQueryUtils;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.preospec.ui.OSpecTable;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMWindowType;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRuleType;
import com.teamcenter.rac.kernel.TCComponentVariantRule;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCProperty;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.kernel.TCVariantService;
import com.teamcenter.rac.pse.variants.sosvi.SelectedOptionSetDialog;
import com.teamcenter.rac.util.MessageBox;
import com.toedter.calendar.JDateChooser;
//import java.io.FileFilter;

/**
 * [20160907][ymjang] 컬럼명 오류 수정
 * [20161014][ymjang] 테스트 소스 수정
 * [20170314][ymjang] 암호화 문서 경고 문구 삽입
 * [20180213][ljg] 시스템 코드 리비전 정보에서 bomline정보로 이동
 * [20180307][LJG] 표준품도 시스템코드를 BOMLine에서 가져오도록 변경
 */
@SuppressWarnings("serial")
public class PreOSpecImportDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
//	private JTextField textField;
	private JTextField tfFilePath;
	private JLabel lblAttachFileMsg;
	
	private JTable table;
	
	private Vector<String> headerVector = new Vector<String>();
	private JComboBox<String> cbGmodel = null;
	private JComboBox<String> cbProject = null;
	private JDateChooser dateChooser = null;
	private JTabbedPane tabbedPane = null;
	private JPanel searchPanel = null;
	private JPanel regPanel = null;
	
	private final int ALREADY_EXIST_ITEM_REVISION = 2;
	private final int CREATION_SUCCESS = 1;
	
	private TCComponentItemRevision ospecRevision = null;
	private PreOSpecCompareDlg comparableDlg = null;
	private JButton btnCompare = null;
	private JButton btnDetailView = null;
	private HashMap<String, HashMap<String, Object>> productAllChildPartsList;
	private HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> productAllChildPartsUsageList;
	private HashMap<String, String> systemCodeMap;
	private String ccnItemID = null;
    private ScriptEngineManager manager = new ScriptEngineManager();
    private ScriptEngine engine = manager.getEngineByName("js"); 

	private String[] parentRevProperties = 
	    {
	        PropertyConstant.ATTR_NAME_ITEMID,
	        PropertyConstant.ATTR_NAME_DISPLAYPARTNO,
	        PropertyConstant.ATTR_NAME_ITEMTYPE,
	        PropertyConstant.ATTR_NAME_ITEMNAME,
	        PropertyConstant.ATTR_NAME_ITEMREVID
	    };
    private String[] bomlineProperties =
        {
            PropertyConstant.ATTR_NAME_BL_ITEM_ID,               //0
            PropertyConstant.ATTR_NAME_BL_SEQUENCE_NO,       //1
            PropertyConstant.ATTR_NAME_BL_OBJECT_TYPE,       //2
            PropertyConstant.ATTR_NAME_BL_REV_OBJECT_TYPE,   //3
            PropertyConstant.ATTR_NAME_BL_QUANTITY,          //4
            PropertyConstant.ATTR_NAME_BL_VARIANT_CONDITION, //5
            PropertyConstant.ATTR_NAME_BL_OCC_FND_OBJECT_ID, //6
            PropertyConstant.ATTR_NAME_BL_ITEM_REVISION_ID,  //7
            PropertyConstant.ATTR_NAME_BL_ABS_OCC_ID,        //8
            
            
            PropertyConstant.ATTR_NAME_BL_MODULE_CODE,         //9
            PropertyConstant.ATTR_NAME_BL_SUPPLY_MODE,         //10
            
            PropertyConstant.ATTR_NAME_BL_REQ_OPT,             //11  REQ OPT
            PropertyConstant.ATTR_NAME_BL_SPEC_DESC,           //12
            PropertyConstant.ATTR_NAME_BL_CHG_CD,              //13     
            PropertyConstant.ATTR_NAME_BL_ALTER_PART,          //14
            PropertyConstant.ATTR_NAME_BL_LEV_M,                //15  LEV M
            PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY,		//16
            
            /* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
            PropertyConstant.ATTR_NAME_BL_DVP_NEEDED_QTY,         // 17
            PropertyConstant.ATTR_NAME_BL_DVP_USE,                // 18
            PropertyConstant.ATTR_NAME_BL_DVP_REQ_DEPT,           // 19
            
            /* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
            PropertyConstant.ATTR_NAME_BL_ENG_DEPT_NM,            // 20
            PropertyConstant.ATTR_NAME_BL_ENG_RESPONSIBLITY,      // 21
            
          //[SR170703-020][LJG]Proto Tooling 컬럼 추가
            PropertyConstant.ATTR_NAME_BL_PROTO_TOOLING,      // 22
            
          //[20180213][LJG] BOMLine으로 이동 ->사용 안함
            PropertyConstant.ATTR_NAME_BL_BUDGETCODE //23
        };
    
    private String[] revisionProperties =
        {
            PropertyConstant.ATTR_NAME_PROJCODE,               // 0
          //[20180213][LJG] BOMLine으로 이동 ->사용 안함
            PropertyConstant.ATTR_NAME_BUDGETCODE,             // 1 
            PropertyConstant.ATTR_NAME_COLORID,                // 2
            PropertyConstant.ATTR_NAME_ESTWEIGHT,              // 3     ESTIMATE WEIGHT
            PropertyConstant.ATTR_NAME_CALWEIGHT,              // 4
            PropertyConstant.ATTR_NAME_TARGET_WEIGHT,          // 5     TARGET WEIGHT
            PropertyConstant.ATTR_NAME_CONTENTS,               // 6
            PropertyConstant.ATTR_NAME_CHG_TYPE_NM,            // 7
//            PropertyConstant.ATTR_NAME_ORIGIN_PROJECT,        // 8 번째
            PropertyConstant.ATTR_NAME_CON_DWG_PLAN,           // 8
            PropertyConstant.ATTR_NAME_CON_DWG_PERFORMANCE,    // 9
            PropertyConstant.ATTR_NAME_CON_DWG_TYPE,           // 10
            PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE,    // 11
            PropertyConstant.ATTR_NAME_PRD_DWG_PERFORMANCE,    // 12
            PropertyConstant.ATTR_NAME_PRD_DWG_PLAN,           // 13
            
            /* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
//            PropertyConstant.ATTR_NAME_DVP_NEEDED_QTY,         // 14
//            PropertyConstant.ATTR_NAME_DVP_USE,                // 15
//            PropertyConstant.ATTR_NAME_DVP_REQ_DEPT,           // 16
            "",											         // 14
            "",									                 // 15
            "",										             // 16
            
            /* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
//            PropertyConstant.ATTR_NAME_ENG_DEPT_NM,            // 17
//            PropertyConstant.ATTR_NAME_ENG_RESPONSIBLITY,      // 18
            "",		// 17
            "",		// 18
//            PropertyConstant.ATTR_NAME_CIC_DEPT_NM,             // 19 번째
            PropertyConstant.ATTR_NAME_EST_COST_MATERIAL,      // 19
            PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL,   // 20
            PropertyConstant.ATTR_NAME_SELECTED_COMPANY,       // 21
            PropertyConstant.ATTR_NAME_PRT_TOOLG_INVESTMENT,   // 22
            PropertyConstant.ATTR_NAME_PRD_TOOL_COST,          // 23
            PropertyConstant.ATTR_NAME_PRD_SERVICE_COST,       // 24
            PropertyConstant.ATTR_NAME_PRD_SAMPLE_COST,        // 25
            //[20160907][ymjang] 컬럼명 오류 수정
            PropertyConstant.ATTR_NAME_PUR_DEPT_NM,            // 26
            PropertyConstant.ATTR_NAME_PUR_RESPONSIBILITY,     // 27
            PropertyConstant.ATTR_NAME_EMPLOYEE_NO,            // 28
            PropertyConstant.ATTR_NAME_CHANGE_DESCRIPTION,     // 29
            PropertyConstant.ATTR_NAME_SELECTIVEPART,          // 30
            PropertyConstant.ATTR_NAME_DR,                     // 31
            PropertyConstant.ATTR_NAME_OLD_PART_NO,            // 32
            PropertyConstant.ATTR_NAME_BOX,                    // 33
            PropertyConstant.ATTR_NAME_REGULATION,             // 34
            PropertyConstant.ATTR_NAME_DISPLAYPARTNO,          // 35
            PropertyConstant.ATTR_NAME_ITEMID,                 // 36
            PropertyConstant.ATTR_NAME_ECO_NO,                  // 37
            PropertyConstant.ATTR_NAME_PRD_PROJ_CODE    //38
        };
    
    InterfaceAIFComponent[] targets;

	/**
	 * Launch the application.
	 */
//	public static void main(String[] args) {
//		try {
//			PreOSpecImportDlg dialog = new PreOSpecImportDlg();
//			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
//			dialog.setVisible(true);
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * Create the dialog.
	 */
	public PreOSpecImportDlg(Frame frame, InterfaceAIFComponent[] target) {
//		super(AIFUtility.getActiveDesktop().getFrame(), false);
		super(frame);
		this.targets = target;
		setTitle("Pre O/Spec Importer");
		setBounds(100, 100, 700, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			tabbedPane = new JTabbedPane(JTabbedPane.TOP);
			contentPanel.add(tabbedPane);
			{
				searchPanel = new JPanel();
				tabbedPane.addTab("Pre O/Spec Selection", null, searchPanel, null);
				searchPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					searchPanel.add(panel, BorderLayout.NORTH);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JPanel fndOptPanel = new JPanel();
						FlowLayout flowLayout = (FlowLayout) fndOptPanel.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel.add(fndOptPanel);
						{
							JLabel lblGmodel = new JLabel("G-Model : ");
							fndOptPanel.add(lblGmodel);
						}
						{
							cbGmodel = new JComboBox<String>();
							cbGmodel.addItem(OpUtil.SELECT_G_MODEL);
							cbGmodel.addItemListener(new ItemListener() {
								public void itemStateChanged(ItemEvent event) {
									if( event.getStateChange() == ItemEvent.SELECTED){
										try{
											OpUtil.refreshProject(cbGmodel, cbProject);
										}catch(Exception e){
											MessageBox.post(PreOSpecImportDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
										}
									}
								}
							});
							fndOptPanel.add(cbGmodel);
						}
						{
							JLabel lblProject = new JLabel("  Project : ");
							fndOptPanel.add(lblProject);
						}
						{
							cbProject = new JComboBox<String>();
							cbProject.addItem(OpUtil.SELECT_PROJECT);
							fndOptPanel.add(cbProject);
						}
						{
							JLabel lblNewLabel = new JLabel(" Date : ");
							fndOptPanel.add(lblNewLabel);
						}
						{
							dateChooser = new JDateChooser(null, "yyyy-MM-dd", false, null);
							fndOptPanel.add(dateChooser);
						}
					}
					{
						JPanel fndPanel = new JPanel();
						panel.add(fndPanel, BorderLayout.EAST);
						{
							final JButton btnNewButton = new JButton("Search");
							btnNewButton.setIcon(new ImageIcon(PreOSpecImportDlg.class.getResource("/icons/search_16.png")));
							btnNewButton.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent event) {
									
									final WaitProgressBar waitProgress = new WaitProgressBar(PreOSpecImportDlg.this);
									waitProgress.start();
									waitProgress.setStatus("Searching....");
									TCSession session = CustomUtil.getTCSession();
									final AbstractAIFOperation operation = new AbstractAIFOperation() {
										
										@Override
										public void executeOperation() throws Exception {
											try{
												refreshTable();
											}catch(Exception e){
												e.printStackTrace();
												storeOperationResult(e.getMessage());
											}
										}
									};
									
									operation.addOperationListener(new InterfaceAIFOperationListener() {
										
										@Override
										public void startOperation(String s) {
										}
										
										@Override
										public void endOperation() {
											String result = (String)operation.getOperationResult();
											if( result != null && !result.equals("") ){
												waitProgress.setStatus(result);
												waitProgress.setShowButton(true);
											}else{
												waitProgress.close();
											}
										}
									});
									session.queueOperation(operation);
								}
							});
							fndPanel.add(btnNewButton);
						}
					}
				}
				{
					JPanel panel = new JPanel();
					searchPanel.add(panel, BorderLayout.CENTER);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JScrollPane scrollPane = new JScrollPane();
						panel.add(scrollPane, BorderLayout.CENTER);
						{
//							table = new JTable();
							headerVector.add("G-Model");
							headerVector.add("Project");
							headerVector.add("Released Date");
							headerVector.add("OSI-No");
							headerVector.add("ospec");
							TableModel model = new DefaultTableModel(null, headerVector) {
								@SuppressWarnings({ "unchecked", "rawtypes" })
                                public Class getColumnClass(int col) {
									return String.class;
								}

								public boolean isCellEditable(int row, int col) {
									return false;
								}
						    };
						    table = new JTable(model);
						    TableColumnModel cm = table.getColumnModel();
						    cm.removeColumn(cm.getColumn(4));
						    table.addMouseListener(new MouseAdapter(){

								@Override
								public void mouseReleased(MouseEvent e) {
									if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
											&& e.isControlDown()==false) {
										
										detailView();
										// O/Spec 상세 정보를 가져온다.
										
									}
									super.mouseReleased(e);
								}
								
							});

						    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
						    table.setRowSorter(sorter);							
							
						    table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
								
								@Override
								public void valueChanged(ListSelectionEvent e) {
									if( table.getSelectedRowCount() == 2){
										btnCompare.setEnabled(true);
										btnDetailView.setEnabled(false);
									}else if( table.getSelectedRowCount() == 1){
										btnDetailView.setEnabled(true);
										btnCompare.setEnabled(false);
									}else{
										btnCompare.setEnabled(false);
										btnDetailView.setEnabled(false);
									}
								}
							});;
						    
							scrollPane.setViewportView(table);
						}
					}
				}
			}
			{
				regPanel = new JPanel();
				tabbedPane.addTab("Pre O/Spec Registration", null, regPanel, null);
				regPanel.setLayout(new BorderLayout(0, 0));
				{
					FlowLayout flowLayout = null;
					
					JPanel msgPanel = new JPanel();
					flowLayout = (FlowLayout) msgPanel.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					regPanel.add(msgPanel, BorderLayout.NORTH);
					{
				        lblAttachFileMsg = new JLabel();
				        lblAttachFileMsg.setFont(new Font("맑은 고딕", Font.BOLD, 13));
				        lblAttachFileMsg.setForeground(Color.RED);
				        lblAttachFileMsg.setText("※ 암호화된 문서는 반드시 암호를 해제하신 후, 등록하셔야 합니다. ※");
				        
				        msgPanel.add(lblAttachFileMsg);
				        
					}
					
					JPanel panel = new JPanel();
					flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.LEADING);
					panel.setBorder(new TitledBorder(null, "New O/Spec Registration", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					regPanel.add(panel, BorderLayout.CENTER);
					{
						tfFilePath = new JTextField();
						panel.add(tfFilePath);
						tfFilePath.setColumns(30);
					}
					{
						JButton btnFind = new JButton("Find..");
						btnFind.setIcon(new ImageIcon(PreOSpecImportDlg.class.getResource("/icons/search_16.png")));
						btnFind.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								
								JFileChooser fileChooser = new JFileChooser();
//								fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
								fileChooser.setFileFilter(new FileFilter(){

									@Override
									public boolean accept(File f) {
										if (f.isDirectory()) {
									        return true;
									    }
										
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
								int result = fileChooser.showOpenDialog(PreOSpecImportDlg.this);
								if( result == JFileChooser.APPROVE_OPTION){
									File selectedFile = fileChooser.getSelectedFile();
									PreOSpecImportDlg.this.tfFilePath.setText( selectedFile.getAbsolutePath() );
								}						
								
							}
						});
						panel.add(btnFind);
					}
				}
				{
					JPanel panel = new JPanel();
					FlowLayout flowLayout = (FlowLayout) panel.getLayout();
					flowLayout.setAlignment(FlowLayout.RIGHT);
					regPanel.add(panel, BorderLayout.SOUTH);
					{
						final JButton regBtn = new JButton("Register");
						regBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								
								if( tfFilePath.getText().trim().equals("")){
									MessageBox.post(PreOSpecImportDlg.this, "Select a O/Spec File to import", "INFO", MessageBox.INFORMATION);
									return;
								}
								
								
//								InterfaceAIFComponent[] coms = CustomUtil.getTargets();
//								if( coms == null || coms.length == 0 || !( coms[0] instanceof TCComponentBOMLine)){
//									MessageBox.post(PreOSpecImportDlg.this, "Select a Pre-Product item", "INFO", MessageBox.INFORMATION);
//									return;
//								}

//								final TCComponentBOMLine selectedLine = (TCComponentBOMLine)coms[0];
								final TCComponentBOMLine selectedLine = (TCComponentBOMLine)targets[0];

//								try {
//                                    if (! CustomUtil.isReleased(selectedLine.getItemRevision()))
//                                    {
//                                        TCComponent ccn = selectedLine.getItemRevision().getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
//                                        if (ccn == null)
//                                        {
//                                            MessageBox.post(PreOSpecImportDlg.this, "Selected Pre-Product item has not contained CCN.\n\n = contact to Administrator. =", "INFO", MessageBox.INFORMATION);
//                                            return;
//                                        }
//                                    }
//                                } catch (TCException e1) {
//                                    MessageBox.post(PreOSpecImportDlg.this, e1.getMessage(), "ERROR", MessageBox.ERROR);
//                                    return;
//                                }

//								final WaitProgressBar waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
								final WaitProgressBar waitProgress = new WaitProgressBar(PreOSpecImportDlg.this);
								waitProgress.setAlwaysOnTop(true);
								waitProgress.start();
								
								waitProgress.setStatus("Checked existing Pre O/Spec....");
								final File selectedFile = new File(tfFilePath.getText());
								try{
									OSpec ospec = OpUtil.getOSpec(selectedFile);
									String osiNo = ospec.getOspecNo().substring(0, ospec.getOspecNo().lastIndexOf("-"));
									TCComponentItemRevision revision = CustomUtil.findItemRevision("S7_OspecSetRevision", osiNo, ospec.getVersion());
									if( revision != null ){
//										throw new Exception("The already existing Pre O/Spec.");
										waitProgress.close();
										MessageBox.post(PreOSpecImportDlg.this, "The already existing Pre O/Spec.", "INFO", MessageBox.INFORMATION);
										return;
									}
								} catch(Exception e){
									MessageBox.post(PreOSpecImportDlg.this, e.toString(), "INFO", MessageBox.INFORMATION);
									return;
								}
								
								
								
								final TCSession session = CustomUtil.getTCSession();
								AbstractAIFOperation operation = new AbstractAIFOperation() {
									
									@Override
									public void executeOperation() throws Exception {
//										File selectedFile = new File(tfFilePath.getText());
//										regBtn.setEnabled(false);
										
										boolean isRevised = false;
										TCComponentBOMLine preProductLine = null;
										TCComponentItemRevision ccnRevision = null;
										TCComponentItemRevision newRevision = null;
										try
							            {
											Calendar sDate = Calendar.getInstance();
											SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
											System.out.println("Start : " + sdf.format(sDate.getTime()));
											waitProgress.setStatus("Do not change Perspective");

											if (! (selectedLine.getItemRevision().getProperty(PropertyConstant.ATTR_NAME_ITEMREVID).equals("000") && CustomUtil.isWorkingStatus(selectedLine.getItemRevision())))
											{
    											if( CustomUtil.isReleased(selectedLine.getItemRevision())){
    											    String projCode = selectedLine.getItemRevision().getProperty(PropertyConstant.ATTR_NAME_PROJCODE);
    											    String gateNo = getGateNo(projCode);
    											    String sysCode = "X00";
    											    ccnRevision = createCCNItem(projCode, gateNo, sysCode);

    											    if (! (selectedLine.getItemRevision().getProperty(PropertyConstant.ATTR_NAME_ITEMREVID).equals("000") && CustomUtil.isWorkingStatus(selectedLine.getItemRevision())))
    											    {
        												TCComponentBOMWindowType winType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
        											    TCComponentRevisionRuleType tccomponentrevisionruletype = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
        											    TCComponentBOMWindow window = winType.create(tccomponentrevisionruletype.getDefaultRule());
        												TCComponentItemRevision tRevision = selectedLine.getItemRevision();
        												String nextRevID = CustomUtil.getNextRevID(tRevision.getItem(), "Item");
        												newRevision = tRevision.saveAs(nextRevID, tRevision.getProperty("object_name"), tRevision.getProperty("object_desc"), false, null);
        												preProductLine = window.setWindowTopLine(null, newRevision, null, null);
        												isRevised = true;
    												
        												newRevision.setReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO, ccnRevision);
        												newRevision.save();

        												((TCComponentChangeItemRevision)ccnRevision).add(TypeConstant.CCN_PROBLEM_ITEM, tRevision);
    											    }
    											    else
    											    {
    											        preProductLine = selectedLine;
    											        newRevision = selectedLine.getItem().getLatestItemRevision();
    											    }

    											    ((TCComponentChangeItemRevision) ccnRevision).add(TypeConstant.CCN_SOLUTION_ITEM, newRevision);
    											}else{
    											    ccnRevision = (TCComponentItemRevision) selectedLine.getItemRevision().getReferenceProperty(PropertyConstant.ATTR_NAME_CCNNO);
    												preProductLine = selectedLine;
    											}
    											if (ccnRevision == null)
    											{
    											    throw new NullPointerException("CCN Object is null.");
    											}
    											ccnItemID = ccnRevision.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
											}
											else
											{
											    preProductLine = selectedLine;
											}

											waitProgress.setStatus("Loading Pre O/Spec....");
											OSpec ospec = OpUtil.getOSpec(selectedFile);
											String osiNo = ospec.getOspecNo().substring(0, ospec.getOspecNo().lastIndexOf("-"));
//											TCComponentItemRevision revision = CustomUtil.findItemRevision("S7_OspecSetRevision", osiNo, ospec.getVersion());
//											if( revision != null ){
//												throw new Exception("The already existing Pre O/Spec.");
//											}
											
											register(preProductLine, ospec, selectedFile, waitProgress);
											
//											if( !CustomUtil.isReleased( preProductLine.getItemRevision())){
//												SYMTcUtil.selfRelease(preProductLine.getItemRevision(), "CSR");
//											}
											if (! (selectedLine.getItemRevision().getProperty(PropertyConstant.ATTR_NAME_ITEMREVID).equals("000") && CustomUtil.isWorkingStatus(selectedLine.getItemRevision())))
											{
												// [NoSR][20160325][jclee] Pre OSpec Upload 시 BUG Fix
//    											TCComponentItemRevision prodRevision = selectedLine.getItemRevision();
    											TCComponentItemRevision prodRevision = preProductLine.getItemRevision();
    											prodRevision.setProperty(PropertyConstant.ATTR_NAME_OSPECNO, osiNo + "-" + ospec.getVersion());
    											SYMTcUtil.selfRelease(ccnRevision, "CSR");
											}

											// Trim정보 I/F를 위해 DB에 저장.
											waitProgress.setStatus("Saving Trim Info....");
											saveTrim(ospec);
											
											// [SR150818-030][20150826][jclee] Trim 삭제 추가 여부 Flag 설정 작업 수행 Procedure 호출
											updateOSpecTrimStat(ospec.getOspecNo());
											
											Calendar eDate = Calendar.getInstance();
											System.out.println("End : " + sdf.format(eDate.getTime()));
											
											//End operation을 이쪽으로 이동
											//Revise된 Product가 Ospec 적용을 위해
											afterExecuteOperation(ospec, preProductLine);
							            } catch (Exception e) {
							            	e.printStackTrace();
							            	waitProgress.setStatus(e.getMessage());
							            	
							                if (ccnRevision != null)
							                    ccnRevision.getItem().delete();
							                if (newRevision != null && isRevised) {
							                	if (preProductLine != null && preProductLine.window() != null) {
							                		preProductLine.window().close();
												}
							                	
							                	newRevision.delete();
							                }

							                
							            	waitProgress.setShowButton(true);
							            } finally {
//							            	regBtn.setEnabled(true);
							            	if( isRevised ){
							            		preProductLine.window().close();
							            	}
							            }	
									}
									
									private void afterExecuteOperation(OSpec ospec, TCComponentBOMLine preProductLine){
										
										SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        System.out.println("Start EO : " + sdf.format(Calendar.getInstance().getTime()));

										// 변경후 Product 하위 모든 파트들의 usgae 정보를 읽어 저장한다.
										try {
								            //[20161014][ymjang] 테스트 소스 수정
										    //ccnItemID = "TestCCNNo1";
											//변경 사항 미발생으로 CCN EPL 만들어지지 않으므로 미사용으로 변경 (소요시간 과다)
                                        	//SR190702-031 epl 생성 잠정 보류
//										    selectedLine.refresh();
//										    if (! (selectedLine.getItemRevision().getProperty(PropertyConstant.ATTR_NAME_ITEMREVID).equals("000") && CustomUtil.isWorkingStatus(selectedLine.getItemRevision())))
//										    {
////    										    waitProgress.setStatus("load OSpec info.");
//                                                TCComponentItemRevision ospecRevision = BomUtil.getOSpecRevisionWithCCN(selectedLine.getItemRevision());
//                                                OSpec oldOspec = BomUtil.getOSpec(ospecRevision);
////    										    getProductAllParts(selectedLine, oldOspec, productAllChildPartsList, productAllChildPartsUsageList, false, waitProgress);
//    										    getProductAllParts(preProductLine, ospec, productAllChildPartsList, productAllChildPartsUsageList, false, waitProgress);
//
//    										    System.out.println("End EO : " + sdf.format(Calendar.getInstance().getTime()));
//										    
//    	                                        System.out.println("Start Check : " + sdf.format(Calendar.getInstance().getTime()));
//    										    checkDifferentUsageParts(productAllChildPartsList, productAllChildPartsUsageList, waitProgress);
//                                                System.out.println("End Check : " + sdf.format(Calendar.getInstance().getTime()));
//
//    										    if (productAllChildPartsList != null && productAllChildPartsList.size() > 0 && ccnItemID != null)
//    										    {
//    										        waitProgress.setStatus("differenct usage qty part insert to db.");
//
//    										        CustomCCNDao dao = new CustomCCNDao();
//    									            dao.insertOSpecEplList(ccnItemID, productAllChildPartsList, productAllChildPartsUsageList);
//    									            dao.insertIfOSpecEplList(ccnItemID, productAllChildPartsList, productAllChildPartsUsageList);
//    										    }
//										    }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
//                                        	PreOSpecImportDlg.this.setVisible(true);
                                        	regBtn.setEnabled(true);
    										waitProgress.dispose();
                                        }

										
									}
								};
								operation.addOperationListener(new InterfaceAIFOperationListener() {
									
									@Override
									public void startOperation(String arg0) {
									    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                        System.out.println("Start SO : " + sdf.format(Calendar.getInstance().getTime()));
                                        
                                        regBtn.setEnabled(false);
//                                        PreOSpecImportDlg.this.setVisible(false);

										// 변경전의 Product 하위 모든 파트들의 usage 정보를 읽어 저장한다.
										try {
	                                        TCComponentItemRevision prodRev = selectedLine.getItemRevision();

	                                        if (! (prodRev.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID).equals("000") && CustomUtil.isWorkingStatus(prodRev)))
	                                        {
                                                productAllChildPartsList = new HashMap<>();
                                                productAllChildPartsUsageList = new HashMap<>();
                                                systemCodeMap = new HashMap<>();
                                                
                                                //변경 사항 미발생으로 CCN EPL 만들어지지 않으므로 미사용으로 변경 (소요시간 과다)
	                                        	//SR190702-031 epl 생성 잠정 보류
//                                                List<LovValue> systemCodeLovLists = SDVLOVUtils.getLOVValues(PropertyConstant.ATTR_NAME_SYSTEMCODE);
//                                                for (LovValue value : systemCodeLovLists)
//                                                {
//                                                    String key = value.getValue() == null ? "" : value.getValue().toString();
//                                                    String keyValue = value.getDescription();
//
//                                                    systemCodeMap.put(key, keyValue);
//                                                }

                                                // 최초 Import 시에는 읽을게 없다.
                                                //변경 사항 미발생으로 CCN EPL 만들어지지 않으므로 미사용으로 변경 (소요시간 과다)
	                                        	//SR190702-031 epl 생성 잠정 보류
//    	                                        if (! (prodRev.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID).equals("000") && CustomUtil.isWorkingStatus(prodRev)))
//    	                                        {
//        										    waitProgress.setStatus("load OSpec info.");
//        										    TCComponentItemRevision ospecRevision = BomUtil.getOSpecRevisionWithCCN(selectedLine.getItemRevision());
//        										    OSpec ospec = BomUtil.getOSpec(ospecRevision);
//                                                    getProductAllParts(selectedLine, ospec, productAllChildPartsList, productAllChildPartsUsageList, true, waitProgress);
//    	                                        }
	                                        }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
										System.out.println("End SO : " + sdf.format(Calendar.getInstance().getTime()));
									}
									
									@Override
									public void endOperation() {
//                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                                        System.out.println("Start EO : " + sdf.format(Calendar.getInstance().getTime()));
//
//										// 변경후 Product 하위 모든 파트들의 usgae 정보를 읽어 저장한다.
//										try {
//								            //[20161014][ymjang] 테스트 소스 수정
//										    //ccnItemID = "TestCCNNo1";
//										    selectedLine.refresh();
//										    if (! (selectedLine.getItemRevision().getProperty(PropertyConstant.ATTR_NAME_ITEMREVID).equals("000") && CustomUtil.isWorkingStatus(selectedLine.getItemRevision())))
//										    {
//    										    waitProgress.setStatus("load OSpec info.");
//                                                TCComponentItemRevision ospecRevision = BomUtil.getOSpecRevisionWithCCN(selectedLine.getItemRevision());
//                                                OSpec ospec = BomUtil.getOSpec(ospecRevision);
//    										    getProductAllParts(selectedLine, ospec, productAllChildPartsList, productAllChildPartsUsageList, false, waitProgress);
//
//    										    System.out.println("End EO : " + sdf.format(Calendar.getInstance().getTime()));
//										    
//    	                                        System.out.println("Start Check : " + sdf.format(Calendar.getInstance().getTime()));
//    										    checkDifferentUsageParts(productAllChildPartsList, productAllChildPartsUsageList, waitProgress);
//                                                System.out.println("End Check : " + sdf.format(Calendar.getInstance().getTime()));
//
//    										    if (productAllChildPartsList != null && productAllChildPartsList.size() > 0 && ccnItemID != null)
//    										    {
//    										        waitProgress.setStatus("differenct usage qty part insert to db.");
//
//    										        CustomCCNDao dao = new CustomCCNDao();
//    									            dao.insertOSpecEplList(ccnItemID, productAllChildPartsList, productAllChildPartsUsageList);
//    									            dao.insertIfOSpecEplList(ccnItemID, productAllChildPartsList, productAllChildPartsUsageList);
//    										    }
//										    }
//                                        } catch (Exception e) {
//                                            e.printStackTrace();
//                                        } finally {
////                                        	PreOSpecImportDlg.this.setVisible(true);
//                                        	regBtn.setEnabled(true);
//    										waitProgress.dispose();
//                                        }

                                        
									}
								});
								session.queueOperationLater(operation);
								
					
							}
						});
						panel.add(regBtn);
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				btnDetailView = new JButton("Detail View");
				btnDetailView.setEnabled(false);
				btnDetailView.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						detailView();
					}
				});
				{
					btnCompare = new JButton("Compare");
					btnCompare.setEnabled(false);
					btnCompare.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							try {
								compare();
							} catch (Exception e) {
								e.printStackTrace();
								MessageBox.post(PreOSpecImportDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
							}
						}
					});
					buttonPane.add(btnCompare);
				}
				btnDetailView.setActionCommand("Detail View");
				buttonPane.add(btnDetailView);
				getRootPane().setDefaultButton(btnDetailView);
			}
			{
				JButton btnClose = new JButton("Close");
				btnClose.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ospecRevision = null;
						PreOSpecImportDlg.this.dispose();
					}
				});
				btnClose.setActionCommand("Cancel");
				buttonPane.add(btnClose);
			}
		}
		
		//Combo Box Data Load..
		try {
			refreshGmodel();
			refreshProject();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	protected void checkDifferentUsageParts(HashMap<String, HashMap<String, Object>> allPartsList, HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> allUsagePartMap, WaitProgressBar waitProgress) throws Exception
	{
        try
        {
            boolean isDifferentUsage = false;
            ArrayList<String> sameUsagePartList = new ArrayList<>();
            if (allPartsList == null || allPartsList.size() == 0)
                return;

            waitProgress.setStatus("All Part usage qty check.");

            for (String mapPartKey : allPartsList.keySet())
            {
                HashMap<String, HashMap<String, HashMap<String, Object>>> usageMap = allUsagePartMap.get(mapPartKey);
//                System.out.println("mapPartKey : "+mapPartKey);
                isDifferentUsage = false;

                for (String usageSOSKey : usageMap.keySet())
                {
//                	System.out.println("usageSOSKey : "+usageSOSKey);
                    HashMap<String, HashMap<String, Object>> oldNewMap = usageMap.get(usageSOSKey);

                    HashMap<String, Object> oldUsageValue = oldNewMap.get("OLD");
                    HashMap<String, Object> newUsageValue = oldNewMap.get("NEW");

//                    System.out.println("oldUsageValue : "+oldUsageValue);
//                    System.out.println("newUsageValue : "+newUsageValue);
                    
                    if (oldUsageValue == null || newUsageValue == null) {
                    	if (oldUsageValue == null && newUsageValue == null) {
                    		continue;
                    	} else {
                    		isDifferentUsage = true;
                    		break;
                    	}
						
					}
                    
                    Object oOldUsageValue = oldUsageValue.get("USAGE_QTY");
                    Object oNewUsageValue = newUsageValue.get("USAGE_QTY");
                    
                    if (oOldUsageValue == null || oNewUsageValue == null) {
                    	if (oOldUsageValue == null && oNewUsageValue == null) {
                    		continue;
                    	} else {
                    		isDifferentUsage = true;
                    		break;
                    	}
					}
                    
                    if (! oldUsageValue.get("USAGE_QTY").equals(newUsageValue.get("USAGE_QTY")))
                    {
                        if (! isDifferentUsage)
                        {
                            isDifferentUsage = true;
                        }
                    }
                }

                if (! isDifferentUsage)
                {
                    sameUsagePartList.add(mapPartKey);
                }
            }

            for (String samePartKey : sameUsagePartList)
            {
                allPartsList.remove(samePartKey);
                allUsagePartMap.remove(samePartKey);
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

	protected TCComponentItemRevision createCCNItem(String projCode, String gateNo, String systemCode) throws Exception
	{
	    try
	    {
            HashMap<String, Object> ccnPropMap = new HashMap<String, Object>();
            ccnPropMap.put(PropertyConstant.ATTR_NAME_PROJCODE, projCode);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_GATENO, gateNo);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_BL_BUDGETCODE, systemCode);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_PROJECTTYPE, "02");
            ccnPropMap.put(PropertyConstant.ATTR_NAME_REGULATION, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_COSTDOWN, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_ORDERINGSPEC, true);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_QUALITYIMPROVEMENT, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_CORRECTIONOFEPL, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_STYLINGUPDATE, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_WEIGHTCHANGE, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_MATERIALCOSTCHANGE, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_THEOTHERS, false);
            ccnPropMap.put(PropertyConstant.ATTR_NAME_ITEMDESC, "OSpec Import");

            return SDVPreBOMUtilities.createCCNItem("02", ccnPropMap);
	    }
	    catch (Exception ex)
	    {
	        throw ex;
	    }
	}

	protected void getProductAllParts(TCComponentBOMLine topLine, OSpec ospec, HashMap<String, HashMap<String, Object>> partsList, HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> usageList, boolean isOldType, WaitProgressBar waitBar) throws Exception
	{
	    try
	    {
	        waitBar.setStatus(topLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID) + " BOM Info load.");
	        topLine.refresh();
	        TCComponentItemRevision latestReleasedRevision = SYMTcUtil.getLatestReleasedRevision(topLine.getItem());
	        HashMap<String, StoredOptionSet> revSOSList = getStoredOptionSets(ospec);
	        TCComponentBOMLine prodLine = BomUtil.getBomLine(latestReleasedRevision, "Latest Released_revision_rule");

            // 2. 하위를 찾으면서 SOS를 적용해 해당 BOMLine을 추출한다.
            if (prodLine != null)
            {
                try
                {
                    ArrayList<BOMLineLoader> bomLoaderList = new ArrayList<BOMLineLoader>();

                    AIFComponentContext[] childContexts = prodLine.getChildren();
                    for (AIFComponentContext functionContext : childContexts)
                    {
                        TCComponentBOMLine functionLine = (TCComponentBOMLine) functionContext.getComponent();
                        if (functionLine.getItemRevision() != null)
                        {
                            String func_code = functionLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID).substring(0, 4);
                            
                            //Test
//                            if(!func_code.equals("F010")){
//                            	continue;
//                            }

                            for (AIFComponentContext fmpContext : functionLine.getChildren())
                            {
                                BOMLineLoader bomLoader = new BOMLineLoader(ccnItemID, func_code, (TCComponentBOMLine) fmpContext.getComponent(), ospec, partsList, usageList, revSOSList, isOldType, waitBar);
                                bomLoaderList.add(bomLoader);
                            }
                        }
                    }

                    ExecutorService executor = Executors.newFixedThreadPool(20);
                    for (BOMLineLoader loader : bomLoaderList)
                    {
                        executor.execute(loader);
                    }

                    executor.shutdown();
                    while (!executor.isTerminated())
                    {}
                }
                catch (Exception ex)
                {
                    throw ex;
                }
                finally
                {
                    prodLine.window().close();
                }
            }
	    }
	    catch (Exception ex)
	    {
	        throw ex;
	    }
	}
	
    private HashMap<String, StoredOptionSet> getStoredOptionSets(OSpec ospec) throws Exception
	{
	    try
	    {
	        HashMap<String, StoredOptionSet> optionSetMap = new HashMap<>();;

            ArrayList<OpTrim> trimList = ospec.getTrimList();
            HashMap<String, ArrayList<Option>> trimOptionMap = ospec.getOptions();
            
            for (OpTrim opTrim : trimList)
            {
                ArrayList<Option> options = trimOptionMap.get(opTrim.getTrim());
                String stdName = opTrim.getTrim() + "_STD";
                String optName = opTrim.getTrim() + "_OPT";
                StoredOptionSet stdSos = new StoredOptionSet(stdName);
                stdSos.add("TRIM", stdName);
                StoredOptionSet optSos = new StoredOptionSet(optName);
                optSos.add("TRIM", optName);
                
                for( Option option : options){
                    if( option.getValue().equalsIgnoreCase("S")){
                        stdSos.add(option.getOp(), option.getOpValue());
                        optSos.add(option.getOp(), option.getOpValue());
                    }else if( !option.getValue().equalsIgnoreCase("-") ){
                        optSos.add(option.getOp(), option.getOpValue());
                    }
                }
                
                optionSetMap.put(stdName, stdSos);
                optionSetMap.put(optName, optSos);
            }

	        return optionSetMap;
	    }
	    catch (Exception ex)
	    {
	        throw ex;
	    }
    }

    private void saveTrim(OSpec ospec) throws Exception{
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("OSPEC_NO", ospec.getOspecNo());
			
			ArrayList<HashMap<String, String>> trims = new ArrayList<HashMap<String, String>>();
			ArrayList<OpTrim> list = ospec.getTrimList();
			int iSeq = 0;
			for( OpTrim trim : list){
				Option transmissionOption = getStandardOption(ospec, trim.getTrim(), "E00");
				Option transferCaseOption = getStandardOption(ospec, trim.getTrim(), "E10");
				
				HashMap<String, String> map = new HashMap<String, String>();
				map.put("TRIM_SEQ", String.valueOf(iSeq));
				map.put("AREA", trim.getArea());
				map.put("PASSENGER", trim.getPassenger());
				map.put("ENGINE", trim.getEngine());
				map.put("GRADE", trim.getGrade());
				map.put("TRIM", trim.getTrim());
				map.put("E00", transmissionOption == null ? "." : transmissionOption.getOpValue());
				map.put("E00_DESC", transmissionOption == null ? "." : transmissionOption.getOpValueName());
				map.put("E10", transferCaseOption == null ? "." : transferCaseOption.getOpValue());
				map.put("E10_DESC", transferCaseOption == null ? "." : transferCaseOption.getOpValueName());
				trims.add(map);
				
				iSeq++;
			}
			ds.put("DATA", trims);
			remote.execute("com.kgm.service.PreOSpecService", "insertTrim", ds);
		} catch (Exception e) {
			throw e;
		}
	}
	
	private Option getStandardOption(OSpec ospec, String trim, String optionCategory){
		HashMap<String, ArrayList<Option>> optionMap = ospec.getOptions();
		ArrayList<Option> optionList = optionMap.get(trim);
		
		for( Option option : optionList){
			if( optionCategory.equals( option.getOp()) && option.getValue().equalsIgnoreCase("S")){
				return option;
			}
		}
		
		return null;
	}
	
	private void register(TCComponentBOMLine productBomLine, OSpec ospec, File selectedFile, final WaitProgressBar waitProgress) throws Exception{
		
		// 1.첨부되어 있는 SOS 삭제.
		TCComponentItemRevision revision = productBomLine.getItemRevision();
		
		waitProgress.setStatus("Deleting previous Stored Option Set....");
		AIFComponentContext[] context =  revision.getChildren("IMAN_reference");
		for( int j = 0; context != null && j < context.length; j++){
			TCComponent com =  (TCComponent)context[j].getComponent();
			String comType = com.getType();
			if( comType.equals("StoredOptionSet")){
				try{
					revision.remove("IMAN_reference", com);
//					com.delete();
				}catch( TCException tce){
					revision.getSession().getUser().getNewStuffFolder().add("contents", com);
					throw tce;
				}
			}
		}
		
		// 2. Project으로 Product를 검색.
//		waitProgress.setStatus("Searching Product....");
//		TCComponent[] tcComponent = CustomUtil.queryComponent("__SYMC_S7_ProductRevision", new String[]{"Project Code"}, new String[]{ospec.getProject()});
//		if( tcComponent == null || tcComponent.length == 0){
//			Registry registry = Registry.getRegistry(PreOSpecImportDlg.class);
//			waitProgress.setStatus(registry.getString("PRE_OSPEC.IMP.COULD_NOT_FIND_PRODUCT"));
//			waitProgress.setShowButton(true);
////			MessageBox.post(PreOSpecImportDlg.this, registry.getString("PRE_OSPEC.IMP.COULD_NOT_FIND_PRODUCT"), "ERROR", MessageBox.ERROR);
//			return;
//		}
		
		
//		for( int i = 0; i < tcComponent.length; i++){
//			String type = tcComponent[i].getType();
//			if( type.equals("S7_PreProductRevision")){
//				TCComponentItemRevision rev = (TCComponentItemRevision)tcComponent[i];
//				revision = rev.getItem().getLatestItemRevision();
//			}
//		}
//		
//		if( revision == null){
//			Registry registry = Registry.getRegistry(PreOSpecImportDlg.class);
//			waitProgress.setStatus(registry.getString("PRE_OSPEC.IMP.COULD_NOT_FIND_PRODUCT"));
//			waitProgress.setShowButton(true);
//			return;
//		}
		// 2-1. Revision이 Release되어 있으면 Revise 필요.
		// SOS가 붙어 있으므로, Save AS 로 처리.
		
		
		//3. Corp Library에 모두 존재하는 옵션인지 확인.
		TCSession session = productBomLine.getSession();
		OptionManager optionManager = null;
		try{
			TCVariantService tcVariantservice = session.getVariantService();
			
//			productBomLine = CustomUtil.getBomline(revision, session);
			optionManager = new OptionManager(productBomLine, true);
			HashMap<String, VariantOption> corpOptionMap = optionManager.getCorpOptionMap();
			
			Vector<VariantValue> optionData = new Vector<VariantValue>();
			
			waitProgress.setStatus("Getting Options....");
//			ArrayList<VariantOption> variantOptionList = null;
//			try{
//				variantOptionList = optionManager.getOptionSet(productBomLine, null, null, null, true, false);;
//			}catch( Exception e){
//				//Product에 옵션이 존재하지 않을 경우 에러 발생. 무시함.
//				e.printStackTrace();
//			}
			HashMap<String, HashMap<String, OpCategory>> options = ospec.getCategory();
			final HashMap<String, HashMap<String, String>> standardVariantOptions = new HashMap<String, HashMap<String, String>>();
			final HashMap<String, HashMap<String, String>> optionVariantOptions = new HashMap<String, HashMap<String, String>>();
			
			String itemID = productBomLine.getItem().getProperty("item_id");
			VariantOption trimVariantOption = new VariantOption(null, itemID, "TRIM", "TRIM");
			
			//Trim NONE Option을 생성하기위해 .
			VariantValue trimVariantValue = new VariantValue(trimVariantOption, "NONE", "NONE", VariantValue.VALUE_USE, true);
			trimVariantOption.addValue(trimVariantValue);
			optionData.add(trimVariantValue);
			
			// Product에 옵션값과 Info(TC_MESSAGE)정보를 셋팅하기 위함.
			ArrayList<OpTrim> trimList = ospec.getTrimList();
			for( OpTrim trim : trimList){
				
				//Trim Option을 생성하기위해 .
				//Trim_STD
				trimVariantValue = new VariantValue(trimVariantOption, trim.getTrim() + "_STD", trim.getTrim() + "_STD", VariantValue.VALUE_USE, true);
				trimVariantOption.addValue(trimVariantValue);
				optionData.add(trimVariantValue);
				//Trim_OPT
				trimVariantValue = new VariantValue(trimVariantOption, trim.getTrim() + "_OPT", trim.getTrim() + "_OPT", VariantValue.VALUE_USE, true);
				trimVariantOption.addValue(trimVariantValue);
				optionData.add(trimVariantValue);
				
				// OSpec의 옵션.
				HashMap<String, OpCategory> categories = (HashMap<String, OpCategory>)options.get(trim.getTrim());
				
				String[] corpOptionKey = corpOptionMap.keySet().toArray(new String[corpOptionMap.size()]);
				for( int i = 0; corpOptionKey != null && i < corpOptionKey.length; i++){
//					if(corpOptionKey[i].equals("S25")){
//						System.out.println("aaaaaaaaaaaaaaa");
//						
//					}
					if( categories.containsKey(corpOptionKey[i])){
						
						OpCategory opCategory = categories.get(corpOptionKey[i]);
						ArrayList<Option> opOptionList = opCategory.getOpValueList();
						
						VariantOption corpVariantOption = corpOptionMap.get(corpOptionKey[i]);
						List<VariantValue> corpVariantValueList = corpVariantOption.getValues();
						for( int j = 0; j < corpVariantValueList.size(); j++){
							VariantValue variantValue = corpVariantValueList.get(j);
							
							boolean bFound = false;
							for( Option option : opOptionList){
								
								// O/Spec에 해당 옵션이 존재함.
//								if(variantValue.getValueName().equals("S25X")){
//									System.out.println("bbbbbbbbbbbbb");
//								}
								if( variantValue.getValueName().equals(option.getOpValue())){
									VariantValue variantValueClone = new VariantValue(corpVariantOption, variantValue.getValueName(), variantValue.getValueDesc(), VariantValue.VALUE_USE, true);
									// VariantValue.VALUE_USE가 우선이며, 이전에 저장된 내용이 존재할시 삭제후 다시 입력.
									optionData.removeElement(variantValueClone);
									optionData.add(variantValueClone);
									bFound = true;
									break;
								}
							}
							
							// O/Spec에 존재하지 않을 경우, 사용안함으로 표기.
							if( !bFound){
								VariantValue variantValueClone = new VariantValue(corpVariantOption, variantValue.getValueName(), variantValue.getValueDesc(), VariantValue.VALUE_NOT_USE, false);
								if( !optionData.contains(variantValueClone)){
									optionData.add(variantValueClone);
								}
							}
						}
						
						for( Option option : opCategory.getOpValueList()){
							
							if( option.getValue().equalsIgnoreCase("S")){
								HashMap<String, String> curCategories = standardVariantOptions.get(trim.getTrim());
								if( curCategories == null){
									curCategories = new HashMap<String, String>();
									standardVariantOptions.put(trim.getTrim(), curCategories);
								}
								
								if( !curCategories.containsKey(option.getOp())){
									curCategories.put(option.getOp(), option.getOpValue());
								}
							}else if( option.getValue().equalsIgnoreCase("O")){
								HashMap<String, String> curCategories = optionVariantOptions.get(trim.getTrim());
								if( curCategories == null){
									curCategories = new HashMap<String, String>();
									optionVariantOptions.put(trim.getTrim(), curCategories);
								}
								
								if( !curCategories.containsKey(option.getOp())){
									curCategories.put(option.getOp(), option.getOpValue());
								}
							}
						}
					}
				}
			}
			
			//3-0. Product에 설정된 옵션 제거
			waitProgress.setStatus("Clearing Option....");
			tcVariantservice.setLineMvl(productBomLine, "");
			productBomLine.window().save();
			productBomLine.window().refresh();
//			if( variantOptionList != null){
//				for( VariantOption variantOption : variantOptionList){
//					tcVariantservice.lineDeleteOption(productBomLine, variantOption.getOveOption().id);
//				}
//			}
//			productBomLine.window().save();
//			productBomLine.window().refresh();
			
			//3-1. Product에 옵션 설정
			waitProgress.setStatus("Applying Option....");
//			BomUtil.apply(optionData, new Vector<String[]>(), new ArrayList<VariantOption>(), new ArrayList<String[]>(), productBomLine, waitProgress, false);
			BomUtil.apply(optionManager, optionData, new Vector<String[]>(), new ArrayList<VariantOption>(), new ArrayList<String[]>(), productBomLine, waitProgress, false);
			
			//4. SOS 생성 및 Product Revision에 연결.
			waitProgress.setStatus("Creating SOS....");
			ExecutorService executor = Executors.newFixedThreadPool(100);
			final TCComponentItemRevision topRevision = revision;
			for( final OpTrim trim : trimList){
				Thread t = new Thread(){
					public void run(){
						try {
							HashMap<String, String> stdMap = standardVariantOptions.get(trim.getTrim());
							
							//트림 옵션 추가.
							stdMap.put("TRIM", trim.getTrim() + "_STD");
							if( stdMap != null && !stdMap.isEmpty()){
								waitProgress.setStatus("Creating Stored Option Set " + trim.getTrim() + "_STD....");
								createStoredOptionSet(topRevision, stdMap, trim.getTrim() + "_STD", "Standard Set");
							}
							
							HashMap<String, String> optMap = optionVariantOptions.get(trim.getTrim());
							//트림 옵션 추가.
							optMap.put("TRIM", trim.getTrim() + "_OPT");
							if( optMap != null && !optMap.isEmpty()){
								waitProgress.setStatus("Creating Stored Option Set " + trim.getTrim() + "_OPT....");
								createStoredOptionSet(topRevision, optMap, trim.getTrim() + "_OPT", "Option Set");
							}
							
							System.gc();
						} catch (TCException e) {
							e.printStackTrace();
							waitProgress.setStatus("[" + trim.getTrim() + "] " + e.getError());
						}
					}
				};
				executor.execute(t);
			}
			
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			
//			for( OpTrim trim : trimList){
//				waitProgress.setStatus("Creating Stored Option Set " + trim.getTrim() + "_STD....");
//				createStoredOptionSetWithBOMLine(productBomLine, standardVariantOptions.get(trim.getTrim()), trim.getTrim() + "_STD");
//				waitProgress.setStatus("Creating Stored Option Set " + trim.getTrim() + "_OPT....");
//				createStoredOptionSetWithBOMLine(productBomLine, optionVariantOptions.get(trim.getTrim()), trim.getTrim() + "_OPT");
//			}
			
			waitProgress.setStatus("Saving to Teamcenter....");
			importOspecToTC(ospec, selectedFile, revision);
		}catch(Exception e){
			throw e;
		}finally{
			if( productBomLine != null){
//				productBomLine.window().close();
			}
			
			optionManager.clear(false);
			
			refreshGmodel();
			refreshProject();
			tabbedPane.setSelectedComponent(searchPanel);
//			waitProgress.dispose();
		}
	}
	
	private void detailView(){
		if( table.getSelectedRow() > -1){
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			int rowIdx = table.convertRowIndexToModel(table.getSelectedRow());
			ospecRevision = (TCComponentItemRevision)model.getValueAt(rowIdx, 4);
			
			try {
				OSpecTable ospecTable = new OSpecTable(getOSpec(ospecRevision), null, true, false);
				PreOSpecViewDlg viewDlg = new PreOSpecViewDlg("Pre O/Spec Detail View", ospecTable);
				viewDlg.setModal(true);
				viewDlg.setVisible(true);
			} catch (Exception e1) {
				MessageBox.post(this, e1.getMessage(), "ERROR", MessageBox.ERROR);
			}
		}
	}
	
	private ArrayList<String> getGModelList() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		try {
			ds.put("NO-PARAM", null);
			@SuppressWarnings("unchecked")
            ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.kgm.service.OSpecService", "getGModel", ds);
			return gModelList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void refreshGmodel() throws Exception{
		
		try {
			for( int i = cbGmodel.getModel().getSize() - 1; i >= 0; i--){
				cbGmodel.removeItemAt(i);
			}
			
			cbGmodel.addItem(OpUtil.SELECT_G_MODEL);
			ArrayList<String> gModelList = getGModelList();
			for( int i = 0; gModelList!=null && i < gModelList.size(); i++){
				cbGmodel.addItem(gModelList.get(i));
			}
		} catch (Exception e) {
			throw e;
		}
		
	}	
	
	private ArrayList<String> getProjectList() throws Exception{
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		if( cbGmodel.getSelectedIndex() > -1){
			String gModel = (String)cbGmodel.getSelectedItem();
			ds.put("G-MODEL", gModel);
		}
		
		try {
			@SuppressWarnings("unchecked")
            ArrayList<String> gModelList = (ArrayList<String>)remote.execute("com.kgm.service.OSpecService", "getProject", ds);
			return gModelList;
		} catch (Exception e) {
			throw e;
		}
	}
	
	private void refreshProject() throws Exception{
		
		try {
			for( int i = cbProject.getModel().getSize() - 1; i >= 0; i--){
				cbProject.removeItemAt(i);
			}
			
			cbProject.addItem(OpUtil.SELECT_PROJECT);
			ArrayList<String> gModelList = getProjectList();
			for( int i = 0; gModelList!=null && i < gModelList.size(); i++){
				cbProject.addItem(gModelList.get(i));
			}
		} catch (Exception e) {
			throw e;
		}
		
	}		
	
	public ArrayList<HashMap<String, String>> refreshTable() throws Exception{
		
		String gModel = cbGmodel.getSelectedItem().toString();
		if( gModel.equals(OpUtil.SELECT_G_MODEL)){
			throw new Exception(OpUtil.SELECT_G_MODEL);
		}
		String project = cbProject.getSelectedItem().toString();
		if( project.equals(OpUtil.SELECT_PROJECT)){
			throw new Exception(OpUtil.SELECT_PROJECT);
		}
		
		String released_date = null;
		Date date = dateChooser.getDate();
		SimpleDateFormat sdf  = new SimpleDateFormat("dd-MMM-yyyy HH:mm");
		if( date != null ){
			released_date = sdf.format(dateChooser.getDate());
		}
		
		TCComponentItemRevision[] revisions =  getOspecRevision(gModel, project, "", released_date);
		
		try {
			
			DefaultTableModel model = (DefaultTableModel)table.getModel();
			for (int i = model.getRowCount() - 1; i >= 0; i--) {
				model.removeRow(i);
			}
			
			HashMap<String, TCComponentItemRevision> revisionMap = new HashMap<String, TCComponentItemRevision>();
			for( int i = 0; revisions != null && i < revisions.length; i++){
				String itemId = revisions[i].getProperty("item_id");
				String revId = revisions[i].getProperty("item_revision_id");
				revisionMap.put(itemId + "/" + revId, revisions[i]);
			}
			String[] keys = revisionMap.keySet().toArray(new String[revisionMap.size()]);
			Arrays.sort(keys);
			
			sdf  = new SimpleDateFormat("yyyy-MM-dd");
			for( int i = 0; keys != null && i < keys.length; i++){
				TCComponentItemRevision revision = revisionMap.get(keys[i]);
				String itemId = revision.getProperty("item_id");
				String revId = revision.getProperty("item_revision_id");
				gModel = revision.getItem().getProperty("s7_Gmodel");
				project = revision.getItem().getProperty("s7_Project");
				TCProperty tcProp = revision.getTCProperty("s7_OspecReleasedDate");
				Date releasedDate = tcProp.getDateValue();
				
				String dateStr = sdf.format(releasedDate);
				
				Vector<Serializable> row = new Vector<Serializable>();
				row.add(gModel);
				row.add(project);
				row.add(dateStr);
				row.add(itemId + "-" + revId);
				row.add(revision);
				model.addRow(row);
			}
			
			TableColumnModel cm = table.getColumnModel();
			if( cm.getColumnCount() > 4){
				cm.removeColumn(cm.getColumn(4));
			}
		} catch (Exception e) {
			throw e;
		}		
		return null;
	}
	
	public int importOspecToTC(OSpec ospec, File file, TCComponentItemRevision prodRevision) throws Exception{
		String osiNo = ospec.getOspecNo().substring(0, ospec.getOspecNo().lastIndexOf("-"));
		
		TCComponentItemRevision latestRevision = null, revision = null;
		try{
			revision = CustomUtil.findItemRevision("S7_OspecSetRevision", osiNo, ospec.getVersion());
			if( revision != null ){
				return ALREADY_EXIST_ITEM_REVISION;
			}
			
			TCComponentItem item = CustomUtil.findItem("S7_OspecSet", osiNo);
			if( item == null ){
				item = CustomUtil.createItem("S7_OspecSet", osiNo, ospec.getVersion(), osiNo, osiNo);
				item.setProperties(new String[]{"s7_Gmodel","s7_Project"}, new String[]{ospec.getgModel(), ospec.getProject()});
				revision = item.getLatestItemRevision();
			}else{
				latestRevision = item.getLatestItemRevision();
				revision = latestRevision.saveAs(ospec.getVersion());
			}
			
			TCProperty tcproperty = revision.getTCProperty("s7_OspecReleasedDate");
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
			tcproperty.setDateValue(sdf.parse(ospec.getReleasedDate() + " 00:00:00"));
			
			revision.lock();
			revision.setTCProperty(tcproperty);
			revision.save();
			revision.unlock();
			
			if( latestRevision != null ){
				AIFComponentContext[] context = revision.getChildren(SYMCECConstant.ITEM_DATASET_REL);
				for( int i = 0; context != null && i < context.length; i++){
					TCComponentDataset ds = (TCComponentDataset)context[i].getComponent();
					String name = ds.getProperty("object_name");
					if( (latestRevision.getProperty("item_id") + "-" + latestRevision.getProperty("item_revision_id")).equals(name)){
						revision.remove(SYMCECConstant.ITEM_DATASET_REL, ds);
					}
				}
			}
			TCComponentDatasetType datasetType = (TCComponentDatasetType) revision.getSession().getTypeComponent("Dataset");
			TCComponentDataset dataset = datasetType.create(ospec.getOspecNo(), "", "MSExcel");
			dataset.setFiles(new String[] { file.getAbsolutePath() }, new String[] { "MSExcel" },
					new String[] { "Plain" }, new String[] { "excel" });
			
			revision.lock();
			revision.add(SYMCECConstant.ITEM_DATASET_REL, dataset);
			revision.unlock();
			
			prodRevision.setProperty(PropertyConstant.ATTR_NAME_OSPECNO, revision.getItem().getProperty("item_id") + "-" + revision.getProperty("item_revision_id"));
		}catch(Exception e){
			throw e;
		}finally{
//			if( revision != null ){
//				revision.unlock();
//			}
		}
		
		return CREATION_SUCCESS;
	}
	
	public TCComponentItemRevision getSelectedOSpec() {
		return ospecRevision;
	}

	public static TCComponentItemRevision[] getOspecRevision(String gModel, String project, String version, String dateStr) throws Exception{
		HashMap<String, String> param = new HashMap<String, String>();
		if( gModel != null && !gModel.equals("")){
			param.put("Gmodel", gModel);
		}
		
		if( project != null && !project.equals("") ){
			param.put("Project", project);
		}
		
		if( version != null && !version.equals("")){
			param.put("Revision", version);
		}
		
		if( dateStr != null && !dateStr.equals("")){
			param.put("Ospec_Released_Date", dateStr);
		}
		
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> values = new ArrayList<String>();
		Set<String> keys = param.keySet();
		for( String name : keys){
			String value = param.get(name);
			names.add(name);
			values.add(value);
		}
		TCComponent[] coms = CustomUtil.queryComponent("SYMC_Search_OspecSet_Revision", (String[])names.toArray(new String[names.size()]), (String[])values.toArray( new String[values.size()]));
		
		TCComponentItemRevision[] revisions = null;
		if( coms != null && coms.length > 0){
			revisions = new TCComponentItemRevision[coms.length];
			System.arraycopy(coms, 0, revisions, 0, coms.length);
		}
		return revisions;
	}
	
	public OSpec getOSpec(TCComponentItemRevision ospecRev) throws Exception{
		
		String ospecStr = ospecRev.getProperty("item_id") + "-" + ospecRev.getProperty("item_revision_id");
		OSpec ospec = null;
		
		AIFComponentContext[] context = ospecRev.getChildren(SYMCECConstant.ITEM_DATASET_REL);
		for( int i = 0; context != null && i < context.length; i++){
			TCComponentDataset ds = (TCComponentDataset)context[i].getComponent();
			if( ospecStr.equals(ds.getProperty("object_name"))){
				File[] files = DatasetService.getFiles(ds);
				ospec = OpUtil.getOSpec(files[0]);
				break;
			};
		}
		
		return ospec;
	}
	
	@SuppressWarnings("rawtypes")
    private void compare() throws Exception{
		if( comparableDlg != null){
			comparableDlg.dispose();
		}
		
		HashMap<String, HashMap<String, ArrayList<OpGroup>>> opGroupMap = new HashMap<String, HashMap<String, ArrayList<OpGroup>>>();
		
		int[] idx = table.getSelectedRows();
		if( idx == null || idx.length != 2) return;
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();
		int srcModelIdx = table.convertRowIndexToModel(idx[0]);
		int targetModelIdx = table.convertRowIndexToModel(idx[1]);
		TCComponentItemRevision srcRevision = (TCComponentItemRevision)model.getValueAt(srcModelIdx, 4);
		TCComponentItemRevision targetRevision = (TCComponentItemRevision)model.getValueAt(targetModelIdx, 4);
		OSpec sourceOspec = getOSpec(srcRevision);
		OSpec targetOspec = getOSpec(targetRevision);
		try {
			OSpecTable sourceOSpecTable = new OSpecTable(sourceOspec, null);
			OSpecTable targetOSpecTable = new OSpecTable(targetOspec, null);
			
			//차후에 Vector를 Clone하므로 데이타 Clone가능하도록 clone OverRide함.
			Vector<Vector> onlySourceData = sourceOSpecTable.minus(targetOSpecTable.getData(), false);
			Vector<Vector> onlyTargetData = targetOSpecTable.minus(sourceOSpecTable.getData(), false);
			
			OSpecTable onlySourceOSpecTable = new OSpecTable(sourceOspec, onlySourceData);
			OSpecTable onlyTargetOSpecTable = new OSpecTable(targetOspec, onlyTargetData);
			
			comparableDlg = new PreOSpecCompareDlg(sourceOSpecTable, targetOSpecTable, onlySourceOSpecTable, onlyTargetOSpecTable
					, opGroupMap);
			comparableDlg.setModal(true);
			comparableDlg.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(this, e.getMessage(), "ERROR", MessageBox.ERROR);
		}
	}	
	
	/**
	 * Stored Option Set을 생성하고 저장함.
	 * @param line
	 * @throws TCException
	 */
	private void createStoredOptionSet(TCComponentItemRevision tRevision, HashMap<String, String> variantOptions, String sosName, String desc) throws TCException{
//		IMAN_reference
		String variantId = tRevision.getItem().getProperty("item_id").toUpperCase();
		variantId = variantId.substring(1, 6).toUpperCase();
//		TCComponentItemRevision tRevision = line.getItemRevision();
//		AIFComponentContext[] context =  tRevision.getChildren("IMAN_reference");
//		for( int j = 0; context != null && j < context.length; j++){
//			TCComponent com =  (TCComponent)context[j].getComponent();
//			String comType = com.getType();
//			if( comType.equals("StoredOptionSet")){
//				String comName = com.getProperty("object_name");
//				if( comName.equals(sosName)){
//					try{
//						tRevision.remove("IMAN_reference", com);
//						com.delete();
//					}catch( TCException tce){
////						BWVariantOptionImpDialog.super.syncItemState(treeItemMap.get(variantId), BWVariantOptionImpDialog.super.STATUS_WARNING, "참조중이므로 삭제 할 수 없습니다. NewStuff로 이동합니다.");
////				    	BWVariantOptionImpDialog.super.syncSetItemText(treeItemMap.get(variantId), 4, "참조중이므로 삭제 할 수 없습니다. NewStuff로 이동합니다.");
//						tRevision.getSession().getUser().getNewStuffFolder().add("contents", com);
//					}
//				}
//			}
//		}
		
		TCComponentBOMWindow window = null;
		SelectedOptionSetDialog sosDlg = null;
		try{
			TCSession session = tRevision.getSession();
			TCComponentBOMWindowType winType = (TCComponentBOMWindowType) session.getTypeComponent("BOMWindow");
		    TCComponentRevisionRuleType tccomponentrevisionruletype = (TCComponentRevisionRuleType) session.getTypeComponent("RevisionRule");
			window = winType.create(tccomponentrevisionruletype.getDefaultRule());
			TCComponentBOMLine newTopLine = window.setWindowTopLine(null, tRevision, null, null);
			sosDlg = new SelectedOptionSetDialog(AIFUtility.getActiveDesktop(), AIFUtility.getCurrentApplication(), newTopLine);
			sosDlg.setValue(newTopLine, variantOptions);
			
			TCVariantService variantService = newTopLine.getSession().getVariantService();
			TCComponent sosComponent = variantService.getSos(newTopLine);
			
			//TCComponentVariantRule parentVariantRule = newTopLine.window().askVariantRule();
			TCComponentVariantRule parentVariantRule = null;
			List<TCComponentVariantRule> rules = newTopLine.window().askVariantRules();
			if(rules != null && rules.size() > 0) {
				parentVariantRule = rules.get(0);
			}
			
			if(parentVariantRule != null) {
				TCComponentVariantRule legacyVariantRule = parentVariantRule.copy();
				TCComponent tccomponent = variantService.createVariantConfig(
						legacyVariantRule, new TCComponent[] {  sosComponent });
				TCComponent tccomponent1;
				try {
					tccomponent1 = variantService.writeStoredConfiguration(sosName, tccomponent);
					tccomponent1.setStringProperty("object_desc", desc);
//				tccomponent1.setProperty("s7_BUILDSPEC", "Y");
					tccomponent1.setProperty("s7_PROJECT_CODE", tRevision.getProperty("s7_PROJECT_CODE"));
					tccomponent1.save();
				} catch (TCException tcexception) {
					variantService.deleteVariantConfig(tccomponent);
					throw tcexception;
				}
				variantService.deleteVariantConfig(tccomponent);
				TCComponentItemRevision tccomponentitemrevision = newTopLine.getItemRevision();
				tccomponentitemrevision.add("IMAN_reference", tccomponent1);				
			}		
		}catch(Exception e){
			e.printStackTrace();
			throw e;
		}finally{
			window.close();
			window = null;
			if( sosDlg != null){
				sosDlg.dispose();
				sosDlg = null;
			}
		}
	}

	/**
	 * 
	 * @author jinil
	 *
	 */
    class BOMLineLoader implements Runnable
    {
        private String functionCode;
        private TCComponentBOMLine fmpLine;
        private OSpec ospec;
        private HashMap<String, StoredOptionSet> usageOptionSetList;
        private HashMap<String, HashMap<String, Object>> allPartsList;
        private HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> allUsageList;
        private boolean isOldType;
        private WaitProgressBar waitBar;
        private String ccnID;

        public BOMLineLoader(String ccnID, String funcCode, TCComponentBOMLine fmpLine, OSpec ospec, HashMap<String, HashMap<String, Object>> partsList, HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> usageList, HashMap<String, StoredOptionSet> sosList, boolean bTypeOldNew, WaitProgressBar waitProgressBar)
        {
            this.ccnID = ccnID;
            this.functionCode = funcCode;
            this.fmpLine = fmpLine;
            this.ospec = ospec;
            this.allPartsList = partsList;
            this.allUsageList = usageList;
            this.usageOptionSetList = sosList;
            this.isOldType = bTypeOldNew;
            this.waitBar = waitProgressBar;
        }

        @Override
        public void run()
        {
            try
            {
                String itemId = fmpLine.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
                waitBar.setStatus("Loading " + itemId + " BOM Info.");

                getChildBOMLineWithSOS(ccnID, functionCode, fmpLine, ospec, usageOptionSetList, allPartsList, allUsageList, isOldType, waitBar);
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
        }
    }

    protected void getChildBOMLineWithSOS(String ccnId, String funcCode, TCComponentBOMLine fmpLine, OSpec ospec, HashMap<String, StoredOptionSet> usageOptionSetList, HashMap<String, HashMap<String, Object>> allPartsList, HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> allUsageList, boolean isOldType, WaitProgressBar waitBar) throws Exception
    {
        try
        {
            if (fmpLine.getItemRevision() == null)
                return;

            AIFComponentContext []childLines = fmpLine.getChildren();

            if (childLines != null && childLines.length > 0)
                getAllChildrenList(childLines, funcCode, ccnId, "", ospec, usageOptionSetList, isOldType, allPartsList, allUsageList, waitBar);
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private void getAllChildrenList(AIFComponentContext[] children, String funcCode, String ccnId, String parentCondition, OSpec ospec, HashMap<String,StoredOptionSet> usageOptionSetList, boolean isOldType, HashMap<String, HashMap<String, Object>> allPartsList, HashMap<String, HashMap<String, HashMap<String, HashMap<String, Object>>>> allUsageList, WaitProgressBar waitBar) throws Exception
    {
        try
        {
            if (children == null)
                return;

            for (AIFComponentContext child : children)
            {
                if (! waitBar.isShowing())
                    return;

                TCComponentBOMLine childBOMLine = (TCComponentBOMLine) child.getComponent();
                if (childBOMLine.getItemRevision() == null)
                    continue;

                TCComponentItemRevision childRevision = childBOMLine.getItemRevision();

                String[] parentValues = childBOMLine.parent().getItemRevision().getProperties(parentRevProperties);
                String[] propValues = childRevision.getProperties(revisionProperties);
                String[] bomValues = childBOMLine.getProperties(bomlineProperties);
                String parentNo = childBOMLine.parent().getItemRevision().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
                String findNo = bomValues[1];
                String childNo = propValues[36];
                String mapKey = funcCode + parentNo + findNo + childNo;
                String curCondition = bomValues[5];
                String thisCondition;

                if (parentCondition == null || parentCondition.equals(""))
                    thisCondition = curCondition;
                else
                    thisCondition = "(" + parentCondition + ") AND (" + curCondition + ")";


                if (isOldType)
                {
                    if (! allPartsList.containsKey(mapKey))
                    {
                        allPartsList.put(mapKey, getPartValue(ccnId, funcCode, childBOMLine, parentValues, propValues, bomValues, ospec, usageOptionSetList, null, isOldType, PropertyConstant.CONST_CCN_CHG_TYPE_CUT));
                        allUsageList.put(mapKey, getUsageValue(childBOMLine, thisCondition, bomValues[4], bomValues[16], ospec, usageOptionSetList, null, isOldType));
                    }
                    else
                    {
                        allUsageList.put(mapKey, getUsageValue(childBOMLine, thisCondition, bomValues[4], bomValues[16], ospec, usageOptionSetList, allUsageList.get(mapKey), isOldType));
                    }
                }
                else
                {
                    if (allPartsList.containsKey(mapKey))
                    {
                        allPartsList.put(mapKey, getPartValue(ccnId, funcCode, childBOMLine, parentValues, propValues, bomValues, ospec, usageOptionSetList, allPartsList.get(mapKey), isOldType, PropertyConstant.CONST_CCN_CHG_TYPE_CHANGE));
                        allUsageList.put(mapKey, getUsageValue(childBOMLine, thisCondition, bomValues[4], bomValues[16], ospec, usageOptionSetList, allUsageList.get(mapKey), isOldType));
                    }
                    else
                    {
                        allPartsList.put(mapKey, getPartValue(ccnId, funcCode, childBOMLine, parentValues, propValues, bomValues, ospec, usageOptionSetList, null, isOldType, PropertyConstant.CONST_CCN_CHG_TYPE_ADD));
                        allUsageList.put(mapKey, getUsageValue(childBOMLine, thisCondition, bomValues[4], bomValues[16], ospec, usageOptionSetList, null, isOldType));
                    }
                }

                if (childBOMLine.hasChildren())
                {
                    getAllChildrenList(childBOMLine.getChildren(), funcCode, ccnId, thisCondition, ospec, usageOptionSetList, isOldType, allPartsList, allUsageList, waitBar);
                }
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private HashMap<String, HashMap<String, HashMap<String, Object>>> getUsageValue(TCComponentBOMLine childBOMLine, String varintCondition, String lineQty, String systemRowKey, OSpec ospec, HashMap<String, StoredOptionSet> usageOptionSetList, HashMap<String, HashMap<String, HashMap<String, Object>>> usageMap, boolean isOldType) throws Exception
    {
        try
        {
            if (usageMap == null)
                usageMap = new HashMap<>();

            //EA는 Double형의 Quantity가 올 수 없다.
            //Integer Type이 아니면 그대로 표기함.
            if (lineQty == null || lineQty.trim().equals(""))
                lineQty = "1";
            try{
                double dNum = Double.parseDouble(lineQty);
                int iNum = (int)dNum;
                if( dNum == iNum){
                    lineQty = "" + iNum;
                }
            }catch(NumberFormatException nfe){
                nfe.printStackTrace();
            }

            String oldNewType = isOldType ? "OLD" : "NEW";
            ArrayList<OpTrim> trimList = ospec.getTrimList();
            String simpleCondition = BomUtil.convertToSimpleCondition(varintCondition);
            
            if (usageOptionSetList != null)
            {
                for (OpTrim trim : trimList)
                {
                    String sosStdName = trim.getTrim() + "_STD";
                    String sosOptName = trim.getTrim() + "_OPT";
                    StoredOptionSet sosStd = usageOptionSetList.get(sosStdName);
                    StoredOptionSet sosOpt = usageOptionSetList.get(sosOptName);
                    if (sosStd.isInclude(engine, simpleCondition))
                    {
                        if (usageMap.containsKey(sosStdName))
                        {
                            HashMap<String, HashMap<String, Object>> oldNewValueMap = usageMap.get(sosStdName);
                            if (oldNewValueMap.containsKey(oldNewType))
                            {
                                String beforeQty = oldNewValueMap.get(oldNewType).get("USAGE_QTY").toString();
                                double curQty = Double.valueOf(beforeQty) + Double.valueOf(lineQty);
                                oldNewValueMap.get(oldNewType).put("USAGE_QTY", String.valueOf(curQty));
                            }
                            else
                            {
                                usageMap.get(sosStdName).put(oldNewType, BomUtil.getUsageInfo(trim, lineQty, "STD", oldNewType, systemRowKey));
                            }
                        }
                        else
                        {
                            HashMap<String, HashMap<String, Object>> newValue = new HashMap<>();
                            newValue.put(oldNewType, BomUtil.getUsageInfo(trim, lineQty, "STD", oldNewType, systemRowKey));
                            usageMap.put(sosStdName, newValue);
                        }
                    }
                    else if (sosOpt.isInclude(engine, simpleCondition))
                    {
                        if (usageMap.containsKey(sosOptName))
                        {
                            HashMap<String, HashMap<String, Object>> oldNewValueMap = usageMap.get(sosOptName);
                            if (oldNewValueMap.containsKey(oldNewType))
                            {
                                String beforeQty = oldNewValueMap.get(oldNewType).get("USAGE_QTY").toString();
                                double curQty = Double.valueOf(beforeQty) + Double.valueOf(lineQty);
                                oldNewValueMap.get(oldNewType).put("USAGE_QTY", String.valueOf(curQty));
                            }
                            else
                            {
                                usageMap.get(sosOptName).put(oldNewType, BomUtil.getUsageInfo(trim, lineQty, "OPT", oldNewType, systemRowKey));
                            }
                        }
                        else
                        {
                            HashMap<String, HashMap<String, Object>> newValue = new HashMap<>();
                            newValue.put(oldNewType, BomUtil.getUsageInfo(trim, lineQty, "STD", oldNewType, systemRowKey));
                            usageMap.put(sosOptName, newValue);
                        }
                    }
                }
            }

            return usageMap;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private HashMap<String, Object> getPartValue(String ccnId, String functionCode, TCComponentBOMLine bomLine, String[] parentValues, String[] propValues, String[] bomValues, OSpec ospec, HashMap<String, StoredOptionSet> usageOptionSetList, HashMap<String, Object> propMap, boolean isOldType, String changeType) throws Exception
    {
        try
        {
            if (propMap == null)
            {
                propMap = new HashMap<>();
            }
            else
            {
                if (! isOldType)
                    propMap.put("CHG_TYPE", changeType);
            }

            propMap.put("PARENT_UNIQUE_NO", parentValues[0]);
            if (null != parentValues[1] && ! parentValues[1].equals("")) {
                propMap.put("PARENT_NO", parentValues[1]);
            }else{
                propMap.put("PARENT_NO", parentValues[0]);
            }
            propMap.put("PARENT_TYPE", parentValues[2]);
            propMap.put("PARENT_NAME", parentValues[3]);
            propMap.put("PARENT_REV", parentValues[4]);
            propMap.put("PARENT_UID", bomLine.parent().getItemRevision().getUid());

            if (! propMap.containsKey("PARENT_MOD_DATE"))
            {
//                AIFComponentContext[] acc = bomLine.parent().getItemRevision().getChildren(PropertyConstant.ATTR_NAME_BL_STRC_REVISION);
//
//                if (acc.length > 0 && null != acc[0])
//                {
//                    TCComponentBOMViewRevision tcBomviewRevision = (TCComponentBOMViewRevision) acc[0].getComponent();
//                    propMap.put("PARENT_MOD_DATE", BomUtil.simpleDateFormat.format(tcBomviewRevision.getDateProperty(PropertyConstant.ATTR_NAME_LASTMODDATE)));
//                }
                String parentModDate = BomUtil.getBVRModifyDate(bomLine.parent().getItemRevision().getUid());
                propMap.put("PARENT_MOD_DATE", parentModDate);
            }

            if (isOldType)
            {
                // BOMLine
                propMap.put("OLD_CHILD_UNIQUE_NO", bomValues[0]);
                propMap.put("OLD_CHILD_PUID", bomLine.getItemRevision().getUid());
                propMap.put("OLD_CHILD_TYPE", bomValues[2]);
                propMap.put("OLD_CHILD_NAME", bomValues[3]);
                propMap.put("OLD_CHILD_QTY", bomValues[4]);
                propMap.put("OLD_CHILD_REV", bomValues[7]);
                propMap.put("OLD_MODULE", bomValues[9]);
                propMap.put("OLD_SMODE", bomValues[10]);
                propMap.put("OLD_MANDATORY_OPT", bomValues[11]);
                propMap.put("OLD_SPECIFICATION", bomValues[12]);
//                propMap.put("OLD_CHG_CD", oldBomProps[13]);
                propMap.put("OLD_ALTER_PART", bomValues[14]);
                propMap.put("OLD_LEV", bomValues[15]);
                propMap.put("OLD_SYSTEM_ROW_KEY", bomValues[16]);
                propMap.put("OLD_FUNCTION", functionCode);
                
                // Item Rev
                propMap.put("OLD_PROJECT", propValues[0]);
//                if (bomLine.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) //[20180307][LJG]주석 - 표준품도 시스템코드를 BOMLine에서 가져오도록 변경
//                {
//                    propMap.put("OLD_SYSTEM_CODE", "X00");
//                    propMap.put("OLD_SYSTEM_NAME", "STANDARD HARD-WARES");
//                }
//                else
//                {
                    propMap.put("OLD_SYSTEM_CODE", bomValues[23]);
//                if (null != bomValues[23] && !bomValues[23].equals("")) {
//                    sysCodeName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, bomValues[23]);
//                }
//                propMap.put("OLD_SYSTEM_NAME", sysCodeName);
                    propMap.put("OLD_SYSTEM_NAME", systemCodeMap.get(bomValues[23]));
//                }
                propMap.put("OLD_COLOR_ID", propValues[2]);
                propMap.put("OLD_EST_WEIGHT", propValues[3]);
                propMap.put("OLD_CAL_WEIGHT", propValues[4]);
                propMap.put("OLD_TGT_WEIGHT", propValues[5]);
                propMap.put("OLD_CONTENTS", propValues[6]);
                if (null != propValues[7] && !propValues[7].equals("")) {
                    propMap.put("OLD_CHG_TYPE_ENGCONCEPT", propValues[7]);
                }else{
                    propMap.put("OLD_CHG_TYPE_ENGCONCEPT", bomValues[13]);
                }            
                propMap.put("OLD_CON_DWG_PLAN", propValues[8]);
                propMap.put("OLD_CON_DWG_PERFORMANCE", propValues[9]);
                propMap.put("OLD_CON_DWG_TYPE", propValues[10]);

//                propMap.put("OLD_DWG_DEPLOYABLE_DATE", bomLine.getItemRevision().getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
                Object dwgDeployDate = BomUtil.getDwgDeployableDateForOspecImport(bomLine.getItemRevision().getUid());
                propMap.put("OLD_DWG_DEPLOYABLE_DATE", dwgDeployDate);

                propMap.put("OLD_PRD_DWG_PERFORMANCE", propValues[12]);
                propMap.put("OLD_PRD_DWG_PLAN", propValues[13]);
                
                /* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
//                propMap.put("OLD_DVP_NEEDED_QTY", propValues[14]);
//                propMap.put("OLD_DVP_USE", propValues[15]);
//                propMap.put("OLD_DVP_REQ_DEPT", propValues[16]);
                propMap.put("OLD_DVP_NEEDED_QTY", bomValues[17]);
                propMap.put("OLD_DVP_USE", bomValues[18]);
                propMap.put("OLD_DVP_REQ_DEPT", bomValues[19]);
                
                /* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
//                propMap.put("OLD_ENG_DEPT_NM", propValues[17]);
//                propMap.put("OLD_ENG_RESPONSIBLITY", BomUtil.getUserIdForName(propValues[18], propValues[17]));
                propMap.put("OLD_ENG_DEPT_NM", bomValues[20]);
                propMap.put("OLD_ENG_RESPONSIBLITY", BomUtil.getUserIdForName(bomValues[21], bomValues[20]));
                
              //[SR170703-020][LJG]Proto Tooling 컬럼 추가
                propMap.put("OLD_PROTO_TOOLING", bomValues[22]);
                
//                propMap.put("EST_COST_MATERIAL", propValues[19]);
//                propMap.put("TGT_COST_MATERIAL", propValues[20]);
//                propMap.put("SELECTED_COMPANY", propValues[21]);
//                propMap.put("PRT_TOOLG_INVESTMENT", propValues[22]);
//                propMap.put("PRD_TOOL_COST", propValues[23]);
//                propMap.put("PRD_SERVICE_COST", propValues[24]);
//                propMap.put("PRD_SAMPLE_COST", propValues[25]);
//                propMap.put("PUR_TEAM", propValues[26]);
//                propMap.put("PUR_RESPONSIBILITY", propValues[27]);
//                propMap.put("EMPLOYEE_NO", propValues[28]);
//                propMap.put("CHANGE_DESC", propValues[29]);
                propMap.put("OLD_SELECTIVE_PART", propValues[30]);
                propMap.put("OLD_CATEGORY", propValues[31]);
                propMap.put("OLD_PRD_PART_NO", propValues[32]);
                propMap.put("OLD_BOX", getBoxValue(propValues[33]));
                propMap.put("OLD_REGULATION", propValues[34]);
                if (null != propValues[35] && !propValues[35].equals("")) {
                    propMap.put("OLD_CHILD_NO", propValues[35]);
                }else{
                    propMap.put("OLD_CHILD_NO", propValues[36]);
                }
                propMap.put("OLD_CHILD_UNIQUE_NO", propValues[36]);
                propMap.put("OLD_ECO", propValues[37]);
                
                propMap.put("OLD_PRD_PROJECT", propValues[38]);

                // DCS 관련 정보 가져오기
//                HashMap<String, Object> dcsMapInfo = getDCSInfo(propValues[0], bomValues[23]);
//                propMap.put("OLD_DC_ID", dcsMapInfo.get("DC_ID"));
//                propMap.put("OLD_DC_REV", dcsMapInfo.get("DC_REV"));
//                propMap.put("OLD_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));
                
                if (! BundleUtil.nullToString(changeType).equals(""))
                {
                    propMap.put("OLD_SEQ", bomValues[1]);
                    propMap.put("CHG_TYPE",changeType);
                    HashMap<String, Object> mapVc = BomUtil.getVariant(bomValues[5]);
                    propMap.put("OLD_VC",mapVc.get("printDescriptions").toString());
//                  propMap.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
//
//                    if (null != bomValues[5] && !bomValues[5].equals("")) {
//                        propMap.put("USAGE_LIST", BomUtil.getTrimInfo(bomLine, "OLD", bomValues[5], ospec, usageOptionSetList));
//                    }
                }

            }
            else
            {
                propMap.put("CCN_ID", ccnId);

                // BOMLine
                propMap.put("NEW_CHILD_UNIQUE_NO", bomValues[0]);
                propMap.put("NEW_CHILD_PUID", bomLine.getItemRevision().getUid());
                propMap.put("NEW_CHILD_TYPE", bomValues[2]);
                propMap.put("NEW_CHILD_NAME", bomValues[3]);
                propMap.put("NEW_CHILD_QTY", bomValues[4]);
                propMap.put("NEW_CHILD_REV", bomValues[7]);
                propMap.put("NEW_MODULE", bomValues[9]);
                propMap.put("NEW_SMODE", bomValues[10]);
                propMap.put("NEW_MANDATORY_OPT", bomValues[11]);
                propMap.put("NEW_SPECIFICATION", bomValues[12]);
//                propMap.put("NEW_CHG_CD", bomValues[13]);
                propMap.put("NEW_ALTER_PART", bomValues[14]);
                propMap.put("NEW_LEV", bomValues[15]);
                propMap.put("NEW_SYSTEM_ROW_KEY", bomValues[16]);
                propMap.put("NEW_FUNCTION", functionCode);

                // Item Rev
                propMap.put("NEW_PROJECT", propValues[0]);
//                if (bomLine.getItem().getType().equals(SYMCClass.S7_STDPARTTYPE)) //[20180307][LJG]주석 - 표준품도 시스템코드를 BOMLine에서 가져오도록 변경
//                {
//                    propMap.put("NEW_SYSTEM_CODE", "X00");
//                    propMap.put("NEW_SYSTEM_NAME", "STANDARD HARD-WARES");
//                }
//                else
//                {
                    propMap.put("NEW_SYSTEM_CODE", bomValues[23]);
//                if (null != bomValues[23] && !bomValues[23].equals("")) {
//                    sysCodeName = SDVLOVUtils.getLovValueDesciption(PropertyConstant.ATTR_NAME_SYSTEMCODE, bomValues[23]);
//                }
//                propMap.put("NEW_SYSTEM_NAME", sysCodeName);
                    propMap.put("NEW_SYSTEM_NAME", systemCodeMap.get(bomValues[23]));
//                }
                propMap.put("NEW_COLOR_ID", propValues[2]);
                propMap.put("NEW_EST_WEIGHT", propValues[3]);
                propMap.put("NEW_CAL_WEIGHT", propValues[4]);
                propMap.put("NEW_TGT_WEIGHT", propValues[5]);
                propMap.put("NEW_CONTENTS", propValues[6]);
                if (null != propValues[7] && !propValues[7].equals("")) {
                    propMap.put("NEW_CHG_TYPE_ENGCONCEPT", propValues[7]);
                }else{
                    propMap.put("NEW_CHG_TYPE_ENGCONCEPT", bomValues[13]);
                }   
                propMap.put("NEW_CON_DWG_PLAN", propValues[8]);
                propMap.put("NEW_CON_DWG_PERFORMANCE", propValues[9]);
                propMap.put("NEW_CON_DWG_TYPE", propValues[10]);

//                propMap.put("NEW_DWG_DEPLOYABLE_DATE", bomLine.getItemRevision().getDateProperty(PropertyConstant.ATTR_NAME_DWG_DEPLOYABLE_DATE));
                Object dwgDeployDate = BomUtil.getDwgDeployableDateForOspecImport(bomLine.getItemRevision().getUid());
                propMap.put("NEW_DWG_DEPLOYABLE_DATE", dwgDeployDate);

                propMap.put("NEW_PRD_DWG_PERFORMANCE", propValues[12]);
                propMap.put("NEW_PRD_DWG_PLAN", propValues[13]);
                
                /* [SR없음][20150914][jclee] DVP Sample 속성 BOMLine으로 이동*/
//                propMap.put("NEW_DVP_NEEDED_QTY", propValues[14]);
//                propMap.put("NEW_DVP_USE", propValues[15]);
//                propMap.put("NEW_DVP_REQ_DEPT", propValues[16]);
                propMap.put("NEW_DVP_NEEDED_QTY", bomValues[17]);
                propMap.put("NEW_DVP_USE", bomValues[18]);
                propMap.put("NEW_DVP_REQ_DEPT", bomValues[19]);

                /* [SR없음][20160317][jclee] Design User, Dept 속성 BOMLine으로 이동 */
//                propMap.put("NEW_ENG_DEPT_NM", propValues[17]);
//                propMap.put("NEW_ENG_RESPONSIBLITY", BomUtil.getUserIdForName(propValues[18], propValues[17]));
                propMap.put("NEW_ENG_DEPT_NM", bomValues[20]);
                propMap.put("NEW_ENG_RESPONSIBLITY", BomUtil.getUserIdForName(bomValues[21], bomValues[20]));
                
                //[SR170703-020][LJG]Proto Tooling 컬럼 추가
                propMap.put("NEW_PROTO_TOOLING", bomValues[22]);
                
                propMap.put("EST_COST_MATERIAL", propValues[19]);
                propMap.put("TGT_COST_MATERIAL", propValues[20]);
                propMap.put("SELECTED_COMPANY", propValues[21]);
                propMap.put("PRT_TOOLG_INVESTMENT", propValues[22]);
                propMap.put("PRD_TOOL_COST", propValues[23]);
                propMap.put("PRD_SERVICE_COST", propValues[24]);
                propMap.put("PRD_SAMPLE_COST", propValues[25]);
                propMap.put("PUR_TEAM", propValues[26]);
                propMap.put("PUR_RESPONSIBILITY", propValues[27]);
                propMap.put("EMPLOYEE_NO", propValues[28]);
                propMap.put("CHANGE_DESC", propValues[29]);
                propMap.put("NEW_SELECTIVE_PART", propValues[30]);
                propMap.put("NEW_CATEGORY", propValues[31]);
                propMap.put("NEW_PRD_PART_NO", propValues[32]);
                propMap.put("NEW_BOX", getBoxValue(propValues[33]));
                propMap.put("NEW_REGULATION", propValues[34]);
                if (null != propValues[35] && !propValues[35].equals("")) {
                    propMap.put("NEW_CHILD_NO", propValues[35]);
                }else{
                    propMap.put("NEW_CHILD_NO", propValues[36]);
                }
                propMap.put("NEW_CHILD_UNIQUE_NO", propValues[36]);
                propMap.put("NEW_ECO", propValues[37]);
                // PREBOM_UNIQUE_ID 는 부모에 ID + "_" + FIND NO + "_" + 자식에 ID 로 구성 된다
                propMap.put("PREBOM_UNIQUE_ID", propMap.get("PARENT_UNIQUE_NO") + "_" + bomValues[23] + "_" + propValues[36]);

                propMap.put("NEW_PRD_PROJECT", propValues[38]);
                
                // DCS 관련 정보 가져오기
                //[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
//                HashMap<String, Object> dcsMapInfo = getDCSInfo(propValues[0], bomValues[23]);
                HashMap<String, Object> dcsMapInfo = getNewDCSInfo(propValues[0], bomValues[23]);
                propMap.put("NEW_DC_ID", dcsMapInfo.get("DC_ID"));
                propMap.put("NEW_DC_REV", dcsMapInfo.get("DC_REV"));
                propMap.put("NEW_RELEASED_DATE", dcsMapInfo.get("DC_RELEASED_DATE"));
                
                if(!BundleUtil.nullToString(changeType).equals("")) {
                    propMap.put("NEW_SEQ", bomValues[1]);
                    propMap.put("CHG_TYPE",changeType);
                    HashMap<String, Object> mapVc = BomUtil.getVariant(bomValues[5]);
                    propMap.put("NEW_VC",mapVc.get("printDescriptions").toString());
//              propMap.setOld_occ_uid(get_Occ_thread_Id(bomComponent));  //removed. change level of the pre-bom is not occ but bom-line.
//                    if (null != bomValues[5] && !bomValues[5].equals("")) {
//                        propMap.put("USAGE_LIST", BomUtil.getTrimInfo(bomLine, "NEW", bomValues[5], ospec, usageOptionSetList));
//                    }
                }

            }
            return propMap;
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }
    
    public static String getBoxValue(String boxValue) {
        if (null == boxValue || boxValue.equals("")) {
            return "";
        }
        return boxValue.substring(0, 1) + "B";
    }

    private HashMap<String, Object> getDCSInfo(String projectCode, String sysCode) throws Exception {
        HashMap<String, Object> dcsMapInfo = new HashMap<String, Object>();
        TCComponent[] tccomps = SDVQueryUtils.executeSavedQuery("SYMC_Search_DesignConcept", new String[]{"Project Code", "System Code"}, new String[]{projectCode, sysCode});
        if (null != tccomps && tccomps.length > 0) {
            TCComponentItem dcsItem = (TCComponentItem) tccomps[0];
            TCComponentItemRevision dcsItemRev = SYMTcUtil.getLatestReleasedRevision(dcsItem);
            if (null != dcsItemRev) {
                String []dcsProperties = dcsItemRev.getProperties(new String[]{PropertyConstant.ATTR_NAME_ITEMID, PropertyConstant.ATTR_NAME_ITEMREVID, PropertyConstant.ATTR_NAME_DATERELEASED});
                dcsMapInfo.put("DC_ID", dcsProperties[0]);
                dcsMapInfo.put("DC_REV", dcsProperties[1]);
                dcsMapInfo.put("DC_RELEASED_DATE", BomUtil.getDCSReleasedDate(dcsItemRev.getUid()));
            }
        }
        return dcsMapInfo;
    }
    
    //[SR181211-009][CSH]External Table에서 DCS 정보 가져오기
    private HashMap<String, Object> getNewDCSInfo(String projectCode, String sysCode) throws Exception {
        HashMap<String, Object> dcsMapInfo = new HashMap<String, Object>();
        
        SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PROJECT_CODE", projectCode);
		ds.put("SYSTEM_CODE", sysCode);
		ArrayList<HashMap<String, Object>> list = (ArrayList<HashMap<String, Object>>)remote.execute("com.kgm.service.OSpecService", "getDCSInfo", ds);
        if(list.size() > 0){
        	dcsMapInfo = list.get(0);
        }

        return dcsMapInfo;
    }

	/**
	 * OSpec 등록 후 변경된 Trim 유무에 따라 Flag 변경 작업 수행 Procedure 호출
	 * @param sOSpecNo
	 * @throws Exception
	 */
	private void updateOSpecTrimStat(String sOSpecNo) throws Exception {
		if (sOSpecNo == null || sOSpecNo.equals("") || sOSpecNo.length() == 0) {
			return;
		}
		
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("ospecNo", sOSpecNo);
		
		remote.execute("com.kgm.service.OSpecService", "updateOSpecTrimStat", ds);
	}

    private String getGateNo(String projectId) throws Exception {
        TCComponent[] tcComponents = SDVPreBOMUtilities.queryComponent("__SYMC_S7_PreProductRevision", new String[]{"Project Code"}, new String[]{projectId});
        if (null != tcComponents && tcComponents.length > 0) {
            TCComponentItemRevision productRevision = null;
            for (TCComponent tcComponent : tcComponents) {
                if (tcComponent.getType().equals(TypeConstant.S7_PREPRODUCTREVISIONTYPE)) {
                    productRevision = SYMTcUtil.getLatestReleasedRevision(((TCComponentItemRevision)tcComponent).getItem());
                    break;
                }
            }
            String sGateNo = productRevision.getProperty(PropertyConstant.ATTR_NAME_GATENO);
            
            return sGateNo;
        }
        return null;
    }
}
