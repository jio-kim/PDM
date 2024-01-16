/**
 * 일괄 Upload작업에서 필요한 Option관리 Class
 */

package com.ssangyong.common.bundlework.bwutil;

public class BWUOption extends BWOption
{
  // Item 생성 여부
  private boolean isItemCreatable = false;
  // Item 수정 여부
  private boolean isItemModifiable = false;
  
  // Item Revision 생성 여부
  private boolean isRevCreatable = false;
  // Item Revision 수정 여부
  private boolean isRevModifiable = false;
  
  // DataSet 사용 여부
  private boolean isDSAvailable = false;
  // DataSet 삭제 후 생성 여부(교체)
  private boolean isDSChangable = false;
  
  // BOM 사용 여부
  private boolean isBOMAvailable = false;
  // BOM Line 속성 수정 여부
  private boolean isBOMLineModifiable = false;
  // BOM 재구성 여부(BOM Structure 삭제 후 재구성)
  private boolean isBOMRearrange = false;
  
  // ItemID, Revision 공백 허용여부(생성시 ID 신규발번)
  private boolean isItemIDBlankable = false;

  // AutoCAD Validate Check 여부( Migration에서 사용하지 않음 )
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
