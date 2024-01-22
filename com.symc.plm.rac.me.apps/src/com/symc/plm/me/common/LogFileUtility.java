package com.symc.plm.me.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.log4j.Logger;

import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;

/**
 * Log 처리를 위한  Custom Class
 * @author Taeku Jeong
 *
 */
public class LogFileUtility {
	private long started; // 시작 시간
	private boolean fileAppend = false;
	private boolean simpleFileAppend = false;
	private File outFile;
	private File simpleOutFile;
	private String fileName;
	private String simpleFileName;
	private boolean outUseSystemOut = false;
	
	private boolean printTrace = false;
	
	/**
	 * 생성자
	 * @param logFileName LogFile의   파일 이름 
	 */
	public LogFileUtility(String logFileName) {

		// Log 파일이 생성될 위치
		String logFilepath = "C:\\TEMP";
		if(logFilepath!=null && logFilepath.trim().length()>0){
			logFilepath = logFilepath.trim();
			if(logFilepath.charAt((logFilepath.length()-1))=='\\'){
				logFilepath = logFilepath.substring(0, (logFilepath.length()-1));
			}
		}
		checkLogFilePath(logFilepath);
		
		fileAppend = false;
		simpleFileAppend = false;
		
		if(logFileName!=null){
			logFileName = logFileName.trim();
		}
		
		this.fileName = logFilepath+"\\"+logFileName;
		this.outFile = new File(this.fileName);
		this.outFile.delete();
		
		this.simpleFileName = logFilepath+"\\Simple_"+logFileName; 
		this.simpleOutFile = new File(this.simpleFileName);
		this.simpleOutFile.delete();
		
		setTimmerStarat();
	}
	
	/**
	 * 주어진  경로에 Log File이 있는지 확인하고 없으면  Log File을 생성한다.
	 * @param logFilepath
	 */
	private void checkLogFilePath(String logFilepath){
		
		logFilepath = logFilepath.replace('\\', '/');
		String[] pathSplits = logFilepath.split("/");

		if(pathSplits!=null && pathSplits.length>0){
			String pathString = null;
			for (int i = 0; i < pathSplits.length; i++) {
				if(i<1){
					pathString = pathSplits[i].trim();
				}else{
					pathString = pathString + "\\"+pathSplits[i].trim();
				}
				
				if(i>0){
					File folder = new File(pathString);
					if(folder.exists()==false){
						folder.mkdir();
					}
				}
			}
		}
	}
	
	/**
	 * 현재 시간의  Time Stamp에 해당하는 문자열을 생성해서 돌려 준다.
	 * @return Time Stamp 문자
	 */
	static public String getCurrentDateStamp(){
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd_HH:mm");
		String time = df.format(new Date());
		return time;
	}
	
	/**
	 * Exception 사항을 Log 파일에 기록한다.
	 * @param exception
	 */
	public void writeReport(String message) {
		
		Exception e1 = new Exception(message);
		StackTraceElement[] stackTraces = e1.getStackTrace();
		StackTraceElement messageStack = null;
		StackTraceElement messageParentStack = null;
		if(stackTraces!=null&&stackTraces.length>=2){
			messageStack = stackTraces[1];
		}
		if(stackTraces!=null&&stackTraces.length>=3){
			messageParentStack = stackTraces[2];
		}

		if(message==null || (message!=null && message.trim().length()<1)){
			message = "";
		}

		String outPutMessage = null;
		if(this.printTrace){
			if(messageStack!=null){
				outPutMessage = "["+getStackMessage(messageParentStack)+" -> "+getStackMessage(messageStack)+"]\n"+message;
			}else{
				outPutMessage = message;
			}
		}else{
			outPutMessage = message;
		}
		
		
		if(outFile!=null){
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = df.format(new Date());
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, fileAppend));
				String contentsString = "[" + time + "(" + getElapsedTime() + ")] "+ outPutMessage + "\n";
				Logger.getLogger(this.getClass()).debug(contentsString);
				if(outUseSystemOut==true){
					System.out.println(contentsString);
				}
				writer.write(contentsString);
				writer.flush();
				writer.close();
				if(!fileAppend){
					fileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}
	
	/**
	 * Exception 사항을 Log 파일에 기록한다.
	 * @param exception
	 */
	public void writeExceptionTrace(Exception exception){
		StringBuffer note = new StringBuffer();
		note.append("Error message : ");
		note.append(exception.getMessage());
		note.append("\n");
		note.append("Error stack : ");
		note.append("\n");

		StackTraceElement[] elements = exception.getStackTrace();
		for (int i = 0; i < elements.length; i++) {
			note.append(elements[i].toString());
			note.append("\n");
		}
		writeReport(note.toString());
	}
	
	/**
	 * Log 파일에 공백 인 줄을 표기한다.
	 * @param spaceCount
	 */
	public void writeBlankeRowReport(int spaceCount) {
		if(outFile!=null){
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, fileAppend));
				if(spaceCount>0){
					for (int i = 0; i < spaceCount; i++) {
						//Logger.getLogger(this.getClass()).debug("\n");
						writer.write("\n");
					}
				}
				writer.flush();
				writer.close();
				if(!fileAppend){
					fileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Function이 호출되는 순간의 시간을 시작 시간으로 설정한다.
	 *
	 */
	public void setTimmerStarat(){
		started = Calendar.getInstance().getTimeInMillis();	
	}

	/**
	 * started 시간으로 부터 소요된 시간을 문자열로 돌려 준다.
	 * @return 소요시간 문자열
	 */
	public String getElapsedTime() {
		String elapseStrig = null;

		float elapsedTime = ((System.currentTimeMillis() - started) / 1000);
		float elapsedMinut = elapsedTime / 60;
		float elapsedSec = elapsedTime % 60;
		int sec = 0;
		int minut = 0;
		int houre = 0;
		Format formatter = new DecimalFormat("##");
		Format formatter2 = new DecimalFormat("######");
		sec = Integer.parseInt(formatter.format(new Double(elapsedSec)));
		minut = Integer.parseInt(formatter2.format(new Double(elapsedMinut)));
		if (elapsedMinut > 60) {
			houre = minut / 60;
			minut = minut % 60;
		}
		houre = Integer.parseInt(formatter2.format(new Double(houre)));
		minut = Integer.parseInt(formatter.format(new Double(minut)));

		elapseStrig = houre + ":" + minut + ":" + sec;

		return elapseStrig;
	}

	/**
	 * Log File의  File 이름을 Return한다.
	 * @return Log File의 이름 (경로 포함)
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Log File의 파일의 파일 이름에 SIMPLE이 붙은 별도의 Log File을 만들고 Log를 남긴다.
	 * 이 Function은 사용자에게 간략한 내용으로  Debug를위한 목적이 아니라 정보 제공을 목적으로 Log를 남길때 사용한다.
	 * 
	 * @param message Log파일에 기록될 내용을 담은 문자열 
	 */
	public void writeSimpleReport(String message) {
		if(simpleOutFile!=null){
			DateFormat df = new SimpleDateFormat("yyyy-MM-d HH:mm:ss");
			String tiem = df.format(new Date());
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(simpleOutFile, simpleFileAppend));
				String contentsString = "[" + tiem + "(" + getElapsedTime() + ")] "+ message + "\n";
				Logger.getLogger(this.getClass()).debug("SIMPLE "+contentsString);
				writer.write(contentsString);
				writer.flush();
				writer.close();
				if(!simpleFileAppend){
					simpleFileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * SIMPLE이라는 이름이 추가된 Log File에 공백 줄을 추가한다.
	 * @param spaceCount
	 */
	public void writeBlankeRowSimpleReport(int spaceCount) {
		if(simpleOutFile!=null){
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(simpleOutFile, simpleFileAppend));
				if(spaceCount>0){
					for (int i = 0; i < spaceCount; i++) {
						writer.write("\n");
					}
				}
				writer.flush();
				writer.close();
				if(!simpleFileAppend){
					simpleFileAppend = true;
				}
			} catch (IOException ie) {
				ie.printStackTrace();
			}
		}
	}

	/**
	 * Simple Log 파일의 이름(경로 포)을 Return한다.
	 * @return
	 */
	public String getSimpleFileName() {
		return simpleFileName;
	}

	/**
	 * Simple Log File을 Return한다.
	 * @return
	 */
	public File getSimpleFile() {
		return simpleOutFile;
	}
	
	/**
	 * Static 형태로 호출 되어 Log 파일에 Log를 남기지 않고 Log를 Console에만 남긴다.
	 * @param message
	 */
	public static void consoleWrite(String message){
		Exception e1 = new Exception(message);
		StackTraceElement[] stackTraces = e1.getStackTrace();
		StackTraceElement messageStack = null;
		StackTraceElement messageParentStack = null;
		if(stackTraces!=null&&stackTraces.length>=2){
			messageStack = stackTraces[1];
		}
		if(stackTraces!=null&&stackTraces.length>=3){
			messageParentStack = stackTraces[2];
		}

		if(message==null || (message!=null && message.trim().length()<1)){
			message = "";
		}

		String outPutMessage = null;
		if(messageStack!=null){
			outPutMessage = "  ["+getCurrentDateStamp()+"\t"+getStackMessage(messageParentStack)+" -> "+getStackMessage(messageStack)+"]\n"+message;
		}else{
			outPutMessage = "  ["+getCurrentDateStamp()+"]\n"+message;
		}
		Logger.getLogger("com.teamcenter.volvo.util.LogFileUtility").debug(outPutMessage);
	}
	
	/**
	 * Message Stack에서 Log에 남길 내용중 Class의 호출 정보를 문자열로 Return한다.
	 * @param messageStack
	 * @return
	 */
	private static String getStackMessage(StackTraceElement messageStack){
		
		if(messageStack==null){
			return (String)"";
		}
		
		return ""+messageStack.getClassName()+"."+messageStack.getMethodName()+" ["+messageStack.getLineNumber()+"]";
	}

	public boolean isOutUseSystemOut() {
		return outUseSystemOut;
	}

	public void setOutUseSystemOut(boolean outUseSystemOut) {
		this.outUseSystemOut = outUseSystemOut;
	}
}
