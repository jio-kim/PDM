/**
 * 
 */
package org.sdv.core.ui;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.sdv.core.beans.DialogStubBean;
import org.sdv.core.common.IDialog;
import org.sdv.core.common.IStubBean;
import org.sdv.core.util.SDVSpringContextUtil;


/**
 * Class Name : SimpleUIManagerImpl
 * Class Description :
 * 
 * @date 2013. 9. 24.
 * 
 */
public class SimpleUIManagerImpl extends UIManager {
    //private static final HashMap<String,UIManager> managers = new HashMap<String,UIManager>();
    private HashMap<String,IStubBean> stubBeans;
    private Layout dialgContainerLayout;
    
    public SimpleUIManagerImpl() {
        stubBeans = new HashMap<String,IStubBean>();      
    }
    
    /**
     * Create Dialog
     * 
     * @method getDialogSkeleton
     * @date 2013. 10. 2.
     * @param
     * @return AbstractSDVDialog
     * @exception
     * @throws
     * @see
     */
    @Override
    protected IDialog getDialogSkeleton(Shell shell, String dialogId) throws Exception {
        // dialog Class String을 가지고 Dialog Instance를 생성
        DialogStubBean dialogStubBean = (DialogStubBean)SDVSpringContextUtil.getBean(dialogId);
        // Dialog ID 설정
        dialogStubBean.setId(dialogId);
        String stubClassName = dialogStubBean.getImplement();
        if(stubClassName == null) {
            //stubClassName = "org.sdv.core.ui.dialog.SimpleSDVDialog";
            stubClassName = "com.symc.plm.me.sdv.dialog.sdvsample.SDVSampleDialog";
//            HashMap<String, String> configMap = (HashMap<String, String>)SpringContextUtil.getBean("ConfigrationManager");
//            stubClassName = configMap.get("DEFAULT_DIALOG_CLASS");
        }
        
        return (IDialog)getSkeltonInstance(stubClassName, shell, dialogStubBean);
    }
    
    /**
     * 
     * @method getSkeltonInstance 
     * @date 2013. 11. 14.
     * @param
     * @return IDialog
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws IllegalArgumentException 
     * @exception
     * @throws
     * @see
     */
    protected Object getSkeltonInstance(String stubClassName, Object ... objects) throws Exception {
        Class<?> cls = Class.forName(stubClassName);
        Constructor<?> ct = null;
        
        if(objects != null && objects.length > 0){
            Class<?> partypes[] = new Class[objects.length];
            for(int i=0; i < objects.length; i++){
                partypes[i] = objects[i].getClass();
            }
            ct = cls.getConstructor(partypes);
            return ct.newInstance(objects);
        }else{
            ct = cls.getConstructor();
            return ct.newInstance();
        }
    }

    protected void addStubBean(String id, IStubBean stubBean) {
        stubBeans.put(id, stubBean);
    }
    
    protected void removeStubBean(String id) {
        stubBeans.remove(id);
    }
    
    /**
     * Dialog Container의 Layout을 가지고온다.
     * 
     * @method getDialgContainerLayout
     * @date 2013. 10. 11.
     * @param
     * @return Layout
     * @exception
     * @throws
     * @see
     */
    public Layout getDialgContainerLayout() {
        return dialgContainerLayout;
    }

    /**
     * Dialog Info 에서 DialgContainer Layout Class정보를 알아내 Layout을 생성한다.
     * 
     * @method setDialgContainerLayout
     * @date 2013. 10. 11.
     * @param
     * @return Layout
     * @exception
     * @throws
     * @see
     */
    public Layout setDialgContainerLayout(Composite container) throws Exception {
        // Class uiLayoutCls = Class.forName((String) dialogInfo.get(REF_UI_DIALOG_LAYOUT_CLASS));
        // dialgContainerLayout = (Layout) uiLayoutCls.newInstance();
        // container.setLayout(dialgContainerLayout);
        return dialgContainerLayout;
    }

}
