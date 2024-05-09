package com.kgm.common.utils;

import java.util.ArrayList;

public class KeyValueArray
{
    private ArrayList keyArray = new ArrayList();
    private ArrayList valueArray = new ArrayList();

    public void clear()
    {
        keyArray.clear();
        valueArray.clear();
    }

    public boolean containsKey(Object key)
    {
        return keyArray.contains(key);
    }

    public boolean containsValue(Object value)
    {
        return valueArray.contains(value);
    }

    public ArrayList getKeys()
    {
        return keyArray;
    }

    public ArrayList getValues()
    {
        return valueArray;
    }

    public Object getKey(int index)
    {
        return keyArray.get(index);
    }

    public Object getKeyAtValue(Object value)
    {
        int index = valueArray.indexOf(value);
        if(index == -1)
        {
            return null;
        }
        return keyArray.get(index);
    }

    public Object getValueAtKey(Object key)
    {
        int index = keyArray.indexOf(key);
        if(index == -1)
        {
            return null;
        }
        return valueArray.get(index);
    }

    public Object getValue(int index)
    {
        return valueArray.get(index);
    }

    public void put(Object key, Object value)
    {
        keyArray.add(key);
        valueArray.add(value);
    }

    public void updateValue(Object key, Object value)
    {
    	int index = keyArray.indexOf(key);
        valueArray.set(index, value);
    }

    public void removeAtKey(Object key)
    {
        int index = keyArray.indexOf(key);
        if(index == -1)
        {
            return;
        }
        keyArray.remove(index);
        valueArray.remove(index);
    }

    public void removeAtValue(Object value)
    {
        int index = valueArray.indexOf(value);
        if(index == -1)
        {
            return;
        }
        keyArray.remove(index);
        valueArray.remove(index);
    }

    public void remove(int index)
    {
        keyArray.remove(index);
        valueArray.remove(index);
    }

    public boolean isEmpty()
    {
        return keyArray.isEmpty();
    }

    public int size()
    {
        return keyArray.size();
    }
}
