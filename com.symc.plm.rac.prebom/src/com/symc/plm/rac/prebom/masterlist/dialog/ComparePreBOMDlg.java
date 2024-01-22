package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

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
import com.symc.plm.rac.prebom.common.PropertyConstant;
import com.symc.plm.rac.prebom.masterlist.model.CellValue;
import com.symc.plm.rac.prebom.masterlist.model.RevisionIDComboBoxObject;
import com.symc.plm.rac.prebom.masterlist.model.StoredOptionSet;
import com.symc.plm.rac.prebom.masterlist.operation.ComparePreBOMWithDateOperation;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;
import com.toedter.calendar.JDateChooser;

public class ComparePreBOMDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JLabel selectedItemLabel = null;
	private JComboBox cbOldPreFMPRevision = null;
	private JComboBox cbNewPreFMPRevision = null;
	private JDateChooser oldDateChooser = null;
	private JDateChooser newDateChooser = null;
	private TCComponentItem selectedItem = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ComparePreBOMDlg dialog = new ComparePreBOMDlg(null);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ComparePreBOMDlg(TCComponentItem selectedItem) {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		this.selectedItem = selectedItem;
		
		init();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void init(){
		setTitle("Compare MLM");
		setBounds(100, 100, 465, 225);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Selected Item", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			contentPanel.add(panel, BorderLayout.NORTH);
			{
				selectedItemLabel = new JLabel(selectedItem.toString());
				selectedItemLabel.setFont(new Font("굴림", Font.BOLD, 12));
				panel.add(selectedItemLabel);
			}
		}
		{
			JPanel panel = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.LEADING);
			
			panel.setBorder(new TitledBorder(null, "Reference date", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			contentPanel.add(panel, BorderLayout.CENTER);
			
			JPanel pnlDateChooser = new JPanel();
//			GridLayout glDateChooser = new GridLayout(2, 5);
//			pnlDateChooser.setLayout(glDateChooser);
			
			GridBagLayout gblDateChooser = new GridBagLayout();
			pnlDateChooser.setLayout(gblDateChooser);
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.insets = new Insets(5,5,0,0);	// Top, Left margin
			
			{
				JLabel lblOld = new JLabel("Old ");
				gbc.gridx = 0;
				gbc.gridy = 0;
				pnlDateChooser.add(lblOld, gbc);
				oldDateChooser = new JDateChooser("yyyy-MM-dd", false);
				gbc.gridx = 1;
				gbc.gridy = 0;
				pnlDateChooser.add(oldDateChooser, gbc);
			}
			{
				JLabel label = new JLabel("      ");
				gbc.gridx = 2;
				gbc.gridy = 0;
				pnlDateChooser.add(label, gbc);
			}
			{
				JLabel lblNew = new JLabel("New");
				gbc.gridx = 3;
				gbc.gridy = 0;
				pnlDateChooser.add(lblNew, gbc);
				newDateChooser = new JDateChooser("yyyy-MM-dd", false);
				gbc.gridx = 4;
				gbc.gridy = 0;
				pnlDateChooser.add(newDateChooser, gbc);
			}
			
			// [SR160426-034][20160509][jclee] Revision 선택 시 Released Date 자동 선택 Combobox 추가
			{
				JLabel lblDummy1 = new JLabel("");
				cbOldPreFMPRevision = new JComboBox();
				cbOldPreFMPRevision.setFont(new Font("굴림", Font.BOLD, 12));
				cbOldPreFMPRevision.addItem("Select a Revision ID");
				gbc.gridx = 0;
				gbc.gridy = 1;
				pnlDateChooser.add(lblDummy1, gbc);
				gbc.gridx = 1;
				gbc.gridy = 1;
				pnlDateChooser.add(cbOldPreFMPRevision, gbc);
			}
			{
				JLabel lblDummy2 = new JLabel("      ");
				gbc.gridx = 2;
				gbc.gridy = 1;
				pnlDateChooser.add(lblDummy2, gbc);
			}
			{
				JLabel lblDummy3 = new JLabel("");
				cbNewPreFMPRevision = new JComboBox();
				cbNewPreFMPRevision.setFont(new Font("굴림", Font.BOLD, 12));
				cbNewPreFMPRevision.addItem("Select a Revision ID");
				gbc.gridx = 3;
				gbc.gridy = 1;
				pnlDateChooser.add(lblDummy3, gbc);
				gbc.gridx = 4;
				gbc.gridy = 1;
				pnlDateChooser.add(cbNewPreFMPRevision, gbc);
			}
			
			panel.add(pnlDateChooser);
			
			try {
				TCComponent[] caRevision = selectedItem.getReferenceListProperty("revision_list");
				for (int inx = 0; inx < caRevision.length; inx++) {
					TCComponentItemRevision irFMP = (TCComponentItemRevision) caRevision[inx]; 
					RevisionIDComboBoxObject rco = new RevisionIDComboBoxObject(irFMP);
					
					if (rco.getItemRevisionID() == null || rco.getItemRevisionID().equals("") || rco.getItemRevisionID().length() == 0 || rco.getItemRevisionID().equals("000")) {
						continue;
					}
					
					cbOldPreFMPRevision.addItem(rco);
					cbNewPreFMPRevision.addItem(rco);
				}
				
				cbOldPreFMPRevision.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {
						if (cbOldPreFMPRevision.getSelectedIndex() == 0) {
							oldDateChooser.setDate(new Date());
						} else {
							Object oSelectedItem = cbOldPreFMPRevision.getSelectedItem();
							
							if (oSelectedItem instanceof RevisionIDComboBoxObject) {
								RevisionIDComboBoxObject rcoSelected = (RevisionIDComboBoxObject) oSelectedItem;
								Date dReleased = rcoSelected.getReleasedDate();
								
								if (dReleased != null) {
									oldDateChooser.setDate(dReleased);
								} else {
									oldDateChooser.setDate(new Date());
								}
							}
						}
					}
				});
				
				cbNewPreFMPRevision.addItemListener(new ItemListener() {
					@Override
					public void itemStateChanged(ItemEvent arg0) {
						if (cbNewPreFMPRevision.getSelectedIndex() == 0) {
							newDateChooser.setDate(new Date());
						} else {
							Object oSelectedItem = cbNewPreFMPRevision.getSelectedItem();
							
							if (oSelectedItem instanceof RevisionIDComboBoxObject) {
								RevisionIDComboBoxObject rcoSelected = (RevisionIDComboBoxObject) oSelectedItem;
								Date dReleased = rcoSelected.getReleasedDate();
								
								if (dReleased != null) {
									newDateChooser.setDate(dReleased);
								} else {
									newDateChooser.setDate(new Date());
								}
							}
						}
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						
						final WaitProgressBar waitBar = new WaitProgressBar(AIFUtility.getActiveDesktop().getFrame());
						waitBar.start();
						final ComparePreBOMWithDateOperation operation = 
								new ComparePreBOMWithDateOperation(selectedItem, oldDateChooser.getDate(), newDateChooser.getDate(), waitBar);
						operation.addOperationListener(new InterfaceAIFOperationListener() {
							
							@Override
							public void startOperation(String s) {
								// TODO Auto-generated method stub
								ComparePreBOMDlg.this.setVisible(false);
							}
							
							@Override
							public void endOperation() {
								// TODO Auto-generated method stub
								HashMap<String, Object> resultData = (HashMap<String, Object>)operation.getOperationResult();
								Object obj = resultData.get(ComparePreBOMWithDateOperation.DATA_ERROR);
								if( obj != null){
									waitBar.setStatus(((Exception)obj).getMessage());
									waitBar.setShowButton(true);
									return;
								}
								
								try {
									waitBar.setStatus("Importing...");
									exportToExcel(resultData);
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									waitBar.dispose();
									ComparePreBOMDlg.this.setVisible(true);
								}
							}
						});
						selectedItem.getSession().queueOperation(operation);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	private void exportToExcel(HashMap<String, Object> resultData){
		
		String itemId = resultData.get(ComparePreBOMWithDateOperation.DATA_ITEM_ID).toString();
		Date oldDate = (Date)resultData.get(ComparePreBOMWithDateOperation.DATA_OLD_DATE);
		Date newDate = (Date)resultData.get(ComparePreBOMWithDateOperation.DATA_NEW_DATE);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String preStr = "MasterList_Comparison_" + itemId + "_" + sdf.format(oldDate) + "-" + sdf.format(oldDate);
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File(preStr + sdf.format(now.getTime()) + ".xls");
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
		int result = fileChooser.showSaveDialog(this);
		if( result == JFileChooser.APPROVE_OPTION){
			File selectedFile = fileChooser.getSelectedFile();
			try
            {
				export(selectedFile, resultData);
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
	
	private HashMap<String, Vector> getMapData(Vector<Vector> data){
		HashMap<String, Vector> resultMap = new HashMap();
		for( int i = 0; data != null &&  i < data.size(); i++){
			Vector row = data.get(i);
			//불필요한 첫 열 제거
			row.remove(0);
			CellValue cellValue = (CellValue)row.get(MasterListTablePanel.MASTER_LIST_PART_ID_IDX);
			HashMap cellData = cellValue.getData();
			String key = (String)cellData.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
			resultMap.put(key, row);
		}
		
		return resultMap;
	}
	
	public void export(File selectedFile, HashMap<String, Object> resultData) throws IOException, WriteException{
		
		Vector<Vector> oldData = (Vector<Vector>)resultData.get(ComparePreBOMWithDateOperation.DATA_OLD_DATA);
		Vector<Vector> newData = (Vector<Vector>)resultData.get(ComparePreBOMWithDateOperation.DATA_NEW_DATA);
		
		HashMap<String, Vector> oldMapData = getMapData(oldData);
		HashMap<String, Vector> newMapData = getMapData(newData);
		
		OSpec oldOspec = (OSpec)resultData.get(ComparePreBOMWithDateOperation.DATA_OLD_OSPEC);
		OSpec newOspec = (OSpec)resultData.get(ComparePreBOMWithDateOperation.DATA_NEW_OSPEC);
		
		HashMap<String, StoredOptionSet> oldStoredOptionSetMap = (HashMap<String, StoredOptionSet>)resultData.get(ComparePreBOMWithDateOperation.DATA_OLD_STORED_OPTION_SET);
		HashMap<String, StoredOptionSet> newStoredOptionSetMap = (HashMap<String, StoredOptionSet>)resultData.get(ComparePreBOMWithDateOperation.DATA_NEW_STORED_OPTION_SET);
		
		ArrayList<String> essentialNames = (ArrayList<String>)resultData.get(ComparePreBOMWithDateOperation.DATA_ESSENTIAL_NAMES);
		
		String currentUserName = (String)resultData.get("USER_NAME");
		String currentUserGroup = (String)resultData.get("USER_GROUP");
		boolean isCordinator = (boolean)resultData.get("IS_CORDINATOR");
		
		// [SR160316-025][20160325][jclee] MLM Compare BUG Fix
//		ArrayList<OpTrim> oldTrims = oldOspec.getTrimList();
//		ArrayList<OpTrim> newTrims = newOspec.getTrimList();
		ArrayList<OpTrim> oldTrims = (ArrayList<OpTrim>) oldOspec.getTrimList();
		ArrayList<OpTrim> newTrims = (ArrayList<OpTrim>) newOspec.getTrimList();
		
		// Trim을 통합함.
		ArrayList<OpTrim> trimsTemp = new ArrayList();
		ArrayList<OpTrim> trims = new ArrayList();
//		ArrayList<OpTrim> clonedTrim = (ArrayList<OpTrim>)newTrims.clone();
//		clonedTrim.removeAll(oldTrims);
//		
//		trims.addAll(oldTrims);
//		trims.addAll(clonedTrim);
		
		for (int inx = 0; inx < oldTrims.size(); inx++) {
			OpTrim oldTrim = oldTrims.get(inx);
			String sTrim = oldTrim.getTrim();
			String sArea = oldTrim.getArea();
			String sPass = oldTrim.getPassenger();
			String sEngine = oldTrim.getEngine();
			String sGrade = oldTrim.getGrade();
			
			if (sTrim == null || sArea == null || sPass == null || sEngine == null || sGrade == null) {
				continue;
			}
			
			boolean isContain = false;
			for (int jnx = 0; jnx < trimsTemp.size(); jnx++) {
				OpTrim trim = trimsTemp.get(jnx);
				String sTrim2 = trim.getTrim();
				
				if (sTrim.equals(sTrim2)) {
					isContain = true;
				}
			}
			
			if (!isContain) {
				trimsTemp.add(oldTrim);
			}
		}
		
		for (int inx = 0; inx < newTrims.size(); inx++) {
			OpTrim newTrim = newTrims.get(inx);
			String sTrim = newTrim.getTrim();
			String sArea = newTrim.getArea();
			String sPass = newTrim.getPassenger();
			String sEngine = newTrim.getEngine();
			String sGrade = newTrim.getGrade();
			
			if (sTrim == null || sArea == null || sPass == null || sEngine == null || sGrade == null) {
				continue;
			}
			
			boolean isContain = false;
			for (int jnx = 0; jnx < trimsTemp.size(); jnx++) {
				OpTrim trim = trimsTemp.get(jnx);
				String sTrim2 = trim.getTrim();
				
				if (sTrim.equals(sTrim2)) {
					isContain = true;
				}
			}
			
			if (!isContain) {
				trimsTemp.add(newTrim);
			}
		}
		
		// Trims Temp에 있는 변수를 Old OSpec(이미 Trim의 순서가 정렬되어있음)의 Col Order 순으로 정렬하여 Trims 변수에 입력 (Header 순서를 정렬하기 위함)
//		Collections.sort(trims);
		ArrayList<OpTrim> trimList = oldOspec.getTrimList();
		for (int inx = 0; inx < trimList.size(); inx++) {
			OpTrim opTrim = trimList.get(inx);
			String trim = opTrim.getTrim();
			
			for (int jnx = 0; jnx < trimsTemp.size(); jnx++) {
				OpTrim opTrim2 = trimsTemp.get(jnx);
				String trim2 = opTrim2.getTrim();
				
				if (trim.equals(trim2)) {
					trims.add(opTrim2);
					break;
				}
			}
		}
		
		String[] fixedColumnPre = new String[] { "UNIQ-No.", "CONTENTS", "SYSTEM", "SYSTEM NAME", "FUNC"
				,"LEV(MAN)", "LEV(A)", "SEQ", "Parent NO", "OLD P/NO", "P/NO", "P/NAME"
				, "REQ. OPT.", "SPEC Desc.", "OPT. Condition", "N,M,C,D", "Project"
				, "Rep. Qty."};
		
		String[] fixedColumnPost = new String[] { "S/MODE", "ESTIMATE", "TARGET", "Module", "ALTER PART"
				, "DR", "Responsibility", "Change Description", "ESTIMATE", "TARGET"
				, "NECESSARY QTY", "USE", "REQ. TEAM", "PERFORM", "PLAN"
				, "2D/3D", "REL. DATE", "PERFORM", "PLAN", "ECO/NO"
				, "Doc. No.", "Rel. Date", "TEAM", "CHARGER", "SELECTED COMPANY"
				, "PROTO TOOL'G", "TOOL'G", "SVC. COST", "SAMPLE", "SUM"
				, "TEAM", "CHARGER"};
		
		ArrayList header = new ArrayList();
		for( int i = 0; i < fixedColumnPre.length; i++){
			header.add(fixedColumnPre[i]);
		}
		header.addAll(trims);
		for( int i = 0; i < fixedColumnPost.length; i++){
			header.add(fixedColumnPost[i]);
		}
		
		WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);
	    // 0번째 Sheet 생성
	    WritableSheet sheet = workBook.createSheet("new sheet", 0);
	    
	    WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    cellFormat.setAlignment(Alignment.CENTRE);
	    cellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat oddCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    oddCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    oddCellFormat.setAlignment(Alignment.CENTRE);
	    oddCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat evenCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    evenCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    evenCellFormat.setAlignment(Alignment.CENTRE);
	    evenCellFormat.setBackground(Colour.AQUA);
	    evenCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    WritableCellFormat diffFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    diffFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
	    diffFormat.setAlignment(Alignment.CENTRE);
	    diffFormat.setBackground(Colour.ORANGE);
	    diffFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
	    
	    Label label = null;
	    
	    WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
	    headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
	    headerCellFormat.setBackground(Colour.GREY_25_PERCENT);

	    int startRow = 0;
	    int initColumnNum = 1;

	    //Header Write & 세로 Merge
	    for( int column = 0; column < header.size(); column++){
	    	
	    	for( int row = startRow; row < startRow + 6; row++){
    			label = new jxl.write.Label(0, startRow + row, 
						"Num",
						headerCellFormat);
				sheet.addCell(label);
    		}
	    	
	    	sheet.mergeCells(0, startRow, 0, startRow + 5);
	    	
	    	WritableCell wCell = sheet.getWritableCell(0, startRow);
			WritableCellFormat cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
			cf.setBackground(Colour.GREY_25_PERCENT);
			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
			cf.setAlignment(Alignment.CENTRE);
		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
		    wCell.setCellFormat(cf);
	    	
	    	Object obj = header.get(column);
	    	label = null;
	    	
	    	if( obj instanceof OpTrim){
	    		
	    		label = new jxl.write.Label(column + initColumnNum, startRow, 
						"Usage",
						headerCellFormat);
				sheet.addCell(label);
				
	    		OpTrim opTrim = (OpTrim)obj;
	    		label = new jxl.write.Label(column + initColumnNum, startRow + 1, 
	    				opTrim.getArea(),
						headerCellFormat);
				sheet.addCell(label);
				
				label = new jxl.write.Label(column + initColumnNum, startRow + 2, 
	    				opTrim.getPassenger(),
						headerCellFormat);
				sheet.addCell(label);
				
				label = new jxl.write.Label(column + initColumnNum, startRow + 3, 
	    				opTrim.getEngine(),
						headerCellFormat);
				sheet.addCell(label);
				
				label = new jxl.write.Label(column + initColumnNum, startRow + 4, 
	    				opTrim.getGrade(),
						headerCellFormat);
				sheet.addCell(label);
				
				label = new jxl.write.Label(column + initColumnNum, startRow + 5, 
	    				opTrim.getTrim(),
						headerCellFormat);
				sheet.addCell(label);
	    	}else{
	    		int headerStartRow = 0, headerEndRow = 6;
	    		switch(column){
	    			case 15:
	    			case 16:
	    				label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
								"ENG. CONCEPT",
								headerCellFormat);
						sheet.addCell(label);
	    				break;
	    			default : 
	    				int tmpCount = fixedColumnPre.length + trims.size();
	    				if( column >= tmpCount + 1 && column < tmpCount + 3){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"WEIGHT(KG)",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 8 && column < tmpCount + 10){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"MATERIAL COST",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 10 && column < tmpCount + 13){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"DVP SAMPLE",
									headerCellFormat);
							sheet.addCell(label);
//							label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
//									"PRT-TEST",
//									headerCellFormat);
//							sheet.addCell(label);
							
	    				}else if( column >= tmpCount + 13 && column < tmpCount + 17){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"CONCEPT DWG",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 17 && column < tmpCount + 20){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"PRD. DWG",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 20 && column < tmpCount + 22){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"Design Concept Doc.",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 22 && column < tmpCount + 24){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"DESIGN CHARGE",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 25 && column < tmpCount + 26){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"EST. INVESTMENT COST",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 26 && column < tmpCount + 30){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"PRD INVENSTMENT COST",
									headerCellFormat);
							sheet.addCell(label);
	    				}else if( column >= tmpCount + 30 && column < tmpCount + 32){
	    					label = new jxl.write.Label(column + initColumnNum, startRow + headerStartRow++, 
									"PROCUMENT",
									headerCellFormat);
	    					
							sheet.addCell(label);
	    				}
	    				
	    		}
	    		
	    		for( int row = headerStartRow; row < headerEndRow; row++){
	    			label = new jxl.write.Label(column + initColumnNum, startRow + row, 
							(String)header.get(column),
							headerCellFormat);
					sheet.addCell(label);
	    		}
	    		
	    		CellView cv = sheet.getColumnView(column + initColumnNum);
				cv.setSize(header.get(column).toString().length() * 700);
				
				sheet.setColumnView(column + initColumnNum, cv);
	    		
	    		sheet.mergeCells(column + initColumnNum, startRow + headerStartRow, column + initColumnNum, startRow + headerEndRow - 1);
	    		
	    		wCell = sheet.getWritableCell(column + initColumnNum, startRow + headerStartRow);
    			cf = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
    			cf.setBackground(Colour.GREY_25_PERCENT);
    			cf.setBorder(Border.ALL, BorderLineStyle.THIN); 
    			cf.setAlignment(Alignment.CENTRE);
    		    cf.setVerticalAlignment(VerticalAlignment.CENTRE);
    		    wCell.setCellFormat(cf);
	    		
	    	}
	    }
	    
	    //Header 가로 Merge
	    int startIdxToMerge = 0, endIdxToMerge = 0;
	    for( int i = 5; i >= 0; i--){
	    	startIdxToMerge = 0; 
	    	endIdxToMerge = 0;
	    	for( int j = 0; j < header.size(); j++){
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
	    
	    int postColumnStartIdx = fixedColumnPre.length + trims.size();
	    startRow = 6;
	    ArrayList<String> usedKeys = new ArrayList();
	    for( int i = 0; i < newData.size(); i++){
	    	Vector newRow = newData.get(i);
	    	CellValue cellValue = (CellValue)newRow.get(0);
	    	HashMap<String, Object> cellData = cellValue.getData();
	    	String key = (String)cellData.get(PropertyConstant.ATTR_NAME_BL_SYSTEM_ROW_KEY);
	    	if( !usedKeys.contains(key)){
	    		usedKeys.add(key);
	    	}
	    	
	    	WritableCellFormat currentCellFormat = (i % 2 == 1) ? evenCellFormat:oddCellFormat;
	    	
	    	label = new jxl.write.Label(0, startRow + (i * 2), 
					(i + 1) + "",
					currentCellFormat);
			sheet.addCell(label);
			
	    	Vector oldRow = oldMapData.get(key);
	    	if( oldRow == null){
	    		for( int j = 0; j < header.size(); j++){
	    			label = new jxl.write.Label(j + initColumnNum, startRow + (i * 2), 
							"",
							currentCellFormat);
					sheet.addCell(label);
	    		}
	    	}else{
	    		for( int j = 0; j < header.size(); j++){
	    			
	    			String valueStr = oldRow.get(j).toString();
	    			//보안 적용
//		    		if( j == postColumnStartIdx + 8 
//		    				|| j == postColumnStartIdx + 9
//							|| (j >= postColumnStartIdx + 25 && j < postColumnStartIdx + 30)){
//		    			
//		    			String deptStr = oldRow.get(22).toString();
//		    			String resStr = oldRow.get(23).toString();
//		    			
//		    			if( currentUserGroup.equalsIgnoreCase(deptStr)
//								&& currentUserName.equalsIgnoreCase(resStr)){
//						}else{
//							if( currentUserGroup.equalsIgnoreCase(deptStr) && isCordinator){
//							}else{
//								valueStr = "******";
//							}
//						}
//		    		}
	    			
	    			label = new jxl.write.Label(j + initColumnNum, startRow + (i * 2), 
	    					valueStr,
							currentCellFormat);
					sheet.addCell(label);
	    		}
	    	}
	    	
	    	label = new jxl.write.Label(0, startRow + (i * 2) + 1, 
	    			(i + 1) + "",currentCellFormat);
			sheet.addCell(label);
			
			sheet.mergeCells(0, startRow + (i * 2), 0, startRow + (i * 2) + 1);
			
	    	boolean isDifference = false;
	    	for( int j = 0; j < header.size(); j++){
	    		
	    		isDifference = false;
	    		if( oldRow != null){
	    			if( !newRow.get(j).toString().equals(oldRow.get(j).toString())){
	    				isDifference = true;
	    			}
	    		}
	    		
	    		String valueStr = newRow.get(j).toString();
    			//보안 적용
//	    		if( j == postColumnStartIdx + 8 
//	    				|| j == postColumnStartIdx + 9
//						|| (j >= postColumnStartIdx + 25 && j < postColumnStartIdx + 30)){
//	    			
//	    			String deptStr = newRow.get(22).toString();
//	    			String resStr = newRow.get(23).toString();
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
	    		
    			label = new jxl.write.Label(j + initColumnNum, startRow + (i * 2) + 1, 
    					valueStr,
						isDifference ?  diffFormat:currentCellFormat);
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
}

