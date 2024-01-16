package com.ssangyong.commands.partmaster;

public interface Constants
{
  public static final String ACTIONTYPE_NEW = "New";
  public static final String ACTIONTYPE_EXTRACT = "Save As(Extract)";
  public static final String ACTIONTYPE_DEFERENT = "Save As(Different)";
  public static final String ACTIONTYPE_DSR = "DSR(Dev.Self Release)";

  public static final String ATTR_NAME_ITEMTYPE = "object_type";
  public static final String ATTR_NAME_ITEMNAME = "object_name";
  public static final String ATTR_NAME_ITEMDESC = "object_desc";
  public static final String ATTR_NAME_ITEMID = "item_id";
  public static final String ATTR_NAME_ACTIONTYPE = "action_type";
  public static final String ATTR_NAME_STAGE = "s7_STAGE";
  public static final String ATTR_NAME_REGULAR = "s7_REGULAR_PART_NO";
  public static final String ATTR_NAME_BASEITEMID = "baseitem";
  public static final String ATTR_NAME_ECOITEMID = "ecoitem";
  
  public static final String ATTR_NAME_DATASETSUCCEED = "dataset_succeed";
  
  
  public static final String ITEM_TYPE_VEHICLEPART = "General Part";
  public static final String ITEM_TYPE_STANDARDPART = "Standard Part";
  public static final String ITEM_TYPE_MATERIALPART = "Material";
  public static final String ITEM_TYPE_ELECTRONICPART = "Electronic Part";
  public static final String ITEM_TYPE_DMUPART = "DMU Part";
  public static final String ITEM_TYPE_SOFTWAREPART = "Software Part";
  

  public static final String ITEM_TYPE_PRODUCT = "Product";
  public static final String ITEM_TYPE_VARIANT = "Variant";
  public static final String ITEM_TYPE_FUNCTION = "Function";
  public static final String ITEM_TYPE_FUNCTIONMASTER = "Function Master";
  
  // [SR140702-059][20140626] KOG SaveAs 기능인지 구분하기위한 Canstants
  public static final String COMMAND_SAVE_AS = "SaveAs";
  

}
