package com.kgm.common.utils.progressbar;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ca.odell.glazedlists.EventList;

import com.kgm.common.utils.progressbar.WaitProgressDialog.MessageData;
import com.teamcenter.rac.util.MessageBox;

/**
 * Wait progress Dialog
 * 
 * WaitProgressor waitProgressor = new WaitProgressor(getShell()); 
 * <BR> 1. waitProgressor.start() //����. 
 * <BR> 2. waitProgressor.isShowMessageTable(true); // Table Message â ǥ�� ���� ����
 * <BR> 3. waitProgressor.end(true); // ����. â �ݱ� ���� ����
 * 
 * @author Administrator
 * 
 */
public class WaitProgressor
{

    private ScheduledExecutorService scheduledThreadPool;
    private WaitDialogLoader dialogLoader = null;
    private Shell parentShell = null;
    private boolean isSizeSetted = false; // ����� �����Ǿ����� ����
    private int width = 0; // Dialog �ʺ�
    private int height = 0; // Dialog ����
    private boolean isShowMessageTable = false; // Message Table�� ������ �ϴ� �� ����
    private int[] msgTableVisibleColumnIndexs; // Message Table �������� Column ���ؽ�
    WaitProgressCancelImpl cancelImpl;
    /**
     * ������
     * 
     * @param parentShell
     */
    public WaitProgressor(Shell parentShell)
    {
        this.parentShell = parentShell;
    }

    
    
    /**
     * Progress ����
     */
    public void start()
    {

        scheduledThreadPool = Executors.newScheduledThreadPool(1);
        dialogLoader = new WaitDialogLoader(parentShell);
        scheduledThreadPool.schedule(dialogLoader, 0, TimeUnit.MICROSECONDS);

    }
    
    
    public void start(WaitProgressCancelImpl cancelImpl)
    {
        this.start();
        this.cancelImpl = cancelImpl;
    }
    

    /**
     * Progress ����
     */
    public void end()
    {
        end(true);
    }

    /**
     * Progress ����
     * 
     * @param isCloseDialog �ڵ� �ݱ� ����
     */
    public void end(boolean isCloseDialog)
    {
        if (scheduledThreadPool == null)
            return;
        try
        {
            if (isCloseDialog)
                dialogLoader.close();
            else
            {
                if (dialogLoader.getDialog() == null)
                {
                    System.out.println("####dialogLoader.getDialog is Null");
                    return;
                }
                // �Ϸ� ó��
                dialogLoader.getDialog().setCompleteAction();
            }
            if (scheduledThreadPool != null)
                scheduledThreadPool.shutdownNow();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (scheduledThreadPool != null)
                scheduledThreadPool.shutdownNow();
            try
            {
                dialogLoader.close();
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
            }
            MessageBox.post(e.toString(), "Error", MessageBox.WARNING);
        }

    }
    
    

    /**
     * ���� �޼��� ���
     * 
     * @param message
     */
    public void setMessage(String message)
    {
        setMainMessage(message);
    }

    /**
     * Main Message �Է�
     * 
     * @param message �޼���
     */
    public void setMainMessage(String message)
    {
        if (dialogLoader == null)
            return;
        try
        {
            while (dialogLoader.getDialog() == null)
            {
                Thread.sleep(100);
            }
            dialogLoader.getDialog().setMainMessage(message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            scheduledThreadPool.shutdownNow();
        }
    }
    
    
    /**
     * Main Message �Է�
     * 
     * @param message �޼���
     */
    public void setProgressTitle(String title)
    {
        if (dialogLoader == null)
            return;
        try
        {
            while (dialogLoader.getDialog() == null)
            {
                Thread.sleep(100);
            }
            dialogLoader.getDialog().setTitle(title);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            scheduledThreadPool.shutdownNow();
        }
    }
    
    

    /**
     * Table Message ����
     * 
     * @param msgType �޼��� ���� (ex. ���� ��)
     * @param targetId �޼����� ��� Item Id
     * @param message �޼��� ����
     */
    public void setTableMessage(String msgType, String targetId, String message)
    {
        if (dialogLoader == null || !isShowMessageTable)
            return;
        try
        {
            while (dialogLoader.getDialog() == null)
            {
                Thread.sleep(100);
            }

            dialogLoader.getDialog().setTableMessage(msgType, targetId, message);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            scheduledThreadPool.shutdownNow();
        }
    }

    /**
     * Table Message ����
     * 
     * @param msgType �޼��� ���� (ex. ���� ��)
     * @param message �޼��� ���� �޼��� ����
     */
    public void setTableMessage(String msgType, String message)
    {
        setTableMessage(msgType, "", message);
    }

    /**
     * Table Message ����
     * 
     * @param message
     */
    public void setTableMessage(String message)
    {
        setTableMessage("", "", message);
    }
    

    

    /**
     * Wait Dialog �� ������
     * 
     * @author Administrator
     * 
     */
    class WaitDialogLoader implements Runnable
    {
        private WaitProgressDialog dialog;
        private Shell shell = null;

        public WaitDialogLoader(Shell shell)
        {
            this.shell = shell;
        }

        public void run()
        {
            try
            {
                Display.getDefault().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        // ����� �����Ǿ��ٸ�
                        if (isSizeSetted)
                            dialog = new WaitProgressDialog(shell, width, height, isShowMessageTable, msgTableVisibleColumnIndexs, cancelImpl);
                        else
                            dialog = new WaitProgressDialog(shell, isShowMessageTable, msgTableVisibleColumnIndexs, cancelImpl);

                        
                        dialog.setBlockOnOpen(true);
                        dialog.open();
                        
                    }
                });

            }
            catch (Exception e)
            {
                if (scheduledThreadPool == null)
                    return;
                scheduledThreadPool.shutdownNow();
                if (dialog == null)
                    return;
                try
                {
                    close();
                }
                catch (Exception e1)
                {
                    MessageBox.post(e1.toString(), "Error", MessageBox.WARNING);
                }

            }
        }

        /**
         * Wait Dialog �ݱ�
         * 
         * @throws Exception
         */
        public void close() throws Exception
        {
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    if (dialog == null)
                        return;
                    dialog.close();
                }
            });
        }

        /**
         * WaitDialog
         * 
         * @return
         */
        public WaitProgressDialog getDialog()
        {
            return dialog;
        }
    }

    /**
     * Dialog ������ ����
     * 
     * @param width
     * @param height
     */
    public void setDialogSize(int width, int height)
    {
        this.width = width;
        this.height = height;
        this.isSizeSetted = true;
    }

    /**
     * �޼��� Table ���̴� ������
     * 
     * @param isShow
     */
    public void isShowMessageTable(boolean isShow)
    {
        isShowMessageTable = isShow;
    }

    /**
     * Message Table�� �������� Column Index
     * 
     * @param index
     */
    public void SetMessageTableVisibleColumns(int[] msgTableVisibleColumnIndexs)
    {
        this.msgTableVisibleColumnIndexs = msgTableVisibleColumnIndexs;
    }

    /**
     * Table �޼��� ����Ʈ�� ������
     * 
     * @return
     */
    public EventList<MessageData> getTableMessageList()
    {

        if (dialogLoader.getDialog() == null)
            return null;
        return dialogLoader.getDialog().getTableDataList();

    }

    /**
     * Dialog Return ��
     * 
     * @return
     */
    public int getDialogReturnCode()
    {
        if (dialogLoader.getDialog() == null || dialogLoader.getDialog().getShell() == null || dialogLoader.getDialog().getShell().isDisposed())
            return Window.OK;
        return dialogLoader.getDialog().getReturnCode();
    }
}
