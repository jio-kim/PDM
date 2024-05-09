package com.kgm.commands.variantconditionset;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import com.kgm.commands.variantoptioneditor.tree.VariantNode;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMStringUtil;
import com.kgm.common.utils.variant.OptionManager;
import com.kgm.common.utils.variant.VariantCheckBoxTableCellEditor;
import com.kgm.common.utils.variant.VariantOption;
import com.kgm.common.utils.variant.VariantValue;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.variants.modularvariants.ConditionElement;
import com.teamcenter.rac.pse.variants.modularvariants.MVLLexer;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
//import com.kgm.common.utils.table.SortableTableModel;


/**
 * 
 * Function�� ������ �ɼ��� �������� ���� ������ BOM line�� ������� �����Ѵ�.
 * 
 * @author slobbie
 *
 */
@SuppressWarnings({"serial", "rawtypes", "unused", "unchecked"})
public class ConditionSetDialog extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable detailTable;
	private ArrayList<VariantOption> enableOptionSet;
	private ArrayList<VariantOption> selectedLineOptionSet;
	private ArrayList<VariantOption> optionSetToDelete = new ArrayList();
	private Registry registry = null;
	private JTree tree;
	private TCComponentBOMLine selectedLine;
	private Vector headerVector = new Vector();
	private int[] columnWidth = {40, 100, 100, 100, 150};
	private VariantNode currentTreeNode = null;
	private OptionManager manager = null;
	private List<ConditionVector> conditions = null;
	private JList combinationResultList = new JList();
	private TCSession session = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ConditionSetDialog dialog = new ConditionSetDialog( null, null, null, null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ConditionSetDialog( ArrayList<VariantOption> enableOptionSet, 
			List<ConditionVector> conditions, TCComponentBOMLine selectedLine, 
			Vector<String[]> userDefineErrorList, OptionManager manager) throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setTitle("Condition Set Dialog");
		setBounds(100, 100, 950, 715);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		this.registry = Registry.getRegistry(this);
		this.enableOptionSet = enableOptionSet;
		this.conditions = conditions;
		this.selectedLine = selectedLine;
		this.manager = manager;
		this.session = selectedLine.getSession();
		initTree();
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBackground(Color.WHITE);
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBackground(Color.WHITE);
			contentPanel.add(panel, BorderLayout.WEST);
			panel.setLayout(new BorderLayout(0, 0));
			{
				JPanel panel_1 = new JPanel();
				panel_1.setBackground(Color.WHITE);
				panel_1.setBorder(new TitledBorder(null, "Enable Options", TitledBorder.LEADING, TitledBorder.TOP, null, null));
//				panel_1.setPreferredSize(new Dimension(200, 300));
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JScrollPane pane = new JScrollPane();
					pane.setPreferredSize(new Dimension(300, 300));
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
			panel.setBackground(Color.WHITE);
//			panel.setPreferredSize(new Dimension(400, 10));
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				headerVector.add("USE");
				headerVector.add("CATEGORY");
				headerVector.add("CATEGORY DESC");
				headerVector.add("OPTION CODE");
				headerVector.add("OPTION DESC");
				
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBackground(Color.WHITE);
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
							
							//�߰� ���̺����� ���� ���̺��� ==> ���
							//1. selectedLineOptionSet���� �ִ��� üũ�Ͽ�, �ִٸ� �� �ɼ��� �����´�.
							//2. �������� �ʴ´ٸ� �ɼ��� ����.
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent arg0) {
									add();
								}
							});
							button.setIcon(registry.getImageIcon("ProuctOptionManageForwardArrow2.ICON"));
							panel_1.add(button);
						}
						{
							JButton button = new JButton(){

								@Override
								public Dimension getPreferredSize() {
									return new Dimension(50, 40);
								}
								
							};
							button.setBackground(Color.WHITE);
							//���� ���̺����� ���� ���̺��� <== ���
							button.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent actionevent) {
									remove();
								}
							});
							button.setIcon(registry.getImageIcon("ProuctOptionManageBackArrow2.ICON"));
							panel_1.add(button);
						}
					}
				}
				
				JPanel panel_1 = new JPanel();
				panel_1.setBackground(Color.WHITE);
				panel.add(panel_1, BorderLayout.CENTER);
				panel_1.setLayout(new BorderLayout(0, 0));
				{
					JPanel panel_2 = new JPanel();
					panel_2.setBackground(Color.WHITE);
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
							JLabel targetBOMLineLabel = new JLabel(selectedLine != null ? selectedLine.toDisplayString():"");
							targetBOMLineLabel.setBackground(Color.WHITE);
							targetBOMLineLabel.setFont(new Font(Font.SERIF, Font.BOLD, 12));
							panel_3.add(targetBOMLineLabel);
						}
					}
				}
				
				TableModel model = new DefaultTableModel(null, headerVector) {
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
			    detailTable = new JTable(model);

			    TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
			    detailTable.setRowSorter(sorter);
			    detailTable.addMouseListener(new MouseAdapter(){

					@Override
					public void mouseReleased(MouseEvent e) {
						if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
								&& e.isControlDown()==false) {
							remove();
						}
						super.mouseReleased(e);
					}
					
				});
			    JScrollPane pane = new JScrollPane();
				pane.setViewportView(detailTable);
				pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
				pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				pane.getViewport().setBackground(Color.WHITE);
			    panel_1.add(pane);
			    columnInit();
			}
			
			JPanel southPanel = new JPanel(new BorderLayout());
			southPanel.setBackground(Color.WHITE);
			JPanel centerButtonPanel = new JPanel();
			centerButtonPanel.setBackground(Color.WHITE);
			southPanel.add(centerButtonPanel, BorderLayout.NORTH);
			
			JButton addBtn = new JButton("Add Row");
			addBtn.setBackground(Color.WHITE);
			addBtn.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// ����� ���� ���̺��� ������ �ɼ��� ��������� �߰���.
					try{
						DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
						ConditionVector condition = ConditionSetDialog.this.manager.getConditionSet(model.getDataVector());
						
						//SRME:: [][20140812] Ư�� category�� �ٸ� Ư�� category�� �Բ� �������� ����(special country)
						if(condition == null){
							MessageBox.post(AIFUtility.getActiveDesktop(), "�߰��� �ɼ� ������ �����ϴ�..", "INFORMATION", MessageBox.ERROR);
							return;
						}
						String tmpStr = condition.toString();
						boolean bFlag = CustomUtil.isCompatibleOptions(session, tmpStr, false);
						if( !bFlag ){
							MessageBox.post(AIFUtility.getActiveDesktop(), "This option includes incompatible.", "INFORMATION", MessageBox.ERROR);
							return;
						}
						
						DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
						if( listModel == null ){
							listModel = new DefaultListModel();
						}
						listModel.addElement(condition);
						
						//[SR140722-022][20140522] Condition Sorting
						//[20140626] YunSungWon. 'Or' Sorting
						Enumeration<ConditionVector> enums = (Enumeration<ConditionVector>)listModel.elements();
						if( enums != null && enums.hasMoreElements()){
							
							ArrayList<ConditionVector> list = new ArrayList();
							while(enums.hasMoreElements()){
								ConditionVector v = enums.nextElement();
								list.add(v);
							}
							Collections.sort(list);
							
							listModel.clear();
							for( ConditionVector v : list){
								listModel.addElement(v);
							}
						}

					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
				
			});
			JButton modifyBtn = new JButton("Modify Row");
			modifyBtn.setBackground(Color.WHITE);
			modifyBtn.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// ����� ���� ���̺��� ������ �ɼ��� ��������� �߰���.
					try{
						DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
						ConditionVector condition = ConditionSetDialog.this.manager.getConditionSet(model.getDataVector());
						
						//SRME:: [][20140812] Ư�� category�� �ٸ� Ư�� category�� �Բ� �������� ����(special country)
						if(condition == null){
							MessageBox.post(AIFUtility.getActiveDesktop(), "�߰��� �ɼ� ������ �����ϴ�..", "INFORMATION", MessageBox.ERROR);
							return;
						}
						String tmpStr = condition.toString();
						boolean bFlag = CustomUtil.isCompatibleOptions(session, tmpStr, false);
						if( !bFlag ){
							MessageBox.post(AIFUtility.getActiveDesktop(), "This option includes incompatible.", "INFORMATION", MessageBox.ERROR);
							return;
						}
						
						DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
						
						//���� ���� �� �߰�
						Object[] selectedObj = combinationResultList.getSelectedValuesList().toArray();
						if(selectedObj.length != 1){
							MessageBox.post(AIFUtility.getActiveDesktop(), "������ �ɼ� ������ �ϳ� �����Ͽ� �ּ���.", "INFORMATION", MessageBox.ERROR);
							return;
						}
						listModel.removeElement(selectedObj[0]);
						// End
						if( listModel == null ){
							listModel = new DefaultListModel();
						}
						listModel.addElement(condition);
						
						//[SR140722-022][20140522] Condition Sorting
						//[20140626] YunSungWon. 'Or' Sorting
						Enumeration<ConditionVector> enums = (Enumeration<ConditionVector>)listModel.elements();
						if( enums != null && enums.hasMoreElements()){
							
							ArrayList<ConditionVector> list = new ArrayList();
							while(enums.hasMoreElements()){
								ConditionVector v = enums.nextElement();
								list.add(v);
							}
							Collections.sort(list);
							
							listModel.clear();
							for( ConditionVector v : list){
								listModel.addElement(v);
							}
						}

					}catch(Exception e){
						e.printStackTrace();
					}
					
				}
				
			});
			JButton delBtn = new JButton("Del Row");
			delBtn.setBackground(Color.WHITE);
			delBtn.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// Combination Results ���̺����� ���õ� �ɼ� ������ ������.
					//Object[] selectedObj = combinationResultList.getSelectedValues();
					Object[] selectedObj = combinationResultList.getSelectedValuesList().toArray();
					DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
					for( int i = 0; selectedObj != null && i < selectedObj.length; i++){
						listModel.removeElement(selectedObj[i]);
					}
					
				}
				
			});
			JButton clearBtn = new JButton("Clear");
			clearBtn.setBackground(Color.WHITE);
			clearBtn.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent arg0) {
					// ����� ���� ���̺��� ������ �ɼ��� ��� ������.
					DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
					for( int i = model.getRowCount() - 1; model != null && i >= 0; i--){
						model.removeRow(i);
					}
					
				}
				
			});
			centerButtonPanel.add(addBtn);
			centerButtonPanel.add(modifyBtn);
			centerButtonPanel.add(delBtn);
			centerButtonPanel.add(clearBtn);
			
			JPanel centerListPanel = new JPanel(new BorderLayout());
			centerListPanel.setBackground(Color.WHITE);
			
			DefaultListModel listModel = new DefaultListModel();
			for( ConditionVector conditionVec : conditions){
				listModel.addElement(conditionVec);
			}
			combinationResultList.setModel(listModel);
			combinationResultList.addMouseListener(new MouseAdapter(){

				@Override
				public void mouseReleased(MouseEvent e) {
					if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
							&& e.isControlDown()==false) {
						
						DefaultTableModel tableModel = (DefaultTableModel)detailTable.getModel();
						for( int i = tableModel.getRowCount() - 1; tableModel != null && i >= 0; i--){
							tableModel.removeRow(i);
						}
						//modify row ������ ���� selection ����
//						combinationResultList.clearSelection();
						int selectedIdx =  combinationResultList.getAnchorSelectionIndex();
						DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
						ConditionVector conditions = (ConditionVector)listModel.get(selectedIdx);
						for( ConditionElement elm : conditions){
							VariantValue value = ConditionSetDialog.this.manager.getValue(elm.option + ":" + elm.value);
							
							if( value == null ){
								MessageBox.post(AIFUtility.getActiveDesktop(), registry.getString("variant.notFoundValue"), "INFORMATION", MessageBox.WARNING);
								return;
							}
							VariantOption option = value.getOption();
							Vector row = new Vector();
							row.add(value);
							row.add(option.getOptionName());
							row.add(option.getOptionDesc());
							row.add(value.getValueName());
							row.add(value.getValueDesc());
							tableModel.addRow(row);
						}
					}
					super.mouseReleased(e);
				}
				
			});
			JScrollPane pane = new JScrollPane();
			pane.setPreferredSize(new Dimension(180,200));
			pane.setViewportView(combinationResultList);
			pane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
			pane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
			pane.getViewport().setBackground(Color.WHITE);
			centerListPanel.add(pane, BorderLayout.CENTER);
			centerListPanel.setBorder(new TitledBorder(null, "Combination Results", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			southPanel.add(centerListPanel, BorderLayout.CENTER);
			
			
			contentPanel.add(southPanel, BorderLayout.SOUTH);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setBackground(Color.WHITE);
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Ok");
				okButton.setBackground(Color.WHITE);
				if( selectedLine != null){
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent actionevent) {
							
							final WaitProgressBar waitProgress = new WaitProgressBar(ConditionSetDialog.this);
							waitProgress.start();
							waitProgress.setStatus("Applying.....");
							AbstractAIFOperation operation = new AbstractAIFOperation(){

								@Override
								public void executeOperation()
										throws Exception {
									try{
										apply();
										waitProgress.close();
										ConditionSetDialog.this.dispose();
									}catch(TCException e){
										waitProgress.setStatus(e.getDetailsMessage());
										waitProgress.setShowButton(true);
									}
								}
								
							};
							session.queueOperation(operation);
						}
					});
				}
				
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Close");
				cancelButton.setBackground(Color.WHITE);
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						ConditionSetDialog.this.dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		VariantCheckBoxTableCellEditor.unUsedValueList.clear();
	}
	
	/**
	 * Condition�� ������.
	 * 
	 * @throws TCException
	 */
	private void apply() throws TCException{
		
		String lineMvl = "";
		DefaultListModel listModel = (DefaultListModel)combinationResultList.getModel();
		for( int i = 0; i < listModel.size(); i++){
			ConditionVector condition = (ConditionVector)listModel.get(i);
			if( condition == null) continue;
			
			String tmpStr = "";
			for( int j = 0; j < condition.size(); j++ ){
				ConditionElement elm = condition.get(j);
				tmpStr += ( j>0 ? " and ":"") + elm.item + ":" + MVLLexer.mvlQuoteId(elm.option, false) + " = " +  MVLLexer.mvlQuoteString(elm.value);
			}
			
			lineMvl += ( !lineMvl.equals("") ? " or ":"") + tmpStr;
		}
		
		/**
		 *  �ɼ� ������ üũ.
		 * [SR140722-022][20140708] swyoon �ɼ� ������ üũ.
		 */	
		// [CSH]4000 byte �߰����� ���� ����.
//		int convertedLength = SYMStringUtil.getConvertedLength(lineMvl);
		int convertedLength = SYMStringUtil.getConvertedLength_(lineMvl);
		if( convertedLength > 4000){
			throw new TCException("[Error] Option length limit is exceeded.("+ convertedLength + " Byte)\n�Է��� Option�� 4000 Byte�� �ʰ��Ͽ����ϴ�.\nOption ���� �� �ٽ� �����Ͽ� �ֽʽÿ�.");
		}		
		
		//�̷��� ����� ���� �Ʒ��� ����
		((SYMCBOMLine)this.selectedLine).setMVLCondition(lineMvl);
	}

	/**
	 * ��밡���� �ɼ��� �����ִ� Tree�� �ʱ�ȭ ��.(Function�� ���ǵ� �ɼǸ� ��밡��)
	 * @return
	 */
	private JTree initTree(){
		
		DefaultMutableTreeNode root = new DefaultMutableTreeNode("Option Set");
		if( this.enableOptionSet != null && this.enableOptionSet.size() > 0){
			for( VariantOption option : this.enableOptionSet){
				
				String desc = option.getOptionDesc() == null || option.getOptionDesc().equals("") ? "" : " | " + option.getOptionDesc();
				VariantNode optionNode = new VariantNode(option);
				List<VariantValue> values = option.getValues();
				
				//��밡���� �ɼ��� �����ϴ°�츸 Option�� �߰��Ѵ�.
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
		tree.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseReleased(MouseEvent e) {
				if( e.getClickCount()==2 && SwingUtilities.isLeftMouseButton(e) 
						&& e.isControlDown()==false) {
					add();
				}
				super.mouseReleased(e);
			}
			
		});
		for( int i = 0; i < tree.getRowCount(); i++){
			tree.expandRow(i);
		}
		return tree;
	}
	
	/**
	 * �ش� �ɼ��� �̹� ���ԵǾ� �ִ��� Ȯ��
	 * 
	 * @param value
	 * @param data
	 * @return
	 */
	private boolean isValidAndCheck(VariantValue value, Vector<Vector> data){
		String optionName = value.getOption().getOptionName();
		String valueName = value.getValueName();
		
		for( int i = 0; i < data.size(); i++ ){
			Vector row = data.get(i);
			if( value.equals(row.get(0))){
				if( value.getValueStatus() == VariantValue.VALUE_NOT_DEFINE)
					value.setValueStatus(VariantValue.VALUE_USE);
				return false;
			}else{
				VariantValue val = (VariantValue)row.get(0);
				if( value.getOption().equals(val.getOption())){
					row.removeAllElements();
					row.add( value );
					row.add(value.getOption().getOptionName());
					row.add(value.getOption().getOptionDesc());
					row.add(value.getValueName());
					row.add(value.getValueDesc());
					data.remove(i);
					data.insertElementAt(row, i);
					DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
					model.fireTableDataChanged();
					return false;
				}
			}
		}
		
		return true;
	}
	
	/**
	 * �÷� ������ �ʱ�ȭ
	 */
	public void columnInit(){
		TableColumnModel columnModel = detailTable.getColumnModel();
		int n = headerVector.size();
		for (int i = 0; i < n; i++) {
			columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
			columnModel.getColumn(i).setWidth(columnWidth[i]);
		}
		columnModel.removeColumn(columnModel.getColumn(columnModel.getColumnIndex("USE")));
	}
	
	/**
	 * Condition �������̺��� �ɼǰ��� �߰���.
	 */
	private void add(){
		TreePath[] paths = tree.getSelectionPaths();
		for( int i = 0; paths != null && i < paths.length; i++){
			TreePath path = paths[i];
			VariantNode node = (VariantNode)path.getLastPathComponent();
			Object obj = node.getUserObject();
			DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
			if( obj instanceof VariantValue){
				
				VariantValue value = (VariantValue)obj;
				VariantOption option = value.getOption();
				if( isValidAndCheck(value, model.getDataVector())){
					value.setNew(true );
					value.setValueStatus( VariantValue.VALUE_USE);
					Vector row = new Vector();
					row.add( value );
					row.add(option.getOptionName());
					row.add(option.getOptionDesc());
					row.add(value.getValueName());
					row.add(value.getValueDesc());
					
					model.addRow(row);
				}
			}
		}
	}
	
	/**
	 * �߰��� Conditiond�� ������.
	 */
	private void remove(){
		int[] selectedIdxs = detailTable.getSelectedRows();
		DefaultTableModel model = (DefaultTableModel)detailTable.getModel();
		ArrayList<VariantValue> selectedValues = new ArrayList();
		for( int i = selectedIdxs.length - 1; i >= 0; i--){
			VariantValue value = (VariantValue)model.getValueAt(selectedIdxs[i], 0);
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
				
			}
		}
		
		model.fireTableDataChanged();
	}
}