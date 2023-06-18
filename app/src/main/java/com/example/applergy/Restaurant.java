package com.example.applergy;

import java.io.Serializable;

public class Restaurant implements Serializable {
    private String author_id;
    private String img;
    private String name;
    private boolean vegetarian;
    private boolean dairyFree;
    private boolean glutenFree;
    private boolean featured;
    private double latitude;
    private double longitude;

    public Restaurant() {

    }

    public Restaurant(String author_id, String img, String name, boolean vegetarian, boolean dairyFree, boolean glutenFree, boolean featured, double latitude, double longitude) {
        this.author_id = author_id;
        this.img = img;
        this.name = name;
        this.vegetarian = vegetarian;
        this.dairyFree = dairyFree;
        this.glutenFree = glutenFree;
        this.featured = featured;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVegetarian() {
        return vegetarian;
    }

    public void setVegetarian(boolean vegetarian) {
        this.vegetarian = vegetarian;
    }

    public boolean isDairyFree() {
        return dairyFree;
    }

    public void setDairyFree(boolean dairyFree) {
        this.dairyFree = dairyFree;
    }

    public boolean isGlutenFree() {
        return glutenFree;
    }

    public void setGlutenFree(boolean glutenFree) {
        this.glutenFree = glutenFree;
    }

    public boolean isFeatured() {
        return featured;
    }

    public void setFeatured(boolean featured) {
        this.featured = featured;
    }

    public String getAuthor_id() {
        return author_id;
    }

    public void setAuthor_id(String author_id) {
        this.author_id = author_id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }
}
