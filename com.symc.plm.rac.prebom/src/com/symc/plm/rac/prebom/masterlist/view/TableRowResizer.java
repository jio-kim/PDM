package com.symc.plm.rac.prebom.masterlist.view;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.MouseInputAdapter;
import java.awt.*;
import java.awt.event.MouseEvent;

public class TableRowResizer extends MouseInputAdapter
{ 
    public static Cursor resizeCursor = Cursor.getPredefinedCursor(Cursor.N_RESIZE_CURSOR); 

    private int mouseYOffset, resizingRow; 
    private Cursor otherCursor = resizeCursor; 
    private JTable fixedTable, table; 

    public TableRowResizer(JTable fixedTable, JTable table){ 
    	this.fixedTable = fixedTable;
        this.table = table; 
        fixedTable.addMouseListener(this); 
        fixedTable.addMouseMotionListener(this); 
    } 

    private int getResizingRow(Point p){ 
        return getResizingRow(p, fixedTable.rowAtPoint(p)); 
    } 

    private int getResizingRow(Point p, int row){ 
        if(row == -1){ 
            return -1; 
        } 
        int col = fixedTable.columnAtPoint(p); 
        if(col==-1) 
            return -1; 
        Rectangle r = fixedTable.getCellRect(row, col, true); 
        r.grow(0, -3); 
        if(r.contains(p)) 
            return -1; 

        int midPoint = r.y + r.height / 2; 
        int rowIndex = (p.y < midPoint) ? row - 1 : row; 

        return rowIndex; 
    } 

    public void mousePressed(MouseEvent e){ 
        Point p = e.getPoint(); 

        resizingRow = getResizingRow(p); 
        mouseYOffset = p.y - fixedTable.getRowHeight(resizingRow); 
    } 

    private void swapCursor(){ 
        Cursor tmp = fixedTable.getCursor(); 
        fixedTable.setCursor(otherCursor); 
        otherCursor = tmp; 
    }

    public void mouseMoved(MouseEvent e){
        if((getResizingRow(e.getPoint())>=0)
           != (fixedTable.getCursor() == resizeCursor)){
            swapCursor();
        }
    }

    public void mouseDragged(MouseEvent e){
        int mouseY = e.getY();

        if(resizingRow >= 0){
            int newHeight = mouseY - mouseYOffset;
            if(newHeight > 0){
            	fixedTable.setRowHeight(resizingRow, newHeight);
                table.setRowHeight(resizingRow, newHeight);
            }
        }
    }
}