package com.kgm.soa.bop.util;

public class FilePathPars {

	private String strFilePath = "";
	private String strFileName = "";
	private String strFileDir = "";
	private String strFileExt = "";		
	private String strFileName_FileExtDel = "";
	
	public FilePathPars(String strFilePath) throws Exception {
		this.strFilePath = strFilePath;
		this.strFileName = getFileName(strFilePath);
		this.strFileDir = getFileDir(strFilePath);
		this.strFileExt = getFileExtion(strFilePath);
		this.strFileName_FileExtDel = getFileName_FileExtDel(strFilePath);
	}

	public String getStrFileDir() {
		return strFileDir;
	}

	public String getStrFileExt() {
		return strFileExt;
	}

	public String getStrFileName() {
		return strFileName;
	}

	public String getStrFilePath() {
		return strFilePath;
	}
	
    private String getFileName(String strFilePath) throws Exception
    {
    	String strFileName = "";
    	try{
    		int intPosit = 0;
    		intPosit = strFilePath.lastIndexOf('\\', strFilePath.length());
    		
    		if (intPosit == -1) {
    			strFileName = strFilePath;
    		}
    		else {
        		intPosit = intPosit + 1;
        		strFileName = strFilePath.substring(intPosit);    		    			
    		}
    	}
    	catch (Exception err) {
    		throw err;
    	}    	
    	return strFileName;
    }   	
    
    private String getFileName_FileExtDel(String strFilePath) throws Exception {
    	String strFileName = "";
    	try {
    		int intPosit = 0;
    		intPosit = strFilePath.lastIndexOf('\\', strFilePath.length());
    		
    		if (intPosit == -1) {
    			strFileName = strFilePath;
    		}
    		else {
        		intPosit = intPosit + 1;
        		strFileName = strFilePath.substring(intPosit);    		    			
    		}    		
    		
    		int intDotPosit = strFileName.lastIndexOf('.', strFileName.length());
    		strFileName = strFileName.substring(0, intDotPosit);		   
    	}
    	catch (Exception err) {
    		throw err;
    	}    	
    	return strFileName;
    }        
	
    private String getFileExtion(String strFilePath) throws Exception {
    	String strFileExt = "";
    	try {
    		//-------------------------------------------------------
    		// 파일 FullPathName 문자열 끝에서부터 점(.)이 시작되는 위치(Index)를 찾아
    		// 확장자를 읽어온다.
    		//-------------------------------------------------------			
    		int intDotPosit = strFilePath.lastIndexOf('.', strFilePath.length());
    		
    		if (intDotPosit == -1) {
    			strFileExt = "";
    		}
    		else {
    			intDotPosit = intDotPosit + 1;
    			strFileExt = strFilePath.substring(intDotPosit);
    		}    		
    	}
    	catch (Exception err) {
    		throw err;
    	}
		
		return strFileExt;
    }	
	
    private String getFileDir(String strFilePath) throws Exception {
    	String strFileDir = "";
    	try {
        	int intPosit = 0;
        	intPosit = strFilePath.lastIndexOf('\\', strFilePath.length());
        	if (intPosit == -1) {
        		strFileDir = "";
        	}
        	else {
        		strFileDir = strFilePath.substring(0, intPosit);
        	}
    	}
    	catch (Exception err) {
    		throw err;
    	}
    	return strFileDir;
    }

	public String getStrFileName_FileExtDel() {
		return strFileName_FileExtDel;
	}   	
	
}
