package com.kgm.commands.replace;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import jxl.Cell;
import jxl.LabelCell;
import jxl.Sheet;
import jxl.Workbook;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.SYMCBOMLine;
import com.teamcenter.rac.kernel.SYMCBOMWindow;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class MultiReplaceDlg extends AbstractAIFDialog {

	private final JPanel contentPanel = new JPanel();
	private JTextField tfPath;
	private int iResultCount = 0;
	private int iTotalCount = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MultiReplaceDlg dialog = new MultiReplaceDlg();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public MultiReplaceDlg() {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		init();
	}

	private void init(){
		setTitle("Multi-Replace");
		setBounds(100, 100, 350, 210);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(2, 1, 0, 0));
		{
			JPanel panel = new JPanel();
			panel.setBorder(new TitledBorder(null, "Please select the Excel form", TitledBorder.LEADING, TitledBorder.TOP, null, null));
			FlowLayout flowLayout = (FlowLayout) panel.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel);
			{
				tfPath = new JTextField();
				panel.add(tfPath);
				tfPath.setColumns(20);
			}
			{
				JButton btnSearch = new JButton("Search");
				btnSearch.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileFilter(new FileFilter(){

							@Override
							public boolean accept(File f) {
								// TODO Auto-generated method stub
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
								// TODO Auto-generated method stub
								return "*.xls";
							}

						});
						int result = fileChooser.showOpenDialog(MultiReplaceDlg.this);
						if( result == JFileChooser.APPROVE_OPTION){
							File selectedFile = fileChooser.getSelectedFile();
							String absPath = selectedFile.getAbsolutePath();
							tfPath.setText(absPath);
						}	
					}
				});
				panel.add(btnSearch);
			}
		}
		{
			JPanel panel_1 = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
			flowLayout.setAlignment(FlowLayout.TRAILING);
			contentPanel.add(panel_1);
			{
				JButton btnNewButton = new JButton("Excel Form Download");
				btnNewButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						
						JFileChooser fileChooser = new JFileChooser();
						fileChooser.setFileFilter(new FileFilter(){

							@Override
							public boolean accept(File f) {
								// TODO Auto-generated method stub
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
								// TODO Auto-generated method stub
								return "*.xls";
							}

						});
						int result = fileChooser.showSaveDialog(MultiReplaceDlg.this);
						if( result == JFileChooser.APPROVE_OPTION){
							File selectedFile = fileChooser.getSelectedFile();
							String absPath = selectedFile.getAbsolutePath();
							int idx = absPath.lastIndexOf(".");
							String extStr = absPath.substring(idx + 1);
							if( !extStr.toLowerCase().equals("xls") && !extStr.toLowerCase().equals("xlsx")){
								absPath += ".xls";
							}
							idx = absPath.lastIndexOf("\\");
							String fileName = absPath.substring(idx + 1);
							File dir = selectedFile.getParentFile();
							downloadForm(dir.getAbsolutePath(), fileName);
						}	
						
					}
				});
				panel_1.add(btnNewButton);
			}
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent event) {
						String path = tfPath.getText();
						
						File file = new File(path);
						try {
							importTo(file);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							MessageBox.post(MultiReplaceDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
						}
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	private void downloadForm(String dir, String fileName){
		TCSession session = CustomUtil.getTCSession();
		try {
			TCComponentItemRevision revision = SYMTcUtil.findItemRevision(session, "MultiReplaceItem", "000");
			AIFComponentContext[] contexts = revision.getRelated(SYMCECConstant.ITEM_DATASET_REL);
			if( contexts != null && contexts.length > 0){
				TCComponentDataset dataSet = (TCComponentDataset) contexts[0].getComponent();
				TCComponentTcFile[] imanFile = SYMTcUtil.getImanFile(dataSet);
				if( imanFile != null && imanFile.length > 0){
					File file = imanFile[0].getFile(dir, fileName);
					
				}
				
			}
			
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void importTo(File file) throws Exception{
		
		HashMap<String, HashMap<String, String>> replaceMap = new HashMap();
	   	Workbook workBook = Workbook.getWorkbook(file);
    	Sheet sheet = workBook.getSheet(0);
    	
    	final WaitProgressBar waitBar = new WaitProgressBar(this);
    	waitBar.start();
    	waitBar.setStatus("File loaded.");
    	
    	int rowIdx = 3;
    	iTotalCount = sheet.getRows() - rowIdx;
    	
    	for (int inx = 0; inx < ((sheet.getRows() - rowIdx) / 50) + 1; inx++) {
    		
    		replaceMap = new HashMap();
    		for( int row = (inx * 50) + rowIdx; row < ((inx + 1) * 50 + rowIdx < sheet.getRows() ? ((inx + 1) * 50) + rowIdx : sheet.getRows()); row++){
    			
    			Cell parentCell = sheet.getCell(0, row);
    			Cell oldPartCell = sheet.getCell(1, row);
    			Cell newPartCell = sheet.getCell(2, row);
    			
//    		if( !(parentCell instanceof LabelCell) || !(oldPartCell instanceof LabelCell) || !(newPartCell instanceof LabelCell)){
//    			continue;
//    		}
    			
    			String parentId = parentCell.getContents().trim().toUpperCase();
    			String oldPart = oldPartCell.getContents().trim().toUpperCase();
    			String newPart = newPartCell.getContents().trim().toUpperCase();
    			
    			if( parentId.equals("") || oldPart.equals("") || newPart.equals("")){
    				continue;
    			}
    			
    			HashMap<String, String> pairMap = replaceMap.get(parentId);
    			if( pairMap == null){
    				pairMap = new HashMap<String, String>();
    				pairMap.put(oldPart, newPart);
    				replaceMap.put(parentId, pairMap);
    			}else{
    				if( pairMap.containsKey(oldPart)){
    					waitBar.setStatus("Duplicated old part[" + oldPart + "]");
    					waitBar.setShowButton(true);
    					return;
    				}
    				pairMap.put(oldPart, newPart);
    			}
    		}
    		
    		if( replaceMap.isEmpty()){
    			waitBar.setStatus("Could not find Replace Target");
    			waitBar.setShowButton(true);
    			return;
    		}
    		
    		final ReplaceOperation op = new ReplaceOperation(replaceMap, waitBar);
    		CustomUtil.getTCSession().queueOperation(op);
    		op.addOperationListener(new InterfaceAIFOperationListener(){
    			
    			@Override
    			public void endOperation() {
    				// TODO Auto-generated method stub
//    				if( op.errorList.isEmpty() && iResultCount == iTotalCount){
    				if( op.errorList.isEmpty()){
    					waitBar.dispose();
    					dispose();
    				}
    			}
    			
    			@Override
    			public void startOperation(String arg0) {
    				// TODO Auto-generated method stub
    				
    			}
    			
    		});
		}
    	
	}
	
	class ReplaceOperation extends AbstractAIFOperation{
		private HashMap<String, HashMap<String, String>> replaceMap;
		private WaitProgressBar waitBar;
		ArrayList<String> errorList = new ArrayList();
		
		ReplaceOperation(HashMap<String, HashMap<String, String>> replaceMap, WaitProgressBar waitBar){
			this.replaceMap = replaceMap;
			this.waitBar = waitBar;
		}
		
		@Override
		public void executeOperation() throws Exception {
			// TODO Auto-generated method stub
			mulReplace(replaceMap);
		}
		
		private void mulReplace(HashMap<String, HashMap<String, String>> replaceMap) throws Exception{
			if( replaceMap == null || replaceMap.isEmpty()){
				return;
			}
			
			String[] parents = replaceMap.keySet().toArray(new String[replaceMap.size()]);
			
			ExecutorService executor = Executors.newFixedThreadPool(10);
			for( int i = 0; i < parents.length; i++){
				HashMap<String, String> pairMap = replaceMap.get(parents[i]);
				SingleReplaceThread t = new SingleReplaceThread(parents[i], pairMap);
				executor.execute(t);				
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}
			
			if( !errorList.isEmpty()){
				for( String errorStr : errorList){
					waitBar.setStatus(errorStr);
				}
				waitBar.setShowButton(true);
			}
		}
		
		class SingleReplaceThread extends Thread{
			
			String parentId;
			HashMap<String, String> pairMap;
			
			SingleReplaceThread(String parentId, HashMap<String, String> pairMap){
				this.parentId = parentId;
				this.pairMap = pairMap;
			}
			
			public void run(){
				TCSession session = (TCSession)ReplaceOperation.this.getSession();
				TCComponentItem parentItem;
				
				try {
					parentItem = SYMTcUtil.findItem(session, parentId);
					if( parentItem == null){
						synchronized(waitBar){
							throw new Exception(" Could not found the " + parentId);
						}
					}
					TCComponentItemRevision parentLatestRevision = parentItem.getLatestItemRevision();
					if( CustomUtil.isReleased(parentLatestRevision)){
						synchronized(waitBar){
							throw new Exception(parentId + " is not a Working condition.");
						}
					}
					
					SYMCBOMWindow window = null;
					try{
						window = (SYMCBOMWindow)CustomUtil.createBOMWindow();
						window.skipHistory(true);
						TCComponentBOMLine topLine = window.setWindowTopLine(null, parentLatestRevision, null, null);
						AIFComponentContext[] childContexts = topLine.getChildren( new String[]{"bl_item_item_id"}, null);
						
						if( childContexts == null ){
							throw new Exception(parentId + " is the child does not exist.");
						}
						TCComponentItemRevision parentECORev = (TCComponentItemRevision)parentLatestRevision.getReferenceProperty("s7_ECO_NO");
						for(AIFComponentContext childContext : childContexts){
							Calendar calStart = Calendar.getInstance();
							long lStartTime = calStart.getTimeInMillis();
							
							SYMCBOMLine childLine = (SYMCBOMLine)childContext.getComponent();
							String oldPartId = childLine.getProperty("bl_item_item_id");
							String newPartId = pairMap.get(oldPartId);
							if( pairMap.containsKey(oldPartId)){
								TCComponentItem newItem = SYMTcUtil.findItem(session, newPartId);
								TCComponentItemRevision childRev = newItem.getLatestItemRevision();
								
								if( childRev.getProperty("release_status_list").equals("")){
									childRev.setReferenceProperty("s7_ECO_NO", parentECORev);
								}
								childLine.replace(newItem, newItem.getLatestItemRevision(), null);
								
								iResultCount++;
								Calendar calEnd = Calendar.getInstance();
								long lEndTime = calEnd.getTimeInMillis();
								System.out.println(String.valueOf(iResultCount) + " > " + oldPartId + "->" + newPartId + " : " + Math.abs(lEndTime - lStartTime));
								
								synchronized(waitBar){
									waitBar.setStatus(oldPartId + " is replaced with " + newPartId);
								}
								
							}
						}
						window.save();
						
					}catch(Exception e){
						throw e;
					}finally{
						if( window != null){
							window.close();
						}
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					errorList.add(e1.getMessage());
				}
			}
		}
	}
}
