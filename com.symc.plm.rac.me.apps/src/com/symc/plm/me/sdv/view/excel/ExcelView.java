package com.symc.plm.me.sdv.view.excel;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.ole.win32.OLE;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.OleControlSite;
import org.eclipse.swt.ole.win32.OleEvent;
import org.eclipse.swt.ole.win32.OleFrame;
import org.eclipse.swt.ole.win32.OleListener;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.sdv.core.common.IButtonInfo;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.UIManager;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;
import org.sdv.core.ui.view.AbstractSDVViewPane;

import swing2swt.layout.BorderLayout;

import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.common.SDVTypeConstant;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentReleaseStatus;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

/**
 * [SR번호][201411141530] shcho, Revise한 공법의 작업표준서 ExcelView에서 sheet 편집이 안되는 오류 수정
 * [SR번호][20141215] shcho, 용접조건표에서도 작업표준서와 마찬가지로 LatestReleased DatasetUID를 가져오는 조건 추가
 * [SR150317-021][20150323] ymjang, 국문 작업표준서 Republish 방지토록 개선
 * [SR150312-024][20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
 * [NON-SR][20150410] shcho, langConfigId 로 비교하여 버튼 활성/비활성 하던 것을 DialogID에서 직접 가져와서 영문국문 식별 후  버튼 활성/비활성 하도록 수정.
 * [SR150714-008][20150713] shcho, Excel Open이 되지 않는 오류 수정
 * [NON-SR][20150714] shcho, Unlock 버튼 활성화 (ExcelView 활성시)
 * 
 */
public abstract class ExcelView extends AbstractSDVViewPane {

    public final static String IID_AppEvents = "{00024413-0000-0000-C000-000000000046}";

    public final static int WindowDeactivate = 0x00000615;
    public final static int WorkbookBeforeSave = 0x00000623;
//    public final static int SelectionChange = 0x00000616;
//    public final static int SheetActivate = 0x00000619;

    protected OleControlSite clientSite;
    protected OleControlSite appControlSite;

    protected Composite oleComposite;
    protected OleFrame frame;
    protected File currentFile;
    protected String filePath;
    protected String oldFilePath;
    

    protected int openMode = 1;

    protected TCComponentDataset targetDataset;

    protected int langConfigId;

    protected boolean isWritable = true;

    public Button btnOpen;

    /** unprotectWorkbook 재시도 횟수  **/
    protected int unprotectTryCount = 0;
    
    // [NON-SR][20160205] taeku.jeong Excel File을 연 Dataset이 포함된 Item Revision 정보를 확인하기위해 추가
	private TCComponentItemRevision itemRevision;
	private String datasetRelationName;
	
	private DatasetCloseWaiter datasetCloseWaiter; 

    public ExcelView(Composite parent, int style, String id) {
        super(parent, style, id);
    }

    public ExcelView(Composite parent, int style, String id, int configId) {
        super(parent, style, id, configId);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public TCComponentDataset getTargetDataset() {
        return targetDataset;
    }

    @Override
    protected void initUI(Composite parent){
        parent.setLayout(new BorderLayout());

        this.oleComposite = new Composite(parent, SWT.NONE);
        this.oleComposite.setLayoutData(BorderLayout.CENTER);
        this.oleComposite.setLayout(new FillLayout());

        this.frame = new OleFrame(this.oleComposite, SWT.NONE);
        this.frame.setLayout(new FillLayout());

        createOpenButton();

        if(getConfigId() == 1) {
            this.targetDataset = getDataset();
            if(targetDataset != null) {
                setFile();
            }
        }
    }

    public void setPreviewWritable(boolean isPreviewWritable)
    {
    	isWritable = isPreviewWritable;
    }

    public void setFile() {
        try {
            TCComponentTcFile[] files = targetDataset.getTcFiles();
            if(files != null && files.length > 0) {
                File file = files[0].getFile(null);
                if(file != null) {
                    this.filePath = file.getAbsolutePath();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openExcel() {
    	
    	System.out.println("View Case1");
    	
        if(closeExcelView()) {
            if(openExcelApplication()) {
            	// Excel Open 버튼을 눌러 Excel이 열려 있는동안 모든 Button을 비 활성화 한다.
                HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
                for(String key : actionToolButtons.keySet()) {
                    ((Button) actionToolButtons.get(key)).setEnabled(false);
                }
            }
        }
    }

    /**
     * OEL Open된 Excel  View를 닫는다.
     * @return
     */
    public boolean closeExcelView() {
        if(closeWorkbook()) {
            if(btnOpen == null) {
                createOpenButton();
            }

            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();

            for(String key : actionToolButtons.keySet()) {
                Button button = actionToolButtons.get(key).getButton();
                if(key.equals("OpenExcel")) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }

            return true;
        }

        return false;
    }

    public void openExcelView() {
        if(openWorkbook()) {
            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
            for(String key : actionToolButtons.keySet()) {
                Button button = actionToolButtons.get(key).getButton();
                //[NON-SR][20150714] shcho, Unlock 버튼 활성화 (ExcelView 활성시)
                if("Unlock".equals(key)) {
                    button.setEnabled(true);
                    continue;
                }
                
                if(!isWritable) {
                    if("ShowHideRibbon".equals(key) || "Close".equals(key)) {
                        button.setEnabled(true);
                    }
                } else {
                    //영문
                	//[NON-SR][20150410] shcho, langConfigId 로 비교하여 버튼 활성/비활성 하던 것을 DialogID에서 직접 가져와서 영문국문 식별 후  버튼 활성/비활성 하도록 수정.
                	String id = UIManager.getCurrentDialog().getId();
                    if(id.endsWith("EN")) {
                        if("ShowHideRibbon".equals(key) || "Save".equals(key) || "Close".equals(key)) {
                            button.setEnabled(true);
                        }
                        //국문
                    } else {
                        //Released인 경우 저장 및 편집 비활성
                        if(isReleasedOP()) {
                            if("ShowHideRibbon".equals(key) || "Close".equals(key)) {
                                button.setEnabled(true);
                            } else {
                                button.setEnabled(false);
                            }                            
                        } else {
                            button.setEnabled(true);
                        }
                    }
                }
            }
        }
    }

    public boolean closeWorkbook() {
        if(clientSite != null) {
            OleAutomation workbook = new OleAutomation(clientSite);
            unprotectWorkbook(workbook);

            workbook.dispose();

            this.clientSite.dispose();
            this.clientSite = null;
        }

        return true;
    }
    
    public boolean openExcelApplication() {
    	
    	System.out.println("ExcelView.openExcelApplication()");
    	
        boolean retVal = false;
        
        LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
        for(String key : buttons.keySet()) {
            buttons.get(key).getButton().setEnabled(false);
        }

        if(this.btnOpen != null) {
            this.btnOpen.dispose();
            this.btnOpen = null;
        }
        
        if(appControlSite != null) {
        	
        	if (appControlSite.isDirty()){ 
            	uploladFile();
        	}
        	
            appControlSite.dispose();
            appControlSite = null;
        }
        
		DatasetOpenForEditManager aDatasetOpenForEditManager = new DatasetOpenForEditManager(targetDataset, this.frame, isWritable);
		// 편집가능한 상태인지 약식으로 확인 한다. (영문작업표준서 Preview 인경우 Message 보이지 않도록 처리 한다.)
		if(aDatasetOpenForEditManager.isCanOpenEditAble()==false && getConfigId() != 1){
			MessageBox.post(this.frame.getShell(), "Excel파일은 읽기 전용으로 열립니다.", "Warning", MessageBox.WARNING);
		}
		
		// 영문 작업 표준서의 경우 화면의 Open-Ko 버튼이 활성화 되도록 해주어야한다.......
		// 이게 해결되어야 영문조립작업표준서 작업이 가능함.
		
		datasetCloseWaiter = new DatasetCloseWaiter(aDatasetOpenForEditManager, this);
		
		// Thread 시작후 끝날때 까지 기다림.
		try {
			aDatasetOpenForEditManager.start();
			Thread.sleep(10);
			datasetCloseWaiter.start();
			// Thread가 완료 될때 까지 기다린다.
			//aDatasetOpenForEditManager.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

        return false;
    }
    
    public void clossExcelApplicatoin(){
    	// Close 행위를 하면된다.
    	
		try {
			// 변경된 파일을 view 화면에 표시하기위해 중요한 부분임.
			this.currentFile = null;
			this.targetDataset.refresh();
			setFile();
		} catch (TCException e) {
			e.printStackTrace();
		}

		// [NON-SR][20161004] taeku.jeong UI 동기화로 인해 ""Problem Occurred" Dialog 생성되는 경우가 있음.
		try {
			Display.getDefault().syncExec(new Runnable() {
				public void run() {
					// Excel을 여는 버튼이 활성화 되도록 한다.
					LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
					for(String key : buttons.keySet()) {
						if("OpenExcel".equals(key)) {
							buttons.get(key).getButton().setEnabled(true);
						}
					}
					
					// kbc 수정 : btnOpen 이 생성 되었을때는 다시 만들지 않음
			        // OLE Open화면에 보이는 "작업그림/관리점 이미지를 여시려면 여기를 클릭해주세요" 버튼이 보임
					if(btnOpen == null) {
		                createOpenButton();
		            }
				}
	    	});
			
		} catch (Exception e) {
				e.printStackTrace();
		}

    }
    
    public boolean openExcelApplication_OLD() {
    	
        boolean retVal = false;
        
        LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
        for(String key : buttons.keySet()) {
            buttons.get(key).getButton().setEnabled(false);
        }

        if(this.btnOpen != null) {
            this.btnOpen.dispose();
            this.btnOpen = null;
        }

        if(appControlSite == null) {
            appControlSite = new OleControlSite(this.frame, SWT.NONE, "Excel.Application");
        }
        appControlSite.doVerb(OLE.OLEIVERB_OPEN);
        
        OleListener beforeSaveListener = new OleListener() {

            @Override
            public void handleEvent(OleEvent event) {
            	System.out.println("Ev1");
                isProcessSheetFile();
            }
        };

        OleListener windowDeactivateListener = new OleListener() {

            @Override
            public void handleEvent(OleEvent event) {
            	
            	System.out.println("Ev2 : ");
            	
                if(isProcessSheetFile()) {
                	
                	// [P0087] [20150130] ymjang TC Save 버튼을 누르지 않고, 열려 있는 Excel프로그램에서 Save시에도 바로 TC로 Save 될 수 있도록 기능 개선 요청
                	// 파일 변경시 변경된 파일 업로드 
                	if (appControlSite.isDirty()) 
                    	uploladFile();
                	
                    if(appControlSite != null) {
                        appControlSite.dispose();
                        appControlSite = null;
                    }

                    // Excel을 여는 버튼이 활성화 되도록 한다.
                    LinkedHashMap<String, IButtonInfo> buttons = getActionToolButtons();
                    for(String key : buttons.keySet()) {
                        if("OpenExcel".equals(key)) {
                            buttons.get(key).getButton().setEnabled(true);
                        }
                    }
                    
                    // OLE Open화면에 보이는 "작업그림/관리점 이미지를 여시려면 여기를 클릭해주세요" 버튼이 보임
                    createOpenButton();
                }
            }
        };

        OleAutomation application = new OleAutomation(appControlSite);
        appControlSite.addEventListener(application, IID_AppEvents, WorkbookBeforeSave, beforeSaveListener);
        appControlSite.addEventListener(application, IID_AppEvents, WindowDeactivate, windowDeactivateListener);
        
        application.setProperty(application.getIDsOfNames(new String[] {"Visible"})[0], new Variant(true));

        OleAutomation workbooks = application.getProperty(application.getIDsOfNames(new String[] {"Workbooks"})[0]).getAutomation();
        Variant varResult = workbooks.invoke(workbooks.getIDsOfNames(new String[] {"Open"})[0], new Variant[] {new Variant(filePath)});
        
        if(varResult != null) {
            System.out.println(" copy invoke result of BSHEET = " + varResult);
            varResult.dispose();
            retVal = true;
            OleAutomation workbook = getAutoProperty(application, "ActiveWorkbook");
            protectWorkbook(workbook);
            workbook.dispose();
        } else {
            System.out.println("=====failed invoke copySheet method ====");
            retVal = false;
        }
        
        workbooks.dispose();
        application.dispose();
        
        return retVal;
    }  // Open Excel Application

    private boolean isProcessSheetFile() {
        boolean retVal = false;
        OleAutomation application = new OleAutomation(appControlSite);
        OleAutomation workbook = getAutoProperty(application, "ActiveWorkbook");
        String name = workbook.getProperty(workbook.getIDsOfNames(new String[] {"Name"})[0]).getString();

        System.out.println("filePath = "+filePath);
        System.out.println("name = "+name);
        
        if(filePath.endsWith(name)) {
            retVal = true;

            if(appControlSite != null) {
                unprotectWorkbook(workbook);
            }
        }

        workbook.dispose();
        application.dispose();

        return retVal;
    }

    public void openInViewer() {
        if(openWorkbook()) {
            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();
            openMode = 0;

            for(String key : actionToolButtons.keySet()) {
                if(key.equals("OpenExcel")) {
                    actionToolButtons.get(key).getButton().setEnabled(false);
                } else {
                    if(key.equals("Close")) {
                        actionToolButtons.get(key).getButton().setToolTipText("Close");
                    }
                    actionToolButtons.get(key).getButton().setEnabled(true);
                }
            }
        }
    }

    public boolean openWorkbook() {
        if(this.btnOpen != null) {
            this.btnOpen.dispose();
            this.btnOpen = null;
        }

        try {
            if(this.currentFile == null || (currentFile!=null && currentFile.exists()==false)) {
            	
            	// [bc.kim] filePath가 null인경우 에러표시 추가
            	if( filePath != null ) {
            		currentFile = new File(filePath);
            		clientSite = null;
            	} else {
            		MessageBox.post( this.frame.getShell() , "해당 공법의 작업표준서 파일이 없습니다.", "Save", MessageBox.INFORMATION);
            		return false;
            	}
            }

            if(clientSite == null) {
                clientSite = new OleControlSite(frame, SWT.NONE, "Excel.Sheet", currentFile);
            }

            clientSite.doVerb(OLE.OLEIVERB_INPLACEACTIVATE);
            clientSite.setFocus();

            OleAutomation workbook = new OleAutomation(clientSite);

            // Workbook에 Dataset의 UID로 통합문서보호 지정
            protectWorkbook(workbook);

            OleAutomation application = getAutoProperty(workbook, "Application");
            OleAutomation activeWindow = getAutoProperty(application, "ActiveWindow");
            activeWindow.setProperty(property(activeWindow, "Zoom"), new Variant(60));
            activeWindow.dispose();

            OleAutomation activeSheet = getAutoProperty(workbook, "ActiveSheet");
            if(hasProperty("activeSheet", activeSheet, "Range")) {
                OleAutomation range = getAutoProperty(activeSheet, "Range", new Variant[] { new Variant("A1") });
                if(hasProperty("Range", range, "Select")) {
                    invokeMethod("Range", range, "Select", null);
                }
                range.dispose();
            }

            showHideRibbonMenu(application, true);

            activeSheet.dispose();
            workbook.dispose();
            application.dispose();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return true;
    }

    abstract public void addSheet();

    abstract public TCComponentDataset getDataset();

    public void removeSheet() {
        OleAutomation workbook = new OleAutomation(clientSite);
        OleAutomation activeSheet = getAutoProperty(workbook, "ActiveSheet");
        String sheetName = getStringProperty(activeSheet, "Name");

        Variant varResult = invokeMethod(sheetName, activeSheet, "Delete", null);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println(" Delete invoke result of [" + sheetName + "] = " + varResult);
            boolean result = varResult.getBoolean();
            System.out.println("=====deleteSheet= result === " + result);
            varResult.dispose();
        } else {
            System.out.println("=====failed invoke deleteSheet method ====");
        }
        activeSheet.dispose();
        workbook.dispose();
    }

    public void showHideRibbon() {
        OleAutomation workbook = new OleAutomation(clientSite);
        String mainSheetName = getStringProperty(workbook, "Name");

        OleAutomation application = getAutoProperty(workbook, "Application");

        Variant varResult = null;
        Variant[] param = new Variant[1];

        boolean flag = true;
        param[0] = new Variant("Get.ToolBar(7," + Dquotation("Ribbon") +")");
        varResult = invokeMethod(mainSheetName, application, "ExecuteExcel4Macro", param);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            flag = varResult.getBoolean();
            showHideRibbonMenu(application, flag);
        } else {
            System.out.println("=====failed invoke copy method ====");
        }

        varResult.dispose();
        workbook.dispose();
        application.dispose();
    }

    protected void setEnable(Composite parent) {
        Control[] controls = parent.getChildren();
        if(controls != null && controls.length > 0) {
            for(Control control : controls) {
                if(control instanceof Composite) {
                    control.setEnabled(true);
                    setEnable((Composite) control);
                }
            }
        }
    }

    public void showHideRibbonMenu(OleAutomation application, boolean flag) {
        Variant varResult = null;
        Variant[] param = new Variant[1];

        if(flag) {
            param[0] = new Variant("SHOW.TOOLBAR(" + Dquotation("Ribbon") + ",False)");
            System.out.println("====flag " + flag + "=========");
        } else {
            param[0] = new Variant("SHOW.TOOLBAR(" + Dquotation("Ribbon") + ",True)");
            System.out.println("====flag " + flag + "=========");
        }

        varResult = invokeMethod("", application, "ExecuteExcel4Macro", param);

        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            varResult.dispose();
        } else {
            System.out.println("=====failed invoke copy method ====");
        }

        unlock();
    }

    public String Dquotation(String strData){
        return "\"" + strData + "\"";
    }

    /**
     * [SR번호][20141217] shcho, NamedReference List만 지우고 실제 imanFile은 (reference 되어있어) 삭제 못하는 경우가 있어서 관련 알림 메시지 내용 변경함.
     */
    public void saveWorkbook() {
        this.clientSite.save(this.currentFile, true);

        if(targetDataset != null) {
            try {
                Vector<File> files = new Vector<File>();
                files.add(currentFile);

                SYMTcUtil.removeAllNamedReference(targetDataset);
                SYMTcUtil.importFiles(targetDataset, files);
                MessageBox.post(this.frame.getShell(), "저장 되었습니다.", "Save", MessageBox.INFORMATION);
            } catch (Exception e) {
                e.printStackTrace();
                MessageBox.post(this.frame.getShell(), "저장을 하지 못했습니다.\n System 관리자에게 문의하세요.\n " + e.getMessage(), "Save", MessageBox.ERROR);
            }
        }

        if(this.clientSite != null) {
            this.clientSite.setFocus();
        }
    }

    protected Variant invokeMethod(String autoName, OleAutomation auto, String methodName, Variant[] varArgs, String[] rgVarDispNames) {
        Variant varResult = null;
        if (auto != null) {
            int[] methodDspIDs = auto.getIDsOfNames(new String[] { methodName });
            System.out.println("Invoke [" + methodName + "] Method of " + autoName);
            if (methodDspIDs != null) {
                System.out.println(autoName + " will invoked method [" + methodName + " | ID=" + methodDspIDs[0] + "] ");

                if (varArgs != null) {
                    int[] rgVarDispIDs = null;
                    if (rgVarDispNames != null) {
                        rgVarDispIDs = propertyIDs(auto, rgVarDispNames);
                        if (rgVarDispIDs != null) {
                            for (int id : rgVarDispIDs) {
                                System.out.println("variantArgName [" + id + "= " + auto.getName(id));
                            }
                        }
                    }

                    if (rgVarDispIDs != null) {
                    	// 변수이름은 제일먼저 메서드명으로 찾아서 메소드 아이디가 항상 먼저 나온다.
                        int[] rgVarDispIDs2 = new int[rgVarDispIDs.length - 1];
                        for (int id = 1; id < rgVarDispIDs.length; id++) {
                            rgVarDispIDs2[id - 1] = id;
                        }
                        varResult = auto.invoke(methodDspIDs[0], varArgs, rgVarDispIDs2);
                    } else {
                        varResult = auto.invoke(methodDspIDs[0], varArgs);
                    }
                } else {
                    varResult = auto.invoke(methodDspIDs[0]);
                }
            } else {
                System.out.println(autoName + " has not this method [" + methodName + "] ");
            }
        }
        return varResult;
    }

    protected Variant invokeMethod(String autoName, OleAutomation auto, String methodName, Variant[] varArgs) {
        return invokeMethod(autoName, auto, methodName, varArgs, null);
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, int dispId) {
        return getAutoProperty(auto, dispId, null);
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, String name) {
        return getAutoProperty(auto, name, null);
    }

    protected OleAutomation getAutoProperty(OleAutomation auto, String name, Variant[] values) {
        return getAutoProperty(auto, property(auto, name), values);
    }

    /**
     *
     * @method getAutoProperty
     * @date 2013. 9. 26.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    protected OleAutomation getAutoProperty(OleAutomation auto, int dispId, Variant[] values) {
        Variant varResult = null;
        varResult = (values != null) ? auto.getProperty(dispId, values) : auto.getProperty(dispId);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            OleAutomation result = varResult.getAutomation();
            varResult.dispose();
            return result;
        }
        return null;
    }

    protected int property(OleAutomation auto, String name) {
        return auto.getIDsOfNames(new String[] { name })[0];
    }

    protected int[] propertyIDs(OleAutomation auto, String[] names) {
        return auto.getIDsOfNames(names);
    }

    protected String getStringProperty(OleAutomation auto, String name) {
        return getStringProperty(auto, property(auto, name));
    }

    protected String getStringProperty(OleAutomation auto, int id) {
        String result = null;
        Variant varResult = auto.getProperty(id);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            result = varResult.getString();
            varResult.dispose();
        }
        return result;
    }

    protected long getLongProperty(OleAutomation auto, String name) {
        return getLongProperty(auto, property(auto, name));
    }

    protected long getLongProperty(OleAutomation auto, int id) {
        long result = 0;
        Variant varResult = auto.getProperty(id);
        if (varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            result = varResult.getLong();
            varResult.dispose();
        }
        return result;
    }

    /**
     *
     * @method hasProperty
     * @date 2013. 9. 26.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    protected boolean hasProperty(String parentName, OleAutomation auto, String propertyName) {
        return hasProperty(parentName, auto, propertyName, true);
    }

    /**
     *
     * @method hasProperty
     * @date 2013. 9. 29.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    protected boolean hasProperty(String parentName, OleAutomation auto, String propertyName, boolean showInfo) {
        boolean result = false;
        if (auto != null) {

            int[] ids = propertyIDs(auto, new String[] { propertyName });
            result = (ids != null && ids.length > 0);
            if(!showInfo) return result;
            if (result) {
                System.out.println(" [ " + parentName + " ] has property [" + propertyName + "] ID = " + ids[0]);
            } else {
                System.out.println(" [ " + parentName + " ] has not property [" + propertyName + "] ============ ");
            }
        }
        return result;
    }

    @Override
    public void setLocalDataMap(IDataMap dataMap) {

    }

    @Override
    public IDataMap getLocalDataMap() {
        return null;
    }

    @Override
    public IDataMap getLocalSelectDataMap() {
        return getLocalDataMap();
    }

    @Override
    public Composite getRootContext() {
        return null;
    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return null;
    }

    public void setTargetDataset(TCComponentDataset dataset) {
        this.targetDataset = dataset;
        setItemRevsion(dataset);
        
        try {
            TCComponentTcFile[] files = dataset.getTcFiles();
            if(files != null && files.length > 0) {
                File file = files[0].getFile(null);
                if(file != null) {
                    this.filePath = file.getAbsolutePath();
                    this.currentFile = new File(this.filePath);

                    // [P0087] [20150130] ymjang TC Save 버튼을 누르지 않고, 열려 있는 Excel프로그램에서 Save시에도 바로 TC로 Save 될 수 있도록 기능 개선 요청
                    // File 변경 여부를 체크하기 위하여 원본 파일을 tmp 폴더에 복사함.
                    /*
                    if (file != null)
                    {
                        this.oldFilePath = fileCopy(file);
                    }
                    */
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 조립작업표준서, 용접조건표 등의 Excel Sheet 시트보호 암호를 해제할때 암호를 Item의 PUID로 정할때
     * 필요한 구현임. 
     * @param dataset
     */
    private void setItemRevsion(TCComponentDataset dataset){
		
		String[] targetTypes = new String[]{};
		String[] relationNames = new String[]{
					SDVTypeConstant.PROCESS_SHEET_KO_RELATION,
					SDVTypeConstant.PROCESS_SHEET_EN_RELATION,
					SDVTypeConstant.WELD_CONDITION_SHEET_RELATION
				};

		TCComponentItemRevision itemRevision = null;
		String relationName = null;
		try {
			AIFComponentContext[] referenced = dataset.whereReferencedByTypeRelation( null, relationNames);

			for (int i = 0;referenced!=null && i < referenced.length; i++) {
				TCComponent component = (TCComponent)referenced[i].getComponent();
				if(component!=null && component instanceof TCComponentItemRevision){
					itemRevision = (TCComponentItemRevision)component;
					relationName = referenced[i].getContext().toString();
					// 만약에 복수개가 검색되는 경우에 대한 고려가 필요하면 이부분 이하에서 추가 구현 해야 한다.
					break;
				}
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
    	
		if(itemRevision!=null){
			this.itemRevision = itemRevision;
			this.datasetRelationName = relationName;
		}
    }

   
    /**
     * [P0087] [20150130] ymjang TC Save 버튼을 누르지 않고, 열려 있는 Excel프로그램에서 Save시에도 바로 TC로 Save 될 수 있도록 기능 개선 요청
     * File 변경 여부를 체크하기 위하여 원본 파일을 tmp 폴더에 복사함.
     * 미사용
     * @return
     */
	private String fileCopy(File inFile) {
		
		String outFilePath = null;
		
		try {
		   
			String inFilePath = inFile.getAbsolutePath();
			String inFileName = inFile.getName();
			
			int idx = inFilePath.lastIndexOf(File.separator);
			if (idx != -1) 
				outFilePath = inFilePath.substring(0, idx) + File.separator + "tmp";
			else
				outFilePath = inFilePath + File.separator + "tmp";
           
			File outFile = new File(outFilePath);
			if (!outFile.exists())
			{
				outFile.mkdir();
			}
			
			outFilePath = outFilePath + File.separator + inFileName;
			
			FileInputStream fis = new FileInputStream(inFilePath);
			FileOutputStream fos = new FileOutputStream(outFilePath);
	    
			int data = 0;
			while((data = fis.read())!=-1) {
				fos.write(data);
			}
			
			fis.close();
			fos.close();
	    
   		} catch (IOException e) {
    	    e.printStackTrace();
   		}
		
		return outFilePath;
	}
     	 
        
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {
        if(result == SDVInitEvent.INIT_SUCCESS){
            
            /* [SR150714-008][20150713] shcho, Excel Open이 되지 않는 오류 수정
             * Preview Open 완료 후 ExcelView의 initalizeLocalData에서 Excel Template 파일 설정을 하게 되는데, 
             * 사용자가 Preview Open 직후 (initalizeLocalData호출 전에) Excel Open 버튼을 클릭하게 되면 파일을 찾을 수 없어 오류가 발생
             * Excel Template 파일 설정을 Preview에서 하도록 변경
             * 
            if(getConfigId() == 0) {
                TCComponentDataset tcDataset = (TCComponentDataset)dataset.getData("exceltemplate");
                if(tcDataset != null){
                    setTargetDataset(tcDataset);
                }
            }
             */
            
        	/*
            * [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
            * [SR150714-008][20150713] shcho, Excel Open이 되지 않는 오류와 마찬가지로 ExcelView에 속하는 버튼 들도 초기화 진행전에 
            *                                      즉, 사용자가 Preview Open 직후 (initalizeLocalData호출 전에) Excel Open 버튼을 클릭하게 되면 없어 오류가 발생
            *                                      Excel Template 파일 설정과 마찬가지로 Preview에서 하도록 변경
             * enabledButton();
             */
            
  
            
             /* 기존 소스 주석 처리함.
            HashMap<String, IButtonInfo> actionToolButtons = getActionToolButtons();

            for(String key : actionToolButtons.keySet()) {
                Button button = actionToolButtons.get(key).getButton();
                if(key.equals("OpenExcel")) {
                    button.setEnabled(true);
                } else {
                    button.setEnabled(false);
                }
            }
            */
            
        }
    }

    @Override
    public void uiLoadCompleted() {

    }

    public boolean protectWorkbook(OleAutomation workbook) {
    	
    	System.out.println("Protection -----------");
    	System.out.println("Item Revision = "+getItemRevision());
    	boolean isItemRevisionMReleased = false;
    	String itemUid = null;
    	if(this.itemRevision!=null){
    		
    		try {
				itemUid = this.itemRevision.getItem().getUid();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
    		
    		System.out.println("Relation nam : "+this.datasetRelationName);
			try {
				String targetStatusName = "M Released";
				TCComponentReleaseStatus status = SYMTcUtil.getStatusObject(this.itemRevision, targetStatusName);
				if(status!=null){
					isItemRevisionMReleased = true;
					System.out.println("Status Name = "+targetStatusName);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
    	}
    	
    	boolean isEngWorkInsSheet = false;
    	if(this.datasetRelationName!=null){
    		if(this.datasetRelationName.trim().equalsIgnoreCase(SDVTypeConstant.PROCESS_SHEET_EN_RELATION)==true ){
    			isEngWorkInsSheet = true;
    		}
    	}
    	
		if(isItemRevisionMReleased == true && isEngWorkInsSheet==false){
			// Protection 진행 해도됩니다.
		}else{
			// Protection 진행 하면 안됩니다.
		}
    	
		System.out.println("Item Uid" + itemUid );

        //Variant varResult = invokeMethod("workbook", workbook, "Protect", new Variant[] { new Variant(targetDataset.getUid()), new Variant(true)});
        Variant varResult = invokeMethod("workbook", workbook, "Protect", new Variant[] { new Variant(itemUid), new Variant(true), new Variant(false)});
        boolean result = false;
        if(varResult!=null){
        	System.out.println("varResult.toString() = "+varResult.toString());
        }
        
        if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println("Protect invoke result = " + varResult);
            result = varResult.getBoolean();
            varResult.dispose();
        } else {
            System.out.println("=====failed invoke protectWorkbook method ====");
        }

        return result;
    }
    

    public boolean unprotectWorkbook(OleAutomation workbook) {
    	
    	String itemUid = null;
    	TCComponentItemRevision tempItemRevision = getItemRevision();
    	if(tempItemRevision!=null){
    		try {
				itemUid = tempItemRevision.getItem().getUid();
			} catch (TCException e) {
				e.printStackTrace();
			}
    	}
        return unprotectWorkbook(workbook, itemUid);
    }
    
    /**
     * [SR번호][201411141530] shcho, Revise한 공법의 작업표준서 ExcelView에서 sheet 편집이 안되는 오류 수정
     *      이미지엑셀파일 은 workbook이 특정 암호(Dataset UID)로 잠겨있다. 
     *      공법을 Revise 할 경우 이미지엑셀파일은 그대로 복사하게 되므로 암호가 이전 Revision 에 붙어있는것과 동일하다.
     *      그런데 unprotectWorkbook은 현 Revision의 Dataset UID를 가지고 체크하므로 잠금해제를 할 수가 없다. 
     *      이를 해결하고자 unprotectWorkbook을 실패 할 경우, 이전 Revision의 Dataset UID를 가지고 1회 더 재귀호출 하도록 하였다.
     *      
     * @param workbook
     * @param datasetUid
     * @return
     */
    public boolean unprotectWorkbook(OleAutomation workbook, String datasetUid) {
    	
    	System.out.println("Un Protection -----------");
    	System.out.println("Item Revision = "+getItemRevision());
    	boolean isItemRevisionMReleased = false;
    	String itemUid = null;
    	if(this.itemRevision!=null){
    		
    		try {
				itemUid = itemRevision.getItem().getUid();
			} catch (TCException e1) {
				e1.printStackTrace();
			}
    		
    		System.out.println("Relation nam : "+this.datasetRelationName);
			try {
				String targetStatusName = "M Released";
				TCComponentReleaseStatus status = SYMTcUtil.getStatusObject(this.itemRevision, targetStatusName);
				if(status!=null){
					isItemRevisionMReleased = true;
					System.out.println("Status Name = "+targetStatusName);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
    	}
    	
    	boolean isEngWorkInsSheet = false;
    	if(this.datasetRelationName!=null){
    		if(this.datasetRelationName.trim().equalsIgnoreCase(SDVTypeConstant.PROCESS_SHEET_EN_RELATION)==true ){
    			isEngWorkInsSheet = true;
    		}
    	}
    	
    	boolean isUnProtectAble = true;
		if(isItemRevisionMReleased == true){
			if(isEngWorkInsSheet==false){
				isUnProtectAble = false;
			}
		}
		
		System.out.println("Item uid");
        Variant varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(itemUid) });
        boolean result = false;
        if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println("Unprotect invoke result = " + varResult);
            result = varResult.getBoolean();
            varResult.dispose();
        } else {
        	// Protection 암호 해재를 몇번 시도 해보도록 처리 한다.
        	result = unProtectCaseByCase(varResult, workbook, getItemRevision());
        }
        return result;
    }
    
    /**
     * 반복해서 몇가지 Protection 암호를 입력해 본다.
     * @param varResult
     * @param workbook
     * @param itemRevision
     * @return
     */
    public boolean unProtectCaseByCase(Variant varResult, OleAutomation workbook, TCComponentItemRevision itemRevision){
    	String datasetUid = getLatestReleasedDatasetUID();
    	String itemRevisionUid = itemRevision.getUid();
    	String baseOnRevisionUid = null;
    	try {
			TCComponentItemRevision baseOnRevision = itemRevision.basedOn();
			if(baseOnRevision!=null){
				baseOnRevisionUid = baseOnRevision.getUid();
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
    	
    	boolean result = false;
    	
    	System.out.println("Item Revision uid = "+ itemRevisionUid);
    	varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(itemRevisionUid) });
    	if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
            System.out.println("Unprotect invoke result = " + varResult);
            result = varResult.getBoolean();
            varResult.dispose();
            unprotectTryCount = 0; //Count 초기화
    	}else{
    		System.out.println("Dataset uid = "+datasetUid);
    		unprotectTryCount++;
    		varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(datasetUid) });
        	if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
                System.out.println("Unprotect invoke result = " + varResult);
                result = varResult.getBoolean();
                varResult.dispose();
                unprotectTryCount = 0; //Count 초기화
        	}else{
        		System.out.println("BaseonRevision uid = "+baseOnRevisionUid);
        		unprotectTryCount++;
        		varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant(baseOnRevisionUid) });
            	if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
                    System.out.println("Unprotect invoke result = " + varResult);
                    result = varResult.getBoolean();
                    varResult.dispose();
                    unprotectTryCount = 0; //Count 초기화
            	}else{
            		System.out.println("Basic uid = symc");
            		unprotectTryCount++;
            		varResult = invokeMethod("workbook", workbook, "Unprotect", new Variant[] { new Variant("symc") });
            		if(varResult != null && varResult.getType() != OLE.VT_EMPTY) {
                        System.out.println("Unprotect invoke result = " + varResult);
                        result = varResult.getBoolean();
                        varResult.dispose();
                        unprotectTryCount = 0; //Count 초기화
                	}else{
                   		System.out.println("=====failed invoke unprotectWorkbook method ====");
                	}
            	}
        	}
    	}
    	
    	return result;
    }

    public void unlock() {
        if(!frame.isEnabled()) {
            setEnable(getShell());
            clientSite.setFocus();
        }
    }

    private void createOpenButton() {
        btnOpen = new Button(this.frame, SWT.FLAT);
        btnOpen.setText("작업그림/관리점 이미지를 여시려면 여기를 클릭해주세요.");
        
        // [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
        // 영문의 경우, 공법의 상태에 따라 버튼 활성/비활성을 경정함.
        enabledButton();
        
        btnOpen.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                openExcelView();
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {

            }
        });

        frame.layout();
    }

    /**
     * [SR번호][201411141530] shcho, LatestReleased 공법 리비전의  DatasetUID를 가져오는 함수 신규 생성
     * [SR번호][20141215] shcho, 용접조건표에서도 작업표준서와 마찬가지로 LatestReleased DatasetUID를 가져오는 조건 추가
     *      
     * @return
     */
    private String getLatestReleasedDatasetUID() {      
        try {
            AIFComponentContext[] arrAIFComponentContext = targetDataset.whereReferenced();
            for (AIFComponentContext aifComponentContext : arrAIFComponentContext) {
                InterfaceAIFComponent aifComponent = aifComponentContext.getComponent();
                if(aifComponent instanceof TCComponentItemRevision) {
                    TCComponentItemRevision itemRevision = (TCComponentItemRevision)aifComponent;
                    TCComponentItem item = itemRevision.getItem();
                    TCComponentItemRevision latestReleasedRevision = SYMTcUtil.getLatestReleasedRevision(item);
                    
                    TCComponent[] comps = null;

                    //용접공법인 경우(용접조건표)
                    if(itemRevision.getType().equals(SDVTypeConstant.BOP_PROCESS_BODY_WELD_OPERATION_ITEM_REV)) {
                    	if(latestReleasedRevision!=null){
                    		comps = latestReleasedRevision.getRelatedComponents(SDVTypeConstant.WELD_CONDITION_SHEET_RELATION);
                    	}
                    }
                    //일반공법인 경우(작업표준서)
                    else {
                        if(langConfigId == 0) {
                        	if(latestReleasedRevision!=null){
                        		comps = latestReleasedRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_KO_RELATION);
                        	}
                        } else {
                        	if(latestReleasedRevision!=null){
                        		comps = latestReleasedRevision.getRelatedComponents(SDVTypeConstant.PROCESS_SHEET_EN_RELATION);
                        	}
                        }
                    }

                    if(comps != null && comps.length > 0) {
                        for(TCComponent comp : comps) {
                            if (comp instanceof TCComponentDataset) {
                                return ((TCComponentDataset) comp).getUid();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox.post(this.frame.getShell(), e.getMessage(), "Excel", MessageBox.ERROR);
        } 
        
        return null;
    }

    /**
     * [P0087] [20150130] ymjang TC Save 버튼을 누르지 않고, 열려 있는 Excel프로그램에서 Save시에도 바로 TC로 Save 될 수 있도록 기능 개선 요청
     * 파일 변경시 변경된 파일 업로드
     * @return
     */
    public boolean uploladFile() {
    	return true;
    }
    
    /**
     * [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
     */
    public void enabledButton() {
        return;
    }
    
    
    
    // [SR150312-024] [20150324] ymjang, Latest Working for ME 상태에서 영문 작업표준서 작업 가능토록 개선
    // 공법의 릴리즈 상태를 체크한다.
    protected boolean isReleasedOP()
    {
        boolean isReleased = true;
        HashMap<String, Object> paramMap = null;
        String id = UIManager.getCurrentDialog().getId();
        IDialog dialog = UIManager.getAvailableDialog(id);
        Map<String, Object> parameters = dialog.getParameters();

        if(parameters == null)
            return false;
        
        // Search 화면에서의 Preview 시에
        if(parameters.containsKey("targetOperaion")) {
            paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
            if(paramMap != null) {
                String date_released = (String) paramMap.get(SDVPropertyConstant.ITEM_DATE_RELEASED);
                if (date_released.isEmpty())
                    isReleased = false;
                else
                    isReleased = true;
            }
        } 
        // BOP 에서의 Preview 시에
        else {
            try {
                TCComponentBOPLine operationLine = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
                Date pdate_released = operationLine.getItemRevision().getDateProperty(SDVPropertyConstant.ITEM_DATE_RELEASED);
                if (pdate_released == null)
                    isReleased = false;
                else
                    isReleased = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        return isReleased ;
        
    }    
    
    /**
     * [NON-SR][20160205] taeku.jeong Excel File을 연 Dataset이 포함된 Item Revision 정보를 확인하기위해 추가
     * @return
     */
    public TCComponentItemRevision getItemRevision() {
		return itemRevision;
	}

    /**
     * [NON-SR][20160205] taeku.jeong Excel File을 연 Dataset이 포함된 Item Revision 정보를 확인하기위해 추가
     * @param itemRevision
     */
	public void setItemRevision(TCComponentItemRevision itemRevision) {
		this.itemRevision = itemRevision;
	}

}
