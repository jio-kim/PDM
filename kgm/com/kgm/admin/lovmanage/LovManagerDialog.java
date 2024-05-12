/**
 * LovManagerDialog.java
 * 
 * 1. bmide_manage_batch_lovs.bat Utility를 사용하여 External Lov List XML로 추출합니다.
 * 2. 추출된 Xml을 Loading하여 화면에 표시 합니다.
 * 3. 각 LOV Data 생성/수정/삭제 기능을 구현합니다.
 * 4. 생성/수정/삭제 된 LOV Data를 Xml로 변환합니다.
 * 5. 변환된 Xml을 bmide_manage_batch_lovs.bat Utility를 사용하여 TeamCenter에 반영합니다.
 */
package com.kgm.admin.lovmanage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import jxl.SheetSettings;
import jxl.Workbook;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.PageOrientation;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.kgm.common.WaitProgressBar;
import com.teamcenter.rac.aif.AIFDesktop;
import com.teamcenter.rac.aif.AIFShell;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.AbstractAIFOperation;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;

/**
 * [20140422][SR140401-044] bskwak, column 명 클릭 시 정렬 기능 추가.
 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
 * 
 * @author bs
 * 
 * @param <maxSeq>
 */
@SuppressWarnings({ "serial", "unused", "rawtypes", "unchecked" })
public class LovManagerDialog<maxSeq> extends AbstractAIFDialog
{
	/** TeamCenter Session */
	public TCSession session = null;
	protected static final String String = null;
	/** Lov List */
	ArrayList<LovItem> lovList = null;
	/** Lov Data List */
	ArrayList<LovDataItem> dataList = null;
	/** Lov Table */
	JTable lovTable = null; // LOV List를 저장할 Table
	/** Lov Data Table */
	JTable dataTable = null; // LOV의 Data를 저장할 리스트
	/** Lov Table Model */
	LovItemTableModel lovItemTableModel = null; // LOV List
	/** Lov Data Table Model */
	LovDataItemTableModel lovDataItemTableModel = null; // LOV의 Data List

	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document document;
	private Document document_lang;
	private String lovName = "";

	/**
	 * LOV 값을 BMIDE에서 생성하지 않고 Reference(?) 형태로 관리하기 위한 부분.
	 * SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * call bmide_manage_batch_lovs.bat -u=infodba -p=infodba -g=dba -option=extract -file=C:/temp/lovname20130326/lov_values_201303261041.xml
	 * 위와 같이 환경Setting을 한 후 bmide_manage_batch_lovs.bat 을 실행하면 지정한 경로에 xml파일이 생성(파일명.xml과 lang/파일명_lang.xml)되고
	 * 이 xml파일안에 LOV의 List와 Value가 있슴.
	 * 이 Class는 이러한 LOV에 실제 값을 추가, 수정, 삭제하고 반영하기 위한 Class임.
	 * LOV 자체를 생성하지는 못함.
	 */

	/**
	 * SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * 위 3개의 라인은 C:\Tc9.properties.txt 파일에 반드시 기록되어 있어야 한다.
	 */

	public static String TOP_NODE_ATTR_NAME_XMLNS = "xmlns";
	public static String TOP_NODE_ATTR_NAME_VERSION = "batchXSDVersion";

	/** xml 파일 상단의 기본 정의를 위한 변수 **/
	private String topNodeAttrXmlns = ""; // xmlns="http://teamcenter.com/BusinessModel/TcBusinessData"
	private String topNodeAttrVersion = ""; // batchXSDVersion="1.0"

	/** lang/_lang.xml 파일 상단의 기본 정의를 위한 변수 **/
	private String topNodeAttrXmlns_lang = ""; // xmlns="http://teamcenter.com/BusinessModel/TcBusinessDataLocalization"
	private String topNodeAttrVersion_lang = ""; // batchXSDVersion="1.0"

	/** xml파일을 내려받기 위한 경로 **/
	private String xmlDownloadPath = "C:/Temp/lovname"; // ex) C:\Temp\lovname20130326				 
	private String xmlDownloadPath_lang = "C:/Temp/lovname"; // ex) C:\Temp\lovname20130326\lang

	/** 내려받을 xml파일명 **/
	private String oldXMLFile = "lov_values"; // BMIDE에서 export 받은 xml 파일명(lov_values_20130101)
	private String oldXMLFile_lang = ""; // BMIDE에서 export 받은 xml Lang 파일명(lov_values_20130101_lang)

	/** Import할 xml파일 명(생성, 수정, 삭제등의 변경사항 적용) **/
	private String newXMLFile = ""; // BMIDE로 import 할 xml 파일명 (XXX_20130101)
	private String newXMLFile_lang = ""; // BMIDE로 import 할 xml Lang파일명(XXX_20130101)

	/** LOV Manager Table을 Excel파일로 Export **/
	private String strExportExcelFileName = ""; // Export할 Excel파일 명

	/** xml파일 확장자 **/
	private String strXMLExtension = ".xml"; // 확장자

	public Registry registry;

	public ArrayList<LovDataItem> selectedList;

	public String infodbaPassword = "";

	/**
	 * 생성자
	 */
	public LovManagerDialog(String _infodbaPassword) throws Exception
	{
		super(AIFDesktop.getActiveDesktop().getFrame(), false);

		infodbaPassword = _infodbaPassword;
		this.lovList = new ArrayList<LovItem>();
		this.dataList = new ArrayList<LovDataItem>();
		this.session = (TCSession) AIFDesktop.getActiveDesktop().getCurrentApplication().getSession();
		this.registry = Registry.getRegistry(this);
		this.selectedList = new ArrayList<LovDataItem>();

		/** xml파일 download 경로 지정 **/
		initDownloadDir();

		/** 서버에 있는 LOV값을 xml파일로 가져온다. */
		this.exportFile(); // LOV 값을 xml 파일로 export한다.

		this.init();

		this.setResizable(true);
		// this.setSize(1000, 800);
		this.setPreferredSize(new Dimension(1000, 500));
		centerToScreen();

		this.loadLovList();

	}

	/**
	 * XML파일이 저장될 폴더의 위치를 생성한다.
	 */
	public void initDownloadDir()
	{
		xmlDownloadPath = xmlDownloadPath + getTodayDate(true).toString() + "/"; // ex) C:/Temp/lovname20130326	
		xmlDownloadPath_lang = xmlDownloadPath + "lang/"; // ex) C:/Temp/lovname20130326/lang/

		/** c:\temp\lovnameyyMMdd 폴더가 없으면 생성 */
		File tmpDir = new File(xmlDownloadPath);
		if (!tmpDir.exists())
			tmpDir.mkdir();

		try
		{
			Thread.sleep(1000);
		} catch (InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * executeTmpBatch.bat 파일 생성 후 bmide_manage_batch_lovs.bat 배치를 실행하여
	 * BMIDE에서 LOV 값을 XML 파일로 export 한다.
	 * 파일 형식은 lov_values_201303261041.xml 형태로 생성된다.
	 * C:\\Tc9.properties.txt 내용 : SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * 
	 * @throws Exception
	 */
	public void exportFile() throws Exception
	{

		/** bmide_manage_batch_lovs.bat 명령을 실행하기 위한 환경변수값이 있는 파일 */
//	   File pFile = new File("C:\\Tc9.properties.txt");
//	   
//	   if(!pFile.exists()) {
//		   String message = registry.getString("lovmanage.MESSAGE.Front") + pFile + registry.getString("lovmanage.MESSAGE.Back");
//		   throw new Exception(message);
//	   }

		WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
		simpleProgressBar.setWindowSize(500, 300);

		/** bmide_manage_batch_lovs.bat(-> XML파일 생성됨) 파일을 실행할 batch 파일을 생성한다. **/
		File path = new File(xmlDownloadPath);
		File batFile = new File(path, "executeTmpBatch.bat");
		if (!batFile.exists())
		{
			batFile.createNewFile();
		}

//	   FileInputStream fis = new FileInputStream(pFile);
//	   Scanner s = new Scanner(fis);

		FileWriter fw = new FileWriter(batFile);
//	   while (s.hasNext()) {
		fw.write("SET TC_ROOT=D:\\SIEMENS\\TC10\r\n");
		fw.write("SET TC_DATA=Y:\\tcdata10\r\n");
		fw.write("call %TC_DATA%\\tc_profilevars.bat\r\n\r\n");
//	   }
//	   s.close();

		/** oldXML 파일명 생성 */
		setOldXmlFileName();

		/** oldXML 파일의 절대경로 지정 */
		String exportFile = xmlDownloadPath + oldXMLFile + strXMLExtension; // ex) C:\Temp\lovname20130326\lov_values_201303261041.xml

		/** bmide_mamage_batch_lovs.bat 파일을 실행하기 위한 명령어를 받아옴 */
		String command = getExecuteBatch(true, exportFile);
		fw.write(command);
		fw.close();

		/** ProgressBar 실행 */
		simpleProgressBar.start();
		simpleProgressBar.setStatus("LOV Export is start...", true);

		/** 배치 파일 실행 */
		String[] cmd = { "CMD", "/C", batFile.getPath() };
		Process p = Runtime.getRuntime().exec(cmd);

		/** 외부 프로그램에 대한 InputStream 을 생성 */
		DataInputStream inputstream = new DataInputStream(p.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

		String strOutput = "";

		while (true)
		{
			// 외부 프로그램이 출력하는 메세지를 한줄씩 읽어들임
			strOutput = reader.readLine();

			if (strOutput != null)
			{
				simpleProgressBar.setStatus(strOutput, true);
			}

			if (strOutput == null)
			{
				p.destroy();
				break;
			}
		}
		simpleProgressBar.setStatus("LOV Export is end...", false);
		simpleProgressBar.close();
	}

	/**
	 * xml파일명을 생성한다.
	 * xml : lov_values_201303261041
	 * xml_lang : lov_values_201303261041_lang
	 */
	public void setOldXmlFileName()
	{
		/** 오늘 날짜를 가져옴(20130101) */
		String currentDate = getTodayDate(false);
		oldXMLFile = oldXMLFile + "_" + currentDate;
		oldXMLFile_lang = oldXMLFile + "_lang";
	}

	/**
	 * LOV Dialog를 초기화한다.
	 * 
	 * @throws Exception
	 */
	private void init() throws Exception
	{
//	  Registry reg = Registry.getRegistry("com.teamcenter.rac.aif.aif");
//      String runnerFile = reg.getString("runnerCommand");
//      String tcRoot = new File(runnerFile).getParentFile().getAbsolutePath();

		this.getContentPane().setLayout(new BorderLayout());
		setTitle("LOV Manager");

		JPanel mainPanel = new JPanel(new BorderLayout());

		this.lovItemTableModel = new LovItemTableModel();
		this.lovTable = new JTable(lovItemTableModel);

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정. 
		JTableHeader lovTableHeader = this.lovTable.getTableHeader();
		lovTableHeader.setUpdateTableInRealTime(true);
		lovTableHeader.addMouseListener(new LovItemColumnHeaderMouseAdapter(this.lovTable));
		//------------------------------------------------------------------------------------------

		this.lovTable.getSelectionModel().addListSelectionListener(new TableSelectionListener());
		this.lovTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane lovListPain = new JScrollPane(lovTable);
		lovListPain.setPreferredSize(new Dimension(452, 300));

		JPanel dataPanel = new JPanel(new BorderLayout());

		this.lovDataItemTableModel = new LovDataItemTableModel();
		this.dataTable = new JTable(lovDataItemTableModel);
		this.dataTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.dataTable.getColumn("Key").setMinWidth(200);
		this.dataTable.getColumn("Description").setMinWidth(150);
		this.dataTable.getColumn("Display Name").setMinWidth(200);
		this.dataTable.getColumn("Status").setMinWidth(100);

		// 각 Column별 SellRenderer Setting
		for (int k = 0; k < lovDataItemTableModel.cNames.length; k++)
		{
			// Custom TableCellRenderer
			LovDataTableCellRenderer tableRenderer = new LovDataTableCellRenderer();
			TableColumn column = this.dataTable.getColumnModel().getColumn(k);
			column.setCellRenderer(tableRenderer);
		}

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정. 
		JTableHeader dataTableHeader = this.dataTable.getTableHeader();
		dataTableHeader.setUpdateTableInRealTime(true);
		dataTableHeader.addMouseListener(new LovDataColumnHeaderMouseAdapter(this.dataTable));
		//------------------------------------------------------------------------------------------

		this.dataTable.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent ee)
			{
			}

			public void mousePressed(MouseEvent ee)
			{
				// 더블클릭 일 경우, 선택된 행의 수정화면으로 이동
				if (ee.getClickCount() == 2)
				{
					int nSelected = dataTable.getSelectedRow();

					if (nSelected < 0)
						return;

					LovDataItem selectedLOVItem = dataList.get(nSelected);
					LovDataDialog dlg = new LovDataDialog(selectedLOVItem);

					dlg.setVisible(true);

				}
			}

			public void mouseClicked(MouseEvent ee)
			{
			}
		});

		JScrollPane dataTablePain = new JScrollPane(dataTable);
		dataTablePain.setPreferredSize(new Dimension(452, 300));

		JPanel buttonPanel = new JPanel(new BorderLayout());
		JPanel leftBtnPanel = new JPanel();
		JPanel centerBtnPanel = new JPanel();
		JPanel rightBtnPanel = new JPanel();

		// LOV 추가
		JButton plusBtn = new JButton("+");
		plusBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{
				final int nSelected = lovTable.getSelectedRow();
				if (nSelected < 0)
					return;
				LovItem selectedLOVItem = lovList.get(nSelected);
				// LOV 추가 Dialog
				LovDataDialog dlg = new LovDataDialog();

				dlg.setVisible(true);

			}
		});

		// LOV 삭제
		JButton minusBtn = new JButton("-");
		minusBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{
				int nSelected = dataTable.getSelectedRow();

				if (nSelected < 0)
					return;

				LovDataItem selectedLOVItem = dataList.get(nSelected);

				if (selectedLOVItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_ADD))
				{
					dataList.remove(nSelected);
				} else
				{
					selectedLOVItem.setData(LovDataItem.INDEX_STATUS, LovDataItem.STATUS_DELETE);
				}

				lovDataItemTableModel.fireTableDataChanged();
			}
		});

		leftBtnPanel.add(plusBtn);
		leftBtnPanel.add(minusBtn);

		final JButton selectTargetBtn = new JButton("Select Target");
		selectTargetBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{
				int nSelected = dataTable.getSelectedRow();

				if (nSelected < 0)
					return;

				LovDataItem selectedLOVItem = dataList.get(nSelected);

				selectedList.clear();

				selectedList.add(selectedLOVItem);

				selectTargetBtn.setText("Select Target:" + selectedList.size());

			}
		});

		JButton moveToBtn = new JButton("Move To");
		moveToBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{
				int nSelected = dataTable.getSelectedRow();

				if (nSelected < 0)
					return;

				LovDataItem selectedLOVItem = dataList.get(nSelected);

				dataList.removeAll(selectedList);

				dataList.addAll(nSelected, selectedList);

				lovDataItemTableModel.fireTableDataChanged();

				selectTargetBtn.setText("Select Target");
			}
		});

		centerBtnPanel.add(selectTargetBtn);
		centerBtnPanel.add(moveToBtn);

		buttonPanel.add(leftBtnPanel, BorderLayout.WEST);
		buttonPanel.add(rightBtnPanel, BorderLayout.EAST);
		buttonPanel.add(centerBtnPanel, BorderLayout.CENTER);

		JButton btnSaveTc = new JButton("Save TC");
		btnSaveTc.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{
				/** 선택된 LOV Data를 TeamCenter에 저장합니다. */
				saveActionTC();
				/** Dialog의 Status 값을 초기화한다. */
				initStatus();
			}
		});

		/** 해당 LOV의 값을 Excel 파일로 저장한다. */
		JButton btnExcelExport = new JButton("Excel Export");
		btnExcelExport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{

				excelExport();
			}
		});

		/** 모든 LOV의 값을 Excel 파일로 저장한다. */
		JButton btnExcelFullExport = new JButton("Excel Full Export");
		btnExcelFullExport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{

				excelFullExport();
			}
		});

		JButton btnCloseDialog = new JButton("EXIT");
		btnCloseDialog.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				setVisible(false);
				dispose();
			}
		});

		rightBtnPanel.add(btnSaveTc);
		rightBtnPanel.add(btnExcelExport);
		rightBtnPanel.add(btnExcelFullExport);
		rightBtnPanel.add(btnCloseDialog);

//    JButton btnSaveTcAll = new JButton("Save TC All");
//    btnSaveTcAll.addActionListener(new ActionListener()
//    {
//      public void actionPerformed(ActionEvent ee)
//      {
//        // 모든 LOV Data를 TeamCenter에 저장합니다.
//        saveActionTCAll();
//      }
//    });
//    rightBtnPanel.add(btnSaveTcAll);

		dataPanel.add(dataTablePain, BorderLayout.CENTER);
		dataPanel.add(buttonPanel, BorderLayout.SOUTH);

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, lovListPain, dataPanel);
		splitPane.setContinuousLayout(true);
		// splitPane.setOneTouchExpandable(true);
		splitPane.setDividerSize(8);
		splitPane.setDividerLocation(200);

		mainPanel.add(splitPane, BorderLayout.CENTER);

		this.getContentPane().add(mainPanel, BorderLayout.CENTER);

		// add for lov file export & import...
//    openFile();
	}

	/**
	 * XML 파일을 parse하여 Document 객체를 생성한다.
	 * 
	 * @param xmlFile
	 * @throws Exception
	 */
	public void openFile() throws Exception
	{
		// xml 파일을 parse하여 document에 넣어둠
		dbf = DocumentBuilderFactory.newInstance();
		db = dbf.newDocumentBuilder();

		String path = xmlDownloadPath + oldXMLFile + strXMLExtension; // ex) C:\Temp\lovname20130326\lov_values_201303261041.xml
		File file1 = new File(path);

		String path_lang = xmlDownloadPath_lang + oldXMLFile_lang + strXMLExtension;

		if (file1.exists())
		{
			document = db.parse(path);
			document_lang = db.parse(path_lang);
		} else
		{
			String message = "지정된 위치(" + path + ")에 파일이 존재하지 않습니다.";
			throw new Exception(message);
		}
	}

	/**
	 * LOV Dialog의 정보를 Excel 파일로 export한다.
	 */
	public void excelExport()
	{
		try
		{
			// 1. 파일 저장 대화상자를 띄운다
			JFileChooser fileChooser = new JFileChooser("C:/");

			/** 해당 디렉토리에서 xls 파일만 보이도록 설정한다. */
			FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
			fileChooser.removeChoosableFileFilter(fileFilter);
			SimpleStructureFilter filterXLS = new SimpleStructureFilter("xls", "엑셀 (.xls)");
			fileChooser.addChoosableFileFilter(filterXLS);

			// 2. 지정한 이름으로 엑셀 파일을 생성한다
			File file = new File("C:/" + strExportExcelFileName + ".xls");
			fileChooser.setSelectedFile(file);

			if (fileChooser.showSaveDialog(AIFDesktop.getActiveDesktop().getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = fileChooser.getSelectedFile();

				if (selectedFile != null)
				{
					String strExtension = getExtension(selectedFile);
					strExtension = strExtension == null ? "" : strExtension;

					// 사용자가 입력한 파일명에 xls 확장자까지 입력 안했을 경우
					if (!strExtension.equalsIgnoreCase("xls"))
					{
						selectedFile = new File(selectedFile.getAbsolutePath() + ".xls");
					}

					// 쓰기가능한 엑셀 Workbook 객체 생성
					WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);

					/** LOV 값으로 Excel 파일에 Write한다. */
					workBook = exportDataXLS(workBook, dataTable, selectedFile, strExportExcelFileName, 0);

					workBook.write();
					workBook.close();
				}

				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
			}
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * LOV Dialog의 모든 LOV값을 Excel파일로 저장한다.
	 * LOV Name은 각각의 Sheet명이 된다.
	 */
	public void excelFullExport()
	{
		try
		{
			// 1. 파일 저장 대화상자를 띄운다
			JFileChooser fileChooser = new JFileChooser("C:/");
			FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
			fileChooser.removeChoosableFileFilter(fileFilter);
			SimpleStructureFilter filterXLS = new SimpleStructureFilter("xls", "엑셀 (.xls)");
			fileChooser.addChoosableFileFilter(filterXLS);

			// 2. 지정한 이름으로 엑셀 파일을 생성한다
			File file = new File("C:/LOV_All_List_" + getTodayDate(true) + ".xls");
			fileChooser.setSelectedFile(file);

			if (fileChooser.showSaveDialog(AIFDesktop.getActiveDesktop().getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = fileChooser.getSelectedFile();

				if (selectedFile != null)
				{
					// 파일의 확장자를 return한다.
					String strExtension = getExtension(selectedFile);
					strExtension = strExtension == null ? "" : strExtension;

					// 사용자가 입력한 파일명에 xls 확장자까지 입력 안했을 경우
					if (!strExtension.equalsIgnoreCase("xls"))
					{
						selectedFile = new File(selectedFile.getAbsolutePath() + ".xls");
						strExtension = "xls";
					}

					// 쓰기가능한 엑셀 Workbook 객체 생성
					WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);

					/** LOV 명을 각각의 Sheet Name으로 지정하고, LOV 값을 Excel에 Write한다. */
					for (int i = 0; i < lovList.size(); i++)
					{
						LovItem selectedLOVItem = lovList.get(i);
						dataList = selectedLOVItem.getAllLovData();

						String sheetName = selectedLOVItem.toString();
						workBook = exportDataXLS(workBook, dataTable, selectedFile, sheetName, i);
					}

					workBook.write();
					workBook.close();
				}

				/** Excel 파일을 실행한다. **/
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
			}
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * LOV Dialog의 JTable 의 내용을 XLS 포맷으로 저장한다.
	 * 
	 * @param jtable
	 *            JTable
	 * @param selectedFile
	 *            File
	 */
	private WritableWorkbook exportDataXLS(WritableWorkbook workBook, JTable jtable, File selectedFile, String strItemKey, int sheetNum) throws Exception
	{
		int iRowCnt = jtable.getRowCount();
		int iColumnCnt = jtable.getColumnCount() - 1;

		try
		{
			// 쓰기가능한 엑셀 Workbook 객체 생성
//    	WritableWorkbook workBook = null;
//      if(sheetNum == 0) {
//    	  workBook = Workbook.createWorkbook(selectedFile);
//      } else {
//    	  Workbook workBook1 = Workbook.getWorkbook(selectedFile);
//    	  workBook = Workbook.createWorkbook(selectedFile, workBook1);
//      }

			// n번째 Sheet 생성
			WritableSheet sheet = workBook.createSheet(strItemKey, sheetNum);

			SheetSettings printSet = sheet.getSettings();
			printSet.setFitWidth(1);
			printSet.setFitToPages(true);
			printSet.setOrientation(PageOrientation.LANDSCAPE);

			// 1. 헤더 정보 가져오기
			int[] iColumnWidth = new int[iColumnCnt];
			int iRemoveCol = 0;
			int iRemoveRow = 0;

			for (int kCnt = 0; kCnt < iColumnCnt; kCnt++)
			{

				Label cellTitle = new Label(kCnt, 0, jtable.getColumnName(kCnt), setCellValueFormat(2));
				sheet.addCell(cellTitle);

				sheet.setRowView(0, 300);
				if (iColumnWidth[kCnt - iRemoveCol] < jtable.getColumnName(kCnt).getBytes().length + 2)
				{
					iColumnWidth[kCnt - iRemoveCol] = jtable.getColumnName(kCnt).getBytes().length + 2;
				}
			}

			// 2. 전체 Row 정보 가져오기
			for (int iCnt = 0; iCnt < iRowCnt; iCnt++)
			{
				for (int kCnt = 0; kCnt < iColumnCnt; kCnt++)
				{
					Object obj = jtable.getValueAt(iCnt, kCnt);
					String strObj = obj != null ? obj.toString() : "";

					Label cellValue = new Label(kCnt, iCnt + 1, obj != null ? obj.toString() : "", setCellValueFormat(0));

					sheet.addCell(cellValue);

					if (iColumnWidth[kCnt - iRemoveCol] < strObj.getBytes().length + 2)
					{
						iColumnWidth[kCnt - iRemoveCol] = strObj.getBytes().length + 2;
					}
				}
				sheet.setRowView(iCnt - iRemoveRow + 1, 300);
			}

			for (int kCnt = 0; kCnt < iColumnCnt; kCnt++)
			{
				sheet.setColumnView(kCnt, iColumnWidth[kCnt]);
			}

//      workBook.write();
//      workBook.close();
		} catch (Exception ex)
		{
			ex.printStackTrace();
		}
		return workBook;
	}

	/**
	 * jxl 의 Cell Format 을 설정한다.
	 * 
	 * @param feature
	 * @return
	 * @throws WriteException
	 */
	private WritableCellFormat setCellValueFormat(int status) throws Exception
	{
		WritableFont wf = new WritableFont(WritableFont.createFont("굴림"), 9, WritableFont.NO_BOLD);

		WritableCellFormat wcf = new WritableCellFormat(wf);
		wcf.setWrap(false);
		wcf.setLocked(false);
		// wcf.setBorder(Border.ALL, BorderLineStyle.THIN);
		wcf.setBorder(Border.ALL, BorderLineStyle.HAIR);

		switch (status)
		{
			case 0:
				wcf.setAlignment(Alignment.LEFT);
				wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
				break;

			case 1:
				wcf.setAlignment(Alignment.LEFT);
				wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
				wcf.setBackground(Colour.LIGHT_BLUE);
				break;

			case 2:
				wcf.setAlignment(Alignment.LEFT);
				wcf.setVerticalAlignment(VerticalAlignment.CENTRE);
				wcf.setBackground(Colour.GREY_25_PERCENT);
				break;

		}
		return wcf;
	}

	/**
	 * 파일의 확장자를 Return
	 * 
	 * @param f
	 *            File
	 * @return String
	 */
	private String getExtension(File f) throws Exception
	{
		if (f != null)
		{
			String filename = f.getName();
			int i = filename.lastIndexOf('.');
			if (i > 0 && i < filename.length() - 1)
			{
				return filename.substring(i + 1).toLowerCase();
			}
		}
		return null;
	}

	/**
	 * LOV List를 Loading합니다.
	 * 1. bmide_manage_batch_lovs.bat Utility를 실행하여 Xml을 생성합니다.
	 * 2. XML을 Loading하여 LOV/LOVData를 Loading합니다.
	 */
	void loadLovList()
	{
		Thread lovLoad = new Thread()
		{
			public void run()
			{
				try
				{

					/** Document 객체를 생성한다. */
					openFile();

					/** newXML 파일을 생성하기 위해 oldXML 파일의 Node값을 읽어온다. **/
					setDefaultXML();

					/** newXML_lang 파일을 생성하기 위해 oldXML 파일의 Node값을 읽어온다. **/
					setDefaultXMLForLang();

					Element rootElement = document.getDocumentElement();

					NodeList memberList = rootElement.getElementsByTagName("TcLOV");

					// LOV의 값을 구한다. - 1 Level
					String lovName = "";
					for (int i = 0; i < memberList.getLength(); i++)
					{ // for 1
						Node node = memberList.item(i);

						if (node.getNodeType() == Node.ELEMENT_NODE)
						{
							LovItem lov = null;
							NamedNodeMap attrs = node.getAttributes();
							for (int j = 0; j < attrs.getLength(); j++)
							{ // for 2
								if ((attrs.item(j).getNodeName()).equals("name"))
								{
									lovName = attrs.item(j).getNodeValue();
									lov = new LovItem(lovName);
									break;
								}
							}

							NodeList childNodeList = node.getChildNodes();
							// 선택된 TCLov의 List값을 구한다. - 2 Level
							for (int k = 0; k < childNodeList.getLength(); k++)
							{ // for 3
								Node childNode = childNodeList.item(k);

								if ((childNode.getNodeName()).equals("TcLOVValue"))
								{ // LOV의 Key, Value값
									LovDataItem dataItem = new LovDataItem();
									NamedNodeMap childAttrs = childNode.getAttributes();
									String key = "";
									for (int y = 0; y < childAttrs.getLength(); y++)
									{ // for 4
										if ((childAttrs.item(y).getNodeName()).equals("value"))
										{
											key = childAttrs.item(y).getNodeValue();
											dataItem.setData(LovDataItem.INDEX_KEY, key);
										} else if ((childAttrs.item(y).getNodeName()).equals("description"))
										{
											dataItem.setData(LovDataItem.INDEX_DESC, childAttrs.item(y).getNodeValue());
											dataItem.setData(LovDataItem.INDEX_STATUS, LovDataItem.STATUS_NORMAL);
										}

									} // end for 4

									// 여기에서 XML_Lang의 desc 값을 구한다.
									String lang_Desc = getDescToXMLFileForLang(lovName, key);
									dataItem.setData(LovDataItem.INDEX_VALUE, lang_Desc);

									lov.setLovData(dataItem);

//                    		if( lov.strName.equals("S7_MAIN_NAME") && dataItem != null && dataItem.getData(LovDataItem.INDEX_KEY).equals("REINF"))
//                    		{
//                    			System.out.println("____ lovName="+ lovName +", key="+ key +", lang_Desc="+ lang_Desc +"::");
//
//                        		System.out.println(dataItem.getData(LovDataItem.INDEX_KEY));
//                        		System.out.println(dataItem.getData(LovDataItem.INDEX_DESC));
//                        		System.out.println(dataItem.getData(LovDataItem.INDEX_VALUE));
//                    		}

								} // end if
							} // end for 3
							lovList.add(lov);
						} // if(node.getNodeType() == Node.ELEMENT_NODE)
					} // end for 1

					lovItemTableModel.fireTableDataChanged();
				} catch (Exception e)
				{
					MessageBox.post(LovManagerDialog.this, e);
					e.printStackTrace();
				}
			}
		};

		lovLoad.start();

	}

	/**
	 * import할 xml 파일을 생성하기 위해 Xmlns 와 batchXSDVersion 값을 lov_values_yyyyMMdd.xml 에서 가져온다.
	 * 
	 * @throws Exception
	 */
	public void setDefaultXML()
	{
		Element rootElement = document.getDocumentElement();

		// XML 파일에서 xmlns 와 batchXSDVersion 정보를 가져온다.
		NamedNodeMap topNodeMap = rootElement.getAttributes();
		for (int i = 0; i < topNodeMap.getLength(); i++)
		{
			if (topNodeMap.item(i).getNodeName().equals(TOP_NODE_ATTR_NAME_XMLNS))
			{
				topNodeAttrXmlns = topNodeMap.item(i).getNodeValue();
			} else if (topNodeMap.item(i).getNodeName().equals(TOP_NODE_ATTR_NAME_VERSION))
			{
				topNodeAttrVersion = topNodeMap.item(i).getNodeValue();
			}
		}
	}

	/**
	 * import할 xml 파일을 생성하기 위해 Xmlns 와 batchXSDVersion 값을 lov_values_yyyyMMdd_Lang.xml 에서 가져온다.
	 * download된 XML_Lang 파일의 기본 정보를 읽어서 저장한다.
	 */
	public void setDefaultXMLForLang() throws SAXException, IOException
	{
		Element rootElement_lang = document_lang.getDocumentElement();

		// XML_lang 파일에서 xmlns 와 batchXSDVersion 정보를 가져온다.
		NamedNodeMap topNodeMap_lang = rootElement_lang.getAttributes();
		for (int i = 0; i < topNodeMap_lang.getLength(); i++)
		{
			if (topNodeMap_lang.item(i).getNodeName().equals(TOP_NODE_ATTR_NAME_XMLNS))
			{
				topNodeAttrXmlns_lang = topNodeMap_lang.item(i).getNodeValue();
			} else if (topNodeMap_lang.item(i).getNodeName().equals(TOP_NODE_ATTR_NAME_VERSION))
			{
				topNodeAttrVersion_lang = topNodeMap_lang.item(i).getNodeValue();
			}
		}
	}

	/**
	 * XML_Lang 파일에서 해당 LOV의 Description 값을 읽어온다.
	 * 
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 */
	public String getDescToXMLFileForLang(String lovName, String keyValue) throws SAXException, IOException
	{
		String strReturn = "";
		Element rootElement_lang = document_lang.getDocumentElement();
		NodeList memberList = rootElement_lang.getElementsByTagName("Add");

		// LOV의 값을 구한다. - 1 Level
		for (int i = 0; i < memberList.getLength(); i++)
		{ // for 1
			Node node = memberList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{

				NodeList childNodeList = node.getChildNodes();
				// 선택된 TCLov의 List값을 구한다. - 2 Level
				for (int k = 0; k < childNodeList.getLength(); k++)
				{ // for 3
					Node childNode = childNodeList.item(k);

					if ((childNode.getNodeName()).equals("key"))
					{ // LOV의 Key, Value값
						NamedNodeMap childAttrs = childNode.getAttributes();

						for (int y = 0; y < childAttrs.getLength(); y++)
						{ // for 4
							if ((childAttrs.item(y).getNodeName()).equals("id"))
							{
								//[20190225 kch] xml 파일로 부터 lov List 정보 read 시 bug 개선 ( contains -> equals )
								//if (childAttrs.item(y).getNodeValue().contains("LOVValue{::}" + lovName + "{::}" + keyValue )) { // && childAttrs.item(y).getNodeValue().endsWith(keyValue)) {
								if (childAttrs.item(y).getNodeValue().equals("LOVValue{::}" + lovName + "{::}" + keyValue))
								{
									strReturn = childNode.getTextContent();
									return strReturn;
								}
							}
						}
					}
				}
			} // if(node.getNodeType() == Node.ELEMENT_NODE)
		} // end for 1

		return strReturn;
	}

	/**
	 * 모든 LOV Data를 TeamCenter에 저장합니다.
	 */
	void saveActionTCAll()
	{
		Thread partListThread = new Thread()
		{

			public void run()
			{
				try
				{

					for (int i = 0; i < lovList.size(); i++)
					{
						// 해당 LOV를 강제 선택합니다.
						lovTable.getSelectionModel().setSelectionInterval(0, i);
						Thread.sleep(1000);
						saveActionTC();
					}

				} catch (Exception e)
				{
				}
			}

		};

		partListThread.start();

	}

	/**
	 * XML Import 후 dialog의 상태값을 초기화한다.
	 */
	void initStatus()
	{
		// STATUS_DELETE 는 table에서 삭제한다.
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem selectedLOVItem = dataList.get(i);

			if (selectedLOVItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_DELETE))
			{
				dataList.remove(i);
			}
		}

		// table의 Status값을 초기화한다.
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem selectedLOVItem = dataList.get(i);

			selectedLOVItem.setData(LovDataItem.INDEX_STATUS, LovDataItem.STATUS_NORMAL);
		}
		lovDataItemTableModel.fireTableDataChanged();
	}

	/**
	 * 선택된 LOV Data를 TeamCenter에 저장합니다.
	 * 
	 */
	void saveActionTC()
	{

		ArrayList<LovDataItem> addList = new ArrayList<LovDataItem>();
		ArrayList<LovDataItem> modifyList = new ArrayList<LovDataItem>();
		ArrayList<LovDataItem> removeList = new ArrayList<LovDataItem>();

		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem dataItem = dataList.get(i);

			if (dataItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_ADD))
			{
				addList.add(dataItem);
			} else if (dataItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_MODIFY))
			{
				modifyList.add(dataItem);
			} else if (dataItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_DELETE))
			{
				removeList.add(dataItem);
			}

		}

		// xml 파일 생성
		writeXML();
		executeLogMessage();
	}

	void writeXML()
	{
		/** document 객체 생성 */
		org.jdom.Document doc = new org.jdom.Document();
		org.jdom.Document doc_Lang = new org.jdom.Document();

		/** NameSpace 생성 */
		org.jdom.Namespace nameSpace = org.jdom.Namespace.getNamespace(topNodeAttrXmlns);
		org.jdom.Namespace nameSpace_Lang = org.jdom.Namespace.getNamespace(topNodeAttrXmlns_lang);

		/** Element 객체 생성 */
		org.jdom.Element topElement = new org.jdom.Element("TcBusinessData", nameSpace); // TcBusinessData
		org.jdom.Element changeElement = new org.jdom.Element("Change", nameSpace); // <Change>
		org.jdom.Element tclovElement = new org.jdom.Element("TcLOV", nameSpace); // <TcLOV
		org.jdom.Element topElement_Lang = new org.jdom.Element("TcBusinessDataLocalization", nameSpace_Lang); // TcBusinessDataLocalization
		org.jdom.Element addElement_Lang = new org.jdom.Element("Add", nameSpace_Lang); // Add

		// 현재 시간
		Date currentDate = new Date();

		topElement.setAttribute(TOP_NODE_ATTR_NAME_VERSION, topNodeAttrVersion);
		topElement.setAttribute("Date", currentDate.toString());

		// Change Tag 속성
		tclovElement.setAttribute("name", lovName);
		tclovElement.setAttribute("usage", "Exhaustive");
		tclovElement.setAttribute("lovType", "ListOfValuesString");
		tclovElement.setAttribute("isManagedExternally", "true");

		// XML_Lang 파일 생성
		topElement_Lang.setAttribute(TOP_NODE_ATTR_NAME_VERSION, topNodeAttrVersion_lang);
		topElement_Lang.setAttribute("Date", currentDate.toString());

		/** xml 파일 및 lang.xml 파일의 LOVValue tag 부분 set */
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem dataItem = dataList.get(i);

			if (dataItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_DELETE))
			{
				continue;
			}

			org.jdom.Element valueElement = new org.jdom.Element("TcLOVValue", nameSpace);
			org.jdom.Element keyElement = new org.jdom.Element("key", nameSpace_Lang);

			String strKey = dataItem.getData(LovDataItem.INDEX_KEY);
			String strValue = dataItem.getData(LovDataItem.INDEX_DESC);

			/** .xml File Node setting */
			valueElement.setAttribute("value", strKey);
			valueElement.setAttribute("description", strValue);
			valueElement.setAttribute("conditionName", "isTrue");

			/** ~~_lang.xml File Node setting */
			keyElement.setAttribute("id", "LOVValue{::}" + lovName + "{::}" + strKey);
			keyElement.setAttribute("locale", "en_US");
			keyElement.setAttribute("status", "Approved");

			keyElement.setText(dataItem.getData(LovDataItem.INDEX_VALUE));

			//각 엘리먼트들 배치 작업 (엘리먼트에 자식 요소를 추가할 떄는 )(위의 주석대로 배치를 해줘야 한다).  addContent()메서드를 이용한다.
			tclovElement.addContent(valueElement);
			addElement_Lang.addContent(keyElement);
		}

		/** lang.xml 파일의 LOVValueDescription 부분 set */
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem dataItem = dataList.get(i);

			if (dataItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_DELETE))
			{
				continue;
			}

			org.jdom.Element keyDescElement = new org.jdom.Element("key", nameSpace_Lang);

			String strKey = dataItem.getData(LovDataItem.INDEX_KEY);

			/** ~~_lang.xml File Node setting */
			keyDescElement.setAttribute("id", "LOVValueDescription{::}" + lovName + "{::}" + strKey);
			keyDescElement.setAttribute("locale", "ko_KR");
			keyDescElement.setAttribute("status", "Approved");

			keyDescElement.setText(dataItem.getData(LovDataItem.INDEX_DESC));//yunjae

			//각 엘리먼트들 배치 작업 (엘리먼트에 자식 요소를 추가할 떄는 )(위의 주석대로 배치를 해줘야 한다).  addContent()메서드를 이용한다.
			addElement_Lang.addContent(keyDescElement);
		}

		changeElement.addContent(tclovElement);
		topElement.addContent(changeElement);
		topElement_Lang.addContent(addElement_Lang);

		// 마지막으로 Document에 최상위 Element를 설정한다.
		doc.setRootElement(topElement);
		doc_Lang.setRootElement(topElement_Lang);

		// 파일로 저장하기 위해서 XMLOutputter 객체가 필요하다
		XMLOutputter xout = new XMLOutputter();

		// 기본 포맷 형태를 불러와 수정한다.
		Format fm = xout.getFormat();

		/** 다국어 지원을 위해 encodeing 형태를 UTF-8로 변경한다. */
//      fm.setEncoding("euc-kr");
		fm.setEncoding("UTF-8");

		// 부모, 자식 태그를 구별하기 위한 탭 범위를 정한다.
		fm.setIndent("   ");
		//태그끼리 줄바꿈을 지정한다.
		fm.setLineSeparator("\r\n");

		// 설정한 XML 파일의 포맷을 set한다.
		xout.setFormat(fm);
		try
		{
//    	  xout.output(doc, new FileWriter(strExportPath + strImportXMLFileName + strXMLExtension));
//    	  xout.output(doc_Lang, new FileWriter(strExportLangPath + strImportXMLLangFileName + strXMLExtension));

			/** XML 파일 저장을 UTF-8로 해야 하기에 FileOutputStream으로 저장함.(FileWriter는 UTF-8로 저장이 안됨) */
			xout.output(doc, new FileOutputStream(xmlDownloadPath + newXMLFile + strXMLExtension));
			xout.output(doc_Lang, new FileOutputStream(xmlDownloadPath_lang + newXMLFile_lang + strXMLExtension));

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * bmide_mamage_batch_lovs.bat 파일을 실행하기 위한 파라미터 값을 리턴한다.
	 * 
	 * @param isExport
	 *            : true이면 extract(XML 파일 Export), false이면 update(XML 파일 Import)
	 * @param file
	 *            : xml 파일의 full path
	 * @return
	 */
	public String getExecuteBatch(boolean isExport, String file)
	{
		String value = "";

		if (isExport)
		{
			value = "call bmide_manage_batch_lovs.bat -u=infodba -p=" + infodbaPassword + " -g=dba -option=extract -file=" + file + "";
		} else
		{
			value = "call bmide_manage_batch_lovs.bat -u=infodba -p=" + infodbaPassword + " -g=dba -option=update -file=" + file + " \n";
			value += "call generate_client_meta_cache.exe -u=infodba -p=" + infodbaPassword + " -g=dba update lovs";
		}
		return value;
	}

	/**
	 * 수정된 LOV 값을 서버로 Export한다.
	 */
//  void executeLogMessage(){
//	  File pFile = new File("C:\\Tc9.properties.txt");		// 이 파일은 항상 존재해야 한다.
//	  FileWriter fw = null;
//	  
//	  try {
//		  if(!pFile.exists()) {
////			   String message = "지정된 위치(" + pFile + ")에 파일이 존재하지 않습니다.";
//			  String message = registry.getString("lovmanage.MESSAGE.Front") + pFile + registry.getString("lovmanage.MESSAGE.Back");
//			  MessageBox.post(message, "알림", MessageBox.INFORMATION);
//			  return;
//		  }
//		  
//		  /** 파일 객체 생성 */
//		  File tmpDir = new File(xmlDownloadPath);
//		  File batFile = new File(tmpDir, "updateTmpBatch.bat");
//		  if(!batFile.exists()) {
//			  batFile.createNewFile();
//		  }
//			
//		  fw = new FileWriter(batFile);
//			 
//		  FileInputStream fis = new FileInputStream(pFile);
//		  Scanner s = new Scanner(fis);
//		  while (s.hasNext()) {
//			  fw.write(s.nextLine().toString() + "\n");
//		  }
//
//		  /** bmide_mamage_batch_lovs.bat 파일을 실행하기 위한 명령어를 받아옴 */
//		  String exportFile = xmlDownloadPath + newXMLFile + strXMLExtension;
//		  String command = getExecuteBatch(false, exportFile);
//		  fw.write(command);
//		  s.close();
//		  fw.close();
//
//		  /** Progress bar 실행 */
//		  WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
//		  simpleProgressBar.setWindowSize(500, 300);
//		  simpleProgressBar.start();
//		  simpleProgressBar.setStatus("LOV Import is start..."	, true);
//		  
//		  /** 배치파일 실행 */
//		  String[] cmd = { "CMD", "/C", batFile.getPath() };
//		  Process p = Runtime.getRuntime().exec(cmd);
//	  
//		  // 외부 프로그램에 대한 InputStream 을 생성
//	      DataInputStream inputstream = new DataInputStream(p.getInputStream());
//	      BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
//	      
//	      String strOutput = "";
//	      while (true)
//	      {
//	    	  // 외부 프로그램이 출력하는 메세지를 한줄씩 읽어들임
//	    	  strOutput = reader.readLine();
//
//	    	  if (strOutput != null)
//	    	  {
//	    		  simpleProgressBar.setStatus(strOutput, true);
//	    	  }
//	    	  if (strOutput == null)
//	    	  {
//	    		  p.destroy();
//	    		  break;
//	    	  }
//	      }
//	      simpleProgressBar.setStatus("LOV Import is end..."	, false);
//		  simpleProgressBar.close("the end", false);
//	      
//	} catch (Exception e) {
//		e.printStackTrace();
//	} 
//  }

	//progress bar 수정
	void executeLogMessage()
	{
		final TransOperation initOp = new TransOperation();
		initOp.addOperationListener(new InterfaceAIFOperationListener()
		{
			@Override
			public void startOperation(String arg0)
			{
			}

			@Override
			public void endOperation()
			{

			}
		});
		session.queueOperation(initOp);
	}

	public class TransOperation extends AbstractAIFOperation
	{
		public TransOperation()
		{
		}

		@Override
		public void executeOperation() throws Exception
		{
			FileWriter fw = null;

			try
			{
				/** 파일 객체 생성 */
				File tmpDir = new File(xmlDownloadPath);
				File batFile = new File(tmpDir, "updateTmpBatch.bat");
				if (!batFile.exists())
				{
					batFile.createNewFile();
				}

				fw = new FileWriter(batFile);
				fw.write("SET TC_ROOT=D:\\SIEMENS\\TC10\r\n");
				fw.write("SET TC_DATA=Y:\\tcdata10\r\n");
				fw.write("call %TC_DATA%\\tc_profilevars.bat\r\n\r\n");

				/** bmide_mamage_batch_lovs.bat 파일을 실행하기 위한 명령어를 받아옴 */
				String exportFile = xmlDownloadPath + newXMLFile + strXMLExtension;
				String command = getExecuteBatch(false, exportFile);
				fw.write(command);
				fw.close();

				/** Progress bar 실행 */
				WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
				simpleProgressBar.setWindowSize(500, 300);
				simpleProgressBar.start();
				simpleProgressBar.setStatus("LOV Import is start...", true);

				/** 배치파일 실행 */
				String[] cmd = { "CMD", "/C", batFile.getPath() };
				Process p = Runtime.getRuntime().exec(cmd);

				// 외부 프로그램에 대한 InputStream 을 생성
				DataInputStream inputstream = new DataInputStream(p.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

				String strOutput = "";
				while (true)
				{
					// 외부 프로그램이 출력하는 메세지를 한줄씩 읽어들임
					strOutput = reader.readLine();

					if (strOutput != null)
					{
						simpleProgressBar.setStatus(strOutput, true);
					}
					if (strOutput == null)
					{
						p.destroy();
						break;
					}
				}
				simpleProgressBar.setStatus("LOV Import is end...", false);
				simpleProgressBar.close("the end", false);

			} catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	/**
	 * 오늘 날짜를 20130101 or 20130101HHmm형태로 변환시켜 리턴한다.
	 * 
	 * @param isFolder
	 *            : true : 20130101 , false : 20130101HHmm
	 * @return
	 */
	public String getTodayDate(boolean isFolder)
	{
		Date date = new Date();
		SimpleDateFormat dateFormat = null;

		if (isFolder)
		{
			dateFormat = new SimpleDateFormat("yyyyMMdd");
		} else
		{
			dateFormat = new SimpleDateFormat("yyyyMMddHHmm");
		}

		return dateFormat.format(date).toString(); // 20130101
	}

	void changeTableData()
	{
		final int nSelected = lovTable.getSelectedRow();
		if (nSelected < 0)
			return;

		LovItem selectedLOVItem = lovList.get(nSelected);
		dataList = selectedLOVItem.getAllLovData();
		lovDataItemTableModel.fireTableDataChanged();
	}

	class TableSelectionListener implements ListSelectionListener
	{
		public void valueChanged(ListSelectionEvent e)
		{
			final int nSelected = lovTable.getSelectedRow();

			if (nSelected < 0)
				return;

			Thread lovDataLoad = new Thread()
			{
				public void run()
				{

					try
					{
						LovItem selectedLOVItem = lovList.get(nSelected);
						newXMLFile = selectedLOVItem.toString() + "_" + getTodayDate(false); // LOV name으로 xml 파일명을 만든다.
						strExportExcelFileName = selectedLOVItem.toString();
						newXMLFile_lang = newXMLFile + "_lang";

						dataList = selectedLOVItem.getAllLovData();
						lovDataItemTableModel.fireTableDataChanged();

						lovName = selectedLOVItem.strName;
					} catch (Exception e)
					{
						e.printStackTrace();
					}
				}
			};

			lovDataLoad.start();
		}
	}

	class LovItemTableModel extends AbstractTableModel
	{
		// Table Column Names
		public String[] cNames = { "List Of Value" };
		// Table Column Classes
		public Class[] colClasses = { String.class };

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정. 
		int sortBy = 0;
		int sortType = LovItemComparator.SORT_BY_CODE;
		int sortOrder = LovItemComparator.SORT_ASC;

		//------------------------------------------------------------------------------------------

		public int getColumnCount()
		{
			return cNames.length;
		}

		public int getRowCount()
		{
			return lovList.size();
		}

		public Object getValueAt(int row, int col)
		{

			return lovList.get(row).strName;
		}

		public String getColumnName(int column)
		{
			return cNames[column];
		}

		public Class getColumnClass(int c)
		{
			return colClasses[c];
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		/**
		 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
		 * sort method
		 */
		public void sort()
		{
			Collections.sort(lovList, new LovItemComparator(sortType, sortOrder));
		}// sort

		/**
		 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
		 * sort 기준 column 지정.
		 * 
		 * @param sortBy
		 */
		public void setSortBy(int sortBy)
		{
			if (this.sortBy == sortBy)
			{
				this.sortOrder = sortOrder * -1;
			} else
			{
				this.sortOrder = LovItemComparator.SORT_ASC;
				this.sortBy = sortBy;
			}// if
		}// setSortBy

	}

	class LovDataItemTableModel extends AbstractTableModel
	{
		// Table Column Names
		public String[] cNames = { "Key", "Display Name", "Description", "Status" };
		// Table Column Classes
		// [20140422] bskwak, column 명 클릭 시 정렬 기능 추가. 
		// 3개였던 것을 4개로 변경 Sort 기능을 쓰면 표기되지 않는 column도 모두 설정해야 하는 듯. 
		public Class[] colClasses = { String.class, String.class, String.class, String.class };

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정. 
		int sortBy = 0;
		int sortType = LovDataItemComparator.SORT_BY_CODE;
		int sortOrder = LovDataItemComparator.SORT_ASC;

		//------------------------------------------------------------------------------------------

		public int getColumnCount()
		{
			return cNames.length;
		}

		public int getRowCount()
		{
			return dataList.size();
		}

		public Object getValueAt(int row, int col)
		{

			return dataList.get(row).getData(col);
		}

		public String getColumnName(int column)
		{
			return cNames[column];
		}

		public Class getColumnClass(int c)
		{
			return colClasses[c];
		}

		public boolean isCellEditable(int row, int col)
		{
			return false;
		}

		/**
		 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
		 * sort method
		 * 
		 */
		public void sort()
		{
			Collections.sort(dataList, new LovDataItemComparator(sortBy, sortType, sortOrder));
		}// sort

		/**
		 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
		 * sort 기준 column 지정.
		 * 
		 * @param sortBy
		 */
		public void setSortBy(int sortBy)
		{
			if (this.sortBy == sortBy)
			{
				this.sortOrder = sortOrder * -1;
			} else
			{
				this.sortOrder = LovDataItemComparator.SORT_ASC;
				this.sortBy = sortBy;
			}// if
		}// setSortBy

	}

	public class LovDataTableCellRenderer extends DefaultTableCellRenderer
	{
		Object value;
		boolean isImageIcon = false;
		boolean isSelected = false;
		boolean isLightGray = false;

		public LovDataTableCellRenderer()
		{
		}

		/**
		 * Invoked as part of DefaultTableCellRenderers implemention. Sets the text of the label.
		 */
		public void setValue(Object value)
		{
			super.setValue(value);

			this.value = value;
		}

		/**
		 * Returns this.
		 */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
		{
			super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

			this.isSelected = isSelected;

			setForeground(Color.black);
			setBackground(Color.white);

			LovDataItem selectedData = dataList.get(row);

			if (selectedData.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_DELETE))
				setBackground(Color.RED);
			else if (selectedData.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_MODIFY))
				setBackground(Color.YELLOW);

			else if (selectedData.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_ADD))
				setBackground(Color.BLUE);

			// Tooltip 지정
			if (value != null && value.getClass().getName().endsWith("String"))
			{
				setToolTipText(value.toString());
			}

			// 선택 Background 지정
			if (isSelected)
			{
				setBackground(new Color(49, 106, 197));
			}

			return this;
		}

	}

	class LovItem
	{

		String strName = null;

		ArrayList<LovDataItem> lovData = null;

		LovItem(String strName)
		{

			this.strName = strName;
			this.lovData = new ArrayList<LovDataItem>();
		}

		void setLovData(LovDataItem data)
		{
			this.lovData.add(data);

		}

		LovDataItem getLovData(int nIndex)
		{
			return this.lovData.get(nIndex);
		}

		ArrayList<LovDataItem> getAllLovData()
		{
			return this.lovData;
		}

		public String toString()
		{
			return this.strName;
		}
	}

	class LovDataItem
	{

		public static final int INDEX_KEY = 0;
		public static final int INDEX_VALUE = 1;
		public static final int INDEX_DESC = 2;
		public static final int INDEX_STATUS = 3;

		public static final String STATUS_NORMAL = "";
		public static final String STATUS_ADD = "Need To Add";
		public static final String STATUS_MODIFY = "Need To Update";
		public static final String STATUS_DELETE = "Need To Delete";

		HashMap<Integer, String> extraData = null;

		LovDataItem()
		{
			this.extraData = new HashMap<Integer, String>();
		}

		void setData(int nIndex, String strValue)
		{
			this.extraData.put(nIndex, strValue);
		}

		String getData(int nKey)
		{
			if (this.extraData.containsKey(nKey))
			{
				return this.extraData.get(nKey);
			} else
			{
				return "";
			}
		}

		public String toString()
		{
			return this.getData(INDEX_KEY) + ":" + this.getData(INDEX_VALUE);
		}

	}

	class LovDataDialog extends AbstractAIFDialog
	{
		LovDataItem lovDataItem;

		JTextField keyField; // Key	
		JTextArea valueField; // Desc
		JTextField descField; // Display 명

		/**
		 * 신규생성인 경우
		 */
		LovDataDialog()
		{
			super(LovManagerDialog.this);

			this.setTitle("Create LOV Data");

			this.init();

			this.setModal(true);
			this.setResizable(false);
			this.setPreferredSize(new Dimension(300, 300));
			this.centerToScreen();
		}

		/**
		 * 신규생성인 경우
		 */
		LovDataDialog(String key, String seq)
		{

			this();
			this.init();

		}

		/**
		 * 수정인 경우
		 * 
		 * @param lovItem
		 */
		LovDataDialog(LovDataItem lovDataItem)
		{
			this();
			this.setTitle("Update LOV Data");
			this.lovDataItem = lovDataItem;
			this.keyField.setText(lovDataItem.getData(LovDataItem.INDEX_KEY));
			this.valueField.setText(lovDataItem.getData(LovDataItem.INDEX_DESC));
			this.descField.setText(lovDataItem.getData(LovDataItem.INDEX_VALUE));
			keyField.setEditable(false);
		}

		void init()
		{
			JPanel mainPanel = new JPanel(new BorderLayout());

			JPanel dataPanel = new JPanel(null);

			JLabel keyLabel = new JLabel("Key");
			JLabel descLabel = new JLabel("Display Name");
			JLabel valueLabel = new JLabel("Desc");

			this.keyField = new JTextField();
			this.descField = new JTextField();
			this.valueField = new JTextArea();

			keyLabel.setBounds(15, 15, 60, 20);
			keyField.setBounds(85, 15, 180, 20);

			valueLabel.setBounds(15, 75, 60, 20);
			valueField.setBounds(85, 75, 180, 50);

			descLabel.setBounds(15, 45, 60, 20);
			descField.setBounds(85, 45, 180, 20);

			dataPanel.add(keyLabel);
			dataPanel.add(keyField);
			dataPanel.add(descLabel);
			dataPanel.add(descField);
			dataPanel.add(valueLabel);
			dataPanel.add(valueField);

			JPanel btnPanel = new JPanel();
			JButton okBtn = new JButton("Ok");
			okBtn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ee)
				{
					okAction();

				}
			});

			JButton cancelBtn = new JButton("Cancel");
			cancelBtn.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent ee)
				{
					LovDataDialog.this.dispose();
				}
			});

			keyField.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent e)
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						okAction();
					}
				}
			});

			valueField.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent e)
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						okAction();
					}
				}
			});

			descField.addKeyListener(new KeyAdapter()
			{
				@Override
				public void keyPressed(KeyEvent e)
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						okAction();
					}
				}
			});

			btnPanel.add(okBtn);
			btnPanel.add(cancelBtn);

			mainPanel.add(dataPanel, BorderLayout.CENTER);
			mainPanel.add(btnPanel, BorderLayout.SOUTH);

			this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		}

		public void okAction()
		{
			if ("".equals(keyField.getText().trim()))
			{
//          MessageBox.post(LovDataDialog.this, "Key Field가 공란입니다.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.KeyField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}
			if ("".equals(valueField.getText().trim()))
			{
//          MessageBox.post(LovDataDialog.this, "Desc Field가 공란입니다.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.ValueField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}
			if ("".equals(descField.getText().trim()))
			{
//          MessageBox.post(LovDataDialog.this, "Display명 Field가 공란입니다.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.DisplayField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}

			// 생성인 경우
			if (lovDataItem == null)
			{
				LovDataItem dataItem = new LovDataItem();
				dataItem.setData(LovDataItem.INDEX_KEY, keyField.getText());
				dataItem.setData(LovDataItem.INDEX_VALUE, descField.getText());
				dataItem.setData(LovDataItem.INDEX_DESC, valueField.getText());
				dataItem.setData(LovDataItem.INDEX_STATUS, LovDataItem.STATUS_ADD);

				int nSelected = dataTable.getSelectedRow();

				if (nSelected < 0)
					dataList.add(dataItem);
				else
					dataList.add(nSelected, dataItem);

			}
			// 수정인 경우
			else
			{
				lovDataItem.setData(LovDataItem.INDEX_KEY, keyField.getText());
				lovDataItem.setData(LovDataItem.INDEX_VALUE, descField.getText());
				lovDataItem.setData(LovDataItem.INDEX_DESC, valueField.getText());

				if (!lovDataItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_ADD))
					lovDataItem.setData(LovDataItem.INDEX_STATUS, LovDataItem.STATUS_MODIFY);
			}

			lovDataItemTableModel.fireTableDataChanged();
			LovDataDialog.this.dispose();
		}

	}

	/**
	 * 
	 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
	 * LovItemComparator class
	 * 소팅에 사용되는 Comparator Class
	 */
	class LovItemComparator implements Comparator
	{
		final static int SORT_BY_NUMBER = 0;
		final static int SORT_BY_CODE = 1;
		final static int SORT_BY_RESERVATION = 2;
		final static int SORT_ASC = 1;
		final static int SORT_DESC = -1;
		protected int sortType = SORT_BY_NUMBER;
		protected int sortOrder = SORT_ASC;

		public LovItemComparator(int sortType, int sortOrder)
		{
			this.sortType = sortType;
			this.sortOrder = sortOrder;
		}// 생성자

		public int compare(Object o1, Object o2)
		{
			int result = 0;
			// STEP 1. 객체 타입이 LovItem 인지 비교
			if (!(o1 instanceof LovManagerDialog.LovItem))
				return 0;
			if (!(o2 instanceof LovManagerDialog.LovItem))
				return 0;

			String str1 = ((LovManagerDialog.LovItem) o1).strName;
			String str2 = ((LovManagerDialog.LovItem) o2).strName;

			// STEP 2. 컬럼 종류에 따라서 대소를 비교한다.
			switch (sortType)
			{
				case SORT_BY_NUMBER:
					int i1 = Integer.parseInt(str1);
					int i2 = Integer.parseInt(str2);
					result = (i1 > i2) ? 1 : -1;
					break;
				case SORT_BY_CODE:
				case SORT_BY_RESERVATION:
					result = str1.compareTo(str2);
			}// switch

			// STEP 3. 소팅 방향을 적용한다.
			result *= sortOrder;
			return result;
		}// compare
	}

	/**
	 * 
	 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
	 * 소팅에 사용되는 Comparator Class
	 * 
	 * @author bs
	 * 
	 */
	class LovDataItemComparator implements Comparator
	{
		final static int SORT_BY_NUMBER = 0;
		final static int SORT_BY_CODE = 1;
		final static int SORT_BY_RESERVATION = 2;
		final static int SORT_ASC = 1;
		final static int SORT_DESC = -1;
		protected int sortBy = 0;
		protected int sortType = SORT_BY_NUMBER;
		protected int sortOrder = SORT_ASC;

		public LovDataItemComparator(int sortBy, int sortType, int sortOrder)
		{
			this.sortBy = sortBy;
			this.sortType = sortType;
			this.sortOrder = sortOrder;
		}// 생성자

		public int compare(Object o1, Object o2)
		{
			int result = 0;
			// STEP 1. 객체 타입이 LovItem 인지 비교
			if (!(o1 instanceof LovManagerDialog.LovDataItem))
				return 0;
			if (!(o2 instanceof LovManagerDialog.LovDataItem))
				return 0;

			String str1 = ((LovManagerDialog.LovDataItem) o1).getData(this.sortBy);
			String str2 = ((LovManagerDialog.LovDataItem) o2).getData(this.sortBy);

			// STEP 2. 컬럼 종류에 따라서 대소를 비교한다.
			switch (sortType)
			{
				case SORT_BY_NUMBER:
					int i1 = Integer.parseInt(str1);
					int i2 = Integer.parseInt(str2);
					result = (i1 > i2) ? 1 : -1;
					break;
				case SORT_BY_CODE:
				case SORT_BY_RESERVATION:
					result = str1.compareTo(str2);
			}// switch

			// STEP 3. 소팅 방향을 적용한다.
			result *= sortOrder;
			return result;
		}// compare
	}

	/**
	 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
	 * LovItemTableModel 용 header mouse adapter
	 * 
	 * @author bs
	 * 
	 */
	public class LovItemColumnHeaderMouseAdapter extends MouseAdapter
	{
		JTable table;

		public LovItemColumnHeaderMouseAdapter(JTable table)
		{
			this.table = table;
		}// 생성자

		public void mouseClicked(MouseEvent e)
		{
			// STEP 1. 어느 컬럼이 클릭되었는지 찾아낸다.
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

			if (modelIndex < 0)
				return;

			// STEP 2. 클릭된 컬럼에 따라 소팅 순서 및 소팅 기준을 변경한다.
			LovItemTableModel tableModel = (LovItemTableModel) table.getModel();
			tableModel.setSortBy(modelIndex);
			tableModel.sort();
			table.repaint();
		}// mouseClicked
	}

	/**
	 * [SR140513-015][20140512] bskwak, column 열 클릭 시 정렬 기능 오류 수정.
	 * LovDataItemTableModel 용 header mouse adapter
	 * 
	 * @author bs
	 * 
	 */
	public class LovDataColumnHeaderMouseAdapter extends MouseAdapter
	{
		JTable table;

		public LovDataColumnHeaderMouseAdapter(JTable table)
		{
			this.table = table;
		}// 생성자

		public void mouseClicked(MouseEvent e)
		{
			// STEP 1. 어느 컬럼이 클릭되었는지 찾아낸다.
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

			if (modelIndex < 0)
				return;

			// STEP 2. 클릭된 컬럼에 따라 소팅 순서 및 소팅 기준을 변경한다.
			LovDataItemTableModel tableModel = (LovDataItemTableModel) table.getModel();
			tableModel.setSortBy(modelIndex);
			tableModel.sort();
			table.repaint();
		}// mouseClicked
	}
}
