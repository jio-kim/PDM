/**
 *
 */
package org.sdv.core.ui;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.sdv.core.common.ILayoutManager;
import org.sdv.core.common.IViewPane;
import org.sdv.core.common.IViewStubBean;
import org.sdv.core.ui.view.layout.BorderLayoutViewPane;
import org.sdv.core.ui.view.layout.FillLayoutViewPane;
import org.sdv.core.ui.view.layout.GridLayoutViewPane;
import org.sdv.core.ui.view.layout.Layout;
import org.sdv.core.ui.view.layout.View;
import org.sdv.core.util.UIUtil;

import swing2swt.layout.BorderLayout;

import common.Logger;

/**
 * Class Name : SDVUILayoutManager
 * Class Description :
 *
 * @date 2013. 10. 15.
 *
 */
public class SDVUILayoutManager implements ILayoutManager {

    private static final Logger logger = Logger.getLogger(SDVUILayoutManager.class);

    public static final String BORDER_LAYOUT = "borderLayoutView";
    public static final String FILL_LAYOUT = "fillLayoutView";
    public static final String SPLIT_LAYOUT = "splitLayout";
    public static final String TAB_LAYOUT = "tabLayout";
    public static final String GRID_LAYOUT = "gridLayoutView";

    public static final String NORTH = "NORTH";
    public static final String EAST = "EAST";
    public static final String WEST = "WEST";
    public static final String CENTER = "CENTER";
    public static final String SOUTH = "SOUTH";
    
    public static final String FILL_HORIZONTAL = "HORIZONTAL";
    public static final String FILL_VERTICAL = "VERTICAL";
    public static final String FILL_BOTH = "BOTH";
    public static final String FILL_BEGINNING = "BEGINNING";
    public static final String FILL_CENTER = "CENTER";
    
    private static JAXBContext jaxbContext;

    /**
     * DialogStubBean 에서 정의된 layout xml을 Unmarshaller 한다.
     *
     * @method getLayoutXml
     * @date 2013. 10. 16.
     * @param
     * @return Layout
     * @exception
     * @throws
     * @see
     */
    public static Layout getLayoutXml(String xml) throws Exception {
        
        if(jaxbContext == null){
            jaxbContext = JAXBContext.newInstance(Layout.class);
        }
        
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        Layout parsedLayout = (Layout)unmarshaller.unmarshal(new StringReader(xml));
        
//        // marshal to System.out
//        Marshaller marshaller = jaxbContext.createMarshaller();
//        marshaller.marshal( parsedLayout, System.out );
        
        return parsedLayout;
        
    }

    /**
     * Dialog에 추가할 View Stub을 이용해 ViewPane을 생성한다.
     *
     * @method createDialogLayout
     * @date 2013. 10. 16.
     * @param
     * @return HashMap<String,IViewPane>
     * @exception
     * @throws
     * @see
     */
    public static List<IViewPane> createViewPaneLayout(HashMap<String, Composite> viewContainerMap, IViewStubBean viewStubBean) throws Exception {
        return createViewPaneLayout(viewContainerMap, viewStubBean, null);
    }

    public static List<IViewPane> createViewPaneLayout(HashMap<String, Composite> viewContainerMap, IViewStubBean viewStubBean, Map<String, Object> parameter) throws Exception {
        String layoutXml =viewStubBean.getLayoutXml();
        if(layoutXml == null) return null;
        Layout rootLayout = getLayoutXml(layoutXml.trim());
        return createLayoutCompenet(viewStubBean.getViews(), viewContainerMap, rootLayout, parameter);
    }


    /**
     * LayoutPane생성 및 ViewPane 생성
     *
     * @method createLayoutCompenet
     * @date 2013. 10. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    @SuppressWarnings("unchecked")
    private static List<IViewPane> createLayoutCompenet(Map<String , IViewStubBean> viewStubMap, HashMap<String, Composite> viewContainerMap, Layout presentLayout, Map<String, Object> parameters) throws Exception {
        List<IViewPane> viewList = new ArrayList<IViewPane>();
        //부모 뷰에 레이아웃에 정의된 뷰가 있는지를 확인한다. 정의되지 않은 뷰가 있다면 그리지 않고 무시한다.                
        if(viewStubMap == null || viewStubMap.size() == 0 || viewContainerMap == null || viewContainerMap.size() == 0) return viewList;

        //최상위 레이아웃은 부모가 전달해준 VIEW CONTAINER에서 기본 컨테이너에 넣는다.
        Composite parent = viewContainerMap.get(IViewPane.DEFAULT_VIEW_CONTAINER_ID);
        
        //만약 하위에 아무것도 삽입된 뷰가 없다면 레이아웃을 부모로 부터 제거한다.
        Composite presentLayoutComposite = getLayoutCompsite(parent, presentLayout);

        //먼저 뷰 리스트를 가져와 부모에 넣는다.
        List<View> childViews = presentLayout.getViews();
        
        if (childViews != null){
            for (View view : childViews){
                String viewId = view.getId();
                logger.debug("[LAYOUT - ViewStub] View ID = " + viewId);

                //레이아웃에 정의된 뷰가  ViewStub의 View에 정의된 뷰가 아니라면 무시한다.
                if(!viewStubMap.containsKey(viewId)) continue;
                
                //뷰생성 정보를 가진 뷰 스텁을 가져온다.
                IViewStubBean viewstub = viewStubMap.get(viewId);
                
                Composite container = presentLayoutComposite;
                //뷰가 담길 뷰컨테이너가 정의되었는지를 확인한다. 뷰가 들어갈 곳이 정의되었다면 해당 뷰 컨테이너에 넣는다.
                if(viewContainerMap.containsKey(viewId)){
                    Composite parentContainer = viewContainerMap.get(viewId);
                    container = getLayoutCompsite(parentContainer, presentLayout);
                }
                
                //뷰인스턴스를 생성한다.
                IViewPane viewPane = UIManager.getView(container, viewstub, view);
                if(viewPane == null) continue;
                
                //뷰가 부모에 삽입될 위치를 정의한다. (주어진 레이아웃이 부모의 레이아웃과 다르면 부모의 레이아웃을 따른다.
                setLayoutData(presentLayout, container, (Composite)viewPane, view.getOrder());

                //View들을 생성한 후에 초기 파라메터를 전달한다.
                if(parameters != null){
                    viewPane.setParameters(parameters);
                }
                viewList.add(viewPane);
            }
        }

        List<Layout> childLayouts = presentLayout.getLayouts();
        
        if (childLayouts != null) {
            for (Layout layout : childLayouts) {
                String layoutId = layout.getId();
                logger.debug("[LAYOUT- Layout ] Layout  ID = " + layoutId);
                
                //뷰가 담길 뷰컨테이너가 정의되었는지를 확인한다. 뷰가 들어갈 곳이 정의되었다면 해당 뷰 컨테이너에 넣는다.
                Composite container = presentLayoutComposite;
                if(viewContainerMap.containsKey(layout.getId())){
                    container = viewContainerMap.get(layout.getId());
                }
                Composite childLayoutComposite = getLayoutCompsite(container, presentLayout);
                
                //레이아웃을 정의하였다면 현재 하위의 레이아웃을 그리기 위해 DEFAULT_VIEW_CONTAINER_ID를 자기자신으로 변경한다.
                HashMap <String, Composite> cloneMap = (HashMap<String, Composite>)viewContainerMap.clone();
                cloneMap.put(IViewPane.DEFAULT_VIEW_CONTAINER_ID, childLayoutComposite);
                viewList.addAll(createLayoutCompenet(viewStubMap, cloneMap, layout, parameters));
            }
        }
        if(presentLayoutComposite != null && presentLayoutComposite.getChildren() == null || presentLayoutComposite.getChildren().length == 0){
            presentLayoutComposite.dispose();
        }
        return viewList;
    }

    /**
     * Layout을 적용한 ViewPane 생성
     *
     * @method getLayoutCompsite
     * @date 2013. 10. 16.
     * @param
     * @return IViewPane
     * @exception
     * @throws
     * @see
     */
    private static Composite getLayoutCompsite(Composite parent, Layout layoutInfo) {
        Composite layoutView = null;
        if (BORDER_LAYOUT.equals(layoutInfo.getType())) {
            layoutView = new BorderLayoutViewPane(parent, SWT.NONE, StringUtils.isEmpty(layoutInfo.getId()) ? UIUtil.getGenerateViewId(BORDER_LAYOUT) : layoutInfo.getId());
        } else if (GRID_LAYOUT.equals(layoutInfo.getType())) {
            layoutView = new GridLayoutViewPane(parent, SWT.NONE, StringUtils.isEmpty(layoutInfo.getId()) ? UIUtil.getGenerateViewId(GRID_LAYOUT) : layoutInfo.getId());
        } else {
            layoutView = new FillLayoutViewPane(parent, SWT.NONE, StringUtils.isEmpty(layoutInfo.getId()) ? UIUtil.getGenerateViewId(FILL_LAYOUT) : layoutInfo.getId());
        }
        
        if(UIUtil.isUseRandomBackground()){
            layoutView.setBackground(UIUtil.getRandomColor());
        }          
        
        if(UIUtil.isUseLayoutMargin()){
            org.eclipse.swt.widgets.Layout layout = layoutView.getLayout();
            if(layout instanceof BorderLayout){
                BorderLayout borderLayout = (BorderLayout)layoutView.getLayout();
                borderLayout.setHgap(5);
                borderLayout.setVgap(5);
            }else if(layout instanceof GridLayout){
                GridLayout gridLayout = (GridLayout)layoutView.getLayout();
                gridLayout.marginTop = 5;
                gridLayout.marginLeft = 5;
                gridLayout.marginBottom = 5;
                gridLayout.marginRight = 5;
                gridLayout.horizontalSpacing =2;
                gridLayout.horizontalSpacing =2;
            }else if(layout instanceof FillLayout){
                FillLayout fillLayout = (FillLayout)layoutView.getLayout();
                fillLayout.marginHeight = 5;
                fillLayout.marginWidth = 5;
                fillLayout.spacing = 0;
            }else{
                
            }
        }
        setLayoutData(layoutInfo, parent, layoutView, layoutInfo.getOrder());
        return layoutView;
        
    }

    /**
     * LayoutData set..
     *
     * @method setLayoutData
     * @date 2013. 10. 16.
     * @param
     * @return void
     * @exception
     * @throws
     * @see
     */
    private static void setLayoutData(Layout layout,Composite parentComposite,  Composite layoutComposite, String order) {

        String parentLayoutType = getLayoutType(parentComposite);

        //실제 부모 판넬의 레이아웃과 지정된 레이아웃이 다르면 레이아웃데이터를 설정하지 않는다.
        if(!parentLayoutType.equals(layout.getType())){
            logger.debug(" [Parent Layout = " + parentLayoutType + ", Defined Layout = " + layout.getType() );
            return;
        }
        
        if (BORDER_LAYOUT.equals(layout.getType())) {
            if (NORTH.equals(order)) {
                layoutComposite.setLayoutData(swing2swt.layout.BorderLayout.NORTH);
            } else if (EAST.equals(order)) {
                layoutComposite.setLayoutData(swing2swt.layout.BorderLayout.EAST);
            } else if (WEST.equals(order)) {
                layoutComposite.setLayoutData(swing2swt.layout.BorderLayout.WEST);
            } else if (CENTER.equals(order)) {
                layoutComposite.setLayoutData(swing2swt.layout.BorderLayout.CENTER);
            } else if (SOUTH.equals(order)) {
                layoutComposite.setLayoutData(swing2swt.layout.BorderLayout.SOUTH);
            }
        } else if (FILL_LAYOUT.equals(layout.getType())) {

        } else if (GRID_LAYOUT.equals(layout.getType())) {
            if (FILL_HORIZONTAL.equals(order)) {
                layoutComposite.setLayoutData(GridData.FILL_HORIZONTAL);
            } else if (FILL_VERTICAL.equals(order)) {
                layoutComposite.setLayoutData(GridData.FILL_VERTICAL);
            } else if (FILL_BOTH.equals(order)) {
                layoutComposite.setLayoutData(GridData.FILL_BOTH);
            } else if (FILL_CENTER.equals(order)) {
                layoutComposite.setLayoutData(GridData.CENTER);
            } else if (FILL_BEGINNING.equals(order)) {
                layoutComposite.setLayoutData(GridData.BEGINNING);
            }else{
                layoutComposite.setLayoutData(GridData.FILL_HORIZONTAL);
            }
        }
    }

    /**
     * 
     * @method getLayoutType 
     * @date 2013. 12. 4.
     * @author CS.Park
     * @param
     * @return String
     * @throws
     * @see
     */
    private static String getLayoutType(Composite composite) {
        org.eclipse.swt.widgets.Layout compLayout = composite.getLayout();
        if(compLayout instanceof BorderLayout){
            return BORDER_LAYOUT;
        }else if(compLayout instanceof FillLayout){
            return FILL_LAYOUT;
        }else if(compLayout instanceof GridLayout){
            return GRID_LAYOUT;
        }else{
            return FILL_LAYOUT;
        }
    }
}
