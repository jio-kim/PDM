package com.kgm.commands.dmu;

import java.io.File;
import java.text.SimpleDateFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFUIApplication;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOMLine;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.pse.services.PSEApplicationService;
import com.teamcenter.rac.util.AbstractCustomPanel;
import com.teamcenter.rac.util.IPageComplete;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;


/**
 * @author Hoony
 *
 */
public class DMUReportInfoPanel extends AbstractCustomPanel implements IPageComplete {
	
	private Composite composite;
	private Registry registry;
	private TCSession session;	
	
	private Label resultLbl;			//ReportResult
	private Label imageLbl;		//ReportImage
	private Label nameLbl;			//ReportName
	private Text resultTxt;			//ReportResult �ؽ�Ʈ �Է� ����
	private Text imageTxt;			//ReportImage �ؽ�Ʈ �Է� ����
	private Text nameTxt;			//ReportName �ؽ�Ʈ �Է� ����
	private Button resultBttn;		//ReportResult ��ư
	private Button imageBttn;		//ReportImage ��ư
	private Button nameBttn;		//ReportName ��ư
	
	/* DMU Report Root ���丮 ��� */
	public static final String DMU_REPORT_ROOT = "D:\\0_DMU_Report";

	public String resultfilterPath;		//FilterPath ���� ����
	public String imageFilterPath;		//ReportImage ���� ������� ����
	public String nameFilterPath;		//ReportName ���� ������� ����
	public boolean imageFilterFlag;
	public boolean nameFilterFlag;
	
	public DMUReportInfoPanel(ScrolledComposite parentScrolledComposite) {
		super(parentScrolledComposite);
		this.session = CustomUtil.getTCSession();
	}
	
	public DMUReportInfoPanel(ScrolledComposite parentScrolledComposite, boolean isCreate) {
		super(parentScrolledComposite);
		this.session = CustomUtil.getTCSession();
	}


	/** (non-Javadoc)
	 * @see com.teamcenter.rac.util.AbstractCustomPanel#createPanel()
	 * 
	 * ȭ�� ���� : ���� ���� ���� ���� ���� �ǹǷ� �������� ȣ�� ����
	 */
	@Override
	public void createPanel() {
		resultfilterPath = DMU_REPORT_ROOT;
		imageFilterPath = DMU_REPORT_ROOT;
		nameFilterPath = DMU_REPORT_ROOT;
		
		imageFilterFlag = true;
		nameFilterFlag = true;

		makeDirectory(resultfilterPath);
		
		registry = Registry.getRegistry(this);
		
		composite = new Composite(parent, SWT.NONE);
		FormLayout formLayout = new FormLayout();
		composite.setLayout(formLayout);
		
		/*
		 * Report result component ����
		*/
		resultLbl = new Label(composite, SWT.NONE);
		resultLbl.setText(registry.getString("DMUReport.LABEL.resultLbl"));
		FormData formData = new FormData();
		formData.left = new FormAttachment(0, 5);
		formData.top = new FormAttachment(0, 10);
		resultLbl.setLayoutData(formData);
		
		resultBttn = new Button(composite, SWT.PUSH);
		resultBttn.setText(registry.getString("DMUReport.Button.search"));		
		formData = new FormData();
		formData.right = new FormAttachment(100, -5);
		formData.top = new FormAttachment(resultLbl, 5);
		resultBttn.setLayoutData(formData);
		resultBttn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				FileDialog dialog = new FileDialog(parent.getShell());
				dialog.setFilterPath(resultfilterPath);
				dialog.setFilterExtensions(new String[] {"*.txt"});
				String selectedFile = dialog.open();
				if (selectedFile != null) {
					resultTxt.setText(selectedFile);
					resultfilterPath=dialog.getFilterPath();
				}
			}
		});
		
		resultTxt = new Text(composite, SWT.SINGLE | SWT.BORDER);
		formData = new FormData(200, 18);
		formData.left = new FormAttachment(resultLbl, 0, SWT.LEFT);
		formData.right = new FormAttachment(resultBttn, -5);
		formData.top = new FormAttachment(resultBttn, 0, SWT.CENTER);
//		formData.right = new FormAttachment(10, 0);
		resultTxt.setLayoutData(formData);
//		resultTxt.setText(resultfilterPath);
		resultTxt.setEditable(false);

		/*
		 * Report Image component ����
		*/
		imageLbl = new Label(composite, SWT.NONE);
		imageLbl.setText(registry.getString("DMUReport.LABEL.imageLbl"));
		formData = new FormData();
		formData.left = new FormAttachment(resultLbl, 0, SWT.LEFT);
		formData.top = new FormAttachment(resultTxt, 20);
		imageLbl.setLayoutData(formData);		
		
		imageBttn = new Button(composite, SWT.PUSH);
		imageBttn.setText(registry.getString("DMUReport.Button.search"));
		formData = new FormData();
		formData.right = new FormAttachment(100, -5);
		formData.top = new FormAttachment(imageLbl, 5);
		imageBttn.setLayoutData(formData);
		imageBttn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(imageFilterFlag) {
					imageFilterFlag = false;
					imageFilterPath = findLastModifiedDirectory(imageFilterPath);				
				}
				
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setFilterPath(imageFilterPath);
				dialog.setMessage("������ �����ϼ���.");
				String selectedDirectory = dialog.open();
				if(selectedDirectory!=null) {					
					imageTxt.setText(selectedDirectory);
					imageFilterPath=selectedDirectory;
				}
			}
		});
		
		imageTxt = new Text(composite, SWT.SINGLE | SWT.BORDER);
		formData = new FormData(200, 18);
		formData.left = new FormAttachment(resultLbl, 0, SWT.LEFT);
		formData.right = new FormAttachment(imageBttn, -5);
		formData.top = new FormAttachment(imageBttn, 0, SWT.CENTER);
		imageTxt.setLayoutData(formData);
		imageTxt.setText(findLastModifiedDirectory(imageFilterPath));
		imageTxt.setEditable(false);
		
		/*
		 * Report Name component ����
		*/
		nameLbl = new Label(composite, SWT.NONE);
		nameLbl.setText(registry.getString("DMUReport.LABEL.nameLbl"));
		formData = new FormData();
		formData.left = new FormAttachment(resultLbl, 0, SWT.LEFT);
		formData.top = new FormAttachment(imageTxt, 20);
		nameLbl.setLayoutData(formData);
		
		nameBttn = new Button(composite, SWT.PUSH);
		nameBttn.setText(registry.getString("DMUReport.Button.search"));
		formData = new FormData();
		formData.right = new FormAttachment(100, -5);
		formData.top = new FormAttachment(nameLbl, 5);
		nameBttn.setLayoutData(formData);		
		nameBttn.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if(nameFilterFlag) {
					nameFilterFlag = false;
					nameFilterPath = findLastModifiedDirectory(nameFilterPath);
				}
				DirectoryDialog dialog = new DirectoryDialog(parent.getShell());
				dialog.setFilterPath(nameFilterPath);
				dialog.setMessage("������ �����ϼ���.");
				String selectedDirectory =dialog.open();
				parent.getShell().setCursor(new Cursor(parent.getShell().getDisplay(), SWT.CURSOR_ARROW));
				if(selectedDirectory != null) {
					String exportFileName = createFileName(selectedDirectory);		
					if(exportFileName != null) {
						nameTxt.setText(exportFileName);								
						nameFilterPath=selectedDirectory;				
					}
				}
			}
		});
		
		nameTxt = new Text(composite, SWT.SINGLE | SWT.BORDER);
		formData = new FormData(200, 18);
		formData.left = new FormAttachment(resultLbl, 0, SWT.LEFT);
		formData.right = new FormAttachment(nameBttn, -5);
		formData.top = new FormAttachment(nameBttn, 0, SWT.CENTER);
		nameTxt.setLayoutData(formData);
		String exportFileName = createFileName(findLastModifiedDirectory(nameFilterPath));		
		if(exportFileName != null) {
			nameTxt.setText(exportFileName);			
		}
		nameTxt.setEditable(false);
	}


	/**
	 * ���� ����
	 * @param directoryPath 
	*/
	private void makeDirectory(String directoryPath) {		
		if(directoryPath != null) {
			File file = new File(directoryPath);		
			if(!file.isDirectory()) {
				file.mkdirs();
			}
		}
	}
	
	
	/**
	 * ���� ���� �������� ���� �ֱٿ� ������ ������ ã��
	 * @param directoryPath 
	*/
	private String findLastModifiedDirectory(String targetDrectory) {
		if(targetDrectory != null) {
			File file = new File(targetDrectory);		
			if(!file.isDirectory()) {
				//targetDrectory�� ���� ���� ���� ��� ������ ���� ����
				file.mkdirs();
				return targetDrectory;
			}
			
			String directoryName ="";
			long modifiedDate = 0;
			File subfiles[] = file.listFiles();
			for(File subfile : subfiles) {
				if(subfile.isDirectory()) {
					long lastModDate = subfile.lastModified();	
					if(lastModDate > modifiedDate) {
						modifiedDate = lastModDate;
						directoryName = subfile.getAbsolutePath();
					}
				}
			}
			
			if(!directoryName.equals("")) {				
				return directoryName;
			}
		}
		
		//���� ������ ���� ��� targetDrectory�� �״�� �����Ѵ�. 
		return targetDrectory;
	}
	
	
	/** (non-Javadoc)
	 * @see com.teamcenter.rac.util.AbstractCustomPanel#getComposite()
	 * 
	 * Composite ��ȯ
	 */	
	@Override
	public Composite getComposite() {
		return composite;
	}

	/** (non-Javadoc)
	 * @see com.teamcenter.rac.util.IPageComplete#isPageComplete()
	 */
	@Override
	public boolean isPageComplete() {
		return false;
	}

	public void create() {
		String resultPath = resultTxt.getText();
		String imagePath = imageTxt.getText();
		String namePath = checkText(nameTxt.getText());
		
		DMUReportOperation report = new DMUReportOperation(session, resultPath, imagePath, namePath);
		
		try {
			report.executeOperation();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private String checkText(String paramText) {	
		String newText = paramText.replaceAll("/", "_");
		return newText;
	}

	/**
	 * export �� excel ������ �̸� ����
	 * ��Ģ : topPartItem_��¥.xls
	 * @param directoryName 
	 */
	private String createFileName(String directoryName) {
		
		//topPartItem ��������
		String topPartName = null;
		AbstractAIFUIApplication aifUtility = AIFUtility.getCurrentApplication();
		
		if(aifUtility instanceof PSEApplicationService) {			
			PSEApplicationService service = (PSEApplicationService) aifUtility;
			TCComponentBOMLine topBomLine = service.getTopBOMLine();
			if(topBomLine != null) {
				try {
					topPartName = topBomLine.getItem().toString();
				} catch (TCException e) {
					e.printStackTrace();
				}
			} else {
				MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Top Part does not exist.!", "ERROR", MessageBox.ERROR);
				return null;
			}
		} 
		
		//������̸� ��������
//		String userName = session.getUserName();
		
		//��¥����
		SimpleDateFormat smplDataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
		String saveTime = smplDataFormat.format(new java.util.Date());	
		
//		String exportFileName = directoryName +"\\" + topPartName+ "_" + userName+ "_" + saveTime + ".xls";
		String exportFileName = directoryName +"\\" + topPartName+ "_" + saveTime + ".xls";
		
		return exportFileName;
	}

	//validation üũ
	public boolean checkTextComponent() {
		if(resultTxt.getText().length() == 0) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Please choose the Report Results.", "WARNING", MessageBox.WARNING);
			return false;
		}
		
		if(imageTxt.getText().length() == 0) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Please choose the Report Image.", "WARNING", MessageBox.WARNING);
			return false;
		}
		
		if(nameTxt.getText().length() == 0) {
			MessageBox.post(AIFUtility.getActiveDesktop().getShell(), "Please choose the Report Name.", "WARNING", MessageBox.WARNING);
			return false;
		}		
		return true;
	}
}
