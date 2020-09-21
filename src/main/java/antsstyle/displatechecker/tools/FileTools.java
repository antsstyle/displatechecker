/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.tools;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class FileTools {

    private static final Logger LOGGER = LogManager.getLogger();

    public static void deleteEmptyDirectories(File folder) {
        if (folder.isDirectory()) {
            File[] list = folder.listFiles();
            if (list != null) {
                for (File file : list) {
                    if (file.isDirectory()) {
                        deleteEmptyDirectories(file);
                        if (file != null && file.listFiles().length == 0) {
                            try {
                                Files.delete(Paths.get(file.getAbsolutePath()));
                            } catch (Exception e) {
                                LOGGER.error("Failed to delete directory: " + folder.getAbsolutePath(), e);
                            }
                        }
                    }
                }
            }
        }
    }

    public static Integer getNumberOfFilesInDir(String folderPath, boolean recursive) {
        File rootDir = new File(folderPath);
        return getNumberOfFilesInDir(rootDir, recursive);
    }

    public static ArrayList<File> getFilesInDir(File file, boolean recursive) {
        ArrayList<File> files = new ArrayList<>();
        if (!file.isDirectory()) {
            files.add(file);
            return files;
        }
        File[] fileArray = file.listFiles();
        for (File f : fileArray) {
            if (f.isDirectory() && recursive) {
                files.addAll(getFilesInDir(f, recursive));
            } else if (!f.isDirectory()) {
                files.add(f);
            }
        }
        return files;
    }

    private static Integer getNumberOfFilesInDir(File file, boolean recursive) {
        if (!file.isDirectory()) {
            return 1;
        }
        int currentCount = 0;
        File[] files = file.listFiles();
        for (File f : files) {
            if (f.isDirectory() && recursive) {
                currentCount += getNumberOfFilesInDir(f, recursive);
            } else if (!f.isDirectory()) {
                currentCount++;
            }
        }
        return currentCount;
    }

    /**
     * Gets the MD5 hashcode for a file.
     *
     * @param filepath The filepath to get the MD5 for.
     * @return The MD5 string, or null if an error occurred.
     */
    public static String getFileMD5(String filepath) {
        Path imageFilePath = Paths.get(filepath);
        try {
            HashCode md5 = com.google.common.io.Files.hash(imageFilePath.toFile(), Hashing.md5());
            String md5String = md5.toString();
            return md5String;
        } catch (Exception e) {
            LOGGER.error("Could not get MD5 for filepath: " + filepath, e);
            return null;
        }
    }

    /**
     * Gets the name of a file from the full filepath.
     *
     * @param filePath The full filepath to the file.
     * @return The name of the file, without any preceding folders.
     */
    public static String getFileNameFromPath(String filePath) {
        filePath = StringUtils.replace(filePath, "\\", "/");
        int index = filePath.lastIndexOf("/");
        String filename = filePath.substring(index + 1, filePath.length());
        return filename;
    }

    /**
     * Gets the extension of a given file.
     *
     * @param filePath The full path to the file.
     * @return The extension of the file, including the dot (e.g. ".jpg").
     */
    public static String getFileExtension(String filePath) {
        int index = filePath.lastIndexOf(".");
        return filePath.substring(index, filePath.length());
    }

    /**
     * Returns a list of the file names in the given directory in lastModified order (oldest first).
     *
     * @param dirPath The path of the directory to get file names for.
     * @param sortByNewest If true, returns filenames newest first; if false, oldest first.
     * @return An ArrayList containing the filenames in the directory.
     */
    public static ArrayList<String> getAllFileNamesFromDir(String dirPath, Boolean sortByNewest) {
        ArrayList<String> results = new ArrayList<>();
        File f = new File(dirPath);
        if (!f.exists() || !f.isDirectory()) {
            return results;
        }
        File[] files = f.listFiles();
        if (sortByNewest) {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        } else {
            Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        }
        for (File f1 : files) {
            if (!f1.isDirectory()) {
                results.add(f1.getName());
            }
        }
        return results;
    }

    /**
     * Gets the most recently added file in a directory.
     *
     * @param dirPath The directory to find the latest file for.
     * @return The filepath of the latest file, or null if one could not be found or an error occurred.
     */
    public static String getLatestFileFromDir(String dirPath) {
        Path path = Paths.get(dirPath);
        try {
            Optional<Path> paths = Files.list(path)
                    .filter(f -> !Files.isDirectory(f))
                    .max(Comparator.comparingLong(f -> f.toFile()
                    .lastModified()));
            if (paths.isPresent()) {
                return paths.get().getFileName().toString();
            } else {
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to get latest file from dir ", e);
            return null;
        }
    }

    /**
     * Deletes an image file, and also deletes its resized version if one exists.
     *
     * @param filePath The filepath of the image file to delete.
     * @return True if no error occurred; false otherwise.
     */
    public static boolean deleteImageAndResizedFile(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (Exception e) {
            LOGGER.error("Could not delete image file!", e);
            return false;
        }
        if (filePath.contains("-resized")) {
            try {
                int index1 = filePath.indexOf("-resized");
                String extension = filePath.substring(index1 + 8, filePath.length());
                String originalFilePath = filePath.substring(0, index1)
                        .concat(extension);
                Files.deleteIfExists(Paths.get(originalFilePath));
            } catch (Exception e) {
                LOGGER.error("Error deleting original image file!", e);
                return false;
            }
        } else {
            try {
                int index1 = filePath.lastIndexOf(".");
                String extension = FileTools.getFileExtension(filePath);
                String resizedFilePath = filePath.substring(0, index1).concat("-resized").concat(extension);
                Files.deleteIfExists(Paths.get(resizedFilePath));
            } catch (Exception e) {
                LOGGER.error("Error deleting original image file!", e);
                return false;
            }
        }
        return true;
    }
}
