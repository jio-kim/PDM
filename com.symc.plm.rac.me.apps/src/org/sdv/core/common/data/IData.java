/**
 * 
 */
package org.sdv.core.common.data;

/**
 * Class Name : IDataEntry
 * Class Description :
 * 
 * @date 2013. 10. 24.
 * 
 */
public interface IData {
	public static final int OBJECT_FIELD = 0;
    public static final int STRING_FIELD = 1;
    public static final int INTEGER_FIELD = 2;
    public static final int NUMBER_FIELD = 4;
    public static final int BOOLEAN_FIELD = 8;
    public static final int TABLE_FIELD = 16;
    public static final int LIST_FIELD = 32;

    public Object getValue();
    
    public void setValue(Object value);

    public String getStringValue();

    public String getName();

    public int getType();
}
