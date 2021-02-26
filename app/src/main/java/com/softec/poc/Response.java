package com.softec.poc;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

class Roi {

    private int height;
    private int width;
    private int x;
    private int y;

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }
}

class ImageFields {
    private String id;
    private Roi roi;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Roi getRoi() {
        return roi;
    }

    public void setRoi(Roi roi) {
        this.roi = roi;
    }
}

class Lines {
    private float confidence;
    private Roi roi;
    private String text;

    public float getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public Roi getRoi() {
        return roi;
    }

    public void setRoi(Roi roi) {
        this.roi = roi;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}

class TextFields {
    private String id;
    private List<Lines> lines = new ArrayList<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<Lines> getLines() {
        return lines;
    }

    public void setLines(List<Lines> lines) {
        this.lines = lines;
    }
}

class Pages {
    private List<ImageFields> imageFields;
    private String modelMetadataId;
    private String name;
    private String normalizedImage;
    private String prediction;
    private List<TextFields> textFields = new ArrayList<>();

    public List<ImageFields> getImageFields() {
        return imageFields;
    }

    public void setImageFields(List<ImageFields> imageFields) {
        this.imageFields = imageFields;
    }

    public String getModelMetadataId() {
        return modelMetadataId;
    }

    public void setModelMetadataId(String modelMetadataId) {
        this.modelMetadataId = modelMetadataId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNormalizedImage() {
        return normalizedImage;
    }

    public void setNormalizedImage(String normalizedImage) {
        this.normalizedImage = normalizedImage;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public List<TextFields> getTextFields() {
        return textFields;
    }

    public void setTextFields(List<TextFields> textFields) {
        this.textFields = textFields;
    }
}

public class Response {
    private List<Pages> pages;

    public List<Pages> getPages() {
        return pages;
    }

    public void setPages(List<Pages> pages) {
        this.pages = pages;
    }
}
