package antsstyle.displatechecker.tools;

import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import antsstyle.displatechecker.db.CoreDB;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.common.io.Files;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;
import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.net.ssl.HttpsURLConnection;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.imgscalr.Scalr;
import org.imgscalr.Scalr.Method;

/**
 *
 * @author Ant
 */
public final class ImageTools {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     */
    public static final List<String> IMAGE_FILE_EXTENSIONS
            = Arrays.asList("jpg", "png", "tif", "tiff", "bmp", "jpeg");

    public static String getImageWriterExtString() {
        String s = "";
        for (String i : ImageIO.getWriterFileSuffixes()) {
            s = s.concat(i).concat(", ");
        }
        if (s.length() > 0) {
            s = s.substring(0, s.length() - 2);
        }
        return s;
    }

    public static Dimension getImageDimensions(File file) {
        try (ImageInputStream in = ImageIO.createImageInputStream(file)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    return new Dimension(reader.getWidth(0), reader.getHeight(0));
                } finally {
                    reader.dispose();
                }
            } else {
                LOGGER.error("No image readers available, or all failed.");
                return null;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to determine dimensions for image with path: " + file.getAbsolutePath(), e);
            return null;
        }
    }

    public static BufferedImage convertARGBToRGB(BufferedImage image) {
        BufferedImage newBufferedImage = new BufferedImage(image.getWidth(),
                image.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = newBufferedImage.createGraphics();
        g.drawImage(image, 0, 0, Color.WHITE, null);
        g.dispose();
        return newBufferedImage;
    }

    /**
     * Scales an image to fit it within both the given paneWidth and paneHeight.
     *
     * @param srcImg The image to scale.
     * @param paneWidth The width of the panel this image needs to fit into.
     * @param paneHeight The height of the panel this image needs to fit into.
     * @return A scaled version of the image, with its original aspect ratio, with neither the width or height being greater than the provided paneWidth or paneHeight.
     */
    public static BufferedImage getScaledImage(BufferedImage srcImg, int paneWidth, int paneHeight, Method scalingMethod) {
        int srcWidth = srcImg.getWidth();
        int srcHeight = srcImg.getHeight();
        if (srcWidth <= paneWidth && srcHeight <= paneHeight) {
            return srcImg;
        }
        double widthRatio = (double) srcWidth / (double) paneWidth;
        double heightRatio = (double) srcHeight / (double) paneHeight;
        int newWidth;
        int newHeight;
        if (heightRatio > widthRatio) {
            newHeight = (int) Math.round(srcHeight / heightRatio);
            newWidth = (int) Math.round(srcWidth / heightRatio);
        } else {
            newHeight = (int) Math.round(srcHeight / widthRatio);
            newWidth = (int) Math.round(srcWidth / widthRatio);
        }
        BufferedImage returnImg = Scalr.resize(srcImg, scalingMethod, Scalr.Mode.FIT_EXACT, newWidth, newHeight);
        return returnImg;
    }

    /**
     *
     */
    public static final String FILE_MOVE_ERROR = "FILE_MOVE_ERROR";

    /**
     *
     */
    public static final String FILE_EXISTS_IN_TARGET_DIR_ERROR = "FILE_EXISTS_IN_TARGET_DIR_ERROR";

    /**
     *
     */
    public static final String UNKNOWN_ERROR = "UNKNOWN_ERROR";

    /**
     * Checks if the given file path points to an image file.
     *
     * @param imageURI The file path to check.
     * @return True if this file path ends in a recognised image file extension, false otherwise.
     */
    public static boolean isSupportedImageFile(String imageURI) {
        int dotIndex = imageURI.lastIndexOf(".") + 1;
        if (dotIndex == 0) {
            return false;
        }
        String extension = imageURI.substring(dotIndex, imageURI.length())
                .trim()
                .toLowerCase();
        return IMAGE_FILE_EXTENSIONS.contains(extension);
    }

    public static boolean downloadImageFromSiteImageIO(String imageURL, String fullFilePath) {
        try {
            URL url = new URL(imageURL);
            HttpURLConnection connection = (HttpURLConnection) url
                    .openConnection();
            connection.setRequestProperty(
                    "User-Agent",
                    "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_7_5) AppleWebKit/537.31 (KHTML, like Gecko) Chrome/26.0.1410.65 Safari/537.31");
            BufferedImage saveImage = ImageIO.read(connection.getInputStream());
            ImageIO.write(saveImage, "jpg", new File(fullFilePath));
            return true;
        } catch (Exception e) {
            LOGGER.error("Failed to download file!", e);
            return false;
        }
    }

    /**
     * Downloads an image from the given URL, and saves it to the given filepath.
     *
     * @param imageURL The URL pointing to the image to save.
     * @param fullFilePath The filepath to save the image to.
     * @return True on success, false otherwise.
     */
    public static boolean downloadImageFromSite(String imageURL, String fullFilePath) {
        HttpGet httpGet = new HttpGet(imageURL);
        httpGet
                .setHeader("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.115 Safari/537.36");
        httpGet.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
        httpGet.setHeader("Accept-Language", "en-US,en;q=0.5");
        httpGet.setHeader("Accept-Encoding", "gzip, deflate, br");
        httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        boolean successful;
        try (CloseableHttpClient httpclient = HttpClients.createDefault();
                CloseableHttpResponse response1 = httpclient.execute(httpGet)) {
            HttpEntity entity1 = response1.getEntity();
            try (InputStream is = entity1.getContent();
                    OutputStream os = new FileOutputStream(new File(fullFilePath))) {
                int bufferSize = 65536;
                byte[] byteBuffer = new byte[bufferSize];
                int length2;
                while ((length2 = is.read(byteBuffer)) != -1) {
                    os.write(byteBuffer, 0, length2);
                }
                successful = true;
            } catch (Exception e) {
                LOGGER.error("Failed to download image from webpage!", e);
                successful = false;
            }
            EntityUtils.consume(entity1);
            return successful;
        } catch (Exception e) {
            LOGGER.error("Could not retrieve URL for image source!", e);
            return false;
        }
    }

    public static String getImageAspectRatio(int width, int height) {
        int gcd = getGCD(width, height);
        int w = width / gcd;
        int h = height / gcd;
        String aspectRatio;
        if ((w % 2 == 0 || h % 2 == 0) && (w > 20 || h > 20)) {
            if (w % 2 == 0) {
                w = w / 2;
                double h2 = (double) h / 2.0;
                aspectRatio = String.valueOf(w).concat("x").concat(String.valueOf(h2));
            } else {
                h = h / 2;
                double w2 = (double) w / 2.0;
                aspectRatio = String.valueOf(w2).concat("x").concat(String.valueOf(h));
            }
        } else {
            aspectRatio = String.valueOf(w).concat("x").concat(String.valueOf(h));
        }
        return aspectRatio;
    }

    private static int getGCD(int a, int b) {
        if (b == 0) {
            return a;
        }
        return getGCD(b, a % b);
    }


}
