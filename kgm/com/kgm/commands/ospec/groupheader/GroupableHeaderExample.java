package com.kgm.commands.ospec.groupheader;
/* (swing1.1beta3)
 *example from 
 http://www.crionics.com/products/opensource/faq/swing_ex/SwingExamples.html 
 *
 */

/* (swing1.1beta3)
 *
 * |-----------------------------------------------------|
 * |        |       Name      |         Language         |
 * |        |-----------------|--------------------------|
 * |  SNo.  |        |        |        |      Others     |
 * |        |   1    |    2   | Native |-----------------|
 * |        |        |        |        |   2    |   3    |  
 * |-----------------------------------------------------|
 * |        |        |        |        |        |        |
 *
 */
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;

/**
 * @version 1.0 11/09/98
 */
public class GroupableHeaderExample extends JFrame {

  public GroupableHeaderExample() {
    super( "Groupable Header Example" );

    DefaultTableModel dm = new DefaultTableModel();
    dm.setDataVector(
    		new Object[][]{
    		/*
      {"119","foo","bar","ja","ko","zh"},
      {"911","bar","foo","en","fr","pt"}
      */
    		{"S","S","S","S","S","S","S","S","S","S","S","S","S","S","S"},
    		{"-","-","-","-","-","-","-","-","-","O","-","-","-","-","O"},
    		{"S","S","S","S","S","S","S","S","S","S","S","S","S","S","S"},
    		{"-","-","-","-","-","-","-","-","-","O","-","-","-","-","O"}	
    }
    		,
    new Object[]{
    		"D7KWS","D7KWD","D7KWH","E7KWD","E7KWH","G5KWD","G5TWD","G5PWD","G5PWH","G5WWD","G7KWD","G7TWD","G7PWD","G7PWH","G7WWD", "TEST"
    		//"SNo.","1","2","Native","2","3"
    		}
    );

    JTable table = new JTable( dm ) {
      protected JTableHeader createDefaultTableHeader() {
          return new GroupableTableHeader(columnModel);
      }
    };
    
    
    TableColumnModel cm = table.getColumnModel();
    
    ColumnGroup v1 = new ColumnGroup("STD");
    v1.add(cm.getColumn(0));
    ColumnGroup v2 = new ColumnGroup("DLX");
    v2.add(cm.getColumn(1));
    ColumnGroup v3 = new ColumnGroup("H/DLX");
    v3.add(cm.getColumn(2));
    ColumnGroup v4 = new ColumnGroup("DLX");
    v4.add(cm.getColumn(3));
    ColumnGroup v5 = new ColumnGroup("H/DLX");
    v5.add(cm.getColumn(4));
    ColumnGroup v6 = new ColumnGroup("DLX");
    v6.add(cm.getColumn(5));
    ColumnGroup v7 = new ColumnGroup("DLX");
    v7.add(cm.getColumn(6));
    ColumnGroup v8 = new ColumnGroup("DLX");
    v8.add(cm.getColumn(7));
    ColumnGroup v9 = new ColumnGroup("H/DLX");
    v9.add(cm.getColumn(8));
    ColumnGroup v10 = new ColumnGroup("DLX");
    v10.add(cm.getColumn(9));
    ColumnGroup v11 = new ColumnGroup("DLX");
    v11.add(cm.getColumn(10));
    ColumnGroup v12 = new ColumnGroup("DLX");
    v12.add(cm.getColumn(11));
    ColumnGroup v13 = new ColumnGroup("DLX");
    v13.add(cm.getColumn(12));
    ColumnGroup v14 = new ColumnGroup("H/DLX");
    v14.add(cm.getColumn(13));
    ColumnGroup v15 = new ColumnGroup("DLX");
    v15.add(cm.getColumn(14));
    
    ColumnGroup t1 = new ColumnGroup("D20DTR");
    t1.add(v1);
    t1.add(v2);
    t1.add(v3);
    ColumnGroup t2 = new ColumnGroup("D20DTR");
    t2.add(v4);
    t2.add(v5);
    ColumnGroup t3 = new ColumnGroup("D20DTR");
    t3.add(v6);
    ColumnGroup t4 = new ColumnGroup("D27DT");
    t4.add(v7);
    ColumnGroup t5 = new ColumnGroup("D27DTP");
    t5.add(v8);
    t5.add(v9);
    ColumnGroup t6 = new ColumnGroup("G32D");
    t6.add(v10);
    ColumnGroup t7 = new ColumnGroup("D20DTR");
    t7.add(v11);
    ColumnGroup t8 = new ColumnGroup("D27DT");
    t8.add(v12);
    ColumnGroup t9 = new ColumnGroup("D27DTP");
    t9.add(v13);
    t9.add(v14);
    ColumnGroup t10 = new ColumnGroup("G32D");
    t10.add(v15);

    ColumnGroup e1 = new ColumnGroup("7 PASS");
    e1.add(t1);
    ColumnGroup e2 = new ColumnGroup("7 PASS");
    e2.add(t2);
    ColumnGroup e3 = new ColumnGroup("5 PASS");
    e3.add(t3);
    e3.add(t4);
    e3.add(t5);
    e3.add(t6);
    ColumnGroup e4 = new ColumnGroup("7 PASS");
    e4.add(t7);
    e4.add(t8);
    e4.add(t9);
    e4.add(t10);
    
    ColumnGroup d1 = new ColumnGroup("DOM(LHD)");
    d1.add(e1);
    ColumnGroup d2 = new ColumnGroup("EU(LHD/RHD)");
    d2.add(e2);
    ColumnGroup d3 = new ColumnGroup("GEN(LHD/RHD)");
    d3.add(e3);
    d3.add(e4);
    
    /*
    ColumnGroup g_name = new ColumnGroup("Name");
    g_name.add(cm.getColumn(1));
    g_name.add(cm.getColumn(2));
    ColumnGroup g_lang = new ColumnGroup("Language");
    g_lang.add(cm.getColumn(3));
    ColumnGroup g_other = new ColumnGroup("Others");
    g_other.add(cm.getColumn(4));
    g_other.add(cm.getColumn(5));
    g_lang.add(g_other);
    */
    table.getColumnModel().setColumnMargin(0);
    GroupableTableHeader header = (GroupableTableHeader)table.getTableHeader();
    header.addColumnGroup(d1);
    header.addColumnGroup(d2);
    header.addColumnGroup(d3);
    /*
    header.addColumnGroup(g_name);
    header.addColumnGroup(g_lang);
    */
    JScrollPane scroll = new JScrollPane( table );
    getContentPane().add( scroll );
    setSize( 400, 500 );   
  }

  public static void main(String[] args) {
    GroupableHeaderExample frame = new GroupableHeaderExample();
    frame.addWindowListener( new WindowAdapter() {
      public void windowClosing( WindowEvent e ) {
  System.exit(0);
      }
    });
    frame.setVisible(true);
  }
}

/*
 * (swing1.1beta3)
 * 
 */



 

