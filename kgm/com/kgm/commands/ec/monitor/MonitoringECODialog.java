package com.kgm.commands.ec.monitor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import com.kgm.commands.ec.SYMCECConstant;
import com.kgm.commands.ec.dao.CustomECODao;
import com.kgm.commands.ec.report.ReportEPLCommand;
import com.kgm.common.SYMCDateTimeButton;
import com.kgm.common.SYMCLOVCombo;
import com.kgm.common.SortListenerFactory;
import com.kgm.common.WaitProgressBar;
import com.kgm.common.dialog.SYMCAbstractDialog;
import com.kgm.common.utils.CustomUtil;
import com.kgm.common.utils.ProcessUtil;
import com.kgm.common.utils.SYMTcUtil;
import com.kgm.dto.SYMCECOStatusData;
import com.teamcenter.rac.aif.kernel.InterfaceAIFComponent;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.ui.services.NavigatorOpenService;
import com.teamcenter.rac.util.MessageBox;

public class MonitoringECODialog extends SYMCAbstractDialog {

	/**
	 * [SR140923-023][20140923][jclee] ECI를 Vision NET으로 전환.
	 * [SR141106-036][2014.11.19][jclee] ECO Report Multi Download 기능 추가
	 */
	private TCSession session;
	// [SR140923-023][20140923][jclee] ECR No 검색조건 추가
	private Text eciNo, ecrNo, ecoNo;
	@SuppressWarnings("unused")
    private SYMCLOVCombo eciStatus, ecoStatus;
	private Text owningTeam, owningUser;
	private SYMCDateTimeButton releaseDateFrom, releaseDateTo;
	private int year, month, day;

	private Button searchButton;

	private Table resultTable;
	// [SR140923-023][20140923][jclee] ECI No -> ECI Approval No로 변경 및 ECI Status 컬럼 제거.
//	private String[] columnName = new String[] { "ECR No.", "ECI No.", "ECI Status", "ECO No.", "ECO Status", "Owning Dept.", "Owning User", "Creation Date",
//			"Submit Date", "Realse Date" };
	private String[] columnName = new String[] { "ECR No.", "ECI Approval No.", "ECO No.", "ECO Status", "Owning Dept.", "Owning User", "Creation Date",
			"Submit Date", "Realse Date" };
//	private int[] columnSize = new int[] { 80, 80, 80, 80, 80, 160, 120, 120, 100, 100, 100 };
	private int[] columnSize = new int[] { 80, 80, 80, 80, 160, 120, 120, 100, 100, 100 };

	private Button closeButton;
	private Button downloadButton;
	private Button downloadECOReport;

	private WaitProgressBar waitProgress;
	private String listFileName;
	private String logPath;

	public MonitoringECODialog(Shell parent, int _selection) {
		/**
		 * [SR140923-045][20141104][jclee] Modeless Dialog로 변환
		 */
//		super(parent, SWT.RESIZE | SWT.TITLE | SWT.PRIMARY_MODAL | SWT.DIALOG_TRIM | _selection);
		super(parent, SWT.RESIZE | SWT.TITLE | SWT.MODELESS | SWT.DIALOG_TRIM | _selection);
		setShellStyle(SWT.RESIZE | SWT.TITLE | SWT.MODELESS | SWT.DIALOG_TRIM | _selection);
		
		setBlockOnOpen(false);
		
		this.session = CustomUtil.getTCSession();
	}

	@Override
	protected boolean apply() {
		return false;
	}

	/** 버튼 변경 */
	protected void createButtonsForButtonBar(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		closeButton = new Button(composite, SWT.PUSH);
		closeButton.setText("Close");
		closeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getShell().close();
			}
		});
	}

	/** Composiste 생성 */
	@Override
	protected Composite createDialogPanel(ScrolledComposite parentScrolledComposite) {
		getShell().setText("Monitoring ECO");
		Composite composite = new Composite(parentScrolledComposite, SWT.NONE);
		composite.setLayout(new GridLayout());

		createSearchComposite(composite);
		cteateSearchResultTable(composite);
		return composite;
	}

	/** 검색 조건 Composiste 생성 */
	private void createSearchComposite(Composite paramComposite) {
		Date today = new Date();
		SimpleDateFormat simpleDateformat = new SimpleDateFormat("yyyy");
		year = Integer.parseInt(simpleDateformat.format(today));
		simpleDateformat = new SimpleDateFormat("MM");
		month = Integer.parseInt(simpleDateformat.format(today))-1;
		simpleDateformat = new SimpleDateFormat("dd");
		day = Integer.parseInt(simpleDateformat.format(today));
		
		Composite composite = new Composite(paramComposite, SWT.NONE);
		GridLayout layout = new GridLayout(10, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		composite.setLayout(layout);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		composite.setLayoutData(gridData);

		// #1
		makeLabel(composite, "ECI Approval NO.", 110);
		eciNo = new Text(composite, SWT.BORDER);
		gridData = new GridData(120, SWT.DEFAULT);
		eciNo.setLayoutData(gridData);
		eciNo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					search();
				}
			}
		});

		// [SR140923-023][20140923][jclee] ECI Status 검색조건 제거
//		makeLabel(composite, "ECI Status", 110);
//		eciStatus = new SYMCLOVCombo(composite, "S7_ECI_MATURITY");
//		gridData = new GridData(100, SWT.DEFAULT);
//		eciStatus.setLayoutData(gridData);
		
		// [SR140923-023][20140923][jclee] ECR No 검색조건 추가
		makeLabel(composite, "ECR NO", 110);
		ecrNo = new Text(composite, SWT.BORDER);
		gridData = new GridData(120, SWT.DEFAULT);
		ecrNo.setLayoutData(gridData);
		ecrNo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					search();
				}
			}
		});

		makeLabel(composite, "ECO NO.", 80);
		ecoNo = new Text(composite, SWT.BORDER);
		gridData = new GridData(120, SWT.DEFAULT);
		ecoNo.setLayoutData(gridData);
		ecoNo.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					search();
				}
			}
		});

		makeLabel(composite, "ECO Status", 80);
		ecoStatus = new SYMCLOVCombo(composite, "S7_ECO_MATURITY");
		gridData = new GridData(100, SWT.DEFAULT);
		ecoStatus.setLayoutData(gridData);

		Label label = new Label(composite, SWT.RIGHT);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		label.setLayoutData(gridData);

		// 검색 버튼
		searchButton = new Button(composite, SWT.PUSH);
		searchButton.setText("Search");
		searchButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				search();
			}
		});

		// #2
		makeLabel(composite, "Owning Dept.", 110);
		owningTeam = new Text(composite, SWT.BORDER);
		gridData = new GridData(120, SWT.DEFAULT);
		owningTeam.setLayoutData(gridData);
		owningTeam.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					search();
				}
			}
		});

		makeLabel(composite, "Owning User", 110);
		owningUser = new Text(composite, SWT.BORDER);
		gridData = new GridData(120, SWT.DEFAULT);
		owningUser.setLayoutData(gridData);
		owningUser.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					search();
				}
			}
		});

		Label label2 = new Label(composite, SWT.RIGHT);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 5;
		label2.setLayoutData(gridData);

		// 다운 버튼
		downloadButton = new Button(composite, SWT.PUSH);
		downloadButton.setText("ECO Date Update");
		
		try {
			String roleName = session.getCurrentRole().getProperty("role_name");
			if(!roleName.equalsIgnoreCase("DBA")) {
				downloadButton.setEnabled(false);
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		}

		downloadButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				downloadButton.setEnabled(false);

				try {
					download();
				} catch (Exception exception) {
					exception.printStackTrace();
				} finally {
					downloadButton.setEnabled(true);
				}
			}
		});
		
		// #5 FIXED 2013.05.31, DJKIM, 승인일 기준 검색
		makeLabel(composite, "Completed Date", 110);
		releaseDateFrom = new SYMCDateTimeButton(composite);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		releaseDateFrom.setLayoutData(gridData);
		releaseDateFrom.setDate(year, month, day);

		Label label7 = new Label(composite, SWT.CENTER);
		label7.setText("~");
		gridData = new GridData(120, SWT.DEFAULT);
		label7.setLayoutData(gridData);

		releaseDateTo = new SYMCDateTimeButton(composite);
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		releaseDateTo.setLayoutData(gridData);
		releaseDateTo.setDate(year, month, day);

		Label label8 = new Label(composite, SWT.RIGHT);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 3;
		label8.setLayoutData(gridData);
		
		/**
		 * [SR141106-036][2014.11.19][jclee] ECO Report Multi Download 기능 추가
		 */
		// ECO Report 다운 버튼
		downloadECOReport = new Button(composite, SWT.PUSH);
		downloadECOReport.setText("ECO Report Download");
		
		try {
			String roleName = session.getCurrentRole().getProperty("role_name");
			if(!roleName.equalsIgnoreCase("DBA")) {
				downloadECOReport.setEnabled(false);
			}
		} catch (TCException e1) {
			e1.printStackTrace();
		}

		downloadECOReport.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				downloadECOReport.setEnabled(false);

				try {
					downloadECOReport();
				} catch (Exception exception) {
					exception.printStackTrace();
				} finally {
					downloadECOReport.setEnabled(true);
				}
			}
		});

		Label label9 = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 10;
		label9.setLayoutData(gridData);

		Label lSeparator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
		gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.horizontalSpan = 10;
		lSeparator.setLayoutData(gridData);

	}

	/** 검색 결과 테이블 생성 */
	private void cteateSearchResultTable(Composite paramComposite) {
		Composite composite = new Composite(paramComposite, SWT.NONE);
		composite.setLayout(new GridLayout());
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, false);
		composite.setLayoutData(layoutData);

		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
		layoutData.minimumHeight = 400;
		layoutData.horizontalSpan = 3;
		resultTable = new Table(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
		resultTable.setHeaderVisible(true);
		resultTable.setLinesVisible(true);
		resultTable.setLayoutData(layoutData);
		resultTable.addMouseListener(new MouseAdapter() {
			public void mouseDoubleClick(MouseEvent e) {
				TableItem[] selectItems = resultTable.getSelection();
				try {
					TCComponentItem item = CustomUtil.findItem(SYMCECConstant.ECOTYPE, (String) selectItems[0].getText(2));
					if (item != null) {
						NavigatorOpenService openService = new NavigatorOpenService();
						openService.open(item);
//						getShell().close();
					}
				} catch (TCException e1) {
					e1.printStackTrace();
				}
			}
		});

		int i = 0;
		for (String value : columnName) {
			TableColumn column = new TableColumn(resultTable, SWT.NONE);
			column.setText(value);
			column.setWidth(columnSize[i]);
			column.addListener(SWT.Selection, SortListenerFactory.getListener(SortListenerFactory.STRING_COMPARATOR));
			i++;
		}
	}

	private void makeLabel(Composite paramComposite, String lblName, int lblSize) {
		GridData layoutData = new GridData(lblSize, SWT.DEFAULT);

		Label label = new Label(paramComposite, SWT.RIGHT);
		label.setText(lblName);
		label.setLayoutData(layoutData);
	}

	/**
	 * 검색 실행
	 */
	private void search() {
		SYMCECOStatusData searchCondition = new SYMCECOStatusData();

		String owning_team = owningTeam.getText();
		if (owning_team != null && owning_team.length() > 0) {
			owning_team = owning_team.replace("*", "%");
			searchCondition.setOwningTeam(owning_team.toUpperCase() + '%');
		}

		String owning_user = owningUser.getText();
		if (owning_user != null && owning_user.length() > 0) {
			owning_user = owning_user.replace("*", "%");
			searchCondition.setOwningUser(owning_user.toUpperCase() + '%');
		}

		// [SR140923-023][20140923][jclee] ECI Status 검색조건 제거 
//		String eci_status = eciStatus.getText();
//		if (eci_status != null && eci_status.length() > 0) {
//			searchCondition.setEciStatus(eci_status);
//		}

		String eco_status = ecoStatus.getText();
		if (eco_status != null && eco_status.length() > 0) {
			searchCondition.setEcoStatus(eco_status);
		}

		String eci_no = eciNo.getText();
		if (eci_no != null && eci_no.length() > 0) {
			eci_no = eci_no.replace("*", "%");
			searchCondition.setEciNo(eci_no.toUpperCase() + '%');
		}
		
		// [SR140923-023][20140923][jclee] ECR No 검색조건 추가
		String ecr_no = ecrNo.getText();
		if (ecr_no != null && ecr_no.length() > 0) {
			ecr_no = ecr_no.replace("*", "%");
			searchCondition.setEcrNo(ecr_no.toUpperCase() + '%');
		}

		String eco_no = ecoNo.getText();
		if (eco_no != null && eco_no.length() > 0) {
			eco_no = eco_no.replace("*", "%");
			searchCondition.setEcoNo(eco_no.toUpperCase() + '%');
		}

		String releaseDateFromDate = releaseDateFrom.getYear() + "-" + String.format("%1$02d", (releaseDateFrom.getMonth() + 1)) + "-"
				+ String.format("%1$02d", releaseDateFrom.getDay()) + "";
		searchCondition.setReleaseDateFrom(releaseDateFromDate);

		String releaseDateToDate = releaseDateTo.getYear() + "-" + String.format("%1$02d", (releaseDateTo.getMonth() + 1)) + "-"
				+ String.format("%1$02d", releaseDateTo.getDay()) + "";
		searchCondition.setReleaseDateTo(releaseDateToDate);

		try {
			CustomECODao dao = new CustomECODao();
			ArrayList<SYMCECOStatusData> resultList = dao.searchEOStatus(searchCondition);

			if (resultList != null) {
				resultTable.removeAll();
				for (SYMCECOStatusData data : resultList) {
					TableItem item = new TableItem(resultTable, SWT.NONE);
					if (data.getEcrNo() != null)
						item.setText(0, data.getEcrNo());
					if (data.getEciNo() != null)
						item.setText(1, data.getEciNo());
					// [SR140923-023][20140923][jclee] ECI Status 컬럼 제거. 아래 컬럼들의 숫자도 하나씩 줄어들었음.
//					if (data.getEciStatus() != null)
//						item.setText(2, data.getEciStatus());
					if (data.getEcoNo() != null)
						item.setText(2, data.getEcoNo());
					if (data.getEcoStatus() != null)
						item.setText(3, data.getEcoStatus());
					if (data.getOwningTeam() != null)
						item.setText(4, data.getOwningTeam());
					if (data.getOwningUser() != null)
						item.setText(5, data.getOwningUser());
					if (data.getCreateDate() != null)
						item.setText(6, data.getCreateDate());
					if (data.getRequestDate() != null)
						item.setText(7, data.getRequestDate());
					if (data.getRealseDate() != null)
						item.setText(8, data.getRealseDate());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	protected boolean validationCheck() {
		return true;
	}

	/**
	 * ECO Dataset Files Download
	 * 
	 * @method download
	 * @date 2013. 4. 4.
	 * @param
	 * @return void
	 * @exception
	 * @throws
	 * @see
	 */
	private void download() {
		final TableItem[] checkItems = resultTable.getSelection();
		getShell().getDisplay().syncExec(new Runnable() {
			public void run() {
				try {
					if (checkItems == null || checkItems.length == 0) {
						return;
					}
					DirectoryDialog directoryDialog = new DirectoryDialog(getShell());
					String directoryPath = directoryDialog.open();
					if (directoryPath == null) {
						return;
					}
					waitProgress = new WaitProgressBar(AIFUtility.getActiveDesktop());
					waitProgress.start();
					waitProgress.setStatus("ECO Date Updating...", true);
					String ecos[] = new String[checkItems.length];
					for (int i = 0; i < checkItems.length; i++) {
						TableItem item = checkItems[i];
						if(item.getText(3).equalsIgnoreCase("Completed")) {
							ecos[i] = item.getText(2).trim();
						} else {
							MessageBox.post(getShell(), "You can update only the ECO status is 'Completed'.", "Notice", MessageBox.INFORMATION);
							return;
						}
					}

					//다운로드 시작
					logPath = directoryPath + "\\List_TCLog_" + getDateTime("yyyyMMddHHmmss") + ".txt";
					writeLog(logPath, "Download Start!");
					
					ArrayList<String> filterType = new ArrayList<String>();
					filterType.add("CATDrawing");
					HashMap<String, HashMap<String, HashMap<String, File[]>>> downloadInfo = SYMTcUtil.getEcosDwgDatasetFiles(ecos, directoryPath, filterType);

					writeLog(logPath, "Download End!");
					
					//다운 후처리
					if (downloadInfo != null && downloadInfo.size() > 0) {
						writeLog(logPath, "ECO Date Update Start!");
						HashMap<String, File> uploadTarget = checkDownloadedFile(directoryPath, downloadInfo);
						
						if(uploadTarget.size() > 0) {
							if (callCAA(directoryPath)) {
								// CAA 프로그램 프로세스가 정상적으로 종료된 후 CATDrawing파일을 Teamcenter의 해당 Dataset에 업로드하는 함수 호출
								upload(uploadTarget);
								String errorMessage = "ECO Date Update completed!";
								writeLog(logPath, errorMessage);
								MessageBox.post(getShell(), errorMessage, "Notification", MessageBox.INFORMATION);
							} else {
								String errorMessage = "ECO Date Update Failed!";
								writeLog(logPath, errorMessage);
								MessageBox.post(getShell(), errorMessage, "ERROR", MessageBox.ERROR);
							}
						} else {
							String errorMessage = "There are No objects to update";
							writeLog(logPath, errorMessage);
							MessageBox.post(getShell(), errorMessage, "Notification", MessageBox.INFORMATION);
						}
					} else {
						String errorMessage = "Download ERROR : Can't Find Target CATDrawing";
						writeLog(logPath, errorMessage);
						MessageBox.post(getShell(), errorMessage, "ERROR", MessageBox.ERROR);
					}
				} catch (Exception e) {
					e.printStackTrace();
					writeLog(logPath, e.toString());
					MessageBox.post(getShell(), e.getMessage(), "ERROR", MessageBox.ERROR);
				} finally {
					if (waitProgress != null) {
						waitProgress.close("Update compleated.", false);
						waitProgress.dispose();
					}
				}
			}
		});
	}

	/**
	 * download 한 파일 정합성 체크하는 함수
	 *  'ECO Date Update'(도면자동화)가 실행되지 않은 신규 리비전의 CATDrawing일 경우 
	 * List.txt파일에 다운받은 CATDrawing 파일의 위치 경로를 작성한다. 
	 * 그렇지 않을경우에는 다운받았던 CATDrawing 파일을 삭제한다.
	 * 
	 * @param directoryPath
	 *            String
	 * @param downloadInfo
	 *            HashMap<String, HashMap<String, File[]>>>
	 * @return 
	 * @throws Exception
	 */
	private HashMap<String, File> checkDownloadedFile(String directoryPath, HashMap<String, HashMap<String, HashMap<String, File[]>>> downloadInfo) throws Exception {
		
		//대상목록 파일생성
		listFileName = "List_" + logPath.substring(logPath.lastIndexOf("_")+1);
		
		FileWriter txtFileWriter = new FileWriter(directoryPath + "\\" + listFileName);
		BufferedWriter txtBufferedWriter = new BufferedWriter(txtFileWriter);
		Iterator<String> iteratorECOs = downloadInfo.keySet().iterator();
		HashMap<String, File> uploadInfoMap = new HashMap<String, File>();
		
		while (iteratorECOs.hasNext()) {
			String ecoID = (String) iteratorECOs.next();
			HashMap<String, HashMap<String, File[]>> hashMapItemRevisions = downloadInfo.get(ecoID);
			Iterator<String> iteratorItemRevisions = hashMapItemRevisions.keySet().iterator();

			while (iteratorItemRevisions.hasNext()) {
				String itemRevisionID = (String) iteratorItemRevisions.next();
				int seperatePoint = itemRevisionID.indexOf("/");
				String itemID = itemRevisionID.substring(0, seperatePoint);
				String revision = itemRevisionID.substring(seperatePoint + 1);

				HashMap<String, File[]> hashMapFiles = hashMapItemRevisions.get(itemRevisionID);
				Iterator<String> iteratorDatasets = hashMapFiles.keySet().iterator();

				while (iteratorDatasets.hasNext()) {
					String datasetFile = (String) iteratorDatasets.next();
					File[] files = hashMapFiles.get(datasetFile);
					if(files != null) {
						for (File file : files) {
							// ItemRevision ID와 동일한 이름의 PDF 파일이 첨부되어 있을 경우 List.txt파일에 CATDrawing 파일의 위치 경로 작성, 
							// 그렇지 않을경우 CATDrawing파일 삭제처리
							if (!checkPDF(itemID, revision)) {
								/* 이름변경부분 취소
								// 파일명 itemID_revision.CATDrawing 형태로 변경 후 List.txt파일에 저장
								// 예) 1234567890_000.CATDrawing
								String renamedFilePath = renameFile(file, itemRevisionID);
	
								if (renamedFilePath != null) {
									txtBufferedWriter.write(renamedFilePath);
									txtBufferedWriter.newLine();
									uploadInfoMap.put(itemRevisionID, new File(renamedFilePath));
									//System.out.println("ecoID : " + ecoID + "     itemRevisionID : " + itemRevisionID + "     datasetFile : " + datasetFile + ",    newfilePath :  " + renamedFilePath + ",     oldfileName : " + file.getName());
								} else {
									txtBufferedWriter.close();
									throw new Exception("Update Error");
								}
								**/
 								
								String filePath = file.getPath().toString();
								
								if (filePath != null) {
									txtBufferedWriter.write(filePath);
									txtBufferedWriter.newLine();
									uploadInfoMap.put(itemRevisionID, new File(filePath));
									//System.out.println("ecoID : " + ecoID + "     itemRevisionID : " + itemRevisionID + "     datasetFile : " + datasetFile + ",    newfilePath :  " + renamedFilePath + ",     oldfileName : " + file.getName());
								} else {
									txtBufferedWriter.close();
									String errorMessage = "Download Error";
									writeLog(logPath, errorMessage);
									throw new Exception(errorMessage);
								}
							} else {
								file.delete();
							}
						}	
					}
				}
			}
		}

		if (txtBufferedWriter != null) {
			txtBufferedWriter.close();
		}
		return uploadInfoMap;
	}

//	/**
//	 * 다운받은 CATDrawing파일의 이름을 변경하는 함수<br>
//	 * 예) itemID_revision (1234567890_000.CATDrawing)
//	 * @param targetFile
//	 *            File
//	 * @return boolean
//	 */
//	private String renameFile(File targetFile, String itemRevisionID) {
//		String filePath = targetFile.getPath().substring(0, targetFile.getPath().lastIndexOf("\\"));
//		String fileName = targetFile.getName();
//		String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);		
//		String newFileName = itemRevisionID.replace("/", "_") + "." + fileExt;;		//	예)1234567890_000
//		String newFileNamePath = filePath + "\\" + newFileName;
//
//		//다운 받은 파일명이 1234567890_000 규칙을 준수하고 있을 경우 그대로 리턴
//		if(fileName.equals(newFileName)) {
//			return targetFile.getPath();
//		} else {
//			//규칙을 준수하지 않는 파일명일경우  파일명을 교체하여 리턴
//			File newFile = new File(newFileNamePath);	
//			//동일한 이름의 파일이 또 있을겨우 삭제 후 진행
//			newFile.delete();
//			if (targetFile.renameTo(newFile)) {
//				return newFile.getPath().toString();
//			}
//		}
//		
//		return null;
//	}

	/**
	 * CAA프로그램을 호출하는 함수 CAA프로그램 작업 종료후 Teamcenter의 해당 Dataset에 파일을 업로드 하는 함수를
	 * 호출한다.
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings("unused")
    private boolean callCAA(String directoryPath) throws Exception {
		// [20140107][jclee] OTW9에서 OTW10으로 변경
//		String caaPath = "C:\\Siemens\\TC\\OTW10\\rac\\catiav5_integration\\bin\\CAA\\V5R21\\win_b64\\code\\bin\\CAADrawingTCBatch.exe";
		
		// [20140112_TC10 Upgrade] FMS_HOME으로부터 Path를 생성하도록 변경.
		String caaPath = "C:\\Siemens\\TC13\\portal\\catiav5_integration\\bin\\CAA\\V5R33\\win_b64\\code\\bin\\CAADrawingTCBatch.exe";
		String fmsHome = System.getenv("FMS_HOME");
		if(fmsHome != null && !fmsHome.isEmpty()) {
			caaPath = fmsHome.substring(0, fmsHome.lastIndexOf("\\")) + "\\portal\\catiav5_integration\\bin\\CAA\\V5R33\\win_b64\\code\\bin\\CAADrawingTCBatch.exe";		
		}
		
		File caaFile = new File(caaPath);

		if (caaFile.isFile()) {
			String batPath = createBatFile(caaPath, directoryPath);
			if (batPath != null) {
				try {
					String[] command = new String[] { batPath };
//					Process process = new ProcessBuilder(command).start();
					// 20130614 : 동작중 멈추는 현상 개선
					ProcessBuilder pb = new ProcessBuilder(command);
					pb.redirectErrorStream(true);
					Process process = pb.start();
					
					// 외부 프로그램 출력 읽기
					BufferedReader stdIn = new BufferedReader(new InputStreamReader(process.getInputStream()));
//					BufferedReader stdError = new BufferedReader(new InputStreamReader(process.getErrorStream()));
					String line;

					// "표준 입출력"과 "표준 에러 출력"을 출력
					while ((line = stdIn.readLine()) != null) {
//						System.out.println(line);
					}

//					while ((line = stdError.readLine()) != null) {
//						System.err.println(line);
//					}


					// 외부 프로그램 반환값 출력
					if (process.exitValue() != 0) {
//						System.out.println("Exit Code: " + process.exitValue());
						
						String errorMessage = "CAADrawingTCBatch.exe Program Error.";
						writeLog(logPath, errorMessage);
						MessageBox.post(getShell(), errorMessage, "Notification", MessageBox.ERROR);
						throw new Exception(errorMessage);
					}
					
					process.destroy();

					return true;
				} catch (Exception e) {
					writeLog(logPath, e.toString());
					MessageBox.post(getShell(), e.toString(), "Notification", MessageBox.ERROR);
				}
			}
		} else {
			String errorMessage = "No 'CAADrawingTCBatch.exe' Program.";
			writeLog(logPath, errorMessage);
			MessageBox.post(getShell(), errorMessage, "Notification", MessageBox.ERROR);
			throw new Exception(errorMessage);
		}
		
		return false;
	}

	/**
	 * CAA프로그램을 호출하는 배치 파일 생성 함수<br>
	 * 생성 위치는 C:\Temp\CAACall.bat
	 * @param directoryPath 
	 * 			  String
	 * @param caaPath
	 *            String
	 * @return batFilePath String
	 * @throws IOException
	 */
	private String createBatFile(String caaFilePath, String directoryPath) throws IOException {
		String batFileParent = "C:\\Temp";
		String batFilePath = batFileParent + "\\CAACall.bat";
		
		File folder = new File(batFileParent);
		if(!folder.isDirectory()) {
			folder.mkdirs();
		}		
		
		FileWriter fileWriter = new FileWriter(batFilePath);
		BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
		bufferedWriter.write("@echo off");
		bufferedWriter.newLine();
		bufferedWriter.write("set PATH=C:\\DS\\B33\\win_b64\\code\\bin;%PATH%");
		bufferedWriter.newLine();
		bufferedWriter.write(caaFilePath + " " + "\""+directoryPath + "\\" + listFileName+ "\"" +" -pdf");

		if (bufferedWriter != null) {
			bufferedWriter.close();
		}

		File file = new File(batFilePath);
		if (file.isFile()) {
			return batFilePath;
		}
		return null;
	}

	
	/**
	 * 새로 생성된 Drawing파일 및 PDF파일을 Dataset에 첨부하는 함수 Drawing파일은 기존 Dataset에
	 * namedReference를 교체한다. PDF파일은 신규 Dataset을 생성하여 첨부한다.
	 * 
	 * @param HashMap<String, String> uploadFileInfo
	 * @exception
	 */
	private void upload(HashMap<String, File> uploadFileInfo) throws Exception {
		
		Iterator<String> iterator = uploadFileInfo.keySet().iterator();
		while(iterator.hasNext()) {
			String itemRevisionID = (String) iterator.next();
			
			File uploadFile = uploadFileInfo.get(itemRevisionID);
			if(uploadFile.isFile()) {
//				Vector<File> uploadFiles = new Vector<File>();
//				uploadFiles.addElement(uploadFile);
				
				String itemID = itemRevisionID.substring(0, itemRevisionID.indexOf("/"));
				String revision = itemRevisionID.substring(itemRevisionID.indexOf("/")+1);
				String datasetName = "";
				
				TCComponentItemRevision itemRevision = CustomUtil.findItemRevision("ItemRevision", itemID, revision);
				ArrayList<TCComponent> datasets = ProcessUtil.getDatasets(itemRevision, "IMAN_specification");

				// NamedReferenced 첨부파일 교체
				for (TCComponent component : datasets) {
					if (component instanceof TCComponentDataset) {
						TCComponentDataset dataset = (TCComponentDataset) component;
						String namedRef = CustomUtil.getNamedRefType(dataset, "CATDrawing");
						if (namedRef.length() > 0 && !namedRef.equals("Text")) {
							/*	 
							 * 삭세후 업로드 작업시 미세한 시차로 인하여 그사이에 수정 불능에 걸려, 삭제만 되고 업로드 실패가 발생하였음.  
							 * 때문에 이 방식은 주석으로 막고 아래와 같이 replaceFiles 함수로 대체 사용함.
							 * SYMTcUtil.removeAllNamedReference(dataset);
							 * SYMTcUtil.importFiles(dataset, uploadFiles);								
							 */
							try {
								File file = new File(uploadFile.getPath());
								dataset.replaceFiles(new File[] { file }, new String[] { uploadFile.getName() }, new String[] { "BINARY" }, new String[] { namedRef });
								// dataset.replaceFiles(File newFile[], String existingNamedReferencedFileName[], String TicketFileType[], String referenceTypeName[]);
							} catch (Exception e) {
								writeLog(logPath, e.toString());
							} finally {
								datasetName = dataset.toDisplayString();			
							}							
						}
					}
				}
				
				// PDF 파일 Dataset 첨부		
				if(datasetName.length() > 0) {
					ArrayList<File> targetPDFs = makePDFFileList(uploadFile);
					int count = targetPDFs.size();
					if(count > 0) {
						for(int i=0; i<count; i++) {
							String PDFDatasetName = "";
							
							if(count != 1) {
								//PDF 파일이 한개일 경우 DataSet 이름은 CATDrawing DataSet명과 동일
								//PDF 파일이 여러개일 경우 DataSet 이름은 CATDrawing DataSet명_sheet명으로 변경
								
								String PDFFileName = targetPDFs.get(i).getName();
								PDFFileName = PDFFileName.substring(0, PDFFileName.lastIndexOf("."));		
								String[] results = PDFFileName.split("_");
								int num = results.length;		

								PDFDatasetName = datasetName + "-" + results[num-2] + "_" +results[num-1];
							} else {
								PDFDatasetName = datasetName;
							}
							
							TCComponentDataset dataset = CustomUtil.createPasteDataset(itemRevision, PDFDatasetName, null, "PDF", "IMAN_specification");
							String namedRef = CustomUtil.getNamedRefType(dataset, "pdf");
							dataset.setFiles(new String[] { targetPDFs.get(i).getPath() }, new String[] { "PDF" }, new String[] { "Plain" }, new String[] { namedRef });														
						}
					} else {
						writeLog(logPath, uploadFile.getName() + " : The converted PDF file does not exist.");
					}					
				} else {
					writeLog(logPath,  uploadFile.getName() + " : Can't make PDF DataSet Name.");
				}
			}
		}
	}
	
	/**
	 * Teamcenter에 업로드 할 PDF파일 목록을 배열로 리턴하는 함수<br>
	 * PDF 파일이 하나일때 : 그대로 배열에 담는다(Teamcenter에 업로드한다).
	 * PDF 파일이 여러개일때 : 대소문자 구분 없이 파일명에 sheet가 포함된 PDF만 배열에 담는다(Teamcenter에 업로드한다).
	 * 
	 * @param catDrawingFile
	 *            File
	 * @return  ArrayList<File>
	 */
	private static ArrayList<File> makePDFFileList(File catDrawingFile) {
		String fileName = catDrawingFile.getName().toLowerCase();
		final String pattern = fileName.substring(0, fileName.lastIndexOf("."));
		String folderPath = catDrawingFile.getParent();
		
		File targetFolder = new File(folderPath);
		File[] pdfFiles = targetFolder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				if(name.toLowerCase().indexOf(pattern) != -1) {
					return name.endsWith(".pdf");			
				} else {
					return false;
				}
			}
		});		
		
		ArrayList<File> arrPDFList = new ArrayList<File>();
		if(pdfFiles.length == 1) {
			arrPDFList.add(pdfFiles[0]);
		} else if (pdfFiles.length > 1) {
			for(int i=0; i<pdfFiles.length; i++) {
				if(pdfFiles[i].getName().toLowerCase().indexOf("sheet") != -1) {
					arrPDFList.add(pdfFiles[i]);
				}
			}
		} else {
			//PDF가 없을때
		}
				
		return arrPDFList;		
	}	

	/**
	 * ItemRevision에 첨부된 Dataset 중 PDF가 있는지 확인하는 함수<br>
	 * ItemRevision ID와 동일한 이름의 PDF 파일이 있을 경우 'ECO Date Update'(도면자동화)가 끝난 아이템으로
	 * 판별한다. Usage: checkPDF("1234567890","000")
	 * 
	 * @param itemID
	 *            String
	 * @param revision
	 *            String
	 * @return boolean 동일한 이름의 pdf 파일이 있으면 true, 없으면 false를 return 한다.
	 * @throws Exception
	 * 
	 */
	private boolean checkPDF(String itemID, String revision) throws Exception {

		TCComponentItemRevision itemRevision = CustomUtil.findItemRevision("ItemRevision", itemID, revision);
		ArrayList<TCComponent> datasets = ProcessUtil.getDatasets(itemRevision, "IMAN_specification");

		return checkPDF(itemRevision, datasets);
	}

	/**
	 * ItemRevision에 첨부된 Dataset 중 PDF가 있는지 확인하는 함수<br>
	 * ItemRevision ID와 동일한 이름의 PDF 파일이 있을 경우 'ECO Date Update'(도면자동화)가 끝난 아이템으로
	 * 판별한다. (ItemRevision ID가 포함된 이름의 파일 있는 경우로 변경)
	 * 
	 * @param itemRevision
	 *            TCComponentItemRevision
	 * @param datasetComponents
	 *            ArrayList <TCComponent>
	 * @return boolean 동일한 이름의 pdf 파일이 있으면 true, 없으면 false를 return 한다.
	 * @throws Exception
	 */
	private boolean checkPDF(TCComponentItemRevision itemRevision, ArrayList<TCComponent> datasetComponents) throws Exception {

		String itemID = itemRevision.getProperty("item_id");
//		String revision = itemRevision.getProperty("item_revision_id");
//		String targetPDF = itemID + "_" + revision + ".pdf";
		for (TCComponent component : datasetComponents) {
			if (component instanceof TCComponentDataset) {
				TCComponentDataset dataset = (TCComponentDataset) component;
				String[] fileNames = dataset.getFileNames("PDF_Reference");
				if (fileNames.length > 0) {
					for (String fileName : fileNames) {
//						if (fileName.equals(targetPDF)) {
						if (fileName.indexOf(itemID) != -1) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	
	/**
	 * log 파일 생성 함수
	 * 
	 * @param String message            
	 * @throws Exception
	*/
	public void writeLog(String logPath, String message) {
		String logMessage = getDateTime("yyyy-MM-dd HH:mm:ss")+ " - " + message;
		PrintWriter pw = null;
		
		try {
			pw = new PrintWriter(new BufferedWriter(new FileWriter(logPath, true)));
			pw.println(logMessage);
			System.out.println(logMessage);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if(pw != null) {
				pw.close();
			}
		}
	}
	
	/**
	 * [SR141106-036][2014.11.19][jclee] ECO Report Multi Download 기능 추가
	 * ECO Report Download
	 */
	private void downloadECOReport() {
		ArrayList<InterfaceAIFComponent[]> aryTargetComponents = getTargetComponents();
		
		if (aryTargetComponents == null || aryTargetComponents.size() == 0) {
			return;
		}
		
		for (int inx = 0; inx < aryTargetComponents.size(); inx++) {
			InterfaceAIFComponent[] targetComponents = new InterfaceAIFComponent[1];
			targetComponents = aryTargetComponents.get(inx);
			new ReportEPLCommand(targetComponents);
		}
	}
	
	/**
	 * 선택한 ECO를 InterfaceAIFComponent 배열 ArrayList로 변환하여 반환
	 */
	private ArrayList<InterfaceAIFComponent[]> getTargetComponents() {
		try {
			ArrayList<InterfaceAIFComponent[]> results = new ArrayList<InterfaceAIFComponent[]>();
			
			// 결과 Table에서 선택한 목록
			TableItem[] checkItems = resultTable.getSelection();
			
			if (checkItems == null || checkItems.length == 0) {
				return null;
			}
			
			// 선택한 ECO No 추출
			for (int inx = 0; inx < checkItems.length; inx++) {
				TableItem item = checkItems[inx];
				String sEcoNo = item.getText(2).trim();
				InterfaceAIFComponent[] aifComponent = new InterfaceAIFComponent[1];
				
				TCComponentItemRevision irECO = SYMTcUtil.findItemRevision(session, sEcoNo, "000");
				
				if (irECO == null) {
					continue;
				}
				
				aifComponent[0] = irECO;
				
				results.add(aifComponent);
			}
			
			return results;
		} catch (Exception e) {
			MessageBox.post(e);
		}
		
		return null;
	}
	
	/**
	 * 년월일시간 생성 함수
	 * 
	 * @param String dateFormat 			  
	 * @return String time
	 * 
	*/
	public String getDateTime(String dateFormat) {
		SimpleDateFormat smpldateFormat = new SimpleDateFormat(dateFormat);
		return smpldateFormat.format(new java.util.Date());
	}
}