package com.kgm.commands.ec.fncepl.operation;

import java.awt.Desktop;
import java.io.File;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.ArrayList;

import com.kgm.commands.ec.fncepl.model.FncEplCheckData;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;

public class DownLoadUploadFileOperation extends AbstractAIFOperation {
	private ArrayList<FncEplCheckData> dataList = null;
	private String directoryPath = null;
	private WaitProgressBar waitProgress;
	private TCSession tcSession = null;

	public DownLoadUploadFileOperation(ArrayList<FncEplCheckData> dataList, String directoryPath) {
		this.dataList = dataList;
		this.directoryPath = directoryPath;
		this.tcSession = CustomUtil.getTCSession();
	}

	@Override
	public void executeOperation() throws Exception {

		try {
			waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
			waitProgress.start();
			waitProgress.setStatus("File DownLoad...");
			executeDownLoad();
			waitProgress.setStatus("Complete");
			waitProgress.close();
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "File was downloaded.\n" + directoryPath, "Complete", MessageBox.INFORMATION);
		} catch (Exception ex) {

			if (waitProgress != null) {
				waitProgress.setStatus("＠ Error Message : ");
				waitProgress.setStatus(ex.toString());
				waitProgress.close("Error", false);
			}
			setAbortRequested(true);
			ex.printStackTrace();

			throw ex;
		}

	}

	/**
	 * DownLoad실행
	 */
	private void executeDownLoad() throws Exception {

		for (FncEplCheckData data : dataList) {
			String uploadFilePuid = data.getAttachFilePuid();
			String functionNo = data.getFunctionNo();
			// String isLatest = data.getIsLatestCheck();
			if (uploadFilePuid == null || uploadFilePuid.isEmpty())
				continue;
			TCComponent comp = tcSession.stringToComponent(uploadFilePuid);
			if (comp == null)
				continue;
			TCComponentDataset dataSetComp = (TCComponentDataset) comp;
			TCComponentTcFile[] tcFiles = dataSetComp.getTcFiles();
			if (tcFiles == null || tcFiles.length < 1)
				continue;
			File[] files = dataSetComp.getFiles(SYMTcUtil.getNamedRefType(dataSetComp, tcFiles[0]), null);

			if (files == null || files.length < 1)
				continue;

			String extension = files[0].getAbsolutePath().substring(files[0].getAbsolutePath().lastIndexOf(".") + 1);
			// String fileName = functionNo + ("V".equals(isLatest) ? "_Latest" : "");
			String fileName = functionNo;
			String filePath = directoryPath + "\\" + fileName + "." + extension;
			Path path = Paths.get(filePath);
			int counter = 1;
			while (Files.exists(path)) {
				filePath = directoryPath + "\\" + fileName + "(" + counter + ")." + extension;
				path = Paths.get(filePath);
				counter++;
			}

			File downLoadFile = new File(filePath);
			files[0].renameTo(downLoadFile);

			// 하나일 경우 Open함
			if (dataList.size() == 1)
				Desktop.getDesktop().open(downLoadFile);
		}

	}
}
