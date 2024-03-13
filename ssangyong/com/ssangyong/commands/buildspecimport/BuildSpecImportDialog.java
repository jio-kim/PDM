package com.ssangyong.commands.buildspecimport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import jxl.CellView;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.ssangyong.common.WaitProgressBar;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.ui.CheckComboBox;
import com.ssangyong.common.ui.MultiLineToolTip;
import com.ssangyong.common.utils.variant.VariantOption;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * @author slobbie
 * Build Spec 생성 Dialog
 */
@SuppressWarnings({"serial", "unchecked", "rawtypes", "unused"})
public class BuildSpecImportDialog extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private Registry registry;
	private TCComponentBOMLine target;
	private Vector headerVector = new Vector();
	private JComboBox projectNameCombo = null;
	private CheckComboBox comboBox = null;
	private JLabel targetLabel = null;
	private JButton okButton = null;
	private HashMap<String, VariantOption> optionMap = null;
	private JCheckBox chk = null;
	private Vector<Vector> allData = new Vector();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			BuildSpecImportDialog dialog = new BuildSpecImportDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Dialog 생성자
	 * 
	 * Create the dialog.
	 * @throws Exception 
	 */
	public BuildSpecImportDialog(TCComponentBOMLine target, HashMap<String, VariantOption> optionMap) throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setTitle("Build Spec Import - " + target.getItem().getProperty("item_id"));
		this.target = target;
		this.optionMap = optionMap;
		setPreferredSize(new Dimension(700, 400));
		setSize(700, 400);
		//컬럼 헤더
		headerVector.add("Class");
		headerVector.add("Project_NO");
		headerVector.add("Spec_NO");
		headerVector.add("Version");
		headerVector.add("Description");
		headerVector.add("Category");
		headerVector.add("Option Value");
		headerVector.add("Last Mod Date");
		headerVector.add("Last Mod User");
		
		registry = Registry.getRegistry(this);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
				flowLayout.setAlignment(FlowLayout.LEADING);
				panel.add(panel_1, BorderLayout.CENTER);
				
				projectNameCombo = new JComboBox();
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				try{
					DataSet ds = new DataSet();
					ArrayList<String> list = (ArrayList)remote.execute("com.ssangyong.service.VariantService", "getProjectCodes", ds);
					if( list != null ){
						for( String prjectName : list){
							projectNameCombo.addItem(prjectName);
						}
					}
				}catch( Exception e){
					e.printStackTrace();
				}
				panel_1.add(projectNameCombo);
				
				comboBox = new CheckComboBox("Please select Specs"){

					@Override
					public JToolTip createToolTip() {
						MultiLineToolTip tip = new MultiLineToolTip();
				        tip.setComponent(this);
				        return tip;
					}
					
				};
				
				comboBox.addMouseListener(new MouseAdapter(){

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
				
				comboBox.setPreferredSize(new Dimension(110, 20));
				comboBox.setPopupWidth(getWidth() - 10);

				//최초 TC DB에 저장된 Build Spec를 가져온다.
				ArrayList specList = new ArrayList();
				ArrayList result = null;
				try {
					DataSet ds = new DataSet();
					ds.put("VARIANT_ID", BuildSpecImportDialog.this.target.getItem().getProperty("item_id"));
					result = (ArrayList)remote.execute("com.ssangyong.service.VariantService", "getLocalBuildSpecList", ds);
					for( int i = 0; result != null && i < result.size(); i++){
						HashMap row = (HashMap)result.get(i);
						String desc = (String)row.get("DESCRIPTION");
						String item = row.get("SPEC_NO") + (desc == null ? "" : " | " + row.get("DESCRIPTION"));
						specList.add(item);
					}
					
				} catch (Exception e) {
					throw e;
				}
				HashSet set = new HashSet();
				set.addAll(specList);
				comboBox.resetObjs(set, false);
				
				panel_1.add(comboBox);
				
				{
					JButton refreshBtn = new JButton("");
					panel_1.add(refreshBtn);
					refreshBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent event) {
							int response = ConfirmationDialog.post("Build-Spec refresh", "Spec No is refreshed. it takes a few minutes. \nAre you sure?");
				            if(response != ConfirmationDialog.YES) {
				                return ;
				            }
				            
				            final WaitProgressBar waitProgress = new WaitProgressBar(BuildSpecImportDialog.this);
							waitProgress.start();
							
//						H-BOM에서 O-SPEC를 조회하여
//						TC의 기존 Option Value를 삭제 하고, 새로 조회한 값을 입력한다.
							waitProgress.setStatus("Refreshing Build-Spec list...");
							AbstractAIFOperation operation = new AbstractAIFOperation() {
								
								@Override
								public void executeOperation() throws Exception {
									SYMCRemoteUtil remote = new SYMCRemoteUtil();
									DataSet ds = new DataSet();
									
									ArrayList specList = new ArrayList();
									ArrayList result = null;
									try {
										ds.put("VARIANT_ID", BuildSpecImportDialog.this.target.getItem().getProperty("item_id"));
										result = (ArrayList)remote.execute("com.ssangyong.service.VariantService", "getBuildSpecList", ds);
										
										comboBox.removeAllItems();
										
										for( int i = 0; result != null && i < result.size(); i++){
											HashMap row = (HashMap)result.get(i);
											String desc = (String)row.get("DESCRIPTION");
											String spec = row.get("SPEC_NO") + (desc == null ? "" : " | " + row.get("DESCRIPTION"));
											specList.add(spec);
										}
										HashSet set = new HashSet();
										set.addAll(specList);
										comboBox.resetObjs(set, false);
										
										waitProgress.close();
									} catch (Exception e) {
										waitProgress.setShowButton(true);
									}
								}
							};
							
							BuildSpecImportDialog.this.target.getSession().queueOperation(operation);
				            
						}
					});
					refreshBtn.setPreferredSize(new Dimension(18, 18));
					refreshBtn.setIcon(registry.getImageIcon("Refresh.ICON"));
				}
				
				comboBox.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						comboBox.getSelectedItem();
					}
				});
			}
			{
				JPanel panel_1 = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
				flowLayout.setAlignment(FlowLayout.RIGHT);
				panel.add(panel_1, BorderLayout.EAST);
				{
					JButton searchBtn = new JButton("Search");
					searchBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							
							// [20240313][UPGRADE] 조건문 수정
							if( comboBox == null || comboBox.getSelectedItems() == null ){
								MessageBox.post(BuildSpecImportDialog.this, registry.getString("variant.selectBuildSpec"), "INFORMATION", MessageBox.WARNING);
								return;
							}
							
							if( comboBox != null && comboBox.getSelectedItems().length > 10 ){
								MessageBox.post(BuildSpecImportDialog.this, "Too many Spec. is selected. Please select only less than 10.", "INFORMATION", MessageBox.WARNING);
								return;
							}
							
							Object[] objs = comboBox.getSelectedItems();
							ArrayList list = new ArrayList();
							for( Object obj : objs){
								String selectedSpec = (String)obj;
								if( selectedSpec.indexOf("|") > -1 ){
									selectedSpec = selectedSpec.substring(0, selectedSpec.indexOf("|")).trim();
								}else{
									selectedSpec = selectedSpec.trim();
								}
								list.add(selectedSpec);
							}
							
							okButton.setEnabled(false);
							
							SYMCRemoteUtil remote = new SYMCRemoteUtil();
							DataSet ds = new DataSet();
							
							ArrayList result = null;
							try {
								
								ds.put("PROJECT_NO", projectNameCombo.getSelectedItem());
								ds.put("VARIANT_ID", BuildSpecImportDialog.this.target.getItem().getProperty("item_id"));
								ds.put("SPEC_NO", list);
								
								result = (ArrayList)remote.execute("com.ssangyong.service.VariantService", "getBuildSpecInfo", ds);
								
								DefaultTableModel model = (DefaultTableModel)table.getModel();
								
								allData.removeAllElements();
								for( int i = 0; result != null && i < result.size(); i++){
									HashMap map = (HashMap)result.get(i);
									Vector row = new Vector();
									
									row.add(map.get("CLASS"));
									row.add(map.get("PROJECT_NO"));
									row.add(((String)map.get("SPEC_NO")).trim());
									row.add(map.get("VERSION"));
									row.add(map.get("DESCRIPTION"));
									row.add(map.get("CATE_NO"));
									row.add(map.get("OPTION_NO"));
									row.add(map.get("LAST_MODIFY_DATE"));
									row.add(map.get("LAST_MODIFY_EMPL_ID"));
									allData.add(row);
								}
								Vector data = getData(chk.isSelected());
								model.setDataVector(data, headerVector);
								tableInit();
								
								if( model.getRowCount() < 1){
									MessageBox.post(BuildSpecImportDialog.this, "Not found.", "INFORMATION", MessageBox.WARNING);
									return;
								}
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					});
					{
						chk = new JCheckBox("Only show not matched");
						chk.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent arg0) {
								DefaultTableModel model = (DefaultTableModel)table.getModel();
								Vector data = getData(chk.isSelected());
								model.setDataVector(data, headerVector);
								tableInit();
							}
						});
						panel_1.add(chk);
					}
					searchBtn.setIcon(registry.getImageIcon("Search.ICON"));
					panel_1.add(searchBtn);
				}
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				TableModel model = new DefaultTableModel(null, headerVector) {
					public Class getColumnClass(int col) {
						return String.class;
					}

					public boolean isCellEditable(int row, int col) {
						return false;
					}
				};
				
				table = new JTable(model);
				tableInit();
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
				table.setRowSorter(sorter);	
				
				JScrollPane pane = new JScrollPane();
				pane.setViewportView(table);
				pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				pane.getViewport().setBackground(Color.WHITE);
				panel.add(pane);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Save SOS");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						if( table.getRowCount() > 0){
							
							//현재 Variant에 설정된 옵션 종류와 Build Spec에서의 옵션 종류가 다르면 SOS를 생성할 수 없다.
							for( Vector<String> row : allData){
								String category = row.get(5);
								if( BuildSpecImportDialog.this.optionMap.containsKey(category)){
									VariantOption option = BuildSpecImportDialog.this.optionMap.get(category);
									HashMap valueMap = option.getValueMap();
									if( !valueMap.containsKey(row.get(6))){
										MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.foundNoMatchOptionValue"), "INFORMATION", MessageBox.WARNING);
										return;
									}
								}else{
									MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.foundNoMatchOptionValue"), "INFORMATION", MessageBox.WARNING);
									return;
								}
							}
							
							final WaitProgressBar waitProgress = new WaitProgressBar(BuildSpecImportDialog.this);
							waitProgress.start();
							
							HashMap<String, HashMap<String, String>> specMap = new HashMap();
							
//							HashMap<String, String> map = new HashMap();
							DefaultTableModel model = (DefaultTableModel)table.getModel();
							for( Vector<String> row : allData){
								String spec = row.get(2);
								HashMap<String, String> map = specMap.get(spec);
								if( map == null ){
									map = new HashMap();
								}
								map.put((String)row.get(5), (String)row.get(6));
								
								specMap.put(spec, map);
							}
							
							Object[] objs = comboBox.getSelectedItems();
							String[] selectedSpecs = new String[objs.length];
							System.arraycopy(objs, 0, selectedSpecs, 0, objs.length);
//							String selectedSpec = (String)comboBox.getSelectedItems();
//							if( selectedSpec.indexOf("|") > -1 ){
//								selectedSpec = selectedSpec.substring(0, selectedSpec.indexOf("|")).trim();
//							}else{
//								selectedSpec = selectedSpec.trim();
//							}
							BuildSpecImportOperation operation = new BuildSpecImportOperation(BuildSpecImportDialog.this.target, (String)projectNameCombo.getSelectedItem(), selectedSpecs, specMap, waitProgress);
							BuildSpecImportDialog.this.target.getSession().queueOperation(operation);
						}
						
					}
				});
				okButton.setActionCommand("OK");
				okButton.setEnabled(false);
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				{
					JButton exportBtn = new JButton("Export");
					exportBtn.setIcon(new ImageIcon(BuildSpecImportDialog.class.getResource("/com/ssangyong/common/images/excel_16.png")));
					exportBtn.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {

							JFileChooser fileChooser = new JFileChooser();
							fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
							Calendar now = Calendar.getInstance();
							SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
							sdf.format(now.getTime());
							File defaultFile = new File("Build_Spec_" + sdf.format(now.getTime()) + ".xls");
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
							int result = fileChooser.showSaveDialog(BuildSpecImportDialog.this);
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
					            	if( selectedFile != null ){
					            		BuildSpecImportDialog.this.dispose();
					            	}
					            }
							}
							
							
						
						}
					});
					buttonPane.add(exportBtn);
				}
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}

	
	/**
	 * Table 컬럼폭 및 렌더러 셋팅.
	 */
	private void tableInit(){
		TableColumnModel columnModel = table.getColumnModel();
		int[] columnWidth = {50, 70, 100, 60, 300, 60, 60, 120, 100 };
		int n = headerVector.size();
		BuildSpecImportCellRenderer cellRenderer = new BuildSpecImportCellRenderer(optionMap);
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
			columnModel.getColumn(i).setWidth(columnWidth[i]);
			columnModel.getColumn(i).setCellRenderer(cellRenderer);
		}
		
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		
		if( table.getRowCount() > 0){
			okButton.setEnabled(true);
		}
	}
	
	
	/**
	 * 테이블의 데이타를 리턴
	 * 
	 * @param bOnlyChecked true : 체크된 데이타, false : 모든 데이타
	 * @return
	 */
	private Vector getData(boolean bOnlyChecked){
		
		Vector newData = new Vector();
		if( bOnlyChecked ){
			for( Vector<String> row : allData){
				String category = row.get(5);
				if( optionMap.containsKey(category)){
					VariantOption option = optionMap.get(category);
					HashMap valueMap = option.getValueMap();
					if( !valueMap.containsKey(row.get(6))){
						newData.add(row);
					}
				}else{
					newData.add(row);
				}
			}
			return newData;
		}else{
			return allData;
		}
	}
	
	/**
	 * Excel형식의 파일로 Export 함.
	 * 
	 * @param file
	 * @throws RowsExceededException
	 * @throws WriteException
	 * @throws IOException
	 */
	private void exportToExcel(File file) throws RowsExceededException, WriteException, IOException{
	    WritableWorkbook workBook = Workbook.createWorkbook(file);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);

	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    WritableCellFormat problemCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    problemCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    problemCellFormat.setBackground(Colour.ORANGE);
	    
	    int startRow = 1;

	    label = new jxl.write.Label(1, startRow, this.target.toDisplayString(), cellFormat);
	    sheet.addCell(label);
	    sheet.mergeCells(1, 1, 3, 1);
	    
	    startRow = 3;
	    for (int i = 0; i < headerVector.size(); i++)
	    {
	      label = new jxl.write.Label(i, startRow, headerVector.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i);
	      cv.setAutosize(true);
	      sheet.setColumnView(i, cv);
	    }

	    startRow = 4;
	    Vector<Vector> data = getData(chk.isSelected());
	    WritableCellFormat curFormat = null;
	    for (int i = 0; i < data.size(); i++)
	    {
	    	
	    	Vector<String> row = data.get(i);
	    	String category = row.get(5);
	    	if( BuildSpecImportDialog.this.optionMap.containsKey(category)){
				VariantOption option = BuildSpecImportDialog.this.optionMap.get(category);
				HashMap valueMap = option.getValueMap();
				if( !valueMap.containsKey(row.get(6))){
					curFormat = problemCellFormat;
				}else{
					curFormat = cellFormat;
				}
			}else{
				curFormat = problemCellFormat;
			}
	    	
	    	for (int j = 0; j < row.size(); j++)
	    	{
	    		String str = "";
	    		Object obj = row.get(j);
	    		if( obj instanceof String){
	    			str = row.get(j).toString();
	    		}else{
	    			System.out.println("ggg");
	    		}
	    		
	    		label = new jxl.write.Label(j, i + startRow, str, curFormat);
	    		sheet.addCell(label);
	    	}
	    }

	    workBook.write();
	    workBook.close();
	}
}
