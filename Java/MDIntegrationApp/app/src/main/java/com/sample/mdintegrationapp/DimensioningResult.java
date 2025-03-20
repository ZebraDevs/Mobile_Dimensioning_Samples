package com.sample.mdintegrationapp;

import java.io.Serializable;
import java.util.List;

public class DimensioningResult implements Serializable {
    private String length;
    private String width;
    private String height;
    private String barcode;
    private String dimensionUnit;
    private String lengthStatus;
    private String widthStatus;
    private String heightStatus;
    private String timestamp;
    private List<Boolean> attributes;
    private String image;

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getDimensionUnit() {
        return dimensionUnit;
    }

    public void setDimensionUnit(String dimensionUnit) {
        this.dimensionUnit = dimensionUnit;
    }

    public String getLengthStatus() {
        return lengthStatus;
    }

    public void setLengthStatus(String lengthStatus) {
        this.lengthStatus = lengthStatus;
    }

    public String getWidthStatus() {
        return widthStatus;
    }

    public void setWidthStatus(String widthStatus) {
        this.widthStatus = widthStatus;
    }

    public String getHeightStatus() {
        return heightStatus;
    }

    public void setHeightStatus(String heightStatus) {
        this.heightStatus = heightStatus;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public List<Boolean> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Boolean> attributes) {
        this.attributes = attributes;
    }
}