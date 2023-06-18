package com.example.applergy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private RecyclerView rv_edit;

    private ArrayList<RestaurantDistance> restaurant_edit;

    private EditAdapter edit_adapter;

    private FirebaseDatabase database;
    private DatabaseReference myObjectRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        database = FirebaseDatabase.getInstance();
        myObjectRef = database.getReference("restaurants");

        mAuth = FirebaseAuth.getInstance();

        bottomNavigationView = findViewById(R.id.bottom_navigator);
        bottomNavigationView.setSelectedItemId(R.id.navigation_profile);

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
                    startActivity(new Intent(getApplicationContext(), RecipeActivity.class));
                    overridePendingTransition(0, 0);
                    return true;
                }

                return false;
            }
        });


        rv_edit = findViewById(R.id.rv_edit);
        rv_edit.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        restaurant_edit = new ArrayList<>();

        edit_adapter = new EditAdapter(restaurant_edit, this, myObjectRef);
        rv_edit.setAdapter(edit_adapter);

        myObjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                restaurant_edit.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Restaurant object = snapshot.getValue(Restaurant.class);

                    if(object.getAuthor_id().equals(mAuth.getCurrentUser().getUid())) {
                        restaurant_edit.add(new RestaurantDistance(
                                object,
                                0,
                                snapshot.getKey()
                        ));
                    }

                }


                edit_adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that occurs
                // ...
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