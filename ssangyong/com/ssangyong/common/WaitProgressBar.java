package com.ssangyong.common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;

import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Wait Progress Bar
 * 
 * ■ 설명 :
 * 프로그램 실행 중 오랜 시간이 걸리거나 상태를 보여줄 경우 사용함.
 * 이 Class는 반드시 Thread에서 사용해야 한다.-동일 Process에서 동작시 UI 멈춤 현상이 발생하여 화면이 보이지 않을 수 있다.
 * 
 * ■ 지원 기능 : 
 *   1. 진행 상황 text로 보여주기
 *   2. 창 이동 및 사이즈 조절 가능
 *   3. 에러 발생 시 테두리를 쓰리 클릭하여 강제 종료 가능
 *   4. 완료 시 사용자 종료 버튼을 통한 종료 기능
 *   5. 자동 종료 기능
 *   6. 로그 저장 기능-진행 상황을 파일로 생성하여 저장 할 수 있음
 *   7. 프로그램 진행 중 사용자 확인이 필요한 경우 "계속" 또는 "닫기" 선택 가능
 * 
 * ■ 사용법(Sample Code)
 *   Thread 또는 Operation 안에서 수행 되어야 이 상태 창이 죽지 않음.
 *   
 *   > instance 생성
 * 		WaitProgressBar waitProgressBar = new WaitProgressBar(#상위 Window(dialog 또는 frame)#);
 * 		waitProgressBar.setWindowSize(800, 500);
 * 		waitProgressBar.start();
 * 		waitProgressBar.setStatus("상태");
 *      waitProgressBar.setStatus("xxx 작성중...", false);
 *      //실행 시키고자 하는 프로그램...
 *      waitProgressBar.setStatus("ok");
 *      int r = waitProgressBar.confirm("선택", false, true);
 *		if (r != WaitProgressBar.CONTINUE)
 *		{
 *			return;
 *		}
 *      waitProgressBar.close("완료", true);
 *
 * @version 2.0
 * @author G082464
 */
public class WaitProgressBar extends JWindow implements Runnable
{
	public static int YES = 3;
	public static int NO = 4;
	/**
	 * 확인 창이 되었을 경우 계속한다는 옵션
	 */
	public static int CONTINUE = 1;
	/**
	 * 확인창이 되었을 경우 중지 또는 닫는 옵
	 */
	public static int CLOSE = 2;
	private int statusValue = 0;
	private static final long serialVersionUID = 1L;
	private JPanel contentPane;
	private JPanel topPanel = new JPanel();
	private JPanel panel = new JPanel();
	private JProgressBar progressBar = new JProgressBar();
	private BorderLayout borderLayout1 = new BorderLayout();
	private BorderLayout borderLayout2 = new BorderLayout();
	private JScrollPane statusScrollPane = new JScrollPane();
	private JTextArea statusTextArea = new JTextArea();
	private BorderLayout borderLayout3 = new BorderLayout();
	private JPanel buttonPanel = new JPanel();
	private JButton yesButton = new JButton("예");
	private JButton noButton = new JButton("아니오");
	private JButton continueButton = new JButton("계속");
	private JButton closeButton = new JButton("닫기");
	private JButton logButton = new JButton("log");
	private Border border = new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 2), "진행정보");
	private boolean isParentClose = false;
	private Window parent;
	private Thread waitThread;
	JScrollBar verticalScroll;

	private final JPanel logPanel = new JPanel();
	private final JLabel sizeLabel = new JLabel();
	protected boolean isDrag = false;
	protected Point dragStartPoint = null;
	private String logInfo = "";

	private TCSession session = CustomUtil.getTCSession();

	/**
	 * 생성자
	 * 
	 * @param _parent 반드시 상태창의 상위에 해당하는 window를 입력해야함.
	 */
	public WaitProgressBar(Window _parent)
	{
		super(_parent);
		parent = _parent;
		initGUI();
	}

	/**
	 * Runnable에서 사용되는 메소드 이므로 개발자는 사용하지 않아야 함.
	 * 쓰레드 시작시 자동 호출됨.
	 */
	public void run()
	{
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
		this.repaint();
	}

	/**
	 * 쓰레드를 시작한다.
	 * 실제 상태 창을 보여준다.
	 */
	public void start()
	{
		waitThread = new Thread(WaitProgressBar.this);
		waitThread.start();
	}

	/**
	 * UI 구성 및 리스너
	 * @throws Exception
	 */
	private void initGUI()
	{
		contentPane = (JPanel) getContentPane();
		contentPane.setLayout(borderLayout1);
		contentPane.setBackground(SystemColor.inactiveCaptionText);
		contentPane.setBorder(border);
		contentPane.add(panel, java.awt.BorderLayout.CENTER);
		contentPane.add(topPanel, java.awt.BorderLayout.SOUTH);
		topPanel.setBackground(Color.WHITE);

		progressBar.setBackground(Color.white);
		progressBar.setStringPainted(true);
		progressBar.setString("");
		progressBar.setIndeterminate(true);

		topPanel.setLayout(borderLayout3);
		panel.setLayout(borderLayout2);
		panel.add(statusScrollPane, java.awt.BorderLayout.CENTER);
		statusScrollPane.getViewport().add(statusTextArea);
		verticalScroll = statusScrollPane.getVerticalScrollBar();
		// statusTextArea.setRequestFocusEnabled(false);
		statusTextArea.setEditable(false);

		buttonPanel.setBackground(Color.white);
		buttonPanel.setVisible(false);
		yesButton.setVisible(false);
		noButton.setVisible(false);
		continueButton.setVisible(false);
		buttonPanel.add(yesButton);
		buttonPanel.add(noButton);
		buttonPanel.add(continueButton);
		buttonPanel.add(closeButton);

		topPanel.add(progressBar, BorderLayout.NORTH);
		topPanel.add(buttonPanel);

		contentPane.setPreferredSize(new Dimension(300, 200));

		contentPane.addMouseMotionListener(new MouseMotionAdapter()
		{
			//창을 찍고 이동 할 수 있음.
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (!isDrag)
				{
					isDrag = true;
					dragStartPoint = e.getPoint();
				} else
				{
					Point newMousePoint = e.getPoint();
					Point location = getLocation();
					setLocation(location.x + (newMousePoint.x - dragStartPoint.x), location.y + (newMousePoint.y - dragStartPoint.y));
				}
			}
		});
		contentPane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseClicked(MouseEvent e)
			{
				//쓰리 클릭일 경우 닫기 버튼을 보여주어 닫을 수 있도록 함.
				if (e.getClickCount() == 3)
				{
					setShowButton(!buttonPanel.isVisible());
				}
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				isDrag = false;
			}
		});
		//확인 상태에서 "계속" 버튼을 클릭하면 동작함.
		continueButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setShowButton(false);
				statusValue = CONTINUE;
				progressBar.setString("");
				progressBar.setIndeterminate(true);
			}
		});
		yesButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setShowButton(false);
				statusValue = YES;
				progressBar.setString("");
				progressBar.setIndeterminate(true);
			}
		});
		noButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setShowButton(false);
				statusValue = NO;
				progressBar.setString("");
				progressBar.setIndeterminate(true);
			}
		});
		//종료.
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//부모창을 함께 닫을 것인지 여부에 따라 결정
				statusValue = CLOSE;
				if (isParentClose)
				{
					parent.setVisible(false);
					parent.dispose();
				} else
				{
					setVisible(false);
					dispose();
				}
			}
		});
		topPanel.add(logPanel, BorderLayout.WEST);
		logPanel.setBackground(Color.WHITE);
		logPanel.setVisible(false);
		logPanel.add(logButton);
		logButton.setMargin(new Insets(2, 4, 2, 4));
		logButton.setToolTipText("진행 정보 로그를 저장합니다.");
		logButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				logButton_actionPerformed(e);
			}
		});
		topPanel.add(sizeLabel, BorderLayout.EAST);
		sizeLabel.setRequestFocusEnabled(false);
		sizeLabel.setToolTipText("크기 조절");
		sizeLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		sizeLabel.addMouseMotionListener(new MouseMotionAdapter()
		{
			//창 사이즈를 조절함.
			@Override
			public void mouseDragged(MouseEvent e)
			{
				if (!isDrag)
				{
					isDrag = true;
					dragStartPoint = e.getPoint();
				} else
				{
					Point newMousePoint = e.getPoint();
					int x = newMousePoint.x - dragStartPoint.x;
					int y = newMousePoint.y - dragStartPoint.y;
					int w = getWidth();
					int h = getHeight();
					int width = w + x;
					int height = h + y;
					if (width < 100)
					{
						width = w;
					}
					if (height < 100)
					{
						height = h;
					}
					setSize(new Dimension(width, height));
					validate();
					repaint();
				}
			}
		});
		sizeLabel.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				sizeLabel.setForeground(Color.RED);
			}

			@Override
			public void mouseReleased(MouseEvent e)
			{
				isDrag = false;
				sizeLabel.setForeground(Color.BLACK);
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				setCursor(new Cursor(Cursor.SE_RESIZE_CURSOR));
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
			}
		});
		sizeLabel.setText("   ");
//		sizeLabel.setIcon(HKMCMppBopUtility.getTCImageIcon("resize.png"));
		addWindowListener(new WindowAdapter()
		{
			//상태 창 실행 시 부모 창을 선택하지 못하도록 함.
			@Override
			public void windowOpened(WindowEvent e)
			{
				getParent().setEnabled(false);
			}

			//상태 창이 닫힐 때 부모창을 다시 선택 가능하도록 함.
			@Override
			public void windowClosed(WindowEvent e)
			{
				getParent().setEnabled(true);
				if (session != null)
				{
					session.setReadyStatus();
				}
			}
		});
	}

	/**
	 * 프로그래스 바에 글을 입력
	 * @param progressString
	 */
	public void setProgressString(String progressString)
	{
		progressBar.setString(progressString);
	}

	/**
	 * 프로그래스 바의 값을 입력하여 0-100%를 표현하는 메서드
	 * @param value 0에서 100까지 입력
	 */
	public void setProgressValue(int value)
	{
		progressBar.setIndeterminate(false);
		progressBar.setValue(value);
		progressBar.repaint();
	}

	/**
	 * button panel에 사용자 지정 component를 추가한다.
	 * @param component
	 */
	public void addUserButtonToButtonPanel(JButton button, final int _statusValue)
	{
		buttonPanel.add(button);
		button.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				setShowButton(false);
				statusValue = _statusValue;
				progressBar.setString("");
				progressBar.setIndeterminate(true);
			}
		});
	}

	/**
	 * 현재의 상태를 입력할 수 있는 method이다.<br>
	 * textarea로 되어 있으며 입력된 글은 자동 스크롤 되어진다.
	 * @param status
	 *        String 상태를 나타내는 글.
	 */
	public void setStatus(String status)
	{
		setStatus(status, true);
	}

	/**
	 * 현재의 상태를 입력할 수 있는 method이다.<br>
	 * nextLine이 true이면 상태글 마지막에 New Line Character를 추가 nextLine이 flase이면 상태글 마지막에 New Line Character를 추가하지 않음.
	 * @param status
	 *        String 상태를 나타내는 글.
	 * @param nextLine
	 *        boolean 상태 글을 추가 시 마지막 라인에 New Line 추가 후 입력 여부 결정
	 */
	public void setStatus(String status, boolean nextLine)
	{
		while (waitThread != null && waitThread.isAlive())
		{
		}
		String statusString = status + (nextLine ? "\r\n" : "");
		try
		{
			//상태 창에 글이 많으면 죽는 경우가 발생함.
			//50kb 이상이 되면 첫번째 줄부터 삭제 시작...
			if (statusTextArea.getText().getBytes().length > 50000)
			{
				statusTextArea.getDocument().remove(0, statusString.length());
			}
		} catch (BadLocationException e)
		{
			e.printStackTrace();
		}
		if (session != null)
		{
			session.setStatus(status);
		}
		statusTextArea.append(statusString);
		logInfo += statusString;
		statusTextArea.repaint();
		statusTextArea.validate();
		repaint();
		scrollDown(true);
	}

	/**
	 * 이 close method는 자동 닫힘 method이다.<br>
	 */
	public void close()
	{
		while (waitThread.isAlive())
		{
		}
		setVisible(false);
		dispose();
	}

	/**
	 * 특정 시간동안 지연 후 닫기도록 하는 method이다.
	 * @param delayTime
	 *        int 단위는 초이다. 5초후에 닫기 위해서는 5를 입력
	 */
	public void close(int delayTime)
	{
		scrollDown(true);
		progressBar.setIndeterminate(false);
		// delayTime초간 지연후 닫김...
		try
		{
			Thread.sleep(delayTime * 1000);
		} catch (InterruptedException ex)
		{
		}
		close();
	}

	/**
	 * 자동으로 닫히지 않고 사용자가 닫을 수 있도록 닫기 버튼을 보여주거나 사라지게 하는 옵션을 추가하였다.<br>
	 * 그리고 프로그래스바는 더이상 움직이지 않게 되며, 그 자리에 value 값이 적히게 된다.<br>
	 * 닫기 버튼은 자기 자신만 닫을 수도 있지만 자신의 부모를 닫을 수도 있다.<br>
	 * 부모를 닫는 옵션의 사용시 생성자에서 받는 부모가 무엇인지 정확히 판단하여야 한다.
	 * @param show
	 *        boolean 닫기 버튼의 보이기 유무.
	 * @param value
	 *        String 프로그래스바에 나타나는 글씨
	 * @param _isAlertMode
	 *        boolean 주의(알림) 강조표시 필요 유무.
	 * @param _isParentClose
	 *        boolean 부모 Window의 close 유무.
	 */
	public void close(String value, boolean _isAlertMode, boolean _isParentClose)
	{
		scrollDown(true);
		logButton.setVisible(true);
		setShowButton(true);
		progressBar.setString(value);
		progressBar.setIndeterminate(false);
		isParentClose = _isParentClose;
		closeButton.requestFocus();
		
		//20140418, 검증시 결과 표시할 때 하단 상태표시 텍스트 시인성 있게 개선 (BOP 요청사항이었음)
        if(_isAlertMode) {
            progressBar.setForeground(Color.RED);
            progressBar.setValue(100);
        }
	}
	
	public void close(String value, boolean _isParentClose)
    {
        close(value, false, _isParentClose);
    }

	/**
	 * 프로세스 진행 중 진행 유무를 확인 할 수 있다.
	 * @param value
	 *        String 프로그래스바에 나타나는 글씨
	 * @param _isParentClose
	 *        boolean 부모 Window의 close 유무.
	 * @param isLogButtonShow
	 *        boolean log 버튼을 보여줄지 유무.
	 * @return
	 */
	public int confirm(String value, boolean _isParentClose, boolean isLogButtonShow)
	{
		toFront();
		statusValue = 0;
		close(value, _isParentClose);
		continueButton.setVisible(true);
		logButton.setVisible(isLogButtonShow);
		while (statusValue == 0)
		{
			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		continueButton.setVisible(false);
		return statusValue;
	}

	/**
	 * 프로세스 진행 중 예, 아니오를 선택하여 값을 받을 수 있다.
	 * @param value
	 *        String 프로그래스바에 나타나는 글씨
	 * @return
	 */
	public int confirm(String value)
	{
		toFront();
		statusValue = 0;
		close(value, false);
		closeButton.setVisible(false);
		yesButton.setVisible(true);
		noButton.setVisible(true);
		while (statusValue == 0)
		{
			try
			{
				Thread.sleep(500);
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}
		closeButton.setVisible(true);
		yesButton.setVisible(false);
		noButton.setVisible(false);
		return statusValue;
	}

	/**
	 * 버튼 panel을 보여주는 부분.
	 * @param showCloseButton
	 */
	public void setShowButton(boolean showCloseButton)
	{
		toFront();
		buttonPanel.setVisible(showCloseButton);
		logPanel.setVisible(showCloseButton);
		int size = 0;
		if (showCloseButton)
		{
			size = +27;
		} else
		{
			size = -27;
		}
		setSize((int) getSize().getWidth(), (int) getSize().getHeight() + size);
		validate();
		repaint();
	}

	/**
	 * 현재 Window의 사이즈를 정한다.
	 * @param width
	 * @param height
	 */
	public void setWindowSize(int width, int height)
	{
		setPreferredSize(new Dimension(width, height));
		validate();
		repaint();
	}

	/**
	 * 로그 버튼을 눌렀을 경우 로컬에 저장하는 부분.
	 * @param e
	 */
	private void logButton_actionPerformed(ActionEvent e)
	{
		JFileChooser jfc = new JFileChooser();
		int r = jfc.showSaveDialog(this);
		if (r != JFileChooser.APPROVE_OPTION)
		{
			return;
		}
		File logFile = jfc.getSelectedFile();
		if (logFile == null)
		{
			return;
		}
		try
		{
			FileWriter fw = new FileWriter(logFile);
			fw.write(logInfo);
			fw.close();
		} catch (IOException ex)
		{
			JOptionPane.showMessageDialog(this, ex);
		}
	}

	/**
	 * 자동 scroll을 위한 부분.
	 * @param isDelay
	 */
	private void scrollDown(final boolean isDelay)
	{
		new Thread(new Runnable()
		{
			public void run()
			{
				if (isDelay)
				{
					try
					{
						Thread.sleep(10);
						statusScrollPane.validate();
					} catch (InterruptedException ex)
					{
					}
				}
				verticalScroll.setValue(verticalScroll.getMaximum());
				verticalScroll.validate();
			}
		}).start();
	}
}
