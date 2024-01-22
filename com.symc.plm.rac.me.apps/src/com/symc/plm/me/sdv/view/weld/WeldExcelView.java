package com.symc.plm.me.sdv.view.weld;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.ole.win32.OleAutomation;
import org.eclipse.swt.ole.win32.Variant;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataSet;
import org.sdv.core.ui.dialog.event.SDVInitEvent;
import org.sdv.core.ui.operation.AbstractSDVInitOperation;

import com.ssangyong.common.utils.SYMTcUtil;
import com.symc.plm.me.common.SDVPropertyConstant;
import com.symc.plm.me.sdv.excel.common.PreviewWeldConditionSheetExcelHelper;
import com.symc.plm.me.sdv.operation.wp.PreviewWeldConditionSheetInitOperation;
import com.symc.plm.me.sdv.view.excel.ExcelView;
import com.symc.plm.me.utils.CustomUtil;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponentBOPLine;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.util.MessageBox;

/**
 * [20150711] [ymjang] Ribbon 메뉴 저장 버튼 오류 수정
 */
public class WeldExcelView extends ExcelView {

	private PreviewWeldConditionSheetExcelHelper excelHelper;

    public WeldExcelView(Composite parent, int style, String id) {
        super(parent, style, id);
    }
    
    @Override
	protected void create(Composite parent) {
    	
    	String documentType = getId();
    	int langType = getConfigId();
    	
    	
    	
        TCComponentBOPLine operationLine = null;

        Map<String, Object> parameters = getParameters();
        if(parameters!=null && parameters.containsKey("targetOperaion")) {
            HashMap<String, Object> paramMap = (HashMap<String, Object>) parameters.get("targetOperaion");
            if(paramMap != null) {
                operationLine = (TCComponentBOPLine) paramMap.get("OPERATION_BOPLINE");
            }
        } else {
            operationLine = (TCComponentBOPLine) AIFUtility.getCurrentApplication().getTargetComponent();
        } 
        
        String mecoNo = null;
        boolean isReleased = false;
        TCComponentItemRevision mecoRevision = null;
        TCComponentItemRevision operationRevision = null;
        
        if(operationLine!=null){
			try {
				operationRevision = (TCComponentItemRevision) operationLine.getItemRevision();
				if(operationRevision!=null){
					mecoRevision = (TCComponentItemRevision) operationRevision.getReferenceProperty(SDVPropertyConstant.OPERATION_REV_MECO_NO);
				}
				if(mecoRevision!=null){
					mecoNo = mecoRevision.getItem().getProperty(SDVPropertyConstant.ITEM_ITEM_ID);
					isReleased = CustomUtil.isReleased(mecoRevision);
				}
			} catch (TCException e) {
				e.printStackTrace();
			}
        }
    	
        if(isReleased==false && mecoRevision!=null && operationRevision!=null){
        	try
			{
				new CustomUtil().buildMEPL((TCComponentChangeItemRevision) mecoRevision, true);
			} catch (Exception e)
			{
				e.printStackTrace();
				MessageBox.post(getShell(), e, true);
			}

//        	MECOCreationUtil aMECOCreationUtil = new MECOCreationUtil(mecoRevision);
//        	boolean isValideMEPL = aMECOCreationUtil.isValideOperationMEPL(mecoNo, operationRevision);
//        	if(isValideMEPL==false){
//        		MECOCreationUtil.createOrUpdateOperationMEPL(documentType, langType, getParameters());
//        	}
//        	System.out.println("isValideMEPL = "+isValideMEPL);
        	System.out.println("isValideMEPL");

        }
    	
		super.create(parent);
	}

	@Override
    public void addSheet()
    {
        OleAutomation targetSheet = null;
        OleAutomation userSheetLast = null;
        int userSheetCnt = 0;
        boolean userSheetCheck = false;
        //String lastSheetName = "";

        OleAutomation workbook = new OleAutomation(clientSite);
        OleAutomation sheets = getAutoProperty(workbook, "Sheets");
        int count = 0;
        if(sheets != null)
        {
            count = (int) getLongProperty(sheets, "Count");
            if(count > 0)
            {
                for(int i = 1; i <= count; i++)
                {
                    Variant var = new Variant(i);
                    OleAutomation sheet = getAutoProperty(sheets, "Item", new Variant[] {var});
                    if(sheet != null)
                    {
                        String name = getStringProperty(sheet, "Name");

                        if(name.startsWith("UserSheet"))
                        {
                            userSheetCnt++;
                            userSheetLast = sheet;
                            userSheetCheck = true;
                        }
                        else if("copySheet".equals(name))
                        {
                            targetSheet = sheet;
                        }
                        if(count == i)
                        {
                            //lastSheetName = name;
                            if (!userSheetCheck)
                                userSheetLast = sheet;
                        }
                    }
                }

                Variant[] params = new Variant[1];
                params[0] = new Variant(userSheetLast);

                if(unprotectWorkbook(workbook))
                {
                    Variant varResult = invokeMethod("copySheet", targetSheet, "Copy", params, new String[] {"Copy", "After"});

                    if(varResult != null)
                    {
                        System.out.println(" copy invoke result of USERSHEET = " + varResult);

                        count = (int) getLongProperty(sheets, "Count");
                        for(int i = 1; i <= count; i++)
                        {
                            Variant var = new Variant(i);
                            OleAutomation sheet = getAutoProperty(sheets, "Item", new Variant[] {var});
                            String name = getStringProperty(sheet, "Name");
                            if (name.equals("copySheet (2)"))
                            {
                                userSheetLast = sheet;
                                break;
                            }
                        }

                        if(userSheetLast.setProperty(property(userSheetLast, "Name"), new Variant("UserSheet" + ++userSheetCnt)))
                        {
                            userSheetLast.setProperty(property(userSheetLast, "Visible"), new Variant(true));
                            userSheetLast.setProperty(property(userSheetLast, "Activate"), new Variant(true));
                        }

                        varResult.dispose();
                    }
                    else
                    {
                        System.out.println("=====failed invoke copySheet method ====");
                    }
                    protectWorkbook(workbook);
                }
            }
        }
    }

    @Override
    public void removeSheet()
    {
        OleAutomation workbook = new OleAutomation(clientSite);
        OleAutomation sheet = new OleAutomation(clientSite);
        OleAutomation activeSheet = getAutoProperty(sheet, "ActiveSheet");
        String sheetName = getStringProperty(activeSheet, "Name");

        if(sheetName.startsWith("MECOList") || Pattern.matches("^[0-9]*$", sheetName))
        {
            MessageDialog.openError(AIFUtility.getActiveDesktop().getShell(), "Error", "System 에서 생성한 Sheet는 삭제할 수 없습니다.");
            return;
        }

        if(unprotectWorkbook(workbook))
        {
            super.removeSheet();
            protectWorkbook(workbook);
        }
    }

    @SuppressWarnings("static-access")
    @Override
    public void saveWorkbook()
    {
        // 체크아웃 체크
        if (!targetDataset.isCheckedOut() && CustomUtil.isWritable(targetDataset))
        {
            try {
                // OleAutomation 을 이용한 save
            	// [20150711] [ymjang] Ribbon 메뉴 저장 버튼 오류 수정
                //if (clientSite.isDirty())
                    clientSite.save(currentFile, true);
                    
                //TCReservationService tcreservationservice = targetDataset.getSession().getReservationService();
                //tcreservationservice.reserve(targetDataset);
                // 유저가 입력한 내용을 용접점 BOMLine 속성에 저장
                excelHelper.updateTeamcenter(currentFile, targetDataset);

                // 현재 까지의 입력한 Excel 파일을 Teamcenter Dataset 에 Update 한다
                if(targetDataset != null)
                {
                    Vector<File> files = new Vector<File>();
                    files.add(currentFile);
                    CustomUtil.removeAllNamedReference(targetDataset);
                    SYMTcUtil.importFiles(targetDataset, files);

                    //tcreservationservice = targetDataset.getSession().getReservationService();
                    //tcreservationservice.unreserve(targetDataset);

                    targetDataset.refresh();
                }
                MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Save", "저장 되었습니다.");
            } catch (IOException e) {
                e.printStackTrace();
                MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Error", "저장이 실패하였습니다. 관리자에게 문의 하세요.");
            } catch (TCException e) {
                e.printStackTrace();
                MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Error", "저장이 실패하였습니다. 관리자에게 문의 하세요.");
            } catch (Exception e) {
                e.printStackTrace();
                MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Error", "저장이 실패하였습니다. 관리자에게 문의 하세요.");
            }
        }
        else
        {
            MessageDialog.openInformation(AIFUtility.getActiveDesktop().getShell(), "Error", "현재 체크아웃 되어 사용중 또는 저장 권한이 없습니다.");
        }

    }

    @Override
    public AbstractSDVInitOperation getInitOperation() {
        return new PreviewWeldConditionSheetInitOperation();
    }

    @Override
    public TCComponentDataset getDataset() {
        return null;
    }
    
    
    /* [SR150714-008][20150713] shcho, Excel Open이 되지 않는 오류 수정
     * 작업표준서 Excel Open 오류 수정으로 인하여 ExcelView Class의  initalizeLocalData 로직 변경 됨.
     * 이로 인해 함께 상속받아 사용하던 WeldExcelView는 initalizeLocalData를 Override 하여 기존 로직 유지하도록 함
     */
    @Override
    public void initalizeLocalData(int result, IViewPane owner, IDataSet dataset) {

        if(result == SDVInitEvent.INIT_SUCCESS){
            if(getConfigId() == 0) {
                TCComponentDataset tcDataset = (TCComponentDataset)dataset.getData("exceltemplate");
                if(tcDataset != null){
                    setTargetDataset(tcDataset);
                }
            }
            
            enabledButton();
                    
        }
    }

}
