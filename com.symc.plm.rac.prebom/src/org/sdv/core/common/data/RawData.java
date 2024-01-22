/**
 * 
 */
package org.sdv.core.common.data;

import org.apache.log4j.Logger;

/**
 * Class Name : RawData
 * Class Description : 
 * @date 2013. 10. 24.
 *
 */
public class RawData implements IData, Cloneable {

    private static final Logger logger = Logger.getLogger(RawData.class);
    
    protected Object value;
    protected String name;
    protected int    type = -1;
    
    public RawData(){
        
    }
    
    public RawData(String name, String value){
        this.name = name;
        this.value = value;
        this.type = IData.STRING_FIELD;
    }
    
    public RawData(String name, Object value, int type){
        this.name = name;
        this.value = value;
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IData#getValue()
     */
    @Override
    public Object getValue() {
        return this.value;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IData#setValue(java.lang.Object)
     */
    @Override
    public void setValue(Object value) {
        this.value = value;

    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IData#getStringValue()
     */
    @Override
    public String getStringValue() {
        if(this.checkType(STRING_FIELD)){
          return (String)this.value;   
        }
        return (this.value == null?null:value.toString());
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IData#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.sdv.core.common.data.IData#getType()
     */
    @Override
    public int getType() {
        return this.type;
    }
    
    protected boolean checkType(int type){
        return (this.type & type) == type;
    }
    
    public Object clone(){
        try { 
            return (RawData) super.clone();
         } catch (CloneNotSupportedException e) {
             logger.debug(e);
         }
        return new RawData(name, value, type);
    }

}
