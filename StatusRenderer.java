package library.csci2010;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class StatusRenderer extends DefaultTableCellRenderer {
  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
      Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
      if (value.toString().startsWith("Overdue")) {
          c.setForeground(Color.RED);
      } else {
          c.setForeground(new Color(0, 128, 0)); 
      }
      return c;
  }
}

