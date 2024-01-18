package com.symc.common.exception;

import java.util.HashMap;

/**
 * BOM line 속성 가져올때 에러가 발생하면
 * NotLoadedChildLineException을 발생함.
 *
 * @author slobbie
 *
 */
@SuppressWarnings("serial")
public class NotLoadedChildLineException extends Exception {

	private HashMap<String, String> exceptionInfoMap = null;

	public static final String ITEM_ID = "ITEM_ID";
	public static final String ITEM_REVISION_ID = "ITEM_REVISION_ID";

    public NotLoadedChildLineException(Throwable t) {
        super(t);
    }

    public NotLoadedChildLineException(String se) {
        super(se);
    }

    public void addInfo(String key, String value){
    	if( exceptionInfoMap == null){
    		exceptionInfoMap = new HashMap<String, String>();
    	}

    	exceptionInfoMap.put(key, value);

    }

	public HashMap<String, String> getExceptionInfoMap() {
		return exceptionInfoMap;
	}

}
