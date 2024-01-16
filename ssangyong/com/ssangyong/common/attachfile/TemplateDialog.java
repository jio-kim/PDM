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

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.TreePath;

import com.teamcenter.rac.aif.AbstractAIFDialog;
import com.teamcenter.rac.aif.kernel.AIFComponentContext;
import com.teamcenter.rac.common.TCTree;
import com.teamcenter.rac.common.TCTreeNode;
import com.teamcenter.rac.kernel.TCComponent;
import com.teamcenter.rac.kernel.TCComponentDataset;
import com.teamcenter.rac.util.*;

public class TemplateDialog extends AbstractAIFDialog {
	
	private static final long serialVersionUID = 1L;
	
	private Registry registry = Registry.getRegistry(this);
    private JPanel contentsPane;
    private VerticalLayout verticalLayout = new VerticalLayout();
    private JPanel mainPanel = new JPanel();
    private JPanel buttonPanel = new JPanel();
    private Separator separator = new Separator();
    private JButton selectButton = new JButton();
    private JButton closeButton = new JButton();
    private BorderLayout borderLayout1 = new BorderLayout();
    private JScrollPane jScrollPane1 = new JScrollPane();
    private TCTree iMANTree1 = new TCTree(null, null, false, false, false, false);
    private boolean isRegistActionMethod = false;
    private Object parentObject = null;
    private String methodName = "";
    public static int ACTION_CLOSE = 0;
    public static int ACTION_SELECT = 1;
    private int action = 0;
    private AIFComponentContext resultArray = null;
    private Object templateRoot = null;

    public TemplateDialog(Frame frame, String title, boolean isModal, Object _templateRoot)
    {
        super(frame, title, isModal);
        templateRoot = _templateRoot;
        init();
    }

    public TemplateDialog(Dialog dialog, String title, boolean isModal, Object _templateRoot)
    {
        super(dialog, title, isModal);
        templateRoot = _templateRoot;
        init();
    }

    /**
     * @deprecated UI 전용 생성자. 절대 사용 금지
     * @param flag boolean
     */
    @SuppressWarnings("unused")
	private TemplateDialog(boolean flag)
    {
        super(flag);
        init();
    }

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

    private void jbInit() throws Exception
    {
        contentsPane = (JPanel)this.getContentPane();
        contentsPane.setLayout(verticalLayout);
        contentsPane.setBackground(Color.white);
        mainPanel.setOpaque(false);
        mainPanel.setLayout(borderLayout1);
        buttonPanel.setOpaque(false);
        jScrollPane1.setOpaque(false);
        jScrollPane1.getViewport().setOpaque(false);
        selectButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                selectButton_actionPerformed(e);
            }
        });
        contentsPane.add("unbound.bind", mainPanel);
        contentsPane.add("bottom.bind", buttonPanel);
        contentsPane.add("bottom.bind", separator);

        buttonPanel.add(selectButton);
        buttonPanel.add(closeButton);
        mainPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jScrollPane1.getViewport().add(iMANTree1);
        selectButton.setText(registry.getString("templateDialogSelectButton.NAME"));
        closeButton.setText(registry.getString("templateDialogCloseButton.NAME"));
        jScrollPane1.setPreferredSize(new Dimension(400, 500));
        if(templateRoot != null)
        {
            iMANTree1.setRoot(templateRoot);
            iMANTree1.expandToLevel(10);
        }
        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                closeButton_actionPerformed(e);
            }
        });
        iMANTree1.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if(e.getClickCount() == 2)
                {
                    TreePath treepath;
                    treepath = iMANTree1.getSelectionPath();
                    if(treepath == null)
                    {
                        return;
                    }
                    TCTreeNode imantreenode = (TCTreeNode)treepath.getLastPathComponent();
                    TCComponent imancomponent = (TCComponent)imantreenode.getComponent();
                    if(imancomponent instanceof TCComponentDataset)
                    {
                        try
                        {
                            ((TCComponentDataset)imancomponent).open(false);
                        } catch(Exception ex)
                        {
                            ex.printStackTrace();
                            MessageBox.post(ex);
                        }
                    }
                }
            }
        });
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

    /**
     * 검색 후 어떤 button을 눌러 action이 일어났는지를 돌려줌.
     * @return int
     */
    public int getAction()
    {
        return action;
    }

    private void closeButton_actionPerformed(ActionEvent e)
    {
        action = ACTION_CLOSE;
        setVisible(false);
        dispose();
    }

    /**
     * 검색 결과 리스트를 돌려줌.
     * @return AIFComponentContext
     */
    public AIFComponentContext getSelectedAIFComponentContext()
    {
        return resultArray;
    }

    private void selectButton_actionPerformed(ActionEvent e)
    {
        AIFComponentContext[] resultAIFContext = iMANTree1.getSelectedContexts();
        if(resultAIFContext == null || resultAIFContext.length != 1)
        {
            MessageBox.post(this, registry.getString("templateDialogNoSelect.MESSAGE"), "Warning", MessageBox.WARNING);
            return;
        }
        if(!(resultAIFContext[0].getComponent() instanceof TCComponentDataset))
        {
            MessageBox.post(this, registry.getString("templateDialogNonDataset.MESSAGE"), "Warning", MessageBox.WARNING);
            return;
        }
        if(isRegistActionMethod)
        {
            try
            {
                Utilities.invokeMethod(parentObject, methodName, new Object[]
                    {resultAIFContext[0]});
            } catch(Exception ex)
            {
                MessageBox.post(ex);
                ex.printStackTrace();
                return;
            }
        }
        action = ACTION_SELECT;
        resultArray = resultAIFContext[0];
        setVisible(false);
        dispose();
    }
}
