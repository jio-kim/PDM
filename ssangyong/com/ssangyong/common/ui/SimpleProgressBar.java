package com.ssangyong.common.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import com.teamcenter.rac.util.HorizontalLayout;
import com.teamcenter.rac.util.Registry;

@SuppressWarnings("serial")
public class SimpleProgressBar extends JWindow
{
    private JPanel panel;
    private JLabel lb;
    
    public SimpleProgressBar()
    {
        init();
        add(panel);
        pack();
        // centerToScreen();
        // setAlwaysOnTop( true );
    }
    
    private void init()
    {
        panel = new JPanel(new HorizontalLayout());
        lb = new JLabel("Loading...");
        Font font = new Font("", Font.CENTER_BASELINE, 12);
        lb.setFont(font);
        Registry registry = Registry.getRegistry("com.ssangyong.common.common");
        ImageIcon icon = registry.getImageIcon("Loadding.ICON");
        JLabel iconLabel = new JLabel(icon);
        panel.setBackground(Color.white);
        panel.setBorder(BorderFactory.createTitledBorder(""));
        panel.add("Right", lb);
        panel.add("left", iconLabel);
        add(panel);
    }
    
    public void setProgressVisible(boolean flag)
    {
        setVisible(flag);
    }
    
    public void setLabel(String label)
    {
        lb.setText(label);
        pack();
    }
    
    public void centerToScreen(Shell shell)
    {
        Rectangle shlParent = shell.getBounds();
        java.awt.Rectangle shlChild = this.getBounds();
        int x = shell.getLocation().x + (shlParent.width - shlChild.width) / 2;
        int y = shell.getLocation().y + (shlParent.height - shlChild.height) / 2;
        this.setLocation(x, y);
    }
    
    public void centerToscree(Frame frame)
    {
        java.awt.Rectangle shlParent = frame.getBounds();
        java.awt.Rectangle shlChild = this.getBounds();
        int x = frame.getLocation().x + (shlParent.width - shlChild.width) / 2;
        int y = frame.getLocation().y + (shlParent.height - shlChild.height) / 2;
        this.setLocation(x, y);
    }

    /**
     * 상위의 Composite 중앙으로 이동시키는 함수
     * 
     * @param parent
     */
    public void centerToParent(Composite parent)
    {
        Rectangle shlParent = parent.getBounds();
        java.awt.Rectangle shlChild = this.getBounds();
        int x = parent.getLocation().x + (shlParent.width - shlChild.width) / 2;
        int y = parent.getLocation().y + (shlParent.height - shlChild.height) / 2;
        this.setLocation(x, y);
    }
}
