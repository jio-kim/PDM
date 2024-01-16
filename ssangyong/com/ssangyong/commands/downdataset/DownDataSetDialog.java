package com.ssangyong.commands.downdataset;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import com.ssangyong.common.dialog.SYMCAWTAbstractDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Utilities;

public class DownDataSetDialog extends SYMCAWTAbstractDialog {

	private static final long serialVersionUID = 1L;
	private AIFComponentContext[] targets;
	private DownDataSetInfoPanel infoPanel;
	
	private Registry registry = Registry.getRegistry(this);

	/**
	 * 생성자.
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 * @param frame
	 */
	public DownDataSetDialog(Frame frame, AIFComponentContext[] targets) {
		super(frame);
		
		this.targets = targets;
		
		initUI();
	}

	/**
	 * UI Panel 생성.
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since  : 2013. 1. 8.
	 */
	private void initUI() {
		setTitle(registry.getString("DownDataSetDialog.TITLE"));
		createDialogUI(registry.getString("DownDataSetDialog.MESSAGE_TITLE"), registry.getImageIcon("DownDataSetDialogHeader.ICON"));
		
		infoPanel = new DownDataSetInfoPanel(this, targets);
		
		add("unbound.bind", infoPanel);
	}


	@Override
	protected JPanel getUIPanel() {
		return infoPanel;
	}

	@Override
	public void invokeOperation(ActionEvent event) {
		try {
			// 이전 경로를 확인 하여 null이 아니면 이전에 열었던 경로를 열음.
			String strCookieDir = Utilities.getCookie("filechooser", "Chooser.DIR", true);
			if (strCookieDir == null) {
				strCookieDir = "";
			}
			JFileChooser fileChooser = new JFileChooser(strCookieDir);
			FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
			fileChooser.removeChoosableFileFilter(fileFilter);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			
			int ii = fileChooser.showDialog(this, "저장");
			System.out.println("ii : "+ii);
			if (ii == JFileChooser.APPROVE_OPTION) {
				// 새로 선택된 파일의 경로를 쿠기에 재 지정.
				String chooserDir = fileChooser.getSelectedFile().getAbsolutePath() + "\\";
				Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);
				
				DownDataSetOperation operation = new DownDataSetOperation(this, chooserDir);
				session.queueOperation(operation);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean validCheck() {
		return infoPanel.validCheck();
	}

	public DownDataSetInfoPanel getInfoPanel() {
		return infoPanel;
	}
}
