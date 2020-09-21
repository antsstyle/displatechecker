/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.imagealgorithms.openimaj;

import java.io.File;
import java.util.Arrays;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openimaj.image.pixel.statistics.HistogramModel;
import org.openimaj.image.ImageUtilities;
import org.openimaj.image.MBFImage;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

/**
 *
 * @author Ant
 */
public class OpenIMAJMethods {

    private static final Logger LOGGER = LogManager.getLogger();

    /**
     *
     */
    public static final int COMPARISON_SUCCESS = 0;

    /**
     *
     */
    public static final int COMPARISON_ERROR = -1;

    /**
     *
     */
    public static final int NO_ROWS_LEFT = -2;

    /**
     *
     */
    public static final int THRESHOLD_EXCEEDED = 5;

    /**
     *
     */
    public static final int THRESHOLD_NOT_EXCEEDED = 4;

    private static final HistogramModel model = new HistogramModel(4, 4, 4);

    public static MultidimensionalHistogram computeHistogram(String filePath) {
        MBFImage image;
        try {
            image = ImageUtilities.readMBF(new File(filePath));
            image.normalise();
        } catch (Exception e) {
            LOGGER.error("Failed to read MBFImage from filepath: " + filePath + ", cannot compute histogram.", e);
            return null;
        }
        model.estimateModel(image);
        MultidimensionalHistogram histogram = model.histogram.clone();
        histogram.values = destroyLastBin(histogram.values);
        // Exclude the final bin (which contains the high-range of RGB, i.e. mostly white colours)
        // histogram.set(0.0, 3, 3, 3);
        return histogram;
    }

    private static double[] destroyLastBin(double[] array) {
        return Arrays.copyOf(array, array.length - 1);
    }

}
