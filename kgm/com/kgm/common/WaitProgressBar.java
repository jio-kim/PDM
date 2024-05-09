package com.kgm.common;

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

import com.kgm.common.utils.CustomUtil;
import com.teamcenter.rac.kernel.TCSession;

/**
 * Wait Progress Bar
 * 
 * �� ���� :
 * ���α׷� ���� �� ���� �ð��� �ɸ��ų� ���¸� ������ ��� �����.
 * �� Class�� �ݵ�� Thread���� ����ؾ� �Ѵ�.-���� Process���� ���۽� UI ���� ������ �߻��Ͽ� ȭ���� ������ ���� �� �ִ�.
 * 
 * �� ���� ��� : 
 *   1. ���� ��Ȳ text�� �����ֱ�
 *   2. â �̵� �� ������ ���� ����
 *   3. ���� �߻� �� �׵θ��� ���� Ŭ���Ͽ� ���� ���� ����
 *   4. �Ϸ� �� ����� ���� ��ư�� ���� ���� ���
 *   5. �ڵ� ���� ���
 *   6. �α� ���� ���-���� ��Ȳ�� ���Ϸ� �����Ͽ� ���� �� �� ����
 *   7. ���α׷� ���� �� ����� Ȯ���� �ʿ��� ��� "���" �Ǵ� "�ݱ�" ���� ����
 * 
 * �� ����(Sample Code)
 *   Thread �Ǵ� Operation �ȿ��� ���� �Ǿ�� �� ���� â�� ���� ����.
 *   
 *   > instance ����
 * 		WaitProgressBar waitProgressBar = new WaitProgressBar(#���� Window(dialog �Ǵ� frame)#);
 * 		waitProgressBar.setWindowSize(800, 500);
 * 		waitProgressBar.start();
 * 		waitProgressBar.setStatus("����");
 *      waitProgressBar.setStatus("xxx �ۼ���...", false);
 *      //���� ��Ű���� �ϴ� ���α׷�...
 *      waitProgressBar.setStatus("ok");
 *      int r = waitProgressBar.confirm("����", false, true);
 *		if (r != WaitProgressBar.CONTINUE)
 *		{
 *			return;
 *		}
 *      waitProgressBar.close("�Ϸ�", true);
 *
 * @version 2.0
 * @author G082464
 */
public class WaitProgressBar extends JWindow implements Runnable
{
	public static int YES = 3;
	public static int NO = 4;
	/**
	 * Ȯ�� â�� �Ǿ��� ��� ����Ѵٴ� �ɼ�
	 */
	public static int CONTINUE = 1;
	/**
	 * Ȯ��â�� �Ǿ��� ��� ���� �Ǵ� �ݴ� ��
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
	private JButton yesButton = new JButton("��");
	private JButton noButton = new JButton("�ƴϿ�");
	private JButton continueButton = new JButton("���");
	private JButton closeButton = new JButton("�ݱ�");
	private JButton logButton = new JButton("log");
	private Border border = new TitledBorder(BorderFactory.createLineBorder(SystemColor.controlText, 2), "��������");
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
	 * ������
	 * 
	 * @param _parent �ݵ�� ����â�� ������ �ش��ϴ� window�� �Է��ؾ���.
	 */
	public WaitProgressBar(Window _parent)
	{
		super(_parent);
		parent = _parent;
		initGUI();
	}

	/**
	 * Runnable���� ���Ǵ� �޼ҵ� �̹Ƿ� �����ڴ� ������� �ʾƾ� ��.
	 * ������ ���۽� �ڵ� ȣ���.
	 */
	public void run()
	{
		pack();
		setLocationRelativeTo(getParent());
		setVisible(true);
		this.repaint();
	}

	/**
	 * �����带 �����Ѵ�.
	 * ���� ���� â�� �����ش�.
	 */
	public void start()
	{
		waitThread = new Thread(WaitProgressBar.this);
		waitThread.start();
	}

	/**
	 * UI ���� �� ������
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
			//â�� ��� �̵� �� �� ����.
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
				//���� Ŭ���� ��� �ݱ� ��ư�� �����־� ���� �� �ֵ��� ��.
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
		//Ȯ�� ���¿��� "���" ��ư�� Ŭ���ϸ� ������.
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
		//����.
		closeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				//�θ�â�� �Բ� ���� ������ ���ο� ���� ����
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
		logButton.setToolTipText("���� ���� �α׸� �����մϴ�.");
		logButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				logButton_actionPerformed(e);
			}
		});
		topPanel.add(sizeLabel, BorderLayout.EAST);
		sizeLabel.setRequestFocusEnabled(false);
		sizeLabel.setToolTipText("ũ�� ����");
		sizeLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		sizeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		sizeLabel.addMouseMotionListener(new MouseMotionAdapter()
		{
			//â ����� ������.
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
			//���� â ���� �� �θ� â�� �������� ���ϵ��� ��.
			@Override
			public void windowOpened(WindowEvent e)
			{
				getParent().setEnabled(false);
			}

			//���� â�� ���� �� �θ�â�� �ٽ� ���� �����ϵ��� ��.
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
	 * ���α׷��� �ٿ� ���� �Է�
	 * @param progressString
	 */
	public void setProgressString(String progressString)
	{
		progressBar.setString(progressString);
	}

	/**
	 * ���α׷��� ���� ���� �Է��Ͽ� 0-100%�� ǥ���ϴ� �޼���
	 * @param value 0���� 100���� �Է�
	 */
	public void setProgressValue(int value)
	{
		progressBar.setIndeterminate(false);
		progressBar.setValue(value);
		progressBar.repaint();
	}

	/**
	 * button panel�� ����� ���� component�� �߰��Ѵ�.
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
	 * ������ ���¸� �Է��� �� �ִ� method�̴�.<br>
	 * textarea�� �Ǿ� ������ �Էµ� ���� �ڵ� ��ũ�� �Ǿ�����.
	 * @param status
	 *        String ���¸� ��Ÿ���� ��.
	 */
	public void setStatus(String status)
	{
		setStatus(status, true);
	}

	/**
	 * ������ ���¸� �Է��� �� �ִ� method�̴�.<br>
	 * nextLine�� true�̸� ���±� �������� New Line Character�� �߰� nextLine�� flase�̸� ���±� �������� New Line Character�� �߰����� ����.
	 * @param status
	 *        String ���¸� ��Ÿ���� ��.
	 * @param nextLine
	 *        boolean ���� ���� �߰� �� ������ ���ο� New Line �߰� �� �Է� ���� ����
	 */
	public void setStatus(String status, boolean nextLine)
	{
		while (waitThread != null && waitThread.isAlive())
		{
		}
		String statusString = status + (nextLine ? "\r\n" : "");
		try
		{
			//���� â�� ���� ������ �״� ��찡 �߻���.
			//50kb �̻��� �Ǹ� ù��° �ٺ��� ���� ����...
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
	 * �� close method�� �ڵ� ���� method�̴�.<br>
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
	 * Ư�� �ð����� ���� �� �ݱ⵵�� �ϴ� method�̴�.
	 * @param delayTime
	 *        int ������ ���̴�. 5���Ŀ� �ݱ� ���ؼ��� 5�� �Է�
	 */
	public void close(int delayTime)
	{
		scrollDown(true);
		progressBar.setIndeterminate(false);
		// delayTime�ʰ� ������ �ݱ�...
		try
		{
			Thread.sleep(delayTime * 1000);
		} catch (InterruptedException ex)
		{
		}
		close();
	}

	/**
	 * �ڵ����� ������ �ʰ� ����ڰ� ���� �� �ֵ��� �ݱ� ��ư�� �����ְų� ������� �ϴ� �ɼ��� �߰��Ͽ���.<br>
	 * �׸��� ���α׷����ٴ� ���̻� �������� �ʰ� �Ǹ�, �� �ڸ��� value ���� ������ �ȴ�.<br>
	 * �ݱ� ��ư�� �ڱ� �ڽŸ� ���� ���� ������ �ڽ��� �θ� ���� ���� �ִ�.<br>
	 * �θ� �ݴ� �ɼ��� ���� �����ڿ��� �޴� �θ� �������� ��Ȯ�� �Ǵ��Ͽ��� �Ѵ�.
	 * @param show
	 *        boolean �ݱ� ��ư�� ���̱� ����.
	 * @param value
	 *        String ���α׷����ٿ� ��Ÿ���� �۾�
	 * @param _isAlertMode
	 *        boolean ����(�˸�) ����ǥ�� �ʿ� ����.
	 * @param _isParentClose
	 *        boolean �θ� Window�� close ����.
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
		
		//20140418, ������ ��� ǥ���� �� �ϴ� ����ǥ�� �ؽ�Ʈ ���μ� �ְ� ���� (BOP ��û�����̾���)
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
	 * ���μ��� ���� �� ���� ������ Ȯ�� �� �� �ִ�.
	 * @param value
	 *        String ���α׷����ٿ� ��Ÿ���� �۾�
	 * @param _isParentClose
	 *        boolean �θ� Window�� close ����.
	 * @param isLogButtonShow
	 *        boolean log ��ư�� �������� ����.
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
	 * ���μ��� ���� �� ��, �ƴϿ��� �����Ͽ� ���� ���� �� �ִ�.
	 * @param value
	 *        String ���α׷����ٿ� ��Ÿ���� �۾�
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
	 * ��ư panel�� �����ִ� �κ�.
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
	 * ���� Window�� ����� ���Ѵ�.
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
	 * �α� ��ư�� ������ ��� ���ÿ� �����ϴ� �κ�.
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
	 * �ڵ� scroll�� ���� �κ�.
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
