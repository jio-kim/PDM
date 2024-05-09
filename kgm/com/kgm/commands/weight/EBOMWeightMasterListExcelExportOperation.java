package com.kgm.commands.weight;

import java.awt.Color;
import java.io.File;
import java.util.Vector;

import javax.swing.JTable;

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

/**
 * [SR170707-024] E-BOM Weight Report ���� ��û
 * @Copyright : Plmsoft
 * @author   : ������
 * @since    : 2017. 7. 10.
 * Package ID : com.kgm.commands.weight.EBOMWeightMasterListExcelExportOperation.java
 */
public class EBOMWeightMasterListExcelExportOperation extends AbstractAIFOperation
{
    private File exportToFile;
    private Vector<Object> columnHeaders;
    private Vector<Vector<Object>> dataVector;
    private TCComponentItem targetItem;


    public EBOMWeightMasterListExcelExportOperation(  File selectedFile, Vector<Object> headerVector, Vector<Vector<Object>> dataVector, TCComponentItem selectedProductItem)
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
                // 0��° Sheet ����
                WritableSheet sheet = workBook.createSheet("New Sheet", 0);

                Label label = null;
            
                WritableCellFormat headerCellFormat = new WritableCellFormat(); // ���� ��Ÿ���� �����ϱ� ���� �κ��Դϴ�.
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
                        
                        if (j == 0) {
                        	label = new jxl.write.Label(j + initColumnNum, i + startRow, (i + 1) + "", getCellFormat(j, row));
                        }
                        else {
                        	
            					label = new jxl.write.Label(j + initColumnNum, i + startRow, rowValue == null ? "" : rowValue.toString(), getCellFormat(j, row));
                        }

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
        	// 2016-06-27 �÷� �߰��� ���� ����
            switch (colIndex) {
            case 0:
                return 5;
            case 1:
                return 17;
            case 2:
                return 8;
            case 3:
                return 6;
            case 4:
            	return 15; //FMP
            case 5:
            	return 8; //SEQ
            case 6:
            	return 6; //LEV(MAN)
            case 7:
                return 15; //PART NO
            case 8:
                return 45; //PART NAME
            case 9:
                return 9;   //SMODE         
            case 10:
                return 8; //WEIGHT
            default:
                return 12;
            }
        }
        catch (Exception ex)
        {
            throw ex;
        }
    }

    private CellFormat getCellFormat(int columnIndex, Vector<?> row) throws Exception
    {
        WritableCellFormat cellFormat = new WritableCellFormat(); // ���� ��Ÿ���� �����ϱ� ���� �κ��Դϴ�.
        cellFormat.setBorder(Border.ALL, BorderLineStyle.THIN); // ���� ��Ÿ���� �����մϴ�. �׵θ��� ���α׸��°ſ���
        cellFormat.setLocked(false);
        cellFormat.setWrap(false);
        
        // Team �߷� ��� ���ǿ� �´� ���� ���� ��� Excel ���Ϸ� �������� �Ҷ� �ش� ���� Cell ������ ����
        //////////////////////////////////////////////////////////////////////////////////////////////////////////
      if ( columnIndex >=12 && columnIndex % 2 == 0 ) {
			if( !((String)row.get(11)).equals( ((String)row.get(columnIndex)))){
				cellFormat.setBackground(Colour.RED);
			}
      }
      /////////////////////////////////////////////////////////////////////////////////////////////////////////

        switch (columnIndex)
        {
            case 5 :
                cellFormat.setAlignment(Alignment.LEFT);
                break;
            default:
                cellFormat.setAlignment(Alignment.CENTRE);
                break;
        }

        return cellFormat;
    }
    
    
}
