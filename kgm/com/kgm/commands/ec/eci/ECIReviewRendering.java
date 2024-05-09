package com.kgm.commands.ec.eci;

import java.io.File;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.kgm.common.utils.FTPConnection;
import com.kgm.dto.VnetTeamReviewData;
import com.kgm.viewer.AbstractSYMCViewer;

/**
 * Vision-NET에서 인터페이스 받은 검토 부서들의 검토 내역 화면을 생성함.
 * @author DJKIM
 *
 */
public class ECIReviewRendering {
    
	private Group group;
	private VnetTeamReviewData reviewData;

	public ECIReviewRendering(Composite parent, VnetTeamReviewData reviewData) {
		this.reviewData = reviewData;
    	group = new Group (parent, SWT.NONE);
		group.setLayout (new GridLayout(4, false));
		group.setText (" ▒▒▒ " + reviewData.getTteam() + " - Approval ");
		GridData layoutData = new GridData (SWT.FILL, SWT.FILL, true, true);
		group.setLayoutData(layoutData);
		createComposite();
    }
    
    public Composite createComposite(){
		Label lblInvest = new Label(group, SWT.RIGHT);
		lblInvest.setText("Review investment");
		lblInvest.setLayoutData(new GridData (140, SWT.DEFAULT));

		Text txtInvest = new Text(group, SWT.BORDER | SWT.RIGHT);
		txtInvest.setText(reviewData.getRev_inve()+"");
		txtInvest.setLayoutData(new GridData (300, SWT.DEFAULT));
		
		Label lblCost = new Label(group, SWT.RIGHT);
		lblCost.setText("Review Changed Cost");
		lblCost.setLayoutData(new GridData (140, SWT.DEFAULT));
		
		Text txtCost = new Text(group, SWT.BORDER | SWT.RIGHT);
		txtCost.setText(reviewData.getRev_cost()+"");
		txtCost.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label lblReview = new Label(group, SWT.RIGHT);
		lblReview.setText("Review Comments");
		lblReview.setLayoutData(new GridData (140, SWT.DEFAULT));
		
		Text txtReview = new Text(group, SWT.BORDER | SWT.RIGHT);
		txtReview.setText(reviewData.getDescp1()+"");
		txtReview.setLayoutData(new GridData (300, SWT.DEFAULT));
		
		Label lblProdInfo = new Label(group, SWT.RIGHT);
		lblProdInfo.setText("Production Info");
		lblProdInfo.setLayoutData(new GridData (140, SWT.DEFAULT));
		
		Text txtProdInfo = new Text(group, SWT.BORDER | SWT.RIGHT);
		txtProdInfo.setText(reviewData.getDescp2()+"");
		txtProdInfo.setLayoutData(new GridData (SWT.FILL, SWT.FILL, true, false));
		
		Label lblAttachFile = new Label(group, SWT.RIGHT);
		lblAttachFile.setText("Attatched File");
		lblAttachFile.setLayoutData(new GridData (140, SWT.DEFAULT));
		
		Text txtAttachFile = new Text(group, SWT.BORDER | SWT.RIGHT);
		txtAttachFile.setText(reviewData.getAppend()+"");
		GridData layoutData = new GridData (SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 3;
		txtAttachFile.setLayoutData(layoutData);
		txtAttachFile.setData(AbstractSYMCViewer.SKIP_ENABLE, "true");
		txtAttachFile.setToolTipText("Double click to download file.");
		txtAttachFile.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				downLoadFile();
			}
		});
		
		Label lblReceiveWorkflow = new Label(group, SWT.RIGHT);
		lblReceiveWorkflow.setText("Received Approval");
		lblReceiveWorkflow.setLayoutData(new GridData (140, SWT.DEFAULT));
		
		Text txtReceiveWorkflow = new Text(group, SWT.BORDER | SWT.LEFT);
		txtReceiveWorkflow.setText(reviewData.getR_approval()+"");
		layoutData = new GridData (SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 3;
		txtReceiveWorkflow.setLayoutData(layoutData);
		
		Label lblFinalWorkflow = new Label(group, SWT.RIGHT);
		lblFinalWorkflow.setText("Final Approval");
		lblFinalWorkflow.setLayoutData(new GridData (140, SWT.DEFAULT));
		
		Text txtFinalWorkflow = new Text(group, SWT.BORDER | SWT.LEFT);
		txtFinalWorkflow.setText(reviewData.getF_approval()+"");
		layoutData = new GridData (SWT.FILL, SWT.FILL, true, false);
		layoutData.horizontalSpan = 3;
		txtFinalWorkflow.setLayoutData(layoutData);
		
		return group;
    }
    
    /**
     * 검토 내역 관련 파일을 FTP를 통해 다운로드 받음.
     */
    public void downLoadFile(){
    	FileDialog saveDialog = new FileDialog(group.getShell(), SWT.SAVE);

    	String filePath = "/Cubic/pis/"+reviewData.getCreate_date().substring(0, 4);
    	String fileName = reviewData.getVnet_registered_id()+reviewData.getAppend();

    	saveDialog.setFileName(reviewData.getAppend());
		
		if(saveDialog.open() != null){
			String name = saveDialog.getFileName();
			if(name.equals("")) return;

			File downFile = new File(saveDialog.getFilterPath(), name);
			if(downFile.exists()) {
				org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(group.getShell(), SWT.ICON_WARNING | SWT.YES | SWT.NO);
				box.setText("Already Exist");
				box.setMessage(downFile.getName() + " is already exist!\nDo you overwrite the file?");
				if(box.open() != SWT.YES) {
					return;
				}
			}

			try{
				FTPConnection ftp = new FTPConnection();
				ftp.download(downFile, filePath, fileName);

				ftp.disconnect();
				
				org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(group.getShell(), SWT.YES | SWT.NO);
				box.setText("Open");
				box.setMessage("Do you want to open the file?");
				if(box.open() == SWT.YES) {
					openSavedFile(downFile);
				}
				
			}catch(Exception e){
				e.printStackTrace();
			}
		}
    }
    
    /**
     * 파일을 해당 어플리케이션으로 열기
     * @param file
     */
	private void openSavedFile(File file){
		final File downFile = file;
		Thread thread = new Thread(new Runnable(){
			public void run(){
				try{
					String[] commandString = {"CMD", "/C", downFile.getPath()};
					com.teamcenter.rac.util.Shell ishell = new com.teamcenter.rac.util.Shell(commandString);
					ishell.run();
				} catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		thread.start();
	}
}