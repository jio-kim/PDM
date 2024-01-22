/**
 * 
 */
package org.sdv.core.common.data;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Class Name : IDataEntryMap
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public interface IDataMap extends Iterable<IData> {
    public void put(IData data);

    public IData put(String name, String value);

    public IData put(String name, Object value, int type);

    public IData put(String name, IData data);
    
    public IData get(Object name);

    public List<Object> getAllValues();

    public Object getValue(String name);

    public Collection<IData> getAllDatas();

    public Set<String> getDataNames();
    
    public boolean containsKey(Object name);

    public String getStringValue(String name);

    public int  getIntValue(String name);

    public List<?> getListValue(String name);

    public List<HashMap<String, Object>> getTableValue(String name);
    
    public Object clone();

}
