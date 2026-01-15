package com.chernykh.moodmovieapp.models;

public class Movie {
    private String title;
    private String description;
    private double ratingImdb;
    private double ratingKp;

    public Movie(String title, String description, double ratingImdb, double ratingKp) {
        this.title = title;
        this.description = description;
        this.ratingImdb = ratingImdb;
        this.ratingKp = ratingKp;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public double getRatingImdb() { return ratingImdb; }
    public double getRatingKp() { return ratingKp; }
}