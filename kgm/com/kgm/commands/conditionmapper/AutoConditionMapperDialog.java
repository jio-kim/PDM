package com.kgm.commands.conditionmapper;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

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
import net.miginfocom.swing.MigLayout;

import com.kgm.commands.optiondefine.VariantOptionDefinitionDialog;
import com.kgm.commands.variantoptioneditor.tree.VariantNode;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.ui.CheckComboBox;
import com.kgm.common.ui.MultiLineToolTip;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMStringUtil;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.util.Registry;

@SuppressWarnings({"serial", "rawtypes", "unchecked", "unused"})
public class AutoConditionMapperDialog extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private ArrayList<VariantOption> enableOptionSet = null;
	private TCComponentBOMLine target = null;
	private TCComponentBOMLine optionOwner = null;
	
	private JTree tree = null;	//옵션 트리(Function)
	private JList selectedOptions = null;
	private JList positiveFilterList = null;	//XX 인 경우
	private JList negativeFilterList = null;	//XX가 아닌 경우
	
	//필터를 위한 Combo Box(클릭시 확장되는 TextField가 Combo보다 크다.)
	private CheckComboBox[] filterCombo = null;
	
	private HashMap<VariantValue, ArrayList<VariantValue>> positiveFilterMap = new HashMap();
	private HashMap<VariantValue, ArrayList<VariantValue>> negativeFilterMap = new HashMap();
	private HashMap<VariantValue, VariantOption> filteredVariantValues = new HashMap();
	private JTable resultConditionTable;
	private JCheckBox checkedCondition;
	private Vector<VariantValueVector> allConditions = new Vector();
	private Vector headerVector = new Vector();
	
	private JLabel countLabel = null;	//경우의 수
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			AutoConditionMapperDialog dialog = new AutoConditionMapperDialog(null, null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public AutoConditionMapperDialog(ArrayList<VariantOption> enableOptionSet, TCComponentBOMLine target, TCComponentBOMLine optionOwner) {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setTitle("Auto Condition Generator");
		
		this.enableOptionSet = enableOptionSet;
		this.target = target;
		this.optionOwner = optionOwner;
		
		contentPanel.setPreferredSize(new Dimension(900, 500));
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JSplitPane splitPane = new JSplitPane();
			splitPane.setDividerSize(15);
			splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
			splitPane.setOneTouchExpandable(true);
			contentPanel.add(splitPane, BorderLayout.CENTER);
			{
				JPanel filterPanel = new JPanel();
				filterPanel.setPreferredSize(new Dimension(10, 350));
				filterPanel.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel = new JPanel();
					filterPanel.add(panel, BorderLayout.WEST);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_1 = new JPanel();
						panel.add(panel_1, BorderLayout.CENTER);
						panel_1.setPreferredSize(new Dimension(300, 300));
						panel_1.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Enable Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
						{
							JScrollPane pane = new JScrollPane();
							pane.setPreferredSize(new Dimension(180, 2));
							pane.setViewportView(initTree());
							pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
							pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							pane.getViewport().setBackground(Color.WHITE);
							panel_1.setLayout(new BorderLayout(0, 0));
							panel_1.add(pane);
						}
					}
					{
						JPanel panel_1 = new JPanel();
						panel.add(panel_1, BorderLayout.EAST);
						panel_1.setLayout(new MigLayout("", "[25px]", "[23px][][][][][][][][][]"));
						{
							JButton btnNewButton_1 = new JButton();
							Registry registry = Registry.getRegistry(com.kgm.commands.variantoptioneditor.OptionSetDialog.class);
							btnNewButton_1.setIcon(registry.getImageIcon("ProuctOptionManageForwardArrow2.ICON"));
							btnNewButton_1.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									addToSelectedOptions();
								}
							});
							btnNewButton_1.setPreferredSize(new Dimension(25, 23));
							panel_1.add(btnNewButton_1, "cell 0 4,alignx left,aligny top");
						}
						{
							JButton btnNewButton_2 = new JButton();
							Registry registry = Registry.getRegistry(com.kgm.commands.variantoptioneditor.OptionSetDialog.class);
							btnNewButton_2.setIcon(registry.getImageIcon("ProuctOptionManageBackArrow2.ICON"));
							btnNewButton_2.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									removeFromSelectedOptions();
								}
							});
							btnNewButton_2.setPreferredSize(new Dimension(25, 23));
							panel_1.add(btnNewButton_2, "cell 0 5");
						}
					}
					
				}
				{
					JPanel panel_1 = new JPanel();
					panel_1.setPreferredSize(new Dimension(400, 300));
					panel_1.setBorder(new TitledBorder(null, "Filter", TitledBorder.LEADING, TitledBorder.TOP, null, null));
					filterPanel.add(panel_1, BorderLayout.CENTER);
					panel_1.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel = new JPanel();
						panel.setPreferredSize(new Dimension(150, 10));
						panel.setBorder(new TitledBorder(null, "Selected Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
						panel_1.add(panel, BorderLayout.WEST);
						panel.setLayout(new BorderLayout(0, 0));
						{
							selectedOptions = new JList();
							selectedOptions.setCellRenderer(new DefaultListCellRenderer(){

								@Override
								public Component getListCellRendererComponent(
										JList jlist, Object obj, int i,
										boolean flag, boolean flag1) {
									
									if( obj instanceof VariantOption){
										Component com = super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
										JLabel label = (JLabel)com;
										VariantOption option = (VariantOption)obj;
										label.setText(option.getOptionName() + " | " + option.getOptionDesc());
										return label;
									}else{
										return super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
									}
								}

								
							});
							
							selectedOptions.addMouseListener(new MouseAdapter(){

								@Override
								public void mouseReleased(MouseEvent e) {
									if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
											&& e.isControlDown()==false) {
										removeFromSelectedOptions();
									}
									super.mouseReleased(e);
								}
								
							});
							selectedOptions.setModel(new DefaultListModel());
							
							JScrollPane pane = new JScrollPane();
							pane.setPreferredSize(new Dimension(80, 2));
							pane.setViewportView(selectedOptions);
							pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
							pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
							pane.getViewport().setBackground(Color.WHITE);
							
							panel.add(pane);
						}
					}
					{
						JPanel panel = new JPanel();
						panel_1.add(panel);
						panel.setLayout(new GridLayout(2, 0, 0, 0));
						{
							JPanel panel_3 = new JPanel();
							panel.add(panel_3);
							panel_3.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Positive Filter", TitledBorder.LEADING, TitledBorder.TOP, null, null));
							panel_3.setLayout(new BorderLayout(0, 0));
							{
								JPanel panel_2 = new JPanel();
								FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
								flowLayout.setAlignment(FlowLayout.RIGHT);
								panel_3.add(panel_2, BorderLayout.NORTH);
								{
									JButton positiveAddBtn = new JButton("Add");
									positiveAddBtn.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											FilterConditionDialog dlg = new FilterConditionDialog(AutoConditionMapperDialog.this, selectedOptions.getModel(), true);
											dlg.setVisible(true);
										}
									});
									panel_2.add(positiveAddBtn);
								}
								{
									JButton positiveRemoveBtn = new JButton("Remove");
									positiveRemoveBtn.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											/**
											 * [20150119][jclee] TC10 Upgrade.
											 * Remove 불가 현상 대응.
											 */
//											Vector list = (Vector)positiveFilterList.getSelectedValue();
											Object oList = positiveFilterList.getSelectedValue();
											Vector list = new Vector<VariantValue>((ArrayList<VariantValue>)oList);
											
											DefaultListModel model = (DefaultListModel)positiveFilterList.getModel();
											model.removeElement(list);
											positiveFilterMap.remove(list.get(0));
											System.out.println("positiveFilterMap size " + positiveFilterMap.size());
										}
									});
									panel_2.add(positiveRemoveBtn);
								}
							}
							{
								positiveFilterList = new JList();
								positiveFilterList.setCellRenderer(new DefaultListCellRenderer(){

									@Override
									public Component getListCellRendererComponent(
											JList jlist, Object obj, int i,
											boolean flag, boolean flag1) {
										JLabel label = (JLabel)super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
										
										List list = (List)obj;
										VariantValue reqValue = (VariantValue)list.get(0);
										VariantOption reqOption = reqValue.getOption();
										Boolean positiveObj = (Boolean)list.get(1);
										VariantValue resValue = (VariantValue)list.get(2);
										VariantOption resOption = resValue.getOption();
										
										String str = null;
										if( positiveObj.booleanValue()){
											str = reqOption.getOptionName() + "=" + reqValue.getValueName() + " THEN "
													+ resOption.getOptionName() + "=" + resValue.getValueName();
										}else{
											str = reqOption.getOptionName() + "!=" + reqValue.getValueName() + " THEN "
													+ resOption.getOptionName() + "=" + resValue.getValueName();
										}
										label.setText(str);
										return label;
									}
									
								});
								positiveFilterList.setModel(new DefaultListModel());
								JScrollPane pane = new JScrollPane();
								pane.setViewportView(positiveFilterList);
								pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
								pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
								pane.getViewport().setBackground(Color.WHITE);
								panel_3.add(pane, BorderLayout.CENTER);
							}
						}
						{
							JPanel panel_3 = new JPanel();
							panel.add(panel_3);
							panel_3.setBorder(new TitledBorder(null, "Negative Filter", TitledBorder.LEADING, TitledBorder.TOP, null, null));
							panel_3.setLayout(new BorderLayout(0, 0));
							{
								JPanel panel_2 = new JPanel();
								FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
								flowLayout.setAlignment(FlowLayout.RIGHT);
								panel_3.add(panel_2, BorderLayout.NORTH);
								{
									JButton negativeAddBtn = new JButton("Add");
									negativeAddBtn.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											FilterConditionDialog dlg = new FilterConditionDialog(AutoConditionMapperDialog.this, selectedOptions.getModel(), false);
											dlg.setVisible(true);
										}
									});
									panel_2.add(negativeAddBtn);
								}
								{
									JButton negativeRemoveBtn = new JButton("Remove");
									negativeRemoveBtn.addActionListener(new ActionListener() {
										public void actionPerformed(ActionEvent e) {
											/**
											 * [20150119][jclee] TC10 Upgrade.
											 * Remove 불가 현상 대응.
											 */
//											Vector list = (Vector)negativeFilterList.getSelectedValue();
											Object oList = negativeFilterList.getSelectedValue();
											Vector list = new Vector<VariantValue>((ArrayList<VariantValue>)oList);
											
											DefaultListModel model = (DefaultListModel)negativeFilterList.getModel();
											model.removeElement(list);
											negativeFilterMap.remove(list.get(0));
											System.out.println("negativeFilterList size " + negativeFilterMap.size());
										}
									});
									panel_2.add(negativeRemoveBtn);
								}
							}
							{
								negativeFilterList = new JList();
								negativeFilterList.setCellRenderer(new DefaultListCellRenderer(){

									@Override
									public Component getListCellRendererComponent(
											JList jlist, Object obj, int i,
											boolean flag, boolean flag1) {
										JLabel label = (JLabel)super.getListCellRendererComponent(jlist, obj, i, flag, flag1);
										
										List list = (List)obj;
										VariantValue reqValue = (VariantValue)list.get(0);
										VariantOption reqOption = reqValue.getOption();
										Boolean positiveObj = (Boolean)list.get(1);
										VariantValue resValue = (VariantValue)list.get(2);
										VariantOption resOption = resValue.getOption();
										
										String str = null;
										if( positiveObj.booleanValue()){
											str = reqOption.getOptionName() + "=" + reqValue.getValueName() + " THEN "
													+ resOption.getOptionName() + "=" + resValue.getValueName();
										}else{
											str = reqOption.getOptionName() + "=" + reqValue.getValueName() + " THEN "
													+ resOption.getOptionName() + "!=" + resValue.getValueName();
										}
										label.setText(str);
										return label;
									}
									
								});
								negativeFilterList.setModel(new DefaultListModel());
								JScrollPane pane = new JScrollPane();
								pane.setViewportView(negativeFilterList);
								pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
								pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
								pane.getViewport().setBackground(Color.WHITE);
								panel_3.add(pane, BorderLayout.CENTER);
							}
						}
					}
					{
						JPanel panel = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel.getLayout();
						flowLayout.setAlignment(FlowLayout.RIGHT);
						panel_1.add(panel, BorderLayout.SOUTH);
						{
							JButton calBtn = new JButton("Load Enable Conditions");
							calBtn.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									
									if( selectedOptions.getModel().getSize() < 1) return;
									
									final WaitProgressBar waitProgress = new WaitProgressBar(AutoConditionMapperDialog.this);
									waitProgress.start();
									
									AutoConditionMapperOperation operation = new AutoConditionMapperOperation(AutoConditionMapperDialog.this, waitProgress);
									TCSession session = AutoConditionMapperDialog.this.target.getSession();
									session.queueOperation(operation);
									
								}
							});
							panel.add(calBtn);
						}
					}
				}
				splitPane.setLeftComponent(filterPanel);
			}
			{
				JPanel resultPanel = new JPanel();
				resultPanel.setPreferredSize(new Dimension(10, 100));
				resultPanel.setBorder(new TitledBorder(null, "Result Condition", TitledBorder.LEADING, TitledBorder.TOP, null, null));
				splitPane.setRightComponent(resultPanel);
				resultPanel.setLayout(new BorderLayout(0, 0));
				{
					resultConditionTable = new JTable();
					resultConditionTable.setModel(new DefaultTableModel());
					JScrollPane pane = new JScrollPane();
					pane.setViewportView(resultConditionTable);
					pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
					pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
					pane.getViewport().setBackground(Color.WHITE);
					resultPanel.add(pane);
				}
				{
					JPanel panel = new JPanel();
					resultPanel.add(panel, BorderLayout.NORTH);
					panel.setLayout(new BorderLayout(0, 0));
					{
						JPanel panel_1 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.LEADING);
						panel.add(panel_1, BorderLayout.CENTER);
						{
							filterCombo = new CheckComboBox[7];
							for( int i = 0; i < filterCombo.length; i++){
								filterCombo[i] = new CheckComboBox("Please select Options"){

									@Override
									public JToolTip createToolTip() {
										MultiLineToolTip tip = new MultiLineToolTip();
								        tip.setComponent(this);
								        return tip;
									}
									
								};
								filterCombo[i].setPreferredSize(new Dimension(90, 20));
								filterCombo[i].setPopupWidth(300);
								filterCombo[i].addActionListener (new ActionListener () {
								    public void actionPerformed(ActionEvent e) {
								    	
								    	filter();
								    }
								});
								filterCombo[i].addMouseListener(new MouseAdapter(){

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
								
								
								panel_1.add(filterCombo[i]);
							}
							
						}
					}
					{
						JPanel panel_1 = new JPanel();
						FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
						flowLayout.setAlignment(FlowLayout.RIGHT);
						panel.add(panel_1, BorderLayout.EAST);
						{
							checkedCondition = new JCheckBox("Show checked condition");
							checkedCondition.addActionListener(new ActionListener(){

								@Override
								public void actionPerformed(ActionEvent arg0) {
									filter();
								}
								
							});
							panel_1.add(checkedCondition);
						}
					}
				}
			}
		}
		{
			JPanel buttonPane = new JPanel();
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			buttonPane.setLayout(new GridLayout(0, 3, 0, 0));
			{
				JPanel panel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel.getLayout();
				flowLayout.setAlignment(FlowLayout.LEADING);
				buttonPane.add(panel);
				{
					JButton okButton = new JButton("Apply to a BOM line");
					panel.add(okButton);
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							
							final int[] selectedRows = resultConditionTable.getSelectedRows();
							
							if( selectedRows == null || selectedRows.length < 1){
								return;
							}
							
							final WaitProgressBar waitProgress = new WaitProgressBar(AutoConditionMapperDialog.this);
							waitProgress.start();
							
							TCSession session = AutoConditionMapperDialog.this.target.getSession();
							session.queueOperation(new AbstractAIFOperation() {
								
								@Override
								public void executeOperation() throws Exception {
									String lineMvl = "";
									
									try{
										
										TableRowSorter sorter = (TableRowSorter)resultConditionTable.getRowSorter();
										DefaultTableModel model = (DefaultTableModel)resultConditionTable.getModel();
										Vector conditionData = model.getDataVector();
										
										TCSession session = CustomUtil.getTCSession();
										ArrayList<VariantValueVector> list = new ArrayList();
										for( int i = 0; selectedRows != null && i < selectedRows.length; i++){
											int modelRowIdx = sorter.convertRowIndexToModel(selectedRows[i]);
											VariantValueVector row = (VariantValueVector)conditionData.get(modelRowIdx);
											String tmpStr = row.toString();
											
											//SRME:: [][20140812] 특정 category는 다른 특정 category랑 함께 쓸수없게 제한(special country)
											boolean bFlag = CustomUtil.isCompatibleOptions(session, tmpStr, false);
											if( !bFlag ){
												waitProgress.setStatus("This option includes incompatible.");
												waitProgress.setShowButton(true);
												return;
											}
											list.add(row);

										}
										
										/**
										 * SRME:: [][20140708] swyoon 옵션 Sorting.
										 */	
										Collections.sort(list);
										for( int i = 0; i < list.size(); i++){
											String tmpStr = "";
											VariantValueVector row = (VariantValueVector)list.get(i);
											for(int j = 1; j < row.size(); j++){
												Object obj = row.get(j);
												if(  obj instanceof VariantValue ){
													VariantValue value = (VariantValue)row.get(j);
													VariantOption option = value.getOption();
													tmpStr += ( !tmpStr.equals("") ? " and ":"") + option.getItemId() + ":" + MVLLexer.mvlQuoteId(option.getOptionName(), false) + " = " +  MVLLexer.mvlQuoteString(value.getValueName());
												}
											}
											
											lineMvl += ( !lineMvl.equals("") ? " or ":" ") + tmpStr;
										}
										
										/**
										 *  옵션 사이즈 체크.
										 * SRME:: [][20140708] swyoon 옵션 사이즈 체크.
										 */										
										//옵션 사이즈 체크.
										int convertedLength = SYMStringUtil.getConvertedLength(lineMvl);
										if( convertedLength > 4000){
											throw new TCException("Option length limit is exceeded.");
										}
										
										if( lineMvl != null && !lineMvl.equals("")){
											InterfaceAIFComponent[] coms = CustomUtil.getTargets();
											for( int i = 0; coms != null && i < coms.length; i++){
												if( coms[i] instanceof TCComponentBOMLine){
													TCComponentBOMLine line = ((SYMCBOMLine)coms[i]);
													TCComponentBOMLine parent = line.parent();
													TCComponentBOMLine foundLine = null;
													while( parent != null ){
														if( parent.equals(AutoConditionMapperDialog.this.optionOwner)){
															foundLine = parent;
															break;
														}
														parent = parent.parent();
													}
													if( foundLine != null){
														waitProgress.setStatus("Apply MVL to " + line.toDisplayString());
														((SYMCBOMLine)coms[i]).refresh();
														((SYMCBOMLine)coms[i]).setMVLCondition(lineMvl);
													}else{
														throw new TCException("Disable BOM line");
													}
													
												}
											}
											
											waitProgress.close();
										}
										
									}catch(TCException tce){
										tce.printStackTrace();
										waitProgress.setStatus(tce.getDetailsMessage());
										waitProgress.setShowButton(true);
									}
								}
							});
							
							
							
						}
					});
					okButton.setActionCommand("OK");
					getRootPane().setDefaultButton(okButton);
				}
			}
			{
				JPanel panel = new JPanel();
				buttonPane.add(panel);
				{
					countLabel = new JLabel("0/0");
					panel.add(countLabel);
				}
			}
			{
				JPanel panel = new JPanel();
				FlowLayout flowLayout = (FlowLayout) panel.getLayout();
				flowLayout.setAlignment(FlowLayout.RIGHT);
				buttonPane.add(panel);
				{
					JButton cancelButton = new JButton("Close");
					cancelButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							AutoConditionMapperDialog.this.dispose();
						}
					});
					{
						JButton btnExcelExport = new JButton("Excel Export");
						btnExcelExport.setIcon(new ImageIcon(VariantOptionDefinitionDialog.class.getResource("/com/ssangyong/common/images/excel_16.png")));
						panel.add(btnExcelExport);
						btnExcelExport.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								
								if( resultConditionTable.getRowCount() < 1) return;
								
								JFileChooser fileChooser = new JFileChooser();
								fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
								Calendar now = Calendar.getInstance();
								SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
								sdf.format(now.getTime());
								File defaultFile = new File("Auto_Condition_Mapper_" + sdf.format(now.getTime()) + ".xls");
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
								int result = fileChooser.showSaveDialog(AutoConditionMapperDialog.this);
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
//						            	if( selectedFile != null ){
//						            		OptionSetDialog.this.dispose();
//						            	}
						            }
								}
							}
						});
					}
					panel.add(cancelButton);
					cancelButton.setActionCommand("Cancel");
				}
			}
		}
	}

	/**
	 * 상위 아이템이 설정된 모든 옵션을 Tree로 셋팅한다. 
	 * @return
	 */
	private JTree initTree(){
		
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Option Set");
		if( this.enableOptionSet != null && this.enableOptionSet.size() > 0){
			for( VariantOption option : this.enableOptionSet){
				
				String desc = option.getOptionDesc() == null || option.getOptionDesc().equals("") ? "" : " | " + option.getOptionDesc();
				VariantNode optionNode = new VariantNode(option);
				List<VariantValue> values = option.getValues();
				
				//사용가능한 옵션이 존재하는경우만 Option을 추가한다.
				if( values != null && !values.isEmpty() ){
					int enableChildCount = 0;
					for( VariantValue value : values){
						if( value.getValueStatus() == VariantValue.VALUE_USE){
							enableChildCount++;
						}
					}
					if( enableChildCount > 0 ){
						root.add(optionNode);
					}
				}
			}
		}
		
		tree = new JTree(root);
		for( int i = 0; i < tree.getRowCount(); i++){
			tree.expandRow(i);
		}
		
		//더블 클릭시 조합식을 위해 추가함.
		tree.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseReleased(MouseEvent e) {
				if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
						&& e.isControlDown()==false) {
					addToSelectedOptions();
				}
				super.mouseReleased(e);
			}
			
		});
		return tree;
	}
	
	
	/**
	 * 0/0 형태의 레이블 리턴
	 * 
	 * @return
	 */
	public JLabel getCountLabel() {
		return countLabel;
	}

	/**
	 * >>   Enable Option에서 Selected Options으로 옵션 추가 
	 */
	private void addToSelectedOptions(){
		TreePath[] paths = tree.getSelectionPaths();
		for( int i = 0; paths != null && i < paths.length; i++){
			TreePath path = paths[i];
			VariantNode node = (VariantNode)path.getLastPathComponent();
			Object obj = node.getUserObject();
			DefaultListModel model = (DefaultListModel)selectedOptions.getModel();
			if( obj instanceof VariantValue){
				
				VariantValue value = (VariantValue)obj;
				VariantOption option = value.getOption();
				if( !model.contains(option)){
					
					model.addElement(option);
					
					ArrayList list = new ArrayList();
					Enumeration enums = model.elements();
					while(enums.hasMoreElements()){
						list.add(enums.nextElement());
					}
					Collections.sort(list);
					model.clear();
					for( int j = 0; j < list.size(); j++){
						model.addElement(list.get(j));
					}
					
				}
			}else if( obj instanceof VariantOption){
				VariantOption option = (VariantOption)obj;
				if( !model.contains(option)){
					model.addElement(option);
					
					ArrayList list = new ArrayList();
					Enumeration enums = model.elements();
					while(enums.hasMoreElements()){
						list.add(enums.nextElement());
					}
					Collections.sort(list);
					model.clear();
					for( int j = 0; j < list.size(); j++){
						model.addElement(list.get(j));
					}					
				}
			}
		}
		selectedOptions.repaint();
	}
	
	/**
	 * <<   Selected Options에서 옵션 제거 
	 */
	private void removeFromSelectedOptions(){
		//Object[] objects = selectedOptions.getSelectedValues();
		Object[] objects = selectedOptions.getSelectedValuesList().toArray();
		DefaultListModel model = (DefaultListModel)selectedOptions.getModel();
		if( objects != null && objects.length > 0){
			for( Object obj : objects){
				model.removeElement(obj);
			}
		}
		
		model = (DefaultListModel)positiveFilterList.getModel();
		model.removeAllElements();
		positiveFilterMap.clear();
		
		model = (DefaultListModel)negativeFilterList.getModel();
		model.removeAllElements();
		negativeFilterMap.clear();
	}
	
	/**
	 * 현재 선택된 옵션들을 리턴
	 * 
	 * @return
	 */
	public JList getSelectedOptions() {
		return selectedOptions;
	}

	/**
	 * Positive Filter 리스트
	 * @return
	 */
	public JList getPositiveFilterList() {
		return positiveFilterList;
	}
	
	/**
	 * Negative Filter 리스트
	 * @return
	 */
	public JList getNegativeFilterList() {
		return negativeFilterList;
	}

	
	/**
	 * Positive and Negative Filter를 거친 경우의 수..
	 * @return
	 */
	public JTable getResultConditionTable() {
		return resultConditionTable;
	}

	/**
	 * 경우의 수를 출력하기 위해 FilterConditionDialog에 선택된 옵션들을 Positive 또는 Negative List로 추가.
	 * @param isPositive
	 * @param filterList
	 */
	void addFilter(boolean isPositive, List filterList){
		if( isPositive){
			DefaultListModel model = (DefaultListModel)positiveFilterList.getModel();
			model.addElement(filterList);
		}else{
			DefaultListModel model = (DefaultListModel)negativeFilterList.getModel();
			model.addElement(filterList);
		}
	}
	
	/**
	 * 결과에 해당하는 경우의 수를 다시 필터 할 수 있는 Combo추가.
	 * @param optionCodes
	 */
	void setComboFilter(ArrayList<VariantValue> optionCodes){
		
		for(CheckComboBox combo : filterCombo){
			combo.removeAllItems();
			combo.addItem("");
		}
		
		HashSet set = new HashSet();
		set.addAll(optionCodes);
		for(CheckComboBox combo : filterCombo){
			combo.resetObjs(set, false);
		}
	}
	
	/**
	 * 경우의 수 결과물을 리턴
	 * 
	 * @param bOnlyChecked	true : 체크된 것만 리턴,	false : 모든 데이타를 리턴
	 * @return
	 */
	private Vector getData(boolean bOnlyChecked){
		
		Vector newData = new Vector();
		if( bOnlyChecked ){
			for( VariantValueVector row : allConditions){
				Object obj = row.get(0);
				if( obj instanceof Boolean){
					Boolean bCheck = (Boolean)obj;
					if(bCheck.booleanValue()){
						
						boolean bInclude = true;
						for( CheckComboBox combo : filterCombo){
							Object[] selectedItems = combo.getSelectedItems();
							if( selectedItems != null && selectedItems.length > 0){
								
								boolean bInclude2 = false;
								for( Object item : selectedItems){
									if( row.contains(item)){
										bInclude2 = true;
										break;
									}
								}
								
								if( !bInclude2 ){
									bInclude = false;
									break;
								}
								
							}else{
								bInclude &= true;
							}
						}
						
						if( bInclude ){
							newData.add(row);
						}
						
					}
				}
				
			}
		}else{
			for( VariantValueVector row : allConditions){
				
				boolean bInclude = true;
				for( CheckComboBox combo : filterCombo){
					Object[] selectedItems = combo.getSelectedItems();
					if( selectedItems != null && selectedItems.length > 0){
						
						boolean bInclude2 = false;
						for( Object item : selectedItems){
							if( row.contains(item)){
								bInclude2 = true;
								break;
							}
						}
						
						if( !bInclude2 ){
							bInclude = false;
							break;
						}
						
					}else{
						bInclude &= true;
					}
				}
				
				if( bInclude ){
					newData.add(row);
				}
			}
		}
		
		return newData;
		
	}
	
	/**
	 * 결과물(Positive, Negative조건에 의한 모든 Condition)을 조건(Filter1...7, Checked)으로 필터함.
	 */
	private void filter(){
		
		Vector data = getData(checkedCondition.isSelected());
		TableModel model = new DefaultTableModel(data, headerVector) {
			public Class getColumnClass(int col) {
				if( col == 0){
					return Boolean.class;
				}else if( col == 1){
					return String.class;
				}else{
					return  VariantValue.class;
				}
				
			}

			public boolean isCellEditable(int row, int col) {
				if( col == 0 || col == 1)
					return true;
				else 
					return false;
			}
	    };
	    resultConditionTable.setModel(model);
	    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
	    resultConditionTable.setRowSorter(sorter);
	    resultConditionTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    
	    TableColumnModel columnModel = resultConditionTable.getColumnModel();
		int n = headerVector.size();
		int columnWidth = 0; 
		for (int i = 0; i < n; i++) {
			if( i == 0 ){
				columnWidth = 25;
			}else{
				columnWidth = 45;
			}
			columnModel.getColumn(i).setPreferredWidth(columnWidth);
			columnModel.getColumn(i).setWidth(columnWidth);
		}

		
		SimpleValueCellRenderer cellRenderer = new SimpleValueCellRenderer();
		for( int i = 1; i < columnModel.getColumnCount(); i++){
			TableColumn column = columnModel.getColumn(i);
			column.setCellRenderer(cellRenderer);
		}
		
		countLabel.setText(data.size() + "/" + allConditions.size());
		countLabel.repaint();
	}
	
	/**
	 * Positive, Negative조건에 의한 모든 Condition
	 * 
	 * @param allCondition
	 */
	void setConditionData(ArrayList<VariantValueVector> allCondition){
		if( allCondition == null || allCondition.isEmpty()) return;
		
		checkedCondition.setSelected(false);
		VariantValueVector vvList = allCondition.get(0);
		
		headerVector.clear();
		for( int i = 0; i < vvList.size(); i++){
			Object obj = vvList.get(i);
			if( obj instanceof VariantValue){
				VariantValue value = (VariantValue)vvList.get(i);
				headerVector.add(value.getOption().getOptionName());
			}else if( obj instanceof Boolean){
				headerVector.add("");
			}else if( obj instanceof String){
				headerVector.add("OR");
			}
		}
		
		allConditions.clear();
		allConditions.addAll(allCondition);
	    
	}
	
	/**
	 * 현재 보여지는 결과물을 Excel로 Export 함.
	 * OR조건으로 묶기위해 동일 숫자가 입력된 Row는 셀병합을 함.
	 * @param selectedFile
	 * @throws IOException
	 * @throws WriteException
	 */
	protected void exportToExcel(File selectedFile) throws IOException,
			WriteException {
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
		// 0번째 Sheet 생성
		WritableSheet sheet = workBook.createSheet("new sheet", 0);

		WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을
																	// 지정하기 위한
																	// 부분입니다.
		cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을
																// 지정합니다. 테두리에
																// 라인그리는거에요
		cellFormat.setWrap(true);
		Label label = null;

		WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의
																		// 스타일을
																		// 지정하기
																		// 위한
																		// 부분입니다.
		headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
		headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

		int startRow = 1;
		int initColumnNum = 0;

		label = new jxl.write.Label(1, startRow, "Auto Condition  Mapper",
				cellFormat);
		sheet.addCell(label);
		sheet.mergeCells(1, 1, 3, 1);

		TableColumnModel cm = resultConditionTable.getColumnModel();
		startRow = 3;
		Vector excelColumnHeader = new Vector();
		for (int i = 1; i < cm.getColumnCount(); i++) {
			TableColumn tc = cm.getColumn(i);
			excelColumnHeader.add(tc.getHeaderValue());
		}

		for (int i = 0; i < excelColumnHeader.size() + 2; i++) {
			String str = null;
			if (i == excelColumnHeader.size()) {
				str = "Condition";
			} else if (i == excelColumnHeader.size() + 1) {
				str = "Condition String";
			} else {
				str = excelColumnHeader.get(i).toString();
			}
			label = new jxl.write.Label(i + initColumnNum + 1, startRow, str,
					headerCellFormat);
			sheet.addCell(label);
			CellView cv = sheet.getColumnView(i + initColumnNum + 1);
			// cv.setAutosize(true);
			if (i == excelColumnHeader.size()) {
				cv.setSize(2500 * excelColumnHeader.size());
			} else if (i == excelColumnHeader.size() + 1) {
				cv.setSize(6000 * excelColumnHeader.size());
			}else{
				cv.setSize(1500);
			}
			sheet.setColumnView(i + initColumnNum + 1, cv);
		}

		startRow = 4;
		DefaultTableModel model = (DefaultTableModel) resultConditionTable
				.getModel();
		
		Vector<Vector> noMergeList = new Vector();
		HashMap<String, Vector<Vector>> mergeMap = new HashMap();
		Vector<Vector> data = model.getDataVector();
		//가져온 데이타를 or조건으로 묶어야 하며, 이를 위해 정렬을 다시 한다.(or조건으로 묶이는 Condition이 위로 오도록함.)
		for (Vector row : data) {
			String mergeKey = (String)row.get(1);
			if( mergeKey != null && !mergeKey.equals("") ){
				Vector<Vector> rows = mergeMap.get(mergeKey);
				if( rows == null){
					rows = new Vector();
				}
				
				if( !rows.contains(row)){
					rows.add(row);
				}
				
				mergeMap.put(mergeKey, rows);
			}else{
				noMergeList.add(row);
			}
		}
		
		//Merge 할 Row를 우선 출력하고, merge를 하지 않는 Row나중에 출력함.
		Set set = mergeMap.keySet();
		ArrayList<String> list = new ArrayList(set);
		Collections.sort(list);
		for( String key : list){
			
			String mConditionStr = "", mRealConditionStr = "";
			Vector<Vector> mergeData = mergeMap.get(key);
			for (int i = 0; i < mergeData.size(); i++) {
				Vector row = mergeData.get(i);
				String conditionStr = "";
				String realConditionStr = "";
				
				//j=0 은 Boolean 값, j=1은 Or 조건으로 셀 병합할 건들.
				for (int j = 1; j < row.size() + 2; j++) {
					String str = "";
					if (j == row.size()) {
						str = conditionStr;
						mConditionStr += (mConditionStr.equals("") ? "" : "\n") + conditionStr;
					} else if (j == row.size() + 1) {
						str = realConditionStr;
						mRealConditionStr += (mRealConditionStr.equals("") ? "" : "\nOR ") + realConditionStr;
					} else {
						Object obj = row.get(j);
						if( obj instanceof VariantValue ){
							VariantValue value = (VariantValue) row.get(j);
							VariantOption option = value.getOption();
							str = value.getValueName();
							conditionStr += (j == 2 ? "" : " AND ") + str;
							realConditionStr += (j == 2 ? "" : " AND ")
									+ option.getItemId()
									+ ":"
									+ MVLLexer.mvlQuoteId(option.getOptionName(), false)
									+ " = "
									+ MVLLexer.mvlQuoteString(value.getValueName());
							
						}else{
							str = (String)obj;
						}
					}

					label = new jxl.write.Label(j + initColumnNum, i + startRow,
							str, cellFormat);
					sheet.addCell(label);
				}
			}
			
			//셀 병합필요.
			sheet.mergeCells(5, startRow, 5, startRow + mergeData.size() - 1);
			label = new jxl.write.Label(5, startRow, mConditionStr, cellFormat);
			sheet.addCell(label);
			
			sheet.mergeCells(6, startRow, 6, startRow + mergeData.size() - 1);
			label = new jxl.write.Label(6, startRow, mRealConditionStr, cellFormat);
			sheet.addCell(label);
			//Strt Row 증가
			startRow += mergeData.size();
		}
		
		for (int i = 0; i < noMergeList.size(); i++) {
			Vector row = noMergeList.get(i);
			String conditionStr = "";
			String realConditionStr = "";
			
			//j=0 은 Boolean 값, j=1은 Or 조건으로 셀 병합할 건들.
			for (int j = 1; j < row.size() + 2; j++) {
				String str = "";
				if (j == row.size()) {
					str = conditionStr;
				} else if (j == row.size() + 1) {
					str = realConditionStr;
				} else {
					Object obj = row.get(j);
					if( obj instanceof VariantValue ){
						VariantValue value = (VariantValue) row.get(j);
						VariantOption option = value.getOption();
						str = value.getValueName();
						conditionStr += (j == 2 ? "" : " AND ") + str;
						realConditionStr += (j == 2 ? "" : " AND ")
								+ option.getItemId()
								+ ":"
								+ MVLLexer.mvlQuoteId(option.getOptionName(), false)
								+ " = "
								+ MVLLexer.mvlQuoteString(value.getValueName());
					}else{
						str = "";
					}
				}

				label = new jxl.write.Label(j + initColumnNum, i + startRow,
						str, cellFormat);
				sheet.addCell(label);
			}
		}

		workBook.write();
		workBook.close();
	}

}
