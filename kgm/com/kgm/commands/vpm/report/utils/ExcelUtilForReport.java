package com.kgm.commands.vpm.report.utils;

import java.io.File;

import jxl.Workbook;
import jxl.SheetSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.PageOrientation;
import jxl.format.VerticalAlignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.kgm.common.utils.StringUtil;
import com.teamcenter.rac.util.MessageBox;

public class ExcelUtilForReport {
    
    /**
     * SWT Table을 Excel Export한다.
     * 
     * @method exportDataXLS 
     * @date 2013. 5. 31.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    public static void exportDataXLS(Shell shell, Table table, String title, int sheetNum) throws Exception {
        FileDialog fDialog = new FileDialog(shell, SWT.SINGLE | SWT.SAVE);
        fDialog.setFilterNames(new String[] { "Excel File" });
        //fDialog.setFileName(".xls");
        // *.xls, *.xlsx Filter 설정
        fDialog.setFilterExtensions(new String[] { "*.xls" });
        fDialog.open();
        String strfileName = fDialog.getFileName();
        if ((strfileName == null) || (strfileName.equals(""))) {
            return;
        }
        String strDownLoadFilePath = fDialog.getFilterPath() + File.separatorChar + strfileName;
        File checkFile = new File(strDownLoadFilePath);
        if(checkFile.isFile()) {
            org.eclipse.swt.widgets.MessageBox box = new org.eclipse.swt.widgets.MessageBox(shell, SWT.YES | SWT.NO);
            box.setText("Confirm");
            box.setMessage("Are you sure you want to change the file?");
            if(!(box.open() == SWT.YES)) {
                return;
            } 
        }
        WritableWorkbook workBook = null;
        try {
            workBook = Workbook.createWorkbook(checkFile);
            WritableSheet sheet = workBook.createSheet(title, sheetNum);
            SheetSettings printSet = sheet.getSettings();
            printSet.setFitWidth(1);
            printSet.setFitToPages(true);
            printSet.setOrientation(PageOrientation.LANDSCAPE);

            // 1. 헤더 정보 가져오기
            TableColumn[] tableColumns = table.getColumns();
            int[] iColumnWidth = new int[tableColumns.length];

            for (int i = 0; i < iColumnWidth.length; i++) {
                jxl.write.Label cellTitle = new jxl.write.Label(i, 0, tableColumns[i].getText(), setCellValueFormat(2));
                sheet.addCell(cellTitle);
                sheet.setRowView(0, 300);
            }
            // 2. 전체 Row 정보 가져오기
            String rowGroupKey = "";
            WritableCellFormat rowCell_0 = setCellValueFormat(0);
            WritableCellFormat rowCell_1 = setCellValueFormat(1);            
            WritableCellFormat rowCell_sel = null; 
            TableItem[] resultDatas = table.getItems();
            for (int i = 0; i < resultDatas.length; i++) {
                TableItem rowItem = resultDatas[i];
                String key = StringUtil.nullToString((String)rowItem.getData("GROUP_KEY"));                
                // key가 다르면 다른 Row 색깔을 변경
                if(i == 0) {
                    rowCell_sel = rowCell_0;
                    rowGroupKey = key;
                } else {
                    if(!rowGroupKey.equals(key)) {                    
                        if(rowCell_sel == rowCell_1) {
                            rowCell_sel = rowCell_0;
                        } else {
                            rowCell_sel = rowCell_1;
                        }
                        rowGroupKey = key;
                    } 
                }
                for (int j = 0; j < iColumnWidth.length; j++) {
                    jxl.write.Label cellValue = new jxl.write.Label(j, (i + 1), rowItem.getText(j), rowCell_sel);
                    sheet.addCell(cellValue);
                    sheet.setRowView(0, 300);
                }
            }
            workBook.write();
            MessageBox.post(shell, "Excel Export Completed.", "Completed", MessageBox.INFORMATION);
        }catch (Exception e) {
            throw e;
        } finally {
            if(workBook != null) {            
                workBook.close();
            }
        }   
    }

    /**
     * jxl 의 Cell Format 을 설정한다.
     * 
     * @param feature
     * @return
     * @throws WriteException
     */
    public static WritableCellFormat setCellValueFormat(int status) throws Exception {
        WritableFont wf = new WritableFont(WritableFont.createFont("굴림"), 9, WritableFont.NO_BOLD);

        WritableCellFormat wcf = new WritableCellFormat(wf);
        wcf.setWrap(false);
        wcf.setLocked(false);
        // wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
        wcf.setBorder(Border.ALL, BorderLineStyle.HAIR);

        switch (status) {
        case 0:
            wcf.setAlignment(Alignment.LEFT);
            wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
            break;

        case 1:
            wcf.setAlignment(Alignment.LEFT);
            wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
            wcf.setBackground(Colour.LIGHT_TURQUOISE);
            break;

        case 2:
            wcf.setAlignment(Alignment.LEFT);
            wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
            wcf.setBackground(Colour.GREY_25_PERCENT);
            break;

        }
        return wcf;
    }
}
