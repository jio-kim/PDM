/**
 *
 */
package org.sdv.core.common.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.sdv.core.common.message.ResultMessage;

/**
 * Class Name : IDataSet
 * Class Description :
 *
 * @date 2013. 10. 24.
 *
 */
public interface IDataSet extends Iterable<IDataMap> {

    public IDataMap getDataMap(String id);

    public Collection<IDataMap> getAllDataMaps();

    public Set<String> getDataMapIDs();

    public Object getData(String dataName);

    public int getMapCount();

    public IDataSet addDataSet(IDataSet dataSet);

    public IDataSet addDataMap(String mapId, IDataMap dataMap);

    public IDataSet removeDataMap(String mapId);

    public boolean containsKey(Object name);

    public Collection<Object> getAllValues();

    public Collection<IData> getAllDatas();

    public Set<String> getDataNames();

    public Collection<Object> getAllValues(String mapId);

    public Collection<IData> getAllDatas(String mapId);

    public Set<String> getDataNames(String mapId);

    public Object getValue(String name);

    public Object[] getValues(String name);

    public IData[] getDatas(String name);

    public Object getValue(String mapId, String name);

    public boolean containsKey(String mapId, Object name);

    public String getStringValue(String mapId, String name);

    public int  getIntValue(String mapId, String name);

    public List<?> getListValue(String mapId, String name);

    public Collection<HashMap<String, Object>> getTableValue(String mapId, String name);


    public ResultMessage getResultMessage();

    public boolean isSuccess();


    /**
     *
     * @method containsMap
     * @date 2013. 11. 21.
     * @param
     * @return boolean
     * @exception
     * @throws
     * @see
     */
    public boolean containsMap(String mapId);

}
