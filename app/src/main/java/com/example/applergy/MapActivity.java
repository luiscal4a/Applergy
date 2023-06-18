package com.example.applergy;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.wms.BuildConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private MapView map;


    private FirebaseDatabase database;

    private DatabaseReference myObjectRef;

    private Map<BooleanKey, BitmapDrawable> map_icon;

    private String id_restaurant;

    LocationManager mLocationManager;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        database = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();

        myObjectRef = database.getReference("restaurants");

        Intent intent = getIntent();
        id_restaurant = intent.getStringExtra("id_restaurant"); // "message" is the key used to retrieve the string


        bottomNavigationView = findViewById(R.id.bottom_navigator);

        bottomNavigationView.setSelectedItemId(R.id.navigation_restaurant);

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.navigation_restaurant) {
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

        TabLayout tabLayout = findViewById(R.id.top_navigator);

        tabLayout.getTabAt(1).select();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if (position == 0) {
                    startActivity(new Intent(getApplicationContext(), RestaurantTextActivity.class));
                    overridePendingTransition(0, 0);
                } else if (position == 1) {

                    Toast.makeText(getApplicationContext(),"Second tab",
                            Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        map_icon = new HashMap<>();

        // dairy, gluten, vegetarian
        map_icon.put(new BooleanKey(false, false, true), getBitmapDrawable(R.drawable.map_vegetarian, 1));
        map_icon.put(new BooleanKey(false, true, false), getBitmapDrawable(R.drawable.map_gluten, 1));
        map_icon.put(new BooleanKey(false, true, true), getBitmapDrawable(R.drawable.map_gluten_vegetarian, 2));
        map_icon.put(new BooleanKey(true, false, false), getBitmapDrawable(R.drawable.map_dairy, 1));
        map_icon.put(new BooleanKey(true, false, true), getBitmapDrawable(R.drawable.map_dairy_vegetarian, 2));
        map_icon.put(new BooleanKey(true, true, false), getBitmapDrawable(R.drawable.map_dairy_gluten, 2));
        map_icon.put(new BooleanKey(true, true, true), getBitmapDrawable(R.drawable.map_dairy_gluten_vegetarian, 2));




        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);

        Location start = getLastKnownLocation();
        GeoPoint startPoint = new GeoPoint(start.getLatitude(), start.getLongitude()); // coordenadas de Madrid
        map.getController().setZoom(17); // nivel de zoom
        map.getController().setCenter(startPoint);

        myObjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                map.getOverlays().clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Restaurant object = snapshot.getValue(Restaurant.class);

                    GeoPoint startPoint = new GeoPoint(object.getLatitude(), object.getLongitude()); // coordenadas de Madrid
                    Marker marker = new Marker(map);
                    marker.setIcon(map_icon.get(new BooleanKey(object.isDairyFree(), object.isGlutenFree(), object.isVegetarian())));
                    marker.setAnchor(0.01f, 1f);
                    marker.setPosition(startPoint);
                    marker.setTitle(object.getName());
                    marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

                    if (id_restaurant != null && snapshot.getKey().equals(id_restaurant)) {
                        marker.showInfoWindow();
                        map.getController().setCenter(new GeoPoint(object.getLatitude(), object.getLongitude()));
                    }
                    map.getOverlays().add(marker);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that occurs
                // ...
            }
        });
    }

    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        android.location.Location bestLocation = null;
        Location myLocation = null;
        // Obtener la última ubicación conocida
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED) {
            for (String provider : providers) {
                android.location.Location l = mLocationManager.getLastKnownLocation(provider);

                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    // Found best last known location: %s", l);
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }

    private BitmapDrawable getBitmapDrawable(int resource_id, int scale) {
        Drawable customMarkerDrawable = getResources().getDrawable(resource_id);
        int desiredSize = 50 * scale; // Define desired size

        Bitmap customMarkerBitmap = Bitmap.createBitmap(desiredSize, desiredSize, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(customMarkerBitmap);
        customMarkerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        customMarkerDrawable.draw(canvas);

        return new BitmapDrawable(getResources(), customMarkerBitmap);
    }

    class BooleanKey {
        private boolean value1;
        private boolean value2;
        private boolean value3;

        public BooleanKey(boolean value1, boolean value2, boolean value3) {
            this.value1 = value1;
            this.value2 = value2;
            this.value3 = value3;
        }

        // Override equals() and hashCode() methods for proper comparison and hashing of keys

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BooleanKey that = (BooleanKey) o;
            return value1 == that.value1 &&
                    value2 == that.value2 &&
                    value3 == that.value3;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value1, value2, value3);
        }
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