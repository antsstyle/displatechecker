/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.queues;

import antsstyle.displatechecker.db.CoreDB;
import antsstyle.displatechecker.db.ResultSetConversion;
import antsstyle.displatechecker.imagealgorithms.openimaj.OpenIMAJMethods;
import antsstyle.displatechecker.tools.ImageTools;
import antsstyle.displatechecker.datastructures.DBResponse;
import antsstyle.displatechecker.datastructures.DisplateFileInfoEntry;
import antsstyle.displatechecker.enumerations.DBTable;
import antsstyle.displatechecker.gui.GUI;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Semaphore;
import javax.swing.JOptionPane;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

/**
 *
 * @author Antsstyle
 */
public class DisplateComparisonQueue implements Runnable {

    private static final Logger LOGGER = LogManager.getLogger();

    private static final DisplateComparisonQueue queue = new DisplateComparisonQueue();
    private static Thread primaryQueueThread = new Thread(queue);

    private static final ArrayList<Thread> comparatorThreads = new ArrayList<>();
    private static volatile TreeMap<Integer, Integer> comparatorThreadFlags = new TreeMap<>();

    private static volatile Integer totalComparisonsDone = 0;

    private static final Integer THREAD_RUNNING = 1;
    private static final Integer THREAD_STOPPED_SUCCESS = 2;
    private static final Integer THREAD_STOPPED_FAILURE = 3;
    private static final Integer STOP_SIGNAL = 4;
    private static final Integer THREAD_FINISHED_LOADING_RETWEETS = 5;
    private static final Integer THREAD_FINISHED_LOADING_DISPLATES = 6;

    private static final Integer NUM_WORKER_THREADS = 1;

    private static final Semaphore dbLock = new Semaphore(1);

    private static Integer displateCurrentLoadedImages = 0;
    private static final Integer displateMaxConcurrentLoadedImages = -1;
    private static Integer currentLoadedImages = 0;
    private static volatile Boolean allThreadsFinishedLoadingDisplates = false;
    private static volatile Boolean allThreadsFinishedLoadingRetweets = false;
    private static final Integer retweetMaxConcurrentLoadedImages = 5000;

    private static final TreeMap<String, MultidimensionalHistogram> imageHistograms = new TreeMap<>();
    private static final ArrayList<String> filePaths = new ArrayList<>();

    private static final ArrayList<TreeMap<String, Object>> displateDBRows = new ArrayList<>();
    private static final TreeMap<Integer, DisplateFileInfoEntry> displateDataMap = new TreeMap<>();
    private static final TreeMap<Integer, Integer> displateDiffMap = new TreeMap<>();

    private boolean hasFinished = false;
    private boolean keepRunning = true;

    private static final Integer numHighestDiffMatchingThreshold = 2;

    private static final Double lowestHistogramThreshold = 0.1;

    private void acquireDBLock() {
        dbLock.acquireUninterruptibly();
    }

    private void releaseDBLock() {
        dbLock.release();
    }

    private DisplateComparisonQueue() {

    }

    public static DisplateComparisonQueue getInstance() {
        return queue;
    }

    protected void interruptQueueThread() {
        if (primaryQueueThread.isAlive()) {
            primaryQueueThread.interrupt();
        }
    }

    public boolean isRunning() {
        return primaryQueueThread.isAlive();
    }

    public void startQueue() {
        if (!primaryQueueThread.isAlive()) {
            if (hasFinished) {
                primaryQueueThread = new Thread(queue);
                primaryQueueThread.setName("Displate Comparison Queue Thread");
                primaryQueueThread.start();
            } else {
                primaryQueueThread.setName("Displate Comparison Queue Thread");
                primaryQueueThread.start();
            }
            return;
        }
    }

    @Override
    public void run() {
        LOGGER.debug("Displate comparison queue started.");
        hasFinished = false;
        String pathToFolder = System.getProperty("user.dir").concat("/Test Images/");
        File file = new File(pathToFolder);
        File[] files;
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
                filePaths.add(StringUtils.replace(f.getAbsolutePath(), "\\", "/"));
            }
        }
        try {
            ArrayList<TreeMap<String, Object>> displateResp = CoreDB.getDisplateInfoRows();
            if (displateResp.isEmpty()) {
                keepRunning = false;
            }
            if (!comparatorThreads.isEmpty()) {
                comparatorThreads.clear();
            }
            displateDBRows.clear();
            displateDBRows.addAll(displateResp);
        } catch (Exception e) {
            LOGGER.error("Failed to retrieve displate info rows - check log output.", e);
        }
        GUI.getInstance().getFindMatchesProgressBar().setValue(0);
        for (int i = 0; i < NUM_WORKER_THREADS; i++) {
            createThread(i);
        }
        Set<Integer> keys = comparatorThreadFlags.keySet();
        boolean allThreadsStopped = false;
        while (!allThreadsStopped) {
            try {
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            boolean stopped = true;
            for (Integer k : keys) {
                if (!comparatorThreadFlags.get(k).equals(THREAD_STOPPED_SUCCESS) && !comparatorThreadFlags.get(k).equals(THREAD_STOPPED_FAILURE)) {
                    stopped = false;
                }
            }
            if (stopped) {
                allThreadsStopped = true;
            }
        }

        allThreadsFinishedLoadingRetweets = true;
        hasFinished = true;
        keepRunning = true;
        LOGGER.debug("Displate comparison threads finished..");
        JOptionPane.showMessageDialog(GUI.getInstance(), "Finished finding Displate matches successfully.",
                "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    private void loadDisplateData(Integer threadNumber) {
        int progress = 0;
        int rowSize = displateDBRows.size();
        while (comparatorThreadFlags.get(threadNumber).equals(THREAD_RUNNING)) {
            TreeMap<String, Object> row = null;
            boolean noMoreEntries = false;
            boolean entriesFull = false;
            synchronized (displateDBRows) {
                if (displateDBRows.isEmpty()) {
                    noMoreEntries = true;
                } else if (displateCurrentLoadedImages.equals(displateMaxConcurrentLoadedImages)) {
                    entriesFull = true;
                } else {
                    row = displateDBRows.remove(0);
                    synchronized (displateCurrentLoadedImages) {
                        displateCurrentLoadedImages++;
                    }
                }
            }
            if (entriesFull || noMoreEntries) {
                break;
            }
            DisplateFileInfoEntry entry = ResultSetConversion.getDisplateFileInfoEntry(row);
            synchronized (displateDiffMap) {
                displateDiffMap.put(entry.getId(), 0);
            }
            entry.setHistogram(OpenIMAJMethods.computeHistogram(entry.getFilePath()));
            synchronized (displateDataMap) {
                displateDataMap.put(entry.getId(), entry);
            }
            progress++;
            int newProgress = Math.min((progress / rowSize / 3), 33);
            GUI.getInstance().getFindMatchesProgressBar().setValue(newProgress);
        }
        if (comparatorThreadFlags.get(threadNumber).equals(THREAD_RUNNING)) {
            comparatorThreadFlags.put(threadNumber, THREAD_FINISHED_LOADING_DISPLATES);
        }
        GUI.getInstance().getFindMatchesProgressBar().setValue(33);
    }

    private void loadImageData(Integer threadNumber) {
        while (!allThreadsFinishedLoadingDisplates) {
            allThreadsFinishedLoadingDisplates();
            try {
                Thread.sleep(1 * 1000);
            } catch (Exception e) {

            }
        }
        if (comparatorThreadFlags.get(threadNumber).equals(THREAD_FINISHED_LOADING_DISPLATES)) {
            comparatorThreadFlags.put(threadNumber, THREAD_RUNNING);
        } else {
            comparatorThreadFlags.put(threadNumber, THREAD_STOPPED_FAILURE);
            return;
        }
        int progress = 0;
        int rowSize = filePaths.size();
        while (comparatorThreadFlags.get(threadNumber).equals(THREAD_RUNNING)) {
            boolean noMoreEntries = false;
            boolean entriesFull = false;
            String filePath = null;
            synchronized (imageHistograms) {
                if (filePaths.isEmpty()) {
                    noMoreEntries = true;
                } else {
                    filePath = filePaths.remove(0);
                    synchronized (currentLoadedImages) {
                        currentLoadedImages++;
                    }
                }
            }
            if (entriesFull || noMoreEntries) {
                break;
            }
            MultidimensionalHistogram histogram = null;
            try {
                histogram = OpenIMAJMethods.computeHistogram(filePath);
            } catch (Exception e) {
                LOGGER.error("Failed to compute histogram for file with path: " + filePath + " !", e);
            }
            synchronized (imageHistograms) {
                imageHistograms.put(filePath, histogram);
            }
            progress++;
            int newProgress = Math.min(33 + ((progress / rowSize) / 3), 66);
            GUI.getInstance().getFindMatchesProgressBar().setValue(newProgress);
        }
        if (comparatorThreadFlags.get(threadNumber).equals(THREAD_RUNNING)) {
            comparatorThreadFlags.put(threadNumber, THREAD_FINISHED_LOADING_RETWEETS);
        }
        if (comparatorThreadFlags.get(threadNumber).equals(THREAD_RUNNING)) {
            comparatorThreadFlags.put(threadNumber, THREAD_FINISHED_LOADING_RETWEETS);
        }
        GUI.getInstance().getFindMatchesProgressBar().setValue(66);
        return;
    }

    private void allThreadsFinishedLoadingImageData() {
        Set<Integer> keys = comparatorThreadFlags.keySet();
        for (Integer k : keys) {
            if (!comparatorThreadFlags.get(k).equals(THREAD_FINISHED_LOADING_RETWEETS)) {
                if (!allThreadsFinishedLoadingRetweets) {
                    allThreadsFinishedLoadingRetweets = false;
                }
                return;
            }
        }
        allThreadsFinishedLoadingRetweets = true;
    }

    private void allThreadsFinishedLoadingDisplates() {
        Set<Integer> keys = comparatorThreadFlags.keySet();
        for (Integer k : keys) {
            if (!comparatorThreadFlags.get(k).equals(THREAD_FINISHED_LOADING_DISPLATES)) {
                if (!allThreadsFinishedLoadingDisplates) {
                    allThreadsFinishedLoadingDisplates = false;
                }
                return;
            }
        }
        allThreadsFinishedLoadingDisplates = true;
    }

    private void performComparisons(Integer threadNumber) {
        LOGGER.debug("Performing comparisons...");
        while (!allThreadsFinishedLoadingRetweets) {
            allThreadsFinishedLoadingImageData();
            try {
                Thread.sleep(1 * 1000);
            } catch (Exception e) {

            }
        }
        if (comparatorThreadFlags.get(threadNumber).equals(THREAD_FINISHED_LOADING_RETWEETS)) {
            comparatorThreadFlags.put(threadNumber, THREAD_RUNNING);
        } else {
            comparatorThreadFlags.put(threadNumber, THREAD_STOPPED_FAILURE);
            return;
        }
        int progress = 0;
        int rowSize = displateDataMap.size() * imageHistograms.size();
        LOGGER.debug("Thread " + threadNumber + " starting comparisons.");
        String insertQuery = "INSERT INTO DISPLATE (DISPLATEFILEPATH, artistfilepath, displateurl, histogramdiff) "
                + "VALUES (?,?,?,?)";
        ArrayList<Object[]> params = new ArrayList<>();
        while (comparatorThreadFlags.get(threadNumber).equals(THREAD_RUNNING)) {
            DisplateFileInfoEntry entry = null;
            boolean noMoreEntries = false;
            synchronized (displateDataMap) {
                Set<Integer> keySet = displateDataMap.keySet();
                if (keySet.isEmpty()) {
                    noMoreEntries = true;
                } else {
                    Integer[] ids = keySet.toArray(new Integer[keySet.size()]);
                    entry = displateDataMap.get(ids[0]);
                    displateDataMap.remove(ids[0]);
                }
            }
            if (noMoreEntries && entry == null) {
                if (!params.isEmpty()) {
                    acquireDBLock();
                    CoreDB.runParameterisedUpdateBatch(insertQuery, params);
                    releaseDBLock();
                    params.clear();
                }
                break;
            }
            boolean thresholdReached = false;
            Set<String> filePathKeys = imageHistograms.keySet();
            for (String filePath : filePathKeys) {
                synchronized (displateDiffMap) {
                    Integer numHighestDiffs = displateDiffMap.get(entry.getId());
                    if (numHighestDiffs >= numHighestDiffMatchingThreshold) {
                        thresholdReached = true;
                    }
                }
                if (thresholdReached) {
                    acquireDBLock();
                    CoreDB.runParameterisedUpdateBatch(insertQuery, params);
                    releaseDBLock();
                    params.clear();
                    break;
                }

                MultidimensionalHistogram imageHistogram = imageHistograms.get(filePath);
                Double distance = entry.getHistogram().compare(imageHistogram, DoubleFVComparison.EUCLIDEAN);
                if (distance <= lowestHistogramThreshold) {
                    synchronized (displateDiffMap) {
                        Integer numHighestDiffs = displateDiffMap.get(entry.getId());
                        displateDiffMap.put(entry.getId(), numHighestDiffs + 1);
                    }
                }
                if (distance < 1) {
                    params.add(new Object[]{entry.getFilePath(), filePath, entry.getUrl(), distance});
                }

                if (params.size() == 100) {
                    acquireDBLock();
                    CoreDB.runParameterisedUpdateBatch(insertQuery, params);
                    releaseDBLock();
                    params.clear();
                }
                progress++;
                int newProgress = Math.min(66 + ((progress / rowSize) / 3), 100);
                GUI.getInstance().getFindMatchesProgressBar().setValue(newProgress);
            }
        }
        if (comparatorThreadFlags.get(threadNumber).equals(THREAD_RUNNING)) {
            comparatorThreadFlags.put(threadNumber, THREAD_STOPPED_SUCCESS);
        }
        GUI.getInstance().getFindMatchesProgressBar().setValue(100);
        LOGGER.info("Thread " + threadNumber + " finished.");
    }

    /**
     * Creates a new Image Comparator thread for comparing images. The threads work as follows:
     * <p>
     * Firstly, each thread attempts to find the first entry in the Retweets Approval Queue that has not finished being compared with all images in the Image Filter Testing database table. It then
     * finds the next filter testing image that the Retweets Approval Queue image needs to be compared with, and performs the comparison. (Note that only the first image in the Retweet Approval
     * Queue's entry is compared; we ignore additional images both for the sake of speed, and since multi-image tweets can often be filtered relatively accurately by checking only the first image
     * anyway.)
     * <p>
     * If a filter matches, the Retweets Approval Queue image's corresponding entry is deleted from that table and moved to the Retweet Approval Queue Filtered database table, to await user review.
     *
     * @param threadNumber The number of the thread to be created (used by the primary Image Comparator queue thread to manage the threads).
     */
    private void createThread(Integer threadNumber) {
        comparatorThreadFlags.put(threadNumber, THREAD_RUNNING);
        Thread thread = new Thread(() -> {
            Integer flagSet = THREAD_STOPPED_FAILURE;
            Boolean terminatedWithException = false;
            if (displateMaxConcurrentLoadedImages != -1) {
                displateDataMap.clear();
                displateCurrentLoadedImages = 0;
            }
            if (retweetMaxConcurrentLoadedImages != -1) {
                imageHistograms.clear();
                currentLoadedImages = 0;
                allThreadsFinishedLoadingRetweets = false;
            }

            try {
                loadDisplateData(threadNumber);
                if (comparatorThreadFlags.get(threadNumber).equals(STOP_SIGNAL)) {
                    flagSet = THREAD_STOPPED_SUCCESS;
                }
            } catch (Exception e) {
                LOGGER.error("Displate comparator thread " + threadNumber + " failed with exception: ", e);
                comparatorThreadFlags.put(threadNumber, THREAD_STOPPED_FAILURE);
                terminatedWithException = true;
            }
            if (comparatorThreadFlags.get(threadNumber).equals(THREAD_FINISHED_LOADING_DISPLATES)) {
                try {
                    loadImageData(threadNumber);
                    if (comparatorThreadFlags.get(threadNumber).equals(STOP_SIGNAL)) {
                        flagSet = THREAD_STOPPED_SUCCESS;
                    }
                } catch (Exception e) {
                    LOGGER.error("Displate comparator thread " + threadNumber + " failed with exception: ", e);
                    comparatorThreadFlags.put(threadNumber, THREAD_STOPPED_FAILURE);
                    terminatedWithException = true;
                }
            }

            if (comparatorThreadFlags.get(threadNumber).equals(THREAD_FINISHED_LOADING_RETWEETS)) {
                try {
                    performComparisons(threadNumber);
                    if (comparatorThreadFlags.get(threadNumber).equals(STOP_SIGNAL)) {
                        flagSet = THREAD_STOPPED_SUCCESS;
                    }
                } catch (Exception e) {
                    LOGGER.error("Displate comparator thread " + threadNumber + " failed with exception: ", e);
                    comparatorThreadFlags.put(threadNumber, THREAD_STOPPED_FAILURE);
                    terminatedWithException = true;
                }
            }

            if (!terminatedWithException) {
                comparatorThreadFlags.put(threadNumber, flagSet);
            }
        });
        thread.setName("Displate Comparator worker thread no. " + String.valueOf(threadNumber + 1));
        comparatorThreads.add(thread);
        thread.start();
    }

}
