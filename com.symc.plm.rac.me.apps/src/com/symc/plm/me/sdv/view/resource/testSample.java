package com.symc.plm.me.sdv.view.resource;

import java.text.Collator;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class testSample extends Composite {

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public testSample(Composite parent, int style) {
        super(parent, style);

        final Table table = new Table(parent, SWT.BORDER);
        table.setHeaderVisible(true);
        final TableColumn column1 = new TableColumn(table, SWT.NONE);
        column1.setText("Column 1");
        final TableColumn column2 = new TableColumn(table, SWT.NONE);
        column2.setText("Column 2");
        TableItem item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "a", "3" });
        item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "b", "2" });
        item = new TableItem(table, SWT.NONE);
        item.setText(new String[] { "c", "1" });
        column1.setWidth(100);
        column2.setWidth(100);
        Listener sortListener = new Listener() {
          public void handleEvent(Event e) {
            TableItem[] items = table.getItems();
            Collator collator = Collator.getInstance(Locale.getDefault());
            TableColumn column = (TableColumn) e.widget;
            int index = column == column1 ? 0 : 1;
            for (int i = 1; i < items.length; i++) {
              String value1 = items[i].getText(index);
              for (int j = 0; j < i; j++) {
                String value2 = items[j].getText(index);
                if (collator.compare(value1, value2) < 0) {
                  String[] values = { items[i].getText(0), items[i].getText(1) };
                  items[i].dispose();
                  TableItem item = new TableItem(table, SWT.NONE, j);
                  item.setText(values);
                  items = table.getItems();
                  break;
                }
              }
            }
            table.setSortColumn(column);
          }
        };
        column1.addListener(SWT.Selection, sortListener);
        column2.addListener(SWT.Selection, sortListener);
        table.setSortColumn(column1);
        table.setSortDirection(SWT.UP);
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }

}
