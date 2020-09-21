/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.gui;

import antsstyle.displatechecker.configuration.modules.GUIConfig;
import antsstyle.displatechecker.datastructures.DBResponse;
import antsstyle.displatechecker.db.CoreDB;
import antsstyle.displatechecker.enumerations.DBTable;
import antsstyle.displatechecker.queues.DisplateComparisonQueue;
import antsstyle.displatechecker.queues.DisplateQueue;
import antsstyle.displatechecker.tools.ImageTools;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableModel;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.imgscalr.Scalr;

/**
 *
 * @author antss
 */
public class GUI extends javax.swing.JFrame {

    private static final Logger LOGGER = LogManager.getLogger();

    private static GUI gui;

    /**
     * Creates new form GUI
     */
    private GUI() {
        initComponents();
    }

    public JProgressBar getRetrieveArtProgressBar() {
        return retrieveArtProgressBar;
    }

    public void setRetrieveArtProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            retrieveArtProgressBar.setValue(value);
        });
    }

    public void setFindMatchesProgress(int value) {
        SwingUtilities.invokeLater(() -> {
            findMatchesProgressBar.setValue(value);
        });
    }

    public JProgressBar getFindMatchesProgressBar() {
        return findMatchesProgressBar;
    }

    public static GUI getInstance() {
        if (gui == null) {
            gui = new GUI();
        }
        return gui;
    }

    public void initialise() {
        userImageFilesTable.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            int row = userImageFilesTable.getSelectedRow();
            if (row == -1) {
                return;
            }
            loadMatchesInTable();
        });
        matchesTable.getSelectionModel().addListSelectionListener((ListSelectionEvent event) -> {
            int row = matchesTable.getSelectedRow();
            if (row == -1) {
                return;
            }
            previewMatchingImage();
        });
        GUI.setAllGUIColours();
    }

    private void loadMatchesInTable() {
        int row = userImageFilesTable.getSelectedRow();
        int modelRow = userImageFilesTable.convertRowIndexToModel(row);
        String path = System.getProperty("user.dir").concat("/Test Images/").concat((String) userImageFilesTable.getModel().getValueAt(modelRow, 0));
        path = StringUtils.replace(path, "\\", "/");
        DefaultTableModel dtm = (DefaultTableModel) matchesTable.getModel();
        dtm.setRowCount(0);
        RowSorter.SortKey k = new RowSorter.SortKey(1, SortOrder.DESCENDING);
        List<RowSorter.SortKey> keys = new LinkedList<>();
        keys.add(k);
        matchesTable.getRowSorter().setSortKeys(keys);
        ArrayList<TreeMap<String, Object>> rows = CoreDB.getDisplateMatchesForArtistImage(path);
        for (TreeMap<String, Object> dbRow : rows) {
            Double similarityPercentage = 100.0 - (((Double) dbRow.get("histogramdiff")) * 100.0);
            String displateURL = (String) dbRow.get("displateurl");
            dtm.addRow(new Object[]{displateURL, similarityPercentage});
        }
    }

    public void addUserImages() {
        String pathToFolder = System.getProperty("user.dir").concat("/Test Images/");
        File file = new File(pathToFolder);
        File[] files;
        DefaultTableModel dtm = (DefaultTableModel) userImageFilesTable.getModel();
        dtm.setRowCount(0);
        if (!file.exists() || !file.isDirectory()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Could not find test images folder - ensure it is in the main DisplateArtChecker folder.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.error("Failed to find search terms.");
            return;
        } else {
            files = file.listFiles();
        }
        for (File f : files) {
            if (ImageTools.isSupportedImageFile(f.getAbsolutePath())) {
                dtm.addRow(new Object[]{f.getName()});
            }
        }
    }

    private void previewMatchingImage() {
        int userRow = userImageFilesTable.getSelectedRow();
        int userModelRow = userImageFilesTable.convertRowIndexToModel(userRow);
        String userPath = System.getProperty("user.dir").concat("/Test Images/").concat((String) userImageFilesTable.getModel().getValueAt(userModelRow, 0));
        int row = matchesTable.getSelectedRow();
        int modelRow = matchesTable.convertRowIndexToModel(row);
        String url = (String) matchesTable.getModel().getValueAt(modelRow, 0);
        String path = CoreDB.checkIfDisplateURLInInfoTable(url);
        if (path != null) {
            try {
                BufferedImage img = ImageIO.read(new File(path));
                BufferedImage userImg = ImageIO.read(new File(userPath));
                userImageLabel.setIcon(new ImageIcon(ImageTools.getScaledImage(userImg, userImageLabel.getWidth(), userImageLabel.getHeight(),
                        Scalr.Method.AUTOMATIC)));
                matchingImageLabel.setIcon(new ImageIcon(ImageTools.getScaledImage(img, matchingImageLabel.getWidth(), matchingImageLabel.getHeight(),
                        Scalr.Method.AUTOMATIC)));
            } catch (Exception e) {
                LOGGER.error("Failed to load images!", e);
            }
        } else {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Error loading path to image (it was null).",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        userImageFilesTable = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        userImageFilesTable1 = new javax.swing.JTable();
        retrieveArtFromDisplateButton = new javax.swing.JButton();
        matchingImageLabel = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        matchesTable = new javax.swing.JTable();
        jScrollPane4 = new javax.swing.JScrollPane();
        matchesTable1 = new javax.swing.JTable();
        findMatchesButton = new javax.swing.JButton();
        retrieveArtProgressBar = new javax.swing.JProgressBar();
        findMatchesProgressBar = new javax.swing.JProgressBar();
        userImageLabel = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        goToTakedownFormButton = new javax.swing.JButton();
        setChromePathButton = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        goToDisplateURLButton = new javax.swing.JButton();
        mainMenuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        exitMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(55, 60, 74));

        jLabel1.setFont(new java.awt.Font("Dialog", 1, 18)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Displate Art Checker");

        jScrollPane1.setMaximumSize(new java.awt.Dimension(289, 161));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(289, 161));
        jScrollPane1.setPreferredSize(new java.awt.Dimension(289, 161));

        userImageFilesTable.setAutoCreateRowSorter(true);
        userImageFilesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Image Filename"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(userImageFilesTable);
        userImageFilesTable.getAccessibleContext().setAccessibleDescription("");

        userImageFilesTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Image Filename"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };
            boolean[] canEdit = new boolean [] {
                false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane3.setViewportView(userImageFilesTable1);

        retrieveArtFromDisplateButton.setText("Retrieve Art from Displate");
        retrieveArtFromDisplateButton.setMaximumSize(new java.awt.Dimension(176, 34));
        retrieveArtFromDisplateButton.setMinimumSize(new java.awt.Dimension(176, 34));
        retrieveArtFromDisplateButton.setPreferredSize(new java.awt.Dimension(176, 34));
        retrieveArtFromDisplateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retrieveArtFromDisplateButtonActionPerformed(evt);
            }
        });

        matchingImageLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        matchingImageLabel.setMaximumSize(new java.awt.Dimension(376, 524));
        matchingImageLabel.setMinimumSize(new java.awt.Dimension(376, 524));
        matchingImageLabel.setPreferredSize(new java.awt.Dimension(376, 524));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("Your images");
        jLabel2.setMaximumSize(new java.awt.Dimension(289, 16));
        jLabel2.setMinimumSize(new java.awt.Dimension(289, 16));
        jLabel2.setPreferredSize(new java.awt.Dimension(289, 16));

        jScrollPane2.setMaximumSize(new java.awt.Dimension(342, 403));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(342, 403));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(342, 403));

        matchesTable.setAutoCreateRowSorter(true);
        matchesTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Displate URL", "Similarity"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane2.setViewportView(matchesTable);
        if (matchesTable.getColumnModel().getColumnCount() > 0) {
            matchesTable.getColumnModel().getColumn(1).setMinWidth(80);
            matchesTable.getColumnModel().getColumn(1).setPreferredWidth(80);
            matchesTable.getColumnModel().getColumn(1).setMaxWidth(80);
        }

        matchesTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Displate URL", "Similarity"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Double.class
            };
            boolean[] canEdit = new boolean [] {
                false, false
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane4.setViewportView(matchesTable1);
        if (matchesTable1.getColumnModel().getColumnCount() > 0) {
            matchesTable1.getColumnModel().getColumn(1).setMinWidth(80);
            matchesTable1.getColumnModel().getColumn(1).setPreferredWidth(80);
            matchesTable1.getColumnModel().getColumn(1).setMaxWidth(80);
        }

        findMatchesButton.setText("Find Matches");
        findMatchesButton.setMaximumSize(new java.awt.Dimension(176, 34));
        findMatchesButton.setMinimumSize(new java.awt.Dimension(176, 34));
        findMatchesButton.setPreferredSize(new java.awt.Dimension(176, 34));
        findMatchesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findMatchesButtonActionPerformed(evt);
            }
        });

        retrieveArtProgressBar.setMaximumSize(new java.awt.Dimension(280, 34));
        retrieveArtProgressBar.setMinimumSize(new java.awt.Dimension(280, 34));
        retrieveArtProgressBar.setPreferredSize(new java.awt.Dimension(280, 34));

        findMatchesProgressBar.setMaximumSize(new java.awt.Dimension(280, 34));
        findMatchesProgressBar.setMinimumSize(new java.awt.Dimension(280, 34));
        findMatchesProgressBar.setPreferredSize(new java.awt.Dimension(280, 34));

        userImageLabel.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 2, true));
        userImageLabel.setMaximumSize(new java.awt.Dimension(376, 524));
        userImageLabel.setMinimumSize(new java.awt.Dimension(376, 524));
        userImageLabel.setPreferredSize(new java.awt.Dimension(376, 524));

        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Your Image");

        jLabel4.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel4.setText("Displate Matching Image");

        goToTakedownFormButton.setText("Go to Takedown Form");
        goToTakedownFormButton.setMaximumSize(new java.awt.Dimension(186, 32));
        goToTakedownFormButton.setMinimumSize(new java.awt.Dimension(186, 32));
        goToTakedownFormButton.setPreferredSize(new java.awt.Dimension(186, 32));
        goToTakedownFormButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToTakedownFormButtonActionPerformed(evt);
            }
        });

        setChromePathButton.setText("Set Chrome Path");
        setChromePathButton.setMaximumSize(new java.awt.Dimension(186, 32));
        setChromePathButton.setMinimumSize(new java.awt.Dimension(186, 32));
        setChromePathButton.setPreferredSize(new java.awt.Dimension(186, 32));
        setChromePathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setChromePathButtonActionPerformed(evt);
            }
        });

        jLabel5.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel5.setText("Displate Match Results");
        jLabel5.setMaximumSize(new java.awt.Dimension(289, 16));
        jLabel5.setMinimumSize(new java.awt.Dimension(289, 16));
        jLabel5.setPreferredSize(new java.awt.Dimension(289, 16));

        goToDisplateURLButton.setText("Go to selected Displate URL");
        goToDisplateURLButton.setMaximumSize(new java.awt.Dimension(186, 32));
        goToDisplateURLButton.setMinimumSize(new java.awt.Dimension(186, 32));
        goToDisplateURLButton.setPreferredSize(new java.awt.Dimension(186, 32));
        goToDisplateURLButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                goToDisplateURLButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(findMatchesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(retrieveArtFromDisplateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(retrieveArtProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(findMatchesProgressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                    .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(jPanel1Layout.createSequentialGroup()
                                        .addGap(0, 0, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                            .addComponent(goToTakedownFormButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(setChromePathButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                            .addComponent(goToDisplateURLButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(userImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addGap(20, 20, 20)
                                .addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(matchingImageLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.PREFERRED_SIZE, 368, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 0, 0))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 22, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(retrieveArtProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(retrieveArtFromDisplateButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(findMatchesProgressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(findMatchesButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 161, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(goToDisplateURLButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(goToTakedownFormButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(setChromePathButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(matchingImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(userImageLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );

        fileMenu.setText("File");

        exitMenuItem.setText("Exit");
        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        mainMenuBar.add(fileMenu);

        setJMenuBar(mainMenuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void findMatchesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findMatchesButtonActionPerformed
        findMatchesButton.setEnabled(false);
        findMatches();
        findMatchesButton.setEnabled(true);
    }//GEN-LAST:event_findMatchesButtonActionPerformed

    public static void setAllGUIColours() {
        UIManager.put("ScrollBar.shadow", GUIConfig.JBUTTON_BG_COLOUR);
        UIManager.put("ScrollBar.thumb", GUIConfig.JBUTTON_BG_COLOUR);
        UIManager.put("ScrollBar.thumbShadow", GUIConfig.JBUTTON_BG_COLOUR);
        UIManager.put("ScrollBar.thumbHighlight", GUIConfig.JBUTTON_BG_COLOUR);
        UIManager.put("ScrollBar.darkShadow", GUIConfig.JBUTTON_BG_COLOUR);
        UIManager.put("ScrollBar.highlight", GUIConfig.JBUTTON_BG_COLOUR);
        setGUIColours(GUI.getInstance());
    }

    public static void setGUIColours(Container comp) {
        if (SwingUtilities.isEventDispatchThread()) {
            setColour(comp);
            Component[] components = comp.getComponents();
            for (Component c : components) {
                if (c instanceof Container) {
                    Container p = (Container) c;
                    setGUIColours(p);
                }
                setColour(c);
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                setColour(comp);
                Component[] components = comp.getComponents();
                for (Component c : components) {
                    if (c instanceof Container) {
                        Container p = (Container) c;
                        setGUIColours(p);
                    }
                    setColour(c);
                }
            });
        }

    }

    private static void setColour(Component c) {
        if (SwingUtilities.isEventDispatchThread()) {
            setComponentColour(c);
        } else {
            SwingUtilities.invokeLater(() -> {
                setComponentColour(c);
            });
        }
    }

    public static void setComponentColour(Component c) {
        if (c instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) c;
            scrollPane.setBackground(GUIConfig.JLABEL_BG_COLOUR);
            scrollPane.getVerticalScrollBar().setOpaque(true);
            scrollPane.getVerticalScrollBar().setBackground(GUIConfig.JLABEL_BG_COLOUR);
            scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {
                @Override
                protected void configureScrollBarColors() {
                    this.thumbColor = GUIConfig.JBUTTON_BG_COLOUR;
                }
            });
        } else if (c instanceof JRadioButton) {
            c.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
            c.setForeground(GUIConfig.JBUTTON_FONT_COLOUR);
        } else if (c instanceof JTable) {
            JTable table = (JTable) c;
            table.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
            table.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
            JViewport parent = (JViewport) table.getParent();
            parent.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
            table.getTableHeader().setOpaque(false);
            table.getTableHeader().setBackground(GUIConfig.JBUTTON_BG_COLOUR);
            table.getTableHeader().setForeground(GUIConfig.JBUTTON_FONT_COLOUR);
        } else if (c instanceof JTabbedPane) {
            JTabbedPane tabbedPane = (JTabbedPane) c;
            int count = tabbedPane.getTabCount();
            tabbedPane.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
            tabbedPane.setForeground(GUIConfig.CONTAINER_BG_COLOUR);
            for (int i = 0; i < count; i++) {
                tabbedPane.setBackgroundAt(i, Color.BLUE);
            }
        } else if (c instanceof JPanel) {
            c.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
        } else if (c instanceof JButton) {
            c.setBackground(GUIConfig.JBUTTON_BG_COLOUR);
            //c.setForeground(GUIConfig.JBUTTON_FONT_COLOUR);
            JButton button = (JButton) c;
            //button.setContentAreaFilled(false);
            button.setUI(new APButtonUI());
            button.setBorder(BorderFactory.createRaisedBevelBorder());
        } else if (c instanceof JToolBar) {
            c.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
        } else if (c instanceof JCheckBox) {
            JCheckBox checkBox = (JCheckBox) c;
            checkBox.setBackground(GUIConfig.JLABEL_BG_COLOUR);
            checkBox.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
            checkBox.setOpaque(true);
        } else if (c instanceof JTextArea) {
            c.setBackground(GUIConfig.JLABEL_BG_COLOUR);
            c.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
        } else if (c instanceof JTextField) {
            c.setBackground(GUIConfig.JLABEL_BG_COLOUR);
            c.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
        } else if (c instanceof JToggleButton) {
            Color oppositeColor = new Color(255 - GUIConfig.JBUTTON_BG_COLOUR.getRed(),
                    255 - GUIConfig.JBUTTON_BG_COLOUR.getGreen(), 255 - GUIConfig.JBUTTON_BG_COLOUR.getBlue());
            if (((JToggleButton) c).isSelected()) {
                c.setBackground(oppositeColor);
            } else {
                c.setBackground(GUIConfig.JBUTTON_BG_COLOUR);
            }
            c.setForeground(GUIConfig.JBUTTON_FONT_COLOUR);
            JToggleButton button = (JToggleButton) c;
            button.setContentAreaFilled(false);
            button.setBorder(BorderFactory.createEtchedBorder());
            ActionListener oldListener;
            if (button.getActionListeners().length != 0) {
                oldListener = button.getActionListeners()[0];
                button.removeActionListener(oldListener);
            }
            button.addActionListener((ActionEvent e) -> {
                JToggleButton source = (JToggleButton) e.getSource();
                if (source.isSelected()) {
                    source.setBackground(oppositeColor);
                } else {
                    source.setBackground(GUIConfig.JBUTTON_BG_COLOUR);
                }
            });
        } else if (c instanceof JList) {
            c.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
            c.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
        } else if (c instanceof JDialog) {
            c.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
            c.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
        } else if (c instanceof JOptionPane) {
            c.setBackground(GUIConfig.CONTAINER_BG_COLOUR);
            c.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
        } else if (c instanceof JLabel) {
            JLabel label = (JLabel) c;
            label.setBackground(GUIConfig.JLABEL_BG_COLOUR);
            label.setForeground(GUIConfig.JLABEL_FONT_COLOUR);
            label.setOpaque(true);
        }
    }

    private void retrieveArtFromDisplateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retrieveArtFromDisplateButtonActionPerformed
        retrieveArtFromDisplateButton.setEnabled(false);
        retrieveArtFromDisplate();
        retrieveArtFromDisplateButton.setEnabled(true);
    }//GEN-LAST:event_retrieveArtFromDisplateButtonActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        System.exit(0);
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void goToDisplateURL() {
        String chromePath = CoreDB.getChromePathFromSettings();
        if (chromePath == null) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Path to Chrome is not set - set that before using this button.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int row = matchesTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "You must select an entry in the matches table to display the Displate page for.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int modelRow = matchesTable.convertRowIndexToModel(row);
        String displateURL = (String) matchesTable.getModel().getValueAt(modelRow, 0);
        try {
            Runtime.getRuntime()
                    .exec(new String[]{chromePath, displateURL});
        } catch (IOException e) {
            LOGGER.error("Failed to open URL!", e);
        }
    }

    private void goToTakedownFormButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToTakedownFormButtonActionPerformed
        goToTakedownFormButton.setEnabled(false);
        goToTakedownForm();
        goToTakedownFormButton.setEnabled(true);
    }//GEN-LAST:event_goToTakedownFormButtonActionPerformed

    private void setChromePathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setChromePathButtonActionPerformed
        setChromePathButton.setSelected(false);
        setChromePath();
        setChromePathButton.setSelected(true);
    }//GEN-LAST:event_setChromePathButtonActionPerformed

    private void goToDisplateURLButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_goToDisplateURLButtonActionPerformed
         goToDisplateURLButton.setEnabled(false);
        goToDisplateURL();
        goToDisplateURLButton.setEnabled(true);
    }//GEN-LAST:event_goToDisplateURLButtonActionPerformed

    private void setChromePath() {
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Executable files", "exe");
        chooser.setFileFilter(filter);
        int returnVal = chooser.showOpenDialog(GUI.getInstance());
        if (returnVal != JFileChooser.APPROVE_OPTION) {
            return;
        }
        String chromePath = chooser.getSelectedFile().getAbsolutePath();
        chromePath = StringUtils.replace(chromePath, "\\", "/");
        DBResponse insertResp = CoreDB.insertIntoTable(DBTable.SETTINGS,
                new String[]{"name", "value"},
                new Object[]{"chromepath", chromePath});
        if (!insertResp.wasSuccessful()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "An error occurred setting the chrome path; check log output.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

    }

    private void goToTakedownForm() {
        String chromePath = CoreDB.getChromePathFromSettings();
        if (chromePath == null) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Path to Chrome is not set - set that before using this button.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try {
            Runtime.getRuntime()
                    .exec(new String[]{chromePath, "https://displate.com/takedown-request"});
        } catch (IOException e) {
            LOGGER.error("Failed to open URL!", e);
        }
    }

    private void findMatches() {
        DisplateComparisonQueue.getInstance().startQueue();
    }

    private void retrieveArtFromDisplate() {
        DisplateQueue.getInstance().startQueue();
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JButton findMatchesButton;
    private javax.swing.JProgressBar findMatchesProgressBar;
    private javax.swing.JButton goToDisplateURLButton;
    private javax.swing.JButton goToTakedownFormButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JMenuBar mainMenuBar;
    private javax.swing.JTable matchesTable;
    private javax.swing.JTable matchesTable1;
    private javax.swing.JLabel matchingImageLabel;
    private javax.swing.JButton retrieveArtFromDisplateButton;
    private javax.swing.JProgressBar retrieveArtProgressBar;
    private javax.swing.JButton setChromePathButton;
    private javax.swing.JTable userImageFilesTable;
    private javax.swing.JTable userImageFilesTable1;
    private javax.swing.JLabel userImageLabel;
    // End of variables declaration//GEN-END:variables
}
