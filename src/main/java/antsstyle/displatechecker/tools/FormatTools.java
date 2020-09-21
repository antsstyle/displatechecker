/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.tools;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public class FormatTools {
    
    private static final Logger LOGGER = LogManager.getLogger();
    
    /**
     *
     */
    public static final DecimalFormat THREE_DP = new DecimalFormat("##.###");
    
    /**
     *
     */
    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     *
     */
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    /**
     *
     */
    public static final DateTimeFormatter DTF_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     *
     */
    public static final DateTimeFormatter DTF_DATABASE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     *
     */
    public static final DateTimeFormatter FEATURE_QUEUE_NEXTTIME_FORMATTER = DateTimeFormatter.ofPattern("MMMM dd, HH:mm:ss");
}
