/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.gui;

import antsstyle.displatechecker.configuration.modules.GUIConfig;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.AbstractButton;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.plaf.TableHeaderUI;
import javax.swing.table.JTableHeader;

/**
 *
 * @author Antsstyle
 */
public class APTableHeaderUI extends TableHeaderUI {

    @Override
    public void paint(Graphics g, JComponent c) {
        JTableHeader b = (JTableHeader) c;
        super.paint(g, c);
        b.setBackground(Color.RED);
        b.setForeground(GUIConfig.JBUTTON_FONT_COLOUR);
    }

}
