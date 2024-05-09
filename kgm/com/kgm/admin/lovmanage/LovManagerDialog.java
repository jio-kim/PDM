/**
 * LovManagerDialog.java
 * 
 * 1. bmide_manage_batch_lovs.bat Utilityë¥? ?‚¬?š©?•˜?—¬ External Lov List XMLë¡? ì¶”ì¶œ?•©?‹ˆ?‹¤.
 * 2. ì¶”ì¶œ?œ Xml?„ Loading?•˜?—¬ ?™”ë©´ì— ?‘œ?‹œ ?•©?‹ˆ?‹¤.
 * 3. ê°? LOV Data ?ƒ?„±/?ˆ˜? •/?‚­? œ ê¸°ëŠ¥?„ êµ¬í˜„?•©?‹ˆ?‹¤.
 * 4. ?ƒ?„±/?ˆ˜? •/?‚­? œ ?œ LOV Dataë¥? Xmlë¡? ë³??™˜?•©?‹ˆ?‹¤.
 * 5. ë³??™˜?œ Xml?„ bmide_manage_batch_lovs.bat Utilityë¥? ?‚¬?š©?•˜?—¬ TeamCenter?— ë°˜ì˜?•©?‹ˆ?‹¤.
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
 * [20140422][SR140401-044] bskwak, column ëª? ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ì¶”ê?.
 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
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
	JTable lovTable = null; // LOV Listë¥? ???¥?•  Table
	/** Lov Data Table */
	JTable dataTable = null; // LOV?˜ Dataë¥? ???¥?•  ë¦¬ìŠ¤?Š¸
	/** Lov Table Model */
	LovItemTableModel lovItemTableModel = null; // LOV List
	/** Lov Data Table Model */
	LovDataItemTableModel lovDataItemTableModel = null; // LOV?˜ Data List

	private DocumentBuilderFactory dbf;
	private DocumentBuilder db;
	private Document document;
	private Document document_lang;
	private String lovName = "";

	/**
	 * LOV ê°’ì„ BMIDE?—?„œ ?ƒ?„±?•˜ì§? ?•Šê³? Reference(?) ?˜•?ƒœë¡? ê´?ë¦¬í•˜ê¸? ?œ„?•œ ë¶?ë¶?.
	 * SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * call bmide_manage_batch_lovs.bat -u=infodba -p=infodba -g=dba -option=extract -file=C:/temp/lovname20130326/lov_values_201303261041.xml
	 * ?œ„?? ê°™ì´ ?™˜ê²½Setting?„ ?•œ ?›„ bmide_manage_batch_lovs.bat ?„ ?‹¤?–‰?•˜ë©? ì§?? •?•œ ê²½ë¡œ?— xml?ŒŒ?¼?´ ?ƒ?„±(?ŒŒ?¼ëª?.xmlê³? lang/?ŒŒ?¼ëª?_lang.xml)?˜ê³?
	 * ?´ xml?ŒŒ?¼?•ˆ?— LOV?˜ List?? Valueê°? ?ˆ?Š´.
	 * ?´ Class?Š” ?´?Ÿ¬?•œ LOV?— ?‹¤? œ ê°’ì„ ì¶”ê?, ?ˆ˜? •, ?‚­? œ?•˜ê³? ë°˜ì˜?•˜ê¸? ?œ„?•œ Class?„.
	 * LOV ?ì²´ë?? ?ƒ?„±?•˜ì§??Š” ëª»í•¨.
	 */

	/**
	 * SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * ?œ„ 3ê°œì˜ ?¼?¸?? C:\Tc9.properties.txt ?ŒŒ?¼?— ë°˜ë“œ?‹œ ê¸°ë¡?˜?–´ ?ˆ?–´?•¼ ?•œ?‹¤.
	 */

	public static String TOP_NODE_ATTR_NAME_XMLNS = "xmlns";
	public static String TOP_NODE_ATTR_NAME_VERSION = "batchXSDVersion";

	/** xml ?ŒŒ?¼ ?ƒ?‹¨?˜ ê¸°ë³¸ ? •?˜ë¥? ?œ„?•œ ë³??ˆ˜ **/
	private String topNodeAttrXmlns = ""; // xmlns="http://teamcenter.com/BusinessModel/TcBusinessData"
	private String topNodeAttrVersion = ""; // batchXSDVersion="1.0"

	/** lang/_lang.xml ?ŒŒ?¼ ?ƒ?‹¨?˜ ê¸°ë³¸ ? •?˜ë¥? ?œ„?•œ ë³??ˆ˜ **/
	private String topNodeAttrXmlns_lang = ""; // xmlns="http://teamcenter.com/BusinessModel/TcBusinessDataLocalization"
	private String topNodeAttrVersion_lang = ""; // batchXSDVersion="1.0"

	/** xml?ŒŒ?¼?„ ?‚´? ¤ë°›ê¸° ?œ„?•œ ê²½ë¡œ **/
	private String xmlDownloadPath = "C:/Temp/lovname"; // ex) C:\Temp\lovname20130326				 
	private String xmlDownloadPath_lang = "C:/Temp/lovname"; // ex) C:\Temp\lovname20130326\lang

	/** ?‚´? ¤ë°›ì„ xml?ŒŒ?¼ëª? **/
	private String oldXMLFile = "lov_values"; // BMIDE?—?„œ export ë°›ì? xml ?ŒŒ?¼ëª?(lov_values_20130101)
	private String oldXMLFile_lang = ""; // BMIDE?—?„œ export ë°›ì? xml Lang ?ŒŒ?¼ëª?(lov_values_20130101_lang)

	/** Import?•  xml?ŒŒ?¼ ëª?(?ƒ?„±, ?ˆ˜? •, ?‚­? œ?“±?˜ ë³?ê²½ì‚¬?•­ ? ?š©) **/
	private String newXMLFile = ""; // BMIDEë¡? import ?•  xml ?ŒŒ?¼ëª? (XXX_20130101)
	private String newXMLFile_lang = ""; // BMIDEë¡? import ?•  xml Lang?ŒŒ?¼ëª?(XXX_20130101)

	/** LOV Manager Table?„ Excel?ŒŒ?¼ë¡? Export **/
	private String strExportExcelFileName = ""; // Export?•  Excel?ŒŒ?¼ ëª?

	/** xml?ŒŒ?¼ ?™•?¥? **/
	private String strXMLExtension = ".xml"; // ?™•?¥?

	public Registry registry;

	public ArrayList<LovDataItem> selectedList;

	public String infodbaPassword = "";

	/**
	 * ?ƒ?„±?
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

		/** xml?ŒŒ?¼ download ê²½ë¡œ ì§?? • **/
		initDownloadDir();

		/** ?„œë²„ì— ?ˆ?Š” LOVê°’ì„ xml?ŒŒ?¼ë¡? ê°?? ¸?˜¨?‹¤. */
		this.exportFile(); // LOV ê°’ì„ xml ?ŒŒ?¼ë¡? export?•œ?‹¤.

		this.init();

		this.setResizable(true);
		// this.setSize(1000, 800);
		this.setPreferredSize(new Dimension(1000, 500));
		centerToScreen();

		this.loadLovList();

	}

	/**
	 * XML?ŒŒ?¼?´ ???¥?  ?´?”?˜ ?œ„ì¹˜ë?? ?ƒ?„±?•œ?‹¤.
	 */
	public void initDownloadDir()
	{
		xmlDownloadPath = xmlDownloadPath + getTodayDate(true).toString() + "/"; // ex) C:/Temp/lovname20130326	
		xmlDownloadPath_lang = xmlDownloadPath + "lang/"; // ex) C:/Temp/lovname20130326/lang/

		/** c:\temp\lovnameyyMMdd ?´?”ê°? ?—†?œ¼ë©? ?ƒ?„± */
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
	 * executeTmpBatch.bat ?ŒŒ?¼ ?ƒ?„± ?›„ bmide_manage_batch_lovs.bat ë°°ì¹˜ë¥? ?‹¤?–‰?•˜?—¬
	 * BMIDE?—?„œ LOV ê°’ì„ XML ?ŒŒ?¼ë¡? export ?•œ?‹¤.
	 * ?ŒŒ?¼ ?˜•?‹?? lov_values_201303261041.xml ?˜•?ƒœë¡? ?ƒ?„±?œ?‹¤.
	 * C:\\Tc9.properties.txt ?‚´?š© : SET TC_ROOT=C:\Siemens\Teamcenter9
	 * SET TC_DATA=\\10.80.28.162\d$\Siemens\tcdata
	 * call %TC_DATA%\tc_profilevars.bat
	 * 
	 * @throws Exception
	 */
	public void exportFile() throws Exception
	{

		/** bmide_manage_batch_lovs.bat ëª…ë ¹?„ ?‹¤?–‰?•˜ê¸? ?œ„?•œ ?™˜ê²½ë??ˆ˜ê°’ì´ ?ˆ?Š” ?ŒŒ?¼ */
//	   File pFile = new File("C:\\Tc9.properties.txt");
//	   
//	   if(!pFile.exists()) {
//		   String message = registry.getString("lovmanage.MESSAGE.Front") + pFile + registry.getString("lovmanage.MESSAGE.Back");
//		   throw new Exception(message);
//	   }

		WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
		simpleProgressBar.setWindowSize(500, 300);

		/** bmide_manage_batch_lovs.bat(-> XML?ŒŒ?¼ ?ƒ?„±?¨) ?ŒŒ?¼?„ ?‹¤?–‰?•  batch ?ŒŒ?¼?„ ?ƒ?„±?•œ?‹¤. **/
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
		// [20240306] ?š´?˜ TC Server ?™˜ê²½ì— ë§ì¶° ê²½ë¡œ ë³?ê²? ?•„?š”
		fw.write("SET TC_ROOT=D:\\SIEMENS\\TC13\r\n");
		fw.write("SET TC_DATA=Y:\\tcdata10\r\n");
		fw.write("call %TC_DATA%\\tc_profilevars.bat\r\n\r\n");
//	   }
//	   s.close();

		/** oldXML ?ŒŒ?¼ëª? ?ƒ?„± */
		setOldXmlFileName();

		/** oldXML ?ŒŒ?¼?˜ ? ˆ??ê²½ë¡œ ì§?? • */
		String exportFile = xmlDownloadPath + oldXMLFile + strXMLExtension; // ex) C:\Temp\lovname20130326\lov_values_201303261041.xml

		/** bmide_mamage_batch_lovs.bat ?ŒŒ?¼?„ ?‹¤?–‰?•˜ê¸? ?œ„?•œ ëª…ë ¹?–´ë¥? ë°›ì•„?˜´ */
		String command = getExecuteBatch(true, exportFile);
		fw.write(command);
		fw.close();

		/** ProgressBar ?‹¤?–‰ */
		simpleProgressBar.start();
		simpleProgressBar.setStatus("LOV Export is start...", true);

		/** ë°°ì¹˜ ?ŒŒ?¼ ?‹¤?–‰ */
		String[] cmd = { "CMD", "/C", batFile.getPath() };
		Process p = Runtime.getRuntime().exec(cmd);

		/** ?™¸ë¶? ?”„ë¡œê·¸?¨?— ???•œ InputStream ?„ ?ƒ?„± */
		DataInputStream inputstream = new DataInputStream(p.getInputStream());
		BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

		String strOutput = "";

		while (true)
		{
			// ?™¸ë¶? ?”„ë¡œê·¸?¨?´ ì¶œë ¥?•˜?Š” ë©”ì„¸ì§?ë¥? ?•œì¤„ì”© ?½?–´?“¤?„
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
	 * xml?ŒŒ?¼ëª…ì„ ?ƒ?„±?•œ?‹¤.
	 * xml : lov_values_201303261041
	 * xml_lang : lov_values_201303261041_lang
	 */
	public void setOldXmlFileName()
	{
		/** ?˜¤?Š˜ ?‚ ì§œë?? ê°?? ¸?˜´(20130101) */
		String currentDate = getTodayDate(false);
		oldXMLFile = oldXMLFile + "_" + currentDate;
		oldXMLFile_lang = oldXMLFile + "_lang";
	}

	/**
	 * LOV Dialogë¥? ì´ˆê¸°?™”?•œ?‹¤.
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
		// [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •. 
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

		// ê°? Columnë³? SellRenderer Setting
		for (int k = 0; k < lovDataItemTableModel.cNames.length; k++)
		{
			// Custom TableCellRenderer
			LovDataTableCellRenderer tableRenderer = new LovDataTableCellRenderer();
			TableColumn column = this.dataTable.getColumnModel().getColumn(k);
			column.setCellRenderer(tableRenderer);
		}

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •. 
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
				// ?”ë¸”í´ë¦? ?¼ ê²½ìš°, ?„ ?ƒ?œ ?–‰?˜ ?ˆ˜? •?™”ë©´ìœ¼ë¡? ?´?™
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

		// LOV ì¶”ê?
		JButton plusBtn = new JButton("+");
		plusBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{
				final int nSelected = lovTable.getSelectedRow();
				if (nSelected < 0)
					return;
				LovItem selectedLOVItem = lovList.get(nSelected);
				// LOV ì¶”ê? Dialog
				LovDataDialog dlg = new LovDataDialog();

				dlg.setVisible(true);

			}
		});

		// LOV ?‚­? œ
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
				/** ?„ ?ƒ?œ LOV Dataë¥? TeamCenter?— ???¥?•©?‹ˆ?‹¤. */
				saveActionTC();
				/** Dialog?˜ Status ê°’ì„ ì´ˆê¸°?™”?•œ?‹¤. */
				initStatus();
			}
		});

		/** ?•´?‹¹ LOV?˜ ê°’ì„ Excel ?ŒŒ?¼ë¡? ???¥?•œ?‹¤. */
		JButton btnExcelExport = new JButton("Excel Export");
		btnExcelExport.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent ee)
			{

				excelExport();
			}
		});

		/** ëª¨ë“  LOV?˜ ê°’ì„ Excel ?ŒŒ?¼ë¡? ???¥?•œ?‹¤. */
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
//        // ëª¨ë“  LOV Dataë¥? TeamCenter?— ???¥?•©?‹ˆ?‹¤.
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
	 * XML ?ŒŒ?¼?„ parse?•˜?—¬ Document ê°ì²´ë¥? ?ƒ?„±?•œ?‹¤.
	 * 
	 * @param xmlFile
	 * @throws Exception
	 */
	public void openFile() throws Exception
	{
		// xml ?ŒŒ?¼?„ parse?•˜?—¬ document?— ?„£?–´?‘ 
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
			String message = "ì§?? •?œ ?œ„ì¹?(" + path + ")?— ?ŒŒ?¼?´ ì¡´ì¬?•˜ì§? ?•Š?Šµ?‹ˆ?‹¤.";
			throw new Exception(message);
		}
	}

	/**
	 * LOV Dialog?˜ ? •ë³´ë?? Excel ?ŒŒ?¼ë¡? export?•œ?‹¤.
	 */
	public void excelExport()
	{
		try
		{
			// 1. ?ŒŒ?¼ ???¥ ???™”?ƒ?ë¥? ?„?š´?‹¤
			JFileChooser fileChooser = new JFileChooser("C:/");

			/** ?•´?‹¹ ?””? ‰?† ë¦¬ì—?„œ xls ?ŒŒ?¼ë§? ë³´ì´?„ë¡? ?„¤? •?•œ?‹¤. */
			FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
			fileChooser.removeChoosableFileFilter(fileFilter);
			SimpleStructureFilter filterXLS = new SimpleStructureFilter("xls", "?—‘?? (.xls)");
			fileChooser.addChoosableFileFilter(filterXLS);

			// 2. ì§?? •?•œ ?´ë¦„ìœ¼ë¡? ?—‘?? ?ŒŒ?¼?„ ?ƒ?„±?•œ?‹¤
			File file = new File("C:/" + strExportExcelFileName + ".xls");
			fileChooser.setSelectedFile(file);

			if (fileChooser.showSaveDialog(AIFDesktop.getActiveDesktop().getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = fileChooser.getSelectedFile();

				if (selectedFile != null)
				{
					String strExtension = getExtension(selectedFile);
					strExtension = strExtension == null ? "" : strExtension;

					// ?‚¬?š©?ê°? ?…? ¥?•œ ?ŒŒ?¼ëª…ì— xls ?™•?¥?ê¹Œì? ?…? ¥ ?•ˆ?–ˆ?„ ê²½ìš°
					if (!strExtension.equalsIgnoreCase("xls"))
					{
						selectedFile = new File(selectedFile.getAbsolutePath() + ".xls");
					}

					// ?“°ê¸°ê??Š¥?•œ ?—‘?? Workbook ê°ì²´ ?ƒ?„±
					WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);

					/** LOV ê°’ìœ¼ë¡? Excel ?ŒŒ?¼?— Write?•œ?‹¤. */
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
	 * LOV Dialog?˜ ëª¨ë“  LOVê°’ì„ Excel?ŒŒ?¼ë¡? ???¥?•œ?‹¤.
	 * LOV Name?? ê°ê°?˜ Sheetëª…ì´ ?œ?‹¤.
	 */
	public void excelFullExport()
	{
		try
		{
			// 1. ?ŒŒ?¼ ???¥ ???™”?ƒ?ë¥? ?„?š´?‹¤
			JFileChooser fileChooser = new JFileChooser("C:/");
			FileFilter fileFilter = fileChooser.getAcceptAllFileFilter();
			fileChooser.removeChoosableFileFilter(fileFilter);
			SimpleStructureFilter filterXLS = new SimpleStructureFilter("xls", "?—‘?? (.xls)");
			fileChooser.addChoosableFileFilter(filterXLS);

			// 2. ì§?? •?•œ ?´ë¦„ìœ¼ë¡? ?—‘?? ?ŒŒ?¼?„ ?ƒ?„±?•œ?‹¤
			File file = new File("C:/LOV_All_List_" + getTodayDate(true) + ".xls");
			fileChooser.setSelectedFile(file);

			if (fileChooser.showSaveDialog(AIFDesktop.getActiveDesktop().getFrame()) == JFileChooser.APPROVE_OPTION)
			{
				File selectedFile = fileChooser.getSelectedFile();

				if (selectedFile != null)
				{
					// ?ŒŒ?¼?˜ ?™•?¥?ë¥? return?•œ?‹¤.
					String strExtension = getExtension(selectedFile);
					strExtension = strExtension == null ? "" : strExtension;

					// ?‚¬?š©?ê°? ?…? ¥?•œ ?ŒŒ?¼ëª…ì— xls ?™•?¥?ê¹Œì? ?…? ¥ ?•ˆ?–ˆ?„ ê²½ìš°
					if (!strExtension.equalsIgnoreCase("xls"))
					{
						selectedFile = new File(selectedFile.getAbsolutePath() + ".xls");
						strExtension = "xls";
					}

					// ?“°ê¸°ê??Š¥?•œ ?—‘?? Workbook ê°ì²´ ?ƒ?„±
					WritableWorkbook workBook = Workbook.createWorkbook(selectedFile);

					/** LOV ëª…ì„ ê°ê°?˜ Sheet Name?œ¼ë¡? ì§?? •?•˜ê³?, LOV ê°’ì„ Excel?— Write?•œ?‹¤. */
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

				/** Excel ?ŒŒ?¼?„ ?‹¤?–‰?•œ?‹¤. **/
				AIFShell aif = new AIFShell("application/vnd.ms-excel", selectedFile.getAbsolutePath());
				aif.start();
			}
		} catch (Exception e1)
		{
			e1.printStackTrace();
		}
	}

	/**
	 * LOV Dialog?˜ JTable ?˜ ?‚´?š©?„ XLS ?¬ë§·ìœ¼ë¡? ???¥?•œ?‹¤.
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
			// ?“°ê¸°ê??Š¥?•œ ?—‘?? Workbook ê°ì²´ ?ƒ?„±
//    	WritableWorkbook workBook = null;
//      if(sheetNum == 0) {
//    	  workBook = Workbook.createWorkbook(selectedFile);
//      } else {
//    	  Workbook workBook1 = Workbook.getWorkbook(selectedFile);
//    	  workBook = Workbook.createWorkbook(selectedFile, workBook1);
//      }

			// në²ˆì§¸ Sheet ?ƒ?„±
			WritableSheet sheet = workBook.createSheet(strItemKey, sheetNum);

			SheetSettings printSet = sheet.getSettings();
			printSet.setFitWidth(1);
			printSet.setFitToPages(true);
			printSet.setOrientation(PageOrientation.LANDSCAPE);

			// 1. ?—¤?” ? •ë³? ê°?? ¸?˜¤ê¸?
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

			// 2. ? „ì²? Row ? •ë³? ê°?? ¸?˜¤ê¸?
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
	 * jxl ?˜ Cell Format ?„ ?„¤? •?•œ?‹¤.
	 * 
	 * @param feature
	 * @return
	 * @throws WriteException
	 */
	private WritableCellFormat setCellValueFormat(int status) throws Exception
	{
		WritableFont wf = new WritableFont(WritableFont.createFont("êµ´ë¦¼"), 9, WritableFont.NO_BOLD);

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
	 * ?ŒŒ?¼?˜ ?™•?¥?ë¥? Return
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
	 * LOV Listë¥? Loading?•©?‹ˆ?‹¤.
	 * 1. bmide_manage_batch_lovs.bat Utilityë¥? ?‹¤?–‰?•˜?—¬ Xml?„ ?ƒ?„±?•©?‹ˆ?‹¤.
	 * 2. XML?„ Loading?•˜?—¬ LOV/LOVDataë¥? Loading?•©?‹ˆ?‹¤.
	 */
	void loadLovList()
	{
		Thread lovLoad = new Thread()
		{
			public void run()
			{
				try
				{

					/** Document ê°ì²´ë¥? ?ƒ?„±?•œ?‹¤. */
					openFile();

					/** newXML ?ŒŒ?¼?„ ?ƒ?„±?•˜ê¸? ?œ„?•´ oldXML ?ŒŒ?¼?˜ Nodeê°’ì„ ?½?–´?˜¨?‹¤. **/
					setDefaultXML();

					/** newXML_lang ?ŒŒ?¼?„ ?ƒ?„±?•˜ê¸? ?œ„?•´ oldXML ?ŒŒ?¼?˜ Nodeê°’ì„ ?½?–´?˜¨?‹¤. **/
					setDefaultXMLForLang();

					Element rootElement = document.getDocumentElement();

					NodeList memberList = rootElement.getElementsByTagName("TcLOV");

					// LOV?˜ ê°’ì„ êµ¬í•œ?‹¤. - 1 Level
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
							// ?„ ?ƒ?œ TCLov?˜ Listê°’ì„ êµ¬í•œ?‹¤. - 2 Level
							for (int k = 0; k < childNodeList.getLength(); k++)
							{ // for 3
								Node childNode = childNodeList.item(k);

								if ((childNode.getNodeName()).equals("TcLOVValue"))
								{ // LOV?˜ Key, Valueê°?
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

									// ?—¬ê¸°ì—?„œ XML_Lang?˜ desc ê°’ì„ êµ¬í•œ?‹¤.
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
	 * import?•  xml ?ŒŒ?¼?„ ?ƒ?„±?•˜ê¸? ?œ„?•´ Xmlns ?? batchXSDVersion ê°’ì„ lov_values_yyyyMMdd.xml ?—?„œ ê°?? ¸?˜¨?‹¤.
	 * 
	 * @throws Exception
	 */
	public void setDefaultXML()
	{
		Element rootElement = document.getDocumentElement();

		// XML ?ŒŒ?¼?—?„œ xmlns ?? batchXSDVersion ? •ë³´ë?? ê°?? ¸?˜¨?‹¤.
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
	 * import?•  xml ?ŒŒ?¼?„ ?ƒ?„±?•˜ê¸? ?œ„?•´ Xmlns ?? batchXSDVersion ê°’ì„ lov_values_yyyyMMdd_Lang.xml ?—?„œ ê°?? ¸?˜¨?‹¤.
	 * download?œ XML_Lang ?ŒŒ?¼?˜ ê¸°ë³¸ ? •ë³´ë?? ?½?–´?„œ ???¥?•œ?‹¤.
	 */
	public void setDefaultXMLForLang() throws SAXException, IOException
	{
		Element rootElement_lang = document_lang.getDocumentElement();

		// XML_lang ?ŒŒ?¼?—?„œ xmlns ?? batchXSDVersion ? •ë³´ë?? ê°?? ¸?˜¨?‹¤.
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
	 * XML_Lang ?ŒŒ?¼?—?„œ ?•´?‹¹ LOV?˜ Description ê°’ì„ ?½?–´?˜¨?‹¤.
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

		// LOV?˜ ê°’ì„ êµ¬í•œ?‹¤. - 1 Level
		for (int i = 0; i < memberList.getLength(); i++)
		{ // for 1
			Node node = memberList.item(i);

			if (node.getNodeType() == Node.ELEMENT_NODE)
			{

				NodeList childNodeList = node.getChildNodes();
				// ?„ ?ƒ?œ TCLov?˜ Listê°’ì„ êµ¬í•œ?‹¤. - 2 Level
				for (int k = 0; k < childNodeList.getLength(); k++)
				{ // for 3
					Node childNode = childNodeList.item(k);

					if ((childNode.getNodeName()).equals("key"))
					{ // LOV?˜ Key, Valueê°?
						NamedNodeMap childAttrs = childNode.getAttributes();

						for (int y = 0; y < childAttrs.getLength(); y++)
						{ // for 4
							if ((childAttrs.item(y).getNodeName()).equals("id"))
							{
								//[20190225 kch] xml ?ŒŒ?¼ë¡? ë¶??„° lov List ? •ë³? read ?‹œ bug ê°œì„  ( contains -> equals )
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
	 * ëª¨ë“  LOV Dataë¥? TeamCenter?— ???¥?•©?‹ˆ?‹¤.
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
						// ?•´?‹¹ LOVë¥? ê°•ì œ ?„ ?ƒ?•©?‹ˆ?‹¤.
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
	 * XML Import ?›„ dialog?˜ ?ƒ?ƒœê°’ì„ ì´ˆê¸°?™”?•œ?‹¤.
	 */
	void initStatus()
	{
		// STATUS_DELETE ?Š” table?—?„œ ?‚­? œ?•œ?‹¤.
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem selectedLOVItem = dataList.get(i);

			if (selectedLOVItem.getData(LovDataItem.INDEX_STATUS).equals(LovDataItem.STATUS_DELETE))
			{
				dataList.remove(i);
			}
		}

		// table?˜ Statusê°’ì„ ì´ˆê¸°?™”?•œ?‹¤.
		for (int i = 0; i < dataList.size(); i++)
		{
			LovDataItem selectedLOVItem = dataList.get(i);

			selectedLOVItem.setData(LovDataItem.INDEX_STATUS, LovDataItem.STATUS_NORMAL);
		}
		lovDataItemTableModel.fireTableDataChanged();
	}

	/**
	 * ?„ ?ƒ?œ LOV Dataë¥? TeamCenter?— ???¥?•©?‹ˆ?‹¤.
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

		// xml ?ŒŒ?¼ ?ƒ?„±
		writeXML();
		executeLogMessage();
	}

	void writeXML()
	{
		/** document ê°ì²´ ?ƒ?„± */
		org.jdom.Document doc = new org.jdom.Document();
		org.jdom.Document doc_Lang = new org.jdom.Document();

		/** NameSpace ?ƒ?„± */
		org.jdom.Namespace nameSpace = org.jdom.Namespace.getNamespace(topNodeAttrXmlns);
		org.jdom.Namespace nameSpace_Lang = org.jdom.Namespace.getNamespace(topNodeAttrXmlns_lang);

		/** Element ê°ì²´ ?ƒ?„± */
		org.jdom.Element topElement = new org.jdom.Element("TcBusinessData", nameSpace); // TcBusinessData
		org.jdom.Element changeElement = new org.jdom.Element("Change", nameSpace); // <Change>
		org.jdom.Element tclovElement = new org.jdom.Element("TcLOV", nameSpace); // <TcLOV
		org.jdom.Element topElement_Lang = new org.jdom.Element("TcBusinessDataLocalization", nameSpace_Lang); // TcBusinessDataLocalization
		org.jdom.Element addElement_Lang = new org.jdom.Element("Add", nameSpace_Lang); // Add

		// ?˜„?¬ ?‹œê°?
		Date currentDate = new Date();

		topElement.setAttribute(TOP_NODE_ATTR_NAME_VERSION, topNodeAttrVersion);
		topElement.setAttribute("Date", currentDate.toString());

		// Change Tag ?†?„±
		tclovElement.setAttribute("name", lovName);
		tclovElement.setAttribute("usage", "Exhaustive");
		tclovElement.setAttribute("lovType", "ListOfValuesString");
		tclovElement.setAttribute("isManagedExternally", "true");

		// XML_Lang ?ŒŒ?¼ ?ƒ?„±
		topElement_Lang.setAttribute(TOP_NODE_ATTR_NAME_VERSION, topNodeAttrVersion_lang);
		topElement_Lang.setAttribute("Date", currentDate.toString());

		/** xml ?ŒŒ?¼ ë°? lang.xml ?ŒŒ?¼?˜ LOVValue tag ë¶?ë¶? set */
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

			//ê°? ?—˜ë¦¬ë¨¼?Š¸?“¤ ë°°ì¹˜ ?‘?—… (?—˜ë¦¬ë¨¼?Š¸?— ??‹ ?š”?†Œë¥? ì¶”ê??•  ?–„?Š” )(?œ„?˜ ì£¼ì„??ë¡? ë°°ì¹˜ë¥? ?•´ì¤˜ì•¼ ?•œ?‹¤).  addContent()ë©”ì„œ?“œë¥? ?´?š©?•œ?‹¤.
			tclovElement.addContent(valueElement);
			addElement_Lang.addContent(keyElement);
		}

		/** lang.xml ?ŒŒ?¼?˜ LOVValueDescription ë¶?ë¶? set */
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

			//ê°? ?—˜ë¦¬ë¨¼?Š¸?“¤ ë°°ì¹˜ ?‘?—… (?—˜ë¦¬ë¨¼?Š¸?— ??‹ ?š”?†Œë¥? ì¶”ê??•  ?–„?Š” )(?œ„?˜ ì£¼ì„??ë¡? ë°°ì¹˜ë¥? ?•´ì¤˜ì•¼ ?•œ?‹¤).  addContent()ë©”ì„œ?“œë¥? ?´?š©?•œ?‹¤.
			addElement_Lang.addContent(keyDescElement);
		}

		changeElement.addContent(tclovElement);
		topElement.addContent(changeElement);
		topElement_Lang.addContent(addElement_Lang);

		// ë§ˆì?ë§‰ìœ¼ë¡? Document?— ìµœìƒ?œ„ Elementë¥? ?„¤? •?•œ?‹¤.
		doc.setRootElement(topElement);
		doc_Lang.setRootElement(topElement_Lang);

		// ?ŒŒ?¼ë¡? ???¥?•˜ê¸? ?œ„?•´?„œ XMLOutputter ê°ì²´ê°? ?•„?š”?•˜?‹¤
		XMLOutputter xout = new XMLOutputter();

		// ê¸°ë³¸ ?¬ë§? ?˜•?ƒœë¥? ë¶ˆëŸ¬?? ?ˆ˜? •?•œ?‹¤.
		Format fm = xout.getFormat();

		/** ?‹¤êµ??–´ ì§??›?„ ?œ„?•´ encodeing ?˜•?ƒœë¥? UTF-8ë¡? ë³?ê²½í•œ?‹¤. */
//      fm.setEncoding("euc-kr");
		fm.setEncoding("UTF-8");

		// ë¶?ëª?, ??‹ ?ƒœê·¸ë?? êµ¬ë³„?•˜ê¸? ?œ„?•œ ?ƒ­ ë²”ìœ„ë¥? ? •?•œ?‹¤.
		fm.setIndent("   ");
		//?ƒœê·¸ë¼ë¦? ì¤„ë°”ê¿ˆì„ ì§?? •?•œ?‹¤.
		fm.setLineSeparator("\r\n");

		// ?„¤? •?•œ XML ?ŒŒ?¼?˜ ?¬ë§·ì„ set?•œ?‹¤.
		xout.setFormat(fm);
		try
		{
//    	  xout.output(doc, new FileWriter(strExportPath + strImportXMLFileName + strXMLExtension));
//    	  xout.output(doc_Lang, new FileWriter(strExportLangPath + strImportXMLLangFileName + strXMLExtension));

			/** XML ?ŒŒ?¼ ???¥?„ UTF-8ë¡? ?•´?•¼ ?•˜ê¸°ì— FileOutputStream?œ¼ë¡? ???¥?•¨.(FileWriter?Š” UTF-8ë¡? ???¥?´ ?•ˆ?¨) */
			xout.output(doc, new FileOutputStream(xmlDownloadPath + newXMLFile + strXMLExtension));
			xout.output(doc_Lang, new FileOutputStream(xmlDownloadPath_lang + newXMLFile_lang + strXMLExtension));

		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * bmide_mamage_batch_lovs.bat ?ŒŒ?¼?„ ?‹¤?–‰?•˜ê¸? ?œ„?•œ ?ŒŒ?¼ë¯¸í„° ê°’ì„ ë¦¬í„´?•œ?‹¤.
	 * 
	 * @param isExport
	 *            : true?´ë©? extract(XML ?ŒŒ?¼ Export), false?´ë©? update(XML ?ŒŒ?¼ Import)
	 * @param file
	 *            : xml ?ŒŒ?¼?˜ full path
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
	 * ?ˆ˜? •?œ LOV ê°’ì„ ?„œë²„ë¡œ Export?•œ?‹¤.
	 */
//  void executeLogMessage(){
//	  File pFile = new File("C:\\Tc9.properties.txt");		// ?´ ?ŒŒ?¼?? ?•­?ƒ ì¡´ì¬?•´?•¼ ?•œ?‹¤.
//	  FileWriter fw = null;
//	  
//	  try {
//		  if(!pFile.exists()) {
////			   String message = "ì§?? •?œ ?œ„ì¹?(" + pFile + ")?— ?ŒŒ?¼?´ ì¡´ì¬?•˜ì§? ?•Š?Šµ?‹ˆ?‹¤.";
//			  String message = registry.getString("lovmanage.MESSAGE.Front") + pFile + registry.getString("lovmanage.MESSAGE.Back");
//			  MessageBox.post(message, "?•Œë¦?", MessageBox.INFORMATION);
//			  return;
//		  }
//		  
//		  /** ?ŒŒ?¼ ê°ì²´ ?ƒ?„± */
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
//		  /** bmide_mamage_batch_lovs.bat ?ŒŒ?¼?„ ?‹¤?–‰?•˜ê¸? ?œ„?•œ ëª…ë ¹?–´ë¥? ë°›ì•„?˜´ */
//		  String exportFile = xmlDownloadPath + newXMLFile + strXMLExtension;
//		  String command = getExecuteBatch(false, exportFile);
//		  fw.write(command);
//		  s.close();
//		  fw.close();
//
//		  /** Progress bar ?‹¤?–‰ */
//		  WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
//		  simpleProgressBar.setWindowSize(500, 300);
//		  simpleProgressBar.start();
//		  simpleProgressBar.setStatus("LOV Import is start..."	, true);
//		  
//		  /** ë°°ì¹˜?ŒŒ?¼ ?‹¤?–‰ */
//		  String[] cmd = { "CMD", "/C", batFile.getPath() };
//		  Process p = Runtime.getRuntime().exec(cmd);
//	  
//		  // ?™¸ë¶? ?”„ë¡œê·¸?¨?— ???•œ InputStream ?„ ?ƒ?„±
//	      DataInputStream inputstream = new DataInputStream(p.getInputStream());
//	      BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));
//	      
//	      String strOutput = "";
//	      while (true)
//	      {
//	    	  // ?™¸ë¶? ?”„ë¡œê·¸?¨?´ ì¶œë ¥?•˜?Š” ë©”ì„¸ì§?ë¥? ?•œì¤„ì”© ?½?–´?“¤?„
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

	//progress bar ?ˆ˜? •
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
				/** ?ŒŒ?¼ ê°ì²´ ?ƒ?„± */
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

				/** bmide_mamage_batch_lovs.bat ?ŒŒ?¼?„ ?‹¤?–‰?•˜ê¸? ?œ„?•œ ëª…ë ¹?–´ë¥? ë°›ì•„?˜´ */
				String exportFile = xmlDownloadPath + newXMLFile + strXMLExtension;
				String command = getExecuteBatch(false, exportFile);
				fw.write(command);
				fw.close();

				/** Progress bar ?‹¤?–‰ */
				WaitProgressBar simpleProgressBar = new WaitProgressBar(AIFUtility.getActiveDesktop());
				simpleProgressBar.setWindowSize(500, 300);
				simpleProgressBar.start();
				simpleProgressBar.setStatus("LOV Import is start...", true);

				/** ë°°ì¹˜?ŒŒ?¼ ?‹¤?–‰ */
				String[] cmd = { "CMD", "/C", batFile.getPath() };
				Process p = Runtime.getRuntime().exec(cmd);

				// ?™¸ë¶? ?”„ë¡œê·¸?¨?— ???•œ InputStream ?„ ?ƒ?„±
				DataInputStream inputstream = new DataInputStream(p.getInputStream());
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputstream));

				String strOutput = "";
				while (true)
				{
					// ?™¸ë¶? ?”„ë¡œê·¸?¨?´ ì¶œë ¥?•˜?Š” ë©”ì„¸ì§?ë¥? ?•œì¤„ì”© ?½?–´?“¤?„
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
	 * ?˜¤?Š˜ ?‚ ì§œë?? 20130101 or 20130101HHmm?˜•?ƒœë¡? ë³??™˜?‹œì¼? ë¦¬í„´?•œ?‹¤.
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
						newXMLFile = selectedLOVItem.toString() + "_" + getTodayDate(false); // LOV name?œ¼ë¡? xml ?ŒŒ?¼ëª…ì„ ë§Œë“ ?‹¤.
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
		// [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •. 
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
		 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
		 * sort method
		 */
		public void sort()
		{
			Collections.sort(lovList, new LovItemComparator(sortType, sortOrder));
		}// sort

		/**
		 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
		 * sort ê¸°ì? column ì§?? •.
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
		// [20140422] bskwak, column ëª? ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ì¶”ê?. 
		// 3ê°œì??˜ ê²ƒì„ 4ê°œë¡œ ë³?ê²? Sort ê¸°ëŠ¥?„ ?“°ë©? ?‘œê¸°ë˜ì§? ?•Š?Š” column?„ ëª¨ë‘ ?„¤? •?•´?•¼ ?•˜?Š” ?“¯. 
		public Class[] colClasses = { String.class, String.class, String.class, String.class };

		//------------------------------------------------------------------------------------------
		// [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •. 
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
		 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
		 * sort method
		 * 
		 */
		public void sort()
		{
			Collections.sort(dataList, new LovDataItemComparator(sortBy, sortType, sortOrder));
		}// sort

		/**
		 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
		 * sort ê¸°ì? column ì§?? •.
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

			// Tooltip ì§?? •
			if (value != null && value.getClass().getName().endsWith("String"))
			{
				setToolTipText(value.toString());
			}

			// ?„ ?ƒ Background ì§?? •
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
		JTextField descField; // Display ëª?

		/**
		 * ?‹ ê·œìƒ?„±?¸ ê²½ìš°
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
		 * ?‹ ê·œìƒ?„±?¸ ê²½ìš°
		 */
		LovDataDialog(String key, String seq)
		{

			this();
			this.init();

		}

		/**
		 * ?ˆ˜? •?¸ ê²½ìš°
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
//          MessageBox.post(LovDataDialog.this, "Key Fieldê°? ê³µë??…?‹ˆ?‹¤.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.KeyField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}
			if ("".equals(valueField.getText().trim()))
			{
//          MessageBox.post(LovDataDialog.this, "Desc Fieldê°? ê³µë??…?‹ˆ?‹¤.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.ValueField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}
			if ("".equals(descField.getText().trim()))
			{
//          MessageBox.post(LovDataDialog.this, "Displayëª? Fieldê°? ê³µë??…?‹ˆ?‹¤.", "Warning", MessageBox.INFORMATION);
				String message = registry.getString("lovmanage.MESSAGE.DisplayField");
				MessageBox.post(LovDataDialog.this, message, "Warning", MessageBox.INFORMATION);
				return;
			}

			// ?ƒ?„±?¸ ê²½ìš°
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
			// ?ˆ˜? •?¸ ê²½ìš°
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
	 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
	 * LovItemComparator class
	 * ?†Œ?Œ…?— ?‚¬?š©?˜?Š” Comparator Class
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
		}// ?ƒ?„±?

		public int compare(Object o1, Object o2)
		{
			int result = 0;
			// STEP 1. ê°ì²´ ???…?´ LovItem ?¸ì§? ë¹„êµ
			if (!(o1 instanceof LovManagerDialog.LovItem))
				return 0;
			if (!(o2 instanceof LovManagerDialog.LovItem))
				return 0;

			String str1 = ((LovManagerDialog.LovItem) o1).strName;
			String str2 = ((LovManagerDialog.LovItem) o2).strName;

			// STEP 2. ì»¬ëŸ¼ ì¢…ë¥˜?— ?”°?¼?„œ ???†Œë¥? ë¹„êµ?•œ?‹¤.
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

			// STEP 3. ?†Œ?Œ… ë°©í–¥?„ ? ?š©?•œ?‹¤.
			result *= sortOrder;
			return result;
		}// compare
	}

	/**
	 * 
	 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
	 * ?†Œ?Œ…?— ?‚¬?š©?˜?Š” Comparator Class
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
		}// ?ƒ?„±?

		public int compare(Object o1, Object o2)
		{
			int result = 0;
			// STEP 1. ê°ì²´ ???…?´ LovItem ?¸ì§? ë¹„êµ
			if (!(o1 instanceof LovManagerDialog.LovDataItem))
				return 0;
			if (!(o2 instanceof LovManagerDialog.LovDataItem))
				return 0;

			String str1 = ((LovManagerDialog.LovDataItem) o1).getData(this.sortBy);
			String str2 = ((LovManagerDialog.LovDataItem) o2).getData(this.sortBy);

			// STEP 2. ì»¬ëŸ¼ ì¢…ë¥˜?— ?”°?¼?„œ ???†Œë¥? ë¹„êµ?•œ?‹¤.
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

			// STEP 3. ?†Œ?Œ… ë°©í–¥?„ ? ?š©?•œ?‹¤.
			result *= sortOrder;
			return result;
		}// compare
	}

	/**
	 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
	 * LovItemTableModel ?š© header mouse adapter
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
		}// ?ƒ?„±?

		public void mouseClicked(MouseEvent e)
		{
			// STEP 1. ?–´?Š ì»¬ëŸ¼?´ ?´ë¦??˜?—ˆ?Š”ì§? ì°¾ì•„?‚¸?‹¤.
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

			if (modelIndex < 0)
				return;

			// STEP 2. ?´ë¦??œ ì»¬ëŸ¼?— ?”°?¼ ?†Œ?Œ… ?ˆœ?„œ ë°? ?†Œ?Œ… ê¸°ì??„ ë³?ê²½í•œ?‹¤.
			LovItemTableModel tableModel = (LovItemTableModel) table.getModel();
			tableModel.setSortBy(modelIndex);
			tableModel.sort();
			table.repaint();
		}// mouseClicked
	}

	/**
	 * [SR140513-015][20140512] bskwak, column ?—´ ?´ë¦? ?‹œ ? •? ¬ ê¸°ëŠ¥ ?˜¤ë¥? ?ˆ˜? •.
	 * LovDataItemTableModel ?š© header mouse adapter
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
		}// ?ƒ?„±?

		public void mouseClicked(MouseEvent e)
		{
			// STEP 1. ?–´?Š ì»¬ëŸ¼?´ ?´ë¦??˜?—ˆ?Š”ì§? ì°¾ì•„?‚¸?‹¤.
			TableColumnModel colModel = table.getColumnModel();
			int columnModelIndex = colModel.getColumnIndexAtX(e.getX());
			int modelIndex = colModel.getColumn(columnModelIndex).getModelIndex();

			if (modelIndex < 0)
				return;

			// STEP 2. ?´ë¦??œ ì»¬ëŸ¼?— ?”°?¼ ?†Œ?Œ… ?ˆœ?„œ ë°? ?†Œ?Œ… ê¸°ì??„ ë³?ê²½í•œ?‹¤.
			LovDataItemTableModel tableModel = (LovDataItemTableModel) table.getModel();
			tableModel.setSortBy(modelIndex);
			tableModel.sort();
			table.repaint();
		}// mouseClicked
	}
}
