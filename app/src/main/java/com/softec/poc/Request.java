package com.softec.poc;

import java.util.ArrayList;
import java.util.List;

public class Request {
    private List<Image> images = new ArrayList();
    private List<String> modelMetadataIds = new ArrayList();

    public List<Image> getImages() {
        return images;
    }

    public void setImages(List<Image> images) {
        this.images = images;
    }

    public List<String> getModelMetadataIds() {
        return modelMetadataIds;
    }

    public void setModelMetadataIds(List<String> modelMetadataIds) {
        this.modelMetadataIds = modelMetadataIds;
    }
}
