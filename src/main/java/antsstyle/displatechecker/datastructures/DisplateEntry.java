/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.datastructures;

/**
 *
 * @author Antsstyle
 */
public class DisplateEntry {
    
    private Integer id;
    private String displateURL;
    private String artistURL;
    private Integer diffValue;
    private String artistFilePath;
    private String displateFilePath;
    private Boolean userMatch;
    private Double histogramDiff;

    public Double getHistogramDiff() {
        return histogramDiff;
    }

    public DisplateEntry setHistogramDiff(Double histogramDiff) {
        this.histogramDiff = histogramDiff;
        return this;
    }

    public Boolean getUserMatch() {
        return userMatch;
    }

    public DisplateEntry setUserMatch(Boolean userMatch) {
        this.userMatch = userMatch;
        return this;
    }

    public String getArtistFilePath() {
        return artistFilePath;
    }

    public DisplateEntry setArtistFilePath(String artistFilePath) {
        this.artistFilePath = artistFilePath;
        return this;
    }

    public String getDisplateFilePath() {
        return displateFilePath;
    }

    public DisplateEntry setDisplateFilePath(String displateFilePath) {
        this.displateFilePath = displateFilePath;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public DisplateEntry setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getDisplateURL() {
        return displateURL;
    }

    public DisplateEntry setDisplateURL(String displateURL) {
        this.displateURL = displateURL;
        return this;
    }

    public String getArtistURL() {
        return artistURL;
    }

    public DisplateEntry setArtistURL(String artistURL) {
        this.artistURL = artistURL;
        return this;
    }

    public Integer getDiffValue() {
        return diffValue;
    }

    public DisplateEntry setDiffValue(Integer diffValue) {
        this.diffValue = diffValue;
        return this;
    }
    
}
