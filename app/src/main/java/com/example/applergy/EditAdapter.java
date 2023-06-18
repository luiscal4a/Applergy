package com.example.applergy;

import static android.content.Context.LOCATION_SERVICE;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditAdapter extends RecyclerView.Adapter<EditAdapter.ViewHolder>{

    private ArrayList<RestaurantDistance> list_restaurants;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private Activity mActivity;

    private LocationManager mLocationManager;

    private DatabaseReference myObjectRef;

    public EditAdapter(ArrayList<RestaurantDistance> list_restaurants, Activity mActivity, DatabaseReference myObjectRef) {
        this.list_restaurants = list_restaurants;
        this.mActivity = mActivity;
        this.myObjectRef = myObjectRef;
    }

    @NonNull
    @Override
    public EditAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_edit, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditAdapter.ViewHolder holder, int position) {
        holder.tv_name.setText(list_restaurants.get(position).getName());

        if(!list_restaurants.get(holder.getAdapterPosition()).isVegetarian())
            holder.imv_vegetarian.setVisibility(View.GONE);
        else
            holder.imv_vegetarian.setVisibility(View.VISIBLE);
        if(!list_restaurants.get(holder.getAdapterPosition()).isGlutenFree())
            holder.imv_gluten.setVisibility(View.GONE);
        else
            holder.imv_gluten.setVisibility(View.VISIBLE);
        if(!list_restaurants.get(holder.getAdapterPosition()).isDairyFree())
            holder.imv_dairy.setVisibility(View.GONE);
        else
            holder.imv_dairy.setVisibility(View.VISIBLE);

        if(URLUtil.isValidUrl(list_restaurants.get(holder.getAdapterPosition()).getImg()))
            Picasso.get()
                    .load(list_restaurants.get(holder.getAdapterPosition()).getImg())
                    .into(holder.imv_cover);
        else
            Picasso.get()
                    .load(R.drawable.applergy_2)
                    .into(holder.imv_cover);


        holder.btn_detail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                ViewGroup viewGroup = holder.btn_detail.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_restaurant, viewGroup, false);

                ImageView imv_cover = dialogView.findViewById(R.id.imv_cover);
                TextView tv_title = dialogView.findViewById(R.id.tv_title);
                ImageView imv_dairy = dialogView.findViewById(R.id.imv_dairy);
                ImageView imv_gluten = dialogView.findViewById(R.id.imv_gluten);
                ImageView imv_vegetarian = dialogView.findViewById(R.id.imv_vegetarian);
                TextView tv_location = dialogView.findViewById(R.id.tv_location);

                tv_title.setText(list_restaurants.get(holder.getAdapterPosition()).getName());
                tv_location.setText(list_restaurants.get(holder.getAdapterPosition()).getLatitude()+ " "+ list_restaurants.get(holder.getAdapterPosition()).getLongitude());

                if(!list_restaurants.get(holder.getAdapterPosition()).isVegetarian())
                    imv_vegetarian.setVisibility(View.GONE);
                if(!list_restaurants.get(holder.getAdapterPosition()).isGlutenFree())
                    imv_gluten.setVisibility(View.GONE);
                if(!list_restaurants.get(holder.getAdapterPosition()).isDairyFree())
                    imv_dairy.setVisibility(View.GONE);

                if(URLUtil.isValidUrl(list_restaurants.get(holder.getAdapterPosition()).getImg()))
                    Picasso.get()
                            .load(list_restaurants.get(holder.getAdapterPosition()).getImg())
                            .into(imv_cover);

                builder.setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton("Close", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });

        holder.btn_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                ViewGroup viewGroup = holder.btn_edit.findViewById(android.R.id.content);
                View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.dialog_restaurant_edit, viewGroup, false);

                EditText et_name = dialogView.findViewById(R.id.et_name);
                EditText et_latitude = dialogView.findViewById(R.id.et_latitude);
                EditText et_longitude = dialogView.findViewById(R.id.et_longitude);

                Button btn_location = dialogView.findViewById(R.id.btn_location);

                CheckBox checkbox_vegetarian = dialogView.findViewById(R.id.checkbox_vegetarian);
                CheckBox checkbox_dairyFree = dialogView.findViewById(R.id.checkbox_dairyFree);
                CheckBox checkbox_glutenFree = dialogView.findViewById(R.id.checkbox_glutenFree);

                et_name.setText(list_restaurants.get(holder.getAdapterPosition()).getName());
                et_latitude.setText(list_restaurants.get(holder.getAdapterPosition()).getLatitude()+"");
                et_longitude.setText(list_restaurants.get(holder.getAdapterPosition()).getLongitude()+"");

                checkbox_vegetarian.setChecked(list_restaurants.get(holder.getAdapterPosition()).isVegetarian());
                checkbox_dairyFree.setChecked(list_restaurants.get(holder.getAdapterPosition()).isDairyFree());
                checkbox_glutenFree.setChecked(list_restaurants.get(holder.getAdapterPosition()).isGlutenFree());

                btn_location.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Location locActual = null;
                        if (ContextCompat.checkSelfPermission(view.getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(mActivity,
                                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    LOCATION_PERMISSION_REQUEST_CODE);
                        } else {
                            // Los permisos ya están otorgados, puedes obtener la ubicación
                            //locActual = getLocation();
                            locActual = getLastKnownLocation();
                            et_longitude.setText(locActual.getLongitude()+"");
                            et_latitude.setText(locActual.getLatitude()+"");
                        }
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

                                    Restaurant currentRestaurant = new Restaurant(
                                            list_restaurants.get(holder.getAdapterPosition()).getAuthor_id(),
                                            "",
                                            str_name,
                                            checkbox_vegetarian.isChecked(),
                                            checkbox_dairyFree.isChecked(),
                                            checkbox_glutenFree.isChecked(),
                                            false,
                                            Double.parseDouble(latitude),
                                            Double.parseDouble(longitude)
                                    );

                                    updateRestaurantInFirebase(new RestaurantDistance(currentRestaurant, 0, list_restaurants.get(holder.getAdapterPosition()).getId()));
                                    alertDialog.dismiss();
                                }
                                else
                                    Toast.makeText(mActivity.getApplicationContext(), "Please include name and location", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                alertDialog.show();
            }
        });

        holder.btn_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setCancelable(false)
                        .setTitle("Are you sure you want to delete this restaurant?")
                        .setPositiveButton("Delete", null)
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
                                deleteRestaurantFromFirebase(list_restaurants.get(holder.getAdapterPosition()).getId());
                                alertDialog.dismiss();
                            }
                        });
                    }
                });

                alertDialog.show();
            }
        });

    }

    private void deleteRestaurantFromFirebase(String id) {
        myObjectRef.child(id).removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(mActivity.getApplicationContext(), "Restaurant deleted", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mActivity.getApplicationContext(), "Error deleting restaurant", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void updateRestaurantInFirebase(RestaurantDistance restaurant) {
        Map<String, Object> updatedData = new HashMap<>();
        updatedData.put("dairyFree", restaurant.isDairyFree());
        updatedData.put("glutenFree", restaurant.isGlutenFree());
        updatedData.put("vegetarian", restaurant.isVegetarian());
        updatedData.put("name", restaurant.getName());
        updatedData.put("latitude", restaurant.getLatitude());
        updatedData.put("longitude", restaurant.getLongitude());

        myObjectRef.child(restaurant.getId()).updateChildren(updatedData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(mActivity.getApplicationContext(), "Restaurant updated", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(mActivity.getApplicationContext(), "Error updating data", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    private Location getLastKnownLocation() {
        mLocationManager = (LocationManager)mActivity.getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        android.location.Location bestLocation = null;
        Location myLocation = null;
        // Obtener la última ubicación conocida
        if (ActivityCompat.checkSelfPermission(mActivity.getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
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
                /*if (bestLocation != null){
                    myLocation = new Location("Luis", "Mi ultima posición", bestLocation.getLatitude(),
                            bestLocation.getLongitude());
                }*/
            }
        }
        return bestLocation;
    }


    @Override
    public int getItemCount() {
        return list_restaurants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private ImageButton btn_detail, btn_edit, btn_delete;

        private ImageView imv_dairy, imv_gluten, imv_vegetarian, imv_cover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            btn_detail = itemView.findViewById(R.id.btn_detail);
            btn_edit = itemView.findViewById(R.id.btn_edit);
            btn_delete = itemView.findViewById(R.id.btn_delete);
            imv_dairy = itemView.findViewById(R.id.imv_dairy);
            imv_gluten = itemView.findViewById(R.id.imv_gluten);
            imv_vegetarian = itemView.findViewById(R.id.imv_vegetarian);
            imv_cover = itemView.findViewById(R.id.imv_cover);
        }
    }
}
