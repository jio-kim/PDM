package com.kgm.admin.lovmanage;

import java.io.*;

import javax.swing.filechooser.FileFilter;

/**
 * 
 * <p>Title: SimpleFilter</p>
 * 
 * <p>Description: JFileChooser에서 사용할 필터</p>
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
   * 특정 확장자의 파일만 JFileChooser에 Display
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
   * 파일 Description을 반환
   * 
   * @return String
   */
  public String getDescription()
  {
    return description;
  }
}
