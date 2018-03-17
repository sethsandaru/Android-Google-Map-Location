package com.sethphat.seth_googlemap_location.Models;

/**
 * Created by sethsandaru on 3/11/18.
 */

public class InfoLocation {
    private String name;
    private String description;
    private String address;
    private int img;

    public InfoLocation(String name, String description, String address, int img) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getImg() {
        return img;
    }

    public void setImg(int img) {
        this.img = img;
    }
}
