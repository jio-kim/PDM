package com.ssangyong.common.utils.progressbar;


import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import ca.odell.glazedlists.EventList;

import com.ssangyong.common.utils.progressbar.WaitProgressDialog.MessageData;
import com.teamcenter.rac.util.MessageBox;

/**
 * Wait progress Dialog
 * 
 * WaitProgressor waitProgressor = new WaitProgressor(getShell()); 
 * <BR> 1. waitProgressor.start() //시작. 
 * <BR> 2. waitProgressor.isShowMessageTable(true); // Table Message 창 표시 유무 설정
 * <BR> 3. waitProgressor.end(true); // 종료. 창 닫기 유무 설정
 * 
 * @author Administrator
 * 
 */
public class WaitProgressor
{

    private ScheduledExecutorService scheduledThreadPool;
    private WaitDialogLoader dialogLoader = null;
    private Shell parentShell = null;
    private boolean isSizeSetted = false; // 사이즈가 설정되었는지 여부
    private int width = 0; // Dialog 너비
    private int height = 0; // Dialog 높이
    private boolean isShowMessageTable = false; // Message Table을 보여야 하는 지 유무
    private int[] msgTableVisibleColumnIndexs; // Message Table 보여지는 Column 인텍스
    WaitProgressCancelImpl cancelImpl;
    /**
     * 생성자
     * 
     * @param parentShell
     */
    public WaitProgressor(Shell parentShell)
    {
        this.parentShell = parentShell;
    }

    
    
    /**
     * Progress 시작
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
     * Progress 중지
     */
    public void end()
    {
        end(true);
    }

    /**
     * Progress 중지
     * 
     * @param isCloseDialog 자동 닫기 유무
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
                // 완료 처리
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
     * 메인 메세지 출력
     * 
     * @param message
     */
    public void setMessage(String message)
    {
        setMainMessage(message);
    }

    /**
     * Main Message 입력
     * 
     * @param message 메세지
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
     * Main Message 입력
     * 
     * @param message 메세지
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
     * Table Message 설정
     * 
     * @param msgType 메세지 유형 (ex. 에러 등)
     * @param targetId 메세지의 대상 Item Id
     * @param message 메세지 내용
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
     * Table Message 설정
     * 
     * @param msgType 메세지 유형 (ex. 에러 등)
     * @param message 메세지 내용 메세지 내용
     */
    public void setTableMessage(String msgType, String message)
    {
        setTableMessage(msgType, "", message);
    }

    /**
     * Table Message 설정
     * 
     * @param message
     */
    public void setTableMessage(String message)
    {
        setTableMessage("", "", message);
    }
    

    

    /**
     * Wait Dialog 를 실행함
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
                        // 사이즈가 설정되었다면
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
         * Wait Dialog 닫기
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
     * Dialog 사이즈 설정
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
     * 메세지 Table 보이는 지여부
     * 
     * @param isShow
     */
    public void isShowMessageTable(boolean isShow)
    {
        isShowMessageTable = isShow;
    }

    /**
     * Message Table의 보여지는 Column Index
     * 
     * @param index
     */
    public void SetMessageTableVisibleColumns(int[] msgTableVisibleColumnIndexs)
    {
        this.msgTableVisibleColumnIndexs = msgTableVisibleColumnIndexs;
    }

    /**
     * Table 메세지 리스트를 가져옴
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
     * Dialog Return 값
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
