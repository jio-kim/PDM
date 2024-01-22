/**
 * 
 */
package org.sdv.core.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * Class Name : RawDataMap
 * Class Description : 
 * @date 2013. 10. 24.
 *
 */
public class RawDataMap extends LinkedHashMap<String, IData> implements IDataMap {

    /**
     * 
     */
    private static final long serialVersionUID = -4495880372341512317L;

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#put(org.sdv.core.common.data.IData)
     */
    @Override
    public void put(IData data) {
        this.put(data.getName(), data);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#put(java.lang.String, java.lang.String)
     */
    @Override
    public IData put(String name, String value) {
        return this.put(name, new RawData(name, value, IData.STRING_FIELD));
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#put(java.lang.String, java.lang.Object, int)
     */
    @Override
    public IData put(String name, Object value, int type) {
        return this.put(name, new RawData(name, value, type));
    }


    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getAllValues()
     */
    @Override
    public List<Object> getAllValues() {
        List<Object> values = new ArrayList<Object>();
        for(IData rawdata: getAllDatas()){
            values.add(rawdata.getValue());
        }
        return values;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getValue(java.lang.String)
     */
    @Override
    public Object getValue(String name) {
        if(containsKey(name)){
            return get(name).getValue();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getAllDatas()
     */
    @Override
    public Collection<IData> getAllDatas() {
        return this.values();
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getDataNames()
     */
    @Override
    public Set<String> getDataNames() {
        return this.keySet();
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getStringValue(java.lang.String)
     */
    @Override
    public String getStringValue(String name) {
        if(containsKey(name)){
            return get(name).getStringValue();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getIntValue(java.lang.String)
     */
    @Override
    public int getIntValue(String name) {
        if(containsKey(name)){
            IData data = get(name);
            if(data != null && data.getType() == IData.INTEGER_FIELD){
                return ((Integer)data.getValue()).intValue();
            }
            throw new IllegalDataTypeException(name + " is not int type data.");
        }
        throw new NullPointerException("Not conained data :" + name);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getListValue(java.lang.String)
     */
    @Override
    public List<?> getListValue(String name) {
        if(containsKey(name)){
            IData data = get(name);
            if(data != null && data.getType() == IData.LIST_FIELD){
                return (List<?>)data.getValue();
            }
            throw new IllegalDataTypeException(name + " is not List type data.");
        }
        throw new NullPointerException("Not conained data :" + name);
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IDataMap#getTableValue(java.lang.String)
     */
    @SuppressWarnings("unchecked")
    @Override
    public List<HashMap<String, Object>> getTableValue(String name) {
        if(containsKey(name)){
            IData data = get(name);
            if(data != null && data.getType() == IData.TABLE_FIELD){
                return (List<HashMap<String, Object>>)data.getValue();
            }
            throw new IllegalDataTypeException(name + " is not Table type data.");
        }
        throw new NullPointerException("Not conained data :" + name);
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IData> iterator() {
        return this.values().iterator();
    }

}
