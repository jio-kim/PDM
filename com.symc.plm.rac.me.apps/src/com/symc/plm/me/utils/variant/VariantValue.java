package com.symc.plm.me.utils.variant;

public class VariantValue implements Comparable<VariantValue>{
    
    private VariantOption option;
    private String valueName;
    private String valueDesc;
    private boolean isNew;
    private boolean isUsing;
    
    private int valueStatus = 0;
    private int initialStatus = 0;
    
    public static int VALUE_USE = 100;
    public static int VALUE_NOT_USE = 200;
    public static int VALUE_NOT_DEFINE = 300;
    public static String TC_MESSAGE_NOT_USE = "[TC_MESSAGE_NOT_USE]";
    public static String TC_MESSAGE_NOT_DEFINE = "[TC_MESSAGE_NOT_DEFINE]";
    
    
    public VariantValue(VariantOption option, String valueName, String valueDesc, int valueStatus, boolean isNew){
        this.option = option;
        this.valueName = valueName;
        this.valueDesc = valueDesc;
        this.valueStatus = valueStatus;
        this.initialStatus = valueStatus;
        this.isNew = isNew;
        this.isUsing = true;
    }
    
    public boolean isUsing() {
        return isUsing;
    }

    public void setUsing(boolean isUsing) {
        this.isUsing = isUsing;
    }

    public VariantOption getOption() {
        return option;
    }

    public void setOption(VariantOption option) {
        this.option = option;
    }

    public String getValueName() {
        return valueName;
    }
    public void setValueName(String valueName) {
        this.valueName = valueName;
    }
    public String getValueDesc() {
        return valueDesc;
    }
    public void setValueDesc(String valueDesc) {
        this.valueDesc = valueDesc;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public int getValueStatus() {
        return valueStatus;
    }

    public void setValueStatus(int valueStatus) {
        this.valueStatus = valueStatus;
    }

    @Override
    public boolean equals(Object obj) {
        if( obj instanceof VariantValue){
            VariantValue value = (VariantValue)obj;
            
            if( option == null){
                if(valueName.equals(value.getValueName())) 
                    return true;
                else{
                    return false;
                }
            }else{
                if(valueName.equals(value.getValueName()) && option.getOptionName().equals(value.getOption().getOptionName())){
                    return true;
                }else{
                    return false;
                }
            }
            
        }
        return super.equals(obj);
    }
    
    /**
     * 초기상태에서 값의 변경 여부.
     * @return
     */
    public boolean isChanged(){
        if( this.initialStatus == this.valueStatus){
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return valueName + ((valueDesc == null || valueDesc.equals("")) ? "":" | " + valueDesc);
//      return super.toString();
    }

    /**
     * Description :
     * @method :
     * @date : 2013. 11. 28.
     * @author : CS.Park
     * @param :
     * @return : 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(VariantValue value) {
        return toString().compareTo(value.toString());
    }
    
    
}
