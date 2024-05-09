package com.symc.plm.rac.prebom.costUpdate;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.commands.commonpartcheck.IconColorCellRenderer;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.common.TypeConstant;
import com.symc.plm.rac.prebom.preospec.dialog.PreOSpecImportDlg;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.ConfirmationDialog;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;

public class TargetCostImportDlg extends AbstractAIFDialog {

//	private static final long serialVersionUID = -4408474910899088180L;
	private JTextField tfFilePath;
	private JButton btnValidation;
	private File selectedFile;
	private HashMap<String,Object> executeMap;
	private DefaultTableModel tableModel;
	private JButton executeButton;
	private JTextArea area;
	private TCSession session;
	private String target_project;
	private String eaiDate = "";

	/**
	 * 
	 * @copyright : Plmsoft
	 * @author : 조석훈
	 * @since  : 2018. 5. 6.
	 * @throws Exception
	 */
	public TargetCostImportDlg() throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		session = CustomUtil.getTCSession();
		initUI();
	}

	/**
	 * UI 그리기 시작
	 * @Copyright : Plmsoft
	 * @author : 조석훈
	 * @since  : 2018. 5. 6.
	 */
	private void initUI() throws Exception{
		setTitle("Target/Esti Cost Material Update");
		getContentPane().setLayout(new VerticalLayout(5,5,5,5,5));
		getContentPane().add("top.bind.center.center", createSearchPanel());
		getContentPane().add("unbound.bind.center.center", createTabPanel());
		getContentPane().add("bottom.bind.center.center", createButtonPanel());
		getContentPane().add("bottom.bind.center.center", new Separator());
		getContentPane().add("unbound.bind.center.center", resultPanel());
		
		setPreferredSize(new Dimension(1200,600));
	}

	
	private JPanel resultPanel(){
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(new TitledBorder(null, "Execute Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		area = new JTextArea();
		area.setEditable(false);
		
		panel.add("unbound.bind.center.center", new JScrollPane(area));
		return panel;
	}
	
	/**
	 * TabPanel 생성
	 * @Copyright : Plmsoft
	 * @author : 조석훈
	 * @since  : 2018. 5. 6.
	 * @return
	 */
	private JPanel createTabPanel(){
		JPanel panel = new JPanel(new VerticalLayout());
		panel.setBorder(new TitledBorder(null, "Validation Result", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		
		String[] column = {"Seq", "Uniq_No", "Rev ID", "P/No","P/Name", "Target Cost", "Prd Tool Cost", "Description"};
		tableModel = new DefaultTableModel(){
			@Override
			public boolean isCellEditable(int paramInt1, int paramInt2) {
				// TODO Auto-generated method stub
				return false;
			}
		};
		tableModel.setColumnIdentifiers(column);

		JTable table = new JTable(tableModel);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);

		int[] width = {5,30,10,30,100,30,30,400};
		TableColumnModel model = table.getColumnModel();
		for(int i=0; i<model.getColumnCount(); i++ ){
			model.getColumn(i).setCellRenderer(new IconColorCellRenderer(new Color(230,230,230)));
			model.getColumn(i).setPreferredWidth(width[i]);
		}
		
		panel.add("unbound.bind.center.center", new JScrollPane(table));

		return panel;
	}
	/**
	 * TabPanel 생성
	 * @Copyright : Plmsoft
	 * @author : 조석훈
	 * @since  : 2018. 5. 6.
	 * @return
	 */
	private JPanel createSearchPanel() throws Exception{
		JPanel regPanel = new JPanel();
		
		regPanel.setLayout(new BorderLayout(0, 0));
		FlowLayout flowLayout = null;
		
		JPanel msgPanel = new JPanel();
		msgPanel.setLayout(new BorderLayout(0, 0));
//		flowLayout = (FlowLayout) msgPanel.getLayout();
//		flowLayout.setAlignment(FlowLayout.LEADING);
		msgPanel.setBorder(new TitledBorder(null, "Search", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		regPanel.add(msgPanel, BorderLayout.NORTH);
		JLabel lblAttachFileMsg = new JLabel();
        lblAttachFileMsg.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        lblAttachFileMsg.setForeground(Color.RED);
        lblAttachFileMsg.setText("※ 암호화된 문서는 반드시 암호를 해제하신 후 등록하셔야 합니다. ※");
        msgPanel.add(lblAttachFileMsg, BorderLayout.NORTH);
	        
		
		JPanel searchPanel = new JPanel();
		flowLayout = (FlowLayout) searchPanel.getLayout();
		flowLayout.setAlignment(FlowLayout.LEADING);
//		searchPanel.setBorder(new TitledBorder(null, "Target/Esti Cost Material Update", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		msgPanel.add(searchPanel, BorderLayout.CENTER);
		tfFilePath = new JTextField();
		searchPanel.add(tfFilePath);
		tfFilePath.setColumns(60);
		JButton btnFind = new JButton("Find..");
		btnFind.setIcon(new ImageIcon(PreOSpecImportDlg.class.getResource("/icons/search_16.png")));
		btnFind.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				
				JFileChooser fileChooser = new JFileChooser();
//				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
				fileChooser.setFileFilter(new FileFilter(){
	
					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
					        return true;
					    }
						
						if( f.isFile()){
							return f.getName().endsWith("txt");
						}
						return false;
					}
	
					@Override
					public String getDescription() {
						return "*.txt";
					}
	
				});
				int result = fileChooser.showOpenDialog(TargetCostImportDlg.this);
				if( result == JFileChooser.APPROVE_OPTION){
					selectedFile = fileChooser.getSelectedFile();
					TargetCostImportDlg.this.tfFilePath.setText( selectedFile.getAbsolutePath() );
					btnValidation.setEnabled(true);
				}						
				
			}
		});
		searchPanel.add(btnFind);
		btnValidation = new JButton("Validation");
		btnValidation.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				validation();
			}
		});
		btnValidation.setEnabled(false);
		searchPanel.add(btnValidation);
		
		JButton btnTemplate = new JButton("Template File Download");
		btnTemplate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				templateDown();
			}
		});
//		btnTemplate.setEnabled(false);
		searchPanel.add(btnTemplate);

		return regPanel;
	}
	
	//I/F Table 저장 로직
	private void templateDown(){
		try {
            File tempFile = new File("c:\\temp\\costUpdate_"+getTodayDate()+".txt");
            Writer output = new BufferedWriter(new FileWriter(tempFile));
            output.write("unique_no;rev_id;target_cost;prd_tool_cost (2행은 Project Code, 1행의 format을 참고하여 3행부터 입력하여 주세요. rev_id는 필수값이 아닙니다.)\r\n");
            output.write("A200");
            output.write("\r\n");
            output.close();
            
            selectedFile = tempFile;
			TargetCostImportDlg.this.tfFilePath.setText( selectedFile.getAbsolutePath() );
			btnValidation.setEnabled(true);
            
			Desktop.getDesktop().open(selectedFile);
			
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	//Part 저장 로직
//	private void templateDown(){
//		try {
//            File tempFile = new File("c:\\temp\\costUpdate_"+getTodayDate()+".txt");
//            Writer output = new BufferedWriter(new FileWriter(tempFile));
//            output.write("unique_no;rev_id;target_cost;prd_tool_cost (1행의 format을 참고하여 2행부터 입력하여 주세요. rev_id는 필수값이 아닙니다.)\r\n");
//            output.close();
//            
//            selectedFile = tempFile;
//			TargetCostImportDlg.this.tfFilePath.setText( selectedFile.getAbsolutePath() );
//			btnValidation.setEnabled(true);
//            
//			Desktop.getDesktop().open(selectedFile);
//			
//        } catch (Exception e) {
//        	e.printStackTrace();
//        }
//	}
	
	private String getTodayDate() {
		  Date date = new Date();
		  SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		  
		  return dateFormat.format(date).toString();
	  }
	
	//I/F Table 저장 로직
	private void validation() {
		try {
			session.setStatus("Validation Start...");
			tableModel.setRowCount(0);
			executeButton.setEnabled(false);
			BufferedReader in = new BufferedReader(new FileReader(selectedFile));
			String s = "";
			int count = 0;
			int execCount = 0;
			HashMap<String,Object> readMap = null;
			HashMap<String,Object> validationMap = null;
			HashMap<String,Object> duplecateMap = new HashMap();
			String[] value = null;
			String item_id = "";
			String rev_id = "";
			String targetCost = "";
			String prdToolCost = "";
			String desc = "";
			executeMap = new HashMap();
			boolean updateTarget = true;
			readMap = new HashMap();
			StringBuffer resultBufMessage = new StringBuffer();
			while ((s = in.readLine()) != null) {
				if(count != 0){
					if(count == 1){
						target_project = s;
					} else {
						if(!s.equals("")){
							value = s.split(";",4);
							readMap.put(String.valueOf(count-2), value);
						}
					}
				}
				count++;
			}
			
			if(target_project == null || target_project.equals("") || target_project.length() > 4){
				area.setText("Target Project의 값을 확인하세요.");
				return;
			}
			
			
			SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
			
			DataSet ds = new DataSet();
			ds.put("TARGETPROJECT", target_project);
			
			Object eaiObject = remoteQuery.execute("com.kgm.service.PreBOMUssageExportService", "getEaiDate", ds);
			
			if(eaiObject == null){
				area.setText(target_project + "의 최신 eai_create_time을 찾을 수 없습니다.");
				return;
			} else {
				eaiDate = eaiObject.toString();
			}
			
			count = 0;
			String[] props;
			int readCount = readMap.size();
			for(int i=0 ; i < readCount ; i++){
					updateTarget = true;
					validationMap = new HashMap();
					value = (String[])readMap.get(String.valueOf(count));
					item_id = value[0].trim();
					validationMap.put("itemId", item_id);
					rev_id = value[1].trim();
					targetCost = value[2].replaceAll(",", "").trim();
					validationMap.put("targetCost", targetCost);
					prdToolCost = value[3].replaceAll(",", "").trim();
					validationMap.put("prdToolCost", prdToolCost);
					
					if(item_id != null && !item_id.equals("")){
						if(!duplecateMap.containsKey(item_id)){
								desc = "OK";
								if(targetCost != null && !targetCost.equals("")){
									try{
										Integer.parseInt(targetCost);
									}catch(Exception e){
										desc = "Target Cost 값이 숫자 형식이 아닙니다.";
										updateTarget = false;
									}
								}
								if(prdToolCost != null && !prdToolCost.equals("")){
									try{
										Integer.parseInt(prdToolCost);
									}catch(Exception e){
										desc = "Prd Tool Cost 값이 숫자 형식이 아닙니다.";
										updateTarget = false;
									}
								}
							duplecateMap.put(item_id, validationMap);
							if(updateTarget){
								executeMap.put(String.valueOf(execCount), validationMap);
								execCount++;
								resultBufMessage.append("update IF_PREBOM_MASTER_FULL_LIST set tgt_cost_material = '"+targetCost+"', prd_tool_cost = '"+prdToolCost+"', prd_total = '"+prdToolCost+"' where TARGET_PROJECT = '"+target_project+"' and child_unique_no='"+item_id+"' and substr(eai_create_time,0,8) = '"+eaiDate+"';\n");
							}
						} else {
							desc = "duplecate";
						}
					} else {
						desc = "Item ID 값이 Null 입니다.";
					}
					tableModel.addRow(new String[]{String.valueOf(count), item_id, rev_id, "", "", targetCost, prdToolCost, desc});
					session.setStatus(i + " / " + readCount);
				count++;
			}
			
			if(execCount > 0){
				executeButton.setEnabled(true);
			}
			resultBufMessage.append("전체 " + readCount + " Line 중에 " +execCount+"개가 업데이트 대상입니다.");
//			area.setText("전체 " + readCount + " Line 중에 " +execCount+"개가 업데이트 대상입니다.");
			area.setText(resultBufMessage.toString());
			session.setStatus("Validation End...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//pre veh part 저장 로직
//	private void validation() {
//		try {
//			session.setStatus("Validation Start...");
//			tableModel.setRowCount(0);
//			executeButton.setEnabled(false);
//			BufferedReader in = new BufferedReader(new FileReader(selectedFile));
//			String s = "";
//			int count = 0;
//			int execCount = 0;
//			HashMap<String,Object> readMap = null;
//			HashMap<String,Object> validationMap = null;
//			HashMap<String,Object> duplecateMap = new HashMap();
//			String[] value = null;
//			String item_id = "";
//			String rev_id = "";
//			String targetCost = "";
//			String prdToolCost = "";
//			String desc = "";
//			String name = "";
//			String dsp_id = "";
//			TCComponentItem item = null;
//			TCComponentItemRevision revision = null;
//			executeMap = new HashMap();
//			boolean updateTarget = true;
//			readMap = new HashMap();
//			while ((s = in.readLine()) != null) {
//				if(count != 0){
//					value = s.split(";",4);
//					readMap.put(String.valueOf(count-1), value);
//				}
//				count++;
//			}
//			
//			count = 0;
//			String[] props;
//			int readCount = readMap.size();
//			for(int i=0 ; i < readCount ; i++){
//					updateTarget = true;
//					validationMap = new HashMap();
//					value = (String[])readMap.get(String.valueOf(count));
//					item_id = value[0].trim();
//					validationMap.put("itemId", item_id);
//					rev_id = value[1].trim();
//					
//					if(!duplecateMap.containsKey(item_id)){
//						if(rev_id == null || rev_id.trim().equals("") || rev_id.length() != 3){
//							item = CustomUtil.findItem("S7_PreVehPart", item_id);
//							if(item != null){
//								revision = SYMTcUtil.getLatestReleasedRevision(item);
//							}
//						} else {
//							revision = CustomUtil.findItemRevision("S7_PreVehPart", item_id, rev_id);
//						}
//						
//						if(revision == null || !revision.getType().equals("S7_PreVehPartRevision")){
//							desc = "PreVehPart 타입이 아닙니다.";
//							updateTarget = false;
//						} else {
//							desc = "OK";
//							targetCost = value[2].replaceAll(",", "").trim();
//							validationMap.put("targetCost", targetCost);
//							prdToolCost = value[3].replaceAll(",", "").trim();
//							validationMap.put("prdToolCost", prdToolCost);
//							validationMap.put("revision", revision);
//							props = revision.getProperties(new String[]{PropertyConstant.ATTR_NAME_ITEMNAME, PropertyConstant.ATTR_NAME_DISPLAYPARTNO, PropertyConstant.ATTR_NAME_ITEMREVID});
//							name = props[0];
//							dsp_id = props[1];
//							rev_id = props[2];
//							
//							if(targetCost != null && !targetCost.equals("")){
//								try{
//									Integer.parseInt(targetCost);
//								}catch(Exception e){
//									desc = "Target Cost 값이 숫자 형식이 아닙니다.";
//									updateTarget = false;
//								}
//							}
//							if(prdToolCost != null && !prdToolCost.equals("")){
//								try{
//									Integer.parseInt(prdToolCost);
//								}catch(Exception e){
//									desc = "Prd Tool Cost 값이 숫자 형식이 아닙니다.";
//									updateTarget = false;
//								}
//							}
//						}
//						
//						tableModel.addRow(new String[]{String.valueOf(count), item_id, rev_id, dsp_id, name, targetCost, prdToolCost, desc});
//						duplecateMap.put(item_id, validationMap);
//						if(updateTarget){
//							executeMap.put(String.valueOf(execCount), validationMap);
//							execCount++;
//						}
//					} else {
//						desc = "duplecate";
//					}
//					session.setStatus(i + " / " + readCount);
//				count++;
//			}
//			
//			if(execCount > 0){
//				executeButton.setEnabled(true);
//			}
//			area.setText("전체 " + readCount + " Line 중에 " +execCount+"개가 업데이트 대상입니다.");
//			session.setStatus("Validation End...");
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}
	

	/**
	 * 하단 Close 버튼 패널
	 * @Copyright : Plmsoft
	 * @author : 조석훈
	 * @since  : 2018. 5. 6.
	 * @return
	 */
	private JPanel createButtonPanel(){
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				dispose();
			}
		});
		
		executeButton = new JButton("Execute");
		executeButton.setEnabled(false);
		executeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				execute();
			}

		});
		buttonPane.add(executeButton);
		buttonPane.add(cancelButton);
		
		return buttonPane;
	}
	
	//I/F Table 저장 로직
	private void execute(){
		
		try {
			session.setStatus("Update Start...");
		
			executeButton.setEnabled(false);
			int exeCount = executeMap.size();
			int response = ConfirmationDialog.post(AIFUtility.getActiveDesktop(), "Confirm", "Do you want to update "+exeCount+" cases?");
			if (response == ConfirmationDialog.YES) {
				String targetCost = "";
				String prdToolCost = "";
				String uniqNo = "";
				HashMap<String,Object> updateMap = new HashMap();
				StringBuffer resultBufMessage = new StringBuffer();
				boolean created = false;
				
				SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
				
//				DataSet ds1 = new DataSet();
//				ds1.put("MAP", executeMap);
//				ds1.put("EAIDATE", eaiDate);
//				ds1.put("TARGETPROJECT", target_project);
//				remoteQuery.execute("com.kgm.service.PreBOMUssageExportService", "updateCost", ds1);
				
				for(int i=0; i<exeCount; i++){
	            	updateMap = (HashMap)executeMap.get(String.valueOf(i));
					targetCost = updateMap.get("targetCost").toString();
					prdToolCost = updateMap.get("prdToolCost").toString();
					uniqNo = updateMap.get("itemId").toString();
					DataSet ds1 = new DataSet();
			        ds1.put("TARGETPROJECT", target_project);
			        ds1.put("UNIQNO", uniqNo);
			        ds1.put("TARGETCOST", targetCost);
			        ds1.put("PRDTOOLCOST", prdToolCost);
			        ds1.put("EAIDATE", eaiDate);
			        session.setStatus("진행중 : "+uniqNo+" "+(i+1)+"/"+exeCount);
			        remoteQuery.execute("com.kgm.service.PreBOMUssageExportService", "updateCost", ds1);
	            }
				
				area.setText("Update Completed...");
			} else {
				executeButton.setEnabled(true);
			}
			
			session.setStatus("Update End...");
		} catch (Exception e1) {
			e1.printStackTrace();
			area.setText(e1.getMessage());
		} 
	}
	
	//pre veh part 저장 로직
//	private void execute(){
//		
//		try {
//			session.setStatus("Update Start...");
//			boolean setBypass = false;
//			if(session.getCurrentGroup().toString().equals("dba")){
//				if(!session.hasBypass()){
//					//admin 수행 시 bypass
//					session.enableBypass(true);
//					setBypass = true;
//				}
//			} else {
//				//일반 사용자는 Released된 preVehPartRevision 수정을 위해 Object ACL 권한 추가
//				
//			}
//			
//		
//			executeButton.setEnabled(false);
//			int exeCount = executeMap.size();
//			int response = ConfirmationDialog.post(AIFUtility.getActiveDesktop(), "Confirm", "Do you want to update "+exeCount+" cases?");
//			if (response == ConfirmationDialog.YES) {
//				TCComponentItemRevision revision = null;
//				TCComponent refComp = null;
//				String targetCost = "";
//				String prdToolCost = "";
//				String uniqNo = "";
//				HashMap<String,Object> updateMap = new HashMap();
//				StringBuffer resultBufMessage = new StringBuffer();
//				boolean created = false;
//				for(int i=0; i<exeCount; i++){
//					updateMap = (HashMap)executeMap.get(String.valueOf(i));
//					revision = (TCComponentItemRevision)updateMap.get("revision");
//					targetCost = updateMap.get("targetCost").toString();
//					prdToolCost = updateMap.get("prdToolCost").toString();
//					uniqNo = updateMap.get("itemId").toString();
//					
//					
//					try {
//						refComp = revision.getReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF);
//						if( refComp != null){//리비전별 속성 이력 관리 안됨.
//							resultBufMessage.append("(" + (i+1) + "/" + exeCount + ") " + uniqNo + " " + revision.toDisplayString() + " : ");
//							if(targetCost != null && !targetCost.equals("")){
//								resultBufMessage.append(" targetCost " + targetCost);
//								refComp.setProperty(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL, targetCost);
//							}
//							if(prdToolCost != null && !prdToolCost.equals("")){
//								resultBufMessage.append(" prdToolCost " + prdToolCost);
//								refComp.setProperty(PropertyConstant.ATTR_NAME_PRD_TOOL_COST, prdToolCost);
//							}
//						} else {//reference 속성을 개정해야한다면 아래 로직으로 적용.
//							resultBufMessage.append("(" + (i+1) + "/" + exeCount + ") " + uniqNo + " " + revision.toDisplayString() + " : ");
//							created = false;
//							ArrayList<String> attrNames = new ArrayList();
//							ArrayList<String> attrValues = new ArrayList();
//							if(targetCost != null && !targetCost.equals("")){
//								attrNames.add(PropertyConstant.ATTR_NAME_TARGET_COST_MATERIAL);
//								attrValues.add(targetCost);
//								created = true;
//								resultBufMessage.append(" targetCost " + targetCost);
//							}
//							if(prdToolCost != null && !prdToolCost.equals("")){
//								attrNames.add(PropertyConstant.ATTR_NAME_PRD_TOOL_COST);
//								attrValues.add(prdToolCost);
//								created = true;
//								resultBufMessage.append(" prdToolCost " + prdToolCost);
//							}
//							if(created){
//								refComp = SYMTcUtil.createApplicationObject(revision.getSession(), TypeConstant.S7_PREVEHTYPEDREFERENCE, attrNames.toArray(new String[attrNames.size()]), attrValues.toArray(new String[attrValues.size()]));
//								revision.setReferenceProperty(PropertyConstant.ATTR_NAME_PRE_VEH_TYPE_REF, refComp);
//							}
//						}
//						
//						resultBufMessage.append(" Update Completed.\n");
//						area.setText(resultBufMessage.toString());
//						
//						session.setStatus(i + " / " + exeCount);
//					} catch (TCException e) {
//						e.printStackTrace();
//						resultBufMessage.append(" Update Failed. >> " + e.toString() + "\n");
//						area.setText(resultBufMessage.toString());
//					}
//				}
//			} else {
//				executeButton.setEnabled(true);
//			}
//			
//			if(setBypass){
//				session.enableBypass(false);
//			}
//			session.setStatus("Update End...");
//		} catch (TCException e1) {
//			e1.printStackTrace();
//			area.setText(e1.getMessage());
//		} 
//	}
}