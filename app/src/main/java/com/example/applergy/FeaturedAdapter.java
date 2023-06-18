package com.example.applergy;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class FeaturedAdapter extends RecyclerView.Adapter<FeaturedAdapter.ViewHolder>{

    private ArrayList<RestaurantDistance> list_restaurants;
    private Activity a;

    public FeaturedAdapter(ArrayList<RestaurantDistance> list_restaurants, Activity a) {
        this.list_restaurants = list_restaurants;
        this.a = a;
    }

    @NonNull
    @Override
    public FeaturedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_restaurant_featured, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FeaturedAdapter.ViewHolder holder, int position) {
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
                Intent intent = new Intent(a, MapActivity.class);
                intent.putExtra("id_restaurant", list_restaurants.get(holder.getAdapterPosition()).getId()); // "message" is the key, and the string is the value
                a.startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        return list_restaurants.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_name;
        private Button btn_detail;

        private ImageView imv_dairy, imv_gluten, imv_vegetarian, imv_cover;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tv_name = itemView.findViewById(R.id.tv_name);
            btn_detail = itemView.findViewById(R.id.btn_detail);
            imv_dairy = itemView.findViewById(R.id.imv_dairy);
            imv_gluten = itemView.findViewById(R.id.imv_gluten);
            imv_vegetarian = itemView.findViewById(R.id.imv_vegetarian);
            imv_cover = itemView.findViewById(R.id.imv_cover);
        }
    }
}
