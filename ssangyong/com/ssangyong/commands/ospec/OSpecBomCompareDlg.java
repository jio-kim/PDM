package com.ssangyong.commands.ospec;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import jxl.Cell;
import jxl.CellView;
import jxl.Sheet;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import com.ssangyong.commands.ospec.op.OPItem;
import com.ssangyong.commands.ospec.op.OpComparableConditionSet;
import com.ssangyong.commands.ospec.op.OpFunction;
import com.ssangyong.commands.ospec.op.OpUtil;
import com.ssangyong.commands.ospec.panel.PublishPanel;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.util.ArraySorter;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import javax.swing.SwingConstants;

public class OSpecBomCompareDlg extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private OSpecMainDlg parentDlg = null;
	private JTable table = null;
	private JComboBox comboBox = null;
	private ArrayList<OpFunction> funcList = new ArrayList();
	private HashMap<String, ArrayList<OPItem>> usedOptionNameMap = null;
	private static final String OPTION_ADD = "ADD";
	private static final String OPTION_REMOVE = "REMOVE";
	private static final String OPTION_CHANGE = "CHANGE";
	private static final String OPTION_EQUAL = "EQUAL";
	private static final String OPTION_CATEGORY_ADD = "CATEGORY ADD";
	private static final String OPTION_CATEGORY_REMOVE = "CATEGORY REMOVE";
	
	private JRadioButton radioBtnReleased = null; //Release 전개 선택 버튼
	private JTextField textFieldPartName = null; //Part Name 
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			OSpecBomCompareDlg dialog = new OSpecBomCompareDlg(null);
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
	public OSpecBomCompareDlg(OSpecMainDlg parentDlg) throws Exception {
		super(parentDlg, true);
		this.parentDlg = parentDlg;
		
		init();
	}

	private void init() throws Exception{
		setTitle("Compare with BOM");
//		setBounds(100, 100, 263, 300);
		setBounds(100, 100, 420, 320);	// Compare with Excel 버튼 추가로 인해 Size 조절
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			PropertyLayout pl_panel = new PropertyLayout(5, 5, 5, 5, 5, 5);
			pl_panel.setBottomMargin(0);
			pl_panel.setHorizontalGap(0);
			pl_panel.setRightMargin(0);
			pl_panel.setTopMargin(0);
			pl_panel.setLeftMargin(0);
			pl_panel.setVerticalGap(0);
			JPanel panel = new JPanel(pl_panel);
			contentPanel.add(panel, BorderLayout.NORTH);
			
			JPanel firstPanel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) firstPanel.getLayout();
			flowLayout.setVgap(0);
			comboBox = new JComboBox();
			firstPanel.add(comboBox);
			
			ArrayList<HashMap<String, String>> functionList = parentDlg.getFuntionList();
			for( int i = 0; functionList != null && i < functionList.size(); i++){
				HashMap map = functionList.get(i);
				OpFunction function = new OpFunction((String)map.get("ITEM_ID"), (String)map.get("ITEM_REV_ID")
						, (String)map.get("ITEM_NAME"), (String)map.get("PRODUCT_ID"), (String)map.get("PROJECT_CODE"));
				comboBox.addItem(function);
			}
			
			JButton button = new JButton("");
			firstPanel.add(button);
			button.setPreferredSize(new Dimension(25, 25));
			button.setIcon(new ImageIcon(OSpecBomCompareDlg.class.getResource("/com/teamcenter/rac/aif/images/add_16.png")));
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					
					if( comboBox.getSelectedIndex() < 0){
						return;
					}
					
					if( funcList.contains(comboBox.getSelectedItem())){
						return;
					}
					
					funcList.add((OpFunction)comboBox.getSelectedItem());
					Collections.sort(funcList);
					
					OpFunction function = null;
					DefaultTableModel model = (DefaultTableModel)table.getModel();
					OSpecBomCompareDlg.this.parentDlg.removeAllRow(table);
					
					for( int i = 0; i < funcList.size(); i++){
						function = funcList.get(i);
						Vector row = new Vector();
						row.add(function.getItemId());
						row.add(function.getItemName());
						row.add(function);
						model.addRow(row);
					}
					
				}
			});
			/**
			 * [20160719] 리스트에서 Function 선택하여 추가기능 추가
			 */
			JButton searchButton = new JButton("");
			firstPanel.add(searchButton);
			searchButton.setPreferredSize(new Dimension(25, 25));
			searchButton.setIcon(new ImageIcon(OSpecBomCompareDlg.class.getResource("/com/teamcenter/rac/common/images/add_content_16.png")));
			searchButton.setToolTipText("add selected items in list");
			
			searchButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent paramActionEvent) {
					OspecSelectFunctionDlg dlg = new OspecSelectFunctionDlg(OSpecBomCompareDlg.this, parentDlg);
					dlg.setVisible(true);

					ArrayList<OpFunction> selectedFunctionList = dlg.getSelectFunctionList();
					if (selectedFunctionList.size() == 0)
						return;
					
					for(OpFunction opFunction : selectedFunctionList)
					{
						boolean isAlreadyExist = false;
						for(OpFunction savedFunction :  funcList)
						{
							if (opFunction.getItemId().equals(savedFunction.getItemId())) 
								isAlreadyExist = true;
						}
						
						if(isAlreadyExist)
							continue;
						funcList.add(opFunction);
					}
					Collections.sort(funcList);

					OpFunction function = null;
					DefaultTableModel model = (DefaultTableModel)table.getModel();
					OSpecBomCompareDlg.this.parentDlg.removeAllRow(table);
					
					for( int i = 0; i < funcList.size(); i++){
						function = funcList.get(i);
						Vector<Object> row = new Vector<Object>();
						row.add(function.getItemId());
						row.add(function.getItemName());
						row.add(function);
						model.addRow(row);
					}					
				}
			});

			JButton button_1 = new JButton("");
			firstPanel.add(button_1);
			button_1.setPreferredSize(new Dimension(25, 25));
			button_1.setIcon(new ImageIcon(OSpecBomCompareDlg.class.getResource("/com/teamcenter/rac/aif/images/remove_16.png")));
			button_1.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					int[] idx = table.getSelectedRows();
					DefaultTableModel model = (DefaultTableModel)table.getModel();
					for( int i = idx.length - 1; i >= 0; i--){
						int modelIdx = table.convertRowIndexToModel(idx[i]);
						OpFunction function = (OpFunction)model.getValueAt(idx[i], 2);
						model.removeRow(modelIdx);
						funcList.remove(function);
					}
				}
			});
			

			/**
			 * [20160719] Working 중인 것도 확인가능하도록 Working 으로 조회 기능 추가
			 */
			radioBtnReleased = new JRadioButton("Released");
			firstPanel.add(radioBtnReleased);
			radioBtnReleased.setSelected(true);
			
			JRadioButton radioBtnWorking = new JRadioButton("Latest Working");
			firstPanel.add(radioBtnWorking);
			
			ButtonGroup bg = new ButtonGroup();
			bg.add(radioBtnReleased);
			bg.add(radioBtnWorking);
		
			/**
			 * [20160721] Part Name을 선택할 수 있도록 함
			 * 
			 */
			JPanel secondPanel = new JPanel();
			JLabel lbPartName = new JLabel("Part Name");
			lbPartName.setHorizontalAlignment(SwingConstants.RIGHT);
			lbPartName.setPreferredSize(new Dimension(100, 20));
			secondPanel.add(lbPartName);
			textFieldPartName = new JTextField();
			secondPanel.add(textFieldPartName);
			textFieldPartName.setColumns(20);
			panel.add("1.1.left", firstPanel);
			panel.add("2.1.left", secondPanel);

		}

		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				Vector<String> header = new Vector();
				header.add("Function");
				header.add("Function Name");
				header.add("Function Obj");
				DefaultTableModel model = new DefaultTableModel(null, header){

					@Override
					public boolean isCellEditable(int arg0, int arg1) {
						// TODO Auto-generated method stub
						return false;
					}
					
				};
				table = new JTable(model);
				TableColumnModel cm = table.getColumnModel();
				cm.removeColumn(cm.getColumn(2));
			
				int width[] = {100, 250 };
				for (int i = 0; i < cm.getColumnCount(); i++) {
					cm.getColumn(i).setPreferredWidth(width[i]);
				}
				
				JScrollPane scrollPane = new JScrollPane(table);
				panel.add(scrollPane);
			}
		}
		{
			
			JPanel buttonPane = new JPanel();
			//buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			buttonPane.setLayout(new BorderLayout(0, 0));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			JPanel leftButtonPanel = new JPanel();
			buttonPane.add(leftButtonPanel, BorderLayout.WEST);
			// [NoSR][2016.01.07][jclee] Compare with Excel 기능 추가
			{
				JButton compareWithExcelButton = new JButton("Compare With Excel");
				compareWithExcelButton.setIcon(new ImageIcon(OSpecBomCompareDlg.class.getResource("/com/ssangyong/common/images/excel_16.png")));
				compareWithExcelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						ArrayList<String> optionCombinations = getOptionCombinationsFromExcel();
						if (optionCombinations == null || optionCombinations.size() == 0) {
							return;
						}
						
						compareWithBOMLineCondition(optionCombinations);
					}
				});
				leftButtonPanel.add(compareWithExcelButton);
				getRootPane().setDefaultButton(compareWithExcelButton);
			}
			{
				
				JPanel rightButtnPanel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) rightButtnPanel.getLayout();
				flowLayout.setAlignment(FlowLayout.TRAILING);
				buttonPane.add(rightButtnPanel);
				
				JButton compareButton = new JButton("Compare");
				compareButton.setIcon(new ImageIcon(OSpecBomCompareDlg.class.getResource("/com/ssangyong/common/images/excel_16.png")));
				compareButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						// [NoSR][2016.01.06][jclee] Compare Method 분리
						PublishPanel panel = (PublishPanel)parentDlg.getPublicationPanel();
						ArrayList<String> optionCombinations = panel.getOptionCombinations(null);
						
						compareWithBOMLineCondition(optionCombinations);
					}
				});
				rightButtnPanel.add(compareButton);
				getRootPane().setDefaultButton(compareButton);
			}
//			{
//				JButton cancelButton = new JButton("Cancel");
//				cancelButton.addActionListener(new ActionListener() {
//					public void actionPerformed(ActionEvent e) {
//						dispose();
//					}
//				});
//				buttonPane.add(cancelButton);
//			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private ArrayList<String> getOptionCombinationsFromExcel() {
		ArrayList<String> optionCombinations = new ArrayList<String>();
		// File Choose Dialog Open(여러개를 한꺼번에 선택 가능해야함)
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if( f.isFile()){
					return f.getName().endsWith("xls");
				}else if(f.isDirectory())
				{
					return true;
				}
				return false;
			}

			public String getDescription() {
				return "*.xls";
			}
		});
		fileChooser.setMultiSelectionEnabled(true);
		
		int result = fileChooser.showOpenDialog(OSpecBomCompareDlg.this);
		if( result == JFileChooser.APPROVE_OPTION){
			File[] selectedFiles = fileChooser.getSelectedFiles();
			if (selectedFiles.length > 0) {
				for (int inx = 0; inx < selectedFiles.length; inx++) {
					File selectedFile = selectedFiles[inx];
					Workbook workbook = null;
					try {
						workbook = Workbook.getWorkbook(selectedFile);
						Sheet sheet = workbook.getSheet(0);
						int iRowCount = sheet.getRows();
						int iStartRow = 5;
						int iCol = 4;
						
						for (int jnx = iStartRow; jnx < iRowCount; jnx++) {
							Cell cell = sheet.getCell(iCol, jnx);
							String sCondition = cell.getContents();
							String sConditionTemp = "";
							String[] sConditionSplit = sCondition.split("\n");
							
							for (int knx = 0; knx < sConditionSplit.length; knx++) {
								if (sConditionTemp.length() == 0) {
									sConditionTemp = sConditionSplit[knx];
								} else {
									sConditionTemp = sConditionTemp + " OR " + sConditionSplit[knx]; 
								}
							}
							
							optionCombinations.add(sConditionTemp);
						}
						
						workbook.close();
					} catch (BiffException e) {
						e.printStackTrace();
						if (workbook != null) {
							workbook.close();
						}
					} catch (IOException e) {
						e.printStackTrace();
						if (workbook != null) {
							workbook.close();
						}
					}
				}
			} else {
				return null;
			}
		}
		
		return optionCombinations;
	}
	
	/**
	 * 
	 * @param optionCombinations
	 */
	@SuppressWarnings("unchecked")
	private void compareWithBOMLineCondition(ArrayList<String> optionCombinations) {
		if( funcList == null || funcList.isEmpty()){
			return;
		}
		
		usedOptionNameMap = new HashMap();
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		try{
			DataSet ds = new DataSet();
			
			ArrayList<String> selectedFunctions = new ArrayList();
			for( OpFunction func : funcList){
				if( !selectedFunctions.contains(func.getItemId())){
					selectedFunctions.add(func.getItemId());
				}
			}
			
			/**
			 * [20160719] Working 중인 것도 조회가능하도록 IS_RELEASE_FLAG Parameter 추가
			 * Release 기준 전개:0, Working 기준 전개 :1 
			 */
			String releaseFlag = radioBtnReleased !=null && !radioBtnReleased.isSelected() ? "1" : "0";

			ds.put("FUNCTION_LIST", selectedFunctions);
			ds.put("IS_RELEASE_FLAG", releaseFlag);
			
			/**
			 * [20160721] 특정 파트 Name 만 비교
			 */
			String partName = textFieldPartName.getText().isEmpty()?null:textFieldPartName.getText();
			ds.put("PART_NAME", partName);
			
			ArrayList<String> usedOptionList = new ArrayList();
			ArrayList<HashMap<String, String>> usedOptionMapList = (ArrayList)remote.execute("com.ssangyong.service.OSpecService", "getUsedCondition", ds);
			if( usedOptionMapList != null && !usedOptionMapList.isEmpty()){
				for( HashMap<String, String> map : usedOptionMapList){
					String condition = map.get("CONDITION");
					if( !usedOptionList.contains(condition)){
						usedOptionList.add(condition);
					}
					//20211206 [CF-2659] seho option 비교에서 제외되는 part가 발생함. 정렬로 인해 key값이 틀어짐. 초기 데이터도 동일하게 값을 정렬함.
					String[] conditionSplit = condition.split(" OR ");
					String sortConditionString = "";
					if (conditionSplit.length > 0)
					{
						ArrayList<String> conditionList = new ArrayList<String>();
						for (int jnx = 0; jnx < conditionSplit.length; jnx++)
						{
							conditionList.add(conditionSplit[jnx]);
						}
						ArraySorter.sort(conditionList);
						for (int jnx = 0; jnx < conditionList.size(); jnx++)
						{
							if (sortConditionString.length() == 0)
							{
								sortConditionString = conditionList.get(jnx);
							} else
							{
								sortConditionString = sortConditionString + " OR " + conditionList.get(jnx);
							}
						}
					}
					if(sortConditionString.isEmpty())
					{
						sortConditionString = condition;
					}

					if (!usedOptionNameMap.containsKey(sortConditionString))
					{
						ArrayList<OPItem> list = new ArrayList();
						list.add(new OPItem(map.get("CHILD_NO"), map.get("CHILD_NAME")));
						usedOptionNameMap.put(sortConditionString, list);
					} else
					{
						ArrayList<OPItem> list = usedOptionNameMap.get(sortConditionString);
						list.add(new OPItem(map.get("CHILD_NO"), map.get("CHILD_NAME")));
					}
				}
			}
			for (int inx = 0; inx < usedOptionList.size(); inx++)
			{
				ArrayList<String> alUsedOptionTemp = new ArrayList<String>();
				String sUsedOptionTemp = "";
				String sUsedOption = usedOptionList.get(inx);
				String[] sUsedOptionSplit = sUsedOption.split(" OR ");
				if (sUsedOptionSplit.length > 0)
				{
					for (int jnx = 0; jnx < sUsedOptionSplit.length; jnx++)
					{
						alUsedOptionTemp.add(sUsedOptionSplit[jnx]);
					}
					ArraySorter.sort(alUsedOptionTemp);
					for (int jnx = 0; jnx < alUsedOptionTemp.size(); jnx++)
					{
						if (sUsedOptionTemp.length() == 0)
						{
							sUsedOptionTemp = alUsedOptionTemp.get(jnx);
						} else
						{
							sUsedOptionTemp = sUsedOptionTemp + " OR " + alUsedOptionTemp.get(jnx);
						}
					}
					usedOptionList.set(inx, sUsedOptionTemp);
				}
			}
			ArraySorter.sort(usedOptionList);

			// Temp Start
			/*
			String[] str = {"A01C AND A03A AND A11A AND C00T AND C01L OR A01C AND A03A AND A11B AND C00T AND C01L"
							,"A01C AND A03A AND A11A AND C00T AND C01P OR A01C AND A03A AND A11B AND C00T AND C01P"};
			ArrayList<OPItem> list = new ArrayList();
			list.add(new OPItem("000000312", "TEST1"));
			usedOptionNameMap.put(str[0], list);
			
			list = new ArrayList();
			list.add(new OPItem("000000678", "TEST2"));
			usedOptionNameMap.put(str[1], list);
			
			usedOptionList.clear();
			usedOptionList.add(str[0]);
			usedOptionList.add(str[1]);
			*/
			
			for (int inx = 0; inx < optionCombinations.size(); inx++)
			{
				ArrayList<String> alOptionCombinationTemp = new ArrayList<String>();
				String sOptionCombinationTemp = "";
				String sOptionCombination = optionCombinations.get(inx);
				String[] sOptionCombinationSplit = sOptionCombination.split(" OR ");
				if (sOptionCombinationSplit.length > 0)
				{
					for (int jnx = 0; jnx < sOptionCombinationSplit.length; jnx++)
					{
						alOptionCombinationTemp.add(sOptionCombinationSplit[jnx]);
					}
					ArraySorter.sort(alOptionCombinationTemp);
					for (int jnx = 0; jnx < alOptionCombinationTemp.size(); jnx++)
					{
						if (sOptionCombinationTemp.length() == 0)
						{
							sOptionCombinationTemp = alOptionCombinationTemp.get(jnx);
						} else
						{
							sOptionCombinationTemp = sOptionCombinationTemp + " OR " + alOptionCombinationTemp.get(jnx);
						}
					}
					optionCombinations.set(inx, sOptionCombinationTemp);
				}
			}
			//[20150209] Yun Sung Won, 순서를 변경하여, 비교시, 보다 유사한 옵션값끼리 매핑되도록 함. 
			ArraySorter.sort(optionCombinations);
			
			ArrayList<String> addedOptionCombinations = ((ArrayList<String>)optionCombinations.clone());
			addedOptionCombinations.removeAll(usedOptionList);
			ArrayList<String> equalOptionCombinations = (ArrayList<String>)optionCombinations.clone();
			equalOptionCombinations.removeAll(addedOptionCombinations);
			
			ArrayList<String> removedOptionCombinations = (ArrayList<String>)usedOptionList;
			removedOptionCombinations.removeAll(optionCombinations);
			
			if( addedOptionCombinations.isEmpty() && removedOptionCombinations.isEmpty()){
				MessageBox.post(OSpecBomCompareDlg.this, "There is not a difference.", "Information", MessageBox.INFORMATION);
				return;
			}
			
			exportToExcel(equalOptionCombinations, addedOptionCombinations, removedOptionCombinations, selectedFunctions);
			
			dispose();
		}catch( Exception e){
			e.printStackTrace();
		}
		
	
	}
	
	private void exportToExcel(ArrayList<String> equalOptionCombinations, ArrayList<String> addedOptionCombinations, ArrayList<String> removedOptionCombinations, ArrayList<String> selectedFunctions){
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File("BOM_Compared_Option_Combination_" + sdf.format(now.getTime()) + ".xls");
		fileChooser.setSelectedFile(defaultFile);
//		fileChooser.addChoosableFileFilter(new OptionDefinitionFileFilter("MSEXCEL"));
		fileChooser.setFileFilter(new FileFilter(){

			public boolean accept(File f) {
				// TODO Auto-generated method stub
				if( f.isFile()){
					return f.getName().endsWith("xls");
				}
				return false;
			}

			public String getDescription() {
				// TODO Auto-generated method stub
				return "*.xls";
			}
			
		});
		int result = fileChooser.showSaveDialog(OSpecBomCompareDlg.this);
		if( result == JFileChooser.APPROVE_OPTION){
			File selectedFile = fileChooser.getSelectedFile();
			try
            {
				exportToExcel(selectedFile, equalOptionCombinations, addedOptionCombinations, removedOptionCombinations, selectedFunctions);
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
            }
            catch (Exception e)
            {
            	e.printStackTrace();
            	MessageBox.post(OSpecBomCompareDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
            }
		}
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	private Vector<OpComparableConditionSet> getData(ArrayList<String> equalOptionCombinations, ArrayList<String> addedOptionCombinations, ArrayList<String> removedOptionCombinations){
		
		Vector<OpComparableConditionSet> data = new Vector();
		Vector<String> checkDupAddOptionList = new Vector<String>(); // 중복된 값 방지
		for( String options: equalOptionCombinations){
			ArrayList<OPItem> list = usedOptionNameMap.get(options);
			String usedOptionNames = "";
			for( int k = 0; list != null && k < list.size(); k++){
				OPItem opItem = list.get(k);
				OpComparableConditionSet row = new OpComparableConditionSet();
				row.add(opItem.getItemID());
				row.add(opItem.getItemName());
				row.add(options);
				row.add(options);
				row.add(OPTION_EQUAL);
				data.add(row);
			}
		}
		
		//Category 삭제된 경우,
		Pattern p = Pattern.compile("[a-zA-Z0-9]{4}");
		for( int i = addedOptionCombinations.size() - 1; i >= 0; i--){
			String addOption = addedOptionCombinations.get(i); 
			boolean isAllOptionFound = true;
			for( int j = removedOptionCombinations.size() - 1; j >= 0; j--){
				// AddOption 하나와 removeOption하나를 비교.
				// 한 라인 비교 후 아니면, 다른 removeOption과 비교,
				String removeOption = removedOptionCombinations.get(j);
				
				Matcher m = p.matcher(addOption);
				isAllOptionFound = true;
				while (m.find()) {
					String option = m.group();
					isAllOptionFound &= (removeOption.indexOf(option) > -1);
					if( !isAllOptionFound ){
						break;
					}
				}
				
				if( isAllOptionFound){
					//AddOption의 모든 옵션값이 removeOption에 포함되면, removeOption의 옵션이 더 많다는 의미이며, 즉 카테고리가 제거 된 경우이다. 
					ArrayList<OPItem> list = usedOptionNameMap.get(removeOption);
					String usedOptionNames = "";
					for( int k = 0; list != null && k < list.size(); k++){
						OPItem opItem = list.get(k);
						OpComparableConditionSet row = new OpComparableConditionSet();
						row.add(opItem.getItemID());
						row.add(opItem.getItemName());
						row.add(removeOption);
						if(!checkDupAddOptionList.contains(addOption))
						{
							row.add(addOption);
							row.add(OPTION_CHANGE);
							checkDupAddOptionList.add(addOption);
						}else
						{
							/**
							 * 중복일 경우에는 값을 입력하지않고 REMOVE 로 표시한다.
							 */
							row.add("");
							row.add(OPTION_REMOVE);
						}
						data.add(row);
						
						removedOptionCombinations.remove(removeOption);
						addedOptionCombinations.remove(addOption);
					}
					
					break;
				}
			}
		}
		
		//Category 추가된 경우.
		for( int i = removedOptionCombinations.size() - 1; i >= 0; i--){
			String removeOption = removedOptionCombinations.get(i); 
			boolean isAllOptionFound = true;
			for( int j = addedOptionCombinations.size() - 1; j >= 0; j--){
				// AddOption 하나와 removeOption하나를 비교.
				// 한 라인 비교 후 아니면, 다른 removeOption과 비교,
				String addOption = addedOptionCombinations.get(j);
				Matcher m = p.matcher(removeOption);
				isAllOptionFound = true;
				while (m.find()) {
					String option = m.group();
					isAllOptionFound &= (addOption.indexOf(option) > -1);
					if( !isAllOptionFound ){
						break;
					}
				}
				
				if( isAllOptionFound){
					//AddOption의 모든 옵션값이 removeOption에 포함되면, removeOption의 옵션이 더 많다는 의미이며, 즉 카테고리가 제거 된 경우이다. 
					ArrayList<OPItem> list = usedOptionNameMap.get(removeOption);
					String usedOptionNames = "";
					for( int k = 0; list != null && k < list.size(); k++){
						OPItem opItem = list.get(k);
						OpComparableConditionSet row = new OpComparableConditionSet();
						row.add(opItem.getItemID());
						row.add(opItem.getItemName());
						row.add(removeOption);
						if(!checkDupAddOptionList.contains(addOption))
						{
							row.add(addOption);
							row.add(OPTION_CHANGE);
							checkDupAddOptionList.add(addOption);
						}else
						{
							/**
							 * 중복일 경우에는 값을 입력하지않고 REMOVE 로 표시한다.
							 */
							row.add("");
							row.add(OPTION_REMOVE);
						}
						data.add(row);
					}
					
					removedOptionCombinations.remove(removeOption);
					addedOptionCombinations.remove(addOption);
					
					break;
				}
			}
		}
		
		//옵션값이 바뀐 경우. Start.
		HashMap<String, ArrayList<String>> removedCategoryMap = new HashMap();
		for( int i = removedOptionCombinations.size() - 1; i >= 0; i--){
			String removeOption = removedOptionCombinations.get(i); 
			boolean isAllOptionFound = true;
			
			ArrayList<String> removedCategoryList = new ArrayList();
			Matcher m = p.matcher(removeOption);
			while (m.find()) {
				String option = m.group();
				String category = OpUtil.getCategory(option);
				if( !removedCategoryList.contains(category)){
					removedCategoryList.add(category);
				}
			}
			
			removedCategoryMap.put(removeOption, removedCategoryList);
		}
		
		for( int j = addedOptionCombinations.size() - 1; j >= 0; j--){
			// AddOption 하나와 removeOption하나를 비교.
			// 한 라인 비교 후 아니면, 다른 removeOption과 비교,
			String addOption = addedOptionCombinations.get(j);
			ArrayList<String> addedCategoryList = new ArrayList();
			Matcher m = p.matcher(addOption);
			while (m.find()) {
				String option = m.group();
				String category = OpUtil.getCategory(option);
				if( !addedCategoryList.contains(category)){
					addedCategoryList.add(category);
				}
			}
			ArrayList<String> cloneAddedCategoryList = (ArrayList<String>)addedCategoryList.clone();
			
			for( int i = removedOptionCombinations.size() - 1; i >= 0; i--){
				String removeOption = removedOptionCombinations.get(i); 
				ArrayList<String> removedCategoryList = removedCategoryMap.get(removeOption);
				ArrayList<String> cloneRemovedCategoryList = (ArrayList<String>)removedCategoryList.clone();
				if( cloneAddedCategoryList.size() == cloneRemovedCategoryList.size()){
					cloneRemovedCategoryList.removeAll(cloneAddedCategoryList);
					
					//모든 카테고리가 일치한다.
					if( cloneRemovedCategoryList.isEmpty()){
						
						ArrayList<OPItem> list = usedOptionNameMap.get(removeOption);
//						String usedOptionNames = "";
						for( int k = 0; list != null && k < list.size(); k++){
							OPItem opItem = list.get(k);
							OpComparableConditionSet row = new OpComparableConditionSet();
							row.add(opItem.getItemID());
							row.add(opItem.getItemName());
							row.add(removeOption);
							if(!checkDupAddOptionList.contains(addOption))
							{
								row.add(addOption);
								row.add(OPTION_CHANGE);
								checkDupAddOptionList.add(addOption);
							}else
							{
								/**
								 * 중복일 경우에는 값을 입력하지않고 REMOVE 로 표시한다.
								 */
								row.add("");
								row.add(OPTION_REMOVE);
							}
							data.add(row);
							
							removedOptionCombinations.remove(removeOption);
							addedOptionCombinations.remove(addOption);
						}
						
						
						break;
					}
				}
			}
		}
		//옵션값이 바뀐 경우. End.
		
		//옵션값 전체가 추가된 경우.
		for( int j = addedOptionCombinations.size() - 1; j >= 0; j--){
			// AddOption 하나와 removeOption하나를 비교.
			// 한 라인 비교 후 아니면, 다른 removeOption과 비교,
			String addOption = addedOptionCombinations.get(j);
			OpComparableConditionSet row = new OpComparableConditionSet();
			row.add("");
			row.add("");
			row.add("");
			row.add(addOption);
			row.add(OPTION_ADD);
			data.add(row);
			addedOptionCombinations.remove(addOption);
		}

		//옵션값 전체가 삭제된 경우.
		for( int i = removedOptionCombinations.size() - 1; i >= 0; i--){
			String removeOption = removedOptionCombinations.get(i); 
			ArrayList<OPItem> list = usedOptionNameMap.get(removeOption);
			String usedOptionNames = "";
			for( int k = 0; list != null && k < list.size(); k++){
				OPItem opItem = list.get(k);
				OpComparableConditionSet row = new OpComparableConditionSet();
				row.add(opItem.getItemID());
				row.add(opItem.getItemName());
				row.add(removeOption);
				row.add("");
				row.add(OPTION_REMOVE);
				data.add(row);
				
				removedOptionCombinations.remove(removeOption);
			}
			
		}
		
		Collections.sort(data);
		
		return data;
	}
	
	private void exportToExcel(File selectedFile, ArrayList<String> equalOptionCombinations, ArrayList<String> addedOptionCombinations, ArrayList<String> removedOptionCombinations, ArrayList<String> selectedFunctions) throws RowsExceededException, WriteException, IOException{

		Vector<OpComparableConditionSet> data = getData(equalOptionCombinations, addedOptionCombinations, removedOptionCombinations);
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);
	    
	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    cellFormat.setWrap(true);
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 1;
	    int initColumnNum = 0;

	    String funcs = "";
	    for( String functionID : selectedFunctions){
	    	if( funcs.equals("")){
	    		funcs = functionID;
	    	}else{
	    		funcs += ", " + functionID;
	    	}
	    }
	    label = new jxl.write.Label(0, startRow, "Selected Functions : " + funcs, cellFormat);
	    sheet.addCell(label);
	    sheet.mergeCells(0, 1, 4, 1);
	    
	    startRow = 3;
	    Vector excelColumnHeader = new Vector();
	    excelColumnHeader.add("Part NO");
	    excelColumnHeader.add("Part Name");
	    excelColumnHeader.add("Old EPL");
	    excelColumnHeader.add("New EPL");
	    excelColumnHeader.add("Type");
	    
	    int lastIdx = excelColumnHeader.size() - 1;
	    for (int i = 0; i < excelColumnHeader.size(); i++)
	    {
	      label = new jxl.write.Label(i + initColumnNum, startRow, excelColumnHeader.get(i).toString(), headerCellFormat);
	      sheet.addCell(label);
	      CellView cv = sheet.getColumnView(i + initColumnNum);
//	      cv.setAutosize(true);
	      switch(i){
	      case 0 :
	    	  cv.setSize(3000);
	    	  break;
	      case 1:
	    	  cv.setSize(6000);
	    	  break;
	      default :
	    	  if( i == excelColumnHeader.size() - 1){
	    		  cv.setSize(3000);
	    	  }else{
	    		  cv.setAutosize(true);
	    	  }
	    	  break;
	      
	      }
	      sheet.setColumnView(i + initColumnNum, cv);
	    }

	    int rowNum = 0;
	    startRow = 4;
	    
	    for (int i = 0; i < data.size(); i++)
	    {
	    	Vector row = data.get(i);
	    	for (int j = 0; j < row.size(); j++)
	    	{
	    		String condition = (String)row.get(j);
	    		condition = condition.replaceAll(" OR ", "\012");
	    		label = new jxl.write.Label(j + initColumnNum, (rowNum) + startRow, condition, cellFormat);
	    		sheet.addCell(label);
	    	}
	    	rowNum++;
	    }

	    //셀 Merge
	    /*
	    int startIdxToMerge = startRow;
	    int endIdxToMerge = startRow;
	    for (int i = 0; i < data.size(); i++){
	    	
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
	    		}
	    		startIdxToMerge = i + 1 + startRow;
	    	}
	    }
	    */
	    workBook.write();
	    workBook.close();
		
	}
}
