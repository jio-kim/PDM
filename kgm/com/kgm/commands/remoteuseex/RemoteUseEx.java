package com.kgm.commands.remoteuseex;

import java.util.ArrayList;

import com.kgm.model.RemoteUseExData;
import com.kgm.service.RemoteUseExService;


public class RemoteUseEx {
	
	/**
	 * ������.
	 * 
	 * @copyright : S-PALM
	 * @author : �ǻ��
	 * @throws Exception 
	 * @throws Exception 
	 * @since : 2012. 12. 13.
	 */
	public RemoteUseEx() throws Exception {
		// MyBatis ���� Remote ����.
		RemoteUseExService reportService = new RemoteUseExService();
		
		ArrayList<RemoteUseExData> result = reportService.getRemoteUseExData();
		
		RemoteUseExData resultList = null;
		for(int i=0; i<result.size(); i++){
			resultList = result.get(i);
			
			System.out.println("result [ " + i + " ] ��° �� : " + resultList.getItem_id());
		}
	}
}
