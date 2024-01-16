package com.ssangyong.common.utils;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.Container;
import java.awt.Frame;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.teamcenter.rac.util.Registry;
import com.ssangyong.viewer.AbstractSYMCViewer;

public class ProgressBar {

    private Registry registry;

    private Shell parentShell;
    private Shell thisShell;

    private JDialog dialog;
    private JLabel textLabel;

    public ProgressBar(Shell parentShell) {
        registry = Registry.getRegistry(AbstractSYMCViewer.class);
        this.parentShell = parentShell;
    }

    private void initUI() {
        parentShell.getDisplay().syncExec(new Runnable() {
            @Override
            public void run() {
                if (!parentShell.isDisposed()) {
                    for (Control childControl : parentShell.getChildren()) {
                        childControl.setEnabled(false);
                        childControl.update();
                    }
                }
            }
        });

        try {
            thisShell = new Shell(parentShell, SWT.APPLICATION_MODAL);
            Composite composite = new Composite(thisShell, SWT.EMBEDDED | SWT.NONE);
            Frame frame = SWT_AWT.new_Frame(composite);

            dialog = new JDialog(frame);
            Icon imageIcon = registry.getImageIcon("ProgressBar.ICON");
            JLabel imageLabel = new JLabel(imageIcon);
            textLabel = new JLabel();

            JPanel imagePanel = new JPanel();
            imagePanel.setBackground(Color.WHITE);
            imagePanel.add(imageLabel);

            JPanel textPanel = new JPanel();
            textPanel.setBackground(new Color(234, 234, 234));
            textPanel.add(textLabel);
            textLabel.setText("<html>Processing...<br>Please wait until completion.</html>");

            Container container = dialog.getContentPane();
            container.setLayout(new BorderLayout());
            container.add(imagePanel, BorderLayout.CENTER);
            container.add(textPanel, BorderLayout.SOUTH);

            dialog.setTitle("Processing");
            dialog.setIconImage(registry.getImageIcon("ProgressBar.titleICON").getImage());
            dialog.setSize(imageIcon.getIconWidth() + 100, imageIcon.getIconHeight() + 100);
            dialog.setUndecorated(false);
            dialog.setAlwaysOnTop(false);
            dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

            UIUtil.centerToParent(parentShell, dialog);

            dialog.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    if (!parentShell.isDisposed()) {
                        parentShell.getDisplay().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                if (!parentShell.isDisposed()) {
                                    for (Control childControl : parentShell.getChildren()) {
                                        childControl.setEnabled(true);
                                        childControl.update();
                                    }
                                }
                            }
                        });
                    }
                }
            });
        } catch (Exception e) {
            parentShell.getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    if (!parentShell.isDisposed()) {
                        for (Control childControl : parentShell.getChildren()) {
                            childControl.setEnabled(true);
                            childControl.update();
                        }
                    }
                }
            });
        }
    }

    public String getText() {
        return textLabel.getText();
    }

    public void setText(String text) {
        textLabel.setText(text);
    }

    public void start() {
        initUI();
        dialog.setVisible(true);
    }

    public void close() {
        if (thisShell != null)
            thisShell.dispose();
    }

    public boolean isDisposed() {
        return thisShell != null ? thisShell.isDisposed() : true;
    }

    public void setActive() {
        if (!thisShell.isDisposed()) {
            thisShell.setActive();
            thisShell.setFocus();
        }
    }
    
    public void setAlwaysOnTop(boolean bln) {
        dialog.setAlwaysOnTop(bln);
    }

}
