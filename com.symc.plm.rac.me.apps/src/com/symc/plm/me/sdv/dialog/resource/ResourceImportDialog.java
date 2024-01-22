package com.symc.plm.me.sdv.dialog.resource;

import java.io.File;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;




import com.symc.plm.me.utils.BundleUtil;
import com.symc.plm.me.utils.SYMTcUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCComponentItemType;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCPreferenceService;
import com.teamcenter.rac.kernel.TCPreferenceService.TCPreferenceLocation;
import com.teamcenter.rac.util.MessageBox;
import common.Logger;

public class ResourceImportDialog extends BWXLSImpDialog {

    private static final Logger logger = Logger.getLogger(ResourceImportDialog.class);
    
    /* Template Item 정의 preference */
    private String preferenceName;

    public ResourceImportDialog(Shell parent, int style) {
        super(parent, style, ResourceImportDialog.class);
    }

    public void dialogOpen() {
       super.dialogOpen();
        
    }
    
    @Override
    public void downLoadTemplate() {
        
        preferenceName =  getTextBundle("PreferenceName", null, dlgClass); 
        int scope = TCPreferenceService.TC_preference_site;
        
        TCPreferenceService preferenceService = session.getPreferenceService();
//        String prefValue = preferenceService.getString(scope, preferenceName);
        String prefValue = preferenceService.getStringValueAtLocation(preferenceName, TCPreferenceLocation.convertLocationFromLegacy(scope));
        
        if (preferenceName.equals(""))
        {
            MessageBox.post(this.shell, getTextBundle("TemplateInvalid", "MSG", dlgClass), "Notification", 2);
            return;
        }
        
        if (prefValue.equals(""))
        {
            MessageBox.post(this.shell, "'" + preferenceName + "' " + getTextBundle("PreferenceInvalid", "MSG", dlgClass), "Notification", 2);
            return;
        }
        
        try
        {
            
            FileDialog fDialog = new FileDialog(this.shell, SWT.SINGLE | SWT.SAVE);
            fDialog.setFilterNames(new String[] { "Excel File" });
            fDialog.setFileName(strTemplateDSName);
            // *.xls, *.xlsx Filter 설정
            fDialog.setFilterExtensions(new String[] { "*.xlsx" });
            String strRet = fDialog.open();
            
            if ((strRet == null) || (strRet.equals("")))
                return;       
            
            String strfileName = fDialog.getFileName();
            if ((strfileName == null) || (strfileName.equals("")))
                return;
            
            String strDownLoadFilePath = fDialog.getFilterPath()+File.separatorChar + strfileName;
            
            File checkFile = new File(strDownLoadFilePath);
            if( checkFile.exists() )
            {
            
                org.eclipse.swt.widgets.MessageBox box1 = new org.eclipse.swt.widgets.MessageBox(shell, SWT.OK | SWT.CANCEL | SWT.ICON_INFORMATION);
                box1.setMessage(strDownLoadFilePath+this.getTextBundle("FileExist", "MSG", dlgClass));
               
                if (box1.open() != SWT.OK)
                {
                    return;
                }
            }
            
            
            File tempFile = getTemplateFile(prefValue);
            
            if( checkFile.exists() )
                checkFile.delete();
            
            tempFile.renameTo(new File(strDownLoadFilePath));
            
            
            MessageBox.post(this.shell, strDownLoadFilePath+" 파일 다운로드가 완료되었습니다.", "Notification", 2);
        }
        catch (Exception e)
        {
            MessageBox.post(this.shell, e.toString(), "Notification", 2);
        }
        
    }
    
    /**
     * Template File을 문서 Item으로 부터 다운받는 함수
     * @param itemID
     * @return
     */
    public File getTemplateFile(String itemID) {
        File file = null;
       
        TCComponentItem item = null;
        TCComponentItemRevision itemRev = null;
        TCComponentDataset dataset = null;
        try {
            TCComponentItemType componentItemType = (TCComponentItemType)session.getTypeComponent("Item");
            TCComponentItem[] findItems = componentItemType.findItems(itemID);
            if(findItems == null || findItems.length == 0) throw new NullPointerException("Cann't found Item as " + itemID);
            item = findItems[0];
            
            itemRev = item.getReleasedItemRevisions()[0];
            AIFComponentContext[] contextList = itemRev.getChildren();
            for(AIFComponentContext context : contextList) {
                InterfaceAIFComponent aifComponent = context.getComponent();
                if(aifComponent instanceof TCComponentDataset) {
                    String compType = aifComponent.getType();
                    if(compType.equals("MSExcel") || compType.equals("MSExcelX")) {
                        dataset = (TCComponentDataset)aifComponent;
                        TCComponentTcFile[] tcFiles = dataset.getTcFiles();
                        
                        if(tcFiles.length>0) {
                            File[] files = dataset.getFiles(SYMTcUtil.getNamedRefType(dataset, tcFiles[0]));
                            
                            if(files != null) {
                                file = files[0];
                            }
                        }
                    }                    
                }
            }
          
        } catch (TCException tcex) {
            logger.error(tcex);
        } catch (Exception ex) {
            logger.error(ex);
        }
        
        return file;
    }
    
    
    /**
     * Excel Cell Value Return
     * 
     * CELL_TYPE_NUMERIC인 경우 Integer로 Casting하여 반환함
     * Long 형태의 값을 원할경우 다르게 구현해야 함.
     * 
     * @param cell
     * @return
     */
    @Override
    public String getCellText(Cell cell)
    {
        String value = "";
        if (cell != null)
        {
            
            switch (cell.getCellType())
            {
                case XSSFCell.CELL_TYPE_FORMULA:
                    value = cell.getStringCellValue();
                    break;
                
                // Integer로 Casting하여 반환함
                case XSSFCell.CELL_TYPE_NUMERIC:
                    value = "" +  getFormatedString(cell.getNumericCellValue());
                    break;
                
                case XSSFCell.CELL_TYPE_STRING:
                    value = "" + cell.getStringCellValue();
                    break;
                
                case XSSFCell.CELL_TYPE_BLANK:
                    // value = "" + cell.getBooleanCellValue();
                    value = "";
                    break;
                
                case XSSFCell.CELL_TYPE_ERROR:
                    value = "" + cell.getErrorCellValue();
                    break;
                default:
            }
            
        }
        
        return BundleUtil.nullToString(value);
    }
}
