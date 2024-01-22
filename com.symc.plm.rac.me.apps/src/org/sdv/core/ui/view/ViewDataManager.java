/**
 * 
 */
package org.sdv.core.ui.view;

import org.sdv.core.common.IViewPane;
import org.sdv.core.common.data.IDataMap;
import org.sdv.core.common.data.IDataSet;

/**
 * Class Name : ViewDataManager
 * Class Description : 
 * @date 2013. 11. 21.
 *
 */
public class ViewDataManager {

    private ViewDataManager(){
    }
    
    
    public static IDataSet addDataSet(IViewPane view, IDataSet dataset ,String viewId) {
        if(viewId == null){
        	dataset.addDataMap(view.getId(), (IDataMap)view.getLocalDataMap());
        }else if(viewId.equals(view.getId())){
        	addDataSetAll(view, dataset);
        }else{
            IViewPane viewPane = view.getView(viewId);
            if(viewPane != null){
            	dataset.addDataSet(viewPane.getDataSetAll());
            }
        }
        return dataset;
    }

    public static IDataSet addDataSetAll(IViewPane view, IDataSet dataset) {
    	dataset.addDataMap(view.getId(), (IDataMap)view.getLocalDataMap());
        if(view.getViewCount() > 0){
            for(IViewPane childView : view.getViews()){
            	dataset.addDataSet(childView.getDataSetAll());
            }
        }
        return dataset;
    }

    public static IDataSet addSelectDataSet(IViewPane view, IDataSet dataset , String viewId) {
        //현재 뷰정보를 요청하거나 빈값을 요청하면 로컬 정보를 반환한다.
        if(viewId == null){
            dataset.addDataMap(view.getId(), (IDataMap)view.getLocalSelectDataMap());
        }else if(viewId.equals(view.getId())){
        	addSelectDataSetAll(view, dataset);
        }else{
            IViewPane viewPane = view.getView(viewId);
            if(viewPane != null){
                dataset.addDataSet(viewPane.getDataSetAll());
            }
        }
        return dataset;
    }

    public static IDataSet addSelectDataSetAll(IViewPane view, IDataSet dataset) {
    	dataset.addDataMap(view.getId(), (IDataMap)view.getLocalSelectDataMap());
        if(view.getViewCount() > 0){
            for(IViewPane childView : view.getViews()){
            	dataset.addDataSet(childView.getSelectDataSetAll());
            }
        }
        return dataset;
    }

}
