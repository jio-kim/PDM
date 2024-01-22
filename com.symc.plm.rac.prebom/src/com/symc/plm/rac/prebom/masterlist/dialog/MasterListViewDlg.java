package com.symc.plm.rac.prebom.masterlist.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.ssangyong.commands.ospec.op.OSpec;
import com.ssangyong.common.utils.variant.VariantOption;
import com.symc.plm.rac.prebom.common.util.OptionManager;
import com.symc.plm.rac.prebom.masterlist.view.MasterListReq;
import com.symc.plm.rac.prebom.masterlist.view.MasterListTablePanel;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCComponentItemRevision;

public class MasterListViewDlg extends AbstractAIFDialog implements MasterListReq{

	private final JPanel contentPanel = new JPanel();
	private MasterListDlg parentDlg = null;
	private OSpec ospec = null;
	private Vector<Vector> data = null;
	private HashMap keyRowMapper, releaseKeyRowMapper = null;
	private String currentUserId = null,currentUserName = null, currentUserGroup = null, currentPa6Group = null;
	private boolean isCordinator = false;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			MasterListViewDlg dialog = new MasterListViewDlg(null, null, null, null, null, null, null , null, null, false);
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public MasterListViewDlg(MasterListDlg parentDlg, OSpec ospec, Vector<Vector> data
			, HashMap keyRowMapper, HashMap releaseKeyRowMapper
			, String currentUserId, String currentUserName, String currentUserGroup, String currentPa6Group, boolean isCordinator) throws Exception {
		super(parentDlg, false);
		
		this.parentDlg = parentDlg;
		this.ospec = ospec;
		this.data = data;
		
		this.keyRowMapper = keyRowMapper;
		this.releaseKeyRowMapper = releaseKeyRowMapper;
		this.currentUserId = currentUserId;
		this.currentUserName = currentUserName;
		this.currentUserGroup = currentUserGroup;
		this.currentPa6Group = currentPa6Group;
		this.isCordinator = isCordinator;
		
		init();
	}

	/**
	 * Create the dialog.
	 * @throws Exception 
	 */
	public void init() throws Exception {
		// [20160427][jclee] Dialog Size Á¶Á¤
//		setBounds(100, 100, 450, 300);
		setSize(AIFDesktop.getActiveDesktop().getSize().width - 20, AIFDesktop.getActiveDesktop().getSize().height - 20);
		
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));
		
		MasterListTablePanel masterListTablePanel = new MasterListTablePanel(this, ospec, data);
		contentPanel.add(masterListTablePanel, BorderLayout.CENTER);
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton closeButton = new JButton("Close");
				closeButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent actionevent) {
						dispose();
					}
				});
				closeButton.setActionCommand("Close");
				buttonPane.add(closeButton);
			}
		}
	}

	@Override
	public String getProject() {
		return parentDlg.getProject();
	}

	@Override
	public ArrayList<String> getEssentialNames() {
		return parentDlg.getEssentialNames();
	}

	@Override
	public boolean isEditable() {
		return false;
	}

	@Override
	public TCComponentItemRevision getFmpRevision() {
		return parentDlg.getFmpRevision();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HashMap<String, Vector> getKeyRowMapper() {
		return keyRowMapper;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public HashMap<String, Vector> getReleaseKeyRowMapper() {
		return releaseKeyRowMapper;
	}

	@Override
	public OptionManager getOptionManager() {
		return null;
	}

	@Override
	public ArrayList<VariantOption> getEnableOptionSet() {
		return null;
	}

	@Override
	public ArrayList<TCComponentBOMLine> getBOMLines(String systemRowKey) {
		return null;
	}

	@Override
	public String getCurrentUserId() {
		return currentUserId;
	}

	@Override
	public String getCurrentUserName() {
		return currentUserName;
	}

	@Override
	public String getCurrentUserGroup() {
		return currentUserGroup;
	}

	@Override
	public String getCurrentUserPa6Group() {
		return currentPa6Group;
	}

	@Override
	public boolean isCordinator() {
		return isCordinator;
	}

	@Override
	public HashMap<String, ArrayList<String>> getWorkingChildRowKeys() {
		return null;
	}

}
