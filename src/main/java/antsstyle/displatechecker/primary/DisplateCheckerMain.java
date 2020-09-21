/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.primary;

import antsstyle.displatechecker.db.CoreDB;
import antsstyle.displatechecker.gui.GUI;
import java.io.File;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author antss
 */
public class DisplateCheckerMain {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            LOGGER.warn("Could not initialise UI look and feel - reverting to default look and feel.");
        }

        File f = new File("chromedriver.exe");
        if (!f.exists()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "You must download chromedriver.exe and put it in the DisplateChecker folder before"
                    + " proceeding. Examine the README to see where to download it.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File f2 = new File("searchterms.txt");
        if (!f2.exists()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "No searchterms.txt file detected. Make sure the file exists in the DisplateChecker folder "
                    + "before proceeding.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        CoreDB.initialise();

        GUI.getInstance().addUserImages();
        GUI.getInstance().initialise();

        SwingUtilities.invokeLater(
                () -> {
                    GUI.getInstance()
                            .setVisible(true);
                }
        );

    }

}
