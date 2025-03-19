package com.sample.mdintegrationapp;

import java.io.Serializable;

public class Attribute implements Serializable {
    private String labelOn;
    private String labelOff;

    public String getLabelOff() {
        return labelOff;
    }

    public void setLabelOff(String labelOff) {
        this.labelOff = labelOff;
    }

    public String getLabelOn() {
        return labelOn;
    }

    public void setLabelOn(String labelOn) {
        this.labelOn = labelOn;
    }
}