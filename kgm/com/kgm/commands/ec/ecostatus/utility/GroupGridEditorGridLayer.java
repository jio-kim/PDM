package com.kgm.commands.ec.ecostatus.utility;

import java.util.Collection;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupExpandCollapseLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupHeaderLayer;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupModel;
import org.eclipse.nebula.widgets.nattable.group.ColumnGroupReorderLayer;
import org.eclipse.nebula.widgets.nattable.hideshow.ColumnHideShowLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.ColumnReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;

public class GroupGridEditorGridLayer<V> extends GridLayer
{
    protected EventList<V> tableDataList;
    protected GroupEditorBodyLayerStack<V> bodyLayer;
    protected ColumnGroupHeaderLayer columnGroupHeaderLayer;
    protected DataLayer columnHeaderDataLayer ;

    public GroupGridEditorGridLayer(Collection<V> valuesToShow, ConfigRegistry configRegistry, final String[] propertyNames,
            Map<String, String> propertyToLabelMap)
    {
        super(true);
        init(valuesToShow, configRegistry, propertyNames, propertyToLabelMap);
    }

    protected void init(Collection<V> valuesToShow, ConfigRegistry configRegistry, final String[] propertyNames, Map<String, String> propertyToLabelMap)
    {

        IColumnPropertyAccessor<V> accessor = new ReflectiveColumnPropertyAccessor<V>(propertyNames);
        bodyLayer = new GroupEditorBodyLayerStack<V>(valuesToShow, accessor, configRegistry);
        tableDataList = bodyLayer.getEventList();
        SelectionLayer selectionLayer = bodyLayer.getSelectionLayer();

        IDataProvider columnHeaderDataProvider = new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap);
        columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
        ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer.getViewportLayer(), selectionLayer);

        SortHeaderLayer<Object> sortHeaderLayer = new SortHeaderLayer<Object>(columnHeaderLayer, new GlazedListsSortModel<V>(
                (SortedList<V>) bodyLayer.getSortedList(), accessor, configRegistry, columnHeaderDataLayer));

        columnGroupHeaderLayer = new ColumnGroupHeaderLayer(sortHeaderLayer, bodyLayer.getSelectionLayer(), bodyLayer.getColumnGroupModel());

        // Default Row Header 설정
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyLayer.getDataLayer().getDataProvider());
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, selectionLayer);

        // Default Corner 설정
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnGroupHeaderLayer);

        setBodyLayer(bodyLayer);
        setColumnHeaderLayer(columnGroupHeaderLayer);
        setRowHeaderLayer(rowHeaderLayer);
        setCornerLayer(cornerLayer);

        // Column Header Label Configure 설정
        columnHeaderDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
    }

    /**
     * Table Data 리스트를 가져옴
     * 
     * @return the tableDataList
     */
    public EventList<V> getTableDataList()
    {
        return tableDataList;
    }

    public GroupEditorBodyLayerStack<V> getBodyLayer()
    {
        return bodyLayer;
    }

    public ColumnGroupHeaderLayer getColumnGroupHeader()
    {
        return columnGroupHeaderLayer;
    }
    
    public DataLayer getColumnHeaderDataLayer()
    {
    	return columnHeaderDataLayer;
    }

    /**
     * Body Layer Stack
     * 
     * @author Administrator
     * 
     */
    static public class GroupEditorBodyLayerStack<V> extends AbstractLayerTransform
    {
        private DataLayer bodyDataLayer;
        private SelectionLayer selectionLayer;
        private SortedList<V> sortedList;
        private ViewportLayer viewportLayer;
        private EventList<V> tableDataList;
        private ColumnGroupModel columnGroupModel;

        public GroupEditorBodyLayerStack(Collection<V> valuesToShow, IColumnPropertyAccessor<V> accessor, ConfigRegistry configRegistry)
        {
            tableDataList = GlazedLists.eventList(valuesToShow);
            // Column header
            columnGroupModel = new ColumnGroupModel();
            // Sort 처리
            sortedList = new SortedList<V>(tableDataList, null);
            IDataProvider bodyDataProvider = new ListDataProvider<V>(sortedList, accessor);
            bodyDataLayer = new DataLayer(bodyDataProvider);
            GlazedListsEventLayer<V> eventLayer = new GlazedListsEventLayer<V>(bodyDataLayer, sortedList);
            // Layer 설정
            ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer(eventLayer);
            ColumnGroupReorderLayer columnGroupReorderLayer = new ColumnGroupReorderLayer(columnReorderLayer, columnGroupModel);
            ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer(columnGroupReorderLayer);
            ColumnGroupExpandCollapseLayer columnGroupExpandCollapseLayer = new ColumnGroupExpandCollapseLayer(columnHideShowLayer, columnGroupModel);
            selectionLayer = new SelectionLayer(columnGroupExpandCollapseLayer);
            viewportLayer = new ViewportLayer(selectionLayer);
            setUnderlyingLayer(viewportLayer);
        }

        public DataLayer getDataLayer()
        {
            return this.bodyDataLayer;
        }

        public SelectionLayer getSelectionLayer()
        {
            return selectionLayer;
        }

        public ViewportLayer getViewportLayer()
        {
            return viewportLayer;
        }

        public EventList<V> getEventList()
        {
            return tableDataList;
        }

        public SortedList<V> getSortedList()
        {
            return sortedList;
        }

        public ColumnGroupModel getColumnGroupModel()
        {
            return columnGroupModel;
        }

    }

}