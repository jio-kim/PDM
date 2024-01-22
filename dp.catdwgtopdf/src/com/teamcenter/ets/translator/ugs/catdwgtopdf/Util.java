package com.teamcenter.ets.translator.ugs.catdwgtopdf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Util {
    // 파일 로그를 생성.
	
	void setLog(String lo, String s) throws IOException {
  
		File zLogFile = new File( lo );			
		zLogFile.createNewFile();	
	      
		BufferedWriter out = new BufferedWriter(new FileWriter(zLogFile));                                    
		out.write(s);	      
		out.newLine();	      
		out.close();		
	} 
}
