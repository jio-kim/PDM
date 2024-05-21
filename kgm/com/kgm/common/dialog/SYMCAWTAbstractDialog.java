package com.kgm.common.dialog;

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

import com.kgm.common.FunctionField;
import com.kgm.common.OnlyDateButton;
import com.kgm.common.utils.CustomUtil;
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
 * AbstractAIFDialog �� ��ӹ޾Ƽ� ���� ���̾�α� �⺻������ OK,Apply,Cancel 
 * ��ư�� �����ϸ� ��� ���� �� �ش� ��ư�� Ŭ�� ��������� �̺�Ʈ ó���� �������̵� �ϸ� �ȴ�. 
 * �������̵� ���� ���� ��� ������ ��ư ��� ����Ʈ �׼�ó���� dispose()�� �ȴ�. 
 * �� ���̾�α׸� ��� ���� �� ��ư �г��� ������ ���� UI�κ��� �ٿ� �ִ´�. 
 * �⺻���� �����ʴ� ��� implements �ϰ� ������ ��� ���� �� �޼ҵ带 ������ �ؼ� ����ϸ� ��.
 * 
 * @Copyright : S-PALM
 * @author : ������
 * @since : 2012. 3. 20. Package ID : sns.teamcenter.common.SpalmAbstractDialog.java
 */
@SuppressWarnings({"unused", "rawtypes"})
public abstract class SYMCAWTAbstractDialog extends AbstractAIFDialog implements InterfaceAIFOperationListener,
		ActionListener, ItemListener, KeyListener, MouseListener, WindowFocusListener,
		PropertyChangeListener, TreeSelectionListener {

	/**
	 * ���̾�α� �ϴ��� ��ư ���ѻ�
	 */
	protected JButton okButton, applyButton, cancelButton;

	private Container container;

	public TCSession session;

	/** ���̾�α� ����� �������� �޼��� label */
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
	 * ���̾�α� ȭ���� �⺻UI�� �����ϴ� �޼ҵ� �������� ȣ�� �Ͽ�����.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2011. 9. 26.
	 */
	protected void createDialogUI(String header_message, Icon header_icon) {
		this.createUI();
		/**
		 * �ֿ��ڵ��� New PLM Project
		 * 2012/12/24 PM ��������, ���� Panel Hidden.
		 */
		// ���� Title, Icon Panel ����.
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
	 * ���̾�α��� ����� �׸��� �޼ҵ�
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
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
		multilinewrappedtextlabel.setFont(new Font("���� ���", Font.BOLD, 16));
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
	 * �ڽ� Ŭ�������� ���� UI�� �׷����� panel�� ���� �� ���� ��Ų��.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 5. 2.
	 * @return
	 */
	protected abstract JPanel getUIPanel();

	/**
	 * �⺻���� ��ư bar�� �׸��� �޼ҵ� OK, APPLY, CANCEL ������ ��ư�� ���� �Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
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
					// [20240522][UPGRADE] Validation Check ����� ���� â�� �������� ����
					if (saveAction(e)) {
						closeDialog();
					}
				} catch (Exception e1)
				{
					MessageBox.post(SYMCAWTAbstractDialog.this, e1);
					e1.printStackTrace();
					return;
				}
//				closeDialog();
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
//				applyButton.addActionListener(this);		// �ߺ� �������� ���� ������.
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
	 * OK��ư Ŭ�������� �̺�Ʈ ó�� �κ� ��ӹ��� �� �� �����ؼ� ����Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2011. 9. 26.
	 */
	// [20240522][UPGRADE] validation Check �� return �� �߰� 
	public boolean saveAction(ActionEvent e) throws Exception {
		if (validCheck()) {
			if (confirmCheck()) {
				invokeOperation(e);
			}
		} else {
			return false;
		}
		return true;
	}

	/**
	 * ������ �ٸ��� �Է� �ϼ˽��ϱ�?? Ȯ�� �޽���â ����.
	 * 
	 * @Copyright : S-PALM
	 * @author : �ǻ��
	 * @since : 2012. 5. 21.
	 * @return
	 */
	public boolean confirmCheck() {
		// int showOK = JOptionPane.showConfirmDialog(null,
		// "�Է� ������ �ٽ� �ѹ� Ȯ�� �Ͻʽÿ�. ������ �ٸ��� �Է� �ϼ˽��ϱ�? \n����(����) �Ͻ÷��� ��(Y) ��ư�� ��������.", "Create...",
		// JOptionPane.YES_NO_OPTION);
		// if(showOK != 0){
		// return false;
		// }
		return true;
	}

	/**
	 * Cancel��ư Ŭ�������� �̺�Ʈ ó�� �κ� ��ӹ��� �� �� �����ؼ� ����Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2011. 9. 26.
	 */
	public void cancelButtonClicked(ActionEvent e) {
		closeDialog();
	}

	/**
	 * Apply��ư Ŭ�������� �̺�Ʈ ó�� �κ� ��ӹ��� �� �� �����ؼ� ����Ѵ�.
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
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
	 * @author : �ǻ��
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
	 * @author : ������
	 * @since : 2011. 9. 30.
	 */
	public void closeDialog() {
		setVisible(false);
		disposeDialog();
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
	 * @since : 2012. 3. 22.
	 * @param size
	 */
	public void setDialogSize(Dimension size) {
		container.setPreferredSize(size);
	}

	/**
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
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
	 * @author : ������
	 * @since : 2012. 3. 22.
	 * @return �������̼� üũ ���
	 */
	public abstract boolean validCheck()  throws Exception;

	/**
	 * operation class�� ȣ��. �Ǵ� operation ������ ����
	 * 
	 * @Copyright : S-PALM
	 * @author : ������
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
