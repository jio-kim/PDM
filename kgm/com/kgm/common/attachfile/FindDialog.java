package com.kgm.common.attachfile;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import com.kgm.common.CustomTCTable;
import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.explorer.common.SearchCriteriaPanel;
import com.teamcenter.rac.explorer.common.SearchPanel;
import com.teamcenter.rac.kernel.TCComponentContextList;
import com.teamcenter.rac.kernel.TCComponentQuery;
import com.teamcenter.rac.kernel.TCComponentQueryType;
import com.teamcenter.rac.kernel.TCException;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.SplitPane;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;

/**
 * Teamcenter�� �⺻ �˻� ���� ȭ���� �̿��Ͽ�(��, saved query�� �����) �� ����� ������ �� �ֵ��� �Ѵ�.<br>
 * column�� ������ �����ϸ� ��� �˻��� ȣ���� parent�� method�� ȣ���� �� �ֵ��� �� �� �ִ�.<br><br>
 *
 * ## ����.<br>
 * <br>
 * #query�� ������ �������� �ʰ�, ����� ������ �������� ���� ���.<br>
 * FindDialog findDialog = new FindDialog(Utilities.getParentJFrame(this), "�˻�", true, "", null, null, null, null, true);<br>
 * ���� class�� selectAction�̶�� method�� �������� �˻�â���� ������ ��� ���� �ϵ��� �Ҷ� ���.<br>
 * ��, method�� parameter ���� single selection�� ��� AIFComponentContext <br>
 *     multi selection�� ��� AIFComponentContext[] �� �ؾ� �Ѵ�.<br>
 * findDialog.setRegistActionMethod(this, "selectAction");<br>
 * findDialog.run();<br>
 * <br>
 * method ȣ�� ���¸� ������� ���� ��쿡�� <br>
 * findDialog.run();<br>
 * run�� ��Ų �� <br>
 * if(findDialog.getAction() == findDialog.ACTION_SELECT)<br>
 * {<br>
 *   AIFComponentContext[] resultAIFContext = findDialog.getSelectedAIFComponentContexts();<br>
 * }<br>
 * ���� code�� �̿��Ͽ� ����� ������ �� �ִ�.<br>
 * <br><br>
 * #Ư�� query�� ����ϰ� �ʱ� value�� ��� �ϰ��� �� ���.<br>
 * (Frame)parent <-- ������ ��ü�� parent�� �Ѵ�.<br>
 * "Search Template Dataset" <-- Ÿ��Ʋ �Է�.<br>
 * true <-- â�� modal�� ����. run�� ��Ų �� ���� �������� ���ؼ��� �ݵ�� modal�� ����� �Ѵ�.<br>
 * templateSavedQuery = "Dataset...";<br>
 * template_AttNameArray = {"Dataset Type", "Name"}; <-- �ݵ�� display name�� ����� �Ѵ�. display name�� ���� ��� ������� ����. ���� ���� �Է��ϰ��� �� ��� �迭�� �Է��Ѵ�.<br>
 * template_AttValueArray = {"text|MSWord|MSExcel", "*����*"}; <-- ���� or �˻� �ϰ� ���� ��� �����ڸ� ����ϰ� ���Ե� text�� ã������ *�� ����Ѵ�.<br>
 * null <-- �˻� ����� attribute �迭�� ����. ��) {"object_name","object_desc"}<br>
 * null <-- �˻� ����� attribute �� display name�� ���� ��){"Name","Description"} <-- �ݵ�� ���� attribute�� array ������ ���ƾ� ��.<br>
 * false <-- �˻� ���� ����Ʈ�� ������ �������� ������.<br>
 * findDialog = new FindDialog((Frame)parent, "Search Template Dataset", true, templateSavedQuery, template_AttNameArray, template_AttValueArray, null, null, false);<br>
 * <br><b>���� ���� ���� ^^;;<b><br>
 * @author park seho
 * @version 1.0
 */
public class FindDialog extends AbstractAIFDialog {

	private static final long serialVersionUID = 1L;

	private Registry registry = Registry.getRegistry(this);
	private TCSession session = CustomUtil.getTCSession();
	public static int ACTION_CLOSE = 0;
	public static int ACTION_SELECT = 1;
	private int action = 0;
	public static int SELECTION_SINGLE = 0;
	public static int SELECTION_MULTI = 2;
	private int selection_mode = 0;
	private boolean isRegistActionMethod = false;
	private Object parentObject = null;
	private String methodName = "";
	private JPanel contentsPane;
	private VerticalLayout verticalLayout = new VerticalLayout();
	private SplitPane mainSplitPane = new SplitPane();
	private Separator separator = new Separator();
	private JPanel buttonPanel = new JPanel();
	private JButton selectButton = new JButton();
	private JButton closeButton = new JButton();
	private SearchPanel searchPanel = new SearchPanel(session, Utilities.getParentJFrame(this), false, true, false);
	private JScrollPane jScrollPane1 = new JScrollPane();
	private CustomTCTable iMANTable = new CustomTCTable();
	private int count = 0;
	private JPanel resultPanel = new JPanel();
	private BorderLayout borderLayout1 = new BorderLayout();
	private JLabel countLabel = new JLabel();
	private AIFComponentContext[] resultArray = null;
	private TCComponentQuery queryComponent = null;

	private void init()
	{
		try
		{
			jbInit();
		} catch(Exception ex)
		{
			MessageBox.post(this, ex);
			ex.printStackTrace();
		}
	}

	/**
	 * ������
	 * @param frame Frame
	 * @param title String                  �˻� â�� Title
	 * @param isModal boolean               modal ����
	 */
	public FindDialog(Frame frame, String title, boolean isModal)
	{
		super(frame, title, isModal);
		init();
	}

	/**
	 * ������
	 * @param dialog Dialog
	 * @param title String                  �˻� â�� Title
	 * @param isModal boolean               modal ����
	 */
	public FindDialog(Dialog dialog, String title, boolean isModal)
	{
		super(dialog, title, isModal);
		init();
	}

	/**
	 * @deprecated UI ���� ������. ���� ��� ����
	 * @param flag boolean
	 */
	@SuppressWarnings("unused")
	private FindDialog(boolean flag)
	{
		super(flag);
		init();
	}

	private void jbInit() throws Exception
	{
		this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
		getContentPane().setBackground(Color.white);
		searchPanel.getQuerySelectionButton().setVisible(false);
		searchPanel.setPreferredSize(new Dimension(300, 200));
		contentsPane = (JPanel)this.getContentPane();
		contentsPane.setLayout(verticalLayout);
		resultPanel.setLayout(borderLayout1);
		buttonPanel.setOpaque(false);
		
		contentsPane.add("unbound.bind", mainSplitPane);
		contentsPane.add("bottom.bind", buttonPanel);
		contentsPane.add("bottom.bind", separator);

		buttonPanel.add(selectButton);
		buttonPanel.add(closeButton);
		mainSplitPane.setLeftComponent(searchPanel);
		mainSplitPane.setRightComponent(resultPanel);
		jScrollPane1.getViewport().add(iMANTable);
		resultPanel.add(countLabel, java.awt.BorderLayout.NORTH);
		resultPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

		selectButton.setText(registry.getString("findDialogSelectButton.NAME"));
		selectButton.setIcon(registry.getImageIcon("OK_16.ICON"));
		closeButton.setText(registry.getString("findDialogCloseButton.NAME"));
		closeButton.setIcon(registry.getImageIcon("Cancel_16.ICON"));
		countLabel.setText(registry.getString("findDialogResultCount.NAME") + count);

		final JButton executionButton = searchPanel.getExecutionButton();
		ActionListener[] actLis = executionButton.getActionListeners();
		for(int i = 0; i < actLis.length; i++)
		{
			executionButton.removeActionListener(actLis[i]);
		}
		JButton refreshButton = searchPanel.getRefreshButton();
		ActionListener[] refreshAction = refreshButton.getActionListeners();
		for(int i = 0; i < refreshAction.length; i++)
		{
			refreshButton.removeActionListener(refreshAction[i]);
		}
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				this_windowClosing(e);
			}
		});
		executionButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//				new Thread(new Runnable()
				//				{
				//					public void run()
				//					{
				//						WaitProgressBar waitProgressBar = new WaitProgressBar(FindDialog.this);
				//						waitProgressBar.start();
				//						waitProgressBar.setStatus("�˻� ����...");
				//						iMANTable.removeAllRows();
				//						waitProgressBar.setStatus("���� �˻� ��� ����...");
				//						if(searchPanel.getSearchCriteriaPanel() == null)
				//						{
				//							waitProgressBar.setStatus("\n���!!! �˻� ������ �����ϴ�..");
				//							waitProgressBar.close("�˻� ����", false);
				//							return;
				//						}
				//						waitProgressBar.setStatus("�˻� ��... ", false);
				//						TCComponentContextList resultList = searchPanel.getSearchCriteriaPanel().executeSearch();
				//						waitProgressBar.setStatus("�Ϸ�");
				//						if(resultList == null)
				//						{
				//							waitProgressBar.setStatus("�˻� ���� �߻�...");
				//							waitProgressBar.close("�˻� ����", false);
				//							return;
				//						}
				//						waitProgressBar.setStatus("�˻� ���... ", false);
				//				count = resultList.getListCount();
				//						waitProgressBar.setStatus("�� " + count + "�� �˻� ��.");
				//						countLabel.setText(registry.getString("findDialogResultCount.NAME") + count);
				//						waitProgressBar.setStatus("�˻� ��� Display ��... ", false);
				//						AIFComponentContext[] resultComp = resultList.toArray();
				//						iMANTable.addRows(resultComp);
				//						iMANTable.updateUI();
				//						iMANTable.validate();
				//						iMANTable.repaint();
				//						waitProgressBar.setStatus("�Ϸ�");
				//						waitProgressBar.close();
				//					}
				//				}).start();
				TCComponentContextList resultList = searchPanel.getSearchCriteriaPanel().executeSearch();
				countLabel.setText(registry.getString("findDialogResultCount.NAME") + resultList.getListCount());
				AIFComponentContext[] resultComp = resultList.toArray();
				iMANTable.addRows(resultComp);
				iMANTable.updateUI();
			}
		});
		refreshButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				executionButton.doClick();
			}
		});
		selectButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				selectButton_actionPerformed(e);
			}
		});
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				closeButton_actionPerformed(e);
			}
		});
		iMANTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() == 2){
					selectButton_actionPerformed(null);
				}
				
			}
		});
		
		/** �˻� ��� ���̺� �÷� ������ ���� */
		tableColumnSize();
	}

	public void setQuerySetting(String queryName, String[] query_attName, String[] query_attValue)
	{
		try
		{
			if(queryName != null && !queryName.trim().equals(""))
			{
				TCComponentQueryType queryType = (TCComponentQueryType)session.getTypeComponent("ImanQuery");
				queryComponent = (TCComponentQuery)queryType.find(queryName);
				//session.getPreferenceService().setString(1, "QRY_dataset_display_option", "2");
				session.getPreferenceService().setStringValue("QRY_dataset_display_option", "2");
			}
		} catch(TCException ex)
		{
			ex.printStackTrace();
		}

		if(queryComponent != null)
		{
			final SearchCriteriaPanel searchCriteriaPanel = new SearchCriteriaPanel(queryComponent, query_attName, query_attValue);
			searchPanel.setCriteriaPanel(searchCriteriaPanel);
			ActionListener actionlistener = new ActionListener()
			{
				public void actionPerformed(ActionEvent actionevent)
				{
					if(searchPanel.getExecutionButton() != null)
					{
						searchPanel.getExecutionButton().doClick();
					}
				}
			};
			searchCriteriaPanel.setButtonListener(actionlistener);
		}
	}

	/**
	 * �˻� ��� Table�� column�� ���Ƿ� ������ �� �ֵ��� ��.
	 *
	 * @param ids String[] �Ӽ� �̸�(schema�� ���ǵ� �̸� ���-��ҹ��� ����)
	 * @param names String[] display �̸�(user���� ������ name�� ������)
	 * @param size String[]
	 * @param queryName String
	 */
	public void setColumnSetting(String ids[], String names[], String[] size, String queryName)
	{
		if(ids != null && names == null)
		{
			iMANTable = new CustomTCTable(session, ids);
		} else if(ids != null && names != null)
		{
			iMANTable = new CustomTCTable(ids, names);
		} else
		{
			iMANTable = new CustomTCTable(session, new String[]{"object_string", "owning_user", "owning_group"});
		}
		if(size != null && size.length > 0)
		{
			iMANTable.setColumnWidths(size);
		}
		jScrollPane1.getViewport().add(iMANTable);
		iMANTable.setEditable(false);
		iMANTable.setAutoResizeMode(JTable.AUTO_RESIZE_SUBSEQUENT_COLUMNS);
		iMANTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
	}

	/**
	 * Query ���ǿ��� query ����Ʈ�� ������ �������� ������.
	 * @param flag boolean
	 */
	public void setVisibleQuerySelectionButton(boolean flag)
	{
		searchPanel.getQuerySelectionButton().setVisible(flag);
	}

	/**
	 * ���ý� �ϳ��� �������� ��Ƽ�� ���������� �����Ѵ�. �⺻�� single
	 * @param mode int
	 */
	public void setSelectionMode(int mode)
	{
		selection_mode = mode;
		iMANTable.setSelectionMode(mode);
	}

	/**
	 * select button�� �̸��� ������ �� ����.
	 * @param text String
	 */
	public void setSelectButtonText(String text)
	{
		selectButton.setText(text);
	}

	/**
	 * close button�� �̸��� ������ �� ����.
	 * @param text String
	 */
	public void setCloseButtonText(String text)
	{
		closeButton.setText(text);
	}

	/**
	 * �˻� �� � button�� ���� action�� �Ͼ������ ������.
	 * @return int
	 */
	public int getAction()
	{
		return action;
	}

	/**
	 * �˻� ��� ����Ʈ�� ������. array
	 * @return AIFComponentContext[]
	 */
	public AIFComponentContext[] getSelectedAIFComponentContexts()
	{
		return resultArray;
	}

	/**
	 * �˻� ��� ����Ʈ�� ������.
	 * @return AIFComponentContext
	 */
	public AIFComponentContext getSelectedAIFComponentContext()
	{
		return resultArray[0];
	}

	/**
	 * �˻�â�� �����ϰ� method�� ����ϸ� ���� select button�� �������� ����� method�� �����ϵ��� �Ǿ�����.
	 * @param obj Object
	 * @param _methodName String
	 */
	public void setRegistActionMethod(Object obj, String _methodName)
	{
		parentObject = obj;
		methodName = _methodName;
		isRegistActionMethod = true;
	}

	private void closeButton_actionPerformed(ActionEvent e)
	{
		action = ACTION_CLOSE;
		setVisible(false);
		dispose();
	}

	private void selectButton_actionPerformed(ActionEvent e)
	{
		AIFComponentContext[] resultAIFContext = iMANTable.getSelectedContextObjects();
		if(resultAIFContext == null || resultAIFContext.length < 1)
		{
			MessageBox.post(this, registry.getString("findDialogNoSelect.MESSAGE"), "Warning", MessageBox.WARNING);
			return;
		}
		if(isRegistActionMethod)
		{
			try
			{
				if(selection_mode == SELECTION_SINGLE)
				{
					Utilities.invokeMethod(parentObject, methodName, new Object[]
					                                                            {resultAIFContext[0]});
				} else
				{
					Utilities.invokeMethod(parentObject, methodName, new Object[]
					                                                            {resultAIFContext});
				}
			} catch(Exception ex)
			{
				MessageBox.post(this, ex);
				ex.printStackTrace();
				return;
			}
		}
		action = ACTION_SELECT;
		resultArray = resultAIFContext;
		setVisible(false);
		dispose();
	}

	private void this_windowClosing(WindowEvent e)
	{
		closeButton.doClick();
	}
	
	/**
	 * �˻� ��� ���̺� �÷� ������ ����.
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since  : 2012. 7. 11.
	 */
	private void tableColumnSize() {
		int colCount = iMANTable.getColumnCount();
		
		if(colCount == 0){
			colCount = 1;
		}
		String[] colSizeArr = new String[colCount];
		for(int i=0; i<colCount; i++){
			if(i == 0){
				colSizeArr[i] = "100";
			}else{
				colSizeArr[i] = "20";
			}
		}
		iMANTable.setColumnWidths(colSizeArr);
	}
	
	public SearchPanel getSearchPanel(){
		return searchPanel;
	}
}
