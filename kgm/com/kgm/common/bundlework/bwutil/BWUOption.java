/**
 * �ϰ� Upload�۾����� �ʿ��� Option���� Class
 */

package com.kgm.common.bundlework.bwutil;

public class BWUOption extends BWOption
{
  // Item ���� ����
  private boolean isItemCreatable = false;
  // Item ���� ����
  private boolean isItemModifiable = false;
  
  // Item Revision ���� ����
  private boolean isRevCreatable = false;
  // Item Revision ���� ����
  private boolean isRevModifiable = false;
  
  // DataSet ��� ����
  private boolean isDSAvailable = false;
  // DataSet ���� �� ���� ����(��ü)
  private boolean isDSChangable = false;
  
  // BOM ��� ����
  private boolean isBOMAvailable = false;
  // BOM Line �Ӽ� ���� ����
  private boolean isBOMLineModifiable = false;
  // BOM �籸�� ����(BOM Structure ���� �� �籸��)
  private boolean isBOMRearrange = false;
  
  // ItemID, Revision ���� ��뿩��(������ ID �űԹ߹�)
  private boolean isItemIDBlankable = false;

  // AutoCAD Validate Check ����( Migration���� ������� ���� )
  private boolean isAutoCADValidatable = false;

  
  
  public boolean isAutoCADValidatable(){return this.isAutoCADValidatable;}
  public boolean isItemIDBlankable(){return this.isItemIDBlankable;}
  public boolean isItemCreatable(){return this.isItemCreatable;}
  public boolean isItemModifiable(){return this.isItemModifiable;}
  public boolean isRevCreatable(){return this.isRevCreatable;}
  public boolean isRevModifiable(){return this.isRevModifiable;}
  
  public boolean isDSAvailable(){return this.isDSAvailable;}
  public boolean isDSChangable(){return this.isDSChangable;}
  
  public boolean isBOMLineModifiable(){return this.isBOMLineModifiable;}
  public boolean isBOMRearrange(){return this.isBOMRearrange;}
  public boolean isBOMAvailable(){return this.isBOMAvailable;}
  
  
  public void setAutoCADValidatable(boolean flag){this.isAutoCADValidatable = flag;}
  public void setItemIDBlankable(boolean flag){this.isItemIDBlankable = flag;}
  public void setItemCreatable(boolean flag){this.isItemCreatable = flag;}
  public void setItemModifiable(boolean flag){this.isItemModifiable = flag;}
  public void setRevCreatable(boolean flag){this.isRevCreatable = flag;}
  public void setRevModifiable(boolean flag){this.isRevModifiable = flag;}
  
  public void setDSAvailable(boolean flag){this.isDSAvailable = flag;}
  public void setDSChangable(boolean flag){this.isDSChangable = flag;}
  
  public void setBOMLineModifiable(boolean flag){this.isBOMLineModifiable = flag;}
  public void setBOMRearrange(boolean flag){this.isBOMRearrange = flag;}
  public void setBOMAvailable(boolean flag){this.isBOMAvailable = flag;}
  
  

  public BWUOption()
  {
    super();
  }
  
  
}
