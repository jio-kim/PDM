package com.kgm.admin.lovmanage;

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * 
 * <p>Title: SimpleFilter</p>
 * 
 * <p>Description: JFileChooser���� ����� ����</p>
 */
public class SimpleStructureFilter extends FileFilter
{
  private String extension;
  private String description;

  public SimpleStructureFilter(String extension, String description)
  {
    this.extension = "." + extension.toLowerCase();
    this.description = description;
  }

  /**
   * Ư�� Ȯ������ ���ϸ� JFileChooser�� Display
   * 
   * @param f File
   * @return boolean
   */
  public boolean accept(File f)
  {
    if (f == null)
      return false;
    if (f.isDirectory())
      return true;
    return f.getName().toLowerCase().endsWith(extension);
  }

  /**
   * ���� Description�� ��ȯ
   * 
   * @return String
   */
  public String getDescription()
  {
    return description;
  }
}
