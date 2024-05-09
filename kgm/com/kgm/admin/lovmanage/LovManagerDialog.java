/**
 * LovManagerDialog.java
 * 
 * 1. bmide_manage_batch_lovs.bat Utility�? ?��?��?��?�� External Lov List XML�? 추출?��?��?��.
 * 2. 추출?�� Xml?�� Loading?��?�� ?��면에 ?��?�� ?��?��?��.
 * 3. �? LOV Data ?��?��/?��?��/?��?�� 기능?�� 구현?��?��?��.
 * 4. ?��?��/?��?��/?��?�� ?�� LOV Data�? Xml�? �??��?��?��?��.
 * 5. �??��?�� Xml?�� bmide_manage_batch_lovs.bat Utility�? ?��?��?��?�� TeamCenter?�� 반영?��?��?��.
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
 * [20140422][SR140401-044] bskwak, column �? ?���? ?�� ?��?�� 기능 추�?.
 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
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
	JTable lovTable = null; // LOV List�? ???��?�� Table
	/** Lov Data Table */
	JTable dataTable = null; // LOV?�� Data�? ???��?�� 리스?��
	/** Lov Table Model */
	LovItemTableModel lovItemTableModel = null; // LOV List
	/** Lov Data Table Model */
	LovDataItemTableModel lovDataItemTableModel = null; // LOV?�� Data List

	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document document;
	private Document document_lang;
	private String lovName = "";

	/**
	 * LOV 값을 BMIDE?��?�� ?��?��?���? ?���? Reference(?) ?��?���? �?리하�? ?��?�� �?�?.
	 * SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * call bmide_manage_batch_lovs.bat -u=infodba -p=infodba -g=dba -option=extract -file=C:/temp/lovname20130326/lov_values_201303261041.xml
	 * ?��?? 같이 ?��경Setting?�� ?�� ?�� bmide_manage_batch_lovs.bat ?�� ?��?��?���? �??��?�� 경로?�� xml?��?��?�� ?��?��(?��?���?.xml�? lang/?��?���?_lang.xml)?���?
	 * ?�� xml?��?��?��?�� LOV?�� List?? Value�? ?��?��.
	 * ?�� Class?�� ?��?��?�� LOV?�� ?��?�� 값을 추�?, ?��?��, ?��?��?���? 반영?���? ?��?�� Class?��.
	 * LOV ?��체�?? ?��?��?���??�� 못함.
	 */

	/**
	 * SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * ?�� 3개의 ?��?��?? C:\Tc9.properties.txt ?��?��?�� 반드?�� 기록?��?�� ?��?��?�� ?��?��.
	 */

	public static String TOP_NODE_ATTR_NAME_XMLNS = "xmlns";
	public static String TOP_NODE_ATTR_NAME_VERSION = "batchXSDVersion";

	/** xml ?��?�� ?��?��?�� 기본 ?��?���? ?��?�� �??�� **/
	private String topNodeAttrXmlns = ""; // xmlns="http://teamcenter.com/BusinessModel/TcBusinessData"
	private String topNodeAttrVersion = ""; // batchXSDVersion="1.0"

	/** lang/_lang.xml ?��?�� ?��?��?�� 기본 ?��?���? ?��?�� �??�� **/
	private String topNodeAttrXmlns_lang = ""; // xmlns="http://teamcenter.com/BusinessModel/TcBusinessDataLocalization"
	private String topNodeAttrVersion_lang = ""; // batchXSDVersion="1.0"

	/** xml?��?��?�� ?��?��받기 ?��?�� 경로 **/
	private String xmlDownloadPath = "C:/Temp/lovname"; // ex) C:\Temp\lovname20130326				 
	private String xmlDownloadPath_lang = "C:/Temp/lovname"; // ex) C:\Temp\lovname20130326\lang

	/** ?��?��받을 xml?��?���? **/
	private String oldXMLFile = "lov_values"; // BMIDE?��?�� export 받�? xml ?��?���?(lov_values_20130101)
	private String oldXMLFile_lang = ""; // BMIDE?��?�� export 받�? xml Lang ?��?���?(lov_values_20130101_lang)

	/** Import?�� xml?��?�� �?(?��?��, ?��?��, ?��?��?��?�� �?경사?�� ?��?��) **/
	private String newXMLFile = ""; // BMIDE�? import ?�� xml ?��?���? (XXX_20130101)
	private String newXMLFile_lang = ""; // BMIDE�? import ?�� xml Lang?��?���?(XXX_20130101)

	/** LOV Manager Table?�� Excel?��?���? Export **/
	private String strExportExcelFileName = ""; // Export?�� Excel?��?�� �?

	/** xml?��?�� ?��?��?�� **/
	private String strXMLExtension = ".xml"; // ?��?��?��

	public Registry registry;

	public ArrayList<LovDataItem> selectedList;

	public String infodbaPassword = "";

	/**
	 * ?��?��?��
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

		/** xml?��?�� download 경로 �??�� **/
		initDownloadDir();

		/** ?��버에 ?��?�� LOV값을 xml?��?���? �??��?��?��. */
		this.exportFile(); // LOV 값을 xml ?��?���? export?��?��.

		this.init();

		this.setResizable(true);
		// this.setSize(1000, 800);
		this.setPreferredSize(new Dimension(1000, 500));
		centerToScreen();

		this.loadLovList();

	}

	/**
	 * XML?��?��?�� ???��?�� ?��?��?�� ?��치�?? ?��?��?��?��.
	 */
	public void initDownloadDir()
	{
		xmlDownloadPath = xmlDownloadPath + getTodayDate(true).toString() + "/"; // ex) C:/Temp/lovname20130326	
		xmlDownloadPath_lang = xmlDownloadPath + "lang/"; // ex) C:/Temp/lovname20130326/lang/

		/** c:\temp\lovnameyyMMdd ?��?���? ?��?���? ?��?�� */
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
	 * executeTmpBatch.bat ?��?�� ?��?�� ?�� bmide_manage_batch_lovs.bat 배치�? ?��?��?��?��
	 * BMIDE?��?�� LOV 값을 XML ?��?���? export ?��?��.
	 * ?��?�� ?��?��?? lov_values_201303261041.xml ?��?���? ?��?��?��?��.
	 * C:\\Tc9.properties.txt ?��?�� : SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * 
	 * @throws Exception
	 */
	public void exportFile() throws Exception
	{

		/** bmide_manage_batch_lovs.bat 명령?�� ?��?��?���? ?��?�� ?��경�??��값이 ?��?�� ?��?�� */
//	   File pFile = new File("C:\\Tc9.properties.txt");
//	   
//	   if(!pFile.exists()) {
//		   String message = registry.getString("lovmanage.MESSAGE.Front") + pFile + registry.getString("lovmanage.MESSAGE.Back");
//		   throw new Exception(message);
//	   }

		WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
		simpleProgressBar.setWindowSize(500, 300);

		/** bmide_manage_batch_lovs.bat(-> XML?��?�� ?��?��?��) ?��?��?�� ?��?��?�� batch ?��?��?�� ?��?��?��?��. **/
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
		// [20240306] ?��?�� TC Server ?��경에 맞춰 경로 �?�? ?��?��
		fw.write("SET TC_ROOT=D:\\SIEMENS\\TC13\r\n");
		fw.write("SET TC_DATA=Y:\\tcdata10\r\n");
		fw.write("call %TC_DATA%\\tc_profilevars.bat\r\n\r\n");
//	   }
//	   s.close();

		/** oldXML ?��?���? ?��?�� */
		setOldXmlFileName();

		/** oldXML ?��?��?�� ?��??경로 �??�� */
		String exportFile = xmlDownloadPath + oldXMLFile + strXMLExtension; // ex) C:\Temp\lovname20130326\lov_values_201303261041.xml

		/** bmide_mamage_batch_lovs.bat ?��?��?�� ?��?��?���? ?��?�� 명령?���? 받아?�� */
		String command = getExecuteBatch(true, exportFile);
		fw.write(command);
		fw.close();

		/** ProgressBar ?��?�� */
		simpleProgressBar.start();
		simpleProgressBar.setStatus("LOV Export is start...", true);

		/** 배치 ?��?�� ?��?�� */
		String[] cmd = { "CMD", "/C", batFile.getPath() };
		Process p = Runtime.getRuntime().exec(cmd);

		/** ?���? ?��로그?��?�� ???�� InputStream ?�� ?��?�� */
		DataInputStream inputstream = new DataInputStream(p.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

		String strOutput = "";

		while (true)
		{
			// ?���? ?��로그?��?�� 출력?��?�� 메세�?�? ?��줄씩 ?��?��?��?��
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
	 * xml?��?��명을 ?��?��?��?��.
	 * xml : lov_values_201303261041
	 * xml_lang : lov_values_201303261041_lang
	 */
	public void setOldXmlFileName()
	{
		/** ?��?�� ?��짜�?? �??��?��(20130101) */
		String currentDate = getTodayDate(false);
		oldXMLFile = oldXMLFile + "_" + currentDate;
		oldXMLFile_lang = oldXMLFile + "_lang";
	}

	/**
	 * LOV Dialog�? 초기?��?��?��.
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
		// [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��. 
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

		// �? Column�? SellRenderer Setting
		for (int k = 0; k < lovDataItemTableModel.cNames.length; k++)
		{
			// Custom TableCellRenderer
			LovDataTableCellRenderer tableRenderer = new LovDataTableCellRenderer();
			TableColumn column = this.dataTable.getColumnModel().getColumn(k);
			column.setCellRenderer(tableRenderer);
		}

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��. 
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
				// ?��블클�? ?�� 경우, ?��?��?�� ?��?�� ?��?��?��면으�? ?��?��
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

		// LOV 추�?
		JButton plusBtn = new JButton("+");
		plusBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{
				final int nSelected = lovTable.getSelectedRow();
				if (nSelected < 0)
					return;
				LovItem selectedLOVItem = lovList.get(nSelected);
				// LOV 추�? Dialog
				LovDataDialog dlg = new LovDataDialog();

				dlg.setVisible(true);

			}
		});

		// LOV ?��?��
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
				/** ?��?��?�� LOV Data�? TeamCenter?�� ???��?��?��?��. */
				saveActionTC();
				/** Dialog?�� Status 값을 초기?��?��?��. */
				initStatus();
			}
		});

		/** ?��?�� LOV?�� 값을 Excel ?��?���? ???��?��?��. */
		JButton btnExcelExport = new JButton("Excel Export");
		btnExcelExport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{

				excelExport();
			}
		});

		/** 모든 LOV?�� 값을 Excel ?��?���? ???��?��?��. */
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
//        // 모든 LOV Data�? TeamCenter?�� ???��?��?��?��.
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
	 * XML ?��?��?�� parse?��?�� Document 객체�? ?��?��?��?��.
	 * 
	 * @param xmlFile
	 * @throws Exception
	 */
	public void openFile() throws Exception
	{
		// xml ?��?��?�� parse?��?�� document?�� ?��?��?��
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
			String message = "�??��?�� ?���?(" + path + ")?�� ?��?��?�� 존재?���? ?��?��?��?��.";
			throw new Exception(message);
		}
	}

	/**
	 * LOV Dialog?�� ?��보�?? Excel ?��?���? export?��?��.
	 */
	public void excelExport()
	{
		try
		{
			// 1. ?��?�� ???�� ???��?��?���? ?��?��?��
			JFileChooser fileChooser = new JFileChooser("C:/");

			/** ?��?�� ?��?��?��리에?�� xls ?��?���? 보이?���? ?��?��?��?��. */
			FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
			fileChooser.removeChoosableFileFilter(fileFilter);
			SimpleStructureFilter filterXLS = new SimpleStructureFilter("xls", "?��?? (.xls)");
			fileChooser.addChoosableFileFilter(filterXLS);

			// 2. �??��?�� ?��름으�? ?��?? ?��?��?�� ?��?��?��?��
			File file = new File("C:/" + strExportExcelFileName + ".xls");
			fileChooser.setSelectedFile(file);

			if (fileChooser.showSaveDialog(AIFDesktop.getActiveDesktop().getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = fileChooser.getSelectedFile();

				if (selectedFile != null)
				{
					String strExtension = getExtension(selectedFile);
					strExtension = strExtension == null ? "" : strExtension;

					// ?��?��?���? ?��?��?�� ?��?��명에 xls ?��?��?��까�? ?��?�� ?��?��?�� 경우
					if (!strExtension.equalsIgnoreCase("xls"))
					{
						selectedFile = new File(selectedFile.getAbsolutePath() + ".xls");
					}

					// ?��기�??��?�� ?��?? Workbook 객체 ?��?��
					WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);

					/** LOV 값으�? Excel ?��?��?�� Write?��?��. */
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
	 * LOV Dialog?�� 모든 LOV값을 Excel?��?���? ???��?��?��.
	 * LOV Name?? 각각?�� Sheet명이 ?��?��.
	 */
	public void excelFullExport()
	{
		try
		{
			// 1. ?��?�� ???�� ???��?��?���? ?��?��?��
			JFileChooser fileChooser = new JFileChooser("C:/");
			FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
			fileChooser.removeChoosableFileFilter(fileFilter);
			SimpleStructureFilter filterXLS = new SimpleStructureFilter("xls", "?��?? (.xls)");
			fileChooser.addChoosableFileFilter(filterXLS);

			// 2. �??��?�� ?��름으�? ?��?? ?��?��?�� ?��?��?��?��
			File file = new File("C:/LOV_All_List_" + getTodayDate(true) + ".xls");
			fileChooser.setSelectedFile(file);

			if (fileChooser.showSaveDialog(AIFDesktop.getActiveDesktop().getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = fileChooser.getSelectedFile();

				if (selectedFile != null)
				{
					// ?��?��?�� ?��?��?���? return?��?��.
					String strExtension = getExtension(selectedFile);
					strExtension = strExtension == null ? "" : strExtension;

					// ?��?��?���? ?��?��?�� ?��?��명에 xls ?��?��?��까�? ?��?�� ?��?��?�� 경우
					if (!strExtension.equalsIgnoreCase("xls"))
					{
						selectedFile = new File(selectedFile.getAbsolutePath() + ".xls");
						strExtension = "xls";
					}

					// ?��기�??��?�� ?��?? Workbook 객체 ?��?��
					WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);

					/** LOV 명을 각각?�� Sheet Name?���? �??��?���?, LOV 값을 Excel?�� Write?��?��. */
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

				/** Excel ?��?��?�� ?��?��?��?��. **/
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
			}
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * LOV Dialog?�� JTable ?�� ?��?��?�� XLS ?��맷으�? ???��?��?��.
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
			// ?��기�??��?�� ?��?? Workbook 객체 ?��?��
//    	WritableWorkbook workBook = null;
//      if(sheetNum == 0) {
//    	  workBook = Workbook.createWorkbook(selectedFile);
//      } else {
//    	  Workbook workBook1 = Workbook.getWorkbook(selectedFile);
//    	  workBook = Workbook.createWorkbook(selectedFile, workBook1);
//      }

			// n번째 Sheet ?��?��
			WritableSheet sheet = workBook.createSheet(strItemKey, sheetNum);

			SheetSettings printSet = sheet.getSettings();
			printSet.setFitWidth(1);
			printSet.setFitToPages(true);
			printSet.setOrientation(PageOrientation.LANDSCAPE);

			// 1. ?��?�� ?���? �??��?���?
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

			// 2. ?���? Row ?���? �??��?���?
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
	 * jxl ?�� Cell Format ?�� ?��?��?��?��.
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
	 * ?��?��?�� ?��?��?���? Return
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
	 * LOV List�? Loading?��?��?��.
	 * 1. bmide_manage_batch_lovs.bat Utility�? ?��?��?��?�� Xml?�� ?��?��?��?��?��.
	 * 2. XML?�� Loading?��?�� LOV/LOVData�? Loading?��?��?��.
	 */
	void loadLovList()
	{
		Thread lovLoad = new Thread()
		{
			public void run()
			{
				try
				{

					/** Document 객체�? ?��?��?��?��. */
					openFile();

					/** newXML ?��?��?�� ?��?��?���? ?��?�� oldXML ?��?��?�� Node값을 ?��?��?��?��. **/
					setDefaultXML();

					/** newXML_lang ?��?��?�� ?��?��?���? ?��?�� oldXML ?��?��?�� Node값을 ?��?��?��?��. **/
					setDefaultXMLForLang();

					Element rootElement = document.getDocumentElement();

					NodeList memberList = rootElement.getElementsByTagName("TcLOV");

					// LOV?�� 값을 구한?��. - 1 Level
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
							// ?��?��?�� TCLov?�� List값을 구한?��. - 2 Level
							for (int k = 0; k < childNodeList.getLength(); k++)
							{ // for 3
								Node childNode = childNodeList.item(k);

								if ((childNode.getNodeName()).equals("TcLOVValue"))
								{ // LOV?�� Key, Value�?
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

									// ?��기에?�� XML_Lang?�� desc 값을 구한?��.
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
	 * import?�� xml ?��?��?�� ?��?��?���? ?��?�� Xmlns ?? batchXSDVersion 값을 lov_values_yyyyMMdd.xml ?��?�� �??��?��?��.
	 * 
	 * @throws Exception
	 */
	public void setDefaultXML()
	{
		Element rootElement = document.getDocumentElement();

		// XML ?��?��?��?�� xmlns ?? batchXSDVersion ?��보�?? �??��?��?��.
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
	 * import?�� xml ?��?��?�� ?��?��?���? ?��?�� Xmlns ?? batchXSDVersion 값을 lov_values_yyyyMMdd_Lang.xml ?��?�� �??��?��?��.
	 * download?�� XML_Lang ?��?��?�� 기본 ?��보�?? ?��?��?�� ???��?��?��.
	 */
	public void setDefaultXMLForLang() throws SAXException, IOException
	{
		Element rootElement_lang = document_lang.getDocumentElement();

		// XML_lang ?��?��?��?�� xmlns ?? batchXSDVersion ?��보�?? �??��?��?��.
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
	 * XML_Lang ?��?��?��?�� ?��?�� LOV?�� Description 값을 ?��?��?��?��.
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

		// LOV?�� 값을 구한?��. - 1 Level
		for (int i = 0; i < memberList.getLength(); i++)
		{ // for 1
			Node node = memberList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{

				NodeList childNodeList = node.getChildNodes();
				// ?��?��?�� TCLov?�� List값을 구한?��. - 2 Level
				for (int k = 0; k < childNodeList.getLength(); k++)
				{ // for 3
					Node childNode = childNodeList.item(k);

					if ((childNode.getNodeName()).equals("key"))
					{ // LOV?�� Key, Value�?
						NamedNodeMap childAttrs = childNode.getAttributes();

						for (int y = 0; y < childAttrs.getLength(); y++)
						{ // for 4
							if ((childAttrs.item(y).getNodeName()).equals("id"))
							{
								//[20190225 kch] xml ?��?���? �??�� lov List ?���? read ?�� bug 개선 ( contains -> equals )
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
	 * 모든 LOV Data�? TeamCenter?�� ???��?��?��?��.
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
						// ?��?�� LOV�? 강제 ?��?��?��?��?��.
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
	 * XML Import ?�� dialog?�� ?��?��값을 초기?��?��?��.
	 */
	void initStatus()
	{
		// STATUS_DELETE ?�� table?��?�� ?��?��?��?��.
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem selectedLOVItem = dataList.get(i);

			if (selectedLOVItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_DELETE))
			{
				dataList.remove(i);
			}
		}

		// table?�� Status값을 초기?��?��?��.
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem selectedLOVItem = dataList.get(i);

			selectedLOVItem.setData(LovDataItem.INDEX_STATUS, LovDataItem.STATUS_NORMAL);
		}
		lovDataItemTableModel.fireTableDataChanged();
	}

	/**
	 * ?��?��?�� LOV Data�? TeamCenter?�� ???��?��?��?��.
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

		// xml ?��?�� ?��?��
		writeXML();
		executeLogMessage();
	}

	void writeXML()
	{
		/** document 객체 ?��?�� */
		org.jdom.Document doc = new org.jdom.Document();
		org.jdom.Document doc_Lang = new org.jdom.Document();

		/** NameSpace ?��?�� */
		org.jdom.Namespace nameSpace = org.jdom.Namespace.getNamespace(topNodeAttrXmlns);
		org.jdom.Namespace nameSpace_Lang = org.jdom.Namespace.getNamespace(topNodeAttrXmlns_lang);

		/** Element 객체 ?��?�� */
		org.jdom.Element topElement = new org.jdom.Element("TcBusinessData", nameSpace); // TcBusinessData
		org.jdom.Element changeElement = new org.jdom.Element("Change", nameSpace); // <Change>
		org.jdom.Element tclovElement = new org.jdom.Element("TcLOV", nameSpace); // <TcLOV
		org.jdom.Element topElement_Lang = new org.jdom.Element("TcBusinessDataLocalization", nameSpace_Lang); // TcBusinessDataLocalization
		org.jdom.Element addElement_Lang = new org.jdom.Element("Add", nameSpace_Lang); // Add

		// ?��?�� ?���?
		Date currentDate = new Date();

		topElement.setAttribute(TOP_NODE_ATTR_NAME_VERSION, topNodeAttrVersion);
		topElement.setAttribute("Date", currentDate.toString());

		// Change Tag ?��?��
		tclovElement.setAttribute("name", lovName);
		tclovElement.setAttribute("usage", "Exhaustive");
		tclovElement.setAttribute("lovType", "ListOfValuesString");
		tclovElement.setAttribute("isManagedExternally", "true");

		// XML_Lang ?��?�� ?��?��
		topElement_Lang.setAttribute(TOP_NODE_ATTR_NAME_VERSION, topNodeAttrVersion_lang);
		topElement_Lang.setAttribute("Date", currentDate.toString());

		/** xml ?��?�� �? lang.xml ?��?��?�� LOVValue tag �?�? set */
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

			//�? ?��리먼?��?�� 배치 ?��?�� (?��리먼?��?�� ?��?�� ?��?���? 추�??�� ?��?�� )(?��?�� 주석??�? 배치�? ?��줘야 ?��?��).  addContent()메서?���? ?��?��?��?��.
			tclovElement.addContent(valueElement);
			addElement_Lang.addContent(keyElement);
		}

		/** lang.xml ?��?��?�� LOVValueDescription �?�? set */
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

			//�? ?��리먼?��?�� 배치 ?��?�� (?��리먼?��?�� ?��?�� ?��?���? 추�??�� ?��?�� )(?��?�� 주석??�? 배치�? ?��줘야 ?��?��).  addContent()메서?���? ?��?��?��?��.
			addElement_Lang.addContent(keyDescElement);
		}

		changeElement.addContent(tclovElement);
		topElement.addContent(changeElement);
		topElement_Lang.addContent(addElement_Lang);

		// 마�?막으�? Document?�� 최상?�� Element�? ?��?��?��?��.
		doc.setRootElement(topElement);
		doc_Lang.setRootElement(topElement_Lang);

		// ?��?���? ???��?���? ?��?��?�� XMLOutputter 객체�? ?��?��?��?��
		XMLOutputter xout = new XMLOutputter();

		// 기본 ?���? ?��?���? 불러?? ?��?��?��?��.
		Format fm = xout.getFormat();

		/** ?���??�� �??��?�� ?��?�� encodeing ?��?���? UTF-8�? �?경한?��. */
//      fm.setEncoding("euc-kr");
		fm.setEncoding("UTF-8");

		// �?�?, ?��?�� ?��그�?? 구별?���? ?��?�� ?�� 범위�? ?��?��?��.
		fm.setIndent("   ");
		//?��그끼�? 줄바꿈을 �??��?��?��.
		fm.setLineSeparator("\r\n");

		// ?��?��?�� XML ?��?��?�� ?��맷을 set?��?��.
		xout.setFormat(fm);
		try
		{
//    	  xout.output(doc, new FileWriter(strExportPath + strImportXMLFileName + strXMLExtension));
//    	  xout.output(doc_Lang, new FileWriter(strExportLangPath + strImportXMLLangFileName + strXMLExtension));

			/** XML ?��?�� ???��?�� UTF-8�? ?��?�� ?��기에 FileOutputStream?���? ???��?��.(FileWriter?�� UTF-8�? ???��?�� ?��?��) */
			xout.output(doc, new FileOutputStream(xmlDownloadPath + newXMLFile + strXMLExtension));
			xout.output(doc_Lang, new FileOutputStream(xmlDownloadPath_lang + newXMLFile_lang + strXMLExtension));

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * bmide_mamage_batch_lovs.bat ?��?��?�� ?��?��?���? ?��?�� ?��?��미터 값을 리턴?��?��.
	 * 
	 * @param isExport
	 *            : true?���? extract(XML ?��?�� Export), false?���? update(XML ?��?�� Import)
	 * @param file
	 *            : xml ?��?��?�� full path
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
	 * ?��?��?�� LOV 값을 ?��버로 Export?��?��.
	 */
//  void executeLogMessage(){
//	  File pFile = new File("C:\\Tc9.properties.txt");		// ?�� ?��?��?? ?��?�� 존재?��?�� ?��?��.
//	  FileWriter fw = null;
//	  
//	  try {
//		  if(!pFile.exists()) {
////			   String message = "�??��?�� ?���?(" + pFile + ")?�� ?��?��?�� 존재?���? ?��?��?��?��.";
//			  String message = registry.getString("lovmanage.MESSAGE.Front") + pFile + registry.getString("lovmanage.MESSAGE.Back");
//			  MessageBox.post(message, "?���?", MessageBox.INFORMATION);
//			  return;
//		  }
//		  
//		  /** ?��?�� 객체 ?��?�� */
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
//		  /** bmide_mamage_batch_lovs.bat ?��?��?�� ?��?��?���? ?��?�� 명령?���? 받아?�� */
//		  String exportFile = xmlDownloadPath + newXMLFile + strXMLExtension;
//		  String command = getExecuteBatch(false, exportFile);
//		  fw.write(command);
//		  s.close();
//		  fw.close();
//
//		  /** Progress bar ?��?�� */
//		  WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
//		  simpleProgressBar.setWindowSize(500, 300);
//		  simpleProgressBar.start();
//		  simpleProgressBar.setStatus("LOV Import is start..."	, true);
//		  
//		  /** 배치?��?�� ?��?�� */
//		  String[] cmd = { "CMD", "/C", batFile.getPath() };
//		  Process p = Runtime.getRuntime().exec(cmd);
//	  
//		  // ?���? ?��로그?��?�� ???�� InputStream ?�� ?��?��
//	      DataInputStream inputstream = new DataInputStream(p.getInputStream());
//	      BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
//	      
//	      String strOutput = "";
//	      while (true)
//	      {
//	    	  // ?���? ?��로그?��?�� 출력?��?�� 메세�?�? ?��줄씩 ?��?��?��?��
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

	//progress bar ?��?��
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
				/** ?��?�� 객체 ?��?�� */
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

				/** bmide_mamage_batch_lovs.bat ?��?��?�� ?��?��?���? ?��?�� 명령?���? 받아?�� */
				String exportFile = xmlDownloadPath + newXMLFile + strXMLExtension;
				String command = getExecuteBatch(false, exportFile);
				fw.write(command);
				fw.close();

				/** Progress bar ?��?�� */
				WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
				simpleProgressBar.setWindowSize(500, 300);
				simpleProgressBar.start();
				simpleProgressBar.setStatus("LOV Import is start...", true);

				/** 배치?��?�� ?��?�� */
				String[] cmd = { "CMD", "/C", batFile.getPath() };
				Process p = Runtime.getRuntime().exec(cmd);

				// ?���? ?��로그?��?�� ???�� InputStream ?�� ?��?��
				DataInputStream inputstream = new DataInputStream(p.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

				String strOutput = "";
				while (true)
				{
					// ?���? ?��로그?��?�� 출력?��?�� 메세�?�? ?��줄씩 ?��?��?��?��
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
	 * ?��?�� ?��짜�?? 20130101 or 20130101HHmm?��?���? �??��?���? 리턴?��?��.
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
						newXMLFile = selectedLOVItem.toString() + "_" + getTodayDate(false); // LOV name?���? xml ?��?��명을 만든?��.
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
		// [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��. 
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
		 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
		 * sort method
		 */
		public void sort()
		{
			Collections.sort(lovList, new LovItemComparator(sortType, sortOrder));
		}// sort

		/**
		 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
		 * sort 기�? column �??��.
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
		// [20140422] bskwak, column �? ?���? ?�� ?��?�� 기능 추�?. 
		// 3개�??�� 것을 4개로 �?�? Sort 기능?�� ?���? ?��기되�? ?��?�� column?�� 모두 ?��?��?��?�� ?��?�� ?��. 
		public Class[] colClasses = { String.class, String.class, String.class, String.class };

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��. 
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
		 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
		 * sort method
		 * 
		 */
		public void sort()
		{
			Collections.sort(dataList, new LovDataItemComparator(sortBy, sortType, sortOrder));
		}// sort

		/**
		 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
		 * sort 기�? column �??��.
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

			// Tooltip �??��
			if (value != null && value.getClass().getName().endsWith("String"))
			{
				setToolTipText(value.toString());
			}

			// ?��?�� Background �??��
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
		JTextField descField; // Display �?

		/**
		 * ?��규생?��?�� 경우
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
		 * ?��규생?��?�� 경우
		 */
		LovDataDialog(String key, String seq)
		{

			this();
			this.init();

		}

		/**
		 * ?��?��?�� 경우
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
//          MessageBox.post(LovDataDialog.this, "Key Field�? 공�??��?��?��.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.KeyField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}
			if ("".equals(valueField.getText().trim()))
			{
//          MessageBox.post(LovDataDialog.this, "Desc Field�? 공�??��?��?��.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.ValueField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}
			if ("".equals(descField.getText().trim()))
			{
//          MessageBox.post(LovDataDialog.this, "Display�? Field�? 공�??��?��?��.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.DisplayField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}

			// ?��?��?�� 경우
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
			// ?��?��?�� 경우
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
	 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
	 * LovItemComparator class
	 * ?��?��?�� ?��?��?��?�� Comparator Class
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
		}// ?��?��?��

		public int compare(Object o1, Object o2)
		{
			int result = 0;
			// STEP 1. 객체 ???��?�� LovItem ?���? 비교
			if (!(o1 instanceof LovManagerDialog.LovItem))
				return 0;
			if (!(o2 instanceof LovManagerDialog.LovItem))
				return 0;

			String str1 = ((LovManagerDialog.LovItem) o1).strName;
			String str2 = ((LovManagerDialog.LovItem) o2).strName;

			// STEP 2. 컬럼 종류?�� ?��?��?�� ???���? 비교?��?��.
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

			// STEP 3. ?��?�� 방향?�� ?��?��?��?��.
			result *= sortOrder;
			return result;
		}// compare
	}

	/**
	 * 
	 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
	 * ?��?��?�� ?��?��?��?�� Comparator Class
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
		}// ?��?��?��

		public int compare(Object o1, Object o2)
		{
			int result = 0;
			// STEP 1. 객체 ???��?�� LovItem ?���? 비교
			if (!(o1 instanceof LovManagerDialog.LovDataItem))
				return 0;
			if (!(o2 instanceof LovManagerDialog.LovDataItem))
				return 0;

			String str1 = ((LovManagerDialog.LovDataItem) o1).getData(this.sortBy);
			String str2 = ((LovManagerDialog.LovDataItem) o2).getData(this.sortBy);

			// STEP 2. 컬럼 종류?�� ?��?��?�� ???���? 비교?��?��.
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

			// STEP 3. ?��?�� 방향?�� ?��?��?��?��.
			result *= sortOrder;
			return result;
		}// compare
	}

	/**
	 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
	 * LovItemTableModel ?�� header mouse adapter
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
		}// ?��?��?��

		public void mouseClicked(MouseEvent e)
		{
			// STEP 1. ?��?�� 컬럼?�� ?���??��?��?���? 찾아?��?��.
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

			if (modelIndex < 0)
				return;

			// STEP 2. ?���??�� 컬럼?�� ?��?�� ?��?�� ?��?�� �? ?��?�� 기�??�� �?경한?��.
			LovItemTableModel tableModel = (LovItemTableModel) table.getModel();
			tableModel.setSortBy(modelIndex);
			tableModel.sort();
			table.repaint();
		}// mouseClicked
	}

	/**
	 * [SR140513-015][20140512] bskwak, column ?�� ?���? ?�� ?��?�� 기능 ?���? ?��?��.
	 * LovDataItemTableModel ?�� header mouse adapter
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
		}// ?��?��?��

		public void mouseClicked(MouseEvent e)
		{
			// STEP 1. ?��?�� 컬럼?�� ?���??��?��?���? 찾아?��?��.
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

			if (modelIndex < 0)
				return;

			// STEP 2. ?���??�� 컬럼?�� ?��?�� ?��?�� ?��?�� �? ?��?�� 기�??�� �?경한?��.
			LovDataItemTableModel tableModel = (LovDataItemTableModel) table.getModel();
			tableModel.setSortBy(modelIndex);
			tableModel.sort();
			table.repaint();
		}// mouseClicked
	}
}
