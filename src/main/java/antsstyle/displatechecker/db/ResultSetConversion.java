/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.db;

import antsstyle.displatechecker.datastructures.DisplateEntry;
import antsstyle.displatechecker.datastructures.DisplateFileInfoEntry;
import java.util.TreeMap;

/**
 *
 * @author antsstyle
 */
public class ResultSetConversion {

    public static DisplateFileInfoEntry getDisplateFileInfoEntry(TreeMap<String, Object> row) {
        DisplateFileInfoEntry entry = new DisplateFileInfoEntry()
                .setId((Integer) row.get("id"))
                .setFilePath((String) row.get("filepath"))
                .setUrl((String) row.get("url"));
        return entry;
    }

    public static DisplateEntry getDisplateEntry(TreeMap<String, Object> row) {
        DisplateEntry entry = new DisplateEntry()
                .setId((Integer) row.get("id"))
                .setDisplateURL((String) row.get("displateurl"))
                .setArtistFilePath((String) row.get("artistfilepath"))
                .setDisplateFilePath((String) row.get("displatefilepath"))
                .setHistogramDiff((Double) row.get("histogramdiff"));
        return entry;
    }

}
