package com.kgm.commands.weight;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;

/**
 * [SR170707-024] E-BOM Weight Report 개발 요청
 * @Copyright : Plmsoft
 * @author   : 이정건
 * @since    : 2017. 7. 10.
 * Package ID : com.kgm.commands.weight.MultiLineHeaderRenderer.java
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class MultiLineHeaderRenderer extends JList implements TableCellRenderer {
    private static final long serialVersionUID = 1L;

    public MultiLineHeaderRenderer() {
      setOpaque(true);
      setForeground(UIManager.getColor("TableHeader.foreground"));
      setBackground(UIManager.getColor("TableHeader.background"));
      setBorder(UIManager.getBorder("TableHeader.cellBorder"));
      ListCellRenderer renderer = getCellRenderer();
      ((JLabel) renderer).setHorizontalAlignment(JLabel.CENTER);
      ((JLabel) renderer).setVerticalAlignment(JLabel.CENTER);
      setCellRenderer(renderer);
    }

    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      setFont(table.getFont());
      String str = (value == null) ? "" : value.toString();
      BufferedReader br = new BufferedReader(new StringReader(str));
      String line;
      Vector v = new Vector();
      try {
        while ((line = br.readLine()) != null) {
          v.addElement(line);
        }
      } catch (IOException ex) {
        ex.printStackTrace();
      }
      setListData(v);
      
      if(str.contains("Act Weight") || str.contains("ACT WEIGHT")){
    	  setBackground(Color.yellow);
      }else{
    	  setBackground(UIManager.getColor("TableHeader.background"));
      }
      
      if( v.size() == 1){
          JLabel label = new JLabel(value.toString());
          JPanel panel = new JPanel(new BorderLayout());
//          label.setForeground(UIManager.getColor("TableHeader.foreground"));
//          label.setBackground(UIManager.getColor("TableHeader.background"));
//          label.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
          label.setHorizontalAlignment(JLabel.CENTER);

          panel.setForeground(UIManager.getColor("TableHeader.foreground"));
          panel.setBackground(UIManager.getColor("TableHeader.background"));
          panel.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
          panel.add(label, BorderLayout.CENTER);

          return panel;
      }
      return this;
    }
  }
