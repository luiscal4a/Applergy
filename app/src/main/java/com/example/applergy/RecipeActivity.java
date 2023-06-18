package com.example.applergy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.example.applergy.rest.Recipe;
import com.example.applergy.rest.RecipeService;
import com.example.applergy.rest.Recipes;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RecipeActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private RecyclerView rv_recipes;
    private ImageButton btn_refresh;

    private ArrayList<Recipe> list_recipes;
    private RecipeAdapter recipe_adapter;

    private TinyDB tinyDB;
    private FirebaseAuth mAuth;

    private String API_KEY = "7c84ad14b8a54d099b63d6cd5523a4a3";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe);

        tinyDB = new TinyDB(getApplicationContext());

        mAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigator);

        bottomNavigationView.setSelectedItemId(R.id.navigation_recipe);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_restaurant) {
                    startActivity(new Intent(getApplicationContext(), RestaurantTextActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (item.getItemId() == R.id.navigation_profile) {
                    startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                } else if (itemId == R.id.navigation_recipe) {
                    return true;
                }

                return false;
            }
        });

        rv_recipes = findViewById(R.id.rv_recipes);
        btn_refresh = findViewById(R.id.btn_refresh);

        btn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshRecipes();
            }
        });


        rv_recipes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        list_recipes = new ArrayList<>();

        recipe_adapter = new RecipeAdapter(list_recipes);
        rv_recipes.setAdapter(recipe_adapter);


        ArrayList<Object> arl_object = tinyDB.getListObject("recipes", Recipe.class);

        if (arl_object.size() > 0) {
            for (Object o : arl_object) {
                list_recipes.add((Recipe) o);
            }
            ArrayList<Object> objectList = new ArrayList<>();

            objectList.addAll(list_recipes);

            recipe_adapter.notifyDataSetChanged();
        } else {
           refreshRecipes();
        }
    }

    public void refreshRecipes() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.spoonacular.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RecipeService service = retrofit.create(RecipeService.class);

        service.listRecipes(10, "vegetarian,dairyFree", API_KEY)
                .enqueue(new Callback<Recipes>() {
                    @Override
                    public void onResponse(Call<Recipes> call, Response<Recipes> response) {
                        list_recipes.clear();

                        for (Recipe recipe : response.body().getRecipes()) {
                            list_recipes.add(recipe);
                        }


                        ArrayList<Object> objectList = new ArrayList<>();

                        objectList.addAll(list_recipes);

                        tinyDB.putListObject("recipes", objectList);

                        recipe_adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<Recipes> call, Throwable t) {
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser == null)
            changeActivity();
    }

    public void changeActivity() {
        startActivity(new Intent(getApplicationContext(), SignInActivity.class));
    }

    }
