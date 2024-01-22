/**
 * 
 */
package org.sdv.core.ui;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.IViewStubBean;
import org.sdv.core.ui.view.layout.View;
import org.sdv.core.util.SDVSpringContextUtil;

/**
 * Class Name : UIManager
 * Class Description :
 * 
 * @date 2013. 9. 23.
 * 
 */
public abstract class UIManager {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(UIManager.class);
    
    public static final String UIMANAGER_BEAN_NAME = "UIManager";

    public static final String REF_UI_MANAGER_CLASS = "uiManagerClass";
    
    public static final String REF_UI_DIALOG_LAYOUT_CLASS = "dialogLayoutClass";

    
    private static UIManager uiManagerImpl = null;
    
    
    /**
     * UIManager가 생성한 다이알로그를 관리하고 보관하는 Map Container;
     */
    private DialogManager dialogManager;

//    
//    private IDialog currentDialog;
    
    /**
     * Spring Bean에서 UIManager를 가져온다.
     * 
     * @method getUIManagerImpl
     * @date 2013. 10. 14.
     * @param
     * @return UIManager
     * @exception
     * @throws
     * @see
     */
    protected static UIManager getUIManagerImpl(){
        if(uiManagerImpl == null){
            uiManagerImpl = (UIManager)SDVSpringContextUtil.getBean(UIMANAGER_BEAN_NAME);
        }
        
        if(uiManagerImpl ==  null){
            throw new IllegalStateException("Can not instantiation for implemented UIManager Class!!!");
        }
        
        return uiManagerImpl;
    }
    
    /**
     * dialogId를 가지고 Dialog를 생성한다.
     * 
     * @method getDialog
     * @date 2013. 10. 2.
     * @param
     * @return AbstractSDVDialog
     * @exception
     * @throws
     * @see
     */
    public static IDialog getDialog(Shell shell, String dialogId) throws Exception {
        return (IDialog)getUIManagerImpl().getDialogImpl(shell, dialogId);
    }
    
    public static IDialog getAvailableDialog(String dialogId){
        return (IDialog)getUIManagerImpl().getDialog(dialogId);
    }

    public static IDialog getCurrentDialog(){
        return (IDialog)getUIManagerImpl().getDialog();
    }

    /**
     * 
     * @method getView
     * @date 2013. 10. 14.
     * @param
     * @return IViewPane
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static IViewPane getView(Composite container, IViewStubBean viewstub, View layoutView) throws Exception {
        return ViewStubBeanFactory.getViewInstance(container, SWT.NONE, viewstub, layoutView);
    }    
    
    /**
     * 
     * @method getView
     * @date 2013. 10. 14.
     * @param
     * @return IViewPane
     * @throws Exception
     * @exception
     * @throws
     * @see
     */
    public static IViewPane getView(String parentId, String viewId) throws Exception {
        return getUIManagerImpl().getViewImpl(parentId, viewId);
    }  
    
   
    /**
     * UIManager Constructor realize 하여 implement 한 클래스에서만 접근 가능
     */
    protected UIManager() {
    	dialogManager = new DialogManager(this);
    }
    

    /**
     * @return the getDialogSkeleton
     */
    protected abstract IDialog getDialogSkeleton(Shell shell, String Id) throws Exception;
    
    /**
     * 
     * @method getCurrentDialog 
     * @date 2013. 11. 14.
     * @param
     * @return IDialog
     * @exception
     * @throws
     * @see
     */
    public IDialog getDialog() {
        return getDialog(null);
    }    
   
    /**
     * 보관하고 있는 다이알로그 리스트 중에서 주어진 아이디의 다이알로그를 찾아서 반환합니다.
     * 
     * @method getDialog 
     * @date 2013. 11. 14.
     * @param
     * @return IDialog
     * @exception
     * @throws
     * @see
     */
    public IDialog getDialog(String dialogId){
    	return dialogManager.getDialog(dialogId);
    }
    
    /**
     * 다이알로그를 반환 메서드 구현
     * 
     * @method getDialogImpl 
     * @date 2013. 11. 14.
     * @param
     * @return IDialog
     * @exception
     * @throws
     * @see
     */
    protected IDialog getDialogImpl(Shell newShell, String dialogId) throws Exception {
        
        IDialog dialog = getDialog(dialogId);
        
        //관리되고 있는 다이알로그가 없다면 새롭게 다이알로그를 생성하여 다이알로그 컨테이너에 등록후  반환한다,
        if(dialog == null){
            dialog = (IDialog) getDialogSkeleton(newShell, dialogId);
            //생성한 다이알로그를 관리하기 위해 저장함.
            addDialog(dialogId, dialog);            
        }
        return dialog;
    }
    
    
    protected IViewPane getViewImpl(String parentId, String viewId){
        IViewPane viewPane = null;
        
        if(dialogManager.getWindowCount() == 0) return viewPane;
        try{
        for(Window window : dialogManager.getWindows()){
           if(window instanceof IDialog){
               viewPane = getChildView((IViewPane)window, parentId, viewId);
               if(viewPane != null) break;
           }
        }
        }catch(IllegalStateException ex){
          //부모아이디는 찾았으나 해당 부모 뷰 하위에서 자식 뷰를 찾지 못한 경우에 오류를 발생시킨다.
        }
        return viewPane;
    }
    
    protected IViewPane getChildView(IViewPane parentView, String parentId, String viewId){
        
        IViewPane view = null;
        if(parentView == null || viewId == null ) return view;
        //주어진 뷰개체가 찾는 뷰의 부모아이디와 같은지 비교한다.  다를지라도 자식들까지 내려가 찾는다.
        boolean isParent = (parentView.getId() != null && parentView.getId().equals(parentId));
        
        //주어진 부모와 대상 뷰의 아이디가 같은 것이 주어질 경우에는 자식이 아닌 해당 View를 반환한다. 
        if(isParent && parentId == viewId ) return parentView;
        
        for(IViewPane childView : parentView.getViews()){
            //자식중에서 같은 것이 있는지 찾는다.
            if(childView != null && childView.getId() != null && childView.getId().equals(viewId)){
                view = childView;
                break;
            }
            //자식 뷰에서 찾지못하면 다시 더 하위로 내려가 찾는다.
            view = getChildView(childView, parentId, viewId);
            if(view != null)break;
        }
        //부모아이디는 맞으나  자식뷰 중에서 해당 뷰가 없는경우에는  오류를 발생하여 전체적으로 찾는 작업을 중단시킨다.
        if(isParent && view == null) throw new IllegalStateException();
        return view;
    }
    
    /*#########################################################################################
     * 
     * DialogListenr Implement Method
     * 
     * ########################################################################################
     */
    protected IDialog addDialog(String dialogId, IDialog dialog) {
        if(dialog == null) return null;
        dialogManager.add((Dialog)dialog);
        return dialog;
    }

}
