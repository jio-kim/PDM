/**
 *
 */
package org.sdv.core.common.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

import org.sdv.core.common.message.ResultMessage;

import common.Logger;

/**
 * Class Name : DataSet
 * Class Description :
 *
 * @date 2013. 10. 24.
 *
 */
public class DataSet implements IDataSet {

    private static final Logger logger = Logger.getLogger(DataSet.class);

    private LinkedHashMap<String, IDataMap> dataMaps;
    private ResultMessage message;

    public DataSet() {
        dataMaps = new LinkedHashMap<String, IDataMap>();
    }

    public DataSet(String id, IDataMap map) {
        this();
        addDataMap(id, map);
    }

    public DataSet(IDataSet dataSet) {
        this();
        addDataSet(dataSet);
    }

    @Override
    public Object getData(String dataName) {
        for (IDataMap dataMap : getAllDataMaps()) {
            if (dataMap.containsKey(dataName)) {
                return dataMap.getValue(dataName);
            }
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#getAllDataMaps()
     */
    @Override
    public Collection<IDataMap> getAllDataMaps() {

        return this.dataMaps.values();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#getDataMapIDs()
     */
    @Override
    public Set<String> getDataMapIDs() {
        return this.dataMaps.keySet();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#getDataMap(java.lang.String)
     */
    @Override
    public IDataMap getDataMap(String id) {
        if (id != null && this.dataMaps.containsKey(id)) {
            return this.dataMaps.get(id);
        }
        return null;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#addDataSet(org.sdv.core.common.data.IDataSet)
     */
    @Override
    public IDataSet addDataSet(IDataSet dataSet) {
        if (dataSet != null) {
            for (String dataMapId : dataSet.getDataMapIDs()) {
                try {
                    addDataMap(dataMapId, (IDataMap) dataSet.getDataMap(dataMapId).clone());
                } catch (Exception ex) {
                    logger.debug(ex);
                    addDataMap(dataMapId, dataSet.getDataMap(dataMapId));
                }
            }
        }
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#addDataMap(java.lang.String, org.sdv.core.common.data.IDataMap)
     */
    @Override
    public IDataSet addDataMap(String id, IDataMap dataMap) {
        if (id != null && dataMap != null) {
            this.dataMaps.put(id, dataMap);
        }
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#removeDataMap(java.lang.String)
     */
    @Override
    public IDataSet removeDataMap(String id) {
        this.dataMaps.remove(id);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#getResultMessage()
     */
    @Override
    public ResultMessage getResultMessage() {
        return message;
    }

    public void setResultMessage(ResultMessage message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return (this.message != null && this.message.isSuccess());
    }

    /*
     * (non-Javadoc)
     *
     * @see org.sdv.core.common.data.IDataSet#containsMap(java.lang.String)
     */
    @Override
    public boolean containsMap(String id) {
        return this.dataMaps.containsKey(id);
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getAllValues()
     */
    @Override
    public Collection<Object> getAllValues() {
        Collection<Object> allValues = new ArrayList<Object>();
        for (IDataMap datamap : dataMaps.values()) {
            allValues.addAll(datamap.getAllValues());
        }
        return allValues;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getAllDatas()
     */
    @Override
    public Collection<IData> getAllDatas() {
        Collection<IData> allValues = new ArrayList<IData>();
        for (IDataMap datamap : dataMaps.values()) {
            allValues.addAll(datamap.getAllDatas());
        }
        return allValues;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getDataNames()
     */
    @Override
    public Set<String> getDataNames() {
        Set<String> dataNames = new HashSet<String>();
        for (IDataMap datamap : dataMaps.values()) {
            dataNames.addAll(datamap.getDataNames());
        }
        return dataNames;
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(Object name) {
        for (IDataMap datamap : dataMaps.values()) {
            if(datamap.containsKey(name)) return true;
        }
        return false;
    }



    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getAllValues(java.lang.String)
     */
    @Override
    public Collection<Object> getAllValues(String mapId) {
        if (!dataMaps.containsKey(mapId))
            return null;
        return dataMaps.get(mapId).getAllValues();

    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getAllDatas(java.lang.String)
     */
    @Override
    public Collection<IData> getAllDatas(String mapId) {
        if (!dataMaps.containsKey(mapId))
            return null;
        return dataMaps.get(mapId).getAllDatas();
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getDataNames(java.lang.String)
     */
    @Override
    public Set<String> getDataNames(String mapId) {
        if (!dataMaps.containsKey(mapId))
            return null;
        return dataMaps.get(mapId).getDataNames();
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getValue(java.lang.String)
     */
    @Override
    public Object getValue(String name) {
        try {
            for (IDataMap datamap : dataMaps.values()) {
                if (datamap.containsKey(name)) {
                    return datamap.getValue(name);
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return null;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getValues(java.lang.String)
     */
    @Override
    public Object[] getValues(String name) {

        List<Object> values = new ArrayList<Object>();
        try {
            for (IDataMap datamap : dataMaps.values()) {
                if (datamap.containsKey(name)) {
                    values.add(datamap.getValue(name));
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return values.toArray();

    }


    /**
     * Description :
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getDatas(java.lang.String)
     */
    @Override
    public IData[] getDatas(String name) {
        List<IData> values = new ArrayList<IData>();
        try {
            for (IDataMap datamap : dataMaps.values()) {
                if (datamap.containsKey(name)) {
                    values.add(datamap.get(name));
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return values.toArray(new IData[values.size()]);
    }


    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getValue(java.lang.String, java.lang.String)
     */
    @Override
    public Object getValue(String mapId, String name) {
        try {
            if(this.dataMaps.containsKey(mapId)){
                if (this.dataMaps.get(mapId).containsKey(name)) {
                    return this.dataMaps.get(mapId).getValue(name);
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return null;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#containsKey(java.lang.String, java.lang.Object)
     */
    @Override
    public boolean containsKey(String mapId, Object name) {
        try {
            if(this.dataMaps.containsKey(mapId)){
                return this.dataMaps.get(mapId).containsKey(name);
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return false;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getStringValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getStringValue(String mapId, String name) {
        try {
            if(this.dataMaps.containsKey(mapId)){
                if (this.dataMaps.get(mapId).containsKey(name)) {
                    return this.dataMaps.get(mapId).getStringValue(name);
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return null;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getIntValue(java.lang.String, java.lang.String)
     */
    @Override
    public int getIntValue(String mapId, String name) {
        try {
            if(this.dataMaps.containsKey(mapId)){
                if (this.dataMaps.get(mapId).containsKey(name)) {
                    return this.dataMaps.get(mapId).getIntValue(name);
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
            throw idtex;
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        //null 일 경우 -1을 반환
        return -1;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getListValue(java.lang.String, java.lang.String)
     */
    @Override
    public List<?> getListValue(String mapId, String name) {
        try {
            if(this.dataMaps.containsKey(mapId)){
                if (this.dataMaps.get(mapId).containsKey(name)) {
                    return this.dataMaps.get(mapId).getListValue(name);
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return null;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see org.sdv.core.common.data.IDataSet#getTableValue(java.lang.String, java.lang.String)
     */
    @Override
    public Collection<HashMap<String, Object>> getTableValue(String mapId, String name) {
        try {
            if(this.dataMaps.containsKey(mapId)){
                if (this.dataMaps.get(mapId).containsKey(name)) {
                    return this.dataMaps.get(mapId).getTableValue(name);
                }
            }
        } catch (IllegalDataTypeException idtex) {
            logger.error(idtex);
        } catch (NullPointerException nex) {
            logger.error(nex);
        }
        return null;
    }

    /**
     * Description :
     *
     * @method :
     * @date : 2013. 11. 26.
     * @author : CS.Park
     * @param :
     * @return :
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<IDataMap> iterator() {
        return this.dataMaps.values().iterator();
    }

    @Override
    public int getMapCount() {
        return this.dataMaps.size();
    }

}
