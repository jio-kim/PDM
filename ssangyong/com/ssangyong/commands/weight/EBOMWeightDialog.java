package com.ssangyong.commands.weight;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.commands.ospec.op.OpCategory;
import com.ssangyong.commands.ospec.op.OpTrim;
import com.ssangyong.commands.ospec.op.Option;
import com.ssangyong.common.SYMCClass;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.ssangyong.common.utils.CustomUtil;
import com.ssangyong.common.utils.PreferenceService;
import com.ssangyong.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [SR180528-064] E-BOM Weight Report 개발 요청
 * @Copyright : Plmsoft
 * @author   : 조석훈
 * @since    : 2018. 5. 30.
 * Package ID : com.ssangyong.commands.weight.EBOMWeightDialog.java
 */

// 2024.01.09 수정 generic 삭제
@SuppressWarnings({ "unchecked" })
public class EBOMWeightDialog extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;
	private TCSession session = null;
	private OSpec ospec = null;
	private TCComponentItem selectedProductItem = null;
	private JTable table;
	private JScrollPane scroll = null;
	private CustomTableModel tableModel = null;
	private MultiLineHeaderRenderer headerRenderer = new MultiLineHeaderRenderer();
	private ArrayList<String> defaultTableColumnHeader = new ArrayList<String>();
	private JComboBox comboProduct;
	private JComboBox comboOspec;
	private JComboBox comboOspecRev;
	private JComboBox comboRevRule;
	private JList comboVariant;
	private HashMap<String, TCComponentItem> productMap = new HashMap<String, TCComponentItem>();
	private HashMap<String, TCComponentItem> ospecMap = new HashMap<String, TCComponentItem>();
	private HashMap<String, TCComponentItemRevision> ospecRevMap = new HashMap<String, TCComponentItemRevision>();
	private Registry registry;
	private ArrayList<TCComponentItem> variantItemList;
	private EBOMAllUsageListOperation usageListOperation;
	private ArrayList<StoredOptionSet> usageOptionSetList;
	
	public EBOMWeightDialog(Frame parentFrame, TCSession tcSession) throws Exception {
		super(parentFrame, false);
		registry = Registry.getRegistry("com.ssangyong.common.common");
		this.session = tcSession;
		init();
		initOpereation();
	}
	
	private void initOpereation() throws Exception{
		String[] possibleProducts = PreferenceService.getValues(TCPreferenceService.TC_preference_site, "SYMC_Selected_Product_Weight");
		
		String includeProductValues = "";
		if( possibleProducts != null ){
			for( int i =0; i < possibleProducts.length ; i++){
				includeProductValues = includeProductValues + (i != 0 ?";":"") + possibleProducts[i];
			}
		}
		
		TCComponent[] findProducts = CustomUtil.queryComponent("Item...", new String[]{"Type", "Item ID"}, new String[]{TypeConstant.S7_PRODUCTTYPE, includeProductValues});
		
		if(findProducts != null && findProducts.length > 0){
			ArrayList<String> productList = new ArrayList<String>(findProducts.length);
			String item_id = "";
			String name = "";
			String product = "";
			for (TCComponent prod : findProducts) {
				TCComponentItemRevision prodRev = ((TCComponentItem)prod).getLatestItemRevision();
				item_id = prodRev.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMID);
				name = prodRev.getItem().getProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
				product = item_id+" - "+name;
				productList.add(product);
				if (!productMap.containsKey(product)){
					productMap.put(product, prodRev.getItem());
				}
			}
			Collections.sort(productList);
			comboProduct.addItem("");
			for(String prd : productList){
				comboProduct.addItem(prd);
			}
		}
	}
	
	private void init() throws Exception {
		setTitle("Weight Master List");

		JPanel contentPanel = new JPanel();
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		String[] column = {"No", "TEAM", "담당자", "SYSCODE", "FUNCTION", "SEQ", "LEV\n(MAN)", "PART NO", "PART NAME", "S/MODE", "OPTION", "ACT WEIGHT\n(EA)" };
		Integer[] column_width =  { 50, 80, 80, 60, 110, 60, 50, 100, 200, 60, 110, 70 };
		ArrayList<Integer> defaultColumnWidths = new ArrayList<Integer>();
		
		defaultTableColumnHeader.addAll(Arrays.asList(column));
		defaultColumnWidths.addAll(Arrays.asList(column_width));

		tableModel = new CustomTableModel(defaultTableColumnHeader.toArray(new String[0]), 0);

		table = new JTable(tableModel) {
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.getTableHeader().setReorderingAllowed(false);
		table.setShowGrid(true);
		table.setAutoCreateRowSorter(true);
		Enumeration<TableColumn> cols = table.getColumnModel().getColumns();
		while (cols.hasMoreElements()) {
			((TableColumn) cols.nextElement()).setHeaderRenderer(headerRenderer);
		}
		for (int i = 0; i < defaultColumnWidths.size(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(defaultColumnWidths.get(i));
		}

		CustomCellRenderer alignRenderer = new CustomCellRenderer();
		DefaultTableCellRenderer alignLeftRenderer = new DefaultTableCellRenderer();
		alignLeftRenderer.setHorizontalAlignment(SwingConstants.LEFT);
		DefaultTableCellRenderer alignCenterRenderer = new DefaultTableCellRenderer();
		alignCenterRenderer.setHorizontalAlignment(SwingConstants.CENTER);

		for (int i = 0; i < table.getColumnCount(); i++)
			table.getColumnModel().getColumn(i).setCellRenderer(alignRenderer);

		scroll = new JScrollPane(table);
		contentPanel.add(scroll, BorderLayout.CENTER);
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		JButton cancelButton = new JButton("Close", registry.getImageIcon("Cancel_24.ICON"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				disposeDialog();
			}
		});
		buttonPanel.add(cancelButton);
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel conditionPanel = new JPanel();
		topPanel.add(conditionPanel, BorderLayout.WEST);
		
		contentPanel.add(topPanel, BorderLayout.NORTH);
		conditionPanel.setOpaque(false);

		JLabel lblProduct = new JLabel("Product");
		lblProduct.setHorizontalAlignment(SwingConstants.CENTER);
		conditionPanel.add(lblProduct);

		comboProduct = new JComboBox();
		comboProduct.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox) paramItemEvent.getSource()).getSelectedItem();

				if (!paramItemEvent.getItem().equals(selectItem) && selectItem != null) {
					productChangeAction(selectItem.toString());
				}
			}
		});
		conditionPanel.add(comboProduct);

		JLabel label = new JLabel("  ");
		conditionPanel.add(label);
		
		//Add [SR190919-016]
		JLabel lblOspec = new JLabel("Ospec");
		lblProduct.setHorizontalAlignment(SwingConstants.CENTER);
		conditionPanel.add(lblOspec);

		comboOspec = new JComboBox();
		comboOspec.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox) paramItemEvent.getSource()).getSelectedItem();

				if (!paramItemEvent.getItem().equals(selectItem) && selectItem != null) {
					ospecChangeAction(selectItem.toString());
				}
			}
		});
		conditionPanel.add(comboOspec);
		
		comboOspecRev = new JComboBox();
		comboOspecRev.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent paramItemEvent) {
				Object selectItem = ((JComboBox) paramItemEvent.getSource()).getSelectedItem();

				if (!paramItemEvent.getItem().equals(selectItem) && selectItem != null) {
					ospecRevChangeAction(selectItem.toString());
				}
			}
		});
		conditionPanel.add(comboOspecRev);

		JLabel label1 = new JLabel("  ");
		conditionPanel.add(label1);
		
		//End

		JLabel lblVariant = new JLabel("Variant");
		conditionPanel.add(lblVariant);

		comboVariant = new JList(new DefaultListModel());
		
		JScrollPane pane = new JScrollPane();
		pane.setPreferredSize(new Dimension(400, 100));
		pane.setViewportView(comboVariant);
		pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		pane.getViewport().setBackground(Color.WHITE);
		conditionPanel.add(pane);

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BorderLayout(0, 0));
		
		JPanel buttPanel = new JPanel();
		topPanel.add(innerPanel, BorderLayout.EAST);
		
		innerPanel.add(buttPanel, BorderLayout.SOUTH);
		
		JButton btnSearch = new JButton("Search");
		buttPanel.add(btnSearch);
		btnSearch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				searchAction();
			}
		});

		JButton btnExportToExcel = new JButton("Download");
		buttPanel.add(btnExportToExcel);
		btnExportToExcel.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				exportToExcelAction();
			}
		});
		
		JPanel revRulePanel = new JPanel();
		innerPanel.add(revRulePanel, BorderLayout.CENTER);
		comboRevRule = new JComboBox();
		comboRevRule.addItem("Latest Released");
		comboRevRule.addItem("Latest Working");
		comboRevRule.setSelectedIndex(0);
		revRulePanel.add(comboRevRule);
		
		JPanel tempPanel = new JPanel();
		innerPanel.add(tempPanel, BorderLayout.NORTH);
		JLabel text = new JLabel("     ");
		tempPanel.add(text);

		setMinimumSize(new Dimension(1150, 768));

		pack();
		centerToScreen(1.1D, 1.0D);
	}

	protected void exportToExcelAction() {
		try {
			if (table.getModel().getRowCount() == 0) {
				MessageBox.post(EBOMWeightDialog.this, "조회된 결과가 없습니다.", "확인", MessageBox.INFORMATION);
				return;
			}

			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			Calendar now = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
//			sdf.format(now.getTime());
			
			File defaultFile = new File("WeightMasterList_[" + selectedProductItem.getStringProperty(PropertyConstant.ATTR_NAME_ITEMID) + "]_" + sdf.format(now.getTime()) + ".xlsx");
			fileChooser.setSelectedFile(defaultFile);
			fileChooser.setFileFilter(new FileFilter() {
				@Override
				public boolean accept(File f) {
					if (f.isFile()) {
						return f.getName().endsWith("xlsx");
					}
					return false;
				}

				@Override
				public String getDescription() {
					return "*.xlsx";
				}
			});
			int result = fileChooser.showSaveDialog(EBOMWeightDialog.this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				String title = selectedProductItem.toString() + " - " + comboRevRule.getSelectedItem().toString();
//				final EBOMWeightExcelExportOperation exportOp = new EBOMWeightExcelExportOperation( selectedFile,
//						tableModel.getIdentifier(), tableModel.getDataVector(), title, usageOptionSetList, ospec);
				//수정 tableModel.getDataVector() = Vector<Vector> 반환함.
				Vector<Vector> rawData = tableModel.getDataVector();
				Vector<Vector<Object>> dataVec = new Vector<>(rawData);
				final EBOMWeightExcelExportOperation exportOp = new EBOMWeightExcelExportOperation( selectedFile,
						tableModel.getIdentifier(), dataVec, title, usageOptionSetList, ospec);
				
				exportOp.addOperationListener(new InterfaceAIFOperationListener() {
					@Override
					public void startOperation(String paramString) {
					}

					@Override
					public void endOperation() {
					}
				});
				session.queueOperation(exportOp);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private StoredOptionSet getStoredOptionSetAllMandatory( String trim, HashMap<String, OpCategory> categoryMap, String E00, String E00Name, String E10, String E10Name, OpTrim ot ) throws Exception {
		StoredOptionSet sosTemp;
		if(categoryMap == null){
			sosTemp = new StoredOptionSet( trim );
		} else {
			sosTemp = new StoredOptionSet( trim + "\n" + E00Name + "\n" + E10Name + "_" + ot.getArea() + "_" + ot.getPassenger() + "_" + ot.getEngine() + "_" + ot.getGrade());
//			sosTemp = new StoredOptionSet( trim + "\n" + E00Name + "\n" + E10Name);
			sosTemp.add("E00" , E00);
			sosTemp.add("E10" , E10);
			
			Set<String> categoryKeySet = categoryMap.keySet();
			ArrayList<String> sosList = new ArrayList<String>(categoryMap.size());
			
			for (Iterator<String> iterator = categoryKeySet.iterator(); iterator.hasNext();) {
				String categoryKey = iterator.next();
				sosList.add(categoryKey);
			}
			Collections.sort(sosList);
			
			for (Iterator<String> iterator = sosList.iterator(); iterator.hasNext();) {
				
				String categoryKey = iterator.next();
//				System.out.println("trim : "+trim);
//				System.out.println("1 : "+categoryKey);
//				if(categoryKey.equals("C20")){
//					System.out.println(categoryKey);
//				}
				
				if(sosTemp.getOptionSet().get(categoryKey) != null){
					continue;
				} else {
					OpCategory opCategory = categoryMap.get (categoryKey);
					ArrayList<Option> opList = opCategory.getOpValueList();
					Option std  = null;
					Option mand = null;
					for (Iterator iterator2 = opList.iterator(); iterator2.hasNext();) {
						
						Option option = (Option) iterator2.next();
						String value = option.getValue();
						if( value != null ){
							if( "S".equals(value) ){
								std = option;
							}else if ( value.substring(0,1).equals("M") ){
								
								String remark = option.getRemark();
								if( remark != null ){
									int start = remark.indexOf("(") +1;
									int end = remark.indexOf(")");
									
									String remarkOption =  remark.substring(start, end);
									String[] orList = remarkOption.split("OR");
									boolean result = true;
									for (int i = 0; i < orList.length; i++) {
										String[] andList = orList[i].trim().split("AND");
										
										for (int j = 0; j < andList.length; j++) {
											String tmpValue = andList[j].trim();
											String opCate = tmpValue.substring(0, 3);
											String sosTempValue = "";
											if(!categoryKey.equals(opCate) && sosTemp.getOptionSet().get(opCate) == null){
												//sostemp에 Madatory 설정을 위해 정의된 참고 값이 없을때....  
												sosTemp = setMandatoryOption(categoryMap, sosTemp, opCate);
											}
											
											if(sosTemp.getOptionSet().get(opCate) != null){
												sosTempValue = sosTemp.getOptionSet().get(opCate).get(0);
											}
											if( ! sosTempValue.equals(tmpValue)){
												result = false;
												break;
											}
										}
										
										if( result ) break;
									}
									
									if( result ){
										mand = option;
									}
								}
							}
						}
					}
					
					if( mand != null ){
						sosTemp.add(mand.getOp() , mand.getOpValue() );
					}else if (  std != null ) {
						sosTemp.add(std.getOp() , std.getOpValue() );
					}
				}
			}
			
//			ArrayList<String> sosList1 = sosTemp.getValueList();
//			Collections.sort(sosList1);
//			for (Iterator iterator = sosList1.iterator(); iterator.hasNext();) {
//				String string = (String) iterator.next();
//				
//				System.out.println(string);
//			}
//			System.out.println("--------------------------------------");
		}
		return sosTemp;
		
	}
	
	private StoredOptionSet setMandatoryOption(HashMap<String, OpCategory> categoryMap, StoredOptionSet sosTemp, String categoryKey){
//		System.out.println("2 : "+categoryKey);
		OpCategory opCategory = categoryMap.get (categoryKey);
		if(opCategory == null){
			return sosTemp;
		}
		ArrayList<Option> opList = opCategory.getOpValueList();
		Option std  = null;
		Option mand = null;
		for (Iterator iterator2 = opList.iterator(); iterator2.hasNext();) {
			Option option = (Option) iterator2.next();
			String value = option.getValue();
			if( value != null ){
				if( "S".equals(value) ){
					std = option;
				}else if ( value.substring(0,1).equals("M") ){
					String remark = option.getRemark();
					if( remark != null ){
						int start = remark.indexOf("(") +1;
						int end = remark.indexOf(")");
						
						String remarkOption =  remark.substring(start, end);
						
						String[] orList = remarkOption.split("OR");
						boolean result = true;
						for (int i = 0; i < orList.length; i++) {
							String[] andList = orList[i].trim().split("AND");
							for (int j = 0; j < andList.length; j++) {
								String tmpValue = andList[j].trim();
								String opCate = tmpValue.substring(0, 3);
								String sosTempValue = "";
								if(!categoryKey.equals(opCate) && sosTemp.getOptionSet().get(opCate) == null){
									//sostemp에 Madatory 설정을 위해 정의된 참고 값이 없을때....  
									sosTemp = setMandatoryOption(categoryMap, sosTemp, opCate);
								}
								if(sosTemp.getOptionSet().get(opCate) != null){
									sosTempValue = sosTemp.getOptionSet().get(opCate).get(0);
								}
								if( ! sosTempValue.equals(tmpValue)){
									result = false;
									break;
								}
							}
							
							if( result ) break;
						}
						
						if( result ){
							mand = option;
						}
					}
				}
			}
		}
		if( mand != null ){
			sosTemp.add(mand.getOp() , mand.getOpValue() );
		}else if (  std != null ) {
			sosTemp.add(std.getOp() , std.getOpValue() );
		}
		
		return sosTemp;
	}
	
	private void setUsageOptionSetList(Object[] selectedVariantObjects) throws Exception {
		String selectedVariant = "";
		variantItemList = new ArrayList<TCComponentItem>();
		TCComponentItem variantItem = null;
		String selectedTrim = null;
		HashMap<String, HashMap<String, OpCategory>> trimCategoryMap = null;
		
		if(ospec != null){
			trimCategoryMap = ospec.getCategory();
		}
			
		HashMap<String, OpCategory> categoryMap = null;
		usageOptionSetList = new ArrayList<StoredOptionSet>();
		
		for(int k=0 ; k<selectedVariantObjects.length ; k++){
			selectedVariant = selectedVariantObjects[k].toString();
			if(selectedVariant.equals("All")){
				continue;
			}
			
			selectedVariant = selectedVariant.substring(0, selectedVariant.indexOf("-"));
			variantItem = CustomUtil.findItem(SYMCClass.S7_VARIANTPARTTYPE, selectedVariant);
			variantItemList.add(variantItem);
			selectedTrim = variantItem.getStringProperty("item_id").substring(1,6);
			
			if(trimCategoryMap != null){
				//excel 셀병합을 위한 트림정보
				ArrayList<OpTrim> trimList = ospec.getTrimList();
				OpTrim ot = null;
				for(OpTrim opt : trimList){
					if(opt.getTrim().equals(selectedTrim)){
						ot = opt;
						break;
					}
				}
				
				categoryMap = trimCategoryMap.get( selectedTrim );
				
				HashMap<String, String> transmission = new HashMap<String, String>(); //Transmission
				OpCategory e00Category = categoryMap.get ("E00");
				if(e00Category == null){
					throw new Exception ("Can not find category E00 in OSPEC.");
				}
				ArrayList<Option> opList = e00Category.getOpValueList();
				for (Iterator iterator2 = opList.iterator(); iterator2.hasNext();) {
					Option option = (Option) iterator2.next();
					transmission.put(option.getOpValue(), option.getOpValueName());
				}
				
				HashMap<String, String> transfercase = new HashMap<String, String>(); //transfercase
				OpCategory e10Category = categoryMap.get ("E10");
				if(e10Category == null){
					throw new Exception ("Can not find category E10 in OSPEC.");
				}
				opList = e10Category.getOpValueList();
				for (Iterator iterator2 = opList.iterator(); iterator2.hasNext();) {
					Option option = (Option) iterator2.next();
					transfercase.put(option.getOpValue(), option.getOpValueName());
				}
				
				for (Iterator iterator = transmission.keySet().iterator(); iterator.hasNext();) {
					String e00 = (String)iterator.next();
					String e00Name = transmission.get(e00);
					for (Iterator iterator1 = transfercase.keySet().iterator(); iterator1.hasNext();) {
						String e10 = (String)iterator1.next();
						String e10Name = transfercase.get(e10);
						
						StoredOptionSet sosTemp = getStoredOptionSetAllMandatory(selectedTrim, categoryMap, e00, e00Name, e10, e10Name, ot);
						if( sosTemp != null ){
//							if(selectedTrim.equals("G5QWS")){
//								System.out.println("");
//							}
							usageOptionSetList.add(sosTemp );
							usageOptionSetList.add(sosTemp );
						}
					}
				}
				
			} else {
				//ospec이 없으면 variant 하위에 정의된 sos를 가지고 와서 보여줌. 더 필요하면 base이외의 option Value를 정의하여 set을 구성하면됨.
				TCComponentItemRevision variantRevision = null;
				if(comboRevRule.getSelectedItem().toString().equals("Latest Working")){
					variantRevision = variantItem.getLatestItemRevision();
				} else {
					variantRevision = SYMTcUtil.getLatestReleasedRevision(variantItem);
				}
				
				AIFComponentContext[] relatedContexts = variantRevision.getRelated("IMAN_reference");
				StoredOptionSet sos = null;
				int sosCount = 0;
				for (AIFComponentContext context : relatedContexts) {
					TCComponent com = (TCComponent) context.getComponent();
					if (com.getType().equals("StoredOptionSet")) {
						sosCount++;
						sos = getOptionSet(selectedTrim, com);
						if( sos != null ){
							usageOptionSetList.add(sos );
							usageOptionSetList.add(sos );
						}
					}
				}
				
				//Variant Revision 하위에 SOS도 없으면.... 빈 SOS를 만들어 usageOptionSetList에 담자...   전체 리스트가 출력되도록....
				if(sosCount == 0){
					StoredOptionSet sosTemp = getStoredOptionSetAllMandatory(selectedTrim, null, null, null, null, null, null);
					if( sosTemp != null ){
						usageOptionSetList.add(sosTemp );
						usageOptionSetList.add(sosTemp );
					}
				}
			}
		}
	}
	
	protected void searchAction() {
		try {
			String selectedProduct = (String) comboProduct.getSelectedItem();
			String selectedOspec = (String) comboOspec.getSelectedItem();
			String selectedOspecRev = (String) comboOspecRev.getSelectedItem();
			Object[] selectedVariantObjects = comboVariant.getSelectedValues();

			if (selectedProduct == null || selectedProduct.trim().equals("")) {
				MessageBox.post(this, "조회할 Product를 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			if (selectedOspec == null || selectedOspec.trim().equals("")) {
				MessageBox.post(this, "조회할 Ospec을 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			if (selectedOspecRev == null || selectedOspecRev.trim().equals("")) {
				MessageBox.post(this, "조회할 Ospec을 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			if (selectedVariantObjects == null || selectedVariantObjects.length == 0) {
				MessageBox.post(this, "조회할 Variant를 선택해 주세요.", "확인", MessageBox.INFORMATION);
				return;
			}
			
			String allText = "All";
			boolean isAll = false;
			for(int kk=0 ; kk<selectedVariantObjects.length ; kk++){
				if(allText.equals(selectedVariantObjects[kk].toString())){
					isAll = true;
					break;
				}
			}
			
			if(isAll){
				int allVariantSize = comboVariant.getModel().getSize();
				selectedVariantObjects = new Object[allVariantSize];
				for(int ii=0; ii<allVariantSize; ii++){
					selectedVariantObjects[ii] = comboVariant.getModel().getElementAt(ii);
				}
			}
			
			setUsageOptionSetList(selectedVariantObjects);
			
			if(usageOptionSetList != null && usageOptionSetList.size() > 0){
				selectedProductItem = productMap.get(selectedProduct);
				
				usageListOperation = new EBOMAllUsageListOperation(EBOMWeightDialog.this, table, usageOptionSetList, defaultTableColumnHeader.size());
				usageListOperation.addOperationListener(new InterfaceAIFOperationListener() {
					@Override
					public void startOperation(String arg0) {
						setTableColumn();
					}
	
					@Override
					public void endOperation() {
					}
				});
				session.queueOperation(usageListOperation);
			}
						
		} catch (Exception ex) {
			MessageBox.post(ex);
			ex.printStackTrace();
		}
	}
	
	private void setTableColumn(){
		table.removeAll();
		tableModel.setNumRows(0);
		CustomCellRenderer alignRenderer = new CustomCellRenderer();

		for (int i = table.getColumnCount() - 1; i >= defaultTableColumnHeader.size(); i--) {
			table.removeColumn(table.getColumnModel().getColumn(i));
		}
		
		table.repaint();

		Vector<String> headerIdentifier = new Vector<String>();
		headerIdentifier.addAll(defaultTableColumnHeader);
		int usageOptSize = usageOptionSetList.size();
		for(int k=0; k<usageOptSize; k++){
			StoredOptionSet ssos = usageOptionSetList.get(k);
			TableColumn estWeightColumn = new TableColumn();
			String headerValue = ssos.getName();
			int ind = headerValue.indexOf("_");
			if(ind > 0){
				headerValue = headerValue.substring(0, ind);
			}
			
			estWeightColumn.setHeaderValue(headerValue);
			estWeightColumn.setPreferredWidth(74);
			estWeightColumn.setHeaderRenderer(headerRenderer);
			estWeightColumn.setCellRenderer(alignRenderer);
			
			table.addColumn(estWeightColumn);
			if(k % 2 != 0){
				headerValue = headerValue + "\nWeight";
			} else {
				headerValue = headerValue + "\nQuantity";
			}
			headerIdentifier.add(headerValue);
		
		}
		
		ArrayList<TableCellRenderer> cellRenderers = new ArrayList<TableCellRenderer>();
		ArrayList<Integer> columnWidths = new ArrayList<Integer>();
		ArrayList<TableCellRenderer> headerRenderers = new ArrayList<TableCellRenderer>();
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			cellRenderers.add(table.getColumnModel().getColumn(i).getCellRenderer());
			columnWidths.add(table.getColumnModel().getColumn(i).getWidth());
			headerRenderers.add(table.getColumnModel().getColumn(i).getHeaderRenderer());
		}

		tableModel.setColumnIdentifiers(headerIdentifier);
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setHeaderRenderer(headerRenderers.get(i));
			table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths.get(i));
			table.getColumnModel().getColumn(i).setCellRenderer(cellRenderers.get(i));
		}

		scroll.revalidate();
		table.repaint();
	}

//	protected void productChangeAction(String selectedProduct) {
//		try {
//			if (selectedProduct == null || selectedProduct.equals("")) {
//				((DefaultListModel<String>) comboVariant.getModel()).clear();
//				return;
//			}
//			
//			TCComponentItem prodItem = productMap.get(selectedProduct);
//			TCComponentItemRevision oSpecRevision = getOSpecRevision(prodItem);
//
//			//Ospec이 없을때 진행여부 확인
//			boolean isContinue = true;
//			if(oSpecRevision == null){
//				ospec = null;
//				int response = ConfirmationDialog.post(AIFUtility.getActiveDesktop(), "Confirm", "Ospec does not exist. Do you want to continue?");
//				if (response == ConfirmationDialog.NO) {
//					isContinue = false;
//				}
//			} else {
//				ospec = BomUtil.getOSpec(oSpecRevision);
//			}
//			
//			if(isContinue){
//				((DefaultListModel<String>) comboVariant.getModel()).clear();
//				((DefaultListModel<String>) comboVariant.getModel()).addElement("All");
//				
//				TCComponentItemRevision prodRev = SYMTcUtil.getLatestReleasedRevision(prodItem);
//				AIFComponentContext[] variants = prodRev.getRelated("view");
//				HashMap<String, String> variantMap = new HashMap<String, String>();
//				if(variants != null && variants.length > 0){
//					TCComponentItem variantItem = null;
//					String variant = "";
//					String trim = "";
//					String variantId = "";
//					for(AIFComponentContext context : variants){
//						if(context.getComponent() instanceof TCComponentItem){
//							variantItem = (TCComponentItem)context.getComponent();
//							variantId = variantItem.getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
//							variant = variantId+"-"+variantItem.getStringProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
//							trim = variantId.substring(1,6);
//							variantMap.put(trim, variant);
//						}
//					}
//				}
//				//환경기술팀 전원용 책임 요청사항 반영
//				//ospec과 동일한 순서대로 variant Item 정렬
//				if(ospec != null){
//					ArrayList<OpTrim> trimList = ospec.getTrimList();
//					String oTrim = "";
//					for(OpTrim ot : trimList){
//						oTrim = ot.getTrim();
//						((DefaultListModel<String>) comboVariant.getModel()).addElement(variantMap.get(oTrim));
//					}
//				} else {
//					Set<String> vSet = variantMap.keySet();
//					for (Iterator<String> iterator = vSet.iterator(); iterator.hasNext();) {
//						String vKey = iterator.next();
//						((DefaultListModel<String>) comboVariant.getModel()).addElement(variantMap.get(vKey));
//					}
//				}
//			}
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	protected void productChangeAction(String selectedProduct) {
		try {
			
			((DefaultListModel) comboVariant.getModel()).clear();
			comboOspec.removeAllItems();
			comboOspecRev.removeAllItems();
			if (selectedProduct == null || selectedProduct.equals("")) {
				return;
			}
			
			TCComponentItem prodItem = productMap.get(selectedProduct);
			TCComponent[] com = CustomUtil.queryComponent("SYMC_Search_OspecSet", new String[]{"Project"}, new String[]{prodItem.getLatestItemRevision().getProperty(PropertyConstant.ATTR_NAME_PROJCODE).substring(0, 2) + "*"});

			TCComponentItem ospecItem = null;
			String ospecID = "";
			ospecMap.clear();
//			comboOspec.removeAllItems();
//			comboOspecRev.removeAllItems();
			
			ArrayList<String> ospecList = new ArrayList<String>(com.length);
			for(int i=0; i<com.length; i++){
				ospecItem = (TCComponentItem)com[i];
				ospecID = ospecItem.getProperty(PropertyConstant.ATTR_NAME_ITEMID);
//				comboOspec.addItem(ospecID);
				ospecList.add(ospecID);
				if (!ospecMap.containsKey(ospecID)){
					ospecMap.put(ospecID, ospecItem);
				}
			}
			Collections.sort(ospecList);
			comboOspec.addItem("");
			for(String ospec : ospecList){
				comboOspec.addItem(ospec);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected void ospecChangeAction(String selectedOspec) {
		try {
			((DefaultListModel) comboVariant.getModel()).clear();
			comboOspecRev.removeAllItems();
			if (selectedOspec == null || selectedOspec.equals("")) {
				return;
			}
			
			TCComponentItem ospecItem = ospecMap.get(selectedOspec);
			TCComponent[] revComps = ospecItem.getRelatedComponents("revision_list");
			
			TCComponentItemRevision ospecRev = null;
			String ospecRevId = "";
			ospecRevMap.clear();
//			comboOspecRev.removeAllItems();
			ArrayList<String> ospecRevList = new ArrayList<String>(revComps.length);
			for(int i=0; i<revComps.length; i++){
				ospecRev = (TCComponentItemRevision)revComps[i];
				ospecRevId = ospecRev.getProperty(PropertyConstant.ATTR_NAME_ITEMREVID);
//				comboOspecRev.addItem(ospecRevId);
				ospecRevList.add(ospecRevId);
				if (!ospecRevMap.containsKey(ospecRevId)){
					ospecRevMap.put(ospecRevId, ospecRev);
				}
			}
			Collections.sort(ospecRevList);
			comboOspecRev.addItem("");
			for(String ospecR : ospecRevList){
				comboOspecRev.addItem(ospecR);
			}
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	protected void ospecRevChangeAction(String selectedOspecRev) {
		try {
			TCComponentItem prodItem = productMap.get(comboProduct.getSelectedItem().toString());
			TCComponentItemRevision oSpecRevision = ospecRevMap.get(selectedOspecRev);

			//Ospec이 없을때 진행여부 확인
			boolean isContinue = true;
			if(oSpecRevision == null){
				ospec = null;
				int response = ConfirmationDialog.post(AIFUtility.getActiveDesktop(), "Confirm", "Ospec does not exist. Do you want to continue?");
				if (response == ConfirmationDialog.NO) {
					isContinue = false;
				}
			} else {
				ospec = BomUtil.getOSpec(oSpecRevision);
			}
			
			if(isContinue){
				((DefaultListModel) comboVariant.getModel()).clear();
				((DefaultListModel) comboVariant.getModel()).addElement("All");
				
				TCComponentItemRevision prodRev = SYMTcUtil.getLatestReleasedRevision(prodItem);
				AIFComponentContext[] variants = prodRev.getRelated("view");
				HashMap<String, String> variantMap = new HashMap<String, String>();
				if(variants != null && variants.length > 0){
					TCComponentItem variantItem = null;
					String variant = "";
					String trim = "";
					String variantId = "";
					for(AIFComponentContext context : variants){
						if(context.getComponent() instanceof TCComponentItem){
							variantItem = (TCComponentItem)context.getComponent();
							variantId = variantItem.getStringProperty(PropertyConstant.ATTR_NAME_ITEMID);
							variant = variantId+"-"+variantItem.getStringProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
							trim = variantId.substring(1,6);
							variantMap.put(trim, variant);
						}
					}
				}
				//환경기술팀 전원용 책임 요청사항 반영
				//ospec과 동일한 순서대로 variant Item 정렬
				if(ospec != null){
					ArrayList<OpTrim> trimList = ospec.getTrimList();
					String oTrim = "";
					for(OpTrim ot : trimList){
						oTrim = ot.getTrim();
						((DefaultListModel) comboVariant.getModel()).addElement(variantMap.get(oTrim));
					}
				} else {
					Set<String> vSet = variantMap.keySet();
					for (Iterator<String> iterator = vSet.iterator(); iterator.hasNext();) {
						String vKey = iterator.next();
						((DefaultListModel) comboVariant.getModel()).addElement(variantMap.get(vKey));
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	// variant 하위 sos의 옵션들을 가져오는 로직
	public StoredOptionSet getOptionSet(String trim, TCComponent com) throws Exception {
		String sosName = com.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
		StoredOptionSet sos = new StoredOptionSet(trim+ "\n" + sosName);
		SYMCRemoteUtil remote = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("PUID", com.getUid());
		ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.MasterListService", "getStoredOptionSet", ds);
		String option = "";
		String optionValue = "";
		for (int i = 0; list != null && i < list.size(); i++) {
			HashMap<String, String> resultMap = list.get(i);
			option = resultMap.get("POPTION");
			optionValue = resultMap.get("PSTRING_VALUE");
			sos.add(option, optionValue);
		}
		return sos;
	}

	// variant 하위 sos의 옵션들을 가져오는 로직
	public StoredOptionSet setOptionSet(TCComponentItemRevision productRevision) throws Exception {
		AIFComponentContext[] relatedContexts = productRevision.getRelated("IMAN_reference");
		StoredOptionSet sos = null;
		for (AIFComponentContext context : relatedContexts) {
			TCComponent com = (TCComponent) context.getComponent();
			if (com.getType().equals("StoredOptionSet")) {
				String sosName = com.getProperty(PropertyConstant.ATTR_NAME_ITEMNAME);
				sos = new StoredOptionSet(sosName);
				SYMCRemoteUtil remote = new SYMCRemoteUtil();
				DataSet ds = new DataSet();
				ds.put("PUID", com.getUid());
				ArrayList<HashMap<String, String>> list = (ArrayList<HashMap<String, String>>) remote.execute("com.ssangyong.service.MasterListService", "getStoredOptionSet", ds);
				for (int i = 0; list != null && i < list.size(); i++) {
					HashMap<String, String> resultMap = list.get(i);
					sos.add(resultMap.get("POPTION"), resultMap.get("PSTRING_VALUE"));
				}
			}
		}
		return sos;
	}

	public TCComponentItemRevision getOSpecRevision(TCComponentItem product) throws Exception {
		TCComponent[] com = CustomUtil.queryComponent("SYMC_Search_OspecSet_Revision", new String[]{"Project"}, new String[]{product.getLatestItemRevision().getProperty(PropertyConstant.ATTR_NAME_PROJCODE)});
		TCComponentItemRevision ospecRev = null;
		TCComponentItem ospecItem = null;
		if( com != null && com.length > 0){
			ospecItem = ((TCComponentItemRevision)com[0]).getItem();
			ospecRev = ospecItem.getLatestItemRevision();
		}
		return ospecRev;
	}

	public class CustomTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;

		public CustomTableModel(String[] columnHeaders, int defaultRowCount) {
			super(columnHeaders, defaultRowCount);
		}

		public Vector<Object> getIdentifier() {
			return columnIdentifiers;
		}
	}

	public class CustomCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			JLabel label = (JLabel) com;

			if (column == 5) {
				label.setHorizontalAlignment(SwingConstants.LEFT);
			} else {
				label.setHorizontalAlignment(SwingConstants.CENTER);
			}

			if (column == 0) {
				label.setText(row + "");
			}

			if (isSelected) {
				label.setBackground(table.getSelectionBackground());
				label.setForeground(table.getSelectionForeground());
			} else {
				if (table.getRowCount() > 0 && table.getValueAt(row, 8) != null && table.getValueAt(row, 8).equals("Total Sum Weight")) {
					label.setBackground(Color.LIGHT_GRAY);
					label.setForeground(table.getForeground());
				} else {
					try {
						label.setBackground(table.getBackground());
						label.setForeground(table.getForeground());
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
			return com;
		}
	}

	public OSpec getOspec(){
		return ospec;
	}

	public TCComponentItem getProductItem(){
		return selectedProductItem;
	}

	public ArrayList getVariantItemList(){
		return variantItemList;
	}

	public HashMap<String, HashMap<String, String>> getProductAllChildPartsList(){
		return usageListOperation.getProductAllChildPartsList();
	}

	public HashMap<String, HashMap<String, String>> getProductAllChildPartsUsageList(){
		return usageListOperation.getProductAllChildPartsUsageList();
	}
	
	public String getRevRule(){
		return comboRevRule.getSelectedItem().toString();
	}
}
