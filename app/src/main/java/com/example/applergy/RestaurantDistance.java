package com.example.applergy;

public class RestaurantDistance extends Restaurant {
    private double distance;

    private String id;

    public RestaurantDistance(Restaurant restaurant, double distance, String id) {
        super(
                restaurant.getAuthor_id(),
                restaurant.getImg(),
                restaurant.getName(),
                restaurant.isVegetarian(),
                restaurant.isDairyFree(),
                restaurant.isGlutenFree(),
                restaurant.isFeatured(),
                restaurant.getLatitude(),
                restaurant.getLongitude()
        );
        this.distance = distance;
        this.id = id;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}

