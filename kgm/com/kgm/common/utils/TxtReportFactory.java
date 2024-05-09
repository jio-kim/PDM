/*
 * �ۼ���        : 2010. 07. 19. 
 * File Name : TxtReportFactory.java 
 * Class ���� : Text ������ Report�� �����մϴ�.
 *             Header,Header ���� ������ �������� RowData ��������.
 * ����                 : Text File Open�� �������� Font(FixedSys, Courier New .. etc)�� ����ϱ� �ٶ��ϴ�.  
 *             
 * Example----------------------
    String[] szHeaderName = {"Item Type","Item ID","Revision","Status", "Message"};
    int[] szHeaderWidth = {20,30,10,20,100};
    TxtReportFactory rptFactory = new TxtReportFactory(szHeaderName,szHeaderWidth,true,true);
    for(;;)
    {
      ArrayList<String> dataList = new ArrayList<String>();
      dataList.add( "#Item Type Value#" );
      dataList.add( "#Item ID Value#" );
      dataList.add( "#Revision Value#" );
      dataList.add( "#Status Value#" );
      dataList.add( "#Message Value#" );
      
      rptFactory.setReportData(dataList, nLevel);
    }   
    rptFactory.saveReport("#File ���#");
    
              
    No.      ||  Level            ||  Item Type             ||  Item ID                         ||  Revision    ||  Status                ||  Message                                                                                             
    =======  ||  ===============  ||  ====================  ||  ==============================  ||  ==========  ||  ====================  ||  =================================
    1        ||  0                ||  Item                  ||  10-SYS000108                    ||  A           ||  Completed             ||  Upload�Ϸ�,Bom Structure ���� �Ϸ�                                                                        
    2        ||   1               ||  Dataset               ||  10-SYS000108 002002             ||              ||  Completed             ||  Upload�Ϸ�                                                                                            
    3        ||   1               ||  Dataset               ||  10-SYS000108 001002             ||              ||  Completed             ||  Upload�Ϸ�                                                                                            
    4        ||   1               ||  Item                  ||  10-SYS000109                    ||  A           ||  Completed             ||  Upload�Ϸ�,Bom Structure ���� �Ϸ�                                                                        
    5        ||    2              ||  Dataset               ||  10-SYS000109                    ||              ||  Completed             ||  Upload�Ϸ�                                                                                            
    6        ||    2              ||  Item                  ||  10-SYS000110                    ||  A           ||  Completed             ||  Upload�Ϸ�,Bom Structure ���� �Ϸ�                                                                        
    7        ||     3             ||  Dataset               ||  10-SYS000110 002002             ||              ||  Completed             ||  Upload�Ϸ�                                                                                            
    8        ||     3             ||  Dataset               ||  10-SYS000110 001002             ||              ||  Completed             ||  Upload�Ϸ�                                                                                            
    9        ||    2              ||  Item                  ||  3445-Z34856                     ||  A           ||  Completed             ||  Upload�Ϸ�,Bom Structure ���� �Ϸ�                                                                        
    10       ||   1               ||  Item                  ||  10-SYS000111                    ||  A           ||  Completed             ||  Upload�Ϸ�,Bom Structure ���� �Ϸ�                                                                        
    11       ||    2              ||  Dataset               ||  10-SYS000111                    ||              ||  Completed             ||  Upload�Ϸ�
    
 * -----------------------------Example                                                                                             
 *             
 */

package com.kgm.common.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class TxtReportFactory
{

  // Tab String
  public static final String strTab = "  ||  ";

  // index Attribute Size
  public static final int nIndexSize = 7;
  // level Attribute Size
  public static final int nLevelSize = 15;

  // Report Row List
  private ArrayList<ReportUnit> reportList;

  // Header Names
  private String[] szHeaderNames;
  // Header Width Names
  private int[] szHeaderWidths;

  // Index ��� Flag
  private boolean isIndexUse = false;
  // Level ��� Flag
  private boolean isLevelUse = false;

  public TxtReportFactory(String[] szHeaderNames, int[] szHeaderWidths, boolean isIndexUse, boolean isLevelUse)
  {
    this.szHeaderNames = szHeaderNames;
    this.szHeaderWidths = szHeaderWidths;
    this.isIndexUse = isIndexUse;
    this.isLevelUse = isLevelUse;

    this.reportList = new ArrayList<ReportUnit>();
  }

  /**
   * Report Item ���
   * 
   * @param itemID : Item ID
   * @param itemLevel : Item Level
   */
  public void setReportData(ArrayList<String> dataList, int nLevel)
  {

    if (this.szHeaderNames.length == dataList.size())
      this.reportList.add(new ReportUnit(dataList, nLevel));
    else
      System.out.println("Txt Report Header ������ Data ������ ��ġ���� �ʽ��ϴ�.");

  }

  /**
   * Report String ��ȯ
   */
  public String toString()
  {
    StringBuffer szReport = new StringBuffer();

    // text������ Header����
    StringBuffer szHeader = new StringBuffer();
    // Text���� Border����
    StringBuffer szBorder = new StringBuffer();

    if (this.isIndexUse)
    {
      szHeader.append(addSpace("No.", nIndexSize) + strTab);
      szBorder.append(getBorder(nIndexSize) + strTab);

    }

    if (this.isLevelUse)
    {
      szHeader.append(addSpace("Level", nLevelSize) + strTab);
      szBorder.append(getBorder(nLevelSize) + strTab);
    }

    for (int i = 0; i < this.szHeaderNames.length; i++)
    {

      szHeader.append(addSpace(this.szHeaderNames[i], this.szHeaderWidths[i]));
      szBorder.append(getBorder(this.szHeaderWidths[i]));

      if (i == (this.szHeaderNames.length - 1))
      {
        szHeader.append("\r\n");
        szBorder.append("\r\n");
      }
      else
      {
        szHeader.append(strTab);
        szBorder.append(strTab);
      }

    }

    szReport.append(szHeader.toString());
    szReport.append(szBorder.toString());

    for (int i = 0; i < this.reportList.size(); i++)
    {
      ReportUnit unit = this.reportList.get(i);

      if (this.isIndexUse)
      {
        szReport.append(addSpace((i + 1) + "", nIndexSize));
        szReport.append(strTab);
      }
      if (this.isLevelUse)
      {
        szReport.append(addSpace(getLevel(unit.getLevel()), nLevelSize));
        szReport.append(strTab);
      }

      ArrayList<String> dataList = unit.getDataList();
      for (int j = 0; j < dataList.size(); j++)
      {
        szReport.append(addSpace( dataList.get(j).replaceAll("\n", " ") , this.szHeaderWidths[j]));

        if (j == (dataList.size() - 1))
          szReport.append("\r\n");
        else
          szReport.append(strTab);

      }

    }

    return szReport.toString();
  }

  /**
   * Border ���� String ��ȯ
   */
  public static String getBorder(int nColumnSize)
  {
    StringBuffer strBuf = new StringBuffer();
    for (int i = 0; i < nColumnSize; i++)
    {

      strBuf.append("=");
    }

    return strBuf.toString();
  }

  /**
   * Text������ Indent�� ���߱� ���� �� Attribute���� Size ����
   */
  public static String addSpace(String strItem, int nColumnSize)
  {
    StringBuffer strBuf = new StringBuffer();

    int nItemSize = 0;
    if (strItem != null)
    {
      nItemSize = charLen(strItem);
      strBuf.append(strItem);
    }
    else
    {
      nItemSize = 0;
    }

    for (int i = nItemSize; i < nColumnSize; i++)
    {
      strBuf.append(" ");
    }

    return strBuf.toString();

  }

  public static int charLen(String value)
  {

    int strlen = 0;

    for (int j = 0; j < value.length(); j++)
    {
      char c = value.charAt(j);
      if (c < 0xac00 || 0xd7a3 < c)
      {
        strlen++;
      }
      // �ѱ��� ���
      else
        strlen += 2;
    }
    return strlen;
  }

  /**
   * Level ���� String ��ȯ
   */
  public String getLevel(int nLevel)
  {

    StringBuffer strBuf = new StringBuffer();
    for (int i = 0; i < nLevel; i++)
    {

      strBuf.append(" ");
    }

    strBuf.append(nLevel);

    return strBuf.toString();
  }

  public void saveReport(String strFileFullPath)
  {

    // Import �۾� ������ File�� ����
    try
    {
      FileOutputStream fos = null;
      fos = new FileOutputStream(strFileFullPath);
      fos.write(this.toString().getBytes());
      fos.close();
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
  }

  /**
   * Report ���� Templete Class
   */
  class ReportUnit
  {
    ArrayList<String> dataList;
    int nLevel = 0;

    ReportUnit(ArrayList<String> dataList, int nLevel)
    {
      this.dataList = dataList;
      this.nLevel = nLevel;
    }

    ArrayList<String> getDataList()
    {
      return this.dataList;
    }

    int getLevel()
    {
      return this.nLevel;
    }

  }

}
