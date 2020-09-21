/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.queues;

import antsstyle.displatechecker.gui.GUI;
import antsstyle.displatechecker.datastructures.DBResponse;
import antsstyle.displatechecker.db.CoreDB;
import antsstyle.displatechecker.db.ResultSetConversion;
import antsstyle.displatechecker.enumerations.DBResponseCode;
import antsstyle.displatechecker.enumerations.DBTable;
import antsstyle.displatechecker.imagealgorithms.openimaj.OpenIMAJMethods;
import antsstyle.displatechecker.tools.ImageTools;
import antsstyle.displatechecker.tools.RegularExpressions;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

/**
 *
 * @author Antsstyle
 */
public class DisplateQueue implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final DisplateQueue queue = new DisplateQueue();
    private static Thread queueThread = new Thread(queue);

    private static final Random random = new Random();

    private static final String DISPLATE_FOLDER_PATH
            = StringUtils.replace(System.getProperty("user.dir").concat("/Displate Images/"), "\\", "/");

    private static WebDriver driver;

    private DisplateQueue() {

    }

    public static DisplateQueue getInstance() {
        return queue;
    }

    protected void interruptQueueThread() {
        if (queueThread.isAlive()) {
            queueThread.interrupt();
        }
    }

    public boolean isRunning() {
        return queueThread.isAlive();
    }

    public void startQueue() {
        if (!queueThread.isAlive()) {
            if (hasFinished) {
                queueThread = new Thread(queue);
                queueThread.setName("Displate Queue Thread");
                queueThread.start();
            } else {
                queueThread.setName("Displate Queue Thread");
                queueThread.start();
            }
            return;
        }
    }

    private boolean hasFinished = false;
    private boolean keepRunning = true;

    @Override
    public void run() {
        File f = new File(DISPLATE_FOLDER_PATH);
        if (!f.exists()) {
            f.mkdir();
        }
        ChromeOptions chromeOptions = new ChromeOptions();
        JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Executable files", "exe");
        chooser.setFileFilter(filter);
        String chromePath = CoreDB.getChromePathFromSettings();
        if (chromePath == null) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Please set the path to Chrome for DisplateChecker to use. This is usually somewhere like "
                    + "\"C:/Program Files/Google/Chrome/Application/chrome.exe\" or similar.",
                    "Set Chrome Path", JOptionPane.INFORMATION_MESSAGE);
            LOGGER.debug("Getting chromepath from filechooser");
            int returnVal = chooser.showOpenDialog(GUI.getInstance());
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            chromePath = chooser.getSelectedFile().getAbsolutePath();
            chromePath = StringUtils.replace(chromePath, "\\", "/");
            DBResponse insertResp = CoreDB.insertIntoTable(DBTable.SETTINGS,
                    new String[]{"name", "value"},
                    new Object[]{"chromepath", chromePath});
            if (!insertResp.wasSuccessful()) {
                LOGGER.error("Failed to insert chrome path into settings table!");
            }
        }

        chromeOptions.setBinary(chromePath);
        LOGGER.debug("Displate queue started.");
        System.setProperty("webdriver.chrome.driver", System.getProperty("user.dir").concat("/chromedriver.exe"));
        driver = new ChromeDriver(chromeOptions);
        driver.manage().window().maximize();

        hasFinished = false;
        ArrayList<String> searchTermsToCheck = new ArrayList<>();
        try {
            List<String> strings = Files.readAllLines(Paths.get(System.getProperty("user.dir").concat("/searchterms.txt")));
            for (String s : strings) {
                searchTermsToCheck.add(StringUtils.replace(s, " ", "%20"));
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "Could not find search terms file - ensure it is in the main DisplateArtChecker folder.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            LOGGER.error("Failed to find search terms.", e);
            return;
        }
        if (searchTermsToCheck.isEmpty()) {
            JOptionPane.showMessageDialog(GUI.getInstance(), "There must be at least one search term in your searchterms.txt file.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Collections.shuffle(searchTermsToCheck);

        GUI.getInstance().setRetrieveArtProgress(0);
        int totalToSearch = searchTermsToCheck.size();
        int totalSearched = 0;
        while (keepRunning) {
            if (searchTermsToCheck.isEmpty()) {
                keepRunning = false;
                continue;
            }
            downloadImagesFromDisplateLink(searchTermsToCheck.get(0));
            try {
                Thread.sleep(random.nextInt(4000) + 1000);
            } catch (Exception e) {

            }
            searchTermsToCheck.remove(0);
            totalSearched++;
            Integer progress = (int) (((totalSearched / (double) totalToSearch)) * 1000);
            GUI.getInstance().setRetrieveArtProgress(progress);
        }
        driver.close();
        hasFinished = true;
        keepRunning = true;
        LOGGER.debug("Displate queue finished.");
        JOptionPane.showMessageDialog(GUI.getInstance(), "Finished retrieving Displate entries successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
        return;
    }

    private void downloadImagesFromDisplateLink(String searchTerm) {
        JavascriptExecutor js = (JavascriptExecutor) driver;
        String displateURL = "https://displate.com/sr-artworks/".concat(searchTerm);

        // launch Chrome and direct it to the Base URL
        driver.get(displateURL);
        int nextScroll;
        int nextWaitTime;
        List<WebElement> elements = driver.findElements(By.className("displate-tile"));
        int matchingElems = getNumMatching(elements);
        int attempts = 0;
        while (matchingElems < 20 && attempts < 8) {
            System.out.println("Matching elements for name \"" + searchTerm + "\": " + matchingElems);
            nextScroll = random.nextInt(300) + 200;
            nextWaitTime = random.nextInt(1500) + 500;
            js.executeScript("window.scrollBy(0, ".concat(String.valueOf(nextScroll).concat(")")));
            try {
                Thread.sleep(nextWaitTime);
            } catch (Exception e) {

            }
            driver.findElements(By.className("displate-tile"));
            matchingElems = getNumMatching(elements);
            attempts++;
        }

        List<WebElement> filteredElements = new ArrayList<>();
        for (WebElement element : elements) {
            WebElement imageElement = element.findElement(By.className("displate-tile__image"));
            String imageLink = imageElement.getAttribute("src");
            if (imageLink != null) {
                filteredElements.add(element);
            }
        }
        for (WebElement element : filteredElements) {
            String id = element.getAttribute("data-item-collection-id");
            String link = "https://displate.com/displate/".concat(id);
            WebElement imageElement = element.findElement(By.className("displate-tile__image"));
            String imageLink = imageElement.getAttribute("src");
            String fileName = imageLink.substring(imageLink.lastIndexOf("/") + 1);
            String fullFilePath = DISPLATE_FOLDER_PATH.concat(fileName);
            if (CoreDB.checkIfDisplateURLInInfoTable(link) == null) {
                if (ImageTools.downloadImageFromSiteImageIO(imageLink, fullFilePath)) {
                    DBResponse insertResponse = CoreDB.insertIntoTable(DBTable.DISPLATEFILEINFO,
                            new String[]{"filepath", "url"},
                            new Object[]{fullFilePath, link});
                    if (!insertResponse.wasSuccessful() && !insertResponse.getStatusCode().equals(DBResponseCode.DUPLICATE_ERROR)) {
                        LOGGER.error("Failed to insert url: " + link + " into DB!");
                    }
                }
            }
            try {
                Thread.sleep(random.nextInt(1000) + 500);
            } catch (Exception e) {

            }
        }

    }

    private static int getNumMatching(List<WebElement> elements) {

        int i = 0;
        for (WebElement e : elements) {
            WebElement e5 = e.findElement(By.className("displate-tile__image"));
            String src = e5.getAttribute("src");
            if (src != null && RegularExpressions.matchesRegex(src, RegularExpressions.DISPLATE_IMAGE_REGEX)) {
                i++;
            }
        }
        return i;

    }

}
