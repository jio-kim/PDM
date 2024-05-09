package com.kgm.commands.ec.monitor;

import java.awt.Frame;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.kgm.common.WaitProgressBar;
import com.kgm.common.remote.DataSet;
import com.kgm.common.remote.SYMCRemoteUtil;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AbstractAIFCommand;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentChangeItemRevision;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentTask;
import com.teamcenter.rac.kernel.TCComponentTcFile;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

public class DatasetCheckCommand extends AbstractAIFCommand {
	TCSession session = (TCSession)AIFUtility.getDefaultSession();
	/**
	 * Revise Command
	 */
	public DatasetCheckCommand() {
		
		try {
			Registry registry = Registry.getRegistry(this);
//			final String downPath = "C:\\Temp";
			final InterfaceAIFComponent[] targetComponents = AIFUtility.getCurrentApplication().getTargetComponents();

//			System.out.println(targetComponents[0].getUid());
			
			if (this.validationCheck(targetComponents)) {
				AIFDesktop activeDesktop = AIFUtility.getActiveDesktop();
				Frame frame = AIFUtility.getActiveDesktop().getFrame();
				
//				File folder = new File(downPath);
//				if(!folder.isDirectory()) {//��ΰ� �������� Ȯ�� 
//					folder.mkdirs();//�ƴϸ� �����.
//				}
				
				final WaitProgressBar waitProgress = new WaitProgressBar(activeDesktop);
				waitProgress.setWindowSize(700, 500);
				waitProgress.setShowButton(true);
//				waitProgress.setAlwaysOnTop(true);
				waitProgress.start();
				
				
				AbstractAIFOperation operation = new AbstractAIFOperation() {
					
					@Override
					public void executeOperation() throws Exception {
						SYMCRemoteUtil remote = new SYMCRemoteUtil();
						DataSet ds = new DataSet();
						
				    	StringBuffer unSavedList = new StringBuffer(); 
						StringBuffer catDoubleCheck = new StringBuffer();
						StringBuffer changeRelation = new StringBuffer();
						ArrayList<String> doubleCheck = null;
						String ecoId = null;
						String pname = "ECO ["+ ecoId +"]" + "3D Saving Check.";
						String ecoNo = "ECO ["+ ecoId +"]";
//						String pname = "";
						int tCount = 0;
						try {
							TCComponent[] solutionItems = null;
							for(InterfaceAIFComponent targetComponent : targetComponents){
								TCComponent targetComp = (TCComponent) targetComponent;
								AIFComponentContext[] targetList =  targetComp.getChildren();
								for(AIFComponentContext target : targetList){
									String targetName = target.getComponent().toString();
									if(targetName.equals("Targets")){
										AIFComponentContext[] targetChildenList = target.getComponent().getChildren();
										for(AIFComponentContext targetChilden : targetChildenList){
											if(targetChilden.getComponent() instanceof TCComponentChangeItemRevision){
												ecoId = targetChilden.getComponent().getProperty("item_id");
												pname = "ECO ["+ ecoId +"]" + "3D Saving Check.";
												ecoNo = "ECO ["+ ecoId +"]";
											}
										}
										
										
									}
								}
							}
							TCComponent[] partRevisionList = CustomUtil.queryComponent("SYMC_Search_ItemRevision_InECO", new String[]{"item_id"}, new String[]{ecoId });
							ArrayList<TCComponent> solutionList = new ArrayList<TCComponent>(Arrays.asList(partRevisionList));
							ArrayList<String> compIDs = new ArrayList<String>();
							ArrayList<TCComponent> sortList = new ArrayList<TCComponent>();
							for(TCComponent comp : solutionList){
								compIDs.add(comp.getProperty("item_id"));
							}
							Collections.sort(compIDs);
							for(String compID : compIDs){
								for(TCComponent solutionItem : solutionList){
									if(solutionItem.getProperty("item_id").equals(compID)){
										sortList.add(solutionItem);
										continue;
									}
								}
							}
//							Arrays.sort(solutionItems);
							if(sortList != null && sortList.size() > 0){
								waitProgress.setStatus(pname + " Start...");
								for(TCComponent partRevision :  sortList){
									tCount++;
									String partID = partRevision.getProperty("item_id") + "/" + partRevision.getProperty("item_revision_id");
									AIFComponentContext[] contexts =  partRevision.getChildren();
									if(contexts != null && contexts.length > 0 ){
										doubleCheck = new ArrayList<String>();
										for(AIFComponentContext context : contexts){
											String dataSetType = context.getComponent().getType();
											if(dataSetType.equals("CATDrawing") ||dataSetType.equals("CATProduct") ||dataSetType.equals("CATPart")){
												TCComponentDataset dataSet = (TCComponentDataset)context.getComponent();
												String relation = context.getContextDisplayName().toString();
												String dataSetName = dataSet.getProperty("object_name");
												String releaseStatus = dataSet.getProperty("date_released");
												String[] originName = null;
												String orginName = null;
												String orginFileSize = null;
												TCComponent namedRef = null;
												TCComponent[] namedRefs = null;
												TCComponent catiaDocAttributes = null;
												TCComponentForm tcForm = null;
												String tcFormName = null;
												
												try{
													namedRefs = dataSet.getNamedReferences();
													for(TCComponent namedRefd : namedRefs){
														if(namedRefd.getType().equals("ImanFile")){//ImanFile�� �ϴ°� �´°�???
															namedRef = namedRefd;
															TCComponentTcFile namedRefTcFile = (TCComponentTcFile) namedRefd;
															orginName = namedRefTcFile.getProperty("original_file_name");
															orginFileSize = namedRefTcFile.getProperty("file_size");
														}
													}
													catiaDocAttributes = dataSet.getNamedRefComponent("catia_doc_attributes");
													tcForm = (TCComponentForm) catiaDocAttributes;
													if(tcForm != null){
														tcFormName = tcForm.getProperty("object_name");
													}
												} catch (Exception e){
													orginFileSize = "0";
													orginName = "The referenced file does not exist.";
												}	
												if(!releaseStatus.equals("")){
													continue;
												}
												int orginNameGapSize = 0;
												int datasetNameGapSize = 0;		
												String datasetNameLength = null;
												String orginNameLength = null;
												String datasetNm = null;
												String orginNm = null;
												try{
													orginNameGapSize = 54 - orginName.length();
													datasetNameGapSize = 35-dataSetName.length();
													datasetNameLength = String.valueOf(datasetNameGapSize);
													orginNameLength = String.valueOf(orginNameGapSize);
													datasetNm = String.format("%-"+datasetNameLength+"s", dataSetName);
													orginNm = String.format("%-"+orginNameLength+"s", orginName);
												}catch(Exception e){
													System.out.println(e);
													datasetNm = dataSetName;
													orginNm = orginName;
												}
												String resultCheck = "Saved";
												if(namedRef == null){
													resultCheck = "Unsaved";
													unSavedList.append(datasetNm + "  |  " + orginNm + " | " + resultCheck + "\r\n");
													waitProgress.setStatus("    " + datasetNm + "  |  " + orginNm + "  |  " + resultCheck + ", (" + orginFileSize + ")" + "  |  Relation : "+relation);
													continue;
												}
												
												String tdatasetname = dataSetName.replace("/", "_");
												if(tdatasetname.indexOf("-") > 0){
													tdatasetname = tdatasetname.substring(0, tdatasetname.indexOf("-"));
												}
												if(!orginName.startsWith(tdatasetname)){
													resultCheck = "Unsaved";
//													unSavedList.append("    " +datasetNm + "  |  " + orginNm  + " | " + resultCheck  + "\r\n");
													unSavedList.append("    " +datasetNm + "  |  " + orginNm  + " | " + "Dataset ���Ӱ� ���� ���� ������ ��ġ���� �ʽ��ϴ�.  " + " | " + resultCheck + "\r\n");
												}
												/*
												 * 20221018 ������ īƼ�� ���Ӱ� ���� ������� īƼ�� ���忩�θ� �Ǵ� �Ͽ��µ� catia_doc_attributes�� ���� �����ͼ��� ������ �ȵǾ�� ���忩�θ� �Ǵ� �� �� ��� �߰� �Ͽ���
												 * �߰� ����
												 * 1. �����ͼ¿� catia_doc_attributes�� ���°�� ���� ���Ѱ����� �Ǵ�
												 * 2. �����ͼ¿� catia_doc_attributes�� �ִµ� �����ͼ°� Name�� �ٸ���� ���� ���Ѱ����� �Ǵ�
												 */
												if(tcFormName != null){
													if(!dataSetName.equals(tcFormName)){
														resultCheck = "Unsaved";
														unSavedList.append("    " +datasetNm + "  |  " + tcFormName  + "                      | " +  "Dataset ���Ӱ� CATIA �Ӽ� Form ������ ��ġ���� �ʽ��ϴ�.  " + " | "  + resultCheck + "\r\n");												
													}
												}
												
												if(tcForm == null){
													resultCheck = "Unsaved";
													unSavedList.append("    " +datasetNm + "  |  " + orginNm  + " | "  + resultCheck + "\r\n");
												} 
												
												if( orginName == null || orginName.equals("") || orginFileSize.equals("0")){
													resultCheck = "Unsaved";
													unSavedList.append("    " +datasetNm + "  |  " + orginNm  + " | " + resultCheck  + "\r\n");
												}
												
												
												if(!relation.equals("Specifications")){
					//								String dataSetRev = dataSet.getProperty("object_name").substring(dataSet.getProperty("object_name").indexOf("/")+1);
													String partRevID = partRevision.getProperty("item_revision_id");
													if(releaseStatus.equals("") && relation.equals("References")){
														TCComponent[] dataSetList = {dataSet}; 
														partRevision.changeRelation("IMAN_reference", "IMAN_specification", dataSetList);
														changeRelation.append("    " +datasetNm + " Change Relation : References -> Specifications \r\n");
													}
												}
												waitProgress.setStatus("    " + datasetNm + "  |  " + orginNm + "  |  " + resultCheck + ", (" + orginFileSize + ")" + " | Relation : "+relation); 
											}
										}
									}else{
										waitProgress.setStatus("    " + partID + " Dataset null...");
										continue;
									}
									if(doubleCheck.contains("CATProduct") && doubleCheck.contains("CATPart")){
										catDoubleCheck.append("    " +partID  + "CATProduct, CATPart exist together... \r\n");
										doubleCheck.clear();
									}
								}
							}else {
								waitProgress.setStatus(pname + " Start...");
								waitProgress.setStatus("    Target does not exist.");
							}
							
							waitProgress.setStatus(pname + " End...");
							
							
							if(tCount == 0){
								waitProgress.close("Target does not exist.",false,false);
							} 
//							else if((unSavedList == null || unSavedList.toString().equals("")) && (zeroSizeList == null || zeroSizeList.toString().equals(""))){
//								waitProgress.setStatus("");
//								if(changeRelation != null && !changeRelation.toString().equals("")){
//									waitProgress.setStatus("Relation Change References -> Specifications List Start...");
//									waitProgress.setStatus(changeRelation.toString(), false);
//									waitProgress.setStatus("Relation Change References -> Specifications List End...\r\n");
//								}
//								if(catDoubleCheck != null && !catDoubleCheck.toString().equals("")){
//									waitProgress.setStatus("Dataset Double Check List Start...");
//									waitProgress.setStatus(catDoubleCheck.toString(), false);
//									waitProgress.setStatus("Dataset Double Check List End...\r\n");
//								}
//								waitProgress.close("All datasets have been saved.",false,false);
//							}
							else {
								waitProgress.setStatus("");
								waitProgress.setStatus("====================================================================================");
								waitProgress.setStatus("[Summary]");
							
								if(changeRelation != null && !changeRelation.toString().equals("")){
									waitProgress.setStatus("Relation Change List Start...");
									waitProgress.setStatus(changeRelation.toString(), false);
									waitProgress.setStatus("Relation Change List End...\r\n");
								}else if(changeRelation.length() == 0){
									waitProgress.setStatus("Relation Check : OK \r\n");
								}
								if(catDoubleCheck != null && !catDoubleCheck.toString().equals("")){
									waitProgress.setStatus("Dataset Double Check List Start...");
									waitProgress.setStatus(catDoubleCheck.toString(), false);
									waitProgress.setStatus("Dataset Double Check List End...\r\n");
								}else if(catDoubleCheck.length() == 0){
									waitProgress.setStatus("Dataset Double Check : OK \r\n");
								}
								if(unSavedList != null && !unSavedList.toString().equals("")){
									waitProgress.setStatus("Dataset Unsaved List Start...");
									waitProgress.setStatus(unSavedList.toString(), false);
									waitProgress.setStatus("Dataset Unsaved List End... \r\n");
								}else if(unSavedList.length() == 0){
									waitProgress.setStatus("Dataset Unsaved Check : OK \r\n");
								}
								if(unSavedList.length() == 0){
									waitProgress.close("All datasets have been saved.",false,false);
								}else{
									waitProgress.close("Dataset Unsaved or Exist Zero Size ",true,false);
								}
							}
							//���� ����ϴ� DatasetCheck���� Ĺ��Ʈ,Ĺ���δ�Ʈ �ߺ�üũ�� Relation ���� �߰��� ��� ���ϰ� ��
//								ds.put("PUID", targetComponent.getUid());
//								result = (ArrayList)remote.execute("com.kgm.service.ECOHistoryService", "datasetCheck", ds);
//								if(result != null && result.size() > 0){
//									pname = (String)((HashMap)result.get(0)).get("PNAME");
//									waitProgress.setStatus(pname + " Start...");
//									for( int i = 0; i < result.size(); i++){
//										tCount++;
//										HashMap row = (HashMap)result.get(i);
//										String datasetName = (String)row.get("DATASETNAME");
//										String originName = (String)row.get("ORIGINNAME");
//										String filePath = (String)row.get("FILEPATH");
//										String puid = (String)row.get("PUID");
//										
//										String tdatasetname = datasetName.replace("/", "_");
//										if(tdatasetname.indexOf("-") > 0){
//											tdatasetname = tdatasetname.substring(0, tdatasetname.indexOf("-"));
//										}
//										
//										TCComponentDataset dataset = (TCComponentDataset)session.stringToComponent(puid);
//										long size = getFileSize(dataset, downPath);
//										
//										String resultCheck = "Saved";
//										if(!originName.startsWith(tdatasetname)){
//											resultCheck = "Unsaved";
//											unSavedList.append("(" + pname + ") " + datasetName + "  :  " + originName + " >> " + resultCheck + "\r\n");
//										}
//										
//										if(size == 0){
//											zeroSizeList.append("(" + pname + ") " + datasetName + "  :  " + originName + " >> Size 0 byte\r\n");
//										}
//										
//										
//										waitProgress.setStatus("    " + datasetName + "  :  " + originName + " >> " + resultCheck + ", (" + size + " byte)");
//	//									waitProgress.setStatus("    datasetName : " + datasetName + ", originName : " + originName);
//										
//									}
//									
//									
//								} else {
//									AIFComponentContext[] referenced = ((TCComponentTask)targetComponent).whereReferenced();
//									TCComponentProcess process = null;
//									for(AIFComponentContext reference : referenced){
//										if (reference.getComponent() instanceof TCComponentProcess) {
//											process = (TCComponentProcess)reference.getComponent();
//											pname = process.toString();
//										}
//									}
//									waitProgress.setStatus(pname + " Start...");
//									waitProgress.setStatus("    Target does not exist.");
//								}
//								
//								waitProgress.setStatus(pname + " End...");
//							
//							if(tCount == 0){
//								waitProgress.close("Target does not exist.",false,false);
//							} else if((unSavedList == null || unSavedList.toString().equals("")) && (zeroSizeList == null || zeroSizeList.toString().equals(""))){
//								waitProgress.close("All datasets have been saved.",false,false);
//							} else {
//								waitProgress.setStatus("");
//								waitProgress.setStatus("[Summary]");
//								if(unSavedList != null && !unSavedList.toString().equals("")){
////									waitProgress.setStatus("");
//									waitProgress.setStatus("Dataset Unsaved List Start...");
//									waitProgress.setStatus(unSavedList.toString(), false);
//									waitProgress.setStatus("Dataset Unsaved List End...");
//								}
//								if(zeroSizeList != null && !zeroSizeList.toString().equals("")){
////									waitProgress.setStatus("");
//									waitProgress.setStatus("Dataset Zero Size List Start...");
//									waitProgress.setStatus(zeroSizeList.toString(), false);
//									waitProgress.setStatus("Dataset Zero Size List End...");
//								}
//								waitProgress.close("Dataset Unsaved or Exist Zero Size",true,false);
//							}
							
						} catch (Exception e) {
							e.printStackTrace();
							waitProgress.setStatus(e.getMessage() + "Error �����ڿ��� �����Ͽ� �ֽʽÿ�.");
//							waitProgress.setShowButton(true);
							waitProgress.close("Error",true,false);
						}
					}
				};
				
				session.queueOperation(operation);
//				AIFDesktop activeDesktop = AIFUtility.getActiveDesktop();
//				ChangeDatasetDialog dialog = new ChangeDatasetDialog(activeDesktop);
//				dialog.setModal(false);
//				setRunnable(dialog);
			}
		} catch (Exception e) {
			e.printStackTrace();
			MessageBox.post(e.getMessage(), "Error", MessageBox.ERROR);
		}
	}
	
	private long getFileSize(TCComponentDataset dataset, String downPath) throws Exception{
		TCComponentTcFile[] imanFile = dataset.getTcFiles();
		String dType = dataset.getType();
		String fRef = "";
		long fileSize = 0;
		for (int k = 0; k < imanFile.length; k++) {
			fileSize = 0;
			fRef = SYMTcUtil.getNamedRefType(dataset, imanFile[k]);
			
			
			
            if("CATDrawing".equals(dType)) {//�����ͼ� Ÿ���� ������ true            
                if("catdrawing".equals(fRef)) {//Ĺ����� �̸� true
                	try{
                		File[] file = dataset.getFiles(fRef, downPath);
                		for (File fil : file){
                			if(fil.isFile()){
                				fileSize = fil.length();
                			}
                			fil.delete();
                		}
                		
                	} catch (Exception e){
                	}
                }
            } else if("CATPart".equals(dType)) {
                if("catpart".equals(fRef)) {
                	try{
                		File[] file = dataset.getFiles(fRef, downPath);
                		for (File fil : file){
                			if(fil.isFile()){
                				fileSize = fil.length();
                			}
                			fil.delete();
                		}
                		
                	} catch (Exception e){
                	}
                }
            } else if("CATProduct".equals(dType)) {
                if("catproduct".equals(fRef)) {
                	try{
                		File[] file = dataset.getFiles(fRef, downPath);
                		for (File fil : file){
                			if(fil.isFile()){
                				fileSize = fil.length();
                			}
                			fil.delete();
                		}
                		
                	} catch (Exception e){
                	}
                }
            }
        }
		return fileSize;
	}

	/**
	 * Function Master�� �����ϴ�. �ֽ� �������̰� Working ����
	 * 
	 * @return
	 * @throws Exception
	 */
	private boolean validationCheck(InterfaceAIFComponent[] targetComponents) throws Exception {
		// 1. ��� ���� �Ǿ�����.
		// 2. ����� Perform Signoff Task ����
		
		if (targetComponents.length == 0) {
			MessageBox.post("����� �����ϼ���.", "Warning", MessageBox.INFORMATION); 
			return false;
		}
		
		for(InterfaceAIFComponent targetComponent : targetComponents){
			if (!(targetComponent instanceof TCComponentTask)) {
				MessageBox.post("Please select a Perform Signoff Task.", "Warning", MessageBox.INFORMATION);
				return false;
			}
		}

		return true;
	}

}
