/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.gui;

import java.awt.Color;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Antsstyle
 */
public class APTableHeaderRenderer implements TableCellRenderer {

    private static final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();

    public APTableHeaderRenderer(JTable table) {
        renderer.setHorizontalAlignment(JLabel.CENTER);
        renderer.setBackground(Color.red);
        renderer.setOpaque(true);
        renderer.setForeground(Color.green);
    }

    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int col) {
        return renderer.getTableCellRendererComponent(
                table, value, isSelected, hasFocus, row, col);
    }

}
