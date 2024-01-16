package com.ssangyong.common.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;

import com.ssangyong.common.FunctionField;
import com.ssangyong.common.OnlyDateButton;
import com.ssangyong.common.utils.CustomUtil;
import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.InterfaceAIFOperationListener;
import com.teamcenter.rac.kernel.TCSession;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.MessageBox;
import com.teamcenter.rac.util.MultiLineWrappedTextLabel;
import com.teamcenter.rac.util.Painter;
import com.teamcenter.rac.util.Picture;
import com.teamcenter.rac.util.Registry;
import com.teamcenter.rac.util.Separator;
import com.teamcenter.rac.util.Utilities;
import com.teamcenter.rac.util.VerticalLayout;
import com.teamcenter.rac.util.iTextArea;

/**
 * AbstractAIFDialog 를 상속받아서 만든 다이얼로그 기본적으로 OK,Apply,Cancel 
 * 버튼이 존재하며 상속 받은 후 해당 버튼이 클릭 됐을경우의 이벤트 처리를 오버라이드 하면 된다. 
 * 오버라이드 하지 않을 경우 세개의 버튼 모두 디폴트 액션처리는 dispose()가 된다. 
 * 이 다이얼로그를 상속 받은 후 버튼 패널을 제외한 실제 UI부분을 붙여 넣는다. 
 * 기본적인 리스너는 모두 implements 하고 있으니 상속 받은 후 메소드를 재정의 해서 사용하면 됨.
 * 
 * @Copyright : S-PALM
 * @author : 이정건
 * @since : 2012. 3. 20. Package ID : sns.teamcenter.common.SpalmAbstractDialog.java
 */
@SuppressWarnings({"unused", "rawtypes"})
public abstract class SYMCAWTAbstractDialog extends AbstractAIFDialog implements InterfaceAIFOperationListener,
		ActionListener, ItemListener, KeyListener, MouseListener, WindowFocusListener,
		PropertyChangeListener, TreeSelectionListener {

	/**
	 * 다이얼로그 하단의 버튼 삼총사
	 */
	protected JButton okButton, applyButton, cancelButton;

	private Container container;

	public TCSession session;

	/** 다이얼로그 헤더에 보여지는 메세지 label */
	private MultiLineWrappedTextLabel multilinewrappedtextlabel;

	private static final long serialVersionUID = 1L;

	public SYMCAWTAbstractDialog(Frame frame) {
		this(frame, false);
	}

	public SYMCAWTAbstractDialog(Dialog dialog) {
		this(dialog, false);
	}
	
	public SYMCAWTAbstractDialog(Frame frame, boolean flag) {
		this(frame, null, flag);
	}

	public SYMCAWTAbstractDialog(Dialog dialog, boolean flag) {
		this(dialog, null, flag);
	}

	public SYMCAWTAbstractDialog(Frame frame, String dialog_title, boolean flag) {
		this(frame, dialog_title, "", null, flag);
	}

	public SYMCAWTAbstractDialog(Dialog dialog, String dialog_title, boolean flag) {
		this(dialog, dialog_title, "", null, flag);
	}

	public SYMCAWTAbstractDialog(Frame frame, String dialog_title, String header_message,
			ImageIcon header_icon, boolean flag) {
		super(frame, dialog_title, flag);
		this.session = CustomUtil.getTCSession();
	}

	public SYMCAWTAbstractDialog(Dialog dialog, String dialog_title, String header_message,
			ImageIcon header_icon, boolean flag) {
		super(dialog, dialog_title, flag);
		this.session = CustomUtil.getTCSession();
	}

	public SYMCAWTAbstractDialog(Frame frame, String s) {
		this(frame, s, false);
	}

	public SYMCAWTAbstractDialog(Dialog dialog, String s) {
		this(dialog, s, false);
	}

	public SYMCAWTAbstractDialog(boolean flag) {
		this(Utilities.getCurrentFrame(), flag);
	}

	/**
	 * 다이얼로그 화면의 기본UI를 셋팅하는 메소드 하위에서 호출 하여야함.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 9. 26.
	 */
	protected void createDialogUI(String header_message, Icon header_icon) {
		this.createUI();
		/**
		 * 쌍용자동차 New PLM Project
		 * 2012/12/24 PM 제안으로, 상위 Panel Hidden.
		 */
		// 상위 Title, Icon Panel 삭제.
		// container.add("top.bind.center.center", createGradientHeader(header_message, header_icon));
	}

	private void createUI() {
		container = getContentPane();
		container.setBackground(Color.WHITE);
		container.setLayout(new VerticalLayout(0, 0, 0, 0, 0));

		container.add("bottom.bind.center.center", createButtonPanel());
		container.add("bottom.bind.center.center", new Separator());
		addListener();
	}

	/**
	 * 다이얼로그의 헤더를 그리는 메소드
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 9. 26.
	 * @param header_message
	 * @param header_icon
	 * @return JPanel
	 */
	private JPanel createGradientHeader(String header_message, Icon header_icon) {
		final Color color = new Color(135, 206, 235);
		final Color color1 = Color.white;
		JPanel headerPanel = new JPanel(new VerticalLayout(0, 0, 0, 0, 0)) {
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				Painter.paintHorizontalGradient(this, g, color, color1);
				super.paint(g);
			}
		};
		headerPanel.setOpaque(false);
		headerPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		JPanel panel1 = new JPanel(new HorizontalLayout(20, 10, 10, 5, 5));
		panel1.setOpaque(false);
		if (header_icon != null) {
			Picture picture = new Picture((ImageIcon) header_icon);
			picture.setOpaque(false);
			panel1.add("left.bind.center.center", picture);
		}

		multilinewrappedtextlabel = new MultiLineWrappedTextLabel(header_message);
		multilinewrappedtextlabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
		multilinewrappedtextlabel.setForeground(Color.DARK_GRAY);
		multilinewrappedtextlabel.setOpaque(false);

		Registry reg = Registry.getRegistry(this);

		Picture picture = new Picture(reg.getImageIcon("ssangyong_main.ICON"));
		picture.setOpaque(false);

		panel1.add("unbound.bind", multilinewrappedtextlabel);
		panel1.add("right.bind.center.center", picture);

		headerPanel.add("bottom.bind.center.center", new Separator());
		headerPanel.add("unbound.bind", panel1);

		return headerPanel;
	}

	/**
	 * 자식 클래스에서 실제 UI가 그려지는 panel을 생성 후 리턴 시킨다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 5. 2.
	 * @return
	 */
	protected abstract JPanel getUIPanel();

	/**
	 * 기본적인 버튼 bar를 그리는 메소드 OK, APPLY, CANCEL 세가지 버튼만 존재 한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 9. 26.
	 */
	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
		panel.setOpaque(false);

		Registry registry = Registry.getRegistry(this);
		okButton = new JButton(registry.getString("OK.TEXT"), registry.getImageIcon("OK_24.ICON"));
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButton.removeActionListener(null);
				try
				{
					saveAction(e);
				} catch (Exception e1)
				{
					MessageBox.post(SYMCAWTAbstractDialog.this, e1);
					e1.printStackTrace();
					return;
				}
				closeDialog();
//				okButton.addActionListener(this);
			}
		});

		applyButton = new JButton(registry.getString("Apply.TEXT"), registry.getImageIcon("Apply_24.ICON"));
		applyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				applyButton.removeActionListener(null);
				try
				{
					saveAction(e);
				} catch (Exception e1)
				{
					e1.printStackTrace();
					MessageBox.post(SYMCAWTAbstractDialog.this, e1);
				}
//				applyButton.addActionListener(this);		// 중복 생성으로 인해 제거함.
			}
		});

		cancelButton = new JButton(registry.getString("Cancel.TEXT"), registry.getImageIcon("Cancel_24.ICON"));
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton.removeActionListener(null);
				cancelButtonClicked(e);
//				cancelButton.addActionListener(this);
			}
		});

		panel.add(okButton);
		panel.add(applyButton);
		panel.add(cancelButton);

		return panel;
	}

	/**
	 * OK버튼 클릭했을때 이벤트 처리 부분 상속받은 후 재 정의해서 사용한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 9. 26.
	 */
	public void saveAction(ActionEvent e) throws Exception {
		if (validCheck()) {
			if (confirmCheck()) {
				invokeOperation(e);
			}
		}
	}

	/**
	 * 정보를 바르게 입력 하셧습니까?? 확인 메시지창 생성.
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 5. 21.
	 * @return
	 */
	public boolean confirmCheck() {
		// int showOK = JOptionPane.showConfirmDialog(null,
		// "입력 정보를 다시 한번 확인 하십시오. 정보를 바르게 입력 하셧습니까? \n진행(생성) 하시려면 예(Y) 버튼을 누르세요.", "Create...",
		// JOptionPane.YES_NO_OPTION);
		// if(showOK != 0){
		// return false;
		// }
		return true;
	}

	/**
	 * Cancel버튼 클릭했을때 이벤트 처리 부분 상속받은 후 재 정의해서 사용한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 9. 26.
	 */
	public void cancelButtonClicked(ActionEvent e) {
		closeDialog();
	}

	/**
	 * Apply버튼 클릭했을때 이벤트 처리 부분 상속받은 후 재 정의해서 사용한다.
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 9. 26.
	 */
//	public void applyButtonClicked(final ActionEvent e) {
//		if (validCheck()) {
//			if (confirmCheck()) {
//				invokeOperation(e);
//				// clearPanel(getUIPanel());
//			}
//		}
//	}

	/**
	 * clear dialog
	 * 
	 * @Copyright : S-PALM
	 * @author : 권상기
	 * @since : 2012. 5. 2.
	 */
	public void clearPanel(JComponent dialog) {

		int size = dialog.getComponentCount();
		for (int i = 0; i < size; i++) {
			Component component = dialog.getComponent(i);
			System.out.println("component" + component);
			if (component instanceof JPanel) {
				clearPanel((JComponent) component);
			}
			if (component instanceof JScrollPane) {
				clearPanel((JComponent) component);
			}
			if (component instanceof JViewport) {
				clearPanel((JComponent) component);
			}
			if (component instanceof FunctionField) {
				((FunctionField) component).setText("");
			} else if (component instanceof OnlyDateButton) {
				((OnlyDateButton) component).setDate(null);
			} else if (component instanceof JComboBox) {
				((JComboBox) component).setSelectedIndex(0);
			} else if (component instanceof iTextArea) {
				((iTextArea) component).setText("");
			}
		}
	}

	/**
	 * close dialog
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2011. 9. 30.
	 */
	public void closeDialog() {
		setVisible(false);
		disposeDialog();
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 22.
	 * @param size
	 */
	public void setDialogSize(Dimension size) {
		container.setPreferredSize(size);
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 22.
	 * @param x
	 * @param y
	 */
	public void setDialogSize(int x, int y) {
		this.setDialogSize(new Dimension(x, y));
	}

	private void addListener() {
		addKeyListener(this);
		addMouseListener(this);
		addPropertyChangeListener(this);
		addWindowFocusListener(this);
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 22.
	 * @return 벨리데이션 체크 결과
	 */
	public abstract boolean validCheck()  throws Exception;

	/**
	 * operation class를 호출. 또는 operation 로직을 구현
	 * 
	 * @Copyright : S-PALM
	 * @author : 이정건
	 * @since : 2012. 3. 22.
	 */
	public abstract void invokeOperation(ActionEvent e) throws Exception;

	@Override
	public void endOperation() {
	}

	@Override
	public void startOperation(String s) {
	}

	@Override
	public void actionPerformed(ActionEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			closeDialog();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
	}

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
	}

	@Override
	public void windowGainedFocus(WindowEvent e) {
	}

	@Override
	public void windowLostFocus(WindowEvent e) {
	}

	@Override
	public void valueChanged(TreeSelectionEvent e) {
	}

	public void setHeaderFont(Font font) {
		multilinewrappedtextlabel.setFont(font);
	}
	
	public void showVisible(boolean value) {
		applyButton.setVisible(value);
	}
}
