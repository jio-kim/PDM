package com.ssangyong.commands.workflow.correction;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import com.ssangyong.commands.ec.dao.CustomECODao;
import com.ssangyong.common.remote.DataSet;
import com.ssangyong.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;

/**
 * [SR160527-024][20160620][ymjang] A ECO 에서 신규로 붙은 Parent 하위 Part 가 
 * B ECO 에서 리비전될 때 B ECO 에 A ECO 에서 신규로 붙은 Parent 가 추출이 안되는 문제 해결 --> EPL 추가
 */
@SuppressWarnings({"serial", "rawtypes", "unchecked"})
public class EcoEplCorrectionDialog extends JDialog {

	private final JPanel contentPanel = new JPanel();
	private JTable table;
	private JLabel ecoNoLabel = null;
	private String[] columnName = new String[] { "N", "Parent No",
			"Parent Rev", "Seq", "Project",
			"Part No", "Part Rev", "Result" };
	private int[] columnWidth = new int[]{20, 85, 65, 50, 80, 85, 65, 200};
	
	private DefaultTableModel model = null;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			EcoEplCorrectionDialog dialog = new EcoEplCorrectionDialog(null, null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public EcoEplCorrectionDialog(String ecoNo, ArrayList<HashMap<String, String>> list) {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setTitle("Incorrect EPL");
		setBounds(100, 100, 747, 344);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			contentPanel.add(panel, BorderLayout.NORTH);
			{
				JLabel lblEcoNo = new JLabel("ECO NO :");
				panel.add(lblEcoNo);
			}
			{
				ecoNoLabel = new JLabel(ecoNo);
				ecoNoLabel.setFont(new Font("굴림", Font.BOLD, 12));
				panel.add(ecoNoLabel);
			}
		}
		{
			JPanel panel = new JPanel();
			contentPanel.add(panel, BorderLayout.CENTER);
			panel.setLayout(new BorderLayout(0, 0));
			{
				Vector columnVec = new Vector();
				for(String str : columnName){
					columnVec.add(str);
				}
				
				Vector data = new Vector();
				for( int i = 0; list != null && i < list.size();i++){
					HashMap<String, String> map = list.get(i);
					Vector row = new Vector();
					row.add((i+1) + "");
					row.add(map.get("PARENT_NO"));
					row.add(map.get("PARENT_REV"));
					row.add(map.get("OLD_SEQ"));
					row.add(map.get("OLD_PROJECT"));
					row.add(map.get("OLD_PART_NO"));
					row.add(map.get("OLD_PART_REV"));
					
					// [SR160527-024][20160620][ymjang] A ECO 에서 신규로 붙은 Parent 하위 Part 가 B ECO 에서 리비전될 때 
					// B ECO 에 A ECO 에서 신규로 붙은 Parent 가 추출이 안되는 문제 해결 --> EPL 추가
					row.add(map.get("CORRECT_DESC"));
					//String eplId = map.get("EPL_ID");
					//row.add(getCorrectionResult(map, eplId, false));
					
					data.add(row);					
					
					
					row = new Vector();
					row.add((i+1) + "");
					row.add(map.get("PARENT_NO"));
					row.add(map.get("PARENT_REV"));
					row.add(map.get("NEW_SEQ"));
					row.add(map.get("NEW_PROJECT"));
					row.add(map.get("NEW_PART_NO"));
					row.add(map.get("NEW_PART_REV"));
					
					// [SR160527-024][20160620][ymjang] A ECO 에서 신규로 붙은 Parent 하위 Part 가 
					// B ECO 에서 리비전될 때 B ECO 에 A ECO 에서 신규로 붙은 Parent 가 추출이 안되는 문제 해결 --> EPL 추가
					row.add(map.get("CORRECT_DESC"));
					//String eplId = map.get("EPL_ID");
					//row.add(getCorrectionResult(map, eplId, true));
					
					data.add(row);
				}
				
				table = new JTable();
				table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
				
				model = new DefaultTableModel(data, columnVec) {

					public Class getColumnClass(int columnIndex) {
						return String.class;
					}
				};
				table.setModel(model);
				
				TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
				table.setRowSorter(sorter);
				
				TableColumnModel columnModel = table.getColumnModel();
				for (int i = 0; i < columnName.length; i++) {
					columnModel.getColumn(i).setPreferredWidth(columnWidth[i]);
					columnModel.getColumn(i).setWidth(columnWidth[i]);
				}

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
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						EcoEplCorrectionDialog.this.dispose();
					}
				});
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
			}
		}
	}
	
	private String getCorrectionResult(HashMap<String, String> incorrectMap, String eplId, boolean isNew){
		
		try{
			String resultStr = "";
			int[] result = new int[5];
			
			SYMCRemoteUtil remote = new SYMCRemoteUtil();
			DataSet ds = new DataSet();
			ds.put("EPL_ID", eplId);
			
			HashMap<String, String> map = (HashMap<String, String>)remote.execute(CustomECODao.ECO_INFO_SERVICE_CLASS, "getEPL", ds);
			if( map == null){
				resultStr = "DELETED";
				return resultStr;
			}else{
				result[0] = 0;
			}
			
			if( isNew && !incorrectMap.get("PARENT_REV").equals(map.get("PARENT_REV"))){
				result[1] = 1;
			}else{
				result[1] = 0;
			}
			
			if( isNew && !incorrectMap.get("NEW_PART_REV").equals(map.get("NEW_PART_REV"))){
				result[2] = 1;
			}else{
				result[2] = 0;
			}
			
			if( isNew && !incorrectMap.get("NEW_PROJECT").equals(map.get("NEW_PROJECT"))){
				result[3] = 1;
			}else{
				result[3] = 0;
			}
			
			if( !isNew && !incorrectMap.get("OLD_PART_REV").equals(map.get("OLD_PART_REV"))){
				result[4] = 1;
			}else{
				result[4] = 0;
			}			
			
			for( int i = 0; i < result.length; i++){

				switch(i){
				case 0:
					if( result[i] == 1){
						resultStr = "DELETED";
						return resultStr;
					}
					break;
				case 1:
					if( result[i] == 1){
						resultStr = "PARENT REV UPDATED";
					}
					break;
				case 2:
					if( result[i] == 1){
						resultStr += (resultStr.equals("") ? "":",") + "NEW PART REV UPDATED";
					}
					break;
				case 3:
					if( result[i] == 1){
						resultStr += (resultStr.equals("") ? "":",") + "NEW PROJECT UPDATED";
					}
					break;
				case 4:
					if( result[i] == 1){
						resultStr += (resultStr.equals("") ? "":",") + "OLD PART REV UPDATED";
					}
					break;
				default:
					resultStr = "";
				}
			}
			
			return resultStr;
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return "";
		
	}

}
