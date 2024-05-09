package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

import com.kgm.commands.ospec.op.OSpec;
import com.kgm.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.masterlist.view.MasterListReq;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.util.MessageBox;

public class MasterListPreBomViewDlg extends JFrame implements MasterListReq {

	private final JPanel contentPanel = new JPanel();

	private OSpec ospec = null;
	private Vector<Vector> data = null;
	private HashMap<String, Vector> keyRowMapper = null, releaseKeyRowMapper;
	
	private ScriptEngineManager manager = new ScriptEngineManager();
	private ScriptEngine engine = manager.getEngineByName("js"); 
	private ArrayList<String> essentialNames = null;
	private MasterListTablePanel masterListTablePanel = null;
	private String title = null;
	private String currentUserId = null;
	private String currentUserName;
	private String currentGroup;
	private String currentPa6Group = null;
	private boolean isCordinator;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MasterListPreBomViewDlg dialog = new MasterListPreBomViewDlg("", null, null, null, null, null, null, null, null, null, false);
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
	public MasterListPreBomViewDlg(String title, OSpec ospec, Vector<Vector> data,
			HashMap<String, Vector> keyRowMapper, HashMap<String, Vector> releaseKeyRowMapper,
			ArrayList<String> essentialNames, String currentUserId, String currentUserName, 
			String currentGroup, String currentPa6Group, boolean isCordinator) throws Exception {
//		super(AIFUtility.getActiveDesktop().getFrame(), false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.title = title;
		this.ospec = ospec;
		this.data = data;
		
		this.keyRowMapper = keyRowMapper;
		if( this.keyRowMapper == null){
			this.keyRowMapper = new HashMap();
		}
		this.releaseKeyRowMapper = releaseKeyRowMapper;
		if( this.releaseKeyRowMapper == null){
			this.releaseKeyRowMapper = new HashMap();
		}
		this.essentialNames = essentialNames;
		
		this.currentUserId = currentUserId;
		this.currentUserName = currentUserName;
		this.currentGroup = currentGroup;
		this.currentPa6Group = currentPa6Group; 
		this.isCordinator = isCordinator;
		
		init();
	}
	
	private void init() throws Exception{
		setTitle(title);
		setIconImage( Toolkit.getDefaultToolkit().getImage(ValidationDlg.class.getResource("/icons/tcdesktop_16.png")) );
		setBounds(100, 100, 1024, 400);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		masterListTablePanel = new MasterListTablePanel(this, ospec, data);
		contentPanel.add(masterListTablePanel, BorderLayout.CENTER);
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						dispose();
					}
				});
				{
					JButton btnExport = new JButton("Export");
					btnExport.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							exportToExcel();
						}
					});
					buttonPane.add(btnExport);
				}
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
			}
		}
	}
	
	private void exportToExcel(){
		
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY );
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddkkmmss");
		sdf.format(now.getTime());
		File defaultFile = new File(title + "_" + sdf.format(now.getTime()) + ".xls");
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
				MasterListDlg.export(selectedFile, masterListTablePanel, currentUserName, currentGroup, isCordinator);
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

	@Override
	public String getProject() {
		// TODO Auto-generated method stub
		return ospec.getProject();
	}

	@Override
	public ArrayList<String> getEssentialNames() {
		// TODO Auto-generated method stub
		return essentialNames;
	}

	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public TCComponentItemRevision getFmpRevision() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashMap<String, Vector> getKeyRowMapper() {
		// TODO Auto-generated method stub
		return keyRowMapper;
	}

	@Override
	public HashMap<String, Vector> getReleaseKeyRowMapper() {
		// TODO Auto-generated method stub
		return releaseKeyRowMapper;
	}

	@Override
	public OptionManager getOptionManager() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<VariantOption> getEnableOptionSet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<TCComponentBOMLine> getBOMLines(String systemRowKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getCurrentUserId() {
		// TODO Auto-generated method stub
		return currentUserId;
	}
	
	@Override
	public String getCurrentUserName() {
		// TODO Auto-generated method stub
		return currentUserName;
	}

	@Override
	public String getCurrentUserGroup() {
		// TODO Auto-generated method stub
		return currentGroup;
	}

	@Override
	public String getCurrentUserPa6Group() {
		// TODO Auto-generated method stub
		return currentPa6Group;
	}

	@Override
	public boolean isCordinator() {
		// TODO Auto-generated method stub
		return isCordinator;
	}

	@Override
	public HashMap<String, ArrayList<String>> getWorkingChildRowKeys() {
		// TODO Auto-generated method stub
		return null;
	}

}
