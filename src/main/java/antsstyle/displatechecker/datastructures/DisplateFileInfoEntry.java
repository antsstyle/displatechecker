/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package antsstyle.displatechecker.datastructures;

import org.openimaj.feature.local.list.LocalFeatureList;
import org.openimaj.image.feature.local.keypoints.Keypoint;
import org.openimaj.math.statistics.distribution.MultidimensionalHistogram;

/**
 *
 * @author Antsstyle
 */
public class DisplateFileInfoEntry {
    
    private Integer id;
    private String filePath;
    private String url;
    private LocalFeatureList<Keypoint> keyPoints;
    private String markedRelevant;
    private String keypointsTextFilePath;
    private String keypointsBinaryFilePath;
    private Integer highestDiffValue;
    private MultidimensionalHistogram histogram;

    public MultidimensionalHistogram getHistogram() {
        return histogram;
    }

    public DisplateFileInfoEntry setHistogram(MultidimensionalHistogram histogram) {
        this.histogram = histogram;
        return this;
    }

    public Double getLowestEuclideanDistance() {
        return lowestEuclideanDistance;
    }

    public DisplateFileInfoEntry setLowestEuclideanDistance(Double lowestEuclideanDistance) {
        this.lowestEuclideanDistance = lowestEuclideanDistance;
        return this;
    }
    private Double lowestEuclideanDistance;

    public Integer getHighestDiffValue() {
        return highestDiffValue;
    }

    public DisplateFileInfoEntry setHighestDiffValue(Integer highestDiffValue) {
        this.highestDiffValue = highestDiffValue;
        return this;
    }

    public String getKeypointsBinaryFilePath() {
        return keypointsBinaryFilePath;
    }

    public DisplateFileInfoEntry setKeypointsBinaryFilePath(String keypointsBinaryFilePath) {
        this.keypointsBinaryFilePath = keypointsBinaryFilePath;
        return this;
    }

    public String getKeypointsTextFilePath() {
        return keypointsTextFilePath;
    }

    public DisplateFileInfoEntry setKeypointsTextFilePath(String keypointsTextFilePath) {
        this.keypointsTextFilePath = keypointsTextFilePath;
        return this;
    }

    public String getMarkedRelevant() {
        return markedRelevant;
    }

    public DisplateFileInfoEntry setMarkedRelevant(String markedRelevant) {
        this.markedRelevant = markedRelevant;
        return this;
    }

    public LocalFeatureList<Keypoint> getKeyPoints() {
        return keyPoints;
    }

    public DisplateFileInfoEntry setKeyPoints(LocalFeatureList<Keypoint> keyPoints) {
        this.keyPoints = keyPoints;
        return this;
    }

    public Integer getId() {
        return id;
    }

    public DisplateFileInfoEntry setId(Integer id) {
        this.id = id;
        return this;
    }

    public String getFilePath() {
        return filePath;
    }

    public DisplateFileInfoEntry setFilePath(String filePath) {
        this.filePath = filePath;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public DisplateFileInfoEntry setUrl(String url) {
        this.url = url;
        return this;
    }
    
}
