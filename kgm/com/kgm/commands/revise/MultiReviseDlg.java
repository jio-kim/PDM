package com.kgm.commands.revise;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.common.SYMCClass;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentRevisionRule;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;

public class MultiReviseDlg extends AbstractAIFDialog {
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	private JTextField tfPath;
	private int iResultCount = 0;
	private int iTotalCount = 0;
	
	/** 2D CheckBox */
	public JCheckBox twoDCheckBox = new JCheckBox("2D CAD");
	/** 3D CheckBox */
	public JCheckBox threeDCheckBox = new JCheckBox("3D CAD");
	/** SoftWare CheckBox */
	public JCheckBox softwareCheckBox = new JCheckBox("Software");

	/**
	 * Create the dialog.
	 */
	public MultiReviseDlg() {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		init();
	}

	private void init() {
		setTitle("Multi-Revise");
		setBounds(100, 100, 350, 210);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new GridLayout(2, 1, 0, 0));
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(null, "Please select the Excel form", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		FlowLayout flowLayoutTemp = (FlowLayout) panel.getLayout();
		flowLayoutTemp.setAlignment(FlowLayout.TRAILING);
		contentPanel.add(panel);
		tfPath = new JTextField();
		panel.add(tfPath);
		tfPath.setColumns(20);
		JButton btnSearch = new JButton("Search");
		btnSearch.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						if (f.isFile()) {
							return f.getName().endsWith("xls");
						}
						return false;
					}

					@Override
					public String getDescription() {
						return "*.xls";
					}

				});
				int result = fileChooser.showOpenDialog(MultiReviseDlg.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String absPath = selectedFile.getAbsolutePath();
					tfPath.setText(absPath);
				}
			}
		});
		panel.add(btnSearch);
		
		JPanel panel_1 = new JPanel();
		FlowLayout flowLayout = (FlowLayout) panel_1.getLayout();
		flowLayout.setAlignment(FlowLayout.TRAILING);
		contentPanel.add(panel_1);
		JButton btnNewButton = new JButton("Excel Form Download");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileFilter(new FileFilter() {

					@Override
					public boolean accept(File f) {
						if (f.isDirectory()) {
							return true;
						}

						if (f.isFile()) {
							return f.getName().endsWith("xls");
						}
						return false;
					}

					@Override
					public String getDescription() {
						return "*.xls";
					}

				});
				int result = fileChooser.showSaveDialog(MultiReviseDlg.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String absPath = selectedFile.getAbsolutePath();
					int idx = absPath.lastIndexOf(".");
					String extStr = absPath.substring(idx + 1);
					if (!extStr.toLowerCase().equals("xls") && !extStr.toLowerCase().equals("xlsx")) {
						absPath += ".xls";
					}
					idx = absPath.lastIndexOf("\\");
					String fileName = absPath.substring(idx + 1);
					File dir = selectedFile.getParentFile();
					downloadForm(dir.getAbsolutePath(), fileName);
				}
			}
		});
		
		JPanel panelDwg = new JPanel(new PropertyLayout());
		//panelDwg.setBorder(new SYMCAWTTitledBorder(""));
		panelDwg.setOpaque(false);
		contentPanel.add(panelDwg);
		
		threeDCheckBox.setOpaque(false);
		twoDCheckBox.setOpaque(false);
		softwareCheckBox.setOpaque(false);

		threeDCheckBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent paramActionEvent) {
				
				if (threeDCheckBox.isSelected())
				{
					twoDCheckBox.setSelected(true);
				}
			}
		});
		
		panelDwg.add("1.1.right.center.preferred.preferred", new JLabel("DWG Chg"));
		panelDwg.add("1.2.left.center.preferred.preferred", threeDCheckBox);
		panelDwg.add("1.3.left.center.preferred.preferred", twoDCheckBox);
		panelDwg.add("1.4.left.center.preferred.preferred", softwareCheckBox);
		panel_1.add(panelDwg);
		panel_1.add(btnNewButton);
		
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		JButton okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				String path = tfPath.getText();

				File file = new File(path);
				try {
					importTo(file);
				} catch (Exception e) {
					e.printStackTrace();
					MessageBox.post(MultiReviseDlg.this, e.getMessage(), "ERROR", MessageBox.ERROR);
				}
			}
		});
		buttonPane.add(okButton);
		getRootPane().setDefaultButton(okButton);
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		cancelButton.setActionCommand("Cancel");
		buttonPane.add(cancelButton);
	}

	private void downloadForm(String dir, String fileName) {
		TCSession session = CustomUtil.getTCSession();
		try {
			TCComponentItemRevision revision = SYMTcUtil.findItemRevision(session, "MultiReviseItem", "000");
			AIFComponentContext[] contexts = revision.getRelated(SYMCECConstant.ITEM_DATASET_REL);
			if (contexts != null && contexts.length > 0) {
				TCComponentDataset dataSet = (TCComponentDataset) contexts[0].getComponent();
				TCComponentTcFile[] imanFile = SYMTcUtil.getImanFile(dataSet);
				if (imanFile != null && imanFile.length > 0) {
					imanFile[0].getFile(dir, fileName);
				}
			}

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void importTo(File file) throws Exception {
		HashMap<String, String> reviseMap = new HashMap<String, String>();
		Workbook workBook = Workbook.getWorkbook(file);
		Sheet sheet = workBook.getSheet(0);

		final WaitProgressBar waitBar = new WaitProgressBar(this);
		waitBar.start();
		waitBar.setStatus("File loaded.");

		int rowIdx = 3;

		HashMap<String, String> reviseMapTemp = new HashMap<String, String>();
		for (int inx = rowIdx; inx < sheet.getRows(); inx++) {
			Cell partNoCell = sheet.getCell(0, inx);
			Cell ecoNoCell = sheet.getCell(1, inx);
			
			String sPartNo = partNoCell.getContents().trim().toUpperCase();
			String sECONo = ecoNoCell.getContents().trim().toUpperCase();
			
			if (sPartNo != null && !sPartNo.equals("") && sECONo != null && !sECONo.equals("")) {
				reviseMapTemp.put(sPartNo, sECONo);
			}
		}
		
		// 중복 제거
		Object[] oPartNos = reviseMapTemp.keySet().toArray();
		for (int inx = 0; inx < oPartNos.length; inx++) {
			String sECONoTemp = reviseMapTemp.get(oPartNos[inx].toString());
			if (reviseMap.isEmpty()) {
				reviseMap.put(oPartNos[inx].toString(), sECONoTemp);
			} else {
				if (reviseMap.containsKey(oPartNos[inx].toString())) {
					waitBar.setStatus("List have duplicate part no.\nCould not revise.\n\n" + oPartNos[inx].toString());
					waitBar.setShowButton(true);
					return;
				} else {
					reviseMap.put(oPartNos[inx].toString(), sECONoTemp);
				}
			}
		}
		
		if (reviseMap.isEmpty()) {
			waitBar.setStatus("Could not find Revise Target");
			waitBar.setShowButton(true);
			return;
		} else {
			//[SR190821-022][CSH]Function과 Item ID가 불일치하는 FMP 개정 방지
			Set<String> keySet = reviseMap.keySet();
			Iterator<String> iterator = keySet.iterator();
			TCComponentItem item = null;
			TCComponentItemRevision partLatestRevision = null;
			TCSession session = (TCSession)AIFUtility.getCurrentApplication().getSession();
			while (iterator.hasNext()) {
				String partNo = iterator.next();
				item = SYMTcUtil.findItem(session, partNo);
				if (item == null) {
					waitBar.setStatus(" Could not found the Item : " + partNo);
					waitBar.setShowButton(true);
					return;
				}
				
				partLatestRevision = item.getLatestItemRevision();
				if(partLatestRevision.getType().equals(SYMCClass.S7_FNCMASTPARTREVISIONTYPE)){
					String fmasterId = partLatestRevision.getProperty("item_id");
					String functionId = "";

					TCComponentRevisionRule revRule = SYMTcUtil.getRevisionRule(session, "Latest Working");
					TCComponent[] imanComps = partLatestRevision.whereUsed(TCComponent.WHERE_USED_CONFIGURED, revRule);
					TCComponentItemRevision fncRev = null;
					for (int j = 0; j < imanComps.length; j++) {
						if( SYMCClass.S7_FNCPARTREVISIONTYPE.equals( imanComps[j].getType())){
							fncRev = (TCComponentItemRevision)imanComps[j];
							break;
						}
					}

					if(fncRev != null){
						functionId = fncRev.getProperty("item_id");
					}

					if(functionId.equals("")){
						waitBar.setStatus(partNo + "\nFunction이 미존재하여 개정이 불가한 FMP입니다.\n기술관리팀에 문의하세요.");
						waitBar.setShowButton(true);
						return;
					}

					if(!fmasterId.substring(1,fmasterId.length()-1).equals(functionId.substring(1))) {
						waitBar.setStatus(partNo + "\nFunction과 Item ID가 불일치하여 Revise 불가한 FMP입니다.\n기술관리팀에 문의하세요.");
						waitBar.setShowButton(true);
						return;
					}
				}
			}
		}

		iTotalCount = reviseMap.size();
		
		final ReviseOperation op = new ReviseOperation(reviseMap, waitBar);
		CustomUtil.getTCSession().queueOperation(op);
		op.addOperationListener(new InterfaceAIFOperationListener() {
			@Override
			public void endOperation() {
				if (op.errorList.isEmpty() && iResultCount == iTotalCount) {
					waitBar.dispose();
					dispose();
				}
			}
			
			@Override
			public void startOperation(String arg0) {
				
			}
		});
	}

	class ReviseOperation extends AbstractAIFOperation {
		private HashMap<String, String> reviseMap;
		private WaitProgressBar waitBar;
		ArrayList<String> errorList = new ArrayList<String>();

		ReviseOperation(HashMap<String, String> reviseMap, WaitProgressBar waitBar) {
			this.reviseMap = reviseMap;
			this.waitBar = waitBar;
		}

		@Override
		public void executeOperation() throws Exception {
			mulRevise(reviseMap);
		}

		private void mulRevise(HashMap<String, String> reviseMap) throws Exception {
			if (reviseMap == null || reviseMap.isEmpty()) {
				return;
			}

			String[] partNos = reviseMap.keySet().toArray(new String[reviseMap.size()]);

			ExecutorService executor = Executors.newFixedThreadPool(10);
			for (int inx = 0; inx < partNos.length; inx++) {
				String sECONo = reviseMap.get(partNos[inx]);
				SingleReviseThread tSingleRevise = new SingleReviseThread(partNos[inx], sECONo);
				executor.execute(tSingleRevise);
			}
			executor.shutdown();
			while (!executor.isTerminated()) {
			}

			if (!errorList.isEmpty()) {
				for (String errorStr : errorList) {
					waitBar.setStatus(errorStr);
				}
				waitBar.setShowButton(true);
			}
		}

		class SingleReviseThread extends Thread {

			String sPartNo;
			String sECONo;

			SingleReviseThread(String sPartNo, String sECONo) {
				this.sPartNo = sPartNo;
				this.sECONo = sECONo;
			}

			public void run() {
				TCSession session = (TCSession) ReviseOperation.this.getSession();
				TCComponentItem parentItem;
				TCComponentItem ecoItem;

				try {
					parentItem = SYMTcUtil.findItem(session, sPartNo);
					ecoItem = SYMTcUtil.findItem(session, sECONo);
					if (parentItem == null) {
						synchronized (waitBar) {
							throw new Exception(" Could not found the " + sPartNo);
						}
					}
					
					TCComponentItemRevision partLatestRevision = parentItem.getLatestItemRevision();
					TCComponentItemRevision ecoLatestRevision = ecoItem.getLatestItemRevision();
					if (!CustomUtil.isReleased(partLatestRevision)) {
//						synchronized (waitBar) {
//							throw new Exception(sPartNo + " is not released.");
//						}
						
						return;
					}
					
					if (CustomUtil.isReleased(ecoLatestRevision) || !ecoLatestRevision.getProperty("item_revision_id").equals("000")) {
						synchronized (waitBar) {
							throw new Exception(sECONo + " is not able to use for revise.");
						}
					}
					
					boolean is3DCheck = threeDCheckBox.isSelected() ? true : false;
					boolean is2DCheck = twoDCheckBox.isSelected() ? true : false;
					boolean isSoftwareCheck = softwareCheckBox.isSelected() ? true : false;
					// 도면 선택
					//TCComponentItemRevision newRevision = CustomUtil.reviseForItemRev(partLatestRevision, false, false, false, false, "Multi Revise", "P", ecoLatestRevision);
					TCComponentItemRevision newRevision = CustomUtil.reviseForItemRev(partLatestRevision, is3DCheck, is2DCheck, isSoftwareCheck, false, "Multi Revise", "P", ecoLatestRevision);
					if (newRevision == null) {
						synchronized (waitBar) {
							throw new Exception(sPartNo + " is failed to revise.");
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					errorList.add(e.getMessage());
				} finally {
					iResultCount++;
					synchronized (waitBar) {
						waitBar.setStatus("Count : " + String.valueOf(iTotalCount) + "/" + String.valueOf(iResultCount));
						System.out.println("Count : " + String.valueOf(iTotalCount) + "/" + String.valueOf(iResultCount));
					}
				}
			}
		}
	}
}
