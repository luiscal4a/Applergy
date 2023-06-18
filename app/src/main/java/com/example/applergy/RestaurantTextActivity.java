package com.example.applergy;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class RestaurantTextActivity extends AppCompatActivity  implements LocationListener {

    private BottomNavigationView bottomNavigationView;

    private RecyclerView rv_featured;
    private RecyclerView rv_restaurants;

    private Button btn_new_restaurant;
    private ArrayList<RestaurantDistance> restaurant_featured;
    private ArrayList<RestaurantDistance> restaurant_normal;
    private RestaurantAdapter restaurant_adapter;
    private FeaturedAdapter featured_adapter;

    private Bitmap photoBitmap = null;

    private ImageView imv_cover;

    public static final int CAMERA_PERM_CODE = 101;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    LocationManager mLocationManager;
    Uri currentUri;

    Restaurant currentRestaurant;

    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private FirebaseAuth mAuth;

    private DatabaseReference myObjectRef;

    private StorageReference myStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_restaurant_text);


        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        myObjectRef = database.getReference("restaurants");
        myStorageRef = storage.getReference();

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

        rv_featured = findViewById(R.id.rv_featured);
        rv_featured.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rv_restaurants = findViewById(R.id.rv_restaurants);
        rv_restaurants.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        ArrayList<RestaurantDistance> all_restaurants = new ArrayList<RestaurantDistance>();

        restaurant_featured = new ArrayList<>();
        restaurant_normal = new ArrayList<>();

        for (RestaurantDistance restaurant : all_restaurants) {
            if(restaurant.isFeatured())
                restaurant_featured.add(restaurant);
        }

        restaurant_adapter = new RestaurantAdapter(all_restaurants, this);
        rv_restaurants.setAdapter(restaurant_adapter);

        featured_adapter = new FeaturedAdapter(restaurant_featured, this);
        rv_featured.setAdapter(featured_adapter);

        TabLayout tabLayout = findViewById(R.id.top_navigator);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();

                if (position == 0) {
                    Toast.makeText(getApplicationContext(),"First tab",
                            Toast.LENGTH_SHORT).show();
                } else if (position == 1) {
                    startActivity(new Intent(getApplicationContext(), MapActivity.class));
                    overridePendingTransition(0, 0);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        btn_new_restaurant = findViewById(R.id.btn_new_restaurant);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(RestaurantTextActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }

        btn_new_restaurant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                ViewGroup viewGroup = findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_new_restaurant, viewGroup, false);

                EditText et_name = dialogView.findViewById(R.id.et_name);
                EditText et_latitude = dialogView.findViewById(R.id.et_latitude);
                EditText et_longitude = dialogView.findViewById(R.id.et_longitude);

                Button btn_location = dialogView.findViewById(R.id.btn_location);
                ImageButton btn_camera = dialogView.findViewById(R.id.btn_camera);
                ImageButton btn_gallery = dialogView.findViewById(R.id.btn_gallery);

                CheckBox checkbox_vegetarian = dialogView.findViewById(R.id.checkbox_vegetarian);
                CheckBox checkbox_dairyFree = dialogView.findViewById(R.id.checkbox_dairyFree);
                CheckBox checkbox_glutenFree = dialogView.findViewById(R.id.checkbox_glutenFree);

                askCameraPermissions();

                currentUri = null;
                currentRestaurant = null;

                imv_cover = dialogView.findViewById(R.id.imv_cover);


                btn_location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Location locActual = null;
                        if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(RestaurantTextActivity.this,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE);
                        } else {
                            locActual = getLastKnownLocation();
                            et_longitude.setText(locActual.getLongitude()+"");
                            et_latitude.setText(locActual.getLatitude()+"");
                        }
                    }
                });

                btn_camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        openCamera();
                    }
                });

                btn_gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        galleryActivityResultLauncher.launch(galleryIntent);
                    }
                });

                builder.setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("Publish", null)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                                /*Toast.makeText(view.getContext(),"you choose no action for alertbox",
                                        Toast.LENGTH_SHORT).show();*/
                            }
                        });
                AlertDialog alertDialog = builder.create();

                alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        // Set custom click listener for positive button after the dialog is shown
                        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String str_name = et_name.getText().toString();
                                String longitude = et_longitude.getText().toString();
                                String latitude = et_latitude.getText().toString();

                                if(!str_name.equals("") && !longitude.equals("") && !latitude.equals("")) {

                                    currentRestaurant = new Restaurant(
                                            mAuth.getCurrentUser().getUid(),
                                            "",
                                            str_name,
                                            checkbox_vegetarian.isChecked(),
                                            checkbox_dairyFree.isChecked(),
                                            checkbox_glutenFree.isChecked(),
                                            false,
                                            Double.parseDouble(latitude),
                                            Double.parseDouble(longitude)
                                    );

                                    if(currentUri != null) {
                                        Toast.makeText(RestaurantTextActivity.this, "Uploading restaurant to firebase", Toast.LENGTH_LONG).show();
                                        uploadUriToFirebase();
                                    }
                                    else
                                        uploadRestaurantToFirebase();
                                    alertDialog.dismiss();
                                }
                                else
                                    Toast.makeText(RestaurantTextActivity.this, "Please include name and location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                alertDialog.show();
            }
        });

        myObjectRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Location current_loc = getLastKnownLocation();

                all_restaurants.clear();

                restaurant_featured.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Restaurant object = snapshot.getValue(Restaurant.class);

                    Location location_restaurant = new Location("");
                    location_restaurant.setLatitude(object.getLatitude());
                    location_restaurant.setLongitude(object.getLongitude());

                    all_restaurants.add(new RestaurantDistance(
                            object,
                            current_loc.distanceTo(location_restaurant),
                            snapshot.getKey()
                    ));
                }


                for (RestaurantDistance restaurant : all_restaurants) {
                    if(restaurant.isFeatured())
                        restaurant_featured.add(restaurant);
                }

                Collections.sort(all_restaurants, new Comparator<RestaurantDistance>() {
                    @Override
                    public int compare(RestaurantDistance r1, RestaurantDistance r2) {
                        return Double.compare(r1.getDistance(), r2.getDistance());
                    }
                });

                restaurant_adapter.notifyDataSetChanged();
                featured_adapter.notifyDataSetChanged();
                // Perform any necessary updates or UI changes with the updated objectList
                // ...
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle any error that occurs
                // ...
            }
        });
/*
        mLocationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);*/
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d("Latitude","changed");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("Latitude","disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("Latitude","enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("Latitude","status");
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
                    Log.e("Trigger", "warning");
                }
            }
        }
        return bestLocation;
    }

    private void uploadUriToFirebase() {

        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp +"."+getFileExt(currentUri);

        myStorageRef.child("images/"+imageFileName).putFile(currentUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully
                    // Retrieve the download URL
                    myStorageRef.child("images/"+imageFileName).getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        currentRestaurant.setImg(downloadUrl);
                        uploadRestaurantToFirebase();
                        // Use the download URL as needed
                        // For example, you can display it in an ImageView or store it in the database
                    }).addOnFailureListener(exception -> {
                        // Handle any errors while retrieving the download URL
                    });
                })
                .addOnFailureListener(exception -> {
                    // Handle any errors while uploading the image
                });
    }

    private void uploadRestaurantToFirebase() {
        myObjectRef.push().setValue(currentRestaurant)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(RestaurantTextActivity.this, "Restaurant added successfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(RestaurantTextActivity.this, "There was an error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        if(ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
             ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, CAMERA_PERM_CODE);
    }


    Uri image_uri;
    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From the Camera");
        image_uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, image_uri);
        cameraActivityResultLauncher.launch(cameraIntent);
    }

    ActivityResultLauncher<Intent> cameraActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        currentUri = image_uri;
                        Bitmap inputImage = uriToBitmap(image_uri);
                        Bitmap rotated = rotateBitmap(inputImage);
                        imv_cover.setImageBitmap(rotated);
                        currentUri = bitmapToUri(rotated);
                    }
                }
            });

    private Uri bitmapToUri(Bitmap bitmap) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    ActivityResultLauncher<Intent> galleryActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Uri image_uri = result.getData().getData();
                        currentUri = image_uri;
                        imv_cover.setImageURI(image_uri);
                    }
                }
            });


    private Bitmap uriToBitmap(Uri selectedFileUri) {
        try {
            ParcelFileDescriptor parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(selectedFileUri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return  null;
    }


    @SuppressLint("Range")
    public Bitmap rotateBitmap(Bitmap input){
        String[] orientationColumn = {MediaStore.Images.Media.ORIENTATION};
        Cursor cur = getContentResolver().query(image_uri, orientationColumn, null, null, null);
        int orientation = -1;
        if (cur != null && cur.moveToFirst()) {
            orientation = cur.getInt(cur.getColumnIndex(orientationColumn[0]));
        }
        Log.d("tryOrientation",orientation+"");
        Matrix rotationMatrix = new Matrix();
        rotationMatrix.setRotate(orientation);
        Bitmap cropped = Bitmap.createBitmap(input,0,0, input.getWidth(), input.getHeight(), rotationMatrix, true);
        return cropped;
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