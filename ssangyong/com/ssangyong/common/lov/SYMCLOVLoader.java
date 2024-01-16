package com.ssangyong.common.lov;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.teamcenter.rac.kernel.TCComponentListOfValues;
import com.teamcenter.rac.kernel.TCComponentListOfValuesType;

public class SYMCLOVLoader {
	private static HashMap<String, TCComponentListOfValues> lovs = new HashMap<String, TCComponentListOfValues>();

	/**
	 * LOV ¹ÝÈ¯
	 * @param sLOVName
	 * @return
	 */
	public static TCComponentListOfValues getLOV(String sLOVName) {
		TCComponentListOfValues lov = null;
		
		if (sLOVName == null || sLOVName.equals("") || sLOVName.length() == 0) {
			return null;
		}
		
		if (lovs != null && lovs.size() > 0) {
			Set<String> sKeys = lovs.keySet();
			Iterator<String> iterator = sKeys.iterator();
			
			while (iterator.hasNext()) {
				String sKey = iterator.next();
				
				if (sKey.equals(sLOVName)) {
					lov = lovs.get(sKey);
					if (lov != null) {
						return lov;
					}
				}
			}
		}
		
		lov = TCComponentListOfValuesType.findLOVByName(sLOVName);
		lovs.put(sLOVName, lov);
		
		return lov;
	}
}
