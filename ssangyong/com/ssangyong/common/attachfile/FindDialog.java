package com.ssangyong.common.attachfile;

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

import com.ssangyong.common.CustomTCTable;
import com.ssangyong.common.utils.CustomUtil;
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
 * Teamcenter의 기본 검색 조건 화면을 이용하여(즉, saved query를 사용함) 그 결과를 보여줄 수 있도록 한다.<br>
 * column의 지정이 가능하며 결과 검색시 호출한 parent의 method를 호출할 수 있도록 할 수 있다.<br><br>
 *
 * ## 사용법.<br>
 * <br>
 * #query도 별도로 지정하지 않고, 결과도 별도로 지정하지 않은 경우.<br>
 * FindDialog findDialog = new FindDialog(Utilities.getParentJFrame(this), "검색", true, "", null, null, null, null, true);<br>
 * 현재 class에 selectAction이라는 method를 만들어놓고 검색창에서 선택할 경우 실행 하도록 할때 사용.<br>
 * 단, method의 parameter 값은 single selection일 경우 AIFComponentContext <br>
 *     multi selection일 경우 AIFComponentContext[] 로 해야 한다.<br>
 * findDialog.setRegistActionMethod(this, "selectAction");<br>
 * findDialog.run();<br>
 * <br>
 * method 호출 형태를 사용하지 않을 경우에는 <br>
 * findDialog.run();<br>
 * run을 시킨 후 <br>
 * if(findDialog.getAction() == findDialog.ACTION_SELECT)<br>
 * {<br>
 *   AIFComponentContext[] resultAIFContext = findDialog.getSelectedAIFComponentContexts();<br>
 * }<br>
 * 위의 code를 이용하여 결과를 가져올 수 있다.<br>
 * <br><br>
 * #특정 query를 사용하고 초기 value를 사용 하고자 할 경우.<br>
 * (Frame)parent <-- 프레임 객체를 parent로 한다.<br>
 * "Search Template Dataset" <-- 타이틀 입력.<br>
 * true <-- 창을 modal로 띄운다. run을 시킨 후 값을 가져오기 위해서는 반드시 modal로 띄워야 한다.<br>
 * templateSavedQuery = "Dataset...";<br>
 * template_AttNameArray = {"Dataset Type", "Name"}; <-- 반드시 display name을 적어야 한다. display name이 없는 경우 사용하지 못함. 여러 값을 입력하고자 할 경우 배열로 입력한다.<br>
 * template_AttValueArray = {"text|MSWord|MSExcel", "*견적*"}; <-- 값을 or 검색 하고 싶을 경우 구분자를 사용하고 포함된 text를 찾을때는 *를 사용한다.<br>
 * null <-- 검색 결과의 attribute 배열을 지정. 예) {"object_name","object_desc"}<br>
 * null <-- 검색 결과의 attribute 의 display name을 지정 예){"Name","Description"} <-- 반드시 위의 attribute와 array 개수가 같아야 함.<br>
 * false <-- 검색 가능 리스트를 보여줄 것인지를 결정함.<br>
 * findDialog = new FindDialog((Frame)parent, "Search Template Dataset", true, templateSavedQuery, template_AttNameArray, template_AttValueArray, null, null, false);<br>
 * <br><b>무단 수정 금지 ^^;;<b><br>
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
	 * 생성자
	 * @param frame Frame
	 * @param title String                  검색 창의 Title
	 * @param isModal boolean               modal 여부
	 */
	public FindDialog(Frame frame, String title, boolean isModal)
	{
		super(frame, title, isModal);
		init();
	}

	/**
	 * 생성자
	 * @param dialog Dialog
	 * @param title String                  검색 창의 Title
	 * @param isModal boolean               modal 여부
	 */
	public FindDialog(Dialog dialog, String title, boolean isModal)
	{
		super(dialog, title, isModal);
		init();
	}

	/**
	 * @deprecated UI 전용 생성자. 절대 사용 금지
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
				//						waitProgressBar.setStatus("검색 시작...");
				//						iMANTable.removeAllRows();
				//						waitProgressBar.setStatus("이전 검색 결과 삭제...");
				//						if(searchPanel.getSearchCriteriaPanel() == null)
				//						{
				//							waitProgressBar.setStatus("\n경고!!! 검색 조건이 없습니다..");
				//							waitProgressBar.close("검색 실패", false);
				//							return;
				//						}
				//						waitProgressBar.setStatus("검색 중... ", false);
				//						TCComponentContextList resultList = searchPanel.getSearchCriteriaPanel().executeSearch();
				//						waitProgressBar.setStatus("완료");
				//						if(resultList == null)
				//						{
				//							waitProgressBar.setStatus("검색 에러 발생...");
				//							waitProgressBar.close("검색 실패", false);
				//							return;
				//						}
				//						waitProgressBar.setStatus("검색 결과... ", false);
				//				count = resultList.getListCount();
				//						waitProgressBar.setStatus("총 " + count + "개 검색 됨.");
				//						countLabel.setText(registry.getString("findDialogResultCount.NAME") + count);
				//						waitProgressBar.setStatus("검색 결과 Display 중... ", false);
				//						AIFComponentContext[] resultComp = resultList.toArray();
				//						iMANTable.addRows(resultComp);
				//						iMANTable.updateUI();
				//						iMANTable.validate();
				//						iMANTable.repaint();
				//						waitProgressBar.setStatus("완료");
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
		
		/** 검색 결과 테이블 컬럼 사이즈 지정 */
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
	 * 검색 결과 Table의 column을 임의로 지정할 수 있도록 함.
	 *
	 * @param ids String[] 속성 이름(schema에 정의된 이름 사용-대소문자 구별)
	 * @param names String[] display 이름(user에게 보여질 name을 지정함)
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
	 * Query 조건에서 query 리스트를 보여줄 것인지를 결정함.
	 * @param flag boolean
	 */
	public void setVisibleQuerySelectionButton(boolean flag)
	{
		searchPanel.getQuerySelectionButton().setVisible(flag);
	}

	/**
	 * 선택시 하나만 선택할지 멀티로 선택할지를 결정한다. 기본은 single
	 * @param mode int
	 */
	public void setSelectionMode(int mode)
	{
		selection_mode = mode;
		iMANTable.setSelectionMode(mode);
	}

	/**
	 * select button의 이름을 변경할 수 있음.
	 * @param text String
	 */
	public void setSelectButtonText(String text)
	{
		selectButton.setText(text);
	}

	/**
	 * close button의 이름을 변경할 수 있음.
	 * @param text String
	 */
	public void setCloseButtonText(String text)
	{
		closeButton.setText(text);
	}

	/**
	 * 검색 후 어떤 button을 눌러 action이 일어났는지를 돌려줌.
	 * @return int
	 */
	public int getAction()
	{
		return action;
	}

	/**
	 * 검색 결과 리스트를 돌려줌. array
	 * @return AIFComponentContext[]
	 */
	public AIFComponentContext[] getSelectedAIFComponentContexts()
	{
		return resultArray;
	}

	/**
	 * 검색 결과 리스트를 돌려줌.
	 * @return AIFComponentContext
	 */
	public AIFComponentContext getSelectedAIFComponentContext()
	{
		return resultArray[0];
	}

	/**
	 * 검색창을 생성하고 method를 등록하면 최종 select button을 눌렀을때 등록한 method를 실행하도록 되어있음.
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
	 * 검색 결과 테이블 컬럼 사이즈 변경.
	 * @Copyright : S-PALM
	 * @author : 권상기
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
