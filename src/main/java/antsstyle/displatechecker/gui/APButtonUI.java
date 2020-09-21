/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.gui;

import antsstyle.displatechecker.configuration.modules.GUIConfig;
import java.awt.Graphics;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JComponent;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author Antsstyle
 */
public class APButtonUI extends BasicButtonUI {

    private Boolean borderColour = false;
    private Boolean warningColour = false;

    public void setWarningColour(Boolean warning) {
        this.warningColour = warning;
    }
    
    public void setBorderColour(Boolean borderColour) {
        this.borderColour = borderColour;
    }

    @Override
    public void paint(Graphics g, JComponent c) {
        AbstractButton b = (AbstractButton) c;
        ButtonModel model = b.getModel();
        super.paint(g, c);
        if (model.isPressed()) {
            b.setBackground(GUIConfig.JBUTTON_BG_COLOUR.darker());
        } else if (model.isRollover()) {
            b.setBackground(GUIConfig.JBUTTON_BG_COLOUR.brighter());
        } else {
            b.setBackground(GUIConfig.JBUTTON_BG_COLOUR);
        }
        if (warningColour) {
            b.setForeground(GUIConfig.JLABEL_FONT_WARNING_COLOUR);
        } else {
            b.setForeground(GUIConfig.JBUTTON_FONT_COLOUR);
        }
        if (borderColour) {
            b.setBorder(BorderFactory.createLineBorder(GUIConfig.JLABEL_FONT_ERROR_COLOUR));
        } else {
            b.setBorder(BorderFactory.createRaisedBevelBorder());
        }
    }

}
