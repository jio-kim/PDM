package com.kgm.common.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.eclipse.ui.internal.util.BundleUtility;
import org.osgi.framework.Bundle;

import com.kgm.Activator;
import com.teamcenter.rac.kernel.TCAttachmentScope;
import com.teamcenter.rac.kernel.TCAttachmentType;
import com.teamcenter.rac.kernel.TCCRDecision;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentProcess;
import com.teamcenter.rac.kernel.TCComponentSignoff;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentUser;
import com.teamcenter.rac.kernel.TCException;

@SuppressWarnings({ "rawtypes", "restriction", "unused", "unchecked" })
public class TemplateParser {
	protected Hashtable<String, String> hashtable;
    protected Vector<Vector> vector;
	protected Vector<String[]> workflowVector;
	protected StringBuilder[] strings;
	private SimpleDateFormat simpleDateFormat;
	
	public TemplateParser() {
	}

	public void parseWorkflow(TCComponentProcess process, File file) {
		try {
			simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			hashtable = new Hashtable<String, String>();
			hashtable.put("@Title", process.getName());
			hashtable.put("@Decision", process.getRootTask().getTaskState());
			hashtable.put("@Creator", process.getRelatedComponent("owning_user").toString());
			hashtable.put("@Date", getDateInfomation(process.getRootTask(), true));
			hashtable.put("@Comments", process.getInstructions());
			TCComponent[] components = process.getRootTask().getAttachments(TCAttachmentScope.GLOBAL, TCAttachmentType.TARGET);
			StringBuilder stringBuilder = new StringBuilder();
			for (int i = 0; i < components.length; i++) {
				stringBuilder.append(components[i].toDisplayString());
				if (i < components.length - 1){
					stringBuilder.append("\n");
				}
			}
			hashtable.put("@Attachments", stringBuilder.toString());
			workflowVector = new Vector<String[]>();
			analyzeWorkflow(process.getRootTask());
			vector = new Vector<Vector>();
			vector.addElement(workflowVector);
			strings = new StringBuilder[vector.size()];
			for (int i = 0; i < vector.size(); i++) {
				strings[i] = new StringBuilder();
			}
			Bundle bundle = Activator.getDefault().getBundle();
			URL url = BundleUtility.find(bundle, "icons\\workflowhistory.html");
			String content = parseContents(url);
			download(file, content);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void analyzeWorkflow(TCComponentTask task) {
		try {
			task.refresh();
			if (task.getTaskType().equals("EPMTask") || task.getTaskType().equals("EPMReviewTask") || task.getTaskType().equals("EPMAcknowledgeTask")) { // AcknowledgeTask 추가
				// TCProperty property = task.getProcess().getTCProperty("owning_user");
				// TCComponentUser user = (TCComponentUser)property.getReferenceValue();
				// model.addRow(new String[] {task.getName(), user.toString(), task.getTaskState(), task.getInstructions(), getDateInfomation(task, true)});
				TCComponentTask[] subTask = task.getSubtasks();
				if (subTask.length > 0) {
					for (int i = 0; i < subTask.length; i++)
						analyzeWorkflow(subTask[i]);
				}
			} else if (task.getTaskType().equals("EPMDoTask")) {
				TCComponentUser user = (TCComponentUser)task.getResponsibleParty();
				workflowVector.addElement(new String[]{task.getName(), user.toString(), getDateInfomation(task, false), task.getInstructions(), task.getTaskState()});
			} else if (task.getTaskType().equals("EPMPerformSignoffTask")) {
				TCComponentSignoff[] signoffs = task.getValidSignoffs();
				if (signoffs.length == 0) {
					workflowVector.addElement(new String[]{task.getParent().getName(), "Pass", "Pass", "Pass", "Pass"});
				}
				for (int j = 0; j < signoffs.length; j++) {
					TCComponentSignoff signoff = signoffs[j];
					signoff.refresh();
					String state = task.getProperty("real_state");
					if (task.getTaskState().equals("Started")) {
						// 2024.01.09 수정  TCCRDecision.REJECT_DECISION -->  signoff.getRejectDecision()
						if (signoff.getDecision().equals(signoff.getRejectDecision()))
							state = signoff.getDecision().toString();
					}
					TCComponentUser user = signoff.getGroupMember().getUser();
					workflowVector.addElement(new String[]{task.getParent().getName(), user.toString(), getDateInfomation(task, false), task.getValidSignoffs()[0].getComments(), task.getTaskState()});
				}
			} else if (task.getTaskType().equals("EPMAddStatusTask")) {
			}
		} catch (TCException e) {
			e.printStackTrace();
		}
	}

	public String getDateInfomation(TCComponentTask task, boolean isCreation) {
		String dateProperty = "";
		if (isCreation) {
			dateProperty = "creation_date";
		} else {
			dateProperty = "last_mod_date";
		}
		try {
			return simpleDateFormat.format(task.getDateProperty(dateProperty));
		} catch (TCException e) {
			return null;
		}
	}

	public String parseContents(URL url) {
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		try {
			InputStream inputStream = url.openStream();
			Reader reader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(reader);
			setVector();
			while ((line = bufferedReader.readLine()) != null) {
				line = parseHashtableData(line);
				line = parseRowDatas(line);
				stringBuilder.append(line + "\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringBuilder.toString();
	}

	public void download(File file, String content) {
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(content);
			writer.flush();
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String parseHashtableData(String line) {
		Enumeration enumeration = hashtable.keys();
		while (enumeration.hasMoreElements()) {
			String key = (String)enumeration.nextElement();
			line = line.replace(key, hashtable.get(key));
		}
		return line;
	}

	private String parseRowDatas(String line) {
		if (line.contains("@Line")) {
			int i = line.indexOf("@Line");
			int index = Integer.parseInt(line.substring(i + 5).trim());
			line = line.replace(line, strings[index - 1]);
		}
		return line;
	}

	private void setVector() {
		for (int i = 0; i < vector.size(); i++) {
			Vector<String[]> rowVector = vector.elementAt(i);
			for (int j = 0; j < rowVector.size(); j++) {
				String[] rowData = rowVector.elementAt(j);
				if(j%2==0){
					strings[i].append("<TR class='odd'>");
				}else{
					strings[i].append("<TR>");
				}
				for (int k = 0; k < rowData.length; k++) {
					strings[i].append("<TH>");
					strings[i].append(rowData[k]);
					strings[i].append("</TH>");
				}
				strings[i].append("</TR>");
			}
		}
	}
}
