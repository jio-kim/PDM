package com.kgm.common.attachfile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Window;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EtchedBorder;

import com.kgm.common.SYMCAWTTitledBorder;
import com.kgm.common.SYMCClass;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.aifrcp.AIFUtility;
import com.teamcenter.rac.commands.open.OpenCommand;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.kernel.TCComponentDatasetType;
import com.teamcenter.rac.kernel.TCComponentForm;
import com.teamcenter.rac.kernel.TCComponentFormType;
import com.teamcenter.rac.kernel.TCComponentItem;
import com.teamcenter.rac.kernel.TCComponentItemRevision;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.PropertyLayout;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.iTextArea;
import com.teamcenter.rac.util.iTextField;
import java.awt.GridLayout;
import java.awt.Font;

/**
 * local�� �ִ� File �� ����Ÿ�� �ִ� �����ͼ���  ���ÿ� �����ϱ� ���� ���� ÷�� panel�̴�.<br>
 * ���� �� �����ͼ��� ÷���ϰ��� �ϴ� Ÿ���� ��ũ�� ÷�εȴ�.
 * [20170314][ymjang] ��ȣȭ ���� ��� ���� ����
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class AttachFilePanel extends JPanel{
	private static final long serialVersionUID = 1L;

	private Registry registry = Registry.getRegistry(this);
	public static String[] DATASET_TYPE_ARRAY = null;
	public static String separator = null; //Separator
	private FindDialog templateFindDialog = null;
	private FindDialog saveasFindDialog = null;
	private JPanel buttonPanel = new JPanel();
	private BorderLayout borderLayout = new BorderLayout(5,5);
	
	private Insets margin = new Insets(0,0,0,0);
	private JButton addFileButton = new JButton(registry.getString("Search.TEXT"));
	private JButton deleteButton = new JButton(registry.getString("Remove.TEXT"));
	private JButton addQueryTemplateButton = new JButton("���ø� �˻�");
	private JButton addTreeTemplateButton = new JButton("Template Ʈ��");
	private JButton addDefineTemplateButton = new JButton("���ǵ� ���ø�");
	private JButton addSaveAsButton = new JButton("��ϵ� �ڷ� �˻�");
	private JButton modifyDatasetInfoButton = new JButton(registry.getString("Modify,TEXT"));
	private JButton webLinkButton = new JButton(registry.getString("attach_url.NAME"));

	private JScrollPane listScrollPane = new JScrollPane();
	private DropList fileList = new DropList();
	private int file_count_limit = 0;
	private JPanel attachLabelPanel = new JPanel();
	private JLabel attachLabel = new JLabel();
    private DefaultListModel listModel = new DefaultListModel();
	private HashMap filters = new HashMap(); //File Filters
	private JFileChooser jfc;
	private boolean isModifiable = true;

	private boolean isURL = false;

	private String savedQuery_Template = null;
	private String[] initAttNameArray_Template = null;
	private String[] initAttValueArray_Template = null;
	private String[] column_Ids_Template = null;
	private String[] column_Names_Template = null;
	private String[] column_Size_Template = null;

	private String savedQuery_SaveAs = null;
	private String[] initAttNameArray_SaveAs = null;
	private String[] initAttValueArray_SaveAs = null;
	private String[] column_Ids_SaveAs = null;
	private String[] column_Names_SaveAs = null;
	private String[] column_Size_SaveAs = null;

	private FlowLayout flowLayout1 = new FlowLayout();
	private TCSession session;
	private Object templateRoot = null;
	private String treeTemplateTitle = "Template";
	private Object defineClassObject = null;
	private String defineMethodName = "";
	private String datasetName = "";
	private String datasetDesc = "";
	private String relationType = SYMCClass.SPECIFICATION_REL;

	public AttachFilePanel()
	{
		try
		{
			session = (TCSession)AIFUtility.getSessionManager().getDefaultSession();
			jbInit();
			//separator = session.getPreferenceService().getString(TCPreferenceService.TC_preference_all, "WSOM_find_list_separator");
			separator = session.getPreferenceService().getStringValue("WSOM_find_list_separator");
		} catch(Exception ex)
		{
			MessageBox.post(ex);
		}
	}

	private void jbInit() throws Exception
	{
		setLayout(borderLayout);
		addFileButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addFileButton_actionPerformed(e);
			}
		});
		deleteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				deleteButton_actionPerformed(e);
			}
		});
		attachLabel.setPreferredSize(new Dimension(50, 20));
		fileList.setModel(listModel);
		fileList.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				fileList_mouseClicked(e);
			}
		});
		addQueryTemplateButton.setVisible(false);
//		addQueryTemplateButton.setMargin(new Insets(0, 0, 0, 0));
		addQueryTemplateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addQueryTemplateButton_actionPerformed(e);
			}
		});
		addTreeTemplateButton.setVisible(false);
//		addTreeTemplateButton.setMargin(new Insets(0, 0, 0, 0));
		addTreeTemplateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addTreeTemplateButton_actionPerformed(e);
			}
		});
		addDefineTemplateButton.setVisible(false);
//		addDefineTemplateButton.setMargin(new Insets(0, 0, 0, 0));
		addDefineTemplateButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addDefineTemplateButton_actionPerformed(e);
			}
		});
		addSaveAsButton.setVisible(false);
//		addSaveAsButton.setMargin(new Insets(0, 0, 0, 0));
		addSaveAsButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				addSaveAsButton_actionPerformed(e);
			}
		});
		modifyDatasetInfoButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				modifyDatasetInfoButton_actionPerformed(e);
			}
		});
		attachLabelPanel.setOpaque(false);
		buttonPanel.setOpaque(false);
		buttonPanel.setLayout(flowLayout1);
		this.setOpaque(false);
		webLinkButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				webLinkButton_actionPerformed(e);
			}
		});
		webLinkButton.setVisible(false);
//		webLinkButton.setMargin(new Insets(0, 0, 0, 0));
		flowLayout1.setAlignment(FlowLayout.RIGHT);
		flowLayout1.setVgap(0);
//		addFileButton.setMargin(new Insets(0, 0, 0, 0));
//		modifyDatasetInfoButton.setMargin(new Insets(0, 0, 0, 0));
//		deleteButton.setMargin(new Insets(0, 0, 0, 0));

		buttonPanel.add(addFileButton);
		buttonPanel.add(webLinkButton);
		buttonPanel.add(addQueryTemplateButton);
		buttonPanel.add(addTreeTemplateButton);
		buttonPanel.add(addDefineTemplateButton);
		buttonPanel.add(addSaveAsButton);
		//        buttonPanel.add(Box.createHorizontalStrut(16));
		buttonPanel.add(modifyDatasetInfoButton);
		buttonPanel.add(deleteButton);
		listScrollPane.setViewportView(fileList);
		fileList.setCellRenderer(new AttachFileListCellRenderer());

		this.add(Box.createHorizontalGlue(), java.awt.BorderLayout.NORTH);
		this.add(Box.createHorizontalGlue(), java.awt.BorderLayout.SOUTH);
		this.add(listScrollPane, java.awt.BorderLayout.CENTER);
		this.add(attachLabelPanel, java.awt.BorderLayout.WEST);
		attachLabelPanel.add(attachLabel);
		attachLabelPanel.setVisible(false);
		this.add(buttonPanel, java.awt.BorderLayout.NORTH);

		addFileButton.setIcon(registry.getImageIcon("Search.ICON"));
		addFileButton.setMargin(margin);
		deleteButton.setIcon(registry.getImageIcon("Remove.ICON"));
		deleteButton.setMargin(margin);
		addQueryTemplateButton.setIcon(registry.getImageIcon("attach_template.ICON"));
		addQueryTemplateButton.setMargin(margin);
		addTreeTemplateButton.setIcon(registry.getImageIcon("attach_template.ICON"));
		addTreeTemplateButton.setMargin(margin);
		addDefineTemplateButton.setIcon(registry.getImageIcon("attach_template.ICON"));
		addDefineTemplateButton.setMargin(margin);
		addSaveAsButton.setIcon(registry.getImageIcon("attach_saveas.ICON"));
		addSaveAsButton.setMargin(margin);
		modifyDatasetInfoButton.setIcon(registry.getImageIcon("attach_modify.ICON"));
		modifyDatasetInfoButton.setMargin(margin);
		webLinkButton.setIcon(registry.getImageIcon("attach_url.ICON"));
		webLinkButton.setMargin(margin);
		addFileButton.setToolTipText(registry.getString("attach_file.TOOLTIP"));
		deleteButton.setToolTipText(registry.getString("attach_delete.TOOLTIP"));
		addQueryTemplateButton.setToolTipText(registry.getString("attach_template.TOOLTIP"));
		addTreeTemplateButton.setToolTipText(registry.getString("attach_template.TOOLTIP"));
		addDefineTemplateButton.setToolTipText(registry.getString("attach_template.TOOLTIP"));
		addSaveAsButton.setToolTipText(registry.getString("attach_saveas.TOOLTIP"));
		modifyDatasetInfoButton.setToolTipText(registry.getString("attach_modify.TOOLTIP"));
		webLinkButton.setToolTipText(registry.getString("attach_url.TOOLTIP"));
		fileList.setToolTipText(registry.getString("attach_list.TOOLTIP"));
		
		JPanel warningMsgPanel = new JPanel();
		warningMsgPanel.setLayout(new GridLayout(0, 1, 0, 0));
		JLabel warningMsgLabel = new JLabel("�� ��ȣȭ�� ������ �ݵ�� ��ȣ�� �����Ͻ� ��, ����ϼž� �մϴ�. ��");
		warningMsgLabel.setFont(new Font("����", Font.BOLD, 13));
		warningMsgLabel.setForeground(Color.RED);
		warningMsgPanel.add(warningMsgLabel);
		warningMsgPanel.setOpaque(false);
		
		this.add(warningMsgPanel, java.awt.BorderLayout.SOUTH);
	}

	/**
	 * Dataset Type ����
	 * @param file File
	 * @return String
	 */
	private String getFileDatasetType(File file)
	{
		String fileName = file.getName();
		String fileExtend = fileName.substring(fileName.lastIndexOf(".") + 1);
		String datasetType = "";

		//Data Type ����
		Iterator itr = filters.values().iterator();

		while(itr.hasNext())
		{
			Hashtable map = (Hashtable)itr.next();

			if(map.get(fileExtend.toLowerCase()) != null)
			{
				datasetType = (String)map.get(fileExtend.toLowerCase());
				break;
			}
		}

		return datasetType;
	}

	/**
	 * ������ �̸��� �Է��ϰ��� �� ��� ����Ѵ�.<br>
	 * �� method�� �� ������ label �̸� �� ������(��)�� �����ϴ� method�̴�.
	 * @param name String  label �̸��� �Է��Ѵ�. default�� ÷������
	 * @param width int    label ���� �Է��Ѵ�. default�� 50
	 */
	public void setNameAttachLabel(String name, int width)
	{
		attachLabel.setText(name);
		attachLabel.setPreferredSize(new Dimension(width, 20));
		attachLabelPanel.setVisible(true);
	}

	/**
	 * dataset panel�� title border�� �����Ѵ�.
	 * @param titleName String
	 */
	public void setTitledBorder(String titleName)
	{
		setBorder(new SYMCAWTTitledBorder(titleName));
		updateUI();
	}

	/**
	 * ÷�� �߰� ��ư�� button name�� �����Ѵ�. �⺻�� File
	 * @param name String �����ϰ��� �ϴ� button name
	 * @deprecated
	 */
	public void setNameAddFileButton(String name)
	{
		addFileButton.setText(name);
	}

	/**
	 * ÷�� �߰� ��ư�� button visible�� ����. �⺻�� File
	 * @param flag boolean �����ϰ��� �ϴ� button
	 */
	public void setVisibleAddFileButton(boolean flag)
	{
		addFileButton.setVisible(flag);
	}

	public JButton getAddFileButton()
	{
		return addFileButton;
	}

	public void setVisibleAddSaveAsButton(boolean flag)
	{
		addSaveAsButton.setVisible(flag);
	}

	/**
	 * ÷�� ���� ��ư�� button name�� �����Ѵ�. �⺻�� Delete
	 * @param name String �����ϰ��� �ϴ� button name
	 * @deprecated
	 */
	public void setNameDeleteButton(String name)
	{
		deleteButton.setText(name);
	}

	/**
	 * Template ��ư�� �̸��� �����Ѵ�. �⺻�� Template
	 * @param name String
	 * @deprecated
	 */
	public void setNameQueryTemplateButton(String name)
	{
		addQueryTemplateButton.setText(name);
	}

	/**
	 * Template ��ư�� �̸��� �����Ѵ�. �⺻�� Template
	 * @param name String
	 * @deprecated
	 */
	public void setNameTreeTemplateButton(String name)
	{
		addTreeTemplateButton.setText(name);
	}

	/**
	 * Template ��ư�� �̸��� �����Ѵ�. �⺻�� Template
	 * @param name String
	 * @deprecated
	 */
	public void setNameDefineTemplateButton(String name)
	{
		addDefineTemplateButton.setText(name);
	}

	/**
	 * Save as ��ư�� �̸��� �����Ѵ�. �⺻�� SaveAs
	 * @param name String
	 */
	public void setNameSaveAsButton(String name)
	{
		addSaveAsButton.setText(name);
	}

	/**
	 *
	 * @param name String
	 * @deprecated
	 */
	public void setNameWebLinkButton(String name)
	{
		webLinkButton.setText(name);
	}

	public void setWebLinkVisible()
	{
		isURL = true;
		webLinkButton.setVisible(true);
	}

	/**
	 * Template Dataset �� �˻��ϱ� ���� query�� �����Ѵ�.<br>
	 * �� method�� ������� ������ ��ư�� Ȱ��ȭ ���� �ʴ´�.<br>
	 * @param _savedQuery String                   ��ȸ�� ���� query name, �����̳� null �� ��� �ڵ� Dataset...���� ������.
	 * @param _InitAttNameArray_Template String[]  �ʱ� �� �Է��� ���� �̸� �迭, �����̳� null�� ��� default�� ������
	 * @param _InitAttValueArray_Template String[] �ʱ� �� �Է��� ���� �� �迭, �����̳� null�� ��� default�� ������
	 * @param _column_Ids_Template String[]        ����� �����ֱ� ���� column id, �����̳� null�� ��� default�� ������
	 * @param _column_Names_Template String[]      ����� �����ֱ� ���� column display name, �����̳� null�� ��� default�� ������
	 */
	public void setQueryTemplate(String _savedQuery, String[] _InitAttNameArray_Template, String[] _InitAttValueArray_Template, String[] _column_Ids_Template, String[] _column_Names_Template, String[] _column_Size_Template)
	{
		savedQuery_Template = _savedQuery;
		initAttNameArray_Template = _InitAttNameArray_Template;
		initAttValueArray_Template = _InitAttValueArray_Template;
		column_Ids_Template = _column_Ids_Template;
		column_Names_Template = _column_Names_Template;
		column_Size_Template = _column_Size_Template;
		addQueryTemplateButton.setVisible(true);
	}

	public void setTreeTemplate(String treeTitle, Object _templateRoot)
	{
		treeTemplateTitle = treeTitle;
		templateRoot = _templateRoot;
		addTreeTemplateButton.setVisible(true);
	}

	public void setDefineTemplateButton(Object _class, String _methodName)
	{
		defineClassObject = _class;
		defineMethodName = _methodName;
		addDefineTemplateButton.setVisible(true);
	}

	/**
	 * ������ �����ϴ� Dataset �� �˻��ϱ� ���� query�� �����Ѵ�.<br>
	 * �� method�� ������� ������ ��ư�� Ȱ��ȭ ���� �ʴ´�.<br>
	 *
	 * @param _savedQuery String                 ��ȸ�� ���� query name, �����̳� null �� ��� �ڵ� Dataset...���� ������.
	 * @param _InitAttNameArray_SaveAs String[]  �ʱ� �� �Է��� ���� �̸� �迭, �����̳� null�� ��� default�� ������
	 * @param _InitAttValueArray_SaveAs String[] �ʱ� �� �Է��� ���� �� �迭, �����̳� null�� ��� default�� ������
	 * @param _column_Ids_SaveAs String[]        ����� �����ֱ� ���� column id, �����̳� null�� ��� default�� ������
	 * @param _column_Names_SaveAs String[]      ����� �����ֱ� ���� column display name, �����̳� null�� ��� default�� ������
	 */
	public void setQuerySaveAs(String _savedQuery, String[] _InitAttNameArray_SaveAs, String[] _InitAttValueArray_SaveAs, String[] _column_Ids_SaveAs, String[] _column_Names_SaveAs, String[] _column_Size_SaveAs)
	{
		savedQuery_SaveAs = _savedQuery;
		initAttNameArray_SaveAs = _InitAttNameArray_SaveAs;
		initAttValueArray_SaveAs = _InitAttValueArray_SaveAs;
		column_Ids_SaveAs = _column_Ids_SaveAs;
		column_Names_SaveAs = _column_Names_SaveAs;
		column_Size_SaveAs = _column_Size_SaveAs;
		addSaveAsButton.setVisible(true);
	}

	/**
	 * �� method�� ÷�� ������ �߰� �� �� �ִ� ������ �����ϴ� method�̴�.<br>
	 * Ư�� ������ŭ ������ �����ϰ� ������ ����ϸ� �ȴ�.
	 * @param count int  �����ϰ� ���� ���� ����. default�� 0 => 0�� ������.
	 */
	public void setAttachFileCountLimit(int count)
	{
		file_count_limit = count;
	}

	public void setRelationType(String relationType)
	{
		this.relationType = relationType;
	}

	public void setDatasetNameDescription(String datasetName, String datasetDesc)
	{
		this.datasetName = datasetName;
		this.datasetDesc = datasetDesc;
	}

	/**
	 * �� method�� ÷�� ���Ͽ� �߰� �ϰ��� �ϴ� ������ Ȯ���� �� ������ �Է��ϴ� method�̴�.<br>
	 * <br>
	 *   Hashtable defineDatasetType = new Hashtable();<br>
	 *   defineDatasetType.put("txt","Text");<br>
	 *   defineDatasetType.put("doc","MSWord");<br>
	 *   defineDatasetType.put("ppt","MSPowerPoint");<br>
	 *   defineDatasetType.put("xls","MSExcel");<br>
	 *   defineDatasetType.put("jpg","Image");<br>
	 *   defineDatasetType.put("bmp","Image");<br>
	 *   defineDatasetType.put("tiff","Image");<br>
	 * <br>
	 * @param _filter Hashtable Ȯ���� key(������ �ҹ���.), dataset type(���ǵȰͰ� �����ϰ�) value�� �ϴ� hashtable . default�� ��� ����
	 * @param _description String  ���� ���� . ��) "�׸� ����" . default�� ��� ����
	 */
	public void setAttachFileFilter(Hashtable datasetMapping, String description)
	{
		filters.clear();
		filters.put(description, datasetMapping);

		DATASET_TYPE_ARRAY = getDatasetTypes();
	}

	/**
	 * Dataset Type ����
	 * @return String
	 */
	public String[] getDatasetTypes()
	{
		HashMap types = new HashMap();

		//�����ڰ� ���� ���
		if(separator == null || separator.equals(""))
		{
			return new String[]
			                  {""};
		}

		//Dataset Types
		Iterator itr = filters.values().iterator();

		while(itr.hasNext())
		{
			Hashtable map = (Hashtable)itr.next();

			Iterator vals = map.values().iterator();

			while(vals.hasNext())
			{
				String type = (String)vals.next();
				types.put(type, type);
			}
		}

		//�����ڷ� ����
		Object[] typeArray = types.keySet().toArray();
		String typeStr = "";
		for(int i = 0; i < typeArray.length; i++)
		{
			typeStr += typeArray[i] + (i == (typeArray.length - 1) ? "" : separator);
		}

		return new String[]
		                  {typeStr};
	}

	/**
	 * Add Choosable File Filter
	 */
	public void addChoosableFileFilter(Hashtable datasetMapping, String description)
	{
		filters.put(description, datasetMapping);

		DATASET_TYPE_ARRAY = getDatasetTypes();
	}

	/**
	 * File Filter ����
	 * @param chooser JFileChooser
	 */
	private void setFileFilter(JFileChooser chooser)
	{
		Iterator keys = filters.keySet().iterator();

		while(keys.hasNext())
		{
			String key = (String)keys.next();
			Hashtable map = (Hashtable)filters.get(key);
			Object[] oexts = map.keySet().toArray();
			String[] exts = new String[oexts.length];

			for(int i = 0; i < oexts.length; i++)
			{
				exts[i] = (String)oexts[i];
			}

			AttachFileFilter filter = new AttachFileFilter(exts, key);
			chooser.addChoosableFileFilter(filter);
		}
	}

	/**
	 * Ȯ����
	 * @param ext String
	 * @return boolean
	 */
	private boolean containsExtension(String ext)
	{
		Iterator itr = filters.values().iterator();

		while(itr.hasNext())
		{
			Hashtable map = (Hashtable)itr.next();

			if(map.containsKey(ext))
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Ȯ����
	 * @param ext String
	 * @return boolean
	 */
	private boolean containsType(String type)
	{
		Iterator itr = filters.values().iterator();
		while(itr.hasNext())
		{
			Hashtable map = (Hashtable)itr.next();
			if(map.containsValue(type))
			{
				return true;
			}
		}

		return false;
	}

	public void addChildAttachList(TCComponent parentComponent) throws Exception
	{
		if(parentComponent == null)
		{
			return;
		}
		AIFComponentContext[] aifChildren = null;
		if(relationType == null || relationType.equals(""))
		{
			aifChildren = parentComponent.getChildren();
		} else
		{
			aifChildren = parentComponent.getRelated(relationType);
		}
		for(int i = 0; i < aifChildren.length; i++)
		{
			if(containsType(aifChildren[i].getComponent().getType()))
			{
				listModel.addElement(new AttachFileComponent(aifChildren[i], AttachFileComponent.NORMAL, null, null));
			}

			if(isURL && aifChildren[i].getComponent().getType().equals("Web Link"))
			{
				listModel.addElement(new AttachFileComponent((TCComponentForm)aifChildren[i].getComponent(), AttachFileComponent.NORMAL));
			}
		}
		fileList.updateUI();
	}

	/**
	 * ������ �߰��� dataset���� �߰��Ѵ�.
	 * @param datasets TCComponent[]
	 */
	public void addAttachList(TCComponent[] datasets)
	{
		for(int i = 0; i < datasets.length; i++)
		{
			if(containsType(datasets[i].getType()))
			{
				listModel.addElement(new AttachFileComponent(new AIFComponentContext(null, datasets[i], null), AttachFileComponent.NORMAL, null, null));
			}
		}
		fileList.updateUI();
	}

	/**
	 * ������ �߰��� revision �߰��Ѵ�.
	 * @param revisions TCComponent[]
	 */
	public void addAttachItemAndRevisionList(TCComponent[] revisions)
	{
		for(int i = 0; i < revisions.length; i++)
		{
			listModel.addElement(new AttachFileComponent(new AIFComponentContext(null, revisions[i], null), AttachFileComponent.NORMAL, null, null));
		}
		fileList.updateUI();
	}

	/**
	 * ������ �߰��� dataset���� �߰��Ѵ�.
	 * @param datasets AttachFileComponent[]
	 */
	public void addAttachList(AttachFileComponent[] datasets)
	{
		for(int i = 0; i < datasets.length; i++)
		{
			listModel.addElement(datasets[i]);
		}
		fileList.updateUI();
	}

	/**
	 * �� method�� attachfilepanel�� �б� �������� ����Ұ����� ���� �����ϰ� ����� �������� �Ǵ��ϰ� �ϴ� method�̴�.
	 *
	 * @param _isModifiable boolean default�� �б� ���� �����̴�.
	 */
	public void setModifiable(boolean _isModifiable)
	{
		isModifiable = _isModifiable;
		addFileButton.setEnabled(isModifiable);
		deleteButton.setEnabled(isModifiable);
		addQueryTemplateButton.setEnabled(isModifiable);
		addTreeTemplateButton.setEnabled(isModifiable);
		addDefineTemplateButton.setEnabled(isModifiable);
		addSaveAsButton.setEnabled(isModifiable);
		modifyDatasetInfoButton.setEnabled(isModifiable);
		if(!isModifiable)
		{
			fileList.setBackground(getBackground());
		} else
		{
			fileList.setBackground(Color.WHITE);
		}
	}

	public void setVisibleDefineTemplateButton(boolean isVisible)
	{
		addDefineTemplateButton.setVisible(isVisible);
	}

	public void setVisibleModifybutton(boolean isVisible)
	{
		modifyDatasetInfoButton.setVisible(isVisible);
	}
	
	public void setSaveAsButtonbutton(boolean isVisible)
	{
		addSaveAsButton.setVisible(isVisible);
	}

	/**
	 * �� method�� dataset�̳� file�� �ϰ� �߰��� �� ����Ѵ�.<br>
	 * dataset(AIFComponentContext) list �Ǵ� file list�� vector�� �־� �Ѱ��ָ� ����Ʈ�� �ڵ����� �ѷ��ش�.<br>
	 * ����, ���� �־ ����� ����.
	 * @param datasetVector Vector AIFComponentContext vector or File vector
	 */
	public void setAttachDataVector(Vector dataVector) throws Exception
	{
		Enumeration enum1 = dataVector.elements();

		while(enum1.hasMoreElements())
		{
			if(!isLimitCount())
			{
				return;
			}
			Object obj = enum1.nextElement();
			if(obj instanceof AIFComponentContext)
			{
				addListModel(new AttachFileComponent((AIFComponentContext)obj, AttachFileComponent.ADD, datasetName, datasetDesc));
			} else if(obj instanceof File)
			{
				addListModel(new AttachFileComponent((File)obj, getFileDatasetType((File)obj), AttachFileComponent.ADD, datasetName, datasetDesc));
			} else if(obj instanceof TCComponentForm)
			{
				if(((TCComponentForm)obj).getType().equals("Web Link"))
				{
					addListModel(new AttachFileComponent((TCComponentForm)obj, AttachFileComponent.ADD));
				}
			}
		}
	}

	/**
	 * List�� Update�ϴ� method�̴�. �����̳� dataset�� �߰��ϰ� �Ⱥ��� ��� �����ϸ� ��Ÿ����.
	 */
	public void refreshList()
	{
		fileList.updateUI();
	}

	/**
	 * �� method�� ���� �� attachfilepanel�� �߰��� ���� �� AIFComponentContext�� �������� �ִ� method�̴�.
	 * @return AttachFileComponent[]  AttachFileComponent �迭�� ���ϵȴ�. �� �迭���� �߰� �����Ǵ� �͵��� ȥ�յǾ� �ִ�.
	 */
	public AttachFileComponent[] getAttachFileComponent()
	{
		int i = 0;
		Enumeration enum1 = listModel.elements();
		AttachFileComponent[] attachFileComponent = new AttachFileComponent[listModel.size()];
		while(enum1.hasMoreElements())
		{
			attachFileComponent[i] = (AttachFileComponent)enum1.nextElement();
			i++;
		}
		return attachFileComponent;
	}

	/**
	 * AttachPanel�� ��� ���ϵ��� �����ϴ� method�̴�.<br>
	 * ���� ��ü�� dataset ��ü ��� �����ȴ�.<br>
	 * �� ���� : �Ϲ� ���������� ����ϵ��� �Ѵ�.
	 */
	public void clearAttachFileComponent()
	{
		while(listModel.getSize() != 0)
		{
			AttachFileComponent attachFileComponent = (AttachFileComponent)listModel.get(0);
			listModel.removeElement(attachFileComponent);
		}
		refreshList();
	}

	/**
	 * �� method�� ������ �߰��ϴ� method�̴�.
	 * @param e ActionEvent
	 */
	public void addFileButton_actionPerformed(ActionEvent e)
	{
		if(!isLimitCount())
		{
			return;
		}

		File[] selectedFile = null;
		
		if(jfc == null)
		{
			// ���� ��θ� Ȯ�� �Ͽ� null�� �ƴϸ� ������ ������ ��θ� ����.
			String strCookieDir = Utilities.getCookie("filechooser", "Chooser.DIR", true);
			if (strCookieDir == null) {
				strCookieDir = "";
			}
						
			jfc = new JFileChooser(strCookieDir);
			if(filters.size() > 0)
			{
				jfc.removeChoosableFileFilter(jfc.getAcceptAllFileFilter());

				//File Filter ����
				setFileFilter(jfc);
			}
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			jfc.setMultiSelectionEnabled(true);
		}
		int r = jfc.showOpenDialog(Utilities.getCurrentWindow());
		if(r == JFileChooser.APPROVE_OPTION)
		{
			selectedFile = jfc.getSelectedFiles();
			if(selectedFile != null && selectedFile.length > 0)
			{
				String errorListString = "";
				// ���� ���õ� ������ ��θ� ��⿡ �� ����.
				String chooserDir = jfc.getSelectedFile().getAbsolutePath().substring(0, jfc.getSelectedFile().getAbsolutePath().lastIndexOf("\\"));
				Utilities.setCookie("filechooser", true, "Chooser.DIR", chooserDir);
				
				for(int iCnt = 0; iCnt < selectedFile.length; iCnt++)
				{
					try
					{
						if(!isLimitCount())
						{
							break;
						}
						if(!selectedFile[iCnt].exists())
						{
							errorListString += "\n" + selectedFile[iCnt] + registry.getString("attach_filenotexist.MESSAGE");
							continue;
						}
						addAttachFileList(selectedFile[iCnt]);
					} catch(Exception ex)
					{
						ex.printStackTrace();
						errorListString += "\n" + ex.getMessage();
					}
				}
				alertMessage(errorListString);
			}
		}
		fileList.updateUI();
	}

	/**
	 * �� method�� ������ �����ϴ� method�̴�.<br>
	 * imancomponentdataset�� ��� ���� ǥ�ø� �ϰ� ���� ������ ���� �ʰ� ������ �����Ѵ�.
	 * @param e ActionEvent
	 */
	public void deleteButton_actionPerformed(ActionEvent e)
	{
		//Object[] object = fileList.getSelectedValues();
		
		List<Object> object = fileList.getSelectedValuesList();
		
		if(object == null)
		{
			return;
		}
		//for(int i = 0; i < object.length; i++)
		for(int i = 0; i < object.size(); i++)
		{
			//AttachFileComponent attachFileComponent = (AttachFileComponent)object[i];
			AttachFileComponent attachFileComponent = (AttachFileComponent)object.get(i);
			
			if(attachFileComponent.isFileComponent()){
				listModel.removeElement(attachFileComponent);
			}
			else if(((AIFComponentContext)attachFileComponent.getAttachObject()).getComponent() instanceof TCComponentItem || ((AIFComponentContext)attachFileComponent.getAttachObject()).getComponent() instanceof TCComponentItemRevision){
				listModel.removeElement(attachFileComponent);
				return;
			}
			if(attachFileComponent.isAIFComponentContext() || attachFileComponent.isWebLinkComponent())
			{
				if(attachFileComponent.getState() == AttachFileComponent.ADD)
				{
					listModel.removeElement(attachFileComponent);
				} else if(attachFileComponent.getState() == AttachFileComponent.DELETE)
				{
					if(isLimitCount())
					{
						attachFileComponent.setState(AttachFileComponent.NORMAL);
					} else
					{
						return;
					}
				} else
				{
					attachFileComponent.setState(AttachFileComponent.DELETE);
				}
			} else
			{
				listModel.removeElement(attachFileComponent);
			}
		}
		fileList.updateUI();
	}

	private void fileList_mouseClicked(MouseEvent e)
	{
		if(e.getClickCount() < 2)
		{
			return;
		}
		AttachFileComponent attachFileComp = (AttachFileComponent)fileList.getSelectedValue();
		if(attachFileComp == null)
		{
			return;
		}
		if(attachFileComp.isAIFComponentContext())
		{
			try
			{
				boolean isWritable = (attachFileComp.getState() == AttachFileComponent.ADD) ? false : isModifiable;
				if(!isWritable)
				{
					MessageBox.post("�� Dataset�� �б� �������� Open �˴ϴ�. �����Ͻ� ������ ������� �ʽ��ϴ�.\n\"Ȯ��\"�� Ŭ���Ͻø� ������ �����ϴ�.", "�б�����", MessageBox.INFORMATION);
				}
				TCComponentDataset dataset = (TCComponentDataset)((AIFComponentContext)attachFileComp.getAttachObject()).getComponent();
				dataset.open(isWritable);

			} catch(Exception ex)
			{
				MessageBox.post(ex);
				return;
			}
		}
		if(attachFileComp.isWebLinkComponent())
		{
			OpenCommand opencommand = new OpenCommand(AIFUtility.getActiveDesktop(), (TCComponent)attachFileComp.getAttachObject());
			opencommand.executeModeless();
		}
		if(attachFileComp.isFileComponent())
		{
			try
			{
				Registry registry = Registry.getRegistry("client_specific");
				Runtime.getRuntime().exec("cmd /C \"\"" + registry.getString("runnerCommand") + "\" \"" + ((File)attachFileComp.getAttachObject()).getAbsolutePath() + "\"\"");
			} catch(IOException ex1)
			{
				MessageBox.post(ex1);
				return;
			}
		}
	}

	/**
	 * ������ ����Ʈ�� �߰��Ѵ�.
	 * @param selectedFile File
	 */
	private void addAttachFileList(File selectedFile) throws Exception
	{
		AttachFileComponent attachFileComponent = new AttachFileComponent(selectedFile, getFileDatasetType(selectedFile), AttachFileComponent.ADD, datasetName, datasetDesc);
		Enumeration enum1 = listModel.elements();

		while(enum1.hasMoreElements())
		{
			AttachFileComponent tmp = (AttachFileComponent)enum1.nextElement();
			if(tmp.isFileComponent() && tmp.toString().equalsIgnoreCase(attachFileComponent.toString()))
			{
				String strExtFile = ((File)tmp.getAttachObject()).getAbsolutePath();
				throw new Exception(registry.getString("attach_existfile.MESSAGE") + strExtFile);
			}
		}
		addListModel(attachFileComponent);
	}

	public void addAttachDatasetList(AIFComponentContext selectedDataset) throws Exception
	{
		AttachFileComponent attachFileComponent = new AttachFileComponent(selectedDataset, AttachFileComponent.ADD, datasetName, datasetDesc);
		Enumeration enum1 = listModel.elements();

		while(enum1.hasMoreElements())
		{
			AttachFileComponent tmp = (AttachFileComponent)enum1.nextElement();
			if(!tmp.isAIFComponentContext())
			{
				continue;
			}
			String uid = ((TCComponent)((AIFComponentContext)tmp.getAttachObject()).getComponent()).getUid();
			if(uid.equalsIgnoreCase(((TCComponent)selectedDataset.getComponent()).getUid()))
			{
				throw new Exception(registry.getString("attach_existdataset.MESSAGE") + tmp.toString());
			}
		}
		addListModel(attachFileComponent);
	}

	private void addAttachWebLinkList(TCComponentForm webLinkObj) throws Exception
	{
		AttachFileComponent attachFileComponent = new AttachFileComponent(webLinkObj, AttachFileComponent.ADD);
		Enumeration enum1 = listModel.elements();

		while(enum1.hasMoreElements())
		{
			AttachFileComponent tmp = (AttachFileComponent)enum1.nextElement();
			if(!tmp.isWebLinkComponent())
			{
				continue;
			}
			String uid = ((TCComponent)(tmp.getAttachObject())).getUid();
			if(uid.equalsIgnoreCase(webLinkObj.getUid()))
			{
				throw new Exception(registry.getString("attach_existurl.MESSAGE") + tmp.toString());
			}
		}
		addListModel(attachFileComponent);
	}

	/**
	 * ����Ʈ�� �߰��ϱ� ���� ���͸��� �Ѵ�.
	 * @param attachFileComponent AttachFileComponent
	 * @throws Exception
	 */
	private void addListModel(AttachFileComponent attachFileComponent) throws Exception
	{
		if(attachFileComponent.isFileComponent())
		{
			if(containsExtension(attachFileComponent.getFileExtend()))
			{
				listModel.addElement(attachFileComponent);
			} else
			{
				throw new Exception(registry.getString("attach_notdefinefile.MESSAGE"));
			}
		} else if(attachFileComponent.isAIFComponentContext())
		{
			//            if(containsType(attachFileComponent.getDatasetType()))
			//            {
			listModel.addElement(attachFileComponent);
			//            } else
			//            {
			//                throw new Exception(registry.getString("attach_notdefinedataset.MESSAGE"));
			//            }
		}
		else if(attachFileComponent.isWebLinkComponent())
		{
			if(isURL)
			{
				listModel.addElement(attachFileComponent);
			}
		}
	}

	/**
	 * ���� ���� ������ �ɸ��� �޼����� �����ְ� false �� return �Ѵ�.
	 * @return
	 */
	public boolean isLimitCount()
	{
		//[2007-01-09] ���� ���� ���ѿ��� AIFComponentContext �̸鼭 State �� DELETE �� ���� �����Ѵ�.
		int iFileComponentCount = 0;
		Enumeration enumList = listModel.elements();
		while(enumList.hasMoreElements())
		{
			AttachFileComponent tmp = (AttachFileComponent)enumList.nextElement();
			if(tmp.isAIFComponentContext() && tmp.getState() == AttachFileComponent.DELETE)
			{
				continue;
			}
			iFileComponentCount++;
		}

		if(file_count_limit != 0 && file_count_limit <= iFileComponentCount)
		{
			MessageBox.post(registry.getString("attach_filecount.MESSAGE") + " [limit = " + file_count_limit + " ]", registry.getString("Message.TITLE"), MessageBox.INFORMATION);
			return false;
		}

		return true;
	}

	/**
	 * alertExtendVector, alertExistVector �� ���� ���� ��� ��� �޼����� �����ش�.
	 */
	private void alertMessage(String errorListMessage)
	{
		if(errorListMessage != null && !errorListMessage.trim().equals(""))
		{
			errorListMessage = "Error List \n" + errorListMessage;
			MessageBox.post(errorListMessage, registry.getString("Message.TITLE"), MessageBox.INFORMATION);
		}
	}

	private void addQueryTemplateButton_actionPerformed(ActionEvent e)
	{
		templateFindDialog = addDatasetAction(templateFindDialog, registry.getString("attach_searchtemplate.TITLE"), true, savedQuery_Template, initAttNameArray_Template, initAttValueArray_Template, column_Ids_Template, column_Names_Template, column_Size_Template);
	}

	private void addTreeTemplateButton_actionPerformed(ActionEvent e)
	{
		Window currentWindow = Utilities.getCurrentWindow();
		TemplateDialog templateDialog = null;
		if(currentWindow instanceof Frame)
		{
			templateDialog = new TemplateDialog((Frame)currentWindow, treeTemplateTitle, true, templateRoot);
		} else
		{
			templateDialog = new TemplateDialog((Dialog)currentWindow, treeTemplateTitle, true, templateRoot);
		}
		templateDialog.run();
		if(templateDialog.getAction() == TemplateDialog.ACTION_SELECT)
		{
			AIFComponentContext resultAIFContext = templateDialog.getSelectedAIFComponentContext();
			String errorMessage = "";
			try
			{
				if(!isLimitCount())
				{
					return;
				}
				addAttachDatasetList(resultAIFContext);
			} catch(Exception ex)
			{
				errorMessage += "\n" + ex.getMessage();
			}
			alertMessage(errorMessage);
		}
	}

	private void addDefineTemplateButton_actionPerformed(ActionEvent e)
	{
		try
		{
			if(defineClassObject == null)
			{
				return;
			}
			Utilities.invokeMethod(defineClassObject, defineMethodName, new Object[0]);
		} catch(Exception ex)
		{
			MessageBox.post(ex, true);
			ex.printStackTrace();
		}
	}

	public void addSaveAsButton_actionPerformed(ActionEvent e)
	{
		saveasFindDialog = addDatasetAction(saveasFindDialog, registry.getString("attach_searchexist.TITLE"), true, savedQuery_SaveAs, initAttNameArray_SaveAs, initAttValueArray_SaveAs, column_Ids_SaveAs, column_Names_SaveAs, column_Size_SaveAs);
	}

	private FindDialog addDatasetAction(FindDialog findDialog, String title, boolean isModal, String savedQuery, String[] initAttNameArray, String[] initAttValueArray, String[] column_Ids, String[] column_Names, String[] column_size)
	{
		if(!isLimitCount())
		{
			return null;
		}
		if(findDialog == null)
		{
			Window currentWindow = Utilities.getCurrentWindow();
			if(currentWindow instanceof Frame)
			{
				findDialog = new FindDialog((Frame)currentWindow, title, isModal);
				findDialog.setQuerySetting(savedQuery, initAttNameArray, initAttValueArray);
				findDialog.setColumnSetting(column_Ids, column_Names, column_size, savedQuery);
			} else
			{
				findDialog = new FindDialog((Dialog)currentWindow, title, isModal);
				findDialog.setQuerySetting(savedQuery, initAttNameArray, initAttValueArray);
				findDialog.setColumnSetting(column_Ids, column_Names, column_size, savedQuery);
			}
		}
		findDialog.setSelectionMode(FindDialog.SELECTION_MULTI);
		findDialog.setVisibleQuerySelectionButton(true);
		findDialog.run();
		if(findDialog.getAction() == FindDialog.ACTION_SELECT)
		{
			AIFComponentContext[] resultAIFContext = findDialog.getSelectedAIFComponentContexts();
			String errorMessage = "";
			for(int i = 0; i < resultAIFContext.length; i++)
			{
				try
				{
					if(!isLimitCount())
					{
						break;
					}
					addAttachDatasetList(resultAIFContext[i]);
				} catch(Exception ex)
				{
					ex.printStackTrace();
					errorMessage += "\n" + ex.getMessage();
				}
			}
			alertMessage(errorMessage);
		}
		return findDialog;
	}

	public static TCComponentDataset createDataset(String datasetName, AttachFileComponent attachFileComponent) throws Exception
	{
		TCComponentDataset newDataset = null;
		try
		{
			TCComponentDatasetType imancomponentdatasettype = (TCComponentDatasetType)CustomUtil.getTCSession().getTypeComponent(attachFileComponent.getDatasetType());
//			newDataset = imancomponentdatasettype.setFiles(attachFileComponent.getNewDatasetName(), attachFileComponent.getNewDatasetDesc(),
			newDataset = imancomponentdatasettype.setFiles(datasetName, attachFileComponent.getNewDatasetDesc(),
					attachFileComponent.getDatasetType(), new String[]
					                                                 {attachFileComponent.getFilePath()}, new String[]
					                                                                                                 {attachFileComponent.getFileNamedReferenceType()});
		} catch(Exception imanexception)
		{
			imanexception.printStackTrace();
			throw imanexception;
		}
		return newDataset;
	}


	/**
	 * Drag & Drop List
	 * @author taejungg kim
	 */
	public class DropList extends JList implements DropTargetListener, DragSourceListener, DragGestureListener
	{
		private static final long serialVersionUID = 1L;

		//Ž���⿡�� Drop ����� ����Ϸ��� �Ʒ��� �ּ��� Ǭ��....
		DropTarget dropTarget = new DropTarget(this, this);
		DragSource dragSource = DragSource.getDefaultDragSource();

		public DropList()
		{
			dragSource.createDefaultDragGestureRecognizer(this, DnDConstants.ACTION_COPY_OR_MOVE, this);
		}

		public void dragEnter(DropTargetDragEvent dropTargetDragEvent)
		{
			dropTargetDragEvent.acceptDrag(DnDConstants.ACTION_COPY_OR_MOVE);
		}

		public synchronized void drop(DropTargetDropEvent dropTargetDropEvent)
		{
			try
			{
				Transferable transferable = dropTargetDropEvent.getTransferable();
				//Drop�� �����Ͱ� file �� ��쿡��
				if(transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor))
				{
					dropTargetDropEvent.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					List drogList = (List)transferable.getTransferData(DataFlavor.javaFileListFlavor);
					Iterator iterator = drogList.iterator();
					String errorMessage = "";
					while(iterator.hasNext())
					{
						if(!isLimitCount())
						{
							return;
						}

						File drogFile = (File)iterator.next();
						if(drogFile.isDirectory())
						{
							continue;
						}

						//                        if(filters.size() > 0)
						//                        {
						//                            FileFilter fileFilter = jfc.getFileFilter();
						//                            if(!fileFilter.accept(drogFile))
						//                            {
						//                                errorMessage += "\n" + registry.getString("attach_extensionproblem.MESSAGE") + drogFile.getAbsolutePath();
						//                                continue;
						//                            }
						//                        }

						try
						{
							addAttachFileList(drogFile);
						} catch(Exception ex)
						{
							errorMessage += "\n" + ex.getMessage();
							ex.printStackTrace();
							continue;
						}
					}
					dropTargetDropEvent.getDropTargetContext().dropComplete(true);
					alertMessage(errorMessage);
				} else
				{
					dropTargetDropEvent.rejectDrop();
				}
			} catch(Exception ex1)
			{
				//                dropTargetDropEvent.rejectDrop();
				MessageBox.post(ex1);
				ex1.printStackTrace();
			}
		}

		public void dragDropEnd(DragSourceDropEvent DragSourceDropEvent)
		{}

		public void dragEnter(DragSourceDragEvent DragSourceDragEvent)
		{}

		public void dragExit(DragSourceEvent DragSourceEvent)
		{}

		public void dragExit(DropTargetEvent dropTargetEvent)
		{}

		public void dragOver(DragSourceDragEvent DragSourceDragEvent)
		{}

		public void dragOver(DropTargetDragEvent dropTargetDragEvent)
		{}

		public void dragGestureRecognized(DragGestureEvent dragGestureEvent)
		{}

		public void dropActionChanged(DragSourceDragEvent DragSourceDragEvent)
		{}

		public void dropActionChanged(DropTargetDragEvent dropTargetDragEvent)
		{}
	}


	/**
	 * dataset�� �߰� ������ �����Ѵ�.
	 * @param parentComponent TCComponent
	 * @return String
	 * @throws Exception
	 */
	public String attachOperation(TCComponent parentComponent) throws Exception
	{
		if(parentComponent == null)
		{
			return registry.getString("attach_parentnull.MESSAGE");
		}
		
		boolean isSetRelationType = relationType != null && !relationType.equals("");
		String resultString = "";
		String errorString = "[ERROR]";
		String newDatasetName = parentComponent.getProperty("item_id") + "/" + parentComponent.getProperty("item_revision_id");
		
		try
		{
			AttachFileComponent[] attachFileComponent = getAttachFileComponent();
			for(int i = 0; i < (attachFileComponent == null ? 0 : attachFileComponent.length); i++)
			{
				//���� �߰��� ������ �ִ°��...
				if(attachFileComponent[i].isFileComponent())
				{
					File selectedFile = (File)attachFileComponent[i].getAttachObject();
					String datasetName = attachFileComponent[i].getNewDatasetName();
					for(int j = 32; j > 0; j--)
					{
						if(datasetName.getBytes().length > 32)
						{
							if(datasetName.length() >= j)
							{
								datasetName = datasetName.substring(0, j);
							}
						} else
						{
							break;
						}
					}
					TCComponentDataset newDataset = createDataset(newDatasetName, attachFileComponent[i]);
					
					// Dataset �Ӽ��� ����� Item�� Item Rev ���� �����Ѵ�.
//					String newRevisionId = parentComponent.getProperty("item_id");
//					String newRevisionRev = parentComponent.getProperty("item_revision_id");
//					newDataset.setProperty("object_desc", newRevisionId + "/" + newRevisionRev);
					
					// Part�� item_id�� revision ���
//					String newRevisionId = newRevision.getProperty("item_id");
//					String newRevisionRev = newRevision.getProperty("item_revision_id");
//					System.out.println(" ------- type = " + type + " && relations = " + relations);
//					System.out.println(" ------- Part No = " + newRevisionId + "/" + newRevisionRev);
					
					
					parentComponent.add(isSetRelationType ? relationType : parentComponent.getPreferredPasteRelation(newDataset), newDataset);
					resultString += "ADD      = [Dataset Name : " + datasetName + "] [Import File : " + selectedFile.getAbsolutePath() + "]\n";
				} else if(attachFileComponent[i].isAIFComponentContext())
				{
					//���� Dataset �߰�
					if(attachFileComponent[i].getState() == AttachFileComponent.ADD)
					{
						AIFComponentContext context = (AIFComponentContext)attachFileComponent[i].getAttachObject();
						TCComponentDataset saveAsDataset = ((TCComponentDataset)context.getComponent()).saveAs(attachFileComponent[i].getNewDatasetName());
						saveAsDataset.setProperty("object_desc", attachFileComponent[i].getNewDatasetDesc());
						parentComponent.add(isSetRelationType ? relationType : parentComponent.getPreferredPasteRelation(saveAsDataset), saveAsDataset);
						resultString += "SaveAs = [Dataset Name : " + saveAsDataset.toString() + "] [Base Dataset : " + ((TCComponentDataset)context.getComponent()).toString() + "]\n";
					}
					//�̹� �����ϴ� dataset�� ����...
					else if(attachFileComponent[i].getState() == AttachFileComponent.DELETE)
					{
						AIFComponentContext context = (AIFComponentContext)attachFileComponent[i].getAttachObject();

						if(context.getParentComponent() == null){
							listModel.removeElement(context);
						}
						else{
							String removeDatasetName = context.getComponent().toString();
							((TCComponent)context.getParentComponent()).remove(context.getContext().toString(), (TCComponent)context.getComponent());
							((TCComponent)context.getComponent()).delete();
							resultString += "Remove   = [Dataset Name : " + removeDatasetName + "]\n";
						}
					}
				}
				else if(attachFileComponent[i].isWebLinkComponent())
				{
					TCComponentForm webLinkComponent = (TCComponentForm)attachFileComponent[i].getAttachObject();
					if(attachFileComponent[i].getState() == AttachFileComponent.ADD)
					{
						webLinkComponent.save();
						parentComponent.add(isSetRelationType ? relationType : parentComponent.getPreferredPasteRelation(webLinkComponent), webLinkComponent);
					} else if(attachFileComponent[i].getState() == AttachFileComponent.DELETE)
					{
						AIFComponentContext[] child = parentComponent.getChildren();
						for(int j = 0; j < child.length; j++)
						{
							if(((TCComponent)child[j].getComponent()).getUid().equals(webLinkComponent.getUid()))
							{
								parentComponent.remove(child[j].getContext().toString(), webLinkComponent);
								webLinkComponent.delete();
								break;
							}
						}
					}
				}
			}
		} catch(Exception ex)
		{
			errorString += "\n" + ex.getMessage();
			ex.printStackTrace();
		}
		if(!errorString.equalsIgnoreCase("[ERROR]"))
		{
			throw new Exception(errorString);
		}
		return resultString;

	}

	private void modifyDatasetInfoButton_actionPerformed(ActionEvent e)
	{
//		Object[] object = fileList.getSelectedValues();
//		
//		if(object == null || object.length == 0)	
//		{
//			return;
//		}
//		if(object.length > 1)
//		{
//			MessageBox.post(registry.getString("attach_nooneselect.MESSAGE"), "Warning", MessageBox.WARNING);
//			return;
//		}
//		AttachFileComponent attachFileComponent = (AttachFileComponent)object[0];
		
		List<Object> object = fileList.getSelectedValuesList();
		if(object == null || object.size() == 0)
		{
			return;
		}
		if(object.size() > 1)
		{
			MessageBox.post(registry.getString("attach_nooneselect.MESSAGE"), "Warning", MessageBox.WARNING);
			return;
		}
		AttachFileComponent attachFileComponent = (AttachFileComponent)object.get(0);

		if(attachFileComponent.isWebLinkComponent())
		{
			return;
		}
		if(attachFileComponent.getState() != AttachFileComponent.ADD)
		{
			MessageBox.post(registry.getString("attach_nonadded.MESSAGE"), "Warning", MessageBox.WARNING);
			return;
		}
		Window currentWindow = Utilities.getCurrentWindow();
		if(currentWindow instanceof Frame)
		{
			new ModifyDatasetInfo((Frame)currentWindow, attachFileComponent);
		} else
		{
			new ModifyDatasetInfo((Dialog)currentWindow, attachFileComponent);
		}
		fileList.updateUI();
	}

	public void webLinkButton_actionPerformed(ActionEvent e)
	{
		Window currentWindow = Utilities.getCurrentWindow();
		if(currentWindow instanceof Frame)
		{
			new NewWebLinkDialog((Frame)currentWindow);
		} else
		{
			new NewWebLinkDialog((Dialog)currentWindow);
		}
		fileList.updateUI();
	}

	private class ModifyDatasetInfo extends JDialog
	{
		private static final long serialVersionUID = 1L;

		private JPanel contentsPane;
		private AttachFileComponent attachFileComponent;
		private iTextField nameTextField = new iTextField(20, 32, true);
		private iTextArea descTextArea = new iTextArea(4, 40, 240, false);

		public ModifyDatasetInfo(Frame buttonParent, AttachFileComponent _attachFileComponent)
		{
			super(buttonParent, "", true);
			attachFileComponent = _attachFileComponent;
			initUI();
			setUndecorated(true);
			pack();
			setLocationRelativeTo(modifyDatasetInfoButton);
			setVisible(true);
		}

		public ModifyDatasetInfo(Dialog buttonParent, AttachFileComponent _attachFileComponent)
		{
			super(buttonParent, "", true);
			attachFileComponent = _attachFileComponent;
			initUI();
			setUndecorated(true);
			pack();
			setLocationRelativeTo(modifyDatasetInfoButton);
			setVisible(true);
		}

		private void initUI()
		{
			contentsPane = (JPanel)getContentPane();
			contentsPane.setLayout(new VerticalLayout());
			contentsPane.setBorder(new EtchedBorder());
			JPanel mainPanel = new JPanel(new PropertyLayout());
			JLabel nameLabel = new JLabel(registry.getString("attach_name.NAME"));
			JLabel descLabel = new JLabel(registry.getString("attach_desc.NAME"));
			nameTextField.setText(attachFileComponent.getNewDatasetName());
			descTextArea.setText(attachFileComponent.getNewDatasetDesc());
			mainPanel.add("1.1.left", nameLabel);
			mainPanel.add("1.2", nameTextField);
			mainPanel.add("2.1.left", descLabel);
			mainPanel.add("2.2", new JScrollPane(descTextArea));
			mainPanel.setOpaque(false);
			JPanel buttonPanel = new JPanel();
			JButton okButton = new JButton(registry.getString("okButton.NAME"));
			JButton cancelButton = new JButton(registry.getString("cancelButton.NAME"));
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			buttonPanel.setOpaque(false);
			contentsPane.add("unbound.bind", mainPanel);
			contentsPane.add("bottom.bind", buttonPanel);
			contentsPane.add("bottom.bind", new Separator());
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String modifyName = nameTextField.getText().trim();
					if(modifyName.equals(""))
					{
						MessageBox.post(ModifyDatasetInfo.this, registry.getString("attach_requirename.MESSAGE"), "Warning", MessageBox.WARNING);
						return;
					}
					attachFileComponent.setNewDatasetName(modifyName);
					attachFileComponent.setNewDatasetDesc(descTextArea.getText());
					setVisible(false);
					dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setVisible(false);
					dispose();
				}
			});
			contentsPane.setBackground(Color.white);
		}
	}


	private class NewWebLinkDialog extends JDialog
	{
		private static final long serialVersionUID = 1L;

		private JPanel contentsPane;
		private iTextField nameTextField = new iTextField(20, 32, true);
		private iTextField urlTextField = new iTextField(40, 2083, true);
		private iTextArea descTextArea = new iTextArea(4, 55, 240, false);

		public NewWebLinkDialog(Frame buttonParent)
		{
			super(buttonParent, "", true);
			initUI();
			setUndecorated(true);
			pack();
			setLocationRelativeTo(webLinkButton);
			setVisible(true);
		}

		public NewWebLinkDialog(Dialog buttonParent)
		{
			super(buttonParent, "", true);
			initUI();
			setUndecorated(true);
			pack();
			setLocationRelativeTo(webLinkButton);
			setVisible(true);
		}

		private void initUI()
		{
			contentsPane = (JPanel)getContentPane();
			contentsPane.setLayout(new VerticalLayout());
			contentsPane.setBorder(new EtchedBorder());
			JPanel mainPanel = new JPanel(new PropertyLayout());
			JLabel nameLabel = new JLabel(registry.getString("attach_name.NAME"));
			JLabel urlLabel = new JLabel(registry.getString("attach_url.NAME"));
			JLabel descLabel = new JLabel(registry.getString("attach_desc.NAME"));
			mainPanel.add("1.1.right.top", nameLabel);
			mainPanel.add("1.2", nameTextField);
			mainPanel.add("2.1.right.top", urlLabel);
			mainPanel.add("2.2", urlTextField);
			mainPanel.add("3.1.right.top", descLabel);
			mainPanel.add("3.2", new JScrollPane(descTextArea));
			mainPanel.setOpaque(false);
			JPanel buttonPanel = new JPanel();
			JButton okButton = new JButton(registry.getString("OK.TEXT"));
			JButton cancelButton = new JButton(registry.getString("Cancel.TEXT"));
			buttonPanel.add(okButton);
			buttonPanel.add(cancelButton);
			buttonPanel.setOpaque(false);
			contentsPane.add("unbound.bind", mainPanel);
			contentsPane.add("bottom.bind", buttonPanel);
			contentsPane.add("bottom.bind", new Separator());
			okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					String modifyName = nameTextField.getText().trim();
					if(modifyName.equals(""))
					{
						MessageBox.post(NewWebLinkDialog.this, registry.getString("attach_requirename.MESSAGE"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
						return;
					}
					String urlName = urlTextField.getText().trim();
					if(urlName.equals(""))
					{
						MessageBox.post(NewWebLinkDialog.this, registry.getString("attach_requireurl.MESSAGE"), registry.getString("Message.TITLE"), MessageBox.INFORMATION);
						return;
					}
					try
					{
						TCComponentFormType imancomponentformtype = (TCComponentFormType)session.getTypeComponent("Form");
						TCComponentForm newURL = imancomponentformtype.create(modifyName, descTextArea.getText(), "Web Link", false);
						newURL.setProperty("url", urlName);
						addAttachWebLinkList(newURL);
					} catch(Exception ex)
					{
						MessageBox.post(ex);
						return;
					}
					setVisible(false);
					dispose();
				}
			});
			cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setVisible(false);
					dispose();
				}
			});
			contentsPane.setBackground(Color.white);
		}
	}

	public DefaultListModel getListModel(){
		return listModel;
	}
	
	public JPanel getButtonPanel(){
		return buttonPanel;
	}

	public DropList getDropList() {
		return fileList;
	}
	
	public JButton getAddSaveAsButton(){
		return addSaveAsButton;
	}
}
