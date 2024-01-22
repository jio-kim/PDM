package com.symc.plm.rac.prebom.prebom.operation.weightmasterlist;

import java.io.File;
import java.util.Vector;

import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.CellFormat;
import jxl.format.Colour;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.kernel.TCComponentItem;

public class WeightMasterListExcelExportOperation extends AbstractAIFOperation
{
    private File exportToFile;
    private Vector<Object> columnHeaders;
    private Vector<Vector<Object>> dataVector;
    private TCComponentItem targetItem;

    public WeightMasterListExcelExportOperation(File selectedFile, Vector<Object> headerVector, Vector<Vector<Object>> dataVector, TCComponentItem selectedProductItem)
    {
        this.exportToFile = selectedFile;
        this.columnHeaders = headerVector;
        this.dataVector = dataVector;
        this.targetItem = selectedProductItem;
    }

    @Override
    public void executeOperation() throws Exception
    {
        try
        {
            exportToExcel(exportToFile, columnHeaders, dataVector, targetItem);

            AIFShell aif = new AIFShell("application/vnd.ms-excel", exportToFile.getAbsolutePath());
            aif.start();
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private void exportToExcel(File targetFile, Vector<Object> colHeaders, Vector<Vector<Object>> dataVector, TCComponentItem topItem) throws Exception
    {
        try
        {
            WritableWorkbook workBook = Workbook.createWorkbook(targetFile);
            try
            {
                // 0번째 Sheet 생성
                WritableSheet sheet = workBook.createSheet("New Sheet", 0);

                Label label = null;
            
                WritableCellFormat headerCellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
                headerCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                headerCellFormat.setBackground(Colour.GREY_25_PERCENT);
                headerCellFormat.setAlignment(Alignment.CENTRE);
                headerCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                headerCellFormat.setWrap(true);
                headerCellFormat.setLocked(false);

                WritableFont wf = new WritableFont(WritableFont.createFont("Arial"), 16, WritableFont.NO_BOLD);
                WritableCellFormat titleCellFormat = new WritableCellFormat(wf);
                titleCellFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
                titleCellFormat.setAlignment(Alignment.CENTRE);
                titleCellFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
                titleCellFormat.setLocked(false);

                int startRow = 1;
                int initColumnNum = 0;

                label = new jxl.write.Label(0, startRow, "Weight Master List(" + topItem.toString() + ")", titleCellFormat);
                sheet.addCell(label);
                sheet.mergeCells(0, 1, colHeaders.size() - 1, 1);
                sheet.setRowView(startRow, 600);
            
                startRow = 3;
                for (int i = 0; i < colHeaders.size(); i++)
                {
                    label = new jxl.write.Label(i + initColumnNum, startRow, colHeaders.get(i).toString(), headerCellFormat);
                    sheet.addCell(label);
                    sheet.setColumnView(i + initColumnNum, getColumnWidth(i));
                }
                sheet.setRowView(startRow, 1000);

                startRow = 4;
                for (int i = 0; i < dataVector.size(); i++)
                {
                    Vector<?> row = dataVector.get(i);
                    Object value = row.get(0);
                    if (value == null)
                        continue;

                    for (int j = 0; j < row.size(); j++)
                    {
                        Object rowValue = row.get(j);

                        if (j == 0)
                            label = new jxl.write.Label(j + initColumnNum, i + startRow, (i + 1) + "", getCellFormat(j));
                        else
                            label = new jxl.write.Label(j + initColumnNum, i + startRow, rowValue == null ? "" : rowValue.toString(), getCellFormat(j));

                        sheet.addCell(label);
                    }
                }

                workBook.write();
            }
            catch (Exception ex)
            {
                workBook.removeSheet(0);
                throw ex;
            }
            finally
            {
                workBook.close();
            }
        }
        catch (Exception ex)
        {
            targetFile.delete();
            throw ex;
        }
    }

    private int getColumnWidth(int colIndex) throws Exception
    {
        try
        {
        	// 2016-06-27 컬럼 추가로 인한 수정
            switch (colIndex) {
            case 0:
                return 5;
            case 1:
                return 15;
            case 2:
                return 8;
            case 3:
                return 6;
            case 4:
            	return 30; //system name
            case 5:
            	return 15; //fmp
            case 6:
            	return 8; //SEQ
            case 7:
                return 6; //LEV(MAN)
            case 8:
                return 15; //PART NO
            case 9:
            	return 45; //PART NAME       
            case 10:
            	return 9;   //SMODE
            case 11:
            	return 10; //project
            case 12:
            	return 10; //nmcd
            case 13:
            	return 10; //WEIGHT
            default:
                return 12;
            }
            
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private CellFormat getCellFormat(int columnIndex) throws Exception
    {
        WritableCellFormat cellFormat = new WritableCellFormat(); // 셀의 스타일을 지정하기 위한 부분입니다.
        cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // 셀의 스타일을 지정합니다. 테두리에 라인그리는거에요
        cellFormat.setLocked(false);
        cellFormat.setWrap(false);

        switch (columnIndex)
        {
            case 6 :
                cellFormat.setAlignment(Alignment.LEFT);
                break;
            default:
                cellFormat.setAlignment(Alignment.CENTRE);
                break;
        }

        return cellFormat;
    }
}
