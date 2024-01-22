
package com.symc.plm.me.common;

import java.text.Collator;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Table Header Sort Å¬·¡½º
 * Class Name : SDVSortListenerFactory
 * Class Description : 
 * @date 2013. 12. 24.
 *
 */
public class SDVSortListenerFactory implements Listener {
    private Comparator<Object> currentComparator = null;

    private Collator col = Collator.getInstance(Locale.getDefault());

    public static final int INT_COMPARATOR = 0;
    public static final int STRING_COMPARATOR = 1;
    public static final int DATE_COMPARATOR = 2;
    public static final int DOUBLE_COMPARATOR = 3;
    public static final int HOUR_COMPARATOR = 4;
    public static final int NUMBER_FORMATTER_COMPARATOR = 5;
    public static final int PERCENT_COMPARATOR = 6;
    public static final int CURRENCY_COMPARATOR = 7;

    public static final String NUMBER_FORMAT = "-###,###,###";
    public static final String DATE_FORMAT = "yyyy-MM-dd";

    private SDVSortListenerFactory(int _comp) {
        switch (_comp) {
        case INT_COMPARATOR:
            currentComparator = intComparator;
            break;

        case STRING_COMPARATOR:
            currentComparator = strComparator;
            break;

        case DATE_COMPARATOR:
            currentComparator = dateComparator;
            break;

        case DOUBLE_COMPARATOR:
            currentComparator = doubleComparator;
            break;

        case HOUR_COMPARATOR:
            currentComparator = hourComparator;
            break;

        case NUMBER_FORMATTER_COMPARATOR:
            currentComparator = formattedNumberComparator;
            break;

        case PERCENT_COMPARATOR:
            currentComparator = percentNumberComparator;
            break;

        case CURRENCY_COMPARATOR:
            currentComparator = currencyComparator;
            break;

        default:
            currentComparator = strComparator;
        }
    }

    public static Listener getListener(int _comp) {
        return new SDVSortListenerFactory(_comp);
    }

    private int colIndex = 0;
    private int updown = -1;

    // Currency Comparator
    private Comparator<Object> currencyComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {
            try {
                TableItem t1 = (TableItem) arg0;
                TableItem t2 = (TableItem) arg1;

                NumberFormat nFormat = NumberFormat.getCurrencyInstance();
                Number t1ParseNumber = nFormat.parse(t1.getText(colIndex));
                Number t2ParseNumber = nFormat.parse(t2.getText(colIndex));

                long t1Long = t1ParseNumber.longValue();
                long t2Long = t2ParseNumber.longValue();

                if (t1Long < t2Long)
                    return -1 * updown;
                if (t1Long > t2Long)
                    return 1 * updown;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }
    };

    // Formatted Percent Number Comparator
    private Comparator<Object> percentNumberComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {
            try {
                TableItem t1 = (TableItem) arg0;
                TableItem t2 = (TableItem) arg1;

                NumberFormat nFormat = NumberFormat.getPercentInstance();
                Number t1ParseNumber = nFormat.parse(t1.getText(colIndex));
                Number t2ParseNumber = nFormat.parse(t2.getText(colIndex));

                long t1Long = t1ParseNumber.longValue();
                long t2Long = t2ParseNumber.longValue();

                if (t1Long < t2Long)
                    return -1 * updown;
                if (t1Long > t2Long)
                    return 1 * updown;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }
    };

    // Formatted Number Comparator
    private Comparator<Object> formattedNumberComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {
            try {
                TableItem t1 = (TableItem) arg0;
                TableItem t2 = (TableItem) arg1;

                NumberFormat nFormat = NumberFormat.getInstance();
                Number t1ParseNumber = nFormat.parse(t1.getText(colIndex).equals("") ? "0" : t1.getText(colIndex));
                Number t2ParseNumber = nFormat.parse(t2.getText(colIndex).equals("") ? "0" : t2.getText(colIndex));

                long t1Long = t1ParseNumber.longValue();
                long t2Long = t2ParseNumber.longValue();

                if (t1Long < t2Long)
                    return -1 * updown;
                if (t1Long > t2Long)
                    return 1 * updown;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return 0;
        }
    };

    // Integer Comparator
    private Comparator<Object> intComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {

            TableItem t1 = (TableItem) arg0;
            TableItem t2 = (TableItem) arg1;

            int v1 = Integer.valueOf(t1.getText(colIndex)).intValue();
            int v2 = Integer.valueOf(t2.getText(colIndex)).intValue();

            if (v1 < v2)
                return -1 * updown;
            if (v1 > v2)
                return 1 * updown;

            return 0;
        }
    };

    // String Comparator
    private Comparator<Object> strComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {

            TableItem t1 = (TableItem) arg0;
            TableItem t2 = (TableItem) arg1;

            String v1 = (t1.getText(colIndex));
            String v2 = (t2.getText(colIndex));

            return (col.compare(v1, v2)) * updown;
        }
    };

    // Double Comparator
    private Comparator<Object> doubleComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {

            TableItem t1 = (TableItem) arg0;
            TableItem t2 = (TableItem) arg1;

            double v1 = Double.parseDouble(t1.getText(colIndex));
            double v2 = Double.parseDouble(t2.getText(colIndex));

            if (v1 < v2)
                return 1 * updown;
            if (v1 > v2)
                return -1 * updown;

            return 0;
        }
    };

    // Hour Comparator (hh:mm:ss)
    private Comparator<Object> hourComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {

            TableItem t1 = (TableItem) arg0;
            TableItem t2 = (TableItem) arg1;

            String v1 = (t1.getText(colIndex)).trim();
            String v2 = (t2.getText(colIndex)).trim();

            DateFormat df = new SimpleDateFormat("hh:mm:ss");

            Date d1 = null;
            Date d2 = null;

            try {
                d1 = df.parse(v1);
            } catch (ParseException e) {
                System.out.println("[WARNING] v1 " + v1);
                try {
                    d1 = df.parse("01:01:01");
                } catch (ParseException e1) {
                }
            }

            try {
                d2 = df.parse(v2);
            } catch (ParseException e) {
                System.out.println("[WARNING] v2 " + v2);
                try {
                    d2 = df.parse("01:01:01");
                } catch (ParseException e1) {
                }
            }

            if (d1.equals(d2))
                return 0;

            return updown * (d1.before(d2) ? 1 : -1);
        }
    };

    private Comparator<Object> dateComparator = new Comparator<Object>() {
        public int compare(Object arg0, Object arg1) {
            TableItem t1 = (TableItem) arg0;
            TableItem t2 = (TableItem) arg1;

            String v1 = (t1.getText(colIndex)).trim();
            String v2 = (t2.getText(colIndex)).trim();

            v1.replaceAll("-", "/");
            v2.replaceAll("-", "/");

            DateFormat df_usa = new SimpleDateFormat(DATE_FORMAT);

            DateFormat df = df_usa;

            Date d1 = null;
            Date d2 = null;

            try {
                d1 = df.parse(v1);
            } catch (ParseException e) {
                try {
                    d1 = df.parse("1900-01-01");
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }

            try {
                d2 = df.parse(v2);
            } catch (ParseException e) {
                try {
                    d2 = df.parse("1900-01-01");
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
            }

            if (d1.equals(d2))
                return 0;

            return updown * (d1.before(d2) ? -1 : 1);
        }
    };

    public void handleEvent(Event e) {
        updown = (updown == 1 ? -1 : 1);

        TableColumn currentColumn = (TableColumn) e.widget;
        Table table = currentColumn.getParent();

        colIndex = table.indexOf(currentColumn);

        table.setRedraw(false);

        TableItem[] items = table.getItems();

        Arrays.sort(items, currentComparator);

        table.setItemCount(items.length);

        for (int i = 0; i < items.length; i++) {
            TableItem item = new TableItem(table, SWT.NONE, i);
            cloneItem(items[i], item);
            items[i].dispose();
        }
        table.setSortColumn(currentColumn);
        table.setSortDirection(updown == 1 ? SWT.UP : SWT.DOWN);
        table.setRedraw(true);
    }

    private void cloneItem(TableItem fromItem, TableItem toItem) {
        toItem.setChecked(fromItem.getChecked());
        toItem.setGrayed(fromItem.getGrayed());
        toItem.setFont(fromItem.getFont());
        toItem.setForeground(fromItem.getForeground());
        toItem.setBackground(fromItem.getBackground());

        Table table = fromItem.getParent();
        for (int i = 0; i < table.getColumnCount(); i++) {
            toItem.setText(i, fromItem.getText(i));
            toItem.setImage(i, fromItem.getImage(i));
            toItem.setFont(i, fromItem.getFont(i));
            toItem.setForeground(i, fromItem.getForeground(i));
            toItem.setBackground(i, fromItem.getBackground(i));
        }
    }
}