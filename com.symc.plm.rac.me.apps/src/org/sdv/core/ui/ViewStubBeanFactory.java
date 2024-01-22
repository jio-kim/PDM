/**
 *
 */
package org.sdv.core.ui;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.widgets.Composite;
import org.sdv.core.beans.ViewPaneStubBean;
import org.sdv.core.common.IStubBean;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.IViewStubBean;
import org.sdv.core.common.IViewStubBeanFactory;
import org.sdv.core.ui.view.layout.View;
import org.sdv.core.util.SDVSpringContextUtil;

import common.Logger;

/**
 * Class Name : DefaultViewPaneFactory
 * Class Description :
 *
 * @date 2013. 9. 24.
 *
 */
public class ViewStubBeanFactory implements IViewStubBeanFactory {

    private static final Logger logger = Logger.getLogger(ViewStubBeanFactory.class);

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public static IViewPane getViewInstance(Composite parent, int style, IViewStubBean viewStub, View viewLayout ) throws Exception {
        if(viewStub == null) return null;

        Constructor<Composite> ct = null;
        Object[] args = null;


        if(viewLayout == null){
            viewLayout = new View();
            viewLayout.setId(viewStub.getId());
            viewLayout.setConfigId(IViewPane.DEFAULT_CONFIG_ID);
        }
        String viewId = viewLayout.getId();
        int configId = viewLayout.getConfigId();
        String order = viewLayout.getOrder();

        ((ViewPaneStubBean) viewStub).setId(viewId);
        ((ViewPaneStubBean) viewStub).setConfigId(configId);
        ((ViewPaneStubBean) viewStub).setOrder(order);

        try{
            String viewClassName = viewStub.getImplement();

            Class instanceViewClass = getViewClass(viewClassName);

            if(configId > 0){

                if(order == null){
                    Class[] paramTypes = new Class[4];
                    paramTypes[0] = Composite.class;
                    paramTypes[1] = int.class;
                    paramTypes[2] = String.class;
                    paramTypes[3] = int.class;

                    ct = instanceViewClass.getConstructor(paramTypes);
                    args = new Object[4];
                    args[0] = parent;
                    args[1] = style;
                    args[2] = viewId;
                    args[3] = configId;
                }else{
                    Class[] paramTypes = new Class[5];
                    paramTypes[0] = Composite.class;
                    paramTypes[1] = int.class;
                    paramTypes[2] = String.class;
                    paramTypes[3] = int.class;
                    paramTypes[4] = String.class;

                    ct = instanceViewClass.getConstructor(paramTypes);
                    args = new Object[5];
                    args[0] = parent;
                    args[1] = style;
                    args[2] = viewId;
                    args[3] = configId;
                    args[4] = order;
                }

            }else{
                //Use Default Configuration
                Class[] paramTypes = new Class[3];
                paramTypes[0] = Composite.class;
                paramTypes[1] = int.class;
                paramTypes[2] = String.class;

                ct = instanceViewClass.getConstructor(paramTypes);
                args = new Object[3];
                args[0] = parent;
                args[1] = style;
                args[2] = viewId;
            }
        }catch(Exception ex){
            logger.error(ex);
        }

        if(ct == null) throw new NoSuchMethodException("ViewPane Class is not supported Non-Default Configuration :" + configId );

        IViewPane newViewPane = (IViewPane) ct.newInstance(args);
        System.out.println(viewStub.getId() + "============================ Created\n");
        newViewPane.setStub(viewStub);
        newViewPane.setCurrentViewLayout(viewLayout);

        return newViewPane;
    }

    @SuppressWarnings({ "rawtypes" })
    protected static Class getViewClass(String viewClass) throws Exception {
        Class viewPane = null;
        viewPane = (Class) Class.forName(viewClass);
        return viewPane;
    }

    public static List<IViewPane> getStubBeanView(Composite container, Map<String, IViewStubBean> stubBeanMap) throws Exception {
        List<IViewPane> views = new ArrayList<IViewPane>();
        for (String viewId : stubBeanMap.keySet()) {
            IViewStubBean viewstub = stubBeanMap.get(viewId);
            View viewLayout = new View();
            viewLayout.setId(viewstub.getId());
            IViewPane viewPane = UIManager.getView(container, viewstub, viewLayout);
            views.add(viewPane);
        }
        return views;
    }


    public static List<IViewPane> getStubBeanView(Composite container, Map<String, IViewStubBean> stubBeanMap, Map<String, View> viewLayoutMap) throws Exception {
        List<IViewPane> views = new ArrayList<IViewPane>();
        for (String viewId : stubBeanMap.keySet()) {
            IViewStubBean viewstub = stubBeanMap.get(viewId);
            View viewLayout = null;
            if(viewLayoutMap != null && viewLayoutMap.containsKey(viewstub.getId())){
                viewLayout = viewLayoutMap.get(viewstub.getId());
            }

            IViewPane viewPane = UIManager.getView(container, viewstub, viewLayout);
            views.add(viewPane);
        }
        return views;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.intface.IViewStubBeanFactory#getViewStub(java.lang.String)
     */
    @Override
    public IStubBean getViewStub(String id) {
        return getViewStub(id, null);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.intface.IViewStubBeanFactory#getViewStub(java.lang.String, java.lang.String)
     */
    @Override
    public IStubBean getViewStub(String id, String context) {
        return (IStubBean) SDVSpringContextUtil.getBean(id, context);
    }

}
