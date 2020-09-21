/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.tools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 *
 * @author Ant
 */
public final class RegularExpressions {

    private static final Logger LOGGER = LogManager.getLogger();
    
    public static final String DISPLATE_IMAGE_REGEX
            = "^https:\\/\\/static\\.displate\\.com\\/[0-9]+[x][0-9]+\\/";

    public static final String VALID_DEADLINE_REGEX
            = "^([1-9][0-9]|[1-9]) (January|February|March|April|May|June|July|August|September|October|November|December"
            + "|Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Oct|Nov|Dec)$";
    
    public static final String TWITTER_STATUS_REGEX
            = "^(http:\\/\\/|https:\\/\\/)?(www\\.)?(twitter\\.com\\/){1}[A-Za-z0-9_]{1,15}(\\/status\\/){1}[0-9]+$";

    public static final String MAINTENANCE_METHOD_REGEX
            = "^m_";

    public static final String AUTOMATIC_MAINTENANCE_METHOD_REGEX
            = "^am_[0-9]+(d|h|m)_((2[0-3][0-5][0-9])|([0-1][0-9][0-5][0-9]))";

    public static final String GRANBLUE_FANTASY_TWITTER_STATUS_REGEX
            = "[A-Za-z0-9]{8}( :Battle ID\\nI need backup\\!\\nLv){1}[A-Za-z0-9 ]+$";

    public static final String GRANBLUE_FANTASY_JP_TWITTER_STATUS_REGEX
            = "[A-Za-z0-9]{8}( :参戦ID\\n参加者募集\\！\\nLv){1}[\\s\\S]+";

    public static final String HH_MM_TIME_REGEX
            = "^([0-1][0-9])|(2[0-3]):[0-5][0-9]$";

    public static final String MULTI_IMAGE_ARTWORK_REGEX
            = "^.*(Image )[0-9]*( of )[0-9]*(\\)).*$";

    public static final String VALID_DIGITAL_PRICE_REGEX
            = "^(([\\$£€](([1-9][0-9]?)|([0]))([\\.](([0-9][1-9])|([1-9][0-9])))?)|(Free))$";

    public static final String VALID_PRINT_PRICES_REGEX
            = "^((([1-9][0-9]?)(x)([1-9][0-9]?)(\\\"))( for )"
            + "([\\$£€](([1-9][0-9]?)|([0]))([\\.](([0-9][1-9])|([1-9][0-9])))?)((, )|$))+";

    public static final String URL_REGEX = "^(?:http(s)?:\\/\\/)?[\\w.-]+(?:\\.[\\w\\.-]+)+[\\w\\-\\._~:\\/?#@!\\$&'\\(\\)\\*\\+,;=.]+$";

    public static final String HEARTHSTONE_DECK_TEXT_REGEX
            = "^(### )([A-Za-z0-9\\-\\_\\! ':,])+(\\n)(# Class: )(Paladin|Rogue|Warrior|Shaman|Mage|Warlock|Druid|Priest|Hunter)"
            + "(\\n)(# Format: (Wild|Standard))(\\n)(#)(\\n)(# ((1|2)(x \\())([0-9]){1,2}(\\) )([A-Za-z0-9\\- ,':\\!])+"
            + "(\\n))+(# )(\\n)([A-Za-z0-9\\+\\=\\-\\/])+(\\n)(# )(\\n)"
            + "(# To use this deck, copy it to your clipboard and create a new deck in Hearthstone)(\\n)?$";

    public static final String VALID_GATEKEEPER_PAYMENT_DATE_RANGE_REGEX
            = "^(0[1-9])|([1-2][1-9])|(3[0-1])\\/(0[1-9])|(1[0-2])\\/[1-9][0-9]{3} - "
            + "(0[1-9])|([1-2][1-9])|(3[0-1])\\/(0[1-9])|(1[0-2])\\/[1-9][0-9]{3}$";

    public static final String VALID_GATEKEEPER_PRICE_RANGE_REGEX
            = "^[\\$£€][1-9][0-9]*(\\.[1-9][0-9])?(\\-[1-9][0-9]*(\\.[1-9][0-9])?)$";

    public static final String ENDS_WITH_NUMBER_SPECIALISED_ART_FEATURE_REGEX
            = ".*[\\#][0-9]+$";

    private RegularExpressions() {

    }

    /**
     * Checks if a given string matches the given regex.
     *
     * @param stringToMatch The string to check for matches.
     * @param regex The regex to use.
     * @return True if the string matches the regex; false otherwise.
     */
    public static boolean matchesRegex(String stringToMatch, String regex) {
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(stringToMatch);
        return m.find();
    }

}
