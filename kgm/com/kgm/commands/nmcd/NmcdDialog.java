package com.kgm.commands.nmcd;


import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import com.kgm.common.lov.SYMCLOVLoader;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.VerticalLayout;

public class NmcdDialog extends AbstractAIFDialog {

	private static final long serialVersionUID = -4408474910899088180L;

	private RegisterPanel registerPanel;
	private SearchPanel searchPanel;
	private String[] nmcdValue;
	private String[] projectCodes;
	private static final String COMMONPARTCHECK_QUERY_SERVICE = "com.kgm.service.CommonPartCheckService";
	private ArrayList<String> teamList = new ArrayList<String>();

	/**
	 * 
	 * @copyright : Plmsoft
	 * @author : Á¶¼®ÈÆ
	 * @since  : 2018. 8. 28.
	 * @throws Exception
	 */
	public NmcdDialog() throws Exception {
		super(AIFUtility.getActiveDesktop().getFrame(), false);
		nmcdValue = SYMCLOVLoader.getLOV("s7_NMCD").getListOfValues().getStringListOfValues();
		projectCodes = SYMCLOVLoader.getLOV("S7_PROJECT_CODE").getListOfValues().getStringListOfValues();
		
		SYMCRemoteUtil remoteQuery = new SYMCRemoteUtil();
		DataSet ds = new DataSet();
		ds.put("ID", "");
		List<HashMap<String, Object>> resultList = (ArrayList<HashMap<String, Object>>) remoteQuery.execute(COMMONPARTCHECK_QUERY_SERVICE, "getVnetTeamNameK", ds);
		for (HashMap<String, Object> map : resultList) {
			teamList.add((String) map.get("TEAM"));
		}
		
		initUI();
	}


	private void initUI() throws Exception{
		setTitle("NMCD Management");
		
		getContentPane().setLayout(new VerticalLayout(5,5,5,5,5));
		getContentPane().add("unbound.bind.center.center", createTabPanel());
		getContentPane().add("bottom.bind.center.center", createButtonPanel());
		getContentPane().add("bottom.bind.center.center", new Separator());
		
		setPreferredSize(new Dimension(1700,800));
	}

	private JTabbedPane createTabPanel() throws Exception{
		JTabbedPane tabbedPane = new JTabbedPane();

		tabbedPane.addTab("Register", registerPanel());
		tabbedPane.addTab("Excel Import", migPanel());
		tabbedPane.addTab("Validation", ValidationPanel());
		
		return tabbedPane;
	}

	private JPanel registerPanel() throws Exception{
		RegisterPanel panel = new RegisterPanel(this);

		return panel;
	}
	
	private JPanel ValidationPanel() throws Exception{
		ValidationPanel panel = new ValidationPanel(this);
		
		return panel;
	}

	private JPanel migPanel() throws Exception{
		SearchPanel panel = new SearchPanel(this);
		
		return panel;
	}


	private JPanel createButtonPanel(){
		JPanel buttonPane = new JPanel();
		buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
		JButton cancelButton = new JButton("Close");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent actionevent) {
				dispose();
			}
		});
		buttonPane.add(cancelButton);
		
		return buttonPane;
	}
	
	public String[] getNmcdValues(){
		return nmcdValue;
	}
	
	public String[] getProjectCodeValues(){
		return projectCodes;
	}
	
	public ArrayList getTeamList(){
		return teamList;
	}
}