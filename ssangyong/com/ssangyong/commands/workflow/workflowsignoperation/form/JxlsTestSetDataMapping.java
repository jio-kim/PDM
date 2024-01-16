package com.ssangyong.commands.workflow.workflowsignoperation.form;

import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WriteException;
import jxl.format.*;

public class JxlsTestSetDataMapping {
	private Label lb = null;
	
	public Label setLabelData(String beansKey, String beansValue, int row, int col){
		
		WritableCellFormat nameFormat = new WritableCellFormat();
		try {
			nameFormat.setAlignment(Alignment.CENTRE);
			nameFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
			nameFormat.setBorder(Border.ALL, BorderLineStyle.THIN); 
		
			if(beansKey != null){
				lb = new Label(col,row,beansValue,nameFormat);
				
			}else{
				System.out.println("파라미터 값이 null입니다.");
				System.exit(0);
			}
		
		} catch (WriteException e) {
			e.printStackTrace();
		}
		
		return lb;
	}
	
public Label setLabelLeftData(String beansKey, String beansValue, int row, int col){
		
		WritableCellFormat nameFormat = new WritableCellFormat();
		try {
			nameFormat.setAlignment(Alignment.LEFT);
			nameFormat.setVerticalAlignment(VerticalAlignment.CENTRE);
			nameFormat.setBorder(Border.ALL, BorderLineStyle.THIN); 
		
			if(beansKey != null){
				lb = new Label(col,row,beansValue,nameFormat);
				
			}else{
				System.out.println("파라미터 값이 null입니다.");
				System.exit(0);
			}
		
		} catch (WriteException e) {
			e.printStackTrace();
		}
		
		return lb;
	}
}
