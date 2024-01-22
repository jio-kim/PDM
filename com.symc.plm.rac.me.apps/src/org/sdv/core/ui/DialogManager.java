/**
 * 
 */
package org.sdv.core.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.window.WindowManager;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;

/**
 * Class Name : DialogManager
 * Class Description :
 * 
 * @date 2014. 1. 8.
 * @author CS.Park
 * 
 */
public class DialogManager extends WindowManager {

	// dialogStack.add(null);  			 // 제일 마지막에 입력한 요소가 최상단에 위치
	// dialogStack.peek();    			 // 제일 상단에 있는 요소를 반환
	// dialogStack.pop();      			 // 제일 상단에 있는 요소를 반환하고 제거
	// dialogStack.push(item); 			 // 요소를 최상단으로 밀어낸다.
	// dialogStack.set(index, elemenet); // 요소를 특정 index 번호로 밀어낸다.
	// dialogStack.search(0);   	     //해당요소의 인덱스 번호 호출
	private Stack<IDialog> dialogStack = new Stack<IDialog>();
	private List<DialogManager>	  subManagers;
	private UIManager uiManager;

	
	public DialogManager(UIManager uiManager)
	{
		super();
	}

	public DialogManager(DialogManager parent, UIManager uiManager)
	{
		this(uiManager);
		parent.addDialogManager(this);
	}
	
	public UIManager getUIManager(){
		return this.uiManager;
	}

	protected void addDialogManager(DialogManager dm)
	{
		if (this.subManagers == null) {
			this.subManagers = new ArrayList<DialogManager>();
		}
		if (!(this.subManagers.contains(dm)))
			this.subManagers.add(dm);
	}
	
	public int getDialogCount()
	{
		return this.dialogStack.size();
	}
	
	public Dialog[] getDialogs()
	{
		Dialog[] bs = new Dialog[this.dialogStack.size()];
		this.dialogStack.toArray(bs);
		return bs;
	}
	
	public void add(IDialog dialog)
	{
		if (!(this.dialogStack.contains(dialog))) {
			this.dialogStack.add(dialog);
			this.setCurrentDialog(dialog);
			dialog.setWindowManager(this);
		}
	}
	
	@Override
	public void add(Window window)
	{
		if(window instanceof IDialog){
			add((IDialog)window);
		}
	}	

	@SuppressWarnings("unchecked")
	@Override
	public boolean close()
	{
		List<Dialog> t = (List<Dialog>)this.dialogStack.clone();
		for(Dialog dialog : t){
			boolean closed = dialog.close();
			if (!(closed)) {
				return false;
			}
		}
		
		if (this.subManagers != null) {
			for(DialogManager dm : this.subManagers){
				boolean closed = dm.close();
				if (!(closed)) {
					return false;
				}
			}
		}
		return true;
	}

	public void remove(IDialog dialog)
	{
		if (this.dialogStack.contains(dialog)) {
			this.dialogStack.remove(dialog);
			dialog.setWindowManager(null);
		}
	}
	
	
	@Override
	public int getWindowCount()
	{
		return getDialogCount();
	}	
	
	@Override
	public Window[] getWindows()
	{
		Window[] bs = new Window[this.dialogStack.size()];
		this.dialogStack.toArray(bs);
		return bs;
	}

	/**
	 * 
	 * @method getCurrentDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return IDialog
	 * @throws
	 * @see
	 */
	public IDialog getCurrentDialog() {
		return this.dialogStack.peek();
	}
	
	public void setCurrentDialog(IDialog dialog){
		if(this.dialogStack.contains(dialog)){
			int index = dialogStack.search(dialog);
			if(index < dialogStack.size() -1){
				dialogStack.set(dialogStack.size() -1, dialog);
			}
		}else{
			dialogStack.push(dialog);
		}
	}

	/**
	 * 
	 * @method unsetCurrentDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	public void unsetCurrentDialog(IDialog dialog){
		//최상단이 아닐경우 무시한다.
		if(dialogStack.peek() == dialog){
			//한개밖에 없을 경우에는 무시한다.
			if(dialogStack.size() == 1) return;
			dialogStack.pop();
			//2번째 다이알로그
			IDialog secondDlg = dialogStack.pop();
			//2번째가 빠지면 두번째 위치에 현재 위치를 넣는다.
			dialogStack.push(dialog);
			if(secondDlg != null){
				dialogStack.push(secondDlg);
			}
		}
	}
	
	/**
	 * 
	 * @method removeDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return void
	 * @throws
	 * @see
	 */
	public void removeDialog(IDialog dialog) {
		dialogStack.remove(dialog);
		remove(dialog);
	}

	/**
	 * 
	 * @method getDialog 
	 * @date 2014. 1. 8.
	 * @author CS.Park
	 * @param
	 * @return IDialog
	 * @throws
	 * @see
	 */
	public IDialog getDialog(String dialogId) {
        //다이알로그의 아이디를 주어지지 않았다면 최근 반환된 다이알로그를 반환합니다.
        if(dialogId == null){
        	return dialogStack.peek();
        }
        
        IDialog dialog = null;
        for(IDialog dlg : dialogStack){
            if(dialogId.equals(dlg.getId())){
            	Shell shell = dlg.getShell();
            	if(shell == null || shell.isDisposed()){
            		remove(dlg);
            	}else{
            		dialog =dlg;    
            	}
            	break;
            }
        }
        return dialog;
	}
}