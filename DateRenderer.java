package library.csci2010;

import java.awt.Component;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

class DateRenderer extends DefaultTableCellRenderer {
  private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM, dd, yyyy");

  @Override
  public Component getTableCellRendererComponent(JTable table, Object value,
          boolean isSelected, boolean hasFocus, int row, int column) {
      if (value instanceof LocalDate) {
          value = ((LocalDate) value).format(formatter);
      }
      return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
  }
}