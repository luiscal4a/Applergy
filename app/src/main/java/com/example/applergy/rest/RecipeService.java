package com.example.applergy.rest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RecipeService {
    @GET("recipes/random?")
    Call<Recipes> listRecipes(@Query("number") int number, @Query("tags") String tags, @Query("apiKey") String apiKey);
}