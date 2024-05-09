package com.kgm.commands.faq;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

import com.kgm.common.remote.DataSet;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 * [NON-SR][ymjang] 항목 문구 변경 및 화면 Pack해서 표시되도록 변경함.
 */
public class FaqAdminView {

	private TCSession session;
	
	private FaqQueryService faqQueryService;
    private AddfileQueryService addfileQueryService;
    private FaqAdminViewPart faqAdminViewPart;
    
	private Shell parentShell;
	private Shell shell; 
	private Text titleText;
	private SWTComboBox typeComboBox; 
	private StyledText contentsStyledText;
	private Text[] noText = null; 
	private Text[] filenameText = null;
	private String[] fileNames = null;
	private String[] filelocations = null;
	private String[] fileDocId = null;
	
    private final int shellWidth = 600;
    private final int shellHeight = 650;
    private final int fileListMaxSize = 5;

	private final int limitTitleLength = 200;
	
	public FaqAdminView() {

	}

	public FaqAdminView(FaqQueryService faqQueryService, AddfileQueryService addfileQueryService, FaqAdminViewPart faqAdminViewPart, Shell parentShell) {
		this.faqQueryService = faqQueryService;
		this.addfileQueryService = addfileQueryService;
		this.faqAdminViewPart = faqAdminViewPart;
		this.parentShell = parentShell;
		
		this.session = (TCSession) AIFUtility.getDefaultSession();
		
	}

	/**
	 * 
	 * 
	 * @method createFaqView
	 * @date 2015. 08. 10.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	public void createFaqView() {
		
		shell = new Shell(parentShell, SWT.PRIMARY_MODAL | SWT.TITLE | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.CLOSE);
		shell.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.FAQ_IMAGE_PATH));
        shell.setSize(shellWidth, shellHeight);
        shell.setText("Create Guide Manual");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setFocus();
		
		centerToParent(parentShell, shell);

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new BorderLayout(0, 0));

		Composite northComposite = new Composite(composite, SWT.NONE);
		northComposite.setLayoutData(BorderLayout.NORTH);
		northComposite.setLayout(new GridLayout(1, false));

		// OK Button
		Button okButton = new Button(northComposite, SWT.NONE);
		GridData gd_okButton = new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1);
		gd_okButton.widthHint = 100;
		okButton.setLayoutData(gd_okButton);
		okButton.setText("Save");
		okButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.OK_IMAGE_PATH));
		okButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				String title = titleText.getText();
				String contents = contentsStyledText.getText();
				
				if (CustomUtil.isNullString(title)) {
					MessageDialog.openInformation(shell, "Information", "Please insert title.");
					titleText.setFocus();
					return;
				}
				if (CustomUtil.isNullString(contents)) {
					MessageDialog.openInformation(shell, "Information", "Please insert contents.");
					contentsStyledText.setFocus();
					return;
				}
				
				HashMap<String, Object> ouidMap = faqQueryService.selectNextOUID();
				if (ouidMap == null) {
					MessageDialog.openError(parentShell, "Error", "Failed to notice create operation." + "\n" + "Connection refused." + "\n" + "Please contact to administrator.");
					return;
				}
				
				// insert FAQ
				String faq_puid = ouidMap.get("FAQ_PUID").toString();
				int faq_seq = Integer.parseInt(ouidMap.get("FAQ_SEQ").toString());
				
				DataSet dataSet = new DataSet();
				dataSet.put("faq_puid", faq_puid);
				dataSet.put("faq_seq", faq_seq);
				dataSet.put("faq_type", getFaqTypeCode(typeComboBox.getSelectedItem().toString()));
				dataSet.put("title", title);
				dataSet.put("contents", contents);
				dataSet.put("create_user", session.getUserName());
				dataSet.put("modify_user", session.getUserName());
				
				faqQueryService.insertFaq(dataSet);

				// insert ADDFILE
				HashMap<String, Object> dataMap = null;
				for (int i = 0; i < noText.length; i++) {
					
					if (filenameText[i].getText().length() == 0)
						continue;
					
					dataSet.put("addfile_puid", faq_puid);
					int addfile_seq = addfileQueryService.selectMaxSeq(dataSet);
					String doc_id = faq_puid + addfile_seq; 
					String filePathName = filelocations[i];
					String fileName = getFileName(filePathName);
					String fileNameWithExt = getFileNameWithExt(filePathName);
					String extension = getExtension(filePathName);
					
					dataSet = new DataSet();
					dataSet.put("addfile_puid", faq_puid);
					dataSet.put("addfile_seq", addfile_seq);
					dataSet.put("doc_id", doc_id);
					dataSet.put("doc_rev_id", "A");
					dataSet.put("dataset_puid", "");
					dataSet.put("file_nm", fileNameWithExt);
					dataSet.put("file_location", fileNameWithExt);
					dataSet.put("file_type", extension);
					
					dataMap = new HashMap<String, Object>();
					dataMap.put("doc_id", doc_id);
					dataMap.put("doc_rev_id", "A");
					dataMap.put("doc_type", "Document");
					dataMap.put("doc_name", fileName);
					dataMap.put("file_type", extension);
					dataMap.put("file_location", filePathName);
					
					try {
						CreateDocOperation createOperation = new CreateDocOperation(dataMap);
						createOperation.executeOperation();
					} catch (Exception e1) {
						e1.printStackTrace();
						MessageDialog.openError(shell, "Error", e1.getMessage() == null ? e1.toString() : e1.getMessage());
						return;
					}

					addfileQueryService.insertAddfile(dataSet);
					
				}
				
				MessageDialog.openInformation(shell, "Information", "Guide Manual is Saved.");
				
				faqAdminViewPart.refreshTable();
				
				shell.dispose();
			}
		});

		// Cancel Button
		/*
		Button cancelButton = new Button(northComposite, SWT.NONE);
		GridData gd_cancelButton = new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1);
		gd_cancelButton.widthHint = 100;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.CANCEL_IMAGE_PATH));
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		*/
		
		// Type
		Label typeLabel = new Label(northComposite, SWT.NONE);
		typeLabel.setText("사용자");

		GridData gd_typeComboBox = new GridData(SWT.NONE, SWT.CENTER, true, true, 1, 1);
		gd_typeComboBox.widthHint = 100;

		typeComboBox = new SWTComboBox(northComposite, SWT.BORDER);
		typeComboBox.getTextField().setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		typeComboBox.setLayoutData(gd_typeComboBox);
	    typeComboBox.addItem("설계"); // - 1
	    typeComboBox.addItem("기관"); // - 2
	    typeComboBox.addItem("DCS");  // - 3
	    typeComboBox.addItem("전체"); // - A
	    typeComboBox.setSelectedItem("설계");

	    // title
		Label titleLabel = new Label(northComposite, SWT.NONE);
		titleLabel.setText("제목");

		GridData gd_titleText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_titleText.widthHint = 100;

		titleText = new Text(northComposite, SWT.BORDER);
		titleText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		titleText.setLayoutData(gd_titleText);
        titleText.setText("");
        
		// contents
        Label contentsLabel = new Label(northComposite, SWT.NONE);
        contentsLabel.setText("내용 (최대 2000 자)");

		GridData gd_contentsText = new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1);
		gd_contentsText.widthHint = 100;
		gd_contentsText.heightHint = 200;

		contentsStyledText = new StyledText(northComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		contentsStyledText.setLayoutData(gd_contentsText);
        
		// Addfile List
        Label addfileLabel = new Label(northComposite, SWT.NONE);
        addfileLabel.setText("첨부파일 (최대 5 개)");

        final ScrolledComposite aScrolledComposite = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        final Composite centerComposite = new Composite(aScrolledComposite, SWT.NONE);
        centerComposite.setLayoutData(BorderLayout.SOUTH);
        centerComposite.setLayout(new GridLayout(3, false));

		GridData gd_noCLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_noCLabel.widthHint = 50;
		
		CLabel noCLabel = new CLabel(centerComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		noCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		noCLabel.setLayoutData(gd_noCLabel);
		noCLabel.setText("No");
		
		GridData gd_filenameCLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_filenameCLabel.widthHint = 250;

		CLabel filenameCLabel = new CLabel(centerComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		filenameCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		filenameCLabel.setLayoutData(gd_filenameCLabel);
		filenameCLabel.setText("파일명");

		GridData gd_fileCLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_fileCLabel.widthHint = 50;
		
		CLabel fileCLabelOfHC = new CLabel(centerComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		fileCLabelOfHC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		fileCLabelOfHC.setLayoutData(gd_fileCLabel);
		fileCLabelOfHC.setText("파일");
		
		noText = new Text[fileListMaxSize];
		filenameText = new Text[fileListMaxSize];
		filelocations = new String[fileListMaxSize];
		
		// Addfile List
		for (int i = 0; i < fileListMaxSize; i++) {
			
			GridData gd_noText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_noText.widthHint = 50;
			
			noText[i] = new Text(centerComposite, SWT.BORDER | SWT.CENTER | SWT.READ_ONLY);
			noText[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			noText[i].setLayoutData(gd_noText);
			noText[i].setText(String.valueOf(i+1));

			GridData gd_filenameText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_filenameText.widthHint = 250;

			filenameText[i] = new Text(centerComposite, SWT.BORDER);
			filenameText[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			filenameText[i].setLayoutData(gd_filenameText);
			filenameText[i].setText("");

			GridData gd_attachButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_attachButton.widthHint = 50;

			final int no = i;
			Button attachedFileDownButton = new Button(centerComposite, SWT.CENTER);
			attachedFileDownButton.setLayoutData(gd_attachButton);
			attachedFileDownButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/open_16.png"));
			attachedFileDownButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					FileDialog dialog = new FileDialog(shell);
					String filePath = dialog.open();
					if (filePath != null) {
						String fileName = getFileNameWithExt(filePath);
						filenameText[no].setText(fileName);
						filelocations[no] = filePath;
					}
				}
			});
			
		}

		aScrolledComposite.setContent(centerComposite);
        aScrolledComposite.setExpandVertical(true);
        aScrolledComposite.setExpandHorizontal(true);
        aScrolledComposite.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
              Rectangle r = aScrolledComposite.getClientArea();
              aScrolledComposite.setMinSize(centerComposite.computeSize(r.width, SWT.DEFAULT));
            }
          });
		
		openShell(shell);
	}

	/**
	 * 
	 * 
	 * @method updateNoticeView
	 * @date 2014. 3. 10.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	public void updateFaqView(TableItem tableItem) {
		
		final String faq_puid = tableItem.getData("faq_puid").toString();
		final ArrayList<HashMap<String, Object>> addfileList = (ArrayList<HashMap<String, Object>>) tableItem.getData("addfileList");
		
		shell = new Shell(parentShell, SWT.PRIMARY_MODAL | SWT.TITLE | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.CLOSE);
		shell.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.FAQ_IMAGE_PATH));
        shell.setSize(shellWidth, shellHeight);
        shell.setText("Modify Guide Manual");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setFocus();
		
		centerToParent(parentShell, shell);

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new BorderLayout(0, 0));

		Composite northComposite = new Composite(composite, SWT.NONE);
		northComposite.setLayoutData(BorderLayout.NORTH);
		northComposite.setLayout(new GridLayout(1, false));

		// OK Button
		Button okButton = new Button(northComposite, SWT.NONE);
		GridData gd_okButton = new GridData(SWT.RIGHT, SWT.FILL, true, false, 1, 1);
		gd_okButton.widthHint = 100;
		okButton.setLayoutData(gd_okButton);
		okButton.setText("Save");
		okButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.OK_IMAGE_PATH));
		okButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				String title = titleText.getText();
				String contents = contentsStyledText.getText();

				if (CustomUtil.isNullString(title)) {
					MessageDialog.openInformation(shell, "Information", "Please insert title.");
					titleText.setFocus();
					return;
				}
				if (CustomUtil.isNullString(contents)) {
					MessageDialog.openInformation(shell, "Information", "Please insert contents.");
					contentsStyledText.setFocus();
					return;
				}
				
				try {
					
					// Update FAQ
					DataSet dataSet = new DataSet();
					dataSet.put("faq_puid", faq_puid);
					dataSet.put("faq_type", getFaqTypeCode(typeComboBox.getSelectedItem().toString()));
					dataSet.put("title", title);
					dataSet.put("contents", contents);
					dataSet.put("modify_user", session.getUserName());

					faqQueryService.updateFaq(dataSet);

					// Delete/Insert ADDFILE
					HashMap<String, Object> dataMap = null;
					for (int i = 0; i < noText.length; i++) {
						
						if (filenameText[i].getText().length() == 0 && fileDocId[i].length() == 0)
							continue;
						
                    	String doc_id = fileDocId[i];

                    	// Delete
						if (filenameText[i].getText().length() == 0 && fileDocId[i].length() > 0)
						{
							TCComponentItem deleteItem = CustomUtil.findItem("Document", doc_id);

		                    dataMap = new HashMap<String, Object>();
							dataMap.put("deleteItem", deleteItem);
							
							DeleteDocOperation deleteOperation = new DeleteDocOperation(dataMap);
							deleteOperation.executeOperation();
							
							dataSet = new DataSet();
							dataSet.put("doc_id", doc_id);
							
							addfileQueryService.deleteAddfile(dataSet);
						}
						// Insert
						else if (filenameText[i].getText().length() > 0 && fileDocId[i].length() == 0)
						{
							dataSet.put("addfile_puid", faq_puid);
							int addfile_seq = addfileQueryService.selectMaxSeq(dataSet);
							doc_id = faq_puid + addfile_seq; 
							String filePathName = filelocations[i];
							String fileName = getFileName(filePathName);
							String fileNameWithExt = getFileNameWithExt(filePathName);
							String extension = getExtension(filePathName);
							
							dataMap = new HashMap<String, Object>();
							dataMap.put("doc_id", doc_id);
							dataMap.put("doc_rev_id", "A");
							dataMap.put("doc_type", "Document");
							dataMap.put("doc_name", fileName);
							dataMap.put("file_type", extension);
							dataMap.put("file_location", filePathName);
							
							CreateDocOperation createOperation = new CreateDocOperation(dataMap);
							createOperation.executeOperation();

							dataSet = new DataSet();
							dataSet.put("addfile_puid", faq_puid);
							dataSet.put("addfile_seq", addfile_seq);
							dataSet.put("doc_id", doc_id);
							dataSet.put("doc_rev_id", "A");
							dataSet.put("dataset_puid", "");
							dataSet.put("file_nm", fileName);
							dataSet.put("file_location", fileNameWithExt);
							dataSet.put("file_type", extension);

							addfileQueryService.insertAddfile(dataSet);
						}
						// Update
						else if (!filenameText[i].getText().equals(fileNames[i]))
						{
							
							// 1. Delete Item
							TCComponentItem deleteItem = CustomUtil.findItem("Document", doc_id);
							if (deleteItem != null)
							{
			                    dataMap = new HashMap<String, Object>();
								dataMap.put("deleteItem", deleteItem);
								
								DeleteDocOperation deleteOperation = new DeleteDocOperation(dataMap);
								deleteOperation.executeOperation();
							}
							
							// 2. Create Item
							dataSet.put("addfile_puid", faq_puid);
							dataSet = new DataSet();
							String filePathName = filelocations[i];
							String fileName = getFileName(filePathName);
							String fileNameWithExt = getFileNameWithExt(filePathName);
							String extension = getExtension(filePathName);
							
							dataMap = new HashMap<String, Object>();
							dataMap.put("doc_id", doc_id);
							dataMap.put("doc_rev_id", "A");
							dataMap.put("doc_type", "Document");
							dataMap.put("doc_name", fileName);
							dataMap.put("file_type", extension);
							dataMap.put("file_location", filePathName);
							
							CreateDocOperation createOperation = new CreateDocOperation(dataMap);
							createOperation.executeOperation();

							// 3. Update Addfile
							dataSet = new DataSet();
							dataSet.put("doc_id", doc_id);
							dataSet.put("doc_rev_id", "A");
							dataSet.put("dataset_puid", "");
							dataSet.put("file_nm", fileName);
							dataSet.put("file_location", fileNameWithExt);
							dataSet.put("file_type", extension);
							
							addfileQueryService.updAddfile(dataSet);
							
						}
						
					}

				} catch (Exception e1) {
					e1.printStackTrace();
					MessageDialog.openError(shell, "Error", e1.getMessage() == null ? e1.toString() : e1.getMessage());
					return;
				}
				
				MessageDialog.openInformation(shell, "Information", "Guide Manual is Saved.");
				
				faqAdminViewPart.refreshTable();
				
				shell.dispose();
			}
		});

		// Cancel Button
		/*
		Button cancelButton = new Button(northComposite, SWT.NONE);
		GridData gd_cancelButton = new GridData(SWT.RIGHT, SWT.FILL, false, false, 1, 1);
		gd_cancelButton.widthHint = 100;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.CANCEL_IMAGE_PATH));
		cancelButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shell.dispose();
			}
		});
		*/
		
		// 사용자
		Label typeLabel = new Label(northComposite, SWT.NONE);
		typeLabel.setText("사용자");

		GridData gd_typeComboBox = new GridData(SWT.NONE, SWT.CENTER, true, true, 1, 1);
		gd_typeComboBox.widthHint = 100;

		typeComboBox = new SWTComboBox(northComposite, SWT.BORDER);
		typeComboBox.getTextField().setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		typeComboBox.setLayoutData(gd_typeComboBox);
	    typeComboBox.addItem("설계");
	    typeComboBox.addItem("기관");
	    typeComboBox.addItem("DCS");
	    typeComboBox.addItem("전체");
	    typeComboBox.setSelectedItem(getFaqTypeName(tableItem.getData("faq_type").toString()));

	    // 제목
		Label titleLabel = new Label(northComposite, SWT.NONE);
		titleLabel.setText("제목");

		GridData gd_titleText = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_titleText.widthHint = 100;

		titleText = new Text(northComposite, SWT.BORDER);
		titleText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		titleText.setLayoutData(gd_titleText);
        titleText.setText(tableItem.getData("title").toString());
        
		// 내용
        Label contentsLabel = new Label(northComposite, SWT.NONE);
        contentsLabel.setText("내용 (최대 2000 자)");

		GridData gd_contentsText = new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1);
		gd_contentsText.widthHint = 100;
		gd_contentsText.heightHint = 200;

		contentsStyledText = new StyledText(northComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		contentsStyledText.setLayoutData(gd_contentsText);
		contentsStyledText.setText(tableItem.getData("contents").toString());
		
		// 첨부파일목록
        Label addfileLabel = new Label(northComposite, SWT.NONE);
        addfileLabel.setText("첨부파일 (최대 5 개)");

        final ScrolledComposite aScrolledComposite = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        final Composite centerComposite = new Composite(aScrolledComposite, SWT.NONE);
        centerComposite.setLayoutData(BorderLayout.SOUTH);
        centerComposite.setLayout(new GridLayout(3, false));

		GridData gd_noCLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_noCLabel.widthHint = 50;
		
		CLabel noCLabel = new CLabel(centerComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		noCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		noCLabel.setLayoutData(gd_noCLabel);
		noCLabel.setText("No");
		
		GridData gd_filenameCLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_filenameCLabel.widthHint = 250;

		CLabel filenameCLabel = new CLabel(centerComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		filenameCLabel.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		filenameCLabel.setLayoutData(gd_filenameCLabel);
		filenameCLabel.setText("파일명");

		GridData gd_fileCLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_fileCLabel.widthHint = 50;
		
		CLabel fileCLabelOfHC = new CLabel(centerComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		fileCLabelOfHC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		fileCLabelOfHC.setLayoutData(gd_fileCLabel);
		fileCLabelOfHC.setText("파일");
		
		noText = new Text[fileListMaxSize];
		filenameText = new Text[fileListMaxSize];
		fileNames = new String[fileListMaxSize];
		filelocations = new String[fileListMaxSize];
		fileDocId = new String[fileListMaxSize];
		
		// Addfile List
		for (int i = 0; i < fileListMaxSize; i++) {
			
			GridData gd_noText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_noText.widthHint = 50;
			
			noText[i] = new Text(centerComposite, SWT.BORDER | SWT.CENTER | SWT.READ_ONLY);
			noText[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			noText[i].setLayoutData(gd_noText);
			noText[i].setText(String.valueOf(i+1));

			GridData gd_filenameText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_filenameText.widthHint = 250;

			filenameText[i] = new Text(centerComposite, SWT.BORDER);
			filenameText[i].setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			filenameText[i].setLayoutData(gd_filenameText);
			if (addfileList != null && addfileList.size() > i)
			{
				filenameText[i].setText(addfileList.get(i).get("FILE_NM").toString());
				fileDocId[i] = addfileList.get(i).get("DOC_ID").toString(); 
				fileNames[i] = addfileList.get(i).get("FILE_NM").toString();
			}
			else
			{
				filenameText[i].setText("");
				fileDocId[i] = "";
				fileNames[i] = "";
			}

			filelocations[i] = ""; 
					
			GridData gd_attachButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_attachButton.widthHint = 50;

			final int no = i;
			Button attachedFileDownButton = new Button(centerComposite, SWT.CENTER);
			attachedFileDownButton.setLayoutData(gd_attachButton);
			attachedFileDownButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/open_16.png"));
			attachedFileDownButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					FileDialog dialog = new FileDialog(shell);
					String filePath = dialog.open();
					if (filePath != null) {
						String fileName = getFileNameWithExt(filePath);
						filenameText[no].setText(fileName);
						filelocations[no] = filePath;
					}
				}
			});
			
		}

		aScrolledComposite.setContent(centerComposite);
        aScrolledComposite.setExpandVertical(true);
        aScrolledComposite.setExpandHorizontal(true);
        aScrolledComposite.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
              Rectangle r = aScrolledComposite.getClientArea();
              aScrolledComposite.setMinSize(centerComposite.computeSize(r.width, SWT.DEFAULT));
            }
          });
		
		openShell(shell);
	}

    /**
     * 경로명을 포함한 파일명 중에서 확장자를 제외한 순수한 파일명만 구한다.
     * @param filefullpath
     * @return
     */
    public String getFileName(String filefullpath)
    {
        
        int idx = filefullpath.lastIndexOf(File.separator);
        int idx_exe = filefullpath.lastIndexOf('.');
        String str = "";
        if (idx_exe > -1)
        {
            str = filefullpath.substring((idx + 1), idx_exe);
        }
        else
        {
            str = filefullpath;
        }
        // System.out.println(str);
        return str;
    }
	
    /**
     * 경로명을 포함한 파일명 중에서 파일명만 구한다.
     * @param filefullpath
     * @return
     */
    public String getFileNameWithExt(String filefullpath)
    {
        
        int idx = filefullpath.lastIndexOf(File.separator);
        String str = "";
        if (idx > -1)
        {
            str = filefullpath.substring((idx + 1));
        }
        else
        {
            str = filefullpath;
        }
        // System.out.println(str);
        return str;
    }
    
    /**
     * 경로명을 포함한 파일명 중에서 확장자만 구한다.
     * @param filename
     * @return
     */
    public String getExtension(String filename)
    {
        String str = null;
        if (filename.lastIndexOf(".") != -1)
        {
            int pos = filename.lastIndexOf(".");
            str = filename.substring(pos + 1).toLowerCase();
        }
        return str;
    }
    
    public void centerToParent(Shell parentShell, Shell childShell) {
        Rectangle parentRectangle = parentShell.getBounds();
        Rectangle childRectangle = childShell.getBounds();
        int x = parentShell.getLocation().x + (parentRectangle.width - childRectangle.width) / 2;
        int y = parentShell.getLocation().y + (parentRectangle.height - childRectangle.height) / 2;
        childShell.setLocation(x, y);
    }
    
    public void openShell(Shell shell) {
        shell.open();
        Display display = shell.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
    
    public String getFaqTypeCode(String typeName) {
    	
    	String typeCode = null;
    	
    	if (typeName == null || typeName.equals("")) {
    		return null;
    	}
    	
    	if (typeName.equals("설계")) {
    		typeCode = "1";
    	} else if (typeName.equals("기관")) {
    		typeCode = "2";
    	} else if (typeName.equals("DCS")) {
    		typeCode = "3";
    	} else if (typeName.equals("전체")) {
    		typeCode = "A";
    	} 
    	
    	return typeCode;
    }
    
    public String getFaqTypeName(String typeCode) {
    	
    	String typeName = null;
    	
    	if (typeCode == null || typeCode.equals("")) {
    		return null;
    	}
    	
    	if (typeCode.equals("1")) {
    		typeName = "설계";
    	} else if (typeCode.equals("2")) {
    		typeName = "기관";
    	} else if (typeCode.equals("3")) {
    		typeName = "DCS";
    	} else if (typeCode.equals("A")) {
    		typeName = "전체";
    	} 
    	
    	return typeName;
    }
    
}
