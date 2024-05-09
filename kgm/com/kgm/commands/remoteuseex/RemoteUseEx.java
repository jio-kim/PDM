package com.kgm.commands.remoteuseex;

import java.util.ArrayList;

import com.kgm.model.RemoteUseExData;
import com.kgm.service.RemoteUseExService;


public class RemoteUseEx {
	
	/**
	 * 생성자.
	 * 
	 * @copyright : S-PALM
	 * @author : 권상기
	 * @throws Exception 
	 * @throws Exception 
	 * @since : 2012. 12. 13.
	 */
	public RemoteUseEx() throws Exception {
		// MyBatis 연결 Remote 예제.
		RemoteUseExService reportService = new RemoteUseExService();
		
		ArrayList<RemoteUseExData> result = reportService.getRemoteUseExData();
		
		RemoteUseExData resultList = null;
		for(int i=0; i<result.size(); i++){
			resultList = result.get(i);
			
			System.out.println("result [ " + i + " ] 번째 값 : " + resultList.getItem_id());
		}
	}
}
