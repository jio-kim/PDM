package com.ssangyong.commands.faq;

import java.awt.Desktop;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.ResourceManager;
import org.eclipse.wb.swt.SWTResourceManager;

import swing2swt.layout.BorderLayout;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemRevisionType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.controls.SWTComboBox;

/**
 * [SR150421-027][20150811][ymjang] PLM system 개선사항 - Manual 조회 관리기능 추가
 * [NON-SR][ymjang] 항목 문구 변경 및 화면 Pack해서 표시되도록 변경함.
 */
public class FaqView {

    private Shell parentShell;
    private Shell shell; 
    
    private final int shellWidth = 600;
    private final int shellHeight = 650;

    public FaqView() {

    }

    public FaqView(Shell parentShell) {
        this.parentShell = parentShell;
    }

    /**
     * @method detailFaqView
     * @date 2014. 3. 10.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public void detailFaqView(TableItem tableItem) {
    	
    	shell = new Shell(parentShell, SWT.PRIMARY_MODAL | SWT.TITLE | SWT.RESIZE | SWT.MIN | SWT.MAX | SWT.CLOSE);
		shell.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, FaqConstant.FAQ_IMAGE_PATH));
        shell.setSize(shellWidth, shellHeight);
        shell.setText("Detail Guide Manual");
		shell.setLayout(new FillLayout(SWT.HORIZONTAL));
		shell.setFocus();

		centerToParent(parentShell, shell);

		final Composite composite = new Composite(shell, SWT.NONE);
		composite.setLayout(new BorderLayout(0, 0));

		final Composite contentsComposite = new Composite(composite, SWT.NONE);
		contentsComposite.setLayoutData(BorderLayout.NORTH);
		contentsComposite.setLayout(new GridLayout(1, false));

		// Cancel Button
		/*
		Button cancelButton = new Button(contentsComposite, SWT.NONE);
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
		Label typeLabel = new Label(contentsComposite, SWT.NONE);
		typeLabel.setText("사용자");

		GridData gd_typeComboBox = new GridData(SWT.NONE, SWT.CENTER, true, true, 1, 1);
		gd_typeComboBox.widthHint = 100;

		Text typeText = new Text(contentsComposite, SWT.BORDER | SWT.READ_ONLY);
		typeText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		typeText.setLayoutData(gd_typeComboBox);
		typeText.setText(getFaqTypeName(tableItem.getData("faq_type").toString()));

/*        SWTComboBox typeComboBox = new SWTComboBox(contentsComposite, SWT.BORDER | SWT.READ_ONLY);
		typeComboBox.getTextField().setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		typeComboBox.setLayoutData(gd_typeComboBox);
	    typeComboBox.addItem("설계");
	    typeComboBox.addItem("기관");
	    typeComboBox.setSelectedItem(tableItem.getData("faq_type").toString().equals("1") ?  "설계"  : "기관");
*/	    
	    // 제목
		Label titleLabel = new Label(contentsComposite, SWT.NONE);
		titleLabel.setText("제목");

		GridData gd_titleText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_titleText.widthHint = 200;

		Text titleText = new Text(contentsComposite, SWT.BORDER | SWT.READ_ONLY);
		titleText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		titleText.setLayoutData(gd_titleText);
        titleText.setText(tableItem.getData("title").toString());
        
		// 내용
        String contents = tableItem.getData("contents").toString();

        Label contentsLabel = new Label(contentsComposite, SWT.NONE);
        contentsLabel.setText("내용");

		GridData gd_contentsText = new GridData(SWT.FILL, SWT.TOP, true, true, 1, 1);
		gd_contentsText.widthHint = 200;
		gd_contentsText.heightHint = 200;
		
        Browser contentsBrowser = new Browser(contentsComposite, SWT.NONE);
        contentsBrowser.setLayoutData(gd_contentsText);
        contentsBrowser.setText(contents);

        StyledText contentsStyledText = new StyledText(contentsComposite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
        contentsStyledText.setLayoutData(gd_contentsText);
        contentsStyledText.setText(contents);
        contentsStyledText.setEditable(false);

        if (CustomUtil.isHTML(contents)) {
            contentsStyledText.dispose();
        } else {
            contentsBrowser.dispose();
        }
        
        final ScrolledComposite aScrolledComposite = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        final Composite fileComposite = new Composite(aScrolledComposite, SWT.NONE);
		fileComposite.setLayoutData(BorderLayout.NORTH);
		fileComposite.setLayout(new GridLayout(3, false));

		GridData gd_noCLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_noCLabel.widthHint = 50;
		
		CLabel noCLabelOfHC = new CLabel(fileComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		noCLabelOfHC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		noCLabelOfHC.setLayoutData(gd_noCLabel);
		noCLabelOfHC.setText("No");
		
		GridData gd_filenameCLabel = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_filenameCLabel.widthHint = 250;

		CLabel filenameCLabelOfHC = new CLabel(fileComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		filenameCLabelOfHC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		filenameCLabelOfHC.setLayoutData(gd_filenameCLabel);
		filenameCLabelOfHC.setText("파일명");

		GridData gd_fileCLabel = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_fileCLabel.widthHint = 50;
		
		CLabel fileCLabelOfHC = new CLabel(fileComposite, SWT.SHADOW_IN | SWT.SHADOW_OUT | SWT.CENTER);
		fileCLabelOfHC.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		fileCLabelOfHC.setLayoutData(gd_fileCLabel);
		fileCLabelOfHC.setText("파일");
		
		// Addfile List
		ArrayList<HashMap<String, Object>> addfileList = (ArrayList<HashMap<String, Object>>) tableItem.getData("addfileList");
		HashMap<String, Object> dataMap = null;
		for (int i = 0; i < addfileList.size(); i++) {
			
			HashMap<String, Object> addfileMap = (HashMap<String, Object>) addfileList.get(i);
			
			if (addfileMap == null)
			   continue;
			  
			dataMap = new HashMap<String, Object>();
			dataMap.put("no", addfileMap.get("ADDFILE_SEQ"));
			dataMap.put("doc_id", addfileMap.get("DOC_ID"));
			dataMap.put("doc_rev_id", addfileMap.get("DOC_REV_ID"));
			dataMap.put("dataset_puid", addfileMap.get("DATASET_PUID"));
			dataMap.put("file_name", addfileMap.get("FILE_NM"));
			dataMap.put("file_location", addfileMap.get("FILE_LOCATION"));
			dataMap.put("file_type", addfileMap.get("FILE_TYPE"));
			
			final String itemId = addfileMap.get("DOC_ID").toString();
			final String itemRevId = addfileMap.get("DOC_REV_ID").toString();
			
			GridData gd_noText = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_noText.widthHint = 50;
			
			Text noText = new Text(fileComposite, SWT.BORDER | SWT.READ_ONLY | SWT.CENTER);
			noText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			noText.setLayoutData(gd_noText);
			noText.setText(String.valueOf(i+1));

			GridData gd_filenameText = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
			gd_filenameText.widthHint = 250;

			final Text filenameText = new Text(fileComposite, SWT.BORDER | SWT.READ_ONLY);
			filenameText.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
			filenameText.setLayoutData(gd_filenameText);
			filenameText.setText(addfileMap.get("FILE_NM").toString());

			GridData gd_attachButton = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
			gd_attachButton.widthHint = 50;

			Button attachedFileDownButton = new Button(fileComposite, SWT.CENTER);
			attachedFileDownButton.setLayoutData(gd_attachButton);
			attachedFileDownButton.setImage(ResourceManager.getPluginImage(FaqConstant.SYMBOLICNAME, "icons/save_16.png"));
			attachedFileDownButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if (filenameText.getText().isEmpty()) {
						MessageDialog.openWarning(shell, "Warning", "등록된 파일이 없습니다.");
					} else {
						fileOpenOrSave(itemId, itemRevId, "IMAN_specification");
					}
				}
			});
			
		}
		
		aScrolledComposite.setContent(fileComposite);
        aScrolledComposite.setExpandVertical(true);
        aScrolledComposite.setExpandHorizontal(true);
        aScrolledComposite.addControlListener(new ControlAdapter() {
            public void controlResized(ControlEvent e) {
              Rectangle r = aScrolledComposite.getClientArea();
              aScrolledComposite.setMinSize(fileComposite.computeSize(r.width, SWT.DEFAULT));
            }
          });
        
        openShell(shell);
    }

	@SuppressWarnings("unchecked")
	public void fileOpenOrSave(String itemId, String revId, String relation) {
		try {
			
		    // 1. 첨부파일 문서 아이템 리비전 찾기
			TCComponentItemRevision itemRevision = null;
			TCSession session = (TCSession) AIFUtility.getDefaultSession();
			TCComponentItemRevisionType itemRevisionType = (TCComponentItemRevisionType) session.getTypeComponent("ItemRevision");
			TCComponentItemRevision[] itemRevisions = itemRevisionType.findRevisions(itemId, revId);
			if (itemRevisions != null && itemRevisions.length > 0) {
				itemRevision = itemRevisions[0];
			}

		    // 2. 첨부파일 문서 아이템 리비전 밑의 데이터 셋 찾기
			TCComponentDataset dataset = null;
			TCComponent component = itemRevision.getRelatedComponent(relation);
			dataset = (TCComponentDataset) component;
			
		    // 3. 첨부파일 문서 아이템 리비전 밑의 데이터 셋의 파일 가져오기
			TCComponentTcFile tcFile = null;
			TCComponent[] components = dataset.getNamedReferences();
			if (components == null || components.length <= 0)
			{
				throw new Exception("File is not Found!");
			}

			tcFile = (TCComponentTcFile) components[0];
			String fileName = tcFile.getProperty("original_file_name");
			
			HashMap<String, Object> dataMap = new HashMap<String, Object>();
			dataMap.put("fileName", fileName);

			OpenOrSaveDialog dialog = new OpenOrSaveDialog(shell, SWT.NONE, dataMap);
			HashMap<String, Object> resultDataMap = (HashMap<String, Object>) dialog.open();
			if (resultDataMap != null) {
				if (resultDataMap.get("action").equals("open")) {
					File file = tcFile.getFmsFile();
					file.setWritable(true);
					Desktop.getDesktop().open(file);
				} else {
					String exportPath = (String) resultDataMap.get("exportPath");
					File file = new File(exportPath);
					if (file.exists()) {
						if (file.delete()) {
							file = new File(exportPath);
						}
					}

					if (tcFile.getFmsFile().renameTo(file)) {
						file.setWritable(true);
					}
				}

				if (resultDataMap.get("action").equals("save")) {
					MessageDialog.openInformation(shell, "Information", "다운로드가 완료되었습니다.");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();

			MessageDialog.openError(shell, "Error", e.getMessage() == null ? e.toString() : e.getMessage());
		}
	}
    
    public void centerToParent(Shell parentShell, Shell childShell) {
        Rectangle parentRectangle = parentShell.getBounds();
        Rectangle childRectangle = childShell.getBounds();
        int x = parentShell.getLocation().x + (parentRectangle.width - childRectangle.width) / 2;
        int y = parentShell.getLocation().y + (parentRectangle.height - childRectangle.height) / 2;
        childShell.setLocation(x, y);
    }
    
    public void openShell(Shell shell) {
    	shell.pack();
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
