package com.symc.plm.me.sdv.view.excel;

public class DatasetCloseWaiter extends Thread {

	private DatasetOpenForEditManager datasetOpenForEditManager;
	private ExcelView excelView;
	
	public DatasetCloseWaiter(DatasetOpenForEditManager datasetOpenForEditManager, ExcelView excelView){
		this.datasetOpenForEditManager = datasetOpenForEditManager;
		this.excelView = excelView;
	}

	@Override
	public void run() {
		while (datasetOpenForEditManager.isExitFlag()==false) {

			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
		
		excelView.clossExcelApplicatoin();
	}
	
	
}
